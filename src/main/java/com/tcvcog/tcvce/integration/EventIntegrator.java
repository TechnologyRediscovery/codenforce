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
import com.tcvcog.tcvce.domain.EventExceptionDeprecated;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.EventCECase;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.EventWithCasePropInfo;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.search.SearchParamsCEEvents;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
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
    
    public EventCategory getEventCategory(int catID) throws IntegrationException{
        
        String query = "SELECT * FROM public.ceeventcategory WHERE categoryID = ?";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        EventCategory ec = new EventCategory();
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setInt(1, catID);
            //System.out.println("EventInteegrator.getEventCategory| sql: " + stmt.toString());
            rs = stmt.executeQuery();
            
            while(rs.next()){
                ec = generateEventCategoryFromRS(rs);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot get event categry", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return ec;
    }
    
    public EventCategory generateEventCategoryFromRS(ResultSet rs) throws SQLException{
        EventCategory ec = new EventCategory();
        ec.setCategoryID(rs.getInt("categoryid"));
        ec.setEventType(EventType.valueOf(rs.getString("categoryType")));
        ec.setEventCategoryTitle(rs.getString("title"));
        ec.setEventCategoryDesc(rs.getString("description"));
        
        ec.setUserdeployable(rs.getBoolean("userdeployable"));
        ec.setMunideployable(rs.getBoolean("munideployable"));
        ec.setPublicdeployable(rs.getBoolean("publicdeployable"));
        ec.setRequiresviewconfirmation(rs.getBoolean("requiresviewconfirmation"));
        ec.setNotifycasemonitors(rs.getBoolean("notifycasemonitors"));
        ec.setCasephasechangetrigger(rs.getBoolean("casephasechangetrigger"));
        ec.setHidable(rs.getBoolean("hidable"));
        
        return ec;
        
    }
    
    public ArrayList<EventCategory> getEventCategoryList() throws IntegrationException{
        String query = "SELECT * FROM public.ceeventcategory;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<EventCategory> categoryList = new ArrayList();
        
        try {
            
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            System.out.println("EventIntegrator.getEventCategoryList | SQL: " + stmt.toString());
            
            while(rs.next()){
                categoryList.add(generateEventCategoryFromRS(rs));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot generate list of event categories", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return categoryList;
    }
    
    public ArrayList<EventCategory> getEventCategoryList(EventType et) throws IntegrationException{
         String query = "SELECT * FROM public.ceeventcategory WHERE categorytype = cast (? as ceeventtype);";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<EventCategory> categoryList = new ArrayList();
        
        try {
            
            stmt = con.prepareStatement(query);
            stmt.setString(1, et.toString());
            rs = stmt.executeQuery();
            System.out.println("EventIntegrator.getEventCategoryList | SQL: " + stmt.toString());
            
            while(rs.next()){
                categoryList.add(generateEventCategoryFromRS(rs));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot generate list of event categories", ex);
            
        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return categoryList;
    }
    
    
    public void insertEventCategory(EventCategory ec) throws IntegrationException{
        
        String query = "INSERT INTO public.ceeventcategory(\n" +
                "categoryid, "
                + "categorytype, title, description, "
                + "userdeployable, munideployable, publicdeployable, "
                + "requiresviewconfirmation, notifycasemonitors, casephasechangetrigger, "
                + "hidable)\n" +
                    "    VALUES (DEFAULT, CAST (? as ceeventtype), ?, ?, ?, \n" +
                    "            ?, ?, ?, ?, \n" +
                    "            ?, ?);";
                
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
            
            stmt.setBoolean(7, ec.isRequiresviewconfirmation());
            stmt.setBoolean(8, ec.isNotifycasemonitors());
            stmt.setBoolean(9, ec.isCasephasechangetrigger());
            
            stmt.setBoolean(10, ec.isHidable());
            
            System.out.println("EventInteegrator.insertEventCategory| sql: " + stmt.toString());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert event category", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    public void updateEventCategory(EventCategory ec) throws IntegrationException{
        
          String query = "UPDATE public.ceeventcategory\n" +
"   SET categorytype=CAST (? as ceeventtype), title=?, description=?, userdeployable=?, \n" +
"       munideployable=?, publicdeployable=?, requiresviewconfirmation=?, \n" +
"       notifycasemonitors=?, casephasechangetrigger=?, hidable=?\n" +
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
            
            stmt.setBoolean(7, ec.isRequiresviewconfirmation());
            stmt.setBoolean(8, ec.isNotifycasemonitors());
            stmt.setBoolean(9, ec.isCasephasechangetrigger());
            
            stmt.setBoolean(10, ec.isHidable());
            
            stmt.setInt(11, ec.getCategoryID());
            
            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update event category", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    public void deleteEventCategory(EventCategory ec) throws IntegrationException{
         String query = "DELETE FROM public.ceeventcategory\n" +
                " WHERE categoryid = ?;";
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

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    /**
     * Attaches an Event to a code enforcement case. No checking of logic occurs
     * in this integration method, so the caller should always be a coordiantor
     * who has vetted the event and the associated case.
     * @param event a fully-baked event ready for insertion. An EventCECase contains
 an integer of the caseID to which the event should be attached
     * @throws IntegrationException when the system is unable to store event in DB
     */
    public void insertEvent(EventCECase event) throws IntegrationException{
        PersonIntegrator pi = getPersonIntegrator();
        int insertedEventID = 0;
        
        String query = "INSERT INTO public.ceevent(\n" +
            "            eventid, ceeventcategory_catid, cecase_caseid, dateofrecord, \n" +
            "            eventtimestamp, eventdescription, login_userid, disclosetomunicipality, \n" +
            "            disclosetopublic, activeevent, requiresviewconfirmation, \n" +
            "            hidden, notes)\n" +
            "    VALUES (DEFAULT, ?, ?, ?, \n" +
            "            now(), ?, ?, ?, \n" +
            "            ?, ?, ?, "
                        + "?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, event.getCategory().getCategoryID());
            stmt.setInt(2, event.getCaseID());
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(event.getDateOfRecord()));
            
            // note that the timestamp is set by a call to postgres's now()
            stmt.setString(4, event.getEventDescription());
            stmt.setInt(5, event.getEventOwnerUser().getUserID());
            stmt.setBoolean(6, event.isDiscloseToMunicipality());
            
            stmt.setBoolean(7, event.isDiscloseToPublic());
            stmt.setBoolean(8, event.isActiveEvent());
            stmt.setBoolean(9, event.isRequiresViewConfirmation());
            stmt.setBoolean(10, event.isHidden());
            stmt.setString(11, event.getNotes());
            
            System.out.println("EventIntegrator.insertEventCategory| sql: " + stmt.toString());
            stmt.execute();
            
            String retrievalQuery = "SELECT currval('ceevent_eventID_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            
            rs = stmt.executeQuery();
            while(rs.next()){
                insertedEventID = rs.getInt(1);
                System.out.println("EventIntegrator.insertEvent | retrieved eventID: " + insertedEventID);
                
            }
            

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert Event into system", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        // now connect people to event that has already been logged
        
        ArrayList<Person> al = event.getEventPersons();
        event.setEventID(insertedEventID);
        
        if(al != null){
            if( al.size() > 0 && event.getEventID() != 0){
                pi.connectPersonsToEvent(event, al);
            } 
        }
        
        
    } // close method
    
    
    
    public void updateEvent(EventCECase event, boolean clearViewConfirmation) throws IntegrationException{
        String query = "UPDATE public.ceevent\n" +
            "   SET ceeventcategory_catid=?, cecase_caseid=?, dateofrecord=?, \n" +
            "       eventtimestamp=now(), eventdescription=?, login_userid=?, disclosetomunicipality=?, \n" +
            "       disclosetopublic=?, activeevent=?, \n" +
            "       hidden=?, notes=?\n" +
            " WHERE eventid = ?;";
        
        // TO DO: finish clearing view confirmation
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, event.getCategory().getCategoryID());
            stmt.setInt(2, event.getCaseID());
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(event.getDateOfRecord()));
            
            // timestamp is updated with a call to postgres's now()
            stmt.setString(4, event.getEventDescription());
            stmt.setInt(5, event.getEventOwnerUser().getUserID());
            stmt.setBoolean(6, event.isDiscloseToMunicipality());
            
            stmt.setBoolean(7, event.isDiscloseToPublic());
            stmt.setBoolean(8, event.isActiveEvent());
            stmt.setBoolean(9, event.isHidden());
            stmt.setString(10, event.getNotes());
            stmt.setInt(11, event.getEventID());
            
            System.out.println("EventInteegrator.getEventByEventID| sql: " + stmt.toString());

           stmt.executeUpdate();
           
           // only call the method if the view has been confirmed--so there's something to clear
           if(clearViewConfirmation && (event.getViewConfirmedAt() != null)){
               clearViewConfFromEvent(event);
           }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive event", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    private void clearViewConfFromEvent(EventCECase ec) throws IntegrationException{
        String query = "UPDATE ceevent SET viewconfirmedby = null, "
                + "viewconfirmedat = null WHERE eventid = ?;";
        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, ec.getEventID());
            
            System.out.println("EventIntegrator.clearViewConfFromEvent | stmt: " + stmt.toString());
           stmt.executeUpdate(); 
           
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive event", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    public void deleteEvent(EventCECase event) throws IntegrationException{
        String query = "DELETE FROM public.ceevent WHERE eventid = ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, event.getCaseID());

           stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot delete event--probalby because one or"
                    + "more other entries reference this event. ", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
        
    private EventWithCasePropInfo generateSuperEvent(ResultSet rs) throws SQLException, IntegrationException{
        PropertyIntegrator pi = getPropertyIntegrator();
        CaseIntegrator ci = getCaseIntegrator();
        EventWithCasePropInfo ev = new EventWithCasePropInfo();
        ev = (EventWithCasePropInfo) generateEventFromRS(rs, ev);
        ev.setEventProp(pi.getProperty(rs.getInt("propertyid")));
        ev.setEventCase(ci.getCECase(rs.getInt("caseid")));
        return ev;

    }
    
    
    private EventCECase generateEventFromRS(ResultSet rs, EventCECase premadeEvent) throws SQLException, IntegrationException{
        EventCECase ev;
        if(premadeEvent != null){
            ev = premadeEvent; 
        } else {
            ev = new EventCECase();
        }
        UserIntegrator ui = getUserIntegrator();
        
        ev.setEventID(rs.getInt("eventid"));
        ev.setCategory(getEventCategory(rs.getInt("ceeventCategory_catID")));
        ev.setCaseID(rs.getInt("cecase_caseid"));
        LocalDateTime dt = rs.getTimestamp("dateofrecord").toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        ev.setDateOfRecord(dt);
        ev.setPrettyDateOfRecord(getPrettyDate(dt));
        
        ev.setEventTimeStamp(rs.getTimestamp("eventtimestamp").toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());
        ev.setEventDescription(rs.getString("eventDescription"));
        ev.setEventOwnerUser(ui.getUser(rs.getInt("login_userid")));
        ev.setDiscloseToMunicipality(rs.getBoolean("disclosetomunicipality"));
        
        ev.setDiscloseToPublic(rs.getBoolean("disclosetopublic"));
        ev.setActiveEvent(rs.getBoolean("activeevent"));
        
        ev.setRequiresViewConfirmation(rs.getBoolean("requiresviewconfirmation"));
        Timestamp ldt = rs.getTimestamp("viewconfirmedat");
        if(ldt !=  null){
            ev.setViewConfirmedBy(ui.getUser(rs.getInt("viewconfirmedby")));
            ev.setViewConfirmedAt(rs.getTimestamp("viewconfirmedat").toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime());
            ev.setViewConfirmed(true);
            
        }
        ev.setHidden(rs.getBoolean("hidden"));
        ev.setNotes(rs.getString("notes"));
        
        return ev;
    }
    
      /*
        
        sb.append("SELECT ceevent.eventid, ");
        sb.append("ceevent.notes, ");
        sb.append("ceevent.hidden, ");
        sb.append("ceevent.ceeventcategory_catid, ");
        sb.append("ceevent.requiresviewconfirmation, ");
        sb.append("ceevent.activeevent, ");
        sb.append("ceevent.disclosetopublic, ");
        sb.append("ceevent.disclosetomunicipality, ");
        sb.append("ceevent.login_userid, ");
        sb.append("ceevent.eventdescription, ");
        sb.append("ceevent.eventtimestamp, ");
        sb.append("ceevent.dateofrecord, ");
        sb.append("ceevent.viewconfirmedby, ");
        sb.append("ceevent.viewconfirmedat, ");
        sb.append("property.propertyid, ");
        sb.append("cecase.caseid, ");
        sb.append("ceeventcategory.categoryid ");
        sb.append("FROM ceevent INNER JOIN ceeventcategory ON (ceeventcategory_catid = categoryid) ");
        sb.append("INNER JOIN cecase ON (cecase_caseid = caseid) ");
        sb.append("INNER JOIN property on (property_propertyid = propertyid) ");
        sb.append("WHERE categorytype = CAST ('Timeline' AS ceeventtype) ");
        sb.append("AND municipality_municode = ?");
        */
    
    public List<EventWithCasePropInfo> getEvents(SearchParamsCEEvents params) throws IntegrationException, IntegrationException{
        List<EventWithCasePropInfo> eventList = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        Connection con = getPostgresCon();
        
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ceevent.eventid, ");
        sb.append("FROM ceevent INNER JOIN ceeventcategory ON (ceeventcategory_catid = categoryid) ");
        sb.append("INNER JOIN cecase ON (cecase_caseid = caseid) ");
        sb.append("INNER JOIN property on (property_propertyid = propertyid) ");
        sb.append("WHERE ");
        // as long as this isn't an ID only search, do the normal SQL building process
         if (!params.isFilterByObjectID()) {
            sb.append("municipality_municode = ? ");
            if(params.isFilterByStartEndDate() || params.isFilterByViewConfirmedAtDateRange()){
                sb.append("AND dateofrecord BETWEEN ? AND ? "); // parm 2 and 3 without ID
            } 
            
            if(params.isFilterByEventCategory()){
                sb.append("AND ceeventcategory_catid = ? ");
            }
            
            if(params.isFilterByEventType()){
                sb.append("AND categorytype = CAST ('?' AS ceeventtype) ");
            }
            
            if(params.isFilterByCaseID()){
                sb.append("AND cecase_caseid = ?");
            }
            
            if(params.isFilterByEventOwner()){
                sb.append("AND login_userid = ?");
            }
            
            if(params.isFilterByActive()){
                if(params.isIsActive()){
                    sb.append("AND activeevent = TRUE ");
                } else {
                    sb.append("AND activeevent = FALSE ");
                }
            }
            
            if(params.isFilterByRequiresViewConfirmation()){
                if(params.isIsViewConfirmationRequired()){
                    sb.append("AND requiresviewconfirmation = TRUE ");
                } else {
                    sb.append("AND requiresviewconfirmation = FALSE ");
                }
            }
            
            if(params.isFilterByHidden()){
                if(params.isIsHidden()){
                    sb.append("AND hidden = TRUE ");
                } else {
                    sb.append("AND hidden = FALSE ");
                }
            }
            
            if(params.isFilterByViewed()){
                if(params.isIsViewed()){
                    sb.append("AND viewconfirmedby IS NOT NULL ");
                } else {
                    sb.append("AND viewconfirmedby IS NULL ");
                }
            }
            
            if(params.isFilterByViewConfirmedBy()){    
                sb.append("AND viewconfirmedby = ? ");
            }
            
        } else {
            sb.append("AND eventid = ? "); // will be param 2 with ID search
        }
        sb.append(";");


        try {
            stmt = con.prepareStatement(sb.toString());
            
            if(!params.isFilterByObjectID()){
                stmt.setInt(1, params.getMuni().getMuniCode());
                if(params.isFilterByStartEndDate()){
                    stmt.setTimestamp(2, params.getStartDateSQLDate());
                    stmt.setTimestamp(3, params.getEndDateSQLDate());
                }
            } else {
                stmt.setInt(1, params.getObjectID());
            }
            
            rs = stmt.executeQuery();
            
            int counter = 0;
            int maxResults;
            if(params.isLimitResultCountTo100()){
                maxResults = 100;
            } else {
                maxResults = Integer.MAX_VALUE;
            }
            while(rs.next() && counter < maxResults){
                eventList.add(generateSuperEvent(rs));
                counter++;
            }

        } catch (SQLException ex) {
            System.out.println(ex);
            throw new IntegrationException("Integration Error: Problem retrieving and generating action request list", ex);
        } finally {
            
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }

        }// close try/catch

        return eventList;
    }

    
    /**
     * First gen query for a single purpose: introducing SearchParams objects with SQL assembly logic
     * in the integration methods
     * 
     * @deprecated 
     * @param m
     * @param start
     * @param end
     * @return
     * @throws IntegrationException 
     */
    public List<EventWithCasePropInfo> getUpcomingTimelineEvents(Municipality m, LocalDateTime start, LocalDateTime end) throws IntegrationException{
        
        ArrayList<EventWithCasePropInfo> eventList = new ArrayList<>();
        
        String query = "SELECT ceevent.eventid, ceevent.ceeventcategory_catid, ceevent.dateofrecord, \n" +
"       ceevent.eventtimestamp, ceevent.eventdescription, ceevent.login_userid, ceevent.disclosetomunicipality, \n" +
"       ceevent.disclosetopublic, ceevent.activeevent, ceevent.requiresviewconfirmation, ceevent.hidden, \n" +
"       ceevent.notes, ceevent.viewconfirmedby, ceevent.viewconfirmedat, property.propertyid, cecase.caseid, ceeventcategory.categoryid\n" +
" FROM ceevent 	INNER JOIN ceeventcategory ON (ceeventcategory_catid = categoryid)\n" +
"		INNER JOIN cecase ON (cecase_caseid = caseid)\n" +
"		INNER JOIN property on (property_propertyid = propertyid)\n" +
" WHERE categorytype = CAST ('Timeline' AS ceeventtype)\n" +
"		AND dateofrecord >= ? AND dateofrecord <= ? \n" +
"		AND activeevent = TRUE\n" +
"		AND ceevent.requiresviewconfirmation = TRUE\n" +
"		AND hidden = FALSE\n" +
"		AND viewconfirmedby IS NULL\n" +
"		AND municipality_municode = ?;";
        
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
            System.out.println("EventIntegrator.getUpcomingTimelineEvents | stmt: " + stmt.toString());
            rs = stmt.executeQuery();
            System.out.println("EventIntegrator.getUpcomingTimelineEvents | rs size: " + rs.getFetchSize());

            while(rs.next()){
                EventWithCasePropInfo ev = new EventWithCasePropInfo();
        
                ev.setEventID(rs.getInt("eventid"));
                ev.setCategory(getEventCategory(rs.getInt("categoryid")));
                ev.setCaseID(rs.getInt("caseid"));
                LocalDateTime dt = rs.getTimestamp("dateofrecord").toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime();
                ev.setDateOfRecord(dt);
                ev.setPrettyDateOfRecord(getPrettyDate(dt));

                ev.setEventTimeStamp(rs.getTimestamp("eventtimestamp").toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime());
                ev.setEventDescription(rs.getString("eventDescription"));
                ev.setEventOwnerUser(ui.getUser(rs.getInt("login_userid")));
                ev.setDiscloseToMunicipality(rs.getBoolean("disclosetomunicipality"));

                ev.setDiscloseToPublic(rs.getBoolean("disclosetopublic"));
                ev.setActiveEvent(rs.getBoolean("activeevent"));

                ev.setRequiresViewConfirmation(rs.getBoolean("requiresviewconfirmation"));
                Timestamp ldt = rs.getTimestamp("viewconfirmedat");
                if(ldt !=  null){
                    ev.setViewConfirmedBy(ui.getUser(rs.getInt("viewconfirmedby")));
                    ev.setViewConfirmedAt(rs.getTimestamp("viewconfirmedat").toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDateTime());
                    ev.setViewConfirmed(true);
                }
                ev.setHidden(rs.getBoolean("hidden"));
                ev.setNotes(rs.getString("notes"));
                
                // now for case and prop info
                
                ev.setEventProp(pi.getProperty(rs.getInt("propertyid")));
                ev.setEventCase(ci.getCECase(rs.getInt("caseid")));
                eventList.add(ev);
                
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
//            throw new IntegrationException("Cannot retrive event", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return eventList;
        
    }
    
    public List<EventCECase> getEventsRequiringViewConfirmation(User u){
        EventCECase ev = null;
        ArrayList<EventCECase> eventList = new ArrayList<>();
        
        String query = "";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
//            stmt.setInt(1, eventID);
            System.out.println("EventInteegrator.getEventByEventID| sql: " + stmt.toString());
            rs = stmt.executeQuery();

            while(rs.next()){
//                ev = generateEventFromRS(rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
//            throw new IntegrationException("Cannot retrive event", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return eventList;
        
    }
    
    public void confirmEventView(User u, EventCECase ev) throws IntegrationException{
        
        String query = "UPDATE ceevent SET viewconfirmedby = ?,\n" +
"		viewconfirmedat = now()\n" +
"		WHERE eventid = ?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, u.getUserID());
            stmt.setInt(2, ev.getEventID());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot udpate event with view details, sorry", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    

    public EventCECase getEventByEventID(int eventID) throws IntegrationException{
        EventCECase ev = null;
        
        String query = "SELECT * FROM public.ceevent WHERE eventid = ?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, eventID);
            System.out.println("EventInteegrator.getEventByEventID| sql: " + stmt.toString());
            rs = stmt.executeQuery();

            while(rs.next()){
                ev = generateEventFromRS(rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive event", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return ev;
    }
    
    public ArrayList<EventCECase> getEventsByCaseID(int caseID) throws IntegrationException{
        ArrayList<EventCECase> eventList = new ArrayList();
        
        String query = "SELECT * FROM public.ceevent WHERE cecase_caseid = ?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, caseID);
            System.out.println("EventIntegrator.getEventsByCaseID| sql: " + stmt.toString());
            rs = stmt.executeQuery();

            while(rs.next()){
                eventList.add(generateEventFromRS(rs));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot generate case list", ex);

        } finally{
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
             if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return eventList;
    }
    

    
    
} // close class