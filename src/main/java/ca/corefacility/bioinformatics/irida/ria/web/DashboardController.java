package ca.corefacility.bioinformatics.irida.ria.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * User Login Page Controller
 * 
 * @author Josh Adam <josh.adam@phac-aspc.gc.ca>
 */
@Controller
public class DashboardController {
	public static final String DASHBOARD_PAGE = "index";
	private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

	@RequestMapping(value = "/dashboard")
	public String showIndex() {
		logger.debug("Displaying dashboard page");
		return DASHBOARD_PAGE;
	}
}
