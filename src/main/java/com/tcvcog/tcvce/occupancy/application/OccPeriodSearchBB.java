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
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodDataHeavy;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodPropertyUnitHeavy;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodType;
import com.tcvcog.tcvce.entities.search.QueryOccPeriod;
import com.tcvcog.tcvce.entities.search.SearchParamsOccPeriod;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Ellen Bascomb
 */
public  class   OccPeriodSearchBB 
        extends BackingBeanUtils {

    private OccPeriodDataHeavy currentOccPeriod;
    private PropertyUnit currentPropertyUnit;
    
//    *******************************
//    ************ WORKFLOW**********
//    *******************************
      private boolean periodStartDateNull;
    private boolean periodEndDateNull;
    
     private String formNoteText;
    
    private List<OccPeriodType> occPeriodTypeList;
    private OccPeriodType selectedOccPeriodType;
    
    private List<PropertyUnit> propertyUnitCandidateList;
    private PropertyUnit selectedPropertyUnit;
    
    private List<User> managerInspectorCandidateList;
    private User selectedManager;
    
    
    
//    *******************************
//    ************ SEARCH ***********
//    *******************************
    private List<OccPeriodType> search_occPeriodTypeList;
    
    private List<QueryOccPeriod> occPeriodQueryList;
    private QueryOccPeriod occPeriodQuerySelected;
    
    private List<Property> search_propList;
    protected List<Person> search_personList;
   
    private List<OccPeriodPropertyUnitHeavy> occPeriodList;
    private List<OccPeriodPropertyUnitHeavy> occPeriodListFiltered;
    private boolean appendResultsToList;
    
    private SearchParamsOccPeriod searchParamsSelected;
    
    /**
     * Creates a new instance of OccPeriodsBB
     */
    public OccPeriodSearchBB()  {
    }
    
    @PostConstruct
    public void initBean(){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        
        
          currentOccPeriod = getSessionBean().getSessOccPeriod();
        PropertyIntegrator pi = getPropertyIntegrator();
        
        periodEndDateNull = false;
        periodStartDateNull = false;
        occPeriodTypeList = getSessionBean().getSessMuni().getProfile().getOccPeriodTypeList();
        
//        try {
//            currentPropertyUnit = pc.getPropertyUnitWithProp(currentOccPeriod.getPropertyUnitID());
//            // TODO
//            propertyUnitCandidateList = getSessionBean().getSessProperty().getUnitList();
//            
//        } catch (IntegrationException ex) {
//            System.out.println(ex);
//        }
        
        
        search_occPeriodTypeList = getSessionBean().getSessMuni().getProfile().getOccPeriodTypeList();
        try {
            occPeriodList = oc.getOccPeriodPropertyUnitHeavy(getSessionBean().getSessOccPeriodList());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        if(occPeriodList != null && occPeriodList.isEmpty()){
            occPeriodList = new ArrayList();
        }
        appendResultsToList = false;
        occPeriodQueryList = getSessionBean().getQueryOccPeriodList();
        if(occPeriodQueryList != null && !occPeriodQueryList.isEmpty()){
            occPeriodQuerySelected = occPeriodQueryList.get(0);
        }
        search_propList = getSessionBean().getSessPropertyList();
        search_personList = getSessionBean().getSessPersonList();
        
        configureParameters();
    }
    
    private void configureParameters(){
        if(occPeriodQuerySelected != null 
                && 
            occPeriodQuerySelected.getParamsList() != null 
                && 
            !occPeriodQuerySelected.getParamsList().isEmpty()){
            
            searchParamsSelected = occPeriodQuerySelected.getParamsList().get(0);
        } else {
            searchParamsSelected = null;
        }
    }
    
    
    
    public void reloadCurrentOccPeriodDataHeavy(){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            currentOccPeriod = oc.assembleOccPeriodDataHeavy(currentOccPeriod, getSessionBean().getSessUser().getMyCredential());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Reloaded occ period ID " + currentOccPeriod.getPeriodID(), ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Unable to reload occ period", ""));
        } catch (SearchException ex) {
            System.out.println(ex);
        }
        
    }
    
       
    
    public int getPeriodListSize(){
        int s = 0;
        if(occPeriodList != null && !occPeriodList.isEmpty()){
            s = occPeriodList.size();
        }
        return s;
    }
     
     public void certifyOccPeriodField(ActionEvent ev){
         
         OccupancyCoordinator oc = getOccupancyCoordinator();
         FacesContext context = getFacesContext();
         String field = context.getExternalContext().getRequestParameterMap().get("fieldtocertify");
         String certifymode = context.getExternalContext().getRequestParameterMap().get("certifymode");
         
         System.out.println("OccInspectionBB.certifuyOccPeriodField | field: " + field + " | mode: " + certifymode);
         
         UserAuthorized u = getSessionBean().getSessUser();
         LocalDateTime now = LocalDateTime.now();
         
         switch(field){
            case "authorization":
                currentOccPeriod.setAuthorizedBy(u);
                currentOccPeriod.setAuthorizedTS(now);
                if(certifymode.equals("withdraw")){
                    currentOccPeriod.setAuthorizedBy(null);
                    currentOccPeriod.setAuthorizedTS(null);
                }
                break;
            case "occperiodtype":
                currentOccPeriod.setPeriodTypeCertifiedBy(u);
                currentOccPeriod.setPeriodTypeCertifiedTS(now);
                if(certifymode.equals("withdraw")){
                    currentOccPeriod.setPeriodTypeCertifiedBy(null);
                    currentOccPeriod.setPeriodTypeCertifiedTS(null);
                }
                break;
            case "startdate":
                if(periodStartDateNull){
                    currentOccPeriod.setStartDate(null);
                }
                currentOccPeriod.setStartDateCertifiedBy(u);
                currentOccPeriod.setStartDateCertifiedTS(now);
                if(certifymode.equals("withdraw")){
                    currentOccPeriod.setStartDateCertifiedBy(null);
                    currentOccPeriod.setStartDateCertifiedTS(null);
                }
                break;
            case "enddate":
                if(periodEndDateNull){
                    currentOccPeriod.setEndDate(null);
                }
                currentOccPeriod.setEndDateCertifiedBy(u);
                currentOccPeriod.setEndDateCertifiedTS(now);
                if(certifymode.equals("withdraw")){
                    currentOccPeriod.setEndDateCertifiedBy(null);
                    currentOccPeriod.setEndDateCertifiedTS(null);
                }
                break;
            default:
                getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Error! Unable to certify field", ""));
         }
         
        try {
            oc.editOccPeriod(currentOccPeriod, u);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully udpated field status!", ""));
        } catch (IntegrationException | BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
            System.out.println(ex);
            
        }
        reloadCurrentOccPeriodDataHeavy();
         
     }
     
     
     public void authorizeOccPeriod(ActionEvent ev){
         OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.authorizeOccPeriod(currentOccPeriod, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_INFO,
               "Success! Occupancy period ID " + currentOccPeriod.getPeriodID() 
                       + " is now authorized and permits can be generated.", ""));
        } catch (AuthorizationException | BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_ERROR,
               ex.getMessage(), ""));
        }
     }
       /**
      * utility pass through method to be called when loading Occperiod advanced settings
      * @param ev 
      */
     public void updateOccPeriodInitialize(ActionEvent ev){
         
     }
     
     public void updateOccPeriodCommit(){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        if(selectedManager != null){
            currentOccPeriod.setManager(selectedManager);
        }
        
        if(selectedOccPeriodType != null){
            currentOccPeriod.setType(selectedOccPeriodType);
        }
        
        try {
            oc.editOccPeriod(currentOccPeriod, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Update successful on OccPeriod ID: " + currentOccPeriod.getPeriodID(), ""));
        } catch (IntegrationException | BObStatusException ex) {
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                ex.getMessage(), ""));
        }
     }
     public void updatePeriodPropUnit(){
//         OccupancyCoordinator oc = getOccupancyCoordinator();
//        try {
//            oc.updateOccPeriodPropUnit(currentOccPeriod, getSelectedPropertyUnit());
//             getFacesContext().addMessage(null,
//                new FacesMessage(FacesMessage.SEVERITY_INFO,
//                "The current occupancy period has been assigned to property unit ID " + getSelectedPropertyUnit().getUnitID(), ""));
//        } catch (IntegrationException ex) {
//            System.out.println(ex);
//             getFacesContext().addMessage(null,
//                new FacesMessage(FacesMessage.SEVERITY_ERROR,
//                ex.getMessage(), ""));
//        }
//        reloadCurrentOccPeriodDataHeavy();
     }
     
     /**
      * Logic container for liasing with the OccCoor to
      * update managers
      * @param ev 
      */
     public void updateOccPeriodManager(ActionEvent ev){
         
         
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
            getSessionBean().setSessProperty(pc.getPropertyDataHeavyByUnit(op.getPropertyUnitID(), getSessionBean().getSessUser()));
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
        if(occPeriodList !=  null){
            occPeriodList.clear();
        }
    }
    
    public void loadOccPeriodHistory(ActionEvent ev){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            occPeriodList.addAll(oc.getOccPeriodPropertyUnitHeavy(oc.assembleOccPeriodHistoryList(getSessionBean().getSessUser().getMyCredential())));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_ERROR,
                           "Unable to assemble the data-rich occ period", ""));
            
        }
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
        
        if(!appendResultsToList){
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
    public List<OccPeriodPropertyUnitHeavy> getOccPeriodList() {
        return occPeriodList;
    }

    /**
     * @return the occPeriodListFiltered
     */
    public List<OccPeriodPropertyUnitHeavy> getOccPeriodListFiltered() {
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
    public void setOccPeriodList(List<OccPeriodPropertyUnitHeavy> occPeriodList) {
        this.occPeriodList = occPeriodList;
    }

    /**
     * @param occPeriodListFiltered the occPeriodListFiltered to set
     */
    public void setOccPeriodListFiltered(List<OccPeriodPropertyUnitHeavy> occPeriodListFiltered) {
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
     * @return the search_occPeriodTypeList
     */
    public List<OccPeriodType> getSearch_occPeriodTypeList() {
        return search_occPeriodTypeList;
    }

    /**
     * @param search_occPeriodTypeList the search_occPeriodTypeList to set
     */
    public void setSearch_occPeriodTypeList(List<OccPeriodType> search_occPeriodTypeList) {
        this.search_occPeriodTypeList = search_occPeriodTypeList;
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
     * @return the search_propList
     */
    public List<Property> getSearch_propList() {
        return search_propList;
    }

    /**
     * @param search_propList the search_propList to set
     */
    public void setSearch_propList(List<Property> search_propList) {
        this.search_propList = search_propList;
    }

    /**
     * @return the search_personList
     */
    public List<Person> getSearch_personList() {
        return search_personList;
    }

    /**
     * @param search_personList the search_personList to set
     */
    public void setSearch_personList(List<Person> search_personList) {
        this.search_personList = search_personList;
    }
    
}
