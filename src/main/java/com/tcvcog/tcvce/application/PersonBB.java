/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.PersonCoordinator;
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
import javax.faces.event.ActionEvent;

/**
 * The inaugural humanized PersonBB for all basic person stuff
 * Backing the personTools.xhtml which gets embedded in the nav container
 * @author sylvia
 */
public class PersonBB extends BackingBeanUtils {
    
    private IFace_humanListHolder currentHumanListHolder;

    private Human currentHuman;
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
        PersonCoordinator pc = getPersonCoordinator();
        try {
            phoneTypeList = pc.getContactPhoneTypeList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
        
        
    }
    
    
    /**********************************************************/
    /************** HUMANS INFRASTRUCTURE *********************/
    /**********************************************************/
    
    /**
     * Listener for user clicks of the human edit button
     */
    public void toggleHumanEditMode(){
        
        
    }
    
    /**
     * Listener for user requests to add a human
     * @param ev 
     */
    public void onHumanAddInitButtonChange(ActionEvent ev){
        
        
    }
    
    /**
     * Listener for user requests to finalize human changes
     * @param ev 
     */
    public void onHumanAddCommitButtonChange(ActionEvent ev){
        
        
        
    }
    
    /**
     * Listener for user requests to start the human edit process
     * @param h
      */
    public void onHumanEditInitButtonChange(Human h){
        
        
    }
    
    /**
     * Listener for user requests to commit the human edit process
     * @param ev 
     */
    public void onHumanEditCommitButtonChange(ActionEvent ev){
        
        
    }
    
    /**
     * Listener for user requests to abort a human add or edit operation
     * @param ev 
     */
    public void onHumanOperationAbortButtonChange(ActionEvent ev){
        
        
    }
    
    
    /**********************************************************/
    /************** Mailing address INFRASTRUCTURE *********************/
    /**********************************************************/
    
    /**
     * Listener for user clicks of the mailing address edit mode
     */
    public void toggleMailingAddressEditMode(){
        
        
    }
    
    
    /**
     * Listener for user requests to start the process of adding a mailing address
     * @param ev 
     */
    public void onMailingAddressAddInitButtonChange(ActionEvent ev){
        
        
    }
    
    /**
     * Listener for user requests to finalize the process of adding a mailing address
     * @param ev 
     */
    public void onMailingAddressAddCommitButtonChange(ActionEvent ev){
        
    }
    
    /**
     * Listener for user requests to start the process of editing a mailing address
     * @param ma
     */
    public void onMailingAddressEditInitButtonChange(MailingAddress ma){
        
        
    }
    
    /**
     * Listener for user requests to finalize mailing address edits
     * @param ev 
     */
    public void onMailingAddressEditCommitButtonChange(ActionEvent ev){
        
        
    }
    
    /**
     * Listener for user requests to abort any mailing address operation
     * @param ev 
     */
    public void onMailingAddressAbortOperationButtonChange(ActionEvent ev){
        
    }
    
    
    /***********************************************************/
    /************** Phone number INFRASTRUCTURE ****************/
    /**********************************************************/
    
    /**
     * Listener for user clicks of the phone edit button
     */
    public void togglePhoneEditMode(){
        
        
    }
    
    /**
     * Listener for user requests to start adding a new phone number
     * @param ev 
     */
    public void onPhoneAddInitButtonChange(ActionEvent ev){
        
        
    }
    
    /**
     * Listener for user requests to finalize a new phone number
     * @param ev 
     */
    public void onPhoneAddCommitButtonChange(ActionEvent ev){
        
        
    }
    
    
    /**
     * Listener for user requests to start the phone editing process
     * @param ph
     */
    public void onPhoneEditInitButtonChange(ContactPhone ph){
        
        
    }
    
    /**
     * Listener for user requests to finalize the phone edit operation
     * @param ev 
     */
    public void onPhoneEditCommitButtonChange(ActionEvent ev){
        
        
    }
    
    /**
     * Listener for user requests to abort any phone operation
     * @param ev 
     */
    public void onPhoneOperationAbortButtonChange(ActionEvent ev){
        
        
    }
    
    
    /***********************************************************/
    /********************* Email INFRASTRUCTURE ****************/
    /**********************************************************/
    
    /**
     * Listener for user clicks of the edit button for emails
     */
    public void toggleEmailEditMode(){
        
        
        
    }
    
     /**
     * Listener for user requests to start adding a new email 
     * @param ev 
     */
    public void onEmailAddInitButtonChange(ActionEvent ev){
        
        
    }
    
    /**
     * Listener for user requests to finalize a new email 
     * @param ev 
     */
    public void onEmailAddCommitButtonChange(ActionEvent ev){
        
        
    }
    
    
    /**
     * Listener for user requests to start the email editing process
     * @param ce 
     */
    public void onEmailEditInitButtonChange(ContactEmail ce){
        
        
    }
    
    /**
     * Listener for user requests to finalize the email edit operation
     * @param ev 
     */
    public void onEmailEditCommitButtonChange(ActionEvent ev){
        
        
    }
    
    /**
     * Listener for user requests to abort any email operation
     * @param ev 
     */
    public void onEmailOperationAbortButtonChange(ActionEvent ev){
        
        
    }
    
    
    /***********************************************************/
    /********************* GETTERS AND SETTERS  ****************/
    /**********************************************************/
    

    /**
     * @return the currentHuman
     */
    public Human getCurrentHuman() {
        return currentHuman;
    }

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
     * @param currentHuman the currentHuman to set
     */
    public void setCurrentHuman(Human currentHuman) {
        this.currentHuman = currentHuman;
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
