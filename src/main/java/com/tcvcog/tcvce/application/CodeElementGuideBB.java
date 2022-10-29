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
import com.tcvcog.tcvce.entities.CodeElementGuideEntry;
import com.tcvcog.tcvce.entities.CodeSource;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.event.ActionEvent;

/** 
 * Backing bean for managing code guide entries
 * @author ellen bascomb of apt 31y
 */
public class CodeElementGuideBB extends BackingBeanUtils implements Serializable {
      
    private CodeElementGuideEntry currentGuideEntry;
    private boolean editModeGuideEntry;
    private List<CodeElementGuideEntry> codeGuideList;
    private List<CodeElementGuideEntry> filteredEntryList;
    
    private List<CodeSource> codeSourceList;
    private CodeSource codeSourceSelected;
    private List<CodeElement> elementList;
    private List<CodeElement> elementListFiltered;
    
    
    
    
    /**
     * Creates a new instance of CodeElementGuide
     */
    public CodeElementGuideBB() {
    }
    
    
     /**
     * Sets up our master lists and option drop downs
     */
    @PostConstruct
    public void initBean() {
        CodeCoordinator cc = getCodeCoordinator();
        try {
            setCodeSourceList(cc.getCodeSourceList());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        elementListFiltered = new ArrayList<>();
        filteredEntryList = new ArrayList<>();
        setEditModeGuideEntry(false);
        
    }
    
    /**
     * Listener for user requests to view the code guide
     * @param ev 
     */
    public void onViewCodeGuideNavLinkClick(ActionEvent ev){
        System.out.println("CodeElementGuideBB.onViewCodeGuideNavLinkClick");
        refreshFullCodeGuideAndUpdateSession();
        
    }
    
    /**
     * Listener for user requests to turn editing on or off for a guide 
     * entry
     * @param ev 
     */
    public void onToggleEditModeCurrentGuideEntryButtonPress(ActionEvent ev){
        toggleEditModeCurrentGuideEntry();
     
    }
    
    /**
     * Internal method for responding to edit toggle: if we have a new guide entry
     * then we insert it, if it's exiting, then we update it, and flip edit mode
     */
    private void toggleEditModeCurrentGuideEntry(){
        
         CodeCoordinator cc = getCodeCoordinator();
        try {
            if (isEditModeGuideEntry()) {
                if (getCurrentGuideEntry() == null) {
                    throw new BObStatusException("Cannot edit a null current guide entry");
                }
                if(getCurrentGuideEntry().getGuideEntryID() == 0){
                    cc.insertCodeElementGuideEntry(getCurrentGuideEntry());
                } else {
                    cc.updateCodeElementGuideEntry(getCurrentGuideEntry());
                }
                refreshCurrentGuideEntry();
                refreshFullCodeGuideAndUpdateSession();

            } else {
                getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_INFO, 
                               "You are now editing guide entry ID " + getCurrentGuideEntry().getGuideEntryID(), ""));
            }
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
        }
        // do the toggle
        setEditModeGuideEntry(!isEditModeGuideEntry());
    }
    
    public void refreshCurrentGuideEntry(){
        if(getCurrentGuideEntry() != null){
            CodeCoordinator cc = getCodeCoordinator();
            try {
                setCurrentGuideEntry(cc.getCodeElementGuideEntry(getCurrentGuideEntry().getGuideEntryID()));
            } catch (BObStatusException | IntegrationException ex) {
                System.out.println(ex);
                
            } 
        }
    }
    
    /**
     * Gets a fresh copy of the whole code guide and injects into session
     */
    public void refreshFullCodeGuideAndUpdateSession(){
        CodeCoordinator cc = getCodeCoordinator();
        try {
            codeGuideList = cc.getCodeElementGuideEntryListComplete();
            getSessionBean().setSessCodeGuideList(codeGuideList);
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }
    
    
    /**
     * Listener for user requests to make a new guide entry
     * @param ev
     */
    public void onCodeElementGuideEntryCreateInitButtonPress(ActionEvent ev){
        CodeCoordinator cc = getCodeCoordinator();
        setCurrentGuideEntry(cc.getCodeElementGuideEntrySkeleton());
        setEditModeGuideEntry(true);
    }
    
    /**
     * listener for user requests to either view, edit, or remove a guide
     * entry
     * @param cege to edit, view, or delete
     */
    public void onCodeElementGuideEntryViewRemoveInitLinkClick(CodeElementGuideEntry cege){
        setCurrentGuideEntry(cege);
    }
    
    /**
     * Listener for user requests to stop the code guide element
     * update process
     * @param ev 
     */
    public void onEditOperationAbort(ActionEvent ev){
        editModeGuideEntry = false;
        System.out.println("CodeElementGuideBB.onEditOperationAbort");
    }
    

    /**
     * Listener to start the guide guide mapping process
     * @param ev 
     */
    public void onLinkElementsToGuideInitButtonPush(ActionEvent ev){
         elementList = new ArrayList<>();
    }
    
    public void onGetElementsInSourceButtonClick(ActionEvent ev){
        refreshElementsInSource();
    }
    
    /**
     * Listener for user requests to get elements from a given source
     * @param ev 
     */
    public void refreshElementsInSource(){
        System.out.println("CodeGuideEntryBB.refreshElementsInSource");
        if(codeSourceSelected != null){
            System.out.println("CodeGuideEntryBB.refreshElementsInSource | source: " + codeSourceSelected.getSourceName());
            CodeCoordinator cc = getCodeCoordinator();
            try {
                elementList = cc.getCodeElemements(codeSourceSelected);
                if(elementList != null){
                    System.out.println("CodeGuideEntryBB.refreshElementsInSource | eleListSize: " + elementList.size());
                }
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Showing all ordinances in source: " + codeSourceSelected.getSourceName(), ""));
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fatal error: Could not load ordinances, sorry!", ""));
            }
        }
    }
    
    /**
     * Listener for user requests to update all the element categories in a batch
     */
    public void updateCodeGuideLinksOnAllElementsInSource(){
        CodeCoordinator cc = getCodeCoordinator();
        try {
            cc.updateAllCodeGuideEntryLinks(getElementList());
            refreshElementsInSource();
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Success! Updated all code categories", ""));
        } catch (IntegrationException | BObStatusException ex) {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
        }
    }
   
    
  
    
     /**
      * listener for user requests to remove a code guide entry
      */
    public void onCodeGuideEntryRemoveCommitButtonClick(){
        if(getCurrentGuideEntry() != null){
            CodeCoordinator cc = getCodeCoordinator();
            try {
                cc.removeCodeElementGuideEntry(getCurrentGuideEntry());
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Code guide entry nuked forever!", ""));
                setCurrentGuideEntry(null);
                refreshFullCodeGuideAndUpdateSession();
            } catch (IntegrationException | BObStatusException ex) {
                System.out.println(ex.toString());

                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            ex.getMessage(), ""));
            }
        } else {
                getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Please select a gode guide entry to delete it", ""));
        }
    }

    /**
     * @return the currentGuideEntry
     */
    public CodeElementGuideEntry getCurrentGuideEntry() {
        return currentGuideEntry;
    }

    /**
     * @return the editModeGuideEntry
     */
    public boolean isEditModeGuideEntry() {
        return editModeGuideEntry;
    }

    /**
     * @return the codeGuideList
     */
    public List<CodeElementGuideEntry> getCodeGuideList() {
        return codeGuideList;
    }

    /**
     * @return the filteredEntryList
     */
    public List<CodeElementGuideEntry> getFilteredEntryList() {
        return filteredEntryList;
    }

    /**
     * @return the codeSourceList
     */
    public List<CodeSource> getCodeSourceList() {
        return codeSourceList;
    }

    /**
     * @return the codeSourceSelected
     */
    public CodeSource getCodeSourceSelected() {
        return codeSourceSelected;
    }

    /**
     * @return the elementList
     */
    public List<CodeElement> getElementList() {
        return elementList;
    }

    /**
     * @return the elementListFiltered
     */
    public List<CodeElement> getElementListFiltered() {
        return elementListFiltered;
    }

    /**
     * @param currentGuideEntry the currentGuideEntry to set
     */
    public void setCurrentGuideEntry(CodeElementGuideEntry currentGuideEntry) {
        this.currentGuideEntry = currentGuideEntry;
    }

    /**
     * @param editModeGuideEntry the editModeGuideEntry to set
     */
    public void setEditModeGuideEntry(boolean editModeGuideEntry) {
        this.editModeGuideEntry = editModeGuideEntry;
    }

    /**
     * @param codeGuideList the codeGuideList to set
     */
    public void setCodeGuideList(List<CodeElementGuideEntry> codeGuideList) {
        this.codeGuideList = codeGuideList;
    }

    /**
     * @param filteredEntryList the filteredEntryList to set
     */
    public void setFilteredEntryList(List<CodeElementGuideEntry> filteredEntryList) {
        this.filteredEntryList = filteredEntryList;
    }

    /**
     * @param codeSourceList the codeSourceList to set
     */
    public void setCodeSourceList(List<CodeSource> codeSourceList) {
        this.codeSourceList = codeSourceList;
    }

    /**
     * @param codeSourceSelected the codeSourceSelected to set
     */
    public void setCodeSourceSelected(CodeSource codeSourceSelected) {
        this.codeSourceSelected = codeSourceSelected;
    }

    /**
     * @param elementList the elementList to set
     */
    public void setElementList(List<CodeElement> elementList) {
        this.elementList = elementList;
    }

    /**
     * @param elementListFiltered the elementListFiltered to set
     */
    public void setElementListFiltered(List<CodeElement> elementListFiltered) {
        this.elementListFiltered = elementListFiltered;
    }
    
   
    
}
