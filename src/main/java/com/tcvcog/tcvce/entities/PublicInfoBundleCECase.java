/*
 * Copyright (C) Technology Rediscovery LLC. 2020
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
package com.tcvcog.tcvce.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A wrapper class that stores a CECase that is stripped of all sensitive
 * information.
 * Look at the JavaDocs of the PublicInfoBundle Class for more information.
 *
 * @author Nathan Dietz
 */
public class PublicInfoBundleCECase extends PublicInfoBundle{

    private CECase bundledCase;

    //Stores lists of anonymized objects
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

    /**
     * Remove all sensitive data from the CECase and set it in the
     * bundledCase field.
     * @param input 
     */
    public void setBundledCase(CECase input) {

        input.setPropertyID(0);
        input.setPropertyUnitID(0);
        input.setNotes("*****");
        input.setSource(new BOBSource());

        setPacc(input.getPublicControlCode());

        //Also, count all the attached objects
        
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