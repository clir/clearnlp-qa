package edu.emory.clir.clearnlp.qa;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author: Tomasz Jurczyk ({@code tomasz.jurczyk@emory.edu})
 */

public class PModel {
    final double alfaFactor = 0.002;
    double [] weights;
    double [] accWeights;
    int c = 1;

    public PModel(int size)
    {
        weights = new double[size];
        accWeights = new double[size];

        for (int i = 0; i < weights.length; i++)
        {
            weights[i] = 1;
            accWeights[i] = 1;
        }
    }

    public PModel(int size, double [] _lambdas)
    {
        weights = new double[size];
        accWeights = new double[size];

        for (int i = 0; i < weights.length; i++)
        {
            weights[i] = _lambdas[i];
            accWeights[i] = _lambdas[i];
        }
    }

    public int test(double [][] x, int correctIndex)
    {
        /* Calculate maximum score */
        double [] y = new double[x.length];

        for (int i = 0; i < x.length; i++)
        {
            for (int j = 0; j < x[i].length; j++)
            {
                y[i] += weights[j]*x[i][j];
            }
        }

        //System.out.println("Calculated maxes: " + Arrays.toString(y));
        double max = y[0];
        int index_max = 0;

        for (int i = 1; i < y.length; i++)
        {
            if (y[i] > max)
            {
                max = y[i];
                index_max = i;
            }
        }

        if (correctIndex != index_max) return -1;
        else return index_max;
    }

    public void iterate(double [][] x, int correctIndex)
    {
        // Increment counter
        c++;

        /* Calculate maximum score */
        double [] y = new double[x.length];

        for (int i = 0; i < x.length; i++)
        {
            for (int j = 0; j < x[i].length; j++)
            {
                y[i] += weights[j]*x[i][j];
            }
        }

        double max = y[0];
        int index_max = 0;

        for (int i = 1; i < y.length; i++)
        {
            if (y[i] > max)
            {
                max = y[i];
                index_max = i;
            }
        }

        //System.out.println("Iteration of the model, correct index = " + correctIndex + ", max_index = " + index_max);

        /* Modify lambdas if necessary */
        if (correctIndex != index_max)
        {
            for (int i = 0; i < weights.length; i++)
            {
                if (x[index_max][i] > x[correctIndex][i] && weights[i] > alfaFactor) weights[i] -= alfaFactor;
                else if (x[index_max][i] < x[correctIndex][i]) weights[i] += alfaFactor;
            }
        }

        // Add weights to the accumulated weights
        for (int i = 0; i < weights.length; i++)
        {
            accWeights[i] += weights[i];
        }

//        for (int i = 0; i < lambdas.length; i++)
//        {
//            System.out.println("After iteration; lambda[" + i + "] = " + lambdas[i]);
//        }
    }

    public void getWeights()
    {
        /* calculate final weights */

        String lambdaString = "{";

        for (int i = 0; i < weights.length; i++)
        {
            System.out.printf("lambda[%d] = %.5f\n", i, (accWeights[i]/c));
            lambdaString += String.format("%.5f", (accWeights[i]/c));

            if (i == weights.length-1)
            {
                lambdaString += "};";
            }
            else
            {
                lambdaString += ", ";
            }
        }

        System.out.println(lambdaString);
    }
}
