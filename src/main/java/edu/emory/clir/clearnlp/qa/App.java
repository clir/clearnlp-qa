package edu.emory.clir.clearnlp.qa;

import com.clearnlp.component.AbstractComponent;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.nlp.NLPGetter;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.tokenization.AbstractTokenizer;
import edu.emory.clir.clearnlp.pos.POSLibEn;
import edu.emory.clir.clearnlp.qa.structure.Instance;
import edu.emory.clir.clearnlp.qa.structure.SemanticType;
import edu.emory.clir.clearnlp.qa.structure.document.EnglishDocument;

import com.clearnlp.nlp.*;

import edu.emory.clir.clearnlp.qa.util.StringUtils;
import org.apache.log4j.*;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
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
    HashMap<String, Boolean> stopwordMap = new HashMap();
    List<String> fieldList = new ArrayList();
    String [] fieldsSettingInQuery = {"text", "verb", "sentence_id", "argument_words",
            "single_semantic_relations", "double_semantic_relations", "dependency_labels",
            "dependency_label_word_pairs"};

    private EnglishDocument document;
    AbstractTokenizer tokenizer;
    AbstractComponent[] components = new AbstractComponent[5];
    Client client;

    public App()
    {
        client = new TransportClient()
                .addTransportAddress(new InetSocketTransportAddress("127.0.0.1", 9300));

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

//                String query = document.getDEPNode(root).lemma + " ";
//
//                for (SemanticType semanticType: semanticRelations)
//                {
//                    if (root.getArgumentList(semanticType) == null) continue;
//
//                    for (Instance i: root.getArgumentList(semanticType))
//                    {
//                        /* Check if this is Wh* word */
//                        if (WhStrings.containsKey(document.getDEPNode(i).lemma))
//                        {
//                            query += "*" + "_" + semanticType.toString() + " ";
//                        }
//                        else
//                        {
//                            query += document.getDEPNode(i).lemma + "_" + semanticType.toString() + " ";
//                        }
//                    }
//                }
//
//                /* Add dependency labels */
//                for (DEPNode node: document.getDEPNodesFromSentence())
//                {
//                    query += node.lemma + "_" + node.getLabel() + " ";
//                }

                if (! isQuestion(tree))
                {
                    processSentence(document.getSentencesCount()-1, s);
                }
                else
                {

                    processQuestionSentence(document.getSentencesCount()-1, s);

//                    System.out.print("For question: " + s + " ");
//                    if (performSearch(correctSentenceAnswer, answer, query))
//                    {
//                        System.out.println("answer HAS BEEN FOUND");
//                    }
//                    else
//                    {
//                        System.out.println("answer HAS NOT BEEN FOUND");
//                    }
                }
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        client.close();
    }

    private void processSentence(int sentenceID, String sentence)
    {
        String json = "{" +
                "\"text\":\"" + sentence + "\", ";

        SentenceRepresentation SR = extractSentence(sentenceID, sentence, false);

        /* Prepare json and index the data */
        json += "verb: \"" + SR.getVerb() + "\", " +
                "sentence_id: \"" + sentenceID + "\", " +
                "argument_words: \"" + SR.getArgumentWords() + "\", " +
                "single_semantic_relations: \"" + SR.getSingleSemanticRelations() + "\", " +
                "double_semantic_relations: \"" + SR.getDoubleSemanticRelations() + "\", " +
                "dependency_labels: \"" + SR.getDependencyLabels() + "\", " +
                "dependency_label_word_pairs: \"" + SR.getDependencyLabelWordPairs() + "\"}";

        client.prepareIndex("index", "sentence")
                .setSource(json)
                .execute()
                .actionGet();
    }

    private void processQuestionSentence(int sentenceID, String sentence)
    {
        /* Refresh the index */
        client.admin().indices().prepareRefresh().execute().actionGet();

        HashMap<Integer, HashMap<String, Double>> questionMatrix = new HashMap();

        SentenceRepresentation SR = extractSentence(sentenceID, sentence, true);

        /* Execute all queries and collect their results */
        for (String field: fieldList)
        {
            System.out.println("Trying to do query for field: " + field + ", with text of: " + SR.getField(field));

            QueryStringQueryBuilder sq = new QueryStringQueryBuilder(SR.getField(field));
            sq.defaultField(field);

            /* Send a request and retrieve the hits */
            SearchRequestBuilder requestBuilder = client.prepareSearch("index").setTypes("sentence");
            requestBuilder.setSize(5);
            requestBuilder.addFields(fieldsSettingInQuery);
            requestBuilder.setQuery(sq);

            System.out.println("Query = " + requestBuilder.toString());
            SearchResponse searchResponse = requestBuilder.execute().actionGet();

            System.out.println("Result = " + searchResponse.toString());
        }
    }

    private SentenceRepresentation extractSentence(int sentenceID, String sentence, boolean isQuestion)
    {
        SentenceRepresentation SR = new SentenceRepresentation();

        /* Prepare semantic role labels */
        Instance root = document.getSentence(document.getSentencesCount()-1);
        DEPNode rootNode = document.getDEPNode(root);
        SR.setVerb(rootNode.lemma);

        for (SemanticType semanticType: semanticRelations)
        {
            if (root.getArgumentList(semanticType) == null) continue;

            for (Instance i: root.getArgumentList(semanticType))
            {
                String word = document.getDEPNode(i).lemma;
                String srlLabel = semanticType.toString();

                if (isQuestion && WhStrings.containsKey(word))
                {
                    SR.addSingleSemanticRelations("*_" + srlLabel);
                    SR.addDoubleSemanticRelations("*_" + srlLabel + "_*");
                }
                else
                {
                    SR.addArgumentWords(word);
                    SR.addSingleSemanticRelations(word + "_" + srlLabel);
                    SR.addDoubleSemanticRelations(word + "_" + srlLabel + "_" + SR.getVerb());
                }
            }
        }

        /* Prepare dependency labels */
        for (DEPNode node: document.getDEPNodesFromSentence())
        {
            if (! stopwordMap.containsKey(node.lemma)
                    && ! node.lemma.equals("_R_")
                    && ! POSLibEn.isPunctuation(node.pos))
            {
                SR.addDependencyLabels(node.getLabel());
                SR.addDependencyLabelWordPairs(node.lemma + "_" + node.getLabel());
            }
        }

        return SR;
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
        app.initData();
        app.experiment();
    }

    public void initData()
    {
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

        fieldList.add("verb");
        fieldList.add("argument_words");
        fieldList.add("single_semantic_relations");
        fieldList.add("double_semantic_relations");
        fieldList.add("dependency_labels");
        fieldList.add("dependency_label_word_pairs");

        stopwordMap.put("a", true);
        stopwordMap.put("about", true);
        stopwordMap.put("above", true);
        stopwordMap.put("after", true);
        stopwordMap.put("again", true);
        stopwordMap.put("against", true);
        stopwordMap.put("all", true);
        stopwordMap.put("am", true);
        stopwordMap.put("an", true);
        stopwordMap.put("and", true);
        stopwordMap.put("any", true);
        stopwordMap.put("are", true);
        stopwordMap.put("aren't", true);
        stopwordMap.put("as", true);
        stopwordMap.put("at", true);
        stopwordMap.put("be", true);
        stopwordMap.put("because", true);
        stopwordMap.put("been", true);
        stopwordMap.put("before", true);
        stopwordMap.put("being", true);
        stopwordMap.put("below", true);
        stopwordMap.put("between", true);
        stopwordMap.put("both", true);
        stopwordMap.put("but", true);
        stopwordMap.put("by", true);
        stopwordMap.put("can't", true);
        stopwordMap.put("cannot", true);
        stopwordMap.put("could", true);
        stopwordMap.put("couldn't", true);
        stopwordMap.put("did", true);
        stopwordMap.put("didn't", true);
        stopwordMap.put("do", true);
        stopwordMap.put("does", true);
        stopwordMap.put("doesn't", true);
        stopwordMap.put("doing", true);
        stopwordMap.put("don't", true);
        stopwordMap.put("down", true);
        stopwordMap.put("during", true);
        stopwordMap.put("each", true);
        stopwordMap.put("few", true);
        stopwordMap.put("for", true);
        stopwordMap.put("from", true);
        stopwordMap.put("further", true);
        stopwordMap.put("had", true);
        stopwordMap.put("hadn't", true);
        stopwordMap.put("has", true);
        stopwordMap.put("hasn't", true);
        stopwordMap.put("have", true);
        stopwordMap.put("haven't", true);
        stopwordMap.put("having", true);
        stopwordMap.put("he", true);
        stopwordMap.put("he'd", true);
        stopwordMap.put("he'll", true);
        stopwordMap.put("he's", true);
        stopwordMap.put("her", true);
        stopwordMap.put("here", true);
        stopwordMap.put("here's", true);
        stopwordMap.put("hers", true);
        stopwordMap.put("herself", true);
        stopwordMap.put("him", true);
        stopwordMap.put("himself", true);
        stopwordMap.put("his", true);
        stopwordMap.put("how", true);
        stopwordMap.put("how's", true);
        stopwordMap.put("i", true);
        stopwordMap.put("i'd", true);
        stopwordMap.put("i'll", true);
        stopwordMap.put("i'm", true);
        stopwordMap.put("i've", true);
        stopwordMap.put("if", true);
        stopwordMap.put("in", true);
        stopwordMap.put("into", true);
        stopwordMap.put("is", true);
        stopwordMap.put("isn't", true);
        stopwordMap.put("it", true);
        stopwordMap.put("it's", true);
        stopwordMap.put("its", true);
        stopwordMap.put("itself", true);
        stopwordMap.put("let's", true);
        stopwordMap.put("me", true);
        stopwordMap.put("more", true);
        stopwordMap.put("most", true);
        stopwordMap.put("mustn't", true);
        stopwordMap.put("my", true);
        stopwordMap.put("myself", true);
        stopwordMap.put("no", true);
        stopwordMap.put("nor", true);
        stopwordMap.put("not", true);
        stopwordMap.put("of", true);
        stopwordMap.put("off", true);
        stopwordMap.put("on", true);
        stopwordMap.put("once", true);
        stopwordMap.put("only", true);
        stopwordMap.put("or", true);
        stopwordMap.put("other", true);
        stopwordMap.put("ought", true);
        stopwordMap.put("our", true);
        stopwordMap.put("ours", true);
        stopwordMap.put("out", true);
        stopwordMap.put("over", true);
        stopwordMap.put("own", true);
        stopwordMap.put("same", true);
        stopwordMap.put("shan't", true);
        stopwordMap.put("she", true);
        stopwordMap.put("she'd", true);
        stopwordMap.put("she'll", true);
        stopwordMap.put("she's", true);
        stopwordMap.put("should", true);
        stopwordMap.put("shouldn't", true);
        stopwordMap.put("so", true);
        stopwordMap.put("some", true);
        stopwordMap.put("such", true);
        stopwordMap.put("than", true);
        stopwordMap.put("that", true);
        stopwordMap.put("that's", true);
        stopwordMap.put("the", true);
        stopwordMap.put("their", true);
        stopwordMap.put("theirs", true);
        stopwordMap.put("them", true);
        stopwordMap.put("themselves", true);
        stopwordMap.put("then", true);
        stopwordMap.put("there", true);
        stopwordMap.put("there's", true);
        stopwordMap.put("these", true);
        stopwordMap.put("they", true);
        stopwordMap.put("they'd", true);
        stopwordMap.put("they'll", true);
        stopwordMap.put("they're", true);
        stopwordMap.put("they've", true);
        stopwordMap.put("this", true);
        stopwordMap.put("those", true);
        stopwordMap.put("through", true);
        stopwordMap.put("to", true);
        stopwordMap.put("too", true);
        stopwordMap.put("under", true);
        stopwordMap.put("until", true);
        stopwordMap.put("up", true);
        stopwordMap.put("very", true);
        stopwordMap.put("was", true);
        stopwordMap.put("wasn't", true);
        stopwordMap.put("we", true);
        stopwordMap.put("we'd", true);
        stopwordMap.put("we'll", true);
        stopwordMap.put("we're", true);
        stopwordMap.put("we've", true);
        stopwordMap.put("were", true);
        stopwordMap.put("weren't", true);
        stopwordMap.put("what", true);
        stopwordMap.put("what's", true);
        stopwordMap.put("when", true);
        stopwordMap.put("when's", true);
        stopwordMap.put("where", true);
        stopwordMap.put("where's", true);
        stopwordMap.put("which", true);
        stopwordMap.put("while", true);
        stopwordMap.put("who", true);
        stopwordMap.put("who's", true);
        stopwordMap.put("whom", true);
        stopwordMap.put("why", true);
        stopwordMap.put("why's", true);
        stopwordMap.put("with", true);
        stopwordMap.put("won't", true);
        stopwordMap.put("would", true);
        stopwordMap.put("wouldn't", true);
        stopwordMap.put("you", true);
        stopwordMap.put("you'd", true);
        stopwordMap.put("you'll", true);
        stopwordMap.put("you're", true);
        stopwordMap.put("you've", true);
        stopwordMap.put("your", true);
        stopwordMap.put("yours", true);
        stopwordMap.put("yourself", true);
        stopwordMap.put("yourselves", true);
    }
}
