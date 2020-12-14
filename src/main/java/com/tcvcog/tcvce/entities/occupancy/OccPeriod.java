/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.application.interfaces.IFace_Loggable;
import com.tcvcog.tcvce.entities.BOBSource;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.IFace_EventHolder;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Primary Business Object BOB for holding data about Occupancy Periods
 * 
 * @author Ellen Bascomb
 */
public  class       OccPeriod 
        extends     OccPeriodPublic  
        implements  IFace_Loggable,
                    IFace_EventHolder,
                    Comparable<OccPeriod>{
    
    protected int periodID;
    protected int propertyUnitID;
    
    protected OccPeriodType type;
    
    protected OccInspection governingInspection;
    
    protected User manager;
     
    protected User periodTypeCertifiedBy;
    protected LocalDateTime periodTypeCertifiedTS;
    protected List<EventCnF> eventList;
    
    protected BOBSource source;
    protected User createdBy;
    protected LocalDateTime createdTS;
    
    protected LocalDateTime startDate;
    protected java.util.Date startDateUtilDate;
    protected LocalDateTime startDateCertifiedTS;
    protected User startDateCertifiedBy;
    
    protected LocalDateTime endDate;
    protected java.util.Date endDateUtilDate;
    protected LocalDateTime endDateCertifiedTS;
    protected User endDateCertifiedBy;
    
    protected LocalDateTime authorizedTS;
    protected User authorizedBy;
    
    protected boolean overrideTypeConfig;
    
    protected String notes;
    
    protected boolean active;
    
    
    
    @Override
    public int compareTo(OccPeriod op) {
        int c = 0;
        if(this.startDate != null && op.getStartDate() != null){
             c = this.startDate.compareTo(op.startDate);
        } else if(this.createdTS != null && op.createdTS != null){
             c = this.createdTS.compareTo(op.createdTS);
        } 
        return c;
        
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
     * @return the startDateUtilDate
     */
    public java.util.Date getStartDateUtilDate() {
        startDateUtilDate = convertUtilDate(startDate);
         return startDateUtilDate;
    }

    /**
     * @return the endDateUtilDate
     */
    public java.util.Date getEndDateUtilDate() {
        endDateUtilDate = convertUtilDate(endDate);
        return endDateUtilDate;
    }

    /**
     * @param startDateUtilDate the startDateUtilDate to set
     */
    public void setStartDateUtilDate(java.util.Date startDateUtilDate) {
        this.startDateUtilDate = startDateUtilDate;
        startDate = convertUtilDate(startDateUtilDate);
    }

    /**
     * @param endDateUtilDate the endDateUtilDate to set
     */
    public void setEndDateUtilDate(java.util.Date endDateUtilDate) {
        this.endDateUtilDate = endDateUtilDate;
        endDate = convertUtilDate(endDateUtilDate);
    }

    /**
     * @return the governingInspection
     */
    public OccInspection getGoverningInspection() {
        return governingInspection;
    }

    /**
     * @param governingInspection the governingInspection to set
     */
    public void setGoverningInspection(OccInspection governingInspection) {
        this.governingInspection = governingInspection;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     *
     * @param lst
     */
    @Override
    public void setEventList(List<EventCnF> lst) {
        eventList = lst;
    }

    /** 
     * Iterates over the BOb's internal list of events and selects only those
     * specified by the enum constant's member vals
     *
     * @param voahle an instance of the enum representing which events you want
     * @return
     */
    @Override
    public List<EventCnF> getEventList(ViewOptionsActiveHiddenListsEnum voahle) {
        List<EventCnF> visEventList = new ArrayList<>();
        if (eventList != null) {
            for (EventCnF ev : eventList) {
                switch (voahle) {
                    case VIEW_ACTIVE_HIDDEN:
                        if (ev.isActive() && ev.isHidden()) {
                            visEventList.add(ev);
                        }
                        break;
                    case VIEW_ACTIVE_NOTHIDDEN:
                        if (ev.isActive() && !ev.isHidden()) {
                            visEventList.add(ev);
                        }
                        break;
                    case VIEW_ALL:
                        visEventList.add(ev);
                        break;
                    case VIEW_INACTIVE:
                        if (!ev.isActive()) {
                            visEventList.add(ev);
                        }
                        break;
                    default:
                        visEventList.add(ev);
                } // close switch
            } // close for
        } // close null check
        return visEventList;
    }
    
    /**
     * Retrieves all events
     * @return 
     */
    @Override
    public List<EventCnF> getEventList(){
        return eventList;
    }

}