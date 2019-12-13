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

import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.CaseLifecycleException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.ChangeOrderAction;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyUnitChange;
import com.tcvcog.tcvce.entities.PropertyDataHeavy;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;


/**
 *
 * @author Nathan Dietz
 */
public class UnitChangesBB extends BackingBeanUtils implements Serializable {

    private List<Property> changedPropList;
    private Municipality selectedMuni;

    private String houseNum;
    private String streetName;
    private Property selectedProperty;
    private PropertyDataHeavy propWithLists;

    private List<PropertyUnit> existingUnitList;
    private List<PropertyUnitChange> proposedUnitList;
    private Person existingOwner;
    private Person proposedOwner;
    private List<ChangeOrderAction> actionList;

    @PostConstruct
    public void initBean() {

        Property activeProp = getSessionBean().getSessionProperty();

        actionList = new ArrayList<>();
        actionList.add(ChangeOrderAction.DoNothing);
        actionList.add(ChangeOrderAction.Accept);
        actionList.add(ChangeOrderAction.Reject);
        
        if (activeProp != null) {

            PropertyIntegrator pi = getPropertyIntegrator();

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

    public void searchForChangedProperties(ActionEvent event) {
        System.out.println("PropSearchBean.searchForchangedPropertiesSingleMuni");
        PropertyIntegrator pi = getPropertyIntegrator();

        try {
            setChangedPropList(pi.searchForChangedProperties(getHouseNum(), getStreetName(), getSessionBean().getSessionMuni().getMuniCode()));
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

    public void manageProperty(Property prop) {
        PropertyIntegrator pi = getPropertyIntegrator();
        SystemIntegrator si = getSystemIntegrator();
        
        try {
            selectedProperty = pi.getPropertyDataHeavy(prop.getPropertyID());
            existingUnitList = pi.getPropertyUnitList(selectedProperty);
            proposedUnitList = pi.getPropertyUnitChangeList(selectedProperty);
            
            si.logObjectView_OverwriteDate(getSessionBean().getSessionUser(), prop);
            
            getSessionBean().getSessionPropertyList().add(prop);

        } catch (IntegrationException | CaseLifecycleException | EventException | AuthorizationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }
    }

    public String submitUnitChanges() {

        PropertyIntegrator pi = getPropertyIntegrator();
        try {
            for (PropertyUnitChange change : proposedUnitList) {

                if (change.getAction() == ChangeOrderAction.Accept) {

//                    change.setApprovedBy(getSessionBean().getFacesUser().getUserID());
                    change.setApprovedOn(Timestamp.valueOf(LocalDateTime.now()));

                    if (change.isAdded()) {

                        pi.insertPropertyUnit(change.toPropertyUnit());

                        
                        
                    } else if (change.isRemoved()) {

                        change.setNotes(change.getNotes().concat(
                                " [Removed on " + change.getChangedOn().toGMTString()
                                + " by " + change.getChangedBy() + "]"));

                        pi.implementPropertyUnitChangeOrder(change);

                    } else {

                        pi.implementPropertyUnitChangeOrder(change);

                    }

                    pi.updatePropertyUnitChange(change);
                    
                } else if (change.getAction() == ChangeOrderAction.Reject) {
                    
//                    change.setApprovedBy(getSessionBean().getFacesUser().getUserID());
//                    
//                    change.setInactive(Timestamp.valueOf(LocalDateTime.now()));
//                    
                    pi.updatePropertyUnitChange(change);
                    
                }

            }
        } catch (IntegrationException ex) {
            System.out.println(ex);

        }
        return "unitchanges";
    }

    public String goToChangeDetail() {

        getSessionBean().setSessionProperty(selectedProperty);

        return "unitchangedetail";

    }

    public List<Property> getChangedPropList() {
        return changedPropList;
    }

    public void setChangedPropList(List<Property> changedPropList) {
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

    public PropertyDataHeavy getPropWithLists() {
        return propWithLists;
    }

    public void setPropWithLists(PropertyDataHeavy propWithLists) {
        this.propWithLists = propWithLists;
    }

    public List<PropertyUnit> getExistingUnitList() {
        return existingUnitList;
    }

    public void setExistingUnitList(List<PropertyUnit> existingUnitList) {
        this.existingUnitList = existingUnitList;
    }

    public List<PropertyUnitChange> getProposedUnitList() {
        return proposedUnitList;
    }

    public void setProposedUnitList(List<PropertyUnitChange> proposedUnitList) {
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

    public List<ChangeOrderAction> getActionList() {
        return actionList;
    }

}
