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
package com.tcvcog.tcvce.entities.occupancy;

import java.util.Objects;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class OccPermitApplicationReason {
    
    private int id;
    private String title;
    private String description;
    private boolean active;
    private OccAppPersonRequirement personsRequirement;
    private String humanFriendlyDescription;
    private OccPeriodType proposalPeriodType;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + this.id;
        hash = 89 * hash + Objects.hashCode(this.title);
        hash = 89 * hash + Objects.hashCode(this.description);
        hash = 89 * hash + (this.active ? 1 : 0);
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
        final OccPermitApplicationReason other = (OccPermitApplicationReason) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    /**
     * @return the personsRequirement
     */
    public OccAppPersonRequirement getPersonsRequirement() {
        return personsRequirement;
    }

    /**
     * @param personsRequirement the personsRequirement to set
     */
    public void setPersonsRequirement(OccAppPersonRequirement personsRequirement) {
        this.personsRequirement = personsRequirement;
    }    

    /**
     * @return the humanFriendlyDescription
     */
    public String getHumanFriendlyDescription() {
        return humanFriendlyDescription;
    }

    /**
     * @param humanFriendlyDescription the humanFriendlyDescription to set
     */
    public void setHumanFriendlyDescription(String humanFriendlyDescription) {
        this.humanFriendlyDescription = humanFriendlyDescription;
    }

    public OccPeriodType getProposalPeriodType() {
        return proposalPeriodType;
    }

    public void setProposalPeriodType(OccPeriodType proposalPeriodType) {
        this.proposalPeriodType = proposalPeriodType;
    }
    
}
