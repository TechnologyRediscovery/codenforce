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


import java.time.LocalDate;
import java.util.Objects;

/**
 * The root object of the humanization system that replaces person records
 * in the database with human records. there is no longer such thing as a 
 * personID, only humanIDs. 
 * @author sylvia
 */
public  class   Human 
        extends TrackedEntity
        implements IFace_noteHolder{
    
        static final String TABLE_NAME = "public.human";
        static final String PKFIELD = "humanid";
        static final String HFNAME = "Person";
        
        
        protected int humanID;
        protected String name;
        protected LocalDate dob;
        protected boolean under18;
        
        protected String jobTitle;
        protected boolean businessEntity;
        protected boolean multiHuman;
        protected BOBSource source;
        
        protected LocalDate deceasedDate;
        protected User deceasedBy;
        
        protected int cloneOfHumanID;
        protected String notes;
        
        
    /**
     * No arg constructor
     */
    public Human(){
        
    }
      
    /**
     * Populates human fields given a human
     * @param h 
     */
    public Human(Human h){
        if(h != null){
            this.humanID = h.humanID;
            this.name = h.name;
            this.dob = h.dob;
            this.under18 = h.under18;
            this.jobTitle = h.jobTitle;
            this.businessEntity = h.businessEntity;
            this.multiHuman = h.multiHuman;
            this.source = h.source;
            this.deceasedDate = h.deceasedDate;
            this.deceasedBy = h.deceasedBy;
            this.cloneOfHumanID = h.cloneOfHumanID;
            this.notes = h.notes;
        }
    }
        
        
    /**
     * @return the humanID
     */
    public int getHumanID() {
        return humanID;
    }
    
    /**
     * Added for reverse compat with Persons who used to have First and Last
     * @return empty string
     */
    public String getFirstName(){
        return "";
    }
    
    /**
     * Added for reverse compat with Persons who used to have First and Last
     * @return the name
     */
    public String getLastName(){
        return name;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the dob
     */
    public LocalDate getDob() {
        return dob;
    }

    /**
     * @return the under18
     */
    public boolean isUnder18() {
        return under18;
    }

    /**
     * @return the jobTitle
     */
    public String getJobTitle() {
        return jobTitle;
    }

    /**
     * @return the businessEntity
     */
    public boolean isBusinessEntity() {
        return businessEntity;
    }

    /**
     * @return the multiHuman
     */
    public boolean isMultiHuman() {
        return multiHuman;
    }

    /**
     * @return the source
     */
    public BOBSource getSource() {
        return source;
    }

 
    /**
     * @return the deceasedDate
     */
    public LocalDate getDeceasedDate() {
        return deceasedDate;
    }

    /**
     * @return the deceasedBy
     */
    public User getDeceasedBy() {
        return deceasedBy;
    }

   
    /**
     * @param humanID the humanID to set
     */
    public void setHumanID(int humanID) {
        this.humanID = humanID;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param dob the dob to set
     */
    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    /**
     * @param under18 the under18 to set
     */
    public void setUnder18(boolean under18) {
        this.under18 = under18;
    }

    /**
     * @param jobTitle the jobTitle to set
     */
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    /**
     * @param businessEntity the businessEntity to set
     */
    public void setBusinessEntity(boolean businessEntity) {
        this.businessEntity = businessEntity;
    }

    /**
     * @param multiHuman the multiHuman to set
     */
    public void setMultiHuman(boolean multiHuman) {
        this.multiHuman = multiHuman;
    }

    /**
     * @param source the source to set
     */
    public void setSource(BOBSource source) {
        this.source = source;
    }

   

    /**
     * @param deceasedDate the deceasedDate to set
     */
    public void setDeceasedDate(LocalDate deceasedDate) {
        this.deceasedDate = deceasedDate;
    }

    /**
     * @param deceasedBy the deceasedBy to set
     */
    public void setDeceasedBy(User deceasedBy) {
        this.deceasedBy = deceasedBy;
    }

  

    /**
     * @return the cloneOfHumanID
     */
    public int getCloneOfHumanID() {
        return cloneOfHumanID;
    }

    /**
     * @param cloneOfHumanID the cloneOfHumanID to set
     */
    public void setCloneOfHumanID(int cloneOfHumanID) {
        this.cloneOfHumanID = cloneOfHumanID;
    }


    @Override
    public int getDBKey() {
        return humanID;
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
        return PKFIELD;
    }

    @Override
    public String getDBTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getNoteHolderFriendlyName() {
        return HFNAME;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + this.humanID;
        hash = 29 * hash + Objects.hashCode(this.name);
        hash = 29 * hash + Objects.hashCode(this.dob);
        hash = 29 * hash + (this.under18 ? 1 : 0);
        hash = 29 * hash + Objects.hashCode(this.jobTitle);
        hash = 29 * hash + (this.businessEntity ? 1 : 0);
        hash = 29 * hash + (this.multiHuman ? 1 : 0);
        hash = 29 * hash + Objects.hashCode(this.source);
        hash = 29 * hash + Objects.hashCode(this.deceasedDate);
        hash = 29 * hash + Objects.hashCode(this.deceasedBy);
        hash = 29 * hash + this.cloneOfHumanID;
        hash = 29 * hash + Objects.hashCode(this.notes);
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
        final Human other = (Human) obj;
        if (this.humanID != other.humanID) {
            return false;
        }
        return true;
    }

    
    
    
    
}
