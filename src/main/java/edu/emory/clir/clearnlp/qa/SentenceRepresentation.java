package edu.emory.clir.clearnlp.qa;

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
            default:
                return null;
        }
    }
}
