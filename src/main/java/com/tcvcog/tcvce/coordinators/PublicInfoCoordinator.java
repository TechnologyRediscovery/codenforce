/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CECasePropertyUnitHeavy;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.FeeAssigned;
import com.tcvcog.tcvce.entities.MoneyOccPeriodFeeAssigned;
import com.tcvcog.tcvce.entities.Payment;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PublicInfoBundle;
import com.tcvcog.tcvce.entities.PublicInfoBundleCEActionRequest;
import com.tcvcog.tcvce.entities.PublicInfoBundleCECase;
import com.tcvcog.tcvce.entities.PublicInfoBundleFeeAssigned;
import com.tcvcog.tcvce.entities.PublicInfoBundleOccInspection;
import com.tcvcog.tcvce.entities.PublicInfoBundleOccPeriod;
import com.tcvcog.tcvce.entities.PublicInfoBundlePayment;
import com.tcvcog.tcvce.entities.PublicInfoBundlePerson;
import com.tcvcog.tcvce.entities.PublicInfoBundleProperty;
import com.tcvcog.tcvce.entities.occupancy.OccInspection;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodDataHeavy;
import com.tcvcog.tcvce.integration.CEActionRequestIntegrator;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import com.tcvcog.tcvce.occupancy.integration.PaymentIntegrator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sylvia
 */
public class PublicInfoCoordinator extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of PublicInfoCoordinator
     */
    public PublicInfoCoordinator() {
    }

    /**
     * The primary entry point for getting PACC data for any relevant object
     * type
     *
     * @param pacc the entered control code from the user
     * @return a linked list of info bundles from any source
     * @throws IntegrationException created by any of the interrogated
     * integrator classes that look for public info.
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.SearchException
     */
    public List<PublicInfoBundle> getPublicInfoBundles(int pacc) throws IntegrationException, BObStatusException, SearchException {

        CaseIntegrator caseInt = getCaseIntegrator();
        List<CEActionRequest> requestList;
        SearchCoordinator sc = getSearchCoordinator();

        // this list will store bundles from all sources polled in this method
        // go polymorphism!!
        List<PublicInfoBundle> infoBundleList = new ArrayList<>();

        // start with CE action requests
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();

        requestList = ceari.getCEActionRequestByControlCode(pacc);

        for (CEActionRequest cear : requestList) {
            PublicInfoBundleCEActionRequest bundle = extractPublicInfo(cear);

            // if the current action request is linked to a CECaseDataHeavy,
            // go grab that case and check for allowed forward access
            // if forwardLinking is allowed, scrape public data from case and add
            if (cear.getCaseID() != 0) {
                CECase caseFromActionRequest = caseInt.getCECase(cear.getCaseID());
                if (caseFromActionRequest.isAllowForwardLinkedPublicAccess()) {
                    infoBundleList.add(extractPublicInfo(caseFromActionRequest));
                }
            }
            infoBundleList.add(bundle);
        }

        // now go and get CECaseDataHeavy bundles and add them to the list
//        QueryCECase qc = sc.initQuery(QueryCECaseEnum.PACC, cred);
//     
//        List<CECase> caseList = 
//        System.out.println("PublicInfoCoordinator.getPublicInfoBundles | num CE cases found: " + caseList.size());;
//      
//        for(CECase c: caseList){
//            // let the extraction method deal with all the assembly logic
//            // and access control issues
//            infoBundleList.add(extractPublicInfo(c));
//        }
        return infoBundleList;
    }

    private PublicInfoBundleCECase extractPublicInfo(CECase cse) throws IntegrationException, SearchException {
        CaseCoordinator cc = getCaseCoordinator();
        CECasePropertyUnitHeavy c = cc.assembleCECasePropertyUnitHeavy(cse, getSessionBean().getSessUser().getMyCredential());

        PublicInfoBundleCECase pib = new PublicInfoBundleCECase();

        pib.setTypeName("CECASE");
        pib.setMuni(c.getProperty().getMuni());

        if (c.isPaccEnabled()) {
            pib.setBundledCase(cse);
            pib.setPropertyAddress(c.getProperty().getAddress());

            pib.setPublicEventList(new ArrayList<EventCnF>());
            for (EventCnF ev : c.getVisibleEventList()) {
                if (ev.isDiscloseToPublic()) {
                    pib.getPublicEventList().add(ev);
                }
            }

            pib.setShowAddMessageButton(false);
            pib.setPaccStatusMessage("Public access enabled");

        } else {

            CECase skeleton = new CECase();
            skeleton.setCaseID(cse.getCaseID());
            pib.setBundledCase(skeleton);
            pib.setPaccStatusMessage("This control code is associated with a "
                    + "code enforcement case but the case manager has not permitted "
                    + "pulic release of this information. Please contact your "
                    + "municipal staff at the contact info displayed here. ");
        }

        return pib;

    }

    private PublicInfoBundleCEActionRequest extractPublicInfo(CEActionRequest req) {

        PublicInfoBundleCEActionRequest pib = new PublicInfoBundleCEActionRequest();

        pib.setDateOfRecord(getPrettyDate(req.getDateOfRecord()));

        if (req.isPaccEnabled()) {

            pib.setBundledRequest(req);
            pib.setPaccStatusMessage("Public access enabled");
            
            pib.setTypeName("Code enforcement action request");


            pib.setShowAddMessageButton(true);
            pib.setShowDetailsPageButton(false);
        } else {
            pib.setBundledRequest(new CEActionRequest());
            pib.setPaccStatusMessage("A public information bundle was found but public "
                    + "access was switched off by a code officer. Please contact your municipal office. ");

        }

        return pib;

    }

    public PublicInfoBundlePerson extractPublicInfo(Person input) {

        PublicInfoBundlePerson pib = new PublicInfoBundlePerson();
        //the Person object does not have a PACC Enabled field, 
        //but I figure that a person under 18 should probably not be revealed to the public.
        if (!input.isUnder18()) {

            PersonCoordinator pc = getPersonCoordinator();

            input = pc.anonymizePersonData(input);

            pib.setBundledPerson(input);

            pib.setTypeName("Person");
            pib.setPaccStatusMessage("Public access enabled");

            pib.setShowAddMessageButton(false);
            pib.setShowDetailsPageButton(true);
        } else {
            pib.setBundledPerson(new Person());
            pib.setPaccStatusMessage("A public information bundle was found but public "
                    + "access was switched off by a code officer. Please contact your municipal office. ");

        }

        return pib;
    }

    public PublicInfoBundleProperty extractPublicInfo(Property input) {
        PublicInfoBundleProperty pib = new PublicInfoBundleProperty();

        //Again, no PACC enabled field. Perhaps this will be a good enough filter for now?
        if (!input.isActive()) {

            pib.setBundledProperty(input);

            pib.setTypeName("Property");
            pib.setPaccStatusMessage("Public access enabled");

            pib.setShowAddMessageButton(false);
            pib.setShowDetailsPageButton(true);
        } else {
            pib.setBundledProperty(new Property());
            pib.setPaccStatusMessage("A public information bundle was found but public "
                    + "access was switched off by a code officer. Please contact your municipal office. ");

        }

        return pib;
    }

    public PublicInfoBundleOccPeriod extractPublicInfo(OccPeriod input) throws IntegrationException, BObStatusException, SearchException {
        PublicInfoBundleOccPeriod pib = new PublicInfoBundleOccPeriod();

        //Again, no PACC enabled field. Perhaps this will be a good enough filter for now?
        if (!input.isActive()) {
            OccupancyCoordinator oc = getOccupancyCoordinator();
            OccPeriodDataHeavy heavy = oc.assembleOccPeriodDataHeavy(input, getSessionBean().getSessUser().getMyCredential());

            pib.setBundledPeriod(input);

            pib.setTypeName("OccPeriod");
            pib.setPaccStatusMessage("Public access enabled");

            ArrayList<PublicInfoBundlePerson> bundledPersons = new ArrayList<>();

            for (Person skeleton : heavy.getPersonList()) {

                bundledPersons.add(extractPublicInfo(skeleton));

            }
            
            ArrayList<PublicInfoBundleOccInspection> bundledInspections= new ArrayList<>();

            for (OccInspection skeleton : (List<OccInspection>) heavy.getInspectionList()) {

                bundledInspections.add(extractPublicInfo(skeleton));

            }
            
            ArrayList<PublicInfoBundleFeeAssigned> bundledFees= new ArrayList<>();

            for (FeeAssigned skeleton : heavy.getFeeList()) {

                bundledFees.add(extractPublicInfo(skeleton));

            }

            pib.setPersonList(bundledPersons);

            pib.setInspectionList(bundledInspections);
            pib.setFeeList(bundledFees);
            pib.setPaymentList();

            pib.setShowAddMessageButton(false);
            pib.setShowDetailsPageButton(true);
        } else {
            pib.setBundledPeriod(new OccPeriod());
            pib.setPaccStatusMessage("A public information bundle was found but public "
                    + "access was switched off by a code officer. Please contact your municipal office. ");

        }

        return pib;
    }
    
    public PublicInfoBundleOccInspection extractPublicInfo(OccInspection input) throws IntegrationException, BObStatusException, SearchException {
        PublicInfoBundleOccInspection pib = new PublicInfoBundleOccInspection();

        if (input.isEnablePacc()) {

            pib.setBundledInspection(input);

            pib.setThirdPartyInspector(extractPublicInfo(input.getThirdPartyInspector()));
            
            pib.setTypeName("OccInspection");
            pib.setPaccStatusMessage("Public access enabled");
            
            pib.setShowAddMessageButton(false);
            pib.setShowDetailsPageButton(true);
        } else {
            pib.setBundledInspection(new OccInspection());
            pib.setPaccStatusMessage("A public information bundle was found but public "
                    + "access was switched off by a code officer. Please contact your municipal office. ");

        }

        return pib;
    }
    
    public PublicInfoBundleFeeAssigned extractPublicInfo(FeeAssigned input) throws IntegrationException, BObStatusException, SearchException {
        PublicInfoBundleFeeAssigned pib = new PublicInfoBundleFeeAssigned();

            pib.setBundledFee(input);
            
            pib.setTypeName("FeeAssigned");
            pib.setPaccStatusMessage("Public access enabled");
            
            pib.setShowAddMessageButton(false);
            pib.setShowDetailsPageButton(true);
        
        return pib;
    }

    
    /**
     * Converts a bundled FeeAssigned to an unbundled FeeAssigned.
     * 
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @param input
     * @return

     */
    public FeeAssigned export(PublicInfoBundleFeeAssigned input) throws IntegrationException {

        PaymentIntegrator pi = getPaymentIntegrator();
        
        FeeAssigned unbundled = input.getBundledFee();
        //FeeAssigned exportable = pi.getFeeAssigned(); TODO: write method to get feeAssigned from ID
        
       ArrayList<Payment> skeletonHorde = new ArrayList<>();
        
        for(PublicInfoBundlePayment bundle : input.getPaymentList()) {
            
            skeletonHorde.add(export(bundle));
            
        }
        
        unbundled.setPaymentList(skeletonHorde);
        
        return unbundled; //should be exportable once that field is ready.

    }
    /**
     * Converts a bundled OccInspection to an unbundled OccInspection.
     * 
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @param input
     * @return

     */
    public OccInspection export(PublicInfoBundleOccInspection input) throws IntegrationException {

        OccupancyIntegrator oi = getOccupancyIntegrator();
        
        OccInspection unbundled = input.getBundledInspection();
        //OccInspection exportable = TODO: Figure out how to get an occinspection from the DB
        
        unbundled.setThirdPartyInspector(export(input.getThirdPartyInspector()));
        
        return unbundled; //should be exportable once that field is ready.

    }
    
    /**
     * Converts a bundled property to an unbundled property. TODO: see if this
     * needs to be updated. When this JavaDoc was written, checking for changes was not necessary.
     * 
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @param input
     * @return

     */
    public Property export(PublicInfoBundleProperty input) throws IntegrationException {

        PropertyCoordinator pc = getPropertyCoordinator();
        
        Property unbundled = input.getBundledProperty();
        Property exportable = pc.getProperty(unbundled.getPropertyID());
        
        exportable.setAddress(unbundled.getAddress());
        exportable.setStatus(unbundled.getStatus());
        exportable.setMuni(unbundled.getMuni());
        exportable.setParID(unbundled.getParID());
        exportable.setLotAndBlock(unbundled.getLotAndBlock());
        exportable.setUseGroup(unbundled.getUseGroup());
        exportable.setConstructionType(unbundled.getConstructionType());
        exportable.setCountyCode(unbundled.getCountyCode());
        exportable.setAddress_city(unbundled.getAddress_city());
        exportable.setAddress_state(unbundled.getAddress_state());
        exportable.setAddress_zip(unbundled.getAddress_zip());
        exportable.setOwnerCode(unbundled.getOwnerCode());
        exportable.setPropclass(unbundled.getPropclass());
        exportable.setUseType(unbundled.getUseType());
        
        return exportable;

    }

    /**
     * Converts a bundled OccPeriod to an unbundled OccPeriod
     *
     * @param input
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.SearchException
     * @return
     */
    public OccPeriodDataHeavy export(PublicInfoBundleOccPeriod input) throws IntegrationException, BObStatusException, SearchException {

        OccupancyCoordinator oc = getOccupancyCoordinator();

        OccPeriod unbundled = input.getBundledPeriod();

        OccPeriodDataHeavy exportable = oc.assembleOccPeriodDataHeavy(unbundled, getSessionBean().getSessUser().getMyCredential());

        ArrayList<Person> skeletonHorde = new ArrayList<>();

        for (PublicInfoBundlePerson bundle : input.getPersonList()) {

            skeletonHorde.add(export(bundle));

        }

        ArrayList<OccInspection> inspectionHorde = new ArrayList<>();
        
        for(PublicInfoBundleOccInspection bundle : input.getInspectionList()) {
            
            inspectionHorde.add(export(bundle));
            
        }
        
        ArrayList<MoneyOccPeriodFeeAssigned> feeHorde = new ArrayList<>();
        
        for(PublicInfoBundleFeeAssigned bundle : input.getFeeList()) {
            
            feeHorde.add((MoneyOccPeriodFeeAssigned) export(bundle));
            
        }
        
        exportable.setPersonList(skeletonHorde);

        exportable.setInspectionList(inspectionHorde);

        exportable.setFeeList(feeHorde);

        exportable.setPaymentList(input.getPaymentList());

        return exportable;

    }

    /**
     * Converts a bundled person to an unbundled person by fetching the original
     * from the database then seeing if any of the fields on the bundled version
     * have been changed and bringing them over. Some fields are not checked for
     * changes because the public should not be able to change them anyway.
     *
     * @param input
     * @return
     * @throws IntegrationException
     */
    public Person export(PublicInfoBundlePerson input) throws IntegrationException {
        PersonCoordinator pc = getPersonCoordinator();
        Person unbundled = input.getBundledPerson();

        Person exportable = pc.getPerson(unbundled.getPersonID());

        //fields are anonymized by being overwritten with asterisks. If these fields no longer contain asterisks,
        //then the field has been edited by the user.
        if (!unbundled.getFirstName().contains("*")) {
            exportable.setFirstName(unbundled.getFirstName());
        }

        if (!unbundled.getLastName().contains("*")) {
            exportable.setLastName(unbundled.getLastName());
        }

        if (!unbundled.getPhoneCell().contains("*")) {
            exportable.setPhoneCell(unbundled.getPhoneCell());
        }

        if (!unbundled.getPhoneHome().contains("*")) {
            exportable.setPhoneHome(unbundled.getPhoneHome());
        }

        if (!unbundled.getPhoneWork().contains("*")) {
            exportable.setPhoneWork(unbundled.getPhoneWork());
        }

        if (!unbundled.getEmail().contains("*")) {
            exportable.setEmail(unbundled.getEmail());
        }

        if (!unbundled.getAddressStreet().contains("*")) {
            exportable.setAddressStreet(unbundled.getAddressStreet());
        }

        if (!unbundled.getAddressCity().contains("*")) {
            exportable.setAddressCity(unbundled.getAddressCity());
        }

        if (!unbundled.getAddressZip().contains("*")) {
            exportable.setAddressZip(unbundled.getAddressZip());
        }

        if (!unbundled.getAddressState().contains("*")) {
            exportable.setAddressState(unbundled.getAddressState());
        }

        if (!unbundled.getMailingAddressStreet().contains("*")) {
            exportable.setMailingAddressStreet(unbundled.getMailingAddressStreet());
        }

        if (!unbundled.getMailingAddressThirdLine().contains("*")) {
            exportable.setMailingAddressThirdLine(unbundled.getMailingAddressThirdLine());
        }

        if (!unbundled.getMailingAddressCity().contains("*")) {
            exportable.setMailingAddressCity(unbundled.getMailingAddressCity());
        }

        if (!unbundled.getMailingAddressZip().contains("*")) {
            exportable.setMailingAddressZip(unbundled.getMailingAddressZip());
        }

        if (!unbundled.getMailingAddressState().contains("*")) {
            exportable.setMailingAddressState(unbundled.getMailingAddressState());
        }

        return exportable;
    }

    public void attachMessageToBundle(PublicInfoBundle bundle, String message) throws IntegrationException {
        LocalDateTime current = LocalDateTime.now();
        PublicInfoBundleCEActionRequest requestBundle;

        System.out.println("PublicInfoCoordinator.attachmessagToBundle: In coordinator");

        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        if (bundle.getTypeName().equals("Code enforcement action request")) {
            requestBundle = (PublicInfoBundleCEActionRequest) bundle;
            StringBuilder sb = new StringBuilder();
            sb.append(requestBundle.getBundledRequest().getPublicExternalNotes());
            sb.append("<br /><br />");
            sb.append("CASE NOTE ADDED AT ");
            sb.append(current.toString());
            sb.append("by public user: <br />");
            sb.append(message);
            sb.append("<br />");
            sb.append("***********************");

            System.out.println("PublicInfoCoordinator.attachmessagToBundle | message: " + sb.toString());
            ceari.attachMessageToCEActionRequest(requestBundle, sb.toString());
        } else if (bundle.getTypeName().equals("CECASE")) {

        }
    }
} // close class
