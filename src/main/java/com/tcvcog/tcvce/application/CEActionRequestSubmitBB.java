/*
 * Copyright (C) 2017 cedba
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
import java.util.Date;
import java.io.Serializable;
import org.primefaces.component.tabview.TabView;
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
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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

    private CEActionRequest currentRequest;

    private Person currentPerson;
    private int actionRequestorAssignmentMethod;
    private List<Person> personCandidateList;
    private boolean disabledPersonFormFields;
    private List<PersonType> submittingPersonTypes;

    private TabView tabView;
    private int currentTabIndex;

    private List<Property> propList; //TODO: Turn into PublicInfoBundles!

    private String houseNum;
    private String streetName;

    private Municipality selectedMuni;

    private Property selectedProperty; //TODO: Turn into PublicInfoBundle!

    private List<CEActionRequestIssueType> issueTypeList;

    private boolean form_atSpecificAddress;
    private String form_nonPropertyLocation;

    private String form_requestDescription;
    private boolean form_isUrgent;
    private Date form_dateOfRecord;

    private boolean form_anonymous;

    private int submittedRequestPACC;

    // located address
    private java.util.Date currentDate;

    private List<Blob> blobList;
    private String blobDescription;
    private int descriptionBlobID;

    /**
     * Creates a new instance of ActionRequestBean
     */
    public CEActionRequestSubmitBB() {
    }

    @PostConstruct
    public void initBean() {

        // set date of record to current date
        form_dateOfRecord = java.util.Date.from(java.time.LocalDateTime.now()
                .atZone(ZoneId.systemDefault()).toInstant());
        // init new, empty photo list
        blobList = new ArrayList<>();

        disabledPersonFormFields = false;
        actionRequestorAssignmentMethod = 1;

        if (currentRequest == null) {

            initializeReqAndMuni();

        }

        submittingPersonTypes = new ArrayList<>();

        //Manually add what person types we want.
        submittingPersonTypes.add(PersonType.MuniStaff);
        submittingPersonTypes.add(PersonType.Owner);
        submittingPersonTypes.add(PersonType.Tenant);
        submittingPersonTypes.add(PersonType.Manager);
        submittingPersonTypes.add(PersonType.Public);
        submittingPersonTypes.add(PersonType.LawEnforcement);

    }

    public void initializeReqAndMuni() {

        CEActionRequest sessReq = getSessionBean().getSessCEAR();
        CaseCoordinator cc = getCaseCoordinator();
        PropertyIntegrator pi = getPropertyIntegrator();
        BlobCoordinator bc = getBlobCoordinator();
        User usr = getSessionBean().getSessUser();

        if (usr != null) {

            selectedMuni = getSessionBean().getSessMuni();

            if (sessReq != null) {

                personCandidateList = getSessionBean().getSessPersonList();

//                if (currentRequest.getRequestProperty() != null) {
//            TODO: occbeta
//            try {
//                personCandidateList = pi.getPropertyDataHeavy(currentRequest.getRequestProperty().getPropertyID()).getPersonOccApplicationList();
//            } catch (IntegrationException | BObStatusException | EventException | AuthorizationException ex) {
//                System.out.println(ex);
//            }
//                }
            }

        }

        currentRequest = sessReq;

        if (currentRequest == null) {
            currentRequest = new CEActionRequest();
            currentRequest.setBlobIDList(new ArrayList<Integer>());
        }

        if (currentRequest.getMuni() != null) {
            try {
//                issueTypeList = cc.cear_getIssueTypes(currentRequest.getMuni());
                issueTypeList = cc.cear_getIssueTypes();
            } catch (IntegrationException ex) {
                System.out.println("Error occured while fetching issue typelist: " + ex);
            }
        }

        if (currentRequest.getBlobIDList().size() > 0) {
            for (Integer idNum : currentRequest.getBlobIDList()) {
                try {
                    blobList.add(bc.getPhotoBlob(idNum));
                } catch (IntegrationException | ClassNotFoundException | IOException | BlobTypeException | NoSuchElementException ex) {
                    System.out.println("Error occured while fetching request blob list: " + ex);
                }
            }

        }

    }

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

    public String requestActionAsFacesUser() {
        currentRequest.setRequestor(getSessionBean().getSessUser().getPerson());
        getSessionBean().setSessCEAR(currentRequest);
        return "reviewAndSubmit";
    }

    //TODO: Determine if die
    public void changePropertyPersonsDropDown() {

    }

    //TODO: Determine if die
    public void changeActionRequestorAssignment() {

    }

    public String assignSelectedRequestorPersonAndContinue() {
        currentRequest.setRequestor(currentPerson);
        getSessionBean().setSessCEAR(currentRequest);
        return "reviewAndSubmit";
    }

    public String validateActionRequestorNewPersonAndContinue() {
        getSessionBean().setSessCEAR(currentRequest);
        getSessionBean().getNavStack().pushCurrentPage();
        return "reviewAndSubmit";
    }

    public int insertActionRequestorNewPerson(Person p) {
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
        return insertedPersonID;
    } // close storePerson 

    /**
     * Entry mechanism to the Code Enforcement Action Request creation process
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
        cear.setDateOfRecordUtilDate(form_dateOfRecord);
        cear.setMuni(selectedMuni);
        cear.setBlobIDList(new ArrayList<Integer>());
        cear.setRequestProperty(new Property());
        getSessionBean().setSessCEAR(cear);
        getSessionBean().getNavStack().pushCurrentPage();
        return "chooseProperty";
    }

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

    public String saveConcernDescriptions() {
//        User u = getSessionBean().getFacesUser();
//        if(u == null){

        if (currentRequest.getBlobIDList() == null) {
            currentRequest.setBlobIDList(new ArrayList<Integer>());
        }
        getSessionBean().setSessCEAR(currentRequest);
        getSessionBean().setBlobList(new ArrayList<Blob>());
        getSessionBean().getNavStack().pushCurrentPage();
        return "photoUpload";
//            
//        } else {
//            
//            return "reviewAndSubmit";
//        }

    }

    /**
     * Not only goes to the next page without connecting photos to the CEAR,
     * deletes all blobs the user has uploaded.
     *
     * @return
     */
    public String skipPhotoUpload() {
        BlobCoordinator blobc = getBlobCoordinator();

        if (blobList.size() > 0) {

            for (Blob b : blobList) {
                try {
                    blobc.deletePhotoBlob(b);
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

            currentRequest.setBlobIDList(new ArrayList<Integer>());
            blobList = new ArrayList<>();
            getSessionBean().setSessCEAR(currentRequest);
        }

        // before moving onto the person page, get a person's skeleton from the coordinator, put it
        // in the session for use on the next page
        setupPersonEntry();
        getSessionBean().getNavStack().pushCurrentPage();
        return "requestorDetails";
    }

    public String savePhotosAndContinue() {

        BlobIntegrator bi = getBlobIntegrator();
        currentRequest.setBlobIDList(new ArrayList<Integer>());
        for (Blob b : blobList) {
            currentRequest.getBlobIDList().add(b.getBlobID());

            //Also, save the description to the database.
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
            //Just use the uploading 
            savePhotosAndContinue();

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

        currentRequest.getBlobIDList().remove(new Integer(input.getBlobID()));
        blobList.remove(input);
    }

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
//        skel.setSourceID(Integer.parseInt(
//                getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
//                .getString("actionRequestPublicUserPersonSourceID")));
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
            blob.setBytes(ev.getFile().getContents());  // set bytes
            blob.setFilename(ev.getFile().getFileName());
            blob.setMunicode(currentRequest.getMuni().getMuniCode());

            blob = blobc.storeBlob(blob);
        } catch (IntegrationException | IOException | ClassNotFoundException | NoSuchElementException ex) {
            System.out.println("CEActionRequestSubmitBB.handleFileUpload | " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Something went wrong while trying to upload your photo,please try again.",
                            "If this problem persists, please call your municipal office."));
        } catch (BlobException ex) {
            System.out.println("CEActionRequestSubmitBB.handleFileUpload | " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(),
                            ""));
        }

        blobList.add(blob);
    }

    /**
     * This action method is called when the request code enforcement action
     * request is submitted online (submit button in submitCERequest
     *
     * @return the page ID for navigation
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public String submitActionRequest() throws IntegrationException {

        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        BlobIntegrator blobI = getBlobIntegrator();
        PersonIntegrator pi = getPersonIntegrator();
        SessionBean sb = getSessionBean();
        BlobIntegrator blobi = getBlobIntegrator();
        SystemIntegrator si = getSystemIntegrator();

        int submittedActionRequestID;
        int personID;
        // start by pulling the person fields and sending them to be entered
        // into db as a person. The ID of this person is returned, and used in our
        // insertion of the action request as a whole. 

        // LT goal: bundle these into a transaction that is rolled back if either 
        // the person or the request bounces
        if (currentRequest.getRequestor().getPersonID() == 0) {
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
            personID = insertActionRequestorNewPerson(currentRequest.getRequestor());
            try {
                currentRequest.setRequestor(pi.getPerson(personID));
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
        } else {

            // do nothing, since we already have the person in the system
        }

        currentRequest.setRequestPublicCC(generateControlCodeFromTime(currentRequest.getMuniCode()));
        // all requests now are required to be at a known address
        currentRequest.setIsAtKnownAddress(true);
        currentRequest.setActive(true);
        currentRequest.setDateOfRecord(LocalDateTime.now(ZoneId.systemDefault()));
//        
        // note that the time stamp is applied by the integration layer
        // with a simple call to the backing bean getTimeStamp method
        try {
            // send the request into the DB
            submittedActionRequestID = ceari.submitCEActionRequest(currentRequest);
            // Now go right back to the DB and get the request we just submitted to verify before displaying the PACC
            sb.setSessCEAR(ceari.getActionRequestByRequestID(submittedActionRequestID));

            for (Integer blobID : currentRequest.getBlobIDList()) {
                try {
                    blobI.linkPhotoBlobToActionRequest(blobID, sb.getSessCEAR().getRequestID());
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

    public String restartRequest() {

        currentRequest = null;
        getSessionBean().setSessCEAR(null);

        if (blobList.size() > 0) {
            BlobCoordinator bc = getBlobCoordinator();

            for (Blob b : blobList) {
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

        clearNavStack();
        return "requestCEActionFlow";
    }

    private void clearNavStack() {
        while (getSessionBean().getNavStack().peekLastPage() != null) {
            try {
                getSessionBean().getNavStack().popLastPage();
            } catch (NavigationException ex) {
                //nothing, we just want to clear the stack anyway.
            }
        }
    }

    public void storePropertyLocationInfo(ActionEvent event) {

    }

    public void storeNoPropertyInfo(ActionEvent event) {
        System.out.println("ActionRequestBean.storeNoPropertyInfo | request location: " + form_nonPropertyLocation);
    }

    public void incrementalFormContinue(ActionEvent event) {
        System.out.println("ActionRequestBean.incrementalFormContinue | tabview: " + currentTabIndex);
    }

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
     * @return the form_nonPropertyLocation
     */
    public String getForm_nonPropertyLocation() {
        return form_nonPropertyLocation;
    }

    /**
     * @param form_nonPropertyLocation the form_nonPropertyLocation to set
     */
    public void setForm_nonPropertyLocation(String form_nonPropertyLocation) {
        this.form_nonPropertyLocation = form_nonPropertyLocation;
    }

    /**
     * @return the form_atSpecificAddress
     */
    public boolean isForm_atSpecificAddress() {
        form_atSpecificAddress = true;
        return form_atSpecificAddress;
    }

    /**
     * @param form_atSpecificAddress the form_atSpecificAddress to set
     */
    public void setForm_atSpecificAddress(boolean form_atSpecificAddress) {
        this.form_atSpecificAddress = form_atSpecificAddress;
    }

    /**
     * @return the form_requestDescription
     */
    public String getForm_requestDescription() {
        return form_requestDescription;
    }

    /**
     * @param form_requestDescription the form_requestDescription to set
     */
    public void setForm_requestDescription(String form_requestDescription) {
        this.form_requestDescription = form_requestDescription;
    }

    /**
     * @return the form_isUrgent
     */
    public boolean isForm_isUrgent() {
        return form_isUrgent;
    }

    /**
     * @param form_isUrgent the form_isUrgent to set
     */
    public void setForm_isUrgent(boolean form_isUrgent) {
        this.form_isUrgent = form_isUrgent;
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

    /**
     * @return the form_anonymous
     */
    public boolean isForm_anonymous() {
        return form_anonymous;
    }

    /**
     * @param form_anonymous the form_anonymous to set
     */
    public void setForm_anonymous(boolean form_anonymous) {
        this.form_anonymous = form_anonymous;
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
        System.out.println("ActionRequestBean.setHouseNum");
        this.houseNum = houseNum;
    }

    /**
     * @return the tabView
     */
    public TabView getTabView() {
        return tabView;
    }

    /**
     * @param tabView the tabView to set
     */
    public void setTabView(TabView tabView) {
        this.tabView = tabView;
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
     * @return the submittedRequestPACC
     */
    public int getSubmittedRequestPACC() {
        return submittedRequestPACC;
    }

    /**
     * @param submittedRequestPACC the submittedRequestPACC to set
     */
    public void setSubmittedRequestPACC(int submittedRequestPACC) {
        this.submittedRequestPACC = submittedRequestPACC;
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
     * @return the blobList
     */
    public List<Blob> getBlobList() {
        return blobList;
    }

    /**
     * @param blobList the blobList to set
     */
    public void setBlobListList(List<Blob> blobList) {
        this.blobList = blobList;
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
     * @return the actionRequestorAssignmentMethod
     */
    public int getActionRequestorAssignmentMethod() {
        return actionRequestorAssignmentMethod;
    }

    /**
     * @param actionRequestorAssignmentMethod the
     * actionRequestorAssignmentMethod to set
     */
    public void setActionRequestorAssignmentMethod(int actionRequestorAssignmentMethod) {
        this.actionRequestorAssignmentMethod = actionRequestorAssignmentMethod;
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

    /**
     * @return the disabledPersonFormFields
     */
    public boolean isDisabledPersonFormFields() {
        return disabledPersonFormFields;
    }

    /**
     * @param disabledPersonFormFields the disabledPersonFormFields to set
     */
    public void setDisabledPersonFormFields(boolean disabledPersonFormFields) {
        this.disabledPersonFormFields = disabledPersonFormFields;
    }

    public int getCurrentTabIndex() {
        return currentTabIndex;
    }

    public void setCurrentTabIndex(int currentTabIndex) {
        this.currentTabIndex = currentTabIndex;
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
