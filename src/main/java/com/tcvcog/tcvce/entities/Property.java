/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import com.tcvcog.tcvce.application.interfaces.IFace_Loggable;
import com.tcvcog.tcvce.entities.occupancy.OccLocationDescriptor;
import com.tcvcog.tcvce.entities.occupancy.OccPermit;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Foundational entity for the system: Property
 * @author Ellen Baskem
 */

public class    Property 
        extends PropertyPublic 
        implements IFace_Loggable{
    
    protected List<PropertyUnit> unitList;
    
    protected int muniCode;
    
    protected String notes;
  
    private LocalDateTime creationTS;
    protected LocalDateTime lastUpdatedTS;
    protected User lastUpdatedBy;
    protected OccLocationDescriptor  locationDescriptor;
    
    protected BOBSource bobSource;
    protected LocalDateTime unfitDateStart;
    protected LocalDateTime unfitDateStop;
    protected User unfitBy;
    
    protected LocalDateTime abandonedDateStart;
    protected LocalDateTime abandonedDateStop;
    protected User abandonedBy;
    protected LocalDateTime vacantDateStart;
    
    protected LocalDateTime vacantDateStop;
    protected User vacantBy;
    protected IntensityClass condition;
    
    protected IntensityClass landBankProspect;
    protected boolean LandBankHeld;
    protected boolean active;
    protected boolean nonAddressable;
    
    /**
     * Creates a new instance of Property
     */
    public Property() {
      
    }


    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }


    /**
     * @param useGroup the useGroup to set
     */
    public void setUseGroup(String useGroup) {
        this.useGroup = useGroup;
    }

    /**
     * @return the constructionType
     */
    public String getConstructionType() {
        return constructionType;
    }


    /**
     * @param muniCode the muniCode to set
     */
    public void setMuniCode(int muniCode) {
        this.muniCode = muniCode;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

   

    /**
     * @param address_city the address_city to set
     */
    public void setAddress_city(String address_city) {
        this.address_city = address_city;
    }

    /**
     * @param address_state the address_state to set
     */
    public void setAddress_state(String address_state) {
        this.address_state = address_state;
    }

    /**
     * @param address_zip the address_zip to set
     */
    public void setAddress_zip(String address_zip) {
        this.address_zip = address_zip;
    }

 /**
     * @return the lastUpdatedTS
     */
    public LocalDateTime getLastUpdatedTS() {
        return lastUpdatedTS;
    }




    /**
     * @return the lastUpdatedBy
     */
    public User getLastUpdatedBy() {
        return lastUpdatedBy;
    }

   

    /**
     * @return the bobSource
     */
    public BOBSource getBobSource() {
        return bobSource;
    }

    /**
     * @return the unfitDateStart
     */
    public LocalDateTime getUnfitDateStart() {
        return unfitDateStart;
    }

    /**
     * @return the unfitDateStop
     */
    public LocalDateTime getUnfitDateStop() {
        return unfitDateStop;
    }

    /**
     * @return the unfitBy
     */
    public User getUnfitBy() {
        return unfitBy;
    }

    /**
     * @return the abandonedDateStart
     */
    public LocalDateTime getAbandonedDateStart() {
        return abandonedDateStart;
    }

    /**
     * @return the abandonedDateStop
     */
    public LocalDateTime getAbandonedDateStop() {
        return this.abandonedDateStop;
    }

    /**
     * @return the abandonedBy
     */
    public User getAbandonedBy() {
        return abandonedBy;
    }

    /**
     * @return the vacantDateStart
     */
    public LocalDateTime getVacantDateStart() {
        return vacantDateStart;
    }

    /**
     * @return the vacantDateStop
     */
    public LocalDateTime getVacantDateStop() {
        return vacantDateStop;
    }

    /**
     * @return the vacantBy
     */
    public User getVacantBy() {
        return vacantBy;
    }

    /**
     * @return the condition
     */
    public IntensityClass getCondition() {
        return condition;
    }
    
    /**
     * sets the condition
     * @param ic 
     */
    public void setCondition(IntensityClass ic){
        condition = ic;
    }

    /**
     * @return the landBankProspect
     */
    public IntensityClass getLandBankProspect() {
        return landBankProspect;
    }

    /**
     * @return the LandBankHeld
     */
    public boolean isLandBankHeld() {
        return LandBankHeld;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }


    /**
     * @param lastUpdatedTS the lastUpdatedTS to set
     */
    public void setLastUpdatedTS(LocalDateTime lastUpdatedTS) {
        this.lastUpdatedTS = lastUpdatedTS;
    }

    /**
     * @param lastUpdatedBy the lastUpdatedBy to set
     */
    public void setLastUpdatedBy(User lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    
    /**
     * @param bobSource the bobSource to set
     */
    public void setBobSource(BOBSource bobSource) {
        this.bobSource = bobSource;
    }

    /**
     * @param unfitDateStart the unfitDateStart to set
     */
    public void setUnfitDateStart(LocalDateTime unfitDateStart) {
        this.unfitDateStart = unfitDateStart;
    }
    
    public java.util.Date getUnfitDateUtilStart() {
        if(unfitDateStart != null){
            return java.util.Date.from(getUnfitDateStart().atZone(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }
    
    public void setUnfitDateUtilStart(java.util.Date unfitDateUtilStart){
        if(unfitDateUtilStart != null){
            this.unfitDateStart = unfitDateUtilStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
    }

    /**
     * @param unfitDateStop the unfitDateStop to set
     */
    public void setUnfitDateStop(LocalDateTime unfitDateStop) {
        this.unfitDateStop = unfitDateStop;
    }
    
    public java.util.Date getUnfitDateUtilStop(){
        if(unfitDateStop != null){
            return java.util.Date.from(getUnfitDateStop().atZone(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }
    
    public void setUnfitDateUtilStop(java.util.Date unfitDateUtilStop){
        if(unfitDateUtilStop != null){
            this.unfitDateStop = unfitDateUtilStop.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
    }

    /**
     * @param unfitBy the unfitBy to set
     */
    public void setUnfitBy(User unfitBy) {
        this.unfitBy = unfitBy;
    }

    /**
     * @param abandonedDateStart the abandonedDateStart to set
     */
    public void setAbandonedDateStart(LocalDateTime abandonedDateStart) {
        this.abandonedDateStart = abandonedDateStart;
    }
    
    public void setAbandonedDateUtilStart(java.util.Date abandonedDateUtilStart){
        if(abandonedDateUtilStart != null){
            this.abandonedDateStart = abandonedDateUtilStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
    }
    
    public java.util.Date getAbandonedDateUtilStart(){
        if(abandonedDateStart != null){
            return java.util.Date.from(getAbandonedDateStart().atZone(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }

    /**
     * @param abandonedDateStop the abandonedDateStop to set
     */
    public void setAbandonedDateStop(LocalDateTime abandonedDateStop) {
        this.abandonedDateStop = abandonedDateStop;
    }
    
    public java.util.Date getAbandonedDateUtilStop(){
        if(this.abandonedDateStop != null){
            return java.util.Date.from(getAbandonedDateStop().atZone(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }
    
    public void setAbandonedDateUtilStop(java.util.Date abandonedDateUtilStop){
        if(abandonedDateUtilStop != null){
            this.abandonedDateStart = abandonedDateUtilStop.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
    }

    /**
     * @param abandonedBy the abandonedBy to set
     */
    public void setAbandonedBy(User abandonedBy) {
        this.abandonedBy = abandonedBy;
    }

    /**
     * @param vacantDateStart the vacantDateStart to set
     */
    public void setVacantDateStart(LocalDateTime vacantDateStart) {
        this.vacantDateStart = vacantDateStart;
    }
    
    public java.util.Date getVacantDateUtilStart(){
        if(vacantDateStart != null){
            return java.util.Date.from(getVacantDateStart().atZone(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }
    
    public void setVacantDateUtilStart(java.util.Date vacantStartUtilDate){
        if(vacantStartUtilDate != null){
            this.vacantDateStart = vacantStartUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
    }

    /**
     * @param vacantDateStop the vacantDateStop to set
     */
    public void setVacantDateStop(LocalDateTime vacantDateStop) {
        this.vacantDateStop = vacantDateStop;
    }
    
    public java.util.Date getVacantDateUtilStop(){
        if(vacantDateStop != null){
            return java.util.Date.from(getVacantDateStop().atZone(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }
    
    public void setVacantDateUtilStop(java.util.Date vacantStopUtilDate){
        if(vacantStopUtilDate != null){
            this.vacantDateStop = vacantStopUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
    }

    /**
     * @param vacantBy the vacantBy to set
     */
    public void setVacantBy(User vacantBy) {
        this.vacantBy = vacantBy;
    }

    /**
     * @param landBankProspectIntensityClassID the landBankProspect to set
     */
    public void setLandBankProspect(IntensityClass ic) {
        this.landBankProspect = ic;
    }

    /**
     * @param LandBankHeld the LandBankHeld to set
     */
    public void setLandBankHeld(boolean LandBankHeld) {
        this.LandBankHeld = LandBankHeld;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return the nonAddressable
     */
    public boolean isNonAddressable() {
        return nonAddressable;
    }

    /**
     * @param nonAddressable the nonAddressable to set
     */
    public void setNonAddressable(boolean nonAddressable) {
        this.nonAddressable = nonAddressable;
    }

    /**
     * @return the locationDescriptor
     */
    public OccLocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    /**
     * @param locationDescriptor the locationDescriptor to set
     */
    public void setLocationDescriptor(OccLocationDescriptor locationDescriptor) {
        this.locationDescriptor = locationDescriptor;
    }

    /**
     * @return the status
     */
    public PropertyStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(PropertyStatus status) {
        this.status = status;
    }



    /**
     * @return the unitList
     */
    public List<PropertyUnit> getUnitList() {
        return unitList;
    }

    /**
     * @param unitList the unitList to set
     */
    public void setUnitList(List<PropertyUnit> unitList) {
        this.unitList = unitList;
    }

    /**
     * @return the creationTS
     */
    public LocalDateTime getCreationTS() {
        return creationTS;
    }

    /**
     * @param creationTS the creationTS to set
     */
    public void setCreationTS(LocalDateTime creationTS) {
        this.creationTS = creationTS;
    }

  
}
