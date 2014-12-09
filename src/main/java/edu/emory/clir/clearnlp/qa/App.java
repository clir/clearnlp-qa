package edu.emory.clir.clearnlp.qa;

import edu.emory.clir.clearnlp.component.AbstractComponent;
import edu.emory.clir.clearnlp.dependency.DEPLib;
import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.nlp.NLPGetter;
import edu.emory.clir.clearnlp.qa.structure.document.EnglishDocument;
import edu.emory.clir.clearnlp.reader.TSVReader;
import edu.emory.clir.clearnlp.srl.SRLTree;
import edu.emory.clir.clearnlp.tokenization.AbstractTokenizer;
import edu.emory.clir.clearnlp.util.IOUtils;
import edu.emory.clir.clearnlp.util.arc.SRLArc;
import edu.emory.clir.clearnlp.util.lang.TLanguage;

import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    private EnglishDocument document;

    public App()
    {
        document = new EnglishDocument();

        String filename = "emory.txt.cnlp";
        TSVReader reader = new TSVReader(0,1,2,3,4,5,6,7);
        reader.open(IOUtils.createFileInputStream(filename));

        DEPTree tree = null;

        tree = reader.next();
        document.addInstances(tree);
        System.out.println(document);
        /*while ((tree = reader.next()) != null)
        {
            document.addInstances(tree);
        }*/
    }



    public static void main( String[] args )
    {
        App app = new App();
    }
}
