package ca.corefacility.bioinformatics.irida.service.analysis.execution.galaxy;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.transaction.annotation.Transactional;

import ca.corefacility.bioinformatics.irida.exceptions.ExecutionManagerException;
import ca.corefacility.bioinformatics.irida.model.enums.AnalysisCleanedState;
import ca.corefacility.bioinformatics.irida.model.workflow.submission.AnalysisSubmission;
import ca.corefacility.bioinformatics.irida.pipeline.upload.galaxy.GalaxyHistoriesService;
import ca.corefacility.bioinformatics.irida.pipeline.upload.galaxy.GalaxyLibrariesService;
import ca.corefacility.bioinformatics.irida.pipeline.upload.galaxy.GalaxyWorkflowService;
import ca.corefacility.bioinformatics.irida.repositories.analysis.submission.AnalysisSubmissionRepository;

/**
 * Service for cleaning up an {@link AnalysisSubmission} within a Galaxy
 * execution manager asynchronously.
 * 
 */
@Async("analysisTaskExecutor")
public class AnalysisExecutionServiceGalaxyCleanupAsync {

	private static final Logger logger = LoggerFactory.getLogger(AnalysisExecutionServiceGalaxyCleanupAsync.class);

	private final AnalysisSubmissionRepository analysisSubmissionRepository;
	private final GalaxyWorkflowService galaxyWorkflowService;
	private final GalaxyHistoriesService galaxyHistoriesService;
	private final GalaxyLibrariesService galaxyLibrariesService;

	/**
	 * Builds a new {@link AnalysisExecutionServiceGalaxyCleanupAsync} with the
	 * given information.
	 * 
	 * @param analysisSubmissionRepository
	 *            A service for analysis submissions.
	 * @param galaxyWorkflowService
	 *            A service for Galaxy workflows.
	 * @param galaxyHistoriesService
	 *            A service for Galaxy histories.
	 * @param galaxyLibrariesService
	 *            A service for Galaxy libraries.
	 */
	@Autowired
	public AnalysisExecutionServiceGalaxyCleanupAsync(AnalysisSubmissionRepository analysisSubmissionRepository,
			GalaxyWorkflowService galaxyWorkflowService, GalaxyHistoriesService galaxyHistoriesService,
			GalaxyLibrariesService galaxyLibrariesService) {
		this.analysisSubmissionRepository = analysisSubmissionRepository;
		this.galaxyWorkflowService = galaxyWorkflowService;
		this.galaxyHistoriesService = galaxyHistoriesService;
		this.galaxyLibrariesService = galaxyLibrariesService;
	}

	/**
	 * Cleans up any intermediate files from this {@link AnalysisSubmission} in
	 * the execution manager.
	 * 
	 * @param analysisSubmission
	 *            The {@link AnalysisSubmission} to clean.
	 * @return A {@link Future} with an {@link AnalysisSubmission} object that
	 *         will be cleaned.
	 * @throws ExecutionManagerException
	 *             If there was an error while cleaning up files in the
	 *             execution manager.
	 */
	@Transactional
	public Future<AnalysisSubmission> cleanupSubmission(AnalysisSubmission analysisSubmission)
			throws ExecutionManagerException {
		checkNotNull(analysisSubmission, "analysisSubmission is null");
		checkArgument(AnalysisCleanedState.CLEANING.equals(analysisSubmission.getAnalysisCleanedState()),
				"analysisCleanedState not " + AnalysisCleanedState.CLEANING);

		logger.debug("Cleaning up " + analysisSubmission);

		// If the submission is in an error state these remote ids aren't
		// guaranteed to exist.
		if (analysisSubmission.hasRemoteAnalysisId()) {
			logger.trace("remoteAnalysisId=" + analysisSubmission.getRemoteAnalysisId() + " exists, cleaning");
			galaxyHistoriesService.deleteHistory(analysisSubmission.getRemoteAnalysisId());
		}

		if (analysisSubmission.hasRemoteInputDataId()) {
			logger.trace("remoteInputDataId=" + analysisSubmission.getRemoteInputDataId() + " exists, cleaning");
			galaxyLibrariesService.deleteLibrary(analysisSubmission.getRemoteInputDataId());
		}

		if (analysisSubmission.hasRemoteWorkflowId()) {
			logger.trace("remoteWorkflowId=" + analysisSubmission.getRemoteWorkflowId() + " exists, cleaning");
			galaxyWorkflowService.deleteWorkflow(analysisSubmission.getRemoteWorkflowId());
		}

		analysisSubmission.setAnalysisCleanedState(AnalysisCleanedState.CLEANED);
		AnalysisSubmission cleanedAnalysis = analysisSubmissionRepository.save(analysisSubmission);

		return new AsyncResult<>(cleanedAnalysis);
	}
}
