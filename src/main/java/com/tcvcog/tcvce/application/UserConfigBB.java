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
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.PageModeEnum;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserMuniAuthPeriod;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.UserAuthorizedForConfig;
import com.tcvcog.tcvce.entities.search.QueryPerson;
import com.tcvcog.tcvce.entities.search.QueryPersonEnum;
import com.tcvcog.tcvce.integration.UserIntegrator;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    
    private PageModeEnum currentMode;
    private List<PageModeEnum> pageModes;

    
    private UserAuthorizedForConfig userAuthorizedInConfig;
    private String freshPasswordCleartext;
    
    private UserMuniAuthPeriod umapInConfig;
    private String formUmapNotes;
    
    private List<User> userListForConfig;
    private boolean userSelected;
    
    
    private RoleType selectedRoleType;
    private List<RoleType> roleTypeCandidateList;
    
    private Municipality selectedMuni;
    private List<Municipality> muniCandidateList;
    
    private String formUsername;
    private String formNoteText;
    private String formInvalidateRecordReason;
    
    private List<Person> userPersonList;
    protected int personIDToLink;
    protected boolean personLinkUseID;
    private Person selectedUserPerson;
    
      /**
     * Creates a new instance of userConfig
     */
    public UserConfigBB() {
    }
    
    /**
     * Initializer for the User configuration process and UMAP creation
     */
    @PostConstruct
    public void initBean(){
        System.out.println("UserConfigBB.initBean()");
        UserCoordinator uc = getUserCoordinator();
        setSelectedMuni(getSessionBean().getSessMuni());
        SearchCoordinator searchCoord = getSearchCoordinator();
        MunicipalityCoordinator mc = getMuniCoordinator();
        
        setPageModes(new ArrayList<PageModeEnum>());
        getPageModes().add(PageModeEnum.LOOKUP);
        getPageModes().add(PageModeEnum.INSERT);
        getPageModes().add(PageModeEnum.UPDATE);
        getPageModes().add(PageModeEnum.REMOVE);
         // use same pathway as clicking the button
        currentMode = PageModeEnum.LOOKUP;
        setCurrentMode(currentMode);
        
        try {
            User uTemp = null;
            if(getSessionBean().getUserForConfig() != null){
                uTemp = getSessionBean().getUserForConfig();
            } else {
                uTemp = getSessionBean().getSessUser();
            }
            if(uTemp != null){
                userAuthorizedInConfig = uc.user_transformUserToUserAuthorizedForConfig(uTemp);
            }
            if(userAuthorizedInConfig != null){
                userSelected = true;
                if(userAuthorizedInConfig.getUmapList() != null && !userAuthorizedInConfig.getUmapList().isEmpty()){
                    umapInConfig = userAuthorizedInConfig.getUmapList().get(0);
                } else {
                    onAuthPeriodNewInit();
                }
                onObjetViewButtonChange(userAuthorizedInConfig);
                userListForConfig = uc.user_auth_assembleUserListForConfig(getSessionBean().getSessUser());
                muniCandidateList = mc.getPermittedMunicipalityListForAdminMuniAssignment(getSessionBean().getSessUser());
                roleTypeCandidateList = uc.auth_getPermittedRoleTypesToGrant(getSessionBean().getSessUser());
                personLinkUseID = false;
            } else {
                System.out.println("UserConfigBB.initBean: FATAL init error; null userconfig");
            }
        } catch (IntegrationException | AuthorizationException ex) {
            System.out.println(ex);
        }
        
         // user our fancy specialized query to get all Persons who are delcared to 
        // be user types
        QueryPerson qp = searchCoord.initQuery(QueryPersonEnum.USER_PERSONS, getSessionBean().getSessUser().getMyCredential());
        try {
            qp = searchCoord.runQuery(qp);
            userPersonList = qp.getResults();
        } catch (SearchException ex) {
            System.out.println(ex);
        }
    }
    
    /**
     * Getter for currentMode
     * @return 
     */
    public PageModeEnum getCurrentMode() {
        return currentMode;
    }

    /**
     * Responds to the user clicking one of the page modes: LOOKUP, ADD, UPDATE, REMOVE
     * @param mode     
     */
    public void setCurrentMode(PageModeEnum mode) {

        //store currentMode into tempCurMode as a temporary value, in case the currenMode equal null
        PageModeEnum tempCurMode = this.currentMode;
        //reset default setting every time the Mode has been selected 
//        loadDefaultPageConfig();
        //check the currentMode == null or not
        if (currentMode == null) {
            this.currentMode = tempCurMode;
        } else {
            this.currentMode = mode;
            switch(currentMode){
                case LOOKUP:
                    onModeLookupInit();
                    break;
                case INSERT:
                    onModeInsertInit();
                    break;
                case UPDATE:
                    onModeUpdateInit();
                    break;
                case REMOVE:
                    onModeRemoveInit();
                    break;
                default:
                    break;
                    
            }
        }
    }
    

    //check if current mode == Lookup
    public boolean getActiveLookupMode() {
        // hard-wired on since there's always a property loaded
        return PageModeEnum.LOOKUP.equals(currentMode) ;
    }

    /**
     * Provide UI elements a boolean true if the mode is UPDATE
     * @return 
     */
    public boolean getActiveUpdateMode(){
        return PageModeEnum.UPDATE.equals(currentMode);
    }


    //check if current mode == Insert
    public boolean getActiveInsertUpdateMode() {
        return PageModeEnum.INSERT.equals(currentMode) || PageModeEnum.UPDATE.equals(currentMode);
    }

    //check if current mode == Remove
    public boolean getActiveRemoveMode() {
        return PageModeEnum.REMOVE.equals(currentMode);
    }

    
    /**
     * Primary listener method which copies a reference to the selected 
     * user from the list and sets it on the selected user perch
     * @param u 
     */
    public void onObjetViewButtonChange(User u){
        UserCoordinator uc = getUserCoordinator();
        if(u != null){
            try {
                userAuthorizedInConfig = uc.user_transformUserToUserAuthorizedForConfig(u);
                getSessionBean().setUserForConfig(userAuthorizedInConfig);
                userSelected = true;
                System.out.println("UserConfigBB.onObjectViewButtonChange: Assmbled user for config for " + userAuthorizedInConfig.getUsername());
            } catch (AuthorizationException | IntegrationException ex) {
            
            }
        }
    }
    
   
     
 
    /**
     * Internal logic container for changes to page mode: Lookup
     */
    private void onModeLookupInit(){
        UserCoordinator uc = getUserCoordinator();
        try {
            userListForConfig = uc.user_auth_assembleUserListForConfig(getSessionBean().getSessUser());
        } catch (IntegrationException | AuthorizationException ex) {
              getFacesContext().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "FATAL: Unable to load user list from database; Apologies", ""));
        }
        
    }
    
   
    
    /**
     * Internal logic container for beginning the user creation change process
     * Delegated from the mode button router
     */
    public void onModeInsertInit(){
        UserCoordinator uc = getUserCoordinator();
        try {
            userAuthorizedInConfig = uc.user_transformUserToUserAuthorizedForConfig(uc.user_getUserSkeleton(getSessionBean().getSessUser()));
        } catch (IntegrationException | AuthorizationException ex) {
            System.out.println(ex);
        }
        System.out.println("UserConfigBB.createNewUser");
    }
    
    
     public void onModeUpdateInit(){
         // nothign to do here yet since the user is selected
     }
     
    
     public void onModeRemoveInit(){
       // nothing to do here yet but let panels reset themselves
    }
     
     
     public void onLoadAllUsersButtonChange(ActionEvent ev){
         UserCoordinator uc = getUserCoordinator();
        try {
            userListForConfig = uc.user_assembleUserListComplete(getSessionBean().getSessUser());
            if(userListForConfig != null && !userListForConfig.isEmpty()){
                userAuthorizedInConfig = uc.user_transformUserToUserAuthorizedForConfig(userListForConfig.get(0));
            } else {
                userAuthorizedInConfig = uc.user_transformUserToUserAuthorizedForConfig(getSessionBean().getSessUser());
            }
            System.out.println("UserConfigBB.onLoadAllUsersButtonChange: ulist size " + userListForConfig.size());
              getFacesContext().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Complete user list loaded!", ""));
        } catch (IntegrationException | AuthorizationException ex) {
            System.out.println(ex);  
            getFacesContext().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "FATAL: Unable to load complete user list from database; Apologies", ""));
            
        }
     }
    
     
     /**
      * Listener method for users to check the username choice to avoid duplicate
      * @param ev 
      */
     public void onUsernameCheckButtonChange(ActionEvent ev){
         System.out.println("UserConfigBB.onUsernameCheckButtonChange: username: " + userAuthorizedInConfig.getUsername());
         UserCoordinator uc = getUserCoordinator();
         if(userAuthorizedInConfig != null 
                 && userAuthorizedInConfig.getUsername() != null &&
                 !userAuthorizedInConfig.getUsername().equals("")){
             if(uc.user_checkUsernameAllowedForInsert(userAuthorizedInConfig.getUsername())){
                getFacesContext().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Proceed! " + userAuthorizedInConfig.getUsername() + " is available.", ""));
                 
             } else {
                 
                getFacesContext().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Halt! " + userAuthorizedInConfig.getUsername() + " is already in use.", ""));
             }
                 
         }
                getFacesContext().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Invalid username: too short, non-existant, or an empty string.", ""));
         
         
     }
    
    
     /**
     * Listener for button clicks when user is ready to insert a new EventRule.
     * Delegates all work to internal method
     * @return 
     */
    public String onUserInsertCommitButtonChange() {
         System.out.println("UserBB.commitInsert");
        UserCoordinator uc = getUserCoordinator();
        PersonCoordinator pc = getPersonCoordinator();
        
        int freshUserID;
        User usr;
        if(personIDToLink != 0){
            
             try {
                 Person p = pc.getPerson(personIDToLink);
                 userAuthorizedInConfig.setPerson(p);
             } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Invalid person ID; please correct the person ID",
                                ""));
             }
        }
        if(userAuthorizedInConfig.getPerson() != null){
            try {
                freshUserID = uc.user_insertNewUser(userAuthorizedInConfig);
                if(freshUserID != 0){
                    usr = uc.user_getUser(freshUserID);
                    getSessionBean().setUserForConfig(userAuthorizedInConfig);
                    reloadCurrentUser();
                    if(usr != null){
                        System.out.println("UserConfigBB.insertUser : retrieved new user");
                    } else {
                        System.out.println("UserConfigBB.insertUser : null new user!");
                        
                    }

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
                                ex.getMessage(),
                                ""));
            }
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "You must create a user with a link to a person object! Please either"
                                    + "select a person from the drop-down box or enter a valid Person ID",""));
            
        }
        return "";
    }

    /**
     * Listener for user requests to submit object updates;
     * Delegates all work to internal method
     * @return 
     */
    public String onUserUpdateCommitButtonChange() {
        UserCoordinator uc = getUserCoordinator();
        try {
            uc.user_updateUser(userAuthorizedInConfig);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully udpated user", ""));
            getSessionBean().setUserForConfig(uc.user_transformUserToUserAuthorizedForConfig( userAuthorizedInConfig));
            reloadCurrentUser();
        } catch (IntegrationException ex) {
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
        mbp.setExistingContent(userAuthorizedInConfig.getNotes());
        mbp.setNewMessageContent(formNoteText);
        mbp.setUser(getSessionBean().getSessUser());
        mbp.setIncludeCredentialSig(false);
        userAuthorizedInConfig.setNotes(sc.appendNoteBlock(mbp));
        try {
            userAuthorizedInConfig.setLastUpdatedTS(LocalDateTime.now());
            uc.user_appendNoteToUser(userAuthorizedInConfig, mbp);
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
    public String onUserRemoveCommitButtonChange() {
        UserCoordinator uc = getUserCoordinator();
        
        try{
            uc.user_deactivateUser(getSessionBean().getSessUser(), userAuthorizedInConfig);
            getFacesContext().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully removed user from active management", ""));
        } catch (AuthorizationException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Unable to deactivate due to Auth or DB error!", ""));
            
        }
        
        return "";
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
    
    /**
     * Listener for users to begin the UMAP invalidation process
     * @param uap 
     */
    public void onInvalidateUserAuthPeriodInit(UserMuniAuthPeriod uap){
        umapInConfig = uap;
        
    }
    
    /**
     * User request to submit invalidation of of a UMAP
     * @return 
     */
    public String onInvalidateUserMuniAuthPeriodCommit(){
        SystemCoordinator sc = getSystemCoordinator();
        UserCoordinator uc = getUserCoordinator();
        try {
            uc.auth_invalidateUserAuthPeriod(umapInConfig, getSessionBean().getSessUser(), formInvalidateRecordReason);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully invalidated auth period id" + umapInConfig.getUserMuniAuthPeriodID(), ""));
            MessageBuilderParams mbp = new MessageBuilderParams();
            mbp.setCred(getSessionBean().getSessUser().getKeyCard());
            mbp.setExistingContent(userAuthorizedInConfig.getNotes());
            mbp.setNewMessageContent(formInvalidateRecordReason);
            mbp.setUser(getSessionBean().getSessUser());
            
            uc.user_appendNoteToUser(userAuthorizedInConfig, mbp);
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
            umapInConfig = uc.auth_initializeUserMuniAuthPeriod_SECURITYCRITICAL(getSessionBean().getSessUser(), 
                                                            userAuthorizedInConfig, 
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
    public String onAuthPeriodNewCommit(){
        UserCoordinator uc = getUserCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        try {
            if(formUmapNotes != null && formUmapNotes.length() > 0){
                umapInConfig.setNotes(sc.formatAndAppendNote(getSessionBean().getSessUser(), 
                                                                formUmapNotes,
                                                                umapInConfig.getNotes()));
            }
            uc.auth_insertUserMuniAuthorizationPeriod_SECURITYCRITICAL(getSessionBean().getSessUser(), userAuthorizedInConfig, umapInConfig);
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
        return "";
    }
    
    
    
   
    
    
    /**
     * Listener for requests to jump to edit a Person record
     * @param p
     * @return 
     */
    public String onEditUserPersonRecordButtonChange(Person p){
        getSessionBean().setSessPersonQueued(userAuthorizedInConfig.getPerson());
        return "persons";
    }

    
    
    /**
     * Internal organ for refreshing the current UserAuthorized
     */
    private void reloadCurrentUser(){
        UserCoordinator uc = getUserCoordinator();
        try {
            userAuthorizedInConfig = uc.user_transformUserToUserAuthorizedForConfig(userAuthorizedInConfig);
            getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_INFO,
                               "Reloaded current user: " + userAuthorizedInConfig.getUsername(), ""));
        } catch (AuthorizationException | IntegrationException ex) {
            
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
            umapInConfig = ui.getUserMuniAuthPeriod(umapInConfig.getUserMuniAuthPeriodID());
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
            uc.user_updateUserPassword_SECURITYCRITICAL(userAuthorizedInConfig, freshPasswordCleartext);
//            getFacesContext().addMessage(null,
//                new FacesMessage(FacesMessage.SEVERITY_INFO, 
//                        "Password reset success! New password for " 
//                                + userAuthorizedInConfig.getUsername() 
//                                + " is now " + freshPasswordCleartext, ""));
          
         } catch (IntegrationException | AuthorizationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Password update error in DB; password unchanged", ""));
            
        }
        
    }
    
    
    /**
     * Logic container for liasing with the UserCoordinator for password reset
     */
    public void onResetUserPassword(){
        UserCoordinator uc = getUserCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        freshPasswordCleartext = uc.user_generateRandomPassword_SECURITYCRITICAL();
        try {
            uc.user_updateUserPassword_SECURITYCRITICAL(userAuthorizedInConfig, freshPasswordCleartext);
//            getFacesContext().addMessage(null,
//                new FacesMessage(FacesMessage.SEVERITY_INFO, 
//                        "Password reset success! New password for " 
//                                + userAuthorizedInConfig.getUsername() 
//                                + " is now " + freshPasswordCleartext, ""));
            // TODO: get this in the right place
//            formInvalidateRecordReason = "";
//            userAuthorizedInConfig.setNotes(sc.formatAndAppendNote(getSessionBean().getSessUser(), 
//                                    formInvalidateRecordReason, 
//                                    userAuthorizedInConfig.getNotes()));
            
            uc.user_forcePasswordReset(userAuthorizedInConfig);

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
    public UserAuthorizedForConfig getUserAuthorizedInConfig() {
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
    public void setUserAuthorizedInConfig(UserAuthorizedForConfig userAuthorizedInConfig) {
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
     * @return the userSelected
     */
    public boolean isUserSelected() {
        return userSelected;
    }

    /**
     * @param userSelected the userSelected to set
     */
    public void setUserSelected(boolean userSelected) {
        this.userSelected = userSelected;
    }

    /**
     * @return the pageModes
     */
    public List<PageModeEnum> getPageModes() {
        return pageModes;
    }

    /**
     * @param pageModes the pageModes to set
     */
    public void setPageModes(List<PageModeEnum> pageModes) {
        this.pageModes = pageModes;
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

   
}
