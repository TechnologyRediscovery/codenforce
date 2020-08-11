/*
 * Copyright (C) 2020 Technology Rediscovery LLC
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

import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.coordinators.WorkflowCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.EventRuleAbstract;
import com.tcvcog.tcvce.entities.PageModeEnum;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUseType;
import com.tcvcog.tcvce.entities.search.QueryProperty;
import com.tcvcog.tcvce.entities.search.QueryPropertyEnum;
import com.tcvcog.tcvce.entities.search.SearchParamsProperty;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author sylvia
 */
public class PropertySearchBB extends BackingBeanUtils{
    
    private PageModeEnum currentMode;
    private List<PageModeEnum> pageModes;
    
    private Property currentProperty;
    private boolean currentPropertySelected;
    
    private List<Property> propListMaster;
    private List<Property> propListDisplayed;
    private boolean appendResultsToList;
    
    private SearchParamsProperty searchParamsSelected;
    private List<SearchParamsProperty> searchParamsCustomized;
    
    private QueryProperty querySelected;
    private List<QueryProperty> queryList;
    
    private List<PropertyUseType> putList;
    
    /**
     * Creates a new instance of SearchBB
     */
    public PropertySearchBB() {
    }
    
    @PostConstruct
    public void initBean(){
        SearchCoordinator sc = getSearchCoordinator();
        PropertyCoordinator pc = getPropertyCoordinator();
        PropertyIntegrator pi = getPropertyIntegrator();
        
        propListMaster = new ArrayList<>();
        propListDisplayed = new ArrayList<>();
        if(getSessionBean().getSessPropertyList() != null && !getSessionBean().getSessPropertyList().isEmpty()){
            propListMaster.addAll(getSessionBean().getSessPropertyList());
            propListDisplayed.addAll(propListMaster);
        }
        appendResultsToList = false;
        
        try {
            // the list of avail queries is built by the SessionInitializer
            // and put on the SessionBean for us to get here
            queryList = getSessionBean().getQueryPropertyList();
            putList = pi.getPropertyUseTypeList();
            pageModes = getSessionBean().assemblePermittedPageModes();
            currentMode = PageModeEnum.LOOKUP;
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    
//        querySelected = getSessionBean().getQueryProperty();
        
        if(querySelected == null && !queryList.isEmpty()){
            querySelected = queryList.get(0);
        }
        configureParameters();
        
    }
    
    private void configureParameters(){
        if(querySelected != null 
                && 
            querySelected.getParamsList() != null 
                && 
            !querySelected.getParamsList().isEmpty()){
            
            searchParamsSelected = querySelected.getParamsList().get(0);
        }
    }
    
    
    public void clearPropertyList(ActionEvent ev){
        propListMaster.clear();
    }
    
    
       
    /**
     * Demo of using the Search system to extract objects from the DB
     * in a controlled way
     * @return 
     */
    private List<Property> testPropertySearch(){
        SearchCoordinator sc = getSearchCoordinator();
        QueryProperty qp = sc.initQuery(QueryPropertyEnum.HOUSESTREETNUM, getSessionBean().getSessUser().getMyCredential());
        if(qp != null && !qp.getParamsList().isEmpty()){
            SearchParamsProperty spp = qp.getParamsList().get(0);
            spp.setAddress_ctl(true);
            spp.setAddress_val("101 Main");
            spp.setMuni_ctl(true);
            spp.setMuni_val(getSessionBean().getSessMuni());
            spp.setLimitResultCount_ctl(true);
            spp.setLimitResultCount_val(20);
        }
        
        try {
            // send assembled query with its configured parameter object
            // to runQuery, which will delegate the work to the searchForXXX methods
            // on the intergrators
            sc.runQuery(qp);
        } catch (SearchException ex) {
            System.out.println(ex);
        }
        List<Property> propList = null;

        if(qp != null && !qp.getBOBResultList().isEmpty()){
            // extract the actual business objects from the Query object as a list
            propList = qp.getBOBResultList();
        }
        
        return propList;
        
    }
    
    /**
     * Action listener for the user's request to run the query
     * @param event 
     */
    public void executeQuery(ActionEvent event){
        System.out.println("PropertySearchBB.executeQuery | querySelected: " + querySelected.getQueryTitle());
        
        SearchCoordinator sc = getSearchCoordinator();
        List<Property> pl;
        
        try {
            
            pl = sc.runQuery(querySelected).getBOBResultList();
            if(!appendResultsToList){
                propListMaster.clear();
            } 
            propListMaster.addAll(pl);
            if(!propListMaster.isEmpty()){
                getSessionBean().setSessPropertyList(propListMaster);
            }
            
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Your search completed with " + pl.size() + " results", ""));
            
        } catch (SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to complete search! ", ""));
        }
    }
    
    /**
     * Listener method for changes in the selected query;
     * Updates search params and UI updates based on this changed value
     */
    public void changeQuerySelected(){
        System.out.println("PropertySearchBB.changeQuerySelected | querySelected: " + querySelected.getQueryTitle());
        configureParameters();
        
    }
    
    
    
    /**
     * Event listener for resetting a query after it's run
     * @param event 
     */
    public void resetQuery(ActionEvent event){
        SearchCoordinator sc = getSearchCoordinator();
        try {
            //        querySelected = sc.initQuery(querySelected.getQueryName(), getSessionBean().getSessUser().getMyCredential());
            queryList = sc.buildQueryPropertyList(getSessionBean().getSessUser().getMyCredential());
            if(queryList != null && !queryList.isEmpty()){
                querySelected = queryList.get(0);
            }
            if(appendResultsToList == false){
                if(propListMaster != null && !propListMaster.isEmpty()){
                    propListMaster.clear();
                }
            }
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Query reset ", ""));
            
        } catch (IntegrationException ex) {
             System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to reset search due to error in search coordinator! ", ""));
        }
        configureParameters();
    }
    
    
    
    /**
     * Loads a data-heavy subclass of the selected property
     * @param prop 
     * @return  
     */
    public String exploreProperty(Property prop){
        PropertyCoordinator pc = getPropertyCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        
        try {
            getSessionBean().setSessProperty(pc.assemblePropertyDataHeavy(prop, getSessionBean().getSessUser()));
            getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_INFO,
                                        "Managing property at " + prop.getAddress() , ""));
            sc.logObjectView(getSessionBean().getSessUser(), prop);
            return "propertyInfo";
        } catch (IntegrationException | BObStatusException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        ex.getMessage(), ""));
        } 
        return "";
        
    }
    

    /**
     * @return the searchParamsSelected
     */
    public SearchParamsProperty getSearchParamsSelected() {
        return searchParamsSelected;
    }

    /**
     * @return the querySelected
     */
    public QueryProperty getQuerySelected() {
        return querySelected;
    }

    /**
     * @return the queryList
     */
    public List<QueryProperty> getQueryList() {
        return queryList;
    }

    /**
     * @param searchParamsSelected the searchParamsSelected to set
     */
    public void setSearchParamsSelected(SearchParamsProperty searchParamsSelected) {
        this.searchParamsSelected = searchParamsSelected;
    }

    /**
     * @param querySelected the querySelected to set
     */
    public void setQuerySelected(QueryProperty querySelected) {
        this.querySelected = querySelected;
    }

    /**
     * @param queryList the queryList to set
     */
    public void setQueryList(List<QueryProperty> queryList) {
        this.queryList = queryList;
    }

    /**
     * @return the searchParamsCustomized
     */
    public List<SearchParamsProperty> getSearchParamsCustomized() {
        return searchParamsCustomized;
    }

    /**
     * @param searchParamsCustomized the searchParamsCustomized to set
     */
    public void setSearchParamsCustomized(List<SearchParamsProperty> searchParamsCustomized) {
        this.searchParamsCustomized = searchParamsCustomized;
    }

    /**
     * @return the propListMaster
     */
    public List<Property> getPropListMaster() {
        return propListMaster;
    }

    /**
     * @param propListMaster the propListMaster to set
     */
    public void setPropListMaster(List<Property> propListMaster) {
        this.propListMaster = propListMaster;
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
     * @return the putList
     */
    public List<PropertyUseType> getPutList() {
        return putList;
    }

    /**
     * @param putList the putList to set
     */
    public void setPutList(List<PropertyUseType> putList) {
        this.putList = putList;
    }

    /**
     * @return the currentMode
     */
    public PageModeEnum getCurrentMode() {
        return currentMode;
    }

    /**
     * @return the pageModes
     */
    public List<PageModeEnum> getPageModes() {
        return pageModes;
    }

    /**
     * @return the currentProperty
     */
    public Property getCurrentProperty() {
        return currentProperty;
    }

    /**
     * @return the currentPropertySelected
     */
    public boolean isCurrentPropertySelected() {
        return currentPropertySelected;
    }

    /**
     * @return the propListDisplayed
     */
    public List<Property> getPropListDisplayed() {
        return propListDisplayed;
    }

    /**
     * @param currentMode the currentMode to set
     */
    public void setCurrentMode(PageModeEnum currentMode) {
        this.currentMode = currentMode;
    }

    /**
     * @param pageModes the pageModes to set
     */
    public void setPageModes(List<PageModeEnum> pageModes) {
        this.pageModes = pageModes;
    }

    /**
     * @param currentProperty the currentProperty to set
     */
    public void setCurrentProperty(Property currentProperty) {
        this.currentProperty = currentProperty;
    }

    /**
     * @param currentPropertySelected the currentPropertySelected to set
     */
    public void setCurrentPropertySelected(boolean currentPropertySelected) {
        this.currentPropertySelected = currentPropertySelected;
    }

    /**
     * @param propListDisplayed the propListDisplayed to set
     */
    public void setPropListDisplayed(List<Property> propListDisplayed) {
        this.propListDisplayed = propListDisplayed;
    }
    
    
    
    
}
