/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.util.Objects;

/**
 *
 * @author sylvia
 */
public class Icon  {
    private int iconID;
    private String name;
    private String styleClass;
    private String fontAwesome;
    private String materialIcon;
    private Boolean active;

    /**
     * @return the iconID
     */
    public int getIconID() {
        return iconID;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + this.iconID;
        hash = 73 * hash + Objects.hashCode(this.name);
        hash = 73 * hash + Objects.hashCode(this.styleClass);
        hash = 73 * hash + Objects.hashCode(this.fontAwesome);
        hash = 73 * hash + Objects.hashCode(this.materialIcon);
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
        final Icon other = (Icon) obj;
        if (this.iconID != other.iconID) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.styleClass, other.styleClass)) {
            return false;
        }
        if (!Objects.equals(this.fontAwesome, other.fontAwesome)) {
            return false;
        }
        if (!Objects.equals(this.materialIcon, other.materialIcon)) {
            return false;
        }
        if (!Objects.equals(this.active, other.active)) {
            return false;
        }
        return true;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the styleClass
     */
    public String getStyleClass() {
        return styleClass;
    }

    /**
     * @return the fontAwesome
     */
    public String getFontAwesome() {
        return fontAwesome;
    }

    /**
     * @return the materialIcon
     */
    public String getMaterialIcon() {
        return materialIcon;
    }

    /**
     * @param iconID the iconID to set
     */
    public void setIconID(int iconID) {
        this.iconID = iconID;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param styleClass the styleClass to set
     */
    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    /**
     * @param fontAwesome the fontAwesome to set
     */
    public void setFontAwesome(String fontAwesome) {
        this.fontAwesome = fontAwesome;
    }

    /**
     * @param materialIcon the materialIcon to set
     */
    public void setMaterialIcon(String materialIcon) {
        this.materialIcon = materialIcon;
    }

    /**
     * @return the active
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(Boolean active) {
        this.active = active;
    }
    
}
