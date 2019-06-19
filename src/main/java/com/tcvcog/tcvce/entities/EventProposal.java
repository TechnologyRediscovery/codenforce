/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.time.LocalDateTime;

/**
 *
 * @author sylvia
 */
public class EventProposal extends EntityUtils {

    private int proposalID;
    private String title;
    private String description;
    
    private User creator;
    
    boolean currentUserCanRespondToProposal;
    
    private EventCategory choice1EventCat;
    private String choice1Description;
    private EventCategory choice2EventCat;
    private String choice2Description;
    private EventCategory choice3EventCat;
    private String choice3Description;
    
    private boolean directPropToDefaultMuniCEO;
    private boolean directPropToDefaultMuniStaffer;
    private boolean directPropToDeveloper;
    
    private int activatesXDaysFromGeneratingEvent;
    private int expiresXDaysFromGeneratingEvent;
    private EventCategory expiryTrigger;
    
    private boolean active;
    
    private EventProposalImplementation implementation;
    

    /**
     * @param directPropToDefaultMuniCEO the directPropToDefaultMuniCEO to set
     */
    public void setDirectPropToDefaultMuniCEO(boolean directPropToDefaultMuniCEO) {
        this.directPropToDefaultMuniCEO = directPropToDefaultMuniCEO;
    }

    /**
     * @return the directPropToDefaultMuniCEO
     */
    public boolean isDirectPropToDefaultMuniCEO() {
        return directPropToDefaultMuniCEO;
    }



    /**
     * @return the proposalID
     */
    public int getProposalID() {
        return proposalID;
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
     * @return the choice1EventCat
     */
    public EventCategory getChoice1EventCat() {
        return choice1EventCat;
    }

    /**
     * @return the choice1Description
     */
    public String getChoice1Description() {
        return choice1Description;
    }

    /**
     * @return the choice2EventCat
     */
    public EventCategory getChoice2EventCat() {
        return choice2EventCat;
    }

    /**
     * @return the choice2Description
     */
    public String getChoice2Description() {
        return choice2Description;
    }

    /**
     * @return the choice3EventCat
     */
    public EventCategory getChoice3EventCat() {
        return choice3EventCat;
    }

    /**
     * @return the choice3Description
     */
    public String getChoice3Description() {
        return choice3Description;
    }

    /**
     * @param proposalID the proposalID to set
     */
    public void setProposalID(int proposalID) {
        this.proposalID = proposalID;
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
     * @param choice1EventCat the choice1EventCat to set
     */
    public void setChoice1EventCat(EventCategory choice1EventCat) {
        this.choice1EventCat = choice1EventCat;
    }

    /**
     * @param choice1Description the choice1Description to set
     */
    public void setChoice1Description(String choice1Description) {
        this.choice1Description = choice1Description;
    }

    /**
     * @param choice2EventCat the choice2EventCat to set
     */
    public void setChoice2EventCat(EventCategory choice2EventCat) {
        this.choice2EventCat = choice2EventCat;
    }

    /**
     * @param choice2Description the choice2Description to set
     */
    public void setChoice2Description(String choice2Description) {
        this.choice2Description = choice2Description;
    }

    /**
     * @param choice3EventCat the choice3EventCat to set
     */
    public void setChoice3EventCat(EventCategory choice3EventCat) {
        this.choice3EventCat = choice3EventCat;
    }

    /**
     * @param choice3Description the choice3Description to set
     */
    public void setChoice3Description(String choice3Description) {
        this.choice3Description = choice3Description;
    }

    /**
     * @return the implementation
     */
    public EventProposalImplementation getImplementation() {
        return implementation;
    }

    /**
     * @param implementation the implementation to set
     */
    public void setImplementation(EventProposalImplementation implementation) {
        this.implementation = implementation;
    }

    /**
     * @return the directPropToDefaultMuniStaffer
     */
    public boolean isDirectPropToDefaultMuniStaffer() {
        return directPropToDefaultMuniStaffer;
    }

    /**
     * @param directPropToDefaultMuniStaffer the directPropToDefaultMuniStaffer to set
     */
    public void setDirectPropToDefaultMuniStaffer(boolean directPropToDefaultMuniStaffer) {
        this.directPropToDefaultMuniStaffer = directPropToDefaultMuniStaffer;
    }

    /**
     * @param currentUserCanRespondToProposal the currentUserCanRespondToProposal to set
     */
    public void setCurrentUserCanRespondToProposal(boolean currentUserCanRespondToProposal) {
        this.currentUserCanRespondToProposal = currentUserCanRespondToProposal;
    }

    /**
     * @return the currentUserCanRespondToProposal
     */
    public boolean isCurrentUserCanRespondToProposal() {
        return currentUserCanRespondToProposal;
    }

    /**
     * @return the directPropToDeveloper
     */
    public boolean isDirectPropToDeveloper() {
        return directPropToDeveloper;
    }

    /**
     * @return the activatesXDaysFromGeneratingEvent
     */
    public int getActivatesXDaysFromGeneratingEvent() {
        return activatesXDaysFromGeneratingEvent;
    }

    /**
     * @return the expiresXDaysFromGeneratingEvent
     */
    public int getExpiresXDaysFromGeneratingEvent() {
        return expiresXDaysFromGeneratingEvent;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param directPropToDeveloper the directPropToDeveloper to set
     */
    public void setDirectPropToDeveloper(boolean directPropToDeveloper) {
        this.directPropToDeveloper = directPropToDeveloper;
    }

    /**
     * @param activatesXDaysFromGeneratingEvent the activatesXDaysFromGeneratingEvent to set
     */
    public void setActivatesXDaysFromGeneratingEvent(int activatesXDaysFromGeneratingEvent) {
        this.activatesXDaysFromGeneratingEvent = activatesXDaysFromGeneratingEvent;
    }

    /**
     * @param expiresXDaysFromGeneratingEvent the expiresXDaysFromGeneratingEvent to set
     */
    public void setExpiresXDaysFromGeneratingEvent(int expiresXDaysFromGeneratingEvent) {
        this.expiresXDaysFromGeneratingEvent = expiresXDaysFromGeneratingEvent;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return the expiryTrigger
     */
    public EventCategory getExpiryTrigger() {
        return expiryTrigger;
    }

    /**
     * @param expiryTrigger the expiryTrigger to set
     */
    public void setExpiryTrigger(EventCategory expiryTrigger) {
        this.expiryTrigger = expiryTrigger;
    }

    /**
     * @return the creator
     */
    public User getCreator() {
        return creator;
    }

    /**
     * @param creator the creator to set
     */
    public void setCreator(User creator) {
        this.creator = creator;
    }
    
    
    
}
