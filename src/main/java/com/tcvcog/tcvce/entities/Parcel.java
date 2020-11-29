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

import java.time.LocalDateTime;

/**
 * Models a parcel as seen from Allegheny County
 * @author sylvia
 */
public  class Parcel 
        extends TrackedEntity
        implements IFace_deactivatable, 
                   IFace_keyIdentified,
                   IFace_noteHolder{
        
    final static String TABLE_NAME = "parcel";
    
    
    private int parcelkey;
    private String countyParcelID;
    private Municipality muni;
    private BOBSource source;
    protected String notes;
    
    protected ParcelInfo parcelInfo;
    

    /**
     * @return the parcelkey
     */
    public int getParcelkey() {
        return parcelkey;
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
     * @param parcelkey the parcelkey to set
     */
    public void setParcelkey(int parcelkey) {
        this.parcelkey = parcelkey;
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
     * @param lastupdatedBy the lastupdatedBy to set
     */
    @Override
    public void setLastupdatedBy(User lastupdatedBy) {
        this.lastupdatedBy = lastupdatedBy;
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
    public boolean isDeactivated() {
        return deactivatedTS != null;
        
    }

    @Override
    public int getDBKey() {
        return parcelkey;
    }

    @Override
    public String getTableName() {
     return TABLE_NAME;    
    }

    
}
