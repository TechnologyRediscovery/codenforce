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
import com.tcvcog.tcvce.application.interfaces.IFace_EventRuleGoverned;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.CECasePropertyUnitHeavy;
import com.tcvcog.tcvce.entities.MuniProfile;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.MunicipalityDataHeavy;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.integration.CourtEntityIntegrator;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Business logic container for all things municipality
 * @author ellen bascomb
 */
public class MunicipalityCoordinator extends BackingBeanUtils implements Serializable {

    public final int DEFAULT_DEADLINE_BUFFER_DAYS = 10;
    public final int DEFAULT_VIOLATION_NO_LETTER_BUFFER_DAYS = 3;
    public final boolean PRIORITIZE_LETTER_BUFFER = false;
    
    /**
     * Creates a new instance of MuniCoordinator
     */
    public MunicipalityCoordinator() {
    }

    public MunicipalityDataHeavy assembleMuniDataHeavy(Municipality muni, UserAuthorized ua) 
            throws IntegrationException, AuthorizationException, BObStatusException, EventException, BlobException {
        MunicipalityDataHeavy mdh = null;
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        mdh = mi.getMunDataHeavy(muni.getMuniCode());
        return configureMuniDataHeavy(mdh, ua);
    }

    /**
     * Sets various variables and lists on a data heavy muni
     * @param mdh
     * @param ua
     * @return
     * @throws IntegrationException
     * @throws BObStatusException
     * @throws AuthorizationException
     * @throws EventException 
     */
    private MunicipalityDataHeavy configureMuniDataHeavy(MunicipalityDataHeavy mdh, UserAuthorized ua) 
            throws IntegrationException, BObStatusException, AuthorizationException, EventException, BlobException {
        if(mdh != null && ua != null){
            PropertyCoordinator pc = getPropertyCoordinator();
            CourtEntityIntegrator cei = getCourtEntityIntegrator();
            UserCoordinator uc = getUserCoordinator();
            // FIX THIS WHEN WE HAVE STABLE AUTHORIZATION PROCEDURES
//            mdh.setUserList(uc.extractUsersFromUserAuthorized(uc.getUserAuthorizedListForConfig(mdh)));
            
            try {
                mdh.setMuniPropertyDH(pc.assemblePropertyDataHeavy(pc.getProperty(mdh.getMuniOfficePropertyId()), ua));
                mdh.setCourtEntities(cei.getCourtEntityList(mdh.getMuniCode()));
                mdh.setSwornOfficerList(uc.user_assembleUserListOfficerOathRequired(mdh));
                mdh.setZipList(pc.getZipListByMunicipality(mdh));
            } catch (SearchException ex) {
                System.out.println(ex);
            }

            mdh.setPropertyCount(pc.computeTotalProperties(mdh.getMuniCode()));
        }

        return mdh;

    }

    /**
     * Retrieves a single municipality from the database
     * @param muniCode
     * @return
     * @throws IntegrationException 
     */
    public Municipality getMuni(int muniCode) throws IntegrationException {
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        return mi.getMuni(muniCode);

    }

    /**
     * A hacky way to get a default occ period
     * @param ua
     * @return
     * @throws IntegrationException
     * @throws AuthorizationException 
     */
    public OccPeriod selectDefaultMuniOccPeriod(UserAuthorized ua) throws IntegrationException, AuthorizationException {
        OccupancyCoordinator oc = getOccupancyCoordinator();
        MunicipalityDataHeavy mdh = null;
        if (ua != null) {
            try {
                mdh = assembleMuniDataHeavy(ua.getMyCredential().getGoverningAuthPeriod().getMuni(), ua);
            } catch (BObStatusException | EventException | AuthorizationException | BlobException ex) {
                System.out.println(ex);
            }
            if (mdh != null && mdh.getDefaultOccPeriodID() != 0) {
                return oc.getOccPeriod(mdh.getDefaultOccPeriodID(), ua);
            }
        }
        return null;
    }
    
    /**
     * Selects an event rule goverend object
     * @param mdh
     * @return 
     */
    public IFace_EventRuleGoverned determineERG(MunicipalityDataHeavy mdh){
        // first, choose a property info case if we have one
        List<CECasePropertyUnitHeavy> cecaseList = new ArrayList<>();
        cecaseList = mdh.getMuniPropertyDH().getCeCaseList();
        if(cecaseList != null && !cecaseList.isEmpty()){
            return (IFace_EventRuleGoverned) cecaseList.get(0);
            
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
        try {
            return mi.getMunDataHeavy(muniCode);
        } catch (BObStatusException ex) {
            throw new IntegrationException(ex.getMessage());
        }
    }
    
    /**
     * Updates fields on the Municipality object
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
     * Writes a new municipality to the system
     * @param muni
     * @param user 
     */
    public void insertMuni(MunicipalityDataHeavy muni, User user){
        muni.setLastUpdatedBy(user);
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        mi.insertMuniDataHeavy(muni);
    }
    
    /**
     * Logic pass through for muni profiles. Set's default values where needed.
     * @param profileID
     * @return
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public MuniProfile getMuniProfile(int profileID) throws IntegrationException, BObStatusException{
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        MuniProfile mp = mi.getMuniProfile(profileID);
        
        return mp;
        
    }
    
    /**
     * Extracts all municipality profiles from DB
     * @return
     * @throws IntegrationException 
     */
    public List<MuniProfile> getMuniProfilesList() throws IntegrationException{
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        try {
            return mi.getMuniProfileList();
        } catch (BObStatusException ex) {
            throw new IntegrationException(ex.getMessage());
        }
    }
    
    /**
     * Writes a new muni profile to the DB
     * @param profile
     * @param ua
     * @return the PK of the fresh profile
     */
    public int insertMuniProfile(MuniProfile profile, UserAuthorized ua){
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        
        return 0;
    }
    
    /**
     * Updates a given municipality profile object
     * @param profile
     * @param ua 
     */
    public void updateMuniProfile(MuniProfile profile, UserAuthorized ua){
        
        
    }
    
    /**
     * Deactivates a given muni profile; Logic will check that no municipality is
     * currently linked to (i.e. using) the profile which under request for deac
     * 
     * @param profile
     * @param ua 
     */
    public void deactivateMuniProfile(MuniProfile profile, UserAuthorized ua){
       
        
        
    }
    

}
