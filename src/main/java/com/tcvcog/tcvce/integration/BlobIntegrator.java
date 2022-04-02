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
package com.tcvcog.tcvce.integration;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.MunicipalityCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.BlobTypeException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.MetadataException;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.BlobLight;
import com.tcvcog.tcvce.entities.BlobLinkEnum;
import com.tcvcog.tcvce.entities.BlobType;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.IFace_BlobHolder;
import com.tcvcog.tcvce.entities.Metadata;
import com.tcvcog.tcvce.entities.MetadataKey;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.occupancy.FieldInspection;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The massive Integrator of Java objects and the database tables
 * that relate to binary objects, such as documents and photos
 * @author noah and Ellen Bascomb of Apartment 31Y
 */
public class BlobIntegrator extends BackingBeanUtils implements Serializable{
    
    /**
     * This method should only be used by the BlobCoordinator.
     * If you need to grab a Blob anywhere else use the coordinator method
     * @param blobid the blobID of the meta to be retrieved from db
     * @return the meta pulled from the db
     * @throws IntegrationException thrown instead of SQLException
     * @throws com.tcvcog.tcvce.domain.MetadataException
     */
    public BlobLight getBlobLight(int blobid) throws IntegrationException, MetadataException{
        if(blobid == 0){
            throw new IntegrationException("Cannot fetch BlobLight with id = 0!");
        }
        BlobLight blob = null;
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query =  "SELECT photodocid, photodocdescription, photodoccommitted, blobbytes_bytesid, \n" +
                        "       muni_municode, blobtype_typeid, metadatamap, title, createdby_userid, \n" +
                        "       photodoc.createdts, lastupdatedts, lastupdatedby_userid, deactivatedts, \n" +
                        "       deactivatedby_userid, filename \n" +
                        "  FROM public.photodoc LEFT JOIN public.blobbytes on photodoc.blobbytes_bytesid = blobbytes.bytesid  WHERE photodocid = ?;";
        
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, blobid);
            rs = stmt.executeQuery();
            while(rs.next()){
                blob = generateBlobLight(rs);
            }
            
        } catch (SQLException | BObStatusException ex) {
            System.out.println("BlobIntegrator.getBlobLight()");
            System.out.println(ex);
            throw new IntegrationException("BlobIntegrator.getPhotoBlobLight: Error retrieving blob. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return blob;
        
    }
    
     /**
     * Asks the inputted BlobHolder for its enum and uses that info
     * to query the correct table in the DB
     * 
     * @param bh not null object and not null Info Enum
     * @return the IDs of the blob light to be fetched
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public List<Integer> getBlobLightIDList(IFace_BlobHolder bh) throws BObStatusException, IntegrationException{
        if(bh == null || bh.getBlobLinkEnum() == null){
            throw new BObStatusException("Cannot retrieve BlobLight list with null holder or info enum");            
        }
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(bh.getBlobLinkEnum().getBlobLinkTablePhotodocIDFieldName());
        sb.append(" FROM ");
        sb.append(bh.getBlobLinkEnum().getBlobLinkTableName());
        sb.append(" WHERE ");
        sb.append(bh.getBlobLinkEnum().getBlobLinkTableParentIDFieldName());
        sb.append(" = ?;");
        
        PreparedStatement stmt = null;
        
        List<Integer> idList = new ArrayList<>();
        
        try {
            
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, bh.getParentObjectID());
            rs = stmt.executeQuery();
            while(rs.next()){
                 idList.add(rs.getInt(bh.getBlobLinkEnum().getBlobLinkTablePhotodocIDFieldName()));
            }
            
        } catch (SQLException ex) {
            //System.out.println(ex);
            throw new IntegrationException("BlobIntegrator.getBlobLightIDList: Could not get blob ID list. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return idList;
        
    }
    
    /**
     * Only used by the BlobCoordinator to get blobs with broken metadata.
     * @param blobID the blobID of the meta to be retrieved from db
     * @return the meta pulled from the db
     * @throws IntegrationException thrown instead of SQLException
     */
    public BlobLight getPhotoBlobLightWithoutMetadata(int blobID) throws IntegrationException {
        BlobLight blob = null;
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT photodocid, photodocdescription, photodoccommitted, blobbytes_bytesid, muni_municode, \n"
                + "uploaddate, blobtype_typeid, uploadpersonid, filename\n"
                + "FROM public.photodoc LEFT JOIN blobbytes on blobbytes_bytesid = bytesid\n"
                + "WHERE photodocid = ?;";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, blobID);
            rs = stmt.executeQuery();
            while(rs.next()){
                System.out.println("BlobIntegrator.getPhotoBlobLightWithoutMetadata: | retrieving blobID "  + blobID);
                blob = generateBlobLight(rs);
            }
            
        } catch (SQLException | MetadataException | BObStatusException ex) {
            System.out.println(ex);
            //System.out.println(ex);
            throw new IntegrationException("BlobIntegrator.getPhotoBlobLightWithoutMetadata: Error retrieving blob. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return blob;
        
    }
    
    /**
     * Generator method for extracting field values from a ResultSet which pulls
     * all columns from the table: photodoc
     * 
     * @param rs
     * @return
     * @throws SQLException
     * @throws MetadataException 
     */
    private BlobLight generateBlobLight(ResultSet rs) throws SQLException, MetadataException, IntegrationException, BObStatusException{
        MunicipalityCoordinator mc = getMuniCoordinator();
        BlobLight blob = new BlobLight();
        SystemIntegrator si = getSystemIntegrator();
        
        blob.setPhotoDocID(rs.getInt("photodocid"));
        blob.setDescription(rs.getString("photodocdescription"));
        blob.setCommitted(rs.getBoolean("photodoccommitted"));
        
        blob.setBytesID(rs.getInt("blobbytes_bytesid"));
        blob.setMuni(mc.getMuni(rs.getInt("muni_municode")));
        blob.setType(getBlobType(rs.getInt("blobtype_typeid")));
        
//        blob.setBlobMetadata(generateBlobMetadata(rs));
        blob.setTitle(rs.getString("title"));
       
        blob.setFilename(rs.getString("filename"));
        
       si.populateTrackedFields(blob, rs, false);
        
        return blob;
    }
    
    /**
     * Unused until metadata is processed
     * 
     * @param rs
     * @return
     * @throws SQLException 
     */
    private BlobLight generatePhotoBlobLightWithoutMetadata(ResultSet rs) throws SQLException {
        BlobLight blob = new BlobLight();
        blob.setPhotoDocID(rs.getInt("photodocid"));
        blob.setBytesID(rs.getInt("blobbytes_bytesid"));
        blob.setDescription(rs.getString("photodocdescription"));
        Timestamp time = rs.getTimestamp("uploaddate");
        if(time != null){
            blob.setCreatedTS(time.toLocalDateTime());
        }
//        blob.setType(BlobTypeEnum.blobTypeFromInt(rs.getInt("blobtype_typeid")));
//        blob.setFilename(rs.getString("filename"));
//        blob.setUploadPersonID(rs.getInt("uploadpersonid"));
//        blob.setMunicode(rs.getInt("muni_municode"));
        
        return blob;
    }
    
   
 
    /**
     * Gets the binary data of a file in the database
     * @param bl
     * @return
     */
    public Blob getBlob(BlobLight bl) {
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT bytesid, createdts, blob, uploadedby_userid, filename\n" +
"  FROM public.blobbytes WHERE bytesid = ?;";
        
        PreparedStatement stmt = null;
        
        Blob blob = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, bl.getBytesID());
            rs = stmt.executeQuery();
            while(rs.next()){
                blob = generateBlob(rs, bl);
            }
            
        } catch (SQLException ex) {
            //System.out.println(ex);
            System.out.println(ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return blob;
        
    } 
    /**
     * Generator for our basic blobs--actual binary data containers
     * @param rs
     * @param bl
     * @return
     * @throws SQLException 
     */
    private Blob generateBlob(ResultSet rs, BlobLight bl) throws SQLException{
        Blob blob = new Blob(bl);
        blob.setFilename(rs.getString("filename"));
        blob.setBytes(rs.getBytes("blob"));
        blob.setBytesID(rs.getInt("bytesid"));
        return blob;
        
    }
    
    
    /**
     * Extracts record from the blobtype table by ID and 
     * builds an object
     * 
     * @param typeid
     * @return the fully baked BlobType
     * @throws IntegrationException 
     */
    public BlobType getBlobType(int typeid) throws IntegrationException{
        
        if(typeid == 0){
            throw new IntegrationException("Cannot retrieve blob type with ID = 0!");
            
        }
        
          Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT typeid, typetitle, icon_iconid, contenttypestring, browserviewable, \n" +
                        "       notes, fileextensionsarr\n" +
                        "  FROM public.blobtype WHERE typeid=?";
        
        PreparedStatement stmt = null;
        
        BlobType bt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, typeid);
            rs = stmt.executeQuery();
            while(rs.next()){
                bt = generateBlobType(rs);
            }
            
        } catch (SQLException ex) {
            //System.out.println(ex);
            throw new IntegrationException("Error retrieving blob type. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        return bt;
    }
    
    
    /**
     * Generator for cute little BLOBType objects
     * @param rs
     * @return
     * @throws IntegrationException
     * @throws SQLException 
     */
    private BlobType generateBlobType(ResultSet rs) throws IntegrationException, SQLException{
        
        if(rs == null){
            throw new IntegrationException("Cannot make blob type with null ResultSet");
        }
        
        SystemIntegrator si = getSystemIntegrator();
        
        BlobType bt = new BlobType();
        bt.setTypeID(rs.getInt("typeid"));
        bt.setTitle(rs.getString("typetitle"));
        bt.setIcon(si.getIcon(rs.getInt("icon_iconid")));
        bt.setContentTypeString(rs.getString("contenttypestring"));
        bt.setBrowserViewable(rs.getBoolean("browserviewable"));
        bt.setNotes(rs.getString("notes"));
        if(rs.getArray("fileextensionsarr") != null){
            bt.setFileExtensionsPermitted(Arrays.asList((String[]) rs.getArray("fileextensionsarr").getArray()));
        }
        
        return bt;
    }
    
    /**
     * Extracts all blob types from the database and returns their IDs for fetching from 
     * getBobType(int id)
     * @return 
     */
    public List<Integer> getBlobTypeList() throws IntegrationException{
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT typeid FROM public.blobtype;";
        
        PreparedStatement stmt = null;
        
        List<Integer> idl = new ArrayList<>();
        
        try {
            
            stmt = con.prepareStatement(query);
            
            rs = stmt.executeQuery();
            while(rs.next()){
                idl.add(rs.getInt("typeid"));
                
            }
            
        } catch (SQLException ex) {
            //System.out.println(ex);
            throw new IntegrationException("Error retrieving blob type. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        return idl;
    }
    
    /**
     * @param rs
     * @return
     * @throws SQLException
     * @throws MetadataException 
     */
    private Metadata generateBlobMetadata(ResultSet rs) throws SQLException, MetadataException{
        Metadata meta = new Metadata();
        meta.setBytesID(rs.getInt("blobbytes_bytesid"));
//        meta.setType(BlobTypeEnum.blobTypeFromInt(rs.getInt("blobtype_typeid")));
        
        // We must now convert the byte array to an object
        
        byte[] mapBytes = rs.getBytes("metadatamap");
        
        try {
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(mapBytes));
        
        meta.replaceDataMap((Map<MetadataKey, String>) in.readObject());
        
        } catch (NullPointerException ex){
            MetadataException metaEx = new MetadataException("The metadata column of blobbytes_bytesid = " 
                                    + meta.getBytesID() + " is null. It is recommended "
                                    + "to strip the metadata of the image and "
                                    + "populate the column before fetching it again");
            
            metaEx.setMapNullError(true);
            throw metaEx;
            
        } catch (IOException | ClassNotFoundException ex) {
            throw new MetadataException("Something went wrong while reading "
                                       + "the metadata that was not related to a null column");
        }
        
        return meta;
    }
    
    /**
     * @return list of Blob IDs for all photos uploaded in the past month
     * @throws IntegrationException 
     */
    public List<Integer> getRecentPhotoBlobs() throws IntegrationException{
        ArrayList<Integer> blobIDList = new ArrayList();
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT photodocid\n"
                + "FROM public.photodoc LEFT JOIN blobbytes on blobbytes_bytesid = bytesid\n"
                + "WHERE uploaddate > ?;";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setTimestamp(1, java.sql.Timestamp.from(LocalDateTime.now().minusMonths(1)  // past month offset
                    .atZone(ZoneId.systemDefault()).toInstant()));
            rs = stmt.executeQuery();
            while(rs.next()){
                blobIDList.add(rs.getInt("photodocid"));

            }
            
        } catch (SQLException ex) {
            //System.out.println(ex);
            throw new IntegrationException("Error retrieving list of recent blobs. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return blobIDList;
        
    }
    
    /**
     * Takes in one of our BlobHolders and its corresponding BlobLight
     * and writes a record to the appropriate linking table
     * @param bh 
     * @param blight 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void linkBlobHolderToBlobMetadata(IFace_BlobHolder bh, BlobLight blight) throws BObStatusException, IntegrationException{
        if(bh == null || blight == null || bh.getBlobLinkEnum() == null){
            throw new BObStatusException("Cannot link blob holder to byte metadata with null object inputs");
        }
          
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(bh.getBlobLinkEnum().getBlobLinkTableName());
        sb.append(" (");
        sb.append(bh.getBlobLinkEnum().getBlobLinkTableParentIDFieldName());
        sb.append(",");
        sb.append(bh.getBlobLinkEnum().getBlobLinkTablePhotodocIDFieldName());
        sb.append(") VALUES (?,?);");
        
        try {
            
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, bh.getParentObjectID());
            stmt.setInt(2, blight.getPhotoDocID());
            
            stmt.execute();
            
        } catch (SQLException ex) {
            //System.out.println(ex);
            throw new IntegrationException("BlobIntegrator.linkBlobHolderToBlobMetadata | Error linking metadata to parent ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * stores this photodoc in the db
     * As of JAN 2022--this is the primary storage method for binary data
     * 
     * 
     * @param bl the meta to be stored
     * @return the id of the newly stored meta
     * @throws com.tcvcog.tcvce.domain.BlobTypeException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public int insertPhotoDoc(BlobLight bl) throws IntegrationException, BlobTypeException{
        
        if(bl == null || bl.getBytesID() == 0){
            throw new IntegrationException("cannot store null BlobLight or cannot link to a byte ID of 0!");
        }
        int lastID = 0;
        Connection con = getPostgresCon();
        String query =  " INSERT INTO public.photodoc(\n" +
                        "            photodocid, photodocdescription, blobbytes_bytesid, \n" +
                        "            muni_municode, blobtype_typeid, metadatamap, title, createdby_userid, \n" +
                        "            createdts, lastupdatedts, lastupdatedby_userid)\n" +
                        "    VALUES (DEFAULT, ?, ?, \n" +
                        "            ?, ?, ?, ?, ?, \n" +
                        "            now(), now(), ?);";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            
            stmt.setString(1, bl.getDescription());
            
            stmt.setInt(2, bl.getBytesID());
            
            if(bl.getMuni() != null){
                stmt.setInt(3, bl.getMuni().getMuniCode());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            if(bl.getType() != null){
                stmt.setInt(4, bl.getType().getTypeID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            if(bl.getBlobMetadata() != null){
                // TODO: Finish metadata
                stmt.setNull(5, java.sql.Types.NULL);
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            
            if(bl.getTitle() != null){
                stmt.setString(6, bl.getTitle());
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            
            if(bl.getCreatedBy() != null){
                stmt.setInt(7, bl.getCreatedBy().getUserID());
            }else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            
            if(bl.getLastUpdatedBy() != null){
                stmt.setInt(8, bl.getLastUpdatedBy().getUserID());
            }else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
                    
            System.out.println("BlobIntegrator.storeBlob | Statement: " + stmt.toString());
            stmt.execute();
            
            String idNumQuery = "SELECT currval('photodoc_photodocid_seq');";
            Statement s = con.createStatement();
            ResultSet rs;
            rs = s.executeQuery(idNumQuery);
            rs.next();
           lastID = rs.getInt(1);
            
            
        } catch (SQLException  ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting blob. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        return lastID;
    }
    
    /**
     * @return list of Blob IDs for all pdfs uploaded in the past month
     * @throws IntegrationException 
     */
    public List<Integer> getRecentPDFBlobs() throws IntegrationException{
        ArrayList<Integer> blobIDList = new ArrayList();
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT pdfdocid\n"
                + "FROM public.pdfdoc LEFT JOIN blobbytes on blobbytes_bytesid = bytesid\n"
                + "WHERE uploaddate > ?;";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setTimestamp(1, java.sql.Timestamp.from(LocalDateTime.now().minusMonths(1)  // past month offset
                    .atZone(ZoneId.systemDefault()).toInstant()));
            rs = stmt.executeQuery();
            while(rs.next()){
                blobIDList.add(rs.getInt("pdfdocid"));

            }
            
        } catch (SQLException ex) {
            //System.out.println(ex);
            throw new IntegrationException("Error retrieving list of recent blobs. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return blobIDList;
        
    }
    
 
    
    /**
     * Master byte input pathway!!!
     * stores the binary data of a file in the db
     * 
     * @param blob the meta to be stored
     * @return the blobID of the newly stored bytes
     * @throws com.tcvcog.tcvce.domain.BlobTypeException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws java.io.IOException
     */
    public int insertBlobBytes(Blob blob) throws BlobTypeException, IntegrationException, IOException{
        
        if(blob.getType() == null){
            throw new BlobTypeException("Attempted to store a blob with null type. ");
        }
        
        Connection con = getPostgresCon();
        String query =  " INSERT INTO public.blobbytes(\n" +
                            "            bytesid, createdts, blob, uploadedby_userid, filename)\n" +
                            "    VALUES (DEFAULT, now(), ?, ?, ?);";
        
        PreparedStatement stmt = null;

        List<Integer> existingBlobs = checkBytes(blob.getBytes());
        
        if(!existingBlobs.isEmpty()){
            //The file is already in our database, link it to the existing file.
            return existingBlobs.get(existingBlobs.size() - 1);
        } else {
            try {

                stmt = con.prepareStatement(query);
                stmt.setBytes(1, blob.getBytes());
                if(blob.getBlobUploadedBy() != null){
                    stmt.setInt(2, blob.getBlobUploadedBy().getUserID());
                } else {
                    stmt.setNull(2, java.sql.Types.NULL);
                }
                stmt.setString(3, blob.getFilename());

                System.out.println("BlobIntegrator.storeBlobBytes | Statement: " + stmt.toString());
                stmt.execute();

                String idNumQuery = "SELECT currval('blobbytes_seq');";
                Statement s = con.createStatement();
                ResultSet rs;
                rs = s.executeQuery(idNumQuery);
                rs.next();
                blob.setBytesID(rs.getInt(1));
                
                return blob.getBytesID();

            } catch (SQLException ex) {
                System.out.println("BlobIntegrator.storeBlobBytes() | ERROR: " + ex);
                throw new IntegrationException("Error saving blob bytes. ", ex);
            } finally{
                 if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
                 if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            } // close finally
        }
    }
    
    /**
     * This method checks to see if a set of bytes already exists in our database,
     * and returns the ID of the bytes if they do indeed exist.
     * It is meant to prevent duplicates from flooding our database.
     * @param proposedBytes
     * @return
     * @throws IntegrationException 
     */
    private List<Integer> checkBytes(byte[] proposedBytes) throws IntegrationException{
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT bytesid FROM public.blobbytes WHERE blob = ?;";
        
        PreparedStatement stmt = null;
        
        List<Integer> idList = new ArrayList<>();
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setBytes(1, proposedBytes);
            rs = stmt.executeQuery();
            while(rs.next()){
                 idList.add(rs.getInt("bytesid"));
            }
            
        } catch (SQLException ex) {
            //System.out.println(ex);
            throw new IntegrationException("Error retrieving blob bytes. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return idList;
        
    } 
    
    /**
     * Since many values in the Blob sphere shouldn't be changed after uploading,
     * this method only updates the blob description
     * @param blob the meta to be updated
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void updateBlobLight(BlobLight blob) throws  IntegrationException{
        if(blob == null){
            throw new IntegrationException("cannot update a null BlobLight");
            
        }
        
        Connection con = getPostgresCon();
        String query =  " UPDATE public.photodoc\n" +
                        "   SET photodocdescription=?, \n" +
                        "       muni_municode=?, blobtype_typeid=?, title=?, \n" +
                        " lastupdatedts=now(), lastupdatedby_userid=?, deactivatedts=?, \n" +
                        "       deactivatedby_userid=?" +
                        "  WHERE photodocid=?;";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setString(1, blob.getDescription());            
            
            if(blob.getMuni() != null){
                stmt.setInt(2, blob.getMuni().getMuniCode());
            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }
            
            if(blob.getType()!= null){
                stmt.setInt(3, blob.getType().getTypeID());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            stmt.setString(4, blob.getTitle());            
            
            if(blob.getLastUpdatedBy()!= null){
                stmt.setInt(5, blob.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            
            if(blob.getDeactivatedTS()!= null){
                stmt.setTimestamp(6, java.sql.Timestamp.valueOf(blob.getDeactivatedTS()));
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            
            if(blob.getDeactivatedBy()!= null){
                stmt.setInt(7, blob.getDeactivatedBy().getUserID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            
            stmt.setInt(8, blob.getPhotoDocID());
            System.out.println("BlobIntegrator.updatePhotoDocMetaData: updating blob pdid: " + blob.getPhotoDocID());
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error updating blob. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * This method updates a blob's filename.
     * NOT SAFE. Use the Coordinator method, it makes sure that the correct 
     * file extension is at the end of the filename.
     * @param blob the meta to be updated
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void updateBlobFilename(BlobLight blob) throws  IntegrationException{
        
        Connection con = getPostgresCon();
        String query = " UPDATE public.blobbytes\n"
                + " SET filename=?\n"
                + " WHERE bytesid=?;\n\n";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
//            stmt.setString(1, blob.getFilename());            
            stmt.setInt(2, blob.getBytesID());
            
            System.out.println("BlobIntegrator.storeBlob | Statement: " + stmt.toString());
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error updating blob. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
  
    /**
     * Updates the bytes of a blob.
     * Should be used only to remove the metadata of blobs that were inserted
     * before metadata stripping was a thing.
     * @param blob the meta to be updated
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws java.io.IOException
     */
    public void updateBlobBytes(Blob blob) throws  IntegrationException, IOException{
        
        Connection con = getPostgresCon();
        String query = "UPDATE public.blobbytes\n"
                + " SET blob=?\n"
                + " WHERE bytesid=?;";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setBytes(1, blob.getBytes());
            stmt.setInt(2, blob.getBytesID());
            
            System.out.println("BlobIntegrator.storeBlob | Statement: " + stmt.toString());
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error updating blob. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * Removes the photodoc row from the photodoc table.
     * No longer removes any connections to the photodoc
     * Users must erase each connection manually via the interface.
     * This method should only be used by the coordinator.
     * Use the method on the coordinator to delete blobs, it is safer - checks for connections first.
     * @param bl
     * @throws IntegrationException thrown instead of a SQLException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public void deletePhotoBlob(BlobLight bl) throws IntegrationException, BObStatusException{
        
        if(bl == null){
            throw new BObStatusException("Cannot remove photodoc with null bloblight");
            
        }
        //delete the main photodoc entry
        String query = "DELETE FROM public.photodoc WHERE photodocid = ?;";
        
        PreparedStatement stmt = null;
        Connection con = getPostgresCon();
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, bl.getPhotoDocID());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("BlobIntegrator.deletePhotoBlob() | ERROR: "+ ex);
            throw new IntegrationException("Error deleting blob. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * Removes the pdfdoc row from the pdfdoc table.
     * No longer removes any connections to the pdfdoc
     * Users must erase each connection manually via the interface.
     * This method should only be used by the coordinator.
     * Use the method on the coordinator to delete blobs, it checks for connections first
     * so it is safer.
     * @param blobID the blob to be removed
     * @throws IntegrationException thrown instead of a SQLException
     */
    public void deletePDFBlob(int blobID) throws IntegrationException{
        
        //delete the main photodoc entry
        String query = "DELETE FROM public.pdfdoc WHERE pdfdocid = ?;";
        
        PreparedStatement stmt = null;
        Connection con = getPostgresCon();
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, blobID);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("BlobIntegrator.deletePDFBlob() | ERROR: "+ ex);
            throw new IntegrationException("Error deleting blob. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * Removes the bytes with the given ID.
     * This method should only be used by the coordinator.
     * Use the Blob removal methods on the coordinator, 
     * they check to make sure no blob is using the bytes before removal.
     * @param bytesID
     * @throws IntegrationException thrown instead of a SQLException
     */
    public void deleteBytes(int bytesID) throws IntegrationException{
        
        //delete the main photodoc entry
        String query = "DELETE FROM public.blobbytes WHERE bytesid = ?;";
        
        PreparedStatement stmt = null;
        Connection con = getPostgresCon();
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, bytesID);
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println("BlobIntegrator.deleteBytes() | ERROR: "+ ex);
            throw new IntegrationException("Error deleting blob. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * Returns the IDs of all photo blobs connected to the given bytesID
     * @param bytesID
     * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public List<Integer> getPhotoBlobsFromBytesID(int bytesID) throws IntegrationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT photodocid FROM public.photodoc WHERE blobbytes_bytesid = ?;";
        
        PreparedStatement stmt = null;
        
        List<Integer> idList = new ArrayList<>();
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, bytesID);
            rs = stmt.executeQuery();
            while(rs.next()){
                 idList.add(rs.getInt("photodocid"));
            }
            
        } catch (SQLException ex) {
            //System.out.println(ex);
            throw new IntegrationException("Error retrieving attached blob IDs. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return idList;
    }
    
    /**
     * Returns the IDs of all pdf blobs connected to the given bytesID
     * @param bytesID
     * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public List<Integer> getPDFBlobsFromBytesID(int bytesID) throws IntegrationException{
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT pdfdocid FROM public.pdfdoc WHERE blobbytes_bytesid = ?;";
        
        PreparedStatement stmt = null;
        
        List<Integer> idList = new ArrayList<>();
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, bytesID);
            rs = stmt.executeQuery();
            while(rs.next()){
                 idList.add(rs.getInt("pdfdocid"));
            }
            
        } catch (SQLException ex) {
            //System.out.println(ex);
            throw new IntegrationException("Error retrieving attached blob IDs. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return idList;
    }
    
    public void commitPhotograph(int photoID) throws IntegrationException{
        Connection con = getPostgresCon();
        String query =  " UPDATE public.photodoc\n" +
                        " SET photodoccommitted = true\n" +
                        " WHERE photodocid = ?;";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, photoID);
            
            System.out.println("ImageServices.commitPhotograph | Statement: " + stmt.toString());
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error commiting photo", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
   

    
    /**
     * Removes a link between a given BLOB light and the requested Linking table
     * represented in the enum. Any objects linked to the given bloblight will be deleted forever
     * @param bl
     * @param blenum
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public void removeLinkToBlobLightByBlobLinkEnum(BlobLight bl, BlobLinkEnum blenum) throws IntegrationException, BObStatusException {

        if(bl == null || blenum == null){
            throw new BObStatusException("cannot remove link to photodoc record with null bloblight or enum!");
        }
        //property linker table
        StringBuilder sb = new StringBuilder();
        
        sb.append("DELETE FROM ");
        sb.append(blenum.getBlobLinkTableName());
        sb.append(" WHERE ");
        sb.append(blenum.getBlobLinkTablePhotodocIDFieldName());
        sb.append("=?;");
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, bl.getPhotoDocID());
            
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println("BlobIntegrator.removeLinkToBlob | ERROR: "+ ex);
            throw new IntegrationException("Error deleting blob link.", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
   
    
    /**
     * TEMPORARY SEARCH METHOD FOR BLOBS.
     * @param filename
     * @param description
     * @param before
     * @param after
     * @param municode
     * @deprecated as I don't encourage using this method, it's temporary
     * @return list of Blob IDs for all photos that meet our search parameters
     * @throws IntegrationException 
     */
    public List<Integer> searchPhotoBlobs(String filename,
                                          String description,
                                          LocalDateTime before,
                                          LocalDateTime after,
                                          int municode)
                                          throws IntegrationException {
        ArrayList<Integer> blobIDList = new ArrayList();
        Connection con = getPostgresCon();
        ResultSet rs = null;

        //Store search terms, more information in a large comment below
        String[] filenameTokens = null;

        String[] descTokens = null;
        
        StringBuilder query = new StringBuilder();

        query.append("SELECT DISTINCT photodocid\n"
                + "FROM public.photodoc LEFT JOIN blobbytes on blobbytes_bytesid = bytesid\n"
                + "WHERE (");

        PreparedStatement stmt = null;
        
        //We will store each thing we would like to test for in this 
        ArrayList<String> clauses = new ArrayList<>();
        
        //First check if any parameters have been set at all
        if (filename != null
                || description != null
                || before != null
                || after != null) {    

            //Dates first
            if (before != null) {
                clauses.add("uploaddate < ?");
            }
            
            if (after != null) {
                clauses.add("uploaddate > ?");
            }

            //we are going to split apart the filename and description search terms 
            //on spaces and search for each token.
            //If a person searches for "house photo", we want "photo of house" to show up

            if (filename != null) {

                ArrayList<String> statements = new ArrayList<>();

                //Split on whitespaces
                filenameTokens = filename.split("\\s");

                //Add as many ILIKE statements as we have terms
                for (int i = 0; i < filenameTokens.length; i++) {
                    statements.add("filename ILIKE ?");
                }

                //Join them together with an OR in between
                //Then throw the completed clause into the clauses array
                String finished = String.join(" OR\n", statements);

                clauses.add(finished);

            }

            if (description != null) {
                ArrayList<String> statements = new ArrayList<>();

                //Split on whitespaces
                descTokens = description.split("\\s");

                //Add as many ILIKE statements as we have terms
                for (int i = 0; i < descTokens.length; i++) {
                    statements.add("description ILIKE ?");
                }

                //Join them togeth with an OR in between
                //Then throw the completed clause into the clauses array
                String finished = String.join(" OR\n", statements);

                clauses.add(finished);
            }

        }

        //We must always include the muni_code in the search.
            
        clauses.add("muni_municode = ?");
            
        //We want to make sure all clauses are satisfied, so put an AND between them
        //And use parantheses to make sure that each is a self-contained logical expression
        String allClauses = String.join(") AND\n(", clauses);
            
        query.append(allClauses + ")\n");
        
        //add limit 
        query.append("LIMIT 150;");
        
        try {
            
            stmt = con.prepareStatement(query.toString());
            
            int index = 0;
            
            //Remember to iterate the index first, then use it.
            
            if (before != null) {
                java.sql.Timestamp stamp = java.sql.Timestamp.valueOf(before);
                index++;
                stmt.setTimestamp(index, stamp);
            }
            
            if (after != null) {
                java.sql.Timestamp stamp = java.sql.Timestamp.valueOf(after);
                index++;
                stmt.setTimestamp(index, stamp);
            }
            
            if(filename != null){
                for(String token : filenameTokens){
                    index++;
                    //Also add the wildcards
                    stmt.setString(index, "%" + token + "%");
                }
            }
            
            if(description != null){
                for(String token : descTokens){
                    index++;
                    //Also add the wildcards
                    stmt.setString(index, "%" + token + "%");
                }
            }
            
            //Finally, set municode
            index++;
            stmt.setInt(index, municode);
            
            rs = stmt.executeQuery();
            while(rs.next()){
                blobIDList.add(rs.getInt("photodocid"));
            }
            
        } catch (SQLException ex) {
            System.out.println("BlobIntegrator.searchPhotoBlobs() | ERROR: " + ex);
            throw new IntegrationException("Error searching Blobs ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return blobIDList;
        
    }
    
    /**
     * TEMPORARY SEARCH METHOD FOR BLOBS.
     * @param filename
     * @param description
     * @param before
     * @param after
     * @param municode
     * @deprecated as I don't encourage using this method, it's temporary
     * @return list of Blob IDs for all photos that meet our search parameters
     * @throws IntegrationException 
     */
    public List<Integer> searchPDFBlobs(String filename,
                                          String description,
                                          LocalDateTime before,
                                          LocalDateTime after,
                                          int municode)
                                          throws IntegrationException {
        ArrayList<Integer> blobIDList = new ArrayList();
        Connection con = getPostgresCon();
        ResultSet rs = null;

        //Store search terms, more information in a large comment below
        String[] filenameTokens = null;

        String[] descTokens = null;
        
        StringBuilder query = new StringBuilder();

        query.append("SELECT DISTINCT pdfdocid\n"
                + "FROM public.pdfdoc LEFT JOIN blobbytes on blobbytes_bytesid = bytesid\n"
                + "WHERE (");

        PreparedStatement stmt = null;
        
        //We will store each thing we would like to test for in this 
        ArrayList<String> clauses = new ArrayList<>();
        
        //First check if any parameters have been set at all
        if (filename != null
                || description != null
                || before != null
                || after != null) {    

            //Dates first
            if (before != null) {
                clauses.add("uploaddate < ?");
            }
            
            if (after != null) {
                clauses.add("uploaddate > ?");
            }

            //we are going to split apart the filename and description search terms 
            //on spaces and search for each token.
            //If a person searches for "house photo", we want "photo of house" to show up

            if (filename != null) {

                ArrayList<String> statements = new ArrayList<>();

                //Split on whitespaces
                filenameTokens = filename.split("\\s");

                //Add as many ILIKE statements as we have terms
                for (int i = 0; i < filenameTokens.length; i++) {
                    statements.add("filename ILIKE ?");
                }

                //Join them together with an OR in between
                //Then throw the completed clause into the clauses array
                String finished = String.join(" OR\n", statements);

                clauses.add(finished);

            }

            if (description != null) {
                ArrayList<String> statements = new ArrayList<>();

                //Split on whitespaces
                descTokens = description.split("\\s");

                //Add as many ILIKE statements as we have terms
                for (int i = 0; i < descTokens.length; i++) {
                    statements.add("description ILIKE ?");
                }

                //Join them togeth with an OR in between
                //Then throw the completed clause into the clauses array
                String finished = String.join(" OR\n", statements);

                clauses.add(finished);
            }

        }

        //We must always include the muni_code in the search.
            
        clauses.add("muni_municode = ?");
            
        //We want to make sure all clauses are satisfied, so put an AND between them
        //And use parantheses to make sure that each is a self-contained logical expression
        String allClauses = String.join(") AND\n(", clauses);
            
        query.append(allClauses + ")\n");
        
        //add limit 
        query.append("LIMIT 150;");
        
        try {
            
            stmt = con.prepareStatement(query.toString());
            
            int index = 0;
            
            //Remember to iterate the index first, then use it.
            
            if (before != null) {
                java.sql.Timestamp stamp = java.sql.Timestamp.valueOf(before);
                index++;
                stmt.setTimestamp(index, stamp);
            }
            
            if (after != null) {
                java.sql.Timestamp stamp = java.sql.Timestamp.valueOf(after);
                index++;
                stmt.setTimestamp(index, stamp);
            }
            
            if(filename != null){
                for(String token : filenameTokens){
                    index++;
                    //Also add the wildcards
                    stmt.setString(index, "%" + token + "%");
                }
            }
            
            if(description != null){
                for(String token : descTokens){
                    index++;
                    //Also add the wildcards
                    stmt.setString(index, "%" + token + "%");
                }
            }
            
            //Finally, set municode
            index++;
            stmt.setInt(index, municode);
            
            rs = stmt.executeQuery();
            while(rs.next()){
                blobIDList.add(rs.getInt("pdfdocid"));
            }
            
        } catch (SQLException ex) {
            System.out.println("BlobIntegrator.searchPDFBlobs() | ERROR: " + ex);
            throw new IntegrationException("Error searching Blobs ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return blobIDList;
        
    }
    
   
}