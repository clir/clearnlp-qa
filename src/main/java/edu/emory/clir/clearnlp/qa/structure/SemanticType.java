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

/**
 * @author Jinho D. Choi ({@code jinho.choi@emory.edu})
 */
public enum SemanticType
{
    /* Semantic roles */
    A0,
	A1,
	A2,
    A3,
    A4,
    BNF,
    DIR,
    EXT,
    LOC,
    MNR,
    PRP,
    TMP,
    VOC,

    /* Dependency labels */
    pass,
    subj,
    acomp,
    advcl,
    advmod,
    agent,
    amod,
    appos,
    attr,
    auxpass,
    aux,
    cc,
    ccomp,
    complm,
    conj,
    csubj,
    csubjpass,
    dep,
    det,
    dobj,
    expl,
    hmod,
    hyph,
    iobj,
    intj,
    mark,
    meta,
    neg,
    nfmod,
    infmod,
    nmod,
    nn,
    npadvmod,
    nsubj,
    nsubjpass,
    num,
    number,
    oprd,
    parataxis,
    partmod,
    pmod,
    pcomp,
    pobj,
    poss,
    possessive,
    preconj,
    predet,
    prep,
    prt,
    punct,
    qmod,
    quantmod,
    rcmod,
    root,
    xcomp,
    rnr,
    ref,
    gap,
    xsubj;
}
