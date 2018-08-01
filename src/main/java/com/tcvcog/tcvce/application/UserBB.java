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


import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.integration.UserIntegrator;
import java.io.Serializable;
import java.time.ZoneId;
import java.util.Date;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

/**
 *
 * @author Eric C. Darsow
 */
@ManagedBean(name="userBB")
@ViewScoped
public class UserBB extends BackingBeanUtils implements Serializable {

    private ArrayList<User> userList;
    private User selectedUser;

//    @ManagedProperty(value="#{sessionBean}")
//    private SessionBean subclassSessionBean;
    
    private int formUserID;
    private RoleType formRoleType;
    private RoleType[] roleTypeArray;
    private String formUsername;
    private String formPassword;
    private int formMuniCode;
    private String formFName;
    private String formLName;
    private String formWorkTitle;
    private String formPhoneCell;
    private String formPhoneHome;
    private String formPhoneWork;
    private String formEmail;
    private String formAddress_street;
    private String formAddress_city;
    private String formAddress_zip;
    private String formAddress_state;
    private String formNotes;
    private Date formActivityStartDate;
    private Date formActivityStopDate;
    private boolean formAccessPermitted;
    

    /**
     * Creates a new instance of UserBB
     */
    public UserBB() {
    }

    public String updateUser() {
        
        getSessionBean().setUtilityUserToUpdate(selectedUser);

        return "userUpdate";
    }

    public String addUser() {
        System.out.println("UserBB.addUser");

        return "userAdd";
    }

    public String commitInsert() {
        System.out.println("UserBB.commitInsert");
        UserIntegrator ui = getUserIntegrator();

        User u = new User();
        u.setUserID(formUserID);
        u.setRoleType(formRoleType);
        u.setUsername(formUsername);
        u.setPassword(formPassword);
        u.setMuniCode(formMuniCode);
        u.setFName(formFName);
        u.setLName(formLName);
        u.setWorkTitle(formWorkTitle);
        u.setPhoneCell(formPhoneCell);
        u.setPhoneHome(formPhoneHome);
        u.setPhoneWork(formPhoneWork);
        u.setEmail(formEmail);
        u.setAddress_street(formAddress_street);
        u.setAddress_city(formAddress_city);
        u.setAddress_zip(formAddress_zip);
        u.setAddress_state(formAddress_state);
        u.setNotes(formNotes);
        u.setActivityStartDate(formActivityStartDate.toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());
        u.setActivityStopDate(formActivityStopDate.toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());
        u.setAccessPermitted(formAccessPermitted);

        try {
            ui.insertUser(u);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully added " + u.getFName()
                            + " to the system and this person can now login and get to work.", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to add user to system, my apologies",
                            "This is a system-level error that msut be corrected by an administrator"));
            return "";
        }

        return "userManage";

    }

    /**
     * @return the userList
     */
    public ArrayList<User> getUserList() {
        UserIntegrator ui = getUserIntegrator();
        try {
            userList = ui.getCompleteUserList();
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
     * @return the selectedUser
     */
    public User getSelectedUser() {
        return selectedUser;
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
     * @return the formMuniCode
     */
    public int getFormMuniCode() {
        return formMuniCode;
    }

    /**
     * @return the formFName
     */
    public String getFormFName() {
        return formFName;
    }

    /**
     * @return the formLName
     */
    public String getFormLName() {
        return formLName;
    }

    /**
     * @return the formWorkTitle
     */
    public String getFormWorkTitle() {
        return formWorkTitle;
    }

    /**
     * @return the formPhoneCell
     */
    public String getFormPhoneCell() {
        return formPhoneCell;
    }

    /**
     * @return the formPhoneHome
     */
    public String getFormPhoneHome() {
        return formPhoneHome;
    }

    /**
     * @return the formPhoneWork
     */
    public String getFormPhoneWork() {
        return formPhoneWork;
    }

    /**
     * @return the formEmail
     */
    public String getFormEmail() {
        return formEmail;
    }

    /**
     * @return the formAddress_street
     */
    public String getFormAddress_street() {
        return formAddress_street;
    }

    /**
     * @return the formAddress_city
     */
    public String getFormAddress_city() {
        return formAddress_city;
    }

    /**
     * @return the formAddress_zip
     */
    public String getFormAddress_zip() {
        return formAddress_zip;
    }

    /**
     * @return the formAddress_state
     */
    public String getFormAddress_state() {
        return formAddress_state;
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
     * @param selectedUser the selectedUser to set
     */
    public void setSelectedUser(User selectedUser) {
        this.selectedUser = selectedUser;
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
     * @param formMuniCode the formMuniCode to set
     */
    public void setFormMuniCode(int formMuniCode) {
        this.formMuniCode = formMuniCode;
    }

    /**
     * @param formFName the formFName to set
     */
    public void setFormFName(String formFName) {
        this.formFName = formFName;
    }

    /**
     * @param formLName the formLName to set
     */
    public void setFormLName(String formLName) {
        this.formLName = formLName;
    }

    /**
     * @param formWorkTitle the formWorkTitle to set
     */
    public void setFormWorkTitle(String formWorkTitle) {
        this.formWorkTitle = formWorkTitle;
    }

    /**
     * @param formPhoneCell the formPhoneCell to set
     */
    public void setFormPhoneCell(String formPhoneCell) {
        this.formPhoneCell = formPhoneCell;
    }

    /**
     * @param formPhoneHome the formPhoneHome to set
     */
    public void setFormPhoneHome(String formPhoneHome) {
        this.formPhoneHome = formPhoneHome;
    }

    /**
     * @param formPhoneWork the formPhoneWork to set
     */
    public void setFormPhoneWork(String formPhoneWork) {
        this.formPhoneWork = formPhoneWork;
    }

    /**
     * @param formEmail the formEmail to set
     */
    public void setFormEmail(String formEmail) {
        this.formEmail = formEmail;
    }

    /**
     * @param formAddress_street the formAddress_street to set
     */
    public void setFormAddress_street(String formAddress_street) {
        this.formAddress_street = formAddress_street;
    }

    /**
     * @param formAddress_city the formAddress_city to set
     */
    public void setFormAddress_city(String formAddress_city) {
        this.formAddress_city = formAddress_city;
    }

    /**
     * @param formAddress_zip the formAddress_zip to set
     */
    public void setFormAddress_zip(String formAddress_zip) {
        this.formAddress_zip = formAddress_zip;
    }

    /**
     * @param formAddress_state the formAddress_state to set
     */
    public void setFormAddress_state(String formAddress_state) {
        this.formAddress_state = formAddress_state;
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

    
   
}
