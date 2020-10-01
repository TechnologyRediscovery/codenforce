/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.time.LocalDateTime;

/**
 *
 * @author noah
 */
public class Blob {

    private int blobID;
    private BlobType type;
    private String description;
    private String filename;
    private LocalDateTime timestamp;
    private int uploadPersonID;
    private byte[] bytes;
    //private StreamedContent image;
    
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
     * @return the bytes
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * @param bytes the bytes to set
     */
    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
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
    
}
