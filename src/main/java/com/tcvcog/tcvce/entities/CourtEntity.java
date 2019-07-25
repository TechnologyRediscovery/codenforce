/*
 * Copyright (C) 2018 Adam Gutonski
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

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Adam Gutonski
 */
public class CourtEntity implements Serializable {
    
    private int courtEntityID;
    private String courtEntityOfficialNum;
    private String jurisdictionLevel;
    private String courtEntityName;
    
    private String addressStreet;
    private String addressCity;
    private String addressZip;
    private String addressState;
    private String addressCounty;
    
    private String phone;
    private String url;
    private String notes;
    
    private String judgeName;
    private int relativeOrder;

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.courtEntityID;
        hash = 37 * hash + Objects.hashCode(this.courtEntityOfficialNum);
        hash = 37 * hash + Objects.hashCode(this.jurisdictionLevel);
        hash = 37 * hash + Objects.hashCode(this.courtEntityName);
        hash = 37 * hash + Objects.hashCode(this.addressStreet);
        hash = 37 * hash + Objects.hashCode(this.addressCity);
        hash = 37 * hash + Objects.hashCode(this.addressZip);
        hash = 37 * hash + Objects.hashCode(this.addressState);
        hash = 37 * hash + Objects.hashCode(this.addressCounty);
        hash = 37 * hash + Objects.hashCode(this.phone);
        hash = 37 * hash + Objects.hashCode(this.url);
        hash = 37 * hash + Objects.hashCode(this.notes);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CourtEntity other = (CourtEntity) obj;
        if (this.courtEntityID != other.courtEntityID) {
            return false;
        }
        
        return true;
    }

    /**
     * @return the courtEntityID
     */
    public int getCourtEntityID() {
        return courtEntityID;
    }

    /**
     * @param courtEntityID the courtEntityID to set
     */
    public void setCourtEntityID(int courtEntityID) {
        this.courtEntityID = courtEntityID;
    }

    /**
     * @return the courtEntityOfficialNum
     */
    public String getCourtEntityOfficialNum() {
        return courtEntityOfficialNum;
    }

    /**
     * @param courtEntityOfficialNum the courtEntityOfficialNum to set
     */
    public void setCourtEntityOfficialNum(String courtEntityOfficialNum) {
        this.courtEntityOfficialNum = courtEntityOfficialNum;
    }

    /**
     * @return the jurisdictionLevel
     */
    public String getJurisdictionLevel() {
        return jurisdictionLevel;
    }

    /**
     * @param jurisdictionLevel the jurisdictionLevel to set
     */
    public void setJurisdictionLevel(String jurisdictionLevel) {
        this.jurisdictionLevel = jurisdictionLevel;
    }

   

    /**
     * @return the courtEntityName
     */
    public String getCourtEntityName() {
        return courtEntityName;
    }

    /**
     * @param courtEntityName the courtEntityName to set
     */
    public void setCourtEntityName(String courtEntityName) {
        this.courtEntityName = courtEntityName;
    }

    /**
     * @return the addressStreet
     */
    public String getAddressStreet() {
        return addressStreet;
    }

    /**
     * @param addressStreet the addressStreet to set
     */
    public void setAddressStreet(String addressStreet) {
        this.addressStreet = addressStreet;
    }

    /**
     * @return the addressZip
     */
    public String getAddressZip() {
        return addressZip;
    }

    /**
     * @param addressZip the addressZip to set
     */
    public void setAddressZip(String addressZip) {
        this.addressZip = addressZip;
    }

    /**
     * @return the addressState
     */
    public String getAddressState() {
        return addressState;
    }

    /**
     * @param addressState the addressState to set
     */
    public void setAddressState(String addressState) {
        this.addressState = addressState;
    }

    /**
     * @return the addressCounty
     */
    public String getAddressCounty() {
        return addressCounty;
    }

    /**
     * @param addressCounty the addressCounty to set
     */
    public void setAddressCounty(String addressCounty) {
        this.addressCounty = addressCounty;
    }

    /**
     * @return the phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone the phone to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
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
     * @return the addressCity
     */
    public String getAddressCity() {
        return addressCity;
    }

    /**
     * @param addressCity the addressCity to set
     */
    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }

    /**
     * @return the judgeName
     */
    public String getJudgeName() {
        return judgeName;
    }

    /**
     * @return the relativeOrder
     */
    public int getRelativeOrder() {
        return relativeOrder;
    }

    /**
     * @param judgeName the judgeName to set
     */
    public void setJudgeName(String judgeName) {
        this.judgeName = judgeName;
    }

    /**
     * @param relativeOrder the relativeOrder to set
     */
    public void setRelativeOrder(int relativeOrder) {
        this.relativeOrder = relativeOrder;
    }
    
    
}
