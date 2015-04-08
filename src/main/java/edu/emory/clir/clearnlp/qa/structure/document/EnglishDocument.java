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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        for (DEPNode node : tree) {
            if (POSLibEn.isPunctuation(node.getPOSTag())) {
                continue;
            }

            Instance nodeInstance = null;
            DEPNode headNode = node.getHead();
            Instance headInstance = null;

            Map<SemanticType, DEPNode> semanticTypeMap = null;
            AttributeType attributeType = null;

            /* Create if necessary instances of node and headNode */
            if ((nodeInstance = getInstance(node)) == null) {
                nodeInstance = new Instance(node);
                addInstance(node, nodeInstance);
            }

            if ((headInstance = getInstance(headNode)) == null) {
                headInstance = new Instance(headNode);
                addInstance(headNode, headInstance);
            }

            /* Check if is Argument of the head */
            if ((semanticTypeMap = getArguments(node)) != null) {
                Instance SemanticHeadInstance = null;
                for (Map.Entry<SemanticType,DEPNode> entry: semanticTypeMap.entrySet())
                {
                    if ((SemanticHeadInstance = getInstance(entry.getValue())) == null) {
                        SemanticHeadInstance = new Instance(entry.getValue());
                        addInstance(entry.getValue(), SemanticHeadInstance);
                    }

                    nodeInstance.putPredicateList(entry.getKey(), SemanticHeadInstance);
                    SemanticHeadInstance.putArgumentList(entry.getKey(), nodeInstance);
                }
            }
            /* Check if is Attribute of the head */
            else if ((attributeType = getAttribute(node)) != null) {
                /* Connect within two attribute relations */
                headInstance.putAttribute(attributeType, nodeInstance);
                nodeInstance.putAttribute(attributeType, headInstance);

                /* Also, connect within regular P-A relation */
                SemanticType semanticType = StringUtils.getSemanticType(node.getLabel());
                headInstance.putArgumentList(semanticType, nodeInstance);
                nodeInstance.putPredicateList(semanticType, headInstance);

            }
            /* Otherwise, store the syntactic relation */
            else {
                SemanticType semanticType = StringUtils.getSemanticType(node.getLabel());
                headInstance.putArgumentList(semanticType, nodeInstance);
                nodeInstance.putPredicateList(semanticType, headInstance);
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
