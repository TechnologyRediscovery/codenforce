/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.reports;

import com.tcvcog.tcvce.application.LegendItem;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.Citation;
import com.tcvcog.tcvce.entities.CodeViolationPropCECaseHeavy;
import com.tcvcog.tcvce.entities.EventCnFPropUnitCasePeriodHeavy;
import com.tcvcog.tcvce.entities.CodeViolationStatusEnum;
import com.tcvcog.tcvce.entities.search.Query;
import com.tcvcog.tcvce.entities.search.QueryBacked;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.primefaces.model.charts.hbar.HorizontalBarChartModel;
import org.primefaces.model.charts.pie.PieChartModel;

/**
 *
 * @author Ellen Bascomb of Apartment 31Y
 */
public class ReportConfigCECaseList 
        extends Report 
        implements Serializable, QueryBacked{
    
    private boolean includeListSummaryFigures;
    
    private boolean includeCaseNames;
    private boolean includeFullOwnerContactInfo;
    private boolean includeViolationList;
    private boolean includeEventSummaryByCase;
    private boolean includeCECaseStatusPie;
    private boolean includeStreetLevelSummary;
    private boolean includeCitationPieChart;
    private boolean includeClosurePieChart;
    
    private boolean includeExtendedPropertyDetails;
    
    private List<ReportCECaseListStreetCECaseContainer> streetContainerList;
    private Map<String, ReportCECaseListStreetCECaseContainer> streetSCC;
    
    
    private List<CECaseDataHeavy> caseListCustomQueryExport;
    
    private List<CECaseDataHeavy> caseListOpenAsOfDateStart;
    private List<CECaseDataHeavy> caseListOpenedInDateRange;
    private List<CECaseDataHeavy> caseListClosedInDateRange;
    private List<CECaseDataHeavy> caseListOpenAsOfDateEnd;
    
    protected List<Citation> citationList;
    
    private List<EventCnFPropUnitCasePeriodHeavy> eventList;
    
    // VIOLATIONS
    
    private List<CodeViolationPropCECaseHeavy> violationsLoggedNOVDateRange;
    private List<CodeViolationPropCECaseHeavy> violationsLoggedComplianceDateRange;
    private List<CodeViolationPropCECaseHeavy> violationsCitedDateRange;
    private List<CodeViolationPropCECaseHeavy> violationsLoggedDateRange;
    private List<CodeViolationPropCECaseHeavy> violationsAccumulatedCompliance;
    private List<CodeViolationPropCECaseHeavy> violationsWithinStipCompWindow;
    private List<CodeViolationPropCECaseHeavy> violationsOutsideCompWindowNOTCited;
    private List<CodeViolationPropCECaseHeavy> violationsNoComplianceButCited;
    
    
    
    private HorizontalBarChartModel barViolationsReport;
    
    private PieChartModel pieViol;
    protected List<LegendItem> pieViolLegend;
    private Map<CodeViolationStatusEnum, Integer> pieViolStatMap;
    private int pieViolCompCount;
    
    
    
    private PieChartModel pieEnforcement;
    protected List<LegendItem> pieEnforcementLegend;
    private PieChartModel pieCitation;
    protected List<LegendItem> pieCitationLegend;
    private PieChartModel pieClosure;
    protected List<LegendItem> pieClosureLegend;
    
    // Metrics
    
    private double averageAgeOfCasesClosed;
    private String averageAgeOfCasesOpenAsOfReportEndDate;
    
    public ReportConfigCECaseList(){
        violationsCitedDateRange = new ArrayList<>();
        violationsLoggedComplianceDateRange = new ArrayList<>();
        violationsCitedDateRange = new ArrayList<>();
    }
    
    public void assembleStreetList(){
        if(streetSCC != null && !streetSCC.isEmpty()){
            streetContainerList = new ArrayList<>();
            streetContainerList.addAll(streetSCC.values());
            streetContainerList.sort(null);
            Collections.reverse(streetContainerList);
            
        }
    }
    
    /**
     * Collapses the opened and open as of lists
     * @return combined list
     */
    public List<CECaseDataHeavy> assembleNonClosedCaseList(){
        List<CECaseDataHeavy> ncl = new ArrayList<>();
        if(caseListOpenedInDateRange != null && !caseListOpenedInDateRange.isEmpty()){
            ncl.addAll(caseListOpenedInDateRange);
        }
        if(caseListOpenAsOfDateEnd != null && !caseListOpenAsOfDateEnd.isEmpty()){
            ncl.addAll(caseListOpenAsOfDateEnd);
        }
        
        return ncl;
        
    }
    
    /**
     * Convenience method for pulling all non-closed cases and all closed cases
     * and feeding them to caller
     * 
     * Should produce the same result as combining opened, continuing, and closed
     * case lists. 
     * 
     * @return 
     */
    public List<CECaseDataHeavy> assembleFullCaseLiset(){
        List<CECaseDataHeavy> cl = new ArrayList<>();
        cl.addAll(assembleNonClosedCaseList());
        if(caseListClosedInDateRange != null && !caseListClosedInDateRange.isEmpty()){
            cl.addAll(caseListClosedInDateRange);
        }
        return cl;
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
    public String getAverageAgeOfCasesOpenAsOfReportEndDate() {
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
    public void setAverageAgeOfCasesOpenAsOfReportEndDate(String averageAgeOfCasesOpenAsOfReportEndDate) {
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

    /**
     * @return the pieEnforcement
     */
    public PieChartModel getPieEnforcement() {
        return pieEnforcement;
    }

    /**
     * @return the pieCitation
     */
    public PieChartModel getPieCitation() {
        return pieCitation;
    }

    /**
     * @return the pieClosure
     */
    public PieChartModel getPieClosure() {
        return pieClosure;
    }

    /**
     * @param pieEnforcement the pieEnforcement to set
     */
    public void setPieEnforcement(PieChartModel pieEnforcement) {
        this.pieEnforcement = pieEnforcement;
    }

    /**
     * @param pieCitation the pieCitation to set
     */
    public void setPieCitation(PieChartModel pieCitation) {
        this.pieCitation = pieCitation;
    }

    /**
     * @param pieClosure the pieClosure to set
     */
    public void setPieClosure(PieChartModel pieClosure) {
        this.pieClosure = pieClosure;
    }

    /**
     * @return the pieViolLegend
     */
    public List<LegendItem> getPieViolLegend() {
        return pieViolLegend;
    }

    /**
     * @return the pieEnforcementLegend
     */
    public List<LegendItem> getPieEnforcementLegend() {
        return pieEnforcementLegend;
    }

    /**
     * @return the pieCitationLegend
     */
    public List<LegendItem> getPieCitationLegend() {
        return pieCitationLegend;
    }

    /**
     * @return the pieClosureLegend
     */
    public List<LegendItem> getPieClosureLegend() {
        return pieClosureLegend;
    }

    /**
     * @param pieViolLegend the pieViolLegend to set
     */
    public void setPieViolLegend(List<LegendItem> pieViolLegend) {
        this.pieViolLegend = pieViolLegend;
    }

    /**
     * @param pieEnforcementLegend the pieEnforcementLegend to set
     */
    public void setPieEnforcementLegend(List<LegendItem> pieEnforcementLegend) {
        this.pieEnforcementLegend = pieEnforcementLegend;
    }

    /**
     * @param pieCitationLegend the pieCitationLegend to set
     */
    public void setPieCitationLegend(List<LegendItem> pieCitationLegend) {
        this.pieCitationLegend = pieCitationLegend;
    }

    /**
     * @param pieClosureLegend the pieClosureLegend to set
     */
    public void setPieClosureLegend(List<LegendItem> pieClosureLegend) {
        this.pieClosureLegend = pieClosureLegend;
    }

    /**
     * @return the citationList
     */
    public List<Citation> getCitationList() {
        return citationList;
    }

    /**
     * @param citationList the citationList to set
     */
    public void setCitationList(List<Citation> citationList) {
        this.citationList = citationList;
    }

    /**
     * @return the pieViolStatMap
     */
    public Map<CodeViolationStatusEnum, Integer> getPieViolStatMap() {
        return pieViolStatMap;
    }

    /**
     * @param pieViolStatMap the pieViolStatMap to set
     */
    public void setPieViolStatMap(Map<CodeViolationStatusEnum, Integer> pieViolStatMap) {
        this.pieViolStatMap = pieViolStatMap;
    }

    /**
     * @return the pieViolCompCount
     */
    public int getPieViolCompCount() {
        return pieViolCompCount;
    }

    /**
     * @param pieViolCompCount the pieViolCompCount to set
     */
    public void setPieViolCompCount(int pieViolCompCount) {
        this.pieViolCompCount = pieViolCompCount;
    }

    /**
     * @return the includeCECaseStatusPie
     */
    public boolean isIncludeCECaseStatusPie() {
        return includeCECaseStatusPie;
    }

    /**
     * @param includeCECaseStatusPie the includeCECaseStatusPie to set
     */
    public void setIncludeCECaseStatusPie(boolean includeCECaseStatusPie) {
        this.includeCECaseStatusPie = includeCECaseStatusPie;
    }

    /**
     * @return the includeStreetLevelSummary
     */
    public boolean isIncludeStreetLevelSummary() {
        return includeStreetLevelSummary;
    }

    /**
     * @param includeStreetLevelSummary the includeStreetLevelSummary to set
     */
    public void setIncludeStreetLevelSummary(boolean includeStreetLevelSummary) {
        this.includeStreetLevelSummary = includeStreetLevelSummary;
    }

   

    /**
     * @return the violationsLoggedDateRange
     */
    public List<CodeViolationPropCECaseHeavy> getViolationsLoggedDateRange() {
        return violationsLoggedDateRange;
    }

    /**
     * @param violationsLoggedDateRange the violationsLoggedDateRange to set
     */
    public void setViolationsLoggedDateRange(List<CodeViolationPropCECaseHeavy> violationsLoggedDateRange) {
        this.violationsLoggedDateRange = violationsLoggedDateRange;
    }

    /**
     * @return the violationsWithinStipCompWindow
     */
    public List<CodeViolationPropCECaseHeavy> getViolationsWithinStipCompWindow() {
        return violationsWithinStipCompWindow;
    }

    /**
     * @param violationsWithinStipCompWindow the violationsWithinStipCompWindow to set
     */
    public void setViolationsWithinStipCompWindow(List<CodeViolationPropCECaseHeavy> violationsWithinStipCompWindow) {
        this.violationsWithinStipCompWindow = violationsWithinStipCompWindow;
    }

    /**
     * @return the includeCitationPieChart
     */
    public boolean isIncludeCitationPieChart() {
        return includeCitationPieChart;
    }

    /**
     * @param includeCitationPieChart the includeCitationPieChart to set
     */
    public void setIncludeCitationPieChart(boolean includeCitationPieChart) {
        this.includeCitationPieChart = includeCitationPieChart;
    }

    /**
     * @return the violationsOutsideCompWindowNOTCited
     */
    public List<CodeViolationPropCECaseHeavy> getViolationsOutsideCompWindowNOTCited() {
        return violationsOutsideCompWindowNOTCited;
    }

    /**
     * @param violationsOutsideCompWindowNOTCited the violationsOutsideCompWindowNOTCited to set
     */
    public void setViolationsOutsideCompWindowNOTCited(List<CodeViolationPropCECaseHeavy> violationsOutsideCompWindowNOTCited) {
        this.violationsOutsideCompWindowNOTCited = violationsOutsideCompWindowNOTCited;
    }

    /**
     * @return the violationsNoComplianceButCited
     */
    public List<CodeViolationPropCECaseHeavy> getViolationsNoComplianceButCited() {
        return violationsNoComplianceButCited;
    }

    /**
     * @param violationsNoComplianceButCited the violationsNoComplianceButCited to set
     */
    public void setViolationsNoComplianceButCited(List<CodeViolationPropCECaseHeavy> violationsNoComplianceButCited) {
        this.violationsNoComplianceButCited = violationsNoComplianceButCited;
    }

    /**
     * @return the caseListOpenAsOfDateStart
     */
    public List<CECaseDataHeavy> getCaseListOpenAsOfDateStart() {
        return caseListOpenAsOfDateStart;
    }

    /**
     * @param caseListOpenAsOfDateStart the caseListOpenAsOfDateStart to set
     */
    public void setCaseListOpenAsOfDateStart(List<CECaseDataHeavy> caseListOpenAsOfDateStart) {
        this.caseListOpenAsOfDateStart = caseListOpenAsOfDateStart;
    }

    /**
     * @return the violationsAccumulatedCompliance
     */
    public List<CodeViolationPropCECaseHeavy> getViolationsAccumulatedCompliance() {
        return violationsAccumulatedCompliance;
    }

    /**
     * @param violationsAccumulatedCompliance the violationsAccumulatedCompliance to set
     */
    public void setViolationsAccumulatedCompliance(List<CodeViolationPropCECaseHeavy> violationsAccumulatedCompliance) {
        this.violationsAccumulatedCompliance = violationsAccumulatedCompliance;
    }

    /**
     * @return the includeClosurePieChart
     */
    public boolean isIncludeClosurePieChart() {
        return includeClosurePieChart;
    }

    /**
     * @param includeClosurePieChart the includeClosurePieChart to set
     */
    public void setIncludeClosurePieChart(boolean includeClosurePieChart) {
        this.includeClosurePieChart = includeClosurePieChart;
    }
    
    
}
