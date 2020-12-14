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
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 * Backing bean for codeElementManage.xhtml
 * Provides facilities for adding, updating, and nuking 
 * Code Sources
 * Code Elements (Ordinances)
 * Code Guide records
 * 
 * @author ellen bascomb of apt 31y
 */
public class CodeElementBB 
        extends BackingBeanUtils 
        implements Serializable{


    final String FACESPAGE_NAV_CODEELEMENTMANAGE = "codeElementManage";
    
    private List<CodeElement> codeElementList;
    private List<CodeElement> codeElementListFiltered;
    private CodeElement currentElement;

    private List<CodeSource> codeSourceList;
    private CodeSource currentCodeSource;
    
    private String formNoteText;

    /**
     * Creates a new instance of CodeElementBB
     */
    public CodeElementBB() {
    }
    
   /**
    * Initializes the bean
    */
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
    
    
    // *************************************************************
    // *********************CODE SOURCES****************************
    // *************************************************************
    
    
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
     * Listener for user requests to start a source udpate operation
     * @param source 
     */
    public void onCodeSourceUpdateInitButtonChange(CodeSource source){
        currentCodeSource = source;
        
        
    }
    
    /**
     * Listener for user requests to finalize a source update operation
     * @return 
     */
    public String onCodeSourceUpdateCommitButtonChange(){
        CodeCoordinator cc = getCodeCoordinator();
        try {
            cc.updateCodeSource(currentCodeSource);
            getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_INFO,
                               "Updated code source: " + currentCodeSource.getSourceName(),""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                               "Unable to update code source; This is fatal error.",""));
            return "";
        }
        
        return FACESPAGE_NAV_CODEELEMENTMANAGE;
        
        
    }
    
    /**
     * Listener for user requests to initiate a source remove operation
     * @param source 
     */
    public void onCodeSourceNukeInitButtonChange(CodeSource source){
        currentCodeSource = source;
    }
    
    
    /**
     * Listener for user requests to finalize a source removal operation
     * @return 
     */
    public String onCodeSourceNukeCommitButtonChange(){
        
        CodeCoordinator cc = getCodeCoordinator();
        try {
            cc.deactivateCodeSource(currentCodeSource);
            getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_INFO,
                               "Poof! Code source " + currentCodeSource.getSourceName() + " has been Nuked!"
                                + "",""));
            
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                               "Unable to nuke code source, "
                                + "probably because it is pointed to by one or more code elements.",""));
            return "";
                        

        }
        return FACESPAGE_NAV_CODEELEMENTMANAGE;
        
    }
    
    /**
     * Listener for user requests to start the source creation operation
     */
    public void onCodeSourceAddInitButtonChange(){
        CodeCoordinator cc = getCodeCoordinator();
        currentCodeSource = cc.getCodeSourceSkeleton();
        
    }
    
    /**
     * Listener for user requests to finalize the source creation process
     * @return 
     */
    public String onCodeSourceAddCommitButtonChange(){
        CodeCoordinator cc = getCodeCoordinator();
        try {
            cc.addNewCodeSource(currentCodeSource);
            getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_INFO,
                               "Succcessfully added new code source!",""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                               "Unable to nuke code source, "
                                + "probably because it is pointed to by one or more code elements.",""));
            return "";
            
        }
        return FACESPAGE_NAV_CODEELEMENTMANAGE;
        
    }
    
    
    
    
    
    
    // *************************************************************
    // **************CODE ELEMENTS (ORDINANCES)*********************
    // *************************************************************
   
    
    /**
     * Utility container for population logic
     */
    private void populateElementListByCurrentCodeSource(){
        
        CodeCoordinator cc = getCodeCoordinator();
        if(currentCodeSource == null) return;
        try {
            codeElementList = cc.getCodeElemements(currentCodeSource);
            if(codeElementList != null && !codeElementList.isEmpty()){
                System.out.println("Loaded code element list of size: " + codeElementList.size());
            }
            System.out.println("");
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
        CodeCoordinator cc = getCodeCoordinator();
        // elemet ID is already in our currentElementObject

        try {
            cc.updateCodeElement(currentElement, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Code element updated!", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to update code element; most sincere apologies!",
                            "This must be corrected by the System Administrator"));
            return "";

        }

        return FACESPAGE_NAV_CODEELEMENTMANAGE;
    }

    
    /**
     * Listener for user requests to remove a code element
     * @param ele
     
     */
    public void onElementNukeInitButtonChange(CodeElement ele){
       currentElement = ele;
        
    }
    
    public String onElementNukeButtonChange() {
     
        CodeCoordinator cc = getCodeCoordinator();
        try {
            cc.deactivateCodeElement(currentElement, getSessionBean().getSessUser());
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Deactivated code element " + currentElement.getElementID(),
                            ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to deactivate element!",
                            "This is probably because it has been included in a code book"));
        }
        return FACESPAGE_NAV_CODEELEMENTMANAGE;
    }
    
    
    
    public void onElementAddInitButtonChange(ActionEvent ev){
        if(currentCodeSource != null){
            
            CodeCoordinator cc = getCodeCoordinator();
            currentElement = cc.getCodeElementSkeleton();
            System.out.println("CodeElementBB.onElementUpdateInitButtonChange:  Updating " + currentElement.getElementID() );
        } 
        
    }
    
    public void onElementUpdateInitButtonChange(CodeElement ele){
        System.out.println("CodeElementBB.onElementUpdateInitButtonChange:  Updating " + ele.getElementID() );
        currentElement = ele;
        System.out.println("test breakpoint");
    }
    
    
    
    public String onElementUpdateCommitButtonChange(){
         CodeCoordinator cc = getCodeCoordinator();
        try {
            cc.updateCodeElement(currentElement, getSessionBean().getSessUser());
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Updated code element " + currentElement.getElementID(),
                            ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to update code element; most sincere apologies!",
                            "This must be corrected by the System Administrator"));
        }
        return FACESPAGE_NAV_CODEELEMENTMANAGE;
    }
    
    
    public String onElementAddCommitButtonChange() {
        CodeCoordinator cc = getCodeCoordinator();
        
        
        currentElement.setSource(currentCodeSource);

        try {
            cc.addCodeElement(currentElement, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Added new code element",""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to add new element", ""));
        }

        return FACESPAGE_NAV_CODEELEMENTMANAGE;
    }
    
    

    
    // *************************************************************
    // *************************** NOTES ***************************
    // *************************************************************
   
    
    
    public void onNoteInitButtonChange(ActionEvent ev){
        formNoteText = "";
        
    }
    
    public void onNoteCommitButtonChange(ActionEvent ev){
        
    }
    

 
    
    // *************************************************************
    // ******************* GETTERS AND SETTERS *********************
    // *************************************************************
   

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
        
        return currentElement;
    }

    /**
     * @param currentElement the currentElement to set
     */
    public void setCurrentElement(CodeElement currentElement) {
        this.currentElement = currentElement;
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
