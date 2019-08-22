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
import com.tcvcog.tcvce.domain.CaseLifecycleException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.InspectionException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Choice;
import com.tcvcog.tcvce.entities.CodeElement;
import com.tcvcog.tcvce.entities.Event;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.Icon;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.MunicipalityComplete;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.Proposable;
import com.tcvcog.tcvce.entities.Proposal;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserWithAccessData;
import com.tcvcog.tcvce.entities.occupancy.OccInspectableStatus;
import com.tcvcog.tcvce.entities.occupancy.OccInspectionStatusEnum;
import com.tcvcog.tcvce.entities.occupancy.OccInspectedSpaceElement;
import com.tcvcog.tcvce.entities.occupancy.OccInspectedSpace;
import com.tcvcog.tcvce.entities.occupancy.OccLocationDescriptor;
import com.tcvcog.tcvce.entities.occupancy.OccPermitApplication;
import com.tcvcog.tcvce.entities.occupancy.OccInspection;
import com.tcvcog.tcvce.entities.occupancy.OccAppPersonRequirement;
import com.tcvcog.tcvce.entities.occupancy.OccChecklistTemplate;
import com.tcvcog.tcvce.entities.occupancy.OccEvent;
import com.tcvcog.tcvce.entities.occupancy.OccInspectionViewOptions;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPermit;
import com.tcvcog.tcvce.entities.occupancy.OccSpace;
import com.tcvcog.tcvce.entities.occupancy.OccSpaceElement;
import com.tcvcog.tcvce.entities.occupancy.OccSpaceType;
import com.tcvcog.tcvce.entities.occupancy.OccSpaceTypeInspectionDirective;
import com.tcvcog.tcvce.entities.reports.ReportConfigOccInspection;
import com.tcvcog.tcvce.entities.reports.ReportConfigOccPermit;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccInspectionIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.SysexMessage;

/**
 *
 * @author Eric C. Darsow
 */
public class OccupancyCoordinator extends BackingBeanUtils implements Serializable {

    private final int MINIMUM_RANK_INSPECTOREVENTS = 5;
    private final int MINIMUM_RANK_STAFFEVENTS = 3;
    
    /**
     * Creates a new instance of OccupancyCoordinator
     */
    public OccupancyCoordinator() {
    }
    
    public OccPeriod configureOccPeriod(OccPeriod period, User u) throws EventException, AuthorizationException, IntegrationException, CaseLifecycleException, ViolationException{
        ChoiceCoordinator cc = getChoiceCoordinator();
        EventCoordinator ec = getEventCoordinator();
        period = cc.configureProposals(period, u);
//        if(period.determineGoverningOccInspection().isReadyForPassedCertification() 
//                && ec.evaluateEventRules(period)){
//            period.setReadyForPeriodAuthorization(true);
//        }
        return period;
        
    }
    
    public OccInspection configureOccInspection(OccInspection inspection){
        boolean allSpacesPassed = true;
        if(inspection != null){
            for(OccInspectedSpace inSpace: inspection.getInspectedSpaceList()){
                if(configureOccInspectedSpace(inSpace).getStatus().getStatusEnum() == OccInspectionStatusEnum.FAIL
                        || configureOccInspectedSpace(inSpace).getStatus().getStatusEnum() == OccInspectionStatusEnum.NOTINSPECTED){
                    allSpacesPassed = false;
                }
            }
            inspection.setReadyForPassedCertification(allSpacesPassed);
        }
        return inspection;
    }
    
    public OccInspectedSpace configureOccInspectedSpace(OccInspectedSpace inSpace){
        SystemIntegrator si = getSystemIntegrator();
        boolean atLeastOneElementInspected = false;
        boolean allElementsPass = true;
        
        for(OccInspectedSpaceElement inSpaceEle: inSpace.getInspectedElementList()){
            configureOccInspectedSpaceElement(inSpaceEle);
            if(inSpaceEle.getStatus().getStatusEnum() == OccInspectionStatusEnum.FAIL){
                allElementsPass = false;
            } else if (inSpaceEle.getLastInspectedTS() != null){
                atLeastOneElementInspected = true;
            }
        }
        
        int iconID = 0;
        try {
            if(!atLeastOneElementInspected){
                
                inSpace.setStatus(new OccInspectableStatus(OccInspectionStatusEnum.NOTINSPECTED));
                iconID = Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                        .getString(OccInspectionStatusEnum.NOTINSPECTED.getIconPropertyLookup()));
                inSpace.getStatus().setIcon(si.getIcon(iconID));
//                System.out.println("OccupancyCoordinator.configureOccInspectedSpace | NOTINSPEC inspectedSpaceID: " + inSpace.getInspectedSpaceID());
                
            } else if(atLeastOneElementInspected && !allElementsPass) {
                
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

            } else if (inSpaceEle.getLastInspectedBy() != null && inSpaceEle.getComplianceGrantedTS() != null){

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
        
    public List<EventType> getPermittedEventTypes(OccPeriod op, User u){
        List<EventType> typeList = new ArrayList<>();
        int rnk = u.getRoleType().getRank();
        
        if(rnk >= MINIMUM_RANK_INSPECTOREVENTS){
            typeList.add(EventType.Action);
            typeList.add(EventType.Timeline);
        }
        
        if(rnk >= MINIMUM_RANK_STAFFEVENTS){
            typeList.add(EventType.Communication);
            typeList.add(EventType.Meeting);
            typeList.add(EventType.Custom);
            typeList.add(EventType.Occupancy);
        }
        return typeList;
    }
    
    public void updateOccPeriodPropUnit(OccPeriod period, PropertyUnit pu) throws IntegrationException{
        OccupancyIntegrator oi = getOccupancyIntegrator();
        period.setPropertyUnitID(pu.getUnitID());
        oi.updateOccPeriod(period);
    }
    
    public void authorizeOccPeriod(OccPeriod period, UserWithAccessData u) throws AuthorizationException, CaseLifecycleException, IntegrationException{
        OccupancyIntegrator oi = getOccupancyIntegrator();
        if(u.getKeyCard().isHasEnfOfficialPermissions()){
            if(period.isReadyForPeriodAuthorization()){
                period.setAuthorizedBy(u);
                period.setAuthorizedTS(LocalDateTime.now());
                oi.updateOccPeriod(period);
            } else {
                throw new CaseLifecycleException("Occ period not ready for authorization");
            }
        } else{
            throw new AuthorizationException("Users must have enforcement official permissions to authorize an occupancy period");
        }
    }
    

    public ReportConfigOccInspection getOccInspectionReportConfigDefault(   OccInspection insp, 
                                                                            OccPeriod period,
                                                                            User usr) throws IntegrationException{
        SystemIntegrator si = getSystemIntegrator();

        ReportConfigOccInspection rpt = new ReportConfigOccInspection();
        rpt.setOccPeriod(period);
        
        rpt.setTitle(getResourceBundle(Constants.MESSAGE_TEXT).getString("report_occinspection_default_title"));
        rpt.setCreator(usr);
        rpt.setMuni(getSessionBean().getSessionMuni());
        
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
        
        rpt.setViewSetting(OccInspectionViewOptions.FAILED_ITEMS_ONLY);
        return rpt;
    }
    
    
    public ReportConfigOccPermit getOccPermitReportConfigDefault(   OccPermit permit,
                                                                    OccPeriod period,
                                                                    PropertyUnit propUnit,
                                                                    User u){
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
    
    public OccPermit getOccPermitSkeleton(User usr){
        OccPermit permit = new OccPermit();
        permit.setDateIssued(LocalDateTime.now());
        permit.setIssuedBy(usr);
        return permit;
        
    }
    
    public OccLocationDescriptor getOccLocationDescriptorSkeleton(){
        return new OccLocationDescriptor();
    }
    
    public int addNewLocationDescriptor(OccLocationDescriptor old) throws IntegrationException{
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        int freshLocID = 0;
            freshLocID  = oii.insertLocationDescriptor(old);
        
        return freshLocID;
    }
    
    public OccInspection getOccInspectionSkeleton(){
        return new OccInspection();
    }
    
    public OccPeriod initializeNewOccPeriod(Property p, PropertyUnit pu, User u, MunicipalityComplete muni) throws IntegrationException{
        SystemIntegrator si = getSystemIntegrator();
        OccPeriod op = new OccPeriod();
        
        op.setPropertyUnitID(pu.getUnitID());
        op.setType(muni.getProfile().getOccPeriodTypeList().get(0));
        op.setManager(u);
        op.setCreatedBy(u);
        op.setCreatedTS(LocalDateTime.now());
        
        op.setStartDate(LocalDateTime.now());
        op.setStartDateCertifiedBy(u);
        op.setStartDateCertifiedTS(LocalDateTime.now());
        
        op.setEndDate(op.getStartDate().plusDays(op.getType().getDefaultValidityPeriodDays()));
        op.setEndDateCertifiedBy(u);
        op.setEndDateCertifiedTS(LocalDateTime.now());
        
        op.setSource(si.getBOBSource(Integer.parseInt(
                getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                .getString("occPeriodNewInternalBOBSourceID"))));
        
        return op;
    }
    
    public int insertNewOccPeriod(OccPeriod op, User u) throws IntegrationException{
        OccupancyIntegrator oi = getOccupancyIntegrator();
        return oi.insertOccPeriod(op);
        
    }
    
    /**
     * Supervises the creation of a new Occupancy Inspection object in the database.
     * The designed flow would be the backing bean calls getOccInspectionSkeleton() and 
     * sets member variables on there and then passes it into this method.
     * 
     * @param in A skeleton of an OccInspection without an ID number
     * @param period the OccPeriod to which the OccInspection should be linked
     * @param templ The template from which the Inspection will draw its SpaceTypes
     * @param user The current user who will become the Inspector
     * @param muni The current Muni
     * @return An OccInspection object with the ID given in the DB and a configured Template inside
     * @throws InspectionException
     * @throws IntegrationException 
     */
    public OccInspection inspectionAction_commenceOccupancyInspection(   OccInspection in,
                                                        OccPeriod period,
                                                        OccChecklistTemplate templ, 
                                                        User user,
                                                        Municipality muni) throws InspectionException, IntegrationException{
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        OccInspection inspec = null;
        
        if(period.getType().isActive()
                &&
            period.getType().isInspectable()){
            
            if(in != null){
                inspec = in;
            } else {
                inspec = new OccInspection();
            }
            inspec.setOccPeriodID(period.getPeriodID());
            inspec.setChecklistTemplate(templ);
            inspec.setInspector(user);
            inspec.setPacc(generateControlCodeFromTime());
//            if(muni.isEnablePublicOccInspectionTODOs()){
//                inspec.setEnablePacc(true);
//            } else {
//                inspec.setEnablePacc(false);
//            }
            inspec = oii.insertOccInspection(in);
        } else {
            throw new InspectionException("Occ period either inactive or uninspectable");
        }
        return inspec;
    }
    
   
    
    
    /**
     * Called by the backing bean when the user selects a space to start inspecting.
     * 
     * @param inspection The current inspection
     * @param u The current user--not necessarily the official Inspector of the OccInspection
     * @param spc The OccSpace pulled from the OccInspectionTemplate list
     * @param loc A populated location descriptor for this Space. Can be an existing location or an new one
     * @return Containing a List of InspectedCodeElement objects ready to be evaluated
     * @throws IntegrationException 
     */
    public OccInspection inspectionAction_commenceSpaceInspection(  OccInspection inspection, 
                                                                    User u, 
                                                                    OccSpace spc, 
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
        while(elementIterator.hasNext()){
            OccSpaceElement ele = elementIterator.next();
            // Create an OccInspectedElement by by passing in a CodeElement using the special constructor
            inspEle = new OccInspectedSpaceElement(ele, ele.getSpaceElementID());
            inspEle.setLastInspectedBy(u);
            inElementList.add(inspEle);
            // each element in this space gets a reference to the same OccLocationDescriptor object
            if(loc == null){
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
        
        inspSpace = inspecInt.getInspectedSpace(inspSpace.getSpaceID());
        System.out.println("OccucpancyCoordinator.inpectionAction_commenceSpaceInspection | retrievedInspectedSpaceid="+inspSpace);
        
        return inspection;
    }    
    
    
    public void inspectionAction_updateSpaceElementData(OccInspection inspection) throws IntegrationException{
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        
    }
    
    public OccPermitApplication getNewOccPermitApplication(){
        OccPermitApplication occpermitapp = new OccPermitApplication();        
        occpermitapp.setSubmissionDate(LocalDateTime.now());        
        return occpermitapp;       
    }
    
    public void updateOccInspection(OccInspection is, User u) throws IntegrationException{
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        oii.updateOccInspection(is);
        
    }
    
    public void activateOccInspection(OccInspection is, User u) throws IntegrationException{
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        oii.activateOccInspection(is);
        
    }
    
    
    /**
     * Sets boolean requirementSatisfied on an OccPermitApplication based on the application reason,
     * the person requirement for that reason, and the PersonTypes of the Persons attached to the 
     * application.
     * @param opa 
     */
    public void verifyOccPermitPersonsRequirement (OccPermitApplication opa){
        boolean isRequirementSatisfied = true;
        OccAppPersonRequirement pr = opa.getReason().getPersonsRequirement();        
        List<PersonType> requiredPersonTypes = pr.getRequiredPersonTypes();
        List<Person> applicationPersons = opa.getAttachedPersons();
        List<PersonType> applicationPersonTypes = new ArrayList<>();
        for (Person applicationPerson:applicationPersons){
            applicationPersonTypes.add(applicationPerson.getPersonType());
        }
        for (PersonType personType:requiredPersonTypes) {
            if (!applicationPersonTypes.contains(personType)){
                isRequirementSatisfied = false;               
            }
        }
        pr.setRequirementSatisfied(isRequirementSatisfied);
    }
    
    public void inspectionAction_removeSpaceFromChecklist(OccInspectedSpace spc, User u, OccInspection oi) throws IntegrationException{
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        oii.deleteInspectedSpace(spc);
    }
    
    public void inspectionAction_recordComplianceWithInspectedElement(   OccInspectedSpaceElement oise, 
                                                        User u, 
                                                        OccInspection oi) throws IntegrationException{
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        
        oise.setComplianceGrantedBy(u);
        oise.setComplianceGrantedTS(LocalDateTime.now());
        oise.setLastInspectedTS(LocalDateTime.now());
        oise.setLastInspectedBy(u);
        
        oii.updateInspectedSpaceElement(oise);
    }
    
    
    public void clearInspectionOfElement(   OccInspectedSpaceElement oise, 
                                            User u, 
                                            OccInspection oi) throws IntegrationException{
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        
        oise.setComplianceGrantedBy(null);
        oise.setComplianceGrantedTS(null);
        oise.setLastInspectedTS(null);
        oise.setLastInspectedBy(null);
        
        oii.updateInspectedSpaceElement(oise);
    }
    
    public void inspectionAction_inspectWithoutCompliance(   OccInspectedSpaceElement oise, 
                                            User u, 
                                            OccInspection oi) throws IntegrationException{
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        
        oise.setComplianceGrantedBy(null);
        oise.setComplianceGrantedTS(null);
        oise.setLastInspectedTS(LocalDateTime.now());
        oise.setLastInspectedBy(u);
        
        oii.updateInspectedSpaceElement(oise);
    }
    
     public void evaluateProposal(   Proposal proposal, 
                                    Proposable chosen, 
                                    OccPeriod occPeriod, 
                                    User u) throws EventException, AuthorizationException, CaseLifecycleException, IntegrationException{
        ChoiceCoordinator cc = getChoiceCoordinator();
        EventCoordinator ec = getEventCoordinator();
        EventIntegrator ei = getEventIntegrator();
        OccEvent propEvent = null;
        int insertedEventID = 0;
        if(cc.determineProposalEvaluatability(proposal, chosen, u)){
            // since we can evaluate this proposal with the chosen Proposable, configure members
            proposal.setResponderActual(u);
            proposal.setResponseTimestamp(LocalDateTime.now());
            proposal.setChosenChoice(chosen);
            
            // ask the EventCoord for a nicely formed Event, which we cast to OccEvent
            propEvent = new OccEvent(ec.generateEventDocumentingProposalEvaluation(proposal, chosen, u));
            // insert the event and grab the new ID
            insertedEventID = attachNewEventToOccPeriod(occPeriod, propEvent, u);
            // go get our new event by ID and inject it into our proposal before writing its evaluation to DB
            proposal.setResponseEvent(ei.getOccEvent(insertedEventID));
            cc.recordProposalEvaluation(proposal);
        } else {
            throw new CaseLifecycleException("Unable to evaluate proposal due to business rule violation");
        }
    }
    
    public int attachNewEventToOccPeriod(OccPeriod period, Event ev, User u) throws IntegrationException{
        EventIntegrator ei = getEventIntegrator();
        
        OccEvent oe = new OccEvent(ev);
        oe.setOccPeriodID(period.getPeriodID());
        int insertedEventID = ei.insertEvent(oe);
        return insertedEventID;
    }
    
    public void editOccEvent(OccEvent ev) throws IntegrationException{
        EventIntegrator ei = getEventIntegrator();
        ei.updateEvent(ev);
    }
} // close class


