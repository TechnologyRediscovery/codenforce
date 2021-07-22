package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.occupancy.*;
import com.tcvcog.tcvce.occupancy.integration.OccInspectionIntegrator;
import com.tcvcog.tcvce.util.Constants;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OccInspectionCoordinator extends BackingBeanUtils implements Serializable {

    public OccInspectionCoordinator() {
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

}
