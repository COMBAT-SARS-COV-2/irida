package ca.corefacility.bioinformatics.irida.repositories.specification;

import java.util.ArrayList;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import ca.corefacility.bioinformatics.irida.model.project.Project;

/**
 * Specification for searching project properties
 * 
 *
 */
public class ProjectSpecification {
	/**
	 * Search for a project by name
	 * 
	 * @param name
	 *            the name to use in the search.
	 * @return a {@link Specification} on project name.
	 */
	public static Specification<Project> searchProjectName(String name) {
		return new Specification<Project>() {
			@Override
			public Predicate toPredicate(Root<Project> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				return cb.like(root.get("name"), "%" + name + "%");
			}

		};
	}

	/**
	 * Exclude the given projects from the results
	 * 
	 * @param projects
	 *            The projects to exclude
	 * @return A specification instructing to exclude the given projects
	 */
	public static Specification<Project> excludeProject(Project... projects) {
		return new Specification<Project>() {
			@Override
			public Predicate toPredicate(Root<Project> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

				ArrayList<Predicate> predicates = new ArrayList<>();
				for (Project p : projects) {
					predicates.add(cb.notEqual(root, p));
				}

				return cb.and(predicates.toArray(new Predicate[predicates.size()]));
			}
		};
	}

	/**
	 * Search for projects based on submitted criteria
	 *
	 * @param searchMap {@link Map} of values to filter by.
	 * @return A {@link Specification}
	 */
	public static Specification<Project> searchProjects(Map<String, String> searchMap) {
		return (root, query, cb) -> {
			ArrayList<Predicate> predicates = new ArrayList<>();

			if (searchMap.containsKey("name")) {
				predicates.add(cb.like(root.get("name"), "%" + searchMap.get("name") + "%"));
			}
			if (searchMap.containsKey("organism")) {
				predicates.add(cb.like(root.get("organism"), "%" + searchMap.get("organism") + "%"));
			}

			return cb.and(predicates.toArray(new Predicate[predicates.size()]));
		};
	}
}
