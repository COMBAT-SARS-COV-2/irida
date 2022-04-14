package ca.corefacility.bioinformatics.irida.ria.web.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import ca.corefacility.bioinformatics.irida.exceptions.EntityNotFoundException;
import ca.corefacility.bioinformatics.irida.exceptions.SequenceFileAnalysisException;
import ca.corefacility.bioinformatics.irida.model.assembly.GenomeAssembly;
import ca.corefacility.bioinformatics.irida.model.joins.impl.ProjectSampleJoin;
import ca.corefacility.bioinformatics.irida.model.joins.impl.SampleGenomeAssemblyJoin;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.sample.QCEntry;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.model.sample.SampleSequencingObjectJoin;
import ca.corefacility.bioinformatics.irida.model.sample.metadata.MetadataEntry;
import ca.corefacility.bioinformatics.irida.model.sequenceFile.*;
import ca.corefacility.bioinformatics.irida.ria.web.models.tables.AntTableResponse;
import ca.corefacility.bioinformatics.irida.ria.web.projects.dto.ProjectCartSample;
import ca.corefacility.bioinformatics.irida.ria.web.projects.dto.ProjectSampleTableItem;
import ca.corefacility.bioinformatics.irida.ria.web.projects.dto.ProjectSamplesTableRequest;
import ca.corefacility.bioinformatics.irida.ria.web.projects.dto.samples.MergeRequest;
import ca.corefacility.bioinformatics.irida.ria.web.projects.dto.samples.ProjectSamplesFilter;
import ca.corefacility.bioinformatics.irida.ria.web.projects.error.SampleMergeException;
import ca.corefacility.bioinformatics.irida.ria.web.samples.dto.SampleDetails;
import ca.corefacility.bioinformatics.irida.ria.web.samples.dto.SampleFiles;
import ca.corefacility.bioinformatics.irida.ria.web.samples.dto.ShareSamplesRequest;
import ca.corefacility.bioinformatics.irida.security.permissions.sample.UpdateSamplePermission;
import ca.corefacility.bioinformatics.irida.service.GenomeAssemblyService;
import ca.corefacility.bioinformatics.irida.service.ProjectService;
import ca.corefacility.bioinformatics.irida.service.SequencingObjectService;
import ca.corefacility.bioinformatics.irida.service.sample.SampleService;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

/**
 * UI Service for samples
 */
@Component
public class UISampleService {
	private final SampleService sampleService;
	private final ProjectService projectService;
	private final UpdateSamplePermission updateSamplePermission;
	private final SequencingObjectService sequencingObjectService;
	private final GenomeAssemblyService genomeAssemblyService;
	private final MessageSource messageSource;
	private final UICartService cartService;

	@Autowired
	public UISampleService(SampleService sampleService, ProjectService projectService,
			UpdateSamplePermission updateSamplePermission, SequencingObjectService sequencingObjectService,
			GenomeAssemblyService genomeAssemblyService, MessageSource messageSource, UICartService cartService) {
		this.sampleService = sampleService;
		this.projectService = projectService;
		this.updateSamplePermission = updateSamplePermission;
		this.sequencingObjectService = sequencingObjectService;
		this.genomeAssemblyService = genomeAssemblyService;
		this.messageSource = messageSource;
		this.cartService = cartService;
	}

	/**
	 * Get full details, including metadata for a {@link Sample}
	 *
	 * @param id Identifier for a {@link Sample}
	 * @return {@link SampleDetails}
	 */
	public SampleDetails getSampleDetails(Long id) {
		Sample sample = sampleService.read(id);
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		boolean isModifiable = updateSamplePermission.isAllowed(authentication, sample);
		Set<MetadataEntry> metadataForSample = sampleService.getMetadataForSample(sample);
		return new SampleDetails(sample, isModifiable, metadataForSample, cartService.isSampleInCart(id));
	}

	/**
	 * Get the sequence files associated with a sample
	 *
	 * @param sampleId  Identifier for a sample
	 * @param projectId Identifier for the project the sample belong to
	 * @return All the sequencing files associated with the sample
	 */
	public SampleFiles getSampleFiles(Long sampleId, Long projectId) {
		Sample sample = sampleService.read(sampleId);
		// get the project if available
		Project project = null;
		if (projectId != null) {
			project = projectService.read(projectId);
		}

		List<SequencingObject> filePairs = getPairedSequenceFilesForSample(sample, project);
		List<SequencingObject> singles = getSingleEndSequenceFilesForSample(sample, project);
		List<SequencingObject> fast5 = getFast5FilesForSample(sample);
		List<GenomeAssembly> genomeAssemblies = getGenomeAssembliesForSample(sample);

		return new SampleFiles(singles, filePairs, fast5, genomeAssemblies);
	}

	/**
	 * Get a list of paired end sequence files for a sample
	 *
	 * @param sample  the {@link Sample} to get the files for.
	 * @param project the {@link Project} the sample belongs to
	 * @return list of paired end sequence files
	 */
	public List<SequencingObject> getPairedSequenceFilesForSample(Sample sample, Project project) {
		Collection<SampleSequencingObjectJoin> filePairJoins = sequencingObjectService.getSequencesForSampleOfType(
				sample, SequenceFilePair.class);
		// add project to qc entries and filter any unavailable entries
		List<SequencingObject> filePairs = new ArrayList<>();
		for (SampleSequencingObjectJoin join : filePairJoins) {
			SequencingObject obj = join.getObject();
			enhanceQcEntries(obj, project);
			filePairs.add(obj);
		}

		return filePairs;
	}

	/**
	 * Get a list of single end sequence files for a sample
	 *
	 * @param sample  the {@link Sample} to get the files for.
	 * @param project the {@link Project} the sample belongs to
	 * @return list of single end sequence files
	 */
	public List<SequencingObject> getSingleEndSequenceFilesForSample(Sample sample, Project project) {
		Collection<SampleSequencingObjectJoin> singleFileJoins = sequencingObjectService.getSequencesForSampleOfType(
				sample, SingleEndSequenceFile.class);

		List<SequencingObject> singles = new ArrayList<>();
		for (SampleSequencingObjectJoin join : singleFileJoins) {
			SequencingObject obj = join.getObject();
			enhanceQcEntries(obj, project);
			singles.add(obj);
		}

		return singles;
	}

	/**
	 * Get a list of fast5 sequence files for a sample
	 *
	 * @param sample the {@link Sample} to get the files for.
	 * @return list of fast5 sequence files
	 */
	public List<SequencingObject> getFast5FilesForSample(Sample sample) {
		Collection<SampleSequencingObjectJoin> fast5FileJoins = sequencingObjectService.getSequencesForSampleOfType(
				sample, Fast5Object.class);
		return fast5FileJoins.stream().map(SampleSequencingObjectJoin::getObject).collect(Collectors.toList());
	}

	/**
	 * Get any genome assemblies that are available for a sample
	 *
	 * @param sample the {@link Sample} to get the assemblies for
	 * @return a list of genome assembly files
	 */
	public List<GenomeAssembly> getGenomeAssembliesForSample(Sample sample) {
		Collection<SampleGenomeAssemblyJoin> genomeAssemblyJoins = genomeAssemblyService.getAssembliesForSample(sample);

		return genomeAssemblyJoins.stream().map(SampleGenomeAssemblyJoin::getObject).collect(Collectors.toList());
	}

	/**
	 * Adds the {@link Project} to any {@link QCEntry} within a {@link SequencingObject}. If the {@link QCEntry} reports
	 * as {@link QCEntry.QCEntryStatus#UNAVAILABLE} after being enhanced it is removed from the list
	 *
	 * @param obj     the {@link SequencingObject} to enhance
	 * @param project the {@link Project} to add
	 */
	private void enhanceQcEntries(SequencingObject obj, Project project) {
		Set<QCEntry> availableEntries = new HashSet<>();
		if (obj.getQcEntries() != null) {
			for (QCEntry q : obj.getQcEntries()) {
				q.addProjectSettings(project);
				if (!q.getStatus().equals(QCEntry.QCEntryStatus.UNAVAILABLE)) {
					availableEntries.add(q);
				}
			}
		}

		obj.setQcEntries(availableEntries);
	}

	/**
	 * Get a list of all {@link Sample} identifiers within a specific project
	 *
	 * @param projectId Identifier for a {@link Project}
	 * @return {@link List} of {@link Sample} identifiers
	 */
	public List<Long> getSampleIdsForProject(Long projectId) {
		Project project = projectService.read(projectId);
		List<Sample> samples = sampleService.getSamplesForProjectShallow(project);
		return samples.stream().map(Sample::getId).collect(Collectors.toUnmodifiableList());
	}

	/**
	 * Share / Move samples with another project
	 *
	 * @param request Request containing the details of the move
	 * @param locale  current users {@link Locale}
	 * @throws Exception if project or samples cannot be found
	 */
	public void shareSamplesWithProject(ShareSamplesRequest request, Locale locale) throws Exception {
		Project currentProject = projectService.read(request.getCurrentId());
		Project targetProject = projectService.read(request.getTargetId());

		List<Sample> samples = (List<Sample>) sampleService.readMultiple(request.getSampleIds());
		if (request.getRemove()) {
			try {
				projectService.moveSamples(currentProject, targetProject, samples);
			} catch (Exception e) {
				throw new Exception(messageSource.getMessage("server.ShareSamples.move-error",
						new Object[] { targetProject.getLabel() }, locale));
			}
		} else {
			try {
				projectService.shareSamples(currentProject, targetProject, samples, !request.getLocked());
			} catch (Exception e) {
				throw new Exception(messageSource.getMessage("server.ShareSamples.copy-error",
						new Object[] { targetProject.getLabel() }, locale));
			}
		}
	}

	/**
	 * Get a page of samples based on the current state of the table options (filters, sort, and pagination)
	 *
	 * @param projectId Identifier for the current project.
	 * @param request   Information about the state of the table (filters, sort, and pagination).
	 * @return a page of samples
	 */
	public AntTableResponse<ProjectSampleTableItem> getPagedProjectSamples(Long projectId,
			ProjectSamplesTableRequest request, Locale locale) {
		Project project = projectService.read(projectId);
		List<Long> projectIds = new ArrayList<>();
		projectIds.add(projectId);
		ProjectSamplesFilter filter = request.getFilters();
		if (filter.getAssociated() != null) {
			projectIds.addAll(filter.getAssociated());
		}
		List<Project> projects = (List<Project>) projectService.readMultiple(projectIds);

		Page<ProjectSampleJoin> page = sampleService.getFilteredSamplesForProjects(projects, ImmutableList.of(), null,
				null, null, null, null, request.getPage(), request.getPageSize(), request.getSort());

		List<ProjectSampleTableItem> content = page.getContent().stream().map(join -> {
			Sample sample = join.getObject();
			Double coverage = null;
			if (project.getGenomeSize() != null) {
				try {
					coverage = sampleService.estimateCoverageForSample(sample, project.getGenomeSize());
				} catch (SequenceFileAnalysisException e) {
					// NOTHING TO DO HERE
				}
			}
			List<QCEntry> qcEntriesForSample = sampleService.getQCEntriesForSample(sample);
			List<String> quality = new ArrayList<>();

			qcEntriesForSample.stream().forEach(entry -> {
				entry.addProjectSettings(project);
				if (entry.getStatus() == QCEntry.QCEntryStatus.NEGATIVE) {
					quality.add(messageSource.getMessage("sample.files.qc." + entry.getType(),
							new Object[] { entry.getMessage() }, locale));
				}
			});
			return new ProjectSampleTableItem(join, quality);
		}).collect(Collectors.toList());

		return new AntTableResponse<>(content, page.getTotalElements());
	}

	/**
	 * Get a list of all samples in the current project and associated project that have been filtered, return a minimal
	 * * representation of them.
	 *
	 * @param projectId Identifier for the current project.
	 * @param request   Details about the filters and associated projects
	 * @return list containing a minimal representation of the samples based on the filters
	 */
	public List<ProjectCartSample> getMinimalSampleDetailsForFilteredProject(Long projectId,
			ProjectSamplesTableRequest request) {
		ProjectSamplesFilter filter = request.getFilters();
		final int MAX_PAGE_SIZE = 5000;
		List<ProjectCartSample> filteredProjectSamples = new ArrayList<>();

		List<Long> projectIds = new ArrayList<>();
		projectIds.add(projectId);
		if (filter.getAssociated() != null) {
			projectIds.addAll(filter.getAssociated());
		}
		List<Project> projects = (List<Project>) projectService.readMultiple(projectIds);

		Page<ProjectSampleJoin> page = sampleService.getFilteredSamplesForProjects(projects, ImmutableList.of(), null,
				null, null, null, null, 0, MAX_PAGE_SIZE, request.getSort());
		while (!page.isEmpty()) {
			// Get the ProjectSampleJoin id
			for (ProjectSampleJoin join : page) {
				filteredProjectSamples.add(new ProjectCartSample(join));
			}

			// Get the next page
			page = sampleService.getFilteredSamplesForProjects(projects, ImmutableList.of(), null, null, null, null,
					null, page.getNumber() + 1, MAX_PAGE_SIZE, request.getSort());
		}

		return filteredProjectSamples;
	}

	/**
	 * Merge 1 or more samples into another sample
	 *
	 * @param projectId identifier for the current project
	 * @param request   details about the samples to merge
	 * @param locale    current users locale information
	 * @return result of the merge
	 * @throws SampleMergeException thrown if there is an error during the merge
	 */
	public String mergeSamples(long projectId, MergeRequest request, Locale locale) throws SampleMergeException {
		Project project = projectService.read(projectId);
		Sample primarySample = sampleService.read(request.getPrimary());

		if (!Strings.isNullOrEmpty(request.getNewName())) {
			primarySample.setSampleName(request.getNewName());
			try {
				sampleService.update(primarySample);
			} catch (EntityNotFoundException | ConstraintViolationException e) {
				throw new SampleMergeException(
						messageSource.getMessage("server.MergeModal.merged-error", new Object[] {}, locale));
			}
		}

		List<Sample> samples = (List<Sample>) sampleService.readMultiple(request.getIds());
		sampleService.mergeSamples(project, primarySample, samples);
		if (request.getIds().size() == 1) {
			return messageSource.getMessage("server.MergeModal.merged-single", new Object[] {
					samples.get(0).getSampleName(), primarySample.getSampleName() }, locale);
		} else {
			return messageSource.getMessage("server.MergeModal.merged-plural",
					new Object[] { samples.size(), primarySample.getSampleName() }, locale);
		}
	}

	/**
	 * Remove 1 or more samples from a project.
	 *
	 * @param projectId identifier for the project
	 * @param sampleIds list of sampleIds to remove
	 * @return result of the removal of samples
	 */
	public String removeSamplesFromProject(Long projectId, List<Long> sampleIds) {
		Project project = projectService.read(projectId);
		projectService.removeSamplesFromProject(project, sampleService.readMultiple(sampleIds));
		return "FOOBAR";
	}

	/**
	 * Get the sequence files for a list of samples
	 *
	 * @param projectId Identifier for the project
	 * @param sampleIds List of identifiers for samples
	 * @param response  {@link HttpServletResponse}
	 * @return Zip File containing sequence files for listed samples
	 */
	public StreamingResponseBody downloadSamples(long projectId, List<Long> sampleIds, HttpServletResponse response) {
		int BUFFER_SIZE = 1024;
		Project project = projectService.read(projectId);
		List<Sample> samples = (List<Sample>) sampleService.readMultiple(sampleIds);

		StreamingResponseBody body = out -> {
			final ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());
			ZipEntry zipEntry = null;
			InputStream inputStream = null;

			// storing used file names to ensure we don't have a conflict
			Set<String> usedFileNames = new HashSet<>();

			try {
				for (Sample sample : samples) {
					Collection<SampleSequencingObjectJoin> sequencingObjectsForSample = sequencingObjectService.getSequencingObjectsForSample(
							sample);

					for (SampleSequencingObjectJoin join : sequencingObjectsForSample) {
						for (SequenceFile file : join.getObject().getFiles()) {
							Path path = file.getFile();

							String fileName =
									project.getName() + "/" + sample.getSampleName() + "/" + path.getFileName()
											.toString();
							if (usedFileNames.contains(fileName)) {
								fileName = handleDuplicate(fileName, usedFileNames);
							}
							zipEntry = new ZipEntry(fileName);
							// set the file creation time on the zip entry to be
							// whatever the creation time is on the filesystem
							final BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
							zipEntry.setCreationTime(attr.creationTime());
							zipEntry.setLastModifiedTime(attr.creationTime());

							zipOutputStream.putNextEntry(zipEntry);
							usedFileNames.add(fileName);
							Files.copy(path, zipOutputStream);
							zipOutputStream.closeEntry();
						}
					}
				}
				zipOutputStream.finish();
			} catch (IOException e) {
				// Do something here
			} finally {
				response.getOutputStream().close();
			}

		};

		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment; filename=example.zip");
		response.addHeader("Pragma", "no-cache");
		response.addHeader("Expires", "0");
		return body;
	}

	/**
	 * Rename a filename {@code original} and ensure it doesn't exist in {@code usedNames}. Uses the windows style of
	 * renaming file.ext to file (1).ext
	 *
	 * @param original  original file name
	 * @param usedNames names that original must not conflict with
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

	public StreamingResponseBody downloadSamplesSpreadsheet(long projectId, String type,
			ProjectSamplesTableRequest request, HttpServletResponse response, Locale locale) {
		List<Long> projectIds = request.getFilters().getAssociated();
		projectIds.add(projectId);
		List<Project> projects = (List<Project>) projectService.readMultiple(projectIds);

		// TODO: Need Eric's sepcification code here.
	}
}
