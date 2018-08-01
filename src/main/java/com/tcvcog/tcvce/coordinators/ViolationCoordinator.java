/*
 * Copyright (C) 2017 Turtle Creek Valley
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
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.integration.CodeViolationIntegrator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ArrayList;

/**
 *
 * @author Eric C. Darsow
 */
public class ViolationCoordinator extends BackingBeanUtils implements Serializable{

    /**
     * Creates a new instance of ViolationCoordinator
     */
    public ViolationCoordinator() {
    }
    
    public CodeViolation generateNewCodeViolation(CECase c, EnforcableCodeElement ece){
        CodeViolation v = new CodeViolation();
        
        System.out.println("ViolationCoordinator.generateNewCodeViolation | enfCodeElID:" + ece.getCodeSetElementID());
        
        v.setViolatedEnfElement(ece);
        v.setStipulatedComplianceDate(LocalDateTime.now()
                .plusDays(ece.getNormDaysToComply()));
        v.setPenalty(ece.getNormPenalty());
        v.setDateOfRecord(LocalDateTime.now());
        v.setAttachedCase(c);
        // control is passed back to the violationAddBB which stores this 
        // generated violation under teh activeCodeViolation in the session
        // which the ViolationAddBB then picks up and edits
        
        return v;
    }
    
    
    public void addNewCodeViolation(CodeViolation v) throws IntegrationException, ViolationException{
        
        CodeViolationIntegrator vi = getCodeViolationIntegrator();
        
        if(verifyCodeViolationAttributes(v)){
            vi.insertCodeViolation(v);
            
        } else {
            throw new ViolationException("Failed violation verification");
        }
        
        
    }
    
    private boolean verifyCodeViolationAttributes(CodeViolation cv) throws ViolationException{
        
        // this is no good--when we get past stip comp date, we'll have a coordinator issue
//        if(cv.getStipulatedComplianceDate().isBefore(LocalDateTime.now())){
//            throw new ViolationException("Stipulated Complicance must be in the future!");
//        }
        
        return true;
    }
    
    public void updateCodeViolation(CodeViolation cv) throws ViolationException, IntegrationException{
        
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        if(verifyCodeViolationAttributes(cv)){
            cvi.updateCodeViolation(cv);
        }
        
        
    }
    
    public void deleteViolation(CodeViolation cv) throws IntegrationException{
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        cvi.deleteCodeViolation(cv);
    }
    
    public ArrayList getCodeViolations(CECase ceCase) throws IntegrationException{
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        ArrayList al = cvi.getCodeViolations(ceCase);
        return al;
    }
    
}
