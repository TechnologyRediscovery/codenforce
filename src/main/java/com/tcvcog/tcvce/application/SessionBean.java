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
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Citation;
import com.tcvcog.tcvce.entities.CodeElement;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.CodeElementGuideEntry;
import com.tcvcog.tcvce.entities.CodeSet;
import com.tcvcog.tcvce.entities.CodeSource;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EventCECase;
import com.tcvcog.tcvce.entities.AccessKeyCard;
import com.tcvcog.tcvce.entities.EventWithCasePropInfo;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.NoticeOfViolation;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Photograph;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyWithLists;
import com.tcvcog.tcvce.entities.PublicInfoBundle;
import com.tcvcog.tcvce.entities.PublicInfoBundleCECase;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.occupancy.entities.OccPermitApplication;
import com.tcvcog.tcvce.occupancy.entities.OccPermitApplicationReason;
import com.tcvcog.tcvce.occupancy.entities.OccPermitType;
import java.io.Serializable;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 *       //@ManagedBean(name="sessionBean")
 *       //@SessionScoped
 * @author Eric C. Darsow
 */
public class SessionBean extends BackingBeanUtils implements Serializable{
    
    // primary security authoriziation container 
    // TODO - remove and get the keycard directly from the User stored in the session
    private AccessKeyCard accessKeyCard;
    
    /* *** System Core Objects Session Shelves ***  */
    private Municipality activeMuni;
    private List<Municipality> availableMuniList;
    private User facesUser;
    private Property activeProp;
    private Person activePerson;
    private List<Person> activePersonList;
    private PropertyWithLists activePropWithList;
    
    /* *** Municipal Code Session Shelves ***  */
    private CodeSource activeCodeSource;
    private CodeSet activeCodeSet;
    private CodeElementGuideEntry activeCodeElementGuideEntry;
    private EnforcableCodeElement selectedEnfCodeElement;
    private CodeElement activeCodeElement;
    
    /* *** Code Enf Action Request Session Shelves ***  */
    
    private Person personForCEActionRequestSubmission;
    
    
    // temporary
    private User utilityUserToUpdate;
    private CEActionRequest ceactionRequestForSubmission;
    private CEActionRequest activeRequest;
    private List<CEActionRequest> cEActionRequestList;
    
    private EventCECase complianceTimeframeClosingEvent;
    private List<EventCECase> complianceTimeframeClosingEventList;
    
    private EventCECase noticeEvent;
    private List<EventCECase> noticeEventList;
    
    /* *** Code Enforcement Case Session Shelves ***  */
    private CECase cECase;
    private List<CECase> cECaseList;
    
    private EventCECase activeEvent;
    private List<EventWithCasePropInfo> activeFancyCEEventList;
    private List<CodeViolation> activeViolationList;
    private NoticeOfViolation activeNotice;
    private Citation activeCitation;
    private CodeViolation activeCodeViolation;
    
    /* *** Public Data Session Shelves ***  */
    private List<PublicInfoBundle> infoBundleList;
    private PublicInfoBundleCECase pibCECase;
    
    
    /* *** Occupancy Permit Application Session Shelves *** */
    private OccPermitApplication occPermitApplication;
    private PropertyUnit activePropUnit;
    private PropertyWithLists activePropWithLists;
    private OccPermitApplicationReason occPermitApplicationReason;
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
     * @return the activeEvent
     */
    public EventCECase getActiveEvent() {
        return activeEvent;
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
     * @return the activeViolationList
     */
    public List<CodeViolation> getActiveViolationList() {
        return activeViolationList;
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
     * @param activeEvent the activeEvent to set
     */
    public void setActiveEvent(EventCECase activeEvent) {
        this.activeEvent = activeEvent;
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
     * @param activeViolationList the activeViolationList to set
     */
    public void setActiveViolationList(List<CodeViolation> activeViolationList) {
        this.activeViolationList = activeViolationList;
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
     * @return the accessKeyCard
     */
    public AccessKeyCard getAccessKeyCard() {
        return accessKeyCard;
    }

    /**
     * @param accessKeyCard the accessKeyCard to set
     */
    public void setAccessKeyCard(AccessKeyCard accessKeyCard) {
        this.accessKeyCard = accessKeyCard;
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
     * @return the cEActionRequestList
     */
    public List<CEActionRequest> getcEActionRequestList() {
        return cEActionRequestList;
    }

    /**
     * @return the complianceTimeframeClosingEvent
     */
    public EventCECase getComplianceTimeframeClosingEvent() {
        return complianceTimeframeClosingEvent;
    }

    /**
     * @return the complianceTimeframeClosingEventList
     */
    public List<EventCECase> getComplianceTimeframeClosingEventList() {
        return complianceTimeframeClosingEventList;
    }

    /**
     * @return the noticeEvent
     */
    public EventCECase getNoticeEvent() {
        return noticeEvent;
    }

    /**
     * @return the noticeEventList
     */
    public List<EventCECase> getNoticeEventList() {
        return noticeEventList;
    }

    /**
     * @return the cECaseList
     */
    public List<CECase> getcECaseList() {
        return cECaseList;
    }

    /**
     * @param cEActionRequestList the cEActionRequestList to set
     */
    public void setcEActionRequestList(List<CEActionRequest> cEActionRequestList) {
        this.cEActionRequestList = cEActionRequestList;
    }

    /**
     * @param complianceTimeframeClosingEvent the complianceTimeframeClosingEvent to set
     */
    public void setComplianceTimeframeClosingEvent(EventCECase complianceTimeframeClosingEvent) {
        this.complianceTimeframeClosingEvent = complianceTimeframeClosingEvent;
    }

    /**
     * @param complianceTimeframeClosingEventList the complianceTimeframeClosingEventList to set
     */
    public void setComplianceTimeframeClosingEventList(List<EventCECase> complianceTimeframeClosingEventList) {
        this.complianceTimeframeClosingEventList = complianceTimeframeClosingEventList;
    }

    /**
     * @param noticeEvent the noticeEvent to set
     */
    public void setNoticeEvent(EventCECase noticeEvent) {
        this.noticeEvent = noticeEvent;
    }

    /**
     * @param noticeEventList the noticeEventList to set
     */
    public void setNoticeEventList(List<EventCECase> noticeEventList) {
        this.noticeEventList = noticeEventList;
    }

    /**
     * @param cECaseList the cECaseList to set
     */
    public void setcECaseList(List<CECase> cECaseList) {
        this.cECaseList = cECaseList;
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
     * @return the availableMuniList
     */
    public List<Municipality> getAvailableMuniList() {
        return availableMuniList;
    }

    /**
     * @param availableMuniList the availableMuniList to set
     */
    public void setAvailableMuniList(List<Municipality> availableMuniList) {
        this.availableMuniList = availableMuniList;
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
     * @return the activePersonList
     */
    public List<Person> getActivePersonList() {
        return activePersonList;
    }

    /**
     * @param activePersonList the activePersonList to set
     */
    public void setActivePersonList(List<Person> activePersonList) {
        this.activePersonList = activePersonList;
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
     * @return the activeFancyCEEventList
     */
    public List<EventWithCasePropInfo> getActiveFancyCEEventList() {
        return activeFancyCEEventList;
    }

    /**
     * @param activeFancyCEEventList the activeFancyCEEventList to set
     */
    public void setActiveFancyCEEventList(List<EventWithCasePropInfo> activeFancyCEEventList) {
        this.activeFancyCEEventList = activeFancyCEEventList;
    }
    
}
