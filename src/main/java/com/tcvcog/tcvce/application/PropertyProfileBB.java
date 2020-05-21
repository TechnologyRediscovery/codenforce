package com.tcvcog.tcvce.application;


import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.BlobType;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PropertyDataHeavy;
import com.tcvcog.tcvce.entities.PropertyUseType;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import org.primefaces.event.FileUploadEvent;

/*
 * Copyright (C) 2018 Technology Rediscovery LLC
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
 * @author ellen bascomb of apt 31y
 */
public class PropertyProfileBB extends BackingBeanUtils implements Serializable{
    
    private PropertyDataHeavy currProp;
    
    private Municipality selectedMuni;
    private List<PropertyUseType> putList;
    
    
    /**
     * Creates a new instance of PropertyProfileBB
     */
    public PropertyProfileBB() {
    }
    
    @PostConstruct
    public void initBean(){
        PropertyIntegrator pi = getPropertyIntegrator();
        
        currProp = getSessionBean().getSessProperty();
        
  
        selectedMuni = getSessionBean().getSessMuni();

        try {
            putList = pi.getPropertyUseTypeList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }
    
    /**
     * Logic intermediary when property edits begin and dialog is displayed
     * @param ev 
     */
    public void initiatePropertyEdit(ActionEvent ev){
        // do nothing
        
    }

     public void commitPropertyUpdates(){
        PropertyCoordinator pc = getPropertyCoordinator();
        try {
//            currProp.setAbandonedDateStart(pc.configureDateTime(currProp.getAbandonedDateStart().to));
            pc.editProperty(currProp, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Successfully updated property with ID " + getCurrProp().getPropertyID() 
                                + ", which is now your 'active property'", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Could not update property, sorries!", ""));
        }
        refreshCurrPropWithLists();
     
    }
    
  
    
    public void refreshCurrPropWithLists(){
        PropertyCoordinator pc = getPropertyCoordinator();
        try {
            setCurrProp(pc.getPropertyDataHeavy(currProp.getPropertyID(), getSessionBean().getSessUser().getMyCredential()));
        } catch (IntegrationException | BObStatusException | SearchException | AuthorizationException | EventException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not update current property with lists | Exception details: " + ex.getMessage(), ""));
        }
        
    }
      
    
    public String addProperty(){
        //getSessionBean().setActiveProp(new Property());  // we do this after the prop has been inserted
        return "propertyAdd";
    }
    
 
    
    
    public String viewPersonProfile(Person p){
        getSessionBean().getSessPersonList().add(0,p);
        return "persons";
    }
    
    
    /**
     * @return the currentProperty
     */
    public PropertyDataHeavy getCurrProp() {
        return currProp;
    }
    
    
    
    /**
     * @param currentProperty the currentProperty to set
     */
    public void setCurrProp(PropertyDataHeavy currentProperty) {
        this.currProp = currentProperty;
    }
    
 
    
   
   

    /**
     * @return the selectedMuni
     */
    public Municipality getSelectedMuni() {
        return selectedMuni;
    }

    /**
     * @param selectedMuni the selectedMuni to set
     */
    public void setSelectedMuni(Municipality selectedMuni) {
        this.selectedMuni = selectedMuni;
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
