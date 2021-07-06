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
package com.tcvcog.tcvce.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Upgraded Citation version to utilize humanization principles
 * 
 * @author ellen bascomb of apt 31y
 */
public class    Citation 
        extends TrackedEntity
        implements IFace_humanListHolder{
    
    final static String CITATION_TABLE = "citation";
    final static String CITATION_PKFIELD = "citationid";
    
    private int cecaseID;
    
    /**
     * Database Key
     */
    private int citationID;
    
    /**
     * Internal tracking
     */
    private String citationNo;
    
    /**
     * External tracking by magistrate
     */
    private String docketNo;
    protected User filingOfficer;
    
    private List<CitationStatusLogEntry> statusLog;
    private CourtEntity origin_courtentity;
    private String officialText;
    
    private LocalDateTime dateOfRecord;
    
    // notice that to avoid cycles, the Citation is allowed to have actual CodeViolation
    // objects in its LinkedList but CodeViolation only gets the citation IDs which
    // it can use to look up a Citation if needs be
    private List<EventCnF> eventList;
    private List<CitationCodeViolationLink> violationList;
    private List<BlobLight> blobList;

    protected LinkedHumanSchemaEnum humanLinkSchemaEnum;
    protected List<HumanLink> humanLinkList;
    
    
    
    private String notes;
    

    /**
     * @return the citationID
     */
    public int getCitationID() {
        return citationID;
    }

    /**
     * @return the citationNo
     */
    public String getCitationNo() {
        return citationNo;
    }
    
    /**
     * @return the dateOfRecord
     */
    public LocalDateTime getDateOfRecord() {
        return dateOfRecord;
    }


    /**
     * @return the notesO
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param citationID the citationID to set
     */
    public void setCitationID(int citationID) {
        this.citationID = citationID;
    }

    /**
     * @param citationNo the citationNo to set
     */
    public void setCitationNo(String citationNo) {
        this.citationNo = citationNo;
    }

    /**
     * @param dateOfRecord the dateOfRecord to set
     */
    public void setDateOfRecord(LocalDateTime dateOfRecord) {
        this.dateOfRecord = dateOfRecord;
    }


    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @return the violationList
     */
    public List<CitationCodeViolationLink> getViolationList() {
        return violationList;
    }

    /**
     * @param violationList the violationList to set
     */
    public void setViolationList(List<CitationCodeViolationLink> violationList) {
        this.violationList = violationList;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + this.citationID;
        hash = 71 * hash + Objects.hashCode(this.citationNo);
        hash = 71 * hash + Objects.hashCode(this.dateOfRecord);
        hash = 71 * hash + Objects.hashCode(this.notes);
        hash = 71 * hash + Objects.hashCode(this.violationList);
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
        final Citation other = (Citation) obj;
        if (this.citationID != other.citationID) {
            return false;
        }
        if (this.getOrigin_courtentity() != other.getOrigin_courtentity()) {
            return false;
        }
        if (!Objects.equals(this.citationNo, other.citationNo)) {
            return false;
        }
        if (!Objects.equals(this.notes, other.notes)) {
            return false;
        }
        if (!Objects.equals(this.dateOfRecord, other.dateOfRecord)) {
            return false;
        }
        if (!Objects.equals(this.violationList, other.violationList)) {
            return false;
        }
        return true;
    }

    /**
     * @return the status
     */
    public List<CitationStatusLogEntry> getStatusLog() {
        return statusLog;
    }

    /**
     * @param status the status to set
     */
    public void setStatusLog(List<CitationStatusLogEntry> status) {
        this.statusLog = status;
    }
    
    public CitationStatusLogEntry getStatus(){
        if(statusLog != null && !statusLog.isEmpty()){
            return statusLog.get(0); // return the first, most current status log entry
        } 
        return null;
    }

    /**
     * @return the origin_courtentity
     */
    public CourtEntity getOrigin_courtentity() {
        return origin_courtentity;
    }

    /**
     * @param origin_courtentity the origin_courtentity to set
     */
    public void setOrigin_courtentity(CourtEntity origin_courtentity) {
        this.origin_courtentity = origin_courtentity;
    }


  
    /**
     * @return the eventList
     */
    public List<EventCnF> getEventList() {
        return eventList;
    }

    /**
     * @param eventList the eventList to set
     */
    public void setEventList(List<EventCnF> eventList) {
        this.eventList = eventList;
    }

   

    /**
     * @return the officialText
     */
    public String getOfficialText() {
        return officialText;
    }

    /**
     * @param officialText the officialText to set
     */
    public void setOfficialText(String officialText) {
        this.officialText = officialText;
    }

    /**
     * @return the docketNo
     */
    public String getDocketNo() {
        return docketNo;
    }

    /**
     * @param docketNo the docketNo to set
     */
    public void setDocketNo(String docketNo) {
        this.docketNo = docketNo;
    }

    /**
     * @return the citationNo
     */
    public String getCitatonNo() {
        return citationNo;
    }

    /**
     * @param citatonNo the citationNo to set
     */
    public void setCitatonNo(String citatonNo) {
        this.citationNo = citatonNo;
    }

    /**
     * @return the blobList
     */
    public List<BlobLight> getBlobList() {
        return blobList;
    }

    /**
     * @param blobList the blobList to set
     */
    public void setBlobList(List<BlobLight> blobList) {
        this.blobList = blobList;
    }


    @Override
    public String getPKFieldName() {
        return CITATION_PKFIELD;
    }

    @Override
    public int getDBKey() {
         return citationID;
    }

    @Override
    public String getDBTableName() {
        return CITATION_TABLE;
    }
    
    @Override
    public List<HumanLink> getHumanLinkList() {
        return humanLinkList;
    }

    @Override
    public void setHumanLinkList(List<HumanLink> hll) {
        humanLinkList = hll;
    }

    @Override
    public LinkedHumanSchemaEnum getLinkSchema() {
        return humanLinkSchemaEnum;
    }

    @Override
    public void setLinkSchema(LinkedHumanSchemaEnum lhse) {
        humanLinkSchemaEnum = lhse;
    }

    @Override
    public int getHostPK() {
        return citationID;
    }

    /**
     * @return the filingOfficer
     */
    public User getFilingOfficer() {
        return filingOfficer;
    }

    /**
     * @param filingOfficer the filingOfficer to set
     */
    public void setFilingOfficer(User filingOfficer) {
        this.filingOfficer = filingOfficer;
    }

    /**
     * @return the cecaseID
     */
    public int getCecaseID() {
        return cecaseID;
    }

    /**
     * @param cecaseID the cecaseID to set
     */
    public void setCecaseID(int cecaseID) {
        this.cecaseID = cecaseID;
    }

}

