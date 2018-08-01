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
import com.tcvcog.tcvce.entities.CodeSource;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Eric C. Darsow
 */
public class CodeElementBB extends BackingBeanUtils implements Serializable{

    /**
     * Creates a new instance of CodeElementBB
     */
    public CodeElementBB() {
    }
    
    private CodeElement currentElement;
    
    private CodeSource activeCodeSource;
    
    private int formOrdChapterNo;
    
    private String formOrdChapterTitle;
    private String formOrdSecNum;
    private String formOrdSecTitle;
    
    private String formOrdSubSecNum;
    private String formOrdSubSecTitle;
    private String formOrdTechnicalText;
    
    private String formOrdHumanFriendlyText;
    private boolean formIsActive;
    
    private String formResourceURL;
    
    private int formGuideEntryID;
    
     public String commitUpdatesToCodeElement() {
        CodeIntegrator integrator = getCodeIntegrator();
        
        // elemet ID is already in our currentElementObject
        currentElement.setOrdchapterNo(formOrdChapterNo);
        
        currentElement.setOrdchapterTitle(formOrdChapterTitle);
        currentElement.setOrdSecNum(formOrdSecNum);
        currentElement.setOrdSecTitle(formOrdSecTitle);
        
        currentElement.setOrdSecNum(formOrdSecNum);
        currentElement.setOrdSubSecTitle(formOrdSubSecTitle);
        currentElement.setOrdTechnicalText(formOrdTechnicalText);
        
        currentElement.setOrdHumanFriendlyText(formOrdHumanFriendlyText);
        currentElement.setIsActive(formIsActive);
        
        currentElement.setResourceURL(formResourceURL);
        currentElement.setGuideEntryID(formGuideEntryID);
        
        
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
    
    public void deleteCodeElement(ActionEvent event){
        
        
    }
    
    
    
    public String insertCodeElement(){
        CodeIntegrator codeIntegrator = getCodeIntegrator();
        CodeElement newCE = new CodeElement();
        
        newCE.setSource(activeCodeSource);
        
        newCE.setOrdchapterNo(formOrdChapterNo);
        
        newCE.setOrdchapterTitle(formOrdChapterTitle);
        newCE.setOrdSecNum(formOrdSecNum);
        newCE.setOrdSecTitle(formOrdSecTitle);
        
        newCE.setOrdSubSecNum(formOrdSecNum);
        newCE.setOrdSubSecTitle(formOrdSubSecTitle);
        newCE.setOrdTechnicalText(formOrdTechnicalText);
        
        newCE.setOrdHumanFriendlyText(formOrdHumanFriendlyText);
        newCE.setIsActive(formIsActive);
        
        newCE.setResourceURL(formResourceURL);
        
        try {
            codeIntegrator.insertCodeElement(newCE);
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to add code elment to code source", 
                        "This must be corrected by the System Administrator"));
        }
        
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Successfully added code element to code source", ""));

        return "codeSourceManage";
    }
    

    /**
     * @return the formOrdChapterNo
     */
    public int getFormOrdChapterNo() {
        if(currentElement != null){
         formOrdChapterNo = currentElement.getOrdchapterNo();
        }
        return formOrdChapterNo;
    }

    /**
     * @param formOrdChapterNo the formOrdChapterNo to set
     */
    public void setFormOrdChapterNo(int formOrdChapterNo) {
        this.formOrdChapterNo = formOrdChapterNo;
    }

    /**
     * @return the formOrdChapterTitle
     */
    public String getFormOrdChapterTitle() {
        if(currentElement != null){
            formOrdChapterTitle = currentElement.getOrdchapterTitle();
        }
        return formOrdChapterTitle;
    }

    /**
     * @param formOrdChapterTitle the formOrdChapterTitle to set
     */
    public void setFormOrdChapterTitle(String formOrdChapterTitle) {
        this.formOrdChapterTitle = formOrdChapterTitle;
    }

    /**
     * @return the formOrdSecNum
     */
    public String getFormOrdSecNum() {
        if(currentElement != null){
            formOrdSecNum = currentElement.getOrdSecNum();
        }
        return formOrdSecNum;
    }

    /**
     * @param formOrdSecNum the formOrdSecNum to set
     */
    public void setFormOrdSecNum(String formOrdSecNum) {
        this.formOrdSecNum = formOrdSecNum;
    }

    /**
     * @return the formOrdSecTitle
     */
    public String getFormOrdSecTitle() {
        if(currentElement != null){
            formOrdSecTitle = currentElement.getOrdSecTitle();
        }
        return formOrdSecTitle;
    }

    /**
     * @param formOrdSecTitle the formOrdSecTitle to set
     */
    public void setFormOrdSecTitle(String formOrdSecTitle) {
        this.formOrdSecTitle = formOrdSecTitle;
    }

    /**
     * @return the formOrdSubSecNum
     */
    public String getFormOrdSubSecNum() {
        if(currentElement != null){
            formOrdSubSecNum = currentElement.getOrdSubSecNum();
        }
        return formOrdSubSecNum;
    }

    /**
     * @param formOrdSubSecNum the formOrdSubSecNum to set
     */
    public void setFormOrdSubSecNum(String formOrdSubSecNum) {
        this.formOrdSubSecNum = formOrdSubSecNum;
    }

    /**
     * @return the formOrdSubSecTitle
     */
    public String getFormOrdSubSecTitle() {
        if(currentElement != null){
         formOrdSubSecTitle = currentElement.getOrdSubSecTitle();
        }
        return formOrdSubSecTitle;
    }

    /**
     * @param formOrdSubSecTitle the formOrdSubSecTitle to set
     */
    public void setFormOrdSubSecTitle(String formOrdSubSecTitle) {
        this.formOrdSubSecTitle = formOrdSubSecTitle;
    }

    /**
     * @return the formOrdTechnicalText
     */
    public String getFormOrdTechnicalText() {
        if(currentElement != null){
            formOrdTechnicalText = currentElement.getOrdTechnicalText();
        }
        return formOrdTechnicalText;
    }

    /**
     * @param formOrdTechnicalText the formOrdTechnicalText to set
     */
    public void setFormOrdTechnicalText(String formOrdTechnicalText) {
        this.formOrdTechnicalText = formOrdTechnicalText;
    }

    /**
     * @return the formOrdHumanFriendlyText
     */
    public String getFormOrdHumanFriendlyText() {
        if(currentElement != null){
            formOrdHumanFriendlyText = currentElement.getOrdHumanFriendlyText();
        }
        return formOrdHumanFriendlyText;
    }

    /**
     * @param formOrdHumanFriendlyText the formOrdHumanFriendlyText to set
     */
    public void setFormOrdHumanFriendlyText(String formOrdHumanFriendlyText) {
        this.formOrdHumanFriendlyText = formOrdHumanFriendlyText;
    }

    

    /**
     * @return the formIsActive
     */
    public boolean isFormIsActive() {
        if(currentElement != null){
            formIsActive = currentElement.isIsActive();
        } else {
            formIsActive = true;
        }
        return formIsActive;
    }

    /**
     * @param formIsActive the formIsActive to set
     */
    public void setFormIsActive(boolean formIsActive) {
        this.formIsActive = formIsActive;
    }

    /**
     * @return the formResourceURL
     */
    public String getFormResourceURL() {
        if(currentElement != null){
            formResourceURL = currentElement.getResourceURL();
        }
        return formResourceURL;
    }

    /**
     * @param formResourceURL the formResourceURL to set
     */
    public void setFormResourceURL(String formResourceURL) {
        this.formResourceURL = formResourceURL;
    }


    /**
     * @return the activeCodeSource
     */
    public CodeSource getActiveCodeSource() {
        activeCodeSource = getSessionBean().getActiveCodeSource();
        return activeCodeSource;
    }

    /**
     * @param activeCodeSource the activeCodeSource to set
     */
    public void setActiveCodeSource(CodeSource activeCodeSource) {
        this.activeCodeSource = activeCodeSource;
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
     * @return the formGuideEntryID
     */
    public int getFormGuideEntryID() {
        return formGuideEntryID;
    }

    /**
     * @param formGuideEntryID the formGuideEntryID to set
     */
    public void setFormGuideEntryID(int formGuideEntryID) {
        this.formGuideEntryID = formGuideEntryID;
    }
    
}
