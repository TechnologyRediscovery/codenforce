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
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.BlobTypeException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.BlobLight;
import com.tcvcog.tcvce.entities.BlobType;
import com.tcvcog.tcvce.entities.Metadata;
import com.tcvcog.tcvce.entities.MetadataKey;
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
     * If you need to grab a Blob anywhere else
     * @param blobID the blobID of the meta to be retrieved from db
     * @return the meta pulled from the db
     * @throws IntegrationException thrown instead of SCLException
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     * @throws com.tcvcog.tcvce.domain.BlobException
     */
    public BlobLight getPhotoBlobLight(int blobID) throws IntegrationException, IOException, ClassNotFoundException, BlobException{
        BlobLight blob = null;
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT photodocid, photodocdescription, photodoccommitted, blobbytes_bytesid, muni_municode, \n"
                + "uploaddate, blobtype_typeid, uploadpersonid, filename, metadatamap\n"
                + "FROM public.photodoc LEFT JOIN blobbytes on blobbytes_bytesid = bytesid\n"
                + "WHERE photodocid = ?;";
        
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, blobID);
            rs = stmt.executeQuery();
            while(rs.next()){
                System.out.println("BlobIntegrator.getBlob: | retrieving blobID "  + blobID);
                blob = generatePhotoBlobLight(rs);
            }
            
        } catch (SQLException ex) {
            //System.out.println(ex);
            throw new IntegrationException("Error retrieving blob. ", ex);
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
     * @throws IntegrationException thrown instead of SCLException
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     * @throws com.tcvcog.tcvce.domain.BlobException
     */
    public BlobLight getPhotoBlobLightWithoutMetadata(int blobID) throws IntegrationException, IOException, ClassNotFoundException {
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
                System.out.println("BlobIntegrator.getBlob: | retrieving blobID "  + blobID);
                blob = generatePhotoBlobLightWithoutMetadata(rs);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex);
            //System.out.println(ex);
            throw new IntegrationException("Error retrieving blob. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return blob;
        
    }
    
    private BlobLight generatePhotoBlobLight(ResultSet rs) throws SQLException, IOException, ClassNotFoundException, BlobException{
        BlobLight blob = new BlobLight();
        blob.setBlobID(rs.getInt("photodocid"));
        blob.setBytesID(rs.getInt("blobbytes_bytesid"));
        blob.setDescription(rs.getString("photodocdescription"));
        
        Timestamp time = rs.getTimestamp("uploaddate");
        if(time != null){
            blob.setTimestamp(time.toLocalDateTime());
        }
        blob.setType(BlobType.blobTypeFromInt(rs.getInt("blobtype_typeid")));
        blob.setFilename(rs.getString("filename"));
        blob.setUploadPersonID(rs.getInt("uploadpersonid"));
        blob.setMunicode(rs.getInt("muni_municode"));
        
        blob.setBlobMetadata(generateBlobMetadata(rs));
        return blob;
    }
    
     private BlobLight generatePhotoBlobLightWithoutMetadata(ResultSet rs) throws SQLException, IOException, ClassNotFoundException {
        BlobLight blob = new BlobLight();
        blob.setBlobID(rs.getInt("photodocid"));
        blob.setBytesID(rs.getInt("blobbytes_bytesid"));
        blob.setDescription(rs.getString("photodocdescription"));
        Timestamp time = rs.getTimestamp("uploaddate");
        if(time != null){
            blob.setTimestamp(time.toLocalDateTime());
        }
        blob.setType(BlobType.blobTypeFromInt(rs.getInt("blobtype_typeid")));
        blob.setFilename(rs.getString("filename"));
        blob.setUploadPersonID(rs.getInt("uploadpersonid"));
        blob.setMunicode(rs.getInt("muni_municode"));
        
        return blob;
    }
    
    /**
     * Gets the bytes 
     * @param bytesID
     * @return
     * @throws IntegrationException 
     */
    public byte[] getBlobBytes(int bytesID) throws IntegrationException{
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT blob FROM public.blobbytes WHERE bytesid = ?;";
        
        PreparedStatement stmt = null;
        
        byte[] blobBytes = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, bytesID);
            rs = stmt.executeQuery();
            while(rs.next()){
                blobBytes = rs.getBytes("blob");
            }
            
        } catch (SQLException ex) {
            //System.out.println(ex);
            throw new IntegrationException("Error retrieving blob bytes. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return blobBytes;
        
    } 
    
    public Metadata generateBlobMetadata(ResultSet rs) throws SQLException, IOException, ClassNotFoundException, BlobException{
        Metadata meta = new Metadata();
        meta.setBytesID(rs.getInt("blobbytes_bytesid"));
        meta.setType(BlobType.blobTypeFromInt(rs.getInt("blobtype_typeid")));
        
        // We must now convert the byte array to an object
        
        byte[] mapBytes = rs.getBytes("metadatamap");
        
        try {
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(mapBytes));
        
        meta.replaceDataMap((Map<MetadataKey, String>) in.readObject());
        
        } catch (NullPointerException ex){
            throw new BlobException("The metadata column of blobbytes_bytesid = " 
                                    + meta.getBytesID() + " is null. It is recommended "
                                    + "to strip the metadata of the image and "
                                    + "populate the column before fetching it again");
            
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
     * stores this photo meta in the db
     * @param blob the meta to be stored
     * @return the blobID of the newly stored meta
     * @throws com.tcvcog.tcvce.domain.BlobException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws java.io.IOException
     */
    public Blob storePhotoBlob(Blob blob) throws BlobException, IntegrationException, IOException{
        
        Connection con = getPostgresCon();
        String query =  " INSERT INTO public.photodoc(photodocid, photodocdescription, blobbytes_bytesid, muni_municode)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?);";
        
        PreparedStatement stmt = null;
        
        int lastID = 0;

        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setString(1, blob.getDescription());
            
            int bytesID = storeBlobBytes(blob);
            
            stmt.setInt(2, bytesID);
            
            stmt.setInt(3, blob.getMunicode());
            
            System.out.println("BlobIntegrator.storePhotoBlob | Statement: " + stmt.toString());
            stmt.execute();
            
            String idNumQuery = "SELECT currval('photodoc_photodocid_seq');";
            Statement s = con.createStatement();
            ResultSet rs;
            rs = s.executeQuery(idNumQuery);
            rs.next();
            lastID = rs.getInt(1);
            
            //set the IDs so we can throw the blob back and they can access the blob and bytes
            blob.setBlobID(lastID);
            blob.setBytesID(bytesID);
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting blob. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        return blob;
    }
    
    /**
     * stores the bytes of a meta in the db
     * @param blob the meta to be stored
     * @return the blobID of the newly stored meta
     * @throws com.tcvcog.tcvce.domain.BlobException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws java.io.IOException
     */
    public int storeBlobBytes(Blob blob) throws BlobException, IntegrationException, IOException{
        if(blob.getType() == null) throw new BlobTypeException("Attempted to store a blob with null type. ");
        
        Connection con = getPostgresCon();
        String query =  " INSERT INTO public.blobbytes(\n" +
                        "            bytesid, uploaddate, blobtype_typeid, \n" +
                        "            blob, uploadpersonid, filename, metadatamap)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, ?, ?, ?);";
        
        PreparedStatement stmt = null;

        List<Integer> existingBlobs = checkBytes(blob.getBytes());
        
        if(!existingBlobs.isEmpty()){
            //The file is already in our database, link it to the existing file.
            return existingBlobs.get(existingBlobs.size() - 1);
        } else {
            try {

                stmt = con.prepareStatement(query);
                stmt.setTimestamp(1, java.sql.Timestamp.from(blob.getTimestamp()
                        .atZone(ZoneId.systemDefault()).toInstant()));
                stmt.setInt(2, blob.getType().getTypeID());
                stmt.setBytes(3, blob.getBytes());
                stmt.setInt(4, blob.getUploadPersonID());
                stmt.setString(5, blob.getFilename());
                stmt.setBytes(6, blob.getBlobMetadata().getMapBytes());

                System.out.println("BlobIntegrator.storeBlobBytes | Statement: " + stmt.toString());
                stmt.execute();

                String idNumQuery = "SELECT currval('blobbytes_seq');";
                Statement s = con.createStatement();
                ResultSet rs;
                rs = s.executeQuery(idNumQuery);
                rs.next();
                blob.setBlobID(rs.getInt(1));
                
                return blob.getBlobID();

            } catch (SQLException ex) {
                System.out.println(ex);
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
    public List<Integer> checkBytes(byte[] proposedBytes) throws IntegrationException{
        
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
    public void updatePhotoBlobDescription(BlobLight blob) throws  IntegrationException{
        
        Connection con = getPostgresCon();
        String query = " UPDATE public.photodoc\n"
                + " SET photodocdescription=?\n"
                + " WHERE photodocid=?;\n\n";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setString(1, blob.getDescription());            
            stmt.setInt(2, blob.getBlobID());
            
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
     * This method updates a blob's filename.
     * NOT SAFE. Use the Coordinator method, it makes sure that the correct 
     * file extension is at the end of the filename.
     * @param blob the meta to be updated
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void updatePhotoBlobFilename(BlobLight blob) throws  IntegrationException{
        
        Connection con = getPostgresCon();
        String query = " UPDATE public.blobbytes\n"
                + " SET filename=?\n"
                + " WHERE bytesid=?;\n\n";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setString(1, blob.getFilename());            
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
     * Updates the metadata and filename of a blobbytes entry.
     * @param blob the meta to be updated
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws java.io.IOException
     */
    public void updateBlobMetadata(BlobLight blob) throws  IntegrationException, IOException{
        
        Connection con = getPostgresCon();
        String query = "UPDATE public.blobbytes\n"
                + " SET filename=?, metadatamap=?\n"
                + " WHERE bytesid=?;";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setString(1, blob.getFilename());
            stmt.setBytes(2, blob.getBlobMetadata().getMapBytes());
            stmt.setInt(3, blob.getBytesID());
            
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
     * Use the method on the coordinator, it is safer - checks for connections first.
     * @param blobID the blob to be removed
     * @throws IntegrationException thrown instead of a SQLException
     */
    public void deletePhotoBlob(int blobID) throws IntegrationException{
        
        //delete the main photodoc entry
        String query = "DELETE FROM public.photodoc WHERE photodocid = ?;";
        
        PreparedStatement stmt = null;
        Connection con = getPostgresCon();
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, blobID);
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
            stmt.executeUpdate();
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
    
    public void removePhotoPropertyLink(int blobID, int propertyID) throws IntegrationException {

        //property linker table
        String query = "DELETE"
                + "  FROM public.propertyphotodoc WHERE photodoc_photodocid = ? AND property_propertyid = ?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, blobID);
            stmt.setInt(2, propertyID);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("BlobIntegrator.removePhotoPropertyLink() | ERROR: "+ ex);
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
            stmt.executeUpdate();
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
            stmt.executeUpdate();
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
            stmt.executeUpdate();
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
            stmt.executeUpdate();
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
            stmt.executeUpdate();
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
    
    public void linkPhotoBlobToProperty(int blobID, int propertyID) throws IntegrationException{
        Connection con = getPostgresCon();
        String query =  " INSERT INTO public.propertyphotodoc(\n" +
                        "            photodoc_photodocid, property_propertyid)\n" +
                        "    VALUES (?, ?);";
        
        PreparedStatement stmt = null;
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, blobID);
            stmt.setInt(2, propertyID);
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
    
}