package ca.corefacility.bioinformatics.irida.ria.unit.web.files;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;

import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.project.ProjectReferenceFileJoin;
import ca.corefacility.bioinformatics.irida.model.project.ReferenceFile;
import ca.corefacility.bioinformatics.irida.ria.web.files.ReferenceFileController;
import ca.corefacility.bioinformatics.irida.service.ProjectService;
import ca.corefacility.bioinformatics.irida.service.ReferenceFileService;

/**
 * Unit Tests for @{link ReferenceFileController}
 *
 * @author Josh Adam <josh.adam@phac-aspc.gc.ca>
 */
public class ReferenceFileControllerTest {
	// Constants
	public static final Long FILE_ID = 1l;
	public static final Long BAD_FILE_ID = 2l;
	public static final String FILE_NAME = "test_file.fastq";
	public static final String FILE_PATH = "src/test/resources/files/test_file.fastq";
	public static final Long PROJECT_ID = 11L;
	private static final Logger logger = LoggerFactory.getLogger(ReferenceFileControllerTest.class);
	// Controller
	private ReferenceFileController controller;

	// Services
	private ProjectService projectService;
	private ReferenceFileService referenceFileService;

	@Before
	public void setUp() {
		projectService = mock(ProjectService.class);
		referenceFileService = mock(ReferenceFileService.class);

		// Set up the reference file
		Path path = Paths.get(FILE_PATH);
		ReferenceFile file = new ReferenceFile(path);
		when(referenceFileService.read(FILE_ID)).thenReturn(file);

		controller = new ReferenceFileController(projectService, referenceFileService);
	}

	@Test
	public void testCreateNewReferenceFile() throws IOException {
		Path path = Paths.get(FILE_PATH);
		byte[] origBytes = Files.readAllBytes(path);
		MockMultipartFile mockMultipartFile = new MockMultipartFile(FILE_NAME, FILE_NAME, "octet-stream",origBytes);
		Project project = new Project("foo"); // TODO: (14-08-14 - Josh) look at Franklin's TestDataFactory
		ReferenceFile referenceFile = new ReferenceFile(path);

		when(projectService.read(PROJECT_ID)).thenReturn(project);
		when(projectService.addReferenceFileToProject(eq(project), any(ReferenceFile.class)))
				.thenReturn(new ProjectReferenceFileJoin(project, referenceFile));

		Map<String, Long> result = controller.createNewReferenceFile(PROJECT_ID, mockMultipartFile);

		assertTrue("Should contain the id of the new sequence file", result.containsKey("id"));

		verify(projectService).read(PROJECT_ID);
		verify(projectService).addReferenceFileToProject(eq(project), any(ReferenceFile.class));
	}

	/***********************************************************************************************
	 * AJAX TESTS
	 ***********************************************************************************************/

	@Test
	public void testDownloadReferenceFile() throws IOException {
		logger.debug("Testing download reference file");
		MockHttpServletResponse response = new MockHttpServletResponse();

		controller.downloadReferenceFile(FILE_ID, response);
		assertTrue("Response should contain a \"Content-Disposition\" header.",
				response.containsHeader("Content-Disposition"));
		assertEquals("Content-Disposition should include the file name", "attachment; filename=\"test_file.fastq\"",
				response.getHeader("Content-Disposition"));

		Path path = Paths.get(FILE_PATH);
		byte[] origBytes = Files.readAllBytes(path);
		byte[] responseBytes = response.getContentAsByteArray();
		assertArrayEquals("Response contents the correct file content", origBytes, responseBytes);
	}
}
