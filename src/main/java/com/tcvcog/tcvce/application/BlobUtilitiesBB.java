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
import com.tcvcog.tcvce.entities.BlobType;
import com.tcvcog.tcvce.entities.IFace_BlobHolder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
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
    
    private List<BlobType> blobTypeList;
    private List<BlobLight> selectedBlobList;
    
    /**
     * Creates a new instance of BlobUtilitiesBB
     */
    public BlobUtilitiesBB() {
    }
    


    @PostConstruct
    public void initBean() {
        BlobCoordinator bc = getBlobCoordinator();
        
        getSessionBean().setBlobList(new ArrayList<Blob>());
        currentBlobHolder = getSessionBean().getSessBlobHolder();
        editModeBlobMetadata = false;
        
        try {
            blobTypeList = bc.getBlobTypeListComplete();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
        selectedBlobList = new ArrayList<>();
        
        
        
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
                   
                Blob freshBlob = blobc.insertBlobAndInsertMetadataAndLinkToParent(
                                                blob, 
                                                currentBlobHolder, 
                                                getSessionBean().getSessUser(), 
                                                getSessionBean().getSessMuni());
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
            try {
                currentBlobHolder = getSessionBean().setAndRefreshSessionBlobHolderAndBuildUpstreamPool(currentBlobHolder);
            } catch (BObStatusException | BlobException | IntegrationException ex) {
                System.out.println(ex);
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not refresh current blob holder, sorry! This is a fatal error." , ""));
            } 
            
        }
    }
    
    private void refreshCurrentBlobLight() throws IntegrationException, BlobException{
        BlobCoordinator bc = getBlobCoordinator();
        if(currentBlobLight != null){
            currentBlobLight = bc.getBlobLight(currentBlobLight.getPhotoDocID());
            
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
        System.out.println("BlobUtilitiesBB.toggleEditBlobMetadata : start of method " + editModeBlobMetadata);
        if(editModeBlobMetadata){
            editBlobMetadata();
        } else {
            // nothing to do -- we were in view mode
        }
        // flip our switch
        editModeBlobMetadata = !editModeBlobMetadata;
        
    }
    
    /**
     * Passes the bean's currentBlobLight to the BlobCoordintor
     * for metadata updates
     */
    private void editBlobMetadata(){
        BlobCoordinator bc = getBlobCoordinator();
        try {
            bc.updateBlobMetatdata(currentBlobLight, getSessionBean().getSessUser());
            refreshCurrentBlobLight();
            refreshCurrentBlobHolder();
            getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_INFO,
                               "Successfully updated metadata on photo/doc ID: " + currentBlobLight.getPhotoDocID(), ""));
        } catch (IntegrationException | BlobException ex) {
             getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Could not update metata data of your photodoc ID: " + currentBlobLight.getPhotoDocID(), ""));
             System.out.println(ex);
        }
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
     * Listener for user requests to cancel blob meta data edit
     * operation; toggles edit mode to false
     * 
     * @param ev 
     */
    public void onBlobEditMetadataAbortButtonClick(ActionEvent ev){
        editModeBlobMetadata = false;

        
    }
    
    /**
     * Listener for user requests to link the selected blobs 
     * in the pool to the current blob holder
     * @param ev 
     */
    public void linkCurrentBlobHolderToPooledBlobs(ActionEvent ev){
        System.out.println("BlobUtilitiesBB.linkCurrentBlobHolderToPooledBlobs | selectedBlobList: " + selectedBlobList.size());        
        BlobCoordinator bc = getBlobCoordinator();
        try {
            bc.linkBlobHolderToBlobList(currentBlobHolder, selectedBlobList);
            refreshCurrentBlobHolder();
            getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_INFO,
                               "Successfully linked current object to selected photos/documents: " , ""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
              getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                               "Fatal error linking photo/docs from pool to current photo/doc holder. There's nothing you can do about this, sorry! Complain to Eric" , ""));
        }
    }
    
    
    

    /**
     * UNUSUALLY--I GO AND GET MY SESSION BLOB HOLDER ON EACH CALL
     * since I work with lots of object families
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

    /**
     * @return the blobTypeList
     */
    public List<BlobType> getBlobTypeList() {
        return blobTypeList;
    }

    /**
     * @param blobTypeList the blobTypeList to set
     */
    public void setBlobTypeList(List<BlobType> blobTypeList) {
        this.blobTypeList = blobTypeList;
    }

    /**
     * @return the selectedBlobList
     */
    public List<BlobLight> getSelectedBlobList() {
        return selectedBlobList;
    }

    /**
     * @param selectedBlobList the selectedBlobList to set
     */
    public void setSelectedBlobList(List<BlobLight> selectedBlobList) {
        this.selectedBlobList = selectedBlobList;
    }

    
}
