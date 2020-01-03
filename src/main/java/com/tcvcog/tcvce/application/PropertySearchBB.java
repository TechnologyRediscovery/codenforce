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
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUseType;
import com.tcvcog.tcvce.entities.search.QueryProperty;
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
    
    private SearchParamsProperty searchParamsSelected;
    
    private List<SearchParamsProperty> searchParamsCustomized;
    
    private QueryProperty querySelected;
    private List<QueryProperty> queryList;
    
    private List<Property> propList;
    private boolean appendResultsToList;
    
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
        
        
        if(getSessionBean().getSessionPropertyList() == null){
            propList = new ArrayList<>();
        } else {
            propList = getSessionBean().getSessionPropertyList();
        }
        appendResultsToList = false;
        
        try {
            queryList = sc.buildQueryPropertyList(getSessionBean().getSessionUser().getMyCredential());
            putList = pi.getPropertyUseTypeList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    
        querySelected = getSessionBean().getQueryProperty();
        if(querySelected == null && !queryList.isEmpty()){
            querySelected = queryList.get(0);
        }
        if(querySelected != null 
                && 
            querySelected.getParmsList() != null 
                && 
            !querySelected.getParmsList().isEmpty()){
            
            searchParamsSelected = querySelected.getParmsList().get(0);
        }
        
        
    }
    
    
    public void clearPropertyList(ActionEvent ev){
        propList.clear();
    }
    
    
    public void executeQuery(ActionEvent event){
        SearchCoordinator sc = getSearchCoordinator();
        List<Property> pl = null;
        try {
            pl = sc.runQuery(querySelected).getBOBResultList();
            if(!appendResultsToList){
                propList.clear();
            } 
            propList.addAll(pl);
            
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Your search completed with " + pl.size() + " results", ""));
            
        } catch (SearchException ex) {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to complete search! ", ""));
        }
    }
    
    
    public void exploreProperty(Property prop){
        PropertyCoordinator pc = getPropertyCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        
        try {
            getSessionBean().setSessionProperty(pc.assemblePropertyDataHeavy(prop, getSessionBean().getSessionUser().getMyCredential()));
            getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_INFO,
                                        "Managing property at " + prop.getAddress() , ""));
            sc.logObjectView(getSessionBean().getSessionUser(), prop);
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        ex.getMessage(), ""));
        }
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
     * @return the propList
     */
    public List<Property> getPropList() {
        return propList;
    }

    /**
     * @param propList the propList to set
     */
    public void setPropList(List<Property> propList) {
        this.propList = propList;
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
    
    
    
    
}
