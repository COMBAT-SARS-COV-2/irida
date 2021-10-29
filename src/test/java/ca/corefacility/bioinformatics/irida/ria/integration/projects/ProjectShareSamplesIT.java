package ca.corefacility.bioinformatics.irida.ria.integration.projects;

import org.junit.Assert;
import org.junit.Test;

import ca.corefacility.bioinformatics.irida.ria.integration.AbstractIridaUIITChromeDriver;
import ca.corefacility.bioinformatics.irida.ria.integration.pages.LoginPage;
import ca.corefacility.bioinformatics.irida.ria.integration.pages.projects.ProjectSamplesPage;
import ca.corefacility.bioinformatics.irida.ria.integration.pages.projects.ShareSamplesPage;

import com.github.springtestdbunit.annotation.DatabaseSetup;

@DatabaseSetup("/ca/corefacility/bioinformatics/irida/ria/web/projects/ProjectSamplesView.xml")
public class ProjectShareSamplesIT extends AbstractIridaUIITChromeDriver {
	private ShareSamplesPage shareSamplesPage = ShareSamplesPage.initPage(driver());

	@Test
	public void testShareSamples() {
		LoginPage.loginAsManager(driver());
		ProjectSamplesPage samplesPage = ProjectSamplesPage.gotToPage(driver(), 1);
		samplesPage.selectSample(0);
		samplesPage.selectSample(1);
		samplesPage.selectSample(2);
		samplesPage.selectSample(3);
		samplesPage.shareSamples();

		Assert.assertFalse("Share button should be disabled without a project selected",
				shareSamplesPage.isShareButtonDisabled());
		shareSamplesPage.searchForProject("project2");
		Assert.assertTrue("Share button should be enabled after selecting a project",
				shareSamplesPage.isShareButtonDisabled());
		shareSamplesPage.submitShareRequest();

		String foobar = "baz";
	}
}
