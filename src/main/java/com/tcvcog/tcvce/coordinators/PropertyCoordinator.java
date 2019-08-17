/*
 * Copyright (C) 2017 Turtle Creek Valley
 * Council of Governments, PA
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
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.CaseLifecycleException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyUnitChange;
import com.tcvcog.tcvce.entities.PropertyUnitWithLists;
import com.tcvcog.tcvce.entities.PropertyWithLists;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Dominic Pimpinella
 */
public class PropertyCoordinator extends BackingBeanUtils implements Serializable {

    private final String DEFAULTUNITNUMBER = "-1";
    private final boolean DEFAULTRENTAL = false;
    
    public PropertyWithLists configurePropertyWithLists(PropertyWithLists propWL) throws IntegrationException, CaseLifecycleException{
        
        PropertyIntegrator pi = getPropertyIntegrator();
        
        if(propWL.getCeCaseList() == null){propWL.setCeCaseList(new ArrayList<CECase>());}
        if(propWL.getUnitWithListsList() == null){propWL.setUnitWithListsList(new ArrayList<PropertyUnitWithLists>());}
        if(propWL.getPersonList()== null){propWL.setPersonList(new ArrayList<Person>());}
        if(propWL.getInfoCaseList()== null){propWL.setInfoCaseList(new ArrayList<CECase>());}
        if(propWL.getChangeList()== null){propWL.setChangeList(new ArrayList<PropertyUnitChange>());}
        if(propWL.getBlobList()== null){propWL.setBlobList(new ArrayList<Integer>());}

        // add a unit number -1 to any PropertyWithoutAnyUnits

        
        return propWL;
    }
    
    public Property configureProperty(Property p){
        if(p.getUnitList() == null){p.setUnitList(new ArrayList<PropertyUnit>());}
        
        return p;
    }
    
    /**
     * Creates a new instance of PropertyUnitCoordinator
     */
    public PropertyCoordinator() {
    }
    
    public Property getNewProperty(){
        Property prop = new Property();
        return prop;
    }
    
    /**
     * This method generates a skeleton PropertyUnit with logical, preset defaults, including
     * empty lists.
     * @return 
     */
    public PropertyUnit getNewPropertyUnit(){
        PropertyUnit propUnit = new PropertyUnit();
        
        propUnit.setUnitNumber(Constants.DEFAULT_UNIT_NUMBER);
//        propUnit.setRental(DEFAULTRENTAL);
//        propUnit.setPropertyUnitPersonList(new ArrayList<Person>());
        return propUnit;
    }
    
    /**
     * Returns PropertyWithLists with all units, including default unit.
     * @param prop
     * @param u
     * @return PropertyWithLists object
     * @throws CaseLifecycleException 
     */
    public PropertyWithLists getPropertyUnits(Property prop, User u) throws CaseLifecycleException, EventException, AuthorizationException{
        PropertyIntegrator pi = getPropertyIntegrator();
        PropertyWithLists propWithLists = null;
        try{
            propWithLists = pi.getPropertyWithLists(prop.getPropertyID(), u);
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }     
        return propWithLists;
    }   
}
