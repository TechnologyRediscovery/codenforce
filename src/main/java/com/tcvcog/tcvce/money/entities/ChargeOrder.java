/*
 * Copyright (C) 2018 Technology Rediscovery, LLC.
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
package com.tcvcog.tcvce.money.entities;

import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.IFace_noteHolder;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.TrackedEntity;
import java.time.LocalDateTime;

/**
 * A fee template that may be applied to an entity
 * @author Nathan Dietz
 */
public  class       ChargeOrder 
        extends     TrackedEntity 
        implements  IFace_noteHolder {
    
    
    final static String CHARGE_ORDER_TABLE_NAME = "moneychargeschedule";
    final static String CHARGE_ORDER_TABLE_PKFIELD = "chargeid";
    final static String CHARGE_ORDER_FRIENDLY_NAME = "Charge Order";
    
    
    private int chargeID;
    private ChargeOrderDomainEnum chargeDomain;
    private Municipality muni;

    private String name;
    private String description;
    private double amount;
    
    private int governingEnforcableCodeElementId;

    private LocalDateTime effectiveDate;
    private LocalDateTime expiryDate;
    
    private RoleType minRoleToAssign;
    private RoleType minRoleToDeactivate;
    
    private EventCategory eventCategoryOnPost;

    private String notes;
    
    public ChargeOrder(){
        
    }
    
    public ChargeOrder(ChargeOrder chgOrder){
        
        this.chargeID = chgOrder.chargeID;
        this.chargeDomain = chgOrder.chargeDomain;
        this.muni = chgOrder.muni;
        this.name = chgOrder.name;
        this.description = chgOrder.description;
        this.amount = chgOrder.amount;
        this.governingEnforcableCodeElementId = chgOrder.governingEnforcableCodeElementId;
        this.effectiveDate = chgOrder.effectiveDate;
        this.expiryDate = chgOrder.expiryDate;
        this.minRoleToAssign = chgOrder.minRoleToAssign;
        this.minRoleToDeactivate = chgOrder.minRoleToDeactivate;
        this.eventCategoryOnPost = chgOrder.eventCategoryOnPost;
        this.notes = chgOrder.notes;
        
    }

    /**
     * @return the chargeID
     */
    public int getChargeID() {
        return chargeID;
    }

    /**
     * @return the chargeDomain
     */
    public ChargeOrderDomainEnum getChargeDomain() {
        return chargeDomain;
    }

    /**
     * @return the muni
     */
    public Municipality getMuni() {
        return muni;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * @return the governingEnforcableCodeElementId
     */
    public int getGoverningEnforcableCodeElementId() {
        return governingEnforcableCodeElementId;
    }

    /**
     * @return the effectiveDate
     */
    public LocalDateTime getEffectiveDate() {
        return effectiveDate;
    }

    /**
     * @return the expiryDate
     */
    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    /**
     * @return the minRoleToAssign
     */
    public RoleType getMinRoleToAssign() {
        return minRoleToAssign;
    }

    /**
     * @return the minRoleToDeactivate
     */
    public RoleType getMinRoleToDeactivate() {
        return minRoleToDeactivate;
    }

    /**
     * @return the eventCategoryOnPost
     */
    public EventCategory getEventCategoryOnPost() {
        return eventCategoryOnPost;
    }

    /**
     * @return the notes
     */
    @Override
    public String getNotes() {
        return notes;
    }

    /**
     * @param chargeID the chargeID to set
     */
    public void setChargeID(int chargeID) {
        this.chargeID = chargeID;
    }

    /**
     * @param chargeDomain the chargeDomain to set
     */
    public void setChargeDomain(ChargeOrderDomainEnum chargeDomain) {
        this.chargeDomain = chargeDomain;
    }

    /**
     * @param muni the muni to set
     */
    public void setMuni(Municipality muni) {
        this.muni = muni;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * @param governingEnforcableCodeElementId the governingEnforcableCodeElementId to set
     */
    public void setGoverningEnforcableCodeElementId(int governingEnforcableCodeElementId) {
        this.governingEnforcableCodeElementId = governingEnforcableCodeElementId;
    }

    /**
     * @param effectiveDate the effectiveDate to set
     */
    public void setEffectiveDate(LocalDateTime effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    /**
     * @param expiryDate the expiryDate to set
     */
    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    /**
     * @param minRoleToAssign the minRoleToAssign to set
     */
    public void setMinRoleToAssign(RoleType minRoleToAssign) {
        this.minRoleToAssign = minRoleToAssign;
    }

    /**
     * @param minRoleToDeactivate the minRoleToDeactivate to set
     */
    public void setMinRoleToDeactivate(RoleType minRoleToDeactivate) {
        this.minRoleToDeactivate = minRoleToDeactivate;
    }

    /**
     * @param eventCategoryOnPost the eventCategoryOnPost to set
     */
    public void setEventCategoryOnPost(EventCategory eventCategoryOnPost) {
        this.eventCategoryOnPost = eventCategoryOnPost;
    }

    /**
     * @param notes the notes to set
     */
    @Override
    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String getPKFieldName() {
        return CHARGE_ORDER_TABLE_NAME;
    }

    @Override
    public int getDBKey() {
        return chargeID;
    }

    @Override
    public String getDBTableName() {
        return CHARGE_ORDER_TABLE_NAME;
    }

    @Override
    public String getNoteHolderFriendlyName() {
        return CHARGE_ORDER_FRIENDLY_NAME;
    }

  

}
