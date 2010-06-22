package org.lexevs.dao.database.ibatis.revision;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

import org.LexGrid.LexBIG.Exceptions.LBRevisionException;
import org.LexGrid.versions.Revision;
import org.apache.commons.lang.StringUtils;
import org.lexevs.dao.database.access.revision.RevisionDao;
import org.lexevs.dao.database.access.systemRelease.SystemReleaseDao;
import org.lexevs.dao.database.ibatis.AbstractIbatisDao;
import org.lexevs.dao.database.ibatis.versions.parameter.InsertRevisionBean;
import org.lexevs.dao.database.schemaversion.LexGridSchemaVersion;

public class IbatisRevisionDao extends AbstractIbatisDao implements RevisionDao {

	/** The VERSION s_ namespace. */
	public static String VERSIONS_NAMESPACE = "Versions.";

	private String INSERT_INTO_REVISION = VERSIONS_NAMESPACE + "insertRevision"; 
	
	private String SELECT_REVISION_GUID_BY_ID = VERSIONS_NAMESPACE + "getRevisionGuidFromId";

	private String GET_REVISION_ID_BY_DATE = VERSIONS_NAMESPACE + "getRevisionIdByDate";
	
	/** system release dao*/
	private SystemReleaseDao systemReleaseDao = null;
	
	@Override
	public List<Revision> getAllRevisions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Revision getRevisionByGuid(String revisionGuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Revision getRevisionByUri(String revisionUri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRevisionUIdById(String revisionId) {
		if(	revisionId == null )
			return null;
		
		String revisionGuid = null;
		
		revisionGuid = (String) this.getSqlMapClientTemplate()
				.queryForObject(SELECT_REVISION_GUID_BY_ID, revisionId);
		
		return revisionGuid;
	}

	@Override
	public String insertRevisionEntry(Revision revision, String releaseURI) throws LBRevisionException {

		String revisionUId = this.createUniqueId();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		
		if (getRevisionUIdById(revision.getRevisionId()) == null) {

			String releaseUId;
			if(StringUtils.isNotBlank(releaseURI)){
				releaseUId = systemReleaseDao.getSystemReleaseUIdByUri(releaseURI);
			} else {
				releaseUId = null;
			}

			InsertRevisionBean insertRevisionBean = new InsertRevisionBean();

			insertRevisionBean.setRevisionGuid(revisionUId);
			insertRevisionBean.setReleaseGuid(releaseUId);
			insertRevisionBean.setRevAppliedDate(new Timestamp(System
					.currentTimeMillis()));
			insertRevisionBean.setRevision(revision);

			this.getSqlMapClientTemplate().insert(INSERT_INTO_REVISION,
					insertRevisionBean);
		} else {
			throw new LBRevisionException("Revision '"
					+ revision.getRevisionId() + "' already exists.");
		}
		
		return revisionUId;
	}

	public String getRevisionIdForDate(Timestamp dateTime) {
	
		String revisionId = null;
		
		HashMap revisionIdMap = (HashMap) this.getSqlMapClientTemplate()
				.queryForMap(GET_REVISION_ID_BY_DATE, dateTime, "revId",
						"revAppliedDate");
		
		if( revisionIdMap != null && !revisionIdMap.isEmpty()) {
			revisionId = (String) revisionIdMap.keySet().toArray()[0];
		}
		
		return revisionId;
	}
	
	@Override
	public <T> T executeInTransaction(IndividualDaoCallback<T> callback) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean supportsLgSchemaVersion(LexGridSchemaVersion version) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<LexGridSchemaVersion> doGetSupportedLgSchemaVersions() {
		// TODO Auto-generated method stub
		return null;
	}

	public SystemReleaseDao getSystemReleaseDao() {
		return systemReleaseDao;
	}

	public void setSystemReleaseDao(SystemReleaseDao systemReleaseDao) {
		this.systemReleaseDao = systemReleaseDao;
	}

	@Override
	public String getNewRevisionId() {

		return this.createUniqueId();
	}

}
