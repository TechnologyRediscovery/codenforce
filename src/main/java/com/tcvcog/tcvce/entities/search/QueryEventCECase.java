/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.CECaseEvent;
import com.tcvcog.tcvce.entities.Credential;
import com.tcvcog.tcvce.entities.EventCECaseCasePropBundle;
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
public class QueryEventCECase extends Query {
    
    private QueryEventCECaseEnum queryName;
    
    
    // should be a list of search params eventually so we can build
    // queries from a set of search params
    private List<SearchParamsEventCECase> eventSearchParamsList;
    private List<EventCECaseCasePropBundle> results;
    
    public QueryEventCECase(QueryEventCECaseEnum qName, 
                            Municipality muni, 
                            Credential c,
                            List<SearchParamsEventCECase> params) {
        super(muni, c);
        eventSearchParamsList = new ArrayList<>();
        eventSearchParamsList.addAll(params);
        queryName = qName;
        results = new ArrayList<>();
        
    }

    
    public List getParamsList() {
        return eventSearchParamsList;
    }

    @Override
    public List<EventCECaseCasePropBundle> getBOBResultList() {
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
    public QueryEventCECaseEnum getQueryName() {
        return queryName;
    }

    /**
     * @param queryName the queryName to set
     */
    public void setQueryName(QueryEventCECaseEnum queryName) {
        this.queryName = queryName;
    }

    public void addToResults(List<EventCECaseCasePropBundle> eventsCECase) {
        results.addAll(eventsCECase);
    }

   
    
    
    
}
