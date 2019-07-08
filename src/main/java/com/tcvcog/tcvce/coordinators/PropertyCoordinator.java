/*
 * Copyright (C) 2017 Turtle Creek Valley
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
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyWithLists;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.occupancy.entities.OccPermit;
import com.tcvcog.tcvce.occupancy.entities.OccPermitApplication;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dominic Pimpinella
 */
public class PropertyCoordinator extends BackingBeanUtils implements Serializable {

    private final String DEFAULTUNITNUMBER = "0";
    private final boolean DEFAULTRENTAL = false;
    
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
        
        propUnit.setUnitNumber(DEFAULTUNITNUMBER);
        propUnit.setRental(DEFAULTRENTAL);
//        propUnit.setPropertyUnitPersonList(new ArrayList<Person>());

        return propUnit;
    }
    
    public PropertyWithLists getNewPropertyWithLists(){
        PropertyWithLists propWithLists = new PropertyWithLists();
        return propWithLists;
    }
    
    /**
     * Returns PropertyWithLists without default unit. Useful for displaying a list of units for
     * a multiunit property.
     * @param prop
     * @return PropertyWithLists object
     * @throws com.tcvcog.tcvce.domain.CaseLifecyleException
     */
    public PropertyWithLists getPropertyUnitsWithoutDefault(Property prop) throws CaseLifecyleException{
        PropertyIntegrator pi = getPropertyIntegrator();
        PropertyWithLists propWithLists = pi.getNewPropertyWithLists();
        
        try{
            propWithLists = pi.getPropertyWithLists(prop.getPropertyID());
            
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
        // Removes the default, automatically generated PropertyUnit        
        List<PropertyUnit> unitList = propWithLists.getUnitList();
        PropertyUnit defaultUnit = null;
        for(PropertyUnit unit:unitList){
            if(unit.getUnitNumber().equals("-1")){
            defaultUnit = unit;
            }
        }
        unitList.remove(defaultUnit);        

        return propWithLists;
    }
    /**
     * Returns PropertyWithLists with all units, including default unit.
     * @param prop
     * @return PropertyWithLists object
     * @throws CaseLifecyleException 
     */
    public PropertyWithLists getPropertyUnits(Property prop) throws CaseLifecyleException{
        PropertyIntegrator pi = getPropertyIntegrator();
        PropertyWithLists propWithLists = pi.getNewPropertyWithLists();
        
        try{
            propWithLists = pi.getPropertyWithLists(prop.getPropertyID());
            
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }     

        return propWithLists;
    }   
    
}
