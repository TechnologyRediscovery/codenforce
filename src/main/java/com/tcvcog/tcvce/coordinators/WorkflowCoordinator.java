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
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.application.interfaces.IFace_EventRuleGoverned;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.ChoiceEventCat;
import com.tcvcog.tcvce.entities.Credential;
import com.tcvcog.tcvce.entities.Directive;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventRuleAbstract;
import com.tcvcog.tcvce.entities.EventRuleImplementation;
import com.tcvcog.tcvce.entities.EventRuleSet;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.Proposal;
import com.tcvcog.tcvce.entities.ProposalCECase;
import com.tcvcog.tcvce.entities.ProposalOccPeriod;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodDataHeavy;
import com.tcvcog.tcvce.integration.WorkflowIntegrator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Iterator;
import com.tcvcog.tcvce.entities.IFace_Openable;
import com.tcvcog.tcvce.entities.IFace_Proposable;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsEventRulesEnum;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Holder of business logic related to event rules and proposals. The EventRule
 * object specifies a set of tests against a given list of EventCnF objects which 
 * are implemented in a series of ten or so methods prefixed with 
 * 
 * ruleSubcheck_XXXX
 * 
 * which all boil down to an EventRule either passing or failing, each outcome 
 * of which can trigger the creation of EventCnF objects and Directives which 
 * guide the end user to take appropriate next steps in a given case.
 * 
 * The other half of my methods govern the attachment and evaluation of proposals
 * which consist of one or more possible choices the user can select to indicate
 * certain conditions exist in the actual casework or actual actions were taken.
 * 
 * Since proposals most often result in an EventCnF getting attached to a BOb 
 * (Choices can also trigger page flows and more!), the evaluation of proposals
 * often impacts the outcomes of one or more EventRules also associated with the
 * BOb of concern.
 * 
 * With proposals helping the user create events to document casework and 
 * event rules governing when certain major case events are appropriate (e.g. 
 * issuing an occupancy permit, or filing a citation with the magistrate), 
 * codeNforce can pass as not just a database but an entire workflow management
 * system for code enforcement and occupancy case sequences.
 * 
 * 
 * @author sylvia
 */
public class WorkflowCoordinator extends BackingBeanUtils implements Serializable{

    /**
     * Creates a new instance of ChoiceCoordinator
     */
    public WorkflowCoordinator() {
    }
    
    /**
     * Database object retrieving method for acquiring all Proposals to inject
     * into our BObs
     * 
     * @param erg as of June 2020, these included CECase and OccPeriod objects
     * @param cred
     * @return 
     */
    public List<Proposal> getProposalList(IFace_EventRuleGoverned erg, Credential cred){
        WorkflowIntegrator wi = getWorkflowIntegrator();
        List<Proposal> propList = new ArrayList<>();
        
     
        return propList;
        
    }
    
    /**
     * Coordinator internal logic container for setting switches on Proposals
     * @param proposal
     * @param u
     * @return 
     */
    private void configureProposal( Proposal proposal, Credential cred){
        
        if(proposal != null && cred != null){

            // start by  setting the most restrictive rights and then relax them as authorization
            // status allows
            proposal.setHidden(true);
            proposal.setReadOnlyCurrentUser(true);

            // hide inactives and exit
            if(!proposal.isActive()){
                return;
            }

            if(proposal.getActivatesOn() != null && proposal.getExpiresOn() != null){
                if(proposal.getActivatesOn().isBefore(LocalDateTime.now()) && proposal.getExpiresOn().isAfter((LocalDateTime.now()))){
                    proposal.setHidden(false);

                }
            }
            if(cred.getGoverningAuthPeriod().getRole().getRank() >= proposal.getDirective().getMinimumRequiredUserRankToView()){
                proposal.setHidden(false);
                if(cred.getGoverningAuthPeriod().getRole().getRank() >= proposal.getDirective().getMinimumRequiredUserRankToEvaluate()){
                    proposal.setReadOnlyCurrentUser(false);
                }
            }
            configureChoiceList(proposal, cred);
        }
    }
    
    public Proposal configureChoiceList(Proposal proposal, Credential cred){
        if(proposal != null && cred != null){
            if(proposal.getDirective().getChoiceList() != null){
                Iterator<IFace_Proposable> iter = proposal.getDirective().getChoiceList().iterator();
                while(iter.hasNext()){
                    IFace_Proposable p = iter.next();
                    configureChoice(p, cred);
                }
            }
        }
        return proposal;
    }
    
    private IFace_Proposable configureChoice(IFace_Proposable choice, Credential cred){
        if(choice != null && cred != null){
            choice.setHidden(true);
            choice.setCanChoose(false);

            // hide inactives and exit
            if(!choice.isActive()){
                return choice;
            }
             if(cred.getGoverningAuthPeriod().getRole().getRank() >= choice.getMinimumRequiredUserRankToView()){
                    choice.setHidden(false);
                    if(cred.getGoverningAuthPeriod().getRole().getRank() >= choice.getMinimumRequiredUserRankToChoose()){
                        choice.setCanChoose(true);
                    }
            }
        }
        return choice;
    }
    
    public boolean determineProposalEvaluatability( Proposal proposal,
                                                    IFace_Proposable chosen, 
                                                    User u){
        if(proposal == null || chosen == null || u== null){
            return false;
        }
        // our proposal must contain our desired choice
        if(!proposal.getDirective().getChoiceList().contains(chosen)){
            return false;
        }
        // we must be allowed to choose the choice
        if(!chosen.isCanChoose()){
            return false;
        }
        if(!proposal.isActive()){
            return false;
        }
        if(proposal.getActivatesOn() != null && proposal.getExpiresOn() != null){
            if(!(proposal.getActivatesOn().isBefore(LocalDateTime.now()) 
                    && proposal.getExpiresOn().isAfter((LocalDateTime.now())))){
                return false;
            }
        }
        return true;
    }
    
    public void recordProposalEvaluation(Proposal p) throws IntegrationException{
        WorkflowIntegrator ci = getWorkflowIntegrator();
        p.setHidden(true);
        ci.recordProposalEvaluation(p);
    }
    
    /**
     * Takes in a Directive object and an OccPeriod or CECaseDataHeavy and 
     * implements that directive by assigning it via a Proposal given sensible initial values
     * @param dir Extracted from the EventCnF to be implemented
     * @param erg which in beta v.0.9 are CECaseDataHeavy and OccPeriod objects
     * @param ev 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void implementDirective(Directive dir, IFace_EventRuleGoverned erg, EventCnF ev) 
            throws IntegrationException{
        WorkflowIntegrator wi = getWorkflowIntegrator();
        Proposal pr = new Proposal();
        pr.setDirective(dir);
        if(dir.isActive()){
            pr.setActive(true);
            pr.setActivatesOn(LocalDateTime.now());
            pr.setHidden(false);
            pr.setProposalRejected(false);
            pr.setOrder(0);
        } else {
            return;
        }
        
        if(ev != null){
            pr.setGeneratingEvent(ev);
            pr.setGeneratingEventID(ev.getEventID());
        }
        
        if(erg instanceof OccPeriod){
            OccPeriod op = (OccPeriod) erg;
            if(!dir.isApplyToClosedBOBs()){
                return;
            }
            ProposalOccPeriod pop = new ProposalOccPeriod(pr, op.getPeriodID());
            wi.insertProposal(pop);
            
        } else if(erg instanceof CECaseDataHeavy){
            CECaseDataHeavy cse = (CECaseDataHeavy) erg;
            if(!cse.isOpen() && !dir.isApplyToClosedBOBs()){
                return;
            }
            ProposalCECase pcec = new ProposalCECase(pr, cse.getCaseID());
            wi.insertProposal(pcec);
        }
    }
    
    
    /**
     * Processes requests to reject a proposal by checking user rank, required status, 
     * and the CECaseDataHeavy's or OccPeriod's open/closed status
     * @param p to be rejected
     * @param erg
     * @param u the current session user
     * @throws IntegrationException
     * @throws AuthorizationException
     * @throws BObStatusException if the directive is required for bob close and if it is open.
     * This method does not allow evaluation of a required proposal after BOB is closed. 
     * If this occurs, there's a bug somewhere in the entitylifecycle that anybody could have closed this bob
     */
    public void rejectProposal(Proposal p, IFace_EventRuleGoverned erg, UserAuthorized u) throws IntegrationException, AuthorizationException, BObStatusException{
        WorkflowIntegrator ci = getWorkflowIntegrator();
        if(u.getRole().getRank() >= p.getDirective().getMinimumRequiredUserRankToEvaluate()){
            if(!p.getDirective().isRequiredEvaluationForBOBClose() && erg.isOpen()){
                // configure our proposal for rejection
                p.setProposalRejected(true);
                p.setResponderActual(u);
                p.setResponseTS(LocalDateTime.now());
                p.setHidden(true);
                // send the updates to the integrator
                ci.updateProposal(p);
            } else {
                throw new BObStatusException("Evaluating this proposal is required. This setting can be overriden by an administrator.");
            }
        } else {
            throw new AuthorizationException("You do not have sufficient privileges to reject this propsoal");
        }
    }
    
    public void clearProposalEvaluation(Proposal p, UserAuthorized u) throws IntegrationException, BObStatusException{
        WorkflowIntegrator ci = getWorkflowIntegrator();
        if(p.isReadOnlyCurrentUser()){
            throw new BObStatusException("User cannot clear a proposal they cannot evaluate");
        }
        p.setResponseTS(null);
        p.setResponderActual(null);
        p.setResponseEvent(null);
        p.setProposalRejected(false);
        ci.updateProposal(p);
    }

    public void rules_updateEventRuleAbstract(EventRuleAbstract era) throws IntegrationException {
        WorkflowIntegrator wi = getWorkflowIntegrator();
        wi.rules_updateEventRule(era);
    }

    private boolean ruleSubcheck_forbiddenEventType(CECaseDataHeavy cse, EventRuleAbstract rule) {
        boolean subcheckPasses = true;
        Iterator<EventCnF> iter = cse.getVisibleEventList().iterator();
        while (iter.hasNext()) {
            EventCnF ev = iter.next();
            if (ev.getCategory().getEventType() == rule.getRequiredEventType()) {
                subcheckPasses = false;
            }
        }
        return subcheckPasses;
    }

    private boolean ruleSubcheck_forbiddenEventType(List<EventCnF> eventList, EventRuleAbstract rule) {
        Iterator<EventCnF> iter = eventList.iterator();
        while (iter.hasNext()) {
            EventType evType = iter.next().getCategory().getEventType();
            if (evType == rule.getForbiddenEventType()) {
                return false;
            }
        }
        return true;
    }

    private boolean ruleSubcheck_requiredEventCategory(CECaseDataHeavy cse, EventRuleAbstract rule) {
        boolean subcheckPasses = true;
        if (rule.getRequiredEventCategory().getCategoryID() != 0) {
            subcheckPasses = false;
            Iterator<EventCnF> iter = cse.getVisibleEventList().iterator();
            while (iter.hasNext()) {
                EventCnF ev = iter.next();
                if (ev.getCategory().getCategoryID() == rule.getRequiredEventCategory().getCategoryID()) {
                    subcheckPasses = true;
                }
            }
        }
        return subcheckPasses;
    }

    private boolean ruleSubcheck_requiredEventCategory(List<EventCnF> eventList, EventRuleAbstract rule) {
        Iterator<EventCnF> iter = eventList.iterator();
        while (iter.hasNext()) {
            EventCnF ev = iter.next();
            // simplest case: check for matching categories
            if (ev.getCategory().getCategoryID() == rule.getRequiredEventCategory().getCategoryID()) {
                return true;
            }
            // if we didn't match, perhaps we need to treat the requried category as a threshold
            // to be applied to an event category's type internal relative order
            if (rule.getRequiredECThreshold_typeInternalOrder() != 0) {
                if (rule.isRequiredECThreshold_typeInternalOrder_treatAsUpperBound()) {
                    if (ev.getCategory().getRelativeOrderWithinType() <= rule.getRequiredECThreshold_typeInternalOrder()) {
                        return true;
                    }
                } else {
                    // treat threshold as a lower bound
                    if (ev.getCategory().getRelativeOrderWithinType() >= rule.getRequiredECThreshold_typeInternalOrder()) {
                        return true;
                    }
                }
            }
            // if we didn't pass the rule with type internal ordering as a thresold, check global
            // ordering thresolds
            if (rule.getRequiredECThreshold_globalOrder() != 0) {
                if (rule.isRequiredECThreshold_globalOrder_treatAsUpperBound()) {
                    if (ev.getCategory().getRelativeOrderGlobal() <= rule.getRequiredECThreshold_globalOrder()) {
                        return true;
                    }
                } else {
                    // treat threshold as a lower bound
                    if (ev.getCategory().getRelativeOrderGlobal() >= rule.getRequiredECThreshold_globalOrder()) {
                        return true;
                    }
                }
            }
        }
        // list did not contain an EventCnF whose category was required or required in a specified range
        return false;
    }

    public EventRuleAbstract rules_getEventRuleAbstract(int eraid) throws IntegrationException {
        WorkflowIntegrator wi = getWorkflowIntegrator();
        return wi.rules_getEventRuleAbstract(eraid);
    }

    public boolean rules_evalulateEventRule(List<EventCnF> eventList, EventRuleAbstract rule) throws IntegrationException, BObStatusException{
        
        if (eventList == null || rule == null) {
            throw new BObStatusException("EventCoordinator.evaluateEventRule | Null event list or rule");
        }
        if (rule.getRequiredEventType() != null) {
            if (!ruleSubcheck_requiredEventType(eventList, rule)) {
                return false;
            }
        }
        if (rule.getForbiddenEventType() != null) {
            if (!ruleSubcheck_forbiddenEventType(eventList, rule)) {
                return false;
            }
        }
        if (rule.getRequiredEventCategory() != null) {
            if (!ruleSubcheck_requiredEventCategory(eventList, rule)) {
                return false;
            }
        }
        if (rule.getForbiddenEventCategory() != null) {
            if (!ruleSubcheck_forbiddenEventCategory(eventList, rule)) {
                return false;
            }
        }
        return true;
    }

    private boolean rules_evalulateEventRule(CECaseDataHeavy cse, EventCnF event) throws BObStatusException {
        EventRuleAbstract rule = new EventRuleAbstract();
        boolean rulePasses = false;
        CaseCoordinator cc = getCaseCoordinator();
        if (    ruleSubcheck_requiredEventType(cse, rule) 
             && ruleSubcheck_forbiddenEventType(cse, rule) 
             && ruleSubcheck_requiredEventCategory(cse, rule) 
             && ruleSubcheck_forbiddenEventCategory(cse, rule)
            ) {
                rulePasses = true;
                try {
                    cc.processCaseOnEventRulePass(cse, rule);
                } catch (ViolationException | IntegrationException ex) {
                    System.out.println(ex);
                }
        }
        return rulePasses;
    }

    /**
     * A BOB-agnostic event generator given a Proposal object and the Choice that was
     * selected by the user.
     * @param erg
     * @param p
     * @param ch
     * @param u
     * @return a configured but not integrated EventCnF superclass. The caller will need to cast it to
    the appropriate subclass and insert it
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.EventException
     */
    public EventCnF generateEventDocumentingProposalEvaluation(IFace_EventRuleGoverned erg, Proposal p, IFace_Proposable ch, UserAuthorized u) 
            throws BObStatusException, IntegrationException, EventException {
        EventCnF ev = null;
        EventCoordinator ec = getEventCoordinator();
        if (ch instanceof ChoiceEventCat) {
            EventCategory ecat = ec.initEventCategory(((ChoiceEventCat) ch).getEventCategory().getCategoryID());
            
            // TODO: Finish this
            ev = ec.initEvent(null, ecat);
            ev.setActive(true);
            ev.setHidden(false);
            ev.setTimeStart(LocalDateTime.now());
            ev.setTimeEnd(ev.getTimeStart().plusMinutes(ecat.getDefaultdurationmins()));
            ev.setUserCreator(u);
            ev.setCreationts(LocalDateTime.now());
            StringBuilder descBldr = new StringBuilder();
            descBldr.append("User ");
            descBldr.append(u.getPerson().getFirstName());
            descBldr.append(" ");
            descBldr.append(u.getPerson().getLastName());
            descBldr.append(" evaluated the proposal titled: '");
            descBldr.append(p.getDirective().getTitle());
            descBldr.append("' on ");
            descBldr.append(getPrettyDateNoTime(p.getResponseTS()));
            descBldr.append(" and selected choice titled:  '");
            descBldr.append(ch.getTitle());
            descBldr.append("'.");
            ev.setDescription(descBldr.toString());
        } else {
            throw new BObStatusException("Generating events for Choice " 
                    + "objects that are not Event triggers is not yet supported. " 
                    + "Thank you in advance for your patience.");
        }
        return ev;
    }

    //    -----------------------------------------------------------
    //    ***************** RULES and WORKFLOWS *********************
    //    -----------------------------------------------------------
    /**
     * Calls appropriate Integration method given a CECase or OccPeriod
     * and generates a configured event rule list.
     * @param erg
     * @param cred
     * @return
     */
    public List<EventRuleImplementation> rules_getEventRuleImpList(IFace_EventRuleGoverned erg, Credential cred) {
        WorkflowIntegrator wi = getWorkflowIntegrator();
        List<EventRuleImplementation> imp = new ArrayList<>();
            try {
                if (erg instanceof CECase) {
                    CECase cse = (CECase) erg;
                    imp.addAll(wi.rules_getEventRuleImplemention(cse));
                } else if (erg instanceof OccPeriod) {
                    OccPeriod op = (OccPeriod) erg;
                    imp.addAll(wi.rules_getEventRuleImpOccPeriodList(op));
                }
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
        return imp;
    }

    /**
     * Takes in an EventRuleSet object which contains a list of EventRuleAbstract objects
    and either an OccPeriod or CECaseDataHeavy and implements those abstract rules
    on that particular business object
     * @param ers
     * @param rg
     * @param usr
     * @throws IntegrationException
     * @throws BObStatusException
     */
    public void rules_attachRuleSet(EventRuleSet ers, IFace_EventRuleGoverned rg, UserAuthorized usr) throws IntegrationException, BObStatusException {
        for (EventRuleAbstract era : ers.getRuleList()) {
            if (rg instanceof OccPeriodDataHeavy) {
                OccPeriodDataHeavy op = (OccPeriodDataHeavy) rg;
                rules_attachEventRuleAbstractToOccPeriod(era, op, usr);
            } else if (rg instanceof CECaseDataHeavy) {
                CECaseDataHeavy cec = (CECaseDataHeavy) rg;
                rules_attachEventRuleAbstractToCECase(era, cec);
            } else {
                throw new BObStatusException("Cannot attach rule set");
            }
        }
    }

    private boolean ruleSubcheck_forbiddenEventCategory(CECaseDataHeavy cse, EventRuleAbstract rule) {
        boolean subcheckPasses = true;
        Iterator<EventCnF> iter = cse.getVisibleEventList().iterator();
        while (iter.hasNext()) {
            EventCnF ev = iter.next();
            if (ev.getCategory().getCategoryID() == rule.getRequiredEventCategory().getCategoryID()) {
                subcheckPasses = false;
            }
        }
        return subcheckPasses;
    }

    private boolean ruleSubcheck_forbiddenEventCategory(List<EventCnF> eventList, EventRuleAbstract rule) {
        Iterator<EventCnF> iter = eventList.iterator();
        while (iter.hasNext()) {
            EventCnF ev = iter.next();
            // simplest case: check for matching categories
            if (ev.getCategory().getCategoryID() == rule.getForbiddenEventCategory().getCategoryID()) {
                return false;
            }
            // if we didn't match, perhaps we need to treat the requried category as a threshold
            // to be applied to an event category's type internal relative order
            if (rule.getForbiddenECThreshold_typeInternalOrder() != 0) {
                if (rule.isForbiddenECThreshold_typeInternalOrder_treatAsUpperBound()) {
                    if (ev.getCategory().getRelativeOrderWithinType() <= rule.getForbiddenECThreshold_typeInternalOrder()) {
                        return false;
                    }
                } else {
                    // treat threshold as a lower bound
                    if (ev.getCategory().getRelativeOrderWithinType() >= rule.getForbiddenECThreshold_typeInternalOrder()) {
                        return false;
                    }
                }
            }
            // if we didn't pass the rule with type internal ordering as a thresold, check global
            // ordering thresolds
            if (rule.getForbiddenECThreshold_globalOrder() != 0) {
                if (rule.isForbiddenECThreshold_globalOrder_treatAsUpperBound()) {
                    if (ev.getCategory().getRelativeOrderGlobal() <= rule.getForbiddenECThreshold_globalOrder()) {
                        return false;
                    }
                } else {
                    // treat threshold as a lower bound
                    if (ev.getCategory().getRelativeOrderGlobal() >= rule.getForbiddenECThreshold_globalOrder()) {
                        return false;
                    }
                }
            }
        }
        // list did not contain an EventCnF whose category was required or required in a specified range
        return true;
    }

    /**
     * Returns complete  dump of the eventrule table
     *
     * @return complete event rule list, including inactive events
     * @throws IntegrationException
     */
    public List<EventRuleSet> rules_getEventRuleSetList() throws IntegrationException {
        WorkflowIntegrator wi = getWorkflowIntegrator();
        return wi.rules_getEventRuleSetList();
    }

    /**
     * TODO: Finish my guts
     * @param era
     * @param cse
     */
    private void rules_attachEventRuleAbstractToCECase(EventRuleAbstract era, CECaseDataHeavy cse) {
    }

    private void rules_attachEventRuleAbstractToOccPeriod(EventRuleAbstract era, OccPeriodDataHeavy period, UserAuthorized usr) throws IntegrationException {
        
        WorkflowIntegrator wi = getWorkflowIntegrator();
        EventRuleOccPeriod erop = new EventRuleOccPeriod(new EventRuleImplementation(era));
        // avoid inserting and duplicating keys
        if (wi.rules_getEventRuleOccPeriod(period.getPeriodID(), era.getRuleid()) == null) {
            erop.setImplementationTS(LocalDateTime.now());
            erop.setOccPeriodID(period.getPeriodID());
            erop.setLastEvaluatedTS(null);
            erop.setPassedRuleTS(null);
            erop.setPassedRuleEvent(null);
            wi.rules_insertEventRuleOccPeriod(erop);
        }
        if (era.getPromptingDirective() != null) {
            implementDirective(era.getPromptingDirective(), period, null);
            System.out.println("EventCoordinator.rules_attachEventRulAbstractToOccPeriod | directive implemented with ID " + era.getPromptingDirective().getDirectiveID());
        }
    }

    /**
     * Attaches a single event rule to an EventRuleGoverned entity, the type of which is determined
    internally with instanceof checks for OccPeriod and CECaseDataHeavy Objects
     *
     * @param era
     * @param rg
     * @param usr
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException if an IFaceEventRuleGoverned instances is neither a CECaseDataHeavy or an OccPeriod
     */
    public void rules_attachEventRule(EventRuleAbstract era, IFace_EventRuleGoverned rg, UserAuthorized usr) throws IntegrationException, BObStatusException {
        int freshObjectID = 0;
        if (rg instanceof OccPeriodDataHeavy) {
            OccPeriodDataHeavy op = (OccPeriodDataHeavy) rg;
            rules_attachEventRuleAbstractToOccPeriod(era, op, usr);
            if (freshObjectID != 0 && era.getPromptingDirective() != null) {
                implementDirective(era.getPromptingDirective(), op, null);
                System.out.println("EventCoordinator.rules_attachEventRule | Found not null prompting directive");
            }
        } else if (rg instanceof CECaseDataHeavy) {
            CECaseDataHeavy cec = (CECaseDataHeavy) rg;
            rules_attachEventRuleAbstractToCECase(era, cec);
            if (freshObjectID != 0 && era.getPromptingDirective() != null) {
                implementDirective(era.getPromptingDirective(), cec, null);
            }
        } else {
            throw new BObStatusException("Cannot attach rule set");
        }
    }

    /**
     * TODO: Finish my guts!
     * @param muni to which we want to include the rule. The Municipality's profile will be pulled and its
     * @param era
     */
    public void rules_includeEventRuleAbstractInCECaseDefSet(Municipality muni, EventRuleAbstract era) {
    }

    /**
     * TODO: finish my guts
     * @param era
     * @param cse
     */
    public void rules_attachEventRuleAbstractToMuniCERuleSet(EventRuleAbstract era, CECaseDataHeavy cse) {
    }

    private boolean ruleSubcheck_requiredEventType(CECaseDataHeavy cse, EventRuleAbstract rule) {
        boolean subcheckPasses = true;
        if (rule.getRequiredEventType() != null) {
            subcheckPasses = false;
            Iterator<EventCnF> iter = cse.getVisibleEventList().iterator();
            while (iter.hasNext()) {
                EventCnF ev = iter.next();
                if (ev.getCategory().getEventType() == rule.getRequiredEventType()) {
                    subcheckPasses = true;
                }
            }
        }
        return subcheckPasses;
    }

    private boolean ruleSubcheck_requiredEventType(List<EventCnF> eventList, EventRuleAbstract rule) {
        Iterator<EventCnF> iter = eventList.iterator();
        while (iter.hasNext()) {
            EventType evType = iter.next().getCategory().getEventType();
            if (evType == rule.getRequiredEventType()) {
                return true;
            }
        }
        return false;
    }

    public void rules_attachEventRuleAbstractToOccPeriodTypeRuleSet(EventRuleAbstract era, OccPeriod period) throws IntegrationException {
        WorkflowIntegrator wi = getWorkflowIntegrator();
        wi.rules_addEventRuleAbstractToOccPeriodTypeRuleSet(era, period.getType().getBaseRuleSetID());
    }

    public EventRuleAbstract rules_getInitializedEventRuleAbstract() {
        EventRuleAbstract era = new EventRuleAbstract();
        era.setActiveRuleAbstract(true);
        return era;
    }

    public boolean rules_evaluateEventRules(OccPeriodDataHeavy period) throws IntegrationException, BObStatusException, ViolationException {
        boolean allRulesPassed = true;
        List<EventRuleImplementation> rlst = period.assembleEventRuleList(ViewOptionsEventRulesEnum.VIEW_ALL);
        for (EventRuleAbstract era : rlst) {
            if (!rules_evalulateEventRule(period.assembleEventList(ViewOptionsActiveHiddenListsEnum.VIEW_ALL), era)) {
                allRulesPassed = false;
                break;
            }
        }
        return allRulesPassed;
    }

    /**
     * Primary entrance point for an EventRuleAbstract instance (not its connection to an Object)
     * @param era required instance
     * @param period optional--only if you're attaching to an OccPeriod
     * @param cse Optional--only if you're attachign to a CECaseDataHeavy
     * @param connectToBOBRuleList Switch me on in order to
     * @param usr
     * @return
     * @throws IntegrationException
     */
    public int rules_createEventRuleAbstract(EventRuleAbstract era, OccPeriodDataHeavy period, CECaseDataHeavy cse, boolean connectToBOBRuleList, UserAuthorized usr) throws IntegrationException {
        WorkflowIntegrator wi = getWorkflowIntegrator();
        int freshEventRuleID;
        if (era.getFormPromptingDirectiveID() != 0) {
            Directive dir = wi.getDirective(era.getFormPromptingDirectiveID());
            if (dir != null) {
                era.setPromptingDirective(dir);
                System.out.println("EventCoordinator.rules_createEventRuleAbstract| Found not null directive ID: " + dir.getDirectiveID());
            }
        }
        freshEventRuleID = wi.rules_insertEventRule(era);
        if (period != null && cse == null) {
            era = wi.rules_getEventRuleAbstract(freshEventRuleID);
            rules_attachEventRuleAbstractToOccPeriod(era, period, usr);
            if (connectToBOBRuleList) {
                rules_attachEventRuleAbstractToOccPeriodTypeRuleSet(era, period);
            }
        }
        if (period == null && cse != null) {
            era = wi.rules_getEventRuleAbstract(freshEventRuleID);
            if (connectToBOBRuleList) {
                rules_attachEventRuleAbstractToMuniCERuleSet(era, cse);
            }
        }
        System.out.println("EventCoordinator.rules_createEventRuleAbstract | returned ID: " + freshEventRuleID);
        return freshEventRuleID;
    }
}