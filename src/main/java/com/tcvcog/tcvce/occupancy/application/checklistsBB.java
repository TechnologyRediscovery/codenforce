/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.CodeCoordinator;
import com.tcvcog.tcvce.coordinators.OccInspectionCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CodeSet;
import com.tcvcog.tcvce.entities.CodeSource;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.occupancy.OccChecklistTemplate;
import com.tcvcog.tcvce.entities.occupancy.OccSpaceElement;
import com.tcvcog.tcvce.entities.occupancy.OccSpaceType;
import com.tcvcog.tcvce.entities.occupancy.OccSpaceTypeChecklistified;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.event.ActionEvent;

/**
 * Backing bean that supports the creation and management of field inspection
 * checklists
 *
 * @author Ellen Bascomb of Apartment 31Y
 */
public class checklistsBB extends BackingBeanUtils {

    private List<OccChecklistTemplate> checklistTemplateList;
    private OccChecklistTemplate currentChecklistTemplate;
    private boolean editModeCurrentChecklistTemplate;
    private List<CodeSource> codeSourceList;
    
    private List<OccSpaceType> spaceTypeList;
    private List<OccSpaceType> spaceTypeListSelected;
    private boolean spaceTypeBatchRequired;
    private OccSpaceType currentOccSpaceType;
    private boolean editModeCurrentOccSpaceType;

    private OccSpaceTypeChecklistified currentOccSpaceTypeChecklistified;
    private boolean editModeCurrentOccSpaceTypeChecklistified;
    
    private CodeSet currentCodeSet;

    private OccSpaceElement currentOccSpaceElement;
    private List<OccSpaceElement> occSpaceElementList;
    private List<OccSpaceElement> occSpaceElementListSelected;
    private List<OccSpaceElement> occSpaceElementListFiltered;

    /**
     * Creates a new instance of checklistsBB
     */
    public checklistsBB() {
    }

    /**
     * Sets up our master lists and option drop downs
     */
    @PostConstruct
    public void initBean() {
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        CodeCoordinator cc = getCodeCoordinator();
        try {
            refreshCurrentChecklistTemplateList();
            codeSourceList = cc.getCodeSourceList();
            refreshSpaceTypeListAndClearSelected();
            currentCodeSet = getSessionBean().getSessCodeSet();
            occSpaceElementList = oic.wrapECEListInOccSpaceElementWrapper(currentCodeSet.getEnfCodeElementList());
            occSpaceElementListFiltered = new ArrayList<>();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }

    }
    
    /**
     * Sets member with list of all space types--muni agnostic
     */
    private void refreshSpaceTypeListAndClearSelected() throws IntegrationException{
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        spaceTypeList = oic.getOccSpaceTypeList();
        spaceTypeListSelected = new ArrayList<>();
        
    }

    
    /**
     * Listener to start the creation process of an OccChecklist
     * @param ev 
     */
    public void onChecklistCreateInitButtonClick(ActionEvent ev){
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        currentChecklistTemplate = oic.getOccChecklistTemplateSkeleton(getSessionBean().getSessMuni());
        editModeCurrentChecklistTemplate = false; 
        toggleEditModeChecklistTemplate();
    }
    
    /**
     * Listener for user requests to view a checklist
     *
     * @param oct
     */
    public void onChecklistViewEditLinkClick(OccChecklistTemplate oct) {
        System.out.println("ChecklistsBB.onChecklistViewEditLinkClick | ChecklsitID: " + oct.getInspectionChecklistID());
        currentChecklistTemplate = oct;
        refreshCurrentChecklistTemplate();

    }
    
    
    /**
     * Pass-through listener to receive button clicks; delegates to
     * no arg method toggleEditModeChecklistTemplate
     * @param ev 
     */
    public void onToggleEditModeChecklistTemplateButtonClick(ActionEvent ev){
        toggleEditModeChecklistTemplate();
    }

    /**
     * Listener for user clicks of the "edit" button which starts the update
     * process and its re-click as the "save edits" button which either writes
     * the new template in or updates the existing one.
     */
    public void toggleEditModeChecklistTemplate() {
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        try {
            System.out.println("InspectionsBB.toggleEditModeChecklistTemplate | start of method value: " + editModeCurrentChecklistTemplate);
            if (editModeCurrentChecklistTemplate) {
                if (currentChecklistTemplate == null) {
                    throw new BObStatusException("Cannot edit a null current checklist template");
                }
                if(currentChecklistTemplate.getInspectionChecklistID() == 0){
                    int freshid = oic.insertChecklistTemplateMetadata(currentChecklistTemplate);
                    currentChecklistTemplate = oic.getChecklistTemplate(freshid);
                    System.out.println("InspectionsBB.toggleEditModeChecklistTemplate | inserted...");
                } else {
                    oic.updateChecklistTemplateMetadata(currentChecklistTemplate);
                    System.out.println("InspectionsBB.toggleEditModeChecklistTemplate | updated...");
                }
                refreshCurrentChecklistTemplateList();
                refreshCurrentChecklistTemplate();

            } 
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
        }

        // do the toggle
        editModeCurrentChecklistTemplate = !editModeCurrentChecklistTemplate;

    }
    
    /**
     * Listener for user requests to abort the edit of a checklist
     * @param ev 
     */
    public void onEditModeChecklistTemplateAbort(ActionEvent ev){
        // turn off edit mode; don't talk to DB about anything!
        
        editModeCurrentChecklistTemplate = !editModeCurrentChecklistTemplate;
        
    }
    
    /**
     * Grabs a fresh copy of the checklist list from DB 
     * and restores the current template to the current Role
     */
    public void refreshCurrentChecklistTemplateList(){
        OccInspectionCoordinator osi = getOccInspectionCoordinator();
        try {
            checklistTemplateList = osi.getOccChecklistTemplateList(getSessionBean().getSessMuni());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
       
    }
    
    /**
     * Asks the coordinator for a fresh copy of the current checklist
     */
    public void refreshCurrentChecklistTemplate(){
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        if(currentChecklistTemplate !=  null){
            try {
                currentChecklistTemplate = oic.getChecklistTemplate(currentChecklistTemplate.getInspectionChecklistID());
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
        }
    }
    
    /**
     * Listener for user requests to start the deactivation process
     * @param oct to be deactivated in next step
     */
    public void onDeactivateChecklistInitLinkClick(ActionEvent ev){
        System.out.println("ChecklistsBB.onDeactivateChecklistInitLinkClick");
        
    }
    
    
    /**
     * Listener for user requests to deactivate a checklist
     * @param ev 
     */
    public void onDeactivateChecklistButtonPress(ActionEvent ev){
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        if(currentChecklistTemplate != null){
            try {
                oic.deactivateChecklistTemplate(getSessionBean().getSessUser(), currentChecklistTemplate);
                refreshCurrentChecklistTemplateList();
                getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_INFO, 
                               "Deactivated checklist template ID: " 
                                       + currentChecklistTemplate.getInspectionChecklistID(), ""));
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
                
            }
        }
    }
    
    /**
     * Listener for user requests to start the process 
     * to create a new space type
     * @param ev 
     */
    public void onCreateNewSpaceTypeInitButtonPush(ActionEvent ev){
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        currentOccSpaceType = oic.getOccSpaceTypeSkeleton();
        editModeCurrentOccSpaceType = true;
    }
    
    
    
      /**
     * Listener for user clicks of the "edit" button which starts the update
     * process and its re-click as the "save edits" button which either writes
     * the new template in or updates the existing one.
     */
    public void toggleEditModeOccSpaceType() {
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        try {
            if (editModeCurrentOccSpaceType) {
                if (currentOccSpaceType == null) {
                    throw new BObStatusException("Cannot edit a null currentOccSpaceType");
                }
                if(currentOccSpaceType.getSpaceTypeID() == 0){
                    oic.insertSpaceType(currentOccSpaceType);
                    getFacesContext().addMessage(null,
                           new FacesMessage(FacesMessage.SEVERITY_INFO, 
                                   "Success: Added new space type: " + currentOccSpaceType.getSpaceTypeTitle(), ""));
                } else {
                    oic.updateSpaceType(currentOccSpaceType);
                    getFacesContext().addMessage(null,
                           new FacesMessage(FacesMessage.SEVERITY_INFO, 
                                   "Success: Updated space type: " + currentOccSpaceType.getSpaceTypeTitle(), ""));
                }
                refreshSpaceTypeListAndClearSelected();
                // rebuild our spaceType list with the new values
                onSpaceTypeLinkInit();

            } else {
                getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_INFO, 
                               "You are now editing space type ID " + currentOccSpaceType.getSpaceTypeID(), ""));

            }
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
        }

        // do the toggle
        editModeCurrentOccSpaceType = !editModeCurrentOccSpaceType;

    }
    
    /**
     * The actual listener that delegated to the toggle method
     * @param ev 
     */
    public void onEditModeOccSpaceTypeToggleButtonPush(ActionEvent ev){
        toggleEditModeOccSpaceType();
        
        
    }
    
    
    /**
     * Listener for user requests to abort the edit of a checklist
     * @param ev 
     */
    public void onEditModeOccSpaceTypeAbort(ActionEvent ev){
        // turn off edit mode; don't talk to DB about anything!
        
        editModeCurrentOccSpaceType = !editModeCurrentOccSpaceType;
        
    }
    
    /**
     * Listener for user requests to start the link process
     * between a space type and the current checklist
     * @param ev 
     */
    public void onSpaceTypeLinkInitButtonChange(ActionEvent ev){
        onSpaceTypeLinkInit();
    }
    
    
    /**
     * Builds a custom spaceTypeLIst that only includes unique values
     * for the current Checklist
     */
    private void onSpaceTypeLinkInit(){
        // Take out the space types already linked
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        List<OccSpaceType> tempTypeList = new ArrayList<>();
        if(spaceTypeList == null){
            spaceTypeList = new ArrayList<>();
        }
        if(spaceTypeListSelected == null){
            spaceTypeListSelected = new ArrayList();
        }
        if(!spaceTypeListSelected.isEmpty()){
            spaceTypeListSelected.clear();
        }

        if(     currentChecklistTemplate != null 
                && currentChecklistTemplate.getOccSpaceTypeList() != null 
                && !currentChecklistTemplate.getOccSpaceTypeList().isEmpty()){
            List<OccSpaceType> typesInChecklist = oic.downcastOccSpaceTypeChecklistified(currentChecklistTemplate.getOccSpaceTypeList());
            // Don't display space types that are already in the template
            for(OccSpaceType ost: spaceTypeList){
                if(!typesInChecklist.contains(ost)){
                    tempTypeList.add(ost);
                }
            }
            spaceTypeList = tempTypeList;
        } 
    }
    /**
     * Listener for user requests to attach all their
     * selected spaces to the current checklist
     */
    public void onLinkSelectedSpaceTypesToChecklist(){
        
        if(currentChecklistTemplate != null && spaceTypeListSelected != null && !spaceTypeListSelected.isEmpty() ){
            OccInspectionCoordinator oic = getOccInspectionCoordinator();
            for(OccSpaceType ost: spaceTypeListSelected){
                try {
                    oic.insertAndLinkSpaceTypeChecklistifiedListToTemplate(currentChecklistTemplate, ost, spaceTypeBatchRequired);
                    getFacesContext().addMessage(null,
                           new FacesMessage(FacesMessage.SEVERITY_INFO, ost.getSpaceTypeTitle() + " linked succesfully!", ""));
                } catch (BObStatusException | IntegrationException ex) {
                    System.out.println(ex);
                    getFacesContext().addMessage(null,
                           new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
                } 
            }
            refreshCurrentChecklistTemplate();
        }
    }

    /**
     * Makes sure members ares setup for easy processing of the dialog's selections
     * of ordinances
     * @param ostchk 
     */
    public void onChooseOrdinancesToLinkToSpaceTypeLinkCLick(OccSpaceTypeChecklistified ostchk){
        currentOccSpaceTypeChecklistified = ostchk;
        
    }
    
    /**
     * Listener for user requests to connect their selected
     * code elements to the current SpaceType
     * 
     * @param ev 
     */
    public void onLinkSelectedECEsToSpaceType(ActionEvent ev){
        if(occSpaceElementListSelected != null && !occSpaceElementListSelected.isEmpty()){
            System.out.println("ChecklistsBB.onLinkSelectedECEsToSpaceType | selected list size " + occSpaceElementListSelected.size());
            OccInspectionCoordinator oic = getOccInspectionCoordinator();
            for(OccSpaceElement ose: occSpaceElementListSelected){
                try {
                    oic.insertAndLinkCodeElementsToSpaceType(currentOccSpaceTypeChecklistified, ose);
                     getFacesContext().addMessage(null,
                               new FacesMessage(FacesMessage.SEVERITY_INFO,"Successfully linked code element ID: " 
                                       + ose.getCodeSetElementID()  
                                       + " to checklist space type: " 
                                       + currentOccSpaceTypeChecklistified.getChecklistSpaceTypeID(), ""));
                } catch (BObStatusException | IntegrationException ex) {
                    System.out.println(ex);
                     getFacesContext().addMessage(null,
                               new FacesMessage(FacesMessage.SEVERITY_ERROR,"Error linking ordinances to space type: " + ex.getMessage(), ""));
                } 
            }
            refreshCurrentChecklistTemplate();
            occSpaceElementListSelected.clear();
        } // null inputs check
    }
    
    /**
     * Listener to view the details of an enforcable ordinance in a space type.
     * Sets up the session bean to hold the choice, and lets the template be
     * displayed
     * @param ose to view
     */
    public void onViewOrdinanceDetailsLinkClick(OccSpaceElement ose){
        System.out.println("ChecklistsBB.onViewOrdinanceDetailsLinkClick | cseID: " + ose.getCodeSetElementID());
        getSessionBean().setSessEnforcableCodeElement((EnforcableCodeElement) ose);
    }
    
    
    /**
     * Listener to start the removal process of a space type from a checklist
     * @param ostchk 
     */
    public void onRemoveSpaceTypeFromChecklistInit(OccSpaceTypeChecklistified ostchk){
        currentOccSpaceTypeChecklistified = ostchk;
    }
    
    /**
     * Listener for user requests to remove a space type from a the current checklist
     * @param ostchk
     */
    public void onRemoveSpaceTypeFromChecklist(){
        if(currentOccSpaceTypeChecklistified != null){
            
            System.out.println("ChecklistsBB.onRemoveSpaceTypeFromChecklist | OSTID: " + currentOccSpaceTypeChecklistified.getChecklistSpaceTypeID());
            OccInspectionCoordinator oic = getOccInspectionCoordinator();
            try {
                oic.detachOccSpaceTypeChecklistifiedFromTemplate(currentOccSpaceTypeChecklistified);
                refreshCurrentChecklistTemplate();
                  getFacesContext().addMessage(null,
                               new FacesMessage(FacesMessage.SEVERITY_INFO,"Successfully removed Space Type ID: " + currentOccSpaceTypeChecklistified.getSpaceTypeID(), ""));
            } catch (BObStatusException | IntegrationException ex) {
                System.out.println(ex);
                  getFacesContext().addMessage(null,
                               new FacesMessage(FacesMessage.SEVERITY_ERROR,ex.getMessage() + " | On space typeID: "+ currentOccSpaceTypeChecklistified.getSpaceTypeID(), ""));
            } 
        }
        
    }


    /**
     * Listener for user requests to start the removal process.
     * @param ose 
     */
    public void onRemoveOccSpaceElementFromSpaceTypeInit(OccSpaceElement ose){
        currentOccSpaceElement = ose;
    }

    /**
     * Listener for user requests to remove a space element from the type
     * @param ose 
     */
    public void onRemoveOccSpaceElementFromSpaceType(){
        if(currentOccSpaceElement != null){
            
            System.out.println("ChecklistsBB.onRemoveSpaceElementFromSpace | OSEID: " + currentOccSpaceElement.getOccChecklistSpaceTypeElementID());
            
            OccInspectionCoordinator oic = getOccInspectionCoordinator();
            try {
                oic.detachCodeElementFromSpaceType(currentOccSpaceElement);
                refreshCurrentChecklistTemplate();
                getFacesContext().addMessage(null,
                             new FacesMessage(FacesMessage.SEVERITY_INFO," Successfully removed occ space element ID: "+ currentOccSpaceElement.getOccChecklistSpaceTypeElementID(), ""));
            } catch (BObStatusException | IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                             new FacesMessage(FacesMessage.SEVERITY_ERROR,ex.getMessage() + " | On space element ID: "+ currentOccSpaceElement.getOccChecklistSpaceTypeElementID(), ""));
            } 
        } else {
            System.out.println("ChecklistsBB.onRemoveSpaceElementFromSpace | NULL OSE INPUT!!");
        }
    }
    
    /**
     * Listener for user requests to abort the removal process
     * @param ev 
     */
    public void onRemoveOperationAbortButtonPress(ActionEvent ev){
        System.out.println("InspectionsBB.onRemoveOperationAbortButtonPress");
    }
    
   
    
    /**
     * Boolean swap tool for making required objects optional and optional ones required
     * @param oschk 
     */
    public void onToggleRequiredOccSpaceTypeChecklistified(OccSpaceTypeChecklistified oschk){
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        System.out.println("ChecklistsBB.onToggleRequiredOccSpaceTypeChecklistified | ostchkID: " + oschk.getChecklistSpaceTypeID());
        try {
            // flip!
            oschk.setRequired(!oschk.isRequired());
            oic.updateSpaceTypeChecklistified(oschk);
            refreshCurrentChecklistTemplate();
            getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_INFO, "Toggled Required on OccSpaceType ID " + oschk.getChecklistSpaceTypeID() + " to " + oschk.isRequired(), ""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_INFO, ex.getMessage(), ""));
        } 
    }
    
    /**
     * Boolean swap tool for making required objects optional and optional ones required
     * @param ose
     */
    public void onToggleRequiredOccSpaceElement(OccSpaceElement ose){
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        if(ose != null){

            System.out.println("ChecklistsBB.onToggleRequiredOccSpaceElement | oseID: " + ose.getOccChecklistSpaceTypeElementID());
            try {
                oic.toggleRequiredAndUpdateOccSpaceElement(ose);
                refreshCurrentChecklistTemplate();
                getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_INFO, "Toggled Required on OccSpaceElement ID " + ose.getOccChecklistSpaceTypeElementID() + " to " + ose.isRequiredForInspection(), ""));
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
            }
        } else {
            getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_ERROR, "No OccSpaceElement selected", ""));
        
        }
    }
    
    
    /**
     * listener for user requests to edit the space type checklistified
     * @param ostchk 
     */
    public void onSpaceTypeChecklistifiedViewEditLinkClick(OccSpaceTypeChecklistified ostchk){
        currentOccSpaceTypeChecklistified = ostchk;
    }


    // ***************************
    // *** GETTERS AND SETTERS  **
    // ***************************
    /**
     * @return the checklistTemplateList
     */
    public List<OccChecklistTemplate> getChecklistTemplateList() {
        return checklistTemplateList;
    }

    /**
     * @return the currentChecklistTemplate
     */
    public OccChecklistTemplate getCurrentChecklistTemplate() {
        return currentChecklistTemplate;
    }

    /**
     * @return the editModeCurrentChecklistTemplate
     */
    public boolean isEditModeCurrentChecklistTemplate() {
        return editModeCurrentChecklistTemplate;
    }

    /**
     * @return the currentOccSpaceType
     */
    public OccSpaceType getCurrentOccSpaceType() {
        return currentOccSpaceType;
    }

    /**
     * @return the editModeCurrentOccSpaceType
     */
    public boolean isEditModeCurrentOccSpaceType() {
        return editModeCurrentOccSpaceType;
    }

    /**
     * @return the currentOccSpaceTypeChecklistified
     */
    public OccSpaceTypeChecklistified getCurrentOccSpaceTypeChecklistified() {
        return currentOccSpaceTypeChecklistified;
    }

    /**
     * @return the editModeCurrentOccSpaceTypeChecklistified
     */
    public boolean isEditModeCurrentOccSpaceTypeChecklistified() {
        return editModeCurrentOccSpaceTypeChecklistified;
    }

    /**
     * @return the occSpaceElementList
     */
    public List<OccSpaceElement> getOccSpaceElementList() {
        return occSpaceElementList;
    }

    /**
     * @return the occSpaceElementListSelected
     */
    public List<OccSpaceElement> getOccSpaceElementListSelected() {
        return occSpaceElementListSelected;
    }

    /**
     * @param checklistTemplateList the checklistTemplateList to set
     */
    public void setChecklistTemplateList(List<OccChecklistTemplate> checklistTemplateList) {
        this.checklistTemplateList = checklistTemplateList;
    }

    /**
     * @param currentChecklistTemplate the currentChecklistTemplate to set
     */
    public void setCurrentChecklistTemplate(OccChecklistTemplate currentChecklistTemplate) {
        this.currentChecklistTemplate = currentChecklistTemplate;
    }

    /**
     * @param editModeCurrentChecklistTemplate the
     * editModeCurrentChecklistTemplate to set
     */
    public void setEditModeCurrentChecklistTemplate(boolean editModeCurrentChecklistTemplate) {
        this.editModeCurrentChecklistTemplate = editModeCurrentChecklistTemplate;
    }

    /**
     * @param currentOccSpaceType the currentOccSpaceType to set
     */
    public void setCurrentOccSpaceType(OccSpaceType currentOccSpaceType) {
        this.currentOccSpaceType = currentOccSpaceType;
    }

    /**
     * @param editModeCurrentOccSpaceType the editModeCurrentOccSpaceType to set
     */
    public void setEditModeCurrentOccSpaceType(boolean editModeCurrentOccSpaceType) {
        this.editModeCurrentOccSpaceType = editModeCurrentOccSpaceType;
    }

    /**
     * @param currentOccSpaceTypeChecklistified the
     * currentOccSpaceTypeChecklistified to set
     */
    public void setCurrentOccSpaceTypeChecklistified(OccSpaceTypeChecklistified currentOccSpaceTypeChecklistified) {
        this.currentOccSpaceTypeChecklistified = currentOccSpaceTypeChecklistified;
    }

    /**
     * @param editModeCurrentOccSpaceTypeChecklistified the
     * editModeCurrentOccSpaceTypeChecklistified to set
     */
    public void setEditModeCurrentOccSpaceTypeChecklistified(boolean editModeCurrentOccSpaceTypeChecklistified) {
        this.editModeCurrentOccSpaceTypeChecklistified = editModeCurrentOccSpaceTypeChecklistified;
    }

    /**
     * @param occSpaceElementList the occSpaceElementList to set
     */
    public void setOccSpaceElementList(List<OccSpaceElement> occSpaceElementList) {
        this.occSpaceElementList = occSpaceElementList;
    }

    /**
     * @param occSpaceElementListSelected the occSpaceElementListSelected to set
     */
    public void setOccSpaceElementListSelected(List<OccSpaceElement> occSpaceElementListSelected) {
        this.occSpaceElementListSelected = occSpaceElementListSelected;
    }

    /**
     * @return the codeSourceList
     */
    public List<CodeSource> getCodeSourceList() {
        return codeSourceList;
    }

    /**
     * @param codeSourceList the codeSourceList to set
     */
    public void setCodeSourceList(List<CodeSource> codeSourceList) {
        this.codeSourceList = codeSourceList;
    }

    /**
     * @return the spaceTypeList
     */
    public List<OccSpaceType> getSpaceTypeList() {
        return spaceTypeList;
    }

    /**
     * @param spaceTypeList the spaceTypeList to set
     */
    public void setSpaceTypeList(List<OccSpaceType> spaceTypeList) {
        this.spaceTypeList = spaceTypeList;
    }

    /**
     * @return the spaceTypeListSelected
     */
    public List<OccSpaceType> getSpaceTypeListSelected() {
        return spaceTypeListSelected;
    }

    /**
     * @param spaceTypeListSelected the spaceTypeListSelected to set
     */
    public void setSpaceTypeListSelected(List<OccSpaceType> spaceTypeListSelected) {
        this.spaceTypeListSelected = spaceTypeListSelected;
    }

    /**
     * @return the spaceTypeBatchRequired
     */
    public boolean isSpaceTypeBatchRequired() {
        return spaceTypeBatchRequired;
    }

    /**
     * @param spaceTypeBatchRequired the spaceTypeBatchRequired to set
     */
    public void setSpaceTypeBatchRequired(boolean spaceTypeBatchRequired) {
        this.spaceTypeBatchRequired = spaceTypeBatchRequired;
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
     * @return the occSpaceElementListFiltered
     */
    public List<OccSpaceElement> getOccSpaceElementListFiltered() {
        return occSpaceElementListFiltered;
    }

    /**
     * @param occSpaceElementListFiltered the occSpaceElementListFiltered to set
     */
    public void setOccSpaceElementListFiltered(List<OccSpaceElement> occSpaceElementListFiltered) {
        this.occSpaceElementListFiltered = occSpaceElementListFiltered;
    }

    /**
     * @return the currentOccSpaceElement
     */
    public OccSpaceElement getCurrentOccSpaceElement() {
        return currentOccSpaceElement;
    }

    /**
     * @param CurrentOccSpaceElement the currentOccSpaceElement to set
     */
    public void setCurrentOccSpaceElement(OccSpaceElement CurrentOccSpaceElement) {
        this.currentOccSpaceElement = CurrentOccSpaceElement;
    }

}
