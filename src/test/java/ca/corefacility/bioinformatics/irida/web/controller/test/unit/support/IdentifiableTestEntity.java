package ca.corefacility.bioinformatics.irida.web.controller.test.unit.support;

import java.util.Date;

import ca.corefacility.bioinformatics.irida.model.IridaThing;

/**
 * 
 * @author Franklin Bristow <franklin.bristow@phac-aspc.gc.ca>
 */
public class IdentifiableTestEntity implements IridaThing, Comparable<IdentifiableTestEntity> {
	Long id;

	private Date createdDate;

	private Date modifiedDate;

	public IdentifiableTestEntity() {

	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long identifier) {
		this.id = identifier;
	}

	@Override
	public int compareTo(IdentifiableTestEntity o) {
		return createdDate.compareTo(o.createdDate);
	}

	@Override
	public Date getTimestamp() {
		return createdDate;
	}

	@Override
	public void setTimestamp(Date timestamp) {
		this.createdDate = timestamp;
	}

	@Override
	public String getLabel() {
		return null;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
}
