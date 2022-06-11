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
 *
 * @author ellen bascomb of apt 31y
 */
public class NoticeOfViolationBB extends BackingBeanUtils implements Serializable {

    private PageModeEnum currentMode;
    private List<PageModeEnum> pageModes;

    private CECaseDataHeavy currentCase;
    private boolean draftNoticeLoaded;

    private NoticeOfViolation currentNotice;
    private List<CodeViolation> activeVList;
    private CodeViolation currentViolation;

    private Person noticePerson;

    private String formNoteText;

    private boolean useManualTextBlockMode;
    
    private List<TextBlock> blockList;
    private List<String> blockCatList;
    
    private List<TextBlock> injectableBlockList;
    private TextBlock currentTemplateBlock;
    
    private Map<String, Integer> blockCatIDMap;
    private String selectedBlockTemplate;

    private List<HumanLink> personCandidateList;
    private List<Person> recipientPersonCandidateList;

    private Person selectedRecipientPerson;
    private MailingAddress selectedRecipAddr;
    
    private List<User> notifyingOfficerCandidateList;
    private User notifyingOfficerCandidateChosen;

    private boolean personLookupUseID;
    private Person retrievedManualLookupPerson;
    private int recipientPersonID;

    private boolean showTextBlocksAllMuni;

    private List<ViewOptionsActiveListsEnum> viewOptionList;
    private ViewOptionsActiveListsEnum selectedViewOption;
    
    private boolean nov_createNoticeFollowupEvent;
    
    // MIGRATED FROM TEXT BLOCK BB
    
    
    private List<TextBlock> filteredBlockList;
    
    private TextBlock selectedBlock;
    
    private HashMap<String, Integer> categoryList;
    
    private Municipality formMuni;
    
    private String formBlockName;
    private String formBlockText;
    private int formCategoryID;
    private int formBlockOrder;

    /**
     * Creates a new instance of NoticeOfViolationBB
     */
    public NoticeOfViolationBB() {

    }

    @PostConstruct
    public void initBean() {
        CaseCoordinator cc = getCaseCoordinator();
        PropertyCoordinator pc = getPropertyCoordinator();
        CaseIntegrator ci = getCaseIntegrator();
        
//        currentNotice = getSessionBean().getSessNotice();
       
        refreshCurrentCase();

        recipientPersonCandidateList = new ArrayList<>();
        showTextBlocksAllMuni = false;
        useManualTextBlockMode = false;

        Municipality m = getSessionBean().getSessMuni();
        
        try {
            injectableBlockList = ci.getTextBlockTemplates(m);
            if(currentTemplateBlock == null){
                if(injectableBlockList != null && !injectableBlockList.isEmpty()){
                    currentTemplateBlock = injectableBlockList.get(0);
                } else {
                    currentTemplateBlock = cc.nov_getTemplateBlockSekeleton(getSessionBean().getSessMuni());
                }
            } 
            if (blockList == null) {
                if (showTextBlocksAllMuni) {
                    blockList = ci.getAllTextBlocks();
                } else {
                    blockList = ci.getTextBlocks(m);
                }
            }
            blockCatIDMap = ci.getTextBlockCategoryMap();
            if(blockCatIDMap != null && !blockCatIDMap.isEmpty()){
                blockCatList = new ArrayList<>(blockCatIDMap.keySet());
                if(blockCatList != null && !blockCatList.isEmpty()){
                    selectedBlockTemplate = blockCatList.get(0);
                }
            }
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
      
        nov_createNoticeFollowupEvent = true;
        
        UserCoordinator uc = getUserCoordinator();
        try {
            notifyingOfficerCandidateList = uc.user_auth_assembleUserListForConfig(getSessionBean().getSessUser());
        } catch (AuthorizationException | IntegrationException | BObStatusException ex) {
            System.out.println(ex );
        } 
        
        
        
    } // close initbean
    
    private void refreshCurrentCase(){
        PropertyCoordinator pc = getPropertyCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
         try {
            currentCase = cc.cecase_assembleCECaseDataHeavy(getSessionBean().getSessCECase(), getSessionBean().getSessUser());
        } catch (BObStatusException | IntegrationException | SearchException ex) {
            System.out.println(ex);
        }
          PropertyDataHeavy pdh = null;
        try {
            pdh = pc.assemblePropertyDataHeavy(currentCase.getProperty(), getSessionBean().getSessUser());
        } catch (IntegrationException | BObStatusException | SearchException ex) {
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
    
    
    // MIGRATED FROM TEXT BLOCK BB
      public String updateTextBlock(){
        CaseIntegrator ci = getCaseIntegrator();
        if(selectedBlock != null){
            try {
                ci.updateTextBlock(selectedBlock);
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,  
                            "Success! Updated text block id " + selectedBlock.getBlockID(), ""));
            } catch (IntegrationException ex) {
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,  
                            "Please select a text block and try again", ""));
            }
        } else {
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, 
                            "Please select a text block and try again", ""));
            
        }
        // clear block list so the page reload forces a DB SELECT 
        blockList = null;
        return "";
    }
    
    public String addNewTextBlock(){
        CaseIntegrator ci = getCaseIntegrator();
        TextBlock newBlock = new TextBlock();
        newBlock.setMuni(getFormMuni());
        newBlock.setTextBlockCategoryID(getFormCategoryID());
        newBlock.setTextBlockName(getFormBlockName());
        newBlock.setTextBlockText(getFormBlockText());
        newBlock.setPlacementOrder(getFormBlockOrder());
        
        try {
            ci.insertTextBlock(newBlock);
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_INFO,  
                       "Success! Added a new text block named " + getFormBlockName() + "to the db!", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_ERROR,  
                       ex.getMessage(), ""));
            
        }
        blockList = null;
        return "";
    }
    
    public String nukeTextBlock(){
        CaseIntegrator ci = getCaseIntegrator();
        if(selectedBlock != null){
            try {
                ci.deleteTextBlock(selectedBlock);
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,  
                            "Success! Nuked block id " + selectedBlock.getBlockID(), ""));
            } catch (IntegrationException ex) {
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,  ex.getMessage(), ""));
            }
        } else {
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, 
                            "Please select a text block and try again", ""));
        }
        blockList = null;
        return "";
    }

    /**
     * @return the blockList
     */
    public List<TextBlock> getBlockList() {
        CaseIntegrator ci = getCaseIntegrator();
        if(blockList == null){
            try {
                blockList = ci.getAllTextBlocks();
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
        }
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
     * @return the categoryList
     */
    public HashMap<String, Integer> getCategoryList() {
        CaseIntegrator ci = getCaseIntegrator();
        try {
            setCategoryList(ci.getTextBlockCategoryMap());
            System.out.println("TextBlockBB.getCategoryMap | isempty: " + categoryList.isEmpty());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        return categoryList;
    }

 
    
    /**
     * Listener for user requests to go back to case
     * @return 
     */
    public String onBackToCaseButtonChange(){
        return "ceCaseSearchProfile";
        
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
            refreshCurrentCase();
            
            System.out.println("NoticeOfViolationBB.onObjectViewButtonChange: " + nov.getNoticeID());
        }
        System.out.println("NoticeOfViolationBB.onObjectViewButtonChange: " + nov);

    }

    
    public void markNoticeOfViolationAsSentInit(NoticeOfViolation nov){
        currentNotice = nov;
        currentNotice.setFollowupEventDaysRequest(20);
        getSessionBean().setSessNotice(currentNotice);
        System.out.println("NoticeOfViolationBB.markNoticeOfViolationAsSentInit: NOV " + currentNotice.getNoticeID());
    }
    
    
    public String markNoticeOfViolationAsSent(ActionEvent ev) {
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

        if(nov_createNoticeFollowupEvent){
            nov_createFollowupEvent(); 
        }
        return "ceCaseProfile";
        
    }
    
    
    public void onNOVFollowupEventCreateButtonPush(ActionEvent ev){
        nov_createFollowupEvent();
    }
    
    
    public void nov_createFollowupEvent(){
        CaseCoordinator cc = getCaseCoordinator();
        currentCase = getSessionBean().getSessCECase();
        currentNotice = getSessionBean().getSessNotice();
        try {
            EventCnF even = cc.nov_createFollowupEvent(currentCase, currentNotice, getSessionBean().getSessUser());
            if(even != null){
                
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Successfully created follow-up event ID " + even.getEventID() + " on " + DateTimeUtil.getPrettyDate(even.getTimeStart()), ""));
            }
        } catch (BObStatusException | EventException | IntegrationException ex) {
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to generate follow-up event due to an object status, event, or integration error", ""));
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
        getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_INFO,
                       "Building a new notice; Next: Apply template", ""));
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
            System.out.println("NoticeOfViolationBB.prepareRecipientPersonList | property person link size: " + getSessionBean().getSessProperty().getHumanLinkList().size());
            recipientPersonCandidateList = pc.getPersonListFromHumanLinkList(pc.assembleLinkedHumanLinks(getSessionBean().getSessProperty()));
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


    public void deleteSelectedEvent() {

    }
    
    public void onBlockManageInitButtonChange(ActionEvent ev){
        // nothing to do!
        
    }

    /**
     * Listener for changes to text block template list selected items
     * @param tb
     */
    public void onTemplateBlockViewChange(TextBlock tb){
        currentTemplateBlock = tb;
    }
    
    /**
     * Listener for user requests to delete text block
     * @param tb 
     */
    public void onTemplateDeleteButtonChange(TextBlock tb){
        System.out.println("NOVBB.deleteTemplate");
        CaseIntegrator ci = getCaseIntegrator();
        try {
            ci.deleteTextBlock(tb);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Removed template!" + tb.getBlockID(), ""));
             injectableBlockList = ci.getTextBlockTemplates(getSessionBean().getSessMuni());
        } catch (IntegrationException ex) {
            
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to delete template", ""));
        }
    }
    
    /**
     * Listener for user reuqests to manage templates
     * @param ev 
     */
    public void onTemplateManageInitButtonChange(ActionEvent ev){
        CaseCoordinator cc = getCaseCoordinator();
        // only load the a block automatically if not selected
    }
    
    /**
     * Listener for user requests to make a new template block
     * @param ev 
     */
    public void onTemplateCreateButton(ActionEvent ev){
        CaseCoordinator cc = getCaseCoordinator();
        CaseIntegrator ci = getCaseIntegrator();
        currentTemplateBlock = cc.nov_getTemplateBlockSekeleton(getSessionBean().getSessMuni());
      
        getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "New Template block created", ""));
    }
    
    /**
     * Listener for user requests to add a new template block
     * @param ev 
     */
    public void onTemplateInsertButtonChange(ActionEvent ev){
        CaseCoordinator cc = getCaseCoordinator();
        CaseIntegrator ci = getCaseIntegrator();
        try {
            if(currentTemplateBlock != null){
                ci.insertTextBlock(currentTemplateBlock);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Inserted template !", ""));
            } else {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "No block to insert!", ""));
            }
            injectableBlockList = ci.getTextBlockTemplates(getSessionBean().getSessMuni());
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not insert new text block", ""));
        }
    }
    
    /**
     * Listener for user requests to update a template block
     * @param ev 
     */
    public void onTemplateUpdateButtonChange(ActionEvent ev){
        CaseIntegrator ci = getCaseIntegrator();
        try {
            ci.updateTextBlock(currentTemplateBlock);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully updated template", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not insert new text block", ""));
        }
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
      * Special getter for person links--I check the session beean's
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
     * @return the currentMode
     */
    public PageModeEnum getCurrentMode() {
        return currentMode;
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
    public List<String> getBlockCatList() {
        return blockCatList;
    }

    /**
     * @param blockCatList the blockCatList to set
     */
    public void setBlockCatList(List<String> blockCatList) {
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
     * @return the formMuni
     */
    public Municipality getFormMuni() {
        return formMuni;
    }

    /**
     * @return the formBlockName
     */
    public String getFormBlockName() {
        return formBlockName;
    }

    /**
     * @return the formBlockText
     */
    public String getFormBlockText() {
        return formBlockText;
    }

    /**
     * @return the formCategoryID
     */
    public int getFormCategoryID() {
        return formCategoryID;
    }

    /**
     * @return the formBlockOrder
     */
    public int getFormBlockOrder() {
        return formBlockOrder;
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
     * @param categoryList the categoryList to set
     */
    public void setCategoryList(HashMap<String, Integer> categoryList) {
        this.categoryList = categoryList;
    }

    /**
     * @param formMuni the formMuni to set
     */
    public void setFormMuni(Municipality formMuni) {
        this.formMuni = formMuni;
    }

    /**
     * @param formBlockName the formBlockName to set
     */
    public void setFormBlockName(String formBlockName) {
        this.formBlockName = formBlockName;
    }

    /**
     * @param formBlockText the formBlockText to set
     */
    public void setFormBlockText(String formBlockText) {
        this.formBlockText = formBlockText;
    }

    /**
     * @param formCategoryID the formCategoryID to set
     */
    public void setFormCategoryID(int formCategoryID) {
        this.formCategoryID = formCategoryID;
    }

    /**
     * @param formBlockOrder the formBlockOrder to set
     */
    public void setFormBlockOrder(int formBlockOrder) {
        this.formBlockOrder = formBlockOrder;
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
     * @param draftNoticeLoaded the draftNoticeLoaded to set
     */
    public void setDraftNoticeLoaded(boolean draftNoticeLoaded) {
        this.draftNoticeLoaded = draftNoticeLoaded;
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
     * @return the notifyingOfficerCandidateList
     */
    public List<User> getNotifyingOfficerCandidateList() {
        return notifyingOfficerCandidateList;
    }

    /**
     * @param notifyingOfficerCandidateList the notifyingOfficerCandidateList to set
     */
    public void setNotifyingOfficerCandidateList(List<User> notifyingOfficerCandidateList) {
        this.notifyingOfficerCandidateList = notifyingOfficerCandidateList;
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

}
