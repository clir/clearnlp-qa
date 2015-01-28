package edu.emory.clir.clearnlp.qa.question.arithmetic;

import java.util.*;

public class ArithmeticQuestions<ArithmeticQuestion> extends AbstractCollection<ArithmeticQuestion>{
    private List<ArithmeticQuestion> questionsList;

    public ArithmeticQuestions()
    {
        questionsList = new ArrayList();
    }

    @Override
    public Iterator<ArithmeticQuestion> iterator() {
        Iterator<ArithmeticQuestion> it = questionsList.iterator();

        return it;
    }

    @Override
    public int size() {
        return questionsList.size();
    }

    @Override
    public boolean add(ArithmeticQuestion arithmeticQuestion) {
        return questionsList.add(arithmeticQuestion);
    }

}
