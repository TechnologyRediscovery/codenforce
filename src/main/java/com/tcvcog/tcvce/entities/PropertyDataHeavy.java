/*
 * Copyright (C) 2018 Turtle Creek Valley
Council of Governments, PA
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
package com.tcvcog.tcvce.entities;

import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsProposalsEnum;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author ellen bascomb of apartment 31Y
 */
public  class       PropertyDataHeavy 
        extends     Property 
        implements  IFace_CredentialSigned{
    
    
    private List<CECasePropertyUnitHeavy> ceCaseList;
    
    private List<PropertyUnitDataHeavy> unitWithListsList;
    
    private List<Person> personList;
    
    private List<CECaseDataHeavy> propInfoCaseList;
    
    private List<Integer> blobList;
    
    private String credentialSignature;
    
    private List<PropertyExtData> extDataList;
    
    public PropertyDataHeavy(){
        
    }
    
    /**
     * Theoretically we store the credential signature of the user who
     * retrieves any data heavy bob, but for now, this is in holding
     * @param prop
     * @param cred 
     */
    public PropertyDataHeavy(Property prop, Credential cred){
        
        this.credentialSignature = cred.getSignature();
           
        this.propertyID = prop.getPropertyID();
        this.muni = prop.getMuni();
        this.muniCode = prop.getMuniCode();
        this.parID = prop.getParID();
        this.lotAndBlock = prop.getLotAndBlock();
        this.address = prop.getAddress();

        this.useType = prop.getUseType();      
        this.useGroup = prop.getUseGroup();
        this.constructionType = prop.getConstructionType();
        this.countyCode = prop.getCountyCode();

        this.notes = prop.getNotes();
        this.address_city = prop.getAddress_city();
        this.address_state = prop.getAddress_state();
        this.address_zip = prop.address_zip;

        this.ownerCode = prop.getOwnerCode();
        this.propclass = prop.getPropclass();

        this.lastUpdatedTS = prop.getLastUpdatedTS();
        this.lastUpdatedBy = prop.getLastUpdatedBy();
        this.locationDescriptor = prop.getLocationDescriptor();

        this.bobSource = prop.getBobSource();
        this.unfitDateStart = prop.getUnfitDateStart();
        this.unfitDateStop = prop.getUnfitDateStop();
        this.unfitBy = prop.getUnfitBy();

        this.abandonedDateStart = prop.getAbandonedDateStart();
        this.abandonedDateStop = prop.getAbandonedDateStop();
        this.abandonedBy = prop.getAbandonedBy();
        this.vacantDateStart = prop.getVacantDateStart();

        this.vacantDateStop = prop.getVacantDateStop();
        this.vacantBy = prop.getVacantBy();
        this.condition = prop.getCondition();

        this.landBankProspect = prop.getLandBankProspect();
        this.LandBankHeld = prop.isLandBankHeld();
        this.active = prop.isActive();
        this.nonAddressable = prop.isNonAddressable();
        
        this.creationTS = prop.getCreationTS();
        
        
    }
    

    public PropertyDataHeavy(Property prop){
        this.propertyID = prop.getPropertyID();
        this.muni = prop.getMuni();
        this.muniCode = prop.getMuniCode();
        this.parID = prop.getParID();
        this.lotAndBlock = prop.getLotAndBlock();
        this.address = prop.getAddress();

        this.useType = prop.getUseType();
        this.useGroup = prop.getUseGroup();
        this.constructionType = prop.getConstructionType();
        this.countyCode = prop.getCountyCode();

        this.notes = prop.getNotes();
        this.address_city = prop.getAddress_city();
        this.address_state = prop.getAddress_state();
        this.address_zip = prop.address_zip;

        this.ownerCode = prop.getOwnerCode();
        this.propclass = prop.getPropclass();

        this.lastUpdatedTS = prop.getLastUpdatedTS();
        this.lastUpdatedBy = prop.getLastUpdatedBy();
        this.locationDescriptor = prop.getLocationDescriptor();

        this.bobSource = prop.getBobSource();
        this.unfitDateStart = prop.getUnfitDateStart();
        this.unfitDateStop = prop.getUnfitDateStop();
        this.unfitBy = prop.getUnfitBy();

        this.abandonedDateStart = prop.getAbandonedDateStart();
        this.abandonedDateStop = prop.getAbandonedDateStop();
        this.abandonedBy = prop.getAbandonedBy();
        this.vacantDateStart = prop.getVacantDateStart();

        this.vacantDateStop = prop.getVacantDateStop();
        this.vacantBy = prop.getVacantBy();
        this.condition = prop.getCondition();

        this.landBankProspect = prop.getLandBankProspect();
        this.LandBankHeld = prop.isLandBankHeld();
        this.active = prop.isActive();
        this.nonAddressable = prop.isNonAddressable();
        
        this.unitList = prop.getUnitList();
        this.creationTS = prop.getCreationTS();
        
        
    }
    
    /**
     * @return the credentialSignature
     */
    @Override
    public String getCredentialSignature() {
        return credentialSignature;
    }

    public List<OccPeriod> getCompletePeriodList(){
        List<OccPeriod> perList = new ArrayList<>();
        if(unitWithListsList != null && !unitWithListsList.isEmpty()){
            for(PropertyUnitDataHeavy pudh: unitWithListsList){
                if(pudh.getPeriodList() != null && !pudh.getPeriodList().isEmpty()){
                    perList.addAll(pudh.getPeriodList());
                }
            }
        }
        
        return perList;
        
    }
    
    /**
     * Extracts proposals from all PropertyInfo cases attached to property
     * @param vope
     * @return 
     */
    public List assembleProposalList(ViewOptionsProposalsEnum vope) {
        List<Proposal> proposalList = new ArrayList<>();
        List<Proposal> proposalListVisible = new ArrayList<>();
        if(propInfoCaseList != null && !propInfoCaseList.isEmpty()){
            for(CECaseDataHeavy cse: propInfoCaseList){
                proposalList.addAll(cse.assembleProposalList(ViewOptionsProposalsEnum.VIEW_ALL));
            }
        }
        
        
        if (!proposalList.isEmpty()) {
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
    
    public List<EventCnF> getCompleteEventList(){
        List<EventCnF> evList = new ArrayList<>();
        
        if(propInfoCaseList != null && !propInfoCaseList.isEmpty()){
            for(CECaseDataHeavy cdh: propInfoCaseList){
                evList.addAll(cdh.getEventList(ViewOptionsActiveHiddenListsEnum.VIEW_ALL));
            }
        }
        return evList;
    }
    
    
    /**
     * @return the ceCaseList
     */
    public List<CECasePropertyUnitHeavy> getCeCaseList() {
        return ceCaseList;
    }


    /**
     * @return the personList
     */
    public List<Person> getPersonList() {
        return personList;
    }

    /**
     * @param ceCaseList the ceCaseList to set
     */
    public void setCeCaseList(List<CECasePropertyUnitHeavy> ceCaseList) {
        this.ceCaseList = ceCaseList;
    }


    /**
     * @param personList the personList to set
     */
    public void setPersonList(List<Person> personList) {
        this.personList = personList;
    }


    /**
     * @param propInfoCaseList the propInfoCaseList to set
     */
    public void setPropInfoCaseList(List<CECaseDataHeavy> propInfoCaseList) {
        this.propInfoCaseList = propInfoCaseList;
    }

    /**
     * @return the blobList
     */
    public List getBlobList() {
        return blobList;
    }

    /**
     * @param blobList the blobList to set
     */
    public void setBlobList(List<Integer> blobList) {
        this.blobList = blobList;
    }

    /**
     * @return the unitWithListsList
     */
    public List<PropertyUnitDataHeavy> getUnitWithListsList() {
        return unitWithListsList;
    }

    /**
     * @param unitWithListsList the unitWithListsList to set
     */
    public void setUnitWithListsList(List<PropertyUnitDataHeavy> unitWithListsList) {
        this.unitWithListsList = unitWithListsList;
    }

    /**
     * @return the propInfoCaseList
     */
    public List<CECaseDataHeavy> getPropInfoCaseList() {
        return propInfoCaseList;
    }

    /**
     * @return the extDataList
     */
    public List<PropertyExtData> getExtDataList() {
        return extDataList;
    }

    /**
     * @param extDataList the extDataList to set
     */
    public void setExtDataList(List<PropertyExtData> extDataList) {
        this.extDataList = extDataList;
    }


  
    
}
