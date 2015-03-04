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
    private HashMap<String, Boolean> actorCounters;

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
            /* Perform search only if it's verb */

            /* Try to locate A0 from Semantic Label A0 */
            DEPNode A0 = null;
            for (DEPNode node : depNode.getDependentList())
            {
                if (StringUtils.extractSemanticRelation(node.getSemanticLabel(depNode)) == SemanticType.A0
                        && ! POSLibEn.isVerb(node.getPOSTag()))
                {
                    A0 = node;
                    break;
                }
            }

            /* If A0 Semantic Label does not exist, try to get a left most dependent child */
            if (A0 == null)
            {
                for (DEPNode node : depNode.getLeftDependentList())
                {
                    if (POSLibEn.isNoun(node.getPOSTag()))
                    {
                        A0 = node;
                        break;
                    }
                }
            }

            /* Find all states under this verb and add to state list */
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


    /**
     * @param source
     * @param semanticType
     *
     */
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
        q.addAll(head.getDependentList());
        List<State> instanceList = new ArrayList();

        while (! q.isEmpty())
        {
            DEPNode current = q.poll();
            if (! POSLibEn.isVerb(current.getPOSTag()))
            {
                if (current.hasHead())
                {
                    if (StringUtils.extractSemanticRelation(current.getLabel()) == SemanticType.num ||
                            StringUtils.isInteger(current.getWordForm()) || StringUtils.isDouble(current.getWordForm()))
                    {
                        DEPNode attrNode = null;
                        DEPNode A1 = null;
                        DEPNode A2 = null;

                        if (POSLibEn.isNoun(current.getHead().getPOSTag()))
                        {
                            /* If head is noun, relation is num->theme */
                            A1 = current.getHead();

                            /* Check if there is any attribute for this noun */
                            for (DEPNode node : current.getHead().getLeftDependentList())
                            {
                                //System.out.println("For noun: " + current.getHead().getWordForm() + ", checking child: " + node.getWordForm());
                                if (POSLibEn.isAdjective(node.getPOSTag()))
                                {
                                    attrNode = node;
                                    break;
                                }
                            }
                        }
                        else if (POSLibEn.isAdjective(current.getHead().getPOSTag()))
                        {
                            /* If head is adjective, search for conj relation */
                            A1 = getLabelRelatedNode(current.getHead(), SemanticType.conj);
                            attrNode = current.getHead();
                        }
                        else if (POSLibEn.isVerb(current.getHead().getPOSTag()))
                        {
                            /* This is _pobj_ relation */
                            //A1 = findDEPNodeInTree(current, SemanticType.pobj);

                        }

                        /* Check if there is any A2 */
                        for (DEPNode depNode: head.getDependentList())
                        {
                            if (StringUtils.extractSemanticRelation(depNode.getSemanticLabel(head)) == SemanticType.A2
                                    && ! POSLibEn.isVerb(depNode.getPOSTag()))
                            {
                                A2 = depNode;
                            }
                        }

                        /* Create a new state and add to the list */
                        DEPNode numNode     = current;

                        Instance pred_inst  = new Instance();
                        Instance A0_inst    = new Instance();
                        Instance A1_inst    = new Instance();
                        Instance A2_inst    = new Instance();
                        Instance q_inst     = new Instance();

                        State s = new State();

                        if (A0 != null) {
                            /* Add A0 (actor) if exists */
                            pred_inst.putArgumentList(SemanticType.A0, A0_inst);
                            s.putInstance(A0, A0_inst);
                        }

                        if (A2 != null)
                        {
                            pred_inst.putArgumentList(SemanticType.A2, A2_inst);
                            s.putInstance(A2, A2_inst);
                        }

                        pred_inst.putArgumentList(SemanticType.A1, A1_inst);
                        A1_inst.putAttribute(AttributeType.QUANTITY, q_inst);

                        if (attrNode != null)
                        {
                            /* If attribute exists for theme, add */
                            Instance attrInstance = new Instance();
                            A1_inst.putAttribute(AttributeType.QUALITY, attrInstance);
                            s.putInstance(attrNode, attrInstance);
                        }

                        s.putInstance(head, pred_inst);
                        s.putInstance(A1, A1_inst);
                        s.putInstance(numNode, q_inst);

                        instanceList.add(s);
                    }
                    else if ((StringUtils.isDouble(current.getWordForm()) ||
                            StringUtils.isInteger(current.getWordForm())) &&
                            instanceList.size() > 0)
                    {
                        /* When there is no num label and this is number, special case (dependency parsing error) */
                        /* Retrieve last added state and use its theme, actor and predicate */
                        State prev_sate = instanceList.get(instanceList.size() - 1);
                        State s = new State(prev_sate);
                        s.set(s.getNumericalInstance(), current);
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

        prepareCounters();

        if (depTree == null) return null;

        /* Find predicate and parse the question */
        DEPNode pred_node   = depTree.getFirstRoot();
        String pred         = pred_node.getLemma();

        if (pred_node == null) return null;

        DEPNode theme = null;
        DEPNode actor = null;
        DEPNode a2_node = null;

        /* Search for noun and possible container */
        Queue<DEPNode> q = new ArrayDeque();
        q.addAll(pred_node.getDependentList());

        while(! q.isEmpty())
        {
            DEPNode candidate = q.poll();
            if (POSLibEn.isNoun(candidate.getPOSTag()) && themeCounters.containsKey(candidate.getLemma()))
            {
                /* If found noun that is in theme dictionary, it's hit */
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

            if (POSLibEn.isNoun(candidate.getPOSTag()) && actorCounters.containsKey(candidate.getLemma()))
            {
                /* If found noun that is an actor, retrieve it */
                actor = candidate;
            }

            /* Add all children of this node */
            q.addAll(candidate.getDependentList());
        }

        /* Check if there is any A2 */
        for (DEPNode node: pred_node.getDependentList())
        {
            if (StringUtils.extractSemanticRelation(node.getSemanticLabel(pred_node)) == SemanticType.A2
                    && ! POSLibEn.isVerb(node.getPOSTag()))
            {
                a2_node = node;
            }
        }

        /* If actor has not been found, try to check A0 */
        if (actor == null)
        {
            /* Try to retrieve actor as A0 from predicate */
            for (DEPNode node: pred_node.getDependentList())
            {
                System.out.println("Comparing node = " + node.getLemma());
                if (StringUtils.extractSemanticRelation(node.getSemanticLabel(pred_node)) == SemanticType.A0
                        && ! POSLibEn.isVerb(node.getPOSTag()))
                {
                    System.out.println("Hit");
                    actor = node;
                    break;
                }
            }
        }

        if (theme == null && actor != null)
        {
            /**
             * Theme has not been found, but actor has.
             * Perform search of container in text states with
             * the same actor
             */
            for (State s : arithmeticQuestion.getQuestionTextStateList())
            {
                Instance pred_inst  = s.getPredicateInstance();
                Instance actor_inst = null;

                if (pred_inst.getArgumentList(SemanticType.A0) != null &&
                        pred_inst.getArgumentList(SemanticType.A0).size() > 0)
                {
                    actor_inst = pred_inst.getArgumentList(SemanticType.A0).get(0);
                }

                if (actor_inst == null) continue;

                DEPNode actor_node = s.get(actor_inst);
                if (actor_node != null && actor_node.getLemma().equals(actor.getLemma()))
                {
                    /* We got it, get theme of this actor */
                    theme = s.get(pred_inst.getArgumentList(SemanticType.A1).get(0));
                }

            }
        }
        else if (theme == null)
        {
            /* Try to retrieve most common theme from question text state list */
            int max = -1;
            String theme_str = "";
            for (Map.Entry<String,Integer> entry : themeCounters.entrySet())
            {
                if (entry.getValue() > max)
                {
                    max = entry.getValue();
                    theme_str = entry.getKey();
                }
            }

            /* Find corresponding DEPNode in State list */
            for (State s : arithmeticQuestion.getQuestionTextStateList()) {
                Instance theme_inst = s.getPredicateInstance().getArgumentList(SemanticType.A1).get(0);

                try
                {
                    DEPNode theme_node = s.get(theme_inst);
                    if (theme_node != null && theme_node.getLemma().equals(theme_str))
                    {
                        theme = theme_node;
                        break;
                    }
                }
                catch (NullPointerException e)
                {
                    e.printStackTrace();
                }
            }

        }

        if (theme == null && actor == null)
        {
            /* If nothing found */
            /* TODO: Only for VerbApp */
            //return null;
        }

        /* Create a state of a question */
        State state             = new State();
        Instance pred_inst      = new Instance();
        Instance theme_inst     = new Instance ();
        Instance actor_inst     = new Instance();
        Instance A2_inst     = new Instance();
        pred_inst.putArgumentList(SemanticType.A1, theme_inst);

        if (attrNode != null)
        {
            Instance attrInstance = new Instance();
            theme_inst.putAttribute(AttributeType.QUALITY, attrInstance);
            state.putInstance(attrNode, attrInstance);
        }

        if (actor != null)
        {
            pred_inst.putArgumentList(SemanticType.A0, actor_inst);
            state.putInstance(actor, actor_inst);
        }

        if (a2_node != null)
        {
            pred_inst.putArgumentList(SemanticType.A2, A2_inst);
            state.putInstance(a2_node, A2_inst);
        }

        state.putInstance(pred_node, pred_inst);
        state.putInstance(theme, theme_inst);

        //System.out.println("Returning state: " + state);
        return state;
    }

    private void prepareCounters()
    {
        themeCounters = new HashMap();
        actorCounters = new HashMap();

        /* Iterate through all states and count themes and actors */
        for (State s : arithmeticQuestion.getQuestionTextStateList())
        {
            Instance pred_inst;
            if ((pred_inst = s.getPredicateInstance()) != null && pred_inst.getArgumentList(SemanticType.A1) != null)
            {
                String theme;
                String actor = null;

                theme = null;

                try {
                    theme = s.get(pred_inst.getArgumentList(SemanticType.A1).get(0)).getLemma();
                }
                catch (NullPointerException e){
                    continue;
                }

                if (pred_inst.getArgumentList(SemanticType.A0) != null &&
                        pred_inst.getArgumentList(SemanticType.A0).size() > 0)
                {
                    actor = s.get(pred_inst.getArgumentList(SemanticType.A0).get(0)).getLemma();
                }

                if (themeCounters.containsKey(theme))
                {
                    themeCounters.put(theme, themeCounters.get(theme) + 1);
                }
                else
                {
                    themeCounters.put(theme, 1);
                }

                if (actor != null)
                {
                    actorCounters.put(actor, true);
                }
            }

        }
    }
}
