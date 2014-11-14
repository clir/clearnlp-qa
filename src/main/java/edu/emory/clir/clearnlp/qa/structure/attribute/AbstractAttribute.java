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
package edu.emory.clir.clearnlp.qa.structure.attribute;

import java.io.Serializable;

public abstract class AbstractAttribute implements Serializable
{
	private static final long serialVersionUID = -3865254402139162161L;
	private AttributeType a_type;

	public AbstractAttribute(AttributeType type)
	{
		setType(type);
	}
	
	public AttributeType getType()
	{
		return a_type;
	}
	
	public void setType(AttributeType type)
	{
		a_type = type;
	}
}
