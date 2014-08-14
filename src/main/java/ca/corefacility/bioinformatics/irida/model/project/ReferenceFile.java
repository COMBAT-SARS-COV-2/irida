package ca.corefacility.bioinformatics.irida.model.project;

import java.nio.file.Path;
import java.util.Date;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;

import ca.corefacility.bioinformatics.irida.model.IridaThing;
import ca.corefacility.bioinformatics.irida.model.VersionedFileFields;

/**
 * A reference file to be associated with a {@link Project}.
 * 
 * @author Franklin Bristow <franklin.bristow@phac-aspc.gc.ca>
 *
 */
@Entity
@Table(name = "reference_file")
@Audited
public class ReferenceFile implements VersionedFileFields<Long>, IridaThing {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "filePath", unique = true)
	@NotNull(message = "{reference.file.file.notnull}")
	private Path file;

	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	private final Date createdDate;

	@Temporal(TemporalType.TIMESTAMP)
	private Date modifiedDate;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, mappedBy = "referenceFile")
	private ProjectReferenceFileJoin project;

	private Long fileRevisionNumber; // the filesystem file revision number
	
	public int hashCode() {
		return file.hashCode();
	}
	
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof ReferenceFile) {
			return Objects.equals(file, ((ReferenceFile) o).file);
		}
		
		return false;
	}

	public ReferenceFile() {
		this.createdDate = new Date();
		this.fileRevisionNumber = 0L;
	}

	public ReferenceFile(Path file) {
		this();
		this.file = file;
	}

	@Override
	public String getLabel() {
		return file.getFileName().toString();
	}

	@Override
	public Long getId() {
		return this.id;
	}

	@Override
	public Date getModifiedDate() {
		return this.modifiedDate;
	}

	@Override
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public Path getFile() {
		return file;
	}

	public void setFile(Path file) {
		this.file = file;
	}

	@Override
	public Date getCreatedDate() {
		return this.createdDate;
	}
	public void incrementFileRevisionNumber() {
		this.fileRevisionNumber++;
	}

	public Long getFileRevisionNumber() {
		return fileRevisionNumber;
	}
}
