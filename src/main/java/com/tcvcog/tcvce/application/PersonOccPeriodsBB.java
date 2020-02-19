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


import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.entities.PersonDataHeavy;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import java.util.List;
import javax.annotation.PostConstruct;

/**
 *
 * @author Ellen Bascomb
 */
public class PersonOccPeriodsBB extends BackingBeanUtils{

    private PersonDataHeavy currPerson;
    
    private List<OccPeriod> occPeriodListFiltered;
    
    /**
     * Creates a new instance of PersonBB
     */
    public PersonOccPeriodsBB() {
      
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
    
    public String exploreOccPeriod(OccPeriod op){
        getSessionBean().setSessOccPeriodQueued(op);
        return "occPeriodWorkflow";
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
     * @return the occPeriodListFiltered
     */
    public List<OccPeriod> getOccPeriodListFiltered() {
        return occPeriodListFiltered;
    }

    /**
     * @param occPeriodListFiltered the occPeriodListFiltered to set
     */
    public void setOccPeriodListFiltered(List<OccPeriod> occPeriodListFiltered) {
        this.occPeriodListFiltered = occPeriodListFiltered;
    }
    
    
}
