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

import com.tcvcog.tcvce.coordinators.BlobCoordinator;
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.BlobTypeException;
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
import com.tcvcog.tcvce.entities.IntensityClass;
import com.tcvcog.tcvce.entities.PageModeEnum;
import com.tcvcog.tcvce.integration.BlobIntegrator;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveListsEnum;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
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
    
    private List<ViewOptionsActiveListsEnum> viewOptionList;
    private ViewOptionsActiveListsEnum selectedViewOption;
    
    private List<IntensityClass> severityList;
    
    private String formNoteText;
    private List<EnforcableCodeElement> filteredElementList;
    private CodeSet currentCodeSet;

    private boolean extendStipCompUsingDate;
    private java.util.Date extendedStipCompDate;
    private int extendedStipCompDaysFromToday;

    /**
     * Creates a new instance of ViolationAdd
     */
    public ViolationBB() {

    }

    @PostConstruct
    public void initBean() {
        CaseCoordinator cc = getCaseCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        try {
            currentCase = cc.cecase_assembleCECaseDataHeavy(getSessionBean().getSessCECase(), getSessionBean().getSessUser());

            currentViolation = getSessionBean().getSessCodeViolation();
            if (currentViolation == null) {
                if (currentCase != null && !currentCase.getViolationList().isEmpty()) {
                    currentViolation = currentCase.getViolationList().get(0);
                }
            }

            severityList = sc.getIntensitySchemaWithClasses(
                    getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("intensityschema_violationseverity"))
                    .getClassList();
        } catch (BObStatusException | IntegrationException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));

        }

        filteredElementList = null;
        extendStipCompUsingDate = true;
        currentCodeSet = getSessionBean().getSessCodeSet();

        pageModes = new ArrayList<>();
        pageModes.add(PageModeEnum.LOOKUP);
        pageModes.add(PageModeEnum.INSERT);
        pageModes.add(PageModeEnum.UPDATE);
        pageModes.add(PageModeEnum.REMOVE);
        if (getSessionBean().getCeCaseViolationsPageModeRequest() != null) {
            setCurrentMode(getSessionBean().getCeCaseViolationsPageModeRequest());
        } else {
            setCurrentMode(PageModeEnum.LOOKUP);
        }
        viewOptionList = Arrays.asList(ViewOptionsActiveListsEnum.values());
        selectedViewOption = ViewOptionsActiveListsEnum.VIEW_ACTIVE;
        
        System.out.println("ViolationBB.initBean()");
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
     */
    public void onObjetViewButtonChange(CodeViolation viol) {

        if (viol != null) {
            getSessionBean().setSessCodeViolation(viol);
            currentViolation = viol;
        }
        System.out.println("ViolationBB.onObjectViewButtonChange: currentViolation is now " + currentViolation.getViolationID());

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
        System.out.println("violationBB.OnModeInsertInit");

        try {
            currentViolation = cc.violation_getCodeViolationSkeleton(currentCase);
        } catch (BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }

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
     * Listener for user selection of a violation from the code set violation
     * table
     *
     * @param ece
     */
    public void onViolationSelectElementButtonChange(EnforcableCodeElement ece) {
        CaseCoordinator cc = getCaseCoordinator();
        try {
            currentViolation = cc.violation_injectOrdinance(currentCase, currentViolation, ece, null);
        } catch (BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }
    }

    /**
     * Listener for the start of the violation choosing process
     *
     * @param ev
     */
    public void onViolationSelectElementInitButtonChange(ActionEvent ev) {
        // do nothing yet
    }

    /**
     * Listener for commencement of extending stip comp date
     *
     * @param ev
     */
    public void onViolationExtendStipCompDateInitButtonChange(ActionEvent ev) {
        extendedStipCompDaysFromToday = CaseCoordinator.DEFAULT_EXTENSIONDAYS;
    }

    /**
     * Listener for requests to commit extension of stip comp date
     *
     * @return
     */
    public String onViolationExtendStipCompDateCommitButtonChange() {
        CaseCoordinator cc = getCaseCoordinator();
        long secBetween;
        try {
            if (extendStipCompUsingDate && extendedStipCompDate != null) {
                LocalDateTime freshDate = extendedStipCompDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                if (freshDate.isBefore(LocalDateTime.now())) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Stipulated compliance dates must be in the future!", ""));
                } else {
                    secBetween = freshDate.toEpochSecond(ZoneOffset.of("-4")) - LocalDateTime.now().toEpochSecond(ZoneOffset.of("-4"));
                    // divide by num seconds in a day
                    long daysBetween = secBetween / (24 * 60 * 60);
                    cc.violation_extendStipulatedComplianceDate(currentViolation, daysBetween, currentCase, getSessionBean().getSessUser());
                }
            } else {
                cc.violation_extendStipulatedComplianceDate(currentViolation, extendedStipCompDaysFromToday, currentCase, getSessionBean().getSessUser());
            }
        } catch (BObStatusException | IntegrationException | ViolationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.toString(), ""));
        } 
        getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "Stipulated compliance dates is now: " + getPrettyDate(currentViolation.getStipulatedComplianceDate()), ""));
        return "ceCaseViolations";

    }

    /**
     * Listener for user reqeusts to commit updates to a codeViolation
     *
     * @return
     * @throws IntegrationException
     * @throws BObStatusException
     */
    public String onViolationUpdateCommitButtonChange() throws IntegrationException, BObStatusException {
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

        }

        return "ceCaseViolations";
    }

    /**
     * Listener for user requests to commit a violation compliance event
     *
     * @return 
     */
    public String onViolationRecordComplianceCommitButtonChange() {
        EventCoordinator ec = getEventCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
        
            // build event details package
            EventCnF e = null;
            try {
                
//                cc.violation_recordCompliance(currentViolation, getSessionBean().getSessUser());
                   getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Compliance recorded", ""));
                
                // ************ TODO: Finish me with events ******************//
                // ************ TODO: Finish me with events ******************//
                e = ec.generateViolationComplianceEvent(currentViolation);
                e.setUserCreator(getSessionBean().getSessUser());
                e.setTimeStart(LocalDateTime.now());
                
                // ************ TODO: Finish me with events ******************//
                // ************ TODO: Finish me with events ******************//
                
                   getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Compliance event attached to case", ""));
            } catch (IntegrationException ex) {
                System.out.println(ex);
                   getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.toString(), ""));
                   return "";
            }

        return "ceCaseViolations";
        

            
        // the user is then shown the add event dialog, and when the
        // event is added to the case, the CaseCoordinator will
        // set the date of record on the violation to match that chosen
        // for the event
//        selectedEvent = e;
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
     *
     * @return
     */
    public String onInsertUpdateAbortButtonChange() {
        getSessionBean().setSessCitation(null);
        return "ceCaseViolations";

    }

    /**
     * Listener
     *
     * @param ev
     */
    public void handlePhotoUpload(FileUploadEvent ev) {
        CaseCoordinator cc = getCaseCoordinator();
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
        
        try {
            BlobCoordinator blobc = getBlobCoordinator();
            
            Blob blob = blobc.getNewBlob();
            blob.setBytes(ev.getFile().getContents());
            blob.setFilename(ev.getFile().getFileName());
            blob.setMunicode(getSessionBean().getSessMuni().getMuniCode());
            this.currentViolation.getBlobIDList().add(blobc.storeBlob(blob).getBlobID());
            this.getBlobList().add(blob);
        } catch (IntegrationException | IOException | ClassNotFoundException | NoSuchElementException ex) {
            System.out.println("ViolationAddBB.handlePhotoUpload | upload failed! " + ex);
        } catch (BlobException ex) {
            System.out.println("ViolationAddBB.handlePhotoUpload | upload failed! " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }
    }

    /**
     * Responds to user reqeusts to commit a new code violation to the CECase
     *
     * @return
     */
    public String onViolationAddCommitButtonChange() {

        CaseCoordinator cc = getCaseCoordinator();

        try {
            cc.violation_attachViolationToCase(currentViolation, currentCase, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success! Violation attached to case.", ""));
            getSessionBean().getSessionBean().setSessCodeViolation(currentViolation);
            System.out.println("ViolationBB.onViolationAddCommmitButtonChange | completed violation process");
        } catch (IntegrationException | SearchException | BObStatusException | EventException | ViolationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
            return "";
        }
        return "ceCaseViolations";

    }

    /**
     * Listener for user requests to remove a violation from a case
     *
     * @return
     */
    public String onViolationRemoveCommitButtonChange() {
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
    
    public String onViolationNullifyCommitButtonChange(){
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
     * Listener for user request to remove photo on violation
     *
     * @param photoid
     * @return
     */
    public String onPhotoRemoveButtonChange(int photoid) {
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.violation_removeLinkBlobToCodeViolation(currentViolation, photoid);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Blob removed with ID " + photoid, ""));
        } catch (BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Cannot remove photo yet: unsupported operation", ""));
            
        } 

        // do something here
        return "ceCaseViolations";

    }
    
    
    /**
     * TODO: NADIT review
     * @param blob 
     */
    public void onPhotoUpdateDescription(Blob blob){
        BlobCoordinator bc = getBlobCoordinator();
        try {
            bc.updateBlobFilename(blob);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully updated photo description", ""));
            
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Cannot update photo description", ""));
            
        } catch (IOException | BlobTypeException | ClassNotFoundException ex) {
            System.out.println(ex);
        } 
        
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
    public List<EnforcableCodeElement> getFilteredElementList() {
        return filteredElementList;
    }

    /**
     * @param filteredElementList the filteredElementList to set
     */
    public void setFilteredElementList(List<EnforcableCodeElement> filteredElementList) {
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

    /**
     * @return the extendedStipCompDaysFromToday
     */
    public int getExtendedStipCompDaysFromToday() {
        return extendedStipCompDaysFromToday;
    }

    /**
     * @param extendedStipCompDaysFromToday the extendedStipCompDaysFromToday to
     * set
     */
    public void setExtendedStipCompDaysFromToday(int extendedStipCompDaysFromToday) {
        this.extendedStipCompDaysFromToday = extendedStipCompDaysFromToday;
    }

    /**
     * @return the extendedStipCompDate
     */
    public java.util.Date getExtendedStipCompDate() {
        return extendedStipCompDate;
    }

    /**
     * @param extendedStipCompDate the extendedStipCompDate to set
     */
    public void setExtendedStipCompDate(java.util.Date extendedStipCompDate) {
        this.extendedStipCompDate = extendedStipCompDate;
    }

    /**
     * @return the extendStipCompUsingDate
     */
    public boolean isExtendStipCompUsingDate() {
        return extendStipCompUsingDate;
    }

    /**
     * @param extendStipCompUsingDate the extendStipCompUsingDate to set
     */
    public void setExtendStipCompUsingDate(boolean extendStipCompUsingDate) {
        this.extendStipCompUsingDate = extendStipCompUsingDate;
    }

  

    /**
     * @return the severityList
     */
    public List<IntensityClass> getSeverityList() {
        return severityList;
    }

    /**
     * @param severityList the severityList to set
     */
    public void setSeverityList(List<IntensityClass> severityList) {
        this.severityList = severityList;
    }

    /**
     * @return the selectedViewOption
     */
    public ViewOptionsActiveListsEnum getSelectedViewOption() {
        return selectedViewOption;
    }

    /**
     * @param selectedViewOption the selectedViewOption to set
     */
    public void setSelectedViewOption(ViewOptionsActiveListsEnum selectedViewOption) {
        this.selectedViewOption = selectedViewOption;
    }

    /**
     * @return the viewOptionList
     */
    public List<ViewOptionsActiveListsEnum> getViewOptionList() {
        return viewOptionList;
    }

    /**
     * @param viewOptionList the viewOptionList to set
     */
    public void setViewOptionList(List<ViewOptionsActiveListsEnum> viewOptionList) {
        this.viewOptionList = viewOptionList;
    }

}
