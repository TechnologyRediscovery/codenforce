package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.InspectionException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.occupancy.*;
import com.tcvcog.tcvce.occupancy.integration.OccInspectionIntegrator;
import com.tcvcog.tcvce.util.Constants;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The premier backing bean for occupancy inspections workflow.
 *
 * @author jurplel
 */
public class OccInspectionsBB extends BackingBeanUtils implements Serializable {

    private List<OccChecklistTemplate> checklistTemplateList;
    private List<User> userList;
    private List<OccLocationDescriptor> locDescriptorList;

    private List<OccSpace> inspectionSpaceList;

    // Form items
    private OccChecklistTemplate selectedChecklistTemplate;
    private User selectedInspector;

    private OccSpace selectedSpace;
    private OccLocationDescriptor selectedLocDescriptor;

    private OccInspection skeletonInspection;


    @PostConstruct
    public void initBean() {
        // Initialize list of checklist templates
        initChecklistTemplates();
        initUserList();
        initLocDescriptorList();
    }

    /**
     * Gets the list of possible checklist template objects and sets the member
     * variable checklistTemplates to its value.
     */
    public void initChecklistTemplates() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            checklistTemplateList = oc.getOccChecklistTemplatelist();
        } catch (IntegrationException ex) {
            System.out.println("Failed to acquire list of checklist templates:" + ex);
        }
    }

    /**
     * Gets the list of all users and sets userList to that list.
     */
    public void initUserList() {
        UserCoordinator uc = getUserCoordinator();

        // TODO: probably shouldn't pass null here...
        userList = uc.user_assembleUserListForSearch(null);
    }

    /**
     * Gets the list of all possible location descriptors and sets locDescriptorList
     */
    public void initLocDescriptorList() {
        // TODO: MVC violation--never do this! I just copied this from OccInspectionBB and can't be bothered to do it properly.
        OccInspectionIntegrator oii = getOccInspectionIntegrator();

        locDescriptorList = new ArrayList<>();
        try {
            locDescriptorList.add(oii.getLocationDescriptor(
                    Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("locationdescriptor_implyfromspacename"))));
        } catch (IntegrationException ex) {
            System.out.println("Failed to acquire list of location descriptors:" + ex);
        }
    }

    /**
     * Initializes empty OccInspection object to build. Requires checklist template and inspector to be selected.
     */
    public void initSkeletonInspection() {
        if (selectedChecklistTemplate == null) {
            System.out.println("Can't initialize skeleton inspection: selected checklist template is null");
            return;
        } else if (selectedInspector == null) {
            System.out.println("Can't initialize skeleton inspection: selected inspector is null");
            return;
        }

        OccPeriod occPeriod = getSessionBean().getSessOccPeriod();

        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            skeletonInspection = oc.inspectionAction_commenceOccupancyInspection(null, selectedChecklistTemplate, occPeriod, selectedInspector);
        } catch (InspectionException | IntegrationException ex) {
            System.out.println("Failed to initialize skeleton OccInspection object: " + ex);
        }

        initInspectionSpaceList();
    }

    /**
     * Initializes joined list of all spaces across all possible space types from the skeleton inspection's checklist template.
     * That doesn't really make sense but basically we're getting the list of all the spaces that this inspection could include.
     */
    public void initInspectionSpaceList() {
        if (skeletonInspection == null) {
            System.out.println("Can't initialize inspection space list: skeleton inspection object is null");
            return;
        } else if (skeletonInspection.getChecklistTemplate() == null) {
            System.out.println("Can't initialize inspection space list: skeleton inspection object's checklist template object is null");
            return;
        }  else if (skeletonInspection.getChecklistTemplate().getOccSpaceTypeTemplateList() == null) {
            System.out.println("Can't initialize inspection space list: skeleton inspection object's checklist template's occ space type list is null");
            return;
        }

        List<OccSpace> occSpaces = new ArrayList<>();

        for (OccSpaceTypeInspectionDirective type : skeletonInspection.getChecklistTemplate().getOccSpaceTypeTemplateList()) {
            if (type.getSpaceList() != null) {
                occSpaces.addAll(type.getSpaceList());
            }
        }

        inspectionSpaceList = occSpaces;
    }

    void addSelectedSpaceToSkeletonInspection() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            // TODO: Maybe its important that i'm not passing a user or OccInspectionStatusEnum but i think its fine.
            skeletonInspection = oc.inspectionAction_commenceSpaceInspection(skeletonInspection, null, selectedSpace, null, selectedLocDescriptor);
        } catch (IntegrationException ex) {
            System.out.println("Failed to add selected space to skeleton inspection object: " + ex);
        }
    }

    /**
     * Clears all parameters that might be selected during the inspection flow
     * so that one may start fresh. May be called, for example, by a button to start the flow.
     */
    public void clearSelections() {
        setSelectedChecklistTemplate(new OccChecklistTemplate());
        setSelectedInspector(new User());
        setSelectedSpace(new OccSpace());
        setSelectedLocDescriptor(new OccLocationDescriptor());
    }

    // getters & setters below you know the drill

    public List<OccChecklistTemplate> getChecklistTemplateList() {
        return checklistTemplateList;
    }

    public List<User> getUserList() {
        return userList;
    }

    public List<OccLocationDescriptor> getLocDescriptorList() {
        return locDescriptorList;
    }

    public List<OccSpace> getInspectionSpaceList() {
        return inspectionSpaceList;
    }

    public OccChecklistTemplate getSelectedChecklistTemplate() {
        return selectedChecklistTemplate;
    }

    public void setSelectedChecklistTemplate(OccChecklistTemplate selectedChecklistTemplate) {
        this.selectedChecklistTemplate = selectedChecklistTemplate;
    }

    public User getSelectedInspector() {
        return selectedInspector;
    }

    public void setSelectedInspector(User selectedInspector) {
        this.selectedInspector = selectedInspector;
    }

    public OccSpace getSelectedSpace() {
        return selectedSpace;
    }

    public void setSelectedSpace(OccSpace selectedSpace) {
        this.selectedSpace = selectedSpace;
    }

    public OccLocationDescriptor getSelectedLocDescriptor() {
        return selectedLocDescriptor;
    }

    public void setSelectedLocDescriptor(OccLocationDescriptor selectedLocDescriptor) {
        this.selectedLocDescriptor = selectedLocDescriptor;
    }

    public OccInspection getSkeletonInspection() {
        return skeletonInspection;
    }

}
