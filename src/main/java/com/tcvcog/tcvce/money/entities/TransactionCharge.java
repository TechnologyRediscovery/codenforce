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

import java.util.List;


/**
 * This BOb represents a fee that has actually been assigned to an entity.
 * @author Nathan Dietz and Ellen Bascomb of Apartment 31Y
 */
public  class       TransactionCharge 
        extends     TransactionDetails{

    private List<ChargeOrderPosted> chargeOrders;
  
    public TransactionCharge() {
        
    }

    public TransactionCharge(TransactionCharge trx){
        super(trx);
        chargeOrders = trx.getChargeOrders();
    }
    
    public TransactionCharge(TransactionDetails trx){
        super(trx);
    }

    /**
     * @return the chargeOrders
     */
    public List<ChargeOrderPosted> getChargeOrders() {
        return chargeOrders;
    }

    /**
     * @param chargeOrders the chargeOrders to set
     */
    public void setChargeOrders(List<ChargeOrderPosted> chargeOrders) {
        this.chargeOrders = chargeOrders;
    }
    
  

 
}