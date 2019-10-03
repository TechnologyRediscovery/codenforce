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
import com.tcvcog.tcvce.entities.MunicipalityListified;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthorized;
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
import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import com.tcvcog.tcvce.util.Constants;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.annotation.PostConstruct;

/**
 *
 * @author Eric C. Darsow
 */
public class SessionInitializer extends BackingBeanUtils implements Serializable {

   
    
    /**
     * Creates a new instance of SessionInitializer
     */
    public SessionInitializer() {
    }
    
    @PostConstruct
    public void initBean(){
        
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
     */
    public String initiateInternalSession() throws IntegrationException, CaseLifecycleException, SQLException{
        String authUserName = getContainerAuthenticatedUser();
        return configureSession(authUserName, null);
    }

    /**
     * JBoss is responsible for the first query against the DB. If a username/pass
     * matches the query, this method will extract the username from any old request
     * @return the username string of an authenticated user from the container
     */
    private String getContainerAuthenticatedUser() {
        FacesContext fc = getFacesContext();
        ExternalContext ec = fc.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) ec.getRequest();
        return request.getRemoteUser();
    }
    
    /**
     * Core configuration method for sessions; called both during an initial login
     * and subsequent changes to the current municipality. It does the work
     * of gathering all necessary info for session config
     * 
     * @param userName
     * @param muni
     * @return nav string
     * @throws CaseLifecycleException
     * @throws IntegrationException 
     */
    public String configureSession(String userName, Municipality muni) throws CaseLifecycleException, IntegrationException{
        FacesContext facesContext = getFacesContext();
        UserCoordinator uc = getUserCoordinator();
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        
        try {
            // The central call which initiates the User's session for a particular municipality
            UserAuthorized authUser = uc.requestUserMuniAuthorization(userName, muni);
            
            // as long as we have an actual user, proceed with session config
            if(authUser != null){
                // The stadnard Municipality object is simple, but we need the full deal
                MunicipalityListified muniComplete = mi.getMuniComplete(authUser.getKeyCard().getMuni().getMuniCode());
                
                // load up our SessionBean with its key objects
                getSessionBean().setSessionMuni(muniComplete);
                getSessionBean().setSessionUser(authUser);
                getSessionBean().setActiveCodeSet(muniComplete.getCodeSet());
                
                // our new UserAuthorized has a Map of municipalities as keys
                // and the MuniAuthPeriod record as the value. Grab the keys as a set
                // and turn them into a List which we can display to the UI
                getSessionBean().setUserAuthMuniList(new ArrayList<>(authUser.getMuniAuthPerMap().keySet()));
                
                populateSessionObjectQueues(authUser, muniComplete);
                
                getLogIntegrator().makeLogEntry(authUser.getUserID(), getSessionID(), 
                        Integer.parseInt(getResourceBundle(Constants.LOGGING_CATEGORIES).getString("login")), 
                         "SessionInitializer.initiateInternalSession | Created internal session", false, false);
                System.out.println("SessionInitializer.initiateInternalSession | auth user: " + authUser.getKeyCard().getAuthRole());
            }
        
        } catch (IntegrationException ex) {
            getLogIntegrator().makeLogEntry(99, getSessionID(),2,"SessionInitializer.initiateInternalSession | user lookup integration error", 
                    true, true);
            System.out.println("SessionInitializer.intitiateInternalSession | error getting facesUser");
            System.out.println(ex);
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, 
                    "Integration module error. Unable to connect your server user to the COG system user.", 
                    "Please contact system administrator Eric Darsow at 412.923.9907"));
            return "failure";
        } catch (AuthorizationException ex) {
            System.out.println("SessionInitializer.intitiateInternalSession | Auth exception");
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    ex.getMessage(), ""));
            return "failure";
        }
        return "success";
    }
    
        
    /**
     * With an active User and Municipality, we're ready to load up our
     * Session-persistent shelves with our core business objects,
     * Queries and some utility lists
     * 
     * @param ua
     * @param m
     * @throws IntegrationException
     * @throws CaseLifecycleException 
     */
    private void populateSessionObjectQueues(UserAuthorized ua, MunicipalityListified m) throws IntegrationException, CaseLifecycleException{
        SessionBean sessionBean = getSessionBean();
        
        PersonCoordinator persCoord = getPersonCoordinator();
        CaseCoordinator caseCoord = getCaseCoordinator();
        PropertyIntegrator propI = getPropertyIntegrator();
        PersonIntegrator persInt = getPersonIntegrator();
        CaseIntegrator caseInt = getCaseIntegrator();
        SearchCoordinator searchCoord = getSearchCoordinator();
        
        
//        sessionBean.setSessionPersonList(persCoord.loadPersonHistoryList(u));
//        sessionBean.setSessionCECaseList(caseCoord.getUserCaseHistoryList(u));
//        
//        QueryCECase queryCECase = searchCoord.runQuery(searchCoord.getQueryInitialCECASE(m, u));
        
//        sessionBean.setSessionCECase(caseInt.getCECase(ua.getGoverningAuthPeriod().getDefaultCECaseID()));
        sessionBean.setSessionProperty(propI.getProperty(m.getMuniOfficePropertyId()));
        sessionBean.setSessionPerson(ua.getPerson());
        
        sessionBean.setSessionCECase(caseInt.getCECase(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("arbitraryPlaceholderCaseID"))));

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
}
