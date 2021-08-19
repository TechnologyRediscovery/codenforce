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
public  class PersonDataHeavy 
        extends Person 
        implements IFace_CredentialSigned{
    
    private List<CECasePropertyUnitHeavy> caseList;
    private List<OccPeriodPropertyUnitHeavy> periodList;
    private List<Property> propertyList;
    private List<EventCnFPropUnitCasePeriodHeavy> eventList;
    
    private String credentialSignature;
    
   
    
    /**
     * @return the credentialSignature
     */
    @Override
    public String getCredentialSignature() {
        return credentialSignature;
    }

    
    
    public PersonDataHeavy(Person p, Credential cred){
        super(p);
        if(cred != null){
            credentialSignature = cred.getSignature();
        }
      
        
    }
    
    /**
     * Dead on arrival method to follow pattern of other BObs whose previous
     * DataHeavy versions did not require Credentials to Instantiate
     * 
     * @deprecated 
     * @param p to be injected into the superclass members
     * 
     * 
     */
     public PersonDataHeavy(Person p){
        super(p);
        
        
    }

    /**
     * @return the caseList
     */
    public List<CECasePropertyUnitHeavy> getCaseList() {
        return caseList;
    }

    /**
     * @return the periodList
     */
    public List<OccPeriodPropertyUnitHeavy> getPeriodList() {
        return periodList;
    }

    /**
     * @return the propertyList
     */
    public List<Property> getPropertyList() {
        return propertyList;
    }

    /**
     * @return the eventList
     */
    public List<EventCnFPropUnitCasePeriodHeavy> getEventList() {
        return eventList;
    }

    /**
     * @param caseList the caseList to set
     */
    public void setCaseList(List<CECasePropertyUnitHeavy> caseList) {
        this.caseList = caseList;
    }

    /**
     * @param periodList the periodList to set
     */
    public void setPeriodList(List<OccPeriodPropertyUnitHeavy> periodList) {
        this.periodList = periodList;
    }

    /**
     * @param propertyList the propertyList to set
     */
    public void setPropertyList(List<Property> propertyList) {
        this.propertyList = propertyList;
    }

    /**
     * @param eventList the eventList to set
     */
    public void setEventList(List<EventCnFPropUnitCasePeriodHeavy> eventList) {
        this.eventList = eventList;
    }

 
   
    
}
