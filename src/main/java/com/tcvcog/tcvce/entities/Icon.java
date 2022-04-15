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
public class Icon extends Managed{
    private String styleClass;
    private String fontAwesome;
    private String materialIcon;
    
    
    public Icon(){
        super(ManagedSchemaEnum.Icon);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + getID();
        hash = 73 * hash + Objects.hashCode(getTitle());
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
        if (getID() != other.getID()) {
            return false;
        }
        if (!Objects.equals(this.getTitle(), other.getTitle())) {
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
}
