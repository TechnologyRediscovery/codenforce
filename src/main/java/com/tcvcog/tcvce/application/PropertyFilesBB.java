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

import com.tcvcog.tcvce.coordinators.BlobCoordinator;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.BlobTypeException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.BlobTypeEnum;
import com.tcvcog.tcvce.entities.PropertyDataHeavy;
import com.tcvcog.tcvce.integration.BlobIntegrator;
import java.io.IOException;
import java.util.NoSuchElementException;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
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
            if(blob.getType().getTypeEnum()== BlobTypeEnum.PHOTO){
//                getBlobIntegrator().removePropertyBlobLink(blobID, currProp.getPropertyID());
            }
        }
        catch (IntegrationException | BlobException ex) {
            System.out.println(ex);
        }
    }
    
    public void handleFileUpload(FileUploadEvent ev){
        
        if (ev == null) {
            System.out.println("PropertyFilesBB.handlePhotoUpload | event: null");
            return;
        }

        System.out.println("PropertyFilesBB.handlePhotoUpload | File: " + ev.getFile().getFileName() + " Type: " + ev.getFile().getContentType());

        BlobCoordinator blobc = getBlobCoordinator();
        Blob blob = null;
        try {
            blob = blobc.generateBlobSkeleton(getSessionBean().getSessUser());  //init new blob
//            blob.setBytes(ev.getFile().getContents());  // set bytes
            blob.setFilename(ev.getFile().getFileName());
            
            int municode = currProp.getMuniCode();
            
            if(municode == 0){
                municode = currProp.getMuni().getMuniCode();
            }
            
            blob.setMuni(getSessionBean().getSessMuni());
            
            if(blob.getDescription() == null || blob.getDescription().isEmpty()){
                
                blob.setDescription("Picture of " + currProp.getAddress());
                
            }

            blob = blobc.storeBlob(blob);
            
            //Connect blob to prop.
            
            BlobIntegrator blobi = getBlobIntegrator();
            
            blobi.linkBlobToProperty(blob.getPhotoDocID(), currProp.getPropertyID());
            
        } catch (IntegrationException | IOException ex) {
            System.out.println("PropertyFilesBB.handleFileUpload | " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Something went wrong while trying to upload your photo,please try again.",
                            "If this problem persists, please call your municipal office."));
        } catch (BlobException | BlobTypeException ex) {
            System.out.println("PropertyFilesBB.handleFileUpload | " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(),
                            ""));
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
