/*
 * Copyright (C) 2021 Technology Rediscovery LLC
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
import java.util.List;

/**
 *  Container for a docket number attached to a citation
 * Docket numbers are attached to citations after their filing, and
 * can vary by court entity, so a given citation might have multiple docket numbers
 * associated with them. 
 * 
 * Additionally Docket numbers may be separate for each defendant, so
 * docket numbers also have one or more persons linked to them to represent
 * these person associations
 *  
 * @author Ellen Bascomb of Apartment 31Y
 */
public  class   CitationDocketRecord 
        extends TrackedEntity
        implements IFace_humanListHolder,
                   IFace_noteHolder {
    
    final static String PK_FIELD = "docketid";
    final static String TABLE_NAME = "citationdocketno";
    protected LinkedObjectSchemaEnum HUMAN_LINK_SCHEMA_ENUM = LinkedObjectSchemaEnum.CITATIONDCKETHUMAN;
    
    protected int docketID;
    private int citationID;
    protected String docketNumber;
    protected LocalDateTime dateOfRecord;
    protected CourtEntity courtEntity;
    protected String notes;
    
    protected List<HumanLink> humanLinkList;
    
    @Override
    public List<HumanLink> getHumanLinkList() {
        return humanLinkList;
    }

    @Override
    public void setHumanLinkList(List<HumanLink> hll) {
        humanLinkList = hll;
    }

    @Override
    public LinkedObjectSchemaEnum getHUMAN_LINK_SCHEMA_ENUM() {
        return HUMAN_LINK_SCHEMA_ENUM;
        
    }


    @Override
    public int getHostPK() {
        return docketID;
    }
    

    @Override
    public String getPKFieldName() {
        return PK_FIELD;
    }

    @Override
    public int getDBKey() {
        return docketID;
    }

    @Override
    public String getDBTableName() {
        return TABLE_NAME;
    }

    /**
     * @return the PK_FIELD
     */
    public static String getPK_FIELD() {
        return PK_FIELD;
    }

    /**
     * @return the TABLE_NAME
     */
    public static String getTABLE_NAME() {
        return TABLE_NAME;
    }

    /**
     * @return the docketID
     */
    public int getDocketID() {
        return docketID;
    }

    /**
     * @return the docketNumber
     */
    public String getDocketNumber() {
        return docketNumber;
    }

    /**
     * @return the dateOfRecord
     */
    public LocalDateTime getDateOfRecord() {
        return dateOfRecord;
    }

    /**
     * @return the courtEntity
     */
    public CourtEntity getCourtEntity() {
        return courtEntity;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

   

    /**
     * @param docketID the docketID to set
     */
    public void setDocketID(int docketID) {
        this.docketID = docketID;
    }

    /**
     * @param docketNumber the docketNumber to set
     */
    public void setDocketNumber(String docketNumber) {
        this.docketNumber = docketNumber;
    }

    /**
     * @param dateOfRecord the dateOfRecord to set
     */
    public void setDateOfRecord(LocalDateTime dateOfRecord) {
        this.dateOfRecord = dateOfRecord;
    }

    /**
     * @param courtEntity the courtEntity to set
     */
    public void setCourtEntity(CourtEntity courtEntity) {
        this.courtEntity = courtEntity;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @return the citationID
     */
    public int getCitationID() {
        return citationID;
    }

    /**
     * @param citationID the citationID to set
     */
    public void setCitationID(int citationID) {
        this.citationID = citationID;
    }

   
    
}
