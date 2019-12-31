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
import com.tcvcog.tcvce.application.interfaces.IFace_EventRuleGoverned;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventRuleCECase;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.EventRuleAbstract;
import com.tcvcog.tcvce.entities.EventRuleImplementation;
import com.tcvcog.tcvce.entities.EventRuleSet;
import com.tcvcog.tcvce.entities.MuniProfile;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.search.QueryEvent;
import com.tcvcog.tcvce.entities.search.SearchParamsEvent;
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
 * @author ellen bascomb of apt 31y
 */
public class EventIntegrator extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of EventIntegrator
     */
    public EventIntegrator() {
    }
    
    
//    --------------------------------------------------------------------------
//    ******************************** EVENTS **********************************
//    --------------------------------------------------------------------------
    
    
    /**
     * Base object creation method under the Grand Unified EventCnF GER Model
     * 
     * @param evid
     * @return a fully-baked event, not configured for any application in 
     * a given authcontext
     * @throws IntegrationException 
     */
    public EventCnF getEvent(int evid) throws IntegrationException{
        
        EventCoordinator ec = getEventCoordinator();
        
        String query = "SELECT eventid, category_catid, cecase_caseid, eventtimestamp, \n" +
                    "       eventdescription, owner_userid, disclosetomunicipality, disclosetopublic, \n" +
                    "       activeevent, notes, occperiod_periodid, timestart, timeend\n" +
                    "  FROM public.event WHERE eventid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        EventCnF ev = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, evid);
            rs = stmt.executeQuery();
            while (rs.next()) {
                ev = generateEventFromRS(rs);
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot generate list of event categories", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return ev;
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
    private EventCnF generateEventFromRS(ResultSet rs) throws SQLException, IntegrationException {
        EventCnF ev = new EventCnF();
        UserIntegrator ui = getUserIntegrator();
        PersonIntegrator pi = getPersonIntegrator();
        
        ev.setEventID(rs.getInt("eventid"));
        ev.setCategory(getEventCategory(rs.getInt("category_catid")));

        ev.setCeCaseID(rs.getInt("cecase_caseid"));
        ev.setOccPeriodID(rs.getInt("occperiod_periodid"));

        ev.setTimestamp(rs.getTimestamp("eventtimestamp").toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());
        ev.setDescription(rs.getString("eventDescription"));
        ev.setOwner(ui.getUser(rs.getInt("owner_userid")));
       
        ev.setDiscloseToMunicipality(rs.getBoolean("disclosetomunicipality"));
        ev.setDiscloseToPublic(rs.getBoolean("disclosetopublic"));
        ev.setActive(rs.getBoolean("activeevent"));

        ev.setNotes(rs.getString("notes"));
        if (rs.getTimestamp("timestart") != null) {
            LocalDateTime dt = rs.getTimestamp("timestart").toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();
            ev.setTimeStart(dt);
        }
        
        if (rs.getTimestamp("timeend") != null) {
            LocalDateTime dt = rs.getTimestamp("timeend").toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();
            ev.setTimeEnd(dt);
        }

        ev.setPersonList(pi.getPersonList(ev));
        
        return ev;
    }
    
     public List<EventCnF> getEventList(IFace_EventRuleGoverned erg) throws IntegrationException{
        
     StringBuilder queryStub = new StringBuilder("SELECT eventid FROM public.event WHERE ");
            
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<EventCnF> el = new ArrayList<>();
        
        if(erg == null){
            return el;
        }
        
        try {
            stmt = con.prepareStatement(queryStub.toString());
        
            if(erg instanceof OccPeriod){
                OccPeriod op = (OccPeriod) erg;
                queryStub.append("occperiod_periodid=?;");
                stmt.setInt(1, op.getPeriodID());
            } else if(erg instanceof CECase){
                CECase cec = (CECase) erg;
                queryStub.append("cecase_caseid=?;");
                stmt.setInt(1, cec.getCaseID());
            }
            
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                el.add(getEvent(rs.getInt("eventid")));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot generate list of event categories", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return el;
    }
    
    /**
     * Attaches an EventCnF to a code enforcement case. No checking of logic occurs
     * in this integration method, so the caller should always be a coordiantor
     * who has vetted the event and the associated case.
     *
     * @param event
     * @return the id of the event just inserted
     * @throws IntegrationException when the system is unable to store event in
     * DB
     */
    public int insertEvent(EventCnF event) throws IntegrationException {
        if(event == null) return 0;
        PersonIntegrator pi = getPersonIntegrator();
        int insertedEventID = 0;

        String query = "INSERT INTO public.event(\n" +
                        "            eventid, category_catid, cecase_caseid, dateofrecord, eventtimestamp, \n" +
                        "            eventdescription, owner_userid, disclosetomunicipality, disclosetopublic, \n" +
                        "            activeevent, notes, occperiod_periodid, timestart, timeend)\n" +
                        "    VALUES (DEFAULT, ?, ?, now(), \n" +
                        "            ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, ?);"; 
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, event.getCategory().getCategoryID());
            
            if(event.getCeCaseID() != 0){
                stmt.setInt(2, event.getCeCaseID());
            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }

            // note that the timestamp is set by a call to postgres's now()
            stmt.setString(3, event.getDescription());
            stmt.setInt(4, event.getOwner().getUserID());
            stmt.setBoolean(5, event.isDiscloseToMunicipality());
            stmt.setBoolean(6, event.isDiscloseToPublic());
            
            stmt.setBoolean(7, event.isActive());
            stmt.setString(8, event.getNotes());
            
            if(event.getOccPeriodID() != 0){
                stmt.setInt(9, event.getOccPeriodID());
            } else {
                stmt.setNull(9, java.sql.Types.NULL);
            }
            
            if (event.getTimeStart() != null) {
                stmt.setTimestamp(10, java.sql.Timestamp.valueOf(event.getTimeStart()));
            } else {
                stmt.setNull(10, java.sql.Types.NULL);
            }
            
            
            if (event.getTimeEnd() != null) {
                stmt.setTimestamp(11, java.sql.Timestamp.valueOf(event.getTimeEnd()));
            } else {
                stmt.setNull(11, java.sql.Types.NULL);
            }
            
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
        List<Person> persList = event.getPersonList();
        event.setEventID(insertedEventID);

        if (persList != null) {
            if (persList.size() > 0 && event.getEventID() != 0) {
                pi.eventPersonsConnect(event, persList);
            }
        }
        
        return insertedEventID;

    } // close method
    


    public void updateEvent(EventCnF event) throws IntegrationException {
        if(event == null) return;
        PersonIntegrator pi = getPersonIntegrator();
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE public.event ");
        sb.append("   SET category_catid=?, cecase_caseid=?, ");
        sb.append("       eventdescription=?, owner_userid=?, disclosetomunicipality=?, ");
        sb.append("       disclosetopublic=?, activeevent=?, ");
        sb.append("       notes=?, occperiod_periodid=?, timestart=?, timeend=? ");
        sb.append(" WHERE eventid = ?;");

        // TO DO: finish clearing view confirmation
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(sb.toString());
           
            stmt.setInt(1, event.getCategory().getCategoryID());
            
            if(event.getCeCaseID() != 0){
                stmt.setInt(2, event.getCeCaseID());
            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }
            
          

            // note that the timestamp is set by a call to postgres's now()
            stmt.setString(3, event.getDescription());
            stmt.setInt(4, event.getOwner().getUserID());
            stmt.setBoolean(5, event.isDiscloseToMunicipality());
            stmt.setBoolean(6, event.isDiscloseToPublic());
            
            stmt.setBoolean(7, event.isActive());
            stmt.setString(8, event.getNotes());
            
            if(event.getOccPeriodID() != 0){
                stmt.setInt(9, event.getOccPeriodID());
            } else {
                stmt.setNull(9, java.sql.Types.NULL);
            }
            
              if (event.getTimeStart() != null) {
                stmt.setTimestamp(10, java.sql.Timestamp.valueOf(event.getTimeStart()));
            } else {
                stmt.setNull(10, java.sql.Types.NULL);
            }
            
            
            if (event.getTimeEnd() != null) {
                stmt.setTimestamp(11, java.sql.Timestamp.valueOf(event.getTimeEnd()));
            } else {
                stmt.setNull(11, java.sql.Types.NULL);
            }
            
            stmt.setInt(12, event.getEventID());
            
            stmt.executeUpdate();
            
           

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive event", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
   
    public void deleteEvent(EventCnF event) throws IntegrationException {
        if(event == null) return;
        
        String query = "DELETE FROM public.event WHERE eventid = ?;";
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
     * Primary search method for EventCnF objects system wide!
     * @param params
     * @return
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public List<Integer> searchForEvents(SearchParamsEvent params) 
            throws IntegrationException, BObStatusException {
        List<Integer> evidlst = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        Connection con = getPostgresCon();
        boolean notFirstCriteria = false;

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT event.eventid ");
        sb.append("FROM event INNER JOIN eventcategory ON (category_catid = categoryid) ");
        sb.append("INNER JOIN cecase ON (cecase_caseid = caseid) ");
        sb.append("INNER JOIN property ON (property_propertyid = propertyid) ");
        sb.append("WHERE eventid IS NOT NULL AND ");
        // as long as this isn't an ID only search, do the normal SQL building process
        if (!params.isBobID_ctl()) {
            if (params.isMuni_ctl()) {
                sb.append("municipality_municode = ? "); // param 1
            }

            if (params.isDate_startEnd_ctl()){
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
                sb.append("categorytype = CAST (? AS ceeventtype) ");
            }

            if (params.isFilterByEventCategory() ) {
                sb.append("eventcategory_catid = ? ");
            }


            if (params.isFilterByCaseID()) {
                sb.append("cecase_caseid = ? ");
            }

            if (params.isFilterByEventOwner() && params.getUserID() != 0) {
                    sb.append("ceevent.owner_userid = ? ");
            }
            
            if (params.isFilterByPerson()) {
//                if(notFirstCriteria){sb.append("AND ");} else {notFirstCriteria = true;}
//                sb.append("person_personid = ?");
            }


            if (params.isActive_ctl()) {
                if (params.isIsActive()) {
                    sb.append("activeevent = TRUE ");
                } else {
                    sb.append("activeevent = FALSE ");
                }
            }
            
            if (params.isFilterByHidden()) {
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

            if (!params.isBobID_ctl()) {
                if (params.isMuni_ctl()) {
                    stmt.setInt(++paramCounter, params.getMuni_val().getMuniCode());
                }
                if (params.isDate_startEnd_ctl()) {
                    stmt.setTimestamp(++paramCounter, params.getStartDate_val_SQLDate());
                    stmt.setTimestamp(++paramCounter, params.getEndDate_val_SQLDate());
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
                    params.getUserID() != 0) {
                        stmt.setInt(++paramCounter, params.getUserID());
                }

                if (params.isFilterByPerson()) {
//                    stmt.setInt(++paramCounter, params.getPerson().getPersonID());
                }
                
            } else {
                stmt.setInt(++paramCounter, params.getBobID_val());
            }

            rs = stmt.executeQuery();

            int counter = 0;
            int maxResults;
            if (params.isLimitResultCount_ctl()) {
                maxResults = 100;
            } else {
                maxResults = Integer.MAX_VALUE;
            }
            while (rs.next() && counter < maxResults) {
                evidlst.add(rs.getInt("eventid"));
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

        return evidlst;
    }
        
//    --------------------------------------------------------------------------
//    ************************** EVENT CATEGORIES ****************************** 
//    --------------------------------------------------------------------------
    
    
     /**
     * Base record retrieval method for EventCategory objects
     * @param catID
     * @return fully baked
     * @throws IntegrationException 
     */
    public EventCategory getEventCategory(int catID) throws IntegrationException {

        String query = " SELECT categoryid, categorytype, title, description, userdeployable, \n" +
                        "       munideployable, publicdeployable, notifycasemonitors, hidable, \n" +
                        "       icon_iconid, relativeorderwithintype, relativeorderglobal, hosteventdescriptionsuggtext, \n" +
                        "       directive_directiveid, defaultdurationmins \n" +
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

    /**
     * Extracts values from ResultSet and populates object as appropriate
     * @param rs
     * @return
     * @throws SQLException
     * @throws IntegrationException 
     */
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
        
        ec.setDefaultdurationmins(rs.getInt("defaultdurationmins"));
        return ec;
    }
   
    
    
    /**
     * Fetches a complete list of eventcategory records, including inactive cats
     * @return
     * @throws IntegrationException 
     */
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
     * List produced here ultimately used for actively picking event categories by the user
     * @param et
     * @return
     * @throws IntegrationException
     */
    public List<EventCategory> getEventCategoryList(EventType et) throws IntegrationException {
        String query = "SELECT categoryid FROM public.eventcategory WHERE categorytype = CAST (? as eventtype);";
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
                        "            directive_directiveid, defaultdurationmins)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, \n" +
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
            
            stmt.setInt(14, ec.getDefaultdurationmins());

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
                        "       hosteventdescriptionsuggtext=?, directive_directiveid=?, defaultdurationmins=?\n" +
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
            stmt.setInt(14, ec.getDefaultdurationmins());
            
            stmt.setInt(15, ec.getCategoryID());
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
    
    
//    --------------------------------------------------------------------------
//    ************************** EVENT RULES *********************************** 
//    --------------------------------------------------------------------------
    
    
    
     /**
     * Getter for rules by ID
     * 
     * @param ruleid
     * @return
     * @throws IntegrationException 
     */
    public EventRuleAbstract rules_getEventRuleAbstract(int ruleid) throws IntegrationException{
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
                        "       mandatorypassreqtocloseentity, autoremoveonentityclose, promptingdirective_directiveid, \n" +
                        "       triggeredeventcatonpass, triggeredeventcatonfail, active, notes\n" +
                        "  FROM public.eventrule WHERE ruleid=?;";
            stmt = con.prepareStatement(s);
            stmt.setInt(1, ruleid);

            rs = stmt.executeQuery();
            while(rs.next()){
                rule = rules_generateEventRule(rs);
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
    private EventRuleAbstract rules_generateEventRule(ResultSet rs) throws SQLException, IntegrationException{
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
        if(rs.getInt("promptingdirective_directiveid") != 0){
            evRule.setPromptingDirective(ci.getDirective(rs.getInt("promptingdirective_directiveid")));
        }
        if(rs.getInt("triggeredeventcatonpass") != 0){
            evRule.setTriggeredECOnRulePass(getEventCategory(rs.getInt("triggeredeventcatonpass")));
        }
        if(rs.getInt("triggeredeventcatonfail") != 0){
            evRule.setTriggeredECOnRuleFail(getEventCategory(rs.getInt("triggeredeventcatonfail")));
        }
        evRule.setActiveRuleAbstract(rs.getBoolean("active"));
        evRule.setNotes(rs.getString("notes"));
        
        return evRule;
    }
    
    public List<EventRuleAbstract> rules_getEventRuleList(int ruleSetID) throws IntegrationException{
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
                list.add(rules_getEventRuleAbstract(rs.getInt("eventrule_ruleid")));
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
    
    public List<EventRuleSet> rules_getEventRuleSetList(MuniProfile profile) throws IntegrationException{
        List<EventRuleSet> setList = new ArrayList<>();
        String query = "SELECT muniprofile_profileid, ruleset_setid FROM \n" +
"  FROM public.muniprofileeventruleset WHERE muniprofile_profileid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, profile.getProfileID());
            rs = stmt.executeQuery();
            while (rs.next()) {
                setList.add(rules_getEventRuleSet(rs.getInt("ruleset_setid")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot generate list of event rules", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return setList;
    }
    
    public List<EventRuleSet> rules_getEventRuleSetList() throws IntegrationException{
        List<EventRuleSet> setList = new ArrayList<>();
        String query = "SELECT rulesetid FROM public.eventruleset; ";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                setList.add(rules_getEventRuleSet(rs.getInt("rulesetid")));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot generate list of event rules", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return setList;
        
    }
    
    public EventRuleSet rules_getEventRuleSet(int setID) throws IntegrationException{
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
                set = rules_generateRuleSet(rs);
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
    
    private EventRuleSet rules_generateRuleSet(ResultSet rs) throws SQLException, IntegrationException{
        EventRuleSet s = new EventRuleSet();
        s.setRulseSetID(rs.getInt("rulesetid"));
        s.setTitle(rs.getString("title"));
        s.setDescription(rs.getString("description"));
        s.setRuleList(rules_getEventRuleList(rs.getInt("rulesetid")));
        return s;
    }
    
    public List<EventRuleCECase> rules_getEventRuleCECaseList(CECase cse) throws IntegrationException{
        EventRuleImplementation ruleImp = null;
        List<EventRuleCECase> ruleList = new ArrayList<>();
        String query =  "   SELECT cecase_caseid, eventrule_ruleid, attachedts, attachedby_userid, \n" +
                        "       lastevaluatedts, passedrulets, passedrule_eventid, active \n" +
                        "  FROM public.cecaseeventrule WHERE cecase_caseid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, cse.getCaseID());
            rs = stmt.executeQuery();
            while (rs.next()) {
                ruleImp = rules_generateEventRuleImplementation(rs);
                ruleList.add(rules_generateCECaseEventRule(rs, ruleImp));
                
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
    
    private EventRuleCECase rules_generateCECaseEventRule(ResultSet rs, EventRuleImplementation imp) 
            throws SQLException, IntegrationException{
        EventCoordinator ec = getEventCoordinator();
        EventRuleCECase evRule = new EventRuleCECase(imp);
        evRule.setCeCaseID(rs.getInt("cecase_caseid"));
        evRule.setPassedRuleEvent(ec.getEvent(rs.getInt("passedrule_eventid")));
        return evRule;
        
    }
    
    private EventRuleImplementation rules_generateEventRuleImplementation(ResultSet rs) throws SQLException, IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        
        EventRuleImplementation ruleImp = new EventRuleImplementation(rules_getEventRuleAbstract(rs.getInt("eventrule_ruleid")));
        ruleImp.setAttachedTS(rs.getTimestamp("attachedts").toLocalDateTime());
        ruleImp.setAttachedBy(ui.getUser(rs.getInt("attachedby_userid")));
        if(rs.getTimestamp("lastevaluatedts") != null){
            ruleImp.setLastEvaluatedTS(rs.getTimestamp("lastevaluatedts").toLocalDateTime());
        } 
       ruleImp.setAttachedBy(ui.getUser(rs.getInt("attachedby_userid")));
       
       
       return ruleImp;
    }
    
      public int rules_insertEventRule(EventRuleAbstract evrua) throws IntegrationException {

        String query = "INSERT INTO public.eventrule(\n" +
                        "            ruleid, title, description, requiredeventtype, forbiddeneventtype, \n" + // 1-4
                        "            requiredeventcat_catid, requiredeventcatupperboundtypeintorder, \n" + //5-6
                        "            requiredeventcatupperboundglobalorder, forbiddeneventcat_catid, \n" + //7-8
                        "            forbiddeneventcatupperboundtypeintorder, forbiddeneventcatupperboundglobalorder, \n" + //9-10
                        "            mandatorypassreqtocloseentity, autoremoveonentityclose, promptingdirective_directiveid, \n" + //11-13
                        "            triggeredeventcatonpass, triggeredeventcatonfail, active, notes, \n" + //14-17
                        "            requiredeventcatthresholdtypeintorder, forbiddeneventcatthresholdtypeintorder, \n" + //18-19
                        "            requiredeventcatthresholdglobalorder, forbiddeneventcatthresholdglobalorder)\n" + // 20-21
                        "    VALUES (DEFAULT, ?, ?, CAST (? AS eventtype), CAST (? AS eventtype), \n" + 
                        "            ?, ?, \n" + //5-6
                        "            ?, ?, \n" + //7-8
                        "            ?, ?, \n" +
                        "            ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, \n" +
                        "            ?, ?, \n" +
                        "            ?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int freshRuleID = 0;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setString(1, evrua.getTitle());
            stmt.setString(2, evrua.getDescription());
            if(evrua.getRequiredEventType() != null){
                stmt.setString(3, evrua.getRequiredEventType().name());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            if(evrua.getForbiddenEventType() != null){
                stmt.setString(4, evrua.getForbiddenEventType().name());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            if(evrua.getRequiredEventCategory() != null){
                stmt.setInt(5, evrua.getRequiredEventCategory().getCategoryID());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            stmt.setBoolean(6, evrua.isRequiredECThreshold_typeInternalOrder_treatAsUpperBound());
            stmt.setBoolean(7, evrua.isRequiredECThreshold_globalOrder_treatAsUpperBound());
            
            
            if(evrua.getForbiddenEventCategory() != null){
                stmt.setInt(8, evrua.getForbiddenEventCategory().getCategoryID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
            stmt.setBoolean(9, evrua.isForbiddenECThreshold_typeInternalOrder_treatAsUpperBound());
            stmt.setBoolean(10, evrua.isForbiddenECThreshold_globalOrder_treatAsUpperBound());
            
            stmt.setBoolean(11, evrua.isMandatoryRulePassRequiredToCloseEntity());
            stmt.setBoolean(12, evrua.isInactivateRuleOnEntityClose());
            if(evrua.getPromptingDirective()!= null){
                stmt.setInt(13, evrua.getPromptingDirective().getDirectiveID());
            } else {
                stmt.setNull(13, java.sql.Types.NULL);
            }
            
            if(evrua.getTriggeredECOnRulePass() != null){
                stmt.setInt(14, evrua.getTriggeredECOnRulePass().getCategoryID());
            } else {
                stmt.setNull(14, java.sql.Types.NULL);
            }
            
            if(evrua.getTriggeredECOnRuleFail() != null){
                stmt.setInt(15, evrua.getTriggeredECOnRuleFail().getCategoryID());
            } else {
                stmt.setNull(15, java.sql.Types.NULL);
            }
            stmt.setBoolean(16, evrua.isActiveRuleAbstract());
            stmt.setString(17, evrua.getNotes());

            stmt.setInt(18, evrua.getRequiredECThreshold_typeInternalOrder());
            stmt.setInt(19, evrua.getForbiddenECThreshold_typeInternalOrder());
            
            stmt.setInt(20, evrua.getRequiredECThreshold_globalOrder());
            stmt.setInt(21, evrua.getForbiddenECThreshold_globalOrder());
            
            stmt.execute();
            
            String retrievalQuery = "SELECT currval('cecasephasechangerule_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            rs = stmt.executeQuery();
            
            while(rs.next()){
                freshRuleID = rs.getInt(1);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert EventRuleAbstract into the system", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return freshRuleID;
       
    } // close method
      public void rules_updateEventRule(EventRuleAbstract evrua) throws IntegrationException {

        String query = "UPDATE public.eventrule\n" +
                    "   SET title=?, description=?, requiredeventtype=CAST (? AS eventtype), forbiddeneventtype= CAST (? AS eventtype), \n" +
                    "       requiredeventcat_catid=?, requiredeventcatupperboundtypeintorder=?, \n" +
                    "       requiredeventcatupperboundglobalorder=?, forbiddeneventcat_catid=?, \n" +
                    "       forbiddeneventcatupperboundtypeintorder=?, forbiddeneventcatupperboundglobalorder=?, \n" +
                    "       mandatorypassreqtocloseentity=?, autoremoveonentityclose=?, promptingdirective_directiveid=?, \n" +
                    "       triggeredeventcatonpass=?, triggeredeventcatonfail=?, active=?, \n" +
                    "       notes=?, requiredeventcatthresholdtypeintorder=?, forbiddeneventcatthresholdtypeintorder=?, \n" +
                    "       requiredeventcatthresholdglobalorder=?, forbiddeneventcatthresholdglobalorder=?\n" +
                    " WHERE ruleid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setString(1, evrua.getTitle());
            stmt.setString(2, evrua.getDescription());
            if(evrua.getRequiredEventType() != null){
                stmt.setString(3, evrua.getRequiredEventType().name());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            if(evrua.getForbiddenEventType() != null){
                stmt.setString(4, evrua.getForbiddenEventType().name());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            if(evrua.getRequiredEventCategory() != null){
                stmt.setInt(5, evrua.getRequiredEventCategory().getCategoryID());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            stmt.setBoolean(6, evrua.isRequiredECThreshold_typeInternalOrder_treatAsUpperBound());
            stmt.setBoolean(7, evrua.isRequiredECThreshold_globalOrder_treatAsUpperBound());
            
            
            if(evrua.getForbiddenEventCategory() != null){
                stmt.setInt(8, evrua.getForbiddenEventCategory().getCategoryID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
            stmt.setBoolean(9, evrua.isForbiddenECThreshold_typeInternalOrder_treatAsUpperBound());
            stmt.setBoolean(10, evrua.isForbiddenECThreshold_globalOrder_treatAsUpperBound());
            
            stmt.setBoolean(11, evrua.isMandatoryRulePassRequiredToCloseEntity());
            stmt.setBoolean(12, evrua.isInactivateRuleOnEntityClose());
            if(evrua.getPromptingDirective()!= null){
                stmt.setInt(13, evrua.getPromptingDirective().getDirectiveID());
            } else {
                stmt.setNull(13, java.sql.Types.NULL);
            }
            
            if(evrua.getTriggeredECOnRulePass() != null){
                stmt.setInt(14, evrua.getTriggeredECOnRulePass().getCategoryID());
            } else {
                stmt.setNull(14, java.sql.Types.NULL);
            }
            
            if(evrua.getTriggeredECOnRuleFail() != null){
                stmt.setInt(15, evrua.getTriggeredECOnRuleFail().getCategoryID());
            } else {
                stmt.setNull(15, java.sql.Types.NULL);
            }
            stmt.setBoolean(16, evrua.isActiveRuleAbstract());
            stmt.setString(17, evrua.getNotes());

            stmt.setInt(18, evrua.getRequiredECThreshold_typeInternalOrder());
            stmt.setInt(19, evrua.getForbiddenECThreshold_typeInternalOrder());
            
            stmt.setInt(20, evrua.getRequiredECThreshold_globalOrder());
            stmt.setInt(21, evrua.getForbiddenECThreshold_globalOrder());
            
            stmt.setInt(22, evrua.getRuleid());
            
            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot update EventRuleAbstract into the system", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    } // close method
      
    public void rules_insertEventRuleOccPeriod(EventRuleOccPeriod erop) throws IntegrationException{
        UserCoordinator uc = getUserCoordinator();
        
        String query = "INSERT INTO public.occperiodeventrule(\n" +
                        "            occperiod_periodid, eventrule_ruleid, attachedts, attachedby_userid, \n" +
                        "            lastevaluatedts, passedrulets, passedrule_eventid, active)\n" +
                        "    VALUES (?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, erop.getOccPeriodID());
            stmt.setInt(2, erop.getRuleid());
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            if(erop.getAttachedBy() != null){
                stmt.setInt(4, erop.getAttachedBy().getUserID());
            } else {
                stmt.setInt(4, uc.getUserRobot().getUserID());
             
            }
            
            // last evaluated TS
            stmt.setNull(5, java.sql.Types.NULL);
            
            // passed rule TS
            stmt.setNull(6, java.sql.Types.NULL);
            
            if(erop.getPassedRuleEvent() != null){
                stmt.setInt(7, erop.getPassedRuleEvent().getEventID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            
            stmt.setBoolean(8, erop.isActiveRuleAbstract());
            
            stmt.execute();
            System.out.println("EventIntegrator.rules_insertEventRuleOccPeriod | inserted rule on OccPeriod ID " + erop.getOccPeriodID());
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert a new event rule occ period into the system", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    public void rules_addEventRuleAbstractToOccPeriodTypeRuleSet(EventRuleAbstract era, int eventRuleSetID) throws IntegrationException{
        
        
        String query = "INSERT INTO public.eventruleruleset(\n" +
                        "            ruleset_rulesetid, eventrule_ruleid)\n" +
                        "    VALUES (?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, eventRuleSetID);
            stmt.setInt(2, era.getRuleid());
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot add EventRUleAbstract to an occ period type rule set", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }

    public void rules_updateEventRuleOccPeriod(EventRuleOccPeriod erop) throws IntegrationException{
        String query = "UPDATE public.occperiodeventrule\n" +
                        "   SET occperiod_periodid=?, eventrule_ruleid=?, attachedts=?, attachedby_userid=?, \n" +
                        "       lastevaluatedts=?, passedrulets=?, passedrule_eventid=?, active=? \n" +
                        " WHERE occperiod_periodid=? AND eventrule_ruleid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            // note the compound primary key of both the occperiodid and ruleid
            stmt.setInt(1, erop.getOccPeriodID());
            stmt.setInt(2, erop.getRuleid());
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            if(erop.getAttachedBy() != null){
                stmt.setInt(4, erop.getAttachedBy().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            
            stmt.setNull(6, java.sql.Types.NULL);
            
            if(erop.getPassedRuleEvent() != null){
                stmt.setInt(7, erop.getPassedRuleEvent().getEventID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            stmt.setBoolean(8, erop.isActiveRuleAbstract());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert OccPeriodEventRule into the system", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    public void rules_insertEventRuleCECase(EventRuleCECase ercec) throws IntegrationException{
          String query = "INSERT INTO public.cecaseeventrule(\n" +
                        "            cecase_caseid, eventrule_ruleid, attachedts, attachedby_userid, \n" +
                        "            lastevaluatedts, passedrulets, passedrule_eventid, active)\n" +
                        "    VALUES (?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ercec.getCeCaseID());
            stmt.setInt(2, ercec.getRuleid());
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            if(ercec.getAttachedBy() != null){
                stmt.setInt(4, ercec.getAttachedBy().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            
            stmt.setNull(6, java.sql.Types.NULL);
            
            if(ercec.getPassedRuleEvent() != null){
                stmt.setInt(7, ercec.getPassedRuleEvent().getEventID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            stmt.setBoolean(8, ercec.isActiveRuleAbstract());
            stmt.execute();
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert EventRuleCECase into the system", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    public void rules_UpdatetEventRuleCECase(EventRuleCECase ercec) throws IntegrationException{
         String query = "UPDATE public.cecaseeventrule\n" +
                        "   SET cecase_caseid=?, eventrule_ruleid=?, attachedts=?, attachedby_userid=?, \n" +
                        "       lastevaluatedts=?, passedrulets=?, passedrule_eventid=?, active=?\n" +
                        " WHERE cecase_caseid=? AND eventrule_ruleid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, ercec.getCeCaseID());
            stmt.setInt(2, ercec.getRuleid());
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            if(ercec.getAttachedBy() != null){
                stmt.setInt(4, ercec.getAttachedBy().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            stmt.setNull(6, java.sql.Types.NULL);
            
            if(ercec.getPassedRuleEvent() != null){
                stmt.setInt(7, ercec.getPassedRuleEvent().getEventID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            stmt.setBoolean(8, ercec.isActiveRuleAbstract());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot update EventRuleCECase", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    private EventRuleOccPeriod rules_generateEventRuleOccPeriod(ResultSet rs, EventRuleImplementation imp) throws SQLException, IntegrationException{
        EventRuleOccPeriod evRule = new EventRuleOccPeriod(imp);
        evRule.setOccPeriodID(rs.getInt("occperiod_periodid"));
        evRule.setPassedRuleEvent(getOccEvent(rs.getInt("passedrule_eventid")));
        return evRule;
    }
    
    
    public EventRuleOccPeriod rules_getEventRuleOccPeriod(int occperiod_periodid, int eventrule_ruleid) throws IntegrationException{
        EventRuleOccPeriod ruleOccPer = null;
        EventRuleImplementation evRuleImp;
        
        String query = "SELECT occperiod_periodid, eventrule_ruleid, attachedts, attachedby_userid, \n" +
                        "       lastevaluatedts, passedrulets, passedrule_eventid, active \n" +
                        "  FROM public.occperiodeventrule WHERE occperiod_periodid=? and eventrule_ruleid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, occperiod_periodid);
            stmt.setInt(2, eventrule_ruleid);
            rs = stmt.executeQuery();
            while (rs.next()) {
                evRuleImp = rules_generateEventRuleImplementation(rs);
                ruleOccPer = (rules_generateEventRuleOccPeriod(rs, evRuleImp));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot generate list of event rules", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return ruleOccPer;
    }
    
    public List<EventRuleOccPeriod> rules_getEventRuleOccPeriodList(OccPeriod op) throws IntegrationException{
        EventRuleImplementation ruleImp;
        List<EventRuleOccPeriod> ruleList = new ArrayList<>();
        String query = "SELECT occperiod_periodid, eventrule_ruleid, attachedts, attachedby_userid, \n" +
                        "       lastevaluatedts, passedrulets, passedrule_eventid, active \n" +
                        "  FROM public.occperiodeventrule WHERE occperiod_periodid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, op.getPeriodID());
            rs = stmt.executeQuery();
            while (rs.next()) {
                ruleImp = rules_generateEventRuleImplementation(rs);
                ruleList.add(rules_generateEventRuleOccPeriod(rs, ruleImp));
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
    
    
       
} // close class