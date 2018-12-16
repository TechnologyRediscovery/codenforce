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
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.search.SearchParamsCEActionRequests;
import com.tcvcog.tcvce.integration.CEActionRequestIntegrator;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.util.ArrayList;
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
    private String invalidMessage;
    private String noViolationFoundMessage;
    
    private ArrayList<CECase> caseListForSelectedProperty;
    private CECase selectedCaseForAttachment;
    
    private int ceCaseIDForConnection;
    
    private boolean actionsAllowedOnSelectedRequest;
    
    private SearchParamsCEActionRequests searchParams;
    
    // search stuff
    
    public void updateRequestList(ActionEvent ev){
        requestList = null;
        System.out.println("ActionRequestManagebb.updateRequestList");
        
    }
    
    public void useSelectedCaseForAttachment(CECase c){
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        selectedCaseForAttachment = c;
        try {
            ceari.connectActionRequestToCECase(selectedRequest.getRequestID(), selectedCaseForAttachment.getCaseID(), getFacesUser().getUserID() );
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Successfully connected action request ID " + selectedRequest.getRequestID() 
                                + " to code enforcement case ID " + selectedCaseForAttachment.getCaseID(), ""));
        } catch (CaseLifecyleException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to connect request to case.", 
                        "This is a system level error that must be corrected by a sys admin--sorries!"));
        }
        
    
        
    }
    
    
    public void manageActionRequest(CEActionRequest req){
        System.out.println("ActionRequestManagebb.manageActionRequest req: " + req.getRequestID());
        selectedRequest = req;
        
    }
    
    public void attachInvalidMessage(ActionEvent ev){
        if(selectedRequest != null){

            CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
            StringBuilder sb = new StringBuilder();
            sb.append(selectedRequest.getPublicExternalNotes());
            sb.append("<br/><br/>********************************<br/>");
            sb.append("Request marked INVALID BY ");
            sb.append(getFacesUser().getFName());
            sb.append(" ");
            sb.append(getFacesUser().getLName());
            sb.append(" at ");
            sb.append(getCurrentTimeStamp().toString());
            sb.append("<br/>");
            sb.append("********************************<br/>");
            sb.append(invalidMessage);
            sb.append("<br/><br/>");
            selectedRequest.setPublicExternalNotes(sb.toString());
            // force the bean to go to the integrator and fetch a fresh, updated
            // list of action requests
            requestList = null;
            try {
                ceari.updateActionRequestNotes(selectedRequest);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, 
                                "Public case note added to action request ID " + selectedRequest.getRequestID() + ".",""));

            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                                "Unable to write message to The Database", 
                                "This is a system level error that must be corrected by a sys admin--sorries!."));
            }
        } else {
            
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "You just tried to attach a message to a nonexistent request!", 
                            "Choose the request to manage on the left, then click manage"));
        }
        
        
    }
    
    public void attachNoViolationFoundMessage(ActionEvent ev){
        if(selectedRequest != null){

            CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
            StringBuilder sb = new StringBuilder();
            sb.append(selectedRequest.getPublicExternalNotes());
            sb.append("<br/><br/>********************************<br/>");
            sb.append("Request marked: NO VIOLATION FOUND by ");
            sb.append(getFacesUser().getFName());
            sb.append(" ");
            sb.append(getFacesUser().getLName());
            sb.append(" at ");
            sb.append(getCurrentTimeStamp().toString());
            sb.append("<br/>");
            sb.append("********************************<br/>");
            sb.append(noViolationFoundMessage);
            sb.append("<br/><br/>");
            selectedRequest.setPublicExternalNotes(sb.toString());
            // force the bean to go to the integrator and fetch a fresh, updated
            // list of action requests
            requestList = null;
            try {
                ceari.updateActionRequestNotes(selectedRequest);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, 
                                "Public case note added to action request ID " + selectedRequest.getRequestID() + ".",""));

            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                                "Unable to write message to The Database", 
                                "This is a system level error that must be corrected by a sys admin--sorries!."));
            }
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "You just tried to attach a message to a nonexistent request!", 
                            "Choose the request to manage on the left, then click manage"));
            
        }
        
        
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
    
    public void updateActionRequestStatus(ActionEvent ev){
        System.out.println("updateStatus");
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        if(selectedChangeToStatus != null){
            
            selectedRequest.setRequestStatus(selectedChangeToStatus);
            try {
                ceari.updateActionRequestStatus(selectedRequest);
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Successfully changed request status for request ID: " + selectedRequest.getCaseID(), ""));
            } catch (IntegrationException ex) {
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, 
                            "Unable to update request ID : " + selectedRequest.getCaseID(), ""));

            } 
        } else {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR
                        ,"Please select a request status from the drop-down box to proceed" , ""));
            
        }
    
    }
    
    public String createNewCaseAtProperty(){
        if(selectedRequest != null){
            if(selectedRequest.getRequestProperty() != null){
                getSessionBean().setActiveProp(selectedRequest.getRequestProperty());
            }
        } else {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR
                        ,"Please select an action request from the table to open a new case" , ""));
            return "";
            
        }
        
        return "addNewCase";
    }
    
    

    /**
     * @return the selectedRequest
     */
    public CEActionRequest getSelectedRequest() {

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

    /**
     * @return the invalidMessage
     */
    public String getInvalidMessage() {
        return invalidMessage;
    }

    /**
     * @param invalidMessage the invalidMessage to set
     */
    public void setInvalidMessage(String invalidMessage) {
        this.invalidMessage = invalidMessage;
    }

    /**
     * @return the caseListForSelectedProperty
     */
    public ArrayList<CECase> getCaseListForSelectedProperty() {
        CaseIntegrator ci = getCaseIntegrator();
        if(selectedRequest != null){
            try {
                caseListForSelectedProperty = ci.getCECasesByProp(selectedRequest.getRequestProperty());
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
        }
        return caseListForSelectedProperty;
    }

    /**
     * @param caseListForSelectedProperty the caseListForSelectedProperty to set
     */
    public void setCaseListForSelectedProperty(ArrayList<CECase> caseListForSelectedProperty) {
        this.caseListForSelectedProperty = caseListForSelectedProperty;
    }

    /**
     * @return the selectedCaseForAttachment
     */
    public CECase getSelectedCaseForAttachment() {
        return selectedCaseForAttachment;
    }

    /**
     * @param selectedCaseForAttachment the selectedCaseForAttachment to set
     */
    public void setSelectedCaseForAttachment(CECase selectedCaseForAttachment) {
        this.selectedCaseForAttachment = selectedCaseForAttachment;
    }

    /**
     * @return the noViolationFoundMessage
     */
    public String getNoViolationFoundMessage() {
        return noViolationFoundMessage;
    }

    /**
     * @param noViolationFoundMessage the noViolationFoundMessage to set
     */
    public void setNoViolationFoundMessage(String noViolationFoundMessage) {
        this.noViolationFoundMessage = noViolationFoundMessage;
    }

    /**
     * @return the actionsAllowedOnSelectedRequest
     */
    public boolean isActionsAllowedOnSelectedRequest() {
        actionsAllowedOnSelectedRequest = true;
        if(selectedRequest.getRequestStatus().getStatusID() == 
                Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("actionRequestInitialStatusCode"))){
            actionsAllowedOnSelectedRequest = false;
        }
        return actionsAllowedOnSelectedRequest;
    }

    /**
     * @param actionsAllowedOnSelectedRequest the actionsAllowedOnSelectedRequest to set
     */
    public void setActionsAllowedOnSelectedRequest(boolean actionsAllowedOnSelectedRequest) {
        this.actionsAllowedOnSelectedRequest = actionsAllowedOnSelectedRequest;
    }
    
}
