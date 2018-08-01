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
import com.tcvcog.tcvce.entities.CodeSource;
import java.io.Serializable;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Eric C. Darsow
 */
public class CodeSourceAddUpdateBB extends BackingBeanUtils implements Serializable{
   
    private CodeSource currentCodeSource;
    
    private int formSourceID;
    private String formSourceName;
    private int formSourceYear; 
    private String formSourceDescription;
    private boolean formSourceIsActive;
    private String formSourceURL;
    private String formSourceNotes;
    
    private boolean updateDisabled;
    private boolean addDisabled;

    /**
     * Creates a new instance of CodeSourceBB
     */
    public CodeSourceAddUpdateBB() {
        
        // default value settings for form
    }
    
  
    
    public String commitCodeSourceUpdates(){
        currentCodeSource.setSourceID(formSourceID);
        currentCodeSource.setSourceName(formSourceName);
        currentCodeSource.setSourceYear(formSourceYear);
        currentCodeSource.setSourceDescription(formSourceDescription);
        currentCodeSource.setIsActive(formSourceIsActive);
        currentCodeSource.setURL(formSourceURL);
        currentCodeSource.setSourceNotes(formSourceNotes);
        
        try {
            getCodeCoordinator().updateCodeSource(currentCodeSource);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Successfully updated code source", ""));
            return "codeSourceManage";
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Unable to UpdateCodeSource", 
                            "Ensure the sourceID matches an existing code source from the table above"));
        }
        return "";
    }
    
    public String addNewSource(){
        System.out.println("codesourceBB.addNewSource");
        CodeSource newCodeSource = new CodeSource();
        newCodeSource.setSourceName(formSourceName);
        newCodeSource.setSourceYear(formSourceYear);
        newCodeSource.setSourceDescription(formSourceDescription);
        newCodeSource.setIsActive(formSourceIsActive);
        newCodeSource.setURL(formSourceURL);
        newCodeSource.setSourceNotes(formSourceNotes);
        try {
            getCodeCoordinator().addNewCodeSource(newCodeSource);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Successfully added new code source", 
                            ""));
        
            return "success";
        } catch (IntegrationException ex) {
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Unable to Add Source", 
                            "This error will require administrator attention"));
             return "";
        }
    }
    
   
    /**
     * @return the currentCodeSource
     */
    public CodeSource getCurrentCodeSource() {
        currentCodeSource = getSessionBean().getActiveCodeSource();
        return currentCodeSource;
    }

    /**
     * @param currentCodeSource the currentCodeSource to set
     */
    public void setCurrentCodeSource(CodeSource currentCodeSource) {
        this.currentCodeSource = currentCodeSource;
    }

    /**
     * @return the formSourceName
     */
    public String getFormSourceName() {
        if(currentCodeSource != null){
            formSourceName = currentCodeSource.getSourceName();
        }
        return formSourceName;
    }

    /**
     * @param formSourceName the formSourceName to set
     */
    public void setFormSourceName(String formSourceName) {
        this.formSourceName = formSourceName;
    }


   

    /**
     * @return the formSourceID
     */
    public int getFormSourceID() {
        if(currentCodeSource != null){
            formSourceID = currentCodeSource.getSourceID();
        }
        return formSourceID;
    }

    /**
     * @param formSourceID the formSourceID to set
     */
    public void setFormSourceID(int formSourceID) {
        this.formSourceID = formSourceID;
    }

    /**
     * @return the formSourceYear
     */
    public int getFormSourceYear() {
        if(currentCodeSource != null){
            formSourceYear = currentCodeSource.getSourceYear();
        }
        return formSourceYear;
    }

    /**
     * @param formSourceYear the formSourceYear to set
     */
    public void setFormSourceYear(int formSourceYear) {
        this.formSourceYear = formSourceYear;
    }

    /**
     * @return the formSourceDescription
     */
    public String getFormSourceDescription() {
        if(currentCodeSource != null){
            formSourceDescription = currentCodeSource.getSourceDescription();
        }
        return formSourceDescription;
    }

    /**
     * @param formSourceDescription the formSourceDescription to set
     */
    public void setFormSourceDescription(String formSourceDescription) {
        this.formSourceDescription = formSourceDescription;
    }

    /**
     * @return the formSourceIsActive
     */
    public boolean isFormSourceIsActive() {
        if(currentCodeSource != null){
            formSourceIsActive = currentCodeSource.isIsActive();
        }
        return formSourceIsActive;
    }

    /**
     * @param formSourceIsActive the formSourceIsActive to set
     */
    public void setFormSourceIsActive(boolean formSourceIsActive) {
        this.formSourceIsActive = formSourceIsActive;
    }

    /**
     * @return the formSourceURL
     */
    public String getFormSourceURL() {
        if(currentCodeSource != null) {
            formSourceURL = currentCodeSource.getURL();
        }
        return formSourceURL;
    }

    /**
     * @param formSourceURL the formSourceURL to set
     */
    public void setFormSourceURL(String formSourceURL) {
        this.formSourceURL = formSourceURL;
    }

    /**
     * @return the formSourceNotes
     */
    public String getFormSourceNotes() {
        if(currentCodeSource != null){
            formSourceNotes = currentCodeSource.getSourceNotes();
        }
        return formSourceNotes;
    }

    /**
     * @param formSourceNotes the formSourceNotes to set
     */
    public void setFormSourceNotes(String formSourceNotes) {
        this.formSourceNotes = formSourceNotes;
    }

    /**
     * @return the updateDisabled
     */
    public boolean isUpdateDisabled() {
        if(currentCodeSource == null){
            updateDisabled = true;
        }
        return updateDisabled;
    }

    /**
     * @return the addDisabled
     */
    public boolean isAddDisabled() {
        if(currentCodeSource != null){
            addDisabled = true;
        }
        return addDisabled;
    }

    /**
     * @param updateDisabled the updateDisabled to set
     */
    public void setUpdateDisabled(boolean updateDisabled) {
        this.updateDisabled = updateDisabled;
    }

    /**
     * @param addDisabled the addDisabled to set
     */
    public void setAddDisabled(boolean addDisabled) {
        this.addDisabled = addDisabled;
    }
    
    
}
