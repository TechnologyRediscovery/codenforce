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
package com.tcvcog.tcvce.entities;

import java.time.LocalDateTime;

/**
 * Represents a single street
 * @author Ellen Bascomb of 31Y
 */
public class MailingStreet extends TrackedEntity{
    
    private int streetID;
    private String name;
    private MailingCityStateZip cityStateZip;
    private String notes;
    private boolean poBox;
    
    
    final String TABLE_NAME = "mailingstreet";
    final String PK_NAME = "streetid";
    

    /**
     * @return the streetID
     */
    public int getStreetID() {
        return streetID;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the cityStateZip
     */
    public MailingCityStateZip getCityStateZip() {
        return cityStateZip;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @return the poBox
     */
    public boolean isPoBox() {
        return poBox;
    }

  

    /**
     * @param streetID the streetID to set
     */
    public void setStreetID(int streetID) {
        this.streetID = streetID;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param cityStateZip the cityStateZip to set
     */
    public void setCityStateZip(MailingCityStateZip cityStateZip) {
        this.cityStateZip = cityStateZip;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @param poBox the poBox to set
     */
    public void setPoBox(boolean poBox) {
        this.poBox = poBox;
    }

  

    @Override
    public String getPKFieldName() {
        return PK_NAME;
    }

    @Override
    public int getDBKey() {
        return streetID;
    }

    @Override
    public String getDBTableName() {
       return TABLE_NAME;
    }
    
}
