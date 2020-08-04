/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.BlobCoordinator;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.BlobType;
import java.io.Serializable;
import java.util.ArrayList;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import org.primefaces.event.FileUploadEvent;

/**
 *
 * @author noah
 */
public class UploadBlobBB extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of uploadBlobBB
     */
    public UploadBlobBB() {

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

        // verify blob types here. Post a FacesMessage if file type is not an image
        String fileType = ev.getFile().getContentType();
        System.out.println("CEActionRequestSubmitBB.handlePhotoUpload | File: " + ev.getFile().getFileName() + " Type: " + fileType);

        if (!fileType.contains("jpg") && !fileType.contains("gif") && !fileType.contains("png") && !fileType.contains("pdf")) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Incompatible file type. ",
                            "Please upload supported file types only (jpg, gif, png, pdf)."));
        }

        BlobCoordinator blobc = getBlobCoordinator();
        Blob blob = null;

        try {
            blob = blobc.getNewBlob();  //init new blob
            blob.setBytes(ev.getFile().getContents());  // set bytes 

            // set filename
            blob.setFilename(ev.getFile().getFileName());

            // set type
            if (fileType.contains("jpg") || fileType.contains("gif") || fileType.contains("png")) {
                blob.setType(BlobType.PHOTO);
            } else if (fileType.contains("pdf")) {
                blob.setType(BlobType.PDF);
            }
            blob.setBlobID(blobc.storeBlob(blob));
        } catch (BlobException | IntegrationException ex) {
            System.out.println("BlobUploadBB.handleBlobUpload | " + ex);
        }

        getSessionBean().getBlobList().add(blob);
    }

    public String navToLinkBlob() {
        return "linkBlob";
    }

}
