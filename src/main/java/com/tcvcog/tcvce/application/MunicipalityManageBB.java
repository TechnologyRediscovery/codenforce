/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.MunicipalityCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CodeSet;
import com.tcvcog.tcvce.entities.CodeSource;
import com.tcvcog.tcvce.entities.MuniProfile;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.MunicipalityDataHeavy;
import com.tcvcog.tcvce.entities.PrintStyle;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

/**
 *
 * @author sylvia
 */
public class MunicipalityManageBB extends BackingBeanUtils implements Serializable {

    private Municipality currentMuni;

    private Map<String, Integer> styleMap;

    /**
     * Creates a new instance of MunicipalityManageBB
     */
    public MunicipalityManageBB() {
    }

    public String updateMuni() {
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
//        try {
//            mi.updateMuniComplete(currentMuni);
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Successfully updated municipality info!", ""));
//            getSessionBean().setSessionMuni(mi.getMuni(currentMuni.getMuniCode()));
//        } catch (IntegrationException ex) {
//            getFacesContext().addMessage(null,
//                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
//                "Unable to update municipality info due to a system error", 
//                        "This could be because the code set"
//                        + "ID or the code source ID you entered is not an "
//                        + "actual database record ID. Check in the "
//                        + "\"municipal code\" section to verify ID numbers"));
//        }

        return "";

    }

    /**
     * @return the currentMuni
     */
    public Municipality getCurrentMuni() {
        currentMuni = getSessionBean().getSessMuni();
        return currentMuni;
    }

    /**
     * @param currentMuni the currentMuni to set
     */
    public void setCurrentMuni(Municipality currentMuni) {
        this.currentMuni = currentMuni;
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

    //xiaohong edit
    private int currentMuniCode;

    private boolean currentMuniSelected;

    private MunicipalityDataHeavy currentMuniDataheavy;

    private Municipality currentMuniBasic;

    private List<Municipality> currentMuniList;

    private String currentMode;

    private ArrayList<CodeSet> currentCodeSetList;

    private ArrayList<CodeSource> currentCodeSourceList;

    private ArrayList<MuniProfile> currentMuniProfileList;

    private ArrayList<User> currentUserList;

    private ArrayList<PrintStyle> currentStyleList;

    @PostConstruct
    public void initBean() {

        //initialize default current mode : Lookup
        currentMode = "Lookup";
        //initialize default setting 
        defaultSetting();
    }

    /**
     * Initialize the whole page into default setting
     */
    public void defaultSetting() {

        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        MunicipalityCoordinator mc = getMuniCoordinator();
        CodeIntegrator ci = getCodeIntegrator();
        UserIntegrator ui = getUserIntegrator();
        SystemIntegrator si = getSystemIntegrator();

        try {

            //initialize default selecte button in list-column: false
            currentMuniSelected = false;
            //initialize default current basic muni list 
            currentMuniList = getMuniList();
            //initialize default current MunicipalityDataHeavy object in terms of current session muni
            currentMuniDataheavy = mc.getMuniDataHeavyList(getSessionBean().getSessMuni().getMuniCode());
            //initialize default current code set list 
            currentCodeSetList = ci.getCodeSets();
            //initialize default current code source list
            currentCodeSourceList = ci.getCompleteCodeSourceList();
            //initialize default current muni profile list
            currentMuniProfileList = mi.getMuniProfileList();
            //initialize default current user list
            currentUserList = ui.getUserList();
            //initialize default current print style list
            currentStyleList = si.getPrintStyle();

        } catch (AuthorizationException | IntegrationException ex) {

            //Message Noticefication
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Municipality Page Unsuccessfully Initialized", ""));

        }
    }

    /**
     *
     * @param currentMode Lookup, Insert, Update, Remove
     * @throws IntegrationException
     * @throws AuthorizationException
     */
    public void setCurrentMode(String currentMode) throws IntegrationException, AuthorizationException {

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
        //create an instance object of MunicipalityDataHeavy if current mode == "Insert"
        if (getActiveInsertMode()) {
            currentMuniDataheavy = new MunicipalityDataHeavy();
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
            
            //set current selected muni (basic)
            setCurrentMuniBasic(mc.getMuni(currentMuniCode));
            //update the current selected muni list in side panel
            currentMuniList = new ArrayList<>();
            currentMuniList.add(getCurrentMuniBasic());
            //set current selected muni (heavy)
            this.currentMuniCode = currentMuniCode;
            currentMuniDataheavy = mc.getMuniDataHeavyList(currentMuniCode);
            
            //Message Noticefication
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Current Selected Municipality: " + currentMuniDataheavy.getMuniName(), ""));

        // "Select" button wasn't selected
        } else {
            //turn to default setting
            defaultSetting();
            
            //Message Noticefication
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Default Selected Municipality: " + currentMuniDataheavy.getMuniName(), ""));
        }

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

    public String onUpdateButtonChange() {
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        try {
            mi.updateMuniDataHeavy(currentMuniDataheavy);
        } catch (IntegrationException ex) {
            System.out.println("update error!");
        }
        return "muniManage";
    }

    public String onInsertButtonChange() {
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        mi.insertMuniDataHeavy(currentMuniDataheavy);
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successful Insert New Municipality", ""));
        return "muniManage";
    }

    public int getCurrentMuniCode() {
        return currentMuniCode;
    }

    public void setCurrentMuniCode(int currentMuniCode) {
        this.currentMuniCode = currentMuniCode;
    }

    public boolean isCurrentMuniSelected() {
        return currentMuniSelected;
    }

    public void setCurrentMuniSelected(boolean currentMuniSelected) {
        this.currentMuniSelected = currentMuniSelected;
    }

    public MunicipalityDataHeavy getCurrentMuniDataheavy() {
        return currentMuniDataheavy;
    }

    public void setCurrentMuniDataheavy(MunicipalityDataHeavy currentMuniDataheavy) {
        this.currentMuniDataheavy = currentMuniDataheavy;
    }

    public Municipality getCurrentMuniBasic() {
        return currentMuniBasic;
    }

    public void setCurrentMuniBasic(Municipality currentMuniBasic) {
        this.currentMuniBasic = currentMuniBasic;
    }

    public List<Municipality> getCurrentMuniList() {
        return currentMuniList;
    }

    public void setCurrentMuniList(List<Municipality> currentMuniList) {
        this.currentMuniList = currentMuniList;
    }

    public String getCurrentMode() {
        return currentMode;
    }

    public ArrayList<CodeSet> getCurrentCodeSetList() {
        return currentCodeSetList;
    }

    public void setCurrentCodeSetList(ArrayList<CodeSet> currentCodeSetList) {
        this.currentCodeSetList = currentCodeSetList;
    }

    public ArrayList<CodeSource> getCurrentCodeSourceList() {
        return currentCodeSourceList;
    }

    public void setCurrentCodeSourceList(ArrayList<CodeSource> currentCodeSourceList) {
        this.currentCodeSourceList = currentCodeSourceList;
    }

    public ArrayList<MuniProfile> getCurrentMuniProfileList() {
        return currentMuniProfileList;
    }

    public void setCurrentMuniProfileList(ArrayList<MuniProfile> currentMuniProfileList) {
        this.currentMuniProfileList = currentMuniProfileList;
    }

    public ArrayList<User> getCurrentUserList() {
        return currentUserList;
    }

    public void setCurrentUserList(ArrayList<User> currentUserList) {
        this.currentUserList = currentUserList;
    }

    public ArrayList<PrintStyle> getCurrentStyleList() {
        return currentStyleList;
    }

    public void setCurrentStyleList(ArrayList<PrintStyle> currentStyleList) {
        this.currentStyleList = currentStyleList;
    }

}
