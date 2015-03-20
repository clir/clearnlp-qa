package edu.emory.clir.clearnlp.qa.question.arithmetic;

import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.pos.POSLibEn;
import edu.emory.clir.clearnlp.qa.structure.Instance;
import edu.emory.clir.clearnlp.qa.structure.SemanticType;
import edu.emory.clir.clearnlp.qa.structure.document.EnglishDocument;
import edu.emory.clir.clearnlp.qa.util.StringUtils;

import java.util.*;

/**
 * @author: Tomasz Jurczyk ({@code tomasz.jurczyk@emory.edu})
 */

public class ArithmeticQuestion {
    /* Rough part */
    private EnglishDocument document;
    private Instance questionRoot;
    private String questionText;
    private HashMap<Instance,Boolean> visitedInstances = new HashMap();

    public EnglishDocument getDocument()
    {
        return document;
    }

    public void setDocument(EnglishDocument document)
    {
        this.document = document;
    }

    public Instance getQuestionRoot()
    {
        return questionRoot;
    }

    public void setQuestionRoot(Instance questionRoot)
    {
        this.questionRoot = questionRoot;
    }

    public String getQuestionText()
    {
        return questionText;
    }

    public void setQuestionText(String questionText)
    {
        this.questionText = questionText;
    }

    /* Actual part of extracted information */
    private List<String> actors = new ArrayList();
    private List<String> themes = new ArrayList();
    private List<String> attrs  = new ArrayList();
    private List<String> verbs  = new ArrayList();

    public void processQuestion()
    {
        /* Iterate through every instance and look for any verbs */
        List<Instance> verbInstances = new ArrayList();

        for (Instance inst: document.getInstances())
        {
            DEPNode instNode = document.getDEPNode(inst);

            if (POSLibEn.isVerb(instNode.getPOSTag()))
            {
                /* If verb, look for any states under it */
                processVerb(inst);
            }
        }

        /* Process question state */
        processQuestionState(questionRoot);
    }

    private void processVerb(Instance verbInstance)
    {
        DEPNode verbNode = document.getDEPNode(verbInstance);
        System.out.println("Trying to parse verb: " + verbNode.getLemma());
        /* Try to retrieve A0 label */
        Instance actorInstance = null;

        if (verbInstance.getArgumentList(SemanticType.A0) != null
                && verbInstance.getArgumentList(SemanticType.A0).size() == 1)
        {
            actorInstance = verbInstance.getArgumentList(SemanticType.A0).get(0);
        }

        Queue<Instance> queue = new ArrayDeque();
        queue.add(verbInstance);

        while(! queue.isEmpty())
        {
            Instance current = queue.remove();
            DEPNode currentNode = document.getDEPNode(current);
            System.out.println("Working on: " + currentNode.getLemma());

            /* Add all arguments and attributes to the queue if not visited */
            for (Instance inst: current.getArgumentList())
            {
                DEPNode instNode = document.getDEPNode(inst);
                if (! POSLibEn.isVerb(instNode.getPOSTag()) && ! visitedInstances.containsKey(inst))
                {
                    System.out.println("Adding arg node to the q: " + instNode.getLemma());
                    queue.add(inst);
                    visitedInstances.put(inst, true);
                }
            }

            for (Instance inst: current.getAttributeList())
            {
                DEPNode instNode = document.getDEPNode(inst);
                System.out.println("Checking attribute: " + instNode.getLemma());
                if (! POSLibEn.isVerb(instNode.getPOSTag()) && ! visitedInstances.containsKey(inst))
                {
                    System.out.println("Adding attr node to the q: " + instNode.getLemma());
                    queue.add(inst);
                    visitedInstances.put(inst, true);
                }
            }

            /* If this is numerical, extract state */
            if (StringUtils.isInteger(currentNode.getWordForm()) || StringUtils.isDouble(currentNode.getWordForm()))
            {
                processState(verbInstance, actorInstance, current);
            }
        }
    }

    private void processState(Instance verbInstance, Instance actorInstance, Instance numInstance)
    {
        DEPNode verbNode = document.getDEPNode(verbInstance);
        DEPNode numNode = document.getDEPNode(numInstance);
        System.out.println("Will be extracting for num = " + numNode.getWordForm() + ", verb = " + verbNode.getLemma());
    }

    private void processQuestionState(Instance rootInstance)
    {

    }
}
