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

/**
 *
 * @author sylvia
 */
public abstract class Choice implements Proposable, Serializable {

    protected boolean active;
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
    
}
