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

import edu.emory.clir.clearnlp.dependency.DEPLib;
import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.pos.POSLibEn;
import edu.emory.clir.clearnlp.qa.structure.Instance;
import edu.emory.clir.clearnlp.qa.structure.SemanticType;
import edu.emory.clir.clearnlp.qa.structure.attribute.AttributeType;
import edu.emory.clir.clearnlp.qa.util.StringUtils;
import edu.emory.clir.clearnlp.util.arc.SRLArc;

/**
 * @author Jinho D. Choi ({@code jinho.choi@emory.edu})
 */


public class EnglishDocument extends AbstractDocument
{
	private static final long serialVersionUID = -1190545348244741736L;
	
	@Override
	public void addInstances(DEPTree tree)
	{
		for (DEPNode node : tree)
		{
            if (POSLibEn.isPunctuation(node.getPOSTag()))
            {
                continue;
            }

            Instance nodeInstance       = null;
            DEPNode headNode            = node.getHead();
            Instance headInstance       = null;

            SemanticType semanticType   = null;
            AttributeType attributeType = null;

            /* Create if necessary instances of node and headNode */
            if ((nodeInstance = getInstance(node)) == null)
            {
                nodeInstance = new Instance(node);
                addInstance(node, nodeInstance);
            }

            if ((headInstance = getInstance(headNode)) == null)
            {
                headInstance = new Instance(headNode);
                addInstance(headNode, headInstance);
            }

            /* Check if is Argument of the head */
            if (headNode != null && (semanticType = getArgument(headNode, node)) != null)
            {
                headInstance.putArgumentList(semanticType, nodeInstance);
                nodeInstance.putPredicateList(semanticType, headInstance);
            }
            /* Check if is Attribute of the head */
            else if ((attributeType = getAttribute(headNode, node)) != null)
            {
                headInstance.putAttribute(attributeType, nodeInstance);
                nodeInstance.putAttribute(attributeType, headInstance);

            }
            /* Otherwise, store the syntactic relation */
            else
            {
                semanticType = StringUtils.extractSemanticRelation(node.getLabel());
                headInstance.putArgumentList(semanticType, nodeInstance);
                nodeInstance.putArgumentList(semanticType, headInstance);
            }

            /* Perform entity check for instance */
		}
	}

    private String parseSemanticRelation(String label)
    {
        if (label.contains("-"))
        {
            return label.split("-")[1];
        }
        else if (label.contains("="))
        {
            return label.split("=")[0];
        }
        else
        {
            return label;
        }
    }
}
