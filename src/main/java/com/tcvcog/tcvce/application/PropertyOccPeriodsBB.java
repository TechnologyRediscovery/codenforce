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
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.InspectionException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.PropertyDataHeavy;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodType;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

/**
 *
 * @author sylvia
 */
public class PropertyOccPeriodsBB 
        extends BackingBeanUtils{

    private PropertyDataHeavy currProp;
    private PropertyUnit currPropUnit;
    private List<OccPeriod> occPeriodListFiltered;
    
    private OccPeriod currOccPeriod;
    private OccPeriodType selectedOccPeriodType;
    private List<OccPeriodType> occPeriodTypeList;
    
    /**
     * Creates a new instance of PropertyCreateBB
     */
    public PropertyOccPeriodsBB() {
    }
    
    
    
     
    @PostConstruct
    public void initBean(){
        currProp = getSessionBean().getSessProperty();
        occPeriodTypeList = getSessionBean().getSessMuni().getProfile().getOccPeriodTypeList();
    }
    /**
     * Final step in creating a new occ period
     * @return 
     */
    public String addNewOccPeriod(){
        
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            if(getSelectedOccPeriodType() != null){
                System.out.println("PropertyProfileBB.initateNewOccPeriod | selectedType: " + getSelectedOccPeriodType().getTypeID());
                setCurrOccPeriod(oc.initOccPeriod(getCurrProp(), getCurrPropUnit(), getSelectedOccPeriodType(), getSessionBean().getSessUser(), getSessionBean().getSessMuni()));
                getCurrOccPeriod().setType(getSelectedOccPeriodType());
                int newID = 0;
                newID = oc.addOccPeriod(getCurrOccPeriod(), getSessionBean().getSessUser());
                getSessionBean().setSessOccPeriod(oc.assembleOccPeriodDataHeavy(oc.getOccPeriod(newID), getSessionBean().getSessUser().getMyCredential()));
            } else {
                getFacesContext().addMessage(null,
                                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                            "Please select a period type" , ""));
                return "";
            }
        } catch (EventException | AuthorizationException | ViolationException | IntegrationException | BObStatusException | InspectionException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        "Could not commit new occ period: " , ""));
            return "";
        }
        return "occPeriodWorkflow";
    }
    
    
   public String exploreOccPeriod(OccPeriod op){
       OccupancyCoordinator oc = getOccupancyCoordinator();
       if(op != null){
           try {
               getSessionBean().setSessOccPeriod(oc.assembleOccPeriodDataHeavy(op, getSessionBean().getSessUser().getMyCredential()));
           } catch (IntegrationException | BObStatusException | SearchException ex) {
               System.out.println(ex);
           }
       } else {
           return "";
       }
       
       return "occPeriodWorkflow";
   }
    
      /**
     * Called when the user initiates new occ period creation
     * @param pu 
     */
    public void initiateNewOccPeriodCreation(PropertyUnit pu){
        setSelectedOccPeriodType(null);
        setCurrPropUnit(pu);
    }
    
    /**
     * @param selectedOccPeriodType the selectedOccPeriodType to set
     */
    public void setSelectedOccPeriodType(OccPeriodType selectedOccPeriodType) {
        this.selectedOccPeriodType = selectedOccPeriodType;
    }
    
        /**
     * @return the selectedOccPeriodType
     */
    public OccPeriodType getSelectedOccPeriodType() {
        return selectedOccPeriodType;
    }
    
    
    /**
     * @return the currOccPeriod
     */
    public OccPeriod getCurrOccPeriod() {
        return currOccPeriod;
    }

    /**
     * @param currOccPeriod the currOccPeriod to set
     */
    public void setCurrOccPeriod(OccPeriod currOccPeriod) {
        this.currOccPeriod = currOccPeriod;
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
     * @return the occPeriodListFiltered
     */
    public List<OccPeriod> getOccPeriodListFiltered() {
        return occPeriodListFiltered;
    }

    /**
     * @param occPeriodListFiltered the occPeriodListFiltered to set
     */
    public void setOccPeriodListFiltered(List<OccPeriod> occPeriodListFiltered) {
        this.occPeriodListFiltered = occPeriodListFiltered;
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

}
