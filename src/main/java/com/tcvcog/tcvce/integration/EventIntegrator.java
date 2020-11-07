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
import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventDomainEnum;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.IFace_EventHolder;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.search.SearchParamsEvent;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class EventIntegrator extends BackingBeanUtils implements Serializable {

    final String ACTIVE_FIELD = "event.active";
    /**
     * Creates a new instance of EventIntegrator
     */
    public EventIntegrator() {
    }
    
    
//    --------------------------------------------------------------------------
//    ******************************** EVENTS **********************************
//    --------------------------------------------------------------------------
    
    
    /**
     * Base object creation method under the Grand Unified EventCnF GUER Model
     * 
     * @param evid
     * @return a fully-baked event, not configured for any application in 
     * a given authcontext
     * @throws IntegrationException 
     */
    public EventCnF getEvent(int evid) throws IntegrationException{
        
        String query = "SELECT eventid, category_catid, cecase_caseid, creationts, eventdescription, \n" +
                        "       creator_userid, active, notes, occperiod_periodid, timestart, \n" +
                        "       timeend, lastupdatedby_userid, lastupdatedts \n" +
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
            throw new IntegrationException("Cannot generate event list", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return ev;
    }
    
      /**
       * 
       * Generator method for EventCnF objects
     * Legacy note: [Zanda was trippin when he wrote this!]
     * ....And when he revised it for occbeta! 
     *
     * @param rs containing all fields in the event table
     * @param premadeEvent used by event creatino pathways that involve instantiation 
     * at other locations -- somewhat hacky and consider unifying
     * @return
     * @throws SQLException
     * @throws IntegrationException
     */
    private EventCnF generateEventFromRS(ResultSet rs) throws SQLException, IntegrationException {
         
        PersonCoordinator pc = getPersonCoordinator();
        UserCoordinator uc = getUserCoordinator();
        
        EventCnF ev = new EventCnF();

        ev.setEventID(rs.getInt("eventid"));
        ev.setCategory(getEventCategory(rs.getInt("category_catid")));
        ev.setDescription(rs.getString("eventDescription"));

        // these values will be used by the configure method to set the domain
        ev.setCeCaseID(rs.getInt("cecase_caseid"));
        ev.setOccPeriodID(rs.getInt("occperiod_periodid"));

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
        
        ev.setUserCreator(uc.user_getUser(rs.getInt("creator_userid")));
        if(rs.getTimestamp("creationts") != null){
            ev.setCreationts(rs.getTimestamp("creationts").toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        
        ev.setLastUpdatedBy(uc.user_getUser(rs.getInt("lastupdatedby_userid")));
        if(rs.getTimestamp("lastupdatedts") != null){
            ev.setCreationts(rs.getTimestamp("lastupdatedts").toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        
        ev.setActive(rs.getBoolean("active"));
        ev.setNotes(rs.getString("notes"));
        
        return ev;
    }
    

    /**
     * Extracts all records of event views by userid
     * @param userID
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> getEventHistory(int userID) throws IntegrationException {
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Integer> al = new ArrayList<>();

        try {
            String s = "SELECT event_eventid, entrytimestamp FROM loginobjecthistory "
                    + "WHERE login_userid = ? "
                    + "AND event_eventid IS NOT NULL "
                    + "ORDER BY entrytimestamp DESC;";
            stmt = con.prepareStatement(s);
            stmt.setInt(1, userID);

            rs = stmt.executeQuery();
            while (rs.next()) {
                al.add(rs.getInt("event_eventid"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("PersonIntegrator.getPerson | Unable to retrieve person", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        System.out.println("PersonIntegrator Retrieved history of size: " + al.size());
        return al;

    }

   
    
    
    /**
     * Builds a List of EventCnF objects given an ERG, which in June 2020 were
     * only CECase and OccPeriod objects
     * @param evHolder
     * @return
     * @throws IntegrationException 
     */
     public List<Integer> getEventList(IFace_EventHolder evHolder) throws IntegrationException{
        
     StringBuilder queryStub = new StringBuilder("SELECT eventid FROM public.event WHERE ");
            
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        List<Integer> evidl = new ArrayList<>();
        
        if(evHolder == null){
            return evidl;
        }
        
        if(evHolder instanceof OccPeriod){
            queryStub.append("occperiod_periodid=?;");
        } else if(evHolder instanceof CECase){
            queryStub.append("cecase_caseid=?;");
        }

        try {
        
            stmt = con.prepareStatement(queryStub.toString());
            
            if(evHolder instanceof OccPeriod){
                OccPeriod op = (OccPeriod) evHolder;
                stmt.setInt(1, op.getPeriodID());
            } else if(evHolder instanceof CECase){
                CECase cec = (CECase) evHolder;
                stmt.setInt(1, cec.getCaseID());
            }
            
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                evidl.add(rs.getInt("eventid"));
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot generate list of events", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return evidl;
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
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public int insertEvent(EventCnF event) throws IntegrationException, BObStatusException {
        if(event == null) return 0;
        PersonIntegrator pi = getPersonIntegrator();
        EventCoordinator ec = getEventCoordinator();
        int insertedEventID = 0;

        String query = "INSERT INTO public.event(\n" +
                        "            eventid, category_catid, cecase_caseid, creationts, eventdescription, \n" +
                        "            creator_userid, active, notes, occperiod_periodid, timestart, \n" +
                        "            timeend, lastupdatedby_userid, lastupdatedts)\n" +
                        "    VALUES (DEFAULT, ?, ?, now(), ?, \n" +
                        "            ?, ?, ?, ?, ?, \n" +
                        "            ?, ?, now());"; 
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = con.prepareStatement(query);
            
            stmt.setInt(1, event.getCategory().getCategoryID());
            
            if(event.getDomain() == EventDomainEnum.CODE_ENFORCEMENT && event.getCeCaseID() != 0){
                stmt.setInt(2, event.getCeCaseID());
            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }

            // note that the timestamp is set by a call to postgres's now()
   
            stmt.setString(3, event.getDescription());
            
            if(event.getUserCreator() != null){
                stmt.setInt(4, event.getUserCreator().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            stmt.setBoolean(5, event.isActive());
            stmt.setString(6, event.getNotes());
            
            if(event.getDomain() == EventDomainEnum.OCCUPANCY && event.getOccPeriodID() != 0){
                stmt.setInt(7, event.getOccPeriodID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            
            if (event.getTimeStart() != null) {
                stmt.setTimestamp(8, java.sql.Timestamp.valueOf(event.getTimeStart()));
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
            
            
            if (event.getTimeEnd() != null) {
                stmt.setTimestamp(9, java.sql.Timestamp.valueOf(event.getTimeEnd()));
            } else {
                stmt.setNull(9, java.sql.Types.NULL);
            }
            
            if(event.getLastUpdatedBy() != null){
                stmt.setInt(10, event.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(10, java.sql.Types.NULL);
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
        event = ec.getEvent(insertedEventID);


        if (event.getEventID() != 0 && event.getPersonList() != null && !event.getPersonList().isEmpty()) {
            pi.eventPersonConnect(event);
        }

        
        return insertedEventID;

    } // close method
    


    /**
     * Updates a record in the event table
     * @param event
     * @throws IntegrationException 
     */
    public void updateEvent(EventCnF event) throws IntegrationException {
        if(event == null) return;
        PersonIntegrator pi = getPersonIntegrator();
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE public.event\n");
        sb.append("   SET cecase_caseid=?, eventdescription=?, \n");
        sb.append("       creator_userid=?, active=?, occperiod_periodid=?, timestart=?, \n" );
        sb.append("       timeend=?, lastupdatedby_userid=?, lastupdatedts=now() \n" );
        sb.append(" WHERE eventid=?;");

        // TO DO: finish clearing view confirmation
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(sb.toString());
            
            
            if(event.getDomain() == EventDomainEnum.CODE_ENFORCEMENT && event.getCeCaseID() != 0){
                stmt.setInt(1, event.getCeCaseID());
            } else {
                stmt.setNull(1, java.sql.Types.NULL);
            }

            // note that the timestamp is set by a call to postgres's now()
   
            stmt.setString(2, event.getDescription());
            
            if(event.getUserCreator() != null){
                stmt.setInt(3, event.getUserCreator().getUserID());
            } else {
                stmt.setNull(3, java.sql.Types.NULL);
            }
            
            stmt.setBoolean(4, event.isActive());
            
            if(event.getDomain() == EventDomainEnum.OCCUPANCY && event.getOccPeriodID() != 0){
                stmt.setInt(5, event.getOccPeriodID());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            
            if (event.getTimeStart() != null) {
                stmt.setTimestamp(6, java.sql.Timestamp.valueOf(event.getTimeStart()));
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            
            
            if (event.getTimeEnd() != null) {
                stmt.setTimestamp(7, java.sql.Timestamp.valueOf(event.getTimeEnd()));
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            
            if(event.getLastUpdatedBy() != null){
                stmt.setInt(8, event.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL);
            }
            
            stmt.setInt(9, event.getEventID());
            
            stmt.executeUpdate();
            

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive event", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
 
    

    /**
     * Updates only the notes field and lastupdatedby field
     * @param event
     * @throws IntegrationException 
     */
    public void updateEventNotes(EventCnF event) throws IntegrationException {
        if(event == null) return;
        PersonIntegrator pi = getPersonIntegrator();
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE public.event\n");
        sb.append("   SET notes=?, lastupdatedby_userid=?, lastupdatedts=now() \n" );
        sb.append(" WHERE eventid=?;");

        // TO DO: finish clearing view confirmation
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(sb.toString());
           

            stmt.setString(1, event.getNotes());
            
        
            
            if(event.getLastUpdatedBy() != null){
                stmt.setInt(2, event.getLastUpdatedBy().getUserID());
            } else {
                stmt.setNull(2, java.sql.Types.NULL);
            }
            
            stmt.setInt(3, event.getEventID());
            
            stmt.executeUpdate();
            

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive event", ex);

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
        
        SearchCoordinator sc = getSearchCoordinator();
        List<Integer> evidlst = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        Connection con = getPostgresCon();

        // we need an EventDomain for the BOBID, too, so set it arbitrarily if it's null
        if(params.getEventDomain_val() == null){
            params.setEventDomain_val(EventDomainEnum.UNIVERSAL);
            params.appendToParamLog("DOMAIN CONTROL: no object specified - Code Enforcement chosen as default; | ");
        }
        
        params.appendSQL("SELECT DISTINCT eventid \n");
        params.appendSQL("FROM public.event INNER JOIN public.eventcategory ON (category_catid = categoryid) \n");
        params.appendSQL("LEFT OUTER JOIN public.eventperson ON (ceevent_eventid = event.eventid) \n");
        // to get to property and hence municipality, we must traverse different key pathways
        // through the database for CE versus Occ. This is all backflippy crazy shit because
        // of the decision decision to maintain only one event tablef or both Occ events and CE events.
        if(params.getEventDomain_val().equals(EventDomainEnum.CODE_ENFORCEMENT)){
            params.appendSQL("LEFT OUTER JOIN public.cecase ON (cecase.caseid = event.cecase_caseid) \n");
            params.appendSQL("LEFT OUTER JOIN public.property ON (cecase.property_propertyid = property_propertyid)  \n");
            
        } else {
            // with only two enum values now, we either have Code enf or occ
            params.appendSQL("LEFT OUTER JOIN public.occperiod ON (occperiod.periodid = event.occperiod_periodid) \n");
            params.appendSQL("LEFT OUTER JOIN public.propertyunit ON (propertyunit.unitid = occperiod.propertyunit_unitid) \n");
            params.appendSQL("LEFT OUTER JOIN public.property ON (property.propertyid = propertyunit.property_propertyid) \n");
        }
        params.appendSQL("WHERE eventid IS NOT NULL \n");
        
        // as long as this isn't an ID only search, do the normal SQL building process
        if (!params.isBobID_ctl()) {
            
            //*******************************
           // **   MUNI,DATES,USER,ACTIVE  **
           // *******************************
            params = (SearchParamsEvent) sc.assembleBObSearchSQL_muniDatesUserActive(
                                                                        params, 
                                                                        SearchParamsEvent.MUNI_DBFIELD,
                                                                        ACTIVE_FIELD);

            //*******************************
           // **      1.EVENT CATEGORY     **
           // *******************************
            if (params.isEventCat_ctl() ) {
                if(params.getEventCat_val() != null){
                    params.appendSQL("AND eventcategory.categoryid=? ");
                } else {
                    params.setEventCat_ctl(false);
                    params.appendToParamLog("EVENT CATEGORY: no object specified; event cat filter disabled; |"); 
                }
            }

           // *******************************
           // **      2.EVENT TYPE         **
           // *******************************
            if (params.isEventType_ctl() ) {
                if(params.getEventType_val()!= null){
                    params.appendSQL("AND public.eventcategory.categorytype = CAST(? AS eventType ");
                } else {
                    params.setEventType_ctl(false);
                    params.appendToParamLog("EVENT TYPE: no object specified; event type filter disabled; | ");
                }
            }
            
            
           // *******************************
           // **     3.EVENT DOMAIN        **
           // *******************************
            if(params.isEventDomain_ctl()){
                params.appendSQL("AND ");
                params.appendSQL(params.getEventDomain_val().getDbField()); //Code enf or Occ
                params.appendSQL(" ");
                params.appendSQL("IS NOT NULL ");
            }
            
           // *******************************
           // **   4.EVENT DOMAIN BOB ID   **
           // *******************************
            if(params.isEventDomainPK_ctl()){
                params.appendSQL("AND ");
                params.appendSQL(params.getEventDomain_val().getDbField());
                params.appendSQL("=? ");
            }
            
           // *******************************
           // **   5.EVENT PERSONS         **
           // *******************************
            if (params.isPerson_ctl()) {
                if(params.getPerson_val() != null){
                    params.appendSQL("AND eventperson.person_personid=? ");
                } else {
                    params.setPerson_ctl(false);
                    params.appendToParamLog("EVENT PERSONS: No Person object specified; person filter disabled; | ");
                }
            }

           // *******************************
           // **    6.DISCLOSE TO MUNI     **
           // *******************************
            if (params.isDiscloseToMuni_ctl()) {
                params.appendSQL("AND disclosetomunicipality=");
                if (params.isDiscloseToMuni_val()) {
                    params.appendSQL("TRUE ");
                } else {
                    params.appendSQL("FALSE ");
                }
            }


           // *******************************
           // **    7.DISCLOSE TO PUBLIC   **
           // *******************************
            if (params.isDiscloseToPublic_ctl()) {
                params.appendSQL("AND disclosetopublic=");
                if (params.isDiscloseToPublic_val()) {
                    params.appendSQL("TRUE ");
                } else {
                    params.appendSQL("FALSE ");
                }
            }
            
           // *******************************
           // **   8.PROPERTY              **
           // *******************************
            if (params.isProperty_ctl()) {
                if(params.getProperty_val()!= null){
                    params.appendSQL("AND property.propertyid=? ");
                } else {
                    params.setProperty_ctl(false);
                    params.appendToParamLog("PROPERTY: No PROPERTY object specified; filter disabled; | ");
                }
            }
            
           // *******************************
           // **   9.PROP USE TYPE         **
           // *******************************
            if (params.isPropertyUseType_ctl()) {
                if(params.getPropertyUseType_val()!= null){
                    params.appendSQL("AND property.propertyid=? ");
                } else {
                    params.setPropertyUseType_ctl(false);
                    params.appendToParamLog("PROPERTY USE TYPE: No PROPERTY object specified; filter disabled; | ");
                }
            }
            
             // *******************************
           // **   10. LAND BANK HELD        **
           // *******************************
            if (params.isDiscloseToPublic_ctl()) {
                params.appendSQL("AND property.landbankheld=");
                if (params.isDiscloseToPublic_val()) {
                    params.appendSQL("TRUE ");
                } else {
                    params.appendSQL("FALSE ");
                }
            }
            
        } else {
            params.appendSQL("AND eventid=? "); // will be param 1 with ID search
        }
        int paramCounter = 0;
        params.appendSQL(";");
            
        try {
            stmt = con.prepareStatement(params.extractRawSQL());

            if (!params.isBobID_ctl()) {
                
                if (params.isMuni_ctl()) {
                     stmt.setInt(++paramCounter, params.getMuni_val().getMuniCode());
                }
                
                if(params.isDate_startEnd_ctl()){
                    stmt.setTimestamp(++paramCounter, params.getDateStart_val_sql());
                    stmt.setTimestamp(++paramCounter, params.getDateEnd_val_sql());
                 }
                
                if (params.isUser_ctl()) {
                   stmt.setInt(++paramCounter, params.getUser_val().getUserID());
                }

                if (params.isEventCat_ctl()) {
                    stmt.setInt(++paramCounter, params.getEventCat_val().getCategoryID());
                }
                
                if (params.isEventType_ctl()) {
                    stmt.setString(++paramCounter, params.getEventType_val().name());
                }

                if(params.isEventDomainPK_ctl()){
                    stmt.setInt(++paramCounter, params.getEventDomainPK_val());
                }
                
                if (params.isPerson_ctl()) {
                    stmt.setInt(++paramCounter, params.getPerson_val().getPersonID());
                }
                
                if (params.isProperty_ctl()) {
                    stmt.setInt(++paramCounter, params.getProperty_val().getPropertyID());
                }
                
                if (params.isPropertyUseType_ctl()) {
                    stmt.setInt(++paramCounter, params.getPropertyUseType_val().getTypeID());
                }
                
            } else {
                stmt.setInt(++paramCounter, params.getBobID_val());
            }

            rs = stmt.executeQuery();

            int counter = 0;
            int maxResults;
            if (params.isLimitResultCount_ctl()) {
                maxResults = params.getLimitResultCount_val();
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
        }
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

        String query =  " SELECT categoryid, categorytype, title, description, notifymonitors, \n" +
                        "       hidable, icon_iconid, relativeorderwithintype, relativeorderglobal, \n" +
                        "       hosteventdescriptionsuggtext, directive_directiveid, defaultdurationmins, \n" +
                        "       active, userrankminimumtoenact, userrankminimumtoview, userrankminimumtoupdate\n" +
                        "  FROM public.eventcategory WHERE categoryid=?;";
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
        WorkflowIntegrator choiceInt = getWorkflowIntegrator();
        
        ec.setCategoryID(rs.getInt("categoryid"));
        
        if(!(rs.getString("categoryType") == null) && !(rs.getString("categoryType").equals(""))){
            ec.setEventType(EventType.valueOf(rs.getString("categoryType")));
        }
        ec.setEventCategoryTitle(rs.getString("title"));
        ec.setEventCategoryDesc(rs.getString("description"));

        ec.setNotifymonitors(rs.getBoolean("notifymonitors"));
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
        ec.setActive(rs.getBoolean("active"));
        ec.setDefaultdurationmins(rs.getInt("defaultdurationmins"));
        
        ec.setUserRankMinimumToEnact(rs.getInt("userrankminimumtoenact"));
        ec.setUserRankMinimumToView(rs.getInt("userrankminimumtoview"));
        ec.setUserRankMinimumToUpdate(rs.getInt("userrankminimumtoupdate"));
        
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

    /**
     * Creates a new record in the eventcategory table
     * @param ec
     * @throws IntegrationException 
     */
    public void insertEventCategory(EventCategory ec) throws IntegrationException {

        String query = "INSERT INTO public.eventcategory(\n" +
                        "            categoryid, categorytype, title, description, notifymonitors, \n" +
                        "            hidable, icon_iconid, relativeorderwithintype, relativeorderglobal, \n" +
                        "            hosteventdescriptionsuggtext, directive_directiveid, defaultdurationmins, \n" +
                        "            active, userrankminimumtoenact, userrankminimumtoview, userrankminimumtoupdate)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, \n" +
                        "            ?, ?, ?, ?);";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, ec.getEventType().name());
            stmt.setString(2, ec.getEventCategoryTitle());
            stmt.setString(3, ec.getEventCategoryDesc());
            stmt.setBoolean(4, ec.isNotifymonitors());
            
            stmt.setBoolean(5, ec.isHidable());

            if(ec.getIcon() != null){
                stmt.setInt(6, ec.getIcon().getIconid());
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            stmt.setInt(7, ec.getRelativeOrderWithinType());
            stmt.setInt(8, ec.getRelativeOrderGlobal());
            
            stmt.setString(9, ec.getHostEventDescriptionSuggestedText());
            if(ec.getDirective() != null){
                    stmt.setInt(10, ec.getDirective().getDirectiveID());
            } else {
                stmt.setNull(10, java.sql.Types.NULL);
            }
            stmt.setInt(11, ec.getDefaultdurationmins());

            stmt.setBoolean(12, ec.isActive());
            stmt.setInt(13, ec.getUserRankMinimumToEnact());
            stmt.setInt(14, ec.getUserRankMinimumToView());
            stmt.setInt(15, ec.getUserRankMinimumToUpdate());

            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert event category", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }

    /**
     * Updates a singel record in the eventcategory table
     * @param ec
     * @throws IntegrationException 
     */
    public void updateEventCategory(EventCategory ec) throws IntegrationException {

        String query =  "UPDATE public.eventcategory\n" +
                        "   SET categorytype=?, title=?, description=?, notifymonitors=?, \n" +
                        "       hidable=?, icon_iconid=?, relativeorderwithintype=?, relativeorderglobal=?, \n" +
                        "       hosteventdescriptionsuggtext=?, directive_directiveid=?, defaultdurationmins=?, \n" +
                        "       active=?, userrankminimumtoenact=?, userrankminimumtoview=?, \n" +
                        "       userrankminimumtoupdate=? WHERE categoryid = ?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, ec.getEventType().name());
            stmt.setString(2, ec.getEventCategoryTitle());
            stmt.setString(3, ec.getEventCategoryDesc());
            stmt.setBoolean(4, ec.isNotifymonitors());
            
            stmt.setBoolean(5, ec.isHidable());

            if(ec.getIcon() != null){
                stmt.setInt(6, ec.getIcon().getIconid());
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            stmt.setInt(7, ec.getRelativeOrderWithinType());
            stmt.setInt(8, ec.getRelativeOrderGlobal());
            
            stmt.setString(9, ec.getHostEventDescriptionSuggestedText());
            if(ec.getDirective() != null){
                    stmt.setInt(10, ec.getDirective().getDirectiveID());
            } else {
                stmt.setNull(10, java.sql.Types.NULL);
            }
            stmt.setInt(11, ec.getDefaultdurationmins());

            stmt.setBoolean(12, ec.isActive());
            stmt.setInt(13, ec.getUserRankMinimumToEnact());
            stmt.setInt(14, ec.getUserRankMinimumToView());
            stmt.setInt(15, ec.getUserRankMinimumToUpdate());
            
            stmt.setInt(16, ec.getCategoryID());

            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to update event category", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }

    /**
     * Nukes a single record in the eventcategory table
     * @param ec
     * @throws IntegrationException 
     */
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
    
    
  
       
} // close class