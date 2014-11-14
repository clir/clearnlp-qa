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
}
