/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import java.io.Serializable;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;

/**
 *
 * @author sylvia
 */
public class DashboardDataBB extends BackingBeanUtils implements Serializable{

    private BarChartModel model;
    private BarChartModel caseClosings;
    
    /**
     * Creates a new instance of DashboardDataBB
     */
    public DashboardDataBB() {
        model = new BarChartModel();
        
        ChartSeries cases = new ChartSeries();
        cases.setLabel("Cases");
        cases.set("Investigation", 12);
        cases.set("Notice", 22);
        cases.set("Citation", 5);
        
        model.addSeries(cases);
        model.setTitle("Cases by stage");
        model.setLegendPosition("ne");
        
        Axis xAxis = model.getAxis(AxisType.X);
        xAxis.setLabel("Case Stage");
        
        Axis yAxis = model.getAxis(AxisType.Y);
        yAxis.setLabel("Num of open cases");
        yAxis.setMin(0);
        yAxis.setMax(100);
        
        caseClosings = new BarChartModel();
        
        ChartSeries closings = new ChartSeries();
        closings.setLabel("Closing");
        closings.set("No notice required", 2);
        closings.set("During notice period", 3);
        closings.set("Through citation", 20);
        
        caseClosings.addSeries(cases);
        caseClosings.setTitle("Case closings");
        caseClosings.setLegendPosition("ne");
        
        Axis xAxis2 = caseClosings.getAxis(AxisType.X);
        xAxis2.setLabel("Closing pathway");
        
        Axis yAxis2 = caseClosings.getAxis(AxisType.Y);
        yAxis2.setLabel("Num of cases");
        yAxis2.setMin(0);
        yAxis2.setMax(30);
    }

    /**
     * @return the model
     */
    public BarChartModel getModel() {
        return model;
    }

    /**
     * @param model the model to set
     */
    public void setModel(BarChartModel model) {
        this.model = model;
    }

    /**
     * @return the caseClosings
     */
    public BarChartModel getCaseClosings() {
        return caseClosings;
    }

    /**
     * @param caseClosings the caseClosings to set
     */
    public void setCaseClosings(BarChartModel caseClosings) {
        this.caseClosings = caseClosings;
    }
    
    
    
}
