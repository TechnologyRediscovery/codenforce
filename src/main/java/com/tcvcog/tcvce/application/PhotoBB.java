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
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;

/**
 *
 * @author sylvia
 */
public class PhotoBB extends BackingBeanUtils implements Serializable {

    private int photoID;
    private Photograph photo;
    private ArrayList<Photograph> photoList;
    
    /**
     * Creates a new instance of PhotoBB
     */
    public PhotoBB() {
    //    this.getAllPhotos();
    }
    
    public StreamedContent displayPhoto(Photograph photo){
        System.out.println("PhotoBB | displayPhoto: in method");
        FacesContext context = FacesContext.getCurrentInstance();
        DefaultStreamedContent sc =null;
        
        if(context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            sc = new DefaultStreamedContent();
            return sc;
        } else {
            sc = new DefaultStreamedContent(new ByteArrayInputStream(photo.getPhotoBytes()), "image/png", Integer.toString(photo.getPhotoID()));
            return sc;
        }
    }
    
    public void deletePhoto(int phID){
        ImageServices is = getImageServices();
        try {
            is.deletePhotograph(phID);
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
    
    public void searchForPhotoByPropID(int propID){
        // TODO: This is a copy/paste job, dont use it yet my G
        ImageServices is = getImageServices();
        try {
            this.setPhoto(is.getPhotograph(propID));
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
     * @return the photoList
     */
    public ArrayList<Photograph> getPhotoList() {
        return photoList;
    }

    /**
     * @param photoList the photoList to set
     */
    public void setPhotoList(ArrayList<Photograph> photoList) {
        this.photoList = photoList;
    }

}
