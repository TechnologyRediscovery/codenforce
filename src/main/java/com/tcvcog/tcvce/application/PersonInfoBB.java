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
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonDataHeavy;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Ellen Bascomb
 */
public class PersonInfoBB extends BackingBeanUtils{

    private PersonDataHeavy currPerson;
    private Person workingPerson;
    private boolean connectToActiveProperty;
    
    private String fieldDump;
    
    private String formNotes;
    
    /**
     * Creates a new instance of PersonBB
     */
    public PersonInfoBB() {
      
    }
    
    @PostConstruct
    public void initBean(){
       PersonCoordinator pc = getPersonCoordinator();
       
       if(getSessionBean().getSessPersonQueued() != null){
            currPerson = pc.assemblePersonDataHeavy(getSessionBean().getSessPersonQueued(), 
                    getSessionBean().getSessUser().getKeyCard());
             getSessionBean().setSessPerson(currPerson);
            getSessionBean().setSessPersonQueued(null);
       } else {
            currPerson = (getSessionBean().getSessPerson());
       }
       
       workingPerson = currPerson;
        
    }
    
    
    public void personEditInit(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
        // keep a stashed copy of our oroginal field values to write to notes
        // on a succesful update
        fieldDump = pc.dumpPerson(currPerson);
        workingPerson = currPerson;
        
    }
    
    /**
     * Listener method for person updates
     * Writes field dump to notes
     * 
     * @param ev 
     */
    public void personEditCommit(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();

        try {
            pc.personEdit(workingPerson, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_INFO, 
                     "Edits of Person saved to database!", ""));
            // with a successful update, write field dump of previous values to person notes
            pc.addNotesToPerson(workingPerson, getSessionBean().getSessUser(), fieldDump);
            // refresh our current person
            currPerson = pc.assemblePersonDataHeavy(pc.getPerson(workingPerson.getPersonID()), getSessionBean().getSessUser().getMyCredential());
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                     "Edits failed on person due to a database bug!", ""));
        }
    }
    
    
     public String viewPersonAssociatedProperty(Property p){
         getSessionBean().setSessPropertyQueued(p);
        return "propertyInfo";
    }
    
    
    public void personCreateInit(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
        workingPerson = pc.personCreateMakeSkeleton(getSessionBean().getSessUser().getMyCredential().getGoverningAuthPeriod().getMuni());
    }
    
    /**
     * Action listener for creation of new person objectgs
     * @return  
     */
    public String personCreateCommit(){
        PersonCoordinator pc = getPersonCoordinator();
    
        try {
            int freshID = pc.personCreate(workingPerson, getSessionBean().getSessUser());
            getSessionBean().setSessPerson(pc.assemblePersonDataHeavy(pc.getPerson(freshID),getSessionBean().getSessUser().getKeyCard()));
               if(connectToActiveProperty){
                   
                   Property property = getSessionBean().getSessProperty();
                   pc.connectPersonToProperty(workingPerson, property);
                   getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Successfully added " + workingPerson.getFirstName() + " to the Database!" 
                                + " and connected to " + property.getAddress(), ""));
               } else {

                   getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, 
                                "Successfully added " + workingPerson.getFirstName() + " to the Database!", ""));
               }
           } catch (IntegrationException ex) {
               System.out.println(ex.toString());
                  getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                               "Unable to add new person to the database, my apologies!", ""));
           }
        return "personInfo";
    }
    
    
    
    public void attachNoteToPerson(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();

        try {
            pc.addNotesToPerson(currPerson, getSessionBean().getSessUser(), getFormNotes());
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO
                    , "Done: Notes added to person ID:" + currPerson.getPersonID(),"" ));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR
                    , "Unable to update notes, sorry!"
                    , getResourceBundle(Constants.MESSAGE_TEXT).getString("systemLevelError")));
        }
    }
    
    
    public String deletePerson(){
        System.out.println("PersonBB.deletePerson | in method");
        PersonCoordinator pc = getPersonCoordinator();
        
        try {
            pc.personNuke(currPerson, getSessionBean().getSessUser().getMyCredential());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        currPerson.getFirstName() + " has been permanently deleted; Goodbye " 
                                + currPerson.getFirstName() 
                                + ". Search results have been cleared.", ""));
            
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Cannot delete person." + ex.toString(), "Best not to delete folks anyway..."));
            return "";
            
        } catch (AuthorizationException ex) {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        ex.getLocalizedMessage(),""));
            return "";
            
        }
        return "personSearch";
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
     * @return the fieldDump
     */
    public String getFieldDump() {
        return fieldDump;
    }

    /**
     * @param fieldDump the fieldDump to set
     */
    public void setFieldDump(String fieldDump) {
        this.fieldDump = fieldDump;
    }

    /**
     * @return the workingPerson
     */
    public Person getWorkingPerson() {
        return workingPerson;
    }

    /**
     * @param workingPerson the workingPerson to set
     */
    public void setWorkingPerson(Person workingPerson) {
        this.workingPerson = workingPerson;
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
    
    
}
