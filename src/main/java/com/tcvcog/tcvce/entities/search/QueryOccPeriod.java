/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Query subclass for retrieving Code Enforcement Action Requests
 * @author Loretta
 */
public class QueryOccPeriod 
        extends Query{

    /**
     * Holds this Query's identity Enum which includes the Query's
     * title and description.
     */
    private QueryPropertyEnum query;
    private List<SearchParamsOccPeriod> searchParamsList; 
    private List<OccPeriod> results;

    public QueryOccPeriod(QueryPropertyEnum name,
                        Municipality m, 
                        List<SearchParamsOccPeriod> params,
                        User u){
        super(m, u);
        query = name;
        searchParamsList = new ArrayList<>();
        searchParamsList.addAll(params);
        results = new ArrayList<>();
    }

    @Override
    public String getQueryTitle(){
        return query.getTitle();
    }
    

   public void addSearchParams(SearchParamsOccPeriod sp){
       searchParamsList.add(sp);
       
   }
    
    public void addToResults(List<OccPeriod> l){
        results.addAll(l);
    }
    
    

    /**
     * @return the results
     */
    public List<OccPeriod> getResults() {
        return results;
    }

    public void setParamsList(List l) {
        searchParamsList = l;
    }

    @Override
    public List<OccPeriod> getBOBResultList() {
        return results;
    }

    @Override
    public void setBOBResultList(List l) {
        results = l;
    }

    @Override
    public List<SearchParamsOccPeriod> getParmsList() {
        return searchParamsList;
    }

    

    /**
     * @return the queryName
     */
    public QueryPropertyEnum getQueryName() {
        return query;
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
        hash = 23 * hash + Objects.hashCode(this.query);
        hash = 23 * hash + Objects.hashCode(this.searchParamsList);
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
        final QueryOccPeriod other = (QueryOccPeriod) obj;
        if (this.query != other.query) {
            return false;
        }
        return true;
    }
    
    
}
