package edu.emory.clir.clearnlp.qa.question.arithmetic;

import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.qa.structure.Instance;
import edu.emory.clir.clearnlp.qa.structure.SemanticType;

import java.util.ArrayList;
import java.util.List;

public class VCExperiment {
    private ArithmeticQuestion arithmeticQuestion;

    public boolean isValid() {
        return isValid;
    }

    private boolean isValid;

    /* Selected Verbs */
    List<String> selectedVerbs = new ArrayList();

    /* Question verb */
    String questionVerb;

    /* Selected polarities */
    List<Double> selectedPolarities = new ArrayList();

    /* theme frequency */

    public VCExperiment(ArithmeticQuestion _arithmeticQuestion, List<Double> _selectedPolarities){
        arithmeticQuestion = _arithmeticQuestion;
        selectedPolarities = _selectedPolarities;

        extractVerbs();
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

        if (questionVerb.equals("")) return false;

        return true;
    }

    private void extractVerbs()
    {
        for (State s : arithmeticQuestion.getQuestionTextStateList())
        {
            Instance pred_inst = s.getPredicateInstance();
            DEPNode  pred_node = s.get(pred_inst);

            selectedVerbs.add(pred_node.getLemma());
        }

        Instance pred_inst = arithmeticQuestion.getQuestionState().getPredicateInstance();
        DEPNode  pred_node = arithmeticQuestion.getQuestionState().get(pred_inst);

        if (pred_node == null) questionVerb = "";
        else questionVerb = pred_node.getLemma();
    }

    public void exportData(String filename)
    {

    }

}
