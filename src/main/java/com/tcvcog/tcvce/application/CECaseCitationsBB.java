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

import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.Citation;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

/**
 *
 * @author sylvia
 */
public class CECaseCitationsBB 
        extends     BackingBeanUtils
        implements  Serializable {

    private CECaseDataHeavy currentCase;
  
    private List<Citation> citationList;
    private Citation selectedCitation;

    
    @PostConstruct
    public void initBean() {
        SessionBean sb = getSessionBean();
        currentCase = sb.getSessCECase();
       
    }

    /**
     * Creates a new instance of CECaseCitations
     */
    public CECaseCitationsBB() {
    }

    

    public String createNewCitation() {
        System.out.println("CaseProfileBB.createNewCitation  | current case tostring: "
                + currentCase);
        getSessionBean().setSessCitation(null);
//        positionCurrentCaseAtHeadOfQueue();
        return "citationEdit";
    }

    public String updateCitation(Citation cit) {
        getSessionBean().setSessCitation(cit);
//        positionCurrentCaseAtHeadOfQueue();
        return "citationEdit";
    }

    public String deleteCitation() {
        if (getSelectedCitation() != null) {
            CaseCoordinator cc = getCaseCoordinator();
            try {
                cc.deleteCitation(getSelectedCitation());
            } catch (IntegrationException ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to delete citation, sorry: "
                                + "probably because it is linked to another DB entity", ""));
                System.out.println(ex);
            }
        }
        return "";
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
     * @return the citationList
     */
    public List<Citation> getCitationList() {
        return citationList;
    }

    /**
     * @return the selectedCitation
     */
    public Citation getSelectedCitation() {
        return selectedCitation;
    }

    /**
     * @param citationList the citationList to set
     */
    public void setCitationList(List<Citation> citationList) {
        this.citationList = citationList;
    }

    /**
     * @param selectedCitation the selectedCitation to set
     */
    public void setSelectedCitation(Citation selectedCitation) {
        this.selectedCitation = selectedCitation;
    }
    
}
