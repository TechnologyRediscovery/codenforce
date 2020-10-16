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
import com.tcvcog.tcvce.domain.NavigationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonChangeOrder;
import com.tcvcog.tcvce.entities.PersonOccApplication;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyUnitChangeOrder;
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
import java.util.Iterator;
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
    private List<PropertyUnit> newUnitList; // Stores the unit list for use in refresh refreshUnitsAndPersons()

    private OccPermitApplicationReason selectedApplicationReason;
    private List<OccPermitApplicationReason> reasonList;

    private List<PublicInfoBundlePerson> attachedPersons;
    private Person applicationPerson;
    private PublicInfoBundlePerson applicant;
    private Person currentOwner;
    private Person newOwner;
    private PublicInfoBundlePerson contactPerson;

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

        try {
            reasonList = opi.getOccPermitApplicationReasons();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }

        currentApplication = getSessionBean().getSessOccPermitApplication();

        searchPerson = new Person();

        // check if we're working with an internal user or the public form
        // internal user procedure
        // TODO: Internal users should probably get their own BB. 
        //This BB has been stuffed will PublicInfoBundles to keep stuff anonymized
        /*
            selectedMuni = getSessionBean().getSessMuni();
            Property p = getSessionBean().getSessProperty;
            workingPropUnits = p.getUnitList();
         */
        prop = getSessionBean().getOccPermitAppActiveProp();

        selectedMuni = getSessionBean().getSessMuniQueued();

        if (prop != null) {

            //If there is no working prop or if the working prop's address is not equal to the active prop's address
            if (getSessionBean().getOccPermitAppWorkingProp() == null
                    || !getSessionBean().getOccPermitAppWorkingProp().getBundledProperty().getAddress().equalsIgnoreCase(prop.getBundledProperty().getAddress())) {

                if (prop.getUnitList() == null) {

                    prop.setUnitList(new ArrayList<PublicInfoBundlePropertyUnit>());
                    workingPropUnits = new ArrayList<>();

                } else {

                    workingPropUnits = prop.getUnitList();

                }

                getSessionBean().setOccPermitAppWorkingProp(prop);
            } else {

                workingPropUnits = getSessionBean().getOccPermitAppWorkingProp().getUnitList();

                selectedUnit = getSessionBean().getOccPermitAppActivePropUnit();
            }
        }

        attachedPersons = getSessionBean().getOccPermitAttachedPersons();

        applicant = getSessionBean().getOccPermitApplicant();

        contactPerson = getSessionBean().getOccPermitPreferredContact();

        if (attachedPersons == null) {
            attachedPersons = new ArrayList();
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

    public String goBack() {
        try {
            return getSessionBean().getNavStack().popLastPage();
        } catch (NavigationException ex) {
            System.out.println("OccPermitApplication.goBack() | ERROR: " + ex);
            //We must do things a little bit different here to make sure messages are kept after the redirect.
            FacesContext context = getFacesContext();
                    context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Something went wrong when we tried to direct you back a page!", ""));
                    context.getExternalContext().getFlash().setKeepMessages(true);
            return "occPermitApplicationFlow";
        }
    }

    /* TODO: Move to internal Occ App BB
    public String beginInternalOccApp(PublicInfoBundlePropertyUnit pu) throws IntegrationException, BObStatusException {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        PropertyIntegrator pi = getPropertyIntegrator();
        PublicInfoCoordinator pic = getPublicInfoCoordinator();

        OccPermitApplication occpermitapp = oc.initOccPermitApplication();
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
        try{
        OccPermitApplication occpermitapp = oc.initOccPermitApplication(selectedMuni.getMuniCode());
         getSessionBean().setSessOccPermitApplication(occpermitapp);
        getSessionBean().getNavStack().pushCurrentPage();
        return "chooseProperty";
        } catch(IntegrationException ex){
            System.out.println("OccPermitApplicationBB.recordSelectedMuni() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occurred while trying to record ", ""));
            return "";
        }
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
            qp = sc.initQuery(QueryPropertyEnum.HOUSESTREETNUM, uc.auth_getPublicUserAuthorized().getMyCredential());

            if (qp != null && !qp.getParamsList().isEmpty()) {
                SearchParamsProperty spp = qp.getPrimaryParams();
                spp.setAddress_ctl(true);
                spp.setAddress_val(houseNum + " " + streetName);
                spp.setMuni_ctl(true);
                spp.setMuni_val(selectedMuni);
                spp.setLimitResultCount_ctl(true);
                spp.setLimitResultCount_val(20);

                sc.runQuery(qp);
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

            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Your search completed with " + getPropList().size() + " results", ""));

        }
    }

    /**
     * Adds a blank unit to propUnitsToAdd list. This newly-created unit can
     * then be selected and edited by the user.
     */
    public void addUnitToNewPropUnits() {
        PropertyCoordinator pc = getPropertyCoordinator();
        PublicInfoCoordinator pic = getPublicInfoCoordinator();
        PropertyUnit newUnit = pc.initPropertyUnit(prop.getBundledProperty());
        newUnit.setUnitNumber("");
        newUnit.setRentalNotes("");
        newUnit.setNotes("");

        try {
            unitToAdd = pic.extractPublicInfo(newUnit);
        } catch (AuthorizationException | BObStatusException | EventException | IntegrationException | SearchException ex) {
            System.out.println("OccPermitApplicationBB.addUnitToNewPropUnits() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occured while trying to add a new unit! Please try again.", ""));
        }

        if (workingPropUnits == null) {
            workingPropUnits = new ArrayList<>();
        }

        workingPropUnits.add(unitToAdd);
    }

    /**
     * Removes a newly-created unit from propUnitsToAdd list.
     *
     * @param selectedUnit
     */
    public void removeSelectedUnit(PublicInfoBundlePropertyUnit selectedUnit) {
        workingPropUnits.remove(selectedUnit);
    }

    /**
     * Sets the activePropWithLists according to the property the user has
     * selected, so they can then configure its units.
     *
     * @return
     */
    public String selectProperty() {

        if (selectedProperty == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a property.", ""));
            return "";
        }

        //We must remove all units that are not active
        Iterator itr = selectedProperty.getUnitList().iterator();

        while (itr.hasNext()) {
            PublicInfoBundlePropertyUnit temp = (PublicInfoBundlePropertyUnit) itr.next();
            if (!temp.getBundledUnit().isActive()) {
                itr.remove();
            }
        }

        getSessionBean().setOccPermitAppActiveProp(selectedProperty);

        if (selectedProperty.getUnitList().size() == 1) {
            getSessionBean().setOccPermitAppActivePropUnit(selectedProperty.getUnitList().get(0));

        }

        getSessionBean().getNavStack().pushCurrentPage();

        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.redirect("/tcvce/public/services/occPermitApplicationFlow/occPermitAddPropertyUnit.xhtml#currentStep");
        } catch (IOException ex) {
        }

        return "addPropertyUnit";
    }

    /**
     * Sets a property unit on the SessionBean and the OccPermitApplication,
     * sends user to occPermitAddReason.xhtml
     *
     * @param unit
     * @return
     */
    public String selectPropertyUnit(PublicInfoBundlePropertyUnit unit) {

        unit.getBundledUnit().setPropertyID(getSessionBean().getOccPermitAppActiveProp().getBundledProperty().getPropertyID());
        getSessionBean().setOccPermitAppActivePropUnit(unit);
        getSessionBean().getNavStack().pushCurrentPage();

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
        getSessionBean().getOccPermitAppWorkingProp().setUnitList(workingPropUnits);
        getSessionBean().getOccPermitAppActiveProp().setUnitList(workingPropUnits);
        getSessionBean().getNavStack().pushCurrentPage();

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
     * Sets the currentApplication on the sessionBean and then sends user to
     * personsRequirementManage.xhtml
     *
     * @return
     */
    public String storeReason() {

        if (currentApplication.getReason() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a reason from the drop-down list.", ""));
            return "";
        }

        getSessionBean().setSessOccPermitApplication(currentApplication);
        getSessionBean().getNavStack().pushCurrentPage();

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

            qp = sc.initQuery(QueryPersonEnum.PERSON_NAME, uc.auth_getPublicUserAuthorized().getMyCredential());

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

            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Your search completed with " + personSearchResults.size() + " results", ""));

        }

    }

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

    public String addANewPerson() {

        PublicInfoBundlePerson skeleton = new PublicInfoBundlePerson();
        skeleton.setBundledPerson(new Person());

        attachedPersons.add(skeleton);

        return "";
    }

    public String getApplicantName() {

        if (applicant != null && applicant.getBundledPerson() != null) {
            return applicant.getBundledPerson().getFirstName() + " " + applicant.getBundledPerson().getLastName();
        } else {
            return "UNSELECTED";
        }

    }

    public String getContactName() {

        //The contact defaults to the Applicant if the applicant doesn't choose one.
        //So supply the applicant's name first.
        if (contactPerson == null && applicant != null) {
            return applicant.getBundledPerson().getFirstName() + " " + applicant.getBundledPerson().getLastName();
        } else if (contactPerson != null && contactPerson.getBundledPerson() != null) {
            return contactPerson.getBundledPerson().getFirstName() + " " + contactPerson.getBundledPerson().getLastName();
        } else {
            return "UNSELECTED";
        }

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

        description.append("Also, please identify yourself by clicking the \"Set As Applicant\" button in the row with your name.");

        descList.add(description.toString());

        return descList;

    }

    public String reviewApplication() {

        OccupancyCoordinator oc = getOccupancyCoordinator();
        List<PersonOccApplication> unbundledPersons = new ArrayList<>();

        //An application we'll use to test if the person requirements have been met.
        OccPermitApplication temp = new OccPermitApplication();

        for (PublicInfoBundlePerson p : attachedPersons) {
            unbundledPersons.add(new PersonOccApplication(p.getBundledPerson()));
        }

        temp.setAttachedPersons(unbundledPersons);

        if (applicant == null || applicant.getBundledPerson() == null) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please specify an applicant.", ""));
            return "";
        }

        temp.setApplicantPerson(applicant.getBundledPerson());

        temp.setReason(getSessionBean().getSessOccPermitApplication().getReason());

        try {
            oc.verifyOccPermitPersonsRequirement(temp);
        } catch (BObStatusException ex) {
            //The user has made an error, let's tell them about it.
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.toString(), ""));
            return "";
        }
        getSessionBean().setOccPermitApplicant(applicant);

        getSessionBean().setOccPermitPreferredContact(contactPerson);

        getSessionBean().setOccPermitAttachedPersons(attachedPersons);
        getSessionBean().getNavStack().pushCurrentPage();

        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();

            ec.redirect("/tcvce/public/services/occPermitApplicationFlow/reviewAndSubmit.xhtml");
        } catch (IOException ex) {
        }

        return "reviewApplication";
    }

    public String submitApplication(String redir) {

        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {

            //so we'll have person records for the export methods to grab
            //Also handles attaching valid person objects to the application
            submitPersonChangeList();

            //This method not only sends a list of unit changes to the database.
            //But also attaches the selected unit to the application.
            submitUnitChangeList();

            oc.insertOccPermitApplication(currentApplication);

            getSessionBean().setSessOccPermitApplication(currentApplication);
            
        } catch (IntegrationException | AuthorizationException | EventException | SearchException ex) {
            System.out.println("OccPermitApplicationBB.submitApplication() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Something when wrong while submitting your application! Please contact us using the link in the side bar and report your error!", ""));
            return "";
        } catch (BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Something when wrong while submitting your application: " + ex, ""));
            return "";
        }

        if (redir.contentEquals("selectForApply")) {

            //We are returning to the unit list to apply for occupancy on more units
            //so let's set the unit and person lists we just inserted into the database onto the sess bean
            //so we don't have to insert them again.
            refreshUnitsAndPersons();

            while (!getSessionBean().getNavStack().peekLastPage().contains("occPermitAddPropertyUnit.xhtml")) { //Clear the navstack until we reach occPermitAddPropertyUnit.xhtml
                try {
                    getSessionBean().getNavStack().popLastPage();
                } catch (NavigationException ex) {
                    //nothing, we just wanted to clear the stack anyway.
                }
            }

        } else {
            while (getSessionBean().getNavStack().peekLastPage() != null) { //Clear the navstack completely
                try {
                    getSessionBean().getNavStack().popLastPage();
                } catch (NavigationException ex) {
                    //nothing, we just wanted to clear the stack anyway.
                }
            }
        }

        return redir;
    }

    public void submitUnitChangeList()
            throws IntegrationException,
            BObStatusException,
            EventException,
            AuthorizationException,
            SearchException {
        PropertyIntegrator pri = getPropertyIntegrator();
        PropertyCoordinator pc = getPropertyCoordinator();
        PublicInfoCoordinator pic = getPublicInfoCoordinator();

        List<PropertyUnitChangeOrder> changeList = new ArrayList<>();

        newUnitList = new ArrayList<>();

        int changedbyID = applicant.getBundledPerson().getPersonID();
        
        //Grab the unit list that is currently attached to the property in the database
        Property existingProp = pc.getProperty(prop.getBundledProperty().getPropertyID());

        //Export the workingPropUnits list from PublicInfoBundles to units. This should preserve changes made by the user.
        for (PublicInfoBundlePropertyUnit bundle : workingPropUnits) {
            newUnitList.add(pic.export(bundle));
        }

        for (PropertyUnit workingUnit : newUnitList) {

            //Intialize change order so it's ready to receive edits.
            PropertyUnitChangeOrder skeleton = new PropertyUnitChangeOrder();

            boolean added = true;

            //Done preparing change order, let's actually start comparing the workingUnit (the on the user made) to units currently in the database.
            for (PropertyUnit existingUnit : existingProp.getUnitList()) {
                if (workingUnit.getUnitID() == existingUnit.getUnitID()) {

                    //this block fires if the unit ID already exists in our database. The unit is an existing one that may have been changed
                    added = false;

                    skeleton = new PropertyUnitChangeOrder(existingUnit, workingUnit); //This constructor will compare the two.

                    break; //for optimization

                }
            }

            if (added == true) {

                workingUnit.setPropertyID(existingProp.getPropertyID());

                workingUnit.setUnitID(pri.insertPropertyUnit(workingUnit));

                //This unit doesn't exist in our database. Save all of its fields so it can be saved to the database
                skeleton = new PropertyUnitChangeOrder(workingUnit);
            }

            skeleton.setAdded(added);

            //if a change has occured, then we will add it to the changeList that we must submit at the end of this method.
            if (skeleton.changedOccured()) {

                changeList.add(skeleton);
            }

            if (workingUnit.getUnitNumber().equals(selectedUnit.getBundledUnit().getUnitNumber())) {
                currentApplication.setApplicationPropertyUnit(workingUnit);//To make sure the application gets linked to this unit
            }

        }

        //We checked for changes and added units. We will now check for removed units
        for (PropertyUnit activeUnit : existingProp.getUnitList()) {

            PropertyUnitChangeOrder skeleton = new PropertyUnitChangeOrder();

            boolean removed = true;

            for (PropertyUnit workingUnit : newUnitList) {

                //If the unit is in the working list or if they weren't active to begin with,
                //then don't add them to the change orders.
                if (workingUnit.getUnitID() == activeUnit.getUnitID() || !activeUnit.isActive()) {

                    removed = false;
                    
                    break;

                }

            }
 
            if (removed == true) {

                boolean duplicate = false;
                
                //We need to make sure there are no active change ordes that ask for removal. If the
                for(PropertyUnitChangeOrder change : pri.getPropertyUnitChangeList(activeUnit.getUnitID())){
                    
                    if(change.isRemoved() && change.getChangedBy() == changedbyID){
                        //We already inserted a remove on this unit. No need to do it again.
                        duplicate = true;
                        
                        break;
                    }
                    
                }
                
                if(!duplicate){
                //Only the unitID and removed flag are needed to deactivate a unit from the database
                skeleton.setUnitID(activeUnit.getUnitID());

                skeleton.setRemoved(removed);

                changeList.add(skeleton);
                }
            }

        }

        //We are done getting change orders. It's time to get them ready for the database and insert them.
        for (PropertyUnitChangeOrder order : changeList) {

            //save who made this change order
            order.setChangedBy(changedbyID);

            //Finally, insert the order
            pri.insertPropertyUnitChange(order);

        }

        System.out.println("end of submitting unit change list");
    }

    public void submitPersonChangeList() throws IntegrationException {
        PersonIntegrator pi = getPersonIntegrator();
        PublicInfoCoordinator pic = getPublicInfoCoordinator();

        List<PersonChangeOrder> changeList = new ArrayList<>();

        List<PersonOccApplication> currentPersonList = new ArrayList<>();

        List<Person> existingPersonList = new ArrayList<>();

        //Before we can save change orders and such, there's a bit of a dependency caveat we need to address
        //We need to set who changed these people, but the applicant might have just been added as well!
        //we'll try and grab the originals from the database.
        //If that fails, we'll insert the person into the database
        Person changedby = pic.export(applicant);

        if (changedby.getPersonID() == 0) {
            //Getting from the database failed. We'll have to insert it.
            changedby.setMuniCode(getSessionBean().getSessMuniQueued().getMuniCode());
            changedby.setPersonID(pi.insertPerson(changedby));
        }

        //Let's set the applicant on the current application.
        currentApplication.setApplicantPerson(changedby);

        Person contactUnbundled = null;
        //We also need to make sure the preferred contact is in the database

        if (contactPerson != null) {

            contactUnbundled = pic.export(contactPerson);

            if (contactUnbundled.getPersonID() == 0) {
                //Getting from the database failed. We'll have to insert it.
                contactUnbundled.setMuniCode(getSessionBean().getSessMuniQueued().getMuniCode());

                contactUnbundled.setPersonID(pi.insertPerson(contactUnbundled));
            }
        } else {
            //As it turns out, the contact person is null! Let's set it equal to the applicant
            contactUnbundled = changedby;
        }

        //Let's set the preferred contact on the application
        currentApplication.setPreferredContact(contactUnbundled);

        //Export the workingPropUnits list from PublicInfoBundles to PersonOccPeriods. This should preserve changes made by the user.
        for (PublicInfoBundlePerson bundle : attachedPersons) {
            currentPersonList.add(new PersonOccApplication(pic.export(bundle))); //This must be a PersonOccApplication in order to store the person type data in the correct field.
        }

        //Grab the persons that are currently in the database
        for (Person p : currentPersonList) {
            existingPersonList.add(pi.getPerson(p.getPersonID()));
        }

        for (PersonOccApplication workingPerson : currentPersonList) {

            //Intialize change order so it's ready to receive edits.
            PersonChangeOrder skeleton = new PersonChangeOrder();

            boolean added = true;

            //Let's start comparing the workingPerson (the on the user made) to Persons currently in the database.
            //Using iterator so we can remove null entries.
            Iterator itr = existingPersonList.iterator();

            while (itr.hasNext()) {
                Person existingPerson = (Person) itr.next();

                if (existingPerson == null) {
                    //they don't exist in the database. No need to keep checking.
                    //Let's remove this entry from the list for performance and safety
                    itr.remove();
                } else if (workingPerson.getPersonID() == existingPerson.getPersonID()) {

                    //this block fires if the person ID already exists in our database. The person is an existing one that may have been changed
                    added = false;

                    skeleton = new PersonChangeOrder(existingPerson, workingPerson); //This constructor will compare the two.

                    break; //for optimization

                }
            }

            if (added == true) {

                //This person was not found in the database. They might be our applicant or preferred contact, 
                //so let's compare the two.
                skeleton = new PersonChangeOrder(changedby, workingPerson);

                if (!skeleton.changedOccured()) {
                    //there were no differences between the applicant and the person we're looking at right now.
                    //It's the applicant, so let's set the ID to reflect that
                    workingPerson.setPersonID(changedby.getPersonID());
                } else {

                    //It wasn't the applicant, so let's try the preferred contact
                    skeleton = new PersonChangeOrder(contactUnbundled, workingPerson);

                    if (!skeleton.changedOccured()) {
                        //there were no differences between the preferred contact and the person we're looking at right now.
                        //It's the preferred contact, so let's set the ID to reflect that
                        workingPerson.setPersonID(contactUnbundled.getPersonID());
                    }
                }
                //This person does not exist in our database.
                //insert it into the database and then create a change order
                workingPerson.setMuniCode(getSessionBean().getSessMuniQueued().getMuniCode());

                workingPerson.setPersonID(pi.insertPerson(workingPerson));

                skeleton = new PersonChangeOrder(workingPerson);

                pi.insertPersonChangeOrder(skeleton);

            }

            skeleton.setAdded(added);

            //if a change has occured, then we will add it to the changeList that we must submit at the end of this method.
            if (skeleton.changedOccured()) {
                changeList.add(skeleton);
            }
        }

        //We are done getting change orders. It's time to get them ready for the database and insert them.
        for (PersonChangeOrder order : changeList) {

            order.setChangedBy(changedby.getPersonID());

            pi.insertPersonChangeOrder(order);

        }

        //Set the persons on the application so they'll be there when we insert the person links.
        currentApplication.setAttachedPersons(currentPersonList);

        System.out.println("end of submitting person change list");
    }

    public void refreshUnitsAndPersons() {
        PublicInfoCoordinator pic = getPublicInfoCoordinator();

        //First let's get a new list of persons and transport over the user's changes
        List<PublicInfoBundlePerson> bundledPersons = new ArrayList<>();

        for (Person donor : currentApplication.getAttachedPersons()) {

            PublicInfoBundlePerson bundle = pic.extractPublicInfo(new Person(donor));

            bundle.getBundledPerson().setFirstName(donor.getFirstName());

            bundle.getBundledPerson().setLastName(donor.getLastName());

            bundle.getBundledPerson().setPersonType(donor.getPersonType());

            bundle.getBundledPerson().setPhoneCell(donor.getPhoneCell());

            bundle.getBundledPerson().setPhoneHome(donor.getPhoneHome());

            bundle.getBundledPerson().setPhoneWork(donor.getPhoneWork());

            bundle.getBundledPerson().setEmail(donor.getEmail());

            bundle.getBundledPerson().setAddressStreet(donor.getAddressStreet());

            bundle.getBundledPerson().setAddressCity(donor.getAddressCity());

            bundle.getBundledPerson().setAddressState(donor.getAddressState());

            bundle.getBundledPerson().setAddressZip(donor.getAddressZip());

            bundle.getBundledPerson().setBusinessEntity(donor.isBusinessEntity());

            bundle.getBundledPerson().setUnder18(donor.isUnder18());

            bundle.getBundledPerson().setUseSeparateMailingAddress(donor.isUseSeparateMailingAddress());

            bundle.getBundledPerson().setMailingAddressStreet(donor.getMailingAddressStreet());

            bundle.getBundledPerson().setMailingAddressThirdLine(donor.getMailingAddressThirdLine());

            bundle.getBundledPerson().setMailingAddressCity(donor.getMailingAddressCity());

            bundle.getBundledPerson().setMailingAddressState(donor.getMailingAddressState());

            bundle.getBundledPerson().setMailingAddressZip(donor.getMailingAddressZip());

            bundledPersons.add(bundle);

        }

        getSessionBean().setOccPermitAttachedPersons(bundledPersons);

        //Now, let's refresh the unit list.
        List<PublicInfoBundlePropertyUnit> bundledUnits = new ArrayList<>();

        for (PropertyUnit donor : newUnitList) {

            try {

                PublicInfoBundlePropertyUnit bundle = pic.extractPublicInfo(new PropertyUnit(donor));

                bundle.getBundledUnit().setUnitNumber(donor.getUnitNumber());

                bundle.getBundledUnit().setRentalNotes(donor.getRentalNotes());

                bundle.getBundledUnit().setNotes(donor.getNotes());

                bundledUnits.add(bundle);

            } catch (IntegrationException | AuthorizationException
                    | BObStatusException | EventException | SearchException ex) {
                System.out.println("OccPermitApplicationBB.refreshUnitsAndPersons() | ERROR: " + ex);

                //Oh no it failed, let's just directly put the donor in.
                //Some info will be lost but not the ID, which is more important
                PublicInfoBundlePropertyUnit bundle = new PublicInfoBundlePropertyUnit();

                bundle.setBundledUnit(donor);

                bundledUnits.add(bundle);
            }
        }

        getSessionBean().getOccPermitAppActiveProp().setUnitList(bundledUnits);

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
