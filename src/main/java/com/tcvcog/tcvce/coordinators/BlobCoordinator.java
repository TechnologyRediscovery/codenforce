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
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.BlobTypeException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.BlobLight;
import com.tcvcog.tcvce.entities.BlobType;
import com.tcvcog.tcvce.entities.Metadata;
import com.tcvcog.tcvce.integration.BlobIntegrator;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author noah
 */
public class BlobCoordinator extends BackingBeanUtils implements Serializable {

    private final StreamedContent image = new DefaultStreamedContent();
    private final int GIGABYTE = 1000000000;

    public Blob getNewBlob() throws IntegrationException {
        Blob blob = new Blob();
        blob.setBlobMetadata(new Metadata());
        blob.setDescription("No description.");
        blob.setTimestamp(LocalDateTime.now());
        if (getSessionBean().getSessUser() != null) {
            blob.setUploadPersonID(getSessionBean().getSessUser().getPersonID());
        } else {
            UserCoordinator uc = getUserCoordinator();
            blob.setUploadPersonID(uc.auth_getPublicUserAuthorized().getUserID());
        }
        return blob;
    }

    /**
     * The BlobCoordinator attempts to automatically retrieve an image for the
     * interface.
     *
     * @return
     * @throws BlobTypeException
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    public StreamedContent getImage() throws BlobTypeException, IOException, ClassNotFoundException {
        // should use EL to verify blob type,  but this will check it anyway
        FacesContext context = FacesContext.getCurrentInstance();
        DefaultStreamedContent sc = null;

        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            return image;
        } else {
            BlobIntegrator bi = getBlobIntegrator();
            int blobID = Integer.parseInt(context.getExternalContext().getRequestParameterMap().get("blobID"));
            try {
                BlobLight blob = bi.getPhotoBlobLight(blobID);
                if (null == blob.getType()) {
                    throw new BlobTypeException("BlobType is null. ");
                } else {
                    switch (blob.getType()) {
                        case PHOTO:
                            sc = new DefaultStreamedContent(new ByteArrayInputStream(bi.getBlobBytes(blobID)));
                            break;
                        case PDF:
                            sc = new DefaultStreamedContent(new FileInputStream(new File("/home/noah/Documents/COG Project/codeconnect/src/main/webapp/images/pdf-icon.png")));
                            break;
                        default:
                            throw new BlobTypeException("Attempted to display incompatible BLOB type. ");
                    }
                }
            } catch (IntegrationException ex) {
                System.out.println("BlobCoordinator.getImage | " + ex);
            } catch (FileNotFoundException ex) {
                System.out.println("BlobCoordinator.getImage | could not find pdf-icon.png ");
            }
            return sc;
        }
    }

    /**
     * Gets an image manually using the given blobID, should be used if
     * getImage() doesn't work.
     *
     * @param blobID
     * @return
     * @throws BlobTypeException
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    public StreamedContent getImageFromID(int blobID) throws BlobTypeException, IOException, ClassNotFoundException {
        //FacesContext context = FacesContext.getCurrentInstance();
        DefaultStreamedContent sc = null;

        BlobIntegrator bi = getBlobIntegrator();
        try {
            BlobLight blob = bi.getPhotoBlobLight(blobID);

            if (blob == null) {
                throw new BlobTypeException("Blob is null, probably due to an invalid ID");
            } else if (null == blob.getType()) {
                throw new BlobTypeException("BlobType is null. ");
            } else {
                switch (blob.getType()) {
                    case PHOTO:
                        sc = new DefaultStreamedContent(new ByteArrayInputStream(bi.getBlobBytes(blob.getBlobID())));
                        break;
                    case PDF:
                        sc = new DefaultStreamedContent(new FileInputStream(new File("/home/noah/Documents/COG Project/codeconnect/src/main/webapp/images/pdf-icon.png")));
                        break;
                    default:
                        throw new BlobTypeException("Attempted to display incompatible BLOB type. ");
                }
            }
        } catch (IntegrationException ex) {
            System.out.println("BlobCoordinator.getImage | " + ex);
        } catch (FileNotFoundException ex) {
            System.out.println("BlobCoordinator.getImage | could not find pdf-icon.png ");
        }
        return sc;
    }

    public int storeBlob(Blob blob) throws BlobException, IntegrationException, IOException {
        //Test to see if the byte array is larger than a GIGABYTE
        if(blob.getBytes().length > GIGABYTE) {
            throw new BlobException("You cannot upload a file larger than 1 gigabyte.");
        }  
        
        // TODO: validate BLOB's and throw exception if corrupted
        
        //First, let's find out what type of file this is.
        
        //split on every dot
        String[] fileNameTokens = blob.getFilename().split(".");
        
        //the last token will contain our file type extension
        String fileExtension = fileNameTokens[fileNameTokens.length - 1];
        
        if(fileExtension.contains("jpg") 
                || fileExtension.contains("jpeg") 
                || fileExtension.contains("gif") 
                || fileExtension.contains("png")){
            blob.setType(BlobType.PHOTO);
        } else if (fileExtension.contains("pdf")){
            blob.setType(BlobType.PDF);
        } else {
            //Incorrect file type
            throw new BlobException("Incompatible file type, please upload a JPG, JPEG, GIF, PNG, or PDF.");
        }
        
        switch(blob.getType()){
            case PHOTO:
                //TODO: Strip metadata from original file and save it in the Metadata dictionary
                
                return getBlobIntegrator().storePhotoBlob(blob);
            case PDF:
                //No PDF methods yet!
                //TODO: Strip metadata from original file and save it in the Metadata dictionary
                return 0;
            default:
                return 0;
        }
        
    }

    public Blob getPhotoBlob(int blobID) throws IntegrationException, IOException, ClassNotFoundException {
        BlobIntegrator bi = getBlobIntegrator();
        
        Blob blob = new Blob(bi.getPhotoBlobLight(blobID));
        
        blob.setBytes(bi.getBlobBytes(blobID));
        
        return blob;
    }

    // TODO: MAYBE seperate into PDF and Photo deletes, verify types appropriately,
    // then delete with integrator.
    public void deleteBlob(int blobID) throws IntegrationException {
        getBlobIntegrator().deletePhotoBlob(blobID);
    }

}
