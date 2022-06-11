/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Icon;
import com.tcvcog.tcvce.entities.PropertyUseType;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
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
public class deprecated_PropertyUseTypeBB extends BackingBeanUtils implements Serializable{

    private List<PropertyUseType> putList;
    private PropertyUseType currentPut;
    
    private List<Icon> iconList;
    
    /**
     * Creates a new 
     * instance of PropertyUseTypeBB
     */
    public deprecated_PropertyUseTypeBB() {
        
        
    }
    
    @PostConstruct
    public void initBean(){
        refreshPutList();
        createNewPut();
    }
    
    public void refreshPutList(){
        SystemCoordinator sc = getSystemCoordinator();
        try {
            putList = sc.getPutList();
            setIconList(sc.getIconList());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }
    
    public void editPut(PropertyUseType p){
        currentPut = p;
    }
    
    public void commitUpdates(ActionEvent ev){
        SystemCoordinator sc = getSystemCoordinator();
        try {
            sc.updatePut(currentPut);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully updated PropertyUseType", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not update PropertyUseType, sorry", ""));
        }
        refreshPutList();
    }
    
    public void commitInsert(ActionEvent ev){
        SystemCoordinator sc = getSystemCoordinator();
        try {
            sc.insertPut(currentPut);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! PropertyUseType inserted", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not insert PropertyUseType, sorry", ""));
        }
        refreshPutList();
    }
    
    //method for deleting existing puts
    public void commitRemove(ActionEvent ev) {
        SystemCoordinator sc = getSystemCoordinator();
        if(currentPut.getTypeID() > 0){             
            try {
                int uses = sc.putCheckForUse(currentPut);
                if(uses == 0){
                    sc.deactivatePut(currentPut);
                    getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Success! PropertyUseType removed", ""));
                } else {
                    getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "PropertyUseType is in use " + uses + " times. Could not remove", ""));
                }
            } catch (IntegrationException ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Could not remove PropertyUseType, sorry", ""));
            }
            refreshPutList();
        } else {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Invalid PropertyUseTypeID: " + currentPut.getTypeID(), ""));
        }
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
        return currentPut;
    }

    /**
     * @param currentPut the currentPut to set
     */
    public void setCurrentPut(PropertyUseType currentPut) {
        this.currentPut = currentPut;
    }

    /**
     * @return the iconList
     */
    public List<Icon> getIconList() {
        return iconList;
    }

    /**
     * @param iconList the iconList to set
     */
    public void setIconList(List<Icon> iconList) {
        this.iconList = iconList;
    }
    
}
