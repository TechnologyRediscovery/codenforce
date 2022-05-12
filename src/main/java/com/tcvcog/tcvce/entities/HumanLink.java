/*
 * Copyright (C) 2020 Technology Rediscovery LLC
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

import java.time.LocalDateTime;
import java.util.List;

/**
 * Attachment metatdata container for Humans connected to any number of BOBs
 * 
 * @author Ellen Bascomb of Apartment 31Y
 */
public class        HumanLink 
        extends     Human
        implements  IFace_trackedEntityLink,
                    IFace_contactable{
    
    protected int linkID;
     
    protected LinkedObjectRole linkRole;
    protected BOBSource linkSource;
    
    protected LocalDateTime linkCreatedTS;
    protected int linkCreatedByUserID;
    
    protected LocalDateTime linkLastUpdatedTS;
    protected int linkLastUpdatedByUserID;
    
    protected LocalDateTime linkDeactivatedTS;
    protected int linkDeactivatedByUserID;
    
    protected String linkNotes;
    
    protected int parentObjectID;
    
    
    // CONTACTABLE STUFF
    protected List<MailingAddressLink> mailingAddressLinkList;
    protected List<ContactEmail> emailList;
    protected List<ContactPhone> phoneList;
    
    private String mailingAddressListPretty;
    private String emailListPretty;
    private String phoneListPretty;
    
    public HumanLink(Human h){
        
        this.humanID = h.humanID;
        this.name = h.name;
        this.dob = h.dob;
        this.under18 = h.under18;
        this.jobTitle = h.jobTitle;
        this.businessEntity = h.businessEntity;
        this.multiHuman = h.multiHuman;
        this.source = h.source;
        this.deceasedDate = h.deceasedDate;
        this.deceasedBy = h.deceasedBy;
        this.cloneOfHumanID = h.cloneOfHumanID;
        this.notes = h.notes;
        
        this.createdTS = h.createdTS;
        this.createdBy = h.createdBy;
        this.lastUpdatedTS = h.lastUpdatedTS;
        this.lastUpdatedBy = h.lastUpdatedBy;
        this.deactivatedTS = h.deactivatedTS;
        this.deactivatedBy = h.deactivatedBy;
    }
    
     /**
     * @param humanID the humanID to set
     */
    @Override
    public void setHumanID(int humanID) {
        this.humanID = humanID;
    }

    /**
     * @return the linkCreatedTS
     */
    @Override
    public LocalDateTime getLinkCreatedTS() {
        return linkCreatedTS;
    }

    /**
     * @return the linkCreatedByUserID
     */
    @Override
    public int getLinkCreatedByUserID() {
        return linkCreatedByUserID;
    }

    /**
     * @return the linkLastUpdatedTS
     */
    @Override
    public LocalDateTime getLinkLastUpdatedTS() {
        return linkLastUpdatedTS;
    }

    /**
     * @return the linkLastUpdatedByUserID
     */
    @Override
    public int getLinkLastUpdatedByUserID() {
        return linkLastUpdatedByUserID;
    }

    /**
     * @return the linkDeactivatedTS
     */
    @Override
    public LocalDateTime getLinkDeactivatedTS() {
        return linkDeactivatedTS;
    }

    /**
     * @return the linkDeactivatedByUserID
     */
    @Override
    public int getLinkDeactivatedByUserID() {
        return linkDeactivatedByUserID;
    }

    /**
     * @return the linkNotes
     */
    @Override
    public String getLinkNotes() {
        return linkNotes;
    }

    /**
     * @param linkCreatedBy the linkCreatedByUserID to set
     */
    @Override
    public void setLinkCreatedByUserID(int linkCreatedBy) {
        this.linkCreatedByUserID = linkCreatedBy;
    }

    /**
     * @param linkLastUpdatedBy the linkLastUpdatedByUserID to set
     */
    @Override
    public void setLinkLastUpdatedByUserID(int linkLastUpdatedBy) {
        this.linkLastUpdatedByUserID = linkLastUpdatedBy;
    }

    /**
     * @param linkDeactivatedBy the linkDeactivatedByUserID to set
     */
    @Override
    public void setLinkDeactivatedByUserID(int linkDeactivatedBy) {
        this.linkDeactivatedByUserID = linkDeactivatedBy;
    }

    /**
     * @param linkNotes the linkNotes to set
     */
    @Override
    public void setLinkNotes(String linkNotes) {
        this.linkNotes = linkNotes;
    }

    @Override
    public boolean isLinkDeactivated() {
        return linkDeactivatedTS != null;
    }

 
    

    
    /**
     * @param linkCreatedTS the linkCreatedTS to set
     */
    @Override
    public void setLinkCreatedTS(LocalDateTime linkCreatedTS) {
        this.linkCreatedTS = linkCreatedTS;
    }

    /**
     * @param linkLastUpdatedTS the linkLastUpdatedTS to set
     */
    @Override
    public void setLinkLastUpdatedTS(LocalDateTime linkLastUpdatedTS) {
        this.linkLastUpdatedTS = linkLastUpdatedTS;
    }

    /**
     * @param linkDeactivatedTS the linkDeactivatedTS to set
     */
    @Override
    public void setLinkDeactivatedTS(LocalDateTime linkDeactivatedTS) {
        this.linkDeactivatedTS = linkDeactivatedTS;
    }

  

    @Override
    public LinkedObjectRole getLinkedObjectRole() {
        return linkRole;
    }

    @Override
    public void setLinkedObjectRole(LinkedObjectRole lor){
        linkRole = lor;
    }
    
    /**
     * @param linkRole the linkRole to set
     */
    public void setLinkRole(LinkedObjectRole linkRole) {
        this.linkRole = linkRole;
    }

   
    /**
     * @return the linkSource
     */
    @Override
    public BOBSource getLinkSource() {
        return linkSource;
    }

    /**
     * @param linkSource the linkSource to set
     */
    @Override
    public void setLinkSource(BOBSource linkSource) {
        this.linkSource = linkSource;
    }

    /**
     * @return the linkID
     */
    public int getLinkID() {
        return linkID;
    }

    /**
     * @param linkID the linkID to set
     */
    public void setLinkID(int linkID) {
        this.linkID = linkID;
    }


    @Override
    public int getParentObjectID() {
        return parentObjectID;
    }

    public void setParentObjectID(int pid){
        parentObjectID = pid;
    }

    @Override
    public List<ContactEmail> getEmailList() {
        return emailList;
    }

    @Override
    public List<ContactPhone> getPhoneList() {
        return phoneList;
    }

    @Override
    public String getMailingAddressListPretty() {
        return mailingAddressListPretty;
    }

    @Override
    public String getPhoneListPretty() {
        return phoneListPretty;
    }

    @Override
    public String getEmailListPretty() {
        return emailListPretty;
    }

    @Override
    public void setEmailList(List<ContactEmail> eml) {
        emailList = eml;
    }

    @Override
    public void setPhoneList(List<ContactPhone> phl) {
        phoneList = phl;
    }

    @Override
    public List<MailingAddressLink> getMailingAddressLinkList() {
        return mailingAddressLinkList;
    }

    @Override
    public void setMailingAddressLinkList(List<MailingAddressLink> ll) {
        mailingAddressLinkList = ll;
    }

    @Override
    public LinkedObjectSchemaEnum getLinkedObjectSchemaEnum() {
        return linkRole.schema;
    }

    @Override
    public int getTargetObjectPK() {
        return humanID;
    }

    /**
     * @param mailingAddressListPretty the mailingAddressListPretty to set
     */
    @Override
    public void setMailingAddressListPretty(String mailingAddressListPretty) {
        this.mailingAddressListPretty = mailingAddressListPretty;
    }

    /**
     * @param emailListPretty the emailListPretty to set
     */
    @Override
    public void setEmailListPretty(String emailListPretty) {
        this.emailListPretty = emailListPretty;
    }

    /**
     * @param phoneListPretty the phoneListPretty to set
     */
    @Override
    public void setPhoneListPretty(String phoneListPretty) {
        this.phoneListPretty = phoneListPretty;
    }
   
}
