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
 *
 * @author sylvia
 */
public  class   MailingAddress 
        extends TrackedEntity
        implements  IFace_deactivatable, 
                    IFace_keyIdentified,
                    IFace_noteHolder{ 
    final String TABLE_NAME = "mailingaddress";
    
    
    protected int addressID;
    protected String num;
    protected String unitNo;
    protected String city;
    protected String state;
    protected String zipCode;
    protected int poBox;
    protected LocalDateTime verifiedTS;
    protected BOBSource source;
    protected String notes;
    
    
    /**
     * @return the addressID
     */
    public int getAddressID() {
        return addressID;
    }

    /**
     * @return the num
     */
    public String getNum() {
        return num;
    }

    /**
     * @return the unitNo
     */
    public String getUnitNo() {
        return unitNo;
    }

    /**
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @return the zipCode
     */
    public String getZipCode() {
        return zipCode;
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
     * @param num the num to set
     */
    public void setNum(String num) {
        this.num = num;
    }

    /**
     * @param unitNo the unitNo to set
     */
    public void setUnitNo(String unitNo) {
        this.unitNo = unitNo;
    }

    /**
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @param zipCode the zipCode to set
     */
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
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
    public boolean isDeactivated() {
        return deactivatedTS != null;
    }

    @Override
    public void setDactivatedTS(LocalDateTime deacTS) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getDBKey() {
        return addressID;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getNotes() {
        return notes;
    }

    @Override
    public void setNotes(String n) {
        notes = n;
    }
    
}
