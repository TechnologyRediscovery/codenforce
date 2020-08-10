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
import com.tcvcog.tcvce.coordinators.MunicipalityCoordinator;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.ExceptionSeverityEnum;
import com.tcvcog.tcvce.domain.SessionException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Credential;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.MunicipalityDataHeavy;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.UserMuniAuthPeriod;
import com.tcvcog.tcvce.entities.UserMuniAuthPeriodLogEntry;
import com.tcvcog.tcvce.entities.UserMuniAuthPeriodLogEntryCatEnum;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.search.QueryCEAR;
import com.tcvcog.tcvce.entities.search.QueryCEAREnum;
import com.tcvcog.tcvce.entities.search.QueryCECase;
import com.tcvcog.tcvce.entities.search.QueryCECaseEnum;
import com.tcvcog.tcvce.integration.UserIntegrator;
import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.SubSysEnum;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * *******************************
 * ***    SECURITY CRITICAL    ***
 * *******************************
 * 
 * Central backing bean and psuedo-coordinator at the Session level
 * that processes the requested UserMuniAuthPeriod and builds an entire
 * session full of business objects once security checks are passed
 * 
 * As expected, this class works in tandem with the UserCoordinator
 * to manage the UserMuniAuthorizationPeriod list, one of which is chosen
 * and becomes and television program!
 * 
 * Contents in brief:
 * <ul>
 * <li>User-centric login and session initialization methods</li>
 * <li>Subsystem initialization controller</li>
 * <li>A heap of subsystem-specific initialization methods</li>
 * </ul>
 * @author ellen bascomb of apt 31y
 */
public  class       SessionInitializer 
        extends     BackingBeanUtils 
        implements  Serializable {

    private String usernameQueuedForSession;
    private UserAuthorized userAuthorizedQueuedForSession;
    private List<UserMuniAuthPeriod> umapCandidateList;
    private UserMuniAuthPeriod umapQueuedForSession;
    private Municipality muniQueuedForSession;
    private SessionBean sb;
    
    /**
     * Creates a new instance of SessionInitializer
     */
    public SessionInitializer() {
    }
    
    @PostConstruct
    public void initBean(){
        sb = getSessionBean();
        System.out.println("SessionInitializer.initBean");
        userAuthorizedQueuedForSession = null;
        User tmpUser = null;
        UserCoordinator uc = getUserCoordinator();
        UserIntegrator ui = getUserIntegrator();
        // check to see if we have an internal session created already
        // to determine which user we authenticate with
        
        if(sb.getUserForReInit() != null){
            usernameQueuedForSession = sb.getUserForReInit().getUsername();
        } else if(usernameQueuedForSession == null){
            try {
                // we have a first init! Ask the container for its user
                usernameQueuedForSession = sessionInit_getContainerAuthenticatedUser();
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
        } 
        try {
            tmpUser = ui.getUser(ui.getUserID(usernameQueuedForSession));
            userAuthorizedQueuedForSession = uc.auth_prepareUserForSessionChoice(tmpUser);
            sb.setSessUser(userAuthorizedQueuedForSession);
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
        umapCandidateList = new ArrayList<>();
        
        List<Municipality> tempMuniList;
        if(userAuthorizedQueuedForSession != null && userAuthorizedQueuedForSession.getMuniAuthPeriodsMap() != null){
            
            Set<Municipality> muniSet = userAuthorizedQueuedForSession.getMuniAuthPeriodsMap().keySet();
            if(!muniSet.isEmpty()){
                tempMuniList = new ArrayList(muniSet);
                for(Municipality muni: tempMuniList){
                    umapCandidateList.addAll(userAuthorizedQueuedForSession.getMuniAuthPeriodsMap().get(muni));
                }

            }
        }
        
    }
    
    
    /**
    * *******************************
    * ***    SECURITY CRITICAL    ***
    * *******************************
    * 
     * JBoss is responsible for the first query against the DB. If a username/pass
     * matches the query, this method will extract the username from any old request
     * @return the username string of an authenticated user from the container
     */
    private String sessionInit_getContainerAuthenticatedUser() throws IntegrationException {
        System.out.println("SessionInitializer.getContainerAuthenticatedUser");
        FacesContext fc = getFacesContext();
        ExternalContext ec = fc.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) ec.getRequest();
        return request.getRemoteUser();
    }
    
    /**
     * Used by developers to load a session that isn't their own.
     * This button will only exist on a session re-init since 
     * it won't be rendered before the user is granted a developer-level
     * permissions under their own authority.
     * 
     * @param u 
     */
    public void sessionInit_loadAuthPeriodsForAlternateUser(User u){
        
        
        
    }
    
    /**
    * *******************************
    * ***    SECURITY CRITICAL    ***
    * *******************************
    * 
     * Processes the user's choice of their authorization period
     * and initiates the entire auth process to create a fully populted session
     * @param umap the chosen UMAP, which should be valid
     * @return 
     */
    public String sessionInit_credentializeUserMuniAuthPeriod(UserMuniAuthPeriod umap){
        String page = sessionInit_configureSession(getSessionBean().getSessUser(), umap);
        
        return page;
        
    }
    
    
    /**
    * *******************************
    * ***    SECURITY CRITICAL    ***
    * *******************************
    * 
     * Core configuration method for sessions that takes in a chosen UMAP from
     * a pre-assembled list of valid UMAPs
     * 
     * The logic work is passed up to the UserCoordinator
     * 
     * @param ua
     * @param umap
     * @return nav string
     */
    public String sessionInit_configureSession(UserAuthorized ua, UserMuniAuthPeriod umap) {
        FacesContext facesContext = getFacesContext();
        UserCoordinator uc = getUserCoordinator();
        System.out.println("SessionInitializer.configureSession()");
        
        try {
            // The central call which initiates the User's session for a particular municipality
            // Muni will be null when called from initiateInternalSession
            UserAuthorized authUser = uc.auth_authorizeUser_SECURITYCRITICAL(ua, umap);
            
            // as long as we have an actual user, proceed with session config
            if(authUser != null){
                initializeSubsystems(authUser);
                System.out.println("Init subsystem success!");
               return "success";
            } else {
                System.out.println("Init subsystem no auth!");
                return "noAuth";
            }
        
        } catch (IntegrationException ex) {
//            getLogIntegrator().makeLogEntry(99, getSessionID(),2,"SessionInitializer.initiateInternalSession | user lookup integration error", 
//                    true, true);
            System.out.println("SessionInitializer.intitiateInternalSession | error getting facesUser");
            System.out.println(ex);
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, 
                    "Integration module error. Unable to connect your server user to the COG system user.", 
                    "Please contact system administrator Eric Darsow at 412.923.9907"));
            return "";
        } catch (AuthorizationException ex) {
            System.out.println("SessionInitializer.intitiateInternalSession | Auth exception");
            System.out.println(ex);
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    ex.getMessage(), ""));
            return "";
        } catch (SessionException ex) {
            System.out.println("SessionInitializer.intitiateInternalSession | Session exception");
            System.out.println(ex);
            return "";
        }
    }
    
    
    /**
    * *******************************
    * ***    SECURITY CRITICAL    ***
    * *******************************
    * 
     * Distributor method for all subsystem initialization routines.
     * Individual subsystems are initialized in an order which allows 
     * each to use objects generated during its predecessors sequences, such as
     * a UserAuthorized, a Credential, a MuniDataHeavy
     * @param ua
     * @throws SessionException 
     */
    private void initializeSubsystems(UserAuthorized ua) throws SessionException{
        if(ua == null){
            throw new SessionException( "No authorized user found for subsystem initialization", 
                                        SubSysEnum.N_USER, 
                                        ExceptionSeverityEnum.SESSION_FATAL);
        }
        
        Credential cred = ua.getMyCredential();
        List<SessionException> exlist = new ArrayList<>();
     
        for(SubSysEnum ss: SubSysEnum.values()){
            try{
                if(ss.isInitialize()){
                    printSubsystemInit(ss);
                    switch(ss){
                        case N_USER:
                            initSubsystem_N_User(ua, ss);
                            break;
                        case I_MUNICIPALITY:
                            initSubsystem_I_Municipality(ua, ss);
                            break;
                        case II_CODEBOOK:
                            initSubsystem_II_CodeBook(cred, ss, sb.getSessMuni());
                            break;
                        case III_PROPERTY:
                            initSubsystem_III_Property(ua, ss);
                            break;
                        case IV_PERSON:
                            initSubsystem_IV_Person(cred, ss);
                            break;
                        case V_EVENT:
                            initSubsystem_V_Event(cred, ss);
                            break;
                        case VI_OCCPERIOD:
                            initSubsystem_VI_OccPeriod(cred, ss, sb.getSessMuni());
                            break;
                        case VII_CECASE:
                            initSubsystem_VII_CECase(ua, ss);
                            break;
                        case VIII_CEACTIONREQ:
                            initSubsystem_VIII_CEActionRequest(cred, ss);
                            break;
                        case VIV_OCCAPP:
                            initSubsystem_VIV_OccApp(cred, ss);
                            break;
                        case X_PAYMENT:
                            initSubsystem_X_Payment(cred, ss);
                            break;
                        case XI_REPORT:
                            initSubsystem_XI_Report(cred, ss);
                            break;
                        case XII_BLOB:
                            initSubsystem_XII_Blob(cred, ss);
                            break;
                        case XIII_PUBLICINFO:
                            initSubsystem_XIII_PublicInfoBundle(cred, ss);
                            break;
                    } // close switch
                    printSubsystemInitComplete(ss);
                } // close if
            } catch (SessionException ex){
                System.out.println(ex.getLocalizedMessage());
                throw new SessionException("Subsystem init catch all", ex);
            }
        } // close for over subsystem enum
        
        // record session data to DB
        initSubsystem_logSession(ua, SubSysEnum.N_USER);
        
    }
    
    /**
    * *******************************
    * ***    SECURITY CRITICAL    ***
    * *******************************
    * 
     * Designed for recording data about the human user's computer connected to our surver.
     * TODO: during early design tests, this method wasn't getting the UserAgent from the HTTP
     * headers like we wanted to
     * @param umaple
     * @return 
     */
    private UserMuniAuthPeriodLogEntry assembleSessionInfo(UserMuniAuthPeriodLogEntry umaple){
        FacesContext fc = getFacesContext();
        HttpServletRequest req = (HttpServletRequest) fc.getExternalContext().getRequest();
        HttpServletResponse res = (HttpServletResponse) fc.getExternalContext().getResponse();
        StringBuilder sb = null;

        Map<String, String[]> headMap = req.getParameterMap();

        umaple.setHeader_remoteaddr(req.getRemoteAddr());
        if(headMap != null && headMap.get(Constants.PARAM_USERAGENT) != null){
             sb = new StringBuilder();
            for(String s: headMap.get(Constants.PARAM_USERAGENT)){
                sb.append(s);
                sb.append("|");
            }
        }
        if(sb != null){
            umaple.setHeader_useragent(sb.toString());
        }
    
        umaple.setHeader_dateraw(res.getHeader(Constants.PARAM_DATERAW));
        
        Cookie[] cooks = req.getCookies();
        if(cooks != null){
            for(Cookie ckie: cooks){
                if(ckie.getName().equals(Constants.PARAM_JSESS)){
                    umaple.setCookie_jsessionid(ckie.getValue());
                    break;
                } // close inner if
            } // close for
        } // close cooks null check
        return umaple;
    }
    
        
        
    /**
     * Utility method for printing to the console notes about session init
     * @param ss 
     */
    private void printSubsystemInit(SubSysEnum ss){
        StringBuilder initString = new StringBuilder();
        initString.append("Initializing subsystem ");
        initString.append(ss.getSubSysID_Roman());
        initString.append(" ");
        initString.append(ss.getTitle());
        System.out.println(initString.toString());
        
    }
    /**
     * Utility method for printing to the console notes about session init
     * @param ss 
     */
    private void printSubsystemInitComplete(SubSysEnum ss){
        StringBuilder initString = new StringBuilder();
        initString.append("Initializing subsystem ");
        initString.append(ss.getSubSysID_Roman());
        initString.append(" ");
        initString.append(ss.getTitle());
        initString.append(": COMPLETE ");
        System.out.println(initString.toString());
        
    }

    /**
    * *******************************
    * ***    SECURITY CRITICAL    ***
    * *******************************
    * 
     * Subsystem initialization controller
     *
     * >>> -------------------------------------------------------------- <<<
     * >>>                   N User                                       <<<
     * >>> -------------------------------------------------------------- <<<
     * 
     * Populates relevant SessionBean members through calls to the governing
     * Coordinator classes. Also initializes query lists for query-able BObs
     * 
     * @param cred of the requesting User
     * @param ss under current configuration
     * @throws SessionException for all initialization issues
     */
    private void initSubsystem_N_User(UserAuthorized authUser, SubSysEnum ss) throws SessionException{
        UserCoordinator uc = getUserCoordinator();
        
        sb.setSessUser(authUser);
       
    }
    
    
    /**
     * Subsystem initialization controller
     *
     * >>> -------------------------------------------------------------- <<<
     * >>>                   I Municipality                               <<<
     * >>> -------------------------------------------------------------- <<<
     * 
     * Populates relevant SessionBean members through calls to the governing
     * Coordinator classes. Also initializes query lists for query-able BObs
     * 
     * @param cred of the requesting User
     * @param ss under current configuration
     * @throws SessionException for all initialization issues
     */
    private void initSubsystem_I_Municipality(UserAuthorized ua, SubSysEnum ss) throws SessionException{
        MunicipalityCoordinator mc = getMuniCoordinator();
        
        MunicipalityDataHeavy muniHeavy;
        try {
            muniHeavy = mc.assembleMuniDataHeavy(ua.getMyCredential().getGoverningAuthPeriod().getMuni(), ua);
        } catch (IntegrationException | AuthorizationException | BObStatusException | EventException ex) {
            System.out.println(ex);
            throw new SessionException("Error creating muni data heavy", ex, ss, ExceptionSeverityEnum.SESSION_FATAL);
        }
        sb.setSessMuni(muniHeavy);
    }


     /**
     * Subsystem initialization controller
     *
     * >>> -------------------------------------------------------------- <<<
     * >>>                   II Codebook                                  <<<
     * >>> -------------------------------------------------------------- <<<
     * 
     * Populates relevant SessionBean members through calls to the governing
     * Coordinator classes. Also initializes query lists for query-able BObs
     * 
     * @param cred of the requesting User
     * @param ss under current configuration
     * @throws SessionException for all initialization issues
     */
    private void initSubsystem_II_CodeBook(Credential cred, SubSysEnum ss, MunicipalityDataHeavy mdh) throws SessionException{
        sb.setSessCodeSet(mdh.getCodeSet());
        
    }




     /**
     * Subsystem initialization controller
     *
     * >>> -------------------------------------------------------------- <<<
     * >>>                   III Property                                 <<<
     * >>> -------------------------------------------------------------- <<<
     * 
     * Populates relevant SessionBean members through calls to the governing
     * Coordinator classes. Also initializes query lists for query-able BObs
     * 
     * @param cred of the requesting User
     * @param ss under current configuration
     * @throws SessionException for all initialization issues
     */  
    private void initSubsystem_III_Property(UserAuthorized ua, SubSysEnum ss) throws SessionException{
        PropertyCoordinator pc = getPropertyCoordinator();
        SearchCoordinator sc = getSearchCoordinator();
        SessionBean sessBean = getSessionBean();
        
        try {

//            TODO: Fix hanging session on property history load
//                  INCIDENT to hanging session on thindes session
//            sessBean.setSessPropertyList(pc.assemblePropertyHistoryList(cred));
            
            if(sessBean.getSessPropertyList() == null || sessBean.getSessPropertyList().isEmpty()){
                sessBean.setSessProperty(pc.selectDefaultProperty(ua));
            } else {
//                sessBean.setSessProperty(pc.assemblePropertyDataHeavy(sessBean.getSessPropertyList().get(0), cred));
            }
        
            sessBean.setQueryPropertyList(sc.buildQueryPropertyList(ua.getKeyCard()));
            
            if(!sessBean.getQueryPropertyList().isEmpty()){
                sessBean.setQueryProperty(sessBean.getQueryPropertyList().get(0));
            }            
        } catch (IntegrationException ex) {
            System.out.println(ex);
            throw new SessionException( "Error setting proerty query list", 
                                        ex, ss, 
                                        ExceptionSeverityEnum.SESSION_RESTRICTING_FAILURE);
        }        
    }


     /**
     * Subsystem initialization controller
     *
     * >>> -------------------------------------------------------------- <<<
     * >>>                   IV Person                                    <<<
     * >>> -------------------------------------------------------------- <<<
     * 
     * Populates relevant SessionBean members through calls to the governing
     * Coordinator classes. Also initializes query lists for query-able BObs
     * 
     * @param cred of the requesting User
     * @param ss under current configuration
     * @throws SessionException for all initialization issues
     */
    private void initSubsystem_IV_Person(Credential cred, SubSysEnum ss) throws SessionException{
        SearchCoordinator sc = getSearchCoordinator();
        PersonCoordinator persc = getPersonCoordinator();
        
        try {
            sb.setSessPersonList(persc.assemblePersonHistory(cred));
            if(sb.getSessPersonList().isEmpty()){
                sb.setSessPersonQueued(persc.selectDefaultPerson(cred));
            } else {
                sb.setSessPersonQueued(sb.getSessPersonList().get(0));
            }
            sb.setQueryPersonList(sc.buildQueryPersonList(cred));
            if(!sb.getQueryPersonList().isEmpty()){
                sb.setQueryPerson(sb.getQueryPersonList().get(0));
            }
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }

     /**
     * Subsystem initialization controller
     *
     * >>> -------------------------------------------------------------- <<<
     * >>>                   V Event                                     <<<
     * >>> -------------------------------------------------------------- <<<
     * 
     * Populates relevant SessionBean members through calls to the governing
     * Coordinator classes. Also initializes query lists for query-able BObs
     * 
     * @param cred of the requesting User
     * @param ss under current configuration
     * @throws SessionException for all initialization issues
     */
    private void initSubsystem_V_Event(Credential cred, SubSysEnum ss) throws SessionException{
        SearchCoordinator sc = getSearchCoordinator();
        sb.setQueryEventList(sc.buildQueryEventList(cred));
        if(!sb.getQueryEventList().isEmpty()){
            sb.setQueryEvent(sb.getQueryEventList().get(0));
        }
    }


     /**
     * Subsystem initialization controller
     *
     * >>> -------------------------------------------------------------- <<<
     * >>>                   VI OccPeriod                                 <<<
     * >>> -------------------------------------------------------------- <<<
     * 
     * Populates relevant SessionBean members through calls to the governing
     * Coordinator classes. Also initializes query lists for query-able BObs
     * 
     * @param cred of the requesting User
     * @param ss under current configuration
     * @throws SessionException for all initialization issues
     */
    private void initSubsystem_VI_OccPeriod(Credential cred, SubSysEnum ss, MunicipalityDataHeavy mdh) throws SessionException{
        SearchCoordinator sc = getSearchCoordinator();
        OccupancyCoordinator occCord = getOccupancyCoordinator();
        MunicipalityCoordinator mc = getMuniCoordinator();
        
        OccPeriod op = null;
        try {
            // Session object init
            sb.setSessOccPeriodList(occCord.assembleOccPeriodHistoryList(cred));
            if(sb.getSessOccPeriodList().isEmpty()){
                op = occCord.getOccPeriod(sb.getSessMuni().getDefaultOccPeriodID());
            } else {
                // if there's a history, convert the head item to a data heavy and put in session
                op = sb.getSessOccPeriodList().get(0);
            }
            
            sb.setSessOccPeriod(occCord.assembleOccPeriodDataHeavy(op, cred));
            if(op == null){
                throw new SessionException("Unable to set a session occ period");
            }
            
            // Query set init
            sb.setQueryOccPeriodList(sc.buildQueryOccPeriodList(cred));
            if(!sb.getQueryOccPeriodList().isEmpty()){
                sb.setQueryOccPeriod(sb.getQueryOccPeriodList().get(0));
            }
        } catch (IntegrationException | BObStatusException | SearchException ex) {
            System.out.println(ex);
            throw new SessionException( "Occ period list or query assembly failure", 
                                        ex, 
                                        ss, 
                                        ExceptionSeverityEnum.SESSION_RESTRICTING_FAILURE);
        }
    }
    
     /**
     * Subsystem initialization controller
     *
     * >>> -------------------------------------------------------------- <<<
     * >>>                   VII Cecase                                   <<<
     * >>> -------------------------------------------------------------- <<<
     * 
     * Populates relevant SessionBean members through calls to the governing
     * Coordinator classes. Also initializes query lists for query-able BObs
     * 
     * @param cred of the requesting User
     * @param ss under current configuration
     * @throws SessionException for all initialization issues
     */
    private void initSubsystem_VII_CECase(UserAuthorized ua, SubSysEnum ss) throws SessionException{
        CaseCoordinator cc = getCaseCoordinator();
        SearchCoordinator sc = getSearchCoordinator();
        QueryCECase cseQ = sc.initQuery(QueryCECaseEnum.OPENCASES, ua.getKeyCard());
        try {
            cseQ = sc.runQuery(cseQ);
            
//            List<CECase> hist = cc.cecase_getCECaseHistory(ua);
            // NEXT LINE: YUCK!!!!!!!!
            sb.setSessCECaseList(cc.cecase_assembleCECasePropertyUnitHeavyList(cseQ.getBOBResultList()));
            
            if(sb.getSessCECaseList().isEmpty()){
                sb.setSessCECase(cc.cecase_assembleCECaseDataHeavy(cc.cecase_selectDefaultCECase(ua), ua));
            } else {
                sb.setSessCECase(cc.cecase_assembleCECaseDataHeavy(sb.getSessCECaseList().get(0), ua));
            }
            
            
            
        } catch (IntegrationException | BObStatusException | SearchException ex) {
            System.out.println(ex);
            throw new SessionException("Error assembling session CECase list", ex, ss, ExceptionSeverityEnum.SESSION_RESTRICTING_FAILURE);
        }

        sb.setQueryCECaseList(sc.buildQueryCECaseList(ua.getKeyCard()));
        if(sb.getQueryCECaseList() != null && !sb.getQueryCECaseList().isEmpty()){
            sb.setQueryCECase(sb.getQueryCECaseList().get(0));
        }
        
    }
    

     /**
     * Subsystem initialization controller
     *
     * >>> -------------------------------------------------------------- <<<
     * >>>                   VIII CEActionrequest                         <<<
     * >>> -------------------------------------------------------------- <<<
     * 
     * Populates relevant SessionBean members through calls to the governing
     * Coordinator classes. Also initializes query lists for query-able BObs
     * 
     * @param cred of the requesting User
     * @param ss under current configuration
     * @throws SessionException for all initialization issues
     */
    private void initSubsystem_VIII_CEActionRequest(Credential cred, SubSysEnum ss) throws SessionException{
        SearchCoordinator sc = getSearchCoordinator();
        QueryCEAR cearQ = sc.initQuery(QueryCEAREnum.UNPROCESSED, cred);
        try {
            cearQ = sc.runQuery(cearQ);
        } catch (SearchException ex) {
            System.out.println(ex);
        }
        sb.setSessCEARList(cearQ.getBOBResultList());
        
        
        sb.setQueryCEARList(sc.buildQueryCEARList(cred));
        if(!sb.getQueryCEARList().isEmpty()){
            sb.setQueryCEAR(sb.getQueryCEARList().get(0));
        }
        
    }


     /**
     * Subsystem initialization controller
     *
     * >>> -------------------------------------------------------------- <<<
     * >>>                   VIV Occapp                                   <<<
     * >>> -------------------------------------------------------------- <<<
     * 
     * Populates relevant SessionBean members through calls to the governing
     * Coordinator classes. Also initializes query lists for query-able BObs
     * 
     * @param cred of the requesting User
     * @param ss under current configuration
     * @throws SessionException for all initialization issues
     */
    private void initSubsystem_VIV_OccApp(Credential cred, SubSysEnum ss) throws SessionException{
        
    }
    



     /**
     * Subsystem initialization controller
     *
     * >>> -------------------------------------------------------------- <<<
     * >>>                   X Payment                                    <<<
     * >>> -------------------------------------------------------------- <<<
     * 
     * Populates relevant SessionBean members through calls to the governing
     * Coordinator classes. Also initializes query lists for query-able BObs
     * 
     * @param cred of the requesting User
     * @param ss under current configuration
     * @throws SessionException for all initialization issues
     */
    private void initSubsystem_X_Payment(Credential cred, SubSysEnum ss) throws SessionException{
        
    }

    
     /**
     * Subsystem initialization controller
     *
     * >>> -------------------------------------------------------------- <<<
     * >>>                   XI Report                                    <<<
     * >>> -------------------------------------------------------------- <<<
     * 
     * Populates relevant SessionBean members through calls to the governing
     * Coordinator classes. Also initializes query lists for query-able BObs
     * 
     * @param cred of the requesting User
     * @param ss under current configuration
     * @throws SessionException for all initialization issues
     */
    private void initSubsystem_XI_Report(Credential cred, SubSysEnum ss) throws SessionException{ 
        
        
    }
    
    

     /**
     * Subsystem initialization controller
     *
     * >>> -------------------------------------------------------------- <<<
     * >>>                   XII Blob                                     <<<
     * >>> -------------------------------------------------------------- <<<
     * 
     * Populates relevant SessionBean members through calls to the governing
     * Coordinator classes. Also initializes query lists for query-able BObs
     * 
     * @param cred of the requesting User
     * @param ss under current configuration
     * @throws SessionException for all initialization issues
     */
    private void initSubsystem_XII_Blob(Credential cred, SubSysEnum ss) throws SessionException{
        
    }

    
    
     /**
     * Subsystem initialization controller
     *
     * >>> -------------------------------------------------------------- <<<
     * >>>                   XIII PublicInfo                              <<<
     * >>> -------------------------------------------------------------- <<<
     * 
     * Populates relevant SessionBean members through calls to the governing
     * Coordinator classes. Also initializes query lists for query-able BObs
     * 
     * @param cred of the requesting User
     * @param ss under current configuration
     * @throws SessionException for all initialization issues
     */
    private void initSubsystem_XIII_PublicInfoBundle(Credential cred, SubSysEnum ss) throws SessionException{
        
        
    }

    
    
    
     /**
     * Subsystem initialization controller
     *
     * >>> -------------------------------------------------------------- <<<
     * >>>                   XIV Logging                                  <<<
     * >>> -------------------------------------------------------------- <<<
     * 
     * Populates relevant SessionBean members through calls to the governing
     * Coordinator classes. Also initializes query lists for query-able BObs
     * 
     * @param cred of the requesting User
     * @param ss under current configuration
     * @throws SessionException for all initialization issues
     */
    private void initSubsystem_logSession(UserAuthorized authUser, SubSysEnum ss) throws SessionException{
         UserMuniAuthPeriodLogEntry umaple;
         UserCoordinator uc = getUserCoordinator();
         
        umaple = uc.auth_assembleUserMuniAuthPeriodLogEntrySkeleton(
                              authUser, 
                              UserMuniAuthPeriodLogEntryCatEnum.SESSION_INIT);

        umaple = assembleSessionInfo(umaple);

        if(umaple != null){
            umaple.setAudit_usersession_userid(sb.getSessUser().getUserID());
            umaple.setAudit_muni_municode(sb.getSessMuni().getMuniCode());
            umaple.setAudit_usercredential_userid(authUser.getMyCredential().getGoverningAuthPeriod().getUserID());
            try {
                uc.auth_logCredentialInvocation(umaple, authUser.getMyCredential().getGoverningAuthPeriod());
            } catch (IntegrationException | AuthorizationException ex) {
                System.out.println(ex);
                throw new SessionException( "Failure creating user muni auth period log entry", 
                                            ex, ss,
                                            ExceptionSeverityEnum.NONCRITICAL_FAILURE);
            }
        }
        
    }

    
    
    
    

    /**
     * @return the userAuthorizedQueuedForSession
     */
    public UserAuthorized getUserAuthorizedQueuedForSession() {
        return userAuthorizedQueuedForSession;
    }


    /**
     * @param userAuthorizedQueuedForSession the userAuthorizedQueuedForSession to set
     */
    public void setUserAuthorizedQueuedForSession(UserAuthorized userAuthorizedQueuedForSession) {
        this.userAuthorizedQueuedForSession = userAuthorizedQueuedForSession;
    }

    /**
     * @return the muniQueuedForSession
     */
    public Municipality getMuniQueuedForSession() {
        System.out.println("SessionInitializer.getMuniQueuedForSession");
        return muniQueuedForSession;
    }

    /**
     * @param muniQueuedForSession the muniQueuedForSession to set
     */
    public void setMuniQueuedForSession(Municipality muniQueuedForSession) {
        this.muniQueuedForSession = muniQueuedForSession;
    }

   

    /**

     * @return the usernameQueuedForSession
     */
    public String getUsernameQueuedForSession() {
        return usernameQueuedForSession;
    }

    /**
     * @param usernameQueuedForSession the usernameQueuedForSession to set
     */
    public void setUsernameQueuedForSession(String usernameQueuedForSession) {
        this.usernameQueuedForSession = usernameQueuedForSession;
    }

    /**
     * @return the umapQueuedForSession
     */
    public UserMuniAuthPeriod getUmapQueuedForSession() {
        return umapQueuedForSession;
    }

    /**
     * @param umapQueuedForSession the umapQueuedForSession to set
     */
    public void setUmapQueuedForSession(UserMuniAuthPeriod umapQueuedForSession) {
        this.umapQueuedForSession = umapQueuedForSession;
    }

    /**
     * @return the umapCandidateList
     */
    public List<UserMuniAuthPeriod> getUmapCandidateList() {
        return umapCandidateList;
    }

    /**
     * @param umapCandidateList the umapCandidateList to set
     */
    public void setUmapCandidateList(List<UserMuniAuthPeriod> umapCandidateList) {
        this.umapCandidateList = umapCandidateList;
    }

   
}
