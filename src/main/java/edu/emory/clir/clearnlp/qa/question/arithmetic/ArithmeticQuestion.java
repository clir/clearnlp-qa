package edu.emory.clir.clearnlp.qa.question.arithmetic;

import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.reader.TSVReader;
import edu.emory.clir.clearnlp.util.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ArithmeticQuestion {
    Parser parser;
    HashMap<String, String> questionsSet;
    HashMap<String, List<DEPTree>> questionsSetTrees;
    TSVReader tSVReader = new TSVReader(0,1,2,3,4,5,6,7);

    public ArithmeticQuestion()
    {
        parser = new Parser();
        questionsSet = parser.parseQuestions();
        questionsSetTrees = new HashMap();
    }

    public void parseQuestions(String file_dir, String file_wildcard) throws IOException {
        if (!(new File(file_dir).exists())) {
            throw new FileNotFoundException();
        }

        for (char i = 'a'; i <= 'z'; i++) {
            for (char j = 'a'; j <= 'z'; j++) {
                BufferedReader bufferedReader = null;

                try
                {
                    /* Read and store question text */
                    bufferedReader = new BufferedReader(new FileReader(file_dir + file_wildcard + i + j));

                    String question = bufferedReader.readLine();
                    questionsSet.put("" + i + j, question);

                    /* Read and store DEPTree of question */
                    DEPTree tree = null;
                    tSVReader.open(IOUtils.createFileInputStream(file_dir + file_wildcard + i + j));
                    questionsSetTrees.put("" + i + j, new ArrayList());

                    while((tree = tSVReader.next()) != null)
                    {
                        (questionsSetTrees.get("" + i + j)).add(tree);
                    }

                } catch (FileNotFoundException e)
                {
                    break;
                } catch (IOException e)
                {
                    throw new IOException();
                }
            }
        }
    }
}
