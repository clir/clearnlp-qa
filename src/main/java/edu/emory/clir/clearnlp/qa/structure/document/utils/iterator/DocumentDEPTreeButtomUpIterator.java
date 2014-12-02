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
package edu.emory.clir.clearnlp.qa.structure.document.utils.iterator;

import java.io.Serializable;

import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;


/**
 * @author 	Yu-Hsin(Henry) Chen ({@code yu-hsin.chen@emory.edu})
 * @version	1.0
 * @since	Dec 2, 2014
 */
public class DocumentDEPTreeButtomUpIterator extends AbstractDocumentIterator<DEPTree, DEPNode> implements Serializable{

	private static final long serialVersionUID = -7950252758969735863L;
	
	public DocumentDEPTreeButtomUpIterator(DEPTree tree) {
		super();
		init(tree);
	}

	@Override
	public void init(DEPTree structure) {
		if(structure != null)
			for(DEPNode head : structure.getRoots()) addNodes(head);
	}
	
	private void addNodes(DEPNode head){
		for (DEPNode node : head.getDependentList()){
			addNodes(node);
		}
		l_nodes.addLast(head);
	}
}
