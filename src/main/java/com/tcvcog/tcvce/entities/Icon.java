/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 *
 * @author sylvia
 */
public class Icon implements Manageable{
    private String styleClass;
    private String fontAwesome;
    private String materialIcon;
    private int ID;
    private String name;
    private LocalDateTime deactivatedts;
    private final static ManagedSchemaEnum MANAGABLE_SCHEMA = ManagedSchemaEnum.Icon;
    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + getID();
        hash = 73 * hash + Objects.hashCode(getName());
        hash = 73 * hash + Objects.hashCode(this.getStyleClass());
        hash = 73 * hash + Objects.hashCode(this.getFontAwesome());
        hash = 73 * hash + Objects.hashCode(this.getMaterialIcon());
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
        if (getID() != other.getID()) {
            return false;
        }
        if (!Objects.equals(this.getName(), other.getName())) {
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
        if (!Objects.equals(this.getDeactivatedts(), other.getDeactivatedts())) {
            return false;
        }
        return true;
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
     * @return the ID
     */
    @Override
    public int getID() {
        return ID;
    }

    /**
     * @param ID the ID to set
     */
    @Override
    public void setID(int ID) {
        this.ID = ID;
    }

    /**
     * @return the name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @param title the name to set
     */
    @Override
    public void setName(String title) {
        this.name = title;
    }

    /**
     * @return the deactivatedts
     */
    @Override
    public LocalDateTime getDeactivatedts() {
        return deactivatedts;
    }

    /**
     * @param deactivatedts the deactivatedts to set
     */
    @Override
    public void setDeactivatedts(LocalDateTime deactivatedts) {
        this.deactivatedts = deactivatedts;
    }

    /**
     * @return the MANAGABLE_SCHEMA
     */
    @Override
    public ManagedSchemaEnum getMANAGEABLE_SCHEMA() {
        return MANAGABLE_SCHEMA;
    }
    
    /**
     * 
     * @return iconID
     */
    @Override
    public Icon getIcon() {
        return this;
    }
}
