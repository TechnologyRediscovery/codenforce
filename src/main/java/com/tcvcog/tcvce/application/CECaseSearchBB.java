/*
 * Copyright (C) 2019 Technology Rediscovery LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.CaseLifecycleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECase;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECaseList;
import com.tcvcog.tcvce.entities.search.Query;
import com.tcvcog.tcvce.entities.search.QueryCECase;
import com.tcvcog.tcvce.entities.search.SearchParamsCECase;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author sylvia
 */
public class CECaseSearchBB 
        extends BackingBeanUtils
        implements Serializable {
    
    private CECase currentCase;
    
    private List<CECase> caseList;
    private ArrayList<CECase> filteredCaseList;
    private SearchParamsCECase searchParams;
    
    private List<QueryCECase> queryList;
    private QueryCECase selectedCECaseQuery;
    private Query selectedBOBQuery;
    
    private ArrayList<CECase> filteredCaseHistoryList;
    
    private List<User> usersForSearchConfig;

    
    private ReportConfigCECaseList reportCECaseList;
    
    /**
     * Creates a new instance of CECaseSearchBB
     */
    public CECaseSearchBB() {
    }
    
    
    @PostConstruct
    public void initBean() {
        CaseCoordinator cc = getCaseCoordinator();
        SearchCoordinator sc = getSearchCoordinator();
        UserCoordinator uc = getUserCoordinator();
        
        SessionBean sb = getSessionBean();
        setCurrentCase(sb.getSessionCECase());
        
        setUsersForSearchConfig(uc.assembleUserListForSearchCriteria(null));
        
        setQueryList(sc.prepareQueryCECaseList(getSessionBean().getSessionMuni(), getSessionBean().getSessionUser().getMyCredential()));
        setSelectedCECaseQuery(getSessionBean().getQueryCECase());
        
        setSearchParams(getSelectedCECaseQuery().getSearchParamsList().get(0));
        
        if(!selectedCECaseQuery.isExecutedByIntegrator()){
            try {
                sc.runQuery(getSelectedCECaseQuery());
            } catch (IntegrationException | CaseLifecycleException ex) {
                System.out.println(ex);
            }
        }
        
        setCaseList(getSelectedCECaseQuery().getResults());
        
        ReportConfigCECaseList listRpt = getSessionBean().getReportConfigCECaseList();
        if (listRpt != null) {
            setReportCECaseList(listRpt);
        } else {
            setReportCECaseList(cc.getDefaultReportConfigCECaseList());
        }
    }
    
     /**
     * Primary injection point for setting the case which will be displayed in
     * the right column (the manage object column) on cECases.xhtml
     *
     * @param c the case to be managed--comes from the data table row button
     */
    public void manageCECase(CECase c) {
        SystemIntegrator si = getSystemIntegrator();
        
        System.out.println("CaseProfileBB.manageCECase | caseid: " + c.getCaseID());
        try {
            si.logObjectView_OverwriteDate(getSessionBean().getSessionUser(), c);
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        setCurrentCase(c);
        getSessionBean().setSessionCECase(getCurrentCase());
        getSessionBean().setSessionProperty(c.getProperty());
    }
    
    
    /**
     * Responder to the query button on the UI
     *
     * @param ev
     */
    public void executeQuery(ActionEvent ev) {
        System.out.println("CaseProfileBB.executeQuery");
        executeQuery();
    }
    
    
    
    public void executeQuery(){
        SearchCoordinator sc = getSearchCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
        int listSize = 0;
        
        try {
            setCaseList(sc.runQuery(getSelectedCECaseQuery()).getResults());
            if (getCaseList() != null) {
                listSize = getCaseList().size();
            }
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Your query completed with " + listSize + " results", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not query the database, sorry.", ""));
        } catch (CaseLifecycleException ex) {
            System.out.println(ex);
        }
    }
    
    
    

    
    public void prepareReportCECaseList(ActionEvent ev) {
        CaseCoordinator cc = getCaseCoordinator();

        if (getReportCECaseList() == null) {
            setReportCECaseList(cc.getDefaultReportConfigCECaseList());
        }
        System.out.println("CaseProfileBB.prepareCaseListReport");

    }
    
    
    

    public String generateReportCECaseList() {
        getReportCECaseList().setCreator(getSessionBean().getSessionUser());
        getReportCECaseList().setMuni(getSessionBean().getSessionMuni());
        getReportCECaseList().setGenerationTimestamp(LocalDateTime.now());
        
        getSessionBean().setReportConfigCECaseList(getReportCECaseList());
        getSessionBean().setReportConfigCECase(null);
        getSessionBean().setSessionReport(getReportCECaseList());
        
        return "reportCECaseList";

    }
    
    

    /**
     * @return the currentCase
     */
    public CECase getCurrentCase() {
        return currentCase;
    }

    /**
     * @return the caseList
     */
    public List<CECase> getCaseList() {
        return caseList;
    }

    /**
     * @return the filteredCaseList
     */
    public ArrayList<CECase> getFilteredCaseList() {
        return filteredCaseList;
    }

    /**
     * @return the searchParams
     */
    public SearchParamsCECase getSearchParams() {
        return searchParams;
    }

    /**
     * @return the queryList
     */
    public List<QueryCECase> getQueryList() {
        return queryList;
    }

    /**
     * @return the selectedCECaseQuery
     */
    public QueryCECase getSelectedCECaseQuery() {
          if(selectedBOBQuery instanceof Query){
            setSelectedCECaseQuery((QueryCECase) getSelectedBOBQuery());
        }
        return selectedCECaseQuery;
    }

    /**
     * @return the selectedBOBQuery
     */
    public Query getSelectedBOBQuery() {
        return selectedBOBQuery;
    }

    /**
     * @return the filteredCaseHistoryList
     */
    public ArrayList<CECase> getFilteredCaseHistoryList() {
        return filteredCaseHistoryList;
    }

    /**
     * @return the usersForSearchConfig
     */
    public List<User> getUsersForSearchConfig() {
        return usersForSearchConfig;
    }

    /**
     * @return the reportCECaseList
     */
    public ReportConfigCECaseList getReportCECaseList() {
        return reportCECaseList;
    }

    /**
     * @param currentCase the currentCase to set
     */
    public void setCurrentCase(CECase currentCase) {
        this.currentCase = currentCase;
    }

    /**
     * @param caseList the caseList to set
     */
    public void setCaseList(List<CECase> caseList) {
        this.caseList = caseList;
    }

    /**
     * @param filteredCaseList the filteredCaseList to set
     */
    public void setFilteredCaseList(ArrayList<CECase> filteredCaseList) {
        this.filteredCaseList = filteredCaseList;
    }

    /**
     * @param searchParams the searchParams to set
     */
    public void setSearchParams(SearchParamsCECase searchParams) {
        this.searchParams = searchParams;
    }

    /**
     * @param queryList the queryList to set
     */
    public void setQueryList(List<QueryCECase> queryList) {
        this.queryList = queryList;
    }

    /**
     * @param selectedCECaseQuery the selectedCECaseQuery to set
     */
    public void setSelectedCECaseQuery(QueryCECase selectedCECaseQuery) {
        this.selectedCECaseQuery = selectedCECaseQuery;
    }

    /**
     * @param selectedBOBQuery the selectedBOBQuery to set
     */
    public void setSelectedBOBQuery(Query selectedBOBQuery) {
        this.selectedBOBQuery = selectedBOBQuery;
    }

    /**
     * @param filteredCaseHistoryList the filteredCaseHistoryList to set
     */
    public void setFilteredCaseHistoryList(ArrayList<CECase> filteredCaseHistoryList) {
        this.filteredCaseHistoryList = filteredCaseHistoryList;
    }

    /**
     * @param usersForSearchConfig the usersForSearchConfig to set
     */
    public void setUsersForSearchConfig(List<User> usersForSearchConfig) {
        this.usersForSearchConfig = usersForSearchConfig;
    }

    /**
     * @param reportCECaseList the reportCECaseList to set
     */
    public void setReportCECaseList(ReportConfigCECaseList reportCECaseList) {
        this.reportCECaseList = reportCECaseList;
    }


    

    
}
