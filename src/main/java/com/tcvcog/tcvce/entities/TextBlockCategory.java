/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a family of related text blocks (sometimes known to users as
 * simply templates)
 * @author sylvia
 */
public class TextBlockCategory {
    private int categoryID;
    private String title;
    private Icon icon;
    private Municipality muni;
    private LocalDateTime deactivatedTS;

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.categoryID;
        hash = 89 * hash + Objects.hashCode(this.title);
        hash = 89 * hash + Objects.hashCode(this.icon);
        hash = 89 * hash + Objects.hashCode(this.muni);
        hash = 89 * hash + Objects.hashCode(this.deactivatedTS);
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
        final TextBlockCategory other = (TextBlockCategory) obj;
        if (this.categoryID != other.categoryID) {
            return false;
        }
        return true;
    }

    
    
    /**
     * @return the categoryID
     */
    public int getCategoryID() {
        return categoryID;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the icon
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * @return the muni
     */
    public Municipality getMuni() {
        return muni;
    }


    /**
     * @param categoryID the categoryID to set
     */
    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    /**
     * @param muni the muni to set
     */
    public void setMuni(Municipality muni) {
        this.muni = muni;
    }


    /**
     * @return the deactivatedTS
     */
    public LocalDateTime getDeactivatedTS() {
        return deactivatedTS;
    }

    /**
     * @param deactivatedTS the deactivatedTS to set
     */
    public void setDeactivatedTS(LocalDateTime deactivatedTS) {
        this.deactivatedTS = deactivatedTS;
    }
    
    
}
