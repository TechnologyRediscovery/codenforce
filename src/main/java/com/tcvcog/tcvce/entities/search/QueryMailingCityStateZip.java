/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CECasePropertyUnitHeavy;
import com.tcvcog.tcvce.entities.Credential;
import com.tcvcog.tcvce.entities.MailingCityStateZip;
import com.tcvcog.tcvce.entities.RoleType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Container for Query related objects for extracting
 * records from the mailingcitystatezip table
 * 
 * @author sylvia
 */
public class QueryMailingCityStateZip 
        extends Query{
    
    private final QueryMailingCityStateZipEnum queryName;
    private List<SearchParamsMailingCityStateZip> searchParamsList; 
    private List<MailingCityStateZip> results;
    
    public QueryMailingCityStateZip( QueryMailingCityStateZipEnum qName, 
                        List<SearchParamsMailingCityStateZip> params,
                        Credential c) {
        super(c);
        queryName = qName;
        searchParamsList = new ArrayList<>();
        if(params != null){
            searchParamsList.addAll(params);
        }
        results = new ArrayList<>();
    }
    
    public void addToResults(List<MailingCityStateZip> list){
        results.addAll(list);
    }
    
    
    @Override
    public void addParams(SearchParams params) {
        if(params != null && params instanceof SearchParamsMailingCityStateZip){
            searchParamsList.add((SearchParamsMailingCityStateZip) params);
        }
    }
    
    /**
     * Spits back the first param in the list
     * @return 
     */
    @Override
    public SearchParamsMailingCityStateZip getPrimaryParams() {
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
    public List<SearchParamsMailingCityStateZip> getParamsList() {
        return searchParamsList;
    }
    

    /**
     * @return the searchParamsList
     */
    public List<SearchParamsMailingCityStateZip> getSearchParamsList() {
        return searchParamsList;
    }

    /**
     * @param searchParamsList the searchParamsList to set
     */
    public void setSearchParamsList(List<SearchParamsMailingCityStateZip> searchParamsList) {
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
    public List<MailingCityStateZip> getResults() {
        return results;
    }

    /**
     * @param results the results to set
     */
    public void setResults(List<MailingCityStateZip> results) {
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
        final QueryMailingCityStateZip other = (QueryMailingCityStateZip) obj;
        if(this.queryName != other.queryName){
            return false;
        }
        
        return true;
    }

    /**
     * @return the queryName
     */
    public QueryMailingCityStateZipEnum getQueryName() {
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
