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

import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;


/**
 * @author 	Yu-Hsin(Henry) Chen ({@code yu-hsin.chen@emory.edu})
 * @version	1.0
 * @since	Dec 2, 2014
 */
public class DocuemntDEPTreeTopDownIterator extends AbstractDocumentIterator<DEPTree, DEPNode> {

	private static final long serialVersionUID = -8936278691360520358L;

	public DocuemntDEPTreeTopDownIterator(DEPTree tree) {
		super();
		init(tree);
	}
	
	@Override
	public void init(DEPTree structure) {
		if(structure != null)
			for(DEPNode head : structure.getRoots()) addNodes(head);
	}

	private void addNodes(DEPNode head){
		l_nodes.addLast(head);
		for (DEPNode node : head.getDependentList()){
			addNodes(node);
		}
	}
}
