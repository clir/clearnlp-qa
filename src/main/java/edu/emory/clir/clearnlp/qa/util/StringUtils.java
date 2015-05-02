package edu.emory.clir.clearnlp.qa.util;

import edu.emory.clir.clearnlp.qa.structure.SemanticType;

public class StringUtils {
    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        }

        return true;
    }

    public static boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
        } catch(NumberFormatException e) {
            return false;
        }

        return true;
    }

    public static SemanticType getSemanticType(String relation)
    {
        /* This is because in 2.0 root is not being returned in getLabel() */
        if (relation == null) {
            return SemanticType.root;
        }
        /* ********************************** */

        System.out.println("Relation = " + relation);

        if (relation.contains("-"))
        {
            relation = relation.split("-")[1];
        }
        else if (relation.contains("="))
        {
            relation = relation.split("=")[0];
        }

        switch (relation)
        {
            case "A0":
                return SemanticType.A0;
            case "A1":
                return SemanticType.A1;
            case "A2":
                return SemanticType.A2;
            case "A3":
                return SemanticType.A3;
            case "A4":
                return SemanticType.A4;
            case "BNF":
                return SemanticType.BNF;
            case "DIR":
                return SemanticType.DIR;
            case "EXT":
                return SemanticType.EXT;
            case "LOC":
                return SemanticType.LOC;
            case "MNR":
                return SemanticType.MNR;
            case "PRP":
                return SemanticType.PRP;
            case "TMP":
                return SemanticType.TMP;
            case "VOC":
                return SemanticType.VOC;
            case "pass":
                return SemanticType.pass;
            case "subj":
                return SemanticType.subj;
            case "acomp":
                return SemanticType.acomp;
            case "advcl":
                return SemanticType.advcl;
            case "advmod":
                return SemanticType.advmod;
            case "agent":
                return SemanticType.agent;
            case "amod":
                return SemanticType.amod;
            case "appos":
                return SemanticType.appos;
            case "attr":
                return SemanticType.attr;
            case "auxpass":
                return SemanticType.auxpass;
            case "aux":
                return SemanticType.aux;
            case "cc":
                return SemanticType.cc;
            case "ccomp":
                return SemanticType.ccomp;
            case "complm":
                return SemanticType.complm;
            case "conj":
                return SemanticType.conj;
            case "csubj":
                return SemanticType.csubj;
            case "csubjpass":
                return SemanticType.csubjpass;
            case "dep":
                return SemanticType.dep;
            case "det":
                return SemanticType.det;
            case "dobj":
                return SemanticType.dobj;
            case "expl":
                return SemanticType.expl;
            case "hmod":
                return SemanticType.hmod;
            case "hyph":
                return SemanticType.hyph;
            case "iobj":
                return SemanticType.iobj;
            case "intj":
                return SemanticType.intj;
            case "mark":
                return SemanticType.mark;
            case "meta":
                return SemanticType.meta;
            case "neg":
                return SemanticType.neg;
            case "nfmod":
                return SemanticType.nfmod;
            case "infmod":
                return SemanticType.infmod;
            case "nmod":
                return SemanticType.nmod;
            case "nn":
                return SemanticType.nn;
            case "npadvmod":
                return SemanticType.npadvmod;
            case "nsubj":
                return SemanticType.nsubj;
            case "nsubjpass":
                return SemanticType.nsubjpass;
            case "num":
                return SemanticType.num;
            case "number":
                return SemanticType.number;
            case "oprd":
                return SemanticType.oprd;
            case "parataxis":
                return SemanticType.parataxis;
            case "partmod":
                return SemanticType.partmod;
            case "pmod":
                return SemanticType.pmod;
            case "pcomp":
                return SemanticType.pcomp;
            case "pobj":
                return SemanticType.pobj;
            case "poss":
                return SemanticType.poss;
            case "possessive":
                return SemanticType.possessive;
            case "preconj":
                return SemanticType.preconj;
            case "predet":
                return SemanticType.predet;
            case "prep":
                return SemanticType.prep;
            case "prt":
                return SemanticType.prt;
            case "punct":
                return SemanticType.punct;
            case "qmod":
                return SemanticType.qmod;
            case "quantmod":
                return SemanticType.quantmod;
            case "rcmod":
                return SemanticType.rcmod;
            case "root":
                return SemanticType.root;
            case "xcomp":
                return SemanticType.xcomp;
            case "rnr":
                return SemanticType.rnr;
            case "ref":
                return SemanticType.ref;
            case "gap":
                return SemanticType.gap;
            case "xsubj":
                return SemanticType.xsubj;
            default:
                return null;
        }
    }
}
