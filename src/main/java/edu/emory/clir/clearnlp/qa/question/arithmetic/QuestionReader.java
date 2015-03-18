package edu.emory.clir.clearnlp.qa.question.arithmetic;

import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.qa.structure.document.AbstractDocument;
import edu.emory.clir.clearnlp.qa.structure.document.EnglishDocument;
import edu.emory.clir.clearnlp.qa.structure.document.utils.iterator.DocumentDEPTreeButtomUpIterator;
import edu.emory.clir.clearnlp.reader.TSVReader;
import edu.emory.clir.clearnlp.util.IOUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Tomasz Jurczyk ({@code tomasz.jurczyk@emory.edu})
 */

public class QuestionReader {
    private String questionsDirPrefix;
    private String questionsNamePrefix;
    private String currentSuffix = null;
    private static final String questionFilesPrefix = "arith-qs.";
    private static final String questionFilesDirPrefix = "files/";
    TSVReader tsvReader = new TSVReader(0,1,2,3,4,5,6,7);

    public QuestionReader(String questionsDirPrefix, String questionsNamePrefix)
    {
        this.questionsDirPrefix = questionsDirPrefix;
        this.questionsNamePrefix = questionsNamePrefix;
        currentSuffix = questionsDirPrefix;
    }

    public QuestionReader()
    {
        this.questionsDirPrefix = questionFilesDirPrefix;
        this.questionsNamePrefix = questionFilesPrefix;
    }

    public EnglishDocument readFile(String questionsDirPrefix, String questionsNamePrefix) throws IOException
    {
        return read(questionsDirPrefix, questionsNamePrefix);
    }

    public EnglishDocument read() throws IOException
    {
        if (currentSuffix == null)
        {
            currentSuffix = "aa";
            return read(currentSuffix);
        }
        else
        {
            if (currentSuffix.equals("zz")) return null;
            if (currentSuffix.charAt(1) == 'z')
            {
                currentSuffix = "" + (char)(currentSuffix.charAt(0) + 1) + "a";
            }
            else
            {
                currentSuffix = "" + currentSuffix.charAt(0) + (char)(currentSuffix.charAt(1) + 1);
            }

            return read(currentSuffix);
        }
    }

    private EnglishDocument readFile(String path) throws IOException{
        BufferedReader bufferedReader;
        try {
            String questionText = null;
            List<DEPTree> depTreeList = new ArrayList();


            /* Read and store question text */
            bufferedReader = new BufferedReader(new FileReader(path));
            questionText = bufferedReader.readLine();

            /* Read and store DEPTree of question */
            DEPTree tree;
            tsvReader.open(IOUtils.createFileInputStream(path + ".cnlp"));

            while ((tree = tsvReader.next()) != null) {
                depTreeList.add(tree);
            }

            AbstractDocument ed = new EnglishDocument();
            ed.addInstances(depTreeList);
            return ed;

        } catch (FileNotFoundException e)
        {
            return null;
        } catch (IOException e)
        {
            throw new IOException();
        }
    }

    public EnglishDocument read(String dirPath, String filePath) throws IOException{
        String path = dirPath + filePath;
        return readFile(path);
    }

    public EnglishDocument read(String suffix) throws IOException{
        String path = questionsDirPrefix + questionsNamePrefix + suffix;
        return readFile(path);
    }
}
