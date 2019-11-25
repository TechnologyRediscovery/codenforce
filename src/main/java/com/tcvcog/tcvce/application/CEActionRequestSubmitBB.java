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

import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import java.util.Date;
import java.io.Serializable;
import org.primefaces.component.tabview.TabView;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.integration.CEActionRequestIntegrator;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
/**
 *
 * @author cedba
 */

public class CEActionRequestSubmitBB extends BackingBeanUtils implements Serializable{
    
    // for request lookup
    
    private Person currentPerson;
    
    private TabView tabView;
    private int currentTabIndex;

    private ArrayList<Property> propList;
    
    private String houseNum;
    private String streetName;
    
    private Map<String, Integer> violationTypeMap;
    private int violationTypeID;
    private String violationTypeName;
    
    private Municipality selectedMuni;
    private List<Municipality> muniList;
    
    private Property selectedProperty;
    
    private boolean form_atSpecificAddress;
    private String form_nonPropertyLocation;
    
    private String form_requestDescription;
    private boolean form_isUrgent;
    private Date form_dateOfRecord;
    
    private boolean form_anonymous;
    
    private int submittedRequestPACC;
    
    // located address
        
    private PersonType submittingPersonType;
    private String form_requestorFName;
    private String form_requestorLName;
    private String form_requestorJobtitle;
    
    private String form_requestor_phoneCell;
    private String form_requestor_phoneHome;
    private String form_requestor_phoneWork;
    
    private String form_requestor_email;
    private String form_requestor_addressStreet;
    private String form_requestor_addressCity;
    private String form_requestor_addressZip;
    private String form_requestor_addressState;
    private java.util.Date currentDate;

    /**
     * Creates a new instance of ActionRequestBean
     */
    public CEActionRequestSubmitBB(){
        // set date of record to current date
        form_dateOfRecord = java.util.Date.from(java.time.LocalDateTime.now()
                .atZone(ZoneId.systemDefault()).toInstant());
        currentTabIndex = 0;
        System.out.println("ActionRequestBean.ActionRequestBean");
    }
    
    public String getReturnValue(){
        return "paccSearch";
        
    }
    
    
    /**
     * Entry mechanism to the Code Enforcement Action Request creation process
     * We grab the muni the user selected, set it in the new Action Request
     * and store it in the session bean which we'll access and manipulate
     * over the next few pages and finally submit on the last page. This is
     * a poor person's flow system
     * @return String pointer to the next step in the process: choose property
     */
    public String storeSelectedMuni(){
        CEActionRequest cear;
        CaseCoordinator cc = getCaseCoordinator();
        cear = cc.getNewActionRequest();
        cear.setDateOfRecordUtilDate(form_dateOfRecord);
        cear.setMuni(selectedMuni);
        getSessionBean().setCeactionRequestForSubmission(cear);
        return "chooseProperty";
    }
    
    public String storePropertyInfo(){
        if(getSessionBean().getCeactionRequestForSubmission().getRequestProperty() == null){
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                       "Please select a property from the list of search results to continue.", ""));
            return "";
        }
        return "describeConcern";
    }
    
    public String addRequestorDetails(){
//        User u = getSessionBean().getFacesUser();
//        if(u == null){
            return "requestorDetails";
//            
//        } else {
//            
//            return "reviewAndSubmit";
//        }
        
    }
    
    
    /**
     * This action method is called when the request code enforcement
     * action request is submitted online (submit button in submitCERequest
     * @return the page ID for navigation
     */
    public String submitActionRequest() {
        
        CEActionRequest req = getSessionBean().getCeactionRequestForSubmission();
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        
        int submittedActionRequestID;
        
        // start by pulling the person fields and sending them to be entered
        // into db as a person. The ID of this person is returned, and used in our
        // insertion of the action request as a whole. 
        
        // LT goal: bundle these into a transaction that is rolled back if either 
        // the person or the request bounces
        int personID = storeActionRequestorPerson(getSessionBean().getCeactionRequestForSubmission().getActionRequestorPerson());
        
        req.setPersonID(personID);
        
        int controlCode = getControlCodeFromTime();
        req.setRequestPublicCC(controlCode);
        
        req.setIsAtKnownAddress(true);
        
//        if (form_atSpecificAddress){
//            req.setRequestProperty(selectedProperty);
//        } else {
//            req.setAddressOfConcern(form_nonPropertyLocation);
//        }
//        
//        req.setIssueType_issueTypeID(violationTypeID);
//        req.setRequestDescription(form_requestDescription);
//        req.setDateOfRecord(form_dateOfRecord
//                .toInstant()
//                .atZone(ZoneId.systemDefault())
//                .toLocalDateTime());
//        req.setIsUrgent(form_isUrgent);
//        
        // note that the time stamp is applied by the integration layer
        // with a simple call to the backing bean getTimeStamp method

        try { 
            // send the request into the DB
            submittedActionRequestID = ceari.submitCEActionRequest(req);
            getSessionBean().setActiveRequest(ceari.getActionRequestByRequestID(submittedActionRequestID));
            
            // Now go right back to the DB and get the request we just submitted to verify before displaying the PACC
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_INFO, 
                       "Success! Your request has been submitted and passed to our code enforcement team.", ""));
            return "success";

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
     * Coordinates the active tab index based on the status of various
     * form fields. it's not pretty, but it's functional
     */
    private void manageTabs(){
        System.out.println("ActionRequestBean | manageTab | prop: " + selectedProperty);
        System.out.println("ActionRequestBean | manageTab | lname: " + form_requestorLName);
        System.out.println("ActionRequestBean | violationType ID: " + violationTypeID);
        
        // check for first tab completed
        if((selectedProperty != null 
                && violationTypeID <= 0
                && form_requestorLName == null) 
                || 
                (form_nonPropertyLocation != null && !form_atSpecificAddress)) {
            System.out.println("selecting tab index 1");
            currentTabIndex = 1; // go to request details tab
        // check for second tab com1pleted
        } else if(selectedProperty != null
                && violationTypeID > 0
                && form_requestorLName == null) {
            System.out.println("selecting tab index 2");
            currentTabIndex = 2; // to to contact info tab
        // check for third tab completed
        } else if(selectedProperty != null
                && violationTypeID > 0
                && form_requestorLName != null){
            System.out.println("selecting tab index 3");
            currentTabIndex = 3; // go to final tab
            
        } else {
            currentTabIndex = 0;
        }
        if(tabView != null){
          tabView.setActiveIndex(currentTabIndex);
        }
    }
    
    public String populateActionRequestorPerson(){
        
        currentPerson = new Person();
        currentPerson.setPersonType(submittingPersonType);
        currentPerson.setMuni(getSessionBean().getCeactionRequestForSubmission().getMuni());
        
        currentPerson.setFirstName(form_requestorFName);
        currentPerson.setLastName(form_requestorLName);
        currentPerson.setJobTitle(form_requestorJobtitle);
        
        currentPerson.setPhoneCell(form_requestor_phoneCell);
        currentPerson.setPhoneHome(form_requestor_phoneHome);
        currentPerson.setPhoneWork(form_requestor_phoneWork);
        
        currentPerson.setEmail(form_requestor_email);
        currentPerson.setAddressStreet(form_requestor_addressStreet);
        currentPerson.setAddressCity(form_requestor_addressCity);
        currentPerson.setAddressZip(form_requestor_addressZip);
        currentPerson.setAddressState(form_requestor_addressState);
        
        currentPerson.setNotes("[System-Generated] This person was created "
                + "from the code enforcement action request form");
        
        currentPerson.setActive(true);
        currentPerson.setUnder18(false);
        currentPerson.setVerifiedBy(null);
        
        getSessionBean().getCeactionRequestForSubmission().setActionRequestorPerson(currentPerson);
        
        return "reviewAndSubmit";
        
    }
    
    public int storeActionRequestorPerson(Person p){
        PersonIntegrator personIntegrator = getPersonIntegrator();
        
        int insertedPersonID = 0;
        
        try {
            insertedPersonID = personIntegrator.insertPerson(p);
        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
            getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "INTEGRATION ERROR: Sorry, the system was unable to store your contact information and as a result, your request has not been recorded.", 
                    "You might call your municipal office to report this error and make a request over the phone. "
                    + "You can also phone the Turtle Creek COG's tecnical support specialist, Eric Darsow, at 412.840.3020 and leave a message"));

            
        } catch (NullPointerException ex){
             System.out.println(ex.toString());
        }
        
        return insertedPersonID;
        
    } // close storePerson 
    
    public void storePropertyLocationInfo(ActionEvent event){
        manageTabs();
        
    }
    
    public void storeNoPropertyInfo(ActionEvent event){
        manageTabs();
        System.out.println("ActionRequestBean.storeNoPropertyInfo | request location: " + form_nonPropertyLocation);
    }
    
    public void incrementalFormContinue(ActionEvent event){
        manageTabs();
        System.out.println("ActionRequestBean.incrementalFormContinue | tabview: " + currentTabIndex);
    }

      
    public void searchForPropertiesSingleMuni(ActionEvent ev){
        PropertyIntegrator pi = getPropertyIntegrator();
        
        try {
            propList = pi.searchForProperties(houseNum, streetName, getSessionBean().getCeactionRequestForSubmission().getMuni().getMuniCode());
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
     * @return the form_requestorFName
     */
    public String getForm_requestorFName() {
        return form_requestorFName;
    }

    /**
     * @param form_requestorFName the form_requestorFName to set
     */
    public void setForm_requestorFName(String form_requestorFName) {
        this.form_requestorFName = form_requestorFName;
    }

    /**
     * @return the form_requestorLName
     */
    public String getForm_requestorLName() {
        return form_requestorLName;
    }

    /**
     * @param form_requestorLName the form_requestorLName to set
     */
    public void setForm_requestorLName(String form_requestorLName) {
        this.form_requestorLName = form_requestorLName;
    }

    /**
     * @return the form_requestor_phoneCell
     */
    public String getForm_requestor_phoneCell() {
        return form_requestor_phoneCell;
    }

    /**
     * @param form_requestor_phoneCell the form_requestor_phoneCell to set
     */
    public void setForm_requestor_phoneCell(String form_requestor_phoneCell) {
        this.form_requestor_phoneCell = form_requestor_phoneCell;
    }

    /**
     * @return the form_requestor_email
     */
    public String getForm_requestor_email() {
        return form_requestor_email;
    }

    /**
     * @param form_requestor_email the form_requestor_email to set
     */
    public void setForm_requestor_email(String form_requestor_email) {
        this.form_requestor_email = form_requestor_email;
    }

    /**
     * @return the form_requestor_addressStreet
     */
    public String getForm_requestor_addressStreet() {
        return form_requestor_addressStreet;
    }

    /**
     * @param form_requestor_addressStreet the form_requestor_addressStreet to set
     */
    public void setForm_requestor_addressStreet(String form_requestor_addressStreet) {
        this.form_requestor_addressStreet = form_requestor_addressStreet;
    }

    /**
     * @return the form_requestor_addressCity
     */
    public String getForm_requestor_addressCity() {
        return form_requestor_addressCity;
    }

    /**
     * @param form_requestor_addressCity the form_requestor_addressCity to set
     */
    public void setForm_requestor_addressCity(String form_requestor_addressCity) {
        this.form_requestor_addressCity = form_requestor_addressCity;
    }

    /**
     * @return the form_requestor_addressZip
     */
    public String getForm_requestor_addressZip() {
        return form_requestor_addressZip;
    }

    /**
     * @param form_requestor_addressZip the form_requestor_addressZip to set
     */
    public void setForm_requestor_addressZip(String form_requestor_addressZip) {
        this.form_requestor_addressZip = form_requestor_addressZip;
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
     * @return the violationTypeID
     */
    public int getViolationTypeID() {
        return violationTypeID;
    }

    /**
     * @param violationTypeID the violationTypeID to set
     */
    public void setViolationTypeID(int violationTypeID) {
        this.violationTypeID = violationTypeID;
    }

    /**
     * @return the violationTypeName
     */
    public String getViolationTypeName() {
        return violationTypeName;
    }

    /**
     * @param violationTypeName the violationTypeName to set
     */
    public void setViolationTypeName(String violationTypeName) {
        this.violationTypeName = violationTypeName;
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
     * @return the submittingPersonType
     */
    public PersonType getSubmittingPersonType() {
        submittingPersonType = PersonType.Public;
        return submittingPersonType;
    }

    /**
     * @param submittingPersonType the submittingPersonType to set
     */
    public void setSubmittingPersonType(PersonType submittingPersonType) {
        this.submittingPersonType = submittingPersonType;
    }

    /**
     * @return the form_requestor_phoneHome
     */
    public String getForm_requestor_phoneHome() {
        return form_requestor_phoneHome;
    }

    /**
     * @param form_requestor_phoneHome the form_requestor_phoneHome to set
     */
    public void setForm_requestor_phoneHome(String form_requestor_phoneHome) {
        this.form_requestor_phoneHome = form_requestor_phoneHome;
    }

    /**
     * @return the form_requestor_phoneWork
     */
    public String getForm_requestor_phoneWork() {
        return form_requestor_phoneWork;
    }

    /**
     * @param form_requestor_phoneWork the form_requestor_phoneWork to set
     */
    public void setForm_requestor_phoneWork(String form_requestor_phoneWork) {
        this.form_requestor_phoneWork = form_requestor_phoneWork;
    }

    

    /**
     * @return the submittingPersonTypes
     */
    public PersonType[] getSubmittingPersonTypes() {
        
        return PersonType.values();
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
     * @return the form_requestorJobtitle
     */
    public String getForm_requestorJobtitle() {
        return form_requestorJobtitle;
    }

    /**
     * @return the form_requestor_addressState
     */
    public String getForm_requestor_addressState() {
        return form_requestor_addressState;
    }

    /**
     * @param form_requestorJobtitle the form_requestorJobtitle to set
     */
    public void setForm_requestorJobtitle(String form_requestorJobtitle) {
        this.form_requestorJobtitle = form_requestorJobtitle;
    }

    /**
     * @param form_requestor_addressState the form_requestor_addressState to set
     */
    public void setForm_requestor_addressState(String form_requestor_addressState) {
        this.form_requestor_addressState = form_requestor_addressState;
    }

  
    /**
     * @return the propList
     */
    public ArrayList getPropList() {
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
    public void setPropList(ArrayList propList) {
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
     * @return the muniList
     */
    public List<Municipality> getMuniList() {
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        try {
            muniList = mi.getCompleteMuniList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        return muniList;
    }

    /**
     * @param muniList the muniList to set
     */
    public void setMuniList(List<Municipality> muniList) {
        this.muniList = muniList;
    }

    /**
     * @return the violationTypeMap
     */
    public Map<String, Integer> getViolationTypeMap() {
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        violationTypeMap = ceari.getViolationMap();
        return violationTypeMap;
    }

    /**
     * @param violationTypeMap the violationTypeMap to set
     */
    public void setViolationTypeMap(Map<String, Integer> violationTypeMap) {
        this.violationTypeMap = violationTypeMap;
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
}