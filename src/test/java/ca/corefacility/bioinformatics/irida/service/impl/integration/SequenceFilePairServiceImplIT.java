package ca.corefacility.bioinformatics.irida.service.impl.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExcecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import ca.corefacility.bioinformatics.irida.config.IridaApiNoGalaxyTestConfig;
import ca.corefacility.bioinformatics.irida.config.data.IridaApiTestDataSourceConfig;
import ca.corefacility.bioinformatics.irida.config.processing.IridaApiTestMultithreadingConfig;
import ca.corefacility.bioinformatics.irida.config.services.IridaApiServicesConfig;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.model.sample.SampleSequencingObjectJoin;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequenceFile;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequenceFilePair;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequencingObject;
import ca.corefacility.bioinformatics.irida.service.SequenceFileService;
import ca.corefacility.bioinformatics.irida.service.SequencingObjectService;
import ca.corefacility.bioinformatics.irida.service.sample.SampleService;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.google.common.collect.Sets;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { IridaApiServicesConfig.class,
		IridaApiNoGalaxyTestConfig.class, IridaApiTestDataSourceConfig.class, IridaApiTestMultithreadingConfig.class })
@ActiveProfiles("test")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class,
		WithSecurityContextTestExcecutionListener.class })
@DatabaseSetup("/ca/corefacility/bioinformatics/irida/service/impl/SequenceFileServiceImplIT.xml")
@DatabaseTearDown(value = "/ca/corefacility/bioinformatics/irida/test/integration/TableReset.xml", type = DatabaseOperation.DELETE_ALL)
public class SequenceFilePairServiceImplIT {
	@Autowired
	private SampleService sampleService;

	@Autowired
	private SequenceFileService sequenceFileService;

	@Autowired
	private SequencingObjectService objectService;

	@Test(expected = DataIntegrityViolationException.class)
	@WithMockUser(username = "fbristow", roles = "SEQUENCER")
	public void testAddSequenceFileWithExistingPair() {
		SequenceFile file1 = sequenceFileService.read(1L);
		SequenceFile file3 = sequenceFileService.read(3L);

		SequenceFilePair sequenceFilePair = new SequenceFilePair(file1, file3);
		objectService.createWithoutFileProcessor(sequenceFilePair);
	}

	@Test
	@WithMockUser(username = "fbristow", roles = "SEQUENCER")
	public void testAddSequenceFilePairAsSequencer() {
		SequenceFile file1 = sequenceFileService.read(1L);
		SequenceFile file2 = sequenceFileService.read(2L);

		SequenceFilePair sequenceFilePair = new SequenceFilePair(file1, file2);

		SequencingObject createSequenceFilePair = objectService.createWithoutFileProcessor(sequenceFilePair);
		assertTrue(createSequenceFilePair.getFiles().contains(file1));
		assertTrue(createSequenceFilePair.getFiles().contains(file2));
	}

	@Test
	@WithMockUser(username = "admin", roles = "ADMIN")
	public void testGetSequenceFilePairForSample() {
		Sample s = sampleService.read(2L);

		Set<Long> fileIds = Sets.newHashSet(3L, 4L);

		Collection<SampleSequencingObjectJoin> sequenceFilePairsForSample = objectService.getSequencesForSampleOfType(
				s, SequenceFilePair.class);
		assertEquals(1, sequenceFilePairsForSample.size());
		SequencingObject pair = sequenceFilePairsForSample.iterator().next().getObject();

		for (SequenceFile file : pair.getFiles()) {
			assertTrue("file id should be in set", fileIds.contains(file.getId()));
			fileIds.remove(file.getId());
		}

		assertTrue("all file ids should have been found", fileIds.isEmpty());
	}
}
