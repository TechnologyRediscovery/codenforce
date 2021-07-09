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

import java.time.LocalDateTime;

/**
 * Holds only the descriptive fields of a Blob, not the file itself.
 * @author noah 
 */
public class BlobLight {

    protected int photoDocID;
    protected String description;
    protected boolean committed;
    
    protected int bytesID;
    protected BlobType type;
    protected String title;

    protected Municipality muni;
    protected LocalDateTime createdTS;
    protected User createdBy;
    
    protected Metadata blobMetadata;
    protected String filename;
    
    /**
     * @return the photoDocID
     */
    public int getPhotoDocID() {
        return photoDocID;
    }

    /**
     * @param photoDocID the photoDocID to set
     */
    public void setPhotoDocID(int photoDocID) {
        this.photoDocID = photoDocID;
    }

    /**
     * @return the type
     */
    public BlobType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(BlobType type) {
        this.type = type;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the createdTS
     */
    public LocalDateTime getCreatedTS() {
        return createdTS;
    }

    /**
     * @param createdTS the createdTS to set
     */
    public void setCreatedTS(LocalDateTime createdTS) {
        this.createdTS = createdTS;
    }

   

    public int getBytesID() {
        return bytesID;
    }

    public void setBytesID(int bytesID) {
        this.bytesID = bytesID;
    }

    public Metadata getBlobMetadata() {
        return blobMetadata;
    }

    public void setBlobMetadata(Metadata blobMetadata) {
        this.blobMetadata = blobMetadata;
    }


    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the createdBy
     */
    public User getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return the committed
     */
    public boolean isCommitted() {
        return committed;
    }

    /**
     * @param committed the committed to set
     */
    public void setCommitted(boolean committed) {
        this.committed = committed;
    }

    /**
     * @return the muni
     */
    public Municipality getMuni() {
        return muni;
    }

    /**
     * @param muni the muni to set
     */
    public void setMuni(Municipality muni) {
        this.muni = muni;
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @param filename the filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
}
