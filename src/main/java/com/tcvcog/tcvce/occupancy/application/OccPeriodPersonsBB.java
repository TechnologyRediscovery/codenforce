/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.application.SessionBean;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodDataHeavy;
import javax.annotation.PostConstruct;

/**
 *
 * @author sylvia
 */
public  class OccPeriodPersonsBB 
        extends BackingBeanUtils{
    
    private OccPeriodDataHeavy currentOccPeriod;
     
    @PostConstruct
    public void initBean() {
        SessionBean sb = getSessionBean();
        currentOccPeriod = sb.getSessOccPeriod();
       
    }
    
    /**
     * Creates a new instance of OccPeriodPersonsBB
     */
    public OccPeriodPersonsBB() {
    }
    
}
