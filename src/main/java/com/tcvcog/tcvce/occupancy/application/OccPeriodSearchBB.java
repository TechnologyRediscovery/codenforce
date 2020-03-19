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
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.Credential;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodDataHeavy;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodType;
import com.tcvcog.tcvce.entities.search.QueryOccPeriod;
import com.tcvcog.tcvce.entities.search.SearchParamsOccPeriod;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Ellen Bascomb
 */
public  class   OccPeriodSearchBB 
        extends BackingBeanUtils {

    private OccPeriodDataHeavy currentOccPeriod;
    private List<OccPeriodType> occPeriodTypeList;
    
    private List<QueryOccPeriod> occPeriodQueryList;
    private QueryOccPeriod occPeriodQuerySelected;
    
    private List<Property> propListForSearch;
   
    private List<OccPeriod> occPeriodList;
    private List<OccPeriod> occPeriodListFiltered;
    private boolean appendResultsToList;
    
    private SearchParamsOccPeriod searchParamsSelected;
    
    /**
     * Creates a new instance of OccPeriodsBB
     */
    public OccPeriodSearchBB()  {
    }
    
    @PostConstruct
    public void initBean(){
        occPeriodTypeList = getSessionBean().getSessMuni().getProfile().getOccPeriodTypeList();
        occPeriodList = getSessionBean().getSessOccPeriodList();
        if(occPeriodList != null && occPeriodList.isEmpty()){
            occPeriodList = new ArrayList();
        }
        appendResultsToList = false;
        occPeriodQueryList = getSessionBean().getQueryOccPeriodList();
        if(occPeriodQueryList != null && !occPeriodQueryList.isEmpty()){
            occPeriodQuerySelected = occPeriodQueryList.get(0);
        }
        configureParameters();
        
        
        
        
    }
    
    private void configureParameters(){
        if(occPeriodQuerySelected != null 
                && 
            occPeriodQuerySelected.getParmsList() != null 
                && 
            !occPeriodQuerySelected.getParmsList().isEmpty()){
            
            searchParamsSelected = occPeriodQuerySelected.getParmsList().get(0);
        } else {
            searchParamsSelected = null;
        }
    }
    
     
     /**
     * Loads an OccPeriodDataHeavy and injects it into the session bean 
     * and sends the user to the Workflow/status page
     * @param op
     * @return 
     */
    public String exploreOccPeriod(OccPeriod op) {
        SystemCoordinator sc = getSystemCoordinator();
        PropertyCoordinator pc = getPropertyCoordinator();
        OccupancyCoordinator oc = getOccupancyCoordinator();
        Credential cred = getSessionBean().getSessUser().getMyCredential();
        
        try {
            getSessionBean().setSessOccPeriod(oc.assembleOccPeriodDataHeavy(op, cred));
            getSessionBean().setSessProperty(pc.getPropertyDataHeavyByUnit(op.getPropertyUnitID(), cred));
            sc.logObjectView(getSessionBean().getSessUser(), op);
        } catch (IntegrationException | BObStatusException | AuthorizationException | EventException | SearchException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to assemble the data-rich occ period", ""));
            
        }
        return "occPeriodWorkflow";
        
    }
    
    /**
     * Listener method for requests from the user to clear the results list
     * @param ev 
     */
    public void clearOccPeriodList(ActionEvent ev){
        occPeriodList.clear();
    }
    
    public void loadOccPeriodHistory(ActionEvent ev){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        occPeriodList.addAll(oc.assembleOccPeriodHistoryList(getSessionBean().getSessUser().getMyCredential()));
    }
    
    
    /**
     * Responder to the query button on the UI
     *
     * @param ev
     */
    public void executeQuery(ActionEvent ev) {
        System.out.println("OccPeriodSearchBB.executeQuery");
        
        
        executeQuery();
    }
    
    
    /**
     * Entry way into the Query world via the SearchCoordinator who is responsible
     * for calling appropriate Coordinators and configuration methods and such
     */
    public void executeQuery(){
        SearchCoordinator sc = getSearchCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
        int listSize = 0;
        
        if(appendResultsToList){
            occPeriodList.clear();
        }
        try {
            occPeriodList.addAll(sc.runQuery(occPeriodQuerySelected).getBOBResultList());
            if (occPeriodList != null) {
                listSize = occPeriodList.size();
            }
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Your query completed with " + listSize + " results", ""));
        } catch (SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not query for Periods due to search errors, sorry.", ""));
        }
    }
    
    public void changeQuerySelected(){
        
        configureParameters();
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "New query loaded!", ""));
    }

    public void resetQuery(ActionEvent ev){
        SearchCoordinator sc = getSearchCoordinator();
        occPeriodQueryList = sc.buildQueryOccPeriodList(getSessionBean().getSessUser().getMyCredential());
        if(occPeriodQueryList != null && !occPeriodQueryList.isEmpty()){
            occPeriodQuerySelected = occPeriodQueryList.get(0);
        }
        configureParameters();
        
    }
    
    
    /**
     * @return the searchParamsSelected
     */
    public SearchParamsOccPeriod getSearchParamsSelected() {
        return searchParamsSelected;
    }

    /**
     * @param searchParamsSelected the searchParamsSelected to set
     */
    public void setSearchParamsSelected(SearchParamsOccPeriod searchParamsSelected) {
        this.searchParamsSelected = searchParamsSelected;
    }

    /**
     * @return the currentOccPeriod
     */
    public OccPeriodDataHeavy getCurrentOccPeriod() {
        return currentOccPeriod;
    }

    /**
     * @return the occPeriodList
     */
    public List<OccPeriod> getOccPeriodList() {
        return occPeriodList;
    }

    /**
     * @return the occPeriodListFiltered
     */
    public List<OccPeriod> getOccPeriodListFiltered() {
        return occPeriodListFiltered;
    }

    /**
     * @return the occPeriodQueryList
     */
    public List<QueryOccPeriod> getOccPeriodQueryList() {
        return occPeriodQueryList;
    }

    /**
     * @return the occPeriodQuerySelected
     */
    public QueryOccPeriod getOccPeriodQuerySelected() {
        return occPeriodQuerySelected;
    }

    /**
     * @param currentOccPeriod the currentOccPeriod to set
     */
    public void setCurrentOccPeriod(OccPeriodDataHeavy currentOccPeriod) {
        this.currentOccPeriod = currentOccPeriod;
    }

    /**
     * @param occPeriodList the occPeriodList to set
     */
    public void setOccPeriodList(List<OccPeriod> occPeriodList) {
        this.occPeriodList = occPeriodList;
    }

    /**
     * @param occPeriodListFiltered the occPeriodListFiltered to set
     */
    public void setOccPeriodListFiltered(List<OccPeriod> occPeriodListFiltered) {
        this.occPeriodListFiltered = occPeriodListFiltered;
    }

    /**
     * @param occPeriodQueryList the occPeriodQueryList to set
     */
    public void setOccPeriodQueryList(List<QueryOccPeriod> occPeriodQueryList) {
        this.occPeriodQueryList = occPeriodQueryList;
    }

    /**
     * @param occPeriodQuerySelected the occPeriodQuerySelected to set
     */
    public void setOccPeriodQuerySelected(QueryOccPeriod occPeriodQuerySelected) {
        this.occPeriodQuerySelected = occPeriodQuerySelected;
    }

    /**
     * @return the occPeriodTypeList
     */
    public List<OccPeriodType> getOccPeriodTypeList() {
        return occPeriodTypeList;
    }

    /**
     * @param occPeriodTypeList the occPeriodTypeList to set
     */
    public void setOccPeriodTypeList(List<OccPeriodType> occPeriodTypeList) {
        this.occPeriodTypeList = occPeriodTypeList;
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
    
}
