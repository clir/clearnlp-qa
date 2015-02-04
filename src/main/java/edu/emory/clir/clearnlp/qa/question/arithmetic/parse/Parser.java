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

    private DEPNode GetSemanticallyRelatedNode(DEPNode source, SemanticType semanticType)
    {
        if (source == null)
        {
            return null;
        }

        for (DEPNode node : source.getDependentList())
        {
            if (StringUtils.extractSemanticRelation(node.getSemanticLabel(source)) == semanticType)
            {
                return node;
            }
        }
        return null;
    }

    private List<State> findAllStates(DEPNode A0, DEPNode head)
    {
        Queue<DEPNode> q = new ArrayDeque();
        q.addAll(head.getRightDependentList());
        List<State> instanceList = new ArrayList();
//        System.out.println("Starting for head = " + head.getLemma());

        while (! q.isEmpty())
        {

            DEPNode current = q.poll();
//            System.out.println("Checking node = " + current.getLemma());
            if (! POSLibEn.isVerb(current.getPOSTag()))
            {
                if (current.hasHead()) {
                    if (StringUtils.extractSemanticRelation(current.getLabel()) == SemanticType.num &&
                            POSLibEn.isNoun(current.getHead().getPOSTag())) {
                        DEPNode A1 = current.getHead();
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
        prepareThemeCounters();

        //System.out.println("theme counters = " + themeCounters);

        if (depTree == null) return null;

        /* Find predicate and parse the question */
        DEPNode root = null;
        for (DEPNode node : depTree) {
            if (node.getLabel().equals("root")) {
                root = node;
                break;
            }
        }

        if (root == null) return null;

        /* predicate go is a special case */

        DEPNode theme = null;

        /* If left most dependent is noun, then it is theme */
        if (root.getDependentList() != null && root.getLeftDependentList().size() > 0 &&
                POSLibEn.isNoun(root.getDependentList().get(0).getPOSTag()))
        {
            theme = root.getDependentList().get(0);
        }

        if (theme == null)
        {
            return null;
        }

        State state = new State();
        Instance rootInstance = new Instance();
        Instance a1Instance = new Instance ();
        rootInstance.putArgumentList(SemanticType.A1, a1Instance);
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
                String theme = s.get(predicate.getArgumentList(SemanticType.A1).get(0)).getLemma();
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
