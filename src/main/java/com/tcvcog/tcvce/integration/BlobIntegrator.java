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

/**
 *
 * @author noah
 */
public class BlobIntegrator extends BackingBeanUtils implements Serializable{
    
    /**
     * 
     * @param blobID the blobID of the blob to be retrieved from db
     * @return the blob pulled from the db
     * @throws IntegrationException thrown instead of SCLException
     */
    public BlobLight getPhotoBlobLight(int blobID) throws IntegrationException{
        BlobLight blob = null;
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT photodocid, photodocdescription, photodocdate, photodoctype_typeid, photodocfilename, \n" +
                        "       photodocblob, \n" +
                        "       photodocuploadpersonid \n" +
                        "  FROM public.photodoc WHERE photodocid = ?;";
        
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
    
    public BlobLight generatePhotoBlobLight(ResultSet rs) throws SQLException{
        BlobLight blob = new BlobLight();
        blob.setBlobID(rs.getInt("photodocid"));
        blob.setDescription(rs.getString("photodocdescription"));
        blob.setTimestamp(rs.getTimestamp("photodocdate").toLocalDateTime());
        blob.setType(BlobType.blobTypeFromInt(rs.getInt("photodoctype_typeid")));
        blob.setFilename(rs.getString("photodocfilename"));

        //blob.setBytes(rs.getBytes("photodocblob"));
        blob.setUploadPersonID(rs.getInt("photodocuploadpersonid"));
        return blob;
    }
    
    /**
     * Gets the bytes 
     * @param blobID
     * @return
     * @throws IntegrationException 
     */
    public byte[] getBlobBytes(int blobID) throws IntegrationException{
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT photodocblob FROM public.photodoc WHERE photodocid = ?;";
        
        PreparedStatement stmt = null;
        
        byte[] blobBytes = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, blobID);
            rs = stmt.executeQuery();
            while(rs.next()){
                blobBytes = rs.getBytes("photodocblob");
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
    
    /**
     * 
     * @return list of Blob IDs for all photos uploaded in the past month
     * @throws IntegrationException 
     */
    public List<Integer> getRecentPhotoBlobs() throws IntegrationException{
        ArrayList<Integer> blobIDList = new ArrayList();
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT photodocid FROM public.photodoc WHERE photodocdate > ?;";
        
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
     * stores this photo blob in the db
     * @param blob the blob to be stored
     * @return the blobID of the newly stored blob
     * @throws com.tcvcog.tcvce.domain.BlobException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public int storePhotoBlob(Blob blob) throws BlobException, IntegrationException{
        if(blob.getType() == null) throw new BlobTypeException("Attempted to store a blob with null type. ");
        // TODO: validate BLOB's and throw exception if corrupted
        
        Connection con = getPostgresCon();
        String query =  " INSERT INTO public.photodoc(\n" +
                        "            photodocid, photodocdescription, photodocdate, photodoctype_typeid, photodocuploadpersonid, photodocfilename, \n" +
                        "            photodocblob)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, ?, \n" +
                        "            ?, ?);";
        
        PreparedStatement stmt = null;
        
        int lastID = 0;

        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setString(1, blob.getDescription());
            stmt.setTimestamp(2, java.sql.Timestamp.from(blob.getTimestamp()
                    .atZone(ZoneId.systemDefault()).toInstant()));
            stmt.setInt(3, blob.getType().getTypeID());
            stmt.setInt(4, blob.getUploadPersonID());
            stmt.setString(5, blob.getFilename());
            
            stmt.setBytes(6, blob.getBytes());
            
            System.out.println("BlobIntegrator.storeBlob | Statement: " + stmt.toString());
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
     * Since many values in the Blob sphere shouldn't be changed after uploaded,
     * this method only updates the filename and description values.
     * @param blob the blob to be updated
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void updatePhotoBlobDescriptors(BlobLight blob) throws  IntegrationException{
        
        Connection con = getPostgresCon();
        String query =  " UPDATE public.photodoc\n" +
                        " SET photodocdescription=?, photodocfilename=?\n" +
                        " WHERE photodocid=?;";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setString(1, blob.getDescription());
            stmt.setString(2, blob.getFilename());
            
            stmt.setInt(3, blob.getBlobID());
            
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
     * removes this blob from the db, as well as any rows associated with this blob in linker tables.
     * @param blobID the blobID of the blob to be removed
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
