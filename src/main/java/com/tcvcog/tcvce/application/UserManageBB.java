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


import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.integration.UserIntegrator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Eric C. Darsow
 */
public class UserManageBB extends BackingBeanUtils implements Serializable{

    /**
     * Creates a new instance of UserUpdate
     */
    public UserManageBB() {
        
    }
    
    private User currentUser;
    
    private RoleType selectedRoleType;
    private List<RoleType> roleTypeCandidateList;
    
    private Municipality selectedMuni;
    private List<Municipality> muniCandidateList;

    
    @PostConstruct
    public void initBean(){
        UserCoordinator uc = getUserCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        roleTypeCandidateList = uc.getPermittedRoleTypes(getSessionBean().getSessionUser());
        try {
            muniCandidateList = sc.getPermittedMunicipalityList(getSessionBean().getSessionUser());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }
    
    public String commitUpdatesToUser(){
        UserIntegrator ui = getUserIntegrator();
        User u = new User();
        u.setUserID(currentUser.getUserID());
       
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
        return "userManage";
    }
 
    /**
     * @return the currentUser
     */
    public User getCurrentUser() {
        
        currentUser = getSessionBean().getUtilityUserToUpdate();
        return currentUser;
    }

    /**
     * @return the selectedRoleType
     */
    public RoleType getSelectedRoleType() {
        // BROKEN
        return selectedRoleType;
    }
  

    /**
     * Method not used
     * @param currentUser the currentUser to set
     */
    public void setCurrentUser(User currentUser) {
        
        this.currentUser = currentUser;
    }

    /**
     * @param selectedRoleType the selectedRoleType to set
     */
    public void setSelectedRoleType(RoleType selectedRoleType) {
        this.selectedRoleType = selectedRoleType;
    }

    
  
    /**
     * @return the roleTypeCandidateList
     */
    public List<RoleType> getRoleTypeCandidateList() {
        return roleTypeCandidateList;
    }

    /**
     * @param roleTypeCandidateList the roleTypeCandidateList to set
     */
    public void setRoleTypeCandidateList(List<RoleType> roleTypeCandidateList) {
        this.roleTypeCandidateList = roleTypeCandidateList;
    }

    /**
     * @return the selectedMuni
     */
    public Municipality getSelectedMuni() {
        return selectedMuni;
    }

    /**
     * @param selectedMuni the selectedMuni to set
     */
    public void setSelectedMuni(Municipality selectedMuni) {
        this.selectedMuni = selectedMuni;
    }

    /**
     * @return the muniCandidateList
     */
    public List<Municipality> getMuniCandidateList() {
        return muniCandidateList;
    }

    /**
     * @param muniCandidateList the muniCandidateList to set
     */
    public void setMuniCandidateList(List<Municipality> muniCandidateList) {
        this.muniCandidateList = muniCandidateList;
    }
    
    
    
}
