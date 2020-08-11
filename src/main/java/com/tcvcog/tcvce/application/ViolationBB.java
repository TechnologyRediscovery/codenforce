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
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.BlobType;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.CodeSet;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.PageModeEnum;
import com.tcvcog.tcvce.integration.BlobIntegrator;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import org.primefaces.event.FileUploadEvent;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class ViolationBB extends BackingBeanUtils implements Serializable {

    
    private PageModeEnum currentMode;
    private List<PageModeEnum> pageModes;

    private CodeViolation currentViolation;
    private CECaseDataHeavy currentCase;
    private List<Blob> blobList;
    
    private String formNoteText;
    private ArrayList<EnforcableCodeElement> filteredElementList;
    private CodeSet currentCodeSet;

    /**
     * Creates a new instance of ViolationAdd
     */
    public ViolationBB() {

    }

    @PostConstruct
    public void initBean() {
        
        currentCase = getSessionBean().getSessCECase();
        
        currentViolation = getSessionBean().getSessCodeViolation();
        if(currentViolation == null){
            if(currentCase != null && !currentCase.getViolationList().isEmpty()){
                currentViolation = currentCase.getViolationList().get(0);
            }
        }
        
        currentCodeSet = getSessionBean().getSessCodeSet();

        pageModes = new ArrayList<>();
        pageModes.add(PageModeEnum.LOOKUP);
        pageModes.add(PageModeEnum.INSERT);
        pageModes.add(PageModeEnum.UPDATE);
        pageModes.add(PageModeEnum.REMOVE);
        if (getSessionBean().getCeCaseSearchProfilePageModeRequest() != null) {
            setCurrentMode(getSessionBean().getCeCaseSearchProfilePageModeRequest());
        }
        setCurrentMode(PageModeEnum.LOOKUP);
    }

     /**
     * Responds to the user clicking one of the page modes: LOOKUP, ADD, UPDATE,
     * REMOVE
     *
     * @param mode
     */
    public void setCurrentMode(PageModeEnum mode) {

        //store currentMode into tempCurMode as a temporary value, in case the currenMode equal null
        PageModeEnum tempCurMode = this.currentMode;
        //reset default setting every time the Mode has been selected 
//        loadDefaultPageConfig();
        //check the currentMode == null or not
        if (mode == null) {
            this.currentMode = tempCurMode;
        } else {
            this.currentMode = mode;
            System.out.println("CECaseSearchProfileBB.setCurrentMode: " + currentMode.getTitle());
            switch (currentMode) {
                case LOOKUP:
                    onModeLookupInit();
                    break;
                case INSERT:
                    onModeInsertInit();
                    break;
                case UPDATE:
                    onModeUpdateInit();
                    break;
                case REMOVE:
                    onModeRemoveInit();
                    break;
                default:
                    break;

            }
        }
    }

    //check if current mode == Lookup
    public boolean getActiveLookupMode() {
        // hard-wired on since there's always a property loaded
        return PageModeEnum.LOOKUP.equals(currentMode);
    }

    /**
     * Provide UI elements a boolean true if the mode is UPDATE
     *
     * @return
     */
    public boolean getActiveUpdateMode() {
        return PageModeEnum.UPDATE.equals(currentMode);
    }

    //check if current mode == Insert
    public boolean getActiveInsertUpdateMode() {
        return PageModeEnum.INSERT.equals(currentMode) || PageModeEnum.UPDATE.equals(currentMode);
    }

    //check if current mode == Insert
    public boolean getActiveInsertMode() {
        return PageModeEnum.INSERT.equals(currentMode);
    }

    //check if current mode == Remove
    public boolean getActiveRemoveMode() {
        return PageModeEnum.REMOVE.equals(currentMode);
    }
    
    /**
     * Primary listener method which copies a reference to the selected user
     * from the list and sets it on the selected user perch
     *
     * @param viol
     * @return 
     */
    public String onObjetViewButtonChange(CodeViolation viol) {
        CaseCoordinator cc = getCaseCoordinator();

        if (viol != null) {
            getSessionBean().setSessCodeViolation(viol);
            currentViolation = viol;
        }
        return "";
    }

    
       /**
     * Internal logic container for changes to page mode: Lookup
     */
    private void onModeLookupInit() {
    }
    
    /**
     * Internal logic container for beginning the user creation change process
     * Delegated from the mode button router
     */
    public void onModeInsertInit() {
        CaseCoordinator cc = getCaseCoordinator();
        
        currentViolation = cc.violation_getCodeViolationSkeleton(currentCase, null);
    }
    
      /**
     * Listener for beginning of update process
     */
    public void onModeUpdateInit() {
        // nothign to do here yet since the user is selected
    }

    /**
     * Listener for the start of the case remove process
     */
    public void onModeRemoveInit() {

    }


    /**
     * Listener for user selection of a violation from the code set violation table
     * @param ece
     */
    public void onViolationSelectButtonChange(EnforcableCodeElement ece){
        currentViolation.setCodeViolated(ece);
        
        
    }
 
    /**
     * Listener for user reqeusts to commit updates to a codeViolation
     * @return
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public String onViolationUpdateCommitButtonChange() throws IntegrationException, BObStatusException{
       CaseCoordinator cc = getCaseCoordinator();
       EventCoordinator eventCoordinator = getEventCoordinator();     
       SystemCoordinator sc = getSystemCoordinator();
       
       EventCategory ec = eventCoordinator.initEventCategory(
               Integer.parseInt(getResourceBundle(Constants.EVENT_CATEGORY_BUNDLE).getString("updateViolationEventCategoryID")));
       
        try {
            

            
             cc.violation_updateCodeViolation(currentCase, currentViolation, getSessionBean().getSessUser());
             
             // if update succeeds without throwing an error, then generate an
            // update violation event
            // TODO: Rewire this to work with new event processing cycle
            
//             eventCoordinator.generateAndInsertCodeViolationUpdateEvent(getCurrentCase(), currentViolation, event);
             
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
        
            return "ceCaseViolations";
    }

    

    
       /**
     * Listener for commencement of note writing process
     *
     * @param ev
     */
    public void onNoteInitButtonChange(ActionEvent ev) {
        setFormNoteText(null);

    }

    /**
     * Listener for user requests to commit new note content to the current
     * object
     *
     * @return
     */
    public String onNoteCommitButtonChange() {
        CaseCoordinator cc = getCaseCoordinator();

        MessageBuilderParams mbp = new MessageBuilderParams();
        mbp.setCred(getSessionBean().getSessUser().getKeyCard());
        mbp.setExistingContent(currentViolation.getNotes());
        mbp.setNewMessageContent(getFormNoteText());
        mbp.setHeader("Violation Note");
        mbp.setUser(getSessionBean().getSessUser());

        try {

            cc.violation_updateNotes(mbp, currentViolation);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Succesfully appended note!", ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Fatal error appending note; apologies!", ""));
            return "";
        }

        return "ceCaseViolations";

    }
    /**
     * Listener for user requests to abort their insert/update operation
     * @return
     */
    public String onInsertUpdateAbortButtonChange() {
        getSessionBean().setSessCitation(null);
        return "ceCaseViolations";

    }
    
    /**
     * Listener 
     * @param ev 
     */
    public void handlePhotoUpload(FileUploadEvent ev) {
        if (this.currentViolation == null) {
            this.currentViolation = getSessionBean().getSessCodeViolation();
        }
        if (ev == null) {
            System.out.println("ViolationAddBB.handlePhotoUpload | event: null");
            return;
        }
        if (this.currentViolation.getBlobIDList() == null) {
            this.currentViolation.setBlobIDList(new ArrayList<Integer>());
        }
        if (this.blobList == null) {
            this.blobList = new ArrayList<>();
        }

        BlobIntegrator blobi = getBlobIntegrator();
        Blob blob = null;
        try {
            blob = getBlobCoordinator().getNewBlob();
            blob.setBytes(ev.getFile().getContents());
            blob.setType(BlobType.PHOTO); // TODO: extract type from context somehow
            this.currentViolation.getBlobIDList().add(blobi.storeBlob(blob));
        } catch (IntegrationException ex) {
            System.out.println("ViolationAddBB.handlePhotoUpload | upload failed!\n" + ex);
            return;
        } catch (BlobException ex) {
            System.out.println(ex);
            return;
        }
        this.getBlobList().add(blob);
    }

    /**
     * Responds to user reqeusts to commit a new code violation to the CECase
     * @return 
     */
    public String onViolationAddCommitButtonChange() {

        CaseCoordinator cc = getCaseCoordinator();

        try {
             cc.violation_attachViolationToCaseAndInsertTimeFrameEvent(currentViolation, currentCase, getSessionBean().getSessUser());
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Success! Violation added.", ""));
            getSessionBean().getSessionBean().setSessCECase(currentCase);
            return "ceCases";
        } catch (IntegrationException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to edit violation.",
                            "This is a system-level error that msut be corrected by an administrator, Sorry!"));

        } catch (ViolationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), "Stipulated compliance date must "
                            + "be in the future; please revise the stipulated compliance date."));
        } catch (BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), "To preserve data integrity, this "
                            + "case's phase restrictions forbid attaching new code violations."));
        } catch (EventException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), "Violation event exception"));
        }
        return "ceCaseViolations";

    }
    
    /**
     * Listener for user requests to remove a violation from a case
     * @return 
     */
    public String onViolationRemoveCommitButtonChange(){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.violation_deactivateCodeViolation(currentViolation, getSessionBean().getSessUser());
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), null));
            return "";
            
        }
        return "ceCaseViolations";
        
    }

    /**
     * Unknown 
     * @return 
     */
    public String addViolationWithPhotos() {
        CaseIntegrator ci = getCaseIntegrator();
        CaseCoordinator cc = getCaseCoordinator();

        try {
             currentViolation.setViolationID(cc.violation_attachViolationToCaseAndInsertTimeFrameEvent(currentViolation, currentCase, getSessionBean().getSessUser()));
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Success! Violation added.", ""));
            return "violationPhotos";
        } catch (IntegrationException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to edit violation.",
                            "This is a system-level error that must be corrected by an administrator, Sorry!"));

        } catch (ViolationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            ex.getMessage(), "Stipulated compliance date must be in the future; please revise the stipulated compliance date."));
        } catch (BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), "To preserve data integrity, this "
                            + "case's phase restrictions forbid attaching new code violations."));
        } catch (EventException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), "Violation event exception"));
        }
        return "";

    }

    public String photosConfirm() {
        /*  TODO: this obviously
        
        if(this.currentViolation == null){
            this.currentViolation = getSessionBean().getSessionCodeViolation();
        }
        if(this.getPhotoList() == null  ||  this.getPhotoList().isEmpty()){
            getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                                "No uploaded photos to commit.", 
                                "Use the 'Return to case home without commiting photos' button bellow if you have no photos to upload."));
            return "";
        }
        
        ImageServices is = getImageServices();
        
        for(Photograph photo : this.getPhotoList()){
            
            try { 
                // commit and link
                is.commitPhotograph(photo.getPhotoID());
                is.linkPhotoToCodeViolation(photo.getPhotoID(), currentViolation.getViolationID());
                
            } catch (IntegrationException ex) {
                System.out.println(ex.toString());
                    getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                                "INTEGRATION ERROR: Unable write request into the database, our apologies!", 
                                "Please call your municipal office and report your concern by phone."));
                    return "";
            }
        }
         */
        return "ceCaseViolations";
    }

    /**
     * @return the currentViolation
     */
    public CodeViolation getCurrentViolation() {

        currentViolation = getSessionBean().getSessCodeViolation();
        return currentViolation;
    }

    /**
     * @param currentViolation the currentViolation to set
     */
    public void setCurrentViolation(CodeViolation currentViolation) {
        this.currentViolation = currentViolation;
    }

    /**
     * @return the photoList
     */
    public List<Blob> getBlobList() {
        return this.blobList;
    }

    /**
     * @param blobList
     */
    public void setBlobList(List<Blob> blobList) {
        this.blobList = blobList;
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
     * @return the currentMode
     */
    public PageModeEnum getCurrentMode() {
        return currentMode;
    }

   
    /**
     * @return the pageModes
     */
    public List<PageModeEnum> getPageModes() {
        return pageModes;
    }

    /**
     * @param pageModes the pageModes to set
     */
    public void setPageModes(List<PageModeEnum> pageModes) {
        this.pageModes = pageModes;
    }

    /**
     * @return the formNoteText
     */
    public String getFormNoteText() {
        return formNoteText;
    }

    /**
     * @param formNoteText the formNoteText to set
     */
    public void setFormNoteText(String formNoteText) {
        this.formNoteText = formNoteText;
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

}
