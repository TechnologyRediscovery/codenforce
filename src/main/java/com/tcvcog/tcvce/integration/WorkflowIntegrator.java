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

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.application.interfaces.IFace_EventRuleGoverned;
import com.tcvcog.tcvce.coordinators.WorkflowCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Choice;
import com.tcvcog.tcvce.entities.ChoiceEventCat;
import com.tcvcog.tcvce.entities.Directive;
import com.tcvcog.tcvce.entities.ChoiceEventPageNavigation;
import com.tcvcog.tcvce.entities.ChoiceEventRule;
import com.tcvcog.tcvce.entities.Proposal;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventRuleAbstract;
import com.tcvcog.tcvce.entities.EventRuleImplementation;
import com.tcvcog.tcvce.entities.EventRuleSet;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.ProposalCECase;
import com.tcvcog.tcvce.entities.ProposalOccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.tcvcog.tcvce.entities.IFace_Proposable;
import com.tcvcog.tcvce.entities.MuniProfile;
import java.time.LocalDateTime;

/**
 * A Choice is given to the user in a Directive and can take one of the
 following forms:
 An EventCategory
 An EventRuleAbstract
 A page redirection via JSF navigation subsystem
 * @author sylvia
 */
public class WorkflowIntegrator extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of ChoiceIntegrator
     */
    public WorkflowIntegrator() {
    }
    
    /**
     * Extracts a single record from the choice table
     * @param choiceID
     * @return
     * @throws IntegrationException 
     */
    public Choice getChoice(int choiceID) throws IntegrationException{
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
        return c;
    }
    
    /**
     * Extracts all available choice entries in the choicedirectivechoice table
     * mapped to a given directive
     * @param directiveID
     * @return
     * @throws IntegrationException 
     */
    public List<IFace_Proposable> getChoiceList(int directiveID) throws IntegrationException{
        List<IFace_Proposable> choiceList = new ArrayList<>();
  
        StringBuilder sb = new StringBuilder();
        sb.append(  "SELECT choice_choiceid, directive_directiveid\n" +
                    "  FROM public.choicedirectivechoice WHERE directive_directiveid=?; ");
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, directiveID);
            rs = stmt.executeQuery();

            while (rs.next()) {
                choiceList.add(getChoice(rs.getInt("choice_choiceid")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive chocielist ", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return choiceList;
    }
    
    /**
     * Generator method for Choice objects
     * @param rs containing all fields in the choice table
     * @return
     * @throws SQLException
     * @throws IntegrationException 
     */
    private Choice generateChoice(ResultSet rs) throws SQLException, IntegrationException{
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
            choiceEvRule.setRule(rules_getEventRuleAbstract(rs.getInt("eventrule_ruleid")));
            choiceEvRule.setAddRuleFuncSwitch(rs.getBoolean("addeventrule"));
            choice = choiceEvRule;
        } else if (rs.getString("worflowpagetriggerconstantvar") != null){
            ChoiceEventPageNavigation choiceNav = new ChoiceEventPageNavigation();
            choiceNav.setNavigationKeyConstant(rs.getString("worflowpagetriggerconstantvar"));
            choice = choiceNav;
        } else {
            throw new IntegrationException("Choice does not have any content!");
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
     * Extracts a single record from the choiceproposal table
     * @param propID
     * @return
     * @throws IntegrationException 
     */
    public Proposal getProposal(int propID) throws IntegrationException{
        Proposal prop = null;
        WorkflowCoordinator cc = getWorkflowCoordinator();
        
        StringBuilder sb = new StringBuilder();
        sb.append(  "SELECT proposalid, directive_directiveid, generatingevent_eventid, \n" +
                    "       initiator_userid, responderintended_userid, activateson, expireson, \n" +
                    "       responderactual_userid, rejectproposal, responsetimestamp, responseevent_eventid, \n" +
                    "       active, notes, relativeorder, generatingevent_eventid, \n" +
                    "       occperiod_periodid, cecase_caseid \n" +
                    "  FROM public.choiceproposal WHERE proposalid=?;");
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, propID);
            rs = stmt.executeQuery();

            while (rs.next()) {
                prop = generateProposal(rs);
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive event proposal response", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return prop;
        
    }
    
    /**
     * Extracts all available proposals in the choiceProposal table
     * @param cse
     * @return
     * @throws IntegrationException 
     */
    public List<Proposal> getProposalList(CECase cse) throws IntegrationException{
        List<Proposal> proposalList = new ArrayList<>();
  
        StringBuilder sb = new StringBuilder();
        sb.append(  "SELECT proposalid\n" +
                    "  FROM public.choiceproposal\n" +
                    "  WHERE cecase_caseid=?;");
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, cse.getCaseID());
            rs = stmt.executeQuery();

            while (rs.next()) {
                proposalList.add(getProposal(rs.getInt("proposalid")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive event proposal response", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        return proposalList;
    }
    
    /**
     * Extracts all existing proposals mapped to a given OccPeriod
     * @param occPer
     * @return
     * @throws IntegrationException 
     */
    public List<Proposal> getProposalList(OccPeriod occPer) throws IntegrationException{
        
        List<Proposal> proposalList = new ArrayList<>();
  
        StringBuilder sb = new StringBuilder();
        sb.append(  "SELECT proposalid\n" +
                    "  FROM public.choiceproposal\n" +
                    "  WHERE occperiod_periodid=?;");
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, occPer.getPeriodID());
            rs = stmt.executeQuery();
            while (rs.next()) {
                proposalList.add(getProposal(rs.getInt("proposalid")));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive proposal list", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return proposalList;
    }
    
    /**
     * Generator method for a Proposal object
     * @param rs containing all fields in the choiceproposal table
     * @return
     * @throws SQLException
     * @throws IntegrationException 
     */
     private Proposal generateProposal(ResultSet rs) throws SQLException, IntegrationException{
        Proposal prop = new Proposal();
        UserCoordinator uc = getUserCoordinator();
        EventCoordinator ec = getEventCoordinator();
        
        prop.setProposalID(rs.getInt("proposalid"));
        
        prop.setDirective(getDirective(rs.getInt("directive_directiveid")));
        if(rs.getInt("generatingevent_eventid") != 0){
            prop.setGeneratingEvent(ec.getEvent(rs.getInt("generatingevent_eventid")));
        }
        if(rs.getInt("generatingevent_eventid") != 0){
            prop.setGeneratingEvent(ec.getEvent(rs.getInt("generatingevent_eventid")));
        }
        if(rs.getInt("responseevent_eventid") != 0){
            prop.setEvaluationEvent(ec.getEvent(rs.getInt("responseevent_eventid")));
        }
               
        prop.setInitiator(uc.user_getUser(rs.getInt("initiator_userid")));
        
        prop.setResponderIntended(uc.user_getUser(rs.getInt("responderintended_userid")));
        if(rs.getTimestamp("activateson") != null){
            prop.setActivatesOn(rs.getTimestamp("activateson").toLocalDateTime());
        }
        if(rs.getTimestamp("expireson") != null){
            prop.setExpiresOn(rs.getTimestamp("expireson").toLocalDateTime());
        }
        
        prop.setResponderActual(uc.user_getUser(rs.getInt("responderactual_userid")));
        prop.setProposalRejected(rs.getBoolean("rejectproposal"));
        if(rs.getTimestamp("responsetimestamp") != null){
            prop.setResponseTS(rs.getTimestamp("responsetimestamp").toLocalDateTime());
        }
        
        prop.setActive(rs.getBoolean("active"));
        prop.setNotes(rs.getString("notes"));
        prop.setOrder(rs.getInt("relativeorder"));
        
        if(rs.getInt("cecase_caseid") != 0){
            ProposalCECase propCECase = new ProposalCECase(prop, rs.getInt("cecase_caseid"));
            return propCECase;
        }
        
        if(rs.getInt("occperiod_periodid") != 0){
            ProposalOccPeriod propPeriod = new ProposalOccPeriod(prop, rs.getInt("occperiod_periodid"));
            return propPeriod;
        }
        return prop;
        
    }
    
     /**
      * Updates a record in the choiceproposal table
      * @param prop
      * @throws IntegrationException 
      */
    public void updateProposal(Proposal prop) throws IntegrationException{
          String query =    "UPDATE public.choiceproposal\n" +
                            "   SET directive_directiveid=?, generatingevent_eventid=?, \n" +   // 1-2
                            "       initiator_userid=?, responderintended_userid=?, activateson=?, \n" + // 3-5
                            "       expireson=?, responderactual_userid=?, rejectproposal=?, responsetimestamp=?, \n" + // 6-9
                            "       responseevent_eventid=?, active=?, notes=?, relativeorder=?, \n" + // 10-13
                            "       generatingevent_eventid=?, \n" + // 14
                            "       occperiod_periodid=?, cecase_caseid=? \n" + // 15-16
                            " WHERE proposalid=?;"; // 17

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, prop.getDirective().getDirectiveID());
            EventCnF ev = prop.getGeneratingEvent();
            if(ev != null){
                stmt.setInt(2, prop.getGeneratingEvent().getEventID());
            }
            if(prop.getInitiator().getUserID() != 0){
                stmt.setInt(3, prop.getInitiator().getUserID());
            }
            
            if(prop.getResponderIntended() != null){
                stmt.setInt(4, prop.getResponderIntended().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            
            if(prop.getActivatesOn() != null){
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(prop.getActivatesOn()));
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }

            if(prop.getExpiresOn() != null){
                stmt.setTimestamp(6, java.sql.Timestamp.valueOf(prop.getExpiresOn()));
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            
            if(prop.getResponderActual() != null){
                stmt.setInt(7, prop.getResponderActual().getUserID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            
            stmt.setBoolean(8, prop.isProposalRejected());
            if(prop.getResponseTS() != null){
                stmt.setTimestamp(9, java.sql.Timestamp.valueOf(prop.getResponseTS()));
            } else {
                stmt.setNull(9, java.sql.Types.NULL);
            }
            
            ev = prop.getEvaluationEvent();
            
            if(ev != null){
                stmt.setInt(10, prop.getEvaluationEvent().getEventID());
            } else {
                stmt.setNull(10, java.sql.Types.NULL);
            }
            
            stmt.setBoolean(11, prop.isActive());
            stmt.setString(12, prop.getNotes());
            stmt.setInt(13, prop.getOrder());
            
            ev = prop.getGeneratingEvent();
            
            if(ev != null){
                stmt.setInt(14, prop.getEvaluationEvent().getEventID());
            } else {
                stmt.setNull(14, java.sql.Types.NULL);
            }
            if(prop instanceof ProposalCECase){
                ProposalCECase pcec = (ProposalCECase) prop;
                stmt.setInt(16, pcec.getHostObjectID());
                stmt.setNull(15, java.sql.Types.NULL);
            } else {
                ProposalOccPeriod pop = (ProposalOccPeriod) prop;
                stmt.setInt(15, pop.getHostObjectID());
                stmt.setNull(16, java.sql.Types.NULL);
            }
            
//            stmt.setBoolean(18, prop.isHidden());
            stmt.setInt(17, prop.getProposalID());
            
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot udpate proposal implementation, sorry", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        }
    }
    
  
    
    
   /**
    * Extracts a single complete record from the choicedirective table
    * @param directiveID
    * @return
    * @throws IntegrationException 
    */
    public Directive getDirective(int directiveID) throws IntegrationException{

        Directive proposal = new Directive();
        
        StringBuilder sb = new StringBuilder();
        sb.append(      "SELECT directiveid, title, overalldescription, creator_userid, directtodefaultmuniceo, \n" +
                        "       directtodefaultmunistaffer, directtodeveloper, executechoiceiflonewolf, \n" +
                        "       applytoclosedentities, instantiatemultiple, inactivategeneventoneval, \n" +
                        "       maintainreldatewindow, autoinactivateonbobclose, autoinactiveongeneventinactivation, \n" +
                        "       minimumrequireduserranktoview, minimumrequireduserranktoevaluate, \n" +
                        "       active, icon_iconid, relativeorder, directtomunisysadmin, requiredevaluationforbobclose, \n" +
                        "       forcehideprecedingproposals, forcehidetrailingproposals, refusetobehidden\n" +
                        "  FROM public.choicedirective WHERE directiveid=?");
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
            throw new IntegrationException("Cannot retrive Directive", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return proposal;
    }
    
    /**
     * Generator method for Directive objects
     * 
     * @param rs containing all fields in the choicedirective table
     * @return
     * @throws SQLException
     * @throws IntegrationException 
     */
    private Directive generateDirective(ResultSet rs) throws SQLException, IntegrationException{
        UserIntegrator ui = getUserIntegrator();
        SystemIntegrator si = getSystemIntegrator();

        Directive dir = new Directive();
        
        dir.setDirectiveID(rs.getInt("directiveid"));
        dir.setTitle(rs.getString("title"));
        dir.setDescription(rs.getString("overalldescription"));
        dir.setCreator(ui.getUser(rs.getInt("creator_userid")));
        dir.setDirectPropToDefaultMuniCEO(rs.getBoolean("directtodefaultmuniceo"));
        
        dir.setDirectPropToDefaultMuniStaffer(rs.getBoolean("directtodefaultmunistaffer"));
        dir.setDirectPropToDeveloper(rs.getBoolean("directtodeveloper"));
        dir.setExecuteChoiceIfLoneWolf(rs.getBoolean("executechoiceiflonewolf"));
        
        dir.setApplyToClosedBOBs(rs.getBoolean("applytoclosedentities"));
        dir.setInstantiateMultipleOnBOB(rs.getBoolean("instantiatemultiple"));
        dir.setInactivateGeneratingEventOnEvaluation(rs.getBoolean("inactivategeneventoneval"));
        
        dir.setMaintainRelativeDateWindow(rs.getBoolean("maintainreldatewindow"));
        dir.setAutoInactiveOnBOBClose(rs.getBoolean("autoinactivateonbobclose"));
        dir.setAutoInactiveOnGenEventInactivation(rs.getBoolean("autoinactiveongeneventinactivation"));
        
        dir.setMinimumRequiredUserRankToView(rs.getInt("minimumrequireduserranktoview"));
        dir.setMinimumRequiredUserRankToEvaluate(rs.getInt("minimumrequireduserranktoevaluate"));
        
        dir.setActive(rs.getBoolean("active"));
        dir.setIcon(si.getIcon(rs.getInt("icon_iconid")));
        dir.setRelativeorder(rs.getInt("relativeorder"));
        dir.setDirectPropToMuniSysAdmin(rs.getBoolean("directtomunisysadmin"));
        dir.setRequiredEvaluationForBOBClose(rs.getBoolean("requiredevaluationforbobclose"));
        
        dir.setForceHidePrecedingProps(rs.getBoolean("forcehideprecedingproposals"));
        dir.setForceHideTrailingProps(rs.getBoolean("forcehidetrailingproposals"));
        dir.setRefuseToBeHidden(rs.getBoolean("refusetobehidden"));
        
        dir.setChoiceList(getChoiceList(dir.getDirectiveID()));
        
        return dir;
    }
    
    /**
     * Extracts all records from the choicedirective table and returns IDs only
     * for coordinator to turn into fully-fledge Directive objects if it pleases
     * @return never null
     * @throws IntegrationException 
     */
    public List<Integer> getDirectiveDump() throws IntegrationException{

        List<Integer> dirIDList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT directiveid FROM public.choicedirective;");
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {

            stmt = con.prepareStatement(sb.toString());
            
            rs = stmt.executeQuery();

            while (rs.next()) {
                dirIDList.add(rs.getInt("directiveid"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot retrive Directive", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return dirIDList;
        
        
    }
    
    /**
     * Creates a new record in the choicedirective table
     * @param dir
     * @throws IntegrationException 
     */
    public void insertDirective(Directive dir) throws IntegrationException{
         String query = "INSERT INTO public.choicedirective(\n" +
                        "            directiveid, title, overalldescription, creator_userid, directtodefaultmuniceo, \n" +
                        "            directtodefaultmunistaffer, directtodeveloper, executechoiceiflonewolf, \n" +
                        "            applytoclosedentities, instantiatemultiple, inactivategeneventoneval, \n" +
                        "            maintainreldatewindow, autoinactivateonbobclose, autoinactiveongeneventinactivation, \n" +
                        "            minimumrequireduserranktoview, minimumrequireduserranktoevaluate, \n" +
                        "            active, icon_iconid, relativeorder, directtomunisysadmin, requiredevaluationforbobclose, \n" +
                        "            forcehideprecedingproposals, forcehidetrailingproposals, refusetobehidden)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, \n" +
                        "            ?, ?, ?, \n" +
                        "            ?, ?, ?, \n" +
                        "            ?, ?, \n" +
                        "            ?, ?, ?, ?, ?, \n" +
                        "            ?, ?, ?);";

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setString(1, dir.getTitle());
            stmt.setString(2, dir.getDescription());
            stmt.setInt(3, dir.getCreator().getUserID());
            stmt.setBoolean(4, dir.isDirectPropToDefaultMuniCEO());
            
            stmt.setBoolean(5, dir.isDirectPropToDefaultMuniStaffer());
            stmt.setBoolean(6, dir.isDirectPropToDeveloper());
            stmt.setBoolean(7, dir.isExecuteChoiceIfLoneWolf());
            
            stmt.setBoolean(8, dir.isApplyToClosedBOBs());
            stmt.setBoolean(9, dir.isInstantiateMultipleOnBOB());
            stmt.setBoolean(10, dir.isInactivateGeneratingEventOnEvaluation());
            
            stmt.setBoolean(11, dir.isMaintainRelativeDateWindow());
            stmt.setBoolean(12, dir.isAutoInactiveOnBOBClose());
            stmt.setBoolean(13, dir.isAutoInactiveOnGenEventInactivation());
            
            stmt.setInt(14, dir.getMinimumRequiredUserRankToView());
            stmt.setInt(15, dir.getMinimumRequiredUserRankToEvaluate());
            
            stmt.setBoolean(16, dir.isActive());
            stmt.setInt(17, dir.getIcon().getIconid());
            stmt.setInt(18, dir.getRelativeorder());
            stmt.setBoolean(19, dir.isDirectPropToMuniSysAdmin());
            stmt.setBoolean(20, dir.isRequiredEvaluationForBOBClose());
            
            stmt.setBoolean(21, dir.isForceHidePrecedingProps());
            stmt.setBoolean(22, dir.isForceHideTrailingProps());
            stmt.setBoolean(23, dir.isRefuseToBeHidden());
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert directive", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    
    /**
     * Updates a record in the choiceproposal table to reflect the evaluation of
     * that proposal by a user
     * @param p
     * @throws IntegrationException 
     */
    public void recordProposalEvaluation(Proposal p) throws IntegrationException{
        StringBuilder sb = new StringBuilder("UPDATE public.choiceproposal ");
        sb.append("   SET responderactual_userid=?, responsetimestamp=now(), \n");
        sb.append("       notes=?, chosen_choiceid=?, \n");
                        
        if(p instanceof ProposalCECase){
            sb.append(" cecase_caseid=?, responseevent_eventid=?, \n");
        } else if (p instanceof ProposalOccPeriod){
            sb.append(" occperiod_periodid=?, responseevent_eventid=?, \n");
        } else {
            throw new IntegrationException("Cannot record given proposal due to incorrect Proposal object type");
        }
//            sb.append(" hidden=? ");
            sb.append(" WHERE proposalid=?;");

        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, p.getResponderActual().getPersonID());
            stmt.setString(2, p.getNotes());
            stmt.setInt(3, p.getChosenChoice().getChoiceID());
            if(p instanceof ProposalCECase){
                ProposalCECase pcec = (ProposalCECase) p;
                stmt.setInt(4, pcec.getHostObjectID());
            } else if (p instanceof ProposalOccPeriod){
                ProposalOccPeriod pop = (ProposalOccPeriod) p;
                stmt.setInt(4, pop.getHostObjectID());
            } 
            
            stmt.setInt(5, p.getEvaluationEvent().getEventID());
//            stmt.setBoolean(6, p.isHidden());
            stmt.setInt(6, p.getProposalID());
            
            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to record proposal evaluation", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
    }
    

    /**
     * Creates a single record in the choiceproposal table
     * @param prop
     * @throws IntegrationException 
     */
    public void insertProposal(Proposal prop) throws IntegrationException{
                        String query = "INSERT INTO public.choiceproposal(\n" +
                "            proposalid, directive_directiveid, generatingevent_eventid, \n" + //1-2
                "            initiator_userid, responderintended_userid, activateson, expireson, \n" +//3-6
                "            responderactual_userid, rejectproposal, responsetimestamp, responseevent_eventid, \n" + //7-10
                "            active, notes, relativeorder, generatingevent_eventid, \n" + //11-14
                "            occperiod_periodid, cecase_caseid)\n" + //15-16
                "    VALUES (DEFAULT, ?, ?, \n" +
                "            ?, ?, ?, ?, \n" +
                "            ?, ?, ?, ?, \n" +
                "            ?, ?, ?, ?, ?, \n" +
                "            ?, ?, ?);";

        
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
             stmt.setInt(1, prop.getDirective().getDirectiveID());
            EventCnF ev = prop.getGeneratingEvent();
            if(ev != null){
                if(ev instanceof EventCnF){
                    stmt.setInt(2, prop.getGeneratingEvent().getEventID());
                    stmt.setNull(14, java.sql.Types.NULL);
                } 
            }
            
            stmt.setInt(3, prop.getInitiator().getUserID());
            
            if(prop.getResponderIntended() != null){
                stmt.setInt(4, prop.getResponderIntended().getUserID());
            } else {
                stmt.setNull(4, java.sql.Types.NULL);
            }
            if(prop.getActivatesOn() != null){
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(prop.getActivatesOn()));
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }

            if(prop.getExpiresOn() != null){
                stmt.setTimestamp(6, java.sql.Timestamp.valueOf(prop.getExpiresOn()));
            } else {
                stmt.setNull(6, java.sql.Types.NULL);
            }
            if(prop.getResponderActual() != null){
                stmt.setInt(7, prop.getResponderActual().getUserID());
            } else {
                stmt.setNull(7, java.sql.Types.NULL);
            }
            stmt.setBoolean(8, prop.isProposalRejected());
            if(prop.getResponseTS() != null){
                stmt.setTimestamp(9, java.sql.Timestamp.valueOf(prop.getResponseTS()));
            } else {
                stmt.setNull(9, java.sql.Types.NULL);
            }
            ev = prop.getEvaluationEvent();
            if(ev != null){
                stmt.setInt(10, prop.getEvaluationEvent().getEventID());
                stmt.setNull(15, java.sql.Types.NULL);
            }
            stmt.setBoolean(11, prop.isActive());
            stmt.setString(12, prop.getNotes());
            stmt.setInt(13, prop.getOrder());
            
             if(prop instanceof ProposalCECase){
                ProposalCECase pcec = (ProposalCECase) prop;
                stmt.setInt(17, pcec.getHostObjectID());
            } else {
                ProposalOccPeriod pop = (ProposalOccPeriod) prop;
                stmt.setInt(16, pop.getHostObjectID());
                
            }
            
            stmt.execute();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to insert EventProposal", ex);

        } finally {
             if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
             if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
    
//    --------------------------------------------------------------------------
//    ************************** EVENT RULES *********************************** 
//    --------------------------------------------------------------------------
    
    
    
     /**
     * Getter for EventRuleImplementations by ID
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
            String s =  "SELECT ruleid, title, description, requiredeventtype, forbiddeneventtype, \n" +
                        "       requiredeventcat_catid, requiredeventcatupperboundtypeintorder, \n" +
                        "       requiredeventcatupperboundglobalorder, forbiddeneventcat_catid, \n" +
                        "       forbiddeneventcatupperboundtypeintorder, forbiddeneventcatupperboundglobalorder, \n" +
                        "       mandatorypassreqtocloseentity, autoremoveonentityclose, triggeredeventcatonpass, \n" +
                        "       triggeredeventcatonfail, active, notes, requiredeventcatthresholdtypeintorder, \n" +
                        "       forbiddeneventcatthresholdtypeintorder, requiredeventcatthresholdglobalorder, \n" +
                        "       forbiddeneventcatthresholdglobalorder, promptingdirective_directiveid, \n" +
                        "       userrankmintoconfigure, userrankmintoimplement, userrankmintowaive, \n" +
                        "       userrankmintooverride, userrankmintodeactivate\n" +
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
     * Fetches all records in the table eventrule table and returns their ID
     * 
     * @return
     * @throws IntegrationException 
     */
    public List<Integer> rules_getEventRuleDump() throws IntegrationException{
        
        List<Integer> ruleIDList = new ArrayList<>();
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String s =  "SELECT ruleid  FROM public.eventrule;";
            stmt = con.prepareStatement(s);

            rs = stmt.executeQuery();
            while(rs.next()){
                ruleIDList.add(rs.getInt("ruleid"));
            }

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Unable to generate case history list", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally
        
        return ruleIDList;
        
        
    }
    
    
     /**
     * Generator method for EventRuleAbstract objects 
     * 
     * @param rs
     * @return
     * @throws SQLException
     */
    private EventRuleAbstract rules_generateEventRule(ResultSet rs) throws SQLException, IntegrationException{
        EventRuleAbstract evRule = new EventRuleAbstract();
        WorkflowIntegrator ci = getWorkflowIntegrator();
        EventIntegrator ei = getEventIntegrator();
        
        evRule.setRuleid(rs.getInt("ruleid"));
        evRule.setTitle(rs.getString("title"));
        evRule.setDescription(rs.getString("description"));
        
        if(!(rs.getString("requiredeventtype") == null) && !(rs.getString("requiredeventtype").equals(""))){
            evRule.setRequiredEventType(EventType.valueOf(rs.getString("requiredeventtype")));
        }
        if(!(rs.getString("forbiddeneventtype") == null) && !(rs.getString("forbiddeneventtype").equals(""))){
            evRule.setForbiddenEventType(EventType.valueOf(rs.getString("forbiddeneventtype")));
        }
        
        evRule.setRequiredEventCategory(ei.getEventCategory(rs.getInt("requiredeventcat_catid")));
        evRule.setRequiredECThreshold_typeInternalOrder(rs.getInt("requiredeventcatthresholdtypeintorder"));
        
        evRule.setRequiredECThreshold_typeInternalOrder_treatAsUpperBound(rs.getBoolean("requiredeventcatupperboundtypeintorder"));
        evRule.setRequiredECThreshold_globalOrder(rs.getInt("requiredeventcatthresholdglobalorder"));
        
        evRule.setRequiredECThreshold_globalOrder_treatAsUpperBound(rs.getBoolean("requiredeventcatupperboundglobalorder"));
        evRule.setForbiddenEventCategory(ei.getEventCategory(rs.getInt("forbiddeneventcat_catid")));
        
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
            evRule.setTriggeredECOnRulePass(ei.getEventCategory(rs.getInt("triggeredeventcatonpass")));
        }
        if(rs.getInt("triggeredeventcatonfail") != 0){
            evRule.setTriggeredECOnRuleFail(ei.getEventCategory(rs.getInt("triggeredeventcatonfail")));
        }
        evRule.setActiveRuleAbstract(rs.getBoolean("active"));
        evRule.setNotes(rs.getString("notes"));
        
        evRule.setUserRankMinToConfigure(rs.getInt("userrankmintoconfigure"));
        evRule.setUserRankMinToImplement(rs.getInt("userrankmintoimplement"));
        evRule.setUserRankMinToWaive(rs.getInt("userrankmintowaive"));
        evRule.setUserRankMinToOverride(rs.getInt("userrankmintooverride"));
        evRule.setUserRankMinToDeactivate(rs.getInt("userrankmintodeactivate"));
        
        return evRule;
    }
    
    /**
     * EventRules are grouped by sets, which are maintained in the DB with 
     * m:m tables so we can get a list of EventRules by setID and attach that
     * entire bundle to a BOb
     * 
     * @param ruleSetID
     * @return
     * @throws IntegrationException 
     */
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
    
    /**
     * Extracts all available EventRuleSets mapped to a given MuniProfile
     * @param profile
     * @return
     * @throws IntegrationException 
     */
    public List<EventRuleSet> rules_getEventRuleSetList(MuniProfile profile) throws IntegrationException{
        List<EventRuleSet> setList = new ArrayList<>();
        String query =  "SELECT muniprofile_profileid, ruleset_setid FROM \n" +
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
    
    
    /**
     * Extracts a complete list of EventRuleSets for configuration
     * @return
     * @throws IntegrationException 
     */
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
    
    /**
     * Extracts a single EventRuleSet by ID
     * @param setID
     * @return
     * @throws IntegrationException 
     */
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
    
    /**
     * Generator for an EventRuleSet
     * @param rs
     * @return
     * @throws SQLException
     * @throws IntegrationException 
     */
    private EventRuleSet rules_generateRuleSet(ResultSet rs) throws SQLException, IntegrationException{
        EventRuleSet s = new EventRuleSet();
        s.setRulseSetID(rs.getInt("rulesetid"));
        s.setTitle(rs.getString("title"));
        s.setDescription(rs.getString("description"));
        s.setRuleList(rules_getEventRuleList(rs.getInt("rulesetid")));
        return s;
    }
    
    /**
     * Extracts an EventRuleImplementation by ID 
     * @param implid
     * @return
     * @throws IntegrationException 
     */
    public EventRuleImplementation rules_getEventRuleImplemention(int implid) throws IntegrationException{
        EventRuleImplementation ruleImp = null;
        
        String query =  "SELECT erimplid, eventrule_ruleid, cecase_caseid, occperiod_periodid, \n" +
                        "       implts, implby_userid, lastevaluatedts, passedrulets, triggeredevent_eventid, \n" +
                        "       waivedts, waivedby_userid, passoverridets, passoverrideby_userid, \n" +
                        "       deacts, deacby_userid, notes\n" +
                        "  FROM public.eventruleimpl WHERE erimplid=?;";
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, implid);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                ruleImp = rules_generateEventRuleImplementation(rs);
                
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot generate event rule implementation", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return ruleImp;
        
    }
    
    /**
     * Generator method for EventRileImplementations
     * @param rs
     * @return
     * @throws SQLException
     * @throws IntegrationException 
     */
    private EventRuleImplementation rules_generateEventRuleImplementation(ResultSet rs) throws SQLException, IntegrationException{
        UserCoordinator uc = getUserCoordinator();
        EventCoordinator ec = getEventCoordinator();
        
        // create our imp by passing an EventRuleAbstract into the constructor for 
        // EventRuleImplementation
        EventRuleImplementation impl = new EventRuleImplementation(rules_getEventRuleAbstract(rs.getInt("eventrule_ruleid")));
        
        // The WorkflowCoordinator's configureEventRuleImplementation will
        // set the EventDomain based on which of these is nonzero
        impl.setCeCaseID(rs.getInt("cecase_caseid"));
        impl.setOccPeriodID(rs.getInt("occperiod_periodid"));
        
        impl.setImplementationTS(rs.getTimestamp("attachedts").toLocalDateTime());
        impl.setImplementedBy(uc.user_getUser(rs.getInt("implby_userid")));
        
        if(rs.getTimestamp("lastevaluatedts") != null){
            impl.setLastEvaluatedTS(rs.getTimestamp("lastevaluatedts").toLocalDateTime());
        } 
        
        if(rs.getTimestamp("passedrulets") != null){
            impl.setLastEvaluatedTS(rs.getTimestamp("passedrulets").toLocalDateTime());
        } 
        
        if(rs.getInt("triggeredevent_eventid") != 0){
            impl.setTriggeredEvent(ec.getEvent(rs.getInt("triggeredevent_eventid")));
        }
        
        if(rs.getInt("triggeredevent_eventid") != 0){
            impl.setTriggeredEvent(ec.getEvent(rs.getInt("triggeredevent_eventid")));
        }
        
        if(rs.getTimestamp("waivedts") != null){
            impl.setWaivedTS(rs.getTimestamp("waivedts").toLocalDateTime());
        }
        impl.setWaivedBy(uc.user_getUser(rs.getInt("waivedby_userid")));
        
        if(rs.getTimestamp("passoverridets") != null){
            impl.setPassOverrideTS(rs.getTimestamp("passoverridets").toLocalDateTime());
        }
        impl.setPassOverrideBy(uc.user_getUser(rs.getInt("passoverrideby_userid")));
        
        if(rs.getTimestamp("deacts") != null){
            impl.setDeactivatedTS(rs.getTimestamp("deacts").toLocalDateTime());
        }
        impl.setDeactivatedBy(uc.user_getUser(rs.getInt("deacby_userid")));
        
        impl.setNotes(rs.getString("notes"));
       
       return impl;
    }
    
    /**
     * Used by the WorkflowCoordinator to build the rule list for a given 
     * CECase or OccPeriod. 
     * @param erg as of June 2020 implementers include CECase and OccPeriod
     * @return a simple list of IDs. Up to the caller to turn them into Objects
     * that are property configured
     * @throws IntegrationException 
     */
    public List<Integer> rules_getEventRuleImplementationList(IFace_EventRuleGoverned erg) throws IntegrationException{
        EventRuleImplementation ruleImp;
        List<Integer> implList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT erimplid FROM public.eventruleimpl WHERE ");
        
        switch(erg.discloseEventDomain()){
            case CODE_ENFORCEMENT:
                sb.append("cecase_caseid=?;");
                break;
            case OCCUPANCY:
                sb.append("occperiod_periodid=?;");
                break;
            default:
                sb.append("cecase_caseid=?");
        }
        
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(sb.toString());
            stmt.setInt(1, erg.getBObID());
            
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                implList.add(rs.getInt("erimplid"));
            }
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot generate list of event rules", ex);
        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
            if (rs != null) { try { rs.close(); } catch (SQLException ex) { /* ignored */ } }
        } // close finally

        return implList;
    }
   
    
    /**
     * Entryway for new EventRuleAbstracts into the DB
     * @param evrua
     * @return the most recently issued event rule ID
     * @throws IntegrationException 
     */
    public int rules_insertEventRule(EventRuleAbstract evrua) throws IntegrationException {

        String query = "INSERT INTO public.eventrule(\n" +
                        "            ruleid, title, description, requiredeventtype, forbiddeneventtype, \n" +
                        "            requiredeventcat_catid, requiredeventcatupperboundtypeintorder, \n" +
                        "            requiredeventcatupperboundglobalorder, forbiddeneventcat_catid, \n" +
                        "            forbiddeneventcatupperboundtypeintorder, forbiddeneventcatupperboundglobalorder, \n" +
                        "            mandatorypassreqtocloseentity, autoremoveonentityclose, triggeredeventcatonpass, \n" +
                        "            triggeredeventcatonfail, active, notes, requiredeventcatthresholdtypeintorder, \n" +
                        "            forbiddeneventcatthresholdtypeintorder, requiredeventcatthresholdglobalorder, \n" +
                        "            forbiddeneventcatthresholdglobalorder, promptingdirective_directiveid, \n" +
                        "            userrankmintoconfigure, userrankmintoimplement, userrankmintowaive, \n" +
                        "            userrankmintooverride, userrankmintodeactivate)\n" +
                        "    VALUES (DEFAULT, ?, ?, CAST (? AS eventtype), CAST (? AS eventtype), \n" + 
                        "            ?, ?, \n" +
                        "            ?, ?, \n" + // 7-8
                        "            ?, ?, \n" + //9-10
                        "            ?, ?, ?, \n" + // 11 mand -13 trigg
                        "            ?, ?, ?, ?, \n" + // 14 - trigcatfail - 17 reqtypintorder
                        "            ?, ?, \n" + // 18-forbidthrestypeint, 19-reqeventcatethresglobal
                        "            ?, ?, \n" + //20-forbideventcatglobal, 21-directive
                        "            ?, ?, ?, \n" +
                        "            ?, ?)";
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
            if(evrua.getTriggeredECOnRulePass() != null){
                stmt.setInt(13, evrua.getTriggeredECOnRulePass().getCategoryID());
            } else {
                stmt.setNull(13, java.sql.Types.NULL);
            }

            
            if(evrua.getTriggeredECOnRuleFail() != null){
                stmt.setInt(14, evrua.getTriggeredECOnRuleFail().getCategoryID());
            } else {
                stmt.setNull(14, java.sql.Types.NULL);
            }
            stmt.setBoolean(15, evrua.isActiveRuleAbstract());
            stmt.setString(16, evrua.getNotes());
            stmt.setInt(17, evrua.getRequiredECThreshold_typeInternalOrder());
            
            stmt.setInt(18, evrua.getForbiddenECThreshold_typeInternalOrder());
            stmt.setInt(19, evrua.getRequiredECThreshold_globalOrder());
            
            stmt.setInt(20, evrua.getForbiddenECThreshold_globalOrder());            
            if(evrua.getPromptingDirective()!= null){
                stmt.setInt(21, evrua.getPromptingDirective().getDirectiveID());
            } else {
                stmt.setNull(21, java.sql.Types.NULL);
            }
            
            stmt.setInt(22, evrua.getUserRankMinToConfigure());
            stmt.setInt(23, evrua.getUserRankMinToImplement());
            stmt.setInt(24, evrua.getUserRankMinToWaive());
            stmt.setInt(25, evrua.getUserRankMinToOverride());
            stmt.setInt(26, evrua.getUserRankMinToDeactivate());
            
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
       
    } 
    
    /**
     * Implements SQL UPDATE on EventRuleAbstract objects
     * 
     * @param evrua
     * @throws IntegrationException 
     */
    public void rules_updateEventRule(EventRuleAbstract evrua) throws IntegrationException {

        String query = "UPDATE public.eventrule\n" +
                    "   SET title=?, description=?, requiredeventtype=CAST (? AS eventtype), forbiddeneventtype= CAST (? AS eventtype), \n" +
                    "       requiredeventcat_catid=?, requiredeventcatupperboundtypeintorder=?, \n" +
                    "       requiredeventcatupperboundglobalorder=?, forbiddeneventcat_catid=?, \n" +
                    "       forbiddeneventcatupperboundtypeintorder=?, forbiddeneventcatupperboundglobalorder=?, \n" +
                    "       mandatorypassreqtocloseentity=?, autoremoveonentityclose=?, promptingdirective_directiveid=?, \n" +
                    "       triggeredeventcatonpass=?, triggeredeventcatonfail=?, active=?, \n" +
                    "       notes=?, requiredeventcatthresholdtypeintorder=?, forbiddeneventcatthresholdtypeintorder=?, \n" +
                    "       requiredeventcatthresholdglobalorder=?, forbiddeneventcatthresholdglobalorder=?, \n" +
                    "       userrankmintoconfigure=?, userrankmintoimplement=?, userrankmintowaive=?, \n" +
                    "       userrankmintooverride=?, userrankmintodeactivate=? WHERE ruleid=?;";
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
            
            stmt.setInt(22, evrua.getUserRankMinToConfigure());
            stmt.setInt(23, evrua.getUserRankMinToImplement());
            stmt.setInt(24, evrua.getUserRankMinToWaive());
            stmt.setInt(25, evrua.getUserRankMinToOverride());
            stmt.setInt(26, evrua.getUserRankMinToDeactivate());

            stmt.setInt(27, evrua.getRuleid());
            
            stmt.executeUpdate();

        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot update EventRuleAbstract into the system", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    } // close method
      
    
  
    /**
     * Makes an entry in the linking table eventruleruleset to add an EventRuleAbstract
     * to a given setID
     * @param era
     * @param eventRuleSetID
     * @throws IntegrationException 
     */
    public void rules_addEventRuleAbstractToRuleSet(EventRuleAbstract era, int eventRuleSetID) throws IntegrationException{
        
        
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

    
    /**
     * Creates a new record in the eventruleimpl table which models a CECase or OccPeriod
     * implementing an EventRuleAbstract
     * 
     * @param erimpl
     * @param erg
     * @return
     * @throws IntegrationException 
     */
    public int implementEventRule(EventRuleImplementation erimpl, IFace_EventRuleGoverned erg) throws IntegrationException{
        
        String query =  "INSERT INTO public.eventruleimpl(\n" +
                        "            erimplid, eventrule_ruleid, cecase_caseid, occperiod_periodid, \n" +
                        "            implts, implby_userid, lastevaluatedts, passedrulets, triggeredevent_eventid, \n" +
                        "            waivedts, waivedby_userid, passoverridets, passoverrideby_userid, \n" +
                        "            deacts, deacby_userid, notes)\n" +
                        "    VALUES (DEFAULT, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, ?, \n" +
                        "            ?, ?, ?, ?, \n" +
                        "            ?, ?, ?);";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;
        int freshImpID = 0;
        try {
            stmt = con.prepareStatement(query);
            
            stmt.setInt(1, erimpl.getRuleid());
            if(erg instanceof OccPeriod){
                stmt.setInt(2, erg.getBObID());
                stmt.setNull(3, java.sql.Types.NULL);
            } else if(erg instanceof CECase){
                stmt.setNull(2, java.sql.Types.NULL);
                stmt.setInt(3, erg.getBObID());
            } else {
                // the Coordinator method should prevent this case
                stmt.setNull(2, java.sql.Types.NULL);
                stmt.setNull(3, java.sql.Types.NULL);
            }
           
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            if(erimpl.getImplementedBy() != null){
                stmt.setInt(5, erimpl.getImplementedBy().getUserID());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            stmt.setNull(6, java.sql.Types.NULL); // lastevalts
            stmt.setNull(7, java.sql.Types.NULL); // passedrulets
            if(erimpl.getTriggeredEvent() != null){
                stmt.setInt(8, erimpl.getTriggeredEvent().getEventID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL); // passedrulets
            }
            
            if(erimpl.getWaivedTS() != null) {
                stmt.setTimestamp(9, java.sql.Timestamp.valueOf(erimpl.getWaivedTS()));
            } else {
                stmt.setNull(9, java.sql.Types.NULL); // passedrulets
            }
            if(erimpl.getWaivedBy() != null){
                stmt.setInt(10, erimpl.getWaivedBy().getUserID());
            } else {
                stmt.setNull(10, java.sql.Types.NULL); // passedrulets
            }
            
            if(erimpl.getPassOverrideTS() != null) {
                stmt.setTimestamp(11, java.sql.Timestamp.valueOf(erimpl.getPassOverrideTS()));
            } else {
                stmt.setNull(11, java.sql.Types.NULL); // passedrulets
            }
            if(erimpl.getPassOverrideBy() != null){
                stmt.setInt(12, erimpl.getPassOverrideBy().getUserID());
            } else {
                stmt.setNull(12, java.sql.Types.NULL); // passedrulets
            }
            
            if(erimpl.getDeactivatedTS() != null) {
                stmt.setTimestamp(13, java.sql.Timestamp.valueOf(erimpl.getDeactivatedTS()));
            } else {
                stmt.setNull(13, java.sql.Types.NULL); // passedrulets
            }
            if(erimpl.getDeactivatedBy() != null){
                stmt.setInt(14, erimpl.getDeactivatedBy().getUserID());
            } else {
                stmt.setNull(14, java.sql.Types.NULL); // passedrulets
            }
            
            stmt.setString(15, erimpl.getNotes());
            
            stmt.execute();
            
            String retrievalQuery = "SELECT currval('eventruleimpl_impid_seq');";
            stmt = con.prepareStatement(retrievalQuery);
            ResultSet rs = stmt.executeQuery();
          
            while(rs.next()){
                freshImpID = rs.getInt(1);
            }
            
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot insert EventRuleCECase into the system", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
        
        return freshImpID;
    }
    
    /**
     * Updates a single record in the eventruleimp table which corresponds
     * with a CECase or OccPeriod implementing an EventRuleAbstract object
     * This method will check for a non-zero value in cecase_caseid or 
     * occperiod_periodid to determine which field to set nonzero and null
     * 
     * @param erimpl
     * @throws IntegrationException 
     */
    public void rules_UpdatetEventRuleImplementation(EventRuleImplementation erimpl) throws IntegrationException{
         String query = "UPDATE public.eventruleimpl\n" +
                        "   SET eventrule_ruleid=?, cecase_caseid=?, occperiod_periodid=?, \n" +
                        "       implts=?, implby_userid=?, lastevaluatedts=?, passedrulets=?, \n" +
                        "       triggeredevent_eventid=?, waivedts=?, waivedby_userid=?, passoverridets=?, \n" +
                        "       passoverrideby_userid=?, deacts=?, deacby_userid=?, notes=?\n" +
                        " WHERE erimplid=?;";
        Connection con = getPostgresCon();
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
              stmt.setInt(1, erimpl.getRuleid());
            if(erimpl.getCeCaseID() != 0){
                stmt.setInt(2, erimpl.getCeCaseID());
                stmt.setNull(3, java.sql.Types.NULL);
            } else if(erimpl.getOccPeriodID() != 0){
                stmt.setNull(2, java.sql.Types.NULL);
                stmt.setInt(3, erimpl.getOccPeriodID());
            } else {
                // the Coordinator method should prevent this case
                stmt.setNull(2, java.sql.Types.NULL);
                stmt.setNull(3, java.sql.Types.NULL);
            }
           
            stmt.setTimestamp(4, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            if(erimpl.getImplementedBy() != null){
                stmt.setInt(5, erimpl.getImplementedBy().getUserID());
            } else {
                stmt.setNull(5, java.sql.Types.NULL);
            }
            stmt.setNull(6, java.sql.Types.NULL); // lastevalts
            stmt.setNull(7, java.sql.Types.NULL); // passedrulets
            if(erimpl.getTriggeredEvent() != null){
                stmt.setInt(8, erimpl.getTriggeredEvent().getEventID());
            } else {
                stmt.setNull(8, java.sql.Types.NULL); // passedrulets
            }
            
            if(erimpl.getWaivedTS() != null) {
                stmt.setTimestamp(9, java.sql.Timestamp.valueOf(erimpl.getWaivedTS()));
            } else {
                stmt.setNull(9, java.sql.Types.NULL); // passedrulets
            }
            if(erimpl.getWaivedBy() != null){
                stmt.setInt(10, erimpl.getWaivedBy().getUserID());
            } else {
                stmt.setNull(10, java.sql.Types.NULL); // passedrulets
            }
            
            if(erimpl.getPassOverrideTS() != null) {
                stmt.setTimestamp(11, java.sql.Timestamp.valueOf(erimpl.getPassOverrideTS()));
            } else {
                stmt.setNull(11, java.sql.Types.NULL); // passedrulets
            }
            if(erimpl.getPassOverrideBy() != null){
                stmt.setInt(12, erimpl.getPassOverrideBy().getUserID());
            } else {
                stmt.setNull(12, java.sql.Types.NULL); // passedrulets
            }
            
            if(erimpl.getDeactivatedTS() != null) {
                stmt.setTimestamp(13, java.sql.Timestamp.valueOf(erimpl.getDeactivatedTS()));
            } else {
                stmt.setNull(13, java.sql.Types.NULL); // passedrulets
            }
            if(erimpl.getDeactivatedBy() != null){
                stmt.setInt(14, erimpl.getDeactivatedBy().getUserID());
            } else {
                stmt.setNull(14, java.sql.Types.NULL); // passedrulets
            }
            stmt.setString(15, erimpl.getNotes());
            stmt.setInt(16, erimpl.getImplementationID());
            
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.toString());
            throw new IntegrationException("Cannot update EventRuleImplementation", ex);

        } finally {
            if (con != null) { try { con.close(); } catch (SQLException e) { /* ignored */} }
            if (stmt != null) { try { stmt.close(); } catch (SQLException e) { /* ignored */} }
        } // close finally
    }
    
}
