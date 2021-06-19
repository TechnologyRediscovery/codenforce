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
public interface IFace_trackedEntityLink {
    
    public LocalDateTime getLinkCreatedTS();
    public void setLinkCreatedTS(LocalDateTime ts);
    public User getLinkCreatedBy();
    public void setLinkCreatedBy(User usr);
    
    public LocalDateTime getLinkLastUpdatedTS();
    public void setLinkLastUpdatedTS(LocalDateTime ts);
    public User getLinkLastUpdatedBy();
    public void setLinkLastUpdatedBy(User usr);
    
    public LocalDateTime getLinkDeactivatedTS();
    public void setLinkDeactivatedTS(LocalDateTime ts);
    public void setLinkDeactivatedBy(User usr);
    public User getLinkDeactivatedBy();
    
    public boolean isLinkDeactivated();
    
    public void setLinkNotes(String n);
    public String getLinkNotes();
    
}
