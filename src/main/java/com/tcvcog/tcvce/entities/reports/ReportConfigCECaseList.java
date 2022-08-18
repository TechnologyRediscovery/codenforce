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
import com.tcvcog.tcvce.entities.RoleType;
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
    
    // ******************************
    // Section: Report Summary Switches
    // *******************************
    private boolean includeSectionReportSummary;
    
    private boolean includeSectionReportSummary_openAsOfSOR;
    private boolean includeSectionReportSummary_openAsOfEOR;
    private boolean includeSectionReportSummary_casesOpened;
    private boolean includeSectionReportSummary_casesClosed;
    private boolean includeSectionReportSummary_newViolations;
    private boolean includeSectionReportSummary_compViol;
    private boolean includeSectionReportSummary_eventsLogged;
    private boolean includeSectionReportSummary_eventsTotalTime;
    
    private boolean includeSectionReportSummary_closurePieChart;
    private boolean includeSectionReportSummary_ceCaseStatusPie; // do not use
    
    // *****************************************
    // Section: Code Violations Switches
    // *****************************************
    private boolean includeSectionCodeViolationStatus;
    private boolean includeSectionCodeViolationStatus_compliance;
    private boolean includeSectionCodeViolationStatus_withinWindow;
    private boolean includeSectionCodeViolationStatus_expiredWindow;
    private boolean includeSectionCodeViolationStatus_cited;
    
    
    // *****************************************
    // Section: Citations Switches
    // *****************************************
    private boolean includeSectionCitations;
    private boolean includeSectionCitations_citationsAnyStage;
    
    private boolean includeSectionCitations_pieChart;
    
    // *****************************************
    // Section: Street summary
    // *****************************************
    private boolean includeSectionStreetLevelSummary;
    
    
    // *****************************************
    // Section: Case Details 
    // *****************************************
    
    private boolean includeSectionCaseByCaseDetail;
    
    private boolean includeSectionCaseByCaseDetail_fullOwnerContactInfo;
    private boolean includeSectionCaseByCaseDetail_caseNames;
    private boolean includeSectionCaseByCaseDetail_violationList;
    private boolean includeSectionCaseByCaseDetail_extendedPropertyDetails;
    private boolean includeSectionCaseByCaseDetail_eventSummary;
    private boolean includeSectionCaseByCaseDetail_eventDescription;
    private RoleType includeSectionCaseByCaseDetail_eventListViewerRank;
    
    
    
    
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
    
    
    /**
     * No arg constructor
     */
    public ReportConfigCECaseList(){
        violationsCitedDateRange = new ArrayList<>();
        violationsLoggedComplianceDateRange = new ArrayList<>();
        violationsCitedDateRange = new ArrayList<>();
    }
    
    
    /**
     * Builds a list of street containers using the streetSCC
     * member. Call setStreetSCC(...) before this method
     */
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
     * @return the includeSectionCaseByCaseDetail_caseNames
     */
    public boolean isIncludeSectionCaseByCaseDetail_caseNames() {
        return includeSectionCaseByCaseDetail_caseNames;
    }

    /**
     * @return the includeSectionCaseByCaseDetail_fullOwnerContactInfo
     */
    public boolean isIncludeSectionCaseByCaseDetail_fullOwnerContactInfo() {
        return includeSectionCaseByCaseDetail_fullOwnerContactInfo;
    }

    /**
     * @return the includeSectionCaseByCaseDetail_violationList
     */
    public boolean isIncludeSectionCaseByCaseDetail_violationList() {
        return includeSectionCaseByCaseDetail_violationList;
    }

    /**
     * @return the includeSectionCaseByCaseDetail_eventSummary
     */
    public boolean isIncludeSectionCaseByCaseDetail_eventSummary() {
        return includeSectionCaseByCaseDetail_eventSummary;
    }

    /**
     * @param includeSectionCaseByCaseDetail_caseNames the includeSectionCaseByCaseDetail_caseNames to set
     */
    public void setIncludeSectionCaseByCaseDetail_caseNames(boolean includeSectionCaseByCaseDetail_caseNames) {
        this.includeSectionCaseByCaseDetail_caseNames = includeSectionCaseByCaseDetail_caseNames;
    }

    /**
     * @param includeSectionCaseByCaseDetail_fullOwnerContactInfo the includeSectionCaseByCaseDetail_fullOwnerContactInfo to set
     */
    public void setIncludeSectionCaseByCaseDetail_fullOwnerContactInfo(boolean includeSectionCaseByCaseDetail_fullOwnerContactInfo) {
        this.includeSectionCaseByCaseDetail_fullOwnerContactInfo = includeSectionCaseByCaseDetail_fullOwnerContactInfo;
    }

    /**
     * @param includeSectionCaseByCaseDetail_violationList the includeSectionCaseByCaseDetail_violationList to set
     */
    public void setIncludeSectionCaseByCaseDetail_violationList(boolean includeSectionCaseByCaseDetail_violationList) {
        this.includeSectionCaseByCaseDetail_violationList = includeSectionCaseByCaseDetail_violationList;
    }

    /**
     * @param includeSectionCaseByCaseDetail_eventSummary the includeSectionCaseByCaseDetail_eventSummary to set
     */
    public void setIncludeSectionCaseByCaseDetail_eventSummary(boolean includeSectionCaseByCaseDetail_eventSummary) {
        this.includeSectionCaseByCaseDetail_eventSummary = includeSectionCaseByCaseDetail_eventSummary;
    }

    /**
     * @return the includeSectionCaseByCaseDetail_extendedPropertyDetails
     */
    public boolean isIncludeSectionCaseByCaseDetail_extendedPropertyDetails() {
        return includeSectionCaseByCaseDetail_extendedPropertyDetails;
    }

    /**
     * @param includeSectionCaseByCaseDetail_extendedPropertyDetails the includeSectionCaseByCaseDetail_extendedPropertyDetails to set
     */
    public void setIncludeSectionCaseByCaseDetail_extendedPropertyDetails(boolean includeSectionCaseByCaseDetail_extendedPropertyDetails) {
        this.includeSectionCaseByCaseDetail_extendedPropertyDetails = includeSectionCaseByCaseDetail_extendedPropertyDetails;
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
     * @return the includeSectionReportSummary_ceCaseStatusPie
     */
    public boolean isIncludeSectionReportSummary_ceCaseStatusPie() {
        return includeSectionReportSummary_ceCaseStatusPie;
    }

    /**
     * @param includeSectionReportSummary_ceCaseStatusPie the includeSectionReportSummary_ceCaseStatusPie to set
     */
    public void setIncludeSectionReportSummary_ceCaseStatusPie(boolean includeSectionReportSummary_ceCaseStatusPie) {
        this.includeSectionReportSummary_ceCaseStatusPie = includeSectionReportSummary_ceCaseStatusPie;
    }

    /**
     * @return the includeSectionStreetLevelSummary
     */
    public boolean isIncludeSectionStreetLevelSummary() {
        return includeSectionStreetLevelSummary;
    }

    /**
     * @param includeSectionStreetLevelSummary the includeSectionStreetLevelSummary to set
     */
    public void setIncludeSectionStreetLevelSummary(boolean includeSectionStreetLevelSummary) {
        this.includeSectionStreetLevelSummary = includeSectionStreetLevelSummary;
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
     * @return the includeSectionCitations_pieChart
     */
    public boolean isIncludeSectionCitations_pieChart() {
        return includeSectionCitations_pieChart;
    }

    /**
     * @param includeSectionCitations_pieChart the includeSectionCitations_pieChart to set
     */
    public void setIncludeSectionCitations_pieChart(boolean includeSectionCitations_pieChart) {
        this.includeSectionCitations_pieChart = includeSectionCitations_pieChart;
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
     * @return the includeSectionReportSummary_closurePieChart
     */
    public boolean isIncludeSectionReportSummary_closurePieChart() {
        return includeSectionReportSummary_closurePieChart;
    }

    /**
     * @param includeSectionReportSummary_closurePieChart the includeSectionReportSummary_closurePieChart to set
     */
    public void setIncludeSectionReportSummary_closurePieChart(boolean includeSectionReportSummary_closurePieChart) {
        this.includeSectionReportSummary_closurePieChart = includeSectionReportSummary_closurePieChart;
    }

    /**
     * @return the includeSectionReportSummary
     */
    public boolean isIncludeSectionReportSummary() {
        return includeSectionReportSummary;
    }

    /**
     * @return the includeSectionReportSummary_openAsOfSOR
     */
    public boolean isIncludeSectionReportSummary_openAsOfSOR() {
        return includeSectionReportSummary_openAsOfSOR;
    }

    /**
     * @return the includeSectionReportSummary_openAsOfEOR
     */
    public boolean isIncludeSectionReportSummary_openAsOfEOR() {
        return includeSectionReportSummary_openAsOfEOR;
    }

    /**
     * @return the includeSectionReportSummary_casesOpened
     */
    public boolean isIncludeSectionReportSummary_casesOpened() {
        return includeSectionReportSummary_casesOpened;
    }

    /**
     * @return the includeSectionReportSummary_casesClosed
     */
    public boolean isIncludeSectionReportSummary_casesClosed() {
        return includeSectionReportSummary_casesClosed;
    }

    /**
     * @return the includeSectionReportSummary_newViolations
     */
    public boolean isIncludeSectionReportSummary_newViolations() {
        return includeSectionReportSummary_newViolations;
    }

    /**
     * @return the includeSectionReportSummary_eventsLogged
     */
    public boolean isIncludeSectionReportSummary_eventsLogged() {
        return includeSectionReportSummary_eventsLogged;
    }

    /**
     * @return the includeSectionReportSummary_eventsTotalTime
     */
    public boolean isIncludeSectionReportSummary_eventsTotalTime() {
        return includeSectionReportSummary_eventsTotalTime;
    }

    /**
     * @return the includeSectionCodeViolationStatus
     */
    public boolean isIncludeSectionCodeViolationStatus() {
        return includeSectionCodeViolationStatus;
    }

    /**
     * @return the includeSectionCodeViolationStatus_compliance
     */
    public boolean isIncludeSectionCodeViolationStatus_compliance() {
        return includeSectionCodeViolationStatus_compliance;
    }

    /**
     * @return the includeSectionCodeViolationStatus_withinWindow
     */
    public boolean isIncludeSectionCodeViolationStatus_withinWindow() {
        return includeSectionCodeViolationStatus_withinWindow;
    }

    /**
     * @return the includeSectionCodeViolationStatus_expiredWindow
     */
    public boolean isIncludeSectionCodeViolationStatus_expiredWindow() {
        return includeSectionCodeViolationStatus_expiredWindow;
    }

    /**
     * @return the includeSectionCodeViolationStatus_cited
     */
    public boolean isIncludeSectionCodeViolationStatus_cited() {
        return includeSectionCodeViolationStatus_cited;
    }

    /**
     * @return the includeSectionCitations
     */
    public boolean isIncludeSectionCitations() {
        return includeSectionCitations;
    }

    /**
     * @return the includeSectionCitations_citationsAnyStage
     */
    public boolean isIncludeSectionCitations_citationsAnyStage() {
        return includeSectionCitations_citationsAnyStage;
    }

    /**
     * @return the includeSectionCaseByCaseDetail
     */
    public boolean isIncludeSectionCaseByCaseDetail() {
        return includeSectionCaseByCaseDetail;
    }

    /**
     * @param includeSectionReportSummary the includeSectionReportSummary to set
     */
    public void setIncludeSectionReportSummary(boolean includeSectionReportSummary) {
        this.includeSectionReportSummary = includeSectionReportSummary;
    }

    /**
     * @param includeSectionReportSummary_openAsOfSOR the includeSectionReportSummary_openAsOfSOR to set
     */
    public void setIncludeSectionReportSummary_openAsOfSOR(boolean includeSectionReportSummary_openAsOfSOR) {
        this.includeSectionReportSummary_openAsOfSOR = includeSectionReportSummary_openAsOfSOR;
    }

    /**
     * @param includeSectionReportSummary_openAsOfEOR the includeSectionReportSummary_openAsOfEOR to set
     */
    public void setIncludeSectionReportSummary_openAsOfEOR(boolean includeSectionReportSummary_openAsOfEOR) {
        this.includeSectionReportSummary_openAsOfEOR = includeSectionReportSummary_openAsOfEOR;
    }

    /**
     * @param includeSectionReportSummary_casesOpened the includeSectionReportSummary_casesOpened to set
     */
    public void setIncludeSectionReportSummary_casesOpened(boolean includeSectionReportSummary_casesOpened) {
        this.includeSectionReportSummary_casesOpened = includeSectionReportSummary_casesOpened;
    }

    /**
     * @param includeSectionReportSummary_casesClosed the includeSectionReportSummary_casesClosed to set
     */
    public void setIncludeSectionReportSummary_casesClosed(boolean includeSectionReportSummary_casesClosed) {
        this.includeSectionReportSummary_casesClosed = includeSectionReportSummary_casesClosed;
    }

    /**
     * @param includeSectionReportSummary_newViolations the includeSectionReportSummary_newViolations to set
     */
    public void setIncludeSectionReportSummary_newViolations(boolean includeSectionReportSummary_newViolations) {
        this.includeSectionReportSummary_newViolations = includeSectionReportSummary_newViolations;
    }

    /**
     * @param includeSectionReportSummary_eventsLogged the includeSectionReportSummary_eventsLogged to set
     */
    public void setIncludeSectionReportSummary_eventsLogged(boolean includeSectionReportSummary_eventsLogged) {
        this.includeSectionReportSummary_eventsLogged = includeSectionReportSummary_eventsLogged;
    }

    /**
     * @param includeSectionReportSummary_eventsTotalTime the includeSectionReportSummary_eventsTotalTime to set
     */
    public void setIncludeSectionReportSummary_eventsTotalTime(boolean includeSectionReportSummary_eventsTotalTime) {
        this.includeSectionReportSummary_eventsTotalTime = includeSectionReportSummary_eventsTotalTime;
    }

    /**
     * @param includeSectionCodeViolationStatus the includeSectionCodeViolationStatus to set
     */
    public void setIncludeSectionCodeViolationStatus(boolean includeSectionCodeViolationStatus) {
        this.includeSectionCodeViolationStatus = includeSectionCodeViolationStatus;
    }

    /**
     * @param includeSectionCodeViolationStatus_compliance the includeSectionCodeViolationStatus_compliance to set
     */
    public void setIncludeSectionCodeViolationStatus_compliance(boolean includeSectionCodeViolationStatus_compliance) {
        this.includeSectionCodeViolationStatus_compliance = includeSectionCodeViolationStatus_compliance;
    }

    /**
     * @param includeSectionCodeViolationStatus_withinWindow the includeSectionCodeViolationStatus_withinWindow to set
     */
    public void setIncludeSectionCodeViolationStatus_withinWindow(boolean includeSectionCodeViolationStatus_withinWindow) {
        this.includeSectionCodeViolationStatus_withinWindow = includeSectionCodeViolationStatus_withinWindow;
    }

    /**
     * @param includeSectionCodeViolationStatus_expiredWindow the includeSectionCodeViolationStatus_expiredWindow to set
     */
    public void setIncludeSectionCodeViolationStatus_expiredWindow(boolean includeSectionCodeViolationStatus_expiredWindow) {
        this.includeSectionCodeViolationStatus_expiredWindow = includeSectionCodeViolationStatus_expiredWindow;
    }

    /**
     * @param includeSectionCodeViolationStatus_cited the includeSectionCodeViolationStatus_cited to set
     */
    public void setIncludeSectionCodeViolationStatus_cited(boolean includeSectionCodeViolationStatus_cited) {
        this.includeSectionCodeViolationStatus_cited = includeSectionCodeViolationStatus_cited;
    }

    /**
     * @param includeSectionCitations the includeSectionCitations to set
     */
    public void setIncludeSectionCitations(boolean includeSectionCitations) {
        this.includeSectionCitations = includeSectionCitations;
    }

    /**
     * @param includeSectionCitations_citationsAnyStage the includeSectionCitations_citationsAnyStage to set
     */
    public void setIncludeSectionCitations_citationsAnyStage(boolean includeSectionCitations_citationsAnyStage) {
        this.includeSectionCitations_citationsAnyStage = includeSectionCitations_citationsAnyStage;
    }

    /**
     * @param includeSectionCaseByCaseDetail the includeSectionCaseByCaseDetail to set
     */
    public void setIncludeSectionCaseByCaseDetail(boolean includeSectionCaseByCaseDetail) {
        this.includeSectionCaseByCaseDetail = includeSectionCaseByCaseDetail;
    }

    /**
     * @return the includeSectionReportSummary_compViol
     */
    public boolean isIncludeSectionReportSummary_compViol() {
        return includeSectionReportSummary_compViol;
    }

    /**
     * @param includeSectionReportSummary_compViol the includeSectionReportSummary_compViol to set
     */
    public void setIncludeSectionReportSummary_compViol(boolean includeSectionReportSummary_compViol) {
        this.includeSectionReportSummary_compViol = includeSectionReportSummary_compViol;
    }

    /**
     * @return the includeSectionCaseByCaseDetail_eventListViewerRank
     */
    public RoleType getIncludeSectionCaseByCaseDetail_eventListViewerRank() {
        return includeSectionCaseByCaseDetail_eventListViewerRank;
    }

    /**
     * @param includeSectionCaseByCaseDetail_eventListViewerRank the includeSectionCaseByCaseDetail_eventListViewerRank to set
     */
    public void setIncludeSectionCaseByCaseDetail_eventListViewerRank(RoleType includeSectionCaseByCaseDetail_eventListViewerRank) {
        this.includeSectionCaseByCaseDetail_eventListViewerRank = includeSectionCaseByCaseDetail_eventListViewerRank;
    }

    /**
     * @return the includeSectionCaseByCaseDetail_eventDescription
     */
    public boolean isIncludeSectionCaseByCaseDetail_eventDescription() {
        return includeSectionCaseByCaseDetail_eventDescription;
    }

    /**
     * @param includeSectionCaseByCaseDetail_eventDescription the includeSectionCaseByCaseDetail_eventDescription to set
     */
    public void setIncludeSectionCaseByCaseDetail_eventDescription(boolean includeSectionCaseByCaseDetail_eventDescription) {
        this.includeSectionCaseByCaseDetail_eventDescription = includeSectionCaseByCaseDetail_eventDescription;
    }
    
    
}
