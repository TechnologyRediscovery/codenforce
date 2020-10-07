/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.BlobType;
import com.tcvcog.tcvce.integration.BlobIntegrator;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author noah
 */
public class manageBlobBB extends BackingBeanUtils implements Serializable{
    
     private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.
     
    private List<Blob> blobList;
    private Blob selectedBlob;

    /**
     * Creates a new instance of manageBlobBB
     */
    public manageBlobBB() {
        
    }
    
    /**
     * load all blobs uploaded in the past month into memory
     * this is temp for testing, to be replaced by proper query search implementation
     */
    @PostConstruct
    public void intiBean(){
        try {
            this.blobList = getBlobIntegrator().getRecentPhotoBlobs();
        } catch (IntegrationException ex) {
            System.out.println("manageBlobBB.initBean | " + ex + "\n" + ex.getException().getLocalizedMessage());
        }
    }
    
    public void downloadBlob(Blob blob){

        // Prepare.
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();

        BufferedInputStream input = null;
        BufferedOutputStream output = null;

        try {
            // Open file.
            input = new BufferedInputStream(new ByteArrayInputStream(blob.getBytes()));

            // Init servlet response.
            response.reset();
            if(blob.getType() == BlobType.PDF)
                response.setHeader("Content-Type", "application/pdf");
            else
                response.setHeader("Content-Type", "image/png");
            response.setHeader("Content-Length", String.valueOf(blob.getBytes().length));
            response.setHeader("Content-Disposition", "inline; filename=\"" + blob.getFilename() + "\"");
            output = new BufferedOutputStream(response.getOutputStream(), DEFAULT_BUFFER_SIZE);

            // Write file contents to response.
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            // Finalize task.
            output.flush();
        } catch (IOException ex) {
            System.out.println("manageBlobBB.downloadPDF | " + ex);
        } finally {
            // close streams.
            if (output != null){  try { output.close(); } catch (IOException ex) { /* Ignore */ } }
            if (input != null){  try { input.close(); } catch (IOException ex) { /* Ignore */ } }
        }

        // Inform JSF that it doesn't need to handle response.
        facesContext.responseComplete();
    }

    /**
     * @return the blobList
     */
    public List<Blob> getBlobList() {
        return blobList;
    }

    /**
     * @param blobList the blobList to set
     */
    public void setBlobList(List<Blob> blobList) {
        this.blobList = blobList;
    }

    /**
     * @return the selectedBlob
     */
    public Blob getSelectedBlob() {
        return selectedBlob;
    }

    /**
     * @param selectedBlob the selectedBlob to set
     */
    public void setSelectedBlob(Blob selectedBlob) {
        this.selectedBlob = selectedBlob;
    }
    
}
