package edu.emory.clir.clearnlp.qa;

import edu.emory.clir.clearnlp.qa.structure.SemanticType;

/**
 * @author: Tomasz Jurczyk ({@code tomasz.jurczyk@emory.edu})
 */
public class SentenceRepresentation {
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    private String text = "";
    private String sentenceID = "";
    private String verb = "";
    private String argumentWords = "";
    private String singleSemanticRelations = "";
    private String doubleSemanticRelations = "";
    private String dependencyLabels = "";
    private String dependencyLabelWordPairs = "";
    private String verbSynonyms = "";

    public String getA0Label() {
        return A0Label;
    }

    public void addA0Label(String a0Label) {
        A0Label = a0Label;
    }

    public String getA1Label() {
        return A1Label;
    }

    public void addA1Label(String a1Label) {
        A1Label = a1Label;
    }

    public String getA4Label() {
        return A4Label;
    }

    public void addA4Label(String a4Label) {
        A4Label = a4Label;
    }

    public String getDIRLabel() {
        return DIRLabel;
    }

    public void addDIRLabel(String DIRLabel) {
        this.DIRLabel = DIRLabel;
    }

    public String getLOCLabel() {
        return LOCLabel;
    }

    public void addLOCLabel(String LOCLabel) {
        this.LOCLabel = LOCLabel;
    }

    public String getA2Label() {
        return A2Label;
    }

    public void addA2Label(String a2Label) {
        A2Label = a2Label;
    }

    public void addSingleSemanticNode(SemanticType semanticType, String node)
    {
        switch(semanticType)
        {
            case A0:
                addA0Label(node);
                break;
            case A1:
                addA1Label(node);
                break;
            case A2:
                addA2Label(node);
                break;
            case A4:
                addA4Label(node);
                break;
            case LOC:
                addLOCLabel(node);
                break;
            case DIR:
                addDIRLabel(node);
                break;
            default:
                break;
        }
    }

    // Single semantic relations
    private String A0Label = "";
    private String A1Label = "";
    private String A2Label = "";
    private String A4Label = "";
    private String DIRLabel = "";
    private String LOCLabel = "";


    public String getVerbSynonyms() {
        return verbSynonyms;
    }

    public void setVerbSynonyms(String verbSynonyms) {
        this.verbSynonyms = verbSynonyms;
    }

    public String getDoubleSemanticRelations() {
        return doubleSemanticRelations;
    }

    public void addDoubleSemanticRelations(String doubleSemanticRelations) {
        this.doubleSemanticRelations += doubleSemanticRelations + " ";
    }

    public String getSentenceID() {
        return sentenceID;
    }

    public void setSentenceID(String sentenceID) {
        this.sentenceID = sentenceID;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public String getArgumentWords() {
        return argumentWords;
    }

    public void addArgumentWords(String argumentWords) {
        this.argumentWords += argumentWords + " ";
    }

    public String getSingleSemanticRelations() {
        return singleSemanticRelations;
    }

    public void addSingleSemanticRelations(String singleSemanticRelations) {
        this.singleSemanticRelations += singleSemanticRelations + " ";
    }

    public String getDependencyLabelWordPairs() {
        return dependencyLabelWordPairs;
    }

    public void addDependencyLabelWordPairs(String dependencyLabelWordPairs)
    {
        this.dependencyLabelWordPairs += dependencyLabelWordPairs + " ";
    }

    public String getDependencyLabels()
    {
        return dependencyLabels;
    }

    public void addDependencyLabels(String dependencyLabels)
    {
        this.dependencyLabels += dependencyLabels + " ";
    }

    public String getField(String fieldName)
    {
        switch(fieldName)
        {
            case "text":
                return getText();
            case "verb":
                return getVerb();
            case "argument_words":
                return getArgumentWords();
            case "single_semantic_relations":
                return getSingleSemanticRelations();
            case "double_semantic_relations":
                return getDoubleSemanticRelations();
            case "dependency_labels":
                return getDependencyLabels();
            case "dependency_label_word_pairs":
                return getDependencyLabelWordPairs();
            case "verb_synonyms":
                return getVerbSynonyms();
            case "sem_a0":
                return getA0Label();
            case "sem_a1":
                return getA1Label();
            case "sem_a2":
                return getA2Label();
            case "sem_a4":
                return getA4Label();
            case "sem_loc":
                return getLOCLabel();
            case "sem_dir":
                return getDIRLabel();
            default:
                return null;
        }
    }
}
