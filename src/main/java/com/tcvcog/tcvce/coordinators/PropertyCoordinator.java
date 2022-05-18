/**
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
import com.tcvcog.tcvce.domain.*;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.entities.search.QueryCECase;
import com.tcvcog.tcvce.entities.search.QueryCECaseEnum;
import com.tcvcog.tcvce.integration.BlobIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Business logic container for All Things Property related
 * 
 * @author Dominic Pimpinella, MaRoSco, Ellen Bascomb (31Y)
 */
public class PropertyCoordinator extends BackingBeanUtils implements Serializable {

    final String DEFAULTUNITNUMBER = "-1";
    final boolean DEFAULTRENTAL = false;
    final static String SPACE = " ";
    final static String COMMA_SPACE = ", ";
    final static String HTML_BR = "<br /> ";
    final static String SEMICOLON_SPACE = "; ";
     /**
     * Creates a new instance of PropertyUnitCoordinator
     */
    public PropertyCoordinator() {
    }

    
    /**
     *  ***************************************************
     *  ***************************************************
     *  ************* MAILING ADDRESS CENTRAL *************
     *  ***************************************************
     *  ***************************************************
     */
    
    
    /**
     * Extracts linked MailingAddresses by implementer of the IFace_addressListHolder
     * which in March 2022 were Property and Person only
     * @param adlh
     * @return the MailingAddressLink list for injection
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public List<MailingAddressLink> getMailingAddressLinkList(IFace_addressListHolder adlh) throws BObStatusException, IntegrationException{
        if(adlh == null || adlh.getLinkedObjectSchemaEnum() == null){
            throw new BObStatusException("Cannot get AddressLinks with null list holder or schema");
        }
        PropertyIntegrator pi = getPropertyIntegrator();
        
        return pi.getMailingAddressLinks(adlh);
    }
    
    
    /**
     * Factory for maling address links
     * @param mad
     * @return 
     */
    public MailingAddressLink getMailingAddressLinkSkeleton(MailingAddress mad){
        
        MailingAddressLink madLink = new MailingAddressLink(mad);
        return madLink;
    }
    
    /**
     * Getter for MAD links
     * @param holder
     * @param linkID
     * @return the MAD link
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public MailingAddressLink getMailingAddressLink(IFace_addressListHolder holder, int linkID) throws IntegrationException, BObStatusException{
        PropertyIntegrator pi = getPropertyIntegrator();
        return pi.getMailingAddressLink(holder, linkID);
        
    }
    
    
    /**
     * Creates a new link between an implementer of our interface for addressLists
     * and any old Mailing address. Will inject a default role if null.
     * @param adlh target of link
     * @param madLink to link
     * @param ua
     * @return the linkID of the fresh link. You should also be able to get this link
     * just by calling getMailingAddressLinkList
     */
    public int linkToMailingAddress(IFace_addressListHolder adlh, MailingAddressLink madLink, UserAuthorized ua) throws BObStatusException, IntegrationException{
        if(adlh == null || madLink == null || ua == null){
            throw new BObStatusException("Cannot deactivate a mailing link with null link or UA or address");
        }
        PropertyIntegrator pi = getPropertyIntegrator();
        SystemIntegrator si =getSystemIntegrator();
        SystemCoordinator sc = getSystemCoordinator();
        
        
        if(madLink.getLinkedObjectRole() == null){
            
            if(adlh instanceof Property){
                madLink.setLinkRole(si.getLinkedObjectRole(
                        Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                                .getString("default_linkedobjectrole_parcel_mailing"))));
            } else if (adlh instanceof Person){
                madLink.setLinkRole(si.getLinkedObjectRole(
                        Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                                .getString("default_linkedobjectrole_human_mailing"))));
            }
        } 
        
        madLink.setSource(sc.getBObSource(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                            .getString("bobsourcePropertyInternal"))));
        
        madLink.setLinkCreatedByUserID(ua.getUserID());
        madLink.setLinkLastUpdatedByUserID(ua.getUserID());
        
        return pi.linkMailingAddress(adlh, madLink);
        
    }
    
    /**
     * Logic intermediary for updates to a mailing address link
     * @param madLink
     * @param ua 
     */
    public void updateMailingAddressLink(MailingAddressLink madLink, UserAuthorized ua) throws BObStatusException, IntegrationException{
        if(madLink == null || ua == null){
            throw new BObStatusException("Cannot deactivate a mailing link with null link or UA");
        }
        PropertyIntegrator pi = getPropertyIntegrator();
        
        madLink.setLinkLastUpdatedByUserID(ua.getUserID());
        
        pi.updateMailingAddressLink(madLink);
        
    }
    
    
    
    public void deactivateLinkToMailingAddress(MailingAddressLink madLink, UserAuthorized ua) throws BObStatusException, IntegrationException{
        if(madLink == null || ua == null){
            throw new BObStatusException("Cannot deactivate a mailing link with null link or UA");
        }
        PropertyIntegrator pi = getPropertyIntegrator();
        
        madLink.setLinkDeactivatedByUserID(ua.getUserID());
        madLink.setDeactivatedTS(LocalDateTime.now());
        madLink.setLinkLastUpdatedByUserID(ua.getUserID());
        
        pi.updateMailingAddressLink(madLink);
    }
    
    
    /**
     * Extracts the official ZIP codes for a given municipality
     * @param muni
     * @return the list of official Zips
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public List<MailingCityStateZip> getZipListByMunicipality(Municipality muni) throws IntegrationException, BObStatusException{
        PropertyIntegrator pi = getPropertyIntegrator();
        List<Integer> zipIDList = pi.getMailingCityStateZipListByMuni(muni);
        List<MailingCityStateZip> zipObList = new ArrayList<>();
        if(zipIDList != null && !zipIDList.isEmpty()){
            for(Integer i: zipIDList){
                zipObList.add(getMailingCityStateZip(i));
            }
        }
        return zipObList;
    }
    
    
    /**
     * Getter for MailingAddress objects
     * @param addressid the ID of the requested address, cannot be zero
     * @return the object
     */
    public MailingAddress getMailingAddress(int addressid) throws IntegrationException, BObStatusException {
        PropertyIntegrator pi = getPropertyIntegrator();
        return configureMailingAddress(pi.getMailingAddress(addressid));
        
    }
    
    /**
     * Internal logic for setting up fields on a mailing address
     * @param addr to configure
     * @return ref to the configured object
     */
    private MailingAddress configureMailingAddress(MailingAddress addr){
        addr.setAddressPretty1Line(buildPropertyAddressStrings(addr, false, false));
        addr.setAddressPretty2LineEscapeFalse(buildPropertyAddressStrings(addr, true, true));
        return addr;
    }
    
    /**
     * Getter for Streets
     * @param streetid the street ID
     * @return the object
     */
    public MailingStreet getMailingStreet(int streetid) throws IntegrationException, BObStatusException{
        PropertyIntegrator pi = getPropertyIntegrator();
        return pi.getMailingStreet(streetid);
        
    }
    
    /**
     * Factory for MailingStreet objects
     * @param csz to be injected into the new street, can be null
     * @return the empty MailingStreet with id = 0
     */
    public MailingStreet getMailingStreetSkeleton(MailingCityStateZip csz){
        MailingStreet ms = new MailingStreet();
        ms.setCityStateZip(csz);
        return ms;
    }
    
    /**
     * Factory method for address objects
     * @return 
     */
    public MailingAddress getMailingAddressSkeleton(){
        MailingAddress ma = new MailingAddress();
        ma.setStreet(new MailingStreet());
        return ma;
    }
    
    
    /**
     * Logic container for inserting a new MailingStreet
     * @param street
     * @param ua
     * @return
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    public int insertMailingStreet(MailingStreet street, UserAuthorized ua) throws BObStatusException, IntegrationException{
        if(street == null || street.getCityStateZip() == null || ua == null){
            throw new BObStatusException("Cannot insert street with null street, zip, or user");
        }
        
        PropertyIntegrator pi = getPropertyIntegrator();
        street.setCreatedBy(ua);
        street.setLastUpdatedBy(ua);
        return pi.insertMailingStreet(street);
        
    }
    
    
    /**
     * Logic intermediary for updating a MailingStreet
     * @param street
     * @param ua
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    public void updateMailingStreet(MailingStreet street, UserAuthorized ua) throws BObStatusException, IntegrationException{
        if(street == null || street.getCityStateZip() == null || ua == null){
            throw new BObStatusException("Cannot update street with null street, zip, or user");
        }
        
        PropertyIntegrator pi = getPropertyIntegrator();
        street.setLastUpdatedBy(ua);
        pi.updateMailingStreet(street);
        
    }
    
    
    /**
     * Logic container for deactivating a street record
     * I will also deactivate all addresses linked to this 
     * street and all the links to those addresses!
     * @param street to deactivate
     * @param ua doing the deactivating
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    public void deactivateMailingStreet(MailingStreet street, UserAuthorized ua) throws BObStatusException, IntegrationException{
        if(street == null || street.getCityStateZip() == null || ua == null){
            throw new BObStatusException("Cannot deactivate street with null street, zip, or user");
        }
        // start by deactivating the addresses on this street
        
        List<MailingAddress> mal = getMailingAddressListByStreet(street);
        if(mal != null && !mal.isEmpty()){
            for(MailingAddress ma: mal){
                deactivateMailingAddress(ma, ua);
            }
        }
        
        PropertyIntegrator pi = getPropertyIntegrator();
        street.setLastUpdatedBy(ua);
        street.setDeactivatedBy(ua);
        pi.updateMailingStreet(street);
        
    }
    
    /**
     * Asks the integrator to fetch a list of MailingStreets given one, both or neither
     * of the Street name part and CityStateZip
     * @param streetName can be null
     * @param csz can be null
     * @return a list, perhaps with results
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public List<MailingStreet> searchForMailingStreet(String streetName, MailingCityStateZip csz) throws IntegrationException, BObStatusException{
        PropertyIntegrator pi = getPropertyIntegrator();
        return pi.searchForStreetsByNameAndZip(streetName, csz);
        
    }
    
    /**
     * Extracts mailing address objects by street
     * @param mstreet
     * @return a list, potentially of 1 or more addresses at the given street
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public List<MailingAddress> getMailingAddressListByStreet(MailingStreet mstreet) throws IntegrationException, BObStatusException{
        PropertyIntegrator pi = getPropertyIntegrator();
        return pi.getMailingAddressListByStreet(mstreet);
        
        
    }
    
    
    
     /**
     * Logic container for retrieving city/state/zip objects
     * @param cszid
     * @return
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public MailingCityStateZip getMailingCityStateZip(int cszid) throws IntegrationException, BObStatusException{
        PropertyIntegrator pi = getPropertyIntegrator();
        return pi.getMailingCityStateZip(cszid);
    }

    
    /**
     * Logic stop for inserts of MailingAddress objects
     * @param addr to update; i'll inject the last updated by
     * @param ua doing the updating
     * @return the object ID of the freshly inserted record
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public int insertMailingAddress(MailingAddress addr, UserAuthorized ua) 
            throws BObStatusException, IntegrationException{
        if(addr == null || ua == null){
            throw new BObStatusException("Cannot update address with null incoming address or user");
        }
        
        if(addr.getStreet() == null || addr.getStreet().getCityStateZip() == null){
            throw new BObStatusException("cannot insert addres with null street or city/state/zip");
        }
        PropertyIntegrator pi = getPropertyIntegrator();
        addr.setCreatedBy(ua);
        addr.setLastUpdatedBy(ua);
        return pi.insertMailingAddress(addr);
    }
    
    /**
     * Logic stop for updates to MailingAddress objects
     * @param addr to update; i'll inject the last updated by
     * @param ua doing the updating
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void updateMailingAddress(MailingAddress addr, UserAuthorized ua) 
            throws BObStatusException, IntegrationException{
        if(addr == null || ua == null){
            throw new BObStatusException("Cannot update address with null incoming address or user");
        }
        PropertyIntegrator pi = getPropertyIntegrator();
        addr.setLastUpdatedBy(ua);
        pi.updateMailingAddress(addr);
    }
    
    /**
     * Deactivates a record in MailingAddress
     * @param addr
     * @param ua
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    public void deactivateMailingAddress(MailingAddress addr, UserAuthorized ua) 
            throws BObStatusException, IntegrationException{
        if(addr == null || ua == null){
            throw new BObStatusException("Cannot update address with null incoming address or user");
        }
        PropertyIntegrator pi = getPropertyIntegrator();
        // deactivate links to this mailing address first
        
        
        
        addr.setDeactivatedBy(ua);
        addr.setLastUpdatedBy(ua);
        addr.setDeactivatedTS(LocalDateTime.now());
        pi.updateMailingAddress(addr);
    }
    
     /**
     * Extracts the house number and street name from the address field
     * and injects into separate members on Property
     * 
     * @param prop
     * @return 
     */
    private Property parseAddress(Property prop){
        if(prop.getAddressString() != null){
            

            Pattern patNum = Pattern.compile("(?<num>\\d+[a-zA-Z]*)\\W+(?<street>\\w.*)");
            Matcher matNum = patNum.matcher(prop.getAddressString());

            while (matNum.find()){
                // Don't need this for humanization since we're already separating out num and street
//                prop.setAddressNum(matNum.group("num"));
//                prop.setAddressStreet(matNum.group("street"));
            }

        }
        return prop;
    }
    
    
        
    /**
     *  ***************************************************
     *  ***************************************************
     *  ************* PROPERTY GENERAL ********************
     *  ***************************************************
     *  ***************************************************
     */
    
    
    

    
    
    
    /**
     * Pass through for getting a property count by municode
     * @param muniCode
     * @return
     * @throws IntegrationException 
     */
    public int computeTotalProperties(int muniCode) throws IntegrationException{
        PropertyIntegrator pi = getPropertyIntegrator();
        return pi.getPropertyCount(muniCode);
    }
    
    
    /**
     * Logic container for initializing members on the Property subclass
     * PropertyWithLists
     *
     * @param prop
     * @param ua
     * @return the data heavy subclass
     * @throws IntegrationException
     * @throws BObStatusException
     * @throws com.tcvcog.tcvce.domain.SearchException
     */
    public PropertyDataHeavy assemblePropertyDataHeavy(Property prop, UserAuthorized ua) throws IntegrationException, BObStatusException, SearchException, BlobException {

        SearchCoordinator sc = getSearchCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
        PropertyIntegrator pi = getPropertyIntegrator();
        PropertyDataHeavy pdh = null;
        BlobCoordinator bc = getBlobCoordinator();
        BlobIntegrator bi = getBlobIntegrator();
        PersonCoordinator pc = getPersonCoordinator();
        
        if(prop != null && ua != null){
            // if we've been given a skeleton, just inject it into data heavy subclass
            if(prop.getParcelKey() == 0){
                pdh = new PropertyDataHeavy(prop);
            } else {

               pdh = new PropertyDataHeavy(getProperty(prop.getParcelKey()));

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
                       pdh.setPropInfoCaseList(new ArrayList<>());
                   }

                   if (pdh.getPropInfoCaseList().isEmpty()) {
                       pdh.getPropInfoCaseList().add(createPropertyInfoCase(pdh, ua));
                   }

                   // UnitDataHeavy list
                   // remember that units data heavy contain all our occ periods, inspections, and PropertyUnitChangeOrders
                   if (pdh.getUnitList() != null && !pdh.getUnitList().isEmpty()) {
                       pdh.setUnitWithListsList(getPropertyUnitDataHeavyList(pdh.getUnitList(), ua));
                   }

                   // Person list
                   pdh.setHumanLinkList(pc.getHumanLinkList(pdh));
                   
                   // Broadview photo
                    if(pdh.getBroadviewPhotoID() == 0){
                        pdh.setBroadviewPhoto(bc.getDefaultBroadviewPhoto());
                    } else {
                        pdh.setBroadviewPhoto(bc.getBlobLight(pdh.getBroadviewPhotoID()));
                    }
                   
                   
                   pdh.setBlobList(bc.getBlobLightList(pdh));
                   // external data\
                   // PARCEL INFO NOW LIVES on the parcel itself and uses the parcelinfo table
                   
//                   pdh.setExtDataList(fetchExternalDataRecords(pi.getPropertyExternalDataRecordIDs(pdh.getParcelKey())));

               } catch (EventException | AuthorizationException | BObStatusException | BlobException | IntegrationException | SearchException ex) {
                   System.out.println(ex);
                   System.out.println();
               }
            }
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
    public PropertyUnitWithProp getPropertyUnitWithProp(int unitid) throws IntegrationException, BObStatusException {
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
    public List<PropertyUnitDataHeavy> getPropertyUnitDataHeavyList(List<PropertyUnit> propUnitList, UserAuthorized ua) throws IntegrationException, EventException, EventException, AuthorizationException, BObStatusException {
        List<PropertyUnitDataHeavy> puwll = new ArrayList<>();
        Iterator<PropertyUnit> iter = propUnitList.iterator();
        while (iter.hasNext()) {
            PropertyUnit pu = iter.next();
            puwll.add(getPropertyUnitDataHeavy(pu, ua));
            
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
    public PropertyUnitDataHeavy getPropertyUnitDataHeavy(PropertyUnit propUnit, UserAuthorized ua) throws IntegrationException, EventException, EventException, AuthorizationException, BObStatusException {
        PropertyIntegrator pi = getPropertyIntegrator();
        if(propUnit == null){
            throw new BObStatusException("Cannot get property unit with lists given null prop unit or credential");
        }
        try {
            return configurePropertyUnitDataHeavy(pi.getPropertyUnitDataHeavy(propUnit.getUnitID()), ua);
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
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public Property configureProperty(Property p) throws IntegrationException, BObStatusException {
        PropertyIntegrator pi = getPropertyIntegrator();
        
        if(p != null){
            
            p.setUnitList(getPropertyUnitList(p));
            p.setMailingAddressLinkList(getMailingAddressLinkList(p));
           
          
        }
        
        
        // Don't need this for humanization
//        parseAddress(p);
        return p;
    }
    
    /**
     * Extracts all units associated with a given property
     * @param p
     * @return a list, perhaps with property units inside
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public List<PropertyUnit> getPropertyUnitList(Property p) throws IntegrationException, BObStatusException{
        PropertyIntegrator pi = getPropertyIntegrator();
        List<PropertyUnit> ul = new ArrayList<>();
        List<Integer> unitidl = pi.getPropertyUnitList(p);
        if(unitidl != null && !unitidl.isEmpty()){
            for(Integer i: unitidl){
                ul.add(getPropertyUnit(i));
            }
        }
        return ul;
    }
    
    
    

    
    /**
     * Assembles an address for pretty printing
     * @param addr for which to generate the address 
     * @param use2Lines if true, a <br /> will be inserted for double line 
     * conventional address printing
     * @return the String for injection into the property
     */
    private String buildPropertyAddressStrings(MailingAddress addr, boolean use2Lines, boolean includeCSZ){
        StringBuilder addrStr = new StringBuilder();
        if(addr != null){
            
            addrStr.append(addr.getBuildingNo());
            if(addr.getStreet() != null){
                addrStr.append(SPACE);
                addrStr.append(addr.getStreet().getName());
                if(addr.getStreet().getCityStateZip() != null && includeCSZ){
                    if(use2Lines){
                        addrStr.append(HTML_BR);
                    } else {
                        addrStr.append(SEMICOLON_SPACE);
                    }
                    addrStr.append(addr.getStreet().getCityStateZip().getCity());
                    addrStr.append(COMMA_SPACE);
                    addrStr.append(addr.getStreet().getCityStateZip().getState());
                    addrStr.append(SPACE);
                    addrStr.append(addr.getStreet().getCityStateZip().getZipCode());
                }
            }
        } else {
            addrStr.append("no address");
        }
        return addrStr.toString();
    }
    
    public LocalDateTime configureDateTime(Date date) {
        return new java.sql.Timestamp(date.getTime()).toLocalDateTime();
    }

    /**
     * Logic container for checking and setting properties on
     * PropertyUnitDataHeavy objects
     *
     * @param pudh
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @return
     */
    public PropertyUnitDataHeavy configurePropertyUnitDataHeavy(PropertyUnitDataHeavy pudh, UserAuthorized ua) 
            throws IntegrationException, AuthorizationException, EventException, BObStatusException, ViolationException {
        
        OccupancyCoordinator oc = getOccupancyCoordinator();
        PersonCoordinator pc = getPersonCoordinator();
        
        pudh.setCredentialSignature(ua.getKeyCard().getSignature());
        pudh.setPeriodList(oc.getOccPeriodList(pudh, ua));
        pudh.setHumanLinkList(pc.getHumanLinkList(pudh));
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
                    cse.setNotes("This is a Case object that contains information and events attached to " + p.getAddressString() + ". "
                            + "This case does not represent an actual code enforcement case.");
                    int freshid = cc.cecase_insertNewCECase(cse, ua, null, null);
                    csehv = cc.cecase_assembleCECaseDataHeavy(cc.cecase_getCECase(freshid), ua);

                } catch (IntegrationException | BObStatusException | EventException | ViolationException ex) {
                    System.out.println(ex);
                }
            }
        }

        return csehv;
    }

    /**
     * Primary pathway for the creation of new records in the parcel
     * table 
     *
     * @param pcl
     * @param ua
     * @return
     * @throws IntegrationException
     */
    public int addParcel(Parcel pcl, UserAuthorized ua) throws IntegrationException, BObStatusException {
        if(pcl == null || ua == null || pcl.getParcelInfo() == null){
            throw new BObStatusException("Cannot insert new parcel with null parcel or user");
        }
        PropertyIntegrator pi = getPropertyIntegrator();
        SystemIntegrator si = getSystemIntegrator();
        SystemCoordinator sc = getSystemCoordinator();
        
        if(pcl.getParcelInfo().isNonAddressable()){
            pcl.setCountyParcelID(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                        .getString("nonaddressable_parcelid"));
        }
        
        pcl.setCreatedBy(ua);
        pcl.setLastUpdatedBy(ua);
        pcl.setSource(si.getBOBSource(
                Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                        .getString("bobsourcePropertyInternal"))));
        
        // this controller class passes the new property to insert
        // over to the data model to be written into the Database
        return pi.insertParcel(pcl);
        

    }

    /**
     * Logic container for property updates
     *
     * @param pcl
     * @param ua
     * @throws IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public void updateParcel(Parcel pcl, UserAuthorized ua) throws IntegrationException, BObStatusException {
        PropertyIntegrator pi = getPropertyIntegrator();
        pcl.setLastUpdatedBy(ua);
        pi.updateParcel(pcl);

    }

    /**
     * Updates a parcel's broadview photo for reporting and profile
     * @param pcl
     * @param ua 
     */
    public void updatePropertyDataHeavyBroadviewPhoto(PropertyDataHeavy pdh, UserAuthorized ua) throws BObStatusException, IntegrationException {
        PropertyIntegrator pi = getPropertyIntegrator();
        
        if(pdh.getBroadviewPhoto() == null){
            throw new BObStatusException("Cannot update a parcel's broadview photo to null");
        }
        if(!pdh.getBroadviewPhoto().getType().isBrowserViewable()){
            throw new BObStatusException("Cannot update the broadview photo to a BLOB that's not browser viewable");
        }
        pdh.setLastUpdatedBy(ua);
        
        pi.updatePropertyDataHeavyBroadviewPhoto(pdh);
        
    }
    
   
    
    /**
     * Logic passthrough for insertions into the parcelinfo table
     * @param info
     * @param ua
     * @return the ID of the fresh record
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    public int insertParcelInfoRecord(ParcelInfo info, UserAuthorized ua) throws BObStatusException, IntegrationException{
        PropertyIntegrator pi =getPropertyIntegrator();
        if(info == null || ua == null){
            throw new BObStatusException("cannot insert a parcel info record with null info or UA");
        }
        info.setCreatedBy(ua);
        info.setLastUpdatedBy(ua);
        
        return pi.insertParcelInfo(info);
    }
    
    /**
     * Logic pass through for updates to the parcelinfo table
     * @param info
     * @param ua doing the updating
     * @throws BObStatusException
     * @throws IntegrationException 
     */
    public void updateParcelInfoRecord(ParcelInfo info, UserAuthorized ua) throws BObStatusException, IntegrationException{
        PropertyIntegrator pi = getPropertyIntegrator();
        if(info == null || ua == null){
            throw new BObStatusException("cannot insert a parcel info record with null info or UA");
        }
        info.setLastUpdatedBy(ua);
        pi.updateParcelInfo(info);
        
    }
    
    /**
     * Deactivates a record in the parcelinfo table
     * @param info
     * @param ua 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void deactivateParcelInfo(ParcelInfo info, UserAuthorized ua) throws BObStatusException, IntegrationException{
        
        PropertyIntegrator pi = getPropertyIntegrator();
        if(info == null || ua == null){
            throw new BObStatusException("cannot insert a parcel info record with null info or UA");
        }
        info.setDeactivatedBy(ua);
        info.setDeactivatedTS(LocalDateTime.now());
        info.setLastUpdatedBy(ua);
        pi.updateParcelInfo(info);
    }
    
    public boolean checkAllDates(Property prop) {
        boolean unfit, vacant, abandoned;

        // TODO: humanization: these values now live on the propertydata records, not direclty on
        // a parcel
//        unfit = checkStartDTisBeforeEndDT(prop.getUnfitDateStart(), prop.getUnfitDateStop());
//        vacant = checkStartDTisBeforeEndDT(prop.getVacantDateStart(), prop.getVacantDateStop());
//        abandoned = checkStartDTisBeforeEndDT(prop.getAbandonedDateStart(), prop.getAbandonedDateStop());
//        if (unfit && vacant && abandoned) {
//            return true;
//        } else {
//            return false;
//        }

        // short circuit logic
        return true;
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
    public PropertyDataHeavy getPropertyDataHeavy(int propID, UserAuthorized ua) throws IntegrationException, BObStatusException, AuthorizationException, EventException, SearchException, BlobException {
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
    public PropertyDataHeavy getPropertyDataHeavyByUnit(int propUnitID, UserAuthorized ua) throws IntegrationException, BObStatusException, AuthorizationException, EventException, SearchException, BlobException {
        PropertyIntegrator pi = getPropertyIntegrator();
        return assemblePropertyDataHeavy(pi.getPropertyUnitWithProp(propUnitID).getProperty(), ua);
    }

    /**
     * Logic intervention method for retrievals of simple properties from the DB
     * Updated to reflect parcelization in which there's no "propertyID" but 
     * rather a parcel ID that's internal to codeNforce and a countyParID
     * 
     * NOTE: we only have support for one parcel info record--we take the most recent record
     * in the DB ordered by last updated timestamp and inject that one into the parcel
     * 
     * @param parcelID
     * @return always a parcel object, perhaps an empty one to avoid null pointers, meaning ID = 0
     * @throws IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public Property getProperty(int parcelID) throws IntegrationException, BObStatusException {
        PropertyIntegrator pi = getPropertyIntegrator();
        Parcel par = pi.getParcel(parcelID);
        if(par == null){
            System.out.println("PropertyCoordinator.getProperty | NULL parcel from integrator | incoming parcelID: " + parcelID);
            par = new Parcel();
        }
        List<Integer> infoIDL = pi.getParcelInfoByParcel(par);
        if(infoIDL != null && !infoIDL.isEmpty()){
            par.setParcelInfo(pi.getParcelInfo(infoIDL.get(0)));
        } else {
            // inject an empty object if we have none to avoid null pointers
            par.setParcelInfo(new ParcelInfo());
        }
        Property p = new Property(par);
        
        return configureProperty(p);

    }

    /**
     * Logic intermediary for retrieval of a PropertyUnit by id
     *
     * @param unitID
     * @return
     * @throws IntegrationException
     */
    public PropertyUnit getPropertyUnit(int unitID) throws IntegrationException, BObStatusException {
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

    public Property getPropertyByPropUnitID(int unitID) throws IntegrationException, BObStatusException {
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
            } catch (IntegrationException | AuthorizationException | BObStatusException | EventException | SearchException | BlobException ex) {
                System.out.println(ex);
            }
        }
        return null;
    }
    
    
   
    /**
     *
     * 
     * 
     * @param cred
     * @return
     */
    public List<Property> assemblePropertyHistoryList(Credential cred) throws BObStatusException {
        PropertyIntegrator pi = getPropertyIntegrator();
        List<Property> propList = new ArrayList<>();
        List<Integer> propIDList = new ArrayList<>();

        if (cred != null) {
            try {
                propIDList.addAll(pi.getPropertyHistoryList(cred));
                while (!propIDList.isEmpty() && propIDList.size() <= Constants.MAX_BOB_HISTORY_SIZE) {
                    // Only developers get a heterogeneous mix of muni in their history
                    if (cred.isHasDeveloperPermissions()) {
                        propList.add(getProperty(propIDList.remove(0)));
                    } else {
                        Municipality m = cred.getGoverningAuthPeriod().getMuni();
                        for (Property pr : propList) {
                            if (pr.getMuni().getMuniCode() == m.getMuniCode()) {
                                propList.add(pr);
                            }
                        }
                    }
                }
            } catch (IntegrationException | BObStatusException  ex) {
                System.out.println(ex);
            }
        }
        return propList;
    }

   
    
    /**
     * TODO: Update for parcelization
     * Generator method for Property objects
     * @param muni
     * @return the skelton property object with only muni set and ID of 0
     */
    public Property generatePropertySkeleton(Municipality muni) {
        Property prop = new Property(new Parcel());
        prop.setParcelInfo(new ParcelInfo());
        prop.getParcelInfo().setNonAddressable(false);
        prop.setParcelKey(0);
        prop.setMuni(muni);
        return prop;
    }

    
        
    /**
     *  ***************************************************
     *  ***************************************************
     *  ************* UNITS UNITS UNITS *******************
     *  ***************************************************
     *  ***************************************************
     */
    
    /**
     * This method generates a skeleton PropertyUnit with logical, preset
     * defaults, including empty lists.
     *
     * @param p
     * @return
     */
    public PropertyUnit getPropertyUnitSkeleton(Property p) {
        PropertyUnit propUnit = new PropertyUnit();
        propUnit.setParcelKey(p.getParcelKey());
        propUnit.setUnitNumber(Constants.TEMP_UNIT_NUM);
        return propUnit;
    }
    
    /**
     * Logic check for inserts of property units
     * @param unit
     * @param pdh
     * @param ua
     * @return the PK of the new unit
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public int insertPropertyUnit(PropertyUnit unit, PropertyDataHeavy pdh, UserAuthorized ua) throws BObStatusException, IntegrationException{
        PropertyIntegrator pi = getPropertyIntegrator();
        
        if(unit == null || pdh == null || ua == null){
            throw new BObStatusException("Cannot insert unit with null unit, property, or user");
        }
        unit.setCreatedBy(ua);
        unit.setLastUpdatedBy(ua);
        unit.setParcelKey(pdh.getParcelKey());
        
        
        return pi.insertPropertyUnit(unit);
        
    }
    
    /**
     * Logic check for updates to a property unit
     * @param unit
     * @param ua 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void updatePropertyUnit(PropertyUnit unit, UserAuthorized ua) throws BObStatusException, IntegrationException{
        PropertyIntegrator pi = getPropertyIntegrator();
        
        if(unit == null || ua == null){
            throw new BObStatusException("Cannot insert unit with null unit, property, or user");
        }
        unit.setLastUpdatedBy(ua);
        
        pi.updatePropertyUnit(unit);
    }
    
    /**
     * Logic block for deactivation requests on a property unit
     * @param unit
     * @param pdh
     * @param ua 
     */
    public void deactivatePropertyUnit(PropertyUnit unit, PropertyDataHeavy pdh, UserAuthorized ua) throws BObStatusException{
        PropertyIntegrator pi = getPropertyIntegrator();
        
        if(unit == null || pdh == null || ua == null){
            throw new BObStatusException("Cannot insert unit with null unit, property, or user");
        }
        
        
        
        
    }
    

    /**
     * A method that takes a unit list and compares it to the database and
     * applies any changes (deactivations and insertions too) BUT BYPASSES THE
     * UNITCHANGEORDER WORKFLOW, so it should only be used internally.
     *
     * @deprecated Nathan old code
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
            unit.setParcelKey(prop.getParcelKey());

            if (unit.getUnitID() == 0) {
                pi.insertPropertyUnit(unit);
            } else {
                pi.updatePropertyUnit(unit);
            }
        }

//        List<PropertyUnit> listTwo = pi.getPropertyUnitList(prop);

//        prop.setUnitList(listTwo);

        // mark parent property as updated now
        updateParcel(prop, getSessionBean().getSessUser());

        return null;

    }

    /**
     * Implements an existing change order and update its corresponding property
     * unit, also deactivates and updates the change order to record who
     * approved the transaction
     *
     * @param uc
     * @throws IntegrationException
     */
    public void implementPropertyUnitChangeOrder(PropertyUnitChangeOrder uc) throws IntegrationException, BObStatusException {

        PropertyIntegrator pi = getPropertyIntegrator();

        //If the user added the unit, their changes will already be in the database. No need to update
        if (!uc.isAdded()) {

            PropertyUnit skeleton = getPropertyUnit(uc.getUnitID());

            if (uc.isRemoved()) {
                skeleton.setDeactivatedTS(LocalDateTime.now()); //just deactivate the unit.
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
