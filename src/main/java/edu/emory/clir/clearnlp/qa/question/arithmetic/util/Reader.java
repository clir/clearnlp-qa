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
    }

    public Reader()
    {
        this.questionsDirPrefix = AppSettings.questionFilesDirPrefix;
        this.questionsNamePrefix = AppSettings.questionFilesPrefix;
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

    private ArithmeticQuestion read(String suffix) throws IOException{
        BufferedReader bufferedReader;
        try {
            String file_path = questionsDirPrefix + questionsNamePrefix + suffix;
            String questionText = null;
            List<DEPTree> depTreeList = new ArrayList();


            /* Read and store question text */
            bufferedReader = new BufferedReader(new FileReader(file_path));
            questionText = bufferedReader.readLine();

            /* Read and store DEPTree of question */
            DEPTree tree;
            tsvReader.open(IOUtils.createFileInputStream(file_path + ".cnlp"));

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
}
