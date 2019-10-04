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

import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthorizationPeriod;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.integration.UserIntegrator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

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
    
    private User currentUser;
    private UserAuthorizationPeriod currentUAP;
    private String freshPasswordCleartext;
    
    private List<UserAuthorized> userList;
    
    private RoleType selectedRoleType;
    private List<RoleType> roleTypeCandidateList;
    
    private Municipality selectedMuni;
    private List<Municipality> muniCandidateList;

    
    @PostConstruct
    public void initBean(){
        UserCoordinator uc = getUserCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        setSelectedMuni(getSessionBean().getSessionMuni());
        roleTypeCandidateList = uc.getPermittedRoleTypesToGrant(getSessionBean().getSessionUser());
        try {
            muniCandidateList = sc.getPermittedMunicipalityList(getSessionBean().getSessionUser());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
    }
    
    public void initiateCreateNewUser(){
        UserCoordinator uc = getUserCoordinator();
        currentUser = (uc.getUserSkeleton());
        
    }
    
    public void initiateCreateNewAuthPeriod(){
        UserCoordinator uc = getUserCoordinator();
        currentUAP = uc.initializeNewAuthPeriod(getSessionBean().getSessionUser(), currentUser, selectedMuni);
        
        
    }
    
    public void commitNewAuthPeriod(){
        UserCoordinator uc = getUserCoordinator();
        
        
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
        freshPasswordCleartext = uc.generateRandomPassword();
        try {
            uc.updateUserPassword(currentUser, freshPasswordCleartext);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Password update success! New password for " 
                                + currentUser.getUsername() 
                                + " is now " + freshPasswordCleartext, ""));
        } catch (IntegrationException | AuthorizationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Password update error in DB; password unchanged", ""));
            
        }
        
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
     * @return the currentUser
     */
    public User getCurrentUser() {
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
     * @return the currentUAP
     */
    public UserAuthorizationPeriod getCurrentUAP() {
        return currentUAP;
    }

    /**
     * @param currentUAP the currentUAP to set
     */
    public void setCurrentUAP(UserAuthorizationPeriod currentUAP) {
        this.currentUAP = currentUAP;
    }
    
}
