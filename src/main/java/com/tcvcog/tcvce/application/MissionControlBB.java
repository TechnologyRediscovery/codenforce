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
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Eric C. Darsow
 */
public class MissionControlBB extends BackingBeanUtils implements Serializable {
    
    private User user;
    private Municipality currentMuni;
    private ArrayList<Municipality> muniList;
    private Municipality selectedMuni;
    
    /**
     * Creates a new instance of InitiateSessionBB
     */
    public MissionControlBB() {
    }
    
    public String switchMuni(){
        CodeIntegrator ci = getCodeIntegrator();
        getSessionBean().setActiveMuni(selectedMuni);
        try {
            getSessionBean().setActiveCodeSet(ci.getCodeSetBySetID(selectedMuni.getDefaultCodeSetID()));
        } catch (IntegrationException ex) {
            FacesContext facesContext = getFacesContext();
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    ex.getMessage(), ""));
        }
        System.out.println("MissionControlBB.switchMuni | selected muni: " + selectedMuni.getMuniName());
        FacesContext facesContext = getFacesContext();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Successfully switch your current municipality to: " + selectedMuni.getMuniName(), ""));
            
        return "missionControl";
    }
    
    public String jumpToPublicPortal(){
        return "publicPortal";
    }
    
    public String loginToMissionControl(){
        
        return "startInitiationProcess";
    }
    
    public String logout(){
        FacesContext context = getFacesContext();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        
        if (session != null) {

//            session.removeAttribute("dBConnection");
//            session.removeAttribute("codeCoordinator");
//            session.removeAttribute("codeIntegrator");
//            session.removeAttribute("municipalitygrator");
//            session.removeAttribute("personIntegrator");
//            session.removeAttribute("propertyIntegrator");
//            session.removeAttribute("cEActionRequestIntegrator");
//            session.removeAttribute("userIntegrator");
            session.invalidate();

            FacesContext facesContext = getFacesContext();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Logout Successful", ""));
            System.out.println("MissionControlBB.logout | Session invalidated");

        } else {
            FacesContext facesContext = getFacesContext();
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "ERROR: Unable to invalidate session.", "Your system administrator has been notified."));
        }
            return "logoutSequenceComplete";
    }

    

    /**
     * @return the user
     */
    public User getUser() {
        user = getFacesUser();
        if(user != null){
            System.out.println("MissionControlBB.getUser | facesUser: " + user.getFName());
        }
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * @return the currentMuni
     */
    public Municipality getCurrentMuni() {
        currentMuni = getSessionBean().getActiveMuni();
        return currentMuni;
    }

    /**
     * @param currentMuni the currentMuni to set
     */
    public void setCurrentMuni(Municipality currentMuni) {
        this.currentMuni = currentMuni;
    }

    /**
     * @return the muniList
     */
    public ArrayList<Municipality> getMuniList() {
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        try {
            muniList = mi.getCompleteMuniList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        return muniList;
    }

    /**
     * @param muniList the muniList to set
     */
    public void setMuniList(ArrayList<Municipality> muniList) {
        this.muniList = muniList;
    }

    /**
     * @return the selectedMuni
     */
    public Municipality getSelectedMuni() {
        return selectedMuni;
    }

    /**
     * @param selectedMuni the selectedMuni to set
     */
    public void setSelectedMuni(Municipality selectedMuni) {
        this.selectedMuni = selectedMuni;
    }
    
    

   
}
