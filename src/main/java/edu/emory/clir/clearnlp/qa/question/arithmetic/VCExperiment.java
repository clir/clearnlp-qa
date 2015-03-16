package edu.emory.clir.clearnlp.qa.question.arithmetic;

import com.sun.tools.doclint.HtmlTag;
import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.qa.question.arithmetic.util.StringUtils;
import edu.emory.clir.clearnlp.qa.structure.Instance;
import edu.emory.clir.clearnlp.qa.structure.SemanticType;
import edu.emory.clir.clearnlp.qa.structure.attribute.AttributeType;

import java.awt.font.NumericShaper;
import java.util.*;

public class VCExperiment {
    private ArithmeticQuestion arithmeticQuestion;

    public String getTrainString() {
        return trainString;
    }

    public String getTestString() {
        return testString;
    }

    private String trainString = "";
    private String testString = "";

    public boolean isValid() {
        return isValid;
    }

    private boolean isValid;

    /* Selected Verbs */
    List<String> selectedVerbs = new ArrayList();
    int          numberOfVerbs = 0;

    /* Selected Themes */
    List<String> selectedThemes = new ArrayList();
    int          numberOfThemes = 0;

    /* Question data */
    String questionVerb;
    String questionTheme;

    /* Selected polarities */
    List<Double> selectedPolarities = new ArrayList();
    List<String> Polarities = new ArrayList();

    /* theme frequency */
    List<Double> themeFrequencies = new ArrayList();

    /* verb frequency */
    List<Double> verbFrequencies = new ArrayList();

    /* Has A0 label */
    List<Boolean> hasA0Labels = new ArrayList();

    /* Has A1 Label */
    List<Boolean> hasA2Labels = new ArrayList();

    /* Has the same theme as question */
    List<Boolean> sameThemeAsQ = new ArrayList();

    /* Distance to theme */
    List<Integer> distanceToQ = new ArrayList();

    /* A0 and A2 labels */
    List<String> A0Labels = new ArrayList();
    List<String> A2Labels = new ArrayList();


    public VCExperiment(ArithmeticQuestion _arithmeticQuestion, List<Double> _selectedPolarities){
        arithmeticQuestion = _arithmeticQuestion;
        selectedPolarities = _selectedPolarities;

        extractVerbsThemes();
        System.out.println(selectedVerbs);
        isValid = validateQuestion();
    }

    private boolean validateQuestion()
    {
        /* Check if list of verbs is equal or greater than list of equationFactors */
        if (selectedVerbs.size() < selectedPolarities.size())
        {
            return false;
        }

        /* Check if all text state have themes */
        for (State s : arithmeticQuestion.getQuestionTextStateList())
        {
            Instance pred_inst = s.getPredicateInstance();
            Instance theme_inst = pred_inst.getArgumentList(SemanticType.A1).get(0);
            DEPNode theme_node = null;

            try
            {
                theme_node = s.get(theme_inst);
            }
            catch (NullPointerException e)
            {
                e.printStackTrace();
            }

            if (theme_node == null) return false;
        }

        /* Check if question state has theme and predicate */
        Instance pred_inst = arithmeticQuestion.getQuestionState().getPredicateInstance();
        if (pred_inst == null)
        {
            return false;
        }

        Instance theme_inst = pred_inst.getArgumentList(SemanticType.A1).get(0);
        DEPNode theme_node = null;

        try
        {
            theme_node = arithmeticQuestion.getQuestionState().get(theme_inst);
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }

        if (theme_node == null) return false;

        String theme_str = theme_node.getLemma();

        if (theme_inst.getAttribute(AttributeType.QUALITY) != null && theme_inst.getAttribute(AttributeType.QUALITY).size() > 0)
        {
            DEPNode attr_node = arithmeticQuestion.getQuestionState().get(theme_inst.getAttribute(AttributeType.QUALITY).get(0));
            theme_str += "_" + attr_node.getLemma();
        }

        questionTheme = theme_str;
        selectedThemes.add(questionTheme);

        if (questionVerb.equals("")) return false;

        return true;
    }

    private void extractVerbsThemes()
    {
        for (State s : arithmeticQuestion.getQuestionTextStateList())
        {
            Instance pred_inst = s.getPredicateInstance();
            Instance theme_inst = null;
            try {
                theme_inst = pred_inst.getArgumentList(SemanticType.A1).get(0);
            } catch (NullPointerException e)
            {
                System.out.println("Question: " + arithmeticQuestion.getQuestionText());
                System.exit(0);
            }
            DEPNode  pred_node = s.get(pred_inst);

            selectedVerbs.add(pred_node.getLemma());

            DEPNode theme_node = s.get(theme_inst);
            String theme_str = "";

            if (theme_node != null)
            {
                theme_str += theme_node.getLemma();
            }

            if (theme_inst.getAttribute(AttributeType.QUALITY) != null && theme_inst.getAttribute(AttributeType.QUALITY).size() > 0)
            {
                DEPNode attr_node = s.get(theme_inst.getAttribute(AttributeType.QUALITY).get(0));
                theme_str += "_" + attr_node.getLemma();
            }

            if (! theme_str.equals(""))
            {
                selectedThemes.add(theme_str);
            }
        }

        Instance pred_inst = arithmeticQuestion.getQuestionState().getPredicateInstance();
        DEPNode  pred_node = arithmeticQuestion.getQuestionState().get(pred_inst);

        if (pred_node == null) questionVerb = "";
        else questionVerb = pred_node.getLemma();

        if (! questionVerb.equals(""))
        {
            selectedVerbs.add(questionVerb);
        }

    }

    public void prepareData(String filename)
    {
        if (! isValid)
        {
            return;
        }

        /* Prepare polarities set */
        preparePolarities();
        //System.out.println("po: " + Polarities);

        /* Prepare theme frequencies */
        prepareThemeFrequency();
        //System.out.println("tf: " + themeFrequencies);

        /* Prepare verb frequencies */
        prepareVerbsFrequency();
        //System.out.println("vf: " + verbFrequencies);

        /* Prepare A0 labels */
        prepareSemanticLabels(hasA0Labels, SemanticType.A0);
        //System.out.println("a0: " + hasA0Labels);

        /* Prepare A2 Labels */
        prepareSemanticLabels(hasA2Labels, SemanticType.A2);
        //System.out.println("a2: " + hasA2Labels);

        /* Same theme as question */
        hasSameThemeAsQ();
        //System.out.println("st: " + sameThemeAsQ);

        /* Distance to the question */
        calculateDistances();
        //System.out.println("dq: " + distanceToQ);

        /* Prepare A0 and A2 labels */
        prepareA0A2Labels();

        for (int i = 0; i < Polarities.size(); i++)
        {
            //System.out.print(Polarities.get(i) + "\t");
            trainString += Polarities.get(i) + "\t";
            testString += "x" + "\t";

            String s = "";
            s += "verb[0]=" + selectedVerbs.get(i) + " ";
            s += "tf[0]=" + themeFrequencies.get(i) + " ";
            s += "vf[0]=" + verbFrequencies.get(i) + " ";
//            s += "a0=" + hasA0Labels.get(i) + " ";
//            s += "a2=" + hasA2Labels.get(i) + " ";
            s += "st=" + sameThemeAsQ.get(i) + " ";
            s += "dq=" + distanceToQ.get(i) + " ";



//            System.out.print("verb[0]=" + selectedVerbs.get(i) + " ");
//            System.out.print("tf[0]=" + themeFrequencies.get(i) + " ");
//            System.out.print("vf[0]=" + verbFrequencies.get(i) + " ");
//            System.out.print("a0=" + hasA0Labels.get(i) + " ");
//            System.out.print("a2=" + hasA2Labels.get(i) + " ");
//            System.out.print("st=" + sameThemeAsQ.get(i) + " ");
//            System.out.print("dq=" + distanceToQ.get(i) + " ");



            if (i - 1 >= 0)
            {
                s += "verb[-1]=" + selectedVerbs.get(i-1) + " ";
            }

            if (i - 2 >= 0)
            {
                s += "verb[-2]=" + selectedVerbs.get(i-2) + " ";
            }

            if (i + 1 < Polarities.size())
            {
                s += "verb[1]=" + selectedVerbs.get(i+1) + " ";
            }

            if (i + 2 < Polarities.size())
            {
                s += "verb[2]=" + selectedVerbs.get(i+2) + " ";
            }

            if (i < Polarities.size() - 1)
            {
                //System.out.println("Comparing: " + selectedThemes.get(i) + " with " + selectedThemes.get(Polarities.size()-1));
                if (selectedVerbs.get(i).equals(selectedVerbs.get(Polarities.size()-1)))
                {
                    s += "sv=true ";
                }
                else
                {
                    s += "sv=false ";
                }
            }

            /* A0/A2 labels of [0] matching with -1 */
            if (i - 1 >= 0)
            {
                if (A0Labels.get(i).equals(A0Labels.get(i-1)))
                {
                    s += "A0[0]A0[-1]=1 ";
                }
                else
                {
                    s += "A0[0]A0[-1]=0 ";
                }

                if (A0Labels.get(i).equals(A2Labels.get(i-1)))
                {
                    s += "A0[0]A2[-1]=1 ";
                }
                else
                {
                    s += "A0[0]A2[-1]=0 ";
                }

                if (A2Labels.get(i).equals(A0Labels.get(i-1)))
                {
                    s += "A2[0]A0[-1]=1 ";
                }
                else
                {
                    s += "A2[0]A0[-1]=0 ";
                }

                if (A2Labels.get(i).equals(A2Labels.get(i-1)))
                {
                    s += "A2[0]A2[-1]=1 ";
                }
                else
                {
                    s += "A2[0]A2[-1]=0 ";
                }
            }

            /* A0/A2 labels of [0] matching with 1 */
            if (i +1 < Polarities.size())
            {
                if (A0Labels.get(i).equals(A0Labels.get(i+1)))
                {
                    s += "A0[0]A0[1]=1 ";
                }
                else
                {
                    s += "A0[0]A0[1]=0 ";
                }

                if (A0Labels.get(i).equals(A2Labels.get(i+1)))
                {
                    s += "A0[0]A2[1]=1 ";
                }
                else
                {
                    s += "A0[0]A2[1]=0 ";
                }

                if (A2Labels.get(i).equals(A0Labels.get(i+1)))
                {
                    s += "A2[0]A0[1]=1 ";
                }
                else
                {
                    s += "A2[0]A0[1]=0 ";
                }

                if (A2Labels.get(i).equals(A2Labels.get(i+1)))
                {
                    s += "A2[0]A2[1]=1 ";
                }
                else
                {
                    s += "A2[0]A2[1]=0 ";
                }
            }

            /* A0/A2 labels between 0 and question */
            if (i < Polarities.size() - 1)
            {
                if (A0Labels.get(i).equals(A0Labels.get(Polarities.size() -1)))
                {
                    s += "A0[0]A0[q]=1 ";
                }
                else
                {
                    s += "A0[0]A0[q]=1 ";
                }
                if (A0Labels.get(i).equals(A2Labels.get(Polarities.size() -1)))
                {
                    s += "A0[0]A2[q]=1 ";
                }
                else
                {
                    s += "A0[0]A2[q]=1 ";
                }
                if (A2Labels.get(i).equals(A0Labels.get(Polarities.size() -1)))
                {
                    s += "A2[0]A0[q]=1 ";
                }
                else
                {
                    s += "A2[0]A0[q]=1 ";
                }
                if (A2Labels.get(i).equals(A2Labels.get(Polarities.size() -1)))
                {
                    s += "A2[0]A2[q]=1 ";
                }
                else
                {
                    s += "A2[0]A2[q]=1 ";
                }
            }

            /* theme|theme */
            if (i - 1 >= 0 && i + 1 < Polarities.size())
            {
                s += "th[-1]|th[0]|th[1]=" + selectedThemes.get(i-1) + "|" + selectedThemes.get(i) + "|" + selectedThemes.get(i+1) + " ";
                s += "vb[-1]|vb[0]|vb[1]=" + selectedVerbs.get(i-1) + "|" + selectedVerbs.get(i) + "|" + selectedVerbs.get(i+1) + " ";
            }
            else if (i - 1 >= 0)
            {
                s += "th[-1]|th[0]=" + selectedThemes.get(i-1) + "|" + selectedThemes.get(i) + " ";
                s += "vb[-1]|vb[0]=" + selectedVerbs.get(i-1) + "|" + selectedVerbs.get(i) + " ";
            }
            else if (i + 1 < Polarities.size())
            {
                s += "th[0]|th[1]=" + selectedThemes.get(i) + "|" + selectedThemes.get(i+1) + " ";
                s += "vb[0]|vb[1]=" + selectedVerbs.get(i) + "|" + selectedVerbs.get(i+1) + " ";
            }

            /* A0|A0 and A2|A2 */
            if (i - 1 >= 0 && i + 1 < Polarities.size())
            {
                s += "A0[-1]|A0[0]|A0[1]=" + A0Labels.get(i-1) + "|" + A0Labels.get(i) + "|" + A0Labels.get(i+1) + " ";
                s += "A2[-1]|A2[0]|A2[1]=" + A2Labels.get(i-1) + "|" + A2Labels.get(i) + "|" + A2Labels.get(i+1) + " ";
            }
            else if (i - 1 >= 0)
            {
                s += "A0[-1]|A0[0]=" + A0Labels.get(i-1) + "|" + A0Labels.get(i) + " ";
                s += "A2[-1]|A2[0]=" + A2Labels.get(i-1) + "|" + A2Labels.get(i) + " ";
            }
            else if (i + 1 < Polarities.size())
            {
                s += "A0[0]|A0[1]=" + A0Labels.get(i) + "|" + A0Labels.get(i+1) + " ";
                s += "A2[0]|A2[1]=" + A2Labels.get(i) + "|" + A2Labels.get(i+1) + " ";
            }

            trainString += s + "\n";
            testString += s + "\n";

//            System.out.println();
        }
    }

    private void preparePolarities()
    {
        Iterator<State>  it_states     = arithmeticQuestion.getQuestionTextStateList().iterator();

        while(it_states.hasNext())
        {
            State s = it_states.next();

            Instance pred_inst  = s.getPredicateInstance();
            Instance theme_inst = pred_inst.getArgumentList(SemanticType.A1).get(0);
            Instance num_inst = null;
            num_inst = theme_inst.getAttribute(AttributeType.QUANTITY).get(0);

            DEPNode num_node    = s.get(num_inst);

            if (! StringUtils.isDouble(num_node.getWordForm()))
            {
                Polarities.add("0");
                continue;
            }

            double d = Double.parseDouble(num_node.getWordForm());

            double currentPolarity = getSign(d);

            if (d == currentPolarity || d == (-1) * currentPolarity)
            {
                if (currentPolarity > 0)
                {
                    Polarities.add("+");
                }
                else
                {
                    Polarities.add("-");
                }
            }
            else
            {
                Polarities.add("0");
            }
        }

        Polarities.add("+");
    }

    private double getSign(double number)
    {
        for (double d: selectedPolarities)
        {
            if (number == d || number == (-1) * d )
            {
                return d;
            }
        }

        return 0;
    }

    private void prepareThemeFrequency()
    {
        HashMap<String, Integer> fq = new HashMap<>();

        for (String s : selectedThemes)
        {
            numberOfThemes++;
            if (! fq.containsKey(s))
            {
                fq.put(s, 1);
            }
            else
            {
                fq.put(s, fq.get(s) + 1);
            }
        }

        for (String s : selectedThemes)
        {
            int theme_freq = fq.get(s);
            double freq = (double)theme_freq/numberOfThemes;
            themeFrequencies.add(freq);
        }
    }

    private void prepareVerbsFrequency()
    {
        HashMap<String, Integer> fq = new HashMap<>();

        for (String s : selectedVerbs)
        {
            numberOfVerbs++;
            if (! fq.containsKey(s))
            {
                fq.put(s, 1);
            }
            else
            {
                fq.put(s, fq.get(s) + 1);
            }
        }

        for (String s : selectedVerbs)
        {
            int verb_freq = fq.get(s);
            double freq = (double)verb_freq/numberOfVerbs;
            verbFrequencies.add(freq);
        }
    }

    private void prepareSemanticLabels(List<Boolean> list, SemanticType semanticType)
    {
        List<State> stateList = new ArrayList();
        stateList.addAll(arithmeticQuestion.getQuestionTextStateList());
        stateList.add(arithmeticQuestion.getQuestionState());

        for (State s: stateList)
        {
            Instance pred_inst = s.getPredicateInstance();
            DEPNode  pred_node = s.get(pred_inst);

            if (pred_inst == null) return;

            List<DEPNode> dependentList = pred_node.getDependentList();
            boolean hasDesiredLabel = false;

            for (DEPNode node: dependentList)
            {
                SemanticType nodeLabel = StringUtils.extractSemanticRelation(node.getSemanticLabel(pred_node));

                if (nodeLabel != null && nodeLabel == semanticType)
                {
                    hasDesiredLabel = true;
                }
            }

            if (hasDesiredLabel)
            {
                list.add(true);
            }
            else
            {
                list.add(false);
            }
        }


    }

    private void hasSameThemeAsQ()
    {
        for (String verb: selectedThemes)
        {
            if (verb.equals(questionTheme))
            {
                sameThemeAsQ.add(true);
            }
            else
            {
                sameThemeAsQ.add(false);
            }
        }
    }

    private void calculateDistances()
    {
        int verbsLength = selectedVerbs.size();
        for (int i = 0; i < verbsLength; i++)
        {
            distanceToQ.add(verbsLength - i - 1);
        }
    }

    private void prepareA0A2Labels()
    {
        List<State> stateList = new ArrayList();
        stateList.addAll(arithmeticQuestion.getQuestionTextStateList());
        stateList.add(arithmeticQuestion.getQuestionState());

        for (State s: stateList)
        {
            Instance pred_inst = s.getPredicateInstance();
            Instance A0_inst;
            Instance A2_inst;

            if (pred_inst.getArgumentList(SemanticType.A0) != null &&
                    pred_inst.getArgumentList(SemanticType.A0).size() > 0)
            {
                A0_inst = pred_inst.getArgumentList(SemanticType.A0).get(0);
                if (s.get(A0_inst) != null)
                {
                    A0Labels.add(s.get(A0_inst).getLemma());
                }
            }
            else
            {
                A0Labels.add("none");
            }

            if (pred_inst.getArgumentList(SemanticType.A2) != null &&
                    pred_inst.getArgumentList(SemanticType.A2).size() > 0)
            {
                A2_inst = pred_inst.getArgumentList(SemanticType.A2).get(0);
                if (s.get(A2_inst) != null)
                {
                    A2Labels.add(s.get(A2_inst).getLemma());
                }
            }
            else
            {
                A2Labels.add("none");
            }
        }

//        System.out.println("A0 labels = " + A0Labels);
//        System.out.println("A2 labels = " + A2Labels);
    }

    public String toString()
    {
//        String s;
//        s = "verbs = " + selectedVerbs + "\n";
//        s += "theme = " + selectedThemes + "\n";
//        s += "t. f. = " + themeFrequencies + "\n";
//        s += "v. f. = " + verbFrequencies + "\n";
//        s += "   a0 = " + hasA0Labels + "\n";
//        s += "   a2 = " + hasA2Labels + "\n";
//        s += "   st = " + sameThemeAsQ + "\n";
//        s += "   dq = " + distanceToQ + "\n";
//
//        return s;

        return trainString;

    }
}
