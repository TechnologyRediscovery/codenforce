/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Icon;
import com.tcvcog.tcvce.entities.PropertyUseType;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author sylvia
 */
public class PropertyUseTypeBB extends BackingBeanUtils implements Serializable{

    private List<PropertyUseType> putList;
    private PropertyUseType currentPut;
    
    /**
     * Creates a new 
     * instance of PropertyUseTypeBB
     */
    public PropertyUseTypeBB() {
        
        
    }
    
    @PostConstruct
    public void initBean(){
        SystemIntegrator si = getSystemIntegrator();
        try {
            putList = si.getPutList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }
    
    public void editPut(PropertyUseType p){
        currentPut = p;
    }
    
    public void commitUpdates(ActionEvent ev){
        SystemIntegrator si = getSystemIntegrator();
        try {
            si.updatePut(currentPut);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully updated PropertyUseType", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not update PropertyUseType, sorry", ""));
        }
        
    }
    
    public void commitInsert(ActionEvent ev){
        SystemIntegrator si = getSystemIntegrator();
        try {
            si.insertPut(currentPut);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! PropertyUseType inserted", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not insert PropertyUseType, sorry", ""));
        }
        
    }
    //method for deleting existing puts
    public void commitDelete(ActionEvent ev) {
//		  SystemIntegrator si = getSystemIntegrator(); 
//		  try {
//			  si.deletePut(currentPut); 
//			  getFacesContext().addMessage(null, 
//					  new FacesMessage(FacesMessage.SEVERITY_INFO, 
//							  "Success! PropertyUseType removed", "")); 
//		  } catch (IntegrationException ex) { getFacesContext().addMessage(null, new
//				  FacesMessage(FacesMessage.SEVERITY_ERROR, "Could not remove PropertyUseType, sorry",
//				  "")); }
//		 
    }
    
    public void createNewPut(){
        PropertyUseType p = new PropertyUseType();
        p.setIcon(new Icon());
        currentPut = p;
    }
    

    /**
     * @return the putList
     */
    public List<PropertyUseType> getPutList() {
        return putList;
    }

    /**
     * @param putList the putList to set
     */
    public void setPutList(List<PropertyUseType> putList) {
        this.putList = putList;
    }

    /**
     * @return the currentPut
     */
    public PropertyUseType getCurrentPut() {
        System.out.println("getCurrentPuts");
        return currentPut;
    }

    /**
     * @param currentPut the currentPut to set
     */
    public void setCurrentPut(PropertyUseType currentPut) {
        this.currentPut = currentPut;
    }
    
}
