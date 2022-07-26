package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.application.SessionBean;
import com.tcvcog.tcvce.coordinators.BlobCoordinator;
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.OccInspectionCoordinator;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.InspectionException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.DomainEnum;
import com.tcvcog.tcvce.entities.IFace_inspectable;
import com.tcvcog.tcvce.entities.IntensityClass;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.occupancy.*;
import com.tcvcog.tcvce.entities.reports.ReportConfigOccInspection;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsOccChecklistItemsEnum;
import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

/**
 * The premier backing bean for field inspections workflow.
 * This applies to both occupancy periods and CE cases!!
 *
 * @author jurplel (& ellen bascomb starting Jan 2022)
 */
public class FieldInspectionBB extends BackingBeanUtils implements Serializable {

    private IFace_inspectable currentInspectable;
    private FieldInspection currentInspection;
    private OccInspectedSpace currentInspectedSpace;
    private OccInspectedSpaceElement currentInspectedSpaceElement;
    private OccSpaceTypeChecklistified currentSpaceType;
    
    private OccChecklistTemplate selectedChecklistTemplate;
    private List<OccChecklistTemplate> checklistTemplateList;
    
    private List<OccInspectionDetermination> determinationList;
    private OccInspectionDetermination selectedDetermination;
    
    private User selectedInspector;

    private List<OccInspectionCause> causeList;
    private List<IntensityClass> failSeverityList;
    
    private String inspectionListComponentForUpdate;
    // Form items--these need to be organized

    private OccLocationDescriptor currentLocationDescriptor;
    private OccLocationDescriptor skeletonLocationDescriptor;

    private OccInspectionStatusEnum selectedElementStatusForBatch;
    private boolean useDefaultFindingsOnCurrentOISE;

    private boolean editModeInspectionMetadata;
    private FieldInspection formFollowUpInspectionTo;
    
    private boolean formMigrateFailedItemsOnFinalization;
    
    
    private int occPeriodIDFortransferFormField;
    
    private ReportConfigOccInspection reportConfigFIN;
    private List<ViewOptionsOccChecklistItemsEnum> ordinanceViewOptionsList;
       

    @PostConstruct
    public void initBean() {
       OccInspectionCoordinator ois = getOccInspectionCoordinator();
       
        try {
            causeList = ois.getOccInspectionCauseList();
            determinationList = ois.getOccDeterminationList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
        ordinanceViewOptionsList = Arrays.asList(ViewOptionsOccChecklistItemsEnum.values());
        formMigrateFailedItemsOnFinalization = true;
        // Initialize list of checklist templates
       initChecklistTemplates();
       initSeverityClassList();
    }
    
    /**
     * Sets up our severity class list
     */
    private void initSeverityClassList(){
        SystemCoordinator sysCor = getSystemCoordinator();
        try{
            
            failSeverityList = sysCor.getIntensitySchemaWithClasses(
                    getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("intensityschema_violationseverity"))
                    .getClassList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }

    /**
     * Gets the list of possible checklist template objects and sets the member
     * variable checklistTemplates to its value.
     */
    private void initChecklistTemplates() {
        SessionBean sb = getSessionBean();
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        try {
            checklistTemplateList = oic.getOccChecklistTemplateList(sb.getSessMuni());
        } catch (IntegrationException ex) {
            System.out.println("Failed to acquire list of checklist templates:" + ex);
        }
    }

    /**
     * Listener for user to view an inspection from either the CECase profile
     * or occ period profile
     * @param holder
     * @param fi 
     */
    public void onViewEditInspectionLinkClick(IFace_inspectable holder, FieldInspection fi){
        currentInspectable = holder;
        currentInspection = fi;
        try {
            refreshCurrentInspectionAndRestoreSelectedSpace();
        } catch (IntegrationException | BObStatusException | BlobException ex) {
            System.out.println(ex);
              getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_ERROR,
                         "Fatal error reloading current inspection! ", ""));
        } 
        getSessionBean().setSessFieldInspection(fi);
        extractComponentForReloadFromRequest();
        
    }
    
    
    /**
     * Called when a user is attempting to start a new inspection on either
     * an occ period or ce case
     * @param inspectable 
     */
    public void onCreateInspectionInitButtonChange(IFace_inspectable inspectable){
        currentInspectable = inspectable;
        extractComponentForReloadFromRequest();
     
    }
    
    /**
     * Extracts the component to update after
     * creation or edit of an inspection by the UI
     * 
     */
    private void extractComponentForReloadFromRequest(){
        inspectionListComponentForUpdate = 
                FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getRequestParameterMap()
                        .get("initiating-inspection-list-component-id");
        System.out.println("FieldInspectionBB.extractComponentForReloadFromRequest | Component = " + inspectionListComponentForUpdate);
    }

    /**
     * Creates empty inspection object for the current occupancy period or cecase
     
     */
    public void createInspection() {
        
        if (selectedChecklistTemplate == null) {
            System.out.println("Can't initialize new OccInspection: selected checklist template is null");
            return;
        } else if (selectedInspector == null) {
            System.out.println("Can't initialize new OccInspection: selected inspector is null");
            return;
        }
        
        System.out.println("OccInspectionBB.createInspection | passed null check");

        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        try {
            currentInspection = oic.inspectionAction_commenceOccupancyInspection(
                    null, // inspection -- it'll make us a new one in the coordinator
                    selectedChecklistTemplate, 
                    currentInspectable, 
                    selectedInspector);
             getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_INFO,
                         "Created new inspection!: " + currentInspection.getInspectionID(), ""));

            refreshInspectionListAndTriggerManagedListReload();
        } catch (InspectionException | IntegrationException | BObStatusException | BlobException ex) {
            System.out.println("Failed to create new OccInspection: " + ex);
        }
    }

    /**
     * Asks the coordinator for a nice new list of field inspections
     * and injects this into the special session spot which
     * managed refresh components will check and inject
     * for active updating.
     */
    private void refreshInspectionListAndTriggerManagedListReload(){
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        if(currentInspectable != null){
            try {
                List<FieldInspection> filist = oic.getOccInspectionList(currentInspectable);
                System.out.println("FieldInspectionBB.refreshInspectionListAndTriggerManagedListReload | filist size: " + filist.size());
                System.out.println("FieldInspectionBB.refreshInspectionListAndTriggerManagedListReload | component for reload: " + inspectionListComponentForUpdate);
                getSessionBean().setSessFieldInspectionListForRefresh(filist);
            } catch (IntegrationException | BObStatusException | BlobException ex) {
                System.out.println(ex);
            } 
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
            currentLocationDescriptor= oc.getOccLocationDescriptor(oc.addNewLocationDescriptor(skeletonLocationDescriptor));
            System.out.println("OccInspectionBB.createLocationDescriptor | ID: " + currentLocationDescriptor.getLocationID());
        } catch (IntegrationException ex) {
            System.out.println("Failed to add skeleton location descriptor: " + ex);
        }
    }
    
    /**
     * Listener for the user to be finished selecting a checklist
     * Sets the selected inspector to the current occ period's manager
     * @param ev 
     */
    public void onChecklistSelectionCompleteButtonClick(ActionEvent ev){
        selectInspectorUsingCurrentInspectionHolder();
        System.out.println("OccInspectionBB.onChecklistSelectionCompleteButtonClick");
        createInspection();
        // tell ui to start the meata data form in edit mode
        editModeInspectionMetadata = true;
    }
    
    /**
     * If the current inspectable is an occ period, make the new
     * inspection's inspector by default that period's manager. 
     * Similarly, if the current inspectable is a cecase, 
     * then make the new inspection's inspector that case's manager;
     */
    private void selectInspectorUsingCurrentInspectionHolder(){
        if(currentInspectable != null){
            if(currentInspectable instanceof OccPeriod){
                selectedInspector = getSessionBean().getSessOccPeriod().getManager();
            } else if(currentInspectable instanceof CECase){
                selectedInspector = getSessionBean().getSessCECase().getManager();
            }
        }
    }
    
    /**
     * Listener for user requests to view a space for inspection.
     * @param ois 
     */
    public void onViewInspectedSpaceLinkClick(OccInspectedSpace ois){
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        currentInspectedSpace = oic.configureElementDisplay(ois);
        System.out.println("OccInspectionsBB.onViewInspectedSpaceLinkClick | ois: " + ois.getInspectedSpaceID());
        
    }

    /**
     * Listener for user requests to start or end an 
     * occ inspection meta data editing session
     * @param ev
     */
    public void onToggleEditModeInspectionMetadata(ActionEvent ev) {
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        System.out.println("FieldInspectionBB.onToggleEditModeInspectionMetadata : component to update" + inspectionListComponentForUpdate);
        System.out.println("FieldInspectionBB.onToggleEditModeInspectionMetadata : current ID " + currentInspection.getInspectionID());
        System.out.println("FieldInspectionBB.onToggleEditModeInspectionMetadata : mode " + editModeInspectionMetadata);
        // if we're hitting the button and we're not in edit mode, don't udpate
        if(editModeInspectionMetadata){
            try {
                // REFACTOR
//                if(formFollowUpInspectionTo != null){
//                    currentInspection.setFollowUpToInspectionID(formFollowUpInspectionTo.getInspectionID());
//                }
                oic.updateOccInspection(currentInspection, getSessionBean().getSessUser());
                System.out.println("FieldInspectionBB.onToggleEditModeInspectionMetadata : Updated inspection ID " + currentInspection.getInspectionID());
                getFacesContext().addMessage(null,
                     new FacesMessage(FacesMessage.SEVERITY_INFO,
                             "Updated inspection ID: " + currentInspection.getInspectionID(), ""));
                refreshCurrentInspectionAndRestoreSelectedSpace();
                refreshInspectionListAndTriggerManagedListReload();
            } catch (IntegrationException | BObStatusException | BlobException ex) {
                System.out.println(ex);
                 getFacesContext().addMessage(null,
                     new FacesMessage(FacesMessage.SEVERITY_ERROR,
                             "Fatal error: Could not update inspection metadata. Please log an issue", ""));
            } 
        }
        // lastly, toggle our edit mode
        editModeInspectionMetadata = !editModeInspectionMetadata;
    }
    
    /**
     * Listener for user requests to abort the occ inspection 
     * metadata update process
     * @param ev 
     */
    public void abortEditsOccInspectionMetadata(ActionEvent ev){
        //turn off edit mode
        editModeInspectionMetadata = false;
        getFacesContext().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Update cancelled!",  ""));
        
    }
    
    /**
     * Listener for user requests to start the image upload process to
     * an occ inspection
     * I take the current inspection and make it the session's 
     * BlobHolder so the reuasable code modules can take over
     * @param ev
     */
    public void onUploadImagesToInspectionInitButtonClick(ActionEvent ev){
        System.out.println("OccInspectionsBB.onUploadImagesToInspectionInitButtonClick");
            try {
                getSessionBean().setSessBlobHolder(currentInspection);
                getSessionBean().setAndRefreshSessionBlobHolderAndBuildUpstreamPool(currentInspection);
            } catch (BObStatusException | BlobException | IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not initiate photo pool",  ""));
                
            } 
    }
    
    /**
     * Listener for user requests to see the photos on an inspection
     * Sets the inspection as the blobholder for the blob UI to take over.
     * And asks our blob coordinator for my most recent blob list
     * @param ev
     */
    public void onViewPhotoPoolLinkClick(ActionEvent ev){
        
        System.out.println("OccInspectionsBB.onViewPhotoPoolLinkClick");
            try {
                getSessionBean().setSessBlobHolder(currentInspection);
                getSessionBean().setAndRefreshSessionBlobHolderAndBuildUpstreamPool(currentInspection);
            } catch (BObStatusException | BlobException | IntegrationException ex) {
                 System.out.println(ex);
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not initiate photo pool",  ""));
                
            }
        
    }
    
    
    /**
     * Listener for user requests to start the certification process
     * @param ev
     */
    public void onCertifyInspectionInitButtonChange(ActionEvent ev){
        System.out.println("FieldInspectionBB.onCertifyInspectionInitButtonChange");
        
        
    }
    
    /**
     * Listener for user requests to certify the given
     * @param ev
     */
    public void onCertifyInspectionCommitButtonChange(ActionEvent ev){

        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        try {
            currentInspection.setDetermination(selectedDetermination);
            IFace_inspectable inspectable;
            if(currentInspection.getDomainEnum() == DomainEnum.OCCUPANCY){
                inspectable = getSessionBean().getSessOccPeriod();
            } else {
                inspectable = getSessionBean().getSessCECase();
            }
            oic.inspectionAction_certifyInspection(currentInspection, getSessionBean().getSessUser(), inspectable);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Inspection determination has been certified and inspection is now locked!",  ""));
            refreshCurrentInspectionAndRestoreSelectedSpace();
            refreshInspectionListAndTriggerManagedListReload();
        } catch (IntegrationException | AuthorizationException | BObStatusException | BlobException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.getMessage(),  ""));

        } 
    }
    
    /**
     * Listener for user requests to remove Finalization of current occ inspection
     */
    public void onRemoveFinalizationOfInspection(){
        if(currentInspection != null){
            OccInspectionCoordinator oic = getOccInspectionCoordinator();
            IFace_inspectable inspectable;
            if(currentInspection.getDomainEnum() == DomainEnum.OCCUPANCY){
                inspectable = getSessionBean().getSessOccPeriod();
            } else {
                inspectable = getSessionBean().getSessCECase();
            }
            try {
                oic.removeOccInspectionFinalization(getSessionBean().getSessUser(), currentInspection, inspectable);
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Inspection has been decertified and determination removed!",  ""));
                refreshCurrentInspectionAndRestoreSelectedSpace();
                refreshInspectionListAndTriggerManagedListReload();
            } catch (IntegrationException | BObStatusException | BlobException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Could not remove certification of occ inspection: " + ex.getMessage(),  ""));
                
                
            }
        }
    }
      
    
    /**
     * Listener for user requests to move from selection of space type
     * to location descriptor choice
     * @param ev 
     */
    public void onAddSpaceTypeSelectedButtonChange(ActionEvent ev){
        
        if(currentSpaceType != null){
            System.out.println("FieldInspectionBB.onAddSpaceTypeSelectedButtonChange | space type = " + currentSpaceType.getSpaceTypeTitle());
        } else {
            System.out.println("FieldInspectionBB.onAddSpaceTypeSelectedButtonChange | NO SPACE TYPE SELECTED! YIKES!");
            
        }
        
    }
      
    /**
     * Listener for user requests to add a chosen OccSpace to the current inspection
     * 
     */
    public void onAddSpaceToInspectionCommitButtonChange() {
        if (currentInspection == null) {
            System.out.println("Can't initialize add space to inspection: selected inspection object is null");
            return;
        }
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        try {
//             Maybe its important that i'm not passing a user or OccInspectionStatusEnum but i think its fine.
            currentInspectedSpace = oic.inspectionAction_commenceInspectionOfSpaceTypeChecklistified(currentInspection, 
                                                currentInspection.getInspector(), 
                                                currentSpaceType, 
                                                OccInspectionStatusEnum.NOTINSPECTED, 
                                                currentLocationDescriptor);
            getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_INFO,
                         "Added space to inspection", ""));
            refreshCurrentInspectionAndRestoreSelectedSpace();

        } catch (IntegrationException | BObStatusException | BlobException ex) {
            System.out.println("Failed to add selected space to skeleton inspection object: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Cannot add space to inspection." + ex.toString(), ""));
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
     * Listener for user requests to bring up the space type selection
     * dialog
     * @param ev 
     */
    public void onAddSpaceInitLinkClick(ActionEvent ev){
        System.out.println("FieldInspectionBB.onAddSpaceLinkClick");

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
            oise = oic.inspectionAction_recordElementInspectionByStatusEnum(oise, getSessionBean().getSessUser(), currentInspection, useDefaultFindingsOnCurrentOISE);
//            refreshCurrentInspectionAndRestoreSelectedSpace();
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
                oic.inspectionAction_batchConfigureInspectedSpace(currentInspectedSpace, 
                                                                    selectedElementStatusForBatch, 
                                                                    getSessionBean().getSessUser(), 
                                                                    currentInspection, 
                                                                    useDefaultFindingsOnCurrentOISE);
                refreshCurrentInspectionAndRestoreSelectedSpace();
                getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_INFO,
                               "Success! Applied status " 
                                       + selectedElementStatusForBatch.getLabel() 
                                       + " To all ordinances in this space!", ""));
            } catch (BObStatusException | IntegrationException | BlobException ex) {
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
            oic.inspectionAction_updateSpaceElementData(currentInspectedSpace, getSessionBean().getSessUser(), currentInspection, null);
            refreshCurrentInspectionAndRestoreSelectedSpace();
            getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_INFO,
                           "Saved changes to space: " + currentInspectedSpace.getType().getSpaceTypeTitle() + " id " + currentInspectedSpace.getInspectedSpaceID(), ""));
        } catch (IntegrationException | BObStatusException | BlobException | AuthorizationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_ERROR,
                           ex.getMessage(), ""));
            
        }
    }
    
    /**
     * LIstener for user requests to abandon the space editing endeavor complete
     * @param ev 
     */
    public void onAbortSpaceInspectionEntry(ActionEvent ev){
        System.out.println("OccPeriodBB.onAbortSpaceInspectionEntry");
          getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_INFO,
                           "Operation aborted", ""));
            
        
    }
     
    /**
     * Listener for user requests to go back and manage a space's ordinances
     * @param ev 
     */
    public void onReturnToSpaceInspectionEntry(ActionEvent ev){
        System.out.println("OccPeriodBB.onReturnToSpaceInspectionEntry");
    }
    
    
    /**
     * Listener for user requests to save their confirmations of violated
     * ordinances in the space
     * @param ev 
     */
    public void onSaveViolatedOrdinanceConfirmationSettings(ActionEvent ev){
        System.out.println("FieldInspectionBB.onSaveViolatedOrdinanceConfirmationSettings");
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        try {
            oic.inspectionAction_updateSpaceElementData(currentInspectedSpace, getSessionBean().getSessUser(), currentInspection, OccInspectionStatusEnum.VIOLATION);
            refreshCurrentInspectionAndRestoreSelectedSpace();
            getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_INFO,
                           "Saved changes to space: " + currentInspectedSpace.getType().getSpaceTypeTitle() + " id " + currentInspectedSpace.getInspectedSpaceID(), ""));
        } catch (IntegrationException | BObStatusException | BlobException | AuthorizationException ex) {
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
     * 
     * I also tell the SessionBean to refresh the current occ period so
     * the UI just has to say to udpate any linked UI elements to see
     * updates
     */
    private void refreshCurrentInspectionAndRestoreSelectedSpace() 
            throws IntegrationException, BObStatusException, BlobException{
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        if(currentInspection != null){
            currentInspection = oic.getOccInspection(currentInspection.getInspectionID());
            getSessionBean().setSessFieldInspection(currentInspection);
            System.out.println("OccInspectionBB.refreshCurrentInspectionAndRestoreSelectedSpace | " + currentInspection.getInspectionID() );
            if(currentInspectedSpace != null){
                // go find my current space in the inspection and make it the selected one
                for(OccInspectedSpace ois: currentInspection.getInspectedSpaceList()){
                    if(ois.getInspectedSpaceID() == currentInspectedSpace.getInspectedSpaceID()){
                        currentInspectedSpace = oic.configureElementDisplay(ois);
                        System.out.println("OccInspectionsBB.refreshCurrentInspectionAndRestoreSelectedSpace | oisid " + ois.getInspectedSpaceID());
                        break;
                    }
                }
            }
        } else {
            throw new BObStatusException("No current inspection selected; cannot refresh");
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
        currentInspectedSpaceElement = oise;
    }
    
    /**
     * Listener for user requests to delete a space
     * @param ev 
     */
    public void onSpaceTypeRemoveLinkClick(ActionEvent ev){
        System.out.println("OccInspectionBB.onSpaceTypeRemoveLinkClick");
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        try {
            oic.inspectionAction_removeSpaceFromInspection(currentInspectedSpace, getSessionBean().getSessUser(), currentInspection);
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
    
    /**
     * Listener for user requests to deactivate the selected inspection
     * @param ev
     */
    public void onDeactivateInspectionButtonClick(ActionEvent ev){
        if(currentInspection != null){
            
            System.out.println("OccInspectionBB.onDeactivateInspectionButtonClick | deactivating inspection " + currentInspection.getInspectionID());
            OccInspectionCoordinator oic = getOccInspectionCoordinator();
            try {
                oic.deactivateOccInspection(getSessionBean().getSessUser(), currentInspection, currentInspectable);
                refreshInspectionListAndTriggerManagedListReload();
                 getFacesContext().addMessage(null,
                           new FacesMessage(FacesMessage.SEVERITY_INFO,
                                   "Occ Inspection deactivated with ID " + currentInspection.getInspectionID(), ""));
            } catch (BObStatusException | AuthorizationException | IntegrationException  ex) {
                System.out.println(ex);
                 getFacesContext().addMessage(null,
                           new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                   ex.getMessage(), ""));
            } 
        } else {
            System.out.println("OccInspectionBB.onDeactivateInspectionButtonClick | cannot deactivate null current inspection");
            getFacesContext().addMessage(null,
                      new FacesMessage(FacesMessage.SEVERITY_ERROR,
                              "Page setup error; cannot deactivate inspection", ""));
        }
    }
    
    /**
     * Listener for user requests to move the current inspection to a new occ period parent 
     * @return  
     */
    public String onOccPeriodXferButtonClick(){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.transferInspectionOccPeriod(currentInspection, occPeriodIDFortransferFormField, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                               "Transfer success!", ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                               ex.getMessage(), ""));
             return "";
        }
        return "missionControl";
        
        
    }
    
    /**
     * Listener for user requests to add to or view blobs on the oise
     * @param oise 
     */
    public void onManageBlobsOnInspectedElement(OccInspectedSpaceElement oise){
        try {
            getSessionBean().setAndRefreshSessionBlobHolderAndBuildUpstreamPool(oise);
        } catch (BObStatusException | BlobException | IntegrationException ex) {
            System.out.println(ex);
        } 
        
    }
 
    
    
    /**
     * Listener for user requests to start the report building process
     * @param ev
     */
    public void onFieldInspectionReportInitLinkClick(ActionEvent ev){
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        IFace_inspectable ins = null;
        try {
            if(currentInspection.getDomainEnum() == DomainEnum.CODE_ENFORCEMENT){
                ins = getSessionBean().getSessCECase();
            } else if(currentInspection.getDomainEnum() == DomainEnum.OCCUPANCY){
                ins = getSessionBean().getSessOccPeriod();
            } else {
                throw new BObStatusException("Field Inspection must have a domain");
            }
            reportConfigFIN = oic.getOccInspectionReportConfigDefault(
                    currentInspection, 
                    ins,
                    getSessionBean().getSessUser());
                    reportConfigFIN.setInspection(currentInspection);
            reportConfigFIN.setInspectedProperty(getSessionBean().getSessProperty());

        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                               ex.getMessage(), ""));
            
        }
        
    }
    
    /**
     * Listener for user requests to start the report building process
     * @return 
     */
    public String onFieldInspectionReportCommitLinkClick(){
        
        reportConfigFIN.getInspection().configureVisibleSpaceElementList(reportConfigFIN.getViewSetting());
        getSessionBean().setReportConfigFieldInspection(reportConfigFIN);
        
        return "inspectionReport";
    }
    
    
    // *****************************************
    // ************ REINSPECTION STUFF *********
    // *****************************************
    
    /**
     * Listener for user requests to start a follow-up inspection
     * @param ev 
     */
    public void onSetupReinspectionButtonClick(ActionEvent ev){
        formFollowUpInspectionTo = currentInspection;
        
    }

        
    // *****************************************
    // ************ GETTERS AND SETTERS ********
    // *****************************************
    


    public List<OccChecklistTemplate> getChecklistTemplateList() {
        return checklistTemplateList;
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

    public OccLocationDescriptor getCurrentLocationDescriptor() {
        return currentLocationDescriptor;
    }

    public void setCurrentLocationDescriptor(OccLocationDescriptor currentLocationDescriptor) {
        this.currentLocationDescriptor = currentLocationDescriptor;
    }

    public OccLocationDescriptor getSkeletonLocationDescriptor() {
        return skeletonLocationDescriptor;
    }

    public FieldInspection getCurrentInspection() {
        return currentInspection;
    }

    public void setCurrentInspection(FieldInspection currentInspection) {
        this.currentInspection = currentInspection;
    }

    public OccInspectedSpace getCurrentInspectedSpace() {
        return currentInspectedSpace;
    }

    public void setCurrentInspectedSpace(OccInspectedSpace currentInspectedSpace) {
        this.currentInspectedSpace = currentInspectedSpace;
    }

    public boolean isEditModeInspectionMetadata() {
        return editModeInspectionMetadata;
    }

    public void setEditModeInspectionMetadata(boolean editModeInspectionMetadata) {
        this.editModeInspectionMetadata = editModeInspectionMetadata;
    }

  

    /**
     * @return the currentSpaceType
     */
    public OccSpaceTypeChecklistified getCurrentSpaceType() {
        return currentSpaceType;
    }

    /**
     * @param currentSpaceType the currentSpaceType to set
     */
    public void setCurrentSpaceType(OccSpaceTypeChecklistified currentSpaceType) {
        this.currentSpaceType = currentSpaceType;
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
     * @return the currentInspectedSpaceElement
     */
    public OccInspectedSpaceElement getCurrentInspectedSpaceElement() {
        return currentInspectedSpaceElement;
    }

    /**
     * @param currentInspectedSpaceElement the currentInspectedSpaceElement to set
     */
    public void setCurrentInspectedSpaceElement(OccInspectedSpaceElement currentInspectedSpaceElement) {
        this.currentInspectedSpaceElement = currentInspectedSpaceElement;
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
        System.out.println("InspectionsBB.setUseDefaultFindingsOnCurrentOISE | " + useDefaultFindingsOnCurrentOISE);
        this.useDefaultFindingsOnCurrentOISE = useDefaultFindingsOnCurrentOISE;
    }

    /**
     * @return the causeList
     */
    public List<OccInspectionCause> getCauseList() {
        return causeList;
    }

    /**
     * @return the determinationList
     */
    public List<OccInspectionDetermination> getDeterminationList() {
        return determinationList;
    }

    /**
     * @param causeList the causeList to set
     */
    public void setCauseList(List<OccInspectionCause> causeList) {
        this.causeList = causeList;
    }

    /**
     * @param determinationList the determinationList to set
     */
    public void setDeterminationList(List<OccInspectionDetermination> determinationList) {
        this.determinationList = determinationList;
    }

    /**
     * @return the occPeriodIDFortransferFormField
     */
    public int getOccPeriodIDFortransferFormField() {
        return occPeriodIDFortransferFormField;
    }

    /**
     * @param occPeriodIDFortransferFormField the occPeriodIDFortransferFormField to set
     */
    public void setOccPeriodIDFortransferFormField(int occPeriodIDFortransferFormField) {
        this.occPeriodIDFortransferFormField = occPeriodIDFortransferFormField;
    }

   

    /**
     * @return the selectedDetermination
     */
    public OccInspectionDetermination getSelectedDetermination() {
        return selectedDetermination;
    }

    /**
     * @param selectedDetermination the selectedDetermination to set
     */
    public void setSelectedDetermination(OccInspectionDetermination selectedDetermination) {
        this.selectedDetermination = selectedDetermination;
    }

    /**
     * @return the reportConfigFIN
     */
    public ReportConfigOccInspection getReportConfigFIN() {
        return reportConfigFIN;
    }

    /**
     * @param reportConfigFIN the reportConfigFIN to set
     */
    public void setReportConfigFIN(ReportConfigOccInspection reportConfigFIN) {
        this.reportConfigFIN = reportConfigFIN;
    }

    /**
     * @return the failSeverityList
     */
    public List<IntensityClass> getFailSeverityList() {
        return failSeverityList;
    }

    /**
     * @param failSeverityList the failSeverityList to set
     */
    public void setFailSeverityList(List<IntensityClass> failSeverityList) {
        this.failSeverityList = failSeverityList;
    }

    /**
     * @return the currentInspectable
     */
    public IFace_inspectable getCurrentInspectable() {
        return currentInspectable;
    }

    /**
     * @param currentInspectable the currentInspectable to set
     */
    public void setCurrentInspectable(IFace_inspectable currentInspectable) {
        this.currentInspectable = currentInspectable;
    }

    /**
     * @return the inspectionListComponentForUpdate
     */
    public String getInspectionListComponentForUpdate() {
        return inspectionListComponentForUpdate;
    }

    /**
     * @param inspectionListComponentForUpdate the inspectionListComponentForUpdate to set
     */
    public void setInspectionListComponentForUpdate(String inspectionListComponentForUpdate) {
        this.inspectionListComponentForUpdate = inspectionListComponentForUpdate;
    }

    /**
     * @return the formFollowUpInspectionTo
     */
    public FieldInspection getFormFollowUpInspectionTo() {
        return formFollowUpInspectionTo;
    }

    /**
     * @param formFollowUpInspectionTo the formFollowUpInspectionTo to set
     */
    public void setFormFollowUpInspectionTo(FieldInspection formFollowUpInspectionTo) {
        this.formFollowUpInspectionTo = formFollowUpInspectionTo;
    }

    /**
     * @return the formMigrateFailedItemsOnFinalization
     */
    public boolean isFormMigrateFailedItemsOnFinalization() {
        return formMigrateFailedItemsOnFinalization;
    }

    /**
     * @param formMigrateFailedItemsOnFinalization the formMigrateFailedItemsOnFinalization to set
     */
    public void setFormMigrateFailedItemsOnFinalization(boolean formMigrateFailedItemsOnFinalization) {
        this.formMigrateFailedItemsOnFinalization = formMigrateFailedItemsOnFinalization;
    }

    /**
     * @return the ordinanceViewOptionsList
     */
    public List<ViewOptionsOccChecklistItemsEnum> getOrdinanceViewOptionsList() {
        return ordinanceViewOptionsList;
    }

    /**
     * @param ordinanceViewOptionsList the ordinanceViewOptionsList to set
     */
    public void setOrdinanceViewOptionsList(List<ViewOptionsOccChecklistItemsEnum> ordinanceViewOptionsList) {
        this.ordinanceViewOptionsList = ordinanceViewOptionsList;
    }

    
}
