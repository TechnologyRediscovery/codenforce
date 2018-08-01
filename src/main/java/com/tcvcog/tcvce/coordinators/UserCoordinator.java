/*
 * Copyright (C) 2017 cedba
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
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.DataStoreException;
import com.tcvcog.tcvce.domain.IntegrationException;
import java.sql.Connection;
import java.io.Serializable;
import com.tcvcog.tcvce.domain.ObjectNotFoundException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Property;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.integration.UserIntegrator;
import javax.faces.application.FacesMessage;

/**
 *
 * @author cedba
 */
@ApplicationScoped
@Named("userCoordinator")
public class UserCoordinator extends BackingBeanUtils implements Serializable {

     Connection con = null;
    
    /**
     * Creates a new instance of UserCoordinator
     */
    public UserCoordinator(){
       
        
    }
    
    /**
     * Responds to login requests by taking the loginName and loginPassword
     * and searching the database for a registered user. If a user is found
     * in the DB, a User object is created and returned, allow the user to progress 
     * pass the authentication screen.
     * 
     * @deprecated due to the switch to glassfish container security. getUser(Stringloginname) instead
     * @param loginName the login name entered by the user
     * @param loginPassword the password entered by the user
     * 
     * @return a User object loaded up with various attributes of the user, 
     * including the user's role in the system.
     * 
     * @throws com.tcvcog.tcvce.domain.ObjectNotFoundException
     * @throws com.tcvcog.tcvce.domain.DataStoreException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public User getUser(String loginName, String loginPassword) 
            throws ObjectNotFoundException, DataStoreException, IntegrationException {
        System.out.println("UserCoordinator.geUser | given: " + loginName + " " + loginPassword);
        con = getPostgresCon();
        User authenticatedUser = null;
        UserIntegrator ui = getUserIntegrator();
        
        authenticatedUser = ui.getAuthenticatedUser(loginName, loginPassword);
        if (authenticatedUser != null){
            
            getSessionBean().setActiveUser(authenticatedUser);
        }
         
        return authenticatedUser;
        
    } // close getUser()
    
    /**
     * Primary user retrieval method: Note that there aren't as many checks here
     * since the glassfish container is managing the lookup of authenticated uers. 
     * We are pulling the login name from the already authenticated glassfish user 
     * and just grabbing their profile from the db
     * 
     * @param loginName
     * @return the fully baked cog user
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.AuthorizationException occurs if the user
     * has been retrieved from the database but their access has been toggled off
     */
    public User getUser(String loginName) throws IntegrationException, AuthorizationException{
        System.out.println("UserCoordinator.geUser | given: " + loginName );
        User authenticatedUser;
        UserIntegrator ui = getUserIntegrator();
                con = getPostgresCon();
        authenticatedUser = ui.getUser(loginName);
        if(authenticatedUser.isAccessPermitted()){
            return authenticatedUser;
            
        } else {
            
            throw new AuthorizationException("User exists but access to system "
                    + "has been switched off. If you believe you are receiving "
                    + "this message in error, please contact system administrator "
                    + "Eric Darsow at 412.923.9907.");
        }
    }
    
} // close class
