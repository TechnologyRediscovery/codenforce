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
public class EventRule extends EntityUtils implements Serializable {
    
    private int ruleid;
    private String title;
    private String description;
    private EventType requiredeventtype;
    private EventType forbiddeneventtype;
    private EventCategory requiredeventcat_catid;
    
    private boolean requiredeventcatthresholdtypeintorder;
    private boolean requiredeventcatupperboundtypeintorder;
    
    private boolean requiredeventcatthresholdglobalorder;
    private boolean requiredeventcatupperboundglobalorder;
    
    private EventCategory forbiddeneventcat_catid;
    
    private boolean forbiddeneventcatthresholdtypeintorder;
    private boolean forbiddeneventcatupperboundtypeintorder;
    
    private boolean forbiddeneventcatthresholdglobalorder;
    private boolean forbiddeneventcatupperboundglobalorder;
    
    private boolean mandatorypassreqtocloseentity;
    private boolean autoremoveonentityclose;
    private int promptingproposal_proposalid;
    
    private EventCategory triggeredeventcatonpass;
    private EventCategory triggeredeventcatonfail;
    
    private boolean active;

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
     * @return the requiredeventcat_catid
     */
    public EventCategory getRequiredeventcat_catid() {
        return requiredeventcat_catid;
    }

    /**
     * @return the requiredeventcatthresholdtypeintorder
     */
    public boolean isRequiredeventcatthresholdtypeintorder() {
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
    public boolean isRequiredeventcatthresholdglobalorder() {
        return requiredeventcatthresholdglobalorder;
    }

    /**
     * @return the requiredeventcatupperboundglobalorder
     */
    public boolean isRequiredeventcatupperboundglobalorder() {
        return requiredeventcatupperboundglobalorder;
    }

    /**
     * @return the forbiddeneventcat_catid
     */
    public EventCategory getForbiddeneventcat_catid() {
        return forbiddeneventcat_catid;
    }

    /**
     * @return the forbiddeneventcatthresholdtypeintorder
     */
    public boolean isForbiddeneventcatthresholdtypeintorder() {
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
    public boolean isForbiddeneventcatthresholdglobalorder() {
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
     * @return the promptingproposal_proposalid
     */
    public int getPromptingproposal_proposalid() {
        return promptingproposal_proposalid;
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
     * @param requiredeventcat_catid the requiredeventcat_catid to set
     */
    public void setRequiredeventcat_catid(EventCategory requiredeventcat_catid) {
        this.requiredeventcat_catid = requiredeventcat_catid;
    }

    /**
     * @param requiredeventcatthresholdtypeintorder the requiredeventcatthresholdtypeintorder to set
     */
    public void setRequiredeventcatthresholdtypeintorder(boolean requiredeventcatthresholdtypeintorder) {
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
    public void setRequiredeventcatthresholdglobalorder(boolean requiredeventcatthresholdglobalorder) {
        this.requiredeventcatthresholdglobalorder = requiredeventcatthresholdglobalorder;
    }

    /**
     * @param requiredeventcatupperboundglobalorder the requiredeventcatupperboundglobalorder to set
     */
    public void setRequiredeventcatupperboundglobalorder(boolean requiredeventcatupperboundglobalorder) {
        this.requiredeventcatupperboundglobalorder = requiredeventcatupperboundglobalorder;
    }

    /**
     * @param forbiddeneventcat_catid the forbiddeneventcat_catid to set
     */
    public void setForbiddeneventcat_catid(EventCategory forbiddeneventcat_catid) {
        this.forbiddeneventcat_catid = forbiddeneventcat_catid;
    }

    /**
     * @param forbiddeneventcatthresholdtypeintorder the forbiddeneventcatthresholdtypeintorder to set
     */
    public void setForbiddeneventcatthresholdtypeintorder(boolean forbiddeneventcatthresholdtypeintorder) {
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
    public void setForbiddeneventcatthresholdglobalorder(boolean forbiddeneventcatthresholdglobalorder) {
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
     * @param promptingproposal_proposalid the promptingproposal_proposalid to set
     */
    public void setPromptingproposal_proposalid(int promptingproposal_proposalid) {
        this.promptingproposal_proposalid = promptingproposal_proposalid;
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

    
    
}
