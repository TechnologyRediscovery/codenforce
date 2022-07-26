/*
 * Copyright (C) 2017 Technology Rediscovery LLC.
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
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.*;
import com.tcvcog.tcvce.domain.*;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.entities.occupancy.OccPermitApplication;
import com.tcvcog.tcvce.entities.occupancy.OccPermitApplicationReason;
import com.tcvcog.tcvce.entities.search.*;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 *
 * @author Dominic Pimpinella, NADGIT, and Sylvia
 *
 */
public class OccPermitApplicationBB extends BackingBeanUtils implements Serializable {

    private OccPermitApplication currentApplication;

    private Municipality selectedMuni;
    
    //Two fields used by the address search
    private String houseNum;
    private String streetName;
    
    private PublicInfoBundleProperty selectedProperty; //The property that contains the unit(s) the user is applying for occupancy
    private List<PublicInfoBundleProperty> propList;

    private PublicInfoBundlePropertyUnit selectedUnit; //The unit the user is currently applying for occupancy
    
    /*
        workingPropUnits stores a copy of the unit list that is attached to a property.
        The user can edit and work on this copy to their hearts' content.
        After they are done, we will compare this list to the original in the database
        and send their changes to be reviewed by a code enforcement officer.
    */
    private List<PublicInfoBundlePropertyUnit> workingPropUnits;
    
    //stores which units the user has already submitted an application for
    private Map<String,PublicInfoBundlePropertyUnit> alreadyApplied;
    
    private List<PropertyUnit> newUnitList; // Stores the unit list for use in refreshUnitsAndPersons()

    //All possible reasons the user may select for an occupancy permit.
    private List<OccPermitApplicationReason> reasonList;

    //list of all persons attached to the occPermitApplication, including the 
    //applicant and preferred contact
    private List<PublicInfoBundlePerson> attachedPersons;
    
    //This field stores a copy of the applicant. The original is in the 
    //attachedPersons list.
    private PublicInfoBundlePerson applicant; 
   
    //This field stores a copy of the preferred contact. The original is in the 
    //attachedPersons list.
    private PublicInfoBundlePerson contactPerson;

    //When we search for a person, we use the fields in searchPerson to hold parameters
    //e.g. when a user wants to search for a person named "John Smith"
    //"John" will be stored in searchPerson.firstName and "Smith" in searchPerson.lastNamed
    private Person searchPerson;
    private List<PublicInfoBundlePerson> personSearchResults; //stores the search results

    //stores only the types of persons that are required for the given reason for application
    private List<PersonType> requiredPersons;
    
    //stores both the required types and the optional types given the selected reasons
    private List<PersonType> optAndReqPersons;

    /**
     * Creates a new instance of OccPermitApplicationBB
     */
    public OccPermitApplicationBB() {
    }

    @PostConstruct
    public void initBean() {
        //Load the application the user is filling out
        currentApplication = getSessionBean().getSessOccPermitApplication();
        
        /***********************************
             INITIALIZE INTERFACE FIELDS
        ************************************/
        try {
            OccupancyIntegrator opi = getOccupancyIntegrator();
            
            reasonList = opi.getOccPermitApplicationReasons();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
//  ----->  TODO: Update for Humanization/Parcelization <------
        // this was changed from new Person() to null
        searchPerson = null;
        
        /***********************************
              LOAD PROPERTY AND MUNI
        ************************************/
        
        // check if we're working with an internal user or the public form
        // internal user procedure
        // TODO: Internal users should probably get their own BB. 
        //This BB has been stuffed will PublicInfoBundles to keep stuff anonymized
        /*
            selectedMuni = getSessionBean().getSessMuni();
            Property p = getSessionBean().getSessProperty;
            workingPropUnits = p.getUnitList();
         */
        selectedProperty = getSessionBean().getOccPermitAppActiveProp();

        selectedMuni = getSessionBean().getSessMuniQueued();
        
        alreadyApplied = getSessionBean().getOccPermitAlreadyApplied();
        
        if(alreadyApplied == null){
            alreadyApplied = new HashMap<>();
        }
        
        if (selectedProperty != null) {

            //Default to an empty list, though we might switch it out below
            workingPropUnits = new ArrayList<>(); 
            
            //If there is no working prop or if the working prop's address is not equal to the active prop's address
            if (getSessionBean().getOccPermitAppWorkingProp() == null
                    || !getSessionBean().getOccPermitAppWorkingProp().getBundledProperty().getAddressString().equalsIgnoreCase(selectedProperty.getBundledProperty().getAddressString())) {

                if (selectedProperty.getUnitList() == null) {

                    selectedProperty.setUnitList(new ArrayList<PublicInfoBundlePropertyUnit>());

                } else {

                    workingPropUnits = selectedProperty.getUnitList();

                }

                getSessionBean().setOccPermitAppWorkingProp(selectedProperty);
            } else {

                workingPropUnits = getSessionBean().getOccPermitAppWorkingProp().getUnitList();

                selectedUnit = getSessionBean().getOccPermitAppActivePropUnit();
            }
        }
        
        /***********************************
                    LOAD PERSONS
        ************************************/
        
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

    /**
     * Return to the previous step of the application process
     * @return 
     */
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

        getSessionBean().setOccPermitAppActiveProp(pi.getProperty(getSessionBean().getSessProperty().getParcelKey()));

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
        
        try{
            OccupancyCoordinator oc = getOccupancyCoordinator();
            OccPermitApplication occpermitapp = oc.initOccPermitApplication(selectedMuni.getMuniCode());
            getSessionBean().setSessOccPermitApplication(occpermitapp);
            getSessionBean().getNavStack().pushCurrentPage();
            return "chooseProperty";
        } catch(IntegrationException ex){
            System.out.println("OccPermitApplicationBB.recordSelectedMuni() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occurred while trying to record municipality selection", ""));
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
            qp = sc.initQuery(QueryPropertyEnum.ADDRESS_BLDG_NUM_ONLY, uc.auth_getPublicUserAuthorized().getMyCredential());

            if (qp != null && !qp.getParamsList().isEmpty()) {
                SearchParamsProperty spp = qp.getPrimaryParams();
                spp.setAddress_bldgNum_ctl(true);
                spp.setAddress_bldgNum_val(houseNum + " " + streetName);
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

        } catch (IntegrationException | SearchException | BObStatusException ex) {
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
        PropertyUnit newUnit = pc.getPropertyUnitSkeleton(selectedProperty.getBundledProperty());
        
        try {
            workingPropUnits.add(pic.extractPublicInfo(newUnit));
        } catch (AuthorizationException | BObStatusException | EventException | IntegrationException | SearchException ex) {
            System.out.println("OccPermitApplicationBB.addUnitToNewPropUnits() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occured while trying to add a new unit! Please try again.", ""));
        }
        
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

        getSessionBean().getNavStack().pushCurrentPage();

        return "addPropertyUnit";
    }

    /**
     * Sets a property unit on the SessionBean and the OccPermitApplication,
     * sends user to occPermit4AddReason.xhtml
     *
     * @param unit
     * @return
     */
    public String selectPropertyUnit(PublicInfoBundlePropertyUnit unit) {

        unit.getBundledUnit().setParcelKey(getSessionBean().getOccPermitAppActiveProp().getBundledProperty().getParcelKey());
        getSessionBean().setOccPermitAppActivePropUnit(unit);
        getSessionBean().getNavStack().pushCurrentPage();
        
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

        return "selectForApply";

    }

    /**
     * Gets a list of reasons for an occupancy permit application for the interface.
     *
     * @return
     */
    public List<OccPermitApplicationReason> getReasonList() {
        return reasonList;
    }

    /**
     * Sets the currentApplication on the sessionBean and then sends user to
     * occPermit5PersonsRequirementManage.xhtml
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
                spp.setName_ctl(true);
                spp.setName_val(searchPerson.getName());
                spp.setEmail_ctl(true);
                spp.setEmail_val(searchPerson.getEmail());
                spp.setPhoneNumber_ctl(true);
                //  ----->  TODO: Update for Humanization/Parcelization <------
//                spp.setPhoneNumber_val(searchPerson.getPhoneCell());
//                spp.setAddress_streetNum_ctl(true);
//                spp.setAddress_streetNum_val(searchPerson.getAddressStreet());
                sc.runQuery(qp);
            } else {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Something when wrong with the person search! Sorry!", ""));
            }

        } catch (IntegrationException | SearchException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Something when wrong with the person search! Sorry!", ""));
        }

        if (qp != null && !qp.getBOBResultList().isEmpty()) {

            PublicInfoCoordinator pic = getPublicInfoCoordinator();
            //  ----->  TODO: Update for Humanization/Parcelization <------
//            List<Person> skeletonHorde = qp.getBOBResultList();
            personSearchResults = new ArrayList<>();
//
//            for (Person skeleton : skeletonHorde) {
//                personSearchResults.add(pic.extractPublicInfo(skeleton));
//            }

            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Your search completed with " + personSearchResults.size() + " results", ""));

        }

    }

    /**
     * Adds a person from the person search results to the attachedPersons list.
     * @param person 
     */
    public void addPersonToApplication(PublicInfoBundlePerson person) {

        boolean duplicateFlag = false;

        //Set their type to Other, as we're not concerned with their overall type
        //The system might say they're law enforcement, owner, etc.
        //But in an occupancy context, they could be a tenant, manager, etc.
        
//  ----->  TODO: Update for Humanization/Parcelization <------
//        person.getBundledPerson().setPersonType(PersonType.Other);

        for (PublicInfoBundlePerson test : attachedPersons) {

            if (test.getBundledPerson().getHumanID() == person.getBundledPerson().getHumanID()) {

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

    /**
     * Adds a blank person to the attachedPersons list so the user may fill 
     * in their information.
     * @return 
     */
    public String addANewPerson() {

        PublicInfoBundlePerson skeleton = new PublicInfoBundlePerson();
        //  ----->  TODO: Update for Humanization/Parcelization <------
//        skeleton.setBundledPerson(new Person());

        attachedPersons.add(skeleton);

        return "";
    }

    /**
     * A method used by the interface to prevent users from submitting two applications
     * for the same unit.
     * @param unitNumber
     * @return 
     */
    public boolean alreadyAppliedFor(String unitNumber){
        return alreadyApplied.containsKey(unitNumber);
    }
    
    /**
     * Method used by the interface to display who is currently selected as the applicant.
     * @return 
     */
    public String getApplicantName() {

        try{
            return applicant.getBundledPerson().getFirstName() + " " + applicant.getBundledPerson().getLastName();
        } catch(NullPointerException ex) {
            //The applicant must be null, we haven't selected them yet
            return "UNSELECTED";
        }

    }

    /**
     * Method used by the interface to display who is currently selected as the 
     * preferred contact.
     * @return 
     */
    public String getContactName() {

        //The contact defaults to the Applicant if the applicant doesn't choose one.
        //So try to grab the contact's name, and if that fails try to
        //get the applicant name
        try{
            return contactPerson.getBundledPerson().getFirstName() + " " + contactPerson.getBundledPerson().getLastName();
        } catch(NullPointerException ex){
            return getApplicantName();
        }

    }

    /**
     * A method that generates a description for the user of what types of 
     * people are required for their application
     * @return 
     */
    public List<String> getPersonRequirementDescription() {
        StringBuilder description = new StringBuilder("It is required that you have these types of people: ");
        List<String> descList = new ArrayList<>();

        //Make description for required people (if the list is valid)
        if (requiredPersons != null) {

            for (PersonType type : requiredPersons) {

                description.append(type.getLabel()).append(", ");

            }

            //Delete the last comma
            int lastComma = description.lastIndexOf(",");
            if (lastComma > -1) {
                description.deleteCharAt(description.lastIndexOf(","));        
            }

            descList.add(description.toString());
            description = new StringBuilder();

        }

        //Make description for optional people (if there are any)
        List<PersonType> optional = getSessionBean().getSessOccPermitApplication().getReason().getPersonsRequirement().getOptionalPersonTypes();
        if (optional.size() > 0) {
            description.append("You may also add these types of people: ");

            for (PersonType type : optional) {
                description.append(type.getLabel()).append(", ");

            }

            //Delete the last comma
            int lastComma = description.lastIndexOf(",");
            if (lastComma > -1) {
                description.deleteCharAt(description.lastIndexOf(","));        
            }
            
            descList.add(description.toString());
            description = new StringBuilder();
        }

        //Tell the user how to identify themselves

        description.append("Also, please identify yourself by clicking the \"Set As Applicant\" button in the row with your name.");

        descList.add(description.toString());

        return descList;

    }

    /**
     * Checks to see if the person list is valid, and then moves on to present
     * the entire application for review
     * @return 
     */
    public String reviewApplication() {

        //First make sure they have an applicant selected
        if (applicant == null || applicant.getBundledPerson() == null) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please specify an applicant.", ""));
            return "";
        }
        
        //An application we'll use to test if the person requirements have been met.
        OccPermitApplication temp = new OccPermitApplication();

        //  ----->  TODO: Update for Humanization/Parcelization <------
        List<HumanLink> unbundledPersons = new ArrayList<>();
        
        for (PublicInfoBundlePerson p : attachedPersons) {
            //  ----->  TODO: Update for Humanization/Parcelization <------
            // SHouldn't be making a new human link here--ask coordinator
//            unbundledPersons.add(new HumanLink(p.getBundledPerson()));
        }

        temp.setAttachedPersons(unbundledPersons);

        temp.setApplicantPerson(applicant.getBundledPerson());

        temp.setReason(getSessionBean().getSessOccPermitApplication().getReason());

        try {
            OccupancyCoordinator oc = getOccupancyCoordinator();
            
            oc.verifyOccPermitPersonsRequirement(temp);
        } catch (BObStatusException ex) {
            //The user has made an error, let's tell them about it.
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.toString(), ""));
            return "";
        }
        
        //The application looks good, let's set all of their selected persons
        //on the session bean
        
        getSessionBean().setOccPermitApplicant(applicant);

        getSessionBean().setOccPermitPreferredContact(contactPerson);

        getSessionBean().setOccPermitAttachedPersons(attachedPersons);
        getSessionBean().getNavStack().pushCurrentPage();

        return "reviewApplication";
    }

    /**
     * A method that not only submits the OccApplication, but also the change lists
     * for persons and units.
     * When it is finished, it redirects a user either to the unit list, so they 
     * may continue filling out more occupancy permits, or to the front page when
     * they have no more applications to submit.
     * @param redir
     * @return 
     */
    public String submitApplication(String redir) {

        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {

            //so we'll have person records for the export methods to grab
            //Also handles attaching valid person objects to the application
            submitPersonChangeList();

            //This method not only sends a list of unit changes to the database.
            //But also attaches the selected unit to the application.
            submitUnitChangeList();

            oc.insertOccPermitApplication(currentApplication, getSessionBean().getSessUser());

            getSessionBean().setSessOccPermitApplication(currentApplication);
            
        } catch (IntegrationException | AuthorizationException | EventException | SearchException | BlobException ex) {
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

        String clearUntil = "|"; //a character no URL in our system would have
        
        if (redir.contentEquals("selectForApply")) {

            //We are returning to the unit list to apply for occupancy on more units
            //so let's set the unit and person lists we just inserted into the database onto the sess bean
            //so we don't have to insert them again.
            refreshUnitsAndPersons();

            //We want to clear until this page
            clearUntil = "occPermit2AddPropertyUnit.xhtml";

        }

//        //Clear the navstack until we reach the specified page
//        //If clearUntil is |, we will loop until there are no more pages to clear
//        while (!getSessionBean().getNavStack().peekLastPage().contains(clearUntil)) {
//            try {
//                getSessionBean().getNavStack().popLastPage();
//            } catch (NavigationException ex) {
//                //nothing, we just wanted to clear the stack anyway.
//            }
//        }

        //Mark down that they've already applied for this unit
        alreadyApplied.put(currentApplication.getApplicationPropertyUnit().getUnitNumber(), selectedUnit);
        
        getSessionBean().setOccPermitAlreadyApplied(alreadyApplied);
        
        return redir;
    }

    /**
     * Finds which units the user has changed, converts these changes into
     * change orders, and sends them to the database for review by a code
     * officer.
     * @throws IntegrationException
     * @throws BObStatusException
     * @throws EventException
     * @throws AuthorizationException
     * @throws SearchException 
     */
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

        int changedbyID = applicant.getBundledPerson().getHumanID();
        
        //Grab the unit list that is currently attached to the property in the database
        Property existingProp = pc.getProperty(selectedProperty.getBundledProperty().getParcelKey());

        //Export the workingPropUnits list from PublicInfoBundles to units. This should preserve changes made by the user.
        for (PublicInfoBundlePropertyUnit bundle : workingPropUnits) {
            newUnitList.add(pic.export(bundle));
        }

        for (PropertyUnit workingUnit : newUnitList) {

            //Intialize change order so it's ready to receive edits.
            PropertyUnitChangeOrder skeleton = new PropertyUnitChangeOrder();

            skeleton.setAdded(true); //We will assume it has been added until determined otherwise.

            //Done preparing change order, let's actually start comparing the workingUnit (the one the user made) to units currently in the database.
            for (PropertyUnit existingUnit : existingProp.getUnitList()) {
                if (workingUnit.getUnitID() == existingUnit.getUnitID()) {

                    //this block fires if the unit ID already exists in our database.
                    //The unit is an existing one that may have been changed
                    skeleton.setAdded(false);

                    skeleton = new PropertyUnitChangeOrder(existingUnit, workingUnit); //This constructor will compare the two.

                    break; //for optimization

                }
            }

            if (skeleton.isAdded()) {

                //This unit doesn't exist in our database. Save all of its fields so it can be saved to the database
                
                workingUnit.setParcelKey(existingProp.getParcelKey());

                workingUnit.setUnitID(pri.insertPropertyUnit(workingUnit));

                //Still add a change order so a user with restricted access can review it
                skeleton = new PropertyUnitChangeOrder(workingUnit);
            }

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
            
            skeleton.setRemoved(true); //We will assume it has been removed until determined otherwise.

            for (PropertyUnit workingUnit : newUnitList) {

                //If the unit is in the working list or if they weren't active to begin with,
                //then don't add them to the change orders.
                if (workingUnit.getUnitID() == activeUnit.getUnitID() || !activeUnit.isActive()) {

                    skeleton.setRemoved(false);
                    
                    break;

                }

            }
 
            if (skeleton.isRemoved()) {

                boolean duplicate = false;
                
                //We need to make sure there are no active change orders that ask for removal. If the
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

    /**
     * Finds which persons the user has changed, converts these changes into
     * change orders, and sends them to the database for review by a code
     * officer.
     * This method also handles inserting the applicant in the database if they
     * aren't there already.
     * @throws IntegrationException 
     */
    public void submitPersonChangeList() throws IntegrationException {
        PersonIntegrator pi = getPersonIntegrator();
        PublicInfoCoordinator pic = getPublicInfoCoordinator();

        List<PersonChangeOrder> changeList = new ArrayList<>();

        List<HumanLink> currentPersonList = new ArrayList<>();

        List<Person> existingPersonList = new ArrayList<>();

        //Before we can save change orders and such, there's a bit of a dependency caveat we need to address
        //We need to set who changed these people, but the applicant might have just been added as well!
        //we'll try and grab the originals from the database.
        //If that fails, we'll insert the person into the database
        Person changedby = pic.export(applicant);

        if (changedby.getHumanID() == 0) {
            
            //  ----->  TODO: Update for Humanization/Parcelization <------
            //Getting from the database failed. We'll have to insert it.
//            changedby.setMuniCode(getSessionBean().getSessMuniQueued().getMuniCode());
//            changedby.setPersonID(pi.insertPerson(changedby));
        }

        //Let's set the applicant on the current application.
        currentApplication.setApplicantPerson(changedby);

        //The preferredContact defaults to the applicant.
        Person contactUnbundled = changedby;

        if (contactPerson != null) {

            //Hold your horses! The user has specified a preferredContact other 
            //than the applicant. Let's see if they are in the database
            contactUnbundled = pic.export(contactPerson);

            if (contactUnbundled.getHumanID() == 0) {
                //Getting from the database failed. We'll have to insert it.
                //  ----->  TODO: Update for Humanization/Parcelization <------
//                contactUnbundled.setMuniCode(getSessionBean().getSessMuniQueued().getMuniCode());

//                contactUnbundled.setPersonID(pi.insertPerson(contactUnbundled));
            }
        }

        //Let's set the preferred contact on the application
        currentApplication.setPreferredContact(contactUnbundled);

        //Export the workingPropUnits list from PublicInfoBundles to PersonOccPeriods. This should preserve changes made by the user.
        for (PublicInfoBundlePerson bundle : attachedPersons) {
            //  ----->  TODO: Update for Humanization/Parcelization <------
//            currentPersonList.add(new OccApplicationHumanLink(pic.export(bundle))); //This must be a OccApplicationHumanLink in order to store the person type data in the correct field.
        }

        //Grab the persons that are currently in the database
        //  ----->  TODO: Update for Humanization/Parcelization <------
//        for (Person p : currentPersonList) {
//            existingPersonList.add(pi.getPerson(p.getHumanID()));
//        }

        for (HumanLink workingPerson : currentPersonList) {

            //Intialize change order so it's ready to receive edits.
            PersonChangeOrder skeleton = new PersonChangeOrder();
            
            skeleton.setAdded(true); //We will assume it has been added until proven otherwise.

            //Let's start comparing the workingPerson (the one the user made) to Persons currently in the database.
            //Using iterator so we can remove null entries.
            Iterator itr = existingPersonList.iterator();

            while (itr.hasNext()) {
                Person existingPerson = (Person) itr.next();

                if (existingPerson == null) {
                    //they don't exist in the database. No need to keep checking.
                    //Let's remove this entry from the list for performance and safety
                    itr.remove();
                } else if (workingPerson.getHumanID() == existingPerson.getHumanID()) {

                    //this block fires if the person ID already exists in our database. The person is an existing one that may have been changed
                    skeleton.setAdded(false);

                    //  ----->  TODO: Update for Humanization/Parcelization <------
//                    skeleton = new PersonChangeOrder(existingPerson, workingPerson); //This constructor will compare the two.

                    break; //for optimization

                }
            }

            if (skeleton.isAdded()) {

                //This person was not found in the database. They might be our applicant or preferred contact, 
                //so let's compare the two.
                //  ----->  TODO: Update for Humanization/Parcelization <------
//                skeleton = new PersonChangeOrder(changedby, workingPerson);

                if (!skeleton.changedOccured()) {
                    //there were no differences between the applicant and the person we're looking at right now.
                    //It's the applicant, so let's set the ID to reflect that
                    //  ----->  TODO: Update for Humanization/Parcelization <------
//                    workingPerson.setPersonID(changedby.getHumanID());
                } else {

                    //It wasn't the applicant, so let's try the preferred contact
                    //  ----->  TODO: Update for Humanization/Parcelization <------
//                    skeleton = new PersonChangeOrder(contactUnbundled, workingPerson);

                    if (!skeleton.changedOccured()) {
                        //there were no differences between the preferred contact and the person we're looking at right now.
                        //It's the preferred contact, so let's set the ID to reflect that
                        //  ----->  TODO: Update for Humanization/Parcelization <------
//                        workingPerson.setPersonID(contactUnbundled.getHumanID());
                    } else {
                    
                        //This person does not exist in our database
                        //And it's not the applicant or contact
                        //which we inserted at the beginning of this metho
                        //insert it into the database
                        //  ----->  TODO: Update for Humanization/Parcelization <------
//                        workingPerson.setMuniCode(getSessionBean().getSessMuniQueued().getMuniCode());

//                        workingPerson.setPersonID(pi.insertPerson(workingPerson));
                        
                    }
                       
                }
                
                //Create a change order and insert it into the database.
                //  ----->  TODO: Update for Humanization/Parcelization <------
//                skeleton = new PersonChangeOrder(workingPerson);

            }
            
            //if a change has occured, then we will add it to the changeList that we must submit at the end of this method.
            if (skeleton.changedOccured()) {
                changeList.add(skeleton);
            }
        }

        //We are done getting change orders. It's time to get them ready for the database and insert them.
        for (PersonChangeOrder order : changeList) {

            order.setChangedBy(changedby.getHumanID());

            pi.insertPersonChangeOrder(order);

        }

        //Set the persons on the application so they'll be there when we insert the person links.
        currentApplication.setAttachedPersons(currentPersonList);

        System.out.println("end of submitting person change list");
    }

    /**
     * After inserting change orders for units and persons, we need to make sure
     * we update the unit and persons lists with the new data we got from 
     * inserting them into the database, while also preserving the user's changes.
     */
    public void refreshUnitsAndPersons() {
        PublicInfoCoordinator pic = getPublicInfoCoordinator();

        //First let's get a new list of persons and transport over the user's changes
        List<PublicInfoBundlePerson> bundledPersons = new ArrayList<>();
        
        //  ----->  TODO: Update for Humanization/Parcelization <------
//
//        for (Person donor : currentApplication.getAttachedPersons()) {
//
//            PublicInfoBundlePerson bundle = pic.extractPublicInfo(new Person(donor));
//
//            //extractPublicInfo() would erase alot of the user's changes. Let's manually bring them over
//            
//            bundle.getBundledPerson().setFirstName(donor.getFirstName());
//
//            bundle.getBundledPerson().setLastName(donor.getLastName());
//
//            bundle.getBundledPerson().setPersonType(donor.getPersonType());
//
//            bundle.getBundledPerson().setPhoneCell(donor.getPhoneCell());
//
//            bundle.getBundledPerson().setPhoneHome(donor.getPhoneHome());
//
//            bundle.getBundledPerson().setPhoneWork(donor.getPhoneWork());
//
//            bundle.getBundledPerson().setEmail(donor.getEmail());
//
//            bundle.getBundledPerson().setAddressStreet(donor.getAddressStreet());
//
//            bundle.getBundledPerson().setAddressCity(donor.getAddressCity());
//
//            bundle.getBundledPerson().setAddressState(donor.getAddressState());
//
//            bundle.getBundledPerson().setAddressZip(donor.getAddressZip());
//
//            bundle.getBundledPerson().setBusinessEntity(donor.isBusinessEntity());
//
//            bundle.getBundledPerson().setUnder18(donor.isUnder18());
//
//            bundle.getBundledPerson().setUseSeparateMailingAddress(donor.isUseSeparateMailingAddress());
//
//            bundle.getBundledPerson().setMailingAddressStreet(donor.getMailingAddressStreet());
//
//            bundle.getBundledPerson().setMailingAddressThirdLine(donor.getMailingAddressThirdLine());
//
//            bundle.getBundledPerson().setMailingAddressCity(donor.getMailingAddressCity());
//
//            bundle.getBundledPerson().setMailingAddressState(donor.getMailingAddressState());
//
//            bundle.getBundledPerson().setMailingAddressZip(donor.getMailingAddressZip());
//
//            bundledPersons.add(bundle);

//        }

//        getSessionBean().setOccPermitAttachedPersons(bundledPersons);

        //Now, let's refresh the unit list.
        List<PublicInfoBundlePropertyUnit> bundledUnits = new ArrayList<>();

//        for (PropertyUnit donor : newUnitList) {
//
//            try {
//
//                PublicInfoBundlePropertyUnit bundle = pic.extractPublicInfo(new PropertyUnit(donor));
//
//                bundle.getBundledUnit().setUnitNumber(donor.getUnitNumber());
//
//                bundle.getBundledUnit().setRentalNotes(donor.getRentalNotes());
//
//                bundle.getBundledUnit().setNotes(donor.getNotes());
//
//                bundledUnits.add(bundle);
//
//            } catch (IntegrationException | AuthorizationException
//                    | BObStatusException | EventException | SearchException ex) {
//                System.out.println("OccPermitApplicationBB.refreshUnitsAndPersons() | ERROR: " + ex);
//
//                //Oh no it failed. Let's just directly put the donor in.
//                //Some info will be lost but not the ID, which is more important
//                PublicInfoBundlePropertyUnit bundle = new PublicInfoBundlePropertyUnit();
//
//                bundle.setBundledUnit(donor);
//
//                bundledUnits.add(bundle);
//            }
//        }
//
//        getSessionBean().getOccPermitAppActiveProp().setUnitList(bundledUnits);

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
     * @param reasonList the reasonList to set
     */
    public void setReasonList(List<OccPermitApplicationReason> reasonList) {
        this.reasonList = reasonList;
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