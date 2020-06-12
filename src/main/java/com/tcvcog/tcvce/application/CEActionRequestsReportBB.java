/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.entities.reports.Report;
import com.tcvcog.tcvce.entities.reports.ReportCEARList;
import java.io.Serializable;
import javax.annotation.PostConstruct;

/**
 *
 * @author sylvia
 */
public class        CEActionRequestsReportBB 
        extends     BackingBeanUtils 
        implements  Serializable{

     private ReportCEARList reportConfig;
    
    
    /**
     * Creates a new instance of CEActionRequestsReportBB
     */
    public CEActionRequestsReportBB() {
    }
    
    @PostConstruct
    public void initBean(){
        Report r = getSessionBean().getSessReport();
        if(r instanceof ReportCEARList){
            reportConfig = (ReportCEARList) r;
        }
        
    }


    /**
     * @return the reportConfig
     */
    public ReportCEARList getReportConfig() {
        return reportConfig;
    }


    


    /**
     * @param reportConfig the reportConfig to set
     */
    public void setReportConfig(ReportCEARList reportConfig) {
        this.reportConfig = reportConfig;
    }


    
    
    
}
