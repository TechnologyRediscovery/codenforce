/*
 * Copyright (C) 2018 Turtle Creek Valley Council of Governments, PA
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
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.BlobTypeException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.MetadataException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.BOb;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.BlobLight;
import com.tcvcog.tcvce.entities.BlobLinkEnum;
import com.tcvcog.tcvce.entities.BlobPool;
import com.tcvcog.tcvce.entities.BlobType;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.IFace_BlobHolder;
import com.tcvcog.tcvce.entities.Metadata;
import com.tcvcog.tcvce.entities.MetadataKey;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.integration.CEActionRequestIntegrator;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccInspectionIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import java.awt.image.BufferedImage;
import com.tcvcog.tcvce.integration.BlobIntegrator;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseId;
import jakarta.imageio.ImageIO;
import jakarta.imageio.ImageReader;
import jakarta.imageio.metadata.IIOMetadata;
import jakarta.imageio.stream.ImageInputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Business logic and coordination tools for photos and docs, called BLOBS, 
 * binary large objects
 * 
 * @author NADGIT & Ellen Bascomb of Apartment 31Y
 */
public class BlobCoordinator extends BackingBeanUtils implements Serializable {

    private final int GIGABYTE = 1000000000;

    private List<BlobType> blobTypeListMaster;
    
    public BlobCoordinator() {

    }
    
    
     @PostConstruct
    public void initBean() {
         System.out.println("BlobCoordinator.initBean(); assembling application tools");
        try {
            blobTypeListMaster = getBlobTypeListComplete();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }
    
    

    
    /**
     * Retrieval point for a BlobType
     * @param typeid
     * @return the BlobType all ready to roll; null if id = 0;
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public BlobType getBlobType(int typeid) throws IntegrationException{
        
        BlobIntegrator bi = getBlobIntegrator();
        if(typeid == 0){
            return null;
        }
        return bi.getBlobType(typeid);
        
        
    }
    
    /**
     * Updates the DB with BlobType bt
     * @param bt the BlobType to update
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void updateBlobType(BlobType bt) throws IntegrationException{
        BlobIntegrator bi = getBlobIntegrator();
        bi.updateBlobType(bt);
    }
    
    /**
     * Inserts BlobType bt into the DB
     * @param bt the BlobType to update
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void insertBlobType(BlobType bt) throws IntegrationException{
        BlobIntegrator bi = getBlobIntegrator();
        bi.insertBlobType(bt);
    }
    
    /**
     * Adds a deactivatedts to BlobType bt in the DB
     * @param bt the BlobType to update
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void deactivateBlobType(BlobType bt) throws IntegrationException{
        BlobIntegrator bi = getBlobIntegrator();
        bi.deactivateBlobType(bt);
    }
    
    /**
     * Extracts all BlobTypes from the DB
     * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public List<BlobType> getBlobTypeListComplete() throws IntegrationException{
        BlobIntegrator bi = getBlobIntegrator();
        List<Integer> idl = bi.getBlobTypeList();
        List<BlobType> typeList = new ArrayList<>();
        if(idl != null && !idl.isEmpty()){
            for(Integer i: idl){
                typeList.add(getBlobType(i));
            }
        }
        
        return typeList;
    }
    
    /**
     * Returns the bloblight of the default broadview photo for a parcel
     * @return 
     */
    public BlobLight getDefaultBroadviewPhoto() throws IntegrationException, BlobException{
       
        BlobLight defBV = getBlobLight(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                .getString("defaultbroadviewphotodocid")));
        return defBV;
        
    }
    

    /**
     * takes in a list of BlobLights and only returns those who are browser viewable (i.e. photos)
     * @param blobList
     * @return a list, perhaps containing a blob or more
     */
    public List<BlobLight> assembleBrowserViewableBlobs(List<BlobLight> blobList){
        List<BlobLight> blist = new ArrayList<>();
        if(blobList != null && !blobList.isEmpty()){
            for(BlobLight blob: blobList){
                if(blob.getType().isBrowserViewable()){
                    blist.add(blob);
                }
            }
        }
        return blist;
    }
    
    /**
     * Factory method for Blobs--byteless blobs!!! 
     * The caller will need to find some bytes somewhere and shove 'em on in
     * 
     * @param ua
     * @return skeleton
     * @throws IntegrationException 
     */
    public Blob generateBlobSkeleton(UserAuthorized ua) throws IntegrationException {
        Blob blob = new Blob();
        blob.setBlobMetadata(new Metadata());
        blob.setDescription("");
        blob.setCreatedTS(LocalDateTime.now());
        if (getSessionBean().getSessUser() != null) {
            blob.setBlobUploadedBy(ua);
        } else {
            UserCoordinator uc = getUserCoordinator();
            try {
                blob.setBlobUploadedBy(uc.auth_getPublicUserAuthorized());
            } catch (BObStatusException ex) {
                throw new IntegrationException(ex.getMessage());
            }
        }
        return blob;
    }

   

    
    /**
     * Official pathway for writing both the bytes to the DB
     * and using the BlobHolder's info to link those bytes to the
     * holder of those bytes
     * 
     * @param blob with bytes and ZERO for bytesID and photodocID
     * @param bh the container of the blob with not null LinkInfoEnum
     * @param ua
     * @param muni
     * 
     * @return the passed in blob 
     * @throws BObStatusException
     * @throws BlobTypeException
     * @throws IntegrationException
     * @throws IOException 
     * @throws com.tcvcog.tcvce.domain.BlobException 
     */
    public Blob insertBlobAndInsertMetadataAndLinkToParent( Blob blob, 
                                                            IFace_BlobHolder bh, 
                                                            UserAuthorized ua,
                                                            Municipality muni) 
            throws  BObStatusException, 
                    BlobTypeException, 
                    IntegrationException, 
                    IOException, 
                    BlobException{
        if(blob == null || bh == null || bh.getBlobLinkEnum() == null){
            throw new BObStatusException("Cannot process blob with null blob, parent, or info enum");
        }
        
        BlobIntegrator bi = getBlobIntegrator();
        blob.setBlobUploadedBy(ua);
        blob.setMuni(muni);
        // ******** AUDIT BLOB AND INSERT *************
        blob.setBytesID(bi.insertBlobBytes(auditAndPrepareBlobForStorage(blob)));
        // we have bytes ID set, so the photodoc table can point to it
        blob.setCreatedBy(ua);
        blob.setLastUpdatedBy(ua);
        blob.setPhotoDocID(bi.insertPhotoDoc(blob));
        // then connect the metadata record to the parent business object
        bi.linkBlobHolderToBlobMetadata(bh, blob);
                      
        return blob;
    }
    
    /**
     * Logic container for interrogating the blob we got from the 
     * upper bits and seeing what type it is via its file extension, 
     * setting the type object appropriately, and injecting 
     * default values if needed.
     * 
     * @param blob
     * @return the configured blob
     */
    private Blob auditAndPrepareBlobForStorage(Blob blob) 
                    throws  BlobException, 
                            IntegrationException, 
                            IOException, 
                            BlobTypeException{
         if (blob.getBytes()== null || blob.getBytes().length == 0) {
            throw new BlobException("You cannot upload a file without binary data");
        }
        
        //Test to see if the byte array is larger than a GIGABYTE
        // commented out on 18-MAR-2022 when officers said they could not upload over 10mb
//        if (blob.getBytes().length > GIGABYTE) {
//            throw new BlobException("You cannot upload a file larger than 1 gigabyte.");
//        }

        String filename = blob.getFilename();
        
        if(filename == null || filename.isEmpty()){
            throw new BlobException("You cannot upload a file without a filename.");
        }
        
        //First, let's find out what type of file this is.
        // note in codeNforce file extension does NOT include the trailing .
        String fileExtension = getFileExtension(blob.getFilename());

        //if the file extension is uppercase, we need to change it to lowercase.
        //This keeps the database standardized.
        
        String lowerCaseExt = fileExtension.toLowerCase();
        
        // This seems heavy and clunky, but this is a relatively rare operation
        // that is only done once on the blob's first INSERTs
        if(blobTypeListMaster == null){
            blobTypeListMaster = getBlobTypeListComplete();
        }
        
        if(lowerCaseExt != null && blobTypeListMaster != null && !blobTypeListMaster.isEmpty()){
            for(BlobType bt: blobTypeListMaster){
                if(bt.getFileExtensionsPermitted().contains(lowerCaseExt)){
                    System.out.println("BlobCoordinator.auditAndPrepareBlobForStorage | Assigning type " + bt.getTypeID() + " to filename " + blob.getFilename());
                    blob.setType(bt);
                    break;
                }
            }
            // if we make it here, we haven't assigned 
            if(blob.getType()== null){
                throw new BlobException("Incompatible file type, please upload a JPG, JPEG, GIF, PNG, or PDF.");
            }
        }
        return blob;
    }
    
    
    /**
     * Connects a blob holder to existing blobs selected from a pool of blobs
     * 
     * @param bh
     * @param blobList
     * @throws BObStatusException 
     */
    public void linkBlobHolderToBlobList(IFace_BlobHolder bh, List<BlobLight> blobList) throws BObStatusException, IntegrationException{
        if(bh == null || blobList == null || blobList.isEmpty()){
            throw new BObStatusException("Cannot link blobholder to list if null holder, list, or empty list");
        }
        
        BlobIntegrator bi = getBlobIntegrator();
        
        for(BlobLight bl: blobList){
            bi.linkBlobHolderToBlobMetadata(bh, bl);
        }
        
    }
    
  
    
    
    /**
     * Logic container for business rules related to blob metadata updates
     * @param bl
     * @param ua 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void updateBlobMetatdata(BlobLight bl, UserAuthorized ua) throws IntegrationException{
        BlobIntegrator bi = getBlobIntegrator();
        
        if(bl != null && ua != null){
            bl.setLastUpdatedBy(ua);
            bi.updateBlobLight(bl);
        }
    }
    
    /**
     * Sets the deactivated by and deactivatedts fields on the given blob light
     * @param bl
     * @param ua doing the deactivating
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.AuthorizationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public void deactivateBlobLightAndAllLinks(BlobLight bl, UserAuthorized ua) throws IntegrationException, AuthorizationException, BObStatusException{
        BlobIntegrator bi = getBlobIntegrator();
        
        if(bl != null && ua != null){
            if(bl.getCreatedBy() != null){
                if(bl.getCreatedBy().getUserID() == ua.getUserID() || ua.getKeyCard().isHasSysAdminPermissions()){
                    System.out.println("BlobCoordinator.deactivateBlobLightAndAllLinks | Permission granted to deac photodoc record");
                    bl.setLastUpdatedBy(ua);
                    bl.setDeactivatedBy(ua);
                    bl.setDeactivatedTS(LocalDateTime.now());
                    bi.updateBlobLight(bl);
                    deleteLinksToPhotoDocRecord(bl, BlobLinkEnum.PROPERTY);
                } else {
                    throw new AuthorizationException("cannot deactivate a blob unless you created that blob or have sys admin cred or better");
                }
            } else {
                throw new AuthorizationException("cannot deactivate a blob unless you created that blob has a creator - permissions issue!");
            }
        } else {
            throw new BObStatusException("cannot deactivate a blob with null blob light or user authorized");
        }
    }
    
    
    /**
     * Iterates over all the linking tables that could connect to this
     * given bloblight and actually deletes those entries.
     * This is unusual, in that in CNF we rarely delete records, usually we deactivate
     * them but blob linking at this level is rather abstract
     * 
     * @param bl to which all links should be removed
     * @param blenum if null, i'll delete records from ALL linking tables, otherwise I'll remove links
     * to the given bloblight in the linking table specified by the given enum
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public void deleteLinksToPhotoDocRecord(BlobLight bl, BlobLinkEnum blenum) throws IntegrationException, BObStatusException{
        BlobIntegrator bi = getBlobIntegrator();
        if(bl != null){
            if(blenum != null){
                List<BlobLinkEnum> blenumList = Arrays.asList(BlobLinkEnum.values());
                if(blenumList != null && !blenumList.isEmpty()){
                    for(BlobLinkEnum en: blenumList){
                        bi.removeLinkToBlobLightByBlobLinkEnum(bl, en);
                    }
                }
            } else {
                bi.removeLinkToBlobLightByBlobLinkEnum(bl, blenum);
            }
        }
    }
    
    
    /**
     * Updates a blob's filename.
     * Safe for BB use, as this checks the file extension and throws an error
     * if the file extension is wrong.
     * @param blob
     * @deprecated we'll never update a blob's filename! Change its title/description
     * on its metadata record in table photodoc
     * @throws IntegrationException
     * @throws IOException
     * @throws BlobTypeException if the supplied file extension is different than what we have in the DB
     * @throws com.tcvcog.tcvce.domain.BlobException
     */
    public void updateBlobFilename(BlobLight blob) 
            throws IntegrationException, 
            IOException,
            BlobTypeException,
            BlobException{
        
        //we must make sure that the file extension has not been changed, as
        //Changing it could break the file.
        
        BlobIntegrator bi = getBlobIntegrator();
        
        BlobLight originalBlob = bi.getPhotoBlobLightWithoutMetadata(blob.getPhotoDocID());
        
//        String newExtension = getFileExtension(blob.getFilename());
        
        String originalExtension = "";
        
//        if(originalBlob.getFilename() != null){
//            originalExtension = getFileExtension(originalBlob.getFilename());
//        } else{
//            //The system is probably automatically updating the filename
//            //But let's make sure the extension is the same as the file's type
//            originalExtension = getFileExtension(generateFilename(bi.getBlob(blob.getBytesID())));
//        }
//        
//        if(!newExtension.equals(originalExtension)){
//            throw new BlobTypeException("File extension of new filename is not the same as the file type");
//        }
        
        //If we reach here, the file extensions are equal, we may update the filename.
        bi.updateBlobFilename(blob);
        
    }
    
 
    
   /**
    * Primary pathway for getting the actual bytes assocaited with a given bloblight
    * which represents a record in the photodoc table
    * @param bl
    * @return the actual Blob with bytes in its belly!
    */
    public Blob getBlob(BlobLight bl){
        BlobIntegrator bi = getBlobIntegrator();
        return bi.getBlob(bl);
        
        
        
    }
    
    /**
     * Extracts a Blob from the DB given a bloblightID
     * The BlobLight knows where to find its own bytes
     * @param blobLightID
     * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     * @throws com.tcvcog.tcvce.domain.MetadataException 
     */
    public Blob getBlob(int blobLightID) throws IntegrationException, MetadataException{
        BlobIntegrator bi  = getBlobIntegrator();
        return bi.getBlob(bi.getBlobLight(blobLightID));
        
        
        
    }
    
    /**
     * Uses an existing BlobLight to make a blob by only grabbing the bytes
     * and attaching them.
     * @param input
     * @return
     * @throws IntegrationException
     */
    public Blob getBlobFromBlobLight(BlobLight input) throws IntegrationException {
        BlobIntegrator bi = getBlobIntegrator();

        Blob blob = new Blob(input);

//        blob.setBytes(bi.getBlobBytes(input.getBytesID()));

        return blob;
    }
    
    /**
     * A method for grabbing PhotoBlobLights that's safe:
     * if it encounters an entry that does not yet have a properly
     * populated metadata column, it strips the metadata and saves it
     * before returning the blob.
     * @param blobID
     * @return
     * @throws IntegrationException
     * @throws com.tcvcog.tcvce.domain.BlobException
     */
    public BlobLight getBlobLight(int blobID) throws IntegrationException, BlobException {
        
        BlobIntegrator bi = getBlobIntegrator();
        
        try {
            return bi.getBlobLight(blobID);
        } catch(MetadataException ex) {
            System.out.println("Metadata Exception!");
            System.out.println(ex);
            if(ex.isMapNullError()){
                //The metadata column isn't properly populated.
                //We'll grab the bytes, strip the metadata from them
                //And save them in the metadata column before fetching
                //The blob and returning it.

                //time to operate
                // TODO: deal with metadata
                
                //grab the BlobLight without metadata so we don't get the same error
                Blob patient = getBlobFromBlobLight(bi.getPhotoBlobLightWithoutMetadata(blobID));
                try {
                patient = stripImageMetadata(patient);

                //Should be all ready, let's update the bytes and the metadata

                bi.updateBlobBytes(patient);

//                bi.updateBlobMetadata(patient);
                
                } catch(IOException | BlobTypeException exTwo){
                    throw new BlobException(exTwo);
                }
            } else {
                throw new BlobException(ex);
            }
        }
        
        //We are now clear to return the blob
        return getBlobLight(blobID);
    }
    
  
    /**
     * Convenience method for getting a list of BlobLights in from a list of IDs
     * @param idList
     * @return
     * @throws IntegrationException
     * @throws BlobException 
     */
    public List<BlobLight> getBlobLightList(List<Integer> idList) throws IntegrationException, BlobException{
        
        // null check
        
        List<BlobLight> blobList = new ArrayList<>();
        
        for(int id : idList){
            if(id != 0){
                blobList.add(getBlobLight(id));
            }
        }
        return blobList;
    }
    
    /**
     * Fancy overload that will create a blob pool on the session bean
     * by building a sneaky instance of IFace_BlobHolder that only contains
     * Blobs that are in the ancestry of the given BlobHolder
     * @param bh from which to extract pool info
     * @return the pool with a BlobLight list for the taking.
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BlobException 
     */
    public BlobPool getUpstreamBlobPool( IFace_BlobHolder bh) throws BObStatusException, IntegrationException, BlobException{
        BlobIntegrator bi = getBlobIntegrator();
        BlobPool upstreamPool = null;
        if(bh.getBlobUpstreamPoolEnumPoolFeederID() != 0 && bh.getBlobUpstreamPoolEnum() != null){
            System.out.println("BlobCoordinator.getUpstreamBlobPool | creating pool for blob holder of type " + bh.getBlobUpstreamPoolEnum().getBlobLinkTableName());
             upstreamPool = new BlobPool(bh.getBlobUpstreamPoolEnumPoolFeederID(), bh.getBlobUpstreamPoolEnum());
             upstreamPool.setBlobList(getBlobLightList(upstreamPool));
        } else {
            throw new BObStatusException("The blobholder passed in does not have a blob pool!! No pool for you.");
        }
        return upstreamPool;
        
    }
    
    /**
     * Central retrieval point for use by all coordinators
     * assembling objects which implement our hallowed IFace_BlobHolder
     * 
     * Note that I ask the BlobHolder for its info enum to figure out
     * where to get the IDs and I even build the complete BlobLight list
     * for the client to inject directly into the BlobHolder
     * 
     * @param holder any implementer
     * @return the BlobLight list ready to be read by the UI for retrieval
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.BlobException
     */
    public List<BlobLight> getBlobLightList(IFace_BlobHolder holder) throws BObStatusException, IntegrationException, BlobException{
        if(holder == null || holder.getBlobLinkEnum() == null){
            throw new BObStatusException("Cannot get blob list with null input or null info enum");
        }
        
        BlobIntegrator bi = getBlobIntegrator();
        return getBlobLightList(bi.getBlobLightIDList(holder));
        
    }
    
    /**
     * Deletes an image from the database, but only if it is not connected to any BObs.
     * If, after deleting the photodoc entry, the bytes are not connected to any
     * other photodoc, the bytes themselves are also deleted.
     * @param blob
     * @throws IntegrationException
     * @throws EventException
     * @throws AuthorizationException
     * @throws ViolationException
     * @throws BObStatusException
     * @throws BlobException 
     */
    public void deletePhotoBlob(BlobLight blob) 
            throws IntegrationException, 
            EventException, 
            AuthorizationException, 
            ViolationException, 
            BObStatusException, 
            BlobException {
        
        BlobIntegrator bi = getBlobIntegrator();
        
        //First we have to make sure that no objects are attached to this blob
        List<BOb> connectedObjects = getAttachedObjects(blob);
        
        if(!connectedObjects.isEmpty()){
            throw new BlobException("The coordinator attempted to remove a blob that is currently connected to other objects.");
        }
        
        //The blob isn't attached to anything, let's delete the blob from the photodoc table
        bi.deletePhotoBlob(blob);
        
        //Let's see if this blob is still attached to other photodoc rows
        List<Integer> connectedPhotoDocs = bi.getPhotoBlobsFromBytesID(blob.getBytesID());
        
        if(connectedPhotoDocs.isEmpty()){
            //No rows are referencing this file, let's delete the bytes too
            bi.deleteBytes(blob.getBytesID());
        }
        
    }
    
        
   
    
    
    

    /**
     * A method that removes all metadata from an image blob's bytes and puts
     * them into its Metadata field. Should always be called before saving an
     * image file to the database.
     *
     * @param input
     * @return The blob that was put into it, stripped of metadata
     * @throws java.io.IOException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.BlobTypeException
     * @throws com.tcvcog.tcvce.domain.BlobException
     */
    public Blob stripImageMetadata(Blob input) 
            throws IOException, 
            IntegrationException,
            BlobTypeException,
            BlobException {

        if(input.getFilename() == null){
            
            //No file name, let's generate one and save it to the database.
            input.setFilename(generateFilename(input.getBytes()));
            
            updateBlobFilename(input);
            
        }
        //First, let's find out what type of file this is.
        String fileExtension = getFileExtension(input.getFilename());
        
        ByteArrayInputStream bis = new ByteArrayInputStream(input.getBytes());

        //Extract metadata and place it in the blob
        
        //First we need to get an image reader.
        //The file extension is required because the default getImageReaders()
        //method guesses what file type the bytes are, and sometimes it guesses wrong.
        //Using the getImageReadersByFormatName() ensures we get the right one.
        Iterator<ImageReader> inReaders = ImageIO.getImageReadersByFormatName(fileExtension);
        
        ImageInputStream iis = ImageIO.createImageInputStream(bis);
        
        ImageReader reader = inReaders.next();
        
        reader.setInput(iis, false, false);
        
        IIOMetadata imgMeta = reader.getImageMetadata(0);
        
        String[] names = imgMeta.getMetadataFormatNames();
        
        Metadata blobMeta = new Metadata();
        
        //Go through each different metadata format and put it into the blobMeta map
        for(int i = 0; i < names.length; i++){
            Node node = imgMeta.getAsTree(names[i]);
            blobMeta = extractMetadataFromNode(node, blobMeta);
        }
        
        input.setBlobMetadata(blobMeta);
        
        //Strip the metadata by reading out only the image data and writing it back
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        BufferedImage temp = reader.read(0);
        
        ImageIO.write(temp, fileExtension, baos);
        
        //These bytes should only be the image file itself, without the metadata. But that in the bytes field.
        
        input.setBytes(baos.toByteArray());
        
        return input;
    }
    
   
    /**
     * Deletes a PDF from the database, but only if it is not connected to any BObs.
     * If, after deleting the pdfdoc entry, the bytes are not connected to any
     * other pdfdoc, the bytes themselves are also deleted.
     * @param blob
     * @throws IntegrationException
     * @throws EventException
     * @throws AuthorizationException
     * @throws ViolationException
     * @throws BObStatusException
     * @throws BlobException 
     */
    public void deletePDFBlob(BlobLight blob) 
            throws IntegrationException, 
            EventException, 
            AuthorizationException, 
            ViolationException, 
            BObStatusException, 
            BlobException {
        
        BlobIntegrator bi = getBlobIntegrator();
        
        //First we have to make sure that no objects are attached to this blob
        List<BOb> connectedObjects = getAttachedObjects(blob);
        
        if(!connectedObjects.isEmpty()){
            throw new BlobException("The coordinator attempted to remove a blob that is currently connected to other objects.");
        }
        
        //The blob isn't attached to anything, let's delete the blob from the photodoc table
        bi.deletePDFBlob(blob.getPhotoDocID());
        
        //Let's see if this blob is still attached to other photodoc rows
        List<Integer> connectedPDFDocs = bi.getPDFBlobsFromBytesID(blob.getBytesID());
        
        if(connectedPDFDocs.isEmpty()){
            //No rows are referencing this file, let's delete the bytes too
            bi.deleteBytes(blob.getBytesID());
        }
        
    }

    /**
     * A method that removes all metadata from a PDF blob's bytes and puts
     * them into its Metadata field. Should always be called before saving a
     * PDF file to the database.
     * @param input
     * @return The blob that was put into it, stripped of metadata
     * @throws java.io.IOException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.BlobTypeException
     * @throws com.tcvcog.tcvce.domain.BlobException
     */
    public Blob stripPDFMetadata(Blob input) 
            throws IOException, 
            IntegrationException,
            BlobTypeException,
            BlobException {

        if(input.getFilename() == null){
            
            //No file name, let's generate one and save it to the database.
            input.setFilename(generateFilename(input.getBytes()));
            
            updateBlobFilename(input);
            
        }

        //Extract metadata and place it in the blob
        
        //Step 1: put bytes in a document.
        
        PDDocument doc = PDDocument.load(input.getBytes());
        
        //Step 2: grab the metadata and then erase it from the document
        
        Metadata blobMeta = new Metadata();
        
        PDDocumentInformation docInfo = doc.getDocumentInformation();
        
        blobMeta.setProperty(new MetadataKey("Author"), docInfo.getAuthor());
        
        docInfo.setAuthor("");
        
        blobMeta.setProperty(new MetadataKey("Title"), docInfo.getTitle());
        
        docInfo.setTitle("");
        
        blobMeta.setProperty(new MetadataKey("Subject"), docInfo.getSubject());
        
        docInfo.setSubject("");
        
        blobMeta.setProperty(new MetadataKey("Keywords"), docInfo.getKeywords());
        
        docInfo.setKeywords("");
        
        blobMeta.setProperty(new MetadataKey("Creator"), docInfo.getCreator());
        
        docInfo.setCreator("");
        
        blobMeta.setProperty(new MetadataKey("Producer"), docInfo.getProducer());
        
        docInfo.setProducer("");
        
        if(docInfo.getCreationDate() != null){
            blobMeta.setProperty(new MetadataKey("CreationDate"), docInfo.getCreationDate().getTime().toString());
        
            docInfo.setCreationDate(null);
        
        }
        
        if(docInfo.getCreationDate() != null){
            blobMeta.setProperty(new MetadataKey("ModificationDate"), docInfo.getModificationDate().getTime().toString());
        
            docInfo.setModificationDate(null);
        }
        
        input.setBlobMetadata(blobMeta);
        
        //put the document, with the now erased metadata, back into the bytes field
        
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        doc.save(output);
        
        doc.close();
        
        input.setBytes(output.toByteArray());
        
        return input;
    }
    
    /**
     * Takes a Node of image metadata and extracts its values and keys into the Metadata
     * object. Once it's done extracting all the information it needs, it tosses
     * the Metadata object back.
     * @param node The node to extract from
     * @param meta The Metadata object to fill with data.
     * @return 
     */
    private Metadata extractMetadataFromNode(Node node, Metadata meta){
        //Check for attributes on the parent node.
        
        NamedNodeMap map = node.getAttributes();
        if(map != null){
            for(int index = 0; index < map.getLength(); index++){
                //extract the value of each attribute.
                Node attr = map.item(index);
                MetadataKey key = new MetadataKey(attr.getNodeName());
                meta.setProperty(key, attr.getNodeValue());
            }
        } /* else {
            //If we ever get to the point where we would want to keep track of metadata categories
            //keep in mind that categories are attributeless but have children. 
            //So, grab the node name from attributeless nodes.
        }
        */
        
        //Extract metadata from each child, if one exists.
        Node child = node.getFirstChild();
        
        while(child != null){
            meta = extractMetadataFromNode(child, meta);
            child = child.getNextSibling();
        }
        
        return meta;
    }
    
    /**
     * Accepts a filename and returns only the file extension.
     * E.g. "image.jpg" -> "jpg"
     * @param filename
     * @return 
     */
    public static String getFileExtension(String filename) {
        //split on every dot
        String[] fileNameTokens = filename.split("\\.");

        //the last token will contain our file type extension
        return fileNameTokens[fileNameTokens.length - 1];
        
    }
    
    /**
     * Takes the bytes of an untitled file, finds its file type,
     * and returns a title for that file.
     * @param bytes
     * @return 
     * @throws java.io.IOException 
     */
    private String generateFilename(byte[] bytes) throws IOException {
        
        InputStream is = new ByteArrayInputStream(bytes);
        
        
        String fileType = URLConnection.guessContentTypeFromStream(is);
        
        //guessContentType will give us a string like "image/png", so let's grab the string after the "/"
        String extension = fileType.substring(fileType.indexOf("/")+1);
        extension = extension.trim();
        //let's add a random number at the end of untitled
        //Makes it a little more easily identifiable than just "untitled".
        String filename = "untitled" + new Random().nextInt(10000) + "." + extension;
        
        return filename;
    }
    
    /**
     * Returns a list of all the objects associated with the given blob
     * @param blob
     * @return
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.EventException 
     * @throws com.tcvcog.tcvce.domain.AuthorizationException 
     * @throws com.tcvcog.tcvce.domain.ViolationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public List<BOb> getAttachedObjects(BlobLight blob) 
        throws IntegrationException,
            EventException, 
            AuthorizationException, 
            ViolationException, 
            BObStatusException{

        BlobIntegrator bi = getBlobIntegrator();

        List<BOb> objectList = new ArrayList<>();

        int blobID = blob.getPhotoDocID();
        
        // TODO: Fix me post blob interfacification
//        if (blob.getType().getTypeEnum() == BlobTypeEnum.PHOTO) {
        if (true) {

            CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
//            
//            for (Integer id : bi.requestsAttachedToPhoto(blobID)) {
//
//                objectList.add(ceari.getActionRequestByRequestID(id));
//                
//            }
//            
//            CaseIntegrator ci = getCaseIntegrator();
//            
//            for (Integer id : bi.violationsAttachedToPhoto(blobID)) {
//
//                objectList.add(ci.getCodeViolation(id));
//                
//            }
//            
//            MunicipalityIntegrator mi = getMunicipalityIntegrator();
//            
//            for(Integer id : bi.munisAttachedToPhoto(blobID)){
//                
//                objectList.add(mi.getMuni(id));
//                
//            }
//            
//            OccInspectionIntegrator occi = getOccInspectionIntegrator();
//            
//            for(Integer id : bi.elementsAttachedToPhoto(blobID)){
//                
//                objectList.add(occi.getInspectedSpaceElement(id));
//                
//            }
//            
//            OccupancyIntegrator oi = getOccupancyIntegrator();
//            
//            for(Integer id : bi.occPeriodsAttachedToPhoto(blobID)){
//                
//                objectList.add(oi.getOccPeriod(id));
//                
//            }
            
        } 

        return objectList;
        
    }
    
    /**
     * TEMPORARY SEARCH METHOD FOR BLOBS.
     * Should search all blob tables, add their entries to one list, and return it.
     * TODO: Add pdf search
     * @param filename
     * @param description
     * @param before
     * @param after
     * @param municode
     * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BlobException 
     */
    public List<BlobLight> searchBlobs(String filename, String description, LocalDateTime before, LocalDateTime after, int municode) 
            throws IntegrationException, BlobException{
        
        BlobIntegrator bi = getBlobIntegrator();
        
        //For GIGO and optimization purposes, throw out the filename and description
        //if they don't contain non-whitespace
        if(!filename.matches(".*\\S.*")){
            filename = null;
        }
        
        if(!description.matches(".*\\S.*")){
            description = null;
        }
        
        List<Integer> idList = new ArrayList<>();
        
        idList.addAll(bi.searchPhotoBlobs(filename, description, before, after, municode));
        
        idList.addAll(bi.searchPDFBlobs(filename, description, before, after, municode));
        
        List<BlobLight> blobList = new ArrayList<>();
        
        for(Integer id : idList){
            if(id != 0){
                BlobLight result = getBlobLight(id);
                if(result != null) {

                    blobList.add(result);

                }
            }
        }
        
        for(Integer id : idList){
            
//            BlobLight result = getPDFBlobLight(id);
            
//            if(result != null) {
//            
//                blobList.add(result);
//            
//            }
            
        }
        
        return blobList;
        
    }
    
}
