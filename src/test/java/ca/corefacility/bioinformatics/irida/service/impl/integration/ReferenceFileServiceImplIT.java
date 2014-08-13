package ca.corefacility.bioinformatics.irida.service.impl.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Path;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExcecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import ca.corefacility.bioinformatics.irida.config.IridaApiServicesConfig;
import ca.corefacility.bioinformatics.irida.config.data.IridaApiTestDataSourceConfig;
import ca.corefacility.bioinformatics.irida.config.processing.IridaApiTestMultithreadingConfig;
import ca.corefacility.bioinformatics.irida.model.joins.Join;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.project.ReferenceFile;
import ca.corefacility.bioinformatics.irida.service.ProjectService;
import ca.corefacility.bioinformatics.irida.service.ReferenceFileService;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { IridaApiServicesConfig.class,
		IridaApiTestDataSourceConfig.class, IridaApiTestMultithreadingConfig.class })
@ActiveProfiles("test")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class,
		WithSecurityContextTestExcecutionListener.class })
@DatabaseSetup("/ca/corefacility/bioinformatics/irida/service/impl/ReferenceFileServiceImplIT.xml")
@DatabaseTearDown("/ca/corefacility/bioinformatics/irida/test/integration/TableReset.xml")
public class ReferenceFileServiceImplIT {

	@Autowired
	private ProjectService projectService;

	@Autowired
	private ReferenceFileService referenceFileService;

	@Autowired
	@Qualifier("referenceFileBaseDirectory")
	private Path referenceFileBaseDirectory;

	@Test
	@WithMockUser(username = "fbristow", roles = "ADMIN")
	public void testGetReferenceFilesForProject() {
		Project p = projectService.read(1L);
		List<Join<Project, ReferenceFile>> prs = referenceFileService.getReferenceFilesForProject(p);
		assertEquals("Wrong number of reference files for project.", 1, prs.size());
		ReferenceFile rf = prs.iterator().next().getObject();
		assertEquals("Wrong reference file attached to project.", Long.valueOf(1), rf.getId());
	}

	@Test(expected = AccessDeniedException.class)
	@WithMockUser(username = "user", roles = "USER")
	public void testReadNotAllowed() {
		referenceFileService.read(1l);
	}

	@Test
	@WithMockUser(username = "fbristow", roles = "USER")
	public void testRead() {
		ReferenceFile read = referenceFileService.read(1l);
		assertNotNull(read);
	}
}
