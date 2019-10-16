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

import com.tcvcog.tcvce.coordinators.MuniCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserMuniAuthPeriod;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.UserConfigReady;
import com.tcvcog.tcvce.entities.search.QueryPerson;
import com.tcvcog.tcvce.entities.search.QueryPersonEnum;
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

    /**
     * Creates a new instance of userConfig
     */
    public UserConfigBB() {
    }
    
    private UserAuthorized currentUser;
    private String freshPasswordCleartext;
    
    private UserMuniAuthPeriod currentUMAP;
    private List<UserMuniAuthPeriod> umapList;
    
    private List<UserAuthorized> userList;
    
    private RoleType selectedRoleType;
    private List<RoleType> roleTypeCandidateList;
    
    private Municipality selectedMuni;
    private List<Municipality> muniCandidateList;
    
     private int formUserID;
    private RoleType formRoleType;
    private RoleType[] roleTypeArray;
    
    private String formUsername;
    private Municipality formMuni;
    private Person formUserPerson;
    private String formInvalidateRecordReason;
    
    private List<Person> userPersonList;
    private Person selectedUserPerson;
    

    
    @PostConstruct
    public void initBean(){
        UserCoordinator uc = getUserCoordinator();
        setSelectedMuni(getSessionBean().getSessionMuni());
        SearchCoordinator searchCoord = getSearchCoordinator();
        MuniCoordinator mc = getMuniCoordinator();
        
        try {
            currentUser = getSessionBean().getSessionUser();
            currentUMAP = currentUser.getMyCredential().getGoverningAuthPeriod();
            userList = (uc.getUserAuthorizedList(getSessionBean().getSessionMuni()));
            muniCandidateList = mc.getPermittedMunicipalityListForAdminMuniAssignment(getSessionBean().getSessionUser());
            roleTypeCandidateList = uc.getPermittedRoleTypesToGrant(getSessionBean().getSessionUser());
        } catch (IntegrationException | AuthorizationException ex) {
            System.out.println(ex);
        }
        
         // user our fancy specialized query to get all Persons who are delcared to 
        // be user types
        QueryPerson qp = searchCoord.assembleQueryPerson(QueryPersonEnum.USER_PERSONS, getSessionBean().getSessionUser(), null, null );
        try {
            qp = searchCoord.runQuery(qp);
            userPersonList = qp.getResults();
        } catch (AuthorizationException | IntegrationException ex) {
            System.out.println(ex);
        }
    }
    
    public void initiateCreateNewUser(){
        UserCoordinator uc = getUserCoordinator();
        currentUser = uc.getUserSkeleton(getSessionBean().getSessionUser());
    }
    
    public void initiateInvalidateUserAuthPeriod(UserAuthorized u, UserMuniAuthPeriod uap){
        currentUMAP = uap;
        currentUser = u;
    }
    
    public void invalidateAuthPeriod(UserMuniAuthPeriod uap){
        UserCoordinator uc = getUserCoordinator();
        try {
            uc.invalidateUserAuthPeriod(uap, getSessionBean().getSessionUser(), formInvalidateRecordReason);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully invalidated auth period id" + currentUMAP.getUserAuthPeriodID(), ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        } catch (AuthorizationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
            
        }
        
    }
    
    public void initiateViewAddAuthPeriods(UserAuthorized ua){
        UserCoordinator uc = getUserCoordinator();
        currentUser = ua;
        umapList = currentUser.getMuniAuthPeriodsMap().get(currentUser.getMyCredential().getGoverningAuthPeriod().getMuni()); 
        System.out.println("UserConfigBB.initiateViewAddAuthPeriods | UMAP list size: " + umapList.size());
        if(umapList != null){
            currentUMAP = umapList.get(0);  
        }
    }
    
    public void commitNewAuthPeriod(){
        UserCoordinator uc = getUserCoordinator();
        try {
            uc.insertNewUserAuthorizationPeriod(getSessionBean().getSessionUser(), currentUser, currentUMAP);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully added new auth period!", ""));
        } catch (AuthorizationException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }
    }
    
     public void initiateUserUpdates(UserAuthorized usr){
        currentUser = usr;
    }
    
    public String editUserPersonRecord(){
        getSessionBean().setSessionPerson(currentUser.getPerson());
        return "persons";
    }

    public void updateUser(UserAuthorized u) {
        
        currentUser = u;

    }
    
    
    public void commitUsernameUpdates(ActionEvent ev){
        UserCoordinator uc = getUserCoordinator();
        try {
            uc.updateUser(currentUser);
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
            freshUserID = uc.insertNewUser(currentUser);
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
            uc.updateUser(currentUser);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully udpated your person link", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not update person link, sorry!", ""));
        }
    }
    
    
    
    /**
     *
     */
    public void commitUpdatesToUser(){
        UserCoordinator uc = getUserCoordinator();
       
        try {
            uc.updateUser(currentUser);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "User Update Successful!", ""));
            
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to update person", "This issue requires administrator attention, sorry"));
        }
        
    }
    
    public void commitUserCreation(){
        UserCoordinator uc = getUserCoordinator();
        int freshUserID;
        try {
            freshUserID = uc.insertNewUser(currentUser);
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
            uc.updateUserPassword(currentUser, freshPasswordCleartext);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Password reset success! New password for " 
                                + currentUser.getUsername() 
                                + " is now " + freshPasswordCleartext, ""));
            formInvalidateRecordReason = "";
            currentUser.setNotes(   sc.formatAndAppendNote(getSessionBean().getSessionUser(), 
                                    formInvalidateRecordReason, 
                                    currentUser.getNotes()));
            uc.updateUser(currentUser);
        } catch (IntegrationException | AuthorizationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Password update error in DB; password unchanged", ""));
            
        }
        
    }

   
    /**
     * @return the currentUser
     */
    public UserAuthorized getCurrentUser() {
        return currentUser;
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
     * @param currentUser the currentUser to set
     */
    public void setCurrentUser(UserAuthorized currentUser) {
        this.currentUser = currentUser;
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
     * @return the currentUMAP
     */
    public UserMuniAuthPeriod getCurrentUMAP() {
        return currentUMAP;
    }

    /**
     * @param currentUMAP the currentUMAP to set
     */
    public void setCurrentUMAP(UserMuniAuthPeriod currentUMAP) {
        this.currentUMAP = currentUMAP;
    }

    /**
     * @return the formUserID
     */
    public int getFormUserID() {
        return formUserID;
    }

    /**
     * @return the formRoleType
     */
    public RoleType getFormRoleType() {
        return formRoleType;
    }

    /**
     * @return the roleTypeArray
     */
    public RoleType[] getRoleTypeArray() {
        return roleTypeArray;
    }

    /**
     * @return the formUsername
     */
    public String getFormUsername() {
        return formUsername;
    }


    /**
     * @return the formMuni
     */
    public Municipality getFormMuni() {
        return formMuni;
    }

    /**
     * @param formUserID the formUserID to set
     */
    public void setFormUserID(int formUserID) {
        this.formUserID = formUserID;
    }

    /**
     * @param formRoleType the formRoleType to set
     */
    public void setFormRoleType(RoleType formRoleType) {
        this.formRoleType = formRoleType;
    }

    /**
     * @param roleTypeArray the roleTypeArray to set
     */
    public void setRoleTypeArray(RoleType[] roleTypeArray) {
        this.roleTypeArray = roleTypeArray;
    }

    /**
     * @param formUsername the formUsername to set
     */
    public void setFormUsername(String formUsername) {
        this.formUsername = formUsername;
    }

    /**
     * @param formMuni the formMuni to set
     */
    public void setFormMuni(Municipality formMuni) {
        this.formMuni = formMuni;
    }

    /**
     * @return the formUserPerson
     */
    public Person getFormUserPerson() {
        return formUserPerson;
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
     * @param formUserPerson the formUserPerson to set
     */
    public void setFormUserPerson(Person formUserPerson) {
        this.formUserPerson = formUserPerson;
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
     * @return the userList
     */
    public List<UserAuthorized> getUserList() {
        return userList;
    }

    /**
     * @param userList the userList to set
     */
    public void setUserList(List<UserAuthorized> userList) {
        this.userList = userList;
    }

    /**
     * @return the umapList
     */
    public List<UserMuniAuthPeriod> getUmapList() {
        return umapList;
    }

    /**
     * @param umapList the umapList to set
     */
    public void setUmapList(List<UserMuniAuthPeriod> umapList) {
        this.umapList = umapList;
    }
    
}
