package edu.emory.clir.clearnlp.qa;

import com.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.pos.POSLibEn;
import edu.emory.clir.clearnlp.qa.structure.Instance;
import edu.emory.clir.clearnlp.qa.structure.SemanticType;
import edu.emory.clir.clearnlp.qa.structure.document.EnglishDocument;

import java.util.*;

/**
 * @author: Tomasz Jurczyk ({@code tomasz.jurczyk@emory.edu})
 */
public class ASModel {
    private EnglishDocument document;
    private int questionSentenceID;
    private int answerQuestionID;

    public ASModel(EnglishDocument _document, int _questionSentenceID, int _answerQuestionID)
    {
        document = _document;
        questionSentenceID = _questionSentenceID;
        answerQuestionID = _answerQuestionID;
    }

    public void train()
    {
        System.out.println("question sentence id = " + questionSentenceID);
        System.out.println("answer sentence id = " + answerQuestionID);

        extractFeatures();
    }

    public void extractFeatures()
    {
        // Set for question semantic roles
        Set<SemanticType> questionSemanticRoles = new HashSet();

        // Instances and nodes
        Instance questionRoot = document.getSentence(questionSentenceID);
        HashMap<String,HashMap<String,String>> wordMap = new HashMap();
        Instance sentenceRoot = document.getSentence(answerQuestionID);

        // Parse question
        ArrayDeque<Instance> questionSentenceQueue = new ArrayDeque();
        questionSentenceQueue.add(sentenceRoot);

        while(! questionSentenceQueue.isEmpty())
        {
            Instance current = questionSentenceQueue.poll();

            for (SemanticType semanticType: current.getArgumentTypeSet())
            {
                if (Globals.semanticRelations.containsKey(semanticType))
                    questionSemanticRoles.add(semanticType);
            }

            questionSentenceQueue.addAll(current.getArgumentList());
        }

        // Parse question
        ArrayDeque<Instance> answerQueue = new ArrayDeque();
        ArrayDeque<Instance> answerQueueTemp = new ArrayDeque();
        answerQueue.add(sentenceRoot);

        int distance = 0;
        while(! answerQueue.isEmpty())
        {
            Instance current = answerQueue.poll();
            DEPNode depNode = document.getDEPNode(current);

            // Create map for current word
            HashMap<String,String> featureMap = new HashMap();
            wordMap.put(depNode.lemma, featureMap);

            // Add word functions features
            featureMap.put("isNoun", Boolean.toString(POSLibEn.isNoun(depNode.pos)));
            featureMap.put("isVerb", Boolean.toString(POSLibEn.isVerb(depNode.pos)));
            featureMap.put("isAdjective", Boolean.toString(POSLibEn.isAdjective(depNode.pos)));

            // Add syntactic features
            featureMap.put("depNode", depNode.getLabel());

            // Add Semantic features
            Set<SemanticType> answerSemanticRoles = new HashSet();

            String semanticRolesString = "";
            for (SemanticType semanticType: current.getPredicateTypeSet())
            {
                if (Globals.semanticRelations.containsKey(semanticType))
                {
                    semanticRolesString += semanticType.toString() + " ";
                    answerSemanticRoles.add(semanticType);
                }
            }

            if (! answerSemanticRoles.isEmpty()) featureMap.put("semRole", semanticRolesString);

            featureMap.put("sSemRoles", questionSemanticRoles.toString());

            if (! answerSemanticRoles.isEmpty() &&
                    questionSemanticRoles.containsAll(answerSemanticRoles)) featureMap.put("sHasSameRole", "true");
            else featureMap.put("sHasSameRole", "false");

            featureMap.put("distanceToRoot", Integer.toString(distance));

            // Add all children
            answerQueueTemp.addAll(current.getArgumentList());

            if (answerQueue.isEmpty())
            {
                answerQueue = answerQueueTemp;
                answerQueueTemp = new ArrayDeque();
                distance++;
            }
        }

        System.out.println("Word features map size: " + wordMap.size());
        for (Map.Entry<String, HashMap<String,String>> entry: wordMap.entrySet())
        {
            System.out.print("Word: " + entry.getKey() + ", features: " + entry.getValue().toString() + "\n");
        }
    }

    /*
    Set of features
    - isNoun
    - isVerb
    - isAdj
    - semRole
    - depNode
    - sSemRoles
    - sHasSameRole
    - distanceToRoot
     */
}
