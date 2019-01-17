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
package com.tcvcog.tcvce.occupancy.entities;

/**
 *
 * @author Eric C. Darsow
 */
public class OccPermitApplicationReason {
    
    private int reasonid;
    private String reasontitle;
    private String reasonDescription;
    private boolean activereason;

    public int getReasonid() {
        return reasonid;
    }

    public void setReasonid(int reasonid) {
        this.reasonid = reasonid;
    }

    public String getReasontitle() {
        return reasontitle;
    }

    public void setReasontitle(String reasontitle) {
        this.reasontitle = reasontitle;
    }

    public String getReasonDescription() {
        return reasonDescription;
    }

    public void setReasonDescription(String reasonDescription) {
        this.reasonDescription = reasonDescription;
    }

    public boolean isActivereason() {
        return activereason;
    }

    public void setActivereason(boolean activereason) {
        this.activereason = activereason;
    }
    
    
    
}
