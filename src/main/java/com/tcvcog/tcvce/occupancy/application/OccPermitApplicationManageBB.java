/*
 * Copyright (C) 2020 Technology Rediscovery
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
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.InspectionException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.NavigationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonOccApplication;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.occupancy.OccApplicationStatusEnum;
import com.tcvcog.tcvce.entities.occupancy.OccPermitApplication;
import com.tcvcog.tcvce.entities.search.QueryPerson;
import com.tcvcog.tcvce.entities.search.QueryPersonEnum;
import com.tcvcog.tcvce.entities.search.SearchParamsPerson;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveListsEnum;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Nathan Dietz
 */
public class OccPermitApplicationManageBB extends BackingBeanUtils implements Serializable {

    private String currentMode;
    private boolean currentApplicationSelected;
    private boolean unitAlreadyDetermined; //used by occPermitNewPeriod.xhtml to flag whether or not the selected path has already determined a unit.
    private boolean disablePACCControl;
    private Property propertyForApplication;
    private List<ViewOptionsActiveListsEnum> allViewOptions;
    private ViewOptionsActiveListsEnum currentViewOption;

    private OccPermitApplication searchParams;
    private List<OccApplicationStatusEnum> statusList;
    private LocalDateTime queryBegin;
    private LocalDateTime queryEnd;
    private boolean reason_ctl;
    private boolean status_ctl;
    private boolean connectedOccPeriod_ctl;
    private boolean connectedOccPeriod_val;

    private Person searchPerson;
    private List<Person> personSearchResults;
    private Person applicant;
    private Person contactPerson;
    private List<PersonType> requiredPersons;
    private List<PersonType> optAndReqPersons;

    private String rejectedApplicationMessage;
    private String invalidApplicationMessage;
    private String acceptApplicationMessage;
    private OccApplicationStatusEnum newStatus;

    private OccPermitApplication selectedApplication;
//    private QueryOccPermitApplication selectedQueryOccApplicaton;
    private List<OccPermitApplication> applicationList;

    private List<PropertyUnit> unitList;
    private List<PersonOccApplication> attachedPersons;

    private String internalNoteText;
    private String externalNoteText;

    public OccPermitApplicationManageBB() {
    }

    @PostConstruct
    public void initBean() {

        //initialize default current mode : Lookup
        currentMode = "Search";

        searchParams = new OccPermitApplication();

        statusList = new ArrayList<>();

        statusList.addAll(Arrays.asList(OccApplicationStatusEnum.values()));

        selectedApplication = getSessionBean().getSessOccPermitApplication();

        unitAlreadyDetermined = getSessionBean().isUnitDetermined();

        searchPerson = new Person();

        try {
            PropertyCoordinator pc = getPropertyCoordinator();
            propertyForApplication = pc.getPropertyByPropUnitID(selectedApplication.getApplicationPropertyUnit().getUnitID());
        } catch (IntegrationException ex) {
            System.out.println("OccPermitManageBB.initBean() | ERROR: " + ex);
        } catch (NullPointerException ex) {
            //do nothing, this is just to check if anything is null in the method call above
        }

        allViewOptions = Arrays.asList(ViewOptionsActiveListsEnum.values());

        if (currentViewOption == null && propertyForApplication != null) {

            setCurrentViewOption(ViewOptionsActiveListsEnum.VIEW_ALL);

        }

        if (selectedApplication != null) {
            attachedPersons = selectedApplication.getAttachedPersons();

            try {
                requiredPersons = selectedApplication.getReason().getPersonsRequirement().getRequiredPersonTypes();

                optAndReqPersons = new ArrayList<>();

                optAndReqPersons.addAll(requiredPersons);

                optAndReqPersons.addAll(selectedApplication.getReason().getPersonsRequirement().getOptionalPersonTypes());
            } catch (NullPointerException ex) {
                //Do nothing. The try block simply acts as an optimized null pointer check.
                //requiredPersonTypes is so deep that otherwise there would have to be three if statements
                //to make this code safe
            }
        }
        //initialize default setting         
        defaultSetting();
    }

    /**
     * Determines whether or not a user should currently be able to select an
     * application. Users should only select CEARs if they're in search mode.
     *
     * @return
     */
    public boolean getSelectedButtonActive() {
        return !"Search".equals(currentMode);
    }

    public boolean getActiveSearchMode() {
        return "Search".equals(currentMode);
    }

    public boolean getActiveActionsMode() {
        return "Actions".equals(currentMode);
    }

    public boolean getActiveObjectsMode() {
        return "Objects".equals(currentMode);
    }

    public boolean getActiveNotesMode() {
        return "Notes".equals(currentMode);
    }

    public String getCurrentMode() {
        return currentMode;
    }

    public void defaultSetting() {

        OccupancyIntegrator oi = getOccupancyIntegrator();

        currentApplicationSelected = false;

        try {
            applicationList = oi.getOccPermitApplicationList();

        } catch (BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error while retrieving Occupancy Permit Applications: " + ex, ""));
        } catch (AuthorizationException | EventException | IntegrationException | ViolationException ex) {
            System.out.println("OccPermitManageBB.defaultSetting() | ERROR: " + ex);
        }
    }

    /**
     *
     * @param currentMode Search, Actions, Object, Notes
     */
    public void setCurrentMode(String currentMode) {

        //store currentMode into tempCurMode as a temporary value, in case the currenMode equal null
        String tempCurMode = this.currentMode;
        //reset default setting every time the Mode has been selected 
//        defaultSetting();
        //check the currentMode == null or not
        if (currentMode == null) {
            this.currentMode = tempCurMode;
        } else {
            this.currentMode = currentMode;
        }
        //show the current mode in p:messages box
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, this.currentMode + " Mode Selected", ""));

    }

    public void executeCustomQuery() {

        OccupancyIntegrator oi = getOccupancyIntegrator();

        currentApplicationSelected = false;

        if (queryBegin == null || queryEnd == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please specify a start and end date when executing a custom query", ""));
            return;
        }

        try {
            List<OccPermitApplication> results = oi.getOccPermitApplicationList();

            Iterator itr = results.iterator();

            while (itr.hasNext()) {

                OccPermitApplication temp = (OccPermitApplication) itr.next();

                if (temp.getSubmissionDate().compareTo(queryBegin) < 0 || temp.getSubmissionDate().compareTo(queryEnd) > 0) {
                    itr.remove();
                } else if (reason_ctl) {

                    if (temp.getReason().getId() != searchParams.getReason().getId()) {
                        itr.remove();
                    }

                } else if (status_ctl) {
                    if (temp.getStatus() != searchParams.getStatus()) {
                        itr.remove();
                    }
                } else if (connectedOccPeriod_ctl) {

                    //A variable that represents the result of a question we ask about an application.
                    //If the answer is different as the given search parameter, remove the application
                    boolean testCondition = temp.getConnectedPeriod() != null && temp.getConnectedPeriod().getPeriodID() != 0; //test if an occperiod is connected

                    if (testCondition != connectedOccPeriod_val) {
                        //if the test does not evaluate to our desired state, remove it
                        itr.remove();
                    }

                }

            }

            applicationList = results;

            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Your search completed with " + results.size() + " results!", ""));

        } catch (BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error while retrieving Occupancy Permit Applications: " + ex, ""));
        } catch (AuthorizationException | EventException | IntegrationException | ViolationException ex) {
            System.out.println("OccPermitManageBB.defaultSetting() | ERROR: " + ex);
        }

    }

    public void onApplicationSelection(OccPermitApplication application) {

        if (currentApplicationSelected) {
            selectedApplication = application;

            applicationList = new ArrayList<>();

            applicationList.add(application);

            attachedPersons = application.getAttachedPersons();

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Currently Selected Application: " + application.getReason().getTitle() + "ID:(" + application.getId() + ")", ""));
        } else {
            defaultSetting();

            attachedPersons = new ArrayList<>();

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Default Application: " + application.getReason().getTitle() + "ID:(" + application.getId() + ")", ""));

        }

    }

    public void changeApplicationStatus(ActionEvent ev) {
        System.out.println("updateStatus");

        if (newStatus == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a status before applying your changes.", ""));
        } else {
            updateSelectedPermitStatus(newStatus);
            defaultSetting();

        }

    }

    public String goToOccPeriod() {

        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            getSessionBean().setSessOccPeriod(
                    oc.assembleOccPeriodDataHeavy(
                            selectedApplication.getConnectedPeriod(),
                            getSessionBean().getSessUser().getMyCredential()));

            return "occPeriodWorkflow";
        } catch (BObStatusException | IntegrationException | SearchException ex) {
            System.out.println("OccPermitApplicationManageBB.goToOccPeriod() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occured while trying to redirect you to the Occ Period Workflow.", ""));
            return "";
        }

    }

    public void searchForPersons() {
        SearchCoordinator sc = getSearchCoordinator();

        QueryPerson qp = null;

        try {

            qp = sc.initQuery(QueryPersonEnum.PERSON_NAME, getSessionBean().getSessUser().getMyCredential());

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

        } catch (SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Something when wrong with the person search! Sorry!", ""));
        }

        if (qp != null && !qp.getBOBResultList().isEmpty()) {

            List<Person> skeletonHorde = qp.getBOBResultList();
            personSearchResults = new ArrayList<>();

            for (Person skeleton : skeletonHorde) {
                personSearchResults.add(skeleton);
            }

            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Your search completed with " + personSearchResults.size() + " results", ""));

        }

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

            attachedPersons.add(new PersonOccApplication(person));

        }
    }

    public void removePersonFromApplication(PersonOccApplication person) {
        attachedPersons.remove(person);
    }

    public String addANewPerson() {

        attachedPersons.add(new PersonOccApplication());

        return "";
    }

    public String getApplicantName() {

        if (applicant != null) {
            return applicant.getFirstName() + " " + applicant.getLastName();
        } else {
            return "UNSELECTED";
        }

    }

    public String getContactName() {

        //The contact defaults to the Applicant if the applicant doesn't choose one.
        //So supply the applicant's name first.
        if (contactPerson == null && applicant != null) {
            return applicant.getFirstName() + " " + applicant.getLastName();
        } else if (contactPerson != null) {
            return contactPerson.getFirstName() + " " + contactPerson.getLastName();
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

        description.append("Also, please identify the applicant by clicking the \"Set As Applicant\" button in the row with their name.");

        descList.add(description.toString());

        return descList;

    }

    /**
     * A method that changes only the selectedApplication's status.
     *
     * @param statusToSet
     */
    private void updateSelectedPermitStatus(OccApplicationStatusEnum statusToSet) {
        OccupancyIntegrator oi = getOccupancyIntegrator();

        try {
            OccPermitApplication app = oi.getOccPermitApplication(selectedApplication.getId());
            app.setStatus(statusToSet);
            oi.updateOccPermitApplication(app);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Status changed on application ID " + selectedApplication.getId(), ""));
            defaultSetting();
        } catch (IntegrationException | AuthorizationException
                | BObStatusException | EventException | ViolationException ex) {
            System.out.println("OccPermitManageBB.updateSelectedPermitStatus() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occured while trying to update application status.", ""));
        }
    }

    public void removeSelectedUnit(PropertyUnit unit) {
        if (unit.getUnitID() == 0) {
            propertyForApplication.getUnitList().remove(unit);
            setCurrentViewOption(currentViewOption);
        }
    }

    public void addNewUnit() {
        PropertyUnit newUnit = new PropertyUnit();
        newUnit.setPropertyID(propertyForApplication.getPropertyID());
        newUnit.setActive(true);
        propertyForApplication.getUnitList().add(newUnit);
        setCurrentViewOption(currentViewOption);
    }

    public String attachToOccPeriod() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        PropertyCoordinator pc = getPropertyCoordinator();

        if (acceptApplicationMessage == null || acceptApplicationMessage.contentEquals("")) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please fill out an acceptance message before attaching this permit to an Occ Period.", ""));
            return "";
        }

        try {

            if (!unitAlreadyDetermined) {

                //If we edited the unit list, let's save it to the database
                List<PropertyUnit> temp = pc.applyUnitList(unitList, propertyForApplication);

                for (PropertyUnit unit : temp) {
                    if (unit.getUnitNumber().contentEquals(selectedApplication.getApplicationPropertyUnit().getUnitNumber())) {
                        selectedApplication.setApplicationPropertyUnit(unit);
                    }
                }
            }

            int newPeriodID = oc.attachApplicationToNewOccPeriod(selectedApplication, acceptApplicationMessage);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Application successfully attached to Occ Period!", ""));

            selectedApplication.setConnectedPeriod(oc.getOccPeriod(newPeriodID));

            getSessionBean().setSessOccPermitApplication(selectedApplication);

            return getSessionBean().getNavStack().popLastPage();

        } catch (AuthorizationException | EventException | InspectionException | IntegrationException | ViolationException ex) {
            System.out.println("OccPermitManageBB.attachToOccPeriod() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occured while trying to attach the application to an Occ Period!", ""));
        } catch (BObStatusException ex) {
            System.out.println("OccPermitManageBB.attachToOccPeriod() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occured while trying to attach the application to an Occ Period: " + ex, ""));
        } catch (NavigationException ex) {
            System.out.println("OccPermitApplicationManageBB.attachToOccPeriod() | ERROR: " + ex);
            //We must do things a little bit different here to make sure messages are kept after the redirect.
            FacesContext context = getFacesContext();
                    context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occured while trying to direct you back to the page you were last on."
                            + " Your changes to the database were saved. Please return to the page manually.",
                            "Do not hit the return button again but note the error."));
                    context.getExternalContext().getFlash().setKeepMessages(true);
        }
        return ""; //only reached if an error was thrown.
    }

    public String cancelAttachment() {

        selectedApplication.setStatus(OccApplicationStatusEnum.Waiting);

        getSessionBean().setSessOccPermitApplication(selectedApplication);

        try {
            return getSessionBean().getNavStack().popLastPage();
        } catch (NavigationException ex) {
            System.out.println("OccPermitApplicationManageBB.cancelAttachment() | ERROR: " + ex);
            //We must do things a little bit different here to make sure messages are kept after the redirect.
            FacesContext context = getFacesContext();
                    context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                             "An error occured while trying to direct you back to the page you were last on."
                            + " No changes to the database were saved. Please return to the page manually.",
                            "Do not hit the return button again but note the error."));
                    context.getExternalContext().getFlash().setKeepMessages(true);
            return "";
        }
    }

    public String path1SpawnNewOccPeriod() {

        selectedApplication.setStatus(OccApplicationStatusEnum.OldUnit);

        getSessionBean().setSessOccPermitApplication(selectedApplication);

        getSessionBean().getNavStack().pushCurrentPage();

        getSessionBean().setUnitDetermined(true);

        return "occPermitNewPeriod";

    }

    public String path2NewUnitAndPeriod() {

        selectedApplication.setStatus(OccApplicationStatusEnum.NewUnit);

        getSessionBean().setSessOccPermitApplication(selectedApplication);

        getSessionBean().getNavStack().pushCurrentPage();

        getSessionBean().setUnitDetermined(false);

        return "occPermitNewPeriod";

    }

    public void path3AttachRejectionMessage(ActionEvent ev) {

        SystemCoordinator sc = getSystemCoordinator();

        OccupancyIntegrator oi = getOccupancyIntegrator();

        selectedApplication.setStatus(OccApplicationStatusEnum.Rejected);

        // build message to document change
        MessageBuilderParams mcc = new MessageBuilderParams();
        mcc.setUser(getSessionBean().getSessUser());
        mcc.setExistingContent(selectedApplication.getExternalPublicNotes());
        mcc.setHeader(getResourceBundle(Constants.MESSAGE_TEXT).getString("rejectedOccPermitApplicationHeader"));
        mcc.setExplanation(getResourceBundle(Constants.MESSAGE_TEXT).getString("rejectedOccPermitApplicationExplanation"));
        mcc.setNewMessageContent(rejectedApplicationMessage);

        selectedApplication.setExternalPublicNotes(sc.appendNoteBlock(mcc));
        try {
            oi.updateOccPermitApplication(selectedApplication);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Public note added to Occupancy permit application ID " + selectedApplication.getId() + ".", ""));

        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to write message to The Database",
                            "This is a system level error that must be corrected by a sys admin--sorries!."));
        }
    }

    public void path4AttachInvalidMessage(ActionEvent ev) {

        SystemCoordinator sc = getSystemCoordinator();

        OccupancyIntegrator oi = getOccupancyIntegrator();

        selectedApplication.setStatus(OccApplicationStatusEnum.Invalid);

        // build message to document change
        MessageBuilderParams mcc = new MessageBuilderParams();
        mcc.setUser(getSessionBean().getSessUser());
        mcc.setExistingContent(selectedApplication.getExternalPublicNotes());
        mcc.setHeader(getResourceBundle(Constants.MESSAGE_TEXT).getString("invalidOccPermitApplicationHeader"));
        mcc.setExplanation(getResourceBundle(Constants.MESSAGE_TEXT).getString("invalidOccPermitApplicationExplanation"));
        mcc.setNewMessageContent(invalidApplicationMessage);

        selectedApplication.setExternalPublicNotes(sc.appendNoteBlock(mcc));
        try {
            oi.updateOccPermitApplication(selectedApplication);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Public note added to Occupancy permit application ID " + selectedApplication.getId() + ".", ""));

        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to write message to The Database",
                            "This is a system level error that must be corrected by a sys admin--sorries!."));
        }
    }
    
    public void changePACCAccess() {
        System.out.println("CEActionRequestsBB.changePACCAccess");
        OccupancyIntegrator oi = getOccupancyIntegrator();

        try {
            oi.updatePACCAccess(selectedApplication);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Done! Public access status is now: " + String.valueOf(selectedApplication.isPaccEnabled())
                    + " for application ID: " + selectedApplication.getId(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to add change public access code status",
                    getResourceBundle(Constants.MESSAGE_TEXT).getString("systemLevelError")));
        }
    }

    public String editAttachedUnits() {

        getSessionBean().setSessOccPermitApplication(selectedApplication);

        getSessionBean().getNavStack().pushCurrentPage();

        return "applicationUnitList";
    }

    public String editAttachedPersons() {

        getSessionBean().setSessOccPermitApplication(selectedApplication);

        getSessionBean().getNavStack().pushCurrentPage();

        return "applicationChangePeople";
    }
    
    public String goToPersonChangeOrders() {

        List<Person> skeletonHorde = new ArrayList<>();
        
        for(PersonOccApplication skeleton : selectedApplication.getAttachedPersons()){
            
            skeletonHorde.add(skeleton);
            
        }
        
        getSessionBean().setSessPersonList(skeletonHorde);

        getSessionBean().getNavStack().pushCurrentPage();

        return "personChanges";
    }
    
    public String goToUnitChangeOrders() {

        PropertyCoordinator pc = getPropertyCoordinator();
        
        try{
        
            getSessionBean().setSessProperty(pc.getPropertyByPropUnitID(selectedApplication.getApplicationPropertyUnit().getUnitID()));

            getSessionBean().getNavStack().pushCurrentPage();

        return "unitsChanges";
        } catch(IntegrationException ex){
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Something when wrong while trying to redirect you to unit changes!", ""));
            return "";
        }
    }

    public String acceptUnitListChanges() {

        SystemCoordinator sc = getSystemCoordinator();
        PropertyCoordinator pc = getPropertyCoordinator();
        OccupancyIntegrator oi = getOccupancyIntegrator();

        if (selectedApplication.getApplicationPropertyUnit() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a property unit for the application.", ""));
            return "";
        }

        try {
            //Let's first attach a message letting the user know the unit list at the property was changed.
            MessageBuilderParams mcc = new MessageBuilderParams();
            mcc.setUser(getSessionBean().getSessUser());
            mcc.setExistingContent(selectedApplication.getExternalPublicNotes());
            mcc.setHeader("UNITS CHANGED");
            mcc.setExplanation("Some changes were made to the unit list of the property you filled out an application for. "
                    + "The unit you applied for occupancy for may have been changed as well");
            selectedApplication.setExternalPublicNotes(sc.appendNoteBlock(mcc));

            List<PropertyUnit> temp = pc.applyUnitList(unitList, propertyForApplication);

            for (PropertyUnit unit : temp) {
                if (unit.getUnitNumber().contentEquals(selectedApplication.getApplicationPropertyUnit().getUnitNumber())) {
                    selectedApplication.setApplicationPropertyUnit(unit);
                }
            }

            oi.updateOccPermitApplication(selectedApplication);

            return getSessionBean().getNavStack().popLastPage();
        } catch (IntegrationException ex) {
            System.out.println("OccPermitManageBB.acceptUnitListChanges() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occured while trying to save your changes.", ""));

        } catch (BObStatusException ex) {
            System.out.println("OccPermitManageBB.acceptUnitListChanges() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occured while trying to save your changes: " + ex, ""));
        } catch (NavigationException ex) {
            System.out.println("OccPermitManageBB.acceptUnitListChanges() | ERROR: " + ex);
            //We must do things a little bit different here to make sure messages are kept after the redirect.
            FacesContext context = getFacesContext();
                    context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occured while trying to direct you back to the page you were on."
                            + " Your changes to the database were saved. Please return to the page manually.",
                            "Do not hit the return button again but note the error."));
                    context.getExternalContext().getFlash().setKeepMessages(true);
        }
        return "";

    }

    public String acceptAttachedPersonChanges() {

        OccupancyCoordinator oc = getOccupancyCoordinator();

        selectedApplication.setApplicantPerson(applicant);

        selectedApplication.setPreferredContact(contactPerson);

        selectedApplication.setAttachedPersons(attachedPersons);

        try {
            selectedApplication = oc.verifyOccPermitPersonsRequirement(selectedApplication);
            oc.updateOccPermitApplicationPersons(selectedApplication);
            return getSessionBean().getNavStack().popLastPage();
        } catch (IntegrationException ex) {
            System.out.println("OccPermitApplicationManageBB.acceptAttachedPersonChanges() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occured while trying to store your changes to the person", ""));
        } catch (BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.toString(), ""));
        } catch(NavigationException ex){
            System.out.println("OccPermitApplicationManageBB.acceptAttachedPersonChanges() | ERROR: " + ex);
            //We must do things a little bit different here to make sure messages are kept after the redirect.
            FacesContext context = getFacesContext();
                    context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occured while trying to direct you back to the page you were on."
                            + " Your changes to the database were saved. Please return to the page manually.",
                            "Do not hit the return button again but note the error."));
                    context.getExternalContext().getFlash().setKeepMessages(true);
        }

        return "";
    }

    public void attachInternalMessage(ActionEvent ev) {
        SystemCoordinator sc = getSystemCoordinator();
        OccupancyIntegrator oi = getOccupancyIntegrator();
        MessageBuilderParams mbp = new MessageBuilderParams();
        mbp.setUser(getSessionBean().getSessUser());
        mbp.setExistingContent(selectedApplication.getInternalNotes());
        mbp.setHeader(getResourceBundle(Constants.MESSAGE_TEXT).getString("internalNote"));
        mbp.setExplanation("");
        mbp.setNewMessageContent(internalNoteText);
        String newNotes = sc.appendNoteBlock(mbp);
        System.out.println("CEActionRequestsBB.attachInternalMessage | msg before adding to request: " + newNotes);

        selectedApplication.setInternalNotes(newNotes);
        try {
            oi.updateOccPermitApplication(selectedApplication);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Done: added internal note to application ID " + selectedApplication.getId(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to update notes, sorry!",
                    getResourceBundle(Constants.MESSAGE_TEXT).getString("systemLevelError")));
        }

    }

    public void attachPublicMessage(ActionEvent ev) {

        SystemCoordinator sc = getSystemCoordinator();
        OccupancyIntegrator oi = getOccupancyIntegrator();
        MessageBuilderParams mbp = new MessageBuilderParams();
        mbp.setUser(getSessionBean().getSessUser());
        mbp.setExistingContent(selectedApplication.getExternalPublicNotes());
        mbp.setHeader(getResourceBundle(Constants.MESSAGE_TEXT).getString("externalNote"));
        mbp.setExplanation("");
        mbp.setNewMessageContent(externalNoteText);

        selectedApplication.setExternalPublicNotes(sc.appendNoteBlock(mbp));
        try {
            oi.updateOccPermitApplication(selectedApplication);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Done: Added a public note to application ID " + selectedApplication.getId(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to update notes, sorry!",
                    getResourceBundle(Constants.MESSAGE_TEXT).getString("systemLevelError")));
        }

    }

    public boolean isPathsDisabled() {

        if (selectedApplication == null) {
            return true;
        }

        return selectedApplication.getStatus() != OccApplicationStatusEnum.Waiting; //paths should be disabled if the occ application is not waiting to be reviewed.

    }
    
    /**
     * @return if the user should not be able to change public access on an object
     */
    public boolean isDisablePACCControl() {
        disablePACCControl = false;
        if (getSessionBean().getSessUser().getMyCredential().isHasMuniStaffPermissions() == false) {
            disablePACCControl = true;
        }
        return disablePACCControl;
    }

    public boolean isCurrentApplicationSelected() {
        return currentApplicationSelected;
    }

    public void setCurrentApplicationSelected(boolean currentApplicationSelected) {
        this.currentApplicationSelected = currentApplicationSelected;
    }

    public List<OccPermitApplication> getApplicationList() {
        return applicationList;
    }

    public void setApplicationList(List<OccPermitApplication> applicationList) {
        this.applicationList = applicationList;
    }

    public OccPermitApplication getSelectedApplication() {
        return selectedApplication;
    }

    public void setSelectedApplication(OccPermitApplication selectedApplication) {
        this.selectedApplication = selectedApplication;
    }

    public OccPermitApplication getSearchParams() {
        return searchParams;
    }

    public void setSearchParams(OccPermitApplication searchParams) {
        this.searchParams = searchParams;
    }

    public boolean isReason_ctl() {
        return reason_ctl;
    }

    public void setReason_ctl(boolean reason_ctl) {
        this.reason_ctl = reason_ctl;
    }

    public boolean isStatus_ctl() {
        return status_ctl;
    }

    public void setStatus_ctl(boolean status_ctl) {
        this.status_ctl = status_ctl;
    }

    public boolean isConnectedOccPeriod_ctl() {
        return connectedOccPeriod_ctl;
    }

    public void setConnectedOccPeriod_ctl(boolean connectedOccPeriod_ctl) {
        this.connectedOccPeriod_ctl = connectedOccPeriod_ctl;
    }

    public boolean isConnectedOccPeriod_val() {
        return connectedOccPeriod_val;
    }

    public void setConnectedOccPeriod_val(boolean connectedOccPeriod_val) {
        this.connectedOccPeriod_val = connectedOccPeriod_val;
    }

    public LocalDateTime getQueryBegin() {
        return queryBegin;
    }

    public void setQueryBegin(LocalDateTime queryBegin) {
        this.queryBegin = queryBegin;
    }

    public Date getQueryBegin_Util() {
        return convertDate(queryEnd);
    }

    public void setQueryBegin_Util(Date queryBeginUtil) {
        queryBegin = convertDate(queryBeginUtil);
    }

    public LocalDateTime getQueryEnd() {
        return queryEnd;
    }

    public void setQueryEnd(LocalDateTime queryEnd) {
        this.queryEnd = queryEnd;
    }

    public Date getQueryEnd_Util() {
        return convertDate(queryEnd);
    }

    public void setQueryEnd_Util(Date queryEndUtil) {
        queryEnd = convertDate(queryEndUtil);
    }

    public List<OccApplicationStatusEnum> getStatusList() {
        return statusList;
    }

    public void setStatusList(List<OccApplicationStatusEnum> statusList) {
        this.statusList = statusList;
    }

    public String getRejectedApplicationMessage() {
        return rejectedApplicationMessage;
    }

    public void setRejectedApplicationMessage(String rejectedApplicationMessage) {
        this.rejectedApplicationMessage = rejectedApplicationMessage;
    }

    public String getInvalidApplicationMessage() {
        return invalidApplicationMessage;
    }

    public void setInvalidApplicationMessage(String invalidApplicationMessage) {
        this.invalidApplicationMessage = invalidApplicationMessage;
    }

    public OccApplicationStatusEnum getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(OccApplicationStatusEnum newStatus) {
        this.newStatus = newStatus;
    }

    public boolean isUnitAlreadyDetermined() {
        return unitAlreadyDetermined;
    }

    public void setUnitAlreadyDetermined(boolean unitAlreadyDetermined) {
        this.unitAlreadyDetermined = unitAlreadyDetermined;
    }

    public Property getPropertyForApplication() {
        return propertyForApplication;
    }

    public void setPropertyForApplication(Property propertyForApplication) {
        this.propertyForApplication = propertyForApplication;
    }

    public String getAcceptApplicationMessage() {
        return acceptApplicationMessage;
    }

    public void setAcceptApplicationMessage(String acceptApplicationMessage) {
        this.acceptApplicationMessage = acceptApplicationMessage;
    }

    public List<ViewOptionsActiveListsEnum> getAllViewOptions() {
        return allViewOptions;
    }

    public void setAllViewOptions(List<ViewOptionsActiveListsEnum> allViewOptions) {
        this.allViewOptions = allViewOptions;
    }

    public ViewOptionsActiveListsEnum getCurrentViewOption() {
        return currentViewOption;
    }

    public void setCurrentViewOption(ViewOptionsActiveListsEnum input) {

        currentViewOption = input;

        unitList = new ArrayList<>();

        if (null == currentViewOption) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occurred while trying to set the current view option. Returning to default.", ""));
            currentViewOption = ViewOptionsActiveListsEnum.VIEW_ACTIVE;
        } else {
            switch (currentViewOption) {
                case VIEW_ALL:
                    unitList.addAll(propertyForApplication.getUnitList());
                    break;

                case VIEW_ACTIVE:
                    for (PropertyUnit unit : propertyForApplication.getUnitList()) {
                        if (unit.isActive()) {
                            unitList.add(unit);
                        }
                    }

                    break;

                case VIEW_INACTIVE:
                    for (PropertyUnit unit : propertyForApplication.getUnitList()) {
                        if (!unit.isActive()) {
                            unitList.add(unit);
                        }
                    }

                    break;
            }

        }

    }

    public List<PersonOccApplication> getAttachedPersons() {
        return attachedPersons;
    }

    public void setAttachedPersons(List<PersonOccApplication> attachedPersons) {
        this.attachedPersons = attachedPersons;
    }

    public List<PropertyUnit> getUnitList() {
        return unitList;
    }

    public void setUnitList(List<PropertyUnit> unitList) {
        this.unitList = unitList;
    }

    public Person getSearchPerson() {
        return searchPerson;
    }

    public void setSearchPerson(Person searchPerson) {
        this.searchPerson = searchPerson;
    }

    public List<Person> getPersonSearchResults() {
        return personSearchResults;
    }

    public void setPersonSearchResults(List<Person> personSearchResults) {
        this.personSearchResults = personSearchResults;
    }

    public Person getApplicant() {
        return applicant;
    }

    public void setApplicant(Person applicant) {
        this.applicant = applicant;
    }

    public Person getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(Person contactPerson) {
        this.contactPerson = contactPerson;
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

    public String getInternalNoteText() {
        return internalNoteText;
    }

    public void setInternalNoteText(String internalNoteText) {
        this.internalNoteText = internalNoteText;
    }

    public String getExternalNoteText() {
        return externalNoteText;
    }

    public void setExternalNoteText(String externalNoteText) {
        this.externalNoteText = externalNoteText;
    }

}
