
/*
 * Copyright (C) 2018 Adam Gutonski
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
 * A superclass of payment methods: check and municipay
 * as of June 2022 launch
 * 
 * @author Adam Gutonski & Nathan Dietz & Ellen Bascomb of Apartment 31Y
 */
public          class       TransactionPayment 
                extends     TransactionDetails {
    
    private List<ChargeOrder> chargeOrders;
    
    public TransactionPayment(){
        
    }
    
    public TransactionPayment(TransactionDetails det){
        super(det);
        
    }

    /**
     * @return the chargeOrders
     */
    public List<ChargeOrder> getChargeOrders() {
        return chargeOrders;
    }

    /**
     * @param chargeOrders the chargeOrders to set
     */
    public void setChargeOrders(List<ChargeOrder> chargeOrders) {
        this.chargeOrders = chargeOrders;
    }
   
}
