package ca.corefacility.bioinformatics.irida.security.permissions;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import ca.corefacility.bioinformatics.irida.model.joins.Join;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.repositories.joins.project.ProjectSampleJoinRepository;
import ca.corefacility.bioinformatics.irida.repositories.sample.SampleRepository;

/**
 * Custom permission implementation for updating {@link Sample}.
 *
 */
@Component
public class UpdateSamplePermission extends BasePermission<Sample, Long> {

	private static final String PERMISSION_PROVIDED = "canUpdateSample";

	private final ProjectSampleJoinRepository projectSampleJoinRepository;

	private final ProjectOwnerPermission projectOwnerPermission;

	/**
	 * Constructs a new instance of {@link UpdateSamplePermission}.
	 * 
	 * @param sampleRepository
	 *            the sample repository
	 * @param projectOwnerPermission
	 *            the project owner permission
	 * @param projectSampleJoinRepository
	 *            the project sample join repository
	 */
	@Autowired
	protected UpdateSamplePermission(final SampleRepository sampleRepository,
			final ProjectOwnerPermission projectOwnerPermission,
			final ProjectSampleJoinRepository projectSampleJoinRepository) {
		super(Sample.class, Long.class, sampleRepository);
		this.projectSampleJoinRepository = projectSampleJoinRepository;
		this.projectOwnerPermission = projectOwnerPermission;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPermissionProvided() {
		return PERMISSION_PROVIDED;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean customPermissionAllowed(final Authentication authentication, final Sample targetDomainObject) {
		final List<Join<Project, Sample>> projects = projectSampleJoinRepository
				.getProjectForSample(targetDomainObject);
		return projects.stream().anyMatch(p -> projectOwnerPermission.isAllowed(authentication, p.getSubject()));
	}
}
