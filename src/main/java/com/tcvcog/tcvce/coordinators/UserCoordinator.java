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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cedba
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
    public int getUserID(String userName) throws IntegrationException{
         UserIntegrator ui = getUserIntegrator();
         return ui.getUserID(userName);
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
     * and grabbing their list of authorized periods
     * 
     * @param umapReq
     * @param umapListValidOnly
     * @return the fully baked cog user
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.AuthorizationException occurs if the user
     * has been retrieved from the database but their access has been toggled off
     */
    public UserAuthorized authorizeUser(UserMuniAuthPeriod umapReq, List<UserMuniAuthPeriod> umapListValidOnly) throws AuthorizationException, IntegrationException{
        UserIntegrator ui = getUserIntegrator();    
        UserAuthorized usrAuth = null;
        Map<Municipality, List<UserMuniAuthPeriod>> umapMasterMap = new HashMap<>();
        Municipality mu;        
        
        System.out.println("UserCoordinator.authorizeUser()");
        
//        User usr = getUser(umapReq.getUserID());
//        List<UserMuniAuthPeriod> safeList = cleanUserMuniAuthPeriodList(ui.getUserMuniAuthPeriodsRaw(umapReq.getUserID()));
        List<UserMuniAuthPeriod> safeList = umapListValidOnly;
        
        if(safeList != null && !safeList.isEmpty()){
            Collections.sort(safeList);
            
            Map<Municipality, List<UserMuniAuthPeriod>> tempMap = new HashMap<>();
            usrAuth = ui.getUserAuthorizedNoAuthPeriods(getUser(umapReq.getUserID()));
            List<UserMuniAuthPeriod> tempUMAPList;
            
            for(UserMuniAuthPeriod umap: safeList){
                mu = umap.getMuni();
                if(umapMasterMap.containsKey(mu)){
                    tempUMAPList = umapMasterMap.get(mu); // pull out our authorized peridos
                    tempUMAPList.add(umap); // add our new one
                    Collections.sort(tempUMAPList); // sort based first on role rank, then assignment order ACROSS munis
                    tempMap.put(umap.getMuni(), tempUMAPList); // and overwrite the previous val (i.e. keep the same reference)
                } else {
                    // no existing record for that muni, so make a list, inject, and put
                    tempUMAPList = new ArrayList<>();
                    tempUMAPList.add(umap);
                    tempMap.put(umap.getMuni(), safeList);
                }
            } // close for over periods

            // ************************************************************
            // ******* GENERATE AND INJECT CREDENTIAL FOR CHOSEN MUNI *****
            // ************************************************************
            Credential cr = generateCredential(umapReq);
            usrAuth.setMyCredential(cr);

            // finally, inject the muniPeriodMap into the UA whose credential is set
            usrAuth.setMuniAuthPeriodsMap(tempMap);
        } 
        return usrAuth;
    }
    
    
    /**
     * Asks the integrtaor for all UMAPs for a given u, cleans that list, 
     * and returns the sorted Collection
     * @param username
     * @return sorted UMAP list which can then be passed to authorizeUser.
     */
    public List<UserMuniAuthPeriod> assembleValidAuthPeriods(String username){
        UserIntegrator ui = getUserIntegrator();
        List<UserMuniAuthPeriod> umapList = null;
        
        try {
            umapList = ui.getUserMuniAuthPeriodsRaw(ui.getUserID(username));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            
        }
        
        cleanUserMuniAuthPeriodList(umapList);
        
        return umapList;
    }
    
    
  
    
    /**
     * Creates a list of Users for use by search criterias on various pages
     * so the user can search by Users who have some past connection to any of the 
     * given Municipality objects passed into the method.
     * 
     * TODO: Customize list based on muni connections
     * NOTED in github issues by ECD 25-may-2020
     * 
     * @return An assembled list of users for authorization
     */
    public List<User> assembleUserListForSearch(User usr){
        // we do nothing with muniList
        UserIntegrator ui = getUserIntegrator();
        
        List<User> ulst = new ArrayList<>();
        
        try {
            for(Integer i: ui.getSystemUserIDList()){
                ulst.add(getUser(i));
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
    private UserMuniAuthPeriod validateUserMuniAuthPeriod(UserMuniAuthPeriod uap){
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
     * Convenience adaptor method for checking the validity of a generic list of raw UMAPs
     * and only returning valid periods. This method also calls Collections.sort on its inputted umap list
     * so that the highest ranked valid period is first
     * 
     * @param rawUMAPList
     * @return the list of only valid UMAPs
     */
    private List<UserMuniAuthPeriod> cleanUserMuniAuthPeriodList(List<UserMuniAuthPeriod> rawUMAPList){
        List<UserMuniAuthPeriod> cleanList = null; 
        //make sure we have a valid list
        if(rawUMAPList != null && !rawUMAPList.isEmpty()){
            cleanList = new ArrayList<>();
            for(UserMuniAuthPeriod umap: rawUMAPList){
                if(validateUserMuniAuthPeriod(umap).getValidatedTS() != null){
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
    
    
    public boolean verifyReInitSessionRequest(UserAuthorized ua, UserMuniAuthPeriod umap){
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
    public List<RoleType> getPermittedRoleTypesToGrant(UserAuthorized user){
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
    
    public UserMuniAuthPeriodLogEntry assembleUserMuniAuthPeriodLogEntrySkeleton(
                                        UserAuthorized ua, 
                                        UserMuniAuthPeriodLogEntryCatEnum cat){
        
        UserMuniAuthPeriodLogEntry skel = new UserMuniAuthPeriodLogEntry();
        skel.setCategory(cat.toString());
        // this is being set here in the skeleton factory and should stay here
        // the redundant injection in the coordinator should be a check instead
        skel.setUserMuniAuthPeriodID(ua.getMyCredential().getGoverningAuthPeriod().getUserMuniAuthPeriodID());
        
        return skel;
    }
    
    public void logCredentialInvocation(UserMuniAuthPeriodLogEntry entry, UserMuniAuthPeriod umap) throws IntegrationException, AuthorizationException{
        UserIntegrator ui = getUserIntegrator();
        if(umap != null && umap.getUserMuniAuthPeriodID() != 0){
            entry.setUserMuniAuthPeriodID(umap.getUserMuniAuthPeriodID());
            ui.insertUserMuniAuthPeriodLogEntry(entry);
        } else {
            throw new AuthorizationException("Credentials must be logged with a valid periodid");
        }
    }
    
    public UserMuniAuthPeriod initializeUserMuniAuthPeriod( UserAuthorized requestor, 
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
    
    public void insertUserMuniAuthorizationPeriod(User requestingUser, User usee, UserMuniAuthPeriod uap) throws AuthorizationException, IntegrationException{
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
    private Credential generateCredential(UserMuniAuthPeriod uap){
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
     * Supplies clients with a UserAuthorized carrying a valid credential
     * backed by a valid UserAuthorizationPeriod required for carrying out many
     * Coordinator tasks
     * @return A UserAuthorized with the lowest possible rank
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public UserAuthorized getPublicUserAuthorized() throws IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        UserAuthorized ua = null;
        int publicUserID = Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                    .getString("publicuserid"));
        int publicUserUMAPID = Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                    .getString("publicuserumap"));
        User u = getUser(publicUserID);
        List<UserMuniAuthPeriod> umapList = null;
        if(u != null){
            
             umapList = assembleValidAuthPeriods(u.getUsername());
        }
//        UserMuniAuthPeriod umap = ui.getUserMuniAuthPeriod(publicUserUMAPID);
        
        try {
            if(umapList != null && !umapList.isEmpty()){
                UserMuniAuthPeriod umap = umapList.get(0);
                umap.setUserID(publicUserID);
                umapList.clear();
                umapList.add(umap);
                ua   = authorizeUser(umap, umapList);
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
    public User getUserRobot() throws IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        User u;
        u = ui.getUser(Integer.parseInt(
                getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                        .getString("cogRobotUserID")));
        return u;
    }
    
    public void appendNoteToUser(User u, MessageBuilderParams mbp) throws IntegrationException{
        SystemCoordinator sc = getSystemCoordinator();
        UserIntegrator ui = getUserIntegrator();
        u.setNotes(sc.appendNoteBlock(mbp));
        ui.updateUser(u);
        
    }
    
    private StringBuilder assemblePersonLinkUpdateNote(StringBuilder sb, Person currPers, Person updatedPers){
        
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
    
    private StringBuilder assembleUsernameUpdateNote(StringBuilder sb, Person currPers, String updatedUsername){
        
        
        return sb;
    }
    
    
    /**
     * Updates the User's Person and username only. 
     * Note -- does not update the password which is updated separately
     * @param u
     * @param persToUpdate
     * @param usernameToUpdate
     * @throws IntegrationException 
     */
    public void updateUser(User u, Person persToUpdate, String usernameToUpdate) throws IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        MessageBuilderParams mb = new MessageBuilderParams();
        StringBuilder sb = new StringBuilder();
        mb.setExistingContent(u.getNotes());
        if(persToUpdate != null){
            
            u.setPerson(persToUpdate);
            sb = assemblePersonLinkUpdateNote(sb, u.getPerson(), persToUpdate);
        }

        if(usernameToUpdate != null){
            sb = assembleUsernameUpdateNote(sb, u.getPerson(), usernameToUpdate );
            u.setUsername(usernameToUpdate);
        }
        
        ui.updateUser(u);
        
        mb.setNewMessageContent(sb.toString());
        appendNoteToUser(u, mb);
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
        skel.setCreatedByUserId(u.getUserID());
        return skel;
    }
   
    
    

     /**
      * Adds a timestamp to the period's invalidation. Normally, a period is not
      * directly invalidated but rather expires and is updated with a new one
      * @param aup
      * @param u
      * @param note
      * @throws IntegrationException
      * @throws AuthorizationException 
      */
    public void invalidateUserAuthPeriod(UserMuniAuthPeriod aup, UserAuthorized u, String note) throws IntegrationException, AuthorizationException{
        SystemCoordinator sc = getSystemCoordinator();
        UserIntegrator ui = getUserIntegrator();
        if(aup.getUserMuniAuthPeriodID() == u.getMyCredential().getGoverningAuthPeriod().getUserMuniAuthPeriodID()){
            throw new AuthorizationException("You are unauthorized to invalidate your current authorization period");
        }
        aup.setNotes(sc.appendNoteBlock(new MessageBuilderParams(aup.getNotes(), "INVALIDATION OF AUTH PERIOD", "", note, u, u.getMyCredential())));
        ui.invalidateUserAuthRecord(aup);
        
        // We should be logging this action
        
    }
    
    
    /**
     * This is NOT a UserAuthorized so we're just passing out objects here
     * @param userID
     * @return
     * @throws IntegrationException 
     */
    public User getUser(int userID) throws IntegrationException{
        if(userID == 0){
            return null;
        }
        UserIntegrator ui = getUserIntegrator();
        return ui.getUser(userID);
    }
    
    
    /**
     * Generates a User list that represents allowable users to engage with
     * for a given muni. This method doesn't return fully-fledged users
     * @param mu
     * @param userRequestor
     * @return
     * @throws IntegrationException
     * @throws AuthorizationException 
     */
    public List<User> assembleUserListForConfig(UserAuthorized userRequestor) throws IntegrationException, AuthorizationException{
        
        List<User> usersForConfig = null;
        
        if(userRequestor != null){
            usersForConfig = new ArrayList<>();
            // build a list of Users who have a valid auth period in any Municipality in which
            // the passed in adminUser has SysAdmin RoleTYpe
            for(Municipality mu: userRequestor.getAuthMuniList()){
                // if the admin's own auth map for the iterated municipality includes a record
                // with SysAdmin or higher (which it should, since they're on the userConfig.xhtml page
                if( userRequestor.getMuniAuthPeriodsMap().get(mu) != null 
                        &&
                    !userRequestor.getMuniAuthPeriodsMap().get(mu).isEmpty()
                        &&
                    (userRequestor.getMuniAuthPeriodsMap().get(mu).get(0).getRole().getRank() >= RoleType.SysAdmin.getRank())
                ){
                    usersForConfig = assembleUserListForConfig(mu,userRequestor);
                } 
            } //close loop over authmunis 
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
    private List<User> assembleUserListForConfig(Municipality m, UserAuthorized uq) throws AuthorizationException, IntegrationException{
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
    
    public List<UserAuthorized> assembleUserAuthorizedListForConfiguration(List<User> users){
        List<UserAuthorized> ual = null;
        if(users != null && !users.isEmpty()){
            ual = new ArrayList<>();
        }
        return ual;
    }
    
    public UserAuthorized transformUserToUserAuthorizedForConfig(UserAuthorized userRequestor, User uToAuth) throws AuthorizationException, IntegrationException{
//        return authorizeUser(uToAuth, null, null);

        return null;
    }
    


    /**
     * Convenience method for upcasting UserAuthorized to simple User and putting
     * them in a list
     * @param uaList
     * @return 
     */
    public List<User> extractUsersFromUserAuthorized(List<UserAuthorized> uaList){
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
