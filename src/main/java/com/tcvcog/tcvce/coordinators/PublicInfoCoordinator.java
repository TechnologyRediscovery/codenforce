/*
 * Copyright (C) 2020 Technology Rediscovery LLC.
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
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventCnFPropUnitCasePeriodHeavy;
import com.tcvcog.tcvce.entities.EventDomainEnum;
import com.tcvcog.tcvce.entities.FeeAssigned;
import com.tcvcog.tcvce.entities.MoneyOccPeriodFeeAssigned;
import com.tcvcog.tcvce.entities.MoneyOccPeriodFeePayment;
import com.tcvcog.tcvce.entities.Payment;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonOccApplication;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyDataHeavy;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyUnitDataHeavy;
import com.tcvcog.tcvce.entities.PublicInfoBundle;
import com.tcvcog.tcvce.entities.PublicInfoBundleCEActionRequest;
import com.tcvcog.tcvce.entities.PublicInfoBundleCECase;
import com.tcvcog.tcvce.entities.PublicInfoBundleCodeViolation;
import com.tcvcog.tcvce.entities.PublicInfoBundleEventCnF;
import com.tcvcog.tcvce.entities.PublicInfoBundleFeeAssigned;
import com.tcvcog.tcvce.entities.PublicInfoBundleOccInspection;
import com.tcvcog.tcvce.entities.PublicInfoBundleOccPeriod;
import com.tcvcog.tcvce.entities.PublicInfoBundleOccPermitApplication;
import com.tcvcog.tcvce.entities.PublicInfoBundlePayment;
import com.tcvcog.tcvce.entities.PublicInfoBundlePerson;
import com.tcvcog.tcvce.entities.PublicInfoBundlePersonOccApplication;
import com.tcvcog.tcvce.entities.PublicInfoBundleProperty;
import com.tcvcog.tcvce.entities.PublicInfoBundlePropertyUnit;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.occupancy.OccInspection;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodDataHeavy;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodPropertyUnitHeavy;
import com.tcvcog.tcvce.entities.search.QueryCECase;
import com.tcvcog.tcvce.entities.search.QueryCECaseEnum;
import com.tcvcog.tcvce.entities.occupancy.OccPermitApplication;
import com.tcvcog.tcvce.integration.CEActionRequestIntegrator;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccInspectionIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import com.tcvcog.tcvce.occupancy.integration.PaymentIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nathan Dietz
 */
public class PublicInfoCoordinator extends BackingBeanUtils implements Serializable {

    final int PUBLIC_VIEW_USER_RANK = 1;
    private UserAuthorized publicUser;

    /**
     * Creates a new instance of PublicInfoCoordinator
     */
    public PublicInfoCoordinator() {
    }

    /**
     * Initializes the public user so coordinator methods can be used by users
     * without a login
     *
     * @throws IntegrationException
     */
    private void setPublicUser() throws IntegrationException {
        if (publicUser == null) {
            UserCoordinator uc = getUserCoordinator();
            publicUser = uc.auth_getPublicUserAuthorized();
        }
    }

    /**
     * The primary entry point for getting PACC data for any relevant object
     * type TODO: UPDATE TO INCLUDE NEW BUNDLES
     *
     * @param pacc the entered control code from the user
     * @return a linked list of info bundles from any source
     * @throws IntegrationException created by any of the interrogated
     * integrator classes that look for public info.
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.SearchException
     * @throws com.tcvcog.tcvce.domain.EventException
     * @throws com.tcvcog.tcvce.domain.AuthorizationException
     * @throws com.tcvcog.tcvce.domain.ViolationException
     */
    public List<PublicInfoBundle> getPublicInfoBundles(int pacc)
            throws IntegrationException,
            BObStatusException,
            SearchException,
            EventException,
            AuthorizationException,
            ViolationException {

        CaseIntegrator caseInt = getCaseIntegrator();
        SearchCoordinator sc = getSearchCoordinator();

        // this list will store bundles from all sources polled in this method
        // go polymorphism!!
        List<PublicInfoBundle> infoBundleList = new ArrayList<>();

        // start with CE action requests
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();

        List<CEActionRequest> requestList = ceari.getCEActionRequestByControlCode(pacc);

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
        //TODO: Fix this, it does not retrieve cases
        //setPublicUser();
        //QueryCECase qc = sc.initQuery(QueryCECaseEnum.PACC, publicUser.getMyCredential());
        //List<CECase> caseList = qc.getBOBResultList();
        
        //quick patch up
        List<CECase> caseList = caseInt.getCECasesByPACC(pacc);

        System.out.println("PublicInfoCoordinator.getPublicInfoBundles | num CE cases found: " + caseList.size());

        for (CECase c : caseList) {
            // let the extraction method deal with all the assembly logic
            // and access control issues
            infoBundleList.add(extractPublicInfo(c));
        }

        //then grab OccPermitApplications
        OccupancyIntegrator oi = getOccupancyIntegrator();

        List<OccPermitApplication> applicationList = oi.getOccPermitApplicationListByControlCode(pacc);

        for (OccPermitApplication opa : applicationList) {

            infoBundleList.add(extractPublicInfo(opa));

        }

        OccInspectionIntegrator oii = getOccInspectionIntegrator();

        List<OccInspection> inspectionList = oii.getOccInspectionListByPACC(pacc);

        for (OccInspection ins : inspectionList) {

            infoBundleList.add(extractPublicInfo(ins));

        }

        return infoBundleList;
    }

    /**
     * Bundles a CECase into a PublicInfoBundleCECase by stripping out its
     * private information.
     *
     * @param cse
     * @return
     * @throws IntegrationException
     * @throws SearchException
     * @throws EventException
     * @throws AuthorizationException
     * @throws BObStatusException
     */
    private PublicInfoBundleCECase extractPublicInfo(CECase cse) 
            throws IntegrationException, 
            SearchException, 
            EventException, 
            AuthorizationException, 
            BObStatusException {
        CaseCoordinator cc = getCaseCoordinator();
        setPublicUser();
        CECaseDataHeavy c = cc.cecase_assembleCECaseDataHeavy(cse, publicUser);

        cse = new CECase(c);
        PublicInfoBundleCECase pib = new PublicInfoBundleCECase();

        pib.setTypeName("CECASE");
        pib.setMuni(c.getProperty().getMuni());
        pib.setPacc(cse.getPublicControlCode());
        pib.setPaccEnabled(c.isPaccEnabled());

        if (c.isPaccEnabled()) {
            pib.setBundledCase(cse);
            if (c.getProperty() == null || c.getProperty().isNonAddressable()) {
                pib.setAddressAssociated(false);
            } else {
                pib.setAddressAssociated(true);
                pib.setPropertyAddress(c.getProperty().getAddress());
            }

            ArrayList<PublicInfoBundleEventCnF> eventBundles = new ArrayList<>();

            for (EventCnF ev : c.getEventList()) {
                //Only add events that are visible to the public.
                if (ev.getCategory().getUserRankMinimumToView() <= PUBLIC_VIEW_USER_RANK) {
                    eventBundles.add(extractPublicInfo(ev));
                }
            }

            pib.setPublicEventList(eventBundles);

            ArrayList<PublicInfoBundleCodeViolation> violationBundles = new ArrayList<>();

            //We only need the unresolved ones.
            for (CodeViolation vio : c.getViolationListUnresolved()) {
                violationBundles.add(extractPublicInfo(vio));
            }

            pib.setViolationList(violationBundles);

            pib.setAddress(c.getProperty());
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

            pib.setShowAddMessageButton(false);
        }

        return pib;

    }

    /**
     * Bundles a CodeViolation into a PublicInfoBundleCodeViolation by stripping
     * out its private information.
     *
     * @param input
     * @return
     */
    public PublicInfoBundleCodeViolation extractPublicInfo(CodeViolation input) {

        PublicInfoBundleCodeViolation pib = new PublicInfoBundleCodeViolation();
        //the CodeViolation object does not have a PACC Enabled field
        //And probably shouldn't because public users need to know what is wrong with their properties

        pib.setBundledViolation(input);

        pib.setTypeName("CodeViolation");
        pib.setPaccStatusMessage("Public access enabled");
        pib.setPaccEnabled(true);

        pib.setShowAddMessageButton(false);
        pib.setShowDetailsPageButton(true);

        return pib;
    }

    /**
     * Bundles a CEActionRequest into a PublicInfoBundleCEActionRequest by
     * stripping out its private information.
     *
     * @param req
     * @throws AuthorizationException
     * @throws BObStatusException
     * @throws EventException
     * @throws IntegrationException
     * @throws SearchException
     * @return
     */
    private PublicInfoBundleCEActionRequest extractPublicInfo(CEActionRequest req) 
            throws IntegrationException, 
            EventException, 
            AuthorizationException, 
            BObStatusException, 
            SearchException {

        PublicInfoBundleCEActionRequest pib = new PublicInfoBundleCEActionRequest();

        pib.setDateOfRecord(getPrettyDate(req.getDateOfRecord()));

        pib.setTypeName("CEAR");

        pib.setPacc(req.getRequestPublicCC());
        pib.setPaccEnabled(req.isPaccEnabled());

        if (req.isPaccEnabled()) {

            pib.setRequestor(extractPublicInfo(req.getRequestor()));

            pib.setRequestProperty(extractPublicInfo(req.getRequestProperty()));

            pib.setBundledRequest(req);
            pib.setPaccStatusMessage("Public access enabled");

            pib.setShowAddMessageButton(true);
            pib.setShowDetailsPageButton(false);
        } else {
            CEActionRequest skeleton = new CEActionRequest();
            skeleton.setRequestID(req.getRequestID());
            pib.setBundledRequest(skeleton);
            pib.setPaccStatusMessage("A public information bundle was found but public "
                    + "access was switched off by a code officer. Please contact your municipal office. ");

            pib.setShowAddMessageButton(false);
        }

        return pib;

    }

    /**
     * Bundles a Payment into a PublicInfoBundlePayment by stripping out its
     * private information.
     *
     * @param input
     * @return
     */
    public PublicInfoBundlePayment extractPublicInfo(Payment input) {

        PublicInfoBundlePayment pib = new PublicInfoBundlePayment();

        pib.setTypeName("Payment");
        pib.setShowAddMessageButton(false);
        //the Paymentobject does not have a PACC Enabled field
        //if (!input.isPaccEnabled()) {

        PersonCoordinator pc = getPersonCoordinator();

        Person skeleton = pc.anonymizePersonData(input.getPayer());

        pib.setPayer(extractPublicInfo(skeleton));

        pib.setBundledPayment(input);

        pib.setPaccStatusMessage("Public access enabled");
        pib.setPaccEnabled(true);

        pib.setShowDetailsPageButton(true);
        /*} else {
            Payment skeleton = new Payment();
            skeleton.setPaymentID(input.getPaymentID());
            pib.setBundledPayment(skeleton);
            pib.setPaccStatusMessage("A public information bundle was found but public "
                    + "access was switched off by a code officer. Please contact your municipal office. ");

        }*/

        return pib;
    }

    /**
     * Bundles a Person into a PublicInfoBundlePerson by stripping out its
     * private information.
     *
     * @param input
     * @return
     */
    public PublicInfoBundlePerson extractPublicInfo(Person input) {

        PublicInfoBundlePerson pib = new PublicInfoBundlePerson();

        pib.setTypeName("Person");
        pib.setShowAddMessageButton(false);
        //the Person object does not have a PACC Enabled field, 
        //but I figure that a person under 18 should probably not be revealed to the public.
        if (!input.isUnder18()) {

            PersonCoordinator pc = getPersonCoordinator();

            input = pc.anonymizePersonData(input);

            pib.setBundledPerson(input);

            pib.setPaccStatusMessage("Public access enabled");
            pib.setPaccEnabled(true);

            pib.setShowDetailsPageButton(true);
        } else {
            Person skeleton = new Person();
            skeleton.setPersonID(input.getPersonID());
            pib.setBundledPerson(skeleton);
            pib.setPaccStatusMessage("A public information bundle was found but public "
                    + "access was switched off by a code officer. Please contact your municipal office. ");

        }

        return pib;
    }

    /**
     * Bundles a PersonOccApplication into a
     * PublicInfoBundlePersonoccApplication by stripping out its private
     * information.
     *
     * @param input
     * @return
     */
    public PublicInfoBundlePersonOccApplication extractPublicInfo(PersonOccApplication input) {

        PublicInfoBundlePersonOccApplication pib = new PublicInfoBundlePersonOccApplication();

        pib.setTypeName("PersonOccApplication");
        pib.setShowAddMessageButton(false);

        //the Person object does not have a PACC Enabled field, 
        //but I figure that a person under 18 should probably not be revealed to the public.
        if (!input.isUnder18()) {

            PersonCoordinator pc = getPersonCoordinator();

            input = pc.anonymizePersonData(input);

            pib.setBundledPerson(input);

            pib.setPaccStatusMessage("Public access enabled");
            pib.setPaccEnabled(true);

            pib.setShowDetailsPageButton(true);
        } else {
            PersonOccApplication skeleton = new PersonOccApplication();
            skeleton.setPersonID(input.getPersonID());
            skeleton.setApplicationID(input.getApplicationID());
            pib.setBundledPerson(skeleton);
            pib.setPaccStatusMessage("A public information bundle was found but public "
                    + "access was switched off by a code officer. Please contact your municipal office. ");

        }

        return pib;
    }

    /**
     * Bundles a Property into a PublicInfoBundleProperty by stripping out its
     * private information.
     *
     * @param input
     * @return
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.EventException
     * @throws com.tcvcog.tcvce.domain.AuthorizationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.SearchException
     */
    public PublicInfoBundleProperty extractPublicInfo(Property input) 
            throws IntegrationException, 
            EventException, 
            AuthorizationException, 
            BObStatusException, 
            SearchException {
        PublicInfoBundleProperty pib = new PublicInfoBundleProperty();

        pib.setTypeName("Property");
        pib.setShowAddMessageButton(false);

        //Again, no PACC enabled field. Perhaps this will be a good enough filter for now?
        if (input.isActive()) {

            pib.setBundledProperty(input);

            ArrayList<PublicInfoBundlePropertyUnit> bundledUnits = new ArrayList<>();

            if (input.getUnitList() != null) {

                for (PropertyUnit unit : input.getUnitList()) {
                    bundledUnits.add(extractPublicInfo(unit));
                }
            }

            pib.setUnitList(bundledUnits);

            pib.setPaccStatusMessage("Public access enabled");
            pib.setPaccEnabled(true);

            pib.setShowDetailsPageButton(true);
        } else {
            Property skeleton = new Property();
            skeleton.setPropertyID(skeleton.getPropertyID());
            pib.setBundledProperty(skeleton);
            pib.setPaccStatusMessage("A public information bundle was found but public "
                    + "access was switched off by a code officer. Please contact your municipal office. ");

        }

        return pib;
    }

    /**
     * Bundles an OccPeriod into a PublicInfoBundleOccPeriod by stripping out
     * its private information. One of the more resource-intense extraction
     * methods, as it has to bundle several lists.
     *
     * @param input
     * @return
     * @throws IntegrationException
     * @throws BObStatusException
     * @throws SearchException
     */
    public PublicInfoBundleOccPeriod extractPublicInfo(OccPeriod input) throws IntegrationException, BObStatusException, SearchException {
        PublicInfoBundleOccPeriod pib = new PublicInfoBundleOccPeriod();

        pib.setTypeName("OccPeriod");
        pib.setShowAddMessageButton(false);
        
        //Again, no PACC enabled field. Perhaps this will be a good enough filter for now?
        if (input.isActive()) {
            OccupancyCoordinator oc = getOccupancyCoordinator();
            setPublicUser();
            OccPeriodDataHeavy heavy = oc.assembleOccPeriodDataHeavy(input, publicUser.getMyCredential());

            pib.setBundledPeriod(input);

            pib.setPaccStatusMessage("Public access enabled");
            pib.setPaccEnabled(true);
            
            ArrayList<PublicInfoBundlePerson> bundledPersons = new ArrayList<>();

            if (heavy.getPersonList() != null) {

                for (Person skeleton : heavy.getPersonList()) {

                    bundledPersons.add(extractPublicInfo(skeleton));

                }
            }

            ArrayList<PublicInfoBundleOccInspection> bundledInspections = new ArrayList<>();

            if (heavy.getInspectionList() != null) {
                for (OccInspection skeleton : (List<OccInspection>) heavy.getInspectionList()) {

                    bundledInspections.add(extractPublicInfo(skeleton));

                }
            }

            ArrayList<PublicInfoBundleFeeAssigned> bundledFees = new ArrayList<>();
            if (heavy.getFeeList() != null) {
                for (FeeAssigned skeleton : heavy.getFeeList()) {

                    bundledFees.add(extractPublicInfo(skeleton));

                }
            }

            ArrayList<PublicInfoBundlePayment> bundledPayments = new ArrayList<>();

            if (heavy.getPaymentList() != null) {
                for (Payment skeleton : heavy.getPaymentList()) {

                    bundledPayments.add(extractPublicInfo(skeleton));

                }
            }

            pib.setPersonList(bundledPersons);
            pib.setInspectionList(bundledInspections);
            pib.setFeeList(bundledFees);
            pib.setPaymentList(bundledPayments);

            pib.setShowDetailsPageButton(true);
        } else {
            OccPeriod skeleton = new OccPeriod();
            skeleton.setPeriodID(input.getPeriodID());
            pib.setBundledPeriod(skeleton);
            pib.setPaccStatusMessage("A public information bundle was found but public "
                    + "access was switched off by a code officer. Please contact your municipal office. ");

        }

        return pib;
    }

    /**
     * Bundles an OccInspection into a PublicInfoBundleOccPermitApplication by
     * stripping out its private information. One of the more resource-intense
     * extraction methods, as it has to bundle a list of OccPeriods.
     *
     * @param input
     * @return
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.EventException
     * @throws com.tcvcog.tcvce.domain.AuthorizationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.SearchException
     */
    public PublicInfoBundleOccPermitApplication extractPublicInfo(OccPermitApplication input)
            throws IntegrationException,
            EventException,
            AuthorizationException,
            BObStatusException,
            SearchException {
        PublicInfoBundleOccPermitApplication pib = new PublicInfoBundleOccPermitApplication();

        pib.setTypeName("OccPermitApplication");

        pib.setPacc(input.getPublicControlCode());
        pib.setPaccEnabled(input.isPaccEnabled());

        if (input.isPaccEnabled()) {

            pib.setBundledApplication(input);

            pib.setApplicationPropertyUnit(extractPublicInfo(input.getApplicationPropertyUnit()));
            pib.setApplicantPerson(extractPublicInfo(input.getApplicantPerson()));
            pib.setPreferredContact(extractPublicInfo(input.getPreferredContact()));
            pib.setConnectedPeriod(extractPublicInfo(input.getConnectedPeriod()));

            List<PublicInfoBundlePersonOccApplication> attachedPersonBundles = new ArrayList<>();

            for (PersonOccApplication p : input.getAttachedPersons()) {

                attachedPersonBundles.add(extractPublicInfo(p));

            }

            pib.setAttachedPersons(attachedPersonBundles);

            pib.setPaccStatusMessage("Public access enabled");

            pib.setShowAddMessageButton(true);
            pib.setShowDetailsPageButton(true);
        } else {
            OccPermitApplication skeleton = new OccPermitApplication();
            skeleton.setId(input.getId());
            pib.setBundledApplication(skeleton);
            pib.setPaccStatusMessage("A public information bundle was found but public "
                    + "access was switched off by a code officer. Please contact your municipal office. ");
            pib.setShowAddMessageButton(false);

        }

        return pib;
    }

    /**
     * Bundles an OccInspection into a PublicInfoBundleOccInspection by
     * stripping out its private information. One of the more resource-intense
     * extraction methods, as it has to bundle a list of OccPeriods.
     *
     * @param input
     * @return
     */
    public PublicInfoBundleOccInspection extractPublicInfo(OccInspection input) {
        PublicInfoBundleOccInspection pib = new PublicInfoBundleOccInspection();

        pib.setTypeName("OccInspection");
        pib.setPacc(input.getPacc());
        pib.setPaccEnabled(input.isEnablePacc());
        pib.setShowAddMessageButton(false);

        if (input.isEnablePacc()) {

            pib.setBundledInspection(input);

            pib.setPaccStatusMessage("Public access enabled");

            pib.setShowDetailsPageButton(true);
        } else {

            OccInspection skeleton = new OccInspection();
            skeleton.setInspectionID(input.getInspectionID());
            pib.setBundledInspection(skeleton);
            pib.setPaccStatusMessage("A public information bundle was found but public "
                    + "access was switched off by a code officer. Please contact your municipal office. ");

        }

        return pib;
    }

    /**
     * Bundles a FeeAssigned into a PublicInfoBundleFeeAssigned by stripping out
     * its private information.
     *
     * @param input
     * @return
     */
    public PublicInfoBundleFeeAssigned extractPublicInfo(FeeAssigned input) {
        PublicInfoBundleFeeAssigned pib = new PublicInfoBundleFeeAssigned();

        pib.setBundledFee(input);

        pib.setTypeName("FeeAssigned");
        pib.setPaccStatusMessage("Public access enabled");
        pib.setPaccEnabled(true);

        pib.setShowAddMessageButton(false);
        pib.setShowDetailsPageButton(true);

        return pib;
    }

    /**
     * Bundles a PropertyUnit into a PublicInfoBundlePropertyUnit by stripping
     * out its private information. One of the more resource-intense exportation
     * methods, as it has to export a list of OccPeriods.
     *
     * @param input
     * @return
     * @throws IntegrationException
     * @throws EventException
     * @throws AuthorizationException
     * @throws BObStatusException
     * @throws SearchException
     */
    public PublicInfoBundlePropertyUnit extractPublicInfo(PropertyUnit input)
            throws IntegrationException, EventException,
            AuthorizationException, BObStatusException, SearchException {
        PublicInfoBundlePropertyUnit pib = new PublicInfoBundlePropertyUnit();
        PropertyCoordinator pc = getPropertyCoordinator();
        setPublicUser();
        PropertyUnitDataHeavy heavyUnit = pc.getPropertyUnitWithLists(input, publicUser.getMyCredential());

        ArrayList<PublicInfoBundleOccPeriod> periodHorde = new ArrayList<>();

        if (heavyUnit.getPeriodList() != null) {

            for (OccPeriod period : heavyUnit.getPeriodList()) {
                periodHorde.add(extractPublicInfo(period));
            }
        }

        pib.setPeriodList(periodHorde);
        pib.setBundledUnit(input);

        pib.setTypeName("PropertyUnit");
        pib.setPaccStatusMessage("Public access enabled");
        pib.setPaccEnabled(true);

        pib.setShowAddMessageButton(false);
        pib.setShowDetailsPageButton(true);

        return pib;
    }

    /**
     * Bundles a EventCnF into a PublicInfoBundleEventCnF by stripping out its
     * private information. One of the more resource-intense exportation
     * methods.
     *
     * @param input
     * @return
     * @throws IntegrationException
     * @throws EventException
     * @throws AuthorizationException
     * @throws BObStatusException
     * @throws SearchException
     */
    public PublicInfoBundleEventCnF extractPublicInfo(EventCnF input)
            throws IntegrationException,
            EventException,
            AuthorizationException,
            BObStatusException,
            SearchException {
        PublicInfoBundleEventCnF pib = new PublicInfoBundleEventCnF();

        pib.setTypeName("EventCnF");
        pib.setShowAddMessageButton(false);

        if (input.getCategory().getUserRankMinimumToView() <= PUBLIC_VIEW_USER_RANK) {

            if (input.getDomain() == EventDomainEnum.CODE_ENFORCEMENT) {
                CaseCoordinator cc = getCaseCoordinator();
                CECase c = cc.cecase_getCECase(input.getCeCaseID());
                pib.setCaseManager(c.getCaseManager());
                pib.setCecaseID(c.getCaseID());
            } else if (input.getDomain() == EventDomainEnum.OCCUPANCY) {
                OccupancyCoordinator oc = getOccupancyCoordinator();
                OccPeriod period = oc.getOccPeriod(input.getOccPeriodID());
                pib.setCaseManager(period.getManager());
                pib.setPeriodID(period.getPeriodID());
            }

            ArrayList<PublicInfoBundlePerson> personHorde = new ArrayList<>();

            if (input.getPersonList() != null) {
                for (Person skeleton : input.getPersonList()) {

                    personHorde.add(extractPublicInfo(skeleton));

                }
            }

            pib.setPersonList(personHorde);

            pib.setBundledEvent(input);

            pib.setPaccStatusMessage("Public access enabled");
            pib.setPaccEnabled(true);

            pib.setShowDetailsPageButton(true);
        } else {
            EventCnF skeleton = new EventCnF();
            skeleton.setEventID(input.getEventID());
            pib.setBundledEvent(skeleton);
            pib.setPaccStatusMessage("A public information bundle was found but public "
                    + "access was switched off by a code officer. Please contact your municipal office. ");
        }

        return pib;
    }

    /**
     * Converts a bundled Payment to an unbundled Payment for internal use. This
     * method does check for changes.
     *
     * @param input
     * @return
     */
    public Payment export(PublicInfoBundlePayment input) {

        PaymentIntegrator pi = getPaymentIntegrator();

        Payment unbundled = input.getBundledPayment();

        try {
            //Checks to see if payment is new. 
            //If the get method throws an error, the payment probably doesn't exist
            Payment exportable = pi.getPayment(unbundled.getPaymentID());
            if (!unbundled.getNotes().contains("*")) {
                exportable.setNotes(unbundled.getNotes());
            }
            exportable.setDomain(unbundled.getDomain());
            return exportable;
        } catch (IntegrationException ex) {
            System.out.println("Exporting payment failed. Assuming exported payment is new, and could not be found in DB.");
            System.out.println("But here's the error message, just in case: " + ex.toString());
            return unbundled;
        }
    }

    /**
     * Converts a bundled FeeAssigned to an unbundled FeeAssigned for internal
     * use. Currently does not check for changes.
     *
     * @param input
     * @return
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public FeeAssigned export(PublicInfoBundleFeeAssigned input) throws IntegrationException {

        PaymentIntegrator pi = getPaymentIntegrator();

        FeeAssigned unbundled = input.getBundledFee();

        FeeAssigned exportable = pi.getFeeAssigned(unbundled.getAssignedFeeID(), unbundled.getDomain());

        ArrayList<Payment> skeletonHorde = new ArrayList<>();

        if (input.getPaymentList() != null) {

            for (PublicInfoBundlePayment bundle : input.getPaymentList()) {
                skeletonHorde.add(export(bundle));
            }

        }
        exportable.setPaymentList(skeletonHorde);
        return exportable;
    }

    /**
     * Converts a bundled PropertyUnit to an unbundled PropertyUnit for internal
     * use. Currently does check for changes. Uses the data-heavy class to
     * contain the necessary fields. One of the more resource-intense
     * exportation methods, as it has to export a list of OccPeriods.
     *
     * @param input
     * @return
     * @throws EventException
     * @throws AuthorizationException
     * @throws BObStatusException
     * @throws IntegrationException
     * @throws SearchException
     */
    public PropertyUnitDataHeavy export(PublicInfoBundlePropertyUnit input)
            throws EventException, AuthorizationException,
            BObStatusException, IntegrationException,
            SearchException {

        PropertyCoordinator pc = getPropertyCoordinator();

        PropertyUnitDataHeavy unbundled = new PropertyUnitDataHeavy(input.getBundledUnit());
        PropertyUnitDataHeavy exportable = new PropertyUnitDataHeavy();

        try {

            exportable = pc.getPropertyUnitWithLists(unbundled, publicUser.getMyCredential());

            exportable.setUnitNumber(unbundled.getUnitNumber());
            exportable.setNotes(unbundled.getNotes());
            if (!unbundled.getRentalNotes().contains("*")) {
                exportable.setRentalNotes(unbundled.getRentalNotes());
            }

        } catch (IntegrationException ex) {
            System.out.println("Exporting payment failed. Assuming exported property unit is new, and could not be found in DB.");
            System.out.println("But here's the error message, just in case: " + ex.toString());
            exportable = unbundled;

        }
        ArrayList<OccPeriod> skeletonHorde = new ArrayList<>();

        if (input.getPeriodList() != null) {

            for (PublicInfoBundleOccPeriod bundle : input.getPeriodList()) {
                skeletonHorde.add(export(bundle));
            }

        }
        exportable.setPeriodList(skeletonHorde);
        return exportable;
    }

    /**
     * Converts a bundled OccInspection to an unbundled OccInspection for
     * internal use. Currently does not check for changes.
     *
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @param input
     * @return
     *
     */
    public OccInspection export(PublicInfoBundleOccInspection input) throws IntegrationException {

        OccInspectionIntegrator oi = getOccInspectionIntegrator();
        OccInspection unbundled = input.getBundledInspection();

        return oi.getOccInspection(unbundled.getInspectionID());
    }

    /**
     * Converts a bundled PublicInfoBundleEventCnF to an unbundled OccInspection
     * for internal use. Currently does not check for changes. Uses the
     * data-heavy class to contain the necessary fields.
     *
     * @param input
     * @return
     * @throws IntegrationException
     * @throws EventException
     * @throws SearchException
     * @throws BObStatusException
     */
    public EventCnFPropUnitCasePeriodHeavy export(PublicInfoBundleEventCnF input) throws IntegrationException, EventException, SearchException, BObStatusException {

        EventCoordinator ec = getEventCoordinator();
        EventCnF unbundled = input.getBundledEvent();
        EventCnFPropUnitCasePeriodHeavy exportable;
        CaseCoordinator cc = getCaseCoordinator();
        OccupancyCoordinator oc = getOccupancyCoordinator();

        try {

            exportable = ec.assembleEventCnFPropUnitCasePeriodHeavy(ec.getEvent(unbundled.getEventID()));

        } catch (IntegrationException ex) {
            System.out.println("Exporting event failed. Assuming exported event is new, and could not be found in DB.");
            System.out.println("But here's the error message, just in case: " + ex.toString());
            exportable = new EventCnFPropUnitCasePeriodHeavy(unbundled);
        }

        if (unbundled.getDomain() == EventDomainEnum.CODE_ENFORCEMENT) {

            CECase ceLight = cc.cecase_getCECase(input.getCecaseID());

            exportable.setCecase(cc.cecase_assembleCECasePropertyUnitHeavy(ceLight));

        } else if (unbundled.getDomain() == EventDomainEnum.OCCUPANCY) {

            OccPeriod opLight = oc.getOccPeriod(input.getPeriodID());

            exportable.setPeriod(new OccPeriodPropertyUnitHeavy(opLight));
        }

        ArrayList<Person> personHorde = new ArrayList<>();

        if (input.getPersonList() != null) {

            for (PublicInfoBundlePerson skeleton : input.getPersonList()) {

                personHorde.add(export(skeleton));

            }

        }
        exportable.setPersonList(personHorde);

        return exportable;

    }

    /**
     * Converts a bundled PublicInfoBundleCECase to an unbundled CECase for
     * internal use. Currently does not check for changes. Uses the data-heavy
     * class to contain the necessary fields.
     *
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @param input
     * @return
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.SearchException
     */
    public CECaseDataHeavy export(PublicInfoBundleCECase input) throws IntegrationException, BObStatusException, SearchException {

        CaseCoordinator cc = getCaseCoordinator();
        setPublicUser();
        CECaseDataHeavy exportable = cc.cecase_assembleCECaseDataHeavy(cc.cecase_getCECase(input.getBundledCase().getCaseID()), publicUser);

        return exportable;

    }

    /**
     * Converts a bundled PublicInfoBundleCodeViolation to an unbundled
     * CodeViolation for internal use. Does not check for changes.
     *
     * @param input
     * @return
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public CodeViolation export(PublicInfoBundleCodeViolation input) throws IntegrationException {

        CaseCoordinator cc = getCaseCoordinator();
        CodeViolation exportable = cc.violation_getCodeViolation(input.getBundledViolation().getViolationID());

        return exportable;

    }

    /**
     * Converts a bundled PublicInfoBundleProperty to an unbundled Property for
     * internal use. Currently does check for changes.
     *
     * @param input
     * @return
     * @throws IntegrationException
     * @throws com.tcvcog.tcvce.domain.AuthorizationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.EventException
     * @throws com.tcvcog.tcvce.domain.SearchException
     */
    public Property export(PublicInfoBundleProperty input) throws IntegrationException, EventException, AuthorizationException, BObStatusException, SearchException {

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

        ArrayList<PropertyUnit> unitHorde = new ArrayList<>();

        if (input.getUnitList() != null) {

            for (PublicInfoBundlePropertyUnit skeleton : input.getUnitList()) {
                unitHorde.add(export(skeleton));
            }
        }

        exportable.setUnitList(unitHorde);

        return exportable;

    }

    /**
     * Converts a bundled PublicInfoBundleOccPeriod to an unbundled OccPeriod
     * for internal use. Currently does not check for changes. Uses the
     * data-heavy class to contain the necessary fields. One of the more
     * resource-intense exportation methods, as it has to export several lists.
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
        setPublicUser();
        OccPeriodDataHeavy exportable = oc.assembleOccPeriodDataHeavy(oc.getOccPeriod(unbundled.getPeriodID()), publicUser.getMyCredential());

        ArrayList<Person> skeletonHorde = new ArrayList<>();

        if (input.getPersonList() != null) {

            for (PublicInfoBundlePerson bundle : input.getPersonList()) {

                skeletonHorde.add(export(bundle));

            }
        }

        ArrayList<OccInspection> inspectionHorde = new ArrayList<>();

        if (input.getInspectionList() != null) {

            for (PublicInfoBundleOccInspection bundle : input.getInspectionList()) {

                inspectionHorde.add(export(bundle));

            }
        }

        ArrayList<MoneyOccPeriodFeeAssigned> feeHorde = new ArrayList<>();

        if (input.getFeeList() != null) {
            for (PublicInfoBundleFeeAssigned bundle : input.getFeeList()) {

                MoneyOccPeriodFeeAssigned temp = new MoneyOccPeriodFeeAssigned(export(bundle));

                temp.setOccPeriodID(exportable.getPeriodID());

                temp.setOccPeriodTypeID(exportable.getType().getTypeID());

                feeHorde.add(temp);

            }
        }

        ArrayList<MoneyOccPeriodFeePayment> paymentHorde = new ArrayList<>();

        if (input.getPaymentList() != null) {

            for (PublicInfoBundlePayment bundle : input.getPaymentList()) {

                paymentHorde.add(new MoneyOccPeriodFeePayment(export(bundle)));

            }
        }

        exportable.setPersonList(skeletonHorde);

        exportable.setInspectionList(inspectionHorde);

        exportable.setFeeList(feeHorde);

        exportable.setPaymentList(paymentHorde);

        return exportable;

    }

    /**
     * Converts a bundled PublicInfoBundlePerson to an unbundled Person for
     * internal use. Currently does check for changes.
     *
     * @param input
     * @return
     * @throws IntegrationException
     */
    public Person export(PublicInfoBundlePerson input) throws IntegrationException {
        PersonCoordinator pc = getPersonCoordinator();
        Person unbundled = input.getBundledPerson();

        Person exportable = pc.getPerson(unbundled.getPersonID());

        if (exportable == null) {

            //the person is new, so skip the comparison
            exportable = unbundled;

        } else {
            //fields are anonymized by being overwritten with asterisks. If these fields no longer contain asterisks,
            //then the field has been edited by the user.
            if (unbundled.getFirstName() != null && !unbundled.getFirstName().contains("*")) {
                exportable.setFirstName(unbundled.getFirstName());
            }

            if (unbundled.getLastName() != null && !unbundled.getLastName().contains("*")) {
                exportable.setLastName(unbundled.getLastName());
            }

            if (unbundled.getPhoneCell() != null && !unbundled.getPhoneCell().contains("*")) {
                exportable.setPhoneCell(unbundled.getPhoneCell());
            }

            if (unbundled.getPhoneHome() != null && !unbundled.getPhoneHome().contains("*")) {
                exportable.setPhoneHome(unbundled.getPhoneHome());
            }

            if (unbundled.getPhoneWork() != null && !unbundled.getPhoneWork().contains("*")) {
                exportable.setPhoneWork(unbundled.getPhoneWork());
            }

            if (unbundled.getEmail() != null && !unbundled.getEmail().contains("*")) {
                exportable.setEmail(unbundled.getEmail());
            }

            if (unbundled.getAddressStreet() != null && !unbundled.getAddressStreet().contains("*")) {
                exportable.setAddressStreet(unbundled.getAddressStreet());
            }

            if (unbundled.getAddressCity() != null && !unbundled.getAddressCity().contains("*")) {
                exportable.setAddressCity(unbundled.getAddressCity());
            }

            if (unbundled.getAddressZip() != null && !unbundled.getAddressZip().contains("*")) {
                exportable.setAddressZip(unbundled.getAddressZip());
            }

            if (unbundled.getAddressState() != null && !unbundled.getAddressState().contains("*")) {
                exportable.setAddressState(unbundled.getAddressState());
            }

            if (unbundled.getMailingAddressStreet() != null && !unbundled.getMailingAddressStreet().contains("*")) {
                exportable.setMailingAddressStreet(unbundled.getMailingAddressStreet());
            }

            if (unbundled.getMailingAddressThirdLine() != null && !unbundled.getMailingAddressThirdLine().contains("*")) {
                exportable.setMailingAddressThirdLine(unbundled.getMailingAddressThirdLine());
            }

            if (unbundled.getMailingAddressCity() != null && !unbundled.getMailingAddressCity().contains("*")) {
                exportable.setMailingAddressCity(unbundled.getMailingAddressCity());
            }

            if (unbundled.getMailingAddressZip() != null && !unbundled.getMailingAddressZip().contains("*")) {
                exportable.setMailingAddressZip(unbundled.getMailingAddressZip());
            }

            if (unbundled.getMailingAddressState() != null && !unbundled.getMailingAddressState().contains("*")) {
                exportable.setMailingAddressState(unbundled.getMailingAddressState());
            }

            //The public-facing Occ Permit Application saves applicationPersonType in the person type field
            //You should probably make sure to preserve the original person type if you need to do that.
            exportable.setPersonType(unbundled.getPersonType());

        }

        return exportable;
    }

    /**
     * Converts a bundled PublicInfoBundlePersonOccPeriods to an unbundled
     * PersonOccApplication for internal use. Currently does check for changes.
     *
     * @param input
     * @return
     * @throws IntegrationException
     */
    public PersonOccApplication export(PublicInfoBundlePersonOccApplication input) throws IntegrationException {
        PersonIntegrator pi = getPersonIntegrator();
        PersonOccApplication unbundled = input.getBundledPerson();

        PersonOccApplication exportable = pi.getPersonOccApplication(unbundled.getPersonID(), unbundled.getApplicationID());

        if (exportable == null) {

            //the person is new, so skip the comparison
            exportable = unbundled;

        } else {
            //fields are anonymized by being overwritten with asterisks. If these fields no longer contain asterisks,
            //then the field has been edited by the user.
            if (unbundled.getFirstName() != null && !unbundled.getFirstName().contains("*")) {
                exportable.setFirstName(unbundled.getFirstName());
            }

            if (unbundled.getLastName() != null && !unbundled.getLastName().contains("*")) {
                exportable.setLastName(unbundled.getLastName());
            }

            if (unbundled.getPhoneCell() != null && !unbundled.getPhoneCell().contains("*")) {
                exportable.setPhoneCell(unbundled.getPhoneCell());
            }

            if (unbundled.getPhoneHome() != null && !unbundled.getPhoneHome().contains("*")) {
                exportable.setPhoneHome(unbundled.getPhoneHome());
            }

            if (unbundled.getPhoneWork() != null && !unbundled.getPhoneWork().contains("*")) {
                exportable.setPhoneWork(unbundled.getPhoneWork());
            }

            if (unbundled.getEmail() != null && !unbundled.getEmail().contains("*")) {
                exportable.setEmail(unbundled.getEmail());
            }

            if (unbundled.getAddressStreet() != null && !unbundled.getAddressStreet().contains("*")) {
                exportable.setAddressStreet(unbundled.getAddressStreet());
            }

            if (unbundled.getAddressCity() != null && !unbundled.getAddressCity().contains("*")) {
                exportable.setAddressCity(unbundled.getAddressCity());
            }

            if (unbundled.getAddressZip() != null && !unbundled.getAddressZip().contains("*")) {
                exportable.setAddressZip(unbundled.getAddressZip());
            }

            if (unbundled.getAddressState() != null && !unbundled.getAddressState().contains("*")) {
                exportable.setAddressState(unbundled.getAddressState());
            }

            if (unbundled.getMailingAddressStreet() != null && !unbundled.getMailingAddressStreet().contains("*")) {
                exportable.setMailingAddressStreet(unbundled.getMailingAddressStreet());
            }

            if (unbundled.getMailingAddressThirdLine() != null && !unbundled.getMailingAddressThirdLine().contains("*")) {
                exportable.setMailingAddressThirdLine(unbundled.getMailingAddressThirdLine());
            }

            if (unbundled.getMailingAddressCity() != null && !unbundled.getMailingAddressCity().contains("*")) {
                exportable.setMailingAddressCity(unbundled.getMailingAddressCity());
            }

            if (unbundled.getMailingAddressZip() != null && !unbundled.getMailingAddressZip().contains("*")) {
                exportable.setMailingAddressZip(unbundled.getMailingAddressZip());
            }

            if (unbundled.getMailingAddressState() != null && !unbundled.getMailingAddressState().contains("*")) {
                exportable.setMailingAddressState(unbundled.getMailingAddressState());
            }

            //The public-facing Occ Permit Application saves applicationPersonType in the person type field
            //You should probably make sure to preserve the original person type if you need to do that.
            exportable.setPersonType(unbundled.getPersonType());

        }

        return exportable;
    }

    /**
     * Converts a bundled PublicInfoBundleOccPermitApplication to an unbundled
     * OccPermitApplication for internal use. Currently does check for changes.
     *
     * @param input
     * @return
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.EventException
     * @throws com.tcvcog.tcvce.domain.AuthorizationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.ViolationException
     * @throws com.tcvcog.tcvce.domain.SearchException
     */
    public OccPermitApplication export(PublicInfoBundleOccPermitApplication input)
            throws IntegrationException,
            EventException,
            AuthorizationException,
            BObStatusException,
            ViolationException,
            SearchException {

        OccupancyIntegrator oi = getOccupancyIntegrator();

        OccPermitApplication unbundled = input.getBundledApplication();
        OccPermitApplication exportable = oi.getOccPermitApplication(input.getBundledApplication().getId());

        exportable.setSubmissionNotes(unbundled.getSubmissionNotes());
        exportable.setExternalPublicNotes(unbundled.getExternalPublicNotes());

        exportable.setApplicationPropertyUnit(export(input.getApplicationPropertyUnit()));
        exportable.setApplicantPerson(export(input.getApplicantPerson()));
        exportable.setPreferredContact(export(input.getPreferredContact()));
        exportable.setConnectedPeriod(export(input.getConnectedPeriod()));

        ArrayList<PersonOccApplication> personHorde = new ArrayList<>();

        if (input.getAttachedPersons() != null) {

            for (PublicInfoBundlePersonOccApplication skeleton : input.getAttachedPersons()) {
                personHorde.add(export(skeleton));
            }
        }

        exportable.setAttachedPersons(personHorde);

        return exportable;

    }

    /**
     * Accepts a List of PublicInfoBundlePropertyUnit objects, validates the
     * input to make sure it is acceptable, sanitizes it, then tosses it back.
     *
     * @param input
     * @return
     * @throws BObStatusException if the input is not acceptable.
     */
    public List<PublicInfoBundlePropertyUnit> sanitizePublicPropertyUnitList(List<PublicInfoBundlePropertyUnit> input) throws BObStatusException {

        PropertyCoordinator pc = getPropertyCoordinator();

        int duplicateNums; //The above boolean is a flag to see if there is more than 1 of  Unit Number. The int to the left stores how many of a given number the loop below finds.

        if (input.isEmpty()) {
            throw new BObStatusException("Please add at least one unit.");
        }

        //use a numeric for loop instead of iterating through the objects so that we can store 
        //the sanitized units
        for (int index = 0; index < input.size(); index++) {
            duplicateNums = 0;

            for (PublicInfoBundlePropertyUnit secondUnit : input) {
                if (input.get(index).getBundledUnit().getUnitNumber().compareTo(secondUnit.getBundledUnit().getUnitNumber()) == 0) {
                    duplicateNums++;
                }
            }

            if (duplicateNums > 1) {
                throw new BObStatusException("Some Units have the same Number");
            }

            PublicInfoBundlePropertyUnit skeleton = input.get(index);

            PropertyUnit sanitary = pc.sanitizePropertyUnit(skeleton.getBundledUnit());
            //We must manually extract the sanitized fields as using the setBundledUnit 
            //method would overwrite some of the user's  changes.
            skeleton.getBundledUnit().setUnitNumber(sanitary.getUnitNumber());

            input.set(index, skeleton);
        }

        return input;

    }

    /**
     * Attaches a message to a bundle. TODO: this method uses the old HTML-style
     * notes, and does not attach messages to all bundle classes
     *
     * @param bundle
     * @param message
     * @throws IntegrationException
     * @throws com.tcvcog.tcvce.domain.EventException
     * @throws com.tcvcog.tcvce.domain.AuthorizationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.ViolationException
     * @throws com.tcvcog.tcvce.domain.SearchException
     */
    public void attachMessageToBundle(PublicInfoBundle bundle, String message) 
            throws IntegrationException, 
            EventException, 
            AuthorizationException, 
            BObStatusException, 
            ViolationException,
            SearchException {
        
        LocalDateTime current = LocalDateTime.now();
        StringBuilder sb = new StringBuilder();
        Property currentProp = null; //We will store the associated property so we can attach an event to it 
        
        //Stores the ID and class of the bundled object
        //so that we can attach them to the event description.
        int objectID = 0;
        String objectKind = "";
        
        //You'll see brackets in the switch below for each case.
        //These are so that each case has its own scope
        //For code readability and optimization purposes
        
        switch (bundle.getTypeName()) {
            
            case "CEAR": {
                CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();

                PublicInfoBundleCEActionRequest requestBundle = (PublicInfoBundleCEActionRequest) bundle;

                objectID = requestBundle.getBundledRequest().getRequestID();
                
                //Get the external notes currently in the database
                CEActionRequest dbRequest = ceari.getActionRequestByRequestID(objectID);
                
                String currentNotes = dbRequest.getPublicExternalNotes();
                
                if (currentNotes != null) {
                    sb.append(currentNotes);
                    sb.append("<br /><br />");
                }
                sb.append("CODE ENFORCEMENT REQUEST NOTE ADDED AT ");
                sb.append(getPrettyDate(current));
                sb.append("by public user: <br />");
                sb.append(message);
                sb.append("<br />");
                sb.append("***********************");

                System.out.println("PublicInfoCoordinator.attachMessageToBundle | message: " + sb.toString());

                ceari.attachMessageToCEActionRequest(requestBundle, sb.toString());
                
                currentProp = dbRequest.getRequestProperty();
                
                objectKind = "Code Enforcement Action Request";
                
            }
            break;
            //Nothing yet. Remember to set objectID and objectKind!
            case "CECASE": {

            }
            break;
            case "OccPermitApplication": {

                OccupancyIntegrator oi = getOccupancyIntegrator();

                PublicInfoBundleOccPermitApplication applicationBundle = (PublicInfoBundleOccPermitApplication) bundle;

                objectID = applicationBundle.getBundledApplication().getId();
                
                //Get the external notes currently in the database
                OccPermitApplication dbApplication = oi.getOccPermitApplication(objectID);
                
                String currentNotes = dbApplication.getExternalPublicNotes();
                
                if (currentNotes != null) {
                    sb.append(applicationBundle.getBundledApplication().getExternalPublicNotes());
                    sb.append("<br /><br />");
                }
                sb.append("APPLICATION NOTE ADDED AT ");
                sb.append(getPrettyDate(current));
                sb.append("by public user: <br />");
                sb.append(message);
                sb.append("<br />");
                sb.append("***********************");

                System.out.println("PublicInfoCoordinator.attachMessageToBundle | message: " + sb.toString());

                oi.attachMessageToOccPermitApplication(applicationBundle, sb.toString());
                
                PropertyIntegrator pi = getPropertyIntegrator();
                
                currentProp = pi.getProperty(dbApplication.getApplicationPropertyUnit().getPropertyID());
                
                objectKind = "Occupancy Permit Application";
                
            }
            break;
            
            default:
                break;
        }
        
        if(currentProp != null){
            PropertyCoordinator pc = getPropertyCoordinator();
            CaseCoordinator cc = getCaseCoordinator();
            EventCoordinator ec = getEventCoordinator();
            setPublicUser();
            PropertyDataHeavy heavyProp = pc.assemblePropertyDataHeavy(currentProp, publicUser);
            
            //determine the governing property info case and then assemble a CECaseDataHeavy with it
            CECaseDataHeavy propertyInfoCase = cc.cecase_assembleCECaseDataHeavy(pc.determineGoverningPropertyInfoCase(heavyProp, publicUser), publicUser);
            
            EventCnF ev = ec.initEvent(propertyInfoCase,
                    ec.getEventCategory(Integer.parseInt(
                            getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                                    .getString("publicnoteeventcatid"))));
            
            sb = new StringBuilder();
            sb.append(getResourceBundle(Constants.MESSAGE_TEXT)
                .getString("publicNoteEventDescription"));
            sb.append(" " + objectKind+ " ");
            sb.append(" ID#: (");
            sb.append(objectID);
            sb.append(")");
            
            ev.setDescription(sb.toString());
            
            ec.addEvent(ev, propertyInfoCase, publicUser);
            
        } else {
            System.out.println("PublicInfoCoordinator.attachMessageToBundle() | ERROR: The system tried to attach a message to a bundle "
                    + "that did not have a property attached to it. Look in this method for a comment explaining more.");
            //A property is needed to send an event to code officers so they know the public added
            //a message to the object. Please get the property for the associated object so we can create an event.
            throw new BObStatusException("PublicInfoCoordinator.attachMessageToBundle() | ERROR: The system tried to attach a message to a bundle "
                    + "that did not have a property attached to it. Look in this method for a comment explaining more.");
        }
        
    }
} // close class
