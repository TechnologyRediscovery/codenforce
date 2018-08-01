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

import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Eric C. Darsow
 */
public class SessionInitializer extends BackingBeanUtils implements Serializable {

    private String glassfishUser;
    private String sessionID;
    private User retrievedCOGUser;
    
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
     * 
     * @return success or failure String used by faces to navigate to the internal page
     * or the error page
     */
    public String initiateInternalSession(){
        CodeIntegrator ci = getCodeIntegrator();
        System.out.println("SessionInitializer.initiateInternalSession");
        FacesContext facesContext = getFacesContext();
        UserCoordinator uc = getUserCoordinator();
        
        try {
            User extractedUser = uc.getUser(getGlassfishUser());
            if(extractedUser != null){
                ExternalContext ec = facesContext.getExternalContext();
                ec.getSessionMap().put("facesUser", extractedUser);
                System.out.println("SessionInitializer.initiateInternalSession "
                        + "| facesUserFromDB: " + extractedUser.getLName());
                // get the user's default municipality
                Municipality muni = extractedUser.getMuni();
                getSessionBean().setActiveMuni(muni);
                // grab code set ID from the muni object, ask integrator for the CodeSet object, 
                //and then and store in sessionBean
                getSessionBean().setActiveCodeSet(ci.getCodeSetBySetID(muni.getDefaultCodeSetID()));
            
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Good morning, " + extractedUser.getFName() + "!", ""));
            }
        
        } catch (IntegrationException ex) {
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
     * @return the glassfishUser
     */
    public String getGlassfishUser() {
        
        FacesContext fc = getFacesContext();
        ExternalContext ec = fc.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) ec.getRequest();
        String sessionUserName = request.getRemoteUser();
        System.out.println("SessionInitializer.getGlassfishUserName | " + sessionUserName);
        glassfishUser = sessionUserName;
        return glassfishUser;
    }

    /**
     * @return the sessionID
     */
    public String getSessionID() {
        
        FacesContext fc = getFacesContext();
        HttpSession session = (HttpSession) fc.getExternalContext().getSession(false);
        // prints out the current session attributes to standard out
//        Enumeration e = session.getAttributeNames();
//        System.out.println("SessionInitailzier.getSessionID | Dumping lots of attrs");
//        while (e.hasMoreElements())
//        {
//          String attr = (String)e.nextElement();
//          System.out.println("      attr  = "+ attr);
//          Object value = session.getValue(attr);
//          System.out.println("      value = "+ value);
//        }
        sessionID = session.getId();
        return sessionID;
    }

    /**
     * @return the retrievedCOGUser
     */
    public User getRetrievedCOGUser() {
        return retrievedCOGUser;
    }

    /**
     * @param glassfishUser the glassfishUser to set
     */
    public void setGlassfishUser(String glassfishUser) {
        this.glassfishUser = glassfishUser;
    }

    /**
     * @param sessionID the sessionID to set
     */
    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    /**
     * @param retrievedCOGUser the retrievedCOGUser to set
     */
    public void setRetrievedCOGUser(User retrievedCOGUser) {
        this.retrievedCOGUser = retrievedCOGUser;
    }
    
}
