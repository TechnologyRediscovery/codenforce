/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.reports;

import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.CodeViolationPropCECaseHeavy;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventCnFPropUnitCasePeriodHeavy;
import com.tcvcog.tcvce.entities.search.Query;
import com.tcvcog.tcvce.entities.search.QueryBacked;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.primefaces.model.charts.hbar.HorizontalBarChartModel;
import org.primefaces.model.charts.pie.PieChartModel;

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
    
    private List<ReportCECaseListStreetCECaseContainer> streetContainerList;
    private Map<String, ReportCECaseListStreetCECaseContainer> streetSCC;
    
    
    private List<CECaseDataHeavy> caseListCustomQueryExport;
    
    private List<CECaseDataHeavy> caseListOpenedInDateRange;
    private List<CECaseDataHeavy> caseListOpenAsOfDateEnd;
    private List<CECaseDataHeavy> caseListClosedInDateRange;
    
    private List<EventCnFPropUnitCasePeriodHeavy> eventList;
    
    // VIOLATIONS
    
    private List<CodeViolationPropCECaseHeavy> violationsLoggedNOVDateRange;
    private List<CodeViolationPropCECaseHeavy> violationsLoggedComplianceDateRange;
    private List<CodeViolationPropCECaseHeavy> violationsCitedDateRange;
    private HorizontalBarChartModel barViolationsReport;
    
    private PieChartModel pieViol;
    // Metrics
    private double averageAgeOfCasesClosed;
    private double averageAgeOfCasesOpenAsOfReportEndDate;
    
    
    
    public void assembleStreetList(){
        
        
        if(streetSCC != null && !streetSCC.isEmpty()){
            streetContainerList = new ArrayList<>();
            streetContainerList.addAll(streetSCC.values());
            streetContainerList.sort(null);
            
        }
    }

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
     * @return the caseListOpenedInDateRange
     */
    public List<CECaseDataHeavy> getCaseListOpenedInDateRange() {
        return caseListOpenedInDateRange;
    }

    /**
     * @return the caseListOpenAsOfDateEnd
     */
    public List<CECaseDataHeavy> getCaseListOpenAsOfDateEnd() {
        return caseListOpenAsOfDateEnd;
    }

    /**
     * @return the caseListClosedInDateRange
     */
    public List<CECaseDataHeavy> getCaseListClosedInDateRange() {
        return caseListClosedInDateRange;
    }

    /**
     * @param caseListOpenedInDateRange the caseListOpenedInDateRange to set
     */
    public void setCaseListOpenedInDateRange(List<CECaseDataHeavy> caseListOpenedInDateRange) {
        this.caseListOpenedInDateRange = caseListOpenedInDateRange;
    }

    /**
     * @param caseListOpenAsOfDateEnd the caseListOpenAsOfDateEnd to set
     */
    public void setCaseListOpenAsOfDateEnd(List<CECaseDataHeavy> caseListOpenAsOfDateEnd) {
        this.caseListOpenAsOfDateEnd = caseListOpenAsOfDateEnd;
    }

    /**
     * @param caseListClosedInDateRange the caseListClosedInDateRange to set
     */
    public void setCaseListClosedInDateRange(List<CECaseDataHeavy> caseListClosedInDateRange) {
        this.caseListClosedInDateRange = caseListClosedInDateRange;
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

    /**
     * @return the averageAgeOfCasesClosed
     */
    public double getAverageAgeOfCasesClosed() {
        return averageAgeOfCasesClosed;
    }

    /**
     * @return the averageAgeOfCasesOpenAsOfReportEndDate
     */
    public double getAverageAgeOfCasesOpenAsOfReportEndDate() {
        return averageAgeOfCasesOpenAsOfReportEndDate;
    }

    /**
     * @param averageAgeOfCasesClosed the averageAgeOfCasesClosed to set
     */
    public void setAverageAgeOfCasesClosed(double averageAgeOfCasesClosed) {
        this.averageAgeOfCasesClosed = averageAgeOfCasesClosed;
    }

    /**
     * @param averageAgeOfCasesOpenAsOfReportEndDate the averageAgeOfCasesOpenAsOfReportEndDate to set
     */
    public void setAverageAgeOfCasesOpenAsOfReportEndDate(double averageAgeOfCasesOpenAsOfReportEndDate) {
        this.averageAgeOfCasesOpenAsOfReportEndDate = averageAgeOfCasesOpenAsOfReportEndDate;
    }

    /**
     * @return the caseListCustomQueryExport
     */
    public List<CECaseDataHeavy> getCaseListCustomQueryExport() {
        return caseListCustomQueryExport;
    }

    /**
     * @param caseListCustomQueryExport the caseListCustomQueryExport to set
     */
    public void setCaseListCustomQueryExport(List<CECaseDataHeavy> caseListCustomQueryExport) {
        this.caseListCustomQueryExport = caseListCustomQueryExport;
    }

    /**
     * @return the violationsCitedDateRange
     */
    public List<CodeViolationPropCECaseHeavy> getViolationsCitedDateRange() {
        return violationsCitedDateRange;
    }

    /**
     * @param violationsCitedDateRange the violationsCitedDateRange to set
     */
    public void setViolationsCitedDateRange(List<CodeViolationPropCECaseHeavy> violationsCitedDateRange) {
        this.violationsCitedDateRange = violationsCitedDateRange;
    }

    /**
     * @return the violationsLoggedNOVDateRange
     */
    public List<CodeViolationPropCECaseHeavy> getViolationsLoggedNOVDateRange() {
        return violationsLoggedNOVDateRange;
    }

    /**
     * @return the violationsLoggedComplianceDateRange
     */
    public List<CodeViolationPropCECaseHeavy> getViolationsLoggedComplianceDateRange() {
        return violationsLoggedComplianceDateRange;
    }

    /**
     * @param violationsLoggedNOVDateRange the violationsLoggedNOVDateRange to set
     */
    public void setViolationsLoggedNOVDateRange(List<CodeViolationPropCECaseHeavy> violationsLoggedNOVDateRange) {
        this.violationsLoggedNOVDateRange = violationsLoggedNOVDateRange;
    }

    /**
     * @param violationsLoggedComplianceDateRange the violationsLoggedComplianceDateRange to set
     */
    public void setViolationsLoggedComplianceDateRange(List<CodeViolationPropCECaseHeavy> violationsLoggedComplianceDateRange) {
        this.violationsLoggedComplianceDateRange = violationsLoggedComplianceDateRange;
    }

    /**
     * @return the barViolationsReport
     */
    public HorizontalBarChartModel getBarViolationsReport() {
        return barViolationsReport;
    }

    /**
     * @param barViolationsReport the barViolationsReport to set
     */
    public void setBarViolationsReport(HorizontalBarChartModel barViolationsReport) {
        this.barViolationsReport = barViolationsReport;
    }

    /**
     * @return the pieViol
     */
    public PieChartModel getPieViol() {
        return pieViol;
    }

    /**
     * @param pieViol the pieViol to set
     */
    public void setPieViol(PieChartModel pieViol) {
        this.pieViol = pieViol;
    }

    /**
     * @return the streetContainerList
     */
    public List<ReportCECaseListStreetCECaseContainer> getStreetContainerList() {
        return streetContainerList;
    }

    /**
     * @param streetContainerList the streetContainerList to set
     */
    public void setStreetContainerList(List<ReportCECaseListStreetCECaseContainer> streetContainerList) {
        this.streetContainerList = streetContainerList;
    }

    /**
     * @return the streetSCC
     */
    public Map<String, ReportCECaseListStreetCECaseContainer> getStreetSCC() {
        return streetSCC;
    }

    /**
     * @param streetSCC the streetSCC to set
     */
    public void setStreetSCC(Map<String, ReportCECaseListStreetCECaseContainer> streetSCC) {
        this.streetSCC = streetSCC;
    }
    
    
}
