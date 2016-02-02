package ca.corefacility.bioinformatics.irida.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.corefacility.bioinformatics.irida.exceptions.DuplicateSampleException;
import ca.corefacility.bioinformatics.irida.exceptions.EntityNotFoundException;
import ca.corefacility.bioinformatics.irida.model.joins.Join;
import ca.corefacility.bioinformatics.irida.model.run.SequencingRun;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.model.sample.SampleSequenceFileJoin;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequenceFile;

/**
 * Service for managing {@link SequenceFile} entities.
 * 
 */
public interface SequenceFileService extends CRUDService<Long, SequenceFile> {

	/**
	 * Persist the {@link SequenceFile} to the database and create a new
	 * relationship between the {@link SequenceFile} and a {@link Sample}
	 * 
	 * Note: This method will throw {@link IllegalArgumentException} if the
	 * {@link SequenceFile} has an associated {@link SequencingRun} with the
	 * wrong type of {@link SequencingRun.LayoutType}
	 * 
	 * @param sequenceFile
	 *            the {@link SequenceFile} to be persisted.
	 * @param sample
	 *            The sample to add the file to
	 * @return the {@link Join} between the {@link SequenceFile} and its
	 *         {@link Sample}.
	 */
	@Deprecated
	public Join<Sample, SequenceFile> createSequenceFileInSample(SequenceFile sequenceFile, Sample sample);

	/**
	 * Create a pair of {@link SequenceFile}s in a {@link Sample}
	 * 
	 * Note: This method will throw {@link IllegalArgumentException} if the
	 * {@link SequenceFile} has an associated {@link SequencingRun} with the
	 * wrong type of {@link SequencingRun.LayoutType}
	 * 
	 * @param file1
	 *            First {@link SequenceFile}
	 * @param file2
	 *            Second {@link SequenceFile}
	 * @param sample
	 *            The {@link Sample} to add to
	 * @return The created {@link Join}s
	 */
	@Deprecated
	public List<Join<Sample, SequenceFile>> createSequenceFilePairInSample(SequenceFile file1, SequenceFile file2,
			Sample sample);

	/**
	 * Get a {@link List} of {@link SequenceFile} references for a specific
	 * {@link Sample}.
	 * 
	 * @param sample
	 *            the {@link Sample} to get the {@link SequenceFile} references
	 *            from.
	 * @return the references to {@link SequenceFile}.
	 */
	@Deprecated
	public List<Join<Sample, SequenceFile>> getSequenceFilesForSample(Sample sample);


	/**
	 * Get a {@link List} of {@link SequenceFile} references for a specific
	 * {@link SequencingRun}.
	 * 
	 * @param sequencingRun
	 *            the {@link SequencingRun} to get the {@link SequenceFile}
	 *            references from.
	 * @return the references to {@link SequenceFile}.
	 */
	@Deprecated
	public Set<SequenceFile> getSequenceFilesForSequencingRun(SequencingRun sequencingRun);

	/**
	 * Get the {@link SequenceFile}s that do not have pairs for a {@link Sample}
	 * 
	 * @param sample
	 *            the sample to get unpaired sequence files for.
	 * @return A List of {@link SampleSequenceFileJoin}s
	 */
	@Deprecated
	public List<Join<Sample, SequenceFile>> getUnpairedSequenceFilesForSample(Sample sample);

	/**
	 * Given a set of {@link SequenceFile}s, constructs a map between the
	 * {@link SequenceFile}s and the corresponding {@link Sample}s.
	 *
	 * @param sequenceFiles
	 *            The set of sequence files.
	 * @return A map linking a sample and the sequence files to run.
	 * @throws DuplicateSampleException
	 *             If there was more than one sequence file with the same
	 *             sample.
	 */
	@Deprecated
	public Map<Sample, SequenceFile> getUniqueSamplesForSequenceFiles(Set<SequenceFile> sequenceFiles)
			throws DuplicateSampleException;
}
