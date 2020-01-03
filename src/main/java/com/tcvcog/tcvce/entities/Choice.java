/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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
 * @author sylvia
 */
public  abstract class  Choice 
        implements      IFace_Proposable, 
                        Serializable, 
                        Comparable<Choice> {

    protected boolean hidden;
    protected boolean active;
    protected boolean canChoose;
    protected int choiceID;
    protected String description;
    private Icon icon;
    protected int minimumRequiredUserRankToChoose;
    protected int minimumRequiredUserRankToView;
    protected int relativeOrder;
    protected String title;
    

    @Override
    public int getChoiceID() {
        return choiceID;
    }

    /**
     *
     * @return
     */
    @Override
    public String getDescription() {
        return description;
    }

   
    /**
     *
     * @return
     */
    @Override
    public Icon getIcon() {
        return icon;
    }

    /**
     *
     * @return
     */
    @Override
    public int getMinimumRequiredUserRankToChoose() {
        return minimumRequiredUserRankToChoose;
    }

    /**
     *
     * @return
     */
    @Override
    public int getMinimumRequiredUserRankToView() {
        return minimumRequiredUserRankToView;
    }

    @Override
    public int getRelativeOrder() {
        return relativeOrder;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public boolean isActive() {
        return active;
    }


    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }


    /**
     * @param choiceID the choiceID to set
     */
    public void setChoiceID(int choiceID) {
        this.choiceID = choiceID;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * @param minimumRequiredUserRankToChoose the minimumRequiredUserRankToChoose to set
     */
    public void setMinimumRequiredUserRankToChoose(int minimumRequiredUserRankToChoose) {
        this.minimumRequiredUserRankToChoose = minimumRequiredUserRankToChoose;
    }

    /**
     * @param minimumRequiredUserRankToView the minimumRequiredUserRankToView to set
     */
    public void setMinimumRequiredUserRankToView(int minimumRequiredUserRankToView) {
        this.minimumRequiredUserRankToView = minimumRequiredUserRankToView;
    }

    /**
     * @param relativeOrder the relativeOrder to set
     */
    public void setRelativeOrder(int relativeOrder) {
        this.relativeOrder = relativeOrder;
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
     * @return the hidden
     */
    @Override
    public boolean isHidden() {
        return hidden;
    }

    /**
     * @param hidden the hidden to set
     */
    @Override
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    public void setCanChoose(boolean ch){
        this.canChoose = ch;
    }

    @Override
    public  boolean isCanChoose(){
        return canChoose;
        
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.hidden ? 1 : 0);
        hash = 97 * hash + (this.active ? 1 : 0);
        hash = 97 * hash + (this.canChoose ? 1 : 0);
        hash = 97 * hash + this.choiceID;
        hash = 97 * hash + Objects.hashCode(this.description);
        hash = 97 * hash + Objects.hashCode(this.icon);
        hash = 97 * hash + this.minimumRequiredUserRankToChoose;
        hash = 97 * hash + this.minimumRequiredUserRankToView;
        hash = 97 * hash + this.relativeOrder;
        hash = 97 * hash + Objects.hashCode(this.title);
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
        final Choice other = (Choice) obj;
        if (this.choiceID != other.choiceID) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Choice o){
        if(this.relativeOrder > o.getRelativeOrder()){
            return 1;
        } else if (this.relativeOrder < o.getRelativeOrder()){
            return -1;
        } else {
            return 0;
        }
    }

    
    
}
