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
import com.tcvcog.tcvce.integration.BlobIntegrator;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    public Blob getNewBlob() throws IntegrationException {
        Blob blob = new Blob();
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
     */
    public StreamedContent getImage() throws BlobTypeException {
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
     */
    public StreamedContent getImageFromID(int blobID) throws BlobTypeException {
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

    public int storeBlob(Blob blob) throws BlobException, IntegrationException {
        return getBlobIntegrator().storePhotoBlob(blob);
    }

    public Blob getPhotoBlob(int blobID) throws IntegrationException {
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
