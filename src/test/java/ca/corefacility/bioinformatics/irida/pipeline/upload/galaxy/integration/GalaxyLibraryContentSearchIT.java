package ca.corefacility.bioinformatics.irida.pipeline.upload.galaxy.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import ca.corefacility.bioinformatics.irida.config.IridaApiServicesConfig;
import ca.corefacility.bioinformatics.irida.config.data.IridaApiTestDataSourceConfig;
import ca.corefacility.bioinformatics.irida.config.pipeline.data.galaxy.NonWindowsLocalGalaxyConfig;
import ca.corefacility.bioinformatics.irida.config.pipeline.data.galaxy.WindowsLocalGalaxyConfig;
import ca.corefacility.bioinformatics.irida.config.processing.IridaApiTestMultithreadingConfig;
import ca.corefacility.bioinformatics.irida.exceptions.ExecutionManagerObjectNotFoundException;
import ca.corefacility.bioinformatics.irida.exceptions.galaxy.NoGalaxyContentFoundException;
import ca.corefacility.bioinformatics.irida.exceptions.galaxy.NoLibraryFoundException;
import ca.corefacility.bioinformatics.irida.model.upload.galaxy.GalaxyFolderName;
import ca.corefacility.bioinformatics.irida.model.upload.galaxy.GalaxyFolderPath;
import ca.corefacility.bioinformatics.irida.model.upload.galaxy.GalaxyProjectName;
import ca.corefacility.bioinformatics.irida.pipeline.upload.galaxy.GalaxyLibraryContentSearch;

import com.github.jmchilton.blend4j.galaxy.LibrariesClient;
import com.github.jmchilton.blend4j.galaxy.beans.Library;
import com.github.jmchilton.blend4j.galaxy.beans.LibraryContent;
import com.github.jmchilton.blend4j.galaxy.beans.LibraryFolder;
import com.github.springtestdbunit.DbUnitTestExecutionListener;

/**
 * Tests for searching for Galaxy library contents.
 * @author Aaron Petkau <aaron.petkau@phac-aspc.gc.ca>
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
		IridaApiServicesConfig.class, IridaApiTestDataSourceConfig.class,
		IridaApiTestMultithreadingConfig.class, NonWindowsLocalGalaxyConfig.class, WindowsLocalGalaxyConfig.class  })
@ActiveProfiles("test")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
		DbUnitTestExecutionListener.class })
public class GalaxyLibraryContentSearchIT {
	
	@Autowired
	private LocalGalaxy localGalaxy;
	
	private GalaxyLibraryContentSearch galaxyLibraryContentSearch;
	
	/**
	 * Sets up objects for GalaxyLibrarySearch.
	 */
	@Before
	public void setup() {
		galaxyLibraryContentSearch = new GalaxyLibraryContentSearch(localGalaxy.getGalaxyInstanceAdmin().getLibrariesClient(),
								localGalaxy.getGalaxyURL());
	}
	
	/**
	 * Builds a Galaxy library with the given name and content.
	 * @param libraryName  The name of the library to build.
	 * @param folderName  The folder name in the library to build.
	 * @return The combined Library and LibraryContent objects of the library.
	 */
	private Library buildLibrary(GalaxyProjectName libraryName, GalaxyFolderName folderName) {
		Library library = new Library();
		library.setName(libraryName.getName());
		
		LibrariesClient librariesClient = localGalaxy.getGalaxyInstanceAdmin().getLibrariesClient();
		Library createdLibrary = librariesClient.createLibrary(library);
		assertNotNull(createdLibrary);
		
		LibraryContent rootContent = librariesClient.getRootFolder(createdLibrary.getId());
		
		LibraryFolder folder = new LibraryFolder();
		folder.setFolderId(rootContent.getId());
		folder.setName(folderName.getName());
		librariesClient.createFolder(createdLibrary.getId(), folder);
		
		List<LibraryContent> contents = librariesClient.getLibraryContents(createdLibrary.getId());
		assertEquals(2, contents.size());
		
		return createdLibrary;
	}
	
	/**
	 * Converts GalaxyFolderName (no leading '/') to a path (leading '/')
	 * @param name  The name to convert.
	 * @return  The same name, but with a leading '/'.
	 */
	private GalaxyFolderPath folderNameToPath(GalaxyFolderName name) {
		return new GalaxyFolderPath("/" + name.getName());
	}
	
	/**
	 * Tests library content exists success.
	 * @throws NoLibraryFoundException
	 */
	@Test
	public void testGalaxyLibraryContentExists() {
		GalaxyProjectName libraryName =
				new GalaxyProjectName("GalaxyLibrarySearchIT_testGalaxyLibraryContentExists");
		GalaxyFolderName folderName = new GalaxyFolderName("folder");
		Library createdLibrary = buildLibrary(libraryName, folderName);
		assertTrue(galaxyLibraryContentSearch.exists(createdLibrary.getId()));
		assertTrue(galaxyLibraryContentSearch.libraryContentExists(createdLibrary.getId(),
				folderNameToPath(folderName)));
	}
	
	/**
	 * Tests library content not exists (invalid library id).
	 * @throws NoLibraryFoundException
	 */
	@Test
	public void testGalaxyLibraryContentNotExistsLibraryId() {
		GalaxyProjectName libraryName =
				new GalaxyProjectName("GalaxyLibrarySearchIT_testGalaxyLibraryContentNotExistsLibraryId");
		GalaxyFolderName folderName = new GalaxyFolderName("folder");
		buildLibrary(libraryName, folderName);
		assertFalse(galaxyLibraryContentSearch.exists("invalid"));
		assertFalse(galaxyLibraryContentSearch.libraryContentExists("invalid",
				folderNameToPath(folderName)));
	}
	
	/**
	 * Tests library content not exists (invalid folder).
	 * @throws NoLibraryFoundException
	 */
	@Test
	public void testGalaxyLibraryContentNotExistsFolder() {
		GalaxyProjectName libraryName =
				new GalaxyProjectName("GalaxyLibrarySearchIT_testGalaxyLibraryContentNotExistsFolder");
		GalaxyFolderName folderName = new GalaxyFolderName("folder");
		Library createdLibrary = buildLibrary(libraryName, folderName);
		assertFalse(galaxyLibraryContentSearch.libraryContentExists(createdLibrary.getId(),
				new GalaxyFolderPath("/invalid_folder")));
	}
	
	/**
	 * Tests find library content success.
	 * @throws ExecutionManagerObjectNotFoundException 
	 */
	@Test
	public void testGalaxyFindLibraryContentSuccess() throws ExecutionManagerObjectNotFoundException {
		GalaxyProjectName libraryName =
				new GalaxyProjectName("GalaxyLibrarySearchIT_testGalaxyFindLibraryContentSuccess");
		GalaxyFolderName folderName = new GalaxyFolderName("folder");
		Library createdLibrary = buildLibrary(libraryName, folderName);
		assertNotNull(galaxyLibraryContentSearch.findById(createdLibrary.getId()));
		LibraryContent foundContent = galaxyLibraryContentSearch.findLibraryContentWithId(createdLibrary.getId(),
				folderNameToPath(folderName));
		assertNotNull(foundContent);
		assertEquals(folderNameToPath(folderName).getName(), foundContent.getName());	
	}
	
	/**
	 * Tests find library content fail (findById).
	 * @throws ExecutionManagerObjectNotFoundException 
	 */
	@Test(expected=NoGalaxyContentFoundException.class)
	public void testGalaxyFindLibraryContentByIdFail() throws ExecutionManagerObjectNotFoundException {
		GalaxyProjectName libraryName =
				new GalaxyProjectName("GalaxyLibrarySearchIT_testGalaxyFindLibraryContentByIdFail");
		GalaxyFolderName folderName = new GalaxyFolderName("folder");
		buildLibrary(libraryName, folderName);
		galaxyLibraryContentSearch.findById("invalid");
	}
	
	/**
	 * Tests find library content fail.
	 * @throws ExecutionManagerObjectNotFoundException 
	 */
	@Test(expected=NoGalaxyContentFoundException.class)
	public void testGalaxyFindLibraryContentFail() throws ExecutionManagerObjectNotFoundException {
		GalaxyProjectName libraryName =
				new GalaxyProjectName("GalaxyLibrarySearchIT_testGalaxyFindLibraryContentFail");
		GalaxyFolderName folderName = new GalaxyFolderName("folder");
		Library createdLibrary = buildLibrary(libraryName, folderName);
		galaxyLibraryContentSearch.findLibraryContentWithId(createdLibrary.getId(),
				new GalaxyFolderPath("/invalid_name"));
	}
	
	/**
	 * Tests getting library content as a map success.
	 * @throws ExecutionManagerObjectNotFoundException 
	 */
	@Test
	public void testGalaxyLibraryContentAsMapSuccess() throws ExecutionManagerObjectNotFoundException {
		GalaxyProjectName libraryName =
				new GalaxyProjectName("GalaxyLibrarySearchIT_testGalaxyLibraryContentAsMapSuccess");
		GalaxyFolderName folderName = new GalaxyFolderName("folder");
		Library createdLibrary = buildLibrary(libraryName, folderName);
		Map<String, LibraryContent> foundContent = 
				galaxyLibraryContentSearch.libraryContentAsMap(createdLibrary.getId());
		assertNotNull(foundContent);
		assertEquals(2, foundContent.size());
		assertTrue(foundContent.containsKey(folderNameToPath(folderName).getName()));
		assertTrue(foundContent.containsKey("/"));
	}
	
	/**
	 * Tests getting library content as a map fail.
	 * @throws ExecutionManagerObjectNotFoundException 
	 */
	@Test(expected=NoGalaxyContentFoundException.class)
	public void testGalaxyLibraryContentAsMapFail() throws ExecutionManagerObjectNotFoundException {
		galaxyLibraryContentSearch.libraryContentAsMap("1");
	}
}
