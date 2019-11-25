/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Query subclass for retrieving Code Enforcement Action Requests
 * @author Loretta
 */
public class QueryCEAR 
        extends Query{

    /**
     * Holds this Query's identity Enum which includes the Query's
     * title and description.
     */
    private QueryCEAREnum queryName;
    private List<SearchParamsCEActionRequests> searchParamsList; 
    private List<CEActionRequest> results;

    public QueryCEAR(   QueryCEAREnum name,
                        Municipality m, 
                        List<SearchParamsCEActionRequests> params,
                        User u){
        super(m, u);
        queryName = name;
        searchParamsList = new ArrayList<>();
        searchParamsList.addAll(params);
        results = new ArrayList<>();
    }

    @Override
    public String getQueryTitle(){
        return queryName.getTitle();
    }
    

   public void addSearchParams(SearchParamsCEActionRequests sp){
       searchParamsList.add(sp);
       
   }
    
    public void addToResults(List<CEActionRequest> l){
        results.addAll(l);
    }
    
    

    /**
     * @return the results
     */
    public List<CEActionRequest> getResults() {
        return results;
    }

    public void setParamsList(List l) {
        searchParamsList = l;
    }

    @Override
    public List<CEActionRequest> getBOBResultList() {
        return results;
    }

    @Override
    public void setBOBResultList(List l) {
        results = l;
    }

    @Override
    public List<SearchParamsCEActionRequests> getParmsList() {
        return searchParamsList;
    }

    

    /**
     * @return the queryName
     */
    public QueryCEAREnum getQueryName() {
        return queryName;
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
        hash = 23 * hash + Objects.hashCode(this.queryName);
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
        final QueryCEAR other = (QueryCEAR) obj;
        if (this.queryName != other.queryName) {
            return false;
        }
        return true;
    }
    
    
}
