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
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;
import org.apache.log4j.*;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
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
import java.util.*;

public class App 
{
    /* Necessary data for the experiment */
    HashMap<String, Boolean> WhStrings            = new HashMap();
    List<SemanticType>       semanticRelations    = new ArrayList();
    HashMap<String, Boolean> stopwordMap          = new HashMap();
    List<String>             fieldList            = new ArrayList();
    List<String>             fieldsSettingInQuery = new ArrayList();
    Client                   client;

    /* ClearNLP section */
    private EnglishDocument document;
    AbstractTokenizer tokenizer;
    AbstractComponent[] components = new AbstractComponent[5];
    final String language = AbstractReader.LANG_EN;
    final String modelType  = "general-en";

    /* WordNet */
    WordNetDatabase wDatabase;

    /* Experiment settings */
    final int mode = 1; // 0 for train, 1 for test
    final int N = 40;
    final String ESServerAddress = "127.0.0.1";
    final int ESServerPort = 9300;
    final int ESResultsLimit = 20;
    final String TrainFile = "data/qa5_three-arg-relations_train.txt";
    final String TestFile = "data/qa5_three-arg-relations_test.txt";
    // Answer selection settings
    final int ASMode = -1; // -1 for disabled (default locateAnswer used), 0 for train, 1 for test

    int line = 0;

    /* Settings for model */
    PModel pModel;
    ResultCollector resultCollector;
    double [] lambdas = {0.00231, 0.00300, 0.01308, 0.02597, 0.00318, 0.00553, 0.00170, 0.05347, 1.06923, 0.95373, 1.04458, 1.00000, 1.00000, 1.00000, 1.15622};
    //double [] lambdas = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

    public App()
    {
        resultCollector = new ResultCollector();

        System.setProperty("wordnet.database.dir", "wordnet_dict/");
        wDatabase = WordNetDatabase.getFileInstance();

        String log4jConfPath = "log4j.properties";
        PropertyConfigurator.configure(log4jConfPath);

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

        /* Prepare the ES client and clear the index */
        client = new TransportClient()
                .addTransportAddress(new InetSocketTransportAddress(ESServerAddress, ESServerPort));
        try
        {
            client.admin().indices().delete(new DeleteIndexRequest("index")).actionGet();
        }
        catch (Exception e){}
    }

    public void train()
    {
        for (int i = 0; i < N; i++)
        {
            System.out.println("Starting iteration: " + (i+1));
            parseFile(TrainFile);
        }
    }

    public void test()
    {
        parseFile(TestFile);
    }

    public void parseFile(String filename)
    {
        try {
            System.out.println("HERE");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
            String s;
            DEPTree tree;
            int correctSentenceAnswer = 0;
            String answer = "";


            document = new EnglishDocument();

            while ((s = bufferedReader.readLine()) != null)
            {

                /* Split a line by spaces */
                String[] stringList = s.split("\\s+");

                /* If line is empty or id is '1' then create a new document and clear the index */
                if (s.equals("") || stringList[0].equals("1")) {
                    document = new EnglishDocument();

                    try
                    {
                        client.admin().indices().delete(new DeleteIndexRequest("index")).actionGet();
                    } catch (Exception e){}
                }

                /* If it's a question, parse and collect answer and answer sentence id */
                /* Otherwise it's a sentence, just parse it */
                if (StringUtils.isInteger(stringList[stringList.length - 1]))
                {
                    s = "";
                    correctSentenceAnswer = Integer.parseInt(stringList[stringList.length - 1]);
                    answer = stringList[stringList.length - 2];

                    for (int i = 1; i < stringList.length - 2; i++)
                    {
                        s += stringList[i] + " ";
                    }
                }
                else
                {
                    s = "";
                    for (int i = 1; i < stringList.length; i++)
                    {
                        s += stringList[i] + " ";
                    }
                }

                tree = NLPGetter.toDEPTree(tokenizer.getTokens(s));

                for (AbstractComponent component : components) {
                    component.process(tree);
                }

                document.addInstances(tree);

                /* Add to index */
                Instance root = document.getSentence(document.getSentencesCount() - 1);

                /* Index a sentence if regular or process question */
                if (!isQuestion(tree))
                {
                    processSentence(document.getSentencesCount()-1, s);
                }
                else
                {
                    processQuestionSentence(document.getSentencesCount()-1, s, correctSentenceAnswer, answer);
                }

                line++;
            }

            System.out.println("At the end of iteration, lambdas: ");
            pModel.getWeights();

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void experiment()
    {
        if (mode == 0)
        {
            train();
        }
        else
        {
            test();
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
                "dependency_label_word_pairs: \"" + SR.getDependencyLabelWordPairs() + "\", " +
                "sem_a0: \"" + SR.getA0Label() + "\", " +
                "sem_a1: \"" + SR.getA1Label() + "\", " +
                "sem_a2: \"" + SR.getA2Label() + "\", " +
                "sem_a4: \"" + SR.getA4Label() + "\", " +
                "sem_loc: \"" + SR.getLOCLabel() + "\", " +
                "sem_dir: \"" + SR.getDIRLabel() + "\", " +
                "verb_synonyms: \"" + SR.getVerbSynonyms() + "\"};";

        client.prepareIndex("index", "sentence")
                .setSource(json)
                .execute()
                .actionGet();
    }

    private void processQuestionSentence(int sentenceID, String sentence, int correctAnswerIndex, String answer)
    {
        /* Refresh the index */
        client.admin().indices().prepareRefresh().execute().actionGet();

        HashMap<Integer, HashMap<String, Float>> questionMatrix = new HashMap();
        HashMap<Integer, String> singleRelations = new HashMap();

        resultCollector.addQuestion();

        SentenceRepresentation SR = extractSentence(sentenceID, sentence, true);

        /* Execute all queries and collect their results */
        for (String field: fieldList)
        {
            if (SR.getField(field).equals(""))
            {
                // Empty query (no need to perform)
                continue;
            }

            QueryStringQueryBuilder sq = new QueryStringQueryBuilder(SR.getField(field));
            sq.defaultField(field);

            /* Send a request and retrieve the hits */
            SearchRequestBuilder requestBuilder = client.prepareSearch("index").setTypes("sentence");
            requestBuilder.setSize(ESResultsLimit);
            requestBuilder.addFields(fieldsSettingInQuery.toArray(new String[fieldsSettingInQuery.size()]));
            requestBuilder.setQuery(sq);

            //System.out.println("Executing query of: " + sq);

            SearchResponse searchResponse = requestBuilder.execute().actionGet();

            /* Process response */
            /* If no hits, move to next one */
            if (searchResponse.getHits().getTotalHits() == 0) continue;

            /* Iterate through all hits and collect their scores */
            for (SearchHit s: searchResponse.getHits().getHits())
            {
                int sentence_id = Integer.parseInt((String )s.getFields().get("sentence_id").getValue());
                HashMap<String, Float> sentenceMap;

                if (! questionMatrix.containsKey(sentence_id))
                {
                    sentenceMap = new HashMap();
                    singleRelations.put(sentence_id, (String) s.getFields().get("single_semantic_relations").getValue());
                    questionMatrix.put(sentence_id, sentenceMap);
                }
                else
                {
                    sentenceMap = questionMatrix.get(sentence_id);
                }

                sentenceMap.put(field, s.getScore());
            }
        }

        /* Create a matrix */
        double [][] matrixData = new double[questionMatrix.size()][fieldList.size()+1];
        String [] singleRelationsArray = new String[questionMatrix.size()];
        int    [] sentenceIDArray      = new int[questionMatrix.size()];

        int indexToPass = -1;

        int iterator = 0;
        for (Map.Entry<Integer, HashMap<String, Float>> entry: questionMatrix.entrySet())
        {
            /* Add the text and id */
            singleRelationsArray[iterator] = singleRelations.get(entry.getKey());
            sentenceIDArray[iterator] = entry.getKey();

            /* If this is a correct answer, this will be passed */
            if (entry.getKey() == correctAnswerIndex - 1) indexToPass = iterator;

            int k = 0;
            for (String field: fieldList)
            {
                if (entry.getValue().get(field) != null)
                {
                    matrixData[iterator][k++] = entry.getValue().get(field);
                    //System.out.printf("%5f ", (entry.getValue().get(field)));
                }
                else
                {
                    matrixData[iterator][k++] = 0;
                    //System.out.print("0.00000 ");
                }
            }

            /* Add the distance */
            matrixData[iterator][k] = ((double)(entry.getKey()+1)/(sentenceID));
            //System.out.printf("%5f\n", matrixData[l][k]);

            iterator++;
        }



        // Print matrix
//        System.out.println("sentence = " + sentence);
//        System.out.print("S\t");
//
//        for(int i = 0; i < fieldList.size()+1; i++)
//        {
//            System.out.print("f[" + i + "]\t");
//        }
//
//        System.out.println();
//
//        for (int i = 0; i < matrixData.length; i++)
//        {
//            System.out.print(sentenceIDArray[i] + "\t");
//            for (int j = 0; j < matrixData[i].length; j++)
//            {
//                System.out.printf("%.2f\t", matrixData[i][j]);
//            }
//            System.out.println();
//        }
//
//        if (indexToPass == -1)
//        {
//            notAnsweredQuestions++;
//            return;
//        }


        if (indexToPass == -1)
        {
            System.out.println("For sentence: " + sentence + ", correct index not found, line = " + line);
            System.exit(1);
        }

        resultCollector.processQuestion();

        /* If train mode, make an iteration */
        if (mode == 0)
        {
            pModel.iterate(matrixData, indexToPass);
        }
        else
        {
            int modelOutput = pModel.test(matrixData, indexToPass);

            if (modelOutput == -1)
            {
                //System.out.println("Not selected for question: " + sentence);
            }
            else
            {
                resultCollector.selectQuestion();

                if (ASMode == 0)
                {
                    ASModel asModel = new ASModel(document, sentenceID, sentenceIDArray[modelOutput]);
                    asModel.train();
                }
                else if (ASMode == 1)
                {

                }
                else if (locateAnswer(SR.getSingleSemanticRelations(), singleRelationsArray[modelOutput], answer) == 0)
                {
                    resultCollector.answerQuestion();
                }
                else
                {
                    // Question not answered
                }
            }
        }
    }

    private SentenceRepresentation extractSentence(int sentenceID, String sentence, boolean isQuestion)
    {
        SentenceRepresentation SR = new SentenceRepresentation();

        /* Prepare semantic role labels */
        Instance root = document.getSentence(document.getSentencesCount()-1);
        DEPNode rootNode = document.getDEPNode(root);
        SR.setVerb(rootNode.lemma);
        SR.setText(sentence);

        for (SemanticType semanticType: semanticRelations)
        {
            if (root.getArgumentList(semanticType) == null) continue;

            String singleNodes = "";

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
                    SR.addSingleSemanticNode(semanticType, word);
                    SR.addArgumentWords(word);
                    SR.addSingleSemanticRelations(word + "_" + srlLabel);
                    SR.addDoubleSemanticRelations(word + "_" + srlLabel + "_" + SR.getVerb());
                }
            }

        }

        /* Extract verb synonyms */
        Synset[] synsets = wDatabase.getSynsets(SR.getVerb(), SynsetType.VERB);
        Set<String> synonymsSet = new HashSet();
        String synonyms = "";
        for (int i = 0; i < synsets.length; i++)
        {
            for (String syn : synsets[i].getWordForms()) synonymsSet.add(syn);
        }

        synonyms = String.join(" ", synonymsSet);
        SR.setVerbSynonyms(synonyms);

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

    private int locateAnswer(String qText, String aText, String answer)
    {
        /* Locate the wildcard semantic relation */
        String semanticRelation = null;
        for (String s: qText.split(" "))
        {
            if (s.contains("*"))
            {
                semanticRelation = s;
                break;
            }
        }

        if (semanticRelation == null) return -1;
        semanticRelation = semanticRelation.split("_")[1];

        /* Perform a search of this relation in answer Text */
        String foundString = null;

        for (String s: aText.split(" "))
        {
            if (s.contains(semanticRelation))
            {
                foundString = s;
                break;
            }
        }

        /* Semantic relation not found */
        if (foundString == null) return -1;

        String foundAnswer = foundString.split("_")[0];
        //System.out.println("found Answer = " + foundAnswer + ", answer = " + answer.toLowerCase());
        if (foundAnswer.equals(answer.toLowerCase()))
        {
            return 0;
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

        /* Set WordNet API database location */
        System.setProperty("wordnet.database.dir", "/Users/george/Downloads/WordNet-3.0/dict");

        App app = new App();
        app.initData();
        app.experiment();

        System.out.println("ESResultsLimit: " + app.ESResultsLimit);
        System.out.println("N = " + app.N);
        System.out.println("Features used:");
        int i;
        for (i = 0; i < app.fieldList.size(); i++)
        {
            System.out.println("f[" + i + "]: " + app.fieldList.get(i));
        }

        System.out.println("f[" + i + "]: distance");

        System.out.println(app.resultCollector);
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

        fieldList.add("text");
        fieldList.add("verb");
        fieldList.add("argument_words");
        fieldList.add("single_semantic_relations");
        fieldList.add("double_semantic_relations");
        fieldList.add("dependency_labels");
        fieldList.add("dependency_label_word_pairs");
        fieldList.add("verb_synonyms");
        fieldList.add("sem_a0");
        fieldList.add("sem_a1");
        fieldList.add("sem_a2");
        fieldList.add("sem_a4");
        fieldList.add("sem_dir");
        fieldList.add("sem_loc");

        //pModel = new PModel(fieldList.size() + 1);
        pModel = new PModel(fieldList.size() + 1, lambdas);

        fieldsSettingInQuery.add("text");
        fieldsSettingInQuery.add("sentence_id");
        fieldsSettingInQuery.add("single_semantic_relations");

//        stopwordMap.put("a", true);
//        stopwordMap.put("about", true);
//        stopwordMap.put("above", true);
//        stopwordMap.put("after", true);
//        stopwordMap.put("again", true);
//        stopwordMap.put("against", true);
//        stopwordMap.put("all", true);
//        stopwordMap.put("am", true);
//        stopwordMap.put("an", true);
//        stopwordMap.put("and", true);
//        stopwordMap.put("any", true);
//        stopwordMap.put("are", true);
//        stopwordMap.put("aren't", true);
//        stopwordMap.put("as", true);
//        stopwordMap.put("at", true);
//        stopwordMap.put("be", true);
//        stopwordMap.put("because", true);
//        stopwordMap.put("been", true);
//        stopwordMap.put("before", true);
//        stopwordMap.put("being", true);
//        stopwordMap.put("below", true);
//        stopwordMap.put("between", true);
//        stopwordMap.put("both", true);
//        stopwordMap.put("but", true);
//        stopwordMap.put("by", true);
//        stopwordMap.put("can't", true);
//        stopwordMap.put("cannot", true);
//        stopwordMap.put("could", true);
//        stopwordMap.put("couldn't", true);
//        stopwordMap.put("did", true);
//        stopwordMap.put("didn't", true);
//        stopwordMap.put("do", true);
//        stopwordMap.put("does", true);
//        stopwordMap.put("doesn't", true);
//        stopwordMap.put("doing", true);
//        stopwordMap.put("don't", true);
//        stopwordMap.put("down", true);
//        stopwordMap.put("during", true);
//        stopwordMap.put("each", true);
//        stopwordMap.put("few", true);
//        stopwordMap.put("for", true);
//        stopwordMap.put("from", true);
//        stopwordMap.put("further", true);
//        stopwordMap.put("had", true);
//        stopwordMap.put("hadn't", true);
//        stopwordMap.put("has", true);
//        stopwordMap.put("hasn't", true);
//        stopwordMap.put("have", true);
//        stopwordMap.put("haven't", true);
//        stopwordMap.put("having", true);
//        stopwordMap.put("he", true);
//        stopwordMap.put("he'd", true);
//        stopwordMap.put("he'll", true);
//        stopwordMap.put("he's", true);
//        stopwordMap.put("her", true);
//        stopwordMap.put("here", true);
//        stopwordMap.put("here's", true);
//        stopwordMap.put("hers", true);
//        stopwordMap.put("herself", true);
//        stopwordMap.put("him", true);
//        stopwordMap.put("himself", true);
//        stopwordMap.put("his", true);
//        stopwordMap.put("how", true);
//        stopwordMap.put("how's", true);
//        stopwordMap.put("i", true);
//        stopwordMap.put("i'd", true);
//        stopwordMap.put("i'll", true);
//        stopwordMap.put("i'm", true);
//        stopwordMap.put("i've", true);
//        stopwordMap.put("if", true);
//        stopwordMap.put("in", true);
//        stopwordMap.put("into", true);
//        stopwordMap.put("is", true);
//        stopwordMap.put("isn't", true);
//        stopwordMap.put("it", true);
//        stopwordMap.put("it's", true);
//        stopwordMap.put("its", true);
//        stopwordMap.put("itself", true);
//        stopwordMap.put("let's", true);
//        stopwordMap.put("me", true);
//        stopwordMap.put("more", true);
//        stopwordMap.put("most", true);
//        stopwordMap.put("mustn't", true);
//        stopwordMap.put("my", true);
//        stopwordMap.put("myself", true);
//        stopwordMap.put("no", true);
//        stopwordMap.put("nor", true);
//        stopwordMap.put("not", true);
//        stopwordMap.put("of", true);
//        stopwordMap.put("off", true);
//        stopwordMap.put("on", true);
//        stopwordMap.put("once", true);
//        stopwordMap.put("only", true);
//        stopwordMap.put("or", true);
//        stopwordMap.put("other", true);
//        stopwordMap.put("ought", true);
//        stopwordMap.put("our", true);
//        stopwordMap.put("ours", true);
//        stopwordMap.put("out", true);
//        stopwordMap.put("over", true);
//        stopwordMap.put("own", true);
//        stopwordMap.put("same", true);
//        stopwordMap.put("shan't", true);
//        stopwordMap.put("she", true);
//        stopwordMap.put("she'd", true);
//        stopwordMap.put("she'll", true);
//        stopwordMap.put("she's", true);
//        stopwordMap.put("should", true);
//        stopwordMap.put("shouldn't", true);
//        stopwordMap.put("so", true);
//        stopwordMap.put("some", true);
//        stopwordMap.put("such", true);
//        stopwordMap.put("than", true);
//        stopwordMap.put("that", true);
//        stopwordMap.put("that's", true);
//        stopwordMap.put("the", true);
//        stopwordMap.put("their", true);
//        stopwordMap.put("theirs", true);
//        stopwordMap.put("them", true);
//        stopwordMap.put("themselves", true);
//        stopwordMap.put("then", true);
//        stopwordMap.put("there", true);
//        stopwordMap.put("there's", true);
//        stopwordMap.put("these", true);
//        stopwordMap.put("they", true);
//        stopwordMap.put("they'd", true);
//        stopwordMap.put("they'll", true);
//        stopwordMap.put("they're", true);
//        stopwordMap.put("they've", true);
//        stopwordMap.put("this", true);
//        stopwordMap.put("those", true);
//        stopwordMap.put("through", true);
//        stopwordMap.put("to", true);
//        stopwordMap.put("too", true);
//        stopwordMap.put("under", true);
//        stopwordMap.put("until", true);
//        stopwordMap.put("up", true);
//        stopwordMap.put("very", true);
//        stopwordMap.put("was", true);
//        stopwordMap.put("wasn't", true);
//        stopwordMap.put("we", true);
//        stopwordMap.put("we'd", true);
//        stopwordMap.put("we'll", true);
//        stopwordMap.put("we're", true);
//        stopwordMap.put("we've", true);
//        stopwordMap.put("were", true);
//        stopwordMap.put("weren't", true);
//        stopwordMap.put("what", true);
//        stopwordMap.put("what's", true);
//        stopwordMap.put("when", true);
//        stopwordMap.put("when's", true);
//        stopwordMap.put("where", true);
//        stopwordMap.put("where's", true);
//        stopwordMap.put("which", true);
//        stopwordMap.put("while", true);
//        stopwordMap.put("who", true);
//        stopwordMap.put("who's", true);
//        stopwordMap.put("whom", true);
//        stopwordMap.put("why", true);
//        stopwordMap.put("why's", true);
//        stopwordMap.put("with", true);
//        stopwordMap.put("won't", true);
//        stopwordMap.put("would", true);
//        stopwordMap.put("wouldn't", true);
//        stopwordMap.put("you", true);
//        stopwordMap.put("you'd", true);
//        stopwordMap.put("you'll", true);
//        stopwordMap.put("you're", true);
//        stopwordMap.put("you've", true);
//        stopwordMap.put("your", true);
//        stopwordMap.put("yours", true);
//        stopwordMap.put("yourself", true);
//        stopwordMap.put("yourselves", true);
    }
}
