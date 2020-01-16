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
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonDataHeavy;
import com.tcvcog.tcvce.entities.Property;
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
       } else {
            currPerson = (getSessionBean().getSessPerson());
       }
        
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
                 new FacesMessage(FacesMessage.SEVERITY_FATAL, 
                     "Edits failed on person due to a database bug!", ""));
        }
    }
    
    
     public String viewPersonAssociatedProperty(Property p){
        getSessionBean().getSessPropertyList().add(0, p);
        return "properties";
    }
    
    
    public void personCreateInit(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
        workingPerson = pc.personCreateMakeSkeleton(getSessionBean().getSessUser().getMyCredential().getGoverningAuthPeriod().getMuni());
    }
    
    /**
     * Action listener for creation of new person objectgs
     * @param ev 
     */
    public void personCreateCommit(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
    
        try {
            pc.personCreate(workingPerson, getSessionBean().getSessUser());
               if(connectToActiveProperty){
                   
                   Property property = getSessionBean().getSessProperty();
                   
                   getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Successfully added " + p.getFirstName() + " to the Database!" 
                                + " and connected to " + property.getAddress(), ""));

               } else {

                   getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, 
                                "Successfully added " + p.getFirstName() + " to the Database!", ""));

               }

           } catch (IntegrationException ex) {
               System.out.println(ex.toString());
                  getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                               "Unable to add new person to the database, my apologies!", ""));
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
    
    
}
