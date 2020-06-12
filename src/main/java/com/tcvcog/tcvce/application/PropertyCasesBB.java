/*
 * Copyright (C) 2020 Technology Rediscovery LLC
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
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.PropertyDataHeavy;
import javax.annotation.PostConstruct;

/**
 *
 * @author sylvia
 */
public class PropertyCasesBB 
        extends BackingBeanUtils{

    private PropertyDataHeavy currProp;
    
    /**
     * Creates a new instance of PropertyCreateBB
     */
    public PropertyCasesBB() {
    }
    
    
    
     
    @PostConstruct
    public void initBean(){
        currProp = getSessionBean().getSessProperty();
        
    }
    
       
    public String exploreCECase(CECaseDataHeavy c){
        getSessionBean().setSessCECase(c);
        return "ceCases";
    }

    public String openCECase(PropertyDataHeavy pdh){
        getSessionBean().setSessProperty(pdh);
        return "addNewCase";
    }
 
    
    /**
     * @return the currProp
     */
    public PropertyDataHeavy getCurrProp() {
        return currProp;
    }

    /**
     * @param currProp the currProp to set
     */
    public void setCurrProp(PropertyDataHeavy currProp) {
        this.currProp = currProp;
    }
    
    
}
