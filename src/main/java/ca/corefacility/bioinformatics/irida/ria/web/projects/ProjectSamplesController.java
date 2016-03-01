package ca.corefacility.bioinformatics.irida.ria.web.projects;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.format.Formatter;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ca.corefacility.bioinformatics.irida.exceptions.EntityExistsException;
import ca.corefacility.bioinformatics.irida.exceptions.EntityNotFoundException;
import ca.corefacility.bioinformatics.irida.model.enums.ProjectRole;
import ca.corefacility.bioinformatics.irida.model.joins.Join;
import ca.corefacility.bioinformatics.irida.model.joins.impl.ProjectUserJoin;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.SequenceFile;
import ca.corefacility.bioinformatics.irida.model.user.Role;
import ca.corefacility.bioinformatics.irida.model.user.User;
import ca.corefacility.bioinformatics.irida.repositories.specification.ProjectSpecification;
import ca.corefacility.bioinformatics.irida.repositories.specification.ProjectUserJoinSpecification;
import ca.corefacility.bioinformatics.irida.ria.utilities.converters.FileSizeConverter;
import ca.corefacility.bioinformatics.irida.service.ProjectService;
import ca.corefacility.bioinformatics.irida.service.SequenceFileService;
import ca.corefacility.bioinformatics.irida.service.sample.SampleService;
import ca.corefacility.bioinformatics.irida.service.user.UserService;
import ca.corefacility.bioinformatics.irida.web.controller.api.projects.RESTProjectSamplesController;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

@Controller
public class ProjectSamplesController {
	// From configuration.properties
	private @Value("${ngsarchive.linker.available}") Boolean LINKER_AVAILABLE;
	private @Value("${ngsarchive.linker.script}") String LINKER_SCRIPT;

	// Sub Navigation Strings
	private static final String ACTIVE_NAV = "activeNav";
	private static final String ACTIVE_NAV_SAMPLES = "samples";

	private static final String PROJECT_NAME_PROPERTY = "name";

	// private static final String ACTIVE_NAV_ANALYSIS = "analysis";

	// Page Names
	private static final String PROJECTS_DIR = "projects/";
	public static final String PROJECT_TEMPLATE_DIR = PROJECTS_DIR + "templates/";
	public static final String LIST_PROJECTS_PAGE = PROJECTS_DIR + "projects";
	public static final String PROJECT_MEMBERS_PAGE = PROJECTS_DIR + "project_members";
	public static final String SPECIFIC_PROJECT_PAGE = PROJECTS_DIR + "project_details";
	public static final String CREATE_NEW_PROJECT_PAGE = PROJECTS_DIR + "project_new";
	public static final String PROJECT_METADATA_PAGE = PROJECTS_DIR + "project_metadata";
	public static final String PROJECT_METADATA_EDIT_PAGE = PROJECTS_DIR + "project_metadata_edit";
	public static final String PROJECT_SAMPLES_PAGE = PROJECTS_DIR + "project_samples";
	private static final Logger logger = LoggerFactory.getLogger(ProjectsController.class);

	// Services
	private final ProjectService projectService;
	private final SampleService sampleService;
	private final UserService userService;
	private final SequenceFileService sequenceFileService;
	private final ProjectControllerUtils projectControllerUtils;
	private MessageSource messageSource;

	/*
	 * Converters
	 */
	Formatter<Date> dateFormatter;
	FileSizeConverter fileSizeConverter;

	@Autowired
	public ProjectSamplesController(ProjectService projectService, SampleService sampleService,
			UserService userService, SequenceFileService sequenceFileService,
			ProjectControllerUtils projectControllerUtils, MessageSource messageSource) {
		this.projectService = projectService;
		this.sampleService = sampleService;
		this.userService = userService;
		this.sequenceFileService = sequenceFileService;
		this.projectControllerUtils = projectControllerUtils;
		this.dateFormatter = new DateFormatter();
		this.fileSizeConverter = new FileSizeConverter();
		this.messageSource = messageSource;
	}

	/**
	 * Get the samples for a given project
	 *
	 * @param model
	 *            A model for the sample list view
	 * @param principal
	 *            The user reading the project
	 * @param projectId
	 *            The ID of the project
	 * @param httpSession
	 *            The user's session
	 *
	 * @return Name of the project samples list view
	 */
	@RequestMapping(value = { "/projects/{projectId}", "/projects/{projectId}/samples" })
	public String getProjectSamplesPage(final Model model, final Principal principal, @PathVariable long projectId,
			HttpSession httpSession) {
		Project project = projectService.read(projectId);
		model.addAttribute("project", project);

		// Set up the template information
		projectControllerUtils.getProjectTemplateDetails(model, principal, project);

		// Exporting functionality
		boolean haveGalaxyCallbackURL = (httpSession.getAttribute(ProjectsController.GALAXY_CALLBACK_VARIABLE_NAME) != null);
		model.addAttribute("linkerAvailable", LINKER_AVAILABLE);
		model.addAttribute("galaxyCallback", haveGalaxyCallbackURL);

		model.addAttribute(ACTIVE_NAV, ACTIVE_NAV_SAMPLES);
		return PROJECT_SAMPLES_PAGE;
	}

	/**
	 * Get the create new sample page.
	 * 
	 * @param projectId
	 *            Id for the {@link Project} the sample will belong to.
	 * @param model
	 *            {@link Model}
	 * @return Name of the add sample page.
	 */
	@RequestMapping("/projects/{projectId}/samples/new")
	public String getCreateNewSamplePage(@PathVariable Long projectId, Model model) {
		Project project = projectService.read(projectId);
		model.addAttribute("project", project);
		return "projects/project_add_sample";
	}

	/**
	 * Special method to add the correct linker script name to the modal
	 * template
	 *
	 * @param model
	 *            {@link Model}
	 *
	 * @return Location of the modal template
	 */
	@RequestMapping("/projects/templates/samples/linker")
	public String getLinkerModal(Model model) {
		model.addAttribute("scriptName", LINKER_SCRIPT);
		return PROJECT_TEMPLATE_DIR + "linker.tmpl";
	}

	/**
	 * Creates the modal to remove samples from a project.
	 *
	 * @param ids
	 * 		{@link List} of sample names to remove.
	 * @param model
	 * 		{@link Model}
	 *
	 * @return
	 */
	@RequestMapping("/projects/templates/remove-modal")
	public String getRemoveSamplesFromProjectModal(@RequestParam(name = "sampleIds[]") List<Long> ids, Model model) {
		List<Sample> samples = new ArrayList<>(ids.size());
		samples.addAll(ids.stream().map(sampleService::read).collect(Collectors.toList()));
		model.addAttribute("samples", samples);
		return PROJECT_TEMPLATE_DIR + "remove-modal.tmpl";
	}

	/**
	 * Get a list of all samples within the project
	 *
	 * @param projectId
	 *            The id for the current {@link Project}
	 *
	 * @return A list of {@link Sample} in the current project
	 */
	@RequestMapping(value = "/projects/{projectId}/ajax/samples", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getProjectSamples(@PathVariable Long projectId) {
		Map<String, Object> result = new HashMap<>();
		Project project = projectService.read(projectId);
		List<Join<Project, Sample>> joinList = sampleService.getSamplesForProject(project);
		List<Map<String, Object>> samples = new ArrayList<>(joinList.size());

		for (Join<Project, Sample> join : joinList) {
			Map<String, Object> sampleMap = getSampleMap(join.getObject(), join.getSubject(), SampleType.LOCAL, join
					.getObject().getId());
			samples.add(sampleMap);
		}
		result.put("samples", samples);
		result.put("project", project);
		return result;
	}

	/**
	 * Search for projects available for a user to copy samples to. If the user
	 * is an admin it will show all projects.
	 *
	 * @param term
	 *            A search term
	 * @param pageSize
	 *            The size of the page requests
	 * @param page
	 *            The page number (0 based)
	 * @param principal
	 *            The logged in user.
	 *
	 * @return a {@code Map<String,Object>} containing: total: total number of
	 *         elements results: A {@code Map<Long,String>} of project IDs and
	 *         project names.
	 */
	@RequestMapping(value = "/projects/ajax/samples/available_projects")
	@ResponseBody
	public Map<String, Object> getProjectsAvailableToCopySamples(@RequestParam String term, @RequestParam int pageSize,
			@RequestParam int page, Principal principal) {
		User user = userService.getUserByUsername(principal.getName());

		List<Map<String, String>> projectMap = new ArrayList<>();
		Map<String, Object> response = new HashMap<>();
		if (user.getAuthorities().contains(Role.ROLE_ADMIN)) {
			Page<Project> projects = projectService.search(ProjectSpecification.searchProjectName(term), page,
					pageSize, Direction.ASC, PROJECT_NAME_PROPERTY);
			for (Project p : projects) {
				Map<String, String> map = new HashMap<>();
				map.put("identifier", p.getId().toString());
				map.put("text", p.getName());
				projectMap.add(map);
			}
			response.put("total", projects.getTotalElements());
		} else {
			// search for projects with a given name where the user is an owner
			Specification<ProjectUserJoin> spec = Specifications.where(
					ProjectUserJoinSpecification.searchProjectNameWithUser(term, user)).and(
					ProjectUserJoinSpecification.getProjectJoinsWithRole(user, ProjectRole.PROJECT_OWNER));
			Page<ProjectUserJoin> projects = projectService.searchProjectUsers(spec, page, pageSize, Direction.ASC);
			for (ProjectUserJoin projectUserJoin : projects) {
				Project p = projectUserJoin.getSubject();
				Map<String, String> map = new HashMap<>();
				map.put("identifier", p.getId().toString());
				map.put("text", p.getName());
				projectMap.add(map);
			}
			response.put("total", projects.getTotalElements());
		}

		response.put("projects", projectMap);

		return response;
	}

	/**
	 * Copy or move samples from one project to another
	 *
	 * @param projectId
	 *            The original project id
	 * @param sampleIds
	 *            the sample identifiers to copy
	 * @param newProjectId
	 *            The new project id
	 * @param removeFromOriginal
	 *            true/false whether to remove the samples from the original
	 *            project
	 * @param locale
	 *            the locale specified by the browser.
	 *
	 * @return A list of warnings
	 */
	@RequestMapping(value = "/projects/{projectId}/ajax/samples/copy", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> copySampleToProject(@PathVariable Long projectId,
			@RequestParam(value = "sampleIds[]") List<Long> sampleIds, @RequestParam Long newProjectId,
			@RequestParam boolean removeFromOriginal, Locale locale) {
		Project originalProject = projectService.read(projectId);
		Project newProject = projectService.read(newProjectId);

		Map<String, Object> response = new HashMap<>();
		List<String> warnings = new ArrayList<>();
		List<Sample> successful = new ArrayList<>();

		for (Long sampleId : sampleIds) {
			Sample sample = sampleService.read(sampleId);
			try {

				if (removeFromOriginal) {
					projectService.moveSampleBetweenProjects(originalProject, newProject, sample);
				} else {
					projectService.addSampleToProject(newProject, sample);
				}

				logger.trace("Copied sample " + sampleId + " to project " + newProjectId);
				successful.add(sample);
			} catch (EntityExistsException ex) {
				logger.warn("Attempted to add sample " + sampleId + " to project " + newProjectId
						+ " where it already exists.");

				warnings.add(messageSource.getMessage("project.samples.copy-error-message",
						new Object[] { sample.getSampleName(), newProject.getName() }, locale));
			}
		}

		if (!warnings.isEmpty() || successful.size() == 0) {
			response.put("result", "warning");
			response.put("warnings", warnings);
		} else {
			response.put("result", "success");
		}
		// 1. Only one sample copied
		// 2. Only one sample moved
		if (successful.size() == 1) {
			if (removeFromOriginal) {
				response.put(
						"message",
						messageSource.getMessage("project.samples.move-single-success-message", new Object[] {
								successful.get(0).getSampleName(), newProject.getName() }, locale));
			} else {
				response.put(
						"message",
						messageSource.getMessage("project.samples.copy-single-success-message", new Object[] {
								successful.get(0).getSampleName(), newProject.getName() }, locale));
			}
		}
		// 3. Multiple samples copied
		// 4. Multiple samples moved
		else if (successful.size() > 1) {
			if (removeFromOriginal) {
				response.put(
						"message",
						messageSource.getMessage("project.samples.move-multiple-success-message", new Object[] {
								successful.size(), newProject.getName() }, locale));
			} else {
				response.put(
						"message",
						messageSource.getMessage("project.samples.copy-multiple-success-message", new Object[] {
								successful.size(), newProject.getName() }, locale));
			}
		}

		response.put("successful", successful.stream().map((s) -> s.getId()).collect(Collectors.toList()));

		return response;
	}

	/**
	 * Remove a list of samples from a a Project.
	 *
	 * @param projectId
	 *            Id of the project to remove the samples from
	 * @param sampleIds
	 *            An array of samples to remove from a project
	 * @param locale
	 *            The locale of the web browser.
	 *
	 * @return Map containing either success or errors.
	 */
	@RequestMapping(value = "/projects/{projectId}/ajax/samples/delete", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> deleteProjectSamples(@PathVariable Long projectId,
			@RequestParam(value = "sampleIds[]") List<Long> sampleIds, Locale locale) {
		Project project = projectService.read(projectId);
		Map<String, Object> result = new HashMap<>();
		for (Long id : sampleIds) {
			try {
				Sample sample = sampleService.read(id);
				projectService.removeSampleFromProject(project, sample);
			} catch (EntityNotFoundException e) {
				result.put("error", "Cannot find sample with id: " + id);
			}

		}
		if (sampleIds.size() == 1) {
			Sample sample = sampleService.read(sampleIds.get(0));
			result.put("message",
                    messageSource.getMessage("project.samples.remove-success-singular", new Object[] { sample.getSampleName(), project.getLabel() }, locale));
		} else {
			result.put("message",
					messageSource.getMessage("project.samples.remove-success-plural", new Object[] { sampleIds.size(), project.getLabel() }, locale));
		}

		result.put("result", "success");
		return result;
	}

	/**
	 * Merges a list of samples into either the first sample in the list with a
	 * new name if provided, or into the selected sample based on the id.
	 *
	 * @param projectId
	 *            The id for the current {@link Project}
	 * @param mergeSampleId
	 *            An id for a {@link Sample} to merge the other samples into.
	 * @param sampleIds
	 *            A list of ids for {@link Sample} to merge together.
	 * @param newName
	 *            An optional new name for the {@link Sample}.
	 * @param locale
	 *            The {@link Locale} of the current user.
	 *
	 * @return a map of {@link Sample} properties representing the merged
	 *         sample.
	 */
	@RequestMapping(value = "/projects/{projectId}/ajax/samples/merge", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, Object> ajaxSamplesMerge(@PathVariable Long projectId,
			@RequestParam Long mergeSampleId, @RequestParam(value = "sampleIds[]") List<Long> sampleIds,
			@RequestParam String newName, Locale locale) {
		Map<String, Object> result = new HashMap<>();
		int samplesMergeCount = sampleIds.size();
		Project project = projectService.read(projectId);
		// Determine which sample to merge into
		Sample mergeIntoSample = sampleService.read(mergeSampleId);
		sampleIds.remove(mergeSampleId);

		if (!Strings.isNullOrEmpty(newName)) {
			try {
				mergeIntoSample = sampleService.update(mergeSampleId, ImmutableMap.of("sampleName", newName));
			} catch (ConstraintViolationException e) {
				logger.error(e.getLocalizedMessage());
				result.put("result", "error");
				result.put("warnings", getErrorsFromViolationException(e));
				return result;
			}
		}

		// Create an update map
		Sample[] mergeSamples = new Sample[sampleIds.size()];
		int count = 0;
		for (Long sampleId : sampleIds) {
			mergeSamples[count++] = sampleService.read(sampleId);
		}

		// Merge the samples
		sampleService.mergeSamples(project, mergeIntoSample, mergeSamples);

		result.put("result", "success");
		result.put(
				"message",
				messageSource.getMessage("project.samples.combine-success", new Object[] { samplesMergeCount,
						mergeIntoSample.getSampleName() }, locale));
		return result;
	}

	/**
	 * Remove the given {@link Sample}s from the given {@link Project}
	 *
	 * @param projectId
	 *            ID of the project to remove from
	 * @param samples
	 *            {@link Sample} ids to remove
	 * @param locale
	 *            User's locale
	 *
	 * @return Map with success message
	 */
	@RequestMapping(value = "/projects/{projectId}/ajax/samples/remove", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> removeSamplesFromProject(@PathVariable Long projectId,
			@RequestParam(value = "samples[]") List<Long> samples, Locale locale) {
		Map<String, Object> result = new HashMap<>();

		// read the project
		Project project = projectService.read(projectId);

		// get the samples
		Iterable<Sample> readMultiple = sampleService.readMultiple(samples);

		// remove all samples
		projectService.removeSamplesFromProject(project, readMultiple);

		// build success message
		result.put("result", "success");
		result.put(
				"message",
				messageSource.getMessage("project.samples.remove.success",
						new Object[] { samples.size(), project.getLabel() }, locale));

		return result;
	}

	/**
	 * Download a set of sequence files from selected samples within a project
	 *
	 * @param projectId
	 *            Id for a {@link Project}
	 * @param ids
	 *            List of ids ofr {@link Sample} within the project
	 * @param response
	 *            {@link HttpServletResponse}
	 *
	 * @throws IOException
	 *             if we fail to read a file from the filesystem.
	 */
	@RequestMapping(value = "/projects/{projectId}/download/files")
	public void downloadSamples(@PathVariable Long projectId, @RequestParam List<Long> ids, HttpServletResponse response)
			throws IOException {
		Project project = projectService.read(projectId);
		List<Sample> samples = (List<Sample>) sampleService.readMultiple(ids);

		// Add the appropriate headers
		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + project.getName() + ".zip\"");
		response.setHeader("Transfer-Encoding", "chunked");

		// storing used filenames to ensure we don't have a conflict
		Set<String> usedFileNames = new HashSet<>();

		try (ZipOutputStream outputStream = new ZipOutputStream(response.getOutputStream())) {
			for (Sample sample : samples) {
				List<Join<Sample, SequenceFile>> sequenceFilesForSample = sequenceFileService
						.getSequenceFilesForSample(sample);
				for (Join<Sample, SequenceFile> join : sequenceFilesForSample) {
					Path path = join.getObject().getFile();
					StringBuilder name = new StringBuilder(project.getName());
					name.append("/").append(sample.getSampleName());
					name.append("/").append(path.getFileName().toString());

					String fileName = name.toString();
					if (usedFileNames.contains(fileName)) {
						fileName = handleDuplicate(fileName, usedFileNames);
					}
					final ZipEntry entry = new ZipEntry(fileName);
					// set the file creation time on the zip entry to be
					// whatever the creation time is on the filesystem
					final BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
					entry.setCreationTime(attr.creationTime());
					entry.setLastModifiedTime(attr.creationTime());

					outputStream.putNextEntry(entry);

					usedFileNames.add(fileName);

					Files.copy(path, outputStream);

					outputStream.closeEntry();
				}
			}
			outputStream.finish();
		} catch (IOException e) {
			// this generally means that the user has cancelled the download
			// from their web browser; we can safely ignore this
			logger.debug("This *probably* means that the user cancelled the download, "
					+ "but it might be something else, see the stack trace below for more information.", e);
		} catch (Exception e) {
			logger.error("Download failed...", e);
		} finally {
			// close the response outputStream so that we're not leaking
			// streams.
			response.getOutputStream().close();
		}
	}
	
	/**
	 * Rename a filename {@code original} and ensure it doesn't exist in
	 * {@code usedNames}. Uses the windows style of renaming file.ext to file
	 * (1).ext
	 * 
	 * @param original
	 *            original file name
	 * @param usedNames
	 *            names that original must not conflict with
	 * @return modified name
	 */
	private String handleDuplicate(String original, Set<String> usedNames) {
		int lastDot = original.lastIndexOf('.');

		int index = 0;
		String result;
		do {
			index++;
			result = original.substring(0, lastDot) + " (" + index + ")" + original.substring(lastDot);
		} while (usedNames.contains(result));

		return result;
	}

	/**
	 * Create a new {@link Sample} in a {@link Project}
	 *
	 * @param projectId
	 *            the ID of the {@link Project} to add to
	 * @param sample
	 *            The {@link Sample} to create
	 * @param response
	 *            {@link HttpServletResponse}
	 *
	 * @return Success status and id if successful, errors if not
	 */
	@RequestMapping(value = "/projects/{projectId}/samples", method = RequestMethod.POST)
	public Map<String, Object> createSampleInProject(@PathVariable Long projectId, @ModelAttribute Sample sample,
			HttpServletResponse response) {
		// get the project
		Project project = projectService.read(projectId);

		Map<String, Object> responseBody = new HashMap<>();

		// try to add the sample to the project
		Join<Project, Sample> addSampleToProject = null;
		try {
			addSampleToProject = projectService.addSampleToProject(project, sample);
			Long sampleId = addSampleToProject.getObject().getId();
			responseBody.put("sampleId", sampleId);
			response.setStatus(HttpStatus.CREATED.value());
		} catch (ConstraintViolationException ex) {
			// if errors respond with the errors
			Map<String, String> errorsFromViolationException = getErrorsFromViolationException(ex);
			responseBody.put("errors", errorsFromViolationException);
			response.setStatus(HttpStatus.BAD_REQUEST.value());
		}

		return responseBody;
	}

	/**
	 * Get the Map format of {@link Sample}s to return for the project/samples
	 * page
	 *
	 * @param sample
	 *            The sample to display
	 * @param project
	 *            The originating project
	 * @param type
	 *            The {@link SampleType} of the sample (LOCAL, ASSOCIATED)
	 * @param identifier
	 *            Object to identify the {@link Sample}. Local samples will be
	 *            the sample id, remote may be a URL
	 * @return a formatted map of {@link Sample} objects.
	 */
	public static Map<String, Object> getSampleMap(Sample sample, Project project, SampleType type, Object identifier) {
		Map<String, Object> sampleMap = new HashMap<>();
		sampleMap.put("sample", sample);
		sampleMap.put("project", project);
		sampleMap.put("sampleType", type);
		sampleMap.put("identifier", identifier);
		String href = linkTo(
			methodOn(RESTProjectSamplesController.class).getProjectSample(
					project.getId(), sample.getId()
				)
			).withSelfRel().getHref();

		sampleMap.put("href", href);
		return sampleMap;
	}

	/**
	 * Changes a {@link ConstraintViolationException} to a usable map of strings
	 * for displaing in the UI.
	 *
	 * @param e
	 *            {@link ConstraintViolationException} for the form submitted.
	 *
	 * @return Map of string {fieldName, error}
	 */
	private Map<String, String> getErrorsFromViolationException(ConstraintViolationException e) {
		Map<String, String> errors = new HashMap<>();
		for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
			String message = violation.getMessage();
			String field = violation.getPropertyPath().toString();
			errors.put(field, message);
		}
		return errors;
	}

	/**
	 * Type of sample being displayed in the project/samples page. This will be
	 * used to determine how to link to resources and add them to the cart.
	 */
	public enum SampleType {
		// samples in the local project
		LOCAL,
		// samples in associated projects
		ASSOCIATED,
		// samples in remote projects
		REMOTE;

	}
}
