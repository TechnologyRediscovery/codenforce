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

/**
 * This implementation of Blob inherits the descriptive fields from BlobLight, and also holds the bytes of file itself.
 * @author noah
 */
public class Blob  extends BlobLight {

    public Blob(){
    }
    
    public Blob(BlobLight input){
        blobID = input.getBlobID();
        bytesID = input.getBytesID();
        type = input.getType();
        description = input.getDescription();
        filename = input.getFilename();
        timestamp = input.getTimestamp();
        uploadPersonID = input.getUploadPersonID();
        
    }
    
    private byte[] bytes;
    
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
    
}
