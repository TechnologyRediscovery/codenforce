/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.reports;

import com.tcvcog.tcvce.entities.search.Query;
import com.tcvcog.tcvce.entities.search.QueryBacked;
import com.tcvcog.tcvce.entities.search.QueryEvent;
import com.tcvcog.tcvce.entities.search.SearchParamsEvent;

/**
 *
 * @author sylvia
 */
public class ReportConfigCEEventList 
        extends Report
        implements QueryBacked{
    
    private QueryEvent queryParams;
    
    private boolean includeAttachedPersons;
    private boolean includeEventTypeSummaryChart;
    private boolean includeActiveCaseListing;
    private boolean includeCaseActionRequestInfo;
    private boolean includeCompleteQueryParamsDump;
    
    
    

    /**
     * @return the includeCaseActionRequestInfo
     */
    public boolean isIncludeCaseActionRequestInfo() {
        return includeCaseActionRequestInfo;
    }

    /**
     * @return the includeAttachedPersons
     */
    public boolean isIncludeAttachedPersons() {
        return includeAttachedPersons;
    }

    /**
     * @param includeCaseActionRequestInfo the includeCaseActionRequestInfo to set
     */
    public void setIncludeCaseActionRequestInfo(boolean includeCaseActionRequestInfo) {
        this.includeCaseActionRequestInfo = includeCaseActionRequestInfo;
    }

    /**
     * @param includeAttachedPersons the includeAttachedPersons to set
     */
    public void setIncludeAttachedPersons(boolean includeAttachedPersons) {
        this.includeAttachedPersons = includeAttachedPersons;
    }

    /**
     * @return the includeEventTypeSummaryChart
     */
    public boolean isIncludeEventTypeSummaryChart() {
        return includeEventTypeSummaryChart;
    }

    /**
     * @param includeEventTypeSummaryChart the includeEventTypeSummaryChart to set
     */
    public void setIncludeEventTypeSummaryChart(boolean includeEventTypeSummaryChart) {
        this.includeEventTypeSummaryChart = includeEventTypeSummaryChart;
    }

    /**
     * @return the includeActiveCaseListing
     */
    public boolean isIncludeActiveCaseListing() {
        return includeActiveCaseListing;
    }

    /**
     * @param includeActiveCaseListing the includeActiveCaseListing to set
     */
    public void setIncludeActiveCaseListing(boolean includeActiveCaseListing) {
        this.includeActiveCaseListing = includeActiveCaseListing;
    }

    /**
     * @return the includeCompleteQueryParamsDump
     */
    public boolean isIncludeCompleteQueryParamsDump() {
        return includeCompleteQueryParamsDump;
    }

    /**
     * @param includeCompleteQueryParamsDump the includeCompleteQueryParamsDump to set
     */
    public void setIncludeCompleteQueryParamsDump(boolean includeCompleteQueryParamsDump) {
        this.includeCompleteQueryParamsDump = includeCompleteQueryParamsDump;
    }


    @Override
    public Query getBOBQuery() {
        return queryParams;
    }

    @Override
    public void setBOBQuery(Query q) {
        queryParams = (QueryEvent) q;
    }
    
    public SearchParamsEvent getQueryParams(){
        return (SearchParamsEvent) queryParams.getParamsList().get(0);
    }
    
    
    
}
