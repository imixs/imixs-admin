package org.imixs.application.admin;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.imixs.melman.RestAPIException;
import org.imixs.melman.WorkflowClient;
import org.imixs.workflow.ItemCollection;

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

    public static final String ITEM_LIST = "$uniqueid,$created,$modified,$workflowstatus,$workflowsummary,type,name,txtname";

    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(SearchController.class.getName());

    private String query = "(type:workitem)";// default query
    private int pageSize = 10;
    private int pageIndex;
    private String sortBy = "$modified";
    private boolean sortOrder = true;
    private List<ItemCollection> searchResult = null;

    @Inject
    ConnectionController connectionController;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
        searchResult = null;
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

    public boolean isSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(boolean sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void nextPage() {
        pageIndex++;
        searchResult = null;
    }

    public void prevPage() {
        if (pageIndex > 0) {
            pageIndex--;
            searchResult = null;
        }
    }

    public void reset() {
        searchResult = null;
    }

    /**
     * Computes the search result based on the current query data from the REST
     * endpoint:
     * <p>
     * /documents/search/QUERY
     * <p>
     * The search result is cached during the conversation state.
     *
     * @return
     */
    public List<ItemCollection> getSearchResult() {

        if (searchResult == null) {
            WorkflowClient workflowClient = connectionController.getWorkflowClient();
            if (workflowClient != null) {
                try {
                    long l = System.currentTimeMillis();
                    workflowClient.setPageIndex(getPageIndex());
                    workflowClient.setPageSize(getPageSize());
                    workflowClient.setSortBy(getSortBy());
                    workflowClient.setSortOrder(getSortBy(), isSortOrder());
                    workflowClient.setItems(ITEM_LIST);
                    // result = workflowClient.getCustomResource("/documents/search/" + getQuery());
                    searchResult = workflowClient.searchDocuments(getQuery());
                    logger.info("...found " + searchResult.size() + " results in " + (System.currentTimeMillis() - l)
                            + "ms");
                } catch (RestAPIException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return searchResult;
    }

}