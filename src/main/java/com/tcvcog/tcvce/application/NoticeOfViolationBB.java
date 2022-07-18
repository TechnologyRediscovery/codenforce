/*
 * Copyright (C) 2018 Turtle Creek Valley
 * Council of Governments, PA
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

import com.tcvcog.tcvce.coordinators.*;
import com.tcvcog.tcvce.domain.*;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.integration.*;
import com.tcvcog.tcvce.util.*;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveListsEnum;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 * Premier backing bean for Notices of Violation, which are now called 
 * generically "Letters"
 * @author ellen bascomb of apt 31y
 */
public class NoticeOfViolationBB extends BackingBeanUtils implements Serializable {

    private CECaseDataHeavy currentCase;

    private NoticeOfViolation currentNotice;
    private List<CodeViolation> activeVList;
    private CodeViolation currentViolation;

    private Person noticePerson;
    private String formNoteText;

    private boolean useManualTextBlockMode;
    
    private List<TextBlock> blockList;
    private List<TextBlockCategory> blockCatList;
    
    final static String NOV_TYPE_EVCAT_PARAM = "novtype-eventcat-field";
    private String currentNovTypeEventCatField;
    
    private NoticeOfViolationType currentNOVType;
    private List<NoticeOfViolationType> novTypeList;
    private boolean editModeNOVType;
    private List<PrintStyle> printStyleList;
    private List<EventType> eventTypeCandidateList;
    private EventType eventTypeSelected;
    private List<EventCategory> eventCatCandidateList;
    private List<BlobLight> novHeaderImageCandidateList;
    private NoticeOfViolationType selectedNOVType;
    private boolean showTextBlocksAllMuni;
    private boolean showTextBlocksAllCategories;
    
    private List<TextBlock> injectableBlockList;
    private TextBlock currentTemplateBlock;
    
    private Map<String, Integer> blockCatIDMap;
    private String selectedBlockTemplate;

    private List<HumanLink> personCandidateList;
    private List<Person> recipientPersonCandidateList;

    private Person selectedRecipientPerson;
    private MailingAddress selectedRecipAddr;
    
    private User notifyingOfficerCandidateChosen;

    private boolean personLookupUseID;
    private Person retrievedManualLookupPerson;
    private int recipientPersonID;


    private List<ViewOptionsActiveListsEnum> viewOptionList;
    private ViewOptionsActiveListsEnum selectedViewOption;
    
    private boolean nov_createNoticeFollowupEvent;
    
    // MIGRATED FROM TEXT BLOCK BB
    
    
    private List<TextBlock> filteredBlockList;
    private TextBlock selectedBlock;
    
    
    

    /**
     * Creates a new instance of NoticeOfViolationBB
     */
    public NoticeOfViolationBB() {

    }

    /**
     * Sets up bean members based on the session's current CECase
     */
    @PostConstruct
    public void initBean() {
        CaseCoordinator cc = getCaseCoordinator();
        PropertyCoordinator pc = getPropertyCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        EventCoordinator evc = getEventCoordinator();
        
        currentNotice = getSessionBean().getSessNotice();
       
        refreshCurrentCase();

        recipientPersonCandidateList = new ArrayList<>();
        showTextBlocksAllMuni = false;
        useManualTextBlockMode = false;

        Municipality m = getSessionBean().getSessMuni();
        
        try {
            blockCatList = sc.getTextBlockCategoryListComplete();
           
            novTypeList = cc.nov_getNOVTypeList(null);
            eventCatCandidateList = evc.getEventCategeryList(EventType.Notice);
            printStyleList = sc.getPrintStyleList();
        } catch (IntegrationException  | BObStatusException | BlobException  ex) {
            System.out.println(ex);
        } 
      
        
        nov_createNoticeFollowupEvent = true;
        
        UserCoordinator uc = getUserCoordinator();
        
        
    } // close initbean
    
    /**
     * Asks coordinator for new case data
     */
    private void refreshCurrentCase(){
        PropertyCoordinator pc = getPropertyCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
         try {
            currentCase = cc.cecase_assembleCECaseDataHeavy(getSessionBean().getSessCECase(), getSessionBean().getSessUser());
        } catch (BObStatusException | IntegrationException | SearchException ex) {
            System.out.println(ex);
        }
        
    }
    
    
    private void refreshCurrentPersonCandidateList(){
        PropertyCoordinator pc = getPropertyCoordinator();
         PropertyDataHeavy pdh = null;
        try {
            pdh = pc.assemblePropertyDataHeavy(currentCase.getProperty(), getSessionBean().getSessUser());
        } catch (IntegrationException | BObStatusException | SearchException | BlobException ex) {
            System.out.println(ex);
        } 
        if(pdh != null){
            personCandidateList = pdh.getHumanLinkList();
        }
        if (personCandidateList != null) {
            System.out.println("NoticeOfViolationBuilderBB.initbean "
                    + "| person candidate list size: " + personCandidateList.size());
        }
        
    }
    
    
    
    
    
    /**
     * listener for type change
     * @param ev 
     */
    public void onNOVTypeChangeInit(ActionEvent ev){
        System.out.println("NoticeOfViolationBB.onNOVTypeChangeInit");
        CaseCoordinator cc = getCaseCoordinator();
        try {
            novTypeList =cc.nov_getNOVTypeList(getSessionBean().getSessMuni());
        } catch (IntegrationException | BlobException ex) {
            System.out.println(ex);
        } 
        
        
    }
    
    /**
     * Listener for type change commit requests
     * @param ev 
     */
    public void onNOVTypeChangeCommit(ActionEvent ev){
        CaseCoordinator cc = getCaseCoordinator();
        if(currentNotice != null){
            currentNotice.setNovType(selectedNOVType);
            try {
                cc.nov_update(currentNotice);
                currentNotice = cc.nov_getNoticeOfViolation(currentNotice.getNoticeID());
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "You Lucky Duck: Updated of letter type: Success!", ""));
            } catch (IntegrationException | BObStatusException | BlobException ex) {
                System.out.println(ex);
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Could not update this letter's type due to a fatal error.", ""));
            }
        }
    }
    
    /**
     * Listener to start the NOV type manage process
     * @param ev 
     */
    public void onNOVTypeManageButtonClick(ActionEvent ev){
        System.out.println("NoticeOfViolationBB.onNOVTypeManageButtonClick");
        refreshCurrentNOVTypeAndList();
        PropertyCoordinator pc = getPropertyCoordinator();
        BlobCoordinator bc = getBlobCoordinator();
        try {
            novHeaderImageCandidateList = bc.getBlobLightList(getSessionBean().getSessMuni().getMuniPropertyDH());
        } catch (BObStatusException | BlobException | IntegrationException  ex) {
            System.out.println(ex);
        } 
        
    }
    
    /**
     * Gets our total NOV type list showing all munis
    */
    private void refreshCurrentNOVTypeAndList(){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            if(currentNOVType != null && currentNOVType.getTypeID() != 0){
                currentNOVType = cc.nov_getNOVType(currentNOVType.getTypeID());
            }
            novTypeList = cc.nov_getNOVTypeList(null);
            System.out.println("NoticeOfViolationBB.refreshCurrentNOVTypeAndList");
        } catch (IntegrationException | BlobException ex) {
            System.out.println(ex);
        }
        
    }
    
    /**
     * Listener for user requests to view an NOVType
     * @param novt 
     */
    public void onNOVTypeView(NoticeOfViolationType novt){
        currentNOVType = novt;
        editModeNOVType = false;
    }
    
    /**
     * Listener for user requesets to edit an NOV type
     * @param novt 
     */
    public void onNOVTypeEdit(NoticeOfViolationType novt){
        currentNOVType = novt;
        editModeNOVType = true;
    }
    
    /**
     * Toggles the NOV record edit mode
     * @param ev 
     */
    public void onToggleEditModeNOVType(ActionEvent ev){
        System.out.println("NoticeOfViolationBB.onToggleEditModeNOVType | incoming edit mode: " + editModeNOVType);
        if(editModeNOVType){
            if(currentNOVType != null){
                if(currentNOVType.getTypeID() == 0){
                    insertCommitNOVType();
                } else {
                    updateCurrentNOVType();
                }
            }
        } else {
            // do nothing
        }
        
        editModeNOVType = !editModeNOVType;
        
        
    }
    
    /**
     * Starts the NOV type creation process
     * @param ev 
     */
    public void onNOVTypeAddInitButtonChange(ActionEvent ev){
        CaseCoordinator cc = getCaseCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        currentNOVType = cc.nov_getNoticeOfViolationTypeSkeleton();
        currentNOVType.setMuni(getSessionBean().getSessMuni());
        try {
            blockCatList = sc.getTextBlockCategoryListComplete();
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
        } 
        editModeNOVType = true; 
        
    }
    
    /**
     * Internal update method for NOV types
     */
    private void updateCurrentNOVType(){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.nov_updateNOVType(currentNOVType);
            refreshCurrentNOVTypeAndList();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Successfully updated a new letter type with ID: " + currentNOVType.getTypeID(), ""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Could not update a letter type with ID: " + currentNOVType.getTypeID(), ""));
        } 
        
        
    }
    
    /**
     * Internal insertion method for NOV types
     */
    private void insertCommitNOVType(){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            
            int freshid = cc.nov_insertNOVType(currentNOVType);
            currentNOVType.setTypeID(freshid);
            refreshCurrentNOVTypeAndList();
            getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Successfully added a new letter type with ID: " + currentNOVType.getTypeID(), ""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Could not insert a new letter type!", ""));
        } 
    }
    
    /**
     * Listener for user requests to start the NOV type deac process
     * @param novt 
     */
    public void onDeactivateNOVTypeInit(NoticeOfViolationType novt){
        currentNOVType = novt;
        
    }
    
    /**
     * Listener for user requests to complete the nov type deac process
     * @param ev 
     */
    public void onDeactivateNOVTypeCommit(ActionEvent ev){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.nov_deactivateNOVType(currentNOVType);
            refreshCurrentNOVTypeAndList();
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Successfully deactivated a letter type with ID: " + currentNOVType.getTypeID(), ""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Could not deactivated letter type with ID: " + currentNOVType.getTypeID(), ""));
            
        } 
        
    }
    
    /**
     * Listener for user requests to stop editing an NOV type
     * @param ev 
     */
    public void onNOVTypeEditAbort(ActionEvent ev){
        editModeNOVType = false;
    }
    
    /**
     * Listener for use requests to start the header image select process
     * @param ev 
     */
    public void onNOVTypeHeaderImageSelectInit(ActionEvent ev){
        System.out.println("Header Image select start");
    }
    
    /**
     * Listener for user requests to select a blob for use as a header image
     * on an NOV
     * @param bl 
     */
    public void onNOVTypeHeaderImageSelectBlobLinkClick(BlobLight bl){
        if(currentNOVType != null){
            currentNOVType.setNovHeaderBlob(bl);
            
        }
    }
    
    /**
     * Listener for user changes to the NOV type drop down box
     * @param ev 
     */
    public void onNOVTypeChange(){
        if(currentNotice != null && currentNotice.getNovType() != null){
            SystemCoordinator sc = getSystemCoordinator();
            
            try {
                if (showTextBlocksAllMuni) {
                    if(showTextBlocksAllCategories){
                        blockList = sc.getTextBlockList(null, null);
                    } else {
                        blockList = sc.getTextBlockList(currentNotice.getNovType().getTextBlockCategory(), null);
                    }
                } else {
                    if(showTextBlocksAllCategories){
                        blockList = sc.getTextBlockList(null, getSessionBean().getSessMuni());
                    } else {
                        blockList = sc.getTextBlockList(currentNotice.getNovType().getTextBlockCategory(), getSessionBean().getSessMuni());
                    }
                }
            } catch (IntegrationException | BObStatusException ex) {
                System.out.println(ex);
            }
            getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_INFO,
                               "Loaded text blocks for Letter type: " + currentNotice.getNovType().getTitle(),
                               ""));
            
        }
        if(blockList != null){
            System.out.println("NoticeOfViolationBB.onNOVTypeChange | Block list size: " + blockList.size());
        } else {
            System.out.println("NoticeOfViolationBB.onNOVTypeChange | Block list null");
        }
        
    }
    
    /**
     * Listener to start the event cat choice process for NOV type events
     * @param ev 
     */
    public void onChooseEventCatInit(ActionEvent ev){
        EventCoordinator ec = getEventCoordinator();
        currentNovTypeEventCatField = getFacesContext().getExternalContext().getRequestParameterMap().get(NOV_TYPE_EVCAT_PARAM);
        
        eventTypeCandidateList = ec.getEventTypesAll();
         onEventTypeListChange();
         
        
    }
    
    /**
     * listener for user changes of the event type drop down
     * @param et 
     */
    public void onEventTypeListChange(){
        EventCoordinator ec = getEventCoordinator();
        if(eventTypeCandidateList != null && !eventTypeCandidateList.isEmpty()){
            try {
                if(eventTypeSelected == null){
                    eventTypeSelected = eventTypeCandidateList.get(0);
                } 
                eventCatCandidateList = ec.getEventCategeryList(eventTypeSelected);
                
                if(eventCatCandidateList != null && !eventCatCandidateList.isEmpty()){
                    eventCatCandidateList = ec.assembleEventCategoryListActiveOnly(eventCatCandidateList);
                    System.out.println("NoticeOfViolationBB.onEventTypeListChange | Candidate cat list size: " + eventCatCandidateList.size());
                }
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
        }
    }
    
    /**
     * listener to finalize teh event cat choice process for NOV type events
     * @param evcat 
     */
    public void onChooseEventCatCommit(EventCategory evcat){
        System.out.println("NoticeOfViolationBB.chooseEventCatCommit | passed in cat id: " + evcat.getCategoryID() );
        if(currentNovTypeEventCatField != null && currentNOVType != null){
            switch(currentNovTypeEventCatField){
                
                case "sent":
                    currentNOVType.setEventCatSent(evcat);
                    break;
                case "followup":
                    currentNOVType.setEventCatFollowUp(evcat);
                    break;
                case "returned":
                    currentNOVType.setEventCatReturned(evcat);
                    break;
                default:
                    System.out.println("Unrecognized event cat field on NOV type");
            }
        } else {
            System.out.println("Error on NOV event cat type setup");
        }
    }
    
    
    
    /**
     * @return the blockList
     */
    public List<TextBlock> getBlockList() {
   
        return blockList;
    }

    /**
     * @return the filteredBlockList
     */
    public List<TextBlock> getFilteredBlockList() {
        return filteredBlockList;
    }

    /**
     * @return the selectedBlock
     */
    public TextBlock getSelectedBlock() {
        if(selectedBlock == null){
            setSelectedBlock(new TextBlock());
        }
        return selectedBlock;
    }

   
 
 
    /**
     * Primary listener method which copies a reference to the selected user
     * from the list and sets it on the selected user perch
     *
     * @param nov
     */
    public void onObjectViewButtonChange(NoticeOfViolation nov) {

        if (nov != null) {
            getSessionBean().setSessNotice(nov);
            currentNotice = nov;
            
            
            System.out.println("NoticeOfViolationBB.onObjectViewButtonChange: " + nov.getNoticeID());
        }
        System.out.println("NoticeOfViolationBB.onObjectViewButtonChange: " + nov);

    }

    /**
     * Listener for user requests to start the sending dialog 
     * @param nov 
     */
    public void markNoticeOfViolationAsSentInit(NoticeOfViolation nov){
        currentNotice = nov;
        currentNotice.setFollowupEventDaysRequest(20);
        getSessionBean().setSessNotice(currentNotice);
        System.out.println("NoticeOfViolationBB.markNoticeOfViolationAsSentInit: NOV " + currentNotice.getNoticeID());
    }
    
    
    /**
     * Listener for finalization of NOV sending
     * @param ev
     * @return 
     */
    public void markNoticeOfViolationAsSent(ActionEvent ev) {
        CaseCoordinator caseCoord = getCaseCoordinator();
        currentNotice = getSessionBean().getSessNotice();
        try {
            caseCoord.nov_markAsSent(currentCase, currentNotice, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Marked notice as sent and added event to case",
                            ""));
            refreshCurrentCase();
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
        } catch (EventException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to generate case event to log phase change",
                            "Note that because this message is being displayed, the phase change"
                            + "has probably succeeded"));
        }

        
        
    }
    
    /***
     * Listener for user requests to start follow-up event creation
     * @param ev 
     */
    public void onNOVFollowupEventCreateButtonPush(ActionEvent ev){
        if(currentNotice != null){
            currentNotice.setFollowupEventDaysRequest(recipientPersonID);
        }
    }
    
   
    
    public void onNoticeDetailsButtonChange(NoticeOfViolation nov){
        currentNotice = nov;
        
    }

     public String resetNotice() {
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.nov_ResetMailing(getCurrentNotice(), getSessionBean().getSessUser());
//            refreshCurrentCase();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Notice mailing status has been reset", ""));
            refreshCurrentCase();
            return "ceCaseSearchProfile";
            
        } catch (IntegrationException | AuthorizationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
        }
        return "";
    }
    
    public void markNoticeOfViolationAsReturned(ActionEvent ev) {
        CaseCoordinator caseCoord = getCaseCoordinator();
        try {
            caseCoord.nov_markAsReturned(currentCase, currentNotice, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Notice no. " + getCurrentNotice().getNoticeID()
                            + " has been marked as returned on today's date", ""));
            refreshCurrentCase();
        } catch (IntegrationException | BObStatusException  ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
            
        }
        
    }
    
    public String deleteNoticeOfViolation() {
        CaseCoordinator caseCoord = getCaseCoordinator();
        try {
            caseCoord.nov_delete(getCurrentNotice());
            currentCase = caseCoord.cecase_assembleCECaseDataHeavy(currentCase, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Notice no. " + getCurrentNotice().getNoticeID() + " has been nuked forever", ""));
            refreshCurrentCase();
            return "ceCaseSearchProfile";
        } catch (BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to delete this notice of violation, "
                            + "probably because it has been sent already", ""));
        } catch (IntegrationException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));

        }
        return "";
    }
    
    public void lockNoticeAndQueueForMailing(NoticeOfViolation nov) {
        CaseCoordinator caseCoord = getCaseCoordinator();

        try {
            caseCoord.nov_LockAndQueue(currentCase, nov, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Notice is locked and ready to be mailed!", ""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
        } catch (EventException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "The automatic event generation associated with this action has thrown an error. "
                            + "Please create an event manually which logs this letter being queued for mailing", ""));

        } catch (ViolationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Unable to queue notice of violatio. "
                            + "Please create an event manually which logs this letter being queued for mailing", ""));
        }
    }
    
    /**
     * Listener for user requests to update the NOV's notifying officer
     * @param usr 
     */
    public void changeNotifyingOfficer(){
        currentNotice.setNotifyingOfficer(notifyingOfficerCandidateChosen);
        
    }

    /**
     * Internal logic container for beginning the user creation change process
     * Delegated from the mode button router
     * @param ev
     */
    public void onModeInsertInit() {
        CaseCoordinator cc = getCaseCoordinator();
        NoticeOfViolation nov;
        refreshCurrentCase();
        try {
            nov = cc.nov_GetNewNOVSkeleton(currentCase, getSessionBean().getSessMuni(), getSessionBean().getSessUser());
            nov.setCreationBy(getSessionBean().getSessUser());
            currentNotice = nov;
            getSessionBean().setSessNotice(currentNotice);
        } catch (AuthorizationException ex) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Database error", ""));
        }
        if (!currentCase.getViolationListUnresolved().isEmpty()) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Skeleton notice created", ""));

        } else {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "No unresolved violations exist for building a letter.", ""));
        }
    }

    public void onNOVEditTextInitButtonChange(ActionEvent ev) {

    }

    public void onBuildNOVUsingBlocks(){
        CaseCoordinator cc = getCaseCoordinator();
        if(currentNotice != null && selectedBlockTemplate != null){
            try {
                currentNotice = cc.nov_assembleNOVFromBlocks(currentNotice, blockCatIDMap.get(selectedBlockTemplate));
            } catch (IntegrationException ex) {
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to build based on template", ""));
            }
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_INFO,
                       "Assembled block using template", ""));
        }
    }
    
    
    public void onStartNewNoticeButtonChange(ActionEvent ev){
        onModeInsertInit();
        CaseCoordinator cc = getCaseCoordinator();
        try {
            novTypeList = cc.nov_getNOVTypeList(getSessionBean().getSessMuni());
            getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_INFO,
                           "Building a new notice; Next: Apply template", ""));
        } catch (IntegrationException | BlobException ex) {
            System.out.println(ex);
        }
    }
    
    
    /**
     * Listener for user requests to update template
     * @param tb 
     */
    public void onTemplateEditButtonChange(TextBlock tb){
        currentTemplateBlock = tb;
    }
    
     /**
     * Listener for user requests to edit a selected template in a table
     * @param ev 
     */
    public void onTemplateViewSelectedButtonChange(ActionEvent ev){
        System.out.println("NOVBB.onTemplateViewSelectedButtonChange | currentTemplateBlock " + currentTemplateBlock.getBlockID());
     }
    
    /**
     * Listener for user requests to bring up the choose person dialog
     *
     * @param ev
     */
    public void onChoosePersonInitButtonChange(ActionEvent ev) {
        prepareRecipientPersonList();
    }

    /**
     * Converts the session property's human link list to a person list with addresses
     */
    private void prepareRecipientPersonList(){
        PersonCoordinator pc = getPersonCoordinator();
        try {
            recipientPersonCandidateList = pc.getPersonListFromHumanLinkList(pc.getHumanLinkList(getSessionBean().getSessProperty()));
            recipientPersonCandidateList.addAll(pc.getPersonListFromHumanLinkList(pc.getHumanLinkList(getSessionBean().getSessCECase())));
            System.out.println("NoticeOfViolationBB.prepareRecipientPersonList | recpient cadidate list size: " + recipientPersonCandidateList.size());
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
        } 
    }

    /**
     * Listener for user requests to build NOV with template
     * @param temp 
     */
    public void onBuildNOVUsingTemplateBlock(TextBlock temp){
        CaseCoordinator cc = getCaseCoordinator();
        currentTemplateBlock = temp;
        try {
            currentNotice = cc.nov_assembleNOVFromTemplate(currentNotice, currentTemplateBlock, currentCase);
            prepareRecipientPersonList();
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_INFO,
                       "Assembled block using template", ""));
        } catch (BObStatusException ex) {
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_ERROR,
                       "Unable to build notice from template.", ""));
            System.out.println(ex);
        }
    }
    
    /**
     * Listener for user requests to view template
     * @param temp 
     */
    public void onTemplateViewButtonChange(TextBlock temp){
        currentTemplateBlock = temp;
        System.out.println("NoticeOfViolationBB.onTemplateViewButtonChange | block ID: " + currentTemplateBlock.getBlockID());

        
    }

    public void loadBlocksAllMunis() {
        blockList = null;
        System.out.println("NOVBB.loadblocksAllMunis");

    }

    public void addBlockBeforeViolations(TextBlock tb) {
        if(currentNotice != null && tb != null){
            currentNotice.getBlocksBeforeViolations().add(tb);
        }
        blockList.remove(tb);
    }

    public void removeBlockBeforeViolations(TextBlock tb) {
        if(currentNotice != null && tb != null){
            currentNotice.getBlocksBeforeViolations().remove(tb);
        }
        blockList.add(tb);

    }

    public void removeBlockAfterViolations(TextBlock tb) {
        if(currentNotice != null && tb != null){
            currentNotice.getBlocksAfterViolations().remove(tb);
        }
        blockList.add(tb);

    }

    public void addBlockAfterViolations(TextBlock tb) {
        if(currentNotice != null && tb != null){
            currentNotice.getBlocksAfterViolations().add(tb);
        }
        blockList.remove(tb);
        
    }

    public void removeViolationFromList(CodeViolationDisplayable viol) {
        currentNotice.getViolationList().remove(viol);
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Done: violation ID " + viol.getViolationID() + "will not be included in letter.", ""));
    }

  
    
    /**
     * listener for user requests to complete the selection process of address and addressee
     * @param ev 
     */
    public void onCompleteRecipientAndAddressSelection(ActionEvent ev){
        if(currentNotice != null){
            if(selectedRecipientPerson != null){
                currentNotice.setRecipient(selectedRecipientPerson);
            } else {
                getFacesContext().addMessage(null,
                      new FacesMessage(FacesMessage.SEVERITY_ERROR,
                      "Recipient missing! Please select a notice recipient", ""));
            }   
            if(getSelectedRecipAddr() != null){
                currentNotice.setRecipientMailingAddress(selectedRecipAddr);
            } else {
                      getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Address missing! Please select an address to continue", ""));
            }
            
            getFacesContext().addMessage(null,
                  new FacesMessage(FacesMessage.SEVERITY_INFO,
                  "Recipient and Address chosen!", ""));
        } else {
            getFacesContext().addMessage(null,
                  new FacesMessage(FacesMessage.SEVERITY_ERROR,
                  "Fatal NOV setup fault. sorry!", ""));
        }
    }
      /**
     * Listener for user choice of a recipient person
     * @param pers 
     */
    public void storeRecipient(Person pers) {
        System.out.println("Store Recipient: " + pers);
        selectedRecipientPerson = pers;
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Notice recipient is now Person: " + pers.getLastName(), ""));

    }
    
    
    /**
     * Listener for user requests to store a recipient's mailing address
     * @param ma
     */
    public void storeRecipientAddress(MailingAddress ma){
        if(ma != null){
            System.out.println("NoticeOfViolationBB.storeRecipientAddress | address ID " + ma.getAddressID());
            selectedRecipAddr = ma;
            currentNotice.setRecipientMailingAddress(selectedRecipAddr);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Notice will be sent to " + ma.getAddressPretty1Line(), ""));
        }
        
        
    }

    public void checkNOVRecipient(ActionEvent ev) {
        PersonCoordinator pc = getPersonCoordinator();
        if (recipientPersonID != 0) {
            try {
                recipientPersonCandidateList.add(pc.getPerson(pc.getHuman(recipientPersonID)));
                System.out.println("NoticeOfViolationBB.checkNOVRecipient | looked up person: " + getRetrievedManualLookupPerson());
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Search complete", ""));
            } catch (IntegrationException | BObStatusException  ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                getRecipientPersonID() + "not mapped to a known person", "Please try again or visit our person search page."));
            }
        }
    }

  
      /**
     * Second gen listener for finalization of notices
     * @return 
     */
    public String finalizeNoticeAndPrint(){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.nov_InsertNotice(currentNotice, currentCase, getSessionBean().getSessUser());
            
        } catch (IntegrationException | BObStatusException ex) {
            
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to save NOV and print", ""));
            System.out.println("");
            return "";
        }
        getSessionBean().setSessNotice(getCurrentNotice());

        return "noticeOfViolationPrint";
    }

    private StringBuilder appendTextBlockAsPara(TextBlock tb, StringBuilder sb) {
        sb.append("<p>");
        sb.append(tb.getTextBlockText());
        sb.append("</p>");
        return sb;
    }

    /**
     * Listener for user requests to create a new notice
     * @return 
     */
    public String onInsertNewNoticeButtonChange() {
        CaseCoordinator cc = getCaseCoordinator();
        try {
                
                cc.nov_InsertNotice(currentNotice, currentCase, getSessionBean().getSessUser());
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Notice saved", ""));
            // make sure our person list is up to date
//            currentCase = getSessionBean().getSessCECase();
//            refreshCurrentCase();
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
            return "";
            
        }
        return "ceCaseProfile";
        
    } // close method
    

   
/**
 * Listener for user requests to update an existing notice
 * @return 
 */
    public String onUpdateNoticeButtonChange() {
        CaseCoordinator cc = getCaseCoordinator();
        try {
            if(currentNotice != null){
                cc.nov_update(currentNotice);
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Notice udated", ""));
            }

        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
            return "";
        }
        return "ceCaseProfile";
    } // close method

   

    public String printNotice() {
        getSessionBean().setSessNotice(currentNotice);
//        positionCurrentCaseAtHeadOfQueue();
        return "noticeOfViolationPrint";
    }

    public String printNotice(NoticeOfViolation nov) {
        currentNotice = nov;
        getSessionBean().setSessNotice(currentNotice);
//        positionCurrentCaseAtHeadOfQueue();
        return "noticeOfViolationPrint";
    }


 
    /**
     * Listener for changes to text block template list selected items
     * @param tb
     */
    public void onTemplateBlockViewChange(TextBlock tb){
        currentTemplateBlock = tb;
    }
    
   
  
    
   
    /**
     * Listener for calls to abort a change operation
     *
     * @return
     */
    public String onAbortOperationButtonChange() {
        getSessionBean().setCeCaseNoticesPageModeRequest(PageModeEnum.VIEW);
        return "ceCaseNotices";
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
        mbp.setExistingContent(currentNotice.getNotes());
        mbp.setNewMessageContent(getFormNoteText());
        mbp.setHeader("Notice of Violation Note");
        mbp.setUser(getSessionBean().getSessUser());

        try {

            cc.nov_updateNotes(mbp, currentNotice);
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

        return "ceCaseNotices";

    }

     /**
      * Special getter for person links--I check the session bean's
      * PersonLinkList to see if there is a new list to send to the UI
      * 
     * @return the recipientPersonCandidateList
     */
    public List<Person> getRecipientPersonCandidateList() {
        if(getSessionBean().getSessHumanListRefreshedList() != null){
            PersonCoordinator pc = getPersonCoordinator();
            try {
                recipientPersonCandidateList = pc.getPersonListFromHumanLinkList(getSessionBean().getSessHumanListRefreshedList());
                getSessionBean().setSessHumanListRefreshedList(null);
            } catch (IntegrationException | BObStatusException ex) {
                System.out.println(ex);
            } 
        }
        return recipientPersonCandidateList;
    }
    
    
    
    /**
     * @return the currentNotice
     */
    public NoticeOfViolation getCurrentNotice() {

        currentNotice = getSessionBean().getSessNotice();
        return currentNotice;
    }

    /**
     * @param currentNotice the currentNotice to set
     */
    public void setCurrentNotice(NoticeOfViolation currentNotice) {
        this.currentNotice = currentNotice;
    }

    /**
     * @param textBlockListByMuni the textBlockListByMuni to set
     */
    public void setTextBlockListByMuni(ArrayList<TextBlock> textBlockListByMuni) {
        this.blockList = textBlockListByMuni;
    }

    /**
     * @return the activeVList
     */
    public List<CodeViolation> getActiveVList() {
        if (activeVList == null) {
            activeVList = getSessionBean().getSessViolationList();
        }
        return activeVList;
    }

    /**
     * @param activeVList the activeVList to set
     */
    public void setActiveVList(ArrayList<CodeViolation> activeVList) {
        this.activeVList = activeVList;
    }

    /**
     * @return the personCandidateList
     */
    public List<HumanLink> getPersonCandidateList() {
        return personCandidateList;
    }

    /**
     * @param personCandidateAL the personCandidateList to set
     */
    public void setPersonCandidateAL(List<HumanLink> personCandidateAL) {
        this.personCandidateList = personCandidateAL;
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
     * @return the retrievedManualLookupPerson
     */
    public Person getRetrievedManualLookupPerson() {
        return retrievedManualLookupPerson;
    }

    /**
     * @return the recipientPersonID
     */
    public int getRecipientPersonID() {
        return recipientPersonID;
    }

    /**
     * @param retrievedManualLookupPerson the retrievedManualLookupPerson to set
     */
    public void setRetrievedManualLookupPerson(Person retrievedManualLookupPerson) {
        this.retrievedManualLookupPerson = retrievedManualLookupPerson;
    }

    /**
     * @param recipientPersonID the recipientPersonID to set
     */
    public void setRecipientPersonID(int recipientPersonID) {
        this.recipientPersonID = recipientPersonID;
    }

   

    /**
     * @param recipientPersonCandidateList the recipientPersonCandidateList to set
     */
    public void setRecipientPersonCandidateList(List<Person> recipientPersonCandidateList) {
        this.recipientPersonCandidateList = recipientPersonCandidateList;
    }

    /**
     * @return the showTextBlocksAllMuni
     */
    public boolean isShowTextBlocksAllMuni() {
        return showTextBlocksAllMuni;
    }

    /**
     * @param showTextBlocksAllMuni the showTextBlocksAllMuni to set
     */
    public void setShowTextBlocksAllMuni(boolean showTextBlocksAllMuni) {
        this.showTextBlocksAllMuni = showTextBlocksAllMuni;
    }

    /**
     * @return the noticePerson
     */
    public Person getNoticePerson() {
        return noticePerson;
    }

    /**
     * @param noticePerson the noticePerson to set
     */
    public void setNoticePerson(Person noticePerson) {
        this.noticePerson = noticePerson;
    }

    /**
     * @return the viewOptionList
     */
    public List<ViewOptionsActiveListsEnum> getViewOptionList() {
        return viewOptionList;
    }

    /**
     * @return the selectedViewOption
     */
    public ViewOptionsActiveListsEnum getSelectedViewOption() {
        return selectedViewOption;
    }

    /**
     * @param viewOptionList the viewOptionList to set
     */
    public void setViewOptionList(List<ViewOptionsActiveListsEnum> viewOptionList) {
        this.viewOptionList = viewOptionList;
    }

    /**
     * @param selectedViewOption the selectedViewOption to set
     */
    public void setSelectedViewOption(ViewOptionsActiveListsEnum selectedViewOption) {
        this.selectedViewOption = selectedViewOption;
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
     * @return the personLookupUseID
     */
    public boolean isPersonLookupUseID() {
        return personLookupUseID;
    }

    /**
     * @param personLookupUseID the personLookupUseID to set
     */
    public void setPersonLookupUseID(boolean personLookupUseID) {
        this.personLookupUseID = personLookupUseID;
    }

    /**
     * @return the blockCatIDMap
     */
    public Map<String, Integer> getBlockCatIDMap() {
        return blockCatIDMap;
    }

    /**
     * @param blockCatIDMap the blockCatIDMap to set
     */
    public void setBlockCatIDMap(Map<String, Integer> blockCatIDMap) {
        this.blockCatIDMap = blockCatIDMap;
    }

    /**
     * @return the selectedBlockTemplate
     */
    public String getSelectedBlockTemplate() {
        return selectedBlockTemplate;
    }

    /**
     * @param selectedBlockTemplate the selectedBlockTemplate to set
     */
    public void setSelectedBlockTemplate(String selectedBlockTemplate) {
        this.selectedBlockTemplate = selectedBlockTemplate;
    }

    /**
     * @return the blockCatList
     */
    public List<TextBlockCategory> getBlockCatList() {
        return blockCatList;
    }

    /**
     * @param blockCatList the blockCatList to set
     */
    public void setBlockCatList(List<TextBlockCategory> blockCatList) {
        this.blockCatList = blockCatList;
    }

    /**
     * @return the useManualTextBlockMode
     */
    public boolean isUseManualTextBlockMode() {
        return useManualTextBlockMode;
    }

    /**
     * @param useManualTextBlockMode the useManualTextBlockMode to set
     */
    public void setUseManualTextBlockMode(boolean useManualTextBlockMode) {
        this.useManualTextBlockMode = useManualTextBlockMode;
    }

    /**
     * @return the injectableBlockList
     */
    public List<TextBlock> getInjectableBlockList() {
        return injectableBlockList;
    }

    /**
     * @param injectableBlockList the injectableBlockList to set
     */
    public void setInjectableBlockList(List<TextBlock> injectableBlockList) {
        this.injectableBlockList = injectableBlockList;
    }

    /**
     * @return the currentTemplateBlock
     */
    public TextBlock getCurrentTemplateBlock() {
        return currentTemplateBlock;
    }

    /**
     * @param currentTemplateBlock the currentTemplateBlock to set
     */
    public void setCurrentTemplateBlock(TextBlock currentTemplateBlock) {
        this.currentTemplateBlock = currentTemplateBlock;
    }

   

    /**
     * @param filteredBlockList the filteredBlockList to set
     */
    public void setFilteredBlockList(List<TextBlock> filteredBlockList) {
        this.filteredBlockList = filteredBlockList;
    }

    /**
     * @param selectedBlock the selectedBlock to set
     */
    public void setSelectedBlock(TextBlock selectedBlock) {
        this.selectedBlock = selectedBlock;
    }

  

    /**
     * @return the draftNoticeLoaded
     */
    public boolean isDraftNoticeLoaded() {
        boolean d = false;
        if(currentNotice != null && currentNotice.getNoticeID() == 0){
            d = true;
        }
        return d;
    }

   

    /**
     * @return the nov_createNoticeFollowupEvent
     */
    public boolean isNov_createNoticeFollowupEvent() {
        return nov_createNoticeFollowupEvent;
    }

    /**
     * @param nov_createNoticeFollowupEvent the nov_createNoticeFollowupEvent to set
     */
    public void setNov_createNoticeFollowupEvent(boolean nov_createNoticeFollowupEvent) {
        this.nov_createNoticeFollowupEvent = nov_createNoticeFollowupEvent;
    }

    /**
     * @return the notifyingOfficerCandidateChosen
     */
    public User getNotifyingOfficerCandidateChosen() {
        return notifyingOfficerCandidateChosen;
    }

    /**
     * @param notifyingOfficerCandidateChosen the notifyingOfficerCandidateChosen to set
     */
    public void setNotifyingOfficerCandidateChosen(User notifyingOfficerCandidateChosen) {
        this.notifyingOfficerCandidateChosen = notifyingOfficerCandidateChosen;
    }

   

   

    /**
     * @param sr
     */
    public void setSelectedRecipAddr(MailingAddressLink sr) {
        this.selectedRecipAddr = sr;
    }

    /**
     * @return the selectedRecipientPerson
     */
    public Person getSelectedRecipientPerson() {
        return selectedRecipientPerson;
    }

    /**
     * @param selectedRecipientPerson the selectedRecipientPerson to set
     */
    public void setSelectedRecipientPerson(Person selectedRecipientPerson) {
        this.selectedRecipientPerson = selectedRecipientPerson;
    }

    /**
     * @return the selectedRecipAddr
     */
    public MailingAddress getSelectedRecipAddr() {
        return selectedRecipAddr;
    }

    /**
     * @param selectedRecipAddr the selectedRecipAddr to set
     */
    public void setSelectedRecipAddr(MailingAddress selectedRecipAddr) {
        this.selectedRecipAddr = selectedRecipAddr;
    }

    /**
     * @return the novTypeList
     */
    public List<NoticeOfViolationType> getNovTypeList() {
        return novTypeList;
    }

    /**
     * @param novTypeList the novTypeList to set
     */
    public void setNovTypeList(List<NoticeOfViolationType> novTypeList) {
        this.novTypeList = novTypeList;
    }

    /**
     * @return the currentNOVType
     */
    public NoticeOfViolationType getCurrentNOVType() {
        return currentNOVType;
    }

    /**
     * @param currentNOVType the currentNOVType to set
     */
    public void setCurrentNOVType(NoticeOfViolationType currentNOVType) {
        this.currentNOVType = currentNOVType;
    }

    /**
     * @return the editModeNOVType
     */
    public boolean isEditModeNOVType() {
        return editModeNOVType;
    }

    /**
     * @param editModeNOVType the editModeNOVType to set
     */
    public void setEditModeNOVType(boolean editModeNOVType) {
        this.editModeNOVType = editModeNOVType;
    }

    /**
     * @return the printStyleList
     */
    public List<PrintStyle> getPrintStyleList() {
        return printStyleList;
    }

    /**
     * @param printStyleList the printStyleList to set
     */
    public void setPrintStyleList(List<PrintStyle> printStyleList) {
        this.printStyleList = printStyleList;
    }

    /**
     * @return the eventCatCandidateList
     */
    public List<EventCategory> getEventCatCandidateList() {
        return eventCatCandidateList;
    }

    /**
     * @param eventCatCandidateList the eventCatCandidateList to set
     */
    public void setEventCatCandidateList(List<EventCategory> eventCatCandidateList) {
        this.eventCatCandidateList = eventCatCandidateList;
    }

    /**
     * @return the currentNovTypeEventCatField
     */
    public String getCurrentNovTypeEventCatField() {
        return currentNovTypeEventCatField;
    }

    /**
     * @param currentNovTypeEventCatField the currentNovTypeEventCatField to set
     */
    public void setCurrentNovTypeEventCatField(String currentNovTypeEventCatField) {
        this.currentNovTypeEventCatField = currentNovTypeEventCatField;
    }

    /**
     * @return the eventTypeCandidateList
     */
    public List<EventType> getEventTypeCandidateList() {
        return eventTypeCandidateList;
    }

    /**
     * @param eventTypeCandidateList the eventTypeCandidateList to set
     */
    public void setEventTypeCandidateList(List<EventType> eventTypeCandidateList) {
        this.eventTypeCandidateList = eventTypeCandidateList;
    }

    /**
     * @return the eventTypeSelected
     */
    public EventType getEventTypeSelected() {
        return eventTypeSelected;
    }

    /**
     * @param eventTypeSelected the eventTypeSelected to set
     */
    public void setEventTypeSelected(EventType eventTypeSelected) {
        this.eventTypeSelected = eventTypeSelected;
    }

    /**
     * @return the novHeaderImageCandidateList
     */
    public List<BlobLight> getNovHeaderImageCandidateList() {
        return novHeaderImageCandidateList;
    }

    /**
     * @param novHeaderImageCandidateList the novHeaderImageCandidateList to set
     */
    public void setNovHeaderImageCandidateList(List<BlobLight> novHeaderImageCandidateList) {
        this.novHeaderImageCandidateList = novHeaderImageCandidateList;
    }

    /**
     * @return the selectedNOVType
     */
    public NoticeOfViolationType getSelectedNOVType() {
        return selectedNOVType;
    }

    /**
     * @param selectedNOVType the selectedNOVType to set
     */
    public void setSelectedNOVType(NoticeOfViolationType selectedNOVType) {
        this.selectedNOVType = selectedNOVType;
    }

    /**
     * @return the showTextBlocksAllCategories
     */
    public boolean isShowTextBlocksAllCategories() {
        return showTextBlocksAllCategories;
    }

    /**
     * @param showTextBlocksAllCategories the showTextBlocksAllCategories to set
     */
    public void setShowTextBlocksAllCategories(boolean showTextBlocksAllCategories) {
        this.showTextBlocksAllCategories = showTextBlocksAllCategories;
    }

}
