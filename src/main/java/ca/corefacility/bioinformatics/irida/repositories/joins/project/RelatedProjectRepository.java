package ca.corefacility.bioinformatics.irida.repositories.joins.project;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import ca.corefacility.bioinformatics.irida.model.joins.impl.RelatedProjectJoin;
import ca.corefacility.bioinformatics.irida.model.project.Project;

/**
 * Repository for managing {@link RelatedProjectJoin}s
 * 
 * @author Thomas Matthews <thomas.matthews@phac-aspc.gc.ca>
 *
 */
public interface RelatedProjectRepository extends CrudRepository<RelatedProjectJoin, Long> {

	/**
	 * Get the List of {@link Project}s that are related to this project as a
	 * list of {@link RelatedProjectJoin}s
	 * 
	 * This method will return {@link RelatedProjectJoin}s where the given
	 * project is the {@code subject} property.
	 * 
	 * @param project
	 *            The Project to search from
	 * @return A List of {@link RelatedProjectJoin}s
	 */
	@Query("FROM RelatedProjectJoin r WHERE r.subject=?")
	public List<RelatedProjectJoin> getRelatedProjectsForProject(Project project);

	/**
	 * Get the list of {@link Project}s that this project is related to as a
	 * list of {@link RelatedProjectJoin}s.
	 * 
	 * This method will return {@link RelatedProjectJoin}s where the given
	 * project is the {@code relatedProject} property.
	 * 
	 * @param project
	 *            The project that is related to other projects
	 * @return A List of {@link RelatedProjectJoin}s
	 */
	@Query("FROM RelatedProjectJoin r WHERE r.relatedProject=?")
	public List<RelatedProjectJoin> getReverseRelatedProjects(Project project);
}
