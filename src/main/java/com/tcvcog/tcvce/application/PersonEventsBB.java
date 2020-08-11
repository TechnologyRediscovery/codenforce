/*
 * Copyright (C) 2018 Turtle Creek Valley
Council of Governments, PA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tcvcog.tcvce.application;


import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventDomainEnum;
import com.tcvcog.tcvce.entities.PersonDataHeavy;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;

/**
 *
 * @author Ellen Bascomb
 */
public class PersonEventsBB extends BackingBeanUtils{

    private PersonDataHeavy currPerson;
    private List<EventCnF> eventListFiltered;
    
    /**
     * Creates a new instance of PersonBB
     */
    public PersonEventsBB() {
      
    }
    
    @PostConstruct
    public void initBean(){
       PersonCoordinator pc = getPersonCoordinator();
       
       if(getSessionBean().getSessPersonQueued() != null){
            currPerson = pc.assemblePersonDataHeavy(getSessionBean().getSessPersonQueued(), 
                    getSessionBean().getSessUser().getKeyCard());
            getSessionBean().setSessPerson(currPerson);
            getSessionBean().setSessPersonQueued(null);
       } else {
            currPerson = (getSessionBean().getSessPerson());
       }
        
    }
    
    
    public String exploreParentObject(EventCnF ev){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
        if(ev != null){
            if(ev.getDomain() == EventDomainEnum.OCCUPANCY){
                try {
                    getSessionBean().setSessOccPeriodQueued(oc.getOccPeriod(ev.getOccPeriodID()));
                    return "occPeriodWorkflow";
                } catch (IntegrationException ex) {
                    System.out.println(ex);
                }
            } else if(ev.getDomain() == EventDomainEnum.CODE_ENFORCEMENT){
                try{
                    getSessionBean().setSessCECaseQueued(cc.cecase_getCECase(ev.getCeCaseID()));
                    return "ceCaseWorkflow";
                } catch (IntegrationException ex) {
                    System.out.println(ex);
                }
            }
        }
        return "";
    }

    /**
     * @return the currPerson
     */
    public PersonDataHeavy getCurrPerson() {
        return currPerson;
    }

    /**
     * @param currPerson the currPerson to set
     */
    public void setCurrPerson(PersonDataHeavy currPerson) {
        this.currPerson = currPerson;
    }

    /**
     * @return the eventListFiltered
     */
    public List<EventCnF> getEventListFiltered() {
        return eventListFiltered;
    }

    /**
     * @param eventListFiltered the eventListFiltered to set
     */
    public void setEventListFiltered(List<EventCnF> eventListFiltered) {
        this.eventListFiltered = eventListFiltered;
    }
    
    
}
