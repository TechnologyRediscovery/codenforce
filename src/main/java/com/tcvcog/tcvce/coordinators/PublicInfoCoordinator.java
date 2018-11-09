/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.PublicInfoBundle;
import com.tcvcog.tcvce.entities.PublicInfoBundleCEActionRequest;
import com.tcvcog.tcvce.integration.CEActionRequestIntegrator;
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
    
    
    
    public List<PublicInfoBundle> getPublicInfoBundles(int pacc) throws IntegrationException{
        
        List<CEActionRequest> requestList;
        List<PublicInfoBundle> infoBundleList = new ArrayList<>();
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
          
        requestList = ceari.getCEActionRequestByControlCode(pacc);
        
        for(CEActionRequest cear: requestList){
            
            infoBundleList.add(extractPublicInfo(cear));
            
        }
        
        return infoBundleList;
        
    }
    
    private PublicInfoBundleCEActionRequest extractPublicInfo(CEActionRequest req){
        
        PublicInfoBundleCEActionRequest pib = new PublicInfoBundleCEActionRequest();
        
        pib.setRequestID(req.getRequestID());
        pib.setPacc(req.getRequestPublicCC());

        pib.setPaccStatusMessage("Public access enabled");
        pib.setAddressAssociated(!req.getNotAtAddress());
        if(!req.getNotAtAddress()){
            pib.setPropertyAddress(req.getRequestProperty().getAddress());
        }
        
        pib.setMuni(req.getMuni());
        
        // there's no case manager to attach to an unlinked action request
        
        
        
        
        // TODO: populate from text file
        pib.setTypeName("Code Enforcement Action Request");
        
        pib.setActionRequestorFLname(req.getActionRequestorPerson().getFirstName() 
                                    + " " + req.getActionRequestorPerson().getLastName());
        
        pib.setIssueTypeString(req.getIssueTypeString());
        
        if(req.getCaseID() == 0) pib.setCaseLinkStatus("No linked case available");
        else pib.setCaseLinkStatus("Connected to case ID " + String.valueOf(req.getCaseID()));
        
        pib.setFormattedSubmittedTimeStamp(req.getFormattedSubmittedTimeStamp());
        pib.setRequestDescription(req.getRequestDescription());
        pib.setPublicExternalNotes(req.getPublicExternalNotes());
        
        return pib;
        
    }
    
    public void attachMessageToBundle(PublicInfoBundle bundle, String message) throws IntegrationException{
        
        LocalDateTime current = LocalDateTime.now();
        
        
        PublicInfoBundleCEActionRequest requestBundle;
        requestBundle = (PublicInfoBundleCEActionRequest) bundle;
        
        System.out.println("PublicInfoCoordinator.attachmessagToBundle: In coordinator");
        
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        if(bundle instanceof PublicInfoBundleCEActionRequest){
            System.out.println("PublicInfoCoordinator.attachmessagToBundle: Found CEActionBundle");
            StringBuilder sb = new StringBuilder();
            sb.append(requestBundle.getPublicExternalNotes());
            sb.append("<br/><br/>");
            sb.append("PUBLIC CASE NOTE ADDED AT ");
            sb.append(current.toString());
            sb.append(message);
            ceari.attachMessageToCEActionRequest(requestBundle, message);
            
        }
        
        
        
    }
    
    
    
}
