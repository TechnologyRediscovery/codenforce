/*
 * Copyright (C) 2018 Turtle Creek Valley
Council of Governments, PA
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

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class TextBlock implements Serializable, Comparable<TextBlock>{
    
    private int blockID;
    private int textBlockCategoryID;
    private String textBlockCategoryTitle;
    private Municipality muni;
    private String textBlockName;
    private String textBlockText;
    private int placementOrder;
    private boolean injectableTemplate;
    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.blockID;
        hash = 97 * hash + this.textBlockCategoryID;
        hash = 97 * hash + Objects.hashCode(this.textBlockCategoryTitle);
        hash = 97 * hash + Objects.hashCode(this.muni);
        hash = 97 * hash + Objects.hashCode(this.textBlockName);
        hash = 97 * hash + Objects.hashCode(this.textBlockText);
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
        final TextBlock other = (TextBlock) obj;
        if (this.blockID != other.blockID) {
            return false;
        }
        if (this.textBlockCategoryID != other.textBlockCategoryID) {
            return false;
        }
        if (!Objects.equals(this.textBlockCategoryTitle, other.textBlockCategoryTitle)) {
            return false;
        }
        if (!Objects.equals(this.textBlockName, other.textBlockName)) {
            return false;
        }
        if (!Objects.equals(this.textBlockText, other.textBlockText)) {
            return false;
        }
        if (!Objects.equals(this.muni, other.muni)) {
            return false;
        }
        return true;
    }

    /**
     * @return the textBlockText
     */
    public String getTextBlockText() {
        return textBlockText;
    }

    /**
     * @param textBlockText the textBlockText to set
     */
    public void setTextBlockText(String textBlockText) {
        this.textBlockText = textBlockText;
    }

    /**
     * @return the blockID
     */
    public int getBlockID() {
        return blockID;
    }

    /**
     * @return the textBlockCategoryID
     */
    public int getTextBlockCategoryID() {
        return textBlockCategoryID;
    }

    /**
     * @return the muni
     */
    public Municipality getMuni() {
        return muni;
    }

    /**
     * @return the textBlockName
     */
    public String getTextBlockName() {
        return textBlockName;
    }

    /**
     * @param blockID the blockID to set
     */
    public void setBlockID(int blockID) {
        this.blockID = blockID;
    }

    /**
     * @param textBlockCategoryID the textBlockCategoryID to set
     */
    public void setTextBlockCategoryID(int textBlockCategoryID) {
        this.textBlockCategoryID = textBlockCategoryID;
    }

    /**
     * @param muni the muni to set
     */
    public void setMuni(Municipality muni) {
        this.muni = muni;
    }

    /**
     * @param textBlockName the textBlockName to set
     */
    public void setTextBlockName(String textBlockName) {
        this.textBlockName = textBlockName;
    }

    /**
     * @return the textBlockCategoryTitle
     */
    public String getTextBlockCategoryTitle() {
        return textBlockCategoryTitle;
    }

    /**
     * @param textBlockCategoryTitle the textBlockCategoryTitle to set
     */
    public void setTextBlockCategoryTitle(String textBlockCategoryTitle) {
        this.textBlockCategoryTitle = textBlockCategoryTitle;
    }

    /**
     * @return the placementOrder
     */
    public int getPlacementOrder() {
        return placementOrder;
    }

    /**
     * @param placementOrder the placementOrder to set
     */
    public void setPlacementOrder(int placementOrder) {
        this.placementOrder = placementOrder;
    }

    @Override
    public int compareTo(TextBlock o) {
        if(o== null){
            return 0;
        }
        if(o.placementOrder > this.placementOrder){
            return 1;
        } else if (o.placementOrder < this.placementOrder){
            return -1;
        }
        return 0;
    }

    /**
     * @return the injectableTemplate
     */
    public boolean isInjectableTemplate() {
        return injectableTemplate;
    }

    /**
     * @param injectableTemplate the injectableTemplate to set
     */
    public void setInjectableTemplate(boolean injectableTemplate) {
        this.injectableTemplate = injectableTemplate;
    }

    
}
