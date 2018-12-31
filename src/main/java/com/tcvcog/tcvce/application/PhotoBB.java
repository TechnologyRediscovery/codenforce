/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Photograph;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author sylvia
 */
public class PhotoBB extends BackingBeanUtils implements Serializable {

    private int photoID;
    private UploadedFile file;
    
    /**
     * Creates a new instance of PhotoBB
     */
    public PhotoBB() {
    }
    
    public void handleFileUpload(FileUploadEvent ev){
        System.out.println("PhotoBB.handleFileUpload | event: " + ev.toString());
        try {
            
            ImageServices is = getImageServices();
            Photograph ph = new Photograph();
            ph.setPhotoBytes(ev.getFile().getContents());
            ph.setPhotoID(1000);
            ph.setDescription("hello photo!");
            ph.setTypeID(1);
            ph.setTimeStamp(LocalDateTime.now());
            is.storePhotograph(ph);
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }
    

    /**
     * @return the photoID
     */
    public int getPhotoID() {
        System.out.println("PhotoBB.getPhotoID");
        photoID = 1000;
        return photoID;
    }

    /**
     * @param photoID the photoID to set
     */
    public void setPhotoID(int photoID) {
        this.photoID = photoID;
    }

    /**
     * @return the file
     */
    public UploadedFile getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(UploadedFile file) {
        this.file = file;
    }
    
}
