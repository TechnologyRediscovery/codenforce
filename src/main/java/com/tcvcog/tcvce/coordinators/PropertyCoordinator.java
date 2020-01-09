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
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.Credential;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.MunicipalityDataHeavy;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyUnitChangeOrder;
import com.tcvcog.tcvce.entities.PropertyUnitDataHeavy;
import com.tcvcog.tcvce.entities.PropertyDataHeavy;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.search.QueryCECase;
import com.tcvcog.tcvce.entities.search.QueryCECaseEnum;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dominic Pimpinella, MaRoSco, Ellen Bascomb (31Y)
 */
public class PropertyCoordinator extends BackingBeanUtils implements Serializable {

    private final String DEFAULTUNITNUMBER = "-1";
    private final boolean DEFAULTRENTAL = false;
    
    /**
     * Logic container for initializing members on the Property subclass PropertyWithLists
     * @param pr
     * @param cred
     * @return
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public PropertyDataHeavy assemblePropertyDataHeavy(Property pr, Credential cred) throws IntegrationException, BObStatusException, SearchException{
        
        PropertyIntegrator pi = getPropertyIntegrator();
        SearchCoordinator sc = getSearchCoordinator();
        
        
        PropertyDataHeavy propWL = new PropertyDataHeavy(pr);
        
        if (propWL.getCeCaseList() == null) {
            QueryCECase qp = null;
            qp = sc.initQuery(QueryCECaseEnum., cred);
            qp.getSearchParamsList().get(0).setProperty_val(pr);
            propWL.setCeCaseList(sc.runQuery(qp).getResults());
        }
        if (propWL.getUnitWithListsList() == null) {
            propWL.setUnitWithListsList(new ArrayList<PropertyUnitDataHeavy>());
            // since it was empty
        }
        
        if (propWL.getPersonList() == null) {
            propWL.setPersonList(new ArrayList<Person>());
        }
        if (propWL.getInfoCaseList() == null) {
            propWL.setPropInfoCaseList(new ArrayList<CECaseDataHeavy>());
        }
        if (propWL.getChangeList() == null) {
            propWL.setChangeList(new ArrayList<PropertyUnitChangeOrder>());
        }
        if (propWL.getBlobList() == null) {
            propWL.setBlobList(new ArrayList<Integer>());
        }

        // add a unit number -1 to any PropertyWithoutAnyUnits
        System.out.println("PropertyCoordinator.assemblePropertyDataheavy()");
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
     * Logic container for checking the existence of a property info case on a given property and 
     * if none exists creating one.
     * @param p
     * @param cred
     * @return 
     */
    public PropertyDataHeavy configurePDHInfoCase(PropertyDataHeavy p, Credential cred){
        CaseCoordinator cc = getCaseCoordinator();
        UserCoordinator uc = getUserCoordinator();
        CECase cse = null;
        try {
            cse = cc.initCECase(p, uc.getUser(cred.getGoverningAuthPeriod().getUserID()));
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        try {
            //review all case mems and set app ones for info case
            cc.insertNewCECase(cse, cred, null);
        } catch (IntegrationException | BObStatusException | ViolationException | EventException ex) {
            System.out.println(ex);
        }
        return p;
    }
    
    public int addProperty(Property prop, UserAuthorized ua) throws IntegrationException{
        PropertyIntegrator pi = getPropertyIntegrator();
        SystemIntegrator si = getSystemIntegrator();
        SystemCoordinator sc = getSystemCoordinator();
        
        prop.setLastUpdatedBy(ua);
        prop.setBobSource(  si.getBOBSource(
                                Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                                .getString("bobsourcePropertyInternal"))));
        prop.setNotes(sc.formatAndAppendNote(   ua, 
                                                "Property created with signature: " + ua.getMyCredential().getSignature(), 
                                                prop.getNotes()));
        
        return pi.insertProperty(prop);
        
        
        
    }
    
    
    public void editProperty(Property prop, UserAuthorized ua) throws IntegrationException{
        PropertyIntegrator pi = getPropertyIntegrator();
        pi.updateProperty(prop);
        
        
    }
    
    
    /**
     * Adapter method for folks who need a PropertyDataHeavy but who only have an
     * int propertyID; first it gets a simple property from the ID and then
     * beefs it up to a PropertyDataHeavy
     * @param propID
     * @param cred
     * @return
     * @throws IntegrationException
     * @throws BObStatusException
     * @throws AuthorizationException
     * @throws EventException 
     */
    public PropertyDataHeavy getPropertyDataHeavy(int propID, Credential cred) throws IntegrationException, BObStatusException, AuthorizationException, EventException{
        return assemblePropertyDataHeavy(getProperty(propID), cred);
    }
    
    
    
    /**
     * Adapter method for folks who need a PropertyDataHeavy but who only have an
     * int propertyID; first it gets a simple property from the ID and then
     * beefs it up to a PropertyDataHeavy
     * @param propUnitID
     * @param cred
     * @return
     * @throws IntegrationException
     * @throws BObStatusException
     * @throws AuthorizationException
     * @throws EventException 
     */
    public PropertyDataHeavy getPropertyDataHeavyByUnit(int propUnitID, Credential cred) throws IntegrationException, BObStatusException, AuthorizationException, EventException{
        PropertyIntegrator pi = getPropertyIntegrator();
        return assemblePropertyDataHeavy(pi.getPropertyUnitWithProp(propUnitID).getProperty(), cred);
    }
    
    
    /**
     * Logic intervention method for retrievals of simple properties from the DB
     * @param propID
     * @return
     * @throws IntegrationException 
     */
    public Property getProperty(int propID) throws IntegrationException{
        PropertyIntegrator pi = getPropertyIntegrator();
        return configureProperty(pi.getProperty(propID));
        
    }
    
    public PropertyUnit getPropertyUnit(int unitID) throws IntegrationException{
        PropertyIntegrator pi = getPropertyIntegrator();
        return pi.getPropertyUnit(unitID);
        
    }
    
    public Property getPropertyByPropUnit(int unitID) throws IntegrationException{
        PropertyIntegrator pi = getPropertyIntegrator();
        return pi.getPropertyUnitWithProp(unitID).getProperty();
    }
    
    
    /**
     * Implements a cascade of logic to determine a best suited startup property
     * for a given session. When a user has no Property history, it extracts
     * the office property of the municipality into which a user is switching
     * 
     * ecd DEC-19
     * 
     * @param cred
     * @return 
     */
    public PropertyDataHeavy selectDefaultProperty(Credential cred){
        
        PropertyIntegrator pi = getPropertyIntegrator();
        MunicipalityCoordinator mc = getMuniCoordinator();
        PropertyCoordinator pc = getPropertyCoordinator();
        
        if(cred != null){
            try {
                MunicipalityDataHeavy mdh = mc.assembleMuniDataHeavy(cred.getGoverningAuthPeriod().getMuni(), cred);
                    if(mdh.getMuniOfficePropertyId() !=0){
                        return pc.assemblePropertyDataHeavy(pc.getProperty(mdh.getMuniOfficePropertyId()), cred);
                    } 
            } catch (IntegrationException | AuthorizationException | BObStatusException | EventException ex) {
                System.out.println(ex);
            }
        }
        return  null;
    }
    
    
    /**
     * 
     * @param cred
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
    
    public Property initProperty(Municipality muni){
        Property prop = new Property();
        prop.setMuni(muni);
        return prop;
    }
    
    /**
     * This method generates a skeleton PropertyUnit with logical, preset defaults, including
     * empty lists.
     * @param p
     * @return 
     */
    public PropertyUnit initPropertyUnit(Property p){
        PropertyUnit propUnit = new PropertyUnit();
        propUnit.setPropertyID(p.getPropertyID());
        propUnit.setUnitNumber(Constants.DEFAULT_UNIT_NUMBER);
//        propUnit.setRental(DEFAULTRENTAL);
//        propUnit.setPropertyUnitPersonList(new ArrayList<Person>());
        return propUnit;
    }
    
   
}
