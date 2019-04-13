/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.entities.ReportConfig;
import com.tcvcog.tcvce.entities.ReportConfigCECase;
import com.tcvcog.tcvce.entities.ReportConfigCEEventList;
import java.io.Serializable;
import javax.annotation.PostConstruct;

/**
 *
 * @author sylvia
 */
public class ReportingBB extends BackingBeanUtils implements Serializable{
    
    private ReportConfigCEEventList reportCEEvent;
    private ReportConfigCECase reportCECase;
    
    private ReportConfig currentReport;
    
   

    /**
     * Creates a new instance of ReportingBB
     */
    public ReportingBB() {
   
    }
    
    @PostConstruct
    public void initBean(){
        reportCECase = getSessionBean().getReportConfigCECase();
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
        return currentReport;
    }

    /**
     * @param currentReport the currentReport to set
     */
    public void setCurrentReport(ReportConfig currentReport) {
        this.currentReport = currentReport;
    }
    
}
