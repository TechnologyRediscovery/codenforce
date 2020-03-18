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

import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.PropertyDataHeavy;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyUnitDataHeavy;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodType;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import java.time.LocalDateTime;
import java.util.Iterator;
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
public class PropertyUnitsBB 
        extends BackingBeanUtils{

    private PropertyDataHeavy currProp;
    private PropertyUnit currPropUnit;
    
    private PropertyUnitDataHeavy currPropUnitWithLists;
    
    
    /**
     * Creates a new instance of PropertyCreateBB
     */
    public PropertyUnitsBB() {
    }
    
    
    
     
    @PostConstruct
    public void initBean(){
        currProp = getSessionBean().getSessProperty();
      
        
    }
    
    
   
    public String goToChanges() {
        
        return "unitchanges";
    }
    
    
    public String manageOccPeriod(OccPeriod op){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        
        try {
            getSessionBean().setSessOccPeriod(oc.assembleOccPeriodDataHeavy(op, getSessionBean().getSessUser().getMyCredential()));
        } catch (IntegrationException | BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not load occupancy period with data" + ex.getMessage(), ""));
            
        }
        return "occPeriodStatus";
        
    }
      
   

    
    /**
     * Logic container for steps needed to be taken before 
     * a unit list is edited
     * @param ev 
     */
    public void beginPropertyUnitUpdates(ActionEvent ev){
        // do nothing as of beta 0.9
    }
    
     /**
     * Adds a blank unit to propUnitsToAdd list. This newly-created unit can
     * then be selected and edited by the user.
     */
    public void addUnitToNewPropUnits() {
        PropertyUnit unitToAdd;
        PropertyCoordinator pc = getPropertyCoordinator();
        unitToAdd = pc.initPropertyUnit(currProp);
        getCurrProp().getUnitList().add(unitToAdd);
        
//        clearAddUnitFormValues();
    }
    
    public void removePropertyUnitFromEditTable(PropertyUnit pu){
        getCurrProp().getUnitList().remove(pu);
        getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Zap!", ""));
        
    }
    
    public void deactivatePropertyUnit(PropertyUnit pu){
        PropertyIntegrator pi = getPropertyIntegrator();
        pu.setActive(false);
        try {
            pi.updatePropertyUnit(pu);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Unit deactivated with ID " + pu.getUnitID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Could not deactivate unit with ID " + pu.getUnitID(), ""));
        }
        
    }
    
       /**
     * Finalizes the unit list the user has created so that it can be compared
     * to the existing one in the database.
     * 
     * @param ev
     */
    public void finalizeUnitList(ActionEvent ev) {
        PropertyIntegrator pi = getPropertyIntegrator();
        PropertyCoordinator pc = getPropertyCoordinator();
        
        boolean missingUnitNum = false;
        boolean duplicateUnitNum = false;
        int duplicateNums = 0;
        //The above boolean is a flag to see if there is more than 1 of  Unit Number. The int to the left stores how many of a given number the loop below finds.

        for (PropertyUnit firstUnit : getCurrProp().getUnitList()) {
            duplicateNums = 0;
            
            // remove any use of the word "unit" in a unit identifier
            firstUnit.setUnitNumber(firstUnit.getUnitNumber().replaceAll("(?i)unit", ""));

            if (firstUnit.getUnitNumber().compareTo("") == 0) {
                missingUnitNum = true;
                break; //break for performance reasons. Can be removed if breaks are not welcome here.
            }

            for (PropertyUnit secondUnit : getCurrProp().getUnitList()) {
                if (firstUnit.getUnitNumber().compareTo(secondUnit.getUnitNumber()) == 0) {
                    duplicateNums++;
                }
            }

            if (duplicateNums > 1) {
                duplicateUnitNum = true;
                break; //break for performance reasons. Can be removed if breaks are not welcome here.
            }
        }

        if (getCurrProp().getUnitList().isEmpty()) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please add at least one unit.", ""));
            
        } else if (missingUnitNum) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "All units must have a Unit Number", ""));

        } else if (duplicateUnitNum) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Some Units have the same Number", ""));

        } else {
            Iterator<PropertyUnit> iter = getCurrProp().getUnitList().iterator();
            while(iter.hasNext()){
                PropertyUnit pu = iter.next();
                
                // decide if we're updating a unit or inserting it based on initial value
                // newly created units don't have an ID, just a default unit number
                pu.setPropertyID(getCurrProp().getPropertyID());
                if(pu.getUnitID() == 0){
                    try {
                        pi.insertPropertyUnit(pu);
                        
                        getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_INFO,
                                        "Success! Inserted property unit: " + pu.getUnitNumber(), ""));
                    } catch (IntegrationException ex) {
                        getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        "Could not insert unit with number: " + pu.getUnitNumber(), ""));
                    }
                } else {
                    try {
                        pi.updatePropertyUnit(pu);
                        getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_INFO,
                                        "Success! Updated property unit: " + pu.getUnitNumber(), ""));
                    } catch (IntegrationException ex) {
                        getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        "Could not update unit with number: " + pu.getUnitNumber(), ""));
                    }
                }
            }
        }
        
        // mark parent property as updated now
       
        try{
            pc.editProperty(currProp, getSessionBean().getSessUser());
//            currProp = pc.assemblePropertyDataHeavy(currProp, getSessionBean().getSessUser().getMyCredential());
        } catch (IntegrationException  ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not update associated property: ", ""));
        }
        
        refreshCurrPropWithLists();
        
    } // close method
    
    
    
    private void refreshCurrPropWithLists(){
        PropertyCoordinator pc = getPropertyCoordinator();
        try {
            currProp = pc.getPropertyDataHeavy(currProp.getPropertyID(), getSessionBean().getSessUser().getMyCredential());
            getSessionBean().setSessProperty(currProp);
        } catch (IntegrationException | BObStatusException | SearchException | AuthorizationException | EventException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not update current property with lists | Exception details: " + ex.getMessage(), ""));
        }
        
    }

    /**
     * @return the currProp
     */
    public PropertyDataHeavy getCurrProp() {
        return currProp;
    }

    /**
     * @param currProp the currProp to set
     */
    public void setCurrProp(PropertyDataHeavy currProp) {
        this.currProp = currProp;
    }
    
       /**
     * @return the currPropUnit
     */
    public PropertyUnit getCurrPropUnit() {
        return currPropUnit;
    }

    /**
     * @param currPropUnit the currPropUnit to set
     */
    public void setCurrPropUnit(PropertyUnit currPropUnit) {
        this.currPropUnit = currPropUnit;
    }

    /**
     * @return the currPropUnitWithLists
     */
    public PropertyUnitDataHeavy getCurrPropUnitWithLists() {
        return currPropUnitWithLists;
    }

    /**
     * @param currPropUnitWithLists the currPropUnitWithLists to set
     */
    public void setCurrPropUnitWithLists(PropertyUnitDataHeavy currPropUnitWithLists) {
        this.currPropUnitWithLists = currPropUnitWithLists;
    }

    
    
    
}
