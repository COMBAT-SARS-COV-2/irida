package ca.corefacility.bioinformatics.irida.ria.integration.projects;

import ca.corefacility.bioinformatics.irida.config.IridaApiPropertyPlaceholderConfig;
import ca.corefacility.bioinformatics.irida.config.data.IridaApiJdbcDataSourceConfig;
import ca.corefacility.bioinformatics.irida.ria.integration.pages.LoginPage;
import ca.corefacility.bioinformatics.irida.ria.integration.pages.projects.ProjectMetadataEditPage;
import ca.corefacility.bioinformatics.irida.ria.integration.pages.projects.ProjectMetadataPage;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
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

import static org.junit.Assert.assertEquals;

/**
 * Edit Project Metadata Integration Test
 *
 * @author Josh Adam <josh.adam@phac-aspc.gc.ca>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {IridaApiJdbcDataSourceConfig.class,
        IridaApiPropertyPlaceholderConfig.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class})
@ActiveProfiles("it")
@DatabaseSetup("/ca/corefacility/bioinformatics/irida/ria/web/ProjectsPageIT.xml")
@DatabaseTearDown("classpath:/ca/corefacility/bioinformatics/irida/test/integration/TableReset.xml")
public class ProjectMetadataEditPageIT {
    public static final String GOOD_PROJECT_NAME = "MY GOOD NAME";
    public static final String GOOD_PROJECT_ORGANISM = "Mr. Good Bug";
    public static final String GOOD_PROJECT_DESCRIPTION = "New project description.";
    public static final String GOOD_PROJECT_REMOTEURL = "http://ghost.ca";
    public static final String BAD_PROJECT_URL = "bad_url";
    private final String PAGE_TITLE = "IRIDA Platform - project - Metadata";
    private final Long PROJECT_ID_OWNER = 1L;
    private final Long PROJECT_ID_COLLABORATOR = 6L;
    private final String PROJECT_NAME = "project";
    private final String PROJECT_DESCRIPTION = "This is an interesting project description.";
    private final String PROJECT_ORGANISM = "E. coli";
    private final String PROJECT_REMOTE_URL = "http://google.ca";

    private WebDriver driver;
    private ProjectMetadataEditPage page;

    @Before
    public void setUp() {
        this.driver = new PhantomJSDriver();
        LoginPage loginPage = LoginPage.to(driver);
        loginPage.doLogin();
        page = new ProjectMetadataEditPage(driver);
    }

    @After
    public void destroy() {
        if (driver != null) {
            driver.close();
            driver.quit();
        }
    }

    @Test
    public void pageCreateCorrectly() {
        driver.get("http://localhost:8080/projects/" + PROJECT_ID_OWNER + "/metadata/edit");
        assertEquals("Contains a placeholder with the project name", PROJECT_NAME, page.getNamePlaceholder());
        assertEquals("Contains a placeholder with the project organism", PROJECT_ORGANISM, page.getOrganismPlaceholder());
        assertEquals("Contains a placeholder with the project description", PROJECT_DESCRIPTION, page.getDescriptionPlaceholder());
        assertEquals("Contains a placeholder with the project remoteURL", PROJECT_REMOTE_URL, page.getRemoteURLPlaceholder());
    }

    @Test
    public void canUpdateProjectInformation() {
        driver.get("http://localhost:8080/projects/" + PROJECT_ID_OWNER + "/metadata/edit");
        page.updateProject(GOOD_PROJECT_NAME, GOOD_PROJECT_ORGANISM, GOOD_PROJECT_DESCRIPTION, GOOD_PROJECT_REMOTEURL);
        assertEquals("Redirects to the metadata page", driver.getCurrentUrl(), "http://localhost:8080/projects/" + PROJECT_ID_OWNER + "/metadata");

        ProjectMetadataPage metadataPage = new ProjectMetadataPage(driver);
        driver.get("http://localhost:8080/projects/" + PROJECT_ID_OWNER + "/metadata");
        assertEquals("Updated the project name", GOOD_PROJECT_NAME, metadataPage.getDataProjectName());
        assertEquals("Updated the oragnism", GOOD_PROJECT_ORGANISM, metadataPage.getDataProjectOrganism());
        assertEquals("Updated the description", GOOD_PROJECT_DESCRIPTION, metadataPage.getDataProjectDescription());
        assertEquals("Updated the remoteULR", GOOD_PROJECT_REMOTEURL, metadataPage.getDataProjectRemoteURL());
    }

    @Test
    public void errorsIfBadProjectInformation() {
        String currentURL = "http://localhost:8080/projects/" + PROJECT_ID_OWNER + "/metadata/edit";
        driver.get(currentURL);
        page.updateProject(GOOD_PROJECT_NAME, GOOD_PROJECT_ORGANISM, GOOD_PROJECT_DESCRIPTION, BAD_PROJECT_URL);
        assertEquals("Remains on the same page", driver.getCurrentUrl(), currentURL);
    }
}
