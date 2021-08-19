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
 * Encapsulates a mailing address
 * @author sylvia
 */
public  class   MailingAddress 
        extends TrackedEntity
        implements  IFace_keyIdentified,
                    IFace_noteHolder{ 
    final static String TABLE_NAME = "mailingaddress";
    final static String PK_FIELD = "addressid";
    
    protected int addressID;
    protected String buildingNo;
    protected MailingStreet street;
    protected int poBox;
    protected LocalDateTime verifiedTS;
    protected User verifiedBy;
    protected BOBSource verifiedSource;
    protected BOBSource source;
    protected String notes;
    
    
    /**
     * @return the addressID
     */
    public int getAddressID() {
        return addressID;
    }

    /**
     * @return the buildingNo
     */
    public String getBuildingNo() {
        return buildingNo;
    }

   
    /**
     * @return the poBox
     */
    public int getPoBox() {
        return poBox;
    }

    /**
     * @return the verifiedTS
     */
    public LocalDateTime getVerifiedTS() {
        return verifiedTS;
    }

    /**
     * @return the source
     */
    public BOBSource getSource() {
        return source;
    }

    /**
     * @param addressID the addressID to set
     */
    public void setAddressID(int addressID) {
        this.addressID = addressID;
    }

    /**
     * @param buildingNo the buildingNo to set
     */
    public void setBuildingNo(String buildingNo) {
        this.buildingNo = buildingNo;
    }

    /**
     * @param poBox the poBox to set
     */
    public void setPoBox(int poBox) {
        this.poBox = poBox;
    }

    /**
     * @param verifiedTS the verifiedTS to set
     */
    public void setVerifiedTS(LocalDateTime verifiedTS) {
        this.verifiedTS = verifiedTS;
    }

    /**
     * @param source the source to set
     */
    public void setSource(BOBSource source) {
        this.source = source;
    }

    

    @Override
    public int getDBKey() {
        return addressID;
    }

    
    @Override
    public String getNotes() {
        return notes;
    }

    @Override
    public void setNotes(String n) {
        notes = n;
    }

  
    @Override
    public String getPKFieldName() {
        return PK_FIELD;
    }

    @Override
    public String getDBTableName() {
        return TABLE_NAME;
    }

    /**
     * @return the street
     */
    public MailingStreet getStreet() {
        return street;
    }

    /**
     * @param street the street to set
     */
    public void setStreet(MailingStreet street) {
        this.street = street;
    }

    /**
     * @return the verifiedBy
     */
    public User getVerifiedBy() {
        return verifiedBy;
    }

    /**
     * @param verifiedBy the verifiedBy to set
     */
    public void setVerifiedBy(User verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    /**
     * @return the verifiedSource
     */
    public BOBSource getVerifiedSource() {
        return verifiedSource;
    }

    /**
     * @param verifiedSource the verifiedSource to set
     */
    public void setVerifiedSource(BOBSource verifiedSource) {
        this.verifiedSource = verifiedSource;
    }

   
}