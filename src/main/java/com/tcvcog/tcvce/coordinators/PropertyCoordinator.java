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
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyUnitDataHeavy;
import com.tcvcog.tcvce.entities.PropertyDataHeavy;
import com.tcvcog.tcvce.entities.PropertyExtData;
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
     * @param ua
     * @return
     * @throws IntegrationException
     * @throws BObStatusException
     * @throws com.tcvcog.tcvce.domain.SearchException
     */
    public PropertyDataHeavy assemblePropertyDataHeavy(Property prop, UserAuthorized ua) throws IntegrationException, BObStatusException, SearchException {

        SearchCoordinator sc = getSearchCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
        PropertyIntegrator pi = getPropertyIntegrator();

        PropertyDataHeavy pdh = new PropertyDataHeavy(prop);

        try {
            // CECase list
            QueryCECase qcse = sc.initQuery(QueryCECaseEnum.PROPERTY, ua.getKeyCard());
            qcse.getPrimaryParams().setProperty_val(prop);
            pdh.setCeCaseList(sc.runQuery(qcse).getResults());

            // Property info cases
            qcse = sc.initQuery(QueryCECaseEnum.PROPINFOCASES, ua.getKeyCard());
            qcse.getPrimaryParams().setProperty_val(prop);
            pdh.setPropInfoCaseList(cc.assembleCECaseDataHeavyList(sc.runQuery(qcse).getBOBResultList(), ua));
            
            // check list and see if it's emtpy; 
            if(pdh.getPropInfoCaseList() == null){
                pdh.setPropInfoCaseList(new ArrayList<CECaseDataHeavy>());
            }
                
            if(pdh.getPropInfoCaseList().isEmpty()){
                createPropertyInfoCase(pdh, ua);
            }
            

            // UnitDataHeavy list
            // remember that units data heavy contain all our occ periods and inspections
            if (pdh.getUnitList() != null && !pdh.getUnitList().isEmpty()) {
                pdh.setUnitWithListsList(getPropertyUnitWithListsList(pdh.getUnitList(), ua));
            }

            // Person list
            QueryPerson qp = sc.initQuery(QueryPersonEnum.PROPERTY_PERSONS, ua.getKeyCard());
            qp.getPrimaryParams().setProperty_val(prop);
            pdh.setPersonList(sc.runQuery(qp).getBOBResultList());

            // change order list
            //delay this
//            pdh.setChangeList(pi.getPropertyUnitChangeListAll(pr));
            // wait on blobs
            //pdh.setBlobList(new ArrayList<Integer>());
            
            // external data
            pdh.setExtDataList(fetchExternalDataRecords(pi.getPropertyExternalDataRecordIDs(pdh.getPropertyID())));
            
        } catch (EventException | AuthorizationException ex) {
            System.out.println(ex);
            System.out.println();
        }
        return pdh;
    }
    
    /**
     * Utility method for calling the integrator method that creates a single
     * external data record given a list of record IDs
     * @param extIDList
     * @return
     * @throws IntegrationException 
     */
    private List<PropertyExtData> fetchExternalDataRecords(List<Integer> extIDList) throws IntegrationException{
        PropertyIntegrator pi = getPropertyIntegrator();
        List<PropertyExtData> extList = new ArrayList<>();
        if(extIDList != null && !extIDList.isEmpty()){
            for(Integer i: extIDList){
               extList.add(pi.getPropertyExternalDataRecord(i));
            }
        }
        return extList;
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
     * @param ua
     * @return
     * @throws IntegrationException
     * @throws EventException
     * @throws com.tcvcog.tcvce.domain.AuthorizationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public List<PropertyUnitDataHeavy> getPropertyUnitWithListsList(List<PropertyUnit> propUnitList, UserAuthorized ua) throws IntegrationException, EventException, EventException, AuthorizationException, BObStatusException {
        List<PropertyUnitDataHeavy> puwll = new ArrayList<>();
        PropertyIntegrator pi = getPropertyIntegrator();
        Iterator<PropertyUnit> iter = propUnitList.iterator();
        while (iter.hasNext()) {
            try {
                PropertyUnit pu = iter.next();
                puwll.add(configurePropertyUnitDataHeavy(pi.getPropertyUnitWithLists(pu.getUnitID()), ua.getKeyCard()));
            } catch (ViolationException ex) {
                System.out.println(ex);
            }
        }
        return puwll;
    }

    /**
     *
     * @param propUnit
     * @param cred
     * @return
     * @throws IntegrationException
     * @throws EventException
     * @throws com.tcvcog.tcvce.domain.AuthorizationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public PropertyUnitDataHeavy getPropertyUnitWithLists(PropertyUnit propUnit, Credential cred) throws IntegrationException, EventException, EventException, AuthorizationException, BObStatusException {
        PropertyIntegrator pi = getPropertyIntegrator();
        try {
            return configurePropertyUnitDataHeavy(pi.getPropertyUnitWithLists(propUnit.getUnitID()), cred);
        } catch (ViolationException ex) {
            System.out.println(ex);
        }
        return new PropertyUnitDataHeavy(propUnit); //just in case something goes horribly wrong
    }

    /**
     * Accepts a List of PropertyUnit objects, validates the input to make sure
     * it is acceptable, sanitizes it, then tosses it back.
     *
     * @param input
     * @return
     * @throws BObStatusException if the input is not acceptable.
     */
    public List<PropertyUnit> sanitizePropertyUnitList(List<PropertyUnit> input) throws BObStatusException {

        int duplicateNums; //The above boolean is a flag to see if there is more than 1 of  Unit Number. The int to the left stores how many of a given number the loop below finds.

        if (input.isEmpty()) {
            throw new BObStatusException("Please add at least one unit.");
        }

        //use a numeric for loop instead of iterating through the objects so that we can store 
        //the sanitized units
        for (int index = 0; index < input.size(); index++) {
            duplicateNums = 0;

            for (PropertyUnit secondUnit : input) {
                if (input.get(index).getUnitNumber().compareTo(secondUnit.getUnitNumber()) == 0) {
                    duplicateNums++;
                }
            }

            if (duplicateNums > 1) {
                throw new BObStatusException("Some Units have the same Number");

            }

            input.set(index, sanitizePropertyUnit(input.get(index)));
        }

        return input;

    }

    /**
     * Accepts a PropertyUnit object, validates the input to make sure it is
     * acceptable, sanitizes it, then tosses it back.
     *
     * @param input
     * @return
     * @throws BObStatusException if the input is not acceptable.
     */
    public PropertyUnit sanitizePropertyUnit(PropertyUnit input) throws BObStatusException {

        input.setUnitNumber(input.getUnitNumber().replaceAll("(?i)unit", ""));

        if (input.getUnitNumber().compareTo("") == 0) {
            throw new BObStatusException("All units must have a Unit Number");
        }

        if (input.getUnitNumber().compareTo("-1") == 0) {
            if (input.getNotes() == null || input.getNotes().compareTo("robot-generated unit representing the primary habitable dwelling on a property") != 0) {
                throw new BObStatusException("The unit number -1 is used for default property units. Please use another number or \'-[space]1\'.");
            }
            throw new BObStatusException("Please change the robot-generated unit to something more meaningful.");

        }

        return input;

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
     *
     * @param pdh
     * @return
     */
    public CECase determineGoverningPropertyInfoCase(PropertyDataHeavy pdh) {
        CECaseDataHeavy chosenCECase = null;
        List<CECaseDataHeavy> cseList = pdh.getPropInfoCaseList();
        if (cseList != null && !cseList.isEmpty()) {
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
     * @throws com.tcvcog.tcvce.domain.SearchException 
     */
    public PropertyDataHeavy createPropertyInfoCase(PropertyDataHeavy p, UserAuthorized ua) throws SearchException{
        CaseCoordinator cc = getCaseCoordinator();
        UserCoordinator uc = getUserCoordinator();
        CECase cse = cc.initCECase(p, ua);
        //review all case mems and set app ones for info case
        
//        try {
//            cse.setCaseManager(uc.getUser(ua.getMyCredential().getGoverningAuthPeriod().getUserID()));
// TODO: Debug later
//            cc.insertNewCECase(cse, ua, null);
//        } catch (IntegrationException | BObStatusException | EventException | ViolationException ex) {
//            System.out.println(ex);
//        }
        if(p.getPropInfoCaseList() == null){
            p.setPropInfoCaseList(new ArrayList<CECaseDataHeavy>());
        }
        
            
        return p;
    }

    /**
     * Primary pathway for the creation of new records in the property table--the biggie!!
     * @param prop
     * @param ua
     * @return
     * @throws IntegrationException 
     */
    
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
    public void editProperty(Property prop, UserAuthorized ua) throws IntegrationException, BObStatusException {
        PropertyIntegrator pi = getPropertyIntegrator();
        prop.setLastUpdatedBy(ua);
        prop.setLastUpdatedTS(LocalDateTime.now());
        if (checkAllDates(prop) == false) {
            throw new BObStatusException("Date error in committing property updates; Ensure no end date is before a start date");
            
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
     * @param ua
     * @return
     * @throws IntegrationException
     * @throws BObStatusException
     * @throws AuthorizationException
     * @throws EventException
     * @throws com.tcvcog.tcvce.domain.SearchException
     */
    public PropertyDataHeavy getPropertyDataHeavy(int propID, UserAuthorized ua) throws IntegrationException, BObStatusException, AuthorizationException, EventException, SearchException {
        return assemblePropertyDataHeavy(getProperty(propID), ua);
    }

    /**
     * Adapter method for folks who need a PropertyDataHeavy but who only have
     * an int propertyID; first it gets a simple property from the ID and then
     * beefs it up to a PropertyDataHeavy
     *
     * @param propUnitID
     * @param ua
     * @return
     * @throws IntegrationException
     * @throws BObStatusException
     * @throws AuthorizationException
     * @throws EventException
     * @throws com.tcvcog.tcvce.domain.SearchException
     */
    public PropertyDataHeavy getPropertyDataHeavyByUnit(int propUnitID, UserAuthorized ua) throws IntegrationException, BObStatusException, AuthorizationException, EventException, SearchException {
        PropertyIntegrator pi = getPropertyIntegrator();
        return assemblePropertyDataHeavy(pi.getPropertyUnitWithProp(propUnitID).getProperty(), ua);
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
     *
     * @param unitID
     * @return
     * @throws IntegrationException
     */
    public PropertyUnit getPropertyUnit(int unitID) throws IntegrationException {
        PropertyIntegrator pi = getPropertyIntegrator();
        return pi.getPropertyUnit(unitID);

    }

    /**
     * Logic intermediary for extracting a complete list of PropertyUseType
     * objects from the DB
     *
     * @return
     */
    public List<PropertyUseType> getPropertyUseTypeList() {
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
     * @param ua
     * @return
     */
    public PropertyDataHeavy selectDefaultProperty(UserAuthorized ua) {

        PropertyIntegrator pi = getPropertyIntegrator();
        MunicipalityCoordinator mc = getMuniCoordinator();
        PropertyCoordinator pc = getPropertyCoordinator();

        if (ua != null) {
            try {
                MunicipalityDataHeavy mdh = mc.assembleMuniDataHeavy(ua.getKeyCard().getGoverningAuthPeriod().getMuni(), ua);
                if (mdh.getMuniOfficePropertyId() != 0) {
                    return pc.assemblePropertyDataHeavy(pc.getProperty(mdh.getMuniOfficePropertyId()), ua);
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
     * Creates a skeleton of a property
     * @param muni
     * @return whose ID is 0
     */
    public PropertyDataHeavy initPropertyDataHeavy(Municipality muni) {
        Property prop = new Property();
        PropertyDataHeavy pdh = new PropertyDataHeavy(prop);
        
        prop.setMuni(muni);
        return pdh;
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
