/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.entities.BOBSource;
import com.tcvcog.tcvce.entities.EntityUtils;
import com.tcvcog.tcvce.entities.User;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import com.tcvcog.tcvce.entities.Openable;

/**
 * Primary Business Object BOB for holding data about Occupancy Periods
 * @author Ellen Baskem
 */
public class OccPeriod 
        extends EntityUtils 
        implements  Serializable,
                    Openable{
    
    protected int periodID;
    protected int propertyUnitID;
    protected OccPeriodType type;
    protected OccPeriodStatusEnum status;
    
    protected boolean readyForPeriodAuthorization;
    
    protected OccInspection governingInspection;
    
    protected User manager;
     
    protected User periodTypeCertifiedBy;
    protected LocalDateTime periodTypeCertifiedTS;
    
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
    
    @Override
    public boolean isOpen() {
        // TEMPORARY until status flow is created
        if(status != null){
            return status.isOpenPeriod();
        } else {
            return true;
        }
                
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
     * @return the readyForPeriodAuthorization
     */
    public boolean isReadyForPeriodAuthorization() {
        return readyForPeriodAuthorization;
    }

    /**
     * @param readyForPeriodAuthorization the readyForPeriodAuthorization to set
     */
    public void setReadyForPeriodAuthorization(boolean readyForPeriodAuthorization) {
        this.readyForPeriodAuthorization = readyForPeriodAuthorization;
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
}