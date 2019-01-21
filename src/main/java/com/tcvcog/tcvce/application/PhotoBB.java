/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Photograph;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import com.tcvcog.tcvce.util.Constants;

/**
 *
 * @author sylvia
 */
public class PhotoBB extends BackingBeanUtils implements Serializable {

    private int photoID;
    private Photograph photo;
    private ArrayList<Photograph> phList;
    
    /**
     * Creates a new instance of PhotoBB
     */
    public PhotoBB() {
    //    this.getAllPhotos();
    }
    
    public StreamedContent displayPhoto(Photograph photo){
        ImageServices is = getImageServices();
        StreamedContent photoSC = null;
        photoSC = new DefaultStreamedContent(
            new ByteArrayInputStream(photo.getPhotoBytes()), "image/jpeg");
        return photoSC;
    }
    
    public void deletePhoto(int phID){
        ImageServices is = getImageServices();
        try {
            is.deletePhotograph(phID);
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }
    
    public void getAllPhotos(){
        ImageServices is = getImageServices();
        try {
            this.setPhList(is.getAllPhotographs());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }
    
    public void searchForPhotoByID(int phID){
        ImageServices is = getImageServices();
        try {
            this.setPhoto(is.getPhotograph(phID));
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }
    
    public void handleFileUpload(FileUploadEvent ev){
        if(ev == null){
            System.out.println("PhotoBB.handleFileUpload | event: null");
            return;
        }
       
        System.out.println("PhotoBB.handleFileUpload | event: " + ev.toString());
        try {
            
            ImageServices is = getImageServices();
            Photograph ph = new Photograph();
            ph.setPhotoBytes(ev.getFile().getContents());
            ph.setDescription("hello photo!");
            ph.setTypeID(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("photoTypeId")));
            ph.setTimeStamp(LocalDateTime.now());
            is.storePhotograph(ph);
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        this.getAllPhotos();
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
     * @return the photo
     */
    public Photograph getPhoto() {
        return photo;
    }

    /**
     * @param photo the photo to set
     */
    public void setPhoto(Photograph photo) {
        this.photo = photo;
    }

    /**
     * @return the phList
     */
    public ArrayList<Photograph> getPhList() {
        return phList;
    }

    /**
     * @param phList the phList to set
     */
    public void setPhList(ArrayList<Photograph> phList) {
        this.phList = phList;
    }

}
