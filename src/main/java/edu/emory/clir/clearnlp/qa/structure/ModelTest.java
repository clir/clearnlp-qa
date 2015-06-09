package edu.emory.clir.clearnlp.qa.structure;

import edu.emory.clir.clearnlp.classification.prediction.StringPrediction;
import edu.emory.clir.clearnlp.classification.instance.StringInstance;
import edu.emory.clir.clearnlp.classification.model.StringModel;
import edu.emory.clir.clearnlp.classification.trainer.AbstractOnlineTrainer;
import edu.emory.clir.clearnlp.classification.trainer.AdaGradSVM;
import edu.emory.clir.clearnlp.classification.vector.StringFeatureVector;
import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.util.IOUtils;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author: Tomasz Jurczyk ({@code tomasz.jurczyk@emory.edu})
 */
public class ModelTest {
    public void train()
    {
        // collecting lexicons
        boolean binary = true;
        StringModel model = new StringModel(binary);
        DEPTree tree = null;

        String label = "true";
        StringInstance instance = new StringInstance(label, createFeatureVector(tree));
        model.addInstance(instance);


        int labelCutoff = 0, featureCutoff = 2;
        boolean average = true;
        double alpha = 0.01; // learning rate
        double rho = 0.1; // regularizaiton
        double bias = 0;

        AbstractOnlineTrainer trainer = new AdaGradSVM(model, labelCutoff, featureCutoff, average, alpha, rho, bias);
        trainer.train();

        ObjectOutputStream out = IOUtils.createObjectXZBufferedOutputStream("model.xz");
        //model.save(out);
        //out.close();

        ObjectInputStream in = IOUtils.createObjectXZBufferedInputStream("model.xz");
        //model.load(in);
        //in.close();

        StringPrediction p = model.predictBest(createFeatureVector(tree));
        p.getLabel(); // true
        p.getScore();
    }

    public StringFeatureVector createFeatureVector(DEPTree tree)
    {
        DEPNode node = tree.get(1);
        StringFeatureVector vector = new StringFeatureVector();

        // boolean
        int booleanType = 0;

        vector.addFeature(booleanType, "A");
        vector.addFeature(booleanType, "B");
        vector.addFeature(booleanType, "C");

        // string
        int dependencyLabelType = 1;
        vector.addFeature(dependencyLabelType, node.getLabel());

        int posTagType = 2;
        vector.addFeature(posTagType, node.getPOSTag());

        return vector;
    }


}
