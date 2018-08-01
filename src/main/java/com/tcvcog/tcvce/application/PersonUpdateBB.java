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
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Eric C. Darsow
 */
public class PersonUpdateBB extends BackingBeanUtils implements Serializable{

    private Person currentPerson;
    
    private int personid;
    private PersonType formPersonType;
    private PersonType[] personTypes;
    private int formMuniCode;
    private HashMap muniMap;
    
    private String formFirstName;
    private String formLastName;
    private String formJobTitle;
    
    private String formPhoneCell;
    private String formPhoneHome;
    private String formPhoneWork;
    
    private String formEmail;
    private String formAddress_street;
    private String formAddress_city;
    
    private String formAddress_state;
    private String formAddress_zip;
    private String formNotes;
    
    private LocalDateTime lastUpdated;
    private java.util.Date formExpiryDate;
    private boolean formIsActive;
    
    private boolean formIsUnder18;
    
    
    /**
     * Creates a new instance of PersonUpdateBB
     */
    public PersonUpdateBB() {
        
        
    }
    
    
    public String updatePerson(){
        System.out.println("PersonUpdateBB.updatePerson");
        Person p = new Person();
        PersonIntegrator pi = getPersonIntegrator();
        
        p.setPersonID(currentPerson.getPersonID());
        p.setPersonType(formPersonType);
        p.setMuniCode(formMuniCode);
        
        p.setFirstName(formFirstName);
        p.setLastName(formLastName);
        p.setJobTitle(formJobTitle);
        
        p.setPhoneCell(formPhoneCell);
        p.setPhoneHome(formPhoneHome);
        p.setPhoneWork(formPhoneWork);
        
        p.setEmail(formEmail);
        p.setAddress_street(formAddress_street);
        p.setAddress_city(formAddress_city);
        
        p.setAddress_zip(formAddress_zip);
        p.setAddress_state(formAddress_state);
        p.setNotes(formNotes);
        
        //integrator sets lastUpdated
        p.setExpiryDate(formExpiryDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
        p.setIsActive(formIsActive);
        
        p.setIsUnder18(formIsUnder18);
        
        try {
            pi.updatePerson(p);
            
            getSessionBean().setActivePerson(p);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Person updated! This updated person is now your 'active person'", ""));
            
        } catch (IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to update person, my apologies", ""));
            
        }
        return "";
    }

    /**
     * @return the personid
     */
    public int getPersonid() {
            
        return personid;
    }

    /**
     * @return the formPersonType
     */
    public PersonType getFormPersonType() {
        formPersonType = currentPerson.getPersonType();
        return formPersonType;
    }

    /**
     * @return the formMuni
     */
    public int getFormMuniCode() {
        //System.out.println("PersonUpdateBB.getFormMuniID | muniID of active currentPerson: " + currentPerson.getMuni().getMuniCode());
        
        formMuniCode = getSessionBean().getActivePerson().getMuniCode();
        return formMuniCode;
    }

    /**
     * @return the formFirstName
     */
    public String getFormFirstName() {
        formFirstName = currentPerson.getFirstName();
        return formFirstName;
    }

    /**
     * @return the formLastName
     */
    public String getFormLastName() {
        formLastName = currentPerson.getLastName();
        return formLastName;
    }

    /**
     * @return the formJobTitle
     */
    public String getFormJobTitle() {
        formJobTitle = currentPerson.getJobTitle();
        return formJobTitle;
    }

    /**
     * @return the formPhoneCell
     */
    public String getFormPhoneCell() {
        formPhoneCell = currentPerson.getPhoneCell();
        return formPhoneCell;
    }

    /**
     * @return the formPhoneHome
     */
    public String getFormPhoneHome() {
        formPhoneHome = currentPerson.getPhoneHome();
        return formPhoneHome;
    }

    /**
     * @return the formPhoneWork
     */
    public String getFormPhoneWork() {
        formPhoneWork = currentPerson.getPhoneWork();
        return formPhoneWork;
    }

    /**
     * @return the formEmail
     */
    public String getFormEmail() {
        formEmail = currentPerson.getEmail();
        return formEmail;
    }

    /**
     * @return the formAddress_street
     */
    public String getFormAddress_street() {
        formAddress_street = currentPerson.getAddress_street();
        return formAddress_street;
    }

    /**
     * @return the formAddress_city
     */
    public String getFormAddress_city() {
        formAddress_city = currentPerson.getAddress_city();
        return formAddress_city;
    }

    /**
     * @return the formAddress_state
     */
    public String getFormAddress_state() {
        formAddress_state = currentPerson.getAddress_state();
        return formAddress_state;
    }

    /**
     * @return the formAddress_zip
     */
    public String getFormAddress_zip() {
        formAddress_zip = currentPerson.getAddress_zip();
        return formAddress_zip;
    }

    /**
     * @return the formNotes
     */
    public String getFormNotes() {
        formNotes = currentPerson.getNotes();
        return formNotes;
    }

    /**
     * @return the lastUpdated
     */
    public LocalDateTime getLastUpdated() {
        lastUpdated = currentPerson.getLastUpdated();
        return lastUpdated;
    }

    
    /**
     * @return the formIsActive
     */
    public boolean isFormIsActive() {
        formIsActive = currentPerson.isIsActive();
        return formIsActive;
    }

    /**
     * @return the formIsUnder18
     */
    public boolean isFormIsUnder18() {
        formIsUnder18 = currentPerson.isIsUnder18();
        return formIsUnder18;
    }

    /**
     * @param personid the personid to set
     */
    public void setPersonid(int personid) {
        this.personid = personid;
    }

    /**
     * @param formPersonType the formPersonType to set
     */
    public void setFormPersonType(PersonType formPersonType) {
        this.formPersonType = formPersonType;
    }

    /**
     * @param formMuni the formMuni to set
     */
    public void setFormMuniCode(int formMuni) {
        this.formMuniCode = formMuni;
    }

    /**
     * @param formFirstName the formFirstName to set
     */
    public void setFormFirstName(String formFirstName) {
        this.formFirstName = formFirstName;
    }

    /**
     * @param formLastName the formLastName to set
     */
    public void setFormLastName(String formLastName) {
        this.formLastName = formLastName;
    }

    /**
     * @param formJobTitle the formJobTitle to set
     */
    public void setFormJobTitle(String formJobTitle) {
        this.formJobTitle = formJobTitle;
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
     * @param formAddress_state the formAddress_state to set
     */
    public void setFormAddress_state(String formAddress_state) {
        this.formAddress_state = formAddress_state;
    }

    /**
     * @param formAddress_zip the formAddress_zip to set
     */
    public void setFormAddress_zip(String formAddress_zip) {
        this.formAddress_zip = formAddress_zip;
    }

    /**
     * @param formNotes the formNotes to set
     */
    public void setFormNotes(String formNotes) {
        this.formNotes = formNotes;
    }

    /**
     * @param lastUpdated the lastUpdated to set
     */
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }



    /**
     * @param formIsActive the formIsActive to set
     */
    public void setFormIsActive(boolean formIsActive) {
        this.formIsActive = formIsActive;
    }

    /**
     * @param formIsUnder18 the formIsUnder18 to set
     */
    public void setFormIsUnder18(boolean formIsUnder18) {
        this.formIsUnder18 = formIsUnder18;
    }

    /**
     * @return the currentPerson
     */
    public Person getCurrentPerson() {
        
        currentPerson = getSessionBean().getActivePerson();
        return currentPerson;
    }

    /**
     * @param currentPerson the currentPerson to set
     */
    public void setCurrentPerson(Person currentPerson) {
        this.currentPerson = currentPerson;
    }

    /**
     * @return the personTypes
     */
    public PersonType[] getPersonTypes() {
        personTypes = PersonType.values();
        return personTypes;
    }

    /**
     * @param personTypes the personTypes to set
     */
    public void setPersonTypes(PersonType[] personTypes) {
        this.personTypes = personTypes;
    }

    /**
     * @return the muniMap
     */
    public HashMap getMuniMap() {
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        try {
            muniMap = mi.getMunicipalityMap();
        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Hark--No elemented selected. Please click on a code element first.", ""));
        }
        return muniMap;
    }

    /**
     * @param muniMap the muniMap to set
     */
    public void setMuniMap(HashMap muniMap) {
        this.muniMap = muniMap;
    }

    /**
     * @return the formExpiryDate
     */
    public java.util.Date getFormExpiryDate() {
        if(currentPerson.getExpiryDate() != null){
            formExpiryDate = Date.from(currentPerson.getExpiryDate().atZone(ZoneId.systemDefault()).toInstant());
        }
        return formExpiryDate;
    }

    /**
     * @param formExpiryDate the formExpiryDate to set
     */
    public void setFormExpiryDate(java.util.Date formExpiryDate) {
        this.formExpiryDate = formExpiryDate;
    }

   

}