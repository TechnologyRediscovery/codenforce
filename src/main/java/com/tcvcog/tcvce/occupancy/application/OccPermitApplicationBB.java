/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyWithLists;
import com.tcvcog.tcvce.entities.search.SearchParamsPersons;
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
    
    private Person applicationPerson;
    private Person currentOwner;
    private Person newOwner;
    private Person contactPerson;
    private ArrayList<Person> newOccupants;
    
    private SearchParamsPersons params;
    
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

    /**
     * @return the params
     */
    public SearchParamsPersons getParams() {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(SearchParamsPersons params) {
        this.params = params;
    }

    /**
     * @return the applicationPerson
     */
    public Person getApplicationPerson() {
        return applicationPerson;
    }

    /**
     * @param applicationPerson the applicationPerson to set
     */
    public void setApplicationPerson(Person applicationPerson) {
        this.applicationPerson = applicationPerson;
    }
    
    /**
     * Set the user-selected municipality. The property search will be done within this municipality.
     * @return "chooseProperty" - Navigates to property selection page
     */    
    public String setActiveMuni(){
        if (selectedMuni == null){
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                       "Please select a municipality.", ""));
            return "";
        }
        getSessionBean().setActiveMuni(selectedMuni);
        OccupancyCoordinator oc = getOccupancyCoordinator();
        OccPermitApplication occpermitapp = oc.getNewOccPermitApplication();                        
        getSessionBean().setOccPermitApplication(occpermitapp);
        return "chooseProperty";
    }
    
    /**
     * Searches for a property within the activeMuni set on the SessionBean.
     */
    public void searchForPropertiesSingleMuni(){
        PropertyIntegrator pi = getPropertyIntegrator();
        Municipality activeMuni = getSessionBean().getActiveMuni();

        try {
            propList = pi.searchForProperties(houseNum, streetName, activeMuni.getMuniCode());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Your search completed with " + getPropList().size() + " result(s)", ""));    
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to complete a property search! Sorry!", ""));            
        }
    }
    
    /**
     * For multiunit properties: Gets the list of property units.
     * For non-multiunit properties: Sets the activePropertyUnit to default unit. 
     * 
     */
    public void onPropertySelection(){
        PropertyCoordinator pc = getPropertyCoordinator();
        
        if(getSessionBean().getOccPermitApplication().isMultiUnit() == true){
            try {
            propWithLists = pc.getPropertyUnitsWithoutDefault(selectedProperty);
            } catch (CaseLifecyleException ex) {
                System.out.println(ex);
            }
        } 
        else {
            try {
            propWithLists = pc.getPropertyUnits(selectedProperty);
            } catch (CaseLifecyleException ex) {
                System.out.println(ex);
            } 
        }
       
        getSessionBean().setActivePropWithLists(propWithLists);
        
        if (propWithLists.getUnitList().size() == 1){
            List<PropertyUnit> propertyUnitList = propWithLists.getUnitList();
            getSessionBean().setActivePropUnit(propertyUnitList.get(0));
            getSessionBean().getOccPermitApplication().setApplicationPropertyUnit(propertyUnitList.get(0));
        }
    }
    
    /**
     * Adds a user-created unit to propUnitsToAdd list. This newly-created unit can then be selected
     * an added to the application.
     */
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
    
    /**
     * Removes a newly-created unit from propUnitsToAdd list.
     * @param selectedUnit 
     */
    public void removeSelectedUnit(PropertyUnit selectedUnit){
        propUnitsToAdd.remove(selectedUnit);
        clearAddUnitFormValues();
    }

    /**
     * Clears add unit form values, so that they are null if the user wishes to add another unit.
     */
    public void clearAddUnitFormValues(){
        unitNum = null;
        rental = false;
        unitNotes = null;
    }        
    
    /**
     * Sets an empty PropertyUnit to activePropUnit on the SessionBean, sends user to occPermitAddPropertyUnit.xhtml
     * @return 
     */
    public String addPropertyUnit(){
        PropertyCoordinator pc = getPropertyCoordinator();
        PropertyUnit pu = pc.getNewPropertyUnit();
        getSessionBean().setActivePropUnit(pu);
       
        return "addPropertyUnit";
    }
    
    /**
     * Sets a property unit on the SessionBean and the OccPermitApplication, sends user to occPermitAddReason.xhtml
     * @param unit
     * @return 
     */
    public String selectPropertyUnit(PropertyUnit unit){
        unit.setThisProperty(getSessionBean().getActivePropWithLists());
        getSessionBean().setActivePropUnit(unit);
        getSessionBean().getOccPermitApplication().setApplicationPropertyUnit(unit);
        return "addReason";
    }
    
    /**
     * Checks that the user has not selected a multiunit property without also selecting a property unit. 
     * Sends user to occPermitAddReason.xhtml
     * @return 
     */
    public String storePropertyUnitInfo(){
        if(getSessionBean().getOccPermitApplication().getApplicationPropertyUnit() == null 
            && getSessionBean().getActivePropUnit() == null){         
            getFacesContext().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                   "This is a multiunit property, please select or add a unit", ""));
            return "";
        }
        return "addReason";
    }
    
    /**
     * Gets a list of reasons for an occupancy permit application to be displayed.
     * @return 
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
     * Sends user to personsRequirementManage.xhtml
     * @return 
     */
    public String storeReason(){
        return "managePeople";
    }
        
    /*
    DP 5/20/2019: The methods below relating to people must be reworked. The goal is to make sure 
    all of the correct people are added per the PersonsRequirement for the selected 
    OccPermitApplicationReason. The user must be able to find people in the database via a search, 
    edit the person if necessary, or alternatively, create and attach a brand new Person to the application.
    
    Features that must be added/reworked:
    - public person search
    - person edit
        - Create a clone Person and edit that
        - Using clones avoids public users overwriting existing fields in person table in the database
    - person add
    - set applicant and preferredContact booleans on OccPermitApplication object
    - Verify that the PersonsRequirement is met (use OccupancyCoordinator.verifyOccPermitPersonsRequirement())    
    */
    public String attachPerson(PersonType personType){
        getSessionBean().setActivePersonType(personType);
        return "searchPeople";
    }
    
    public String addPersonToApplication(Person person){
        person.setPersonType(getSessionBean().getActivePersonType());
        List<Person> attachedPersons = getSessionBean().getOccPermitApplication().getAttachedPersons();
        
        if (attachedPersons == null){
            attachedPersons = new ArrayList();
        }
        attachedPersons.add(person);
        getSessionBean().getOccPermitApplication().setAttachedPersons(attachedPersons);
        OccupancyCoordinator oc = getOccupancyCoordinator();
        oc.verifyOccPermitPersonsRequirement(getSessionBean().getOccPermitApplication());
        return "managePeople";              
    }    
    
    public String editPersonInfo(Person person){
        person.setPersonType(getSessionBean().getActivePersonType());
        List<Person> attachedPersons = getSessionBean().getOccPermitApplication().getAttachedPersons();
        
        if (attachedPersons == null){
            attachedPersons = new ArrayList();
        }
        getSessionBean().getOccPermitApplication().setAttachedPersons(attachedPersons);
        getSessionBean().setActiveAnonPerson(person);
        return "editPerson";
    }
    
    public String addANewPerson(){        
        return "addPerson";
    }
    
    public String reviewApplication(){
        if (getSessionBean().getOccPermitApplication().getApplicantPerson() == null) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                       "Please identify yourself by selecting your name from the list below.", ""));
            return "";
        }
        return "reviewApplication";
    }
    
    public String submitApplication(){
        OccupancyPermitIntegrator opi = getOccupancyPermitIntegrator();
        try{
            int applicationId = opi.insertOccPermitApplicationAndReturnId(getSessionBean().getOccPermitApplication());
            getSessionBean().getOccPermitApplication().setId(applicationId);
            opi.insertOccPermitPersons(getSessionBean().getOccPermitApplication());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }          
        
        return "";
    }
}
