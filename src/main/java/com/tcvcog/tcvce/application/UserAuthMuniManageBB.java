/*
 * Copyright (C) 2019 Eric C. Darsow
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
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.integration.UserIntegrator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Dominic Pimpinella
 */
public class UserAuthMuniManageBB extends BackingBeanUtils implements Serializable {
    
    private List<Municipality> muniList;    
    private List<Municipality> authMuniList;
    private List<Municipality> unauthorizedMuniList;    
    private List<User> userList;
    
    private List<Municipality> selectedMunis;
    private Municipality selectedMuni;
    private User selectedUser;

    public UserAuthMuniManageBB() {
        
    }
    
   
    
    public void setMuniList(List<Municipality> muniList) {        
        this.muniList = muniList;
    }
    
    public List<Municipality> getAuthMuniList() {
        return authMuniList;
    }

    public void setAuthMuniList(List<Municipality> authMuniList) {
        this.authMuniList = authMuniList;
    }
    
    public List<Municipality> getUnauthorizedMuniList() {
        if (selectedUser == null) {
            return unauthorizedMuniList;
        }
        else {
            try {
                UserCoordinator uc = getUserCoordinator();            
                unauthorizedMuniList = uc.getUnauthorizedMunis(selectedUser);
            } catch (IntegrationException ex) {
                System.out.println("UserAuthMuniManageBB.getUnauthorizedMuniList | " 
                        + ex.toString());
            }            
        }
        return unauthorizedMuniList;
    }

    public void setUnauthorizedMuniList(List<Municipality> unauthorizedMuniList) {
        this.unauthorizedMuniList = unauthorizedMuniList;
    }

    public List<User> getUserList() {
        UserIntegrator ui = getUserIntegrator();
        try {
            if (userList == null) {
                userList = ui.getCompleteActiveUserList();
            }
        } catch (IntegrationException ex) {
            System.out.println("UserAuthMuniManageBB.getUserList | " + ex.toString());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to acquire list of users.",
                            "This is a system-level error that must be corrected by an "
                                    + "administrator."));
        }
        return userList;
    }
    
    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    public List<Municipality> getSelectedMunis() {
        return selectedMunis;
    }

    public void setSelectedMunis(List<Municipality> selectedMunis) {
        this.selectedMunis = selectedMunis;
    }
    
    public Municipality getSelectedMuni() {
        return selectedMuni;
    }
    
    public void setSelectedMuni(Municipality selectedMuni) {
        this.selectedMuni = selectedMuni;
    }    
    
    public User getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(User selectedUser) {
        this.selectedUser = selectedUser;
    }
    
    /** 
     * This method is called whenever the user selects a username from the list of users. Whenever 
     * it is called, it replaces the current authMuniList and also creates the unauthorizedMuniList
     * for the selected user.
     */
    public void onSelectedUserChange() {
        System.out.println("UserAuthMuniManageBB.onSelectedUserChange |" + selectedUser.getUserID());
        clearAuthMuniList();
        clearUnauthorizedMuniList();

        try {
            unauthorizedMuniList = getUnauthorizedMuniList();
            UserIntegrator ui = getUserIntegrator();
            authMuniList = ui.getUserAuthMunis(selectedUser.getUserID(), this);
        } catch(IntegrationException ex){
            System.out.println("UserAuthMuniManageBB.onSelectedUserChange | " + ex.toString());
        }
        selectedMunis = new ArrayList<>();            
    }

    /** 
     * This method adds the selected municipality to a list of municipalities that will be mapped 
     * to the selected user when updateAuthMunis() is called.  
     * @return An empty String, which refreshes the page.
     */
    public String addAuthMuni() {
        if(selectedMuni == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a municipality",""));
        }
        else if (checkForDuplicateMuni(selectedMuni)){
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Cannot add the same municipality more than once, please make a "
                                    + "different selection.",""));
        } 
        else {
            System.out.println("UserAuthMuniManageBB.addAuthMuni | " + selectedUser.getUserID() + " & " 
                + selectedMuni.getMuniName());
             selectedMunis.add(selectedMuni);
        }
        return "";
    }
    
    /**
     * Updates the loginmuni data table with the list of selected municipalities corresponding to 
     * the selected user. Also clears all selections and lists of municipalities, so that the user
     * can make another selection to edit user-municipality mappings.
     * @return An empty String, which refreshes the page.
     */
    public String updateAuthMunis() {
        
        if(selectedMuni == null || selectedMunis.isEmpty() || selectedUser == null){
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a user and add one or more municipalities.",""));
        }
        else {
            System.out.println("UserAuthMuniManageBB.updateAuthMunis | " + selectedUser.getUserID() 
                    + " & " + selectedMunis);
            UserIntegrator ui = getUserIntegrator();
            try {
                ui.setUserAuthMunis(selectedUser, selectedMunis);
            }
            catch (IntegrationException ex) {

            }
            UserCoordinator uc = getUserCoordinator();

            for(Municipality m:selectedMunis){          
            getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                m.getMuniName() + " mapped to " + selectedUser.getUsername() + ".",
                                ""));
            }
            selectedMunis.clear();
            selectedMuni = null;
            selectedUser = null;
            authMuniList = null;
            unauthorizedMuniList = null;
        }        
        return "";
    }
    
    /**
     * Removes a municipality from the selectedMunis list.
     * @param muni
     * @return  An empty String, which refreshes the page.
     */
    public String removeSelectedMuni(Municipality muni) {
        selectedMunis.remove(muni);        
        return "";
    }
    
    /**
     * Removes user-municipality mapping for a given user and municipality. Updates authMuniList and
     * unauthorizedMuniList.
     * @param muni - a municipality
     * @return An empty String, which refreshes the page.
     */
    public String removeAuthMuni(Municipality muni){
        UserIntegrator ui = getUserIntegrator();        
        try {
            ui.deleteUserAuthMuni(selectedUser, muni);
        }
        catch(IntegrationException ex){
            System.out.println("UserAuthMuniManageBB.removeAuthMuni | " + ex);
        }
        getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Removed " + muni.getMuniName() + " from " + selectedUser.getUsername() 
                                    + ".", ""));
        try {
            authMuniList = ui.getUserAuthMunis(selectedUser.getUserID(), this);
        }
        catch (IntegrationException ex){
             System.out.println("UserAuthMuniManageBB.removeAuthMuni | " + ex);
        }
        unauthorizedMuniList = getUnauthorizedMuniList();
        return "";
    }
    
    
    /**
     * Checks for duplicates in the selectedMunis list in order to avoid SQLExceptions when adding
     * user-municipality mappings to the loginmuni table in the database.
     * @param muni
     * @return A boolean indicating the presence of duplicates in the list.
     */
    public boolean checkForDuplicateMuni (Municipality muni){
        boolean duplicate = false;
        for (Municipality m:selectedMunis){
            if(m.equals(muni)){
                duplicate = true;
            } 
        }
        return duplicate;
    }
    
    /**
     * The following methods are utility methods used to clear selections and lists.
     */
    public void clearAuthMuniList() {
        authMuniList = null;
    }
    
    public void clearUnauthorizedMuniList(){
        unauthorizedMuniList = null;
    }
    
    public void clearSelectedMuni(){
        selectedMuni = null;
    }
    
}
