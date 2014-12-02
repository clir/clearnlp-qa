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

import java.util.ArrayList;
import java.util.List;

import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.qa.structure.Instance;

/**
 * @author Jinho D. Choi ({@code jinho.choi@emory.edu})
 */
public class EnglishDocument extends AbstractDocument
{
	private static final long serialVersionUID = -1190545348244741736L;
	
	@Override
	public void addInstances(DEPTree tree){
		
		for (DEPNode head : tree.getRoots())
			search(head);

		for(DEPNode node : tree){
			Instance instance = new Instance();
			
			
			
		
		
		
		}
	}
	
	public void search(DEPNode head)
	{
		for (DEPNode node : head.getDependentList())
		{
			search(node);
		}
	}
	
}
