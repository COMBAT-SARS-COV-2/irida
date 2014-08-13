package ca.corefacility.bioinformatics.irida.ria.integration.pages.clients;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.corefacility.bioinformatics.irida.ria.integration.pages.BasePage;

public class CreateClientPage {
	private WebDriver driver;
	private static final Logger logger = LoggerFactory.getLogger(CreateClientPage.class);

	private final String CREATE_PAGE = BasePage.URL + "clients/create";
	public static String SUCCESS_PAGE = BasePage.URL + "clients/\\d+";

	public CreateClientPage(WebDriver driver) {
		this.driver = driver;
		driver.get(CREATE_PAGE);
	}

	public void createClientWithDetails(String id, String grant) {
		logger.trace("Creating client with id: " + id + " and grant: " + grant);

		WebElement idField = driver.findElement(By.id("clientId"));
		idField.sendKeys(id);

		WebElement grantField = driver.findElement(By.id("authorizedGrantTypes"));
		grantField.sendKeys(grant);

		WebElement submit = driver.findElement(By.id("create-client-submit"));
		submit.click();
	}

	public boolean checkSuccess() {
		if (driver.getCurrentUrl().matches(SUCCESS_PAGE)) {
			return true;
		} else {
			return false;
		}
	}
}
