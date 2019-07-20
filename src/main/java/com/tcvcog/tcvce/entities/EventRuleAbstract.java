/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.io.Serializable;

/**
 * @author sylvia
 */
public class EventRuleAbstract extends EntityUtils implements Serializable {
    
    protected int ruleid;
    protected String title;
    protected String description;
    protected EventType requiredeventtype;
    protected EventType forbiddeneventtype;
    protected EventCategory requiredEventCat;
    protected EventCategory forbiddenEventCat;
    
    protected int requiredeventcatthresholdtypeintorder;
    protected boolean requiredeventcatupperboundtypeintorder;
    
    protected int requiredeventcatthresholdglobalorder;
    protected boolean requiredeventcatupperboundglobalorder;
    
    
    protected int forbiddeneventcatthresholdtypeintorder;
    protected boolean forbiddeneventcatupperboundtypeintorder;
    
    protected int forbiddeneventcatthresholdglobalorder;
    protected boolean forbiddeneventcatupperboundglobalorder;
    
    protected boolean mandatorypassreqtocloseentity;
    protected boolean autoremoveonentityclose;
    protected Proposal promptingProposal;
    
    protected EventCategory triggeredeventcatonpass;
    protected EventCategory triggeredeventcatonfail;
    
    protected boolean active;
    protected String notes;

    /**
     * @return the ruleid
     */
    public int getRuleid() {
        return ruleid;
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
     * @return the requiredeventtype
     */
    public EventType getRequiredeventtype() {
        return requiredeventtype;
    }

    /**
     * @return the forbiddeneventtype
     */
    public EventType getForbiddeneventtype() {
        return forbiddeneventtype;
    }

    /**
     * @return the requiredEventCat
     */
    public EventCategory getRequiredEventCat() {
        return requiredEventCat;
    }

    /**
     * @return the requiredeventcatthresholdtypeintorder
     */
    public int isRequiredeventcatthresholdtypeintorder() {
        return requiredeventcatthresholdtypeintorder;
    }

    /**
     * @return the requiredeventcatupperboundtypeintorder
     */
    public boolean isRequiredeventcatupperboundtypeintorder() {
        return requiredeventcatupperboundtypeintorder;
    }

    /**
     * @return the requiredeventcatthresholdglobalorder
     */
    public int isRequiredeventcatthresholdglobalorder() {
        return requiredeventcatthresholdglobalorder;
    }

    /**
     * @return the requiredeventcatupperboundglobalorder
     */
    public boolean isRequiredeventcatupperboundglobalorder() {
        return requiredeventcatupperboundglobalorder;
    }


    /**
     * @return the forbiddeneventcatthresholdtypeintorder
     */
    public int isForbiddeneventcatthresholdtypeintorder() {
        return forbiddeneventcatthresholdtypeintorder;
    }

    /**
     * @return the forbiddeneventcatupperboundtypeintorder
     */
    public boolean isForbiddeneventcatupperboundtypeintorder() {
        return forbiddeneventcatupperboundtypeintorder;
    }

    /**
     * @return the forbiddeneventcatthresholdglobalorder
     */
    public int isForbiddeneventcatthresholdglobalorder() {
        return forbiddeneventcatthresholdglobalorder;
    }

    /**
     * @return the forbiddeneventcatupperboundglobalorder
     */
    public boolean isForbiddeneventcatupperboundglobalorder() {
        return forbiddeneventcatupperboundglobalorder;
    }

    /**
     * @return the mandatorypassreqtocloseentity
     */
    public boolean isMandatorypassreqtocloseentity() {
        return mandatorypassreqtocloseentity;
    }

    /**
     * @return the autoremoveonentityclose
     */
    public boolean isAutoremoveonentityclose() {
        return autoremoveonentityclose;
    }

    /**
     * @return the promptingProposal
     */
    public Proposal getPromptingProposal() {
        return promptingProposal;
    }

    /**
     * @return the triggeredeventcatonpass
     */
    public EventCategory getTriggeredeventcatonpass() {
        return triggeredeventcatonpass;
    }

    /**
     * @return the triggeredeventcatonfail
     */
    public EventCategory getTriggeredeventcatonfail() {
        return triggeredeventcatonfail;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param ruleid the ruleid to set
     */
    public void setRuleid(int ruleid) {
        this.ruleid = ruleid;
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
     * @param requiredeventtype the requiredeventtype to set
     */
    public void setRequiredeventtype(EventType requiredeventtype) {
        this.requiredeventtype = requiredeventtype;
    }

    /**
     * @param forbiddeneventtype the forbiddeneventtype to set
     */
    public void setForbiddeneventtype(EventType forbiddeneventtype) {
        this.forbiddeneventtype = forbiddeneventtype;
    }

    /**
     * @param requiredEventCat the requiredEventCat to set
     */
    public void setRequiredEventCat(EventCategory requiredEventCat) {
        this.requiredEventCat = requiredEventCat;
    }

    /**
     * @param requiredeventcatthresholdtypeintorder the requiredeventcatthresholdtypeintorder to set
     */
    public void setRequiredeventcatthresholdtypeintorder(int requiredeventcatthresholdtypeintorder) {
        this.requiredeventcatthresholdtypeintorder = requiredeventcatthresholdtypeintorder;
    }

    /**
     * @param requiredeventcatupperboundtypeintorder the requiredeventcatupperboundtypeintorder to set
     */
    public void setRequiredeventcatupperboundtypeintorder(boolean requiredeventcatupperboundtypeintorder) {
        this.requiredeventcatupperboundtypeintorder = requiredeventcatupperboundtypeintorder;
    }

    /**
     * @param requiredeventcatthresholdglobalorder the requiredeventcatthresholdglobalorder to set
     */
    public void setRequiredeventcatthresholdglobalorder(int requiredeventcatthresholdglobalorder) {
        this.requiredeventcatthresholdglobalorder = requiredeventcatthresholdglobalorder;
    }

    /**
     * @param requiredeventcatupperboundglobalorder the requiredeventcatupperboundglobalorder to set
     */
    public void setRequiredeventcatupperboundglobalorder(boolean requiredeventcatupperboundglobalorder) {
        this.requiredeventcatupperboundglobalorder = requiredeventcatupperboundglobalorder;
    }


    /**
     * @param forbiddeneventcatthresholdtypeintorder the forbiddeneventcatthresholdtypeintorder to set
     */
    public void setForbiddeneventcatthresholdtypeintorder(int forbiddeneventcatthresholdtypeintorder) {
        this.forbiddeneventcatthresholdtypeintorder = forbiddeneventcatthresholdtypeintorder;
    }

    /**
     * @param forbiddeneventcatupperboundtypeintorder the forbiddeneventcatupperboundtypeintorder to set
     */
    public void setForbiddeneventcatupperboundtypeintorder(boolean forbiddeneventcatupperboundtypeintorder) {
        this.forbiddeneventcatupperboundtypeintorder = forbiddeneventcatupperboundtypeintorder;
    }

    /**
     * @param forbiddeneventcatthresholdglobalorder the forbiddeneventcatthresholdglobalorder to set
     */
    public void setForbiddeneventcatthresholdglobalorder(int forbiddeneventcatthresholdglobalorder) {
        this.forbiddeneventcatthresholdglobalorder = forbiddeneventcatthresholdglobalorder;
    }

    /**
     * @param forbiddeneventcatupperboundglobalorder the forbiddeneventcatupperboundglobalorder to set
     */
    public void setForbiddeneventcatupperboundglobalorder(boolean forbiddeneventcatupperboundglobalorder) {
        this.forbiddeneventcatupperboundglobalorder = forbiddeneventcatupperboundglobalorder;
    }

    /**
     * @param mandatorypassreqtocloseentity the mandatorypassreqtocloseentity to set
     */
    public void setMandatorypassreqtocloseentity(boolean mandatorypassreqtocloseentity) {
        this.mandatorypassreqtocloseentity = mandatorypassreqtocloseentity;
    }

    /**
     * @param autoremoveonentityclose the autoremoveonentityclose to set
     */
    public void setAutoremoveonentityclose(boolean autoremoveonentityclose) {
        this.autoremoveonentityclose = autoremoveonentityclose;
    }

    /**
     * @param promptingProposal the promptingProposal to set
     */
    public void setPromptingProposal(Proposal promptingProposal) {
        this.promptingProposal = promptingProposal;
    }

    /**
     * @param triggeredeventcatonpass the triggeredeventcatonpass to set
     */
    public void setTriggeredeventcatonpass(EventCategory triggeredeventcatonpass) {
        this.triggeredeventcatonpass = triggeredeventcatonpass;
    }

    /**
     * @param triggeredeventcatonfail the triggeredeventcatonfail to set
     */
    public void setTriggeredeventcatonfail(EventCategory triggeredeventcatonfail) {
        this.triggeredeventcatonfail = triggeredeventcatonfail;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @return the forbiddenEventCat
     */
    public EventCategory getForbiddenEventCat() {
        return forbiddenEventCat;
    }

    /**
     * @param forbiddenEventCat the forbiddenEventCat to set
     */
    public void setForbiddenEventCat(EventCategory forbiddenEventCat) {
        this.forbiddenEventCat = forbiddenEventCat;
    }

    
    
}
