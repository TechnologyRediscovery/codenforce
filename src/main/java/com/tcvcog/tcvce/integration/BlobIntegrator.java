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
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.BlobTypeException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.MetadataException;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.BlobLight;
import com.tcvcog.tcvce.entities.BlobType;
import com.tcvcog.tcvce.entities.BlobTypeEnum;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Metadata;
import com.tcvcog.tcvce.entities.MetadataKey;
import com.tcvcog.tcvce.entities.Property;
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
import java.util.List;
import java.util.Map;

/**
 *
 * @author noah
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
        String query = "SELECT photodocid, photodocdescription, photodoccommitted, blobbytes_bytesid, \n" +
                        "       muni_municode, blobtype_typeid, metadatamap, title, createdby_userid, \n" +
                        "       photodoc.createdts, filename \n" +
                        "  FROM public.photodoc LEFT JOIN public.blobbytes on photodoc.blobbytes_bytesid = blobbytes.bytesid  WHERE photodocid = ?;";
        
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, blobid);
            rs = stmt.executeQuery();
            while(rs.next()){
                blob = generateBlobLight(rs);
            }
            
        } catch (SQLException ex) {
            //System.out.println(ex);
            throw new IntegrationException("BlobIntegrator.getPhotoBlobLight: Error retrieving blob. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return blob;
        
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
            
        } catch (SQLException | MetadataException ex) {
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
    private BlobLight generateBlobLight(ResultSet rs) throws SQLException, MetadataException, IntegrationException{
        MunicipalityCoordinator mc = getMuniCoordinator();
        BlobLight blob = new BlobLight();
        UserCoordinator uc = getUserCoordinator();
        
        blob.setPhotoDocID(rs.getInt("photodocid"));
        blob.setDescription(rs.getString("photodocdescription"));
        blob.setCommitted(rs.getBoolean("photodoccommitted"));
        
        blob.setBytesID(rs.getInt("blobbytes_bytesid"));
        blob.setMuni(mc.getMuni(rs.getInt("muni_municode")));
        blob.setType(getBlobType(rs.getInt("blobtype_typeid")));
        
//        blob.setBlobMetadata(generateBlobMetadata(rs));
        blob.setTitle(rs.getString("title"));
        blob.setCreatedBy(uc.user_getUser(rs.getInt("createdby_userid")));
        blob.setFilename(rs.getString("filename"));
        
        Timestamp time = rs.getTimestamp("createdts");
        if(time != null){
            blob.setCreatedTS(time.toLocalDateTime());
        }
        
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
     * This method should only be used by the BlobCoordinator.
     * If you need to grab a Blob anywhere else use the coordinator method
     * @param blobID the blobID of the meta to be retrieved from db
     * @return the meta pulled from the db
     * @throws IntegrationException thrown instead of SQLException
     * @throws com.tcvcog.tcvce.domain.MetadataException
     */
    public BlobLight getPDFBlobLight(int blobID) throws IntegrationException, MetadataException{
        BlobLight blob = null;
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT pdfdocid, pdfdocdescription, pdfdoccommitted, blobbytes_bytesid, muni_municode, \n"
                + "uploaddate, blobtype_typeid, uploadpersonid, filename, metadatamap\n"
                + "FROM public.pdfdoc LEFT JOIN blobbytes on blobbytes_bytesid = bytesid\n"
                + "WHERE pdfdocid = ?;";
        
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, blobID);
            rs = stmt.executeQuery();
            while(rs.next()){
                System.out.println("BlobIntegrator.getPDFBlobLight: | retrieving blobID "  + blobID);
                blob = generatePDFBlobLight(rs);
            }
            
        } catch (SQLException ex) {
            //System.out.println(ex);
            throw new IntegrationException("BlobIntegrator.getPDFBlobLight: Error retrieving blob. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return blob;
        
    }
    
    /**
     * Only used by the BlobCoordinator to get blobs with broken metadata.
     * @param blobID the blobID of the blob to be retrieved from db
     * @return the meta pulled from the db
     * @throws IntegrationException thrown instead of SQLException
     */
    public BlobLight getPDFBlobLightWithoutMetadata(int blobID) throws IntegrationException {
        BlobLight blob = null;
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT pdfdocid, pdfdocdescription, pdfdoccommitted, blobbytes_bytesid, muni_municode, \n"
                + "uploaddate, blobtype_typeid, uploadpersonid, filename\n"
                + "FROM public.pdfdoc LEFT JOIN blobbytes on blobbytes_bytesid = bytesid\n"
                + "WHERE pdfdocid = ?;";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, blobID);
            rs = stmt.executeQuery();
            while(rs.next()){
                System.out.println("BlobIntegrator.getPDFBlobLightWithoutMetadata: | retrieving blobID "  + blobID);
                blob = generatePDFBlobLightWithoutMetadata(rs);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex);
            //System.out.println(ex);
            throw new IntegrationException("BlobIntegrator.getPDFBlobLightWithoutMetadata: Error retrieving blob. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return blob;
        
    }
    
    /**
     * @param rs
     * 
     * @deprecated 
     * @return
     * @throws SQLException
     * @throws MetadataException 
     */
    private BlobLight generatePDFBlobLight(ResultSet rs) throws SQLException, MetadataException{
        BlobLight blob = new BlobLight();
        
        blob.setPhotoDocID(rs.getInt("photodocid"));
        blob.setBytesID(rs.getInt("blobbytes_bytesid"));
        blob.setDescription(rs.getString("pdfdocdescription"));
        
        Timestamp time = rs.getTimestamp("uploaddate");
        if(time != null){
            blob.setCreatedTS(time.toLocalDateTime());
        }
        
        blob.setFilename(rs.getString("filename"));
//        blob.setType(BlobTypeEnum.blobTypeFromInt(rs.getInt("blobtype_typeid")));
//        blob.setFilename(rs.getString("filename"));
//        blob.setUploadPersonID(rs.getInt("uploadpersonid"));
//        blob.setMunicode(rs.getInt("muni_municode"));
        
//        blob.setBlobMetadata(generateBlobMetadata(rs));
        return blob;
    }
    
    /**
     * @deprecated 
     * @param rs
     * @return
     * @throws SQLException 
     */
    private BlobLight generatePDFBlobLightWithoutMetadata(ResultSet rs) throws SQLException {
        BlobLight blob = new BlobLight();
        blob.setPhotoDocID(rs.getInt("pdfdocid"));
        blob.setBytesID(rs.getInt("blobbytes_bytesid"));
        blob.setDescription(rs.getString("pdfdocdescription"));
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
    
    private Blob generateBlob(ResultSet rs, BlobLight bl) throws SQLException{
        Blob blob = new Blob(bl);
        blob.setFilename(rs.getString("filename"));
        blob.setBytes(rs.getBytes("blob"));
        blob.setBytesID(rs.getInt("bytesid"));
        return blob;
        
    }
    
    public BlobType getBlobType(int typeid) throws IntegrationException{
        
        if(typeid == 0){
            throw new IntegrationException("Cannot retrieve blob type with ID = 0!");
            
        }
        
          Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT typeid, typetitle, icon_iconid\n" +
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
        bt.setTitle(rs.getString("typetitle"));
        bt.setIcon(si.getIcon(rs.getInt("icon_iconid")));
        bt.setTypeEnum(BlobTypeEnum.blobTypeFromInt(rs.getInt("typeid")));
        
        return bt;
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
        meta.setType(BlobTypeEnum.blobTypeFromInt(rs.getInt("blobtype_typeid")));
        
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
     * stores this photodoc in the db
     * 
     * @param blob the meta to be stored
     * @return the blobID of the newly stored meta
     * @throws com.tcvcog.tcvce.domain.BlobTypeException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public Blob storeBlob(Blob blob) throws IntegrationException, BlobTypeException{
        
        if(blob == null){
            throw new IntegrationException("cannot store null blob!");
        }
        
        Connection con = getPostgresCon();
        String query =  " INSERT INTO public.photodoc(\n" +
                        "            photodocid, photodocdescription, blobbytes_bytesid, \n" +
                        "            muni_municode, blobtype_typeid, metadatamap, title, createdby_userid, \n" +
                        "            createdts)\n" +
                        "    VALUES (DEFAULT, ?, ?, \n" +
                        "            ?, ?, ?, ?, ?, \n" +
                        "            now());";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            
            stmt.setString(1, blob.getDescription());
            
            int bytesID = storeBlobBytes(blob);
            
            stmt.setInt(2, bytesID);
            
            if(blob.getMuni() != null){
                stmt.setInt(3, blob.getMuni().getMuniCode());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            if(blob.getType() != null){
                stmt.setInt(4, blob.getType().getTypeEnum().getTypeID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            if(blob.getBlobMetadata() != null){
                // TODO: Finish metadata
                stmt.setNull(5, java.sql.Types.NULL);
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            
            if(blob.getTitle() != null){
                stmt.setString(6, blob.getTitle());
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            
            if(blob.getCreatedBy() != null){
                stmt.setInt(7, blob.getCreatedBy().getUserID());
                
            }else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
                    
            System.out.println("BlobIntegrator.storeBlob | Statement: " + stmt.toString());
            stmt.execute();
            
            String idNumQuery = "SELECT currval('photodoc_photodocid_seq');";
            Statement s = con.createStatement();
            ResultSet rs;
            rs = s.executeQuery(idNumQuery);
            rs.next();
            int lastID = rs.getInt(1);
            
            //set the IDs so after we throw the blob back they can access the blob and bytes
            blob.setPhotoDocID(lastID);
            blob.setBytesID(bytesID);
            
        } catch (SQLException | IOException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting blob. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        return blob;
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
     * stores this pdfdoc in the db
     * @param blob the meta to be stored
     * @deprecated  replaced by unified doc and photo workflow
     * @return the blobID of the newly stored meta
     * @throws com.tcvcog.tcvce.domain.BlobTypeException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @deprecated 
     */
    public Blob storePDFBlob(Blob blob) throws IntegrationException, BlobTypeException{
        
        Connection con = getPostgresCon();
        String query =  " INSERT INTO public.pdfdoc(pdfdocid, pdfdocdescription, blobbytes_bytesid, muni_municode)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?);";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setString(1, blob.getDescription());
            
            int bytesID = storeBlobBytes(blob);
            
            stmt.setInt(2, bytesID);
            
//            stmt.setInt(3, blob.getMunicode());
            
            System.out.println("BlobIntegrator.storePDFBlob | Statement: " + stmt.toString());
            stmt.execute();
            
            //We use the photodoc sequence for both PDFs and Photos so there aren't any collisions
            String idNumQuery = "SELECT currval('photodoc_photodocid_seq');"; 
            Statement s = con.createStatement();
            ResultSet rs;
            rs = s.executeQuery(idNumQuery);
            rs.next();
            int lastID = rs.getInt(1);
            
            //set the IDs so we can throw the blob back and they can access the blob and bytes
            blob.setPhotoDocID(lastID);
            blob.setBytesID(bytesID);
            
        } catch (SQLException | IOException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting blob. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        return blob;
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
    private int storeBlobBytes(Blob blob) throws BlobTypeException, IntegrationException, IOException{
        
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
    public void updatePhotoDocMetadata(BlobLight blob) throws  IntegrationException{
        
        Connection con = getPostgresCon();
        String query = " UPDATE public.photodoc\n"
                + " SET photodocdescription=?, title=?\n"
                + " WHERE photodocid=?;";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setString(1, blob.getDescription());            
            stmt.setString(2, blob.getTitle());            
            stmt.setInt(3, blob.getPhotoDocID());
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
     * Since many values in the Blob sphere shouldn't be changed after uploading,
     * this method only updates the blob description
     * @param blob the meta to be updated
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void updatePDFBlobDescription(BlobLight blob) throws  IntegrationException{
        
        Connection con = getPostgresCon();
        String query = " UPDATE public.pdfdoc\n"
                + " SET pdfdocdescription=?\n"
                + " WHERE pdfdocid=?;\n\n";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setString(1, blob.getDescription());            
            stmt.setInt(2, blob.getPhotoDocID());
            
            System.out.println("BlobIntegrator.updatePDFBlobDescription | Statement: " + stmt.toString());
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
     * BROKEN!!!
     * 
     * Updates the metadata and filename of a blobbytes entry.
     * @param blob the meta to be updated
     * @throws com.tcvcog.tcvce.domain.IntegrationException
    
     */
    public void updateBlobMetadata(BlobLight blob) throws  IntegrationException{
        
        Connection con = getPostgresCon();
        String query = "UPDATE public.blobbytes\n"
                + " SET filename=? "
                + " WHERE bytesid=?;";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
//            stmt.setString(1, blob.getFilename());
            stmt.setBytes(1, blob.getBlobMetadata().getMapBytes());
            stmt.setInt(2, blob.getBytesID());
            
            System.out.println("BlobIntegrator.storeBlob | Statement: " + stmt.toString());
            stmt.execute();
            
        } catch (SQLException | IOException ex) {
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
     * @param photoID
     * @throws IntegrationException 
     */
    public void commitPDF(int photoID) throws IntegrationException{
        Connection con = getPostgresCon();
        String query =  " UPDATE public.pdfdoc\n" +
                        " SET pdfdoccommitted = true\n" +
                        " WHERE pdfdocid = ?;";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, photoID);
            
            System.out.println("ImageServices.commitPDF | Statement: " + stmt.toString());
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error commiting pdf", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    
    /**
     * Removes a link between property and blob, leaving the bytes record intact for later fishing out if needed
     * 
     * @param bl
     * @param prop
     * @throws IntegrationException 
     */
    public void removePropertyBlobLink(BlobLight bl, Property prop) throws IntegrationException, BObStatusException {

        if(bl == null || prop == null){
            throw new BObStatusException("cannot remove property blob link with null blob or prop!");
        }
        //property linker table
        String query = "DELETE"
                + "  FROM public.propertyphotodoc WHERE photodoc_photodocid = ? AND property_propertyid = ?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, bl.getPhotoDocID());
            stmt.setInt(2, prop.getPropertyID());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("BlobIntegrator.removePropertyBlobLink() | ERROR: "+ ex);
            throw new IntegrationException("Error deleting link. Photo-Property", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    /**
     * Removes a link between case and blob, leaving the bytes record intact for later fishing out if needed
     * 
     * @param bl
     * @param cse
     * @throws IntegrationException 
     */
    public void removeCECaseBlobLink(BlobLight bl, CECase cse) throws IntegrationException, BObStatusException {

        if(bl == null || cse == null){
            throw new BObStatusException("cannot remove property blob link with null blob or prop!");
        }
        //property linker table
        String query = "DELETE"
                + "  FROM public.cecasephotodoc WHERE photodoc_photodocid = ? AND cecase_caseid = ?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, bl.getPhotoDocID());
            stmt.setInt(2, cse.getCaseID());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("BlobIntegrator.removeCECaseBlobLink() | ERROR: "+ ex);
            throw new IntegrationException("Error deleting link. Photo-Property", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    public void removePhotoCEARLink(int blobID, int requestID) throws IntegrationException {

        //property linker table
        String query = "DELETE"
                + "  FROM public.ceactionrequestphotodoc WHERE photodoc_photodocid = ? AND ceactionrequest_requestid = ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, blobID);
            stmt.setInt(2, requestID);
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println("BlobIntegrator.removePhotoCEARLink() | ERROR: "+ ex);
            throw new IntegrationException("Error deleting link. Photo-CEAR", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    public void removePhotoViolationsLink(int blobID, int violationID) throws IntegrationException {

        //property linker table
        String query = "DELETE"
                + "  FROM public.codeviolationphotodoc WHERE photodoc_photodocid = ? AND codeviolation_violationid = ?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, blobID);
            stmt.setInt(2, violationID);
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println("BlobIntegrator.removePhotoViolationsLink() | ERROR: "+ ex);
            throw new IntegrationException("Error deleting link. Photo-Violation", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    public void removePhotoMuniLink(int blobID, int muniCode) throws IntegrationException {

        //property linker table
        String query = "DELETE"
                + "  FROM public.muniphotodoc WHERE photodoc_photodocid = ? AND muni_municode = ?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, blobID);
            stmt.setInt(2, muniCode);
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println("BlobIntegrator.removePhotoMuniLink() | ERROR: "+ ex);
            throw new IntegrationException("Error deleting link. Photo-Muni", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    public void removePhotoInspectedSpaceElementLink(int blobID, int elementID) throws IntegrationException {

        //property linker table
        String query = "DELETE"
                + "  FROM public.occinspectedspaceelementphotodoc WHERE photodoc_photodocid = ? AND inspectedspaceelement_elementid = ?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, blobID);
            stmt.setInt(2, elementID);
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println("BlobIntegrator.removePhotoInspectedSpaceElementLink() | ERROR: "+ ex);
            throw new IntegrationException("Error deleting link. Photo-Muni", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    public void removePhotoOccPeriodLink(int blobID, int periodID) throws IntegrationException {

        //property linker table
        String query = "DELETE"
                + "  FROM public.occperiodphotodoc WHERE photodoc_photodocid = ? AND occperiod_periodid = ?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, blobID);
            stmt.setInt(2, periodID);
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println("BlobIntegrator.removePhotoOccPeriodLink() | ERROR: "+ ex);
            throw new IntegrationException("Error deleting link. Photo-Muni", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    public void linkPhotoBlobToActionRequest(int blobID, int requestID) throws IntegrationException{
        Connection con = getPostgresCon();
        String query =  " INSERT INTO public.ceactionrequestphotodoc(\n" +
                        "            photodoc_photodocid, ceactionrequest_requestid)\n" +
                        "    VALUES (?, ?);";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, blobID);
            stmt.setInt(2, requestID);
            
            stmt.execute();
            System.out.println("BlobIntegrator.linkBlobToActionRequest | link succesful. ");
            
        } catch (SQLException ex) {
            //System.out.println(ex);
            throw new IntegrationException("Error linking Blob to ActionRequest", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    public void linkPhotoBlobToCodeViolation(int blobID, int cvID) throws IntegrationException{
        Connection con = getPostgresCon();
        String query =  " INSERT INTO public.codeviolationphotodoc(\n" +
                        "            photodoc_photodocid, codeviolation_violationid)\n" +
                        "    VALUES (?, ?);";
        
        PreparedStatement stmt = null;
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, blobID);
            stmt.setInt(2, cvID);
            stmt.execute();
            System.out.println("BlobIntegrator.linkBlobToCodeViolation | link succesfull. ");
            
        } catch (SQLException ex) {
            //System.out.println(ex);
            throw new IntegrationException("Error linking Blob to CodeViolation", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    public void linkBlobToProperty(BlobLight bl, Property prop) throws IntegrationException{
        if(bl == null || prop == null){
            throw new IntegrationException("Cannot link blob to property with null prop or blob");
            
        }
        
        Connection con = getPostgresCon();
        String query =  " INSERT INTO public.propertyphotodoc(\n" +
                        "            photodoc_photodocid, property_propertyid)\n" +
                        "    VALUES (?, ?);";
        
        PreparedStatement stmt = null;
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, bl.getPhotoDocID());
            stmt.setInt(2, prop.getPropertyID());
            stmt.execute();
            System.out.println("BlobIntegrator.linkBlobToProperty | link succesfull. ");
            
        } catch (SQLException ex) {
            //System.out.println(ex);
            throw new IntegrationException("Error linking Blob to Property", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    public void linkPhotoBlobToPerson(int blobID, int personID) throws IntegrationException{
        Connection con = getPostgresCon();
        String query =  " INSERT INTO public.personphotodoc(\n" +
                        "            photodoc_photodocid, person_personid)\n" +
                        "    VALUES (?, ?);";
        
        PreparedStatement stmt = null;
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, blobID);
            stmt.setInt(2, personID);
            stmt.execute();
            System.out.println("BlobIntegrator.linkBlobToProperty | link succesfull. ");
            
        } catch (SQLException ex) {
            //System.out.println(ex);
            throw new IntegrationException("Error linking Blob to Person", ex);
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
    
    /**
     * Get the IDs of requests attached to a given photodoc
     * @param photodocID
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> requestsAttachedToPhoto(int photodocID) throws IntegrationException{
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT ceactionrequest_requestid FROM public.ceactionrequestphotodoc WHERE photodoc_photodocid = ?;";
        
        PreparedStatement stmt = null;
        
        List<Integer> idList = new ArrayList<>();
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, photodocID);
            rs = stmt.executeQuery();
            while(rs.next()){
                 idList.add(rs.getInt("ceactionrequest_requestid"));
            }
            
        } catch (SQLException ex) {
            //System.out.println(ex);
            throw new IntegrationException("Error retrieving attachment IDs. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return idList;
        
    }
    
    /**
     * Get the IDs of violations attached to a given photodoc
     * @param photodocID
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> violationsAttachedToPhoto(int photodocID) throws IntegrationException{
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT codeviolation_violationid FROM public.codeviolationphotodoc WHERE photodoc_photodocid = ?;";
        
        PreparedStatement stmt = null;
        
        List<Integer> idList = new ArrayList<>();
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, photodocID);
            rs = stmt.executeQuery();
            while(rs.next()){
                 idList.add(rs.getInt("codeviolation_violationid"));
            }
            
        } catch (SQLException ex) {
            //System.out.println(ex);
            throw new IntegrationException("Error retrieving attachment IDs. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return idList;
        
    }
    
    /**
     * Get the IDs of municipalities attached to a given photodoc
     * @param photodocID
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> munisAttachedToPhoto(int photodocID) throws IntegrationException{
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT muni_municode FROM public.muniphotodoc WHERE photodoc_photodocid = ?;";
        
        PreparedStatement stmt = null;
        
        List<Integer> idList = new ArrayList<>();
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, photodocID);
            rs = stmt.executeQuery();
            while(rs.next()){
                 idList.add(rs.getInt("muni_municode"));
            }
            
        } catch (SQLException ex) {
            //System.out.println(ex);
            throw new IntegrationException("Error retrieving attachment IDs. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return idList;
        
    }
    
    /**
     * Get the IDs of OccInspectedSpaceElements attached to a given photodoc
     * @param photodocID
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> elementsAttachedToPhoto(int photodocID) throws IntegrationException{
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT inspectedspaceelement_elementid FROM public.occinspectedspaceelementphotodoc WHERE photodoc_photodocid = ?;";
        
        PreparedStatement stmt = null;
        
        List<Integer> idList = new ArrayList<>();
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, photodocID);
            rs = stmt.executeQuery();
            while(rs.next()){
                 idList.add(rs.getInt("inspectedspaceelement_elementid"));
            }
            
        } catch (SQLException ex) {
            //System.out.println(ex);
            throw new IntegrationException("Error retrieving attachment IDs. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return idList;
        
    }
    
    /**
     * Get the IDs of OccPeriods attached to a given photodoc
     * @param photodocID
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> occPeriodsAttachedToPhoto(int photodocID) throws IntegrationException{
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT occperiod_periodid FROM public.occperiodphotodoc WHERE photodoc_photodocid = ?;";
        
        PreparedStatement stmt = null;
        
        List<Integer> idList = new ArrayList<>();
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, photodocID);
            rs = stmt.executeQuery();
            while(rs.next()){
                 idList.add(rs.getInt("occperiod_periodid"));
            }
            
        } catch (SQLException ex) {
            //System.out.println(ex);
            throw new IntegrationException("Error retrieving attachment IDs. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return idList;
        
    }
    
    /**
     * Get the IDs of Properties attached to a given photodoc
     * @param photodocID
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> propertiesAttachedToPhoto(int photodocID) throws IntegrationException{
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT property_propertyid FROM public.propertyphotodoc WHERE photodoc_photodocid = ?;";
        
        PreparedStatement stmt = null;
        
        List<Integer> idList = new ArrayList<>();
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, photodocID);
            rs = stmt.executeQuery();
            while(rs.next()){
                 idList.add(rs.getInt("property_propertyid"));
            }
            
        } catch (SQLException ex) {
            //System.out.println(ex);
            throw new IntegrationException("Error retrieving attachment IDs. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return idList;
        
    }
    
    /**
     * @param blobID the ID of the blob to be linked
     * @param target the ID of the property the blob will be linked to.
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void linkBlobToProperty(int blobID, int target) throws IntegrationException {
        
        Connection con = getPostgresCon();
        String query =  " INSERT INTO public.propertyphotodoc(photodoc_photodocid, property_propertyid)\n" +
                        "    VALUES (?, ?);";
        
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setInt(1, blobID);
            stmt.setInt(2, target);
            
            System.out.println("BlobIntegrator.linkBlobToProperty | Statement: " + stmt.toString());
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting blob-Property link. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * @param blobID the ID of the blob to be linked
     * @param target the ID of the CEAR the blob will be linked to.
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void linkBlobToActionRequest(int blobID, int target) throws IntegrationException {
        
        Connection con = getPostgresCon();
        String query =  " INSERT INTO public.ceactionrequestphotodoc(photodoc_photodocid, ceactionrequest_requestid)\n" +
                        "    VALUES (?, ?);";
        
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setInt(1, blobID);
            stmt.setInt(2, target);
            
            System.out.println("BlobIntegrator.linkBlobToActionRequest | Statement: " + stmt.toString());
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting blob-CEActionRequest link. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * @param blobID the ID of the blob to be linked
     * @param target the ID of the violation the blob will be linked to.
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void linkBlobToViolation(int blobID, int target) throws IntegrationException {
        
        Connection con = getPostgresCon();
        String query =  " INSERT INTO public.codeviolationphotodoc(photodoc_photodocid, codeviolation_violationid)\n" +
                        "    VALUES (?, ?);";
        
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setInt(1, blobID);
            stmt.setInt(2, target);
            
            System.out.println("BlobIntegrator.linkBlobToViolation | Statement: " + stmt.toString());
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting blob-CodeViolation link. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    /**
     * Connects a blob to a CECase
     * 
     * @param bl
     * @param cse
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void linkBlobToCECase(BlobLight bl, CECase cse) throws IntegrationException {
        
        if(bl == null || bl.getPhotoDocID() == 0 || cse == null || cse.getCaseID() == 0){
            throw new IntegrationException("Cannot link null or zero ID'd objects");
        }
        
        Connection con = getPostgresCon();
        String query =  "INSERT INTO public.cecasephotodoc(\n" +
                        "            photodoc_photodocid, cecase_caseid)\n" +
                        "    VALUES (?, ?);";
        
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setInt(1, bl.getPhotoDocID());
            stmt.setInt(2, cse.getCaseID());
            
            System.out.println("BlobIntegrator.linkBlobToCECase| Statement: " + stmt.toString());
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting blob-cecase link. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * @param blobID the ID of the blob to be linked
     * @param municode the code of the muni the blob will be linked to.
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void linkBlobToMuni(int blobID, int municode) throws IntegrationException {
        
        Connection con = getPostgresCon();
        String query =  " INSERT INTO public.muniphotodoc(photodoc_photodocid, muni_municode)\n" +
                        "    VALUES (?, ?);";
        
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setInt(1, blobID);
            stmt.setInt(2, municode);
            
            System.out.println("BlobIntegrator.linkBlobToMuni | Statement: " + stmt.toString());
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting blob-muni link. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * @param blobID the ID of the blob to be linked
     * @param target the ID of the Inspected Space Element the blob will be linked to.
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void linkBlobToInspectedSpaceElement(int blobID,  int target) throws IntegrationException {
        
        Connection con = getPostgresCon();
        String query =  " INSERT INTO public.occinspectedspaceelementphotodoc(photodoc_photodocid, occinspectedspaceelementphotodoc)\n" +
                        "    VALUES (?, ?);";
        
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setInt(1, blobID);
            stmt.setInt(2, target);
            
            System.out.println("BlobIntegrator.linkBlobToInspectedSpaceElement | Statement: " + stmt.toString());
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting blob-OccInspectedSpaceElement link. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * @param blobID the ID of the blob to be linked
     * @param target the ID of the OccPeriod the blob will be linked to.
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void linkBlobToOccPeriod(int blobID,  int target) throws IntegrationException {
        
        Connection con = getPostgresCon();
        String query =  " INSERT INTO public.occperiodphotodoc(photodoc_photodocid, occperiodphotodoc__occperiod_fk)\n" +
                        "    VALUES (?, ?);";
        
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setInt(1, blobID);
            stmt.setInt(2, target);
            
            System.out.println("BlobIntegrator.linkBlobToOccPeriod | Statement: " + stmt.toString());
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting blob-linkBlobToOccPeriod link. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * Get the IDs of photos attached to a given request
     * @param requestID
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> photosAttachedToRequest(int requestID) throws IntegrationException{
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT photodoc_photodocid FROM public.ceactionrequestphotodoc WHERE ceactionrequest_requestid = ?;";
        
        PreparedStatement stmt = null;
        
        List<Integer> idList = new ArrayList<>();
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, requestID);
            rs = stmt.executeQuery();
            while(rs.next()){
                 idList.add(rs.getInt("photodoc_photodocid"));
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
     * Get the IDs of photos attached to a given violation
     * @param cse
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> getBlobIDs(CECase cse) throws IntegrationException{
        
        if(cse == null || cse.getCaseID() == 0){
            throw new IntegrationException("Cannot fetch blobs by case with null case or id = 0");
        }
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT photodoc_photodocid FROM public.cecasephotodoc WHERE cecase_caseid = ?;";
        
        PreparedStatement stmt = null;
        
        List<Integer> idList = new ArrayList<>();
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, cse.getCaseID());
            rs = stmt.executeQuery();
            while(rs.next()){
                 idList.add(rs.getInt("photodoc_photodocid"));
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
     * Get the IDs of photos attached to a given violation
     * @param violationID
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> getblobsByViolation(int violationID) throws IntegrationException{
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT photodoc_photodocid FROM public.codeviolationphotodoc WHERE codeviolation_violationid = ?;";
        
        PreparedStatement stmt = null;
        
        List<Integer> idList = new ArrayList<>();
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, violationID);
            rs = stmt.executeQuery();
            while(rs.next()){
                 idList.add(rs.getInt("photodoc_photodocid"));
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
     * Get the IDs of photos attached to a given muni
     * @param municode
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> photosAttachedToMuni(int municode) throws IntegrationException{
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT photodoc_photodocid FROM public.muniphotodoc WHERE muni_municode = ?;";
        
        PreparedStatement stmt = null;
        
        List<Integer> idList = new ArrayList<>();
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, municode);
            rs = stmt.executeQuery();
            while(rs.next()){
                 idList.add(rs.getInt("photodoc_photodocid"));
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
     * Get the IDs of photos attached to a given inspected space element
     * @param elementID
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> photosAttachedToInspectedSpaceElement(int elementID) throws IntegrationException{
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT photodoc_photodocid FROM public.occinspectedspaceelementphotodoc WHERE inspectedspaceelement_elementid = ?;";
        
        PreparedStatement stmt = null;
        
        List<Integer> idList = new ArrayList<>();
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, elementID);
            rs = stmt.executeQuery();
            while(rs.next()){
                 idList.add(rs.getInt("photodoc_photodocid"));
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
     * Get the IDs of photos attached to a given occ period
     * @param periodID
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> photosAttachedToOccPeriod(int periodID) throws IntegrationException{
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT photodoc_photodocid FROM public.occperiodphotodoc WHERE occperiod_periodid = ?;";
        
        PreparedStatement stmt = null;
        
        List<Integer> idList = new ArrayList<>();
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, periodID);
            rs = stmt.executeQuery();
            while(rs.next()){
                 idList.add(rs.getInt("photodoc_photodocid"));
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
     * Get the IDs of photos attached to a given property
     * @param prop
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> getBlobIDs(Property prop) throws IntegrationException, BObStatusException{
        
        if(prop == null){
            throw new BObStatusException("Cannot get BlobIDs by property with null Prop");
            
        }
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT photodoc_photodocid FROM public.propertyphotodoc WHERE property_propertyid = ?;";
        
        PreparedStatement stmt = null;
        
        List<Integer> idList = new ArrayList<>();
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, prop.getPropertyID());
            rs = stmt.executeQuery();
            while(rs.next()){
                 idList.add(rs.getInt("photodoc_photodocid"));
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
    
}