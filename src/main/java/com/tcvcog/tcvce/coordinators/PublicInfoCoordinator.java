/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.EventCECase;
import com.tcvcog.tcvce.entities.PublicInfoBundle;
import com.tcvcog.tcvce.entities.PublicInfoBundleCEActionRequest;
import com.tcvcog.tcvce.entities.PublicInfoBundleCECase;
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
     * The primary entry point for getting PACC data for any relevant object type
     * @param pacc the entered control code from the user
     * @return a linked list of info bundles from any source
     * @throws IntegrationException created by any of the interrogated integrator
     * classes that look for public info.
     */
    public List<PublicInfoBundle> getPublicInfoBundles(int pacc) throws IntegrationException{
        
        CaseIntegrator caseInt = getCaseIntegrator();
        List<CEActionRequest> requestList;
        
        // this list will store bundles from all sources polled in this method
        // go polymorphism!!
        List<PublicInfoBundle> infoBundleList = new ArrayList<>();
        
        // start with CE action requests
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
          
        requestList = ceari.getCEActionRequestByControlCode(pacc);
        
        for(CEActionRequest cear: requestList){
            PublicInfoBundleCEActionRequest bundle = extractPublicInfo(cear);
            
            // if the current action request is linked to a CECase,
            // go grab that case and check for allowed forward access
            // if forwardLinking is allowed, scrape public data from case and add
            if(cear.getCaseID() != 0){
                CECase caseFromActionRequest = caseInt.getCECase(cear.getCaseID());
                if(caseFromActionRequest.isAllowForwardLinkedPublicAccess()){
                    infoBundleList.add(extractPublicInfo(caseFromActionRequest));
                }
            }   
            infoBundleList.add(bundle);
        }
        

        // now go and get CECase bundles and add them to the list
        
        List<CECase> caseList = caseInt.getCECasesByPACC(pacc);
        System.out.println("PublicInfoCoordinator.getPublicInfoBundles | num CE cases found: " + caseList.size());
        
        for(CECase c: caseList){
            // let the extraction method deal with all the assembly logic
            // and access control issues
            infoBundleList.add(extractPublicInfo(c));
        }
          
        
        return infoBundleList;
    }
    
    private PublicInfoBundleCECase extractPublicInfo(CECase c){
        PublicInfoBundleCECase pib = new PublicInfoBundleCECase();
        pib.setCaseID(c.getCaseID());
        pib.setPacc(c.getPublicControlCode());
        pib.setTypeName("CECASE");
        pib.setMuni(c.getProperty().getMuni());
        if(c.isPaccEnabled()){
            pib.setCasePhase(c.getCasePhase());
            pib.setOriginiationDatePretty(getPrettyDate(c.getOriginationDate()));
            pib.setPropertyAddress(c.getProperty().getAddress());
            
            
            if(c.getClosingDate() != null){
                pib.setClosingDatePretty(getPrettyDate(c.getClosingDate()));
            } else {
                pib.setClosingDatePretty("(case is open)");
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append(c.getUser().getFName());
            sb.append(" ");
            sb.append(c.getUser().getLName());
            pib.setCaseManagerName(sb.toString());
            pib.setCaseManagerContact(c.getUser().getPhoneWork());
            
            
            pib.setPublicEventList(new ArrayList<EventCECase>());
            for(EventCECase ev: c.getEventList()){
                if(ev.isDiscloseToPublic()){
                    pib.getPublicEventList().add(ev);
                }
            }
            
            pib.setCountViolations(c.getViolationList().size());
            pib.setCountNoticeLetters(c.getNoticeList().size());
            pib.setCountCitations(c.getCitationList().size());
            pib.setShowDetailsPageButton(true);
            pib.setShowAddMessageButton(false);
            
            
            
        } else {
            pib.setPaccStatusMessage("This control code is associated with a "
                    + "code enforcement case but the case manager has not permitted "
                    + "pulic release of this information. Please contact your "
                    + "municipal staff at the contact info displayed here. ");
        }
        
        return pib;
        
        
    }
    
    private PublicInfoBundleCEActionRequest extractPublicInfo(CEActionRequest req){
        
        PublicInfoBundleCEActionRequest pib = new PublicInfoBundleCEActionRequest();
        
        pib.setRequestID(req.getRequestID());
        pib.setPacc(req.getRequestPublicCC());
        pib.setDateOfRecord(getPrettyDate(req.getDateOfRecord()));

        if(req.isPaccEnabled()){
            
            pib.setPaccStatusMessage("Public access enabled");
            pib.setAddressAssociated(!req.getNotAtAddress());
            if(!req.getNotAtAddress()){
                pib.setPropertyAddress(req.getRequestProperty().getAddress());
            }

            pib.setMuni(req.getMuni());

            // there's no case manager to attach to an unlinked action request


            // TODO: populate from text file
            pib.setTypeName("Code enforcement action request");

            pib.setActionRequestorFLname(req.getActionRequestorPerson().getFirstName() 
                                        + " " + req.getActionRequestorPerson().getLastName());

            pib.setIssueTypeString(req.getIssueTypeString());

            if(req.getCaseID() == 0){
                pib.setCaseLinkStatus("Request not linked to a code enforcement case");
                pib.setLinkedToCase(false);
            }
            else{
                pib.setCaseLinkStatus("Connected to case ID " + String.valueOf(req.getCaseID()));
                pib.setLinkedToCase(true);
            }

            pib.setFormattedSubmittedTimeStamp(req.getFormattedSubmittedTimeStamp());
            pib.setRequestDescription(req.getRequestDescription());
            pib.setPublicExternalNotes(req.getPublicExternalNotes());
            pib.setRequestStatus(req.getRequestStatus());

            pib.setShowAddMessageButton(true);
            pib.setShowDetailsPageButton(false);
        } else {
            pib.setPaccStatusMessage("A public information bundle was found but public "
                    + "access was switched off by a code officer. Please contact your municipal office at " + req.getMuni().getPhone());
            
        }
        
        return pib;
        
    }
    
    public void attachMessageToBundle(PublicInfoBundle bundle, String message) throws IntegrationException{
        LocalDateTime current = LocalDateTime.now();
        PublicInfoBundleCEActionRequest requestBundle;
        
        System.out.println("PublicInfoCoordinator.attachmessagToBundle: In coordinator");
        
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        if(bundle.getTypeName().equals("CEAR")){
            requestBundle = (PublicInfoBundleCEActionRequest) bundle;
            StringBuilder sb = new StringBuilder();
            sb.append(requestBundle.getPublicExternalNotes());
            sb.append("<br/><br/>");
            sb.append("PUBLIC CASE NOTE ADDED AT ");
            sb.append(current.toString());
            sb.append("<br/><br/>");
            sb.append(message);
            sb.append("<br/><br/>");
            System.out.println("PublicInfoCoordinator.attachmessagToBundle | message: " + sb.toString());
            ceari.attachMessageToCEActionRequest(requestBundle, sb.toString());
        } else if(bundle.getTypeName().equals("CECASE")){
            
        }
    }
} // close class
