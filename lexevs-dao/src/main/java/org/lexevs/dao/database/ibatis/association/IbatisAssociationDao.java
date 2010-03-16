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
package org.lexevs.dao.database.ibatis.association;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.LexGrid.relations.AssociationPredicate;
import org.LexGrid.relations.AssociationQualification;
import org.LexGrid.relations.AssociationSource;
import org.LexGrid.relations.AssociationTarget;
import org.LexGrid.relations.Relations;
import org.LexGrid.util.sql.lgTables.SQLTableConstants;
import org.junit.Assert;
import org.lexevs.dao.database.access.association.AssociationDao;
import org.lexevs.dao.database.access.association.batch.AssociationSourceBatchInsertItem;
import org.lexevs.dao.database.access.association.batch.TransitiveClosureBatchInsertItem;
import org.lexevs.dao.database.access.codingscheme.CodingSchemeDao;
import org.lexevs.dao.database.ibatis.AbstractIbatisDao;
import org.lexevs.dao.database.ibatis.association.parameter.InsertAssociationPredicateBean;
import org.lexevs.dao.database.ibatis.association.parameter.InsertAssociationQualificationOrUsageContextBean;
import org.lexevs.dao.database.ibatis.association.parameter.InsertAssociationSourceBean;
import org.lexevs.dao.database.ibatis.association.parameter.InsertRelationsBean;
import org.lexevs.dao.database.ibatis.association.parameter.InsertTransitiveClosureBean;
import org.lexevs.dao.database.ibatis.batch.IbatisBatchInserter;
import org.lexevs.dao.database.ibatis.batch.IbatisInserter;
import org.lexevs.dao.database.ibatis.batch.SqlMapExecutorBatchInserter;
import org.lexevs.dao.database.ibatis.parameter.PrefixedParameter;
import org.lexevs.dao.database.ibatis.parameter.PrefixedParameterTuple;
import org.lexevs.dao.database.ibatis.versions.IbatisVersionsDao;
import org.lexevs.dao.database.schemaversion.LexGridSchemaVersion;
import org.lexevs.dao.database.utility.DaoUtility;
import org.springframework.orm.ibatis.SqlMapClientCallback;

import com.ibatis.sqlmap.client.SqlMapExecutor;

/**
 * The Class IbatisAssociationDao.
 * 
 * @author <a href="mailto:kevin.peterson@mayo.edu">Kevin Peterson</a>
 */
public class IbatisAssociationDao extends AbstractIbatisDao implements AssociationDao {

	/** The supported datebase version. */
	private LexGridSchemaVersion supportedDatebaseVersion = LexGridSchemaVersion.parseStringToVersion("2.0");
	
	/** The INSER t_ relation s_ sql. */
	private static String INSERT_RELATIONS_SQL = "insertRelations";
	
	/** The INSER t_ entit y_ assn s_ t o_ entit y_ sql. */
	private static String INSERT_ENTITY_ASSNS_TO_ENTITY_SQL = "insertEntityAssnsToEntity";
	
	/** The INSER t_ associatio n_ qua l_ o r_ contex t_ sql. */
	private static String INSERT_ASSOCIATION_QUAL_OR_CONTEXT_SQL = "insertAssociationQualificationOrUsageContext";
	
	/** The INSER t_ associatio n_ predicat e_ sql. */
	private static String INSERT_ASSOCIATION_PREDICATE_SQL = "insertAssociationPredicate";
	
	/** The INSER t_ transitiv e_ closur e_ sql. */
	private static String INSERT_TRANSITIVE_CLOSURE_SQL = "insertTransitiveClosure";
	
	/** The GE t_ associatio n_ instanc e_ ke y_ sql. */
	private static String GET_ASSOCIATION_INSTANCE_KEY_SQL = "getAccociationInstanceKey";
	
	/** The GE t_ relation s_ ke y_ sql. */
	private static String GET_RELATIONS_KEY_SQL = "getRelationsKey";
	
	/** The GE t_ associatio n_ predicat e_ ke y_ sql. */
	private static String GET_ASSOCIATION_PREDICATE_KEY_SQL = "getAssociationPredicateKey";
	
	private static String GET_ASSOCIATION_PREDICATE_NAME_FOR_ID_SQL = "getAssociationPredicateNameForId";
	
	/** The ibatis versions dao. */
	private IbatisVersionsDao ibatisVersionsDao;
	
	/** The coding scheme dao. */
	private CodingSchemeDao codingSchemeDao;

	/* (non-Javadoc)
	 * @see org.lexevs.dao.database.access.association.AssociationDao#getAssociationPredicateId(java.lang.String, java.lang.String, java.lang.String)
	 */
	public String getAssociationPredicateId(String codingSchemeId,
			String relationContainerId, String associationPredicateName) {
		String prefix = this.getPrefixResolver().resolvePrefixForCodingScheme(codingSchemeId);
		return
			(String) 
				this.getSqlMapClientTemplate().queryForObject(GET_ASSOCIATION_PREDICATE_KEY_SQL, new PrefixedParameterTuple(
						prefix, relationContainerId, associationPredicateName));
	}
	
	/* (non-Javadoc)
	 * @see org.lexevs.dao.database.access.association.AssociationDao#getRelationsId(java.lang.String, java.lang.String)
	 */
	public String getRelationsId(String codingSchemeId, String relationsName) {
		String prefix = this.getPrefixResolver().resolvePrefixForCodingScheme(codingSchemeId);
		return
			(String) 
				this.getSqlMapClientTemplate().queryForObject(GET_RELATIONS_KEY_SQL, new PrefixedParameterTuple(prefix, codingSchemeId, relationsName));
	}
	
	public String getAssociationPredicateNameForId(String codingSchemeId, String associationPredicateId) {
		String prefix = this.getPrefixResolver().resolvePrefixForCodingScheme(codingSchemeId);
		return
			(String) 
				this.getSqlMapClientTemplate().queryForObject(GET_ASSOCIATION_PREDICATE_NAME_FOR_ID_SQL, new PrefixedParameter(prefix, associationPredicateId));
	}

	/**
	 * Insert relations.
	 * 
	 * @param codingSchemeUri the coding scheme uri
	 * @param version the version
	 * @param relations the relations
	 */
	public void insertRelations(String codingSchemeUri, String version,
			Relations relations) {
		String codingSchemeId = codingSchemeDao.getCodingSchemeIdByUriAndVersion(codingSchemeUri, version);
		this.insertRelations(
				codingSchemeId, 
				relations);
	}

	/* (non-Javadoc)
	 * @see org.lexevs.dao.database.access.association.AssociationDao#insertRelations(java.lang.String, org.LexGrid.relations.Relations)
	 */
	public String insertRelations(String codingSchemeId,
			Relations relations) {
		String relationsId = this.createUniqueId();
		String entryStateId = this.createUniqueId();
		
		InsertRelationsBean bean = new InsertRelationsBean();
		bean.setEntryStateId(entryStateId);
		bean.setId(relationsId);
		bean.setCodingSchemeId(codingSchemeId);
		bean.setRelations(relations);
		bean.setPrefix(this.getPrefixResolver().resolvePrefixForCodingScheme(codingSchemeId));
		
		this.getSqlMapClientTemplate().insert(INSERT_RELATIONS_SQL, bean);
		
		for(AssociationPredicate predicate : relations.getAssociationPredicate()){
			this.insertAssociationPredicate(codingSchemeId, relationsId, predicate);
		}
	
		return relationsId;
	}

	/* (non-Javadoc)
	 * @see org.lexevs.dao.database.access.association.AssociationDao#insertAssociationPredicate(java.lang.String, java.lang.String, org.LexGrid.relations.AssociationPredicate)
	 */
	public String insertAssociationPredicate(String codingSchemeId, String relationId,
			AssociationPredicate associationPredicate) {
		
		String id = this.createUniqueId();
		InsertAssociationPredicateBean bean = new InsertAssociationPredicateBean();
		bean.setPrefix(this.getPrefixResolver().resolvePrefixForCodingScheme(codingSchemeId));
		bean.setAssociationPredicate(associationPredicate);
		bean.setId(id);
		bean.setRelationId(relationId);
		
		this.getSqlMapClientTemplate().insert(INSERT_ASSOCIATION_PREDICATE_SQL, bean);
		
		this.insertBatchAssociationSources(codingSchemeId, id, Arrays.asList(associationPredicate.getSource()));
		
		return id;
	}
	
	/* (non-Javadoc)
	 * @see org.lexevs.dao.database.access.association.AssociationDao#insertBatchAssociationSources(java.lang.String, java.util.List)
	 */
	public void insertBatchAssociationSources(final String codingSchemeId,
			final List<AssociationSourceBatchInsertItem> list) {
		
		this.getSqlMapClientTemplate().execute(new SqlMapClientCallback(){
		
			public Object doInSqlMapClient(SqlMapExecutor executor)
					throws SQLException {
				IbatisBatchInserter batchInserter = new SqlMapExecutorBatchInserter(executor);
				
				batchInserter.startBatch();
				
				for(AssociationSourceBatchInsertItem item : list){
					
					insertAssociationSource(
							codingSchemeId, 
							item.getParentId(), 
							item.getAssociationSource(), 
							batchInserter);
				}
				
				batchInserter.executeBatch();
				
				return null;
			}	
		});
	}
	
	/* (non-Javadoc)
	 * @see org.lexevs.dao.database.access.association.AssociationDao#insertAssociationSource(java.lang.String, java.lang.String, org.LexGrid.relations.AssociationSource)
	 */
	public void insertAssociationSource(String codingSchemeId,
			String associationPredicateId, AssociationSource source){
		this.insertAssociationSource(codingSchemeId, associationPredicateId, source, 
				this.getNonBatchTemplateInserter());
	}
	
	/* (non-Javadoc)
	 * @see org.lexevs.dao.database.access.association.AssociationDao#insertBatchAssociationSources(java.lang.String, java.lang.String, java.util.List)
	 */
	@Override
	public void insertBatchAssociationSources(final String codingSchemeId,
			final String associationPredicateId, final List<AssociationSource> batch) {
		this.getSqlMapClientTemplate().execute(new SqlMapClientCallback(){
			
			public Object doInSqlMapClient(SqlMapExecutor executor)
					throws SQLException {
				IbatisBatchInserter batchInserter = new SqlMapExecutorBatchInserter(executor);
				
				batchInserter.startBatch();
				
				for(AssociationSource source : batch) {
					insertAssociationSource(codingSchemeId, associationPredicateId, source);
				}
				
				batchInserter.executeBatch();
				
				return null;
			}	
		});
	}
	
	/**
	 * Insert into transitive closure.
	 * 
	 * @param codingSchemeId the coding scheme id
	 * @param associationPredicateId the association predicate id
	 * @param sourceEntityCode the source entity code
	 * @param sourceEntityCodeNamespace the source entity code namespace
	 * @param targetEntityCode the target entity code
	 * @param targetEntityCodeNamespace the target entity code namespace
	 * @param executor the executor
	 * 
	 * @return the string
	 */
	public String insertIntoTransitiveClosure(
			String codingSchemeId,
			String associationPredicateId, 
			String sourceEntityCode,
			String sourceEntityCodeNamespace, 
			String targetEntityCode,
			String targetEntityCodeNamespace, 
			IbatisInserter executor) {
		
		String id = this.createUniqueId();
		
		InsertTransitiveClosureBean bean = new InsertTransitiveClosureBean();
		bean.setPrefix(this.getPrefixResolver().resolvePrefixForCodingScheme(codingSchemeId));
		bean.setId(id);
		bean.setAssociationPredicateId(associationPredicateId);
		bean.setSourceEntityCode(sourceEntityCode);
		bean.setSourceEntityCodeNamespace(sourceEntityCodeNamespace);
		bean.setTargetEntityCode(targetEntityCode);
		bean.setTargetEntityCodeNamespace(targetEntityCodeNamespace);
		
		this.getSqlMapClientTemplate().insert(INSERT_TRANSITIVE_CLOSURE_SQL, bean);
		
		return id;
		
	}

	/* (non-Javadoc)
	 * @see org.lexevs.dao.database.access.association.AssociationDao#insertIntoTransitiveClosure(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String insertIntoTransitiveClosure(String codingSchemeId,
			String associationPredicateId, String sourceEntityCode,
			String sourceEntityCodeNamespace, String targetEntityCode,
			String targetEntityCodeNamespace) {
		
		return this.insertIntoTransitiveClosure(codingSchemeId,
				associationPredicateId, 
				sourceEntityCode,
				sourceEntityCodeNamespace, 
				targetEntityCode,
				targetEntityCodeNamespace,
				this.getNonBatchTemplateInserter());
	}
	
	/**
	 * Insert batch transitive closure.
	 * 
	 * @param codingSchemeId the coding scheme id
	 * @param batch the batch
	 */
	public void insertBatchTransitiveClosure(final String codingSchemeId,
			final List<TransitiveClosureBatchInsertItem> batch) {
		
		this.getSqlMapClientTemplate().execute(new SqlMapClientCallback(){

			public Object doInSqlMapClient(SqlMapExecutor executor)
					throws SQLException {
				IbatisBatchInserter batchInserter = new SqlMapExecutorBatchInserter(executor);
				
				batchInserter.startBatch();
				
				for(TransitiveClosureBatchInsertItem item : batch){
					insertIntoTransitiveClosure(codingSchemeId,
							item.getAssociationPredicateId(), 
							item.getSourceEntityCode(),
							item.getSourceEntityCodeNamespace(), 
							item.getTargetEntityCode(),
							item.getTargetEntityCodeNamespace());
				}

				batchInserter.executeBatch();

				return null;
			}
		});
	}


	/**
	 * Insert association source.
	 * 
	 * @param codingSchemeId the coding scheme id
	 * @param associationPredicateId the association predicate id
	 * @param source the source
	 * @param inserter the inserter
	 */
	public void insertAssociationSource(String codingSchemeId,
			String associationPredicateId, AssociationSource source, IbatisInserter inserter) {
		Assert.assertTrue("Must Insert at least ONE AssociationTarget per AssociationSource.", source.getTarget().length > 0);
		
		String prefix = this.getPrefixResolver().resolvePrefixForCodingScheme(codingSchemeId);
		
		for(AssociationTarget target : source.getTarget()){
			String associationTargetId = this.createUniqueId();
			String entryStateId = this.createUniqueId();
			
			InsertAssociationSourceBean bean = new InsertAssociationSourceBean();
			bean.setPrefix(prefix);
			bean.setAssociationPredicateId(associationPredicateId);
			bean.setAssociationSource(source);
			bean.setAssociationTarget(target);
			bean.setEntryStateId(entryStateId);
			bean.setId(associationTargetId);
			inserter.insert(INSERT_ENTITY_ASSNS_TO_ENTITY_SQL, bean);
			
			ibatisVersionsDao.insertEntryState(codingSchemeId, entryStateId, associationTargetId, "associationSource", null, target.getEntryState(), inserter);
			
			for(AssociationQualification qual : target.getAssociationQualification()){
				String qualId = this.createUniqueId();
				
				InsertAssociationQualificationOrUsageContextBean qualBean = new InsertAssociationQualificationOrUsageContextBean();
				qualBean.setAssociationTargetId(associationTargetId);
				qualBean.setId(qualId);
				qualBean.setPrefix(prefix);
				qualBean.setQualifierName(qual.getAssociationQualifier());
				qualBean.setQualifierValue(qual.getQualifierText().getContent());
				
				inserter.insert(
						INSERT_ASSOCIATION_QUAL_OR_CONTEXT_SQL, 
						qualBean);
			}
			
			for(String context : target.getUsageContext()){
				String contextId = this.createUniqueId();
				
				InsertAssociationQualificationOrUsageContextBean contextBean = new InsertAssociationQualificationOrUsageContextBean();
				contextBean.setAssociationTargetId(associationTargetId);
				contextBean.setId(contextId);
				contextBean.setPrefix(prefix);
				contextBean.setQualifierName(SQLTableConstants.TBLCOLVAL_USAGECONTEXT);
				contextBean.setQualifierValue(context);
				
				inserter.insert(
						INSERT_ASSOCIATION_QUAL_OR_CONTEXT_SQL, 
						contextBean);
			}
		}
	}
	

	/* (non-Javadoc)
	 * @see org.lexevs.dao.database.access.association.AssociationDao#insertAssociationQualifier(java.lang.String, java.lang.String, org.LexGrid.relations.AssociationQualification)
	 */
	public void insertAssociationQualifier(String codingSchemeId,
			String associationInstanceId, AssociationQualification qualifier) {
		String prefix = this.getPrefixResolver().resolvePrefixForCodingScheme(codingSchemeId);
		String associationTargetId = this.getKeyForAssociationInstanceId(codingSchemeId, associationInstanceId);
		
		String qualId = this.createUniqueId();
		
		InsertAssociationQualificationOrUsageContextBean contextBean = new InsertAssociationQualificationOrUsageContextBean();
		contextBean.setAssociationTargetId(associationTargetId);
		contextBean.setId(qualId);
		contextBean.setPrefix(prefix);
		contextBean.setQualifierName(qualifier.getAssociationQualifier());
		contextBean.setQualifierValue(qualifier.getQualifierText().getContent());
		
		this.getSqlMapClientTemplate().insert(
				INSERT_ASSOCIATION_QUAL_OR_CONTEXT_SQL, 
				contextBean);
	}
	
	/**
	 * Gets the key for association instance id.
	 * 
	 * @param codingSchemeId the coding scheme id
	 * @param associationInstanceId the association instance id
	 * 
	 * @return the key for association instance id
	 */
	protected String getKeyForAssociationInstanceId(String codingSchemeId, String associationInstanceId){
		String prefix = this.getPrefixResolver().resolvePrefixForCodingScheme(codingSchemeId);
		
		return (String) this.getSqlMapClientTemplate().queryForObject(
				
				GET_ASSOCIATION_INSTANCE_KEY_SQL, 
				new PrefixedParameterTuple(prefix, codingSchemeId, associationInstanceId));
	}
	
	/* (non-Javadoc)
	 * @see org.lexevs.dao.database.access.AbstractBaseDao#doGetSupportedLgSchemaVersions()
	 */
	@Override
	public List<LexGridSchemaVersion> doGetSupportedLgSchemaVersions() {
		return DaoUtility.createList(LexGridSchemaVersion.class, supportedDatebaseVersion);
	}

	

	/**
	 * Gets the ibatis versions dao.
	 * 
	 * @return the ibatis versions dao
	 */
	public IbatisVersionsDao getIbatisVersionsDao() {
		return ibatisVersionsDao;
	}

	/**
	 * Sets the ibatis versions dao.
	 * 
	 * @param ibatisVersionsDao the new ibatis versions dao
	 */
	public void setIbatisVersionsDao(IbatisVersionsDao ibatisVersionsDao) {
		this.ibatisVersionsDao = ibatisVersionsDao;
	}

	/**
	 * Sets the coding scheme dao.
	 * 
	 * @param codingSchemeDao the new coding scheme dao
	 */
	public void setCodingSchemeDao(CodingSchemeDao codingSchemeDao) {
		this.codingSchemeDao = codingSchemeDao;
	}

	/**
	 * Gets the coding scheme dao.
	 * 
	 * @return the coding scheme dao
	 */
	public CodingSchemeDao getCodingSchemeDao() {
		return codingSchemeDao;
	}
}
