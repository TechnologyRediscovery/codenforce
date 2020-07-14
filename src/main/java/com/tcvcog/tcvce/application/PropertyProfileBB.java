package com.tcvcog.tcvce.application;


import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.BOBSource;
import com.tcvcog.tcvce.entities.IntensityClass;
import com.tcvcog.tcvce.entities.IntensitySchema;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.PageModeEnum;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PropertyDataHeavy;
import com.tcvcog.tcvce.entities.PropertyUseType;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

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
 * You should have received a copy of the GNU General Public License <h:outputText id="header-ot" styleClass="dataText" value="#{propertyProfileBB.currentProperty.address}" />
                                    <h:outputText value=" | " />
                                    <h:outputText value="#{propertyProfileBB.currentProperty.muni.muniName}" />
                                    <h:outputText value=" | " />
                                    <h:outputText id="header-lob-ot" value="Lot-block: #{propertyProfileBB.currentProperty.parID}"/>  
                                    <h:outputText value=" | " />
                                    <h:outputText id="header-propid-ot" value="ID: #{propertyProfileBB.currentProperty.propertyID}"/>  
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class PropertyProfileBB extends BackingBeanUtils implements Serializable{
    
     
    private PageModeEnum currentMode;
    private List<PageModeEnum> pageModes;
    
    private PropertyDataHeavy currentProperty;
    
    private List<Municipality> muniList;
    private Municipality muniSelected;
    
    
    private List<PropertyUseType> putList;
    private PropertyUseType selectedPropertyUseType;
    
    private List<IntensityClass> conditionIntensityList;
    private IntensityClass conditionIntensitySelected;
    
    private List<BOBSource> sourceList;
    private BOBSource sourceSelected;
    
    
    
    
    
    /**
     * Creates a new instance of PropertyProfileBB
     */
    public PropertyProfileBB() {
    }
    
    @PostConstruct
    public void initBean(){
        PropertyIntegrator pi = getPropertyIntegrator();
        
        currentProperty = getSessionBean().getSessProperty();
        
  
        muniSelected = getSessionBean().getSessMuni();

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
//            currentProperty.setAbandonedDateStart(pc.configureDateTime(currentProperty.getAbandonedDateStart().to));
            pc.editProperty(currentProperty, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Successfully updated property with ID " + getCurrentProperty().getPropertyID() 
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
            setCurrentProperty(pc.getPropertyDataHeavy(currentProperty.getPropertyID(), getSessionBean().getSessUser().getMyCredential()));
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
    public PropertyDataHeavy getCurrentProperty() {
        return currentProperty;
    }
    
    
    
    /**
     * @param currentProperty the currentProperty to set
     */
    public void setCurrentProperty(PropertyDataHeavy currentProperty) {
        this.currentProperty = currentProperty;
    }
    
 
    
   
   

    /**
     * @return the muniSelected
     */
    public Municipality getMuniSelected() {
        return muniSelected;
    }

    /**
     * @param muniSelected the muniSelected to set
     */
    public void setMuniSelected(Municipality muniSelected) {
        this.muniSelected = muniSelected;
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
