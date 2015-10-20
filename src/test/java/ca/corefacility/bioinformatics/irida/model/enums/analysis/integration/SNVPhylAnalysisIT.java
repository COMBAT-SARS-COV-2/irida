package ca.corefacility.bioinformatics.irida.model.enums.analysis.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExcecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import ca.corefacility.bioinformatics.irida.config.IridaApiGalaxyTestConfig;
import ca.corefacility.bioinformatics.irida.config.conditions.WindowsPlatformCondition;
import ca.corefacility.bioinformatics.irida.model.enums.AnalysisState;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequenceFilePair;
import ca.corefacility.bioinformatics.irida.model.workflow.IridaWorkflow;
import ca.corefacility.bioinformatics.irida.model.workflow.analysis.Analysis;
import ca.corefacility.bioinformatics.irida.model.workflow.analysis.AnalysisPhylogenomicsPipeline;
import ca.corefacility.bioinformatics.irida.model.workflow.analysis.ToolExecution;
import ca.corefacility.bioinformatics.irida.model.workflow.submission.AnalysisSubmission;
import ca.corefacility.bioinformatics.irida.repositories.analysis.submission.AnalysisSubmissionRepository;
import ca.corefacility.bioinformatics.irida.service.AnalysisExecutionScheduledTask;
import ca.corefacility.bioinformatics.irida.service.CleanupAnalysisSubmissionCondition;
import ca.corefacility.bioinformatics.irida.service.DatabaseSetupGalaxyITService;
import ca.corefacility.bioinformatics.irida.service.analysis.execution.AnalysisExecutionService;
import ca.corefacility.bioinformatics.irida.service.impl.AnalysisExecutionScheduledTaskImpl;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Integration tests for SNVPhyl pipeline.
 * 
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { IridaApiGalaxyTestConfig.class })
@ActiveProfiles("test")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class,
		WithSecurityContextTestExcecutionListener.class })
@DatabaseSetup("/ca/corefacility/bioinformatics/irida/model/enums/analysis/integration/SNVPhyl/SNVPhylAnalysisIT.xml")
@DatabaseTearDown("/ca/corefacility/bioinformatics/irida/test/integration/TableReset.xml")
public class SNVPhylAnalysisIT {

	@Autowired
	private DatabaseSetupGalaxyITService databaseSetupGalaxyITService;

	@Autowired
	private AnalysisSubmissionRepository analysisSubmissionRepository;

	@Autowired
	private AnalysisExecutionService analysisExecutionService;

	@Autowired
	private IridaWorkflow snvPhylWorkflow;

	private AnalysisExecutionScheduledTask analysisExecutionScheduledTask;

	private Path sequenceFilePathA1;
	private Path sequenceFilePathA2;
	private Path sequenceFilePathB1;
	private Path sequenceFilePathB2;
	private Path sequenceFilePathC1;
	private Path sequenceFilePathC2;
	private Path referenceFilePath;
	
	private List<Path> sequenceFilePathsA1List;
	private List<Path> sequenceFilePathsA2List;
	private List<Path> sequenceFilePathsB1List;
	private List<Path> sequenceFilePathsB2List;
	private List<Path> sequenceFilePathsC1List;
	private List<Path> sequenceFilePathsC2List;

	private Path outputSnpTable1;
	private Path outputSnpMatrix1;
	private Path vcf2core1;
	private Path filterStats1;
	
	private Path outputSnpTable2;
	private Path outputSnpMatrix2;
	private Path vcf2core2;
	private Path filterStats2;

	/**
	 * Sets up variables for testing.
	 * 
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	@Before
	public void setup() throws URISyntaxException, IOException {
		Assume.assumeFalse(WindowsPlatformCondition.isWindows());

		analysisExecutionScheduledTask = new AnalysisExecutionScheduledTaskImpl(analysisSubmissionRepository,
				analysisExecutionService, CleanupAnalysisSubmissionCondition.NEVER_CLEANUP);
		
		Path tempDir = Files.createTempDirectory("snvphylTest");

		Path sequenceFilePathRealA1 = Paths.get(SNVPhylAnalysisIT.class.getResource(
				"SNVPhyl/test1/input/fastq/a_1.fastq").toURI());
		Path sequenceFilePathRealA2 = Paths.get(SNVPhylAnalysisIT.class.getResource(
				"SNVPhyl/test1/input/fastq/a_2.fastq").toURI());
		Path sequenceFilePathRealB1 = Paths.get(SNVPhylAnalysisIT.class.getResource(
				"SNVPhyl/test1/input/fastq/b_1.fastq").toURI());
		Path sequenceFilePathRealB2 = Paths.get(SNVPhylAnalysisIT.class.getResource(
				"SNVPhyl/test1/input/fastq/b_2.fastq").toURI());
		Path sequenceFilePathRealC1 = Paths.get(SNVPhylAnalysisIT.class.getResource(
				"SNVPhyl/test1/input/fastq/c_1.fastq").toURI());
		Path sequenceFilePathRealC2 = Paths.get(SNVPhylAnalysisIT.class.getResource(
				"SNVPhyl/test1/input/fastq/c_2.fastq").toURI());
		Path referenceFilePathReal = Paths.get(SNVPhylAnalysisIT.class.getResource(
				"SNVPhyl/test1/input/reference.fasta").toURI());

		sequenceFilePathA1 = tempDir.resolve("a_R1_001.fastq");
		Files.copy(sequenceFilePathRealA1, sequenceFilePathA1, StandardCopyOption.REPLACE_EXISTING);
		sequenceFilePathA2 = tempDir.resolve("a_R2_001.fastq");
		Files.copy(sequenceFilePathRealA2, sequenceFilePathA2, StandardCopyOption.REPLACE_EXISTING);

		sequenceFilePathB1 = tempDir.resolve("b_R1_001.fastq");
		Files.copy(sequenceFilePathRealB1, sequenceFilePathB1, StandardCopyOption.REPLACE_EXISTING);
		sequenceFilePathB2 = tempDir.resolve("b_R2_001.fastq");
		Files.copy(sequenceFilePathRealB2, sequenceFilePathB2, StandardCopyOption.REPLACE_EXISTING);

		sequenceFilePathC1 = tempDir.resolve("c_R1_001.fastq");
		Files.copy(sequenceFilePathRealC1, sequenceFilePathC1, StandardCopyOption.REPLACE_EXISTING);
		sequenceFilePathC2 = tempDir.resolve("c_R2_001.fastq");
		Files.copy(sequenceFilePathRealC2, sequenceFilePathC2, StandardCopyOption.REPLACE_EXISTING);

		sequenceFilePathsA1List = new LinkedList<>();
		sequenceFilePathsA1List.add(sequenceFilePathA1);
		sequenceFilePathsA2List = new LinkedList<>();
		sequenceFilePathsA2List.add(sequenceFilePathA2);

		sequenceFilePathsB1List = new LinkedList<>();
		sequenceFilePathsB1List.add(sequenceFilePathB1);
		sequenceFilePathsB2List = new LinkedList<>();
		sequenceFilePathsB2List.add(sequenceFilePathB2);

		sequenceFilePathsC1List = new LinkedList<>();
		sequenceFilePathsC1List.add(sequenceFilePathC1);
		sequenceFilePathsC2List = new LinkedList<>();
		sequenceFilePathsC2List.add(sequenceFilePathC2);

		referenceFilePath = Files.createTempFile("reference", ".fasta");
		Files.copy(referenceFilePathReal, referenceFilePath, StandardCopyOption.REPLACE_EXISTING);

		outputSnpTable1 = Paths.get(SNVPhylAnalysisIT.class.getResource("SNVPhyl/test1/output1/snpTable.tsv").toURI());
		outputSnpMatrix1 = Paths.get(SNVPhylAnalysisIT.class.getResource("SNVPhyl/test1/output1/snpMatrix.tsv").toURI());
		vcf2core1 = Paths.get(SNVPhylAnalysisIT.class.getResource("SNVPhyl/test1/output1/vcf2core.csv").toURI());
		filterStats1 = Paths.get(SNVPhylAnalysisIT.class.getResource("SNVPhyl/test1/output1/filterStats.txt").toURI());
		
		outputSnpTable2 = Paths.get(SNVPhylAnalysisIT.class.getResource("SNVPhyl/test1/output2/snpTable.tsv").toURI());
		outputSnpMatrix2 = Paths.get(SNVPhylAnalysisIT.class.getResource("SNVPhyl/test1/output2/snpMatrix.tsv").toURI());
		vcf2core2 = Paths.get(SNVPhylAnalysisIT.class.getResource("SNVPhyl/test1/output2/vcf2core.csv").toURI());
		filterStats2 = Paths.get(SNVPhylAnalysisIT.class.getResource("SNVPhyl/test1/output2/filterStats.txt").toURI());
	}

	private void waitUntilAnalysisStageComplete(Set<Future<AnalysisSubmission>> submissionsFutureSet)
			throws InterruptedException, ExecutionException {
		for (Future<AnalysisSubmission> submissionFuture : submissionsFutureSet) {
			submissionFuture.get();
		}
	}

	private void completeSubmittedAnalyses(Long submissionId) throws Exception {
		waitUntilAnalysisStageComplete(analysisExecutionScheduledTask.downloadFiles());
		waitUntilAnalysisStageComplete(analysisExecutionScheduledTask.prepareAnalyses());
		waitUntilAnalysisStageComplete(analysisExecutionScheduledTask.executeAnalyses());

		AnalysisSubmission submission = analysisSubmissionRepository.findOne(submissionId);

		databaseSetupGalaxyITService.waitUntilSubmissionComplete(submission);
		waitUntilAnalysisStageComplete(analysisExecutionScheduledTask.monitorRunningAnalyses());
		waitUntilAnalysisStageComplete(analysisExecutionScheduledTask.transferAnalysesResults());
	}

	/**
	 * Tests out successfully executing the SNVPhyl pipeline.
	 * 
	 * @throws Exception
	 */
	@Test
	@WithMockUser(username = "aaron", roles = "ADMIN")
	public void testSNVPhylSuccess() throws Exception {
		SequenceFilePair sequenceFilePairA = databaseSetupGalaxyITService.setupSampleSequenceFileInDatabase(1L,
				sequenceFilePathsA1List, sequenceFilePathsA2List).get(0);
		SequenceFilePair sequenceFilePairB = databaseSetupGalaxyITService.setupSampleSequenceFileInDatabase(2L,
				sequenceFilePathsB1List, sequenceFilePathsB2List).get(0);
		SequenceFilePair sequenceFilePairC = databaseSetupGalaxyITService.setupSampleSequenceFileInDatabase(3L,
				sequenceFilePathsC1List, sequenceFilePathsC2List).get(0);
		
		Map<String,String> parameters = ImmutableMap.of("alternative-allele-fraction", "0.75", "minimum-read-coverage", "2");

		AnalysisSubmission submission = databaseSetupGalaxyITService.setupPairSubmissionInDatabase(
				Sets.newHashSet(sequenceFilePairA, sequenceFilePairB, sequenceFilePairC), referenceFilePath,
				parameters, snvPhylWorkflow.getWorkflowIdentifier());
		
		completeSubmittedAnalyses(submission.getId());

		submission = analysisSubmissionRepository.findOne(submission.getId());
		assertEquals("analysis state should be completed.", AnalysisState.COMPLETED, submission.getAnalysisState());

		Analysis analysis = submission.getAnalysis();
		assertEquals("Should have generated a phylogenomics pipeline analysis type.",
				AnalysisPhylogenomicsPipeline.class, analysis.getClass());
		AnalysisPhylogenomicsPipeline analysisPhylogenomics = (AnalysisPhylogenomicsPipeline) analysis;

		assertEquals("the phylogenomics pipeline should have 7 output files.", 7, analysisPhylogenomics
				.getAnalysisOutputFiles().size());
		
		@SuppressWarnings("resource")
		String matrixContent = new Scanner(analysisPhylogenomics.getSnpMatrix().getFile().toFile()).useDelimiter("\\Z")
				.next();
		assertTrue(
				"snpMatrix should be the same but is \"" + matrixContent + "\"",
				com.google.common.io.Files.equal(outputSnpMatrix1.toFile(), analysisPhylogenomics.getSnpMatrix()
						.getFile().toFile()));
		assertNotNull("file should have tool provenance attached.", analysisPhylogenomics.getSnpMatrix()
				.getCreatedByTool());
		
		@SuppressWarnings("resource")
		String snpTableContent = new Scanner(analysisPhylogenomics.getSnpTable().getFile().toFile()).useDelimiter(
				"\\Z").next();
		assertTrue(
				"snpTable should be the same but is \"" + snpTableContent + "\"",
				com.google.common.io.Files.equal(outputSnpTable1.toFile(), analysisPhylogenomics.getSnpTable().getFile()
						.toFile()));
		assertNotNull("file should have tool provenance attached.", analysisPhylogenomics.getSnpTable()
				.getCreatedByTool());
		
		@SuppressWarnings("resource")
		String vcf2coreContent = new Scanner(analysisPhylogenomics.getCoreGenomeLog().getFile().toFile()).useDelimiter(
				"\\Z").next();
		assertTrue(
				"vcf2core should be the same but is \"" + vcf2coreContent + "\"",
				com.google.common.io.Files.equal(vcf2core1.toFile(), analysisPhylogenomics.getCoreGenomeLog().getFile()
						.toFile()));
		assertNotNull("file should have tool provenance attached.", analysisPhylogenomics.getCoreGenomeLog()
				.getCreatedByTool());
		
		// only check size of mapping quality file due to samples output in random order
		assertTrue("the mapping quality file should not be empty.",
				Files.size(analysisPhylogenomics.getMappingQuality().getFile()) > 0);
		
		@SuppressWarnings("resource")
		String filterStatsContent = new Scanner(analysisPhylogenomics.getFilterStats().getFile().toFile()).useDelimiter(
				"\\Z").next();
		assertTrue(
				"filterStats should be the same but is \"" + filterStatsContent + "\"",
				com.google.common.io.Files.equal(filterStats1.toFile(), analysisPhylogenomics.getFilterStats().getFile()
						.toFile()));
		assertNotNull("file should have tool provenance attached.", analysisPhylogenomics.getFilterStats()
				.getCreatedByTool());
		
		// only test to make sure the files have a valid size since PhyML uses a
		// random seed to generate the tree (and so changes results)
		assertTrue("the phylogenetic tree file should not be empty.",
				Files.size(analysisPhylogenomics.getPhylogeneticTree().getFile()) > 0);
		assertTrue("the phylogenetic tree stats file should not be empty.",
				Files.size(analysisPhylogenomics.getPhylogeneticTreeStats().getFile()) > 0);

		// try to follow the phylogenomics provenance all the way back to the
		// upload tools
		final List<ToolExecution> toolsToVisit = Lists.newArrayList(analysisPhylogenomics.getPhylogeneticTree()
				.getCreatedByTool());
		assertFalse("file should have tool provenance attached.", toolsToVisit.isEmpty());

		boolean foundReadsInputTool = false;
		boolean foundReferenceInputTool = false;

		// navigate through the tree to make sure that you can find both types
		// of input tools: the one where you upload the reference file, and the
		// one where you upload the reads.
		while (!toolsToVisit.isEmpty()) {
			final ToolExecution ex = toolsToVisit.remove(0);
			toolsToVisit.addAll(ex.getPreviousSteps());

			if (ex.isInputTool()) {
				final Map<String, String> params = ex.getExecutionTimeParameters();
				foundReferenceInputTool |= params.get("files.NAME").contains("reference")
						&& params.get("file_type").contains("fasta");
				foundReadsInputTool |= params.get("file_type").contains("fastq");
			}
		}

		assertTrue("Should have found both reads and reference input tools.", foundReadsInputTool
				&& foundReferenceInputTool);
	}
	
	/**
	 * Tests out successfully executing the SNVPhyl pipeline and passing a higher value for fraction of reads to call a SNP.
	 * 
	 * @throws Exception
	 */
	@Test
	@WithMockUser(username = "aaron", roles = "ADMIN")
	public void testSNVPhylSuccessHigherSNVReadProportion() throws Exception {
		SequenceFilePair sequenceFilePairA = databaseSetupGalaxyITService.setupSampleSequenceFileInDatabase(1L,
				sequenceFilePathsA1List, sequenceFilePathsA2List).get(0);
		SequenceFilePair sequenceFilePairB = databaseSetupGalaxyITService.setupSampleSequenceFileInDatabase(2L,
				sequenceFilePathsB1List, sequenceFilePathsB2List).get(0);
		SequenceFilePair sequenceFilePairC = databaseSetupGalaxyITService.setupSampleSequenceFileInDatabase(3L,
				sequenceFilePathsC1List, sequenceFilePathsC2List).get(0);

		Map<String,String> parameters = ImmutableMap.of("alternative-allele-fraction", "0.90", "minimum-read-coverage", "2",
				"minimum-percent-coverage", "75", "minimum-mean-mapping-quality", "20");
		
		AnalysisSubmission submission = databaseSetupGalaxyITService.setupPairSubmissionInDatabase(
				Sets.newHashSet(sequenceFilePairA, sequenceFilePairB, sequenceFilePairC), referenceFilePath,
				parameters, snvPhylWorkflow.getWorkflowIdentifier());

		completeSubmittedAnalyses(submission.getId());

		submission = analysisSubmissionRepository.findOne(submission.getId());
		assertEquals("analysis state should be completed.", AnalysisState.COMPLETED, submission.getAnalysisState());

		Analysis analysis = submission.getAnalysis();
		assertEquals("Should have generated a phylogenomics pipeline analysis type.",
				AnalysisPhylogenomicsPipeline.class, analysis.getClass());
		AnalysisPhylogenomicsPipeline analysisPhylogenomics = (AnalysisPhylogenomicsPipeline) analysis;

		assertEquals("the phylogenomics pipeline should have 7 output files.", 7, analysisPhylogenomics
				.getAnalysisOutputFiles().size());
		
		@SuppressWarnings("resource")
		String matrixContent = new Scanner(analysisPhylogenomics.getSnpMatrix().getFile().toFile()).useDelimiter("\\Z")
				.next();
		assertTrue(
				"snpMatrix should be the same but is \"" + matrixContent + "\"",
				com.google.common.io.Files.equal(outputSnpMatrix2.toFile(), analysisPhylogenomics.getSnpMatrix()
						.getFile().toFile()));
		assertNotNull("file should have tool provenance attached.", analysisPhylogenomics.getSnpMatrix()
				.getCreatedByTool());
		
		@SuppressWarnings("resource")
		String snpTableContent = new Scanner(analysisPhylogenomics.getSnpTable().getFile().toFile()).useDelimiter(
				"\\Z").next();
		assertTrue(
				"snpTable should be the same but is \"" + snpTableContent + "\"",
				com.google.common.io.Files.equal(outputSnpTable2.toFile(), analysisPhylogenomics.getSnpTable().getFile()
						.toFile()));
		assertNotNull("file should have tool provenance attached.", analysisPhylogenomics.getSnpTable()
				.getCreatedByTool());
		
		@SuppressWarnings("resource")
		String vcf2coreContent = new Scanner(analysisPhylogenomics.getCoreGenomeLog().getFile().toFile()).useDelimiter(
				"\\Z").next();
		assertTrue(
				"vcf2core should be the same but is \"" + vcf2coreContent + "\"",
				com.google.common.io.Files.equal(vcf2core2.toFile(), analysisPhylogenomics.getCoreGenomeLog().getFile()
						.toFile()));
		assertNotNull("file should have tool provenance attached.", analysisPhylogenomics.getCoreGenomeLog()
				.getCreatedByTool());
		
		// only check size of mapping quality file due to samples output in random order
		assertTrue("the mapping quality file should not be empty.",
				Files.size(analysisPhylogenomics.getMappingQuality().getFile()) > 0);
		
		@SuppressWarnings("resource")
		String filterStatsContent = new Scanner(analysisPhylogenomics.getFilterStats().getFile().toFile()).useDelimiter(
				"\\Z").next();
		assertTrue(
				"filterStats should be the same but is \"" + filterStatsContent + "\"",
				com.google.common.io.Files.equal(filterStats2.toFile(), analysisPhylogenomics.getFilterStats().getFile()
						.toFile()));
		assertNotNull("file should have tool provenance attached.", analysisPhylogenomics.getFilterStats()
				.getCreatedByTool());
		
		// only test to make sure the files have a valid size since PhyML uses a
		// random seed to generate the tree (and so changes results)
		assertTrue("the phylogenetic tree file should not be empty.",
				Files.size(analysisPhylogenomics.getPhylogeneticTree().getFile()) > 0);
		assertTrue("the phylogenetic tree stats file should not be empty.",
				Files.size(analysisPhylogenomics.getPhylogeneticTreeStats().getFile()) > 0);

		// try to follow the phylogenomics provenance all the way back to the
		// upload tools
		List<ToolExecution> toolsToVisit = Lists.newArrayList(analysisPhylogenomics.getPhylogeneticTree()
				.getCreatedByTool());
		assertFalse("file should have tool provenance attached.", toolsToVisit.isEmpty());

		String minimumFreebayesCoverage = null;
		String altAlleleFraction = null;
		String minimumPercentCoverage = null;
		String minimumDepthVerify = null;
		
		// navigate through the tree to make sure that you can find both types
		// of input tools: the one where you upload the reference file, and the
		// one where you upload the reads.
		while (!toolsToVisit.isEmpty()) {
			final ToolExecution ex = toolsToVisit.remove(0);
			toolsToVisit.addAll(ex.getPreviousSteps());

			if (ex.getToolName().contains("FreeBayes")) {
				final Map<String, String> params = ex.getExecutionTimeParameters();
				minimumFreebayesCoverage = params.get("options_type.section_input_filters_type.min_coverage");
				altAlleleFraction = params.get("options_type.section_input_filters_type.min_alternate_fraction");
				break;
			}
		}
		
		// try to follow the mapping quality provenance all the way back to the
		// upload tools
		toolsToVisit = Lists.newArrayList(analysisPhylogenomics.getMappingQuality()
				.getCreatedByTool());
		assertFalse("file should have tool provenance attached.", toolsToVisit.isEmpty());
		
		while (!toolsToVisit.isEmpty()) {
			final ToolExecution ex = toolsToVisit.remove(0);
			toolsToVisit.addAll(ex.getPreviousSteps());
			
			if (ex.getToolName().contains("Verify Mapping Quality")) {
				final Map<String, String> params = ex.getExecutionTimeParameters();
				minimumPercentCoverage = params.get("minmap");
				minimumDepthVerify = params.get("mindepth");
			}
		}
		
		assertEquals("incorrect minimum freebayes coverage", "2", minimumFreebayesCoverage);
		assertEquals("incorrect alternative allele fraction", "0.9", altAlleleFraction);
		assertEquals("incorrect minimum depth for verify map", "\"2\"", minimumDepthVerify);
		assertEquals("incorrect min percent coverage for verify map", "\"75\"", minimumPercentCoverage);
	}
}
