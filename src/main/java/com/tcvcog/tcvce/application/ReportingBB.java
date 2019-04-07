/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.entities.ReportConfigCECase;
import com.tcvcog.tcvce.entities.ReportConfigCEEventList;
import java.io.Serializable;
import javax.annotation.PostConstruct;

/**
 *
 * @author sylvia
 */
public class ReportingBB extends BackingBeanUtils implements Serializable{
    
    private ReportConfigCEEventList rptCEEvent;
    private ReportConfigCECase rptCECase;
    
   

    /**
     * Creates a new instance of ReportingBB
     */
    public ReportingBB() {
   
    }
    
    @PostConstruct
    public void initBean(){
        rptCECase = getSessionBean().getReportConfigCECase();
    }
    

    /**
     * @return the rptCEEvent
     */
    public ReportConfigCEEventList getRptCEEvent() {
        return rptCEEvent;
    }

    /**
     * @return the rptCECase
     */
    public ReportConfigCECase getRptCECase() {
        return rptCECase;
    }

    /**
     * @param rptCEEvent the rptCEEvent to set
     */
    public void setRptCEEvent(ReportConfigCEEventList rptCEEvent) {
        this.rptCEEvent = rptCEEvent;
    }

    /**
     * @param rptCECase the rptCECase to set
     */
    public void setRptCECase(ReportConfigCECase rptCECase) {
        this.rptCECase = rptCECase;
    }
    
}
