package ca.corefacility.bioinformatics.irida.service.export;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Date;

import javax.validation.Validator;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import ca.corefacility.bioinformatics.irida.model.NcbiExportSubmission;
import ca.corefacility.bioinformatics.irida.model.export.NcbiBioSampleFiles;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequenceFile;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequenceFilePair;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SingleEndSequenceFile;
import ca.corefacility.bioinformatics.irida.repositories.NcbiExportSubmissionRepository;
import ca.corefacility.bioinformatics.irida.service.impl.export.NcbiExportSubmissionServiceImpl;

/**
 * Test for {@link NcbiExportSubmissionService}
 */
public class NcbiExportSubmissionServceTest {

	NcbiExportSubmissionService service;
	NcbiExportSubmissionRepository repository;
	Validator validator;

	@Before
	public void setup() {
		validator = mock(Validator.class);
		repository = mock(NcbiExportSubmissionRepository.class);

		service = new NcbiExportSubmissionServiceImpl(repository, validator);
	}

	@Test
	public void testCreate() {
		SingleEndSequenceFile sequenceFile = new SingleEndSequenceFile(new SequenceFile());

		NcbiBioSampleFiles ncbiBioSampleFiles = new NcbiBioSampleFiles("sample", Lists.newArrayList(sequenceFile),
				Lists.newArrayList(), null, "library_name", null, null, null, "library_construction_protocol",
				"namespace");
		NcbiExportSubmission submission = new NcbiExportSubmission(null, null, "bioProjectId", "organization",
				"ncbiNamespace", new Date(), Lists.newArrayList(ncbiBioSampleFiles));

		service.create(submission);

		verify(repository).save(submission);
	}

	@Test
	public void testCreatePairs() {
		SequenceFile sequenceFile = new SequenceFile();

		NcbiBioSampleFiles ncbiBioSampleFiles = new NcbiBioSampleFiles("sample", Lists.newArrayList(),
				Lists.newArrayList(new SequenceFilePair(sequenceFile, sequenceFile)), null, "library_name", null, null,
				null, "library_construction_protocol", "namespace");
		NcbiExportSubmission submission = new NcbiExportSubmission(null, null, "bioProjectId", "organization",
				"ncbiNamespace", new Date(), Lists.newArrayList(ncbiBioSampleFiles));

		service.create(submission);

		verify(repository).save(submission);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateNoFiles() {

		NcbiBioSampleFiles ncbiBioSampleFiles = new NcbiBioSampleFiles("sample", Lists.newArrayList(),
				Lists.newArrayList(), null, "library_name", null, null, null, "library_construction_protocol",
				"namespace");
		NcbiExportSubmission submission = new NcbiExportSubmission(null, null, "bioProjectId", "organization",
				"ncbiNamespace", new Date(), Lists.newArrayList(ncbiBioSampleFiles));

		service.create(submission);
	}
}
