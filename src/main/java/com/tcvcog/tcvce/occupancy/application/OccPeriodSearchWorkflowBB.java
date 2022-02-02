/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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
import com.tcvcog.tcvce.application.SessionBean;
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodDataHeavy;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodPropertyUnitHeavy;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodType;
import com.tcvcog.tcvce.entities.search.QueryOccPeriod;
import com.tcvcog.tcvce.entities.search.SearchParamsOccPeriod;
import com.tcvcog.tcvce.integration.PropertyIntegrator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

/**
 * Backer for the OccPeriod
 * @author Ellen Bascomb, JURPLEL
 */
public class OccPeriodSearchWorkflowBB
        extends BackingBeanUtils {

    private OccPeriod lastSavedOccPeriod;
    private OccPeriodDataHeavy currentOccPeriod;
    private PropertyUnit currentPropertyUnit;

//  *******************************
//  ************ WORKFLOW**********
//  *******************************
    private List<OccPeriodType> occPeriodTypeList;
    private OccPeriodType selectedOccPeriodType;

    private List<PropertyUnit> propertyUnitCandidateList;
    private PropertyUnit selectedPropertyUnit;

    private List<User> managerInspectorCandidateList;
    private User selectedManager;


//  *******************************
//  ************ SEARCH ***********
//  *******************************
    private List<OccPeriodType> search_occPeriodTypeList;

    private List<QueryOccPeriod> occPeriodQueryList;
    private QueryOccPeriod occPeriodQuerySelected;

    private List<Property> search_propList;
    protected List<Person> search_personList;

    private List<OccPeriodPropertyUnitHeavy> occPeriodList;
    private List<OccPeriodPropertyUnitHeavy> occPeriodListFiltered;
    private boolean appendResultsToList;

    private SearchParamsOccPeriod searchParamsSelected;

    /**
     * Creates a new instance of OccPeriodSearchWorkflowBB
     */
    public OccPeriodSearchWorkflowBB() {
    }

    /**
     * Assume a zen-like state and initialize this bean
     */
    @PostConstruct
    public void initBean() {
        System.out.printf("OccPeriodSearchWorkflowBB constructed");
        OccupancyCoordinator oc = getOccupancyCoordinator();
        SessionBean sb = getSessionBean();

        currentOccPeriod = sb.getSessOccPeriod();
        
        setLastSavedOccPeriod(new OccPeriod(currentOccPeriod));
        PropertyIntegrator pi = getPropertyIntegrator();

        setOccPeriodTypeList(sb.getSessMuni().getProfile().getOccPeriodTypeList());

        try {
            currentPropertyUnit = pi.getPropertyUnitWithProp(currentOccPeriod.getPropertyUnitID());
            setPropertyUnitCandidateList(sb.getSessProperty().getUnitList());
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
        }


        search_occPeriodTypeList = sb.getSessMuni().getProfile().getOccPeriodTypeList();
        occPeriodList = sb.getSessOccPeriodList();

        if (occPeriodList != null && occPeriodList.isEmpty()) {
            occPeriodList = new ArrayList();
        }
        appendResultsToList = false;
        occPeriodQueryList = sb.getQueryOccPeriodList();
        if (occPeriodQueryList != null && !occPeriodQueryList.isEmpty()) {
            occPeriodQuerySelected = occPeriodQueryList.get(0);
        }
        search_propList = sb.getSessPropertyList();
        search_personList = sb.getSessPersonList();

        configureParameters();
    }

    /**
     * Sets up search fields
     */
    private void configureParameters() {
        if (occPeriodQuerySelected != null
                &&
                occPeriodQuerySelected.getParamsList() != null
                &&
                !occPeriodQuerySelected.getParamsList().isEmpty()) {

            searchParamsSelected = occPeriodQuerySelected.getParamsList().get(0);
        } else {
            searchParamsSelected = null;
        }
    }


    /**
     * Gets an updated instance of the current OccPeriod
     */
    public void reloadCurrentOccPeriodDataHeavy() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            currentOccPeriod = oc.assembleOccPeriodDataHeavy(currentOccPeriod, getSessionBean().getSessUser().getMyCredential());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Reloaded occ period ID " + currentOccPeriod.getPeriodID(), ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to reload occ period", ""));
        } catch (SearchException ex) {
            System.out.println(ex);
        }

    }


    public int getPeriodListSize() {
        int s = 0;
        if (occPeriodList != null && !occPeriodList.isEmpty()) {
            s = occPeriodList.size();
        }
        return s;
    }


    /**
     * Listener for user requests to certify a field on the OccPeriod
     */
    public void certifyOccPeriodField() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        FacesContext context = getFacesContext();
        String field = context.getExternalContext().getRequestParameterMap().get("fieldtocertify");

        System.out.println("OccPeriodSearchWorkflowBB.certifyOccPeriodField | field: " + field);

        UserAuthorized u = getSessionBean().getSessUser();
        LocalDateTime now = LocalDateTime.now();

        // Set actual values
        switch (field) {
            case "occperiodtype":
                if (currentOccPeriod.getPeriodTypeCertifiedBy() == null) {
                    currentOccPeriod.setPeriodTypeCertifiedBy(u);
                    currentOccPeriod.setPeriodTypeCertifiedTS(now);
                } else {
                    currentOccPeriod.setPeriodTypeCertifiedBy(null);
                    currentOccPeriod.setPeriodTypeCertifiedTS(null);
                }
                break;
            case "startdate":
                if (currentOccPeriod.getStartDateCertifiedBy() == null) {
                    currentOccPeriod.setStartDateCertifiedBy(u);
                    currentOccPeriod.setStartDateCertifiedTS(now);
                } else {
                    currentOccPeriod.setStartDateCertifiedBy(null);
                    currentOccPeriod.setStartDateCertifiedTS(null);
                }
                break;
            case "enddate":
                if (currentOccPeriod.getEndDateCertifiedBy() == null) {
                    currentOccPeriod.setEndDateCertifiedBy(u);
                    currentOccPeriod.setEndDateCertifiedTS(now);
                } else {
                    currentOccPeriod.setEndDateCertifiedBy(null);
                    currentOccPeriod.setEndDateCertifiedTS(null);
                }
                break;
            default:
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error! Unable to certify field", ""));
        }

        saveOccPeriodChanges();
    }


    /**
     * Listener for the requests to authorize or unauthorize the OccPeriod
     */
    public void toggleOccPeriodAuthorization() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.toggleOccPeriodAuthorization(currentOccPeriod, getSessionBean().getSessUser());
            if (currentOccPeriod.getAuthorizedBy() != null) {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Success! Occupancy period ID " + currentOccPeriod.getPeriodID()
                                + " is now authorized and permits can be generated.", ""));
            } else {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Occupancy period ID " + currentOccPeriod.getPeriodID()
                                + " has been successfully deauthorized.", ""));
            }
        } catch (AuthorizationException | BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.getMessage(), ""));
        }
    }


    /**
     * utility pass through method to be called when loading Occperiod advanced settings
     *
     */
    public void updateOccPeriodInitialize() {

    }

    /**
     * This method attempts to update the database entry for the currentOccPeriod.
     * It will fail in certain conditions, in which case the currentOccPeriod is returned to
     * a backup made before any current unsaved changes.
     */
    public void saveOccPeriodChanges() {
        OccupancyCoordinator oc = getOccupancyCoordinator();

        try {
            oc.editOccPeriod(currentOccPeriod, getSessionBean().getSessUser());
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Save successful on OccPeriod ID: " + currentOccPeriod.getPeriodID(), ""));
            System.out.println("OccPeriodSearchWorkflowBB.saveOccPeriodChanges successful");

            // Set backup copy in case of failure if saving to database succeeds
            setLastSavedOccPeriod(new OccPeriod(currentOccPeriod));
        } catch (IntegrationException | BObStatusException ex) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.getMessage(), ""));
            System.out.println("OccPeriodSearchWorkflowBB.saveOccPeriodChanges failure");

            // Restore working copy of occ period to last working one if saving to database fails.
            discardOccPeriodChanges();
        }
    }

    /**
     * This method will discard the changes to the current working occ period and
     * set its value to that of the occ period present in the last successful save.
     */
    public void discardOccPeriodChanges() {
        System.out.println("OccPeriodSearchWorkflowBB.discardOccPeriodChanges");

        currentOccPeriod = new OccPeriodDataHeavy(getLastSavedOccPeriod());
    }

    /**
     * Listener for user request to map the current occ period to a a new property
     * unit
     */
    public void updatePeriodPropUnit() {
//        OccupancyCoordinator oc = getOccupancyCoordinator();
//        try {
//            oc.updateOccPeriodPropUnit(currentOccPeriod, getSelectedPropertyUnit());
//             getFacesContext().addMessage(null,
//                new FacesMessage(FacesMessage.SEVERITY_INFO,
//                "The current occupancy period has been assigned to property unit ID " + getSelectedPropertyUnit().getUnitID(), ""));
//        } catch (IntegrationException ex) {
//            System.out.println(ex);
//             getFacesContext().addMessage(null,
//                new FacesMessage(FacesMessage.SEVERITY_ERROR,
//                ex.getMessage(), ""));
//        }
//        reloadCurrentOccPeriodDataHeavy();
    }

    /**
     * Listener for requests to go view the property profile of a property associated
     * with the given case
     * largely copied from CECaseSearchProfileBB (Maybe this should be in BackingBeanUtils?)
     *
     * @return
     */
    public String exploreProperty() {
        // TODO: update for humanziation
//            getSessionBean().setSessProperty(currentPropertyUnit.);
        return "propertyInfo";

    }

    /**
     * Listener for requests to reload the current OccPeriodDataHeavy
     * Concept copied from CECaseSearchProfile
     *
     * @return
     */
    public String refreshCurrentPeriod() {
        return "occPeriodWorkflow";

    }


    /**
     * Loads an OccPeriodDataHeavy and injects it into the session bean
     * and sends the user to the Workflow/status page
     *
     * @param op
     * @return
     */
    public String exploreOccPeriod(OccPeriod op) {
        SystemCoordinator sc = getSystemCoordinator();
        PropertyCoordinator pc = getPropertyCoordinator();
        OccupancyCoordinator oc = getOccupancyCoordinator();
        Credential cred = getSessionBean().getSessUser().getMyCredential();

        try {
            getSessionBean().setSessOccPeriod(oc.assembleOccPeriodDataHeavy(op, cred));
            getSessionBean().setSessProperty(pc.getPropertyDataHeavyByUnit(op.getPropertyUnitID(), getSessionBean().getSessUser()));
            sc.logObjectView(getSessionBean().getSessUser(), op);
        } catch (IntegrationException | BObStatusException | AuthorizationException | EventException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to assemble the data-rich occ period", ""));

        }

        return "occPeriodWorkflow";
    }

    /**
     * Listener method for requests from the user to clear the results list
     *
     */
    public void clearOccPeriodList() {
        if (occPeriodList != null) {
            occPeriodList.clear();
        }
    }

    /**
     * Asks the Coordinator for the OccPeriod viewing history
     */
    public void loadOccPeriodHistory() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            occPeriodList.addAll(oc.getOccPeriodPropertyUnitHeavyList(oc.assembleOccPeriodHistoryList(getSessionBean().getSessUser().getMyCredential())));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to assemble the data-rich occ period", ""));

        }
    }

    /**
     * Entry way into the Query world via the SearchCoordinator who is responsible
     * for calling appropriate Coordinators and configuration methods and such
     */
    public void executeQuery() {
        System.out.println("OccPeriodSearchWorkflowBB.executeQuery");
        SearchCoordinator sc = getSearchCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
        int listSize = 0;

        if (!appendResultsToList) {
            occPeriodList.clear();
        }
        try {
            occPeriodList.addAll(sc.runQuery(occPeriodQuerySelected).getBOBResultList());
            if (occPeriodList != null) {
                listSize = occPeriodList.size();
            }
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Your query completed with " + listSize + " results", ""));
        } catch (SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not query for Periods due to search errors, sorry.", ""));
        }
    }

    /**
     * Listener for user requests to start a new query
     */
    public void changeQuerySelected() {

        configureParameters();
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "New query loaded!", ""));
    }

    /**
     * Listener for user requests to clear out and restart the current Query
     */
    public void resetQuery() {
        SearchCoordinator sc = getSearchCoordinator();
        occPeriodQueryList = sc.buildQueryOccPeriodList(getSessionBean().getSessUser().getMyCredential());
        if (occPeriodQueryList != null && !occPeriodQueryList.isEmpty()) {
            occPeriodQuerySelected = occPeriodQueryList.get(0);
        }
        configureParameters();

    }
    
    /**
     * Listener for user requests to view or add blobs to this period
     * @param ev
     */
    public void manageBlobsOnOccPeriod(ActionEvent ev){
        try {
            getSessionBean().setAndRefreshSessionBlobHolderAndBuildUpstreamPool(currentOccPeriod);
        } catch (BObStatusException | BlobException | IntegrationException ex) {
            System.out.println(ex);
            
        } 
        
    }


    /**
     * @return the searchParamsSelected
     */
    public SearchParamsOccPeriod getSearchParamsSelected() {
        return searchParamsSelected;
    }

    /**
     * @param searchParamsSelected the searchParamsSelected to set
     */
    public void setSearchParamsSelected(SearchParamsOccPeriod searchParamsSelected) {
        this.searchParamsSelected = searchParamsSelected;
    }

    /**
     * @return the currentOccPeriod
     */
    public OccPeriodDataHeavy getCurrentOccPeriod() {
        return currentOccPeriod;
    }

    /**
     * @param currentOccPeriod the currentOccPeriod to set
     */
    public void setCurrentOccPeriod(OccPeriodDataHeavy currentOccPeriod) {
        this.currentOccPeriod = currentOccPeriod;
    }

    /**
     * @return the currentPropertyUnit
     */
    public PropertyUnit getCurrentPropertyUnit() {
        return currentPropertyUnit;
    }

    /**
     * @return the occPeriodList
     */
    public List<OccPeriodPropertyUnitHeavy> getOccPeriodList() {
        return occPeriodList;
    }

    /**
     * @return the occPeriodListFiltered
     */
    public List<OccPeriodPropertyUnitHeavy> getOccPeriodListFiltered() {
        return occPeriodListFiltered;
    }

    /**
     * @return the occPeriodQueryList
     */
    public List<QueryOccPeriod> getOccPeriodQueryList() {
        return occPeriodQueryList;
    }

    /**
     * @return the occPeriodQuerySelected
     */
    public QueryOccPeriod getOccPeriodQuerySelected() {
        return occPeriodQuerySelected;
    }

    /**
     * @param occPeriodList the occPeriodList to set
     */
    public void setOccPeriodList(List<OccPeriodPropertyUnitHeavy> occPeriodList) {
        this.occPeriodList = occPeriodList;
    }

    /**
     * @param occPeriodListFiltered the occPeriodListFiltered to set
     */
    public void setOccPeriodListFiltered(List<OccPeriodPropertyUnitHeavy> occPeriodListFiltered) {
        this.occPeriodListFiltered = occPeriodListFiltered;
    }

    /**
     * @param occPeriodQueryList the occPeriodQueryList to set
     */
    public void setOccPeriodQueryList(List<QueryOccPeriod> occPeriodQueryList) {
        this.occPeriodQueryList = occPeriodQueryList;
    }

    /**
     * @param occPeriodQuerySelected the occPeriodQuerySelected to set
     */
    public void setOccPeriodQuerySelected(QueryOccPeriod occPeriodQuerySelected) {
        this.occPeriodQuerySelected = occPeriodQuerySelected;
    }

    /**
     * @return the search_occPeriodTypeList
     */
    public List<OccPeriodType> getSearch_occPeriodTypeList() {
        return search_occPeriodTypeList;
    }

    /**
     * @param search_occPeriodTypeList the search_occPeriodTypeList to set
     */
    public void setSearch_occPeriodTypeList(List<OccPeriodType> search_occPeriodTypeList) {
        this.search_occPeriodTypeList = search_occPeriodTypeList;
    }

    /**
     * @return the appendResultsToList
     */
    public boolean isAppendResultsToList() {
        return appendResultsToList;
    }

    /**
     * @param appendResultsToList the appendResultsToList to set
     */
    public void setAppendResultsToList(boolean appendResultsToList) {
        this.appendResultsToList = appendResultsToList;
    }

    /**
     * @return the search_propList
     */
    public List<Property> getSearch_propList() {
        return search_propList;
    }

    /**
     * @param search_propList the search_propList to set
     */
    public void setSearch_propList(List<Property> search_propList) {
        this.search_propList = search_propList;
    }

    /**
     * @return the search_personList
     */
    public List<Person> getSearch_personList() {
        return search_personList;
    }

    /**
     * @param search_personList the search_personList to set
     */
    public void setSearch_personList(List<Person> search_personList) {
        this.search_personList = search_personList;
    }

    /**
     * @return the lastSavedOccPeriod
     */
    public OccPeriod getLastSavedOccPeriod() {
        return lastSavedOccPeriod;
    }

    /**
     * @return the occPeriodTypeList
     */
    public List<OccPeriodType> getOccPeriodTypeList() {
        return occPeriodTypeList;
    }

    /**
     * @return the selectedPropertyUnit
     */
    public PropertyUnit getSelectedPropertyUnit() {
        return selectedPropertyUnit;
    }

    /**
     * @return the managerInspectorCandidateList
     */
    public List<User> getManagerInspectorCandidateList() {
        return managerInspectorCandidateList;
    }

    /**
     * @return the selectedManager
     */
    public User getSelectedManager() {
        return selectedManager;
    }

    /**
     * @param lastSavedOccPeriod the lastSavedOccPeriod to set
     */
    public void setLastSavedOccPeriod(OccPeriod lastSavedOccPeriod) {
        this.lastSavedOccPeriod = lastSavedOccPeriod;
    }

    /**
     * @param occPeriodTypeList the occPeriodTypeList to set
     */
    public void setOccPeriodTypeList(List<OccPeriodType> occPeriodTypeList) {
        this.occPeriodTypeList = occPeriodTypeList;
    }

    /**
     * @param selectedPropertyUnit the selectedPropertyUnit to set
     */
    public void setSelectedPropertyUnit(PropertyUnit selectedPropertyUnit) {
        this.selectedPropertyUnit = selectedPropertyUnit;
    }

    /**
     * @param managerInspectorCandidateList the managerInspectorCandidateList to set
     */
    public void setManagerInspectorCandidateList(List<User> managerInspectorCandidateList) {
        this.managerInspectorCandidateList = managerInspectorCandidateList;
    }

    /**
     * @param selectedManager the selectedManager to set
     */
    public void setSelectedManager(User selectedManager) {
        this.selectedManager = selectedManager;
    }

    /**
     * @return the selectedOccPeriodType
     */
    public OccPeriodType getSelectedOccPeriodType() {
        return selectedOccPeriodType;
    }

    /**
     * @return the propertyUnitCandidateList
     */
    public List<PropertyUnit> getPropertyUnitCandidateList() {
        return propertyUnitCandidateList;
    }

    /**
     * @param selectedOccPeriodType the selectedOccPeriodType to set
     */
    public void setSelectedOccPeriodType(OccPeriodType selectedOccPeriodType) {
        this.selectedOccPeriodType = selectedOccPeriodType;
    }

    /**
     * @param propertyUnitCandidateList the propertyUnitCandidateList to set
     */
    public void setPropertyUnitCandidateList(List<PropertyUnit> propertyUnitCandidateList) {
        this.propertyUnitCandidateList = propertyUnitCandidateList;
    }

}
