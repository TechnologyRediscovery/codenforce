/*
 * Copyright (C) 2020 Technology Rediscovery LLC
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
package com.tcvcog.tcvce.entities;

import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Models a parcel as seen from Allegheny County
 * @author sylvia
 */
public  class Parcel 
        extends TrackedEntity
        implements IFace_keyIdentified,
                   IFace_noteHolder,
                   IFace_EventHolder{
        
    final static String TABLE_NAME = "parcel";
    final static String PKFIELD = "parcelkey";
    final static String HF_NAME = "Parcel";
    final static DomainEnum PARCEL_DOMAIN = DomainEnum.PARCEL;
    
    protected int parcelKey;
    protected String countyParcelID;
    protected String lotAndBlock;
    protected Municipality muni;
    protected BOBSource source;
    protected String notes;
    
    protected ParcelInfo parcelInfo;
    protected int broadviewPhotoID;
    
    protected List<EventCnF> eventList;
    
    public Parcel(){
        
    }
    
    public Parcel(Parcel p){
        if(p != null){
            this.parcelKey = p.parcelKey;
            this.countyParcelID = p.countyParcelID;
            this.lotAndBlock = p.lotAndBlock;
            this.muni = p.muni;
            this.source = p.source;
            this.notes = p.notes;
            this.parcelInfo = p.parcelInfo;
            this.broadviewPhotoID = p.broadviewPhotoID;
        }
    }
    

    /**
     * @return the parcelKey
     */
    public int getParcelKey() {
        return parcelKey;
    }

    /**
     * @return the countyParcelID
     */
    public String getCountyParcelID() {
        return countyParcelID;
    }

    /**
     * @return the source
     */
    public BOBSource getSource() {
        return source;
    }

 

    /**
     * @param parcelKey the parcelKey to set
     */
    public void setParcelKey(int parcelKey) {
        this.parcelKey = parcelKey;
    }

    /**
     * @param countyParcelID the countyParcelID to set
     */
    public void setCountyParcelID(String countyParcelID) {
        this.countyParcelID = countyParcelID;
    }

    /**
     * @param source the source to set
     */
    public void setSource(BOBSource source) {
        this.source = source;
    }

    /**
     * @param createdts the createdTS to set
     */
    @Override
    public void setCreatedTS(LocalDateTime createdts) {
        this.createdTS = createdts;
    }

    /**
     * @param createdBy the createdBy to set
     */
    @Override
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @param lastUpdatedTS the lastUpdatedTS to set
     */
    @Override
    public void setLastUpdatedTS(LocalDateTime lastUpdatedTS) {
        this.lastUpdatedTS = lastUpdatedTS;
    }

    /**
     * @param lastupdatedBy the lastUpdatedBy to set
     */
    @Override
    public void setLastUpdatedBy(User lastupdatedBy) {
        this.lastUpdatedBy = lastupdatedBy;
    }

   

    /**
     * @param deactivatedBy the deactivatedBy to set
     */
    @Override
    public void setDeactivatedBy(User deactivatedBy) {
        this.deactivatedBy = deactivatedBy;
    }

   

    /**
     * @return the muni
     */
    public Municipality getMuni() {
        return muni;
    }

    /**
     * @param muni the muni to set
     */
    public void setMuni(Municipality muni) {
        this.muni = muni;
    }

    /**
     * @return the notes
     */
    @Override
    public String getNotes() {
        return notes;
    }

    /**
     * @param notes the notes to set
     */
    @Override
    public void setNotes(String notes) {
        this.notes = notes;
    }

  

    @Override
    public int getDBKey() {
        return parcelKey;
    }

   
    /**
     * @return the lotAndBlock
     */
    public String getLotAndBlock() {
        return lotAndBlock;
    }

    /**
     * @param lotAndBlock the lotAndBlock to set
     */
    public void setLotAndBlock(String lotAndBlock) {
        this.lotAndBlock = lotAndBlock;
    }

    @Override
    public String getPKFieldName() {
        return PKFIELD;
    }

    @Override
    public String getDBTableName() {
        return TABLE_NAME;
    }

    /**
     * @return the parcelInfo
     */
    public ParcelInfo getParcelInfo() {
        return parcelInfo;
    }

    /**
     * @param parcelInfo the parcelInfo to set
     */
    public void setParcelInfo(ParcelInfo parcelInfo) {
        this.parcelInfo = parcelInfo;
    }

    @Override
    public String getNoteHolderFriendlyName() {
        return HF_NAME;
    }

    /**
     * @return the broadviewPhotoID
     */
    public int getBroadviewPhotoID() {
        return broadviewPhotoID;
    }

    /**
     * @param broadviewPhotoID the broadviewPhotoID to set
     */
    public void setBroadviewPhotoID(int broadviewPhotoID) {
        this.broadviewPhotoID = broadviewPhotoID;
    }

    @Override
    public DomainEnum getEventDomain() {
        return PARCEL_DOMAIN;
    }

    @Override
    public void setEventList(List<EventCnF> evList) {
        eventList = evList;
    }

    @Override
    public List<EventCnF> getEventList(ViewOptionsActiveHiddenListsEnum evViewOpt) {
        return eventList;
    }

    @Override
    public List<EventCnF> getEventList() {
        return eventList;
    }

    @Override
    public int getBObID() {
        return parcelKey;
    }

    
    
}
