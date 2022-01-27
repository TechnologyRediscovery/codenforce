/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.BlobCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.BlobTypeException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.BlobTypeEnum;
import com.tcvcog.tcvce.entities.IFace_BlobHolder;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import org.primefaces.event.FileUploadEvent;

/**
 * Backing bean for the code resuse blob UI tools
 * that all work with our objects that implement
 * IFace_BlobHolder
 * 
 * @author noah and Ellen Bascomb as of Jan 2022
 */
public class BlobToolsUniversalBB 
        extends BackingBeanUtils 
        implements Serializable {

    /**
     * Creates a new instance of uploadBlobBB
     */
    public BlobToolsUniversalBB() {

    }

    @PostConstruct
    public void initBean() {
        getSessionBean().setBlobList(new ArrayList<Blob>());
    }

    public void handleBlobUpload(FileUploadEvent ev) {
        if (ev == null) {
            System.out.println("CEActionRequestBB.handlePhotoUpload | event: null");
            return;
        }

        System.out.println("CEActionRequestSubmitBB.handlePhotoUpload | File: " + ev.getFile().getFileName() + " Type: " + ev.getFile().getContentType());

        BlobCoordinator blobc = getBlobCoordinator();
        Blob blob = null;

        try {
            blob = blobc.generateBlobSkeleton(getSessionBean().getSessUser());  //init new blob
// TODO pf migrtation https://primefaces.github.io/primefaces/10_0_0/#/../migrationguide/8_0
//            blob.setBytes(ev.getFile().getContents());  // set bytes 

            // set filename
            blob.setFilename(ev.getFile().getFileName());

            blob.setMuni(getSessionBean().getSessMuni());
            
            blob = blobc.storeBlob(blob);
        } catch (IntegrationException | IOException | NoSuchElementException ex) {
            System.out.println("BlobUploadBB.handleBlobUpload | " + ex);
        } catch (BlobException | BlobTypeException ex){
            System.out.println("BlobUploadBB.handleBlobUpload | " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(), ""));
        }

        getSessionBean().getBlobList().add(blob);
    }

    public String navToLinkBlob() {
        return "linkBlob";
    }
    
    
    /**
     * Listener for user requests to upload a file and attach to case
     * I ask the coordinator for the current Blob_Holder and interrogate it
     * for information about where to store its blobs in the DB
     *
     * @param ev
     */
    public void onBlobUploadCommitButtonChange(FileUploadEvent ev) {
        IFace_BlobHolder bh = getSessionBean().getSessBlobHolder();
        System.out.println("BlobToolsUniversalBB.onBlobUploadCommitButtonChange | Beginning storage cycle!");
        if (bh != null && bh.getBlobLinkEnum() != null && ev != null && ev.getFile() != null) {

            try {
                BlobCoordinator blobc = getBlobCoordinator();

                Blob blob = blobc.generateBlobSkeleton(getSessionBean().getSessUser());
                blob.setBytes(ev.getFile().getContent());
                blob.setFilename(ev.getFile().getFileName());
                blob.setMuni(getSessionBean().getSessMuni());

                Blob freshBlob = blobc.insertBlobAndInsertMetadataAndLinkToParent(blob, bh);
                // ship to coordinator for storage
                if (freshBlob != null) {
                    System.out.println("cecaseSearchProfileBB.onBlobUploadCommitButtonChange | fresh blob ID: " + freshBlob.getPhotoDocID());
                }

            } catch (IntegrationException | IOException | BlobException | BlobTypeException | BObStatusException ex) {
                System.out.println("cecaseSearchProfileBB.onBlobUploadCommitButtonChange | upload failed! " + ex);
                System.out.println(ex);
            }
        }
    }

}
