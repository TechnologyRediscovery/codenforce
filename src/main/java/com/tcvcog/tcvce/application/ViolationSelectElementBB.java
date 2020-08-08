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
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CodeSet;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class ViolationSelectElementBB extends BackingBeanUtils implements Serializable {

    private ArrayList<EnforcableCodeElement> filteredElementList;
    private CodeSet currentCodeSet;

    /**
     * Creates a new instance of ViolationSelectElementBB
     */
    public ViolationSelectElementBB() {
    }
    
    
    @PostConstruct
    public void initBean(){
        CodeIntegrator integrator = getCodeIntegrator();
        CodeSet codeSet = getSessionBean().getSessCodeSet();
        
        currentCodeSet = getSessionBean().getSessCodeSet();
        
    }

    public String useSelectedElement(EnforcableCodeElement ece) {
        CaseCoordinator cc = getCaseCoordinator();
        CodeViolation cv = cc.violation_generateNewCodeViolation(getSessionBean().getSessCECase(), ece);
        getSessionBean().setSessCodeViolation(cv);
        return "violationAdd";


    }


   
    /**
     * @return the currentCodeSet
     */
    public CodeSet getCurrentCodeSet() {
        return currentCodeSet;
    }

    /**
     * @param currentCodeSet the currentCodeSet to set
     */
    public void setCurrentCodeSet(CodeSet currentCodeSet) {
        this.currentCodeSet = currentCodeSet;
    }

    /**
     * @return the filteredElementList
     */
    public ArrayList<EnforcableCodeElement> getFilteredElementList() {
        return filteredElementList;
    }

    /**
     * @param filteredElementList the filteredElementList to set
     */
    public void setFilteredElementList(ArrayList<EnforcableCodeElement> filteredElementList) {
        this.filteredElementList = filteredElementList;
    }

}
