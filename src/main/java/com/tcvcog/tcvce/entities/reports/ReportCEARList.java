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
    
    /**
     * This report config needs to work for printing single requests
     * and multiple ones. Turn this switch on when reporting only the active CEAR
     */
    private boolean printFullCEARQueue;
    private boolean includePhotos;
    private boolean insertPageBreakBeforeEachPhotoSet;
    private boolean insertPageBreakBeforeEachIndivPhoto;
    private QueryCEAR queryCEAR;

    @Override
    public QueryCEAR getBOBQuery() {
        return queryCEAR;

    }

    @Override
    public void setBOBQuery(Query q) {
        queryCEAR = (QueryCEAR) q;
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

    /**
     * @return the insertPageBreakBeforeEachPhotoSet
     */
    public boolean isInsertPageBreakBeforeEachPhotoSet() {
        return insertPageBreakBeforeEachPhotoSet;
    }

    /**
     * @return the insertPageBreakBeforeEachIndivPhoto
     */
    public boolean isInsertPageBreakBeforeEachIndivPhoto() {
        return insertPageBreakBeforeEachIndivPhoto;
    }

    /**
     * @param insertPageBreakBeforeEachPhotoSet the insertPageBreakBeforeEachPhotoSet to set
     */
    public void setInsertPageBreakBeforeEachPhotoSet(boolean insertPageBreakBeforeEachPhotoSet) {
        this.insertPageBreakBeforeEachPhotoSet = insertPageBreakBeforeEachPhotoSet;
    }

    /**
     * @param insertPageBreakBeforeEachIndivPhoto the insertPageBreakBeforeEachIndivPhoto to set
     */
    public void setInsertPageBreakBeforeEachIndivPhoto(boolean insertPageBreakBeforeEachIndivPhoto) {
        this.insertPageBreakBeforeEachIndivPhoto = insertPageBreakBeforeEachIndivPhoto;
    }

   
    
}
