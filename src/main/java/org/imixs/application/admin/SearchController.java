package org.imixs.application.admin;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.imixs.melman.BasicAuthenticator;
import org.imixs.melman.FormAuthenticator;
import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.services.rest.RestClient;

import jakarta.enterprise.context.ConversationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * The ConnectionController stores the connection data to an Imixs Workflow
 * Instance Endpoint
 * <p>
 * The endpoint defines the URL to the rest API. The key is the userID or cookie
 * used for authentication. The token is the user password or cookie value for
 * authentication. The type defines the login type.
 *
 * @author rsoika
 *
 */
@Named
@ConversationScoped
public class SearchController implements Serializable {

    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(SearchController.class.getName());

    private String query = "(type:workitem)";// default query

    private int pageSize;
    private int pageIndex;
    private String sortBy;

    @Inject
    ConnectionController connectionController;

    public void search() {
        logger.info("...search: " + query);
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    /**
     * Computes the search result based on the current query data
     *
     * @return
     */
    public List<ItemCollection> getSearchResult() {
        List<ItemCollection> result = new ArrayList<ItemCollection>();

        logger.info("...compute searchResult...");

        // FormBasedAuthenticationHelper helper = new
        // FormBasedAuthenticationHelper(connectionController.getEndpoint());

        WorkflowClient workflowClient = connectionController.getWorkflowClient();

//        // Init the workflowClient with a basis URL
//        WorkflowClient workflowClient = new WorkflowClient(connectionController.getEndpoint());
//        if ("BASIC".equals(connectionController.getType())) {
//            // Create a authenticator
//            BasicAuthenticator basicAuth = new BasicAuthenticator(connectionController.getKey(),
//                    connectionController.getToken());
//            // register the authenticator
//            workflowClient.registerClientRequestFilter(basicAuth);
//        }
//        if ("FORM".equals(connectionController.getType())) {
//            // Create a authenticator
//
//            logger.info("..form connect:" + connectionController.getEndpoint() + " - " + connectionController.getKey()
//                    + " - " + connectionController.getToken());
//            // connectionController.getEndpoint() "https://demo.internal.office-workflow.de"
//
//            // === Dieser Code funktioniert
////            try {
////                logger.info("wir testen mal den imixs-workflow rest client");
////                RestClient restClient = new RestClient(connectionController.getEndpoint());
////                org.imixs.workflow.services.rest.FormAuthenticator fa = new org.imixs.workflow.services.rest.FormAuthenticator(
////                        "https://demo.internal.office-workflow.de", connectionController.getKey(),
////                        connectionController.getToken());
////                restClient.registerRequestFilter(fa);
////
////                String rResult = restClient.get("/api/model");
////                logger.info(" rResult Staus=" + restClient.getLastHTTPResult());
////                logger.info(" page result=" + rResult);
////            } catch (org.imixs.workflow.services.rest.RestAPIException e) {
////                // TODO Auto-generated catch block
////                e.printStackTrace();
////            }
//            // ===============
//
//            FormAuthenticator formAuth = new FormAuthenticator(connectionController.getEndpoint(),
//                    connectionController.getKey(), connectionController.getToken());
//            // register the authenticator
//            workflowClient.registerClientRequestFilter(formAuth);
//        }

        try {
            workflowClient.setPageIndex(getPageIndex());
            workflowClient.setPageSize(getPageSize());
            workflowClient.setSortBy(getSortBy());
            workflowClient.setSortOrder(getSortBy(), false);

            // result = workflowClient.getCustomResource("/documents/search/" + getQuery());
            result = workflowClient.searchDocuments(getQuery());
            logger.info("...found " + result.size() + " results");
        } catch (RestAPIException | UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }

}