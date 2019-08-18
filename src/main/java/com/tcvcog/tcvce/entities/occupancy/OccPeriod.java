/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.entities.BOBSource;
import com.tcvcog.tcvce.entities.CECaseEvent;
import com.tcvcog.tcvce.entities.EntityUtils;
import com.tcvcog.tcvce.entities.Proposal;
import com.tcvcog.tcvce.entities.EventRuleAbstract;
import com.tcvcog.tcvce.entities.EventRuleOccPeriod;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonOccPeriod;
import com.tcvcog.tcvce.entities.ProposalOccPeriod;
import com.tcvcog.tcvce.entities.User;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.tcvcog.tcvce.entities.Openable;
import java.util.Collections;

/**
 *
 * @author sylvia
 */
public class OccPeriod 
        extends EntityUtils 
        implements  Serializable,
                    Openable{
    
    private int periodID;
    private int propertyUnitID;
    private OccPeriodType type;
    private OccPeriodStatusEnum status;
    
    private List<OccPermitApplication> applicationList;
    private List<PersonOccPeriod> personList;
    private List<OccEvent> eventList;
    private boolean showHiddenEvents;
    private boolean showInactiveEvents;
    
    private List<Proposal> proposalList;
    private List<EventRuleOccPeriod> eventRuleOccPeriodList;
    
    private List<OccInspection> inspectionList;
    private List<OccPermit> permitList;
    private List<Integer> blobIDList;
    
    private User manager;
     
    private User periodTypeCertifiedBy;
    private LocalDateTime periodTypeCertifiedTS;
    
    private BOBSource source;
    private User createdBy;
    private LocalDateTime createdTS;
    
    private LocalDateTime startDate;
    private java.util.Date startDateUtilDate;
    private LocalDateTime startDateCertifiedTS;
    private User startDateCertifiedBy;
    
    private LocalDateTime endDate;
    private java.util.Date endDateUtilDate;
    private LocalDateTime endDateCertifiedTS;
    private User endDateCertifiedBy;
    
    private LocalDateTime authorizedTS;
    private User authorizedBy;
    
    private boolean overrideTypeConfig;
    
    private String notes;
    
     @Override
    public boolean isOpen() {
        return status.isOpenPeriod();
    }

    public List<OccEvent> getVisibleEventList(){
        List<OccEvent> visEventList = new ArrayList<>();
        for (OccEvent ev : eventList) {
            if (!ev.isActive() && !isShowInactiveEvents()) {
                continue;
            }
            if (ev.isHidden() && !isShowHiddenEvents()) {
                continue;
            }
            visEventList.add(ev);
        } // close for   
        return visEventList;
        
    }
    
      public List<OccEvent> getActiveEventList() {
        List<OccEvent> actEvList = new ArrayList<>();
            Iterator<OccEvent> iter = eventList.iterator();
                while(iter.hasNext()){
                    OccEvent ev = iter.next();
                    if(ev.isActive()){
                        actEvList.add(ev);
                    }
                }
        return actEvList;
    }
      
    public OccInspection determineGoverningOccInspection(){
        OccInspection selIns = null;
        Collections.sort(inspectionList);
        // logic for determining the currentOccInspection
        if(inspectionList != null){
            if(inspectionList.size() == 1){
                return inspectionList.get(0);
            } else {
                for(OccInspection ins: inspectionList){
                    if(ins.isActive()){
                        selIns = ins;
                    }
                }
            }
        }
        return selIns;
    }
    
    /**
     * @return the periodID
     */
    public int getPeriodID() {
        return periodID;
    }

    /**
     * @return the propertyUnitID
     */
    public int getPropertyUnitID() {
        return propertyUnitID;
    }

    /**
     * @return the applicationList
     */
    public List<OccPermitApplication> getApplicationList() {
        return applicationList;
    }

    /**
     * @return the personList
     */
    public List<PersonOccPeriod> getPersonList() {
        return personList;
    }

    /**
     * @return the eventList
     */
    public List<OccEvent> getEventList() {
        return eventList;
    }

    /**
     * @return the proposalList
     */
    public List<Proposal> getProposalList() {
        return proposalList;
    }

    /**
     * @return the inspectionList
     */
    public List<OccInspection> getInspectionList() {
        return inspectionList;
    }

    /**
     * @return the permitList
     */
    public List<OccPermit> getPermitList() {
        return permitList;
    }

    /**
     * @return the blobIDList
     */
    public List<Integer> getBlobIDList() {
        return blobIDList;
    }

    /**
     * @return the manager
     */
    public User getManager() {
        return manager;
    }

    /**
     * @return the type
     */
    public OccPeriodType getType() {
        return type;
    }

    /**
     * @return the periodTypeCertifiedBy
     */
    public User getPeriodTypeCertifiedBy() {
        return periodTypeCertifiedBy;
    }

    /**
     * @return the periodTypeCertifiedTS
     */
    public LocalDateTime getPeriodTypeCertifiedTS() {
        return periodTypeCertifiedTS;
    }

    /**
     * @return the source
     */
    public BOBSource getSource() {
        return source;
    }

    /**
     * @return the createdBy
     */
    public User getCreatedBy() {
        return createdBy;
    }

    /**
     * @return the createdTS
     */
    public LocalDateTime getCreatedTS() {
        return createdTS;
    }

    /**
     * @return the startDate
     */
    public LocalDateTime getStartDate() {
        return startDate;
    }

    /**
     * @return the startDateCertifiedTS
     */
    public LocalDateTime getStartDateCertifiedTS() {
        return startDateCertifiedTS;
    }

    /**
     * @return the startDateCertifiedBy
     */
    public User getStartDateCertifiedBy() {
        return startDateCertifiedBy;
    }

    /**
     * @return the endDate
     */
    public LocalDateTime getEndDate() {
        return endDate;
    }

    /**
     * @return the endDateCertifiedTS
     */
    public LocalDateTime getEndDateCertifiedTS() {
        return endDateCertifiedTS;
    }

    /**
     * @return the endDateCertifiedBy
     */
    public User getEndDateCertifiedBy() {
        return endDateCertifiedBy;
    }

    /**
     * @return the authorizedTS
     */
    public LocalDateTime getAuthorizedTS() {
        return authorizedTS;
    }

    /**
     * @return the authorizedBy
     */
    public User getAuthorizedBy() {
        return authorizedBy;
    }

    /**
     * @return the overrideTypeConfig
     */
    public boolean isOverrideTypeConfig() {
        return overrideTypeConfig;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param periodID the periodID to set
     */
    public void setPeriodID(int periodID) {
        this.periodID = periodID;
    }

    /**
     * @param propertyUnitID the propertyUnitID to set
     */
    public void setPropertyUnitID(int propertyUnitID) {
        this.propertyUnitID = propertyUnitID;
    }

    /**
     * @param applicationList the applicationList to set
     */
    public void setApplicationList(List<OccPermitApplication> applicationList) {
        this.applicationList = applicationList;
    }

    /**
     * @param personList the personList to set
     */
    public void setPersonList(List<PersonOccPeriod> personList) {
        this.personList = personList;
    }

    /**
     * @param eventList the eventList to set
     */
    public void setEventList(List<OccEvent> eventList) {
        this.eventList = eventList;
    }

    /**
     * @param proposalList the proposalList to set
     */
    public void setProposalList(List<Proposal> proposalList) {
        this.proposalList = proposalList;
    }

    /**
     * @param inspectionList the inspectionList to set
     */
    public void setInspectionList(List<OccInspection> inspectionList) {
        this.inspectionList = inspectionList;
    }

    /**
     * @param permitList the permitList to set
     */
    public void setPermitList(List<OccPermit> permitList) {
        this.permitList = permitList;
    }

    /**
     * @param blobIDList the blobIDList to set
     */
    public void setBlobIDList(List<Integer> blobIDList) {
        this.blobIDList = blobIDList;
    }

    /**
     * @param manager the manager to set
     */
    public void setManager(User manager) {
        this.manager = manager;
    }

    /**
     * @param type the type to set
     */
    public void setType(OccPeriodType type) {
        this.type = type;
    }

    /**
     * @param periodTypeCertifiedBy the periodTypeCertifiedBy to set
     */
    public void setPeriodTypeCertifiedBy(User periodTypeCertifiedBy) {
        this.periodTypeCertifiedBy = periodTypeCertifiedBy;
    }

    /**
     * @param periodTypeCertifiedTS the periodTypeCertifiedTS to set
     */
    public void setPeriodTypeCertifiedTS(LocalDateTime periodTypeCertifiedTS) {
        this.periodTypeCertifiedTS = periodTypeCertifiedTS;
    }

    /**
     * @param source the source to set
     */
    public void setSource(BOBSource source) {
        this.source = source;
    }

    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @param createdTS the createdTS to set
     */
    public void setCreatedTS(LocalDateTime createdTS) {
        this.createdTS = createdTS;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    /**
     * @param startDateCertifiedTS the startDateCertifiedTS to set
     */
    public void setStartDateCertifiedTS(LocalDateTime startDateCertifiedTS) {
        this.startDateCertifiedTS = startDateCertifiedTS;
    }

    /**
     * @param startDateCertifiedBy the startDateCertifiedBy to set
     */
    public void setStartDateCertifiedBy(User startDateCertifiedBy) {
        this.startDateCertifiedBy = startDateCertifiedBy;
    }

    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    /**
     * @param endDateCertifiedTS the endDateCertifiedTS to set
     */
    public void setEndDateCertifiedTS(LocalDateTime endDateCertifiedTS) {
        this.endDateCertifiedTS = endDateCertifiedTS;
    }

    /**
     * @param endDateCertifiedBy the endDateCertifiedBy to set
     */
    public void setEndDateCertifiedBy(User endDateCertifiedBy) {
        this.endDateCertifiedBy = endDateCertifiedBy;
    }

    /**
     * @param authorizedTS the authorizedTS to set
     */
    public void setAuthorizedTS(LocalDateTime authorizedTS) {
        this.authorizedTS = authorizedTS;
    }

    /**
     * @param authorizedBy the authorizedBy to set
     */
    public void setAuthorizedBy(User authorizedBy) {
        this.authorizedBy = authorizedBy;
    }

    /**
     * @param overrideTypeConfig the overrideTypeConfig to set
     */
    public void setOverrideTypeConfig(boolean overrideTypeConfig) {
        this.overrideTypeConfig = overrideTypeConfig;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

   

    /**
     * @return the eventRuleOccPeriodList
     */
    public List<EventRuleOccPeriod> getEventRuleOccPeriodList() {
        return eventRuleOccPeriodList;
    }

    /**
     * @param eventRuleOccPeriodList the eventRuleOccPeriodList to set
     */
    public void setEventRuleOccPeriodList(List<EventRuleOccPeriod> eventRuleOccPeriodList) {
        this.eventRuleOccPeriodList = eventRuleOccPeriodList;
    }

    /**
     * @return the startDateUtilDate
     */
    public java.util.Date getStartDateUtilDate() {
         if(startDate != null){
            startDateUtilDate = java.util.Date.from(getStartDate().atZone(ZoneId.systemDefault()).toInstant());
        }
         return startDateUtilDate;
    }

    /**
     * @return the endDateUtilDate
     */
    public java.util.Date getEndDateUtilDate() {
        if(endDate != null){
            endDateUtilDate = java.util.Date.from(getEndDate().atZone(ZoneId.systemDefault()).toInstant());
        }
        return endDateUtilDate;
    }

    /**
     * @param startDateUtilDate the startDateUtilDate to set
     */
    public void setStartDateUtilDate(java.util.Date startDateUtilDate) {
        this.startDateUtilDate = startDateUtilDate;
        if(startDateUtilDate != null){
            startDate = startDateUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        
        this.startDateUtilDate = startDateUtilDate;
    }

    /**
     * @param endDateUtilDate the endDateUtilDate to set
     */
    public void setEndDateUtilDate(java.util.Date endDateUtilDate) {
        this.endDateUtilDate = endDateUtilDate;
        if(endDateUtilDate != null){
            endDate = endDateUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        this.endDateUtilDate = endDateUtilDate;
    }

    /**
     * @return the status
     */
    public OccPeriodStatusEnum getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(OccPeriodStatusEnum status) {
        this.status = status;
    }

    /**
     * @return the showHiddenEvents
     */
    public boolean isShowHiddenEvents() {
        return showHiddenEvents;
    }

    /**
     * @return the showInactiveEvents
     */
    public boolean isShowInactiveEvents() {
        return showInactiveEvents;
    }

    /**
     * @param showHiddenEvents the showHiddenEvents to set
     */
    public void setShowHiddenEvents(boolean showHiddenEvents) {
        this.showHiddenEvents = showHiddenEvents;
    }

    /**
     * @param showInactiveEvents the showInactiveEvents to set
     */
    public void setShowInactiveEvents(boolean showInactiveEvents) {
        this.showInactiveEvents = showInactiveEvents;
    }

   
     
    
}
