/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.reports;

import com.tcvcog.tcvce.entities.search.Query;
import com.tcvcog.tcvce.entities.search.QueryBacked;
import com.tcvcog.tcvce.entities.search.QueryCEAR;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author sylvia
 */
public class ReportCEARList 
        extends Report 
        implements Serializable, QueryBacked{
    
    private boolean printFullCEARQueue;
    private boolean includePhotos;
    
    
    /**
     * a Query has search params and a List of BOB results
     */
    private QueryCEAR cearQuery;

    @Override
    public Query getBOBQuery() {
        return cearQuery;

    }

    @Override
    public void setBOBQuery(Query q) {
        cearQuery = (QueryCEAR) q;
    }
    

    /**
     * @return the printFullCEARQueue
     */
    public boolean isPrintFullCEARQueue() {
        return printFullCEARQueue;
    }

    /**
     * @return the includePhotos
     */
    public boolean isIncludePhotos() {
        return includePhotos;
    }

    /**
     * @param printFullCEARQueue the printFullCEARQueue to set
     */
    public void setPrintFullCEARQueue(boolean printFullCEARQueue) {
        this.printFullCEARQueue = printFullCEARQueue;
    }

    /**
     * @param includePhotos the includePhotos to set
     */
    public void setIncludePhotos(boolean includePhotos) {
        this.includePhotos = includePhotos;
    }

   
    
}
