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
package com.tcvcog.tcvce.entities;

import java.io.Serializable;
import java.util.Objects;


 /**s 
 * @author Eric Darsow
 */
public class EventCategory implements Serializable {
    
    private EventType eventType;
    private int categoryID;
    private String eventCategoryTitle;
    private String eventCategoryDesc;
    
    private boolean userdeployable;
    private boolean munideployable;
    private boolean publicdeployable;
    
    private boolean notifycasemonitors;
    
    private EventProposal eventProposal;
    
    private boolean hidable;
    private boolean requestable;
    
    private Icon icon;
    
    private int relativeOrderWithinType;
    private int relativeOrderGlobal;
    private String hostEventDescriptionSuggestedText;
    
    /**
     * @return the eventType
     */
    public EventType getEventType() {
        return eventType;
    }

    /**
     * @return the categoryID
     */
    public int getCategoryID() {
        return categoryID;
    }

    /**
     * @return the eventCategoryTitle
     */
    public String getEventCategoryTitle() {
        return eventCategoryTitle;
    }

    /**
     * @return the eventCategoryDesc
     */
    public String getEventCategoryDesc() {
        return eventCategoryDesc;
    }

    /**
     * @param eventType the eventType to set
     */
    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    /**
     * @param categoryID the categoryID to set
     */
    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    /**
     * @param eventCategoryTitle the eventCategoryTitle to set
     */
    public void setEventCategoryTitle(String eventCategoryTitle) {
        this.eventCategoryTitle = eventCategoryTitle;
    }

    /**
     * @param eventCategoryDesc the eventCategoryDesc to set
     */
    public void setEventCategoryDesc(String eventCategoryDesc) {
        this.eventCategoryDesc = eventCategoryDesc;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.eventType);
        hash = 59 * hash + this.categoryID;
        hash = 59 * hash + Objects.hashCode(this.eventCategoryTitle);
        hash = 59 * hash + Objects.hashCode(this.eventCategoryDesc);
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
        final EventCategory other = (EventCategory) obj;
        if (this.categoryID != other.categoryID) {
            return false;
        }
        if (!Objects.equals(this.eventCategoryTitle, other.eventCategoryTitle)) {
            return false;
        }
        if (!Objects.equals(this.eventCategoryDesc, other.eventCategoryDesc)) {
            return false;
        }
        if (this.eventType != other.eventType) {
            return false;
        }
        return true;
    }

    /**
     * @return the userdeployable
     */
    public boolean isUserdeployable() {
        return userdeployable;
    }

    /**
     * @return the munideployable
     */
    public boolean isMunideployable() {
        return munideployable;
    }

    /**
     * @return the publicdeployable
     */
    public boolean isPublicdeployable() {
        return publicdeployable;
    }

   
    /**
     * @return the notifycasemonitors
     */
    public boolean isNotifycasemonitors() {
        return notifycasemonitors;
    }

   

    /**
     * @return the hidable
     */
    public boolean isHidable() {
        return hidable;
    }

    /**
     * @param userdeployable the userdeployable to set
     */
    public void setUserdeployable(boolean userdeployable) {
        this.userdeployable = userdeployable;
    }

    /**
     * @param munideployable the munideployable to set
     */
    public void setMunideployable(boolean munideployable) {
        this.munideployable = munideployable;
    }

    /**
     * @param publicdeployable the publicdeployable to set
     */
    public void setPublicdeployable(boolean publicdeployable) {
        this.publicdeployable = publicdeployable;
    }

   
    /**
     * @param notifycasemonitors the notifycasemonitors to set
     */
    public void setNotifycasemonitors(boolean notifycasemonitors) {
        this.notifycasemonitors = notifycasemonitors;
    }

    

    /**
     * @param hidable the hidable to set
     */
    public void setHidable(boolean hidable) {
        this.hidable = hidable;
    }

    /**
     * @return the icon
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    /**
     * @return the requestable
     */
    public boolean isRequestable() {
        return requestable;
    }

    /**
     * @param requestable the requestable to set
     */
    public void setRequestable(boolean requestable) {
        this.requestable = requestable;
    }


    /**
     * @return the eventProposal
     */
    public EventProposal getEventProposal() {
        return eventProposal;
    }

    /**
     * @param eventProposal the eventProposal to set
     */
    public void setEventProposal(EventProposal eventProposal) {
        this.eventProposal = eventProposal;
    }

    /**
     * @return the relativeOrderWithinType
     */
    public int getRelativeOrderWithinType() {
        return relativeOrderWithinType;
    }

    /**
     * @return the relativeOrderGlobal
     */
    public int getRelativeOrderGlobal() {
        return relativeOrderGlobal;
    }

    /**
     * @return the hostEventDescriptionSuggestedText
     */
    public String getHostEventDescriptionSuggestedText() {
        return hostEventDescriptionSuggestedText;
    }

    /**
     * @param relativeOrderWithinType the relativeOrderWithinType to set
     */
    public void setRelativeOrderWithinType(int relativeOrderWithinType) {
        this.relativeOrderWithinType = relativeOrderWithinType;
    }

    /**
     * @param relativeOrderGlobal the relativeOrderGlobal to set
     */
    public void setRelativeOrderGlobal(int relativeOrderGlobal) {
        this.relativeOrderGlobal = relativeOrderGlobal;
    }

    /**
     * @param hostEventDescriptionSuggestedText the hostEventDescriptionSuggestedText to set
     */
    public void setHostEventDescriptionSuggestedText(String hostEventDescriptionSuggestedText) {
        this.hostEventDescriptionSuggestedText = hostEventDescriptionSuggestedText;
    }

    
    
    
    
}
