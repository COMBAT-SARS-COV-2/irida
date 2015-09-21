package ca.corefacility.bioinformatics.irida.config.workflow;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;

import ca.corefacility.bioinformatics.irida.exceptions.IridaWorkflowException;
import ca.corefacility.bioinformatics.irida.model.enums.AnalysisType;
import ca.corefacility.bioinformatics.irida.model.workflow.IridaWorkflow;
import ca.corefacility.bioinformatics.irida.model.workflow.description.IridaWorkflowToolRepository;
import ca.corefacility.bioinformatics.irida.pipeline.upload.galaxy.integration.LocalGalaxy;
import ca.corefacility.bioinformatics.irida.service.workflow.IridaWorkflowLoaderService;
import ca.corefacility.bioinformatics.irida.service.workflow.IridaWorkflowsService;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;
import com.github.jmchilton.blend4j.galaxy.ToolShedRepositoriesClient;
import com.github.jmchilton.blend4j.galaxy.beans.InstalledRepository;
import com.github.jmchilton.blend4j.galaxy.beans.InstalledRepository.InstallationStatus;
import com.github.jmchilton.blend4j.galaxy.beans.RepositoryInstall;

/**
 * Class used configure workflows in Galaxy for integration testing.
 * 
 *
 */
@Configuration
@Profile("test")
public class IridaWorkflowsGalaxyIntegrationTestConfig {

	private static final Logger logger = LoggerFactory.getLogger(IridaWorkflowsGalaxyIntegrationTestConfig.class);

	@Autowired
	private LocalGalaxy localGalaxy;

	@Autowired
	private IridaWorkflowLoaderService iridaWorkflowLoaderService;

	@Autowired
	private IridaWorkflowsService iridaWorkflowsService;

	private UUID snvPhylWorkflowId = UUID.fromString("3fd2719d-8729-4e91-bd01-c6c20b99874d");

	/**
	 * Registers a production SNVPhyl workflow for testing.
	 * 
	 * @return A production {@link IridaWorkflow} for testing.
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws IridaWorkflowException
	 */
	@Lazy
	@Bean
	public IridaWorkflow snvPhylWorkflow() throws IOException, URISyntaxException, IridaWorkflowException {
		Path snvPhylProductionPath = Paths.get(AnalysisType.class.getResource("workflows/SNVPhyl").toURI());

		Set<IridaWorkflow> snvPhylWorkflows = iridaWorkflowLoaderService
				.loadAllWorkflowImplementations(snvPhylProductionPath);
		iridaWorkflowsService.registerWorkflows(snvPhylWorkflows);

		IridaWorkflow snvPhylWorkflow = iridaWorkflowsService.getIridaWorkflow(snvPhylWorkflowId);
		importAllTools(localGalaxy.getGalaxyInstanceAdmin(), snvPhylWorkflow);

		return snvPhylWorkflow;
	}

	/**
	 * Imports all tools to Galaxy for the passed workflow.
	 * 
	 * @param galaxyInstance
	 *            The instance of Galaxy to import tools into.
	 * @param iridaWorkflow
	 *            The workflow to import all tools for.
	 * @throws IridaWorkflowException
	 */
	private void importAllTools(GalaxyInstance galaxyInstance, IridaWorkflow iridaWorkflow) throws IridaWorkflowException {
		for (IridaWorkflowToolRepository workflowTool : iridaWorkflow.getWorkflowDescription().getToolRepositories()) {
			ToolShedRepositoriesClient toolRepositoriesClient = galaxyInstance.getRepositoriesClient();

			RepositoryInstall toolInstall = new RepositoryInstall();
			toolInstall.setName(workflowTool.getName());
			toolInstall.setOwner(workflowTool.getOwner());
			toolInstall.setToolShedUrl(workflowTool.getUrl().toString());
			toolInstall.setChangsetRevision(workflowTool.getRevision());
			toolInstall.setInstallRepositoryDependencies(true);
			toolInstall.setInstallToolDependencies(true);

			logger.debug("Installing tool " + workflowTool);
			List<InstalledRepository> installedRepositories = toolRepositoriesClient.installRepository(toolInstall);
			for (InstalledRepository installedRepository : installedRepositories) {
				InstallationStatus status = installedRepository.getInstallationStatus();
				logger.debug("Installation status=" + status + " for tool " + workflowTool);
				if (status.equals(InstallationStatus.ERROR)) {
					// don't even try to proceed with tests if you can't install the required tools.
					throw new IridaWorkflowException("Failed to install tool [" + workflowTool
							+ "], possible reason: [" + installedRepository.getErrorMessage() + "]");
				}
			}
		}
	}
}
