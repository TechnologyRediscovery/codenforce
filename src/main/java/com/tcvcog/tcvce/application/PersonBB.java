/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.ContactEmail;
import com.tcvcog.tcvce.entities.ContactPhone;
import com.tcvcog.tcvce.entities.ContactPhoneType;
import com.tcvcog.tcvce.entities.Human;
import com.tcvcog.tcvce.entities.IFace_humanListHolder;
import com.tcvcog.tcvce.entities.MailingAddress;
import com.tcvcog.tcvce.entities.Person;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 * The inaugural humanized PersonBB for all basic person stuff
 * Backing the personTools.xhtml which gets embedded in the nav container
 * @author sylvia
 */
public class PersonBB extends BackingBeanUtils {
    
    private IFace_humanListHolder currentHumanListHolder;

    
    private Person currentPerson;
    private boolean currentPersonEditMode;
    
    private MailingAddress currentMailingAddress;
    private boolean currentMailingAddressEditMode;
    
    private ContactPhone currentContactPhone;
    private boolean currentContactPhoneEditMode;
    private List<ContactPhoneType> phoneTypeList;
    private boolean currentContactPhoneDisconnected;
    
    private ContactEmail currentContactEmail;
    private boolean currentContactEmailEditMode;
    
    
    /**
     * Creates a new instance of PersonBB
     */
    public PersonBB() {
    }
    
     @PostConstruct
    public void initBean(){
        currentPersonEditMode = false;
        currentContactPhoneEditMode = false;
        currentContactEmailEditMode = false;
        
        PersonCoordinator pc = getPersonCoordinator();
        try {
            phoneTypeList = pc.getContactPhoneTypeList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
    }
    
    /**********************************************************/
    /************** REFRESHING  *******************************/
    /**********************************************************/
    
    /**
     * Refreshes the current person
     */
    private void refreshCurrentPerson(){
        PersonCoordinator pc = getPersonCoordinator();
        try {
            currentPerson = pc.getPerson(currentPerson);
            
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not refresh current Person", ""));
        }
        
        
    }
    
    
    /**********************************************************/
    /************** HUMANS INFRASTRUCTURE *********************/
    /**********************************************************/
    
    /**
     * Listener for user clicks of the human edit button
     */
    public void toggleHumanEditMode(){
        if(currentPersonEditMode){
            if(currentPerson != null && currentPerson.getHumanID() == 0){
                // we've got a new record, so commit our add to DB
                onHumanAddCommitButtonChange(null);
            } else {
                // we've got an existing human, so commit updates
                onHumanEditCommitButtonChange(null);
            }
        }
        currentPersonEditMode = !currentPersonEditMode;
        refreshCurrentPerson();
        
        
    }
    
    /**
     * Listener for user requests to add a human
     * @param ev 
     */
    public void onHumanAddInitButtonChange(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
        currentPerson = pc.createPersonSkeleton(getSessionBean().getSessMuni());
        
        
    }
    
    /**
     * Listener for user requests to finalize human changes
     * @param ev 
     */
    public void onHumanAddCommitButtonChange(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
        try {
            currentPerson = pc.humanAdd(currentPerson, getSessionBean().getSessUser());
            refreshCurrentPerson();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Succesfully added person to database!", ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "FATAL ERROR CODE P001: Could not add new Person to the database!", ""));
        }    
        
        
        
    }
    
    /**
     * Listener for user requests to start the human edit process
     * @param h
      */
    public void onHumanEditInitButtonChange(Human h){
        PersonCoordinator pc = getPersonCoordinator();
        try {
            currentPerson = pc.getPerson(h);
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
        }
    }
    
    /**
     * Listener for user requests to commit the human edit process
     * @param ev 
     */
    public void onHumanEditCommitButtonChange(ActionEvent ev){
        
        PersonCoordinator pc = getPersonCoordinator();
        try {
            pc.humanEdit(currentPerson, getSessionBean().getSessUser());
            refreshCurrentPerson();
              getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Succesfully updated " + currentPerson.getName() + ", id " + currentPerson.getHumanID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "ERROR updating " + currentPerson.getName() + ", id " + currentPerson.getHumanID(), ""));
            
        }
        
        
    }
    
    /**
     * Listener for user requests to abort a human add or edit operation
     * @param ev 
     */
    public void onHumanOperationAbortButtonChange(ActionEvent ev){
        currentPersonEditMode = false;
        getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_INFO,
                         "Abort successful; no changes made", ""));
    }
    
    
   
    
    /***********************************************************/
    /************** Phone number INFRASTRUCTURE ****************/
    /**********************************************************/
    
    /**
     * Listener for user clicks of the phone edit button
     */
    public void togglePhoneEditMode(){
        
        if(currentContactPhoneEditMode){
            PersonCoordinator pc = getPersonCoordinator();
            if(currentContactPhone != null && currentContactPhone.getPhoneID() == 0){
                onPhoneAddCommitButtonChange(null);
            } else {
                onPhoneEditCommitButtonChange(null);
            }
        }
        currentContactPhoneEditMode = !currentContactPhoneEditMode;
        
    }
    
    /**
     * Listener for user requests to start adding a new phone number
     * @param ev 
     */
    public void onPhoneAddInitButtonChange(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
        currentContactPhone = pc.createContactPhoneSkeleton();
        
        
    }
    
    /**
     * Listener for user requests to finalize a new phone number
     * @param ev 
     */
    public void onPhoneAddCommitButtonChange(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
        try {
            currentContactPhone = pc.contactPhoneAdd(currentContactPhone, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Succesfully inserted new phone number with ID " + currentContactPhone.getPhoneID(), ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to insert new ContactPhone, sorry. This problem must be fixed by an dev.", ""));
        }
    }
    
    
    /**
     * Listener for user requests to start the phone editing process
     * @param ph
     */
    public void onPhoneEditInitButtonChange(ContactPhone ph){
        if(ph != null){
            currentContactPhone = ph;
        }
        currentContactEmailEditMode = true;
    }
    
    /**
     * Listener for user requests to finalize the phone edit operation
     * @param ev 
     */
    public void onPhoneEditCommitButtonChange(ActionEvent ev){
         PersonCoordinator pc = getPersonCoordinator();
        try {
            pc.contactPhoneUpdate(currentContactPhone, getSessionBean().getSessUser());
            currentContactPhone = pc.getContactPhone(currentContactPhone);
            getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Succesfully Updated new phone number with ID " + currentContactPhone.getPhoneID(), ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to Update phone number, sorry. This problem must be fixed by an dev.", ""));
        }
    }
    
    /**
     * Listener for user requests to abort any phone operation
     * @param ev 
     */
    public void onPhoneOperationAbortButtonChange(ActionEvent ev){
        currentContactPhoneEditMode = false;
        getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Aborted; no changes made!", ""));
        
    }
    
    
    /***********************************************************/
    /********************* Email INFRASTRUCTURE ****************/
    /**********************************************************/
    
    /**
     * Listener for user clicks of the edit button for emails
     */
    public void toggleEmailEditMode(){
        if(currentContactEmailEditMode){
            if(currentContactEmail != null && currentContactEmail.getEmailID() == 0){
                onEmailAddCommitButtonChange(null);
            } else {
                onEmailEditCommitButtonChange(null);
            }
        }
        currentContactEmailEditMode = !currentContactEmailEditMode;
    }
    
    
     /**
     * Listener for user requests to start adding a new email 
     * @param ev 
     */
    public void onEmailAddInitButtonChange(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
        currentContactEmail = pc.createContactEmailSkeleton();
        currentContactEmailEditMode = true;
        
    }
    
    /**
     * Listener for user requests to finalize a new email 
     * @param ev 
     */
    public void onEmailAddCommitButtonChange(ActionEvent ev){
          PersonCoordinator pc = getPersonCoordinator();
        try {
            currentContactEmail = pc.contactEmailAdd(currentContactEmail, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Succesfully updated " + currentContactEmail.getEmailaddress()+ ", id " + currentPerson.getHumanID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "ERROR updating email address with id " + currentContactEmail.getEmailID(), ""));
        }
    }
    
    
    /**
     * Listener for user requests to start the email editing process
     * @param ce 
     */
    public void onEmailEditInitButtonChange(ContactEmail ce){
        currentContactEmail = ce;
        currentContactEmailEditMode = true;
        
    }
    
    /**
     * Listener for user requests to finalize the email edit operation
     * @param ev 
     */
    public void onEmailEditCommitButtonChange(ActionEvent ev){
          PersonCoordinator pc = getPersonCoordinator();
        try {
            pc.contactEmailUpdate(currentContactEmail, getSessionBean().getSessUser());
            currentContactEmail = pc.getContactEmail(currentContactEmail);
              getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Succesfully updated " + currentPerson.getName() + ", id " + currentPerson.getHumanID(), ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "ERROR updating " + currentPerson.getName() + ", id " + currentPerson.getHumanID(), ""));
            
        }
        
    }
    
    /**
     * Listener for user requests to abort any email operation
     * @param ev 
     */
    public void onEmailOperationAbortButtonChange(ActionEvent ev){
          getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Aborted; No changes made!", ""));
          currentContactEmailEditMode = false;
        
    }
    
    
    /***********************************************************/
    /********************* GETTERS AND SETTERS  ****************/
    /**********************************************************/
    

   

    /**
     * @return the currentPersonEditMode
     */
    public boolean isCurrentPersonEditMode() {
        return currentPersonEditMode;
    }

    /**
     * @return the currentMailingAddress
     */
    public MailingAddress getCurrentMailingAddress() {
        return currentMailingAddress;
    }

    /**
     * @return the currentMailingAddressEditMode
     */
    public boolean isCurrentMailingAddressEditMode() {
        return currentMailingAddressEditMode;
    }

    /**
     * @return the currentContactPhone
     */
    public ContactPhone getCurrentContactPhone() {
        return currentContactPhone;
    }

    /**
     * @return the currentContactPhoneEditMode
     */
    public boolean isCurrentContactPhoneEditMode() {
        return currentContactPhoneEditMode;
    }

    /**
     * @return the currentContactEmail
     */
    public ContactEmail getCurrentContactEmail() {
        return currentContactEmail;
    }

    /**
     * @return the currentContactEmailEditMode
     */
    public boolean isCurrentContactEmailEditMode() {
        return currentContactEmailEditMode;
    }

  

    /**
     * @param currentPersonEditMode the currentPersonEditMode to set
     */
    public void setCurrentPersonEditMode(boolean currentPersonEditMode) {
        this.currentPersonEditMode = currentPersonEditMode;
    }

    /**
     * @param currentMailingAddress the currentMailingAddress to set
     */
    public void setCurrentMailingAddress(MailingAddress currentMailingAddress) {
        this.currentMailingAddress = currentMailingAddress;
    }

    /**
     * @param currentMailingAddressEditMode the currentMailingAddressEditMode to set
     */
    public void setCurrentMailingAddressEditMode(boolean currentMailingAddressEditMode) {
        this.currentMailingAddressEditMode = currentMailingAddressEditMode;
    }

    /**
     * @param currentContactPhone the currentContactPhone to set
     */
    public void setCurrentContactPhone(ContactPhone currentContactPhone) {
        this.currentContactPhone = currentContactPhone;
    }

    /**
     * @param currentContactPhoneEditMode the currentContactPhoneEditMode to set
     */
    public void setCurrentContactPhoneEditMode(boolean currentContactPhoneEditMode) {
        this.currentContactPhoneEditMode = currentContactPhoneEditMode;
    }

    /**
     * @param currentContactEmail the currentContactEmail to set
     */
    public void setCurrentContactEmail(ContactEmail currentContactEmail) {
        this.currentContactEmail = currentContactEmail;
    }

    /**
     * @param currentContactEmailEditMode the currentContactEmailEditMode to set
     */
    public void setCurrentContactEmailEditMode(boolean currentContactEmailEditMode) {
        this.currentContactEmailEditMode = currentContactEmailEditMode;
    }

    /**
     * @return the currentHumanListHolder
     */
    public IFace_humanListHolder getCurrentHumanListHolder() {
        return currentHumanListHolder;
    }

    /**
     * @param currentHumanListHolder the currentHumanListHolder to set
     */
    public void setCurrentHumanListHolder(IFace_humanListHolder currentHumanListHolder) {
        this.currentHumanListHolder = currentHumanListHolder;
    }

    /**
     * @return the currentPerson
     */
    public Person getCurrentPerson() {
        return currentPerson;
    }

    /**
     * @param currentPerson the currentPerson to set
     */
    public void setCurrentPerson(Person currentPerson) {
        this.currentPerson = currentPerson;
    }

    /**
     * @return the phoneTypeList
     */
    public List<ContactPhoneType> getPhoneTypeList() {
        return phoneTypeList;
    }

    /**
     * @param phoneTypeList the phoneTypeList to set
     */
    public void setPhoneTypeList(List<ContactPhoneType> phoneTypeList) {
        this.phoneTypeList = phoneTypeList;
    }

    /**
     * @return the currentContactPhoneDisconnected
     */
    public boolean isCurrentContactPhoneDisconnected() {
        return currentContactPhoneDisconnected;
    }

    /**
     * @param currentContactPhoneDisconnected the currentContactPhoneDisconnected to set
     */
    public void setCurrentContactPhoneDisconnected(boolean currentContactPhoneDisconnected) {
        this.currentContactPhoneDisconnected = currentContactPhoneDisconnected;
    }
    
     
    
    
    
}
