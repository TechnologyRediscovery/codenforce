/*
 * Copyright (C) 2021 Technology Rediscovery LLC
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
package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.application.interfaces.IFace_EventRuleGoverned;

import com.tcvcog.tcvce.entities.*;

import com.tcvcog.tcvce.util.viewoptions.ViewOptionsEventRulesEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsProposalsEnum;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The Data Intensive subclass of the OccPeriod tree
 * We want to be able to load info about OccPeriods without having to 
 * initialize and configure each and every event and proposal and rule
 * associated with each occ period on each unit on each property
 * that we browse, so we'll only load the DataHeavy version of this
 * Object if we're actually editing that particular OccPeriod
 * @author sylvia
 */
public  class       OccPeriodDataHeavy 
        extends     OccPeriodPropertyUnitHeavy
        implements  IFace_EventRuleGoverned, 
                    IFace_CredentialSigned,
                    IFace_PaymentHolder,
                    IFace_humanListHolder,
                    IFace_BlobHolder,
                    IFace_inspectable{
    
    final static LinkedObjectSchemaEnum HUMAN_LINK_SCHEMA_ENUM = LinkedObjectSchemaEnum.OccPeriodHuman;
    final static BlobLinkEnum BLOB_LINK_ENUM = BlobLinkEnum.OCC_PERIOD;
    final static BlobLinkEnum BLOB_UPSTREAMPOOL_ENUM = BlobLinkEnum.PROPERTY;
    
    protected OccPeriodStatusEnum status;

    private List<OccPermitApplication> applicationList;
    
    protected List<HumanLink> humanLinkList;
    
    private List<Proposal> proposalList;
    private List<EventRuleImplementation> eventRuleList;
    
    private List<FieldInspection> inspectionList;
    
    private List<Integer> blobIDList;
    
    private List<FeeAssigned> feeList;
    private List<Payment> paymentList;
    
    private List<BlobLight> blobList;

    private LocalDateTime configuredTS;
    private String credentialSignature;
    
    public OccPeriodDataHeavy() {
    }
    
    
    /**
     * Populates superclass members and stamps the 
     * authorizing Credential's signature
     * 
     * @param otherPeriodLight
     * @param cred 
     */
    public OccPeriodDataHeavy(OccPeriod otherPeriodLight, Credential cred) {
        super(otherPeriodLight);

        this.credentialSignature = cred.getSignature();
    }

    /**
     * Populates superclass members and stamps the
     * authorizing Credential's signature
     * This one is for a superclass with property unit info, though.
     *
     * @param otherPeriodLighter
     * @param cred
     */
    public OccPeriodDataHeavy(OccPeriodPropertyUnitHeavy otherPeriodLighter, Credential cred) {
        super(otherPeriodLighter);

        this.credentialSignature = cred.getSignature();
    }

    /**
     * Complete copy of another OccPeriodDataHeavy, including credential signature
     *
     * @param otherPeriod
     */
    public OccPeriodDataHeavy(OccPeriodDataHeavy otherPeriod) {
        super(otherPeriod);

        this.status = otherPeriod.status;

        this.applicationList = otherPeriod.applicationList;

        this.proposalList = otherPeriod.proposalList;
        this.eventRuleList = otherPeriod.eventRuleList;

        this.inspectionList = otherPeriod.inspectionList;
        this.permitList = otherPeriod.permitList;

        this.blobIDList = otherPeriod.blobIDList;

        this.feeList = otherPeriod.feeList;
        this.paymentList = otherPeriod.paymentList;

        this.configuredTS = otherPeriod.configuredTS;
        this.credentialSignature = otherPeriod.credentialSignature;
    }

    /**
     * Pre-credential requiring method for creating detailed subclass
     *
     * @param otherPeriodLight
     */
    public OccPeriodDataHeavy(OccPeriod otherPeriodLight) {
        super(otherPeriodLight);
    }

    
    @Override
    public DomainEnum discloseEventDomain() {
        return DomainEnum.OCCUPANCY;
    }

    
    @Override
    public void setEventRuleList(List<EventRuleImplementation> lst) {
        eventRuleList = lst;
    }
    
    
    @Override
    public boolean isAllRulesPassed() {
        boolean allPassed = true;
        for(EventRuleImplementation er: eventRuleList){
            if(er.getPassedRuleTS() == null){
                allPassed = false;
                break;
            }
        }
        return allPassed;
    }

    

    @Override
    public List assembleEventRuleList(ViewOptionsEventRulesEnum voere) {
        List<EventRuleImplementation> evRuleList = new ArrayList<>();
        if (eventRuleList != null) {
            for (EventRuleImplementation eri : eventRuleList) {
                switch (voere) {
                    case VIEW_ACTIVE_NOT_PASSED:
                        if (eri.isActiveRuleAbstract() && eri.getPassedRuleTS() == null) {
                            evRuleList.add(eri);
                        }
                        break;
                    case VIEW_ACTIVE_PASSED:
                        if (eri.isActiveRuleAbstract() && eri.getPassedRuleTS() != null) {
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
    public List assembleProposalList(ViewOptionsProposalsEnum vope) {
        List<Proposal> proposalListVisible = new ArrayList<>();
        if (proposalList != null && !proposalList.isEmpty()) {
            for (Proposal p : proposalList) {
                switch (vope) {
                    case VIEW_ALL:
                        proposalListVisible.add(p);
                        break;
                    case VIEW_ACTIVE_HIDDEN:
                        if (p.isActive() && p.isHidden()) {
                            proposalListVisible.add(p);
                        }
                        break;
                    case VIEW_ACTIVE_NOTHIDDEN:
                        if (p.isActive() && !p.isHidden() && !p.getDirective().isRefuseToBeHidden()) {
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
     * @return the paymentList
     */
    @Override
    public List<Payment> getPaymentList() {
        return paymentList;
    }

    /**
     * @param paymentList the paymentList to set
     */
    @Override
    public void setPaymentList(List<Payment> paymentList) {
        this.paymentList = paymentList;
    }
    
    /**
     * Takes the general Payment type and converts it to 
     * @param paymentList the paymentList to set
     */
    @Override
    public void setPaymentListGeneral(List<Payment> paymentList) {
        List<Payment> skeletonHorde = new ArrayList<>();
        
        for (Payment p : paymentList) {
            
            skeletonHorde.add(new Payment(p));
            
        }
        
        this.paymentList = skeletonHorde;
    }

    /**
     * @return the applicationList
     */
    public List<OccPermitApplication> getApplicationList() {
        return applicationList;
    }
    
        /**
     * @param applicationList the applicationList to set
     */
    public void setApplicationList(List<OccPermitApplication> applicationList) {
        this.applicationList = applicationList;
    }


    /**
     * @return the proposalList
     */
    public List<Proposal> getProposalList() {
        return proposalList;
    }

    /**
     * @return the inspectionList
     */
    public List<FieldInspection> getInspectionList() {
        return inspectionList;
    }
    /**
     * @return the permitList
     */

    /**
     * @return the blobIDList
     */
    public List<Integer> getBlobIDList() {
        return blobIDList;
    }

    /**
     * @param personListApplicants the personListApplicants to set
     */
    public void setPersonListApplicants(List<HumanLink> personListApplicants) {
        this.humanLinkList = personListApplicants;
    }

    /**
     * @param proposalList the proposalList to set
     */
    @Override
    public void setProposalList(List<Proposal> proposalList) {
        this.proposalList = proposalList;
    }

    /**
     * @param inspectionList the inspectionList to set
     */
    public void setInspectionList(List<FieldInspection> inspectionList) {
        this.inspectionList = inspectionList;
    }
    /**
     * @param permitList the permitList to set
     */

    /**
     * @param blobIDList the blobIDList to set
     */
    public void setBlobIDList(List<Integer> blobIDList) {
        this.blobIDList = blobIDList;
    }

    /**
     * @return the configuredTS
     */
    public LocalDateTime getConfiguredTS() {
        return configuredTS;
    }

    /**
     * @param configuredTS the configuredTS to set
     */
    public void setConfiguredTS(LocalDateTime configuredTS) {
        this.configuredTS = configuredTS;
    }

    /**
     * @return the feeList
     */
    public List<FeeAssigned> getFeeList() {
        return feeList;
    }

    /**
     * @param feeList the feeList to set
     */
    public void setFeeList(List<FeeAssigned> feeList) {
        this.feeList = feeList;
    }

    /**
     * @return the credentialSignature
     */
    @Override
    public String getCredentialSignature() {
        return credentialSignature;
    }

    /**
     * @param credentialSignature the credentialSignature to set
     */
    public void setCredentialSignature(String credentialSignature) {
        this.credentialSignature = credentialSignature;
    }

    /**
     * @return the status
     */
    public OccPeriodStatusEnum getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(OccPeriodStatusEnum status) {
        this.status = status;
    }

  

    @Override
    public boolean isOpen() {
        if(status != null){
            return status.isOpenPeriod();
        } else {
            return false;
        }
        
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
        return periodID;
    }

    @Override
    public void setBlobList(List<BlobLight> bl) {
        this.blobList = bl;
    }

    @Override
    public List<BlobLight> getBlobList() {
        return blobList;
    }

    @Override
    public BlobLinkEnum getBlobLinkEnum() {
        return BLOB_LINK_ENUM;
    }

    @Override
    public int getParentObjectID() {
        return periodID;
    }

    @Override
    public BlobLinkEnum getBlobUpstreamPoolEnum() {
        return BLOB_UPSTREAMPOOL_ENUM;
    }

    /**
     * I send back the parcel key of the containing property
     * so I can use its poool of Blobs, if I want
     * @return 
     */
    @Override
    public int getBlobUpstreamPoolEnumPoolFeederID() {
        return this.getPropUnitProp().getParcelKey();
    }

    @Override
    public DomainEnum getDomainEnum() {
        return OCC_DOMAIN;
    }

    @Override
    public boolean isNewInspectionsAllowed() {
        return authorizedTS == null;
    }

   
    
}
