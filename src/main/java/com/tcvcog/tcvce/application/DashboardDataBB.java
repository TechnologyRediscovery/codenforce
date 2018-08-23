/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.HorizontalBarChartModel;

/**
 *
 * @author sylvia
 */
public class DashboardDataBB extends BackingBeanUtils implements Serializable{

    private BarChartModel model;
    private BarChartModel caseClosings;
    
    private BarChartModel barModel;
    private HorizontalBarChartModel horizontalBarModel;
 
    @PostConstruct
    public void init() {
        createBarModels();
    }
 
    public BarChartModel getBarModel() {
        return barModel;
    }
     
    public HorizontalBarChartModel getHorizontalBarModel() {
        return horizontalBarModel;
    }
 
    private BarChartModel initBarModel() {
        BarChartModel model1 = new BarChartModel();
 
        ChartSeries boys = new ChartSeries();
        boys.setLabel("Boys");
        boys.set("2004", 120);
        boys.set("2005", 100);
        boys.set("2006", 44);
        boys.set("2007", 150);
        boys.set("2008", 25);
 
        ChartSeries girls = new ChartSeries();
        girls.setLabel("Girls");
        girls.set("2004", 52);
        girls.set("2005", 60);
        girls.set("2006", 110);
        girls.set("2007", 135);
        girls.set("2008", 120);
 
        model1.addSeries(boys);
        model1.addSeries(girls);
         
        return model1;
    }
     
    private void createBarModels() {
        createBarModel();
        createHorizontalBarModel();
    }
     
    private void createBarModel() {
        barModel = initBarModel();
         
        barModel.setTitle("Bar Chart");
        barModel.setLegendPosition("ne");
         
        Axis xAxis = barModel.getAxis(AxisType.X);
        xAxis.setLabel("Gender");
         
        Axis yAxis = barModel.getAxis(AxisType.Y);
        yAxis.setLabel("Births");
        yAxis.setMin(0);
        yAxis.setMax(200);
    }
     
    private void createHorizontalBarModel() {
        horizontalBarModel = new HorizontalBarChartModel();
 
        ChartSeries boys = new ChartSeries();
        boys.setLabel("Boys");
        boys.set("2004", 50);
        boys.set("2005", 96);
        boys.set("2006", 44);
        boys.set("2007", 55);
        boys.set("2008", 25);
 
        ChartSeries girls = new ChartSeries();
        girls.setLabel("Girls");
        girls.set("2004", 52);
        girls.set("2005", 60);
        girls.set("2006", 82);
        girls.set("2007", 35);
        girls.set("2008", 120);
 
        horizontalBarModel.addSeries(boys);
        horizontalBarModel.addSeries(girls);
         
        horizontalBarModel.setTitle("Horizontal and Stacked");
        horizontalBarModel.setLegendPosition("e");
        horizontalBarModel.setStacked(true);
         
        Axis xAxis = horizontalBarModel.getAxis(AxisType.X);
        xAxis.setLabel("Births");
        xAxis.setMin(0);
        xAxis.setMax(200);
         
        Axis yAxis = horizontalBarModel.getAxis(AxisType.Y);
        yAxis.setLabel("Gender");        
    }
    
    
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
        System.out.println("DashboardDataBB.DashboardDataBB.getModel");
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
