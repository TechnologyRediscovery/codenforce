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
    
    private EventCategory choice1EventCat;
    private String choice1Description;
    private EventCategory choice2EventCat;
    private String choice2Description;
    private EventCategory choice3EventCat;
    private String choice3Description;
    private boolean requestActionByDefaultMuniCEO;
    private boolean requestActionByDefaultMuniStaffer;
    
    private EventProposalResponse response;
    
    


    /**
     * @param requestActionByDefaultMuniCEO the requestActionByDefaultMuniCEO to set
     */
    public void setRequestActionByDefaultMuniCEO(boolean requestActionByDefaultMuniCEO) {
        this.requestActionByDefaultMuniCEO = requestActionByDefaultMuniCEO;
    }

    /**
     * @return the requestActionByDefaultMuniCEO
     */
    public boolean isRequestActionByDefaultMuniCEO() {
        return requestActionByDefaultMuniCEO;
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
     * @return the response
     */
    public EventProposalResponse getResponse() {
        return response;
    }

    /**
     * @param response the response to set
     */
    public void setResponse(EventProposalResponse response) {
        this.response = response;
    }

    /**
     * @return the requestActionByDefaultMuniStaffer
     */
    public boolean isRequestActionByDefaultMuniStaffer() {
        return requestActionByDefaultMuniStaffer;
    }

    /**
     * @param requestActionByDefaultMuniStaffer the requestActionByDefaultMuniStaffer to set
     */
    public void setRequestActionByDefaultMuniStaffer(boolean requestActionByDefaultMuniStaffer) {
        this.requestActionByDefaultMuniStaffer = requestActionByDefaultMuniStaffer;
    }
    
    
    
}
