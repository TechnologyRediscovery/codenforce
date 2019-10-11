/*
 * Copyright (C) 2018 Turtle Creek Valley
Council of Governments, PA
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


import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthPeriod;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.search.QueryPerson;
import com.tcvcog.tcvce.entities.search.QueryPersonEnum;
import com.tcvcog.tcvce.integration.UserIntegrator;
import java.io.Serializable;
import java.time.ZoneId;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Eric C. Darsow
 */

public class UserBB extends BackingBeanUtils implements Serializable {

    private List<User> userList;
    private UserAuthorized currentUser;
    
    private UserAuthPeriod currentUserAuthPeriod;
    
    private int formUserID;
    private RoleType formRoleType;
    private RoleType[] roleTypeArray;
    private String formUsername;
    
    private String formPassword;
    
    private Municipality formMuni;
    
    private String formNotes;
    private Date formActivityStartDate;
    private Date formActivityStopDate;
    private boolean formAccessPermitted;
    
    private boolean formIsEnfOfficial;
    private String formBadgeNum;
    private String formOriNum;
    
    private Person formUserPerson;
    
    private List<Person> userPersonList;
    private Person selectedUserPerson;
    

    /**
     * Creates a new instance of UserBB
     */
    public UserBB() {
    }
    
    @PostConstruct
    public void initBean(){
        SearchCoordinator sc = getSearchCoordinator();
        currentUser = getSessionBean().getSessionUser();
        
        // user our fancy specialized query to get all Persons who are delcared to 
        // be user types
        QueryPerson qp = sc.assembleQueryPerson(QueryPersonEnum.USER_PERSONS, currentUser, null, null );
        try {
            qp = sc.runQuery(qp);
            userPersonList = qp.getResults();
        } catch (AuthorizationException | IntegrationException ex) {
            System.out.println(ex);
        }
        
    }

    public void initializeNewAuthPeriod(User u){
        
        
    }
    
    /**
     * Pass through method called when user settings dialog is displayed
     * @param ev 
     */
    public void initiateUserUpdates(ActionEvent ev){
        currentUser = getSessionBean().getSessionUser();
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
    
    public void commitPasswordUpdates(ActionEvent ev){
        
        UserCoordinator uc = getUserCoordinator();
        try {
            uc.updateUserPassword(currentUser, formPassword);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully udpated your password to --> " + formPassword 
                                    + " <-- Please write this down in a safe place; "
                                    + "If you lose it, you'll have to make a new one.", ""));
            formPassword = "";
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not update password in DB", ""));
            
        } catch (AuthorizationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Authorization error on password update", ""));
        }
        
        
        
        
        
    }
    
    
    
    public void commitUserUpdates(ActionEvent ev){
    }


    
    /**
     * @return the userList
     */
    public List<User> getUserList() {
        UserIntegrator ui = getUserIntegrator();
        try {
            userList = ui.getCompleteActiveUserList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Unable to acquire list of users",
                            "This is a system-level error that msut be corrected by an administrator"));
        }
        return userList;
    }

    

    /**
     * @return the formRoleType
     */
    public RoleType getFormRoleType() {
        return formRoleType;
    }

    /**
     * @return the formUsername
     */
    public String getFormUsername() {
        return formUsername;
    }

    /**
     * @return the formPassword
     */
    public String getFormPassword() {
        return formPassword;
    }

   
  
    /**
     * @return the formNotes
     */
    public String getFormNotes() {
        return formNotes;
    }

    /**
     * @return the formActivityStartDate
     */
    public Date getFormActivityStartDate() {
        return formActivityStartDate;
    }

    /**
     * @return the formActivityStopDate
     */
    public Date getFormActivityStopDate() {
        return formActivityStopDate;
    }

    /**
     * @return the formAccessPermitted
     */
    public boolean isFormAccessPermitted() {
        formAccessPermitted = true;
        return formAccessPermitted;
    }

    /**
     * @param userList the userList to set
     */
    public void setUserList(ArrayList<User> userList) {
        this.userList = userList;
    }

   
    /**
     * @param formRoleType the formRoleType to set
     */
    public void setFormRoleType(RoleType formRoleType) {
        this.formRoleType = formRoleType;
    }

    /**
     * @param formUsername the formUsername to set
     */
    public void setFormUsername(String formUsername) {
        this.formUsername = formUsername;
    }

    /**
     * @param formPassword the formPassword to set
     */
    public void setFormPassword(String formPassword) {
        this.formPassword = formPassword;
    }

   
    /**
     * @param formNotes the formNotes to set
     */
    public void setFormNotes(String formNotes) {
        this.formNotes = formNotes;
    }

    /**
     * @param formActivityStartDate the formActivityStartDate to set
     */
    public void setFormActivityStartDate(Date formActivityStartDate) {
        this.formActivityStartDate = formActivityStartDate;
    }

    /**
     * @param formActivityStopDate the formActivityStopDate to set
     */
    public void setFormActivityStopDate(Date formActivityStopDate) {
        this.formActivityStopDate = formActivityStopDate;
    }

    /**
     * @param formAccessPermitted the formAccessPermitted to set
     */
    public void setFormAccessPermitted(boolean formAccessPermitted) {
        this.formAccessPermitted = formAccessPermitted;
    }

    /**
     * @return the roleTypeArray
     */
    public RoleType[] getRoleTypeArray() {
        roleTypeArray = RoleType.values();
        return roleTypeArray;
    }

    /**
     * @param roleTypeArray the roleTypeArray to set
     */
    public void setRoleTypeArray(RoleType[] roleTypeArray) {
        this.roleTypeArray = roleTypeArray;
    }

    /**
     * @return the formUserID
     */
    public int getFormUserID() {
        return formUserID;
    }

    /**
     * @param formUserID the formUserID to set
     */
    public void setFormUserID(int formUserID) {
        this.formUserID = formUserID;
    }

    /**
     * @return the formIsEnfOfficial
     */
    public boolean isFormIsEnfOfficial() {
        return formIsEnfOfficial;
    }

    /**
     * @param formIsEnfOfficial the formIsEnfOfficial to set
     */
    public void setFormIsEnfOfficial(boolean formIsEnfOfficial) {
        this.formIsEnfOfficial = formIsEnfOfficial;
    }

    /**
     * @return the formBadgeNum
     */
    public String getFormBadgeNum() {
        return formBadgeNum;
    }

    /**
     * @return the formOriNum
     */
    public String getFormOriNum() {
        return formOriNum;
    }

    /**
     * @param formBadgeNum the formBadgeNum to set
     */
    public void setFormBadgeNum(String formBadgeNum) {
        this.formBadgeNum = formBadgeNum;
    }

    /**
     * @param formOriNum the formOriNum to set
     */
    public void setFormOriNum(String formOriNum) {
        this.formOriNum = formOriNum;
    }

    /**
     * @return the formUserPerson
     */
    public Person getFormUserPerson() {
        return formUserPerson;
    }

    /**
     * @param formUserPerson the formUserPerson to set
     */
    public void setFormUserPerson(Person formUserPerson) {
        this.formUserPerson = formUserPerson;
    }

    /**
     * @return the formMuni
     */
    public Municipality getFormMuni() {
        return formMuni;
    }

    /**
     * @param formMuni the formMuni to set
     */
    public void setFormMuni(Municipality formMuni) {
        this.formMuni = formMuni;
    }

    /**
     * @return the currentUser
     */
    public UserAuthorized getCurrentUser() {
        return currentUser;
    }

    /**
     * @param currentUser the currentUser to set
     */
    public void setCurrentUser(UserAuthorized currentUser) {
        this.currentUser = currentUser;
    }

   

    /**
     * @return the currentUserAuthPeriod
     */
    public UserAuthPeriod getCurrentUserAuthPeriod() {
        return currentUserAuthPeriod;
    }

    /**
     * @param currentUserAuthPeriod the currentUserAuthPeriod to set
     */
    public void setCurrentUserAuthPeriod(UserAuthPeriod currentUserAuthPeriod) {
        this.currentUserAuthPeriod = currentUserAuthPeriod;
    }

    /**
     * @return the userPersonList
     */
    public List<Person> getUserPersonList() {
        return userPersonList;
    }

    /**
     * @param userPersonList the userPersonList to set
     */
    public void setUserPersonList(List<Person> userPersonList) {
        this.userPersonList = userPersonList;
    }

    /**
     * @return the selectedUserPerson
     */
    public Person getSelectedUserPerson() {
        return selectedUserPerson;
    }

    /**
     * @param selectedUserPerson the selectedUserPerson to set
     */
    public void setSelectedUserPerson(Person selectedUserPerson) {
        this.selectedUserPerson = selectedUserPerson;
    }

    
   
}
