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
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnitWithProp;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPermitPropUnitHeavy;
import com.tcvcog.tcvce.entities.occupancy.OccPermitType;
import com.tcvcog.tcvce.entities.reports.ReportConfigOccPermit;
import com.tcvcog.tcvce.entities.search.QueryOccPermit;
import com.tcvcog.tcvce.entities.search.SearchParamsOccPermit;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

/**
 * Tools for finding an occupancy period in the database
 * See the OccPermitBB for period management methods
 * 
 * @author sylvia
 */
public class    OccPermitSearchBB 
       extends  BackingBeanUtils {
    
//  *******************************
//  ************ SEARCH ***********
//  *******************************
    private List<OccPermitType> search_occPermitTypeList;

    private List<QueryOccPermit> occPermitQueryList;
    private QueryOccPermit occPermitQuerySelected;

    private List<Person> search_personList;

    private List<OccPermitPropUnitHeavy> occPermitList;
    private List<OccPermitPropUnitHeavy> occPermitListFiltered;
    private boolean appendResultsToList;

    private SearchParamsOccPermit searchParamsSelected;

    /**
     * Creates a new instance of OccPermitSearchBB
     */
    public OccPermitSearchBB() {
    }
    
     @PostConstruct
    public void initBean() {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        SessionBean sb = getSessionBean();
        search_occPermitTypeList = sb.getSessMuni().getProfile().getOccPermitTypeList();
        occPermitList = sb.getSessOccPermitList();

        if (occPermitList == null ) {
            occPermitList = new ArrayList();
        }
        appendResultsToList = false;
        occPermitQueryList = sb.getQueryOccPermitList();
        if (occPermitQueryList != null && !occPermitQueryList.isEmpty()) {
            occPermitQuerySelected = occPermitQueryList.get(0);
        }
        search_personList = sb.getSessPersonList();

        configureParameters();
    }
    
     /**
     * Sets up search fields
     */
    private void configureParameters() {
        if (getOccPermitQuerySelected() != null
                &&
                getOccPermitQuerySelected().getParamsList() != null
                &&
                !occPermitQuerySelected.getParamsList().isEmpty()) {

            setSearchParamsSelected(getOccPermitQuerySelected().getParamsList().get(0));
        } else {
            setSearchParamsSelected(null);
        }
    }
    
    
     /**
     * Listener method for requests from the user to clear the results list
     *
     */
    public void clearOccPermitList() {
        if (occPermitList != null) {
            occPermitList.clear();
        }
    }

   
    /**
     * Entry way into the Query world via the SearchCoordinator who is responsible
     * for calling appropriate Coordinators and configuration methods and such
     */
    public void executeQuery() {
        System.out.println("occPermitBB.executeQuery");
        SearchCoordinator sc = getSearchCoordinator();
        int listSize = 0;
        if(occPermitQuerySelected != null){
            if (!appendResultsToList) {
                occPermitList.clear();
            }
            occPermitQuerySelected.setRequestingUser(getSessionBean().getSessUser());
            try {
                occPermitList.addAll(sc.runQuery(occPermitQuerySelected).getBOBResultList());
                if (occPermitList != null) {
                    listSize = occPermitList.size();
                }
                getSessionBean().setSessOccPermitList(occPermitList);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Your query completed with " + listSize + " results", ""));
            } catch (SearchException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Could not query for permits due to search errors, sorry.", ""));
            }
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
        occPermitQueryList = sc.buildQueryOccPermitList(getSessionBean().getSessUser().getMyCredential());
        if (occPermitQueryList != null && !occPermitQueryList.isEmpty()) {
            occPermitQuerySelected = occPermitQueryList.get(0);
        }
        configureParameters();

    }
    
    /**
     * Listener for user requests to view a permit for printing
     * @param permit
     * @return 
     */
    public String onViewPermitLinkClick(OccPermitPropUnitHeavy permit){
        
        OccupancyCoordinator oc = getOccupancyCoordinator();
        PropertyCoordinator pc = getPropertyCoordinator();
        try{
            OccPeriod op = oc.getOccPeriod(permit.getPeriodID(), getSessionBean().getSessUser());
            ReportConfigOccPermit rcop = oc.getOccPermitReportConfigDefault(
                    permit, 
                    op, 
                    pc.getPropertyUnit(op.getPropertyUnitID()),
                    getSessionBean().getSessUser());
            getSessionBean().setReportConfigOccPermit(rcop);
        } catch (BObStatusException | IntegrationException ex){
            System.out.println(ex);
        }
        return "occPermit";
        
    }
    
    /**
     * Listener for user requests to view a permit's associated permit file
     * @param permit
     * @return 
     */
    public String onViewFileLinkClick(OccPermitPropUnitHeavy permit){
        
        try {
            return getSessionBean().navigateToPageCorrespondingToObject(permit);
        } catch (BObStatusException ex) {
            System.out.println(ex);
        }
        return "";
        
    }
    
    /**
     * Listener for user requests to view a permit's associated property
     * @param permit
     * @return 
     */
    public String onViewPropertyLinkClick(OccPermitPropUnitHeavy permit){
        
        try {
            
            PropertyCoordinator pc = getPropertyCoordinator();
            OccupancyCoordinator oc = getOccupancyCoordinator();
            OccPeriod period = oc.getOccPeriod(permit.getPeriodID(), getSessionBean().getSessUser());
            PropertyUnitWithProp puwp = pc.getPropertyUnitWithProp(period.getPropertyUnitID());
            return getSessionBean().navigateToPageCorrespondingToObject(puwp.getProperty());
        } catch (BObStatusException | IntegrationException ex){
            System.out.println(ex);
        }
        
        return "";
        
    }
    
    
    

    //  ********************************************
    //  ************ GETTERS AND SETTERS ***********
    //  ********************************************
    
    /**
     * @return the search_occPermitTypeList
     */
    public List<OccPermitType> getSearch_occPermitTypeList() {
        return search_occPermitTypeList;
    }

    /**
     * @return the occPermitQueryList
     */
    public List<QueryOccPermit> getOccPermitQueryList() {
        return occPermitQueryList;
    }

    /**
     * @return the occPermitQuerySelected
     */
    public QueryOccPermit getOccPermitQuerySelected() {
        return occPermitQuerySelected;
    }

    /**
     * @return the search_personList
     */
    public List<Person> getSearch_personList() {
        return search_personList;
    }

    /**
     * @return the occPermitList
     */
    public List<OccPermitPropUnitHeavy> getOccPermitList() {
        return occPermitList;
    }

    /**
     * @return the occPermitListFiltered
     */
    public List<OccPermitPropUnitHeavy> getOccPermitListFiltered() {
        return occPermitListFiltered;
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
    public SearchParamsOccPermit getSearchParamsSelected() {
        return searchParamsSelected;
    }

    /**
     * @param search_occPermitTypeList the search_occPermitTypeList to set
     */
    public void setSearch_occPermitTypeList(List<OccPermitType> search_occPermitTypeList) {
        this.search_occPermitTypeList = search_occPermitTypeList;
    }

    /**
     * @param occPermitQueryList the occPermitQueryList to set
     */
    public void setOccPermitQueryList(List<QueryOccPermit> occPermitQueryList) {
        this.occPermitQueryList = occPermitQueryList;
    }

    /**
     * @param occPermitQuerySelected the occPermitQuerySelected to set
     */
    public void setOccPermitQuerySelected(QueryOccPermit occPermitQuerySelected) {
        this.occPermitQuerySelected = occPermitQuerySelected;
    }

    /**
     * @param search_personList the search_personList to set
     */
    public void setSearch_personList(List<Person> search_personList) {
        this.search_personList = search_personList;
    }

    /**
     * @param occPermitList the occPermitList to set
     */
    public void setOccPermitList(List<OccPermitPropUnitHeavy> occPermitList) {
        this.occPermitList = occPermitList;
    }

    /**
     * @param occPermitListFiltered the occPermitListFiltered to set
     */
    public void setOccPermitListFiltered(List<OccPermitPropUnitHeavy> occPermitListFiltered) {
        this.occPermitListFiltered = occPermitListFiltered;
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
    public void setSearchParamsSelected(SearchParamsOccPermit searchParamsSelected) {
        this.searchParamsSelected = searchParamsSelected;
    }
    
}
