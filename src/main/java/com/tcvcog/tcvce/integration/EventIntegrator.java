 /*
 * Copyright (C) 2017 Turtle Creek Valley
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
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.CaseLifecycleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CECaseEvent;
import com.tcvcog.tcvce.entities.EventRuleCECase;
import com.tcvcog.tcvce.entities.CasePhase;
import com.tcvcog.tcvce.entities.Event;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.EventCECaseCasePropBundle;
import com.tcvcog.tcvce.entities.EventRuleAbstract;
import com.tcvcog.tcvce.entities.EventRuleImplementation;
import com.tcvcog.tcvce.entities.EventRuleSet;
import com.tcvcog.tcvce.entities.MuniProfile;
import com.tcvcog.tcvce.entities.Proposal;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.search.QueryEventCECase;
import com.tcvcog.tcvce.entities.search.SearchParamsEventCECase;
import com.tcvcog.tcvce.entities.occupancy.OccEvent;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.EventRuleOccPeriod;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Eric C. Darsow
 */
public class EventIntegrator extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of EventIntegrator
     */
    public EventIntegrator() {
    }

    public EventCategory getEventCategory(int catID) throws IntegrationException {

        String query = " SELECT categoryid, categorytype, title, description, userdeployable, \n" +
                        "       munideployable, publicdeployable, notifycasemonitors, hidable, \n" +
                        "       icon_iconid, relativeorderwithintype, relativeorderglobal, hosteventdescriptionsuggtext, \n" +
                        "       directive_directiveid\n" +
                        "  FROM public.eventcategory WHERE categoryid = ?";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        EventCategory ec = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, catID);
            rs = stmt.executeQuery();

            while (rs.next()) {
                ec = generateEventCategoryFromRS(rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get event categry", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return ec;
    }

    private EventCategory generateEventCategoryFromRS(ResultSet rs) throws SQLException, IntegrationException {
        SystemIntegrator si = getSystemIntegrator();
        EventCategory ec = new EventCategory();
        ChoiceIntegrator choiceInt = getChoiceIntegrator();
        
        ec.setCategoryID(rs.getInt("categoryid"));
        if(!(rs.getString("categoryType") == null) && !(rs.getString("categoryType").equals(""))){
            ec.setEventType(EventType.valueOf(rs.getString("categoryType")));
        }
        ec.setEventCategoryTitle(rs.getString("title"));
        ec.setEventCategoryDesc(rs.getString("description"));
        ec.setUserdeployable(rs.getBoolean("userdeployable"));
        
        ec.setMunideployable(rs.getBoolean("munideployable"));
        ec.setPublicdeployable(rs.getBoolean("publicdeployable"));
        ec.setNotifycasemonitors(rs.getBoolean("notifycasemonitors"));
        ec.setHidable(rs.getBoolean("hidable"));
        
        if(rs.getInt("icon_iconid") != 0){
            ec.setIcon(si.getIcon(rs.getInt("icon_iconid")));
        }
        ec.setRelativeOrderWithinType(rs.getInt("relativeorderwithintype"));
        ec.setRelativeOrderGlobal(rs.getInt("relativeorderglobal"));
        ec.setHostEventDescriptionSuggestedText(rs.getString("hosteventdescriptionsuggtext"));
        
        if(rs.getInt("directive_directiveid") != 0){
            ec.setDirective(choiceInt.getDirective(rs.getInt("directive_directiveid")));
        }
        return ec;
    }
    
    
    public CECaseEvent getEventCECase(int eventID) throws IntegrationException {
        CECaseEvent ev = null;

        String query = "SELECT eventid, category_catid, cecase_caseid, dateofrecord, \n" +
                "       eventtimestamp, eventdescription, owner_userid, disclosetomunicipality, \n" +
                "       disclosetopublic, activeevent, hidden, ceevent.notes, property_propertyid,  municipality_municode " +
                "       FROM public.ceevent INNER JOIN public.cecase ON (cecase_caseid = caseid)\n" +
                "                           INNER JOIN public.property on (property_propertyid = propertyid) " +
                "       WHERE eventid = ?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, eventID);
            rs = stmt.executeQuery();

            while (rs.next()) {
                ev = new CECaseEvent(generateEventFromRS(rs));
                ev.setCaseID(rs.getInt("cecase_caseid"));
            }
            

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive event", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return ev;    
    }
    
    public OccEvent getOccEvent(int eventID) throws IntegrationException{
        OccEvent occEv = null;

        String query =  "SELECT eventid, category_catid, occperiod_periodid, dateofrecord, eventtimestamp,  \n" +
                        "                               eventdescription, owner_userid, disclosetomunicipality, disclosetopublic,  \n" +
                        "                               activeevent, hidden, occevent.notes, property_propertyid, propertyunit_unitid, municipality_municode \n" +
                        "	FROM public.occevent \n" +
                        "	INNER JOIN public.occperiod ON (occperiod_periodid = periodid)\n" +
                        "	INNER JOIN public.propertyunit ON (propertyunit_unitid = unitid) \n" +
                        "	INNER JOIN public.property on (property_propertyid = propertyid)  \n" +
                        "	WHERE eventid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, eventID);
            rs = stmt.executeQuery();

            while (rs.next()) {
                occEv = new OccEvent(generateEventFromRS(rs));
                occEv.setOccPeriodID(rs.getInt("occperiod_periodid"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive event", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return occEv;    
    }
    
    public List<OccEvent> getOccEvents(int occPeriodID) throws IntegrationException{
        List<OccEvent> evList = new ArrayList<>();

        String query =  "SELECT eventid FROM public.occevent WHERE occperiod_periodid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, occPeriodID);
            rs = stmt.executeQuery();

            while (rs.next()) {
                evList.add(getOccEvent(rs.getInt("eventid")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive event", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return evList;
    }
    
    
    public List<EventCategory> getEventCategoryList() throws IntegrationException {
        String query = "SELECT categoryid FROM public.eventcategory;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<EventCategory> categoryList = new ArrayList();

        try {
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                categoryList.add(getEventCategory(rs.getInt("categoryid")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot generate list of event categories", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return categoryList;
    }
    
    /**
     * Existed when we had event categories as the only way of requesting things
     * 
     * @deprecated 
     * @return
     * @throws IntegrationException 
     */
    public List<EventCategory> getRequestableEventCategories() 
            throws IntegrationException {
        String query = "SELECT categoryid FROM public.eventcategory WHERE requestable = TRUE;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<EventCategory> categoryList = new ArrayList();

        try {
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                categoryList.add(getEventCategory(rs.getInt("categoryid")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot generate list of event categories", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return categoryList;
    }
    
    /**
     * 
     * @param et
     * @return
     * @throws IntegrationException
     */
    public List<EventCategory> getEventCategoryList(EventType et) throws IntegrationException {
        String query = "SELECT categoryid FROM public.eventcategory WHERE categorytype = cast (? as ceeventtype);";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<EventCategory> categoryList = new ArrayList();

        try {

            stmt = con.prepareStatement(query);
            stmt.setString(1, et.toString());
            rs = stmt.executeQuery();
            while (rs.next()) {
                categoryList.add(getEventCategory(rs.getInt("categoryid")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot generate list of event categories", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return categoryList;
    }

    public void insertEventCategory(EventCategory ec) throws IntegrationException {

        String query = "INSERT INTO public.eventcategory(\n" +
                        "            categoryid, categorytype, title, description, userdeployable, \n" +
                        "            munideployable, publicdeployable, notifycasemonitors, hidable, \n" +
                        "            icon_iconid, relativeorderwithintype, relativeorderglobal, hosteventdescriptionsuggtext, \n" +
                        "            directive_directiveid)\n" +
                        "    VALUES (?, ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, \n" +
                        "            ?);";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, ec.getEventType().name());
            stmt.setString(2, ec.getEventCategoryTitle());
            stmt.setString(3, ec.getEventCategoryDesc());
            stmt.setBoolean(4, ec.isUserdeployable());
            
            stmt.setBoolean(5, ec.isMunideployable());
            stmt.setBoolean(6, ec.isPublicdeployable());
            stmt.setBoolean(7, ec.isNotifycasemonitors());
            stmt.setBoolean(8, ec.isHidable());

            stmt.setInt(9, ec.getIcon().getIconid());
            stmt.setInt(10, ec.getRelativeOrderWithinType());
            stmt.setInt(11, ec.getRelativeOrderGlobal());
            stmt.setString(12, ec.getHostEventDescriptionSuggestedText());
            
            if(ec.getDirective() != null){
                    stmt.setInt(13, ec.getDirective().getDirectiveID());
            } else {
                stmt.setNull(13, java.sql.Types.NULL);
            }

            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert event category", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }

    public void updateEventCategory(EventCategory ec) throws IntegrationException {

        String query = "UPDATE public.eventcategory\n" +
                        "   SET categorytype=?, title=?, description=?, userdeployable=?, \n" +
                        "       munideployable=?, publicdeployable=?, notifycasemonitors=?, hidable=?, \n" +
                        "       icon_iconid=?, relativeorderwithintype=?, relativeorderglobal=?, \n" +
                        "       hosteventdescriptionsuggtext=?, directive_directiveid=?\n" +
                        " WHERE categoryid = ?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, ec.getEventType().name());
            stmt.setString(2, ec.getEventCategoryTitle());
            stmt.setString(3, ec.getEventCategoryDesc());
            stmt.setBoolean(4, ec.isUserdeployable());

            stmt.setBoolean(5, ec.isMunideployable());
            stmt.setBoolean(6, ec.isPublicdeployable());
            stmt.setBoolean(7, ec.isNotifycasemonitors());
            stmt.setBoolean(8, ec.isHidable());

            if(ec.getIcon() != null){
                stmt.setInt(9, ec.getIcon().getIconid());
            } else {
                stmt.setNull(9, java.sql.Types.NULL);
            }
            stmt.setInt(10, ec.getRelativeOrderWithinType());
            stmt.setInt(11, ec.getRelativeOrderGlobal());
            
            stmt.setString(12, ec.getHostEventDescriptionSuggestedText());

            if(ec.getDirective() != null){
                stmt.setInt(13, ec.getDirective().getDirectiveID());
                
            } else {
                stmt.setNull(13, java.sql.Types.NULL);
            }
            
            stmt.setInt(14, ec.getCategoryID());
            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update event category", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }

    public void deleteEventCategory(EventCategory ec) throws IntegrationException {
        String query = "DELETE FROM public.eventcategory\n"
                + " WHERE categoryid = ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ec.getCategoryID());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot delete event--probably because another"
                    + "part of the database has a reference to this event category. Next best: marking"
                    + "the event as inactive.", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    
    
    

    /**
     * Attaches an Event to a code enforcement case. No checking of logic occurs
     * in this integration method, so the caller should always be a coordiantor
     * who has vetted the event and the associated case.
     *
     * @param event a fully-baked event ready for insertion. An CECaseEvent
 contains an integer of the caseID to which the event should be attached
     * @return the id of the event just inserted
     * @throws IntegrationException when the system is unable to store event in
     * DB
     */
    public int insertEvent(CECaseEvent event) throws IntegrationException {
        PersonIntegrator pi = getPersonIntegrator();
        int insertedEventID = 0;

        String query = "INSERT INTO public.ceevent(\n"
                + "            eventid, category_catid, cecase_caseid, dateofrecord, \n"
                + "            eventtimestamp, eventdescription, owner_userid, disclosetomunicipality, \n"
                + "            disclosetopublic, activeevent, \n"
                + "            hidden, notes)\n"
                + "    VALUES (DEFAULT, ?, ?, ?, \n"
                + "            now(), ?, ?, ?, \n"
                + "            ?, ?, " // params 7-8
                + "            ?, ?);";  // 12-14
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, event.getCategory().getCategoryID());
            stmt.setInt(2, event.getCaseID());
            if (event.getDateOfRecord() != null) {
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(event.getDateOfRecord()));
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }

            // note that the timestamp is set by a call to postgres's now()
            stmt.setString(4, event.getDescription());
            stmt.setInt(5, event.getOwner().getUserID());
            stmt.setBoolean(6, event.isDiscloseToMunicipality());

            stmt.setBoolean(7, event.isDiscloseToPublic());
            stmt.setBoolean(8, event.isActive());
            
            stmt.setBoolean(9, event.isHidden());
            stmt.setString(10, event.getNotes());
            
            stmt.execute();

            String retrievalQuery = "SELECT currval('ceevent_eventID_seq');";
            stmt = con.prepareStatement(retrievalQuery);

            rs = stmt.executeQuery();
            while (rs.next()) {
                insertedEventID = rs.getInt(1);

            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert Event into system", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

        // now connect people to event that has already been logged
        List<Person> al = event.getPersonList();
        event.setEventID(insertedEventID);

        if (al != null) {
            if (al.size() > 0 && event.getEventID() != 0) {
                pi.connectPersonsToEvent(event, al);
            }
        }
        
        return insertedEventID;

    } // close method
    
    public int insertEvent(OccEvent event) throws IntegrationException {
        PersonIntegrator pi = getPersonIntegrator();
        int insertedEventID = 0;

        String query = "INSERT INTO public.occevent(\n" +
                        "            eventid, category_catid, occperiod_periodid, dateofrecord, eventtimestamp, \n" +
                        "            eventdescription, owner_userid, disclosetomunicipality, disclosetopublic, \n" +
                        "            activeevent, hidden, notes)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, now(), \n" +
                        "            ?, ?, ?, ?, \n" +
                        "            ?, ?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, event.getCategory().getCategoryID());
            stmt.setInt(2, event.getOccPeriodID());
            if (event.getDateOfRecord() != null) {
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(event.getDateOfRecord()));
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }

            // note that the timestamp is set by a call to postgres's now()
            stmt.setString(4, event.getDescription());
            stmt.setInt(5, event.getOwner().getUserID());
            stmt.setBoolean(6, event.isDiscloseToMunicipality());

            stmt.setBoolean(7, event.isDiscloseToPublic());
            stmt.setBoolean(8, event.isActive());
            
            stmt.setBoolean(9, event.isHidden());
            stmt.setString(10, event.getNotes());
            
            stmt.execute();

            String retrievalQuery = "SELECT currval('occevent_eventid_seq');";
            stmt = con.prepareStatement(retrievalQuery);

            rs = stmt.executeQuery();
            while (rs.next()) {
                insertedEventID = rs.getInt(1);

            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert Event into system", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally

        if(event.getPersonList() != null){
            List<Person> persList = event.getPersonList();
            if (persList.size() > 0 && event.getEventID() != 0) {
                pi.connectPersonsToEvent(event, persList);
            }
        }
        return insertedEventID;
    } // close method

    public void inactivateEvent(int eventIdToInactivate) throws IntegrationException {
        String query = "UPDATE public.ceevent\n"
                + "   SET activeevent=false, hidden=true WHERE eventid = ?;";

        // TO DO: finish clearing view confirmation
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, eventIdToInactivate);

            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive event", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           
        } // close finally

    }

    public void updateEvent(CECaseEvent event) throws IntegrationException {
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE public.ceevent ");
        sb.append("   SET category_catid=?, cecase_caseid=?, dateofrecord=?, ");
        sb.append("       eventdescription=?, owner_userid=?, disclosetomunicipality=?, ");
        sb.append("       disclosetopublic=?, activeevent=?, ");
        sb.append("       hidden=?, notes=? ");
        
        sb.append(" WHERE eventid = ?;");

        // TO DO: finish clearing view confirmation
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, event.getCategory().getCategoryID());
            stmt.setInt(2, event.getCaseID());
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(event.getDateOfRecord()));

            // timestamp is updated with a call to postgres's now()
            stmt.setString(4, event.getDescription());
            stmt.setInt(5, event.getOwner().getUserID());
            stmt.setBoolean(6, event.isDiscloseToMunicipality());

            stmt.setBoolean(7, event.isDiscloseToPublic());
            stmt.setBoolean(8, event.isActive());
            stmt.setBoolean(9, event.isHidden());
            stmt.setString(10, event.getNotes());
            
            stmt.setInt(11, event.getEventID());
            
            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive event", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    

    public void updateEvent(OccEvent event) throws IntegrationException {
       String sql = "UPDATE public.occevent\n" +
                    "   SET category_catid=?, dateofrecord=?, \n" +
                    "       eventtimestamp=?, eventdescription=?, owner_userid=?, disclosetomunicipality=?, \n" +
                    "       disclosetopublic=?, activeevent=?, hidden=?, notes=?\n" +
                    " WHERE eventid=?;";

        // TO DO: finish clearing view confirmation
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(sql);
            stmt.setInt(1, event.getCategory().getCategoryID());
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(event.getDateOfRecord()));

            // timestamp is updated with a call to postgres's now()
            stmt.setString(3, event.getDescription());
            stmt.setInt(4, event.getOwner().getUserID());
            stmt.setBoolean(5, event.isDiscloseToMunicipality());

            stmt.setBoolean(6, event.isDiscloseToPublic());
            stmt.setBoolean(7, event.isActive());
            stmt.setBoolean(8, event.isHidden());
            stmt.setString(9, event.getNotes());
            
            stmt.setInt(10, event.getEventID());
            
            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot update occ event", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
   
    public void deleteEvent(CECaseEvent event) throws IntegrationException {
        String query = "DELETE FROM public.ceevent WHERE eventid = ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, event.getEventID());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot delete event--probalby because one or"
                    + "more other entries reference this event. ", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
   
    public void deleteEvent(OccEvent event) throws IntegrationException {
        String query = "DELETE FROM public.occeventg WHERE eventid = ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, event.getEventID());
            stmt.execute();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot delete event--probalby because one or"
                    + "more other entries reference this event. ", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
   
    /**
     * Legacy note: [Zanda was trippin when he wrote this!]
     * ....And when he revised it for occbeta! 
     *
     * @param rs
     * @param premadeEvent used by event creatino pathways that involve instantiation 
     * at other locations -- somewhat hacky and consider unifying
     * @return
     * @throws SQLException
     * @throws IntegrationException
     */
    private Event generateEventFromRS(ResultSet rs) throws SQLException, IntegrationException {
        Event ev = new Event();
        UserIntegrator ui = getUserIntegrator();
        SystemCoordinator ssc = getSystemCoordinator();
        
        ev.setPropertyID(rs.getInt("property_propertyid"));
        ev.setMuniCode(rs.getInt("municipality_municode"));
        ev.setMuniName(ssc.getMuniCodeNameMap().get(rs.getInt("municipality_municode")));

        ev.setEventID(rs.getInt("eventid"));
        ev.setCategory(getEventCategory(rs.getInt("category_catid")));
//        ev.setCaseID(rs.getInt("cecase_caseid"));
        
        if (rs.getTimestamp("dateofrecord") != null) {
            LocalDateTime dt = rs.getTimestamp("dateofrecord").toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();
            ev.setDateOfRecord(dt);
        }

        ev.setTimestamp(rs.getTimestamp("eventtimestamp").toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());
        ev.setDescription(rs.getString("eventDescription"));
        ev.setOwner(ui.getUser(rs.getInt("owner_userid")));
       
        ev.setDiscloseToMunicipality(rs.getBoolean("disclosetomunicipality"));
        ev.setDiscloseToPublic(rs.getBoolean("disclosetopublic"));
        ev.setActive(rs.getBoolean("activeevent"));

        ev.setHidden(rs.getBoolean("hidden"));
        ev.setNotes(rs.getString("notes"));
        
        return ev;
    }
    
    
    
    public EventCECaseCasePropBundle getEventCasePropBundle(int eventid) throws IntegrationException, CaseLifecycleException{
        EventCECaseCasePropBundle evCPBundle = null;
        CaseIntegrator ci = getCaseIntegrator();

       StringBuilder sb = new StringBuilder();
        sb.append("SELECT eventid, cecase_caseid FROM ceevent "
                + "INNER JOIN cecase ON (cecase_caseid = caseid) "
                + "WHERE eventid = ?");
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, eventid);
            rs = stmt.executeQuery();

            while (rs.next()) {
                evCPBundle = new EventCECaseCasePropBundle();
                evCPBundle.setEvent(getEventCECase(rs.getInt("eventid")));
                evCPBundle.setEventCaseBare(ci.getCECaseBare(rs.getInt("cecase_caseid")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive event", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return evCPBundle;
    }
    
    
   
    
    
    
    /**
     * Convenience method for extracting the case-dependent events from their
     * Wrapper bundle for use by the CaseManager page who just needs simple events
     * for each case.
     * The Params are passed through to the getEventsCECase method and the results
 are iterated over for object extraction and building a new list.
     * 
     * @param params
     * @return
     * @throws IntegrationException 
     */
    public List<CECaseEvent> queryCaseDependentEvents(SearchParamsEventCECase params) throws IntegrationException, CaseLifecycleException{
        List<EventCECaseCasePropBundle> wrappedEventList = getEventsCECase(params);
        List<CECaseEvent> depList = new ArrayList<>();
        for(EventCECaseCasePropBundle ec: wrappedEventList){
            depList.add(ec.getEvent());
        }
        return depList;
    }

  
    public QueryEventCECase runQueryEventCECase(QueryEventCECase q) 
            throws IntegrationException, CaseLifecycleException{
        List<SearchParamsEventCECase> pList = q.getParmsList();
        
        for(SearchParamsEventCECase sp: pList){
            q.addToResults(getEventsCECase(sp));
        }
        q.setExecutionTimestamp(LocalDateTime.now());
        q.setExecutedByIntegrator(true);
        return q;
    }
    
    public List<EventCECaseCasePropBundle> getEventsCECase(SearchParamsEventCECase params) 
            throws IntegrationException, CaseLifecycleException {
        List<EventCECaseCasePropBundle> eventList = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        Connection con = getPostgresCon();
        boolean notFirstCriteria = false;

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ceevent.eventid ");
        sb.append("FROM ceevent INNER JOIN eventcategory ON (category_catid = categoryid) ");
        sb.append("INNER JOIN cecase ON (cecase_caseid = caseid) ");
        sb.append("INNER JOIN property ON (property_propertyid = propertyid) ");
        sb.append("WHERE ");
        // as long as this isn't an ID only search, do the normal SQL building process
        if (!params.isFilterByObjectID()) {
            if (params.isFilterByMuni()) {
                if(notFirstCriteria){sb.append("AND ");} else {notFirstCriteria = true;}
                sb.append("municipality_municode = ? "); // param 1
            }

            if (params.isFilterByStartEndDate()){
                if(notFirstCriteria){sb.append("AND ");} else {notFirstCriteria = true;}
                if(params.isApplyDateSearchToDateOfRecord()){
                    sb.append("dateofrecord "); 
                } else if(params.isUseRespondedAtDateRange()){
                    sb.append("viewconfirmedat ");
                } else if(params.isUseEntryTimestamp()){
                    sb.append("entrytimestamp "); 
                } else {
                    sb.append("dateofrecord "); 
                }
                sb.append("BETWEEN ? AND ? "); // parm 2 and 3 without ID
            }

            if (params.isFilterByEventType() ) {
                if(notFirstCriteria){sb.append("AND ");} else {notFirstCriteria = true;}
                sb.append("categorytype = CAST (? AS ceeventtype) ");
            }

            if (params.isFilterByEventCategory() ) {
                if(notFirstCriteria){sb.append("AND ");} else {notFirstCriteria = true;}
                sb.append("eventcategory_catid = ? ");
            }


            if (params.isFilterByCaseID()) {
                if(notFirstCriteria){sb.append("AND ");} else {notFirstCriteria = true;}
                sb.append("cecase_caseid = ? ");
            }

            if (params.isFilterByEventOwner() && params.getUser() != null) {
                    if(notFirstCriteria){sb.append("AND ");} else {notFirstCriteria = true;}
                    sb.append("ceevent.owner_userid = ? ");
            }
            
            if (params.isFilterByPerson()) {
//                if(notFirstCriteria){sb.append("AND ");} else {notFirstCriteria = true;}
//                sb.append("person_personid = ?");
            }


            if (params.isActive_filterBy()) {
                if(notFirstCriteria){sb.append("AND ");} else {notFirstCriteria = true;}
                if (params.isIsActive()) {
                    sb.append("activeevent = TRUE ");
                } else {
                    sb.append("activeevent = FALSE ");
                }
            }
            
            if (params.isFilterByHidden()) {
                if(notFirstCriteria){sb.append("AND ");} else {notFirstCriteria = true;}
                if (params.isIsHidden()) {
                    sb.append("hidden = TRUE ");
                } else {
                    sb.append("hidden = FALSE ");
                }
            }
        } else {
            sb.append("eventid = ? "); // will be param 1 with ID search
        }
        int paramCounter = 0;
            
        try {
            stmt = con.prepareStatement(sb.toString());

            if (!params.isFilterByObjectID()) {
                if (params.isFilterByMuni()) {
                    stmt.setInt(++paramCounter, params.getMuni().getMuniCode());
                }
                if (params.isFilterByStartEndDate()) {
                    stmt.setTimestamp(++paramCounter, params.getStartDateSQLDate());
                    stmt.setTimestamp(++paramCounter, params.getEndDateSQLDate());
                }
                if (params.isFilterByEventType()) {
                    stmt.setString(++paramCounter, params.getEvtType().name());
                }

                if (params.isFilterByEventCategory() 
                        && 
                    params.getEventCategory() != null) {
                    stmt.setInt(++paramCounter, params.getEventCategory().getCategoryID());
                }

                if (params.isFilterByCaseID()) {
                    stmt.setInt(++paramCounter, params.getCaseId());
                }

                if (params.isFilterByEventOwner() 
                        && 
                    params.getUser() != null) {
                        stmt.setInt(++paramCounter, params.getUser().getUserID());
                }

                if (params.isFilterByPerson()) {
//                    stmt.setInt(++paramCounter, params.getPerson().getPersonID());
                }
                
            } else {
                stmt.setInt(++paramCounter, params.getObjectID());
            }

            rs = stmt.executeQuery();

            int counter = 0;
            int maxResults;
            if (params.isLimitResultCountTo100()) {
                maxResults = 100;
            } else {
                maxResults = Integer.MAX_VALUE;
            }
            while (rs.next() && counter < maxResults) {
                eventList.add(getEventCasePropBundle(rs.getInt("eventid")));
                counter++;
            }

        } catch (SQLException ex) {
            System.out.println(ex);
//            throw new IntegrationException("Integration Error: Problem retrieving and generating action request list", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }

        }// close try/catch

        return eventList;
    }
    

    public List<CECaseEvent> getEventsByCaseID(int caseID) throws IntegrationException {
        List<CECaseEvent> eventList = new ArrayList();

        String query = "SELECT eventid FROM public.ceevent WHERE cecase_caseid = ?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, caseID);
            rs = stmt.executeQuery();

            while (rs.next()) {
                eventList.add(getEventCECase(rs.getInt("eventid")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot generate case list", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        
        }
        return eventList;
    }
    
    
     /**
     * Getter for rules by ID
     * 
     * @param ruleid
     * @return
     * @throws IntegrationException 
     */
    public EventRuleAbstract getEventRule(int ruleid) throws IntegrationException{
        if(ruleid == 0){
            return null;
        }
        
        EventRuleAbstract rule = null;
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String s = "SELECT ruleid, title, description, requiredeventtype, forbiddeneventtype, \n" +
                        "       requiredeventcat_catid, requiredeventcatthresholdtypeintorder, \n" +
                        "       requiredeventcatupperboundtypeintorder, requiredeventcatthresholdglobalorder, \n" +
                        "       requiredeventcatupperboundglobalorder, forbiddeneventcat_catid, \n" +
                        "       forbiddeneventcatthresholdtypeintorder, forbiddeneventcatupperboundtypeintorder, \n" +
                        "       forbiddeneventcatthresholdglobalorder, forbiddeneventcatupperboundglobalorder, \n" +
                        "       mandatorypassreqtocloseentity, autoremoveonentityclose, promptingproposal_proposalid, \n" +
                        "       triggeredeventcatonpass, triggeredeventcatonfail, active, notes\n" +
                        "  FROM public.eventrule WHERE ruleid=?;";
            stmt = con.prepareStatement(s);
            stmt.setInt(1, ruleid);

            rs = stmt.executeQuery();
            while(rs.next()){
                rule = generateEventRule(rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to generate case history list", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return rule;
        
        
    }  
    
     /**
     * Instantiation and population of CasePhaseRule changes
     * 
     * @param rs
     * @return
     * @throws SQLException
     */
    private EventRuleAbstract generateEventRule(ResultSet rs) throws SQLException, IntegrationException{
        EventRuleAbstract evRule = new EventRuleAbstract();
        ChoiceIntegrator ci = getChoiceIntegrator();
        
        evRule.setRuleid(rs.getInt("ruleid"));
        evRule.setTitle(rs.getString("title"));
        evRule.setDescription(rs.getString("description"));
        
        if(!(rs.getString("requiredeventtype") == null) && !(rs.getString("requiredeventtype").equals(""))){
            evRule.setRequiredEventType(EventType.valueOf(rs.getString("requiredeventtype")));
        }
        if(!(rs.getString("forbiddeneventtype") == null) && !(rs.getString("forbiddeneventtype").equals(""))){
            evRule.setForbiddenEventType(EventType.valueOf(rs.getString("forbiddeneventtype")));
        }
        
        evRule.setRequiredEventCategory(getEventCategory(rs.getInt("requiredeventcat_catid")));
        evRule.setRequiredECThreshold_typeInternalOrder(rs.getInt("requiredeventcatthresholdtypeintorder"));
        
        evRule.setRequiredECThreshold_typeInternalOrder_treatAsUpperBound(rs.getBoolean("requiredeventcatupperboundtypeintorder"));
        evRule.setRequiredECThreshold_globalOrder(rs.getInt("requiredeventcatthresholdglobalorder"));
        
        evRule.setRequiredECThreshold_globalOrder_treatAsUpperBound(rs.getBoolean("requiredeventcatupperboundglobalorder"));
        evRule.setForbiddenEventCategory(getEventCategory(rs.getInt("forbiddeneventcat_catid")));
        
        evRule.setForbiddenECThreshold_typeInternalOrder(rs.getInt("forbiddeneventcatthresholdtypeintorder"));
        evRule.setForbiddenECThreshold_typeInternalOrder_treatAsUpperBound(rs.getBoolean("forbiddeneventcatupperboundtypeintorder"));
        
        evRule.setForbiddenECThreshold_globalOrder(rs.getInt("forbiddeneventcatthresholdglobalorder"));
        evRule.setForbiddenECThreshold_globalOrder_treatAsUpperBound(rs.getBoolean("forbiddeneventcatupperboundglobalorder"));
        
        evRule.setMandatoryRulePassRequiredToCloseEntity(rs.getBoolean("mandatorypassreqtocloseentity"));
        evRule.setInactivateRuleOnEntityClose(rs.getBoolean("autoremoveonentityclose"));
        if(rs.getInt("promptingproposal_proposalid") != 0){
            evRule.setPromptingProposal(ci.getProposal(rs.getInt("promptingproposal_proposalid")));
        }
        if(rs.getInt("triggeredeventcatonpass") != 0){
            evRule.setTriggeredECOnRulePass(getEventCategory(rs.getInt("triggeredeventcatonpass")));
        }
        if(rs.getInt("triggeredeventcatonfail") != 0){
            evRule.setTriggeredECOnRuleFail(getEventCategory(rs.getInt("triggeredeventcatonfail")));
        }
        evRule.setActive(rs.getBoolean("active"));
        evRule.setNotes(rs.getString("notes"));
        
        return evRule;
    }
    
    public List<EventRuleAbstract> getEventRuleList(int ruleSetID) throws IntegrationException{
        List<EventRuleAbstract> list = new ArrayList<>();
        String query = "SELECT eventrule_ruleid\n" +
                        "  FROM public.eventruleruleset WHERE ruleset_rulesetid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ruleSetID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(getEventRule(rs.getInt("eventrule_ruleid")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot generate list of event rules", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return list;
        
    }
    
    public EventRuleSet getEventRuleSet(MuniProfile profile) throws IntegrationException{
        EventRuleSet set = null;
        String query = "SELECT muniprofile_profileid, ruleset_setid\n" +
"  FROM public.muniprofileeventruleset WHERE muniprofile_profileid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, profile.getProfileID());
            rs = stmt.executeQuery();
            while (rs.next()) {
                set = getEventRuleSet(rs.getInt("ruleset_setid"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot generate list of event rules", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return set;
        
    }
    
    public EventRuleSet getEventRuleSet(int setID) throws IntegrationException{
        EventRuleSet set = null;
        String query = "SELECT rulesetid, title, description\n" +
                        "  FROM public.eventruleset WHERE rulesetid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, setID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                set = generateRuleSet(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot generate list of event rules", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return set;
    }
    
    private EventRuleSet generateRuleSet(ResultSet rs) throws SQLException, IntegrationException{
        EventRuleSet s = new EventRuleSet();
        s.setRulseSetID(rs.getInt("rulesetid"));
        s.setTitle(rs.getString("title"));
        s.setDescription(rs.getString("description"));
        s.setRuleList(getEventRuleList(rs.getInt("rulesetid")));
        return s;
    }
    
    public List<EventRuleCECase> getEventRuleCECaseList(CECase cse) throws IntegrationException{
        EventRuleImplementation ruleImp = null;
        List<EventRuleCECase> ruleList = new ArrayList<>();
        String query =  "   SELECT cecase_caseid, eventrule_ruleid, attachedts, attachedby_userid, \n" +
                        "       lastevaluatedts, passedrulets, passedrule_eventid\n" +
                        "  FROM public.cecaserule WHERE cecase_caseid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, cse.getCaseID());
            rs = stmt.executeQuery();
            while (rs.next()) {
                ruleImp = generateEventRuleImplementation(rs);
                ruleList.add(generateCECaseEventRule(rs, ruleImp));
                
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot generate list of event rules", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return ruleList;
        
    }
    
    private EventRuleCECase generateCECaseEventRule(ResultSet rs, EventRuleImplementation imp) 
            throws SQLException, IntegrationException{
        EventRuleCECase evRule = new EventRuleCECase(imp);
        evRule.setCeCaseID(rs.getInt("cecase_caseid"));
        evRule.setPassedRuleEvent(getEventCECase(rs.getInt("passedrule_eventid")));
        return evRule;
        
    }
    
    private EventRuleImplementation generateEventRuleImplementation(ResultSet rs) throws SQLException, IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        
        EventRuleImplementation ruleImp = new EventRuleImplementation(getEventRule(rs.getInt("eventrule_ruleid")));
        ruleImp.setAttachedTS(rs.getTimestamp("attachedts").toLocalDateTime());
        ruleImp.setAttachedBy(ui.getUser(rs.getInt("attachedby_userid")));
        if(rs.getTimestamp("lastevaluatedts") != null){
            ruleImp.setLastEvaluatedTS(rs.getTimestamp("lastevaluatedts").toLocalDateTime());
        } 
       ruleImp.setAttachedBy(ui.getUser(rs.getInt("attachedby_userid")));
       
       return ruleImp;
    }
    
    private EventRuleOccPeriod generateOccPeriodEventRule(ResultSet rs, EventRuleImplementation imp) throws SQLException, IntegrationException{
        EventRuleOccPeriod evRule = new EventRuleOccPeriod(imp);
        evRule.setOccPeriodID(rs.getInt("occperiod_periodid"));
        evRule.setPassedRuleEvent(getOccEvent(rs.getInt("passedrule_eventid")));
        return evRule;
    }
    
    public List<EventRuleOccPeriod> getEventRuleOccPeriodList(OccPeriod op) throws IntegrationException{
        EventRuleImplementation ruleImp;
        List<EventRuleOccPeriod> ruleList = new ArrayList<>();
        String query = "SELECT occperiod_periodid, eventrule_ruleid, attachedts, attachedby_userid, \n" +
                        "       lastevaluatedts, passedrulets, passedrule_eventid\n" +
                        "  FROM public.occperiodeventrule WHERE occperiod_periodid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, op.getPeriodID());
            rs = stmt.executeQuery();
            while (rs.next()) {
                ruleImp = generateEventRuleImplementation(rs);
                ruleList.add(generateOccPeriodEventRule(rs, ruleImp));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot generate list of event rules", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return ruleList;
        
    }
    
    

    /**
     * First gen query for a single purpose: introducing SearchParams objects
     * with SQL assembly logic in the integration methods
     * 
     * Included for reference since the sql is complicated
     *
     * @deprecated
     * @param m
     * @param start
     * @param end
     * @return
     * @throws IntegrationException
     */
    public List<EventCECaseCasePropBundle> getUpcomingTimelineEvents(Municipality m, LocalDateTime start, LocalDateTime end) throws IntegrationException {

        ArrayList<EventCECaseCasePropBundle> eventList = new ArrayList<>();

        String query = "SELECT ceevent.eventid, ceevent.category_catid, ceevent.dateofrecord, \n"
                + "       ceevent.eventtimestamp, ceevent.eventdescription, ceevent.owner_userid, ceevent.disclosetomunicipality, \n"
                + "       ceevent.disclosetopublic, ceevent.activeevent, ceevent.requestsAction, ceevent.hidden, \n"
                + "       ceevent.notes, ceevent.viewconfirmedby, ceevent.viewconfirmedat, cecase.caseid, eventcategory.categoryid\n"
                + " FROM ceevent 	INNER JOIN eventcategory ON (category_catid = categoryid)\n"
                + "		INNER JOIN cecase ON (cecase_caseid = caseid)\n"
                + " WHERE categorytype = CAST ('Timeline' AS ceeventtype)\n"
                + "		AND dateofrecord >= ? AND dateofrecord <= ? \n"
                + "		AND activeevent = TRUE\n"
                + "		AND ceevent.requestsAction = TRUE\n"
                + "		AND hidden = FALSE\n"
                + "		AND viewconfirmedby IS NULL\n"
                + "		AND municipality_municode = ?;";

        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        UserIntegrator ui = getUserIntegrator();
        PropertyIntegrator pi = getPropertyIntegrator();
        CaseIntegrator ci = getCaseIntegrator();

        try {

            stmt = con.prepareStatement(query);
            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(start));
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(end));
            stmt.setInt(3, m.getMuniCode());
            rs = stmt.executeQuery();

            while (rs.next()) {
                CECaseEvent ev = new CECaseEvent(new Event());

                ev.setEventID(rs.getInt("eventid"));
                ev.setCategory(getEventCategory(rs.getInt("categoryid")));
                ev.setCaseID(rs.getInt("caseid"));
                LocalDateTime dt = rs.getTimestamp("dateofrecord").toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime();
                ev.setDateOfRecord(dt);

                ev.setTimestamp(rs.getTimestamp("eventtimestamp").toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime());
                ev.setDescription(rs.getString("eventDescription"));
                ev.setOwner(ui.getUser(rs.getInt("owner_userid")));
                ev.setDiscloseToMunicipality(rs.getBoolean("disclosetomunicipality"));

                ev.setDiscloseToPublic(rs.getBoolean("disclosetopublic"));
                ev.setActive(rs.getBoolean("activeevent"));

                Timestamp ldt = rs.getTimestamp("viewconfirmedat");
                // removed action requests
                ev.setHidden(rs.getBoolean("hidden"));
                ev.setNotes(rs.getString("notes"));

                // now for case and prop info
//                ev.setCaseID(ci.getCECase(rs.getInt("caseid")));
//                eventList.add(ev);

            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
//            throw new IntegrationException("Cannot retrive event", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return eventList;

    }

   
    
    /**
     * @deprecated 
     * @param ec
     * @throws IntegrationException 
     */
    public void clearResponseToActionRequest(CECaseEvent ec) throws IntegrationException{
         String query = "UPDATE public.ceevent\n" +
            "   SET responsetimestamp=NULL, respondernotes=NULL, \n" +
            "       responseevent_eventid=NULL, rejeecteventrequest=FALSE, "
                + "responderactual_userid=NULL WHERE eventid = ?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, ec.getEventID());
            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive event", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
  
    


} // close class
