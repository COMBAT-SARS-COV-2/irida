package ca.corefacility.bioinformatics.irida.config.pipeline.data.galaxy;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstanceFactory;
import com.github.jmchilton.galaxybootstrap.BootStrapper;
import com.github.jmchilton.galaxybootstrap.BootStrapper.GalaxyDaemon;
import com.github.jmchilton.galaxybootstrap.DownloadProperties;
import com.github.jmchilton.galaxybootstrap.GalaxyData;
import com.github.jmchilton.galaxybootstrap.GalaxyProperties;
import com.github.jmchilton.galaxybootstrap.GalaxyData.User;

import ca.corefacility.bioinformatics.irida.config.conditions.NonWindowsPlatformCondition;
import ca.corefacility.bioinformatics.irida.exceptions.galaxy.GalaxyConnectException;
import ca.corefacility.bioinformatics.irida.model.upload.galaxy.GalaxyAccountEmail;
import ca.corefacility.bioinformatics.irida.model.upload.galaxy.GalaxyProjectName;
import ca.corefacility.bioinformatics.irida.pipeline.upload.Uploader;
import ca.corefacility.bioinformatics.irida.pipeline.upload.Uploader.DataStorage;
import ca.corefacility.bioinformatics.irida.pipeline.upload.galaxy.GalaxyAPI;
import ca.corefacility.bioinformatics.irida.pipeline.upload.galaxy.GalaxyConnector;
import ca.corefacility.bioinformatics.irida.pipeline.upload.galaxy.GalaxyUploader;
import ca.corefacility.bioinformatics.irida.pipeline.upload.galaxy.integration.LocalGalaxy;

/**
 * Builds a local running instance of Galaxy (requires mercurial and python) for integration testing.
 * @author Aaron Petkau <aaron.petkau@phac-aspc.gc.ca>
 *
 */
@Configuration
@Profile("test")
@Conditional(NonWindowsPlatformCondition.class)
public class NonWindowsLocalGalaxyConfig implements LocalGalaxyConfig {
	
	/**
	 * The system property name to set the URL to the Galaxy mercurial repository. 
	 */
	private final static String GALAXY_URL_PROPERTY = "test.galaxy.repository.url";
	
	/**
	 * The system property name to set the branch of the Galaxy repository to use.
	 */
	private final static String GALAXY_BRANCH_PROPERTY = "test.galaxy.branch";
	
	/**
	 * The system property name to set the revision of the Galaxy repository to use.
	 */
	private final static String GALAXY_REVISION_PROPERTY = "test.galaxy.revision";
	
	/**
	 * Boolean to determine of Galaxy was successfully built the very first time.
	 */
	private boolean galaxyFailedToBuild = false;
	
	/**
	 * Exception on failure to build Galaxy for the first time.
	 */
	private Exception galaxyBuildException = null;
	
	private static final Logger logger = LoggerFactory
			.getLogger(NonWindowsLocalGalaxyConfig.class);

	private static final int largestPort = 65535;
	
	private static final String LATEST_REVISION_STRING = "latest";
	private static final String DEFAULT_REPSITORY_URL = "https://bitbucket.org/galaxy/galaxy-dist";
	private static final String DEFAULT_BRANCH = "default";

	/**
	 * Builds a GalaxyUploader to connect to a running instance of Galaxy.
	 * @return  An Uploader connected to a running instance of Galaxy.
	 * @throws MalformedURLException  If there was an issue when contructing a URL.
	 * @throws GalaxyConnectException If there was an issue connecting to the running instance of Galaxy.
	 */
	@Lazy
	@Bean
	public Uploader<GalaxyProjectName, GalaxyAccountEmail> galaxyUploader() throws MalformedURLException, GalaxyConnectException {
		GalaxyUploader galaxyUploader = new GalaxyUploader();
		galaxyUploader.connectToGalaxy(galaxyConnector());

		return galaxyUploader;
	}
	
	/**
	 * Creates a new GalaxyConnector to connect to the local Galaxy instance.
	 * @return  A GalaxyConnector to connect to the local Galaxy instance.
	 * @throws MalformedURLException  If the Galaxy URL is not formed properly.
	 */
	@Lazy
	@Bean
	public GalaxyConnector galaxyConnector() throws MalformedURLException {
		GalaxyConnector galaxyConnector = new GalaxyConnector(localGalaxy().getGalaxyURL(),
				localGalaxy().getAdminName(), localGalaxy().getAdminAPIKey());
		galaxyConnector.setDataStorage(DataStorage.REMOTE);
		
		return galaxyConnector;
	}

	/**
	 * Builds a GalaxyAPI object to connect to a running instance of Galaxy.
	 * @return  A GalaxyAPI object connected to a running instance of Galaxy.
	 * @throws MalformedURLException  If there was an issue building some of the URLs.
	 * @throws GalaxyConnectException If there was an issue connecting to the Galaxy instance.
	 */
	@Lazy
	@Bean
	public GalaxyAPI galaxyAPI() throws MalformedURLException, GalaxyConnectException {
		return new GalaxyAPI(localGalaxy().getGalaxyURL(), localGalaxy()
				.getAdminName(), localGalaxy().getAdminAPIKey());
	}

	/**
	 * Builds a new LocalGalaxy allowing for connecting with a running Galaxy instance.
	 * @return  A LocalGalaxy with information about the running Galaxy instance.
	 * @throws MalformedURLException  If there was an issue constructing the URLs.
	 */
	@Lazy
	@Bean
	public LocalGalaxy localGalaxy() throws MalformedURLException {
		
		if (galaxyFailedToBuild) {
			throw new RuntimeException("Galaxy could not be built the first time, don't attempt to try again", galaxyBuildException);
		} else {
			LocalGalaxy localGalaxy = null;
			
			try {
				localGalaxy = new LocalGalaxy();
				
				URL repositoryURL = getGalaxyRepositoryURL(GALAXY_URL_PROPERTY);
				String branchName = getGalaxyRepositoryBranch(GALAXY_BRANCH_PROPERTY);
				String revisionHash = getGalaxyRevision(GALAXY_REVISION_PROPERTY);
		
				String randomPassword = UUID.randomUUID().toString();
		
				localGalaxy.setAdminName(new GalaxyAccountEmail("admin@localhost"));
				localGalaxy.setAdminPassword(randomPassword);
				localGalaxy.setUser1Name(new GalaxyAccountEmail("user1@localhost"));
				localGalaxy.setUser1Password(randomPassword);
				localGalaxy.setUser2Name(new GalaxyAccountEmail("user2@localhost"));
				localGalaxy.setUser2Password(randomPassword);
				localGalaxy.setNonExistentGalaxyAdminName(new GalaxyAccountEmail(
						"admin_no_exist@localhost"));
				localGalaxy.setNonExistentGalaxyUserName(new GalaxyAccountEmail(
						"no_exist@localhost"));
		
				localGalaxy.setInvalidGalaxyUserName(new GalaxyAccountEmail(
						"<a href='localhost'>invalid user</a>"));
		
				GalaxyData galaxyData = new GalaxyData();
		
				BootStrapper bootStrapper = downloadGalaxy(localGalaxy, repositoryURL, branchName, revisionHash);
				localGalaxy.setBootStrapper(bootStrapper);
		
				GalaxyProperties galaxyProperties = setupGalaxyProperties(localGalaxy,revisionHash);
				localGalaxy.setGalaxyProperties(galaxyProperties);
		
				buildGalaxyUsers(galaxyData, localGalaxy);
		
				GalaxyDaemon galaxyDaemon = runGalaxy(galaxyData, localGalaxy);
				localGalaxy.setGalaxyDaemon(galaxyDaemon);
		
				localGalaxy.setGalaxyInstanceAdmin(GalaxyInstanceFactory.get(
						localGalaxy.getGalaxyURL().toString(),
						localGalaxy.getAdminAPIKey()));
				localGalaxy.setGalaxyInstanceUser1(GalaxyInstanceFactory.get(
						localGalaxy.getGalaxyURL().toString(),
						localGalaxy.getUser1APIKey()));
				localGalaxy.setGalaxyInstanceUser2(GalaxyInstanceFactory.get(
						localGalaxy.getGalaxyURL().toString(),
						localGalaxy.getUser2APIKey()));
				
				localGalaxy.setupWorkflows();
				
				return localGalaxy;
			} catch (Exception e) {
				// If Galaxy failed to build, we don't want it to re-build on every test
				// which will waste time and resources.  Instead, we save the failure exception
				// and throw it on every further attempt to build a local instance of Galaxy.
				
				galaxyFailedToBuild = true;
				galaxyBuildException = e;
				
				// cleanup files if Galaxy was downloaded but couldn't be run
				if (localGalaxy != null) {
					localGalaxy.deleteGalaxy();
				}
				
				throw e;
			}
		}
	}
	
	/**
	 * Gets the URL to a Galaxy repository to download and test against.
	 * @param systemProperty  The system property storing the URL.
	 * @return  A URL to a Galaxy repository to download and test against.
	 * @throws MalformedURLException 
	 */
	private URL getGalaxyRepositoryURL(String systemProperty) throws MalformedURLException {
		String repsitoryURLString = System.getProperty(systemProperty);
		URL repositoryURL = new URL(DEFAULT_REPSITORY_URL);
		
		if (repsitoryURLString != null && !"".equals(repsitoryURLString)) {
			repositoryURL = new URL(repsitoryURLString);
		}
		
		return repositoryURL;
	}
	
	/**
	 * Gets the branch within a Galaxy repository to download and test against.
	 * @param systemProperty  The system property storing the branch name.
	 * @return A branch name within a Galaxy repository.
	 */
	private String getGalaxyRepositoryBranch(String systemProperty) {
		String repsitoryBranchString = System.getProperty(systemProperty);
		
		if (repsitoryBranchString != null && !"".equals(repsitoryBranchString)) {
			return repsitoryBranchString;
		} else {
			return DEFAULT_BRANCH;
		}
	}

	/**
	 * Given a system property string gets the revision hash for the version of Galaxy
	 * 	from this property.  Corresponds to commit in https://bitbucket.org/galaxy/galaxy-dist.
	 * @param systemProperty  The system property storing the revision hash.
	 * @return  The revision hash code to download Galaxy at, DownloadProperties.LATEST_REVISION
	 * 	if no hash is defined.
	 */
	private String getGalaxyRevision(String systemProperty) {
		String revisionHash = System.getProperty(systemProperty);
		if (revisionHash != null) {
			
			// must be LATEST_REVISION_STRING or a hex number
			if (LATEST_REVISION_STRING.equalsIgnoreCase(revisionHash)) {
				revisionHash = DownloadProperties.LATEST_REVISION;
				logger.debug("Galaxy revision from " + systemProperty + "=" + LATEST_REVISION_STRING);
			}
			else if (!revisionHash.matches("^[a-fA-F0-9]+$")) {
				throw new IllegalArgumentException(systemProperty + "=" + revisionHash + " is invalid");
			} else {
				logger.debug("Galaxy revision from " + systemProperty + "=" + revisionHash);
			}
		} else {
			revisionHash = DownloadProperties.LATEST_REVISION;
			logger.debug("No Galaxy revision set in " + systemProperty + " defaulting to latest revision");
		}
		
		return revisionHash;
	}

	/**
	 * Downloads the latest stable release of Galaxy.
	 * @param localGalaxy  The LocalGalaxy object used to fill in information about Galaxy.
	 * @param repositoryURL The URL of the repository storing the Galaxy code.
	 * @param revisionHash  The mercurial revisionHash of Galaxy to download. 
	 * @return  A BootStrapper object describing the downloaded Galaxy.
	 */
	@SuppressWarnings("deprecation")
	private BootStrapper downloadGalaxy(LocalGalaxy localGalaxy, URL repositoryURL,
			String branchName, String revisionHash) {
		final File DEFAULT_DESTINATION = null;
		
		DownloadProperties downloadProperties
			= new DownloadProperties(repositoryURL.toString(), branchName, revisionHash, DEFAULT_DESTINATION);
		BootStrapper bootStrapper = new BootStrapper(downloadProperties);

		bootStrapper.setupGalaxy();

		return bootStrapper;
	}

	/**
	 * Does some custom configuration for Galaxy to work with the tests.
	 * @param localGalaxy  The object describing the local running instance of Galaxy.
	 * @param revisionHash  The mercurial revision hash of the Galaxy version to download. 
	 * @return  A GalaxyProperties object defining properties of the running instance of Galaxy.
	 * @throws MalformedURLException  If there was an issue constructing the Galaxy URL.
	 */
	private GalaxyProperties setupGalaxyProperties(LocalGalaxy localGalaxy, String revisionHash)
			throws MalformedURLException {
		GalaxyProperties galaxyProperties = new GalaxyProperties()
				.assignFreePort().configureNestedShedTools();
		
		// only pre-populate if latest Galaxy
		// speeds up database construction, but database wouldn't be valid for previous versions of Galaxy
		if (DownloadProperties.LATEST_REVISION.equals(revisionHash)) {
			galaxyProperties.prepopulateSqliteDatabase();
		}
		
		galaxyProperties.setAppProperty("allow_library_path_paste", "true");

		int galaxyPort = galaxyProperties.getPort();
		URL galaxyURL = new URL("http://localhost:" + galaxyPort + "/");
		localGalaxy.setGalaxyURL(galaxyURL);

		// set wrong port to something Galaxy is not running on
		int wrongPort = (galaxyPort + 1);
		if (wrongPort > largestPort) {
			wrongPort = galaxyPort - 1;
		}
		URL wrongGalaxyURL = new URL("http://localhost:" + wrongPort + "/");
		localGalaxy.setInvalidGalaxyURL(wrongGalaxyURL);
		
		// setup another port for running tests on
		int wrongPort2 = (galaxyPort + 2);
		if (wrongPort2 > largestPort) {
			wrongPort2 = galaxyPort - 2;
		}
		URL wrongGalaxyURL2 = new URL("http://localhost:" + wrongPort2 + "/");
		localGalaxy.setTestGalaxyURL(wrongGalaxyURL2);

		return galaxyProperties;
	}

	/**
	 * Configures the users for the Galaxy for integration testing.
	 * @param galaxyData  A GalaxyData object used to setup users.
	 * @param localGalaxy  An object containing information about the local running Galaxy.
	 */
	private void buildGalaxyUsers(GalaxyData galaxyData, LocalGalaxy localGalaxy) {
		GalaxyProperties galaxyProperties = localGalaxy.getGalaxyProperties();

		User adminUser = new User(localGalaxy.getAdminName().getName());
		adminUser.setPassword(localGalaxy.getAdminPassword());
		localGalaxy.setAdminAPIKey(adminUser.getApiKey());

		User user1 = new User(localGalaxy.getUser1Name().getName());
		user1.setPassword(localGalaxy.getUser1Password());
		localGalaxy.setUser1APIKey(user1.getApiKey());

		User user2 = new User(localGalaxy.getUser2Name().getName());
		user2.setPassword(localGalaxy.getUser2Password());
		localGalaxy.setUser2APIKey(user2.getApiKey());

		galaxyData.getUsers().add(adminUser);
		galaxyData.getUsers().add(user1);
		galaxyData.getUsers().add(user2);

		galaxyProperties.setAdminUser(adminUser.getUsername());
	}

	/**
	 * Runs the downloaded and configured instance of Galaxy.
	 * @param galaxyData  The data used to run Galaxy.
	 * @param localGalaxy  The object containing information about the local Galaxy instance.
	 * @return  A GalaxyDaemon object containing information about the running Galaxy process.
	 */
	private GalaxyDaemon runGalaxy(GalaxyData galaxyData,
			LocalGalaxy localGalaxy) {
		GalaxyDaemon galaxyDaemon;

		GalaxyProperties galaxyProperties = localGalaxy.getGalaxyProperties();
		BootStrapper bootStrapper = localGalaxy.getBootStrapper();

		galaxyDaemon = bootStrapper.run(galaxyProperties, galaxyData);

		if (!galaxyDaemon.waitForUp()) {
			System.err.println("Could not launch Galaxy on "
					+ localGalaxy.getGalaxyURL());
			System.exit(1);
		}

		return galaxyDaemon;
	}
}
