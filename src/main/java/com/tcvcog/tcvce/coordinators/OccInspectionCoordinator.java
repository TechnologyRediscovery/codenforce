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
import com.tcvcog.tcvce.domain.InspectionException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.occupancy.*;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccChecklistIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccInspectionIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    
    
    
    public OccInspectionCoordinator() {
    }
    
    /**
     * Factory of occupnacy inspections
     * @return 
     */
    public OccInspection getOccInspectionSkeleton() {
        return new OccInspection();
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
     * @param in     A skeleton of an OccInspection without an ID number
     * @param tem
     * @param period the OccPeriod to which the OccInspection should be linked
     * @param user   The current user who will become the Inspector
     * @return An OccInspection object with the ID given in the DB and a
     * configured Template inside
     * @throws InspectionException
     * @throws IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public OccInspection inspectionAction_commenceOccupancyInspection(OccInspection in,
                                                                      OccChecklistTemplate tem,
                                                                      OccPeriod period,
                                                                      User user) 
            throws InspectionException, IntegrationException, BObStatusException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        OccChecklistIntegrator oci = getOccChecklistIntegrator();
        OccInspection inspec = null;

        if (period.getType().isActive() && period.getType().isInspectable()) {

            if (in != null) {
                inspec = in;
            } else {
                inspec = new OccInspection();
            }
            inspec.setOccPeriodID(period.getPeriodID());
            if (tem == null) {
                inspec.setChecklistTemplate(oci.getChecklistTemplate(period.getType().getChecklistID()));
            } else {
                inspec.setChecklistTemplate(tem);
            }
            inspec.setInspector(user);
            inspec.setPacc(generateControlCodeFromTime(user.getHomeMuniID()));

            inspec = getOccInspection(oii.insertOccInspection(inspec));
        } else {
            throw new InspectionException("Occ period either inactive or uninspectable");
        }
        return inspec;
    }

    
    /**
     * Logic container for updating space element data on all elements in an inspection
     * EXCEPT for inspection status which must be routed through the appropriate other methods on
     * this Coordinator
     * @param osi
     * @throws IntegrationException 
     */
    public void inspectionAction_updateSpaceElementData(OccInspectedSpace osi) throws IntegrationException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        if(osi != null && osi.getInspectedElementList() != null && !osi.getInspectedElementList().isEmpty()){
            for(OccInspectedSpaceElement oise: osi.getInspectedElementList()){
                oii.updateInspectedSpaceElement(oise);
            }
        }
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
     *                      OccInspection.
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
    public OccInspectedSpace inspectionAction_commenceInspectionOfSpaceTypeChecklistified  (OccInspection inspection,
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
                case FAIL:
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
     * @param det to certify; must be relevant for context (i.e. occperiod)
     * @param ua doing the certification (must be OP manager or sys admin or better)
     * @param op The host occ period--which cannot be authorized
     */
    public void inspectionAction_certifyInspection( OccInspection oi,
                                                    UserAuthorized ua,
                                                    OccPeriod op) throws IntegrationException, BObStatusException, AuthorizationException{
        
        if(oi == null || oi.getDetermination() == null || ua == null || op == null || op.getManager() == null){
            throw new BObStatusException("Cannot certify with null inspection, determination, user, period, or manager!");
        }
        
        if(ua.getUserID() != op.getManager().getUserID() || !ua.getKeyCard().isHasSysAdminPermissions()){
            throw new AuthorizationException("Given user cannot certify this inspection because user is not period manager or does not have sys admin or better permissions");
        }
      
        oi.setDeterminationBy(ua);
        oi.setDeterminationTS(LocalDateTime.now());
        
        updateOccInspection(oi, ua);
              
    }

    /**
     * Logic containe for retrieving and configuring an OccInspection
     * @param inspectionID
     * @return
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public OccInspection getOccInspection(int inspectionID) throws IntegrationException, BObStatusException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        OccInspection oi = oii.getOccInspection(inspectionID);
        oi.setChecklistTemplate(getChecklistTemplate(oi.getChecklistTemplateID()));
        oi.setInspectedSpaceList(oii.getInspectedSpaceList(oi.getInspectionID()));

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
    public void updateOccInspection(OccInspection oi, UserAuthorized ua) throws IntegrationException, BObStatusException{
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
     * @param op parent occ period of the inspection to be deactivated
     * @throws com.tcvcog.tcvce.domain.BObStatusException for null of any input param
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.AuthorizationException inputted ua must be the manager or inspector or have sys admin or better permissions
     */
    public void deactivateOccInspection(UserAuthorized ua, OccInspection oi, OccPeriod op) 
            throws BObStatusException, IntegrationException, AuthorizationException{
        if(ua == null || oi == null || op == null || oi.getInspector() == null || op.getManager() == null){
            throw new BObStatusException("Cannot deactivate an inspection with null user, inspec, or period, or their manager/inspector!");
            
        }
        if(op.getPeriodID() != oi.getOccPeriodID()){
            throw new BObStatusException("Cannot deactivate inspection because the given inspection is not contained by the given occ period. This is a fatal error.");
        }
        if(verifyUserAuthorizationForInspectionActions(ua, oi, op)) {
            OccInspectionIntegrator oii = getOccInspectionIntegrator();
            oi.setDeactivatedBy(ua);
            oi.setDeactivatedTS(LocalDateTime.now());
            oii.updateOccInspection(oi);
        } else {
            throw new AuthorizationException("Cannot deactivate an occ inspection unless user is the manager of period, the inspector, or a sys admin or better");
        }
    }
    
    
    /**
     * Removes the values for the given OccInspection's determination, detTS, and DetUser
     * @param ua
     * @param oi
     * @param op 
     */
    public void removeOccInspectionFinalization(UserAuthorized ua, OccInspection oi, OccPeriod op) throws IntegrationException, BObStatusException{
        if(verifyUserAuthorizationForInspectionActions(ua, oi, op)){
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
                                                                OccInspection oi, 
                                                                OccPeriod op){
        
        boolean auth = false;
        if(ua != null && oi != null && op != null && op.getManager() != null){
            if(ua.getUserID() == oi.getInspector().getUserID() || ua.getUserID() == op.getManager().getUserID() || ua.getKeyCard().isHasSysAdminPermissions()){
                auth = true;
            }
        }
        
        return auth;
    }

    
    
    
    /**
     * Assembles a list of all OccInspections for a given period
     * @param period
     * @return
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public List<OccInspection> getOccInspectionsFromOccPeriod(OccPeriodDataHeavy period) throws IntegrationException, BObStatusException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();

        List<Integer> inspectionIDList = oii.getOccInspectionList(period);

        List<OccInspection> inspectionList = new ArrayList();
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
    private OccInspection configureOccInspection(OccInspection inspection) throws BObStatusException {
        boolean allSpacesPassed = true;
        if (inspection != null) {
            for (OccInspectedSpace inSpace : inspection.getInspectedSpaceList()) {
                if (configureOccInspectedSpace(inSpace).getStatus().getStatusEnum() == OccInspectionStatusEnum.FAIL
                        || configureOccInspectedSpace(inSpace).getStatus().getStatusEnum() == OccInspectionStatusEnum.NOTINSPECTED) {
                    allSpacesPassed = false;
                }
            }
            inspection.setReadyForPassedCertification(allSpacesPassed);
            if (!inspection.getInspectedSpaceList().isEmpty()) {
                Collections.sort(inspection.getInspectedSpaceList());
                Collections.reverse(inspection.getInspectedSpaceList());
            }
        }
        return inspection;
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
        elbsm.put(OccInspectionStatusEnum.FAIL, new ArrayList<>());
        elbsm.put(OccInspectionStatusEnum.NOTINSPECTED, new ArrayList<>());
              
        for (OccInspectedSpaceElement inSpaceEle : inSpace.getInspectedElementList()) {
            configureOccInspectedSpaceElement(inSpaceEle);
            switch(inSpaceEle.getStatusEnum()){
                case FAIL:
                    allElementsPass = false;
                    elbsm.get(OccInspectionStatusEnum.FAIL).add(inSpaceEle);
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
            List<OccInsElementGroup> groupList = new ArrayList<>();
            for(String c: catSet){
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

        int iconID = 0;

        try {
            if (inSpaceEle == null) {
                throw new BObStatusException("Cannot configure a null OccInspectedSpaceElement...");
            }

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
    
    /**
     * Coordinates removing a space from being part of a checklist, 
     * @param spc
     * @param u
     * @param oi
     * @throws IntegrationException 
     */
    public void inspectionAction_removeSpaceFromInspection(OccInspectedSpace spc, User u, OccInspection oi) throws IntegrationException {
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
     * @throws AuthorizationException
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    public void inspectionAction_recordElementInspectionByStatusEnum(OccInspectedSpaceElement oise,
                                                                     UserAuthorized ua,
                                                                     OccInspection oi,
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
            case FAIL:
                inspectionAction_configureElementForInspectionNoCompliance(oise, ua, oi, useDefFindOnFail);
                break;
            case PASS:
                inspectionAction_configureElementForCompliance(oise, ua, oi);
                break;
        }
        // write changes to db
        oii.updateInspectedSpaceElement(oise);

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
                                                                            OccInspection oi,
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
                    case FAIL:
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
                                                                      OccInspection oi)  {

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
                                         OccInspection oi)  {

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
                                                          OccInspection oi,
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
        List<Integer> didl = oii.getDeterminationListActiveOnly();
        List<OccInspectionDetermination> detList = new ArrayList<>();
        if(didl != null && !didl.isEmpty()){
            for(Integer i: didl){
                detList.add(oii.getDetermination(i));
            }
        }
        return detList;
    }
}
