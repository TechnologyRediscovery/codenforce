/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.DomainEnum;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.IFace_EventHolder;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;

/**
 * Attempt at a new approach to shared list services
 * @author sylvia
 */
public class EventListRequestScopedBB extends BackingBeanUtils{

    private List<EventCnF> managedEventList;
    private IFace_EventHolder currentEvHolder;
    private boolean includeDeactivatedEvents;
    
    /**
     * Creates a new instance of CECaseEventListBB
     */
    public EventListRequestScopedBB() {
    }
    
    @PostConstruct
    public void initBean(){
        EventCoordinator ec = getEventCoordinator();
        
        DomainEnum domain = getSessionEventConductor().getSessEventsPageEventDomainRequest();
        if(domain != null){
            System.out.println("EventListRequestScopedBB.initBean | domain enum: " + domain.getTitle());
        } else {
            System.out.println("EventListRequestScopedBB.initBean | domain enum null: " );
        }
        try {
        if(domain != null){
            switch(domain){
                case CODE_ENFORCEMENT:
                    managedEventList = ec.getEventList(getSessionBean().getSessCECase());
                    currentEvHolder = getSessionBean().getSessCECase();
                    break;
                case OCCUPANCY:
                    managedEventList = ec.getEventList(getSessionBean().getSessOccPeriod());
                    currentEvHolder = getSessionBean().getSessOccPeriod();
                    break;
                case PARCEL:
                    managedEventList = ec.getEventList(getSessionBean().getSessProperty());
                    currentEvHolder = getSessionBean().getSessProperty();
                    break;
                case UNIVERSAL:
                    managedEventList = new ArrayList<>();
                    break;
                default: 
                    managedEventList = new ArrayList<>();
            }
        }
            
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        setIncludeDeactivatedEvents(getSessionBean().isSessFlagIncludeDeactivatedEvents());
        
//        
            System.out.println("EventListRequestScopedBB.getManagedEventList | size BEFORE weeding: " + managedEventList.size() );
            configureManagedEventList();
//        }
        System.out.println("EventListRequestScopedBB.getManagedEventList | size AFTER weeding: " + managedEventList.size() );
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
     * @return the currentEvHolder
     */
    public IFace_EventHolder getCurrentEvHolder() {
        return currentEvHolder;
    }

    /**
     * @param currentEvHolder the currentEvHolder to set
     */
    public void setCurrentEvHolder(IFace_EventHolder currentEvHolder) {
        this.currentEvHolder = currentEvHolder;
    }
    
    
}
