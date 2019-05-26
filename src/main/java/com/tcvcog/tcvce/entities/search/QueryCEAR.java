/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.Municipality;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sylvia
 */
public class QueryCEAR 
        extends Query{

    private List<SearchParamsCEActionRequests> searchParams; 
    private List<CEActionRequest> results;
    
    
    public QueryCEAR(String queryTitle, Municipality muni) {
        super(queryTitle, muni);
        searchParams = new ArrayList<>();
        results = new ArrayList<>();
    }
    
    public QueryCEAR(Municipality m){
        super(m);
        searchParams = new ArrayList<>();
        results = new ArrayList<>();
    }


   public void addSearchParams(SearchParamsCEActionRequests sp){
       searchParams.add(sp);
       
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

   

    @Override
    public void setParamsList(List l) {
        searchParams = l;
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
        return searchParams;
    }
    
    
}
