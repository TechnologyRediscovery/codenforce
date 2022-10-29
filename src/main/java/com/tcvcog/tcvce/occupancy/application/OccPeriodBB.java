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
import com.tcvcog.tcvce.session.SessionBean;
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
import com.tcvcog.tcvce.entities.occupancy.OccPermitType;
import com.tcvcog.tcvce.entities.occupancy.OccPermit;
import com.tcvcog.tcvce.entities.reports.ReportConfigOccPermit;
import com.tcvcog.tcvce.entities.search.QueryOccPeriod;
import com.tcvcog.tcvce.entities.search.SearchParamsOccPeriod;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.util.Constants;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;

/**
 * Backer for the OccPeriod
 * @author Ellen Bascomb, JURPLEL
 */
public class OccPeriodBB
        extends BackingBeanUtils {

    
    final static String PERMIT_BLOCKS_STIPULATIONS = "textblock_cat_permit_stipulations";
    final static String PERMIT_BLOCKS_NOTICES = "textblock_cat_permit_notices";
    final static String PERMIT_BLOCKS_COMMENTS = "textblock_cat_permit_comments";
    
    final static String PERMIT_BLOCK_CATEGORY_PARAM_KEY = "permit-block-cat";
    final static String PERMIT_PERSON_LIST_PARAM_KEY = "permit-person-list";
    
    private OccPeriod lastSavedOccPeriod;
    private OccPeriodDataHeavy currentOccPeriod;
    private PropertyUnitDataHeavy currentPropertyUnit;
    
    private List<OccPermitType> permitTypeCandidateList;
    
    private OccPermit currentOccPermit;
    private ReportConfigOccPermit currentOccPermitConfig;
    private boolean editModeOccPermit;
    private PropertyDataHeavy currentPropertyDH;
    
    private List<TextBlock> permitBlockCandidatesStipulations;
    private List<TextBlock> permitBlockCandidatesNotices;
    private List<TextBlock> permitBlockCandidatesComments;
    private List<TextBlock> permitBlocksActiveCandidateList;
    private List<TextBlock> permitBlocksSelectedList;
    private List<TextBlock> permitBlocksFilteredList;
    private TextBlock currentTextBlock;
    private TextBlockPermitFieldEnum currentTextBlockPermitFieldEnum;
    
    private List<HumanLink> occPermitCandidateHumanLinkList;
    private List<HumanLink> occPermitSelectedHumanLinkList;
    private HumanLink occPermitSelectedHumanLink;
    private OccPermitPersonListEnum occPermitCurrentPersonLinkEnum;
    
//  *******************************
//  ************ WORKFLOW**********
//  *******************************
    private List<OccPermitType> occPeriodTypeList;
    private OccPermitType selectedOccPeriodType;

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
        PropertyCoordinator pc = getPropertyCoordinator();
        // setup event view
        getSessionEventConductor().setSessEventsPageEventDomainRequest(DomainEnum.OCCUPANCY);
        
        getSessionBean().setSessHumanListRefreshedList(null);
        currentOccPeriod = sb.getSessOccPeriod();
        if(currentOccPeriod != null){
            occPeriodTypeList = sb.getSessMuni().getProfile().getOccPermitTypeList();
            setLastSavedOccPeriod(new OccPeriod(currentOccPeriod));

            setOccPeriodTypeList(sb.getSessMuni().getProfile().getOccPermitTypeList());

            try {
                currentPropertyUnit = pc.getPropertyUnitDataHeavy(currentOccPeriod.getPropUnitProp(), getSessionBean().getSessUser());
                currentPropertyDH = pc.getPropertyDataHeavy(currentPropertyUnit.getParcelKey(), getSessionBean().getSessUser());
                setPropertyUnitCandidateList(sb.getSessProperty().getUnitList());
                
                setupPermitTextBlockCandidateLists(null);
                permitTypeCandidateList = oc.getOccPeriodTypesFromProfileID(getSessionBean().getSessMuni().getProfile());
            } catch (IntegrationException | BObStatusException | AuthorizationException | EventException | SearchException | BlobException ex) {
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

        saveOccPeriodChanges(null);
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
     * @param ev
     */
    public void saveOccPeriodChanges(ActionEvent ev) {
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
     * @param eveventList
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
                reloadCurrentOccPeriodDataHeavy();
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
     * Listener for user requests to be done selecting persons. Since all the other
     * operations are AJAXED, we don't need to do anything
     * @param ev 
     */
    public void onOccPermitDoneWithPersonsSelection(ActionEvent ev){
        System.out.println("OccPeriodBB.onOccPermitDoneWithPersonsSelection");
        getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Done with person assignments!", ""));
    }
    
    /**
     * Writes a skeleton occ permit into the db
     * @param ev 
     */
    public void onOccPermitInitCommitButtonChange(ActionEvent ev){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        
        int freshPermitID;
        try {
        freshPermitID = oc.insertOccPermit(currentOccPermit, currentOccPeriod, getSessionBean().getSessUser());
        currentOccPermit = oc.getOccPermit(freshPermitID, getSessionBean().getSessUser());
        configureCurrentOccPermitForOfficerReview();
        
        System.out.println("OccPeriodBB.onOccPermitInitCommitButtonChange | current occ permit ID: " + currentOccPermit.getPermitID());
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
              getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        } 
        
        
    }
    
    /**
     * Grabs a new copy of the curent occ permit, if it's ID is not zero
     * @param ev 
     */
    public void refreshCurrentOccPermit(ActionEvent ev){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        if(currentOccPermit != null){
            try {
                currentOccPermit = oc.getOccPermit(currentOccPermit.getPermitID(), getSessionBean().getSessUser());
            } catch (IntegrationException | BObStatusException ex) {
                System.out.println(ex);
              getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
                
            } 
        }
        
        
    }
    
    /**
     * Internal organ for asking the coordinator to make sensible
     * occ permit injections such as dates of various inspections
     * and human links if they can be deciphered.
     * 
     */
    private void configureCurrentOccPermitForOfficerReview(){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        // SHIP all we have to the coordinator for logic check and AUDIT
        // for PERMIT ISSUANCE CLEARANCE CLARENCE, WHAT?
        try {
            oc.occPermitAssignSensibleDynamicValuesAndAudit(currentOccPermit,
                    currentOccPeriod,
                    getSessionBean().getSessUser(),
                    currentPropertyDH);
        } catch (BObStatusException ex) {
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
    public void onOccpermitViewConfigLinkClick(OccPermit permit){
        currentOccPermit = permit;
        configureCurrentOccPermitForOfficerReview();
    }
    
    /**
     * Sends the current occ period and permit with all the goodies injected to the 
     * coordinator for static String extraction
     * @param ev 
     */
    public void onOccPermitGenerateStaticFields(ActionEvent ev){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.occPermitPopulateStaticFieldsFromDynamicFields(currentOccPeriod, currentOccPermit, getSessionBean().getSessUser(), getSessionBean().getSessMuni());
            refreshCurrentOccPermit(null);
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
     * Begins finalization process
     * @param ev 
     */
    public void onOccPermitAuditForFinalization(ActionEvent ev) {
        System.out.println("OccPeriodBB.onOccPermitAuditForFinalization");
        OccupancyCoordinator oc = getOccupancyCoordinator();
        refreshCurrentOccPermit(null);
        try {
            if(oc.occPermitAuditForFinalization(currentOccPermit, getSessionBean().getSessUser())){
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "This permit PASSED and is ready for finalization", ""));
                
            } else {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "This permit did NOT pass audit!", ""));
                
            }
        } catch (BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
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
            refreshCurrentOccPermit(null);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Finalized Occ permit ref Number: " + currentOccPermit.getReferenceNo(), ""));
            reloadCurrentOccPeriodDataHeavy();
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
    public void onOccPermitFinalizeOverride(ActionEvent ev){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.occPermitFinalizeOverrideAudit(currentOccPermit, getSessionBean().getSessUser(),getSessionBean().getSessMuni());
            refreshCurrentOccPermit(null);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Finalized Occ permit No " + currentOccPermit.getReferenceNo(), ""));
            reloadCurrentOccPeriodDataHeavy();
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
              getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        } 
    }
    
    /**
     * Starts the deactivation process
     * @param ev 
     */
    public void onOccPermitDeactivateInit(ActionEvent ev){
        System.out.println("OccPeriodBB.onOccPermitDeactivateInit");
        
    }
    
        /**
     * Listener for user requests to deactivate the current non-finalized occ permit
     * @param ev 
     */
    public void onOccpermitDeactivateCommit(ActionEvent ev){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        if(currentOccPermit != null){
            currentOccPermit.setDeactivatedBy(getSessionBean().getSessUser());
            currentOccPermit.setDeactivatedTS(LocalDateTime.now());
            try {
                oc.updateOccPermit(currentOccPermit, getSessionBean().getSessUser());
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                           "Occ Permit ID " + currentOccPermit.getPermitID() + " has been deactivated! ", ""));
                refreshCurrentOccPermit(null);
                reloadCurrentOccPeriodDataHeavy();
            } catch (BObStatusException | IntegrationException ex) {
                System.out.println(ex);
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
                
            } 
        }
    }
    
    /**
     * Redirects user to page to print occ permit
     * @param op
     * @return 
     */
    public String onOccPermitPrintLinkClick(OccPermit op){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        currentOccPermit = op;
        currentOccPermitConfig = oc.getOccPermitReportConfigDefault(currentOccPermit, currentOccPeriod, currentPropertyUnit, getSessionBean().getSessUser());
        getSessionBean().setReportConfigOccPermit(currentOccPermitConfig);
        return "occPermit";
    }

     /**
     * Internal organ for loading candidate text blocks for occ permit
     * generation
     */
    private void setupPermitTextBlockCandidateLists(Municipality m) throws IntegrationException{
        SystemCoordinator sc = getSystemCoordinator();
        try{
            
            TextBlockCategory tbc = sc.getTextBlockCategory(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString(PERMIT_BLOCKS_STIPULATIONS)));
            permitBlockCandidatesStipulations = sc.getTextBlockList(tbc, m);

            tbc = sc.getTextBlockCategory(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString(PERMIT_BLOCKS_NOTICES)));
            permitBlockCandidatesNotices = sc.getTextBlockList(tbc, m);

            tbc = sc.getTextBlockCategory(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString(PERMIT_BLOCKS_COMMENTS)));
            permitBlockCandidatesComments = sc.getTextBlockList(tbc, m);
        } catch (BObStatusException | IntegrationException ex){
            System.out.println(ex);
        }
        
        permitBlocksSelectedList = new ArrayList<>();
        permitBlocksFilteredList = new ArrayList<>();
    }
    
    
    
    /**
     * Listener for user requests to look at an individual text block for a permit
     * @param tb 
     */
    public void onOccPermitViewTextBlock(TextBlock tb){
        currentTextBlock = tb;
    }
    
    /**
     * Listener for user requests to remove a text block from one of the three
     * fields that can hold the contents of a text bock.
     * This method asks the request param for the value of the key: permit-block-cat
     * @param tb 
     */
    public void onOccPermitRemoveTextBlockFromQueue(TextBlock tb){
        
        String cat = getFacesContext().getExternalContext().getRequestParameterMap().get(PERMIT_BLOCK_CATEGORY_PARAM_KEY);
        currentTextBlockPermitFieldEnum = TextBlockPermitFieldEnum.valueOf(cat);
        System.out.println("OccPeriodBB.onOccPermitRemoveTextBlockFromQueue | block enum selected: " + currentTextBlockPermitFieldEnum.getLabel());
         switch(currentTextBlockPermitFieldEnum){
            case STIPULATIONS:
                currentOccPermit.getTextBlocks_stipulations().remove(tb);
                break;
            case NOTICES:
                currentOccPermit.getTextBlocks_notice().remove(tb);
                break;
            case COMMENTS:
                currentOccPermit.getTextBlocks_comments().remove(tb);
                break;
        }
        
    }
    
    /**
     * Listener for user requests to start the block choice process
     * This method asks the request param for the value of the key: permit-block-cat
     * which it then uses to figure out which candidate block list to load into the
     * primary active list. 
     * @param ev 
     */
    public void onOccPermitExtractBlockListEnumAndConfigureCandidatesLinkClick(ActionEvent ev){
        String cat = getFacesContext().getExternalContext().getRequestParameterMap().get(PERMIT_BLOCK_CATEGORY_PARAM_KEY);
        currentTextBlockPermitFieldEnum = TextBlockPermitFieldEnum.valueOf(cat);
        System.out.println("OccPeriodBB.onOccPermitChooseBlocksForPermitLinkClick | block enum selected: " + currentTextBlockPermitFieldEnum.getLabel());
        if(permitBlocksActiveCandidateList == null){
            permitBlocksActiveCandidateList = new ArrayList<>();
        }
        permitBlocksActiveCandidateList.clear();
        switch(currentTextBlockPermitFieldEnum){
            case STIPULATIONS:
                if(permitBlockCandidatesStipulations != null && !permitBlockCandidatesStipulations.isEmpty()){
                    permitBlocksActiveCandidateList.addAll(permitBlockCandidatesStipulations);
                }
                break;
            case NOTICES:
                if(permitBlockCandidatesNotices != null && !permitBlockCandidatesNotices.isEmpty()){
                    permitBlocksActiveCandidateList.addAll(permitBlockCandidatesNotices);
                }
                break;
            case COMMENTS:
                if(permitBlockCandidatesComments != null && !permitBlockCandidatesComments.isEmpty()){                    
                    permitBlocksActiveCandidateList.addAll(permitBlockCandidatesComments);
                }
                break;
        }
    }
    
    /**
     * Listener for user requests to add the selected blocks to the correct permit field.
     * This method uses the value of the bean's currentTextBlockPermitFieldEnum
     * to figure out which list to add the selected ones to
     * @param ev 
     */
    public void onOccPermitAddSelectedBlocksToPermit(ActionEvent ev){
        System.out.println("OccPeriodBB.onOccPermitAddSelectedBlocksToPermit | permitBlocksSelectedList: " + permitBlocksSelectedList.size());
        if(currentOccPermit != null){

            switch(currentTextBlockPermitFieldEnum){
                case STIPULATIONS:
                    if(currentOccPermit.getTextBlocks_stipulations() == null){
                        currentOccPermit.setTextBlocks_stipulations(new ArrayList<>());
                    }
                    currentOccPermit.getTextBlocks_stipulations().addAll(permitBlocksSelectedList);
                    break;
                case NOTICES:
                    if(currentOccPermit.getTextBlocks_notice()== null){
                        currentOccPermit.setTextBlocks_notice(new ArrayList<>());
                    }
                    currentOccPermit.getTextBlocks_notice().addAll(permitBlocksSelectedList);
                    break;
                case COMMENTS:
                    
                    if(currentOccPermit.getTextBlocks_comments()== null){
                        currentOccPermit.setTextBlocks_comments(new ArrayList<>());
                    }
                    currentOccPermit.getTextBlocks_comments().addAll(permitBlocksSelectedList);
                    break;
            }
             getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Added " + permitBlocksSelectedList.size() + " text blocks to occ permit text field: " + currentTextBlockPermitFieldEnum.getLabel(), ""));
            permitBlocksSelectedList.clear();
        }
    }
    
    /**
     * Listener for user requests to start the process of picking people to link
     * to the current permit's person fields
     * @param ev 
     */
    public void onOccPermitPersonLinkingInitButtonChange(ActionEvent ev){
        String cat = getFacesContext().getExternalContext().getRequestParameterMap().get(PERMIT_PERSON_LIST_PARAM_KEY);
        occPermitCurrentPersonLinkEnum = OccPermitPersonListEnum.valueOf(cat);
        System.out.println("OccPeriodBB.onOccPermitPersonLinkingInitButtonChange | selected person link enum: " + occPermitCurrentPersonLinkEnum.getLabel());
        configureOccPermitSelectedHumanLinkList(occPermitCurrentPersonLinkEnum);
        
        occPermitCandidateHumanLinkList = new ArrayList<>();
        if(currentOccPeriod.getHumanLinkList() != null && !currentOccPeriod.getHumanLinkList().isEmpty()){
            occPermitCandidateHumanLinkList.addAll(currentOccPeriod.getHumanLinkList());
        }
        if(currentPropertyDH != null && currentPropertyDH.getHumanLinkList() != null && !currentPropertyDH.getHumanLinkList().isEmpty()){
            occPermitCandidateHumanLinkList.addAll(currentPropertyDH.getHumanLinkList());
        }
        
        if(currentPropertyUnit != null && currentPropertyUnit.getHumanLinkList() != null && !currentPropertyUnit.getHumanLinkList().isEmpty()){
            occPermitCandidateHumanLinkList.addAll(currentPropertyUnit.getHumanLinkList());
        }
        
        System.out.println("OccPeriodBB.onOccPermitPersonLinkingInitButtonChange | person candidate size " + occPermitCandidateHumanLinkList.size());
        
    }
    
    /**
     * Utility for making the single queued list reflect the chosen occ permit human list
     * @param opple 
     */
    private void configureOccPermitSelectedHumanLinkList(OccPermitPersonListEnum opple){
        
        if(opple != null){
            occPermitCurrentPersonLinkEnum = opple;
        }
        
        occPermitSelectedHumanLinkList = new ArrayList<>();
        occPermitSelectedHumanLinkList.clear();
        
        if(occPermitCurrentPersonLinkEnum != null){
            switch(occPermitCurrentPersonLinkEnum){
                case CURRENT_OWNER:
                    if(currentOccPermit.getOwnerSellerLinkList() != null && !currentOccPermit.getOwnerSellerLinkList().isEmpty()){
                        occPermitSelectedHumanLinkList.addAll(currentOccPermit.getOwnerSellerLinkList());
                    }
                    break;
                case MANAGERS:
                    if(currentOccPermit.getManagerLinkList() != null && !currentOccPermit.getManagerLinkList().isEmpty()){
                        occPermitSelectedHumanLinkList.addAll(currentOccPermit.getManagerLinkList());
                    }
                    break;
                case NEW_OWNER:
                    if(currentOccPermit.getBuyerTenantLinkList()!= null && !currentOccPermit.getBuyerTenantLinkList().isEmpty()){
                        occPermitSelectedHumanLinkList.addAll(currentOccPermit.getBuyerTenantLinkList());
                        
                    }
                    break;
                case TENANTS:
                    if(currentOccPermit.getTenantLinkList() != null && !currentOccPermit.getTenantLinkList().isEmpty()){
                        occPermitSelectedHumanLinkList.addAll(currentOccPermit.getTenantLinkList());
                    }
                    break;
            }
        }
    }
    
    
    /**
     * Listener to switch between the four person link lists on the permit
     * @param ev 
     */
    public void onOCcPermitPersonLinkListChooseLinkClick(ActionEvent ev){
        String cat = getFacesContext().getExternalContext().getRequestParameterMap().get(PERMIT_PERSON_LIST_PARAM_KEY);
        occPermitCurrentPersonLinkEnum = OccPermitPersonListEnum.valueOf(cat);
        System.out.println("OccPeriodBB.onOCcPermitPersonLinkListChooseLinkClick | selected person link enum: " + occPermitCurrentPersonLinkEnum.getLabel());
        configureOccPermitSelectedHumanLinkList(occPermitCurrentPersonLinkEnum);
    }
    
    /**
     * Listener for user requests to queue their chosen person to the
     * bean's currently selected person link list represented by the enum
     * value 
     * @param hl 
     */
    public void onOccPermitPersonLinkQueuePerson(HumanLink hl){
        System.out.println("OccPeriodBB.onOccPermitPersonLinkQueuePerson | queuing Person ID  " + hl.getHumanID());
        System.out.println("OccPeriodBB.onOccPermitPersonLinkQueuePerson | currentPersonLinkEnum  " + occPermitCurrentPersonLinkEnum.getLabel());
        if(currentOccPermit != null){

            switch(occPermitCurrentPersonLinkEnum){
                    case CURRENT_OWNER:
                        if(currentOccPermit.getOwnerSellerLinkList() == null){
                            currentOccPermit.setOwnerSellerLinkList(new ArrayList<>());
                        }
                        currentOccPermit.getOwnerSellerLinkList().add(hl);
                        break;
                    case MANAGERS:
                        if(currentOccPermit.getManagerLinkList()== null){
                            currentOccPermit.setManagerLinkList(new ArrayList<>());
                        }
                        currentOccPermit.getManagerLinkList().add(hl);
                        break;
                    case NEW_OWNER:
                        if(currentOccPermit.getBuyerTenantLinkList()== null){
                            currentOccPermit.setBuyerTenantLinkList(new ArrayList<>());
                        }
                        currentOccPermit.getBuyerTenantLinkList().add(hl);
                        break;
                    case TENANTS:
                        if(currentOccPermit.getTenantLinkList()== null){
                            currentOccPermit.setTenantLinkList(new ArrayList<>());
                        }
                        currentOccPermit.getTenantLinkList().add(hl);
                        break;
            }
            configureOccPermitSelectedHumanLinkList(occPermitCurrentPersonLinkEnum);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Added person ID " + hl.getHumanID() + " to occ permit person list:  " + occPermitCurrentPersonLinkEnum.getLabel(), ""));
        } else {
            System.out.println("OccPeriodBB.onOccPermitPersonLinkQueuePerson | NULL currentOccPermit");
        }
        
        
        
    }
    
    /**
     * Listener for user requests to remove the chosen human link from
     * the bean's currently selected person link list
     * @param hl 
     */
    public void onOccPermitPersonLinkRemove(HumanLink hl){
         switch(occPermitCurrentPersonLinkEnum){
                case CURRENT_OWNER:
                    currentOccPermit.getOwnerSellerLinkList().remove(hl);
                    break;
                case MANAGERS:
                    currentOccPermit.getManagerLinkList().remove(hl);
                    break;
                case NEW_OWNER:
                    currentOccPermit.getBuyerTenantLinkList().remove(hl);
                    break;
                case TENANTS:
                    currentOccPermit.getTenantLinkList().remove(hl);
                    break;
            }
         configureOccPermitSelectedHumanLinkList(null);
    }
    
    /**
     * Listener for userers to start the nullification process
     * @param op 
     */
    public void onOccPermitNullifyInitLinkClick(OccPermit op){
        System.out.println("OccPeriodBB.onOccPermitNullifyInitLinkClick");
        currentOccPermit = op;
        
    }
    
    
    /**
     * Listener for user requests to nullify a passed in occ permit
     * @param ev
     * @param op 
     */
    public void onOccPermitNullifyCommitLinkClick(ActionEvent ev){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.nullifyOccPermit(currentOccPermit, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_INFO,
                      "Occ Permit ID " + currentOccPermit.getPermitID() + " has been nullified! ", ""));
            refreshCurrentOccPermit(null);
            reloadCurrentOccPeriodDataHeavy();
        } catch (BObStatusException | IntegrationException  ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_ERROR,
                       ex.getMessage(), ""));
        } 
        
    }
    
    
     /**
     * Special getter for the event list whose contents is managed by a shared 
     * set of utility methods on EventBB
     * @return the fresh list of events for this case
     */
    public List<EventCnF> getManagedEventList(){
        List<EventCnF> evlist = getSessionEventConductor().getSessEventListForRefreshUptake();
        if(currentOccPeriod != null){
            if(evlist != null){
                System.out.println("CECaseBB.getManagedEventList | fresh event list found on sessionbean of size: " + evlist.size());
                currentOccPeriod.setEventList(evlist);
                getSessionEventConductor().setSessEventListForRefreshUptake(null);
            } 
            return currentOccPeriod.getEventList();
        }
        return new ArrayList<>();
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
    public List<OccPermitType> getOccPeriodTypeList() {
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
    public void setOccPeriodTypeList(List<OccPermitType> occPeriodTypeList) {
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
    public OccPermitType getSelectedOccPeriodType() {
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
    public void setSelectedOccPeriodType(OccPermitType selectedOccPeriodType) {
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

    /**
     * @return the currentPropertyDH
     */
    public PropertyDataHeavy getCurrentPropertyDH() {
        return currentPropertyDH;
    }

    /**
     * @param currentPropertyDH the currentPropertyDH to set
     */
    public void setCurrentPropertyDH(PropertyDataHeavy currentPropertyDH) {
        this.currentPropertyDH = currentPropertyDH;
    }

    /**
     * @return the permitBlockCandidatesStipulations
     */
    public List<TextBlock> getPermitBlockCandidatesStipulations() {
        return permitBlockCandidatesStipulations;
    }

    /**
     * @return the permitBlockCandidatesNotices
     */
    public List<TextBlock> getPermitBlockCandidatesNotices() {
        return permitBlockCandidatesNotices;
    }

    /**
     * @return the permitBlockCandidatesComments
     */
    public List<TextBlock> getPermitBlockCandidatesComments() {
        return permitBlockCandidatesComments;
    }

    /**
     * @param permitBlockCandidatesStipulations the permitBlockCandidatesStipulations to set
     */
    public void setPermitBlockCandidatesStipulations(List<TextBlock> permitBlockCandidatesStipulations) {
        this.permitBlockCandidatesStipulations = permitBlockCandidatesStipulations;
    }

    /**
     * @param permitBlockCandidatesNotices the permitBlockCandidatesNotices to set
     */
    public void setPermitBlockCandidatesNotices(List<TextBlock> permitBlockCandidatesNotices) {
        this.permitBlockCandidatesNotices = permitBlockCandidatesNotices;
    }

    /**
     * @param permitBlockCandidatesComments the permitBlockCandidatesComments to set
     */
    public void setPermitBlockCandidatesComments(List<TextBlock> permitBlockCandidatesComments) {
        this.permitBlockCandidatesComments = permitBlockCandidatesComments;
    }

    /**
     * @return the currentTextBlock
     */
    public TextBlock getCurrentTextBlock() {
        return currentTextBlock;
    }

    /**
     * @param currentTextBlock the currentTextBlock to set
     */
    public void setCurrentTextBlock(TextBlock currentTextBlock) {
        this.currentTextBlock = currentTextBlock;
    }

    /**
     * @return the currentTextBlockPermitFieldEnum
     */
    public TextBlockPermitFieldEnum getCurrentTextBlockPermitFieldEnum() {
        return currentTextBlockPermitFieldEnum;
    }

    /**
     * @param currentTextBlockPermitFieldEnum the currentTextBlockPermitFieldEnum to set
     */
    public void setCurrentTextBlockPermitFieldEnum(TextBlockPermitFieldEnum currentTextBlockPermitFieldEnum) {
        this.currentTextBlockPermitFieldEnum = currentTextBlockPermitFieldEnum;
    }

    /**
     * @return the permitBlocksActiveCandidateList
     */
    public List<TextBlock> getPermitBlocksActiveCandidateList() {
        return permitBlocksActiveCandidateList;
    }

    /**
     * @param permitBlocksActiveCandidateList the permitBlocksActiveCandidateList to set
     */
    public void setPermitBlocksActiveCandidateList(List<TextBlock> permitBlocksActiveCandidateList) {
        this.permitBlocksActiveCandidateList = permitBlocksActiveCandidateList;
    }

    /**
     * @return the permitBlocksSelectedList
     */
    public List<TextBlock> getPermitBlocksSelectedList() {
        return permitBlocksSelectedList;
    }

    /**
     * @param permitBlocksSelectedList the permitBlocksSelectedList to set
     */
    public void setPermitBlocksSelectedList(List<TextBlock> permitBlocksSelectedList) {
        this.permitBlocksSelectedList = permitBlocksSelectedList;
    }

    /**
     * @return the occPermitSelectedHumanLinkList
     */
    public List<HumanLink> getOccPermitSelectedHumanLinkList() {
        return occPermitSelectedHumanLinkList;
    }

    /**
     * @return the occPermitSelectedHumanLink
     */
    public HumanLink getOccPermitSelectedHumanLink() {
        return occPermitSelectedHumanLink;
    }

    /**
     * @return the occPermitCurrentPersonLinkEnum
     */
    public OccPermitPersonListEnum getOccPermitCurrentPersonLinkEnum() {
        return occPermitCurrentPersonLinkEnum;
    }

    /**
     * @param occPermitSelectedHumanLinkList the occPermitSelectedHumanLinkList to set
     */
    public void setOccPermitSelectedHumanLinkList(List<HumanLink> occPermitSelectedHumanLinkList) {
        this.occPermitSelectedHumanLinkList = occPermitSelectedHumanLinkList;
    }

    /**
     * @param occPermitSelectedHumanLink the occPermitSelectedHumanLink to set
     */
    public void setOccPermitSelectedHumanLink(HumanLink occPermitSelectedHumanLink) {
        this.occPermitSelectedHumanLink = occPermitSelectedHumanLink;
    }

    /**
     * @param occPermitCurrentPersonLinkEnum the occPermitCurrentPersonLinkEnum to set
     */
    public void setOccPermitCurrentPersonLinkEnum(OccPermitPersonListEnum occPermitCurrentPersonLinkEnum) {
        this.occPermitCurrentPersonLinkEnum = occPermitCurrentPersonLinkEnum;
    }

    /**
     * @return the occPermitCandidateHumanLinkList
     */
    public List<HumanLink> getOccPermitCandidateHumanLinkList() {
        return occPermitCandidateHumanLinkList;
    }

    /**
     * @param occPermitCandidateHumanLinkList the occPermitCandidateHumanLinkList to set
     */
    public void setOccPermitCandidateHumanLinkList(List<HumanLink> occPermitCandidateHumanLinkList) {
        this.occPermitCandidateHumanLinkList = occPermitCandidateHumanLinkList;
    }

    /**
     * @return the permitBlocksFilteredList
     */
    public List<TextBlock> getPermitBlocksFilteredList() {
        return permitBlocksFilteredList;
    }

    /**
     * @param permitBlocksFilteredList the permitBlocksFilteredList to set
     */
    public void setPermitBlocksFilteredList(List<TextBlock> permitBlocksFilteredList) {
        this.permitBlocksFilteredList = permitBlocksFilteredList;
    }

    /**
     * @return the permitTypeCandidateList
     */
    public List<OccPermitType> getPermitTypeCandidateList() {
        return permitTypeCandidateList;
    }

    /**
     * @param permitTypeCandidateList the permitTypeCandidateList to set
     */
    public void setPermitTypeCandidateList(List<OccPermitType> permitTypeCandidateList) {
        this.permitTypeCandidateList = permitTypeCandidateList;
    }

   

}
