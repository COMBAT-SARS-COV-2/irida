package ca.corefacility.bioinformatics.irida.ria.integration.clients;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.Dimension;
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
import ca.corefacility.bioinformatics.irida.ria.integration.pages.clients.ClientDetailsPage;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

/**
 * IT for the client details page
 * 
 * @author Thomas Matthews <thomas.matthews@phac-aspc.gc.ca>
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { IridaApiJdbcDataSourceConfig.class,
		IridaApiPropertyPlaceholderConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class })
@ActiveProfiles("it")
@DatabaseSetup("/ca/corefacility/bioinformatics/irida/ria/web/IridaClientDetailsServiceImplIT.xml")
@DatabaseTearDown("classpath:/ca/corefacility/bioinformatics/irida/test/integration/TableReset.xml")
public class ClientDetailsPageIT {
	private WebDriver driver;
	private ClientDetailsPage page;

	Long id = 1l;
	String clientId = "testClient";

	@Before
	public void setup() {
		driver = new PhantomJSDriver();
		driver.manage().window().setSize(new Dimension(1024, 900));
		LoginPage loginPage = LoginPage.to(driver);
		loginPage.doLogin();

		page = new ClientDetailsPage(driver, id);
	}

	@After
	public void destroy() {
		if (driver != null) {
			driver.close();
			driver.quit();
		}
	}

	@Test
	public void testCheckClientsPageNumber() {
		assertTrue(page.verifyClient(id, clientId));

	}

	@Test
	public void testDeleteClient() {
		page.clickDeleteButton();
		page.confirmDelete();
		assertTrue(page.checkDeleteSuccess());
	}

}
