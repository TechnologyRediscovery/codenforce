/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyUnitChange;
import com.tcvcog.tcvce.entities.PropertyWithLists;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;



/*
 * Copyright (C) 2018 Turtle Creek Valley
Council of Governments, PA
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

/**
 *
 * @author Nathan Dietz
 */
public class UnitChangesBB extends BackingBeanUtils implements Serializable{
    
    private ArrayList<Property> changedPropList;
    private Municipality selectedMuni;
    
    private String houseNum;
    private String streetName;
    private Property selectedProperty;
    private PropertyWithLists propWithLists;
    
    private ArrayList<PropertyUnit> existingUnitList;
    private ArrayList<PropertyUnitChange> proposedUnitList;
    private Person existingOwner;
    private Person proposedOwner;
    
    
    
    @PostConstruct
    public void initBean() {
        
        Property activeProp = getSessionBean().getActiveProp();
        
        if(activeProp != null) {
            
            PropertyIntegrator pi = new PropertyIntegrator();
            
            try {
                
                existingUnitList = pi.getPropertyUnitList(activeProp);
                
                proposedUnitList = pi.getPropertyUnitChangeList(activeProp);
                
            } catch (IntegrationException ex) {
                System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to get unit lists! ", ""));
                
            }
            
            
            
        }
        
    }

    public void searchForChangedProperties(ActionEvent event){
        System.out.println("PropSearchBean.searchForPropertiesSingleMuni");
        PropertyIntegrator pi = new PropertyIntegrator();
        
        try {
            setChangedPropList(pi.searchForChangedProperties(getHouseNum(), getStreetName(), getSessionBean().getActiveMuni().getMuniCode()));
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Your search completed with " + getChangedPropList().size() + " results", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to complete search! ", ""));
        }
    }
    
    public void manageProperty(Property prop){
        PropertyIntegrator pi = getPropertyIntegrator();
        UserIntegrator ui = getUserIntegrator();
        try {
            selectedProperty = pi.getPropertyWithLists(prop.getPropertyID());
            existingUnitList = pi.getPropertyUnitList(selectedProperty);
            proposedUnitList = pi.getPropertyUnitChangeList(selectedProperty);
            ui.logObjectView(getSessionBean().getFacesUser(), prop);
            getSessionBean().getPropertyQueue().add(prop);
            
        } catch (IntegrationException | CaseLifecyleException ex) {
            System.out.println(ex);
        }
    }
    
    public String goToChangeDetail(){
        
        getSessionBean().setActiveProp(selectedProperty);
        
        return "unitchangedetail";
        
    }
    
    public ArrayList<Property> getChangedPropList() {
        return changedPropList;
    }

    public void setChangedPropList(ArrayList<Property> changedPropList) {
        this.changedPropList = changedPropList;
    }

    public Municipality getSelectedMuni() {
        return selectedMuni;
    }

    public void setSelectedMuni(Municipality selectedMuni) {
        this.selectedMuni = selectedMuni;
    }

    
    
    public String getHouseNum() {
        return houseNum;
    }

    public void setHouseNum(String houseNum) {
        this.houseNum = houseNum;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public Property getSelectedProperty() {
        return selectedProperty;
    }

    public void setSelectedProperty(Property selectedProperty) {
        this.selectedProperty = selectedProperty;
    }

    public PropertyWithLists getPropWithLists() {
        return propWithLists;
    }

    public void setPropWithLists(PropertyWithLists propWithLists) {
        this.propWithLists = propWithLists;
    }

    public ArrayList<PropertyUnit> getExistingUnitList() {
        return existingUnitList;
    }

    public void setExistingUnitList(ArrayList<PropertyUnit> existingUnitList) {
        this.existingUnitList = existingUnitList;
    }

    public ArrayList<PropertyUnitChange> getProposedUnitList() {
        return proposedUnitList;
    }

    public void setProposedUnitList(ArrayList<PropertyUnitChange> proposedUnitList) {
        this.proposedUnitList = proposedUnitList;
    }

    public Person getExistingOwner() {
        return existingOwner;
    }

    public void setExistingOwner(Person existingOwner) {
        this.existingOwner = existingOwner;
    }

    public Person getProposedOwner() {
        return proposedOwner;
    }

    public void setProposedOwner(Person proposedOwner) {
        this.proposedOwner = proposedOwner;
    }
    
    
    
}
