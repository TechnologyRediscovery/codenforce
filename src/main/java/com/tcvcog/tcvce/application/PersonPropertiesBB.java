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
package com.tcvcog.tcvce.application;


import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.PersonDataHeavy;
import com.tcvcog.tcvce.entities.Property;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Ellen Bascomb
 */
public class PersonPropertiesBB extends BackingBeanUtils{

    private PersonDataHeavy currPerson;
    
    private List<Property> propList;
    private Property selectedProperty;
    
    /**
     * Creates a new instance of PersonBB
     */
    public PersonPropertiesBB() {
      
    }
    
    @PostConstruct
    public void initBean(){
       PersonCoordinator pc = getPersonCoordinator();
       
       if(getSessionBean().getSessPersonQueued() != null){
            currPerson = pc.assemblePersonDataHeavy(getSessionBean().getSessPersonQueued(), 
                    getSessionBean().getSessUser().getKeyCard());
             getSessionBean().setSessPerson(currPerson);
            getSessionBean().setSessPersonQueued(null);
       } else {
            currPerson = (getSessionBean().getSessPerson());
       }
       
       propList = new ArrayList<>();
       if(getSessionBean().getSessPropertyList() != null && !getSessionBean().getSessPropertyList().isEmpty()){
           propList.addAll(getSessionBean().getSessPropertyList());
       }
        
    }
    
    public String exploreProperty(Property pr){
        getSessionBean().setSessPropertyQueued(pr);
        return "propertyInfo";
    }
    
   /**
     * Maps the session active property to the current person
     * @param ev 
     */
    public void connectCurrentPersonToProperty(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
        if(selectedProperty != null){
            try {
                pc.connectPersonToProperty(currPerson, selectedProperty);
                 getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Successfully connected person ID " + currPerson.getPersonID() + " to property ID " + getSelectedProperty().getPropertyID() , ""));
                currPerson = pc.assemblePersonDataHeavy(currPerson, getSessionBean().getSessUser().getMyCredential());
            } catch (IntegrationException ex) {
                System.out.println(ex);
                 getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            ex.getMessage(), ""));
            }
        }
        // clear list so the list itself looks properties up again
        
    }

    /**
     * @return the currPerson
     */
    public PersonDataHeavy getCurrPerson() {
        return currPerson;
    }

    /**
     * @param currPerson the currPerson to set
     */
    public void setCurrPerson(PersonDataHeavy currPerson) {
        this.currPerson = currPerson;
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
     * @return the selectedProperty
     */
    public Property getSelectedProperty() {
        return selectedProperty;
    }

    /**
     * @param selectedProperty the selectedProperty to set
     */
    public void setSelectedProperty(Property selectedProperty) {
        this.selectedProperty = selectedProperty;
    }
    
    
}
