/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Photograph;
import com.tcvcog.tcvce.entities.User;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author sylvia
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
            return new DefaultStreamedContent();
        } else {
            int photoID = Integer.parseInt(context.getExternalContext().getRequestParameterMap().get("photoID"));
            
            sc = new DefaultStreamedContent(new ByteArrayInputStream(getPhotograph(photoID).getPhotoBytes()), "image/png", Integer.toString(photoID));
            return sc;
        }
    }
    
    public void deletePhotograph(int photoID) throws IntegrationException{
        // TODO: delete entry in property helper table
        Connection con = getPostgresCon();
        String query = "DELETE" +
                        "  FROM public.photodoc WHERE photodocid = ?;";
        
        PreparedStatement stmt = null;
        
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, photoID);
            stmt.executeQuery();
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting new photo", ex);
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
    
    public void storePhotograph(Photograph ph) throws IntegrationException{
            System.out.println("ImageServices.storePhotograph");
        Connection con = getPostgresCon();
        String query =  " INSERT INTO public.photodoc(\n" +
                        "            photodocid, photodocdescription, photodocdate, photodoctype_typeid, \n" +
                        "            photodocblob)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, \n" +
                        "            ?);";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setString(1, ph.getDescription());
            stmt.setTimestamp(2, java.sql.Timestamp.from(ph.getTimeStamp()
                    .atZone(ZoneId.systemDefault()).toInstant()));
            stmt.setInt(3, ph.getTypeID());
            stmt.setBytes(4, ph.getPhotoBytes());
            
            System.out.println("ImageServices.storePhotograph | Statement: " + stmt.toString());
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting photo", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    
}
