package ca.corefacility.bioinformatics.irida.ria.integration.projects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.corefacility.bioinformatics.irida.ria.integration.AbstractIridaUIITChromeDriver;
import ca.corefacility.bioinformatics.irida.ria.integration.pages.LoginPage;
import ca.corefacility.bioinformatics.irida.ria.integration.pages.projects.ProjectSampleMetadataImportPage;

import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.google.common.collect.ImmutableList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DatabaseSetup("/ca/corefacility/bioinformatics/irida/ria/web/projects/ProjectSampleMetadataView.xml")
public class ProjectSampleMetadataImportPageIT extends AbstractIridaUIITChromeDriver {
	private static final String GOOD_FILE_PATH = "src/test/resources/files/metadata-upload/good.xlsx";
	private static final String MIXED_FILE_PATH = "src/test/resources/files/metadata-upload/mixed.xlsx";
	private static final String INVALID_FILE_PATH = "src/test/resources/files/metadata-upload/invalid.xlsx";

	@BeforeEach
	public void init() {
		LoginPage.loginAsManager(driver());
	}

	@Test
	public void testGoodFileAndHeaders() {
		ProjectSampleMetadataImportPage page = ProjectSampleMetadataImportPage.goToPage(driver());
		page.uploadMetadataFile(GOOD_FILE_PATH);
		page.selectSampleNameColumn();
		assertEquals(5, page.getUpdateCount(), "Has incorrect amount of update sample rows");
		assertEquals(0, page.getNewCount(), "Has incorrect amount of new sample rows");

		/*
		Check formatting.  A special check for number column formatting has been added in July 2020.
		Expected: 2.2222 -> 2.22 (if formatting set to 2 decimals). Actual numbers from file, before formatting:
		   2.222222
           2.3666
           1.5689
           63.89756
           59.6666
		 */
		List<Double> values = ImmutableList.of(2.222222, 2.3666, 1.5689, 63.89756, 59.6666)
				.stream()
				.map(num -> BigDecimal.valueOf(num)
						.setScale(2, RoundingMode.HALF_UP)
						.doubleValue())
				.collect(Collectors.toList());
		List<String> formattedNumbers = page.getValuesForColumnByName("Numbers");
		formattedNumbers.forEach(num -> assertTrue(values.contains(Double.valueOf(num)),
				"Found " + num + " that was not formatted properly"));

	}

	@Test
	public void testMixedFileAndHeaders() {
		ProjectSampleMetadataImportPage page = ProjectSampleMetadataImportPage.goToPage(driver());
		page.uploadMetadataFile(MIXED_FILE_PATH);
		page.selectSampleNameColumn();
		assertEquals(5, page.getUpdateCount(), "Has incorrect amount of update sample rows");
		assertEquals(2, page.getNewCount(), "Has incorrect amount of new sample rows");
	}

	@Test
	public void testSuccessfulUpload() {
		ProjectSampleMetadataImportPage page = ProjectSampleMetadataImportPage.goToPage(driver());
		page.uploadMetadataFile(GOOD_FILE_PATH);
		assertEquals("NLEP #", page.getValueForSelectedSampleNameColumn(),
				"Has incorrect pre-populated sample name header");
		page.goToReviewPage();
		page.goToCompletePage();
		assertTrue(page.isSuccessDisplayed(), "Success message did not display");

	}

	@Test
	public void testFailedUpload() {
		ProjectSampleMetadataImportPage page = ProjectSampleMetadataImportPage.goToPage(driver());
		page.uploadMetadataFile(INVALID_FILE_PATH);
		page.goToReviewPage();
		assertTrue(page.isAlertDisplayed(), "Validation message did not display");
	}
}
