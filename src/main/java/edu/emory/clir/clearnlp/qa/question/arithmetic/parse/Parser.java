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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
            State i = getState(node);

            if (i != null) stateList.add(i);
        }

        return stateList;
    }

    private State getState(DEPNode depNode)
    {
        State s = null;
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

            DEPNode A1 = null;
            for (DEPNode node : depNode.getDependentList())
            {
                if (StringUtils.extractSemanticRelation(node.getSemanticLabel(depNode)) == SemanticType.A1)
                {
                    A1 = node;
                    break;
                }
            }

            if (A1 != null)
            {
                for (DEPNode node : A1.getDependentList())
                {
                    if (StringUtils.extractSemanticRelation(node.getLabel()) == SemanticType.num)
                    {
                        Instance predicate = new Instance();
                        Instance A0_inst = new Instance();
                        Instance A1_inst = new Instance();
                        Instance quantityAttribute = new Instance();

                        s = new State();

                        if (A0 != null)
                        {
                            predicate.putArgumentList(SemanticType.A0, A0_inst);
                            s.putInstance(A0, A0_inst);
                        }

                        predicate.putArgumentList(SemanticType.A1, A1_inst);
                        A1_inst.putAttribute(AttributeType.QUANTITY, quantityAttribute);


                        s.putInstance(depNode, predicate);

                        s.putInstance(A1, A1_inst);
                        s.putInstance(node, quantityAttribute);

                        return s;
                    }
                }
            }
        }

        return s;
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

        DEPNode theme = null;

        /* If let most dependent is noun, then it is theme */
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
