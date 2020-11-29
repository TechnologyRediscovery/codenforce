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
public  class   ContactPhone 
        extends Contact{
    
    final static String TABLE_NAME = "contactphone";
    
    
    protected int phoneID;
    protected int humanID;
    protected String phoneNumber;
    protected int extension;
    protected ContactPhoneType phoneType;
    protected String notes;
    protected LocalDateTime disconnectTS;
    protected User disconnectRecordedBy;
    
    public ContactPhone(){
        
    }

    /**
     * @return the phoneID
     */
    public int getPhoneID() {
        return phoneID;
    }

    /**
     * @return the phoneNumber
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * @return the extension
     */
    public int getExtension() {
        return extension;
    }

    /**
     * @return the phoneType
     */
    public ContactPhoneType getPhoneType() {
        return phoneType;
    }

    /**
     * @param phoneID the phoneID to set
     */
    public void setPhoneID(int phoneID) {
        this.phoneID = phoneID;
    }

    /**
     * @param phoneNumber the phoneNumber to set
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * @param extension the extension to set
     */
    public void setExtension(int extension) {
        this.extension = extension;
    }

    /**
     * @param phoneType the phoneType to set
     */
    public void setPhoneType(ContactPhoneType phoneType) {
        this.phoneType = phoneType;
    }

    @Override
    public String getNotes() {
        return notes;
    }

    @Override
    public void setNotes(String n) {
        this.notes = n;
    }

    @Override
    public int getDBKey() {
        return phoneID;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    /**
     * @return the humanID
     */
    public int getHumanID() {
        return humanID;
    }

    /**
     * @param humanID the humanID to set
     */
    public void setHumanID(int humanID) {
        this.humanID = humanID;
    }

    /**
     * @return the disconnectRecordedBy
     */
    public User getDisconnectRecordedBy() {
        return disconnectRecordedBy;
    }

    /**
     * @param disconnectRecordedBy the disconnectRecordedBy to set
     */
    public void setDisconnectRecordedBy(User disconnectRecordedBy) {
        this.disconnectRecordedBy = disconnectRecordedBy;
    }

    /**
     * @return the disconnectTS
     */
    public LocalDateTime getDisconnectTS() {
        return disconnectTS;
    }

    /**
     * @param disconnectTS the disconnectTS to set
     */
    public void setDisconnectTS(LocalDateTime disconnectTS) {
        this.disconnectTS = disconnectTS;
    }
    
}
