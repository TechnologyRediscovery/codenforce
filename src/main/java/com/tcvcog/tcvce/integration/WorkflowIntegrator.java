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
import com.tcvcog.tcvce.coordinators.WorkflowCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.Choice;
import com.tcvcog.tcvce.entities.ChoiceEventCat;
import com.tcvcog.tcvce.entities.Directive;
import com.tcvcog.tcvce.entities.ChoiceEventPageNavigation;
import com.tcvcog.tcvce.entities.ChoiceEventRule;
import com.tcvcog.tcvce.entities.Proposal;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventRuleAbstract;
import com.tcvcog.tcvce.entities.EventRuleCECase;
import com.tcvcog.tcvce.entities.EventRuleImplementation;
import com.tcvcog.tcvce.entities.EventRuleOccPeriod;
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
     * TODO: complete for occbeta
     * @param rs
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
            prop.setResponseEvent(ec.getEvent(rs.getInt("responseevent_eventid")));
        }
               
        prop.setInitiator(uc.getUser(rs.getInt("initiator_userid")));
        
        prop.setResponderIntended(uc.getUser(rs.getInt("responderintended_userid")));
        if(rs.getTimestamp("activateson") != null){
            prop.setActivatesOn(rs.getTimestamp("activateson").toLocalDateTime());
        }
        if(rs.getTimestamp("expireson") != null){
            prop.setExpiresOn(rs.getTimestamp("expireson").toLocalDateTime());
        }
        
        prop.setResponderActual(uc.getUser(rs.getInt("responderactual_userid")));
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
            
            ev = prop.getResponseEvent();
            
            if(ev != null){
                stmt.setInt(10, prop.getResponseEvent().getEventID());
            } else {
                stmt.setNull(10, java.sql.Types.NULL);
            }
            
            
            stmt.setBoolean(11, prop.isActive());
            stmt.setString(12, prop.getNotes());
            stmt.setInt(13, prop.getOrder());
            
            ev = prop.getGeneratingEvent();
            
            if(ev != null){
                stmt.setInt(14, prop.getResponseEvent().getEventID());
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
     
     * 
     * @param rs
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
            
            stmt.setInt(5, p.getResponseEvent().getEventID());
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
            ev = prop.getResponseEvent();
            if(ev != null){
                stmt.setInt(10, prop.getResponseEvent().getEventID());
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
    
    public List<EventRuleCECase> rules_getEventRuleImpCECaseList(CECase cse) throws IntegrationException{
        EventRuleImplementation ruleImp = null;
        List<EventRuleCECase> ruleList = new ArrayList<>();
        String query =  "   SELECT cecase_caseid, eventrule_ruleid, attachedts, attachedby_userid, \n" +
                        "       lastevaluatedts, passedrulets, passedrule_eventid, active \n" +
                        "  FROM public.eventruleimpl WHERE cecase_caseid=?;";
        Connection con = getPostgresCon();
        ResultSet rs = null;
        PreparedStatement stmt = null;

        try {
            stmt = con.prepareStatement(query);
            stmt.setInt(1, cse.getCaseID());
            rs = stmt.executeQuery();
            while (rs.next()) {
                ruleImp = rules_generateEventRuleImplementation(rs);
                ruleList.add(rules_generateEventRuleCECase(rs, ruleImp));
                
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
    
      public List<EventRuleOccPeriod> rules_getEventRuleImpOccPeriodList(OccPeriod op) throws IntegrationException{
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
    
    private EventRuleCECase rules_generateEventRuleCECase(ResultSet rs, EventRuleImplementation imp) 
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
          String query = "INSERT INTO public.eventruleimp (\n" +
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
         String query = "UPDATE public.eventrule\n" +
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
        EventCoordinator ec = getEventCoordinator();
        EventRuleOccPeriod evRule = new EventRuleOccPeriod(imp);
        evRule.setOccPeriodID(rs.getInt("occperiod_periodid"));
        evRule.setPassedRuleEvent(ec.getEvent(rs.getInt("passedrule_eventid")));
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
    
}
