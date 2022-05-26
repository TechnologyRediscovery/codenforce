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
package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.entities.CodeSource;
import com.tcvcog.tcvce.entities.HumanLink;
import com.tcvcog.tcvce.entities.ParcelInfo;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.TextBlock;
import com.tcvcog.tcvce.entities.TrackedEntity;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.util.Constants;
import j2html.TagCreator;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class OccPermit extends TrackedEntity {
    
    final static String TABLE_NAME = "occpermit";
    final static String PERMIT_PK = "permitid";
    
    
    public OccPermit(){
        dynamicPopulationLog = new String();
        finalizationAuditLog = new String();
    }
    
    
    private int permitID;
    // used for storing municipality-generated IDs associated with the permit
    private String referenceNo;
    private int periodID;

    private String permitAdditionalText;
    private String notes;
    
    private LocalDateTime finalizedts;
    private User finalizedBy;
    
    private LocalDateTime nullifiedTS;
    private User nullifiedBy;
    
    
    
    
    // ********************************************************************
    // ********************** DYNAMIC FIELDS ******************************
    // ***** USED DURING THE CONFIGURATION OF THE PERMIT AND THESE  *******
    // ***** ARE READ INTO THE STATIC FIELDS BY THE COORDINATOR'S   *******
    // ***** updateOccPermitStaticFields(...) method                *******
    // ********************************************************************
    
    private String dynamicPopulationLog;
    private String finalizationAuditLog;
    
    /**
     * Tacks whatever String is passed in and appends it to the config log
     * @param s 
     */
    public void appendToDynamicPopulationLog(String s){
        if(dynamicPopulationLog != null && s != null){
            StringBuilder sb = new StringBuilder(dynamicPopulationLog);
            sb.append(s);
            sb.append(Constants.FMT_HTML_BREAK);
            dynamicPopulationLog = sb.toString();
            
        }
    }

    /**
     * Clears the dynamic population log
     */
    public void clearDynamicPopulationLog(){
        dynamicPopulationLog = new String();
    }
    
    /**
     * Tacks on input to the finalization audit log
     * @param s 
     */
    public void appendToFinalizationAuditLog(String s){
        if(finalizationAuditLog != null && s != null){
            StringBuilder sb = new StringBuilder(finalizationAuditLog);
            sb.append(s);
            sb.append(Constants.FMT_HTML_BREAK);
            finalizationAuditLog = sb.toString();
        }
        
    }
    
    /**
     * Clears finalization audit log
     */
    public void clearFinalizationAuditLog(){
        finalizationAuditLog = new String();
    }
    
    private LocalDateTime dynamicPopulationReadyForFinalizationTS;
    private LocalDateTime finalizationAuditPassTS;
    
    
    private List<HumanLink> ownerSellerLinkList;
    private List<HumanLink> buyerTenantLinkList;
    private List<HumanLink> managerLinkList;
    private List<HumanLink> tenantLinkList;
    
    private ParcelInfo parcelInfo;
    private User issuingOfficer;
    private List<CodeSource> issuingCodeSourceList;
    
    private List<TextBlock> textBlocks_stipulations;
    private List<TextBlock> textBlocks_notice;
    // both combined for the comments static field
    private List<TextBlock> textBlocks_comments;
    
    
    
    private String text_comments;
    
    private LocalDateTime dynamicDateOfApplication;
    private OccPermitApplication dynamicsDateOfApplicationAppRef;
    
    private LocalDateTime dynamicInitialInspection;
    private FieldInspection dynamicInitialInspectionFINRef;
            
    private LocalDateTime dynamicreinspectiondate;
    private FieldInspection dynamicReInspectionFINRef;
    
    private LocalDateTime dynamicfinalinspection;
    private FieldInspection dynamicFinalInspectionFINRef;
    
    private LocalDateTime dynamicdateofissue;
    
    
    // ********************************************************************
    // ********************** STATIC FIELDS *******************************
    // ********************************************************************
    
    
    private LocalDateTime staticdateofapplication;
    private LocalDateTime staticinitialinspection;
    private LocalDateTime staticreinspectiondate;
    private LocalDateTime staticfinalinspection;
    private LocalDateTime staticdateofissue;
    
    private String statictitle;
    private String staticmuniaddress;
    private String staticpropertyinfo;
    private String staticownerseller;
    
    private String staticcolumnlink;
    private String staticbuyertenant;
    private String staticproposeduse;   // from period type
    private String staticusecode;       // from parcel info
    private String staticconstructiontype;  // from parcelinfo
    
    private String staticpropclass;     // from parcelinfo
    private String staticofficername; // from dynamic field on permit
    private String staticissuedundercodesourceid;   // from chosen code source
    private String staticstipulations;
    
    private String staticcomments;
    private String staticmanager;
    private String statictenants;
    private String staticleaseterm;
    
    private String staticleasestatus;
    private String staticpaymentstatus;
    private String staticnotice;

    
    /**
     * @return the permitID
     */
    public int getPermitID() {
        return permitID;
    }

    /**
     * @return the referenceNo
     */
    public String getReferenceNo() {
        return referenceNo;
    }

   


  

    /**
     * @return the permitAdditionalText
     */
    public String getPermitAdditionalText() {
        return permitAdditionalText;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param permitID the permitID to set
     */
    public void setPermitID(int permitID) {
        this.permitID = permitID;
    }

    /**
     * @param referenceNo the referenceNo to set
     */
    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    
    
   

    /**
     * @param permitAdditionalText the permitAdditionalText to set
     */
    public void setPermitAdditionalText(String permitAdditionalText) {
        this.permitAdditionalText = permitAdditionalText;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }


    /**
     * @return the periodID
     */
    public int getPeriodID() {
        return periodID;
    }

    /**
     * @param periodID the periodID to set
     */
    public void setPeriodID(int periodID) {
        this.periodID = periodID;
    }

   

    /**
     * @return the finalizedts
     */
    public LocalDateTime getFinalizedts() {
        return finalizedts;
    }

    /**
     * @return the staticdateofapplication
     */
    public LocalDateTime getStaticdateofapplication() {
        return staticdateofapplication;
    }

    /**
     * @return the staticinitialinspection
     */
    public LocalDateTime getStaticinitialinspection() {
        return staticinitialinspection;
    }

    /**
     * @return the staticreinspectiondate
     */
    public LocalDateTime getStaticreinspectiondate() {
        return staticreinspectiondate;
    }

    /**
     * @return the staticfinalinspection
     */
    public LocalDateTime getStaticfinalinspection() {
        return staticfinalinspection;
    }

    /**
     * @return the staticdateofissue
     */
    public LocalDateTime getStaticdateofissue() {
        return staticdateofissue;
    }

    /**
     * @return the statictitle
     */
    public String getStatictitle() {
        return statictitle;
    }

    /**
     * @return the staticmuniaddress
     */
    public String getStaticmuniaddress() {
        return staticmuniaddress;
    }

    /**
     * @return the staticpropertyinfo
     */
    public String getStaticpropertyinfo() {
        return staticpropertyinfo;
    }

    /**
     * @return the staticownerseller
     */
    public String getStaticownerseller() {
        return staticownerseller;
    }

    /**
     * @return the staticcolumnlink
     */
    public String getStaticcolumnlink() {
        return staticcolumnlink;
    }

    /**
     * @return the staticbuyertenant
     */
    public String getStaticbuyertenant() {
        return staticbuyertenant;
    }

    /**
     * @return the staticproposeduse
     */
    public String getStaticproposeduse() {
        return staticproposeduse;
    }

    /**
     * @return the staticusecode
     */
    public String getStaticusecode() {
        return staticusecode;
    }

    /**
     * @return the staticpropclass
     */
    public String getStaticpropclass() {
        return staticpropclass;
    }

    /**
     * @return the staticofficername
     */
    public String getStaticofficername() {
        return staticofficername;
    }

    /**
     * @return the staticissuedundercodesourceid
     */
    public String getStaticissuedundercodesourceid() {
        return staticissuedundercodesourceid;
    }

    /**
     * @return the staticstipulations
     */
    public String getStaticstipulations() {
        return staticstipulations;
    }

    /**
     * @return the staticcomments
     */
    public String getStaticcomments() {
        return staticcomments;
    }

    /**
     * @return the staticmanager
     */
    public String getStaticmanager() {
        return staticmanager;
    }

    /**
     * @return the statictenants
     */
    public String getStatictenants() {
        return statictenants;
    }

    /**
     * @return the staticleaseterm
     */
    public String getStaticleaseterm() {
        return staticleaseterm;
    }

    /**
     * @return the staticleasestatus
     */
    public String getStaticleasestatus() {
        return staticleasestatus;
    }

    /**
     * @return the staticpaymentstatus
     */
    public String getStaticpaymentstatus() {
        return staticpaymentstatus;
    }

    /**
     * @return the staticnotice
     */
    public String getStaticnotice() {
        return staticnotice;
    }

    /**
     * @param finalizedts the finalizedts to set
     */
    public void setFinalizedts(LocalDateTime finalizedts) {
        this.finalizedts = finalizedts;
    }

    /**
     * @param staticdateofapplication the staticdateofapplication to set
     */
    public void setStaticdateofapplication(LocalDateTime staticdateofapplication) {
        this.staticdateofapplication = staticdateofapplication;
    }

    /**
     * @param staticinitialinspection the staticinitialinspection to set
     */
    public void setStaticinitialinspection(LocalDateTime staticinitialinspection) {
        this.staticinitialinspection = staticinitialinspection;
    }

    /**
     * @param staticreinspectiondate the staticreinspectiondate to set
     */
    public void setStaticreinspectiondate(LocalDateTime staticreinspectiondate) {
        this.staticreinspectiondate = staticreinspectiondate;
    }

    /**
     * @param staticfinalinspection the staticfinalinspection to set
     */
    public void setStaticfinalinspection(LocalDateTime staticfinalinspection) {
        this.staticfinalinspection = staticfinalinspection;
    }

    /**
     * @param staticdateofissue the staticdateofissue to set
     */
    public void setStaticdateofissue(LocalDateTime staticdateofissue) {
        this.staticdateofissue = staticdateofissue;
    }

    /**
     * @param statictitle the statictitle to set
     */
    public void setStatictitle(String statictitle) {
        this.statictitle = statictitle;
    }

    /**
     * @param staticmuniaddress the staticmuniaddress to set
     */
    public void setStaticmuniaddress(String staticmuniaddress) {
        this.staticmuniaddress = staticmuniaddress;
    }

    /**
     * @param staticpropertyinfo the staticpropertyinfo to set
     */
    public void setStaticpropertyinfo(String staticpropertyinfo) {
        this.staticpropertyinfo = staticpropertyinfo;
    }

    /**
     * @param staticownerseller the staticownerseller to set
     */
    public void setStaticownerseller(String staticownerseller) {
        this.staticownerseller = staticownerseller;
    }

    /**
     * @param staticcolumnlink the staticcolumnlink to set
     */
    public void setStaticcolumnlink(String staticcolumnlink) {
        this.staticcolumnlink = staticcolumnlink;
    }

    /**
     * @param staticbuyertenant the staticbuyertenant to set
     */
    public void setStaticbuyertenant(String staticbuyertenant) {
        this.staticbuyertenant = staticbuyertenant;
    }

    /**
     * @param staticproposeduse the staticproposeduse to set
     */
    public void setStaticproposeduse(String staticproposeduse) {
        this.staticproposeduse = staticproposeduse;
    }

    /**
     * @param staticusecode the staticusecode to set
     */
    public void setStaticusecode(String staticusecode) {
        this.staticusecode = staticusecode;
    }

    /**
     * @param staticpropclass the staticpropclass to set
     */
    public void setStaticpropclass(String staticpropclass) {
        this.staticpropclass = staticpropclass;
    }

    /**
     * @param staticofficername the staticofficername to set
     */
    public void setStaticofficername(String staticofficername) {
        this.staticofficername = staticofficername;
    }

    /**
     * @param staticissuedundercodesourceid the staticissuedundercodesourceid to set
     */
    public void setStaticissuedundercodesourceid(String staticissuedundercodesourceid) {
        this.staticissuedundercodesourceid = staticissuedundercodesourceid;
    }

    /**
     * @param staticstipulations the staticstipulations to set
     */
    public void setStaticstipulations(String staticstipulations) {
        this.staticstipulations = staticstipulations;
    }

    /**
     * @param staticcomments the staticcomments to set
     */
    public void setStaticcomments(String staticcomments) {
        this.staticcomments = staticcomments;
    }

    /**
     * @param staticmanager the staticmanager to set
     */
    public void setStaticmanager(String staticmanager) {
        this.staticmanager = staticmanager;
    }

    /**
     * @param statictenants the statictenants to set
     */
    public void setStatictenants(String statictenants) {
        this.statictenants = statictenants;
    }

    /**
     * @param staticleaseterm the staticleaseterm to set
     */
    public void setStaticleaseterm(String staticleaseterm) {
        this.staticleaseterm = staticleaseterm;
    }

    /**
     * @param staticleasestatus the staticleasestatus to set
     */
    public void setStaticleasestatus(String staticleasestatus) {
        this.staticleasestatus = staticleasestatus;
    }

    /**
     * @param staticpaymentstatus the staticpaymentstatus to set
     */
    public void setStaticpaymentstatus(String staticpaymentstatus) {
        this.staticpaymentstatus = staticpaymentstatus;
    }

    /**
     * @param staticnotice the staticnotice to set
     */
    public void setStaticnotice(String staticnotice) {
        this.staticnotice = staticnotice;
    }

    @Override
    public String getPKFieldName() {
        return PERMIT_PK;
    }

    @Override
    public int getDBKey() {
        return permitID;
    }

    @Override
    public String getDBTableName() {
        return TABLE_NAME;
    }

    /**
     * @return the finalizedBy
     */
    public User getFinalizedBy() {
        return finalizedBy;
    }

    /**
     * @param finalizedBy the finalizedBy to set
     */
    public void setFinalizedBy(User finalizedBy) {
        this.finalizedBy = finalizedBy;
    }

    /**
     * @return the staticconstructiontype
     */
    public String getStaticconstructiontype() {
        return staticconstructiontype;
    }

    /**
     * @param staticconstructiontype the staticconstructiontype to set
     */
    public void setStaticconstructiontype(String staticconstructiontype) {
        this.staticconstructiontype = staticconstructiontype;
    }

    /**
     * @return the ownerSellerLinkList
     */
    public List<HumanLink> getOwnerSellerLinkList() {
        return ownerSellerLinkList;
    }

    /**
     * @return the buyerTenantLinkList
     */
    public List<HumanLink> getBuyerTenantLinkList() {
        return buyerTenantLinkList;
    }

    /**
     * @return the managerLinkList
     */
    public List<HumanLink> getManagerLinkList() {
        return managerLinkList;
    }

    /**
     * @return the tenantLinkList
     */
    public List<HumanLink> getTenantLinkList() {
        return tenantLinkList;
    }

    /**
     * @return the parcelInfo
     */
    public ParcelInfo getParcelInfo() {
        return parcelInfo;
    }

    /**
     * @return the issuingOfficer
     */
    public User getIssuingOfficer() {
        return issuingOfficer;
    }

    /**
     * @return the textBlocks_stipulations
     */
    public List<TextBlock> getTextBlocks_stipulations() {
        return textBlocks_stipulations;
    }

    /**
     * @return the textBlocks_notice
     */
    public List<TextBlock> getTextBlocks_notice() {
        return textBlocks_notice;
    }

    /**
     * @return the textBlocks_comments
     */
    public List<TextBlock> getTextBlocks_comments() {
        return textBlocks_comments;
    }

    /**
     * @return the text_comments
     */
    public String getText_comments() {
        return text_comments;
    }

    /**
     * @return the dynamicDateOfApplication
     */
    public LocalDateTime getDynamicDateOfApplication() {
        return dynamicDateOfApplication;
    }

    /**
     * @return the dynamicInitialInspection
     */
    public LocalDateTime getDynamicInitialInspection() {
        return dynamicInitialInspection;
    }

    /**
     * @return the dynamicreinspectiondate
     */
    public LocalDateTime getDynamicreinspectiondate() {
        return dynamicreinspectiondate;
    }

    /**
     * @return the dynamicfinalinspection
     */
    public LocalDateTime getDynamicfinalinspection() {
        return dynamicfinalinspection;
    }

    /**
     * @return the dynamicdateofissue
     */
    public LocalDateTime getDynamicdateofissue() {
        return dynamicdateofissue;
    }

    /**
     * @param ownerSellerLinkList the ownerSellerLinkList to set
     */
    public void setOwnerSellerLinkList(List<HumanLink> ownerSellerLinkList) {
        this.ownerSellerLinkList = ownerSellerLinkList;
    }

    /**
     * @param buyerTenantLinkList the buyerTenantLinkList to set
     */
    public void setBuyerTenantLinkList(List<HumanLink> buyerTenantLinkList) {
        this.buyerTenantLinkList = buyerTenantLinkList;
    }

    /**
     * @param managerLinkList the managerLinkList to set
     */
    public void setManagerLinkList(List<HumanLink> managerLinkList) {
        this.managerLinkList = managerLinkList;
    }

    /**
     * @param tenantLinkList the tenantLinkList to set
     */
    public void setTenantLinkList(List<HumanLink> tenantLinkList) {
        this.tenantLinkList = tenantLinkList;
    }

    /**
     * @param parcelInfo the parcelInfo to set
     */
    public void setParcelInfo(ParcelInfo parcelInfo) {
        this.parcelInfo = parcelInfo;
    }

    /**
     * @param issuingOfficer the issuingOfficer to set
     */
    public void setIssuingOfficer(User issuingOfficer) {
        this.issuingOfficer = issuingOfficer;
    }

    /**
     * @param textBlocks_stipulations the textBlocks_stipulations to set
     */
    public void setTextBlocks_stipulations(List<TextBlock> textBlocks_stipulations) {
        this.textBlocks_stipulations = textBlocks_stipulations;
    }

    /**
     * @param textBlocks_notice the textBlocks_notice to set
     */
    public void setTextBlocks_notice(List<TextBlock> textBlocks_notice) {
        this.textBlocks_notice = textBlocks_notice;
    }

    /**
     * @param textBlocks_comments the textBlocks_comments to set
     */
    public void setTextBlocks_comments(List<TextBlock> textBlocks_comments) {
        this.textBlocks_comments = textBlocks_comments;
    }

    /**
     * @param text_comments the text_comments to set
     */
    public void setText_comments(String text_comments) {
        this.text_comments = text_comments;
    }

    /**
     * @param dynamicDateOfApplication the dynamicDateOfApplication to set
     */
    public void setDynamicDateOfApplication(LocalDateTime dynamicDateOfApplication) {
        this.dynamicDateOfApplication = dynamicDateOfApplication;
    }

    /**
     * @param dynamicInitialInspection the dynamicInitialInspection to set
     */
    public void setDynamicInitialInspection(LocalDateTime dynamicInitialInspection) {
        this.dynamicInitialInspection = dynamicInitialInspection;
    }

    /**
     * @param dynamicreinspectiondate the dynamicreinspectiondate to set
     */
    public void setDynamicreinspectiondate(LocalDateTime dynamicreinspectiondate) {
        this.dynamicreinspectiondate = dynamicreinspectiondate;
    }

    /**
     * @param dynamicfinalinspection the dynamicfinalinspection to set
     */
    public void setDynamicfinalinspection(LocalDateTime dynamicfinalinspection) {
        this.dynamicfinalinspection = dynamicfinalinspection;
    }

    /**
     * @param dynamicdateofissue the dynamicdateofissue to set
     */
    public void setDynamicdateofissue(LocalDateTime dynamicdateofissue) {
        this.dynamicdateofissue = dynamicdateofissue;
    }

    /**
     * @return the issuingCodeSourceList
     */
    public List<CodeSource> getIssuingCodeSourceList() {
        return issuingCodeSourceList;
    }

    /**
     * @param issuingCodeSourceList the issuingCodeSourceList to set
     */
    public void setIssuingCodeSourceList(List<CodeSource> issuingCodeSourceList) {
        this.issuingCodeSourceList = issuingCodeSourceList;
    }

    /**
     * @return the nullifiedTS
     */
    public LocalDateTime getNullifiedTS() {
        return nullifiedTS;
    }

    /**
     * @return the nullifiedBy
     */
    public User getNullifiedBy() {
        return nullifiedBy;
    }

    /**
     * @param nullifiedTS the nullifiedTS to set
     */
    public void setNullifiedTS(LocalDateTime nullifiedTS) {
        this.nullifiedTS = nullifiedTS;
    }

    /**
     * @param nullifiedBy the nullifiedBy to set
     */
    public void setNullifiedBy(User nullifiedBy) {
        this.nullifiedBy = nullifiedBy;
    }

    /**
     * @return the dynamicPopulationLog
     */
    public String getDynamicPopulationLog() {
        return dynamicPopulationLog;
    }

    /**
     * @param dynamicPopulationLog the dynamicPopulationLog to set
     */
    public void setDynamicPopulationLog(String dynamicPopulationLog) {
        this.dynamicPopulationLog = dynamicPopulationLog;
    }

    /**
     * @return the dynamicPopulationReadyForFinalizationTS
     */
    public LocalDateTime getDynamicPopulationReadyForFinalizationTS() {
        return dynamicPopulationReadyForFinalizationTS;
    }

    /**
     * @param dynamicPopulationReadyForFinalizationTS the dynamicPopulationReadyForFinalizationTS to set
     */
    public void setDynamicPopulationReadyForFinalizationTS(LocalDateTime dynamicPopulationReadyForFinalizationTS) {
        this.dynamicPopulationReadyForFinalizationTS = dynamicPopulationReadyForFinalizationTS;
    }

    /**
     * @return the dynamicsDateOfApplicationAppRef
     */
    public OccPermitApplication getDynamicsDateOfApplicationAppRef() {
        return dynamicsDateOfApplicationAppRef;
    }

    /**
     * @return the dynamicInitialInspectionFINRef
     */
    public FieldInspection getDynamicInitialInspectionFINRef() {
        return dynamicInitialInspectionFINRef;
    }

    /**
     * @return the dynamicReInspectionFINRef
     */
    public FieldInspection getDynamicReInspectionFINRef() {
        return dynamicReInspectionFINRef;
    }

    /**
     * @return the dynamicFinalInspectionFINRef
     */
    public FieldInspection getDynamicFinalInspectionFINRef() {
        return dynamicFinalInspectionFINRef;
    }

    /**
     * @param dynamicsDateOfApplicationAppRef the dynamicsDateOfApplicationAppRef to set
     */
    public void setDynamicsDateOfApplicationAppRef(OccPermitApplication dynamicsDateOfApplicationAppRef) {
        this.dynamicsDateOfApplicationAppRef = dynamicsDateOfApplicationAppRef;
    }

    /**
     * @param dynamicInitialInspectionFINRef the dynamicInitialInspectionFINRef to set
     */
    public void setDynamicInitialInspectionFINRef(FieldInspection dynamicInitialInspectionFINRef) {
        this.dynamicInitialInspectionFINRef = dynamicInitialInspectionFINRef;
    }

    /**
     * @param dynamicReInspectionFINRef the dynamicReInspectionFINRef to set
     */
    public void setDynamicReInspectionFINRef(FieldInspection dynamicReInspectionFINRef) {
        this.dynamicReInspectionFINRef = dynamicReInspectionFINRef;
    }

    /**
     * @param dynamicFinalInspectionFINRef the dynamicFinalInspectionFINRef to set
     */
    public void setDynamicFinalInspectionFINRef(FieldInspection dynamicFinalInspectionFINRef) {
        this.dynamicFinalInspectionFINRef = dynamicFinalInspectionFINRef;
    }

    /**
     * @return the finalizationAuditLog
     */
    public String getFinalizationAuditLog() {
        return finalizationAuditLog;
    }

    /**
     * @param finalizationAuditLog the finalizationAuditLog to set
     */
    public void setFinalizationAuditLog(String finalizationAuditLog) {
        this.finalizationAuditLog = finalizationAuditLog;
    }

    /**
     * @return the finalizationAuditPassTS
     */
    public LocalDateTime getFinalizationAuditPassTS() {
        return finalizationAuditPassTS;
    }

    /**
     * @param finalizationAuditPassTS the finalizationAuditPassTS to set
     */
    public void setFinalizationAuditPassTS(LocalDateTime finalizationAuditPassTS) {
        this.finalizationAuditPassTS = finalizationAuditPassTS;
    }

  
    
    
    
}
