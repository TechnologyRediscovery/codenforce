/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author sylvia
 */
public class BOBSource implements Serializable {
    
    private int sourceid;
    private String title;
    private String description;
    private int creatorUserID;
    private Municipality muni;
    private boolean userattributable;
    private boolean active;
    private String notes;

    /**
     * @return the sourceid
     */
    public int getSourceid() {
        return sourceid;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }


    /**
     * @return the muni
     */
    public Municipality getMuni() {
        return muni;
    }

    /**
     * @return the userattributable
     */
    public boolean isUserattributable() {
        return userattributable;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param sourceid the sourceid to set
     */
    public void setSourceid(int sourceid) {
        this.sourceid = sourceid;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * @param muni the muni to set
     */
    public void setMuni(Municipality muni) {
        this.muni = muni;
    }

    /**
     * @param userattributable the userattributable to set
     */
    public void setUserattributable(boolean userattributable) {
        this.userattributable = userattributable;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @return the creatorUserID
     */
    public int getCreatorUserID() {
        return creatorUserID;
    }

    /**
     * @param creatorUserID the creatorUserID to set
     */
    public void setCreatorUserID(int creatorUserID) {
        this.creatorUserID = creatorUserID;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + this.sourceid;
        hash = 41 * hash + Objects.hashCode(this.title);
        hash = 41 * hash + Objects.hashCode(this.description);
        hash = 41 * hash + this.creatorUserID;
        hash = 41 * hash + Objects.hashCode(this.muni);
        hash = 41 * hash + (this.userattributable ? 1 : 0);
        hash = 41 * hash + (this.active ? 1 : 0);
        hash = 41 * hash + Objects.hashCode(this.notes);
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
        final BOBSource other = (BOBSource) obj;
        if (this.sourceid != other.sourceid) {
            return false;
        }
        if (this.creatorUserID != other.creatorUserID) {
            return false;
        }
        if (this.userattributable != other.userattributable) {
            return false;
        }
        if (this.active != other.active) {
            return false;
        }
        if (!Objects.equals(this.title, other.title)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.notes, other.notes)) {
            return false;
        }
        if (!Objects.equals(this.muni, other.muni)) {
            return false;
        }
        return true;
    }
    
    
    
}
