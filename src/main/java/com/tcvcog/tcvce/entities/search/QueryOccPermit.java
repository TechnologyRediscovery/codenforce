/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.Credential;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodPropertyUnitHeavy;
import com.tcvcog.tcvce.entities.occupancy.OccPermitPropUnitHeavy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Query subclass for retrieving Code Enforcement Action Requests
 * @author Loretta
 */
public class    QueryOccPermit 
        extends Query{

    /**
     * Holds this Query's identity Enum which includes the Query's
     * title and description.
     */
    private QueryOccPermitEnum queryName;
    private List<SearchParamsOccPermit> searchParamsList; 
    private List<OccPermitPropUnitHeavy> results;

    public QueryOccPermit(QueryOccPermitEnum name,
                        List<SearchParamsOccPermit> params,
                        Credential c){
        super(c);
        queryName = name;
        searchParamsList = new ArrayList<>();
        if(params != null){
            searchParamsList.addAll(params);
        }
        results = new ArrayList<>();
    }
    
    
    @Override
    public void addParams(SearchParams params) {
         if(params instanceof SearchParamsOccPermit){
            searchParamsList.add((SearchParamsOccPermit) params);
        }
        
    }
    
    @Override
    public SearchParamsOccPermit getPrimaryParams() {
        if(searchParamsList != null && !searchParamsList.isEmpty()){
            return searchParamsList.get(0);
        }
        return null;
    }

    @Override
    public int getParamsListSize() {
        return searchParamsList.size();
    }


    @Override
    public String getQueryTitle(){
        return queryName.getTitle();
    }
    

   public void addSearchParams(SearchParamsOccPermit sp){
        getSearchParamsList().add(sp);
       
   }
    
    public void addToResults(List<OccPermitPropUnitHeavy> l){
        results.addAll(l);
    }
    
    

    /**
     * @return the results
     */
    public List<OccPermitPropUnitHeavy> getResults() {
        return results;
    }

    public void setParamsList(List l) {
        setSearchParamsList((List<SearchParamsOccPermit>) l);
    }

    @Override
    public List<OccPermitPropUnitHeavy> getBOBResultList() {
        return results;
    }

    @Override
    public void addBObListToResults(List l) {
        results = l;
    }

    @Override
    public List<SearchParamsOccPermit> getParamsList() {
        return searchParamsList;
    }

    

    @Override
    public void clearResultList() {
        if(results != null){
            results.clear();
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.getQueryName());
        hash = 23 * hash + Objects.hashCode(this.getSearchParamsList());
        hash = 23 * hash + Objects.hashCode(this.results);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QueryOccPermit other = (QueryOccPermit) obj;
        if (this.getQueryName() != other.getQueryName()) {
            return false;
        }
        return true;
    }


    /**
     * @return the searchParamsList
     */
    public List<SearchParamsOccPermit> getSearchParamsList() {
        return searchParamsList;
    }


    /**
     * @param searchParamsList the searchParamsList to set
     */
    public void setSearchParamsList(List<SearchParamsOccPermit> searchParamsList) {
        this.searchParamsList = searchParamsList;
    }

    public QueryOccPermitEnum getQueryName(){
        return queryName;
    }
    
    /**
     * @param queryName the queryName to set
     */
    public void setQueryName(QueryOccPermitEnum queryName) {
        this.queryName = queryName;
    }

    
}
