/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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
package com.tcvcog.tcvce.entities;

import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodPropertyUnitHeavy;
import java.util.List;

/**
 * The Listified version of a Person
 * @author sylvia
 */
public  class PersonLinkHeavy 
        extends Person {
    
    private List<HumanLink> humanLinkList;
    
    /**
     * Dead on arrival method to follow pattern of other BObs whose previous
     * DataHeavy versions did not require Credentials to Instantiate
     * 
     * @deprecated 
     * @param p to be injected into the superclass members
     * 
     */
     public PersonLinkHeavy(Person p){
        super(p);
    }

    /**
     * @return the humanLinkList
     */
    public List<HumanLink> getHumanLinkList() {
        return humanLinkList;
    }

    /**
     * @param humanLinkList the humanLinkList to set
     */
    public void setHumanLinkList(List<HumanLink> humanLinkList) {
        this.humanLinkList = humanLinkList;
    }
}
