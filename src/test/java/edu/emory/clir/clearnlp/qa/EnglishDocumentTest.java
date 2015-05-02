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
package edu.emory.clir.clearnlp.qa;

import java.io.FileInputStream;
import java.io.IOException;

import com.clearnlp.dependency.DEPTree;
import org.junit.Test;

import edu.emory.clir.clearnlp.qa.structure.document.EnglishDocument;
import edu.emory.clir.clearnlp.reader.TSVReader;


/**
 * @author 	Yu-Hsin(Henry) Chen ({@code yu-hsin.chen@emory.edu})
 * @version	1.0
 * @since	Nov 30, 2014
 */
public class EnglishDocumentTest {
	public static String FILEPATH = "/Users/HenryChen/Desktop/testExamples/1QSample.txt.cnlp";
	
	@Test
	public void Building() throws IOException{
		TSVReader DEPTreeReader = new TSVReader(0, 1, 2, 3, 4, 5, 6, 7);
		DEPTreeReader.open(new FileInputStream(FILEPATH));
		EnglishDocument testEngine = new EnglishDocument();
		
//		DEPTree tree = DEPTreeReader.next();
//		while(tree != null){
//			testEngine.addInstances(tree);
//			tree = DEPTreeReader.next();
//		}	
	}
}
