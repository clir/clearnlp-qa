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
    List<EnglishDocument> questions = new ArrayList();

    public static void main(String[] args) {

        Experiment exp = new Experiment();
        exp.readQuestions();
        //exp.prepareDataset();
    }

    private void readQuestions()
    {
        QuestionReader reader = new QuestionReader();

        try {
            EnglishDocument ed;

//            aq = areader.read("files/", "arith-qs.ah");
//            arithmeticQuestions.add(aq);
//            System.out.println("Question: " + aq.getQuestionText());
//            System.out.println("States: " + aq.getQuestionTextStateList());
//            System.out.println("Question State: " + aq.getQuestionState() + "\n");

            while ((ed = reader.read()) != null) {
                questions.add(ed);

                System.out.println("Question: " + ed);
//                System.out.println("States: " + aq.getQuestionTextStateList());
//                System.out.println("Question State: " + aq.getQuestionState() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
