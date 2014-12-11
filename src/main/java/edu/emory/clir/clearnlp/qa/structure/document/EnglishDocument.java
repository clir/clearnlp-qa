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
import edu.emory.clir.clearnlp.qa.structure.Instance;
import edu.emory.clir.clearnlp.qa.structure.SemanticType;
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
            Instance instance;

            if ((instance = getInstance(node)) == null)
            {
                instance = new Instance();
                addInstance(node, instance);
            }

            Instance headInstance;

            if (! node.getSemanticHeadArcList().isEmpty())
            {
                for (SRLArc arc : node.getSemanticHeadArcList()) {

                    SemanticType type = SemanticType.valueOf(parseSemanticRelation(arc.getLabel()));
                    headInstance = getInstance(arc.getNode());
                    if (headInstance == null) {
                        headInstance = new Instance();
                        addInstance(arc.getNode(), headInstance);
                    }

                    headInstance.putArgumentList(type, instance);
                    instance.putPredicateList(type, headInstance);
                }
            }
            else if (! node.isLabel("root"))
            {
                SemanticType type = SemanticType.valueOf(parseSemanticRelation(node.getLabel()));
                if ((headInstance = getInstance(node.getHead())) == null)
                {
                    headInstance = new Instance();
                    addInstance(node.getHead(), headInstance);
                }

                headInstance.putArgumentList(type, instance);
                instance.putArgumentList(type, headInstance);
            }
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
