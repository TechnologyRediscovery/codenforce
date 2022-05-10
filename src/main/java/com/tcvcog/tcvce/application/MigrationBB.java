/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.OccInspectionCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.CECasePropertyUnitHeavy;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.CodeViolationMigrationPathwayEnum;
import com.tcvcog.tcvce.entities.CodeViolationMigrationSettings;
import com.tcvcog.tcvce.entities.CodeViolationMigrationSourceEnum;
import com.tcvcog.tcvce.entities.DomainEnum;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.TransferrableEnum;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 * Backing bean tools for migrating failed inspection elements
 * to a ce case and violations from case to case
 * 
 * @author sylvia
 */
public class MigrationBB extends BackingBeanUtils{

    private List<CodeViolationMigrationPathwayEnum> transferOptionsListCaseViolation;
    private List<CodeViolationMigrationPathwayEnum> transferOptionsListCaseInspection;
    private List<CodeViolationMigrationPathwayEnum> transferOptionsListOccInspection;
    
    private CodeViolationMigrationPathwayEnum selectedTransferPathway;
    private CodeViolationMigrationSettings currentMigrationSettings;
    private List<CECasePropertyUnitHeavy> caseCandidateList;
    private List<EventCategory> caseOriginationEventCategoryCandidateList;
    
    private LocalDateTime batchUpdatedStipCompDate;
    
    /**
     * Creates a new instance of MigrationBB
     */
    public MigrationBB() {
    }
    
    /**
     * Sets up the bean, mostly by creating possible pathways based on the
     * source object of the violations
     */
    @PostConstruct
    public void initBean() {
        System.out.println("MigrationBB.InitBean");
        setTransferOptionsListCaseInspection(new ArrayList<>());
        getTransferOptionsListCaseInspection().add(CodeViolationMigrationPathwayEnum.CASE_INSPECTION_TO_EXISTING_CASE);
        getTransferOptionsListCaseInspection().add(CodeViolationMigrationPathwayEnum.CASE_INSPECTION_TO_HOST_CASE);
        getTransferOptionsListCaseInspection().add(CodeViolationMigrationPathwayEnum.CASE_INSPECTION_TO_NEW_CASE);
        
        
        setTransferOptionsListCaseViolation(new ArrayList<>());
        getTransferOptionsListCaseViolation().add(CodeViolationMigrationPathwayEnum.CASE_TO_EXISTING_CASE);
        getTransferOptionsListCaseViolation().add(CodeViolationMigrationPathwayEnum.CASE_TO_NEW_CASE);
        
        setTransferOptionsListOccInspection(new ArrayList<>());
        getTransferOptionsListOccInspection().add(CodeViolationMigrationPathwayEnum.OCC_INSPECTION_TO_EXISTING_CASE);
        getTransferOptionsListOccInspection().add(CodeViolationMigrationPathwayEnum.OCC_INSPECTION_TO_NEW_CASE);
        
        batchUpdatedStipCompDate = LocalDateTime.now().plusDays(30);
    }
    
    /**
     * Listener for user requests to view migration options
     * @param ev 
     */
    public void onMIgrationInitButtonChange(ActionEvent ev){
        System.out.println("MigrationBB.onOpenMigrationOptionsDialog");
    }
    
    /**
     * Listener for user requests to start the transfer process by selecting a pathway
     * @param ev 
     */
    public void onTransferPathwaySelectionCompleteButtonChange(ActionEvent ev){
       initMigrationSettings();
    }
    
    /**
     * Grabs the migration options skeleton and sets up all sorts of components
     * for the migration form UI
     * 
     */
    private void initMigrationSettings(){
    
       CaseCoordinator cc = getCaseCoordinator();
       EventCoordinator ec = getEventCoordinator();
       
       currentMigrationSettings = cc.violation_migration_getCodeViolationMigrationSettingsSkeleton();
       currentMigrationSettings.setProp(getSessionBean().getSessProperty());

       if(selectedTransferPathway != null){
            System.out.println("MigrationBB.initMigrationSettings | selected pathway: " + selectedTransferPathway.getDescription());
            currentMigrationSettings.setPathway(selectedTransferPathway);
       } else {
            System.out.println("MigrationBB.initMigrationSettings | null pathway" );
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "No pathway to build migration settings; Aborting!" , ""));
             return;
       }
       
       try{
            if(currentMigrationSettings.getPathway().getViolationSourceEnum() == CodeViolationMigrationSourceEnum.CECASE_VIOLATIONS){
                
                // ****************************************************************************
                // EXTRACT VIOLATIONS FROM CECASE FOR USE IN THE MIGRATION DIALOG
                // ****************************************************************************
                 currentMigrationSettings.setViolationListToMigrate(cc.violation_migration_assembleViolationsForMigrationFromCECase(getSessionBean().getSessCECase()));
            } else if(currentMigrationSettings.getPathway().getViolationSourceEnum() == CodeViolationMigrationSourceEnum.CECASE_INSPECTION
                    || currentMigrationSettings.getPathway().getViolationSourceEnum() == CodeViolationMigrationSourceEnum.OCCUPNACY_PERIOD_INSPECTION){
                
                // ****************************************************************************
                // EXTRACT VIOLATED ELEMENTS IN THE SESSION FIN FOR USE IN THE MIGRATION DIALOG
                // ****************************************************************************
                 currentMigrationSettings.setViolationListToMigrate(cc.violation_buildViolationListFromFailedInspectionItems(getSessionBean().getSessFieldInspection()));
            }
       } catch (BObStatusException | ViolationException ex){
           System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Could not assemble violation list; Aborting!" , ""));
       }
    
       caseCandidateList = cc.cecase_assembleListOfOpenCases(getSessionBean().getSessProperty().getCeCaseList(), getSessionBean().getSessCECase());

       if(currentMigrationSettings.getPathway().getDomain() == DomainEnum.CODE_ENFORCEMENT){
            caseOriginationEventCategoryCandidateList = ec.determinePermittedEventCategories(EventType.Origination, getSessionBean().getSessUser());
       } else {
            caseOriginationEventCategoryCandidateList = ec.determinePermittedEventCategories(EventType.Occupancy, getSessionBean().getSessUser());
       }
    }
    
    
    /**
     * Listener for user request to undertake the migration process
     * @param ev
     */
    public void onMigrationCommitButtonChange(ActionEvent ev){
        
        CaseCoordinator cc = getCaseCoordinator();
        if(currentMigrationSettings != null){
            currentMigrationSettings.setUserEnactingMigration(getSessionBean().getSessUser());
            if(currentMigrationSettings.getPathway() != null){
                if(currentMigrationSettings.getPathway().getViolationSourceEnum() == CodeViolationMigrationSourceEnum.CECASE_VIOLATIONS){
                    currentMigrationSettings.setSourceCase(getSessionBean().getSessCECase());
                    // turn me off to let the user select manager
//                    currentMigrationSettings.setNewCECaseManager(getSessionBean().getSessCECase().getManager());
                }
            }
                
            try {
                currentMigrationSettings = cc.violation_migration_migrateViolations(currentMigrationSettings);
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully migrated " + currentMigrationSettings.getViolationListSuccessfullyMigrated().size() + " violations!", ""));
            } catch (BObStatusException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage() , ""));
            } 
        }
    }
    
    
    /**
     * Listener for user requests to batch update the stip date
     * @param ev
    */
    public void onBatchUpdateStipCompDate(ActionEvent ev){
        if(batchUpdatedStipCompDate != null 
                && currentMigrationSettings != null 
                && currentMigrationSettings.getViolationListToMigrate()!= null
                && !currentMigrationSettings.getViolationListToMigrate().isEmpty()){
            for(CodeViolation cv: currentMigrationSettings.getViolationListToMigrate()){
               cv.setStipulatedComplianceDate(batchUpdatedStipCompDate);
                System.out.println("MigrationBB.onBatchUpdateStipCompDate: new stip date for CV ID " + cv.getViolationID() + " is " + cv.getStipulatedComplianceDate().toString());
            }
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Batch compliance date success!", ""));
            
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_FATAL, "Unable to batch update stipulated compliance date", ""));
            
        }
    }

    /**
     * Listener for user requests to remove a violation from the list
     * to migrate
     * @param cv 
     */
    public void onRemoveViolationFromMigrationList(CodeViolation cv){
        if(currentMigrationSettings != null 
                && currentMigrationSettings.getViolationListToMigrate() != null 
                && !currentMigrationSettings.getViolationListToMigrate().isEmpty()){
            if(cv != null){
                currentMigrationSettings.getViolationListToMigrate().remove(cv);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Removed violation ID " + cv.getViolationID(), ""));
            }
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fatal error removing violation" , ""));
            
        }
    }
    
    /**
     * Listener for user requests to go to the target case
     * @return page nav ID
     */
    public String onViewTargetCase(){
        if(currentMigrationSettings != null && currentMigrationSettings.getCeCaseParent() != null){
            getSessionBean().setSessCECase(currentMigrationSettings.getCeCaseParent());
            return "ceCaseProfile";
        } else {
            return "";
        }
        
    }
    
    
    
    
    // ***********************************************************************
    // ***************** GETTERS AND SETTERS *********************************
    // ***********************************************************************

    /**
     * @return the transferOptionsListCaseViolation
     */
    public List<CodeViolationMigrationPathwayEnum> getTransferOptionsListCaseViolation() {
        return transferOptionsListCaseViolation;
    }

    /**
     * @return the transferOptionsListCaseInspection
     */
    public List<CodeViolationMigrationPathwayEnum> getTransferOptionsListCaseInspection() {
        return transferOptionsListCaseInspection;
    }

    /**
     * @return the transferOptionsListOccInspection
     */
    public List<CodeViolationMigrationPathwayEnum> getTransferOptionsListOccInspection() {
        return transferOptionsListOccInspection;
    }

    /**
     * @return the selectedTransferPathway
     */
    public CodeViolationMigrationPathwayEnum getSelectedTransferPathway() {
        return selectedTransferPathway;
    }

    /**
     * @return the currentMigrationSettings
     */
    public CodeViolationMigrationSettings getCurrentMigrationSettings() {
        return currentMigrationSettings;
    }

    /**
     * @param transferOptionsListCaseViolation the transferOptionsListCaseViolation to set
     */
    public void setTransferOptionsListCaseViolation(List<CodeViolationMigrationPathwayEnum> transferOptionsListCaseViolation) {
        this.transferOptionsListCaseViolation = transferOptionsListCaseViolation;
    }

    /**
     * @param transferOptionsListCaseInspection the transferOptionsListCaseInspection to set
     */
    public void setTransferOptionsListCaseInspection(List<CodeViolationMigrationPathwayEnum> transferOptionsListCaseInspection) {
        this.transferOptionsListCaseInspection = transferOptionsListCaseInspection;
    }

    /**
     * @param transferOptionsListOccInspection the transferOptionsListOccInspection to set
     */
    public void setTransferOptionsListOccInspection(List<CodeViolationMigrationPathwayEnum> transferOptionsListOccInspection) {
        this.transferOptionsListOccInspection = transferOptionsListOccInspection;
    }

    /**
     * @param selectedTransferPathway the selectedTransferPathway to set
     */
    public void setSelectedTransferPathway(CodeViolationMigrationPathwayEnum selectedTransferPathway) {
        this.selectedTransferPathway = selectedTransferPathway;
    }

    /**
     * @param currentMigrationSettings the currentMigrationSettings to set
     */
    public void setCurrentMigrationSettings(CodeViolationMigrationSettings currentMigrationSettings) {
        this.currentMigrationSettings = currentMigrationSettings;
    }

    /**
     * @return the caseCandidateList
     */
    public List<CECasePropertyUnitHeavy> getCaseCandidateList() {
        return caseCandidateList;
    }

    /**
     * @param caseCandidateList the caseCandidateList to set
     */
    public void setCaseCandidateList(List<CECasePropertyUnitHeavy> caseCandidateList) {
        this.caseCandidateList = caseCandidateList;
    }

    /**
     * @return the caseOriginationEventCategoryCandidateList
     */
    public List<EventCategory> getCaseOriginationEventCategoryCandidateList() {
        return caseOriginationEventCategoryCandidateList;
    }

    /**
     * @param caseOriginationEventCategoryCandidateList the caseOriginationEventCategoryCandidateList to set
     */
    public void setCaseOriginationEventCategoryCandidateList(List<EventCategory> caseOriginationEventCategoryCandidateList) {
        this.caseOriginationEventCategoryCandidateList = caseOriginationEventCategoryCandidateList;
    }

    /**
     * @return the batchUpdatedStipCompDate
     */
    public LocalDateTime getBatchUpdatedStipCompDate() {
        return batchUpdatedStipCompDate;
    }

    /**
     * @param batchUpdatedStipCompDate the batchUpdatedStipCompDate to set
     */
    public void setBatchUpdatedStipCompDate(LocalDateTime batchUpdatedStipCompDate) {
        this.batchUpdatedStipCompDate = batchUpdatedStipCompDate;
    }
    
}
