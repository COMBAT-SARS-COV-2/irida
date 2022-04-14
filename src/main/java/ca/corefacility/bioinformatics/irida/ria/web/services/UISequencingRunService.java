package ca.corefacility.bioinformatics.irida.ria.web.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import ca.corefacility.bioinformatics.irida.model.run.SequencingRun;
import ca.corefacility.bioinformatics.irida.ria.web.models.tables.TableResponse;
import ca.corefacility.bioinformatics.irida.ria.web.sequencingRuns.dto.SequencingRunModel;
import ca.corefacility.bioinformatics.irida.ria.web.sequencingRuns.dto.SequencingRunsListRequest;
import ca.corefacility.bioinformatics.irida.service.SequencingRunService;

/**
 * UI Service for handling requests related to {@link SequencingRun}s
 */
@Component
public class UISequencingRunService {
	private final SequencingRunService sequencingRunService;
	private final MessageSource messageSource;

	@Autowired
	public UISequencingRunService(SequencingRunService sequencingRunService, MessageSource messageSource) {
		this.sequencingRunService = sequencingRunService;
		this.messageSource = messageSource;
	}

	/**
	 * Get the details for a specific sequencing run.
	 *
	 * @param runId - the id of the sequencing run
	 * @return {@link SequencingRun}
	 */
	public SequencingRun getSequencingRun(Long runId) {
		return sequencingRunService.read(runId);
	}
	
	/**
	 * Get the current page contents for a table displaying sequencing runs.
	 *
	 * @param sequencingRunsListRequest {@link SequencingRunsListRequest} specifies what data is required.
	 * @param locale                    {@link Locale}
	 * @return {@link TableResponse}
	 */
	public TableResponse<SequencingRunModel> listSequencingRuns(SequencingRunsListRequest sequencingRunsListRequest,
			Locale locale) {
		Page<SequencingRun> list = sequencingRunService.list(sequencingRunsListRequest.getCurrent(),
				sequencingRunsListRequest.getPageSize(), sequencingRunsListRequest.getSort());

		List<SequencingRunModel> runs = new ArrayList<>();
		for (SequencingRun run : list.getContent()) {
			runs.add(new SequencingRunModel(run,
					messageSource.getMessage("sequencingruns.status." + run.getUploadStatus().toString(),
							new Object[] {}, locale)));
		}

		return new TableResponse<>(runs, list.getTotalElements());
	}
}

