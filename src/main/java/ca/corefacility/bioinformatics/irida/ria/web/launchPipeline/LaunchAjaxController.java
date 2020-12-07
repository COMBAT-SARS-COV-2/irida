package ca.corefacility.bioinformatics.irida.ria.web.launchPipeline;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ca.corefacility.bioinformatics.irida.exceptions.IridaWorkflowException;
import ca.corefacility.bioinformatics.irida.ria.web.ajax.dto.ajax.AjaxCreateItemSuccessResponse;
import ca.corefacility.bioinformatics.irida.ria.web.ajax.dto.ajax.AjaxErrorResponse;
import ca.corefacility.bioinformatics.irida.ria.web.ajax.dto.ajax.AjaxResponse;
import ca.corefacility.bioinformatics.irida.ria.web.launchPipeline.dtos.LaunchRequest;
import ca.corefacility.bioinformatics.irida.ria.web.ajax.dto.pipeline.SavedPipelineParameters;
import ca.corefacility.bioinformatics.irida.ria.web.launchPipeline.dtos.LaunchSamples;
import ca.corefacility.bioinformatics.irida.ria.web.services.UIPipelineSampleService;
import ca.corefacility.bioinformatics.irida.ria.web.services.UIPipelineService;

/**
 * Controller to handle AJAX requests from the UI for Workflow Pipelines
 */
@RestController
@RequestMapping("/ajax/pipeline")
public class LaunchAjaxController {
    private final UIPipelineService pipelineService;
    private final UIPipelineSampleService sampleService;

    @Autowired
    public LaunchAjaxController(UIPipelineService pipelineService, UIPipelineSampleService sampleService) {
        this.pipelineService = pipelineService;
        this.sampleService = sampleService;
    }

    /**
     * Get the launch page for a specific IRIDA Workflow Pipeline.
     *
     * @param id     identifier for a pipeline.
     * @param locale current users locale information
     * @return The details about a specific pipeline else returns a status that the pipeline cannot be found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AjaxResponse> getPipelineDetails(@PathVariable UUID id, Locale locale) {
        try {
            return ResponseEntity.ok(pipelineService.getPipelineDetails(id, locale));
        } catch (IridaWorkflowException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new AjaxErrorResponse("Cannot find this pipeline"));
        }
    }

    @GetMapping("/samples")
    public ResponseEntity<List<LaunchSamples>> getPipelineSamples() {
        return ResponseEntity.ok(sampleService.getPipelineSamples());
    }

    /**
     * Launch a new IRIDA Workflow Pipeline
     *
     * @param request required parameters to launch the pipeline
     * @return A response to let the UI know the pipeline was launched successfully
     */
    @PostMapping("/{id}")
    public ResponseEntity<String> launchPipeline(@RequestBody LaunchRequest request) {
        return ResponseEntity.ok("YAY!!!!");
    }

    /**
     * Save a new set of named pipeline parameters
     *
     * @param id         identifier for a irida workflow
     * @param parameters details about the new set of parameters
     * @return The identifier for the newly created named parameter set, wrapped in a ajax response
     */
    @PostMapping("/{id}/parameters")
    public ResponseEntity<AjaxResponse> saveNewPipelineParameters(@PathVariable UUID id,
            @RequestBody SavedPipelineParameters parameters) {
        return ResponseEntity.ok(
                new AjaxCreateItemSuccessResponse(pipelineService.saveNewPipelineParameters(id, parameters)));
    }
}
