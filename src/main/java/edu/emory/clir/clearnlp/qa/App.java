package edu.emory.clir.clearnlp.qa;

import com.clearnlp.component.AbstractComponent;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.nlp.NLPGetter;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.tokenization.AbstractTokenizer;
import edu.emory.clir.clearnlp.qa.structure.document.EnglishDocument;

import com.clearnlp.nlp.*;


/**
 * Hello world!
 *
 */
public class App 
{
    private EnglishDocument document;

    public App()
    {
        final String language = AbstractReader.LANG_EN;
        document = new EnglishDocument();
        String s = "Tomasz walked to cinema.";
        DEPTree tree = null;

        try
        {
            AbstractTokenizer tokenizer = NLPGetter.getTokenizer(language);
            AbstractComponent tagger = NLPGetter.getComponent("general-en", language, NLPMode.MODE_POS);
            AbstractComponent parser = NLPGetter.getComponent("general-en", language, NLPMode.MODE_DEP);
            AbstractComponent identifier = NLPGetter.getComponent("general-en", language, NLPMode.MODE_PRED);
            AbstractComponent classifier = NLPGetter.getComponent("general-en", language, NLPMode.MODE_ROLE);
            AbstractComponent labeler = NLPGetter.getComponent("general-en", language, NLPMode.MODE_SRL);

            AbstractComponent[] components = {tagger, parser, identifier, classifier, labeler};

            tree = NLPGetter.toDEPTree(tokenizer.getTokens(s));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        System.out.println("Document mapped to structure:" + tree);


    }



    public static void main( String[] args )
    {
        App app = new App();
    }
}
