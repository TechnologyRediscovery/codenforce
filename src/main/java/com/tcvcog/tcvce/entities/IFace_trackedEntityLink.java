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
 * Unified interface for link objects created during Humanization TurkeyDay2020
 * 
 * @author Ellen Bascomb (APT 31Y)
 */
public  interface   IFace_trackedEntityLink 
        extends     IFace_keyIdentified{
    
    public LocalDateTime getLinkCreatedTS();
    public void setLinkCreatedTS(LocalDateTime ts);
    public int getLinkCreatedByUserID();
    public void setLinkCreatedByUserID(int userID);
    
    public void setLinkSource(BOBSource source);
    public BOBSource getLinkSource();
    
    public LocalDateTime getLinkLastUpdatedTS();
    public void setLinkLastUpdatedTS(LocalDateTime ts);
    public int getLinkLastUpdatedByUserID();
    public void setLinkLastUpdatedByUserID(int userID);
    
    public LocalDateTime getLinkDeactivatedTS();
    public void setLinkDeactivatedTS(LocalDateTime ts);
    public int getLinkDeactivatedByUserID();
    public void setLinkDeactivatedByUserID(int usrID);
    
    public boolean isLinkDeactivated();
    
    public void setLinkNotes(String n);
    public String getLinkNotes();
    
    public LinkedObjectRole getLinkedObjectRole();
    public void setLinkedObjectRole(LinkedObjectRole lor);
    
    public int getParentObjectID();
    
}
