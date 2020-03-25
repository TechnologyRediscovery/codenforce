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
import com.tcvcog.tcvce.entities.Credential;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.MunicipalityDataHeavy;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.UserMuniAuthPeriod;
import com.tcvcog.tcvce.entities.UserMuniAuthPeriodLogEntry;
import com.tcvcog.tcvce.entities.UserMuniAuthPeriodLogEntryCatEnum;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.integration.UserIntegrator;
import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.SubSysEnum;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public  class       SessionInitializer 
        extends     BackingBeanUtils 
        implements  Serializable {

    private String userQueuedForSession;
    private UserAuthorized userAuthorizedQueuedForSession;
    private UserMuniAuthPeriod umapQueuedForSession;
    private Municipality muniQueuedForSession;
    private SessionBean sb;
    
    private List<UserMuniAuthPeriod> umapListValidForUserSelect;
    
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
        UserCoordinator uc = getUserCoordinator();
        // check to see if we have an internal session created already
        // to determine which user we authenticate with
        
        if(sb.getUserForReInit() != null){
            userQueuedForSession = sb.getUserForReInit().getUsername();
        } else if(userQueuedForSession == null){
            try {
                // we have a first init! Ask the container for its user
                userQueuedForSession = getContainerAuthenticatedUser();
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
        } 
            
            
            umapListValidForUserSelect = uc.assembleValidAuthPeriods(userQueuedForSession);
            getSessionBean().setSessUMAPListValidOnly(umapListValidForUserSelect);
//            userAuthorizedQueuedForSession = uc.authorizeUser(userQueuedForSession, muniQueuedForSession, umapQueuedForSession);
    }
    
    
    /**
     * JBoss is responsible for the first query against the DB. If a username/pass
     * matches the query, this method will extract the username from any old request
     * @return the username string of an authenticated user from the container
     */
    private String getContainerAuthenticatedUser() throws IntegrationException {
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
    public void loadAuthPeriodsForAlternateUser(User u){
        
        
        
    }
    
    /**
     * Processes the user's choice of their authorization period
     * and initiates the entire auth process to create a fully populted session
     * @param umap the chosen UMAP, which should be valid
     * @return 
     */
    public String credentializeUserMuniAuthPeriod(UserMuniAuthPeriod umap){
           return configureSession(umap);
        
    }
    
    
    /**
     * Core configuration method for sessions that takes in a chosen UMAP from
     * a pre-assembled list of valid UMAPs
     * 
     * The logic work is passed up to the UserCoordinator
     * 
     * @param umap
     * @return nav string
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    public String configureSession(UserMuniAuthPeriod umap) {
        FacesContext facesContext = getFacesContext();
        UserCoordinator uc = getUserCoordinator();
        System.out.println("SessionInitializer.configureSession()");
        
        try {
            // The central call which initiates the User's session for a particular municipality
            // Muni will be null when called from initiateInternalSession
            UserAuthorized authUser = uc.authorizeUser(umap, getSessionBean().getSessUMAPListValidOnly());
            
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
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    ex.getMessage(), ""));
            System.out.println(ex);
            return "";
        } catch (SessionException ex) {
            System.out.println("SessionInitializer.intitiateInternalSession | Session exception");
            System.out.println(ex);
            return "";
        }
    }
    
    
    /**
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
                            initSubsystem_I_Municipality(cred, ss);
                            break;
                        case II_CODEBOOK:
                            initSubsystem_II_CodeBook(cred, ss, sb.getSessMuni());
                            break;
                        case III_PROPERTY:
                            initSubsystem_III_Property(cred, ss);
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
                            initSubsystem_VII_CECase(cred, ss);
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
    private void initSubsystem_I_Municipality(Credential cred, SubSysEnum ss) throws SessionException{
        MunicipalityCoordinator mc = getMuniCoordinator();
        
        MunicipalityDataHeavy muniHeavy;
        try {
            muniHeavy = mc.assembleMuniDataHeavy(cred.getGoverningAuthPeriod().getMuni(), cred);
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
    private void initSubsystem_III_Property(Credential cred, SubSysEnum ss) throws SessionException{
        PropertyCoordinator pc = getPropertyCoordinator();
        SearchCoordinator sc = getSearchCoordinator();
        SessionBean sessBean = getSessionBean();
        
        try {

//            TODO: Fix hanging session on property history load
//                  INCIDENT to hanging session on thindes session
//            sessBean.setSessPropertyList(pc.assemblePropertyHistoryList(cred));
            
            if(sessBean.getSessPropertyList() == null || sessBean.getSessPropertyList().isEmpty()){
                sessBean.setSessProperty(pc.selectDefaultProperty(cred));
            } else {
//                sessBean.setSessProperty(pc.assemblePropertyDataHeavy(sessBean.getSessPropertyList().get(0), cred));
            }
        
            sessBean.setQueryPropertyList(sc.buildQueryPropertyList(cred));
            
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
        } catch (IntegrationException | BObStatusException ex) {
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
    private void initSubsystem_VII_CECase(Credential cred, SubSysEnum ss) throws SessionException{
        CaseCoordinator cc = getCaseCoordinator();
        SearchCoordinator sc = getSearchCoordinator();
        
        try {
        
            sb.setSessCECaseList(cc.assembleCaseHistory(cred));
            if(sb.getSessCECaseList().isEmpty()){
                sb.setSessCECase(cc.assembleCECaseDataHeavy(cc.selectDefaultCECase(cred), cred));
            } else {
                sb.setSessCECase(cc.assembleCECaseDataHeavy(sb.getSessCECaseList().get(0), cred));
            }
            
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            throw new SessionException("Error assembling session CECase list from history", ex, ss, ExceptionSeverityEnum.SESSION_RESTRICTING_FAILURE);
        }

        sb.setQueryCECaseList(sc.buildQueryCECaseList(cred));
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
         
        umaple = uc.assembleUserMuniAuthPeriodLogEntrySkeleton(
                              authUser, 
                              UserMuniAuthPeriodLogEntryCatEnum.SESSION_INIT);

        umaple = assembleSessionInfo(umaple);

        if(umaple != null){
            umaple.setAudit_usersession_userid(sb.getSessUser().getUserID());
            umaple.setAudit_muni_municode(sb.getSessMuni().getMuniCode());
            umaple.setAudit_usercredential_userid(authUser.getMyCredential().getGoverningAuthPeriod().getUserID());
            try {
                uc.logCredentialInvocation(umaple, authUser.getMyCredential().getGoverningAuthPeriod());
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

     * @return the userQueuedForSession
     */
    public String getUserQueuedForSession() {
        return userQueuedForSession;
    }

    /**
     * @param userQueuedForSession the userQueuedForSession to set
     */
    public void setUserQueuedForSession(String userQueuedForSession) {
        this.userQueuedForSession = userQueuedForSession;
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
     * @return the umapListValidForUserSelect
     */
    public List<UserMuniAuthPeriod> getUmapListValidForUserSelect() {
        return umapListValidForUserSelect;
    }

    /**
     * @param umapListValidForUserSelect the umapListValidForUserSelect to set
     */
    public void setUmapListValidForUserSelect(List<UserMuniAuthPeriod> umapListValidForUserSelect) {
        this.umapListValidForUserSelect = umapListValidForUserSelect;
    }
}
