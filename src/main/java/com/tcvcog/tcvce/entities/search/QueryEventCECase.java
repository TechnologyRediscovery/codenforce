/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.EventCECase;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.User;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author sylvia
 */
public class QueryEventCECase extends Query {
    
    private QueryEventCECase queryEventCECase;
    
    // should be a list of search params eventually so we can build
    // queries from a set of search params
    private List<SearchParamsEventCECase> eventSearchParamsList;
    private List<EventCECase> results;
    
    public QueryEventCECase(Municipality muni, User u) {
        super(muni, u);
    }

    
    public List getParamsList() {
        return eventSearchParamsList;
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
        return eventSearchParamsList;
    }

    @Override
    public String getQueryTitle() {
        return queryEventCECase.getQueryTitle();
    }

    @Override
    public void clearResultList() {
        if(results != null){
            results.clear();
        }
    }

   
    
    
    
}
