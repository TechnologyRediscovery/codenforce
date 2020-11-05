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
import com.tcvcog.tcvce.integration.CodeIntegrator;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccInspectionIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
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
            try {
                BlobLight blob = bi.getPhotoBlobLight(blobID);
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
                System.out.println("BlobCoordinator.getImage | " + ex);
            } catch (FileNotFoundException ex) {
                System.out.println("BlobCoordinator.getImage | could not find pdf-icon.png ");
            }
            return sc;
        }
    }

    public int storeBlob(Blob blob) throws BlobException, IntegrationException, IOException {
        //Test to see if the byte array is larger than a GIGABYTE
        if (blob.getBytes().length > GIGABYTE) {
            throw new BlobException("You cannot upload a file larger than 1 gigabyte.");
        }

        // TODO: validate BLOB's and throw exception if corrupted
        //First, let's find out what type of file this is.
        String fileExtension = getFileExtension(blob.getFilename());

        if (fileExtension.contains("jpg")
                || fileExtension.contains("jpeg")
                || fileExtension.contains("gif")
                || fileExtension.contains("png")) {
            blob.setType(BlobType.PHOTO);
        } else if (fileExtension.contains("pdf")) {
            blob.setType(BlobType.PDF);
        } else {
            //Incorrect file type
            throw new BlobException("Incompatible file type, please upload a JPG, JPEG, GIF, PNG, or PDF.");
        }

        switch (blob.getType()) {
            case PHOTO:
                blob = stripImageMetadata(blob);
                return getBlobIntegrator().storePhotoBlob(blob);
            case PDF:
                //No PDF methods yet!
                //TODO: Strip metadata from original file and save it in the Metadata dictionary
                return 0;
            default:
                return 0;
        }

    }

    public Blob getPhotoBlob(int blobID) throws IntegrationException, IOException, ClassNotFoundException {
        BlobIntegrator bi = getBlobIntegrator();

        Blob blob = new Blob(bi.getPhotoBlobLight(blobID));

        blob.setBytes(bi.getBlobBytes(blobID));

        return blob;
    }

    // TODO: MAYBE seperate into PDF and Photo deletes, verify types appropriately,
    // then delete with integrator.
    public void deleteBlob(int blobID) throws IntegrationException {
        getBlobIntegrator().deletePhotoBlob(blobID);
    }

    /**
     * A method that removes all metadata from an image blob's bytes and puts
     * them into its Metadata field. Should always be called before saving an
     * image file to the database.
     *
     * @param input
     * @return The blob that was put into it, stripped of metadata
     * @throws java.io.IOException
     */
    public Blob stripImageMetadata(Blob input) throws IOException, NoSuchElementException {

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
     * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     * @throws java.io.IOException 
     * @throws java.lang.ClassNotFoundException 
     */
    public List<BlobLight> searchBlobs(String filename, String description, LocalDateTime before, LocalDateTime after) 
            throws IntegrationException, 
            IOException, 
            ClassNotFoundException {
        
        BlobIntegrator bi = getBlobIntegrator();
        
        //For GIGO and optimization purposes, throw out the filename and description
        //if they don't contain non-whitespace
        if(!filename.matches("\\S")){
            filename = null;
        }
        
        if(!description.matches("\\S")){
            description = null;
        }
        
        List<Integer> idList = new ArrayList<>();
        
        idList.addAll(bi.searchPhotoBlobs(filename, description, before, after));
        
        //No PDF Search yet!
        //idList.addAll(bi.searchPDFBlobs(filename, description, before, after));
        
        List<BlobLight> blobList = new ArrayList<>();
        
        for(Integer id : idList){
            blobList.add(bi.getPhotoBlobLight(id));
        }
        
        //No "getPDFBlob()" method!
        
        return blobList;
        
    }

}