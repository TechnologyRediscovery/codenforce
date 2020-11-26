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
 * Implemented by entities which have a deactivatedTS and a deactivatedby_userid
 * FK field in the DB (created as the standard during Humanization TurkeyDay2020
 * @author Ellen Bascomb (APT 31Y)
 */
public interface IFace_deactivatable
        extends IFace_keyIdentified{
    
    /**
     * Convenience method for getting a computed boolean 
     * based on the presence check for a deactivation timestamp
     * @return true if there is a deactivation time
     */
    public boolean isDeactivated();
    
    /**
     * Setter for the deactivatedby_userid field
     * @param u the User doing the deactivating
     */
    public void setDeactivatedBy(User u);
    
    /**
     * Getter for who deactivated the object
     * @return the user who deactivated the object
     */
    public User getDeactivatedBy();
    
    /**
     * setter for the deactivation timestamp
     * @param deacTS time of deactivation
     */
    public void setDactivatedTS(LocalDateTime deacTS);
    
    /**
     * Getter for the deactivation timestamp
     * @return the time the object was deactivated
     */
    public LocalDateTime getDeactivatedTS();
    
}
