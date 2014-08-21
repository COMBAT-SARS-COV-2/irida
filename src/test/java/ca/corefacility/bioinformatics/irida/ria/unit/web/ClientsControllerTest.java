package ca.corefacility.bioinformatics.irida.ria.unit.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.ui.ExtendedModelMap;

import ca.corefacility.bioinformatics.irida.model.IridaClientDetails;
import ca.corefacility.bioinformatics.irida.ria.utilities.components.DataTable;
import ca.corefacility.bioinformatics.irida.ria.web.oauth.ClientsController;
import ca.corefacility.bioinformatics.irida.service.IridaClientDetailsService;

import com.google.common.collect.Lists;

public class ClientsControllerTest {
	private IridaClientDetailsService clientDetailsService;
	private ClientsController controller;
	private MessageSource messageSource;
	private Locale locale;

	@Before
	public void setUp() {
		clientDetailsService = mock(IridaClientDetailsService.class);
		messageSource = mock(MessageSource.class);
		controller = new ClientsController(clientDetailsService, messageSource);
		locale = LocaleContextHolder.getLocale();
	}

	@Test
	public void testGetClientsPage() {
		String clientsPage = controller.getClientsPage();
		assertEquals(ClientsController.CLIENTS_PAGE, clientsPage);
	}

	@Test
	public void testRead() {
		Long clientId = 1l;
		ExtendedModelMap model = new ExtendedModelMap();
		IridaClientDetails iridaClientDetails = new IridaClientDetails();
		iridaClientDetails.setId(clientId);

		when(clientDetailsService.read(clientId)).thenReturn(iridaClientDetails);

		String detailsPage = controller.read(clientId, model, locale);

		assertEquals(ClientsController.CLIENT_DETAILS_PAGE, detailsPage);
		assertEquals(model.get("client"), iridaClientDetails);
		assertTrue(model.containsAttribute("grants"));

		verify(clientDetailsService).read(clientId);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetAjaxClientList() {
		int page = 0;
		int size = 10;
		int draw = 1;
		int sortColumn = 0;
		String direction = "asc";
		String searchValue = "";
		IridaClientDetails client1 = new IridaClientDetails();
		client1.setId(1l);
		IridaClientDetails client2 = new IridaClientDetails();
		client2.setId(2l);
		Page<IridaClientDetails> clientPage = new PageImpl<>(Lists.newArrayList(client1, client2));

		when(
				clientDetailsService.search(any(Specification.class), eq(page), eq(size), any(Direction.class),
						any(String.class))).thenReturn(clientPage);

		Map<String, Object> ajaxClientList = controller.getAjaxClientList(page, size, draw, sortColumn, direction,
				searchValue);

		assertNotNull(ajaxClientList.get(DataTable.RESPONSE_PARAM_DATA));
		List<List<String>> clientList = (List<List<String>>) ajaxClientList.get(DataTable.RESPONSE_PARAM_DATA);

		assertEquals(2, clientList.size());

		verify(clientDetailsService).search(any(Specification.class), eq(page), eq(size), any(Direction.class),
				any(String.class));

	}

	@Test
	public void testGetAddClientPage() {
		ExtendedModelMap model = new ExtendedModelMap();

		String addClientPage = controller.getAddClientPage(model);

		assertEquals(ClientsController.ADD_CLIENT_PAGE, addClientPage);
		assertTrue(model.containsAttribute("errors"));
		assertTrue(model.containsAttribute("given_tokenValidity"));
	}

	@Test
	public void testPostCreateClient() {
		IridaClientDetails client = new IridaClientDetails();
		client.setId(1l);
		ExtendedModelMap model = new ExtendedModelMap();

		when(clientDetailsService.create(client)).thenReturn(client);

		String postCreateClient = controller.postCreateClient(client, model, locale);

		assertEquals("redirect:/clients/1", postCreateClient);
		verify(clientDetailsService).create(client);
	}

	@Test
	public void testPostCreateClientError() {
		IridaClientDetails client = new IridaClientDetails();
		client.setId(1l);
		ExtendedModelMap model = new ExtendedModelMap();
		Locale locale = LocaleContextHolder.getLocale();

		DataIntegrityViolationException ex = new DataIntegrityViolationException("Error: "
				+ IridaClientDetails.CLIENT_ID_CONSTRAINT_NAME);

		when(clientDetailsService.create(client)).thenThrow(ex);

		String postCreateClient = controller.postCreateClient(client, model, locale);

		assertEquals(ClientsController.ADD_CLIENT_PAGE, postCreateClient);
		assertTrue(model.containsAttribute("errors"));
		@SuppressWarnings("unchecked")
		Map<String, String> errors = (Map<String, String>) model.get("errors");
		assertTrue(errors.containsKey("clientId"));

		verify(clientDetailsService).create(client);
	}

	@Test
	public void testRemoveClient() {
		Long id = 1l;

		String removeClient = controller.removeClient(id);

		assertEquals("redirect:/clients", removeClient);

		verify(clientDetailsService).delete(id);
	}
}
