/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.BlobCoordinator;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.BlobTypeException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.BlobType;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.NoSuchElementException;
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

        System.out.println("CEActionRequestSubmitBB.handlePhotoUpload | File: " + ev.getFile().getFileName() + " Type: " + ev.getFile().getContentType());

        BlobCoordinator blobc = getBlobCoordinator();
        Blob blob = null;

        try {
            blob = blobc.generateBlobSkeleton();  //init new blob
// TODO pf migrtation https://primefaces.github.io/primefaces/10_0_0/#/../migrationguide/8_0
//            blob.setBytes(ev.getFile().getContents());  // set bytes 

            // set filename
            blob.setFilename(ev.getFile().getFileName());

            blob.setMunicode(getSessionBean().getSessMuni().getMuniCode());
            
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

}
