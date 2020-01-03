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
import java.util.List;

/**
 *
 * @author sylvia
 */
public class EventRuleSet  implements Serializable {
    
    private int rulseSetID;
    private String title;
    private String description;
    private List<EventRuleAbstract> ruleList;

    /**
     * @return the rulseSetID
     */
    public int getRulseSetID() {
        return rulseSetID;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the ruleList
     */
    public List<EventRuleAbstract> getRuleList() {
        return ruleList;
    }

    /**
     * @param rulseSetID the rulseSetID to set
     */
    public void setRulseSetID(int rulseSetID) {
        this.rulseSetID = rulseSetID;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param ruleList the ruleList to set
     */
    public void setRuleList(List<EventRuleAbstract> ruleList) {
        this.ruleList = ruleList;
    }
    
}
