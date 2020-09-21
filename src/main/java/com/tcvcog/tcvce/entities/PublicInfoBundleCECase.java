/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

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
public class PublicInfoBundleCECase extends PublicInfoBundle{

    private CECase bundledCase;
    private boolean paccEnabled;

    private List<PublicInfoBundleEventCnF> publicEventList;
    private List<PublicInfoBundleCodeViolation> violationList;

    private LocalDateTime mostRecentLoggedEvent;

    private int countNoticeLetters;
    private int countViolations;
    private int countCitations;

    public PublicInfoBundleCECase() {
    }

    @Override
    public String toString() {
        return this.getClass().getName() + bundledCase.getCaseID();
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

        if (input.getViolationList() != null) {
            countViolations = input.getViolationList().size();
        }
        input.setViolationList(new ArrayList<CodeViolation>());

        if (input.getNoticeList() != null) {
            countNoticeLetters = input.getNoticeList().size();
        }
        input.setNoticeList(new ArrayList<NoticeOfViolation>());

        if (input.getCitationList() != null) {
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
    @Override
    public boolean isPaccEnabled() {
        return paccEnabled;
    }

    /**
     * @param paccEnabled the paccEnabled to set
     */
    @Override
    public void setPaccEnabled(boolean paccEnabled) {
        this.paccEnabled = paccEnabled;
    }

    /**
     * @return the publicEventList
     */
    public List<PublicInfoBundleEventCnF> getPublicEventList() {
        return publicEventList;
    }

    /**
     * @param publicEventList the publicEventList to set
     */
    public void setPublicEventList(List<PublicInfoBundleEventCnF> publicEventList) {
        this.publicEventList = publicEventList;
    }

    public List<PublicInfoBundleCodeViolation> getViolationList() {
        return violationList;
    }

    public void setViolationList(List<PublicInfoBundleCodeViolation> violationList) {
        this.violationList = violationList;
    }

}
