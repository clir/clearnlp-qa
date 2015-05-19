package edu.emory.clir.clearnlp.qa;

import com.clearnlp.component.AbstractComponent;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.nlp.NLPGetter;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.tokenization.AbstractTokenizer;
import edu.emory.clir.clearnlp.qa.structure.Instance;
import edu.emory.clir.clearnlp.qa.structure.SemanticType;
import edu.emory.clir.clearnlp.qa.structure.document.EnglishDocument;

import com.clearnlp.nlp.*;

import edu.emory.clir.clearnlp.qa.util.StringUtils;
import org.apache.log4j.*;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class App 
{
    final String language = AbstractReader.LANG_EN;
    final String modelType  = "general-en";
    HashMap<String, Boolean> WhStrings = new HashMap();
    List<SemanticType> semanticRelations = new ArrayList();

    private EnglishDocument document;
    AbstractTokenizer tokenizer;
    AbstractComponent[] components = new AbstractComponent[5];
    Client client;

    public App()
    {
        client = new TransportClient()
                .addTransportAddress(new InetSocketTransportAddress("127.0.0.1", 9300));

        semanticRelations.add(SemanticType.A0);
        semanticRelations.add(SemanticType.A1);
        semanticRelations.add(SemanticType.A2);
        semanticRelations.add(SemanticType.A3);
        semanticRelations.add(SemanticType.A4);
        semanticRelations.add(SemanticType.BNF);
        semanticRelations.add(SemanticType.DIR);
        semanticRelations.add(SemanticType.EXT);
        semanticRelations.add(SemanticType.LOC);
        semanticRelations.add(SemanticType.MNR);
        semanticRelations.add(SemanticType.PRP);
        semanticRelations.add(SemanticType.TMP);
        semanticRelations.add(SemanticType.VOC);

        WhStrings.put("what", true);
        WhStrings.put("who", true);
        WhStrings.put("where", true);

        BasicConfigurator.configure();
        try {
            tokenizer = NLPGetter.getTokenizer(language);

            components[0] = NLPGetter.getComponent(modelType, language, NLPMode.MODE_POS);
            components[1] = NLPGetter.getComponent(modelType, language, NLPMode.MODE_DEP);
            components[2] = NLPGetter.getComponent(modelType, language, NLPMode.MODE_PRED);
            components[3] = NLPGetter.getComponent(modelType, language, NLPMode.MODE_ROLE);
            components[4] = NLPGetter.getComponent(modelType, language, NLPMode.MODE_SRL);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void experiment()
    {

        client.admin().indices().delete(new DeleteIndexRequest("index")).actionGet();
        /* Read the input */
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("data/qa1_single-supporting-fact_test.txt"));
            String s;
            DEPTree tree;
            int correctSentenceAnswer = 0;
            String answer = "";

            document = new EnglishDocument();

            System.out.println("HERE");
            while((s = bufferedReader.readLine()) != null)
            {
                if (s.equals(""))
                {
                    //System.out.println("Document parsed to: " + document);
                    document = new EnglishDocument();
                    System.out.println("\n\n\nNew Document\n\n\n");
                    client.admin().indices().delete(new DeleteIndexRequest("index")).actionGet();
                    continue;
                }

                String [] stringList = s.split("\\s+");
                if (StringUtils.isInteger(stringList[stringList.length-1]))
                {
                    /* It's a question */
                    s = "";
                    correctSentenceAnswer = Integer.parseInt(stringList[stringList.length-1]);
                    answer = stringList[stringList.length-2];

                    for (int i = 1; i < stringList.length - 2; i++)
                    {
                        s += stringList[i] + " ";
                    }
                }
                else
                {
                    /* It's a sentence */
                    s = "";
                    for (int i = 1; i < stringList.length; i++)
                    {
                        s += stringList[i] + " ";
                    }
                }

                //System.out.println("Selected s = " + s);

                tree = NLPGetter.toDEPTree(tokenizer.getTokens(s));

                for (AbstractComponent component : components)
                {
                    component.process(tree);
                }

                document.addInstances(tree);

                /* Add to index */
                Instance root = document.getSentence(document.getSentencesCount()-1);

                String query = document.getDEPNode(root).lemma + " ";

                for (SemanticType semanticType: semanticRelations)
                {
                    if (root.getArgumentList(semanticType) == null) continue;

                    for (Instance i: root.getArgumentList(semanticType))
                    {
                        /* Check if this is Wh* word */
                        if (WhStrings.containsKey(document.getDEPNode(i).lemma))
                        {
                            query += "*" + "_" + semanticType.toString() + " ";
                        }
                        else
                        {
                            query += document.getDEPNode(i).lemma + "_" + semanticType.toString() + " ";
                        }
                    }
                }

                /* Add dependency labels */
                for (DEPNode node: document.getDEPNodesFromSentence())
                {
                    query += node.lemma + "_" + node.getLabel() + " ";
                }

                if (! isQuestion(tree))
                {
                    sendPOSTData(query, document.getSentencesCount()-1);
                }
                else
                {
                    System.out.print("For question: " + s + " ");
                    if (performSearch(correctSentenceAnswer, answer, query))
                    {
                        System.out.println("answer HAS BEEN FOUND");
                    }
                    else
                    {
                        System.out.println("answer HAS NOT BEEN FOUND");
                    }
                }


                //System.out.println("Document parsed to: " + document);
                //System.out.println("Query = " + query);
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        client.close();
    }

    private void sendPOSTData(String query, int sentenceID)
    {
        String json = "{" +
                "\"text\":\"" + query + "\"," +
                "\"sentenceID\":\"" + sentenceID + "\"}";

        client.prepareIndex("index", "sentence")
                .setSource(json)
                .execute()
                .actionGet();
    }

    private boolean performSearch(int sentenceID, String answer, String query)
    {
        /* Refresh the index */
        client.admin().indices().prepareRefresh().execute().actionGet();

        /* Build the query to ask */
        QueryStringQueryBuilder sq = new QueryStringQueryBuilder(query);
        System.out.println("\n\nquery = " + sq + ", sentenceID = " + sentenceID);

        /* Send a request and retrieve the hits */
        SearchResponse response = client.prepareSearch("index")
                .setTypes("sentence")
                .setQuery(sq)
                .execute()
                .actionGet();

        SearchHit[] searchHits = response.getHits().getHits();

        System.out.println("Search hit = " + response.getHits().toString());
        return extractAnswer(sentenceID, answer, query, searchHits);

        //int length = searchHits.length;

//        for (int i = 0; i < length; i++) {
//            if (i == 3)
//            {
//                break;
//            }
//            JSONObject obj = new JSONObject(searchHits[i].getSourceAsString());
//            int foundSentenceID = obj.getInt("sentenceID");
//
//            System.out.println("Correct sentenceID = " + sentenceID + ", found = " + (foundSentenceID+1));
//
//            if ((foundSentenceID+1) == sentenceID)
//            {
//                System.out.println("SentenceID correctly identified in hit nr: " + i);
//                return;
//            }
//        }
//
//        System.out.println("SentenceID not recognized");

        //System.out.println("Response hits = " +response.getHits());
    }

    private boolean extractAnswer(int answerSentenceID, String answer, String queryText, SearchHit [] searchHits)
    {
        HashMap<Integer, Boolean> alreadyChecked = new HashMap();

        /* Parse all SearchHits */
        JSONObject [] hitsArray = new JSONObject[searchHits.length];
        for (int i = 0; i < searchHits.length; i++)
        {
            hitsArray[i] = new JSONObject(searchHits[i].getSourceAsString());
        }


        /* Extract semantic relation from query by locating the wildcard item */
        String semanticRelation = null;
        for (String s: queryText.split(" "))
        {
            if (s.contains("*"))
            {
                semanticRelation = s;
                break;
            }
        }

        if (semanticRelation == null) return false;
        semanticRelation = semanticRelation.split("_")[1];

        while (alreadyChecked.size() != hitsArray.length)
        {
            int difference = 0;
            JSONObject candidate = null;

            for (int i = 0; i < hitsArray.length; i++)
            {
                if (candidate == null && ! alreadyChecked.containsKey(hitsArray[i].getInt("sentenceID")))
                {
                    difference = Math.abs(hitsArray[i].getInt("sentenceID") - answerSentenceID);
                    candidate = hitsArray[i];
                }
                else if (Math.abs(hitsArray[i].getInt("sentenceID") - answerSentenceID) < difference &&
                        ! alreadyChecked.containsKey(hitsArray[i].getInt("sentenceID")))
                {
                    difference = Math.abs(hitsArray[i].getInt("sentenceID") - answerSentenceID);
                    candidate = hitsArray[i];
                }
            }

            System.out.println("Looking for answer of semantic Relation: " + semanticRelation + ", in candidate: " + candidate.getString("text") +
                    " that has ID of: " + (candidate.getInt("sentenceID") + 1)  + " correctAnswerSentence ID: " + answerSentenceID);

            alreadyChecked.put(candidate.getInt("sentenceID"), true);
            int searchResult = locateAnswer(semanticRelation, candidate.getString("text"), answer);
            if (searchResult == 1)
            {
                return true;
            }
            else if (searchResult == -1)
            {
                return false;
            }
        }

        return false;
    }

    private int locateAnswer(String semanticRelation, String text, String answer)
    {
        String foundString = null;

        for (String s: text.split(" "))
        {
            if (s.contains(semanticRelation))
            {
                foundString = s;
                break;
            }
        }

        /* Semantic relation not found */
        if (foundString == null) return 0;

        String foundAnswer = foundString.split("_")[0];
        System.out.println("found Answer = " + foundAnswer + ", answer = " + answer.toLowerCase());
        if (foundAnswer.equals(answer.toLowerCase()))
        {
            return 1;
        }
        else
        {
            return -1;
        }
    }

    private boolean isQuestion(DEPTree tree)
    {
        for (DEPNode node: tree)
        {
            if (node.lemma.equals("?"))
            {
                return true;
            }
        }

        return false;
    }

    public static void main( String[] args )
    {
        String log4jConfPath = "log4j.properties";
        PropertyConfigurator.configure(log4jConfPath);
        App app = new App();
        System.out.println("Aaaaaa");
        app.experiment();

    }
}
