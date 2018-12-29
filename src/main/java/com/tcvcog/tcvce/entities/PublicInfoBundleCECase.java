/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * An experimental structure that only exposes setters on the publicly released
 * members on a CEActionRequest object. The actual member variables don't exist:
 * only the getters that access the data in the actionRequest memvar;
 * @author sylvia
 */
public class PublicInfoBundleCECase extends PublicInfoBundle implements Serializable {
    
    private int caseID;
    private boolean paccEnabled;

    private CasePhase casePhase;
    // extract only the publicly released events from the CECase's list
    
    
    // not used
    private LocalDateTime originationDate;
    
    private String originiationDatePretty;

    private LocalDateTime closingDate;
    
    private String closingDatePretty;
    
    private List<EventCECase> publicEventList;
    
    private LocalDateTime mostRecentLoggedEvent;
    
    
    private int countNoticeLetters;
    private int countViolations;
    private int countCitations;

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
        sb.append("<br/>");
        
         sb.append("<span class=\"bold\">");
        sb.append("Property Address: ");
        sb.append("</span>");
        sb.append(getPropertyAddress());
        sb.append("<br/>");
        
        sb.append("<span class=\"bold\">");
        sb.append("Case opening date: ");
        sb.append("</span>");
        sb.append(originiationDatePretty);
        sb.append("<br/>");
        
        sb.append("<span class=\"bold\">");
        sb.append("Case closing date: ");
        sb.append("</span>");
        sb.append(closingDatePretty);
        sb.append("<br/>");
        
        sb.append("</p>");
        
        return sb.toString();
        
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.caseID;
        hash = 89 * hash + (this.paccEnabled ? 1 : 0);
        hash = 89 * hash + Objects.hashCode(this.casePhase);
        hash = 89 * hash + Objects.hashCode(this.originationDate);
        hash = 89 * hash + Objects.hashCode(this.originiationDatePretty);
        hash = 89 * hash + Objects.hashCode(this.closingDate);
        hash = 89 * hash + Objects.hashCode(this.closingDatePretty);
        hash = 89 * hash + Objects.hashCode(this.publicEventList);
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
        if (this.caseID != other.caseID) {
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
        if (!Objects.equals(this.originiationDatePretty, other.originiationDatePretty)) {
            return false;
        }
        if (!Objects.equals(this.closingDatePretty, other.closingDatePretty)) {
            return false;
        }
        if (this.casePhase != other.casePhase) {
            return false;
        }
        if (!Objects.equals(this.originationDate, other.originationDate)) {
            return false;
        }
        if (!Objects.equals(this.closingDate, other.closingDate)) {
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
     * @return the caseID
     */
    public int getCaseID() {
        return caseID;
    }


    /**
     * @return the paccEnabled
     */
    public boolean isPaccEnabled() {
        return paccEnabled;
    }

    /**
     * @return the casePhase
     */
    public CasePhase getCasePhase() {
        return casePhase;
    }

    /**
     * @return the originationDate
     */
    public LocalDateTime getOriginationDate() {
        return originationDate;
    }

    /**
     * @return the originiationDatePretty
     */
    public String getOriginiationDatePretty() {
        return originiationDatePretty;
    }

    /**
     * @return the closingDate
     */
    public LocalDateTime getClosingDate() {
        return closingDate;
    }

    /**
     * @return the closingDatePretty
     */
    public String getClosingDatePretty() {
        return closingDatePretty;
    }

    /**
     * @return the publicEventList
     */
    public List<EventCECase> getPublicEventList() {
        return publicEventList;
    }

    /**
     * @param caseID the caseID to set
     */
    public void setCaseID(int caseID) {
        this.caseID = caseID;
    }

  

    /**
     * @param paccEnabled the paccEnabled to set
     */
    public void setPaccEnabled(boolean paccEnabled) {
        this.paccEnabled = paccEnabled;
    }

    /**
     * @param casePhase the casePhase to set
     */
    public void setCasePhase(CasePhase casePhase) {
        this.casePhase = casePhase;
    }

    /**
     * @param originationDate the originationDate to set
     */
    public void setOriginationDate(LocalDateTime originationDate) {
        this.originationDate = originationDate;
    }

    /**
     * @param originiationDatePretty the originiationDatePretty to set
     */
    public void setOriginiationDatePretty(String originiationDatePretty) {
        this.originiationDatePretty = originiationDatePretty;
    }

    /**
     * @param closingDate the closingDate to set
     */
    public void setClosingDate(LocalDateTime closingDate) {
        this.closingDate = closingDate;
    }

    /**
     * @param closingDatePretty the closingDatePretty to set
     */
    public void setClosingDatePretty(String closingDatePretty) {
        this.closingDatePretty = closingDatePretty;
    }

    /**
     * @param publicEventList the publicEventList to set
     */
    public void setPublicEventList(List<EventCECase> publicEventList) {
        this.publicEventList = publicEventList;
    }
    
    
    
    
    
}
