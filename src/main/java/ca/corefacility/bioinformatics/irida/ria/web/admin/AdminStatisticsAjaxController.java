package ca.corefacility.bioinformatics.irida.ria.web.admin;

import java.util.Date;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ca.corefacility.bioinformatics.irida.ria.web.admin.dto.BasicStats;
import ca.corefacility.bioinformatics.irida.service.AnalysisSubmissionService;
import ca.corefacility.bioinformatics.irida.service.ProjectService;
import ca.corefacility.bioinformatics.irida.service.sample.SampleService;
import ca.corefacility.bioinformatics.irida.service.user.UserService;


/**
 * Controller to handle ajax requests for the Admin Panel statistics page.
 */

@RestController
@Scope("session")
@RequestMapping("/ajax/admin")
public class AdminStatisticsAjaxController {

	private ProjectService projectService;
	private UserService userService;
	private SampleService sampleService;
	private AnalysisSubmissionService analysisSubmissionService;

	@Autowired
	public AdminStatisticsAjaxController(ProjectService projectService, UserService userService,
			SampleService sampleService, AnalysisSubmissionService analysisSubmissionService) {
		this.projectService = projectService;
		this.userService = userService;
		this.sampleService = sampleService;
		this.analysisSubmissionService = analysisSubmissionService;
	}

	/**
	 * Get basic usage statistics for projects, samples, analyses, and users
	 * for the provided time period
	 *
	 * @param timePeriod The time period for which to retrieve usage stats for
	 * @return dto with basic usage stats
	 */
	@RequestMapping(value = "/statistics", method = RequestMethod.GET)
	public ResponseEntity<BasicStats> getAdminStatistics(Integer timePeriod) {
		Date currDate = new Date();
		Date minimumCreatedDate = new DateTime(currDate).minusDays(timePeriod)
				.toDate();

		Long analysesRan = analysisSubmissionService.getAnalysesRan(minimumCreatedDate);
		Long projectsCreated = projectService.getProjectsCreated(minimumCreatedDate);
		Long samplesCreated = sampleService.getSamplesCreated(minimumCreatedDate);
		Long usersCreated = userService.getUsersCreatedInTimePeriod(minimumCreatedDate);
		Long usersLoggedIn = userService.getUsersLoggedIn(minimumCreatedDate);

		return ResponseEntity.ok(new BasicStats(analysesRan, projectsCreated, samplesCreated, usersCreated, usersLoggedIn));
	}

	/**
	 * Get updated usage statistics for projects for the provided time period
	 *
	 * @param timePeriod The time period for which to retrieve updated project usage stats for
	 * @return dto with updated project usage stats
	 */
	@RequestMapping(value = "/project-statistics", method = RequestMethod.GET)
	public ResponseEntity getAdminProjectStatistics(Long timePeriod) {
		return ResponseEntity.ok("Retrieved stats for projects created in the last " + timePeriod);
	}

	/**
	 * Get updated usage statistics for users for the provided time period
	 *
	 * @param timePeriod The time period for which to retrieve updated user usage stats for
	 * @return dto with updated user usage stats
	 */
	@RequestMapping(value = "/user-statistics", method = RequestMethod.GET)
	public ResponseEntity getAdminUserStatistics(Long timePeriod) {
		return ResponseEntity.ok("Retrieved stats for users logged in the last " + timePeriod);
	}

	/**
	 * Get updated usage statistics for analyses for the provided time period
	 *
	 * @param timePeriod The time period for which to retrieve updated analyses usage stats for
	 * @return dto with updated analyses usage stats
	 */
	@RequestMapping(value = "/analyses-statistics", method = RequestMethod.GET)
	public ResponseEntity getAdminAnalysesStatistics(Long timePeriod) {
		return ResponseEntity.ok("Retrieved stats for analyses run in the last " + timePeriod);
	}

	/**
	 * Get updated usage statistics for samples for the provided time period
	 *
	 * @param timePeriod The time period for which to retrieve updated sample usage stats for
	 * @return dto with updated sample usage stats
	 */
	@RequestMapping(value = "/sample-statistics", method = RequestMethod.GET)
	public ResponseEntity getAdminSampleStatistics(Long timePeriod) {
		return ResponseEntity.ok("Retrieved stats for samples created in the last " + timePeriod);
	}
}