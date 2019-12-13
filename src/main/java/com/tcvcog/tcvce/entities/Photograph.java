/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.time.LocalDateTime;

/**
 *
 * @author sylvia
 */
public class Photograph  {
    
    private int photoID;
    private String description;
    private LocalDateTime timeStamp;
    private String timestampPretty;
    private int typeID;
    private byte[] photoBytes;


    /**
     * @return the photoBytes
     */
    public byte[] getPhotoBytes() {
        return photoBytes;
    }

    /**
     * @param photoBytes the photoBytes to set
     */
    public void setPhotoBytes(byte[] photoBytes) {
        this.photoBytes = photoBytes;
    }

    /**
     * @return the photoID
     */
    public int getPhotoID() {
        return photoID;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the timeStamp
     */
    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    /**
     * @return the typeID
     */
    public int getTypeID() {
        return typeID;
    }

    /**
     * @param photoID the photoID to set
     */
    public void setPhotoID(int photoID) {
        this.photoID = photoID;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        System.out.println("in setDescription");
        this.description = description;
    }

    /**
     * @param timeStamp the timeStamp to set
     */
    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * @param typeID the typeID to set
     */
    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }

    /**
     * @return the getTimestampPretty
     */
    public String getTimestampPretty() {
        if(timeStamp != null){
            timestampPretty = EntityUtils.getPrettyDate(timeStamp);
        }
        return timestampPretty;
    }

    /**
     * @param timestampPretty
     */
    public void setTimestampPretty(String timestampPretty) {
        this.timestampPretty = timestampPretty;
    }
    
    
    
    
}
