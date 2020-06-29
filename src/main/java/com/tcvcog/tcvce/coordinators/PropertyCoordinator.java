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
import com.tcvcog.tcvce.entities.PropertyUnitWithProp;
import com.tcvcog.tcvce.entities.PropertyUseType;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.search.QueryCECase;
import com.tcvcog.tcvce.entities.search.QueryCECaseEnum;
import com.tcvcog.tcvce.entities.search.QueryPerson;
import com.tcvcog.tcvce.entities.search.QueryPersonEnum;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Dominic Pimpinella, MaRoSco, Ellen Bascomb (31Y)
 */
public class PropertyCoordinator extends BackingBeanUtils implements Serializable {

    private final String DEFAULTUNITNUMBER = "-1";
    private final boolean DEFAULTRENTAL = false;

    /**
     * Logic container for initializing members on the Property subclass
     * PropertyWithLists
     *
     * @param prop
     * @param cred
     * @return
     * @throws IntegrationException
     * @throws BObStatusException
     * @throws com.tcvcog.tcvce.domain.SearchException
     */
    public PropertyDataHeavy assemblePropertyDataHeavy(Property prop, Credential cred) throws IntegrationException, BObStatusException, SearchException {

        SearchCoordinator sc = getSearchCoordinator();
        CaseCoordinator cc = getCaseCoordinator();

        PropertyDataHeavy pdh = new PropertyDataHeavy(prop);

        try {
            // CECase list
            QueryCECase qcse = sc.initQuery(QueryCECaseEnum.PROPERTY, cred);
            qcse.getPrimaryParams().setProperty_val(prop);
            pdh.setCeCaseList(sc.runQuery(qcse).getResults());

            // Property info cases
            qcse = sc.initQuery(QueryCECaseEnum.PROPINFOCASES, cred);
            qcse.getPrimaryParams().setProperty_val(prop);
            pdh.setPropInfoCaseList(cc.getCECaseHeavyList(sc.runQuery(qcse).getBOBResultList(), cred));

            // UnitDataHeavy list
            // remember that units data heavy contain all our occ periods and inspections
            if (pdh.getUnitList() != null && !pdh.getUnitList().isEmpty()) {
                pdh.setUnitWithListsList(getPropertyUnitWithListsList(pdh.getUnitList(), cred));
            }

            // Person list
            QueryPerson qp = sc.initQuery(QueryPersonEnum.PROPERTY_PERSONS, cred);
            qp.getPrimaryParams().setProperty_val(prop);
            pdh.setPersonList(sc.runQuery(qp).getBOBResultList());

            // change order list
            //delay this
//            pdh.setChangeList(pi.getPropertyUnitChangeListAll(pr));
            // wait on blobs
            //pdh.setBlobList(new ArrayList<Integer>());
        } catch (EventException | AuthorizationException ex) {
            System.out.println(ex);
            System.out.println();
        }
        return pdh;
    }

    /**
     * Logic pass through for acquiring a PropertyUnitWithProp for OccPeriods
     * and such that need a property address but only have a unit ID on them
     *
     * @param unitid
     * @return
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public PropertyUnitWithProp getPropertyUnitWithProp(int unitid) throws IntegrationException {
        PropertyIntegrator pi = getPropertyIntegrator();
        return pi.getPropertyUnitWithProp(unitid);

    }

    /**
     *
     * @param propUnitList
     * @param cred
     * @return
     * @throws IntegrationException
     * @throws EventException
     * @throws com.tcvcog.tcvce.domain.AuthorizationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public List<PropertyUnitDataHeavy> getPropertyUnitWithListsList(List<PropertyUnit> propUnitList, Credential cred) throws IntegrationException, EventException, EventException, AuthorizationException, BObStatusException {
        List<PropertyUnitDataHeavy> puwll = new ArrayList<>();
        PropertyIntegrator pi = getPropertyIntegrator();
        Iterator<PropertyUnit> iter = propUnitList.iterator();
        while (iter.hasNext()) {
            try {
                PropertyUnit pu = iter.next();
                puwll.add(configurePropertyUnitDataHeavy(pi.getPropertyUnitWithLists(pu.getUnitID()), cred));
            } catch (ViolationException ex) {
                System.out.println(ex);
            }
        }
        return puwll;
    }

    /**
     * Logic container for Property setting configuration
     *
     * @param p
     * @return
     */
    public Property configureProperty(Property p) {
        if (p.getUnitList() == null) {

            p.setUnitList(new ArrayList<PropertyUnit>());

        }

        return p;
    }

    public LocalDateTime configureDateTime(Date date) {
        return new java.sql.Timestamp(date.getTime()).toLocalDateTime();
    }

    /**
     * Logic container for checking and setting properties on
     * PropertyUnitDataHeavy objects
     *
     * @param pudh
     * @param cred
     * @return
     */
    public PropertyUnitDataHeavy configurePropertyUnitDataHeavy(PropertyUnitDataHeavy pudh, Credential cred) {
        pudh.setCredentialSignature(cred.getSignature());

        return pudh;

    }
    
    /**
     * Logic container for choosing a property info case
     * @param pdh
     * @return 
     */
    public CECase determineGoverningPropertyInfoCase(PropertyDataHeavy pdh){
        CECaseDataHeavy chosenCECase = null;
        List<CECaseDataHeavy> cseList = pdh.getPropInfoCaseList();
        if(cseList != null && !cseList.isEmpty()){
            Collections.sort(cseList);
            chosenCECase = cseList.get(0);
        }
        return chosenCECase;
    }
    
    
    
    /**
     * Logic container for checking the existence of a property info case on a
     * given property and if none exists creating one.
     *
     * @param p
     * @param ua
     * @return 
     */
    public PropertyDataHeavy configurePDHInfoCase(PropertyDataHeavy p, UserAuthorized ua){
        CaseCoordinator cc = getCaseCoordinator();
        UserCoordinator uc = getUserCoordinator();
        CECase cse = null;
        try {
            cse = cc.initCECase(p, uc.getUser(ua.getKeyCard().getGoverningAuthPeriod().getUserID()));
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        //review all case mems and set app ones for info case
        try {
            cc.insertNewCECase(cse, ua, null);
        } catch (IntegrationException | BObStatusException | EventException | ViolationException ex) {
            System.out.println(ex);
        }
        return p;
    }

    public int addProperty(Property prop, UserAuthorized ua) throws IntegrationException {
        PropertyIntegrator pi = getPropertyIntegrator();
        SystemIntegrator si = getSystemIntegrator();
        SystemCoordinator sc = getSystemCoordinator();

        prop.setLastUpdatedBy(ua);
        prop.setBobSource(si.getBOBSource(
                Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                        .getString("bobsourcePropertyInternal"))));
        prop.setNotes(sc.formatAndAppendNote(ua,
                ua.getMyCredential(),
                "Property created",
                prop.getNotes()));
        // this controller class passes the new property to insert
        // over to the data model to be written into the Database
        return pi.insertProperty(prop);

    }

    /**
     * Logic container for property updates
     *
     * @param prop
     * @param ua
     * @throws IntegrationException
     */
    public void editProperty(Property prop, UserAuthorized ua) throws IntegrationException {
        PropertyIntegrator pi = getPropertyIntegrator();
        prop.setLastUpdatedBy(ua);
        prop.setLastUpdatedTS(LocalDateTime.now());
//        prop.setAbandonedDateStart(LocalDateTime.parse(prop.getAbandonedDateStart().toString()));
//        prop.setAbandonedDateStop(LocalDateTime.parse(prop.getAbandonedDateStop().toString()));
//        prop.setVacantDateStart(LocalDateTime.parse(prop.getVacantDateStart().toString()));
//        prop.setVacantDateStop(LocalDateTime.parse(prop.getVacantDateStop().toString()));
//        prop.setUnfitDateStart(LocalDateTime.parse(prop.getUnfitDateStart().toString()));
//        prop.setUnfitDateStop(LocalDateTime.parse(prop.getUnfitDateStop().toString()));
        if (checkAllDates(prop) == false) {

        }
        pi.updateProperty(prop);

    }

    public boolean checkAllDates(Property prop) {
        boolean unfit, vacant, abandoned;
        unfit = checkStartDTisBeforeEndDT(prop.getUnfitDateStart(), prop.getUnfitDateStop());
        vacant = checkStartDTisBeforeEndDT(prop.getVacantDateStart(), prop.getVacantDateStop());
        abandoned = checkStartDTisBeforeEndDT(prop.getAbandonedDateStart(), prop.getAbandonedDateStop());
        if (unfit && vacant && abandoned) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkStartDTisBeforeEndDT(LocalDateTime start, LocalDateTime end) {

        if (start == null) {
            return end == null; //If start is null, then end must also be null. Otherwise we have a end without a beginning!
            
        } else if (end != null) { //If start is not null and end is not null, then we can run the following code block
            if (end.isBefore(start) || end.equals(start)) {
                return false;
            } else {
                return true;
            }
        }
        
        return true; //If start is not null, but end is null, no check is necessary. Also, checking would cause a null pointer error.
    }

    /**
     * Adapter method for folks who need a PropertyDataHeavy but who only have
     * an int propertyID; first it gets a simple property from the ID and then
     * beefs it up to a PropertyDataHeavy
     *
     * @param propID
     * @param cred
     * @return
     * @throws IntegrationException
     * @throws BObStatusException
     * @throws AuthorizationException
     * @throws EventException
     * @throws com.tcvcog.tcvce.domain.SearchException
     */
    public PropertyDataHeavy getPropertyDataHeavy(int propID, Credential cred) throws IntegrationException, BObStatusException, AuthorizationException, EventException, SearchException {
        return assemblePropertyDataHeavy(getProperty(propID), cred);
    }

    /**
     * Adapter method for folks who need a PropertyDataHeavy but who only have
     * an int propertyID; first it gets a simple property from the ID and then
     * beefs it up to a PropertyDataHeavy
     *
     * @param propUnitID
     * @param cred
     * @return
     * @throws IntegrationException
     * @throws BObStatusException
     * @throws AuthorizationException
     * @throws EventException
     * @throws com.tcvcog.tcvce.domain.SearchException
     */
    public PropertyDataHeavy getPropertyDataHeavyByUnit(int propUnitID, Credential cred) throws IntegrationException, BObStatusException, AuthorizationException, EventException, SearchException {
        PropertyIntegrator pi = getPropertyIntegrator();
        return assemblePropertyDataHeavy(pi.getPropertyUnitWithProp(propUnitID).getProperty(), cred);
    }

    /**
     * Logic intervention method for retrievals of simple properties from the DB
     *
     * @param propID
     * @return
     * @throws IntegrationException
     */
    public Property getProperty(int propID) throws IntegrationException {
        PropertyIntegrator pi = getPropertyIntegrator();
        return configureProperty(pi.getProperty(propID));

    }
    
    /**
     * Logic intermediary for retrieval of a PropertyUnit by id
     * @param unitID
     * @return
     * @throws IntegrationException 
     */
    public PropertyUnit getPropertyUnit(int unitID) throws IntegrationException{
        PropertyIntegrator pi = getPropertyIntegrator();
        return pi.getPropertyUnit(unitID);

    }
    
    /**
     * Logic intermediary for extracting a complete list of PropertyUseType
     * objects from the DB
     * @return 
     */
    public List<PropertyUseType> getPropertyUseTypeList(){
        PropertyIntegrator pi = getPropertyIntegrator();
        List<PropertyUseType> putList = null;
        try {
            putList = pi.getPropertyUseTypeList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
        return putList;
    }
    

    public Property getPropertyByPropUnitID(int unitID) throws IntegrationException {
        PropertyIntegrator pi = getPropertyIntegrator();
        return pi.getPropertyUnitWithProp(unitID).getProperty();
    }
    /**
     * Implements a cascade of logic to determine a best suited startup property
     * for a given session. When a user has no Property history, it extracts the
     * office property of the municipality into which a user is switching
     *
     * ecd DEC-19
     *
     * @param cred
     * @return
     */
    public PropertyDataHeavy selectDefaultProperty(Credential cred) {

        PropertyIntegrator pi = getPropertyIntegrator();
        MunicipalityCoordinator mc = getMuniCoordinator();
        PropertyCoordinator pc = getPropertyCoordinator();

        if (cred != null) {
            try {
                MunicipalityDataHeavy mdh = mc.assembleMuniDataHeavy(cred.getGoverningAuthPeriod().getMuni(), cred);
                if (mdh.getMuniOfficePropertyId() != 0) {
                    return pc.assemblePropertyDataHeavy(pc.getProperty(mdh.getMuniOfficePropertyId()), cred);
                }
            } catch (IntegrationException | AuthorizationException | BObStatusException | EventException | SearchException ex) {
                System.out.println(ex);
            }
        }
        return null;
    }

    /**
     *
     * @param cred
     * @return
     */
    public List<Property> assemblePropertyHistoryList(Credential cred) {
        PropertyIntegrator pi = getPropertyIntegrator();
        List<Property> propList = new ArrayList<>();
        List<Integer> propIDList = new ArrayList<>();

        if (cred != null) {
            try {
                propIDList.addAll(pi.getPropertyHistoryList(cred));
                while (!propIDList.isEmpty() && propIDList.size() <= Constants.MAX_BOB_HISTORY_SIZE) {
                    // Only developers get a heterogeneous mix of muni in their history
                    if (cred.isHasDeveloperPermissions()) {
                        propList.add(pi.getProperty(propIDList.remove(0)));
                    } else {
                        Municipality m = cred.getGoverningAuthPeriod().getMuni();
                        for (Property pr : propList) {
                            if (pr.getMuniCode() == m.getMuniCode()) {
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

    public Property initProperty(Municipality muni) {
        Property prop = new Property();
        prop.setMuni(muni);
        return prop;
    }

    /**
     * This method generates a skeleton PropertyUnit with logical, preset
     * defaults, including empty lists.
     *
     * @param p
     * @return
     */
    public PropertyUnit initPropertyUnit(Property p) {
        PropertyUnit propUnit = new PropertyUnit();
        propUnit.setPropertyID(p.getPropertyID());
        propUnit.setUnitNumber(Constants.TEMP_UNIT_NUM);
//        propUnit.setRental(false);
        propUnit.setActive(true);
        propUnit.setNotes("");
//        propUnit.setPropertyUnitPersonList(new ArrayList<Person>());
        return propUnit;
    }

}
