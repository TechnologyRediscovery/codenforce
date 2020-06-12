/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CECasePropertyUnitHeavy;
import com.tcvcog.tcvce.entities.Credential;
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
    private List<CECasePropertyUnitHeavy> results;
    
    public QueryCECase( QueryCECaseEnum qName, 
                        List<SearchParamsCECase> params,
                        Credential c) {
        super(c);
        queryName = qName;
        searchParamsList = new ArrayList<>();
        if(params != null){
            searchParamsList.addAll(params);
        }
        results = new ArrayList<>();
    }
    
    public void addToResults(List<CECasePropertyUnitHeavy> list){
        results.addAll(list);
    }
    
    
    @Override
    public void addParams(SearchParams params) {
        if(params != null && params instanceof SearchParamsCECase){
            searchParamsList.add((SearchParamsCECase) params);
        }
    }
    
      @Override
    public SearchParamsCECase getPrimaryParams() {
        if(searchParamsList != null && !searchParamsList.isEmpty()){
            return searchParamsList.get(0);
        }
        return null;
    }
    

    @Override
    public List getBOBResultList() {
        return results;
    }

    @Override
    public void addBObListToResults(List l) {
        results = l;
    }

    @Override
    public List<SearchParamsCECase> getParamsList() {
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
     * @return the results
     */
    public List<CECasePropertyUnitHeavy> getResults() {
        return results;
    }

    /**
     * @param results the results to set
     */
    public void setResults(List<CECasePropertyUnitHeavy> results) {
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

    @Override
    public int getParamsListSize() {
        int size = 0;
        if(searchParamsList != null){
            return searchParamsList.size();
        }
        return size;
    }

    
    
    
}
