package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.application.SessionBean;
import com.tcvcog.tcvce.coordinators.OccInspectionCoordinator;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.InspectionException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.occupancy.*;
import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 * The premier backing bean for occupancy inspections workflow.
 *
 * @author jurplel (& ellen bascomb starting Jan 2022)
 */
public class OccInspectionsBB extends BackingBeanUtils implements Serializable {

    private List<OccChecklistTemplate> checklistTemplateList;
    private List<User> userList;

    // Form items--these need to be organized
    private OccChecklistTemplate selectedChecklistTemplate;
    private User selectedInspector;

    private OccLocationDescriptor selectedLocDescriptor;

    private OccLocationDescriptor skeletonLocationDescriptor;

    private OccInspection selectedInspection;
    private OccSpaceTypeChecklistified selectedSpaceType;

    private OccInspectedSpace selectedInspectedSpace;
    private OccInspectedSpaceElement selectedSpaceElement;
    
    private OccInspectionStatusEnum selectedElementStatusForBatch;
    private boolean useDefaultFindingsOnCurrentOISE;

    private boolean editMode;

    @PostConstruct
    public void initBean() {
        
        
        // Initialize list of checklist templates
       initChecklistTemplates();
        initUserList();
        
    }

    /**
     * Gets the list of possible checklist template objects and sets the member
     * variable checklistTemplates to its value.
     */
    public void initChecklistTemplates() {
        SessionBean sb = getSessionBean();
        OccInspectionCoordinator oc = getOccInspectionCoordinator();
        try {
            checklistTemplateList = oc.getOccChecklistTemplateList(sb.getSessMuni());
        } catch (IntegrationException ex) {
            System.out.println("Failed to acquire list of checklist templates:" + ex);
        }
    }

    /**
     * Gets the list of all users and sets userList to that list.
     */
    public void initUserList() {
        UserCoordinator uc = getUserCoordinator();

        try {
            // TODO: probably shouldn't pass null here...
            userList = uc.user_assembleUserListForSearch(null);
            if(userList != null){
                System.out.println("OccInspectionsBB.initUserList: size = " + userList.size());
            } else{
                System.out.println("OccInspectionsBB.initUserList: null");
            }
        } catch (BObStatusException ex) {
            System.out.println(ex);
        }
    }

    /**
     * Creates empty inspection object for the current occupancy period.
     */
    public void createInspection() {
        if (selectedChecklistTemplate == null) {
            System.out.println("Can't initialize new OccInspection: selected checklist template is null");
            return;
        } else if (selectedInspector == null) {
            System.out.println("Can't initialize new OccInspection: selected inspector is null");
            return;
        }

        OccPeriodDataHeavy occPeriod = getSessionBean().getSessOccPeriod();

        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        try {
            OccInspection newInspection = oic.inspectionAction_commenceOccupancyInspection(
                    null, // inspection
                    selectedChecklistTemplate, 
                    occPeriod, 
                    selectedInspector);

            if (occPeriod.getInspectionList() == null) {
                occPeriod.setInspectionList(new ArrayList());
            }
            occPeriod.getInspectionList().add(newInspection);
            getSessionBean().setSessOccPeriod(occPeriod);
        } catch (InspectionException | IntegrationException ex) {
            System.out.println("Failed to create new OccInspection: " + ex);
        }
    }

    /**
     * Asks coordinator for LocationDescriptor object
     */
    public void initSkeletonLocDescriptor() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        skeletonLocationDescriptor = oc.getOccLocationDescriptorSkeleton();
    }

    /**
     * Listener to user confirmation that the they want to add 
     * their new location descriptor
     */
    public void createLocDescriptor() {
        if (skeletonLocationDescriptor == null) {
            System.out.println("Can't create new loc descriptor: skeleton location descriptor object is null");
            return;
        }

        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            // store and retrieve this location descriptors
            selectedLocDescriptor= oc.getOccLocationDescriptor(oc.addNewLocationDescriptor(skeletonLocationDescriptor));
            System.out.println("OccInspectionBB.createLocationDescriptor | ID: " + selectedLocDescriptor.getLocationID());
        } catch (IntegrationException ex) {
            System.out.println("Failed to add skeleton location descriptor: " + ex);
        }
    }
    
    /**
     * Listener for the user to be finished selecting a checklist
     * Sets the selected inspector to the current occ period's manager
     * @param ev 
     */
    public void onChecklistSelectionCompleteButtonActuation(ActionEvent ev){
        selectedInspector = getSessionBean().getSessOccPeriod().getManager();
        
    }
    
    /**
     * Listener for user requests to view a space for inspection.
     * @param ois 
     * @param oi the inspection
     */
    public void onViewInspectedSpaceLinkClick(OccInspectedSpace ois, OccInspection oi){
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        selectedInspectedSpace = oic.configureElementDisplay(ois);
        selectedInspection = oi;
        System.out.println("OccInspectionsBB.onViewInspectedSpaceLinkClick | ois: " + ois.getInspectedSpaceID());
        
    }

    /**
     * Listener for user requests to add a chosen OccSpace to the current inspection
     * 
     */
    public void addSelectedSpaceToSelectedInspection() {
        if (selectedInspection == null) {
            System.out.println("Can't initialize add space to inspection: selected inspection object is null");
            return;
        }
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        try {
//             Maybe its important that i'm not passing a user or OccInspectionStatusEnum but i think its fine.
            selectedInspection = oic.inspectionAction_commenceInspectionOfSpaceTypeChecklistified(
                                                selectedInspection, 
                                                selectedInspection.getInspector(), 
                                                selectedSpaceType, 
                                                OccInspectionStatusEnum.NOTINSPECTED, 
                                                selectedLocDescriptor);
            getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_INFO,
                         "Added space to inspection", ""));

        } catch (IntegrationException ex) {
            System.out.println("Failed to add selected space to skeleton inspection object: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Cannot add space to inspection.", ""));
        }
    }

    /**
     * Extracts all values of th enum OccINspectionStatusEnum
     * @return 
     */
    public OccInspectionStatusEnum[] getStatuses() {
        return OccInspectionStatusEnum.values();
    }

    /**
     * Clears all parameters that might be selected during an inspection flow
     * so that one may start completely fresh. May be called, for example, by a button to start a flow.
     * ECD: We shouldn;t be making objects here!!!!
     */
    public void clearSelections() {
        setSelectedChecklistTemplate(new OccChecklistTemplate());
        setSelectedInspector(new User());

        setSelectedLocDescriptor(new OccLocationDescriptor());
    }
    
    /**
     * Listener for user requests to bring up the space type selection
     * dialog
     * @param oi
     * @param ev 
     */
    public void onAddSpaceLinkClick(OccInspection oi){
//        clearSelections();
        selectedInspection = oi;
    }
    
    /**
     * Listener for user clicks of the not inspected/pass/fail button
     * 
     * @param oise the element in the inspection accordion
     */
    public void onElementInspectionStatusButtonChange(OccInspectedSpaceElement oise){
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        System.out.println("OccInspectionBB.onElementInspectionStatusButtonChange | oise: " + oise.getInspectedSpaceElementID() + " status: " + oise.getStatusEnum().getLabel());
        try {
            oic.inspectionAction_recordElementInspectionByStatusEnum(oise, getSessionBean().getSessUser(), selectedInspection, useDefaultFindingsOnCurrentOISE);
            refreshCurrentInspectionAndRestoreSelectedSpace();
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Recorded status of element ID: " + oise.getInspectedSpaceElementID(), ""));
        } catch (AuthorizationException | BObStatusException | IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.toString(), ""));
          
        } 
        
    }
    
    /**
     * Listener for user requests to apply a status to all elements in the current
     * spacetype
     * @param ev 
     */
    public void onBatchProcessElementLinkClick(ActionEvent ev){
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        if(selectedElementStatusForBatch != null){
            try {
                oic.inspectionAction_batchConfigureInspectedSpace(  selectedInspectedSpace, 
                                                                    selectedElementStatusForBatch, 
                                                                    getSessionBean().getSessUser(), 
                                                                    selectedInspection, 
                                                                    useDefaultFindingsOnCurrentOISE);
                refreshCurrentInspectionAndRestoreSelectedSpace();
                getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_INFO,
                               "Success! Applied status " 
                                       + selectedElementStatusForBatch.getLabel() 
                                       + " To all ordinances in this space!", ""));
            } catch (BObStatusException | IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                               ex.toString(), ""));
            } 
        }
        
        
    }
    
    /**
     * Listener for user requests to save updates to their current space
     * @param ev
     */
    public void onSavCurrentSpaceChanges(ActionEvent ev){
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        try {
            oic.inspectionAction_updateSpaceElementData(selectedInspectedSpace);
            getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_INFO,
                           "Saved changes to space: " + selectedInspectedSpace.getType().getSpaceTypeTitle() + " id " + selectedInspectedSpace.getInspectedSpaceID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_ERROR,
                           ex.getMessage(), ""));
            
        }
    }
    
    /**
     * Asks coordinator for new inspection and finds the current space type in the
     * returned freshly updated inspection object and makes its version of the
     * current space the current one
     */
    private void refreshCurrentInspectionAndRestoreSelectedSpace() throws IntegrationException, BObStatusException{
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        selectedInspection = oic.getOccInspection(selectedInspection.getInspectionID());
        for(OccInspectedSpace ois: selectedInspection.getInspectedSpaceList()){
            if(ois.getInspectedSpaceID() == selectedInspectedSpace.getInspectedSpaceID()){
                selectedInspectedSpace = oic.configureElementDisplay(ois);
                System.out.println("OccInspectionsBB.refreshCurrentInspectionAndRestoreSelectedSpace | oisid " + ois.getInspectedSpaceID());
                break;
            }
        }
    }
    
    /**
     * Listener for user requests to edit a space (from the inspection dialog)
     * @param ev 
     */
    public void onSpaceTypeEditLinkClick(ActionEvent ev){
        System.out.println("OccInspectionBB.onSpaceTypeEditLinkClick");
        
    }
    
    
    /**
     * Listener for user requests to view a specific space element in a dialog
     * @param oise 
     */
    public void onSpaceElementViewLinkClick(OccInspectedSpaceElement oise){
        selectedSpaceElement = oise;
    }
    
    /**
     * Listener for user requests to delete a space
     * @param ev 
     */
    public void onSpaceTypeRemoveLinkClick(ActionEvent ev){
        System.out.println("OccInspectionBB.onSpaceTypeRemoveLinkClick");
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        try {
            oic.inspectionAction_removeSpaceFromInspection(selectedInspectedSpace, getSessionBean().getSessUser(), selectedInspection);
              getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_INFO,
                               "Success: The selected space has been removed from this inspection.", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
              getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                               "Failure: Could not remove this inspected space from the inspection, sorry.i", ""));
        }
        
    }

    // getters & setters below you know the drill

    public List<OccChecklistTemplate> getChecklistTemplateList() {
        return checklistTemplateList;
    }

    public List<User> getUserList() {
        return userList;
    }

    public OccChecklistTemplate getSelectedChecklistTemplate() {
        return selectedChecklistTemplate;
    }

    public void setSelectedChecklistTemplate(OccChecklistTemplate selectedChecklistTemplate) {
        this.selectedChecklistTemplate = selectedChecklistTemplate;
    }

    public User getSelectedInspector() {
        return selectedInspector;
    }

    public void setSelectedInspector(User selectedInspector) {
        this.selectedInspector = selectedInspector;
    }

    public OccLocationDescriptor getSelectedLocDescriptor() {
        return selectedLocDescriptor;
    }

    public void setSelectedLocDescriptor(OccLocationDescriptor selectedLocDescriptor) {
        this.selectedLocDescriptor = selectedLocDescriptor;
    }

    public OccLocationDescriptor getSkeletonLocationDescriptor() {
        return skeletonLocationDescriptor;
    }

    public OccInspection getSelectedInspection() {
        return selectedInspection;
    }

    public void setSelectedInspection(OccInspection selectedInspection) {
        this.selectedInspection = selectedInspection;
    }

    public OccInspectedSpace getSelectedInspectedSpace() {
        return selectedInspectedSpace;
    }

    public void setSelectedInspectedSpace(OccInspectedSpace selectedInspectedSpace) {
        this.selectedInspectedSpace = selectedInspectedSpace;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public void toggleEditMode() {
        editMode = !editMode;
    }

    /**
     * @return the selectedSpaceType
     */
    public OccSpaceTypeChecklistified getSelectedSpaceType() {
        return selectedSpaceType;
    }

    /**
     * @param selectedSpaceType the selectedSpaceType to set
     */
    public void setSelectedSpaceType(OccSpaceTypeChecklistified selectedSpaceType) {
        this.selectedSpaceType = selectedSpaceType;
    }

    /**
     * @return the selectedElementStatusForBatch
     */
    public OccInspectionStatusEnum getSelectedElementStatusForBatch() {
        return selectedElementStatusForBatch;
    }

    /**
     * @param selectedElementStatusForBatch the selectedElementStatusForBatch to set
     */
    public void setSelectedElementStatusForBatch(OccInspectionStatusEnum selectedElementStatusForBatch) {
        this.selectedElementStatusForBatch = selectedElementStatusForBatch;
    }

    /**
     * @return the selectedSpaceElement
     */
    public OccInspectedSpaceElement getSelectedSpaceElement() {
        return selectedSpaceElement;
    }

    /**
     * @param selectedSpaceElement the selectedSpaceElement to set
     */
    public void setSelectedSpaceElement(OccInspectedSpaceElement selectedSpaceElement) {
        this.selectedSpaceElement = selectedSpaceElement;
    }

    /**
     * @return the useDefaultFindingsOnCurrentOISE
     */
    public boolean isUseDefaultFindingsOnCurrentOISE() {
        return useDefaultFindingsOnCurrentOISE;
    }

    /**
     * @param useDefaultFindingsOnCurrentOISE the useDefaultFindingsOnCurrentOISE to set
     */
    public void setUseDefaultFindingsOnCurrentOISE(boolean useDefaultFindingsOnCurrentOISE) {
        this.useDefaultFindingsOnCurrentOISE = useDefaultFindingsOnCurrentOISE;
    }
}
