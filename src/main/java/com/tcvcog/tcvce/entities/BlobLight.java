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

    protected int blobID;
    protected int bytesID;
    protected BlobType type;
    protected String description, filename;
    protected LocalDateTime timestamp;
    protected int uploadPersonID;
    protected int municode;
    
    protected Metadata blobMetadata;
    
    /**
     * @return the blobID
     */
    public int getBlobID() {
        return blobID;
    }

    /**
     * @param blobID the blobID to set
     */
    public void setBlobID(int blobID) {
        this.blobID = blobID;
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
     * @return the timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the uploadPersonID
     */
    public int getUploadPersonID() {
        return uploadPersonID;
    }

    /**
     * @param uploadPersonID the uploadPersonID to set
     */
    public void setUploadPersonID(int uploadPersonID) {
        this.uploadPersonID = uploadPersonID;
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

    public int getMunicode() {
        return municode;
    }

    public void setMunicode(int municode) {
        this.municode = municode;
    }
    
}
