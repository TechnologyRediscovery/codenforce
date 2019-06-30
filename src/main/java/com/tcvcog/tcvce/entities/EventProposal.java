/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author sylvia
 */
public class EventProposal extends EntityUtils {

    private int proposalID;
    private String title;
    private String description;
    
    private List<Proposable> choiceList;
    
    private User creator;
    
    private boolean directPropToDefaultMuniCEO;
    private boolean directPropToDefaultMuniStaffer;
    private boolean directPropToDeveloper;
    
    private boolean executeChoiceIfLoneWolf;
    private boolean inactivateGeneratingEventOnEvaluation;
    private boolean maintainRelativeDateWindow;
    private boolean instantiateMultipleOnBOB;
    private boolean applyToClosedBOBs;
    private boolean autoInactiveOnBOBClose;
    private boolean autoInactiveOnGenEventInactivation;
    
    private int minimumRequiredUserRankToView;
    private int minimumRequiredUserRankToEvaluate;
    
    private boolean active;
    
    private Icon icon;

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
     * @return the directPropToDeveloper
     */
    public boolean isDirectPropToDeveloper() {
        return directPropToDeveloper;
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
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
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

    /**
     * @return the choiceList
     */
    public List<Proposable> getChoiceList() {
        return choiceList;
    }

    /**
     * @param choiceList the choiceList to set
     */
    public void setChoiceList(List<Proposable> choiceList) {
        this.choiceList = choiceList;
    }

    /**
     * @return the executeChoiceIfLoneWolf
     */
    public boolean isExecuteChoiceIfLoneWolf() {
        return executeChoiceIfLoneWolf;
    }

    /**
     * @return the inactivateGeneratingEventOnEvaluation
     */
    public boolean isInactivateGeneratingEventOnEvaluation() {
        return inactivateGeneratingEventOnEvaluation;
    }

    /**
     * @return the maintainRelativeDateWindow
     */
    public boolean isMaintainRelativeDateWindow() {
        return maintainRelativeDateWindow;
    }

    /**
     * @return the instantiateMultipleOnBOB
     */
    public boolean isInstantiateMultipleOnBOB() {
        return instantiateMultipleOnBOB;
    }

    /**
     * @return the applyToClosedBOBs
     */
    public boolean isApplyToClosedBOBs() {
        return applyToClosedBOBs;
    }

    /**
     * @return the autoInactiveOnBOBClose
     */
    public boolean isAutoInactiveOnBOBClose() {
        return autoInactiveOnBOBClose;
    }

    /**
     * @return the autoInactiveOnGenEventInactivation
     */
    public boolean isAutoInactiveOnGenEventInactivation() {
        return autoInactiveOnGenEventInactivation;
    }

    /**
     * @return the minimumRequiredUserRankToView
     */
    public int getMinimumRequiredUserRankToView() {
        return minimumRequiredUserRankToView;
    }

    /**
     * @return the minimumRequiredUserRankToEvaluate
     */
    public int getMinimumRequiredUserRankToEvaluate() {
        return minimumRequiredUserRankToEvaluate;
    }

    /**
     * @param executeChoiceIfLoneWolf the executeChoiceIfLoneWolf to set
     */
    public void setExecuteChoiceIfLoneWolf(boolean executeChoiceIfLoneWolf) {
        this.executeChoiceIfLoneWolf = executeChoiceIfLoneWolf;
    }

    /**
     * @param inactivateGeneratingEventOnEvaluation the inactivateGeneratingEventOnEvaluation to set
     */
    public void setInactivateGeneratingEventOnEvaluation(boolean inactivateGeneratingEventOnEvaluation) {
        this.inactivateGeneratingEventOnEvaluation = inactivateGeneratingEventOnEvaluation;
    }

    /**
     * @param maintainRelativeDateWindow the maintainRelativeDateWindow to set
     */
    public void setMaintainRelativeDateWindow(boolean maintainRelativeDateWindow) {
        this.maintainRelativeDateWindow = maintainRelativeDateWindow;
    }

    /**
     * @param instantiateMultipleOnBOB the instantiateMultipleOnBOB to set
     */
    public void setInstantiateMultipleOnBOB(boolean instantiateMultipleOnBOB) {
        this.instantiateMultipleOnBOB = instantiateMultipleOnBOB;
    }

    /**
     * @param applyToClosedBOBs the applyToClosedBOBs to set
     */
    public void setApplyToClosedBOBs(boolean applyToClosedBOBs) {
        this.applyToClosedBOBs = applyToClosedBOBs;
    }

    /**
     * @param autoInactiveOnBOBClose the autoInactiveOnBOBClose to set
     */
    public void setAutoInactiveOnBOBClose(boolean autoInactiveOnBOBClose) {
        this.autoInactiveOnBOBClose = autoInactiveOnBOBClose;
    }

    /**
     * @param autoInactiveOnGenEventInactivation the autoInactiveOnGenEventInactivation to set
     */
    public void setAutoInactiveOnGenEventInactivation(boolean autoInactiveOnGenEventInactivation) {
        this.autoInactiveOnGenEventInactivation = autoInactiveOnGenEventInactivation;
    }

    /**
     * @param minimumRequiredUserRankToView the minimumRequiredUserRankToView to set
     */
    public void setMinimumRequiredUserRankToView(int minimumRequiredUserRankToView) {
        this.minimumRequiredUserRankToView = minimumRequiredUserRankToView;
    }

    /**
     * @param minimumRequiredUserRankToEvaluate the minimumRequiredUserRankToEvaluate to set
     */
    public void setMinimumRequiredUserRankToEvaluate(int minimumRequiredUserRankToEvaluate) {
        this.minimumRequiredUserRankToEvaluate = minimumRequiredUserRankToEvaluate;
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
    
    
    
}
