/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.DataCoordinator;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.BlobLight;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CEActionRequestStatus;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.reports.ReportCEARList;
import com.tcvcog.tcvce.entities.search.QueryCEAR;
import com.tcvcog.tcvce.entities.search.QueryCEAREnum;
import com.tcvcog.tcvce.entities.search.QueryProperty;
import com.tcvcog.tcvce.entities.search.QueryPropertyEnum;
import com.tcvcog.tcvce.entities.search.SearchParamsCEActionRequests;
import com.tcvcog.tcvce.entities.search.SearchParamsProperty;
import com.tcvcog.tcvce.integration.CEActionRequestIntegrator;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private String currentMode;
    private boolean currentCEARSelected;

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

    private List<CECase> caseListForSelectedProperty;
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
        CaseCoordinator cc = getCaseCoordinator();

        currentMode = "Search";

        //First search for CEARs using a standard query
        selectedQueryCEAR = sc.initQuery(QueryCEAREnum.UNPROCESSED, getSessionBean().getSessUser().getMyCredential());

        try {
            requestList = sc.runQuery(selectedQueryCEAR).getResults();

            //Update the selected request with the one from the database.
            if (getSessionBean().getSessCEAR() != null) {
                selectedRequest = cc.cear_getCEActionRequest(getSessionBean().getSessCEAR().getRequestID());
            }
        } catch (SearchException | IntegrationException ex) {
            System.out.println(ex);
        }
        
        //First try and get the current request from the session bean
        selectedRequest = getSessionBean().getSessCEAR();
        
        //If it's still null, just grab the first from the list.
        if (selectedRequest == null && requestList != null && requestList.size() > 0) {
            selectedRequest = requestList.get(0);
            
        }
        
        generateCEARReasonDonutModel();
        searchParams = new SearchParamsCEActionRequests();
        queryList = sc.buildQueryCEARList(getSessionBean().getSessUser().getMyCredential());

        ReportCEARList rpt = cc.report_getInitializedReportConficCEARs(
                getSessionBean().getSessUser(), getSessionBean().getSessMuni());

        rpt.setPrintFullCEARQueue(false);
        QueryCEAR query = sc.initQuery(
                QueryCEAREnum.CUSTOM,
                getSessionBean().getSessUser().getMyCredential());
        List<CEActionRequest> singleReqList = new ArrayList<>();
        if (selectedRequest != null) {
            selectedRequest.setInsertPageBreakBefore(false);
            singleReqList.add(selectedRequest);
            query.addToResults(singleReqList);
        }
        query.setExecutionTimestamp(LocalDateTime.now());
        rpt.setBOBQuery(query);
        rpt.setGenerationTimestamp(LocalDateTime.now());
        rpt.setTitle("Code enforcement request");

        reportConfig = rpt;
    }

    private void generateCEARReasonDonutModel() {
        DataCoordinator dc = getDataCoordinator();

        if (requestList != null && requestList.size() > 0) {

            CaseCoordinator cc = getCaseCoordinator();
            DonutChartModel donut = new DonutChartModel();

            donut.addCircle(dc.computeCountsByCEARReason(requestList));

            donut.setTitle("Requests by reason");
            donut.setLegendPosition("nw");
            donut.setShowDataLabels(true);

            requestReasonDonut = donut;
        }
    }

    public void executeQuery(ActionEvent ev) {
        SearchCoordinator searchC = getSearchCoordinator();

        selectedQueryCEAR = searchC.initQuery(selectedQueryCEAR.getQueryName(), getSessionBean().getSessUser().getMyCredential());

        requestList = new ArrayList<>();

        try {
            if (selectedQueryCEAR != null && !selectedQueryCEAR.getParamsList().isEmpty()) {

                searchC.runQuery(selectedQueryCEAR).getResults();

            }

            if (selectedQueryCEAR != null && !selectedQueryCEAR.getBOBResultList().isEmpty()) {

                requestList = selectedQueryCEAR.getBOBResultList();

            }
            generateCEARReasonDonutModel();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Your query completed with " + requestList.size() + " results!", ""));
        } catch (SearchException ex) {
            System.out.println("CEActionRequestsBB.executeQuery() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to query action requests, sorry", ""));
        }

    }

    public void executeCustomQuery(ActionEvent ev) {
        SearchCoordinator searchCoord = getSearchCoordinator();
        try {

            requestList = new ArrayList<>();

            selectedQueryCEAR = searchCoord.initQuery(QueryCEAREnum.CUSTOM, getSessionBean().getSessUser().getMyCredential());

            //We will manually grab each parameter so we don't also grab the existing SQL statements that are inside searchParams
            SearchParamsCEActionRequests queryParams = selectedQueryCEAR.getPrimaryParams();

            if (searchParams.getDate_start_val() == null || searchParams.getDate_end_val() == null) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Please specify a start and end date when using a custom query.", ""));
                return;
            }

            if (selectedQueryCEAR != null && !selectedQueryCEAR.getParamsList().isEmpty()) {

                queryParams.setDate_start_val(searchParams.getDate_start_val());

                queryParams.setDate_end_val(searchParams.getDate_end_val());

                queryParams.setCaseAttachment_ctl(searchParams.isCaseAttachment_ctl());

                queryParams.setCaseAttachment_val(searchParams.isCaseAttachment_val());

                queryParams.setUrgent_ctl(searchParams.isUrgent_ctl());

                queryParams.setUrgent_val(searchParams.isUrgent_val());

                queryParams.setRequestStatus_ctl(searchParams.isRequestStatus_ctl());

                queryParams.setRequestStatus_val(searchParams.getRequestStatus_val());

                queryParams.setNonaddressable_ctl(queryParams.isNonaddressable_ctl());

                queryParams.setNonaddressable_val(queryParams.isNonaddressable_val());
            } else {
                throw new SearchException("The query object was not properly initialized.");
            }
            //Time to run the query!

            if (selectedQueryCEAR != null && !selectedQueryCEAR.getParamsList().isEmpty()) {

                searchCoord.runQuery(selectedQueryCEAR).getResults();

            }

            if (selectedQueryCEAR != null && !selectedQueryCEAR.getBOBResultList().isEmpty()) {

                requestList = selectedQueryCEAR.getBOBResultList();

            }

            generateCEARReasonDonutModel();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Your query completed with " + requestList.size() + " results!", ""));
        } catch (SearchException ex) {
            System.out.println("CEActionRequestsBB.executeCustomQuery() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to query action requests, sorry", ""));
        }
    }

    /**
     * Determines whether or not a user should currently be able to select a
     * CEAR. Users should only select CEARs if they're in search mode.
     *
     * @return
     */
    public boolean getSelectedButtonActive() {
        return !"Search".equals(currentMode);
    }

    public boolean getActiveSearchMode() {
        return "Search".equals(currentMode);
    }

    public boolean getActiveActionsMode() {
        return "Actions".equals(currentMode);
    }

    public boolean getActiveObjectsMode() {
        return "Objects".equals(currentMode);
    }

    public boolean getActiveNotesMode() {
        return "Notes".equals(currentMode);
    }

    public String getCurrentMode() {
        return currentMode;
    }

    /**
     *
     * @param currentMode Search, Actions, Object, Notes
     */
    public void setCurrentMode(String currentMode) {

        //store currentMode into tempCurMode as a temporary value, in case the currenMode equal null
        String tempCurMode = this.currentMode;
        //reset default setting every time the Mode has been selected 
        defaultSetting();
        //check the currentMode == null or not
        if (currentMode == null) {
            this.currentMode = tempCurMode;
        } else {
            this.currentMode = currentMode;
        }
        //show the current mode in p:messages box
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, this.currentMode + " Mode Selected", ""));

    }

    public boolean isCurrentCEARSelected() {
        return currentCEARSelected;
    }

    public void setCurrentCEARSelected(boolean currentCEARSelected) {
        this.currentCEARSelected = currentCEARSelected;
    }

    public void defaultSetting() {
        currentCEARSelected = false;
    }

    public String goToCase() {

        CaseCoordinator cc = getCaseCoordinator();
        try {
            CECase cse = cc.cecase_getCECase(selectedRequest.getCaseID());

            getSessionBean().setSessCECase(cc.cecase_assembleCECaseDataHeavy(cse, getSessionBean().getSessUser()));

            return "ceCaseWorkflow";
        } catch (BObStatusException | IntegrationException | SearchException ex) {
            System.out.println("CEActionRequestsBB.goToCase() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occured while trying to redirect you to the CE Case Workflow.", ""));
            return "";
        }
    }

    public void prepareReportMultiCEAR(ActionEvent ev) {
        CaseCoordinator cc = getCaseCoordinator();
        SearchCoordinator searchCoord = getSearchCoordinator();

        ReportCEARList rpt = cc.report_getInitializedReportConficCEARs(
                getSessionBean().getSessUser(), getSessionBean().getSessMuni());

        rpt.setPrintFullCEARQueue(true);
        if (selectedQueryCEAR != null) {
            //go run the Query if it hasn't been yet
            if (selectedQueryCEAR.getExecutionTimestamp() == null) {
                try {
                    selectedQueryCEAR = searchCoord.runQuery(selectedQueryCEAR);
                } catch (SearchException ex) {
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
        getSessionBean().setSessCEAR(selectedRequest);
        getSessionBean().setSessReport(reportConfig);
        getSessionBean().setQueryCEAR(selectedQueryCEAR);
        return "reportCEARList";

    }

    public String generateReportMultiCEAR() {
        getSessionBean().setSessCEAR(selectedRequest);

        // Not working
//        Collections.sort(reportConfig.getBOBQuery().getBOBResultList());
        // tell the first request in the list to not print a page break before itself
        reportConfig.getBOBQuery().getBOBResultList().get(0).setInsertPageBreakBefore(false);

        getSessionBean().setSessReport(reportConfig);
        getSessionBean().setQueryCEAR(selectedQueryCEAR);
        return "reportCEARList";
    }

    public String path1CreateNewCaseAtProperty() {
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        SystemCoordinator sc = getSystemCoordinator();
        PropertyCoordinator pc = getPropertyCoordinator();

        if (selectedRequest != null) {
            try {
                if (selectedRequest.getRequestProperty() != null) {
                    getSessionBean().setSessProperty(pc.assemblePropertyDataHeavy(selectedRequest.getRequestProperty(), getSessionBean().getSessUser()));
                }

                MessageBuilderParams mbp = new MessageBuilderParams();
                mbp.setUser(getSessionBean().getSessUser());
                mbp.setExistingContent(selectedRequest.getPublicExternalNotes());
                mbp.setHeader(getResourceBundle(Constants.MESSAGE_TEXT).getString("attachedToCaseHeader"));
                mbp.setExplanation(getResourceBundle(Constants.MESSAGE_TEXT).getString("attachedToCaseExplanation"));
                mbp.setNewMessageContent("");

                selectedRequest.setPublicExternalNotes(sc.appendNoteBlock(mbp));

                // force the bean to go to the integrator and fetch a fresh, updated
                // list of action requests
                ceari.updateActionRequestNotes(selectedRequest);
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to update action request with case attachment notes", ""));
            } catch (BObStatusException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to create a new case at property due to a BobStatusException", ""));
            } catch (SearchException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to create a new case at property due to a SearchException", ""));
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
        getSessionBean().setSessCEAR(selectedRequest);

        //Here's a navstack to guide the user back after they add a case:
        getSessionBean().getNavStack().pushCurrentPage();

        return "addNewCase";
    }

    public void path2UseSelectedCaseForAttachment(CECase c) {
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        selectedCaseForAttachment = c;
        try {
            ceari.connectActionRequestToCECase(selectedRequest.getRequestID(), selectedCaseForAttachment.getCaseID(), getSessionBean().getSessUser().getUserID());
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Successfully connected action request ID " + selectedRequest.getRequestID()
                    + " to code enforcement case ID " + selectedCaseForAttachment.getCaseID(), ""));
        } catch (BObStatusException | IntegrationException ex) {
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
            mcc.setUser(getSessionBean().getSessUser());
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
            mbp.setUser(getSessionBean().getSessUser());
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
        SearchCoordinator sc = getSearchCoordinator();

        propertyList = new ArrayList<>();

        QueryProperty qp = null;

        try {
            qp = sc.initQuery(QueryPropertyEnum.HOUSESTREETNUM, getSessionBean().getSessUser().getMyCredential());

            if (muniForPropSwitchSearch == null) {

                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Please select a municipality first", ""));

            } else if (qp != null && !qp.getParamsList().isEmpty()) {
                SearchParamsProperty spp = qp.getPrimaryParams();
                spp.setAddress_ctl(true);
                spp.setAddress_val(houseNumSearch + " " + streetNameSearch);
                spp.setMuni_ctl(true);
                spp.setMuni_val(muniForPropSwitchSearch);
                spp.setLimitResultCount_ctl(true);
                spp.setLimitResultCount_val(20);

                sc.runQuery(qp);

            } else {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Something when wrong with the property search! Sorry!", ""));
            }

        } catch (SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Something when wrong with the property search! Sorry!", ""));
        }

        if (qp != null && !qp.getBOBResultList().isEmpty()) {
            propertyList = qp.getBOBResultList();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Your search completed with " + getPropertyList().size() + " results", ""));
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
        mbp.setUser(getSessionBean().getSessUser());
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
        mbp.setUser(getSessionBean().getSessUser());
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
        mbp.setUser(getSessionBean().getSessUser());
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
        mbp.setUser(getSessionBean().getSessUser());
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

    public void deletePhoto(int blobID) {
        for (Integer bid : this.selectedRequest.getBlobIDList()) {
            if (bid.compareTo(blobID) == 0) {
                this.selectedRequest.getBlobIDList().remove(bid);
                break;
            }
        }
        
        try {
            BlobLight target = getBlobIntegrator().getPhotoBlobLight(blobID);
            getBlobCoordinator().deletePhotoBlob(target);
        } catch (IntegrationException 
                | AuthorizationException 
                | BObStatusException 
                | BlobException 
                | ClassNotFoundException 
                | EventException 
                | IOException 
                | ViolationException ex) {
            System.out.println(ex);
        }
    }

    public void manageActionRequest(CEActionRequest req) {
        System.out.println("ActionRequestManagebb.manageActionRequest req: " + req.getRequestID());
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                "You are now managing request ID: " + req.getRequestID(), ""));
        selectedRequest = req;
        getSessionBean().setSessCEAR(req);

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
    public List<CECase> getCaseListForSelectedProperty() {
        CaseIntegrator ci = new CaseIntegrator();
        if (selectedRequest != null) {

            try {
                caseListForSelectedProperty = ci.getCECasesByProp(selectedRequest.getRequestProperty().getPropertyID());
                System.out.println("CEActionRequestsBB.getCaseListForSelectedProperty | case list size: " + caseListForSelectedProperty.size());
            } catch (IntegrationException | BObStatusException ex) {
                System.out.println(ex);
            }
        }
        return caseListForSelectedProperty;
    }

    /**
     * @param caseListForSelectedProperty the caseListForSelectedProperty to set
     */
    public void setCaseListForSelectedProperty(List<CECase> caseListForSelectedProperty) {
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
                = !(cc.cear_determineCEActionRequestRoutingActionEnabledStatus(
                        selectedRequest,
                        getSessionBean().getSessUser()));

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
     * @return if the user should not be able to change public access on an object
     */
    public boolean isDisablePACCControl() {
        disablePACCControl = false;
        if (getSessionBean().getSessUser().getMyCredential().isHasMuniStaffPermissions() == false) {
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
        if (requestReasonDonut == null) {
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
