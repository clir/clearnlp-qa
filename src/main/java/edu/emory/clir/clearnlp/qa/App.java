package edu.emory.clir.clearnlp.qa;

import edu.emory.clir.clearnlp.qa.question.arithmetic.ArithmeticQuestion;
import edu.emory.clir.clearnlp.qa.question.arithmetic.ArithmeticQuestions;
import edu.emory.clir.clearnlp.qa.question.arithmetic.type.ArithmeticQuestionType;
import edu.emory.clir.clearnlp.qa.question.arithmetic.util.Reader;
import edu.emory.clir.clearnlp.qa.structure.document.EnglishDocument;
import edu.emory.clir.clearnlp.reader.TSVReader;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Hello world!
 *
 */
public class App 
{
    private EnglishDocument document;
    HashMap<String,Integer> wordsMap = new HashMap<String,Integer>();
    ValueComparator bvc =  new ValueComparator(wordsMap);
    TreeMap<String,Integer> sorted_map = new TreeMap<String,Integer>(bvc);
    String filename_prefix = "files/arith-qs";
    TSVReader reader = new TSVReader(0,1,2,3,4,5,6,7);

    public App()
    {
    }

    public static void main( String[] args )
    {
        App app = new App();
        //app.calculateIdf();

        ArithmeticQuestions arithmeticQuestions = new ArithmeticQuestions();

        /* Read answers */
        HashMap<Integer, Double> qAnswers = new HashMap();

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("ans.txt"));

            int i = 0;
            String line;
            while((line = bufferedReader.readLine()) != null)
            {
                qAnswers.put(i++, Double.parseDouble(line));
            }
        }
        catch (IOException e)
        {

        }


        edu.emory.clir.clearnlp.qa.question.arithmetic.util.Reader areader = new Reader();
        int counter = 0;
        int i = 0;
        int correctAnswers    = 0;
        int notCorrectAnswers = 0;
        List<Integer> properlySolved = new ArrayList();

        try {
            ArithmeticQuestion aq;
            while ((aq = areader.read()) != null)
            {
                if (aq.getArithmeticQuestionType() == ArithmeticQuestionType.SUM)
                {

                    arithmeticQuestions.add(aq);
                    double foundAnswer = aq.solve();
                    foundAnswer = round(foundAnswer, 1);
                    if (foundAnswer == round(qAnswers.get(i), 1))
                    {
                        //System.out.println("Found answer is correct that is: " + foundAnswer);
                        properlySolved.add(i);
                        correctAnswers++;
                    }
                    else
                    {
                        //System.out.println("id: " + i + " Question: " + aq);
                        //System.out.println("Found answer is NOT correct: " + foundAnswer + ", should be: " +
                        //        qAnswers.get(i));
                        notCorrectAnswers++;
                    }
                    //System.out.println("\n");
                    counter++;
                }
                else
                {
                    System.out.println(aq.getQuestionText());
                }
                i++;
            }
            // This is for single question case
//            aq = areader.readFile("files/", "arith-sample.ad");
//            System.out.println("Question: " + aq.detailedToString());
//            arithmeticQuestions.add(aq);
//            double foundAnswer = aq.solve();
//            System.out.println("Solution: " + foundAnswer);

        } catch (IOException e)
        {
            e.printStackTrace();
        }

        System.out.println("Parsed questions: " + counter);
        System.out.println("Correctly answered questions: " + correctAnswers);
        System.out.println("Incorrectly answered questions: " + notCorrectAnswers);
        System.out.println("Properly answered ids: " + properlySolved);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    class ValueComparator implements Comparator<String> {

        Map<String, Integer> base;
        public ValueComparator(Map<String, Integer> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with equals.
        public int compare(String a, String b) {
            if (base.get(a) >= base.get(b)) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys
        }
    }


}
