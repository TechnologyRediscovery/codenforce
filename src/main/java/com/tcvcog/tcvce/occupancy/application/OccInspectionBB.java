/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.InspectionException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.entities.occupancy.*;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsOccChecklistItemsEnum;
import com.tcvcog.tcvce.entities.reports.ReportConfigOccInspection;
import com.tcvcog.tcvce.entities.reports.ReportConfigOccPermit;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccInspectionIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

/**
 * Primary backing bean for the inspections.xhtml page which is the central
 management point for all occupancy inspection related tasks including:\
 * 
 Reviewing, editing (generally processing) occupancy applications
 Initiating all occupancy inspection related events such as starting a new 
  occupancy inspection, editing an existing one, checking on the status of one
 Initiating the creation of an occupancy permit based on a successful inspection
 
 Classes with similar functions for different core business objects:
 CaseProfileBB.java
 PersonsBB.java
 CEActionRequestsBB.java
  
 Design considerations:
 The primary methods on this bean are to manage the querying for and displaying
 occupancy inspection objects, which contain all sorts of goodies
 
 The convention in the family of backing beans that do the same kind of work
 is to maintain as a member variable a List of the main business object, 
 in this case, an OccInspection
 
 and a member variable named something like selectedXXXX or currentXXX which
 is loaded when the user clicks on a row button on the left column's data table
 display and then used to populate all of the object-specific fields in the
 right -side management page.
 *  
 * You may want separate backing beans to manage tasks related to occupancy inspections
 to keep this bean mostly about querying, displaying and selecting our core business
 object of the OccInspection
 * 
 * 
 * @author Ellen Bascomb
 */
public class OccInspectionBB extends BackingBeanUtils implements Serializable {
    
    public static final String ADD_WITH_COMPLIANCE = "comp";
    public static final String ADD_AS_UNINSPECTED = "insp";

    private OccPeriodDataHeavy currentOccPeriod;
    
    private OccInspection currentInspection;
    private PropertyUnitWithProp currentPropertyUnit;
    private Property currentProperty;
    
    private OccInspectedSpace currentInSpc;
    private OccInspectedSpaceElement currentInSpcEl;
    
    private OccLocationDescriptor currentLocation;
    private OccLocationDescriptor selectedLocation;
    private List<OccLocationDescriptor> workingLocationList;
    
    private List<OccChecklistTemplate> inspectionTemplateCandidateList;
    private OccChecklistTemplate selectedInspectionTemplate;
    
    private List<OccInspectedSpace> visibleInspectedSpaceList;
    private boolean includeSpacesWithNoElements;
    
    private OccSpaceTypeInspectionDirective selectedOccSpaceType;
    private OccSpace selectedOccSpace;
    
    private List<OccSpace> spacesInTypeList;
    private List<OccSpaceElement> elementsInSpaceList;
    
    private List<OccInspectionStatusEnum> inspectedElementAddValueCandidateList;
    private OccInspectionStatusEnum selectedInspectedElementAddValue;
    private boolean markNewlyAddedSpacesWithCompliance;
    private boolean promptForSpaceLocationUponAdd;
    
    private List<User> managerInspectorCandidateList;
    private User selectedInspector;
   
    
    // reports
    private ReportConfigOccInspection reportConfigOccInspec;
    private List<ViewOptionsOccChecklistItemsEnum> itemFilterOptions;
    
    private OccPermit currentOccPermit;
    private ReportConfigOccPermit reportConfigOccPermit;
    
   
    private String formNoteText;
  
    
     // payments
    private List<MoneyOccPeriodFeePayment> paymentList;
    private List<MoneyOccPeriodFeePayment> filteredPaymentList;
    private Payment selectedPayment;
    
    //fees
    private List<MoneyOccPeriodFeeAssigned> feeList;
    private List<MoneyOccPeriodFeeAssigned> filteredFeeList;
    private MoneyOccPeriodFeeAssigned selectedFee;
    
    /**
     * Creates a new instance of InspectionsBB
     */
    public OccInspectionBB() {
    }
    
    @PostConstruct
    public void initBean(){

        PropertyIntegrator pi = getPropertyIntegrator();
        OccupancyCoordinator oc = getOccupancyCoordinator();
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        EventCoordinator ec = getEventCoordinator();
        UserCoordinator uc = getUserCoordinator();
        
        // set our blank lists used only by elements on this page
        spacesInTypeList = new ArrayList<>();
        visibleInspectedSpaceList = new ArrayList<>();
        currentOccPeriod = getSessionBean().getSessOccPeriod();
        try {
            setupUnitMemberVariablesBasedOnCurrentOccPeriod();
        } catch (IntegrationException | BObStatusException | SearchException ex) {
            System.out.println(ex);
        }
        
        // general setting of drop-down box lists
        if(workingLocationList == null){
            workingLocationList = new ArrayList<>();
            try {
                workingLocationList.add(oii.getLocationDescriptor(
                        Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("locationdescriptor_implyfromspacename"))));
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
        }
        
        managerInspectorCandidateList = uc.user_assembleUserListForSearch(getSessionBean().getSessUser());
        itemFilterOptions = Arrays.asList(ViewOptionsOccChecklistItemsEnum.values());
        inspectedElementAddValueCandidateList = Arrays.asList(OccInspectionStatusEnum.values());
        
        try {
            inspectionTemplateCandidateList = oii.getChecklistTemplateList(getSessionBean().getSessMuni());
            reportConfigOccInspec =
                    oc.getOccInspectionReportConfigDefault(
                            currentInspection,
                            currentOccPeriod,
                            getSessionBean().getSessUser());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
       
    }
    
    private void setupUnitMemberVariablesBasedOnCurrentOccPeriod() throws IntegrationException, BObStatusException, SearchException{
        OccupancyCoordinator oc = getOccupancyCoordinator();
        PropertyIntegrator pi = getPropertyIntegrator();
        if(currentOccPeriod != null){
                if(currentOccPeriod.getConfiguredTS() == null){
                    currentOccPeriod = oc.assembleOccPeriodDataHeavy(currentOccPeriod, getSessionBean().getSessUser().getMyCredential());
                }
                currentPropertyUnit = pi.getPropertyUnitWithProp(currentOccPeriod.getPropertyUnitID());
                currentInspection = currentOccPeriod.getGoverningInspection();
                // all inspected spaces are visible by default
                if(currentInspection != null){
                    currentInspection.setViewSetting(ViewOptionsOccChecklistItemsEnum.ALL_ITEMS);
                }
            }
        
       
        feeList = currentOccPeriod.getFeeList();
        paymentList = currentOccPeriod.getPaymentList();
        
    }
    
    public void loadSpacesInType(){
        if(selectedOccSpaceType != null){
            spacesInTypeList = selectedOccSpaceType.getSpaceList();
            System.out.println("OccInspectionBB.loadSpacesInType");
        }
    }
    
    public void loadElementsInSpace(){
        if(selectedOccSpace != null){
            elementsInSpaceList = selectedOccSpace.getSpaceElementList();
            System.out.println("OccInspectionBB.loadElementsInSpace");
        }
    }

    // TODO: finish me
    public void deletePhoto(int photoID){
        
    }
    
    
     public void commenceOccInspection(){

    }
    
    /**
     * Placeholder method for the action listener on the client side button
     * @param ev 
     */
    public void viewStaticChecklistTemplate(ActionEvent ev){
        // dialog will appear clientside on complete
    }
    
    public void checklistAction_addAllRequiredSpaceTypes(ActionEvent ev){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        System.out.println("OccinspectionBB.addAllrequiredSpaceTypes");
          
        for(OccSpaceTypeInspectionDirective stid: currentInspection.getChecklistTemplate().getOccSpaceTypeTemplateList()){
            for(OccSpace spc: stid.getSpaceList()){
                if(spc.isRequired()){
                    try {
                        oc.inspectionAction_commenceSpaceInspection(    
                                currentInspection,
                                getSessionBean().getSessUser(),
                                spc,
                                selectedInspectedElementAddValue,
                                null);
                    } catch (IntegrationException ex) {
                        System.out.println(ex);
                    }
                }
            }
        }
        reloadCurrentInspection();
    }
    
    public void initiateOccLocationDescriptorDialog(){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        currentLocation = oc.getOccLocationDescriptorSkeleton();
    }
    
    public void addNewLocationDescriptor(){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        int freshLocID = 0;
        try {
            oc.addNewLocationDescriptor(currentLocation);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Created new location descriptor of ID " + freshLocID, ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                ex.getMessage(), ""));
        }
    }
    
    public void initiateOccLocationUpdate(OccLocationDescriptor old){
        currentLocation = old;
    }
    
    public void initializeSpaceAdd(ActionEvent ev){
        // does this need to be nothing?
        
    }
    
    public void checklistAction_activateOccInspection(OccInspection ins){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        if(getSessionBean().getSessUser().getMyCredential().isHasEnfOfficialPermissions()){
            try {
                
                oc.activateOccInspection(ins);
                currentInspection = ins;
                reloadCurrentInspection();
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Inspection ID " + ins.getInspectionID() + " is now your active inspection", ""));
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        ex.getMessage(), ""));
            }
        }
    }
    
    
    
    private void reloadCurrentInspection(){
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        try {
            if(currentInspection != null){
                currentInspection = oii.getOccInspection(currentInspection.getInspectionID());
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Reloaded inspection ID " + currentInspection.getInspectionID(), ""));
            }
        } catch (IntegrationException ex) {
            System.out.println(ex);
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Unable to reload inspection", ""));
        }
    }
    

    
    public void markInspectionAsGoverning(OccInspection insp){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.activateOccInspection(insp);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Success! Activated inspection ID: " + insp.getInspectionID(), ""));
        } catch (IntegrationException ex) {
            
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                ex.getMessage(), ""));
        }
    }
    
    public void checklistAction_addSpaceToChecklist(OccSpace space) {
        FacesContext fc = getFacesContext();
        String paramVal = fc.getExternalContext().getRequestParameterMap().get("occperiod-elementstatusonadd");
        System.out.println("OccInspectionBB.addSpaceToChecklist | param val: " + paramVal);
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.inspectionAction_commenceSpaceInspection(currentInspection,
                            getSessionBean().getSessUser(),
                            space,
                            selectedInspectedElementAddValue,
                            null);
            
        System.out.println("OccInspectionBB.addSpaceToChecklist | space name: " + space.getName());
        reloadCurrentInspection();
        selectedOccSpace = null;
        selectedOccSpaceType = null;
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Space added to checklist!", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Unable to add space to checklist", ""));
        }
    }
    
    public void filterChecklist_failedItems(ActionEvent ev){
        currentInspection.setViewSetting(ViewOptionsOccChecklistItemsEnum.FAILED_ITEMS_ONLY);
//        currentInspection.configureVisibleSpaceElementList();
    }
    
    public void filterChecklist_uninspectedItems(ActionEvent ev){
        currentInspection.setViewSetting(ViewOptionsOccChecklistItemsEnum.UNISPECTED_ITEMS_ONLY);
    }
    
    public void filterChecklist_allItems(ActionEvent ev){
        currentInspection.setViewSetting(ViewOptionsOccChecklistItemsEnum.ALL_ITEMS);
    }
    
    
 
      public void reloadCurrentOccPeriodDataHeavy(){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            currentOccPeriod = oc.assembleOccPeriodDataHeavy(currentOccPeriod, getSessionBean().getSessUser().getMyCredential());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Reloaded occ period ID " + currentOccPeriod.getPeriodID(), ""));
        } catch (IntegrationException | BObStatusException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Unable to reload occ period", ""));
        }
        
    }
    
    public void checklistAction_implementNewInspectionChecklist(ActionEvent ev){
        if(selectedInspectionTemplate != null){
            OccupancyCoordinator oc = getOccupancyCoordinator();
            try {
                oc.activateOccInspection(oc.inspectionAction_commenceOccupancyInspection(null, selectedInspectionTemplate, currentOccPeriod, getSessionBean().getSessUser()));
                reloadCurrentOccPeriodDataHeavy();
                reloadCurrentInspection();
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Success! Created new inspection.", ""));
            } catch (InspectionException | IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.getMessage(), ""));
            }
        } else {
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please select an inspection template!", ""));
        }
    }
    
    /**
     * Action listener for new note
     * @param ev 
     */
    public void initiateNoteOnInspection(ActionEvent ev){
        formNoteText = null;
    }
    
    
    
    /**
     * Action listener for new note on the occ period itself
     * @param ev 
     */
    public void initiateNoteOnPeriod(ActionEvent ev){
        formNoteText = null;
    }
    
    
    
    
    public void attachNoteToInspection(ActionEvent ev){
                 OccInspectionIntegrator oii = getOccInspectionIntegrator();
         if(currentInspection != null){
            StringBuilder sb = new StringBuilder();
            if(currentInspection.getNotes() !=null){
                sb.append(currentInspection.getNotes());
                sb.append("<br />****************<br />");
            }
            sb.append(formNoteText);
            currentInspection.setNotes(sb.toString());
           try {
               oii.updateOccInspection(currentInspection);
                getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_INFO,
                   "Success! Note added", ""));
           } catch (IntegrationException ex) {
                getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_ERROR,
                   ex.getMessage(), ""));
           }
            reloadCurrentInspection();
         }
        
    }
    
    /**
     * Listener method called when the user is done creating a new note text
     * @param ev 
     */
    public void attachNoteToPeriod(ActionEvent ev){
        SystemCoordinator sc = getSystemCoordinator();
        OccupancyCoordinator oc = getOccupancyCoordinator();
        currentOccPeriod.setNotes(  sc.formatAndAppendNote(getSessionBean().getSessUser(),
                                    getSessionBean().getSessUser().getMyCredential(),
                                    formNoteText,
                                    currentOccPeriod.getNotes()));
        try {
            oc.attachNoteToOccPeriod(currentOccPeriod);
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_INFO,
               "Success! Note added", ""));
        } catch (IntegrationException ex) {
                getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_ERROR,
                   ex.getMessage(), ""));
        }
    }
    
    
    
        
    
    /**
     * Utility method called when the user begins inspection report process.
     * The report object is put in place on the bean during init()
     * @param ev 
     */
    public void reports_initializeOccInspectionReport(ActionEvent ev){
       // go ahead!
    }
    
    public String reports_generateOccInspectionReport(){
        reloadCurrentInspection();
        currentOccPeriod.setGoverningInspection(currentInspection);
        reportConfigOccInspec.setOccPeriod(currentOccPeriod);
        reportConfigOccInspec.setPropUnitWithProp(currentPropertyUnit);
        getSessionBean().setReportConfigInspection(reportConfigOccInspec);
        return "inspectionReport";
    }
    
    public void reports_initializeOccPermitReport(){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        currentOccPermit = oc.getOccPermitSkeleton(getSessionBean().getSessUser());
    }

    public String reports_generateOccPermit(OccPermit permit){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        currentOccPermit = permit;
        reportConfigOccPermit = oc.getOccPermitReportConfigDefault( currentOccPermit, 
                                                                    currentOccPeriod, 
                                                                    currentPropertyUnit, 
                                                                    getSessionBean().getSessUser());
        getSessionBean().setReportConfigOccPermit(reportConfigOccPermit);
        
        return "occPermit";
    }
    
    
 
    /**
     * Called when the user clicks a command button inside the row of the
 OccInspection table to manage it
     * @param ev
     */
    public void checklistAction_beginInspectionMetadataEdit(ActionEvent ev){
        // do nothing since a dialog is brought up for the user
        
    }
    
     /**
      * Edits the currentInspection 
      * @param e 
      */
     public void checklistAction_editOccupancyInspectionMetadata(ActionEvent e){
         OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.updateOccInspection(currentInspection, getSessionBean().getSessUser());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }
     
     public void checklistAction_removeSpaceFromChecklist(OccInspectedSpace spc){
         OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.inspectionAction_removeSpaceFromChecklist(spc, getSessionBean().getSessUser(), currentInspection);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Successfully removed InspectedSpace ID: " + spc.getInspectedSpaceID() , ""));
        } catch (IntegrationException ex) {
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                ex.getMessage(), ""));
        }
         reloadCurrentInspection();
     }
     
     public void checklistAction_recordComplianceWithElement(OccInspectedSpaceElement inSpcEl){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.inspectionAction_recordComplianceWithInspectedElement(    inSpcEl,
                                                        getSessionBean().getSessUser(),
                                                        currentInspection);
            reloadCurrentInspection();
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Compliance recorded for Space Element: " + inSpcEl.getInspectedSpaceElementID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                ex.getMessage() + inSpcEl.getInspectedSpaceElementID(), ""));
        }
     }
     
     public void checklistAction_removeComplianceWithElement(OccInspectedSpaceElement inSpcEl){
         OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.inspectionAction_inspectWithoutCompliance(inSpcEl,
                                                        getSessionBean().getSessUser(),
                                                        currentInspection);
            reloadCurrentInspection();
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Compliance removed for Space Element: " + inSpcEl.getInspectedSpaceElementID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                ex.getMessage() + inSpcEl.getInspectedSpaceElementID(), ""));
        }
     }
     
     public void checklistAction_inspectElementWithoutCompliance(OccInspectedSpaceElement inSpcEl){
         OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.inspectionAction_inspectWithoutCompliance(inSpcEl,
                                        getSessionBean().getSessUser(),
                                        currentInspection);
            reloadCurrentInspection();
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Compliance removed for Space Element: " + inSpcEl.getInspectedSpaceElementID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                ex.getMessage() + inSpcEl.getInspectedSpaceElementID(), ""));
        }
     }
     
     public void checklistAction_clearInspectionOfElement(OccInspectedSpaceElement inSpcEl){
        System.out.println("OccInspectionBB.clearInspection");
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.clearInspectionOfElement(    inSpcEl,
                                            getSessionBean().getSessUser(),
                                            currentInspection);
            reloadCurrentInspection();
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Inspection cleared for Space Element: " + inSpcEl.getInspectedSpaceElementID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                ex.getMessage() + inSpcEl.getInspectedSpaceElementID(), ""));
        }
     }
     
     public void checklistAction_initiateNoteOnInspectedElement(OccInspectedSpaceElement spcEl){
         formNoteText = null;
         currentInSpcEl = spcEl;
         
     }
     
     public void checklistAction_addNoteToInspectedElement(){
         OccInspectionIntegrator oii = getOccInspectionIntegrator();
         if(currentInSpcEl != null){
            StringBuilder sb = new StringBuilder();
            if(currentInSpcEl.getNotes() !=null){
                sb.append(currentInSpcEl.getNotes());
                sb.append("****************<br />");
            }
            sb.append(formNoteText);
            currentInSpcEl.setNotes(sb.toString());
           try {
               oii.updateInspectedSpaceElement(currentInSpcEl);
                getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_INFO,
                   "Success! Note added", ""));
           } catch (IntegrationException ex) {
                getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_ERROR,
                   ex.getMessage(), ""));
           }
            reloadCurrentInspection();
         }
     }
     
     public void checklistAction_recordComplianceForAllElements(ActionEvent ev){
            
     }
     
     public void checklistAction_certifyInspection(ActionEvent ev){
         OccupancyCoordinator oc = getOccupancyCoordinator();
         currentInspection.setPassedInspectionTS(LocalDateTime.now());
         currentInspection.setPassedInspectionCertifiedBy(getSessionBean().getSessUser());
        try {
            oc.updateOccInspection(currentInspection, selectedInspector);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Success! Inspection certified as passed!", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Error! Unable to certify inspection as passed, sorry.", ""));
        }
        reloadCurrentOccPeriodDataHeavy();
     }
     
     /**
      * Placeholder method so the update button UI can call a method
      * @param ev 
      */
     public void initiatePropUnitUpdate(ActionEvent ev){
        // do nothing!
     }
     

     
     
     
     public void authorizeOccPeriod(ActionEvent ev){
         OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.authorizeOccPeriod(currentOccPeriod, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_INFO,
               "Success! Occupancy period ID " + currentOccPeriod.getPeriodID() 
                       + " is now authorized and permits can be generated.", ""));
        } catch (AuthorizationException | BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_ERROR,
               ex.getMessage(), ""));
        }
     }
     
     public void updatePeriodPropUnit(){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.updateOccPeriodPropUnit(currentOccPeriod, currentPropertyUnit);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "The current occupancy period has been assigned to property unit ID " + currentPropertyUnit.getUnitID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                ex.getMessage(), ""));
        }
        reloadCurrentOccPeriodDataHeavy();
     }
     
    public String editOccPeriodPayments(){
         getSessionBean().setFeeManagementDomain(EventDomainEnum.OCCUPANCY);
         getSessionBean().setFeeManagementOccPeriod(currentOccPeriod);
         getSessionBean().getNavStack().pushCurrentPage();
         
         return "payments";
     }
     
    public String editOnePayment(Payment thisPayment){
         getSessionBean().setFeeManagementDomain(EventDomainEnum.OCCUPANCY);
         getSessionBean().setSessionPayment(thisPayment);
         getSessionBean().getNavStack().pushCurrentPage();
         
         return "payments";
     }
     
     public String editOccPeriodFees(){
         getSessionBean().setFeeManagementDomain(EventDomainEnum.OCCUPANCY);
         getSessionBean().setFeeManagementOccPeriod(currentOccPeriod);
         getSessionBean().getNavStack().pushCurrentPage();
         
         return "feeManage";
     }
     
     
     public void editLocation(OccInspectedSpace inSpace){
         
     }
     
   
     
    
     /**
      * We can only delete one that was JUST made - OK if this doesn't get implemented
      * until the end
      * @param e 
      */
    public void deleteSelectedOccupancyInspection(ActionEvent e){
        OccupancyIntegrator oii = getOccupancyIntegrator();
            
    }

    /**
     * Happens in a dialog in inspections.xhtml
     * @param e 
     */
    public void updateOccInspectionCommitChanges(ActionEvent e){
        OccupancyIntegrator oii = getOccupancyIntegrator();
        OccInspectionIntegrator ci = getOccInspectionIntegrator();
        
        try{
            ci.updateOccInspection(currentInspection);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Occupancy Inspection Record updated!", ""));
        } catch (IntegrationException ex){
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to update occupancy inspection record in database.",
                    "This must be corrected by the System Administrator"));
        }
    }

    /**
     * @return the currentInspection
     */
    public OccInspection getCurrentInspection() {
        return currentInspection;
    }

    /**
     * @param currentInspection the currentInspection to set
     */
    public void setCurrentInspection(OccInspection currentInspection) {
        this.currentInspection = currentInspection;
    }

    /**
     * @return the selectedOccSpaceType
     */
    public OccSpaceTypeInspectionDirective getSelectedOccSpaceType() {
        return selectedOccSpaceType;
    }

    /**
     * @param selectedOccSpaceType the selectedOccSpaceType to set
     */
    public void setSelectedOccSpaceType(OccSpaceTypeInspectionDirective selectedOccSpaceType) {
        this.selectedOccSpaceType = selectedOccSpaceType;
    }

    /**
     * @return the spacesInTypeList
     */
    public List<OccSpace> getSpacesInTypeList() {
        
        return spacesInTypeList;
    }

    /**
     * @param spacesInTypeList the spacesInTypeList to set
     */
    public void setSpacesInTypeList(List<OccSpace> spacesInTypeList) {
        this.spacesInTypeList = spacesInTypeList;
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
     * @return the currentInSpcEl
     */
    public OccInspectedSpaceElement getCurrentInSpcEl() {
        return currentInSpcEl;
    }

    /**
     * @param currentInSpcEl the currentInSpcEl to set
     */
    public void setCurrentInSpcEl(OccInspectedSpaceElement currentInSpcEl) {
        this.currentInSpcEl = currentInSpcEl;
    }

    /**
     * @return the currentInSpc
     */
    public OccInspectedSpace getCurrentInSpc() {
        return currentInSpc;
    }

    /**
     * @param currentInSpc the currentInSpc to set
     */
    public void setCurrentInSpc(OccInspectedSpace currentInSpc) {
        this.currentInSpc = currentInSpc;
    }

    /**
     * @return the managerInspectorCandidateList
     */
    public List<User> getManagerInspectorCandidateList() {
        return managerInspectorCandidateList;
    }

    /**
     * @param managerInspectorCandidateList the managerInspectorCandidateList to set
     */
    public void setManagerInspectorCandidateList(List<User> managerInspectorCandidateList) {
        this.managerInspectorCandidateList = managerInspectorCandidateList;
    }

    /**
     * @return the selectedInspector
     */
    public User getSelectedInspector() {
        return selectedInspector;
    }

    /**
     * @param selectedInspector the selectedInspector to set
     */
    public void setSelectedInspector(User selectedInspector) {
        this.selectedInspector = selectedInspector;
    }

    /**
     * @return the currentOccPeriod
     */
    public OccPeriod getCurrentOccPeriod() {
        return currentOccPeriod;
    }

    /**
     * @return the currentPropertyUnit
     */
    public PropertyUnitWithProp getCurrentPropertyUnit() {
        return currentPropertyUnit;
    }

    /**
     * @return the currentProperty
     */
    public Property getCurrentProperty() {
        return currentProperty;
    }

    /**
     * @param currentOccPeriod the currentOccPeriod to set
     */
    public void setCurrentOccPeriod(OccPeriodDataHeavy currentOccPeriod) {
        this.currentOccPeriod = currentOccPeriod;
    }

    /**
     * @param currentPropertyUnit the currentPropertyUnit to set
     */
    public void setCurrentPropertyUnit(PropertyUnitWithProp currentPropertyUnit) {
        this.currentPropertyUnit = currentPropertyUnit;
    }

    /**
     * @param currentProperty the currentProperty to set
     */
    public void setCurrentProperty(Property currentProperty) {
        this.currentProperty = currentProperty;
    }

    /**
     * @return the reportConfigOccInspec
     */
    public ReportConfigOccInspection getReportConfigOccInspec() {
        return reportConfigOccInspec;
    }

    /**
     * @return the reportConfigOccPermit
     */
    public ReportConfigOccPermit getReportConfigOccPermit() {
        return reportConfigOccPermit;
    }

    /**
     * @param reportConfigOccInspec the reportConfigOccInspec to set
     */
    public void setReportConfigOccInspec(ReportConfigOccInspection reportConfigOccInspec) {
        this.reportConfigOccInspec = reportConfigOccInspec;
    }

    /**
     * @param reportConfigOccPermit the reportConfigOccPermit to set
     */
    public void setReportConfigOccPermit(ReportConfigOccPermit reportConfigOccPermit) {
        this.reportConfigOccPermit = reportConfigOccPermit;
    }

    /**
     * @return the currentOccPermit
     */
    public OccPermit getCurrentOccPermit() {
        return currentOccPermit;
    }

    /**
     * @param currentOccPermit the currentOccPermit to set
     */
    public void setCurrentOccPermit(OccPermit currentOccPermit) {
        this.currentOccPermit = currentOccPermit;
    }

    
    /**
     * @return the markNewlyAddedSpacesWithCompliance
     */
    public boolean isMarkNewlyAddedSpacesWithCompliance() {
        return markNewlyAddedSpacesWithCompliance;
    }

    /**
     * @return the promptForSpaceLocationUponAdd
     */
    public boolean isPromptForSpaceLocationUponAdd() {
        return promptForSpaceLocationUponAdd;
    }

    /**
     * @param markNewlyAddedSpacesWithCompliance the markNewlyAddedSpacesWithCompliance to set
     */
    public void setMarkNewlyAddedSpacesWithCompliance(boolean markNewlyAddedSpacesWithCompliance) {
        this.markNewlyAddedSpacesWithCompliance = markNewlyAddedSpacesWithCompliance;
    }

    /**
     * @param promptForSpaceLocationUponAdd the promptForSpaceLocationUponAdd to set
     */
    public void setPromptForSpaceLocationUponAdd(boolean promptForSpaceLocationUponAdd) {
        this.promptForSpaceLocationUponAdd = promptForSpaceLocationUponAdd;
    }

    /**
     * @return the selectedOccSpace
     */
    public OccSpace getSelectedOccSpace() {
        return selectedOccSpace;
    }

    /**
     * @param selectedOccSpace the selectedOccSpace to set
     */
    public void setSelectedOccSpace(OccSpace selectedOccSpace) {
        this.selectedOccSpace = selectedOccSpace;
    }

    /**
     * @return the elementsInSpaceList
     */
    public List<OccSpaceElement> getElementsInSpaceList() {
        return elementsInSpaceList;
    }

    /**
     * @param elementsInSpaceList the elementsInSpaceList to set
     */
    public void setElementsInSpaceList(List<OccSpaceElement> elementsInSpaceList) {
        this.elementsInSpaceList = elementsInSpaceList;
    }

    /**
     * @return the currentLocation
     */
    public OccLocationDescriptor getCurrentLocation() {
        return currentLocation;
    }

    /**
     * @param currentLocation the currentLocation to set
     */
    public void setCurrentLocation(OccLocationDescriptor currentLocation) {
        this.currentLocation = currentLocation;
    }

    /**
     * @return the selectedLocation
     */
    public OccLocationDescriptor getSelectedLocation() {
        return selectedLocation;
    }

    /**
     * @param selectedLocation the selectedLocation to set
     */
    public void setSelectedLocation(OccLocationDescriptor selectedLocation) {
        this.selectedLocation = selectedLocation;
    }
    
    
    /**
     * @return the workingLocationList
     */
    public List<OccLocationDescriptor> getWorkingLocationList() {
        return workingLocationList;
    }

    /**
     * @param workingLocationList the workingLocationList to set
     */
    public void setWorkingLocationList(List<OccLocationDescriptor> workingLocationList) {
        this.workingLocationList = workingLocationList;
    }

  
    /**
     * @return the visibleInspectedSpaceList
     */
    public List<OccInspectedSpace> getVisibleInspectedSpaceList() {
        return visibleInspectedSpaceList;
    }

    /**
     * @param visibleInspectedSpaceList the visibleInspectedSpaceList to set
     */
    public void setVisibleInspectedSpaceList(List<OccInspectedSpace> visibleInspectedSpaceList) {
        this.visibleInspectedSpaceList = visibleInspectedSpaceList;
    }

    /**
     * @return the includeSpacesWithNoElements
     */
    public boolean isIncludeSpacesWithNoElements() {
        return includeSpacesWithNoElements;
    }

    /**
     * @param includeSpacesWithNoElements the includeSpacesWithNoElements to set
     */
    public void setIncludeSpacesWithNoElements(boolean includeSpacesWithNoElements) {
        this.includeSpacesWithNoElements = includeSpacesWithNoElements;
    }

  

    /**
     * @return the itemFilterOptions
     */
    public List<ViewOptionsOccChecklistItemsEnum> getItemFilterOptions() {
        return itemFilterOptions;
    }

    /**
     * @param itemFilterOptions the itemFilterOptions to set
     */
    public void setItemFilterOptions(List<ViewOptionsOccChecklistItemsEnum> itemFilterOptions) {
        this.itemFilterOptions = itemFilterOptions;
    }

    /**
     * @return the inspectionTemplateCandidateList
     */
    public List<OccChecklistTemplate> getInspectionTemplateCandidateList() {
        return inspectionTemplateCandidateList;
    }

    /**
     * @param inspectionTemplateCandidateList the inspectionTemplateCandidateList to set
     */
    public void setInspectionTemplateCandidateList(List<OccChecklistTemplate> inspectionTemplateCandidateList) {
        this.inspectionTemplateCandidateList = inspectionTemplateCandidateList;
    }

    /**
     * @return the selectedInspectionTemplate
     */
    public OccChecklistTemplate getSelectedInspectionTemplate() {
        return selectedInspectionTemplate;
    }

    /**
     * @param selectedInspectionTemplate the selectedInspectionTemplate to set
     */
    public void setSelectedInspectionTemplate(OccChecklistTemplate selectedInspectionTemplate) {
        this.selectedInspectionTemplate = selectedInspectionTemplate;
    }

    /**
     * @return the selectedInspectedElementAddValue
     */
    public OccInspectionStatusEnum getSelectedInspectedElementAddValue() {
        return selectedInspectedElementAddValue;
    }

    /**
     * @param selectedInspectedElementAddValue the selectedInspectedElementAddValue to set
     */
    public void setSelectedInspectedElementAddValue(OccInspectionStatusEnum selectedInspectedElementAddValue) {
        this.selectedInspectedElementAddValue = selectedInspectedElementAddValue;
    }

    /**
     * @return the inspectedElementAddValueCandidateList
     */
    public List<OccInspectionStatusEnum> getInspectedElementAddValueCandidateList() {
        return inspectedElementAddValueCandidateList;
    }

    /**
     * @param inspectedElementAddValueCandidateList the inspectedElementAddValueCandidateList to set
     */
    public void setInspectedElementAddValueCandidateList(List<OccInspectionStatusEnum> inspectedElementAddValueCandidateList) {
        this.inspectedElementAddValueCandidateList = inspectedElementAddValueCandidateList;
    }



    /**
     * @return the filteredPaymentList
     */
    public List<MoneyOccPeriodFeePayment> getFilteredPaymentList() {
        return filteredPaymentList;
    }

    /**
     * @return the selectedPayment
     */
    public Payment getSelectedPayment() {
        return selectedPayment;
    }

    /**
     * @return the filteredFeeList
     */
    public List<MoneyOccPeriodFeeAssigned> getFilteredFeeList() {
        return filteredFeeList;
    }

    /**
     * @return the selectedFee
     */
    public MoneyOccPeriodFeeAssigned getSelectedFee() {
        return selectedFee;
    }

    public List<MoneyOccPeriodFeePayment> getPaymentList() {
        return paymentList;
    }

    public void setPaymentList(List<MoneyOccPeriodFeePayment> paymentList) {
        this.paymentList = paymentList;
    }
    
    /**
     * @param filteredPaymentList the filteredPaymentList to set
     */
    public void setFilteredPaymentList(List<MoneyOccPeriodFeePayment> filteredPaymentList) {
        this.filteredPaymentList = filteredPaymentList;
    }

    /**
     * @param selectedPayment the selectedPayment to set
     */
    public void setSelectedPayment(Payment selectedPayment) {
        this.selectedPayment = selectedPayment;
    }

    public List<MoneyOccPeriodFeeAssigned> getFeeList() {
        return feeList;
    }

    public void setFeeList(List<MoneyOccPeriodFeeAssigned> feeList) {
        this.feeList = feeList;
    }
    
    /**
     * @param filteredFeeList the filteredFeeList to set
     */
    public void setFilteredFeeList(List<MoneyOccPeriodFeeAssigned> filteredFeeList) {
        this.filteredFeeList = filteredFeeList;
    }

    /**
     * @param selectedFee the selectedFee to set
     */
    public void setSelectedFee(MoneyOccPeriodFeeAssigned selectedFee) {
        this.selectedFee = selectedFee;
    }

     
}
