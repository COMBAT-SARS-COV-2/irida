package ca.corefacility.bioinformatics.irida.ria.integration.pages.projects;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import ca.corefacility.bioinformatics.irida.ria.integration.utilities.Ajax;
import ca.corefacility.bioinformatics.irida.ria.integration.utilities.SortUtilities;

import com.google.common.base.Strings;

/**
 * <p>
 * Page Object to represent the project samples page.
 * </p>
 *
 * @author Josh Adam <josh.adam@phac-aspc.gc.ca>
 */
public class ProjectSamplesPage {
	public static final String DATE_FORMAT = "dd MMM YYYY";
	private static final String URL = "http://localhost:8080/projects/1/samples";
	private static int TIMEOUT_TIME = 100000;
	private WebDriver driver;

	public ProjectSamplesPage(WebDriver driver) {
		this.driver = driver;
	}

	public void goToPage() throws NoSuchElementException {
		driver.get(URL);
		waitForAjax();
		(new WebDriverWait(driver, 10)).until(ExpectedConditions.presenceOfElementLocated(By
				.id("samplesTable")));
	}

	/**
	 * The the h1 heading for the page
	 * 
	 * @return String value from within the h1 tag
	 */
	public String getTitle() {
		return driver.findElement(By.tagName("h1")).getText();
	}

	/**
	 * Get the number of displayed samples on the page.
	 * 
	 * @return integer value of displayed samples on the page.
	 */
	public int getDisplayedSampleCount() throws NoSuchElementException {
		(new WebDriverWait(driver, 10)).until(ExpectedConditions.presenceOfElementLocated(By
				.id("samplesTable")));
		return driver.findElements(By.cssSelector("tbody tr")).size();
	}

	/**
	 * Get the number of samples that contain files.
	 * 
	 * @return integer value of samples that contain files.
	 */
	public int getCountOfSamplesWithFiles() {
		return driver.findElements(By.cssSelector("td.details-control a")).size();
	}

	/**
	 * Get the number of selected inputs in the samples table body.
	 * 
	 * @return integer value of the number of selected inuts contained within
	 *         the tbody
	 */
	public int getSelectedSampleCount() {
		return driver.findElements(By.cssSelector("tbody input[type=\"checkbox\"]:checked")).size();
	}

	/**
	 * Checks to see if the selectAll checkbox is in an indeterminate state.
	 * 
	 * @return True if in an indeterminate state.
	 */
	public boolean isSelectAllInIndeterminateState() {
		String exists = driver.findElement(By.id("selectAll")).getAttribute("indeterminate");
		if (Strings.isNullOrEmpty(exists)) {
			return false;
		}
		return true;
	}

	/**
	 * Checks to see if the selectAll checkbox is in a checked state.
	 * 
	 * @return True if in a checked state.
	 */
	public boolean isSelectAllSelected() {
		String exists = driver.findElement(By.id("selectAll")).getAttribute("checked");
		if (Strings.isNullOrEmpty(exists)) {
			return false;
		}
		return exists.equals("true");
	}

	/**
	 * Checks to see if the Sample Name column is sorted ascending
	 * 
	 * @return True if entire column is sorted ascending
	 */
	public boolean isSampleNameColumnSortedAsc() {
		(new WebDriverWait(driver, 10)).until(ExpectedConditions.presenceOfElementLocated(By
				.cssSelector("tbody td:nth-child(2)")));
		List<String> list = driver.findElements(By.cssSelector("tbody td:nth-child(2)")).stream()
				.map(WebElement::getText).collect(Collectors.toList());
		return SortUtilities.isStringListSortedAsc(list);
	}

	/**
	 * Checks to see if the Sample Name column is sorted descending
	 *
	 * @return True if entire column is sorted descending
	 */
	public boolean isSampleNameColumnSortedDesc() {
		List<String> list = driver.findElements(By.cssSelector("tbody td:nth-child(2)")).stream()
				.map(WebElement::getText).collect(Collectors.toList());
		return SortUtilities.isStringListSortedDesc(list);
	}

	/**
	 * Checks to see if the Sample Added On column is sorted ascending
	 *
	 * @return True if entire column is sorted ascending
	 */
	public boolean isAddedOnDateColumnSortedAsc() {
		(new WebDriverWait(driver, 10)).until(ExpectedConditions.presenceOfElementLocated(By
				.cssSelector("tbody td:nth-child(4)")));
		List<String> list = driver.findElements(By.cssSelector("tbody td:nth-child(4)")).stream()
				.map(WebElement::getText).collect(Collectors.toList());
		return SortUtilities.isDateSortedAsc(list, DATE_FORMAT);
	}

	/**
	 * Checks to see if the Sample Added On column is sorted descending
	 *
	 * @return True if entire column is sorted descending
	 */
	public boolean isAddedOnDateColumnSortedDesc() {
		(new WebDriverWait(driver, 10)).until(ExpectedConditions.presenceOfElementLocated(By
				.cssSelector("tbody td:nth-child(4)")));
		List<String> list = driver.findElements(By.cssSelector("tbody td:nth-child(4)")).stream()
				.map(WebElement::getText).collect(Collectors.toList());
		return SortUtilities.isDateSortedDesc(list, DATE_FORMAT);
	}

	/**
	 * Checks to see if the is an input field named "editName" visible and that
	 * there is only one.
	 * 
	 * @return true if there is 1 visisble
	 */
	public boolean isRenameInputVisible() {
		return driver.findElements(By.id("editName")).size() == 1;
	}

    public String getSampleName() {
        return driver.findElement(By.cssSelector("tbody tr:nth-child(1) td:nth-child(2)")).getText();
    }

	/**
	 * Test to show that the noty message was shown.
	 * 
	 * @return True if the message area is present.
	 */
    public boolean successMessageShown() {
        return driver.findElements(By.cssSelector(".noty_message")).size() > 0;
    }

	/**
	 * Test to see fi the qtip message is shown.
	 * 
	 * @return True if the qtip message is shown.
	 */
	public boolean hasErrorMessage() {
        return driver.findElements(By.className("qtip-content")).size() == 1;
    }

    /**
	 * Test to see if there is a formatting message displayed.
	 * 
	 * @return True if the words "space character" is shown.
	 */
	public boolean hasFormattingMessage() {
        return driver.findElement(By.className("qtip-content")).getText().contains("space character");
    }

    /**
	 * Check to see if the delete button is disabled
	 * 
	 * @return Return true if the button is disabled
	 */
	public boolean isDeleteBtnDisabled() {
		return driver.findElement(By.id("deleteBtn")).getAttribute("disabled").equals("true");
	}

	/**
	 * Check to see if the table empty row is displayed
	 * 
	 * @return True if the table empty row is displayed.
	 */
	public boolean isTableEmptyRowShown() {
		return driver.findElements(By.className("dataTables_empty")).size() == 1;
	}

    /**
     * Determine if the modal to combine samples is open
     * @return true if the modal is open
     */
    public boolean isCombineSamplesModalOpen() {
        return driver.findElements(By.cssSelector(".noty_bar h2")).size() == 1;
    }

    /**
     * Get the name of the sample in a particular row
     * @param rowNum The row to look for the sample name in
     * @return The name of the sampel in the row
     */
    public String getSampleNameForRow(int rowNum) {
        return driver.findElement(By.cssSelector("tbody tr:nth-child(" + rowNum + ") .name")).getText();
    }

    /**
     * Determine if there is an error displayed for a samples merge.
     * @return
     */
    public boolean isSampleMergeErrorDisplayed() {
	    (new WebDriverWait(driver, 10)).until(ExpectedConditions.presenceOfElementLocated(By
			    .id("merge-error")));
	    return true;
    }

	public boolean isFilesViewOpen() throws NoSuchElementException {
		(new WebDriverWait(driver, 10)).until(ExpectedConditions.presenceOfElementLocated(By
				.id("files-view")));
		return true;
	}

	public int getDisplayedFilesCount() {
		return driver.findElements(By.className("files-name")).size();
	}

	public boolean isFilesViewControllerIndeterminate() {
		String exists = driver.findElement(By.cssSelector("tbody tr:nth-child(5) .sampleCB")).getAttribute(
				"indeterminate");
		if (Strings.isNullOrEmpty(exists)) {
			return false;
		}
		return exists.equals("true");
	}

	public boolean isFilesViewControllerSelected() {
		String exists = driver.findElement(By.cssSelector("tbody tr:nth-child(5) .sampleCB")).getAttribute("indeterminate");
		if (Strings.isNullOrEmpty(exists)) {
			return false;
		}
		return true;
	}

	public boolean areAllFilesSelected() {
		boolean result = true;
		List<WebElement> cbs = driver.findElements(By.className("fileCB"));
		for (WebElement e : cbs) {
			String exists = e.getAttribute("checked");
			if(Strings.isNullOrEmpty(exists)) {
				result = false;
			}
		}
		return result;
	}

	public boolean ensureCorrectFilesSelected(int[] checkboxes) {
		boolean result = true;
		List<WebElement> cbs = driver.findElements(By.className("fileCB"));
		for (int i = 0; i < checkboxes.length; i++) {
			if (cbs.get(checkboxes[i]).getAttribute("checked").equals("false")) {
				result = false;
				break;
			}
		}
		return result;
	}

	/**************************************************************************************
	 * EVENTS
	 **************************************************************************************/

	/**
	 * Click on the Select All Checkbox
	 */
	public void clickSelectAllCheckbox() {
		driver.findElement(By.id("selectAll")).click();
	}

	/**
	 * Click on the first checkbox in the tbody.
	 */
	public void clickFirstSampleCheckbox() {
		driver.findElement(By.cssSelector("tbody > tr input[type=\"checkbox\"]")).click();
	}

	/**
	 * Open the row that contains files.
	 */
	public void showFilesView() {
		(new WebDriverWait(driver, 10)).until(ExpectedConditions.presenceOfElementLocated(By
				.className("fileViewLink")));
		driver.findElement(By.className("fileViewLink")).click();
		(new WebDriverWait(driver, 10)).until(ExpectedConditions.presenceOfElementLocated(By
				.id("files-view")));
	}

	public void hideFilesView() {
		(new WebDriverWait(driver, 10)).until(ExpectedConditions.presenceOfElementLocated(By
				.id("files-view")));
		driver.findElement(By.className("fileViewLink")).click();
		(new WebDriverWait(driver, 10)).until(ExpectedConditions.invisibilityOfElementLocated(By
				.id("files-view")));
	}

	/**
	 * Click the table header for the samples name. This will enable sorting by
	 * that column.
	 */
	public void clickSampleNameHeader() {
		driver.findElement(By.cssSelector("thead th:nth-child(2)")).click();
		waitForAjax();
	}

	/**
	 * Click the table header for the samples added on date.. This will enable
	 * sorting by that column.
	 */
	public void clickCreatedDateHeader() {
		driver.findElement(By.cssSelector("thead th:nth-child(4)")).click();
		waitForAjax();
	}

	/**
	 * Display the sample edit
	 */
	public void clickEditFirstSample() {
		driver.findElement(By.cssSelector("tbody tr:nth-child(1) button.edit")).click();
	}

	public void selectFirstSample() {
		driver.findElement(By.cssSelector("tbody tr:nth-child(1) input[type=\"checkbox\"]")).click();
	}

	/**
	 * Rename a sample
	 */
	public void sendRenameSample(String name) {
        WebElement el = driver.findElement(By.id("editName"));
        el.sendKeys(name);
        el.sendKeys(Keys.ENTER);
        waitForAjax();
    }

    /**
     * Click on the cancel edit button
     */
    public void clickOnEditCancel() {
        driver.findElement(By.cssSelector("button.cancel")).click();
    }

    /**
     * Delete selected samples samples
     */
	public void clickDeleteSamples() {
		driver.findElement(By.id("deleteBtn")).click();
		WebDriverWait wait = new WebDriverWait(driver, TIMEOUT_TIME);
		wait.until(ExpectedConditions.elementToBeClickable(By.id("button-0")));
		driver.findElement(By.id("button-0")).click();
		waitForAjax();
	}
	
	/**
	 * Click copy samples button
	 */
	public void copySamples(String id) throws NoSuchElementException {
		driver.findElement(By.id("copyBtn")).click();
		(new WebDriverWait(driver, 10))
				.until(ExpectedConditions.presenceOfElementLocated(By.className("noty_message")));
		WebElement projectBox = driver.findElement(By.id("copy-sample-project"));
		projectBox.sendKeys(id);
		(new WebDriverWait(driver, 10)).until(ExpectedConditions.elementToBeClickable(By.id("button-0")));
		driver.findElement(By.id("button-0")).click();
	}

    /**
     * Select the first three samples in the table
     */
    public void clickFirstThreeCheckboxes() {
        List<WebElement> checkboxes = driver.findElements(By.cssSelector("tbody input[type=\"checkbox\"]"));
        for (int i = 0; i < 3; i++) {
            checkboxes.get(i).click();
        }
    }

    /**
     * Click on the combine samples button
     */
    public void clickCombineSamples() {
        driver.findElement(By.id("combineBtn")).click();
		(new WebDriverWait(driver, 10)).until(ExpectedConditions.presenceOfElementLocated(By
				.className("noty_message")));
    }

	public void clickOnFileCheckBox(int boxNum) {
		List<WebElement> cbs = driver.findElements(By.className("fileCB"));
		cbs.get(boxNum).click();
	}

    /**
     * When combining samples this will select and item in the select2 box based on the name passed.
     * If name is not present the name will be the new name
     * @param name The new name for the samples
     */
    public void selectTheMergedSampleName(String name) {
	    (new WebDriverWait(driver, 10))
			    .until(ExpectedConditions.presenceOfElementLocated(By.className("select2-choice")));
        WebElement el = driver.findElement(By.className("select2-choice"));
        el.click();
        el.sendKeys(name);
        el.sendKeys(Keys.TAB);
        driver.findElement(By.cssSelector(".noty_buttons .btn-primary")).click();
        waitForAjax();
    }

	public void clickRunPipelineButton() {
		driver.findElement(By.id("runPipelineBtn")).click();
		(new WebDriverWait(driver, 10)).until(ExpectedConditions.presenceOfElementLocated(By
				.className("mfp-content")));
	}

	private void waitForAjax() {
		Wait<WebDriver> wait = new WebDriverWait(driver, 60);
		wait.until(Ajax.waitForAjax(60000));
	}
}
