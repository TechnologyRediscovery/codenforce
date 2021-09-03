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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class OccInspectionCoordinator extends BackingBeanUtils implements Serializable {

    public OccInspectionCoordinator() {
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
                                                                      User user) throws InspectionException, IntegrationException {
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
     * @param type          The space type which will have a list of SpaceElements inside it
     * @param initialStatus The initial status of the created OccInspectedSpace
     * @param locDesc       A populated location descriptor for the new OccInspectedSpace. Can be an
     *                      existing location or a new one.
     *
     * @return Containing a List of InspectedCodeElement objects ready to be
     * evaluated
     *
     * @throws IntegrationException
     */
    public OccInspection createInspectedSpace(OccInspection inspection,
                                              User user,
                                              OccSpaceType type,
                                              final OccInspectionStatusEnum initialStatus,
                                              OccLocationDescriptor locDesc) throws IntegrationException {

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
        inspectedSpace.setAddedToChecklistTS(LocalDateTime.now());

        // Wrap each CodeElement in an InspectedCodeElement blanket to keep it warm :)
        List<OccInspectedSpaceElement> inspectedElements;
        inspectedElements = type.getCodeElementList().stream().map(element -> {
            OccInspectedSpaceElement inspectedElement = new OccInspectedSpaceElement(element);

            switch (initialStatus) {
                case FAIL:
                    inspectedElement.setLastInspectedBy(user);
                    inspectedElement.setLastInspectedTS(LocalDateTime.now());
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

        // ...also make it the OccInspectedSpace's list of InspectedCodeElements
        inspectedSpace.setInspectedElementList(inspectedElements);

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

    public OccInspection getOccInspection(int inspectionID) throws IntegrationException, BObStatusException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();

        return configureOccInspection(oii.getOccInspection(inspectionID));
    }

    public List<OccInspection> getOccInspectionsFromOccPeriod(OccPeriodDataHeavy period) throws IntegrationException, BObStatusException {
        OccInspectionIntegrator oii = getOccInspectionIntegrator();

        List<Integer> inspectionIDList = oii.getOccInspectionList(period);

        List<OccInspection> inspectionList = new ArrayList();
        for (Integer id : inspectionIDList) {
            inspectionList.add(getOccInspection(id));
        }

        return inspectionList;
    }

    public OccInspection configureOccInspection(OccInspection inspection) throws BObStatusException {
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

    public OccInspectedSpace configureOccInspectedSpace(OccInspectedSpace inSpace) throws BObStatusException {
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

    public OccInspectedSpaceElement configureOccInspectedSpaceElement(OccInspectedSpaceElement inSpaceEle) throws BObStatusException {
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
}
