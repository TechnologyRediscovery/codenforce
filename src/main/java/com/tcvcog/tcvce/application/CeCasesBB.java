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
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.EventCase;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Eric C. Darsow
 */
public class CeCasesBB extends BackingBeanUtils implements Serializable{

    
    private ArrayList<CECase> caseList;
    private ArrayList<CECase> caseHistoryList;
    private CECase selectedCase;
    private ArrayList<EventCase> recentEventList;
    private ArrayList<Person> muniPeopleList;
    
    /**
     * Creates a new instance of ceCasesBB
     */
    public CeCasesBB() {
    }
    
    public String viewCase(){
        getSessionBean().setActiveCase(selectedCase);
        // make the property associated with a selected case the active property
        getSessionBean().setActiveProp(selectedCase.getProperty());
        return "caseProfile";
    }

    /**
     * @return the CaseList
     */
    public ArrayList<CECase> getCaseList() {
        CaseIntegrator ci = getCaseIntegrator();
        
        int muniCodeForFetching = getSessionBean().getActiveMuni().getMuniCode();
        
        try {
            caseList = ci.getOpenCECases(muniCodeForFetching);
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Unable to load cases for this municipality due to an error in the Integration Module", ""));
        }
        return caseList;
    }
    
    
    /**
     * @return the CaseList
     */
    public ArrayList<CECase> getCaseHistoryList() {
        CaseIntegrator ci = getCaseIntegrator();
        
        int muniCodeForFetching = getSessionBean().getActiveMuni().getMuniCode();
        
        try {
            caseList = ci.getCECaseHistory(muniCodeForFetching);
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Unable to load cases for this municipality due to an error in the Integration Module", ""));
        }
        return caseList;
    }
    
    

    /**
     * @return the recentEventList
     */
    public ArrayList<EventCase> getRecentEventList() {
        return recentEventList;
    }

    /**
     * @return the muniPeopleList
     */
    public ArrayList<Person> getMuniPeopleList() {
        return muniPeopleList;
    }

    /**
     * @param CaseList the CaseList to set
     */
    public void setCaseList(ArrayList<CECase> CaseList) {
        this.caseList = CaseList;
    }

    /**
     * @param recentEventList the recentEventList to set
     */
    public void setRecentEventList(ArrayList<EventCase> recentEventList) {
        this.recentEventList = recentEventList;
    }

    /**
     * @param muniPeopleList the muniPeopleList to set
     */
    public void setMuniPeopleList(ArrayList<Person> muniPeopleList) {
        this.muniPeopleList = muniPeopleList;
    }

    /**
     * @return the selectedCase
     */
    public CECase getSelectedCase() {
        return selectedCase;
    }

    /**
     * @param selectedCase the selectedCase to set
     */
    public void setSelectedCase(CECase selectedCase) {
        this.selectedCase = selectedCase;
    }

    /**
     * @param caseHistoryList the caseHistoryList to set
     */
    public void setCaseHistoryList(ArrayList<CECase> caseHistoryList) {
        this.caseHistoryList = caseHistoryList;
    }

   

    

   
}
