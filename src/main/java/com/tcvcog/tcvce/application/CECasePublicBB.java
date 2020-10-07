/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

/**
 *
 * @author sylvia
 */
public class CECasePublicBB 
        extends     BackingBeanUtils
        implements  Serializable {

    private CECaseDataHeavy currentCase;
    private int freshPACC;
    
    
    @PostConstruct
    public void initBean() {
        SessionBean sb = getSessionBean();
        currentCase = sb.getSessCECase();
       
    }
    
    /**
     * Creates a new instance of CECasePublicBB
     */
    public CECasePublicBB() {
    }

    
    public void generateNewPublicCC() {
        CaseIntegrator ci = getCaseIntegrator();

        try {
            freshPACC = generateControlCodeFromTime(currentCase.getProperty().getMuni().getMuniCode());

            currentCase.setPublicControlCode(freshPACC);
            ci.updateCECaseMetadata(currentCase);

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Done! Public access control code for case : " + currentCase.getCaseID()
                    + "is now " + freshPACC, ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to add change public access code ",
                    getResourceBundle(Constants.MESSAGE_TEXT).getString("systemLevelError")));
        }

    }
    
    public void changePACCAccess() {
        System.out.println("CEActionRequestsBB.changePACCAccess");
        CaseIntegrator ci = getCaseIntegrator();

        try {
            ci.updateCECaseMetadata(currentCase);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Done! Public access status is now: " + String.valueOf(currentCase.isPaccEnabled())
                    + " and action request forward linking is statusnow: " + String.valueOf(currentCase.isAllowForwardLinkedPublicAccess())
                    + " for case ID: " + currentCase.getCaseID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to add change public access code status",
                    getResourceBundle(Constants.MESSAGE_TEXT).getString("systemLevelError")));
        }
    }

    /**
     * @return the currentCase
     */
    public CECaseDataHeavy getCurrentCase() {
        return currentCase;
    }

    /**
     * @param currentCase the currentCase to set
     */
    public void setCurrentCase(CECaseDataHeavy currentCase) {
        this.currentCase = currentCase;
    }

    /**
     * @return the freshPACC
     */
    public int getFreshPACC() {
        return freshPACC;
    }

    /**
     * @param freshPACC the freshPACC to set
     */
    public void setFreshPACC(int freshPACC) {
        this.freshPACC = freshPACC;
    }
    
}
