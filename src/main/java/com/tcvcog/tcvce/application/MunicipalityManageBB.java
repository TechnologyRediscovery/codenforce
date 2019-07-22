/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

/**
 *
 * @author sylvia
 */
public class MunicipalityManageBB extends BackingBeanUtils implements Serializable {

    private Municipality currentMuni;
    
    private List<User> codeOfficerUserList;
    private Map<String, Integer> styleMap;
    
    
    
    /**
     * Creates a new instance of MunicipalityManageBB
     */
    public MunicipalityManageBB() {
    }
    
    @PostConstruct
    public void initBean(){
        UserIntegrator ui = getUserIntegrator();
        SystemIntegrator si = getSystemIntegrator();
        try {
            codeOfficerUserList = ui.getActiveCodeOfficerList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
        try {
            styleMap = si.getPrintStyleMap();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
        
    }
    
    public String updateMuni(){
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        try {
            mi.updateMuni(currentMuni);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Successfully updated municipality info!", ""));
            getSessionBean().setSessionMuni(mi.getMuni(currentMuni.getMuniCode()));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Unable to update municipality info due to a system error", 
                        "This could be because the code set"
                        + "ID or the code source ID you entered is not an "
                        + "actual database record ID. Check in the "
                        + "\"municipal code\" section to verify ID numbers"));
        }
        
        return "";
        
        
    }

    /**
     * @return the currentMuni
     */
    public Municipality getCurrentMuni() {
        currentMuni = getSessionBean().getSessionMuni();
        return currentMuni;
    }

    /**
     * @param currentMuni the currentMuni to set
     */
    public void setCurrentMuni(Municipality currentMuni) {
        this.currentMuni = currentMuni;
    }

    /**
     * @return the codeOfficerUserList
     */
    public List<User> getCodeOfficerUserList() {
        return codeOfficerUserList;
    }

    /**
     * @param codeOfficerUserList the codeOfficerUserList to set
     */
    public void setCodeOfficerUserList(List<User> codeOfficerUserList) {
        this.codeOfficerUserList = codeOfficerUserList;
    }

    /**
     * @return the styleMap
     */
    public Map<String, Integer> getStyleMap() {
        return styleMap;
    }

    /**
     * @param styleMap the styleMap to set
     */
    public void setStyleMap(Map<String, Integer> styleMap) {
        this.styleMap = styleMap;
    }
    
}
