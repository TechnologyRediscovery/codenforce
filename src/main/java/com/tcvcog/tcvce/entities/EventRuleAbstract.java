/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.io.Serializable;

/**
 *  Wraps a heap of Event-related constraints or instructions which involve
 * reviewing an entire list of Events on an EventRuleGoverned object and creating
 * new events in response to the result of a given Rule's evaluation status.
 * 
 * This class's subclass, EventRule
 * 
 * @author ellen bascomb
 */
public class        EventRuleAbstract  
        implements  Serializable {
    
    protected int ruleid;
    protected String title;
    protected String description;
    protected EventType requiredEventType;
    protected EventType forbiddenEventType;
    protected EventCategory requiredEventCategory;
    protected EventCategory forbiddenEventCategory;
    
    protected int requiredECThreshold_typeInternalOrder;
    protected boolean requiredECThreshold_typeInternalOrder_treatAsUpperBound;
    
    protected int requiredECThreshold_globalOrder;
    protected boolean requiredECThreshold_globalOrder_treatAsUpperBound;
    
    protected int forbiddenECThreshold_typeInternalOrder;
    protected boolean forbiddenECThreshold_typeInternalOrder_treatAsUpperBound;
    
    protected int forbiddenECThreshold_globalOrder;
    protected boolean forbiddenECThreshold_globalOrder_treatAsUpperBound;
    
    protected boolean mandatoryRulePassRequiredToCloseEntity;
    protected boolean inactivateRuleOnEntityClose;
    
    protected Directive promptingDirective;
    protected int formPromptingDirectiveID;
   
    protected EventCategory triggeredECOnRulePass;
    protected EventCategory triggeredECOnRuleFail;
    
    protected boolean activeRuleAbstract;
    protected String notes;
    
    protected int userRankMinToConfigure;
    protected int userRankMinToImplement;
    protected int userRankMinToWaive;
    protected int userRankMinToOverride;
    protected int userRankMinToDeactivate;

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
     * @return the requiredEventType
     */
    public EventType getRequiredEventType() {
        return requiredEventType;
    }

    /**
     * @return the forbiddenEventType
     */
    public EventType getForbiddenEventType() {
        return forbiddenEventType;
    }

    /**
     * @return the requiredEventCategory
     */
    public EventCategory getRequiredEventCategory() {
        return requiredEventCategory;
    }

    /**
     * @return the requiredECThreshold_typeInternalOrder
     */
    public int isRequiredeventcatthresholdtypeintorder() {
        return getRequiredECThreshold_typeInternalOrder();
    }

    /**
     * @return the requiredECThreshold_typeInternalOrder_treatAsUpperBound
     */
    public boolean isRequiredECThreshold_typeInternalOrder_treatAsUpperBound() {
        return requiredECThreshold_typeInternalOrder_treatAsUpperBound;
    }

    /**
     * @return the requiredECThreshold_globalOrder
     */
    public int isRequiredeventcatthresholdglobalorder() {
        return getRequiredECThreshold_globalOrder();
    }

    /**
     * @return the requiredECThreshold_globalOrder_treatAsUpperBound
     */
    public boolean isRequiredECThreshold_globalOrder_treatAsUpperBound() {
        return requiredECThreshold_globalOrder_treatAsUpperBound;
    }


    /**
     * @return the forbiddenECThreshold_typeInternalOrder
     */
    public int isForbiddeneventcatthresholdtypeintorder() {
        return getForbiddenECThreshold_typeInternalOrder();
    }

    /**
     * @return the forbiddenECThreshold_typeInternalOrder_treatAsUpperBound
     */
    public boolean isForbiddenECThreshold_typeInternalOrder_treatAsUpperBound() {
        return forbiddenECThreshold_typeInternalOrder_treatAsUpperBound;
    }

    /**
     * @return the forbiddenECThreshold_globalOrder
     */
    public int isForbiddeneventcatthresholdglobalorder() {
        return getForbiddenECThreshold_globalOrder();
    }

    /**
     * @return the forbiddenECThreshold_globalOrder_treatAsUpperBound
     */
    public boolean isForbiddenECThreshold_globalOrder_treatAsUpperBound() {
        return forbiddenECThreshold_globalOrder_treatAsUpperBound;
    }

    /**
     * @return the mandatoryRulePassRequiredToCloseEntity
     */
    public boolean isMandatoryRulePassRequiredToCloseEntity() {
        return mandatoryRulePassRequiredToCloseEntity;
    }

    /**
     * @return the inactivateRuleOnEntityClose
     */
    public boolean isInactivateRuleOnEntityClose() {
        return inactivateRuleOnEntityClose;
    }


    /**
     * @return the triggeredECOnRulePass
     */
    public EventCategory getTriggeredECOnRulePass() {
        return triggeredECOnRulePass;
    }

    /**
     * @return the triggeredECOnRuleFail
     */
    public EventCategory getTriggeredECOnRuleFail() {
        return triggeredECOnRuleFail;
    }

    /**
     * @return the activeRuleAbstract
     */
    public boolean isActiveRuleAbstract() {
        return activeRuleAbstract;
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
     * @param requiredEventType the requiredEventType to set
     */
    public void setRequiredEventType(EventType requiredEventType) {
        this.requiredEventType = requiredEventType;
    }

    /**
     * @param forbiddenEventType the forbiddenEventType to set
     */
    public void setForbiddenEventType(EventType forbiddenEventType) {
        this.forbiddenEventType = forbiddenEventType;
    }

    /**
     * @param requiredEventCategory the requiredEventCategory to set
     */
    public void setRequiredEventCategory(EventCategory requiredEventCategory) {
        this.requiredEventCategory = requiredEventCategory;
    }

    /**
     * @param requiredECThreshold_typeInternalOrder the requiredECThreshold_typeInternalOrder to set
     */
    public void setRequiredECThreshold_typeInternalOrder(int requiredECThreshold_typeInternalOrder) {
        this.requiredECThreshold_typeInternalOrder = requiredECThreshold_typeInternalOrder;
    }

    /**
     * @param requiredECThreshold_typeInternalOrder_treatAsUpperBound the requiredECThreshold_typeInternalOrder_treatAsUpperBound to set
     */
    public void setRequiredECThreshold_typeInternalOrder_treatAsUpperBound(boolean requiredECThreshold_typeInternalOrder_treatAsUpperBound) {
        this.requiredECThreshold_typeInternalOrder_treatAsUpperBound = requiredECThreshold_typeInternalOrder_treatAsUpperBound;
    }

    /**
     * @param requiredECThreshold_globalOrder the requiredECThreshold_globalOrder to set
     */
    public void setRequiredECThreshold_globalOrder(int requiredECThreshold_globalOrder) {
        this.requiredECThreshold_globalOrder = requiredECThreshold_globalOrder;
    }

    /**
     * @param requiredECThreshold_globalOrder_treatAsUpperBound the requiredECThreshold_globalOrder_treatAsUpperBound to set
     */
    public void setRequiredECThreshold_globalOrder_treatAsUpperBound(boolean requiredECThreshold_globalOrder_treatAsUpperBound) {
        this.requiredECThreshold_globalOrder_treatAsUpperBound = requiredECThreshold_globalOrder_treatAsUpperBound;
    }


    /**
     * @param forbiddenECThreshold_typeInternalOrder the forbiddenECThreshold_typeInternalOrder to set
     */
    public void setForbiddenECThreshold_typeInternalOrder(int forbiddenECThreshold_typeInternalOrder) {
        this.forbiddenECThreshold_typeInternalOrder = forbiddenECThreshold_typeInternalOrder;
    }

    /**
     * @param forbiddenECThreshold_typeInternalOrder_treatAsUpperBound the forbiddenECThreshold_typeInternalOrder_treatAsUpperBound to set
     */
    public void setForbiddenECThreshold_typeInternalOrder_treatAsUpperBound(boolean forbiddenECThreshold_typeInternalOrder_treatAsUpperBound) {
        this.forbiddenECThreshold_typeInternalOrder_treatAsUpperBound = forbiddenECThreshold_typeInternalOrder_treatAsUpperBound;
    }

    /**
     * @param forbiddenECThreshold_globalOrder the forbiddenECThreshold_globalOrder to set
     */
    public void setForbiddenECThreshold_globalOrder(int forbiddenECThreshold_globalOrder) {
        this.forbiddenECThreshold_globalOrder = forbiddenECThreshold_globalOrder;
    }

    /**
     * @param forbiddenECThreshold_globalOrder_treatAsUpperBound the forbiddenECThreshold_globalOrder_treatAsUpperBound to set
     */
    public void setForbiddenECThreshold_globalOrder_treatAsUpperBound(boolean forbiddenECThreshold_globalOrder_treatAsUpperBound) {
        this.forbiddenECThreshold_globalOrder_treatAsUpperBound = forbiddenECThreshold_globalOrder_treatAsUpperBound;
    }

    /**
     * @param mandatoryRulePassRequiredToCloseEntity the mandatoryRulePassRequiredToCloseEntity to set
     */
    public void setMandatoryRulePassRequiredToCloseEntity(boolean mandatoryRulePassRequiredToCloseEntity) {
        this.mandatoryRulePassRequiredToCloseEntity = mandatoryRulePassRequiredToCloseEntity;
    }

    /**
     * @param inactivateRuleOnEntityClose the inactivateRuleOnEntityClose to set
     */
    public void setInactivateRuleOnEntityClose(boolean inactivateRuleOnEntityClose) {
        this.inactivateRuleOnEntityClose = inactivateRuleOnEntityClose;
    }

    /**
     * @param triggeredECOnRulePass the triggeredECOnRulePass to set
     */
    public void setTriggeredECOnRulePass(EventCategory triggeredECOnRulePass) {
        this.triggeredECOnRulePass = triggeredECOnRulePass;
    }

    /**
     * @param triggeredECOnRuleFail the triggeredECOnRuleFail to set
     */
    public void setTriggeredECOnRuleFail(EventCategory triggeredECOnRuleFail) {
        this.triggeredECOnRuleFail = triggeredECOnRuleFail;
    }

    /**
     * @param activeRuleAbstract the activeRuleAbstract to set
     */
    public void setActiveRuleAbstract(boolean activeRuleAbstract) {
        this.activeRuleAbstract = activeRuleAbstract;
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
     * @return the forbiddenEventCategory
     */
    public EventCategory getForbiddenEventCategory() {
        return forbiddenEventCategory;
    }

    /**
     * @param forbiddenEventCategory the forbiddenEventCategory to set
     */
    public void setForbiddenEventCategory(EventCategory forbiddenEventCategory) {
        this.forbiddenEventCategory = forbiddenEventCategory;
    }

    /**
     * @return the requiredECThreshold_typeInternalOrder
     */
    public int getRequiredECThreshold_typeInternalOrder() {
        return requiredECThreshold_typeInternalOrder;
    }

    /**
     * @return the requiredECThreshold_globalOrder
     */
    public int getRequiredECThreshold_globalOrder() {
        return requiredECThreshold_globalOrder;
    }

    /**
     * @return the forbiddenECThreshold_typeInternalOrder
     */
    public int getForbiddenECThreshold_typeInternalOrder() {
        return forbiddenECThreshold_typeInternalOrder;
    }

    /**
     * @return the forbiddenECThreshold_globalOrder
     */
    public int getForbiddenECThreshold_globalOrder() {
        return forbiddenECThreshold_globalOrder;
    }


    /**
     * @return the promptingDirective
     */
    public Directive getPromptingDirective() {
        return promptingDirective;
    }

    /**
     * @return the formPromptingDirectiveID
     */
    public int getFormPromptingDirectiveID() {
        return formPromptingDirectiveID;
    }

    /**
     * @param promptingDirective the promptingDirective to set
     */
    public void setPromptingDirective(Directive promptingDirective) {
        this.promptingDirective = promptingDirective;
    }

    /**
     * @param formPromptingDirectiveID the formPromptingDirectiveID to set
     */
    public void setFormPromptingDirectiveID(int formPromptingDirectiveID) {
        this.formPromptingDirectiveID = formPromptingDirectiveID;
    }

    /**
     * @return the userRankMinToConfigure
     */
    public int getUserRankMinToConfigure() {
        return userRankMinToConfigure;
    }

    /**
     * @return the userRankMinToImplement
     */
    public int getUserRankMinToImplement() {
        return userRankMinToImplement;
    }

    /**
     * @return the userRankMinToWaive
     */
    public int getUserRankMinToWaive() {
        return userRankMinToWaive;
    }

    /**
     * @return the userRankMinToOverride
     */
    public int getUserRankMinToOverride() {
        return userRankMinToOverride;
    }

    /**
     * @return the userRankMinToDeactivate
     */
    public int getUserRankMinToDeactivate() {
        return userRankMinToDeactivate;
    }

    /**
     * @param userRankMinToConfigure the userRankMinToConfigure to set
     */
    public void setUserRankMinToConfigure(int userRankMinToConfigure) {
        this.userRankMinToConfigure = userRankMinToConfigure;
    }

    /**
     * @param userRankMinToImplement the userRankMinToImplement to set
     */
    public void setUserRankMinToImplement(int userRankMinToImplement) {
        this.userRankMinToImplement = userRankMinToImplement;
    }

    /**
     * @param userRankMinToWaive the userRankMinToWaive to set
     */
    public void setUserRankMinToWaive(int userRankMinToWaive) {
        this.userRankMinToWaive = userRankMinToWaive;
    }

    /**
     * @param userRankMinToOverride the userRankMinToOverride to set
     */
    public void setUserRankMinToOverride(int userRankMinToOverride) {
        this.userRankMinToOverride = userRankMinToOverride;
    }

    /**
     * @param userRankMinToDeactivate the userRankMinToDeactivate to set
     */
    public void setUserRankMinToDeactivate(int userRankMinToDeactivate) {
        this.userRankMinToDeactivate = userRankMinToDeactivate;
    }

    
    
}
