package edu.emory.clir.clearnlp.qa;

import edu.emory.clir.clearnlp.component.AbstractComponent;
import edu.emory.clir.clearnlp.dependency.DEPLib;
import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.qa.structure.document.EnglishDocument;
import edu.emory.clir.clearnlp.reader.TSVReader;
import edu.emory.clir.clearnlp.srl.SRLTree;
import edu.emory.clir.clearnlp.tokenization.AbstractTokenizer;
import edu.emory.clir.clearnlp.util.IOUtils;
import edu.emory.clir.clearnlp.util.arc.SRLArc;
import edu.emory.clir.clearnlp.util.lang.TLanguage;

import java.util.ArrayList;
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
        List<DEPTree> treeList = new ArrayList();

        DEPTree tree = null;

        while ((tree = reader.next()) != null)
        {
            treeList.add(tree);
        }

        document.addInstances(treeList);
        System.out.println("Document mapped to structure:");
        System.out.println(document);
    }



    public static void main( String[] args )
    {
        App app = new App();
    }
}
