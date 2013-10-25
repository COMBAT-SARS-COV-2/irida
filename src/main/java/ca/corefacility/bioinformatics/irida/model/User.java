package ca.corefacility.bioinformatics.irida.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.hibernate.validator.constraints.Email;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import ca.corefacility.bioinformatics.irida.validators.Patterns;

/**
 * A user object.
 * 
 * @author Franklin Bristow <franklin.bristow@phac-aspc.gc.ca>
 * @author Thomas Matthews <thomas.matthews@phac-aspc.gc.ca>
 */
@Entity
@Table(name = "user", uniqueConstraints = { @UniqueConstraint(name = "user_email_constraint", columnNames = "email"),
		@UniqueConstraint(name = "user_username_constraint", columnNames = "username") })
@Audited
public class User implements IridaThing, Comparable<User>, UserDetails {

	private static final long serialVersionUID = -7516211470008791995L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@NotNull(message = "{user.username.notnull}")
	@Size(min = 3, message = "{user.username.size}")
	// @Column(unique = true)
	private String username;
	@NotNull(message = "{user.email.notnull}")
	@Size(min = 5, message = "{user.email.size}")
	@Email(message = "{user.email.invalid}")
	// @Column(unique = true)
	private String email;
	@NotNull(message = "{user.password.notnull}")
	// passwords must be at least six characters long, but prohibit passwords
	// longer than 1024 (who's going to remember a password that long anyway?)
	// to prevent DOS attacks on our password hashing.
	@Size(min = 6, max = 1024, message = "{user.password.size}")
	@Patterns({ @Pattern(regexp = "^.*[A-Z].*$", message = "{user.password.uppercase}"),
			@Pattern(regexp = "^.*[0-9].*$", message = "{user.password.number}"),
			@Pattern(regexp = "^.*[a-z].*$", message = "{user.password.lowercase}") })
	private String password;
	@NotNull(message = "{user.firstName.notnull}")
	@Size(min = 2, message = "{user.firstName.size}")
	private String firstName;
	@NotNull(message = "{user.lastName.notnull}")
	@Size(min = 2, message = "{user.lastName.size}")
	private String lastName;
	@NotNull(message = "{user.phoneNumber.notnull}")
	@Size(min = 4, message = "{user.phoneNumber.size}")
	private String phoneNumber;
	@NotNull
	private Boolean enabled = true;

	@ManyToOne
	@JoinColumn(name = "system_role")
	@NotNull
	private Role systemRole;

	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate;

	@Temporal(TemporalType.TIMESTAMP)
	private Date modifiedDate;

	private String locale;

	private boolean credentialsNonExpired;

	/**
	 * Construct an instance of {@link User} with no properties set.
	 */
	public User() {
		createdDate = new Date();
		modifiedDate = createdDate;
		locale = "en";
		credentialsNonExpired = true;
		this.systemRole = Role.ROLE_USER;
	}

	/**
	 * Construct an instance of {@link User} with all properties (except
	 * {@link UserIdentifier}) set.
	 * 
	 * @param username
	 *            the username for this {@link User}.
	 * @param email
	 *            the e-mail for this {@link User}.
	 * @param password
	 *            the password for this {@link User}.
	 * @param firstName
	 *            the first name of this {@link User}.
	 * @param lastName
	 *            the last name of this {@link User}.
	 * @param phoneNumber
	 *            the phone number of this {@link User}.
	 */
	public User(String username, String email, String password, String firstName, String lastName, String phoneNumber) {
		this();
		this.username = username;
		this.email = email;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phoneNumber = phoneNumber;
	}

	/**
	 * Construct an instance of {@link User} with all properties set.
	 * 
	 * @param id
	 *            the {@link UserIdentifier} for this {@link User}.
	 * @param username
	 *            the username for this {@link User}.
	 * @param email
	 *            the e-mail for this {@link User}.
	 * @param password
	 *            the password for this {@link User}.
	 * @param firstName
	 *            the first name of this {@link User}.
	 * @param lastName
	 *            the last name of this {@link User}.
	 * @param phoneNumber
	 *            the phone number of this {@link User}.
	 */
	public User(Long id, String username, String email, String password, String firstName, String lastName,
			String phoneNumber) {
		this(username, email, password, firstName, lastName, phoneNumber);
		this.id = id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(username, email, password, firstName, lastName, phoneNumber, createdDate, modifiedDate,
				credentialsNonExpired);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object other) {
		if (other instanceof User) {
			User u = (User) other;
			return Objects.equals(username, u.username) && Objects.equals(email, u.email)
					&& Objects.equals(password, u.password) && Objects.equals(firstName, u.firstName)
					&& Objects.equals(lastName, u.lastName) && Objects.equals(phoneNumber, u.phoneNumber)
					&& Objects.equals(createdDate, u.createdDate) && Objects.equals(modifiedDate, u.modifiedDate)
					&& Objects.equals(credentialsNonExpired, u.credentialsNonExpired);
		}

		return false;
	}

	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(User u) {
		return modifiedDate.compareTo(u.modifiedDate);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return com.google.common.base.Objects.toStringHelper(User.class).add("username", username).add("email", email)
				.add("firstName", firstName).add("lastName", lastName).add("phoneNumber", phoneNumber).toString();
	}

	@Override
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	@Override
	public String getLabel() {
		return firstName + " " + lastName;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		ArrayList<Role> roles = new ArrayList<>();
		roles.add(systemRole);
		return roles;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return credentialsNonExpired;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean valid) {
		this.enabled = valid;
	}

	@Override
	public Date getTimestamp() {
		return createdDate;
	}

	@Override
	public void setTimestamp(Date date) {
		this.createdDate = date;
	}

	@Override
	public Date getModifiedDate() {
		return modifiedDate;
	}

	@Override
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public Role getSystemRole() {
		return systemRole;
	}

	public void setCredentialsNonExpired(boolean credentialsNonExpired) {
		this.credentialsNonExpired = credentialsNonExpired;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public void setSystemRole(Role systemRole) {
		this.systemRole = systemRole;
	}
}