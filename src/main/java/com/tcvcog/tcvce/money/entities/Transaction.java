/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.money.entities;

import com.tcvcog.tcvce.entities.DomainEnum;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.HumanLink;
import com.tcvcog.tcvce.entities.IFace_humanListHolder;
import com.tcvcog.tcvce.entities.IFace_noteHolder;
import com.tcvcog.tcvce.entities.LinkedObjectSchemaEnum;
import com.tcvcog.tcvce.entities.TrackedEntity;
import com.tcvcog.tcvce.entities.User;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a record in a ledger on either a permitfile account or a ce case account
 * @author sylvia
 */
public class Transaction 
        extends TrackedEntity 
        implements IFace_noteHolder,
                    IFace_humanListHolder{
    
  static final String TNX_PKFIELD = "transactionid";
  static final String TNX_TABLENAME = "moneyledger";
  static final String CLASS_NAME_FRIENDLY = "Transaction";
  static final LinkedObjectSchemaEnum TNX_HUMAN_LORSCHEMA = LinkedObjectSchemaEnum.TransactionHuman;
  
    
  protected int transactionID;
  protected int ceCaseID;
  protected int occPeriodID;
  protected DomainEnum tnxDomain;
  protected TnxTypeEnum  tnxType;
  protected double amount;
  protected LocalDateTime dateOfRecord;

  protected int trxSourceID;
  protected TnxSource trxSource;
  protected int trackingEventID;
  
  protected LocalDateTime lockedTS;
  protected User lockedByUser;
  protected String notes;
  
  protected List<HumanLink> humanLinkList;
  
  protected LocalDateTime auditPassTS;
  
  public Transaction(){
      
  }
  
  /**
   * Creates a new instance of Transaction from a Transaction
   * Not a deep copy - references will migrate
   * @param trx 
   */
  public Transaction(Transaction trx){
      
    this.transactionID = trx.transactionID;
    
    this.ceCaseID = trx.ceCaseID;
    this.occPeriodID = trx.occPeriodID;
    this.tnxDomain = trx.tnxDomain;
    this.tnxType = trx.tnxType;
    this.amount = trx.amount;
    this.dateOfRecord = trx.dateOfRecord;
    this.trxSource = trx.trxSource;
    this.trackingEventID = trx.trackingEventID;
    this.lockedTS = trx.lockedTS;
    this.lockedByUser = trx.lockedByUser;
    this.notes = trx.notes;      
    
    this.humanLinkList = trx.humanLinkList;
    
    this.auditPassTS = trx.auditPassTS;
      
      
  }
  

    @Override
    public String getPKFieldName() {
        return TNX_PKFIELD;
    }

    @Override
    public int getDBKey() {
        return getTransactionID();
    }

    @Override
    public String getDBTableName() {
        return TNX_TABLENAME;
    }

    @Override
    public String getNotes() {
        return notes;
    }

    @Override
    public void setNotes(String n) {
        notes = n;
    }

    @Override
    public String getNoteHolderFriendlyName() {
        return CLASS_NAME_FRIENDLY;
    }

    /**
     * @return the transactionID
     */
    public int getTransactionID() {
        return transactionID;
    }

    /**
     * @return the tnxDomain
     */
    public DomainEnum getTnxDomain() {
        return tnxDomain;
    }

    /**
     * @return the tnxType
     */
    public TnxTypeEnum getTnxType() {
        return tnxType;
    }

    /**
     * @return the amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * @return the dateOfRecord
     */
    public LocalDateTime getDateOfRecord() {
        return dateOfRecord;
    }

    /**
     * @return the trxSource
     */
    public TnxSource getTrxSource() {
        return trxSource;
    }

    /**
     * @return the trackingEvent
     */
    public int getTrackingEventID() {
        return trackingEventID;
    }


    /**
     * @return the lockedTS
     */
    public LocalDateTime getLockedTS() {
        return lockedTS;
    }

    /**
     * @return the lockedByUser
     */
    public User getLockedByUser() {
        return lockedByUser;
    }

    /**
     * @param transactionID the transactionID to set
     */
    public void setTransactionID(int transactionID) {
        this.transactionID = transactionID;
    }

    /**
     * @param tnxDomain the tnxDomain to set
     */
    public void setTnxDomain(DomainEnum tnxDomain) {
        this.tnxDomain = tnxDomain;
    }

    /**
     * @param tnxType the tnxType to set
     */
    public void setTnxType(TnxTypeEnum tnxType) {
        this.tnxType = tnxType;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * @param dateOfRecord the dateOfRecord to set
     */
    public void setDateOfRecord(LocalDateTime dateOfRecord) {
        this.dateOfRecord = dateOfRecord;
    }

    /**
     * @param trxSource the trxSource to set
     */
    public void setTrxSource(TnxSource trxSource) {
        this.trxSource = trxSource;
    }

    /**
     * @param id
     */
    public void setTrackingEventID(int id) {
        this.trackingEventID = id;
    }


    /**
     * @param lockedTS the lockedTS to set
     */
    public void setLockedTS(LocalDateTime lockedTS) {
        this.lockedTS = lockedTS;
    }

    /**
     * @param lockedByUser the lockedByUser to set
     */
    public void setLockedByUser(User lockedByUser) {
        this.lockedByUser = lockedByUser;
    }

    /**
     * @return the ceCaseID
     */
    public int getCeCaseID() {
        return ceCaseID;
    }

    /**
     * @return the occPeriodID
     */
    public int getOccPeriodID() {
        return occPeriodID;
    }

    /**
     * @param ceCaseID the ceCaseID to set
     */
    public void setCeCaseID(int ceCaseID) {
        this.ceCaseID = ceCaseID;
    }

    /**
     * @param occPeriodID the occPeriodID to set
     */
    public void setOccPeriodID(int occPeriodID) {
        this.occPeriodID = occPeriodID;
    }

    /**
     * @return the humanLinkList
     */
  @Override
    public List<HumanLink> getHumanLinkList() {
        return humanLinkList;
    }

    /**
     * @param humanLinkList the humanLinkList to set
     */
  @Override
    public void setHumanLinkList(List<HumanLink> humanLinkList) {
        this.humanLinkList = humanLinkList;
    }

    @Override
    public LinkedObjectSchemaEnum getHUMAN_LINK_SCHEMA_ENUM() {
        return TNX_HUMAN_LORSCHEMA;
    }

    @Override
    public int getHostPK() {
        return transactionID;
    }

    /**
     * @return the auditPassTS
     */
    public LocalDateTime getAuditPassTS() {
        return auditPassTS;
    }

    /**
     * @param auditPassTS the auditPassTS to set
     */
    public void setAuditPassTS(LocalDateTime auditPassTS) {
        this.auditPassTS = auditPassTS;
    }

    /**
     * @return the trxSourceID
     */
    public int getTrxSourceID() {
        return trxSourceID;
    }

    /**
     * @param trxSourceID the trxSourceID to set
     */
    public void setTrxSourceID(int trxSourceID) {
        this.trxSourceID = trxSourceID;
    }
  
    
}
