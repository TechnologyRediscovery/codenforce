/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.entities.EventCECaseCasePropBundle;
import com.tcvcog.tcvce.entities.reports.ReportConfigCEEventList;
import java.util.List;

/**
 *
 * @author sylvia
 */
public class EventCECaseReportBB extends BackingBeanUtils{

    private ReportConfigCEEventList reportConfig; 
    
    /**
     * Creates a new instance of EventCECaseReportBB
     */
    public EventCECaseReportBB() {
    }

    /**
     * @return the reportConfig
     */
    public ReportConfigCEEventList getReportConfig() {
        return reportConfig;
    }

    /**
     * @param reportConfig the reportConfig to set
     */
    public void setReportConfig(ReportConfigCEEventList reportConfig) {
        this.reportConfig = reportConfig;
    }

    
    
}
