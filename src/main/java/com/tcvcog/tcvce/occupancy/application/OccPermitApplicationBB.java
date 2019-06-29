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
import com.tcvcog.tcvce.entities.PropertyUnitChange;
import com.tcvcog.tcvce.entities.PropertyWithLists;
import com.tcvcog.tcvce.entities.search.SearchParamsPersons;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.occupancy.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.occupancy.entities.OccPermitApplication;
import com.tcvcog.tcvce.occupancy.entities.OccPermitApplicationReason;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Dominic Pimpinella
 */
public class OccPermitApplicationBB extends BackingBeanUtils implements Serializable {

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
    private List<PropertyUnit> workingPropUnits;
    private String unitNum;
    private boolean rental;
    private String unitNotes;

    private OccPermitApplicationReason selectedApplicationReason;
    private List<OccPermitApplicationReason> reasonList;

    private List<Person> attachedPersons;
    private Person applicationPerson;
    private Person applicant;
    private Person currentOwner;
    private Person newOwner;
    private Person contactPerson;
    private ArrayList<Person> newOccupants;

    private ArrayList<PersonType> requiredPersons;
    private ArrayList<PersonType> optAndReqPersons;

    private SearchParamsPersons params;

    /**
     * Creates a new instance of OccPermitApplicationBB
     */
    public OccPermitApplicationBB() {
    }

    /**
     *
     */
    @PostConstruct
    public void initBean() {

        OccupancyIntegrator opi = getOccupancyIntegrator();
        PropertyIntegrator pri = getPropertyIntegrator();

        try {
            reasonList = opi.getOccPermitApplicationReasons();

        } catch (IntegrationException ex) {
            System.out.println(ex);
        }

        propWithLists = getSessionBean().getActivePropWithLists();

        if (propWithLists != null) {

            if (getSessionBean().getWorkingPropWithLists() == null || !getSessionBean().getWorkingPropWithLists().getAddress().equalsIgnoreCase(propWithLists.getAddress())) {

                workingPropUnits = propWithLists.getUnitList();

                if (workingPropUnits == null) {

                    propWithLists.setUnitList(new ArrayList<PropertyUnit>());
                    workingPropUnits = new ArrayList<PropertyUnit>();

                } else {
                    try {
                        workingPropUnits = pri.getPropertyUnitList(propWithLists);
                    } catch (IntegrationException ex) {

                        System.out.println(ex);
                    }
                }

                getSessionBean().setWorkingPropWithLists(propWithLists);
            } else {

                workingPropUnits = getSessionBean().getWorkingPropWithLists().getUnitList();

            }

            if (attachedPersons == null && getSessionBean().getOccPermitApplication() != null) {
                attachedPersons = getSessionBean().getOccPermitApplication().getAttachedPersons();
                if (attachedPersons == null) {
                    attachedPersons = new ArrayList();
                }
            }

        }

        if (getSessionBean().getOccPermitApplication() != null
                && getSessionBean().getOccPermitApplication().getReason() != null
                && getSessionBean().getOccPermitApplication().getReason().getPersonsRequirement() != null) { //I apologize for the ugly code. It is necessary to prevent a null pointer exception. - Nathan

            requiredPersons = getSessionBean().getOccPermitApplication().getReason().getPersonsRequirement().getRequiredPersonTypes();
            
            optAndReqPersons = new ArrayList<>();
            
            optAndReqPersons.addAll(requiredPersons);

            optAndReqPersons.addAll(getSessionBean().getOccPermitApplication().getReason().getPersonsRequirement().getOptionalPersonTypes());

        }

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
     *
     * @param echo Input that needs filtered
     * @return The filtered list: this prevents the old list from being bound
     */
    public List<PropertyUnit> initPropUnits(List<PropertyUnit> echo) {

        return echo;
    }

    /**
     * @return the propUnitsToAdd
     */
    public List<PropertyUnit> getWorkingPropUnits() {
        return workingPropUnits;
    }

    /**
     * @param propUnitsToAdd the propUnitsToAdd to set
     */
    public void setWorkingPropUnits(List<PropertyUnit> propUnitsToAdd) {
        this.workingPropUnits = propUnitsToAdd;
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

    public Person getApplicant() {
        return applicant;
    }

    public void setApplicant(Person applicant) {
        this.applicant = applicant;
    }

    public List<Person> getAttachedPersons() {
        return attachedPersons;
    }

    public void setAttachedPersons(List<Person> attachedPersons) {
        this.attachedPersons = attachedPersons;
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

    public ArrayList<PersonType> getRequiredPersons() {
        return requiredPersons;
    }

    public void setRequiredPersons(ArrayList<PersonType> requiredPersons) {
        this.requiredPersons = requiredPersons;
    }

    public ArrayList<PersonType> getOptAndReqPersons() {
        return optAndReqPersons;
    }

    public void setOptAndReqPersons(ArrayList<PersonType> optAndReqPersons) {
        this.optAndReqPersons = optAndReqPersons;
    }

    /**
     * Set the user-selected municipality. The property search will be done
     * within this municipality.
     *
     * @return "chooseProperty" - Navigates to property selection page
     */
    public String setActiveMuni() {
        if (selectedMuni == null) {
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
    public void searchForPropertiesSingleMuni() {
        PropertyIntegrator pi = getPropertyIntegrator();
        Municipality activeMuni = getSessionBean().getActiveMuni();

        try {
            propList = pi.searchForProperties(houseNum, streetName, activeMuni.getMuniCode());

            if (propList.size() > 50) {
                propList.subList(50, propList.size()).clear(); //Limits the search to 50 results.  
            }
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
     * For multiunit properties: Gets the list of property units. For
     * non-multiunit properties: Sets the activePropertyUnit to default unit.
     *
     * Its functionality has been moved to the
     *
     * @deprecated
     */
    public void onPropertySelection() {
        PropertyCoordinator pc = getPropertyCoordinator();

        if (getSessionBean().getOccPermitApplication().isMultiUnit() == true) {
            try {
                propWithLists = pc.getPropertyUnitsWithoutDefault(selectedProperty);
            } catch (CaseLifecyleException ex) {
                System.out.println(ex);
            }
        } else {
            try {
                propWithLists = pc.getPropertyUnits(selectedProperty);
            } catch (CaseLifecyleException ex) {
                System.out.println(ex);
            }
        }

        getSessionBean().setActivePropWithLists(propWithLists);

        if (propWithLists.getUnitList().size() == 1) {
            List<PropertyUnit> propertyUnitList = propWithLists.getUnitList();
            getSessionBean().setActivePropUnit(propertyUnitList.get(0));
            getSessionBean().getOccPermitApplication().setApplicationPropertyUnit(propertyUnitList.get(0));
        }
    }

    /**
     * Adds a blank unit to propUnitsToAdd list. This newly-created unit can
     * then be selected and edited by the user.
     */
    public void addUnitToNewPropUnits() {
        PropertyCoordinator pc = getPropertyCoordinator();
        unitToAdd = pc.getNewPropertyUnit();
        unitToAdd.setUnitNumber("");
        unitToAdd.setRental(false);
        unitToAdd.setNotes("");

        if (workingPropUnits == null) {
            workingPropUnits = new ArrayList<>();
        }

        workingPropUnits.add(unitToAdd);
        clearAddUnitFormValues();
    }

    /**
     * Removes a newly-created unit from propUnitsToAdd list.
     *
     * @param selectedUnit
     */
    public void removeSelectedUnit(PropertyUnit selectedUnit) {
        workingPropUnits.remove(selectedUnit);
        clearAddUnitFormValues();
    }

    /**
     * Clears add unit form values, so that they are null if the user wishes to
     * add another unit.
     */
    public void clearAddUnitFormValues() {
        unitNum = null;
        rental = false;
        unitNotes = null;
    }

    /**
     * Sets the activePropWithLists according to the property the user has
     * selected, so they can then configure its units.
     *
     * @return
     */
    public String selectProperty() {

        PropertyCoordinator pc = getPropertyCoordinator();

        if (selectedProperty != null) {

            if (getSessionBean().getOccPermitApplication().isMultiUnit() == true) {
                try {
                    propWithLists = pc.getPropertyUnitsWithoutDefault(selectedProperty);
                } catch (CaseLifecyleException ex) {
                    System.out.println(ex);
                }
            } else {
                try {
                    propWithLists = pc.getPropertyUnits(selectedProperty);
                } catch (CaseLifecyleException ex) {
                    System.out.println(ex);
                }
            }

            getSessionBean().setActivePropWithLists(propWithLists);

            if (propWithLists.getUnitList().size() == 1) {
                List<PropertyUnit> propertyUnitList = propWithLists.getUnitList();
                getSessionBean().setActivePropUnit(propertyUnitList.get(0));
                getSessionBean().getOccPermitApplication().setApplicationPropertyUnit(propertyUnitList.get(0));
            }
            return "addPropertyUnit";
        } else {

            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a property.", ""));

            return "";
        }
    }

    /**
     * Sets a property unit on the SessionBean and the OccPermitApplication,
     * sends user to occPermitAddReason.xhtml
     *
     * @param unit
     * @return
     */
    public String selectPropertyUnit(PropertyUnit unit) {

        boolean missingUnitNum = false;
        boolean duplicateUnitNum = false;
        int duplicateNums = 0; //The above boolean is a flag to see if there is more than 1 of  Unit Number. The int to the left stores how many of a given number the loop below finds.

        for (PropertyUnit firstUnit : workingPropUnits) {
            duplicateNums = 0;

            if (firstUnit.getUnitNumber().compareTo("") == 0) {
                missingUnitNum = true;
                break; //break for performance reasons. Can be removed if breaks are not welcome here.
            }

            for (PropertyUnit secondUnit : workingPropUnits) {
                if (firstUnit.getUnitNumber().compareTo(secondUnit.getUnitNumber()) == 0) {
                    duplicateNums++;
                }
            }

            if (duplicateNums > 1) {
                duplicateUnitNum = true;
                break; //break for performance reasons. Can be removed if breaks are not welcome here.
            }
        }

        if (workingPropUnits.isEmpty()) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please add at least one unit.", ""));
            return "";
        } else if (missingUnitNum) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "All units must have a Unit Number", ""));
            return "";

        } else if (duplicateUnitNum) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Some Units have the same Number", ""));
            return "";

        } else {
            unit.setThisProperty(getSessionBean().getActivePropWithLists());
            getSessionBean().setActivePropUnit(unit);
            getSessionBean().getOccPermitApplication().setApplicationPropertyUnit(unit);

            return "addReason";
        }
    }

    /**
     * Finalizes the unit list the user has created so that it can be compared
     * to the existing one in the database.
     *
     * @return
     */
    public String finalizeUnitList() {

        boolean missingUnitNum = false;
        boolean duplicateUnitNum = false;
        int duplicateNums = 0; //The above boolean is a flag to see if there is more than 1 of  Unit Number. The int to the left stores how many of a given number the loop below finds.

        for (PropertyUnit firstUnit : workingPropUnits) {
            duplicateNums = 0;

            firstUnit.setUnitNumber(firstUnit.getUnitNumber().replaceAll("(?i)unit", ""));
            
            if (firstUnit.getUnitNumber().compareTo("") == 0) {
                missingUnitNum = true;
                break; //break for performance reasons. Can be removed if breaks are not welcome here.
            }

            for (PropertyUnit secondUnit : workingPropUnits) {
                if (firstUnit.getUnitNumber().compareTo(secondUnit.getUnitNumber()) == 0) {
                    duplicateNums++;
                }
            }

            if (duplicateNums > 1) {
                duplicateUnitNum = true;
                break; //break for performance reasons. Can be removed if breaks are not welcome here.
            }
        }

        if (workingPropUnits.isEmpty()) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please add at least one unit.", ""));
            return "";
        } else if (missingUnitNum) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "All units must have a Unit Number", ""));
            return "";

        } else if (duplicateUnitNum) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Some Units have the same Number", ""));
            return "";

        } else {
            getSessionBean().getOccPermitApplication().setMultiUnit(workingPropUnits.size() > 1); //if there is more than one unit on the workingPropUnits list, set it to multiunit.
            getSessionBean().getWorkingPropWithLists().setUnitList(workingPropUnits);
            getSessionBean().getActivePropWithLists().setUnitList(workingPropUnits); //This line is different from the original method (above)
            return "selectForApply";
        }

    }

    /**
     * Checks that the user has not selected a multiunit property without also
     * selecting a property unit. Sends user to occPermitAddReason.xhtml
     *
     * @return
     */
    public String storePropertyUnitInfo() {
        if (getSessionBean().getOccPermitApplication().getApplicationPropertyUnit() == null
                && getSessionBean().getActivePropUnit() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "This is a multiunit property, please select or add a unit", ""));
            return "";
        }
        return "addReason";
    }

    /**
     * Gets a list of reasons for an occupancy permit application to be
     * displayed.
     *
     * @return
     */
    public List<OccPermitApplicationReason> getReasonList() {
        return reasonList;
    }

    /**
     * Sends user to personsRequirementManage.xhtml
     *
     * @return
     */
    public String storeReason() {
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
    public String attachPerson(PersonType personType) {
        getSessionBean().setActivePersonType(personType);
        return "searchPeople";
    }

    public void addPersonToApplication(Person person) {

        boolean duplicateFlag = false;

        person.setPersonType(PersonType.Other);

        for (Person test : attachedPersons) {

            if (test.getPersonID() == person.getPersonID()) {

                duplicateFlag = true;

                break;

            }

        }

        if (duplicateFlag) {

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "You already attached that person to the application.", ""));

        } else {

            attachedPersons.add(person);

        }
    }

    public void removePersonFromApplication(Person person) {
        attachedPersons.remove(person);
    }

    public String editPersonInfo(Person person) {
        person.setPersonType(getSessionBean().getActivePersonType());
        List<Person> attachedPersons = getSessionBean().getOccPermitApplication().getAttachedPersons();

        if (attachedPersons == null) {
            attachedPersons = new ArrayList();
        }
        getSessionBean().getOccPermitApplication().setAttachedPersons(attachedPersons);
        getSessionBean().setActiveAnonPerson(person);
        return "editPerson";
    }

    public String addANewPerson() {
        OccPermitApplication temp = getSessionBean().getOccPermitApplication();

        for (Person p : attachedPersons) {

            if (p.isApplicant()) {

                if (applicant == null) {

                    applicant = p;

                } else {

                    getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "You have identified multiple people as yourself.", ""));
                    return "";

                }

            }

        }

        temp.setApplicantPerson(applicant);

        temp.setAttachedPersons(attachedPersons);

        return "addPerson";
    }

    public ArrayList<String> getPersonRequirementDescription() {

        ArrayList<PersonType> required = requiredPersons;

        ArrayList<PersonType> optional = getSessionBean().getOccPermitApplication().getReason().getPersonsRequirement().getOptionalPersonTypes();

        StringBuilder description = new StringBuilder("It is required that you have these types of people: ");

        ArrayList<String> descList = new ArrayList<String>();

        for (PersonType type : required) {

            description.append(type.getLabel() + ", ");

        }

        description.deleteCharAt(description.lastIndexOf(","));

        descList.add(description.toString());

        description = new StringBuilder();

        description.append("You may also add these types of people: ");

        for (PersonType type : optional) {

            description.append(type.getLabel() + ", ");

        }

        if (optional.size() > 0) {
            description.deleteCharAt(description.lastIndexOf(","));
        }

        descList.add(description.toString());

        description = new StringBuilder();

        description.append("Also, please identify yourself by checking the check box in the row with your name.");

        descList.add(description.toString());

        return descList;

    }

    public String reviewApplication() {

        applicant = null;

        ArrayList<Integer> countTypes = new ArrayList();

        for (int index = 0; index < 17; index++) {

            countTypes.add(0);

        }
        for (Person p : attachedPersons) {

            int index = p.getPersonType().ordinal();

            int temp = countTypes.get(index) + 1;

            countTypes.set(index, temp);

            if (p.isApplicant()) {

                if (applicant == null) {

                    applicant = p;

                } else {

                    getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "You have identified multiple people as yourself.", ""));
                    return "";

                }

            }

        }

        for (PersonType type : requiredPersons) {

            int index = type.ordinal();

            if (countTypes.get(index) < 1) {

                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "You are missing a " + type.getLabel(), ""));
                return "";

            }

        }

        if (applicant == null) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please identify yourself by selecting your name from the list below.", ""));
            return "";
        }

        getSessionBean().getOccPermitApplication().setApplicantPerson(applicant);

        getSessionBean().getOccPermitApplication().setAttachedPersons(attachedPersons);

        return "reviewApplication";
    }

    public String submitApplication() {

        submitUnitChangeList();

        OccupancyIntegrator opi = getOccupancyIntegrator();        
        try {
            int applicationId = opi.insertOccPermitApplicationAndReturnId(getSessionBean().getOccPermitApplication());
            getSessionBean().getOccPermitApplication().setId(applicationId);
            opi.insertOccPermitPersons(getSessionBean().getOccPermitApplication());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
        return "selectForApply";
    }

    public void submitUnitChangeList() {

        ArrayList<PropertyUnitChange> changeList = new ArrayList<PropertyUnitChange>();
        
        PropertyIntegrator pri = getPropertyIntegrator();
        
        for (PropertyUnit workingUnit : workingPropUnits) {

            PropertyUnitChange skeleton = new PropertyUnitChange();

            boolean added = true;

            for (PropertyUnit activeUnit : propWithLists.getUnitList()) {

                if (workingUnit.getUnitID() == activeUnit.getUnitID()) {

                    added = false;

                    skeleton.setUnitID(workingUnit.getUnitID());

                    if (workingUnit.getOtherKnownAddress() != null && workingUnit.getOtherKnownAddress().compareToIgnoreCase(activeUnit.getOtherKnownAddress()) != 0) {

                        skeleton.setOtherKnownAddress(workingUnit.getOtherKnownAddress());

                    }

                    if (workingUnit.getNotes().compareToIgnoreCase(activeUnit.getNotes()) != 0) {

                        skeleton.setNotes(workingUnit.getNotes());

                    }

                    if (workingUnit.isRental() != activeUnit.isRental()) {

                        skeleton.setRental(workingUnit.isRental());

                    }

                }

            }

            if (added == true) {

                skeleton.setOtherKnownAddress(workingUnit.getOtherKnownAddress());

                skeleton.setNotes(workingUnit.getNotes());

                skeleton.setRental(workingUnit.isRental());

            }

            skeleton.setAdded(added);

            if (skeleton.changedOccured()) {

                changeList.add(skeleton);
            }

        }

        for (PropertyUnit activeUnit : propWithLists.getUnitList()) {

            PropertyUnitChange skeleton = new PropertyUnitChange();

            boolean removed = true;

            for (PropertyUnit workingUnit : workingPropUnits) {

                if (workingUnit.getUnitID() == activeUnit.getUnitID()) {

                    removed = false;

                }

            }

            if (removed == true) {

                skeleton.setOtherKnownAddress(activeUnit.getOtherKnownAddress());

                skeleton.setNotes(activeUnit.getNotes());

                skeleton.setRental(activeUnit.isRental());

                skeleton.setRemoved(removed);

                changeList.add(skeleton);

            }

        }

        
        for(PropertyUnitChange order : changeList) {
            
            try {
                pri.insertPropertyUnitChange(order);
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
            
            
        }
        
        System.out.println("end of submitting unit change list");
    }

}
