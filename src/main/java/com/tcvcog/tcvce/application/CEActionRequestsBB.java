/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CEActionRequestStatus;
import com.tcvcog.tcvce.entities.search.SearchParamsCEActionRequests;
import com.tcvcog.tcvce.integration.CEActionRequestIntegrator;
import com.tcvcog.tcvce.integration.EventIntegrator;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author sylvia
 */
public class CEActionRequestsBB extends BackingBeanUtils implements Serializable {
    
    private CEActionRequest selectedRequest;
    private List<CEActionRequest> requestList;
    private int requestListSize;
    
    private List<CEActionRequestStatus> statusList;
    private CEActionRequestStatus selectedStatus;
    
    
    private CEActionRequestStatus selectedChangeToStatus;
    
    private int ceCaseIDForConnection;
    
    private SearchParamsCEActionRequests searchParams;
    
    // search stuff
    
    public void updateRequestList(ActionEvent ev){
        requestList = null;
        System.out.println("ActionRequestManagebb.updateRequestList");
        
    }
    
    
    public void manageActionRequest(CEActionRequest req){
        System.out.println("ActionRequestManagebb.manageActionRequest req: " + req.getRequestID());
        selectedRequest = req;
        
    }

    /**
     * @return the ceCaseIDForConnection
     */
    public int getCeCaseIDForConnection() {
        return ceCaseIDForConnection;
    }

    /**
     * @param ceCaseIDForConnection the ceCaseIDForConnection to set
     */
    public void setCeCaseIDForConnection(int ceCaseIDForConnection) {
        this.ceCaseIDForConnection = ceCaseIDForConnection;
    }

    /**
     * @return the requestList
     */
    public List<CEActionRequest> getRequestList() {
        System.out.println("ActionRequestManageBB.getRequestList");
        
        CEActionRequestIntegrator ari = getcEActionRequestIntegrator();
        SearchParamsCEActionRequests spcear = getSearchParams();
        if(requestList == null || requestList.isEmpty()){
            System.out.println("CeActionRequestsBB.getUnlinkedRequestList | unlinkedrequests is null");
            try {
                requestList = ari.getCEActionRequestList(spcear);
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                                "Unable to load action requests due to an error in the Integration Module", ""));
            }
        }
        return requestList;
    }

    /**
     * @param requestList the requestList to set
     */
    public void setRequestList(List<CEActionRequest> requestList) {
        this.requestList = requestList;
    }

   
    
    
    
    public void connectActionRequestToCECase(){
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        // TODO: create event if the CEAR is connected to a CECASE
        EventIntegrator ei = getEventIntegrator();
        
        try {
            ceari.connectActionRequestToCECase(selectedRequest.getRequestID(), 
                    ceCaseIDForConnection, 
                    getFacesUser().getUserID());
            
            
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully linked action request ID: " 
                        + selectedRequest.getRequestID() 
                        + " to code enforcement case ID: " + ceCaseIDForConnection
                        + "\n REFRESH your page to see the changes reflected in the action list", ""));
            // force a list reset
            requestList = null;
            
            // create case sevent
            
        } catch (IntegrationException ex) {
            // thrown if the integrator cannot find a CECase to link
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Could not connect action request to case-- database integration error", ""));
        } catch (CaseLifecyleException ex) {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
        }
    }
    

    /**
     * Creates a new instance of ActionRequestManageBB
     */
    public CEActionRequestsBB() {
    
    }
    
    public String updateActionRequest(){
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        try {
            ceari.updateActionRequest(selectedRequest);
        } catch (IntegrationException ex) {
            
            
        }
        return "missionControl";
        
    }
    
    public void changeActionRequestStatus(ActionEvent ev){
        
        
        
    }
    
    public void searchForCEActionRequests(ActionEvent ev){
        
        
    }
    
    

    /**
     * @return the selectedRequest
     */
    public CEActionRequest getSelectedRequest() {
        selectedRequest = getSessionBean().getcEActionRequest();
        return selectedRequest;
    }

    /**
     * @param selectedRequest the selectedRequest to set
     */
    public void setSelectedRequest(CEActionRequest selectedRequest) {
        this.selectedRequest = selectedRequest;
    }

    /**
     * @return the statusList
     */
    public List<CEActionRequestStatus> getStatusList() {
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        if(statusList == null){
            try {
                statusList = ceari.getRequestStatusList();
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
        }
        return statusList;
    }

    /**
     * @return the selectedStatus
     */
    public CEActionRequestStatus getSelectedStatus() {
        return selectedStatus;
    }

    /**
     * @param statusList the statusList to set
     */
    public void setStatusList(List<CEActionRequestStatus> statusList) {
        this.statusList = statusList;
    }

    /**
     * @param selectedStatus the selectedStatus to set
     */
    public void setSelectedStatus(CEActionRequestStatus selectedStatus) {
        this.selectedStatus = selectedStatus;
    }

    /**
     * @return the searchParams
     */
    public SearchParamsCEActionRequests getSearchParams() {
        System.out.println("ActionRequestManageBB.getSearchparams");
        if(searchParams == null){
            System.out.println("ActionRequestManageBB.getSearchparams | params is null");
            SearchCoordinator sc = getSearchCoordinator();
            searchParams = sc.getDefaultSearchParamsCEActionRequests();
        }
        return searchParams;
    }

    /**
     * @param searchParams the searchParams to set
     */
    public void setSearchParams(SearchParamsCEActionRequests searchParams) {
        this.searchParams = searchParams;
    }

    /**
     * @return the selectedChangeToStatus
     */
    public CEActionRequestStatus getSelectedChangeToStatus() {
        return selectedChangeToStatus;
    }

    /**
     * @param selectedChangeToStatus the selectedChangeToStatus to set
     */
    public void setSelectedChangeToStatus(CEActionRequestStatus selectedChangeToStatus) {
        this.selectedChangeToStatus = selectedChangeToStatus;
    }

    /**
     * @return the requestListSize
     */
    public int getRequestListSize() {
        requestList = null;
        int ls = 0;
        if(!(getRequestList() == null)){
         ls = getRequestList().size();
        } 
        requestListSize = ls;
        return requestListSize;
    }

    /**
     * @param requestListSize the requestListSize to set
     */
    public void setRequestListSize(int requestListSize) {
        this.requestListSize = requestListSize;
    }
    
}
