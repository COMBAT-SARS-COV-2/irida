package ca.corefacility.bioinformatics.irida.service.analysis.execution.galaxy.phylogenomics.impl;

import ca.corefacility.bioinformatics.irida.model.workflow.analysis.AnalysisPhylogenomicsPipeline;
import ca.corefacility.bioinformatics.irida.model.workflow.galaxy.phylogenomics.RemoteWorkflowPhylogenomics;
import ca.corefacility.bioinformatics.irida.model.workflow.submission.galaxy.phylogenomics.AnalysisSubmissionPhylogenomics;
import ca.corefacility.bioinformatics.irida.pipeline.upload.galaxy.GalaxyHistoriesService;
import ca.corefacility.bioinformatics.irida.pipeline.upload.galaxy.GalaxyWorkflowService;
import ca.corefacility.bioinformatics.irida.service.analysis.execution.galaxy.AnalysisExecutionServiceGalaxy;
import ca.corefacility.bioinformatics.irida.service.analysis.workspace.galaxy.phylogenomics.impl.WorkspaceServicePhylogenomics;

/**
 * An execution service for performing a Phylogenomics Pipeline analysis.
 * 
 * @author Aaron Petkau <aaron.petkau@phac-aspc.gc.ca>
 *
 */
public class AnalysisExecutionServicePhylogenomics
		extends
		AnalysisExecutionServiceGalaxy<AnalysisPhylogenomicsPipeline, WorkspaceServicePhylogenomics, RemoteWorkflowPhylogenomics, AnalysisSubmissionPhylogenomics> {

	/**
	 * Builds a new Phylogenomis Pipeline analysis with the given service
	 * classes.
	 * 
	 * @param galaxyWorkflowService
	 *            A GalaxyWorkflowService for interacting with Galaxy workflows.
	 * @param galaxyHistoriesService
	 *            A GalaxyHistoriesService for interacting with Galaxy
	 *            Histories.
	 * @param workspaceService
	 *            A PreparationService for preparing workflows.
	 */
	public AnalysisExecutionServicePhylogenomics(
			GalaxyWorkflowService galaxyWorkflowService,
			GalaxyHistoriesService galaxyHistoriesService,
			WorkspaceServicePhylogenomics workspaceService) {
		super(galaxyWorkflowService, galaxyHistoriesService, workspaceService);
	}
}
