/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.ReportCEARs;
import com.tcvcog.tcvce.entities.search.QueryCEAR;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;

/**
 *
 * @author sylvia
 */
public class CEActionRequestsReportBB extends BackingBeanUtils implements Serializable{

     private List<CEActionRequest> requestReportList;
     private ReportCEARs reportConfig;
     private QueryCEAR queryCEAR;
     private boolean validReport;
    
    /**
     * Creates a new instance of CEActionRequestsReportBB
     */
    public CEActionRequestsReportBB() {
    }
    
    @PostConstruct
    public void initBean(){
        
    }

    /**
     * @return the requestReportList
     */
    public List<CEActionRequest> getRequestReportList() {
        return requestReportList;
    }

    /**
     * @return the reportConfig
     */
    public ReportCEARs getReportConfig() {
        return reportConfig;
    }

    /**
     * @return the queryCEAR
     */
    public QueryCEAR getQueryCEAR() {
        return queryCEAR;
    }

    /**
     * @return the validReport
     */
    public boolean isValidReport() {
        return validReport;
    }

    /**
     * @param requestReportList the requestReportList to set
     */
    public void setRequestReportList(List<CEActionRequest> requestReportList) {
        this.requestReportList = requestReportList;
    }

    /**
     * @param reportConfig the reportConfig to set
     */
    public void setReportConfig(ReportCEARs reportConfig) {
        this.reportConfig = reportConfig;
    }

    /**
     * @param queryCEAR the queryCEAR to set
     */
    public void setQueryCEAR(QueryCEAR queryCEAR) {
        this.queryCEAR = queryCEAR;
    }

    /**
     * @param validReport the validReport to set
     */
    public void setValidReport(boolean validReport) {
        this.validReport = validReport;
    }
    
    
    
}
