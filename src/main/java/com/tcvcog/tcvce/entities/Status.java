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
public abstract class Status implements Serializable {
    
    protected int statusID;
    protected String statusTitle;
    protected String description;
    private Icon icon;

    /**
     * @return the statusID
     */
    public int getStatusID() {
        return statusID;
    }

    /**
     * @return the statusTitle
     */
    public String getStatusTitle() {
        return statusTitle;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param statusID the statusID to set
     */
    public void setStatusID(int statusID) {
        this.statusID = statusID;
    }

    /**
     * @param statusTitle the statusTitle to set
     */
    public void setStatusTitle(String statusTitle) {
        this.statusTitle = statusTitle;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + this.statusID;
        hash = 79 * hash + Objects.hashCode(this.statusTitle);
        hash = 79 * hash + Objects.hashCode(this.description);
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
        if (!obj.equals(this.statusTitle)) {
            return false;
        }
        if (!obj.equals(this.description)) {
            return false;
        }
        return true;
    }

    /**
     * @return the icon
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(Icon icon) {
        this.icon = icon;
    }
    
}
