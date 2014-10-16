package ca.corefacility.bioinformatics.irida.ria.unit.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Base64;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ExtendedModelMap;

import ca.corefacility.bioinformatics.irida.exceptions.EntityNotFoundException;
import ca.corefacility.bioinformatics.irida.model.user.PasswordReset;
import ca.corefacility.bioinformatics.irida.model.user.User;
import ca.corefacility.bioinformatics.irida.ria.utilities.EmailController;
import ca.corefacility.bioinformatics.irida.ria.web.PasswordResetController;
import ca.corefacility.bioinformatics.irida.service.user.PasswordResetService;
import ca.corefacility.bioinformatics.irida.service.user.UserService;

/**
 * 
 * @author Thomas Matthews <thomas.matthews@phac-aspc.gc.ca>
 *
 */
public class PasswordResetControllerTest {
	private UserService userService;
	private PasswordResetService passwordResetService;
	private EmailController emailController;
	private MessageSource messageSource;
	private PasswordResetController controller;

	@Before
	public void setUp() {
		userService = mock(UserService.class);
		passwordResetService = mock(PasswordResetService.class);
		emailController = mock(EmailController.class);
		messageSource = mock(MessageSource.class);

		controller = new PasswordResetController(userService, passwordResetService, emailController, messageSource);
	}

	@Test
	public void testGetResetPage() {
		User user = new User(1l, "tom", null, null, null, null, null);
		PasswordReset passwordReset = new PasswordReset(user);
		String resetId = passwordReset.getId();
		ExtendedModelMap model = new ExtendedModelMap();

		when(passwordResetService.read(resetId)).thenReturn(passwordReset);

		String resetPage = controller.getResetPage(resetId, null, model);
		assertEquals(PasswordResetController.PASSWORD_RESET_PAGE, resetPage);
		assertTrue(model.containsKey("errors"));
		assertTrue(model.containsKey("passwordReset"));
		assertTrue(model.containsKey("user"));

		verify(passwordResetService).read(resetId);
	}

	@Test
	public void testSubmitPasswordReset() {
		String username = "tom";
		String email = "tom@somewhere.com";
		User user = new User(1l, username, email, null, null, null, null);
		PasswordReset passwordReset = new PasswordReset(user);
		String resetId = passwordReset.getId();
		String password = "Password1";
		ExtendedModelMap model = new ExtendedModelMap();

		when(passwordResetService.read(resetId)).thenReturn(passwordReset);

		String sendNewPassword = controller.sendNewPassword(resetId, password, password, model,
				LocaleContextHolder.getLocale());

		assertEquals(PasswordResetController.SUCCESS_REDIRECT + Base64.getEncoder().encodeToString(email.getBytes()),
				sendNewPassword);
		assertEquals("User should not be logged in after resetting password", null, SecurityContextHolder.getContext()
				.getAuthentication());

		verify(passwordResetService).read(resetId);
		verify(userService).changePassword(user.getId(), password);
		verify(passwordResetService).delete(resetId);
	}

	@Test
	public void testSubmitPasswordNoMatch() {
		User user = new User(1l, "tom", null, null, null, null, null);
		PasswordReset passwordReset = new PasswordReset(user);
		String resetId = passwordReset.getId();
		String password = "Password1";
		ExtendedModelMap model = new ExtendedModelMap();

		when(passwordResetService.read(resetId)).thenReturn(passwordReset);

		String sendNewPassword = controller.sendNewPassword(resetId, password, "not the same", model,
				LocaleContextHolder.getLocale());

		assertEquals(PasswordResetController.PASSWORD_RESET_PAGE, sendNewPassword);
		assertTrue(model.containsKey("errors"));

		verify(passwordResetService, times(2)).read(resetId);
	}

	@Test
	public void testGetNoLoginEmailPage() {
		assertEquals(PasswordResetController.CREATE_RESET_PAGE, controller.noLoginResetPassword(null));
	}

	@Test
	public void testSubmitEmail() {
		String email = "tom@somewhere.com";
		User user = new User("tom", email, null, null, null, null);
		ExtendedModelMap model = new ExtendedModelMap();

		when(userService.loadUserByEmail(email)).thenReturn(user);

		String submitEmail = controller.submitEmail(email, model);
		assertEquals(PasswordResetController.CREATED_REDIRECT + Base64.getEncoder().encodeToString(email.getBytes()),
				submitEmail);
		assertTrue(model.containsKey("email"));

		verify(userService).loadUserByEmail(email);
		verify(passwordResetService).create(any(PasswordReset.class));
		verify(emailController).sendPasswordResetLinkEmail(eq(user), any(PasswordReset.class));
	}

	@Test
	public void testSubmitEmailNotExists() {
		String email = "tom@nowhere.com";
		ExtendedModelMap model = new ExtendedModelMap();

		when(userService.loadUserByEmail(email)).thenThrow(new EntityNotFoundException("email doesn't exist"));

		String submitEmail = controller.submitEmail(email, model);
		assertEquals(PasswordResetController.CREATE_RESET_PAGE, submitEmail);

		assertTrue(model.containsKey("email"));
		assertTrue(model.containsKey("emailError"));

		verify(userService).loadUserByEmail(email);
		verifyZeroInteractions(emailController);
	}
}
