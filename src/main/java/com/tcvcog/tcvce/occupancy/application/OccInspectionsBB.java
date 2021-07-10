package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.InspectionException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.occupancy.*;

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

    private List<OccSpace> inspectionSpaceList;

    // Form items
    private OccChecklistTemplate selectedChecklistTemplate;
    private User selectedInspector;

    private OccSpace selectedSpace;

    private OccInspection skeletonInspection;


    @PostConstruct
    public void initBean() {
        // Initialize list of checklist templates
        initChecklistTemplates();
        initUserList();
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

    /**
     * Clears all parameters that might be selected during the inspection flow
     * so that one may start fresh. May be called, for example, by a button to start the flow.
     */
    public void clearSelections() {
        setSelectedChecklistTemplate(new OccChecklistTemplate());
        setSelectedInspector(new User());
    }

    // getters & setters below you know the drill

    public List<OccChecklistTemplate> getChecklistTemplateList() {
        return checklistTemplateList;
    }

    public List<User> getUserList() {
        return userList;
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

    public OccInspection getSkeletonInspection() {
        return skeletonInspection;
    }

}
