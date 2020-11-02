/*
 * Copyright (C) 2020 Technology Rediscovery LLC
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
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.BlobLight;
import com.tcvcog.tcvce.entities.BlobType;
import com.tcvcog.tcvce.integration.BlobIntegrator;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author noah
 */
public class manageBlobBB extends BackingBeanUtils implements Serializable{
    
    private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.
     
    private String currentMode;
     
    private List<BlobLight> blobList;
    private BlobLight selectedBlob;
    private boolean currentBlobSelected;
    
    //search parameters.
    //Should eventually be replaced with an implementation of the Query object
    private String searchFilename;
    private String searchDescription;
    private LocalDateTime searchBefore;
    private LocalDateTime searchAfter;
    
    /**
     * load all blobs uploaded in the past month into memory
     * this is temp for testing, to be replaced by proper query search implementation
     */
    @PostConstruct
    public void initBean(){
        
        setCurrentMode("Info");
        
        try {
            BlobIntegrator bi = getBlobIntegrator();
            blobList = new ArrayList<>();
            List<Integer> blobIDs = bi.getRecentPhotoBlobs();
            for (int idnum : blobIDs) {
                blobList.add(bi.getPhotoBlobLight(idnum));
            }
            
        } catch (IntegrationException | ClassNotFoundException | IOException ex) {
            System.out.println("manageBlobBB.initBean | ERROR: " + ex);
        }
    }
    
    public void downloadSelectedBlob(){

        // Prepare.
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();

        BufferedInputStream input = null;
        BufferedOutputStream output = null;

        BlobCoordinator bc = getBlobCoordinator();
        
        Blob blob = null;
        
        try {
            
            // Init servlet response.
            response.reset();
            
            switch(selectedBlob.getType()){
            case PDF:
                response.setHeader("Content-Type", "application/pdf");
                //PDF downloads not yet supported
                //blob = bc.getPDFBlob(selectedBlob.getBlobID())
                break;
                
            case PHOTO:
                response.setHeader("Content-Type", "image/" + bc.getFileExtension(selectedBlob.getFilename()));
                blob = bc.getPhotoBlob(selectedBlob.getBlobID());
                break;
                
            default:
                //do nothing
                break;
            }
            
            if(blob != null){
            // Open file.
            input = new BufferedInputStream(new ByteArrayInputStream(blob.getBytes()));
                
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
            } else {
                throw new BlobException("BlobType not yet supported for download or blob failed to load.");
            }
        } catch (IOException | ClassNotFoundException | IntegrationException | BlobException ex) {
            System.out.println("manageBlobBB.downloadSelectedBlob | " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occured while trying to prepare your download!", ""));
        } finally {
            // close streams.
            if (output != null){  try { output.close(); } catch (IOException ex) { /* Ignore */ } }
            if (input != null){  try { input.close(); } catch (IOException ex) { /* Ignore */ } }
        }

        // Inform JSF that it doesn't need to handle response.
        facesContext.responseComplete();
    }

    public void executeQuery(){
        BlobCoordinator bc = getBlobCoordinator();
        
        try{
            blobList = bc.searchBlobs(searchFilename, searchDescription, searchBefore, searchAfter);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Found " + blobList.size() + " files matching your criteria!", ""));
        } catch(ClassNotFoundException | IOException | IntegrationException ex){
            System.out.println("manageBlobBB.executeQuery() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occured while trying to search for files!", ""));
        }
    }
    
    public void selectBlob(BlobLight blob){
        BlobCoordinator bc = getBlobCoordinator();
        
        selectedBlob = blob;
        
        currentBlobSelected = true;
        
    }
    
    /**
     * @return the selectedBlob
     */
    public BlobLight getSelectedBlob() {
        return selectedBlob;
    }

    /**
     * @param selectedBlob the selectedBlob to set
     */
    public void setSelectedBlob(BlobLight selectedBlob) {
        this.selectedBlob = selectedBlob;
    }

    /**
     * Determines whether or not a user should currently be able to select a
     * blob or deselect a blob.
     *
     * @return
     */
    public boolean getSelectedButtonActive() {
        return getActiveInfoMode() || getActiveViewMode();
    }

    public boolean getActiveInfoMode() {
        return "Info".equals(currentMode);
    }

    public boolean getActiveViewMode() {
        return "View".equals(currentMode);
    }

    public boolean getActiveObjectsMode() {
        return "Objects".equals(currentMode);
    }

    public boolean getActiveUpdateMode() {
        return "Update".equals(currentMode);
    }

    public String getCurrentMode() {
        return currentMode;
    }
    
    public void setCurrentMode(String input) {
        if(input != null){
        currentMode = input;
        getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            this.currentMode + " Mode Selected", ""));
        }
    }

    public List<BlobLight> getBlobList() {
        return blobList;
    }

    public void setBlobList(List<BlobLight> blobList) {
        this.blobList = blobList;
    }

    public boolean isCurrentBlobSelected() {
        return currentBlobSelected;
    }

    public void setCurrentBlobSelected(boolean currentBlobSelected) {
        this.currentBlobSelected = currentBlobSelected;
    }

    public String getSearchFilename() {
        return searchFilename;
    }

    public void setSearchFilename(String searchFilename) {
        this.searchFilename = searchFilename;
    }

    public String getSearchDescription() {
        return searchDescription;
    }

    public void setSearchDescription(String searchDescription) {
        this.searchDescription = searchDescription;
    }

    public LocalDateTime getSearchBefore() {
        return searchBefore;
    }

    public void setSearchBefore(LocalDateTime searchBefore) {
        this.searchBefore = searchBefore;
    }

    public LocalDateTime getSearchAfter() {
        return searchAfter;
    }

    public void setSearchAfter(LocalDateTime searchAfter) {
        this.searchAfter = searchAfter;
    }
    
    public java.util.Date getSearchBeforeUtil() {
        return convertDate(searchBefore);
    }

    public void setSearchBeforeUtil(java.util.Date searchBefore) {
        this.searchBefore = convertDate(searchBefore);
    }

    public java.util.Date getSearchAfterUtil() {
        return convertDate(searchAfter);
    }

    public void setSearchAfterUtil(java.util.Date searchAfter) {
        this.searchAfter = convertDate(searchAfter);
    }
    
}
