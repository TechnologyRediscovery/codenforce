/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.Photograph;
import com.tcvcog.tcvce.entities.User;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneId;
import java.util.ArrayList;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author noah
 */
public class ImageServices extends BackingBeanUtils implements Serializable{

     
    /**
     * Creates a new instance of ImageServices
     */
    public ImageServices() {
    }
    
    public StreamedContent getImage() throws IOException, IntegrationException{
        System.out.println("ImageServices | getImage: in method");
        FacesContext context = FacesContext.getCurrentInstance();
        DefaultStreamedContent sc =null;
        
        if(context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            sc = new DefaultStreamedContent();
            return sc;
        } else {
            int photoID = Integer.parseInt(context.getExternalContext().getRequestParameterMap().get("photoID"));
            
            sc = new DefaultStreamedContent(new ByteArrayInputStream(getPhotograph(photoID).getPhotoBytes()), "image/png", Integer.toString(photoID));
            return sc;
        }
    }
    
    public void deletePhotograph(int photoID) throws IntegrationException{
        // TODO: delete from linker tables as they are added
        
        //actionrequest linker table
        Connection con = getPostgresCon();
        String query = "DELETE" +
                        "  FROM public.ceactionrequestphotodoc WHERE photodoc_photodocid = ?;";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, photoID);
            stmt.executeQuery();
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error deleting photo", ex);
        }
        
        //violation linker table
        query = "DELETE FROM public.codeviolationphotodoc WHERE photodoc_photodocid = ?";
        stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, photoID);
            stmt.executeQuery();
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error deleting photo", ex);
        }
        
        //delete the main photodoc entry
        query = "DELETE FROM public.photodoc WHERE photodocid = ?;";
        
        stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, photoID);
            stmt.executeQuery();
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error deleting photo", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    public Photograph getPhotograph(int photoID) throws IntegrationException{
        Photograph ph = null;
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT photodocid, photodocdescription, photodocdate, photodoctype_typeid, \n" +
                        "       photodocblob\n" +
                        "  FROM public.photodoc WHERE photodocid = ?;";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, photoID);
            rs = stmt.executeQuery();
            while(rs.next()){
                ph = new Photograph();
                System.out.println("ImageServices.getPhotograph: | retrieving photoID "  + photoID);
                ph.setPhotoID(rs.getInt("photodocid"));
                ph.setDescription(rs.getString("photodocdescription"));
                ph.setTimeStamp(rs.getTimestamp("photodocdate").toLocalDateTime());
                ph.setTypeID(rs.getInt("photodoctype_typeid"));
                ph.setPhotoBytes(rs.getBytes("photodocblob"));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting new photo", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return ph;
    }
    
    public ArrayList<Photograph> getAllPhotographs() throws IntegrationException{
        ArrayList<Photograph> phList = new ArrayList<>();
        Photograph ph = null;
        Connection con = getPostgresCon();
        ResultSet rs = null;
        String query = "SELECT photodocid, photodocdescription, photodocdate, photodoctype_typeid, " +
                        "photodocblob\n" +
                        "  FROM public.photodoc;";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while(rs.next()){
                ph = new Photograph();
                ph.setPhotoID(rs.getInt("photodocid"));
                ph.setDescription(rs.getString("photodocdescription"));
                ph.setTimeStamp(rs.getTimestamp("photodocdate").toLocalDateTime());
                ph.setTypeID(rs.getInt("photodoctype_typeid"));
                ph.setPhotoBytes(rs.getBytes("photodocblob"));
                System.out.println("ImageServices.getPhotograph: | retrieved photoID "  + ph.getPhotoID());
                phList.add(ph);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error retrieving all photos", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        return phList;
    }
    
    public void linkPhotoToActionRequest(int photoID, int requestID) throws IntegrationException{
        Connection con = getPostgresCon();
        String query =  " INSERT INTO public.ceactionrequestphotodoc(\n" +
                        "            photodoc_photodocid, ceactionrequest_requestid)\n" +
                        "    VALUES (?, ?);";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, photoID);
            stmt.setInt(2, requestID);
            
            stmt.execute();
            System.out.println("ImageServices.linkPhotoToActionRequest | link succesful!");
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error linking photo to action request", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * 
     * @param ph photograph to be stored
     * @return photID the photoID of the newly inserted photo
     * @throws IntegrationException 
     */
    public int storePhotograph(Photograph ph) throws IntegrationException{
        Connection con = getPostgresCon();
        String query =  " INSERT INTO public.photodoc(\n" +
                        "            photodocid, photodocdescription, photodocdate, photodoctype_typeid, \n" +
                        "            photodocblob, photodoccommitted)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, \n" +
                        "            ?, ?);";
        
        PreparedStatement stmt = null;
        
        int lastID = 0;

        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setString(1, ph.getDescription());
            stmt.setTimestamp(2, java.sql.Timestamp.from(ph.getTimeStamp()
                    .atZone(ZoneId.systemDefault()).toInstant()));
            stmt.setInt(3, ph.getTypeID());
            
            stmt.setBytes(4, ph.getPhotoBytes());
            stmt.setBoolean(5, false);
            
            System.out.println("ImageServices.storePhotograph | Statement: " + stmt.toString());
            stmt.execute();
            
            String idNumQuery = "SELECT currval('photodoc_photodocid_seq');";
            Statement s = con.createStatement();
            ResultSet rs;
            rs = s.executeQuery(idNumQuery);
            rs.next();
            lastID = rs.getInt(1);
            ph.setPhotoID(lastID);
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting photo", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        return lastID;
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
    
    public void updatePhotoDescription(Photograph photo) throws IntegrationException{
        Connection con = getPostgresCon();
        String query =  " UPDATE public.photodoc\n" +
                        " SET photodocdescription = ?\n" +
                        " WHERE photodocid = ?;";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, photo.getDescription());
            stmt.setInt(2, photo.getPhotoID());
            
            System.out.println("ImageServices.updatePhotoDescription | Statement: " + stmt.toString());
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error updating photo description", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    public void linkPhotoToActionRequest(Photograph ph, CEActionRequest ar) throws IntegrationException{
        //Store Photograph first please 
        Connection con = getPostgresCon();
        String query =  " INSERT INTO public.ceactionrequestphotodoc(\n" +
                        "            photodoc_photodocid, ceactionrequest_requestid)\n" +
                        "    VALUES (?, ?);";
        
        PreparedStatement stmt = null;
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ph.getPhotoID());
            stmt.setInt(2, ar.getRequestID());
            System.out.println("ImageServices.linkPhotoToActionRequest | Statement: " + stmt.toString());
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error linking photo to actionrequest", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    public void linkPhotoToCodeViolation(int photoID, int cvID) throws IntegrationException{
        //Store Photograph first please 
        Connection con = getPostgresCon();
        String query =  " INSERT INTO public.codeviolationphotodoc(\n" +
                        "            photodoc_photodocid, codeviolation_violationid)\n" +
                        "    VALUES (?, ?);";
        
        PreparedStatement stmt = null;
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, photoID);
            stmt.setInt(2, cvID);
            System.out.println("ImageServices.linkPhotoToCodeViolation | Statement: " + stmt.toString());
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error linking photo to code violation", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    
    public void linkPhotoToMuni(int photoID, int muniCode) throws IntegrationException{
        //Store Photograph first please 
        Connection con = getPostgresCon();
        String query =  " INSERT INTO public.muniphotodoc(\n" +
                        "            photodoc_photodocid, muni_municode)\n" +
                        "    VALUES (?, ?);";
        
        PreparedStatement stmt = null;
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, photoID);
            stmt.setInt(2, muniCode);
            System.out.println("ImageServices.linkPhotoToMuni| Statement: " + stmt.toString());
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error linking photo to muni", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
}
