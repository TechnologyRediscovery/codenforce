/*
 * Copyright (C) 2021 Technology Rediscovery LLC
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
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.BlobLight;
import com.tcvcog.tcvce.integration.BlobIntegrator;
import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * Attempt at a bean that gets image and pdf blobs:  Perhaps some is
 * not working, but ECD checked on 26JAN22 and the working
 * system is using retrieveBlob method!!
 * 
 * 
 * @author Ellen Bascomb
 */
public class BlobRetrieveBB extends BackingBeanUtils {

    /**
     * Creates a new instance of BlobRetrieveBB
     */
    public BlobRetrieveBB() {
    }
    
     private StreamedContent blobStream = null;

    public void setupBlob() {
        blobStream = DefaultStreamedContent.builder()
                    .contentType("image/jpeg")
                    .stream(() -> {
                        FacesContext context = FacesContext.getCurrentInstance();
                        String userId = context.getExternalContext().getRequestParameterMap().get("user");
                        return this.getClass().getResourceAsStream("user" + userId + ".jpg");
                    })
                    .build();
    }

    /**
     * Listener for client requests to stream blob bytes from a given blobLight
     * @param b
     * @return 
     */
    public StreamedContent retrieveBlob(BlobLight b) {
       
        if(b != null){
            
            FacesContext fc = FacesContext.getCurrentInstance();
            ExternalContext ec = fc.getExternalContext();

            // as of JAN 2022--getting a "response already committed error, so commenting out
            //ec.responseReset(); // Some JSF component library or some Filter might have set some headers in the buffer beforehand. We want to get rid of them, else it may collide.


                 BlobCoordinator bc = getBlobCoordinator();
                 Blob blob = bc.getBlob(b);
                 if(blob != null && blob.getBytes() != null){
                     System.out.println("BobRetrieveBB.retrieveBlob: received BlobLight ID " + b.getPhotoDocID() );
                     System.out.println("BobRetrieveBB.retrieveBlob: extracted blob bytes ID " + blob.getBytesID() + " | bytea size: " + blob.getBytes().length);

                 blobStream = DefaultStreamedContent.builder()
                         .contentType(b.getType().getContentTypeString())
                         .name(blob.getFilename())
                         .stream(() -> new ByteArrayInputStream(blob.getBytes()))
                         .build();
                 } else {
                     System.out.println("BobRetrieveBB.retrieveBlob: extracted null blob from BlobLight ID " + b.getPhotoDocID() );

                 }
             fc.responseComplete(); 
        }
    
        return blobStream;
    }
    
}
