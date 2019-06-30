/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.io.Serializable;

/**
 *
 * @author sylvia
 */
public class EventChoice 
        extends EntityUtils 
        implements Proposable, Serializable {
    
    private int choiceID;
    private String title;
    private String description;
    private EventCategory eventCategory;
    private EventRule eventRule;
    private boolean addEventRule;
    private int relativeOrder;
    private boolean active;
    private int minimumRequiredUserRankToView;
    private int minimumRequiredUserRankToChoose;
    
    private Icon icon;
    

    @Override
    public int getChoiceID() {
        return choiceID;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public EventCategory getEventCategory() {
        return eventCategory;
    }

    @Override
    public EventRule getEventRule() {
        return eventRule;
    }

    @Override
    public boolean isAddEventRule() {
        return addEventRule;
    }

    @Override
    public int getRelativeOrder() {
        return relativeOrder;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public int getMinimumRequiredUserRankToView() {
        return minimumRequiredUserRankToView;
    }

    @Override
    public int getMinimumRequiredUserRankToChoose() {
        return minimumRequiredUserRankToChoose;
    }

    /**
     * @param choiceID the choiceID to set
     */
    public void setChoiceID(int choiceID) {
        this.choiceID = choiceID;
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
     * @param eventCategory the eventCategory to set
     */
    public void setEventCategory(EventCategory eventCategory) {
        this.eventCategory = eventCategory;
    }

    /**
     * @param eventRule the eventRule to set
     */
    public void setEventRule(EventRule eventRule) {
        this.eventRule = eventRule;
    }

    /**
     * @param addEventRule the addEventRule to set
     */
    public void setAddEventRule(boolean addEventRule) {
        this.addEventRule = addEventRule;
    }

    /**
     * @param relativeOrder the relativeOrder to set
     */
    public void setRelativeOrder(int relativeOrder) {
        this.relativeOrder = relativeOrder;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @param minimumRequiredUserRankToView the minimumRequiredUserRankToView to set
     */
    public void setMinimumRequiredUserRankToView(int minimumRequiredUserRankToView) {
        this.minimumRequiredUserRankToView = minimumRequiredUserRankToView;
    }

    /**
     * @param minimumRequiredUserRankToChoose the minimumRequiredUserRankToChoose to set
     */
    public void setMinimumRequiredUserRankToChoose(int minimumRequiredUserRankToChoose) {
        this.minimumRequiredUserRankToChoose = minimumRequiredUserRankToChoose;
    }

    @Override
    public Icon getIcon() {
        return icon;
    }
    
    

}
