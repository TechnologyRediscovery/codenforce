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

import com.tcvcog.tcvce.entities.DomainEnum;
import com.tcvcog.tcvce.money.entities.ChargeOrder;

/**
 * This BOb represents a fee that has actually been assigned to an entity.
 * @author Nathan Dietz
 */
public  class       TransactionCharge 
        extends     TransactionDetails{

    

    private ChargeOrder chargeOrder;
    private DomainEnum domain;
  
    

    public TransactionCharge() {
    }

    public TransactionCharge(TransactionCharge trx){
        super(trx);
        
        
        
    }
    


    /**
     * @return the chargeOrder
     */
    public ChargeOrder getChargeOrder() {
        return chargeOrder;
    }

    /**
     * @return the domain
     */
    public DomainEnum getDomain() {
        return domain;
    }

    /**
     * @param chargeOrder the chargeOrder to set
     */
    public void setChargeOrder(ChargeOrder chargeOrder) {
        this.chargeOrder = chargeOrder;
    }

    /**
     * @param domain the domain to set
     */
    public void setDomain(DomainEnum domain) {
        this.domain = domain;
    }
}