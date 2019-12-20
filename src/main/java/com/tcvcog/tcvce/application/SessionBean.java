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

import com.tcvcog.tcvce.domain.CaseLifecycleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.entities.reports.Report;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECase;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECaseList;
import com.tcvcog.tcvce.entities.reports.ReportConfigCEEventList;
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
import com.tcvcog.tcvce.entities.NoticeOfViolation;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PublicInfoBundle;
import com.tcvcog.tcvce.entities.PublicInfoBundleCECase;
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
import com.tcvcog.tcvce.entities.reports.ReportConfigOccInspection;
import com.tcvcog.tcvce.entities.reports.ReportConfigOccPermit;
import com.tcvcog.tcvce.entities.search.QueryOccPeriod;
import com.tcvcog.tcvce.entities.search.QueryPerson;
import com.tcvcog.tcvce.entities.search.QueryProperty;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;

/**
 * Stores member vars of pretty much all our custom types
 * for persistence across an entire session (i.e. across page changes)
 * Many backing beans will grab this SessionBean in their initBean() method
 * and check for the presence of a session object. If not null, the method injects
 * those objects into its own members. If null, beans will decide if they need an object
 * and where to get it.
 * 
 * When many beans facilitate navigation to other pages, they will put their working
 * object on one of these session shelves for others to work with and to maintain
 * user state across page changes.
 * 
 * @author Eric C. Darsow
 */
public class SessionBean extends BackingBeanUtils implements Serializable{
    
    // BOB individual object session shelves - NOT NULL
    private MunicipalityDataHeavy sessionMuni;
    private UserAuthorized sessionUser;
    private User sessionUserForReInitSession;
    private UserMuniAuthPeriod umapRequestedForReInit;
    
    private CECase sessionCECase;
    private Property sessionProperty;
    private Person sessionPerson;
    private OccPeriod sessionOccPeriod;
    
    // BOB individual object session shelves - NOT ALWAYS POPULATED
    private CEActionRequest sessionCEAR;
    private PropertyUnit sessionPropertyUnit;
    private OccInspection sessionOccInspection;
    private OccPermit sessionOccPermit;
    
    // CECase-specific objects
    private NoticeOfViolation sessionNotice;
    private Citation sessionCitation;
    private CodeViolation sessionCodeViolation;
    
    // BOB Lists
    private List<Property> sessionPropertyList;
    private List<Person> sessionPersonList;
    private List<CEActionRequest> sessionCEARList;
    private List<CECase> sessionCECaseList;
    private List<EventCECaseCasePropBundle> sessionEventWithCasePropList;
    private List<CodeViolation> sessionViolationList;
    private List<OccPeriod> sessionOccPeriodList;
    private List<Blob> blobList;
    
    // BOB queries
    private QueryProperty queryProperty;
    private QueryPerson queryPerson;
    private QueryCEAR queryCEAR;
    private QueryCECase queryCECase;
    private QueryEventCECase queryEventCECase;
    private QueryOccPeriod queryOccPeriod;
    
    /* *** Municipal Code Session Shelves ***  */
    private CodeSource activeCodeSource;
    private CodeSet activeCodeSet;
    private CodeElementGuideEntry activeCodeElementGuideEntry;
    private EnforcableCodeElement selectedEnfCodeElement;
    private CodeElement activeCodeElement;
    
    /* *** Occupancy Permit Application Session Shelves *** */
    private OccPermitApplication sessionOccPermitApplication;
    private Property occPermitAppActiveProp;
    private Property occPermitAppWorkingProp;
    private PropertyUnit occPermitAppActivePropUnit;
    private PersonType occPermitAppActivePersonType;
    
    /* *** Code Enf Action Request Session Shelves ***  */
    private Person personForCEActionRequestSubmission;
    private User utilityUserToUpdate;
    private CEActionRequest ceactionRequestForSubmission;
    
    
    /* *** Public Data Session Shelves ***  */
    private List<PublicInfoBundle> infoBundleList;
    private PublicInfoBundleCECase pibCECase;

    /* *** Reporting *** */
    private Report sessionReport;
    
    private ReportConfigCECase reportConfigCECase;
    private ReportConfigCECaseList reportConfigCECaseList;
    private ReportConfigCEEventList reportConfigCEEventList;
    
    private ReportConfigOccInspection reportConfigInspection;
    private ReportConfigOccPermit reportConfigOccPermit;
    
    /* *** Public Person Search/Edit Session Shelves *** */
    private Person activeAnonPerson;
    private OccPermitApplicationReason occPermitApplicationReason;

    /* *** Payment and Fee Management Shelves *** */
    private Payment sessionPayment;
    private String paymentRedirTo;
    
    private EventDomainEnum feeManagementDomain;
    private CECase feeManagementCeCase;
    private OccPeriod feeManagementOccPeriod;
    private String feeRedirTo;
    /* *** Blob Upload Session Shelves *** */
    //linking


    /**
     * Creates a new instance of getSessionBean()
     */
    public SessionBean() {
        System.out.println("SessionBean.SessionBean");
    }
    
    
    @PostConstruct
    public void initBean(){
        System.out.println("SessionBean.initBean");
    }

    /**
     * @return the sessionProperty
     */
    public Property getSessionProperty() {
        return sessionProperty;
    }

    /**
     * @return the sessionCECase
     */
    public CECase getSessionCECase() {
        return sessionCECase;
        
    }
    
    public void refreshActiveCase() throws IntegrationException, CaseLifecycleException{
        CaseIntegrator ci = getCaseIntegrator();
        if(sessionCECase != null){
            CECase c = ci.getCECase(sessionCECase.getCaseID());
            sessionCECase = c;
        }
    }

    /**
     * @return the sessionPerson
     */
    public Person getSessionPerson() {
        return sessionPerson;
    }

   
    /**
     * @return the sessionNotice
     */
    public NoticeOfViolation getSessionNotice() {
        return sessionNotice;
    }
    
    public void setActiveCodeSet(CodeSet cs){
        activeCodeSet = cs;
    }

    

    /**
     * Adaptor method to preserve backward compatability;
     * The MuniHeavy stores the active copy of these 
     * @return the activeCodeSet
     */
    public CodeSet getActiveCodeSet() {
//        if(sessionMuni != null){
//            activeCodeSet = sessionMuni.getCodeSet();
//        }
        return activeCodeSet;
    }

    /**
     * @return the sessionCitation
     */
    public Citation getSessionCitation() {
        return sessionCitation;
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
     * @return the sessionViolationList
     */
    public List<CodeViolation> getSessionViolationList() {
        return sessionViolationList;
    }

    /**
     * @return the activeCodeElementGuideEntry
     */
    public CodeElementGuideEntry getActiveCodeElementGuideEntry() {
        return activeCodeElementGuideEntry;
    }

    /**
     * @param sessionProperty the sessionProperty to set
     */
    public void setSessionProperty(Property sessionProperty) {
        this.sessionProperty = sessionProperty;
    }

    /**
     * @param sessionCECase the sessionCECase to set
     */
    public void setSessionCECase(CECase sessionCECase) {
        this.sessionCECase = sessionCECase;
    }



    /**
     * @param sessionPerson the sessionPerson to set
     */
    public void setSessionPerson(Person sessionPerson) {
        this.sessionPerson = sessionPerson;
    }

   
    /**
     * @param sessionNotice the sessionNotice to set
     */
    public void setSessionNotice(NoticeOfViolation sessionNotice) {
        this.sessionNotice = sessionNotice;
    }


    /**
     * @param sessionCitation the sessionCitation to set
     */
    public void setSessionCitation(Citation sessionCitation) {
        this.sessionCitation = sessionCitation;
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
     * @param sessionViolationList the sessionViolationList to set
     */
    public void setSessionViolationList(List<CodeViolation> sessionViolationList) {
        this.sessionViolationList = sessionViolationList;
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
     * @return the sessionMuni
     */
    public MunicipalityDataHeavy getSessionMuni() {
        return sessionMuni;
    }

    /**
     * @param sessionMuni the sessionMuni to set
     */
    public void setSessionMuni(MunicipalityDataHeavy sessionMuni) {
        this.sessionMuni = sessionMuni;
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
     * @return the sessionCEARList
     */
    public List<CEActionRequest> getSessionCEARList() {
        
        return sessionCEARList;
    }

    /**
     * @return the sessionCECaseList
     */
    public List<CECase> getSessionCECaseList() {
        return sessionCECaseList;
    }

    /**
     * @param qc
     */
    public void setSessionCEARList(List<CEActionRequest> qc) {
        if(qc != null && qc.size() > 0 ){
            setQueryCEAR(null);
    
            this.sessionCEARList = qc;
        }
    }
    

    /**
     * @param sessionCECaseList the sessionCECaseList to set
     */
    public void setSessionCECaseList(List<CECase> sessionCECaseList) {
        this.sessionCECaseList = sessionCECaseList;
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
     * @return the sessionUser
     */
    
    public UserAuthorized getSessionUser() {
        return sessionUser;
    }

    /**
     * @param sessionUser the sessionUser to set
     */
    
    public void setSessionUser(UserAuthorized sessionUser) {
        this.sessionUser = sessionUser;
    }

    /**
     * @return the sessionPersonList
     */
    public List<Person> getSessionPersonList() {
        return sessionPersonList;
    }

    /**
     * @param sessionPersonList the sessionPersonList to set
     */
    public void setSessionPersonList(List<Person> sessionPersonList) {
        this.sessionPersonList = sessionPersonList;
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

    
    public OccPermitApplication getSessionOccPermitApplication() {
        return sessionOccPermitApplication;
    }

    public void setSessionOccPermitApplication(OccPermitApplication sessionOccPermitApplication) {
        this.sessionOccPermitApplication = sessionOccPermitApplication;
    }

    public PropertyUnit getSessionPropertyUnit() {
        return sessionPropertyUnit;
    }

    public void setSessionPropertyUnit(PropertyUnit sessionPropertyUnit) {
        this.sessionPropertyUnit = sessionPropertyUnit;
    }

  
    
    /*
     * @return the sessionEventWithCasePropList
     */
    public List<EventCECaseCasePropBundle> getSessionEventWithCasePropList() {
        return sessionEventWithCasePropList;
    }

    /**
     * @param sessionEventWithCasePropList the sessionEventWithCasePropList to set
     */
    public void setSessionEventWithCasePropList(List<EventCECaseCasePropBundle> sessionEventWithCasePropList) {
        this.sessionEventWithCasePropList = sessionEventWithCasePropList;
    }

    /**
     * @return the sessionPropertyList
     */
    public List<Property> getSessionPropertyList() {
        return sessionPropertyList;
    }

    /**
     * @param sessionPropertyList the sessionPropertyList to set
     */
    public void setSessionPropertyList(List<Property> sessionPropertyList) {
        this.sessionPropertyList = sessionPropertyList;
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

    /**
     * @return the occPermitAppActiveProp
     */
    public Property getOccPermitAppActiveProp() {
        return occPermitAppActiveProp;
    }

    /**
     * @return the occPermitAppWorkingProp
     */
    public Property getOccPermitAppWorkingProp() {
        return occPermitAppWorkingProp;
    }

    /**
     * @param activeProp the occPermitAppActiveProp to set
     */
    public void setOccPermitAppActiveProp(Property activeProp) {
        this.occPermitAppActiveProp = activeProp;
    }

    /**
     * @param workingProp the occPermitAppWorkingProp to set
     */
    public void setOccPermitAppWorkingProp(Property workingProp) {
        this.occPermitAppWorkingProp = workingProp;
    }

    /**
     * @return the occPermitAppActivePropUnit
     */
    public PropertyUnit getOccPermitAppActivePropUnit() {
        return occPermitAppActivePropUnit;
    }

    /**
     * @param occPermitAppActivePropUnit the occPermitAppActivePropUnit to set
     */
    public void setOccPermitAppActivePropUnit(PropertyUnit occPermitAppActivePropUnit) {
        this.occPermitAppActivePropUnit = occPermitAppActivePropUnit;
    }

    /**
     * @return the occPermitAppActivePersonType
     */
    public PersonType getOccPermitAppActivePersonType() {
        return occPermitAppActivePersonType;
    }

    /**
     * @param occPermitAppActivePersonType the occPermitAppActivePersonType to set
     */
    public void setOccPermitAppActivePersonType(PersonType occPermitAppActivePersonType) {
        this.occPermitAppActivePersonType = occPermitAppActivePersonType;
    }

    /**
     * @return the reportConfigOccPermit
     */
    public ReportConfigOccPermit getReportConfigOccPermit() {
        return reportConfigOccPermit;
    }

    /**
     * @param reportConfigOccPermit the reportConfigOccPermit to set
     */
    public void setReportConfigOccPermit(ReportConfigOccPermit reportConfigOccPermit) {
        this.reportConfigOccPermit = reportConfigOccPermit;
    }

    /**
     * @return the reportConfigInspection
     */
    public ReportConfigOccInspection getReportConfigInspection() {
        return reportConfigInspection;
    }

    /**
     * @param reportConfigInspection the reportConfigInspection to set
     */
    public void setReportConfigInspection(ReportConfigOccInspection reportConfigInspection) {
        this.reportConfigInspection = reportConfigInspection;
    }

    /**
     * @return the sessionUserForReInitSession
     */
    public User getSessionUserForReInitSession() {
        return sessionUserForReInitSession;
    }

    /**
     * @param sessionUserForReInitSession the sessionUserForReInitSession to set
     */
    public void setSessionUserForReInitSession(User sessionUserForReInitSession) {
        this.sessionUserForReInitSession = sessionUserForReInitSession;
    }

    /**
     * @return the umapRequestedForReInit
     */
    public UserMuniAuthPeriod getUmapRequestedForReInit() {
        return umapRequestedForReInit;
    }

    /**
     * @param umapRequestedForReInit the umapRequestedForReInit to set
     */
    public void setUmapRequestedForReInit(UserMuniAuthPeriod umapRequestedForReInit) {
        this.umapRequestedForReInit = umapRequestedForReInit;
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

    /**
     * @return the sessionPayment
     */
    public Payment getSessionPayment() {
        return sessionPayment;
    }

    /**
     * @return the paymentRedirTo
     */
    public String getPaymentRedirTo() {
        return paymentRedirTo;
    }

    /**
     * @return the feeManagementOccPeriod
     */
    public OccPeriod getFeeManagementOccPeriod() {
        return feeManagementOccPeriod;
    }

    /**
     * @return the feeRedirTo
     */
    public String getFeeRedirTo() {
        return feeRedirTo;
    }

    /**
     * @param sessionPayment the sessionPayment to set
     */
    public void setSessionPayment(Payment sessionPayment) {
        this.sessionPayment = sessionPayment;
    }

    /**
     * @param paymentRedirTo the paymentRedirTo to set
     */
    public void setPaymentRedirTo(String paymentRedirTo) {
        this.paymentRedirTo = paymentRedirTo;
    }

    /**
     * @param feeManagementOccPeriod the feeManagementOccPeriod to set
     */
    public void setFeeManagementOccPeriod(OccPeriod feeManagementOccPeriod) {
        this.feeManagementOccPeriod = feeManagementOccPeriod;
    }

    /**
     * @param feeRedirTo the feeRedirTo to set
     */
    public void setFeeRedirTo(String feeRedirTo) {
        this.feeRedirTo = feeRedirTo;
    }

    public CECase getFeeManagementCeCase() {
        return feeManagementCeCase;
    }

    public void setFeeManagementCeCase(CECase feeManagementCeCase) {
        this.feeManagementCeCase = feeManagementCeCase;
    }

    public EventDomainEnum getFeeManagementDomain() {
        return feeManagementDomain;
    }

    public void setFeeManagementDomain(EventDomainEnum feeManagementDomain) {
        this.feeManagementDomain = feeManagementDomain;
    }
    
}
