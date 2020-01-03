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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.faces.application.FacesMessage;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class UserUpdateBB extends BackingBeanUtils implements Serializable{

    /**
     * Creates a new instance of UserUpdate
     */
    public UserUpdateBB() {
    }
    
    private User userToUpdate;
    
    private RoleType formRoleType;
    private RoleType[] roleTypeArray;
    private String formUsername;
    private String formNotes;

    
    public String commitUpdatesToUser(){
        UserIntegrator ui = getUserIntegrator();
        User u = new User();
        u.setUserID(userToUpdate.getUserID());
        u.setRoleType(formRoleType);
        u.setUsername(formUsername);
        u.setNotes(formNotes);
       
        try {
            ui.updateUser(u);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "User Update Successful!", ""));
            
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to update person", "This issue requires administrator attention, sorry"));
            
        }
        
        return "/userManage";
}
 
    /**
     * @return the userToUpdate
     */
    public User getUserToUpdate() {
        
        userToUpdate = getSessionBean().getUtilityUserToUpdate();
        return userToUpdate;
    }

    /**
     * @return the formRoleType
     */
    public RoleType getFormRoleType() {
        formRoleType = userToUpdate.getRoleType();
        return formRoleType;
    }

    /**
     * @return the formUsername
     */
    public String getFormUsername() {
        formUsername = userToUpdate.getUsername();
        return formUsername;
    }

    
    /**
     * @return the formNotes
     */
    public String getFormNotes() {
        formNotes = userToUpdate.getNotes();
        return formNotes;
    }

  
   

    /**
     * Method not used
     * @param userToUpdate the userToUpdate to set
     */
    public void setUserToUpdate(User userToUpdate) {
        
        this.userToUpdate = userToUpdate;
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
     * @param formNotes the formNotes to set
     */
    public void setFormNotes(String formNotes) {
        this.formNotes = formNotes;
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
    
    
    
}
