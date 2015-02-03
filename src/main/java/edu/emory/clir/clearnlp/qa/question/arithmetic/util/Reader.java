package edu.emory.clir.clearnlp.qa.question.arithmetic.util;

import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.qa.AppSettings;
import edu.emory.clir.clearnlp.qa.question.arithmetic.ArithmeticQuestion;
import edu.emory.clir.clearnlp.reader.TSVReader;
import edu.emory.clir.clearnlp.util.IOUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Reader {
    private String questionsDirPrefix;
    private String questionsNamePrefix;
    private String currentSuffix = null;
    TSVReader tsvReader = new TSVReader(0,1,2,3,4,5,6,7);

    public Reader(String questionsDirPrefix, String questionsNamePrefix)
    {
        this.questionsDirPrefix = questionsDirPrefix;
        this.questionsNamePrefix = questionsNamePrefix;
        currentSuffix = questionsDirPrefix;
    }

    public Reader()
    {
        this.questionsDirPrefix = AppSettings.questionFilesDirPrefix;
        this.questionsNamePrefix = AppSettings.questionFilesPrefix;
    }

    public ArithmeticQuestion readFile(String questionsDirPrefix, String questionsNamePrefix) throws IOException
    {
        return read(questionsDirPrefix, questionsNamePrefix);
    }

    public ArithmeticQuestion read() throws IOException
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

    private ArithmeticQuestion readFile(String path) throws IOException{
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

            return (new ArithmeticQuestion(questionText, depTreeList));

        } catch (FileNotFoundException e)
        {
            return null;
        } catch (IOException e)
        {
            throw new IOException();
        }
    }

    public ArithmeticQuestion read(String dirPath, String filePath) throws IOException{
        String path = dirPath + filePath;
        return readFile(path);
    }

    public ArithmeticQuestion read(String suffix) throws IOException{
        String path = questionsDirPrefix + questionsNamePrefix + suffix;
        return readFile(path);
    }
}
