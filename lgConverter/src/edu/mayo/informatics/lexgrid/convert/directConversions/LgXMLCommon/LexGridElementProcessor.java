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
 *      http://www.eclipse.org/legal/epl-v10.html
 * 
 */
package edu.mayo.informatics.lexgrid.convert.directConversions.LgXMLCommon;

import java.util.ArrayList;

import org.LexGrid.codingSchemes.CodingScheme;
import org.LexGrid.concepts.Entities;
import org.LexGrid.concepts.Entity;
import org.LexGrid.naming.Mappings;
import org.LexGrid.relations.AssociationPredicate;
import org.LexGrid.relations.AssociationSource;
import org.LexGrid.relations.Relations;
import org.LexGrid.valueSets.PickListDefinition;
import org.LexGrid.valueSets.ValueSetDefinition;
import org.LexGrid.versions.Revision;
import org.LexGrid.versions.SystemRelease;
import org.lexevs.dao.database.service.exception.CodingSchemeAlreadyLoadedException;

/**
 * @author  <A HREF="mailto:scott.bauer@mayo.edu">Scott Bauer </A>
 *
 */
public class LexGridElementProcessor {
    
    private static ArrayList<CodingScheme> codingSchemes = new ArrayList<CodingScheme>();
    private static  CodingScheme[] cs = null;
    


    public static CodingScheme[] setAndRetrieveCodingSchemes() {
        cs = new CodingScheme[codingSchemes.size()];
        for (int i = 0; i < codingSchemes.size(); i++) {
            cs[i] = codingSchemes.get(i);
        }
        return cs;
    }

    /**
     * @param service
     * @param parent
     * @param child
     */
    public static void processCodingSchemeMetadata(XMLDaoServiceAdaptor service, Object parent, Object child) {
        CodingScheme scheme = (CodingScheme) parent;
        try {
            codingSchemes.add(scheme);
            service.storeCodingScheme(scheme);
        } catch (CodingSchemeAlreadyLoadedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param service
     * @param parent
     * @param child
     */
    public static void processCodingSchemeEntity(XMLDaoServiceAdaptor service, Object parent, Object child) {
        Entity e = (Entity) child;
        Entities entities = (Entities) parent;
        CodingScheme c = (CodingScheme) entities.getParent();
        service.storeEntity(e, c);
        entities.removeEntity(e);
    }

    /**
     * @param isPredicateLoaded
     * @param service
     * @param parent
     * @param child
     */
    public static void processCodingSchemeAssociation(boolean isPredicateLoaded, XMLDaoServiceAdaptor service,
            Object parent, Object child) {
        AssociationPredicate a = (AssociationPredicate) parent;
        Relations relations = (Relations) a.getParent();
        CodingScheme cs = (CodingScheme) relations.getParent();
        service.storeRelation(cs.getCodingSchemeURI(), cs.getRepresentsVersion(), relations);
        if (!isPredicateLoaded) {
            service.storeAssociationPredicate(cs.getCodingSchemeURI(), cs.getRepresentsVersion(), relations
                    .getContainerName(), (AssociationPredicate) parent);
        } else {
            service.storeAssociation(cs.getCodingSchemeURI(), cs.getRepresentsVersion(), relations.getContainerName(),
                    a.getAssociationName(), (AssociationSource) child);
        }
        a.removeSource((AssociationSource) child);
    }

    /**
     * @param parent
     */
    public static void removeEntitiesContainer(Object parent) {
        ((CodingScheme) parent).setEntities(null);
    }

    /**
     * @param service
     * @param parent
     * @param child
     */
    public static void processRevisionMetadata(XMLDaoServiceAdaptor service, Object parent, Object child) {
        Revision revision = (Revision) parent;
        System.out.println("storeRevision(c.getCodingSchemeURI()," + "c.getRepresentsVersion(),e.getEntityCode(), "
                + "e.getEntityCodeNamespace(), revision));");
        System.out.println("id: " + revision.getRevisionId());
        System.out.println("description: " + revision.getEntityDescription().getContent());
        System.out.println("change agent: " + revision.getChangeAgent(0));
        System.out.println("change instructions: " + revision.getChangeInstructions().getContent());
    }

    /**
     * @param parent
     * @return
     */
    public static SystemRelease processSystemReleaseMetadata(Object parent) {
        // AuthoringService.loadSystemRelease(parent);
        return (SystemRelease) parent;
    }

    /**
     * @param service
     * @param parent
     * @param child
     * @param mappings
     * @param systemReleaseURI
     */
    public static void processValueSet(XMLDaoServiceAdaptor service, Object parent, Object child, Mappings mappings,
            String systemReleaseURI) {
        ValueSetDefinition valueSet = (ValueSetDefinition) child;
        service.storeValueSet(valueSet, systemReleaseURI, mappings);
    }

    /**
     * @param service
     * @param parent
     * @param child
     * @return
     */
    public static Mappings processValueSetMappings(XMLDaoServiceAdaptor service, Object parent, Object child) {
        Mappings mappings = (Mappings) child;
        return mappings;
    }

    /**
     * @param service
     * @param parent
     * @param child
     * @return
     */
    public static Mappings processPickListMappings(XMLDaoServiceAdaptor service, Object parent, Object child) {
        Mappings mappings = (Mappings) child;
        return mappings;
    }

    /**
     * @param service
     * @param child
     * @param mappings
     * @param systemReleaseURI
     */
    public static void processPickListDefinition(XMLDaoServiceAdaptor service, Object child, Mappings mappings,
            String systemReleaseURI) {
        PickListDefinition picklist = (PickListDefinition) child;
        service.storePickList(picklist, systemReleaseURI, mappings);
    }

}
