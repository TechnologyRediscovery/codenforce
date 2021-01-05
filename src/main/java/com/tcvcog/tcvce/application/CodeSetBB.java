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

import com.tcvcog.tcvce.coordinators.CodeCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CodeElement;
import com.tcvcog.tcvce.entities.CodeSet;
import com.tcvcog.tcvce.entities.CodeSource;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 * Backing bean for management of code sets (code books)
 * @author ellen bascomb of apt 31y
 */
public class CodeSetBB 
        extends BackingBeanUtils 
        implements Serializable {
    


    private List<CodeSet> codeSetList;
    private CodeSet currentCodeSet;
    
    private Municipality selectedMuniForCodeSet;
    private Municipality selectedMuniForCodeSetMapping;
    
    private List<EnforcableCodeElement> enforcableCodeElementListFiltered;
    private EnforcableCodeElement currentEnforcableCodeElement;
    
    private Map<Municipality, CodeSet> muniSetMap;
    
    private List<CodeSource> codeSourceList;
    private CodeSource selectedCodeSource;

    private List<CodeElement> codeElementList;
    private List<CodeElement> selectedElementsToAddToSet;
    
    
    /**
     * Creates a new instance of CodeSetBB
     */
    public CodeSetBB() {
    }
    
    @PostConstruct
    public void initBean(){
        CodeCoordinator cc = getCodeCoordinator();
        try {
            codeSetList = cc.getCodeSetListComplete();
            codeSourceList = cc.getCodeSourceList();
//            selectedMuniForCodeSet = getSessionBean().getSessMuni();
            muniSetMap = cc.getMuniCodeSetDefaultMap();
            codeElementList = new ArrayList<>();
            selectedElementsToAddToSet = new ArrayList<>();
            
            

            // if we have a set in the session, make it current on page load

            if(getSessionBean().getSessCodeSet() != null){
                currentCodeSet = getSessionBean().getSessCodeSet();
                currentCodeSet = cc.getCodeSet(currentCodeSet.getCodeSetID());

            }
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
        }
        
    }

    
    
    
    /**************************************************************************
    /****************** CODE SET RELATED LISTENERS ****************************
    /**************************************************************************
    
    
    /**
     * Listener for user requests to view elements in a code set (code book)
     * @param set 
     */
    public void onViewCodeSetButtonChange(CodeSet set) {
        currentCodeSet = set;
        getSessionBean().setSessCodeSet(currentCodeSet);
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Viewing Ordinances in Code Book " + currentCodeSet.getCodeSetName(), ""));

    }
    
    
    /**
     * Listener for user requests to start an update of a given code set
     * @param set 
     */
    public void onCodeSetUpdateInitButtonChange(CodeSet set){
        currentCodeSet = set;
        
        
    }
    
    /**
     * Listener for user request to finalize a code set update
     * @param event
     * @return 
     */
    public String onCodeSetUpdateCommitButtonChange() {
        CodeCoordinator cc = getCodeCoordinator();

        try {
            cc.updateCodeSetMetadata(currentCodeSet);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully update code book ID " + currentCodeSet.getCodeSetID(),
                            ""));
           return "codeSetManage";
            
        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to update code set, sorry.",
                            "This must be corrected by the System Administrator"));
            return "";
        }
    }
    
    

    
    /**
     * Listener for user requests to start a new codebook
     * @param ev 
     */
    public void onCodeSetAddInitButtonChange(ActionEvent ev){
        CodeCoordinator cc = getCodeCoordinator();
        currentCodeSet = cc.getCodeSetSkeleton();
        
        
    }

    /**
     * Listener for user requests to commit a new code book
     * @return 
     */
    public String onCodeSetAddCommitButtonChange() {
        int freshID;
        CodeCoordinator cc = getCodeCoordinator();
        try {
            freshID = cc.insertCodeSet(currentCodeSet);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "New Code Set ID" + freshID  + "has been added", ""));
            return "codeSetManage";
            
            
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to add code set to DB", "Your fearless system administrator will need to correct this."));
            return "";
        }

    }
    
    /**
     * Listener for user request to start the nuking process of a code set
     * 
     * @param set 
     */
    public void onCodeSetNukeInitButtonChange(CodeSet set){
        currentCodeSet = set;
        
        
    }
    
    /**
     * Listener for user requests to commit a code set nuke operation
     * @return 
     */
    public String onCodeSetNukeCommitButtonChange(){
        CodeCoordinator cc = getCodeCoordinator();
        try {
            cc.deactivateCodeSet(currentCodeSet);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Code set ID " + currentCodeSet.getCodeSetID() + " has beeen nuked forever!", ""));
            return "codeSetManage";
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Cannot nuke code set due to a database error that must be corrected by a admin", ""));
            return "";
        } catch (BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Cannot nuke code set because it is currently the default in one or more municipalities!", 
                            "Check the connections displayed on the main page and choose a new active code set for each municipality using this code set"));
            return "";
        }
    }
    
    /**
     * Listener for user request to connect a chosen municipality to a code set
     * @return 
     */
    public String onUpdateMuniCodeSetMapping(){
         CodeCoordinator cc = getCodeCoordinator();
        try {
            cc.activateCodeSetAsMuniDefault(currentCodeSet, selectedMuniForCodeSetMapping);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Code book: " + currentCodeSet.getCodeSetName()+ " is now the active book for " + selectedMuniForCodeSetMapping.getMuniName(), ""));
            return "codeSetManage";
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Cannot connect code book to municipality due to a database error that must be corrected by a admin", ""));
            return "";
        } catch (BObStatusException ex) { 
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
            
        }
        return "codeSetManage";
    }

    
    /**************************************************************************
    /****************** ENFORCABLE CODE ELEMENT METHODS ***********************
    /****************** roughly in order a user might call them ***************
    /**************************************************************************
   
   
    /**
     * Listener for user requests to start the addition process of ECEs to the 
     * current CodeSet
     * @param ev
     */
    public void onAddCodeElementsToCodeSetInitButtonChange(ActionEvent ev) {
        
       // nothing to do here yet
       
    }
     
    
   
    /**
     * Listener for user requests to grab all elements from a source
     * to be added to a code book
     * @param event 
     */
    public void retrieveCodeElementsFromSelectedSource(ActionEvent event) {
        CodeCoordinator cc = getCodeCoordinator();
        if(selectedCodeSource != null){
            
            try {
                codeElementList.clear();
                codeElementList.addAll(cc.getCodeElemements(selectedCodeSource));
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Loaded ordinances in code source: " + selectedCodeSource.getSourceName(), ""));
            } catch (IntegrationException ex) {
                System.out.println(ex.toString());
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to find any code elements in the selected source, sorry.", ""));
            }
        }
    }

    /**
     * Listener for user requests to add one or more elements to a code set
     * @return 
     */
    public String onAddSelectedElementsToCodeSetCommitButtonChange() {
        CodeCoordinator cc = getCodeCoordinator();
        if (selectedElementsToAddToSet != null && !selectedElementsToAddToSet.isEmpty()) {
            EnforcableCodeElement ece;
            
            for(CodeElement ele: selectedElementsToAddToSet){
                ece = cc.getEnforcableCodeElementSkeleton(ele);
                try {
                    cc.insertEnforcableCodeElement(ece, currentCodeSet, getSessionBean().getSessUser());
                } catch (IntegrationException | BObStatusException ex) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
                    return "codeSetManage";
                }
                
            } // close for over elements to add
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success! Added " 
                            + selectedElementsToAddToSet.size() + " elements to code set: " 
                            + currentCodeSet.getCodeSetName(), ""));
            return "codeSetManage";
            
        } else {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please select at least one element from this source to add to the current code set", ""));
            return "codeSetManage";
        }
    }
    
    /**
     * Listener to user requests to start the update operation on an ECE
     * @param ece 
     */
    public void onSetElementUpdateInitButtonChange(EnforcableCodeElement ece){
        currentEnforcableCodeElement = ece;
        
        
    }
    
    
    /**
     * Listener for user requests to commit an ece update operation
     * @return 
     */
    public String onSetElementUpdateCommitButtonChange(){
        CodeCoordinator cc = getCodeCoordinator();
        try {
            cc.updateEnforcableCodeElement(currentEnforcableCodeElement, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
              new FacesMessage(FacesMessage.SEVERITY_INFO,
                  "Successfully updated enforcability info on enforcable ordinance ID: " + currentEnforcableCodeElement.getCodeSetElementID(), ""));
            return "codeSetManage";
            
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
              getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.getMessage(), ""));
            return "codeSetManage";
        }
        
    }

    
    
    /**
     * Listener for user request to start the nuking operation on a code set ele
     * @param ece 
     */
    public void onNukeCodeSetElementInit(EnforcableCodeElement ece){
        currentEnforcableCodeElement = ece;
        
    }
    
    /**
     * Listener for user requests to commit a nuke operation on a code set ele
     * @return 
     */
    public String onNukeCodeSetElementCommit() {
        CodeCoordinator cc = getCodeCoordinator();
        try {
            System.out.println("CodeSetBB.onNukeCSECommit: Nuking element ID " + currentEnforcableCodeElement.getCodeSetElementID());
            cc.deactivateEnforcableCodeElement(currentEnforcableCodeElement, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Success: Removed Enf. Code Element no. " 
                                    + currentEnforcableCodeElement.getCodeSetElementID() + " from the code book", ""));
        } catch (IntegrationException | BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
        }
        return "codeSetManage";
    }

  

    
   
    
    
    
    /**************************************************************************
    /****************** GETTERS AND SETTERS: NO LOGIC HERE! *******************
    /**************************************************************************
    
    

     /**
     * @return the currentCodeSet
     */
    public CodeSet getCurrentCodeSet() {
       
        return currentCodeSet;
    }

    /**
     * @return the selectedMuniForCodeSet
     */
    public Municipality getSelectedMuniForCodeSet() {
        return selectedMuniForCodeSet;
    }

    /**
     * @param selectedMuniForCodeSet the selectedMuniForCodeSet to set
     */
    public void setSelectedMuniForCodeSet(Municipality selectedMuniForCodeSet) {
        this.selectedMuniForCodeSet = selectedMuniForCodeSet;
    }

   

  

   
   
    /**
     * @param csl
     */
    public void setCodeSetList(List<CodeSet> csl) {
        codeSetList = csl;
    }

   

    
    /**
     * @return the currentEnforcableCodeElement
     */
    public EnforcableCodeElement getCurrentEnforcableCodeElement() {
        return currentEnforcableCodeElement;
    }

    /**
     * @param currentEnforcableCodeElement the currentEnforcableCodeElement to
 set
     */
    public void setCurrentEnforcableCodeElement(EnforcableCodeElement currentEnforcableCodeElement) {
        this.currentEnforcableCodeElement = currentEnforcableCodeElement;
    }

   

    /**
     * @return the codeSourceList
     */
    public List<CodeSource> getCodeSourceList() {
        return codeSourceList;
    }

    /**
     * @return the selectedCodeSource
     */
    public CodeSource getSelectedCodeSource() {
        return selectedCodeSource;
    }

    /**
     * @return the codeElementList
     */
    public List<CodeElement> getCodeElementList() {
        return codeElementList;
    }

    /**
     * @return the selectedElementsToAddToSet
     */
    public List<CodeElement> getSelectedElementsToAddToSet() {
        return selectedElementsToAddToSet;
    }

    

    /**
     * @param currentCodeSet the currentCodeSet to set
     */
    public void setCurrentCodeSet(CodeSet currentCodeSet) {
        this.currentCodeSet = currentCodeSet;
    }

    /**
     * @param codeSourceList the codeSourceList to set
     */
    public void setCodeSourceList(List<CodeSource> codeSourceList) {
        this.codeSourceList = codeSourceList;
    }

    /**
     * @param selectedCodeSource the selectedCodeSource to set
     */
    public void setSelectedCodeSource(CodeSource selectedCodeSource) {
        this.selectedCodeSource = selectedCodeSource;
    }

    /**
     * @param codeElementList the codeElementList to set
     */
    public void setCodeElementList(List<CodeElement> codeElementList) {
        this.codeElementList = codeElementList;
    }

    /**
     * @param selectedElementsToAddToSet the selectedElementsToAddToSet to set
     */
    public void setSelectedElementsToAddToSet(List<CodeElement> selectedElementsToAddToSet) {
        this.selectedElementsToAddToSet = selectedElementsToAddToSet;
    }

    /**
     * @return the codeSetList
     */
    public List<CodeSet> getCodeSetList() {
        return codeSetList;
    }

    /**
     * @return the muniSetMap
     */
    public Map<Municipality, CodeSet> getMuniSetMap() {
        return muniSetMap;
    }

    /**
     * @param muniSetMap the muniSetMap to set
     */
    public void setMuniSetMap(Map<Municipality, CodeSet> muniSetMap) {
        this.muniSetMap = muniSetMap;
    }

    /**
     * @return the selectedMuniForCodeSetMapping
     */
    public Municipality getSelectedMuniForCodeSetMapping() {
        return selectedMuniForCodeSetMapping;
    }

    /**
     * @param selectedMuniForCodeSetMapping the selectedMuniForCodeSetMapping to set
     */
    public void setSelectedMuniForCodeSetMapping(Municipality selectedMuniForCodeSetMapping) {
        this.selectedMuniForCodeSetMapping = selectedMuniForCodeSetMapping;
    }

    /**
     * @return the enforcableCodeElementListFiltered
     */
    public List<EnforcableCodeElement> getEnforcableCodeElementListFiltered() {
        return enforcableCodeElementListFiltered;
    }

    /**
     * @param enforcableCodeElementListFiltered the enforcableCodeElementListFiltered to set
     */
    public void setEnforcableCodeElementListFiltered(List<EnforcableCodeElement> enforcableCodeElementListFiltered) {
        this.enforcableCodeElementListFiltered = enforcableCodeElementListFiltered;
    }

}
