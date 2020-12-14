/*
 * Copyright (C) 2020 Technology Rediscovery LLC
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
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.domain.BlobTypeException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.BlobType;
import com.tcvcog.tcvce.entities.PropertyDataHeavy;
import java.io.IOException;
import java.util.NoSuchElementException;
import javax.annotation.PostConstruct;
import org.primefaces.event.FileUploadEvent;

/**
 *
 * @author sylvia
 */
public class PropertyFilesBB 
        extends BackingBeanUtils{

    private PropertyDataHeavy currProp;
    private int selectedPhotoID;
    
    /**
     * Creates a new instance of PropertyCreateBB
     */
    public PropertyFilesBB() {
    }
    
    
    
     
    @PostConstruct
    public void initBean(){
        currProp = getSessionBean().getSessProperty();
        
    }
    
    /**
     * Removes the link between the current property and the select blob.
     * @param blobID
     */
    public void removePhoto(int blobID){
        try {
            Blob blob = getBlobCoordinator().getPhotoBlob(blobID);
            if(blob.getType() == BlobType.PHOTO){
                getBlobIntegrator().removePhotoPropertyLink(blobID, currProp.getPropertyID());
            }
        }
        catch (IntegrationException | ClassNotFoundException | IOException | BlobTypeException | NoSuchElementException ex) {
            System.out.println(ex);
        }
    }
    
    public void handleFileUpload(FileUploadEvent ev){
        
        try{
        Blob blob = getBlobCoordinator().getNewBlob();
        blob.setBytes(ev.getFile().getContents());
        blob.setType(BlobType.PHOTO); // TODO: BAD CHANGE THIS SOON
         // DO nothing because I'm moving on to other issues,
        // need to be able to compile before I can do much in the way of testing
        } catch (IntegrationException ex){
            
        }
    }
   

    /**
     * @return the currProp
     */
    public PropertyDataHeavy getCurrProp() {
        return currProp;
    }

    /**
     * @param currProp the currProp to set
     */
    public void setCurrProp(PropertyDataHeavy currProp) {
        this.currProp = currProp;
    }
    
    
 

    /**
     * @param selectedPhotoID the selectedPhotoID to set
     */
    public void setSelectedPhotoID(int selectedPhotoID) {
        this.selectedPhotoID = selectedPhotoID;
    }

    /**
     * @return the selectedPhotoID
     */
    public int getSelectedPhotoID() {
        return selectedPhotoID;
    }

}
