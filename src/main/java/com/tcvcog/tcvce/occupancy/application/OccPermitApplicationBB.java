/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.coordinators.PublicInfoCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyUnitChangeOrder;
import com.tcvcog.tcvce.entities.PropertyDataHeavy;
import com.tcvcog.tcvce.entities.PublicInfoBundlePerson;
import com.tcvcog.tcvce.entities.PublicInfoBundleProperty;
import com.tcvcog.tcvce.entities.PublicInfoBundlePropertyUnit;
import com.tcvcog.tcvce.entities.search.SearchParamsPerson;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.entities.occupancy.OccPermitApplication;
import com.tcvcog.tcvce.entities.occupancy.OccPermitApplicationReason;
import com.tcvcog.tcvce.entities.search.QueryPerson;
import com.tcvcog.tcvce.entities.search.QueryPersonEnum;
import com.tcvcog.tcvce.entities.search.QueryProperty;
import com.tcvcog.tcvce.entities.search.QueryPropertyEnum;
import com.tcvcog.tcvce.entities.search.SearchParamsProperty;
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

    private OccPermitApplication currentApplication;

    private Municipality selectedMuni;
    private Date form_dateOfRecord;

    private boolean multiUnit;
    private String houseNum;
    private String streetName;
    private PublicInfoBundleProperty selectedProperty;
    private List<PublicInfoBundleProperty> propList;
    private PublicInfoBundleProperty prop;

    private PublicInfoBundlePropertyUnit selectedUnit;
    private PublicInfoBundlePropertyUnit unitToAdd;
    private List<PublicInfoBundlePropertyUnit> workingPropUnits;
    private String unitNum;
    private boolean rental;
    private String unitNotes;

    private OccPermitApplicationReason selectedApplicationReason;
    private List<OccPermitApplicationReason> reasonList;

    private List<PublicInfoBundlePerson> attachedPersons;
    private Person applicationPerson;
    private PublicInfoBundlePerson applicant;
    private Person currentOwner;
    private Person newOwner;
    private PublicInfoBundlePerson contactPerson;
    private List<Person> newOccupants;

    private Person searchPerson;
    private List<PublicInfoBundlePerson> personSearchResults;

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
        PublicInfoCoordinator pic = getPublicInfoCoordinator();

        try {
            reasonList = opi.getOccPermitApplicationReasons();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }

        currentApplication = getSessionBean().getSessOccPermitApplication();

        searchPerson = new Person();

        // check if we're working with an internal user or the public form
        if (getSessionBean().getSessUser() != null) {

            // internal user procedure
            // TODO: Internal users should probably get their own BB. 
            //This BB has been stuffed will PublicInfoBundles to keep stuff anonymized
            /*
            selectedMuni = getSessionBean().getSessMuni();
            Property p = getSessionBean().getSessProperty;
            workingPropUnits = p.getUnitList();
             */
        } else { // start public user init procedure

            prop = getSessionBean().getOccPermitAppActiveProp();

            selectedMuni = getSessionBean().getSessMuniQueued();

            if (prop != null) {

                if (getSessionBean().getOccPermitAppWorkingProp() == null
                        || !getSessionBean().getOccPermitAppWorkingProp().getBundledProperty().getAddress().equalsIgnoreCase(prop.getBundledProperty().getAddress())) {

                    workingPropUnits = prop.getUnitList();

                    if (workingPropUnits == null) {

                        prop.setUnitList(new ArrayList<PublicInfoBundlePropertyUnit>());
                        workingPropUnits = new ArrayList<>();

                    } else {
                        try {

                            List<PropertyUnit> unbundledUnits = pri.getPropertyUnitList(prop.getBundledProperty());

                            workingPropUnits = new ArrayList<>();

                            for (PropertyUnit unit : unbundledUnits) {
                                workingPropUnits.add(pic.extractPublicInfo(unit));
                            }

                        } catch (IntegrationException | AuthorizationException
                                | BObStatusException | EventException
                                | SearchException ex) {

                            System.out.println(ex);
                        }
                    }

                    getSessionBean().setOccPermitAppWorkingProp(prop);
                } else {

                    workingPropUnits = getSessionBean().getOccPermitAppWorkingProp().getUnitList();

                }
            }

            if (attachedPersons == null && getSessionBean().getSessOccPermitApplication() != null) {

                attachedPersons = getSessionBean().getOccPermitAttachedPersons();

                if (attachedPersons == null) {
                    attachedPersons = new ArrayList();
                }
            }

        }

        try {
            requiredPersons = getSessionBean().getSessOccPermitApplication().getReason().getPersonsRequirement().getRequiredPersonTypes();

            optAndReqPersons = new ArrayList<>();

            optAndReqPersons.addAll(requiredPersons);

            optAndReqPersons.addAll(getSessionBean().getSessOccPermitApplication().getReason().getPersonsRequirement().getOptionalPersonTypes());
        } catch (NullPointerException ex) {
            //Do nothing. The try block simply acts as an optimized null pointer check.
            //requiredPersonTypes is so deep that otherwise there would have to be three if statements
            //to make this code safe
        }

    } // end postConstruct

    /* TODO: Move to internal Occ App BB
    public String beginInternalOccApp(PublicInfoBundlePropertyUnit pu) throws IntegrationException, BObStatusException {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        PropertyIntegrator pi = getPropertyIntegrator();
        PublicInfoCoordinator pic = getPublicInfoCoordinator();

        OccPermitApplication occpermitapp = oc.getNewOccPermitApplication();
        getSessionBean().setSessOccPermitApplication(occpermitapp);

        getSessionBean().setOccPermitAppActiveProp(pi.getProperty(getSessionBean().getSessProperty().getPropertyID()));

        if (prop.getUnitList().size() == 1) {
            List<PropertyUnit> propertyUnitList = prop.getUnitList();
            getSessionBean().setOccPermitAppActivePropUnit(propertyUnitList.get(0));
            getSessionBean().getSessOccPermitApplication().setApplicationPropertyUnit(pu);
        }

        return "beginInternalOccUpp";

    }*/
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
        getSessionBean().setSessMuniQueued(selectedMuni);
        OccupancyCoordinator oc = getOccupancyCoordinator();
        OccPermitApplication occpermitapp = oc.getNewOccPermitApplication();
        getSessionBean().setSessOccPermitApplication(occpermitapp);
        return "chooseProperty";
    }

    /**
     * Searches for a property within the activeMuni set on the SessionBean.
     */
    public void searchForPropertiesSingleMuni() {
        SearchCoordinator sc = getSearchCoordinator();
        UserCoordinator uc = getUserCoordinator();

        propList = new ArrayList<>();

        QueryProperty qp = null;

        try {
            qp = sc.initQuery(QueryPropertyEnum.HOUSESTREETNUM, uc.getPublicUserAuthorized().getMyCredential());

            if (qp != null && !qp.getParamsList().isEmpty()) {
                SearchParamsProperty spp = qp.getPrimaryParams();
                spp.setAddress_ctl(true);
                spp.setAddress_val(houseNum + " " + streetName);
                spp.setMuni_ctl(true);
                spp.setMuni_val(selectedMuni);
                spp.setLimitResultCount_ctl(true);
                spp.setLimitResultCount_val(20);

                sc.runQuery(qp);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Your search completed with " + getPropList().size() + " results", ""));
            } else {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Something when wrong with the property search! Sorry!", ""));
            }

        } catch (IntegrationException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Something when wrong with the property search! Sorry!", ""));
        }

        if (qp != null && !qp.getBOBResultList().isEmpty()) {
            PublicInfoCoordinator pic = getPublicInfoCoordinator();

            List<Property> unbundled = qp.getBOBResultList();
            propList = new ArrayList<>();

            for (Property p : unbundled) {
                try {
                    propList.add(pic.extractPublicInfo(p));
                } catch (AuthorizationException | BObStatusException
                        | EventException | IntegrationException
                        | SearchException ex) {
                    System.out.print("OccPermitApplicationBB.searchForPropertiesSingleMuni() | ERROR: " + ex);
                }
            }

        }
    }

    /*
     * For multiunit properties: Gets the list of property units. For
     * non-multiunit properties: Sets the activePropertyUnit to default unit.
     *
     * Its functionality has been moved to the
     *
     * deprecated public void onPropertySelection() { PropertyCoordinator pc =
     * getPropertyCoordinator();
     *
     * getSessionBean().setOccPermitAppActiveProp(prop);
     *
     * if (prop.getUnitList().size() == 1) { List<PropertyUnit> propertyUnitList
     * = prop.getUnitList();
     * getSessionBean().setOccPermitAppActivePropUnit(propertyUnitList.get(0));
     * getSessionBean().getSessOccPermitApplication().setApplicationPropertyUnit(propertyUnitList.get(0));
     * } }
     */
    /**
     * Adds a blank unit to propUnitsToAdd list. This newly-created unit can
     * then be selected and edited by the user.
     */
    public void addUnitToNewPropUnits() {
        PropertyCoordinator pc = getPropertyCoordinator();
        PublicInfoCoordinator pic = getPublicInfoCoordinator();
        PropertyUnit newUnit = pc.initPropertyUnit(selectedProperty.getBundledProperty());
        newUnit.setUnitNumber("");
//        newUnit.setRental(false);
        newUnit.setNotes("");

        try {
            unitToAdd = pic.extractPublicInfo(newUnit);
        } catch (AuthorizationException | BObStatusException | EventException | IntegrationException | SearchException ex) {
            System.out.print("OccPermitApplicationBB.addUnitToNewPropUnits() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occured while trying to add a new unit! Please try again.", ""));
        }

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
    public void removeSelectedUnit(PublicInfoBundlePropertyUnit selectedUnit) {
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

        if (selectedProperty != null) {
// TODO: occbeta
//        PropertyCoordinator pc = getPropertyCoordinator();

//            if (getSessionBean().getOccPermitApplication().isMultiUnit() == true) {
//                try {
//                    propWithLists = pc.getPropertyUnitsWithoutDefault(selectedProperty);
//                } catch (BObStatusException ex) {
//                    System.out.println(ex);
//                }
//            } else {
//                try {
//                    propWithLists = pc.getPropertyDataHeavy(selectedProperty);
//                } catch (BObStatusException ex) {
//                    System.out.println(ex);
//                }
//            }
            getSessionBean().setOccPermitAppActiveProp(prop);

            if (prop.getUnitList().size() == 1) {
                PublicInfoCoordinator pic = getPublicInfoCoordinator();

                List<PublicInfoBundlePropertyUnit> propertyUnitList = prop.getUnitList();
                getSessionBean().setOccPermitAppActivePropUnit(propertyUnitList.get(0));

                try {

                    PropertyUnit p = pic.export(propertyUnitList.get(0));

                    getSessionBean().getSessOccPermitApplication().setApplicationPropertyUnit(p);

                } catch (AuthorizationException | BObStatusException | EventException | IntegrationException | SearchException ex) {
                    System.out.print("OccPermitApplicationBB.selectProperty() | ERROR: " + ex);
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "An error occured while trying to select a property! Please try again.", ""));
                    return "";
                }

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
    public String selectPropertyUnit(PublicInfoBundlePropertyUnit unit) {

        PublicInfoCoordinator pic = getPublicInfoCoordinator();

        unit.getBundledUnit().setPropertyID(getSessionBean().getOccPermitAppActiveProp().getBundledProperty().getPropertyID());
        getSessionBean().setOccPermitAppActivePropUnit(unit);

        try {

            PropertyUnit p = pic.export(unit);

            getSessionBean().getSessOccPermitApplication().setApplicationPropertyUnit(p);

        } catch (AuthorizationException | BObStatusException | EventException | IntegrationException | SearchException ex) {
            System.out.print("OccPermitApplicationBB.selectPropertyUnit() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occured while trying to select a property Unit! Please try again.", ""));
            return "";
        }

        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();

            ec.redirect("/tcvce/public/services/occPermitApplicationFlow/occPermitAddReason.xhtml#currentStep");
        } catch (IOException ex) {
        }

        return "addReason";
    }

    /**
     * Finalizes the unit list the user has created so that it can be compared
     * to the existing one in the database.
     *
     * @return
     */
    public String finalizeUnitList() {

        PublicInfoCoordinator pic = getPublicInfoCoordinator();
        try {
            workingPropUnits = pic.sanitizePublicPropertyUnitList(workingPropUnits);
        } catch (BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.toString(), ""));
            return "";
        }
//          getSessionBean().getOccPermitApplication().setMultiUnit(workingPropUnits.size() > 1); //if there is more than one unit on the workingPropUnits list, set it to multiunit.
        getSessionBean().getOccPermitAppWorkingProp().setUnitList(workingPropUnits);
        getSessionBean().getOccPermitAppActiveProp().setUnitList(workingPropUnits); //This line is different from the original method (above)

        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();

            ec.redirect("/tcvce/public/services/occPermitApplicationFlow/occPermitSelectForApply.xhtml#currentStep");
        } catch (IOException ex) {
        }

        return "selectForApply";

    }

    /**
     * Checks that the user has not selected a multiunit property without also
     * selecting a property unit. Sends user to occPermitAddReason.xhtml
     *
     * @return
     */
    public String storePropertyUnitInfo() {
        if (getSessionBean().getSessOccPermitApplication().getApplicationPropertyUnit() == null
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

    public void searchForPersons() {

        UserCoordinator uc = getUserCoordinator();
        SearchCoordinator sc = getSearchCoordinator();

        QueryPerson qp = null;

        try {

            qp = sc.initQuery(QueryPersonEnum.PERSON_NAME, uc.getPublicUserAuthorized().getMyCredential());

            if (qp != null && !qp.getParamsList().isEmpty()) {
                SearchParamsPerson spp = qp.getPrimaryParams();
                spp.setName_last_ctl(true);
                spp.setName_last_val(searchPerson.getLastName());
                spp.setName_first_ctl(true);
                spp.setName_first_val(searchPerson.getFirstName());
                spp.setEmail_ctl(true);
                spp.setEmail_val(searchPerson.getEmail());
                spp.setPhoneNumber_ctl(true);
                spp.setPhoneNumber_val(searchPerson.getPhoneCell());
                spp.setAddress_streetNum_ctl(true);
                spp.setAddress_streetNum_val(searchPerson.getAddressStreet());
                sc.runQuery(qp);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Your search completed with " + personSearchResults.size() + " results", ""));
            } else {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Something when wrong with the person search! Sorry!", ""));
            }

        } catch (IntegrationException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Something when wrong with the person search! Sorry!", ""));
        }

        if (qp != null && !qp.getBOBResultList().isEmpty()) {

            PublicInfoCoordinator pic = getPublicInfoCoordinator();
            List<Person> skeletonHorde = qp.getBOBResultList();
            personSearchResults = new ArrayList<>();

            for (Person skeleton : skeletonHorde) {
                personSearchResults.add(pic.extractPublicInfo(skeleton));
            }

        }

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

    public void addPersonToApplication(PublicInfoBundlePerson person) {

        boolean duplicateFlag = false;

        person.getBundledPerson().setPersonType(PersonType.Other);

        for (PublicInfoBundlePerson test : attachedPersons) {

            if (test.getBundledPerson().getPersonID() == person.getBundledPerson().getPersonID()) {

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

    public void removePersonFromApplication(PublicInfoBundlePerson person) {
        attachedPersons.remove(person);
    }

    /*
    Currently not used by the interface
    public String editPersonInfo(Person person) {
        person.setPersonType(getSessionBean().getOccPermitAppActivePersonType());
        attachedPersons = getSessionBean().getSessOccPermitApplication().getAttachedPersons();

        if (attachedPersons == null) {
            attachedPersons = new ArrayList();
        }
        getSessionBean().getSessOccPermitApplication().setAttachedPersons(attachedPersons);
        getSessionBean().setActiveAnonPerson(person);
        return "editPerson";
    }
     */
    public String addANewPerson() {

//         for (Person p : attachedPersons) {
// TODO: occbeta: Add a "set as applicant" button on each row holding a person. It will simply overwrite the current applicant.
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
//    }
        attachedPersons.add(new PublicInfoBundlePerson());

        return "";
    }

    public List<String> getPersonRequirementDescription() {

        List<PersonType> required = requiredPersons;

        List<PersonType> optional = getSessionBean().getSessOccPermitApplication().getReason().getPersonsRequirement().getOptionalPersonTypes();

        StringBuilder description = new StringBuilder("It is required that you have these types of people: ");

        List<String> descList = new ArrayList<>();

        for (PersonType type : required) {

            description.append(type.getLabel()).append(", ");

        }

        description.deleteCharAt(description.lastIndexOf(","));

        descList.add(description.toString());

        description = new StringBuilder();

        description.append("You may also add these types of people: ");

        for (PersonType type : optional) {

            description.append(type.getLabel()).append(", ");

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

        PublicInfoCoordinator pic = getPublicInfoCoordinator();

        List<Integer> countTypes = new ArrayList();

        for (int index = 0; index < 17; index++) {

            countTypes.add(0);

        }
        for (PublicInfoBundlePerson p : attachedPersons) {

            int index = p.getBundledPerson().getPersonType().ordinal();

            int temp = countTypes.get(index) + 1;

            countTypes.set(index, temp);

//            TODO: occbeta
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

        getSessionBean().setOccPermitApplicant(applicant);

        getSessionBean().setOccPermitAttachedPersons(attachedPersons);

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
            int applicationId = opi.insertOccPermitApplicationAndReturnId(getSessionBean().getSessOccPermitApplication());
            getSessionBean().getSessOccPermitApplication().setId(applicationId);
            opi.insertOccPeriodPersons(getSessionBean().getSessOccPermitApplication());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }

        return redir;
    }

    public void submitUnitChangeList() {

        List<PropertyUnitChangeOrder> changeList = new ArrayList<>();

        List<PropertyUnit> currentUnitList = new ArrayList<>();

        PropertyIntegrator pri = getPropertyIntegrator();

        PublicInfoCoordinator pic = getPublicInfoCoordinator();

        PropertyDataHeavy existingProp = new PropertyDataHeavy();

        Person changedby = getSessionBean().getSessOccPermitApplication().getApplicantPerson();

//         TODO: Occbeta
//        try {
//            existingProp = pri.getPropertyDataHeavy(prop.getPropertyID());
//                    
//        } catch (IntegrationException | BObStatusException | EventException | AuthorizationException ex) {
//            System.out.println(ex);
//        }
        for (PublicInfoBundlePropertyUnit bundle : workingPropUnits) {
            try {
                currentUnitList.add(pic.export(bundle));
            } catch (AuthorizationException | BObStatusException | EventException | IntegrationException | SearchException ex) {
                System.out.println("OccPermitApplicationBB.submitUnitChangeList() | ERROR: " + ex);
            }

        }

        for (PropertyUnit workingUnit : currentUnitList) {

            PropertyUnitChangeOrder skeleton = new PropertyUnitChangeOrder();

            boolean added = true;

            skeleton.setPropertyID(getSessionBean().getSessOccPermitApplication().getApplicationPropertyUnit().getPropertyID());

            if (changedby.getPersonID() != 0) {

                PersonIntegrator pi = getPersonIntegrator();

                Person temp = new Person();

                try {
                    temp = pi.getPerson(changedby.getPersonID());
                } catch (IntegrationException ex) {
                    System.out.println(ex);
                }

                String changeName = temp.getFirstName() + " " + temp.getLastName() + " ID: " + temp.getPersonID();

                skeleton.setChangedBy(changeName);

            } else {

                skeleton.setChangedBy(changedby.getFirstName() + " " + changedby.getLastName());

            }

            for (PropertyUnit activeUnit : existingProp.getUnitList()) {

                if (workingUnit.getUnitID() == activeUnit.getUnitID() && workingUnit.getUnitID() != 0) {

                    added = false;

                    skeleton.setUnitID(workingUnit.getUnitID());

                    if (workingUnit.getUnitNumber() != null) {

                        skeleton.setUnitNumber(workingUnit.getUnitNumber());

                    }

                    if (workingUnit.getOtherKnownAddress() != null && workingUnit.getOtherKnownAddress().compareToIgnoreCase(activeUnit.getOtherKnownAddress()) != 0) {

                        skeleton.setOtherKnownAddress(workingUnit.getOtherKnownAddress());

                    }

                    if (workingUnit.getNotes().compareToIgnoreCase(activeUnit.getNotes()) != 0) {

                        skeleton.setNotes(workingUnit.getNotes());

                    }

                    if (workingUnit.getRentalNotes().compareToIgnoreCase(activeUnit.getRentalNotes()) != 0) {

                        skeleton.setRentalNotes(workingUnit.getRentalNotes());

                    }
                }

            }

            if (added == true) {

                skeleton.setUnitNumber(workingUnit.getUnitNumber());

                skeleton.setOtherKnownAddress(workingUnit.getOtherKnownAddress());

                skeleton.setNotes(workingUnit.getNotes());

                skeleton.setRentalNotes(workingUnit.getRentalNotes());
            }

            skeleton.setAdded(added);

            if (skeleton.changedOccured()) {

                changeList.add(skeleton);
            }

        }

        for (PropertyUnit activeUnit : existingProp.getUnitList()) {

            PropertyUnitChangeOrder skeleton = new PropertyUnitChangeOrder();

            skeleton.setPropertyID(getSessionBean().getSessOccPermitApplication().getApplicationPropertyUnit().getPropertyID());

            if (changedby.getPersonID() != 0) {

                PersonIntegrator pi = getPersonIntegrator();

                Person temp = new Person();

                try {
                    temp = pi.getPerson(changedby.getPersonID());
                } catch (IntegrationException ex) {
                    System.out.println(ex);
                }

                String changeName = temp.getFirstName() + " " + temp.getLastName() + " ID: " + temp.getPersonID();

                skeleton.setChangedBy(changeName);
            } else {

                skeleton.setChangedBy(changedby.getFirstName() + " " + changedby.getLastName());

            }

            boolean removed = true;

            for (PropertyUnit workingUnit : currentUnitList) {

                if (workingUnit.getUnitID() == activeUnit.getUnitID()) {

                    removed = false;

                }

            }

            if (removed == true) {

                skeleton.setUnitID(activeUnit.getUnitID());

                skeleton.setUnitNumber(activeUnit.getUnitNumber());

                skeleton.setOtherKnownAddress(activeUnit.getOtherKnownAddress());

                skeleton.setNotes(activeUnit.getNotes());

                skeleton.setRentalNotes(activeUnit.getRentalNotes());

                skeleton.setRemoved(removed);

                changeList.add(skeleton);

            }

        }

        for (PropertyUnitChangeOrder order : changeList) {

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

    public List<PublicInfoBundleProperty> getPropList() {
        return propList;
    }

    public void setPropList(List<PublicInfoBundleProperty> propList) {
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
    public PublicInfoBundlePerson getContactPerson() {
        return contactPerson;
    }

    /**
     * @param contactPerson the contactPerson to set
     */
    public void setContactPerson(PublicInfoBundlePerson contactPerson) {
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
    public PublicInfoBundleProperty getProp() {
        return prop;
    }

    /**
     * @param p the propWithLists to set
     */
    public void setProp(PublicInfoBundleProperty p) {
        this.prop = p;
    }

    /**
     * @return the selectedProperty
     */
    public PublicInfoBundleProperty getSelectedProperty() {
        return selectedProperty;
    }

    /**
     * @param selectedProperty the selectedProperty to set
     */
    public void setSelectedProperty(PublicInfoBundleProperty selectedProperty) {
        this.selectedProperty = selectedProperty;
    }

    /**
     * @return the selectedUnit
     */
    public PublicInfoBundlePropertyUnit getSelectedUnit() {
        return selectedUnit;
    }

    /**
     * @param selectedUnit the selectedUnit to set
     */
    public void setSelectedUnit(PublicInfoBundlePropertyUnit selectedUnit) {
        this.selectedUnit = selectedUnit;
    }

    /**
     * @return the unitToAdd
     */
    public PublicInfoBundlePropertyUnit getUnitToAdd() {
        return unitToAdd;
    }

    /**
     * @param unitToAdd the unitToAdd to set
     */
    public void setUnitToAdd(PublicInfoBundlePropertyUnit unitToAdd) {
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
    public List<PublicInfoBundlePropertyUnit> getWorkingPropUnits() {
        return workingPropUnits;
    }

    /**
     * @param propUnitsToAdd the propUnitsToAdd to set
     */
    public void setWorkingPropUnits(List<PublicInfoBundlePropertyUnit> propUnitsToAdd) {
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

    public PublicInfoBundlePerson getApplicant() {
        return applicant;
    }

    public void setApplicant(PublicInfoBundlePerson applicant) {
        this.applicant = applicant;
    }

    public List<PublicInfoBundlePerson> getAttachedPersons() {
        return attachedPersons;
    }

    public void setAttachedPersons(List<PublicInfoBundlePerson> attachedPersons) {
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

    public OccPermitApplication getCurrentApplication() {
        return currentApplication;
    }

    public void setCurrentApplication(OccPermitApplication currentApplication) {
        this.currentApplication = currentApplication;
    }

    public Person getSearchPerson() {
        return searchPerson;
    }

    public void setSearchPerson(Person searchPerson) {
        this.searchPerson = searchPerson;
    }

    public List<PublicInfoBundlePerson> getPersonSearchResults() {
        return personSearchResults;
    }

    public void setPersonSearchResults(List<PublicInfoBundlePerson> personSearchResults) {
        this.personSearchResults = personSearchResults;
    }

}
