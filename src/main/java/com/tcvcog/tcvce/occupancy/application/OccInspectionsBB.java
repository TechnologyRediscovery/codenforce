package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.application.SessionBean;
import com.tcvcog.tcvce.coordinators.OccInspectionCoordinator;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.InspectionException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.occupancy.*;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The premier backing bean for occupancy inspections workflow.
 *
 * @author jurplel
 */
public class OccInspectionsBB extends BackingBeanUtils implements Serializable {

    private List<OccChecklistTemplate> checklistTemplateList;
    private List<User> userList;

    // Form items--these need organized
    private OccChecklistTemplate selectedChecklistTemplate;
    private User selectedInspector;

    private OccLocationDescriptor selectedLocDescriptor;

    private OccLocationDescriptor skeletonLocationDescriptor;

    private OccInspection selectedInspection;

    private OccInspectedSpace selectedInspectedSpace;

    private boolean editMode;

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
        SessionBean sb = getSessionBean();
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            checklistTemplateList = oc.getOccChecklistTemplateList(sb.getSessMuni());
        } catch (IntegrationException ex) {
            System.out.println("Failed to acquire list of checklist templates:" + ex);
        }
    }

    /**
     * Gets the list of all users and sets userList to that list.
     */
    public void initUserList() {
        UserCoordinator uc = getUserCoordinator();

        try {
            // TODO: probably shouldn't pass null here...
            userList = uc.user_assembleUserListForSearch(null);
        } catch (BObStatusException ex) {
            System.out.println(ex);
        }
    }

    /**
     * Creates empty inspection object for the current occupancy period.
     */
    public void createInspection() {
        if (selectedChecklistTemplate == null) {
            System.out.println("Can't initialize new OccInspection: selected checklist template is null");
            return;
        } else if (selectedInspector == null) {
            System.out.println("Can't initialize new OccInspection: selected inspector is null");
            return;
        }

        OccPeriodDataHeavy occPeriod = getSessionBean().getSessOccPeriod();

        OccInspectionCoordinator oic = getOccInspectionCoordinator();
        try {
            OccInspection newInspection = oic.inspectionAction_commenceOccupancyInspection(null, selectedChecklistTemplate, occPeriod, selectedInspector);

            if (occPeriod.getInspectionList() == null) {
                occPeriod.setInspectionList(new ArrayList());
            }
            occPeriod.getInspectionList().add(newInspection);
            getSessionBean().setSessOccPeriod(occPeriod);
        } catch (InspectionException | IntegrationException ex) {
            System.out.println("Failed to create new OccInspection: " + ex);
        }
    }

    public void initSkeletonLocDescriptor() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        skeletonLocationDescriptor = oc.getOccLocationDescriptorSkeleton();
    }

    public void createLocDescriptor() {
        if (skeletonLocationDescriptor == null) {
            System.out.println("Can't create new loc descriptor: skeleton location descriptor object is null");
            return;
        }

        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.addNewLocationDescriptor(skeletonLocationDescriptor);
        } catch (IntegrationException ex) {
            System.out.println("Failed to add skeleton location descriptor: " + ex);
        }
    }

    public void addSelectedSpaceToSelectedInspection() {
//        if (selectedInspection == null) {
//            System.out.println("Can't initialize add space to inspection: selected inspection object is null");
//            return;
//        }
//        OccupancyCoordinator oc = getOccupancyCoordinator();
//        try {
            // Maybe its important that i'm not passing a user or OccInspectionStatusEnum but i think its fine.
//            selectedInspection = oc.inspectionAction_commenceSpaceInspection(selectedInspection, selectedInspection.getInspector(), selectedSpace, null, null);

//        } catch (IntegrationException ex) {
//            System.out.println("Failed to add selected space to skeleton inspection object: " + ex);
//        }
    }

    public OccInspectionStatusEnum[] getStatuses() {
        return OccInspectionStatusEnum.values();
    }

    /**
     * Clears all parameters that might be selected during an inspection flow
     * so that one may start completely fresh. May be called, for example, by a button to start a flow.
     */
    public void clearSelections() {
        setSelectedChecklistTemplate(new OccChecklistTemplate());
        setSelectedInspector(new User());

        setSelectedLocDescriptor(new OccLocationDescriptor());
    }

    // getters & setters below you know the drill

    public List<OccChecklistTemplate> getChecklistTemplateList() {
        return checklistTemplateList;
    }

    public List<User> getUserList() {
        return userList;
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

    public OccLocationDescriptor getSelectedLocDescriptor() {
        return selectedLocDescriptor;
    }

    public void setSelectedLocDescriptor(OccLocationDescriptor selectedLocDescriptor) {
        this.selectedLocDescriptor = selectedLocDescriptor;
    }

    public OccLocationDescriptor getSkeletonLocationDescriptor() {
        return skeletonLocationDescriptor;
    }

    public OccInspection getSelectedInspection() {
        return selectedInspection;
    }

    public void setSelectedInspection(OccInspection selectedInspection) {
        this.selectedInspection = selectedInspection;
    }

    public OccInspectedSpace getSelectedInspectedSpace() {
        return selectedInspectedSpace;
    }

    public void setSelectedInspectedSpace(OccInspectedSpace selectedInspectedSpace) {
        this.selectedInspectedSpace = selectedInspectedSpace;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public void toggleEditMode() {
        editMode = !editMode;
    }
}
