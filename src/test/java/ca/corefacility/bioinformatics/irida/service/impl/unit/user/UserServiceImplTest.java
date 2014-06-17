package ca.corefacility.bioinformatics.irida.service.impl.unit.user;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import ca.corefacility.bioinformatics.irida.exceptions.EntityExistsException;
import ca.corefacility.bioinformatics.irida.exceptions.EntityNotFoundException;
import ca.corefacility.bioinformatics.irida.model.user.Role;
import ca.corefacility.bioinformatics.irida.model.user.User;
import ca.corefacility.bioinformatics.irida.repositories.joins.project.ProjectUserJoinRepository;
import ca.corefacility.bioinformatics.irida.repositories.joins.user.UserGroupJoinRepository;
import ca.corefacility.bioinformatics.irida.repositories.user.UserRepository;
import ca.corefacility.bioinformatics.irida.service.impl.user.UserServiceImpl;
import ca.corefacility.bioinformatics.irida.service.user.UserService;

import com.google.common.collect.ImmutableMap;

/**
 * Testing the behavior of {@link UserServiceImpl}
 * 
 * @author Franklin Bristow <franklin.bristow@phac-aspc.gc.ca>
 */
public class UserServiceImplTest {

	private UserService userService;
	private UserRepository userRepository;
	private ProjectUserJoinRepository pujRepository;
	private Validator validator;
	private PasswordEncoder passwordEncoder;
	private UserGroupJoinRepository userGroupJoinRepository;

	@Before
	public void setUp() {
		validator = mock(Validator.class);
		userRepository = mock(UserRepository.class);
		passwordEncoder = mock(PasswordEncoder.class);
		pujRepository = mock(ProjectUserJoinRepository.class);
		userGroupJoinRepository = mock(UserGroupJoinRepository.class);
		userService = new UserServiceImpl(userRepository, pujRepository, userGroupJoinRepository, passwordEncoder,
				validator);
	}

	@Test(expected = EntityNotFoundException.class)
	// should throw the exception to the caller instead of swallowing it.
	public void testBadUsername() {
		String username = "superwrongusername";
		when(userRepository.loadUserByUsername(username)).thenThrow(new EntityNotFoundException("not found"));
		userService.getUserByUsername(username);
	}

	@Test
	public void testPasswordUpdate() {
		final String password = "Password1";
		final String encodedPassword = "ENCODED_" + password;
		final User persisted = user();
		final Long id = persisted.getId();

		Map<String, Object> properties = new HashMap<>();
		properties.put("password", (Object) password);
		// Map<String, Object> encodedPasswordProperties =
		// ImmutableMap.of("password", (Object) encodedPassword);

		when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
		when(userRepository.save(persisted)).thenReturn(persisted);
		when(userRepository.findOne(id)).thenReturn(persisted);
		when(userRepository.exists(id)).thenReturn(true);

		User u = userService.update(id, properties);
		assertEquals("User-type was not returned.", persisted, u);

		verify(passwordEncoder).encode(password);
		verify(userRepository).findOne(id);
		verify(userRepository).save(persisted);
		verify(userRepository).exists(id);
	}

	@Test
	public void updateNoPassword() {
		Map<String, Object> properties = ImmutableMap.of("username", (Object) "updated");

		when(userRepository.exists(1l)).thenReturn(true);
		when(userRepository.findOne(1l)).thenReturn(user());
		userService.update(1l, properties);
		verifyZeroInteractions(passwordEncoder);
	}

	@Test
	public void testCreateGoodPassword() {
		final User u = user();
		final String password = u.getPassword();
		final String encodedPassword = "ENCODED_" + password;

		when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
		when(userRepository.save(u)).thenReturn(u);

		userService.create(u);
		assertEquals("User password was not encoded.", encodedPassword, u.getPassword());

		verify(passwordEncoder).encode(password);
		verify(userRepository).save(u);
	}

	@Test
	public void testLoadUserByUsername() {
		User user = user();
		user.setSystemRole(Role.ROLE_USER);
		String username = user.getUsername();
		String password = user.getPassword();

		when(userRepository.loadUserByUsername(username)).thenReturn(user);

		UserDetails userDetails = userService.loadUserByUsername(username);

		assertEquals(username, userDetails.getUsername());
		assertEquals(password, userDetails.getPassword());
	}

	@Test
	public void testUpdatePasswordGoodPassword() {
		String password = "Password1";
		String encodedPassword = password + "_ENCODED";

		when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
		when(userRepository.exists(1l)).thenReturn(true);
		when(userRepository.findOne(1l)).thenReturn(user());

		userService.changePassword(1l, password);

		ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(argument.capture());
		User saved = argument.getValue();
		assertEquals("password field was not encoded.", encodedPassword, saved.getPassword());
	}

	@Test(expected = EntityExistsException.class)
	public void testCreateUserWithIntegrityConstraintViolations() {
		User u = new User();

		ConstraintViolationException constraintViolationException = new ConstraintViolationException("Duplicate", null,
				User.USER_USERNAME_CONSTRAINT_NAME);
		DataIntegrityViolationException integrityViolationException = new DataIntegrityViolationException("Duplicate",
				constraintViolationException);

		when(userRepository.save(any(User.class))).thenThrow(integrityViolationException);
		when(validator.validateValue(eq(User.class), eq("password"), any(String.class))).thenReturn(
				new HashSet<ConstraintViolation<User>>());

		userService.create(u);
	}

	@Test(expected = DataIntegrityViolationException.class)
	public void testCreateUserWithUnknownIntegrityConstraintViolation() {
		User u = new User();

		DataIntegrityViolationException integrityViolationException = new DataIntegrityViolationException("Duplicate");

		when(userRepository.save(any(User.class))).thenThrow(integrityViolationException);
		when(validator.validateValue(eq(User.class), eq("password"), any(String.class))).thenReturn(
				new HashSet<ConstraintViolation<User>>());

		userService.create(u);
	}

	@Test(expected = EntityExistsException.class)
	public void testCreateUserWithUnknownIntegrityConstraintViolationName() {
		User u = new User();

		ConstraintViolationException constraintViolationException = new ConstraintViolationException("Duplicate", null,
				"Not a very nicely formatted constraint violation name.");
		DataIntegrityViolationException integrityViolationException = new DataIntegrityViolationException("Duplicate",
				constraintViolationException);

		when(userRepository.save(any(User.class))).thenThrow(integrityViolationException);
		when(validator.validateValue(eq(User.class), eq("password"), any(String.class))).thenReturn(
				new HashSet<ConstraintViolation<User>>());

		userService.create(u);
	}

	@Test(expected = EntityExistsException.class)
	public void testCreateUserWithNoConstraintViolationName() {
		User u = new User();

		ConstraintViolationException constraintViolationException = new ConstraintViolationException(null, null, null);
		DataIntegrityViolationException integrityViolationException = new DataIntegrityViolationException("Duplicate",
				constraintViolationException);

		when(userRepository.save(any(User.class))).thenThrow(integrityViolationException);
		when(validator.validateValue(eq(User.class), eq("password"), any(String.class))).thenReturn(
				new HashSet<ConstraintViolation<User>>());

		userService.create(u);
	}

	@Test
	public void testLoadUserByEmail() {
		String email = "fbristow@gmail.com";
		User u = user();
		User u2 = user();

		when(userRepository.loadUserByEmail(email)).thenReturn(u);

		u2.setModifiedDate(u.getModifiedDate());

		User loadUserByEmail = userService.loadUserByEmail(email);

		assertEquals(u2, loadUserByEmail);
	}

	@Test(expected = EntityNotFoundException.class)
	public void testLoadUserByEmailNotFound() {
		String email = "bademail@nowhere.com";
		when(userRepository.loadUserByEmail(email)).thenReturn(null);

		userService.loadUserByEmail(email);

	}

	private User user() {
		String username = "fbristow";
		String password = "Password1";
		String email = "fbristow@gmail.com";
		String firstName = "Franklin";
		String lastName = "Bristow";
		String phoneNumber = "7029";
		User u = new User(username, email, password, firstName, lastName, phoneNumber);
		return u;
	}
}
