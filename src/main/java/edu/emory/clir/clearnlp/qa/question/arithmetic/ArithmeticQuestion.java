package edu.emory.clir.clearnlp.qa.question.arithmetic;

import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.qa.question.arithmetic.parse.Parser;

import java.util.ArrayList;
import java.util.List;

public class ArithmeticQuestion {
    private String questionText;
    private List<DEPTree> questionTreeList;
    private List<State> questionTextStates;
    private State questionState;

    public ArithmeticQuestion()
    {
    }

    public ArithmeticQuestion(String questionText, List<DEPTree> depTreeList)
    {
        this.questionText = questionText;
        this.questionTreeList = depTreeList;
        questionTextStates = new ArrayList();
        prepareInstances();
    }

    private void prepareInstances()
    {
        Parser parser = new Parser();
        boolean isQuestion = false;
        for (DEPTree depTree : questionTreeList)
        {
            for (DEPNode node : depTree)
            {
                if (node.getLemma().equals("?"))
                {
                    isQuestion = true;
                    break;
                }
            }

            if (isQuestion)
            {
                isQuestion = false;
                questionState = parser.parseQuestion(depTree);
            }
            else
            {
                questionTextStates.addAll(parser.parseTree(depTree));
            }
        }
    }

    public String toString()
    {
        return "Text: " + questionText + "\nStates: " + questionTextStates;
    }



//    public void parseText(String file_dir, String file_wildcard) throws IOException {
//        if (! (new File(file_dir).exists())) {
//            throw new FileNotFoundException();
//        }
//
//        for (char i = 'a'; i <= 'b'; i++) {
//            for (char j = 'a'; j <= 'z'; j++) {
//                BufferedReader bufferedReader;
//
//                try
//                {
//                    /* Read and store question text */
//                    bufferedReader = new BufferedReader(new FileReader(file_dir + file_wildcard + i + j));
//
//                    String question = bufferedReader.readLine();
//
//                    questionsSet.put("" + i + j, question);
//
//                    /* Read and store DEPTree of question */
//                    DEPTree tree;
//                    tSVReader.open(IOUtils.createFileInputStream(file_dir + file_wildcard + i + j + ".cnlp"));
//                    questionsSetTrees.put("" + i + j, new ArrayList());
//
//                    while((tree = tSVReader.next()) != null)
//                    {
//                        (questionsSetTrees.get("" + i + j)).add(tree);
//                    }
//
//                } catch (FileNotFoundException e)
//                {
//                    break;
//                } catch (IOException e)
//                {
//                    throw new IOException();
//                }
//            }
//        }
//    }

//    public void splitIntoCategories()
//    {
//        sumQuestionList = new ArrayList();
//
//        for (String key : questionsSet.keySet())
//        {
//            if (isSumQuestion(key))
//            {
//                sumQuestionList.add(new SumQuestion(questionsSetTrees.get(key)));
//            }
//        }
//    }

//    private boolean isSumQuestion(String key)
//    {
//        DEPTree qTree = null;
//        boolean found = false;
//
//        /* Select the tree with the question mark */
//        for (DEPTree depTree : questionsSetTrees.get(key))
//        {
//            for (DEPNode node : depTree)
//            {
//                if (node.getLemma().equals("?"))
//                {
//                    qTree = depTree;
//                    found = true;
//                    break;
//                }
//            }
//
//            if (found)
//            {
//                break;
//            }
//        }
//
//        /* Iterate through faster */
//        for (DEPNode node : qTree) {
//            /* If question is sum-question, mark it */
//            if (node.getLemma().equalsIgnoreCase("total") || node.getLemma().equalsIgnoreCase("all") ||
//                    node.getLemma().equalsIgnoreCase("together")){
//                return true;
//            }
//        }
//
//        return false;
//    }
}
