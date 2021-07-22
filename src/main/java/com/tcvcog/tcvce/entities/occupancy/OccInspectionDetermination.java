package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.entities.EventCategory;

public class OccInspectionDetermination {

    private int determinationID;

    private String title;
    private String description;

    private String notes;

    // This could be the ID? depends if the object is too heavy with it's icon and stuff
    private EventCategory eventCategory;

    private boolean active;

    public OccInspectionDetermination() {}

    public int getDeterminationID() {
        return determinationID;
    }

    public void setDeterminationID(int determinationID) {
        this.determinationID = determinationID;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public EventCategory getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(EventCategory eventCategory) {
        this.eventCategory = eventCategory;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
