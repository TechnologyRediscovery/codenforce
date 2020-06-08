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
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.InspectionException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.CodeElement;
import com.tcvcog.tcvce.entities.Credential;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventRuleSet;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.MunicipalityDataHeavy;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.Proposal;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.occupancy.OccInspectableStatus;
import com.tcvcog.tcvce.entities.occupancy.OccInspectionStatusEnum;
import com.tcvcog.tcvce.entities.occupancy.OccInspectedSpaceElement;
import com.tcvcog.tcvce.entities.occupancy.OccInspectedSpace;
import com.tcvcog.tcvce.entities.occupancy.OccLocationDescriptor;
import com.tcvcog.tcvce.entities.occupancy.OccPermitApplication;
import com.tcvcog.tcvce.entities.occupancy.OccInspection;
import com.tcvcog.tcvce.entities.occupancy.OccAppPersonRequirement;
import com.tcvcog.tcvce.entities.occupancy.OccChecklistTemplate;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsOccChecklistItemsEnum;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodDataHeavy;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodType;
import com.tcvcog.tcvce.entities.occupancy.OccPermit;
import com.tcvcog.tcvce.entities.occupancy.OccSpace;
import com.tcvcog.tcvce.entities.occupancy.OccSpaceElement;
import com.tcvcog.tcvce.entities.occupancy.OccSpaceType;
import com.tcvcog.tcvce.entities.occupancy.OccSpaceTypeInspectionDirective;
import com.tcvcog.tcvce.entities.reports.ReportConfigOccInspection;
import com.tcvcog.tcvce.entities.reports.ReportConfigOccPermit;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccInspectionIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import com.tcvcog.tcvce.entities.IFace_Proposable;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodPropertyUnitHeavy;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodStatusEnum;
import com.tcvcog.tcvce.entities.search.QueryEvent;
import com.tcvcog.tcvce.entities.search.QueryEventEnum;
import com.tcvcog.tcvce.entities.search.QueryPerson;
import com.tcvcog.tcvce.entities.search.QueryPersonEnum;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.occupancy.integration.PaymentIntegrator;

/**
 * King of all business logic implementation for the entire Occupancy object tree
 * the central of which is the Business Object OccPeriod
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
     * Primary retrieval point for extracted OccPeriod objects. Backing beans should not
     * be calling Integrators directly since the Coordinator is responsible for implementing
     * initialization logic usually through a call to configureBObXXXX() method
     * 
     * @param periodID
     * @return
     * @throws IntegrationException 
     */
    public OccPeriod getOccPeriod(int periodID) throws IntegrationException{
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
     * Assembles a special subclass of OccPeriod that contains a PropertyUnitWithProp object for
     * displaying property address info
     * @param periodid
     * @return
     * @throws IntegrationException 
     */
    public OccPeriodPropertyUnitHeavy getOccPeriodPropertyUnitHeavy(int periodid) throws IntegrationException{
        PropertyCoordinator pc = getPropertyCoordinator();
        OccPeriodPropertyUnitHeavy oppu = new OccPeriodPropertyUnitHeavy(getOccPeriod(periodid));
        oppu.setPropUnitProp(pc.getPropertyUnitWithProp(oppu.getPropertyUnitID()));
        return oppu;
    }
    
    public List<OccPeriodPropertyUnitHeavy> getOccPeriodPropertyUnitHeavy(List<OccPeriod> perList) throws IntegrationException{
        List<OccPeriodPropertyUnitHeavy> oppuList = new ArrayList<>();
        for(OccPeriod op: perList){
            oppuList.add(OccupancyCoordinator.this.getOccPeriodPropertyUnitHeavy(op.getPeriodID()));
        }
        
        return oppuList;
        
    }
    
    /**
     * Retrieval point for Data-rich occupancy periods
     * @param per
     * @param cred
     * @return
     * @throws IntegrationException 
     */
    public OccPeriodDataHeavy assembleOccPeriodDataHeavy(OccPeriod per, Credential cred) throws IntegrationException, BObStatusException, SearchException{
        if(per == null || cred == null){
            throw new BObStatusException("Cannot assemble an OccPeriod data heavy without base period or Credential");
        }
        
        OccupancyIntegrator oi = getOccupancyIntegrator();
        OccInspectionIntegrator inspecInt = getOccInspectionIntegrator();
        PaymentIntegrator pai = getPaymentIntegrator();
        WorkflowCoordinator chc = getWorkflowCoordinator();
        SearchCoordinator sc = getSearchCoordinator();
        EventCoordinator ec = getEventCoordinator();
        
        OccPeriodDataHeavy opdh = new OccPeriodDataHeavy(per);
        
        // now get all the lists from their respective integrators
        // this is the Java version of table joins in SQL; we're doing them interatively
        // in our integrators for each BOB
        try {
            per = oi.getOccPeriod(per.getPeriodID());
            // APPLICATION LIST
            opdh.setApplicationList(oi.getOccPermitApplicationList(opdh));

            // PERSON LIST
            QueryPerson qp = sc.initQuery(QueryPersonEnum.OCCPERIOD_PERSONS, cred);
            if(!qp.getParamsList().isEmpty()){
                qp.getParamsList().get(0).setOccPeriod_val(per);
            }
            opdh.setPersonList(sc.runQuery(qp).getBOBResultList());

            // EVENT LIST
            QueryEvent qe = sc.initQuery(QueryEventEnum.OCCPERIOD, cred);
            if(!qe.getParamsList().isEmpty()){
                qe.getParamsList().get(0).setEventDomainPK_val(per.getPeriodID());
            }
            opdh.setEventList(ec.downcastEventCnFPropertyUnitHeavy(qe.getBOBResultList()));

            // PROPOSAL LIST
            opdh.setProposalList(chc.getProposalList(opdh, cred));
            
            // EVENT RULE LIST
            opdh.setEventRuleList(chc.rules_getEventRuleImpList(opdh, cred));
            
            // INSPECTION LIST
            opdh.setInspectionList(inspecInt.getOccInspectionList(opdh));

            // FEE AND PAYMENT LIST
            opdh.setPaymentListGeneral(pai.getPaymentList(opdh));
            opdh.setFeeList(pai.getFeeAssigned(opdh));

            // PERMIT LIST
            opdh.setPermitList(oi.getOccPermitList(opdh));
            // BLOB LIST
            opdh.setBlobIDList(oi.getBlobList(opdh));
            
            opdh.setGoverningInspection(designateGoverningInspection(opdh));
        
        } catch (BObStatusException | SearchException | EventException | AuthorizationException | IntegrationException | ViolationException ex) {
            System.out.println(ex);
        } 
        
        return opdh;
        
    }
    
    
    
    /**
     * Logic container for determining occ period status based on a OPDH
     * 
     * As of Beta 0.9, we're just arbitrarily setting the status to unknown
     * 
     * @param opdh
     * @param cred
     * @return 
     */
    private OccPeriodDataHeavy configureOccPeriodDataHeavy(OccPeriodDataHeavy opdh, Credential cred){
        if(opdh == null){
            return opdh;
        }
        
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
    
    
    public List<OccPeriod> assembleOccPeriodHistoryList(Credential cred){
        OccupancyIntegrator oi = getOccupancyIntegrator();
        
        
        List<OccPeriod> opList = new ArrayList<>();
        try {
            for(Integer i: oi.getOccPeriodHistoryList(cred.getGoverningAuthPeriod().getUserID())){
                    opList.add(getOccPeriod(i));
            }
        } catch (IntegrationException ex) {            
            System.out.println(ex);
        }
        
        
        return opList;
        
    }
    
    
    
    /**
     * TODO: Finish
     * @param period 
     */
    public void configureRuleSet(OccPeriodDataHeavy period){
//        List<EventRuleImplementation> evRuleList = period.getb(ViewOptionsEventRulesEnum.VIEW_ALL);
//        for(EventRuleAbstract era: evRuleList){
//            if(era.getPromptingDirective()!= null){
//                // TODO: Finish
//            }
//        }
    }
    
    public OccInspection configureOccInspection(OccInspection inspection){
        boolean allSpacesPassed = true;
        if (inspection != null) {
            for (OccInspectedSpace inSpace : inspection.getInspectedSpaceList()) {
                if (configureOccInspectedSpace(inSpace).getStatus().getStatusEnum() == OccInspectionStatusEnum.FAIL
                        || configureOccInspectedSpace(inSpace).getStatus().getStatusEnum() == OccInspectionStatusEnum.NOTINSPECTED) {
                    allSpacesPassed = false;
                }
            }
            inspection.setReadyForPassedCertification(allSpacesPassed);
            if(!inspection.getInspectedSpaceList().isEmpty()){
                Collections.sort(inspection.getInspectedSpaceList());
                Collections.reverse(inspection.getInspectedSpaceList());
            }
        }
        return inspection;
    }

    public OccInspectedSpace configureOccInspectedSpace(OccInspectedSpace inSpace) {
        SystemIntegrator si = getSystemIntegrator();
        boolean atLeastOneElementInspected = false;
        boolean allElementsPass = true;

        for (OccInspectedSpaceElement inSpaceEle : inSpace.getInspectedElementList()) {
            configureOccInspectedSpaceElement(inSpaceEle);
            if (inSpaceEle.getStatus().getStatusEnum() == OccInspectionStatusEnum.FAIL) {
                allElementsPass = false;
            } else if (inSpaceEle.getLastInspectedTS() != null) {
                atLeastOneElementInspected = true;
            }
        }

        int iconID = 0;
        try {
            if (!atLeastOneElementInspected) {

                inSpace.setStatus(new OccInspectableStatus(OccInspectionStatusEnum.NOTINSPECTED));
                iconID = Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                        .getString(OccInspectionStatusEnum.NOTINSPECTED.getIconPropertyLookup()));
                inSpace.getStatus().setIcon(si.getIcon(iconID));
//                System.out.println("OccupancyCoordinator.configureOccInspectedSpace | NOTINSPEC inspectedSpaceID: " + inSpace.getInspectedSpaceID());

            } else if (atLeastOneElementInspected && !allElementsPass) {

                inSpace.setStatus(new OccInspectableStatus(OccInspectionStatusEnum.FAIL));
                iconID = Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                        .getString(OccInspectionStatusEnum.FAIL.getIconPropertyLookup()));
                inSpace.getStatus().setIcon(si.getIcon(iconID));
//                System.out.println("OccupancyCoordinator.configureOccInspectedSpace | FAIL inspectedSpaceID: " + inSpace.getInspectedSpaceID());

            } else {

                inSpace.setStatus(new OccInspectableStatus(OccInspectionStatusEnum.PASS));
                iconID = Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                        .getString(OccInspectionStatusEnum.PASS.getIconPropertyLookup()));
                inSpace.getStatus().setIcon(si.getIcon(iconID));
//                System.out.println("OccupancyCoordinator.configureOccInspectedSpace | PASS inspectedSpaceID: " + inSpace.getInspectedSpaceID());
            }
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }

        Collections.sort(inSpace.getInspectedElementList());
        return inSpace;

    }

    public OccInspectedSpaceElement configureOccInspectedSpaceElement(OccInspectedSpaceElement inSpaceEle) {
        SystemIntegrator si = getSystemIntegrator();

        int iconID = 0;

        try {
            if (inSpaceEle.getLastInspectedBy() != null && inSpaceEle.getComplianceGrantedTS() == null) {

                inSpaceEle.setStatus(new OccInspectableStatus(OccInspectionStatusEnum.FAIL));
                iconID = Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                        .getString(OccInspectionStatusEnum.FAIL.getIconPropertyLookup()));
                inSpaceEle.getStatus().setIcon(si.getIcon(iconID));

//                System.out.println("OccupancyCoordinator.configureOccInspectedSpaceEleement | FAIL inspectedSpaceElementID: " + inSpaceEle.getInspectedSpaceID());
            } else if (inSpaceEle.getLastInspectedBy() != null && inSpaceEle.getComplianceGrantedTS() != null) {

                inSpaceEle.setStatus(new OccInspectableStatus(OccInspectionStatusEnum.PASS));
                iconID = Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                        .getString(OccInspectionStatusEnum.PASS.getIconPropertyLookup()));
                inSpaceEle.getStatus().setIcon(si.getIcon(iconID));
//                System.out.println("OccupancyCoordinator.configureOccInspectedSpaceEleement | PASS inspectedSpaceElementID: " + inSpaceEle.getInspectedSpaceID());

            } else {

                inSpaceEle.setStatus(new OccInspectableStatus(OccInspectionStatusEnum.NOTINSPECTED));
                iconID = Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                        .getString(OccInspectionStatusEnum.NOTINSPECTED.getIconPropertyLookup()));
                inSpaceEle.getStatus().setIcon(si.getIcon(iconID));
//                System.out.println("OccupancyCoordinator.configureOccInspectedSpaceEleement | NOT INSPECTED inspectedSpaceElementID: " + inSpaceEle.getInspectedSpaceID());

            }
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        return inSpaceEle;
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
     * Logic container for checking the basic permissions for authorization of an
     * occupancy period. An authorized occupancy period is one for which an 
     * occupancy permit can be issued
     * @param period
     * @param u doing the authorizing; must have code officer permissions
     * @throws AuthorizationException
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    public void authorizeOccPeriod(OccPeriod period, UserAuthorized u) throws AuthorizationException, BObStatusException, IntegrationException {
        OccupancyIntegrator oi = getOccupancyIntegrator();
        if (u.getKeyCard().isHasEnfOfficialPermissions()) {
            // TODO: Figure out occupancy period status and authorization permission
            
            if ( true ) {
                period.setAuthorizedBy(u);
                period.setAuthorizedTS(LocalDateTime.now());
                oi.updateOccPeriod(period);
            } else {
                throw new BObStatusException("Occ period not ready for authorization");
            }
        } else {
            throw new AuthorizationException("Users must have enforcement official permissions to authorize an occupancy period");
        }
    }

    public ReportConfigOccInspection getOccInspectionReportConfigDefault(OccInspection insp,
            OccPeriod period,
            User usr) throws IntegrationException {
        SystemIntegrator si = getSystemIntegrator();

        ReportConfigOccInspection rpt = new ReportConfigOccInspection();
        rpt.setGenerationTimestamp(LocalDateTime.now());
        rpt.setOccPeriod(period);

        rpt.setTitle(getResourceBundle(Constants.MESSAGE_TEXT).getString("report_occinspection_default_title"));
        rpt.setCreator(usr);
        rpt.setMuni(getSessionBean().getSessMuni());

        rpt.setDefaultItemIcon(si.getIcon(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                .getString(OccInspectionStatusEnum.NOTINSPECTED.getIconPropertyLookup()))));

        rpt.setIncludeOccPeriodInfoHeader(true);

        rpt.setIncludePhotos_pass(false);
        rpt.setIncludePhotos_fail(true);

        rpt.setIncludeFullOrdText(false);
        rpt.setIncludeElementNotes(true);

        rpt.setIncludeElementLastInspectedInfo(false);
        rpt.setIncludeElementComplianceInfo(false);

        rpt.setIncludeRemedyInfo(false);
        rpt.setIncludeSignature(false);
        
        rpt.setViewSetting(ViewOptionsOccChecklistItemsEnum.FAILED_ITEMS_ONLY);
        return rpt;
    }

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
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        rpt.setCreator(u);

        return rpt;
    }

    public OccPermit getOccPermitSkeleton(User usr) {
        OccPermit permit = new OccPermit();
        permit.setDateIssued(LocalDateTime.now());
        permit.setIssuedBy(usr);
        return permit;

    }

    public OccLocationDescriptor getOccLocationDescriptorSkeleton() {
        return new OccLocationDescriptor();
    }

    public int addNewLocationDescriptor(OccLocationDescriptor old) throws IntegrationException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        int freshLocID = 0;
        freshLocID = oii.insertLocationDescriptor(old);

        return freshLocID;
    }

    public OccInspection getOccInspectionSkeleton() {
        return new OccInspection();
    }
    
    /**
     * Updates DB to mark the passed in OccInspection the governing one in the 
     * given OccPeriod
     * @param period
     
     * @return the governing Inspection
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public OccInspection designateGoverningInspection(OccPeriodDataHeavy period) throws BObStatusException{
        List<OccInspection> inspectionList = period.getInspectionList();
        OccInspection selIns = null;
        // logic for determining the currentOccInspection
        if(inspectionList != null){
            if(inspectionList.size() == 1){
                selIns = inspectionList.get(0);
            } else {
                Collections.sort(inspectionList);
                for(OccInspection ins: inspectionList){
                    if(ins.isActive()){
                        selIns = ins;
                    }
                }
            }
        }
        try {
            if(period.getGoverningInspection() != null){
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
     * Initialization method for creating a skeleton of an OccPeriod with 
     * sensible default values for first insertion into DB
     * @param p
     * @param pu
     * @param perType
     * @param u
     * @param muni
     * @return
     * @throws IntegrationException 
     */
    public OccPeriod initOccPeriod(         Property p, 
                                            PropertyUnit pu, 
                                            OccPeriodType perType,
                                            User u, 
                                            MunicipalityDataHeavy muni) throws IntegrationException{
        SystemIntegrator si = getSystemIntegrator();
        OccPeriod period = new OccPeriod();

        period.setPropertyUnitID(pu.getUnitID());
        period.setType(perType);

        period.setManager(u);
        period.setCreatedBy(u);
        period.setCreatedTS(LocalDateTime.now());

        period.setStartDate(LocalDateTime.now().plusDays(DEFAULT_OCC_PERIOD_START_DATE_OFFSET));
        period.setStartDateCertifiedBy(null);
        period.setStartDateCertifiedTS(null);
        
        if(period.getStartDate() != null){
            period.setEndDate(period.getStartDate().plusDays(period.getType().getDefaultValidityPeriodDays()));
        }
        period.setEndDateCertifiedBy(null);
        period.setEndDateCertifiedTS(null);

        period.setSource(si.getBOBSource(Integer.parseInt(
                getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                        .getString("occPeriodNewInternalBOBSourceID"))));

        System.out.println("OccupancyCoordinator.intitializeNewOccPeriod | period: " + period);
        return period;
    }
    
    
    /**
     * Primary insertion point for the creation of new OccPeriod objects in the DB
     * The caller  must already have an initialized OccPeriod object to insert
     * 
     * @param op an initialized object which can be retrieved from the method
 initOccPeriod in this class
     * @param u the UserAuthorized requesting the new Period
     * @return the unique ID given to the fresh OccPeriod by the database 
     * @throws IntegrationException
     * @throws InspectionException 
     * @throws com.tcvcog.tcvce.domain.EventException 
     * @throws com.tcvcog.tcvce.domain.AuthorizationException 
     * @throws com.tcvcog.tcvce.domain.ViolationException 
     */
    public int insertNewOccPeriod(OccPeriod op, UserAuthorized u) 
            throws  IntegrationException, 
                    InspectionException, 
                    EventException,
                    AuthorizationException,
                    ViolationException {
        OccupancyIntegrator oi = getOccupancyIntegrator();
        EventIntegrator ei = getEventIntegrator();
        EventCoordinator ec = getEventCoordinator();
        
        int freshOccPeriodID = oi.insertOccPeriod(op); 
        
//        OccPeriodDataHeavy opdh = oi.generateOccPeriodDataHeavy(oi.getOccPeriod(freshOccPeriodID));
//        
//        if(op.getType().getBaseRuleSetID()!= 0){
//            EventRuleSet ers = ei.rules_getEventRuleSet(op.getType().getBaseRuleSetID());
//            ec.rules_attachRuleSet(ers, opdh, u);
//        }
       System.out.println("OccupancyCoordinator.insertNewOccPeriod | freshid: " + freshOccPeriodID);

        return freshOccPeriodID;
    }
    
    
   
    /**
     * Supervises the creation of a new Occupancy Inspection object in the
     * database. The designed flow would be the backing bean calls
     * getOccInspectionSkeleton() and sets member variables on there and then
     * passes it into this method.
     *
     * @param in A skeleton of an OccInspection without an ID number
     * @param tem
     * @param period the OccPeriod to which the OccInspection should be linked
     * @param user The current user who will become the Inspector
     * @return An OccInspection object with the ID given in the DB and a
     * configured Template inside
     * @throws InspectionException
     * @throws IntegrationException
     */
    public OccInspection inspectionAction_commenceOccupancyInspection(  OccInspection in,
                                                                        OccChecklistTemplate tem,
                                                                        OccPeriod period,
                                                                        User user) throws InspectionException, IntegrationException{
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        OccInspection inspec = null;

        if (period.getType().isActive()
                && period.getType().isInspectable()) {

            if (in != null) {
                inspec = in;
            } else {
                inspec = new OccInspection();
            }
            inspec.setOccPeriodID(period.getPeriodID());
            if(tem == null){
                inspec.setChecklistTemplate(oii.getChecklistTemplate(period.getType().getChecklistID()));                
            } else {
                inspec.setChecklistTemplate(tem);
            }
            inspec.setInspector(user);
            inspec.setPacc(generateControlCodeFromTime());
//            if(muni.isEnablePublicOccInspectionTODOs()){
//                inspec.setEnablePacc(true);
//            } else {
//                inspec.setEnablePacc(false);
//            }
            inspec = oii.insertOccInspection(inspec);
        } else {
            throw new InspectionException("Occ period either inactive or uninspectable");
        }
        return inspec;
    }

    /**
     * Called by the backing bean when the user selects a space to start
     * inspecting.
     *
     * @param inspection The current inspection
     * @param u The current user--not necessarily the official Inspector of the
     * OccInspection
     * @param spc The OccSpace pulled from the OccInspectionTemplate list
     * @param initialStatus
     * @param loc A populated location descriptor for this Space. Can be an existing location or an new one
     * @return Containing a List of InspectedCodeElement objects ready to be evaluated
     * @throws IntegrationException 
     */
    public OccInspection inspectionAction_commenceSpaceInspection(  OccInspection inspection, 
                                                                    User u, 
                                                                    OccSpace spc, 
                                                                    OccInspectionStatusEnum initialStatus,
                                                                    OccLocationDescriptor loc) 
                                                                throws IntegrationException{
        OccInspectionIntegrator inspecInt = getOccInspectionIntegrator();

        // Feed the given OccSpace to the constructor of the InspectedSpace
        OccInspectedSpace inspSpace = new OccInspectedSpace(spc);
        // then configure the OccInspectedSpace for first insertion
        inspSpace.setLocation(loc);
        inspSpace.setAddedToChecklistBy(u);
        inspSpace.setAddedToChecklistTS(LocalDateTime.now());
        // We are inspecting all the code elements associated with this space in the checklist template
        ListIterator<OccSpaceElement> elementIterator = spc.getSpaceElementList().listIterator();
        OccInspectedSpaceElement inspEle; // Holds our new objects as we add them to the list
        List<OccInspectedSpaceElement> inElementList = new ArrayList<>();

        // wrap each CodeElement in this space in a InspectedCodeElement blanket to keep it warm
        while (elementIterator.hasNext()) {
            OccSpaceElement ele = elementIterator.next();
            // Create an OccInspectedElement by by passing in a CodeElement using the special constructor
            inspEle = new OccInspectedSpaceElement(ele, ele.getSpaceElementID());
            if(initialStatus == null){
                initialStatus = OccInspectionStatusEnum.NOTINSPECTED;
            }
            switch(initialStatus){
                case FAIL:
                    inspEle.setLastInspectedBy(u);
                    inspEle.setLastInspectedTS(LocalDateTime.now());
                    break;
                case NOTINSPECTED:
                    inspEle.setLastInspectedBy(null);
                    inspEle.setLastInspectedTS(null);
                    break;
                case PASS:
                    inspEle.setLastInspectedBy(u);
                    inspEle.setLastInspectedTS(LocalDateTime.now());
                    inspEle.setComplianceGrantedBy(u);
                    inspEle.setComplianceGrantedTS(LocalDateTime.now());
                    break;
                default:
                    inspEle.setLastInspectedBy(null);
                    inspEle.setLastInspectedTS(null);
                    
            }
            inElementList.add(inspEle);
            // each element in this space gets a reference to the same OccLocationDescriptor object
            if (loc == null) {
                inspSpace.setLocation(inspecInt.getLocationDescriptor(Integer.parseInt(
                        getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                                .getString("locationdescriptor_implyfromspacename"))));
            } else {
                inspSpace.setLocation(loc);
            }
        }

        // Critical moment of injecting an new (i.e. ID-less) InspectedElement into its OccInspectedSpace
        inspSpace.setInspectedElementList(inElementList);

        // With a fully built inspected space, we can record our start of inspection in the DB
        inspSpace = inspecInt.recordCommencementOfSpaceInspection(inspSpace, inspection);
        System.out.println("OccucpancyCoordinator.inpectionAction_commenceSpaceInspection | commenced inspecting of space");

        // now use our convenience method to record Inspection of the space's individual elements
        inspecInt.recordInspectionOfSpaceElements(inspSpace, inspection);
        
        // check sequence by retrieving new inspected space and displaying info
        inspSpace = inspecInt.getInspectedSpace(inspSpace.getSpaceID());
        System.out.println("OccucpancyCoordinator.inpectionAction_commenceSpaceInspection | retrievedInspectedSpaceid= "+inspSpace);
        
        return inspection;
    }

    public void inspectionAction_updateSpaceElementData(OccInspection inspection) throws IntegrationException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();

    }

    public OccPermitApplication getNewOccPermitApplication() {
        OccPermitApplication occpermitapp = new OccPermitApplication();
        occpermitapp.setSubmissionDate(LocalDateTime.now());
        return occpermitapp;
    }

    /**
     * Logic pass through method for updates on the OccPeriod
     * @param period
     * @param u
     * @throws IntegrationException
     * @throws BObStatusException if the OccPeriod is authorized
     */
    public void updateOccPeriod(OccPeriod period, User u) throws IntegrationException, BObStatusException {
        OccupancyIntegrator oi = getOccupancyIntegrator();

        if(period.getAuthorizedTS() != null){
            throw new BObStatusException("Cannot change period type or manager on an authorized period; the period must first be unauthorized");
        }

        oi.updateOccPeriod(period);
    }
    
    /**
     * Attaches a note to the given Occ Period. 
     * @param period whose note field contains the properly formatted note
     * that includes all old note text. This is best done by a call to the 
     * SystemCoordinator's appendNoteBlock() method
     * @throws IntegrationException 
     */
    public void attachNoteToOccPeriod(OccPeriod period) throws IntegrationException{
        OccupancyIntegrator oi = getOccupancyIntegrator();
        oi.updateOccPeriod(period);
    }
    

    public void updateOccInspection(OccInspection is, User u) throws IntegrationException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        oii.updateOccInspection(is);

    }
    
    public void activateOccInspection(OccInspection is) throws IntegrationException{
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        oii.activateOccInspection(is);

    }

    /**
     * Sets boolean requirementSatisfied on an OccPermitApplication based on the
     * application reason, the person requirement for that reason, and the
     * PersonTypes of the Persons attached to the application.
     *
     * @param opa
     */
    public void verifyOccPermitPersonsRequirement(OccPermitApplication opa) {
        boolean isRequirementSatisfied = true;
        OccAppPersonRequirement pr = opa.getReason().getPersonsRequirement();
        List<PersonType> requiredPersonTypes = pr.getRequiredPersonTypes();
        List<Person> applicationPersons = opa.getAttachedPersons();
        List<PersonType> applicationPersonTypes = new ArrayList<>();
        for (Person applicationPerson : applicationPersons) {
            applicationPersonTypes.add(applicationPerson.getPersonType());
        }
        for (PersonType personType : requiredPersonTypes) {
            if (!applicationPersonTypes.contains(personType)) {
                isRequirementSatisfied = false;
            }
        }
        pr.setRequirementSatisfied(isRequirementSatisfied);
    }

    public void inspectionAction_removeSpaceFromChecklist(OccInspectedSpace spc, User u, OccInspection oi) throws IntegrationException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        oii.deleteInspectedSpace(spc);
    }

    public void inspectionAction_recordComplianceWithInspectedElement(OccInspectedSpaceElement oise,
            User u,
            OccInspection oi) throws IntegrationException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();

        oise.setComplianceGrantedBy(u);
        oise.setComplianceGrantedTS(LocalDateTime.now());
        oise.setLastInspectedTS(LocalDateTime.now());
        oise.setLastInspectedBy(u);

        oii.updateInspectedSpaceElement(oise);
    }

    public void clearInspectionOfElement(OccInspectedSpaceElement oise,
            User u,
            OccInspection oi) throws IntegrationException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();

        oise.setComplianceGrantedBy(null);
        oise.setComplianceGrantedTS(null);
        oise.setLastInspectedTS(null);
        oise.setLastInspectedBy(null);

        oii.updateInspectedSpaceElement(oise);
    }

    public void inspectionAction_inspectWithoutCompliance(OccInspectedSpaceElement oise,
            User u,
            OccInspection oi) throws IntegrationException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();

        oise.setComplianceGrantedBy(null);
        oise.setComplianceGrantedTS(null);
        oise.setLastInspectedTS(LocalDateTime.now());
        oise.setLastInspectedBy(u);

        oii.updateInspectedSpaceElement(oise);
    }

    public void evaluateProposal(Proposal proposal,
            IFace_Proposable chosen,
            UserAuthorized u) throws    EventException, 
                                        AuthorizationException, 
                                        BObStatusException, 
                                        IntegrationException {
        
        WorkflowCoordinator wc = getWorkflowCoordinator();
        EventCoordinator ec = getEventCoordinator();
        EventIntegrator ei = getEventIntegrator();
        
        EventCnF propEvent = null;
        
        int insertedEventID = 0;
        
        if (wc.determineProposalEvaluatability(proposal, chosen, u)) {
            // since we can evaluate this proposal with the chosen Proposable, configure members
            proposal.setResponderActual(u);
            proposal.setResponseTS(LocalDateTime.now());
            proposal.setChosenChoice(chosen);

//            TODO ECD: finish proposal infrastructure
            // ask the EventCoord for a nicely formed EventCnF, which we cast to EventCnF
//            propEvent = wc.generateEventDocumentingProposalEvaluation(proposal, chosen, u);
            // insert the event and grab the new ID
//            insertedEventID = attachNewEventToOccPeriod(occPeriod, propEvent, u);
            // go get our new event by ID and inject it into our proposal before writing its evaluation to DB
            proposal.setResponseEvent(ec.getEvent(insertedEventID));
            wc.recordProposalEvaluation(proposal);
        } else {
            throw new BObStatusException("Unable to evaluate proposal due to business rule violation");
        }
    }

    public int attachNewEventToOccPeriod(OccPeriod period, EventCnF ev, User u) throws IntegrationException {
        EventIntegrator ei = getEventIntegrator();
        
        EventCnF oe = new EventCnF();
        oe.setOccPeriodID(period.getPeriodID());
        int insertedEventID = ei.insertEvent(oe);
        return insertedEventID;
    }

    public void editOccEvent(EventCnF ev) throws IntegrationException {
        EventIntegrator ei = getEventIntegrator();
        ei.updateEvent(ev);
    }

    //adding xiaohong Checklistbuilder
    //check
    public List<OccChecklistTemplate> getOccChecklistTemplatelist() throws IntegrationException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        return oii.getOccChecklistTemplatelist();
    }

    public List<OccSpaceTypeInspectionDirective> getOccSpaceTypeList(int checklistid) throws IntegrationException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        return oii.getOccInspecTemplateSpaceTypeList(checklistid);
    }

    public OccSpaceTypeInspectionDirective getOccSpaceTypeSkeleton() {
        return new OccSpaceTypeInspectionDirective(new OccSpaceType());
    }

    public void addNewChecklistSpacetype(int checklistid, OccSpaceTypeInspectionDirective os) throws IntegrationException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        OccSpaceType st = (OccSpaceType) os;
        //Inserting occspacetype table
        oii.insertSpaceType(st);
        int currentSpaceTypeId = oii.getLastInsertSpaceTypeid();
        os.setSpaceTypeID(currentSpaceTypeId);
        //Inserting occchecklistspacetype table
        oii.insertOccChecklistSpaceType(checklistid, os);

    }

    public void updateChecklistSpacetype(OccSpaceTypeInspectionDirective os) throws IntegrationException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        OccSpaceType st = (OccSpaceType) os;
        //Updating occspacetype table
        oii.updateSpaceType(st);
        //Updating occchecklistspacetype table
        oii.updateOccChecklistSpaceType(os);
    }

    public void deleteChecklistSpacetype(OccSpaceTypeInspectionDirective os) throws IntegrationException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
       
        for(OccSpace oss: os.getSpaceList()){
            oii.detachElement(oss.getSpaceID());
        }
        OccSpaceType st = (OccSpaceType) os;
        //Deleting occspace table
        oii.detachSpacefromSpaceType(st);
        //Deleting occchecklistspacetype table
        oii.deleteOccChecklistSpaceType(st);
        //Deleting occspacetype table
        oii.deleteSpaceType(st);
        
        
    }

    public OccSpace getOccSpaceSkeleton() {
        return new OccSpace();
    }

    public void addNewChecklistSpace(OccSpace os, OccSpaceTypeInspectionDirective ost) throws IntegrationException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        int spacetypeid = ost.getSpaceTypeID();
        os.setOccSpaceTypeID(spacetypeid);
        //Inserting space
        oii.insertSpace(os);
    }

    public void updateChecklistSpace(OccSpace os) throws IntegrationException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        //Updating space
        oii.updateSpace(os);
    }

    public void deleteChecklistSpace(OccSpace os) throws IntegrationException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();

        oii.detachElement(os.getSpaceID());
        //Deleting space
        oii.deleteSpace(os);
    }

    public OccSpaceElement getOccSpaceElementSkeleton() {
        return new OccSpaceElement(new CodeElement());
    }

    public void deleteChecklistElement(OccSpace os, int elementid) throws IntegrationException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        CodeIntegrator ci = getCodeIntegrator();
        CodeElement ce = ci.getCodeElement(elementid);
        oii.detachCodeElementFromSpace(os, ce);
    }

    public void createChecklistElement(OccSpace os, int elementid) throws IntegrationException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        CodeIntegrator ci = getCodeIntegrator();
        CodeElement ce = ci.getCodeElement(elementid);
        oii.attachCodeElementToSpace(os, ce);
    }

    public List<OccSpace> getSpacelist(int spacetypeid) throws IntegrationException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        return oii.getOccSpaceList(spacetypeid);
    }

    public OccSpace getSpace(int spaceid) throws IntegrationException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        return oii.getOccSpace(spaceid);
    }

    public OccChecklistTemplate getChecklistTemplate(int checklistid) throws IntegrationException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        return oii.getChecklistTemplate(checklistid);
    }

    public OccSpaceTypeInspectionDirective getSpaceType(int spacetypeid) throws IntegrationException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        return oii.getOccInspecTemplateSpaceType(spacetypeid);
    }

} // close class

