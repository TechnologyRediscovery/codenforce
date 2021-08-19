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

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Base class for entities created during humanization
 * that contains creation, update, deactivation fields
 * 
 * @author sylvia
 */
public abstract class   TrackedEntity 
        implements      Serializable, 
                        IFace_keyIdentified{
    
    /** Humanization Object standard fields **/
    protected LocalDateTime createdTS;
    protected User createdBy;
    protected LocalDateTime lastUpdatedTS;
    protected User lastUpdatedBy;
    protected LocalDateTime deactivatedTS;
    protected User deactivatedBy;
    
    
    public static String getPrettyDate(LocalDateTime ldtDate){
        String formattedDateTime = "";
        if(ldtDate != null){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE dd MMM yyyy, HH:mm");
            formattedDateTime = ldtDate.format(formatter); 
            
        }
        return formattedDateTime;
    }
    
    
    /**
     * @return the createdTS
     */
    public LocalDateTime getCreatedTS() {
        return createdTS;
    }

    /**
     * @return the createdBy
     */
    public User getCreatedBy() {
        return createdBy;
    }

    /**
     * @return the lastUpdatedTS
     */
    public LocalDateTime getLastUpdatedTS() {
        return lastUpdatedTS;
    }

    /**
     * @return the lastUpdatedBy
     */
    public User getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    /**
     * @return the deactivatedTS
     */
    public LocalDateTime getDeactivatedTS() {
        return deactivatedTS;
    }

    /**
     * @return the deactivatedBy
     */
    public User getDeactivatedBy() {
        return deactivatedBy;
    }

   

    /**
     * @param createdTS the createdTS to set
     */
    public void setCreatedTS(LocalDateTime createdTS) {
        this.createdTS = createdTS;
    }

    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @param lastUpdatedTS the lastUpdatedTS to set
     */
    public void setLastUpdatedTS(LocalDateTime lastUpdatedTS) {
        this.lastUpdatedTS = lastUpdatedTS;
    }

    /**
     * @param lastUpdatedBy the lastUpdatedBy to set
     */
    public void setLastUpdatedBy(User lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    /**
     * @param deactivatedTS the deactivatedTS to set
     */
    public void setDeactivatedTS(LocalDateTime deactivatedTS) {
        this.deactivatedTS = deactivatedTS;
    }

    /**
     * @param deactivatedBy the deactivatedBy to set
     */
    public void setDeactivatedBy(User deactivatedBy) {
        this.deactivatedBy = deactivatedBy;
    }
    
    public boolean isActive(){
        return deactivatedTS == null;
    }

  

}
