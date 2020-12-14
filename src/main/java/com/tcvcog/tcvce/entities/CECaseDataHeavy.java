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
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsEventRulesEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsProposalsEnum;

/**
 *  Listified CECase object 
 * 
 * @author Ellen Bascomb (Apartment 31Y)
 */
public class CECaseDataHeavy
        extends CECase
        implements Cloneable,
        IFace_EventRuleGoverned,
        IFace_CredentialSigned,
        IFace_Loggable {

    // accessed through methods specified in the interfaces
    private Property property;
    private PropertyUnit propertyUnit;
    
    
    private List<Proposal> proposalList;
    private List<EventRuleImplementation> eventRuleList;

    private List<CEActionRequest> ceActionRequestList;

    private List<MoneyCECaseFeeAssigned> feeList;
    private List<MoneyCECaseFeePayment> paymentList;
    
    private List<Integer> blobIDList;
    
   
    public CECaseDataHeavy(CECase cse){
        if(cse != null){
            
        this.caseID = cse.caseID;
        this.publicControlCode = cse.publicControlCode;
        this.paccEnabled = cse.paccEnabled;

        this.allowForwardLinkedPublicAccess = cse.allowForwardLinkedPublicAccess;

        this.propertyID = cse.propertyID;
        this.propertyUnitID = cse.propertyUnitID;

        this.caseManager = cse.caseManager;
        this.caseName = cse.caseName;

        this.originationDate = cse.originationDate;
        this.closingDate = cse.closingDate;
        this.creationTimestamp = cse.creationTimestamp;

        this.notes = cse.notes;

        this.source = cse.source;

        this.citationList = cse.citationList;
        this.noticeList = cse.noticeList;
        this.violationList = cse.violationList;
       
        this.active = cse.active;
        this.propertyInfoCase = cse.propertyInfoCase;
        this.personInfoPersonID = cse.getPersonInfoPersonID();
        
        this.lastUpdatedBy = cse.getLastUpdatedBy();
        this.lastUpdatedTS = cse.getLastUpdatedTS();
        this.statusBundle = cse.getStatusBundle();
        
        this.eventList = cse.eventList;
        this.statusAssignmentLog = cse.statusAssignmentLog;
        
        eventListMaster = new ArrayList<>();
        } else {
            System.out.println("CECaseDataHeavy.const: null input case");
        }
        
    }
    
    

    

   
    
    @Override
    public EventDomainEnum discloseEventDomain() {
        return EventDomainEnum.CODE_ENFORCEMENT;
    }
    
    
    @Override
    public int getBObID() {
        return caseID;
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

    @Override
    public String getCredentialSignature() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return the propertyUnit
     */
    public PropertyUnit getPropertyUnit() {
        return propertyUnit;
    }

    /**
     * @param propertyUnit the propertyUnit to set
     */
    public void setPropertyUnit(PropertyUnit propertyUnit) {
        this.propertyUnit = propertyUnit;
    }

    /**
     * @return the property
     */
    public Property getProperty() {
        return property;
    }

    /**
     * @param property the property to set
     */
    public void setProperty(Property property) {
        this.property = property;
    }

    /**
     * @return the blobIDList
     */
    public List<Integer> getBlobIDList() {
        return blobIDList;
    }

    /**
     * @param blobIDList the blobIDList to set
     */
    public void setBlobIDList(List<Integer> blobIDList) {
        this.blobIDList = blobIDList;
    }

    @Override
    public boolean isOpen() {
        if(statusBundle != null){
            return statusBundle.getPhase().isCaseOpen();
        } 
        return true;
        
    }

    /**
     * @return the ceActionRequestList
     */
    public List<CEActionRequest> getCeActionRequestList() {
        return ceActionRequestList;
    }

}
