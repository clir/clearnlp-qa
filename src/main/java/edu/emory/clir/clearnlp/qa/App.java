package edu.emory.clir.clearnlp.qa;

import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.pos.POSLibEn;
import edu.emory.clir.clearnlp.qa.question.arithmetic.ArithmeticQuestion;
import edu.emory.clir.clearnlp.qa.question.arithmetic.ArithmeticQuestions;
import edu.emory.clir.clearnlp.qa.question.arithmetic.util.Reader;
import edu.emory.clir.clearnlp.qa.structure.document.EnglishDocument;
import edu.emory.clir.clearnlp.reader.TSVReader;
import edu.emory.clir.clearnlp.util.IOUtils;

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

    public void calculateIdf()
    {
        int c = 0;
        for (char i = 'a'; i <= 'z'; i++){
            for (char j = 'a'; j <= 'z'; j++){
                String filename = filename_prefix + "." + i + j + ".cnlp";

                /* Check if file exists, if not break */
                File f = new File(filename);
                if(! f.exists()) break;

                HashMap<String,Boolean> qMap = new HashMap<String,Boolean>();

                /* Open and read the file using TSVReader */
                reader.open(IOUtils.createFileInputStream(filename));
                DEPTree tree = null;
                while((tree = reader.next()) != null){
                    //System.out.println("Reading for file = " + filename);
                    for (DEPNode node : tree){
                        if (POSLibEn.isVerb(node.getPOSTag())) {
                            qMap.put(node.getLemma(), true);
                        }
                    }
                }

                for(String word : qMap.keySet()){
                    if (wordsMap.containsKey(word)){
                        wordsMap.put(word, wordsMap.get(word) + 1);
                    } else {
                        wordsMap.put(word, 1);
                    }
                }
                c++;

            }
        }

        sorted_map.putAll(wordsMap);

        for (Map.Entry<String,Integer> entry : sorted_map.entrySet()){
            System.out.println("Word: " + entry.getKey() + ", value = " + round(Math.log((double) c / entry.getValue()), 2));

        }
    }

//    public void selectSumQuestions(HashMap<String, String> sumQuestions)
//    {
//        int a = 0;
//        for (char i = 'a'; i <= 'z'; i++) {
//            for (char j = 'a'; j <= 'z'; j++) {
//                String filename = filename_prefix + "." + i + j + ".cnlp";
//
//                /* Check if file exists */
//                File f = new File(filename);
//                if (!f.exists()) break;
//
//                /* Open and fast forward to the question (last sentence) */
//                reader.open(IOUtils.createFileInputStream(filename));
//                DEPTree tree = null;
//                DEPTree faster = null;
//                while ((tree = reader.next()) != null) {
//                    faster = tree;
//                }
//
//                /* Iterate through faster */
//                for (DEPNode node : faster) {
//                    /* If question is sum-question, mark it */
//                    if (node.getLemma().equalsIgnoreCase("total") || node.getLemma().equalsIgnoreCase("all") ||
//                            node.getLemma().equalsIgnoreCase("together")){
//
//                        a++;
//
//                        /* Open file and read its content */
//                        BufferedReader bufferedReader = null;
//
//                        try{
//                            bufferedReader = new BufferedReader(new FileReader(filename_prefix + "." + i + j));
//                            String text = null;
//                            while ((text = bufferedReader.readLine()) != null){
//                                System.out.println("Question = " + text);
//                                sumQuestions.put("" + i  + j, text);
//                            }
//                        } catch (FileNotFoundException e){
//                            e.printStackTrace();
//                        } catch (IOException e){
//                            e.printStackTrace();
//                        }
//
//                        break;
//                    }
//                }
//            }
//        }
//
//        System.out.println("a = " + a);
//
//    }

    public static void main( String[] args )
    {
        HashMap<String, String> sumQuestions = new HashMap();
        App app = new App();
        //app.calculateIdf();

        ArithmeticQuestions arithmeticQuestions = new ArithmeticQuestions();

        edu.emory.clir.clearnlp.qa.question.arithmetic.util.Reader areader = new Reader();

        try {
            ArithmeticQuestion aq;
            while ((aq = areader.read()) != null)
            {
                System.out.println("Got arithmticquestion: " + aq);
                arithmeticQuestions.add(aq);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
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
