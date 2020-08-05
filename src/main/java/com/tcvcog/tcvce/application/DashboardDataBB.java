/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    private BarChartModel caseCountByPhase;
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
     
    /**
     * First gen testing methods for charts
     * @deprecated 
     */
    private void createBarModels() {
//        createBarModel();
    }
     
    /**
     * First gen testing methods for charts
     * @deprecated 
     */
    private void createBarModel() {
        
         caseCountByPhase = new BarChartModel();
        
        ChartSeries cases = new ChartSeries();
        cases.setLabel("Cases");
        Map<String, Integer> ccMap = getCaseCountMap();
        Set<String> phaseKeys = ccMap.keySet();
        for (String key : phaseKeys) {
            cases.set(key ,ccMap.get(key));
        }
        
        
        caseCountByPhase.addSeries(cases);
        caseCountByPhase.setTitle("Case count by phase");
        caseCountByPhase.setLegendPosition("ne");
        caseCountByPhase.getAxis(AxisType.X).setTickAngle(45);
        
        Axis xAxis = caseCountByPhase.getAxis(AxisType.X);
        xAxis.setLabel("Case Phase");
        
        Axis yAxis = caseCountByPhase.getAxis(AxisType.Y);
        yAxis.setLabel("Num of open cases");
        yAxis.setMin(0);
        yAxis.setMax(15);
        
    }
     
   
    /**
     * Creates a new instance of DashboardDataBB
     */
    public DashboardDataBB() {
        
    }
    
    /**
     * First gen testing method for charts
     * @deprecated 
     * @return 
     */
    private Map<String, Integer> getCaseCountMap(){
        Map<String, Integer> caseCountMap = null;
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        SystemCoordinator ssc = getSystemCoordinator();
        SystemIntegrator si = getSystemIntegrator();
//        try {
//             caseCountMap = si.getCaseCountsByPhase(getSessionBean().getSessionMuni().getMuniCode());
//        } catch (IntegrationException ex) {
//            Logger.getLogger(DashboardDataBB.class.getName()).log(Level.SEVERE, null, ex);
//            System.out.println("DashboardDataBB.getCaseCountMap");
//        }
        return caseCountMap;
    }

    /**
     * @return the caseCountByPhase
     */
    public BarChartModel getCaseCountByPhase() {
        System.out.println("DashboardDataBB.DashboardDataBB.getModel");
        return caseCountByPhase;
    }

    /**
     * @param caseCountByPhase the caseCountByPhase to set
     */
    public void setCaseCountByPhase(BarChartModel caseCountByPhase) {
        this.caseCountByPhase = caseCountByPhase;
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
