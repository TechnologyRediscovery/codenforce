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

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.IntegrationException;
import java.io.Serializable;
import com.tcvcog.tcvce.entities.UserAuthCredential;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.RoleType;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthPeriod;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.UserConfigReady;
import com.tcvcog.tcvce.entities.UserMuniAuthPeriodLogEntry;
import com.tcvcog.tcvce.entities.UserMuniAuthPeriodLogEntryCatEnum;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
     * Pass-through method to the converter method on the Integrator which
     * returns a userid from a String username. Used only on first session auth
     * when JBoss gives us a username from the login system
     * 
     * @param userName
     * @return
     * @throws IntegrationException 
     */
    public int getUserID(String userName) throws IntegrationException{
         UserIntegrator ui = getUserIntegrator();
         return ui.getUserID(userName);
     }
    
    private Municipality determineInitMuni(  UserAuthorized ua, 
                                                Map<Municipality, UserAuthPeriod> muniPerMap) 
                                                        throws AuthorizationException{
        Municipality initMuni = null;
        
        UserAuthPeriod workingUAP = null;
        int maxRank = Integer.MIN_VALUE;
        
        if(ua ==  null || muniPerMap == null || muniPerMap.isEmpty()){
            throw new AuthorizationException("Suspicious call to configInitialUserAuth; no AuthUser supplied");
        }
        // interate over each municipality to which the user has a valid AuthPeriod
        for (Municipality mu : muniPerMap.keySet()) {
            workingUAP = muniPerMap.get(mu);
            if(initMuni == null || workingUAP.getAssignmentRank() > maxRank) {
                initMuni = muniPerMap.get(mu).getMuni();
                maxRank = workingUAP.getAssignmentRank();
            }
        }
        return initMuni;
    }
    
    public boolean validateUserMuniAuthPeriod(UserAuthPeriod uap){
        boolean valid = true;
        if(         uap.getRecorddeactivatedTS() == null 
                ||  uap.getStartDate().isBefore(LocalDateTime.now())
                ||  uap.getStopDate().isAfter(LocalDateTime.now())){
            valid = false;
        }
        return valid;
    }
    
    public String generateRandomPassword(){
        java.math.BigInteger bigInt = new BigInteger(1024, new Random());
        String randB64 = Base64.encode(bigInt.toByteArray());
        StringBuilder sb = new StringBuilder();
        sb.append(randB64.substring(0,3));
        sb.append("-");
        sb.append(randB64.substring(randB64.length()-3,randB64.length()));
        sb.append("-");
        sb.append(randB64.substring(randB64.length()-3,randB64.length()));
        return sb.toString();
        
    }
    
    
     /**
     * Primary user retrieval method: Note that there aren't as many checks here
     * since the jboss container is managing the lookup of authenticated users. 
     * We are pulling the login name from the already authenticated jboss session user 
     * and just grabbing their profile from the db
     * 
     * @param usr
     * @param muni
     * @return the fully baked cog user
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.AuthorizationException occurs if the user
     * has been retrieved from the database but their access has been toggled off
     */
    public UserAuthorized authorizeUser(User usr, Municipality muni) throws AuthorizationException, IntegrationException{
        UserIntegrator ui = getUserIntegrator();    
        UserAuthorized usrAuth;
        Map<Municipality, UserAuthPeriod> muniAuthMap = assembleValidAuthPeriodMap(usr);
        UserAuthPeriod wuap;
        
        if(!muniAuthMap.isEmpty()){
            usrAuth = ui.getUserAuthorized(new UserAuthorized(usr));
            if(muni != null){
                // Meaning we have an internal switch from the initial muni to a new one
                wuap = muniAuthMap.get(muni);
            } else {
                // first authorization so figure out which muni to load up first
                wuap = muniAuthMap.get(determineInitMuni(usrAuth, muniAuthMap));
            }
            // setup our UserAuthorized for letting lose
            usrAuth.setCredential(generateCredential(wuap));
        } else {
            return null;
        }
        return usrAuth;
    }
    
   
    /**
     * Primary logic container for determining authorization statuses for a given User
     * across ALL existing municipalities for which the User has a record
     * @param u
     * @return A Map of all Municipalities for which the passed in User has a valid
     * authentication period record, meaning the period start/end dates include today
     * and there is not a deactivation timestamp
     * @throws IntegrationException
     * @throws AuthorizationException 
     */
    private Map<Municipality, UserAuthPeriod> assembleValidAuthPeriodMap(User u) 
                                                throws  IntegrationException, 
                                                        AuthorizationException{
        UserIntegrator ui = getUserIntegrator();
        List<UserAuthPeriod> candidatePeriods;
        Map<Municipality, UserAuthPeriod> muniPerMap = new HashMap<>();
        
        candidatePeriods = ui.getUserAuthPeriods(u);
        if(!candidatePeriods.isEmpty()){
            for(UserAuthPeriod uap: candidatePeriods){
                // Filter out deactivated records and expired records
                if(validateUserMuniAuthPeriod(uap)){
                    //  check for existing muni periods and use the most recent valid period
                    if(muniPerMap.containsKey(uap.getMuni())){
                        UserAuthPeriod existingRecord = muniPerMap.get(uap.getMuni());
                        if(uap.getCreatedTS().isAfter(existingRecord.getCreatedTS())){
                            // we found a newer record, so swap it out
                            muniPerMap.put(uap.getMuni(), uap);
                        }
                    } else {
                        // no existing record for that muni, so add it to the map
                        muniPerMap.put(uap.getMuni(), uap);
                    }
                }
            } // close for over period candidates
        } else {
            throw new AuthorizationException("No candidate authorization periods exist for user");
        }
        return muniPerMap;
    }
    
    
   
   
    /**
     * Generates a list of what role types a given user can assign to new users 
     * they create. As of Oct 2019, this logic said you can add somebody of lesser 
     * in your municipaltiy. Developers have all power.
     * @param user
     * @return 
     */
    public List<RoleType> getPermittedRoleTypesToGrant(UserAuthorized user){
        List<RoleType> rtl;
        List<RoleType> rtlAuthorized = new ArrayList<>();
        rtl = new ArrayList<>(Arrays.asList(RoleType.values()));
        for(RoleType rt: rtl){
            // only allow users to add new users of roles of lesser ranks
            if(rt.getRank() < user.getCredential().getGoverningAuthPeriod().getRole().getRank()
                    || user.getCredential().getGoverningAuthPeriod().getRole() == RoleType.Developer){
                rtlAuthorized.add(rt);
            }
        }
        return rtlAuthorized;
    }
    
    public UserMuniAuthPeriodLogEntry assembleUserMuniAuthPeriodLogEntrySkeleton(
                                        UserAuthorized ua, 
                                        UserMuniAuthPeriodLogEntryCatEnum cat){
        UserMuniAuthPeriodLogEntry skel = new UserMuniAuthPeriodLogEntry();
        skel.setAuthPeriod(ua.getCredential().getGoverningAuthPeriod());
        return skel;
    }
    
    public void logCredentialInvocation(UserMuniAuthPeriodLogEntry entry) throws IntegrationException, AuthorizationException{
        UserIntegrator ui = getUserIntegrator();
        ui.insertMuniAuthPeriodLogEntry(entry);
    }
    
    public void insertNewUserAuthorizationPeriod(User requestingUser, User usee, UserAuthPeriod uap) throws AuthorizationException, IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        if(uap != null && requestingUser != null && usee != null && uap.getMuni() != null){
            if(uap.getStartDate() != null && !uap.getStartDate().isAfter(LocalDateTime.now().minusYears(1))){
                if(uap.getStopDate() != null && !uap.getStopDate().isAfter(LocalDateTime.now())){
                    uap.setCreatedByUserID(requestingUser.getUserID());
                    uap.setUserID(usee.getUserID());
                    ui.insertNewUserAuthorizationPeriod(uap);
                } else {
                    throw new AuthorizationException("Stop date must be not null and in the future");
                }
            } else {
                throw new AuthorizationException("Start date must not be or dated more than a year past");
            }
        } else {
            throw new AuthorizationException("One or more required objects is null");
        }
        
        
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
    private UserAuthCredential generateCredential(UserAuthPeriod uap){
        UserAuthCredential cred = null;
        
        switch(uap.getRole()){
            case Developer: 
                cred = new UserAuthCredential(  uap,
                                                true,   //developer
                                                true,   // sysadmin
                                                true,   // cogstaff
                                                true,   // enfOfficial
                                                true,   // muniStaff
                                                true);  // muniReader
               break;
            
            case SysAdmin:
                cred = new UserAuthCredential(  uap,
                                                false,   //developer
                                                true,   // sysadmin
                                                true,   // cogstaff
                                                true,   // enfOfficial
                                                true,   // muniStaff
                                                true);  // muniReader
               break;               
               
            case CogStaff:
                cred = new UserAuthCredential(  uap,
                                                false,   //developer
                                                false,   // sysadmin
                                                true,   // cogstaff
                                                false,   // enfOfficial
                                                true,   // muniStaff
                                                true);  // muniReader
               break;               
               
            case EnforcementOfficial:
                cred = new UserAuthCredential(  uap,
                                                false,   //developer
                                                false,   // sysadmin
                                                false,   // cogstaff
                                                true,   // enfOfficial
                                                true,   // muniStaff
                                                true);  // muniReader
               break;
               
            case MuniStaff:
                cred = new UserAuthCredential(  uap,
                                                false,   //developer
                                                false,   // sysadmin
                                                false,   // cogstaff
                                                false,   // enfOfficial
                                                true,   // muniStaff
                                                true);  // muniReader
               break;
               
            case MuniReader:
                cred = new UserAuthCredential(  uap,
                                                false,   //developer
                                                false,   // sysadmin
                                                false,   // cogstaff
                                                false,   // enfOfficial
                                                false,   // muniStaff
                                                true);  // muniReader
               break;               
            default:
        }        
        return cred;
    }    
    
   
    
    /**
     * @param usr
     * @return
     * @throws IntegrationException 
     */
    public int insertNewUser(User usr) throws IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        int newUserID = ui.insertUser(usr);
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
    public User getUserRobot() throws IntegrationException{
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
    
    public User getUserSkeleton(User u){
        User skel = new User();
        return skel;
    }
   
    
     public UserAuthPeriod initializeNewAuthPeriod( UserAuthorized requestor, 
                                                            User requestee, 
                                                            Municipality m){
        UserAuthPeriod per = null;

        // Only Users who have sys admin permission in the requested muni or are devs
        if((requestor.getCredential().getGoverningAuthPeriod().getMuni().getMuniCode() == m.getMuniCode()
                && requestor.getCredential().isHasSysAdminPermissions())
                || requestor.getCredential().isHasDeveloperPermissions()){
            per = new UserAuthPeriod(m);
            per.setStartDate(LocalDateTime.now());
            per.setStopDate(LocalDateTime.now().plusYears(1));
        }
        return per;
    }
     

     
    public void invalidateUserAuthPeriod(UserAuthPeriod aup, User u) throws IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        ui.invalidateUserAuthRecord(aup);
        
    }
    
    public User getUser(int userID) throws IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        return ui.getUser(userID);
    }
    
    public List<UserAuthorized> getUserList(Municipality m) throws IntegrationException, AuthorizationException{
        UserIntegrator ui = getUserIntegrator();
        List<UserAuthorized> ual = ui.getUserAuthorizedList(m);
        for(UserAuthorized usrAuth: ual){
            if(validateUserMuniAuthPeriod(usrAuth.getCredential().getGoverningAuthPeriod())){
                if(!ual.contains(usrAuth)){
                    ual.add(usrAuth);
                }
            }
        }
        return ual;
    }
    
   
     /**
      * Given a single user, coordinates the creation of a list of Users who 
      * that user can configure.
     * @param ua
      * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
      */
     public List<UserConfigReady> getUsersForConfiguration(UserAuthorized ua) throws IntegrationException, AuthorizationException{
         UserIntegrator ui = getUserIntegrator();
         
         List<UserAuthorized> userAuthInMuniList = new ArrayList<>();
         List<UserConfigReady> userAllowedList = new ArrayList<>();
         
         for(Municipality m: ua.getMuniAuthPeriodMap().keySet()){
             userAuthInMuniList = ui.getUserAuthorizedList(m);
             for(UserAuthorized u: userAuthInMuniList){
                switch(ua.getRoleType()){
                    case SysAdmin:
                        if(u.getRoleType() != RoleType.Developer){
                            userAllowedList.add(ui.getUserConfigReady(u));
                            break;
                        } else {
                            break;
                        }
                    
                    case Developer:
                        userAllowedList.add(ui.getUserConfigReady(u));
                        break;
                    
                    default:
                        break;
                }
             } // user for
         } // muni for
         return userAllowedList;
     }
    
     /**
      * Coordinates the creation of a list of all a particular user's Authorized
      * municipalities
      * @param userID
      * @return
      * @throws IntegrationException 
      */
    public List<Municipality> getUserAuthMuniList(int userID) throws IntegrationException{
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        List<Municipality> ml = mi.getUserAuthMunis(userID);
        return ml;
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
