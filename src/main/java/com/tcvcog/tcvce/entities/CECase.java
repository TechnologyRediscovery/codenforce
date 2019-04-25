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
public class CECase extends CECaseBaseClass implements Cloneable{
    
    
    private List<CodeViolation> violationList;
    private List<CodeViolation> violationListResolved;
    private List<CodeViolation> violationListUnresolved;
    
    
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
     * ** but member variables and methods sure are!
     * 
     * @param cnl 
     */
    public CECase(CECaseBaseClass cnl){
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
     *
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    public CECase clone() throws CloneNotSupportedException{
        super.clone();
        return null;
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

    /**
     * @return the violationListUnresolved
     */
    public List<CodeViolation> getViolationListUnresolved() {
        
        violationListUnresolved = new ArrayList<>();
        if(violationList != null && violationList.size() > 0){
            for(CodeViolation v: violationList){
                if(v.getActualComplianceDate() == null){
                    violationListUnresolved.add(v);
                }
            }
        }
        

        return violationListUnresolved;
    }

    /**
     * @param violationListUnresolved the violationListUnresolved to set
     */
    public void setViolationListUnresolved(List<CodeViolation> violationListUnresolved) {
        this.violationListUnresolved = violationListUnresolved;
    }

    /**
     * @return the violationListResolved
     */
    public List<CodeViolation> getViolationListResolved() {
        violationListResolved = new ArrayList<>();
        if(violationList != null && violationList.size() > 0){
            for(CodeViolation v: violationList){
                if(v.getActualComplianceDate() != null){
                    violationListResolved.add(v);
                }
            }
        }
        
        return violationListResolved;
    }

    /**
     * @param violationListResolved the violationListResolved to set
     */
    public void setViolationListResolved(List<CodeViolation> violationListResolved) {
        this.violationListResolved = violationListResolved;
    }

  
    
    
    
    
}
