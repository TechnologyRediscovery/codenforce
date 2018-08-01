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
import com.tcvcog.tcvce.entities.CodeSet;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import java.io.Serializable;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Eric C. Darsow
 */
public class CodeSetBB extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of CodeSetBB
     */
    public CodeSetBB() {
    }

    private HashMap muniMap;
    private Municipality selectedMuni;
    private ArrayList<CodeSet> codeSetList;

    // used by codeSetElementManage
    private CodeSet selectedCodeSet;
    private EnforcableCodeElement selectedEnforcableCodeElement;

    private Map<String, Integer> codeSetMap;
    private Integer selectedCodeSetID;

    private int currentCodeSetID;
    private int selectedMuniCode;

    private String currentCodeSetName;
    private String currentCodeSetDescription;
    private int currentCodeSetMuniCode;
    private String currentCodeSetMuniName;

    private CodeSet setToUpdate;

    private String formCodeSetName;
    private String formCodeSetDescription;
    
    private int formNewMuniCode;
    private String formNewCodeSetName;
    private String formNewCodeSetDescription;


    public String manageCodeSetElements() {
        if (selectedCodeSetID != null) {
            CodeIntegrator ci = getCodeIntegrator();
            try {
                getSessionBean().setActiveCodeSet(ci.getCodeSetBySetID(selectedCodeSetID));
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
            return "codeSetElementList";
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Please select a code set to view", ""));
            return "";
        }
    }
    public String buildCodeSet() {

        if (selectedCodeSetID != null) {
            CodeIntegrator ci = getCodeIntegrator();
            try {
                getSessionBean().setActiveCodeSet(ci.getCodeSetBySetID(selectedCodeSetID));
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
            return "codeSetBuilder";
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Please select a code set to build ", ""));
            return "";
        }
    }

    public void commitUpdatesToCodeSet(ActionEvent event) {
        setToUpdate = new CodeSet();

        setToUpdate.setCodeSetID(selectedCodeSetID);
        setToUpdate.setCodeSetName(formCodeSetName);
        setToUpdate.setCodeSetDescription(formCodeSetDescription);

        CodeIntegrator codeInt = getCodeIntegrator();
        try {
            codeInt.updateCodeSetMetadata(setToUpdate);
        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to update code set, sorry.",
                            "This must be corrected by the System Administrator"));
        }
    }

    public void displaySelectedCodeSet(ActionEvent event) {
        if (selectedCodeSet != null) {
            formCodeSetName = selectedCodeSet.getCodeSetName();
            formCodeSetDescription = selectedCodeSet.getCodeSetDescription();

        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Hark! No code set selected--Please select a code set", ""));

        }
    }

    public void addNewCodeSet(ActionEvent event) {
        CodeSet cs = new CodeSet();
        cs.setMuniCode(formNewMuniCode);
        cs.setCodeSetName(formNewCodeSetName);
        cs.setCodeSetDescription(formNewCodeSetDescription);
        CodeIntegrator codeInt = getCodeIntegrator();

        try {
            codeInt.insertCodeSetMetadata(cs);
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Unable to add code set to DB", "Your fearless system administrator will need to correct this."));
        }
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "New Code Set named " + cs.getCodeSetName() + " has been added", ""));

//        return "";
    }

    public String makeSelectedCodeSetActive() {

        if (selectedCodeSet != null) {
            getSessionBean().setActiveCodeSet(selectedCodeSet);
            return "missionControl";
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Please select a code set to make your active set", ""));
            return "";
        }
    }

    /**
     * @return the muniMap
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public HashMap getMuniMap() throws IntegrationException {
        MunicipalityIntegrator muniInt = getMunicipalityIntegrator();
        muniMap = muniInt.getMunicipalityMap();
        return muniMap;
    }

    /**
     * @param muniMap the muniMap to set
     */
    public void setMuniMap(HashMap muniMap) {
        this.muniMap = muniMap;
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

    /**
     * @return the selectedMuniCode
     */
    public int getSelectedMuniCode() {
        return selectedMuniCode;
    }

    /**
     * @param selectedMuniCode the selectedMuniCode to set
     */
    public void setSelectedMuniCode(int selectedMuniCode) {
        this.selectedMuniCode = selectedMuniCode;
    }

    /**
     * @return the currentCodeSetID
     */
    public int getCurrentCodeSetID() {
        return currentCodeSetID;
    }

    /**
     * @param currentCodeSetID the currentCodeSetID to set
     */
    public void setCurrentCodeSetID(int currentCodeSetID) {
        this.currentCodeSetID = currentCodeSetID;
    }

    /**
     * @return the currentCodeSetName
     */
    public String getCurrentCodeSetName() {
        return currentCodeSetName;
    }

    /**
     * @param currentCodeSetName the currentCodeSetName to set
     */
    public void setCurrentCodeSetName(String currentCodeSetName) {
        this.currentCodeSetName = currentCodeSetName;
    }

    /**
     * @return the currentCodeSetDescription
     */
    public String getCurrentCodeSetDescription() {
        return currentCodeSetDescription;
    }

    /**
     * @param currentCodeSetDescription the currentCodeSetDescription to set
     */
    public void setCurrentCodeSetDescription(String currentCodeSetDescription) {
        this.currentCodeSetDescription = currentCodeSetDescription;
    }

    /**
     * @return the currentCodeSetMuniCode
     */
    public int getCurrentCodeSetMuniCode() {
        return currentCodeSetMuniCode;
    }

    /**
     * @param currentCodeSetMuniCode the currentCodeSetMuniCode to set
     */
    public void setCurrentCodeSetMuniCode(int currentCodeSetMuniCode) {
        this.currentCodeSetMuniCode = currentCodeSetMuniCode;
    }

    /**
     * @return the formCodeSetName
     */
    public String getFormCodeSetName() {
        if (selectedCodeSetID != null) {
            CodeIntegrator ci = getCodeIntegrator();
            try {
                formCodeSetName = ci.getCodeSetBySetID(selectedCodeSetID).getCodeSetName();
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
        }
        return formCodeSetName;
    }

    /**
     * @param formCodeSetName the formCodeSetName to set
     */
    public void setFormCodeSetName(String formCodeSetName) {
        this.formCodeSetName = formCodeSetName;
    }

    /**
     * @return the formCodeSetDescription
     */
    public String getFormCodeSetDescription() {
        if (selectedCodeSetID != null) {
            
            CodeIntegrator ci = getCodeIntegrator();
            try {
                formCodeSetDescription = ci.getCodeSetBySetID(selectedCodeSetID).getCodeSetDescription();
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
        }

        return formCodeSetDescription;
    }

    /**
     * @param formCodeSetDescription the formCodeSetDescription to set
     */
    public void setFormCodeSetDescription(String formCodeSetDescription) {
        this.formCodeSetDescription = formCodeSetDescription;
    }

    /**
     * @return the codeSetList
     */
    public ArrayList<CodeSet> getCodeSetList() {

        CodeIntegrator codeInt = getCodeIntegrator();
        try {
            codeSetList = codeInt.getCodeSets(selectedMuniCode);
        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to retrieve code sets by Muni Code, sorry.",
                            "This must be corrected by the System Administrator"));
        }

        return codeSetList;
    }

    /**
     * @param codeSetList the codeSetList to set
     */
    public void setCodeSetList(ArrayList<CodeSet> codeSetList) {
        this.codeSetList = codeSetList;
    }

    /**
     * @return the selectedCodeSet
     */
    public CodeSet getSelectedCodeSet() {
        CodeIntegrator ci = getCodeIntegrator();
        try {
            selectedCodeSet = ci.getCodeSetBySetID(selectedCodeSetID);
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        return selectedCodeSet;
    }

    /**
     * @param selectedCodeSet the selectedCodeSet to set
     */
    public void setSelectedCodeSet(CodeSet selectedCodeSet) {
        this.selectedCodeSet = selectedCodeSet;
    }

    /**
     * @return the currentCodeSetMuniName
     */
    public String getCurrentCodeSetMuniName() {
        return currentCodeSetMuniName;
    }

    /**
     * @param currentCodeSetMuniName the currentCodeSetMuniName to set
     */
    public void setCurrentCodeSetMuniName(String currentCodeSetMuniName) {
        this.currentCodeSetMuniName = currentCodeSetMuniName;
    }

    /**
     * @return the setToUpdate
     */
    public CodeSet getSetToUpdate() {
        return setToUpdate;
    }

    /**
     * @param setToUpdate the setToUpdate to set
     */
    public void setSetToUpdate(CodeSet setToUpdate) {
        this.setToUpdate = setToUpdate;
    }

    /**
     * @return the formNewMuniCode
     */
    public int getFormNewMuniCode() {
        return formNewMuniCode;
    }

    /**
     * @param formNewMuniCode the formNewMuniCode to set
     */
    public void setFormNewMuniCode(int formNewMuniCode) {
        this.formNewMuniCode = formNewMuniCode;
    }

    /**
     * @return the selectedEnforcableCodeElement
     */
    public EnforcableCodeElement getSelectedEnforcableCodeElement() {
        return selectedEnforcableCodeElement;
    }

    /**
     * @param selectedEnforcableCodeElement the selectedEnforcableCodeElement to
     * set
     */
    public void setSelectedEnforcableCodeElement(EnforcableCodeElement selectedEnforcableCodeElement) {
        this.selectedEnforcableCodeElement = selectedEnforcableCodeElement;
    }

    /**
     * @return the codeSetMap
     */
    public Map<String, Integer> getCodeSetMap() {
        CodeIntegrator ci = getCodeIntegrator();
        try {
            codeSetMap = ci.getSystemWideCodeSetMap();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        return codeSetMap;
    }

    /**
     * @param codeSetMap the codeSetMap to set
     */
    public void setCodeSetMap(Map<String, Integer> codeSetMap) {
        this.codeSetMap = codeSetMap;
    }

    /**
     * @return the selectedCodeSetID
     */
    public Integer getSelectedCodeSetID() {
        return selectedCodeSetID;
    }

    /**
     * @param selectedCodeSetID the selectedCodeSetID to set
     */
    public void setSelectedCodeSetID(Integer selectedCodeSetID) {
        this.selectedCodeSetID = selectedCodeSetID;
    }

    /**
     * @return the formNewCodeSetName
     */
    public String getFormNewCodeSetName() {
        return formNewCodeSetName;
    }

    /**
     * @return the formNewCodeSetDescription
     */
    public String getFormNewCodeSetDescription() {
        return formNewCodeSetDescription;
    }

    /**
     * @param formNewCodeSetName the formNewCodeSetName to set
     */
    public void setFormNewCodeSetName(String formNewCodeSetName) {
        this.formNewCodeSetName = formNewCodeSetName;
    }

    /**
     * @param formNewCodeSetDescription the formNewCodeSetDescription to set
     */
    public void setFormNewCodeSetDescription(String formNewCodeSetDescription) {
        this.formNewCodeSetDescription = formNewCodeSetDescription;
    }

}
