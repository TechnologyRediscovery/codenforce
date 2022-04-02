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
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.BlobTypeException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.BOb;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.BlobLight;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.MetadataKey;
import com.tcvcog.tcvce.entities.MetadataUI;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.occupancy.OccInspectedSpace;
import com.tcvcog.tcvce.entities.occupancy.OccInspectedSpaceElement;
import com.tcvcog.tcvce.entities.occupancy.FieldInspection;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.integration.BlobIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccInspectionIntegrator;
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

/**
 * Backing Bean for Blob Management
 * 
 * @author noah, Ellen, and NADGIT
 */
public class ManageBlobBB extends BackingBeanUtils implements Serializable{
    
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
    
    //Lists of connected objects
    
    private List<CEActionRequest> connectedRequests;
    private List<CodeViolation> connectedViolations;
    private List<Municipality> connectedMunis;
    private List<OccInspectedSpaceElement> connectedElements;
    private List<OccPeriod> connectedPeriods;
    private List<Property> connectedProperties;
    
    private List<MetadataUI> metaList; //A list of metadata tags for users to view
    
    //Files for updating blobs.
    private String newFilename;
    private String newDescription;
    
    /**
     * load all blobs uploaded in the past month into memory
     * this is temp for testing, to be replaced by proper query search implementation
     */
    @PostConstruct
    public void initBean() {

        setCurrentMode("Info");

//        try {
//                
//                BlobIntegrator bi = getBlobIntegrator();
//                BlobCoordinator bc = getBlobCoordinator();
//                
//                List<Integer> blobIDs = bi.getRecentPhotoBlobs();
//                blobList = new ArrayList<>();
//                
//                for (int idnum : blobIDs) {
//                    blobList.add(bc.getBlobLight(idnum));
//                }
//        } catch (IntegrationException | BlobException ex) {
//            System.out.println("ManageBlobBB.initBean | ERROR: " + ex);
//        }
    }
    
    /**
     * Reloads the blobs after changes have been made.
     * Will also return a user to the blob and mode they
     * were editing before if possible.
     */
    public void reloadBlobs() {

        String tempMode = getCurrentMode();

        int currentBlobID = selectedBlob.getPhotoDocID();

        if (searchFilename == null
                && searchDescription == null
                && searchBefore == null
                && searchAfter == null) {
            //No search, just reload recent blobs.
            initBean();
        } else {
            executeQuery();
        }

        if (currentBlobID == 0) {
            //Blob was deleted, deselect blob and don't search
            selectedBlob = null;
            metaList = new ArrayList<>();
            currentBlobSelected = false;
            setCurrentMode("Info");
            reloadConnections();
        } else {
            for (BlobLight b : blobList) {
                if (b.getPhotoDocID() == currentBlobID) {
                    //This is the blob the user had selected, reload it.
                    selectBlob(b);
                    setCurrentMode(tempMode);
                }
            }
        }
    }
    
    /**
     * Reloads only the connections of the selected blob.
     * If no blob is selected, it just wipes the connection lists.
     */
    public void reloadConnections() {

        connectedRequests = new ArrayList<>();
        connectedViolations = new ArrayList<>();
        connectedMunis = new ArrayList<>();
        connectedElements = new ArrayList<>();
        connectedPeriods = new ArrayList<>();
        connectedProperties = new ArrayList<>();

        if (selectedBlob != null) {

            BlobCoordinator bc = getBlobCoordinator();

            try {
                for (BOb object : bc.getAttachedObjects(selectedBlob)) {

                    //Check to see what class the object is
                    String className = object.getClass().getSimpleName();

                    switch (className) {
                        case "CEActionRequest":
                            connectedRequests.add((CEActionRequest) object);
                            break;

                        case "CodeViolation":
                            connectedViolations.add((CodeViolation) object);
                            break;

                        case "Municipality":
                            connectedMunis.add((Municipality) object);
                            break;

                        case "OccInspectedSpaceElement":
                            connectedElements.add((OccInspectedSpaceElement) object);
                            break;

                        case "OccPeriod":
                            connectedPeriods.add((OccPeriod) object);
                            break;
                    }

                }
            } catch (AuthorizationException
                    | BObStatusException
                    | EventException
                    | ViolationException
                    | IntegrationException ex) {
                System.out.println("manageBlobBB.reloadConnections() | ERROR: " + ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "An error occurred while trying load file connections!", ""));
            }
        }
    }
    
    /**
     * Listener for user requests to 
     * @param blob 
     */
    public void selectBlob(BlobLight blob) {

        newFilename = "";
        
        newDescription = "";
        
        selectedBlob = blob;

        currentBlobSelected = true;

        //We have to load all the metadata properties into the metadataUI list
        //So our users can sort it alphabetically, etc.
        
        metaList = new ArrayList<>();
        
        for(MetadataKey key : selectedBlob.getBlobMetadata().getPropertiesList()){
            MetadataUI skeleton = new MetadataUI(
                    key.getLabel(), 
                    key.getKey(), 
                    selectedBlob.getBlobMetadata().getProperty(key));
            
            metaList.add(skeleton);
            
        }
        
        reloadConnections();
        
    }
    
    /**
     * TODO: Fix blob manager
     * @param blight 
     */
    public void downloadBlob(BlobLight blight){
        selectedBlob = blight;
        
//        downloadSelectedBlob();
        
    }
    
   

    public void executeQuery(){
        BlobCoordinator bc = getBlobCoordinator();
        
        int municode = getSessionBean().getSessMuni().getMuniCode();
        
        try{
            blobList = bc.searchBlobs(searchFilename, searchDescription, searchBefore, searchAfter, municode);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Found " + blobList.size() + " files matching your criteria!", ""));
        } catch(IntegrationException | BlobException ex){
            System.out.println("manageBlobBB.executeQuery() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occurred while trying to search for files!", ""));
        }
    }
    
    public void deleteSelectedBlob() {

        BlobCoordinator bc = getBlobCoordinator();
            try {

                // TODO: Figureo out how to do custom stuff by type!! Do I need an enum 
                // after all???
                if (selectedBlob.getType().getTypeID() == 222222222) {
                    bc.deletePhotoBlob(selectedBlob);
                } else if (selectedBlob.getType().getTypeID() == 3433333) {
                    bc.deletePDFBlob(selectedBlob);
                }
                
                

                //Setting blobID to 0 tells the reloadBlobs() method
                //not to search for the blob after reloading.
                selectedBlob.setPhotoDocID(0);
                
                reloadBlobs();
                
            } catch (IntegrationException 
                    | EventException 
                    | AuthorizationException 
                    | BObStatusException 
                    | ViolationException ex) {
                System.out.println("manageBlobBB.deleteSelectedBlob | ERROR: " + ex);
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Something went wrong when trying to delete the selected file!", ""));
            } catch (BlobException ex){
                System.out.println("manageBlobBB.deleteSelectedBlob | ERROR: " + ex);
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Something went wrong when trying to delete the selected file! "
                                    + "Make sure that the file is not connected to any "
                                    + "other objects in the system before trying to delete it", ""));
            }
    }
    
    /**
     * @deprecated we don't update filenames
     */
    public void updateBlobFilename(){
        
        BlobCoordinator bc = getBlobCoordinator();
        
//        String originalName = selectedBlob.getFilename();
//        
//        try{
//            selectedBlob.setFilename(newFilename);
//            
//            bc.updateBlobFilename(selectedBlob);
//            
//            reloadBlobs();
//            
//            getFacesContext().addMessage(null,
//                    new FacesMessage(FacesMessage.SEVERITY_INFO,
//                            "Successfully updated filename!", ""));
//        } catch(IOException | IntegrationException | BlobException  ex){
//            //Rollback the filename
//            selectedBlob.setFilename(originalName);
//            System.out.println("ManageBlobBB.updateBlobFilename() | ERROR: " + ex);
//            getFacesContext().addMessage(null,
//                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
//                            "An error occurred while trying to update the filename!", ""));
//        } catch(BlobTypeException ex){
//            //Rollback the filename
//            selectedBlob.setFilename(originalName);
//            System.out.println("ManageBlobBB.updateBlobFilename() | ERROR: " + ex);
//            getFacesContext().addMessage(null,
//                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
//                            "Please end the file name with the file extension [" +
//                            BlobCoordinator.getFileExtension(originalName) +"]", ""));
//        }
        
    }
    
    public void onBlobUpdateCommitButtonChange(){
        
        BlobCoordinator bc = getBlobCoordinator();
        
        try{
//            selectedBlob.setDescription(newDescription);
            
            bc.updateBlobMetatdata(selectedBlob, getSessionBean().getSessUser());
            
//            reloadBlobs();
            
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully updated blob title and description!", ""));
        } catch(IntegrationException ex){
            System.out.println("manageBlobBB.updateBlobDescription() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occurred while trying to update the description!", ""));
        }
        
    }
    
    /**
     * @return the selectedBlob
     */
    public BlobLight getSelectedBlob() {
        return selectedBlob;
    }

    /**
     * Set the selected property on the session bean and go to propertySearchProfile.xhtml
     * @param input
     * @return 
     */
    public String goToProperty(Property input){
        
        getSessionBean().setSessProperty(input);
        
        return "propertyInfo";
    }
    
    /**
     * Set the selected CEAR on the session bean and go to ceActionRequests.xhtml
     * @param input
     * @return 
     */
    public String goToRequest(CEActionRequest input){
        
        getSessionBean().setSessCEAR(input);
        
        return "cEActionRequests";
    }
    
    /**
     * Set the selected violation on the session bean and go to ceCaseViolations.xhtml
     * @param input
     * @return 
     */
    public String goToCodeViolation(CodeViolation input){
        
        getSessionBean().setSessCodeViolation(input);
        
        return "ceCaseViolations";
    }
    
    /**
     * Set the OccPeriod associated with the selected OccInspectedSpaceElement 
     * on the session bean and go to occPeriodInspections.xhtml
     * @param input
     * @return 
     */
    public String goToInspectedSpaceElement(OccInspectedSpaceElement input) {

        OccInspectionIntegrator oii = getOccInspectionIntegrator();
        OccupancyCoordinator oc = getOccupancyCoordinator();

        try {
            OccInspectedSpace space = oii.getInspectedSpace(input.getInspectedSpaceID());

            FieldInspection ins = oii.getOccInspection(space.getInspectionID());

            getSessionBean().setSessOccPeriodFromPeriodBase(oc.getOccPeriod(ins.getOccPeriodID()));
            return "occInspectionAdd";
        } catch (IntegrationException ex) {
            System.out.println("manageBlobBB.goToInspectedSpaceElement() | ERROR: " + ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occurred while redirecting you to the relevant occupancy inspection!", ""));
            return "";
        }
    }
    
    /**
     * Set the selected OccPeriod on the session bean and go to occPeriodWorkflow.xhtml
     * @param input
     * @return 
     */
    public String goToOccPeriod(OccPeriod input){
        
        getSessionBean().setSessOccPeriodFromPeriodBase(input);
        
        return "occPeriodWorkflow";
    }
   
    
   
    
    /**
     * Primefaces can't compare enums in EL. So, this translates the type to
     * an integer value so it can make sure to render the correct elements.
     * // TODO: Update for blobs22
     * @return 
     */
    public int getSelectedBlobType(){
//        switch(selectedBlob.getType().getTypeEnum()){
//            case PHOTO:
//                return 0;  
//            
//            case PDF:
//                return 1;
//            
//            default:
//                //Invalid type
//                return -1;
//        }
        return 0;
        
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

    /**
     * Checks to see if all connection lists are empty.
     * Controls if the "Delete blob" is enabled.
     * @return 
     */
    public boolean isAllConnectionsEmpty() {
        return  connectedElements.isEmpty() 
                && connectedMunis.isEmpty() 
                && connectedPeriods.isEmpty() 
                && connectedProperties.isEmpty()
                && connectedRequests.isEmpty()
                && connectedViolations.isEmpty();
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

    public List<CEActionRequest> getConnectedRequests() {
        return connectedRequests;
    }

    public void setConnectedRequests(List<CEActionRequest> connectedRequests) {
        this.connectedRequests = connectedRequests;
    }

    public List<CodeViolation> getConnectedViolations() {
        return connectedViolations;
    }

    public void setConnectedViolations(List<CodeViolation> connectedViolations) {
        this.connectedViolations = connectedViolations;
    }

    public List<Municipality> getConnectedMunis() {
        return connectedMunis;
    }

    public void setConnectedMunis(List<Municipality> connectedMunis) {
        this.connectedMunis = connectedMunis;
    }

    public List<OccInspectedSpaceElement> getConnectedElements() {
        return connectedElements;
    }

    public void setConnectedElements(List<OccInspectedSpaceElement> connectedElements) {
        this.connectedElements = connectedElements;
    }

    public List<OccPeriod> getConnectedPeriods() {
        return connectedPeriods;
    }

    public void setConnectedPeriods(List<OccPeriod> connectedPeriods) {
        this.connectedPeriods = connectedPeriods;
    }

    public List<Property> getConnectedProperties() {
        return connectedProperties;
    }

    public void setConnectedProperties(List<Property> connectedProperties) {
        this.connectedProperties = connectedProperties;
    }

    public String getNewFilename() {
        return newFilename;
    }

    public void setNewFilename(String newFilename) {
        this.newFilename = newFilename;
    }

    public String getNewDescription() {
        return newDescription;
    }

    public void setNewDescription(String newDescription) {
        this.newDescription = newDescription;
    }

    public List<MetadataUI> getMetaList() {
        return metaList;
    }

    public void setMetaList(List<MetadataUI> metaList) {
        this.metaList = metaList;
    }
    
}