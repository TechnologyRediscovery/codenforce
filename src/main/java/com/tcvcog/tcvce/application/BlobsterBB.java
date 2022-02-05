/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.BlobCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.MetadataException;
import com.tcvcog.tcvce.entities.Blob;
import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * Bean for loading images
 * @author sylvia
 */
public class BlobsterBB extends BackingBeanUtils{
    
    private StreamedContent image;

    /**
     * Creates a new instance of BlobsterBB
     */
    public BlobsterBB() {
            
        FacesContext fc = FacesContext.getCurrentInstance();

        // as of JAN 2022--getting a "response already committed error, so commenting out
        //ec.responseReset(); // Some JSF component library or some Filter might have set some headers in the buffer beforehand. We want to get rid of them, else it may collide.

        BlobCoordinator bc = getBlobCoordinator();
        Blob blob = null;
        String blobidStr = fc.getExternalContext().getRequestParameterMap().get("blobid");
        
        if(blobidStr != null && !blobidStr.equals("")){

            try {
                blob = bc.getBlob(Integer.parseInt(blobidStr));
            } catch (IntegrationException | MetadataException ex) {
                System.out.println(ex);

            }

            if(blob != null && blob.getBytes() != null){
                System.out.println("BobRetrieveBB.retrieveBlob: received BlobLight ID " + blob.getPhotoDocID() );
                System.out.println("BobRetrieveBB.retrieveBlob: extracted blob bytes ID " + blob.getBytesID() + " | bytea size: " + blob.getBytes().length);
                final Blob b = blob;
                image = DefaultStreamedContent.builder()
                        .contentType(blob.getType().getContentTypeString())
                        .name(blob.getFilename())
                        .stream(() -> new ByteArrayInputStream(b.getBytes()))
                        .build();
             } else {
                 System.out.println("BobRetrieveBB.retrieveBlob: extracted null blob from BlobLight ID " );

             }
        }
    }

    /**
     * @return the image
     */
    public StreamedContent getImage() {
        return image;
    }
    
    
    
}
