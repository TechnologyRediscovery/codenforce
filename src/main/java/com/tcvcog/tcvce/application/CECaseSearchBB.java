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
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.CECasePropertyUnitHeavy;
import com.tcvcog.tcvce.entities.CaseStageEnum;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECaseList;
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

    private CECasePropertyUnitHeavy currentCase;

    private List<CECasePropertyUnitHeavy> caseList;
    private List<CECasePropertyUnitHeavy> filteredCaseList;
    private SearchParamsCECase searchParamsSelected;

    private List<QueryCECase> queryList;
    private QueryCECase querySelected;
    private boolean appendResultsToList;

    private List<Property> propListForSearch;
    private CaseStageEnum[] caseStageList;

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

        SessionBean sb = getSessionBean();
        currentCase = (sb.getSessCECase());

        queryList = sc.buildQueryCECaseList(getSessionBean().getSessUser().getMyCredential());
        querySelected = getSessionBean().getQueryCECase();

        configureParameters();

        caseList = new ArrayList<>();
        caseList.addAll(sb.getSessCECaseList());

        propListForSearch = sb.getSessPropertyList();
        caseStageList = CaseStageEnum.values();

        ReportConfigCECaseList listRpt = getSessionBean().getReportConfigCECaseList();
        if (listRpt != null) {
            reportCECaseList = listRpt;
        } else {
            reportCECaseList = cc.getDefaultReportConfigCECaseList();
        }
    }

    /**
     * Loads the first parameter bundle of a selected query object
     */
    private void configureParameters() {
        if (querySelected != null
                && querySelected.getParamsList() != null
                && !querySelected.getParamsList().isEmpty()) {

            searchParamsSelected = querySelected.getParamsList().get(0);

        } else {
            searchParamsSelected = null;
        }
    }

    /**
     * Primary injection point for setting the case which will be displayed in
     * the right column (the manage object column) on cECases.xhtml
     *
     * @param c the case to be managed--comes from the data table row button
     * @return
     */
    public String manageCECase(CECase c) {
        SystemIntegrator si = getSystemIntegrator();
        CaseCoordinator cc = getCaseCoordinator();
        PropertyCoordinator pc = getPropertyCoordinator();

        System.out.println("CaseProfileBB.manageCECase | caseid: " + c.getCaseID());
        try {
            si.logObjectView(getSessionBean().getSessUser(), c);

            getSessionBean().setSessCECase(cc.assembleCECaseDataHeavy(
                    currentCase,
                    getSessionBean().getSessUser().getMyCredential()));

            getSessionBean().setSessProperty(pc.assemblePropertyDataHeavy(
                    cc.assembleCECasePropertyUnitHeavy(c, getSessionBean().getSessUser().getMyCredential()).getProperty(),
                    getSessionBean().getSessUser().getMyCredential()));

        } catch (BObStatusException | SearchException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        } catch (IntegrationException ex) {

            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Cannot load full case with data.", ""));
        }
        return "ceCaseWorkflow";

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

    /**
     * Logic block for executing selected query
     */
    public void executeQuery() {
        System.out.println("CECaseSearchBB.executeQuery");
        SearchCoordinator sc = getSearchCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
        int listSize = 0;

        if (!appendResultsToList) {
            caseList.clear();
        }

        try {
            caseList.addAll(sc.runQuery(getQuerySelected()).getResults());
            if (caseList != null) {
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
        if (caseList != null && !caseList.isEmpty()) {
            s = caseList.size();
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
            caseList.addAll(cc.assembleCECasePropertyUnitHeavyList(cc.assembleCaseHistory(getSessionBean().getSessUser().getMyCredential())));
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Case history loaded", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not load case history, sorry.", ""));
        } catch (SearchException | BObStatusException ex) {
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
        if (caseList != null) {
            caseList.clear();
        }
    }

    /**
     * Listener method for changes in selected query objects
     *
     * @param ev
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
        queryList = sc.buildQueryCECaseList(getSessionBean().getSessUser().getMyCredential());
        if (queryList != null && !queryList.isEmpty()) {
            querySelected = queryList.get(0);
        }
        configureParameters();
    }

    public void prepareReportCECaseList(ActionEvent ev) {
        CaseCoordinator cc = getCaseCoordinator();

        if (getReportCECaseList() == null) {
            setReportCECaseList(cc.getDefaultReportConfigCECaseList());
        }
        System.out.println("CaseProfileBB.prepareCaseListReport");

    }

    public String generateReportCECaseList() {
        getReportCECaseList().setCreator(getSessionBean().getSessUser());
        getReportCECaseList().setMuni(getSessionBean().getSessMuni());
        getReportCECaseList().setGenerationTimestamp(LocalDateTime.now());

        getSessionBean().setReportConfigCECaseList(getReportCECaseList());
        getSessionBean().setReportConfigCECase(null);
        getSessionBean().setSessReport(getReportCECaseList());

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
     * @return the reportCECaseList
     */
    public ReportConfigCECaseList getReportCECaseList() {
        return reportCECaseList;
    }

    /**
     * @param currentCase the currentCase to set
     */
    public void setCurrentCase(CECaseDataHeavy currentCase) {
        this.currentCase = currentCase;
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
     * @param reportCECaseList the reportCECaseList to set
     */
    public void setReportCECaseList(ReportConfigCECaseList reportCECaseList) {
        this.reportCECaseList = reportCECaseList;
    }

    /**
     * @return the appendResultsToList
     */
    public boolean isAppendResultsToList() {
        return appendResultsToList;
    }

    /**
     * @param appendResultsToList the appendResultsToList to set
     */
    public void setAppendResultsToList(boolean appendResultsToList) {
        this.appendResultsToList = appendResultsToList;
    }

    /**
     * @return the propListForSearch
     */
    public List<Property> getPropListForSearch() {
        return propListForSearch;
    }

    /**
     * @param propListForSearch the propListForSearch to set
     */
    public void setPropListForSearch(List<Property> propListForSearch) {
        this.propListForSearch = propListForSearch;
    }

    /**
     * @return the caseStageList
     */
    public CaseStageEnum[] getCaseStageList() {
        return caseStageList;
    }

    /**
     * @param caseStageList the caseStageList to set
     */
    public void setCaseStageList(CaseStageEnum[] caseStageList) {
        this.caseStageList = caseStageList;
    }

}
