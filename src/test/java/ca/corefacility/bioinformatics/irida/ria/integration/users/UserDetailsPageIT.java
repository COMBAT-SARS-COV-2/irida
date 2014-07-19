package ca.corefacility.bioinformatics.irida.ria.integration.users;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import ca.corefacility.bioinformatics.irida.config.IridaApiPropertyPlaceholderConfig;
import ca.corefacility.bioinformatics.irida.config.data.IridaApiJdbcDataSourceConfig;
import ca.corefacility.bioinformatics.irida.ria.integration.pages.LoginPage;
import ca.corefacility.bioinformatics.irida.ria.integration.pages.user.UserDetailsPage;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { IridaApiJdbcDataSourceConfig.class,
		IridaApiPropertyPlaceholderConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class })
@ActiveProfiles("it")
@DatabaseSetup("/ca/corefacility/bioinformatics/irida/ria/web/users/UserDetailsPageIT.xml")
@DatabaseTearDown("classpath:/ca/corefacility/bioinformatics/irida/test/integration/TableReset.xml")
public class UserDetailsPageIT {
	private WebDriver driver;
	private UserDetailsPage usersPage;

	@Before
	public void setup() {
		driver = new PhantomJSDriver();
		LoginPage loginPage = LoginPage.to(driver);
		loginPage.doLogin();

		usersPage = new UserDetailsPage(driver);
	}

	@After
	public void destroy() {
		if (driver != null) {
			driver.close();
            driver.quit();
		}
	}

	@Test
	public void testGetUserId() {
		String currentUserId = usersPage.getCurrentUserId();
		assertEquals(String.valueOf(1l), currentUserId);
	}

	@Test
	public void testGetOtherUserId() {
		Long id = 2l;
		String otherUserId = usersPage.getOtherUserId(id);
		assertEquals(String.valueOf(id), otherUserId);
	}

	@Test
	public void testGetEditUserButton() {
		assertTrue("Should see button for user 1", usersPage.canGetEditLink(1l));
		assertFalse("Should not see button for user 2", usersPage.canGetEditLink(2l));
	}
	
	@Test
	public void testGetUserProjects(){
		List<String> userProjectIds = usersPage.getUserProjectIds(1l);
		assertTrue(userProjectIds.contains("1"));
		assertTrue(userProjectIds.contains("2"));
	}

}
