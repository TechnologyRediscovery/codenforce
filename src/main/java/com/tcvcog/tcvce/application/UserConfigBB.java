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
import com.tcvcog.tcvce.entities.PageModeEnum;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserMuniAuthPeriod;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.UserAuthorizedForConfig;
import com.tcvcog.tcvce.entities.search.QueryPerson;
import com.tcvcog.tcvce.entities.search.QueryPersonEnum;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;
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
    private String formNotes;
    private String formInvalidateRecordReason;
    
    private List<Person> userPersonList;
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
            userAuthorizedInConfig = uc.user_assembleUserAuthorizedForConfig(getSessionBean().getSessUser());
            userSelected = true;
            umapInConfig = userAuthorizedInConfig.getMyCredential().getGoverningAuthPeriod();
            userListForConfig = uc.user_auth_assembleUserListForConfig(getSessionBean().getSessUser());
            muniCandidateList = mc.getPermittedMunicipalityListForAdminMuniAssignment(getSessionBean().getSessUser());
            roleTypeCandidateList = uc.auth_getPermittedRoleTypesToGrant(getSessionBean().getSessUser());
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

    //check if current mode == Lookup
    public boolean getActiveViewMode() {
        // hard-wired on since there's always a property loaded
        return true;
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
                userAuthorizedInConfig = uc.user_transformUserToUserAuthorizedForConfig(getSessionBean().getSessUser(), u);
                userSelected = true;
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
              getFacesContext().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Loaded user list for configuration", ""));
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
            userAuthorizedInConfig = uc.user_assembleUserAuthorizedForConfig(new UserAuthorized(uc.user_getUserSkeleton(getSessionBean().getSessUser())));
        } catch (IntegrationException ex) {
            
        }
        System.out.println("UserConfigBB.createNewUser");
    }
    
    
     public void onModeUpdateInit(){
         // nothign to do here yet since the user is selected
     }
     
    
     public void onModeRemoveInit(){
       // nothing to do here yet but let panels reset themselves
    }
     
     
     
    
    
    
     /**
     * Listener for button clicks when user is ready to insert a new EventRule.
     * Delegates all work to internal method
     * @param ev
     */
    public void onUserInsertCommitButtonChange(ActionEvent ev) {
         System.out.println("UserBB.commitInsert");
        UserCoordinator uc = getUserCoordinator();
        String freshUserPswd = null;
        int freshUserID;
        User usr;
        
        try {
            freshUserID = uc.user_insertNewUser(userAuthorizedInConfig);
            if(freshUserID != 0){
                usr = uc.user_getUser(freshUserID);
                freshUserPswd = uc.user_generateRandomPassword_SECURITYCRITICAL();
                uc.user_updateUserPassword_SECURITYCRITICAL(usr, freshUserPswd);
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
        }//show successfully inserting message in p:messages box
    }

    /**
     * Listener for user requests to submit object updates;
     * Delegates all work to internal method
     * @param ev
     */
    public void onUserUpdateCommitButtonChange(ActionEvent ev) {
        UserCoordinator uc = getUserCoordinator();
        try {
            uc.user_updateUser(userAuthorizedInConfig, null, formUsername);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully udpated user", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not update user", ""));
            
        }
        //show successfully updating message in p:messages box
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully Updated User", ""));
        //reminder: set muniManageSample in faces-config.xml
        //return to muniManage_sample.xhtml page
        
    }
    
    

    /**
     * Listener for user requests to remove the currently selected ERA;
     * Delegates all work to internal, non-listener method.
     * @param ev
     */
    public void onUserRemoveCommitButtonChange(ActionEvent ev) {
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully removed user from active management", ""));
    }
    
    /**
     * Listener for user requests to abandon their update or insert changes
     * @param ev 
     */
    public void onDiscardChangesButton(ActionEvent ev){
        
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
    
    public void onInvalidateUserAuthPeriodInit(UserMuniAuthPeriod uap){
        umapInConfig = uap;
        
    }
    
    public void onInvalidateUserMuniAuthPeriodCommit(){
        UserCoordinator uc = getUserCoordinator();
        try {
            uc.auth_invalidateUserAuthPeriod(umapInConfig, getSessionBean().getSessUser(), formInvalidateRecordReason);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully invalidated auth period id" + umapInConfig.getUserMuniAuthPeriodID(), ""));
        } catch (IntegrationException | AuthorizationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }
        
    }

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
    
    public void onAuthPeriodNewCommit(){
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
            reloadCurrentUMAP();
        } catch (AuthorizationException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }
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
            userAuthorizedInConfig = uc.user_transformUserToUserAuthorizedForConfig(getSessionBean().getSessUser(), userAuthorizedInConfig);
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
     * Finalizes updates to Users with the UserCoordinator
     */
    public void onUserPersonLinkUpdate(){
        UserCoordinator uc = getUserCoordinator();
        try {
            uc.user_updateUser(userAuthorizedInConfig, selectedUserPerson, null);
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
    
    
    
    
    /**
     * Logic container for liasing with the UserCoordinator for password reset
     */
    public void onForceResetUserPassword(){
        UserCoordinator uc = getUserCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        freshPasswordCleartext = uc.user_generateRandomPassword_SECURITYCRITICAL();
        try {
            uc.user_updateUserPassword_SECURITYCRITICAL(userAuthorizedInConfig, freshPasswordCleartext);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Password reset success! New password for " 
                                + userAuthorizedInConfig.getUsername() 
                                + " is now " + freshPasswordCleartext, ""));
            formInvalidateRecordReason = "";
            userAuthorizedInConfig.setNotes(sc.formatAndAppendNote(getSessionBean().getSessUser(), 
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
     * Logic container for liasing with the UserCoordinator for password reset
     */
    public void onResetUserPassword(){
        UserCoordinator uc = getUserCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        freshPasswordCleartext = uc.user_generateRandomPassword_SECURITYCRITICAL();
        try {
            uc.user_updateUserPassword_SECURITYCRITICAL(userAuthorizedInConfig, freshPasswordCleartext);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Password reset success! New password for " 
                                + userAuthorizedInConfig.getUsername() 
                                + " is now " + freshPasswordCleartext, ""));
            formInvalidateRecordReason = "";
            userAuthorizedInConfig.setNotes(sc.formatAndAppendNote(getSessionBean().getSessUser(), 
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

   
}
