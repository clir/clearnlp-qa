package edu.emory.clir.clearnlp.qa;

import edu.emory.clir.clearnlp.qa.structure.SemanticType;

import java.util.*;

/**
 * @author: Tomasz Jurczyk ({@code tomasz.jurczyk@emory.edu})
 */
public class Globals {
    public static Map<SemanticType,Boolean> semanticRelations;

    static
    {
        HashMap<SemanticType,Boolean> semanticMap = new HashMap();

        semanticMap.put(SemanticType.A0, true);
        semanticMap.put(SemanticType.A1, true);
        semanticMap.put(SemanticType.A2, true);
        semanticMap.put(SemanticType.A3, true);
        semanticMap.put(SemanticType.A4, true);
        semanticMap.put(SemanticType.BNF, true);
        semanticMap.put(SemanticType.DIR, true);
        semanticMap.put(SemanticType.EXT, true);
        semanticMap.put(SemanticType.LOC, true);
        semanticMap.put(SemanticType.MNR, true);
        semanticMap.put(SemanticType.PRP, true);
        semanticMap.put(SemanticType.TMP, true);
        semanticMap.put(SemanticType.VOC, true);

        semanticRelations = Collections.unmodifiableMap(semanticMap);
    }
}
