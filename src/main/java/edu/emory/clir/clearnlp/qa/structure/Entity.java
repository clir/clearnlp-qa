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
import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;

public class Entity implements Serializable
{
    private static final long serialVersionUID = 1990738058561340684L;
    private Set<Instance> s_instances;
    
	public Entity(Instance... instances)
	{
		s_instances = Sets.newHashSet(instances);
	}
	
	public void addInstance(Instance instance)
	{
		s_instances.add(instance);
	}
	
	public void addInstances(Collection<Instance> instances)
	{
		s_instances.addAll(instances);
	}
	
	public Set<Instance> getInstanceSet()
	{
		return s_instances;
	}
	
	public boolean removeInstance(Instance instance)
	{
		return s_instances.remove(instance);
	}
}
