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
public  class   ContactEmail 
        extends Contact{
    
    final static String TABLE_NAME = "contactemail";
    final static String PKFIELD = "emailid";
    
    protected int emailID;
    protected int humanID;
    protected String emailaddress;
    protected LocalDateTime bounceTS;
    protected String notes;

    public ContactEmail(){
        
    }
    
    /**
     * @return the emailID
     */
    public int getEmailID() {
        return emailID;
    }

    /**
     * @return the emailaddress
     */
    public String getEmailaddress() {
        return emailaddress;
    }

    /**
     * @return the bounceTS
     */
    public LocalDateTime getBounceTS() {
        return bounceTS;
    }

    /**
     * @param emailID the emailID to set
     */
    public void setEmailID(int emailID) {
        this.emailID = emailID;
    }

    /**
     * @param emailaddress the emailaddress to set
     */
    public void setEmailaddress(String emailaddress) {
        this.emailaddress = emailaddress;
    }

    /**
     * @param bounceTS the bounceTS to set
     */
    public void setBounceTS(LocalDateTime bounceTS) {
        this.bounceTS = bounceTS;
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
        return emailID;
    }

   
    @Override
    public String getPKFieldName() {
        return PKFIELD;
    }

    @Override
    public String getDBTableName() {
        return TABLE_NAME;
    }
    
}
