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
import com.tcvcog.tcvce.entities.CodeElement;
import com.tcvcog.tcvce.entities.CodeSet;
import com.tcvcog.tcvce.entities.CodeSource;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import java.io.Serializable;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Eric C. Darsow
 */
public class CodeSetBuilderBB extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of CodeSetBuilderBB
     */
    public CodeSetBuilderBB() {
        formMaxPenalty = 1000;
        formMinPenalty = 10;
        formNormPenalty = 50;
        formPenaltyNotes = "Default value";
        formNormDaysToComply = 30;
        formDaysToComplyNotes = "Default value";

    }

    private ArrayList<CodeSource> codeSourceList;
    private int selectedCodeSourceID;
    private HashMap<String, CodeSource> codeSourceMap;

    private CodeSet currentCodeSet;

    private ArrayList<CodeElement> codeElementList;
    private ArrayList<CodeElement> selectedElementsToAddToSet;

    // default data values for all added EnforcableCodeElements
    private double formMaxPenalty;
    private double formMinPenalty;
    private double formNormPenalty;
    private String formPenaltyNotes;
    private int formNormDaysToComply;
    private String formDaysToComplyNotes;

    // memebers for editing existing enforcablecodeelements 
    private EnforcableCodeElement selectedECE;

    public void retrieveCodeElementsFromSelectedSource(ActionEvent event) {
        System.out.println("CodeSetBuilderBB.retrieveCodeElementsFromSelectedSource | Start of method");
        CodeIntegrator integrator = getCodeIntegrator();
        try {
            System.out.println("CodeSetBuilderBB.retrieveCodeElementsFromSelectedSource | selected source: " + selectedCodeSourceID);
            codeElementList = integrator.getCodeElements(selectedCodeSourceID);
        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to find any code elements in the selected source, sorry.", ""));
        }
    }

    public String addElementsToCodeSet() {
        if (selectedElementsToAddToSet != null) {
            CodeIntegrator ci = getCodeIntegrator();
            EnforcableCodeElement ece;
            ListIterator<CodeElement> iterator = selectedElementsToAddToSet.listIterator();
            while (iterator.hasNext()) {
                ece = new EnforcableCodeElement();
                // gets the next code element in the selected list
                ece.setCodeElement(iterator.next());
                ece.setMaxPenalty(formMaxPenalty);
                ece.setMinPenalty(formMinPenalty);
                ece.setNormPenalty(formNormPenalty);
                ece.setPenaltyNotes(formPenaltyNotes);
                ece.setNormDaysToComply(formNormDaysToComply);
                ece.setDaysToComplyNotes(formDaysToComplyNotes);
                try {
                    ci.addEnforcableCodeElementToCodeSet(ece, currentCodeSet.getCodeSetID());
                } catch (IntegrationException ex) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
                }
            }
        } else {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please select at least one element from this source to add to the current code set", ""));
        }
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Success! Added " 
                        + selectedElementsToAddToSet.size() + " elements to code set: " 
                        + currentCodeSet.getCodeSetName(), ""));
        return "";
    }

    public String nukeCodeSetElement() {
        CodeIntegrator ci = getCodeIntegrator();
        try {
            ci.deleteEnforcableCodeElementFromCodeSet(selectedECE);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Success: Removed Enf. Code Element no. " 
                                    + selectedECE.getCodeSetElementID() + " from the code set", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
        }
        return "";
    }

    public String viewCodeSetElementsInSet() {

        getSessionBean().setActiveCodeSet(currentCodeSet);
        if (currentCodeSet != null) {
            //System.out.println("CodeSetBB.buildCodeSet | selected set: " + selectedCodeSet.getCodeSetName());
            return "codeSetBuilder";
        } else {
            return "";
        }
    }

    /**
     * @return the currentCodeSet
     */
    public CodeSet getCurrentCodeSet() {
        CodeIntegrator ci = getCodeIntegrator();
        CodeSet cs = getSessionBean().getActiveCodeSet();
        try {
            currentCodeSet = ci.getCodeSetBySetID(cs.getCodeSetID());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        return currentCodeSet;
    }

    /**
     * @param currentCodeSet the currentCodeSet to set
     */
    public void setCurrentCodeSet(CodeSet currentCodeSet) {
        this.currentCodeSet = currentCodeSet;
    }

    /**
     * @return the codeSourceList
     */
    public ArrayList<CodeSource> getCodeSourceList() {
        return codeSourceList;
    }

    /**
     * @param codeSourceList the codeSourceList to set
     */
    public void setCodeSourceList(ArrayList<CodeSource> codeSourceList) {
        this.codeSourceList = codeSourceList;
    }

    /**
     * @return the codeElementList
     */
    public ArrayList<CodeElement> getCodeElementList() {
        return codeElementList;
    }

    /**
     * @param codeElementList the codeElementList to set
     */
    public void setCodeElementList(ArrayList<CodeElement> codeElementList) {
        this.codeElementList = codeElementList;
    }

    /**
     * @return the codeSourceMap
     */
    public HashMap getCodeSourceMap() {

        System.out.println("CodeSetBuilderBB.getCodeSourceMap");
        CodeIntegrator integrator = getCodeIntegrator();
        try {
            codeSourceMap = integrator.getCodeSourceMap();
        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to find code sources, sorry.", "This is an unwanted system error."));
        }
        return codeSourceMap;
    }

    /**
     * @param codeSourceMap the codeSourceMap to set
     */
    public void setCodeSourceMap(HashMap<String, CodeSource> codeSourceMap) {
        this.codeSourceMap = codeSourceMap;
    }

    /**
     * @return the selectedCodeSource
     */
    public int getSelectedCodeSource() {
        return selectedCodeSourceID;
    }

    /**
     * @param selectedCodeSource the selectedCodeSource to set
     */
    public void setSelectedCodeSource(int selectedCodeSource) {
        this.selectedCodeSourceID = selectedCodeSource;
    }

    

    /**
     * @return the formMaxPenalty
     */
    public double getFormMaxPenalty() {
        return formMaxPenalty;
    }

    /**
     * @return the formMinPenalty
     */
    public double getFormMinPenalty() {
        return formMinPenalty;
    }

    /**
     * @return the formNormPenalty
     */
    public double getFormNormPenalty() {
        return formNormPenalty;
    }

    /**
     * @return the formPenaltyNotes
     */
    public String getFormPenaltyNotes() {
        return formPenaltyNotes;
    }

    /**
     * @return the formNormDaysToComply
     */
    public int getFormNormDaysToComply() {
        return formNormDaysToComply;
    }

    /**
     * @return the formDaysToComplyNotes
     */
    public String getFormDaysToComplyNotes() {
        return formDaysToComplyNotes;
    }

    

    /**
     * @param formMaxPenalty the formMaxPenalty to set
     */
    public void setFormMaxPenalty(double formMaxPenalty) {
        this.formMaxPenalty = formMaxPenalty;
    }

    /**
     * @param formMinPenalty the formMinPenalty to set
     */
    public void setFormMinPenalty(double formMinPenalty) {
        this.formMinPenalty = formMinPenalty;
    }

    /**
     * @param formNormPenalty the formNormPenalty to set
     */
    public void setFormNormPenalty(double formNormPenalty) {
        this.formNormPenalty = formNormPenalty;
    }

    /**
     * @param formPenaltyNotes the formPenaltyNotes to set
     */
    public void setFormPenaltyNotes(String formPenaltyNotes) {
        this.formPenaltyNotes = formPenaltyNotes;
    }

    /**
     * @param formNormDaysToComply the formNormDaysToComply to set
     */
    public void setFormNormDaysToComply(int formNormDaysToComply) {
        this.formNormDaysToComply = formNormDaysToComply;
    }

    /**
     * @param formDaysToComplyNotes the formDaysToComplyNotes to set
     */
    public void setFormDaysToComplyNotes(String formDaysToComplyNotes) {
        this.formDaysToComplyNotes = formDaysToComplyNotes;
    }

    /**
     * @return the selectedCodeSourceID
     */
    public int getSelectedCodeSourceID() {
        return selectedCodeSourceID;
    }

    /**
     * @param selectedCodeSourceID the selectedCodeSourceID to set
     */
    public void setSelectedCodeSourceID(int selectedCodeSourceID) {
        this.selectedCodeSourceID = selectedCodeSourceID;
    }

    /**
     * @return the selectedElementsToAddToSet
     */
    public ArrayList<CodeElement> getSelectedElementsToAddToSet() {
        return selectedElementsToAddToSet;
    }

    /**
     * @param selectedElementsToAddToSet the selectedElementsToAddToSet to set
     */
    public void setSelectedElementsToAddToSet(ArrayList<CodeElement> selectedElementsToAddToSet) {
        this.selectedElementsToAddToSet = selectedElementsToAddToSet;
    }

    /**
     * @return the selectedECE
     */
    public EnforcableCodeElement getSelectedECE() {
        return selectedECE;
    }

    /**
     * @param selectedECE the selectedECE to set
     */
    public void setSelectedECE(EnforcableCodeElement selectedECE) {
        this.selectedECE = selectedECE;
    }

}
