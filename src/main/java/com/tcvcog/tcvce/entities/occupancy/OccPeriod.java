/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.entities.BOBSource;
import com.tcvcog.tcvce.entities.EntityUtils;
import com.tcvcog.tcvce.entities.Proposal;
import com.tcvcog.tcvce.entities.EventRule;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonOccPeriod;
import com.tcvcog.tcvce.entities.Photograph;
import com.tcvcog.tcvce.entities.User;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author sylvia
 */
public class OccPeriod extends EntityUtils implements Serializable{
    
    private int periodID;
    private int propertyUnitID;
    private OccPeriodType type;
    
    private List<OccPermitApplication> applicationList;
    private List<PersonOccPeriod> personList;
    private List<OccEvent> eventList;
    private List<Proposal> eventProposalList;
    private List<EventRule> eventRuleList;
    private List<OccInspection> inspectionList;
    private List<OccPermit> permitList;
    private List<Integer> photoIDList;
    
    private User manager;
    
    private User periodTypeCertifiedBy;
    private LocalDateTime periodTypeCertifiedTS;
    
    private BOBSource source;
    private User createdBy;
    private LocalDateTime createdTS;
    
    private LocalDateTime startDate;
    private LocalDateTime startDateCertifiedTS;
    private User startDateCertifiedBy;
     
    private LocalDateTime endDate;
    private LocalDateTime endDateCertifiedTS;
    private User endDateCertifiedBy;
    
    private int defaultValidityPeriodDays;
    
    private LocalDateTime authorizedTS;
    private User authorizedBy;
    
    private boolean overrideTypeConfig;
    
    private String notes;

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
     * @return the eventProposalList
     */
    public List<Proposal> getEventProposalList() {
        return eventProposalList;
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
     * @return the photoIDList
     */
    public List<Integer> getPhotoIDList() {
        return photoIDList;
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
     * @param eventProposalList the eventProposalList to set
     */
    public void setEventProposalList(List<Proposal> eventProposalList) {
        this.eventProposalList = eventProposalList;
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
     * @param photoIDList the photoIDList to set
     */
    public void setPhotoIDList(List<Integer> photoIDList) {
        this.photoIDList = photoIDList;
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
     * @return the defaultValidityPeriodDays
     */
    public int getDefaultValidityPeriodDays() {
        return defaultValidityPeriodDays;
    }

    /**
     * @param defaultValidityPeriodDays the defaultValidityPeriodDays to set
     */
    public void setDefaultValidityPeriodDays(int defaultValidityPeriodDays) {
        this.defaultValidityPeriodDays = defaultValidityPeriodDays;
    }

    /**
     * @return the eventRuleList
     */
    public List<EventRule> getEventRuleList() {
        return eventRuleList;
    }

    /**
     * @param eventRuleList the eventRuleList to set
     */
    public void setEventRuleList(List<EventRule> eventRuleList) {
        this.eventRuleList = eventRuleList;
    }
     
    
}
