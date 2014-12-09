/**
 * Copyright 2014, Emory University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.emory.clir.clearnlp.qa.structure.document;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.qa.structure.Entity;
import edu.emory.clir.clearnlp.qa.structure.Instance;
import edu.emory.clir.clearnlp.qa.structure.SemanticType;

public abstract class AbstractDocument implements Serializable
{
	private static final long serialVersionUID = -7660427524131511673L;
	private Map<DEPNode,Instance> m_instances;
	private Map<Instance,Entity>  m_entities;
	
	public AbstractDocument()
	{
		m_instances = new HashMap<>();
		m_entities  = new HashMap<>();
	}
	
	public abstract void addInstances(DEPTree tree);
	
	public void coreference(DEPNode node1, DEPNode node2)
	{
		Instance inst1 = m_instances.get(node1);
		Instance inst2 = m_instances.get(node2);
		
		if (inst1 == null && inst2 == null)
			return;
		
		if (inst1 == null)
			addInstance(inst2);
		else if (inst2 == null)
			addInstance(inst1);
		else
		{
			Entity ent1 = m_entities.get(inst1);
			Entity ent2 = m_entities.get(inst2);
			
			if (ent1 == null && ent2 == null)
			{
				ent1 = new Entity(inst1, inst2);
				m_entities.put(inst1, ent1);
				m_entities.put(inst2, ent1);
			}
			else if (ent1 == null)
			{
				ent2.addInstance(inst1);
				m_entities.put(inst1, ent2);
			}
			else if (ent2 == null)
			{
				ent1.addInstance(inst2);
				m_entities.put(inst2, ent1);
			}
			else
			{
				ent1.addInstances(ent2.getInstanceSet());
				m_entities.put(inst2, ent1);
			}
		}
	}

    public Instance getInstance(DEPNode node)
    {
        return m_instances.get(node);
    }

    public DEPNode getDEPNode(Instance instance)
    {
        for (Map.Entry<DEPNode, Instance> entry : m_instances.entrySet())
        {
            if (entry.getValue().equals(instance))
            {
                return entry.getKey();
            }
        }

        return null;
    }

    public void addInstance(DEPNode node, Instance instance)
    {
        m_instances.put(node, instance);
    }

    public void printInstances()
    {
        for (Map.Entry<DEPNode, Instance> entry : m_instances.entrySet())
        {
            System.out.println("Node: " + entry.getKey().getWordForm() + " has an instance: " + entry.getValue());
        }
    }
	
	private void addInstance(Instance instance)
	{
		Entity entity = m_entities.get(instance);
		
		if (entity == null)
		{
			entity = new Entity();
			m_entities.put(instance, entity);
		}
		
		entity.addInstance(instance);		
	}

    public String toString()
    {
        StringBuilder sb = new StringBuilder("Instances:\n\n");

        for (Map.Entry<DEPNode, Instance> entry : m_instances.entrySet())
        {
            sb.append("Instance: " + entry.getKey().getWordForm() + "\n");

            if (entry.getValue().getArgumentList(SemanticType.AGENT) != null)
            {
                for (Instance instance : entry.getValue().getArgumentList(SemanticType.AGENT))
                {
                    sb.append("has an argument relation (AGENT) to -> " + getDEPNode(instance).getWordForm() + "\n");
                }
            }
            if (entry.getValue().getArgumentList(SemanticType.OTHER) != null)
            {
                for (Instance instance : entry.getValue().getArgumentList(SemanticType.OTHER))
                {
                    sb.append("has an argument relation (OTHER) to -> " + getDEPNode(instance).getWordForm() + "\n");
                }
            }
            if (entry.getValue().getPredicateList(SemanticType.AGENT) != null)
            {
                for (Instance instance : entry.getValue().getPredicateList(SemanticType.AGENT))
                {
                    sb.append("has a predicate relation (AGENT) to -> " + getDEPNode(instance).getWordForm() + "\n");
                }
            }
            if (entry.getValue().getPredicateList(SemanticType.OTHER) != null)
            {
                for (Instance instance : entry.getValue().getPredicateList(SemanticType.OTHER))
                {
                    sb.append("has a predicate relation (OTHER) to -> " + getDEPNode(instance).getWordForm() + "\n");
                }
            }

            sb.append("\n");
        }

        return sb.toString();
    }
}
