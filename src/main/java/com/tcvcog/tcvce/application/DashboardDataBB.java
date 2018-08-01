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
    
    
    
}
