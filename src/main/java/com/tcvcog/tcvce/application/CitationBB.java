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
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.Citation;
import com.tcvcog.tcvce.entities.CitationStatus;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.CourtEntity;
import com.tcvcog.tcvce.entities.PageModeEnum;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.CourtEntityIntegrator;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 * Backing bean to support all manner of citation-related operations
 *
 * @author ellen bascomb of apt 31y
 */
public class CitationBB extends BackingBeanUtils implements Serializable {

    private PageModeEnum currentMode;
    private List<PageModeEnum> pageModes;

    private Citation currentCitation;
    private CECaseDataHeavy currentCase;

    private List<CitationStatus> citationStatusList;
    private List<CourtEntity> courtEntityList;

    private boolean issueCitationDisabled;
    private boolean updateCitationDisabled;

    private String formNoteText;

    private List<CodeViolation> removedViolationList;
    private String citationEditEventDescription;

    /**
     * Creates a new instance of CitationBB
     */
    public CitationBB() {

    }

    @PostConstruct
    public void initBean() {
        CaseIntegrator ci = getCaseIntegrator();
        CaseCoordinator cc = getCaseCoordinator();
        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        currentCase = getSessionBean().getSessCECase();
        try {
            if(currentCase != null){
                currentCase = cc.cecase_assembleCECaseDataHeavy(currentCase, getSessionBean().getSessUser());
            }
            citationStatusList = ci.getCitationStatusList();
        } catch (IntegrationException | BObStatusException | SearchException ex) {
            System.out.println(ex);
        }

        removedViolationList = new ArrayList<>();
        try {
            courtEntityList = cei.getCourtEntityList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }

        Citation c = getSessionBean().getSessCitation();
        if (c != null) {
            currentCitation = c;
        } else {
            if (currentCase.getCitationList() != null && !currentCase.getCitationList().isEmpty()) {
                currentCitation = currentCase.getCitationList().get(0);
            }
        }

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
     * @param cit
     * @return 
     */
    public String onObjetViewButtonChange(Citation cit) {
        CaseCoordinator cc = getCaseCoordinator();

        if (cit != null) {
            getSessionBean().setSessCitation(cit);
        }
        currentCitation = cit;
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
        
        try {
            currentCitation = cc.citation_getCitationSkeleton(getSessionBean().getSessUser(), currentCase);
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
            
        }
    }

    /**
     * Listener for user requests to abort their insert/update operation
     * @return
     */
    public String onInsertUpdateAbortButtonChange() {
        getSessionBean().setSessCitation(null);
        return "ceCaseCitations";

    }

    /**
     * Listener for user requests to open new case at current property
     *
     * @return
     */
    public String onCaseOpenButtonChange() {
        getSessionBean().getNavStack().pushPage("ceCaseSearchProfile");
        return "caseAdd";

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
     * Listener for user requests to remove a citation
     *
     * @return
     */
    public String onCitationRemoveCommitButtonChange() {
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.citation_removeCitation(currentCitation);
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
            return "";
        }

        return "ceCaseCitations";

    }

    /**
     * Listener for commencement of note writing process
     *
     * @param ev
     */
    public void onNoteInitButtonChange(ActionEvent ev) {
        formNoteText = null;

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
        mbp.setExistingContent(currentCitation.getNotes());
        mbp.setNewMessageContent(getFormNoteText());
        mbp.setHeader("Citation Note");
        mbp.setUser(getSessionBean().getSessUser());

        try {

            cc.citation_updateNotes(mbp, currentCitation);
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

        return "ceCaseCitations";

    }

    public void removeViolationFromCitation(CodeViolation v) {
        currentCitation.getViolationList().remove(v);
        removedViolationList.add(v);
    }

    public void returnViolation(CodeViolation v) {
        currentCitation.getViolationList().add(v);
        removedViolationList.remove(v);
    }

    /**
     * Listener for user requests to commit citation updates
     *
     * @return
     */
    public String onUpdateCitationCommitButtonChange() {
        System.out.println("CitationBB.updateCitation");
        CaseCoordinator cc = getCaseCoordinator();

        try {
            cc.citation_updateCitation(currentCitation);
        } catch (IntegrationException | BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
            System.out.println(ex);
            return "";
        }
        return "ceCaseCitations";
    }

    /**
     * Listener for user requests to commit changes to citation status
     *
     * @return
     */
    public String onUpdateCitationStatusCommitButtonChange() {
        System.out.println("CitationBB.updateCitationStatus");
        CaseCoordinator cc = getCaseCoordinator();

        try {
            cc.citation_updateCitation(currentCitation);
        } catch (IntegrationException | BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
            System.out.println(ex);
            return "";
        }
        return "ceCaseCitations";
    }

    /**
     * Listener for user requests to issue a citation
     *
     * @return
     */
    public String insertCitation() {
        System.out.println("CitationBB.IssueCitation");
        CaseCoordinator cc = getCaseCoordinator();

        Citation c = currentCitation;
        c.setUserOwner(getSessionBean().getSessUser());
        try {
            cc.citation_issueCitation(c);

            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "New citation added to database!", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to issue citation due to a database integration error", ""));
            System.out.println(ex);
            return "";
        }
        return "ceCaseCitations";
    }

    /**
     * @return the currentCitation
     */
    public Citation getCurrentCitation() {
        return currentCitation;
    }

    /**
     * @return the removedViolationList
     */
    public List<CodeViolation> getRemovedViolationList() {

        return removedViolationList;
    }

    /**
     * @param currentCitation the currentCitation to set
     */
    public void setCurrentCitation(Citation currentCitation) {
        this.currentCitation = currentCitation;
    }

    /**
     * @param violationList the removedViolationList to set
     */
    public void setViolationList(ArrayList<CodeViolation> violationList) {
        this.removedViolationList = violationList;
    }

    /**
     * @return the CitationStatusList
     */
    public List<CitationStatus> getCitationStatusList() {

        return citationStatusList;
    }

    /**
     * @param citationStatusList
     */
    public void setCitationStatusList(List<CitationStatus> citationStatusList) {
        this.citationStatusList = citationStatusList;
    }

    /**
     * @return the courtEntityList
     */
    public List<CourtEntity> getCourtEntityList() {
        return courtEntityList;
    }

    /**
     * @param courtEntityList the courtEntityList to set
     */
    public void setCourtEntityList(ArrayList<CourtEntity> courtEntityList) {
        this.courtEntityList = courtEntityList;
    }

    /**
     * @return the issueCitationDisabled
     */
    public boolean isIssueCitationDisabled() {
        issueCitationDisabled = currentCitation.getCitationNo() != null;
        return issueCitationDisabled;
    }

    /**
     * @return the updateCitationDisabled
     */
    public boolean isUpdateCitationDisabled() {
        updateCitationDisabled = currentCitation.getCitationNo() == null;
        return updateCitationDisabled;
    }

    /**
     * @param issueCitationDisabled the issueCitationDisabled to set
     */
    public void setIssueCitationDisabled(boolean issueCitationDisabled) {
        this.issueCitationDisabled = issueCitationDisabled;
    }

    /**
     * @param updateCitationDisabled the updateCitationDisabled to set
     */
    public void setUpdateCitationDisabled(boolean updateCitationDisabled) {
        this.updateCitationDisabled = updateCitationDisabled;
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
     * @return the citationEditEventDescription
     */
    public String getCitationEditEventDescription() {
        return citationEditEventDescription;
    }

    /**
     * @param citationEditEventDescription the citationEditEventDescription to
     * set
     */
    public void setCitationEditEventDescription(String citationEditEventDescription) {
        this.citationEditEventDescription = citationEditEventDescription;
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

}
