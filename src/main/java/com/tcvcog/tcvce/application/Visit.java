/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.EventCase;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.Citation;
import com.tcvcog.tcvce.entities.CodeSet;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.CodeElementGuideEntry;
import com.tcvcog.tcvce.entities.NoticeOfViolation;
import com.tcvcog.tcvce.entities.Person;
import java.util.ArrayList;
import java.util.ArrayList;
import javax.annotation.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author cedba
 */

@SessionScoped
public class Visit {

    private Property activeProp;
    private CECase activeCase;
    private EventCase activeEvent;
    private Person activePerson;
    private User activeUser;
    private NoticeOfViolation activeNotice;
    private CEActionRequest actionRequest;
    private CodeSet activeCodeSet;
    private Citation activeCitation;
    private EnforcableCodeElement selectedEnfCodeElement;
    private CodeViolation activeCodeViolation;
    private ArrayList<CodeViolation> activeViolationList;
    private CodeElementGuideEntry currentCodeElementGuide;
    //private ArrayList<EnforcableCodeElement> eceList;

    /**
     * Creates a new instance of Visit
     */
    public Visit() {
    }

    /**
     * @return the activeProp
     */
    public Property getActiveProp() {
        return activeProp;
    }

    /**
     * @param activeProp the activeProp to set
     */
    public void setActiveProp(Property activeProp) {
        this.activeProp = activeProp;
    }

    /**
     * @return the activeCase
     */
    public CECase getActiveCase() {
        return activeCase;
    }

    /**
     * @param activeCase the activeCase to set
     */
    public void setActiveCase(CECase activeCase) {
        this.activeCase = activeCase;
    }

    /**
     * @return the activeEvent
     */
    public EventCase getActiveEvent() {
        return activeEvent;
    }

    /**
     * @param activeEvent the activeEvent to set
     */
    public void setActiveEvent(EventCase activeEvent) {
        this.activeEvent = activeEvent;
    }

    /**
     * @return the actionRequest
     */
    public CEActionRequest getActionRequest() {
        return actionRequest;
    }

    /**
     * @param actionRequest the actionRequest to set
     */
    public void setActionRequest(CEActionRequest actionRequest) {
        this.actionRequest = actionRequest;
    }

    /**
     * @return the activeUser
     */
    public User getActiveUser() {
        return activeUser;
    }

    /**
     * @param activeUser the activeUser to set
     */
    public void setActiveUser(User activeUser) {
        this.activeUser = activeUser;
    }

    /**
     * @return the activeCodeSet
     */
    public CodeSet getActiveCodeSet() {
        return activeCodeSet;
    }

    /**
     * @param activeCodeSet the activeCodeSet to set
     */
    public void setActiveCodeSet(CodeSet activeCodeSet) {
        this.activeCodeSet = activeCodeSet;
    }
//
//    /**
//     * @return the eceList
//     */
//    public ArrayList<EnforcableCodeElement> getEceList() {
//        return eceList;
//    }
//
//    /**
//     * @param eceList the eceList to set
//     */
//    public void setEceList(ArrayList<EnforcableCodeElement> eceList) {
//        this.eceList = eceList;
//    }

    /**
     * @return the activePerson
     */
    public Person getActivePerson() {
        return activePerson;
    }

    /**
     * @param activePerson the activePerson to set
     */
    public void setActivePerson(Person activePerson) {
        this.activePerson = activePerson;
    }

    /**
     * @return the selectedEnfCodeElement
     */
    public EnforcableCodeElement getSelectedEnfCodeElement() {
        return selectedEnfCodeElement;
    }

    /**
     * @param selectedEnfCodeElement the selectedEnfCodeElement to set
     */
    public void setSelectedEnfCodeElement(EnforcableCodeElement selectedEnfCodeElement) {
        this.selectedEnfCodeElement = selectedEnfCodeElement;
    }

    /**
     * @return the activeCodeViolation
     */
    public CodeViolation getActiveCodeViolation() {
        return activeCodeViolation;
    }

    /**
     * @param activeCodeViolation the activeCodeViolation to set
     */
    public void setActiveCodeViolation(CodeViolation activeCodeViolation) {
        this.activeCodeViolation = activeCodeViolation;
    }

    /**
     * @return the activeNotice
     */
    public NoticeOfViolation getActiveNotice() {
        return activeNotice;
    }

    /**
     * @param activeNotice the activeNotice to set
     */
    public void setActiveNotice(NoticeOfViolation activeNotice) {
        this.activeNotice = activeNotice;
    }

    /**
     * @return the activeCitation
     */
    public Citation getActiveCitation() {
        return activeCitation;
    }

    /**
     * @param activeCitation the activeCitation to set
     */
    public void setActiveCitation(Citation activeCitation) {
        this.activeCitation = activeCitation;
    }

    /**
     * @return the activeViolationList
     */
    public ArrayList<CodeViolation> getActiveViolationList() {
        return activeViolationList;
    }

    /**
     * @param activeViolationList the activeViolationList to set
     */
    public void setActiveViolationList(ArrayList<CodeViolation> activeViolationList) {
        this.activeViolationList = activeViolationList;
    }

    /**
     * @return the currentCodeElementGuide
     */
    public CodeElementGuideEntry getCurrentCodeElementGuide() {
        return currentCodeElementGuide;
    }

    /**
     * @param currentCodeElementGuide the currentCodeElementGuide to set
     */
    public void setCurrentCodeElementGuide(CodeElementGuideEntry currentCodeElementGuide) {
        this.currentCodeElementGuide = currentCodeElementGuide;
    }

}
