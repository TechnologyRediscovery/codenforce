/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.BOb;
import com.tcvcog.tcvce.entities.Credential;
import com.tcvcog.tcvce.entities.Person;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Query subclass for retrieving Code Enforcement Action Requests
 * @author Loretta
 */
public class    QueryPerson 
        extends Query{

    /**
     * Holds this Query's identity Enum which includes the Query's
     * title and description.
     */
    private QueryPersonEnum query;
    private List<SearchParamsPerson> searchParamsList; 
    private List<Person> results;

    /**
     * Unified Query subclass constructor
     * @param name
     * @param params if not null, the Query subclass is marked as CUSTOM and the
     * passed in params are added to the params assembled according to the enum
     * and no 
     * @param c 
     */
    public QueryPerson( QueryPersonEnum name,
                        List<SearchParamsPerson> params,
                        Credential c){
        super(c);
        query = name;
        searchParamsList = new ArrayList<>();
        if(params != null){
            searchParamsList.addAll(params);
        }
        results = new ArrayList<>();
    }
    
    
    @Override
    public int getParamsListSize() {
        return searchParamsList.size();
    }
    
    @Override
    public SearchParamsPerson getPrimaryParams() {
        if(searchParamsList != null && !searchParamsList.isEmpty()){
            return searchParamsList.get(0);
        }
        return null;
    }

    @Override
    public String getQueryTitle(){
        return query.getTitle();
    }
    
    @Override
    public void addParams(SearchParams params) {
        if(params instanceof SearchParamsPerson){
            searchParamsList.add((SearchParamsPerson) params);
        }
    }

   public void addSearchParams(SearchParamsPerson sp){
       searchParamsList.add(sp);
   }
   
    public void addToResults(List<Person> l){
        results.addAll(l);
    }
    
    /**
     *
     * @param l
     */
    @Override
    public void addBObListToResults(List l) {
        
        
    }

    /**
     * @return the results
     */
    public List<Person> getResults() {
        return results;
    }

    @Override
    public List<Person> getBOBResultList() {
        return results;
    }

  
    @Override
    public List<SearchParamsPerson> getParamsList() {
        return searchParamsList;
    }

    /**
     * @return the queryName
     */
    public QueryPersonEnum getQueryName() {
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
        final QueryPerson other = (QueryPerson) obj;
        if (this.query != other.query) {
            return false;
        }
        return true;
    }

}
