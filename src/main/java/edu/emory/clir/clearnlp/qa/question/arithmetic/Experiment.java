package edu.emory.clir.clearnlp.qa.question.arithmetic;

import edu.emory.clir.clearnlp.qa.structure.document.EnglishDocument;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
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

        /* Serialize the list */
        try
        {
            FileOutputStream fileOut = new FileOutputStream("q.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);

            for (ArithmeticQuestion aq: exp.questions)
            {
                out.writeObject(aq);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void readQuestions()
    {
        /* Read questions trees */
        QuestionReader reader = new QuestionReader();

        try {
            ArithmeticQuestion aq;
            int i = 0;

//            aq = reader.read("files/", "arith-qs.ew");
//            questions.add(aq);
//            System.out.println("Question: " + aq);

            while (i < 400 && (aq = reader.read()) != null) {
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
