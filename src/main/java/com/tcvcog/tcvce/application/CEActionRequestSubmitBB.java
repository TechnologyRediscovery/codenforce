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
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.BlobCoordinator;
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.BlobTypeException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.NavigationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.BlobLight;
import java.io.Serializable;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CEActionRequestIssueType;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.integration.CEActionRequestIntegrator;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.search.QueryProperty;
import com.tcvcog.tcvce.entities.search.QueryPropertyEnum;
import com.tcvcog.tcvce.entities.search.SearchParamsProperty;
import com.tcvcog.tcvce.integration.BlobIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import org.primefaces.event.FileUploadEvent;

/**
 *
 * @author cedba
 */
public class CEActionRequestSubmitBB extends BackingBeanUtils implements Serializable {

    private CEActionRequest currentRequest; //the CEAR the user is filling out

    private Person currentPerson; //Used when selecting a person to attach to the CEAR
    
    //If the user is an employee with restricted access, they can select some 
    //persons using the restricted tools. They will appear in this field.
    private List<Person> personCandidateList;
    
    private List<PersonType> submittingPersonTypes; //A list of person types that can fill out a CEAR;

    private List<Property> propList; //TODO: Turn into PublicInfoBundles!

    //Two fields used for searching properties
    private String houseNum;
    private String streetName;

    private Municipality selectedMuni;

    private Property selectedProperty; //TODO: Turn into PublicInfoBundle!

    private List<CEActionRequestIssueType> issueTypeList;

    //Used by the interface to prevent users from selecting dates in the future.
    private java.util.Date currentDate;
    
    //Fields used when adding descriptions to blobs.
    private String blobDescription;
    private int descriptionBlobID;

    /**
     * Creates a new instance of ActionRequestBean
     */
    public CEActionRequestSubmitBB() {
    }

    @PostConstruct
    public void initBean() {

        if (currentRequest == null) {

            initializeReqAndMuni();

        }

        submittingPersonTypes = new ArrayList<>();

        //Manually add what person types users should choose from
        submittingPersonTypes.add(PersonType.MuniStaff);
        submittingPersonTypes.add(PersonType.Owner);
        submittingPersonTypes.add(PersonType.Tenant);
        submittingPersonTypes.add(PersonType.Manager);
        submittingPersonTypes.add(PersonType.Public);
        submittingPersonTypes.add(PersonType.LawEnforcement);

    }

    public void initializeReqAndMuni() {

        currentRequest = getSessionBean().getSessCEAR();
        CaseCoordinator cc = getCaseCoordinator();
        User usr = getSessionBean().getSessUser();

        if (usr != null) {

            selectedMuni = getSessionBean().getSessMuni();

            if (currentRequest != null) {

                personCandidateList = getSessionBean().getSessPersonList();

            //TODO: This allows you to use persons related to a property, 
            //but this should only be used by users with restricted access.
            //So maybe add it to a restricted CEAR interface
            
//              PropertyIntegrator pi = getPropertyIntegrator();
//                if (currentRequest.getRequestProperty() != null) {
//                      try {
//                          personCandidateList = pi.getPropertyDataHeavy(currentRequest.getRequestProperty().getPropertyID()).getPersonOccApplicationList();
//                      } catch (IntegrationException | BObStatusException | EventException | AuthorizationException ex) {
//                          System.out.println(ex);
//                      }
//                }
            }

        }

        if (currentRequest == null) {
            currentRequest = new CEActionRequest();
            currentRequest.setBlobList(new ArrayList<BlobLight>());
        }

        //Load the issue types so the user can categorize their problem
            try {
                issueTypeList = cc.cear_getIssueTypes();
            } catch (IntegrationException ex) {
                System.out.println("Error occured while fetching issue typelist: " + ex);
            }
            
            //If at a later date we wish to only get issue types from a certain muni
            //You can always use:
            //cc.cear_getIssueTypes(currentRequest.getMuni());
    }

    /**
     * Return to the previous step
     * @return 
     */
    public String goBack() {
        try {
            return getSessionBean().getNavStack().popLastPage();
        } catch (NavigationException ex) {
            System.out.println("CEActionRequestSubmitBB.goBack() | ERROR: " + ex);
            //We must do things a little bit different here to make sure messages are kept after the redirect.
            FacesContext context = getFacesContext();
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Something went wrong when we tried to direct you back a page!", ""));
            
            context.getExternalContext().getFlash().setKeepMessages(true);
            
            return "requestCEActionFlow";
        }
    }

    /**
     * A user with restricted access can use their account information
     * instead of filling out the request manually.
     * @return 
     */
    public String requestActionAsFacesUser() {
        currentRequest.setRequestor(getSessionBean().getSessUser().getPerson());
        getSessionBean().setSessCEAR(currentRequest);
        return "reviewAndSubmit";
    }

    /**
     * A user with restricted access can select a person from a list and use
     * their information instead of filling out the person manually.
     * @return 
     */
    public String assignSelectedRequestorPersonAndContinue() {
        currentRequest.setRequestor(currentPerson);
        getSessionBean().setSessCEAR(currentRequest);
        return "reviewAndSubmit";
    }

    /**
     * If the user filled in the requestor's information manually, we double-check
     * that information before setting it on the SessionBean and moving on.
     * @return 
     */
    public String validateActionRequestorNewPersonAndContinue() {
        //Make a copy of the requestor for testing.
        Person test = currentRequest.getRequestor();
        
        //Mostly we need to make sure certain fields were not left blank
        
        if(test.getFirstName() == null || test.getFirstName().trim().length() == 0){
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please enter in your first name",
                            ""));
            return "";
        }
        
        if(test.getLastName() == null || test.getLastName().trim().length() == 0){
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please enter in your last name",
                            ""));
            return "";
        }
        
        if(test.getAddressStreet() == null || test.getAddressStreet().trim().length() == 0
                || test.getAddressCity()== null || test.getAddressCity().trim().length() == 0
                || test.getAddressState()== null || test.getAddressState().trim().length() == 0
                || test.getAddressZip()== null || test.getAddressZip().trim().length() == 0){
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please enter in your full address",
                            ""));
            return "";
        }
        
        if(test.getEmail()== null || test.getEmail().trim().length() == 0){
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please enter in an email where we can reach you",
                            ""));
            return "";
        }
        
        getSessionBean().setSessCEAR(currentRequest);
        getSessionBean().getNavStack().pushCurrentPage();
        return "reviewAndSubmit";
    }
    
    /**
     * A method used to insert the requestor into the database
     * when finalizing the request process.
     * Used by the method submitActionRequest() in this class.
     * @param p
     * @return 
     */
    private int insertActionRequestorNewPerson(Person p) {
        PersonIntegrator personIntegrator = getPersonIntegrator();
        SystemIntegrator si = getSystemIntegrator();
        int insertedPersonID = 0;

        try {
            p.setSource(si.getBOBSource(
                    Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                            .getString("actionRequestPublicUserPersonSourceID"))));

            insertedPersonID = personIntegrator.insertPerson(p);
            System.out.println("CEActionReqeustSubmitBB.storeActionRequestorPerson | PersonID " + insertedPersonID);
        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "INTEGRATION ERROR: Sorry, the system was unable to store your contact information and as a result, your request has not been recorded.",
                            "You might call your municipal office to report this error and make a request over the phone. "
                            + "You can also phone the Turtle Creek COG's technical support specialist, Eric Darsow, at 412.840.3020 and leave a message"));
        } catch (NullPointerException ex) {
            System.out.println(ex.toString());
        }
        
        //Remember to throw back the person's ID!
        return insertedPersonID;
    }

    /**
     * Entry mechanism to the Code Enforcement Action Request creation process:
     * We grab the muni the user selected, set it in the new Action Request and
     * store it in the session bean which we'll access and manipulate over the
     * next few pages and finally submit on the last page. This is a poor
     * person's flow system
     *
     * @return String pointer to the next step in the process: choose property
     */
    public String storeSelectedMuni() {
        CEActionRequest cear;
        CaseCoordinator cc = getCaseCoordinator();
        cear = cc.cear_getInititalizedCEActionRequest();
        cear.setMuni(selectedMuni);
        cear.setBlobList(new ArrayList<BlobLight>());
        cear.setRequestProperty(new Property());
        getSessionBean().setSessCEAR(cear);
        getSessionBean().getNavStack().pushCurrentPage();
        return "chooseProperty";
    }

    /**
     * Stores the property that requires code enforcement before moving on to
     * the description step.
     * @return 
     */
    public String storePropertyInfo() {
        if (currentRequest.getRequestProperty() == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a property from the list of search results to continue.", ""));
            return "";
        }
        getSessionBean().setSessCEAR(currentRequest);
        getSessionBean().getNavStack().pushCurrentPage();
        return "describeConcern";
    }

    /**
     * Save the descriptive fields of the request on the sessionBean and move
     * to the next page.
     * Also validates to make sure the user has filled out all the required fields.
     * @return 
     */
    public String saveConcernDescriptions() {

        if(currentRequest.getIssue() == null){
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please specify what kind of issue you are reporting", ""));
            return "";
        }
        
        if(currentRequest.getRequestDescription()== null || currentRequest.getRequestDescription().trim().length() == 0){
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please give a short description of the issue", ""));
            return "";
        }
        
        if (currentRequest.getBlobList() == null) {
            currentRequest.setBlobList(new ArrayList<BlobLight>());
        }
        getSessionBean().setSessCEAR(currentRequest);
        getSessionBean().getNavStack().pushCurrentPage();
        return "photoUpload";
    }

    /**
     * Not only goes to the next page without connecting photos to the CEAR,
     * deletes all blobs the user has uploaded.
     *
     * @return
     */
    public String skipPhotoUpload() {
        BlobCoordinator blobc = getBlobCoordinator();

        if (currentRequest.getBlobList().size() > 0) {

            for (BlobLight b : currentRequest.getBlobList()) {
                try {
                    blobc.deletePhotoBlob((Blob) b);
                } catch (IntegrationException
                        | AuthorizationException
                        | BObStatusException
                        | BlobException
                        | EventException
                        | ViolationException ex) {
                    System.out.println(ex);
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "An error occured while skipping photo upload.", ""));
                }
            }

            currentRequest.setBlobList(new ArrayList<BlobLight>());
            getSessionBean().setSessCEAR(currentRequest);
        }

        // before moving onto the person page, get a person's skeleton from the coordinator, put it
        // in the session for use on the next page
        setupPersonEntry();
        getSessionBean().getNavStack().pushCurrentPage();
        return "requestorDetails";
    }

    /**
     * The photos the user uploaded are already saved in the database, so let's 
     * just save the descriptions the user entered in and move on.
     * @return 
     */
    public String savePhotosAndContinue() {

        BlobIntegrator bi = getBlobIntegrator();
        for (BlobLight b : currentRequest.getBlobList()) {

            //save the description to the database.
            try {
                bi.updatePhotoBlobDescription(b);
            } catch (IntegrationException ex) {
                System.out.println("CEActionRequestSubmitBB.savePhotosAndContinue() | ERROR: " + ex);
            }
        }
        getSessionBean().setSessCEAR(currentRequest);
        // before moving onto the person page, get a person's skeleton from the coordinator, put it
        // in the session for use on the next page
        setupPersonEntry();
        getSessionBean().getNavStack().pushCurrentPage();
        return "requestorDetails";
    }

    /**
     * Going back after uploading photos can cause photos to remain unlinked to
     * an object and slip through the cracks, so we'll save their connections.
     * The user can always delete them later.
     *
     * @return
     */
    public String goBackFromPhotos() {
        try {
            //Just use the uploading method
            savePhotosAndContinue();

            //savePhotosAndContinue() puts another page on the stack, so let's 
            //toss that into the void
            getSessionBean().getNavStack().popLastPage();
            
            return getSessionBean().getNavStack().popLastPage();
        } catch (NavigationException ex) {
            System.out.println("CEActionRequestSubmitBB.goBackFromPhotos() | ERROR: " + ex);
            //We must do things a little bit different here to make sure messages are kept after the redirect.
            FacesContext context = getFacesContext();
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Something went wrong when we tried to direct you back a page!", ""));
            context.getExternalContext().getFlash().setKeepMessages(true);
            return "requestCEActionFlow";
        }
    }

    /**
     * Deletes one photo the user selects.
     *
     * @param input
     */
    public void deletePhoto(Blob input) {
        BlobCoordinator blobc = getBlobCoordinator();
        try {
            blobc.deletePhotoBlob(input);
        } catch (IntegrationException
                | AuthorizationException
                | BObStatusException
                | BlobException
                | EventException
                | ViolationException ex) {
            System.out.println("CEActionRequestSubmitBB.deletePhoto() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occured while trying to delete the selected photo.", ""));
        }

        currentRequest.getBlobList().remove(input);
    }

    /**
     * This method sets up the requestor field on the request so it can be manipulated
     * by the interface when the user enters in their data.
     */
    private void setupPersonEntry() {
        UserCoordinator uc = getUserCoordinator();
        PersonCoordinator pc = getPersonCoordinator();
        Municipality m = currentRequest.getMuni();
        Person skel = pc.personCreateMakeSkeleton(m);
        try {
            skel.setCreatorUserID(uc.user_getUserRobot().getUserID());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        currentRequest.setRequestor(skel);
        getSessionBean().setSessCEAR(currentRequest);
    }

    public void handlePhotoUpload(FileUploadEvent ev) {
        if (ev == null) {
            System.out.println("CEActionRequestBB.handlePhotoUpload | event: null");
            return;
        }

        System.out.println("CEActionRequestSubmitBB.handlePhotoUpload | File: " + ev.getFile().getFileName() + " Type: " + ev.getFile().getContentType());

        BlobCoordinator blobc = getBlobCoordinator();
        Blob blob = null;
        try {
            blob = blobc.getNewBlob();  //init new blob
            // TODO: PF migration https://primefaces.github.io/primefaces/10_0_0/#/../migrationguide/8_0
//            blob.setBytes(ev.getFile().getContents());  // set bytes
            blob.setFilename(ev.getFile().getFileName());
            blob.setMunicode(currentRequest.getMuni().getMuniCode());

            blob = blobc.storeBlob(blob);
        } catch (IntegrationException | IOException | BlobTypeException ex) {
            System.out.println("CEActionRequestSubmitBB.handleFileUpload | " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Something went wrong while trying to upload your photo, please try again.",
                            "If this problem persists, please call your municipal office."));
        } catch (BlobException ex) {
            System.out.println("CEActionRequestSubmitBB.handleFileUpload | " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(),
                            ""));
        }

        currentRequest.getBlobList().add(blob);
    }

    /**
     * This method finalizes the CEAR submission process by saving the request
     * in the database as well as all the objects attached to it.
     *
     * @return
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public String submitActionRequest() throws IntegrationException {

        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        BlobIntegrator blobI = getBlobIntegrator();
        PersonIntegrator pi = getPersonIntegrator();
        SessionBean sb = getSessionBean();
        SystemIntegrator si = getSystemIntegrator();

        int submittedActionRequestID;
        int personID;
        // start by pulling the person fields and sending them to be saved
        // in db as a person. The ID of this person is returned, and used in our
        // insertion of the action request as a whole. 

        // LT goal: bundle these into a transaction that is rolled back if either 
        // the person or the request bounces
        if (currentRequest.getRequestor().getPersonID() == 0) {
            
            //The person is not in our database, prepared it for saving
            if (getSessionBean().getSessUser() != null) {
                currentRequest.getRequestor().setSource(
                        si.getBOBSource(Integer.parseInt(
                                getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                                        .getString("actionRequestNewPersonByInternalUserPersonSourceID"))));
            } else {
                currentRequest.getRequestor().setSource(
                        si.getBOBSource(Integer.parseInt(
                                getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                                        .getString("actionRequestPublicUserPersonSourceID"))));
            }
            
            //insert it into the database.
            personID = insertActionRequestorNewPerson(currentRequest.getRequestor());
            try {
                //We want to get the entry we just inserted into the database
                currentRequest.setRequestor(pi.getPerson(personID));
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
        } else {

            // do nothing, since we already have the person in the system
        }

        //Generate a PACC for the user to access the CEAR
        currentRequest.setRequestPublicCC(generateControlCodeFromTime(currentRequest.getMuniCode()));
        // all requests now are required to be at a known address
        currentRequest.setIsAtKnownAddress(true);
        currentRequest.setActive(true);
        currentRequest.setDateOfRecord(LocalDateTime.now(ZoneId.systemDefault()));
        
        // note that the time stamp is applied by the integration layer
        // with a simple call to the backing bean getTimeStamp method
        try {
            // send the request into the DB
            submittedActionRequestID = ceari.submitCEActionRequest(currentRequest);
            
            // get the request we just submitted to attach it to the files the user uploaded
            // before displaying the PACC
            sb.setSessCEAR(ceari.getActionRequestByRequestID(submittedActionRequestID));

            for (BlobLight blob : currentRequest.getBlobList()) {
                try {
                    blobI.linkPhotoBlobToActionRequest(blob.getBlobID(), sb.getSessCEAR().getRequestID());
                } catch (IntegrationException ex) {
                    System.out.println(ex);
                }
            }

            clearNavStack();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Your request has been submitted and passed to our code enforcement team.", ""));
            return "successCEAR";

        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "INTEGRATION ERROR: Unable write request into the database, our apologies!",
                            "Please call your municipal office and report your concern by phone."));
            return "";
        }
    }

    /**
     * Destroys all data the user has filled out so far (also deletes files they
     * have uploaded) and returns them to the beginning of the request process
     * so they can start over.
     * Currently not used by the interface, as it could cause confusion.
     * Still, this method is here in case it would like to be used at a later date.
     * @return 
     */
    public String restartRequest() {

        if (currentRequest.getBlobList().size() > 0) {
            BlobCoordinator bc = getBlobCoordinator();

            for (BlobLight b : currentRequest.getBlobList()) {
                try {
                    bc.deletePhotoBlob(b);
                } catch (IntegrationException
                        | AuthorizationException
                        | BObStatusException
                        | BlobException
                        | EventException
                        | ViolationException ex) {
                    System.out.println("CEActionRequestSubmitBB.restartRequest | ERROR: " + ex.toString());
                }
            }
        }
        
        currentRequest = null;
        getSessionBean().setSessCEAR(null);
        clearNavStack();
        return "requestCEActionFlow";
    }

    /**
     * A method that clears the navStack of all entries to ensure the user does 
     * not retrace steps that are no longer needed.
     */
    private void clearNavStack() {
        while (getSessionBean().getNavStack().peekLastPage() != null) {
            try {
                getSessionBean().getNavStack().popLastPage();
            } catch (NavigationException ex) {
                //nothing, we just want to clear the stack anyway.
            }
        }
    }

    /**
     * Uses the terms entered by the user to search properties within the muni
     * the user selected.
     * @param ev 
     */
    public void searchForPropertiesSingleMuni(ActionEvent ev) {
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
                spp.setMuni_val(currentRequest.getMuni());
                spp.setLimitResultCount_ctl(true);
                spp.setLimitResultCount_val(20);

                sc.runQuery(qp);

            } else {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Something went wrong with the property search! Sorry!", ""));
            }

        } catch (IntegrationException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Something went wrong with the property search! Sorry!", ""));
        }

        if (qp != null && !qp.getBOBResultList().isEmpty()) {
            propList = qp.getBOBResultList();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Your search completed with " + getPropList().size() + " results", ""));
        }

    }

    /**
     * @return the currentPerson
     */
    public Person getCurrentPerson() {
        return currentPerson;
    }

    /**
     * @param currentPerson the currentPerson to set
     */
    public void setCurrentPerson(Person currentPerson) {
        this.currentPerson = currentPerson;
    }

    /**
     * @return the submittingPersonTypes
     */
    public List<PersonType> getSubmittingPersonTypes() {

        return submittingPersonTypes;
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
     * @return the propList
     */
    public List<Property> getPropList() {
        return propList;
    }

    /**
     * @return the houseNum
     */
    public String getHouseNum() {
        return houseNum;
    }

    /**
     * @param propList the propList to set
     */
    public void setPropList(List<Property> propList) {
        this.propList = propList;
    }

    /**
     * @param houseNum the houseNum to set
     */
    public void setHouseNum(String houseNum) {
        this.houseNum = houseNum;
    }

    /**
     * @return the streetName
     */
    public String getStreetName() {
        return streetName;
    }

    /**
     * @param streetName the streetName to set
     */
    public void setStreetName(String streetName) {
        this.streetName = streetName;
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
     * @return the currentDate
     */
    public java.util.Date getCurrentDate() {
        currentDate = java.util.Date.from(java.time.LocalDateTime.now()
                .atZone(ZoneId.systemDefault()).toInstant());
        return currentDate;
    }

    /**
     * @param currentDate the currentDate to set
     */
    public void setCurrentDate(java.util.Date currentDate) {
        this.currentDate = currentDate;
    }

    /**
     * @return the currentRequest
     */
    public CEActionRequest getCurrentRequest() {
        return currentRequest;
    }

    /**
     * @param currentRequest the currentRequest to set
     */
    public void setCurrentRequest(CEActionRequest currentRequest) {
        this.currentRequest = currentRequest;
    }
    
    /**
     * @return the personCandidateList
     */
    public List<Person> getPersonCandidateList() {
        return personCandidateList;
    }

    /**
     * @param personCandidateList the personCandidateList to set
     */
    public void setPersonCandidateList(List<Person> personCandidateList) {
        this.personCandidateList = personCandidateList;
    }

    public List<CEActionRequestIssueType> getIssueTypeList() {
        return issueTypeList;
    }

    public void setIssueTypeList(List<CEActionRequestIssueType> issueTypeList) {
        this.issueTypeList = issueTypeList;
    }

    public String getBlobDescription() {
        return blobDescription;
    }

    public void setBlobDescription(String blobDescription) {
        this.blobDescription = blobDescription;
    }

    public int getDescriptionBlobID() {
        return descriptionBlobID;
    }

    public void setDescriptionBlobID(int descriptionBlobID) {
        this.descriptionBlobID = descriptionBlobID;
    }

}