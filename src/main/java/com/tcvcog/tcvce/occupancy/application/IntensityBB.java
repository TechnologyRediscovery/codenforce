/*
 * Copyright (C) 2019 Nathan Dietz
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
import com.tcvcog.tcvce.entities.Intensity;
import java.io.Serializable;
import java.util.ArrayList;
import javax.annotation.PostConstruct;

/**
 *
 * @author Nathan Dietz
 */
public class IntensityBB extends BackingBeanUtils implements Serializable {
    
    private ArrayList<Intensity> workingIntensityList;
    private ArrayList<Intensity> existingIntensityList;
    
    
    public IntensityBB() {
        
    }
    
    @PostConstruct
    public void initBean() {
        
    }
            
    
    
}
