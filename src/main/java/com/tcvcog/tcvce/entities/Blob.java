/*
 * Copyright (C) 2018 Turtle Creek Valley Council of Governments, PA
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
 * This implementation of Blob inherits the descriptive fields from BlobLight, 
 * but also holds the binary data of the file itself.
 * 
 * @author noah and NADGIT and Ellen Bascom
 */
public class Blob  extends BlobLight {
    
    private byte[] bytes;
    private LocalDateTime blobUploadedTS;
    private User blobUploadedBy;
    

    public Blob(){
    }
    
    public Blob(BlobLight bl){
        this.photoDocID = bl.photoDocID;
        this.description = bl.description;
        this.committed = bl.committed;
        this.bytesID = bl.bytesID;
        this.type = bl.type;
        this.title = bl.title;
        this.muni = bl.muni;
        this.createdTS = bl.createdTS;
        this.createdBy = bl.createdBy;
        this.blobMetadata = bl.blobMetadata;
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
     * @return the filename
     */
    /**
     * @param filename the filename to set
     */

    /**
     * @return the blobUploadedTS
     */
    public LocalDateTime getBlobUploadedTS() {
        return blobUploadedTS;
    }

    /**
     * @return the blobUploadedBy
     */
    public User getBlobUploadedBy() {
        return blobUploadedBy;
    }

    /**
     * @param blobUploadedTS the blobUploadedTS to set
     */
    public void setBlobUploadedTS(LocalDateTime blobUploadedTS) {
        this.blobUploadedTS = blobUploadedTS;
    }

    /**
     * @param blobUploadedBy the blobUploadedBy to set
     */
    public void setBlobUploadedBy(User blobUploadedBy) {
        this.blobUploadedBy = blobUploadedBy;
    }

  

  
}
