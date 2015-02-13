package edu.emory.clir.clearnlp.qa.question.arithmetic.type;

import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.qa.question.arithmetic.ArithmeticQuestion;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Detector {
    private ArithmeticQuestion question;
    private final List<String> sumKeywords = Arrays.asList("total", "all", "together", "altogether");

    public Detector(ArithmeticQuestion arithmeticQuestion)
    {
        question = arithmeticQuestion;
    }

    public ArithmeticQuestionType detectQuestionType()
    {
        if (isSumQuestion()) return ArithmeticQuestionType.SUM;
        else return ArithmeticQuestionType.TRANSITION;
    }

    private boolean isSumQuestion()
    {
        /* Find sentence with a question mark */
        DEPTree qTree = null;
        boolean isQuestionFound = false;

        Iterator<DEPTree> it = question.getDEPTrees().iterator();
        while(it.hasNext())
        {
            qTree = it.next();

            for (DEPNode node : qTree)
            {
                if (node.getLemma().equals("?"))
                {
                    isQuestionFound = true;
                }
            }

            if (isQuestionFound) break;
        }

        if (! isQuestionFound) return false;

        /* Check question part if contain specific keywords */
        for (DEPNode node : qTree)
        {
            if (sumKeywords.stream().filter(s -> s.equals(node.getLemma())).count() > 0)
            {
                return true;
            }
        }

        return false;
    }
}
