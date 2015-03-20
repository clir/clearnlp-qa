package edu.emory.clir.clearnlp.qa.question.arithmetic;

import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.pos.POSLibEn;
import edu.emory.clir.clearnlp.qa.structure.Instance;
import edu.emory.clir.clearnlp.qa.structure.SemanticType;
import edu.emory.clir.clearnlp.qa.structure.attribute.AttributeType;
import edu.emory.clir.clearnlp.qa.structure.document.EnglishDocument;
import edu.emory.clir.clearnlp.qa.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author: Tomasz Jurczyk ({@code tomasz.jurczyk@emory.edu})
 */

public class ArithmeticQuestion {
    /* Rough part */
    private EnglishDocument document;
    private Instance questionRoot;
    private String questionText;
    private HashMap<Instance,Boolean> visitedInstances = new HashMap();

    public EnglishDocument getDocument()
    {
        return document;
    }

    public void setDocument(EnglishDocument document)
    {
        this.document = document;
    }

    public Instance getQuestionRoot()
    {
        return questionRoot;
    }

    public void setQuestionRoot(Instance questionRoot)
    {
        this.questionRoot = questionRoot;
    }

    public String getQuestionText()
    {
        return questionText;
    }

    public void setQuestionText(String questionText)
    {
        this.questionText = questionText;
    }

    /* Actual part of extracted information */
    private List<String> actors = new ArrayList();
    private List<String> themes = new ArrayList();
    private List<String> attrs  = new ArrayList();
    private List<String> verbs  = new ArrayList();

    public void processQuestion()
    {
        /* Iterate through every instance and look for any verbs */
        List<Instance> verbInstances = new ArrayList();

        for (Instance inst: document.getInstances())
        {
            DEPNode instNode = document.getDEPNode(inst);

            if (POSLibEn.isVerb(instNode.getPOSTag()))
            {
                /* If verb, look for any states under it */
                processVerb(inst);
            }
        }

        /* Process question state */
        processQuestionState(questionRoot);
    }

    private void processVerb(Instance verbInstance)
    {
        /* Try to retrieve A0 label */
        Instance actor = null;

        if (verbInstance.getArgumentList(SemanticType.A0) != null
                && verbInstance.getArgumentList(SemanticType.A0).size() == 1)
        {
            actor = verbInstance.getArgumentList(SemanticType.A0).get(0);
        }

        List<Instance> queue = new ArrayList();
        queue.add(verbInstance);
    }

    private void processQuestionState(Instance rootInstance)
    {

    }
}
