/*
 * Copyright: (c) 2004-2010 Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 * 
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 * 		http://www.eclipse.org/legal/epl-v10.html
 * 
 */
package org.lexgrid.loader.meta.processor.support;

import org.LexGrid.commonTypes.Text;
import org.lexevs.dao.database.utility.DaoUtility;
import org.lexgrid.loader.meta.constants.MetaLoaderConstants;
import org.lexgrid.loader.processor.support.OptionalQualifierResolver;
import org.lexgrid.loader.rrf.model.Mrrel;

/**
 * The Class SelfReferencingAssocQualResolver.
 * 
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class SelfReferencingAssocQualResolver implements OptionalQualifierResolver<Mrrel>{

	/* (non-Javadoc)
	 * @see org.lexgrid.loader.processor.support.QualifierResolver#getQualifierName()
	 */
	public String getQualifierName() {
		return MetaLoaderConstants.SELF_REFERENCING_QUALIFIER;
	}

	/* (non-Javadoc)
	 * @see org.lexgrid.loader.processor.support.QualifierResolver#getQualifierValue(java.lang.Object)
	 */
	public Text getQualifierValue(Mrrel item) {
		return DaoUtility.createText(MetaLoaderConstants.SELF_REFERENCING_QUALIFIER_TRUE_VALUE);
	}

	/* (non-Javadoc)
	 * @see org.lexgrid.loader.processor.support.OptionalQualifierResolver#toProcess(java.lang.Object)
	 */
	public boolean toProcess(Mrrel item) {
		return item.getCui1().equals(item.getCui2());
	}	
}