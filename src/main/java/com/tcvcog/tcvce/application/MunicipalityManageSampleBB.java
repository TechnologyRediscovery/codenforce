/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.MunicipalityCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Municipality;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

/**
 *
 * @author sylvia
 */
public class MunicipalityManageSampleBB extends BackingBeanUtils implements Serializable {

    //******************************************************//
    //******************* Code Template ********************//
    //******************************************************//
    public MunicipalityManageSampleBB() {

    }

    @PostConstruct
    public void initBean() {

        //initialize default current mode : Lookup
        currentMode = "Lookup";
        //initialize default setting
        defaultSetting();

    }

    private String currentMode;

    public String getCurrentMode() {
        return currentMode;
    }

    /**
     *
     * @param currentMode Lookup, Insert, Update, Remove
     * @throws IntegrationException
     */
    public void setCurrentMode(String currentMode) throws IntegrationException {

        //store currentMode into tempCurMode as a temporary value, in case the currenMode equal null
        String tempCurMode = this.currentMode;
        //reset default setting every time the Mode has been selected 
        defaultSetting();
        //check the currentMode == null or not
        if (currentMode == null) {
            this.currentMode = tempCurMode;
        } else {
            this.currentMode = currentMode;
        }
        //show the current mode in p:messages box
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, this.currentMode + " Mode Selected", ""));

    }

    //check if current mode == Lookup
    public boolean getActiveLookupMode() {
        return "Lookup".equals(currentMode);
    }

    //check if current mode == Insert
    public boolean getActiveInsertMode() {
        return "Insert".equals(currentMode);
    }

    //check if current mode == Update
    public boolean getActiveUpdateMode() {
        return "Update".equals(currentMode);
    }

    //check if current mode == Remove
    public boolean getActiveRemoveMode() {
        return "Remove".equals(currentMode);
    }

    //Select button on side panel can only be used in either Lookup Mode or Update Mode
    public boolean getSelectedButtonActive() {
        return !("Lookup".equals(currentMode) || "Update".equals(currentMode));
    }

    /**
     * Initialize the whole page into default setting
     */
    public void defaultSetting() {
        try {
            //initialize default selecte button in list-column: false
            currentMuniSelected = false;
            //initialize default current basic muni list
            currentMuniList = getMuniList();
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Municipality Page Unsuccessfully Initialized", ""));
        }
    }

    public String onInsertButtonChange() {
        //show successfully inserting message in p:messages box
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully Insert Municipality", ""));
        //reminder: set muniManageSample in faces-config.xml
        //return to muniManage_sample.xhtml page
        return "muniManageSample";
    }

    public String onUpdateButtonChange() {
        //show successfully updating message in p:messages box
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully Update Municipality", ""));
        //reminder: set muniManageSample in faces-config.xml
        //return to muniManage_sample.xhtml page
        return "muniManageSample";
    }

    public String onRemoveButtonChange() {
        //show successfully updating message in p:messages box
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully Remove Municipality", ""));
        //reminder: set muniManageSample in faces-config.xml
        //return to muniManage_sample.xhtml page
        return "muniManageSample";
    }

    //******************************************************//
    //*********** Code specifically for Muni ***************//
    //******************************************************//
    private List<Municipality> currentMuniList;

    private boolean currentMuniSelected;

    public List<Municipality> getCurrentMuniList() {
        return currentMuniList;
    }

    /**
     *
     * @param currentMuniList
     */
    public void setCurrentMuniList(List<Municipality> currentMuniList) {
        this.currentMuniList = currentMuniList;
    }

    public boolean isCurrentMuniSelected() {
        return currentMuniSelected;
    }

    /**
     *
     * @param currentMuniSelected
     */
    public void setCurrentMuniSelected(boolean currentMuniSelected) {
        this.currentMuniSelected = currentMuniSelected;
    }

    /**
     * Getting basic municipality list in terms of session user
     *
     * @return
     * @throws IntegrationException
     */
    public List<Municipality> getMuniList() throws IntegrationException {
        MunicipalityCoordinator mc = getMuniCoordinator();
        return mc.getPermittedMunicipalityListForAdminMuniAssignment(getSessionBean().getSessUser());
    }

    /**
     * Changing of muni item being selected and not being selected
     *
     * @param currentMuniCode
     * @throws IntegrationException
     * @throws AuthorizationException
     */
    public void onMuniSelectedButtonChange(int currentMuniCode) throws IntegrationException, AuthorizationException {

        MunicipalityCoordinator mc = getMuniCoordinator();

        // "Select" button was selected
        if (currentMuniSelected == true) {
            currentMuniList = new ArrayList<>();
            currentMuniList.add(mc.getMuni(currentMuniCode));
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Current Selected Municipality: " + mc.getMuni(currentMuniCode).getMuniName(), ""));
            // "Select" button wasn't selected
        } else {
            defaultSetting();
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Default Selected Municipality: " + mc.getMuni(currentMuniCode).getMuniName(), ""));
        }

    }

}
