package ca.corefacility.bioinformatics.irida.web.controller.test.unit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import ca.corefacility.bioinformatics.irida.exceptions.EntityNotFoundException;
import ca.corefacility.bioinformatics.irida.model.Project;
import ca.corefacility.bioinformatics.irida.model.User;
import ca.corefacility.bioinformatics.irida.model.joins.Join;
import ca.corefacility.bioinformatics.irida.model.joins.impl.ProjectUserJoin;
import ca.corefacility.bioinformatics.irida.service.ProjectService;
import ca.corefacility.bioinformatics.irida.service.UserService;
import ca.corefacility.bioinformatics.irida.web.assembler.resource.ResourceCollection;
import ca.corefacility.bioinformatics.irida.web.assembler.resource.project.ProjectResource;
import ca.corefacility.bioinformatics.irida.web.assembler.resource.user.UserResource;
import ca.corefacility.bioinformatics.irida.web.controller.api.GenericController;
import ca.corefacility.bioinformatics.irida.web.controller.api.UsersController;

import com.google.common.collect.Lists;

/**
 * Unit tests for {@link UsersController}.
 *
 * @author Franklin Bristow <franklin.bristow@phac-aspc.gc.ca>
 */
public class UserControllerTest {

    private UsersController controller;
    private UserService userService;
    private ProjectService projectService;

    @Before
    public void setUp() {
        userService = mock(UserService.class);
        projectService = mock(ProjectService.class);
        controller = new UsersController(userService, projectService);

        // fake out the servlet response so that the URI builder will work.
        RequestAttributes ra = new ServletRequestAttributes(new MockHttpServletRequest());
        RequestContextHolder.setRequestAttributes(ra);
    }

    @Test
    public void testGetUserProjects() {
        // set up expectations
        String username = "fbristow";
        User u = new User();
        u.setUsername(username);
        List<Join<Project,User>> projects = new ArrayList<>();
        Project p = TestDataFactory.constructProject();
        Join<Project, User> join = new ProjectUserJoin(p, u);
        projects.add(join);
        // set up mocks
        when(userService.getUserByUsername(username)).thenReturn(u);
        when(projectService.getProjectsForUser(u)).thenReturn(projects);
        // run the test
        ModelMap output = controller.getUserProjects(username);
        @SuppressWarnings("unchecked")
        ResourceCollection<ProjectResource> pulledProjects = (ResourceCollection<ProjectResource>) output
                .get("projectResources");
        List<ProjectResource> projectResources = pulledProjects.getResources();
        assertEquals(1, projectResources.size());
        ProjectResource resource = projectResources.get(0);
        assertEquals(p.getName(), resource.getName());
        assertEquals(1, resource.getLinks().size());
        Link link = resource.getLinks().get(0);
        assertEquals(Link.REL_SELF, link.getRel());
        assertTrue(link.getHref().contains(p.getId().toString()));
    }

    @Test
    public void testGetUserProjectsBadUser() {
        String username = "superbad";
        when(userService.getUserByUsername(username)).thenThrow(
                new EntityNotFoundException(username));
        try {
            controller.getUserProjects(username);
            fail();
        } catch (EntityNotFoundException e) {
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testGetAllUsers() {
        String username = "fbristow";
        User u = new User();
        u.setUsername(username);
        List<User> users = Lists.newArrayList(u);
        when(userService.findAll()).thenReturn(users);
        when(userService.count()).thenReturn(1L);

        ModelMap output = controller.listAllResources();

        @SuppressWarnings("unchecked")
		ResourceCollection<UserResource> usersCollection = (ResourceCollection<UserResource>) output.get(
                GenericController.RESOURCE_NAME);
        assertEquals("users collection is the wrong size.", 1, usersCollection.size());
        UserResource userResource = usersCollection.iterator().next();
        assertEquals("username is not correct.", username, userResource.getUsername());
    }
}