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
import com.tcvcog.tcvce.application.interfaces.IFace_Loggable;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.BOBSource;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.CasePhaseEnum;
import com.tcvcog.tcvce.entities.Icon;
import com.tcvcog.tcvce.entities.ImprovementSuggestion;
import com.tcvcog.tcvce.entities.IntensityClass;
import com.tcvcog.tcvce.entities.IntensitySchema;
import com.tcvcog.tcvce.entities.ListChangeRequest;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PrintStyle;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPermit;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class SystemIntegrator extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of SystemIntegrator
     */
    public SystemIntegrator() {
    }

    public Map<String, Integer> getPrintStyleMap() throws IntegrationException {
        Connection con = getPostgresCon();
        ResultSet rs = null;
        Map<String, Integer> styleMap = new HashMap<>();
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT styleid, description FROM printstyle;");

        try {
            stmt = con.prepareStatement(sb.toString());
            rs = stmt.executeQuery();
            while (rs.next()) {
                styleMap.put(rs.getString("description"), rs.getInt("styleid"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to generate icon", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return styleMap;
    }

    public PrintStyle getPrintStyle(int styleID) throws IntegrationException {
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PrintStyle style = null;
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT styleid, description, headerimage_photodocid, headerheight, novtopmargin, \n"
                + "       novaddresseleftmargin, novaddressetopmargin, browserheadfootenabled, novtexttopmargin\n"
                + "  FROM public.printstyle WHERE styleid=?;");
        Icon i = null;

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, styleID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                style = generatePrintStyle(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to generate icon", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return style;

    }

    private PrintStyle generatePrintStyle(ResultSet rs) throws SQLException {
        PrintStyle style = new PrintStyle();

        style.setStyleID(rs.getInt("styleid"));

        style.setDescription(rs.getString("description"));
        style.setHeader_img_id(rs.getInt("headerimage_photodocid"));
        style.setHeader_height(rs.getInt("headerheight"));

        style.setNov_page_margin_top(rs.getInt("novtopmargin"));
        style.setNov_addressee_margin_left(rs.getInt("novaddresseleftmargin"));
        style.setNov_addressee_margin_top(rs.getInt("novaddressetopmargin"));
        style.setNov_text_margin_top(rs.getInt("novtexttopmargin"));

        return style;
    }

    public Icon getIcon(int iconID) throws IntegrationException {
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT iconid, name, styleclass, fontawesome, materialicons ");
        sb.append("FROM public.icon WHERE iconid=?;");
        Icon i = null;

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, iconID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                i = new Icon();
                i.setIconid(rs.getInt("iconid"));
                i.setName(rs.getString("name"));
                i.setStyleClass(rs.getString("styleclass"));
                i.setFontAwesome(rs.getString("fontawesome"));
                i.setMaterialIcon(rs.getString("materialicons"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to generate icon", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return i;

    }

    public void updateIcon(Icon i) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE public.icon SET name=?, styleclass=?, fontawesome=?, materialicons=? ");
        sb.append(" WHERE iconid = ?;");

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setString(1, i.getName());
            stmt.setString(2, i.getStyleClass());
            stmt.setString(3, i.getFontAwesome());
            stmt.setString(4, i.getMaterialIcon());
            stmt.setInt(5, i.getIconid());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to update icon", ex);
        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally
    }

    public void insertIcon(Icon i) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO public.icon(");
        sb.append("iconid, name, styleclass, fontawesome, materialicons) ");
        sb.append("VALUES (DEFAULT, ?, ?, ?, ?);");

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setString(1, i.getName());
            stmt.setString(2, i.getStyleClass());
            stmt.setString(3, i.getFontAwesome());
            stmt.setString(4, i.getMaterialIcon());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to insert icon", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        } // close finally
    }

   

    public List<Icon> getIconList() throws IntegrationException {
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT iconid FROM public.icon;");
        List<Icon> iList = new ArrayList<>();

        try {
            stmt = con.prepareStatement(sb.toString());
            rs = stmt.executeQuery();
            while (rs.next()) {
                iList.add(getIcon(rs.getInt("iconid")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to generate icon", ex);
        } finally {
           if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return iList;
    }

    public void insertImprovementSuggestion(ImprovementSuggestion is) throws IntegrationException {

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        String s = " INSERT INTO public.improvementsuggestion(\n"
                + "            improvementid, improvementtypeid, improvementsuggestiontext, \n"
                + "            improvementreply, statusid, submitterid, submissiontimestamp)\n"
                + "    VALUES (DEFAULT, ?, ?, \n"
                + "            NULL, ?, ?, now());";

        try {
            stmt = con.prepareStatement(s);
            stmt.setInt(1, is.getImprovementTypeID());
            stmt.setString(2, is.getSuggestionText());
            // hard-coded status for expediency
            stmt.setInt(3, is.getStatusID());
            stmt.setInt(4, is.getSubmitter().getUserID());
            System.out.println("PersonIntegrator.getPersonListByPropertyID | sql: " + stmt.toString());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert improvement suggestion, sorry", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        } // close finally
    }

    final String impSugQuery = "SELECT improvementid, improvementtypeid, improvementsuggestiontext, \n"
            + "       improvementreply, statusid, statustitle, typetitle, submitterid, submissiontimestamp\n"
            + "  FROM public.improvementsuggestion INNER JOIN improvementstatus USING (statusid)\n"
            + "  INNER JOIN improvementtype ON improvementtypeid = typeid;";

    public ArrayList<ImprovementSuggestion> getImprovementSuggestions() throws IntegrationException {
        ArrayList<ImprovementSuggestion> impList = new ArrayList<>();

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(impSugQuery);
            rs = stmt.executeQuery();
            while (rs.next()) {
                impList.add(generateImprovementSuggestion(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build property unit list due to an DB integration error", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return impList;
    }

    public ResultSet getImprovementSuggestionsRS() throws IntegrationException {
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(impSugQuery);
            rs = stmt.executeQuery();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build property unit list due to an DB integration error", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
//             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return rs;
    }

    private ImprovementSuggestion generateImprovementSuggestion(ResultSet rs) throws SQLException, IntegrationException {
        UserIntegrator ui = getUserIntegrator();
        ImprovementSuggestion is = new ImprovementSuggestion();
        is.setSuggestionID(rs.getInt("improvementid"));
        is.setSubmitter(ui.getUser(rs.getInt("submitterid")));
        is.setImprovementTypeID(rs.getInt("improvementtypeid"));
        is.setImprovementTypeStr(rs.getString("typetitle"));
        is.setReply(rs.getString("improvementreply"));
        is.setStatusID(rs.getInt("statusid"));
        is.setStatusStr(rs.getString("statustitle"));
        is.setSuggestionText(rs.getString("improvementsuggestiontext"));
        is.setSubmissionTimeStamp(rs.getTimestamp("submissiontimestamp").toLocalDateTime());
        return is;
    }

    public void insertListChangeRequest(ListChangeRequest lcr) throws IntegrationException {

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        String s = " INSERT INTO public.listchangerequest(\n"
                + " changeid, changetext)\n"
                + " VALUES (DEFAULT, ?);";

        try {
            stmt = con.prepareStatement(s);
            stmt.setString(1, lcr.getChangeRequestText());
            System.out.println("PersonIntegrator.getPersonListByPropertyID | sql: " + stmt.toString());

            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator.getPerson | Unable to retrieve person", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        } // close finally
    }

    public HashMap<String, Integer> getSuggestionTypeMap() throws IntegrationException {
        HashMap<String, Integer> hm = new HashMap<>();

        String query = "SELECT typeid, typetitle, typedescription\n"
                + "  FROM public.improvementtype;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                hm.put(rs.getString("typetitle"), rs.getInt("typeid"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build property unit list due to an DB integration error", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return hm;
    }

    /**
     * Returns all Source IDs in the bobsource table
     * @return
     * @throws IntegrationException 
     */
     public List<Integer> getBobSourceListComplete() throws IntegrationException{
          List<Integer> sidl = new ArrayList<>();
          BOBSource bs = null;
          
          String query =    "   SELECT sourceid FROM public.bobsource;";
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                sidl.add(rs.getInt("sourceid"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build property unit list due to an DB integration error", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return sidl;
     }
      
      public BOBSource getBOBSource(int sourceID) throws IntegrationException{
          if(sourceID == 0){
              return null;
          }
          BOBSource bs = null;
          
          String query =    "   SELECT sourceid, title, description, creator, muni_municode, userattributable, \n" +
                            "           active, notes\n" +
                            "           FROM public.bobsource WHERE sourceid = ?;";
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, sourceID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                bs = generateBOBSource(rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to build property unit list due to an DB integration error", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return bs;
    }

    private BOBSource generateBOBSource(ResultSet rs) throws SQLException, IntegrationException {
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        BOBSource bs = new BOBSource();
        bs.setSourceid(rs.getInt("sourceid"));
        bs.setTitle(rs.getString("title"));;
        bs.setDescription(rs.getString("description"));
        bs.setCreatorUserID(rs.getInt("creator"));
        bs.setMuni(mi.getMuni(rs.getInt("muni_municode")));;
        bs.setActive(rs.getBoolean("active"));;
        bs.setNotes(rs.getString("notes"));
        return bs;

    }

    private IntensityClass generateIntensityClass(ResultSet rs) throws IntegrationException {

        IntensityClass intsty = new IntensityClass();
        MunicipalityIntegrator mi = new MunicipalityIntegrator();

        try {
            intsty.setClassID(rs.getInt("classid"));
            intsty.setTitle(rs.getString("title"));
            intsty.setMuni(mi.getMuni(rs.getInt("muni_municode")));
            intsty.setNumericRating(rs.getInt("numericrating"));
            intsty.setSchema(new IntensitySchema(rs.getString("schemaName")));
            intsty.setActive(rs.getBoolean("active"));
            intsty.setIcon(getIcon(rs.getInt("icon_iconid")));
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error generating Intensity from ResultSet", ex);
        }

        return intsty;

    }
    
    
    public IntensityClass getIntensityClass(int intensityClassID) throws IntegrationException{
        
        IntensityClass in = null;

        String query =  "SELECT classid, title, muni_municode, numericrating, schemaname, active, \n" +
                        "       icon_iconid\n" +
                        "  FROM public.intensityclass WHERE classid=?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, intensityClassID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                in = generateIntensityClass(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("SystemIntegrator.getIntensityClassList | Unable to get Intensity List", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return in;
    }
    
 
    /**
     * Utility adaptor method for legacy compatability
     * @param cat
     * @return
     * @throws IntegrationException 
     */
    public List<IntensityClass> getIntensityClassList(IntensitySchema cat) throws IntegrationException {
        
        return getIntensityClassList(cat.getLabel());
        
    }
    
    
    /**
     * A search-like method for intensity classes with any schema name like X
     * @param schemaLabel
     * @param cat
     * @return
     * @throws IntegrationException 
     */
    public List<IntensityClass> getIntensityClassList(String schemaLabel) throws IntegrationException {

        List<IntensityClass> inList = new ArrayList<>();

        String query = "SELECT classid, title, muni_municode, numericrating, schemaname, active, icon_iconid FROM intensityclass WHERE schemaname ILIKE ?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, schemaLabel);
            rs = stmt.executeQuery();
            while (rs.next()) {
                inList.add(generateIntensityClass(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("SystemIntegrator.getIntensityClassList | Unable to get Intensity List", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return inList;

    }

    public void updateIntensityClass(IntensityClass intsty) throws IntegrationException {

        String query = "UPDATE public.intensityclass\n"
                + "SET title=?, muni_municode=?, numericrating=?,\n"
                + "schemaname=?, active=?, icon_iconid=?\n"
                + "WHERE classid=?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, intsty.getTitle());
            stmt.setInt(2, intsty.getMuni().getMuniCode());
            stmt.setInt(3, intsty.getNumericRating());
            stmt.setString(4, intsty.getSchema().getLabel());
            stmt.setBoolean(5, intsty.isActive());
            stmt.setInt(6, intsty.getIcon().getIconid());
            stmt.setInt(7, intsty.getClassID());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update Intensity", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             
        }

    }

    public void insertIntensityClass(IntensityClass intsty) throws IntegrationException {
        String query = "INSERT INTO public.intensityclass(classid, title, \n"
                + "muni_municode, numericrating, schemaname, \n"
                + "active, icon_iconid)\n"
                + "VALUES (DEFAULT, ?, ?, ?, ?, ?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, intsty.getTitle());
            stmt.setInt(2, intsty.getMuni().getMuniCode());
            stmt.setInt(3, intsty.getNumericRating());
            stmt.setString(4, intsty.getSchema().getLabel());
            stmt.setBoolean(5, intsty.isActive());
            stmt.setInt(6, intsty.getIcon().getIconid());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert Intensity Class", ex);

        } finally {
                if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }

    public void deleteIntensityClass(IntensityClass intsty) throws IntegrationException {
        String query = "DELETE FROM public.intensityclass\n"
                + "WHERE classid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, intsty.getClassID());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot delete Intensity Class", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

    }
    
    public List<IntensitySchema> getIntensitySchemaList() throws IntegrationException {

        List<IntensitySchema> inList = new ArrayList<>();

        String query = "SELECT DISTINCT schemaname FROM intensityclass;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                inList.add(generateIntensitySchema(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("SystemIntegrator.getIntensitySchemaList | Unable to get Intensity Schema List", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return inList;

    }

    public IntensitySchema generateIntensitySchema(ResultSet rs) throws IntegrationException {
        
        String schemaName = "";

        try {
            schemaName = rs.getString("schemaname");
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error generating IntensitySchema from ResultSet", ex);
        }

        return new IntensitySchema(schemaName);

    }
    
    /**
     * Writes in a history record when a User accesses that object.
     * The Object's type will be checked against existing history
     * recording opportunities and create an appropriate entry in the
     * loginobjecthistory table.
     *
     * 
     *
     * @param u the User who viewed the object
     * @param ob any Object that's displayed in a data table or list in the system
     * @throws IntegrationException
     */
    public void logObjectView(User u, IFace_Loggable ob) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        StringBuilder insertSB = new StringBuilder();
        insertSB.append("INSERT INTO loginobjecthistory ");
        try {
            if (ob instanceof Person) {
                Person p = (Person) ob;
                
                insertSB.append("(login_userid, person_personid, entrytimestamp) VALUES (?, ?, DEFAULT); ");
                stmt = con.prepareStatement(insertSB.toString());
                
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, p.getPersonID());
                
                stmt.execute();
                
                System.out.println("SystemIntegrator.logObjectView: Person view logged id = " + p.getPersonID());
                
            } else if (ob instanceof Property) {
                Property p = (Property) ob;
                
                insertSB.append("(login_userid, property_propertyid, entrytimestamp) VALUES (?, ?, DEFAULT); ");
                stmt = con.prepareStatement(insertSB.toString());
                
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, p.getPropertyID());

                stmt.execute();

                System.out.println("SystemIntegrator.logObjectView: Property view logged id = " + p.getPropertyID());
            } else if (ob instanceof CECaseDataHeavy) {
                CECaseDataHeavy c = (CECaseDataHeavy) ob;
                
                insertSB.append("(login_userid, cecase_caseid, entrytimestamp) VALUES (?, ?, DEFAULT); ");
                stmt = con.prepareStatement(insertSB.toString());
                
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, c.getCaseID());

                stmt.execute();

                System.out.println("SystemIntegrator.logObjectView: Case view logged id = " + c.getCaseID());
            } else if (ob instanceof OccPeriod) {
                OccPeriod op = (OccPeriod) ob;

                insertSB.append("(login_userid, occperiod_periodid, entrytimestamp) VALUES (?, ?, DEFAULT); ");
                stmt = con.prepareStatement(insertSB.toString());

                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, op.getPeriodID());

                stmt.execute();
                System.out.println("SystemIntegrator.logObjectView: Occ Period logged id = " + op.getPeriodID() );
            }
            
            
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error writign object history: persons, properties, or cecases", ex);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    /* ignored */
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    /* ignored */
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        } // close finally
    }

    /**
     * Writes in a history record when a User accesses that object.
     * The Object's type will be checked against existing history
     * recording opportunities and create an appropriate entry in the
     * loginobjecthistory table.
     *
     * Checks for duplicates in the table before inserting.
     * If duplicate object ID exists, update existing entry with the current
     * time stamp only.
     *
     * @deprecated 
     * @param u the User who viewed the object
     * @param ob any Object that's displayed in a data table or list in the system
     * @throws IntegrationException
     */
    public void logObjectView_OverwriteDate(User u, Object ob) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        StringBuilder selectSB = new StringBuilder();
        selectSB.append("SELECT historyentryid FROM loginobjecthistory " + "WHERE login_userid = ? ");
        StringBuilder insertSB = new StringBuilder();
        insertSB.append("INSERT INTO loginobjecthistory ");
        StringBuilder updateSB = new StringBuilder();
        updateSB.append("UPDATE loginobjecthistory SET entrytimestamp = now() " + "WHERE login_userid = ? ");
        try {
            if (ob instanceof Person) {
                Person p = (Person) ob;
                // prepare SELECT statement
                selectSB.append("AND person_personid = ? ");
                stmt = con.prepareStatement(selectSB.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, p.getPersonID());
                rs = stmt.executeQuery();
                if (rs.first()) {
                    // history entry with this user and person already exists
                    updateSB.append("AND person_personid = ? ");
                    stmt = con.prepareStatement(updateSB.toString());
                } else {
                    // pair not in history, do fresh insert
                    insertSB.append("(login_userid, person_personid, entrytimestamp) VALUES (?, ?, DEFAULT); ");
                    stmt = con.prepareStatement(insertSB.toString());
                }
                // each UPDATE and INSERT SQL structures take the params in this order
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, p.getPersonID());
                stmt.execute();
                System.out.println("SystemIntegrator.logObjectView: Person view logged id = " + p.getPersonID());
            } else if (ob instanceof Property) {
                Property p = (Property) ob;
                // prepare SELECT statement
                selectSB.append("AND property_propertyid = ? ");
                stmt = con.prepareStatement(selectSB.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, p.getPropertyID());
                rs = stmt.executeQuery();
                if (rs.first()) {
                    // history entry with this user and person already exists
                    updateSB.append("AND property_propertyid = ? ");
                    stmt = con.prepareStatement(updateSB.toString());
                } else {
                    // pair not in history, do fresh insert
                    insertSB.append("(login_userid, property_propertyid, entrytimestamp) VALUES (?, ?, DEFAULT); ");
                    stmt = con.prepareStatement(insertSB.toString());
                }
                // each UPDATE and INSERT SQL structures take the params in this order
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, p.getPropertyID());
                stmt.execute();
                System.out.println("SystemIntegrator.logObjectView: Property view logged id = " + p.getPropertyID());
            } else if (ob instanceof CECaseDataHeavy) {
                CECaseDataHeavy c = (CECaseDataHeavy) ob;
                // prepare SELECT statement
                selectSB.append("AND cecase_caseid = ? ");
                stmt = con.prepareStatement(selectSB.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, c.getCaseID());
                rs = stmt.executeQuery();
                if (rs.first()) {
                    // history entry with this user and person already exists
                    updateSB.append("AND cecase_caseid = ? ");
                    stmt = con.prepareStatement(updateSB.toString());
                } else {
                    // pair not in history, do fresh insert
                    insertSB.append("(login_userid, cecase_caseid, entrytimestamp) VALUES (?, ?, DEFAULT); ");
                    stmt = con.prepareStatement(insertSB.toString());
                }
                // each UPDATE and INSERT SQL structures take the params in this order
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, c.getCaseID());
                stmt.execute();
                System.out.println("SystemIntegrator.logObjectView: Case view logged id = " + c.getCaseID());
            } else if (ob instanceof OccPeriod) {
                OccPeriod op = (OccPeriod) ob;
                // prepare SELECT statement
                selectSB.append("AND occperiod_periodid = ? ");
                stmt = con.prepareStatement(selectSB.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, op.getPeriodID());
                rs = stmt.executeQuery();
                if (rs.first()) {
                    // history entry with this user and person already exists
                    updateSB.append("AND cecase_caseid = ? ");
                    stmt = con.prepareStatement(updateSB.toString());
                } else {
                    // pair not in history, do fresh insert
                    insertSB.append("(login_userid, cecase_caseid, entrytimestamp) VALUES (?, ?, DEFAULT); ");
                    stmt = con.prepareStatement(insertSB.toString());
                }
                // each UPDATE and INSERT SQL structures take the params in this order
                stmt.setInt(1, u.getUserID());
                stmt.setInt(2, op.getPeriodID());
                stmt.execute();
                System.out.println("SystemIntegrator.logObjectView: Occ Period logged id = " + op.getPeriodID() );
            }
            
            
            
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error writign object history: persons, properties, or cecases", ex);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    /* ignored */
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    /* ignored */
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        } // close finally
    }
    
    //xiaohong add
    public ArrayList<PrintStyle> getPrintStyle() throws IntegrationException {

        String query = "SELECT styleid, description, headerimage_photodocid, headerheight, novtopmargin, \n"
                + "       novaddresseleftmargin, novaddressetopmargin, browserheadfootenabled, \n"
                + "       novtexttopmargin\n"
                + "  FROM public.printstyle;";

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<PrintStyle> styleList = new ArrayList<>();
        PrintStyle style = null;

        try {
            con = getPostgresCon();
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                style = generatePrintStyle(rs);
                if(style != null){
                    styleList.add(style);
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("unable to generate icon", ex);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    /* ignored */ }
            }
        } // close finally
        return styleList;

    }
}
