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


import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import java.io.Serializable;
import java.time.ZoneId;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Eric C. Darsow
 */
public class CaseUpdateBB extends BackingBeanUtils implements Serializable{

    /**
     * Creates a new instance of CaseUpdateBB
     */
    public CaseUpdateBB() {
    }
    
    private CECase currentCase;
    
    private int formControlCode;
    private String formCaseName;
    private java.util.Date formOriginDate;
    private String formCaseNotes;
    
    public String updateCase(){
        CaseCoordinator cc = getCaseCoordinator();
        currentCase.setPublicControlCode(formControlCode);
        currentCase.setCaseName(formCaseName);
        currentCase.setOriginationDate(formOriginDate
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        currentCase.setNotes(formCaseNotes);
        try {
            cc.updateCase(currentCase);
            cc.refreshCase(currentCase);
        } catch (CaseLifecyleException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        ex.getMessage(),
                        "Please try again with a revised origination date"));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        ex.getMessage(),
                        "This issue must be corrected by a system administrator, sorry"));
        }
        
        getFacesContext().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Case details updated!",""));
        
        return "";
    }

    /**
     * @return the formControlCode
     */
    public int getFormControlCode() {
        formControlCode = currentCase.getPublicControlCode();
        return formControlCode;
    }

    /**
     * @return the formCaseName
     */
    public String getFormCaseName() {
        formCaseName = currentCase.getCaseName();
        return formCaseName;
    }

    /**
     * @return the formOriginDate
     */
    public java.util.Date getFormOriginDate() {
        formOriginDate = java.util.Date.from(currentCase
                .getOriginationDate().atZone(ZoneId.systemDefault()).toInstant());
        return formOriginDate;
    }

    /**
     * @return the formCaseNotes
     */
    public String getFormCaseNotes() {
        formCaseNotes = currentCase.getNotes();
        return formCaseNotes;
    }

    /**
     * @param formControlCode the formControlCode to set
     */
    public void setFormControlCode(int formControlCode) {
        this.formControlCode = formControlCode;
    }

    /**
     * @param formCaseName the formCaseName to set
     */
    public void setFormCaseName(String formCaseName) {
        this.formCaseName = formCaseName;
    }

    /**
     * @param formOriginDate the formOriginDate to set
     */
    public void setFormOriginDate(java.util.Date formOriginDate) {
        this.formOriginDate = formOriginDate;
    }

    /**
     * @param formCaseNotes the formCaseNotes to set
     */
    public void setFormCaseNotes(String formCaseNotes) {
        this.formCaseNotes = formCaseNotes;
    }

    /**
     * @return the currentCase
     */
    public CECase getCurrentCase() {
        
        currentCase = getSessionBean().getActiveCase();
        return currentCase;
    }

    /**
     * @param currentCase the currentCase to set
     */
    public void setCurrentCase(CECase currentCase) {
        this.currentCase = currentCase;
    }
    
    
    
    
    
    
    
}
