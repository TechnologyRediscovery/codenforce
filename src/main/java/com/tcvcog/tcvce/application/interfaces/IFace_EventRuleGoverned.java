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
package com.tcvcog.tcvce.application.interfaces;

import com.tcvcog.tcvce.entities.Event;
import com.tcvcog.tcvce.entities.EventRuleImplementation;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsEventRulesEnum;
import java.util.List;

/**
 * Interface to unify communication between Occperiods and CECases
 * which both have EventRule objects which influence their statuses, etc.
 * @author sylvia
 */
public interface IFace_EventRuleGoverned {
    public void setEventList(List<Event> lst);
    public List<Event> assembleEventList(ViewOptionsActiveHiddenListsEnum voahle);
    
    public void setEventRuleList(List<EventRuleImplementation> lst);
    public List<EventRuleImplementation> assembleEventRuleList(ViewOptionsEventRulesEnum voere);
    
    public boolean isAllRulesPassed();
}
