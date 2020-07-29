/*
 * Copyright (C) 2020 Technology Rediscovery
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
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.InspectionException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.occupancy.OccApplicationStatusEnum;
import com.tcvcog.tcvce.entities.occupancy.OccPermitApplication;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveListsEnum;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Nathan Dietz
 */
public class OccPermitManageBB extends BackingBeanUtils implements Serializable {

    private String currentMode;
    private boolean currentApplicationSelected;
    private boolean unitAlreadyDetermined; //used by occPermitNewPeriod.xhtml to flag whether or not the selected path has already determined a unit.
    private PropertyUnit unitForApplication;
    private Property propertyForApplication;
    private List<ViewOptionsActiveListsEnum> allViewOptions;
    private ViewOptionsActiveListsEnum currentViewOption;

    private OccPermitApplication searchParams;
    private List<OccApplicationStatusEnum> statusList;
    private LocalDateTime queryBegin;
    private LocalDateTime queryEnd;
    private boolean reason_ctl;
    private boolean status_ctl;
    private boolean connectedOccPeriod_ctl;
    private boolean connectedOccPeriod_val;

    private String rejectedApplicationMessage;
    private String invalidApplicationMessage;
    private String acceptApplicationMessage;
    private OccApplicationStatusEnum newStatus;

    private OccPermitApplication selectedApplication;
//    private QueryOccPermitApplication selectedQueryOccApplicaton;
    private List<OccPermitApplication> applicationList;
    private List<PropertyUnit> unitList;

    public OccPermitManageBB() {
    }

    @PostConstruct
    public void initBean() {

        //initialize default current mode : Lookup
        currentMode = "Search";

        searchParams = new OccPermitApplication();

        statusList = new ArrayList<>();

        statusList.addAll(Arrays.asList(OccApplicationStatusEnum.values()));

        unitForApplication = getSessionBean().getSessPropertyUnit();

        selectedApplication = getSessionBean().getSessOccPermitApplication();

        unitAlreadyDetermined = getSessionBean().isUnitDetermined();
        
        
        
        try {
            PropertyCoordinator pc = getPropertyCoordinator();
            propertyForApplication = pc.getPropertyByPropUnitID(selectedApplication.getApplicationPropertyUnit().getUnitID());
        } catch (IntegrationException ex) {
            System.out.println("OccPermitManageBB.initBean() | ERROR: " + ex);
        } catch (NullPointerException ex) {
            //do nothing, this is just to check if anything is null in the method call above
        }
        
        allViewOptions = Arrays.asList(ViewOptionsActiveListsEnum.values());

        if (currentViewOption == null && propertyForApplication != null) {

            setCurrentViewOption(ViewOptionsActiveListsEnum.VIEW_ACTIVE);

        }

        //initialize default setting         
        defaultSetting();
    }

    /**
     * Determines whether or not a user should currently be able to select an
     * application. Users should only select CEARs if they're in search mode.
     *
     * @return
     */
    public boolean getSelectedButtonActive() {
        return !"Search".equals(currentMode);
    }

    public boolean getActiveSearchMode() {
        return "Search".equals(currentMode);
    }

    public boolean getActiveActionsMode() {
        return "Actions".equals(currentMode);
    }

    public boolean getActiveObjectsMode() {
        return "Objects".equals(currentMode);
    }

    public boolean getActiveNotesMode() {
        return "Notes".equals(currentMode);
    }

    public String getCurrentMode() {
        return currentMode;
    }

    public void defaultSetting() {

        OccupancyIntegrator oi = getOccupancyIntegrator();

        currentApplicationSelected = false;

        try {
            applicationList = oi.getOccPermitApplicationList();

        } catch (BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error while retrieving Occupancy Permit Applications: " + ex, ""));
        } catch (AuthorizationException | EventException | IntegrationException | ViolationException ex) {
            System.out.println("OccPermitManageBB.defaultSetting() | ERROR: " + ex);
        }
    }

    /**
     *
     * @param currentMode Search, Actions, Object, Notes
     */
    public void setCurrentMode(String currentMode) {

        //store currentMode into tempCurMode as a temporary value, in case the currenMode equal null
        String tempCurMode = this.currentMode;
        //reset default setting every time the Mode has been selected 
//        defaultSetting();
        //check the currentMode == null or not
        if (currentMode == null) {
            this.currentMode = tempCurMode;
        } else {
            this.currentMode = currentMode;
        }
        //show the current mode in p:messages box
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, this.currentMode + " Mode Selected", ""));

    }

    public void executeCustomQuery() {

        OccupancyIntegrator oi = getOccupancyIntegrator();

        currentApplicationSelected = false;

        if (queryBegin == null || queryEnd == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please specify a start and end date when executing a custom query", ""));
            return;
        }

        try {
            List<OccPermitApplication> results = oi.getOccPermitApplicationList();

            Iterator itr = results.iterator();

            while (itr.hasNext()) {

                OccPermitApplication temp = (OccPermitApplication) itr.next();

                //A variable that represents the result of a question we ask about an application.
                //If the answer is different as the given search parameter, remove the application
                boolean testCondition = false;

                if (temp.getSubmissionDate().compareTo(queryBegin) < 0 || temp.getSubmissionDate().compareTo(queryEnd) > 0) {
                    itr.remove();
                } else if (reason_ctl) {

                    if (temp.getReason().getId() != searchParams.getReason().getId()) {
                        itr.remove();
                    }

                } else if (status_ctl) {
                    if (temp.getStatus() != searchParams.getStatus()) {
                        itr.remove();
                    }
                } else if (connectedOccPeriod_ctl) {

                    testCondition = temp.getConnectedPeriod() != null && temp.getConnectedPeriod().getPeriodID() != 0; //test if an occperiod is connected

                    if (testCondition != connectedOccPeriod_val) {
                        //if the test does not evaluate to our desired state, remove it
                        itr.remove();
                    }

                }

            }

            applicationList = results;

        } catch (BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error while retrieving Occupancy Permit Applications: " + ex, ""));
        } catch (AuthorizationException | EventException | IntegrationException | ViolationException ex) {
            System.out.println("OccPermitManageBB.defaultSetting() | ERROR: " + ex);
        }

    }

    public void onApplicationSelection(OccPermitApplication application) {

        if (currentApplicationSelected) {
            selectedApplication = application;

            applicationList = new ArrayList<>();

            applicationList.add(application);

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Currently Selected Application: " + application.getReason().getTitle() + "ID:(" + application.getId() + ")", ""));
        } else {
            defaultSetting();

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Default Application: " + application.getReason().getTitle() + "ID:(" + application.getId() + ")", ""));

        }

    }

    public void changeApplicationStatus(ActionEvent ev) {
        System.out.println("updateStatus");

        if (newStatus == null) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a status before applying your changes.", ""));
        } else {
            updateSelectedPermitStatus(newStatus);
            defaultSetting();

        }

    }

    /**
     * A method that changes only the selectedApplication's status.
     *
     * @param statusToSet
     */
    private void updateSelectedPermitStatus(OccApplicationStatusEnum statusToSet) {
        OccupancyIntegrator oi = getOccupancyIntegrator();

        try {
            OccPermitApplication app = oi.getOccPermitApplication(selectedApplication.getId());
            oi.updateOccPermitApplication(app);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Status changed on application ID " + selectedApplication.getId(), ""));
            defaultSetting();
        } catch (IntegrationException | AuthorizationException
                | BObStatusException | EventException | ViolationException ex) {
            System.out.println("OccPermitManageBB.updateSelectedPermitStatus() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occured while trying to update application status.", ""));
        }
    }

    public void removeSelectedUnit(PropertyUnit unit) {
        propertyForApplication.getUnitList().remove(unit);
    }

    public void addNewUnit() {
        PropertyUnit newUnit = new PropertyUnit();
        newUnit.setPropertyID(propertyForApplication.getPropertyID());
        newUnit.setActive(true);
        propertyForApplication.getUnitList().add(newUnit);
    }

    public String attachToOccPeriod() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        PropertyCoordinator pc = getPropertyCoordinator();
        try {
            
            if(!unitAlreadyDetermined){

                //If we edited the unit list, let's save it to the database
                
                List<PropertyUnit> temp = pc.applyUnitList(unitList, propertyForApplication);
            
                for(PropertyUnit unit : temp){
                    if (unit.getUnitNumber().contentEquals(selectedApplication.getApplicationPropertyUnit().getUnitNumber())){
                        selectedApplication.setApplicationPropertyUnit(unit);
                    }
                }
            }
            
            int newPeriodID = oc.attachApplicationToNewOccPeriod(selectedApplication, acceptApplicationMessage);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Application successfully attached to Occ Period!", ""));
            
            selectedApplication.setConnectedPeriod(oc.getOccPeriod(newPeriodID));
            
        } catch (AuthorizationException | EventException | InspectionException | IntegrationException | ViolationException ex) {
            System.out.println("OccPermitManageBB.attachToOccPeriod() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occured while trying to attach the application to an Occ Period!", ""));
            selectedApplication.setStatus(OccApplicationStatusEnum.Waiting);
        } catch (BObStatusException ex) {
            System.out.println("OccPermitManageBB.attachToOccPeriod() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occured while trying to attach the application to an Occ Period: " + ex, ""));
            selectedApplication.setStatus(OccApplicationStatusEnum.Waiting);
        }

        getSessionBean().setSessOccPermitApplication(selectedApplication);

        return getSessionBean().getNavStack().popLastPage();
    }

    public String cancelAttachment() {

        selectedApplication.setStatus(OccApplicationStatusEnum.Waiting);

        getSessionBean().setSessOccPermitApplication(selectedApplication);

        return getSessionBean().getNavStack().popLastPage();
    }

    public String path1SpawnNewOccPeriod(){
        
        selectedApplication.setStatus(OccApplicationStatusEnum.OldUnit);

        getSessionBean().setSessOccPermitApplication(selectedApplication);
        
        getSessionBean().getNavStack().pushCurrentPage();
        
        getSessionBean().setUnitDetermined(true);
        
        return "occPermitNewPeriod";
        
    }
    
    public String path2NewUnitAndPeriod(){
        
        selectedApplication.setStatus(OccApplicationStatusEnum.NewUnit);

        getSessionBean().setSessOccPermitApplication(selectedApplication);
        
        getSessionBean().getNavStack().pushCurrentPage();
        
        getSessionBean().setUnitDetermined(false);
        
        return "occPermitNewPeriod";
        
    }
    
    public void path3AttachRejectionMessage(ActionEvent ev) {

        SystemCoordinator sc = getSystemCoordinator();

        OccupancyIntegrator oi = getOccupancyIntegrator();

        selectedApplication.setStatus(OccApplicationStatusEnum.Rejected);

        // build message to document change
        MessageBuilderParams mcc = new MessageBuilderParams();
        mcc.setUser(getSessionBean().getSessUser());
        mcc.setExistingContent(selectedApplication.getExternalPublicNotes());
        mcc.setHeader(getResourceBundle(Constants.MESSAGE_TEXT).getString("rejectedOccPermitApplicationHeader"));
        mcc.setExplanation(getResourceBundle(Constants.MESSAGE_TEXT).getString("rejectedOccPermitApplicationExplanation"));
        mcc.setNewMessageContent(rejectedApplicationMessage);

        selectedApplication.setExternalPublicNotes(sc.appendNoteBlock(mcc));
        try {
            oi.updateOccPermitApplication(selectedApplication);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Public note added to Occupancy permit application ID " + selectedApplication.getId() + ".", ""));

        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to write message to The Database",
                            "This is a system level error that must be corrected by a sys admin--sorries!."));
        }
    }

    public void path4AttachInvalidMessage(ActionEvent ev) {

        SystemCoordinator sc = getSystemCoordinator();

        OccupancyIntegrator oi = getOccupancyIntegrator();

        selectedApplication.setStatus(OccApplicationStatusEnum.Invalid);

        // build message to document change
        MessageBuilderParams mcc = new MessageBuilderParams();
        mcc.setUser(getSessionBean().getSessUser());
        mcc.setExistingContent(selectedApplication.getExternalPublicNotes());
        mcc.setHeader(getResourceBundle(Constants.MESSAGE_TEXT).getString("invalidOccPermitApplicationHeader"));
        mcc.setExplanation(getResourceBundle(Constants.MESSAGE_TEXT).getString("invalidOccPermitApplicationExplanation"));
        mcc.setNewMessageContent(invalidApplicationMessage);

        selectedApplication.setExternalPublicNotes(sc.appendNoteBlock(mcc));
        try {
            oi.updateOccPermitApplication(selectedApplication);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Public note added to Occupancy permit application ID " + selectedApplication.getId() + ".", ""));

        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to write message to The Database",
                            "This is a system level error that must be corrected by a sys admin--sorries!."));
        }
    }

    public boolean isPathsDisabled() {

        if (selectedApplication == null) {
            return true;
        }

        return selectedApplication.getStatus() != OccApplicationStatusEnum.Waiting; //paths should be disabled if the occ application is not waiting to be reviewed.

    }

    public boolean isCurrentApplicationSelected() {
        return currentApplicationSelected;
    }

    public void setCurrentApplicationSelected(boolean currentApplicationSelected) {
        this.currentApplicationSelected = currentApplicationSelected;
    }

    public List<OccPermitApplication> getApplicationList() {
        return applicationList;
    }

    public void setApplicationList(List<OccPermitApplication> applicationList) {
        this.applicationList = applicationList;
    }

    public OccPermitApplication getSelectedApplication() {
        return selectedApplication;
    }

    public void setSelectedApplication(OccPermitApplication selectedApplication) {
        this.selectedApplication = selectedApplication;
    }

    public OccPermitApplication getSearchParams() {
        return searchParams;
    }

    public void setSearchParams(OccPermitApplication searchParams) {
        this.searchParams = searchParams;
    }

    public boolean isReason_ctl() {
        return reason_ctl;
    }

    public void setReason_ctl(boolean reason_ctl) {
        this.reason_ctl = reason_ctl;
    }

    public boolean isStatus_ctl() {
        return status_ctl;
    }

    public void setStatus_ctl(boolean status_ctl) {
        this.status_ctl = status_ctl;
    }

    public boolean isConnectedOccPeriod_ctl() {
        return connectedOccPeriod_ctl;
    }

    public void setConnectedOccPeriod_ctl(boolean connectedOccPeriod_ctl) {
        this.connectedOccPeriod_ctl = connectedOccPeriod_ctl;
    }

    public boolean isConnectedOccPeriod_val() {
        return connectedOccPeriod_val;
    }

    public void setConnectedOccPeriod_val(boolean connectedOccPeriod_val) {
        this.connectedOccPeriod_val = connectedOccPeriod_val;
    }

    public LocalDateTime getQueryBegin() {
        return queryBegin;
    }

    public void setQueryBegin(LocalDateTime queryBegin) {
        this.queryBegin = queryBegin;
    }

    public Date getQueryBegin_Util() {

        Date utilDate = null;
        if (queryBegin != null) {
            utilDate = Date.from(queryBegin.atZone(ZoneId.systemDefault()).toInstant());
        }
        return utilDate;
    }

    public void setQueryBegin_Util(Date queryBeginUtil) {
        if (queryBeginUtil != null) {
            queryBegin = queryBeginUtil.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
    }

    public LocalDateTime getQueryEnd() {
        return queryEnd;
    }

    public void setQueryEnd(LocalDateTime queryEnd) {
        this.queryEnd = queryEnd;
    }

    public Date getQueryEnd_Util() {
        Date utilDate = null;
        if (queryEnd != null) {
            utilDate = Date.from(queryEnd.atZone(ZoneId.systemDefault()).toInstant());
        }
        return utilDate;
    }

    public void setQueryEnd_Util(Date queryEndUtil) {
        if (queryEndUtil != null) {
            queryEnd = queryEndUtil.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
    }

    public List<OccApplicationStatusEnum> getStatusList() {
        return statusList;
    }

    public void setStatusList(List<OccApplicationStatusEnum> statusList) {
        this.statusList = statusList;
    }

    public String getRejectedApplicationMessage() {
        return rejectedApplicationMessage;
    }

    public void setRejectedApplicationMessage(String rejectedApplicationMessage) {
        this.rejectedApplicationMessage = rejectedApplicationMessage;
    }

    public String getInvalidApplicationMessage() {
        return invalidApplicationMessage;
    }

    public void setInvalidApplicationMessage(String invalidApplicationMessage) {
        this.invalidApplicationMessage = invalidApplicationMessage;
    }

    public OccApplicationStatusEnum getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(OccApplicationStatusEnum newStatus) {
        this.newStatus = newStatus;
    }

    public boolean isUnitAlreadyDetermined() {
        return unitAlreadyDetermined;
    }

    public void setUnitAlreadyDetermined(boolean unitAlreadyDetermined) {
        this.unitAlreadyDetermined = unitAlreadyDetermined;
    }

    public PropertyUnit getUnitForApplication() {
        return unitForApplication;
    }

    public void setUnitForApplication(PropertyUnit unitForApplication) {
        this.unitForApplication = unitForApplication;
    }

    public Property getPropertyForApplication() {
        return propertyForApplication;
    }

    public void setPropertyForApplication(Property propertyForApplication) {
        this.propertyForApplication = propertyForApplication;
    }

    public String getAcceptApplicationMessage() {
        return acceptApplicationMessage;
    }

    public void setAcceptApplicationMessage(String acceptApplicationMessage) {
        this.acceptApplicationMessage = acceptApplicationMessage;
    }

    public List<ViewOptionsActiveListsEnum> getAllViewOptions() {
        return allViewOptions;
    }

    public void setAllViewOptions(List<ViewOptionsActiveListsEnum> allViewOptions) {
        this.allViewOptions = allViewOptions;
    }

    public ViewOptionsActiveListsEnum getCurrentViewOption() {
        return currentViewOption;
    }

    public void setCurrentViewOption(ViewOptionsActiveListsEnum input) {

        currentViewOption = input;

        unitList = new ArrayList<>();

        if (null == currentViewOption) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occurred while trying to set the current view option. Returning to default.", ""));
            currentViewOption = ViewOptionsActiveListsEnum.VIEW_ACTIVE;
        } else {
            switch (currentViewOption) {
                case VIEW_ALL:
                    unitList.addAll(propertyForApplication.getUnitList());
                    break;

                case VIEW_ACTIVE:
                    for (PropertyUnit unit : propertyForApplication.getUnitList()) {
                        if (unit.isActive()) {
                            unitList.add(unit);
                        }
                    }

                    break;

                case VIEW_INACTIVE:
                    for (PropertyUnit unit : propertyForApplication.getUnitList()) {
                        if (!unit.isActive()) {
                            unitList.add(unit);
                        }
                    }

                    break;
            }

        }

        
    }

    public List<PropertyUnit> getUnitList() {
        return unitList;
    }

    public void setUnitList(List<PropertyUnit> unitList) {
        this.unitList = unitList;
    }
    
}
