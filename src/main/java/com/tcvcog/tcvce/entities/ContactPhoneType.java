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

import java.time.LocalDateTime;

/**
 *
 * @author sylvia
 */
public class ContactPhoneType {
    protected int phoneTypeID;
    protected String title;
    protected LocalDateTime createdTS;
    protected LocalDateTime deactivatedTS;

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

    /**
     * @return the phoneTypeID
     */
    public int getPhoneTypeID() {
        return phoneTypeID;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the createdTS
     */
    public LocalDateTime getCreatedTS() {
        return createdTS;
    }

    /**
     * @param phoneTypeID the phoneTypeID to set
     */
    public void setPhoneTypeID(int phoneTypeID) {
        this.phoneTypeID = phoneTypeID;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @param createdTS the createdTS to set
     */
    public void setCreatedTS(LocalDateTime createdTS) {
        this.createdTS = createdTS;
    }
    
}
