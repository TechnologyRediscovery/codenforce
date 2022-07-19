/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodDataHeavy;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;

/**
 * Attempt at a new approach to shared list services
 * @author sylvia
 */
public class OccPeriodEventListBB extends BackingBeanUtils{

    private List<EventCnF> managedEventList;
    private OccPeriodDataHeavy currentPeriod;
    private boolean includeDeactivatedEvents;
    
    /**
     * Creates a new instance of CECaseEventListBB
     */
    public OccPeriodEventListBB() {
    }
    
    @PostConstruct
    public void initBean(){
        EventCoordinator ec = getEventCoordinator();
        System.out.println("CECaseEventListBB.initBean");
        
        currentPeriod = getSessionBean().getSessOccPeriod();
        try {
            managedEventList = ec.getEventList(currentPeriod);
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        setIncludeDeactivatedEvents(getSessionBean().isSessFlagIncludeDeactivatedEvents());
        
//        List<EventCnF> evlist = getSessionBean().getSessEventListForRefreshUptake();
        
        
//        if(getCurrentCase() != null){
//            if(evlist != null){
//                setManagedEventList(evlist);
//                System.out.println("CECaseEventListBB.getManagedEventList | fresh event list found on sessionbean of size: " + evlist.size());
//                getSessionBean().setSessEventListForRefreshUptake(null);
//            } else {
//                setManagedEventList(getCurrentCase().getEventList());
//            }
//            
//            if(managedEventList == null){
//                // something's wrong and we just need a list
//                setManagedEventList(new ArrayList<>());
//            }
            System.out.println("CECaseEventListBB.getManagedEventList | size BEFORE weeding: " + managedEventList.size() );
            configureManagedEventList();
//        }
        System.out.println("CECaseEventListBB.getManagedEventList | size AFTER weeding: " + managedEventList.size() );
    }
    
    /**
     * Checks boolean flag on cecase profile page's event list for including deactivated events
     */
    public void configureManagedEventList(){
        List<EventCnF> weededEvList = new ArrayList<>();
        if(managedEventList != null && !managedEventList.isEmpty()){
            for(EventCnF ev: managedEventList){
                boolean include = false;
                if(ev.getDeactivatedTS() == null) include = true;
                if(isIncludeDeactivatedEvents() && ev.getDeactivatedTS() != null) include = true;
                if(include) weededEvList.add(ev);
            }
            managedEventList = weededEvList;
        }
    }
    
    /**
     * Special getter for the event list whose contents is managed by a shared 
     * set of utility methods on EventBB
     * @return the fresh list of events for this case
     */
    public List<EventCnF> getManagedEventList(){
        return managedEventList;
    }
    

    /**
     * @param managedEventList the managedEventList to set
     */
    public void setManagedEventList(List<EventCnF> managedEventList) {
        this.managedEventList = managedEventList;
    }

    
    /**
     * @return the includeDeactivatedEvents
     */
    public boolean isIncludeDeactivatedEvents() {
        return includeDeactivatedEvents;
    }

    /**
     * @param includeDeactivatedEvents the includeDeactivatedEvents to set
     */
    public void setIncludeDeactivatedEvents(boolean includeDeactivatedEvents) {
        this.includeDeactivatedEvents = includeDeactivatedEvents;
    }

    /**
     * @return the currentPeriod
     */
    public OccPeriodDataHeavy getCurrentPeriod() {
        return currentPeriod;
    }

    /**
     * @param currentPeriod the currentPeriod to set
     */
    public void setCurrentPeriod(OccPeriodDataHeavy currentPeriod) {
        this.currentPeriod = currentPeriod;
    }
    
    
}
