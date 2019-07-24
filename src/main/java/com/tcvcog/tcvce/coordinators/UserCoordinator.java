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
import com.tcvcog.tcvce.domain.IntegrationException;
import java.io.Serializable;
import com.tcvcog.tcvce.entities.AccessKeyCard;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.RoleType;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserWithAccessData;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author cedba
 */
public class UserCoordinator extends BackingBeanUtils implements Serializable {
    final int MIN_PSSWD_LENGTH = 8;
    
    /**
     * Creates a new instance of UserCoordinator
     */
    public UserCoordinator(){
    
    }    
    
    /**
     * Primary user retrieval method: Note that there aren't as many checks here
     * since the jboss container is managing the lookup of authenticated users. 
     * We are pulling the login name from the already authenticated glassfish user 
     * and just grabbing their profile from the db
     * 
     * @param loginName
     * @return the fully baked cog user
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.AuthorizationException occurs if the user
     * has been retrieved from the database but their access has been toggled off
     */
    public UserWithAccessData getUserWithAccessData(String loginName) throws IntegrationException, AuthorizationException{
        System.out.println("UserCoordinator.getUser | given by jboss: " + loginName );
        UserWithAccessData authenticatedUser = null;
        UserIntegrator ui = getUserIntegrator();
        // convert the login name given to us by the JBoss container into a local ID num
        int authUserID = ui.getUserID(loginName);
        // use this id number to figure out which muni to grant initial auth to
        Municipality m = ui.getUserDefaultMunicipality(authUserID);
        // retrieve the access record from the DB for use in computing access permissions
        authenticatedUser = ui.getUserWithAccessData(authUserID, m);
        // now configure the user's permissions for this specific muni
        configureUserMuniAccess(authenticatedUser, m);
        // send back our lovely fancy UserWithAccessData
        return authenticatedUser;
    }
    
    private User configureUserMuniAccess(UserWithAccessData u, Municipality m) throws AuthorizationException{
        
        if( m.getMuniCode() == u.getAccessRecord().getMuni_municode()
                &&
            u.getAccessRecord().getAccessgranteddatestart().isBefore(LocalDateTime.now())
                &&
            u.getAccessRecord().getAccessgranteddatestop().isAfter(LocalDateTime.now())
                &&
            u.getAccessRecord().getRecorddeactivatedts() != null){
            
                // the current user is allowed access to this muni, so now determine RoleType
                // based on assigned start and stop dates for various roles as specified in the
                // current UserAccessRecord
                if(u.getAccessRecord().getSupportstartdate().isBefore(LocalDateTime.now())
                        &&
                    u.getAccessRecord().getSupportstopdate().isAfter(LocalDateTime.now())){
                    u.setRoleType(RoleType.Developer);
                } else if(u.getAccessRecord().getSysadminstartdate().isBefore(LocalDateTime.now())
                        &&
                    u.getAccessRecord().getSysadminstopdate().isAfter(LocalDateTime.now())){
                    u.setRoleType(RoleType.SysAdmin);
                    
                } else if(u.getAccessRecord().getCodeofficerstartdate().isBefore(LocalDateTime.now())
                        &&
                    u.getAccessRecord().getCodeofficerstopdate().isAfter(LocalDateTime.now())){
                    u.setRoleType(RoleType.EnforcementOfficial);
                } else if(u.getAccessRecord().getStaffstartdate().isBefore(LocalDateTime.now())
                        &&
                    u.getAccessRecord().getStaffstopdate().isAfter(LocalDateTime.now())){
                    u.setRoleType(RoleType.MuniStaff);
                } else {
                    u.setRoleType(RoleType.MuniReader);
                }
                
                u.setKeyCard(getAccessKeyCard(u.getRoleType()));
                
            return u;
        } else {
            throw new AuthorizationException("User exists but access to system "
                    + "has been switched off. If you believe you are receiving "
                    + "this message in error, please contact system administrator "
                    + "Eric Darsow at 412.923.9907.");
        }
    }
    
    
    public int insertNewUser(User u) throws IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        String tempPassword = String.valueOf(generateControlCodeFromTime());
//        u.setPassword(tempPassword);
        int newUserID = ui.insertUser(u);
        return newUserID;
        
        
    }
    
    /**
     * The COGBot is a user that exists only in cyberspace and is used 
     * as the owner of events created by the public and can also make requests
     * to users at various times for various reasons. No human should ever
     * attempt to take on the role of the COGBot for risk of becoming a cyborg
     * is, indeed, very great.
     * @return
     * @throws IntegrationException 
     */
    public User getRobotUser() throws IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        User u;
        u = ui.getUser(Integer.parseInt(
                getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                        .getString("cogRobotUserID")));
        return u;
    }
    
  
    
    
    public void updateUser(User u) throws IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        ui.updateUser(u);
    }
    
    public void updateUserPassword(User u, String pw) throws IntegrationException, AuthorizationException{
        UserIntegrator ui = getUserIntegrator();
        if(pw.length() >= MIN_PSSWD_LENGTH){
            ui.setUserPassword(u, pw);
        } else {
            throw new AuthorizationException("Password must be at least " + MIN_PSSWD_LENGTH + " characters");
        }
    }
    
    public User getUserSkeleton(){
        User u = new User();
        return u;
    }
   
    
    public Municipality getDefaultyMuni(User u) throws IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        return ui.getUserDefaultMunicipality(u.getUserID());
    }
    
    public boolean setDefaultMuni(User u, Municipality m) throws IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        return ui.setDefaultMunicipality(u, m);
        
    }
    
    public List<Municipality> getUserAuthMuniList(int userID) throws IntegrationException{
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        List<Municipality> ml = mi.getUserAuthMunis(userID);
        return ml;
    }
    
    
    /**
     * Container for all access control mechanism authorization switches
     * 
     * Design goal: have boolean type getters on users for use by the View
     * to turn on and off rendering and enabling as needed.
     * 
     * NOTE: This is the ONLY method system wide that calls any setters for
     * access permissions
     * 
     * @param rt
     * @return a User object whose access controls switches are configured
     */
    private AccessKeyCard getAccessKeyCard(RoleType rt){
        AccessKeyCard card = null;
        
        switch(rt){
            case Developer:
                card = new AccessKeyCard( true,   //developer
                                    true,   // sysadmin
                                    true,   // cogstaff
                                    true,   // enfOfficial
                                    true,   // muniStaff
                                    true);  // muniReader
               break;
            
            case SysAdmin:
                card = new AccessKeyCard( false,   //developer
                                    true,   // sysadmin
                                    true,   // cogstaff
                                    true,   // enfOfficial
                                    true,   // muniStaff
                                    true);  // muniReader
               break;               
               
            case CogStaff:
                card = new AccessKeyCard( false,   //developer
                                    false,   // sysadmin
                                    true,   // cogstaff
                                    false,   // enfOfficial
                                    true,   // muniStaff
                                    true);  // muniReader
               break;               
               
            case EnforcementOfficial:
                card = new AccessKeyCard( false,   //developer
                                    false,   // sysadmin
                                    false,   // cogstaff
                                    true,   // enfOfficial
                                    true,   // muniStaff
                                    true);  // muniReader
               break;
               
            case MuniStaff:
                card = new AccessKeyCard( false,   //developer
                                    false,   // sysadmin
                                    false,   // cogstaff
                                    false,   // enfOfficial
                                    true,   // muniStaff
                                    true);  // muniReader
               break;
               
            case MuniReader:
                card = new AccessKeyCard( false,   //developer
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
    
    /**
     * Return a list of Municipalities that User u does not currently have the
     * authorization to access.
     * @param u the user in question
     * @return A list of Municipality objects that the user does not have 
     *     access to  
     * @throws IntegrationException 
     */
    public List<Municipality> getUnauthorizedMunis(User u) throws IntegrationException {
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        
        UserIntegrator ui = getUserIntegrator();
        List<Municipality> authMunis = mi.getUserAuthMunis(u.getUserID());        
        List<Municipality> munis = mi.getMuniList();
        
        if(authMunis != null){
            for(Municipality authMuni:authMunis){
                munis.remove(authMuni);
            }
        }        
        return munis;
    }
    
    
    
} // close class
