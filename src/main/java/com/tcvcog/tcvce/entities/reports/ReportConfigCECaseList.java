/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.reports;

import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventCnFPropUnitCasePeriodHeavy;
import com.tcvcog.tcvce.entities.search.Query;
import com.tcvcog.tcvce.entities.search.QueryBacked;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author sylvia
 */
public class ReportConfigCECaseList 
        extends Report 
        implements Serializable, QueryBacked{
    
    private boolean includeListSummaryFigures;
    
    private boolean includeCaseNames;
    private boolean includeFullOwnerContactInfo;
    private boolean includeViolationList;
    private boolean includeEventSummaryByCase;
    
    private boolean includeExtendedPropertyDetails;
    
    private List<CECaseDataHeavy> caseListOpened;
    private List<CECaseDataHeavy> caseListCurrent;
    private List<CECaseDataHeavy> caseListClosed;
    
    private List<EventCnFPropUnitCasePeriodHeavy> eventList;

    /**
     * @return the includeListSummaryFigures
     */
    public boolean isIncludeListSummaryFigures() {
        return includeListSummaryFigures;
    }

    /**
     * @return the includeCaseNames
     */
    public boolean isIncludeCaseNames() {
        return includeCaseNames;
    }

    /**
     * @return the includeFullOwnerContactInfo
     */
    public boolean isIncludeFullOwnerContactInfo() {
        return includeFullOwnerContactInfo;
    }

    /**
     * @return the includeViolationList
     */
    public boolean isIncludeViolationList() {
        return includeViolationList;
    }

    /**
     * @return the includeEventSummaryByCase
     */
    public boolean isIncludeEventSummaryByCase() {
        return includeEventSummaryByCase;
    }

    /**
     * @param includeListSummaryFigures the includeListSummaryFigures to set
     */
    public void setIncludeListSummaryFigures(boolean includeListSummaryFigures) {
        this.includeListSummaryFigures = includeListSummaryFigures;
    }

    /**
     * @param includeCaseNames the includeCaseNames to set
     */
    public void setIncludeCaseNames(boolean includeCaseNames) {
        this.includeCaseNames = includeCaseNames;
    }

    /**
     * @param includeFullOwnerContactInfo the includeFullOwnerContactInfo to set
     */
    public void setIncludeFullOwnerContactInfo(boolean includeFullOwnerContactInfo) {
        this.includeFullOwnerContactInfo = includeFullOwnerContactInfo;
    }

    /**
     * @param includeViolationList the includeViolationList to set
     */
    public void setIncludeViolationList(boolean includeViolationList) {
        this.includeViolationList = includeViolationList;
    }

    /**
     * @param includeEventSummaryByCase the includeEventSummaryByCase to set
     */
    public void setIncludeEventSummaryByCase(boolean includeEventSummaryByCase) {
        this.includeEventSummaryByCase = includeEventSummaryByCase;
    }

    /**
     * @return the includeExtendedPropertyDetails
     */
    public boolean isIncludeExtendedPropertyDetails() {
        return includeExtendedPropertyDetails;
    }

    /**
     * @param includeExtendedPropertyDetails the includeExtendedPropertyDetails to set
     */
    public void setIncludeExtendedPropertyDetails(boolean includeExtendedPropertyDetails) {
        this.includeExtendedPropertyDetails = includeExtendedPropertyDetails;
    }

    @Override
    public Query getBOBQuery() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setBOBQuery(Query q) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return the caseListOpened
     */
    public List<CECaseDataHeavy> getCaseListOpened() {
        return caseListOpened;
    }

    /**
     * @return the caseListCurrent
     */
    public List<CECaseDataHeavy> getCaseListCurrent() {
        return caseListCurrent;
    }

    /**
     * @return the caseListClosed
     */
    public List<CECaseDataHeavy> getCaseListClosed() {
        return caseListClosed;
    }

    /**
     * @param caseListOpened the caseListOpened to set
     */
    public void setCaseListOpened(List<CECaseDataHeavy> caseListOpened) {
        this.caseListOpened = caseListOpened;
    }

    /**
     * @param caseListCurrent the caseListCurrent to set
     */
    public void setCaseListCurrent(List<CECaseDataHeavy> caseListCurrent) {
        this.caseListCurrent = caseListCurrent;
    }

    /**
     * @param caseListClosed the caseListClosed to set
     */
    public void setCaseListClosed(List<CECaseDataHeavy> caseListClosed) {
        this.caseListClosed = caseListClosed;
    }

    /**
     * @return the eventList
     */
    public List<EventCnFPropUnitCasePeriodHeavy> getEventList() {
        return eventList;
    }

    /**
     * @param eventList the eventList to set
     */
    public void setEventList(List<EventCnFPropUnitCasePeriodHeavy> eventList) {
        this.eventList = eventList;
    }
    
    
}
