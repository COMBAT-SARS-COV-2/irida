package ca.corefacility.bioinformatics.irida.service.impl.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import ca.corefacility.bioinformatics.irida.config.IridaApiServicesConfig;
import ca.corefacility.bioinformatics.irida.config.data.IridaApiTestDataSourceConfig;
import ca.corefacility.bioinformatics.irida.exceptions.EntityNotFoundException;
import ca.corefacility.bioinformatics.irida.model.RemoteAPI;
import ca.corefacility.bioinformatics.irida.model.RemoteAPIToken;
import ca.corefacility.bioinformatics.irida.model.Role;
import ca.corefacility.bioinformatics.irida.model.User;
import ca.corefacility.bioinformatics.irida.service.RemoteAPIService;
import ca.corefacility.bioinformatics.irida.service.RemoteAPITokenService;
import ca.corefacility.bioinformatics.irida.service.UserService;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.google.common.collect.ImmutableList;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { IridaApiServicesConfig.class,
		IridaApiTestDataSourceConfig.class })
@ActiveProfiles("test")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class })
@DatabaseSetup("/ca/corefacility/bioinformatics/irida/service/impl/RemoteAPITokenServiceImplIT.xml")
@DatabaseTearDown("/ca/corefacility/bioinformatics/irida/test/integration/TableReset.xml")
public class RemoteAPITokenServiceImplIT {
	@Autowired
	UserService userService;
	@Autowired
	RemoteAPITokenService tokenService;
	@Autowired
	RemoteAPIService apiService;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Before
	public void setUp() {
		User u = new User();
		u.setUsername("tom");
		u.setPassword(passwordEncoder.encode("Password1"));
		u.setSystemRole(Role.ROLE_USER);

		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(u, "Password1",
				ImmutableList.of(Role.ROLE_USER));
		auth.setDetails(u);
		SecurityContextHolder.getContext().setAuthentication(auth);
	}

	@Test
	public void testGetToken() {
		RemoteAPI api = apiService.read(1l);
		RemoteAPIToken token = tokenService.getToken(api);
		assertNotNull(token);
		assertEquals("123456789", token.getTokenString());
	}

	@Test(expected=EntityNotFoundException.class)
	public void testGetTokenNotExists() {
		RemoteAPI api = apiService.read(2l);
		tokenService.getToken(api);
	}
	
	@Test
	public void addToken(){
		RemoteAPI api = apiService.read(2l);
		RemoteAPIToken token = new RemoteAPIToken("111111111", api, new Date());
		tokenService.addToken(token);
		
		RemoteAPIToken readToken = tokenService.getToken(api);
		
		assertEquals(token,readToken);
		
	}
	
	@Test
	public void addTokenExisting(){
		RemoteAPI api = apiService.read(1l);
		RemoteAPIToken originalToken = tokenService.getToken(api);
		
		RemoteAPIToken token = new RemoteAPIToken("111111111", api, new Date());
		tokenService.addToken(token);
		
		RemoteAPIToken readToken = tokenService.getToken(api);
		
		assertNotEquals(token,originalToken);
		assertEquals(token,readToken);
		
	}

}
