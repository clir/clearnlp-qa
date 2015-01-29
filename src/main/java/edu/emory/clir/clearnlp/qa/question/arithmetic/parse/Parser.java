package edu.emory.clir.clearnlp.qa.question.arithmetic.parse;

import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.pos.POSLibEn;
import edu.emory.clir.clearnlp.qa.question.arithmetic.State;
import edu.emory.clir.clearnlp.qa.question.arithmetic.util.StringUtils;
import edu.emory.clir.clearnlp.qa.structure.Instance;
import edu.emory.clir.clearnlp.qa.structure.SemanticType;
import edu.emory.clir.clearnlp.qa.structure.attribute.AttributeType;

import java.util.ArrayList;
import java.util.List;

public class Parser {
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

            if (A0 != null && A1 != null)
            {
                for (DEPNode node : A1.getDependentList())
                {
                    if (StringUtils.extractSemanticRelation(node.getLabel()) == SemanticType.num)
                    {
                        Instance predicate = new Instance();
                        Instance A0_inst = new Instance();
                        Instance A1_inst = new Instance();
                        Instance quantityAttribute = new Instance();

                        predicate.putArgumentList(SemanticType.A0, A0_inst);
                        predicate.putArgumentList(SemanticType.A1, A1_inst);
                        A1_inst.putAttribute(AttributeType.QUANTITY, quantityAttribute);

                        s = new State();
                        s.putInstance(depNode, predicate);
                        s.putInstance(A0, A0_inst);
                        s.putInstance(A1, A1_inst);
                        s.putInstance(node, quantityAttribute);

                        return s;
                    }
                }
            }
        }

        return s;
    }

    public State parseQuestion(DEPTree depTree)
    {
        if (depTree == null) return null;

        /* Find predicate and parse the question */
        DEPNode root = null;
        for (DEPNode node : depTree)
        {
            if (node.getLabel().equals("root"))
            {
                root = node;
                break;
            }
        }

        if (root == null) return null;

        DEPNode a1Node = null;
        for (DEPNode node : root.getDependentList())
        {
            if (StringUtils.extractSemanticRelation(node.getSemanticLabel(root)) == SemanticType.A1)
            {
                a1Node = node;
            }
        }

        if (a1Node == null) return null;

        State state = new State();
        Instance rootInstance = new Instance();
        Instance a1Instance = new Instance ();
        rootInstance.putArgumentList(SemanticType.A1, a1Instance);
        state.putInstance(root, rootInstance);
        state.putInstance(a1Node, a1Instance);

        return state;
    }
}
