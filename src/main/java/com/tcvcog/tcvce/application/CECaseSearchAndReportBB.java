/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.CECasePropertyUnitHeavy;
import com.tcvcog.tcvce.entities.CaseStageEnum;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECase;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECaseList;
import com.tcvcog.tcvce.entities.search.QueryCECase;
import com.tcvcog.tcvce.entities.search.SearchParamsCECase;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 * Manages searches against the CECase table and composing objects
 *
 * @author sylvia
 */
public class CECaseSearchAndReportBB
        extends BackingBeanUtils {

    private SearchParamsCECase searchParamsSelected;
    private List<QueryCECase> queryList;
    private QueryCECase querySelected;
    private boolean appendResultsToList;
    private List<Property> propListForSearch;
    private CaseStageEnum[] caseStageList;

    private List<CECasePropertyUnitHeavy> caseList;
    private List<CECasePropertyUnitHeavy> filteredCaseList;
    

    private ReportConfigCECaseList reportCECaseList;
  
    

    /**
     * Creates a new instance of CECaseSearchBB
     */
    public CECaseSearchAndReportBB() {
    }

    /**
     * Do when I make the bean of this class (after construction)
     */
    @PostConstruct
    public void initBean() {
        CaseCoordinator cc = getCaseCoordinator();
        SearchCoordinator sc = getSearchCoordinator();
        SessionBean sb = getSessionBean();
        
        setCaseList(new ArrayList<>());
        try {
            getCaseList().addAll(cc.cecase_refreshCECasePropertyUnitHeavyList(sb.getSessCECaseList()));
        } catch (BObStatusException ex) {
            System.out.println(ex);
        }
        

        queryList = sc.buildQueryCECaseList(getSessionBean().getSessUser().getMyCredential());
        querySelected = getSessionBean().getQueryCECase();

        configureParameters();

        propListForSearch = sb.getSessPropertyList();
        caseStageList = CaseStageEnum.values();

    }

    /**
     * Loads the first parameter bundle of a selected query object
     */
    private void configureParameters() {
        if (getQuerySelected() != null
                && getQuerySelected().getParamsList() != null
                && !querySelected.getParamsList().isEmpty()) {

            setSearchParamsSelected(getQuerySelected().getParamsList().get(0));

        } else {
            setSearchParamsSelected(null);
        }
    }

    /**
     * Primary listener method which copies a reference to the selected user
     * from the list and sets it on the selected user perch
     *
     * @param cse
     * @return
     */
    public String onObjetViewButtonChange(CECase cse) {
        CaseCoordinator cc = getCaseCoordinator();

       return "ceCaseProfile";
       
    }

    /**
     * Responder to the query button on the UI
     *
     * @param ev
     */
    public void executeQuery(ActionEvent ev) {
        System.out.println("CaseProfileBB.executeQuery: Listener");
        executeQuery();
    }

    /**
     * Logic block for executing selected query
     */
    public void executeQuery() {
        System.out.println("CECaseSearchBB.executeQuery");
        SearchCoordinator sc = getSearchCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
        int listSize = 0;

        if (!isAppendResultsToList()) {
            getCaseList().clear();
        }

        try {
            getCaseList().addAll(sc.runQuery(getQuerySelected()).getResults());
            if (getCaseList() != null) {
                listSize = getCaseList().size();
            }
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Your query completed with " + listSize + " results", ""));
        } catch (SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not query the database, sorry.", ""));
        }
    }

    /**
     * Convenience method for accessing the size of the CECase List
     *
     * @return
     */
    public int getCaseListSize() {
        int s = 0;
        if (getCaseList() != null && !caseList.isEmpty()) {
            s = getCaseList().size();
        }
        return s;

    }

    /**
     * Listener method for requests to load case history
     *
     * @param ev
     */
    public void loadCECaseHistory(ActionEvent ev) {
        CaseCoordinator cc = getCaseCoordinator();
        try {
            getCaseList().addAll(cc.cecase_assembleCECasePropertyUnitHeavyList(cc.cecase_getCECaseHistory(getSessionBean().getSessUser())));
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Case history loaded", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not load case history, sorry.", ""));
        } catch (BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }
    }

    /**
     * Listener method for requests to clear the search results list
     *
     * @param ev
     */
    public void clearCECaseList(ActionEvent ev) {
        if (getCaseList() != null) {
            getCaseList().clear();
        }
    }

    /**
     * Listener method for changes in selected query objects
     *
     */
    public void changeQuerySelected() {
        configureParameters();
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "New query loaded!", ""));
    }

    /**
     * Action listener for query resets
     *
     * @param ev
     */
    public void resetQuery(ActionEvent ev) {
        SearchCoordinator sc = getSearchCoordinator();
        setQueryList(sc.buildQueryCECaseList(getSessionBean().getSessUser().getMyCredential()));
        if (getQueryList() != null && !queryList.isEmpty()) {
            setQuerySelected(getQueryList().get(0));
        }
        configureParameters();
    }
    
    
    // *************************************************************************
    // ******************* CECASE REPORTING ORGANS *****************************
    // *************************************************************************
   
     /**
     * Listener for the report initiation process
     * @return 
     */
    public String prepareReportCECaseListCustomQuery() {
        getSessionBean().setSessCECaseList(getCaseList());
        
//       rpt.setCaseListCustomQueryExport();
        return "ceCaseListExport";
        
    }
    
    
    
    
    
    /**
     * 
     * @param ev 
     */
    public void prepareReportCECaseList(ActionEvent ev) {
        CaseCoordinator cc = getCaseCoordinator();

        if (getReportCECaseList() == null) {
            setReportCECaseList(cc.report_getDefaultReportConfigCECaseList());
        }
        getReportCECaseList().setTitle("Code Enforcement Activity Report");
        getReportCECaseList().setDate_start_val(LocalDateTime.now().minusDays(30));
        getReportCECaseList().setDate_end_val(LocalDateTime.now());
        System.out.println("CaseProfileBB.prepareCaseListReport");

    }
    
    
   

    /**
     * 
     * @return 
     */
    public String generateReportCECaseList() {
        CaseCoordinator cc = getCaseCoordinator();
        getReportCECaseList().setCreator(getSessionBean().getSessUser());
        getReportCECaseList().setMuni(getSessionBean().getSessMuni());
        getReportCECaseList().setGenerationTimestamp(LocalDateTime.now());

        try {
            setReportCECaseList(cc.report_buildCECaseListReport(getReportCECaseList(), getSessionBean().getSessUser()));
        } catch (SearchException ex) {
            System.out.println(ex);
            
        }
        getSessionBean().setReportConfigCECaseList(getReportCECaseList());
        getSessionBean().setReportConfigCECase(null);
        getSessionBean().setSessReport(getReportCECaseList());

        return "reportCECaseList";

    }

    /**
     * @return the searchParamsSelected
     */
    public SearchParamsCECase getSearchParamsSelected() {
        return searchParamsSelected;
    }

    /**
     * @return the queryList
     */
    public List<QueryCECase> getQueryList() {
        return queryList;
    }

    /**
     * @return the querySelected
     */
    public QueryCECase getQuerySelected() {
        return querySelected;
    }

    /**
     * @return the appendResultsToList
     */
    public boolean isAppendResultsToList() {
        return appendResultsToList;
    }

    /**
     * @return the propListForSearch
     */
    public List<Property> getPropListForSearch() {
        return propListForSearch;
    }

    /**
     * @return the caseStageList
     */
    public CaseStageEnum[] getCaseStageList() {
        return caseStageList;
    }

    /**
     * @return the caseList
     */
    public List<CECasePropertyUnitHeavy> getCaseList() {
        return caseList;
    }

    /**
     * @return the filteredCaseList
     */
    public List<CECasePropertyUnitHeavy> getFilteredCaseList() {
        return filteredCaseList;
    }



    /**
     * @return the reportCECaseList
     */
    public ReportConfigCECaseList getReportCECaseList() {
        return reportCECaseList;
    }

    /**
     * @param searchParamsSelected the searchParamsSelected to set
     */
    public void setSearchParamsSelected(SearchParamsCECase searchParamsSelected) {
        this.searchParamsSelected = searchParamsSelected;
    }

    /**
     * @param queryList the queryList to set
     */
    public void setQueryList(List<QueryCECase> queryList) {
        this.queryList = queryList;
    }

    /**
     * @param querySelected the querySelected to set
     */
    public void setQuerySelected(QueryCECase querySelected) {
        this.querySelected = querySelected;
    }

    /**
     * @param appendResultsToList the appendResultsToList to set
     */
    public void setAppendResultsToList(boolean appendResultsToList) {
        this.appendResultsToList = appendResultsToList;
    }

    /**
     * @param propListForSearch the propListForSearch to set
     */
    public void setPropListForSearch(List<Property> propListForSearch) {
        this.propListForSearch = propListForSearch;
    }

    /**
     * @param caseStageList the caseStageList to set
     */
    public void setCaseStageList(CaseStageEnum[] caseStageList) {
        this.caseStageList = caseStageList;
    }

    /**
     * @param caseList the caseList to set
     */
    public void setCaseList(List<CECasePropertyUnitHeavy> caseList) {
        this.caseList = caseList;
    }

    /**
     * @param filteredCaseList the filteredCaseList to set
     */
    public void setFilteredCaseList(List<CECasePropertyUnitHeavy> filteredCaseList) {
        this.filteredCaseList = filteredCaseList;
    }

   
    /**
     * @param reportCECaseList the reportCECaseList to set
     */
    public void setReportCECaseList(ReportConfigCECaseList reportCECaseList) {
        this.reportCECaseList = reportCECaseList;
    }
    
    
}
