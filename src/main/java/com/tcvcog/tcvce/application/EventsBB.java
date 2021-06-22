/*
 * Copyright (C) 2021 Technology Rediscovery LLC
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
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventCnFPropUnitCasePeriodHeavy;
import com.tcvcog.tcvce.entities.EventDomainEnum;
import com.tcvcog.tcvce.entities.IFace_EventHolder;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class EventsBB extends BackingBeanUtils implements Serializable {

    private IFace_EventHolder currentEventHolder;
    private List<EventCnF> eventList;

    // Single selected event for editing and viewing
    private EventCnF selectedEvent;

    public EventsBB() {}

    @PostConstruct
    public void initBean() {
        updateEventHolder();
    }

    /**
     * Grabs the current page event type from the sessionBean and uses that to populate
     * the currentEventHolder object with something we can grab events from!
     */
    public void updateEventHolder() {
        SessionBean sb = getSessionBean();

        EventDomainEnum pageEventDomain = sb.getSessEventsPageEventDomainRequest();
        switch (pageEventDomain) {
            case CODE_ENFORCEMENT:
                currentEventHolder = sb.getSessCECase();
                break;
            case OCCUPANCY:
                currentEventHolder = sb.getSessOccPeriod();
                break;
            case UNIVERSAL:
                System.out.println("EventsBB reached universal case in updateEventHolder()--do something about this maybe?");
                break;
        }
        updateEventList();
    }

    /**
     * Repopulates eventList parameter with the latest and greatest events from
     * our special guest the event-containing object.
     */
    public void updateEventList() {
        if (currentEventHolder != null)
            eventList = currentEventHolder.getEventList();
    }

    //
    // Getters and setters start here
    //

    public List<EventCnF> getEventList() {
        return eventList;
    }

    public int getEventListSize() {
        int size = 0;
        if (eventList != null)
            size = eventList.size();

        return size;
    }

    public EventCnF getSelectedEvent() {
        return selectedEvent;
    }

    public void setSelectedEvent(EventCnF selectedEvent) {
        this.selectedEvent = selectedEvent;
    }
}
