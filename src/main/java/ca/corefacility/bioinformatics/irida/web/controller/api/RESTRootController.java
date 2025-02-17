package ca.corefacility.bioinformatics.irida.web.controller.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import ca.corefacility.bioinformatics.irida.web.assembler.resource.ResponseResource;

import io.swagger.v3.oas.annotations.Operation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import ca.corefacility.bioinformatics.irida.model.user.Role;
import ca.corefacility.bioinformatics.irida.web.assembler.resource.RootResource;
import ca.corefacility.bioinformatics.irida.web.controller.api.projects.RESTProjectsController;
import ca.corefacility.bioinformatics.irida.web.controller.api.sequencingrun.RESTSequencingRunController;

import com.google.common.collect.Sets;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * A basis for clients to begin discovering other URLs in our API.
 */
@Controller
public class RESTRootController {

	/**
	 * A collection of the controllers in our system accessible by all
	 * authenticated users.
	 */
	public static final Map<String, Class<?>> PUBLIC_CONTROLLERS = new ConcurrentHashMap<>();

	/**
	 * A collection of the controllers in our system accessible by users with
	 * `ROLE_ADMIN` or `ROLE_SEQUENCER` authorization.
	 */
	public static final Map<String, Class<?>> RESTRICTED_CONTROLLERS = new ConcurrentHashMap<>();

	/**
	 * The roles that are permitted to view the restricted controllers.
	 */
	private static final Set<String> RESTRICTED_ROLES = Sets.newHashSet(Role.ROLE_ADMIN.getAuthority(),
			Role.ROLE_SEQUENCER.getAuthority());

	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(RESTRootController.class);

	/**
	 * Grab IRIDA Version string from properties
	 */
	@Value("${irida.version}")
	private String iridaVersion;

	/**
	 * Initialize a collection of all controllers in the system.
	 */
	@PostConstruct
	public void initLinks() {
		PUBLIC_CONTROLLERS.put("users", RESTUsersController.class);
		PUBLIC_CONTROLLERS.put("projects", RESTProjectsController.class);
		PUBLIC_CONTROLLERS.put(RESTAnalysisSubmissionController.SUBMISSIONS_REL,
				RESTAnalysisSubmissionController.class);
		PUBLIC_CONTROLLERS.put("sequencingRuns", RESTSequencingRunController.class);
	}

	/**
	 * Creates a response with a set of links used to discover the rest of the
	 * system.
	 *
	 * @param request Incoming HTTP request object to check the user's role.
	 * @return a response to the client.
	 */
	@Operation(operationId = "getLinks", summary = "Get the set of links used to discover the API", description = "Get the set of links used to discover the API.", tags = "api")
	@RequestMapping(method = RequestMethod.GET, value = "/api")
	public ResponseResource<RootResource> getLinks(final HttpServletRequest request) {
		logger.debug("Discovering application");
		RootResource resource = new RootResource();
		List<Link> links = new ArrayList<>();

		links.addAll(buildLinks(PUBLIC_CONTROLLERS));

		if (RESTRICTED_ROLES.stream()
				.anyMatch(r -> request.isUserInRole(r))) {
			links.addAll(buildLinks(RESTRICTED_CONTROLLERS));
		}

		// add a self-rel to the current page
		resource.add(linkTo(methodOn(RESTRootController.class).getLinks(request)).withSelfRel());

		// Add the version route to the links list
		links.add(linkTo(methodOn(RESTRootController.class).version()).withRel("version"));
		// add all of the links to the response
		resource.add(links);

		ResponseResource<RootResource> responseObject = new ResponseResource<>(resource);

		// respond to the client
		return responseObject;
	}

	/**
	 * Creates a response with the current build version.
	 *
	 * @return a response to the client
	 */
	@Operation(operationId = "version", summary = "Get the current API build version", description = "Get the current API build version.", tags = "api")
	@RequestMapping(method = RequestMethod.GET, value = "/api/version")
	public ResponseVersion version() {
		ResponseVersion responseObject = new ResponseVersion(iridaVersion);
		return responseObject;
	}

	/**
	 * Build a collection of links for the specified map of link rel names to
	 * controllers.
	 *
	 * @param controllers the collection of controllers.
	 * @return the list of links for that collection of controllers.
	 */
	private List<Link> buildLinks(final Map<String, Class<?>> controllers) {
		final List<Link> links = new ArrayList<>();
		// create a link to all of the controllers defined in our set, then add
		// the link to the list of links.
		for (final Entry<String, Class<?>> entry : controllers.entrySet()) {
			final Link link = linkTo(entry.getValue()).withRel(entry.getKey());
			links.add(link);
		}
		return links;
	}

	/**
	 * A convenient class for returning the api version.
	 */
	public class ResponseVersion {
		private String version;

		public ResponseVersion(String version) {
			this.version = version;
		}

		public String getVersion() {
			return version;
		}

	}
}
