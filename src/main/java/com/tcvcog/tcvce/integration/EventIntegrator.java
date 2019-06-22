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
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.SessionSystemCoordinator;
import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.MalformedBOBException;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Event;
import com.tcvcog.tcvce.entities.EventCECase;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.EventCECaseCasePropBundle;
import com.tcvcog.tcvce.entities.EventProposal;
import com.tcvcog.tcvce.entities.EventProposalImplementation;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.search.QueryEventCECase;
import com.tcvcog.tcvce.entities.search.SearchParamsCECases;
import com.tcvcog.tcvce.entities.search.SearchParamsEventCECase;
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

    public EventCategory getEventCategory(int catID) throws IntegrationException {

        String query = "SELECT categoryid, categorytype, title, description, userdeployable, \n" +
                        "       munideployable, publicdeployable, notifycasemonitors, hidable, \n" +
                        "       icon_iconid, requestable, phasechangerule_ruleid"
                + "  FROM public.eventcategory WHERE categoryID = ?";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        EventCategory ec = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, catID);
            //System.out.println("EventInteegrator.getEventCategory| sql: " + stmt.toString());
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
        CaseIntegrator ci = getCaseIntegrator();
        
        ec.setCategoryID(rs.getInt("categoryid"));
        ec.setEventType(EventType.valueOf(rs.getString("categoryType")));
        ec.setEventCategoryTitle(rs.getString("title"));
        ec.setEventCategoryDesc(rs.getString("description"));

        ec.setUserdeployable(rs.getBoolean("userdeployable"));
        ec.setMunideployable(rs.getBoolean("munideployable"));
        ec.setPublicdeployable(rs.getBoolean("publicdeployable"));
        ec.setNotifycasemonitors(rs.getBoolean("notifycasemonitors"));
        ec.setHidable(rs.getBoolean("hidable"));
        ec.setRequestable(rs.getBoolean("requestable"));
        
        if(rs.getInt("icon_iconid") != 0){
            ec.setIcon(si.getIcon(rs.getInt("icon_iconid")));
        }
        if(rs.getInt("phasechangerule_ruleid") != 0){
            ec.setCasePhaseChangeRule(ci.getCaseChangeRule(rs.getInt("phasechangerule_ruleid")));
        }
        return ec;
    }

    public ArrayList<EventCategory> getEventCategoryList() throws IntegrationException {
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
    
    public List<EventCategory> getRequestableEventCategories() throws IntegrationException {
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
    
    public ArrayList<EventCategory> getEventCategoryList(EventType et) throws IntegrationException {
        String query = "SELECT categoryid FROM public.eventcategory WHERE categorytype = cast (? as ceeventtype);";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        ArrayList<EventCategory> categoryList = new ArrayList();

        try {

            stmt = con.prepareStatement(query);
            stmt.setString(1, et.toString());
            rs = stmt.executeQuery();
            System.out.println("EventIntegrator.getEventCategoryList | SQL: " + stmt.toString());
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

        String query = "INSERT INTO public.eventcategory(\n"
                + "categoryid, "
                + "categorytype, title, description, "
                + "userdeployable, munideployable, publicdeployable, "
                + "notifycasemonitors, phasechangerule_ruleid"
                + "hidable, icon_iconid, requestable )\n"
                + "    VALUES (DEFAULT, CAST (? as ceeventtype), ?, ?, ?, \n"
                + "            ?, ?, ?, ?, \n"
                + "            ?, ?, ?);";

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
            stmt.setInt(8, ec.getCasePhaseChangeRule().getRuleID());

            stmt.setBoolean(9, ec.isHidable());
            stmt.setInt(10, ec.getIcon().getIconid());
            stmt.setBoolean(11, ec.isRequestable());

            System.out.println("EventInteegrator.insertEventCategory| sql: " + stmt.toString());
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

        String query = "UPDATE public.eventcategory\n"
                + "   SET categorytype=CAST (? as ceeventtype), title=?, description=?, userdeployable=?, \n"
                + "       munideployable=?, publicdeployable=?, \n"
                + "       notifycasemonitors=?, phasechangerule_ruleid=?, hidable=?, icon_iconid=?, requestable=? \n"
                + " WHERE categoryid = ?;";

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
            stmt.setInt(8, ec.getCasePhaseChangeRule().getRuleID());

            stmt.setBoolean(9, ec.isHidable());
            if(ec.getIcon() != null){
                stmt.setInt(10, ec.getIcon().getIconid());
            } else {
                stmt.setNull(10, java.sql.Types.NULL);
            }
            stmt.setBoolean(11, ec.isRequestable());
            stmt.setInt(12, ec.getCategoryID());

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
     * @param event a fully-baked event ready for insertion. An EventCECase
     * contains an integer of the caseID to which the event should be attached
     * @return the id of the event just inserted
     * @throws IntegrationException when the system is unable to store event in
     * DB
     */
    public int insertEvent(EventCECase event) throws IntegrationException {
        PersonIntegrator pi = getPersonIntegrator();
        int insertedEventID = 0;

        String query = "INSERT INTO public.ceevent(\n"
                + "            eventid, ceeventcategory_catid, cecase_caseid, dateofrecord, \n"
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
            System.out.println("EventIntegrator.inactivateEvent | inactivating event ID: " + eventIdToInactivate);

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive event", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
           
        } // close finally

    }

    public void updateEvent(EventCECase event) throws IntegrationException {
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE public.ceevent ");
        sb.append("   SET ceeventcategory_catid=?, cecase_caseid=?, dateofrecord=?, ");
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
            System.out.println("EventIntegrator.updateEvent: executed event updated on ID " + event.getEventID());

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive event", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }

    public void deleteEvent(EventCECase event) throws IntegrationException {
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

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
   
    /**
     * Legacy note: [Zanda was trippin when he wrote this!]
     *
     * @param rs
     * @param premadeEvent used by event creatino pathways that involve instantiation 
     * at other locations -- somewhat hacky and consider unifying
     * @return
     * @throws SQLException
     * @throws IntegrationException
     */
    private EventCECase generateEventFromRS(ResultSet rs) throws SQLException, IntegrationException {
        EventCECase ev = new EventCECase();
        UserIntegrator ui = getUserIntegrator();
        SessionSystemCoordinator ssc = getSessionSystemCoordinator();
        
        ev.setPropertyID(rs.getInt("property_propertyid"));
        ev.setMuniCode(rs.getInt("municipality_municode"));
        ev.setMuniName(ssc.getMuniCodeNameMap().get(rs.getInt("municipality_municode")));

        ev.setEventID(rs.getInt("eventid"));
        ev.setCategory(getEventCategory(rs.getInt("ceeventCategory_catID")));
        ev.setCaseID(rs.getInt("cecase_caseid"));
        
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
    
    
    
    public EventCECaseCasePropBundle getEventCasePropBundle(int eventid) throws IntegrationException, CaseLifecyleException{
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
     * Builds an EventProposal object from the eventproposal table in the DB.
     * An EventProposal contains up to three EventCategory objects which the user 
     * can select to create next in their case management workflow
     * @param proposalID
     * @return all fields populated
     * @throws IntegrationException 
     */
    public EventProposal getEventProposal(int proposalID) throws IntegrationException{

        EventProposal proposal = new EventProposal();
        
        StringBuilder sb = new StringBuilder();
        sb.append(      "SELECT proposalid, title, overalldescription, creator_userid, choice1eventcat_catid, \n" +
                        "       choice1description, choice2eventcat_catid, choice2description, \n" +
                        "       choice3eventcat_catid, choice3description, directproposaltodefaultmuniceo, \n" +
                        "       directproposaltodefaultmunistaffer, directproposaltodeveloper, \n" +
                        "       activatesxdaysfromgenevent, expiresxdaysfromgenevent, expirytrigger_eventcatid, \n" +
                        "       active\n" +
                        "  FROM public.ceeventproposal WHERE proposalid = ?;");
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, proposalID);
            rs = stmt.executeQuery();

            while (rs.next()) {
                proposal = generateEventProposal(rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive EventProposal", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return proposal;
        
        
    }
    
    /**
     * Site of EventPropsal instantiation.
     * Populates fields of EventProposals given a ResultSet returned from a DB query
     * 
     * @param rs
     * @return
     * @throws SQLException
     * @throws IntegrationException 
     */
    private EventProposal generateEventProposal(ResultSet rs) throws SQLException, IntegrationException{
        EventProposal proposal = new EventProposal();
        
        proposal.setProposalID(rs.getInt("proposalid"));
        proposal.setTitle(rs.getString("title"));
        proposal.setDescription(rs.getString("overalldescription"));
        
        proposal.setChoice1EventCat(getEventCategory(rs.getInt("choice1eventcat_catid")));
        proposal.setChoice1Description(rs.getString("choice1description"));
        proposal.setChoice2EventCat(getEventCategory(rs.getInt("choice2eventcat_catid")));
        proposal.setChoice1Description(rs.getString("choice2description"));
        proposal.setChoice3EventCat(getEventCategory(rs.getInt("choice3eventcat_catid")));
        proposal.setChoice1Description(rs.getString("choice3description"));
        
        proposal.setDirectPropToDefaultMuniCEO(rs.getBoolean("directproposaltodefaultmuniceo"));
        proposal.setDirectPropToDefaultMuniStaffer(rs.getBoolean("directproposaltodefaultmunistaffer"));
        proposal.setDirectPropToDeveloper(rs.getBoolean("directproposaltodeveloper"));
        
        proposal.setActivatesXDaysFromGeneratingEvent(rs.getInt("activatesxdaysfromgenevent"));
        proposal.setExpiresXDaysFromGeneratingEvent(rs.getInt("expiresxdaysfromgenevent"));
        proposal.setExpiryTrigger(getEventCategory(rs.getInt("expirytrigger_eventcatid")));
        
        proposal.setActive(rs.getBoolean("active"));
        
        return proposal;
    }
    
    public void insertEventProposal(EventProposal prop) throws IntegrationException{
         String query = "INSERT INTO public.ceeventproposal(\n" +
                        "            proposalid, title, overalldescription, creator_userid, choice1eventcat_catid, \n" +
                        "            choice1description, choice2eventcat_catid, choice2description, \n" +
                        "            choice3eventcat_catid, choice3description, directproposaltodefaultmuniceo, \n" +
                        "            directproposaltodefaultmunistaffer, directproposaltodeveloper, \n" +
                        "            activatesxdaysfromgenevent, expiresxdaysfromgenevent, expirytrigger_eventcatid, \n" +
                        "            active)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, \n" +
                        "            ?, ?, ?, \n" +
                        "            ?, ?, \n" +
                        "            ?, ?, ?, \n" +
                        "            ?);";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, prop.getTitle());
            stmt.setString(2, prop.getDescription());
            stmt.setInt(3, prop.getCreator().getUserID());
            stmt.setInt(4, prop.getChoice1EventCat().getCategoryID());
            
            stmt.setString(5, prop.getChoice1Description());
            stmt.setInt(6, prop.getChoice2EventCat().getCategoryID());
            stmt.setString(7, prop.getChoice2Description());
            
            stmt.setInt(8, prop.getChoice3EventCat().getCategoryID());
            stmt.setString(9, prop.getChoice3Description());
            stmt.setBoolean(10, prop.isDirectPropToDefaultMuniCEO());
            
            stmt.setBoolean(11, prop.isDirectPropToDefaultMuniStaffer());
            stmt.setBoolean(12, prop.isDirectPropToDeveloper());
            
            stmt.setInt(13, prop.getActivatesXDaysFromGeneratingEvent());
            stmt.setInt(14, prop.getExpiresXDaysFromGeneratingEvent());
            stmt.setInt(15, prop.getExpiryTrigger().getCategoryID());
            
            stmt.setBoolean(16, prop.isActive());

            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert EventProposal", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    public void updateEventProposal(EventProposal prop) throws IntegrationException{
         String query = "   UPDATE public.ceeventproposal\n" +
                        "   SET title=?, overalldescription=?, creator_userid=?, \n" +
                        "       choice1eventcat_catid=?, choice1description=?, choice2eventcat_catid=?, \n" +
                        "       choice2description=?, choice3eventcat_catid=?, choice3description=?, \n" +
                        "       directproposaltodefaultmuniceo=?, directproposaltodefaultmunistaffer=?, \n" +
                        "       directproposaltodeveloper=?, activatesxdaysfromgenevent=?, expiresxdaysfromgenevent=?, \n" +
                        "       expirytrigger_eventcatid=?, active=?\n" +
                        " WHERE proposalid = ?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, prop.getTitle());
            stmt.setString(2, prop.getDescription());
            stmt.setInt(3, prop.getCreator().getUserID());
            stmt.setInt(4, prop.getChoice1EventCat().getCategoryID());
            
            stmt.setString(5, prop.getChoice1Description());
            stmt.setInt(6, prop.getChoice2EventCat().getCategoryID());
            stmt.setString(7, prop.getChoice2Description());
            
            stmt.setInt(8, prop.getChoice3EventCat().getCategoryID());
            stmt.setString(9, prop.getChoice3Description());
            stmt.setBoolean(10, prop.isDirectPropToDefaultMuniCEO());
            
            stmt.setBoolean(11, prop.isDirectPropToDefaultMuniStaffer());
            stmt.setBoolean(12, prop.isDirectPropToDeveloper());
            
            stmt.setInt(13, prop.getActivatesXDaysFromGeneratingEvent());
            stmt.setInt(14, prop.getExpiresXDaysFromGeneratingEvent());
            stmt.setInt(15, prop.getExpiryTrigger().getCategoryID());
            
            stmt.setBoolean(16, prop.isActive());
            stmt.setInt(17, prop.getProposalID());

            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert EventProposal", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
     /**
     * 
     * @param event for which we will search for a creation trigger 
     * @return the Event that lists the incoming event as its response
     * @throws IntegrationException 
     */
    public EventProposalImplementation getProposalImplAssociatedWithEvent(Event event) throws IntegrationException{
        EventProposalImplementation propImp = null;

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT implementationid "
                + "FROM ceeventproposalimplementation "
                + "INNER JOIN ceevent ON generatingevent_eventid = eventid "
                + "WHERE eventid = ?");
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, event.getEventID());
            rs = stmt.executeQuery();

            while (rs.next()) {
                propImp = getEventProposalImplementation(rs.getInt("implementationid"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive event", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return propImp;
    }
    
    public void insertEventProposalImplementation(EventProposalImplementation imp) throws MalformedBOBException, IntegrationException{
        String query = "INSERT INTO public.ceeventproposalimplementation(\n" +
                        "            implementationid, proposal_propid, generatingevent_eventid, initiator_userid, \n" +
                        "            responderintended_userid, activateson, expireson, responderactual_userid, \n" +
                        "            rejectproposal, responsetimestamp, responseevent_eventid, expiredorinactive, \n" +
                        "            notes)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, \n" +
                        "            ?);";

        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, imp.getProposalID());
            stmt.setInt(2, imp.getGeneratingEventID());
            
            if(imp.getInitiator() != null){
                stmt.setInt(3, imp.getInitiator().getUserID());
            } else { 
                throw new MalformedBOBException("EventProposalImplementations must contain a User object as an initiator");
            }
            
            if(imp.getResponderIntended()!= null){
                stmt.setInt(4, imp.getResponderIntended().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(imp.getActivatesOn()));
            stmt.setTimestamp(6, java.sql.Timestamp.valueOf(imp.getExpiresOn()));
            
            if(imp.getResponderActual() != null){
                stmt.setInt(7, imp.getResponderActual().getUserID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            
            stmt.setBoolean(8, imp.isProposalRejected());
            
            if(imp.getResponseTimestamp() != null){
                stmt.setTimestamp(9, java.sql.Timestamp.valueOf(imp.getResponseTimestamp()));
            } else {
                stmt.setNull(9, java.sql.Types.NULL);
            }

            stmt.setInt(10, imp.getResponseEventID());
            stmt.setBoolean(11, imp.isExpiredorinactive());
            stmt.setString(12, imp.getNotes());
            
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert EventProposal", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    /**
     * Builds an EventProposalImplementation object given the PK of the DB record
     * @param propImpID
     * @return
     * @throws IntegrationException 
     */
    public EventProposalImplementation getEventProposalImplementation(int propImpID) throws IntegrationException{
         EventProposalImplementation response = new EventProposalImplementation();
        
        StringBuilder sb = new StringBuilder();
        sb.append(  "SELECT implementationid, proposal_propid, generatingevent_eventid, initiator_userid, \n" +
                    "       responderintended_userid, activateson, expireson, responderactual_userid, \n" +
                    "       rejectproposal, responsetimestamp, responseevent_eventid, expiredorinactive, \n" +
                    "       notes\n" +
                    "  FROM public.ceeventproposalimplementation WHERE implementationid = ?;");
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, propImpID);
            rs = stmt.executeQuery();

            while (rs.next()) {
                response = generateEventProposalImplementation(rs);
                
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive event proposal response", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return response;
        
    }
    
    private EventProposalImplementation generateEventProposalImplementation(ResultSet rs) throws SQLException, IntegrationException{
        EventProposalImplementation propImp = new EventProposalImplementation();
        UserIntegrator ui = getUserIntegrator();
        
        propImp.setImplementationID(rs.getInt("implementationid"));
        propImp.setProposalID(rs.getInt("proposal_propid"));
        
        propImp.setInitiator(ui.getUser(rs.getInt("initiator")));
        propImp.setResponderIntended(ui.getUser(rs.getInt("responderintended_userid")));
        propImp.setResponderActual(ui.getUser(rs.getInt("responder_userid")));
        
        if(rs.getTimestamp("activateson") != null){
            propImp.setActivatesOn(rs.getTimestamp("activateson").toLocalDateTime());
        }
        if(rs.getTimestamp("expireson") != null){
            propImp.setExpiresOn(rs.getTimestamp("expireson").toLocalDateTime());
        }
        propImp.setResponseEventID(rs.getInt("responseevent_eventid"));
        if(rs.getTimestamp("responsetimestamp") != null){
            propImp.setResponseTimestamp(rs.getTimestamp("responsetimestamp").toLocalDateTime());
        }
        propImp.setProposalRejected(rs.getBoolean("rejectproposal"));
        
        propImp.setExpiredorinactive(rs.getBoolean("expiredorinactive"));
        propImp.setNotes(rs.getString("notes"));
        
        return propImp;
        
    }
    
    public void updateProposalImplementationMetadata(EventProposalImplementation imp) throws IntegrationException{
          String query =    "UPDATE public.ceeventproposalimplementation\n" +
                            "   SET proposal_propid=?, generatingevent_eventid=?, \n" +
                            "       initiator_userid=?, responderintended_userid=?, activateson=?, \n" +
                            "       expireson=?, expiredorinactive=?, notes=?\n" +
                            " WHERE implementationid=?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, imp.getProposalID());
            stmt.setInt(2, imp.getGeneratingEventID());
            stmt.setInt(3, imp.getInitiator().getUserID());
            stmt.setInt(4, imp.getResponderIntended().getUserID());
            
            if(imp.getActivatesOn() != null){
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(imp.getActivatesOn()));
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            
            if(imp.getExpiresOn() != null){
                stmt.setTimestamp(6, java.sql.Timestamp.valueOf(imp.getExpiresOn()));
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            
            stmt.setString(7, imp.getNotes());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot udpate event proposal implementation, sorry", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
        
        
    }
    
    public void logResponseToProposal(EventCECase ev) throws IntegrationException {

       String query = "UPDATE public.ceeventproposalimplementation\n" +
                        "   SET responderactual_userid=?, rejectproposal=?, responsetimestamp=?, \n" +
                        "       responseevent_eventid=?, notes=?\n" +
                        " WHERE implementationid = ?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        EventProposalImplementation imp;
        
        if(ev.getEventProposalImplementation()!= null){
            imp = ev.getEventProposalImplementation();

            try {

                stmt = con.prepareStatement(query);
                stmt.setInt(1, imp.getResponderActual().getUserID());
                stmt.setBoolean(2, imp.isProposalRejected());
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(imp.getResponseTimestamp()));
                int responseEventID;
                if(imp.getResponseEvent()!= null){
                    responseEventID = imp.getResponseEvent().getEventID();
                    stmt.setInt(4, responseEventID);
                } else {
                    stmt.setNull(4, java.sql.Types.NULL);
                }
                stmt.setString(5, imp.getNotes());
                stmt.executeUpdate();
            } catch (SQLException ex) {
                System.out.println(ex.toString());
                throw new IntegrationException("Cannot udpate event proposal implementation, sorry", ex);

            } finally {
                if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
                if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            }
        }
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
    public List<EventCECase> queryCaseDependentEvents(SearchParamsEventCECase params) throws IntegrationException, CaseLifecyleException{
        List<EventCECaseCasePropBundle> wrappedEventList = getEventsCECase(params);
        List<EventCECase> depList = new ArrayList<>();
        for(EventCECaseCasePropBundle ec: wrappedEventList){
            depList.add(ec.getEvent());
        }
        return depList;
    }

  
    public QueryEventCECase runQueryEventCECase(QueryEventCECase q) 
            throws IntegrationException, CaseLifecyleException{
        List<SearchParamsEventCECase> pList = q.getParmsList();
        
        for(SearchParamsEventCECase sp: pList){
            q.addToResults(getEventsCECase(sp));
        }
        q.setExecutionTimestamp(LocalDateTime.now());
        q.setExecutedByIntegrator(true);
        return q;
    }
    
    public List<EventCECaseCasePropBundle> getEventsCECase(SearchParamsEventCECase params) 
            throws IntegrationException, CaseLifecyleException {
        List<EventCECaseCasePropBundle> eventList = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        Connection con = getPostgresCon();
        boolean notFirstCriteria = false;

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ceevent.eventid ");
        sb.append("FROM ceevent INNER JOIN eventcategory ON (ceeventcategory_catid = categoryid) ");
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
                if(params.isUseDateOfRecord()){
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

            if (params.isFilterByEventType() 
                    && 
                !params.isFilterByrequestsAction()
                    &&
                !params.isFilterByRequestedResponseEventCat()) {
                if(notFirstCriteria){sb.append("AND ");} else {notFirstCriteria = true;}
                sb.append("categorytype = CAST (? AS ceeventtype) ");
            }

            if (params.isFilterByEventCategory() 
                    && 
                !params.isFilterByrequestsAction()
                    &&
                !params.isFilterByRequestedResponseEventCat()) {
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


            if (params.isFilterByActive()) {
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

            if (params.isFilterByrequestsAction()) {
                if(notFirstCriteria){sb.append("AND ");} else {notFirstCriteria = true;}
                if (params.isRequestsAction()) {
                    sb.append("requestedeventcat_catid IS NOT NULL ");
                } else {
                    sb.append("requestedeventcat_catid IS NULL ");
                }
            }

            //      ALL of these criteria are only evaluated if filtering by case action request
            if(params.isFilterByrequestsAction()){
                if (params.isFilterByHasResponseEvent()) {
                    if(notFirstCriteria){sb.append("AND ");} else {notFirstCriteria = true;}
                    if (params.isHasResponseEvent()) {
                        sb.append("responseevent_eventid IS NOT NULL ");
                    } else {
                        sb.append("responseevent_eventid IS NULL ");
                    }
                }

                if (params.isFilterByRequestedResponseEventCat()) {
                    if(notFirstCriteria){sb.append("AND ");} else {notFirstCriteria = true;}
                    sb.append("requestedeventcat_catid = ? ");
                }
                
                if (params.isFilterByRequestor()) {
                    if(notFirstCriteria){sb.append("AND ");} else {notFirstCriteria = true;}
                    sb.append("actionrequestedby_userid = ? ");
                }
                 
                if (params.isFilterByResponderIntended()) {
                    if(notFirstCriteria){sb.append("AND ");} else {notFirstCriteria = true;}
                    sb.append("responderintended_userid = ? ");
                }
                
                if (params.isFilterByResponderActual()) {
                    if(notFirstCriteria){sb.append("AND ");} else {notFirstCriteria = true;}
                    sb.append("responderactual_userid = ? ");
                }
                
                if (params.isFilterByRejectedRequest()) {
                    if(notFirstCriteria){sb.append("AND ");} else {notFirstCriteria = true;}
                    if (params.isRejectedRequest()) {
                        sb.append("rejeecteventrequest = TRUE ");
                    } else {
                        sb.append("rejeecteventrequest = FALSE ");
                    }
                } // close if for rejected
            } // close if for requested action
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
                
                // ALL of these criteria are only evaluated if filtering by case action request
                if(params.isFilterByrequestsAction()){

                    if (params.isFilterByRequestedResponseEventCat()) {
                        stmt.setInt(++paramCounter, params.getEventCategory().getCategoryID());
                    }

                    if (params.isFilterByRequestor()) {
                        stmt.setInt(++paramCounter, params.getUser().getUserID());
                    }

                    if (params.isFilterByResponderIntended()) {
                        stmt.setInt(++paramCounter, params.getUser().getUserID());
                    }

                    if (params.isFilterByResponderActual()) {
                        stmt.setInt(++paramCounter, params.getUser().getUserID());
                    }
                } // close if for requests action
            

                // ignore all other criteria and just search by ID
            } else {
                stmt.setInt(++paramCounter, params.getObjectID());
            }

            System.out.println("EventIntegrator.getEvents | Param counter before execution: " + paramCounter);
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

        String query = "SELECT ceevent.eventid, ceevent.ceeventcategory_catid, ceevent.dateofrecord, \n"
                + "       ceevent.eventtimestamp, ceevent.eventdescription, ceevent.owner_userid, ceevent.disclosetomunicipality, \n"
                + "       ceevent.disclosetopublic, ceevent.activeevent, ceevent.requestsAction, ceevent.hidden, \n"
                + "       ceevent.notes, ceevent.viewconfirmedby, ceevent.viewconfirmedat, cecase.caseid, eventcategory.categoryid\n"
                + " FROM ceevent 	INNER JOIN eventcategory ON (ceeventcategory_catid = categoryid)\n"
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
            System.out.println("EventIntegrator.getUpcomingTimelineEvents | stmt: " + stmt.toString());
            rs = stmt.executeQuery();
            System.out.println("EventIntegrator.getUpcomingTimelineEvents | rs size: " + rs.getFetchSize());

            while (rs.next()) {
                EventCECase ev = new EventCECase();

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

   
    
    
    public void clearResponseToActionRequest(EventCECase ec) throws IntegrationException{
         String query = "UPDATE public.ceevent\n" +
            "   SET responsetimestamp=NULL, respondernotes=NULL, \n" +
            "       responseevent_eventid=NULL, rejeecteventrequest=FALSE, "
                + "responderactual_userid=NULL WHERE eventid = ?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(query);
            stmt.setInt(1, ec.getEventID());

            System.out.println("EventIntegrator.clearing response to action request");
            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive event", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
  

    public EventCECase getEventCECase(int eventID) throws IntegrationException {
        EventCECase ev = null;

        String query = "SELECT eventid, ceeventcategory_catid, cecase_caseid, dateofrecord, \n" +
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
            System.out.println("EventInteegrator.getEventByEventID| sql: " + stmt.toString());
            rs = stmt.executeQuery();

            while (rs.next()) {
                    ev = generateEventFromRS(rs);
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

    public List<EventCECase> getEventsByCaseID(int caseID) throws IntegrationException {
        List<EventCECase> eventList = new ArrayList();

        String query = "SELECT eventid FROM public.ceevent WHERE cecase_caseid = ?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, caseID);
            System.out.println("EventIntegrator.getEventsByCaseID| sql: " + stmt.toString());
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

} // close class
