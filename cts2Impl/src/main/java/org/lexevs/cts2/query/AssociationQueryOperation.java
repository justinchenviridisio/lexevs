/*
 * Copyright: (c) 2004-2009 Mayo Foundation for Medical Education and Research
 * (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the triple-shield Mayo
 * logo are trademarks and service marks of MFMER.
 * 
 * Except as contained in the copyright notice above, or as used to identify
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 * 
 * Licensed under the Eclipse Public License, Version 1.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.lexevs.cts2.query;

import org.LexGrid.LexBIG.DataModel.Collections.ResolvedConceptReferenceList;
import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag;
import org.LexGrid.LexBIG.DataModel.Core.ConceptReference;
import org.LexGrid.LexBIG.DataModel.Core.ResolvedConceptReference;

public interface AssociationQueryOperation {
	public ResolvedConceptReferenceList listAssociations(
			String codingSystemName, CodingSchemeVersionOrTag versionOrTag,
			String namespace, String code, String associationName,
			boolean isBackward, int depth, int maxToReturn);

	public ResolvedConceptReference determineTransitiveConceptRelationship(
			String codingSystemUri, CodingSchemeVersionOrTag versionOrTag,
			String relationContainerName, String associationName,
			String sourceCode, String sourceNS, String targetCode, String targetNS);

	public boolean computeSubsumptionRelationship(String codingSystemName,
			CodingSchemeVersionOrTag versionOrTag, String associationtype,
			ConceptReference parentCode, ConceptReference childCode);

	public void getAssociationDetails();
}
