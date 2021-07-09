package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.occupancy.OccChecklistTemplate;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.List;

/**
 * The premier backing bean for occupancy inspections workflow.
 *
 * @author jurplel
 */
public class OccInspectionsBB extends BackingBeanUtils implements Serializable {

    private List<OccChecklistTemplate> checklistTemplateList;

    private List<User> userList;

    private OccChecklistTemplate selectedChecklistTemplate;

    private User selectedInspector;

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
        System.out.println("set selected inspector");
        this.selectedInspector = selectedInspector;
    }

}
