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
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

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
    
    

    private List<EnforcableCodeElement> codeElementListFiltered;
    private List<EnforcableCodeElement> codeElementListSelected;

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

        CodeCoordinator cc = getCodeCoordinator();
        try {
            refreshCurrentChecklistTemplateAndList();
            codeSourceList = cc.getCodeSourceList();
            initSpaceTypeLists();
            currentCodeSet = getSessionBean().getSessCodeSet();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }

    }
    
    /**
     * Sets member with list of all space types--muni agnostic
     */
    private void initSpaceTypeLists() throws IntegrationException{
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
        currentChecklistTemplate = oct;

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
            if (editModeCurrentChecklistTemplate) {
                if (currentChecklistTemplate == null) {
                    throw new BObStatusException("Cannot edit a null current checklist template");
                }
                if(currentChecklistTemplate.getInspectionChecklistID() == 0){
                    oic.insertChecklistTemplateMetadata(currentChecklistTemplate);
                } else {
                    oic.updateChecklistTemplateMetadata(currentChecklistTemplate);
                }

            } else {
                getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_INFO, 
                               "You are now editing checklist ID " + currentChecklistTemplate.getInspectionChecklistID(), ""));

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
    public void refreshCurrentChecklistTemplateAndList(){
        OccInspectionCoordinator osi = getOccInspectionCoordinator();
        try {
            checklistTemplateList = osi.getOccChecklistTemplateList(getSessionBean().getSessMuni());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        if(currentChecklistTemplate != null && checklistTemplateList != null && !checklistTemplateList.isEmpty()){
            // replace our currentChecklistTemplate with a fresh one from the complete list we just got
            currentChecklistTemplate = checklistTemplateList.get(checklistTemplateList.indexOf(currentChecklistTemplate));
        }
    }
    
    /**
     * Listener for user requests to deactivate a checklist
     * @param ev 
     */
    public void onDeactivateChecklistLinkClick(ActionEvent ev){
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        if(currentChecklistTemplate != null){
            try {
                oic.deactivateChecklistTemplate(getSessionBean().getSessUser(), currentChecklistTemplate);
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
                } else {
                    oic.updateSpaceType(currentOccSpaceType);
                }

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
     * Listener for user requests to add a space type to a checklist
     * @param ev 
     */
    public void onSpaceTypeLinkInitButtonChange(ActionEvent ev){
        // Take out the space types already linked
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        List<OccSpaceType> tempTypeList = new ArrayList<>();
        if(spaceTypeList != null && currentChecklistTemplate != null && !currentChecklistTemplate.getOccSpaceTypeList().isEmpty()){
            List<OccSpaceType> typesInChecklist = oic.downcastOccSpaceTypeChecklistified(currentChecklistTemplate.getOccSpaceTypeList());
            // Don't display space types that are already in the template
            for(OccSpaceType ost: spaceTypeList){
                if(!typesInChecklist.contains(ost)){
                    tempTypeList.add(ost);
                }
            }
        }
        spaceTypeList = tempTypeList;
        
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
     * Boolean swap tool for making required objects optional and optional ones required
     * @param oschk 
     */
    public void onToggleRequiredOccSpaceTypeChecklistified(OccSpaceTypeChecklistified oschk){
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        try {
            // flip!
            oschk.setRequired(!oschk.isRequired());
            oic.updateSpaceTypeChecklistified(oschk);
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
        } 
    }
    
    /**
     * Boolean swap tool for making required objects optional and optional ones required
     * @param ose
     */
    public void onToggleRequiredOccSpaceElement(OccSpaceElement ose){
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        try {
            oic.toggleRequiredAndUpdateOccSpaceElement(ose);
            getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_INFO, "Toggled Required on OccSpaceElement ID " + ose.getOccChecklistSpaceTypeElementID() + " to " + ose.isRequiredForInspection(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
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
     * @return the codeElementListFiltered
     */
    public List<EnforcableCodeElement> getCodeElementListFiltered() {
        return codeElementListFiltered;
    }

    /**
     * @return the codeElementListSelected
     */
    public List<EnforcableCodeElement> getCodeElementListSelected() {
        return codeElementListSelected;
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
     * @param codeElementListFiltered the codeElementListFiltered to set
     */
    public void setCodeElementListFiltered(List<EnforcableCodeElement> codeElementListFiltered) {
        this.codeElementListFiltered = codeElementListFiltered;
    }

    /**
     * @param codeElementListSelected the codeElementListSelected to set
     */
    public void setCodeElementListSelected(List<EnforcableCodeElement> codeElementListSelected) {
        this.codeElementListSelected = codeElementListSelected;
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

}
