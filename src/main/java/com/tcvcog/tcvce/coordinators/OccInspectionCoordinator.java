/*
 * Copyright (C) 2021 Technology Rediscovery LLC
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
import com.tcvcog.tcvce.entities.CECasePropertyUnitHeavy;
import com.tcvcog.tcvce.entities.CodeSet;
import com.tcvcog.tcvce.entities.DomainEnum;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.IFace_inspectable;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.MunicipalityDataHeavy;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.occupancy.*;
import com.tcvcog.tcvce.entities.reports.ReportConfigOccInspection;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccChecklistIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccInspectionIntegrator;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsOccChecklistItemsEnum;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Business logic reservoir for field inspections, formerly called Occupancy Inspections
 * but since field inspections are used for both silos, that distinction is now moot
 * @author Ellen Bascomb of apt 31Y
 */
public class OccInspectionCoordinator extends BackingBeanUtils implements Serializable {

    final static String NO_ELEMENT_CATEGORY_TITLE = "Uncategorized";
    final static String SPACE = " ";
    final static boolean COMMENCE_SPACE_WITH_DEF_FAIL_FINDINGS = false;
    final static int DEFAULT_PHOTO_WIDTH = 600;
    
    
    
    public OccInspectionCoordinator() {
    }
    
    /**
     * Factory of occupnacy inspections
     * @return 
     */
    public FieldInspection getOccInspectionSkeleton() {
        return new FieldInspection();
    }

    /**
     * STEP 1 of occupancy process. THe UI must give the user a list of
     * OccChecklistTemplate objects
     * <p>
     * Residential House
     * Commericial Building
     * <p>
     * <p>
     * Under that
     * Supervises the creation of a new Occupancy Inspection object in the
     * database. The designed flow would be the backing bean calls
     * getOccInspectionSkeleton() and sets member variables on there and then
     * passes it into this method.
     *
     * @param in     A skeleton of an FieldInspection without an ID number
     * @param tem
     * @param inspectable
     * @param user   The current user who will become the Inspector
     * @return An FieldInspection object with the ID given in the DB and a
 configured Template inside
     * @throws InspectionException
     * @throws IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.BlobException
     */
    public FieldInspection inspectionAction_commenceOccupancyInspection(FieldInspection in,
                                                                      OccChecklistTemplate tem,
                                                                      IFace_inspectable inspectable,
                                                                      User user) 
            throws InspectionException, IntegrationException, BObStatusException, BlobException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        OccChecklistIntegrator oci = getOccChecklistIntegrator();
        FieldInspection inspec = null;

        if(inspectable == null || user == null){
            throw new BObStatusException("cannot commence inspection with null template, inspectable, or user");
        }
        
        if (in != null) {
            inspec = in;
        } else {
            inspec = new FieldInspection();
        }
        switch(inspectable.getDomainEnum()){
            case OCCUPANCY:
                inspec.setOccPeriodID(inspectable.getHostPK());
                inspec.setDomainEnum(DomainEnum.OCCUPANCY);
                break;
            case CODE_ENFORCEMENT:
                inspec.setCecaseID(inspectable.getHostPK());
                inspec.setDomainEnum(DomainEnum.CODE_ENFORCEMENT);
                break;
            case UNIVERSAL:
                throw new BObStatusException("Cannot initiate new inspection with domain enum set to UNIVERSAL");
        }
        
        if (tem == null) {
//            inspec.setChecklistTemplate(oci.getChecklistTemplate(period.getType().getChecklistID()));
                throw new BObStatusException("Instance of occ inspection template required; bi-domain compatibility can't extract a template");
        } else {
            inspec.setChecklistTemplate(tem);
        }
        inspec.setInspector(user);
        inspec.setPacc(generateControlCodeFromTime(user.getHomeMuniID()));
        inspec.setEffectiveDateOfRecord(LocalDateTime.now());
        inspec = getOccInspection(oii.insertOccInspection(inspec));
        return inspec;
    }

    
    /**
     * Logic container for updating space element data on all elements in an inspection
     
     * @param osi
     * @param ua
     * @param oi
     * @param statusFilter if provided, this method will filter by this status and only update ordinances coming in with that status
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.AuthorizationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public void inspectionAction_updateSpaceElementData(OccInspectedSpace osi, UserAuthorized ua, FieldInspection oi, OccInspectionStatusEnum statusFilter) 
            throws IntegrationException, AuthorizationException, BObStatusException {
        if(osi != null && osi.getInspectedElementList() != null && !osi.getInspectedElementList().isEmpty()){
            for(OccInspectedSpaceElement oise: osi.getInspectedElementList()){
                if(statusFilter != null){
                    if(oise.getStatusEnum() == statusFilter){
                        inspectionAction_recordElementInspectionByStatusEnum(oise, ua, oi, COMMENCE_SPACE_WITH_DEF_FAIL_FINDINGS);
                    }
                } else {
                    inspectionAction_recordElementInspectionByStatusEnum(oise, ua, oi, COMMENCE_SPACE_WITH_DEF_FAIL_FINDINGS);
                }
            }
        }
    }
    
    public void inspectionActino_updateSpaceElement(OccInspectedSpaceElement oise, FieldInspection fin) 
            throws IntegrationException, BObStatusException{
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        if(oise == null || fin == null){
            throw new BObStatusException("Cannot update an inspectd space element with null element or inspection ");
        }
        oii.updateInspectedSpaceElement(oise);
    }

    /**
     * Called by the backing bean when the user selects a space to start
     * inspecting.
     *
     * Effectively creates a new OccInspectedSpace object, fills out some fields automatically,
     * adds it to the passed inspection object, and
     *
     * @param inspection    The current inspection
     * @param user          The current user--not necessarily the official inspector of the
                      FieldInspection.
     * @param tpe          The space type which will have a list of SpaceElements inside it
     * @param initialStatus The initial status of the created OccInspectedSpace
     * @param locDesc       A populated location descriptor for the new OccInspectedSpace. Can be an
     *                      existing location or a new one.
     *
     * @return The sapce type passed in turned into an inspected space 
     * with a DB ID
     *
     * @throws IntegrationException
     */
    public OccInspectedSpace inspectionAction_commenceInspectionOfSpaceTypeChecklistified  (FieldInspection inspection,
                                                                                        User user,
                                                                                        OccSpaceTypeChecklistified tpe,
                                                                                        OccInspectionStatusEnum initialStatus,
                                                                                        OccLocationDescriptor locDesc) 
                                                                                throws IntegrationException {

        OccInspectionIntegrator oii = getOccInspectionIntegrator();

        // Default value for location descriptor if null
        if (locDesc == null) {
            locDesc = oii.getLocationDescriptor(Integer.parseInt(
                    getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                            .getString("locationdescriptor_implyfromspacename")));
        }

        // Create new inspected space and populate fields
        OccInspectedSpace inspectedSpace = new OccInspectedSpace();

        inspectedSpace.setLocation(locDesc);

        inspectedSpace.setAddedToChecklistBy(user);
        // actually this is stamped by now() in db
//        inspectedSpace.setAddedToChecklistTS(LocalDateTime.now());

        // Wrap each CodeElement in an InspectedCodeElement blanket to keep it warm :)
        List<OccInspectedSpaceElement> inspectedElements;
        inspectedElements = tpe.getCodeElementList().stream().map(element -> {
            OccInspectedSpaceElement inspectedElement = new OccInspectedSpaceElement(element);

                switch (initialStatus) {
                    case VIOLATION:
                        inspectionAction_configureElementForInspectionNoCompliance( 
                                            inspectedElement, 
                                            user, 
                                            inspection, 
                                            COMMENCE_SPACE_WITH_DEF_FAIL_FINDINGS);
                        break;
                    case PASS:
                        inspectionAction_configureElementForCompliance(inspectedElement, user, inspection);
                        break;
                    default:
                        inspectionAction_configureElementForNotInspected(inspectedElement, user, inspection);
                }
                inspectedElement.setMigrateToCaseOnFail(true);
                return inspectedElement;
            }).collect(Collectors.toList());

        inspectedSpace.setInspectedElementList(inspectedElements);
        inspectedSpace.setType(tpe);
        // With a fully built inspected space, we can record our start of inspection in the DB
        inspectedSpace = oii.recordCommencementOfSpaceInspection(inspectedSpace, inspection);
        System.out.println("OccupancyCoordinator.inspectionAction_commenceSpaceInspection | commenced inspecting of space");

        // now use our convenience method to initiate Inspection of the space's individual elements
        oii.recordInspectionOfSpaceElements(inspectedSpace, inspection);

        // check sequence by retrieving new inspected space and displaying info
//        inspectedSpace = oii.getInspectedSpace(inspectedSpace.getInspectedSpaceID());
        System.out.println("OccupancyCoordinator.inspectionAction_commenceSpaceInspection | retrievedInspectedSpaceID= " + inspectedSpace);

        return inspectedSpace;
    }
    
    /**
     * Sets the certified fields on an inspection and injects a determination;
     * Implements logic to ensure proper privileges and throws exceptions
     * with useful messages inside
     * 
     * @param oi to certify
     * @param ua doing the certification (must be OP manager or sys admin or better)
     * @param insptable
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.AuthorizationException
     */
    public void inspectionAction_certifyInspection( FieldInspection oi,
                                                    UserAuthorized ua,
                                                    IFace_inspectable insptable) 
            throws IntegrationException, BObStatusException, AuthorizationException{
        
        if(oi == null || oi.getDetermination() == null || ua == null || insptable == null || insptable.getManager() == null){
            throw new BObStatusException("Cannot certify with null inspection, determination, user, period, or manager!");
        }
        
        if(ua.getUserID() != insptable.getManager().getUserID() || !ua.getKeyCard().isHasSysAdminPermissions()){
            throw new AuthorizationException("Given user cannot certify this inspection because user is not period manager or does not have sys admin or better permissions");
        }
      
        oi.setDeterminationBy(ua);
        oi.setDeterminationTS(LocalDateTime.now());
        
        updateOccInspection(oi, ua);
              
    }

    /**
     * Logic containe for retrieving and configuring an FieldInspection
     * @param inspectionID
     * @return
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public FieldInspection getOccInspection(int inspectionID) throws IntegrationException, BObStatusException, BlobException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        BlobCoordinator bc = getBlobCoordinator();
        
        
        // get the base object with non-list members
        FieldInspection oi = oii.getOccInspection(inspectionID);
        
        // setup our lists
        oi.setChecklistTemplate(getChecklistTemplate(oi.getChecklistTemplateID()));
        oi.setInspectedSpaceList(oii.getInspectedSpaceList(oi.getInspectionID()));
        oi.setBlobList(bc.getBlobLightList(oi));
        oi = configureOccInspection(oi);
        return oi;
    }
    
    
    /**
     * Configures members and updates an existing record in the 
     * occinspection table. The beast!
     * 
     * @param oi with changed fields. This method injects lastupdatedby stuff
     * @param ua the user doing the updates
     */
    public void updateOccInspection(FieldInspection oi, UserAuthorized ua) throws IntegrationException, BObStatusException{
      OccInspectionIntegrator oii = getOccInspectionIntegrator();
      if(oi == null || oi.getInspectionID() == 0 || ua == null){
          throw new BObStatusException("Cannot update an inspection with null inspection or ID of 0 or null use");
      }
      oi.setLastUpdatedBy(ua);
      // database stamps update TS with now()
      oii.updateOccInspection(oi);
    }
    
    /**
     * Logic container for ensuring that deactivation is allowed
     * @param ua doing the deactivating; must be either the inspector or OP manager
     * @param oi to be deactivated
     * @param inspectable
     * @throws com.tcvcog.tcvce.domain.BObStatusException for null of any input param
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.AuthorizationException inputted ua must be the manager or inspector or have sys admin or better permissions
     */
    public void deactivateOccInspection(UserAuthorized ua, FieldInspection oi, IFace_inspectable inspectable) 
            throws BObStatusException, IntegrationException, AuthorizationException{
        if(ua == null || oi == null || oi.getInspector() == null){
            throw new BObStatusException("Cannot deactivate an inspection with null user, inspec, or period, or their manager/inspector!");
            
        }
        if(verifyUserAuthorizationForInspectionActions(ua, oi, inspectable)) {
            if(oi.getDetermination() != null){
                throw new BObStatusException("A field inspection that has already been finalized cannot be deactivated. Remove finalization first if you must deactivate.");
            }
            OccInspectionIntegrator oii = getOccInspectionIntegrator();
            oi.setDeactivatedBy(ua);
            oi.setDeactivatedTS(LocalDateTime.now());
            oii.updateOccInspection(oi);
        } else {
            throw new AuthorizationException("Cannot deactivate an occ inspection unless user is the manager of period, the inspector, or a sys admin or better");
        }
    }
    
    
    /**
     * Removes the values for the given FieldInspection's determination, detTS, and DetUser
     * @param ua
     * @param oi
     * @param inspectable
     * @param op 
     */
    public void removeOccInspectionFinalization(UserAuthorized ua, FieldInspection oi, IFace_inspectable inspectable) throws IntegrationException, BObStatusException{
        if(verifyUserAuthorizationForInspectionActions(ua, oi, inspectable)){
            System.out.println("OccInspectionCoordinator.removeOccInspectionFinalization | passed checks, about to remove finalization for inspection ID " + oi.getInspectionID());
            oi.setDetermination(null);
            oi.setDeterminationBy(null);
            oi.setDeterminationTS(null);
            updateOccInspection(oi, ua);
        }
    }
    
    
    /**
     * Logic container for checking that an inspection
     * can be modified by a given user on our particular occ period
     * 
     * @return true if the user has authorization; false if the user should be prohibited
     * from undertaking action
     */
    private boolean verifyUserAuthorizationForInspectionActions(UserAuthorized ua,
                                                                FieldInspection fin, 
                                                                IFace_inspectable inspectable){
        
        boolean auth = false;
        if(ua != null && fin != null && fin.getInspector() != null && inspectable != null && inspectable.getManager() != null){
            if(ua.getKeyCard().isHasSysAdminPermissions() || ua.getUserID() == fin.getInspector().getUserID() || ua.getUserID() == inspectable.getManager().getUserID() ){
                // domain enum not working for deac
//                if(fin.getDomainEnum() != null && fin.getDomainEnum() == inspectable.getDomainEnum()){
                    auth = true;
//                }
            }
        }
        
        return auth;
    }

    /**
     * Assembles a list of all OccInspections for a given instance of IFace_inspectable
     * @param inspectable
     * @return
     * @throws IntegrationException
     * @throws BObStatusException 
     * @throws com.tcvcog.tcvce.domain.BlobException 
     */
    public List<FieldInspection> getOccInspectionList(IFace_inspectable inspectable) 
            throws IntegrationException, BObStatusException, BlobException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        if(inspectable == null){
            throw new BObStatusException("Cannot retrieve inspections from null inspectable");
        }
        List<Integer> inspectionIDList = oii.getOccInspectionList(inspectable);

        List<FieldInspection> inspectionList = new ArrayList();
        for (Integer id : inspectionIDList) {
            inspectionList.add(getOccInspection(id));
        }

        return inspectionList;
    }

    /**
     * Logic for setting members on the Occupancy Inspection objects
     * 
     * @param inspection
     * @return
     * @throws BObStatusException 
     */
    private FieldInspection configureOccInspection(FieldInspection inspection) throws BObStatusException, IntegrationException {
        boolean allSpacesPassed = true;
        BlobCoordinator bc = getBlobCoordinator();
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        if (inspection == null) {
            throw new BObStatusException("Cannot configure a null inspection");
        }
        if(inspection.getOccPeriodID() != 0 && inspection.getCecaseID() != 0){
            throw new BObStatusException("Inspection has nonzero occ period ID and cecase ID! One and only one may be zero.");
        }
        if(inspection.getOccPeriodID() != 0){
            inspection.setDomainEnum(DomainEnum.OCCUPANCY);
        } else {
            inspection.setDomainEnum(DomainEnum.CODE_ENFORCEMENT);
        }
        
        for (OccInspectedSpace inSpace : inspection.getInspectedSpaceList()) {
            configureOccInspectedSpace(inSpace);
            if (inSpace.getStatus().getStatusEnum() == OccInspectionStatusEnum.VIOLATION
                    || inSpace.getStatus().getStatusEnum() == OccInspectionStatusEnum.NOTINSPECTED) {
                allSpacesPassed = false;
            }
        }
        
        // check for dispatches and load if necessary
        inspection.setDispatch(oii.getOccInspectionDispatch(oii.getOccInspectionDispatchByInspection(inspection)));

        inspection.setReadyForPassedCertification(allSpacesPassed);
        if (!inspection.getInspectedSpaceList().isEmpty()) {
            // disable and debug later
//            Collections.sort(inspection.getInspectedSpaceList());
//            Collections.reverse(inspection.getInspectedSpaceList());
        }
        

        return inspection;
    }
    
    /**
     * Generator for dispatch objects less an ID which makes them skeletons
     * @param fin
     * @param ua
     * @return 
     */
    public OccInspectionDispatch getOccInspectionDispatchSkeleton(FieldInspection fin, UserAuthorized ua){
        OccInspectionDispatch oid = new OccInspectionDispatch();
        if(fin != null){
            oid.setInspectionID(fin.getInspectionID());
        }
        return oid;
        
        
    }
    
    /**
     * Basic getter for dispatches
     * @param did
     * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public OccInspectionDispatch getOccInspectionDispatch(int did) throws IntegrationException, BObStatusException{
        OccInspectionIntegrator oic = getOccInspectionIntegrator();
        return oic.getOccInspectionDispatch(did);
        
    }
    
    
    /**
     * Logic intermediary for dispatch insertion
     * @param fin which is being dispatched
     * @param oid
     * @param ua doing the actual dispatching
     * @return
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public int insertOccInspectionDispatch(FieldInspection fin, OccInspectionDispatch oid, UserAuthorized ua) throws IntegrationException, BObStatusException{
        if(oid == null || fin == null){
            throw new IntegrationException("Cannot insert null inspection dispatch and cannot do so with null FIN");
        }
        
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        
        if(fin.getDispatch() != null && fin.getDispatch().getDispatchID() != 0){
            throw new BObStatusException("The given field inspection contains a dispatch with an ID already!");
        }
        
        oid.setInspectionID(fin.getInspectionID());
        
        oid.setCreatedBy(ua);
        oid.setLastUpdatedBy(ua);
        
        return oii.insertOccInspectionDispatch(oid);
    }
    
    /**
     * Updates a record in the occinspectiondispatch table
     * @param oid
     * @param ua 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void updateOccInspectionDispatch(OccInspectionDispatch oid, UserAuthorized ua) throws BObStatusException, IntegrationException{
        if(oid == null || ua == null){
            throw new BObStatusException("Cannot update dispatch with null dispatch or user");            
        }
        
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
         
        oid.setLastUpdatedBy(ua);
        oii.updateOccInspectionDispatch(oid);
        
        
    }
    
    
    /**
     * Logic block to set deactivation TS and user on a dispatch
     * @param oid to deactivate
     * @param ua doing the deactivation
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void deactivateOccInspectionDispatch(OccInspectionDispatch oid, UserAuthorized ua) throws BObStatusException, IntegrationException{
        if(oid == null || ua == null){
            throw new BObStatusException("Cannot deactivate dispatch with null dispatch or user");            
        }
        
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        oid.setDeactivatedBy(ua);
        oid.setDeactivatedTS(LocalDateTime.now());
        oid.setLastUpdatedBy(ua);
        
        oii.updateOccInspectionDispatch(oid);
        
        
    }

    /**
     * Logic for setting members on OccInspectedSpace on extraction from DB
     * @param inSpace
     * @return
     * @throws BObStatusException 
     */
    private OccInspectedSpace configureOccInspectedSpace(OccInspectedSpace inSpace) throws BObStatusException {
        SystemIntegrator si = getSystemIntegrator();
        boolean atLeastOneElementInspected = false;
        boolean allElementsPass = true;
        
        Map<OccInspectionStatusEnum, List<OccInspectedSpaceElement>> elbsm = new HashMap<>();
        elbsm.put(OccInspectionStatusEnum.PASS, new ArrayList<>());
        elbsm.put(OccInspectionStatusEnum.VIOLATION, new ArrayList<>());
        elbsm.put(OccInspectionStatusEnum.NOTINSPECTED, new ArrayList<>());
              
        for (OccInspectedSpaceElement inSpaceEle : inSpace.getInspectedElementList()) {
            configureOccInspectedSpaceElement(inSpaceEle);
            switch(inSpaceEle.getStatusEnum()){
                case VIOLATION:
                    allElementsPass = false;
                    elbsm.get(OccInspectionStatusEnum.VIOLATION).add(inSpaceEle);
                    atLeastOneElementInspected = true;
                    break;
                case NOTINSPECTED:
                    elbsm.get(OccInspectionStatusEnum.NOTINSPECTED).add(inSpaceEle);
                    break;
                case PASS:
                    elbsm.get(OccInspectionStatusEnum.PASS).add(inSpaceEle);
                    atLeastOneElementInspected = true;
                    break;
            }
        }
        
        inSpace.setElementStatusMap(elbsm);

        int iconID = 0;
        try {
            if (!atLeastOneElementInspected) {

                inSpace.setStatus(new OccInspectableStatus(OccInspectionStatusEnum.NOTINSPECTED));
                iconID = Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                        .getString(OccInspectionStatusEnum.NOTINSPECTED.getIconPropertyLookup()));
                inSpace.getStatus().setIcon(si.getIcon(iconID));
//                System.out.println("OccupancyCoordinator.configureOccInspectedSpace | NOTINSPEC inspectedSpaceID: " + inSpace.getInspectedSpaceID());

            } else if (atLeastOneElementInspected && !allElementsPass) {

                inSpace.setStatus(new OccInspectableStatus(OccInspectionStatusEnum.VIOLATION));
                iconID = Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                        .getString(OccInspectionStatusEnum.VIOLATION.getIconPropertyLookup()));
                inSpace.getStatus().setIcon(si.getIcon(iconID));
//                System.out.println("OccupancyCoordinator.configureOccInspectedSpace | VIOLATION inspectedSpaceID: " + inSpace.getInspectedSpaceID());

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

        // DEBUG LATER
//        Collections.sort(inSpace.getInspectedElementList());
        return inSpace;

    }
    
    /**
     * Builds a single generation tree of elements for display in an accordion
     * panel whose "folds" each has a list of logically grouped elements
     * 
     * @param ois containing all the CodeElements in its belly
     * @return the same ois but with a nice List of OccInsElementGroup objects!
     */
    public OccInspectedSpace configureElementDisplay(OccInspectedSpace ois){
        if(ois != null){
            // build a map first, keyed to code guide category name
            Map<String, List<OccInspectedSpaceElement>> oismap = new HashMap<>();
            
            if(!ois.getInspectedElementList().isEmpty()){
                // Disable for debugging
//                Collections.sort(ois.getInspectedElementList());
                for(OccInspectedSpaceElement oise: ois.getInspectedElementList()){
                    String cat = null;
                    if(oise.getGuideEntry() != null){
                         cat = oise.getGuideEntry().getCategory();
                    }
                    if(cat == null){
                        cat = NO_ELEMENT_CATEGORY_TITLE;
                    }
                    if(oismap.containsKey(cat)){
                        oismap.get(cat).add(oise);
                    } else {
                        List<OccInspectedSpaceElement> oisel = new ArrayList<>();
                        oisel.add(oise);                        
                        oismap.put(cat, oisel);
                    }
                }
            }
            
            // now unpack the map and build our groupings for display
            
            Set<String> catSet = oismap.keySet();
            List<String> catList = new ArrayList<>();
            catList.addAll(catSet);
            Collections.sort(catList);
            
            List<OccInsElementGroup> groupList = new ArrayList<>();
            for(String c: catList){
                OccInsElementGroup grp = new OccInsElementGroup(c, oismap.get(c));
                groupList.add(grp);
            }
            ois.setInspectedElementGroupList(groupList);
        } // end if for non null input param
        return ois;
    }

    /**
     * Implements business logic to set the status of each space element
     * @param inSpaceEle
     * @return
     * @throws BObStatusException 
     */
    private OccInspectedSpaceElement configureOccInspectedSpaceElement(OccInspectedSpaceElement inSpaceEle) throws BObStatusException {
        SystemIntegrator si = getSystemIntegrator();
        BlobCoordinator bc = getBlobCoordinator();
        

        int iconID = 0;

        try {
            if (inSpaceEle == null) {
                throw new BObStatusException("Cannot configure a null OccInspectedSpaceElement...");
            }
            // Inject blobs
            inSpaceEle.setBlobList(bc.getBlobLightList(inSpaceEle));
            
            if (inSpaceEle.getLastInspectedBy() != null && inSpaceEle.getComplianceGrantedTS() == null) {

                inSpaceEle.setStatus(new OccInspectableStatus(OccInspectionStatusEnum.VIOLATION));
                iconID = Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                        .getString(OccInspectionStatusEnum.VIOLATION.getIconPropertyLookup()));
                inSpaceEle.getStatus().setIcon(si.getIcon(iconID));

//                System.out.println("OccupancyCoordinator.configureOccInspectedSpaceEleement | VIOLATION inspectedSpaceElementID: " + inSpaceEle.getInspectedSpaceID());
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
        } catch (IntegrationException | BObStatusException | BlobException  ex) {
            System.out.println(ex);
        } 
        return inSpaceEle;
    }

    
    /**
     * Coordinates removing a space from being part of a checklist, 
     * @param spc
     * @param u
     * @param oi
     * @throws IntegrationException 
     */
    public void inspectionAction_removeSpaceFromInspection(OccInspectedSpace spc, User u, FieldInspection oi) throws IntegrationException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        oii.deleteInspectedSpace(spc);
    }
    
    /**
     * Routing method for code elements as they are inspected. This method 
     * 
     * calls the respective methods inside this coordinator based on the value of the
     * OccInspectedSpaceElements statusEnum
     * 
     * TODO: make sure add to code enf on fail gets recorded as 
     * as true by default
     * 
     * @param oise the element being inspected with a statusEnum set to the desired state
     * @param ua the user doing the inspecting; must have CEO or better permissions
     * @param oi the inspection in which the element lives
     * @param useDefFindOnFail if true is passed in, the default findings will be appended to any findings
     * @return 
     * @throws AuthorizationException
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    public OccInspectedSpaceElement inspectionAction_recordElementInspectionByStatusEnum(OccInspectedSpaceElement oise,
                                                                     UserAuthorized ua,
                                                                     FieldInspection oi,
                                                                     boolean useDefFindOnFail) throws AuthorizationException, BObStatusException, IntegrationException{
        if(oise == null || ua == null || oi == null){
            throw new BObStatusException("Cannot update code element status with null element, user, or inspection");
        }
        if(!ua.getMyCredential().isHasEnfOfficialPermissions()){
            throw new AuthorizationException("User " + ua.getUsername() + " is not authorized to change code element status");
        }
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        
        switch(oise.getStatusEnum()){
            case NOTINSPECTED:
                inspectionAction_configureElementForNotInspected(oise, ua, oi);
                break;
            case VIOLATION:
                inspectionAction_configureElementForInspectionNoCompliance(oise, ua, oi, useDefFindOnFail);
                break;
            case PASS:
                inspectionAction_configureElementForCompliance(oise, ua, oi);
                break;
        }
        // write changes to db // test without writing
        oii.updateInspectedSpaceElement(oise);
        return oise;

    }
    
    /**
     * Undertakes a batch operation for all inspected space elements in the inspected space
     * @param ois
     * @param oise
     * @param ua
     * @param oi
     * @param useDefFindOnFail when true the default findings are appended to existing findings
     * @return
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    public OccInspectedSpace inspectionAction_batchConfigureInspectedSpace(OccInspectedSpace ois,
                                                                            OccInspectionStatusEnum oise,
                                                                            UserAuthorized ua,
                                                                            FieldInspection oi,
                                                                            boolean useDefFindOnFail) throws BObStatusException, IntegrationException{
        if(ois == null || oise == null || ua == null || oi == null){
            throw new BObStatusException("Cannot batch update with null ois, user, or inspection");
        }
        
        List<OccInspectedSpaceElement> oisel = ois.getInspectedElementList();
        if(oisel != null && !oisel.isEmpty()){
            OccInspectionIntegrator oii = getOccInspectionIntegrator();
            for(OccInspectedSpaceElement ele: oisel){
                switch(oise){
                    case NOTINSPECTED:
                        inspectionAction_configureElementForNotInspected(ele, ua, oi);
                        break;
                    case VIOLATION:
                        inspectionAction_configureElementForInspectionNoCompliance(ele, ua, oi, useDefFindOnFail);
                        break;
                    case PASS:
                        inspectionAction_configureElementForCompliance(ele, ua, oi);
                        break;
                } // close switch
                oii.updateInspectedSpaceElement(ele);
            } // close for over elements
        } // close not null
        return ois;
    }
    

    /**
     * Sets members on an OccInspectedSpaceElement and writes to DB
     * @param oise
     * @param u
     * @param oi
     * @return with members properly set, ready for writing to db
     */
    public OccInspectedSpaceElement inspectionAction_configureElementForCompliance(OccInspectedSpaceElement oise,
                                                                      User u,
                                                                      FieldInspection oi)  {

        oise.setComplianceGrantedBy(u);
        oise.setComplianceGrantedTS(LocalDateTime.now());
        oise.setLastInspectedTS(LocalDateTime.now());
        oise.setLastInspectedBy(u);
        return oise;
    }

    /**
     * Removes status of space inspection in DB
     * @param oise
     * @param u
     * @param oi
     * @return with members set
     */
    public OccInspectedSpaceElement inspectionAction_configureElementForNotInspected(OccInspectedSpaceElement oise,
                                         User u,
                                         FieldInspection oi)  {

        oise.setComplianceGrantedBy(null);
        oise.setComplianceGrantedTS(null);
        oise.setLastInspectedTS(null);
        oise.setLastInspectedBy(null);

        return oise;
    }

    /**
     * Implements business rules for marking an element as inspected but not 
     * with compliance. From a database and Java logic perspective, 
     * there's no field that corresponds to "failure" but rather the failure
     * of an element during an inspection is derived from a timestamp of having 
     * been inspected but not being flagged as in compliance.
     * 
     * @param oise the element that has been inspected but is not in compliance
     * @param u
     * @param oi
     * @param useDefFindOnFail appends default findings to findings if true
     * @return with members set, ready for sending to DB
     */
    public OccInspectedSpaceElement inspectionAction_configureElementForInspectionNoCompliance(OccInspectedSpaceElement oise,
                                                          User u,
                                                          FieldInspection oi,
                                                          boolean useDefFindOnFail)  {

        oise.setComplianceGrantedBy(null);
        oise.setComplianceGrantedTS(null);
        oise.setLastInspectedTS(LocalDateTime.now());
        oise.setLastInspectedBy(u);
        
        if(useDefFindOnFail){
            StringBuilder sb = new StringBuilder();
            if(oise.getInspectionNotes() != null){
                sb.append(oise.getInspectionNotes());
                sb.append(SPACE);
            }
            if(oise.getDefaultViolationDescription() != null){
                sb.append(oise.getDefaultViolationDescription());
            }
            oise.setInspectionNotes(sb.toString());
        }

        return oise;
    }
    
   
    
    /** 
     * ************************************************************
     * ********************* CHECKLIST TEMPLATES ******************
     * ************************************************************
     */
    
    
    /**
     * Factory method of OccChecklistTemplate objects
     * NO Database write here--ID is zero
     * @param muni injected into the new template.
     * @return skeleton--ID=0
     */
    public OccChecklistTemplate getOccChecklistTemplateSkeleton(Municipality muni){
        OccChecklistTemplate oct = new OccChecklistTemplate();
        oct.setActive(true);
        oct.setMuni(muni);
        return oct;
        
        
    }
    
    /**
     * Deactivates the given template
     * @param ua doing the deactivation
     * @param oct to be deactivated
     */
    public void deactivateChecklistTemplate(UserAuthorized ua, OccChecklistTemplate oct) throws IntegrationException{
        if(oct != null && ua != null){
            OccChecklistIntegrator oci = getOccChecklistIntegrator();
            oct.setActive(false);
            oci.updateChecklistTemplateMetadata(oct);
            
            
        }
    }
    
     /**
     * Logic container for retrieving a ChecklistTemplate, which is used 
     * to create an actual OccupancyInspection
     * @param checklistID
     * @return fully baked checklist template; null if ID == 0
     * @throws IntegrationException 
     */
    public OccChecklistTemplate getChecklistTemplate(int checklistID) throws IntegrationException {
        OccChecklistTemplate oct = null;
        OccChecklistIntegrator oci = getOccChecklistIntegrator();
        if(checklistID != 0){
            oct = oci.getChecklistTemplate(checklistID);
            if(oct != null && oct.getInspectionChecklistID() != 0){
                oct.setOccSpaceTypeList(getOccSpaceTypeChecklistifiedList(oct));
            }
            return oct;
        }
        return null;
    }
 
      /**
     * Call me when the backing bean loads to get a list of possible
     * inspections to carry out such as "Commercial building" or
     * "residential"
     *
     * @param muni
     * @return a list, possibly containing checklist template objects
     * @throws IntegrationException
     */
    public List<OccChecklistTemplate> getOccChecklistTemplateList(Municipality muni) throws IntegrationException {
        OccChecklistIntegrator oci = getOccChecklistIntegrator();
        List<OccChecklistTemplate> templateList = new ArrayList<>();
        if(muni != null){
            List<Integer> idl = oci.getOccChecklistTemplateList(muni);
            if(idl != null && !idl.isEmpty()){
                for(Integer i: idl){
                    templateList.add(getChecklistTemplate(i));
                }
            }
        }
        return templateList;
    }

    /**
     * Factory method for creating OccSpaceElement
     * @return the Fresh Object
     */
    public OccSpaceElement getOccSpaceElementSkeleton() {
        return new OccSpaceElement();

    }

   
    
    /**
     * Extracts all occ space types for injection into the Template.
     * Used during the construction of an OccChecklistTemplate
     * @param oct
     * @return
     * @throws IntegrationException 
     */
    public List<OccSpaceTypeChecklistified> getOccSpaceTypeChecklistifiedList(OccChecklistTemplate oct) throws IntegrationException{
        OccChecklistIntegrator oci = getOccChecklistIntegrator();
        List<OccSpaceTypeChecklistified> ostcl = null;
        if(oct != null){
            ostcl = new ArrayList<>();
            List<Integer> ostcidl = oci.getOccSpaceTypeChecklistifiedIDListByChecklist(oct.getInspectionChecklistID());
            for (Integer ostcid: ostcidl) {
                ostcl.add(oci.getOccSpaceTypeChecklistified(ostcid));
            }
        }
        return ostcl;
    }

    /**
     * Logic intermedi Oary for creating a new checklist template in a given muni
     * @param plate
     * @return the database PK of the new record in occhecklisttemplate
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public int insertChecklistTemplateMetadata(OccChecklistTemplate plate) throws IntegrationException, BObStatusException{
        if(plate == null){
            throw new BObStatusException("Cannot insert a null template");
        }
        plate.setActive(true);
        OccChecklistIntegrator oci = getOccChecklistIntegrator();
        return oci.insertChecklistTemplateMetadata(plate);
        
    }
    
    
    /**
     * Undertakes an update operation on a ChecklistTemplate, including
     * deactivation
     * @param plate 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public void updateChecklistTemplateMetadata(OccChecklistTemplate plate) throws IntegrationException, BObStatusException{
        if(plate == null){
            throw new BObStatusException("Cannot update a null checklist template");
        }
        OccChecklistIntegrator oci = getOccChecklistIntegrator();
        oci.updateChecklistTemplateMetadata(plate);
    }
    
      /**
     * Fields requests for a non checklistified SpaceType by ID
     * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public OccSpaceType getOccSpaceTypeSkeleton() {
        
        return new OccSpaceType();
    }
    
      /**
     * Fields requests for a non checklistified SpaceType by ID
     * @param tpeID
     * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public OccSpaceType getOccSpaceType(int tpeID) throws IntegrationException{
        OccChecklistIntegrator oci = getOccChecklistIntegrator();
        return oci.getOccSpaceType(tpeID);
    }
    
    /**
     * Extracts all space types from the database--muni agnostic
     * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public List<OccSpaceType> getOccSpaceTypeList() throws IntegrationException{
        List<Integer> stidl = getOccChecklistIntegrator().getOccSpaceTypeIDListComplete();
        List<OccSpaceType> spaceTypeList = new ArrayList<>();
        if(stidl != null && !stidl.isEmpty()){
            for(Integer i: stidl){
                spaceTypeList.add(getOccSpaceType(i));
                
            }
        }
        
        return spaceTypeList;
    }
    
    
    /**
     * Logic intermediary for creating a new space type; these are
     * general across all munis, so they are just a classification scheme:
     * Kitchen, rear exterior, etc.
     * @param ost
     * @return of the freshly inserted record into occspacetype
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public int insertSpaceType(OccSpaceType ost) throws IntegrationException, BObStatusException{
        OccChecklistIntegrator oci = getOccChecklistIntegrator();
        if(ost == null){
            throw new BObStatusException("Cannot insert null space type");
        }
        return oci.insertSpaceType(ost);
        
        
        
    }
    
    /**
     * We want the user to be able to turn on or off required flags
     * on the ECEs before we link them to a space type, so this method
     * wraps an arbitrary list of ECEs in OccSpaceElement objects
     * that are skeletons--no IDs--since they haven't been in the DB yet
     * @param ecel 
     * @return  
     */
    public List<OccSpaceElement> wrapECEListInOccSpaceElementWrapper(List<EnforcableCodeElement> ecel){
        List<OccSpaceElement> osel = new ArrayList<>();
        if(ecel != null && !ecel.isEmpty()){
            for(EnforcableCodeElement ece: ecel){
                osel.add(new OccSpaceElement(ece));
            }
        }
        return osel;
    }
    
    /**
     * Undertakes an update operation on a space type
     * @param ost 
     */
    public void updateSpaceType(OccSpaceType ost) throws BObStatusException, IntegrationException{
        OccChecklistIntegrator oci = getOccChecklistIntegrator();
        if(ost == null){
            throw new BObStatusException("Cannot update null space type");
        }
        oci.updateSpaceType(ost);
    }


    /**
     * If not linked to something, removes an OccSpaceType from the DB
     * @param ost 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void removeOccSpaceType(OccSpaceType ost) throws BObStatusException, IntegrationException{
        if(ost == null){
            throw new BObStatusException("Cannot remove occ space type with null input");
        }
        getOccChecklistIntegrator().deleteSpaceType(ost);
        
    }
    
    
    /**
     * Updates a single SpaceType's connection to a Checklist
     * @param ostc 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void updateSpaceTypeChecklistified(OccSpaceTypeChecklistified ostc) throws BObStatusException, IntegrationException{
        OccChecklistIntegrator oci = getOccChecklistIntegrator();
        if(ostc == null){
            throw new BObStatusException("Cannot update null space type");
        }
        oci.updateSpaceTypeChecklistified(ostc);
    }
    
    /**
     * Does the switcheroo and passes it to the integrator for writing out 
     * @param ose 
     */
    public void toggleRequiredAndUpdateOccSpaceElement(OccSpaceElement ose) throws IntegrationException{
        OccChecklistIntegrator oci = getOccChecklistIntegrator();
        if(ose != null){
            // do the flip
            ose.setRequiredForInspection(!ose.isRequiredForInspection());
            oci.updateOccSpaceElement(ose);
        }
        
    }
    
    
    /**
     * Connects a list of OccSpaceTypes wrapped in their checklist wrapper
     * to the given occChecklistTemplate
     * 
     * @param plate
     * @param ost
     * @param required
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void insertAndLinkSpaceTypeChecklistifiedListToTemplate(OccChecklistTemplate plate, OccSpaceType ost, boolean required) 
            throws BObStatusException, IntegrationException{
    
        if(plate == null || ost == null ){
            throw new BObStatusException("cannot insertAndLinkSpaceTypeChecklistifiedListToTemplate with null template or ostc list");
        }
        
        OccChecklistIntegrator oci = getOccChecklistIntegrator();
            
            OccSpaceTypeChecklistified ostchk = new OccSpaceTypeChecklistified(ost);
            ostchk.setRequired(required);
            ostchk.setChecklistParentID(plate.getInspectionChecklistID());
            oci.verifyUniqueChecklistSpaceTypeLink(ostchk);
            oci.insertOccChecklistSpaceTypeChecklistified(ostchk);
        
    }
    
    /**
     * Utility method for downcasting
     * @param ostchkl
     * @return 
     */
    public List<OccSpaceType> downcastOccSpaceTypeChecklistified(List<OccSpaceTypeChecklistified> ostchkl){
        List<OccSpaceType> ostl = new ArrayList<>();
        if(ostchkl != null && !ostchkl.isEmpty()){
            for(OccSpaceTypeChecklistified ostchk: ostchkl){
                ostl.add((OccSpaceType) ostchk);
            }
        }
        return ostl;
    }
    
    /**
     * Removes a link between a SpaceType and its parent checklist. When this call
     * is done, pulling this OSTC's parent checklist will no longer include
     * this SpaceType or any of that space type's elements
     * @param ostc 
     */
    public void detachOccSpaceTypeChecklistifiedFromTemplate(OccSpaceTypeChecklistified ostc) throws BObStatusException, IntegrationException{
        if(ostc == null){
            throw new BObStatusException("Cannot detach space type checklistified with null input");
        }
        OccChecklistIntegrator oci = getOccChecklistIntegrator();
        // first get rid of the element to occspacetype links
        if(ostc.getCodeElementList() != null && !ostc.getCodeElementList().isEmpty()){
            for(OccSpaceElement ose: ostc.getCodeElementList()){
                oci.detachOccSpaceElementFromOccSpaceTypeChecklistified(ose);
            }
        }
        
        // then nuke the space type
        oci.deleteOccSpaceTypeChecklistified(ostc);
    }
    
    /**
     * Connects a given list of OccSpaceElements to the provided SpaceType that
     * has been connected to a given checklist.
     * Will complain with BOBStatusException if the pairing is not unique on the space type.
     * @param ostc
     * @param ose
     * @throws com.tcvcog.tcvce.domain.BObStatusException: for many reasons, including unique mappings
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void insertAndLinkCodeElementsToSpaceType(OccSpaceTypeChecklistified ostc, OccSpaceElement ose) throws BObStatusException, IntegrationException{
        if(ostc == null || ose == null ){
            throw new BObStatusException("Cannot insert and link elements to space with null space type or ordinance");
        }
        OccChecklistIntegrator oci = getOccChecklistIntegrator();
        
        ose.setParentSpaceTypeID(ostc.getChecklistSpaceTypeID());
        oci.verifyUniqueCodeElementInSpaceType(ose);
        oci.attachCodeElementsToSpaceTypeInChecklist(ose);
        
    }
    
    
    /**Removes a link between a code element and an occ space type
     * 
     * @param ose 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void detachCodeElementFromSpaceType(OccSpaceElement ose) throws BObStatusException, IntegrationException{
        if(ose == null){
            throw new BObStatusException("Cannot detach code element from space with null code element");
            
        }
        
        getOccChecklistIntegrator().detachOccSpaceElementFromOccSpaceTypeChecklistified(ose);
    }
    
    
    
    /**
     * Implements logic to create a pseudo deep copy of a given checklist template
     * to the target muni
     * @param muniTarget where the checklist should be copied. This muni will
     * be turned into a muni DataHeavy using its default CodeBook. Only 
     * EnforcableCodeElements which wrap code elements in the target Muni's default 
     * codebook which match the source template will be incorporated into the
     * cloned checklist
     * 
     * TODO: Finish my guts 
     * 
     * @param template
     * @param ua
     * @return the ID of the new checklist
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public int cloneOccChecklistTemplate(   Municipality muniTarget, 
                                            OccChecklistTemplate template, 
                                            UserAuthorized ua) 
            throws BObStatusException, IntegrationException, AuthorizationException, EventException{
        
        if(muniTarget == null || template == null || ua == null){
            throw new BObStatusException("cannot clone checklist with null muni or source template");
        }
        
        
        MunicipalityCoordinator mc = getMuniCoordinator();
        MunicipalityDataHeavy mdh;
        try {
            mdh = mc.assembleMuniDataHeavy(muniTarget, ua);
        } catch (BlobException ex) {
            throw new BObStatusException("Cannot get MuniDH because of blob exception");

        }
        
        
        CodeSet codeSet = mdh.getCodeSet();
        
        
        return 0;
        
        
    }
    
    
    
    
  /** 
     * ************************************************************
     * ********************* MISC   ******************
     * ************************************************************
     */
    
  
    
    /**
     * Retrieves all active causes for use in selection boxes
     * @return
     * @throws IntegrationException 
     */
    public List<OccInspectionCause> getOccInspectionCauseList() throws IntegrationException{
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        List<Integer> cidl = oii.getCauseListActiveOnly();
        List<OccInspectionCause> causeList = new ArrayList<>();
        if(cidl != null && !cidl.isEmpty()){
            for(Integer i: cidl){
                causeList.add(oii.getCause(i));
            }
        }
        return causeList;
    }
    /**
     * Retrieves all active determinations for use in selection boxes
     * @return
     * @throws IntegrationException 
     */
    public List<OccInspectionDetermination> getOccDeterminationList() throws IntegrationException{
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        List<OccInspectionDetermination> detList = oii.getDeterminationList();
        
        return detList;
    }
    
    /**
     * Generates a skeleton field inspection report
     * @param insp
     * @param inspectable
     * @param usr
     * @return
     * @throws IntegrationException 
     */
     public ReportConfigOccInspection getOccInspectionReportConfigDefault(FieldInspection insp,
                                                                         IFace_inspectable inspectable,
                                                                         User usr) throws IntegrationException {
        SystemIntegrator si = getSystemIntegrator();

        ReportConfigOccInspection rpt = new ReportConfigOccInspection();
        rpt.setGenerationTimestamp(LocalDateTime.now());
        
        if(inspectable instanceof OccPeriodPropertyUnitHeavy){
            OccPeriodPropertyUnitHeavy op = (OccPeriodPropertyUnitHeavy) inspectable;
            rpt.setOccPeriod(op);
            rpt.setInspectedUnit(op.getPropUnitProp());
            rpt.setInspectedProperty(op.getPropUnitProp().getProperty());
        } else if(inspectable instanceof CECasePropertyUnitHeavy){
            CECasePropertyUnitHeavy csepuh  = (CECasePropertyUnitHeavy) inspectable;
            rpt.setCeCase(csepuh);
            rpt.setInspectedProperty(csepuh.getProperty());
            rpt.setInspectedUnit(csepuh.getPropUnit());
        }
        
        rpt.setInspection(insp);

        rpt.setTitle(getResourceBundle(Constants.MESSAGE_TEXT).getString("report_occinspection_default_title"));
        rpt.setCreator(usr);
        rpt.setMuni(getSessionBean().getSessMuni());

        rpt.setDefaultItemIcon(si.getIcon(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                .getString(OccInspectionStatusEnum.NOTINSPECTED.getIconPropertyLookup()))));

        rpt.setIncludeParentObjectInfoHeader(false);

        rpt.setIncludeOwnerInfo(true);
        rpt.setIncludeOwnerPhones(true);
        rpt.setIncludeOwnerAddresses(true);
        rpt.setIncludeOwnerEmails(true);
        
        rpt.setIncludeBedBathOccCounts(true);
        
        rpt.setIncludePhotos_pass(true);
        rpt.setIncludePhotos_fail(true);
        
        rpt.setIncludePhotoIDs(true);
        rpt.setIncludePhotoTitles(true);
        rpt.setIncludePhotoOriginalFileNames(false);
        rpt.setIncludePhotoDescriptions(true);
        rpt.setUnifiedPhotoWidth(DEFAULT_PHOTO_WIDTH);
        
        rpt.setIncludeFullOrdText(true);
        rpt.setIncludeOrdinanceFindings(true);

        rpt.setIncludeOrdinanceInspectionTimestamps(true);
        

        rpt.setIncludeRemedyInfo(false);
        rpt.setIncludeSignature(false);

        rpt.setViewSetting(ViewOptionsOccChecklistItemsEnum.FAILED_PASSEDWPHOTOFINDING);
        return rpt;
    }


     /**
      * Logic intermediary for getter of determination
      * @param currentDetermination
      * @return
      * @throws IntegrationException 
      */
    public int determinationCheckForUse(OccInspectionDetermination currentDetermination) throws IntegrationException {
       OccInspectionIntegrator oii = getOccInspectionIntegrator();
       return oii.determinationCheckForUse(currentDetermination);
    }
    
    public OccInspectionDetermination getDetermination(int determinationID) throws IntegrationException {
       OccInspectionIntegrator oii = getOccInspectionIntegrator();
       return oii.getDetermination(determinationID);
    }

    public void deactivateDetermination(OccInspectionDetermination currentDetermination) throws IntegrationException {
       OccInspectionIntegrator oii = getOccInspectionIntegrator();
       oii.deactivateDetermination(currentDetermination);
    }

    public void updateDetermination(OccInspectionDetermination currentDetermination) throws IntegrationException {
       OccInspectionIntegrator oii = getOccInspectionIntegrator();
       oii.updateDetermination(currentDetermination);
    }

    public void insertDetermination(OccInspectionDetermination currentDetermination) throws IntegrationException {
       OccInspectionIntegrator oii = getOccInspectionIntegrator();
       oii.insertDetermination(currentDetermination);
    }
   
    public List<OccInspectionDetermination> getDeterminationList() throws IntegrationException {
       OccInspectionIntegrator oii = getOccInspectionIntegrator();
       return oii.getDeterminationList();
    }
    
    public int requirementCheckForUse(OccInspectionRequirement currentRequirement) throws IntegrationException {
       OccInspectionIntegrator oii = getOccInspectionIntegrator();
       return oii.requirementCheckForUse(currentRequirement);
    }
    
    public OccInspectionRequirement getRequirement(int requirementID) throws IntegrationException {
       OccInspectionIntegrator oii = getOccInspectionIntegrator();
       return oii.getRequirement(requirementID);
    }

    public void deactivateRequirement(OccInspectionRequirement currentRequirement) throws IntegrationException {
       OccInspectionIntegrator oii = getOccInspectionIntegrator();
       oii.deactivateRequirement(currentRequirement);
    }

    public void updateRequirement(OccInspectionRequirement currentRequirement) throws IntegrationException {
       OccInspectionIntegrator oii = getOccInspectionIntegrator();
       oii.updateRequirement(currentRequirement);
    }

    public void insertRequirement(OccInspectionRequirement currentRequirement) throws IntegrationException {
       OccInspectionIntegrator oii = getOccInspectionIntegrator();
       oii.insertRequirement(currentRequirement);
    }
   
    public List<OccInspectionRequirement> getRequirementList() throws IntegrationException {
       OccInspectionIntegrator oii = getOccInspectionIntegrator();
       return oii.getRequirementList();
    }

}
