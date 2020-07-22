/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.BlobTypeException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Blob;
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
            blob.setUploadPersonID(uc.getPublicUserAuthorized().getUserID());
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
                Blob blob = bi.getBlob(blobID);
                if (null == blob.getType()) {
                    throw new BlobTypeException("BlobType is null. ");
                } else {
                    switch (blob.getType()) {
                        case PHOTO:
                            sc = new DefaultStreamedContent(new ByteArrayInputStream(blob.getBytes()));
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
        FacesContext context = FacesContext.getCurrentInstance();
        DefaultStreamedContent sc = null;

        BlobIntegrator bi = getBlobIntegrator();
        try {
            Blob blob = bi.getBlob(blobID);

            if (blob == null) {
                throw new BlobTypeException("Blob is null, probably due to an invalid ID");
            } else if (null == blob.getType()) {
                throw new BlobTypeException("BlobType is null. ");
            } else {
                switch (blob.getType()) {
                    case PHOTO:
                        sc = new DefaultStreamedContent(new ByteArrayInputStream(blob.getBytes()));
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
        return getBlobIntegrator().storeBlob(blob);
    }

    public Blob getBlob(int blobID) throws IntegrationException {
        return getBlobIntegrator().getBlob(blobID);
    }

    // TODO: MAYBE seperate into PDF and Photo deletes, verify types appropriately,
    // then delete with integrator.
    public void deleteBlob(int blobID) throws IntegrationException {
        getBlobIntegrator().deleteBlob(blobID);
    }

}
