package ca.corefacility.bioinformatics.irida.ria.unit.web.projects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockHttpServletResponse;

import ca.corefacility.bioinformatics.irida.exceptions.EntityExistsException;
import ca.corefacility.bioinformatics.irida.model.enums.ProjectRole;
import ca.corefacility.bioinformatics.irida.model.joins.Join;
import ca.corefacility.bioinformatics.irida.model.joins.impl.ProjectSampleJoin;
import ca.corefacility.bioinformatics.irida.model.joins.impl.ProjectUserJoin;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.model.sample.SampleSequencingObjectJoin;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequenceFile;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SingleEndSequenceFile;
import ca.corefacility.bioinformatics.irida.model.user.Role;
import ca.corefacility.bioinformatics.irida.model.user.User;
import ca.corefacility.bioinformatics.irida.ria.unit.TestDataFactory;
import ca.corefacility.bioinformatics.irida.ria.web.projects.ProjectControllerUtils;
import ca.corefacility.bioinformatics.irida.ria.web.projects.ProjectSamplesController;
import ca.corefacility.bioinformatics.irida.service.ProjectService;
import ca.corefacility.bioinformatics.irida.service.SequencingObjectService;
import ca.corefacility.bioinformatics.irida.service.sample.SampleService;
import ca.corefacility.bioinformatics.irida.service.user.UserService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class ProjectSamplesControllerTest {
	public static final String PROJECT_ORGANISM = "E. coli";
	private static final String USER_NAME = "testme";
	private static final User user = new User(USER_NAME, null, null, null, null, null);
	private static final String PROJECT_NAME = "test_project";
	private static final Long PROJECT_ID = 1L;
	private static final Long PROJECT_MODIFIED_DATE = 1403723706L;
	private static Project project = null;
	public static final String FILE_PATH = "src/test/resources/files/test_file.fastq";

	// Services
	private ProjectService projectService;
	private ProjectSamplesController controller;
	private SampleService sampleService;
	private UserService userService;
	private SequencingObjectService sequencingObjectService;
	private MessageSource messageSource;
	private ProjectControllerUtils projectUtils;

	@Before
	public void setUp() {
		projectService = mock(ProjectService.class);
		sampleService = mock(SampleService.class);
		userService = mock(UserService.class);
		sequencingObjectService = mock(SequencingObjectService.class);
		projectUtils = mock(ProjectControllerUtils.class);
		messageSource = mock(MessageSource.class);

		controller = new ProjectSamplesController(projectService, sampleService, userService, sequencingObjectService,
				projectUtils, messageSource);
		user.setId(1L);

		mockSidebarInfo();
	}

	/**
	 * Mocks the information found within the project sidebar.
	 */
	private void mockSidebarInfo() {
		Project project = getProject();
		Collection<Join<Project, User>> ownerList = new ArrayList<>();
		ownerList.add(new ProjectUserJoin(project, user, ProjectRole.PROJECT_OWNER));
		when(userService.getUsersForProjectByRole(any(Project.class), any(ProjectRole.class))).thenReturn(ownerList);
		when(projectService.read(PROJECT_ID)).thenReturn(project);
		when(userService.getUserByUsername(anyString())).thenReturn(user);
	}

	private Project getProject() {
		if (project == null) {
			project = new Project(PROJECT_NAME);
			project.setId(PROJECT_ID);
			project.setOrganism(PROJECT_ORGANISM);
			project.setModifiedDate(new Date(PROJECT_MODIFIED_DATE));
		}
		return project;
	}


	@Test
	public void testCopySampleToProjectRemove() {
		Long projectId = 1L;
		List<Long> sampleIds = ImmutableList.of(2L, 3L);
		Long newProjectId = 4L;
		boolean removeFromOriginal = true;
		Project oldProject = new Project("oldProject");
		Project newProject = new Project("newProject");
		Sample s2 = new Sample("s2");
		Sample s3 = new Sample("s3");

		when(projectService.read(projectId)).thenReturn(oldProject);
		when(projectService.read(newProjectId)).thenReturn(newProject);
		when(sampleService.read(2L)).thenReturn(s2);
		when(sampleService.read(3L)).thenReturn(s3);

		Map<String, Object> result = controller.copySampleToProject(projectId, sampleIds, newProjectId,
				removeFromOriginal, Locale.US);

		assertTrue(result.containsKey("result"));
		assertTrue(result.containsKey("message"));

		verify(projectService).read(projectId);
		verify(projectService).read(newProjectId);
		for (Long x : sampleIds) {
			verify(sampleService).read(x);
		}

		verify(projectService).moveSampleBetweenProjects(oldProject, newProject, s2);
		verify(projectService).moveSampleBetweenProjects(oldProject, newProject, s3);
	}

	@Test
	public void testCopySampleToProject() {
		Long projectId = 1L;
		List<Long> sampleIds = ImmutableList.of(2L, 3L);
		Long newProjectId = 4L;
		boolean removeFromOriginal = false;
		Project oldProject = new Project("oldProject");
		Project newProject = new Project("newProject");
		Sample s2 = new Sample("s2");
		Sample s3 = new Sample("s3");

		when(projectService.read(projectId)).thenReturn(oldProject);
		when(projectService.read(newProjectId)).thenReturn(newProject);
		when(sampleService.read(2L)).thenReturn(s2);
		when(sampleService.read(3L)).thenReturn(s3);

		controller.copySampleToProject(projectId, sampleIds, newProjectId,
				removeFromOriginal, Locale.US);

		verify(projectService).read(projectId);
		verify(projectService).read(newProjectId);
		for (Long x : sampleIds) {
			verify(sampleService).read(x);
		}
		verify(projectService).addSampleToProject(newProject, s2);
		verify(projectService).addSampleToProject(newProject, s3);
		verify(projectService, times(0)).removeSampleFromProject(any(Project.class), any(Sample.class));
	}

	@Test
	public void testCopySampleToProjectSampleExists() {
		Long projectId = 1L;
		List<Long> sampleIds = ImmutableList.of(2L, 3L);
		Long newProjectId = 4L;
		boolean removeFromOriginal = false;
		Project oldProject = new Project("oldProject");
		Project newProject = new Project("newProject");
		Sample s2 = new Sample("s2");
		Sample s3 = new Sample("s3");

		when(projectService.read(projectId)).thenReturn(oldProject);
		when(projectService.read(newProjectId)).thenReturn(newProject);
		when(sampleService.read(2L)).thenReturn(s2);
		when(sampleService.read(3L)).thenReturn(s3);
		when(projectService.addSampleToProject(newProject, s3)).thenThrow(
				new EntityExistsException("that sample exists in the project"));

		Map<String, Object> copySampleToProject = controller.copySampleToProject(projectId, sampleIds, newProjectId,
				removeFromOriginal, Locale.US);

		assertTrue(copySampleToProject.containsKey("warnings"));

		verify(projectService).read(projectId);
		verify(projectService).read(newProjectId);
		for (Long x : sampleIds) {
			verify(sampleService).read(x);
		}
		verify(projectService).addSampleToProject(newProject, s2);
		verify(projectService).addSampleToProject(newProject, s3);
		verify(projectService, times(0)).removeSampleFromProject(any(Project.class), any(Sample.class));
	}

	@Test
	public void testDeleteProjectSamples() {
		Project project1 = getProject();
		Sample sample = new Sample("test");
		sample.setId(1L);
		projectService.addSampleToProject(project1, sample);
		List<Long> idList = new ArrayList<>();
		idList.add(1L);
		when(projectService.read(PROJECT_ID)).thenReturn(project1);
		when(sampleService.read(anyLong())).thenReturn(sample);
		Map<String, Object> result = controller.deleteProjectSamples(PROJECT_ID, idList, Locale.US);
		assertTrue("Result contains the word success", result.containsKey("result"));
		verify(projectService).removeSampleFromProject(project1, sample);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAjaxSamplesMerge() {
		String newName = "FRED";
		Project project = TestDataFactory.constructProject();
		Sample sample1 = new Sample("Wilma");
		sample1.setId(1L);
		sample1.setSampleName(newName);
		Sample sample2 = new Sample("Betty");
		sample2.setId(11L);
		List<Long> sampleIds = new ArrayList<>();
		sampleIds.add(1L);
		sampleIds.add(11L);

		when(sampleService.read(1L)).thenReturn(sample1);
		when(sampleService.read(11L)).thenReturn(sample2);
		when(projectService.read(TestDataFactory.PROJECT_ID)).thenReturn(project);
		when(sampleService.update(anyLong(), anyMap())).thenReturn(sample1);

		// Call the controller with a new name
		Map<String, Object> result = controller.ajaxSamplesMerge(TestDataFactory.PROJECT_ID, 1L, sampleIds, newName, Locale.US);

		// Ensure that the merge was requested
		verify(sampleService, times(1)).mergeSamples(any(Project.class), any(Sample.class), any());

		// Ensure that the rename was not requested
		Map<String, Object> updateMap = new HashMap<>();
		updateMap.put("sampleName", newName);
		verify(sampleService, times(1)).update(1L, updateMap);
		assertEquals("Result is success", result.get("result"), "success");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetProjectsAvailableToCopySamplesAsAdmin() {
		String term = "";
		int page = 0;
		int pagesize = 10;
		Direction order = Direction.ASC;
		String property = "name";

		Principal principal = () -> USER_NAME;
		User puser = new User(USER_NAME, null, null, null, null, null);
		puser.setSystemRole(Role.ROLE_ADMIN);
		Page<Project> projects = new PageImpl<>(Lists.newArrayList(TestDataFactory.constructProject(), TestDataFactory.constructProject()));

		when(userService.getUserByUsername(USER_NAME)).thenReturn(puser);
		when(projectService.search(any(Specification.class), eq(page), eq(pagesize), eq(order), eq(property)))
				.thenReturn(projects);

		Map<String, Object> projectsAvailableToCopySamples = controller.getProjectsAvailableToCopySamples(
				term, pagesize, page, principal);

		assertTrue(projectsAvailableToCopySamples.containsKey("total"));
		assertEquals(2L, projectsAvailableToCopySamples.get("total"));
		assertTrue(projectsAvailableToCopySamples.containsKey("projects"));

		verify(userService).getUserByUsername(USER_NAME);
		verify(projectService).search(any(Specification.class), eq(page), eq(pagesize), eq(order), eq(property));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetProjectsAvailableToCopySamplesAsUser() {
		String term = "";
		int page = 0;
		int pagesize = 10;
		Direction order = Direction.ASC;

		Principal principal = () -> USER_NAME;
		User puser = new User(USER_NAME, null, null, null, null, null);
		puser.setSystemRole(Role.ROLE_USER);
		Page<ProjectUserJoin> projects = new PageImpl<>(Lists.newArrayList(new ProjectUserJoin(TestDataFactory.constructProject(),
				puser, ProjectRole.PROJECT_OWNER), new ProjectUserJoin(TestDataFactory.constructProject(), puser,
				ProjectRole.PROJECT_OWNER)));

		when(userService.getUserByUsername(USER_NAME)).thenReturn(puser);
		when(projectService.searchProjectUsers(any(Specification.class), eq(page), eq(pagesize), eq(order)))
				.thenReturn(projects);

		Map<String, Object> projectsAvailableToCopySamples = controller
				.getProjectsAvailableToCopySamples(term, pagesize, page, principal);

		assertTrue(projectsAvailableToCopySamples.containsKey("total"));
		assertEquals(2L, projectsAvailableToCopySamples.get("total"));
		assertTrue(projectsAvailableToCopySamples.containsKey("projects"));

		verify(userService).getUserByUsername(USER_NAME);
		verify(projectService).searchProjectUsers(any(Specification.class), eq(page), eq(pagesize), eq(order));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testGetAjaxProjectSamplesMap() {
		Project project = TestDataFactory.constructProject();
		Sample sample = TestDataFactory.constructSample();

		when(projectService.read(anyLong())).thenReturn(project);
		when(sampleService.getSamplesForProject(any(Project.class))).thenReturn(ImmutableList.of(
				new ProjectSampleJoin(project, sample)
		));

		Map<String, Object> response = controller.getProjectSamples(1L);

		// Make sure it has the expected keys:
		assertTrue("Has a list of samples", response.containsKey("samples"));

		// Check out the samples
		Object listObject = response.get("samples");
		assertTrue("Samples list really is a list", listObject instanceof List);
		List<HashMap<String, Object>> samplesList = (List<HashMap<String, Object>>) listObject;

		assertEquals("Has the correct number of samples", 1, samplesList.size());
	}

	@Test
	public void testDownloadSamples() throws IOException {
		Project project = TestDataFactory.constructProject();
		Sample sample = TestDataFactory.constructSample();
		MockHttpServletResponse response = new MockHttpServletResponse();

		Path path = Paths.get(FILE_PATH);
		SequenceFile file = new SequenceFile(path);

		ImmutableList<SampleSequencingObjectJoin> filejoin = ImmutableList.of(new SampleSequencingObjectJoin(sample,
				new SingleEndSequenceFile(file)));

		when(projectService.read(project.getId())).thenReturn(project);
		when(sampleService.readMultiple(ImmutableList.of(sample.getId()))).thenReturn(ImmutableList.of(sample));
		when(sequencingObjectService.getSequencingObjectsForSample(sample)).thenReturn(filejoin);

		controller.downloadSamples(project.getId(), ImmutableList.of(sample.getId()), response);

		verify(projectService).read(project.getId());
		verify(sampleService).readMultiple(ImmutableList.of(sample.getId()));
		verify(sequencingObjectService).getSequencingObjectsForSample(sample);

		assertTrue("Response should contain a \"Content-Disposition\" header.",
				response.containsHeader("Content-Disposition"));
		assertEquals("Content-Disposition should include the file name", "attachment; filename=\"test_project.zip\"",
				response.getHeader("Content-Disposition"));

		try (ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(response.getContentAsByteArray()))) {
			ZipEntry nextEntry = zipStream.getNextEntry();
			String fileName = nextEntry.getName();
			assertTrue("incorrect file in zip stream: " + file.getFileName(), fileName.endsWith(file.getFileName()));
		}
	}

	@Test
	public void testDownloadSamplesWithSameName() throws IOException {
		Project project = TestDataFactory.constructProject();
		Sample sample = TestDataFactory.constructSample();
		MockHttpServletResponse response = new MockHttpServletResponse();

		Path path = Paths.get(FILE_PATH);
		SequenceFile file = new SequenceFile(path);

		ImmutableList<SampleSequencingObjectJoin> filejoin = ImmutableList.of(new SampleSequencingObjectJoin(sample,
				new SingleEndSequenceFile(file)), new SampleSequencingObjectJoin(sample,
				new SingleEndSequenceFile(file)), new SampleSequencingObjectJoin(sample,
				new SingleEndSequenceFile(file)));

		when(projectService.read(project.getId())).thenReturn(project);
		when(sampleService.readMultiple(ImmutableList.of(sample.getId()))).thenReturn(ImmutableList.of(sample));
		when(sequencingObjectService.getSequencingObjectsForSample(sample)).thenReturn(filejoin);

		controller.downloadSamples(project.getId(), ImmutableList.of(sample.getId()), response);

		verify(projectService).read(project.getId());
		verify(sampleService).readMultiple(ImmutableList.of(sample.getId()));
		verify(sequencingObjectService).getSequencingObjectsForSample(sample);

		assertTrue("Response should contain a \"Content-Disposition\" header.",
				response.containsHeader("Content-Disposition"));
		assertEquals("Content-Disposition should include the file name", "attachment; filename=\"test_project.zip\"",
				response.getHeader("Content-Disposition"));

		try (ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(response.getContentAsByteArray()))) {
			Set<String> names = new HashSet<>();

			ZipEntry nextEntry = zipStream.getNextEntry();
			while (nextEntry != null) {
				names.add(nextEntry.getName());
				nextEntry = zipStream.getNextEntry();
			}

			assertEquals("should be 3 unique filenames", 3, names.size());
		}

	}
}
