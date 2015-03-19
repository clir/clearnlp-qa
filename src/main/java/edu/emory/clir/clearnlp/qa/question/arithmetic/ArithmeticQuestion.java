package edu.emory.clir.clearnlp.qa.question.arithmetic;

import edu.emory.clir.clearnlp.qa.structure.Instance;
import edu.emory.clir.clearnlp.qa.structure.document.EnglishDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Tomasz Jurczyk ({@code tomasz.jurczyk@emory.edu})
 */

public class ArithmeticQuestion {
    public EnglishDocument getDocument() {
        return document;
    }

    public void setDocument(EnglishDocument document) {
        this.document = document;
    }

    public Instance getQuestionRoot() {
        return questionRoot;
    }

    public void setQuestionRoot(Instance questionRoot) {
        this.questionRoot = questionRoot;
    }

    private EnglishDocument document;
    private Instance questionRoot;


}
