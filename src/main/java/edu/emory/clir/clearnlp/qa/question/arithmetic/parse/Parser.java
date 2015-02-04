package edu.emory.clir.clearnlp.qa.question.arithmetic.parse;

import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.pos.POSLibEn;
import edu.emory.clir.clearnlp.qa.question.arithmetic.ArithmeticQuestion;
import edu.emory.clir.clearnlp.qa.question.arithmetic.State;
import edu.emory.clir.clearnlp.qa.question.arithmetic.util.StringUtils;
import edu.emory.clir.clearnlp.qa.structure.Instance;
import edu.emory.clir.clearnlp.qa.structure.SemanticType;
import edu.emory.clir.clearnlp.qa.structure.attribute.AttributeType;

import java.util.*;

public class Parser {
    private ArithmeticQuestion arithmeticQuestion;
    private HashMap<String, Integer> themeCounters;

    public Parser(ArithmeticQuestion arithmeticQuestion)
    {
        this.arithmeticQuestion = arithmeticQuestion;
    }

    public List<State> parseTree(DEPTree depTree)
    {
        if (depTree == null) return null;

        List<State> stateList = new ArrayList();

        for (DEPNode node : depTree)
        {
            List<State> list = getStates(node);

            if (list != null)
            {
                stateList.addAll(list);
            }
        }

        return stateList;
    }

    private List<State> getStates(DEPNode depNode)
    {
        List<State> stateList = new ArrayList();

        if (POSLibEn.isVerb(depNode.getPOSTag()))
        {
            DEPNode A0 = null;
            for (DEPNode node : depNode.getDependentList())
            {
                if (StringUtils.extractSemanticRelation(node.getSemanticLabel(depNode)) == SemanticType.A0)
                {
                    A0 = node;
                    break;
                }
            }

            if (A0 == null)
            {
                /* Check if there is any noun on the left */
                for (DEPNode node : depNode.getLeftDependentList())
                {
                    if (POSLibEn.isNoun(node.getPOSTag()))
                    {
                        A0 = node;
                        break;
                    }
                }
            }

            stateList.addAll(findAllStates(A0, depNode));

            return stateList;
        }
        else
        {
            return null;
        }
    }

    private DEPNode selectThemeFromGoPredicate(DEPNode depNode)
    {
        for (DEPNode node : depNode.getRightDependentList())
        {
            if (node.getLemma().equals("to"))
            {
                /* Return noun under 'to' node */
                for (DEPNode node1 : node.getDependentList())
                {
                    if (POSLibEn.isNoun(node1.getPOSTag()))
                    {
                        return node1;
                    }
                }
            }
        }

        return null;
    }

    private DEPNode getLabelRelatedNode(DEPNode source, SemanticType semanticType)
    {
        if (source == null)
        {
            return null;
        }

        for (DEPNode node : source.getDependentList())
        {
            if (StringUtils.extractSemanticRelation(node.getLabel()) == semanticType)
            {
                return node;
            }
        }
        return null;
    }

    private DEPNode findRightDEPNodeNeighbor(DEPNode node)
    {
        if (node == null)
        {
            return null;
        }

        boolean foundNode = false;
        for (DEPNode n : node.getHead().getDependentList())
        {
            if (foundNode)
            {
                return n;
            }
            if (n == node)
            {
                foundNode = true;
            }
        }

        return null;
    }

    private DEPNode findDEPNodeInTree(DEPNode root, SemanticType semanticType)
    {
        Queue<DEPNode> q = new ArrayDeque();
        q.addAll(root.getRightDependentList());
        while (! q.isEmpty())
        {
            DEPNode current = q.poll();
            SemanticType currentLabel = StringUtils.extractSemanticRelation(current.getLabel());
            if (currentLabel != null && currentLabel == semanticType)
            {
                return current;
            }

            q.addAll(current.getDependentList());
        }

        return null;
    }

    private List<State> findAllStates(DEPNode A0, DEPNode head)
    {
        Queue<DEPNode> q = new ArrayDeque();
        //q.addAll(head.getRightDependentList());
        q.addAll(head.getDependentList());
        List<State> instanceList = new ArrayList();
//        System.out.println("Starting for head = " + head.getLemma());

        while (! q.isEmpty())
        {
            DEPNode current = q.poll();
//            System.out.println("Checking node = " + current.getSimplifiedForm());
//            System.out.println("Its head = " + current.getHead().getSimplifiedForm());
            if (! POSLibEn.isVerb(current.getPOSTag()))
            {
                if (current.hasHead()) {
                    //System.out.println("Checking node: " + current.getWordForm());
                    if (StringUtils.extractSemanticRelation(current.getLabel()) == SemanticType.num) {

                        //System.out.println("Inide for: " + current.getWordForm());
                        DEPNode attrNode = null;
                        DEPNode A1 = null;
                        if (POSLibEn.isNoun(current.getHead().getPOSTag()))
                        {
//                            DEPNode rightNeighbor = findRightDEPNodeNeighbor(current);
//                            if (rightNeighbor != null && rightNeighbor.getLabel().equals("prep") && rightNeighbor.getLemma().equals("of"))
//                            {
//                                A1 = findDEPNodeInTree(rightNeighbor, SemanticType.pobj);
//                                if (A1 == null)
//                                {
//                                    /* FIXME: This is because parsing error, sometimes pcomp exists
//                                              instead of pobj, then find dobj of pcomp
//                                     */
//                                    DEPNode pcomp = findDEPNodeInTree(rightNeighbor, SemanticType.pcomp);
//                                    for (DEPNode n : pcomp.getDependentList())
//                                    {
//                                        if (StringUtils.extractSemanticRelation(n.getLabel()) == SemanticType.dobj)
//                                        {
//                                            A1 = n;
//                                            break;
//                                        }
//                                    }
//                                }
//                            }
//                            else
//                            {
                                A1 = current.getHead();
//                            }

                            for (DEPNode node : current.getHead().getLeftDependentList())
                            {
                                if (POSLibEn.isAdjective(node.getPOSTag()))
                                {
                                    attrNode = node;
                                    break;
                                }
                            }
                        }
                        else if (POSLibEn.isAdjective(current.getHead().getPOSTag()))
                        {
                            /* It is most likely conj */
                            A1 = getLabelRelatedNode(current.getHead(), SemanticType.conj);
                            attrNode = current.getHead();
                        }

                        DEPNode numNode = current;

                        Instance predicate = new Instance();
                        Instance A0_inst = new Instance();
                        Instance A1_inst = new Instance();
                        Instance quantityAttribute = new Instance();

                        State s = new State();

                        if (A0 != null) {
                            predicate.putArgumentList(SemanticType.A0, A0_inst);
                            s.putInstance(A0, A0_inst);
                        }

                        predicate.putArgumentList(SemanticType.A1, A1_inst);
                        A1_inst.putAttribute(AttributeType.QUANTITY, quantityAttribute);

                        if (attrNode != null)
                        {
                            Instance attrInstance = new Instance();
                            A1_inst.putAttribute(AttributeType.QUALITY, attrInstance);
                            s.putInstance(attrNode, attrInstance);
                        }


//                        System.out.println("Adding value: " + numNode.getWordForm());
                        s.putInstance(head, predicate);

                        s.putInstance(A1, A1_inst);
                        s.putInstance(numNode, quantityAttribute);

                        instanceList.add(s);
                    }
                }
                q.addAll(current.getDependentList());
            }
        }

        return instanceList;
    }

    public State parseQuestion(DEPTree depTree) {
        DEPNode attrNode = null;

        prepareThemeCounters();

        //System.out.println("theme counters = " + themeCounters);

        if (depTree == null) return null;

        /* Find predicate and parse the question */
        DEPNode root = depTree.getFirstRoot();

        if (root == null) return null;

        /* predicate go is a special case */

        DEPNode theme = null;

        /* Search left most for noun and possible container */
        Queue<DEPNode> q = new ArrayDeque();
        q.addAll(root.getLeftDependentList());
        while(! q.isEmpty())
        {
            DEPNode candidate = q.poll();
            if (POSLibEn.isNoun(candidate.getPOSTag()) && themeCounters.containsKey(candidate.getLemma()))
            {
                theme = candidate;

                /* Check if it has any attributes */
                for (DEPNode node : theme.getLeftDependentList())
                {
                    if (! node.getLemma().equals("many") && ! node.getLemma().equals("much") && POSLibEn.isAdjective(node.getPOSTag()))
                    {
                        attrNode = node;
                    }
                }

                break;
            }

            q.addAll(candidate.getDependentList());
        }

        if (theme == null)
        {
            return null;
        }


        //depTree

        State state = new State();
        Instance rootInstance = new Instance();
        Instance a1Instance = new Instance ();
        rootInstance.putArgumentList(SemanticType.A1, a1Instance);

        if (attrNode != null)
        {
            Instance attrInstance = new Instance();
            a1Instance.putAttribute(AttributeType.QUALITY, attrInstance);
            state.putInstance(attrNode, attrInstance);
        }

        state.putInstance(root, rootInstance);
        state.putInstance(theme, a1Instance);

        return state;
    }

    private void prepareThemeCounters()
    {
        themeCounters = new HashMap();

        for (State s : arithmeticQuestion.getQuestionTextStateList())
        {
            Instance predicate;
            if ((predicate = s.getPredicateInstance()) != null && predicate.getArgumentList(SemanticType.A1) != null)
            {
                String theme = null;
                try{
                    theme = s.get(predicate.getArgumentList(SemanticType.A1).get(0)).getLemma();
                } catch (NullPointerException e)
                {
                    //System.out.println("State = " + s);
                    System.out.println("question = " + arithmeticQuestion.getQuestionText());
                    //System.out.println("textStates = " + arithmeticQuestion.getQuestionTextStateList());
                    System.exit(1);
                }
                if (themeCounters.containsKey(theme))
                {
                    themeCounters.put(theme, themeCounters.get(theme) + 1);
                }
                else
                {
                    themeCounters.put(theme, 1);
                }
            }

        }
    }
}
