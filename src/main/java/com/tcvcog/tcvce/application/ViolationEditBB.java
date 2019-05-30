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
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EventCECase;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import java.io.Serializable;
import java.time.ZoneId;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Eric C. Darsow
 */
public class ViolationEditBB extends BackingBeanUtils implements Serializable{

    
    private CodeViolation currentViolation;
    private CECase currentCase;
    
    
    // violation update event fields
    private boolean formDiscloseToMuni;
    private boolean formDiscloseToPublic;
    private String formEventNotes;
    
    /**
     * Creates a new instance of ViolationEditBB
     */
    public ViolationEditBB() {
    }
    
    @PostConstruct
    public void initBean(){
        
         currentViolation = getSessionBean().getActiveCodeViolation();
         currentCase = getSessionBean().getcECaseQueue().get(0);
         formDiscloseToMuni = true;
         formDiscloseToPublic = true;
    }
    
    public String editViolation() throws IntegrationException, CaseLifecyleException{
       CaseCoordinator cc = getCaseCoordinator();
       EventCoordinator eventCoordinator = getEventCoordinator();       
       
       EventCategory ec = eventCoordinator.getInitiatlizedEventCategory(
               Integer.parseInt(getResourceBundle(Constants.EVENT_CATEGORY_BUNDLE).getString("updateViolationEventCategoryID")));
       
       EventCECase event = eventCoordinator.getInitializedEvent(getCurrentCase(), ec);
        
        // load up edit event data
        event.setNotes(formEventNotes);
        event.setDiscloseToMunicipality(formDiscloseToMuni);
        event.setDiscloseToPublic(formDiscloseToPublic);
        
        MessageBuilderParams mcc = new MessageBuilderParams();
        mcc.existingContent = currentViolation.getNotes();
        mcc.newMessageContent = formEventNotes;
        mcc.user = getSessionBean().getFacesUser();
        currentViolation.setNotes(appendNoteBlock(mcc));
        
        try {
            
             cc.updateCodeViolation(currentCase, currentViolation, getSessionBean().getFacesUser());
             
             // if update succeeds without throwing an error, then generate an
             // update violation event
             eventCoordinator.generateAndInsertCodeViolationUpdateEvent(getCurrentCase(), currentViolation, event);
             
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Success! Violation updated and notice event generated", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Unable to edit violation in the database", 
                                "This is a system-level error that msut be corrected by an administrator, Sorry!"));
            
        } catch (ViolationException ex) {
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, 
                            ex.getMessage(), "Please revise the stipulated compliance date"));
             
        } catch (EventException ex) {
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            ex.getMessage(), "Unable to generate automated event to log violation update"));
        }
        
            return "ceCases";
    }

    /**
     * @return the currentViolation
     */
    public CodeViolation getCurrentViolation() {
       
      
        return currentViolation;
    }
    
    public String backToCaseManager(){
        return "caseProfile";
    }

   
   

    /**
     * @param currentViolation the currentViolation to set
     */
    public void setCurrentViolation(CodeViolation currentViolation) {
        this.currentViolation = currentViolation;
    }

   

    /**
     * @return the formDiscloseToMuni
     */
    public boolean isFormDiscloseToMuni() {
        return formDiscloseToMuni;
    }

    /**
     * @return the formDiscloseToPublic
     */
    public boolean isFormDiscloseToPublic() {
        return formDiscloseToPublic;
    }


    /**
     * @param formDiscloseToMuni the formDiscloseToMuni to set
     */
    public void setFormDiscloseToMuni(boolean formDiscloseToMuni) {
        this.formDiscloseToMuni = formDiscloseToMuni;
    }

    /**
     * @param formDiscloseToPublic the formDiscloseToPublic to set
     */
    public void setFormDiscloseToPublic(boolean formDiscloseToPublic) {
        this.formDiscloseToPublic = formDiscloseToPublic;
    }

    /**
     * @return the formEventNotes
     */
    public String getFormEventNotes() {
        return formEventNotes;
    }

    /**
     * @param formEventNotes the formEventNotes to set
     */
    public void setFormEventNotes(String formEventNotes) {
        this.formEventNotes = formEventNotes;
    }

    /**
     * @return the currentCase
     */
    public CECase getCurrentCase() {
        return currentCase;
    }

    /**
     * @param currentCase the currentCase to set
     */
    public void setCurrentCase(CECase currentCase) {
        this.currentCase = currentCase;
    }
    
}
