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
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.MunicipalityDataHeavy;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserMuniAuthPeriod;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sylvia
 */
public class MunicipalityCoordinator extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of MuniCoordinator
     */
    public MunicipalityCoordinator() {
    }
    
    public MunicipalityDataHeavy getMuniDataHeavy(int muniCode) throws IntegrationException, AuthorizationException{
        MunicipalityDataHeavy mdh = null;
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        mdh = mi.getMuniListified(muniCode);
        return mdh;
    }    
   
    
    
     
    /**
     * !!SECURITY CRITICAL!!
     * Implements logic to generate the authorized list of Municipalities to which
     * a given Administrator User can assign a new User in the system.
     * 
     * 
     * @param user
     * @return
     * @throws IntegrationException 
     */
      public List<Municipality> getPermittedMunicipalityListForAdminMuniAssignment(UserAuthorized user) throws IntegrationException{
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        List<Municipality> muniList = new ArrayList<>();
        if(user.getMyCredential().getGoverningAuthPeriod().getRole() == RoleType.Developer){
            muniList.addAll(mi.getMuniList());
        } else {
            if(user.getRole() != null && user.getRole() == RoleType.SysAdmin){
                muniList.add(user.getMyCredential().getGoverningAuthPeriod().getMuni());
            }
        }
        
        return muniList;
    }
    
  
    
    
}
