package ca.corefacility.bioinformatics.irida.model;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.URL;

import ca.corefacility.bioinformatics.irida.model.joins.impl.ProjectSampleJoin;
import ca.corefacility.bioinformatics.irida.model.joins.impl.ProjectUserJoin;
import ca.corefacility.bioinformatics.irida.model.user.Organization;
import ca.corefacility.bioinformatics.irida.validators.annotations.ValidProjectName;

/**
 * A project object.
 * 
 * @author Franklin Bristow <franklin.bristow@phac-aspc.gc.ca>
 * @author Thomas Matthews <thomas.matthews@phac-aspc.gc.ca>
 */
@Entity
@Table(name = "project")
@Audited
public class Project implements IridaThing, Comparable<Project> {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@NotNull(message = "{project.name.notnull}")
	@Size(min = 5, message = "{project.name.size}")
	@ValidProjectName
	private String name;

	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	private final Date createdDate;

	@Temporal(TemporalType.TIMESTAMP)
	private Date modifiedDate;

	@Lob
	private String projectDescription;

	@URL(message = "{project.remoteURL.url}")
	private String remoteURL;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, mappedBy = "project")
	private List<ProjectUserJoin> users;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, mappedBy = "project")
	private List<ProjectSampleJoin> samples;

	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
	private Organization organization;

	public Project() {
		createdDate = new Date();
		modifiedDate = createdDate;
	}

	/**
	 * Create a new {@link Project} with the given name
	 * 
	 * @param name
	 *            The name of the project
	 */
	public Project(String name) {
		this();
		this.name = name;
	}

	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Project) {
			Project p = (Project) other;
			return Objects.equals(createdDate, p.createdDate) && Objects.equals(modifiedDate, p.modifiedDate)
					&& Objects.equals(name, p.name) && Objects.equals(organization, p.organization);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(createdDate, modifiedDate, name, organization);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int compareTo(Project p) {
		return modifiedDate.compareTo(p.modifiedDate);
	}

	@Override
	public String getLabel() {
		return name;
	}

	@Override
	public Date getTimestamp() {
		return createdDate;
	}

	@Override
	public Date getModifiedDate() {
		return modifiedDate;
	}

	@Override
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getProjectDescription() {
		return projectDescription;
	}

	public void setProjectDescription(String projectDescription) {
		this.projectDescription = projectDescription;
	}

	public String getRemoteURL() {
		return remoteURL;
	}

	public void setRemoteURL(String remoteURL) {
		this.remoteURL = remoteURL;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}
}
