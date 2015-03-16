package edu.emory.clir.clearnlp.qa.question.arithmetic;

import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.qa.question.arithmetic.util.Reader;
import edu.emory.clir.clearnlp.qa.question.arithmetic.util.StringUtils;
import edu.emory.clir.clearnlp.qa.structure.Instance;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.dcoref.Mention;
import edu.stanford.nlp.pipeline.DeterministicCorefAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.*;

public class VerbApp {
    public static HashMap<Pair<String, String>, List<SequenceElement>> pairs = new HashMap();
    public static HashMap<Triplet<String,String,String>, List<SequenceElement>> triplets = new HashMap();
    public static final int FREQUENT_PATTERN_THRESHOLD = 5;
    ArithmeticQuestions arithmeticQuestions;
    List<ArithmeticQuestion> selectedQuestions = new ArrayList();
    List<Double> answers = new ArrayList();

    public static void main(String[] args) {

        VerbApp app = new VerbApp();
        app.readQuestions();
        app.prepareDataset();
    }

    private void prepareDataset()
    {
        /* Load equations */

        HashMap<Integer, String> eq = new HashMap();
        List<Double> all_answers = new ArrayList();

        try
        {
            BufferedReader br = new BufferedReader(new FileReader("eq.txt"));
            String line;
            int i = 0;
            while((line = br.readLine()) != null)
            {
                eq.put(i++, line);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            BufferedReader br = new BufferedReader(new FileReader("ans.txt"));
            String line;
            while((line = br.readLine()) != null)
            {
                all_answers.add(Double.parseDouble(line));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        /* Find equation pairs */
        ArithmeticQuestion aq;
        Iterator<ArithmeticQuestion> it = arithmeticQuestions.iterator();
        int i = 0;
        List<VCExperiment> experimentList = new ArrayList();

        System.out.println("size = " + arithmeticQuestions.size());

        while(it.hasNext())
        {
            aq = it.next();

            List<Double> factors = extractEquationFactors(eq.get(i));

            VCExperiment vcExperiment = new VCExperiment(aq, factors);

            //System.out.println("Question: " + aq.getQuestionText());
            //System.out.println("isValid = " + vcExperiment.isValid());

            if (vcExperiment.isValid())
            {
                experimentList.add(vcExperiment);
                vcExperiment.prepareData("dummy");
                System.out.println("Adding Question: " + aq.getQuestionText());
//                System.out.println("States: " + aq.getQuestionTextStateList());
//                System.out.println("Question State: " + aq.getQuestionState() + "\n");
                selectedQuestions.add(aq);
                answers.add(all_answers.get(i));
                //System.out.println(vcExperiment);
            }

            //System.out.println(factors);

            /* Create list of verbs + factors */

            for (Double d : factors) {
                for (State s : aq.getQuestionTextStateList()) {
                    Instance num_inst = s.getNumericalInstance();
                    DEPNode num_node = s.get(num_inst);

                    if (s.get(s.getPredicateInstance()).getLemma().equals("have") || s.get(s.getPredicateInstance()).getLemma().equals("be"))
                    {
                        continue;
                    }
                    if (num_node != null && Double.parseDouble(num_node.getWordForm()) == Math.abs(d)) {
                        //System.out.print(s.get(s.getPredicateInstance()).getLemma() + " " + d + " ");
                        break;
                    }
                }
            }

            i++;

            System.out.println();
        }

        experimentList.remove(experimentList.size()-1);
        selectedQuestions.remove(selectedQuestions.size()-1);
        answers.remove(selectedQuestions.size()-1);

        System.out.println("experimentList size = " + experimentList.size());
        int k_size = experimentList.size() / 3;


        for (int j = 0; j < 3; j++)
        {
            try
            {
                FileWriter trainfile = new FileWriter("train" + j + ".txt");
                FileWriter testfile = new FileWriter("test" + j + ".txt");

                int k = 0;
                System.out.println("Working on j = " + j);
                while(k < experimentList.size())
                {

                    if (k >= j*k_size && k < (j+1)*k_size)
                    {
                        System.out.println(k + " goes to test");
                        /* Train file */
                        testfile.write(experimentList.get(k).getTrainString() + "\n");
                    }
                    else
                    {
                        System.out.println(k + " goes to train");
                        /* Test file */
                        trainfile.write(experimentList.get(k).getTrainString() + "\n");
                    }

                    k++;
                }

                trainfile.close();
                testfile.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        /* Prepare polarity set */
        List<List<String>> polarity_set = new ArrayList();

        List<String> files = new ArrayList();
        files.add("output0.txt");
        files.add("output1.txt");
        files.add("output2.txt");

        Iterator<String> files_it = files.iterator();

        while(files_it.hasNext())
        {
            String filename = files_it.next();

            try
            {
                BufferedReader reader = new BufferedReader(new FileReader(filename));
                String line;
                List<String> polarities_of_verbs = new ArrayList();
                while ((line = reader.readLine()) != null)
                {
                    if (line.equals(""))
                    {
                        polarity_set.add(polarities_of_verbs);
                        polarities_of_verbs = new ArrayList();
                    }
                    else
                    {
                        polarities_of_verbs.add(line.split("\t")[1]);
                    }
                }

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        System.out.println("Size = " + polarity_set.size());

        /* Try to solve questions */
        for (int j = 0; j < selectedQuestions.size(); j++)
        {
            double res = selectedQuestions.get(j).sumUpAllStates(polarity_set.get(j));

            System.out.println("Question: " + selectedQuestions.get(j).getQuestionText());
            System.out.println("Expected results: " + answers.get(j) + ", calculated: " + res + "\n");
        }

    }

    private List<Double> extractEquationFactors(String equation)
    {
        List<Double> list = new ArrayList();
        double sign = 1;

        for (String token : equation.split(" "))
        {
            if (token.charAt(0) == '-') sign = -1;
            else if (token.charAt(0) == '+') sign = 1;

            if (StringUtils.isDouble(token) || StringUtils.isInteger(token))
            {

                Double x = Double.parseDouble(token);
                x *= sign;
                list.add(x);
            }
        }

        list.add(1.0);

        return list;
    }

    private void readQuestions()
    {
        arithmeticQuestions = new ArithmeticQuestions();

        edu.emory.clir.clearnlp.qa.question.arithmetic.util.Reader areader = new Reader();

        try {
            ArithmeticQuestion aq;

//            aq = areader.read("files/", "arith-qs.ah");
//            arithmeticQuestions.add(aq);
//            System.out.println("Question: " + aq.getQuestionText());
//            System.out.println("States: " + aq.getQuestionTextStateList());
//            System.out.println("Question State: " + aq.getQuestionState() + "\n");

            while ((aq = areader.read()) != null) {
                arithmeticQuestions.add(aq);

//                System.out.println("Question: " + aq.getQuestionText());
//                System.out.println("States: " + aq.getQuestionTextStateList());
//                System.out.println("Question State: " + aq.getQuestionState() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void doExperiment()
    {
        edu.emory.clir.clearnlp.qa.question.arithmetic.util.Reader areader = new Reader();
        int counter = 0;

        try {
            ArithmeticQuestion aq;
            while ((aq = areader.read()) != null) {
                arithmeticQuestions.add(aq);

                /* Iterate through all verbs and get all pairs */
//                System.out.println("Question: " + aq.getQuestionText());
//                System.out.println("States: " + aq.getQuestionTextStateList());
//                System.out.println("Question state: " + aq.getQuestionState());

                if (aq.getQuestionTextStateList() == null)
                {
                    counter++;
                }

                List<State> stateList = aq.getQuestionTextStateList();

                System.out.println("Question = " + aq.getQuestionText());
                System.out.println(aq.getQuestionTextStateList());
                System.out.println();

                State prev = null;
                State next = null;
                Iterator<State> it = stateList.iterator();

                /* Do the work for text instances */
                if (it.hasNext()) prev = it.next();

                while (it.hasNext()) {
                    next = it.next();

                    updatePairEntry(prev, next, aq);

                    prev = next;
                }

                /* Do the work for question state */
                if (prev == null) continue;

                if ((next = aq.getQuestionState()) != null) {
                    updatePairEntry(prev, next, aq);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Counter = " + counter);

        /* Sort pairs */
        ValueComparator bvc = new ValueComparator(pairs);
        TreeMap<Pair<String, String>,List<SequenceElement>> sorted_map = new TreeMap<>(bvc);
        sorted_map.putAll(pairs);

//        for (Map.Entry entry : sorted_map.entrySet())
//        {
//            System.out.println(entry.getKey() + " -> " + ((List<State>) entry.getValue()).size());
//        }

        //System.out.println("Triplets: ");

//        findTriplets();
//
//        for (Map.Entry entry : triplets.entrySet())
//        {
//            System.out.println(entry.getKey() + " -> " + ((List<State>) entry.getValue()).size());
//        }
    }

    private void updatePairEntry(State s1, State s2, ArithmeticQuestion aq) {

        if (s2.getPredicateInstance() == null) {
            return;
        }

        String prev_verb = s1.get(s1.getPredicateInstance()).getLemma();
        String next_verb = s2.get(s2.getPredicateInstance()).getLemma();

        Pair<String, String> pair = new Pair(prev_verb, next_verb);

        List<SequenceElement> sequenceElementList = pairs.get(pair);

        if (sequenceElementList == null) {
            sequenceElementList = new ArrayList();
            pairs.put(pair, sequenceElementList);
        }

        SequenceElement se = new SequenceElement(s1, aq);
        sequenceElementList.add(se);
    }

    private void findTriplets()
    {

        /* Iterate through all of the elements */
        for (Map.Entry<Pair<String, String>, List<SequenceElement>> entry : pairs.entrySet())
        {
            /* If less than threshold, skip */
            if (entry.getValue().size() < FREQUENT_PATTERN_THRESHOLD)
            {
                continue;
            }

            /* For each element in list of states */
            for (SequenceElement s : entry.getValue())
            {
                ArithmeticQuestion aq = s.aq;
                State startState = s.startState;

                /* Check previous -> pair */
                State prev = null;

                int startStateIndex = aq.getQuestionTextStateList().indexOf(startState);

                if (startStateIndex > 0)
                {
                    /* Triplet prev -> Pair */

                    prev = aq.getQuestionTextStateList().get(startStateIndex - 1);
                    //System.out.println("Q: " + aq.getQuestionText() + ", prev = " + prev);
                    String prevString = prev.get(prev.getPredicateInstance()).getLemma();
                    Triplet<String,String,String> triple = new Triplet(prevString, entry.getKey().getValue0(), entry.getKey().getValue1());
                    updateTripleEntry(triple, aq, prev);
                    //System.out.println("After: " + triplets.containsKey(triple));
                }

                /* Check pair -> next */
                prev = null;
                if (startStateIndex + 2 < aq.getQuestionTextStateList().size())
                {
                    prev = aq.getQuestionTextStateList().get(startStateIndex + 2);
                }
                else if (startStateIndex + 2 == aq.getQuestionTextStateList().size())
                {
                    prev = aq.getQuestionState();
                }

                if (prev != null && prev.getPredicateInstance() != null)
                {
                    String prevString = prev.get(prev.getPredicateInstance()).getLemma();
                    Triplet<String,String,String> triple = new Triplet(prevString, entry.getKey().getValue0(), entry.getKey().getValue1());
                    updateTripleEntry(triple, aq, prev);
                }
            }
        }
    }

    private void updateTripleEntry(Triplet triple, ArithmeticQuestion aq, State startState)
    {
        List<SequenceElement> sequenceElementList = triplets.get(triple);

        if (sequenceElementList == null)
        {
            sequenceElementList = new ArrayList();
            triplets.put(triple, sequenceElementList);

        }

        SequenceElement sequenceElement = new SequenceElement(startState, aq);
        sequenceElementList.add(sequenceElement);
    }

    static class ValueComparator implements Comparator<Pair<String, String>> {

        Map<Pair<String, String>, List<SequenceElement>> base;

        public ValueComparator(Map<Pair<String, String>, List<SequenceElement>> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with equals.
        public int compare(Pair<String, String> a, Pair<String, String> b) {
            if (base.get(a).size() >= base.get(b).size()) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys


        }
    }

    private class SequenceElement
    {
        private State startState;
        private ArithmeticQuestion aq;

        public SequenceElement(State s, ArithmeticQuestion arithmeticQuestion)
        {
            startState = s;
            aq = arithmeticQuestion;
        }
    }
}
