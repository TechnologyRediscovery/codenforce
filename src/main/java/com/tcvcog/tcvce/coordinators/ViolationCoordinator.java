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
import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.EventCECase;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.NoticeOfViolation;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.integration.CodeViolationIntegrator;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.util.Constants;
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
        v.setCeCaseID(c.getCaseID());
        // control is passed back to the violationAddBB which stores this 
        // generated violation under teh activeCodeViolation in the session
        // which the ViolationAddBB then picks up and edits
        
        return v;
    }
    
    /**
     * Standard coordinator method which calls the integration method after 
     * checking businses rules. 
     * ALSO creates a corresponding timeline event to match the stipulated compliance
     * date on the violation that's added.
     * @param v
     * @param c
     * @return the database key assigned to the inserted violation
     * @throws IntegrationException
     * @throws ViolationException 
     * @throws com.tcvcog.tcvce.domain.CaseLifecyleException 
     */
    public int attachViolationToCaseAndInsertTimeFrameEvent(CodeViolation v, CECase c) throws IntegrationException, ViolationException, CaseLifecyleException{
        
        CodeViolationIntegrator vi = getCodeViolationIntegrator();
        EventCoordinator ec = getEventCoordinator();
        EventCECase tfEvent;
        int violationStoredDBKey;
        StringBuilder sb = new StringBuilder();
        
//        EventCategory eventCat = ec.getInitiatlizedEventCategory(
//                                Integer.parseInt(getResourceBundle(Constants.EVENT_CATEGORY_BUNDLE)
//                                .getString("complianceTimeframeExpiry")));
        EventCategory eventCat = ec.getInitiatlizedEventCategory(113);
        tfEvent = ec.getInitializedEvent(c, eventCat);
        tfEvent.setDateOfRecord(v.getStipulatedComplianceDate());
        tfEvent.setCreator(c.getCaseManager());
        
        sb.append(getResourceBundle(Constants.MESSAGE_TEXT)
                        .getString("complianceTimeframeEndEventDesc"));
        sb.append("Case: ");
        sb.append(c.getCaseName());
        sb.append(" at ");
        sb.append(c.getProperty().getAddress());
        sb.append("(");
        sb.append(c.getProperty().getMuni().getMuniName());
        sb.append(")");
        sb.append("; Violation: ");
        sb.append(v.getViolatedEnfElement().getCodeElement().getHeaderString());
        tfEvent.setDescription(sb.toString());
        
        if(verifyCodeViolationAttributes(v)){
            violationStoredDBKey = vi.insertCodeViolation(v);
            ec.insertEvent(tfEvent);
        } else {
            throw new ViolationException("Failed violation verification");
        }
        return violationStoredDBKey;
        
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
    
    /**
     * CodeViolation should have the actual compliance date set from the user's 
     * event date of record
     * @param cv
     * @param u the user carrying out the compliance certification
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void recordCompliance(CodeViolation cv, User u) throws IntegrationException{
        
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        EventIntegrator ei = getEventIntegrator();
        // update violation record for compliance
        cv.setComplianceTimeStamp(LocalDateTime.now());
        cv.setComplianceUser(u);
        cvi.recordCompliance(cv);
                
        // inactivate timeframe expiry event
        ei.inactivateEvent(cv.getCompTimeFrameComplianceEvent().getEventID());
        
    }
    
    public NoticeOfViolation getNewNoticeOfViolation(){
        NoticeOfViolation nov = new NoticeOfViolation();
        nov.setDateOfRecord(LocalDateTime.now());
        return nov;
        
    }
    
    public void deleteViolation(CodeViolation cv) throws IntegrationException{
        //TODO: delete photos and photo links
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        cvi.deleteCodeViolation(cv);
    }
    
    public ArrayList getCodeViolations(CECase ceCase) throws IntegrationException{
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        ArrayList al = cvi.getCodeViolations(ceCase);
        return al;
    }
    
}
