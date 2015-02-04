package edu.emory.clir.clearnlp.qa.question.arithmetic;

import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.pos.POSLibEn;
import edu.emory.clir.clearnlp.qa.question.arithmetic.util.StringUtils;
import edu.emory.clir.clearnlp.qa.structure.Instance;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class State {
    private HashMap<Instance, DEPNode> m_instances;

    public State()
    {
        m_instances = new HashMap();
    }

    public DEPNode putInstance(DEPNode depNode, Instance instance)
    {
        return m_instances.put(instance, depNode);
    }

    public Set<Instance> keySet()
    {
        return m_instances.keySet();
    }

    public DEPNode get(Instance i)
    {
        return m_instances.get(i);
    }

    public Instance getPredicateInstance()
    {
        for (Map.Entry<Instance,DEPNode> entry : m_instances.entrySet())
        {
            if (entry.getValue() != null && POSLibEn.isVerb(entry.getValue().getPOSTag()))
            {
                return entry.getKey();
            }
        }

        return null;
    }

    public String toString()
    {
        String s = "State: \n";
        for (Map.Entry<Instance, DEPNode> entry : m_instances.entrySet())
        {
            s += entry.getKey() + ": " + entry.getValue().getWordForm() + "\n";
        }

        return s;
    }
}
