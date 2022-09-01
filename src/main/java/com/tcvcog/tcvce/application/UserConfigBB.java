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
import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.integration.UserIntegrator;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.event.ActionEvent;

/**
 * SECURITY CRITICAL: Backs the internal management page for users.
 * This involves creating users and most importantly, creating 
 * UserMuniAuthPeriod objects which allow a user into a municipality
 * with a certain credential from date x to y.
 * 
 * @author sylvia
 */
public class UserConfigBB extends BackingBeanUtils{
    
    
    private UserAuthorizedForConfig currentUserAuthorizedForConfig;
    private String freshPasswordCleartext;
    
    private UserMuniAuthPeriod currentUMAPInConfig;
    private String formUmapNotes;
    
    private List<UserAuthorizedForConfig> userListForConfig;
    private List<UserAuthorizedForConfig> userListForConfigFiltered;
    
    
    private RoleType selectedRoleType;
    private List<RoleType> roleTypeCandidateList;
    
    private Municipality selectedMuni;
    private List<Municipality> muniCandidateList;
    
    private String formUsername;
    private String formNoteText;
    private String formInvalidateRecordReason;
    
    private boolean humanLinkEditMode;
    private Human humanForLinking;
    
    private List<Human> userPersonList;
    protected int personIDToLink;
    protected boolean personLinkUseID;
    private Person selectedUserPerson;
    
      /**
     * Creates a new instance of userConfig
     */
    public UserConfigBB() {
    }
    
    /**
     * Initializer for the User configuration process and UMAP creation.
     * Reworked for mccandless June 2022 to revise wrt consensus patterns
     */
    @PostConstruct
    public void initBean(){
        System.out.println("UserConfigBB.initBean()");
        UserCoordinator uc = getUserCoordinator();
        setSelectedMuni(getSessionBean().getSessMuni());
        PersonCoordinator pc = getPersonCoordinator();
        MunicipalityCoordinator mc = getMuniCoordinator();
        
        
        try {
            User uTemp = null;
            // session queue first
            if(getSessionBean().getUserForConfig() != null){
                uTemp = getSessionBean().getUserForConfig();
            // then the user itself
            } else {
                uTemp = getSessionBean().getSessUser();
            }
            // convert to config subclass
            if(uTemp != null){
                currentUserAuthorizedForConfig = uc.user_transformUserToUserAuthorizedForConfig(uTemp);
            }
            if(currentUserAuthorizedForConfig != null){
                if(currentUserAuthorizedForConfig.getUmapList() != null && !currentUserAuthorizedForConfig.getUmapList().isEmpty()){
                    currentUMAPInConfig = currentUserAuthorizedForConfig.getUmapList().get(0);
                } else {
                    System.out.println("UserConfigBB.initBean: ERR-UCBB-I1:NO UMAP found, even in the current user!");
                    
                }
                onConfigureUserLinkClick(currentUserAuthorizedForConfig);
                reloadUserForConfigList();
                userListForConfigFiltered = new ArrayList<>();
                muniCandidateList = mc.getPermittedMunicipalityListForAdminMuniAssignment(getSessionBean().getSessUser());
                roleTypeCandidateList = uc.auth_getPermittedRoleTypesToGrant(getSessionBean().getSessUser());
                personLinkUseID = false;
            } else {
                System.out.println("UserConfigBB.initBean: ERR-UCBB-I2 FATAL init error; null userconfig");
            }
        } catch (IntegrationException | AuthorizationException | BObStatusException ex) {
            System.out.println(ex);
        }
        
        try {
            userPersonList = pc.getHumansMappedToUsers();
        } catch ( IntegrationException ex) {
            System.out.println(ex);
        }
    }
    
    /**
     * gets a new set of users from DB
     */
    private void reloadUserForConfigList() throws IntegrationException, AuthorizationException, BObStatusException{
        UserCoordinator uc = getUserCoordinator();
        userListForConfig = uc.user_auth_assembleUserListForConfig(getSessionBean().getSessUser());
        
    }
    
    /**
     * Primary listener method which copies a reference to the selected 
     * user from the list and sets it on the selected user perch
     * @param uafc
     */
    public void onConfigureUserLinkClick(UserAuthorizedForConfig uafc){
        UserCoordinator uc = getUserCoordinator();
        if(uafc != null){
            try {
                currentUserAuthorizedForConfig = uc.user_transformUserToUserAuthorizedForConfig(uafc);
                
                // maybe don't set the session here
                getSessionBean().setUserForConfig(currentUserAuthorizedForConfig);
                System.out.println("UserConfigBB.onObjectViewButtonChange: Assmbled user for config for " + currentUserAuthorizedForConfig.getUsername());
            } catch (AuthorizationException | IntegrationException | BObStatusException ex) {
                System.out.println("UserConfigBB.onObjectViewButtonChange: EXception converting inputted user;");
                getFacesContext().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "FATAL: ERR-UC-CUL1:Error setting user up for configuration", ""));
            }
        }
    }
    
    /**
     * Internal logic container for beginning the user creation change process
     * Delegated from the mode button router
     */
    public void onAddUserInitButtonChange(){
        UserCoordinator uc = getUserCoordinator();
        try {
            currentUserAuthorizedForConfig = uc.user_transformUserToUserAuthorizedForConfig(uc.user_getUserSkeleton(getSessionBean().getSessUser()));
            currentUserAuthorizedForConfig.setHuman(getSessionBean().getSessPerson());
            currentUserAuthorizedForConfig.setHomeMuni(getSessionBean().getSessMuni());
        } catch (IntegrationException | AuthorizationException | BObStatusException ex) {
            System.out.println(ex);
        }
        System.out.println("UserConfigBB.createNewUser");
    }
     
     /**
      * Listener method for users to check the username choice to avoid duplicate
      * @param ev 
      */
     public void onUsernameCheckButtonChange(ActionEvent ev){
         System.out.println("UserConfigBB.onUsernameCheckButtonChange: username: " + currentUserAuthorizedForConfig.getUsername());
         UserCoordinator uc = getUserCoordinator();
         if(currentUserAuthorizedForConfig != null 
                 && currentUserAuthorizedForConfig.getUsername() != null &&
                 !currentUserAuthorizedForConfig.getUsername().equals("")){
             if(uc.user_checkUsernameAllowedForInsert(currentUserAuthorizedForConfig.getUsername())){
                getFacesContext().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Proceed! " + currentUserAuthorizedForConfig.getUsername() + " is available.", ""));
                 
             } else {
                 
                getFacesContext().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Halt! " + currentUserAuthorizedForConfig.getUsername() + " is already in use.", ""));
             }
                 
         }
                getFacesContext().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Invalid username: too short, non-existant, or an empty string.", ""));
     }
    
    
     /**
     * Listener for button clicks when user is ready to insert a new EventRule.
     * Delegates all work to internal method
     * @param ev
     * @return 
     */
    public void onUserInsertCommitButtonChange(ActionEvent ev) {
         System.out.println("UserBB.commitInsert");
        UserCoordinator uc = getUserCoordinator();
        PersonCoordinator pc = getPersonCoordinator();
        
        int freshUserID;
        User usr;
        
        if(currentUserAuthorizedForConfig.getHuman() != null){
            try {
                freshUserID = uc.user_insertNewUser(currentUserAuthorizedForConfig);
                if(freshUserID != 0){
                    usr = uc.user_getUser(freshUserID);
                    getSessionBean().setUserForConfig(currentUserAuthorizedForConfig);
                    reloadCurrentUser();
                    reloadUserForConfigList();
                    if(usr != null){
                        System.out.println("UserConfigBB.insertUser : retrieved new user");
                    } else {
                        System.out.println("UserConfigBB.insertUser : null new user!");
                    }
                }

            } catch (IntegrationException | BObStatusException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to add user to system, my apologies",
                                "This is a system-level error that must be corrected by an administrator"));
            } catch (AuthorizationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                ex.getMessage(),
                                ""));
            }
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "You must create a user with a link to a person object! Please either"
                                    + "select a person from the drop-down box or enter a valid Person ID",""));
            
        }
        
    }

    
    /**
     * Starts user-person update sequence
     * @param ev 
     */
    public void onUpdateUserPersonLinkInitLinkClick(ActionEvent ev){
        humanLinkEditMode = true;
        humanForLinking = getSessionBean().getSessPerson();
        
        
    }
    
    
    /**
     * Commits user-person link updates
     * @param ev 
     */
    public void onUpdateUserPersonLinkCommitButtonChange(ActionEvent ev){
        
        if(humanForLinking != null && humanForLinking.getHumanID() != 0){
            currentUserAuthorizedForConfig.setHuman(humanForLinking);
            currentUserAuthorizedForConfig.setHumanID(humanForLinking.getHumanID());
            onUserUpdateCommitButtonChange();
              getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "User " + currentUserAuthorizedForConfig.getUsername() + " is now linked to " + humanForLinking.getName() + " (HumanID:" + humanForLinking.getHumanID() + ")", ""));
        } else {
            
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not update person link because selected human is null or has an ID of 0", ""));
        }
        
        humanLinkEditMode = false;
        
        
    }
    
    
    public void onUpdateUserPersonAbortButtonCange(ActionEvent ev){
        getFacesContext().addMessage(null,
              new FacesMessage(FacesMessage.SEVERITY_INFO,
                      "User " + currentUserAuthorizedForConfig.getUsername() + " is now linked to " + humanForLinking.getName() + " (HumanID:" + humanForLinking.getHumanID() + ")", ""));
        humanLinkEditMode = false;
    }
    
    
    
    
    
    /**
     * Listener for user requests to submit object updates;
     * Delegates all work to coordinator method
     * @return 
     */
    private String onUserUpdateCommitButtonChange() {
        UserCoordinator uc = getUserCoordinator();
        try {
            uc.user_updateUser(currentUserAuthorizedForConfig);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully udpated user", ""));
            getSessionBean().setUserForConfig(uc.user_transformUserToUserAuthorizedForConfig(currentUserAuthorizedForConfig));
            reloadCurrentUser();
            reloadUserForConfigList();
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not update user", ""));
            
        } catch (AuthorizationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }
        return "";
    }
   
      /**
     * Listener for commencement of note writing process
     * @param ev 
     */
    public void onNoteInitButtonChange(ActionEvent ev){
        formNoteText = new String();
    }
    
    /**
     * Listener for user requests to commit new note content to the current Property
     * 
     * @param ev 
     */
    public void onNoteCommitButtonChange(ActionEvent ev){
        SystemCoordinator sc = getSystemCoordinator();
        UserCoordinator uc = getUserCoordinator();
        
        MessageBuilderParams mbp = new MessageBuilderParams();
        mbp.setCred(getSessionBean().getSessUser().getKeyCard());
        mbp.setExistingContent(currentUserAuthorizedForConfig.getNotes());
        mbp.setNewMessageContent(formNoteText);
        mbp.setUser(getSessionBean().getSessUser());
        mbp.setIncludeCredentialSig(false);
        currentUserAuthorizedForConfig.setNotes(sc.appendNoteBlock(mbp));
        try {
            currentUserAuthorizedForConfig.setLastUpdatedTS(LocalDateTime.now());
            uc.user_appendNoteToUser(currentUserAuthorizedForConfig, mbp);
            getFacesContext().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Succesfully appended note!", ""));
            reloadCurrentUser();
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Fatal error updating user in DB; apologies!", ""));
        }        
    }

    /**
     * Listener for user requests to start to remove the currently selected ERA;
     * @param ev
     */
    public void onUserDeactivateInitButtonChange(ActionEvent ev) {
        //  nothign to do here yet
    }
    
    /**
     * Listener for user requests to remove the currently selected ERA;
     * Delegates all work to internal, non-listener method.
     * @return 
     */
    public void onUserDeactivateCommitButtonChange(ActionEvent ev) {
        UserCoordinator uc = getUserCoordinator();
        
        try{
            uc.user_deactivateUser(getSessionBean().getSessUser(), currentUserAuthorizedForConfig);
            reloadCurrentUser();
            reloadUserForConfigList();
            getFacesContext().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully removed user from active management", ""));
        } catch (AuthorizationException | IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Unable to deactivate due to Auth or DB error!", ""));
            
        }
        
        
    }
    
    /**
     * Listener for user requests to abandon their update or insert changes
     * @param ev 
     */
    public void onDiscardChangesButton(ActionEvent ev){
            getFacesContext().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Changes not saved!", ""));
        
    }
    
    
 
    /**
     * Listener for user requests to view super table
     * @param ev 
     */
    public void onLoadFullTableButtonChange(ActionEvent ev){
        
        
    }
    
    
    
    
    /**
     * @return the userListForConfig
     */
    public List<UserAuthorizedForConfig> getUserListForConfig() {
        return userListForConfig;
    }

    /**
     * @param userListForConfig the userListForConfig to set
     */
    public void setUserListForConfig(List<UserAuthorizedForConfig> userListForConfig) {
        this.userListForConfig = userListForConfig;
    }

  
    
    /**
     * Listener for special Dev privileges to start a session from ANY VALID UMAP
     * @param umap
     * @param ua
     * @return 
     */
    public String onSessionReinitWithNewUMAP(UserMuniAuthPeriod umap, UserAuthorized ua){
        UserCoordinator uc = getUserCoordinator();
        MunicipalityCoordinator mc = getMuniCoordinator();
        
        if(uc.auth_verifyReInitSessionRequest_SECURITYCRITICAL(getSessionBean().getSessUser(), umap)){
            try {
                getSessionBean().setSessMuni(mc.assembleMuniDataHeavy(mc.getMuni(umap.getMuni().getMuniCode()), ua));
                getSessionBean().setUserForReInit(uc.user_getUser(umap.getUserID()));
            } catch (IntegrationException | AuthorizationException | BObStatusException | EventException | BlobException ex) {
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
    
    /**
     * Listener for users to begin the UMAP invalidation process
     * @param uap 
     */
    public void onInvalidateUserAuthPeriodInit(UserMuniAuthPeriod uap){
        currentUMAPInConfig = uap;
        
    }
    
    /**
     * User request to submit invalidation of of a UMAP
     * @return 
     */
    public String onInvalidateUserMuniAuthPeriodCommit(){
        SystemCoordinator sc = getSystemCoordinator();
        UserCoordinator uc = getUserCoordinator();
        try {
            uc.auth_invalidateUserAuthPeriod(currentUMAPInConfig, getSessionBean().getSessUser(), formInvalidateRecordReason);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully invalidated auth period id" + currentUMAPInConfig.getUserMuniAuthPeriodID(), ""));
            MessageBuilderParams mbp = new MessageBuilderParams();
            mbp.setCred(getSessionBean().getSessUser().getKeyCard());
            mbp.setExistingContent(currentUserAuthorizedForConfig.getNotes());
            mbp.setNewMessageContent(formInvalidateRecordReason);
            mbp.setUser(getSessionBean().getSessUser());
            
            uc.user_appendNoteToUser(currentUserAuthorizedForConfig, mbp);
            formInvalidateRecordReason = "";
        } catch (IntegrationException | AuthorizationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }
     return "";   
    }

    /**
     * Listener for user beginning UMAP insert process
     */
    public void onAuthPeriodNewInit(){
        UserCoordinator uc = getUserCoordinator();
        try {
            currentUMAPInConfig = uc.auth_initializeUserMuniAuthPeriod_SECURITYCRITICAL(getSessionBean().getSessUser(), 
                                                            currentUserAuthorizedForConfig, 
                                                            getSessionBean().getSessMuni());
            formUmapNotes = "";
        } catch (AuthorizationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }
    }
    
    /**
     * Listener for user's to indicate completion of UMAP insert form for processing
     * @return 
     */
    public void onAuthPeriodNewCommit(ActionEvent ev){
        UserCoordinator uc = getUserCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        try {
            if(formUmapNotes != null && formUmapNotes.length() > 0){
                currentUMAPInConfig.setNotes(sc.formatAndAppendNote(getSessionBean().getSessUser(), 
                                                                formUmapNotes,
                                                                currentUMAPInConfig.getNotes()));
            }
            uc.auth_insertUserMuniAuthorizationPeriod_SECURITYCRITICAL(getSessionBean().getSessUser(), currentUserAuthorizedForConfig, currentUMAPInConfig);
            reloadCurrentUser();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully added new auth period!", ""));
            // wipe our previously inserted UMAP with a fresh one
            onAuthPeriodNewInit();
        } catch (AuthorizationException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Auth period add error: " + ex.getMessage(), ""));
        }
        
    }
    
    
    
   
    
    
    /**
     * Listener for requests to jump to edit a Person record
     * @param p
     * @return 
     */
    public String onEditUserPersonRecordButtonChange(Person p){
        PersonCoordinator pc = getPersonCoordinator();
        try {
            getSessionBean().setSessPersonQueued(pc.getPerson(currentUserAuthorizedForConfig.getHuman()));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
        } 
        return "persons";
    }

    
    
    /**
     * Internal organ for refreshing the current UserAuthorized
     */
    private void reloadCurrentUser(){
        UserCoordinator uc = getUserCoordinator();
        try {
            currentUserAuthorizedForConfig = uc.user_transformUserToUserAuthorizedForConfig(currentUserAuthorizedForConfig);
            getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_INFO,
                               "Reloaded current user: " + currentUserAuthorizedForConfig.getUsername(), ""));
        } catch (AuthorizationException | IntegrationException | BObStatusException ex) {
            
         getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }
        
        
    }
    
    /**
     * Internal organ for refreshing the UMAP being edited
     */
    private void reloadCurrentUMAP(){
        UserIntegrator ui = getUserIntegrator();
        try {
            currentUMAPInConfig = ui.getUserMuniAuthPeriod(currentUMAPInConfig.getUserMuniAuthPeriodID());
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                               "Could not reload UMAP", ""));
            
        }
    }
    


    
  
    
    
    /**
     * Logic container for liasing with the UserCoordinator for password reset
     */
    public void onForceResetUserPassword(){
        UserCoordinator uc = getUserCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        freshPasswordCleartext = uc.user_generateRandomPassword_SECURITYCRITICAL();
        try {
            uc.user_updateUserPassword_SECURITYCRITICAL(currentUserAuthorizedForConfig, freshPasswordCleartext);
//            getFacesContext().addMessage(null,
//                new FacesMessage(FacesMessage.SEVERITY_INFO, 
//                        "Password reset success! New password for " 
//                                + currentUserAuthorizedForConfig.getUsername() 
//                                + " is now " + freshPasswordCleartext, ""));
          
         } catch (IntegrationException | AuthorizationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Password update error in DB; password unchanged", ""));
            
        }
        
    }
    
    /**
     * Starts the new password process
     * @param ev 
     */
    public void onRestUserPasswordInit(ActionEvent ev){
        System.out.println("UserConfigBB.onResetUserPasswordInit");
    }
    
    
    /**
     * Logic container for liasing with the UserCoordinator for password reset
     */
    public void onResetUserPassword(){
        UserCoordinator uc = getUserCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        freshPasswordCleartext = uc.user_generateRandomPassword_SECURITYCRITICAL();
        try {
            uc.user_updateUserPassword_SECURITYCRITICAL(currentUserAuthorizedForConfig, freshPasswordCleartext);
//            getFacesContext().addMessage(null,
//                new FacesMessage(FacesMessage.SEVERITY_INFO, 
//                        "Password reset success! New password for " 
//                                + currentUserAuthorizedForConfig.getUsername() 
//                                + " is now " + freshPasswordCleartext, ""));
            // TODO: get this in the right place
//            formInvalidateRecordReason = "";
//            currentUserAuthorizedForConfig.setNotes(sc.formatAndAppendNote(getSessionBean().getSessUser(), 
//                                    formInvalidateRecordReason, 
//                                    currentUserAuthorizedForConfig.getNotes()));
            
            uc.user_forcePasswordReset(currentUserAuthorizedForConfig);

         } catch (IntegrationException | AuthorizationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Password update error in DB; password unchanged", ""));
            
        }
        
    }

   
    /**
     * @return the currentUserAuthorizedForConfig
     */
    public UserAuthorizedForConfig getCurrentUserAuthorizedForConfig() {
        return currentUserAuthorizedForConfig;
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
     * @param currentUserAuthorizedForConfig the currentUserAuthorizedForConfig to set
     */
    public void setCurrentUserAuthorizedForConfig(UserAuthorizedForConfig currentUserAuthorizedForConfig) {
        this.currentUserAuthorizedForConfig = currentUserAuthorizedForConfig;
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
     * @return the currentUMAPInConfig
     */
    public UserMuniAuthPeriod getCurrentUMAPInConfig() {
        return currentUMAPInConfig;
    }

    /**
     * @param currentUMAPInConfig the currentUMAPInConfig to set
     */
    public void setCurrentUMAPInConfig(UserMuniAuthPeriod currentUMAPInConfig) {
        this.currentUMAPInConfig = currentUMAPInConfig;
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
    public List<Human> getUserPersonList() {
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
    public void setUserPersonList(List<Human> userPersonList) {
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
     * @return the formNoteText
     */
    public String getFormNoteText() {
        return formNoteText;
    }

    /**
     * @param formNoteText the formNoteText to set
     */
    public void setFormNoteText(String formNoteText) {
        this.formNoteText = formNoteText;
    }

  
 
    /**
     * @return the personIDToLink
     */
    public int getPersonIDToLink() {
        return personIDToLink;
    }

    /**
     * @param personIDToLink the personIDToLink to set
     */
    public void setPersonIDToLink(int personIDToLink) {
        this.personIDToLink = personIDToLink;
    }

    /**
     * @return the personLinkUseID
     */
    public boolean isPersonLinkUseID() {
        return personLinkUseID;
    }

    /**
     * @param personLinkUseID the personLinkUseID to set
     */
    public void setPersonLinkUseID(boolean personLinkUseID) {
        this.personLinkUseID = personLinkUseID;
    }

    /**
     * @return the userListForConfigFiltered
     */
    public List<UserAuthorizedForConfig> getUserListForConfigFiltered() {
        return userListForConfigFiltered;
    }

    /**
     * @param userListForConfigFiltered the userListForConfigFiltered to set
     */
    public void setUserListForConfigFiltered(List<UserAuthorizedForConfig> userListForConfigFiltered) {
        this.userListForConfigFiltered = userListForConfigFiltered;
    }

    /**
     * @return the humanLinkEditMode
     */
    public boolean isHumanLinkEditMode() {
        return humanLinkEditMode;
    }

    /**
     * @param humanLinkEditMode the humanLinkEditMode to set
     */
    public void setHumanLinkEditMode(boolean humanLinkEditMode) {
        this.humanLinkEditMode = humanLinkEditMode;
    }

    /**
     * @return the humanForLinking
     */
    public Human getHumanForLinking() {
        return humanForLinking;
    }

    /**
     * @param humanForLinking the humanForLinking to set
     */
    public void setHumanForLinking(Human humanForLinking) {
        this.humanForLinking = humanForLinking;
    }

   
}
