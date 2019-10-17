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
import com.tcvcog.tcvce.entities.Credential;
import java.io.Serializable;
import com.tcvcog.tcvce.entities.Credential;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.RoleType;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserMuniAuthPeriod;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.UserConfigReady;
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
     * @param muni if the desire is the Authorize the user in a particular muni
     * when null, this method will request a credential for the muni containing
     * the highest-ranked assignment ranking of its list of authorized period
     * assignment rankings
     * @return the fully baked cog user
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.AuthorizationException occurs if the user
     * has been retrieved from the database but their access has been toggled off
     */
    public UserAuthorized authorizeUser(User usr, Municipality muni) throws AuthorizationException, IntegrationException{
        UserIntegrator ui = getUserIntegrator();    
        UserAuthorized usrAuth = null;
        List<UserMuniAuthPeriod> safeList = validateAndCleanUserMuniAuthPeriodList(ui.getUserMuniAuthPeriodsRaw(usr));
        
        if(!safeList.isEmpty()){
            usrAuth = ui.getUserAuthorizedSkel(usr);
            usrAuth = configureUserAuthorized(usrAuth, safeList, muni);
        } else {
            return usrAuth;
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
    private UserAuthorized configureUserAuthorized( UserAuthorized ua, 
                                                    List<UserMuniAuthPeriod> umapList, 
                                                    Municipality requestedMuni)
                                            throws  IntegrationException, 
                                                    AuthorizationException{
        
        Map<Municipality, List<UserMuniAuthPeriod>> muniPeriodMap = null;
        
        List<UserMuniAuthPeriod> periodList = null;
        Municipality mu = null;
        int maxRelOrder = Integer.MIN_VALUE; 
        Municipality credMuni = null;
        Collections.sort(umapList);
        
        if(!umapList.isEmpty()){
            muniPeriodMap = new HashMap<>();
            for(UserMuniAuthPeriod umap: umapList){
                mu = umap.getMuni();
                if(muniPeriodMap.containsKey(mu)){
                    periodList = muniPeriodMap.get(mu); // pull out our authorized peridos
                    periodList.add(umap); // add our new one
                    Collections.sort(periodList); // sort based first on role rank, then assignment order ACROSS munis
                    muniPeriodMap.put(umap.getMuni(), periodList); // and overwrite the previous val (i.e. keep the same reference)
                } else {
                    // no existing record for that muni, so make a list, inject, and put
                    periodList = new ArrayList<>();
                    periodList.add(umap);
                    muniPeriodMap.put(umap.getMuni(), periodList);
                }
                // User cannot switch to a muni for which they have no authorized periods
                if(requestedMuni != null && muniPeriodMap.containsKey(requestedMuni)){
                    credMuni = requestedMuni;
                } else if(periodList.get(0).getAssignmentRelativeOrder() > maxRelOrder){
                    maxRelOrder = periodList.get(0).getAssignmentRelativeOrder();
                    credMuni = mu;
                }
                
                // ************************************************************
                // ******* GENERATE AND INJECT CREDENTIAL FOR CHOSEN MUNI *****
                // ************************************************************
                ua.setMyCredential(generateCredential(muniPeriodMap.get(credMuni).get(0)));
                
            } // close for over period candidates
            
            // finally, inject the muniPeriodMap into the UA whose credential is set
            ua.setMuniAuthPeriodsMap(muniPeriodMap);
           
        } else {
            throw new AuthorizationException("No candidate authorization periods exist for user");
        }
        return ua;
    }
    
    
    private List<UserMuniAuthPeriod> validateUserMuniAuthPeriodList(List<UserMuniAuthPeriod> umapList){
        List<UserMuniAuthPeriod> tempList = null;
        if(umapList != null && !umapList.isEmpty()){
                tempList = new ArrayList<>();
            
            for(UserMuniAuthPeriod umap: umapList){
                tempList.add(validateUserMuniAuthPeriod(umap));
            }
            return tempList;
        }
        return tempList;
    }
    
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
     * and only returning valid periods
     * @param rawUMAPList
     * @return the list of only valid UMAPs
     */
    private List<UserMuniAuthPeriod> validateAndCleanUserMuniAuthPeriodList(List<UserMuniAuthPeriod> rawUMAPList){
        List<UserMuniAuthPeriod> cleanList = null; 
        if(rawUMAPList != null && !rawUMAPList.isEmpty()){
            cleanList = new ArrayList<>();
            for(UserMuniAuthPeriod umap: rawUMAPList){
                if(validateUserMuniAuthPeriod(umap).getValidatedTS() != null){
                    cleanList.add(umap);
                }
            }
            if(!cleanList.isEmpty()){
                Collections.sort(cleanList);
            }
        }
        return cleanList;
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
    
    public void insertNewUserAuthorizationPeriod(User requestingUser, User usee, UserMuniAuthPeriod uap) throws AuthorizationException, IntegrationException{
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
    
    public UserAuthorized getUserSkeleton(User u){
        UserAuthorized skel = new UserAuthorized(u);
        return skel;
    }
   
    
     public UserMuniAuthPeriod initializeNewAuthPeriod( UserAuthorized requestor, 
                                                            User requestee, 
                                                            Municipality m){
        UserMuniAuthPeriod per = null;

        // Only Users who have sys admin permission in the requested muni or are devs
        if((requestor.getMyCredential().getGoverningAuthPeriod().getMuni().getMuniCode() == m.getMuniCode()
                && requestor.getMyCredential().isHasSysAdminPermissions())
                || requestor.getMyCredential().isHasDeveloperPermissions()){
            per = new UserMuniAuthPeriod(m);
            per.setStartDate(LocalDateTime.now());
            per.setStopDate(LocalDateTime.now().plusYears(1));
        }
        return per;
    }
     

     
    public void invalidateUserAuthPeriod(UserMuniAuthPeriod aup, UserAuthorized u, String note) throws IntegrationException, AuthorizationException{
        SystemCoordinator sc = getSystemCoordinator();
        UserIntegrator ui = getUserIntegrator();
        if(aup.getUserMuniAuthPeriodID() == u.getMyCredential().getGoverningAuthPeriod().getUserMuniAuthPeriodID()){
            throw new AuthorizationException("You are unauthorized to invalidate your current authorization period");
        }
        aup.setNotes(sc.appendNoteBlock(new MessageBuilderParams(aup.getNotes(), "INVALIDATION OF AUTH PERIOD", "", note, u)));
        ui.invalidateUserAuthRecord(aup);
        
        // We should be logging this action
        
    }
    
    public User getUser(int userID) throws IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        return ui.getUser(userID);
    }
    
    
    /**
     * Generates a User list that represents allowable users to engage with
     * for a given muni. This method doesn't return fully-fledged users
     * @param mu
     * @param adminUser
     * @return
     * @throws IntegrationException
     * @throws AuthorizationException 
     */
    public List<UserAuthorized> getUserAuthorizedListForConfig(Municipality mu, UserAuthorized adminUser) throws IntegrationException, AuthorizationException{
        if(adminUser.getRole().getRank() < RoleType.MuniStaff.getRank()){
            throw new AuthorizationException("Must rank sysadmin or higher to administrate users");
        }
        UserIntegrator ui = getUserIntegrator();
        List<UserMuniAuthPeriod> umapList = null;
        List<UserAuthorized> ual = null;
        if(mu != null){
            umapList = ui.getUserMuniAuthPeriodsRaw(mu);
            if(!umapList.isEmpty()){
                ual = new ArrayList<>();
                for(UserMuniAuthPeriod umap: umapList){
                    // note that authorizeUser will got get MuniAuthPeriods by
                    // user and only return a UseAuthorized if at least one is valid
                    UserAuthorized ua = authorizeUser(ui.getUser(umap.getUserID()), mu);
                    if(ua == null && adminUser.getRole().getRank() <  RoleType.Developer.getRank()){
                        break;
                    } else { 
                        ual.add(ua);
                    }
                }
            }
        }
        return ual;
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
