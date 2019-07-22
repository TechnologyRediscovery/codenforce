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

import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.entities.reports.Report;
import com.tcvcog.tcvce.entities.reports.ReportCEARList;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECase;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECaseList;
import com.tcvcog.tcvce.entities.reports.ReportConfigCEEventList;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.search.QueryCEAR;
import com.tcvcog.tcvce.entities.search.QueryCECase;
import com.tcvcog.tcvce.entities.search.QueryEventCECase;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.entities.occupancy.OccPermitApplication;
import com.tcvcog.tcvce.entities.occupancy.OccInspection;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPermit;
import com.tcvcog.tcvce.entities.occupancy.OccPermitApplicationReason;
import com.tcvcog.tcvce.entities.search.QueryOccPeriod;
import com.tcvcog.tcvce.entities.search.QueryPerson;
import com.tcvcog.tcvce.entities.search.QueryProperty;
import java.io.Serializable;
import java.util.ArrayList;
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
    
    private List<CEActionRequest> queueCEAR;
    
    private CEActionRequest sessionCEAR;
    
    private List<CECase> cECaseQueue;
    private List<EventCECaseCasePropBundle> cEEventWCPIQueue;
    private List<CodeViolation> violationQueue;
    private List<OccInspection> inspectionQueue;
    
    private QueryProperty queryProperty;
    private QueryPerson queryPerson;
    private QueryCEAR queryCEAR;
    private QueryCECase queryCECase;
    private QueryEventCECase queryEventCECase;
    private QueryOccPeriod queryOccPeriod;
    
   
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
    private CECase sessionCECase;
    
     /* *** Code Enforcement Case Session Shelves ***  */
    private NoticeOfViolation activeNotice;
    private Citation activeCitation;
    private CodeViolation sessionCodeViolation;
    
    /* *** Public Data Session Shelves ***  */
    private List<PublicInfoBundle> infoBundleList;
    private PublicInfoBundleCECase pibCECase;

    /* *** Reporting *** */
    private Report sessionReport;
    
    private ReportConfigCECase reportConfigCECase;
    private ReportConfigCECaseList reportConfigCECaseList;
    private ReportConfigCEEventList reportConfigCEEventList;
    
    /* *** Occupancy Permit Application Session Shelves *** */
    private OccPermitApplication occPermitApplication;
    private PropertyUnit activePropUnit;
    private PropertyWithLists activePropWithLists;
    private PropertyWithLists workingPropWithLists;
    private PersonType activePersonType;
    
    private OccPeriod sessionOccPeriod;
    private List<OccPeriod> sessionOccPeriodList;
    private OccInspection sessionOccInspection;
    private OccPermit sessionOccPermit;
    
    /* *** Public Person Search/Edit Session Shelves *** */
    private Person activeAnonPerson;
    private OccPermitApplicationReason occPermitApplicationReason;

    /* *** Blob Upload Session Shelves *** */
    private List<Blob> blobList;
    //linking


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
     * @return the sessionCECase
     */
    public CECase getSessionCECase() {
        return sessionCECase;
        
    }
    
    public void refreshActiveCase() throws IntegrationException, CaseLifecyleException{
        CaseIntegrator ci = getCaseIntegrator();
        if(sessionCECase != null){
            CECase c = ci.getCECase(sessionCECase.getCaseID());
            sessionCECase = c;
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
     * @return the sessionCodeViolation
     */
    public CodeViolation getSessionCodeViolation() {
        return sessionCodeViolation;
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
     * @param sessionCECase the sessionCECase to set
     */
    public void setSessionCECase(CECase sessionCECase) {
        this.sessionCECase = sessionCECase;
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
     * @param sessionCodeViolation the sessionCodeViolation to set
     */
    public void setSessionCodeViolation(CodeViolation sessionCodeViolation) {
        this.sessionCodeViolation = sessionCodeViolation;
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
     * @return the queueCEAR
     */
    public List<CEActionRequest> getQueueCEAR() {
        
        return queueCEAR;
    }

    /**
     * @return the cECaseQueue
     */
    public List<CECase> getcECaseQueue() {
        return cECaseQueue;
    }

    /**
     * @param qc
     */
    public void setQueueCEAR(List<CEActionRequest> qc) {
        if(qc != null && qc.size() > 0 ){
            setQueryCEAR(null);
    
            this.queueCEAR = qc;
        }
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
     * @return the sessionCEAR
     */
    public CEActionRequest getSessionCEAR() {
        return sessionCEAR;
    }

    /**
     * @param sessionCEAR the sessionCEAR to set
     */
    public void setSessionCEAR(CEActionRequest sessionCEAR) {
        this.sessionCEAR = sessionCEAR;
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
    
    public PropertyWithLists getWorkingPropWithLists() {
        return workingPropWithLists;
    }

    public void setWorkingPropWithLists(PropertyWithLists workingPropWithLists) {
        this.workingPropWithLists = workingPropWithLists;
    }
    
    /*
     * @return the cEEventWCPIQueue
     */
    public List<EventCECaseCasePropBundle> getcEEventWCPIQueue() {
        return cEEventWCPIQueue;
    }

    /**
     * @param cEEventWCPIQueue the cEEventWCPIQueue to set
     */
    public void setcEEventWCPIQueue(List<EventCECaseCasePropBundle> cEEventWCPIQueue) {
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
    public List<OccInspection> getInspectionQueue() {
        return inspectionQueue;
    }

    /**
     * @param inspectionQueue the inspectionQueue to set
     */
    public void setInspectionQueue(List<OccInspection> inspectionQueue) {
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
     * @return the sessionReport
     */
    public Report getSessionReport() {
        return sessionReport;
    }

    /**
     * @param sessionReport the sessionReport to set
     */
    public void setSessionReport(Report sessionReport) {
        this.sessionReport = sessionReport;
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
     * @return the reportConfigCEEventList
     */
    public ReportConfigCEEventList getReportConfigCEEventList() {
        return reportConfigCEEventList;
    }

    /**
     * @param reportConfigCEEventList the reportConfigCEEventList to set
     */
    public void setReportConfigCEEventList(ReportConfigCEEventList reportConfigCEEventList) {
        this.reportConfigCEEventList = reportConfigCEEventList;
    }

    /**
     * @return the queryCEAR
     */
    public QueryCEAR getQueryCEAR() {
        return queryCEAR;
    }

    /**
     * @param queryCEAR the queryCEAR to set
     */
    public void setQueryCEAR(QueryCEAR queryCEAR) {
        this.queryCEAR = queryCEAR;
    }

  

   

    /**
     * @return the queryCECase
     */
    public QueryCECase getQueryCECase() {
        return queryCECase;
    }

    /**
     * @param queryCECase the queryCECase to set
     */
    public void setQueryCECase(QueryCECase queryCECase) {
        this.queryCECase = queryCECase;
    }

    /**
     * @return the activePersonType
     */
    public PersonType getActivePersonType() {
        return activePersonType;
    }

    /**
     * @param activePersonType the activePersonType to set
     */
    public void setActivePersonType(PersonType activePersonType) {
        this.activePersonType = activePersonType;
    }

    /**
     * @return the activeAnonPerson
     */
    public Person getActiveAnonPerson() {
        return activeAnonPerson;
    }

    /**
     * @param activeAnonPerson the activeAnonPerson to set
     */
    public void setActiveAnonPerson(Person activeAnonPerson) {
        this.activeAnonPerson = activeAnonPerson;
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

    /**
     * @return the sessionOccPeriod
     */
    public OccPeriod getSessionOccPeriod() {
        return sessionOccPeriod;
    }

    /**
     * @return the queryOccPeriod
     */
    public QueryOccPeriod getQueryOccPeriod() {
        return queryOccPeriod;
    }

    /**
     * @return the sessionOccPeriodList
     */
    public List<OccPeriod> getSessionOccPeriodList() {
        return sessionOccPeriodList;
    }

    /**
     * @return the sessionOccInspection
     */
    public OccInspection getSessionOccInspection() {
        return sessionOccInspection;
    }

    /**
     * @return the sessionOccPermit
     */
    public OccPermit getSessionOccPermit() {
        return sessionOccPermit;
    }

    /**
     * @param sessionOccPeriod the sessionOccPeriod to set
     */
    public void setSessionOccPeriod(OccPeriod sessionOccPeriod) {
        this.sessionOccPeriod = sessionOccPeriod;
    }

    /**
     * @param queryOccPeriod the queryOccPeriod to set
     */
    public void setQueryOccPeriod(QueryOccPeriod queryOccPeriod) {
        this.queryOccPeriod = queryOccPeriod;
    }

    /**
     * @param sessionOccPeriodList the sessionOccPeriodList to set
     */
    public void setSessionOccPeriodList(List<OccPeriod> sessionOccPeriodList) {
        this.sessionOccPeriodList = sessionOccPeriodList;
    }

    /**
     * @param sessionOccInspection the sessionOccInspection to set
     */
    public void setSessionOccInspection(OccInspection sessionOccInspection) {
        this.sessionOccInspection = sessionOccInspection;
    }

    /**
     * @param sessionOccPermit the sessionOccPermit to set
     */
    public void setSessionOccPermit(OccPermit sessionOccPermit) {
        this.sessionOccPermit = sessionOccPermit;
    }

    /**
     * @return the queryProperty
     */
    public QueryProperty getQueryProperty() {
        return queryProperty;
    }

    /**
     * @return the queryPerson
     */
    public QueryPerson getQueryPerson() {
        return queryPerson;
    }

    /**
     * @return the queryEventCECase
     */
    public QueryEventCECase getQueryEventCECase() {
        return queryEventCECase;
    }

    /**
     * @param queryProperty the queryProperty to set
     */
    public void setQueryProperty(QueryProperty queryProperty) {
        this.queryProperty = queryProperty;
    }

    /**
     * @param queryPerson the queryPerson to set
     */
    public void setQueryPerson(QueryPerson queryPerson) {
        this.queryPerson = queryPerson;
    }

    /**
     * @param queryEventCECase the queryEventCECase to set
     */
    public void setQueryEventCECase(QueryEventCECase queryEventCECase) {
        this.queryEventCECase = queryEventCECase;
    }
    
    
}
