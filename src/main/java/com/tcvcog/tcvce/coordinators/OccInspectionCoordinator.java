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
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.InspectionException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.occupancy.*;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccChecklistIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccInspectionIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic reservoir for field inspections, formerly called Occupancy Inspections
 * but since field inspections are used for both silos, that distinction is now moot
 * @author Ellen Bascomb of apt 31Y
 */
public class OccInspectionCoordinator extends BackingBeanUtils implements Serializable {

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
     */
    public OccInspection inspectionAction_commenceOccupancyInspection(OccInspection in,
                                                                      OccChecklistTemplate tem,
                                                                      OccPeriod period,
                                                                      User user) 
            throws InspectionException, IntegrationException {
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
//            if (muni.isEnablePublicOccInspectionTODOs()) {
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
     * Logic container for updating space element data on all elements in an inspection
     * @param inspection
     * @throws IntegrationException 
     */
    public void inspectionAction_updateSpaceElementData(OccInspection inspection) throws IntegrationException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();

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
     * @return Containing a List of InspectedCodeElement objects ready to be
     * evaluated
     *
     * @throws IntegrationException
     */
    public OccInspection        inspectSpace  (OccInspection inspection,
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
                    inspectedElement.setLastInspectedBy(user);
                    inspectedElement.setLastInspectedTS(LocalDateTime.now());
                    inspectedElement.setComplianceGrantedBy(null);// fail means this is null
                    inspectedElement.setComplianceGrantedTS(null);// fail means this is null
                    break;
                case PASS:
                    inspectedElement.setLastInspectedBy(user);
                    inspectedElement.setLastInspectedTS(LocalDateTime.now());
                    inspectedElement.setComplianceGrantedBy(user);
                    inspectedElement.setComplianceGrantedTS(LocalDateTime.now());
                    break;
                default:
                    inspectedElement.setLastInspectedBy(null);
                    inspectedElement.setLastInspectedTS(null);
            }
            return inspectedElement;
        }).collect(Collectors.toList());

        inspectedSpace.setInspectedElementList(inspectedElements);
        inspectedSpace.setType(tpe);
        // With a fully built inspected space, we can record our start of inspection in the DB
        inspectedSpace = oii.recordCommencementOfSpaceInspection(inspectedSpace, inspection);
        System.out.println("OccupancyCoordinator.inspectionAction_commenceSpaceInspection | commenced inspecting of space");

        // now use our convenience method to record Inspection of the space's individual elements
        oii.recordInspectionOfSpaceElements(inspectedSpace, inspection);

        // check sequence by retrieving new inspected space and displaying info
        inspectedSpace = oii.getInspectedSpace(inspectedSpace.getInspectedSpaceID());
        System.out.println("OccupancyCoordinator.inspectionAction_commenceSpaceInspection | retrievedInspectedSpaceID= " + inspectedSpace);

        return inspection;
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
     * Logic for setting members on OccInspectedSpace
     * @param inSpace
     * @return
     * @throws BObStatusException 
     */
    private OccInspectedSpace configureOccInspectedSpace(OccInspectedSpace inSpace) throws BObStatusException {
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
     * Sets members on an OccInspectedSpaceElement and writes to DB
     * @param oise
     * @param u
     * @param oi
     * @throws IntegrationException 
     */
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

    /**
     * Removes status of space inspection in DB
     * @param oise
     * @param u
     * @param oi
     * @throws IntegrationException 
     */
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
     * @throws IntegrationException 
     */
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
}
