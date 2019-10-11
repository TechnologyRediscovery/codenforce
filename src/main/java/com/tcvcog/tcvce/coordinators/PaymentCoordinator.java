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
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Fee;
import com.tcvcog.tcvce.entities.Payment;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.occupancy.integration.PaymentIntegrator;
import java.util.ArrayList;

/**
 * Implements business logic related to payments.
 * 
 * @author NADGIT and SYLVIA
 */
public class PaymentCoordinator extends BackingBeanUtils {

    /**
     * Creates a new instance of PaymentCoordinator
     */
    public PaymentCoordinator() {
    }
    
    /**
     * Container for business logic related to an individual Payment instance
     * @param pmt
     * @return 
     */
    public Payment configurePayment(Payment pmt){
        
        return pmt;
        
        
        
    }
    
    public void insertAutoAssignedFees(OccPeriod period) throws IntegrationException{

    PaymentIntegrator pi = getPaymentIntegrator();
        
    ArrayList<Fee> feeList = (ArrayList<Fee>) pi.getFeeList(period.getType());
    
    }
    
    
}
