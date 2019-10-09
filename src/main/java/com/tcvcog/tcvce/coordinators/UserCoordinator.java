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
import com.tcvcog.tcvce.entities.AccessKeyCard;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.RoleType;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthorizationPeriod;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.UserListified;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    
    private Municipality determineDefaultMuni(  User u, 
                                                Map<Municipality, UserAuthorizationPeriod> muniPerMap) 
                                                        throws AuthorizationException{
        
        UserAuthorizationPeriod  userAuthPeriod;
        UserAuthorized authUser = null;
        Municipality defMuni = null;
        
        if(muniPerMap == null || muniPerMap.isEmpty()){
            throw new AuthorizationException("Suspicious call to configInitialUserAuth; no auths supplied");
        }
        
        for(UserAuthorizationPeriod uap: muniPerMap.values()){
            if(uap.isDefaultMuni()){ // take our first muni declared default
                defMuni = uap.getMuni();
            }
        }
        // backup logic for when no muni is declared default; pick one
        if(defMuni == null){
            List<UserAuthorizationPeriod> lst;
            lst = new ArrayList<>(muniPerMap.values());
            defMuni = lst.get(0).getMuni();
        } 
        return defMuni;
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
    
    private UserAuthorized generateUserAuthorized(  User u, 
                                                    UserAuthorizationPeriod uap) throws AuthorizationException{
        
        UserAuthorized ua = null;
        if(u.getUserID() == uap.getUserID()){
            AccessKeyCard acc = generateAccessKeyCard(uap);
            ua = new UserAuthorized(u, uap, acc);
            ua.setGoverningAuthPeriod(uap);
        } else {
            throw new AuthorizationException("incorrect User and AuthorizationPeriod pairing; No UserAuthorized will be generated.");
        }
        return ua;
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
    private Map<Municipality, UserAuthorizationPeriod> assembleValidAuthPeriodMap(User u) 
                                                throws  IntegrationException, 
                                                        AuthorizationException{
        UserIntegrator ui = getUserIntegrator();
        List<UserAuthorizationPeriod> candidatePeriods;
        Map<Municipality, UserAuthorizationPeriod> muniPerMap = new HashMap<>();
        
        candidatePeriods = ui.getUserAuthorizationPeriods(u);
        if(!candidatePeriods.isEmpty()){
            for(UserAuthorizationPeriod uap: candidatePeriods){
                // Filter out deactivated records and expired records
                if(uap.getRecorddeactivatedTS() == null 
                        && uap.getStartDate().isBefore(LocalDateTime.now())
                        && uap.getStopDate().isAfter(LocalDateTime.now())){
                    //  check for existing muni periods and use the most recent valid period
                    if(muniPerMap.containsKey(uap.getMuni())){
                        UserAuthorizationPeriod existingRecord = muniPerMap.get(uap.getMuni());
                        if(uap.getCreatedTS().isAfter(existingRecord.getCreatedTS())){
                            // we found a newer record, so swap it out
                            muniPerMap.put(uap.getMuni(), uap);
                        }
                    } else {
                        // no existing record for that muni, so add it to the map
                        muniPerMap.put(uap.getMuni(), uap);
                    }
                }
            } // close for over candidates
        } else {
            throw new AuthorizationException("No candidate authorization periods exist for user");
        }
        return muniPerMap;
    }
    
    
    /**
     * Primary user retrieval method: Note that there aren't as many checks here
     * since the jboss container is managing the lookup of authenticated users. 
     * We are pulling the login name from the already authenticated jboss session user 
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
        usr = ui.getUser(authUserID);
        
        if(usr != null){ // make sure we have a real user in the system
            muniAuthMap = assembleValidAuthPeriodMap(usr);
            if(targetMuni == null){ // from a session initialization
                targetMuni = determineDefaultMuni(usr, muniAuthMap);
            } 
            au = generateUserAuthorized(usr, muniAuthMap.get(targetMuni));
            au.setMuniAuthPerMap(muniAuthMap);
        } else {
            throw new AuthorizationException("The database could not retrieve the User skeleton");
        }
        
        return au;
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
            if(rt.getRank() < user.getKeyCard().getAuthRole().getRank()
                    || user.getKeyCard().getAuthRole() == RoleType.Developer){
                rtlAuthorized.add(rt);
            }
        }
        return rtlAuthorized;
    }
    
    public void insertNewUserAuthorizationPeriod(User requestingUser, User usee, UserAuthorizationPeriod uap) throws AuthorizationException, IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        if(uap != null && requestingUser != null && usee != null && uap.getMuni() != null){
            if(uap.getStartDate() != null && !uap.getStartDate().isAfter(LocalDateTime.now().minusYears(1))){
                if(uap.getStopDate() != null && !uap.getStopDate().isAfter(LocalDateTime.now())){
                    uap.setCreatedBy(requestingUser);
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
    private AccessKeyCard generateAccessKeyCard(UserAuthorizationPeriod uap){
        AccessKeyCard card = null;
        
        switch(uap.getAuthorizedRole()){
            case Developer: 
                card = new AccessKeyCard(   uap.getUserID(),
                                            uap.getMuni(),
                                            uap.getAuthorizedRole(),
                                            true,   //developer
                                            true,   // sysadmin
                                            true,   // cogstaff
                                            true,   // enfOfficial
                                            true,   // muniStaff
                                            true);  // muniReader
               break;
            
            case SysAdmin:
                card = new AccessKeyCard(   uap.getUserID(),
                                            uap.getMuni(),
                                            uap.getAuthorizedRole(),
                                            false,   //developer
                                            true,   // sysadmin
                                            true,   // cogstaff
                                            true,   // enfOfficial
                                            true,   // muniStaff
                                            true);  // muniReader
               break;               
               
            case CogStaff:
                card = new AccessKeyCard(   uap.getUserID(),
                                            uap.getMuni(),
                                            uap.getAuthorizedRole(),
                                            false,   //developer
                                            false,   // sysadmin
                                            true,   // cogstaff
                                            false,   // enfOfficial
                                            true,   // muniStaff
                                            true);  // muniReader
               break;               
               
            case EnforcementOfficial:
                card = new AccessKeyCard(   uap.getUserID(),
                                            uap.getMuni(),
                                            uap.getAuthorizedRole(),
                                            false,   //developer
                                            false,   // sysadmin
                                            false,   // cogstaff
                                            true,   // enfOfficial
                                            true,   // muniStaff
                                            true);  // muniReader
               break;
               
            case MuniStaff:
                card = new AccessKeyCard(   uap.getUserID(),
                                            uap.getMuni(),
                                            uap.getAuthorizedRole(),
                                            false,   //developer
                                            false,   // sysadmin
                                            false,   // cogstaff
                                            false,   // enfOfficial
                                            true,   // muniStaff
                                            true);  // muniReader
               break;
               
            case MuniReader:
                card = new AccessKeyCard(   uap.getUserID(),
                                            uap.getMuni(),
                                            uap.getAuthorizedRole(),
                                            false,   //developer
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
     * First whack at a logic implementation to determine session authorizations
     * given the access record from the DB
     * 
     * @deprecated much too combersome to work with date pairs for each role type!
     * Method now maintained as an archive of the development process
     * @param userWAccess
     * @param m
     * @return
     * @throws AuthorizationException 
     */
    private User configureUserMuniAccess(UserAuthorized userWAccess, Municipality m) throws AuthorizationException{
        
        
        /**
        
        if(userWAccess == null || m == null){
            throw new AuthorizationException("UserCoordinator.configureUserMuniAccess | Incoming user or muni is null");
        }
        
        if( m.getMuniCode() == userWAccess.getAccessRecord().getMuni_municode()
                &&
            userWAccess.getAccessRecord().getAccessgranteddatestart().isBefore(LocalDateTime.now())
                &&
            userWAccess.getAccessRecord().getAccessgranteddatestop().isAfter(LocalDateTime.now())
                &&
            userWAccess.getAccessRecord().getRecorddeactivatedts() == null){
            
                // the current user is allowed access to this muni, so now determine RoleType
                // based on assigned start and stop dates for various roles as specified in the
                // current UserAccessRecord
                if(userWAccess.getAccessRecord().getSupportstartdate() != null
                        && userWAccess.getAccessRecord().getSupportstopdate() != null
                        && userWAccess.getAccessRecord().getSupportstartdate().isBefore(LocalDateTime.now())
                        && userWAccess.getAccessRecord().getSupportstopdate().isAfter(LocalDateTime.now())){
                    userWAccess.setRoleType(RoleType.Developer);
                } else if(userWAccess.getAccessRecord().getSysadminstartdate() != null
                        && userWAccess.getAccessRecord().getSysadminstopdate() != null
                        && userWAccess.getAccessRecord().getSysadminstartdate().isBefore(LocalDateTime.now())
                        && userWAccess.getAccessRecord().getSysadminstopdate().isAfter(LocalDateTime.now())){
                    userWAccess.setRoleType(RoleType.SysAdmin);
                    
                } else if(userWAccess.getAccessRecord().getCodeofficerstartdate() != null
                        && userWAccess.getAccessRecord().getCodeofficerstopdate() != null
                        && userWAccess.getAccessRecord().getCodeofficerstartdate().isBefore(LocalDateTime.now())
                        &&userWAccess.getAccessRecord().getCodeofficerstopdate().isAfter(LocalDateTime.now())){
                    userWAccess.setRoleType(RoleType.EnforcementOfficial);
                } else if(userWAccess.getAccessRecord().getStaffstartdate() != null
                        && userWAccess.getAccessRecord().getStaffstopdate() != null
                        && userWAccess.getAccessRecord().getStaffstartdate().isBefore(LocalDateTime.now())
                        && userWAccess.getAccessRecord().getStaffstopdate().isAfter(LocalDateTime.now())){
                    userWAccess.setRoleType(RoleType.MuniStaff);
                } else {
                    userWAccess.setRoleType(RoleType.MuniReader);
                }
                userWAccess.setKeyCard(getAccessKeyCard(userWAccess.getRoleType()));
            return userWAccess;
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
   
    
     public UserAuthorizationPeriod initializeNewAuthPeriod( UserAuthorized requestor, 
                                                            User requestee, 
                                                            Municipality m){
        UserAuthorizationPeriod per = null;

        // Only Users who have sys admin permission in the requested muni or are devs
        if((requestor.getGoverningAuthPeriod().getMuni().getMuniCode() == m.getMuniCode()
                && requestor.getKeyCard().isHasSysAdminPermissions())
                || requestor.getKeyCard().isHasDeveloperPermissions()){
            per = new UserAuthorizationPeriod(m);
            per.setStartDate(LocalDateTime.now());
            per.setStopDate(LocalDateTime.now().plusYears(1));
        }
        return per;
    }
     

     
    public void invalidateUserAuthPeriod(UserAuthorizationPeriod aup, User u) throws IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        ui.invalidateUserAuthRecord(aup);
        
    }
    
   
     /**
      * Given a single user, coordinates the creation of a list of Users who 
      * that user can configure.
      * @param u
      * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
      */
     public List<UserListified> getUsersForConfiguration(User u) throws IntegrationException{
         UserIntegrator ui = getUserIntegrator();
         List<User> uListComp = ui.getCompleteActiveUserList();
         List<UserListified> uListConfig = new ArrayList<>();
         for(User usr: uListComp){
             uListConfig.add(ui.getUserListified(usr));
         }
         return uListConfig;
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
