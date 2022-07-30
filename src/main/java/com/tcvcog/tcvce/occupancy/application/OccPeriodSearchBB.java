/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.session.SessionBean;
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodPropertyUnitHeavy;
import com.tcvcog.tcvce.entities.occupancy.OccPermitType;
import com.tcvcog.tcvce.entities.search.QueryOccPeriod;
import com.tcvcog.tcvce.entities.search.SearchParamsOccPeriod;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

/**
 * Tools for finding an occupancy period in the database
 * See the OccPeriodBB for period management methods
 * 
 * @author sylvia
 */
public class    OccPeriodSearchBB 
       extends  BackingBeanUtils {
    
//  *******************************
//  ************ SEARCH ***********
//  *******************************
    private List<OccPermitType> search_occPeriodTypeList;

    private List<QueryOccPeriod> occPeriodQueryList;
    private QueryOccPeriod occPeriodQuerySelected;

    private List<Property> search_propList;
    private List<Person> search_personList;

    private List<OccPeriodPropertyUnitHeavy> occPeriodList;
    private List<OccPeriodPropertyUnitHeavy> occPeriodListFiltered;
    private boolean appendResultsToList;

    private SearchParamsOccPeriod searchParamsSelected;

    /**
     * Creates a new instance of OccPeriodSearchBB
     */
    public OccPeriodSearchBB() {
    }
    
     @PostConstruct
    public void initBean() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        SessionBean sb = getSessionBean();
        search_occPeriodTypeList = sb.getSessMuni().getProfile().getOccPermitTypeList();
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
        if (getOccPeriodQuerySelected() != null
                &&
                getOccPeriodQuerySelected().getParamsList() != null
                &&
                !occPeriodQuerySelected.getParamsList().isEmpty()) {

            setSearchParamsSelected(getOccPeriodQuerySelected().getParamsList().get(0));
        } else {
            setSearchParamsSelected(null);
        }
    }
    
    
     /**
     * Listener method for requests from the user to clear the results list
     *
     */
    public void clearOccPeriodList() {
        if (occPeriodList != null) {
            occPeriodList.clear();
            getSessionBean().setSessOccPermitList(new ArrayList<>());
        }
    }

    /**
     * Asks the Coordinator for the OccPeriod viewing history
     * @deprecated : use search 
     */
    public void loadOccPeriodHistory() {
        System.out.println("OccPeriodSearchBB.loadOccPeriodHistory | DEPRECATED API USE");
    }

    /**
     * Entry way into the Query world via the SearchCoordinator who is responsible
     * for calling appropriate Coordinators and configuration methods and such
     */
    public void executeQuery() {
        System.out.println("occPeriodBB.executeQuery");
        SearchCoordinator sc = getSearchCoordinator();
        int listSize = 0;

        if (!appendResultsToList) {
            occPeriodList.clear();
        }
        occPeriodQuerySelected.setRequestingUser(getSessionBean().getSessUser());
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
        getSessionBean().setSessOccPermitList(new ArrayList<>());
        configureParameters();

    }

    //  ********************************************
    //  ************ GETTERS AND SETTERS ***********
    //  ********************************************
    
    /**
     * @return the search_occPeriodTypeList
     */
    public List<OccPermitType> getSearch_occPeriodTypeList() {
        return search_occPeriodTypeList;
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
     * @return the search_propList
     */
    public List<Property> getSearch_propList() {
        return search_propList;
    }

    /**
     * @return the search_personList
     */
    public List<Person> getSearch_personList() {
        return search_personList;
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
     * @return the appendResultsToList
     */
    public boolean isAppendResultsToList() {
        return appendResultsToList;
    }

    /**
     * @return the searchParamsSelected
     */
    public SearchParamsOccPeriod getSearchParamsSelected() {
        return searchParamsSelected;
    }

    /**
     * @param search_occPeriodTypeList the search_occPeriodTypeList to set
     */
    public void setSearch_occPeriodTypeList(List<OccPermitType> search_occPeriodTypeList) {
        this.search_occPeriodTypeList = search_occPeriodTypeList;
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
     * @param search_propList the search_propList to set
     */
    public void setSearch_propList(List<Property> search_propList) {
        this.search_propList = search_propList;
    }

    /**
     * @param search_personList the search_personList to set
     */
    public void setSearch_personList(List<Person> search_personList) {
        this.search_personList = search_personList;
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
     * @param appendResultsToList the appendResultsToList to set
     */
    public void setAppendResultsToList(boolean appendResultsToList) {
        this.appendResultsToList = appendResultsToList;
    }

    /**
     * @param searchParamsSelected the searchParamsSelected to set
     */
    public void setSearchParamsSelected(SearchParamsOccPeriod searchParamsSelected) {
        this.searchParamsSelected = searchParamsSelected;
    }
    
}
