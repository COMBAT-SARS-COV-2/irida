package ca.corefacility.bioinformatics.irida.ria.integration.pages.user;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Edit user page for selenium testing
 * 
 * @author Thomas Matthews <thomas.matthews@phac-aspc.gc.ca>
 *
 */
public class EditUserPage {

	public static String EDIT_PAGE = "http://localhost:8080/users/1/edit";
	public static String SUCCESS_PAGE = "http://localhost:8080/users/1";
	private WebDriver driver;

	public EditUserPage(WebDriver driver) {
		this.driver = driver;
		driver.get(EDIT_PAGE);
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
		return driver.getCurrentUrl().equals(SUCCESS_PAGE);
	}

}
