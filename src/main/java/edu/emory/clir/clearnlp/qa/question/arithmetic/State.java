package edu.emory.clir.clearnlp.qa.question.arithmetic;

import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.qa.structure.Instance;

import java.util.HashMap;
import java.util.Map;

public class State {
    private HashMap<DEPNode, Instance> m_instances;

    public State()
    {
        m_instances = new HashMap();
    }

    public Instance putInstance(DEPNode depNode, Instance instance)
    {
        return m_instances.put(depNode, instance);
    }

    public String toString()
    {
        String s = "\n";
        for (Map.Entry<DEPNode, Instance> entry : m_instances.entrySet())
        {
            s += entry.getKey().getLemma() + ": " + entry.getValue() + "\n";
        }

        return s;
    }
}
