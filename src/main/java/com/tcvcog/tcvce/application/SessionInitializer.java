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
import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.search.QueryCECase;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import com.tcvcog.tcvce.util.Constants;
import java.util.ArrayList;

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
     */
    public String initiateInternalSession() throws IntegrationException, CaseLifecyleException{
        CodeIntegrator ci = getCodeIntegrator();
        System.out.println("SessionInitializer.initiateInternalSession");
        FacesContext facesContext = getFacesContext();
        UserCoordinator uc = getUserCoordinator();
        PropertyIntegrator pi = getPropertyIntegrator();
        PersonIntegrator persInt = getPersonIntegrator();
        CaseIntegrator caseint = getCaseIntegrator();
        
        try {
            User extractedUser = uc.getUser(getContainerAuthenticatedUser());
            if(extractedUser != null){
                
                ExternalContext ec = facesContext.getExternalContext();
                ec.getSessionMap().put("facesUser", extractedUser);
                System.out.println("SessionInitializer.initiateInternalSession ");

                Municipality muni = uc.getDefaultyMuni(extractedUser);
                
//                getSessionBean().setActivePerson(persInt.getPerson(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
//                        .getString("arbitraryPlaceholderPersonID"))));
                getSessionBean().setFacesUser(extractedUser);
//                getSessionBean().setActivePersonList(persInt.getPersonHistory(extractedUser));
                getSessionBean().setActiveMuni(muni);
                getSessionBean().setUserAuthMuniList(uc.getUserAuthMuniList(extractedUser.getUserID()));
                
                // grab code set ID from the muni object,  ask integrator for the CodeSet object, 
                //and then and store in sessionBean
                getSessionBean().setActiveCodeSet(ci.getCodeSetBySetID(muni.getDefaultCodeSetID()));
                
                populateSessionObjectQueues(extractedUser, muni);

                getLogIntegrator().makeLogEntry(extractedUser.getUserID(), getSessionID(), 
                        Integer.parseInt(getResourceBundle(Constants.LOGGING_CATEGORIES).getString("login")), 
                         "SessionInitializer.initiateInternalSession | Created internal session", false, false);

                
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
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    ex.getMessage(), ""));
        }
          
        return "success";
    }

    /**
     * @return the username string of an authenticated user from the container
     */
    private String getContainerAuthenticatedUser() {
        
        FacesContext fc = getFacesContext();
        ExternalContext ec = fc.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) ec.getRequest();
        return request.getRemoteUser();
    }

    
        
    private void populateSessionObjectQueues(User u, Municipality m) throws IntegrationException, CaseLifecyleException{
        SessionBean sessionBean = getSessionBean();
        
        PersonCoordinator persCoord = getPersonCoordinator();
        CaseCoordinator caseCoord = getCaseCoordinator();
        PropertyIntegrator propI = getPropertyIntegrator();
        PersonIntegrator persInt = getPersonIntegrator();
        CaseIntegrator caseInt = getCaseIntegrator();
        SearchCoordinator searchCoord = getSearchCoordinator();
        
        
        sessionBean.setPersonQueue(persCoord.loadPersonHistoryList(u));
        sessionBean.setcECaseQueue(caseCoord.getUserCaseHistoryList(u));
        
        QueryCECase queryCECase = searchCoord.runQuery(searchCoord.getQueryInitialCECASE(m, u));
        sessionBean.setSessionQueryCECase(queryCECase);
        
        Property p = propI.getProperty(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                .getString("arbitraryPlaceholderPropertyID")));
        
        sessionBean.setActiveProp(p);

        sessionBean.setActivePerson(persInt.getPerson(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                .getString("arbitraryPlaceholderPersonID"))));
        
        sessionBean.setSessionQueryCEAR(searchCoord.getQueryInitialCEAR(u, m));
        
        CECase c = caseInt.getCECase(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                .getString("arbitraryPlaceholderCaseID")));
        sessionBean.setcECase(c);
        
//        sessionBean.setcECaseQueue(new ArrayList<CECase>());
//        sessionBean.getcECaseQueue().add(c);
        
        sessionBean.setPropertyQueue(propI.getPropertyHistoryList(u));
        sessionBean.getPropertyQueue().add(p);
    }
}
