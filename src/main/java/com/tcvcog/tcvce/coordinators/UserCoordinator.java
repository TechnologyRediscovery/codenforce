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
import java.io.Serializable;
import com.tcvcog.tcvce.domain.ObjectNotFoundException;
import com.tcvcog.tcvce.entities.KeyCard;
import com.tcvcog.tcvce.entities.RoleType;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.integration.UserIntegrator;

/**
 *
 * @author cedba
 */
@ApplicationScoped
@Named("userCoordinator")
public class UserCoordinator extends BackingBeanUtils implements Serializable {

    
    /**
     * Creates a new instance of UserCoordinator
     */
    public UserCoordinator(){
       
        
    }
    
    /**
     * DEPRECATED with move to glassfish servlet authentication
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
    private User getUser(String loginName, String loginPassword) 
            throws ObjectNotFoundException, DataStoreException, IntegrationException {
        System.out.println("UserCoordinator.geUser | given: " + loginName + " " + loginPassword);
        User authenticatedUser = null;
        UserIntegrator ui = getUserIntegrator();
        
//        authenticatedUser = ui.getAuthenticatedUser(loginName, loginPassword);
        if (authenticatedUser != null){
            
            // session bean doesn't store user--it's in the context map
            //getSessionBean().setActiveUser(authenticatedUser);
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
        authenticatedUser = ui.getUser(loginName);
        // integrator sets high level system access permissions
        if(authenticatedUser.isSystemAccessPermitted()){
            // set user permissions with the role type that comes from the DB
            // which the Integrator sets
            authenticatedUser.setKeyCard(acquireAccessKeyCard(authenticatedUser.getRoleType()));
            return authenticatedUser;
            
        } else {
            
            throw new AuthorizationException("User exists but access to system "
                    + "has been switched off. If you believe you are receiving "
                    + "this message in error, please contact system administrator "
                    + "Eric Darsow at 412.923.9907.");
        }
    }
    
    /**
     * Container for all access control mechanism authorization switches
     * 
     * NOTE: This is the ONLY method system wide that calls any setters for
     * access permissions
     * 
     * @param rt
     * @return a User object whose access controls switches are configured
     */
    private KeyCard acquireAccessKeyCard(RoleType rt){
        KeyCard card = null;
        
        switch(rt){
            case Developer:
                card = new KeyCard( true,   //developer
                                    true,   // sysadmin
                                    true,   // cogstaff
                                    true,   // enfOfficial
                                    true,   // muniStaff
                                    true);  // muniReader
               break;
            
            case SysAdmin:
                card = new KeyCard( false,   //developer
                                    true,   // sysadmin
                                    true,   // cogstaff
                                    false,   // enfOfficial
                                    true,   // muniStaff
                                    true);  // muniReader
               break;
               
               
            case CogStaff:
                card = new KeyCard( false,   //developer
                                    false,   // sysadmin
                                    true,   // cogstaff
                                    false,   // enfOfficial
                                    true,   // muniStaff
                                    true);  // muniReader
               break;
               
               
            case EnforcementOfficial:
                card = new KeyCard( false,   //developer
                                    false,   // sysadmin
                                    false,   // cogstaff
                                    true,   // enfOfficial
                                    true,   // muniStaff
                                    true);  // muniReader
               break;
               
            case MuniStaff:
                card = new KeyCard( false,   //developer
                                    false,   // sysadmin
                                    false,   // cogstaff
                                    false,   // enfOfficial
                                    true,   // muniStaff
                                    true);  // muniReader
               break;
               
            case MuniReader:
                card = new KeyCard( false,   //developer
                                    false,   // sysadmin
                                    false,   // cogstaff
                                    false,   // enfOfficial
                                    false,   // muniStaff
                                    true);  // muniReader
               break;
               
               
            default:
               
        }
        
        return card;
    }
    
} // close class
