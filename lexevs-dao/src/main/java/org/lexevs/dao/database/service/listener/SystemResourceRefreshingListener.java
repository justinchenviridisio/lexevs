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
package org.lexevs.dao.database.service.listener;

import org.LexGrid.LexBIG.DataModel.Core.AbsoluteCodingSchemeVersionReference;
import org.lexevs.dao.database.service.event.codingscheme.PostCodingSchemeInsertEvent;
import org.lexevs.dao.index.service.entity.EntityIndexService;
import org.lexevs.locator.LexEvsServiceLocator;
import org.lexevs.logging.LoggerFactory;
import org.lexevs.system.service.SystemResourceService;

/**
 * The listener interface for receiving systemResourceRefreshing events.
 * The class that is interested in processing a systemResourceRefreshing
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addSystemResourceRefreshingListener<code> method. When
 * the systemResourceRefreshing event occurs, that object's appropriate
 * method is invoked.
 * 
 * @see SystemResourceRefreshingEvent
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class SystemResourceRefreshingListener extends DefaultServiceEventListener {
	
	/* (non-Javadoc)
	 * @see org.lexevs.dao.database.service.listener.DefaultServiceEventListener#onCodingSchemeInsert(org.lexevs.dao.database.service.event.codingscheme.CodingSchemeInsertEvent)
	 */
	@Override
	public boolean onPostCodingSchemeInsert(PostCodingSchemeInsertEvent event) {
		SystemResourceService systemResourceService = LexEvsServiceLocator.getInstance().getSystemResourceService();
		
		systemResourceService.refresh();
		
		String uri = event.getCodingScheme().getCodingSchemeURI();
		String version = event.getCodingScheme().getRepresentsVersion();
		
		AbsoluteCodingSchemeVersionReference ref = new AbsoluteCodingSchemeVersionReference();
		ref.setCodingSchemeURN(uri);
		ref.setCodingSchemeVersion(version);
		
		EntityIndexService indexService = 
			LexEvsServiceLocator.getInstance().
				getIndexServiceManager().getEntityIndexService();
		
		if(indexService.doesIndexExist(ref)){

			LoggerFactory.getLogger().warn("Detected an existing Lucene index for URI: " + uri + " Version: " + version +
					" -- the old index will be overwritten.");
			
			indexService.dropIndex(ref);	
		}

		return true;
	}
}