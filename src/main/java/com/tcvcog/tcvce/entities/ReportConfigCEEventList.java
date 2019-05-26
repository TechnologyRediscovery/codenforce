/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import com.tcvcog.tcvce.entities.search.SearchParamsCEEvents;

/**
 *
 * @author sylvia
 */
public class ReportConfigCEEventList extends Report{
    
    private boolean includeAttachedPersons;
    private boolean includeEventTypeSummaryChart;
    private boolean includeActiveCaseListing;
    private boolean includeCaseActionRequestInfo;
    private boolean includeCompleteQueryParamsDump;
    
    private SearchParamsCEEvents queryParams;

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

    /**
     * @return the queryParams
     */
    public SearchParamsCEEvents getQueryParams() {
        return queryParams;
    }

    /**
     * @param queryParams the queryParams to set
     */
    public void setQueryParams(SearchParamsCEEvents queryParams) {
        this.queryParams = queryParams;
    }
    
    
    
    
}
