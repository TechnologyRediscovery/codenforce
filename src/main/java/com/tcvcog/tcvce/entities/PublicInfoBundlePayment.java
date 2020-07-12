/*
 * Copyright (C) 2020 Technology Rediscovery LLC.
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

/**
 *
 * @author Nathan Dietz
 */
public class PublicInfoBundlePayment extends PublicInfoBundle {
    
    private Payment bundledPayment;
    private PublicInfoBundlePerson payer;
    
    @Override
    public String toString(){
        
        return this.getClass().getName() + bundledPayment.getPaymentID();
        
    }

    public Payment getBundledPayment() {
        return bundledPayment;
    }
    
    public void setBundledPayment(Payment input) {

        input.setPayer(new Person());       
        input.setRecordedBy(new User());
        input.setEntryTimestamp(LocalDateTime.MIN);
        input.setNotes("*****");
        
        bundledPayment = input;
    }

    public PublicInfoBundlePerson getPayer() {
        return payer;
    }

    public void setPayer(PublicInfoBundlePerson payer) {
        this.payer = payer;
    }
    
}
