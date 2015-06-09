package edu.emory.clir.clearnlp.qa;

import edu.smu.tspell.wordnet.*;

import java.util.HashSet;
import java.util.Set;

/**
 * @author: Tomasz Jurczyk ({@code tomasz.jurczyk@emory.edu})
 */
public class App2 {
    public static void main(String [] args)
    {
        System.setProperty("wordnet.database.dir", "wordnet_dict/");

        VerbSynset verbSynset;
        VerbSynset[] hyponyms;

        WordNetDatabase database = WordNetDatabase.getFileInstance();
        Synset[] synsets = database.getSynsets("buy ", SynsetType.VERB);
        Set<String> synonyms = new HashSet<>();

        for (int i = 0; i < synsets.length; i++) {
            for (String syn : synsets[i].getWordForms())
                synonyms.add(syn);
        }

        System.out.println(synonyms.toString());

//            verbSynset = (VerbSynset)(synsets[i]);
//            hyponyms = verbSynset.getVerbGroup();
//            System.out.println(verbSynset.getWordForms()[0] +
//                    ": " + verbSynset.getDefinition() + ") has " + hyponyms.length + " hypernyms");
//
//            for (int j = 0; j < verbSynset.getVerbGroup().length; j++)
//            {
//                System.out.println("Verb group: " + verbSynset.getVerbGroup()[j].getVerbGroup()[0]);
//            }
        //}
    }
}
