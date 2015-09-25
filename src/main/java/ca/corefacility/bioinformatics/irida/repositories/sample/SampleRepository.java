package ca.corefacility.bioinformatics.irida.repositories.sample;

import org.springframework.data.jpa.repository.Query;

import ca.corefacility.bioinformatics.irida.exceptions.EntityNotFoundException;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.repositories.IridaJpaRepository;

/**
 * A repository for storing Sample objects
 * 
 */
public interface SampleRepository extends IridaJpaRepository<Sample, Long> {
	/**
	 * Get a {@link Sample} with the given string sample identifier from a
	 * specific project.
	 * 
	 * @param p
	 *            The {@link Project} that the {@link Sample} belongs to.
	 * @param sampleName
	 *            The string sample name for a sample
	 * @return The {@link Sample} for this identifier
	 * @throws EntityNotFoundException
	 *             if a sample with this identifier doesn't exist
	 */
	@Query("select j.sample from ProjectSampleJoin j where j.project = ?1 and j.sample.sampleName = ?2")
	public Sample getSampleBySampleName(Project p, String sampleName) throws EntityNotFoundException;
}
