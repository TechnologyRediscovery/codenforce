/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import com.tcvcog.tcvce.money.entities.TransactionPayment;
import com.tcvcog.tcvce.money.entities.TransactionCharge;
import java.util.ArrayList;
import java.util.List;
import com.tcvcog.tcvce.application.interfaces.IFace_EventRuleGoverned;
import com.tcvcog.tcvce.application.interfaces.IFace_Loggable;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsEventRulesEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsProposalsEnum;
import com.tcvcog.tcvce.application.interfaces.IFace_ActivatableBOB;
import com.tcvcog.tcvce.entities.occupancy.FieldInspection;

/**
 *  Listified CECase object 
 * 
 * @author Ellen Bascomb (Apartment 31Y)
 */
public class CECaseDataHeavy
        extends CECase
        implements  Cloneable,
                    IFace_EventRuleGoverned,
                    IFace_CredentialSigned,
                    IFace_Loggable,
                    IFace_ActivatableBOB,
                    IFace_BlobHolder,
                    IFace_humanListHolder,
                    IFace_inspectable{

    // accessed through methods specified in the interfaces
    final static LinkedObjectSchemaEnum HUMAN_LINK_SCHEMA_ENUM = LinkedObjectSchemaEnum.CECaseHuman;
    final static BlobLinkEnum BLOB_LINK_ENUM = BlobLinkEnum.CE_CASE;
    final static BlobLinkEnum BLOB_LINK_UPSTREAM_POOL = BlobLinkEnum.PROPERTY;
    
    
    private Property property;
    private PropertyUnit propertyUnit;
    
    protected List<HumanLink> humanLinkList;
    
    protected List<FieldInspection> inspectionList;
    
    private List<Proposal> proposalList;
    private List<EventRuleImplementation> eventRuleList;

    private List<CEActionRequest> ceActionRequestList;

    private List<TransactionCharge> feeList;
    private List<TransactionPayment> paymentList;
    
    private List<BlobLight> blobList;
    
   
    public CECaseDataHeavy(CECase cse) {
       super(cse);
        
    }
    
    public CECaseDataHeavy(CECaseDataHeavy csedh){
        super(csedh);
        this.property = csedh.property;
        this.propertyUnit = csedh.propertyUnit;
        this.humanLinkList = csedh.humanLinkList;
        this.inspectionList = csedh.inspectionList;
        this.proposalList = csedh.proposalList;
        this.eventRuleList = csedh.eventRuleList;
        this.ceActionRequestList = csedh.ceActionRequestList;
        this.feeList = csedh.feeList;
        this.paymentList = csedh.paymentList;
        this.blobList = csedh.blobList;
    }


    @Override
    public DomainEnum discloseEventDomain() {
        return DomainEnum.CODE_ENFORCEMENT;
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
    public List<TransactionCharge> getFeeList() {
        return feeList;
    }

    /**
     * @param feeList the feeList to set
     */
    public void setFeeList(List<TransactionCharge> feeList) {
        this.feeList = feeList;
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

    /**
     * @return the blobList
     */
    public List<BlobLight> getBlobList() {
        return blobList;
    }

    /**
     * @param blobList the blobList to set
     */
    public void setBlobList(List<BlobLight> blobList) {
        this.blobList = blobList;
    }
        

    @Override
    public List<HumanLink> getHumanLinkList() {
        return humanLinkList;
    }

    @Override
    public void setHumanLinkList(List<HumanLink> hll) {
        humanLinkList = hll;
    }

    @Override
    public LinkedObjectSchemaEnum getHUMAN_LINK_SCHEMA_ENUM() {
        return HUMAN_LINK_SCHEMA_ENUM;
    }

    

    @Override
    public int getHostPK() {
        return caseID;
    }

    @Override
    public BlobLinkEnum getBlobLinkEnum() {
        return BLOB_LINK_ENUM;
    }

    @Override
    public int getParentObjectID() {
        return caseID;
    }

    @Override
    public BlobLinkEnum getBlobUpstreamPoolEnum() {
        return BLOB_LINK_UPSTREAM_POOL;
    }

    @Override
    public int getBlobUpstreamPoolEnumPoolFeederID() {
        return property.parcelKey;
    }

    /**
     * @return the inspectionList
     */
    @Override
    public List<FieldInspection> getInspectionList() {
        return inspectionList;
    }

    /**
     * @param inspectionList the inspectionList to set
     */
    @Override
    public void setInspectionList(List<FieldInspection> inspectionList) {
        this.inspectionList = inspectionList;
    }

    @Override
    public DomainEnum getDomainEnum() {
        return CECASE_ENUM;
    }

    @Override
    public User getManager() {
        return caseManager;
    }

    @Override
    public boolean isNewInspectionsAllowed() {
        return closingDate == null;
    }

}
