/*
 * Copyright (C) Technology Rediscovery LLC
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
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.InspectionException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.entities.occupancy.OccInspectionStatusEnum;
import com.tcvcog.tcvce.entities.occupancy.OccLocationDescriptor;
import com.tcvcog.tcvce.entities.occupancy.OccPermitApplication;
import com.tcvcog.tcvce.entities.occupancy.FieldInspection;
import com.tcvcog.tcvce.entities.occupancy.OccAppPersonRequirement;
import com.tcvcog.tcvce.integration.*;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsOccChecklistItemsEnum;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodDataHeavy;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodType;
import com.tcvcog.tcvce.entities.occupancy.OccPermit;
import com.tcvcog.tcvce.entities.reports.ReportConfigOccInspection;
import com.tcvcog.tcvce.entities.reports.ReportConfigOccPermit;
import com.tcvcog.tcvce.occupancy.integration.OccInspectionIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.tcvcog.tcvce.entities.occupancy.OccApplicationStatusEnum;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodPropertyUnitHeavy;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodStatusEnum;
import com.tcvcog.tcvce.entities.search.QueryEvent;
import com.tcvcog.tcvce.entities.search.QueryEventEnum;
import com.tcvcog.tcvce.entities.search.QueryPerson;
import com.tcvcog.tcvce.entities.search.QueryPersonEnum;
import com.tcvcog.tcvce.integration.BlobIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.occupancy.integration.PaymentIntegrator;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * King of all business logic implementation for the entire Occupancy object
 * tree the central of which is the Business Object OccPeriod
 *
 * @author ellen bascomb of apt 31y
 */
public class OccupancyCoordinator extends BackingBeanUtils implements Serializable {

    private final int MINIMUM_RANK_INSPECTOREVENTS = 5;
    private final int MINIMUM_RANK_STAFFEVENTS = 3;
    private final int DEFAULT_OCC_PERIOD_START_DATE_OFFSET = 30;

    /**
     * Creates a new instance of OccupancyCoordinator
     */
    public OccupancyCoordinator() {
    }

    /**
     * Primary retrieval point for extracted OccPeriod objects. Backing beans
     * should not be calling Integrators directly since the Coordinator is
     * responsible for implementing initialization logic usually through a call
     * to configureBObXXXX() method
     *
     * @param periodID
     * @return
     * @throws IntegrationException
     */
    public OccPeriod getOccPeriod(int periodID) throws IntegrationException {
        OccupancyIntegrator oi = getOccupancyIntegrator();
        OccPeriod op = null;
        try {
            op = configureOccPeriod(oi.getOccPeriod(periodID));
        } catch (EventException | AuthorizationException | BObStatusException | ViolationException ex) {
            System.out.println(ex);
        }
        return op;

    }

    /**
     * Assembles a special subclass of OccPeriod that contains a
     * PropertyUnitWithProp object for displaying property address info
     *
     * @param periodid
     * @return
     * @throws IntegrationException
     */
    public OccPeriodPropertyUnitHeavy getOccPeriodPropertyUnitHeavy(int periodid) throws IntegrationException {
        PropertyCoordinator pc = getPropertyCoordinator();
        OccPeriodPropertyUnitHeavy oppu = new OccPeriodPropertyUnitHeavy(getOccPeriod(periodid));
        try {
            oppu.setPropUnitProp(pc.getPropertyUnitWithProp(oppu.getPropertyUnitID()));
        } catch (BObStatusException ex) {
            throw new IntegrationException(ex.getMessage());
        }
        return oppu;
    }

    /**
     * Assembles a list of OccPeriodPropertyUnitHeavy objects that are handy because
     * you can ask them all about their parent property and unit, the periods of which normally
     * cannot look up the tree.
     * 
     * @param perList
     * @return
     * @throws IntegrationException 
     */
    public List<OccPeriodPropertyUnitHeavy> getOccPeriodPropertyUnitHeavyList(List<OccPeriod> perList) throws IntegrationException {
        List<OccPeriodPropertyUnitHeavy> oppuList = new ArrayList<>();
        for (OccPeriod op : perList) {
            oppuList.add(OccupancyCoordinator.this.getOccPeriodPropertyUnitHeavy(op.getPeriodID()));
        }

        return oppuList;

    }
    
     /**
     * Typesafe adaptor for getOccPeriodIDListByUnitID()
     * @param pu
     * @param u
     * @return
     * @throws IntegrationException
     * @throws AuthorizationException
     * @throws EventException
     * @throws BObStatusException
     * @throws ViolationException 
     */
    public List<OccPeriod> getOccPeriodList(PropertyUnit pu, UserAuthorized u) 
            throws  IntegrationException, 
                    AuthorizationException, 
                    EventException, 
                    BObStatusException, 
                    ViolationException {
        OccupancyIntegrator oi = getOccupancyIntegrator();
        
        if(pu == null || u == null){
            throw new BObStatusException("Cannot get Occ Perid list with null prop unit or user");
        }
        
        
        List<Integer> opidl = oi.getOccPeriodIDListByUnitID(pu.getUnitID());
        List<OccPeriod> occPeriodList = new ArrayList<>();
        if(opidl != null && !opidl.isEmpty()){
            for(Integer i: opidl){
                occPeriodList.add(configureOccPeriod(oi.getOccPeriod(i)));
            }
        }
        
        return occPeriodList;
    }
    

    /**
     * Retrieval point for Data-rich occupancy periods
     *
     * @param per
     * @param ua
     * @return
     * @throws IntegrationException
     */
    public OccPeriodDataHeavy assembleOccPeriodDataHeavy(OccPeriod per, UserAuthorized ua) throws IntegrationException, BObStatusException, SearchException {
        if (per == null || ua == null) {
            throw new BObStatusException("Cannot assemble an OccPeriod data heavy without base period or Credential");
        }

        OccupancyIntegrator oi = getOccupancyIntegrator();
        BlobCoordinator bc = getBlobCoordinator();
        PaymentIntegrator pai = getPaymentIntegrator();
        WorkflowCoordinator chc = getWorkflowCoordinator();
        SearchCoordinator sc = getSearchCoordinator();
        EventCoordinator ec = getEventCoordinator();
        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        PropertyCoordinator pc = getPropertyCoordinator();
        PersonCoordinator persc = getPersonCoordinator();
        OccPeriodDataHeavy opdh = new OccPeriodDataHeavy(per);

        // now get all the lists from their respective integrators
        // this is the Java version of table joins in SQL; we're doing them interatively
        // in our integrators for each BOB
        try {
            per = oi.getOccPeriod(per.getPeriodID());
            opdh.setPropUnitProp(pc.getPropertyUnitWithProp(opdh.getPropertyUnitID()));
            // APPLICATION LIST
            opdh.setApplicationList(oi.getOccPermitApplicationList(opdh));

          
            // TODO: Humanization upgrade after Ben's integration

            opdh.setHumanLinkList(persc.getHumanLinkList(opdh));

            // EVENT LIST

            QueryEvent qe = sc.initQuery(QueryEventEnum.OCCPERIOD, ua.getKeyCard());
            if (!qe.getParamsList().isEmpty()) {
                qe.getParamsList().get(0).setEventDomainPK_val(per.getPeriodID());
            }
            qe = sc.runQuery(qe);
            
            opdh.setEventList(ec.downcastEventCnFPropertyUnitHeavy(qe.getBOBResultList()));

            // PROPOSAL LIST
            // Potentially turn off proposal process? if its broken or erroring?
            opdh.setProposalList(chc.getProposalList(opdh, ua.getKeyCard()));

            // EVENT RULE LIST
            // Potentially turn off this thingy? if its broken or erroring?
            opdh.setEventRuleList(chc.rules_getEventRuleImpList(opdh, ua.getKeyCard()));

            // INSPECTION LIST

            opdh.setInspectionList(oic.getOccInspectionList(opdh));

            // FEE AND PAYMENT LIST
//          TODO: Activate fee/payment stuff  
//            opdh.setPaymentListGeneral(pai.getPaymentList(opdh));
//            opdh.setFeeList(pai.getFeeAssigned(opdh));

            // PERMIT LIST
            opdh.setPermitList(getOccPermitList(per, ua));
            // BLOB LIST
            opdh.setBlobList(bc.getBlobLightList(opdh));

            // Commented out 26-JAN-2022 here since we  aren't implementing rigid logic
            // on occ period permit generation based on inspection output data
            // TODO: Reconsider governing inspection idea            //
//            opdh.setGoverningInspection(designateGoverningInspection(opdh));

        } catch (BObStatusException  | EventException | AuthorizationException | IntegrationException | ViolationException | BlobException  ex) {
            System.out.println(ex);
        }

        return opdh;

    }
    
    /**
     * Connects a given inspection to the new period specified by the target. 
     
     * @param ins to Xfer to a new period
     * @param periodIDTarget ID of the period to which the given inspection should
     * be attached
     */
    public void transferInspectionOccPeriod(FieldInspection ins, int periodIDTarget) throws BObStatusException, IntegrationException{
        if(ins == null || periodIDTarget == 0){
            throw new BObStatusException("Cannot xcer an inspection to a new period with null Inspection or target ID of 0");
        }
        OccPeriod per = getOccPeriod(periodIDTarget);
        if(per == null){
            throw new BObStatusException("Invalid target occupancy period ID");
        }
        
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        ins.setOccPeriodID(periodIDTarget);
        oii.updateOccInspection(ins);
        
    }

    public List<OccPeriodType> getOccPeriodTypesFromProfileID(int profileID) {

        OccupancyIntegrator oi = getOccupancyIntegrator();
        List<OccPeriodType> typeList = new ArrayList<>();
        try {
            typeList = oi.getOccPeriodTypeList(profileID);
        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
        }
        return typeList;

    }

    /**
     * Logic container for determining occ period status based on a OPDH
     * <p>
     * As of Beta 0.9, we're just arbitrarily setting the status to unknown
     *
     * @param opdh
     * @param cred
     * @return
     */
    private OccPeriodDataHeavy configureOccPeriodDataHeavy(OccPeriodDataHeavy opdh, Credential cred) {
        if (opdh == null) {
            return opdh;
        }

        // TODO: Write logic for occperiod status
        opdh.setStatus(OccPeriodStatusEnum.UNKNOWN);

        return opdh;
    }

    /**
     * Shell container for holding configuration logic applicable to OccPeriods
     * minus their many lists
     *
     * @param period
     * @return
     * @throws EventException
     * @throws AuthorizationException
     * @throws IntegrationException
     * @throws BObStatusException
     * @throws ViolationException
     */
    public OccPeriod configureOccPeriod(OccPeriod period)
            throws EventException, AuthorizationException, IntegrationException, BObStatusException, ViolationException {


        return period;

    }
    
    
    
    /**
     * Logic intermediary for occ permits
     * @param permit
     * @param ua
     * @return 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public OccPermit configureOccPermit(OccPermit permit, UserAuthorized ua) throws BObStatusException{
        if(permit == null || ua == null){
            throw new BObStatusException("Cannot configure occ permit with null permit or ua");
        }
        PropertyCoordinator pc = getPropertyCoordinator();
        
        
        if(permit.getFinalizedts() == null){
            try {
                OccPeriod op = getOccPeriod(permit.getPeriodID());
                PropertyDataHeavy pdh = pc.assemblePropertyDataHeavy(pc.getPropertyByPropUnitID(op.getPropertyUnitID()), ua);
                permit.setParcelInfo(pdh.getParcelInfo());
            } catch (SearchException | BObStatusException | BlobException | IntegrationException ex) {
                throw new BObStatusException("Unable to configure parcel info on permit");
            } 
            
            permit.setBuyerTenantLinkList(new ArrayList<>());
            permit.setManagerLinkList(new ArrayList<>());
            permit.setTenantLinkList(new ArrayList<>());
            permit.setTextBlocks_stipulations(new ArrayList<>());
            permit.setTextBlocks_notice(new ArrayList<>());
            permit.setTextBlocks_comments(new ArrayList<>());               
    
        }
        
        
        return permit;
        
        
    }
    
    
    /**
     * Generator method for creating a new occ period skeleton for
     * creation origination
     * @param pu
     * @param ua the current user, and if they have an officer swearing
     * then they'll become the unit manager
     * @return 
     */
    public OccPeriod getOccPeriodSkeleton(PropertyUnit pu, UserAuthorized ua) throws BObStatusException{
       if(pu == null || ua == null){
           throw new BObStatusException("Cannot make occ period skeleton with null unit or user");
           
       }
        OccPeriod per = new OccPeriod();
        per.setPropertyUnitID(pu.getUnitID());
        if(ua.getKeyCard().getGoverningAuthPeriod().getOathTS() != null 
                || ua.getKeyCard().getGoverningAuthPeriod().getRole().getRank() == RoleType.Developer.getRank()){
            per.setManager(ua);
        }
        
        return per;
    }

    /**
     * Coordinates with the integrator to build an occ period history list
     * @param cred
     * @return 
     */
    public List<OccPeriod> assembleOccPeriodHistoryList(Credential cred) {
        OccupancyIntegrator oi = getOccupancyIntegrator();

        List<OccPeriod> opList = new ArrayList<>();
        try {
            for (Integer i : oi.getOccPeriodHistoryList(cred.getGoverningAuthPeriod().getUserID())) {
                opList.add(getOccPeriod(i));
            }
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }

        return opList;

    }

    /**
     * TODO: Finish
     *
     * @param period
     */
    public void configureRuleSet(OccPeriodDataHeavy period) {
//        List<EventRuleImplementation> evRuleList = period.getb(ViewOptionsEventRulesEnum.VIEW_ALL);
//        for(EventRuleAbstract era: evRuleList){
//            if(era.getPromptingDirective()!= null){
//                // TODO: Finish
//            }
//        }
    }

    public List<EventType> getPermittedEventTypes(OccPeriod op, UserAuthorized u) {
        List<EventType> typeList = new ArrayList<>();
        int rnk = u.getRoleType().getRank();

        if (rnk >= MINIMUM_RANK_INSPECTOREVENTS) {
            typeList.add(EventType.Action);
            typeList.add(EventType.Timeline);
        }

        if (rnk >= MINIMUM_RANK_STAFFEVENTS) {
            typeList.add(EventType.Communication);
            typeList.add(EventType.Meeting);
            typeList.add(EventType.Custom);
            typeList.add(EventType.Occupancy);
        }

        return typeList;
    }

    public void updateOccPeriodPropUnit(OccPeriod period, PropertyUnit pu) throws IntegrationException {
        OccupancyIntegrator oi = getOccupancyIntegrator();
        period.setPropertyUnitID(pu.getUnitID());
        oi.updateOccPeriod(period);
    }

    /**
     * Logic container for checking the basic permissions for authorization of
     * an occupancy period. An authorized occupancy period is one for which an
     * occupancy permit can be issued
     *
     * @param period
     * @param u      doing the authorizing; must have code officer permissions
     * @throws AuthorizationException
     * @throws BObStatusException
     * @throws IntegrationException
     */
    public void toggleOccPeriodAuthorization(OccPeriod period, UserAuthorized u) throws AuthorizationException, BObStatusException, IntegrationException {
        OccupancyIntegrator oi = getOccupancyIntegrator();
        if (u.getKeyCard().isHasEnfOfficialPermissions()) {
            // TODO: Figure out occupancy period status and authorization permission

            // If it is not currently authorized
            if (period.getAuthorizedBy() == null) {
                // And it is ready for authorization
                if (period.getEndDateCertifiedBy() != null &&
                        period.getStartDateCertifiedBy() != null &&
                        period.getPeriodTypeCertifiedBy() != null) {
                    // Authorize it!
                    period.setAuthorizedBy(u);
                    period.setAuthorizedTS(LocalDateTime.now());
                    oi.updateOccPeriod(period);
                } else {
                    throw new BObStatusException("Occupancy period is not ready for authorization");
                }
            } else {
                // If it's already authorized, toggle it back to unauthorized
                period.setAuthorizedBy(null);
                period.setAuthorizedTS(null);
                oi.updateOccPeriod(period);
            }
        } else {
            throw new AuthorizationException("Users must have enforcement official permissions to authorize or deauthorize an occupancy period");
        }
    }

   /**
    * An occ permit report is the container that prints an occ permit, it's not a 
    * report about occ permits, for that see reportOccupancyList
    * @param permit
    * @param period
    * @param propUnit
    * @param u
    * @return with sensible defaults
    */
    public ReportConfigOccPermit getOccPermitReportConfigDefault(OccPermit permit,

                                                                 OccPeriod period,
                                                                 PropertyUnit propUnit,
                                                                 User u) {
        PropertyIntegrator pi = getPropertyIntegrator();
        ReportConfigOccPermit rpt = new ReportConfigOccPermit();
        rpt.setTitle(getResourceBundle(Constants.MESSAGE_TEXT).getString("report_occpermit_default_title"));

        rpt.setPermit(permit);
        rpt.setPeriod(period);
        try {
            rpt.setPropUnitWithProp(pi.getPropertyUnitWithProp(propUnit.getUnitID()));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
        }
        rpt.setCreator(u);

        return rpt;
    }

    
    // *************************************************************************
    // ********************* OCC PERMITS   ************************************
    // *************************************************************************
    
    /**
     * Factory for OccPermits
     * @param usr
     * @return 
     */
    public OccPermit getOccPermitSkeleton(User usr) {
        OccPermit permit = new OccPermit();
        permit.setReferenceNo("GENERATED ON FINALIZATION");
        permit.setCreatedBy(usr);

        
        return permit;

    }
    
    /**
     * Getter for occupancy permits
     * @param permitID
     * @param ua
     * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public OccPermit getOccPermit(int permitID, UserAuthorized ua) throws IntegrationException, BObStatusException{
        OccupancyIntegrator oi = getOccupancyIntegrator();
        if(permitID == 0){
            throw new BObStatusException("OccupancyCoordinator.getOccPermit | Cannot fetch a permit with ID 0");
        }
        return configureOccPermit(oi.getOccPermit(permitID), ua);
    }

    
    /**
     * 
     * @param op
     * @param ua
     * @return
     * @throws BObStatusException 
     */
    public List<OccPermit> getOccPermitList(OccPeriod op, UserAuthorized ua) throws BObStatusException, IntegrationException{
        
        
       if(op == null || ua == null){
            throw new BObStatusException("OccupancyCoordinator.getOccPermitList | Cannot get occ permit list with null occ period or user");
        }
       if(op.getPeriodID() == 0){
            throw new BObStatusException("OccupancyCoordinator.getOccPermitList | Cannot get occ permit list with occ period whose ID == 0");
           
       }
        
        OccupancyIntegrator oi = getOccupancyIntegrator();
        List<Integer> opermitIDList = oi.getOccPermitIDList(op);
        
        List<OccPermit> occPermitList = new ArrayList<>();
        if(opermitIDList != null && !opermitIDList.isEmpty()){
            for(Integer i: opermitIDList){
                occPermitList.add(getOccPermit(i, ua));
            }
        }
        
        return occPermitList;
    
    }
    
    /**
     * insertion pathway for occ permits
     * @param permit
     * @param per
     * @param ua
     * @return
     * @throws BObStatusException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public int insertOccPermit(OccPermit permit, OccPeriod per, UserAuthorized ua) throws BObStatusException, IntegrationException{
        if(permit == null || ua == null || per == null){
            throw new BObStatusException("Cannot insert occ permit with null permit or user or period");
            
        }
        OccupancyIntegrator oi = getOccupancyIntegrator();
        permit.setPeriodID(per.getPeriodID());
        permit.setCreatedBy(ua);
        permit.setLastUpdatedBy(ua);
        return oi.insertOccPermit(permit);
        
    }
    
    /**
     * Updates the meta data of an occ permit only. Use separate method for static fields
     * @param permit
     * @param ua
     * @throws BObStatusException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void updateOccPermit(OccPermit permit, UserAuthorized ua) throws BObStatusException, IntegrationException{
        if(permit == null || ua == null){
            throw new BObStatusException("Cannot insert occ permit with null permit or user");
        }
        OccupancyIntegrator oi = getOccupancyIntegrator();
        permit.setLastUpdatedBy(ua);
        oi.updateOccPermit(permit);
        
    }
    
    
    /**
     * Updates the meta data of an occ permit only. Use separate method for static fields
     * @param permit
     * @param ua
     * @param m
     * @throws BObStatusException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void occPermitFinalize(OccPermit permit, UserAuthorized ua, Municipality m) throws BObStatusException, IntegrationException{
        if(permit == null || ua == null){
            throw new BObStatusException("Cannot finalize occ permit with null permit or user");
        }
        OccupancyIntegrator oi = getOccupancyIntegrator();
        permit.setLastUpdatedBy(ua);
        permit.setFinalizedBy(ua);
        permit.setFinalizedts(LocalDateTime.now());
        permit.setReferenceNo(generateOccPermitReferenceNumber(permit, m));
        
        oi.updateOccPermit(permit);
        
    }
    
    /**
     * Deactivates an occ permit only. Use separate method for static fields
     * @param permit Cannot be finalized
     * @param ua
     * @throws BObStatusException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void occPermitDeactivate(OccPermit permit, UserAuthorized ua) throws BObStatusException, IntegrationException{
        if(permit == null || ua == null){
            throw new BObStatusException("Cannot finalize occ permit with null permit or user");
        }
        if(permit.getFinalizedts() != null){
            throw new BObStatusException("Umm, you aren't allowed to deactivate a finalized permit");
        }
        OccupancyIntegrator oi = getOccupancyIntegrator();
        permit.setLastUpdatedBy(ua);
        permit.setDeactivatedBy(ua);
        permit.setDeactivatedTS(LocalDateTime.now());
        
        oi.updateOccPermit(permit);
        
    }
    
    
    /**
     * The lifeblood logic house for extracting the objects from the OccPermit's
     * non static fields and extracting the appropriate data and writing
     * fixed String/Date values to the OccPermit's static fields and then writing those
     * changes to the DB
     * @param period
     * @param permit
     * @param ua
     * @param mdh
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    public void updateOccPermitStaticFields(OccPeriodDataHeavy period, OccPermit permit, UserAuthorized ua, MunicipalityDataHeavy mdh) throws BObStatusException, IntegrationException{
        if(permit == null || ua == null || period == null || mdh == null){
            throw new BObStatusException("Cannot insert occ permit with null permit or user or period or munidh");
        }
        if(permit.getFinalizedts() != null){
            throw new BObStatusException("Cannot update static fields of a finalized permit!");
        }
        OccupancyIntegrator oi = getOccupancyIntegrator();
        PropertyCoordinator pc = getPropertyCoordinator();       
        StringBuilder sb;
        
         // now do property and unit static fields
        PropertyUnitWithProp puwp = pc.getPropertyUnitWithProp(period.getPropertyUnitID());
        sb = new StringBuilder();
        if(puwp != null){
            if(!puwp.getUnitNumber().equals(pc.DEFAULTUNITNUMBER)){
                sb.append(puwp.getUnitNumber());
                sb.append(Constants.FMT_HTML_BREAK);
            }
            sb.append(puwp.getProperty().getAddress().getAddressPretty2LineEscapeFalse());
            sb.append(Constants.FMT_HTML_BREAK);
            sb.append(puwp.getProperty().getCountyParcelID());
        }
        // muni addresss
        permit.setStaticmuniaddress(mdh.getMuniPropertyDH().getAddress().getAddressPretty2LineEscapeFalse());

        // move over dates
        permit.setStaticdateofapplication(permit.getDynamicdateofapplication());
        permit.setStaticinitialinspection(permit.getDynamicfinalinspection());
        permit.setStaticreinspectiondate(permit.getDynamicreinspectiondate());
        permit.setStaticfinalinspection(permit.getDynamicfinalinspection());
        permit.setStaticdateofissue(permit.getDynamicdateofissue());
        
        // based on parcel info, populate parcel info static fields
        if(permit.getParcelInfo() == null){
            throw new BObStatusException("Cannot populate occ permit with null parcel info in occ permit");
        }
        if(permit.getIssuingCodeSource() == null){
            throw new BObStatusException("Cannot populate occ permit with null code source");
        }
        if(permit.getIssuingOfficer()== null){
            throw new BObStatusException("Cannot populate occ permit with null issuing officer");
        }
        // inject parcel info fields
        permit.setStaticpropclass(permit.getParcelInfo().getPropClass());
        permit.setStaticusecode(permit.getParcelInfo().getUseGroup());
        permit.setStaticproposeduse(period.getType().getAuthorizeduses());
        permit.setStaticconstructiontype(permit.getParcelInfo().getConstructionType());
        
        // build code source string
        sb = new StringBuilder();
        sb.append(permit.getIssuingCodeSource().getSourceName());
        sb.append(" (");
        sb.append(permit.getIssuingCodeSource().getSourceYear());
        sb.append(")");
        permit.setStaticissuedundercodesourceid(sb.toString());
        
        // issuing officer
        permit.setStaticofficername(permit.getIssuingOfficer().getHuman().getName());
        
       // now deal with person links
       if(permit.getOwnerSellerLinkList() != null && !permit.getOwnerSellerLinkList().isEmpty()){
           sb = new StringBuilder();
           for(HumanLink hl: permit.getOwnerSellerLinkList()){
                sb.append(buildPersonContactString(hl));
           }
           permit.setStaticownerseller(sb.toString());
       }
        
       if(permit.getBuyerTenantLinkList() != null && !permit.getBuyerTenantLinkList().isEmpty()){
           sb = new StringBuilder();
           for(HumanLink hl: permit.getBuyerTenantLinkList()){
                sb.append(buildPersonContactString(hl));
           }
           permit.setStaticbuyertenant(sb.toString());
           
       }
        
       if(permit.getManagerLinkList() != null && !permit.getManagerLinkList().isEmpty()){
           sb = new StringBuilder();
           for(HumanLink hl: permit.getManagerLinkList()){
                sb.append(buildPersonContactString(hl));
           }
           permit.setStaticmanager(sb.toString());
       }
        
       if(permit.getTenantLinkList() != null && !permit.getTenantLinkList().isEmpty()){
           sb = new StringBuilder();
           for(HumanLink hl: permit.getTenantLinkList()){
                sb.append(buildPersonContactString(hl));
           }
           permit.setStatictenants(sb.toString());
       }

       // Now do text lists injection
       permit.setStaticstipulations(buildSingleStringFromTextBlockList(permit.getTextBlocks_stipulations()));
       permit.setStaticnotice(buildSingleStringFromTextBlockList(permit.getTextBlocks_notice()));
       sb = new StringBuilder();
       if(permit.getTextBlocks_comments() != null && !permit.getTextBlocks_comments().isEmpty()){
           sb.append(buildSingleStringFromTextBlockList(permit.getTextBlocks_comments()));
           sb.append(Constants.FMT_HTML_BREAK);
       }
       if(permit.getText_comments() != null){
           
           sb.append(permit.getText_comments());
       }
       permit.setStaticcomments(sb.toString());
       
        permit.setLastUpdatedBy(ua);
        oi.updateOccPermitStaticFields(permit);
    }
    
    /**
     * Utility method for weaving together name and contact into a string for printing
     * @param hl
     * @return
     * @throws BObStatusException 
     */
    private String buildPersonContactString(HumanLink hl) throws BObStatusException{
        StringBuilder sb = new StringBuilder();
        if(hl == null){
            throw new BObStatusException("cannot build person string with null link");
        }
        
        sb.append(hl.getName());
        if(hl.getMailingAddressListPretty() != null){
            sb.append(Constants.FMT_HTML_BREAK);
            sb.append(hl.getMailingAddressListPretty());
        }
        if(hl.getPhoneListPretty() != null){
            sb.append(Constants.FMT_HTML_BREAK);
            sb.append(hl.getPhoneListPretty());
        }
        
        if(hl.getEmailListPretty() != null){
            sb.append(Constants.FMT_HTML_BREAK);
            sb.append(hl.getEmailListPretty());
        }
        return sb.toString();
    }
    /**
     * Takes in a list of text blocks and spits out a single string
     * with breaks in between blocks
     * @param tbl
     * @return 
     */
    private String buildSingleStringFromTextBlockList(List<TextBlock> tbl){
        if(tbl == null){
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if(!tbl.isEmpty()){
            for(TextBlock tb: tbl){
                sb.append(tb.getTextBlockText());
                sb.append(Constants.FMT_HTML_BREAK);
            }
        }
        return sb.toString();
    }
    
    
    /**
     * Builds a custom reference number for the permit of the form
     * MUNICODE-YY-SEQ
     * eg. 814-22-1
     * @param permit
     * @param ua
     * @return 
     */
    private String generateOccPermitReferenceNumber(OccPermit permit, Municipality m) throws BObStatusException, IntegrationException{
        OccupancyIntegrator oi = getOccupancyIntegrator();
        StringBuilder sb = new StringBuilder();
        sb.append(m.getMuniCode());
        sb.append(Constants.FMT_DTYPE_KEY_SEP_DESC);
        sb.append(LocalDateTime.now().getYear());
        sb.append(Constants.FMT_DTYPE_KEY_SEP_DESC);
        sb.append("CNF");
        sb.append(Constants.FMT_DTYPE_KEY_SEP_DESC);
        
        int currentCount = oi.countFinalizedPermitsByMuni(m);
        int increcount = currentCount + 1;
        sb.append(increcount);
        return sb.toString();
        
   }
    
    // *************************************************************************
    // ********************* OCC LOCATION DESCRIPTORS **************************
    // *************************************************************************
    
    /**
     * Logic intermediary for Location Descriptors
     * @param locid
     * @return
     * @throws IntegrationException 
     */
    public OccLocationDescriptor getOccLocationDescriptor(int locid) throws IntegrationException{
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        return oii.getLocationDescriptor(locid);
       
    }
    
    /**
     * Factory for OccLocationDescriptors
     * @return 
     */
    public OccLocationDescriptor getOccLocationDescriptorSkeleton() {
        return new OccLocationDescriptor();
    }

    /**
     * Logic intermediary for creating new occ location descriptors
     * @param old
     * @return
     * @throws IntegrationException 
     */
    public int addNewLocationDescriptor(OccLocationDescriptor old) throws IntegrationException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        int freshLocID = 0;
        freshLocID = oii.insertLocationDescriptor(old);

        return freshLocID;
    }


    /**
     * Updates DB to mark the passed in FieldInspection the governing one in the
 given OccPeriod
     *
     * @param period
     * @return the governing Inspection
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public FieldInspection designateGoverningInspection(OccPeriodDataHeavy period) throws BObStatusException {
        List<FieldInspection> inspectionList = period.getInspectionList();
        FieldInspection selIns = null;
        // logic for determining the currentOccInspection
        if (inspectionList != null) {
            if (inspectionList.size() == 1) {
                selIns = inspectionList.get(0);
            } else {
                Collections.sort(inspectionList);
                for (FieldInspection ins : inspectionList) {
                    // TODO: if you are refactoring this, isActive used to be checked here!
                    selIns = ins;
                }
            }
        }
        try {
            if (period.getGoverningInspection() != null && selIns != null) {
                if (selIns.getInspectionID() != period.getGoverningInspection().getInspectionID()) {
                    activateOccInspection(selIns);
                }
            }
        } catch (IntegrationException ex) {
            throw new BObStatusException("Cannot designate governing inspection");
        }
        return selIns;
    }

    /**
     * Initialization method for creating a of an OccPeriod with
     * sensible default values for first insertion into DB
     * 
     * Also attaches an initialization event to the occ period
     *
     * @param period
     * @param ua
     * @return
     * @throws IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.EventException
     */
    public int insertOccPeriod(   OccPeriod period,
                                        UserAuthorized ua) 
            throws IntegrationException, BObStatusException, EventException {
        SystemIntegrator si = getSystemIntegrator();
        OccupancyIntegrator oi = getOccupancyIntegrator();
        
        if(period == null || period.getPropertyUnitID() == 0 || period.getType() == null || ua == null){
            throw new BObStatusException("cannot create new occ period with null period, type, unit of id 0, or user");
        }
        
        period.setCreatedBy(ua);
        period.setLastUpdatedBy(ua);
        

        period.setStartDate(LocalDateTime.now().plusDays(DEFAULT_OCC_PERIOD_START_DATE_OFFSET));
        period.setStartDateCertifiedBy(null);
        period.setStartDateCertifiedTS(null);

        if (period.getStartDate() != null) {
            period.setEndDate(period.getStartDate().plusDays(period.getType().getDefaultValidityPeriodDays()));
        }
        period.setEndDateCertifiedBy(null);
        period.setEndDateCertifiedTS(null);

        period.setSource(si.getBOBSource(Integer.parseInt(
                getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                        .getString("occPeriodNewInternalBOBSourceID"))));

        System.out.println("OccupancyCoordinator.intitializeNewOccPeriod | period: " + period);
        
        // send to integrator!
        int freshID = oi.insertOccPeriod(period);
        
        if(period.getOriginationEventCategory() != null){
            EventCoordinator ec = getEventCoordinator();
            EventCnF ev = ec.initEvent(period, period.getOriginationEventCategory());
            ev.setUserCreator(ua);
            ev.setLastUpdatedBy(ua);
            ev.setOccPeriodID(freshID);
            ec.addEvent(ev, period, ua);
        }
        
        return freshID;
    }

   
    /**
     * Factory method for creating new OccPermitApplications
     *
     * @param muniCode
     * @return
     * @throws IntegrationException If an error occurs while generating a control code
     */
    public OccPermitApplication initOccPermitApplication(int muniCode) throws IntegrationException {
        OccPermitApplication occpermitapp = new OccPermitApplication();
        occpermitapp.setPublicControlCode(generateControlCodeFromTime(muniCode));
        occpermitapp.setSubmissionDate(LocalDateTime.now());
        return occpermitapp;
    }

    /**
     * Logic pass through method for updates on the OccPeriod
     *
     * @param period
     * @param ua
     * @throws IntegrationException
     * @throws BObStatusException   if the OccPeriod is authorized
     */
    public void editOccPeriod(OccPeriod period, UserAuthorized ua) throws IntegrationException, BObStatusException {
        OccupancyIntegrator oi = getOccupancyIntegrator();

        if (period.getAuthorizedTS() != null) {
            throw new BObStatusException("Cannot change period type or manager on an authorized period; the period must first be unauthorized");
        }

        oi.updateOccPeriod(period);
    }

    /**
     * Attaches a note to the given Occ Period.
     *
     * @param mbp
     * @param period whose note field contains the properly formatted note that
     *               includes all old note text. This is best done by a call to the
     *               SystemCoordinator's appendNoteBlock() method
     * @throws IntegrationException
     */
    public void attachNoteToOccPeriod(MessageBuilderParams mbp, OccPeriod period) throws BObStatusException, IntegrationException {
        OccupancyIntegrator oi = getOccupancyIntegrator();
        SystemCoordinator sc = getSystemCoordinator();

        if (period == null || mbp == null) {
            throw new BObStatusException("Cannot append if notes, occperiod, or user are null");
        }

        period.setNotes(sc.appendNoteBlock(mbp));
        period.setLastUpdatedBy(mbp.getUser());
        oi.updateOccPeriod(period);
    }

 

    public void activateOccInspection(FieldInspection is) throws IntegrationException {
        // Nothing to do here yet
        
        

    }

    /**
     * Sets boolean requirementSatisfied on an OccPermitApplication based on the
     * application reason, the person requirement for that reason, and the
     * PersonTypes of the Persons attached to the application.
     * Also checks for Applicant.
     * If Preferred Contact is null, then it sets the Preferred Contact equal to the applicant
     *
     * @param opa
     * @return the sanitize OccPermitApplication
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public OccPermitApplication verifyOccPermitPersonsRequirement(OccPermitApplication opa) throws BObStatusException {
        OccAppPersonRequirement pr = opa.getReason().getPersonsRequirement();
        pr.setRequirementSatisfied(false); // If we meet the requirement, we will reach the end and set this to true.

        if (opa.getApplicantPerson() == null) {
            throw new BObStatusException("Please specify an applicant.");
        }


        if (opa.getPreferredContact() == null) {

// TODO: finsih me for humanization
//            opa.setPreferredContact(opa.getApplicantPerson());
        }

        List<PersonType> applicationPersonTypes = new ArrayList<>();
    // TODO: finish for humanization
//        for (PersonOccApplication applicationPerson : opa.getAttachedPersons()) {
//            if (applicationPerson.getFirstName() == null || applicationPerson.getFirstName().contentEquals("")) {
//                throw new BObStatusException("The first name field is not optional. "
//                        + "If you are filling in the name of a business, "
//                        + "please put your business\' full name in the first name field.");
//            }
//            applicationPersonTypes.add(applicationPerson.getApplicationPersonType());
//        }

        for (PersonType personType : pr.getRequiredPersonTypes()) {
            if (!applicationPersonTypes.contains(personType)) {
                throw new BObStatusException("Please specify a(n) " + personType.getLabel());
            }
        }
        pr.setRequirementSatisfied(true);//we've reached the end, the requirement was satisfied.
        return opa;
    }

    /**
     * Inserts an application to the database and attaches it to the default
     *
     * @param application
     * @return the application ID
     * @throws IntegrationException
     * @throws com.tcvcog.tcvce.domain.AuthorizationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.EventException
     */
    public int insertOccPermitApplication(OccPermitApplication application)
            throws IntegrationException, AuthorizationException,
            BObStatusException, EventException, BlobException {

        OccupancyIntegrator opi = getOccupancyIntegrator();

        OccPeriod connectedPeriod = null;

        if (getSessionBean().getSessUser() != null) {
            //if we are in the middle of an internal session, the muni will be stored in the SessMuni field.
            connectedPeriod = getOccPeriod(getSessionBean().getSessMuni().getDefaultOccPeriodID());
        } else {
            //if sessUser is null, we're in an external session, which stores the current muni in sessMuniQueued
            MunicipalityCoordinator mc = getMuniCoordinator();
            UserCoordinator uc = getUserCoordinator();

            MunicipalityDataHeavy temp = mc.assembleMuniDataHeavy(getSessionBean().getSessMuniQueued(), uc.auth_getPublicUserAuthorized());

            connectedPeriod = getOccPeriod(temp.getDefaultOccPeriodID());

        }
        application.setConnectedPeriod(connectedPeriod);

        int applicationId = opi.insertOccPermitApplication(application);

        application.setId(applicationId);

        insertOccApplicationPersonLinks(application);

        return applicationId;

    }

    /**
     * Inserts a new
     *
     * @param application
     * @param notes
     * @return the ID of the newly inserted OccPeriod
     * @throws IntegrationException
     * @throws AuthorizationException
     * @throws BObStatusException
     * @throws EventException
     * @throws InspectionException
     * @throws ViolationException
     * @throws com.tcvcog.tcvce.domain.BlobException
     */
    public int attachApplicationToNewOccPeriod(OccPermitApplication application, String notes)
            throws IntegrationException, AuthorizationException,
            BObStatusException, EventException,
            InspectionException, ViolationException, BlobException {

        PropertyIntegrator pri = getPropertyIntegrator();
        MunicipalityCoordinator mc = getMuniCoordinator();
        SystemIntegrator si = getSystemIntegrator();
        SystemCoordinator sc = getSystemCoordinator();
        UserAuthorized user = getSessionBean().getSessUser();
        OccupancyIntegrator oi = getOccupancyIntegrator();

        Property prop = pri.getPropertyUnitWithProp(application.getApplicationPropertyUnit().getUnitID()).getProperty();

        MunicipalityDataHeavy muni = mc.assembleMuniDataHeavy(prop.getMuni(), user);
//        TODO: FIX ME POST HUMANIZATION
        
//        OccPeriod connectedPeriod = insertOccPeriod(
//                prop,
//                application.getApplicationPropertyUnit(),
//                application.getReason().getProposalPeriodType(),
//                user,
//                muni);
//
//        connectedPeriod.setNotes(sc.formatAndAppendNote(user, notes, connectedPeriod.getNotes()));
//
//        connectedPeriod.setSource(si.getBOBSource(
//                Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
//                        .getString("occPeriodPublicUserBOBSourceID"))));
//
//        int newPeriodID = addOccPeriod(connectedPeriod, user);
//
//        //Now we need to update the Application with the fact that it was ttached
//        connectedPeriod.setPeriodID(newPeriodID);

//        application.setConnectedPeriod(connectedPeriod);

        MessageBuilderParams mcc = new MessageBuilderParams();
        mcc.setUser(getSessionBean().getSessUser());
        mcc.setExistingContent(application.getExternalPublicNotes());
        if (application.getStatus() == OccApplicationStatusEnum.NewUnit) {
            mcc.setHeader(getResourceBundle(Constants.MESSAGE_TEXT).getString("acceptedNewUnitOccPermitApplicationHeader"));
            mcc.setExplanation(getResourceBundle(Constants.MESSAGE_TEXT).getString("acceptedNewUnitOccPermitApplicationExplanation"));
        } else {
            //If it's not attached to a new unit, it should be attached to an existing one.
            mcc.setHeader(getResourceBundle(Constants.MESSAGE_TEXT).getString("acceptedExistingUnitOccPermitApplicationHeader"));
            mcc.setExplanation(getResourceBundle(Constants.MESSAGE_TEXT).getString("acceptedExistingUnitOccPermitApplicationExplanation"));
        }
        mcc.setNewMessageContent(notes);

        application.setExternalPublicNotes(sc.appendNoteBlock(mcc));

        oi.updateOccPermitApplication(application);

        return 0;

    }

    /**
     * Inserts all the persons attached to an OccPermitApplication
     * into the occpermitapplicationperson table.
     * The default value for the applicant column is false, and that
     * column will be set to true when the applicant person is the same as a
     * person within the OccPermitApplication's attachedPersons variable. The
     * boolean for the preferred contact is set similarly.
     * WARNING: Assumes all person objects are already in the database.
     * If any personID == 0, you're going to get an integration Exception.
     *
     * @param application
     * @throws IntegrationException
     */
    public void insertOccApplicationPersonLinks(OccPermitApplication application) throws IntegrationException {

        OccupancyIntegrator oi = getOccupancyIntegrator();

            //: TODO update for humanization

//        List<PersonOccApplication> applicationPersons = application.getAttachedPersons();
//        for (PersonOccApplication person : applicationPersons) {

            //see javadoc
//            if (person.getPersonID() == 0) {
//                throw new IntegrationException("OccupancyCoordinator.insertOccApplicationPersonLinks() detected a person not yet in the database."
//                        + " Please insert persons into the database before running this method!");
//            }
            
            
            /* If the person  is the applicantPerson on the 
            OccPermitApplication, set applicant to true*/
//            person.setApplicant(application.getApplicantPerson().getHumanID() == person.getHumanID());

            /* If the person is the preferredContact on the 
            OccPermitApplication, set preferredcontact to true */
//            person.setPreferredContact(application.getPreferredContact().getPersonID() == person.getPersonID());
//
//            oi.insertOccApplicationPerson(person, application.getId());

//        }
    }

    public void updateOccPermitApplicationPersons(OccPermitApplication opa) throws IntegrationException {

        PersonIntegrator pi = getPersonIntegrator();
        OccupancyIntegrator oi = getOccupancyIntegrator();
        PropertyIntegrator pri = getPropertyIntegrator();
        PropertyCoordinator pc = getPropertyCoordinator();
        // TODO Update for humanization
//        
//        List<PersonOccApplication> existingList = pi.getPersonOccApplicationListWithInactive(opa);
//
//        PersonOccApplication applicationPerson = new PersonOccApplication();
//
//        for (PersonOccApplication existingPerson : existingList) {
//
//            boolean removed = true;
//
//            Iterator itr = opa.getAttachedPersons().iterator();
//
//            while (itr.hasNext()) {
//
//                applicationPerson = (PersonOccApplication) itr.next();

                /* If the person  is the applicantPerson on the 
                    OccPermitApplication, set applicant to true*/
//                applicationPerson.setApplicant(opa.getApplicantPerson() != null && opa.getApplicantPerson().equals(applicationPerson));

                /* If the person is the preferredContact on the 
                    OccPermitApplication, set preferredcontact to true */
//                applicationPerson.setPreferredContact(opa.getPreferredContact() != null && opa.getPreferredContact().equals(applicationPerson));
//
//                if (applicationPerson.getPersonID() == 0) {
//
//                    applicationPerson.setPersonType(applicationPerson.getApplicationPersonType());
//
//                    Property prop = pc.getProperty(opa.getApplicationPropertyUnit().getPropertyID());
//
//                    applicationPerson.setMuniCode(prop.getMuni().getMuniCode());
//
//                    applicationPerson.setPersonID(pi.insertPerson(applicationPerson));
//
//                    oi.insertOccApplicationPerson(applicationPerson, opa.getId());

                    //We've inserted this new person to the database already. 
                    //Let's remove them so we don't insert them every time the for loop fires
//                    itr.remove();
//                    break;
//                } else if (applicationPerson.getPersonID() == existingPerson.getPersonID()) {
//                    removed = false;
//
//                    applicationPerson.setLinkActive(true);
//
//                    pi.updatePerson(applicationPerson);
//
//                    oi.updatePersonOccPeriod(applicationPerson, opa);
//
//                    break;
//                }

//            }

//            if (removed == true) {
//
//                //we never found it in the while loop above, it's been removed
//                existingPerson.setLinkActive(false);
//                oi.updatePersonOccPeriod(existingPerson, opa);
//
//            }
//        }
    }

  

    /**
     * For inter-coordinator processing only! I get called by the EvCoor during
     * EventCnF insertion
     *
     * @param evList
     * @param period
     * @param ua
     * @return a reference to the same list that was passed in with any
     * additional events added to the queue for insertion by the
     * EventCoordinator
     * @throws IntegrationException
     */
    protected List<EventCnF> addEvent_processForOccDomain(List<EventCnF> evList, OccPeriod period, UserAuthorized ua) throws IntegrationException {
        // No guts yet!

        return evList;
    }


  
            

} // close class

