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
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodDataHeavy;
import com.tcvcog.tcvce.integration.WorkflowIntegrator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Iterator;
import com.tcvcog.tcvce.entities.IFace_Proposable;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsEventRulesEnum;
import java.util.ArrayList;
import java.util.List;

/**
 * Holder of business logic related to event rules and proposals. The EventRule
 * object specifies a set of tests against a given list of EventCnF objects which 
 * are implemented in a series of ten or so methods prefixed with <br>
 * <br>
 * ruleSubcheck_XXXX<br>
 * <br>
 * which all boil down to an EventRule either passing or failing, each outcome 
 * of which can trigger the creation of EventCnF objects and Directives which 
 * guide the end user to take appropriate next steps in a given case.<br>
 * 
 * <br>The other half of my methods govern the attachment and evaluation of proposals
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
    
    
    // *************************************************************************
    // *                 DIRECTIVES AND PROPOSALS                              *
    // *************************************************************************
    
    
    public Proposal getProposal(int propid) throws IntegrationException{
        WorkflowIntegrator wi = getWorkflowIntegrator();
        return wi.getProposal(propid);
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
    
    /**
     * Logic intermediary for extraction of Directives (remember: a Directive 
     * is used as the 
     * brains of a Proposal object; Directives are packaged inside this Proposal
     * when that Proposal is attached to an instance of IFace_EventRuleGoverned
     * which in June 2020 included CECase and OccPeriod objects
     * 
     * 
     * @param dirID
     * @return the configured Directive
     * @throws IntegrationException 
     */
    public Directive getDirective(int dirID) throws IntegrationException{
        WorkflowIntegrator wi = getWorkflowIntegrator();
        Directive d = null;
        d = wi.getDirective(dirID);
        return d;
    }
    
    /**
     * Extracts all Directive objects in the DB; useful only for configuration purposes
     * 
     * @param ua
     * @return
     * @throws IntegrationException 
     */
    public List<Directive> getDirectiveListForConfig(UserAuthorized ua) throws IntegrationException{
        WorkflowIntegrator wi = getWorkflowIntegrator();
        List<Directive> dlist = new ArrayList<>();
        
        dlist.addAll(getDirectives(wi.getDirectiveDump()));
        
        return dlist;
        
    }
    
    /**
     * Utility method to iteratively call getDirective(id)
     * @param idList
     * @return
     * @throws IntegrationException 
     */
    private List<Directive> getDirectives(List<Integer> idList) throws IntegrationException{
        List<Directive> dlist = new ArrayList<>();
        if(idList != null && !idList.isEmpty()){
            for(Integer i: idList){
                dlist.add(getDirective(i));
            }
        }
        return dlist;
    }
    
    /**
     * Iterates over the Choices inside a given Proposal and flips switches
     * on them based on the permissions held by the credential param
     * @param proposal
     * @param cred
     * @return 
     */
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
    
    /**
     * Internal logic container for setting up an individual choice
     * @param choice
     * @param cred
     * @return 
     */
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
    
    
   /**
     * Pathway for "Evaluating a Proposal" or in other words, making a workflow choice.
     * The exception list is a beast because so many components of the system are impacted
     * by an event creation.
     * 
     * @param proposal containing the chosen choice
     * @param chosen this method will double check that no funny business is going on
     * namely a choice is trying to be made that's not in the Proposal
     * @param erg the parent ERG
     * @param ua doing the choosing
     * @return all EventCnFs that are ADDED to the parent ERG during evaluation
     * which could be more than 1 since the mother ERG will be refreshed
     * and all rules re-evaluated for each triggered EventCnF, all of which happen
     * before this method returns
     * @throws BObStatusException
     */
    public List<EventCnF> evaluateProposal(     Proposal proposal, 
                                                IFace_Proposable chosen, 
                                                IFace_EventRuleGoverned erg, 
                                                UserAuthorized ua) 
                                        throws  BObStatusException {
        
        WorkflowCoordinator wc = getWorkflowCoordinator();
        EventCoordinator ec = getEventCoordinator();
        WorkflowIntegrator wi = getWorkflowIntegrator();
        
        List<EventCnF> propEvDoneList = new ArrayList<>();
        
        try {
            if(wc.determineProposalEvaluatability(proposal, chosen, ua)){
                // farm out processing to internal methods based on subtype of chosen
                if(chosen instanceof ChoiceEventCat){
                    ChoiceEventCat cec = (ChoiceEventCat) chosen;
                    propEvDoneList.addAll(processChoice(erg, proposal, cec, ua));
                } else if (chosen instanceof ChoiceEventRule){
                    ChoiceEventRule cer = (ChoiceEventRule) chosen;
                    propEvDoneList.addAll(processChoice(erg, proposal, cer, ua));
                }
                // since we can evaluate this proposal with the chosen Proposable, configure members
                proposal.setResponderActual(ua);
                proposal.setResponseTS(LocalDateTime.now());
                proposal.setChosenChoice(chosen);

                // go get our new event by ID and inject it into our proposal before writing its evaluation to DB
                List<EventCnF> tmpEvList = evaluateProposal_recordEvaluation(proposal, chosen, erg, ua);
                
                if(tmpEvList != null && !tmpEvList.isEmpty()){
                    proposal.setEvaluationEvent(tmpEvList.get(0));
                } else {
                    proposal.setEvaluationEvent(null);
                }
                propEvDoneList.addAll(tmpEvList);
                
                // now that the proposal has been evaluated, no need to see it
                // but in reality, it will be marked hidden by the config method 
                // on BOb relaod, so this is just for consistency's sake
                proposal.setHidden(true);
                wi.recordProposalEvaluation(proposal);
            } else {
                throw new BObStatusException("Unable to evaluate proposal due to business rule violation");
            }
        } catch (IntegrationException | EventException ex) {
            throw new BObStatusException(ex.getMessage());
        } 
        return propEvDoneList;
    }
    
    private List<EventCnF> evaluateProposal_recordEvaluation( Proposal proposal, 
                                                        IFace_Proposable chosen, 
                                                        IFace_EventRuleGoverned erg, 
                                                        UserAuthorized ua) 
                                            throws      IntegrationException, 
                                                        BObStatusException, 
                                                        EventException {
        
        EventCoordinator ec = getEventCoordinator();
        EventCnF ev = null;
        EventCategory workflowCat = ec.getEventCategory(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                    .getString("actionRequestPublicUserPersonSourceID")));
        
        ev = ec.initEvent(erg, workflowCat);

        ev.setActive(true);
        ev.setHidden(false);

        ev.setTimeStart(LocalDateTime.now());
        ev.setTimeEnd(ev.getTimeStart().plusMinutes(workflowCat.getDefaultdurationmins()));
        ev.setUserCreator(ua);
        ev.setCreationts(LocalDateTime.now());
        StringBuilder descBldr = new StringBuilder();
        descBldr.append("User ");
        descBldr.append(ua.getPerson().getFirstName());
        descBldr.append(" ");
        descBldr.append(ua.getPerson().getLastName());
        descBldr.append("(");
        descBldr.append(ua.getUsername());
        descBldr.append(") ");
        descBldr.append(" evaluated the proposal titled: '");
        descBldr.append(proposal.getDirective().getTitle());
        descBldr.append("' on ");
        descBldr.append(getPrettyDateNoTime(proposal.getResponseTS()));
        descBldr.append(" and selected choice titled:  '");
        descBldr.append(chosen.getTitle());
        descBldr.append("'.");
        ev.setDescription(descBldr.toString());
        return ec.addEvent(ev, erg, ua);
    }
    
    /**
     * Internal logic intermediary for implementing the EventRuleAbstract object
     * associated with a given Proposal evaluation instance
     * @param erg
     * @param p
     * @param ch
     * @param ua
     * @return
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    private List<EventCnF> processChoice(  IFace_EventRuleGoverned erg,
                                    Proposal p,
                                    ChoiceEventRule ch,
                                    UserAuthorized ua) throws IntegrationException, BObStatusException{
        if(ch.getRule() != null){
            rules_attachEventRule(ch.getRule(), erg, ua);
        }
        
        return null;
    }

    /**
     * A BOB-flexible event generator given a Proposal object and the Choice that was
     * selected by the user.
     * @param erg
     * @param p
     * @param ch
     * @param u
     * @return a configured but not integrated EventCnF. The EventDomain is set before return
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.EventException
     */
    private List<EventCnF> processChoice(   IFace_EventRuleGoverned erg, 
                                            Proposal p, 
                                            ChoiceEventCat ch, 
                                            UserAuthorized u) 
                                    throws  BObStatusException, 
                                            IntegrationException, 
                                            EventException {
        EventCoordinator ec = getEventCoordinator();
        
        EventCnF ev = null;
        
        
        if (ch instanceof ChoiceEventCat) {
            EventCategory ecat = ec.initEventCategory(ch.getEventCategory().getCategoryID());
            ev = ec.initEvent(erg, ecat);
            ev.setDescription(ch.getEventCategory().getHostEventDescriptionSuggestedText());
                   
        } else {
            throw new BObStatusException("Generating events for Choice " 
                    + "objects that are not Event triggers is not yet supported. " 
                    + "Thank you in advance for your patience.");
        }
        return ec.addEvent(ev, erg, u);
    }

    
    /**
     * Logic container for checking if a User can actually make the desired choice
     * which includes checking to make sure the chosen object is an option inside
     * the passed in Proposal object
     * 
     * @param proposal
     * @param chosen
     * @param u
     * @return 
     */
    private boolean determineProposalEvaluatability( Proposal proposal,
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
    
    /**
     * Takes in a Directive object and an OccPeriod or CECaseDataHeavy and 
     * implements that directive by assigning it via a Proposal given sensible initial values
     * @param dir Extracted from the EventCnF to be implemented
     * @param erg which in beta v.0.9 are CECaseDataHeavy and OccPeriod objects
     * @param ev if not null, this event's id will be attached to the implementation of the directive 
     * as having been triggered by this object
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public void implementDirective(Directive dir, IFace_EventRuleGoverned erg, EventCnF ev) 
            throws IntegrationException, BObStatusException{
        
        WorkflowIntegrator wi = getWorkflowIntegrator();
        
        if(erg == null){
            throw new BObStatusException("Cannot implement a directive on a null erg");
        }
        if(dir == null){
            throw new BObStatusException("Cannot implement a null directive");
        }
        
        Proposal pr = new Proposal();
        pr.setDirective(dir);
        if(dir.isActive()){
            pr.setActive(true);
            pr.setActivatesOn(LocalDateTime.now());
            pr.setHidden(false);
            pr.setProposalRejected(false);
            // TODO: implement some logic here for order assignment
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
            if(cse.getStatusBundle() != null && !cse.getStatusBundle().getPhase().isCaseOpen() && !dir.isApplyToClosedBOBs()){
                throw new BObStatusException("Directive does not allow attachment to closed entities");
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
    
    /**
     * Logic container for setting proper fields on a Proposal to clear its
     * previsou evaluation
     * @param p
     * @param u
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public void clearProposalEvaluation(Proposal p, UserAuthorized u) throws IntegrationException, BObStatusException{
        WorkflowIntegrator ci = getWorkflowIntegrator();
        if(p.isReadOnlyCurrentUser()){
            throw new BObStatusException("User cannot clear a proposal they cannot evaluate");
        }
        p.setResponseTS(null);
        p.setResponderActual(null);
        p.setEvaluationEvent(null);
        p.setProposalRejected(false);
        ci.updateProposal(p);
    }
    
    // *************************************************************************
    // *                     EVENT RULES : EVALUATION                          *
    // *************************************************************************
    
    
    
    /**
     * Breaks a given event rule down and farms out checks for each of its subrules
     * and returns an overall pass/fail determination based on those determinations
     * @param eventList
     * @param rule
     * @return pass/fail determination on the overall even rule
     * @throws IntegrationException
     * @throws BObStatusException 
     */
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

    
    /**
     * Evaluates a given list of EventCnF objects for the presence of an EventCnF
     * that CANNOT be in the list for passage of the EventCnF rule
     * 
     * @param eventList
     * @param rule
     * @return a pass/fail determination for this component of the EventRule
     */
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
    
      
    /**
     * Evaluates a given list of EventCnF objects for the presence of a required
     * EventType for the passing of the EventRule
     * @param eventList
     * @param rule
     * @return a pass/fail determination for this component of the EventRule
     */
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

  
/**
 * Evaluates a given List of EventCnF objects to make sure it contains an instance
 * of a required EventCategory for passage of the rule
 * 
 * @param eventList
 * @param rule
 * @return a pass/fail determination for this component of the EventRule
 */
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
    
  

    /**
     * Evaluates a given List of EventCnF objects to see if it contains an event
     * that cannot exist in that List for the EventRule to pass
     * @param eventList
     * @param rule
     * @return a pass/fail determination for the forbidden event category
     */
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

    
    
    // *************************************************************************
    // *                   EVENT RULES : ADMIN                                 *
    // *************************************************************************
    
    /**
     * Extracts all existing records in the eventrule table for config
     * @param ua
     * @return
     * @throws IntegrationException 
     */
    public List<EventRuleAbstract> rules_getEventRuleAbstractListForConfig(UserAuthorized ua) throws IntegrationException{
        WorkflowIntegrator wi = getWorkflowIntegrator();
        List<EventRuleAbstract> eraList = new ArrayList<>();
        List<Integer> idList = wi.rules_getEventRuleDump();
        if(idList != null && !idList.isEmpty()){
            for(Integer i: idList){
                eraList.add(rules_getEventRuleAbstract(i));
            }
        }
        return eraList;
        
    }
    
/**
 * Logic intermediary for retrieval of EventRuleAbstract Objects from the DB
 * @param eraid
 * @return
 * @throws IntegrationException 
 */
    public EventRuleAbstract rules_getEventRuleAbstract(int eraid) throws IntegrationException {
        WorkflowIntegrator wi = getWorkflowIntegrator();
        return wi.rules_getEventRuleAbstract(eraid);
    }

    /**
     * Logic intermediary for updates to to an EventRule's specifications in the
     * DB. Remember, an EventRuleAbstract species a rule that could be but is not
     * yet attached to a given CECase or OccPeriod
     * 
     * @param era
     * @throws IntegrationException 
     */
    public void rules_updateEventRuleAbstract(EventRuleAbstract era) throws IntegrationException {
        WorkflowIntegrator wi = getWorkflowIntegrator();
        wi.rules_updateEventRule(era);
    }
    
    /**
     * Checks User's rank and, if allowed, toggles EventRuleAbstract's active 
     * flag to false and sends update to the DB
     * @param era to deactivate
     * @param ua doing the deactivating
     * @throws IntegrationException 
     */
    public void rules_removeEventRuleAbstract(EventRuleAbstract era, UserAuthorized ua) throws IntegrationException{
        WorkflowIntegrator wi = getWorkflowIntegrator();
        
        if(era.getUserRankMinToDeactivate() <= ua.getKeyCard().getGoverningAuthPeriod().getRole().getRank()){
            era.setActiveRuleAbstract(false);
            wi.rules_updateEventRule(era);
        }
        
    }
    

    /**
     * Calls appropriate Integration method given a CECase or OccPeriod
     * and generates a configured event rule list.
     * @param erg
     * @param cred
     * @return
     */
    public List<EventRuleImplementation> rules_getEventRuleImpList(IFace_EventRuleGoverned erg, Credential cred) throws IntegrationException {
        WorkflowIntegrator wi = getWorkflowIntegrator();
        List<EventRuleImplementation> impList = new ArrayList<>();
        List<Integer> impIDs = wi.rules_getEventRuleImplementationList(erg);
        if(impIDs != null && !impIDs.isEmpty()){
            for(Integer i: impIDs){
                impList.add(wi.rules_getEventRuleImplemention(i));
            }
        }
        return impList;
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

   
    /**
     * Returns complete  dump of the eventrule table for configuration purposes
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

    /**
     * Internal method for taking in an ERA and creating an Implemented Event Rule
     * on an Occuapancy Period
     * @param era
     * @param period
     * @param usr
     * @throws IntegrationException 
     */
    private void rules_attachEventRuleAbstractToOccPeriod(  EventRuleAbstract era, 
                                                            OccPeriodDataHeavy period, 
                                                            UserAuthorized usr) 
                                                throws      IntegrationException {
        
    }
    
    private int checkForExistingERImp(EventRuleAbstract era, IFace_EventRuleGoverned erg){
        List<EventRuleImplementation> erimpList = erg.assembleEventRuleList(ViewOptionsEventRulesEnum.VIEW_ALL);
        int existingERImpID = 0;
        if(erimpList != null && !erimpList.isEmpty()){
            for(EventRuleImplementation erimp: erimpList){
                if(erimp.getRuleid() == era.getRuleid()){
                    existingERImpID = erimp.getImplementationID();
                }
            }
        }
        return existingERImpID;
    }

    /**
     * Attaches a single event rule to an EventRuleGoverned entity, the type of which is determined
     * internally with instanceof checks for OccPeriod and CECaseDataHeavy Objects
     *
     * @param era
     * @param erg
     * @param usr
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException if an IFaceEventRuleGoverned instances is neither a CECaseDataHeavy or an OccPeriod
     */
    public void rules_attachEventRule(EventRuleAbstract era, IFace_EventRuleGoverned erg, UserAuthorized usr) throws IntegrationException, BObStatusException {
        int freshObjectID = 0;
        WorkflowIntegrator wi = getWorkflowIntegrator();
        EventRuleImplementation erimpl = new EventRuleImplementation(era);
        // avoid inserting and duplicating keys
        int exer = checkForExistingERImp(era, erg);
        if (exer != 0) {
            erimpl.setImplementationTS(LocalDateTime.now());
            erimpl.setLastEvaluatedTS(null);
            erimpl.setPassedRuleTS(null);
        } else {
            throw new BObStatusException("Cannot attach Event Rule because it's already implemented in Impl ID " + String.valueOf(exer));
        }
                
        if (erg instanceof OccPeriodDataHeavy) {
            erimpl.setOccPeriodID(erg.getBObID());
        } else if (erg instanceof CECaseDataHeavy) {
            erimpl.setCeCaseID(erg.getBObID());
        } else {
            throw new BObStatusException("No valid instance of EventRuleGoverned found");
        }
        
        // send down to integrator
        wi.implementEventRule(erimpl, erg);
        
        // EventRules can be added to the case as a result of a choice
        // that is proposed to the user by a Proposal which itself
        // is attached to the BOb through the implementation of a directive
        if (era.getPromptingDirective() != null) {
            implementDirective(era.getPromptingDirective(), erg,  null);
            System.out.println("EventCoordinator.rules_attachEventRulAbstractToOccPeriod | directive implemented with ID " + era.getPromptingDirective().getDirectiveID());
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

    
    /**
     * Logic pass through method for including a given EventRuleAbstract to the
     * RuleSet indicated in the RuleSet inside the given OccPeriod
     * 
     * @param era
     * @param period the OccPeriod whose type is mapped to the desired
     * RuleSet in which the client wishes to attach the given EventRuleAbstract
     * @throws IntegrationException 
     */
    public void rules_attachEventRuleAbstractToOccPeriodTypeRuleSet(EventRuleAbstract era, OccPeriod period) throws IntegrationException {
        WorkflowIntegrator wi = getWorkflowIntegrator();
        wi.rules_addEventRuleAbstractToRuleSet(era, period.getType().getBaseRuleSetID());
    }

    /**
     * Generator method for EventRuleAbstracts; sets sensible initial values
     * @return an ERA ready for user config and then insertion in the DB
     */
    public EventRuleAbstract rules_getInitializedEventRuleAbstract() {
        EventRuleAbstract era = new EventRuleAbstract();
        era.setActiveRuleAbstract(true);
        return era;
    }

  
    /**
     * Primary entrance point for an EventRuleAbstract instance (not its connection to an Object)
     * This method will check to see if the int value on the ERA is nonzero, if so
     * and the DB has a record of that value present, the object Directive will
     * be replaced with the one fetched by ID
     * @param era required instance
     * @param ua
     * @return
     * @throws IntegrationException
     */
    public int rules_createEventRuleAbstract(EventRuleAbstract era, UserAuthorized ua) throws IntegrationException {
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
    
        System.out.println("EventCoordinator.rules_createEventRuleAbstract | returned ID: " + freshEventRuleID);
        return freshEventRuleID;
    }
    
}