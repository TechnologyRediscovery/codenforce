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
import com.tcvcog.tcvce.entities.BlobLight;
import com.tcvcog.tcvce.entities.IFace_BlobHolder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import org.primefaces.event.FileUploadEvent;

/**
 * Master Bean for our shared blob tools made possible by the
 * IFace_BlobHolder. This is a view scoped bean that backs
 * blobTools.xhtml composition
 * 
 * @author Ellen Bascomb (Piloted Jan 2022)
 */
public class    BlobUtilitiesBB 
        extends BackingBeanUtils{

    private IFace_BlobHolder currentBlobHolder;
    private BlobLight currentBlobLight;
    private boolean editModeBlobMetadata;
    
    
    /**
     * Creates a new instance of BlobUtilitiesBB
     */
    public BlobUtilitiesBB() {
    }
    


    @PostConstruct
    public void initBean() {
        getSessionBean().setBlobList(new ArrayList<Blob>());
        currentBlobHolder = getSessionBean().getSessBlobHolder();
        editModeBlobMetadata = false;
    }

    
    /**
     * Old version not oriented at code reuse
     * @deprecated 
     * @param ev 
     */
    public void handleBlobUpload(FileUploadEvent ev) {
        if (ev == null) {
            System.out.println("BlobUtilitiesBB.handlePhotoUpload | event: null");
            return;
        }

        System.out.println("BlobUtilitiesBB.handlePhotoUpload | File: " + ev.getFile().getFileName() + " Type: " + ev.getFile().getContentType());

        BlobCoordinator blobc = getBlobCoordinator();
        Blob blob = null;

        try {
            blob = blobc.generateBlobSkeleton(getSessionBean().getSessUser());  //init new blob
            
            blob.setBytes(ev.getFile().getContent());  // set bytes 

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
        // as the session for our blob holder
        // client UIs must set this up for me to know who to connect 
        // the blob to
        
        currentBlobHolder = getSessionBean().getSessBlobHolder();
        System.out.println("BlobToolsUniversalBB.onBlobUploadCommitButtonChange | Beginning storage cycle!");
        
        if (currentBlobHolder != null && currentBlobHolder.getBlobLinkEnum() != null && ev != null && ev.getFile() != null) {

            try {
                BlobCoordinator blobc = getBlobCoordinator();

                Blob blob = blobc.generateBlobSkeleton(getSessionBean().getSessUser());
                blob.setBytes(ev.getFile().getContent());
                blob.setFilename(ev.getFile().getFileName());
                blob.setMuni(getSessionBean().getSessMuni());
                Blob freshBlob = blobc.insertBlobAndInsertMetadataAndLinkToParent(blob, currentBlobHolder, getSessionBean().getSessUser());
                // ship to coordinator for storage
                if (freshBlob != null) {
                    System.out.println("BlobUtilitiesBB.onBlobUploadCommitButtonChange | fresh blob ID: " + freshBlob.getPhotoDocID());
                
                    getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Upload success! File " + ev.getFile().getFileName() + " Is stored with PhotoDoc ID: " + freshBlob.getPhotoDocID(), ""));
                }
                refreshCurrentBlobHolder();

            } catch (IntegrationException | IOException | BlobException | BlobTypeException | BObStatusException ex) {
                System.out.println("BlobUtilitiesBB.onBlobUploadCommitButtonChange | upload failed! " + ex);
                System.out.println(ex);
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Fatal error on upload of file: " + ev.getFile().getFileName() , ""));
            }
        }
    }
    
    /**
     * Asks the session bean to refresh the current blob holder and
     * then I update this bean's current blob holder
     */
    public void refreshCurrentBlobHolder(){
        if(currentBlobHolder != null){
            currentBlobHolder = getSessionBean().refreshSessionBlobHolder();
            
        }
    }
    
    /**
     * Listener for user requests to view a bloblight
     * BlobLights don't have the bytes in them--they just
     * have the ID of the bytes which can be used
     * to extract all the bytes
     * @param bl 
     */
    public void onViewBlobLinkClick(BlobLight bl){
        System.out.println("BlobUtilitiesBB.onViewBlobLinkClick | viewing blob light ID " + bl.getPhotoDocID());
        currentBlobLight = bl;
    }
    
    
    /**
     * Listener for user requests to switch between 
     * view and edit mode for blob metadata
     */
    public void toggleEditModeBlobMetadata(){
        if(editModeBlobMetadata){
            
        } else {
            // nothing to do -- we were in view mode
        }
        // flip our switch
        editModeBlobMetadata = !editModeBlobMetadata;
        
    }
    
    /**
     * Listener for user request to edit a blob's meta data
     * @param bl 
     */
    public void onBlobEditMetaDataLinkClick(BlobLight bl){
        currentBlobLight = bl;
        editModeBlobMetadata = true;
        
    }
    
    
    
    

    /**
     * @return the currentBlobHolder
     */
    public IFace_BlobHolder getCurrentBlobHolder() {
        currentBlobHolder = getSessionBean().getSessBlobHolder();
        if(currentBlobHolder != null){
            System.out.println("BlobUtilitiesBB.getCurrentBlobHolder | Class " + currentBlobHolder.getClass());
            System.out.println("BlobUtilitiesBB.getCurrentBlobHolder | parent ID: " + currentBlobHolder.getParentObjectID());
            
        } else {
            System.out.println("BlobUtilitiesBB.getCurrentBlobHolder" );
            
        }
        return currentBlobHolder;
    }

    /**
     * @param currentBlobHolder the currentBlobHolder to set
     */
    public void setCurrentBlobHolder(IFace_BlobHolder currentBlobHolder) {
        this.currentBlobHolder = currentBlobHolder;
    }

    /**
     * @return the currentBlobLight
     */
    public BlobLight getCurrentBlobLight() {
        return currentBlobLight;
    }

    /**
     * @param currentBlobLight the currentBlobLight to set
     */
    public void setCurrentBlobLight(BlobLight currentBlobLight) {
        this.currentBlobLight = currentBlobLight;
    }

    /**
     * @return the editModeBlobMetadata
     */
    public boolean isEditModeBlobMetadata() {
        return editModeBlobMetadata;
    }

    /**
     * @param editModeBlobMetadata the editModeBlobMetadata to set
     */
    public void setEditModeBlobMetadata(boolean editModeBlobMetadata) {
        this.editModeBlobMetadata = editModeBlobMetadata;
    }

    
}
