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

import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.BlobLight;
import com.tcvcog.tcvce.entities.BlobType;
import com.tcvcog.tcvce.entities.PageModeEnum;
import com.tcvcog.tcvce.integration.BlobIntegrator;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
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
     
    private PageModeEnum currentMode;
    private List<PageModeEnum> pageModes;
     
    private List<BlobLight> blobList;
    private Blob selectedBlob;
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
        pageModes = new ArrayList<>();
        pageModes.add(PageModeEnum.LOOKUP);
        pageModes.add(PageModeEnum.INSERT);
        pageModes.add(PageModeEnum.UPDATE);
        pageModes.add(PageModeEnum.REMOVE);
        
        setCurrentMode(PageModeEnum.LOOKUP);
        
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

    //check if current mode == Lookup
    public boolean getActiveLookupMode() {
        // hard-wired on since there's always a property loaded
        return PageModeEnum.LOOKUP.equals(currentMode);
    }

    //check if current mode == Lookup
    public boolean getActiveViewMode() {
        return PageModeEnum.VIEW.equals(currentMode) || PageModeEnum.LOOKUP.equals(currentMode);

    }

    //check if current mode == Insert
    public boolean getActiveInsertUpdateMode() {
        return PageModeEnum.INSERT.equals(currentMode) || PageModeEnum.UPDATE.equals(currentMode);
    }

    //check if current mode == Remove
    public boolean getActiveRemoveMode() {
        return PageModeEnum.REMOVE.equals(currentMode);
    }
    
    public PageModeEnum getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(PageModeEnum input) {
        if(input != null){
        currentMode = input;
        getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            this.currentMode.getTitle() + " Mode Selected", ""));
        }
    }

    public List<PageModeEnum> getPageModes() {
        return pageModes;
    }

    public void setPageModes(List<PageModeEnum> pageModes) {
        this.pageModes = pageModes;
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
