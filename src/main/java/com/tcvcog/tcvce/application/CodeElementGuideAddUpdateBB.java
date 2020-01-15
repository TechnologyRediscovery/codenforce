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
import com.tcvcog.tcvce.entities.CodeElementGuideEntry;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import java.io.Serializable;
import javax.faces.application.FacesMessage;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class CodeElementGuideAddUpdateBB extends BackingBeanUtils implements Serializable {
      
    private CodeElementGuideEntry currentGuideEntry;
    
    private String formCategory;
    private String formSubCategory;
    private String formDescription;
    private boolean formPriority;
    private String formEnforcementGuidelines;
    private String formInspectionGuidelines;
    
    /**
     * Creates a new instance of CodeElementGuide
     */
    public CodeElementGuideAddUpdateBB() {
    }
    
    public String addCodeElementGuideEntry(){
        
        CodeElementGuideEntry cege = new CodeElementGuideEntry();
        CodeIntegrator ci = getCodeIntegrator();
        
        cege.setCategory(formCategory);
        cege.setSubCategory(formSubCategory);
        cege.setDescription(formDescription);
        cege.setEnforcementGuidelines(formEnforcementGuidelines);
        cege.setInspectionGuidelines(formInspectionGuidelines);
        cege.setPriority(formPriority);
        
        try {
            ci.insertCodeElementGuideEntry(cege);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Succesfully added code guide entry", ""));
        } catch (IntegrationException ex) {

            System.out.println(ex.toString());

            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        ex.getLocalizedMessage(), 
                        "This must be corrected by the System Administrator, sorry"));
        }
        
        return "codeGuideView";
    }
    
    public String commitUpdatesToCodeGuideEntry(){
        System.out.println("CodeElementGuideBB.updateCodeElement");
        CodeIntegrator ci = getCodeIntegrator();
        
        currentGuideEntry.setCategory(formCategory);
        currentGuideEntry.setSubCategory(formSubCategory);
        currentGuideEntry.setDescription(formDescription);
        currentGuideEntry.setEnforcementGuidelines(formEnforcementGuidelines);
        currentGuideEntry.setInspectionGuidelines(formInspectionGuidelines);
        currentGuideEntry.setPriority(formPriority);
        
        try {
            System.out.println("CodeElementGuideBB.updateCodeElementGuideEntry: " 
                    + currentGuideEntry.getGuideEntryID());
            ci.updateCodeElementGuideEntry(currentGuideEntry);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Updated code element guide entry!", ""));

        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        ex.getLocalizedMessage(), 
                        "This must be corrected by the System Administrator"));
        }
        return "codeGuideView";
            
    }
    
    /**
     * @return the currentGuideEntry
     */
    public CodeElementGuideEntry getCurrentGuideEntry() {
        currentGuideEntry = getSessionBean().getActiveCodeElementGuideEntry();
        System.out.println("CodeElementGuideAddUpdateBB.getCurrentGuideEntry | retrieved guide entry:" + currentGuideEntry.getDescription());
        return currentGuideEntry;
    }

    /**
     * @return the formCategory
     */
    public String getFormCategory() {
        if(currentGuideEntry != null){
            formCategory = currentGuideEntry.getCategory();
        }
        return formCategory;
    }

    /**
     * @return the formSubCategory
     */
    public String getFormSubCategory() {
        if(currentGuideEntry != null){
            formSubCategory = currentGuideEntry.getSubCategory();
        }
        return formSubCategory;
    }

    /**
     * @return the formDescription
     */
    public String getFormDescription() {
        if(currentGuideEntry != null){
            formDescription = currentGuideEntry.getDescription();
        }
        return formDescription;
    }

    /**
     * @return the formEnforcementGuidelines
     */
    public String getFormEnforcementGuidelines() {
        if(currentGuideEntry != null){
            formEnforcementGuidelines = currentGuideEntry.getEnforcementGuidelines();
        }
        return formEnforcementGuidelines;
    }

    /**
     * @return the formInspectionGuidelines
     */
    public String getFormInspectionGuidelines() {
        if(currentGuideEntry != null){
            formInspectionGuidelines = currentGuideEntry.getInspectionGuidelines();
        }
        return formInspectionGuidelines;
    }

    /**
     * @return the formPriority
     */
    public boolean isFormPriority() {
        if(currentGuideEntry != null){
            formPriority = currentGuideEntry.isPriority();
        }
        return formPriority;
    }

    /**
     * @param currentGuideEntry the currentGuideEntry to set
     */
    public void setCurrentGuideEntry(CodeElementGuideEntry currentGuideEntry) {
        this.currentGuideEntry = currentGuideEntry;
    }

    /**
     * @param formCategory the formCategory to set
     */
    public void setFormCategory(String formCategory) {
        this.formCategory = formCategory;
    }

    /**
     * @param formSubCategory the formSubCategory to set
     */
    public void setFormSubCategory(String formSubCategory) {
        this.formSubCategory = formSubCategory;
    }

    /**
     * @param formDescription the formDescription to set
     */
    public void setFormDescription(String formDescription) {
        this.formDescription = formDescription;
    }

    /**
     * @param formEnforcementGuidelines the formEnforcementGuidelines to set
     */
    public void setFormEnforcementGuidelines(String formEnforcementGuidelines) {
        this.formEnforcementGuidelines = formEnforcementGuidelines;
    }

    /**
     * @param formInspectionGuidelines the formInspectionGuidelines to set
     */
    public void setFormInspectionGuidelines(String formInspectionGuidelines) {
        this.formInspectionGuidelines = formInspectionGuidelines;
    }

    /**
     * @param formPriority the formPriority to set
     */
    public void setFormPriority(boolean formPriority) {
        this.formPriority = formPriority;
    }
    
}
