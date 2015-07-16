package ca.corefacility.bioinformatics.irida.service.export;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.google.common.collect.ImmutableMap;

import ca.corefacility.bioinformatics.irida.model.NcbiExportSubmission;
import ca.corefacility.bioinformatics.irida.model.enums.ExportUploadState;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequenceFilePair;

/**
 * Class which handles uploading a {@link NcbiExportSubmission} to NCBI
 */
@Service
public class ExportUploadService {
	private static final Logger logger = LoggerFactory.getLogger(ExportUploadService.class);

	private static final String NCBI_TEMPLATE = "ncbi";

	private NcbiExportSubmissionService exportSubmissionService;
	private TemplateEngine templateEngine;

	@Autowired
	public ExportUploadService(NcbiExportSubmissionService exportSubmissionService,
			@Qualifier("exportUploadTemplateEngine") TemplateEngine templateEngine) {
		this.exportSubmissionService = exportSubmissionService;
		this.templateEngine = templateEngine;
	}

	/**
	 * Check for new {@link NcbiExportSubmission}s to be uploaded and begin
	 * their upload
	 */
	public synchronized void launchUpload() {

		logger.trace("Getting new exports");

		List<NcbiExportSubmission> submissionsWithState = exportSubmissionService
				.getSubmissionsWithState(ExportUploadState.NEW);

		for (NcbiExportSubmission submission : submissionsWithState) {

			logger.trace("Updating submission " + submission.getId());

			submission = exportSubmissionService.update(submission.getId(),
					ImmutableMap.of("uploadState", ExportUploadState.PROCESSING));

			logger.trace("Going to sleep " + submission.getId());

			try {
				Thread.sleep(30000);
				createXml(submission);
			} catch (InterruptedException e) {

			}

			logger.trace("Finished sleep " + submission.getId());

			submission = exportSubmissionService.update(submission.getId(),
					ImmutableMap.of("uploadState", ExportUploadState.COMPLETE));
		}

	}

	public void createXml(NcbiExportSubmission submission) {
		final Context ctx = new Context();
		List<SequenceFilePair> pairFiles = submission.getPairFiles();

		ctx.setVariable("pairFiles", pairFiles);

		final String htmlContent = templateEngine.process(NCBI_TEMPLATE, ctx);

		logger.debug(htmlContent);
	}
}
