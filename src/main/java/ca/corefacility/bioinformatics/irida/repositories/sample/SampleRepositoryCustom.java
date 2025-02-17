package ca.corefacility.bioinformatics.irida.repositories.sample;

import java.util.Date;

import ca.corefacility.bioinformatics.irida.model.sample.Sample;

/**
 * Custom repository methods for {@link Sample}s
 */
public interface SampleRepositoryCustom {

	/**
	 * Update the modifiedDate in a {@link Sample} to the specified value.
	 *
	 * @param sample       The {@link Sample} to update
	 * @param modifiedDate The new {@link Date}
	 */
	void updateSampleModifiedDate(Sample sample, Date modifiedDate);
}
