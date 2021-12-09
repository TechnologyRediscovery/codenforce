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


import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.PersonDataHeavy;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.util.Constants;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 * Backing bean for person info page 
 * 
 * @author Ellen Bascomb of apartment 31Y
 */
public class PersonInfoBB extends BackingBeanUtils{

    private PersonDataHeavy currPerson;
    
    
    private String formNotes;
    
    // temp storage field values for writing old values to notes
    protected String fieldNamePrevious;
    
    
    /**
     * Creates a new instance of PersonBB
     */
    public PersonInfoBB() {
      
    }
    
    @PostConstruct
    public void initBean(){
       PersonCoordinator pc = getPersonCoordinator();
       
       if(getSessionBean().getSessPersonQueued() != null){
           try {
               currPerson = pc.assemblePersonDataHeavy(getSessionBean().getSessPersonQueued(),
                       getSessionBean().getSessUser().getKeyCard());
           } catch (IntegrationException | BObStatusException ex) {
               System.out.println(ex);
           }
             getSessionBean().setSessPerson(currPerson);
            getSessionBean().setSessPersonQueued(null);
       } else {
            currPerson = (getSessionBean().getSessPerson());
       }
       
        
    }
    
    
    /**
     * Listener for user requests to rename the current person
     * @param ev 
     */
    public void onPersonNameEditInit(ActionEvent ev){
        fieldNamePrevious = currPerson.getName();
    }
    
    /**
     * Listener for user requests to finalize a person name edit
     * @param ev 
     */
    public void onPersonNameEditCommit(ActionEvent ev){
        
        
    }
    
    /**
     * Listener method for person updates
     * Writes field dump to notes
     * 
     * @param ev 
     */
    public void personEditCommit(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
        SystemCoordinator sc = getSystemCoordinator();

        try {
//            pc.humanEdit(workingPerson, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_INFO, 
                     "Edits of Person saved to database!", ""));
            // with a successful update, write field dump of previous values to person notes
//            pc.addNotesToPerson(workingPerson, getSessionBean().getSessUser(), fieldDump);
            // refresh our current person
//            currPerson = pc.assemblePersonDataHeavy(pc.getPerson(workingPerson.getHumanID()), getSessionBean().getSessUser().getMyCredential());
            sc.logObjectView(getSessionBean().getSessUser(), currPerson);
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                     "Edits failed on person due to a database bug!", ""));
        }
    }
    
    
    public void personCreateInit(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
//        workingPerson = pc.createPersonSkeleton(getSessionBean().getSessUser().getMyCredential().getGoverningAuthPeriod().getMuni());
    }
    
    /**
     * Action listener for creation of new person objectgs
     * @return  
     */
    public String personCreateCommit(){
        PersonCoordinator pc = getPersonCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
    
         
        return "personSearch";
    }
    
    
    
    public void attachNoteToPerson(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();

        try {
            pc.addNotesToPerson(currPerson, getSessionBean().getSessUser(), getFormNotes());
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO
                    , "Done: Notes added to person ID:" + currPerson.getHumanID(),"" ));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR
                    , "Unable to update notes, sorry!"
                    , getResourceBundle(Constants.MESSAGE_TEXT).getString("systemLevelError")));
        } catch (BObStatusException ex) {
            System.out.println();
        }
    }
    
  
    
    /**
     * @return the currPerson
     */
    public PersonDataHeavy getCurrPerson() {
        return currPerson;
    }

    /**
     * @param currPerson the currPerson to set
     */
    public void setCurrPerson(PersonDataHeavy currPerson) {
        this.currPerson = currPerson;
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
     * @return the fieldNamePrevious
     */
    public String getFieldNamePrevious() {
        return fieldNamePrevious;
    }

    /**
     * @param fieldNamePrevious the fieldNamePrevious to set
     */
    public void setFieldNamePrevious(String fieldNamePrevious) {
        this.fieldNamePrevious = fieldNamePrevious;
    }
    
    
}
