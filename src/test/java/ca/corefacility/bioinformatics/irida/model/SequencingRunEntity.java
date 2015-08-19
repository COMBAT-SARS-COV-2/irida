package ca.corefacility.bioinformatics.irida.model;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import ca.corefacility.bioinformatics.irida.model.enums.SequencingRunUploadStatus;
import ca.corefacility.bioinformatics.irida.model.run.SequencingRun;

/**
 * Test sequencing run entity
 * 
 *
 */
@Entity
@Table(name = "sequencing_run_entity")
@Audited
public class SequencingRunEntity extends SequencingRun {
	private String data;

	public SequencingRunEntity() {
		super(LayoutType.PAIRED_END, SequencingRunUploadStatus.UPLOADING);
	}

	public SequencingRunEntity(String data) {
		this();
		this.data = data;
	}

	/**
	 * @return the data
	 */
	public String getData() {
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(String data) {
		this.data = data;
	}

	@Override
	public String getSequencerType() {
		return "TestSequencer";
	}

	@Override
	public int compareTo(SequencingRun o) {
		return 0;
	}

}
