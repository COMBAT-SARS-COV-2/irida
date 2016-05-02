package ca.corefacility.bioinformatics.irida.model.sequenceFile;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import ca.corefacility.bioinformatics.irida.model.irida.IridaSequenceFilePair;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableSet;

@Entity
@Table(name = "sequence_file_pair")
@EntityListeners(AuditingEntityListener.class)
@Audited
public class SequenceFilePair extends SequencingObject implements IridaSequenceFilePair {

	/**
	 * Pattern for matching forward {@link SequenceFile}s from a file name.
	 */
	private static final Pattern FORWARD_PATTERN = Pattern.compile(".*_R1_.*");

	/**
	 * Pattern for matching reverse {@link SequenceFile}s from a file name.
	 */
	private static final Pattern REVERSE_PATTERN = Pattern.compile(".*_R2_.*");

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@Size(min = 2, max = 2)
	@CollectionTable(name = "sequence_file_pair_files", joinColumns = @JoinColumn(name = "pair_id"), uniqueConstraints = @UniqueConstraint(columnNames = { "files_id" }, name = "UK_SEQUENCE_FILE_PAIR"))
	private Set<SequenceFile> files;

	public SequenceFilePair() {
		super();
		files = new HashSet<>();
	}

	public SequenceFilePair(SequenceFile file1, SequenceFile file2) {
		this();
		files.add(file1);
		files.add(file2);
	}

	/**
	 * Gets the forward {@link SequenceFile} from the pair.
	 * 
	 * @return The forward {@link SequenceFile} from the pair.
	 */
	public SequenceFile getForwardSequenceFile() {
		return files.stream().filter(f -> FORWARD_PATTERN.matcher(f.getFile().getFileName().toString()).matches())
				.findFirst().get();
	}

	/**
	 * Gets the reverse {@link SequenceFile} from the pair.
	 * 
	 * @return The reverse {@link SequenceFile} from the pair.
	 */
	public SequenceFile getReverseSequenceFile() {
		return files.stream().filter(f -> REVERSE_PATTERN.matcher(f.getFile().getFileName().toString()).matches())
				.findFirst().get();
	}

	@JsonIgnore
	@Override
	public void setModifiedDate(Date modifiedDate) {
		throw new UnsupportedOperationException("Cannot update a sequence file pair");
	}

	@Override
	public String getLabel() {
		return toString();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		Iterator<SequenceFile> iterator = files.iterator();
		builder.append(iterator.next().getLabel()).append(", ").append(iterator.next().getLabel());
		return builder.toString();
	}

	public Set<SequenceFile> getFiles() {
		// returning an ImmutableSet to ensure it isn't changed
		return ImmutableSet.copyOf(files);
	}

	/**
	 * Set the {@link SequenceFile}s in this pair. Note it must contain 2 files.
	 * 
	 * @param files
	 *            The set of {@link SequenceFile}s
	 */
	public void setFiles(Set<SequenceFile> files) {
		if (files.size() != 2) {
			throw new IllegalArgumentException("SequenceFilePair must have 2 files");
		}

		this.files = files;
	}
}
