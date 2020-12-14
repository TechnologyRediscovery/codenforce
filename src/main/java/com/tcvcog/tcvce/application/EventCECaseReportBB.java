/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.entities.reports.Report;
import com.tcvcog.tcvce.entities.reports.ReportConfigCEEventList;
import java.util.List;
import javax.annotation.PostConstruct;

/**
 *
 * @author sylvia
 */
public class EventCECaseReportBB extends BackingBeanUtils{

    private ReportConfigCEEventList reportConfig; 
    
    /**
     * Creates a new instance of EventCnFReportBBcecase
     */
    public EventCECaseReportBB() {
    }
    
    @PostConstruct
    public void initBean(){
        Report configs = getSessionBean().getSessReport();
        if(configs instanceof ReportConfigCEEventList){
            reportConfig = (ReportConfigCEEventList) configs;
        }
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
