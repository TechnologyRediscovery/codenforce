/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.DataCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.CaseLifecycleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CEActionRequestStatus;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.reports.ReportCEARList;
import com.tcvcog.tcvce.entities.search.Query;
import com.tcvcog.tcvce.entities.search.QueryCEAR;
import com.tcvcog.tcvce.entities.search.QueryCEAREnum;
import com.tcvcog.tcvce.entities.search.SearchParamsCEActionRequests;
import com.tcvcog.tcvce.integration.CEActionRequestIntegrator;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import org.primefaces.model.chart.DonutChartModel;

/**
 * Primary backing bean for managing all code enforcement action requests.
 * Contains four methods, each of which routes a selected request down a
 * workflow pathway and adjusts the request object's status accordingly.
 *
 * Also contains utility methods for manipulating and editing requests.
 *
 * @author Ellen Baskem
 */
public class CEActionRequestsBB extends BackingBeanUtils implements Serializable {

    private CEActionRequest selectedRequest;
    private List<CEActionRequest> requestList;
    private ReportCEARList reportConfig;

    private DonutChartModel requestReasonDonut;

    private List<CEActionRequestStatus> statusList;
    private CEActionRequestStatus selectedStatus;

    private List<QueryCEAR> queryList;
    private QueryCEAR selectedQueryCEAR;
    private SearchParamsCEActionRequests searchParams;

    private CEActionRequestStatus selectedChangeToStatus;
    private String invalidMessage;
    private String noViolationFoundMessage;

    private String internalMessageText;
    private String muniMessageText;
    private String publicMessageText;

    private Person selectedPersonForAttachment;

    private ArrayList<CECase> caseListForSelectedProperty;
    private String houseNumSearch;
    private String streetNameSearch;

    private CECase selectedCaseForAttachment;

    private Municipality muniForPropSwitchSearch;
    private Property propertyForPropSwitch;
    private List<Property> propertyList;

    private int ceCaseIDForConnection;
    private boolean disablePACCControl;
    private boolean disabledDueToRoutingNotAllowed;

    /**
     * Creates a new instance of ActionRequestManageBB
     */
    public CEActionRequestsBB() {

    }

    @PostConstruct
    public void initBean() {
        SearchCoordinator sc = getSearchCoordinator();

        QueryCEAR sessionQuery = getSessionBean().getQueryCEAR();

        selectedRequest = getSessionBean().getSessionCEAR();

        try {
            requestList = sc.runQuery(sessionQuery).getResults();
            if (selectedRequest == null && requestList.size() > 0) {
                selectedRequest = requestList.get(0);
                generateCEARReasonDonutModel();
            }
            selectedQueryCEAR = sessionQuery;
            searchParams = sessionQuery.getParmsList().get(0);
            queryList = sc.buildQueryCEARList(getSessionBean().getSessionUser(), getSessionBean().getSessionMuni());
        } catch (IntegrationException | AuthorizationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
        }
        
        CaseCoordinator cc = getCaseCoordinator();
        SearchCoordinator searchCoord = getSearchCoordinator();
        ReportCEARList rpt = cc.getInitializedReportConficCEARs(
                getSessionBean().getSessionUser(), getSessionBean().getSessionMuni());
        rpt.setPrintFullCEARQueue(false);
        try {
            QueryCEAR query = searchCoord.assembleQueryCEAR(
                                                QueryCEAREnum.CUSTOM, 
                                                getSessionBean().getSessionUser(), 
                                                getSessionBean().getSessionMuni(), 
                                                null);
            List<CEActionRequest> singleReqList = new ArrayList<>();
            if(selectedRequest != null){
                selectedRequest.setInsertPageBreakBefore(false);
                singleReqList.add(selectedRequest);
                query.addToResults(singleReqList);
            }
            query.setExecutionTimestamp(LocalDateTime.now());
            rpt.setBOBQuery(query);
            rpt.setGenerationTimestamp(LocalDateTime.now());
            rpt.setTitle("Code enforcement request");
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                             "Unable to build query, sorry!", ""));
        }
        reportConfig = rpt;
    }

    private void generateCEARReasonDonutModel() {
        DataCoordinator dc = getDataCoordinator();
        
        if(requestList != null && requestList.size() > 0){

            CaseCoordinator cc = getCaseCoordinator();
            DonutChartModel donut =  new DonutChartModel();

            donut.addCircle(dc.computeCountsByCEARReason(requestList));

            donut.setTitle("Requests by reason");
            donut.setLegendPosition("nw");
            donut.setShowDataLabels(true);
            
            requestReasonDonut = donut;
        } 
    }

    public void executeQuery(ActionEvent ev) {
        SearchCoordinator searchC = getSearchCoordinator();
        try {
            requestList = searchC.runQuery(selectedQueryCEAR).getResults();
            generateCEARReasonDonutModel();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                             "Your query completed with " + requestList.size() + " results!", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                             "Unable to query action requests, sorry", ""));
        } catch (AuthorizationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                             ex.getMessage(), ""));
        }

    }

    public void executeCustomQuery(ActionEvent ev) {
        CaseCoordinator cc = getCaseCoordinator();
        SearchCoordinator searchCoord = getSearchCoordinator();
        try {
            
            selectedQueryCEAR = searchCoord.assembleQueryCEAR(
                                                    QueryCEAREnum.CUSTOM,
                                                    getSessionBean().getSessionUser(), 
                                                    getSessionBean().getSessionMuni(), 
                                                    searchParams);
            requestList =searchCoord.runQuery(selectedQueryCEAR).getResults();
            
            generateCEARReasonDonutModel();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                             "Your query completed with " + requestList.size() + " results!", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                             "Unable to query action requests, sorry", ""));
        } catch (AuthorizationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                             ex.getMessage(), ""));
        }
    }

    public void prepareReportMultiCEAR(ActionEvent ev) {
        CaseCoordinator cc = getCaseCoordinator();
        SearchCoordinator searchCoord = getSearchCoordinator();
        
        ReportCEARList rpt = cc.getInitializedReportConficCEARs(
                getSessionBean().getSessionUser(), getSessionBean().getSessionMuni());
        
        rpt.setPrintFullCEARQueue(true);
        if (selectedQueryCEAR != null) {
            //go run the Query if it hasn't been yet
            if(!selectedQueryCEAR.isExecutedByIntegrator()){
                try {
                    selectedQueryCEAR = searchCoord.runQuery(selectedQueryCEAR);
                } catch (AuthorizationException | IntegrationException ex) {
                    System.out.println(ex);
                }
            }
            rpt.setTitle("Code enforcement requests: " + selectedQueryCEAR.getQueryName().getTitle());
            rpt.setNotes(selectedQueryCEAR.getQueryName().getDesc());
            rpt.setBOBQuery(selectedQueryCEAR);

        }
        reportConfig = rpt;
    }
    
  

    public String generateReportSingleCEAR() {
        getSessionBean().setSessionCEAR(selectedRequest);
        getSessionBean().setSessionReport(reportConfig);
        getSessionBean().setQueryCEAR(selectedQueryCEAR);
        return "reportCEARList";

    }
    
    public String generateReportMultiCEAR(){
        getSessionBean().setSessionCEAR(selectedRequest);
        
        // Not working
//        Collections.sort(reportConfig.getBOBQuery().getBOBResultList());
        
        // tell the first request in the list to not print a page break before itself
        reportConfig.getBOBQuery().getBOBResultList().get(0).setInsertPageBreakBefore(false);
        
        getSessionBean().setSessionReport(reportConfig);
        getSessionBean().setQueryCEAR(selectedQueryCEAR);
        return "reportCEARList";
    }

    public String path1CreateNewCaseAtProperty() {
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        SystemCoordinator sc = getSystemCoordinator();

        if (selectedRequest != null) {
            if (selectedRequest.getRequestProperty() != null) {
                getSessionBean().setSessionProperty(selectedRequest.getRequestProperty());
            }

            MessageBuilderParams mbp = new MessageBuilderParams();
            mbp.setUser(getSessionBean().getSessionUser());
            mbp.setExistingContent(selectedRequest.getPublicExternalNotes());
            mbp.setHeader(getResourceBundle(Constants.MESSAGE_TEXT).getString("attachedToCaseHeader"));
            mbp.setExplanation(getResourceBundle(Constants.MESSAGE_TEXT).getString("attachedToCaseExplanation"));
            mbp.setNewMessageContent("");

            selectedRequest.setPublicExternalNotes(sc.appendNoteBlock(mbp));

            // force the bean to go to the integrator and fetch a fresh, updated
            // list of action requests
            try {
                ceari.updateActionRequestNotes(selectedRequest);
            } catch (IntegrationException ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                 "Unable to update action request with case attachment notes", ""));
            }
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                             "Please select an action request from the table to open a new case", ""));
            return "";
        }

        updateSelectedRequestStatusWithBundleKey("actionRequestNewCaseStatusCode");

// This shelf will be checked by the case creation coordinator
        // and link the request to the new case so we don't lose track of it
        getSessionBean().setCeactionRequestForSubmission(selectedRequest);

        return "addNewCase";
    }

    public void path2UseSelectedCaseForAttachment(CECase c) {
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        selectedCaseForAttachment = c;
        try {
            ceari.connectActionRequestToCECase(selectedRequest.getRequestID(), selectedCaseForAttachment.getCaseID(), getSessionBean().getSessionUser().getUserID());
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Successfully connected action request ID " + selectedRequest.getRequestID()
                    + " to code enforcement case ID " + selectedCaseForAttachment.getCaseID(), ""));
        } catch (CaseLifecycleException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to connect request to case.",
                    getResourceBundle(Constants.MESSAGE_TEXT).getString("systemLevelError")));
        }
//          selectedRequest.
        selectedRequest.setCaseID(selectedCaseForAttachment.getCaseID());
        updateSelectedRequestStatusWithBundleKey("actionRequestExistingCaseStatusCode");
        // force a reload of request list
        requestList = null;
    }

    public void path3AttachInvalidMessage(ActionEvent ev) {
        SystemCoordinator sc = getSystemCoordinator();
        
        if (selectedRequest != null) {
            CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
            updateSelectedRequestStatusWithBundleKey("actionRequestInvalidStatusCode");

            // build message to document change
            MessageBuilderParams mcc = new MessageBuilderParams();
            mcc.setUser(getSessionBean().getSessionUser());
            mcc.setExistingContent(selectedRequest.getPublicExternalNotes());
            mcc.setHeader(getResourceBundle(Constants.MESSAGE_TEXT).getString("invalidActionRequestHeader"));
            mcc.setExplanation(getResourceBundle(Constants.MESSAGE_TEXT).getString("invalidActionRequestExplanation"));
            mcc.setNewMessageContent(invalidMessage);
            selectedRequest.setPublicExternalNotes(sc.appendNoteBlock(mcc));
            try {
                ceari.updateActionRequestNotes(selectedRequest);
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Public case note added to action request ID " + selectedRequest.getRequestID() + ".", ""));
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to write message to The Database",
                                "This is a system level error that must be corrected by a sys admin--sorries!."));
            }
        } else {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "You just tried to attach a message to a nonexistent request!",
                    "Choose the request to manage on the left, then click manage"));
        }
        requestList = null;
    }

    public void path4AttachNoViolationFoundMessage(ActionEvent ev) {
        SystemCoordinator sc = getSystemCoordinator();
        if (selectedRequest != null) {

            CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();

            updateSelectedRequestStatusWithBundleKey("actionRequestNoViolationStatusCode");

            // build message to document change
            MessageBuilderParams mbp = new MessageBuilderParams();
            mbp.setUser(getSessionBean().getSessionUser());
            mbp.setExistingContent(selectedRequest.getPublicExternalNotes());
            mbp.setHeader(getResourceBundle(Constants.MESSAGE_TEXT).getString("noViolationFoundHeader"));
            mbp.setExplanation(getResourceBundle(Constants.MESSAGE_TEXT).getString("noViolationFoundExplanation"));
            mbp.setNewMessageContent(noViolationFoundMessage);

            selectedRequest.setPublicExternalNotes(sc.appendNoteBlock(mbp));

            // force the bean to go to the integrator and fetch a fresh, updated
            // list of action requests
            requestList = null;
            try {
                ceari.updateActionRequestNotes(selectedRequest);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Public case note added to action request ID " + selectedRequest.getRequestID() + ".", ""));

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
                            "No request selected!",
                            "Choose the request to manage on the left, then click manage"));
        }
    }

    private void updateSelectedRequestStatusWithBundleKey(String newStatusKey) {
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();

        try {
            selectedRequest.setRequestStatus(ceari.getRequestStatus(Integer.parseInt(
                    getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                            .getString(newStatusKey))));
            ceari.updateActionRequestStatus(selectedRequest);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Status changed on request ID " + selectedRequest.getRequestID(), ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to change request status",
                            "This is a system level error that must be corrected by a sys admin--sorries!."));
        }
    }

    public void updateRequestList(ActionEvent ev) {
        requestList = null;
        System.out.println("ActionRequestManagebb.updateRequestList");
    }

    public void searchForProperties(ActionEvent ev) {

        PropertyIntegrator pi = getPropertyIntegrator();
        try {
            propertyList = pi.searchForProperties(houseNumSearch, streetNameSearch, muniForPropSwitchSearch.getMuniCode());
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Your search completed with " + propertyList.size() + " results", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                     "Unable to complete a property search! Sorry!",
                     getResourceBundle(Constants.MESSAGE_TEXT).getString("systemLevelError")));
        }

    }

    public void updateRequestProperty(ActionEvent ev) {
        SystemCoordinator sc = getSystemCoordinator();
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();

        Property formerProp = selectedRequest.getRequestProperty();
        selectedRequest.setRequestProperty(propertyForPropSwitch);

        try {
            ceari.updateActionRequestProperty(selectedRequest);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Done! Property udpate for request ID " + selectedRequest.getRequestID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                     "Unable to change request property, sorry!",
                     getResourceBundle(Constants.MESSAGE_TEXT).getString("systemLevelError")));
        }

        StringBuilder sb = new StringBuilder();
        if (formerProp != null) {
            sb.append("Previous address: ");
            sb.append(formerProp.getAddress());
            sb.append(" (");
            sb.append(formerProp.getMuni().getMuniName());
            sb.append(")");
            sb.append("New address: ");
            sb.append(selectedRequest.getRequestProperty().getAddress());
            sb.append(" (");
            sb.append(selectedRequest.getRequestProperty().getMuni().getMuniName());
            sb.append(")");
        } else {
            sb.append(getResourceBundle(Constants.MESSAGE_TEXT).getString("noPreviousAddress"));
        }

        MessageBuilderParams mbp = new MessageBuilderParams();
        mbp.setUser(getSessionBean().getSessionUser());
        mbp.setExistingContent(selectedRequest.getPublicExternalNotes());
        mbp.setHeader(getResourceBundle(Constants.MESSAGE_TEXT).getString("propertyChangedHeader"));
        mbp.setExplanation(getResourceBundle(Constants.MESSAGE_TEXT).getString("propertyChangedExplanation"));
        mbp.setNewMessageContent(sb.toString());

        selectedRequest.setPublicExternalNotes(sc.appendNoteBlock(mbp));
        try {
            ceari.updateActionRequestNotes(selectedRequest);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Automatic case note generated for property update", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                     "Unable to add property change note to public listing, sorry!",
                     getResourceBundle(Constants.MESSAGE_TEXT).getString("systemLevelError")));
        }
        // force table reload to show changes
        requestList = null;
    }

    public void selectNewRequestPerson(Person p) {
        selectedPersonForAttachment = p;
    }

    public void updateRequestor(ActionEvent ev) {
        System.out.println("CEActionRequestsBB.updateRequestor");
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        selectedRequest.setRequestor(selectedPersonForAttachment);

        try {
            ceari.updateActionRequestor(selectedRequest);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Done! Requestor is now: "
                    + String.valueOf(selectedRequest.getRequestor().getFirstName())
                    + String.valueOf(selectedRequest.getRequestor().getLastName())
                    + " for action request ID: " + selectedRequest.getRequestID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                     "Unable to change requestor person",
                     getResourceBundle(Constants.MESSAGE_TEXT).getString("systemLevelError")));
        }
    }

    public void changePACCAccess() {
        System.out.println("CEActionRequestsBB.changePACCAccess");
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();

        try {
            ceari.updatePACCAccess(selectedRequest);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Done! Public access status is now: " + String.valueOf(selectedRequest.isPaccEnabled())
                    + " for action request ID: " + selectedRequest.getRequestID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                     "Unable to add change public access code status",
                     getResourceBundle(Constants.MESSAGE_TEXT).getString("systemLevelError")));
        }
    }

    public void attachInternalMessage(ActionEvent ev) {
        SystemCoordinator sc = getSystemCoordinator();
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        MessageBuilderParams mbp = new MessageBuilderParams();
        mbp.setUser(getSessionBean().getSessionUser());
        mbp.setExistingContent(selectedRequest.getCogInternalNotes());
        mbp.setHeader(getResourceBundle(Constants.MESSAGE_TEXT).getString("internalNote"));
        mbp.setExplanation("");
        mbp.setNewMessageContent(internalMessageText);
        String newNotes = sc.appendNoteBlock(mbp);
        System.out.println("CEActionRequestsBB.attachInternalMessage | msg before adding to request: " + newNotes);
        selectedRequest.setCogInternalNotes(newNotes);
        try {
            ceari.updateActionRequestNotes(selectedRequest);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                     "Done: added internal note to request ID " + selectedRequest.getRequestID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                     "Unable to update notes, sorry!",
                     getResourceBundle(Constants.MESSAGE_TEXT).getString("systemLevelError")));
        }

    }

    public void attachMuniMessage(ActionEvent ev) {
        SystemCoordinator sc = getSystemCoordinator();

        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        MessageBuilderParams mbp = new MessageBuilderParams();
        mbp.setUser(getSessionBean().getSessionUser());
        mbp.setExistingContent(selectedRequest.getMuniNotes());
        mbp.setHeader(getResourceBundle(Constants.MESSAGE_TEXT).getString("muniNote"));
        mbp.setExplanation("");
        mbp.setNewMessageContent(muniMessageText);

        selectedRequest.setMuniNotes(sc.appendNoteBlock(mbp));
        try {
            ceari.updateActionRequestNotes(selectedRequest);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Done: Added a municipal-only notes to request ID " + selectedRequest.getRequestID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                     "Unable to update notes, sorry!",
                     getResourceBundle(Constants.MESSAGE_TEXT).getString("systemLevelError")));
        }

    }

    public void attachPublicMessage(ActionEvent ev) {

        SystemCoordinator sc = getSystemCoordinator();
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        MessageBuilderParams mbp = new MessageBuilderParams();
        mbp.setUser(getSessionBean().getSessionUser());
        mbp.setExistingContent(selectedRequest.getPublicExternalNotes());
        mbp.setHeader(getResourceBundle(Constants.MESSAGE_TEXT).getString("externalNote"));
        mbp.setExplanation("");
        mbp.setNewMessageContent(publicMessageText);

        selectedRequest.setPublicExternalNotes(sc.appendNoteBlock(mbp));
        try {
            ceari.updateActionRequestNotes(selectedRequest);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Done: Added a public note to request ID " + selectedRequest.getRequestID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                     "Unable to update notes, sorry!",
                     getResourceBundle(Constants.MESSAGE_TEXT).getString("systemLevelError")));
        }

    }
    
    public void deletePhoto(int blobID){
        // TODO: remove entry from linker tbale for deleted photos
        for(Integer bid : this.selectedRequest.getBlobIDList()){
            if(bid.compareTo(blobID) == 0){
                this.selectedRequest.getBlobIDList().remove(bid);
                break;
            }
        }
        try {
            getBlobCoordinator().deleteBlob(blobID);
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }

    public void manageActionRequest(CEActionRequest req) {
        System.out.println("ActionRequestManagebb.manageActionRequest req: " + req.getRequestID());
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                "You are now managing request ID: " + req.getRequestID(), ""));
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

    public void updateActionRequestStatus(ActionEvent ev) {
        System.out.println("updateStatus");
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        if (selectedChangeToStatus != null) {

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
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                             "Please select a request status from the drop-down box to proceed", ""));

        }
        // force a reload of request list
        requestList = null;

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
        if (statusList == null) {
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
        if (selectedRequest != null) {
            try {
                caseListForSelectedProperty = ci.getCECasesByProp(selectedRequest.getRequestProperty());
                System.out.println("CEActionRequestsBB.getCaseListForSelectedProperty | case list size: " + caseListForSelectedProperty.size());
            } catch (IntegrationException ex) {
                System.out.println(ex);
            } catch (CaseLifecycleException ex) {
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
     * @return the disabledDueToRoutingNotAllowed
     */
    public boolean getIsDisabledDueToRoutingNotAllowed() {
        CaseCoordinator cc = getCaseCoordinator();

        disabledDueToRoutingNotAllowed
                = !(cc.determineCEActionRequestRoutingActionEnabledStatus(
                        selectedRequest,
                        getSessionBean().getSessionUser()));

        return disabledDueToRoutingNotAllowed;
    }

    /**
     * @param disabledDueToRoutingNotAllowed the disabledDueToRoutingNotAllowed
     * to set
     */
    public void setDisabledDueToRoutingNotAllowed(boolean disabledDueToRoutingNotAllowed) {
        this.disabledDueToRoutingNotAllowed = disabledDueToRoutingNotAllowed;
    }

    /**
     * @return the internalMessageText
     */
    public String getInternalMessageText() {
        return internalMessageText;
    }

    /**
     * @return the muniMessageText
     */
    public String getMuniMessageText() {
        return muniMessageText;
    }

    /**
     * @return the publicMessageText
     */
    public String getPublicMessageText() {
        return publicMessageText;
    }

    /**
     * @param internalMessageText the internalMessageText to set
     */
    public void setInternalMessageText(String internalMessageText) {
        this.internalMessageText = internalMessageText;
    }

    /**
     * @param muniMessageText the muniMessageText to set
     */
    public void setMuniMessageText(String muniMessageText) {
        this.muniMessageText = muniMessageText;
    }

    /**
     * @param publicMessageText the publicMessageText to set
     */
    public void setPublicMessageText(String publicMessageText) {
        this.publicMessageText = publicMessageText;
    }

    /**
     * @return the muniForPropSwitchSearch
     */
    public Municipality getMuniForPropSwitchSearch() {
        return muniForPropSwitchSearch;
    }

    /**
     * @return the propertyForPropSwitch
     */
    public Property getPropertyForPropSwitch() {
        return propertyForPropSwitch;
    }

    /**
     * @param muniForPropSwitchSearch the muniForPropSwitchSearch to set
     */
    public void setMuniForPropSwitchSearch(Municipality muniForPropSwitchSearch) {
        this.muniForPropSwitchSearch = muniForPropSwitchSearch;
    }

    /**
     * @param propertyForPropSwitch the propertyForPropSwitch to set
     */
    public void setPropertyForPropSwitch(Property propertyForPropSwitch) {
        this.propertyForPropSwitch = propertyForPropSwitch;
    }

    /**
     * @return the propertyList
     */
    public List<Property> getPropertyList() {
        return propertyList;
    }

    /**
     * @param propertyList the propertyList to set
     */
    public void setPropertyList(List<Property> propertyList) {
        this.propertyList = propertyList;
    }

    /**
     * @return the houseNumSearch
     */
    public String getHouseNumSearch() {
        return houseNumSearch;
    }

    /**
     * @return the streetNameSearch
     */
    public String getStreetNameSearch() {
        return streetNameSearch;
    }

    /**
     * @param houseNumSearch the houseNumSearch to set
     */
    public void setHouseNumSearch(String houseNumSearch) {
        this.houseNumSearch = houseNumSearch;
    }

    /**
     * @param streetNameSearch the streetNameSearch to set()
     */
    public void setStreetNameSearch(String streetNameSearch) {
        this.streetNameSearch = streetNameSearch;
    }

    /**
     * @return the disablePACCControl
     */
    public boolean isDisablePACCControl() {
        disablePACCControl = false;
        if (getSessionBean().getSessionUser().getMyCredential().isHasMuniStaffPermissions() == false) {
            disablePACCControl = true;
        }
        return disablePACCControl;
    }

    /**
     * @param disablePACCControl the disablePACCControl to set
     */
    public void setDisablePACCControl(boolean disablePACCControl) {
        this.disablePACCControl = disablePACCControl;
    }

    /**
     * @return the selectedPersonForAttachment
     */
    public Person getSelectedPersonForAttachment() {
        return selectedPersonForAttachment;
    }

    /**
     * @param selectedPersonForAttachment the selectedPersonForAttachment to set
     */
    public void setSelectedPersonForAttachment(Person selectedPersonForAttachment) {
        this.selectedPersonForAttachment = selectedPersonForAttachment;
    }


    /**
     * @return the selectedQueryCEAR
     */
    public QueryCEAR getSelectedQueryCEAR() {
        return selectedQueryCEAR;
    }

    /**
     * @param selectedQueryCEAR the selectedQueryCEAR to set
     */
    public void setSelectedQueryCEAR(QueryCEAR selectedQueryCEAR) {
        this.selectedQueryCEAR = selectedQueryCEAR;
    }

    /**
     * @return the queryList
     */
    public List<QueryCEAR> getQueryList() {
        return queryList;
    }

    /**
     * @param queryList the queryList to set
     */
    public void setQueryList(List<QueryCEAR> queryList) {
        this.queryList = queryList;
    }

    /**
     * @return the requestList
     */
    public List<CEActionRequest> getRequestList() {
        return requestList;
    }

    /**
     * @param requestList the requestList to set
     */
    public void setRequestList(List<CEActionRequest> requestList) {
        this.requestList = requestList;
    }

    /**
     * @return the reportConfig
     */
    public ReportCEARList getReportConfig() {
        return reportConfig;
    }

    /**
     * @param reportConfig the reportConfig to set
     */
    public void setReportConfig(ReportCEARList reportConfig) {
        this.reportConfig = reportConfig;
    }

    /**
     * @return the requestReasonDonut
     */
    public DonutChartModel getRequestReasonDonut() {
        if(requestReasonDonut == null){
            requestReasonDonut = new DonutChartModel();
        }
        return requestReasonDonut;
    }

    /**
     * @param requestReasonDonut the requestReasonDonut to set
     */
    public void setRequestReasonDonut(DonutChartModel requestReasonDonut) {
        this.requestReasonDonut = requestReasonDonut;
    }

}
