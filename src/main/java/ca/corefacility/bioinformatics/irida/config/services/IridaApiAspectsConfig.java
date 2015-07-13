package ca.corefacility.bioinformatics.irida.config.services;

import javax.validation.Validator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import ca.corefacility.bioinformatics.irida.events.ProjectEventAspect;
import ca.corefacility.bioinformatics.irida.events.ProjectEventHandler;
import ca.corefacility.bioinformatics.irida.repositories.ProjectEventRepository;
import ca.corefacility.bioinformatics.irida.repositories.ProjectRepository;
import ca.corefacility.bioinformatics.irida.repositories.analysis.submission.AnalysisSubmissionRepository;
import ca.corefacility.bioinformatics.irida.repositories.joins.project.ProjectSampleJoinRepository;
import ca.corefacility.bioinformatics.irida.service.analysis.execution.AnalysisExecutionServiceAspect;
import ca.corefacility.bioinformatics.irida.validators.ValidMethodParametersAspect;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class IridaApiAspectsConfig {

	@Bean
	public ValidMethodParametersAspect validMethodsParametersAspect(Validator validator) {
		return new ValidMethodParametersAspect(validator);
	}

	@Bean
	public ProjectEventAspect projectEventAspect(ProjectEventRepository eventRepository,
			ProjectSampleJoinRepository psjRepository, ProjectRepository projectRepository) {
		return new ProjectEventAspect(new ProjectEventHandler(eventRepository, psjRepository, projectRepository));
	}

	@Bean
	public AnalysisExecutionServiceAspect analysisExecutionServiceAspect(
			AnalysisSubmissionRepository analysisSubmissionRepository) {
		return new AnalysisExecutionServiceAspect(analysisSubmissionRepository);
	}
}
