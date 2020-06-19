/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.util.ArrayList;
import java.util.List;
import com.tcvcog.tcvce.application.interfaces.IFace_EventRuleGoverned;
import com.tcvcog.tcvce.application.interfaces.IFace_Loggable;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsEventRulesEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsProposalsEnum;

/**
 *
 * @author Ellen Bascomb
 */
public class CECaseDataHeavy
        extends CECasePropertyUnitHeavy
        implements Cloneable,
        IFace_EventRuleGoverned,
        IFace_CredentialSigned,
        IFace_Loggable {

    // accessed through methods specified in the interfaces
    private List<Proposal> proposalList;
    private List<EventRuleImplementation> eventRuleList;

    private List<CEActionRequest> ceActionRequestList;

    private List<MoneyCECaseFeeAssigned> feeList;
    private List<MoneyCECaseFeePayment> paymentList;

    private String credentialSignature;

    /**
     * Constructor used to create an instance of this object with a CECase
     * without any lists. Transfers the member variables from the incoming
     * object to this sublcass
     *
     * ** CONSTRUCTORS ARE NOT INHERITED! ** but member variables and methods
     * sure are!
     *
     * @param cse
     */
    public CECaseDataHeavy(CECasePropertyUnitHeavy cse) {
        super(cse);
        this.property = cse.property;
        this.propUnit = cse.propUnit;
        this.showHiddenEvents = cse.isShowHiddenEvents();
        this.showInactiveEvents = cse.isShowInactiveEvents();
        this.eventList = cse.getEventList();
        this.completeEventList = cse.getCompleteEventList();
    }

    @Override
    public String getCredentialSignature() {
        return credentialSignature;
    }

    /**
     *
     * @return @throws CloneNotSupportedException
     */
    @Override
    public CECaseDataHeavy clone() throws CloneNotSupportedException {
        super.clone();
        return null;
    }

    @Override
    public List<EventCnF> assembleEventList(ViewOptionsActiveHiddenListsEnum voahle) {
        List<EventCnF> visEventList = new ArrayList<>();
        if (eventList != null) {
            for (EventCnF ev : eventList) {
                switch (voahle) {
                    case VIEW_ACTIVE_HIDDEN:
                        if (ev.isActive()
                                && ev.isHidden()) {
                            visEventList.add(ev);
                        }
                        break;
                    case VIEW_ACTIVE_NOTHIDDEN:
                        if (ev.isActive()
                                && !ev.isHidden()) {
                            visEventList.add(ev);
                        }
                        break;
                    case VIEW_ALL:
                        visEventList.add(ev);
                        break;
                    case VIEW_INACTIVE:
                        if (!ev.isActive()) {
                            visEventList.add(ev);
                        }
                        break;
                    default:
                        visEventList.add(ev);
                } // close switch
            } // close for   
        } // close null check
        return visEventList;
    }

    @Override
    public List<EventRuleImplementation> assembleEventRuleList(ViewOptionsEventRulesEnum voere) {
        List<EventRuleImplementation> evRuleList = new ArrayList<>();
        if (eventRuleList != null) {
            for (EventRuleImplementation eri : eventRuleList) {
                switch (voere) {
                    case VIEW_ACTIVE_NOT_PASSED:
                        if (eri.isActiveRuleAbstract()
                                && eri.getPassedRuleTS() == null) {
                            evRuleList.add(eri);
                        }
                        break;
                    case VIEW_ACTIVE_PASSED:
                        if (eri.isActiveRuleAbstract()
                                && eri.getPassedRuleTS() != null) {
                            evRuleList.add(eri);
                        }
                        break;
                    case VIEW_ALL:
                        evRuleList.add(eri);
                        break;
                    case VIEW_INACTIVE:
                        if (!eri.isActiveRuleAbstract()) {
                            evRuleList.add(eri);
                        }
                        break;
                    default:
                        evRuleList.add(eri);
                } // close switch
            } // close loop
        } // close null check
        return evRuleList;
    }

    @Override
    public boolean isAllRulesPassed() {
        boolean allPassed = true;
        for (EventRuleImplementation er : eventRuleList) {
            if (er.getPassedRuleTS() == null) {
                allPassed = false;
                break;
            }
        }
        return allPassed;
    }

    @Override
    public List<Proposal> assembleProposalList(ViewOptionsProposalsEnum vope) {
        List<Proposal> proposalListVisible = new ArrayList<>();
        if (proposalList != null && !proposalList.isEmpty()) {
            for (Proposal p : proposalList) {
                switch (vope) {
                    case VIEW_ALL:
                        proposalListVisible.add(p);
                        break;
                    case VIEW_ACTIVE_HIDDEN:
                        if (p.isActive()
                                && p.isHidden()) {
                            proposalListVisible.add(p);
                        }
                        break;
                    case VIEW_ACTIVE_NOTHIDDEN:
                        if (p.isActive()
                                && !p.isHidden()
                                && !p.getDirective().isRefuseToBeHidden()) {
                            proposalListVisible.add(p);
                        }
                        break;
                    case VIEW_EVALUATED:
                        if (p.getResponseTS() != null) {
                            proposalListVisible.add(p);
                        }
                        break;
                    case VIEW_INACTIVE:
                        if (!p.isActive()) {
                            proposalListVisible.add(p);
                        }
                        break;
                    case VIEW_NOT_EVALUATED:
                        if (p.getResponseTS() == null) {
                            proposalListVisible.add(p);
                        }
                        break;
                    default:
                        proposalListVisible.add(p);
                } // switch
            } // for
        } // if
        return proposalListVisible;
    }

    /**
     * @param eventRuleList the eventRuleList to set
     */
    @Override
    public void setEventRuleList(List<EventRuleImplementation> eventRuleList) {
        this.eventRuleList = eventRuleList;
    }

    /**
     * @return the ceActionRequestList
     */
    public List<CEActionRequest> getCeActionRequestList() {
        return ceActionRequestList;
    }

    /**
     * @param ceActionRequestList the ceActionRequestList to set
     */
    public void setCeActionRequestList(List<CEActionRequest> ceActionRequestList) {
        this.ceActionRequestList = ceActionRequestList;
    }

    /**
     * @return the violationListUnresolved
     */
    public List<CodeViolation> getViolationListUnresolved() {

        List<CodeViolation> violationListUnresolved = new ArrayList<>();
        if (violationList != null && violationList.size() > 0) {
            for (CodeViolation v : violationList) {
                if (v.getActualComplianceDate() == null) {
                    violationListUnresolved.add(v);
                }
            }
        }

        return violationListUnresolved;
    }

    /**
     * @return the violationListResolved
     */
    public List<CodeViolation> getViolationListResolved() {
        List<CodeViolation> violationListResolved = new ArrayList<>();
        if (violationList != null && violationList.size() > 0) {
            for (CodeViolation v : violationList) {
                if (v.getActualComplianceDate() != null) {
                    violationListResolved.add(v);
                }
            }
        }

        return violationListResolved;
    }

    /**
     * @return the proposalList
     */
    public List<Proposal> getProposalList() {
        return proposalList;
    }

    /**
     * @param proposalList the proposalList to set
     */
    @Override
    public void setProposalList(List<Proposal> proposalList) {
        this.proposalList = proposalList;
    }

    /**
     * @return the feeList
     */
    public List<MoneyCECaseFeeAssigned> getFeeList() {
        return feeList;
    }

    /**
     * @return the paymentList
     */
    public List<MoneyCECaseFeePayment> getPaymentList() {
        return paymentList;
    }

    /**
     * @param feeList the feeList to set
     */
    public void setFeeList(List<MoneyCECaseFeeAssigned> feeList) {
        this.feeList = feeList;
    }

    /**
     * @param paymentList the paymentList to set
     */
    public void setPaymentList(List<MoneyCECaseFeePayment> paymentList) {
        this.paymentList = paymentList;
    }

    /**
     * Takes the general Payment type and converts it to
     *
     * @param paymentList the paymentList to set
     */
    public void setPaymentListGeneral(List<Payment> paymentList) {
        List<MoneyCECaseFeePayment> skeletonHorde = new ArrayList<>();

        for (Payment p : paymentList) {

            skeletonHorde.add(new MoneyCECaseFeePayment(p));

        }

        this.paymentList = skeletonHorde;
    }

}
