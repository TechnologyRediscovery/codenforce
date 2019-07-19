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
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.BOBSource;
import com.tcvcog.tcvce.entities.CasePhase;
import com.tcvcog.tcvce.entities.Icon;
import com.tcvcog.tcvce.entities.ImprovementSuggestion;
import com.tcvcog.tcvce.entities.Intensity;
import com.tcvcog.tcvce.entities.IntensitySchema;
import com.tcvcog.tcvce.entities.ListChangeRequest;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PrintStyle;
import com.tcvcog.tcvce.entities.User;
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
 * @author Eric C. Darsow
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

    /**
     *
     * @param casephase
     * @return
     * @throws IntegrationException
     */
    public Icon getIcon(CasePhase casephase) throws IntegrationException {
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT iconid ");
        sb.append("FROM public.cecasestatusicon WHERE status=?::casephase;");
        Icon i = null;

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setString(1, casephase.toString());
            rs = stmt.executeQuery();
            while (rs.next()) {
                i = getIcon(rs.getInt("iconid"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to generate icon", ex);
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
        return i;

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

    public ImprovementSuggestion generateImprovementSuggestion(ResultSet rs) throws SQLException, IntegrationException {
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
        return hm;
    }

    /**
     * First generation data querying method: still raw SQL! Replaced by the
     * SearchParams* family
     *
     * @deprecated
     * @param muniCode
     * @return
     * @throws IntegrationException
     */
    public Map<String, Integer> getCaseCountsByPhase(int muniCode) throws IntegrationException {

        CasePhase[] phaseValuesArray = new CasePhase[8];
        phaseValuesArray[0] = CasePhase.PrelimInvestigationPending;
        phaseValuesArray[1] = CasePhase.NoticeDelivery;
        phaseValuesArray[2] = CasePhase.InitialComplianceTimeframe;
        phaseValuesArray[3] = CasePhase.SecondaryComplianceTimeframe;
        phaseValuesArray[4] = CasePhase.AwaitingHearingDate;
        phaseValuesArray[5] = CasePhase.HearingPreparation;
        phaseValuesArray[6] = CasePhase.InitialPostHearingComplianceTimeframe;
        phaseValuesArray[7] = CasePhase.SecondaryPostHearingComplianceTimeframe;
        //CasePhase[] phaseValuesArray = CasePhase.values();

        Map<String, Integer> caseCountMap = new LinkedHashMap<>();
        PreparedStatement stmt = null;
        Connection con = null;
        String query = "SELECT count(caseid) FROM cecase join property "
                + "ON property.propertyid = cecase.property_propertyid "
                + "WHERE property.municipality_municode = ? "
                + "AND casephase = CAST(? AS casephase) ;";
        ResultSet rs = null;

        try {
            con = getPostgresCon();
            for (CasePhase c : phaseValuesArray) {
                stmt = con.prepareStatement(query);
                stmt.setInt(1, muniCode);
                String phaseString = c.toString();
                stmt.setString(2, phaseString);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    caseCountMap.put(phaseString, rs.getInt(1));
                }

            }

        } catch (SQLException ex) {
            System.out.println("MunicipalityIntegrator.getMuniFromMuniCode | " + ex.toString());
            throw new IntegrationException("Exception in MunicipalityIntegrator.getCaseCountsByPhase", ex);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {/* ignored */ }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    /* ignored */ }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    /* ignored */
                }
            }
        } // close finally

        return caseCountMap;

    }

    public BOBSource getBOBSource(int sourceID) throws IntegrationException {
        BOBSource bs = null;

        String query = "   SELECT sourceid, title, description, creator, muni_municode, userattributable, \n"
                + "           active, notes\n"
                + "           FROM public.bobsource WHERE sourceid = ?;";

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
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                }
            }
        } // close finally
        return bs;
    }

    private BOBSource generateBOBSource(ResultSet rs) throws SQLException, IntegrationException {
        UserIntegrator ui = getUserIntegrator();
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        BOBSource bs = new BOBSource();
        bs.setSourceid(rs.getInt("sourceid"));
        bs.setTitle(rs.getString("title"));;
        bs.setDescription(rs.getString("description"));
        bs.setCreator(ui.getUser(rs.getInt("creator")));
        bs.setMuni(mi.getMuni(rs.getInt("muni_municode")));;
        bs.setActive(rs.getBoolean("active"));;
        bs.setNotes(rs.getString("notes"));
        return bs;

    }

    public Intensity generateIntensityClass(ResultSet rs) throws IntegrationException {

        Intensity intsty = new Intensity();
        MunicipalityIntegrator mi = new MunicipalityIntegrator();

        try {
            intsty.setClassID(rs.getInt("classid"));
            intsty.setTitle(rs.getString("title"));
            intsty.setMuni(mi.getMuni(rs.getInt("muni_municode")));
            intsty.setNumericRating(rs.getInt("numericrating"));
            intsty.setSchema(IntensitySchema.valueOf(rs.getString("schemaName")));
            intsty.setActive(rs.getBoolean("active"));
            intsty.setIcon(getIcon(rs.getInt("icon_iconid")));
        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Error generating Intensity from ResultSet", ex);
        }

        return intsty;

    }

    public List<Intensity> getIntensityClassList(IntensitySchema cat) throws IntegrationException {

        List<Intensity> inList = new ArrayList<>();

        String query = "SELECT * FROM intensityclass WHERE schemaname LIKE ?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, cat.getLabel());
            rs = stmt.executeQuery();
            while (rs.next()) {
                inList.add(generateIntensityClass(rs));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("SystemIntegrator.getIntensityClassList | Unable to get Intensity List", ex);
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

        return inList;

    }

    public void updateIntensityClass(Intensity intsty) throws IntegrationException {

        String query = "UPDATE public.intensityclass\n"
                + "SET classid=?, title=?, muni_municode=?, numericrating=?,\n"
                + "schemaname=?, active=?, icon_iconid=?\n"
                + "WHERE classid=?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, intsty.getClassID());
            stmt.setString(2, intsty.getTitle());
            stmt.setInt(3, intsty.getMuni().getMuniCode());
            stmt.setInt(4, intsty.getNumericRating());
            stmt.setString(5, intsty.getSchema().getLabel());
            stmt.setBoolean(6, intsty.isActive());
            stmt.setInt(7, intsty.getIcon().getIconid());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update Intensity", ex);
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
        }

    }

    public void insertIntensityClass(Intensity intsty) throws IntegrationException {
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

    public void deleteIntensityClass(Intensity intsty) throws IntegrationException {
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

}
