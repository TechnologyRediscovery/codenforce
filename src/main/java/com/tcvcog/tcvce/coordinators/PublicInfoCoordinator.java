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

        pib.setPaccStatusMessage("Code Enforcement Data Available");
        
        // TODO: populate from text file
        pib.setTypeName("Code Enforcement ACtion Request");
        
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
    
}
