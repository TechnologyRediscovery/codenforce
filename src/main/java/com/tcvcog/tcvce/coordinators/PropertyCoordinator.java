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
import com.tcvcog.tcvce.entities.PropertyUnitDataHeavy;
import com.tcvcog.tcvce.entities.PropertyDataHeavy;
import com.tcvcog.tcvce.entities.PropertyExtData;
import com.tcvcog.tcvce.entities.PropertyUnitChangeOrder;
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
import com.tcvcog.tcvce.util.MessageBuilderParams;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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

        PropertyDataHeavy pdh = new PropertyDataHeavy(getProperty(prop.getPropertyID()));

        try {
            // CECase list
            QueryCECase qcse = sc.initQuery(QueryCECaseEnum.PROPERTY, ua.getKeyCard());
            qcse.getPrimaryParams().setProperty_val(prop);
            pdh.setCeCaseList(sc.runQuery(qcse).getResults());

            // Property info cases
            qcse = sc.initQuery(QueryCECaseEnum.PROPINFOCASES, ua.getKeyCard());
            qcse.getPrimaryParams().setProperty_val(prop);
            pdh.setPropInfoCaseList(cc.cecase_assembleCECaseDataHeavyList(sc.runQuery(qcse).getBOBResultList(), ua));

            // check list and see if it's emtpy; 
            if (pdh.getPropInfoCaseList() == null) {
                pdh.setPropInfoCaseList(new ArrayList<CECaseDataHeavy>());
            }

            if (pdh.getPropInfoCaseList().isEmpty()) {
                pdh.getPropInfoCaseList().add(createPropertyInfoCase(pdh, ua));
            }

            // UnitDataHeavy list
            // remember that units data heavy contain all our occ periods, inspections, and PropertyUnitChangeOrders
            if (pdh.getUnitList() != null && !pdh.getUnitList().isEmpty()) {
                pdh.setUnitWithListsList(getPropertyUnitWithListsList(pdh.getUnitList(), ua));
            }

            // Person list
            QueryPerson qp = sc.initQuery(QueryPersonEnum.PROPERTY_PERSONS, ua.getKeyCard());
            qp.getPrimaryParams().setProperty_val(prop);
            pdh.setPersonList(sc.runQuery(qp).getBOBResultList());
            System.out.println("PropertyCoordinator.assemblePropertyDH: personlist size: " + pdh.getPersonList().size());

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
     *
     * @param extIDList
     * @return
     * @throws IntegrationException
     */
    private List<PropertyExtData> fetchExternalDataRecords(List<Integer> extIDList) throws IntegrationException {
        PropertyIntegrator pi = getPropertyIntegrator();
        List<PropertyExtData> extList = new ArrayList<>();
        if (extIDList != null && !extIDList.isEmpty()) {
            for (Integer i : extIDList) {
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

    /**
     * Logic container for checking requests to connect a person to a property
     * using a linkage table. The actual DB interaction is delegated to the
     * PersonCoordinator who may check their own stuff
     *
     * @param pdh
     * @param pers
     * @throws IntegrationException
     * @throws BObStatusException
     */
    public void connectPersonToProperty(PropertyDataHeavy pdh, Person pers) throws IntegrationException, BObStatusException {
        boolean proceedWithConnect = true;
        PersonCoordinator pc = getPersonCoordinator();
        if (pdh != null && pers != null) {
            for (Person p : pdh.getPersonList()) {
                if (p.getPersonID() == pers.getPersonID()) {
                    proceedWithConnect = false;
                }
            }
        }
        if (proceedWithConnect) {
            pc.connectPersonToProperty(pers, pdh);
        } else {
            throw new BObStatusException("Person Link Already Exists");
        }

    }

    /**
     * Logic pass through for removals of property and person connections and I
     * create a note documenting the removal and post to property
     *
     * @param p
     * @param pers
     * @param ua the user requesting the link removal for note
     * @throws IntegrationException
     * @throws BObStatusException
     */
    public void connectRemovePersonToProperty(Property p, Person pers, UserAuthorized ua) throws IntegrationException, BObStatusException {
        PersonCoordinator pc = getPersonCoordinator();
        PropertyIntegrator pi = getPropertyIntegrator();
        SystemCoordinator sc = getSystemCoordinator();

        pc.connectRemovePersonToProperty(pers, p);

        // build person link removal note
        MessageBuilderParams mbp = new MessageBuilderParams();
        mbp.setUser(ua);
        StringBuilder sb = new StringBuilder();
        sb.append("Removal of link between Property at ");
        sb.append(p.getAddress());
        sb.append(" in ");
        sb.append(p.getMuni().getMuniName());
        sb.append(" (ID:");
        sb.append(p.getPropertyID());
        sb.append(")");
        sb.append(" and Person ");
        sb.append(pers.getFirstName());
        sb.append(" ");
        sb.append(pers.getLastName());
        sb.append(" (ID:");
        sb.append(pers.getPersonID());
        sb.append(") ");
        mbp.setNewMessageContent(sb.toString());
        mbp.setExistingContent(p.getNotes());
        mbp.setIncludeCredentialSig(false);
        p.setNotes(sc.appendNoteBlock(mbp));
        // commit person link removal note
        pi.updateProperty(p);

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
     * @param ua
     * @return
     * @throws com.tcvcog.tcvce.domain.SearchException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public CECase determineGoverningPropertyInfoCase(PropertyDataHeavy pdh, UserAuthorized ua) throws SearchException, IntegrationException {
        CECaseDataHeavy chosenCECase = null;
        List<CECaseDataHeavy> cseList = pdh.getPropInfoCaseList();
        if (cseList != null && !cseList.isEmpty()) {
            Collections.sort(cseList);
            chosenCECase = cseList.get(0);
        } else {
            //There's no existing info case, let's make one.
            chosenCECase = createPropertyInfoCase(pdh, ua);
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
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public CECaseDataHeavy createPropertyInfoCase(PropertyDataHeavy p, UserAuthorized ua) throws SearchException, IntegrationException {
        CaseCoordinator cc = getCaseCoordinator();
        UserCoordinator uc = getUserCoordinator();
        CECase cse = null;
        CECaseDataHeavy csehv = null;
        if (p != null && ua != null) {
            cse = cc.cecase_initCECase(p, ua);
            //review all case mems and set app ones for info case
            if (cse != null) {
                try {
                    cse.setCaseManager(uc.user_getUser(ua.getMyCredential().getGoverningAuthPeriod().getUserID()));
                    cse.setPropertyInfoCase(true);
                    cse.setNotes("This is a Case object that contains information and events attached to " + p.getAddress() + ". "
                            + "This case does not represent an actual code enforcement case.");
                    cse.setCaseID(cc.cecase_insertNewCECase(cse, ua, null));
                    csehv = cc.cecase_assembleCECaseDataHeavy(cse, ua);

                } catch (IntegrationException | BObStatusException | EventException | ViolationException ex) {
                    System.out.println(ex);
                }
            }
        }

        return csehv;
    }

    /**
     * Primary pathway for the creation of new records in the property
     * table--the biggie!!
     *
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
     * @throws com.tcvcog.tcvce.domain.BObStatusException
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
     *
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

    /**
     * A method that takes a unit list and compares it to the database and
     * applies any changes (deactivations and insertions too) BUT BYPASSES THE
     * UNITCHANGEORDER WORKFLOW, so it should only be used internally.
     *
     * @param unitList the edited unit list we would like to compare with the
     * DB's list
     * @param prop the property we would like to compare it with. Does not use
     * the built in list, but fetches it from the DB
     * @return the new list grabbed from the Database!
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public List<PropertyUnit> applyUnitList(List<PropertyUnit> unitList, Property prop)
            throws BObStatusException, IntegrationException {

        PropertyIntegrator pi = getPropertyIntegrator();

        sanitizePropertyUnitList(unitList);

        for (PropertyUnit unit : unitList) {

            // decide if we're updating a unit or inserting it based on initial value
            // newly created units don't have an ID, just a default unit number
            unit.setPropertyID(prop.getPropertyID());

            if (unit.getUnitID() == 0) {
                pi.insertPropertyUnit(unit);

            } else {

                pi.updatePropertyUnit(unit);

            }
        }

        List<PropertyUnit> listTwo = pi.getPropertyUnitList(prop);

        prop.setUnitList(listTwo);

        // mark parent property as updated now
        editProperty(prop, getSessionBean().getSessUser());

        return listTwo;

    }

    /**
     * Implements an existing change order and update its corresponding property
     * unit, also deactivates and updates the change order to record who
     * approved the transaction
     *
     * @param uc
     * @throws IntegrationException
     */
    public void implementPropertyUnitChangeOrder(PropertyUnitChangeOrder uc) throws IntegrationException {

        PropertyIntegrator pi = getPropertyIntegrator();

        //If the user added the unit, their changes will already be in the database. No need to update
        if (!uc.isAdded()) {

            PropertyUnit skeleton = getPropertyUnit(uc.getUnitID());

            if (uc.isRemoved()) {
                skeleton.setActive(false); //just deactivate the unit.
            } else {
                if (uc.getUnitNumber() != null) {
                    skeleton.setUnitNumber(uc.getUnitNumber());
                }

                /* TODO: What is this?
                if (uc.getOtherKnownAddress() != null) {
                    skeleton.setOtherKnownAddress("Updated");
                } else {
                    skeleton.setOtherKnownAddress("Updated");
                }*/
                if (uc.getOtherKnownAddress() != null) {
                    skeleton.setOtherKnownAddress(uc.getOtherKnownAddress());
                }

                if (uc.getNotes() != null) {
                    skeleton.setNotes(uc.getNotes());
                }

                if (uc.getRentalNotes() != null) {
                    skeleton.setRentalNotes(uc.getRentalNotes());
                }
            }

            pi.updatePropertyUnit(skeleton);
        }

        //Time to update the change order
        uc.setActive(false);
        uc.setApprovedOn(Timestamp.valueOf(LocalDateTime.now()));
        uc.setApprovedBy(getSessionBean().getSessUser());

        pi.updatePropertyUnitChange(uc);

    }
}
