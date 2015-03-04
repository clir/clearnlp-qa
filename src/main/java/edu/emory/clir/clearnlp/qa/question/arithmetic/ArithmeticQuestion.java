package edu.emory.clir.clearnlp.qa.question.arithmetic;

import edu.emory.clir.clearnlp.collection.pair.Pair;
import edu.emory.clir.clearnlp.collection.set.DisjointSet;
import edu.emory.clir.clearnlp.coreference.AbstractCoreferenceResolution;
import edu.emory.clir.clearnlp.coreference.EnglishCoreferenceResolution;
import edu.emory.clir.clearnlp.coreference.mention.Mention;
import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.qa.question.arithmetic.type.ArithmeticQuestionType;
import edu.emory.clir.clearnlp.qa.question.arithmetic.parse.Parser;
import edu.emory.clir.clearnlp.qa.question.arithmetic.type.Detector;
import edu.emory.clir.clearnlp.qa.structure.Instance;
import edu.emory.clir.clearnlp.qa.structure.SemanticType;
import edu.emory.clir.clearnlp.qa.structure.attribute.AttributeType;

import java.util.ArrayList;
import java.util.List;

public class ArithmeticQuestion {
    private String          questionText;
    Pair<List<Mention>,DisjointSet> coRefEntities;

    public List<DEPTree> getQuestionTreeList() {
        return questionTreeList;
    }

    private List<DEPTree>   questionTreeList;

    private List<State>     questionTextStateList;

    public State getQuestionState() {
        return questionState;
    }

    private State           questionState;

    ArithmeticQuestionType qType;

    public ArithmeticQuestion(String questionText, List<DEPTree> depTreeList)
    {
        this.questionText       = questionText;
        this.questionTreeList   = depTreeList;
        questionTextStateList   = new ArrayList();
        prepareInstances();
        detectQuestionType();
        AbstractCoreferenceResolution coref = new EnglishCoreferenceResolution();
        coRefEntities = coref.getEntities(questionTreeList);

//        for (int i = 0; i < coRefEntities.o1.size(); i++)
//        {
//            System.out.println("MentionList[" + i +"] = " + coRefEntities.o1.get(i).getNode());
//        }
//
//        System.out.println("Set = " + coRefEntities.o2.toString());
//
//        System.out.println("Coref = " + coRefEntities);
        processCoReferences();
    }

    public List<DEPTree> getDEPTrees()
    {
        return questionTreeList;
    }

    public ArithmeticQuestionType getArithmeticQuestionType()
    {
        return qType;
    }

    public List<State> getQuestionTextStateList()
    {
        return questionTextStateList;
    }

    public String getQuestionText()
    {
        return questionText;
    }

    private void prepareInstances()
    {
        Parser parser = new Parser(this);
        boolean isQuestion = false;

        for (DEPTree depTree : questionTreeList)
        {
            for (DEPNode node : depTree)
            {
                if (node.getLemma().equals("?"))
                {
                    isQuestion = true;
                    break;
                }
            }

            if (isQuestion)
            {
                isQuestion = false;
//
                //System.out.println(questionTextStateList);
//                System.exit(1);
                /* Try to fix unmarked themes */
                fillThemes();

                questionState = parser.parseQuestion(depTree);
                questionTextStateList.addAll(parser.parseTree(depTree));
            }
            else
            {
                questionTextStateList.addAll(parser.parseTree(depTree));
            }
        }
    }

    private void detectQuestionType()
    {
        Detector detector = new Detector(this);
        qType = detector.detectQuestionType();
    }

    public String toString()
    {
        String s = questionText;

        return s;
    }

    public String detailedToString()
    {
        String s = "Question: " + questionText;
        s += "\nText states: ";

        for (State st : questionTextStateList)
        {
            s += st + "\n";
        }

        s += "\nQuestion state: " + questionState + "\n";

        return s;
    }

    public double solve()
    {
        switch (qType){
            case SUM:
                return solveSum();
        }

        return -1;
    }

    private double solveSum()
    {
        /* Detect the container and predicate in question */
        String container = null;
        String predicate = null;
        String attribute = null;

        if (questionState == null)
        {
            System.out.println("Error with parsing question, no question state");
            return -1;
        }

        /* Find container, predicate and attribute from questionState */
        for (Instance i : questionState.keySet())
        {
            if (i.getArgumentList(SemanticType.A1) != null && i.getArgumentList(SemanticType.A1).size() > 0)
            {
                predicate = questionState.get(i).getLemma();

                Instance containerInstance = i.getArgumentList(SemanticType.A1).get(0);
                container = questionState.get(containerInstance).getLemma();

                Instance attrInstance;

                if (containerInstance.getAttribute(AttributeType.QUALITY) != null &&
                        containerInstance.getAttribute(AttributeType.QUALITY).size() > 0)
                {
                    attrInstance = containerInstance.getAttribute(AttributeType.QUALITY).get(0);
                    attribute = questionState.get(attrInstance).getLemma();
                }
            }
        }

        //System.out.println("container = " + container + ", predicate = " + predicate);

        /* Select numerals from states with matched predicates and containers */
        List<String> matchingNumbers = new ArrayList();

        for (State s : questionTextStateList)
        {
            Instance predicateInstance  = s.getPredicateInstance();
            DEPNode  predicateNode      = s.get(predicateInstance);

            /* TODO: Currently not matching predicates in sum. */

            //if (i_node.getLemma().equals(predicate))
            //{

            Instance containerInstance = predicateInstance.getArgumentList(SemanticType.A1).get(0);
            DEPNode containerNode = s.get(containerInstance);

            Instance numericalInstance = containerInstance.getAttribute(AttributeType.QUANTITY).get(0);
            DEPNode numericalNode = s.get(numericalInstance);

            Instance attributeInstance = null;
            DEPNode attributeNode = null;

            /* If attribute exists, retrieve */
            if (containerInstance.getAttribute(AttributeType.QUALITY) != null &&
                    containerInstance.getAttribute(AttributeType.QUALITY).size() > 0)
            {
                attributeInstance = containerInstance.getAttribute(AttributeType.QUALITY).get(0);
                attributeNode = s.get(attributeInstance);
            }

            /* If attribute existed, include in checking */
            String numerical = null;

            if (attributeInstance != null && container.equals(containerNode.getLemma()) &&
                    attribute.equals(attributeNode.getLemma()))
            {
                numerical = numericalNode.getWordForm();
            }
            else if (attributeInstance == null && container.equals(containerNode.getLemma()))
            {
                numerical = numericalNode.getWordForm();
            }

            if (numerical != null) matchingNumbers.add(numerical);
            //}
        }

        /* Sum up all collected numbers, this is the answer */
        double sum = 0;
        for (String s : matchingNumbers)
        {
            sum += Double.parseDouble(s);
        }

        if (sum == 0) return -1;
        return sum;
    }

    private void fillThemes()
    {
//        System.out.println("Before counting, states = " + questionTextStateList);
//        System.exit(1);
        for (State s : questionTextStateList)
        {
            if (s.getPredicateInstance().getArgumentList(SemanticType.A1) == null)
            {
                System.out.println("Is null for question = " + questionText + ", predicate = " + s.get(s.getPredicateInstance()));
                //System.out.println("State = " + s);
                //System.exit(0);

            }
            Instance theme_inst = s.getPredicateInstance().getArgumentList(SemanticType.A1).get(0);
            DEPNode theme_node = s.get(theme_inst);

            if (theme_node == null)
            {
                s.putInstance(findClosestTheme(s), theme_inst);
            }
        }

    }

    private DEPNode findClosestTheme(State s)
    {
        int index = questionTextStateList.indexOf(s);
        int diff = 1;

        while (index - diff >= 0 || index + diff < questionTextStateList.size())
        {
            if (index - diff >= 0) {
                /* Check previous if exists */
                State state = questionTextStateList.get(index - diff);

                Instance theme_inst = state.getPredicateInstance().getArgumentList(SemanticType.A1).get(0);
                DEPNode theme_node = state.get(theme_inst);

                if (theme_node != null) return theme_node;
            }

            if (index + diff < questionTextStateList.size())
            {
                /* Check next if exists */
                State state = questionTextStateList.get(index + diff);
                Instance theme_inst = state.getPredicateInstance().getArgumentList(SemanticType.A1).get(0);
                DEPNode theme_node = state.get(theme_inst);

                if (theme_node != null) return theme_node;
            }

            diff++;
        }

        return null;
    }

    private void processCoReferences()
    {
        List<State> tmpList = new ArrayList();
        tmpList.addAll(questionTextStateList);
        tmpList.add(questionState);

        for (State s: tmpList)
        {
            Instance pred_inst = s.getPredicateInstance();
            Instance A0_inst = null;
            Instance A2_inst = null;

            if (pred_inst.getArgumentList(SemanticType.A0) != null && pred_inst.getArgumentList(SemanticType.A0).size() > 0)
            {
                A0_inst = pred_inst.getArgumentList(SemanticType.A0).get(0);
            }

            if (pred_inst.getArgumentList(SemanticType.A2) != null && pred_inst.getArgumentList(SemanticType.A2).size() > 0)
            {
                A2_inst = pred_inst.getArgumentList(SemanticType.A2).get(0);
            }

            DEPNode A0_node;
            DEPNode A2_node;
            A0_node = s.get(A0_inst);
            A2_node = s.get(A2_inst);

            if (A0_node != null && (A0_node.getLemma().equals("she") || A0_node.getLemma().equals("he")))
            {
                DEPNode coreferenced = extractCoRefenceFromNode(A0_node);

                if (coreferenced != null)
                {
                    s.putInstance(coreferenced, A0_inst);
                }
            }

            if (A2_node != null && (A2_node.getLemma().equals("her") || A2_node.getLemma().equals("him")))
            {
                DEPNode coreferenced = extractCoRefenceFromNode(A2_node);

                if (coreferenced != null)
                {
                    s.putInstance(coreferenced, A2_inst);
                }
            }
        }
    }

    private DEPNode extractCoRefenceFromNode(DEPNode node)
    {
        int i = 0;
        for (; i < coRefEntities.o1.size(); i++)
        {
            if (node == coRefEntities.o1.get(i).getNode())
            {
                /* i-th index in Set has "corefence to" node */
                int j = coRefEntities.o2.find(i);
                return coRefEntities.o1.get(coRefEntities.o2.find(j)).getNode();
            }
        }

        return node;
    }
}
