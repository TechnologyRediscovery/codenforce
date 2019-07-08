/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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

import com.sun.org.apache.xalan.internal.xsltc.compiler.Constants;
import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.MalformedBOBException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CECaseEvent;
import com.tcvcog.tcvce.entities.Choice;
import com.tcvcog.tcvce.entities.ChoiceEventCat;
import com.tcvcog.tcvce.entities.Directive;
import com.tcvcog.tcvce.entities.ChoiceEventPageNavigation;
import com.tcvcog.tcvce.entities.ChoiceEventRule;
import com.tcvcog.tcvce.entities.Proposal;
import com.tcvcog.tcvce.entities.Event;
import com.tcvcog.tcvce.occupancy.entities.OccPeriod;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A Choice is given to the user in a Directive and can take one of the
 following forms:
 An EventCategory
 An EventRule
 A page redirection via JSF navigation subsystem
 * @author sylvia
 */
public class ChoiceIntegrator extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of ChoiceIntegrator
     */
    public ChoiceIntegrator() {
    }
    
    public Choice getChoice(int choiceID) throws IntegrationException, MalformedBOBException{
        
       Choice c = null;
  
        StringBuilder sb = new StringBuilder();
        sb.append(  " SELECT choiceid, title, description, eventcat_catid, addeventcat, eventrule_ruleid, \n" +
                    "       addeventrule, relativeorder, active, minimumrequireduserranktoview, \n" +
                    "       minimumrequireduserranktochoose, icon_iconid, worflowpagetriggerconstantvar\n" +
                    "  FROM public.choice WHERE choiceid = ?;\n");
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, choiceID);
            rs = stmt.executeQuery();

            while (rs.next()) {
                c = generateChoice(rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive event proposal response", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return new ChoiceEventCat();
    }
    
    public List<Choice> getChoiceList(int proposalID){
        
        return new ArrayList<>();
        
    }
    
    public List<Proposal> getProposal(CECase cse){
        
        return new ArrayList<>();
        
    }
    
    public List<Proposal> getProposalList(OccPeriod occPer){
        
        
        return new ArrayList<>();
        
    }
    
  
    
    
    private Choice generateChoice(ResultSet rs) throws SQLException, MalformedBOBException, IntegrationException{
        EventIntegrator ei = getEventIntegrator();
        SystemIntegrator si = getSystemIntegrator();
        Choice choice;
        
        // CRUDE FIRST DFRAFT AT DECIDING WHAT SUB-TYPE OF CHOICE TO MAKE
        // USE WHICHEVER NONZERO CONTENT COLUMN (EVENTCAT, EVENTRULE, OR NAV)
        // I ENCOUNTER FIRST
        if(rs.getInt("eventcat_catid") != 0) { // This choice will propose an event of a given category
            ChoiceEventCat choiceEvCat = new ChoiceEventCat();
            choiceEvCat.setEventCategory(ei.getEventCategory(rs.getInt("eventcat_catid")));
            choiceEvCat.setAddCategoryFuncSwitch(rs.getBoolean("addeventcat"));
            choice = choiceEvCat;
        } else if (rs.getInt("eventrule_ruleid") != 0) {
            ChoiceEventRule choiceEvRule = new ChoiceEventRule();
            choiceEvRule.setRule(ei.getEventRule(rs.getInt("eventrule_ruleid")));
            choiceEvRule.setAddRuleFuncSwitch(rs.getBoolean("addeventrule"));
            choice = choiceEvRule;
        } else if (rs.getString("worflowpagetriggerconstantvar") != null){
            ChoiceEventPageNavigation choiceNav = new ChoiceEventPageNavigation();
            choiceNav.setNavigationKeyConstant(rs.getString("worflowpagetriggerconstantvar"));
            choice = choiceNav;
        } else {
            throw new MalformedBOBException("Choice does not have any content!");
        }
        
        choice.setChoiceID(rs.getInt("choiceid"));
        choice.setTitle(rs.getString("title"));
        choice.setDescription(rs.getString("description"));
        choice.setActive(rs.getBoolean("active"));
        choice.setMinimumRequiredUserRankToView(rs.getInt("minimumrequireduserranktoview"));
        choice.setMinimumRequiredUserRankToChoose(rs.getInt("minimumrequireduserranktochoose"));
        choice.setIcon(si.getIcon(rs.getInt("icon_iconid")));
        choice.setRelativeOrder(rs.getInt("relativeorder"));
        return choice;
    }
    
    /**
     * TODO: complete for occbeta
     * @param rs
     * @return
     * @throws SQLException
     * @throws IntegrationException 
     */
     private Proposal generateProposal(ResultSet rs) throws SQLException, IntegrationException{
        Proposal propImp = new Proposal();
        UserIntegrator ui = getUserIntegrator();
        
        propImp.setImplementationID(rs.getInt("implementationid"));
        
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
        
        propImp.setNotes(rs.getString("notes"));
        
        return propImp;
        
    }
    
    public void updateProposal(Proposal imp) throws IntegrationException{
          String query =    "UPDATE public.ceeventproposalimplementation\n" +
                            "   SET proposal_propid=?, generatingevent_eventid=?, \n" +
                            "       initiator_userid=?, responderintended_userid=?, activateson=?, \n" +
                            "       expireson=?, expiredorinactive=?, notes=?\n" +
                            " WHERE implementationid=?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
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
    
    public void logResponseToProposal(CECaseEvent ev) throws IntegrationException {

       String query = "UPDATE public.ceeventproposalimplementation\n" +
                        "   SET responderactual_userid=?, rejectproposal=?, responsetimestamp=?, \n" +
                        "       responseevent_eventid=?, notes=?\n" +
                        " WHERE implementationid = ?;";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        Proposal imp;
        
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
    
    
    
  
    public Directive getDirective(int directiveID) throws IntegrationException{

        Directive proposal = new Directive();
        
        StringBuilder sb = new StringBuilder();
        sb.append(      "SELECT directiveid, title, overalldescription, creator_userid, directtodefaultmuniceo, \n" +
                        "       directtodefaultmunistaffer, directtodeveloper, executechoiceiflonewolf, \n" +
                        "       applytoclosedentities, instantiatemultiple, inactivategeneventoneval, \n" +
                        "       maintainreldatewindow, autoinactivateonbobclose, autoinactiveongeneventinactivation, \n" +
                        "       minimumrequireduserranktoview, minimumrequireduserranktoevaluate, \n" +
                        "       active, icon_iconid, directproposaltodefaultmuniadmin, relativeorder, \n" +
                        "       directtomunisysadmin\n" +
                        "  FROM public.choicedirective WHERE directiveid = ?;");
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, directiveID);
            rs = stmt.executeQuery();

            while (rs.next()) {
                proposal = generateDirective(rs);
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
    private Directive generateDirective(ResultSet rs) throws SQLException, IntegrationException{
        Directive directive = new Directive();
        
        directive.setTitle(rs.getString("title"));
        directive.setDescription(rs.getString("overalldescription"));
        
        directive.setDirectPropToDefaultMuniCEO(rs.getBoolean("directproposaltodefaultmuniceo"));
        directive.setDirectPropToDefaultMuniStaffer(rs.getBoolean("directproposaltodefaultmunistaffer"));
        directive.setDirectPropToDeveloper(rs.getBoolean("directproposaltodeveloper"));
        
        directive.setActive(rs.getBoolean("active"));
        
        return directive;
    }
    
    public void insertDirective(Directive prop) throws IntegrationException{
         String query = "INSERT INTO public.choicedirective(\n" +
                        "            directiveid, title, overalldescription, creator_userid, directtodefaultmuniceo, \n" +
                        "            directtodefaultmunistaffer, directtodeveloper, executechoiceiflonewolf, \n" +
                        "            applytoclosedentities, instantiatemultiple, inactivategeneventoneval, \n" +
                        "            maintainreldatewindow, autoinactivateonbobclose, autoinactiveongeneventinactivation, \n" +
                        "            minimumrequireduserranktoview, minimumrequireduserranktoevaluate, \n" +
                        "            active, icon_iconid, directproposaltodefaultmuniadmin, relativeorder, \n" +
                        "            directtomunisysadmin)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, \n" +
                        "            ?, ?, ?, \n" +
                        "            ?, ?, ?, \n" +
                        "            ?, ?, \n" +
                        "            ?, ?, ?, ?, \n" +
                        "            ?);";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, prop.getTitle());
            stmt.setString(2, prop.getDescription());
            stmt.setInt(3, prop.getCreator().getUserID());
            stmt.setBoolean(10, prop.isDirectPropToDefaultMuniCEO());
            
            stmt.setBoolean(11, prop.isDirectPropToDefaultMuniStaffer());
            stmt.setBoolean(12, prop.isDirectPropToDeveloper());
            
            
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
    
     /**
     * 
     * @param event for which we will search for a creation trigger 
     * @return the Event that lists the incoming event as its response
     * @throws IntegrationException 
     */
    public Proposal getProposalImplAssociatedWithEvent(Event event) throws IntegrationException{
        Proposal propImp = null;

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
    
    
    
    /**
     * Builds an Proposal object given the PK of the DB record
     * @param propImpID
     * @return
     * @throws IntegrationException 
     */
    public Proposal getEventProposalImplementation(int propImpID) throws IntegrationException{
         Proposal response = new Proposal();
        
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
                response = generateProposal(rs);
                
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

    public void insertChoiceProposal(Proposal prop) throws MalformedBOBException, IntegrationException{
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
            stmt.setInt(2, prop.getGeneratingEventID());
            
            if(prop.getInitiator() != null){
                stmt.setInt(3, prop.getInitiator().getUserID());
            } else { 
                throw new MalformedBOBException("EventProposalImplementations must contain a User object as an initiator");
            }
            
            if(prop.getResponderIntended()!= null){
                stmt.setInt(4, prop.getResponderIntended().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            stmt.setTimestamp(5, java.sql.Timestamp.valueOf(prop.getActivatesOn()));
            stmt.setTimestamp(6, java.sql.Timestamp.valueOf(prop.getExpiresOn()));
            
            if(prop.getResponderActual() != null){
                stmt.setInt(7, prop.getResponderActual().getUserID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            
            stmt.setBoolean(8, prop.isProposalRejected());
            
            if(prop.getResponseTimestamp() != null){
                stmt.setTimestamp(9, java.sql.Timestamp.valueOf(prop.getResponseTimestamp()));
            } else {
                stmt.setNull(9, java.sql.Types.NULL);
            }

            stmt.setInt(10, prop.getResponseEventID());
            stmt.setString(12, prop.getNotes());
            
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert EventProposal", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    
    private Proposal generateProposal(ResultSet rs, Directive dir){
        Proposal proposal = new Proposal();
        
        
        return proposal;
        
    }

    
    
    
}
