/*
 * Copyright (C) 2021 Technology Rediscovery LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.application.IFace_pinnable;
import com.tcvcog.tcvce.application.interfaces.IFace_Loggable;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.util.DateTimeUtil;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.tcvcog.tcvce.application.interfaces.IFace_ActivatableBOB;
import com.tcvcog.tcvce.entities.HumanLink;

/**
 * Primary Business Object BOB for holding data about Occupancy Periods
 * 
 * @author Ellen Bascomb
 */
public  class       OccPeriod 
        extends     TrackedEntity  
        implements  IFace_Loggable,
                    IFace_EventHolder,
                    Comparable<OccPeriod>,
                    IFace_ActivatableBOB,
                    IFace_noteHolder,
                    IFace_pinnable,
                    IFace_stateful{
    
    final static String OCCPERIOD_TABLE_NAME = "occperiod";
    final static String OCCPERIOD_PK_FIELD = "periodid";
    final static String OCCPERIOD_HFNAME = "Occupancy Period";
    
    final static DomainEnum OCC_DOMAIN = DomainEnum.OCCUPANCY;
    
    final static String PIN_TABLE_NAME = "public.occperiodpin";
    final static String PIN_FK_FIELD = "occperiod_periodid";
    
    protected int periodID;
    protected int propertyUnitID;
    
    
    protected List<HumanLink> humans;
    
    protected FieldInspection governingInspection;
    
    protected User manager;
    
    protected boolean pinned;
    protected User pinner;
    
    protected User periodTypeCertifiedBy;
    protected LocalDateTime periodTypeCertifiedTS;
    protected List<EventCnF> eventList;
    
    protected BOBSource source;
    
    protected LocalDateTime startDate;
    protected LocalDateTime startDateCertifiedTS;
    protected User startDateCertifiedBy;
    
    protected LocalDateTime endDate;
    protected LocalDateTime endDateCertifiedTS;
    protected User endDateCertifiedBy;

    protected User authorizedBy;
    protected LocalDateTime authorizedTS;
    
    protected boolean overrideTypeConfig;
    
    protected String notes;
    
    protected boolean active;
    
    // Used during initiation to store the origination event
    protected EventCategory originationEventCategory;
    protected List<OccPermit> permitList;

    public OccPeriod() {
    }

    public OccPeriod(OccPeriod otherPeriod) {
        if(otherPeriod != null){

            this.periodID = otherPeriod.getPeriodID();
            this.propertyUnitID = otherPeriod.getPropertyUnitID();
            this.governingInspection = otherPeriod.getGoverningInspection();

            this.manager = otherPeriod.getManager();
            this.periodTypeCertifiedBy = otherPeriod.getPeriodTypeCertifiedBy();
            this.periodTypeCertifiedTS = otherPeriod.getPeriodTypeCertifiedTS();

            this.eventList = otherPeriod.getEventList();
            this.source = otherPeriod.getSource();

            this.startDate = otherPeriod.getStartDate();
            this.startDateCertifiedBy = otherPeriod.getStartDateCertifiedBy();
            this.startDateCertifiedTS = otherPeriod.getStartDateCertifiedTS();

            this.endDate = otherPeriod.getEndDate();
            this.endDateCertifiedBy = otherPeriod.getEndDateCertifiedBy();
            this.endDateCertifiedTS = otherPeriod.getEndDateCertifiedTS();

            this.authorizedBy = otherPeriod.getAuthorizedBy();
            this.authorizedTS = otherPeriod.getAuthorizedTS();

            this.overrideTypeConfig = otherPeriod.isOverrideTypeConfig();
            this.notes = otherPeriod.getNotes();
        }
    }

    @Override
    public int compareTo(OccPeriod op) {
        int c = 0;
        if(this.startDate != null && op.getStartDate() != null){
             c = this.startDate.compareTo(op.startDate);
        } 
        return c;
    }

    public long getPeriodAge() {
        if(endDate != null){
            return DateTimeUtil.getTimePeriodAsDays(startDate, endDate);
        } else {
            return DateTimeUtil.getTimePeriodAsDays(startDate, LocalDateTime.now());
        }
    }

    public long getPeriodAgeAsOf(LocalDateTime ageEndTime){
        return DateTimeUtil.getTimePeriodAsDays(startDate, ageEndTime);
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
     * @return the prettified startdate
     */
    public String getStartDatePretty() {
        if(startDate != null){
            return DateTimeUtil.getPrettyDate(startDate);
        }
        return null;
    }

    public String getStartDatePrettyNoTime() {
        if(startDate != null){
            return DateTimeUtil.getPrettyDateNoTime(startDate);
        }
        return null;
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
     * @return the prettified enddate
     */
    public String getEndDatePretty() {
        if(endDate != null){
            return DateTimeUtil.getPrettyDate(endDate);
        }
        return null;
    }

    public String getEndDatePrettyNoTime() {
        if(endDate != null){
            return DateTimeUtil.getPrettyDateNoTime(endDate);
        }
        return null;
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
    @Override
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
    @Override
    public void setNotes(String notes) {
        this.notes = notes;
    }
   
    
    /**
     * @return the governingInspection
     */
    public FieldInspection getGoverningInspection() {
        return governingInspection;
    }

    /**
     * @param governingInspection the governingInspection to set
     */
    public void setGoverningInspection(FieldInspection governingInspection) {
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

    @Override
    public int getBObID() {
        return periodID;
    }

    @Override
    public String getPKFieldName() {
        return OCCPERIOD_PK_FIELD;
    }

    @Override
    public int getDBKey() {
        return periodID;
    }

    @Override
    public String getDBTableName() {
        return OCCPERIOD_TABLE_NAME;
    }

    @Override
    public String getNoteHolderFriendlyName() {
        return OCCPERIOD_HFNAME;
    }

    @Override
    public DomainEnum getEventDomain() {
        return OCC_DOMAIN;
    }

    /**
     * @return the originationEventCategory
     */
    public EventCategory getOriginationEventCategory() {
        return originationEventCategory;
    }

    /**
     * @param originationEventCategory the originationEventCategory to set
     */
    public void setOriginationEventCategory(EventCategory originationEventCategory) {
        this.originationEventCategory = originationEventCategory;
    }

    /**
     * @return the permitList
     */
    public List<OccPermit> getPermitList() {
        return permitList;
    }

    /**
     * @param permitList the permitList to set
     */
    public void setPermitList(List<OccPermit> permitList) {
        this.permitList = permitList;
    }

    /**
     * @return the pinned
     */
    @Override
    public boolean isPinned() {
        return pinned;
    }

    /**
     * @return the pinner
     */
    @Override
    public User getPinner() {
        return pinner;
    }

    /**
     * @param pinner the pinner to set
     */
    @Override
    public void setPinner(User pinner) {
        this.pinner = pinner;
    }

    @Override
    public String getPinTableFKString() {
        return PIN_FK_FIELD;
    }

    @Override
    public String getPinTableName() {
        return PIN_TABLE_NAME;
    }

    /**
     * @param pinned the pinned to set
     */
    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    @Override
    public StateEnum getState() {
        if(permitList == null || permitList.isEmpty()){
            return StateEnum.OPEN;
        }
        for(OccPermit pmt: permitList){
            if(pmt.getFinalizedts() != null){
                return StateEnum.CLOSED;
            }
        }
        return StateEnum.OPEN;
    }
}