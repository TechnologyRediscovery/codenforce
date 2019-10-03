/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.DataCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.CaseLifecycleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CasePhase;
import com.tcvcog.tcvce.entities.CaseStage;
import com.tcvcog.tcvce.entities.reports.Report;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECase;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECaseList;
import com.tcvcog.tcvce.entities.reports.ReportConfigCEEventList;
import com.tcvcog.tcvce.entities.reports.ReportConfigOccInspection;
import com.tcvcog.tcvce.entities.reports.ReportConfigOccPermit;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.DonutChartModel;
import org.primefaces.model.chart.HorizontalBarChartModel;

/**
 *
 * @author sylvia
 */
public class ReportingBB extends BackingBeanUtils implements Serializable{
    
    private ReportConfigCECase reportCECase;
    private ReportConfigCECaseList reportCECaseList;
    
    private Report currentReport;
   
    private ReportConfigCEEventList reportCEEvent;
    
    private ReportConfigOccInspection reportConfigOccInspection;
    private ReportConfigOccPermit reportConfigOccPermit;
    
    private HorizontalBarChartModel caseCountByPhase;
    private HorizontalBarChartModel caseCountByStage;
    
    private BarChartModel violationCountByOrdinance;
    
    private DonutChartModel violationDonut;
    
    
    private List<CECase> caseList;
    private Map<CasePhase, Integer> cPhaseMap;
     

    /**
     * Creates a new instance of ReportingBB
     */
    public ReportingBB() {
   
    }
    
    @PostConstruct
    public void initBean(){
        caseList = getSessionBean().getSessionCECaseList();
        DataCoordinator dc = getDataCoordinator();
        
        if(caseList != null && !caseList.isEmpty()){
            
            try {
                cPhaseMap = dc.getCaseCountsByPhase(caseList);
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
            generateModelCaseCountByPhase();
            generateModelCaseCountsByStage();
        }
        
        reportCECase = getSessionBean().getReportConfigCECase();
        reportCECaseList = getSessionBean().getReportConfigCECaseList();
        reportConfigOccPermit = getSessionBean().getReportConfigOccPermit();
        reportConfigOccInspection = getSessionBean().getReportConfigInspection();
        
        if(reportCECase != null){
            currentReport = reportCECase;
        } else {
            currentReport = reportCECaseList;
        }
        
        
    }
    
     private void generateModelCaseCountByPhase() {
        
        caseCountByPhase = new HorizontalBarChartModel();
        
        ChartSeries caseCountSeries = new ChartSeries();
        caseCountSeries.setLabel("Count of CE cases");
        Set<CasePhase> phaseSet = cPhaseMap.keySet();
        Integer max = 0;
        for(CasePhase p : phaseSet) {
            Integer cnt = cPhaseMap.get(p);
            if(cnt > max){
                max = cnt;
            }
            caseCountSeries.set(p,cnt);
        }
              
        caseCountByPhase.addSeries(caseCountSeries);
        caseCountByPhase.setTitle("Case count by phase");
        caseCountByPhase.setLegendPosition("ne");
        caseCountByPhase.getAxis(AxisType.Y).setTickAngle(-45);
        
        Axis yAxis = caseCountByPhase.getAxis(AxisType.Y);
        yAxis.setLabel("Case Phase");
        
        Axis xAxis = caseCountByPhase.getAxis(AxisType.X);
        xAxis.setLabel("Num of open cases");
        xAxis.setMin(0);
        // add 2 for spacing
        xAxis.setMax(max + 1);
        
    }
     
     public void generateModelCaseCountsByStage(){
         caseCountByStage = new HorizontalBarChartModel();
         DataCoordinator dc = getDataCoordinator();
        ChartSeries caseCountSeries = new ChartSeries();
        caseCountSeries.setLabel("Count of CE cases");
        Map<CaseStage, Integer> stageMap = null;
        try {
             stageMap = dc.getCaseCountsByStage(caseList);
        } catch (IntegrationException | CaseLifecycleException ex) {
            System.out.println(ex);
        }
        Integer max = 0;
        if(stageMap != null && stageMap.keySet() != null){
            
            Set<CaseStage> stageSet = stageMap.keySet();
            for(CaseStage s : stageSet) {
                Integer cnt = stageMap.get(s);
                if(cnt > max){
                    max = cnt;
                }
                caseCountSeries.set(s,cnt);
            }
        }
              
        caseCountByStage.addSeries(caseCountSeries);
        caseCountByStage.setTitle("Case count by stage");
        caseCountByStage.setLegendPosition("ne");
        caseCountByStage.getAxis(AxisType.Y).setTickAngle(-45);
        
        Axis yAxis = caseCountByStage.getAxis(AxisType.Y);
        yAxis.setLabel("Case Stage");
        
        Axis xAxis = caseCountByStage.getAxis(AxisType.X);
        xAxis.setLabel("Num of open cases");
        xAxis.setMin(0);
        // add 1 to breathe
        xAxis.setMax(max + 1);
     }
     
     
    

    /**
     * @return the reportCEEvent
     */
    public ReportConfigCEEventList getReportCEEvent() {
        return reportCEEvent;
    }

    /**
     * @return the reportCECase
     */
    public ReportConfigCECase getReportCECase() {
        return reportCECase;
    }

    /**
     * @param reportCEEvent the reportCEEvent to set
     */
    public void setReportCEEvent(ReportConfigCEEventList reportCEEvent) {
        this.reportCEEvent = reportCEEvent;
    }

    /**
     * @param reportCECase the reportCECase to set
     */
    public void setReportCECase(ReportConfigCECase reportCECase) {
        this.reportCECase = reportCECase;
    }

    /**
     * @return the currentReport
     */
    public Report getCurrentReport() {
        currentReport = getSessionBean().getSessionReport();
        return currentReport;
    }

    /**
     * @param currentReport the currentReport to set
     */
    public void setCurrentReport(Report currentReport) {
        
        this.currentReport = currentReport;
    }

    /**
     * @return the reportCECaseList
     */
    public ReportConfigCECaseList getReportCECaseList() {
        return reportCECaseList;
    }

    /**
     * @param reportCECaseList the reportCECaseList to set
     */
    public void setReportCECaseList(ReportConfigCECaseList reportCECaseList) {
        this.reportCECaseList = reportCECaseList;
    }

    /**
     * @return the caseCountByPhase
     */
    public HorizontalBarChartModel getCaseCountByPhase() {
        return caseCountByPhase;
    }

    /**
     * @param caseCountByPhase the caseCountByPhase to set
     */
    public void setCaseCountByPhase(HorizontalBarChartModel caseCountByPhase) {
        this.caseCountByPhase = caseCountByPhase;
    }

    /**
     * @return the violationCountByOrdinance
     */
    public BarChartModel getViolationCountByOrdinance() {
        return violationCountByOrdinance;
    }

    /**
     * @param violationCountByOrdinance the violationCountByOrdinance to set
     */
    public void setViolationCountByOrdinance(BarChartModel violationCountByOrdinance) {
        this.violationCountByOrdinance = violationCountByOrdinance;
    }

    /**
     * @return the caseList
     */
    public List<CECase> getCaseList() {
        return caseList;
    }

    /**
     * @param caseList the caseList to set
     */
    public void setCaseList(List<CECase> caseList) {
        this.caseList = caseList;
    }

    /**
     * @return the cPhaseMap
     */
    public Map<CasePhase, Integer> getcPhaseMap() {
        return cPhaseMap;
    }

    /**
     * @param cPhaseMap the cPhaseMap to set
     */
    public void setcPhaseMap(Map<CasePhase, Integer> cPhaseMap) {
        this.cPhaseMap = cPhaseMap;
    }

    /**
     * @return the caseCountByStage
     */
    public HorizontalBarChartModel getCaseCountByStage() {
        return caseCountByStage;
    }

    /**
     * @param caseCountByStage the caseCountByStage to set
     */
    public void setCaseCountByStage(HorizontalBarChartModel caseCountByStage) {
        this.caseCountByStage = caseCountByStage;
    }

    /**
     * @return the violationDonut
     */
    public DonutChartModel getViolationDonut() {
        return violationDonut;
    }

    /**
     * @param violationDonut the violationDonut to set
     */
    public void setViolationDonut(DonutChartModel violationDonut) {
        this.violationDonut = violationDonut;
    }

    /**
     * @return the reportConfigOccInspection
     */
    public ReportConfigOccInspection getReportConfigOccInspection() {
        return reportConfigOccInspection;
    }

    /**
     * @param reportConfigOccInspection the reportConfigOccInspection to set
     */
    public void setReportConfigOccInspection(ReportConfigOccInspection reportConfigOccInspection) {
        this.reportConfigOccInspection = reportConfigOccInspection;
    }

    /**
     * @return the reportConfigOccPermit
     */
    public ReportConfigOccPermit getReportConfigOccPermit() {
        return reportConfigOccPermit;
    }

    /**
     * @param reportConfigOccPermit the reportConfigOccPermit to set
     */
    public void setReportConfigOccPermit(ReportConfigOccPermit reportConfigOccPermit) {
        this.reportConfigOccPermit = reportConfigOccPermit;
    }

   
    
}
