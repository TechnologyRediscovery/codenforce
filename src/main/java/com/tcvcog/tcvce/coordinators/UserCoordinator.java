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
import com.tcvcog.tcvce.entities.Credential;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserMuniAuthPeriod;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.UserAuthorizedForConfig;
import com.tcvcog.tcvce.entities.UserMuniAuthPeriodLogEntry;
import com.tcvcog.tcvce.entities.UserMuniAuthPeriodLogEntryCatEnum;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * *** SECURITY SENSITIVE CLASS ***
 * Primary business logic container for all things User and access related
 * All changes to this class should be made by knowledgeable folks only;
 * System are in place to check various organ function multiple times
 * before allowing changes. As such, AuthorizationExceptionsa are thrown
 * to client methods a bunch, most of which live in the UserConfigBB
 * 
 * Note the method prefix schema divides the methods roughly between 
 * auth_ which are methods pertaining mostly to authorization as a security event whose client
 * is the SessionInitializer 
 * <br>
 * user_ which are methods governing the creation, editing, and management of the User
 * object world, which is both a data concept and a security-backing class
 * 
 * @author Ellen Bascomb (Apartment: 31Y)
 */
public class UserCoordinator extends BackingBeanUtils implements Serializable {
    
    final int MIN_PSSWD_LENGTH = 8;
    final int DEFAULT_USERMUNIAUTHPERIODLENGTHYEARS = 1;
    final int DEFAULT_ASSIGNMENT_RANK = 1;
    final int PERIOD_VALIDITYBUFFERMINUTES = 10;
    
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
    public int auth_getUserID(String userName) throws IntegrationException{
         UserIntegrator ui = getUserIntegrator();
         return ui.getUserID(userName);
     }
    
   
    
    /**
     * Builds a password used for temporary user management and password resets
     * @return 
     */
    public String user_generateRandomPassword_SECURITYCRITICAL(){
        java.math.BigInteger bigInt = new BigInteger(1024, new Random());
        String randB64 = Base64.encode(bigInt.toByteArray());
        System.out.println("Randomly generated BigInt: " + randB64);
        StringBuilder sb = new StringBuilder();
        sb.append(randB64.substring(0,3));
        sb.append("-");
        sb.append(randB64.substring(4,7));
        sb.append("-");
        sb.append(randB64.substring(randB64.length()-3,randB64.length()));
        return sb.toString();
        
    }
    
    
     /**
     * Primary user retrieval method: Note that there aren't as many checks here
     * since the jboss container is managing the lookup of authenticated users. 
     * We are pulling the login name from the already authenticated jboss session user 
     * and grabbing their list of authorized periods
     * 
     * Client methods are system login backing beans; NOT config
     * 
     * @param ua
     * @param umapReq with complete Muni-UMAP map in place
     * @return the fully baked cog user
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.AuthorizationException occurs if the user
     * has been retrieved from the database but their access has been toggled off
     */
    public UserAuthorized auth_authorizeUser_SECURITYCRITICAL(UserAuthorized ua, UserMuniAuthPeriod umapReq ) throws AuthorizationException, IntegrationException{
        
        if(ua != null && umapReq != null){
            
            if(umapReq.getRecorddeactivatedTS() != null){
                throw new AuthorizationException("Cannot credentialize a deactivated authorization period!");
            }
            
            // ************************************************************
            // ******* GENERATE AND INJECT CREDENTIAL FOR CHOSEN MUNI *****
            // ************************************************************
            Credential cr = auth_generateCredential_SECURITYCRITICAL(umapReq);
            ua.setMyCredential(cr);
        } else {
            throw new AuthorizationException("UserAuthorized and requested UMAP required for auth");
            
        }

            // finally, inject the muniPeriodMap into the UA whose credential is set
        return ua;
    }
    
    /**
     * Provides its sole client of the SessionInitializer with a UserAuthorized
     * minus a credential but only valid UMAPs keyed by Muni
     * @param usr
     * @return
     * @throws IntegrationException 
     */
    public UserAuthorized auth_prepareUserForSessionChoice(User usr) throws IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        
        UserAuthorized ua = ui.getUserAuthorizedNoAuthPeriods(usr);
        ua.setMuniAuthPeriodsMap(auth_cleanMuniUMAPMap(auth_assembleMuniUMAPMapRaw(usr)));
        return ua;
    }
    
    
   
    
    /**
     * Serves the userConfigBB as its sole client who needs to display both valid
     * and invalid UMAPS to the admin user
     * @param u
     * @return 
     */
    private Map<Municipality, List<UserMuniAuthPeriod>> auth_assembleMuniUMAPMapRaw(User u){
        
        UserIntegrator ui = getUserIntegrator();
        Map<Municipality, List<UserMuniAuthPeriod>> umapMapRaw = null;
        List<UserMuniAuthPeriod> rawList = null;
        
        try {
            umapMapRaw = auth_buildMuniUMAPMap(ui.getUserMuniAuthPeriodsRaw(u.getUserID()));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            
        }
        
        return umapMapRaw;
        
        
    }
    
    /**
     * Utility method for creating a mapping of Municipality objects to 
     * lists of UMAPs for that muni
     * 
     * @param umapListRaw
     * @return 
     */
    private Map<Municipality, List<UserMuniAuthPeriod>> auth_buildMuniUMAPMap(List<UserMuniAuthPeriod> umapListRaw){
        
        Map<Municipality, List<UserMuniAuthPeriod>> tempMap = new HashMap<>();
        List<UserMuniAuthPeriod> tempUMAPList;
        
          if(umapListRaw != null && !umapListRaw.isEmpty()){
                for(UserMuniAuthPeriod umap: umapListRaw){
                    Municipality mu = umap.getMuni();
                    if(tempMap.containsKey(mu)){
                        tempUMAPList = tempMap.get(mu); // pull out our authorized peridos
                        tempUMAPList.add(umap); // add our new one
                        Collections.sort(tempUMAPList); // sort based first on role rank, then assignment order ACROSS munis
                    } else {
                        // no existing record for that muni, so make a list, inject, and put
                        tempUMAPList = new ArrayList<>();
                        tempUMAPList.add(umap);
                    }
                    tempMap.put(umap.getMuni(), tempUMAPList);
                } // close for over periods
          }
            
            return tempMap;
        
    }
    
  
    
    /**
     * Extracts all UMAP objects from the DB, valid or not
     * @param u
     * @return
     * @throws IntegrationException 
     */
    private List<UserMuniAuthPeriod> user_auth_getUMAPListRaw(User u) throws IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        return ui.getUserMuniAuthPeriodsRaw(u.getUserID());
        
    }
    
  
    
    /**
     * Creates a list of Users for use by search criterias on various pages
     * so the user can search by Users who have some past connection to any of the 
     * given Municipality objects passed into the method.
     * 
     * TODO: Customize list based on muni connections
     * NOTED in github issues by ECD 25-may-2020
     * 
     * @param usr
     * @return An assembled list of users for authorization
     */
    public List<User> user_assembleUserListForSearch(User usr){
        // we do nothing with muniList
        UserIntegrator ui = getUserIntegrator();
        
        List<User> ulst = new ArrayList<>();
        
        try {
            for(Integer i: ui.getSystemUserIDList()){
                ulst.add(user_getUser(i));
            }
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        return ulst;
        
    }
    
    /**
     * Container for business logic surrounding user period Validation
     * 
     * Returns a Period which has been evaluated for validity. The client method
     * is usually going to assess a Period as Valid if the return value of
     * getValidatedTS() is not null
     * 
     * @param uap the Period to be evaluated for validation
     * @return 
     */
    private UserMuniAuthPeriod auth_validateUserMuniAuthPeriod_SECURITYCRITICAL(UserMuniAuthPeriod uap){
        // they all get evaluated and stamped
        LocalDateTime syncNow = LocalDateTime.now();
        uap.setValidityEvaluatedTS(syncNow);
        if( 
                uap.getRecorddeactivatedTS() != null 
            ||  uap.getStartDate().isAfter(LocalDateTime.now())
            ||  uap.getStopDate().isBefore(LocalDateTime.now())
        ){
            System.out.println("UserCoordinator.validateUserMuniAuthPeriod | declared invalid: " + uap.getUserMuniAuthPeriodID());
            return uap;
        }
        // since we have a valid period, git it the extra valid stamp
        uap.setValidatedTS(syncNow);
        return uap;
    }
    
    
    
    /**
     * Internal untility method for rebuild a Muni-UMAP map with only valid UMAPS
     * @param umapMapRaw
     * @return 
     */
    private Map<Municipality, List<UserMuniAuthPeriod>> auth_cleanMuniUMAPMap(Map<Municipality, List<UserMuniAuthPeriod>> umapMapRaw){
        List<UserMuniAuthPeriod> tempUMAPList = new ArrayList<>();
        List<Municipality> tempMuniList;
        Map<Municipality, List<UserMuniAuthPeriod>> tempMap = new HashMap<>();

            if(umapMapRaw != null && !umapMapRaw.isEmpty()){
                Set<Municipality> muniSet = umapMapRaw.keySet();
                if(!muniSet.isEmpty()){
                    tempMuniList = new ArrayList(muniSet);
                    for(Municipality muni: tempMuniList){
                        tempUMAPList = umapMapRaw.get(muni);
                        tempMap.put(muni, auth_cleanUserMuniAuthPeriodList(tempUMAPList));
                    }

                }
            }
            return tempMap;
            
        }
    
    /**
     * Convenience adaptor method for checking the validity of a generic list of raw UMAPs
     * and only returning valid periods. This method also calls Collections.sort on its inputted umap list
     * so that the highest ranked valid period is first
     * 
     * @param rawUMAPList
     * @return the list of only valid UMAPs
     */
    private List<UserMuniAuthPeriod> auth_cleanUserMuniAuthPeriodList(List<UserMuniAuthPeriod> rawUMAPList){
        List<UserMuniAuthPeriod> cleanList = null; 
        //make sure we have a valid list
        if(rawUMAPList != null && !rawUMAPList.isEmpty()){
            cleanList = new ArrayList<>();
            for(UserMuniAuthPeriod umap: rawUMAPList){
                if(auth_validateUserMuniAuthPeriod_SECURITYCRITICAL(umap).getValidatedTS() != null){
                    cleanList.add(umap);
                }
            }
            /// sort our list before returning
            if(!cleanList.isEmpty()){
                Collections.sort(cleanList);
            }
        }
        return cleanList;
   }
    
    
    /**
     * *** SECURITY CRITICAL METHOD ***
     * Authorization check to that only developer users are allowed to 
     * re-initialize their session as any other user! 
     * @param ua
     * @param umap
     * @return 
     */
    public boolean auth_verifyReInitSessionRequest_SECURITYCRITICAL(UserAuthorized ua, UserMuniAuthPeriod umap){
        boolean v = false;
        if(ua.getMyCredential().isHasDeveloperPermissions()){
            v = true;
        } 
        return v;
    }
   
   
    /**
     * Generates a list of what role types a given user can assign to new users 
     * they create. As of Oct 2019, this logic said you can add somebody of lesser 
     * in your municipaltiy. Developers have all power.
     * @param user
     * @return 
     */
    public List<RoleType> auth_getPermittedRoleTypesToGrant(UserAuthorized user){
        List<RoleType> rtl;
        List<RoleType> rtlAuthorized = new ArrayList<>();
        rtl = new ArrayList<>(Arrays.asList(RoleType.values()));
        for(RoleType rt: rtl){
            // only allow users to add new users of roles of lesser ranks
            if(rt.getRank() < user.getMyCredential().getGoverningAuthPeriod().getRole().getRank()
                    || user.getMyCredential().getGoverningAuthPeriod().getRole() == RoleType.Developer){
                rtlAuthorized.add(rt);
            }
        }
        return rtlAuthorized;
    }
    
    /**
     * Utility method for creating a loaded up object for logging the authorization
     * process results to the DB
     * @param ua
     * @param cat
     * @return 
     */
    public UserMuniAuthPeriodLogEntry auth_assembleUserMuniAuthPeriodLogEntrySkeleton(
                                        UserAuthorized ua, 
                                        UserMuniAuthPeriodLogEntryCatEnum cat){
        
        UserMuniAuthPeriodLogEntry skel = new UserMuniAuthPeriodLogEntry();
        skel.setCategory(cat.toString());
        // this is being set here in the skeleton factory and should stay here
        // the redundant injection in the coordinator should be a check instead
        skel.setUserMuniAuthPeriodID(ua.getMyCredential().getGoverningAuthPeriod().getUserMuniAuthPeriodID());
        
        return skel;
    }
    
    /**
     * Utility method for logging the use of a credential to access a restricted
     * object of many kinds
     * 
     * @param entry
     * @param umap
     * @throws IntegrationException
     * @throws AuthorizationException 
     */
    public void auth_logCredentialInvocation(UserMuniAuthPeriodLogEntry entry, UserMuniAuthPeriod umap) throws IntegrationException, AuthorizationException{
        UserIntegrator ui = getUserIntegrator();
        if(umap != null && umap.getUserMuniAuthPeriodID() != 0){
            entry.setUserMuniAuthPeriodID(umap.getUserMuniAuthPeriodID());
            ui.insertUserMuniAuthPeriodLogEntry(entry);
        } else {
            throw new AuthorizationException("Credentials must be logged with a valid periodid");
        }
    }
    
    /**
     * *** SECURITY CRITICAL METHOD ***
     * <br>
     * Creates a base AuthorizationPeriod which the admin user configures 
     * for injection into the DB, allowing user to access that Muni's functions
     * 
     * @param requestor
     * @param userCandidate
     * @param m
     * @return
     * @throws AuthorizationException 
     */
    public UserMuniAuthPeriod auth_initializeUserMuniAuthPeriod_SECURITYCRITICAL( UserAuthorized requestor, 
                                                            UserAuthorized userCandidate, 
                                                            Municipality m) throws AuthorizationException{
        UserMuniAuthPeriod umap = null;
        // check that the requestor has at least SysAdmin or better in the requested Muni
        if(requestor != null 
                && 
            requestor.getMuniAuthPeriodsMap().get(m) != null
                &&
            requestor.getMuniAuthPeriodsMap().get(m).get(0).getRole().getRank() >= RoleType.SysAdmin.getRank()){
            umap = new UserMuniAuthPeriod(m);
            umap.setUserID(userCandidate.getUserID());
            umap.setStartDate(LocalDateTime.now());
            umap.setStopDate(LocalDateTime.now().plusYears(DEFAULT_USERMUNIAUTHPERIODLENGTHYEARS));
            umap.setCreatedByUserID(requestor.getCreatedByUserId());
            umap.setAssignmentRelativeOrder(DEFAULT_ASSIGNMENT_RANK);
            umap.setNotes("");
        } else {
            throw new AuthorizationException("Requesting user is not authorized to add auth periods in this muni");
        }
        return umap;
    }
    
    /**
     * *** SECURITY CRITICAL METHOD ***
     * 
     * Insertion point and logic intermediary for UMAPS
     * 
     * @param requestingUser
     * @param usee
     * @param uap
     * @throws AuthorizationException
     * @throws IntegrationException 
     */
    public void auth_insertUserMuniAuthorizationPeriod_SECURITYCRITICAL(User requestingUser, User usee, UserMuniAuthPeriod uap) throws AuthorizationException, IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        if(uap != null && requestingUser != null && usee != null && uap.getMuni() != null){
            if(uap.getStartDate() != null && uap.getStartDate().isBefore(uap.getStopDate())){
                if(uap.getStopDate() != null && uap.getStopDate().isAfter(LocalDateTime.now())){
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
     * *** SECURITY CRITICAL METHOD ***
     * 
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
    private Credential auth_generateCredential_SECURITYCRITICAL(UserMuniAuthPeriod uap){
        Credential cred = null;
        
        switch(uap.getRole()){
            case Developer: 
                cred = new Credential(  uap,
                                                true,   //developer
                                                true,   // sysadmin
                                                true,   // cogstaff
                                                true,   // enfOfficial
                                                true,   // muniStaff
                                                true);  // muniReader
               break;
            
            case SysAdmin:
                cred = new Credential(  uap,
                                                false,   //developer
                                                true,   // sysadmin
                                                true,   // cogstaff
                                                true,   // enfOfficial
                                                true,   // muniStaff
                                                true);  // muniReader
               break;               
               
            case CogStaff:
                cred = new Credential(  uap,
                                                false,   //developer
                                                false,   // sysadmin
                                                true,   // cogstaff
                                                false,   // enfOfficial
                                                true,   // muniStaff
                                                true);  // muniReader
               break;               
               
            case EnforcementOfficial:
                cred = new Credential(  uap,
                                                false,   //developer
                                                false,   // sysadmin
                                                false,   // cogstaff
                                                true,   // enfOfficial
                                                true,   // muniStaff
                                                true);  // muniReader
               break;
               
            case MuniStaff:
                cred = new Credential(  uap,
                                                false,   //developer
                                                false,   // sysadmin
                                                false,   // cogstaff
                                                false,   // enfOfficial
                                                true,   // muniStaff
                                                true);  // muniReader
               break;
               
            case MuniReader:
                cred = new Credential(  uap,
                                                false,   //developer
                                                false,   // sysadmin
                                                false,   // cogstaff
                                                false,   // enfOfficial
                                                false,   // muniStaff
                                                true);  // muniReader
               break;  
            case Public: 
                cred = new Credential(  uap,
                                                false,   //developer
                                                false,   // sysadmin
                                                false,   // cogstaff
                                                false,   // enfOfficial
                                                false,   // muniStaff
                                                false);  // muniReader
                break;
            default:
                cred = null;
        }        
        return cred;
    }    
    
   
    
    /**
     * Insertion point for new User objects. NOTE that this is NOT a 
     * security critical method since a User by itself cannot even login
     * @param usr
     * @return
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.AuthorizationException 
     */
    public int user_insertNewUser(User usr) throws IntegrationException, AuthorizationException{
        int newUserID = 0;
        UserIntegrator ui = getUserIntegrator();
        if(usr != null && usr.getUsername() != null){
            if(user_checkUsernameAllowedForInsert(usr.getUsername())){
                newUserID = ui.insertUser(usr);
            } else {
                throw new AuthorizationException("Non-unique username!");
            }
        } else {
            throw new AuthorizationException("Cannot create new user from Null or without username");
            
        }
        return newUserID;
    }
    
    /**
     * Utility method for querying for an existing username
     * @param uname
     * @return true if the username is unique and user insert can proceed; false 
     * if name is not unique--abort insert
     */
    public boolean user_checkUsernameAllowedForInsert(String uname){
        UserIntegrator ui = getUserIntegrator();
        if(uname == null){
            return false;
        }
        boolean allowed = false;
        try {
            int checkID = ui.getUserID(uname);
            if (checkID == 0) {
                System.out.println("UserCoordinator.user_checkUsernameAllowedForInsert: no user found with name " + uname);
                allowed = true; 
            } else {
                System.out.println("UserCoordinator.user_checkUsernameAllowedForInsert: DUPLICATE NAME: " + uname);
                
            }
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
                
        return allowed;
        
    }
    
    /**
     * Supplies clients with a UserAuthorized carrying a valid credential
     * backed by a valid UserAuthorizationPeriod required for carrying out many
     * Coordinator tasks
     * @return A UserAuthorized with the lowest possible rank
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public UserAuthorized auth_getPublicUserAuthorized() throws IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        UserAuthorized ua = null;
        int publicUserID = Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                    .getString("publicuserid"));

        User u = user_getUser(publicUserID);
       
        if(u != null){
            
             ua = auth_prepareUserForSessionChoice(u);
        }
//        UserMuniAuthPeriod umap = ui.getUserMuniAuthPeriod(publicUserUMAPID);
        
        try {
            if(ua != null && ua.getMuniAuthPeriodsMap() != null && !ua.getMuniAuthPeriodsMap().isEmpty()){
                Map<Municipality, List<UserMuniAuthPeriod>> umapMapTemp = ua.getMuniAuthPeriodsMap();
                // be design, we have only one muni, COGland for this user
                List<Municipality> muniList  =new ArrayList<>();
                muniList.addAll(umapMapTemp.keySet());
                if(!muniList.isEmpty() && umapMapTemp.containsKey(muniList.get(0))){
                    List<UserMuniAuthPeriod> umapList = umapMapTemp.get(muniList.get(0));
                    if(umapList != null && !umapList.isEmpty()){
                        ua = auth_authorizeUser_SECURITYCRITICAL(ua, umapList.get(0));
                    }
                }
            }
        } catch (AuthorizationException ex) {
            System.out.println(ex);
        }
        
        return ua;
        
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
    public User user_getUserRobot() throws IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        User u;
        u = ui.getUser(Integer.parseInt(
                getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                        .getString("cogRobotUserID")));
        return u;
    }
    
    /**
     * Utility method for attaching notes
     * @param u
     * @param mbp
     * @throws IntegrationException 
     */
    public void user_appendNoteToUser(User u, MessageBuilderParams mbp) throws IntegrationException{
        SystemCoordinator sc = getSystemCoordinator();
        UserIntegrator ui = getUserIntegrator();
        u.setNotes(sc.appendNoteBlock(mbp));
        ui.updateUser(u);
        
    }
    
    /**
     * Users are backed by a Person object which holds contact info. This method
     * creates a note documenting changes in these DB mappings
     * 
     * @param sb
     * @param currPers
     * @param updatedPers
     * @return 
     */
    private StringBuilder user_assemblePersonLinkUpdateNote(StringBuilder sb, Person currPers, Person updatedPers){
        
            sb.append("Updating person link from ");
            sb.append(currPers.getFirstName());
            sb.append(" ");
            sb.append(currPers.getLastName());
            sb.append("(");
            sb.append(currPers.getPersonID());
            sb.append(")");
            
            sb.append(" to ");
            
            sb.append(updatedPers.getFirstName());
            sb.append(" ");
            sb.append(updatedPers.getLastName());
            sb.append("(");
            sb.append(updatedPers.getPersonID());
            sb.append(")");
            
            return sb;
    }
    
   
    
    /**
     * Logic container for checking permissibility of User record deactivation.
     * Key role is to prohibit any UI-based deactivation of developer users :)
     * @param ua requesting user
     * @param ufcToDeac 
     * @throws com.tcvcog.tcvce.domain.AuthorizationException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void user_deactivateUser(UserAuthorized ua, UserAuthorizedForConfig ufcToDeac) throws AuthorizationException, IntegrationException{
        System.out.println("UserCoordinator.user_deactivateUser");
        UserIntegrator ui = getUserIntegrator();
        if(ufcToDeac != null && ua != null){
            if(ua.getUserID() == ufcToDeac.getUserID()){
                throw new AuthorizationException("Users cannot deactivate themselves!");
            }
            // users can only deactivate users whose highest UMAP rank is less than theirs, or have dev rank
            if(auditUMAPList_determineHighestRank(ufcToDeac.getUmapList()).getRank() < ua.getKeyCard().getGoverningAuthPeriod().getRole().getRank()
                    || ua.getKeyCard().getGoverningAuthPeriod().getRole().getRank() == RoleType.Developer.getRank()){
                ufcToDeac.setDeactivatedBy(ua.getUserID());
                ufcToDeac.setDeactivatedTS(LocalDateTime.now());
                ui.updateUser(ufcToDeac);
            } else {
                throw new AuthorizationException("Users with a UMAP of rank: developer cannot be deactivated from the UI");
            }
        } else {
            throw new AuthorizationException("Cannot deactivate a null user!");
        }
    }
    
    /**
     * Mini logic container to check a list of UMAPs for dev credentials
     * @param umapList
     * @return true if no dev ranked UMAPs were found
     */
    private RoleType auditUMAPList_determineHighestRank(List<UserMuniAuthPeriod> umapList){
         
        RoleType highestRole = RoleType.Public;
        if(umapList != null && !umapList.isEmpty()){
            
            for(UserMuniAuthPeriod umap: umapList){
                        if(umap.getRole().getRank() > highestRole.getRank()){
                            highestRole = umap.getRole();
                        }
                    }
        }
        
        return highestRole;
    }
    /**
     * Mini logic container to check a list of UMAPs for dev credentials
     * @param umapList
     * @return true if no dev ranked UMAPs were found
     */
    private boolean auditUMAPList_allowDeactivation_freeOfDevCreds(List<UserMuniAuthPeriod> umapList){
         
        boolean allowDeactivation = true;
        if(umapList != null && !umapList.isEmpty()){
            
            for(UserMuniAuthPeriod umap: umapList){
                        if(umap.getRole() == RoleType.Developer){
                            allowDeactivation = false;
                        }
                    }
        }
        
        return allowDeactivation;
    }
    
    
    /**
     * Updates the User's Person and username only. 
     * Note -- does not update the password which is updated separately
     * @param u
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.AuthorizationException 
     */
    public void user_updateUser(User u) throws IntegrationException, AuthorizationException{
        UserIntegrator ui = getUserIntegrator();
        StringBuilder sb = new StringBuilder();
        if(u != null && (u.getPerson() != null || u.getPersonID() != 0)){
                ui.updateUser(u);
        }
    }
    
    /**
     * Logic intermediary for forcing password reset on next login
     * // As of BETA 2020 this doesn't get "forced" yet
     * 
     * @param u
     * @throws IntegrationException 
     */
    public void user_forcePasswordReset(User u) throws IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        ui.forcePasswordReset(u);
        
    }
    
    
    public void user_updateUserPersonLink(User u, Person freshPerson) throws IntegrationException, AuthorizationException{
        
        UserIntegrator ui = getUserIntegrator();
        if(u != null && u.getPersonID() != 0){
    
            // TODO: complete note on user udpates
            MessageBuilderParams mb = new MessageBuilderParams();
            mb.setExistingContent(u.getNotes());
    //        mb.setNewMessageContent(sb.toString());
    //        user_appendNoteToUser(u, mb);

            ui.updateUser(u);
            
        } else {
            throw new AuthorizationException("User-person links must be to real Person objects");
        }
    }
    
    
    /**
     * *** SECURITY CRITICAL METHOD ***
     * @param u
     * @param pw
     * @throws IntegrationException
     * @throws AuthorizationException 
     */
    public void user_updateUserPassword_SECURITYCRITICAL(User u, String pw) throws IntegrationException, AuthorizationException{
        UserIntegrator ui = getUserIntegrator();
        if(pw.length() >= MIN_PSSWD_LENGTH){
            ui.setUserPassword_SECURITYCRITICAL(u, pw);
        } else {
            throw new AuthorizationException("Password must be at least " + MIN_PSSWD_LENGTH + " characters");
        }
    }
    
    /**
     * Factory method for User objects
     * 
     * @param u the requesting user
     * @return 
     */
    public User user_getUserSkeleton(User u){
        User skel = new User();
        if(u != null){
            skel.setHomeMuniID(u.getHomeMuniID());
            skel.setCreatedByUserId(u.getUserID());
        }
        return skel;
    }
   
    
    

     /**
      * Adds a timestamp to the period's invalidation. Normally, a period is not
      * directly invalidated but rather expires and is updated with a new one
      * @param aup
      * @param requestingUser
      * @param note
      * @throws IntegrationException
      * @throws AuthorizationException 
      */
    public void auth_invalidateUserAuthPeriod(UserMuniAuthPeriod aup, UserAuthorized requestingUser, String note) throws IntegrationException, AuthorizationException{
        SystemCoordinator sc = getSystemCoordinator();
        UserIntegrator ui = getUserIntegrator();
        if(aup.getUserMuniAuthPeriodID() == requestingUser.getMyCredential().getGoverningAuthPeriod().getUserMuniAuthPeriodID()){
            throw new AuthorizationException("You are unauthorized to invalidate your current authorization period");
        }
        aup.setNotes(sc.appendNoteBlock(new MessageBuilderParams(aup.getNotes(), "INVALIDATION OF AUTH PERIOD", "", note, requestingUser, requestingUser.getMyCredential())));
        ui.invalidateUserAuthRecord(aup);
        
        // We should be logging this action
        
    }
    
    
    /**
     * This is NOT a UserAuthorized so we're just passing out objects here
     * @param userID
     * @return
     * @throws IntegrationException 
     */
    public User user_getUser(int userID) throws IntegrationException{
        if(userID == 0){
            return null;
        }
        UserIntegrator ui = getUserIntegrator();
        return ui.getUser(userID);
    }
    
    
    /**
     * Generates a User list that represents allowable users to engage with
     * for a given muni. This method doesn't return fully-fledged users
     * @param userRequestor
     * @return
     * @throws IntegrationException
     * @throws AuthorizationException 
     */
    public List<User> user_auth_assembleUserListForConfig(UserAuthorized userRequestor) throws IntegrationException, AuthorizationException{
        
        UserIntegrator ui = getUserIntegrator();
        List<User> usersForConfig = null;
        
        if(userRequestor != null){
            usersForConfig = new ArrayList<>();
            // build a list of Users who have a valid auth period in any Municipality in which
            // the passed in adminUser has SysAdmin RoleType
            for(Municipality mu: userRequestor.getAuthMuniList()){
                // if the admin's own auth map for the iterated municipality includes a record
                // with SysAdmin or higher (which it should, since they're on the userConfig.xhtml page
                if( userRequestor.getMuniAuthPeriodsMap().get(mu) != null 
                        &&
                    !userRequestor.getMuniAuthPeriodsMap().get(mu).isEmpty()
                        &&
                    (userRequestor.getMuniAuthPeriodsMap().get(mu).get(0).getRole().getRank() >= RoleType.SysAdmin.getRank())
                ){
                    usersForConfig = user_auth_assembleUserListForConfig(mu,userRequestor);
                } 
            } //close loop over authmunis 
            
            // add any users who don't have any auth periods in that muni but whose home muni is the user's auth muni
            List<User> usersInHomeMuni = ui.getUsersByHomeMuni(userRequestor.getKeyCard().getGoverningAuthPeriod().getMuni());
            List<User> usersToAdd = new ArrayList<>();
            for(User usr: usersInHomeMuni){
                if(!usersForConfig.contains(usr)){
                    usersToAdd.add(usr);
                }
            }
            if(!usersToAdd.isEmpty()){
                usersForConfig.addAll(usersToAdd);
            }
            
        } // close param not null check
        return usersForConfig;
    }
    
    
    
    /**
     * Assembled Users are those who have had any auth period, 
     * currently valid OR NOT in the passed in municipality.
     * This is basically an adapter to convert the raw user IDs that
     * come from the integrator's list of UMAPs into fully-baked 
     * User objects. As of OCT 2019 at this method's birth, 
     * no additional logic is implemented other than the User existing.
     * 
     * @param m
     * @return 
     */
    private List<User> user_auth_assembleUserListForConfig(Municipality m, UserAuthorized uq) throws AuthorizationException, IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        
        List<UserMuniAuthPeriod> umapList;
        List<Integer> userIDList = null;
        List<User> userList = null;
        
        if(m != null){
            userList = new ArrayList<>();
            
            umapList = ui.getUserMuniAuthPeriodsRaw(m);
            
            if(umapList != null && !umapList.isEmpty()){
                userIDList = new ArrayList<>();
               
                // Consider adding a step here to remove invalid periods,
                // meaning we can restrict to only seeing "active users"
                // in your municipality to say those Users ranked Dev or better
//                if(uq.getRole().getRank() == RoleType.SysAdmin.getRank()){
//                    umapList = cleanUserMuniAuthPeriodList(umapList);
//                }
                for(UserMuniAuthPeriod umap: umapList){
                    if(!userIDList.contains(umap.getUserID())){
                        userIDList.add(umap.getUserID());
                    }
                }
            } // close build list of user IDs to fetch for passed in muni
            
            // as long as we have a userID for fetching
            if(userIDList != null && !userIDList.isEmpty()){
                for(Integer i: userIDList){
                    userList.add(ui.getUser(i));
                }
            } // we have a list of Users to return!
        }
        return userList;


    }
    
    /**
     * Extracts all user records for Developer config
     * @param ua
     * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public List<User> user_assembleUserListComplete(UserAuthorized ua) throws IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        List<User> ul = new ArrayList<>();
        if(ua.getKeyCard().isHasDeveloperPermissions()){
            List<Integer> idl = ui.getUserListComplete();
            if(idl != null && !idl.isEmpty()){
                for(Integer i: idl){
                    ul.add(user_getUser(i));
                }
            }
        }
        return ul;
    }
    
    
    /**
     * Logic bundle for building a UserAuthorized without a credential for
     * configuration purposes; Credential objects are only inserted 
     * when that UserAuthorized is backing a system session
     * @param userToConfigure
     * @return
     * @throws AuthorizationException
     * @throws IntegrationException 
     */
    public UserAuthorizedForConfig user_transformUserToUserAuthorizedForConfig(User userToConfigure) throws AuthorizationException, IntegrationException{
        UserAuthorizedForConfig uafc = null;
        UserIntegrator ui = getUserIntegrator();
        if(userToConfigure != null){
            User utemp = user_getUser(userToConfigure.getUserID());
            uafc = new UserAuthorizedForConfig(ui.getUserAuthorizedNoAuthPeriods(utemp));
            uafc.setMuniAuthPeriodsMap(auth_assembleMuniUMAPMapRaw(userToConfigure));
            uafc.setUmapList(ui.getUserMuniAuthPeriodsRaw(userToConfigure.getUserID()));
            System.out.println("UserCoordinator.user_transformUserToUserAuthorizedForConfig: Transformed username " + userToConfigure.getUsername());
        }
        return uafc;
    }
    


    /**
     * Convenience method for upcasting UserAuthorized to simple User and putting
     * them in a list
     * @param uaList
     * @return 
     */
    public List<User> user_extractUsersFromUserAuthorized(List<UserAuthorized> uaList){
        List<User> uList = null;
        if(uaList != null && !uaList.isEmpty()){
            uList = new ArrayList<>();
            for(UserAuthorized ua: uaList){
                uList.add( (User) ua);
            }
        }
        return uList;
    }
    
    
   
    
    /**
     * Return a list of Municipalities that User u does not currently have the
     * authorization to access.
     * @param u the user in question
     * @return A list of Municipality objects that the user does not have 
     *     access to  
     * @throws IntegrationException 
     */
    public List<Municipality> auth_getUnauthorizedMunis(User u) throws IntegrationException {
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
