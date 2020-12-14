/*
 * Copyright (C) 2018 Turtle Creek Valley
Council of Governments, PA
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
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.BOb;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.BlobLight;
import com.tcvcog.tcvce.entities.BlobType;
import com.tcvcog.tcvce.entities.Metadata;
import com.tcvcog.tcvce.entities.MetadataKey;
import com.tcvcog.tcvce.integration.BlobIntegrator;
import com.tcvcog.tcvce.integration.CEActionRequestIntegrator;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccInspectionIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import java.awt.image.BufferedImage;
import com.tcvcog.tcvce.entities.PrintStyle;
import com.tcvcog.tcvce.integration.BlobIntegrator;
import com.tcvcog.tcvce.integration.CaseIntegrator;
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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author noah
 */
public class BlobCoordinator extends BackingBeanUtils implements Serializable {

    private final StreamedContent image = new DefaultStreamedContent();
    private final int GIGABYTE = 1000000000;

    public BlobCoordinator() {

    }

    public Blob getNewBlob() throws IntegrationException {
        Blob blob = new Blob();
        blob.setBlobMetadata(new Metadata());
        blob.setDescription("No description.");
        blob.setTimestamp(LocalDateTime.now());
        if (getSessionBean().getSessUser() != null) {
            blob.setUploadPersonID(getSessionBean().getSessUser().getPersonID());
        } else {
            UserCoordinator uc = getUserCoordinator();
            blob.setUploadPersonID(uc.auth_getPublicUserAuthorized().getUserID());
        }
        return blob;
    }

    /**
     * The BlobCoordinator attempts to automatically retrieve an image for the
     * interface.
     *
     * @return
     * @throws BlobTypeException
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    public StreamedContent getImage() throws BlobTypeException, IOException, ClassNotFoundException {
        // should use EL to verify blob type,  but this will check it anyway
        FacesContext context = FacesContext.getCurrentInstance();
        DefaultStreamedContent sc = null;

        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            return image;
        } else {
            BlobIntegrator bi = getBlobIntegrator();
            
            //Get the blob ID from the Faces context
            int blobID = Integer.parseInt(context.getExternalContext().getRequestParameterMap().get("blobID"));
            System.out.println("BlobCoordinator.getImage: image ID " + blobID);
            try {
                BlobLight blob = getPhotoBlobLight(blobID);
                if (null == blob.getType()) {
                    throw new BlobTypeException("BlobType is null.");
                } else {
                    switch (blob.getType()) {
                        case PHOTO:
                            sc = new DefaultStreamedContent(new ByteArrayInputStream(bi.getBlobBytes(blob.getBytesID())));
                            break;
                        case PDF:
                            sc = new DefaultStreamedContent(new FileInputStream(new File("/home/noah/Documents/COG Project/codeconnect/src/main/webapp/images/pdf-icon.png")));
                            break;
                        default:
                            throw new BlobTypeException("Attempted to display incompatible BLOB type. ");
                    }
                }
            } catch (IntegrationException ex) {
                System.out.println(ex);
                System.out.println("BlobCoordinator.getImage | " + ex);
            } catch (FileNotFoundException ex) {
                System.out.println(ex);
                System.out.println("BlobCoordinator.getImage | could not find pdf-icon.png ");
            }
            return sc;
        }
    }

    /**
     * Validates blobs and prepares them for storage, whether they are photos
     * or documents.
     * TODO: make this method throw exception if blob is corrupted
     * @param blob
     * @return
     * @throws BlobException
     * @throws IntegrationException
     * @throws IOException 
     * @throws java.lang.ClassNotFoundException 
     */
    public Blob storeBlob(Blob blob) throws BlobException, IntegrationException, IOException, NoSuchElementException, ClassNotFoundException {
        //Test to see if the byte array is larger than a GIGABYTE
        if (blob.getBytes().length > GIGABYTE) {
            throw new BlobException("You cannot upload a file larger than 1 gigabyte.");
        }

        //First, let's find out what type of file this is.
        String fileExtension = getFileExtension(blob.getFilename());

        //if the file extension is uppercase, we need to change it to lowercase.
        //This keeps the database standardized.
        
        String lowerCaseExt = fileExtension.toLowerCase();
        
        if(!fileExtension.equals(lowerCaseExt)) {
            
            String filename = blob.getFilename();
            blob.setFilename(filename.replace("." + fileExtension, "." + lowerCaseExt));

            fileExtension = lowerCaseExt;
        }
        
        
        if (fileExtension.equals("jpg")
                || fileExtension.equals("jpeg")
                || fileExtension.equals("gif")
                || fileExtension.equals("png")) {
            
            blob.setType(BlobType.PHOTO);
            
            blob = stripImageMetadata(blob);
            return getBlobIntegrator().storePhotoBlob(blob);
            
        } else if (fileExtension.contains("pdf")) {
            
            blob.setType(BlobType.PDF);
            
            //No PDF methods yet!
            //TODO: Strip metadata from original file and save it in the Metadata dictionary
            return null;
            
        } else {
            //Incorrect file type
            throw new BlobException("Incompatible file type, please upload a JPG, JPEG, GIF, PNG, or PDF.");
        }

    }
    
    /**
     * Updates a blob's filename.
     * Safe for BB use, as this checks the file extension and throws an error
     * if the file extension is wrong.
     * @param blob
     * @throws IntegrationException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws BlobTypeException if the supplied file extension is different than what we have in the DB
     */
    public void updateBlobFilename(BlobLight blob) 
            throws IntegrationException, 
            IOException, 
            ClassNotFoundException, 
            BlobTypeException{
        
        //we must make sure that the file extension has not been changed, as
        //Changing it could break the file.
        
        BlobIntegrator bi = getBlobIntegrator();
        
        BlobLight originalBlob = getPhotoBlobLight(blob.getBlobID());
        
        String newExtension = getFileExtension(blob.getFilename());
        
        String originalExtension = "";
        
        if(originalBlob.getFilename() != null){
            originalExtension = getFileExtension(originalBlob.getFilename());
        } else{
            //The system is probably automatically updating the filename
            //But let's make sure the extension is the same as the file's type
            originalExtension = getFileExtension(generateFilename(bi.getBlobBytes(blob.getBytesID())));
        }
        
        if(!newExtension.equals(originalExtension)){
            throw new BlobTypeException("File extension of new filename is not the same as the file type");
        }
        
        //If we reach here, the file extensions are equal, we may update the filename.
        bi.updatePhotoBlobFilename(blob);
        
    }

    public Blob getPhotoBlob(int blobID) throws IntegrationException, IOException, ClassNotFoundException, NoSuchElementException, BlobTypeException {
        BlobIntegrator bi = getBlobIntegrator();

        Blob blob = new Blob(getPhotoBlobLight(blobID));

        blob.setBytes(bi.getBlobBytes(blob.getBytesID()));

        return blob;
    }
    
    /**
     * Uses an existing BlobLight to make a blob by only grabbing the bytes
     * and attaching them.
     * @param input
     * @return
     * @throws IntegrationException
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public Blob getPhotoBlob(BlobLight input) throws IntegrationException, IOException, ClassNotFoundException {
        BlobIntegrator bi = getBlobIntegrator();

        Blob blob = new Blob(input);

        blob.setBytes(bi.getBlobBytes(input.getBytesID()));

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
     * @throws IOException
     * @throws ClassNotFoundException 
     * @throws com.tcvcog.tcvce.domain.BlobTypeException 
     */
    public BlobLight getPhotoBlobLight(int blobID) throws IntegrationException, IOException, ClassNotFoundException, NoSuchElementException, BlobTypeException{
        
        BlobIntegrator bi = getBlobIntegrator();
        
        try {
        return bi.getPhotoBlobLight(blobID);
        } catch(BlobException ex) {
            //The metadata column isn't properly populated.
            //We'll grab the bytes, strip the metadata from them
            //And save them in the metadata column before fetching
            //The blob and returning it.
            
            //time to operate
            //grab the BlobLight without metadata so we don't get the same error
            Blob patient = getPhotoBlob(bi.getPhotoBlobLightWithoutMetadata(blobID));
            
            patient = stripImageMetadata(patient);
            
            //Should be all ready, let's update the bytes and the metadata
            
            bi.updateBlobBytes(patient);
            
            bi.updateBlobMetadata(patient);
            
        }
        
        /*
            We are now clear to return the photo.
        
            NOTE TO FUTURE DEBUGGERS:
            This is a recursive function and
            you will get stuck in an infinite loop if
            BlobIntegrator.getPhotoBlobLight()
            throws a BlobException for reasons that the
            catch block above can't fix.
        */
        return getPhotoBlobLight(blobID);
    }
    
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
        bi.deletePhotoBlob(blob.getBlobID());
        
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
     * @throws java.lang.ClassNotFoundException
     * @throws com.tcvcog.tcvce.domain.BlobTypeException
     */
    public Blob stripImageMetadata(Blob input) 
            throws IOException, 
            NoSuchElementException, 
            IntegrationException, 
            ClassNotFoundException, 
            BlobTypeException {

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
     * Accepts a filename and returns only the file extension
     * E.g. "image.jpg" -> "jpg"
     * @param filename
     * @return 
     */
    public String getFileExtension(String filename) {
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
    public String generateFilename(byte[] bytes) throws IOException{
        
        InputStream is = new ByteArrayInputStream(bytes);
        String extension = URLConnection.guessContentTypeFromStream(is);
        
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
            BObStatusException {

        BlobIntegrator bi = getBlobIntegrator();

        List<BOb> objectList = new ArrayList<>();

        int blobID = blob.getBlobID();
        
        if (blob.getType() == BlobType.PHOTO) {

            CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
            
            for (Integer id : bi.requestsAttachedToPhoto(blobID)) {

                objectList.add(ceari.getActionRequestByRequestID(id));
                
            }
            
            CaseIntegrator ci = getCaseIntegrator();
            
            for (Integer id : bi.violationsAttachedToPhoto(blobID)) {

                objectList.add(ci.getCodeViolation(id));
                
            }
            
            MunicipalityIntegrator mi = getMunicipalityIntegrator();
            
            for(Integer id : bi.munisAttachedToPhoto(blobID)){
                
                objectList.add(mi.getMuni(id));
                
            }
            
            OccInspectionIntegrator occi = getOccInspectionIntegrator();
            
            for(Integer id : bi.elementsAttachedToPhoto(blobID)){
                
                objectList.add(occi.getInspectedSpaceElement(id));
                
            }
            
            OccupancyIntegrator oi = getOccupancyIntegrator();
            
            for(Integer id : bi.occPeriodsAttachedToPhoto(blobID)){
                
                objectList.add(oi.getOccPeriod(id));
                
            }
            
        } else if (blob.getType() == BlobType.PDF){
            
            //No PDF Connections yet
            
        }

        return objectList;
        
    }
    
    /**
     * TEMPORARY SEARCH METHOD FOR BLOBS.
     * Should search all blob tables, add their entries to one list, and return it.
     * @param filename
     * @param description
     * @param before
     * @param after
     * @param municode
     * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     * @throws java.io.IOException 
     * @throws java.lang.ClassNotFoundException 
     * @throws com.tcvcog.tcvce.domain.BlobTypeException 
     */
    public List<BlobLight> searchBlobs(String filename, String description, LocalDateTime before, LocalDateTime after, int municode) 
            throws IntegrationException, 
            IOException, 
            ClassNotFoundException,
            BlobTypeException {
        
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
        
        //No PDF Search yet!
        //idList.addAll(bi.searchPDFBlobs(filename, description, before, after));
        
        List<BlobLight> blobList = new ArrayList<>();
        
        for(Integer id : idList){
            blobList.add(getPhotoBlobLight(id));
        }
        
        //No "getPDFBlob()" method!
        
        return blobList;
        
    }

}
