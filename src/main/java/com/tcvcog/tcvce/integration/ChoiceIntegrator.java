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
import com.tcvcog.tcvce.coordinators.ChoiceCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.Choice;
import com.tcvcog.tcvce.entities.ChoiceEventCat;
import com.tcvcog.tcvce.entities.Directive;
import com.tcvcog.tcvce.entities.ChoiceEventPageNavigation;
import com.tcvcog.tcvce.entities.ChoiceEventRule;
import com.tcvcog.tcvce.entities.Proposal;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.ProposalCECase;
import com.tcvcog.tcvce.entities.ProposalOccPeriod;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import com.tcvcog.tcvce.entities.IFace_Proposable;

/**
 * A Choice is given to the user in a Directive and can take one of the
 following forms:
 An EventCategory
 An EventRuleAbstract
 A page redirection via JSF navigation subsystem
 * @author sylvia
 */
public class ChoiceIntegrator extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of ChoiceIntegrator
     */
    public ChoiceIntegrator() {
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
            choiceEvRule.setRule(ei.rules_getEventRuleAbstract(rs.getInt("eventrule_ruleid")));
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
        ChoiceCoordinator cc = getChoiceCoordinator();
        
        StringBuilder sb = new StringBuilder();
        sb.append(  "SELECT proposalid, directive_directiveid, generatingevent_cecaseeventid, \n" +
                    "       initiator_userid, responderintended_userid, activateson, expireson, \n" +
                    "       responderactual_userid, rejectproposal, responsetimestamp, responseevent_cecaseeventid, \n" +
                    "       active, notes, relativeorder, generatingevent_occeventid, \n" +
                    "       responseevent_occeventid, occperiod_periodid, cecase_caseid \n" +
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
    
    public List<Proposal> getProposalList(CECaseDataHeavy cse) throws IntegrationException{
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
        UserIntegrator ui = getUserIntegrator();
        EventIntegrator ei = getEventIntegrator();
        
        prop.setProposalID(rs.getInt("proposalid"));
        
        prop.setDirective(getDirective(rs.getInt("directive_directiveid")));
        if(rs.getInt("generatingevent_cecaseeventid") != 0){
            prop.setGeneratingEvent(ei.getEvent(rs.getInt("generatingevent_cecaseeventid")));
        }
        if(rs.getInt("generatingevent_occeventid") != 0){
            prop.setGeneratingEvent(ei.getEvent(rs.getInt("generatingevent_occeventid")));
        }
        if(rs.getInt("responseevent_cecaseeventid") != 0){
            prop.setResponseEvent(ei.getEvent(rs.getInt("responseevent_cecaseeventid")));
        }
        if(rs.getInt("responseevent_occeventid") != 0){
            prop.setResponseEvent(ei.getEvent(rs.getInt("responseevent_occeventid")));
        }
               
        prop.setInitiator(ui.getUser(rs.getInt("initiator_userid")));
        
        prop.setResponderIntended(ui.getUser(rs.getInt("responderintended_userid")));
        if(rs.getTimestamp("activateson") != null){
            prop.setActivatesOn(rs.getTimestamp("activateson").toLocalDateTime());
        }
        if(rs.getTimestamp("expireson") != null){
            prop.setExpiresOn(rs.getTimestamp("expireson").toLocalDateTime());
        }
        
        prop.setResponderActual(ui.getUser(rs.getInt("responderactual_userid")));
        prop.setProposalRejected(rs.getBoolean("rejectproposal"));
        if(rs.getTimestamp("responsetimestamp") != null){
            prop.setResponseTS(rs.getTimestamp("responsetimestamp").toLocalDateTime());
        }
        
        prop.setActive(rs.getBoolean("active"));
        prop.setNotes(rs.getString("notes"));
        prop.setOrder(rs.getInt("relativeorder"));
        
        if(rs.getInt("cecase_caseid") != 0){
            ProposalCECase propCECase = new ProposalCECase(prop);
            propCECase.setCeCaseID(rs.getInt("cecase_caseid"));
            return propCECase;
        }
        
        if(rs.getInt("occperiod_periodid") != 0){
            ProposalOccPeriod propPeriod = new ProposalOccPeriod(prop);
            propPeriod.setOccperiodID(rs.getInt("occperiod_periodid"));
            return propPeriod;
        }
        return prop;
        
    }
    
    public void updateProposal(Proposal prop) throws IntegrationException{
          String query =    "UPDATE public.choiceproposal\n" +
                            "   SET directive_directiveid=?, generatingevent_cecaseeventid=?, \n" +   // 1-2
                            "       initiator_userid=?, responderintended_userid=?, activateson=?, \n" + // 3-5
                            "       expireson=?, responderactual_userid=?, rejectproposal=?, responsetimestamp=?, \n" + // 6-9
                            "       responseevent_cecaseeventid=?, active=?, notes=?, relativeorder=?, \n" + // 10-13
                            "       generatingevent_occeventid=?, responseevent_occeventid=?, \n" + // 14-15
                            "       occperiod_periodid=?, cecase_caseid=? \n" + // 16-18
                            " WHERE proposalid=?;"; // 19

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
                if(ev instanceof EventCnF){
                    stmt.setInt(10, prop.getResponseEvent().getEventID());
                    stmt.setNull(15, java.sql.Types.NULL);
                } 
            }
            stmt.setBoolean(11, prop.isActive());
            stmt.setString(12, prop.getNotes());
            stmt.setInt(13, prop.getOrder());
//            stmt.setBoolean(14, prop.isHidden());
            
            if(prop instanceof ProposalCECase){
                ProposalCECase pcec = (ProposalCECase) prop;
                stmt.setInt(17, pcec.getCeCaseID());
            } else {
                ProposalOccPeriod pop = (ProposalOccPeriod) prop;
                stmt.setInt(16, pop.getOccperiodID());
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
            sb.append(" cecase_caseid=?, responseevent_cecaseeventid=?, \n");
        } else if (p instanceof ProposalOccPeriod){
            sb.append(" occperiod_periodid=?, responseevent_occeventid=?, \n");
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
                stmt.setInt(4, pcec.getCeCaseID());
            } else if (p instanceof ProposalOccPeriod){
                ProposalOccPeriod pop = (ProposalOccPeriod) p;
                stmt.setInt(4, pop.getOccperiodID());
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
                "            proposalid, directive_directiveid, generatingevent_cecaseeventid, \n" + //1-2
                "            initiator_userid, responderintended_userid, activateson, expireson, \n" +//3-6
                "            responderactual_userid, rejectproposal, responsetimestamp, responseevent_cecaseeventid, \n" + //7-10
                "            active, notes, relativeorder, generatingevent_occeventid, \n" + //11-14
                "            responseevent_occeventid, occperiod_periodid, cecase_caseid)\n" + //15-17
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
                stmt.setInt(17, pcec.getCeCaseID());
            } else {
                ProposalOccPeriod pop = (ProposalOccPeriod) prop;
                stmt.setInt(16, pop.getOccperiodID());
                
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
}
