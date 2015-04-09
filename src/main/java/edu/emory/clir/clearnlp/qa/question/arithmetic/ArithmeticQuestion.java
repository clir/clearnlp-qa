package edu.emory.clir.clearnlp.qa.question.arithmetic;

import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.pos.POSLibEn;
import edu.emory.clir.clearnlp.qa.structure.Instance;
import edu.emory.clir.clearnlp.qa.structure.SemanticType;
import edu.emory.clir.clearnlp.qa.structure.attribute.AttributeType;
import edu.emory.clir.clearnlp.qa.structure.document.EnglishDocument;
import edu.emory.clir.clearnlp.qa.util.StringUtils;

import java.io.Serializable;
import java.util.*;

/**
 * @author: Tomasz Jurczyk ({@code tomasz.jurczyk@emory.edu})
 */

public class ArithmeticQuestion implements Serializable, Comparable<ArithmeticQuestion> {
    private static final long serialVersionUID = 225832398333787185L;
    /* Rough part */
    private transient EnglishDocument document;
    private transient Instance questionRoot;
    private String questionText;
    private transient HashMap<Instance,Boolean> visitedInstances = new HashMap();

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
    private List<String> actors     = new ArrayList();
    private List<String> actorsA2   = new ArrayList();
    private List<String> themes     = new ArrayList();
    private List<String> attrs      = new ArrayList();
    private List<String> verbs      = new ArrayList();
    private List<String> nums       = new ArrayList();

    /* Additional structures for DEPNodes */


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
        /* Try to retrieve A0 label */
        Instance actorInstance      = null;
        Instance actorA2Instance    = null;

        if (verbInstance.getArgumentList(SemanticType.A0) != null
                && verbInstance.getArgumentList(SemanticType.A0).size() == 1)
        {
            actorInstance = verbInstance.getArgumentList(SemanticType.A0).get(0);
        }

        if (verbInstance.getArgumentList(SemanticType.A2) != null
                && verbInstance.getArgumentList(SemanticType.A2).size() == 1)
        {
            actorA2Instance = verbInstance.getArgumentList(SemanticType.A2).get(0);
        }

        Queue<Instance> queue = new ArrayDeque();
        queue.add(verbInstance);

        while(! queue.isEmpty())
        {
            Instance current = queue.remove();
            DEPNode currentNode = document.getDEPNode(current);

            /* Add all arguments and attributes to the queue if not visited */
            for (Instance inst: current.getArgumentList())
            {
                DEPNode instNode = document.getDEPNode(inst);
                if (! POSLibEn.isVerb(instNode.getPOSTag()) && ! visitedInstances.containsKey(inst))
                {
                    queue.add(inst);
                    visitedInstances.put(inst, true);
                }
            }

            for (Instance inst: current.getAttributeList())
            {
                DEPNode instNode = document.getDEPNode(inst);
                if (! POSLibEn.isVerb(instNode.getPOSTag()) && ! visitedInstances.containsKey(inst))
                {
                    queue.add(inst);
                    visitedInstances.put(inst, true);
                }
            }

            /* If this is numerical, extract state */
            if (StringUtils.isInteger(currentNode.getWordForm()) || StringUtils.isDouble(currentNode.getWordForm()))
            {
                processState(verbInstance, actorInstance, actorA2Instance, current);
            }
        }
    }

    private void processState(Instance verbInstance, Instance actorInstance, Instance actorA2Instance,
                              Instance numInstance)
    {
        Instance themeInstance  = null;
        Instance attrInstance   = null;
        DEPNode numNode         = document.getDEPNode(numInstance);

        DEPNode numHeadNode         = numNode.getHead();
        Instance numHeadInstance    = document.getInstance(numHeadNode);

        /* Check if the head is a noun. If so, it is a num-theme relation */
        if (POSLibEn.isNoun(numHeadNode.getPOSTag()))
        {
            themeInstance = document.getInstance(numHeadNode);

            /* Check if there is any attribute */
            if (themeInstance.getAttributeList(AttributeType.QUALITY) != null &&
                    themeInstance.getAttributeList(AttributeType.QUALITY).size() == 1)
            {
                attrInstance = themeInstance.getAttributeList(AttributeType.QUALITY).get(0);
            }
        }
        /* Check if the head is an adjective. Is so, search for a conj relation */
        else if (POSLibEn.isAdjective(numHeadNode.getPOSTag()))
        {
            attrInstance = numHeadInstance;
            if (numHeadInstance.getArgumentList(SemanticType.conj) != null)
            {
                themeInstance = numHeadInstance.getArgumentList(SemanticType.conj).get(0);
            }
        }
        /* Check for prep phrase in num */
//        else if (numInstance.getArgumentList(SemanticType.prep) != null
//                && numInstance.getArgumentList(SemanticType.prep).size() == 1
//                && (themeInstance = findNounInTree(numInstance.getArgumentList(SemanticType.prep).get(0))) != null) {
//        }
        /* Check if A1 to a verb exist, search for a possible theme */
        else if (verbInstance.getArgumentList(SemanticType.A1) != null
                && verbInstance.getArgumentList(SemanticType.A1).size() == 1
                && (themeInstance = findNounInTree(verbInstance.getArgumentList(SemanticType.A1).get(0))) != null) {}
        /* Check if A3 to a verb exist, search for a possible theme */
        else if (verbInstance.getArgumentList(SemanticType.A3) != null
                && verbInstance.getArgumentList(SemanticType.A3).size() == 1
                && (themeInstance = findNounInTree(verbInstance.getArgumentList(SemanticType.A3).get(0))) != null) {}

        verbs.add(document.getDEPNode(verbInstance).getLemma());
        nums.add(document.getDEPNode(numInstance).getWordForm());

        /* If theme is empty, try to look in previous */
        if (themeInstance == null && themes.size() > 0)
        {
            if (! themes.get(themes.size()-1).equals(""))
            {
                themes.add(themes.get(themes.size()-1));
            }
        }
        else
        {
            themes.add(themeInstance != null ? document.getDEPNode(themeInstance).getLemma() : "");
        }

        /* Check if previous does not have theme */
        if (themeInstance != null && themes.size() > 1 && themes.get(themes.size()-2).equals(""))
        {
            themes.set(themes.size()-2, document.getDEPNode(themeInstance).getLemma());
        }

        actors.add(actorInstance != null ? document.getDEPNode(actorInstance).getLemma() : "");
        actorsA2.add(actorA2Instance != null ? document.getDEPNode(actorA2Instance).getLemma() : "");
        attrs.add(attrInstance != null ? document.getDEPNode(attrInstance).getLemma() : "");
    }

    private void processQuestionState(Instance rootInstance)
    {
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Question text: " + questionText + "\n");
        sb.append("Verbs: " + verbs + "\n");
        sb.append("Themes: " + themes + "\n");
        sb.append("Nums: " + nums + "\n");
        sb.append("Attrs: " + attrs + "\n");
        sb.append("Actors: " + actors + "\n");
        sb.append("Actors A2: " + actorsA2 + "\n");

        return sb.toString();
    }

    private Instance findNounInTree(Instance instance)
    {
        Queue<Instance> queue = new ArrayDeque();
        queue.add(instance);

        while(! queue.isEmpty())
        {
            Instance current = queue.poll();
            for (Instance inst: current.getArgumentList())
            {
                queue.add(inst);
            }

            if (POSLibEn.isNoun(document.getDEPNode(current).getPOSTag()))
            {
                return current;
            }
        }

        return null;
    }

    @Override
    public int compareTo(ArithmeticQuestion aq)
    {
        if (this.verbs.size() != aq.verbs.size() || ! equalsTwoLists(this.verbs, aq.verbs)) return -1;
        if (this.actors.size() != aq.actors.size() || ! equalsTwoLists(this.actors, aq.actors)) return -1;
        if (this.actorsA2.size() != aq.actorsA2.size() || ! equalsTwoLists(this.actorsA2, aq.actorsA2)) return -1;
        if (this.themes.size() != aq.themes.size() || ! equalsTwoLists(this.themes, aq.themes)) return -1;
        if (this.attrs.size() != aq.attrs.size() || ! equalsTwoLists(this.attrs, aq.attrs)) return -1;
        if (this.nums.size() != aq.nums.size() || ! equalsTwoLists(this.nums, aq.nums)) return -1;

        return 0;
    }

    private boolean equalsTwoLists(List<String> l1, List<String> l2)
    {
        for(int i = 0; i < l1.size(); i++)
        {
            if (! l1.get(i).equals(l2.get(i))) return false;
        }

        return true;
    }
}
