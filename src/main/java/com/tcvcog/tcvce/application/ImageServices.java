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
        FacesContext context = FacesContext.getCurrentInstance();
        
        if(context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            return new DefaultStreamedContent();
        } else {
            int photoID = Integer.parseInt(context.getExternalContext().getRequestParameterMap().get("photoID"));
            return new DefaultStreamedContent(
                    new ByteArrayInputStream(getPhotograph(photoID).getPhotoBytes()));
        }
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
            throw new IntegrationException("Error inserting new person", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return ph;
    }
    
    
    public void storePhotograph(Photograph ph) throws IntegrationException{
            System.out.println("ImageServices.storePhotograph");
        Connection con = getPostgresCon();
        String query =  " INSERT INTO public.photodoc(\n" +
                        "            photodocid, photodocdescription, photodocdate, photodoctype_typeid, \n" +
                        "            photodocblob)\n" +
                        "    VALUES (?, ?, ?, ?, \n" +
                        "            ?);";
        
        PreparedStatement stmt = null;
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ph.getPhotoID());
            stmt.setString(2, ph.getDescription());
            stmt.setTimestamp(3, java.sql.Timestamp.from(ph.getTimeStamp()
                    .atZone(ZoneId.systemDefault()).toInstant()));
            stmt.setInt(4, ph.getTypeID());
            stmt.setBytes(5, ph.getPhotoBytes());
            
            System.out.println("ImageServices.storePhotograph | Statement: " + stmt.toString());
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error inserting person", ex);
        } finally{
             if (stmt != null){ try { stmt.close(); } catch (SQLException ex) {/* ignored */ } }
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    
}
