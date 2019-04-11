/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

/**
 *
 * @author sylvia
 */
public class ReportConfigCEEventList extends ReportConfig{
    
    private boolean includeCaseActionRequestInfo;
    private boolean includeAttachedPersons;

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
    
    
    
    
}
