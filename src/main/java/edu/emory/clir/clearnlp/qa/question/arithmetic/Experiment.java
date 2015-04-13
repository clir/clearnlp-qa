package edu.emory.clir.clearnlp.qa.question.arithmetic;

import edu.emory.clir.clearnlp.qa.structure.document.EnglishDocument;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Tomasz Jurczyk ({@code tomasz.jurczyk@emory.edu})
 */

public class Experiment {
    List<String>          questionsText = new ArrayList();
    List<ArithmeticQuestion> questions  = new ArrayList();
    List<ArithmeticQuestion> sQuestions = new ArrayList();

    public static void main(String[] args) {

        Experiment exp = new Experiment();
        exp.readQuestions();

        //exp.prepareDataset();

        /* Serialize the list */
//        try
//        {
//            FileOutputStream fileOut = new FileOutputStream("q.ser");
//            ObjectOutputStream out = new ObjectOutputStream(fileOut);
//
//            out.writeObject(exp.questions);
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }

        /* Deserialize list */
        //exp.deserializeDataSet(exp);

        /* Compare result with deserialized */
        //System.out.println("Start comparing datasets.");
        //exp.compareDataSets(exp);

        /* Serialize result */
        // serializeDataSet()
    }

    private void compareDataSets(Experiment exp)
    {
        if (exp.questions.size() != exp.sQuestions.size())
        {
            System.out.println("Size is different!");
            System.exit(1);
        }

        int j = 1;

        for (int i = 0; i < exp.questions.size(); i++)
        {
            if (exp.questions.get(i).compareTo(exp.sQuestions.get(i)) != 0)
            {
                System.out.println("Found " + j++ + " different question!\nSerialized:" + exp.sQuestions.get(i) +
                        "\nResult:\n" + exp.questions.get(i));
            }
        }
    }

    private void serializeDataSet()
    {

    }

    private void deserializeDataSet(Experiment exp)
    {
        try
        {
            FileInputStream fileIn = new FileInputStream("q.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);

            exp.sQuestions = (List<ArithmeticQuestion>) in.readObject();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
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

//                System.out.println("Question: " + aq);
//                System.out.println("Question instance: " + aq.getQuestionRoot());
//                System.out.println("States: " + aq.getQuestionTextStateList());
//                System.out.println("Question State: " + aq.getQuestionState() + "\n");
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
