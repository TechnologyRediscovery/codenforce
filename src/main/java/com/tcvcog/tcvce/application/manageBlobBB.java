/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.integration.BlobIntegrator;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;

/**
 *
 * @author noah
 */
public class manageBlobBB extends BackingBeanUtils implements Serializable{
    
    private List<Blob> blobList;

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
            this.blobList = getBlobIntegrator().getRecentBlobs();
        } catch (IntegrationException ex) {
            System.out.println("manageBlobBB.initBean | " + ex);
        }
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
    
}
