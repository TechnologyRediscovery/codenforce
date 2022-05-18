/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.application.SessionBean;
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.entities.occupancy.FieldInspection;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodDataHeavy;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodPropertyUnitHeavy;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodType;
import com.tcvcog.tcvce.entities.occupancy.OccPermit;
import com.tcvcog.tcvce.entities.reports.ReportConfigOccPermit;
import com.tcvcog.tcvce.entities.search.QueryOccPeriod;
import com.tcvcog.tcvce.entities.search.SearchParamsOccPeriod;
import com.tcvcog.tcvce.integration.PropertyIntegrator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

/**
 * Backer for the OccPeriod
 * @author Ellen Bascomb, JURPLEL
 */
public class OccPeriodBB
        extends BackingBeanUtils {

    private OccPeriod lastSavedOccPeriod;
    private OccPeriodDataHeavy currentOccPeriod;
    private PropertyUnit currentPropertyUnit;
    
    private OccPermit currentOccPermit;
    private ReportConfigOccPermit currentOccPermitConfig;
    private boolean editModeOccPermit;

//  *******************************
//  ************ WORKFLOW**********
//  *******************************
    private List<OccPeriodType> occPeriodTypeList;
    private OccPeriodType selectedOccPeriodType;

    private List<PropertyUnit> propertyUnitCandidateList;
    private PropertyUnit selectedPropertyUnit;

    private List<User> managerInspectorCandidateList;
    private User selectedManager;



    /**
     * Creates a new instance of OccPeriodSearchWorkflowBB
     */
    public OccPeriodBB() {
    }

    /**
     * Assume a zen-like state and initialize this bean
     */
    @PostConstruct
    public void initBean() {
        System.out.printf("OccPeriodSearchWorkflowBB constructed");
        OccupancyCoordinator oc = getOccupancyCoordinator();
        SessionBean sb = getSessionBean();

        currentOccPeriod = sb.getSessOccPeriod();
        if(currentOccPeriod != null){
            occPeriodTypeList = sb.getSessMuni().getProfile().getOccPeriodTypeList();
            setLastSavedOccPeriod(new OccPeriod(currentOccPeriod));
            PropertyIntegrator pi = getPropertyIntegrator();

            setOccPeriodTypeList(sb.getSessMuni().getProfile().getOccPeriodTypeList());

            try {
                currentPropertyUnit = pi.getPropertyUnitWithProp(currentOccPeriod.getPropertyUnitID());
                setPropertyUnitCandidateList(sb.getSessProperty().getUnitList());
            } catch (IntegrationException | BObStatusException ex) {
                System.out.println(ex);
            }
        }
    }


    /**
     * Gets an updated instance of the current OccPeriod
     */
    public void reloadCurrentOccPeriodDataHeavy() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            currentOccPeriod = oc.assembleOccPeriodDataHeavy(currentOccPeriod, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Reloaded occ period ID " + currentOccPeriod.getPeriodID(), ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to reload occ period", ""));
        } catch (SearchException ex) {
            System.out.println(ex);
        }

    }



    /**
     * Listener for user requests to certify a field on the OccPeriod
     */
    public void certifyOccPeriodField() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        FacesContext context = getFacesContext();
        String field = context.getExternalContext().getRequestParameterMap().get("fieldtocertify");

        System.out.println("occPeriodBB.certifyOccPeriodField | field: " + field);

        UserAuthorized u = getSessionBean().getSessUser();
        LocalDateTime now = LocalDateTime.now();

        // Set actual values
        switch (field) {
            case "occperiodtype":
                if (currentOccPeriod.getPeriodTypeCertifiedBy() == null) {
                    currentOccPeriod.setPeriodTypeCertifiedBy(u);
                    currentOccPeriod.setPeriodTypeCertifiedTS(now);
                } else {
                    currentOccPeriod.setPeriodTypeCertifiedBy(null);
                    currentOccPeriod.setPeriodTypeCertifiedTS(null);
                }
                break;
            case "startdate":
                if (currentOccPeriod.getStartDateCertifiedBy() == null) {
                    currentOccPeriod.setStartDateCertifiedBy(u);
                    currentOccPeriod.setStartDateCertifiedTS(now);
                } else {
                    currentOccPeriod.setStartDateCertifiedBy(null);
                    currentOccPeriod.setStartDateCertifiedTS(null);
                }
                break;
            case "enddate":
                if (currentOccPeriod.getEndDateCertifiedBy() == null) {
                    currentOccPeriod.setEndDateCertifiedBy(u);
                    currentOccPeriod.setEndDateCertifiedTS(now);
                } else {
                    currentOccPeriod.setEndDateCertifiedBy(null);
                    currentOccPeriod.setEndDateCertifiedTS(null);
                }
                break;
            default:
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error! Unable to certify field", ""));
        }

        saveOccPeriodChanges();
    }


    /**
     * Listener for the requests to authorize or unauthorize the OccPeriod
     */
    public void toggleOccPeriodAuthorization() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.toggleOccPeriodAuthorization(currentOccPeriod, getSessionBean().getSessUser());
            if (currentOccPeriod.getAuthorizedBy() != null) {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Success! Occupancy period ID " + currentOccPeriod.getPeriodID()
                                + " is now authorized and permits can be generated.", ""));
            } else {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Occupancy period ID " + currentOccPeriod.getPeriodID()
                                + " has been successfully deauthorized.", ""));
            }
        } catch (AuthorizationException | BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.getMessage(), ""));
        }
    }


    /**
     * utility pass through method to be called when loading Occperiod advanced settings
     *
     */
    public void updateOccPeriodInitialize() {

    }

    /**
     * This method attempts to update the database entry for the currentOccPeriod.
     * It will fail in certain conditions, in which case the currentOccPeriod is returned to
     * a backup made before any current unsaved changes.
     */
    public void saveOccPeriodChanges() {
        OccupancyCoordinator oc = getOccupancyCoordinator();

        try {
            oc.editOccPeriod(currentOccPeriod, getSessionBean().getSessUser());
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Save successful on OccPeriod ID: " + currentOccPeriod.getPeriodID(), ""));
            System.out.println("occPeriodBB.saveOccPeriodChanges successful");

            // Set backup copy in case of failure if saving to database succeeds
            setLastSavedOccPeriod(new OccPeriod(currentOccPeriod));
        } catch (IntegrationException | BObStatusException ex) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.getMessage(), ""));
            System.out.println("occPeriodBB.saveOccPeriodChanges failure");

            // Restore working copy of occ period to last working one if saving to database fails.
            discardOccPeriodChanges();
        }
    }

    /**
     * This method will discard the changes to the current working occ period and
     * set its value to that of the occ period present in the last successful save.
     */
    public void discardOccPeriodChanges() {
        System.out.println("occPeriodBB.discardOccPeriodChanges");

        currentOccPeriod = new OccPeriodDataHeavy(getLastSavedOccPeriod());
    }

    /**
     * Listener for user request to map the current occ period to a a new property
     * unit
     */
    public void updatePeriodPropUnit() {
//        OccupancyCoordinator oc = getOccupancyCoordinator();
//        try {
//            oc.updateOccPeriodPropUnit(currentOccPeriod, getSelectedPropertyUnit());
//             getFacesContext().addMessage(null,
//                new FacesMessage(FacesMessage.SEVERITY_INFO,
//                "The current occupancy period has been assigned to property unit ID " + getSelectedPropertyUnit().getUnitID(), ""));
//        } catch (IntegrationException ex) {
//            System.out.println(ex);
//             getFacesContext().addMessage(null,
//                new FacesMessage(FacesMessage.SEVERITY_ERROR,
//                ex.getMessage(), ""));
//        }
//        reloadCurrentOccPeriodDataHeavy();
    }

    /**
     * Listener for requests to go view the property profile of a property associated
     * with the given case
     * largely copied from CECaseSearchProfileBB (Maybe this should be in BackingBeanUtils?)
     *
     * @return
     */
    public String exploreProperty() {
        // TODO: update for humanziation
//            getSessionBean().setSessProperty(currentPropertyUnit.);
        return "propertyInfo";

    }

    /**
     * Listener for requests to reload the current OccPeriodDataHeavy
     * Concept copied from CECaseSearchProfile
     *
     * @return
     */
    public String refreshCurrentPeriod() {
        return "occPeriodWorkflow";

    }
    
    


    /**
     * Loads an OccPeriodDataHeavy and injects it into the session bean
     * and sends the user to the Workflow/status page
     *
     * @param op
     * @return
     */
    public String exploreOccPeriod(OccPeriod op) {
        SystemCoordinator sc = getSystemCoordinator();
        PropertyCoordinator pc = getPropertyCoordinator();
        OccupancyCoordinator oc = getOccupancyCoordinator();
        Credential cred = getSessionBean().getSessUser().getMyCredential();

        try {
            getSessionBean().setSessOccPeriod(oc.assembleOccPeriodDataHeavy(op, getSessionBean().getSessUser()));
            getSessionBean().setSessProperty(pc.getPropertyDataHeavyByUnit(op.getPropertyUnitID(), getSessionBean().getSessUser()));
            sc.logObjectView(getSessionBean().getSessUser(), op);
        } catch (IntegrationException | BObStatusException | AuthorizationException | EventException | SearchException | BlobException  ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to assemble the data-rich occ period", ""));

        }

        return "occPeriodWorkflow";
    }
    
    

   
    
    /**
     * Listener for user requests to view or add blobs to this period
     * @param ev
     */
    public void manageBlobsOnOccPeriod(ActionEvent ev){
        try {
            getSessionBean().setAndRefreshSessionBlobHolderAndBuildUpstreamPool(currentOccPeriod);
        } catch (BObStatusException | BlobException | IntegrationException ex) {
            System.out.println(ex);
            
        } 
        
    }
    
    /**
     * Special getter that asks the session for its refreshed list
     * and injects into the current occ period for viewing
     * @return 
     */
    public List<FieldInspection> getManagedFieldInspectionList(){
        List<FieldInspection> filist = getSessionBean().getSessFieldInspectionListForRefresh();
        if(currentOccPeriod != null){
        System.out.println("OccPeriodBB.getManagedFieldInspectionList() | session filist: " + filist );
            
            if(filist != null && !filist.isEmpty()){
                System.out.println("OccPeriodBB.getManagedFieldInspectionList() | session filist size: " + filist.size());
                currentOccPeriod.setInspectionList(filist);
                getSessionBean().setSessFieldInspectionListForRefresh(null);
                return currentOccPeriod.getInspectionList();
            } else {
                return currentOccPeriod.getInspectionList();
            }
        } else {
            return new ArrayList<>();
        }
    }
    
    
    
    
    // *************************************************************************
    // ********************* OCC PERMITS   ************************************
    // *************************************************************************
    
    
    /**
     * Listener for user requests to start a new occ permit
     * @param ev 
     */
    public void onOccPermitInitButtonChange(ActionEvent ev){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        currentOccPermit = oc.getOccPermitSkeleton(getSessionBean().getSessUser());
    }
    
    /**
     * Listener to commit the new permit process
     * @param ev 
     */
    public void onOccPermitInitCommitButtonChange(ActionEvent ev){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.insertOccPermit(currentOccPermit, getSessionBean().getSessUser());
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
              getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        } 
    }
    
    /**
     * Listener for user requests to view the config fields on an occ permit
     * @param permit 
     */
    public void onOccpermitConfigLinkClick(OccPermit permit){
        currentOccPermit = permit;
    }
    
    /**
     * Begins finalization process
     * @param ev 
     */
    public void onOccPermitFinalizeInitButtonChange(ActionEvent ev){
        System.out.println("OccPeriodBB.OccPermitFinalizeInitButtonChange");
    }
    
    /**
     * Sends the current occ period and permit with all the goodies injected to the 
     * coordinator for static String extraction
     * @param ev 
     */
    public void onOccPermitGenerateStaticFields(ActionEvent ev){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.updateOccPermitStaticFields(currentOccPeriod, currentOccPermit, getSessionBean().getSessUser(), getSessionBean().getSessMuni());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Finalized Occ permit No " + currentOccPermit.getReferenceNo(), ""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
              getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        } 
        
        
        
    }
    
    /**
     * Writes finalization fields to DB on an occ permit
     * @param ev 
     */
    public void onOccpermitFinalizeCommitButtonChange(ActionEvent ev){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.occPermitFinalize(currentOccPermit, getSessionBean().getSessUser(),getSessionBean().getSessMuni());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Finalized Occ permit No " + currentOccPermit.getReferenceNo(), ""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
              getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        } 
    }
    
    /**
     * Starts the deactivation process
     * @param op 
     */
    public void onOccPermitDeactivateInit(OccPermit op){
        System.out.println("OccPeriodBB.onOccPermitDeactivateInit");
        
    }
    
    /**
     * concludes the deactivation process
     * @param op 
     */
    public void onOccPermitDeactivateCommit(OccPermit op){
         OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            System.out.println("OccPeriodBB.onOccPermitDeactivateCommit");
            oc.occPermitDeactivate(currentOccPermit, getSessionBean().getSessUser());
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
              getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        } 
    }
    
    /**
     * Redirects user to page to print occ permit
     * @param op
     * @return 
     */
    public String onOccPermitPrintLinkClick(OccPermit op){
        
        
        return "occPermit";
        
    }

    
    
    
    // *************************************************************************
    // ********************* GETTERS AND SETTERS *******************************
    // *************************************************************************
    

   
    /**
     * @return the currentOccPeriod
     */
    public OccPeriodDataHeavy getCurrentOccPeriod() {
        return currentOccPeriod;
    }

    /**
     * @param currentOccPeriod the currentOccPeriod to set
     */
    public void setCurrentOccPeriod(OccPeriodDataHeavy currentOccPeriod) {
        this.currentOccPeriod = currentOccPeriod;
    }

    /**
     * @return the currentPropertyUnit
     */
    public PropertyUnit getCurrentPropertyUnit() {
        return currentPropertyUnit;
    }

   
    /**
     * @return the lastSavedOccPeriod
     */
    public OccPeriod getLastSavedOccPeriod() {
        return lastSavedOccPeriod;
    }

    /**
     * @return the occPeriodTypeList
     */
    public List<OccPeriodType> getOccPeriodTypeList() {
        return occPeriodTypeList;
    }

    /**
     * @return the selectedPropertyUnit
     */
    public PropertyUnit getSelectedPropertyUnit() {
        return selectedPropertyUnit;
    }

    /**
     * @return the managerInspectorCandidateList
     */
    public List<User> getManagerInspectorCandidateList() {
        return managerInspectorCandidateList;
    }

    /**
     * @return the selectedManager
     */
    public User getSelectedManager() {
        return selectedManager;
    }

    /**
     * @param lastSavedOccPeriod the lastSavedOccPeriod to set
     */
    public void setLastSavedOccPeriod(OccPeriod lastSavedOccPeriod) {
        this.lastSavedOccPeriod = lastSavedOccPeriod;
    }

    /**
     * @param occPeriodTypeList the occPeriodTypeList to set
     */
    public void setOccPeriodTypeList(List<OccPeriodType> occPeriodTypeList) {
        this.occPeriodTypeList = occPeriodTypeList;
    }

    /**
     * @param selectedPropertyUnit the selectedPropertyUnit to set
     */
    public void setSelectedPropertyUnit(PropertyUnit selectedPropertyUnit) {
        this.selectedPropertyUnit = selectedPropertyUnit;
    }

    /**
     * @param managerInspectorCandidateList the managerInspectorCandidateList to set
     */
    public void setManagerInspectorCandidateList(List<User> managerInspectorCandidateList) {
        this.managerInspectorCandidateList = managerInspectorCandidateList;
    }

    /**
     * @param selectedManager the selectedManager to set
     */
    public void setSelectedManager(User selectedManager) {
        this.selectedManager = selectedManager;
    }

    /**
     * @return the selectedOccPeriodType
     */
    public OccPeriodType getSelectedOccPeriodType() {
        return selectedOccPeriodType;
    }

    /**
     * @return the propertyUnitCandidateList
     */
    public List<PropertyUnit> getPropertyUnitCandidateList() {
        return propertyUnitCandidateList;
    }

    /**
     * @param selectedOccPeriodType the selectedOccPeriodType to set
     */
    public void setSelectedOccPeriodType(OccPeriodType selectedOccPeriodType) {
        this.selectedOccPeriodType = selectedOccPeriodType;
    }

    /**
     * @param propertyUnitCandidateList the propertyUnitCandidateList to set
     */
    public void setPropertyUnitCandidateList(List<PropertyUnit> propertyUnitCandidateList) {
        this.propertyUnitCandidateList = propertyUnitCandidateList;
    }

    /**
     * @return the currentOccPermit
     */
    public OccPermit getCurrentOccPermit() {
        return currentOccPermit;
    }

    /**
     * @return the currentOccPermitConfig
     */
    public ReportConfigOccPermit getCurrentOccPermitConfig() {
        return currentOccPermitConfig;
    }

    /**
     * @return the editModeOccPermit
     */
    public boolean isEditModeOccPermit() {
        return editModeOccPermit;
    }

    /**
     * @param currentOccPermit the currentOccPermit to set
     */
    public void setCurrentOccPermit(OccPermit currentOccPermit) {
        this.currentOccPermit = currentOccPermit;
    }

    /**
     * @param currentOccPermitConfig the currentOccPermitConfig to set
     */
    public void setCurrentOccPermitConfig(ReportConfigOccPermit currentOccPermitConfig) {
        this.currentOccPermitConfig = currentOccPermitConfig;
    }

    /**
     * @param editModeOccPermit the editModeOccPermit to set
     */
    public void setEditModeOccPermit(boolean editModeOccPermit) {
        this.editModeOccPermit = editModeOccPermit;
    }

   

}
