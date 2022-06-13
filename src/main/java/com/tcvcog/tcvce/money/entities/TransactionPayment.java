
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

import com.tcvcog.tcvce.entities.BOBSource;
import com.tcvcog.tcvce.entities.IFace_trackedEntityLink;
import com.tcvcog.tcvce.entities.LinkedObjectRole;
import java.time.LocalDateTime;


/**
 * A superclass of payment methods: check and municipay
 * as of June 2022 launch
 * 
 * @author Adam Gutonski & Nathan Dietz & Ellen Bascomb of Apartment 31Y
 */
public  class       TransactionPayment 
                extends     TransactionDetails {
    
    
    public TransactionPayment(){
        
    }
    
    public TransactionPayment(TransactionDetails det){
        super(det);
        
    }
   
}
