package org.lexevs.dao.database.ibatis.picklist.parameter;

import org.LexGrid.valueSets.PickListEntry;
import org.LexGrid.valueSets.PickListEntryExclusion;

public class PickListEntryNodeBean extends org.LexGrid.commonTypes.Versionable 
implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String pickListEntryId;
	
	private Boolean include;
	
	private PickListEntry pickListEntry;
	
	private PickListEntryExclusion pickListEntryExclusion;

	/**
	 * @return the pickListEntryId
	 */
	public String getPickListEntryId() {
		return pickListEntryId;
	}

	/**
	 * @param pickListEntryId the _pickListEntryId to set
	 */
	public void setPickListEntryId(String pickListEntryId) {
		this.pickListEntryId = pickListEntryId;
	}

	/**
	 * @return the include
	 */
	public Boolean getInclude() {
		return include;
	}

	/**
	 * @param include the include to set
	 */
	public void setInclude(Boolean include) {
		this.include = include;
	}

	/**
	 * @return the pickListEntry
	 */
	public PickListEntry getPickListEntry() {
		return pickListEntry;
	}

	/**
	 * @param pickListEntry the pickListEntry to set
	 */
	public void setPickListEntry(PickListEntry pickListEntry) {
		this.pickListEntry = pickListEntry;
	}

	/**
	 * @return the pickListEntryExclusion
	 */
	public PickListEntryExclusion getPickListEntryExclusion() {
		return pickListEntryExclusion;
	}

	/**
	 * @param pickListEntryExclusion the pickListEntryExclusion to set
	 */
	public void setPickListEntryExclusion(
			PickListEntryExclusion pickListEntryExclusion) {
		this.pickListEntryExclusion = pickListEntryExclusion;
	}
	
}
