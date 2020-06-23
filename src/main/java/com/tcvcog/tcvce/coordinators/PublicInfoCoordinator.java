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
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PublicInfoBundle;
import com.tcvcog.tcvce.entities.PublicInfoBundleCEActionRequest;
import com.tcvcog.tcvce.entities.PublicInfoBundleCECase;
import com.tcvcog.tcvce.entities.PublicInfoBundlePerson;
import com.tcvcog.tcvce.entities.PublicInfoBundleProperty;
import com.tcvcog.tcvce.integration.CEActionRequestIntegrator;
import com.tcvcog.tcvce.integration.CaseIntegrator;
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
            pib.setAddressAssociated(!req.getNotAtAddress());
            if (!req.getNotAtAddress()) {
                pib.setPropertyAddress(req.getRequestProperty().getAddress());
            }

            // there's no case manager to attach to an unlinked action request
            // TODO: populate from text file
            pib.setTypeName("Code enforcement action request");

            pib.setActionRequestorFLname(req.getRequestor().getFirstName()
                    + " " + req.getRequestor().getLastName());

//            pib.setIssueTypeString(req.getIssueTypeString());
            if (req.getCaseID() == 0) {
                pib.setCaseLinkStatus("Request not linked to a code enforcement case");
                pib.setLinkedToCase(false);
            } else {
                pib.setCaseLinkStatus("Connected to case ID " + String.valueOf(req.getCaseID()));
                pib.setLinkedToCase(true);
            }

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
            
            pib.setDateOfRecord(getPrettyDate(input.getCreationTimeStamp()));
            
            pib.setShowAddMessageButton(false);
            pib.setShowDetailsPageButton(true);
        } else {
            pib.setBundledPerson(new Person());
            pib.setPaccStatusMessage("A public information bundle was found but public "
                    + "access was switched off by a code officer. Please contact your municipal office. ");

        }

        return pib;
    }
    
    public PublicInfoBundleProperty extractPublicInfo(Property input){
        PublicInfoBundleProperty pib = new PublicInfoBundleProperty();
        
        return pib;
    }
    
    /**
     *Converts a bundled person to an unbundled person by fetching the original from the database
     * then seeing if any of the fields on the bundled version have been changed and bringing them over.
     * Some fields are not checked for changes because the public should not be able to change them anyway.
     * @param input
     * @return
     * @throws IntegrationException
     */
    public Person export(PublicInfoBundlePerson input) throws IntegrationException{
        PersonCoordinator pc = getPersonCoordinator();
        Person unbundled = input.getBundledPerson();
        
        Person exportable = pc.getPerson(unbundled.getPersonID());
        
        //fields are anonymized by being overwritten with asterisks. If these fields no longer contain asterisks,
        //then the field has been edited by the user.
        if(!unbundled.getFirstName().contains("*")){
            exportable.setFirstName(unbundled.getFirstName());
        }
        
        if(!unbundled.getLastName().contains("*")){
            exportable.setLastName(unbundled.getLastName());
        }
        
        if(!unbundled.getPhoneCell().contains("*")){
            exportable.setPhoneCell(unbundled.getPhoneCell());
        }
        
        if(!unbundled.getPhoneHome().contains("*")){
            exportable.setPhoneHome(unbundled.getPhoneHome());
        }
        
        if(!unbundled.getPhoneWork().contains("*")){
            exportable.setPhoneWork(unbundled.getPhoneWork());
        }
        
        if(!unbundled.getEmail().contains("*")){
            exportable.setEmail(unbundled.getEmail());
        }
        
        if(!unbundled.getAddressStreet().contains("*")){
            exportable.setAddressStreet(unbundled.getAddressStreet());
        }
        
        if(!unbundled.getAddressCity().contains("*")){
            exportable.setAddressCity(unbundled.getAddressCity());
        }
        
        if(!unbundled.getAddressZip().contains("*")){
            exportable.setAddressZip(unbundled.getAddressZip());
        }
        
        if(!unbundled.getAddressState().contains("*")){
            exportable.setAddressState(unbundled.getAddressState());
        }
        
        if(!unbundled.getMailingAddressStreet().contains("*")){
            exportable.setMailingAddressStreet(unbundled.getMailingAddressStreet());
        }
        
        if(!unbundled.getMailingAddressThirdLine().contains("*")){
            exportable.setMailingAddressThirdLine(unbundled.getMailingAddressThirdLine());
        }
        
        if(!unbundled.getMailingAddressCity().contains("*")){
            exportable.setMailingAddressCity(unbundled.getMailingAddressCity());
        }
        
        if(!unbundled.getMailingAddressZip().contains("*")){
            exportable.setMailingAddressZip(unbundled.getMailingAddressZip());
        }
        
        if(!unbundled.getMailingAddressState().contains("*")){
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
