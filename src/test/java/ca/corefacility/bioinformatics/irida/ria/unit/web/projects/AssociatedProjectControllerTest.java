package ca.corefacility.bioinformatics.irida.ria.unit.web.projects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.hateoas.Link;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import ca.corefacility.bioinformatics.irida.model.RemoteAPI;
import ca.corefacility.bioinformatics.irida.model.joins.impl.RelatedProjectJoin;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.remote.RemoteRelatedProject;
import ca.corefacility.bioinformatics.irida.model.user.Role;
import ca.corefacility.bioinformatics.irida.model.user.User;
import ca.corefacility.bioinformatics.irida.ria.web.projects.AssociatedProjectsController;
import ca.corefacility.bioinformatics.irida.ria.web.projects.ProjectControllerUtils;
import ca.corefacility.bioinformatics.irida.service.ProjectService;
import ca.corefacility.bioinformatics.irida.service.RemoteAPIService;
import ca.corefacility.bioinformatics.irida.service.RemoteRelatedProjectService;
import ca.corefacility.bioinformatics.irida.service.remote.ProjectRemoteService;
import ca.corefacility.bioinformatics.irida.service.user.UserService;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class AssociatedProjectControllerTest {
	private static final String USER_NAME = "testme";

	private ProjectService projectService;
	private AssociatedProjectsController controller;
	private UserService userService;
	private ProjectControllerUtils projectUtils;
	private RemoteRelatedProjectService remoteRelatedProjectService;
	private RemoteAPIService apiService;
	private ProjectRemoteService projectRemoteService;
	private MessageSource messageSource;

	@Before
	public void setUp() {
		projectService = mock(ProjectService.class);
		userService = mock(UserService.class);
		projectUtils = mock(ProjectControllerUtils.class);
		apiService = mock(RemoteAPIService.class);
		projectRemoteService = mock(ProjectRemoteService.class);
		remoteRelatedProjectService = mock(RemoteRelatedProjectService.class);
		messageSource = mock(MessageSource.class);
		controller = new AssociatedProjectsController(remoteRelatedProjectService, projectService, projectUtils,
				userService, apiService, projectRemoteService, messageSource);
		
        // fake out the servlet response so that the URI builder will work.
        RequestAttributes ra = new ServletRequestAttributes(new MockHttpServletRequest());
        RequestContextHolder.setRequestAttributes(ra);
	}

	@Test
	public void testGetAssociatedProjectsPage() {

		ExtendedModelMap model = new ExtendedModelMap();
		Principal principal = () -> USER_NAME;
		Long projectId = 1L;
		User u = new User();
		u.setSystemRole(Role.ROLE_ADMIN);
		Project p = new Project("my project");
		p.setId(projectId);
		Project o = new Project("other project");
		o.setId(2L);
		List<RelatedProjectJoin> relatedProjects = Lists.newArrayList(new RelatedProjectJoin(p, o));

		RemoteAPI remoteAPI = new RemoteAPI();
		List<RemoteRelatedProject> remoteRelatedProjects = Lists.newArrayList(new RemoteRelatedProject(p, remoteAPI,
				"http://somewhere"));

		when(projectService.read(projectId)).thenReturn(p);

		when(userService.getUserByUsername(USER_NAME)).thenReturn(u);
		when(projectService.getRelatedProjects(p)).thenReturn(relatedProjects);
		when(remoteRelatedProjectService.getRemoteProjectsForProject(p)).thenReturn(remoteRelatedProjects);

		controller.getAssociatedProjectsPage(projectId, model, principal);

		assertTrue(model.containsAttribute("isAdmin"));
		assertTrue(model.containsAttribute("associatedProjects"));
		assertTrue(model.containsAttribute("remoteProjectsByApi"));

		verify(projectService).read(projectId);
		verify(userService, times(2)).getUserByUsername(USER_NAME);
		verify(projectService).getRelatedProjects(p);
		verify(remoteRelatedProjectService).getRemoteProjectsForProject(p);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetPotentialAssociatedProjectsAsAdmin() {
		Long projectId = 1L;
		Principal principal = () -> USER_NAME;
		int page = 1;
		int count = 10;
		String sortedBy = "id";
		String sortDir = "asc";
		String projectName = "";

		Project p1 = new Project("p1");
		when(projectService.read(projectId)).thenReturn(p1);

		User user = new User();
		user.setSystemRole(Role.ROLE_ADMIN);
		when(userService.getUserByUsername(USER_NAME)).thenReturn(user);
		// (specification, page, count, sortDirection, sortedBy);
		Project p2 = new Project("p2");
		p2.setId(2L);
		Project p3 = new Project("p3");
		p3.setId(3L);

		List<RelatedProjectJoin> relatedJoins = Lists.newArrayList(new RelatedProjectJoin(p1, p2));
		when(projectService.getRelatedProjects(p1)).thenReturn(relatedJoins);

		Page<Project> projectPage = new PageImpl<>(Lists.newArrayList(p2, p3));
		when(projectService.getUnassociatedProjects(p1, projectName, page, count, Direction.ASC, sortedBy)).thenReturn(projectPage);

		Map<String, Object> potentialAssociatedProjects = controller.getPotentialAssociatedProjects(projectId,
				principal, page, count, sortedBy, sortDir, projectName);

		assertTrue(potentialAssociatedProjects.containsKey("associated"));

		List<Map<String, String>> associated = (List<Map<String, String>>) potentialAssociatedProjects
				.get("associated");
		assertEquals(2, associated.size());
		for (Map<String, String> pmap : associated) {
			if (pmap.get("id").equals("2")) {
				assertTrue(pmap.containsKey("associated"));
			}
		}

		verify(projectService).read(projectId);
		verify(projectService).getRelatedProjects(p1);
		verify(projectService)
				.getUnassociatedProjects(p1, projectName, page, count, Direction.ASC, sortedBy);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetPotentialAssociatedProjectsAsUser() {
		Long projectId = 1L;
		Principal principal = () -> USER_NAME;
		int page = 1;
		int count = 10;
		String sortedBy = "id";
		String sortDir = "asc";
		String projectName = "";

		Project p1 = new Project("p1");
		when(projectService.read(projectId)).thenReturn(p1);

		User user = new User();
		user.setSystemRole(Role.ROLE_USER);
		when(userService.getUserByUsername(USER_NAME)).thenReturn(user);
		Project p2 = new Project("p2");
		p2.setId(2L);
		Project p3 = new Project("p3");
		p3.setId(3L);

		List<RelatedProjectJoin> relatedJoins = Lists.newArrayList(new RelatedProjectJoin(p1, p2));
		when(projectService.getRelatedProjects(p1)).thenReturn(relatedJoins);

		Page<Project> projectPage = new PageImpl<>(Lists.newArrayList(p2, p3));
		when(projectService.getUnassociatedProjects(p1, projectName, page, count, Direction.ASC, "id")).thenReturn(projectPage);

		Map<String, Object> potentialAssociatedProjects = controller.getPotentialAssociatedProjects(projectId,
				principal, page, count, sortedBy, sortDir, projectName);

		assertTrue(potentialAssociatedProjects.containsKey("associated"));

		List<Map<String, String>> associated = (List<Map<String, String>>) potentialAssociatedProjects
				.get("associated");
		assertEquals(2, associated.size());
		for (Map<String, String> pmap : associated) {
			if (pmap.get("id").equals("2")) {
				assertTrue(pmap.containsKey("associated"));
			}
		}

		verify(projectService).read(projectId);
		verify(projectService).getRelatedProjects(p1);
		verify(projectService).getUnassociatedProjects(p1, "", page, count, Direction.ASC, "id");
	}

	@Test
	public void testAddAssociatedProject() {
		Long projectId = 1L;
		Long associatedProjectId = 2L;
		Project p1 = new Project();
		Project p2 = new Project();

		when(projectService.read(projectId)).thenReturn(p1);
		when(projectService.read(associatedProjectId)).thenReturn(p2);
		when(messageSource.getMessage("project.associated.added", new Object[]{}, Locale.US)).thenReturn("Success");

		ImmutableMap.of("associatedProjectId", associatedProjectId);
		controller.addAssociatedProject(projectId, associatedProjectId, Locale.US);

		verify(projectService).addRelatedProject(p1, p2);
	}

	@Test
	public void testRemoveAssociatedProject() {
		Long projectId = 1L;
		Long associatedProjectId = 2L;
		Project p1 = new Project();
		Project p2 = new Project();

		when(projectService.read(projectId)).thenReturn(p1);
		when(projectService.read(associatedProjectId)).thenReturn(p2);
		when(messageSource.getMessage("project.associated.removed", new Object[]{}, Locale.US)).thenReturn("Removed");

		controller.removeAssociatedProject(projectId, associatedProjectId, Locale.US);

		verify(projectService).removeRelatedProject(p1, p2);
	}

	@Test
	public void testEditAssociatedProjectsForProject() {
		Long projectId = 1L;
		ExtendedModelMap model = new ExtendedModelMap();
		Principal principal = () -> USER_NAME;

		when(apiService.findAll()).thenReturn(Lists.newArrayList(new RemoteAPI()));
		String editAssociatedProjectsForProject = controller.editAssociatedProjectsForProject(projectId, model,
				principal);

		verify(apiService).findAll();

		assertEquals("projects/project_settings", editAssociatedProjectsForProject);
	}

	@Test
	public void testGetPotentialRemoteAssociatedProjectsForApi() {
		Long projectId = 1L;
		Long apiId = 2L;
		Project project = new Project();
		RemoteAPI api = new RemoteAPI();

		when(projectService.read(projectId)).thenReturn(project);
		when(apiService.read(apiId)).thenReturn(api);

		Project rp1 = new Project();
		rp1.setId(3L);
		rp1.add(new Link("http://somewhere", Link.REL_SELF));

		String selfRel2 = "http://somewhere-else";
		Project rp2 = new Project();
		rp2.setId(4L);
		rp2.add(new Link("http://somewhere-else", Link.REL_SELF));

		RemoteRelatedProject rrp = new RemoteRelatedProject(project, api, selfRel2);

		when(projectRemoteService.listProjectsForAPI(api)).thenReturn(Lists.newArrayList(rp1, rp2));
		when(remoteRelatedProjectService.getRemoteProjectsForProject(project)).thenReturn(Lists.newArrayList(rrp));

		List<Map<String, String>> potentialRemoteAssociatedProjectsForApi = controller
				.getPotentialRemoteAssociatedProjectsForApi(projectId, apiId);
		assertEquals(2, potentialRemoteAssociatedProjectsForApi.size());

		int associatedCount = 0;
		for (Map<String, String> map : potentialRemoteAssociatedProjectsForApi) {
			if (map.containsKey("associated")) {
				associatedCount++;
			}
		}
		assertEquals("1 associated project should be found", 1, associatedCount);

		verify(projectRemoteService).listProjectsForAPI(api);
		verify(remoteRelatedProjectService).getRemoteProjectsForProject(project);
	}

	@Test
	public void testAddRemoteAssociatedProject() {
		Long projectId = 1L;

		String projectLink = "http://somewhere/projects/1";
		Project rp1 = new Project();
		rp1.setId(3L);
		rp1.add(new Link(projectLink, Link.REL_SELF));

		RemoteAPI api = new RemoteAPI();
		rp1.setRemoteAPI(api);

		Project project = new Project();

		when(projectService.read(projectId)).thenReturn(project);
		when(projectRemoteService.read(projectLink)).thenReturn(rp1);

		Map<String, String> addRemoteAssociatedProject = controller.addRemoteAssociatedProject(projectId, projectLink);

		assertEquals("success", addRemoteAssociatedProject.get("result"));

		ArgumentCaptor<RemoteRelatedProject> argumentCaptor = ArgumentCaptor.forClass(RemoteRelatedProject.class);
		verify(remoteRelatedProjectService).create(argumentCaptor.capture());

		RemoteRelatedProject value = argumentCaptor.getValue();
		assertEquals(api, value.getRemoteAPI());
		assertEquals(project, value.getLocalProject());
		assertEquals(projectLink, value.getRemoteProjectURI());
	}

	@Test
	public void testRemoveRemoteAssociatedProject() {
		Long projectId = 1L;
		Project project = new Project();

		String projectLink = "http://somewhere/projects/1";
		Project rp1 = new Project();
		rp1.setId(3L);
		rp1.add(new Link(projectLink, Link.REL_SELF));

		RemoteRelatedProject rrp = new RemoteRelatedProject();

		when(projectService.read(projectId)).thenReturn(project);
		when(remoteRelatedProjectService.getRemoteRelatedProjectForProjectAndURI(project, projectLink)).thenReturn(rrp);

		controller.removeRemoteAssociatedProject(projectId, projectLink);

		verify(remoteRelatedProjectService).delete(rrp.getId());
	}

}
