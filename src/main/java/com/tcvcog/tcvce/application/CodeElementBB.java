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
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CodeElement;
import com.tcvcog.tcvce.entities.CodeSource;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class CodeElementBB extends BackingBeanUtils implements Serializable{

     /**
     * Creates a new instance of CodeElementBB
     */
    public CodeElementBB() {
    }

    private List<CodeElement> codeElementList;
    private List<CodeElement> codeElementListFiltered;
    private CodeElement currentElement;

    private List<CodeSource> codeSourceList;
    private CodeSource currentCodeSource;
    
    private String formNoteText;
    
    

   
    @PostConstruct
    public void initBean() {
        CodeCoordinator cc = getCodeCoordinator();
        try {
            codeSourceList = cc.getCodeSourceList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
        if(getSessionBean().getSessCodeSource() != null){
            currentCodeSource = getSessionBean().getSessCodeSource();
            populateElementListByCurrentCodeSource();
        }
        
    }
    
    /**
     * Listener for user requests to view code elements by selecting a source
     * @param source 
     */
    public void onCodeSourceViewButtonChange(CodeSource source){
        currentCodeSource = source;
        getSessionBean().setSessCodeSource(source);
        populateElementListByCurrentCodeSource();
    }
    
    /**
     * Utility container for population logic
     */
    private void populateElementListByCurrentCodeSource(){
        
        CodeCoordinator cc = getCodeCoordinator();
        if(currentCodeSource == null) return;
        try {
            codeElementList = cc.getCodeElemements(currentCodeSource);
         getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Viewing ordinances in " + getCurrentCodeSource().getSourceName(),""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to load ordiance list",""));
            
        }
        
    }

    public String commitUpdatesToCodeElement() {
        CodeIntegrator integrator = getCodeIntegrator();

        // elemet ID is already in our currentElementObject

        try {
            integrator.updateCodeElement(currentElement);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Code element updated!", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to update code element; most sincere apologies!",
                            "This must be corrected by the System Administrator"));

        }

        return "codeElementList";
    }

    
    /**
     * Listener for user requests to remove a code element
     * @param ev 
     */
    public void onElementNukeInitButtonChange(ActionEvent ev){
        // nothing to do here yet
        
    }
    
    public String onElementNukeButtonChange() {
        CodeCoordinator cc = getCodeCoordinator();
        cc.deactivateCodeElement(currentElement, getSessionBean().getSessUser());
        return "codeElementManage";
    }

    public void onNoteInitButtonChange(ActionEvent ev){
        formNoteText = "";
        
    }
    
    public void onNoteCommitButtonChange(ActionEvent ev){
        
    }
    
    public void onElementAddInitButtonChange(ActionEvent ev){
        CodeCoordinator cc = getCodeCoordinator();
        currentElement = cc.getCodeElementSkeleton();
        
        
        
    }
    
    public void onElementUpdateInitButtonChange(CodeElement ele){
        currentElement = ele;
    }
    
    public void onElementUpdateCommitButtonChange(){
        
    }
    
    
    public String onElementAddCommitButtonChange() {
        CodeCoordinator cc = getCodeCoordinator();
        
        
        currentElement.setSource(currentCodeSource);

            cc.addCodeElement(currentElement, getSessionBean().getSessUser());
//            getFacesContext().addMessage(null,
//                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
//                            "Unable to add code elment to code source",
//                            "This must be corrected by the System Administrator"));

        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Successfully added code element to code source", ""));

        return "codeSourceManage";
    }

 

    /**
     * @param currentCodeSource the currentCodeSource to set
     */
    public void setCurrentCodeSource(CodeSource currentCodeSource) {
        this.currentCodeSource = currentCodeSource;
    }

    /**
     * @return the currentElement
     */
    public CodeElement getCurrentElement() {
        currentElement = getSessionBean().getActiveCodeElement();
        return currentElement;
    }

    /**
     * @param currentElement the currentElement to set
     */
    public void setCurrentElement(CodeElement currentElement) {
        this.currentElement = currentElement;
    }

 

    /**
     * Listener for user requests to begin creating a new code element
     */
    public void onElementAddInitButtonChange(){
        CodeCoordinator cc = getCodeCoordinator();
        currentElement = cc.getCodeElementSkeleton();
        
    }
    
   

    /**
     * @return the codeSourceList
     */
    public List<CodeSource> getCodeSourceList() {
        return codeSourceList;
    }

    /**
     * @param codeSourceList the codeSourceList to set
     */
    public void setCodeSourceList(List<CodeSource> codeSourceList) {
        this.codeSourceList = codeSourceList;
    }

    /**
     * @return the currentCodeSource
     */
    public CodeSource getCurrentCodeSource() {
        return currentCodeSource;
    }

    /**
     * @return the codeElementList
     */
    public List<CodeElement> getCodeElementList() {
        return codeElementList;
    }

    /**
     * @param codeElementList the codeElementList to set
     */
    public void setCodeElementList(List<CodeElement> codeElementList) {
        this.codeElementList = codeElementList;
    }

    /**
     * @return the formNoteText
     */
    public String getFormNoteText() {
        return formNoteText;
    }

    /**
     * @param formNoteText the formNoteText to set
     */
    public void setFormNoteText(String formNoteText) {
        this.formNoteText = formNoteText;
    }

    /**
     * @return the codeElementListFiltered
     */
    public List<CodeElement> getCodeElementListFiltered() {
        return codeElementListFiltered;
    }

    /**
     * @param codeElementListFiltered the codeElementListFiltered to set
     */
    public void setCodeElementListFiltered(List<CodeElement> codeElementListFiltered) {
        this.codeElementListFiltered = codeElementListFiltered;
    }
}
