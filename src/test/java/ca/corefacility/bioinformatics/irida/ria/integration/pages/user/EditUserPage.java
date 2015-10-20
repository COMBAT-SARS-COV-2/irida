package ca.corefacility.bioinformatics.irida.ria.integration.pages.user;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import ca.corefacility.bioinformatics.irida.ria.integration.pages.AbstractPage;

/**
 * Edit user page for selenium testing
 * 
 *
 */
public class EditUserPage extends AbstractPage {

	public static String EDIT_PAGE = "users/1/edit";

	public EditUserPage(WebDriver driver) {
		super(driver);
	}

	public void goTo() {
		get(driver, EDIT_PAGE);
	}

	public String updateFirstName(String newName) {
		WebElement firstNameBox = driver.findElement(By.id("firstName"));
		firstNameBox.sendKeys(newName);

		driver.findElement(By.className("submit")).click();

		WebElement nameDisplay = driver.findElement(By.id("user-name"));

		return nameDisplay.getText();
	}
	
	public void updatePassword(String password, String confirm){
		WebElement passwordBox = driver.findElement(By.id("password"));
		WebElement confirmPasswordBox = driver.findElement(By.id("confirmPassword"));
		passwordBox.sendKeys(password);
		confirmPasswordBox.sendKeys(confirm);

		driver.findElement(By.className("submit")).click();
	}
	
	public boolean updateSuccess(){
		try {
			waitForElementVisible(By.className("user-details-title"));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
