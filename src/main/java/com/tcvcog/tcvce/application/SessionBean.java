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
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Citation;
import com.tcvcog.tcvce.entities.CodeElement;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.CodeElementGuideEntry;
import com.tcvcog.tcvce.entities.CodeSet;
import com.tcvcog.tcvce.entities.CodeSource;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EventCase;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.NoticeOfViolation;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.User;
import java.io.Serializable;
import java.util.ArrayList;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author Eric C. Darsow
 */
@ManagedBean(name="sessionBean")
@SessionScoped
public class SessionBean extends BackingBeanUtils implements Serializable{
    
    private User utilityUserToUpdate;
    private Municipality activeMuni;
    private CodeSource activeCodeSource;
    private Property activeProp;
    private CECase activeCase;
    private EventCase activeEvent;
    private Person activePerson;
    private User activeUser;
    private NoticeOfViolation activeNotice;
    private CEActionRequest actionRequest;
    private CodeSet activeCodeSet;
    private Citation activeCitation;
    private CodeElement activeCodeElement;
    private EnforcableCodeElement selectedEnfCodeElement;
    private CodeViolation activeCodeViolation;
    private ArrayList<CodeViolation> activeViolationList;
    private CodeElementGuideEntry activeCodeElementGuideEntry;
 

    /**
     * Creates a new instance of getSessionBean()
     */
    public SessionBean() {
        System.out.println("SessionBean.SessionBean");
    }

    /**
     * @return the activeProp
     */
    public Property getActiveProp() {
        return activeProp;
    }

    /**
     * @return the activeCase
     */
    public CECase getActiveCase() {
        return activeCase;
    }

    /**
     * @return the activeEvent
     */
    public EventCase getActiveEvent() {
        return activeEvent;
    }

    /**
     * @return the activePerson
     */
    public Person getActivePerson() {
        return activePerson;
    }

    /**
     * @return the activeUser
     */
    public User getActiveUser() {
        return activeUser;
    }

    /**
     * @return the activeNotice
     */
    public NoticeOfViolation getActiveNotice() {
        return activeNotice;
    }

    /**
     * @return the actionRequest
     */
    public CEActionRequest getActionRequest() {
        return actionRequest;
    }

    /**
     * @return the activeCodeSet
     */
    public CodeSet getActiveCodeSet() {
        return activeCodeSet;
    }

    /**
     * @return the activeCitation
     */
    public Citation getActiveCitation() {
        return activeCitation;
    }

    /**
     * @return the selectedEnfCodeElement
     */
    public EnforcableCodeElement getSelectedEnfCodeElement() {
        return selectedEnfCodeElement;
    }

    /**
     * @return the activeCodeViolation
     */
    public CodeViolation getActiveCodeViolation() {
        return activeCodeViolation;
    }

    /**
     * @return the activeViolationList
     */
    public ArrayList<CodeViolation> getActiveViolationList() {
        return activeViolationList;
    }

    /**
     * @return the activeCodeElementGuideEntry
     */
    public CodeElementGuideEntry getActiveCodeElementGuideEntry() {
        return activeCodeElementGuideEntry;
    }

    /**
     * @param activeProp the activeProp to set
     */
    public void setActiveProp(Property activeProp) {
        this.activeProp = activeProp;
    }

    /**
     * @param activeCase the activeCase to set
     */
    public void setActiveCase(CECase activeCase) {
        this.activeCase = activeCase;
    }

    /**
     * @param activeEvent the activeEvent to set
     */
    public void setActiveEvent(EventCase activeEvent) {
        this.activeEvent = activeEvent;
    }

    /**
     * @param activePerson the activePerson to set
     */
    public void setActivePerson(Person activePerson) {
        this.activePerson = activePerson;
    }

    /**
     * @param activeUser the activeUser to set
     */
    public void setActiveUser(User activeUser) {
        this.activeUser = activeUser;
    }

    /**
     * @param activeNotice the activeNotice to set
     */
    public void setActiveNotice(NoticeOfViolation activeNotice) {
        this.activeNotice = activeNotice;
    }

    /**
     * @param actionRequest the actionRequest to set
     */
    public void setActionRequest(CEActionRequest actionRequest) {
        this.actionRequest = actionRequest;
    }

    /**
     * @param activeCodeSet the activeCodeSet to set
     */
    public void setActiveCodeSet(CodeSet activeCodeSet) {
        this.activeCodeSet = activeCodeSet;
    }

    /**
     * @param activeCitation the activeCitation to set
     */
    public void setActiveCitation(Citation activeCitation) {
        this.activeCitation = activeCitation;
    }

    /**
     * @param selectedEnfCodeElement the selectedEnfCodeElement to set
     */
    public void setSelectedEnfCodeElement(EnforcableCodeElement selectedEnfCodeElement) {
        this.selectedEnfCodeElement = selectedEnfCodeElement;
    }

    /**
     * @param activeCodeViolation the activeCodeViolation to set
     */
    public void setActiveCodeViolation(CodeViolation activeCodeViolation) {
        this.activeCodeViolation = activeCodeViolation;
    }

    /**
     * @param activeViolationList the activeViolationList to set
     */
    public void setActiveViolationList(ArrayList<CodeViolation> activeViolationList) {
        this.activeViolationList = activeViolationList;
    }

    /**
     * @param activeCodeElementGuideEntry the activeCodeElementGuideEntry to set
     */
    public void setActiveCodeElementGuideEntry(CodeElementGuideEntry activeCodeElementGuideEntry) {
        this.activeCodeElementGuideEntry = activeCodeElementGuideEntry;
    }

    /**
     * @return the utilityUserToUpdate
     */
    public User getUtilityUserToUpdate() {
        return utilityUserToUpdate;
    }

    /**
     * @return the activeCodeSource
     */
    public CodeSource getActiveCodeSource() {
        return activeCodeSource;
    }

    /**
     * @param utilityUserToUpdate the utilityUserToUpdate to set
     */
    public void setUtilityUserToUpdate(User utilityUserToUpdate) {
        this.utilityUserToUpdate = utilityUserToUpdate;
    }

    /**
     * @param activeCodeSource the activeCodeSource to set
     */
    public void setActiveCodeSource(CodeSource activeCodeSource) {
        this.activeCodeSource = activeCodeSource;
    }

    /**
     * @return the activeMuni
     */
    public Municipality getActiveMuni() {
        return activeMuni;
    }

    /**
     * @param activeMuni the activeMuni to set
     */
    public void setActiveMuni(Municipality activeMuni) {
        System.out.println("MissionControlBB.setActiveMuni | set: " + activeMuni.getMuniName());
        this.activeMuni = activeMuni;
    }

    /**
     * @return the activeCodeElement
     */
    public CodeElement getActiveCodeElement() {
        return activeCodeElement;
    }

    /**
     * @param activeCodeElement the activeCodeElement to set
     */
    public void setActiveCodeElement(CodeElement activeCodeElement) {
        this.activeCodeElement = activeCodeElement;
    }
    
}
