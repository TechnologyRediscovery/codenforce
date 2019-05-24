/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.User;
import java.util.Objects;

/**
 *
 * @author sylvia
 */
public class QueryEventCECase extends Query {
    
    private SearchParamsCEEvents eventSearchParams;
    
    public QueryEventCECase(String queryTitle, Municipality muni) {
        super(queryTitle, muni);
    }

    /**
     * @return the eventSearchParams
     */
    public SearchParamsCEEvents getEventSearchParams() {
        return eventSearchParams;
    }

    /**
     * @param eventSearchParams the eventSearchParams to set
     */
    public void setEventSearchParams(SearchParamsCEEvents eventSearchParams) {
        this.eventSearchParams = eventSearchParams;
    }

   
    
    
    
}
