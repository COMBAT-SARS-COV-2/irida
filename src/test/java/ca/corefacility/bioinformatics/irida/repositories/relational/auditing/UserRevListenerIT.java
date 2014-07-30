package ca.corefacility.bioinformatics.irida.repositories.relational.auditing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.hibernate.envers.AuditReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.history.Revision;
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
import ca.corefacility.bioinformatics.irida.model.Project;
import ca.corefacility.bioinformatics.irida.repositories.ProjectRepository;
import ca.corefacility.bioinformatics.irida.security.annotations.WithMockOAuth2Client;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { IridaApiServicesConfig.class,
		IridaApiTestDataSourceConfig.class, IridaApiTestMultithreadingConfig.class })
@ActiveProfiles("test")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class,
		WithSecurityContextTestExcecutionListener.class })
@DatabaseSetup("/ca/corefacility/bioinformatics/irida/repositories/relational/auditing/UserRevListenerIT.xml")
@DatabaseTearDown("/ca/corefacility/bioinformatics/irida/test/integration/TableReset.xml")
public class UserRevListenerIT {
	@Autowired
	ProjectRepository projectRepository;

	@Autowired
	AuditReader auditReader;

	@Test
	@WithMockOAuth2Client(clientId = "testClient", username = "fbristow", password = "Password1")
	public void testModifyWithOAuth2() {

		Project read = projectRepository.findOne(1l);
		read.setName("A new name");
		projectRepository.save(read);

		Revision<Integer, Project> findLastChangeRevision = projectRepository.findLastChangeRevision(read.getId());
		UserRevEntity findRevision = auditReader.findRevision(UserRevEntity.class,
				findLastChangeRevision.getRevisionNumber());
		assertEquals("client id should be set in revision", new Long(1), findRevision.getClientId());
	}

	@Test
	@WithMockUser(username = "fbristow", password = "Password1")
	public void testModifyWithUsernamePassword() {
		Project read = projectRepository.findOne(1l);
		read.setName("A new name");
		projectRepository.save(read);

		Revision<Integer, Project> findLastChangeRevision = projectRepository.findLastChangeRevision(read.getId());
		UserRevEntity findRevision = auditReader.findRevision(UserRevEntity.class,
				findLastChangeRevision.getRevisionNumber());
		assertNull(findRevision.getClientId());
	}
}
