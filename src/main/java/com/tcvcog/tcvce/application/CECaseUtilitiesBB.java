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
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import java.io.Serializable;
import java.util.Iterator;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author sylvia
 */
public class CECaseUtilitiesBB 
        extends     BackingBeanUtils
        implements  Serializable {

    private CECase currentCase;
    
    @PostConstruct
    public void initBean() {
        SessionBean sb = getSessionBean();
        currentCase = sb.getSessionCECase();
       
    }
    
    /**
     * Creates a new instance of CECaseUtilitiesBB
     */
    public CECaseUtilitiesBB() {
    }
    
    
    
    
    public String viewCasePropertyProfile(){
        getSessionBean().getSessionPropertyList().add(0, currentCase.getProperty());
        positionCurrentCaseAtHeadOfQueue();
        return "properties";
    }
    
    
    private void positionCurrentCaseAtHeadOfQueue(){
        getSessionBean().getSessionCECaseList().remove(currentCase);
        getSessionBean().getSessionCECaseList().add(0, currentCase);
        getSessionBean().setSessionCECase(currentCase);
    }

   

    
    
   public void updateCase(ActionEvent ev){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.updateCoreCECaseData(currentCase);
            cc.refreshCase(currentCase);
        } catch (BObStatusException ex) {
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
        
    }
   
     /**
     * Designed to update the case phase or name in the master case list in
     * ceCases.xhtml when a case is updated. Prevents rebuilding all the cases
     * in the entire list, which could be massive
     *
     * @deprecated 
     * @param c
     */
    private void updateCaseInCaseList(CECase c) {

//        CaseIntegrator ci = getCaseIntegrator();
//        Iterator<CECase> it = caseList.iterator();
//        CECase localCase;
//        int idx = 0;
//        while (it.hasNext()) {
//            localCase = it.next();
//            if (localCase.getCaseID() == c.getCaseID()) {
//                try {
//                    caseList.set(idx, ci.getCECase(c.getCaseID()));
//                } catch (IntegrationException | BObStatusException ex) {
//                    System.out.println(ex);
//                    getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
//                }
//            } // end if
//            idx++;
//        } // end while
    }

    /**
     * Pass through method for clicks of the refresh case data button on
     * cECases.xhtml
     *
     * @param ev
     */
    public void refreshCurrentCase(ActionEvent ev) {
        refreshCurrentCase();
        
    }

    public void refreshCurrentCase() {
        CaseIntegrator ci = getCaseIntegrator();
        System.out.println("CaseProfileBB.refreshCurrentCase | Refreshing case ID " + currentCase.getCaseID());
        try {
            currentCase = ci.getCECase(currentCase.getCaseID());
        } catch (IntegrationException ex) {
            System.out.println("CaseProfileBB.refreshCurrentCase | integration ex" + ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
        } catch (BObStatusException ex) {
            System.out.println("CaseProfileBB.refreshCurrentCase | lifecycle ex" + ex);
        }

    }

    
    
    
}
