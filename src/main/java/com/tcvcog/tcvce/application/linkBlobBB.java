/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.BlobCoordinator;
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.integration.BlobIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;


/**
 *
 * @author noah
 */
public class linkBlobBB extends BackingBeanUtils implements Serializable{
    
    private int codeViolationID, propertyID, selectedBlobID, personID;

    /**
     * Creates a new instance of linkBlobBB
     */
    public linkBlobBB() {
    }
    
    @PostConstruct
    public void initBean() {
    }
    
    public void linkBlobToCodeViolation() {
        BlobIntegrator bi = getBlobIntegrator();
        CaseCoordinator cc = getCaseCoordinator();
        
        try{
            cc.violation_getCodeViolation(codeViolationID);
            System.out.println("linkBlobBB.linkBlobToCodeViolation | retrieved code violation");  //TESTING
        }catch(IntegrationException e){
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR
                            ,"Unable to find Code Violation with that ID. " , ""));
            return;
        }
        
        try {
            bi.linkPhotoBlobToCodeViolation(selectedBlobID, codeViolationID);
            System.out.println("linkBlobBB.linkBlobToCodeViolation | link succesfull");  //TESTING
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR
                            ,"Failed to link file to selected violation. Sorry! " , ""));
        }
        
    }
    
    public void linkBlobToProperty() {
        BlobIntegrator bi = getBlobIntegrator();
        PropertyIntegrator pi = getPropertyIntegrator();
        
        try{
            pi.getProperty(propertyID);
        }catch(IntegrationException e){
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR
                            ,"Unable to find Property with that ID. " , ""));
            return;
        }
        
        try {
            bi.linkPhotoBlobToProperty(selectedBlobID, propertyID);
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR
                            ,"Failed to link file to Property. Sorry! " , ""));
        }
        
    }
    
    public void linkBlobToPerson() {
        BlobIntegrator bi = getBlobIntegrator();
        PersonIntegrator pi = getPersonIntegrator();
        
        try{
            pi.getPerson(getPersonID());
        }catch(IntegrationException e){
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR
                            ,"Unable to find Person with that ID. " , ""));
            return;
        }
        
        try {
            bi.linkPhotoBlobToPerson(selectedBlobID, getPersonID());
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR
                            ,"Failed to link file to Person. Sorry! " , ""));
        }
    }
    
    public String navToDash() {
        return "missionControl";
    }

    /**
     * @return the codeViolationID
     */
    public int getCodeViolationID() {
        return codeViolationID;
    }

    /**
     * @param codeViolationID the codeViolationID to set
     */
    public void setCodeViolationID(int codeViolationID) {
        this.codeViolationID = codeViolationID;
    }

    /**
     * @return the propertyID
     */
    public int getPropertyID() {
        return propertyID;
    }

    /**
     * @param propertyID the propertyID to set
     */
    public void setPropertyID(int propertyID) {
        this.propertyID = propertyID;
    }

    /**
     * @return the selectedBlob
     */
    public int getSelectedBlobID() {
        return selectedBlobID;
    }

    /**
     * @param selectedBlobID
     */
    public void setSelectedBlobID(int selectedBlobID) {
        System.out.println("linkBlobBB.setSelectedBlobID | blobID = " + selectedBlobID);
        this.selectedBlobID = selectedBlobID;
    }

    /**
     * @return the personID
     */
    public int getPersonID() {
        return personID;
    }

    /**
     * @param personID the personID to set
     */
    public void setPersonID(int personID) {
        this.personID = personID;
    }
}
