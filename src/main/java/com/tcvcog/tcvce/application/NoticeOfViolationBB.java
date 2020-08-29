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

import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.CodeViolationDisplayable;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.NoticeOfViolation;
import com.tcvcog.tcvce.entities.PageModeEnum;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PropertyDataHeavy;
import com.tcvcog.tcvce.entities.TextBlock;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveListsEnum;
import java.io.Serializable;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    private NoticeOfViolation currentNotice;
    private List<CodeViolation> activeVList;
    private CodeViolation currentViolation;

    private Person noticePerson;

    private String formNoteText;

    private List<TextBlock> blockList;

    private List<Person> personCandidateList;
    private List<Person> manualRetrievedPersonList;

    private List<TextBlock> blockListBeforeViolations;
    private List<TextBlock> blockListAfterViolations;

    private boolean personLookupUseID;
    private Person retrievedManualLookupPerson;
    private int recipientPersonID;

    private boolean showTextBlocksAllMuni;

    private List<ViewOptionsActiveListsEnum> viewOptionList;
    private ViewOptionsActiveListsEnum selectedViewOption;

    /**
     * Creates a new instance of NoticeOfViolationBB
     */
    public NoticeOfViolationBB() {

    }

    @PostConstruct
    public void initBean() {
        CaseCoordinator cc = getCaseCoordinator();
        currentNotice = getSessionBean().getSessNotice();
        try {
            currentCase = cc.cecase_assembleCECaseDataHeavy(getSessionBean().getSessCECase(), getSessionBean().getSessUser());
        } catch (BObStatusException | IntegrationException | SearchException ex) {
            System.out.println(ex);
        }

        blockListBeforeViolations = new ArrayList<>();
        blockListAfterViolations = new ArrayList<>();
        PropertyDataHeavy pdh = getSessionBean().getSessProperty();

        personCandidateList = pdh.getPersonList();
        if (personCandidateList != null) {
            System.out.println("NoticeOfViolationBuilderBB.initbean "
                    + "| person candidate list size: " + personCandidateList.size());
        }

        manualRetrievedPersonList = new ArrayList<>();
        showTextBlocksAllMuni = false;

        setPageModes(new ArrayList<PageModeEnum>());
        getPageModes().add(PageModeEnum.LOOKUP);
        getPageModes().add(PageModeEnum.INSERT);
        getPageModes().add(PageModeEnum.UPDATE);
        getPageModes().add(PageModeEnum.REMOVE);
        if (getSessionBean().getCeCaseNoticesPageModeRequest() != null) {
            setCurrentMode(getSessionBean().getCeCaseNoticesPageModeRequest());
        } else {
            setCurrentMode(PageModeEnum.LOOKUP);
        }

        viewOptionList = Arrays.asList(ViewOptionsActiveListsEnum.values());
        selectedViewOption = ViewOptionsActiveListsEnum.VIEW_ACTIVE;

    } // close initbean

    /**
     * Responds to the user clicking one of the page modes: LOOKUP, ADD, UPDATE,
     * REMOVE
     *
     * @param mode
     */
    public void setCurrentMode(PageModeEnum mode) {

        //store currentMode into tempCurMode as a temporary value, in case the currenMode equal null
        PageModeEnum tempCurMode = this.getCurrentMode();
        //reset default setting every time the Mode has been selected 
//        loadDefaultPageConfig();
        //check the currentMode == null or not
        if (mode == null) {
            this.currentMode = tempCurMode;
        } else {
            this.currentMode = mode;
        }
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

    public boolean getActiveViewMode() {
        if (PageModeEnum.LOOKUP.equals(currentMode) || PageModeEnum.VIEW.equals(currentMode)) {
            if (currentNotice != null) {
                return true;
            }
        }
        return false;
    }

    //check if current mode == Insert
    public boolean getActiveInsertUpdateMode() {
        return PageModeEnum.INSERT.equals(currentMode) || PageModeEnum.UPDATE.equals(currentMode);
    }

    //check if current mode == Insert
    public boolean getActiveInsertMode() {
        return PageModeEnum.INSERT.equals(getCurrentMode());
    }

    //check if current mode == Remove
    public boolean getActiveRemoveMode() {
        return PageModeEnum.REMOVE.equals(getCurrentMode());
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
        NoticeOfViolation nov;
        PropertyCoordinator pc = getPropertyCoordinator();
        try {
            nov = cc.nov_GetNewNOVSkeleton(currentCase, getSessionBean().getSessMuni());
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
//        if(currentCase.getViolationList() == null || currentCase.getViolationList().isEmpty()){
//                getFacesContext().addMessage(null,
//                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
//                                "This case has no violations; start by adding a code violation: Click Code Enf >> Violations ", ""));
//        } else{
//            
//            try {
//                currentNotice = cc.nov_GetNewNOVSkeleton(currentCase, getSessionBean().getSessMuni());
//                getFacesContext().addMessage(null,
//                        new FacesMessage(FacesMessage.SEVERITY_INFO,
//                                "Loaded notice skeleton.", ""));
//            } catch (AuthorizationException ex) {
//                System.out.println(ex);
//                getFacesContext().addMessage(null,
//                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
//                                ex.getMessage(), ""));
//            }
//
//        }
    }

    public void onNOVEditTextInitButtonChange(ActionEvent ev) {

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

    public void loadBlocksAllMunis() {
        blockList = null;
        System.out.println("NOVBB.loadblocksAllMunis");

    }

    public void addBlockBeforeViolations(TextBlock tb) {
        blockList.remove(tb);
        blockListBeforeViolations.add(tb);
    }

    public void removeBlockBeforeViolations(TextBlock tb) {
        blockListBeforeViolations.remove(tb);
        blockList.add(tb);

    }

    public void removeBlockAfterViolations(TextBlock tb) {
        blockListAfterViolations.remove(tb);
        blockList.add(tb);

    }

    public void addBlockAfterViolations(TextBlock tb) {
        blockList.remove(tb);
        blockListAfterViolations.add(tb);
    }

    public void removeViolationFromList(CodeViolationDisplayable viol) {
        currentNotice.getViolationList().remove(viol);
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Done: violation ID " + viol.getViolationID() + "will not be included in letter.", ""));
    }

    public void storeRecipient(Person pers) {
        System.out.println("Store Recipient: " + pers);
        if (currentNotice != null) {
            currentNotice.setRecipient(pers);
            getSessionBean().setSessNotice(currentNotice);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Notice recipient is now Person: " + pers.getLastName(), ""));
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Notice is null, sorry!", ""));

        }
    }

    public void checkNOVRecipient(ActionEvent ev) {
        PersonIntegrator pi = getPersonIntegrator();
        if (recipientPersonID != 0) {
            try {
                manualRetrievedPersonList.add(pi.getPerson(getRecipientPersonID()));
                System.out.println("NoticeOfViolationBB.checkNOVRecipient | looked up person: " + getRetrievedManualLookupPerson());
                if (getRetrievedManualLookupPerson() != null) {
                    getManualRetrievedPersonList().add(getRetrievedManualLookupPerson());
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucess! Found person " + getRetrievedManualLookupPerson().getPersonID() + "( " + getRetrievedManualLookupPerson().getLastName() + ")", ""));

                } else {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Person not found; try again!", ""));

                }
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                getRecipientPersonID() + "not mapped to a known person", "Please try again or visit our person search page."));
            }
        }
    }

    public String assembleNotice() {

        if (currentNotice != null && currentNotice.getRecipient() != null) {
            CaseCoordinator cc = getCaseCoordinator();
            int newNoticeId = 0;

            StringBuilder sb = new StringBuilder();
            Iterator<TextBlock> it = blockListBeforeViolations.iterator();
            while (it.hasNext()) {
                appendTextBlockAsPara(it.next(), sb);
            }
            currentNotice.setNoticeTextBeforeViolations(sb.toString());

            sb = new StringBuilder();
            it = blockListAfterViolations.iterator();
            while (it.hasNext()) {
                appendTextBlockAsPara(it.next(), sb);
            }

            currentNotice.setNoticeTextAfterViolations(sb.toString());

            try {
                newNoticeId = cc.nov_InsertNotice(currentNotice, currentCase, getSessionBean().getSessUser());
            } catch (IntegrationException ex) {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
                System.out.println(ex);
            }
            currentNotice.setNoticeID(newNoticeId);
            getSessionBean().setSessNotice(currentNotice);
            return "ceCaseNotices";
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "nonselected recipient or null current notice", ""));

            return "";
        }
    }

    private StringBuilder appendTextBlockAsPara(TextBlock tb, StringBuilder sb) {
        sb.append("<p>");
        sb.append(tb.getTextBlockText());
        sb.append("</p>");
        return sb;
    }

    public String saveNoticeDraft() {
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.nov_update(currentNotice);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Notice udated", ""));

        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
        }
        return "ceCaseNotices";
    } // close method

    public String resetNotice() {
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.nov_ResetMailing(currentNotice, getSessionBean().getSessUser());
//            refreshCurrentCase();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Notice mailing status has been reset", ""));
        } catch (IntegrationException | AuthorizationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
            return "";
        }
        return "ceCaseNotices";
    }

    public String printNotice() {
        getSessionBean().setSessNotice(currentNotice);
//        positionCurrentCaseAtHeadOfQueue();
        return "noticeOfViolationPrint";
    }

    public String lockNoticeAndQueueForMailing() {
        CaseCoordinator caseCoord = getCaseCoordinator();

        try {
            caseCoord.nov_LockAndQueue(currentCase, currentNotice, getSessionBean().getSessUser());
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
            return "";
        } catch (EventException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "The automatic event generation associated with this action has thrown an error. "
                            + "Please create an event manually which logs this letter being queued for mailing", ""));
            return "";

        } catch (ViolationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Unable to queue notice of violatio. "
                            + "Please create an event manually which logs this letter being queued for mailing", ""));
            return "";
        }
        return "ceCaseNotices";

    }

    public void deleteSelectedEvent() {

    }

    /**
     * Listener for user requests to bring up the choose person dialog
     *
     * @param ev
     */
    public void onChoosePersonInitButtonChange(ActionEvent ev) {

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

    public String deleteNoticeOfViolation() {
        CaseCoordinator caseCoord = getCaseCoordinator();
        try {
            caseCoord.nov_delete(currentNotice);
            currentCase = caseCoord.cecase_assembleCECaseDataHeavy(currentCase, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Notice no. " + currentNotice.getNoticeID() + " has been nuked forever", ""));
        } catch (BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to delete this notice of violation, "
                            + "probably because it has been sent already", ""));
            return "";
        } catch (IntegrationException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
            return "";

        }
        return "ceCaseNotices";

    }

    public String markNoticeOfViolationAsSent() {
        CaseCoordinator caseCoord = getCaseCoordinator();
        try {
            caseCoord.nov_markAsSent(currentCase, currentNotice, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Marked notice as sent and added event to case",
                            ""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
            return "";
        } catch (EventException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to generate case event to log phase change",
                            "Note that because this message is being displayed, the phase change"
                            + "has probably succeeded"));
            return "";
        }
        return "ceCaseNotices";

    }

    public String markNoticeOfViolationAsReturned() {
        CaseCoordinator caseCoord = getCaseCoordinator();
        try {
            caseCoord.nov_markAsReturned(currentCase, currentNotice, getSessionBean().getSessUser());
            currentCase = caseCoord.cecase_assembleCECaseDataHeavy(currentCase, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Notice no. " + currentNotice.getNoticeID()
                            + " has been marked as returned on today's date", ""));
        } catch (IntegrationException | BObStatusException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
            return "";
        }
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
     * @return the textBlockListByMuni
     */
    public List<TextBlock> getBlockList() {
        CaseIntegrator ci = getCaseIntegrator();
        Municipality m = getSessionBean().getSessMuni();
        if (blockList == null) {
            try {
                if (showTextBlocksAllMuni) {
                    blockList = ci.getAllTextBlocks();
                } else {
                    blockList = ci.getTextBlocks(m);
                }
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
        }
        return blockList;
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
    public List<Person> getPersonCandidateList() {
        return personCandidateList;
    }

    /**
     * @param personCandidateAL the personCandidateList to set
     */
    public void setPersonCandidateAL(ArrayList<Person> personCandidateAL) {
        this.personCandidateList = personCandidateAL;
    }

    /**
     * @return the blockListBeforeViolations
     */
    public List<TextBlock> getBlockListBeforeViolations() {
        return blockListBeforeViolations;
    }

    /**
     * @param blockListBeforeViolations the blockListBeforeViolations to set
     */
    public void setBlockListBeforeViolations(List<TextBlock> blockListBeforeViolations) {
        this.blockListBeforeViolations = blockListBeforeViolations;
    }

    /**
     * @return the blockListAfterViolations
     */
    public List<TextBlock> getBlockListAfterViolations() {
        return blockListAfterViolations;
    }

    /**
     * @param blockListAfterViolations the blockListAfterViolations to set
     */
    public void setBlockListAfterViolations(List<TextBlock> blockListAfterViolations) {
        this.blockListAfterViolations = blockListAfterViolations;
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
     * @return the manualRetrievedPersonList
     */
    public List<Person> getManualRetrievedPersonList() {
        return manualRetrievedPersonList;
    }

    /**
     * @param manualRetrievedPersonList the manualRetrievedPersonList to set
     */
    public void setManualRetrievedPersonList(List<Person> manualRetrievedPersonList) {
        this.manualRetrievedPersonList = manualRetrievedPersonList;
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

}
