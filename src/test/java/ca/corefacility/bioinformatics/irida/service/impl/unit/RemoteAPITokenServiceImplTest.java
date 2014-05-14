package ca.corefacility.bioinformatics.irida.service.impl.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import ca.corefacility.bioinformatics.irida.exceptions.UserNotInSecurityContextException;
import ca.corefacility.bioinformatics.irida.model.RemoteAPI;
import ca.corefacility.bioinformatics.irida.model.RemoteAPIToken;
import ca.corefacility.bioinformatics.irida.model.User;
import ca.corefacility.bioinformatics.irida.repositories.RemoteApiTokenRepository;
import ca.corefacility.bioinformatics.irida.repositories.UserRepository;
import ca.corefacility.bioinformatics.irida.service.RemoteAPITokenService;
import ca.corefacility.bioinformatics.irida.service.impl.RemoteAPITokenServiceImpl;

/**
 * Unit tests class for {@link RemoteAPITokenServiceImpl}
 * @author Thomas Matthews <thomas.matthews@phac-aspc.gc.ca>
 *
 */
public class RemoteAPITokenServiceImplTest {
	private RemoteAPITokenService service;
	private RemoteApiTokenRepository tokenRepository;
	private UserRepository userRepo;
	private RemoteAPIToken remoteAPIToken;
	private RemoteAPI remoteAPI;
	private User user;
	
	
	@Before
	public void setUp(){
		tokenRepository = mock(RemoteApiTokenRepository.class);
		userRepo = mock(UserRepository.class);
		service = new RemoteAPITokenServiceImpl(tokenRepository, userRepo);
		
		user = new User("tom", "an@email.com", "password1", "tom", "matthews", "123456789");
		remoteAPI = new RemoteAPI("http://nowhere", "a test api", "clientId", "clientSecret");
		SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(user, null));
		remoteAPIToken = new RemoteAPIToken("token",remoteAPI,new Date());
	}
	
	@Test
	public void testAddToken() {
		when(userRepo.loadUserByUsername(user.getUsername())).thenReturn(user);
		
		service.addToken(remoteAPIToken);
		
		verify(tokenRepository).save(remoteAPIToken);
		verify(userRepo,times(2)).loadUserByUsername(user.getUsername());
		verify(tokenRepository,times(0)).delete(remoteAPIToken);
	}
	
	@Test
	public void testAddTokenExisting() {
		when(userRepo.loadUserByUsername(user.getUsername())).thenReturn(user);
		when(tokenRepository.readTokenForApiAndUser(remoteAPI, user)).thenReturn(remoteAPIToken);
		
		service.addToken(remoteAPIToken);
		
		verify(tokenRepository).save(remoteAPIToken);
		verify(userRepo,times(2)).loadUserByUsername(user.getUsername());
		verify(tokenRepository).readTokenForApiAndUser(remoteAPI, user);
		verify(tokenRepository).delete(remoteAPIToken);
	}
	
	@Test(expected=UserNotInSecurityContextException.class)
	public void testAddTokenNotLoggedIn() {
		SecurityContextHolder.clearContext();
				
		service.addToken(remoteAPIToken);
	}

	@Test
	public void testGetToken() {
		when(userRepo.loadUserByUsername(user.getUsername())).thenReturn(user);
		when(tokenRepository.readTokenForApiAndUser(remoteAPI, user)).thenReturn(remoteAPIToken);
		
		RemoteAPIToken token = service.getToken(remoteAPI);
		
		assertEquals(remoteAPIToken, token);
		
		verify(userRepo).loadUserByUsername(user.getUsername());
		verify(tokenRepository).readTokenForApiAndUser(remoteAPI, user);
	}



}
