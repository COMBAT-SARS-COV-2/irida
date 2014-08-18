package ca.corefacility.bioinformatics.irida.ria.web.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.Formatter;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import ca.corefacility.bioinformatics.irida.exceptions.EntityNotFoundException;
import ca.corefacility.bioinformatics.irida.model.joins.Join;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.project.ReferenceFile;
import ca.corefacility.bioinformatics.irida.ria.utilities.converters.FileSizeConverter;
import ca.corefacility.bioinformatics.irida.service.ProjectService;
import ca.corefacility.bioinformatics.irida.service.ReferenceFileService;

/**
 * Controller for all {@link ReferenceFile} related views
 *
 * @author Josh Adam <josh.adam@phac-aspc.gc.ca>
 */
@Controller
@RequestMapping("/referenceFiles")
public class ReferenceFileController {
	private static final Logger logger = LoggerFactory.getLogger(ReferenceFileController.class);
	// Converters
	Formatter<Date> dateFormatter;
	private ProjectService projectService;
	private ReferenceFileService referenceFileService;
	private FileSizeConverter fileSizeConverter;

	@Autowired
	public ReferenceFileController(ProjectService projectService, ReferenceFileService referenceFileService) {
		this.projectService = projectService;
		this.referenceFileService = referenceFileService;
		this.fileSizeConverter = new FileSizeConverter();
		this.dateFormatter = new DateFormatter();
	}

	/**
	 * Download a reference file based on the id passed.
	 *
	 * @param fileId   The id of the file to download
	 * @param response {@link HttpServletResponse} to write to file to
	 * @throws IOException
	 */
	@RequestMapping(value = "/download/{fileId}")
	public void downloadReferenceFile(@PathVariable Long fileId,
			HttpServletResponse response) throws IOException {
		ReferenceFile file = referenceFileService.read(fileId);
		Path path = file.getFile();
		response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getLabel() + "\"");
		Files.copy(path, response.getOutputStream());
		response.flushBuffer();
	}

	/**
	 * Add a new reference file to a project.
	 * @param projectId The id of the project to add the file to.
	 * @param file {@link MultipartFile} file being uploaded.
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/project/{projectId}/new")
	public @ResponseBody Map<String, String> createNewReferenceFile(@PathVariable Long projectId,
			@RequestParam("file") MultipartFile file) throws IOException {

		logger.debug("Adding reference file to project " + projectId);
		logger.trace("Uploaded file size: " + file.getSize() + " bytes");

		Project project = projectService.read(projectId);
		logger.trace("Read project " + projectId);

		// Prepare a new reference file using the multipart file supplied by the caller
		Path temp = Files.createTempDirectory(null);
		Path target = temp.resolve(file.getOriginalFilename());

		file.transferTo(target.toFile());
		logger.debug("Wrote temp file to " + target);

		ReferenceFile referenceFile = new ReferenceFile(target);
		Join<Project, ReferenceFile> projectReferenceFileJoin = projectService
				.addReferenceFileToProject(project, referenceFile);
		logger.debug("Created reference file in project " + projectId);

		ReferenceFile refFile = projectReferenceFileJoin.getObject();
		Map<String, String> result = new HashMap<>();
		Path path = refFile.getFile();
		long size = 0;
		if (Files.exists(path)) {
			size = Files.size(path);
		}
		result.put("size", fileSizeConverter.convert(size));
		result.put("id", refFile.getId().toString());
		result.put("label", refFile.getLabel());
		result.put("createdDate", dateFormatter.print(refFile.getCreatedDate(), LocaleContextHolder.getLocale()));

		// Clean up temporary files
		Files.deleteIfExists(target);
		Files.deleteIfExists(temp);
		return result;
	}

	/**
	 * Delete a reference file.  This will remove it from the project.
	 * @param fileId The id of the file to remove.
	 * @param response {@link HttpServletResponse} required for returning an error state.
	 * @return Success or error based on the result of deleting the file.
	 */
	@RequestMapping("/delete")
	public @ResponseBody String deleteReferenceFile(@RequestParam Long fileId, HttpServletResponse response) {
		String result = "success";
		try {
			referenceFileService.delete(fileId);
		} catch (EntityNotFoundException e) {
			// This is required else the client does not know that an error was thrown!
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			logger.error("Failed to upload reference file, reason unknown.", e);
			result = "error";
		}
		return result;
	}
}

