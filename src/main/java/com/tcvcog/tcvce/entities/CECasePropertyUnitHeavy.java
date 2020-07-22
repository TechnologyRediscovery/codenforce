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

import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author sylvia
 */
public class CECasePropertyUnitHeavy
        extends CECase {

    protected Property property;
    protected PropertyUnit propUnit;
    protected boolean showHiddenEvents;
    protected boolean showInactiveEvents;
    protected List<EventCnF> eventList;
    protected List<EventCnF> completeEventList;

    public CECasePropertyUnitHeavy(CECase cse) {
        this.caseID = cse.caseID;
        this.publicControlCode = cse.publicControlCode;
        this.paccEnabled = cse.paccEnabled;

        this.allowForwardLinkedPublicAccess = cse.allowForwardLinkedPublicAccess;

        this.propertyID = cse.propertyID;
        this.propertyUnitID = cse.propertyUnitID;

        this.caseManager = cse.caseManager;
        this.caseName = cse.caseName;

        this.casePhase = cse.casePhase;
        this.casePhaseIcon = cse.casePhaseIcon;

        this.originationDate = cse.originationDate;
        this.closingDate = cse.closingDate;
        this.creationTimestamp = cse.creationTimestamp;

        this.notes = cse.notes;

        this.source = cse.source;

        this.citationList = cse.citationList;
        this.noticeList = cse.noticeList;
        this.violationList = cse.violationList;
       
        this.active = cse.active;
        this.propertyInfoCase = cse.propertyInfoCase;
        this.personInfoPersonID = cse.getPersonInfoPersonID();
        
        this.lastUpdatedBy = cse.getLastUpdatedBy();
        this.lastUpdatedTS = cse.getLastUpdatedTS();

    }

    /**
     * @return the property
     */
    public Property getProperty() {
        return property;
    }

    /**
     * @return the propUnit
     */
    public PropertyUnit getPropUnit() {
        return propUnit;
    }

    /**
     * @param property the property to set
     */
    public void setProperty(Property property) {
        this.property = property;
    }

    /**
     * @param propUnit the propUnit to set
     */
    public void setPropUnit(PropertyUnit propUnit) {
        this.propUnit = propUnit;
    }

    /**
     * Implements logic to check each event for hidden status and inactive
     * status and based on the value of the showHiddenEvents and
     * showInactiveEvents flags, add the event from the complete list to the
     * visible list
     *
     * @return the visibleEventList
     */
    public List<EventCnF> getVisibleEventList() {
        List<EventCnF> visEventList = new ArrayList<>();
        for (EventCnF ev : completeEventList) {
            if (!ev.isActive() && !showInactiveEvents) {
                continue;
            }
            if (ev.isHidden() && !showHiddenEvents) {
                continue;
            }
            visEventList.add(ev);
        } // close for   
        return visEventList;
    }

    /**
     * @return the activeEventList
     */
    public List<EventCnF> getActiveEventList() {
        List<EventCnF> actEvList = new ArrayList<>();
        Iterator<EventCnF> iter = completeEventList.iterator();
        while (iter.hasNext()) {
            EventCnF ev = iter.next();
            if (ev.isActive()) {
                actEvList.add(ev);
            }
        }
        return actEvList;
    }
    
    public List<EventCnF> assembleEventList(ViewOptionsActiveHiddenListsEnum voahle) {
        List<EventCnF> visEventList = new ArrayList<>();
        if (eventList != null) {
            for (EventCnF ev : eventList) {
                switch (voahle) {
                    case VIEW_ACTIVE_HIDDEN:
                        if (ev.isActive()
                                && ev.isHidden()) {
                            visEventList.add(ev);
                        }
                        break;
                    case VIEW_ACTIVE_NOTHIDDEN:
                        if (ev.isActive()
                                && !ev.isHidden()) {
                            visEventList.add(ev);
                        }
                        break;
                    case VIEW_ALL:
                        visEventList.add(ev);
                        break;
                    case VIEW_INACTIVE:
                        if (!ev.isActive()) {
                            visEventList.add(ev);
                        }
                        break;
                    default:
                        visEventList.add(ev);
                } // close switch
            } // close for   
        } // close null check
        return visEventList;
    }

    public boolean isShowHiddenEvents() {
        return showHiddenEvents;
    }

    public void setShowHiddenEvents(boolean showHiddenEvents) {
        this.showHiddenEvents = showHiddenEvents;
    }

    public boolean isShowInactiveEvents() {
        return showInactiveEvents;
    }

    public void setShowInactiveEvents(boolean showInactiveEvents) {
        this.showInactiveEvents = showInactiveEvents;
    }

    public List<EventCnF> getCompleteEventList() {
        return completeEventList;
    }

    public void setCompleteEventList(List<EventCnF> completeEventList) {
        this.completeEventList = completeEventList;
    }

    public List<EventCnF> getEventList() {
        return eventList;
    }

    public void setEventList(List<EventCnF> eventList) {
        this.eventList = eventList;
    }

}
