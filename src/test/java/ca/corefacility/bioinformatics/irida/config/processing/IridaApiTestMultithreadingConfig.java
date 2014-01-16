package ca.corefacility.bioinformatics.irida.config.processing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@Configuration
@Profile("test")
public class IridaApiTestMultithreadingConfig {
	@Bean(name = "fileProcessingChainExecutor")
	@Description("In test, we actually just want to run in the current thread, don't bother making a thread pool.")
	public TaskExecutor fileProcessingChainExecutor() {
		return new SyncTaskExecutor();
	}
}
