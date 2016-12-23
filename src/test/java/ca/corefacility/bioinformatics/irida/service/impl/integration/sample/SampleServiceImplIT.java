package ca.corefacility.bioinformatics.irida.service.impl.integration.sample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExcecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import ca.corefacility.bioinformatics.irida.config.data.IridaApiJdbcDataSourceConfig;
import ca.corefacility.bioinformatics.irida.config.services.IridaApiServicesConfig;
import ca.corefacility.bioinformatics.irida.exceptions.EntityNotFoundException;
import ca.corefacility.bioinformatics.irida.exceptions.SequenceFileAnalysisException;
import ca.corefacility.bioinformatics.irida.model.joins.impl.ProjectSampleJoin;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.project.ReferenceFile;
import ca.corefacility.bioinformatics.irida.model.sample.QCEntry;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.model.workflow.submission.AnalysisSubmission;
import ca.corefacility.bioinformatics.irida.service.AnalysisSubmissionService;
import ca.corefacility.bioinformatics.irida.service.ProjectService;
import ca.corefacility.bioinformatics.irida.service.SequencingObjectService;
import ca.corefacility.bioinformatics.irida.service.sample.SampleService;

/**
 * Integration tests for the sample service.
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { IridaApiServicesConfig.class,
		IridaApiJdbcDataSourceConfig.class })
@ActiveProfiles("it")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class,
		WithSecurityContextTestExcecutionListener.class })
@DatabaseSetup("/ca/corefacility/bioinformatics/irida/service/impl/SampleServiceImplIT.xml")
@DatabaseTearDown("/ca/corefacility/bioinformatics/irida/test/integration/TableReset.xml")
public class SampleServiceImplIT {

	@Autowired
	private SampleService sampleService;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private SequencingObjectService objectService;
	@Autowired
	private AnalysisSubmissionService analysisSubmissionService;

	/**
	 * Variation in a floating point number to be considered equal.
	 */
	private static final double deltaFloatEquality = 0.000001;

	@Test
	@WithMockUser(username = "fbristow", roles = "ADMIN")
	public void testCreateSample() {
		Sample s = new Sample();
		String sampleName = "sampleName";
		s.setSampleName(sampleName);
		Sample saved = sampleService.create(s);
		assertEquals("Wrong name was saved.", sampleName, saved.getSampleName());
	}

	/**
	 * Straightforward merging of samples all belonging to the same project.
	 */
	@Test
	@WithMockUser(username = "fbristow", roles = "ADMIN")
	public void testMergeSamples() {
		Sample mergeInto = sampleService.read(1L);
		Project p = projectService.read(1L);

		Sample merged = sampleService.mergeSamples(p, mergeInto, sampleService.read(2L), sampleService.read(3L));

		assertEquals("Merged sample should be same as mergeInto.", mergeInto, merged);

		// merged samples should be deleted
		assertSampleNotFound(2L);
		assertSampleNotFound(3L);

		// the merged sample should have 3 sequence files
		assertEquals("Merged sample should have 3 sequence files", 3,
				objectService.getSequencingObjectsForSample(merged).size());
	}

	/**
	 * Sample merging should be rejected when samples are attempted to be joined
	 * where they do not share the same project.
	 */
	@Test(expected = IllegalArgumentException.class)
	@WithMockUser(username = "fbristow", roles = "ADMIN")
	public void testMergeSampleReject() {
		Sample mergeInto = sampleService.read(1L);
		Project p = projectService.read(1L);
		sampleService.mergeSamples(p, mergeInto, sampleService.read(4L));
	}

	@Test
	@WithMockUser(username = "fbristow", roles = "ADMIN")
	@DatabaseSetup("/ca/corefacility/bioinformatics/irida/service/impl/SampleServiceImplIT_duplicateSampleIds.xml")
	public void testGetSampleByExternalIdDuplicates() {
		Project p = projectService.read(7L);
		Sample s = sampleService.getSampleBySampleName(p, "sample");
		assertEquals("Should have retrieved sample with ID 1L.", Long.valueOf(7L), s.getId());
	}

	@WithMockUser(username = "fbristow", roles = "ADMIN")
	@Test(expected = EntityNotFoundException.class)
	public void testgetSampleByExternalNotFound() {
		Project p = projectService.read(1L);
		sampleService.getSampleBySampleName(p, "garbage");
	}

	@Test
	@WithMockUser(username = "fbristow", roles = "SEQUENCER")
	public void testReadSampleByExternalIdAsSequencer() {
		String externalId = "sample5";
		Project p = projectService.read(3L);
		Sample s = sampleService.getSampleBySampleName(p, externalId);

		assertNotNull("Sample was not populated.", s);
		assertEquals("Wrong external id.", externalId, s.getSampleName());
	}

	@Test
	@WithMockUser(username = "fbristow", roles = "SEQUENCER")
	public void testReadSampleAsSequencer() {
		Long sampleID = 1L;
		Sample s = sampleService.read(sampleID);

		assertNotNull("Sample was not populated.", s);
		assertEquals("Wrong external id.", sampleID, s.getId());
	}

	@Test
	@WithMockUser(username = "fbristow", roles = "SEQUENCER")
	public void testGetSampleForProjectAsSequencer() {
		Long sampleID = 2L;
		Long projectID = 1L;
		Project p = projectService.read(projectID);
		Sample s = sampleService.getSampleForProject(p, sampleID);

		assertNotNull("Sample was not populated.", s);
		assertEquals("Wrong external id.", sampleID, s.getId());
	}

	@Test
	@WithMockUser(username = "fbristow", roles = "USER")
	public void testGetSampleForProjectAsUser() {
		Long sampleID = 2L;
		Sample s = sampleService.read(sampleID);

		assertNotNull("Sample was not populated.", s);
		assertEquals("Wrong external id.", sampleID, s.getId());
	}

	@Test(expected = ConstraintViolationException.class)
	@WithMockUser(username = "fbristow", roles = "ADMIN")
	public void testUpdateWithInvalidLatLong() {
		Long sampleId = 2L;
		Map<String, Object> properties = ImmutableMap.of("latitude", "not a geographic latitude", "longitude",
				"not a geographic longitude");

		sampleService.update(sampleId, properties);
	}

	@Test(expected = ConstraintViolationException.class)
	@WithMockUser(username = "fbristow", roles = "ADMIN")
	public void testUpdateWithInvalidRangeLatLong() {
		Long sampleId = 2L;
		Map<String, Object> properties = ImmutableMap.of("latitude", "-1000.00", "longitude", "1000.00");
		sampleService.update(sampleId, properties);
	}

	@Test
	@WithMockUser(username = "fbristow", roles = "ADMIN")
	public void testUpdateLatLong() {
		Long sampleId = 2L;
		String latitude = "50.00";
		String longitude = "-100.00";
		Map<String, Object> properties = ImmutableMap.of("latitude", latitude, "longitude", longitude);
		Sample s = sampleService.update(sampleId, properties);
		assertEquals("Wrong latitude was stored.", latitude, s.getLatitude());
		assertEquals("Wrong longitude was stored.", longitude, s.getLongitude());
	}

	@Test
	@WithMockUser(username = "fbristow", roles = "ADMIN")
	public void testGetSamplesForProjectWithName() {
		int pageSize = 2;
		Project project = projectService.read(1L);
		Page<ProjectSampleJoin> pageSamplesForProject = sampleService.getSamplesForProjectWithName(project, "", 0,
				pageSize, Direction.ASC, "createdDate");
		assertEquals(pageSize, pageSamplesForProject.getNumberOfElements());
		assertEquals(3, pageSamplesForProject.getTotalElements());

		pageSamplesForProject = sampleService.getSamplesForProjectWithName(project, "2", 0, pageSize, Direction.ASC,
				"createdDate");
		assertEquals(1, pageSamplesForProject.getTotalElements());
	}

	/**
	 * Tests getting the total bases for a sample as an admin user.
	 * 
	 * @throws SequenceFileAnalysisException
	 */
	@Test
	@WithMockUser(username = "fbristow", roles = "ADMIN")
	public void testGetBasesForSample() throws SequenceFileAnalysisException {
		Long sampleID = 1L;
		Sample s = sampleService.read(sampleID);

		long bases = sampleService.getTotalBasesForSample(s);
		assertEquals(1000, bases);
	}

	/**
	 * Tests getting the total bases for a sample as a regular user.
	 * 
	 * @throws SequenceFileAnalysisException
	 */
	@Test
	@WithMockUser(username = "fbristow", roles = "USER")
	public void testGetBasesForSampleAsUser() throws SequenceFileAnalysisException {
		Long sampleID = 1L;
		Sample s = sampleService.read(sampleID);

		long bases = sampleService.getTotalBasesForSample(s);
		assertEquals(1000, bases);
	}

	/**
	 * Tests failing to get bases for a sample for a user not on the project.
	 * 
	 * @throws SequenceFileAnalysisException
	 */
	@Test(expected = AccessDeniedException.class)
	@WithMockUser(username = "dr-evil", roles = "USER")
	public void testGetBasesForSampleInvalidUser() throws SequenceFileAnalysisException {
		Sample s = new Sample();
		s.setId(1L);

		sampleService.getTotalBasesForSample(s);
	}

	/**
	 * Tests failing to get coverage for a sample for a user not on the project.
	 * 
	 * @throws SequenceFileAnalysisException
	 */
	@Test(expected = AccessDeniedException.class)
	@WithMockUser(username = "dr-evil", roles = "USER")
	public void testEstimateCoverageForSampleInvalidUser() throws SequenceFileAnalysisException {
		Sample s = new Sample();
		s.setId(1L);

		sampleService.estimateCoverageForSample(s, 500L);
	}

	/**
	 * Tests getting the coverage as a regular user.
	 * 
	 * @throws SequenceFileAnalysisException
	 */
	@Test
	@WithMockUser(username = "fbristow", roles = "USER")
	public void testEstimateCoverageForSampleAsUser() throws SequenceFileAnalysisException {
		Long sampleID = 1L;
		Sample s = sampleService.read(sampleID);

		double coverage = sampleService.estimateCoverageForSample(s, 500);
		assertEquals(2.0, coverage, deltaFloatEquality);
	}

	/**
	 * Tests esimating coverage with a reference file.
	 * 
	 * @throws SequenceFileAnalysisException
	 */
	@Test
	@WithMockUser(username = "fbristow", roles = "USER")
	public void testEstimateCoverageForSampleReferenceFile() throws SequenceFileAnalysisException {
		Long sampleID = 1L;
		Sample s = sampleService.read(sampleID);

		ReferenceFile referenceFile = new ReferenceFile();
		referenceFile.setFileLength(500L);

		double coverage = sampleService.estimateCoverageForSample(s, referenceFile);
		assertEquals(2.0, coverage, deltaFloatEquality);
	}

	/**
	 * Tests failing to get the coverage for a sample with no fastqc results.
	 * 
	 * @throws SequenceFileAnalysisException
	 */
	@Test(expected = SequenceFileAnalysisException.class)
	@WithMockUser(username = "fbristow", roles = "ADMIN")
	public void testEstimateCoverageForSampleNoFastQC() throws SequenceFileAnalysisException {
		Long sampleID = 2L;
		Sample s = sampleService.read(sampleID);

		sampleService.estimateCoverageForSample(s, 500);
	}
	
	@Test
	@WithMockUser(username = "fbristow", roles = "ADMIN")
	public void testGetSampleOrganismForProject(){
		Project p = projectService.read(1L);
		List<String> organisms = sampleService.getSampleOrganismsForProject(p);
		assertEquals("should be 2 organisms", 2, organisms.size());
	}

	@Test
	@WithMockUser(username = "fbristow", roles = "USER")
	public void testGetSamplesForAnalysisSubmission() {
		AnalysisSubmission submission = analysisSubmissionService.read(1L);
		Collection<Sample> samples = sampleService.getSamplesForAnalysisSubimssion(submission);
		
		assertEquals("should be 2 samples", 2, samples.size());
		
		Set<Long> ids = Sets.newHashSet(8L, 9L);
		samples.forEach(s -> ids.remove(s.getId()));
		
		assertTrue("all sample ids should be found", ids.isEmpty());
	}
	
	@Test
	@WithMockUser(username = "fbristow", roles = "ADMIN")
	public void testGetQCEntiresForSample() {
		Sample s = sampleService.read(1L);
		List<QCEntry> qcEntriesForSample = sampleService.getQCEntriesForSample(s);

		assertEquals("should be 1 qc entry", 1L, qcEntriesForSample.size());
	}

	private void assertSampleNotFound(Long id) {
		try {
			sampleService.read(id);
			fail("Merged sample with id [" + id + "] should be deleted.");
		} catch (EntityNotFoundException e) {
		} catch (Exception e) {
			fail("Failed for unknown reason; ");
		}
	}
}
