package ca.corefacility.bioinformatics.irida.model.sequenceFile;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.google.common.collect.ImmutableSet;

import ca.corefacility.bioinformatics.irida.model.IridaResourceSupport;
import ca.corefacility.bioinformatics.irida.model.IridaThing;
import ca.corefacility.bioinformatics.irida.model.genomeFile.AssembledGenomeAnalysis;
import ca.corefacility.bioinformatics.irida.model.irida.IridaSequenceFilePair;

@Entity
@Table(name = "sequence_file_pair")
@EntityListeners(AuditingEntityListener.class)
@Audited
public class SequenceFilePair extends IridaResourceSupport implements IridaThing, IridaSequenceFilePair {

	/**
	 * Pattern for matching forward {@link SequenceFile}s from a file name.
	 */
	private static final Pattern FORWARD_PATTERN = Pattern.compile(".*_R1_.*");

	/**
	 * Pattern for matching reverse {@link SequenceFile}s from a file name.
	 */
	private static final Pattern REVERSE_PATTERN = Pattern.compile(".*_R2_.*");

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@NotNull
	@CreatedDate
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_date")
	private Date createdDate;

	@OneToMany(cascade = CascadeType.DETACH, fetch = FetchType.EAGER)
	@Size(min = 2, max = 2)
	@CollectionTable(name = "sequence_file_pair_files", joinColumns = @JoinColumn(name = "pair_id"), uniqueConstraints = @UniqueConstraint(columnNames = { "files_id" }, name = "UK_SEQUENCE_FILE_PAIR"))
	private Set<SequenceFile> files;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "assembled_genome", unique = true, nullable = true)
	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	private AssembledGenomeAnalysis assembledGenome;

	public SequenceFilePair() {
		createdDate = new Date();
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

	public Long getId() {
		return id;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	/**
	 * Gets an {@link AssembledGenomeAnalysis} that was run from this pair of
	 * sequence files.
	 * 
	 * @return An {@link AssembledGenomeAnalysis} that was run from this pair of
	 *         sequence files.
	 */
	public AssembledGenomeAnalysis getAssembledGenome() {
		return assembledGenome;
	}

	/**
	 * Sets an {@link AssembledGenomeAnalysis} that was run from this pair of
	 * sequence files.
	 * 
	 * @param assembledGenome
	 *            An {@link AssembledGenomeAnalysis} that was run from this pair
	 *            of sequence files.
	 */
	public void setAssembledGenome(AssembledGenomeAnalysis assembledGenome) {
		this.assembledGenome = assembledGenome;
	}

	/**
	 * Whether or not this {@link SequenceFilePair} has an associated
	 * {@link AssembledGenomeAnalysis}.
	 * 
	 * @return True if there as an associated genome, false otherwise.
	 */
	public boolean hasAssembledGenome() {
		return assembledGenome != null;
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

	@Override
	public int hashCode() {
		return Objects.hash(files, assembledGenome);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SequenceFilePair) {
			SequenceFilePair pair = (SequenceFilePair) obj;

			return Objects.equals(files, pair.files) && Objects.equals(assembledGenome, pair.assembledGenome);
		}

		return false;
	}
}
