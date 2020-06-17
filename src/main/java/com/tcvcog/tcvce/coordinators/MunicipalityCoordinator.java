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
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.Credential;
import com.tcvcog.tcvce.entities.MuniProfile;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.MunicipalityDataHeavy;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserMuniAuthPeriod;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ellen bascomb
 */
public class MunicipalityCoordinator extends BackingBeanUtils implements Serializable {

    /**
     * Creates a new instance of MuniCoordinator
     */
    public MunicipalityCoordinator() {
    }

    public MunicipalityDataHeavy assembleMuniDataHeavy(Municipality muni, Credential cred) throws IntegrationException, AuthorizationException, BObStatusException, EventException {
        MunicipalityDataHeavy mdh = null;
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        mdh = mi.getMunDataHeavy(muni.getMuniCode());
        return configureMuniDataHeavy(mdh, cred);
    }

    private MunicipalityDataHeavy configureMuniDataHeavy(MunicipalityDataHeavy mdh, Credential cred) throws IntegrationException, BObStatusException, AuthorizationException, EventException {
        PropertyCoordinator pc = getPropertyCoordinator();
        try {
            pc.getPropertyDataHeavy(mdh.getMuniOfficePropertyId(), cred);
        } catch (SearchException ex) {
            System.out.println(ex);
        }

        return mdh;

    }

    public Municipality getMuni(int muniCode) throws IntegrationException {
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        try {
            return mi.getMuni(muniCode);
        } catch (SQLException ex) {
            System.out.println(ex);
        }

        return null;

    }

    public OccPeriod selectDefaultMuniOccPeriod(Credential cred) throws IntegrationException, AuthorizationException {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        MunicipalityDataHeavy mdh = null;
        if (cred != null) {
            try {
                mdh = assembleMuniDataHeavy(cred.getGoverningAuthPeriod().getMuni(), cred);
            } catch (BObStatusException | EventException ex) {
                System.out.println(ex);
            }
            if (mdh != null && mdh.getDefaultOccPeriodID() != 0) {
                return oc.getOccPeriod(mdh.getDefaultOccPeriodID());
            }
        }
        return null;
    }

    /**
     * !!SECURITY CRITICAL!! Implements logic to generate the authorized list of
     * Municipalities to which a given Administrator User can assign a new User
     * in the system.
     *
     *
     * @param user
     * @return
     * @throws IntegrationException
     */
    public List<Municipality> getPermittedMunicipalityListForAdminMuniAssignment(UserAuthorized user) throws IntegrationException {
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        List<Municipality> muniList = new ArrayList<>();
        if (user.getMyCredential().getGoverningAuthPeriod().getRole() == RoleType.Developer) {
            muniList.addAll(mi.getMuniList());
        } else {
            if (user.getRole() != null && user.getRole() == RoleType.SysAdmin) {
                muniList.add(user.getMyCredential().getGoverningAuthPeriod().getMuni());
            }
        }

        return muniList;
    }

    /**
     * 
     * @param muniCode
     * @return
     * @throws IntegrationException
     * @throws AuthorizationException 
     */
    public MunicipalityDataHeavy getMuniDataHeavyList(int muniCode) throws IntegrationException, AuthorizationException {
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        return mi.getMunDataHeavy(muniCode);
    }
    
    /**
     * 
     * @param muni
     * @param user 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void updateMuni(MunicipalityDataHeavy muni, User user) throws IntegrationException{
        muni.setLastUpdatedBy(user);
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        mi.updateMuniDataHeavy(muni);
    }
    /**
     * 
     * @param muni
     * @param user 
     */
    public void insertMuni(MunicipalityDataHeavy muni, User user){
        muni.setLastUpdatedBy(user);
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        mi.insertMuniDataHeavy(muni);
    }
    
    /**
     * 
     * @return
     * @throws IntegrationException 
     */
    public ArrayList<MuniProfile> getMuniProfilesList() throws IntegrationException{
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        return mi.getMuniProfileList();
    }
    
    

}
