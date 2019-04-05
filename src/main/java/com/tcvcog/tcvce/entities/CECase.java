/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import com.tcvcog.tcvce.integration.EventIntegrator;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sylvia Baskem
 */
public class CECase extends CECaseNoLists{
    
    
    private List<CodeViolation> violationList;
    private List<EventCECase> eventList;
    private List<EventCECase> eventListActionRequests;
    private List<Citation> citationList;
    private List<NoticeOfViolation> noticeList;
    private List<CEActionRequest> requestList;
    
    
    public CECase(){
        
    }

    /**
     * Constructor used to create an instance of this object with a
     * CECase without any lists. Transfers the member variables
     * from the incoming object to this sublcass
     * 
     * ** CONSTRUCTORS ARE NOT INHERITED!
     * 
     * @param cnl 
     */
    public CECase(CECaseNoLists cnl){
        this.caseID = cnl.caseID;
        this.publicControlCode = cnl.publicControlCode;
        this.paccEnabled = cnl.paccEnabled;
        this.allowForwardLinkedPublicAccess = cnl.allowForwardLinkedPublicAccess;
        this.property = cnl.property;
        this.propertyUnit = cnl.propertyUnit;
        this.caseManager = cnl.caseManager;
        this.caseName = cnl.caseName;
        this.casePhase = cnl.casePhase;
        this.casePhaseIcon = cnl.casePhaseIcon;
        this.originationDate = cnl.originationDate;
        this.closingDate = cnl.closingDate;
        this.creationTimestamp = cnl.creationTimestamp;
        this.notes = cnl.notes;
    }
    
    
    /**
     * @return the violationList
     */
    public List<CodeViolation> getViolationList() {
        return violationList;
    }

    /**
     * @param violationList the violationList to set
     */
    public void setViolationList(List<CodeViolation> violationList) {
        this.violationList = violationList;
    }

    /**
     * @return the eventList
     */
    public List<EventCECase> getEventList() {
        
        
        return eventList;
    }

    /**
     * @param eventList the eventList to set
     */
    public void setEventList(List<EventCECase> eventList) {
        this.eventList = eventList;
    }



    /**
     * @return the citationList
     */
    public List<Citation> getCitationList() {
        return citationList;
    }

    /**
     * @param citationList the citationList to set
     */
    public void setCitationList(List<Citation> citationList) {
        this.citationList = citationList;
    }


    /**
     * @return the noticeList
     */
    public List<NoticeOfViolation> getNoticeList() {
        return noticeList;
    }

    /**
     * @param noticeList the noticeList to set
     */
    public void setNoticeList(List<NoticeOfViolation> noticeList) {
        this.noticeList = noticeList;
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
     * @return the eventListActionRequests
     */
    public List<EventCECase> getEventListActionRequests() {
        
        
        return eventListActionRequests;
    }

    /**
     * @param eventListActionRequests the eventListActionRequests to set
     */
    public void setEventListActionRequests(List<EventCECase> eventListActionRequests) {
        this.eventListActionRequests = eventListActionRequests;
    }

  
    
    
    
    
}
