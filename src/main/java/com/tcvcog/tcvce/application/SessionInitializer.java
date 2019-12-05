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
import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.CaseLifecycleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.MunicipalityDataHeavy;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.UserMuniAuthPeriod;
import com.tcvcog.tcvce.entities.UserMuniAuthPeriodLogEntry;
import com.tcvcog.tcvce.entities.UserMuniAuthPeriodLogEntryCatEnum;
import com.tcvcog.tcvce.entities.search.QueryCEAREnum;
import com.tcvcog.tcvce.entities.search.QueryCECase;
import com.tcvcog.tcvce.entities.search.QueryCECaseEnum;
import com.tcvcog.tcvce.entities.search.QueryEventCECaseEnum;
import com.tcvcog.tcvce.entities.search.QueryOccPeriodEnum;
import com.tcvcog.tcvce.entities.search.QueryPersonEnum;
import com.tcvcog.tcvce.entities.search.QueryPropertyEnum;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;
import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import com.tcvcog.tcvce.util.Constants;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Eric C. Darsow
 */
public class SessionInitializer extends BackingBeanUtils implements Serializable {

    private User userQueuedForSession;
    private UserAuthorized userAuthorizedQueuedForSession;
    private UserMuniAuthPeriod umapQueuedForSession;
    private Municipality muniQueuedForSession;
    
    /**
     * Creates a new instance of SessionInitializer
     */
    public SessionInitializer() {
    }
    
    @PostConstruct
    public void initBean(){
        System.out.println("SessionInitializer.initBean");
        userAuthorizedQueuedForSession = null;
        UserCoordinator uc = getUserCoordinator();
        // check to see if we have an internal session created already
        // to determine which user we authenticate with
        muniQueuedForSession = getSessionBean().getSessionMuni();
        userQueuedForSession = getSessionBean().getSessionUserForReInitSession();
        umapQueuedForSession = getSessionBean().getUmapRequestedForReInit();
        
        
        try {
            if(muniQueuedForSession == null
                    &&
                userQueuedForSession == null
                    &&
                umapQueuedForSession == null){
                // we have a first init! Ask the container for its user
                userQueuedForSession = getContainerAuthenticatedUser();
            } 
            userAuthorizedQueuedForSession = uc.authorizeUser(userQueuedForSession, muniQueuedForSession, umapQueuedForSession);
        } catch (AuthorizationException | IntegrationException ex) {
            System.out.println(ex);
        }
    }
    
    /**
     * Central method for setting up the user's session:
     * 1) First get the user from the system
     * 2) User comes back with a default municipality object, which is stored in the session
     * 3) From this muni, extract the default code set ID, which is then used to grab
     * the code set from the DB and store this in the session as well.
     * 4) Fill in a bunch of session objects to quash those null pointers
     * 
     * @return success or failure String used by faces to navigate to the internal page
     * or the error page
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.CaseLifecycleException
     * @throws java.sql.SQLException
     */
    public String initiateInternalSession() throws IntegrationException, CaseLifecycleException, SQLException{
        System.out.println("SessionInitializer.initiateInternalSession");
        UserCoordinator uc = getUserCoordinator();
        if(getSessionBean().getSessionUser() == null){
            return configureSession(getContainerAuthenticatedUser(), null, null);
        } else {
            // if we have an existing session, send in the userAuthorizedQueuedForSession and let the
            // auth logic choose the muni
            return configureSession(userAuthorizedQueuedForSession, muniQueuedForSession, umapQueuedForSession);
        }
    }

    /**
     * JBoss is responsible for the first query against the DB. If a username/pass
     * matches the query, this method will extract the username from any old request
     * @return the username string of an authenticated user from the container
     */
    private User getContainerAuthenticatedUser() throws IntegrationException {
        System.out.println("SessionInitializer.getContainerAuthenticatedUser");
        UserIntegrator ui  = getUserIntegrator();
        FacesContext fc = getFacesContext();
        ExternalContext ec = fc.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) ec.getRequest();
        return ui.getUser(ui.getUserID(request.getRemoteUser()));
    }
    
    
    
    /**
     * Core configuration method for sessions; called both during an initial login
     * and subsequent changes to the current municipality. It does the work
     * of gathering all necessary info for session config
     * 
     * @param u
     * @param muni if null, the system will choose the highest ranked role in the
     * muni record added most recently
     * @param umap
     * @return nav string
     * @throws CaseLifecycleException
     * @throws IntegrationException 
     */
    public String configureSession(User u, Municipality muni, UserMuniAuthPeriod umap) throws CaseLifecycleException, IntegrationException{
        FacesContext facesContext = getFacesContext();
        UserCoordinator uc = getUserCoordinator();
        PropertyIntegrator pi = getPropertyIntegrator();
        PersonIntegrator persInt = getPersonIntegrator();
        CaseIntegrator caseint = getCaseIntegrator();
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        UserMuniAuthPeriodLogEntry umaple;
        
        try {
            // The central call which initiates the User's session for a particular municipality
            // Muni will be null when called from initiateInternalSession
            UserAuthorized authUser = uc.authorizeUser(u, muni, umap);
            
            // as long as we have an actual user, proceed with session config
            if(authUser != null){
                // The stadnard Municipality object is simple, but we need the full deal
                MunicipalityDataHeavy muniHeavy = 
                        mi.getMuniListified(authUser.getMyCredential().getGoverningAuthPeriod().getMuni().getMuniCode());
                System.out.println("SessionInitializer.configureSession | loaded MuniHeavy: " + muniHeavy.getMuniName());
                
                // load up our SessionBean with its key objects
                getSessionBean().setSessionMuni(muniHeavy);
                getSessionBean().setSessionUser(authUser);
                
                populateSessionObjectQueues(authUser, muniHeavy);
                
                umaple = uc.assembleUserMuniAuthPeriodLogEntrySkeleton(
                                authUser, 
                                UserMuniAuthPeriodLogEntryCatEnum.SESSION_INIT);
        
                umaple = assembleSessionInfo(umaple);
                
                if(umaple != null){
                    umaple.setAudit_usersession_userid(getSessionBean().getSessionUser().getUserID());
                    umaple.setAudit_muni_municode(muniHeavy.getMuniCode());
                    umaple.setAudit_usercredential_userid(authUser.getMyCredential().getGoverningAuthPeriod().getUserID());
                    System.out.println("SessionInitializer.configureSession | loaded UserMuniAuthPeriod: " + umaple);
                    uc.logCredentialInvocation(umaple, authUser.getMyCredential().getGoverningAuthPeriod());
                }
             
               return "success";
            } else {
                return "noAuth";
            }
        
        } catch (IntegrationException ex) {
            getLogIntegrator().makeLogEntry(99, getSessionID(),2,"SessionInitializer.initiateInternalSession | user lookup integration error", 
                    true, true);
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
            return "";
        }
    }
    
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
     * With an active User and Municipality, we're ready to load up our
     * Session-persistent shelves with our core business objects,
     * Queries and some utility lists
     * 
     * @param u
     * @param m
     * @throws IntegrationException
     * @throws CaseLifecycleException 
     */
    private void populateSessionObjectQueues(UserAuthorized ua, MunicipalityDataHeavy m) throws IntegrationException, CaseLifecycleException{
        SessionBean sessionBean = getSessionBean();
        
        PropertyIntegrator propI = getPropertyIntegrator();
        SearchCoordinator searchCoord = getSearchCoordinator();
        
        
//        sessionBean.setSessionPersonList(persCoord.loadPersonHistoryList(u));
//        sessionBean.setSessionCECaseList(caseCoord.getUserCaseHistoryList(u));
//        
//        QueryCECase queryCECase = searchCoord.runQuery(searchCoord.getQueryInitialCECASE(m, u));
        
        sessionBean.setSessionProperty(propI.getProperty(m.getMuniOfficePropertyId()));
        sessionBean.setSessionPerson(ua.getPerson());
        
//        Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
//                .getString("arbitraryPlaceholderCaseID")
//                

//        sessionBean.setcECaseQueue(new ArrayList<CECase>());
//        sessionBean.getcECaseQueue().add(c);
        
        // Note that these are Query skeletons and have not yet ben run
        // It's up to the individual beans to check the Query object's
        // "run by integrator" member and run the query if they choose
        sessionBean.setQueryProperty(
                searchCoord.assembleQueryProperty(
                QueryPropertyEnum.OPENCECASES_OCCPERIODSINPROCESS, ua, m, null));
        
        sessionBean.setQueryPerson(
                searchCoord.assembleQueryPerson(
                QueryPersonEnum.CUSTOM, ua, m, null));
        
        sessionBean.setQueryCEAR(
                searchCoord.assembleQueryCEAR(
                QueryCEAREnum.ALL_PAST30, ua, m, null));
        
        sessionBean.setQueryCECase(
                searchCoord.assembleQueryCECase(
                QueryCECaseEnum.OPENCASES, ua, m, null));
        
        sessionBean.setQueryEventCECase(
                searchCoord.assembleQueryEventCECase(
                QueryEventCECaseEnum.MUNICODEOFFICER_ACTIVITY_PAST30DAYS, ua, m, null));
        
        sessionBean.setQueryOccPeriod(
                searchCoord.assembleQueryOccPeriod(
                QueryOccPeriodEnum.CUSTOM, ua, m, null));
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
    public User getUserQueuedForSession() {
        return userQueuedForSession;
    }

    /**
     * @param userQueuedForSession the userQueuedForSession to set
     */
    public void setUserQueuedForSession(User userQueuedForSession) {
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
}
