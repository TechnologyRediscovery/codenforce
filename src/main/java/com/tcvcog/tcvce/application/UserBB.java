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


import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
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
    private User currentUser;

//    @ManagedProperty(value="#{sessionBean}")
//    private SessionBean subclassSessionBean;
    
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

    

    /**
     * Creates a new instance of UserBB
     */
    public UserBB() {
    }
    
    @PostConstruct
    public void initBean(){
        currentUser = getSessionBean().getSessionUser();
    }
    

    public void updateUser(User u) {
        
        currentUser = u;

    }
    
    public void commitUpdates(ActionEvent ev){
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

    public void addUser(ActionEvent ev) {
        UserCoordinator uc = getUserCoordinator();
        System.out.println("UserBB.addUser");
        currentUser = uc.getUserSkeleton();
    }

    public void commitInsert(ActionEvent ev) {
        System.out.println("UserBB.commitInsert");
        UserCoordinator uc = getUserCoordinator();
        int newUserID;
        
        try {
            newUserID = uc.insertNewUser(currentUser);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully added user with id" + newUserID
                            + " to the system and this person can now login and get to work!", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to add user to system, my apologies",
                            "This is a system-level error that msut be corrected by an administrator"));
        }

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
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * @param currentUser the currentUser to set
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    
   
}
