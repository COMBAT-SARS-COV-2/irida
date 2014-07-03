package ca.corefacility.bioinformatics.irida.security.permissions;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import ca.corefacility.bioinformatics.irida.model.Project;
import ca.corefacility.bioinformatics.irida.model.enums.ProjectRole;
import ca.corefacility.bioinformatics.irida.model.joins.Join;
import ca.corefacility.bioinformatics.irida.model.user.User;
import ca.corefacility.bioinformatics.irida.repositories.ProjectRepository;
import ca.corefacility.bioinformatics.irida.repositories.joins.project.ProjectUserJoinRepository;
import ca.corefacility.bioinformatics.irida.repositories.user.UserRepository;

/**
 * Confirms that a given user is the owner of a project
 * 
 * @author Thomas Matthews <thomas.matthews@phac-aspc.gc.ca>
 *
 */
@Component
public class ProjectOwnerPermission extends BasePermission<Project> {
	private static final Logger logger = LoggerFactory.getLogger(ProjectOwnerPermission.class);
	private static final String PERMISSION_PROVIDED = "isProjectOwner";

	private UserRepository userRepository;
	private ProjectUserJoinRepository pujRepository;

	/**
	 * Construct an instance of {@link ReadProjectPermission}.
	 */
	@Autowired
	public ProjectOwnerPermission(ProjectRepository projectRepository, UserRepository userRepository,
			ProjectUserJoinRepository pujRepository) {
		super(Project.class, projectRepository);
		this.userRepository = userRepository;
		this.pujRepository = pujRepository;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean customPermissionAllowed(Authentication authentication, Project p) {
		logger.trace("Testing permission for [" + authentication + "] has manager permissions on project [" + p + "]");
		// check if the user is a project owner for this project
		User u = userRepository.loadUserByUsername(authentication.getName());
		List<Join<Project, User>> projectUsers = pujRepository.getUsersForProjectByRole(p, ProjectRole.PROJECT_OWNER);

		for (Join<Project, User> projectUser : projectUsers) {
			if (projectUser.getObject().equals(u)) {
				logger.trace("Permission GRANTED for [" + authentication + "] on project [" + p + "]");
				// this user is an owner for the project.
				return true;
			}
		}

		logger.trace("Permission DENIED for [" + authentication + "] on project [" + p + "]");
		return false;
	}

	@Override
	public String getPermissionProvided() {
		return PERMISSION_PROVIDED;
	}
}
