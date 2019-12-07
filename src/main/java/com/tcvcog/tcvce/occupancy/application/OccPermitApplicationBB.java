/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.CaseLifecycleException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyUnitChange;
import com.tcvcog.tcvce.entities.PropertyDataHeavy;
import com.tcvcog.tcvce.entities.search.SearchParamsPerson;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.entities.occupancy.OccPermitApplication;
import com.tcvcog.tcvce.entities.occupancy.OccPermitApplicationReason;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

/**
 *
 * @author Dominic Pimpinella, NADGIT, and Sylvia
 *
 */
public class OccPermitApplicationBB extends BackingBeanUtils implements Serializable {

    private Municipality selectedMuni;
    private Date form_dateOfRecord;

    private boolean multiUnit;
    private String houseNum;
    private String streetName;
    private Property selectedProperty;
    private List<Property> propList;
    private Property prop;

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
    private List<Person> newOccupants;

    private List<PersonType> requiredPersons;
    private List<PersonType> optAndReqPersons;

    private SearchParamsPerson params;

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
        
        // check if we're working with an internal user or the public form
        if(getSessionBean().getSessionUser() != null){
            
            // internal user procedure
            
            selectedMuni = getSessionBean().getSessionMuni();
            Property p = getSessionBean().getSessionProperty();
            workingPropUnits = p.getUnitList();
            

        } else { // start public user init procedure
            
            prop = getSessionBean().getOccPermitAppActiveProp();

            if (prop != null) {

                if (getSessionBean().getOccPermitAppWorkingProp() == null 
                        || 
                    !getSessionBean().getOccPermitAppWorkingProp().getAddress().equalsIgnoreCase(prop.getAddress())) {

                    workingPropUnits = prop.getUnitList();

                    if (workingPropUnits == null) {

                        prop.setUnitList(new ArrayList<PropertyUnit>());
                        workingPropUnits = new ArrayList<>();

                    } else {
                        try {
                            workingPropUnits = pri.getPropertyUnitList(prop);
                        } catch (IntegrationException ex) {

                            System.out.println(ex);
                        }
                    }

                    getSessionBean().setOccPermitAppWorkingProp(prop);
                } else {

                    workingPropUnits = getSessionBean().getOccPermitAppWorkingProp().getUnitList();

                }
            }

            if (attachedPersons == null && getSessionBean().getSessionOccPermitApplication() != null) {
                attachedPersons = getSessionBean().getSessionOccPermitApplication().getAttachedPersons();
                if (attachedPersons == null) {
                    attachedPersons = new ArrayList();
                }
            }

        }

        //I apologize for the ugly code. It is necessary to prevent a null pointer exception. - Nathan
        if (getSessionBean().getSessionOccPermitApplication() != null
                && getSessionBean().getSessionOccPermitApplication().getReason() != null
                && getSessionBean().getSessionOccPermitApplication().getReason().getPersonsRequirement() != null) { 

            requiredPersons = getSessionBean().getSessionOccPermitApplication().getReason().getPersonsRequirement().getRequiredPersonTypes();

            optAndReqPersons = new ArrayList<>();

            optAndReqPersons.addAll(requiredPersons);

            optAndReqPersons.addAll(getSessionBean().getSessionOccPermitApplication().getReason().getPersonsRequirement().getOptionalPersonTypes());

        }

    } // end postConstruct
    
    
    public String beginInternalOccApp(PropertyUnit pu) throws IntegrationException, CaseLifecycleException{
        OccupancyCoordinator oc = getOccupancyCoordinator();
        PropertyIntegrator pi = getPropertyIntegrator();
        
        OccPermitApplication occpermitapp = oc.getNewOccPermitApplication();
        getSessionBean().setSessionOccPermitApplication(occpermitapp);
        
        getSessionBean().setOccPermitAppActiveProp(pi.getProperty(getSessionBean().getSessionProperty().getPropertyID()));

        if (prop.getUnitList().size() == 1) {
            List<PropertyUnit> propertyUnitList = prop.getUnitList();
            getSessionBean().setOccPermitAppActivePropUnit(propertyUnitList.get(0));
            getSessionBean().getSessionOccPermitApplication().setApplicationPropertyUnit(pu);
        }
        
        
        
        return "beginInternalOccUpp";
        
    }
    

    /**
     * Set the user-selected municipality. The property search will be done
     * within this municipality.
     *
     * @return "chooseProperty" - Navigates to property selection page
     */
    public String recordSelectedMuni() {
        if (selectedMuni == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a municipality.", ""));
            return "";
        }
//        getSessionBean().recordSelectedMuni(selectedMuni);
        OccupancyCoordinator oc = getOccupancyCoordinator();
        OccPermitApplication occpermitapp = oc.getNewOccPermitApplication();
        getSessionBean().setSessionOccPermitApplication(occpermitapp);
        return "chooseProperty";
    }

    /**
     * Searches for a property within the activeMuni set on the SessionBean.
     */
    public void searchForPropertiesSingleMuni() {
        PropertyIntegrator pi = getPropertyIntegrator();
        Municipality activeMuni = getSessionBean().getSessionMuni();

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
// todo: occbeta
//        if (getSessionBean().getOccPermitApplication().isMultiUnit() == true) {
//            try {
//                propWithLists = pc.getPropertyUnitsWithoutDefault(selectedProperty);
//            } catch (CaseLifecycleException ex) {
//                System.out.println(ex);
//            }
//        } else {
//            try {
//                propWithLists = pc.getPropertyUnits(selectedProperty);
//            } catch (CaseLifecycleException ex) {
//                System.out.println(ex);
//            }
//        }

        getSessionBean().setOccPermitAppActiveProp(prop);

        if (prop.getUnitList().size() == 1) {
            List<PropertyUnit> propertyUnitList = prop.getUnitList();
            getSessionBean().setOccPermitAppActivePropUnit(propertyUnitList.get(0));
            getSessionBean().getSessionOccPermitApplication().setApplicationPropertyUnit(propertyUnitList.get(0));
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
//        unitToAdd.setRental(false);
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
// todo occbeta
//            if (getSessionBean().getOccPermitApplication().isMultiUnit() == true) {
//                try {
//                    propWithLists = pc.getPropertyUnitsWithoutDefault(selectedProperty);
//                } catch (CaseLifecycleException ex) {
//                    System.out.println(ex);
//                }
//            } else {
//                try {
//                    propWithLists = pc.getPropertyUnits(selectedProperty);
//                } catch (CaseLifecycleException ex) {
//                    System.out.println(ex);
//                }
//            }

            getSessionBean().setOccPermitAppActiveProp(prop);

            if (prop.getUnitList().size() == 1) {
                List<PropertyUnit> propertyUnitList = prop.getUnitList();
                getSessionBean().setOccPermitAppActivePropUnit(propertyUnitList.get(0));
                getSessionBean().getSessionOccPermitApplication().setApplicationPropertyUnit(propertyUnitList.get(0));
            }
            
            try {
                ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
                ec.redirect("/tcvce/public/services/occPermitApplicationFlow/occPermitAddPropertyUnit.xhtml#currentStep");
            } catch (IOException ex) {
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
            unit.setPropertyID(getSessionBean().getOccPermitAppActiveProp().getPropertyID());
            getSessionBean().setOccPermitAppActivePropUnit(unit);
            getSessionBean().getSessionOccPermitApplication().setApplicationPropertyUnit(unit);

            try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            
                ec.redirect("/tcvce/public/services/occPermitApplicationFlow/occPermitAddReason.xhtml#currentStep");
            } catch (IOException ex) {
            }
            
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
//            getSessionBean().getOccPermitApplication().setMultiUnit(workingPropUnits.size() > 1); //if there is more than one unit on the workingPropUnits list, set it to multiunit.
            getSessionBean().getOccPermitAppWorkingProp().setUnitList(workingPropUnits);
            getSessionBean().getOccPermitAppActiveProp().setUnitList(workingPropUnits); //This line is different from the original method (above)
            
            try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
   
            
                ec.redirect("/tcvce/public/services/occPermitApplicationFlow/occPermitSelectForApply.xhtml#currentStep");
            } catch (IOException ex) {
            }
            
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
        if (getSessionBean().getSessionOccPermitApplication().getApplicationPropertyUnit() == null
                && getSessionBean().getOccPermitAppActivePropUnit() == null) {
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
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            
                ec.redirect("/tcvce/public/services/occPermitApplicationFlow/personsRequirementManage.xhtml#currentStep");
            } catch (IOException ex) {
            }
        
        return "managePeople";
    }

    /*
    DP 5/20/2019: The methods below relating to people must be reworked. The goal is to make sure 
    all of the correct people are added per the OccAppPersonRequirement for the selected 
    OccPermitApplicationReason. The user must be able to find people in the database via a search, 
    edit the person if necessary, or alternatively, create and attach a brand new Person to the application.
    
    Features that must be added/reworked:
    - public person search
    - person edit
        - Create a clone Person and edit that
        - Using clones avoids public users overwriting existing fields in person table in the database
    - person add
    - set applicant and preferredContact booleans on OccPermitApplication object
    - Verify that the OccAppPersonRequirement is met (use OccupancyCoordinator.verifyOccPermitPersonsRequirement())    
     */
    public String attachPerson(PersonType personType) {
        getSessionBean().setOccPermitAppActivePersonType(personType);
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
        person.setPersonType(getSessionBean().getOccPermitAppActivePersonType());
        attachedPersons = getSessionBean().getSessionOccPermitApplication().getAttachedPersons();

        if (attachedPersons == null) {
            attachedPersons = new ArrayList();
        }
        getSessionBean().getSessionOccPermitApplication().setAttachedPersons(attachedPersons);
        getSessionBean().setActiveAnonPerson(person);
        return "editPerson";
    }

    public String addANewPerson() {
        OccPermitApplication temp = getSessionBean().getSessionOccPermitApplication();

        for (Person p : attachedPersons) {
// todo occbeta
//            if (p.isApplicant()) {
//
//                if (applicant == null) {
//
//                    applicant = p;
//
//                } else {
//
//                    getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
//                            "You have identified multiple people as yourself.", ""));
//                    return "";
//
//                }
//
//            }

        }

        temp.setApplicantPerson(applicant);

        temp.setAttachedPersons(attachedPersons);

        return "addPerson";
    }

    public List<String> getPersonRequirementDescription() {

        List<PersonType> required = requiredPersons;

        List<PersonType> optional = getSessionBean().getSessionOccPermitApplication().getReason().getPersonsRequirement().getOptionalPersonTypes();

        StringBuilder description = new StringBuilder("It is required that you have these types of people: ");

        List<String> descList = new ArrayList<>();

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

        List<Integer> countTypes = new ArrayList();

        for (int index = 0; index < 17; index++) {

            countTypes.add(0);

        }
        for (Person p : attachedPersons) {

            int index = p.getPersonType().ordinal();

            int temp = countTypes.get(index) + 1;

            countTypes.set(index, temp);

//            todo occbeta
//            if (p.isApplicant()) {
//
//                if (applicant == null) {
//
//                    applicant = p;
//
//                } else {
//
//                    getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
//                            "You have identified multiple people as yourself.", ""));
//                    return "";
//
//                }
//
//            }

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

        getSessionBean().getSessionOccPermitApplication().setApplicantPerson(applicant);

        getSessionBean().getSessionOccPermitApplication().setAttachedPersons(attachedPersons);

        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            
                ec.redirect("/tcvce/public/services/occPermitApplicationFlow/reviewAndSubmit.xhtml");
            } catch (IOException ex) {
            }
        
        return "reviewApplication";
    }

    public String submitApplication(String redir) {

        submitUnitChangeList();

        OccupancyIntegrator opi = getOccupancyIntegrator();        
        try {
            int applicationId = opi.insertOccPermitApplicationAndReturnId(getSessionBean().getSessionOccPermitApplication());
            getSessionBean().getSessionOccPermitApplication().setId(applicationId);
            opi.insertOccPeriodPersons(getSessionBean().getSessionOccPermitApplication());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }

        return redir;
    }

    public void submitUnitChangeList() {

        List<PropertyUnitChange> changeList = new ArrayList<>();

        PropertyIntegrator pri = getPropertyIntegrator();

        PropertyDataHeavy existingProp = new PropertyDataHeavy();
        
        Person changedby = getSessionBean().getSessionOccPermitApplication().getApplicantPerson();
        
        try {
            existingProp = pri.getPropertyDataHeavy(prop.getPropertyID());
                    
        } catch (IntegrationException | CaseLifecycleException | EventException | AuthorizationException ex) {
            System.out.println(ex);
        }
        
        for (PropertyUnit workingUnit : workingPropUnits) {

            PropertyUnitChange skeleton = new PropertyUnitChange();

            boolean added = true;

            skeleton.setPropertyID(getSessionBean().getSessionOccPermitApplication().getApplicationPropertyUnit().getPropertyID());
            
            if (changedby.getPersonID() != 0)
            {
                
                PersonIntegrator pi = getPersonIntegrator();
                
                Person temp = new Person();
                
                try {
                    temp = pi.getPerson(changedby.getPersonID());
                } catch (IntegrationException ex) {
                    System.out.println(ex);
                }
                
                String changeName = temp.getFirstName() + " " + temp.getLastName() + " ID: " + temp.getPersonID();
                
                skeleton.setChangedBy(changeName);
                
            }
            else
            {
            
                skeleton.setChangedBy(changedby.getFirstName() + " " + changedby.getLastName());
                
            }
            
            for (PropertyUnit activeUnit : existingProp.getUnitList()) {

                if (workingUnit.getUnitID() == activeUnit.getUnitID() && workingUnit.getUnitID() != 0) {

                    added = false;

                    skeleton.setUnitID(workingUnit.getUnitID());
                    
                    if(workingUnit.getUnitNumber() != null) {
                        
                        skeleton.setUnitNumber(workingUnit.getUnitNumber());
                        
                    }
                    
                    if (workingUnit.getOtherKnownAddress() != null && workingUnit.getOtherKnownAddress().compareToIgnoreCase(activeUnit.getOtherKnownAddress()) != 0) {

                        skeleton.setOtherKnownAddress(workingUnit.getOtherKnownAddress());

                    }

                    if (workingUnit.getNotes().compareToIgnoreCase(activeUnit.getNotes()) != 0) {

                        skeleton.setNotes(workingUnit.getNotes());

                    }

//                    if (workingUnit.isRental() != activeUnit.isRental()) {
//
//                        skeleton.setRental(workingUnit.isRental());
//
//                    }

                }

            }

            if (added == true) {

                skeleton.setUnitNumber(workingUnit.getUnitNumber());
                
                skeleton.setOtherKnownAddress(workingUnit.getOtherKnownAddress());

                skeleton.setNotes(workingUnit.getNotes());

//                skeleton.setRental(workingUnit.isRental());

            }

            skeleton.setAdded(added);

            if (skeleton.changedOccured()) {

                changeList.add(skeleton);
            }

        }

        for (PropertyUnit activeUnit : existingProp.getUnitList()) {

            PropertyUnitChange skeleton = new PropertyUnitChange();

            skeleton.setPropertyID(getSessionBean().getSessionOccPermitApplication().getApplicationPropertyUnit().getPropertyID());
            
            if (changedby.getPersonID() != 0)
            {
                
                PersonIntegrator pi = getPersonIntegrator();
                
                Person temp = new Person();
                
                try {
                    temp = pi.getPerson(changedby.getPersonID());
                } catch (IntegrationException ex) {
                    System.out.println(ex);
                }
                
                String changeName = temp.getFirstName() + " " + temp.getLastName() + " ID: " + temp.getPersonID();
                
                skeleton.setChangedBy(changeName);
            }
            else
            {
            
                skeleton.setChangedBy(changedby.getFirstName() + " " + changedby.getLastName());
                
            }
            
            boolean removed = true;

            for (PropertyUnit workingUnit : workingPropUnits) {

                if (workingUnit.getUnitID() == activeUnit.getUnitID()) {

                    removed = false;

                }

            }

            if (removed == true) {

                skeleton.setUnitID(activeUnit.getUnitID());
                
                skeleton.setUnitNumber(activeUnit.getUnitNumber());
                
                skeleton.setOtherKnownAddress(activeUnit.getOtherKnownAddress());

                skeleton.setNotes(activeUnit.getNotes());

//                skeleton.setRental(activeUnit.isRental());

                skeleton.setRemoved(removed);

                changeList.add(skeleton);

            }

        }

        for (PropertyUnitChange order : changeList) {

            try {
                pri.insertPropertyUnitChange(order);
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }

        }

        System.out.println("end of submitting unit change list");
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

    public void setPropList(List<Property> propList) {
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
    public List<Person> getNewOccupants() {
        return newOccupants;
    }

    /**
     * @param newOccupants the newOccupants to set
     */
    public void setNewOccupants(List<Person> newOccupants) {
        this.newOccupants = newOccupants;
    }

    /**
     * @return the propWithLists
     */
    public Property getProp() {
        return prop;
    }

    /**
     * @param p the propWithLists to set
     */
    public void setProp(Property p) {
        this.prop = p;
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
    public SearchParamsPerson getParams() {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(SearchParamsPerson params) {
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

    public List<PersonType> getRequiredPersons() {
        return requiredPersons;
    }

    public void setRequiredPersons(List<PersonType> requiredPersons) {
        this.requiredPersons = requiredPersons;
    }

    public List<PersonType> getOptAndReqPersons() {
        return optAndReqPersons;
    }

    public void setOptAndReqPersons(List<PersonType> optAndReqPersons) {
        this.optAndReqPersons = optAndReqPersons;
    }
    
}
