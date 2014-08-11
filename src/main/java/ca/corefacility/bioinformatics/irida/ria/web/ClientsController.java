package ca.corefacility.bioinformatics.irida.ria.web;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ca.corefacility.bioinformatics.irida.model.IridaClientDetails;
import ca.corefacility.bioinformatics.irida.repositories.specification.IridaClientDetailsSpecification;
import ca.corefacility.bioinformatics.irida.ria.utilities.Formats;
import ca.corefacility.bioinformatics.irida.ria.utilities.components.DataTable;
import ca.corefacility.bioinformatics.irida.service.IridaClientDetailsService;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * Controller for all {@link IridaClientDetails} related views
 * 
 * @author Thomas Matthews <thomas.matthews@phac-aspc.gc.ca>
 */
@Controller
@RequestMapping(value = "/clients")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class ClientsController {

	public static final String CLIENTS_PAGE = "clients/list";
	public static final String CLIENT_DETAILS_PAGE = "clients/client_details";
	public static final String ADD_CLIENT_PAGE = "clients/create";

	private final IridaClientDetailsService clientDetailsService;
	private final MessageSource messageSource;

	private final String SORT_BY_ID = "id";
	private final List<String> SORT_COLUMNS = Lists.newArrayList(SORT_BY_ID, "clientId", "authorizedGrantTypes",
			"createdDate");
	private static final String SORT_ASCENDING = "asc";

	private final List<String> AVAILABLE_GRANTS = Lists.newArrayList("password", "authorization_code");

	@Autowired
	public ClientsController(IridaClientDetailsService clientDetailsService, MessageSource messageSource) {
		this.clientDetailsService = clientDetailsService;
		this.messageSource = messageSource;
	}

	/**
	 * Request for the page to display a list of all clients available.
	 * 
	 * @return The name of the page.
	 */
	@RequestMapping
	public String getClientsPage() {
		return CLIENTS_PAGE;
	}

	/**
	 * Read an individual client
	 * 
	 * @param clientId
	 *            The ID of the client to display
	 * @param model
	 *            The model object for this view
	 * @return The view name of the client details page
	 */
	@RequestMapping("/{clientId}")
	public String read(@PathVariable Long clientId, Model model) {
		IridaClientDetails client = clientDetailsService.read(clientId);

		String grants = getAuthorizedGrantTypesString(client);
		model.addAttribute("client", client);
		model.addAttribute("grants", grants);
		return CLIENT_DETAILS_PAGE;
	}

	/**
	 * Get the create client page
	 * 
	 * @param model
	 *            Model for the view
	 * @return The name of the create client page
	 */
	@RequestMapping(value = "/create", method = RequestMethod.GET)
	public String getAddClientPage(Model model) {
		if (!model.containsAttribute("errors")) {
			model.addAttribute("errors", new HashMap<String, String>());
		}

		model.addAttribute("available_grants", AVAILABLE_GRANTS);

		// set the default token validity
		if (!model.containsAttribute("given_tokenValidity")) {
			model.addAttribute("given_tokenValidity", IridaClientDetails.DEFAULT_TOKEN_VALIDITY);
		}

		return ADD_CLIENT_PAGE;
	}

	/**
	 * Create a new client
	 * 
	 * @param client
	 *            The client to add
	 * @param model
	 *            Model for the view
	 * @param locale
	 *            Locale of the current user session
	 * @return Redirect to the newly created client page, or back to the
	 *         creation page in case of an error.
	 */
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public String postCreateClient(IridaClientDetails client, Model model, Locale locale) {
		client.setClientSecret(generateClientSecret());

		Map<String, String> errors = new HashMap<>();
		String responsePage = null;
		try {
			IridaClientDetails create = clientDetailsService.create(client);
			responsePage = "redirect:/clients/" + create.getId();
		} catch (DataIntegrityViolationException ex) {
			if (ex.getMessage().contains(IridaClientDetails.CLIENT_ID_CONSTRAINT_NAME)) {
				errors.put("clientId", messageSource.getMessage("client.add.clientId.exists", null, locale));
			}
		}

		if (!errors.isEmpty()) {
			model.addAttribute("errors", errors);

			model.addAttribute("given_clientId", client.getClientId());
			model.addAttribute("given_tokenValidity", client.getAccessTokenValiditySeconds());

			responsePage = getAddClientPage(model);
		}

		return responsePage;
	}

	/**
	 * Ajax request page for getting a list of all clients
	 * 
	 * @param start
	 *            The start element of the page
	 * @param length
	 *            The page length
	 * @param draw
	 *            Whether to draw the table
	 * @param sortColumn
	 *            The column to sort on
	 * @param direction
	 *            The direction of the sort
	 * @param searchValue
	 *            The string search value for the table
	 * @return a Map<String,Object> for the table
	 */
	@RequestMapping(value = "/ajax/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, Object> getAjaxClientList(
			@RequestParam(DataTable.REQUEST_PARAM_START) Integer start,
			@RequestParam(DataTable.REQUEST_PARAM_LENGTH) Integer length,
			@RequestParam(DataTable.REQUEST_PARAM_DRAW) Integer draw,
			@RequestParam(value = DataTable.REQUEST_PARAM_SORT_COLUMN, defaultValue = "0") Integer sortColumn,
			@RequestParam(value = DataTable.REQUEST_PARAM_SORT_DIRECTION, defaultValue = "asc") String direction,
			@RequestParam(DataTable.REQUEST_PARAM_SEARCH_VALUE) String searchValue) {

		String sortString;

		try {
			sortString = SORT_COLUMNS.get(sortColumn);
		} catch (IndexOutOfBoundsException ex) {
			sortString = SORT_BY_ID;
		}

		Sort.Direction sortDirection = direction.equals(SORT_ASCENDING) ? Sort.Direction.ASC : Sort.Direction.DESC;

		int pageNum = start / length;

		Page<IridaClientDetails> search = clientDetailsService.search(
				IridaClientDetailsSpecification.searchClient(searchValue), pageNum, length, sortDirection, sortString);

		List<List<String>> clientsData = new ArrayList<>();
		for (IridaClientDetails client : search) {

			String grants = getAuthorizedGrantTypesString(client);

			List<String> row = new ArrayList<>();
			row.add(client.getId().toString());
			row.add(client.getClientId());
			row.add(grants);
			row.add(Formats.DATE.format(client.getTimestamp()));

			clientsData.add(row);
		}

		Map<String, Object> map = new HashMap<>();
		map.put(DataTable.RESPONSE_PARAM_DRAW, draw);
		map.put(DataTable.RESPONSE_PARAM_RECORDS_TOTAL, search.getTotalElements());
		map.put(DataTable.RESPONSE_PARAM_RECORDS_FILTERED, search.getTotalElements());

		map.put(DataTable.RESPONSE_PARAM_DATA, clientsData);
		return map;
	}

	/**
	 * Get a string representation of the authorized grant types
	 * 
	 * @param clientDetails
	 *            The client details object to get grants from
	 * @return A joined string separated by commas of the grant types
	 */
	private String getAuthorizedGrantTypesString(IridaClientDetails clientDetails) {
		Set<String> authorizedGrantTypes = clientDetails.getAuthorizedGrantTypes();

		return StringUtils.collectionToDelimitedString(authorizedGrantTypes, ", ");
	}

	/**
	 * Generate a temporary password for a user
	 * 
	 * @return A temporary password
	 */
	private static String generateClientSecret() {
		int PASSWORD_LENGTH = 42;
		int ALPHABET_SIZE = 26;
		int SINGLE_DIGIT_SIZE = 10;
		int RANDOM_LENGTH = PASSWORD_LENGTH - 3;

		List<Character> pwdArray = new ArrayList<>(PASSWORD_LENGTH);
		SecureRandom random = new SecureRandom();

		// 1. Create 1 random uppercase.
		pwdArray.add((char) ('A' + random.nextInt(ALPHABET_SIZE)));

		// 2. Create 1 random lowercase.
		pwdArray.add((char) ('a' + random.nextInt(ALPHABET_SIZE)));

		// 3. Create 1 random number.
		pwdArray.add((char) ('0' + random.nextInt(SINGLE_DIGIT_SIZE)));

		// 4. Create 5 random.
		int c = 'A';
		int rand = 0;
		for (int i = 0; i < RANDOM_LENGTH; i++) {
			rand = random.nextInt(3);
			switch (rand) {
			case 0:
				c = '0' + random.nextInt(SINGLE_DIGIT_SIZE);
				break;
			case 1:
				c = 'a' + random.nextInt(ALPHABET_SIZE);
				break;
			case 2:
				c = 'A' + random.nextInt(ALPHABET_SIZE);
				break;
			}
			pwdArray.add((char) c);
		}

		// 5. Shuffle.
		Collections.shuffle(pwdArray, random);

		// 6. Create string.
		Joiner joiner = Joiner.on("");
		return joiner.join(pwdArray);
	}
}
