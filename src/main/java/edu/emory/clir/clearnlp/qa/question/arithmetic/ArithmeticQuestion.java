package edu.emory.clir.clearnlp.qa.question.arithmetic;

import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.pos.POSLibEn;
import edu.emory.clir.clearnlp.qa.question.arithmetic.parse.Parser;
import edu.emory.clir.clearnlp.qa.structure.Instance;
import edu.emory.clir.clearnlp.qa.structure.SemanticType;
import edu.emory.clir.clearnlp.qa.structure.attribute.AttributeType;

import java.util.ArrayList;
import java.util.List;

public class ArithmeticQuestion {
    private String questionText;
    private List<DEPTree> questionTreeList;
    private List<State> questionTextStates;
    private State questionState;
    ArithmeticQuestionType arithmeticQuestionType;

    public ArithmeticQuestion(String questionText, List<DEPTree> depTreeList)
    {
        this.questionText = questionText;
        this.questionTreeList = depTreeList;
        questionTextStates = new ArrayList();
        prepareInstances();
        detectQuestionType();
    }

    private void prepareInstances()
    {
        Parser parser = new Parser();
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
                questionState = parser.parseQuestion(depTree);
            }
            else
            {
                questionTextStates.addAll(parser.parseTree(depTree));
            }
        }
    }

    private void detectQuestionType()
    {
        /*
        TODO: question types parsing here
         */
        arithmeticQuestionType = ArithmeticQuestionType.SUM;
    }

    public String toString()
    {
        //return "Text: " + questionText + "\nText states: " + questionTextStates + "\nQuestion state: " + questionState;
        return questionText;
    }

    public void solveProblem()
    {
        switch (arithmeticQuestionType){
            case SUM:
                solveSumProblem();
        }

    }

    private void solveSumProblem()
    {
        /* Detect the container and predicate in question */
        String container = null;
        String predicate = null;

        for (Instance i : questionState.keySet())
        {
            if (i.getArgumentList(SemanticType.A1) != null && i.getArgumentList(SemanticType.A1).size() > 0)
            {
                predicate = questionState.get(i).getLemma();
                for (Instance j : i.getArgumentList(SemanticType.A1))
                {
                    container = questionState.get(j).getLemma();
                }
            }
        }

        /* Select numerals from states with matched predicates and containers */
        List<String> matchingNumericals = new ArrayList();
        for (State s : questionTextStates)
        {
            for (Instance i : s.keySet())
            {
                DEPNode i_node = s.get(i);
                if (POSLibEn.isVerb(i_node.getPOSTag()) && i_node.getLemma().equals(predicate))
                {
                    Instance containerInstance = i.getArgumentList(SemanticType.A1).get(0);
                    DEPNode containerNode = s.get(containerInstance);
                    Instance numericalInstance = containerInstance.getAttribute(AttributeType.QUANTITY).get(0);
                    DEPNode numericalNode = s.get(numericalInstance);
                    if (container.equals(containerNode.getLemma()))
                    {
                        /* Get numerical and add */
                        String numerical = numericalNode.getWordForm();
                        matchingNumericals.add(numerical);
                    }
                }
            }
        }

        double sum = 0;
        for (String s : matchingNumericals)
        {
            sum += Double.parseDouble(s);
        }

        System.out.println("Answer is: " + sum);
    }
}
