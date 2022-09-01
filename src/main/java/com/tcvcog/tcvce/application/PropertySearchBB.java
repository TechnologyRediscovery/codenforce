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

import com.tcvcog.tcvce.coordinators.*;
import com.tcvcog.tcvce.domain.*;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.entities.search.QueryProperty;
import com.tcvcog.tcvce.entities.search.SearchParamsProperty;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.event.ActionEvent;

/**
 * Primary backing bean for the Property Search process
 * and as of FEB 2022, no longer also the backer for
 * property profile stuff; see PropertyProfileBB, as per
 * legacy structure
 * 
 * @author Ellen Bascomb of Apartment 31Y
 */
public class PropertySearchBB extends BackingBeanUtils{
    
    
    private PropertyDataHeavy currentProperty;
    private Property freshProperty;
    private boolean currentPropertySelected;
    
    private List<Property> propListMaster;
    private List<Property> propListDisplayed;
    private boolean appendResultsToList;
    
    private SearchParamsProperty searchParamsSelected;
    private List<SearchParamsProperty> searchParamsCustomized;
    
    private QueryProperty querySelected;
    private List<QueryProperty> queryList;
    
    private List<PropertyUseType> putList;
    
    private List<IntensityClass> conditionIntensityList;
    private List<IntensityClass> landBankProspectIntensityList;
    private List<BOBSource> sourceList;
    
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
            // build a fresh copy of our session's property
            currentProperty = pc.assemblePropertyDataHeavy(getSessionBean().getSessProperty(),getSessionBean().getSessUser());
            // the list of avail queries is built by the SessionInitializer
            // and put on the SessionBean for us to get here
            queryList = sc.buildQueryPropertyList(getSessionBean().getSessUser().getMyCredential());
            putList = pi.getPropertyUseTypeList();
        } catch (IntegrationException | BObStatusException | SearchException | BlobException ex) {
            System.out.println(ex);
        }
    
//        querySelected = getSessionBean().getQueryProperty();
        
        if(querySelected == null && !queryList.isEmpty()){
            querySelected = queryList.get(0);
        }
      
        // setup search
        configureParameters();
        
    }
   
    
    /**
     * Sets up search parameters for properties
     */
    private void configureParameters(){
        SystemCoordinator sc = getSystemCoordinator();
        if(querySelected != null 
                && 
            querySelected.getParamsList() != null 
                && 
            !querySelected.getParamsList().isEmpty()){
            
            searchParamsSelected = querySelected.getParamsList().get(0);
        }
        
        setSourceList(sc.getBobSourceListComplete());
           
        try {
            setConditionIntensityList(sc.getIntensitySchemaWithClasses(
                    getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("intensityschema_propertycondition"))
                    .getClassList());
            setLandBankProspectIntensityList(sc.getIntensitySchemaWithClasses(
                    getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("intensityschema_landbankprospect"))
                    .getClassList());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }
    
    
    public void clearPropertyList(ActionEvent ev){
        propListMaster.clear();
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
            
        } catch (SearchException |  BObStatusException ex) {
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
     * @return  page ID
     */
    public String exploreProperty(Property prop){
        PropertyCoordinator pc = getPropertyCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        
        try {
            String outcome = getSessionBean().navigateToPageCorrespondingToObject(prop);
            getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_INFO,
                                        "Managing property parcel ID: " + prop.getCountyParcelID() , ""));
            // disable until after launch
//            sc.logObjectView(getSessionBean().getSessUser(), prop);
            return outcome;
        } catch (BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        ex.getMessage(), ""));
        } 
        return "";
        
    }
    
     /**
     * Loads a skeleton property into which we inject values from the form
     */
    public void onPropertyAddInit() {
        PropertyCoordinator pc = getPropertyCoordinator();
        try {
            currentProperty = (pc.assemblePropertyDataHeavy(pc.generatePropertySkeleton(getSessionBean().getSessMuni()),getSessionBean().getSessUser()));
        } catch (IntegrationException | BObStatusException | SearchException | BlobException ex) {
            System.out.println(ex);
        }
    }
    
    /**
     * Listener for user toggling non-addressable on new property
     
     */
    public void onPropertyCreateNonAddressableSliderActuation(){
        System.out.println("PropertySearchBB.onPropertyCreateNonAddressableSliderActuation | nonaddressable? " + currentProperty.getParcelInfo().isNonAddressable());
    }
    
      /**
     * Liases with coordinator to insert a new property object
     * @return jumps to property profile page
     */
    public String onPropertyAddCommit() {
        PropertyCoordinator pc = getPropertyCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        int newID;
        MessageBuilderParams mbp = new MessageBuilderParams(null, 
                "Property creation notes", 
                "At the time of record creation", 
                currentProperty.getNotes(), 
                getSessionBean().getSessUser(), 
                null);
        currentProperty.setNotes(sc.appendNoteBlock(mbp));
        
        try {
            newID = pc.addParcel(currentProperty, getSessionBean().getSessUser());
            currentProperty = pc.getPropertyDataHeavy(newID, getSessionBean().getSessUser());
            getSessionBean().setSessProperty(currentProperty);
            sc.logObjectView(getSessionBean().getSessUser(), currentProperty);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully added property with ID: " + currentProperty.getParcelKey()
                            + ", which is now your 'active property'", ""));
            return "propertyInfo";
            
        } catch (AuthorizationException | BObStatusException | EventException | IntegrationException | SearchException | BlobException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not add new property, sorries!" + ex.getClass().toString(), ""));
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
     * @return the currentProperty
     */
    public Property getCurrentProperty() {
        return currentProperty;
    }

    /**
     * @return the currentPropertySelected
     */
    public boolean isCurrentPropertySelected() {
        currentPropertySelected = currentProperty != null;
        return currentPropertySelected;
    }

    /**
     * @return the propListDisplayed
     */
    public List<Property> getPropListDisplayed() {
        return propListDisplayed;
    }

  
    /**
     * @param currentProperty the currentProperty to set
     */
    public void setCurrentProperty(PropertyDataHeavy currentProperty) {
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

    /**
     * @return the freshProperty
     */
    public Property getFreshProperty() {
        return freshProperty;
    }

    /**
     * @param freshProperty the freshProperty to set
     */
    public void setFreshProperty(Property freshProperty) {
        this.freshProperty = freshProperty;
    }

    /**
     * @return the landBankProspectIntensityList
     */
    public List<IntensityClass> getLandBankProspectIntensityList() {
        return landBankProspectIntensityList;
    }

    /**
     * @param landBankProspectIntensityList the landBankProspectIntensityList to set
     */
    public void setLandBankProspectIntensityList(List<IntensityClass> landBankProspectIntensityList) {
        this.landBankProspectIntensityList = landBankProspectIntensityList;
    }

    /**
     * @return the conditionIntensityList
     */
    public List<IntensityClass> getConditionIntensityList() {
        return conditionIntensityList;
    }

    /**
     * @return the sourceList
     */
    public List<BOBSource> getSourceList() {
        return sourceList;
    }

    /**
     * @param conditionIntensityList the conditionIntensityList to set
     */
    public void setConditionIntensityList(List<IntensityClass> conditionIntensityList) {
        this.conditionIntensityList = conditionIntensityList;
    }

    /**
     * @param sourceList the sourceList to set
     */
    public void setSourceList(List<BOBSource> sourceList) {
        this.sourceList = sourceList;
    }

   
    
    
    
}
