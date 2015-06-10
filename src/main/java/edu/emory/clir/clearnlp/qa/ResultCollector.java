package edu.emory.clir.clearnlp.qa;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author: Tomasz Jurczyk ({@code tomasz.jurczyk@emory.edu})
 */
public class ResultCollector {
    private int numberOfQuestions = 0;
    private int processedQuestions = 0;
    private int selectedQuestions = 0;
    private int answeredQuestions = 0;
    private HashMap<Integer,Integer> rankMap = new HashMap();

    public ResultCollector()
    {
        rankMap.put(0, 0);
        rankMap.put(1, 0);
        rankMap.put(2, 0);
    }

    public void addQuestion()
    {
        numberOfQuestions++;
    }

    public void processQuestion()
    {
        processedQuestions++;
    }

    public void selectQuestion()
    {
        selectedQuestions++;
    }

    public void answerQuestion()
    {
        answeredQuestions++;
    }

    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Number of all questions recognized: " + numberOfQuestions + "\n");
        stringBuilder.append("Number of all questions processed : " + processedQuestions + "\n");
        stringBuilder.append("Number of selected questions:       " + selectedQuestions + "\n");
        stringBuilder.append("Number of answered questions:       " + answeredQuestions + "\n");

        return stringBuilder.toString();
    }
}
