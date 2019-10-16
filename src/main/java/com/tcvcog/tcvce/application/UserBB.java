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
import com.tcvcog.tcvce.entities.UserMuniAuthPeriod;
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

    private UserAuthorized currentUser;
   
    
    private String formUsername;
    private String formPassword;
    
    private String formNotes;
    
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
        userPersonList = new ArrayList<>();
        
        // user our fancy specialized query to get all Persons who are delcared to 
        // be user types
//        QueryPerson qp = sc.assembleQueryPerson(QueryPersonEnum.USER_PERSONS, currentUser, null, null );
//        try {
//            qp = sc.runQuery(qp);
//            userPersonList = qp.getResults();
//        } catch (AuthorizationException | IntegrationException ex) {
//            System.out.println(ex);
//        }
        
    }

    

    public void updateUser(User u) {
        
        currentUser = u;

    }
    
    public void credentializeUserMuniAuthPeriod(UserMuniAuthPeriod umap){
        // TODO: finish me!
        
        
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
