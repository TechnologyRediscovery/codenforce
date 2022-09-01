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
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.util.Constants;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;

/**
 *
 * @author sylvia
 */
public class LoginBB extends BackingBeanUtils {

    private UserAuthorized publicUA;
    private String patchRecord;
    private String ds;
    private String releaseID;
    /**
     * Creates a new instance of LoginBB
     */
    public LoginBB() {
    }
    
         
    @PostConstruct
    public void initBean(){
        UserCoordinator uc = getUserCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        try {
            publicUA = uc.auth_getPublicUserAuthorized();
            patchRecord = sc.getDBPatchIDList();
        } catch (IntegrationException | BObStatusException  ex) {
            System.out.println(ex);
        }
        
        ds = getResourceBundle(Constants.DB_CONNECTION_PARAMS).getString("jndi_name");
        releaseID = getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("releaseidentifier");
    }
    
    /**
     * Listener for user clicks to the "login to mission control"
     * button on systemHome.xhtml
     * @return 
     */
     public String loginToMissionControl(){
         System.out.println("LoginBB.loginToMissionControl");
         System.out.print("External User: ");
         ExternalContext ec = getFacesContext().getExternalContext();
         System.out.println(ec.getRemoteUser());
         System.out.println(ec.getAuthType());
         System.out.println(ec.getSessionId(false));
         
        
        return "startInitiationProcess";
//           return "testInit";
           
    }

    /**
     * @return the publicUA
     */
    public UserAuthorized getPublicUA() {
        return publicUA;
    }

    /**
     * @param publicUA the publicUA to set
     */
    public void setPublicUA(UserAuthorized publicUA) {
        this.publicUA = publicUA;
    }

    /**
     * @return the patchRecord
     */
    public String getPatchRecord() {
        return patchRecord;
    }

    /**
     * @param patchRecord the patchRecord to set
     */
    public void setPatchRecord(String patchRecord) {
        this.patchRecord = patchRecord;
    }

    /**
     * @return the ds
     */
    public String getDs() {
        return ds;
    }

    /**
     * @param ds the ds to set
     */
    public void setDs(String ds) {
        this.ds = ds;
    }

    /**
     * @return the releaseID
     */
    public String getReleaseID() {
        return releaseID;
    }

    /**
     * @param releaseID the releaseID to set
     */
    public void setReleaseID(String releaseID) {
        this.releaseID = releaseID;
    }
    
}
