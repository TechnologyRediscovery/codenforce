/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.BOb;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthorized;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author sylvia
 */
public class QueryCECase 
        extends Query{
    
    private QueryCECaseEnum queryName;
    private List<SearchParamsCECase> searchParamsList; 
    private List<CECase> results;
    
    public QueryCECase( QueryCECaseEnum qName, 
                        Municipality muni, 
                        List<SearchParamsCECase> params,
                        UserAuthorized u) {
        super(muni, u);
        queryName = qName;
        searchParamsList = new ArrayList<>();
        searchParamsList.addAll(params);
        results = new ArrayList<>();
    }
    
    public void addToResults(List<CECase> list){
        results.addAll(list);
    }

    @Override
    public List getBOBResultList() {
        return results;
    }

    @Override
    public void setBOBResultList(List l) {
        results = l;
    }

    @Override
    public List getParmsList() {
        return searchParamsList;
    }
    

    /**
     * @return the searchParamsList
     */
    public List<SearchParamsCECase> getSearchParamsList() {
        return searchParamsList;
    }

    /**
     * @param searchParamsList the searchParamsList to set
     */
    public void setSearchParamsList(List<SearchParamsCECase> searchParamsList) {
        this.searchParamsList = searchParamsList;
    }

    @Override
    public void clearResultList() {
        if(results != null){
            results.clear();
        }
    }

    /**
     *
     * @return
     */
    public List<SearchParamsCECase> getParamsList() {
        return searchParamsList;
    }

    /**
     * @return the results
     */
    public List<CECase> getResults() {
        return results;
    }

    /**
     * @param results the results to set
     */
    public void setResults(List<CECase> results) {
        this.results = results;
    }

    public void setParamsList(List l) {
        searchParamsList = l;
    }

    @Override
    public String getQueryTitle() {
        return queryName.getTitle();
        
    }
    

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.searchParamsList);
        hash = 97 * hash + Objects.hashCode(this.results);
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
        final QueryCECase other = (QueryCECase) obj;
        if(this.queryName != other.queryName){
            return false;
        }
        
        return true;
    }

    /**
     * @return the queryName
     */
    public QueryCECaseEnum getQueryName() {
        return queryName;
    }
    
    
    
    
}
