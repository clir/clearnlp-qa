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
package edu.emory.clir.clearnlp.qa.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.qa.structure.attribute.AbstractAttribute;
import edu.emory.clir.clearnlp.qa.structure.attribute.AttributeType;

public class Instance implements Serializable
{
	private static final long serialVersionUID = 17479620890779053L;
	private Map<AttributeType,List<Instance>>    m_attributes;
	private Map<SemanticType,List<Instance>>     m_predicates;
	private Map<SemanticType,List<Instance>>     m_arguments;
    private DEPNode                              depNode;
	
	public Instance()
	{
		m_attributes = new HashMap<>();
		m_predicates = new HashMap<>();
		m_arguments  = new HashMap<>();
	}

    public Instance(DEPNode _depNode)
    {
        m_attributes = new HashMap<>();
        m_predicates = new HashMap<>();
        m_arguments  = new HashMap<>();
        depNode      = _depNode;
    }

    public DEPNode getDepNode() {
        return depNode;
    }

    public void setDepNode(DEPNode depNode) {
        this.depNode = depNode;
    }

	public Set<AttributeType> getAttributeTypeSet()
	{
		return m_attributes.keySet();
	}

    public List<Instance> getAttributeList()
    {
        List<Instance> instanceList = new ArrayList();
        for (List<Instance> l: m_arguments.values())
        {
            instanceList.addAll(l);
        }

        return instanceList;
    }
    
	public List<Instance> getAttributeList(AttributeType type)
	{
		return m_attributes.get(type);
	}
	
	public List<Instance> putAttribute(AttributeType type, Instance instance)
	{
        List<Instance> list_instances = m_attributes.get(type);

        if (list_instances == null)
        {
            list_instances = new ArrayList<>();
            m_attributes.put(type, list_instances);
        }

        list_instances.add(instance);
        return list_instances;
	}
	
	public List<Instance> removeAttribute(AttributeType type)
	{
		return m_attributes.remove(type);
	}
	
	public Set<SemanticType> getPredicateTypeSet()
	{
		return m_predicates.keySet();
	}
	
	public List<Instance> getPredicateList(SemanticType type)
	{
		return m_predicates.get(type);
	}
	
	public List<Instance> putPredicateList(SemanticType type, Instance predicate)
	{
		return putInstance(m_predicates, type, predicate);
	}
	
	public boolean removePredicate(Instance predicate)
	{
		return removeInstance(m_predicates, predicate);
	}
	
	public Set<SemanticType> getArgumentTypeSet()
	{
		return m_arguments.keySet();
	}

    public List<Instance> getArgumentList()
    {
        List<Instance> instanceList = new ArrayList();
        for (List<Instance> l: m_arguments.values())
        {
            instanceList.addAll(l);
        }

        return instanceList;
    }
	
	public List<Instance> getArgumentList(SemanticType type)
	{
		return m_arguments.get(type);
	}
	
	public List<Instance> putArgumentList(SemanticType type, Instance argument)
	{
		return putInstance(m_arguments, type, argument);
	}
	
	public boolean removeArgument(Instance argument)
	{
		return removeInstance(m_arguments, argument);
	}
	
	private List<Instance> putInstance(Map<SemanticType,List<Instance>> map, SemanticType type, Instance instance)
	{
		List<Instance> list_instances = map.get(type);
		
		if (list_instances == null)
		{
            list_instances = new ArrayList<>();
            map.put(type, list_instances);
		}
		
		list_instances.add(instance);
		return list_instances;
	}
	
	public boolean removeInstance(Map<SemanticType,List<Instance>> map, Instance instance)
	{
		List<Instance> list;
		
		for (SemanticType type : map.keySet())
		{
			list = map.get(type);
			
			for (Instance i : list)
			{
				if (i == instance)
				{
					list.remove(instance);
					if (list.isEmpty()) map.remove(type);
					return true;
				}
			}
		}
		
		return false;
	}
}
