package org.imixs.application.admin.mvc;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.mvc.annotation.Controller;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.imixs.application.admin.ConnectionController;

/**
 * The Connect controller is used to establish a connection to Imixs-Worklfow
 * remote interface.
 * 
 * @author rsoika
 *
 */
@Controller
@Path("/connection")
public class LoginController implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(LoginController.class.getName());

	@Inject
	ConnectionController connectionController;

	public LoginController() {
		super();
	}

	@GET
	public String actionHome() {
		return "connect.xhtml";
	}

	@GET
	@Path("/configuration")
	public String showConfiguration() {
		return "configuration.xhtml";
	}

	/**
	 * Calls the ConnectionController to connect. If the connection was successful,
	 * then the search page is shown. Otherwise the connect page is shown.
	 * 
	 * @param url
	 * @param userid
	 * @param password
	 * @return
	 */
	@POST
	public String actionConnect(@FormParam("url") String url, @FormParam("userid") String userid,
			@FormParam("password") String password, @FormParam("authentication") String authentication) {
		logger.info("url=" + url);
		connectionController.setUrl(url);
		connectionController.setUserid(userid);
		connectionController.setPassword(password);

		boolean connected = connectionController.connect(url, userid, password, authentication);

		if (connected) {
			logger.info("...connection: " + url + " = OK");
			// return "search.xhtml";
			return "redirect:query/";
		} else {
			logger.info("...connection: " + url + " = FAILED");
			return "connect.xhtml";
		}

	}

}