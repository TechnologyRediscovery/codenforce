/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyWithLists;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.occupancy.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.occupancy.entities.OccPermitApplication;
import com.tcvcog.tcvce.occupancy.entities.OccPermitApplicationReason;
import com.tcvcog.tcvce.occupancy.integration.OccupancyPermitIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Dominic Pimpinella
 */

public class OccPermitApplicationBB extends BackingBeanUtils implements Serializable{

    private Municipality selectedMuni;
    private Date form_dateOfRecord;
    
    private boolean multiUnit;
    private String houseNum;
    private String streetName;
    private Property selectedProperty;
    private List<Property> propList;
    private PropertyWithLists propWithLists;
    
    private PropertyUnit selectedUnit;
    private PropertyUnit unitToAdd;
    private List<PropertyUnit> propUnitsToAdd;
    private String unitNum;
    private boolean rental;
    private String unitNotes;
    
    private OccPermitApplicationReason selectedApplicationReason;
    private List<OccPermitApplicationReason> reasonList;
    
    private Person currentOwner;
    private Person newOwner;
    private Person contactPerson;
    private ArrayList<Person> newOccupants;
    
    /**
     * Creates a new instance of OccPermitApplicationBB
     */
    public OccPermitApplicationBB() {
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
     * @return the form_dateOfRecord
     */
    public Date getForm_dateOfRecord() {
        return form_dateOfRecord;
    }

    /**
     * @param form_dateOfRecord the form_dateOfRecord to set
     */
    public void setForm_dateOfRecord(Date form_dateOfRecord) {
        this.form_dateOfRecord = form_dateOfRecord;
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

    public List<Property> getPropList() {
        return propList;
    }

    public void setPropList(ArrayList<Property> propList) {
        this.propList = propList;
    } 

    /**
     * @return the reasonList
     */
    public List<OccPermitApplicationReason> getReasonList() {
        OccupancyPermitIntegrator opi = getOccupancyPermitIntegrator();
        try {
            reasonList = opi.getOccPermitApplicationReasons();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        return reasonList;
    }

    /**
     * @param reasonList the reasonList to set
     */
    public void setReasonList(List<OccPermitApplicationReason> reasonList) {
        this.reasonList = reasonList;
    }

    /**
     * @return the currentOwner
     */
    public Person getCurrentOwner() {
        return currentOwner;
    }

    /**
     * @param currentOwner the currentOwner to set
     */
    public void setCurrentOwner(Person currentOwner) {
        this.currentOwner = currentOwner;
    }

    /**
     * @return the newOwner
     */
    public Person getNewOwner() {
        return newOwner;
    }

    /**
     * @param newOwner the newOwner to set
     */
    public void setNewOwner(Person newOwner) {
        this.newOwner = newOwner;
    }

    /**
     * @return the contactPerson
     */
    public Person getContactPerson() {
        return contactPerson;
    }

    /**
     * @param contactPerson the contactPerson to set
     */
    public void setContactPerson(Person contactPerson) {
        this.contactPerson = contactPerson;
    }

    /**
     * @return the newOccupants
     */
    public ArrayList<Person> getNewOccupants() {
        return newOccupants;
    }

    /**
     * @param newOccupants the newOccupants to set
     */
    public void setNewOccupants(ArrayList<Person> newOccupants) {
        this.newOccupants = newOccupants;
    }
    
    public void onPropertySelection(){
        PropertyCoordinator pc = getPropertyCoordinator();
        propWithLists = pc.checkPropertyForUnits(getSessionBean().getOccPermitApplication().getApplicationProperty());
        getSessionBean().setActivePropWithLists(propWithLists);
    }
        
    public String storeSelectedMuni(){        
        OccupancyCoordinator oc = getOccupancyCoordinator();
        OccPermitApplication occpermitapp = oc.getNewOccPermitApplication();                        
        getSessionBean().setOccPermitApplication(occpermitapp);
        getSessionBean().setActiveMuni(selectedMuni);
        return "chooseProperty";
    }    

    public void searchForPropertiesSingleMuni(ActionEvent ev){
        PropertyIntegrator pi = getPropertyIntegrator();
        Municipality activeMuni = getSessionBean().getActiveMuni();

        try {
            propList = pi.searchForProperties(houseNum, streetName, activeMuni.getMuniCode());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Your search completed with " + getPropList().size() + " results", ""));
            
            
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to complete a property search! Sorry!", ""));
            
        }
    }

    public String storePropertyInfo(){
        if(getSessionBean().getOccPermitApplication().getApplicationProperty() == null){        
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                       "Please select a property from the list of search results to continue.", ""));
            return "addReason";
        }
        PropertyCoordinator pc = getPropertyCoordinator();
        PropertyUnit pu = pc.getNewPropertyUnit();
        getSessionBean().setActivePropUnit(pu);
        
        return "addPropertyUnit";
    }
    
        public String storePropertyUnitInfo(){
            if(getSessionBean().getOccPermitApplication().getApplicationProperty() == null 
                && getSessionBean().getActivePropUnit() == null){        
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                       "Please select a property and property unit to continue.", ""));
            return "";
        }
        return "addReason";
        }
        
        public String storeNewPropertyUnit(){
            return "addReason";
        }
    
    
    
    public void removeSelectedUnit(PropertyUnit selectedUnit){
        propUnitsToAdd.remove(selectedUnit);
        clearAddUnitFormValues();
    }
    
    public void addUnitToPropWithLists(){
        PropertyCoordinator pc = getPropertyCoordinator();
        unitToAdd = pc.getNewPropertyUnit();
        unitToAdd.setUnitNumber(unitNum);
        unitToAdd.setRental(rental);
        unitToAdd.setNotes(unitNotes);
        
        if(propUnitsToAdd == null){
            propUnitsToAdd = new ArrayList<>();
        }
        
        propUnitsToAdd.add(unitToAdd);
        clearAddUnitFormValues();        
    }
    
    public void clearAddUnitFormValues(){
        unitNum = null;
        rental = false;
        unitNotes = null;
    }
    
    public String selectPropertyUnit(PropertyUnit unit){
        getSessionBean().setActivePropUnit(unit);
        return "addReason";
    }
    
    public String storeReason(){
        System.out.println("Occpermitapplication.storeReason");
        getSessionBean().setOccPermitApplicationReason(selectedApplicationReason);
        return "addPeople";
    }

    /**
     * @return the propWithLists
     */
    public PropertyWithLists getPropWithLists() {        
        return propWithLists;
    }

    /**
     * @param propWithLists the propWithLists to set
     */
    public void setPropWithLists(PropertyWithLists propWithLists) {
        this.propWithLists = propWithLists;
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

    /**
     * @return the selectedUnit
     */
    public PropertyUnit getSelectedUnit() {
        return selectedUnit;
    }

    /**
     * @param selectedUnit the selectedUnit to set
     */
    public void setSelectedUnit(PropertyUnit selectedUnit) {
        this.selectedUnit = selectedUnit;
    }

    /**
     * @return the unitToAdd
     */
    public PropertyUnit getUnitToAdd() {
        return unitToAdd;
    }

    /**
     * @param unitToAdd the unitToAdd to set
     */
    public void setUnitToAdd(PropertyUnit unitToAdd) {
        this.unitToAdd = unitToAdd;
    }

    /**
     * @return the propUnitsToAdd
     */
    public List<PropertyUnit> getPropUnitsToAdd() {
        return propUnitsToAdd;
    }

    /**
     * @param propUnitsToAdd the propUnitsToAdd to set
     */
    public void setPropUnitsToAdd(List<PropertyUnit> propUnitsToAdd) {
        this.propUnitsToAdd = propUnitsToAdd;
    }
    
        /**
     * @return the unitNum
     */
    public String getUnitNum() {
        return unitNum;
    }

    /**
     * @param unitNum the unitNum to set
     */
    public void setUnitNum(String unitNum) {
        this.unitNum = unitNum;
    }

    /**
     * @return the rental
     */
    public boolean isRental() {
        return rental;
    }

    /**
     * @param rental the rental to set
     */
    public void setRental(boolean rental) {
        this.rental = rental;
    }

    /**
     * @return the unitNotes
     */
    public String getUnitNotes() {
        return unitNotes;
    }

    /**
     * @param unitNotes the unitNotes to set
     */
    public void setUnitNotes(String unitNotes) {
        this.unitNotes = unitNotes;
    }

    /**
     * @return the selectedApplicationReason
     */
    public OccPermitApplicationReason getSelectedApplicationReason() {
        return selectedApplicationReason;
    }

    /**
     * @param selectedApplicationReason the selectedApplicationReason to set
     */
    public void setSelectedApplicationReason(OccPermitApplicationReason selectedApplicationReason) {
        this.selectedApplicationReason = selectedApplicationReason;
    }

    /**
     * @return the multiUnit
     */
    public boolean isMultiUnit() {
        return multiUnit;
    }

    /**
     * @param multiUnit the multiUnit to set
     */
    public void setMultiUnit(boolean multiUnit) {
        this.multiUnit = multiUnit;
    }
    
}
