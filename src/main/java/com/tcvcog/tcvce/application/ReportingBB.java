/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.SessionSystemCoordinator;
import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CasePhase;
import com.tcvcog.tcvce.entities.CaseStage;
import com.tcvcog.tcvce.entities.Report;
import com.tcvcog.tcvce.entities.ReportConfigCECase;
import com.tcvcog.tcvce.entities.ReportConfigCECaseList;
import com.tcvcog.tcvce.entities.ReportConfigCEEventList;
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
        SessionSystemCoordinator ssc = getSsCoordinator();
        caseList = getSessionBean().getcECaseQueue();
        
        try {
            cPhaseMap = ssc.getCaseCountsByPhase(caseList);
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
        reportCECase = getSessionBean().getReportConfigCECase();
        reportCECaseList = getSessionBean().getReportConfigCECaseList();
        if(reportCECase != null){
            currentReport = reportCECase;
        } else {
            currentReport = reportCECaseList;
        }
        
        generateModelCaseCountByPhase();
        generateModelCaseCountsByStage();
        generateModelViolationDonut();
        
    }
    
    private void generateModelViolationDonut(){
        CaseCoordinator cseCoord = getCaseCoordinator();
        CECase cse = getSessionBean().getcECaseQueue().get(0);
        violationDonut = new DonutChartModel();
        
        Map<String, Number> violComp = new LinkedHashMap<>();
        violComp.put("Resolved", cse.getViolationListResolved().size());
        violComp.put("Inside compliance timeframe", cse.getViolationListUnresolved().size());
        violComp.put("Expired compliance timeframe", cse.getViolationListUnresolved().size());
        violComp.put("Citation", cse.getViolationListUnresolved().size());
        violationDonut.addCircle(violComp);
        
        Map<String, Number> goalRing = new LinkedHashMap<>();
        goalRing.put("Goal: Resolved", 10 );
        goalRing.put("Goal: Unresolved", 90);
        violationDonut.addCircle(goalRing);
        
        
        violationDonut.setTitle("Violation status");
        violationDonut.setLegendPosition("e");
        
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
        SessionSystemCoordinator ssc = getSsCoordinator();
        ChartSeries caseCountSeries = new ChartSeries();
        caseCountSeries.setLabel("Count of CE cases");
        Map<CaseStage, Integer> stageMap = null;
        try {
             stageMap = ssc.getCaseCountsByStage(caseList);
        } catch (IntegrationException ex) {
            Logger.getLogger(ReportingBB.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CaseLifecyleException ex) {
            Logger.getLogger(ReportingBB.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Set<CaseStage> stageSet = stageMap.keySet();
        Integer max = 0;
        for(CaseStage s : stageSet) {
            Integer cnt = stageMap.get(s);
            if(cnt > max){
                max = cnt;
            }
            caseCountSeries.set(s,cnt);
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
        currentReport = getSessionBean().getActiveReport();
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

   
    
}
