/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.entities.ReportConfig;
import com.tcvcog.tcvce.entities.ReportConfigCECase;
import com.tcvcog.tcvce.entities.ReportConfigCECaseList;
import com.tcvcog.tcvce.entities.ReportConfigCEEventList;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import org.primefaces.model.chart.BarChartModel;

/**
 *
 * @author sylvia
 */
public class ReportingBB extends BackingBeanUtils implements Serializable{
    
    private ReportConfigCECase reportCECase;
    private ReportConfigCECaseList reportCECaseList;
    
    private ReportConfig currentReport;
   
    private ReportConfigCEEventList reportCEEvent;
    
    private BarChartModel caseCountByStage;
    private BarChartModel violationCountByOrdinance;
     

    /**
     * Creates a new instance of ReportingBB
     */
    public ReportingBB() {
   
    }
    
    @PostConstruct
    public void initBean(){
        reportCECase = getSessionBean().getReportConfigCECase();
        reportCECaseList = getSessionBean().getReportConfigCECaseList();
        if(reportCECase != null){
            currentReport = reportCECase;
        } else {
            currentReport = reportCECaseList;
        }
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
    public ReportConfig getCurrentReport() {
        currentReport = getSessionBean().getActiveReport();
        return currentReport;
    }

    /**
     * @param currentReport the currentReport to set
     */
    public void setCurrentReport(ReportConfig currentReport) {
        
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
     * @return the caseCountByStage
     */
    public BarChartModel getCaseCountByStage() {
        return caseCountByStage;
    }

    /**
     * @param caseCountByStage the caseCountByStage to set
     */
    public void setCaseCountByStage(BarChartModel caseCountByStage) {
        this.caseCountByStage = caseCountByStage;
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
    
}
