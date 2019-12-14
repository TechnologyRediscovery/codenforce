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
import com.tcvcog.tcvce.entities.Credential;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.MunicipalityDataHeavy;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyUnitChange;
import com.tcvcog.tcvce.entities.PropertyUnitDataHeavy;
import com.tcvcog.tcvce.entities.PropertyDataHeavy;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dominic Pimpinella
 */
public class PropertyCoordinator extends BackingBeanUtils implements Serializable {

    private final String DEFAULTUNITNUMBER = "-1";
    private final boolean DEFAULTRENTAL = false;
    
    /**
     * Logic container for initializing members on the Property subclass PropertyWithLists
     * @param propWL
     * @return
     * @throws IntegrationException
     * @throws CaseLifecycleException 
     */
    public PropertyDataHeavy configurePropertyWithLists(PropertyDataHeavy propWL) throws IntegrationException, CaseLifecycleException{
        
        PropertyIntegrator pi = getPropertyIntegrator();
        
        if (propWL.getCeCaseList() == null) {
            propWL.setCeCaseList(new ArrayList<CECase>());
        }
        if (propWL.getUnitWithListsList() == null) {
            propWL.setUnitWithListsList(new ArrayList<PropertyUnitDataHeavy>());
        }
        if (propWL.getPersonList() == null) {
            propWL.setPersonList(new ArrayList<Person>());
        }
        if (propWL.getInfoCaseList() == null) {
            propWL.setInfoCaseList(new ArrayList<CECase>());
        }
        if (propWL.getChangeList() == null) {
            propWL.setChangeList(new ArrayList<PropertyUnitChange>());
        }
        if (propWL.getBlobList() == null) {
            propWL.setBlobList(new ArrayList<Integer>());
        }

        // add a unit number -1 to any PropertyWithoutAnyUnits
        
        return propWL;
    }
    
    /**
     * Logic container for Property setting configuration
     * @param p
     * @return 
     */
    public Property configureProperty(Property p){
        if(p.getUnitList() == null){
        
            p.setUnitList(new ArrayList<PropertyUnit>());
        
        }
        
        return p;
    }
    
     /**
     * Returns PropertyWithLists with all units, including default unit.
     * @param prop
     * @param cred
     * @return PropertyWithLists object
     * @throws CaseLifecycleException 
     * @throws com.tcvcog.tcvce.domain.EventException 
     * @throws com.tcvcog.tcvce.domain.AuthorizationException 
     */
    public PropertyDataHeavy getPropertyDataHeavy(Property prop, Credential cred) throws CaseLifecycleException, EventException, AuthorizationException{
        PropertyIntegrator pi = getPropertyIntegrator();
        PropertyDataHeavy propWithLists = null;
        try{
            propWithLists = pi.getPropertyDataHeavy(prop.getPropertyID());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }     
        return propWithLists;
    }   
    
    /**
     * Adapter method for folks who need a PropertyDataHeavy but who only have an
     * int propertyID; first it gets a simple property from the ID and then
     * beefs it up to a PropertyDataHeavy
     * @param propID
     * @param cred
     * @return
     * @throws IntegrationException
     * @throws CaseLifecycleException
     * @throws AuthorizationException
     * @throws EventException 
     */
    public PropertyDataHeavy getPropertyDataHeavy(int propID, Credential cred) throws IntegrationException, CaseLifecycleException, AuthorizationException, EventException{
        return getPropertyDataHeavy(getProperty(propID), cred);
    }
    
    
    
    /**
     * Adapter method for folks who need a PropertyDataHeavy but who only have an
     * int propertyID; first it gets a simple property from the ID and then
     * beefs it up to a PropertyDataHeavy
     * @param propUnitID
     * @param cred
     * @return
     * @throws IntegrationException
     * @throws CaseLifecycleException
     * @throws AuthorizationException
     * @throws EventException 
     */
    public PropertyDataHeavy getPropertyDataHeavyByUnit(int propUnitID, Credential cred) throws IntegrationException, CaseLifecycleException, AuthorizationException, EventException{
        PropertyIntegrator pi = getPropertyIntegrator();
        return getPropertyDataHeavy(pi.getPropertyUnitWithProp(propUnitID).getProperty(), cred);
    }
    
    
    /**
     * Logic intervention method for retrievals of simple properties from the DB
     * @param propID
     * @return
     * @throws IntegrationException 
     */
    public Property getProperty(int propID) throws IntegrationException{
        PropertyIntegrator pi = getPropertyIntegrator();
        return pi.getProperty(propID);
        
    }
    
    /**
     * Implements a cascade of logic to determine a best suited startup property
     * for a given session. When a user has no Property history, it extracts
     * the office property of the municipality into which a user is switching
     * 
     * ecd DEC-19
     * 
     * @param ua
     * @return 
     */
    public PropertyDataHeavy selectDefaultProperty(Credential cred){
        
        PropertyIntegrator pi = getPropertyIntegrator();
        MunicipalityCoordinator mc = getMuniCoordinator();
        
        
        if(cred != null){
            
             
            try {
                MunicipalityDataHeavy mdh = mc.getMuniDataHeavy(cred.getGoverningAuthPeriod().getMuni().getMuniCode());
                    if(mdh.getMuniOfficePropertyId() !=0){
                        return pi.getPropertyDataHeavy(mdh.getMuniOfficePropertyId());
                    } else {
                        return pi.getPropertyDataHeavy(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("arbitraryPlaceholderPropertyID")));
                    }
            } catch (IntegrationException | AuthorizationException | CaseLifecycleException | EventException ex) {
                System.out.println(ex);
            }
        }
        
        return  null;
        
    }
    
    
    
    /**
     * 
     * @param ua
     * @return 
     */
    public List<Property> assemblePropertyHistoryList(Credential cred){
        PropertyIntegrator pi = getPropertyIntegrator();
        List<Property> propList = new ArrayList<>();
        List<Integer> propIDList = new ArrayList<>();
        
        if(cred != null){
            try {
                propIDList.addAll(pi.getPropertyHistoryList(cred));
                while(!propIDList.isEmpty() && propIDList.size() <= Constants.MAX_BOB_HISTORY_SIZE){
                    // Only developers get a heterogeneous mix of muni in their history
                    if(cred.isHasDeveloperPermissions()){
                        propList.add(pi.getProperty(propIDList.remove(0)));
                    } else {
                        Municipality m = cred.getGoverningAuthPeriod().getMuni();
                        for(Property pr: propList){
                            if(pr.getMuniCode() == m.getMuniCode()){
                                propList.add(pr);
                            }
                        }
                    }
                }
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
            
        }
        return propList;
        
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
    
   
}
