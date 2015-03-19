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

import edu.emory.clir.clearnlp.collection.pair.Pair;
import edu.emory.clir.clearnlp.collection.set.DisjointSet;
import edu.emory.clir.clearnlp.coreference.AbstractCoreferenceResolution;
import edu.emory.clir.clearnlp.coreference.EnglishCoreferenceResolution;
import edu.emory.clir.clearnlp.coreference.mention.Mention;
import edu.emory.clir.clearnlp.dependency.DEPLib;
import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.pos.POSLibEn;
import edu.emory.clir.clearnlp.qa.structure.Instance;
import edu.emory.clir.clearnlp.qa.structure.SemanticType;
import edu.emory.clir.clearnlp.qa.structure.attribute.AttributeType;
import edu.emory.clir.clearnlp.qa.util.StringUtils;
import edu.emory.clir.clearnlp.util.arc.SRLArc;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jinho D. Choi ({@code jinho.choi@emory.edu})
 */


public class EnglishDocument extends AbstractDocument
{
    private List<DEPTree> depTreeList = new ArrayList();
	private static final long serialVersionUID = -1190545348244741736L;

    private AbstractCoreferenceResolution coRef = new EnglishCoreferenceResolution();
    private Pair<List<Mention>,DisjointSet> coRefEntities;

    @Override
    public void addInstances(DEPTree tree)
    {
        /* add root */
        DEPNode root = tree.getFirstRoot();
        Instance rootInstance = new Instance(root.getHead());
        addInstance(root.getHead(), rootInstance);

        for (DEPNode node: tree)
        {
            Instance newInstance = new Instance(node);
            addInstance(node, newInstance);
        }

        for (DEPNode node : tree) {
            if (POSLibEn.isPunctuation(node.getPOSTag())) {
                continue;
            }

            Instance nodeInstance       = getInstance(node);
            DEPNode headNode            = node.getHead();
            Instance headInstance       = getInstance(headNode);

            SemanticType semanticType   = null;
            AttributeType attributeType = null;

            /* Check if is Argument of the head */
            if (headNode != null && (semanticType = getArgument(headNode, node)) != null) {
                headInstance.putArgumentList(semanticType, nodeInstance);
                nodeInstance.putPredicateList(semanticType, headInstance);
            }
            /* Check if is Attribute of the head */
            else if ((attributeType = getAttribute(headNode, node)) != null) {
                headInstance.putAttribute(attributeType, nodeInstance);
                nodeInstance.putAttribute(attributeType, headInstance);

            }
            /* Otherwise, store the syntactic relation */
            else {
                semanticType = StringUtils.extractSemanticRelation(node.getLabel());
                headInstance.putArgumentList(semanticType, nodeInstance);
                nodeInstance.putArgumentList(semanticType, headInstance);
            }
        }
    }

	@Override
	public void addInstances(List<DEPTree> treeList)
    {
        /* Add each tree */
        for (DEPTree depTree: treeList)
        {
            addInstances(depTree);
        }

        /* Get Coreference resolutions */
        depTreeList.addAll(treeList);
        coRefEntities = coRef.getEntities(depTreeList);

//        for (int i = 0; i < coRefEntities.o1.size(); i++)
//        {
//            System.out.println("MentionList[" + i +"] = " + coRefEntities.o1.get(i).getNode());
//        }
//
//        System.out.println("Set = " + coRefEntities.o2.toString());
//
//        System.out.println("Coref = " + coRefEntities);

        /* Build entity set */
        processCoReference();
    }

    private void processCoReference()
    {
        int i = 0;
        for (; i < coRefEntities.o1.size(); i++)
        {
            if (coRefEntities.o2.find(i) != -1)
            {
                DEPNode n1 = coRefEntities.o1.get(i).getNode();
                DEPNode n2 = coRefEntities.o1.get(coRefEntities.o2.find(i)).getNode();
                coreference(n1, n2);
            }
        }
    }
}
