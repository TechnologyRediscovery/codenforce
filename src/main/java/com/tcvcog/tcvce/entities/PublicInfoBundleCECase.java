/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An experimental structure that only exposes setters on the publicly released
 * members on a CEActionRequest object. The actual member variables don't exist:
 * only the getters that access the data in the actionRequest memvar;
 *
 * @author sylvia
 */
public class PublicInfoBundleCECase extends PublicInfoBundle implements Serializable {

    private CECase bundledCase;
    private boolean paccEnabled;

    private List<EventCnF> publicEventList;

    private LocalDateTime mostRecentLoggedEvent;

    private int countNoticeLetters;
    private int countViolations;
    private int countCitations;

    public PublicInfoBundleCECase() {
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<h2>");
        sb.append("Code Enforcement Case");
        sb.append("</h2>");
        sb.append("<p>");

        sb.append("<span class=\"bold\">");
        sb.append("Public access code: ");
        sb.append("</span>");
        sb.append(getPacc());
        sb.append("<br />");

        sb.append("<span class=\"bold\">");
        sb.append("Property Address: ");
        sb.append("</span>");
        sb.append(getPropertyAddress());
        sb.append("<br />");

        sb.append("<span class=\"bold\">");
        sb.append("Case opening date: ");
        sb.append("</span>");
        sb.append(bundledCase.getOriginiationDatePretty());
        sb.append("<br />");

        sb.append("<span class=\"bold\">");
        sb.append("Case closing date: ");
        sb.append("</span>");
        sb.append(bundledCase.getClosingDatePretty());
        sb.append("<br />");

        sb.append("</p>");
        return sb.toString();

    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.paccEnabled ? 1 : 0);
        hash = 89 * hash + Objects.hashCode(this.bundledCase);
        hash = 89 * hash + Objects.hashCode(this.mostRecentLoggedEvent);
        hash = 89 * hash + this.countNoticeLetters;
        hash = 89 * hash + this.countViolations;
        hash = 89 * hash + this.countCitations;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PublicInfoBundleCECase other = (PublicInfoBundleCECase) obj;
        if (this.bundledCase.equals(other.bundledCase)) {
            return false;
        }
        if (this.paccEnabled != other.paccEnabled) {
            return false;
        }
        if (this.countNoticeLetters != other.countNoticeLetters) {
            return false;
        }
        if (this.countViolations != other.countViolations) {
            return false;
        }
        if (this.countCitations != other.countCitations) {
            return false;
        }
        if (!Objects.equals(this.publicEventList, other.publicEventList)) {
            return false;
        }
        if (!Objects.equals(this.mostRecentLoggedEvent, other.mostRecentLoggedEvent)) {
            return false;
        }
        return true;
    }

    public CECase getBundledCase() {
        return bundledCase;
    }

    public void setBundledCase(CECase input) {

        input.setPropertyID(0);
        input.setPropertyUnitID(0);
        input.setNotes("*****");
        input.setSource(new BOBSource());

        setPacc(input.getPublicControlCode());

        if (input.getCaseManager() != null && input.getCaseManager().getPerson() != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(input.getCaseManager().getPerson().getFirstName());
            sb.append(" ");
            sb.append(input.getCaseManager().getPerson().getLastName());
            setCaseManagerName(sb.toString());
            setCaseManagerContact(input.getCaseManager().getPerson().getPhoneWork());
        }
        input.setCaseManager(new User());

        if (input.getViolationList() != null) {
            countViolations = input.getViolationList().size();
        }
        input.setViolationList(new ArrayList<CodeViolation>());
        
        if (input.getNoticeList() != null) {
            countNoticeLetters = input.getNoticeList().size();
        }
        input.setNoticeList(new ArrayList<NoticeOfViolation>());

        if (input.getCitationList() != null){
        countCitations = input.getCitationList().size();
        }
        input.setCitationList(new ArrayList<Citation>());

        bundledCase = input;
    }

    /**
     * @return the mostRecentLoggedEvent
     */
    public LocalDateTime getMostRecentLoggedEvent() {
        return mostRecentLoggedEvent;
    }

    /**
     * @return the countNoticeLetters
     */
    public int getCountNoticeLetters() {
        return countNoticeLetters;
    }

    /**
     * @return the countViolations
     */
    public int getCountViolations() {
        return countViolations;
    }

    /**
     * @return the countCitations
     */
    public int getCountCitations() {
        return countCitations;
    }

    /**
     * @param mostRecentLoggedEvent the mostRecentLoggedEvent to set
     */
    public void setMostRecentLoggedEvent(LocalDateTime mostRecentLoggedEvent) {
        this.mostRecentLoggedEvent = mostRecentLoggedEvent;
    }

    /**
     * @param countNoticeLetters the countNoticeLetters to set
     */
    public void setCountNoticeLetters(int countNoticeLetters) {
        this.countNoticeLetters = countNoticeLetters;
    }

    /**
     * @param countViolations the countViolations to set
     */
    public void setCountViolations(int countViolations) {
        this.countViolations = countViolations;
    }

    /**
     * @param countCitations the countCitations to set
     */
    public void setCountCitations(int countCitations) {
        this.countCitations = countCitations;
    }

    //************************************************
    //*******Code Enforcement case public data********
    //************************************************
    /**
     * @return the paccEnabled
     */
    public boolean isPaccEnabled() {
        return paccEnabled;
    }

    /**
     * @return the publicEventList
     */
    public List<EventCnF> getPublicEventList() {
        return publicEventList;
    }

    /**
     * @param paccEnabled the paccEnabled to set
     */
    public void setPaccEnabled(boolean paccEnabled) {
        this.paccEnabled = paccEnabled;
    }

    /**
     * @param publicEventList the publicEventList to set
     */
    public void setPublicEventList(List<EventCnF> publicEventList) {
        this.publicEventList = publicEventList;
    }

}
