package ca.corefacility.bioinformatics.irida.ria.web.ajax.projects;

import java.util.List;
import java.util.Locale;

import ca.corefacility.bioinformatics.irida.ria.web.exceptions.UIConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ca.corefacility.bioinformatics.irida.model.user.User;
import ca.corefacility.bioinformatics.irida.ria.web.ajax.dto.NewMemberRequest;
import ca.corefacility.bioinformatics.irida.ria.web.exceptions.UIProjectWithoutOwnerException;
import ca.corefacility.bioinformatics.irida.ria.web.models.tables.TableRequest;
import ca.corefacility.bioinformatics.irida.ria.web.models.tables.TableResponse;
import ca.corefacility.bioinformatics.irida.ria.web.projects.dto.ProjectMemberTableModel;
import ca.corefacility.bioinformatics.irida.ria.web.services.UIProjectMembersService;

/**
 * Controller for all asynchronous request from the UI for Project Members
 */
@RestController
@RequestMapping("/ajax/projects/members")
public class ProjectMembersAjaxController {
	private final UIProjectMembersService projectMembersService;

	@Autowired
	public ProjectMembersAjaxController(UIProjectMembersService projectMembersService) {
		this.projectMembersService = projectMembersService;
	}

	/**
	 * Get a paged listing of project members passed on parameters set in the table request.
	 *
	 * @param projectId    - identifier for the current project
	 * @param tableRequest - details about the current page of the table
	 * @return sorted and filtered list of project members
	 */
	@RequestMapping("")
	public ResponseEntity<TableResponse<ProjectMemberTableModel>> getProjectMembers(@RequestParam Long projectId,
			@RequestBody TableRequest tableRequest) {
		return ResponseEntity.ok(projectMembersService.getProjectMembers(projectId, tableRequest));
	}

	/**
	 * Remove a user from the project
	 *
	 * @param projectId - identifier for the current project
	 * @param id        - identifier for the user to remove from the project
	 * @param locale    - of the currently logged in user
	 * @return message to display to the user about the outcome of the removal.
	 */
	@RequestMapping(value = "", method = RequestMethod.DELETE)
	public ResponseEntity<String> removeUserFromProject(@RequestParam Long projectId, @RequestParam Long id,
			Locale locale) {
		try {
			return ResponseEntity.ok(projectMembersService.removeUserFromProject(projectId, id, locale));
		} catch (UIProjectWithoutOwnerException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	/**
	 * Update a users role on a project
	 *
	 * @param projectId   Identifier for the current project
	 * @param id          Identifier for the user to remove from the project
	 * @param projectRole Project role to update the user to
	 * @param locale      Locale of the currently logged in user
	 * @return message to display to the user about the outcome of the change in role.
	 */
	@RequestMapping(value = "/role", method = RequestMethod.PUT)
	public ResponseEntity<String> updateUserRoleOnProject(@RequestParam Long projectId, @RequestParam Long id,
			String projectRole, Locale locale) {
		try {
			return ResponseEntity.ok(projectMembersService.updateUserRoleOnProject(projectId, id, projectRole, locale));
		} catch (UIProjectWithoutOwnerException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		} catch (UIConstraintViolationException ex) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getErrorMessage());
		}
	}

	/**
	 * Update a users metadata role on a project
	 *
	 * @param projectId    Identifier for the current project
	 * @param id           Identifier for the user to remove from the project
	 * @param metadataRole Metadata role to update the user to
	 * @param locale       Locale of the currently logged in user
	 * @return message to display to the user about the outcome of the change in role.
	 */
	@RequestMapping(value = "/metadata-role", method = RequestMethod.PUT)
	public ResponseEntity<String> updateUserMetadataRoleOnProject(@RequestParam Long projectId, @RequestParam Long id,
			@RequestParam(required = false) String metadataRole, Locale locale) {
		try {
			return ResponseEntity.ok(
					projectMembersService.updateUserMetadataRoleOnProject(projectId, id, metadataRole, locale));
		} catch (UIConstraintViolationException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getErrorMessage());
		}
	}

	/**
	 * Get a filtered list of available IRIDA instance users for this project
	 *
	 * @param projectId - identifier for the current project
	 * @param query     - search query to filter the users by
	 * @return List of filtered users.
	 */
	@RequestMapping("/available")
	public ResponseEntity<List<User>> getAvailableMembersForProject(@RequestParam Long projectId,
			@RequestParam String query) {
		return ResponseEntity.ok(projectMembersService.getAvailableUsersForProject(projectId, query));
	}

	/**
	 * Add a user to a project
	 *
	 * @param projectId - identifier for the current project
	 * @param request   - details about the user to add to the project (id and role)
	 * @param locale    - of the currently logged in user
	 * @return message to display to the user about the outcome of adding the user to the project
	 */
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public ResponseEntity<String> addMemberToProject(@RequestParam Long projectId,
			@RequestBody NewMemberRequest request, Locale locale) {
		return ResponseEntity.ok(projectMembersService.addMemberToProject(projectId, request, locale));
	}
}
