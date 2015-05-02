package edu.emory.clir.clearnlp.qa;

import com.clearnlp.component.AbstractComponent;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.nlp.NLPGetter;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.tokenization.AbstractTokenizer;
import edu.emory.clir.clearnlp.qa.structure.document.EnglishDocument;

import com.clearnlp.nlp.*;

import org.apache.log4j.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class App 
{
    final String language = AbstractReader.LANG_EN;
    final String modelType  = "general-en";

    private EnglishDocument document;
    AbstractTokenizer tokenizer;
    AbstractComponent[] components = new AbstractComponent[5];

    public App()
    {

        BasicConfigurator.configure();
        try {
            tokenizer = NLPGetter.getTokenizer(language);

            components[0] = NLPGetter.getComponent(modelType, language, NLPMode.MODE_POS);
            components[1] = NLPGetter.getComponent(modelType, language, NLPMode.MODE_DEP);
            components[2] = NLPGetter.getComponent(modelType, language, NLPMode.MODE_PRED);
            components[3] = NLPGetter.getComponent(modelType, language, NLPMode.MODE_ROLE);
            components[4] = NLPGetter.getComponent(modelType, language, NLPMode.MODE_SRL);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void experiment()
    {
        document = new EnglishDocument();

        /* Read the input */
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("data/qa1_single-supporting-fact_test.txt"));
            String s;
            DEPTree tree;

            while((s = bufferedReader.readLine()) != null)
            {
                tree = NLPGetter.toDEPTree(tokenizer.getTokens(s));

                for (AbstractComponent component : components)
                {
                    component.process(tree);
                }

                //System.out.println("Before: " + tree.toStringSRL());

                document.addInstances(tree);
            }

            document.test();
            System.out.println("Document parsed to: " + document);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }


        String s = "Tomasz walked to the cinema.";
//        DEPTree tree;
//
//        tree = NLPGetter.toDEPTree(tokenizer.getTokens(s));
//
//        for (AbstractComponent component : components)
//            component.process(tree);
//
//        System.out.println("Document mapped to structure:" + tree.toStringSRL());
    }

    public static void main( String[] args )
    {
        App app = new App();
        app.experiment();
    }
}
