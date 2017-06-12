package ca.corefacility.bioinformatics.irida.ria.unit.web.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;

import com.google.common.collect.Lists;

import ca.corefacility.bioinformatics.irida.exceptions.IridaWorkflowNotFoundException;
import ca.corefacility.bioinformatics.irida.model.enums.AnalysisState;
import ca.corefacility.bioinformatics.irida.model.enums.AnalysisType;
import ca.corefacility.bioinformatics.irida.model.workflow.IridaWorkflow;
import ca.corefacility.bioinformatics.irida.model.workflow.description.IridaWorkflowDescription;
import ca.corefacility.bioinformatics.irida.model.workflow.description.IridaWorkflowInput;
import ca.corefacility.bioinformatics.irida.model.workflow.submission.AnalysisSubmission;
import ca.corefacility.bioinformatics.irida.ria.unit.TestDataFactory;
import ca.corefacility.bioinformatics.irida.ria.web.analysis.AnalysisController;
import ca.corefacility.bioinformatics.irida.security.permissions.UpdateAnalysisSubmissionPermission;
import ca.corefacility.bioinformatics.irida.service.AnalysisSubmissionService;
import ca.corefacility.bioinformatics.irida.service.ProjectService;
import ca.corefacility.bioinformatics.irida.service.sample.MetadataTemplateService;
import ca.corefacility.bioinformatics.irida.service.sample.SampleService;
import ca.corefacility.bioinformatics.irida.service.user.UserService;
import ca.corefacility.bioinformatics.irida.service.workflow.IridaWorkflowsService;

/**
 */
public class AnalysisControllerTest {
	/*
	 * CONTROLLER
	 */
	private AnalysisController analysisController;

	/*
	 * SERVICES
	 */
	private AnalysisSubmissionService analysisSubmissionServiceMock;
	private IridaWorkflowsService iridaWorkflowsServiceMock;
	private UserService userServiceMock;
	private ProjectService projectServiceMock;
	private SampleService sampleService;
	private UpdateAnalysisSubmissionPermission updatePermission;
	private MetadataTemplateService metadataTemplateService;

	@Before
	public void init() {
		analysisSubmissionServiceMock = mock(AnalysisSubmissionService.class);
		iridaWorkflowsServiceMock = mock(IridaWorkflowsService.class);
		projectServiceMock = mock(ProjectService.class);
		updatePermission = mock(UpdateAnalysisSubmissionPermission.class);
		sampleService = mock(SampleService.class);
		MessageSource messageSourceMock = mock(MessageSource.class);
		analysisController = new AnalysisController(analysisSubmissionServiceMock, iridaWorkflowsServiceMock,
				userServiceMock, sampleService, projectServiceMock, updatePermission, metadataTemplateService,
				messageSourceMock);
	}

	@Test
	public void testGetAnalysisDetailsTree() throws IOException, IridaWorkflowNotFoundException {
		Long submissionId = 1L;
		ExtendedModelMap model = new ExtendedModelMap();
		Locale locale = Locale.ENGLISH;

		final IridaWorkflowInput input = new IridaWorkflowInput("single", "paired", "reference", true);
		AnalysisSubmission submission = TestDataFactory.constructAnalysisSubmission();
		IridaWorkflowDescription description = new IridaWorkflowDescription(submission.getWorkflowId(), "My Workflow",
				"V1", AnalysisType.PHYLOGENOMICS, input, Lists.newArrayList(), Lists.newArrayList(),
				Lists.newArrayList());
		IridaWorkflow iridaWorkflow = new IridaWorkflow(description, null);
		submission.setAnalysisState(AnalysisState.COMPLETED);

		when(analysisSubmissionServiceMock.read(submissionId)).thenReturn(submission);
		when(iridaWorkflowsServiceMock.getIridaWorkflow(submission.getWorkflowId())).thenReturn(iridaWorkflow);

		String detailsPage = analysisController.getDetailsPage(submissionId, model, locale);
		assertEquals("should be details page", AnalysisController.PAGE_DETAILS_DIRECTORY + "tree", detailsPage);

		assertEquals("Tree preview should be set", "tree", model.get("preview"));

		assertEquals("submission should be in model", submission, model.get("analysisSubmission"));
		
		assertEquals("submission reference file should be in model.", submission.getReferenceFile().get(), model.get("referenceFile"));
	}

	@Test
	public void testGetAnalysisDetailsNotCompleted() throws IOException, IridaWorkflowNotFoundException {
		Long submissionId = 1L;
		ExtendedModelMap model = new ExtendedModelMap();
		Locale locale = Locale.ENGLISH;

		final IridaWorkflowInput input = new IridaWorkflowInput("single", "paired", "reference", true);
		AnalysisSubmission submission = TestDataFactory.constructAnalysisSubmission();
		IridaWorkflowDescription description = new IridaWorkflowDescription(submission.getWorkflowId(), "My Workflow",
				"V1", AnalysisType.PHYLOGENOMICS, input, Lists.newArrayList(), Lists.newArrayList(),
				Lists.newArrayList());
		IridaWorkflow iridaWorkflow = new IridaWorkflow(description, null);
		submission.setAnalysisState(AnalysisState.RUNNING);

		when(analysisSubmissionServiceMock.read(submissionId)).thenReturn(submission);
		when(iridaWorkflowsServiceMock.getIridaWorkflow(submission.getWorkflowId())).thenReturn(iridaWorkflow);

		String detailsPage = analysisController.getDetailsPage(submissionId, model, locale);
		assertEquals("should be details page", AnalysisController.PAGE_DETAILS_DIRECTORY + "tree", detailsPage);

		assertFalse("No preview should be available", model.containsAttribute("preview"));

		assertEquals("submission should be in model", submission, model.get("analysisSubmission"));
	}

	// ************************************************************************************************
	// AJAX TESTS
	// ************************************************************************************************

	@Test
	public void TestGetAjaxDownloadAnalysisSubmission() {
		Long analysisSubmissionId = 1L;
		MockHttpServletResponse response = new MockHttpServletResponse();

		when(analysisSubmissionServiceMock.read(analysisSubmissionId)).thenReturn(
				TestDataFactory.constructAnalysisSubmission());
		try {
			analysisController.getAjaxDownloadAnalysisSubmission(analysisSubmissionId, response);
			assertEquals("Has the correct content type", "application/zip", response.getContentType());
			assertEquals("Has the correct 'Content-Disposition' headers", "attachment;filename=submission-5.zip",
					response.getHeader("Content-Disposition"));
		} catch (final Exception e) {
			fail();
		}
	}
}
