package edu.emory.clir.clearnlp.qa.question.arithmetic;

import edu.emory.clir.clearnlp.qa.structure.document.EnglishDocument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Tomasz Jurczyk ({@code tomasz.jurczyk@emory.edu})
 */

public class Experiment {
    List<String>          questionsText = new ArrayList();
    List<ArithmeticQuestion> questions  = new ArrayList();

    public static void main(String[] args) {

        Experiment exp = new Experiment();
        exp.readQuestions();
        //exp.prepareDataset();
    }

    private void readQuestions()
    {
        /* Read questions trees */
        QuestionReader reader = new QuestionReader();

        try {
            ArithmeticQuestion aq;
            int i = 0;

//            aq = areader.read("files/", "arith-qs.ah");
//            arithmeticQuestions.add(aq);
//            System.out.println("Question: " + aq.getQuestionText());
//            System.out.println("States: " + aq.getQuestionTextStateList());
//            System.out.println("Question State: " + aq.getQuestionState() + "\n");

            while (i < 10 && (aq = reader.read()) != null) {
                questions.add(aq);

                System.out.println("Question: " + aq);
                //System.out.println("Question instance: " + aq.getQuestionRoot());
//                System.out.println("States: " + aq.getQuestionTextStateList());
//                System.out.println("Question State: " + aq.getQuestionState() + "\n");
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
