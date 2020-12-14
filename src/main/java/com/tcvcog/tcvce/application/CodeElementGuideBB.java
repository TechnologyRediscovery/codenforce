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
import com.tcvcog.tcvce.entities.CodeElementGuideEntry;
import com.tcvcog.tcvce.entities.CodeSource;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;

/** 
*
 * @author ellen bascomb of apt 31y
 */
public class CodeElementGuideBB extends BackingBeanUtils implements Serializable {
      
    private CodeElementGuideEntry currentGuideEntry;
    private List<CodeElementGuideEntry> entryList;
    private List<CodeElementGuideEntry> filteredEntryList;
    
    private CodeElementGuideEntry selectedGuideEntry;
    
    private CodeSource currentSource;
    
    
    private List<CodeElement> elementList;
    private List<CodeElement> filteredElementList;
    // no selected elements I don't think
    private CodeElement selectedElement;
    
    
    
    /**
     * Creates a new instance of CodeElementGuide
     */
    public CodeElementGuideBB() {
    }
    
    
    public String updateCodeGuideLinks(){
        CodeIntegrator ci = getCodeIntegrator();
        ListIterator<CodeElement> eleIterator = elementList.listIterator();
        CodeElement ce;
        int guideEntryID;
        
        while(eleIterator.hasNext()){
            ce = eleIterator.next();
            guideEntryID = ce.getGuideEntryID();
            if(guideEntryID != 0){
                try {
                    ci.linkElementToCodeGuideEntry(ce, guideEntryID );
                    getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Success! Linked guide entry ID " 
                                + guideEntryID + " to element ID " + ce.getElementID(), ""));
                } catch (IntegrationException ex) {
                    getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
                }
            }
        }
        return "";
    }
   
    
    private List<CodeElement> loadCodeElementList(){
        
        List<CodeElement> elList = null;
        CodeSource source = getSessionBean().getSessCodeSource();
        CodeIntegrator codeIntegrator = getCodeIntegrator();
        try {
            if(source != null){
                elList = codeIntegrator.getCodeElements(source.getSourceID());
            }
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Unable to populate list of code elements, sorry!", 
                            "This error will require administrator attention"));
        }
        return elList;
        
    }
    
     public List<CodeElement> getElementList() {
        if(elementList == null){
            elementList = loadCodeElementList();
        } 
        return elementList;
    }
    
    public String deleteCodeElementGuideEntry(){
        if(selectedGuideEntry != null){
            
            CodeElementGuideEntry cege = selectedGuideEntry;
            try {
                CodeIntegrator ci = getCodeIntegrator();
                ci.deleteCodeElementGuideEntry(cege);

                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Code guide entry nuked forever!", ""));
            } catch (IntegrationException ex) {
                System.out.println(ex.toString());

                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            ex.getLocalizedMessage(), 
                            "This must be corrected by the System Administrator"));
            }
            return "codeGuideView";
        } else {
                getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Please select a gode guide entry to delete it", ""));
            return "";
        }
    }
    
    public String updateSelectedGuideEntry(){
        if(selectedGuideEntry != null){
            getSessionBean().setActiveCodeElementGuideEntry(selectedGuideEntry);
            System.out.println("CodeElementGuideBB.updateSelectedGuideEntry | selectedGuideEntry: " + selectedGuideEntry.getDescription());
            return "codeGuideEntryUpdate";
        } else {
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Please select a gode guide entry to update it", ""));
            return "";
        }
    }
    
    public String addNewCodeGuideEntry(){
        return "codeGuideEntryAdd";
    }
    
    /**
     * @return the currentGuideEntry
     */
    public CodeElementGuideEntry getCurrentGuideEntry() {
        currentGuideEntry = getSessionBean().getActiveCodeElementGuideEntry();
        return currentGuideEntry;
    }

    /**
     * @return the entryList
     */
    public List<CodeElementGuideEntry> getEntryList() {
        System.out.println("CodeElementGuideBB.getEntryList");
        CodeIntegrator ci = getCodeIntegrator();
        if(entryList == null){
            try {
                entryList = ci.getCodeElementGuideEntries();
            } catch (IntegrationException ex) {
                System.out.println("CodeElementGuideBB.getEntryList | " + ex.getMessage());
            }
        }
        return entryList;
    }

    /**
     * @return the selectedGuideEntry
     */
    public CodeElementGuideEntry getSelectedGuideEntry() {
        return selectedGuideEntry;
    }

  

    /**
     * @param currentGuideEntry the currentGuideEntry to set
     */
    public void setCurrentGuideEntry(CodeElementGuideEntry currentGuideEntry) {
        this.currentGuideEntry = currentGuideEntry;
    }

    /**
     * @param entryList the entryList to set
     */
    public void setEntryList(ArrayList<CodeElementGuideEntry> entryList) {
        this.entryList = entryList;
    }

    /**
     * @param selectedGuideEntry the selectedGuideEntry to set
     */
    public void setSelectedGuideEntry(CodeElementGuideEntry selectedGuideEntry) {
        this.selectedGuideEntry = selectedGuideEntry;
    }

    /**
     * @return the filteredEntryList
     */
    public List<CodeElementGuideEntry> getFilteredEntryList() {
        return filteredEntryList;
    }

    /**
     * @param filteredEntryList the filteredEntryList to set
     */
    public void setFilteredEntryList(ArrayList<CodeElementGuideEntry> filteredEntryList) {
        this.filteredEntryList = filteredEntryList;
    }


    /**
     * @return the selectedElement
     */
    public CodeElement getSelectedElement() {
        return selectedElement;
    }

    /**
     * @param selectedElement the selectedElement to set
     */
    public void setSelectedElement(CodeElement selectedElement) {
        this.selectedElement = selectedElement;
    }

   
    /**
     * @param elementList the elementList to set
     */
    public void setElementList(ArrayList<CodeElement> elementList) {
        this.elementList = elementList;
    }

    /**
     * @return the filteredElementList
     */
    public List<CodeElement> getFilteredElementList() {
        return filteredElementList;
    }

    /**
     * @param filteredElementList the filteredElementList to set
     */
    public void setFilteredElementList(ArrayList<CodeElement> filteredElementList) {
        this.filteredElementList = filteredElementList;
    }

    /**
     * @return the currentSource
     */
    public CodeSource getCurrentSource() {
        currentSource = getSessionBean().getSessCodeSource();
        return currentSource;
    }

    /**
     * @param currentSource the currentSource to set
     */
    public void setCurrentSource(CodeSource currentSource) {
        this.currentSource = currentSource;
    }

   
    
}
