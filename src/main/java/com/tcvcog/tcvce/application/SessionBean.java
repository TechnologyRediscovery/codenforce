/*
 * Copyright (C) 2018 Turtle Creek Valley
Council of Governments, PA
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

import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Citation;
import com.tcvcog.tcvce.entities.CodeElement;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.CodeElementGuideEntry;
import com.tcvcog.tcvce.entities.CodeSet;
import com.tcvcog.tcvce.entities.CodeSource;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EventCasePropBundle;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.NoticeOfViolation;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyWithLists;
import com.tcvcog.tcvce.entities.PublicInfoBundle;
import com.tcvcog.tcvce.entities.PublicInfoBundleCECase;
import com.tcvcog.tcvce.entities.ReportConfig;
import com.tcvcog.tcvce.entities.ReportConfigCECase;
import com.tcvcog.tcvce.entities.ReportConfigCECaseList;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.occupancy.entities.OccPermitApplication;
import com.tcvcog.tcvce.occupancy.entities.OccPermitApplicationReason;
import com.tcvcog.tcvce.occupancy.entities.OccupancyInspection;
import java.io.Serializable;
import java.util.List;

/**
 *
 *       //@ManagedBean(name="sessionBean")
 *       //@SessionScoped
 * @author Eric C. Darsow
 */
public class SessionBean extends BackingBeanUtils implements Serializable{
    
    // primary security authoriziation container 
    // TODO - remove and get the keycard directly from the User stored in the session

    private List<Property> propertyQueue;
    private List<Person> personQueue;
    private List<CEActionRequest> cEActionRequestQueue;
    private List<CECase> cECaseQueue;
    private List<EventCasePropBundle> cEEventWCPIQueue;
    private List<CodeViolation> violationQueue;
    private List<OccupancyInspection> inspectionQueue;
    
    /* *** System Core Objects Session Shelves ***  */
    private Municipality activeMuni;
    private List<Municipality> userAuthMuniList;
    private User facesUser;
    private Property activeProp;
    private Person activePerson;
    private PropertyWithLists activePropWithList;
    
    /* *** Municipal Code Session Shelves ***  */
    private CodeSource activeCodeSource;
    private CodeSet activeCodeSet;
    private CodeElementGuideEntry activeCodeElementGuideEntry;
    private EnforcableCodeElement selectedEnfCodeElement;
    private CodeElement activeCodeElement;
    
    /* *** Code Enf Action Request Session Shelves ***  */
    private Person personForCEActionRequestSubmission;
    private User utilityUserToUpdate;
    private CEActionRequest ceactionRequestForSubmission;
    private CEActionRequest activeRequest;
    private CECase cECase;
    
     /* *** Code Enforcement Case Session Shelves ***  */
    private NoticeOfViolation activeNotice;
    private Citation activeCitation;
    private CodeViolation activeCodeViolation;
    
    /* *** Public Data Session Shelves ***  */
    private List<PublicInfoBundle> infoBundleList;
    private PublicInfoBundleCECase pibCECase;

    /* *** Reporting *** */
    private ReportConfigCECase reportConfigCECase;
    private ReportConfigCECaseList reportConfigCECaseList;
    private ReportConfig activeReport;
    
    
    /* *** Occupancy Permit Application Session Shelves *** */
    private OccPermitApplication occPermitApplication;
    private PropertyUnit activePropUnit;
    private PropertyWithLists activePropWithLists;
    private OccPermitApplicationReason occPermitApplicationReason;

    /* *** Blob Upload Session Shelves *** */
    private List<Blob> blobList;


    /**
     * Creates a new instance of getSessionBean()
     */
    public SessionBean() {
        System.out.println("SessionBean.SessionBean");
    }

    /**
     * @return the activeProp
     */
    public Property getActiveProp() {
        return activeProp;
    }

    /**
     * @return the cECase
     */
    public CECase getcECase() {
        return cECase;
        
    }
    
    public void refreshActiveCase() throws IntegrationException{
        CaseIntegrator ci = getCaseIntegrator();
        if(cECase != null){
            CECase c = ci.getCECase(cECase.getCaseID());
            cECase = c;
        }
    }

   
    /**
     * @return the activePerson
     */
    public Person getActivePerson() {
        return activePerson;
    }

   
    /**
     * @return the activeNotice
     */
    public NoticeOfViolation getActiveNotice() {
        return activeNotice;
    }

    

    /**
     * @return the activeCodeSet
     */
    public CodeSet getActiveCodeSet() {
        return activeCodeSet;
    }

    /**
     * @return the activeCitation
     */
    public Citation getActiveCitation() {
        return activeCitation;
    }

    /**
     * @return the selectedEnfCodeElement
     */
    public EnforcableCodeElement getSelectedEnfCodeElement() {
        return selectedEnfCodeElement;
    }

    /**
     * @return the activeCodeViolation
     */
    public CodeViolation getActiveCodeViolation() {
        return activeCodeViolation;
    }

    /**
     * @return the violationQueue
     */
    public List<CodeViolation> getViolationQueue() {
        return violationQueue;
    }

    /**
     * @return the activeCodeElementGuideEntry
     */
    public CodeElementGuideEntry getActiveCodeElementGuideEntry() {
        return activeCodeElementGuideEntry;
    }

    /**
     * @param activeProp the activeProp to set
     */
    public void setActiveProp(Property activeProp) {
        this.activeProp = activeProp;
    }

    /**
     * @param cECase the cECase to set
     */
    public void setcECase(CECase cECase) {
        this.cECase = cECase;
    }



    /**
     * @param activePerson the activePerson to set
     */
    public void setActivePerson(Person activePerson) {
        this.activePerson = activePerson;
    }

   
    /**
     * @param activeNotice the activeNotice to set
     */
    public void setActiveNotice(NoticeOfViolation activeNotice) {
        this.activeNotice = activeNotice;
    }

 

    /**
     * @param activeCodeSet the activeCodeSet to set
     */
    public void setActiveCodeSet(CodeSet activeCodeSet) {
        this.activeCodeSet = activeCodeSet;
    }

    /**
     * @param activeCitation the activeCitation to set
     */
    public void setActiveCitation(Citation activeCitation) {
        this.activeCitation = activeCitation;
    }

    /**
     * @param selectedEnfCodeElement the selectedEnfCodeElement to set
     */
    public void setSelectedEnfCodeElement(EnforcableCodeElement selectedEnfCodeElement) {
        this.selectedEnfCodeElement = selectedEnfCodeElement;
    }

    /**
     * @param activeCodeViolation the activeCodeViolation to set
     */
    public void setActiveCodeViolation(CodeViolation activeCodeViolation) {
        this.activeCodeViolation = activeCodeViolation;
    }

    /**
     * @param violationQueue the violationQueue to set
     */
    public void setViolationQueue(List<CodeViolation> violationQueue) {
        this.violationQueue = violationQueue;
    }

    /**
     * @param activeCodeElementGuideEntry the activeCodeElementGuideEntry to set
     */
    public void setActiveCodeElementGuideEntry(CodeElementGuideEntry activeCodeElementGuideEntry) {
        this.activeCodeElementGuideEntry = activeCodeElementGuideEntry;
    }

    /**
     * @return the utilityUserToUpdate
     */
    public User getUtilityUserToUpdate() {
        return utilityUserToUpdate;
    }

    /**
     * @return the activeCodeSource
     */
    public CodeSource getActiveCodeSource() {
        return activeCodeSource;
    }

    /**
     * @param utilityUserToUpdate the utilityUserToUpdate to set
     */
    public void setUtilityUserToUpdate(User utilityUserToUpdate) {
        this.utilityUserToUpdate = utilityUserToUpdate;
    }

    /**
     * @param activeCodeSource the activeCodeSource to set
     */
    public void setActiveCodeSource(CodeSource activeCodeSource) {
        this.activeCodeSource = activeCodeSource;
    }

    /**
     * @return the activeMuni
     */
    public Municipality getActiveMuni() {
        return activeMuni;
    }

    /**
     * @param activeMuni the activeMuni to set
     */
    public void setActiveMuni(Municipality activeMuni) {
        System.out.println("MissionControlBB.setActiveMuni | set: " + activeMuni.getMuniName());
        this.activeMuni = activeMuni;
    }

    /**
     * @return the activeCodeElement
     */
    public CodeElement getActiveCodeElement() {
        return activeCodeElement;
    }

    /**
     * @param activeCodeElement the activeCodeElement to set
     */
    public void setActiveCodeElement(CodeElement activeCodeElement) {
        this.activeCodeElement = activeCodeElement;
    }

  

    /**
     * @return the infoBundleList
     */
    public List<PublicInfoBundle> getInfoBundleList() {
        return infoBundleList;
    }

    /**
     * @param infoBundleList the infoBundleList to set
     */
    public void setInfoBundleList(List<PublicInfoBundle> infoBundleList) {
        this.infoBundleList = infoBundleList;
    }

    /**
     * @return the pibCECase
     */
    public PublicInfoBundleCECase getPibCECase() {
        return pibCECase;
    }

    /**
     * @param pibCECase the pibCECase to set
     */
    public void setPibCECase(PublicInfoBundleCECase pibCECase) {
        this.pibCECase = pibCECase;
    }

    /**
     * @return the cEActionRequestQueue
     */
    public List<CEActionRequest> getcEActionRequestQueue() {
        return cEActionRequestQueue;
    }

    /**
     * @return the cECaseQueue
     */
    public List<CECase> getcECaseQueue() {
        return cECaseQueue;
    }

    /**
     * @param cEActionRequestQueue the cEActionRequestQueue to set
     */
    public void setcEActionRequestQueue(List<CEActionRequest> cEActionRequestQueue) {
        this.cEActionRequestQueue = cEActionRequestQueue;
    }
    

    /**
     * @param cECaseQueue the cECaseQueue to set
     */
    public void setcECaseQueue(List<CECase> cECaseQueue) {
        this.cECaseQueue = cECaseQueue;
    }

    /**
     * @return the ceactionRequestForSubmission
     */
    public CEActionRequest getCeactionRequestForSubmission() {
        return ceactionRequestForSubmission;
    }

    /**
     * @param ceactionRequestForSubmission the ceactionRequestForSubmission to set
     */
    public void setCeactionRequestForSubmission(CEActionRequest ceactionRequestForSubmission) {
        this.ceactionRequestForSubmission = ceactionRequestForSubmission;
    }

    /**
     * @return the userAuthMuniList
     */
    public List<Municipality> getUserAuthMuniList() {
        return userAuthMuniList;
    }

    /**
     * @param userAuthMuniList the userAuthMuniList to set
     */
    public void setUserAuthMuniList(List<Municipality> userAuthMuniList) {
        this.userAuthMuniList = userAuthMuniList;
    }

    /**
     * @return the activeRequest
     */
    public CEActionRequest getActiveRequest() {
        return activeRequest;
    }

    /**
     * @param activeRequest the activeRequest to set
     */
    public void setActiveRequest(CEActionRequest activeRequest) {
        this.activeRequest = activeRequest;
    }

    /**
     * @return the facesUser
     */
    @Override
    public User getFacesUser() {
        return facesUser;
    }

    /**
     * @param facesUser the facesUser to set
     */
    @Override
    public void setFacesUser(User facesUser) {
        this.facesUser = facesUser;
    }

    /**
     * @return the personQueue
     */
    public List<Person> getPersonQueue() {
        return personQueue;
    }

    /**
     * @param personQueue the personQueue to set
     */
    public void setPersonQueue(List<Person> personQueue) {
        this.personQueue = personQueue;
    }

   
    /**
     * @return the personForCEActionRequestSubmission
     */
    public Person getPersonForCEActionRequestSubmission() {
        return personForCEActionRequestSubmission;
    }

    /**
     * @param personForCEActionRequestSubmission the personForCEActionRequestSubmission to set
     */
    public void setPersonForCEActionRequestSubmission(Person personForCEActionRequestSubmission) {
        this.personForCEActionRequestSubmission = personForCEActionRequestSubmission;
    }

    /**
     * @return the activePropWithList
     */
    public PropertyWithLists getActivePropWithList() {
        return activePropWithList;
    }

    /**
     * @param activePropWithList the activePropWithList to set
     */
    public void setActivePropWithList(PropertyWithLists activePropWithList) {
        this.activePropWithList = activePropWithList;
    }
    
    public OccPermitApplication getOccPermitApplication() {
        return occPermitApplication;
    }

    public void setOccPermitApplication(OccPermitApplication occPermitApplication) {
        this.occPermitApplication = occPermitApplication;
    }

    public PropertyUnit getActivePropUnit() {
        return activePropUnit;
    }

    public void setActivePropUnit(PropertyUnit activePropUnit) {
        this.activePropUnit = activePropUnit;
    }

    /**
     * @return the activePropWithLists
     */
    public PropertyWithLists getActivePropWithLists() {
        return activePropWithLists;
    }

    /**
     * @param activePropWithLists the activePropWithLists to set
     */
    public void setActivePropWithLists(PropertyWithLists activePropWithLists) {
        this.activePropWithLists = activePropWithLists;
    }

    /**
     * @return the occPermitApplicationReason
     */
    public OccPermitApplicationReason getOccPermitApplicationReason() {
        return occPermitApplicationReason;
    }

    /**
     * @param occPermitApplicationReason the occPermitApplicationReason to set
     */
    public void setOccPermitApplicationReason(OccPermitApplicationReason occPermitApplicationReason) {
        this.occPermitApplicationReason = occPermitApplicationReason;
    }
     
    /*
     * @return the cEEventWCPIQueue
     */
    public List<EventCasePropBundle> getcEEventWCPIQueue() {
        return cEEventWCPIQueue;
    }

    /**
     * @param cEEventWCPIQueue the cEEventWCPIQueue to set
     */
    public void setcEEventWCPIQueue(List<EventCasePropBundle> cEEventWCPIQueue) {
        this.cEEventWCPIQueue = cEEventWCPIQueue;
    }

    /**
     * @return the propertyQueue
     */
    public List<Property> getPropertyQueue() {
        return propertyQueue;
    }

    /**
     * @param propertyQueue the propertyQueue to set
     */
    public void setPropertyQueue(List<Property> propertyQueue) {
        this.propertyQueue = propertyQueue;
    }

    /**
     * @return the inspectionQueue
     */
    public List<OccupancyInspection> getInspectionQueue() {
        return inspectionQueue;
    }

    /**
     * @param inspectionQueue the inspectionQueue to set
     */
    public void setInspectionQueue(List<OccupancyInspection> inspectionQueue) {
        this.inspectionQueue = inspectionQueue;
    }

    /**
     * @return the reportConfigCECase
     */
    public ReportConfigCECase getReportConfigCECase() {
        return reportConfigCECase;
    }

    /**
     * @param reportConfigCECase the reportConfigCECase to set
     */
    public void setReportConfigCECase(ReportConfigCECase reportConfigCECase) {
        this.reportConfigCECase = reportConfigCECase;
    }

    /**
     * @return the activeReport
     */
    public ReportConfig getActiveReport() {
        return activeReport;
    }

    /**
     * @param activeReport the activeReport to set
     */
    public void setActiveReport(ReportConfig activeReport) {
        this.activeReport = activeReport;
    }

    /**
     * @return the reportConfigCECaseList
     */
    public ReportConfigCECaseList getReportConfigCECaseList() {
        return reportConfigCECaseList;
    }

    /**
     * @param reportConfigCECaseList the reportConfigCECaseList to set
     */
    public void setReportConfigCECaseList(ReportConfigCECaseList reportConfigCECaseList) {
        this.reportConfigCECaseList = reportConfigCECaseList;
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
    public void setBlobList(List<Blob> blobList) {
        this.blobList = blobList;
    }
    
}
