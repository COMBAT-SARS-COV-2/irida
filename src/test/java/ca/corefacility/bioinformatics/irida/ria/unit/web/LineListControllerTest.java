package ca.corefacility.bioinformatics.irida.ria.unit.web;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.MessageSource;

import ca.corefacility.bioinformatics.irida.ria.web.linelist.LineListController;
import ca.corefacility.bioinformatics.irida.security.permissions.project.ProjectOwnerPermission;
import ca.corefacility.bioinformatics.irida.security.permissions.sample.UpdateSamplePermission;
import ca.corefacility.bioinformatics.irida.service.ProjectService;
import ca.corefacility.bioinformatics.irida.service.sample.MetadataTemplateService;
import ca.corefacility.bioinformatics.irida.service.sample.SampleService;

import static org.mockito.Mockito.mock;

/**
 * Unit test for {@link LineListController}
 */
public class LineListControllerTest {
	private LineListController lineListController;
	private ProjectService projectService;
	private MetadataTemplateService metadataTemplateService;
	private SampleService sampleService;
	private UpdateSamplePermission updateSamplePermission;
	private ProjectOwnerPermission ownerPermission;
	private MessageSource messageSource;

	@Before
	public void setUp() {
		projectService = mock(ProjectService.class);
		sampleService = mock(SampleService.class);
		metadataTemplateService = mock(MetadataTemplateService.class);
		updateSamplePermission = mock(UpdateSamplePermission.class);
		messageSource = mock(MessageSource.class);
		ownerPermission = mock(ProjectOwnerPermission.class);
		lineListController = new LineListController(projectService, sampleService, metadataTemplateService,
				updateSamplePermission, ownerPermission, messageSource);
	}

	@Test
	public void testGetProjectMetadataTemplateFields() {
		long projectId = 1L;
//		lineListController.getProjectMetadataTemplateFields(projectId, Locale.ENGLISH);
//		verify(projectService, times(1)).read(projectId);
//		verify(metadataTemplateService, times(1)).getMetadataFieldsForProject(any(Project.class));
	}

	@Test
	public void testGetAllProjectMetadataEntries() {
//		long projectId = 1L;
//		lineListController.getProjectSamplesMetadataEntries(projectId, new ArrayList<>());
//		verify(sampleService, times(1)).getSamplesForProject(any(Project.class));
	}
}
