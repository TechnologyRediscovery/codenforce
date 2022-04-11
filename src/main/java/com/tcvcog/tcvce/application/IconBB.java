/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Icon;
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
public class IconBB extends BackingBeanUtils implements Serializable{

    private List<Icon> iconList;
    private Icon currentIcon;
    
    /**
     * Creates a new 
     * instance of IconBB
     */
    public IconBB() {
        
        
    }
    
    @PostConstruct
    public void initBean(){
        refreshIconList();
    }
    
    public void refreshIconList(){
        SystemCoordinator sc = getSystemCoordinator();
        try {
            iconList = sc.getIconList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }
    
    public void editIcon(Icon i){
        currentIcon = i;
        
    }
    
    public void commitUpdates(ActionEvent ev){
        SystemCoordinator sc = getSystemCoordinator();
        try {
            sc.updateIcon(currentIcon);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully updated icon", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not update icon, sorry", ""));
        }
        refreshIconList();
    }
    
    public void commitInsert(ActionEvent ev){
        SystemCoordinator sc = getSystemCoordinator();
        try {
            sc.insertIcon(currentIcon);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Icon inserted", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not insert icon, sorry", ""));
        }
        refreshIconList();
    }
    
    public void commitRemove(ActionEvent ev) {
        SystemCoordinator sc = getSystemCoordinator();
        if(currentIcon.getIconID() > 0){             
            try {
                int uses = sc.iconCheckForUse(currentIcon);
                if(uses == 0){
                    sc.deactivateIcon(currentIcon);
                    getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Success! Icon removed", ""));
                } else {
                    getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Icon is in use " + uses + " times. Could not remove", ""));
                }
            } catch (IntegrationException ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Could not remove Icon, sorry", ""));
            }
            refreshIconList();
        } else {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Invalid IconID: " + currentIcon.getIconID(), ""));
        }
    }
    
    public void createNewIcon(){
        Icon i = new Icon();
        currentIcon = i;
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

    /**
     * @return the currentIcon
     */
    public Icon getCurrentIcon() {
        return currentIcon;
    }

    /**
     * @param currentIcon the currentIcon to set
     */
    public void setCurrentIcon(Icon currentIcon) {
        this.currentIcon = currentIcon;
    }
    
}
