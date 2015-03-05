package edu.emory.clir.clearnlp.qa.question.arithmetic;

import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.pos.POSLibEn;
import edu.emory.clir.clearnlp.qa.question.arithmetic.util.StringUtils;
import edu.emory.clir.clearnlp.qa.structure.Instance;
import edu.emory.clir.clearnlp.qa.structure.SemanticType;
import edu.emory.clir.clearnlp.qa.structure.attribute.AttributeType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class State {
    private HashMap<Instance, DEPNode> m_instances;

    public State()
    {
        m_instances = new HashMap();
    }

    public State(State copyFromState)
    {
        m_instances = new HashMap<>(copyFromState.m_instances);
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

    public DEPNode set(Instance instance, DEPNode depNode)
    {
        return m_instances.put(instance, depNode);
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

    public Instance getNumericalInstance()
    {
        for (Map.Entry<Instance,DEPNode> entry : m_instances.entrySet())
        {
            if (entry.getValue() != null && (StringUtils.isInteger(entry.getValue().getWordForm()) ||
            StringUtils.isDouble(entry.getValue().getWordForm())))
            {
                return entry.getKey();
            }
        }

        return null;
    }

    public Instance getThemeInstance()
    {
        return getRelatedInstance(SemanticType.A1);
    }

    private Instance getRelatedInstance(SemanticType semanticType)
    {
        Instance pred_inst  = getPredicateInstance();
        DEPNode pred_node   = get(getPredicateInstance());

        if (pred_inst.getArgumentList(semanticType) != null && pred_inst.getArgumentList(semanticType).size() > 0)
        {
            return pred_inst.getArgumentList(semanticType).get(0);
        }

        return null;
    }

    public String toString()
    {
        String s = "State: \n";
        Instance pred_inst = null;
        DEPNode pred_node = null;

        /* Find predicate */
        for (Map.Entry<Instance, DEPNode> entry : m_instances.entrySet())
        {
            if (entry.getValue() != null && POSLibEn.isVerb(entry.getValue().getPOSTag()))
            {
                pred_inst = entry.getKey();
                pred_node = entry.getValue();
            }
        }

        if (pred_inst == null) return "Predicate error";

        s += pred_inst + ": " + pred_node.getWordForm() + " (predicate)\n";

        if (pred_inst.getArgumentList(SemanticType.A0) != null)
        {
            for (Instance i : pred_inst.getArgumentList(SemanticType.A0)) {
                s += i + ": " + m_instances.get(i).getWordForm() + " (A0)\n";
            }
        }
        else
        {
            s += "No A0 instance\n";
        }

        Instance theme_inst = null;
        if (pred_inst.getArgumentList(SemanticType.A1) != null)
        {
            theme_inst = pred_inst.getArgumentList(SemanticType.A1).get(0);
            if (m_instances.get(theme_inst) == null) {
                s += theme_inst + ": " + "null\n";
            } else {
                s += theme_inst + ": " + m_instances.get(theme_inst).getWordForm() + " (A1)\n";
            }
        }
        else
        {
            s += "No A1 instance\n";
        }

        Instance actor_inst = null;
        if (pred_inst.getArgumentList(SemanticType.A2) != null)
        {
            actor_inst = pred_inst.getArgumentList(SemanticType.A2).get(0);
            if (m_instances.get(actor_inst) == null) {
                s += actor_inst + ": " + "null\n";
            } else {
                s += actor_inst + ": " + m_instances.get(actor_inst).getWordForm() + " (A2)\n";
            }
        }
        else
        {
            s += "No A2 instance\n";
        }

        if (theme_inst != null && theme_inst.getAttribute(AttributeType.QUANTITY) != null)
        {
            Instance num_inst = theme_inst.getAttribute(AttributeType.QUANTITY).get(0);
            if (m_instances.get(num_inst) == null) {
                s += num_inst + ": " + "null\n";
            } else {
                s += num_inst + ": " + m_instances.get(num_inst).getWordForm() + " (QUANTITY)\n";
            }
        }
        else
        {
            s += "No num instance\n";
        }

        if (theme_inst != null && theme_inst.getAttribute(AttributeType.QUALITY) != null)
        {
            for (Instance i : theme_inst.getAttribute(AttributeType.QUALITY)) {
                s += i + ": " + m_instances.get(i).getWordForm() + " (QUALITY)\n";
            }
        }

//        for (Map.Entry<Instance, DEPNode> entry : m_instances.entrySet())
//        {
//            if (entry.getValue() != null)
//            {
//                s += entry.getKey() + ": " + entry.getValue().getWordForm() + "\n";
//            }
//            else
//            {
//                s += entry.getKey() + ": " + null + "\n";
//            }
//        }

        return s;
    }
}
