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
import com.tcvcog.tcvce.integration.CodeIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
/**
 *
 * @author Eric C. Darsow
 */
public class CodeSourceBB extends BackingBeanUtils implements Serializable{
    
    private ArrayList<CodeSource> codeSourceList;
    private int sourceID;
    private CodeSource selectedCodeSource;

    /**
     * Creates a new instance of CodeSourceBB
     */
    public CodeSourceBB() {
        
        // default value settings for form
    }

    public String addNewSource(){
        getSessionBean().setActiveCodeSource(null);
        return "codeSourceAddUpdate";
    }
    
    public String updateCodeSource(){
        System.out.println("CodeSourceBB.updateCodeSource | selected source: " + selectedCodeSource.getSourceName());
        if(selectedCodeSource != null){
            getSessionBean().setActiveCodeSource(selectedCodeSource);
            return "codeSourceAddUpdate";
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Please select a code source and try again", ""));
            return "";
        }
    }
    
    public String addElementToSource(){
        getSessionBean().setActiveCodeSource(selectedCodeSource);
        // remvoe any active element so when we jump to the
        // add page, there aren't any pre-populated element fields
        getSessionBean().setActiveCodeElement(null);
        return "codeElementAdd";
    }
    
    public String viewElementsInSource(){
        getSessionBean().setActiveCodeSource(selectedCodeSource);
        return "codeElementList";
    }
    
    public String linkElementsToCodeGuide(){
        getSessionBean().setActiveCodeSource(selectedCodeSource);
        return "codeGuideLink";
    }
    

    /**
     * @return the sourceID
     */
    public int getSourceID() {
        return sourceID;
    }

    /**
     * @param sourceID the sourceID to set
     */
    public void setSourceID(int sourceID) {
        this.sourceID = sourceID;
    }

    /**
     * @return the codeSourceList
     */
    public ArrayList<CodeSource> getCodeSourceList() {
        CodeIntegrator codeInt = getCodeIntegrator();
        try {
            codeSourceList = codeInt.getCompleteCodeSourceList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        return codeSourceList;
    }

    /**
     * @param codeSourceList the codeSourceList to set
     */
    public void setCodeSourceList(ArrayList<CodeSource> codeSourceList) {
        this.codeSourceList = codeSourceList;
    }

    /**
     * @return the selectedCodeSource
     */
    public CodeSource getSelectedCodeSource() {
        return selectedCodeSource;
    }

    /**
     * @param selectedCodeSource the selectedCodeSource to set
     */
    public void setSelectedCodeSource(CodeSource selectedCodeSource) {
        this.selectedCodeSource = selectedCodeSource;
    }
}
