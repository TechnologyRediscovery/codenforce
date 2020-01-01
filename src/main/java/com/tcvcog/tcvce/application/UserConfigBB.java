/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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

import com.tcvcog.tcvce.coordinators.MunicipalityCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.Credential;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserMuniAuthPeriod;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.UserConfigReady;
import com.tcvcog.tcvce.entities.search.QueryPerson;
import com.tcvcog.tcvce.entities.search.QueryPersonEnum;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author sylvia
 */
public class UserConfigBB extends BackingBeanUtils{
    
    

    private UserAuthorized userAuthorizedInConfig;
    private String freshPasswordCleartext;
    
    private UserMuniAuthPeriod umapInConfig;
    private List<UserMuniAuthPeriod> umapListInConfigFromUserAuth;
    private String formUmapNotes;
    
    private List<User> userListForConfig;
    private User userSelectedForConfig;
    
    private RoleType selectedRoleType;
    private List<RoleType> roleTypeCandidateList;
    
    private Municipality selectedMuni;
    private List<Municipality> muniCandidateList;
    
    private String formUsername;
    private String formNotes;
    private String formInvalidateRecordReason;
    
    private List<Person> userPersonList;
    private Person selectedUserPerson;
    
    @PostConstruct
    public void initBean(){
        UserCoordinator uc = getUserCoordinator();
        setSelectedMuni(getSessionBean().getSessionMuni());
        SearchCoordinator searchCoord = getSearchCoordinator();
        MunicipalityCoordinator mc = getMuniCoordinator();
        
        try {
            userAuthorizedInConfig = getSessionBean().getSessionUser();
            umapInConfig = userAuthorizedInConfig.getMyCredential().getGoverningAuthPeriod();
            userListForConfig = uc.assembleUserListForConfig(getSessionBean().getSessionUser());
            muniCandidateList = mc.getPermittedMunicipalityListForAdminMuniAssignment(getSessionBean().getSessionUser());
            roleTypeCandidateList = uc.getPermittedRoleTypesToGrant(getSessionBean().getSessionUser());
        } catch (IntegrationException | AuthorizationException ex) {
            System.out.println(ex);
        }
        
         // user our fancy specialized query to get all Persons who are delcared to 
        // be user types
        QueryPerson qp = searchCoord.initQuery(QueryPersonEnum.USER_PERSONS, getSessionBean().getSessionUser().getMyCredential());
        try {
            qp = searchCoord.runQuery(qp);
            userPersonList = qp.getResults();
        } catch (SearchException ex) {
            Logger.getLogger(UserConfigBB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    /**
     * @return the userListForConfig
     */
    public List<User> getUserListForConfig() {
        return userListForConfig;
    }

    /**
     * @param userListForConfig the userListForConfig to set
     */
    public void setUserListForConfig(List<User> userListForConfig) {
        this.userListForConfig = userListForConfig;
    }

    /**
     * Creates a new instance of userConfig
     */
    public UserConfigBB() {
    }
    
    
    public void initiateCreateNewUser(ActionEvent ev){
        UserCoordinator uc = getUserCoordinator();
        userAuthorizedInConfig = new UserAuthorized(uc.getUserSkeleton(getSessionBean().getSessionUser()));
        System.out.println("UserConfigBB.createNewUser");
    }
    
    public String reInitSession(UserMuniAuthPeriod umap, Credential cred){
        UserCoordinator uc = getUserCoordinator();
        MunicipalityCoordinator mc = getMuniCoordinator();
        
        if(uc.verifyReInitSessionRequest(getSessionBean().getSessionUser(), umap)){
            try {
                getSessionBean().setSessionMuni(mc.assembleMuniDataHeavy(mc.getMuni(umap.getMuni().getMuniCode()), cred));
                getSessionBean().setSessionUserForReInitSession(uc.getUser(umap.getUserID()));
            } catch (IntegrationException | AuthorizationException | BObStatusException | EventException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Could not set your new session's municipality correctly, sorry!", ""));
            } 
            return "startInitiationProcess";
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Yikes! Improper authorization to become another user in another UMAP; "
                                    + "Seeing this message itself, in fact, cosntitutes a serious error!", ""));
            return "";
        }
        
    }
    
    public void initiateInvalidateUserAuthPeriod(UserAuthorized u, UserMuniAuthPeriod uap){
        umapInConfig = uap;
        userAuthorizedInConfig = u;
    }
    
    public void invalidateUserMuniAuthPeriod(){
        UserCoordinator uc = getUserCoordinator();
        try {
            uc.invalidateUserAuthPeriod(umapInConfig, getSessionBean().getSessionUser(), formInvalidateRecordReason);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully invalidated auth period id" + umapInConfig.getUserMuniAuthPeriodID(), ""));
        } catch (IntegrationException | AuthorizationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }
        
    }
    
    public void initiateManageAuthPeriods(User ua){
        try {
            UserCoordinator uc = getUserCoordinator();
            userAuthorizedInConfig = uc.transformUserToUserAuthorizedForConfig(getSessionBean().getSessionUser(), ua);
            umapListInConfigFromUserAuth = userAuthorizedInConfig.getMuniAuthPeriodsMap().get(userAuthorizedInConfig.getMyCredential().getGoverningAuthPeriod().getMuni());
            System.out.println("UserConfigBB.initiateViewAddAuthPeriods | UMAP list size: " + umapListInConfigFromUserAuth.size());
            if(umapListInConfigFromUserAuth != null){  
                umapInConfig = umapListInConfigFromUserAuth.get(0);
            }
        } catch (AuthorizationException | IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
            
        }
    }
    
    public void initiateAddAuthPeriod(){
        UserCoordinator uc = getUserCoordinator();
        try {
            umapInConfig = uc.initializeUserMuniAuthPeriod(getSessionBean().getSessionUser(), 
                                                            userAuthorizedInConfig, 
                                                            getSessionBean().getSessionMuni());
            formUmapNotes = "";
        } catch (AuthorizationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }
    }
    
    public void commitNewAuthPeriod(){
        UserCoordinator uc = getUserCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        try {
            if(formUmapNotes != null && formUmapNotes.length() > 0){
                umapInConfig.setNotes(sc.formatAndAppendNote(getSessionBean().getSessionUser(), 
                                                                formUmapNotes,
                                                                umapInConfig.getNotes()));
            }
            uc.insertUserMuniAuthorizationPeriod(getSessionBean().getSessionUser(), userAuthorizedInConfig, umapInConfig);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully added new auth period!", ""));
            reloadCurrentUMAP();
        } catch (AuthorizationException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }
    }
    
     public void initiateUserUpdates(User usr){
        UserCoordinator uc = getUserCoordinator();
        try {
            userAuthorizedInConfig = uc.transformUserToUserAuthorizedForConfig(getSessionBean().getSessionUser(), usr);
        } catch (AuthorizationException | IntegrationException ex) {
            getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                               "Could not authorize user for configuration", ""));

            
        }
    }
    
    public String editUserPersonRecord(){
        getSessionBean().setSessionPerson(userAuthorizedInConfig.getPerson());
        return "persons";
    }

    public void updateUser(UserAuthorized u) {
        
        userAuthorizedInConfig = u;

    }
    
    private void reloadCurrentUser(){
        UserCoordinator uc = getUserCoordinator();
        try {
            userAuthorizedInConfig = uc.transformUserToUserAuthorizedForConfig(getSessionBean().getSessionUser(), userAuthorizedInConfig);
            getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_INFO,
                               "Reloaded current user: " + userAuthorizedInConfig.getUsername(), ""));
        } catch (AuthorizationException | IntegrationException ex) {
            
         getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }
        
        
    }
    
    private void reloadCurrentUMAP(){
        UserIntegrator ui = getUserIntegrator();
        try {
            umapInConfig = ui.getUserMuniAuthPeriod(umapInConfig.getUserMuniAuthPeriodID());
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                               "Could not reload UMAP", ""));
            
        }
    }
    
    
    public void commitUsernameUpdates(ActionEvent ev){
        UserCoordinator uc = getUserCoordinator();
        try {
            uc.updateUser(userAuthorizedInConfig, null, formUsername);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully udpated user", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not update user", ""));
            
        }
    }
    
    
    
    public void commitUserInsert(ActionEvent ev) {
        System.out.println("UserBB.commitInsert");
        UserCoordinator uc = getUserCoordinator();
        String freshUserPswd = null;
        int freshUserID;
        User usr;
        
        try {
            freshUserID = uc.insertNewUser(userAuthorizedInConfig);
            if(freshUserID != 0){
                usr = uc.getUser(freshUserID);
                freshUserPswd = uc.generateRandomPassword();
                uc.updateUserPassword(usr, freshUserPswd);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Successfully added username " + usr.getUsername()
                                + " to the system with an initial password of " + freshUserPswd, ""));
                
            }
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to add user to system, my apologies",
                            "This is a system-level error that must be corrected by an administrator"));
        } catch (AuthorizationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to add user to system, my apologies",
                            "This is an authroization be corrected by an administrator"));
        }

    }

    
    
    public void commitUserPersonUpdates(ActionEvent ev){
        UserCoordinator uc = getUserCoordinator();
        try {
            uc.updateUser(userAuthorizedInConfig, selectedUserPerson, null);
            reloadCurrentUser();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully udpated your person link: see notes", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not update person link, sorry!", ""));
        }
    }
    
    
    
    
    public void commitUserCreation(){
        UserCoordinator uc = getUserCoordinator();
        int freshUserID;
        try {
            freshUserID = uc.insertNewUser(userAuthorizedInConfig);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "User add success! New user ID: " + freshUserID, ""));
        } catch (IntegrationException ex) {
            
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "User add failed; integration error", ""));
        }
        
    }
    
    public void resetCurrentUserPassword(){
        UserCoordinator uc = getUserCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        freshPasswordCleartext = uc.generateRandomPassword();
        try {
            uc.updateUserPassword(userAuthorizedInConfig, freshPasswordCleartext);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Password reset success! New password for " 
                                + userAuthorizedInConfig.getUsername() 
                                + " is now " + freshPasswordCleartext, ""));
            formInvalidateRecordReason = "";
            userAuthorizedInConfig.setNotes(sc.formatAndAppendNote(getSessionBean().getSessionUser(), 
                                    formInvalidateRecordReason, 
                                    userAuthorizedInConfig.getNotes()));
         } catch (IntegrationException | AuthorizationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Password update error in DB; password unchanged", ""));
            
        }
        
    }

   
    /**
     * @return the userAuthorizedInConfig
     */
    public UserAuthorized getUserAuthorizedInConfig() {
        return userAuthorizedInConfig;
    }

    /**
     * @return the selectedRoleType
     */
    public RoleType getSelectedRoleType() {
        return selectedRoleType;
    }

    /**
     * @return the roleTypeCandidateList
     */
    public List<RoleType> getRoleTypeCandidateList() {
        return roleTypeCandidateList;
    }

    /**
     * @return the selectedMuni
     */
    public Municipality getSelectedMuni() {
        return selectedMuni;
    }

    /**
     * @return the muniCandidateList
     */
    public List<Municipality> getMuniCandidateList() {
        return muniCandidateList;
    }

    /**
     * @param userAuthorizedInConfig the userAuthorizedInConfig to set
     */
    public void setUserAuthorizedInConfig(UserAuthorized userAuthorizedInConfig) {
        this.userAuthorizedInConfig = userAuthorizedInConfig;
    }

    /**
     * @param selectedRoleType the selectedRoleType to set
     */
    public void setSelectedRoleType(RoleType selectedRoleType) {
        this.selectedRoleType = selectedRoleType;
    }

    /**
     * @param roleTypeCandidateList the roleTypeCandidateList to set
     */
    public void setRoleTypeCandidateList(List<RoleType> roleTypeCandidateList) {
        this.roleTypeCandidateList = roleTypeCandidateList;
    }

    /**
     * @param selectedMuni the selectedMuni to set
     */
    public void setSelectedMuni(Municipality selectedMuni) {
        this.selectedMuni = selectedMuni;
    }

    /**
     * @param muniCandidateList the muniCandidateList to set
     */
    public void setMuniCandidateList(List<Municipality> muniCandidateList) {
        this.muniCandidateList = muniCandidateList;
    }

    /**
     * @return the freshPasswordCleartext
     */
    public String getFreshPasswordCleartext() {
        return freshPasswordCleartext;
    }

    /**
     * @param freshPasswordCleartext the freshPasswordCleartext to set
     */
    public void setFreshPasswordCleartext(String freshPasswordCleartext) {
        this.freshPasswordCleartext = freshPasswordCleartext;
    }

    /**
     * @return the umapInConfig
     */
    public UserMuniAuthPeriod getUmapInConfig() {
        return umapInConfig;
    }

    /**
     * @param umapInConfig the umapInConfig to set
     */
    public void setUmapInConfig(UserMuniAuthPeriod umapInConfig) {
        this.umapInConfig = umapInConfig;
    }


    /**
     * @return the formUsername
     */
    public String getFormUsername() {
        return formUsername;
    }



    /**
     * @param formUsername the formUsername to set
     */
    public void setFormUsername(String formUsername) {
        this.formUsername = formUsername;
    }


    /**
     * @return the userPersonList
     */
    public List<Person> getUserPersonList() {
        return userPersonList;
    }

    /**
     * @return the selectedUserPerson
     */
    public Person getSelectedUserPerson() {
        return selectedUserPerson;
    }

    /**
     * @param userPersonList the userPersonList to set
     */
    public void setUserPersonList(List<Person> userPersonList) {
        this.userPersonList = userPersonList;
    }

    /**
     * @param selectedUserPerson the selectedUserPerson to set
     */
    public void setSelectedUserPerson(Person selectedUserPerson) {
        this.selectedUserPerson = selectedUserPerson;
    }

    /**
     * @return the formInvalidateRecordReason
     */
    public String getFormInvalidateRecordReason() {
        return formInvalidateRecordReason;
    }

    /**
     * @param formInvalidateRecordReason the formInvalidateRecordReason to set
     */
    public void setFormInvalidateRecordReason(String formInvalidateRecordReason) {
        this.formInvalidateRecordReason = formInvalidateRecordReason;
    }

   

    /**
     * @return the umapListInConfigFromUserAuth
     */
    public List<UserMuniAuthPeriod> getUmapListInConfigFromUserAuth() {
        return umapListInConfigFromUserAuth;
    }

    /**
     * @param umapListInConfigFromUserAuth the umapListInConfigFromUserAuth to set
     */
    public void setUmapListInConfigFromUserAuth(List<UserMuniAuthPeriod> umapListInConfigFromUserAuth) {
        this.umapListInConfigFromUserAuth = umapListInConfigFromUserAuth;
    }

    /**
     * @return the formUmapNotes
     */
    public String getFormUmapNotes() {
        return formUmapNotes;
    }

    /**
     * @param formUmapNotes the formUmapNotes to set
     */
    public void setFormUmapNotes(String formUmapNotes) {
        this.formUmapNotes = formUmapNotes;
    }

    /**
     * @return the userSelectedForConfig
     */
    public User getUserSelectedForConfig() {
        return userSelectedForConfig;
    }

    /**
     * @param userSelectedForConfig the userSelectedForConfig to set
     */
    public void setUserSelectedForConfig(User userSelectedForConfig) {
        this.userSelectedForConfig = userSelectedForConfig;
    }

    /**
     * @return the formNotes
     */
    public String getFormNotes() {
        return formNotes;
    }

    /**
     * @param formNotes the formNotes to set
     */
    public void setFormNotes(String formNotes) {
        this.formNotes = formNotes;
    }
    
}
