/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Icon;
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
        SystemIntegrator si = getSystemIntegrator();
        try {
            iconList = si.getIconList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }
    
    public void editIcon(Icon i){
        currentIcon = i;
        
    }
    
    public void commitUpdates(ActionEvent ev){
        SystemIntegrator si = getSystemIntegrator();
        try {
            si.updateIcon(currentIcon);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully updated icon", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not update icon, sorry", ""));
        }
        
    }
    
    public void commitInsert(ActionEvent ev){
        SystemIntegrator si = getSystemIntegrator();
        try {
            si.insertIcon(currentIcon);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Icon inserted", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not insert icon, sorry", ""));
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
