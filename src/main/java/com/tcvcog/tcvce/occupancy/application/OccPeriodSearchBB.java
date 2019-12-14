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
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.occupancy.OccInspection;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodType;
import com.tcvcog.tcvce.entities.search.QueryOccPeriod;
import com.tcvcog.tcvce.entities.search.SearchParamsOccPeriod;
import com.tcvcog.tcvce.occupancy.integration.OccInspectionIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

/**
 *
 * @author sylvia
 */
public class OccPeriodSearchBB extends BackingBeanUtils implements Serializable{

    private OccPeriod currentOccPeriod;
    private List<OccPeriodType> occPeriodTypeList;
    
    private List<OccPeriod> occPeriodList;
    private List<OccPeriod> occPeriodListFiltered;
    
    private List<QueryOccPeriod> occPeriodQueryList;
    private QueryOccPeriod occPeriodQuerySelected;
    private SearchParamsOccPeriod searchParams;
    
    /**
     * Creates a new instance of OccPeriodsBB
     */
    public OccPeriodSearchBB()  {
    }
    
    @PostConstruct
    public void initBean(){
        OccupancyIntegrator oi = getOccupancyIntegrator();
        occPeriodTypeList = getSessionBean().getSessionMuni().getProfile().getOccPeriodTypeList();
    }
    
     public void commenceOccInspection(){

    }

    /**
     * @return the searchParams
     */
    public SearchParamsOccPeriod getSearchParams() {
        return searchParams;
    }

    /**
     * @param searchParams the searchParams to set
     */
    public void setSearchParams(SearchParamsOccPeriod searchParams) {
        this.searchParams = searchParams;
    }

    /**
     * @return the currentOccPeriod
     */
    public OccPeriod getCurrentOccPeriod() {
        return currentOccPeriod;
    }

    /**
     * @return the occPeriodList
     */
    public List<OccPeriod> getOccPeriodList() {
        return occPeriodList;
    }

    /**
     * @return the occPeriodListFiltered
     */
    public List<OccPeriod> getOccPeriodListFiltered() {
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
     * @param currentOccPeriod the currentOccPeriod to set
     */
    public void setCurrentOccPeriod(OccPeriod currentOccPeriod) {
        this.currentOccPeriod = currentOccPeriod;
    }

    /**
     * @param occPeriodList the occPeriodList to set
     */
    public void setOccPeriodList(List<OccPeriod> occPeriodList) {
        this.occPeriodList = occPeriodList;
    }

    /**
     * @param occPeriodListFiltered the occPeriodListFiltered to set
     */
    public void setOccPeriodListFiltered(List<OccPeriod> occPeriodListFiltered) {
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
     * @return the occPeriodTypeList
     */
    public List<OccPeriodType> getOccPeriodTypeList() {
        return occPeriodTypeList;
    }

    /**
     * @param occPeriodTypeList the occPeriodTypeList to set
     */
    public void setOccPeriodTypeList(List<OccPeriodType> occPeriodTypeList) {
        this.occPeriodTypeList = occPeriodTypeList;
    }
    
}
