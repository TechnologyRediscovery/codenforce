/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.Credential;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventCnFPropUnitCasePeriodHeavy;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sylvia
 */
public class QueryEvent 
        extends Query {
    
    private QueryEventEnum queryName;
    
    private List<SearchParamsEvent> searchParamsList;
    private List<EventCnFPropUnitCasePeriodHeavy> results;
    
    public QueryEvent(QueryEventEnum qName, 
                            List<SearchParamsEvent> params,
                            Credential c) {
        super(c);
        searchParamsList = new ArrayList<>();
        if(params != null){
            searchParamsList.addAll(params);
        }
        queryName = qName;
        results = new ArrayList<>();
        
    }

    public long computeTotalEventDurationsInResults(){
        long totalDuration = 0;
        if(this.isQueryExecuted()){
            if(this.results != null && !this.results.isEmpty()){
                for(EventCnF ev: this.results){
                    totalDuration = totalDuration + ev.getDuration();
                }
            }
        }
        return totalDuration;
    }
    
    
    @Override
    public List<SearchParamsEvent> getParamsList() {
        return searchParamsList;
    }
    
    @Override
    public SearchParamsEvent getPrimaryParams() {
        if(searchParamsList != null && !searchParamsList.isEmpty()){
            return searchParamsList.get(0);
        }
        return null;
    }

    @Override
    public List<EventCnFPropUnitCasePeriodHeavy> getBOBResultList() {
        return results;
    }

    /**
     * Does not work yet
     * @param l 
     */
    @Override
    public void addBObListToResults(List l) {
        throw new UnsupportedOperationException("must still deal with Inheritance snafoo");
    }

  

    @Override
    public String getQueryTitle() {
        return queryName.getTitle();
    }

    @Override
    public void clearResultList() {
        if(results != null){
            results.clear();
        }
    }

    /**
     * @return the queryName
     */
    public QueryEventEnum getQueryName() {
        return queryName;
    }

    /**
     * @param queryName the queryName to set
     */
    public void setQueryName(QueryEventEnum queryName) {
        this.queryName = queryName;
    }

    public void addToResults(List<EventCnFPropUnitCasePeriodHeavy> events) {
        results.addAll(events);
    }

    @Override
    public void addParams(SearchParams params) {
         if(params instanceof SearchParamsEvent){
            searchParamsList.add((SearchParamsEvent) params);
        }
    }

    @Override
    public int getParamsListSize() {
        return searchParamsList.size();
    }
    
    
}
