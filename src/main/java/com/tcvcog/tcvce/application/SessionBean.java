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

import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.entities.reports.*;
import com.tcvcog.tcvce.entities.search.*;
import com.tcvcog.tcvce.entities.occupancy.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
 * @author ellen bascomb of apt 31y
 */
public class    SessionBean 
        extends BackingBeanUtils {
    
    /* >>> -------------------------------------------------------------- <<< */
    /* >>>                      N User                                    <<< */
    /* >>> -------------------------------------------------------------- <<< */
    
    private UserAuthorized sessUser;
    private User sessUserQueued;
    private User userForReInit;
    private UserAuthorizedForConfig userForConfig;
    
    private UserMuniAuthPeriod umapRequestedForReInit;
    private List<UserMuniAuthPeriod> sessUMAPListValidOnly;
    
    
    /* >>> -------------------------------------------------------------- <<< */
    /* >>>                    I Municipality                              <<< */
    /* >>> -------------------------------------------------------------- <<< */
    
    private MunicipalityDataHeavy sessMuni;
    private Municipality sessMuniQueued;
    
    
    /* >>> -------------------------------------------------------------- <<< */
    /* >>>                   III CodeBook                                 <<< */
    /* >>> -------------------------------------------------------------- <<< */
    
    private CodeSet sessCodeSet;
    private CodeSource sessCodeSource;
    private CodeElementGuideEntry activeCodeElementGuideEntry;
    private EnforcableCodeElement selectedEnfCodeElement;
    private CodeElement activeCodeElement;
    
    
    /* >>> -------------------------------------------------------------- <<< */
    /* >>>                   III Property                                 <<< */
    /* >>> -------------------------------------------------------------- <<< */
    private PropertyDataHeavy sessProperty;
    private Property sessPropertyQueued;
    private List<Property> sessPropertyList;
    
    private PropertyUnit sessPropertyUnit;
    
    private boolean startPropInfoPageWithAdd;
    
    /* >>> QUERY PROPERTY <<< */
    private QueryProperty queryProperty;
    private List<QueryProperty> queryPropertyList;
    
    /**
     * Convenience method for setting the session property with only an ID
     * @param propID
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    public void setSessProperty(int propID) throws BObStatusException, IntegrationException{
        PropertyCoordinator pc = getPropertyCoordinator();
        if(propID == 0){
            throw new BObStatusException("Prop ID cannot be 0");
        }
        pc.getProperty(propID);
    }
    
    /**
     * Convenience method for setting the session property DH with only a base
     * class instance
     * 
     * @param prop 
     */
    public void setSessProperty(Property prop){
        PropertyCoordinator pc = getPropertyCoordinator();
        try {
            sessProperty = pc.assemblePropertyDataHeavy(prop, sessUser);
        } catch (BObStatusException | IntegrationException | SearchException ex) {
            System.out.println("SessionBean.setSessionProperty: error setting session prop");
            System.out.println(ex);
        }
    }
    
    
    /* >>> -------------------------------------------------------------- <<< */
    /* >>>                     IV Person                                  <<< */
    /* >>> -------------------------------------------------------------- <<< */
    
    
    private PersonDataHeavy sessPerson;
    private Person sessPersonQueued;
    private List<Person> sessPersonList;
    
    
    /* >>> QUERY PERSON <<< */
    private QueryPerson queryPerson;
    private List<QueryPerson> queryPersonList;
    
    
    
    /* >>> -------------------------------------------------------------- <<< */
    /* >>>                   V Event                                      <<< */
    /* >>> -------------------------------------------------------------- <<< */
    
    /**
     * There is no longer a notion of a session event domain, only a requested
     * event domain for event manipulators to use as guidance to figure out 
     * if the relevant event list is extracted from the proper session business 
     * objects
     */
    private PageModeEnum sessEventsPagePageModeRequest;
    private EventDomainEnum sessEventsPageEventDomainRequest;
    private List<EventCnFPropUnitCasePeriodHeavy> sessEventList;
    private EventCnF sessEvent;
    
    
    /* >>> QUERY EVENT <<< */
    private QueryEvent queryEvent;
    private List<QueryEvent> queryEventList;
    
    
    /* >>> -------------------------------------------------------------- <<< */
    /* >>>                  VI OccPeriod                                  <<< */
    /* >>> -------------------------------------------------------------- <<< */
    
    private OccPeriodDataHeavy sessOccPeriod;
    private OccPeriod sessOccPeriodQueued;
    private List<OccPeriod> sessOccPeriodList;
    
    private OccPermit sessOccPermit;
    private OccInspection sessOccInspection;
    
    /* >>> QUERY OCCPERIOD <<< */
    private QueryOccPeriod queryOccPeriod;
    private List<QueryOccPeriod> queryOccPeriodList;
    
    
    /* >>> -------------------------------------------------------------- <<< */
    /* >>>                  VII CECaseDataHeavy                                    <<< */
    /* >>> -------------------------------------------------------------- <<< */
    
    private CECaseDataHeavy sessCECase;
    private CECase sessCECaseQueued;
    private List<CECasePropertyUnitHeavy> sessCECaseList;
    
    private PageModeEnum ceCaseSearchProfilePageModeRequest;
    private PageModeEnum ceCaseViolationsPageModeRequest; 
    private PageModeEnum ceCaseNoticesPageModeRequest; 
    private PageModeEnum ceCaseCitationsPageModeRequest; 
    
    private CodeViolation sessCodeViolation;
    private List<CodeViolation> sessViolationList;
    
    private NoticeOfViolation sessNotice;
    private Citation sessCitation;
    
    
    
    
    /* >>> QUERY CECASE <<< */
    private QueryCECase queryCECase;
    private List<QueryCECase> queryCECaseList;
    
    
    /* >>> -------------------------------------------------------------- <<< */
    /* >>>              VIII CEActionRequest                              <<< */
    /* >>> -------------------------------------------------------------- <<< */
    
    private List<CEActionRequest> sessCEARList;
    private CEActionRequest sessCEAR;
    
    /* *** Code Enf Action Request Session Shelves ***  */
    private Person personForCEActionRequestSubmission;
    private User utilityUserToUpdate;
    
    // --- QUERY CEAR ---
    private QueryCEAR queryCEAR;
    private List<QueryCEAR> queryCEARList;
    
    /* >>> -------------------------------------------------------------- <<< */
    /* >>>                     VIV OccApp                                 <<< */
    /* >>> -------------------------------------------------------------- <<< */
    
    //Fields used both externally and internally
    private OccPermitApplication sessOccPermitApplication;
    private PersonType occPermitAppActivePersonType;
    private OccPermitApplicationReason occPermitApplicationReason;
    
    //Fields used externally
    private PublicInfoBundleProperty occPermitAppActiveProp;
    private PublicInfoBundleProperty occPermitAppWorkingProp;
    private PublicInfoBundlePropertyUnit occPermitAppActivePropUnit;
    private List<PublicInfoBundlePerson> occPermitAttachedPersons;
    private PublicInfoBundlePerson occPermitApplicant;
    private PublicInfoBundlePerson occPermitPreferredContact;
    
    //Fields only used internally
    private boolean unitDetermined;
    
    /* >>> -------------------------------------------------------------- <<< */
    /* >>>                        X Payment                               <<< */
    /* >>> -------------------------------------------------------------- <<< */

    private Payment sessPayment;
    private String paymentRedirTo;
    
    private OccPeriod feeManagementOccPeriod;
    private String feeRedirTo;
    private Payment sessionPayment;
    
    private EventDomainEnum feeManagementDomain;
    private CECase feeManagementCeCase;
    
    /* >>> -------------------------------------------------------------- <<< */
    /* >>>                         XI Report                              <<< */
    /* >>> -------------------------------------------------------------- <<< */

    private Report sessReport;
    
    private ReportConfigCECase reportConfigCECase;
    private ReportConfigCECaseList reportConfigCECaseList;
    private ReportConfigCEEventList reportConfigCEEventList;
    
    private ReportConfigOccInspection reportConfigInspection;
    private ReportConfigOccPermit reportConfigOccPermit;
    
    /* >>> -------------------------------------------------------------- <<< */
    /* >>>                         XII Blob                                <<< */
    /* >>> -------------------------------------------------------------- <<< */
    
    private Blob sessBlob;
    private List<Blob> blobList;
    private PageModeEnum blobPageModeRequest;
    
    /* >>> -------------------------------------------------------------- <<< */
    /* >>>                  XIII PublicInfoBundle                          <<< */
    /* >>> -------------------------------------------------------------- <<< */
    
    private List<PublicInfoBundle> infoBundleList;
    private PublicInfoBundleCECase pibCECase;
    
    /* *** Public Person Search/Edit Session Shelves *** */
    private Person activeAnonPerson;

  
    /* *** Blob Upload Session Shelves *** */ 

    
    /* >>> -------------------------------------------------------------- <<< */
    /* >>>                  NAVIGATION                                    <<< */
    /* >>> -------------------------------------------------------------- <<< */
    
    /* *** Navigation Shelves *** */
    private NavigationStack navStack;
    
    /* >>> -------------------------------------------------------------- <<< */
    /* >>>                  SESSION SERVICES                              <<< */
    /* >>> -------------------------------------------------------------- <<< */
    
    public List<PageModeEnum> assemblePermittedPageModes(){
        
        
    // Load possible page modes
        // Thank you to Chen&Chen for designing this system
        List<PageModeEnum> pageModeOptions = new ArrayList<>();
        List<PageModeEnum> modesPossible = Arrays.asList(PageModeEnum.values());
        if(modesPossible != null && !modesPossible.isEmpty()){
            for(PageModeEnum pm: modesPossible){
                // only allow users to see modes for which they are authorized
                if(sessUser != null && pm.getMinUserRankToEnable() <= sessUser.getKeyCard().getGoverningAuthPeriod().getRole().getRank()){
                    pageModeOptions.add(pm);
                }
            }
        }
        return pageModeOptions;
    
    }
    
    /**
     * Creates a new instance of getSessionBean()
     */
    public SessionBean() {
        System.out.println("SessionBean.SessionBean");
    }
    
    
    @PostConstruct
    public void initBean(){
        System.out.println("SessionBean.initBean");
        navStack = new NavigationStack();
    }

    /**
     * @return the sessProperty
     */
    public PropertyDataHeavy getSessProperty() {
        return sessProperty;
    }

    /**
     * @return the sessCECase
     */
    public CECaseDataHeavy getSessCECase() {
        return sessCECase;
        
    }
    

   
    /**
     * @return the sessNotice
     */
    public NoticeOfViolation getSessNotice() {
        return sessNotice;
    }
    
    public void setSessCodeSet(CodeSet cs){
        sessCodeSet = cs;
    }

    

    /**
     * Adaptor method to preserve backward compatability;
     * The MuniHeavy stores the active copy of these 
     * @return the activeCodeSet
     */
    public CodeSet getSessCodeSet() {
//        if(sessMuni != null){
//            activeCodeSet = sessMuni.getCodeSet();
//        }
        return sessCodeSet;
    }

    /**
     * @return the sessCitation
     */
    public Citation getSessCitation() {
        return sessCitation;
    }

    /**
     * @return the selectedEnfCodeElement
     */
    public EnforcableCodeElement getSelectedEnfCodeElement() {
        return selectedEnfCodeElement;
    }

    /**
     * @return the sessCodeViolation
     */
    public CodeViolation getSessCodeViolation() {
        return sessCodeViolation;
    }

    /**
     * @return the sessViolationList
     */
    public List<CodeViolation> getSessViolationList() {
        return sessViolationList;
    }

    /**
     * @return the activeCodeElementGuideEntry
     */
    public CodeElementGuideEntry getActiveCodeElementGuideEntry() {
        return activeCodeElementGuideEntry;
    }

    /**
     * @param sessProperty the sessProperty to set
     */
    public void setSessProperty(PropertyDataHeavy sessProperty) {
        this.sessProperty = sessProperty;
    }

    /**
     * @param sessCECase the sessCECase to set
     */
    public void setSessCECase(CECaseDataHeavy sessCECase) {
        this.sessCECase = sessCECase;
    }



   
    /**
     * @param sessNotice the sessNotice to set
     */
    public void setSessNotice(NoticeOfViolation sessNotice) {
        this.sessNotice = sessNotice;
    }


    /**
     * @param sessCitation the sessCitation to set
     */
    public void setSessCitation(Citation sessCitation) {
        this.sessCitation = sessCitation;
    }

    /**
     * @param selectedEnfCodeElement the selectedEnfCodeElement to set
     */
    public void setSelectedEnfCodeElement(EnforcableCodeElement selectedEnfCodeElement) {
        this.selectedEnfCodeElement = selectedEnfCodeElement;
    }

    /**
     * @param sessCodeViolation the sessCodeViolation to set
     */
    public void setSessCodeViolation(CodeViolation sessCodeViolation) {
        this.sessCodeViolation = sessCodeViolation;
    }

    /**
     * @param sessViolationList the sessViolationList to set
     */
    public void setSessViolationList(List<CodeViolation> sessViolationList) {
        this.sessViolationList = sessViolationList;
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
     * @return the sessCodeSource
     */
    public CodeSource getSessCodeSource() {
        return sessCodeSource;
    }

    /**
     * @param utilityUserToUpdate the utilityUserToUpdate to set
     */
    public void setUtilityUserToUpdate(User utilityUserToUpdate) {
        this.utilityUserToUpdate = utilityUserToUpdate;
    }

    /**
     * @param sessCodeSource the sessCodeSource to set
     */
    public void setSessCodeSource(CodeSource sessCodeSource) {
        this.sessCodeSource = sessCodeSource;
    }

    /**
     * @return the sessMuni
     */
    public MunicipalityDataHeavy getSessMuni() {
        return sessMuni;
    }

    /**
     * @param sessMuni the sessMuni to set
     */
    public void setSessMuni(MunicipalityDataHeavy sessMuni) {
        this.sessMuni = sessMuni;
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
     * @return the sessCEARList
     */
    public List<CEActionRequest> getSessCEARList() {
        
        return sessCEARList;
    }

    /**
     * @return the sessCECaseList
     */
    public List<CECasePropertyUnitHeavy> getSessCECaseList() {
        return sessCECaseList;
    }

    /**
     * @param qc
     */
    public void setSessCEARList(List<CEActionRequest> qc) {
        if(qc != null && qc.size() > 0 ){
            setQueryCEAR(null);
    
            this.sessCEARList = qc;
        }
    }
    
    public void setSessCECaseListWithDowncastAndLookup(List<CECaseDataHeavy> cseldh){
        CaseCoordinator cc = getCaseCoordinator();
        List<CECasePropertyUnitHeavy> cseListPDH = new ArrayList<>();
        if(cseldh != null && !cseldh.isEmpty()){
            for(CECaseDataHeavy csedh: cseldh){
                try {
                    cseListPDH.add(cc.cecase_assembleCECasePropertyUnitHeavy(csedh));
                } catch (IntegrationException | SearchException ex) {
                    System.out.println(ex);
                }
            }
            sessCECaseList = cseListPDH;
        }
        
    }
    
    public void setSessCECase(CECase cse){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            sessCECase = cc.cecase_assembleCECaseDataHeavy(cse, sessUser);
        } catch (BObStatusException | IntegrationException | SearchException ex) {
            System.out.println(ex);
        }
        
        
    }
    
    /**
     * @param sessCECaseList the sessCECaseList to set
     */
    public void setSessCECaseList(List<CECasePropertyUnitHeavy> sessCECaseList) {
        this.sessCECaseList = sessCECaseList;
    }

    /**
     * @return the sessCEAR
     */
    public CEActionRequest getSessCEAR() {
        return sessCEAR;
    }

    /**
     * @param sessCEAR the sessCEAR to set
     */
    public void setSessCEAR(CEActionRequest sessCEAR) {
        this.sessCEAR = sessCEAR;
    }

    /**
     * @return the sessUser
     */
    
    public UserAuthorized getSessUser() {
        return sessUser;
    }

    /**
     * @param sessUser the sessUser to set
     */
    
    public void setSessUser(UserAuthorized sessUser) {
        this.sessUser = sessUser;
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

    
    public OccPermitApplication getSessOccPermitApplication() {
        return sessOccPermitApplication;
    }

    public void setSessOccPermitApplication(OccPermitApplication sessOccPermitApplication) {
        this.sessOccPermitApplication = sessOccPermitApplication;
    }

    public PropertyUnit getSessPropertyUnit() {
        return sessPropertyUnit;
    }

    public void setSessPropertyUnit(PropertyUnit sessPropertyUnit) {
        this.sessPropertyUnit = sessPropertyUnit;
    }

  

    /**
     * @return the sessPropertyList
     */
    public List<Property> getSessPropertyList() {
        return sessPropertyList;
    }

    /**
     * @param sessPropertyList the sessPropertyList to set
     */
    public void setSessPropertyList(List<Property> sessPropertyList) {
        this.sessPropertyList = sessPropertyList;
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
     * @return the sessReport
     */
    public Report getSessReport() {
        return sessReport;
    }

    /**
     * @param sessReport the sessReport to set
     */
    public void setSessReport(Report sessReport) {
        this.sessReport = sessReport;
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
     * @return the queryOccPeriod
     */
    public QueryOccPeriod getQueryOccPeriod() {
        return queryOccPeriod;
    }

    /**
     * @return the sessOccPeriodList
     */
    public List<OccPeriod> getSessOccPeriodList() {
        return sessOccPeriodList;
    }

    /**
     * @return the sessOccInspection
     */
    public OccInspection getSessOccInspection() {
        return sessOccInspection;
    }

    /**
     * @return the sessOccPermit
     */
    public OccPermit getSessOccPermit() {
        return sessOccPermit;
    }

  

    
    /**
     * @param queryOccPeriod the queryOccPeriod to set
     */
    public void setQueryOccPeriod(QueryOccPeriod queryOccPeriod) {
        this.queryOccPeriod = queryOccPeriod;
    }

    /**
     * @param sessOccPeriodList the sessOccPeriodList to set
     */
    public void setSessOccPeriodList(List<OccPeriod> sessOccPeriodList) {
        this.sessOccPeriodList = sessOccPeriodList;
    }

    /**
     * @param sessOccInspection the sessOccInspection to set
     */
    public void setSessOccInspection(OccInspection sessOccInspection) {
        this.sessOccInspection = sessOccInspection;
    }

    /**
     * @param sessOccPermit the sessOccPermit to set
     */
    public void setSessOccPermit(OccPermit sessOccPermit) {
        this.sessOccPermit = sessOccPermit;
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
     * @return the queryEvent
     */
    public QueryEvent getQueryEvent() {
        return queryEvent;
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
     * @param queryEvent the queryEvent to set
     */
    public void setQueryEvent(QueryEvent queryEvent) {
        this.queryEvent = queryEvent;
    }

    /**
     * @return the occPermitAppActiveProp
     */
    public PublicInfoBundleProperty getOccPermitAppActiveProp() {
        return occPermitAppActiveProp;
    }

    /**
     * @return the occPermitAppWorkingProp
     */
    public PublicInfoBundleProperty getOccPermitAppWorkingProp() {
        return occPermitAppWorkingProp;
    }

    /**
     * @param activeProp the occPermitAppActiveProp to set
     */
    public void setOccPermitAppActiveProp(PublicInfoBundleProperty activeProp) {
        this.occPermitAppActiveProp = activeProp;
    }

    /**
     * @param workingProp the occPermitAppWorkingProp to set
     */
    public void setOccPermitAppWorkingProp(PublicInfoBundleProperty workingProp) {
        this.occPermitAppWorkingProp = workingProp;
    }

    /**
     * @return the occPermitAppActivePropUnit
     */
    public PublicInfoBundlePropertyUnit getOccPermitAppActivePropUnit() {
        return occPermitAppActivePropUnit;
    }

    /**
     * @param occPermitAppActivePropUnit the occPermitAppActivePropUnit to set
     */
    public void setOccPermitAppActivePropUnit(PublicInfoBundlePropertyUnit occPermitAppActivePropUnit) {
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
     * @return the userForReInit
     */
    public User getUserForReInit() {
        return userForReInit;
    }

    /**
     * @param userForReInit the userForReInit to set
     */
    public void setUserForReInit(User userForReInit) {
        this.userForReInit = userForReInit;
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
     * @return the sessPayment
     */
    public Payment getSessPayment() {
        return sessPayment;
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
     * @param sessPayment the sessPayment to set
     */
    public void setSessPayment(Payment sessPayment) {
        this.sessPayment = sessPayment;
    }
    /**
     * @param feeManagementOccPeriod the feeManagementOccPeriod to set
     */
    public void setFeeManagementOccPeriod(OccPeriod feeManagementOccPeriod) {
        this.feeManagementOccPeriod = feeManagementOccPeriod;
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

    public NavigationStack getNavStack() {
        return navStack;
    }

    public void setNavStack(NavigationStack navStack) {
        this.navStack = navStack;
    }

    /**
     * @return the sessBlob
     */
    public Blob getSessBlob() {
        return sessBlob;
    }

    /**
     * @param sessBlob the sessBlob to set
     */
    public void setSessBlob(Blob sessBlob) {
        this.sessBlob = sessBlob;
    }

    /**
     * @return the sessOccPeriod
     */
    public OccPeriodDataHeavy getSessOccPeriod() {
        return sessOccPeriod;
    }

    /**
     * @param sessOccPeriod the sessOccPeriod to set
     */
    public void setSessOccPeriod(OccPeriodDataHeavy sessOccPeriod) {
        this.sessOccPeriod = sessOccPeriod;
    }
    /**
     * @param opBase     
     */
    public void setSessOccPeriod(OccPeriod opBase) {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        OccPeriodDataHeavy opdh = null;
        try {
            opdh = oc.assembleOccPeriodDataHeavy(opBase, sessUser.getKeyCard());
        } catch (IntegrationException | BObStatusException | SearchException ex) {
            System.out.println(ex);
        }
        this.sessOccPeriod = opdh;
    }

    /**
     * @return the queryPropertyList
     */
    public List<QueryProperty> getQueryPropertyList() {
        return queryPropertyList;
    }

    /**
     * @param queryPropertyList the queryPropertyList to set
     */
    public void setQueryPropertyList(List<QueryProperty> queryPropertyList) {
        this.queryPropertyList = queryPropertyList;
    }

    /**
     * @return the queryPersonList
     */
    public List<QueryPerson> getQueryPersonList() {
        return queryPersonList;
    }

    /**
     * @param queryPersonList the queryPersonList to set
     */
    public void setQueryPersonList(List<QueryPerson> queryPersonList) {
        this.queryPersonList = queryPersonList;
    }

    /**
     * @return the queryOccPeriodList
     */
    public List<QueryOccPeriod> getQueryOccPeriodList() {
        return queryOccPeriodList;
    }

    /**
     * @param queryOccPeriodList the queryOccPeriodList to set
     */
    public void setQueryOccPeriodList(List<QueryOccPeriod> queryOccPeriodList) {
        this.queryOccPeriodList = queryOccPeriodList;
    }

    /**
     * @return the queryCECaseList
     */
    public List<QueryCECase> getQueryCECaseList() {
        return queryCECaseList;
    }

    /**
     * @param queryCECaseList the queryCECaseList to set
     */
    public void setQueryCECaseList(List<QueryCECase> queryCECaseList) {
        this.queryCECaseList = queryCECaseList;
    }

    /**
     * @return the queryCEARList
     */
    public List<QueryCEAR> getQueryCEARList() {
        return queryCEARList;
    }

    /**
     * @param queryCEARList the queryCEARList to set
     */
    public void setQueryCEARList(List<QueryCEAR> queryCEARList) {
        this.queryCEARList = queryCEARList;
    }

    /**
     * @return the queryEventList
     */
    public List<QueryEvent> getQueryEventList() {
        return queryEventList;
    }

    /**
     * @param queryEventList the queryEventList to set
     */
    public void setQueryEventList(List<QueryEvent> queryEventList) {
        this.queryEventList = queryEventList;
    }

    /**
     * @return the sessEventList
     */
    public List<EventCnFPropUnitCasePeriodHeavy> getSessEventList() {
        return sessEventList;
    }

    /**
     * @param sessEventList the sessEventList to set
     */
    public void setSessEventList(List<EventCnFPropUnitCasePeriodHeavy> sessEventList) {
        this.sessEventList = sessEventList;
    }

    /**
     * @return the sessUMAPListValidOnly
     */
    public List<UserMuniAuthPeriod> getSessUMAPListValidOnly() {
        return sessUMAPListValidOnly;
    }

    /**
     * @param sessUMAPListValidOnly the sessUMAPListValidOnly to set
     */
    public void setSessUMAPListValidOnly(List<UserMuniAuthPeriod> sessUMAPListValidOnly) {
        this.sessUMAPListValidOnly = sessUMAPListValidOnly;
    }

    /**
     * @return the sessPerson
     */
    public PersonDataHeavy getSessPerson() {
        return sessPerson;
    }

    /**
     * @param sessPerson the sessPerson to set
     */
    public void setSessPerson(PersonDataHeavy sessPerson) {
        this.sessPerson = sessPerson;
    }

    /**
     * @return the sessPersonQueued
     */
    public Person getSessPersonQueued() {
        return sessPersonQueued;
    }

    /**
     * @param sessPersonQueued the sessPersonQueued to set
     */
    public void setSessPersonQueued(Person sessPersonQueued) {
        this.sessPersonQueued = sessPersonQueued;
    }

    /**
     * @return the sessPersonList
     */
    public List<Person> getSessPersonList() {
        return sessPersonList;
    }

    /**
     * @param sessPersonList the sessPersonList to set
     */
    public void setSessPersonList(List<Person> sessPersonList) {
        this.sessPersonList = sessPersonList;
    }

    /**
     * @return the sessUserQueued
     */
    public User getSessUserQueued() {
        return sessUserQueued;
    }

    /**
     * @param sessUserQueued the sessUserQueued to set
     */
    public void setSessUserQueued(User sessUserQueued) {
        this.sessUserQueued = sessUserQueued;
    }

    /**
     * @return the sessMuniQueued
     */
    public Municipality getSessMuniQueued() {
        return sessMuniQueued;
    }

    /**
     * @param sessMuniQueued the sessMuniQueued to set
     */
    public void setSessMuniQueued(Municipality sessMuniQueued) {
        this.sessMuniQueued = sessMuniQueued;
    }

    /**
     * @return the sessPropertyQueued
     */
    public Property getSessPropertyQueued() {
        return sessPropertyQueued;
    }

    /**
     * @param sessPropertyQueued the sessPropertyQueued to set
     */
    public void setSessPropertyQueued(Property sessPropertyQueued) {
        this.sessPropertyQueued = sessPropertyQueued;
    }

    /**
     * @return the sessEvent
     */
    public EventCnF getSessEvent() {
        return sessEvent;
    }

    /**
     * @param sessEvent the sessEvent to set
     */
    public void setSessEvent(EventCnF sessEvent) {
        this.sessEvent = sessEvent;
    }

    /**
     * @return the sessOccPeriodQueued
     */
    public OccPeriod getSessOccPeriodQueued() {
        return sessOccPeriodQueued;
    }

    /**
     * @param sessOccPeriodQueued the sessOccPeriodQueued to set
     */
    public void setSessOccPeriodQueued(OccPeriod sessOccPeriodQueued) {
        this.sessOccPeriodQueued = sessOccPeriodQueued;
    }

    /**
     * @return the sessCECaseQueued
     */
    public CECase getSessCECaseQueued() {
        return sessCECaseQueued;
    }

    /**
     * @param sessCECaseQueued the sessCECaseQueued to set
     */
    public void setSessCECaseQueued(CECase sessCECaseQueued) {
        this.sessCECaseQueued = sessCECaseQueued;
    }

    /**
     * @return the paymentRedirTo
     */
    public String getPaymentRedirTo() {
        return paymentRedirTo;
    }

    /**
     * @return the sessionPayment
     */
    public Payment getSessionPayment() {
        return sessionPayment;
    }

    /**
     * @param paymentRedirTo the paymentRedirTo to set
     */
    public void setPaymentRedirTo(String paymentRedirTo) {
        this.paymentRedirTo = paymentRedirTo;
    }

    /**
     * @param sessionPayment the sessionPayment to set
     */
    public void setSessionPayment(Payment sessionPayment) {
        this.sessionPayment = sessionPayment;
    }

    /**
     * @return the sessEventsPageEventDomainRequest
     */
    public EventDomainEnum getSessEventsPageEventDomainRequest() {
        return sessEventsPageEventDomainRequest;
    }

    /**
     * @param sessEventsPageEventDomainRequest the sessEventsPageEventDomainRequest to set
     */
    public void setSessEventsPageEventDomainRequest(EventDomainEnum sessEventsPageEventDomainRequest) {
        this.sessEventsPageEventDomainRequest = sessEventsPageEventDomainRequest;
    }

    /**
     * @return the startPropInfoPageWithAdd
     */
    public boolean isStartPropInfoPageWithAdd() {
        return startPropInfoPageWithAdd;
    }

    /**
     * @param startPropInfoPageWithAdd the startPropInfoPageWithAdd to set
     */
    public void setStartPropInfoPageWithAdd(boolean startPropInfoPageWithAdd) {
        this.startPropInfoPageWithAdd = startPropInfoPageWithAdd;
    }
    
    public List<PublicInfoBundlePerson> getOccPermitAttachedPersons() {
        return occPermitAttachedPersons;
    }

    public void setOccPermitAttachedPersons(List<PublicInfoBundlePerson> occPermitAttachedPersons) {
        this.occPermitAttachedPersons = occPermitAttachedPersons;
    }

    public PublicInfoBundlePerson getOccPermitApplicant() {
        return occPermitApplicant;
    }

    public void setOccPermitApplicant(PublicInfoBundlePerson occPermitApplicant) {
        this.occPermitApplicant = occPermitApplicant;
    }

    public PublicInfoBundlePerson getOccPermitPreferredContact() {
        return occPermitPreferredContact;
    }

    public void setOccPermitPreferredContact(PublicInfoBundlePerson occPermitPreferredContact) {
        this.occPermitPreferredContact = occPermitPreferredContact;
    }

    public boolean isUnitDetermined() {
        return unitDetermined;
    }

    public void setUnitDetermined(boolean unitDetermined) {
        this.unitDetermined = unitDetermined;
    }
    
    /**
     * @return the userForConfig
     */
    public UserAuthorizedForConfig getUserForConfig() {
        return userForConfig;
    }

    /**
     * @param userForConfig the userForConfig to set
     */
    public void setUserForConfig(UserAuthorizedForConfig userForConfig) {
        this.userForConfig = userForConfig;
    }

    /**
     * @return the ceCaseViolationsPageModeRequest
     */
    public PageModeEnum getCeCaseViolationsPageModeRequest() {
        return ceCaseViolationsPageModeRequest;
    }

    /**
     * @return the ceCaseNoticesPageModeRequest
     */
    public PageModeEnum getCeCaseNoticesPageModeRequest() {
        return ceCaseNoticesPageModeRequest;
    }

    /**
     * @return the ceCaseCitationsPageModeRequest
     */
    public PageModeEnum getCeCaseCitationsPageModeRequest() {
        return ceCaseCitationsPageModeRequest;
    }

    /**
     * @param ceCaseViolationsPageModeRequest the ceCaseViolationsPageModeRequest to set
     */
    public void setCeCaseViolationsPageModeRequest(PageModeEnum ceCaseViolationsPageModeRequest) {
        this.ceCaseViolationsPageModeRequest = ceCaseViolationsPageModeRequest;
    }

    /**
     * @param ceCaseNoticesPageModeRequest the ceCaseNoticesPageModeRequest to set
     */
    public void setCeCaseNoticesPageModeRequest(PageModeEnum ceCaseNoticesPageModeRequest) {
        this.ceCaseNoticesPageModeRequest = ceCaseNoticesPageModeRequest;
    }

    /**
     * @param ceCaseCitationsPageModeRequest the ceCaseCitationsPageModeRequest to set
     */
    public void setCeCaseCitationsPageModeRequest(PageModeEnum ceCaseCitationsPageModeRequest) {
        this.ceCaseCitationsPageModeRequest = ceCaseCitationsPageModeRequest;
    }

    /**
     * @return the sessEventsPagePageModeRequest
     */
    public PageModeEnum getSessEventsPagePageModeRequest() {
        return sessEventsPagePageModeRequest;
    }

    /**
     * @param sessEventsPagePageModeRequest the sessEventsPagePageModeRequest to set
     */
    public void setSessEventsPagePageModeRequest(PageModeEnum sessEventsPagePageModeRequest) {
        this.sessEventsPagePageModeRequest = sessEventsPagePageModeRequest;
    }

    /**
     * @return the blobPageModeRequest
     */
    public PageModeEnum getBlobPageModeRequest() {
        return blobPageModeRequest;
    }

    /**
     * @param blobPageModeRequest the blobPageModeRequest to set
     */
    public void setBlobPageModeRequest(PageModeEnum blobPageModeRequest) {
        this.blobPageModeRequest = blobPageModeRequest;
    }

    /**
     * @return the ceCaseSearchProfilePageModeRequest
     */
    public PageModeEnum getCeCaseSearchProfilePageModeRequest() {
        return ceCaseSearchProfilePageModeRequest;
    }

    /**
     * @param ceCaseSearchProfilePageModeRequest the ceCaseSearchProfilePageModeRequest to set
     */
    public void setCeCaseSearchProfilePageModeRequest(PageModeEnum ceCaseSearchProfilePageModeRequest) {
        this.ceCaseSearchProfilePageModeRequest = ceCaseSearchProfilePageModeRequest;
    }
    
}
