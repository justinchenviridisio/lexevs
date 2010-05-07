/*
 * Copyright: (c) 2004-2009 Mayo Foundation for Medical Education and 
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
package org.lexgrid.valuesets.impl;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.LexGrid.LexBIG.DataModel.Collections.AbsoluteCodingSchemeVersionReferenceList;
import org.LexGrid.LexBIG.DataModel.Collections.CodingSchemeRenderingList;
import org.LexGrid.LexBIG.DataModel.Collections.ConceptReferenceList;
import org.LexGrid.LexBIG.DataModel.Collections.ResolvedConceptReferenceList;
import org.LexGrid.LexBIG.DataModel.Core.AbsoluteCodingSchemeVersionReference;
import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeSummary;
import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag;
import org.LexGrid.LexBIG.DataModel.Core.LogEntry;
import org.LexGrid.LexBIG.DataModel.Core.ResolvedConceptReference;
import org.LexGrid.LexBIG.DataModel.InterfaceElements.CodingSchemeRendering;
import org.LexGrid.LexBIG.DataModel.InterfaceElements.LoadStatus;
import org.LexGrid.LexBIG.DataModel.InterfaceElements.types.ProcessState;
import org.LexGrid.LexBIG.Exceptions.LBException;
import org.LexGrid.LexBIG.Exceptions.LBInvocationException;
import org.LexGrid.LexBIG.Exceptions.LBParameterException;
import org.LexGrid.LexBIG.Impl.LexBIGServiceImpl;
import org.LexGrid.LexBIG.Impl.loaders.LexGridMultiLoaderImpl;
import org.LexGrid.LexBIG.Impl.loaders.MessageDirector;
import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet;
import org.LexGrid.LexBIG.LexBIGService.LexBIGService;
import org.LexGrid.LexBIG.LexBIGService.LexBIGServiceManager;
import org.LexGrid.LexBIG.Utility.Constructors;
import org.LexGrid.LexBIG.Utility.Iterators.ResolvedConceptReferencesIterator;
import org.LexGrid.LexBIG.Utility.LBConstants.MatchAlgorithms;
import org.LexGrid.LexBIG.Utility.logging.LgLoggerIF;
import org.LexGrid.annotations.LgClientSideSafe;
import org.LexGrid.commonTypes.Text;
import org.LexGrid.concepts.Entity;
import org.LexGrid.concepts.Presentation;
import org.LexGrid.naming.Mappings;
import org.LexGrid.valueSets.PickListDefinition;
import org.LexGrid.valueSets.PickListEntry;
import org.LexGrid.valueSets.PickListEntryExclusion;
import org.LexGrid.valueSets.PickListEntryNode;
import org.apache.commons.lang.StringUtils;
import org.lexevs.dao.database.service.DatabaseServiceManager;
import org.lexevs.dao.database.service.valuesets.PickListDefinitionService;
import org.lexevs.locator.LexEvsServiceLocator;
import org.lexevs.logging.LoggerFactory;
import org.lexevs.system.service.SystemResourceService;
import org.lexgrid.valuesets.LexEVSPickListDefinitionServices;
import org.lexgrid.valuesets.LexEVSValueSetDefinitionServices;
import org.lexgrid.valuesets.dto.ResolvedPickListEntry;
import org.lexgrid.valuesets.dto.ResolvedPickListEntryList;
import org.lexgrid.valuesets.dto.ResolvedValueSetCodedNodeSet;
import org.lexgrid.valuesets.dto.ResolvedValueSetDefinition;
import org.lexgrid.valuesets.helper.PLEntryNodeSortUtil;
import org.lexgrid.valuesets.helper.VSDServiceHelper;

/**
 * Implements LexEVSPickListSerives.
 * 
 * @author <A HREF="mailto:dwarkanath.sridhar@mayo.edu">Sridhar Dwarkanath</A>
 */
public class LexEVSPickListDefinitionServicesImpl implements LexEVSPickListDefinitionServices {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Associated service ...
	private LexBIGService lbs_;
	private PickListDefinitionService pls_;
	private VSDServiceHelper sh_;
	
	protected MessageDirector md_;
	protected LoadStatus status_;
	private static final String name_ = "LexEVSPickListDefinitionServicesImpl";
	private static final String desc_ = "Implements LexGrid Pick List Definition services.";
	private static final String provider_ = "Mayo Clinic";
	private static final String version_ = "2.0";
	
	private static LexEVSPickListDefinitionServicesImpl pickListService_ = null;
	
	private DatabaseServiceManager databaseServiceManager = LexEvsServiceLocator.getInstance().getDatabaseServiceManager();
	
	/**
     * Returns a default singleton instance of the service.
     * <p>
     * Note: This is the recommended method of acquiring the service, since it
     * will allow the application to run without change in distributed LexBIG
     * environments (in which case the default instance is actually a
     * distributed service). However, use of the public constructor is supported
     * to preserve backward compatibility.
     * 
     * @return LexEVSPickListDefinitionServicesImpl
     */
    public static LexEVSPickListDefinitionServicesImpl defaultInstance() {
        if (pickListService_ == null)
        	pickListService_ = new LexEVSPickListDefinitionServicesImpl();
        return pickListService_;
    }

    /**
     * Assigns the default singleton instance of the service.
     * <p>
     * Note: While this method is public, it is generally not intended to be
     * part of the externalized API. It is made public so that the runtime
     * system has the ability to assign the default instance when running in
     * distributed LexBIG environments, etc.
     * 
     * @param LexEVSPickListDefinitionServicesImpl
     *            the default instance.
     */
    public static void setDefaultInstance(LexEVSPickListDefinitionServicesImpl defaultInstance) {
    	pickListService_ = defaultInstance;
    }
    
	public LexEVSPickListDefinitionServicesImpl() {
		getStatus().setState(ProcessState.PROCESSING);
		getStatus().setStartTime(new Date(System.currentTimeMillis()));
		md_ = new MessageDirector(getName(), status_);
	}

	private LoadStatus getStatus() {
		if (status_ == null)
			status_ = new LoadStatus();
		return status_;
	}

	private LgLoggerIF getLogger() {
		return LoggerFactory.getLogger();
	}
	
	/* (non-Javadoc)
	 * @see org.lexgrid.valuesets.LexEVSPickListDefinitionServices#loadPickList(org.LexGrid.valueSets.PickListDefinition, java.lang.String)
	 */
	public void loadPickList(PickListDefinition pldef, String systemReleaseURI, Mappings mappings)
			throws LBException {
		getLogger().logMethod(new Object[] { pldef, systemReleaseURI});
		
		if (pldef != null)
		{
			String pickListId = pldef.getPickListId();
			md_.info("Loading Pick List Definition with ID : " + pickListId);			
			this.databaseServiceManager.getPickListDefinitionService().insertPickListDefinition(pldef, systemReleaseURI != null ? systemReleaseURI.toString():null, mappings);
			md_.info("Finished loading Pick list Definition ID : " + pickListId);
		}
	}

	/* (non-Javadoc)
	 * @see org.lexgrid.valuesets.LexEVSPickListDefinitionServices#loadPickList(java.io.InputStream, boolean)
	 */
	public void loadPickList(InputStream inputStream, boolean failOnAllErrors)
			throws LBException {
		throw new LBException("Method not implemented");
	}

	/* (non-Javadoc)
	 * @see org.lexgrid.valuesets.LexEVSPickListDefinitionServices#loadPickList(java.lang.String, boolean)
	 */
	public void loadPickList(String xmlFileLocation, boolean failOnAllErrors)
			throws LBException {
		getLogger().logMethod(new Object[] { xmlFileLocation });
		LexBIGServiceManager lbsm = LexBIGServiceImpl.defaultInstance().getServiceManager(null);

        LexGridMultiLoaderImpl loader = (LexGridMultiLoaderImpl) lbsm
                .getLoader("LexGrid_Loader");
        
        md_.info("Loading pick list definitions from file : " + xmlFileLocation);
        // load non-async - this should block
        loader.load(new File(xmlFileLocation).toURI(), true, false);
        
        while (loader.getStatus().getEndTime() == null) {
            try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        md_.info("Finished loading pick list definitions");
	}
	
	/* (non-Javadoc)
	 * @see org.lexgrid.valuesets.LexEVSPickListDefinitionServices#validate(java.net.URI, int)
	 */
	public void validate(URI uri, int v1) throws LBException{
		LexBIGServiceManager lbsm;
		lbsm = LexBIGServiceImpl.defaultInstance().getServiceManager(null);
		LexGridMultiLoaderImpl loader = (LexGridMultiLoaderImpl) lbsm
            	.getLoader("LexGrid_Loader");
		loader.validate(uri, v1);
	}
	
	/* (non-Javadoc)
	 * @see org.lexgrid.valuesets.LexEVSPickListDefinitionServices#getPickListDefinitionById(java.lang.String)
	 */
	public PickListDefinition getPickListDefinitionById(String pickListId) throws LBException{
		return (PickListDefinition) this.databaseServiceManager.getPickListDefinitionService().getPickListDefinitionByPickListId(pickListId);
	}

	/* (non-Javadoc)
	 * @see org.lexgrid.valuesets.LexEVSPickListDefinitionServices#listPickListIds()
	 */
	public List<String> listPickListIds() throws LBException {
		return this.databaseServiceManager.getPickListDefinitionService().listPickListIds();
	}	

	/* (non-Javadoc)
	 * @see org.lexgrid.valuesets.LexEVSPickListDefinitionServices#getPickListValueSetDefinition(java.lang.String)
	 */
	public URI getPickListValueSetDefinition(String pickListId) throws LBException {
		URI valueDomainURI = null;
		
		try {
			PickListDefinition pickList = getPickListDefinitionById(pickListId);
			
			if (pickList != null)
				valueDomainURI = new URI(pickList.getRepresentsValueSetDefinition()); // TODO need to change representsValueDomain from String to URI in XML schema
		} catch (URISyntaxException e) {
			md_.fatal("Problem getting PickLists for pickListId : " + pickListId, e);
			throw new LBException("Problem getting PickLists for pickListId : " + pickListId, e);
		}
		return valueDomainURI;
	}

	/* (non-Javadoc)
	 * @see org.lexgrid.valuesets.LexEVSPickListDefinitionServices#getPickListDefinitionsForValueSetDef(java.net.URI)
	 */
	public List<String> getPickListDefinitionIdForValueSetDefinitionUri(URI valueSetDefURI) throws LBException {
		return this.databaseServiceManager.getPickListDefinitionService().getPickListDefinitionIdForValueSetDefinitionUri(valueSetDefURI.toString());		
	}
	
	
	/* (non-Javadoc)
	 * @see org.lexgrid.valuesets.LexEVSPickListDefinitionServices#resolvePickListForTerm(java.lang.String, java.lang.String, org.LexGrid.LexBIG.Utility.LBConstants.MatchAlgorithms)
	 */
	public ResolvedPickListEntryList resolvePickListForTerm(String pickListId,
			String term, String matchAlgorithm, String language, String[] context, boolean sortByText) throws LBException {
		ResolvedPickListEntryList plList = new ResolvedPickListEntryList();
		
		List<String> rcrCodes = new ArrayList<String>();
		List<String> excludeEntityCodes = new ArrayList<String>();
		
		PickListDefinition pickList = this.databaseServiceManager.getPickListDefinitionService().getPickListDefinitionByPickListId(pickListId);
		if (pickList != null)
		{
			String defaultCS = pickList.getDefaultEntityCodeNamespace();
			String defaultLang = pickList.getDefaultLanguage();
			
			boolean completeDomain = pickList.isCompleteSet();
			
			//TODO, if completeDomain is true, dynamically populate pickListEntries
			if (completeDomain)
			{
				return internalResolvePickListForTerm(pickList.getRepresentsValueSetDefinition(), term, matchAlgorithm, language, context, sortByText);
			}
			
			// get all static pickListEntryNodes to get any exclude entries.
			List<PickListEntryNode> plEntryNodeList = pickList.getPickListEntryNodeAsReference();
			
			// Get all exclude list
			for (int i = 0; i < plEntryNodeList.size(); i++)
			{
				PickListEntryNode plEntryNode = plEntryNodeList.get(i);
				
				if (plEntryNode.getPickListEntryNodeChoice() != null)
				{
					PickListEntryExclusion plEntryExclusion = plEntryNode.getPickListEntryNodeChoice().getExclusionEntry();
					if (plEntryExclusion != null)
						excludeEntityCodes.add(plEntryExclusion.getEntityCode());
				}
			}
			
			CodedNodeSet cns = null;
			ConceptReferenceList crList = new ConceptReferenceList();			
			
			
			for (int i = 0; i < plEntryNodeList.size(); i++)
			{
				PickListEntry plEntry = plEntryNodeList.get(i).getPickListEntryNodeChoice().getInclusionEntry();
				
				if (plEntry != null && !excludeEntityCodes.contains(plEntry.getEntityCode()))
				{
					String cs = plEntry.getEntityCodeNamespace();
					if (StringUtils.isEmpty(cs))
						cs = defaultCS;
					
					if (StringUtils.isNotEmpty(cs))
					{
						crList.addConceptReference(Constructors.createConceptReference(plEntry.getEntityCode(), cs));
						if (StringUtils.isEmpty(defaultCS))
							defaultCS = cs;
						if (StringUtils.isEmpty(defaultLang))
							defaultLang = plEntry.getLanguage();
					}
				}
			}
			
			if (crList.getConceptReferenceCount() > 0)
			{
				//TODO not sure about this.. should we get the coding scheme version from the user ?
				AbsoluteCodingSchemeVersionReferenceList acsvrList = getServiceHelper().getAbsoluteCodingSchemeVersionReference(defaultCS);
				CodingSchemeVersionOrTag csvt = null;
				
				if (acsvrList.getAbsoluteCodingSchemeVersionReferenceCount() > 0)
				{
					AbsoluteCodingSchemeVersionReference acsvr = acsvrList.getAbsoluteCodingSchemeVersionReference(0);
					csvt = Constructors.createCodingSchemeVersionOrTag(null, acsvr.getCodingSchemeVersion());
				}
				
				cns = getLexBIGService().getCodingSchemeConcepts(defaultCS, csvt);
				
				cns.restrictToCodes(crList);
				
				cns.restrictToMatchingDesignations(term, null, matchAlgorithm != null ? matchAlgorithm : MatchAlgorithms.LuceneQuery.name(), defaultLang);
				
				ResolvedConceptReferencesIterator rcrItr = cns.resolve(null, null, null, null, false);
				
				while (rcrItr.hasNext())
				{
					rcrCodes.add(rcrItr.next().getCode());
				}
				
				for (int i = 0; i < plEntryNodeList.size(); i++)
				{
					PickListEntry plEntry = plEntryNodeList.get(i).getPickListEntryNodeChoice().getInclusionEntry();
					
					if (plEntry != null && rcrCodes.contains(plEntry.getEntityCode()))
					{
						ResolvedPickListEntry rpl = new ResolvedPickListEntry();
						rpl.setDefault(plEntry.isIsDefault());
						rpl.setEntityCode(plEntry.getEntityCode());
						rpl.setEntityCodeNamespace(plEntry.getEntityCodeNamespace());
						rpl.setPickText(plEntry.getPickText());
						rpl.setPropertyId(plEntry.getPropertyId());
						plList.addResolvedPickListEntry(rpl);
					}
				}
			}
		}
		
		return plList;
	}
	
	private ResolvedPickListEntryList internalResolvePickListForTerm(String valueDomainURI,
			String term, String matchAlgorithm, String language, String[] context, boolean sortByText) throws LBException {
		
		ResolvedPickListEntryList plList = new ResolvedPickListEntryList();
		
		LexEVSValueSetDefinitionServices vds = new LexEVSValueSetDefinitionServicesImpl();
		
		ResolvedValueSetCodedNodeSet rvdCNS;
		
		try {
			rvdCNS = vds.getValueSetDefinitionEntitiesForTerm(term, matchAlgorithm, new URI(valueDomainURI), null, null);
		} catch (URISyntaxException e) {
			throw new LBException("Problem with ValueDomain URI", e);
		}
		
		CodedNodeSet cns = rvdCNS.getCodedNodeSet();
		
		ResolvedConceptReferencesIterator rcrItr = cns.resolve(null, null, null, null, true);
		
		while (rcrItr.hasNext())
		{
			ResolvedConceptReference rcr = rcrItr.next();
			ResolvedPickListEntry rpl = new ResolvedPickListEntry();
			rpl.setEntityCode(rcr.getCode());
			rpl.setEntityCodeNamespace(rcr.getCodeNamespace());
			Entity entity = rcr.getEntity();
			Presentation[] presentations = entity.getPresentation();
			for (Presentation pres : presentations)
			{
				if (pres.isIsPreferred())
				{
					rpl.setPickText(pres.getValue().getContent());
					rpl.setPropertyId(pres.getPropertyId());
					plList.addResolvedPickListEntry(rpl);
				}				
			}
		}
		
		return plList;
	}
	
	/* (non-Javadoc)
	 * @see org.lexgrid.valuesets.LexEVSPickListDefinitionServices#resolvePickList(java.lang.String, boolean)
	 */
	public ResolvedPickListEntryList resolvePickList(String pickListId, boolean sortByText) throws LBException {
		ResolvedPickListEntryList plList = new ResolvedPickListEntryList();
        
        List<String> excludeEntityCodes = new ArrayList<String>();
        Set<String> includedEntityCodes = new HashSet<String>();
        
        PickListDefinition pickList = this.databaseServiceManager.getPickListDefinitionService().getPickListDefinitionByPickListId(pickListId);

        if (pickList != null)
        {
            String defaultCS = pickList.getDefaultEntityCodeNamespace();
//            String defaultLang = pickList.getDefaultLanguage();
            
            boolean completeDomain = pickList.isCompleteSet();
            
            // get all static pickListEntryNodes to get any exclude entries.
            List<PickListEntryNode> plEntryNodeList = pickList.getPickListEntryNodeAsReference();
            
            // Get all exclude list
            for (int i = 0; i < plEntryNodeList.size(); i++)
            {
                PickListEntryNode plEntryNode = plEntryNodeList.get(i);
                
                PickListEntryExclusion plEntryExclusion = plEntryNode.getPickListEntryNodeChoice().getExclusionEntry();
                if (plEntryExclusion != null)
                    excludeEntityCodes.add(plEntryExclusion.getEntityCode());
            }
            
            
            for (int i = 0; i < plEntryNodeList.size(); i++)
            {
                PickListEntry plEntry = plEntryNodeList.get(i).getPickListEntryNodeChoice().getInclusionEntry();
                
                if (plEntry != null && !excludeEntityCodes.contains(plEntry.getEntityCode()))
                {
                    String cs = plEntry.getEntityCodeNamespace();
                    if (StringUtils.isEmpty(cs))
                        cs = defaultCS;
                    
                    includedEntityCodes.add(plEntry.getEntityCode());
                    
                    ResolvedPickListEntry rpl = new ResolvedPickListEntry();
                    rpl.setDefault(plEntry.isIsDefault());
                    rpl.setEntityCode(plEntry.getEntityCode());
                    rpl.setEntityCodeNamespace(StringUtils.isEmpty(plEntry.getEntityCodeNamespace()) ? cs : plEntry.getEntityCodeNamespace());
                    rpl.setEntryOrder(Integer.valueOf(plEntry.getEntryOrder().toString()));
                    rpl.setPickText(plEntry.getPickText());
                    rpl.setPropertyId(plEntry.getPropertyId());
                    plList.addResolvedPickListEntry(rpl);
                }
            }
            
            //if completeDomain is true, resolve value domain and add any missing entities to the list
            if (completeDomain)
            {
            	ResolvedPickListEntryList vdPLList = internalResolvePickListForTerm(pickList.getRepresentsValueSetDefinition(), sortByText);
            	if (plList.getResolvedPickListEntryCount() > 0 )
            	{
            		for (int i = 0; i < vdPLList.getResolvedPickListEntryCount(); i++)
            		{
            			ResolvedPickListEntry vdple = vdPLList.getResolvedPickListEntry(i);
            			if (!includedEntityCodes.contains(vdple.getEntityCode()) && !excludeEntityCodes.contains(vdple.getEntityCode()))
            				plList.addResolvedPickListEntry(vdple);
            		}
            	}
            	else
            	{
            		return vdPLList;
            	}
            }
            
            
        }
        
        return plList;
    }
    
	/**
	 * Resolves pickList definition for supplied pickListId.
	 * 
	 * @param pickListId
	 * 			pickListId of a pickListDefinition.
	 * @param sortByText
	 * 			If 1-Ascending, 2-Descending, and 3-Custom;
	 * @return
	 * 			Resolved PickListEntries.
	 * @throws LBException
	 */
	public ResolvedPickListEntryList resolvePickList(String pickListId,
			Integer sortType) throws LBException {

		ResolvedPickListEntryList resolvedPLEntry = resolvePickList(pickListId,
				true);

		if( sortType == null ) {
			sortType = PLEntryNodeSortUtil.CUSTOM;
		}
		
		ResolvedPickListEntry[] sortedPLEntry = PLEntryNodeSortUtil.sort(
				resolvedPLEntry.getResolvedPickListEntry(), sortType);

		resolvedPLEntry.setResolvedPickListEntry(sortedPLEntry);

		return resolvedPLEntry;
	}
	
    private ResolvedPickListEntryList internalResolvePickListForTerm(String valueDomainURI, boolean sortByText) throws LBException {
        
        ResolvedPickListEntryList plList = new ResolvedPickListEntryList();
        LexEVSValueSetDefinitionServices vds = new LexEVSValueSetDefinitionServicesImpl();
        
        ResolvedValueSetDefinition rvdDef;
        
        try {
            rvdDef = vds.resolveValueSetDefinition(new URI(valueDomainURI), null, null);
        } catch (URISyntaxException e) {
            throw new LBException("Problem with ValueDomain URI", e);
        }
        
        ResolvedConceptReferencesIterator rcrItr = rvdDef.getResolvedConceptReferenceIterator();
        
        while (rcrItr.hasNext())
        {
            ResolvedConceptReference rcr = rcrItr.next();
            ResolvedPickListEntry rpl = new ResolvedPickListEntry();
            rpl.setEntityCode(rcr.getCode());
            rpl.setEntityCodeNamespace(rcr.getCodeNamespace());
            Entity entity = rcr.getEntity();
            Presentation[] presentations = entity.getPresentation();
            for (Presentation pres : presentations)
            {
                if (pres.isIsPreferred())
                {
                    rpl.setPickText(pres.getValue().getContent());
                    rpl.setPropertyId(pres.getPropertyId());
                    plList.addResolvedPickListEntry(rpl);
                }               
            }
        }
        
        return plList;
    }

	/* (non-Javadoc)
	 * @see org.lexgrid.valuesets.LexEVSPickListDefinitionServices#removePickList(java.lang.String)
	 */
	public void removePickList(String pickListId) throws LBException{
		getLogger().logMethod(new Object[] { pickListId });
		if (pickListId != null)
		{
			System.out.println("removing pick list definition : " + pickListId);
			SystemResourceService service = LexEvsServiceLocator.getInstance().getSystemResourceService();
			this.databaseServiceManager.getPickListDefinitionService().removePickListDefinitionByPickListId(pickListId);
			service.removePickListDefinitionResourceFromSystem(pickListId, null);
			System.out.println("DONE removing pick list definition : " + pickListId);
		}
		
	}
	
	public Map<String, String> getReferencedPLDefinitions(String entityCode,
			String entityCodeNameSpace, String propertyId,
			Boolean extractPickListName) throws LBException {
		Map<String, String> refPLDef = null;
		List<String> plIds = getPickListService().getPickListDefinitionIdForEntityReference(entityCode,
				entityCodeNameSpace, propertyId);
		
		if (plIds != null && plIds.size() > 0)
		{
			refPLDef = new HashMap<String, String>();
			
			for (String plId : plIds)
			{
				refPLDef.put(plId, extractPickListName ? getPickListName(plId) : null);
			}
		}		
		
		return refPLDef;
	}

	public Map<String, String> getReferencedPLDefinitions(String valueSet, Boolean extractPickListName)
			throws LBException {
		Map<String, String> refPLDef = null;
		
		List<String> plIds = getPickListService().getPickListDefinitionIdForValueSetDefinitionUri(valueSet);
		
		if (plIds != null && plIds.size() > 0)
		{
			refPLDef = new HashMap<String, String>();
			
			for (String plId : plIds)
			{
				refPLDef.put(plId, extractPickListName ? getPickListName(plId) : null);
			}
		}		
		
		return refPLDef;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.LexGrid.LexBIG.Extensions.Extendable#getDescription()
	 */
	public String getDescription() {
		return desc_;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.LexGrid.LexBIG.Extensions.Extendable#getName()
	 */
	public String getName() {
		return name_;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.LexGrid.LexBIG.Extensions.Extendable#getProvider()
	 */
	public String getProvider() {
		return provider_;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.LexGrid.LexBIG.Extensions.Extendable#getVersion()
	 */
	public String getVersion() {
		return version_;
	}
	
	public LogEntry[] getLogEntries(){
		if (md_ != null)
			return md_.getLogEntries(null);
		
		return null;
	}
	
	/**
	 * Assign the associated LexBIGService instance.
	 * <p>
	 * Note: This method must be invoked by users of the distributed LexBIG API
	 * to set the service to an EVSApplicationService object, allowing client
	 * side implementations to use these convenience methods.
	 */
	@LgClientSideSafe
	public void setLexBIGService(LexBIGService lbs) {
		lbs_ = lbs;
	}

	/**
	 * Return the associated LexBIGService instance; lazy initialized as
	 * required.
	 */
	@LgClientSideSafe
	public LexBIGService getLexBIGService() {
		if (lbs_ == null)
			lbs_ = LexBIGServiceImpl.defaultInstance();
		return lbs_;
	}

	private PickListDefinitionService getPickListService() throws LBException {
		if (pls_ == null)
			pls_ = this.databaseServiceManager.getPickListDefinitionService();
		return pls_;
	}
	
	private String getPickListName(String pickListId) throws LBException {
		
		String pickListName = null;
		LexBIGService lbSvc = LexBIGServiceImpl.defaultInstance();
		
		CodingSchemeRenderingList suppCodingSchemes = lbSvc.getSupportedCodingSchemes();

		CodingSchemeRendering[] csRendering = suppCodingSchemes.getCodingSchemeRendering();

		for (int i = 0; i < csRendering.length; i++) {CodingSchemeSummary csSummary = csRendering[i].getCodingSchemeSummary();

			String csName = csSummary.getLocalName();
			String version = csSummary.getRepresentsVersion();

			CodingSchemeVersionOrTag versionOrTag = new CodingSchemeVersionOrTag();
			versionOrTag.setVersion(version);

			CodedNodeSet codeSet = lbSvc.getCodingSchemeConcepts(csName, versionOrTag);

			codeSet.restrictToCodes(Constructors.createConceptReferenceList(pickListId));

			ResolvedConceptReferenceList conceptRef = codeSet
					.resolveToList(null, null, null, -1);

			if (conceptRef.getResolvedConceptReferenceCount() > 0) {
				Entity entity = conceptRef.getResolvedConceptReference(0).getEntity();

				if (entity != null) {
					if (entity.getEntityDescription() != null)
						pickListName = entity.getEntityDescription().getContent();

					if (pickListName == null) {
						Presentation[] allProps = entity.getPresentation();

						for (int j = 0; j < allProps.length; j++) {
							if (allProps[j].getIsPreferred()) {
	
								Text value = allProps[j].getValue();
								if (value != null)
									pickListName = value.getContent();
							}
						}
					}
				}
				break;
			}
		}

		return pickListName;
	}

	@Override
	public List<String> getPickListIdsForSupportedTagAndValue(
			String supportedTag, String value) {
		return this.databaseServiceManager.getPickListDefinitionService().getPickListDefinitionIdForSupportedTagAndValue(supportedTag, value);
	}

	public VSDServiceHelper getServiceHelper(){
		if (sh_ == null)
		{
			try {
				sh_ = new VSDServiceHelper(true, md_);
			} catch (LBParameterException e) {
				md_.fatal("Problem getting ServiceHelper", e);
				e.printStackTrace();
			} catch (LBInvocationException e) {
				md_.fatal("Problem getting ServiceHelper", e);
				e.printStackTrace();
			}
		}
		return sh_;
	}
//	private String getStringFromURI(URI uri) throws LBParameterException {
//        if ("file".equals(uri.getScheme()))
//
//        {
//            File temp = new File(uri);
//            return temp.getAbsolutePath();
//        } 
//        
//        return uri.toString();
//    }
}