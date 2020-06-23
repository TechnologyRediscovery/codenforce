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
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Property;
import java.io.Serializable;
import java.time.ZoneId;
import java.util.Date;
import javax.faces.application.FacesMessage;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class CECaseAddBB extends BackingBeanUtils implements Serializable{

    private Property caseProperty;
    private int formPropertyUnitID;
    private String formCaseName;
    private Date formOriginationDate;
    private String formCaseNotes;
    private boolean isUnitAssociated;
    
    /**
     * Creates a new instance of CaseAddBB
     */
    public CECaseAddBB() {
        formOriginationDate = java.util.Date.from(java.time.LocalDateTime.now()
                .atZone(ZoneId.systemDefault()).toInstant());
        
    }
    
    public String addNewCase(){
        // note that in this case, the case coordinator not this 
        // backing bean will interact with the caseintegrator
        // to enforce business logic concerning cases
        CaseCoordinator cc = getCaseCoordinator();
        CECase newCase;
        // check to see if we have an action request that needs to be connected
        // to this new case
        CEActionRequest cear = getSessionBean().getCeactionRequestForSubmission();
        newCase = cc.initCECase(caseProperty, getSessionBean().getSessUser());
        newCase.setCaseName(formCaseName);
        newCase.setOriginationDate(formOriginationDate.toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());
        newCase.setNotes(formCaseNotes);
        
        try {
            cc.insertNewCECase(newCase, getSessionBean().getSessUser().getMyCredential(), cear);
            getSessionBean().setSessCECase(cc.assembleCECaseDataHeavy(newCase, getSessionBean().getSessUser().getMyCredential()));
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Successfully added case to property! Access the case from the list below.", ""));
            return "ceCases";
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Integration Module error: Unable to add case to current property.", 
                            "Best try again or note the error and complain to Eric."));
        } catch (BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "A code enf action request was found in queue to be attached to this case. "
                                    + "I couldn't do that, though, sorry..", 
                            "Best try again or note the error and complain to Eric."));
            System.out.println(ex);
        } catch (ViolationException | EventException |SearchException ex) {
            System.out.println(ex);
        }
        
        // stick our new case on the session self for easy access
        
        //reload page on error
        return "";
    }

    /**
     * @return the formPropertyUnitID
     */
    public int getFormPropertyUnitID() {
        return formPropertyUnitID;
    }

    /**
     * @return the formCaseName
     */
    public String getFormCaseName() {
        return formCaseName;
    }

    /**
     * @return the formOriginationDate
     */
    public Date getFormOriginationDate() {
        return formOriginationDate;
    }

    /**
     * @return the formCaseNotes
     */
    public String getFormCaseNotes() {
        return formCaseNotes;
    }

    /**
     * @param formPropertyUnitID the formPropertyUnitID to set
     */
    public void setFormPropertyUnitID(int formPropertyUnitID) {
        this.formPropertyUnitID = formPropertyUnitID;
    }

    /**
     * @param formCaseName the formCaseName to set
     */
    public void setFormCaseName(String formCaseName) {
        this.formCaseName = formCaseName;
    }

    /**
     * @param formOriginationDate the formOriginationDate to set
     */
    public void setFormOriginationDate(Date formOriginationDate) {
        this.formOriginationDate = formOriginationDate;
    }

    /**
     * @param formCaseNotes the formCaseNotes to set
     */
    public void setFormCaseNotes(String formCaseNotes) {
        this.formCaseNotes = formCaseNotes;
    }

    /**
     * @return the caseProperty
     */
    public Property getCaseProperty() {
        
        caseProperty = getSessionBean().getSessProperty();
        return caseProperty;
    }

    /**
     * @param caseProperty the caseProperty to set
     */
    public void setCaseProperty(Property caseProperty) {
        this.caseProperty = caseProperty;
    }

    /**
     * @return the isUnitAssociated
     */
    public boolean isIsUnitAssociated() {
        return isUnitAssociated;
    }

    /**
     * @param isUnitAssociated the isUnitAssociated to set
     */
    public void setIsUnitAssociated(boolean isUnitAssociated) {
        this.isUnitAssociated = isUnitAssociated;
    }
    
}
