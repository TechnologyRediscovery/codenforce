/*
 * Copyright (C) 2017 Turtle Creek Valley
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
import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CasePhase;
import com.tcvcog.tcvce.entities.CaseStage;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Eric C. Darsow
 */
public class SystemCoordinator extends BackingBeanUtils implements Serializable{

    private Map<Integer, String> muniCodeNameMap;
    
    /**
     * Creates a new instance of LoggingCoordinator
     */
    public SystemCoordinator() {
    }
    
    
    
    

    /**
     * @return the muniCodeNameMap
     */
    public Map<Integer, String> getMuniCodeNameMap() {
        if(muniCodeNameMap == null){
            
            Map<Integer, String> m = null;
            MunicipalityIntegrator mi = getMunicipalityIntegrator();
            try {
                m = mi.getMunicipalityMap();
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
            muniCodeNameMap = m;
        }
        return muniCodeNameMap;
    }
    
    public User getPublicAccessUser(){
        User u = new User();
        u.setRoleType(RoleType.Public);
        return u;
    }
    
    

    /**
     * @param muniCodeNameMap the muniCodeNameMap to set
     */
    public void setMuniCodeNameMap(Map<Integer, String> muniCodeNameMap) {
        this.muniCodeNameMap = muniCodeNameMap;
    }
    
    
    
}
