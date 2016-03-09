package ca.corefacility.bioinformatics.irida.service.remote;

import java.util.List;

import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequenceFile;

/**
 * Service for reading {@link SequenceFile}s
 * 
 *
 */
@Deprecated
public interface SequenceFileRemoteService extends RemoteService<SequenceFile> {

	/**
	 * Get the list of sequence files in a {@link Sample}
	 * 
	 * @param sample
	 *            The {@link Sample} to read
	 * @return A list of {@link SequenceFile}s
	 */
	public List<SequenceFile> getSequenceFilesForSample(Sample sample);

	/**
	 * Get the {@link SequenceFile}s for a given {@link Sample} that do not have
	 * pairs
	 * 
	 * @param sample
	 *            The {@link Sample} to get files for
	 * @return List of {@link SequenceFile}s
	 */
	List<SequenceFile> getUnpairedSequenceFilesForSample(Sample sample);

}
