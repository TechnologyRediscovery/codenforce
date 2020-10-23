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
     * 
     * @param blobID the blobID of the meta to be retrieved from db
     * @return the meta pulled from the db
     * @throws IntegrationException thrown instead of SCLException
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    public BlobLight getPhotoBlobLight(int blobID) throws IntegrationException, IOException, ClassNotFoundException{
        BlobLight blob = null;
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT photodocid, photodocdescription, photodoccommitted, blobbytes_bytesid, \n"
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
    
    public BlobLight generatePhotoBlobLight(ResultSet rs) throws SQLException, IOException, ClassNotFoundException{
        BlobLight blob = new BlobLight();
        blob.setBlobID(rs.getInt("photodocid"));
        blob.setBytesID(rs.getInt("blobbytes_bytesid"));
        blob.setDescription(rs.getString("photodocdescription"));
        blob.setTimestamp(rs.getTimestamp("uploaddate").toLocalDateTime());
        blob.setType(BlobType.blobTypeFromInt(rs.getInt("blobtype_typeid")));
        blob.setFilename(rs.getString("filename"));
        blob.setUploadPersonID(rs.getInt("uploadpersonid"));
        
        blob.setBlobMetadata(generateBlobMetadata(rs));
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
    
    public Metadata generateBlobMetadata(ResultSet rs) throws SQLException, IOException, ClassNotFoundException{
        Metadata meta = new Metadata();
        meta.setBytesID(rs.getInt("blobbytes_bytesid"));
        meta.setType(BlobType.blobTypeFromInt(rs.getInt("blobtype_typeid")));
        
        // We must now convert the byte array to an object
        
        byte[] mapBytes = rs.getBytes("metadatamap");
        
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(mapBytes));
        
        meta.replaceDataMap((Map<MetadataKey, String>) in.readObject());
        
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
    public int storePhotoBlob(Blob blob) throws BlobException, IntegrationException, IOException{
        
        Connection con = getPostgresCon();
        String query =  " INSERT INTO public.photodoc(photodocid, photodocdescription, blobbytes_bytesid)\n" +
                        "    VALUES (DEFAULT, ?, ?);";
        
        PreparedStatement stmt = null;
        
        int lastID = 0;

        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setString(1, blob.getDescription());
            
            int bytesID = storeBlobBytes(blob);
            
            stmt.setInt(2, bytesID);
            
            System.out.println("BlobIntegrator.storePhotoBlob | Statement: " + stmt.toString());
            stmt.execute();
            
            String idNumQuery = "SELECT currval('photodoc_photodocid_seq');";
            Statement s = con.createStatement();
            ResultSet rs;
            rs = s.executeQuery(idNumQuery);
            rs.next();
            lastID = rs.getInt(1);
            blob.setBlobID(lastID);
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting blob. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        return lastID;
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

                String idNumQuery = "SELECT currval('photodoc_photodocid_seq');";
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
                + " SET photodocdescription=?,\n"
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
     * removes this meta from the db, as well as any rows associated with this meta in linker tables.
     * TODO: Add functionality to delete bytes too?
     * @param blobID the blobID of the meta to be removed
     * @throws IntegrationException thrown instead of a SQLException
     */
    public void deletePhotoBlob(int blobID) throws IntegrationException{
        // TODO: delete from linker tables as they are added
        
        //actionrequest linker table
        Connection con = getPostgresCon();
        String query = "DELETE" +
                        "  FROM public.ceactionrequestphotodoc WHERE photodoc_photodocid = ?;";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, blobID);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("BlobIntegrator.deleteBlob() | ERROR: "+ ex);
            throw new IntegrationException("Error deleting link. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        
        //violation linker table
        query = "DELETE FROM public.codeviolationphotodoc WHERE photodoc_photodocid = ?";
        stmt = null;
        con = getPostgresCon();
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, blobID);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("BlobIntegrator.deleteBlob() | ERROR: "+ ex);
            throw new IntegrationException("Error deleting link. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        
        //property linker table
        query = "DELETE" +
                        "  FROM public.propertyphotodoc WHERE photodoc_photodocid = ?;";
        
        stmt = null;
        con = getPostgresCon();
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, blobID);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("BlobIntegrator.deleteBlob() | ERROR: "+ ex);
            throw new IntegrationException("Error deleting link. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        //person linker table - which doesn't exist at the moment, so this code is commented out.
        /*
        query = "DELETE" +
                        "  FROM public.personphotodoc WHERE photodoc_photodocid = ?;";
        
        stmt = null;
        con = getPostgresCon();
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, blobID);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("BlobIntegrator.deletePhotoBlob() | ERROR: "+ ex);
            throw new IntegrationException("Error deleting link. ", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {ignored} }
             if (con != null) { try { con.close(); } catch (SQLException e) { ignored } }
        } // close finally
        */
        
        //delete the main photodoc entry
        query = "DELETE FROM public.photodoc WHERE photodocid = ?;";
        
        stmt = null;
        con = getPostgresCon();
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, blobID);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("BlobIntegrator.deleteBlob() | ERROR: "+ ex);
            throw new IntegrationException("Error deleting blob. ", ex);
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
    
}
