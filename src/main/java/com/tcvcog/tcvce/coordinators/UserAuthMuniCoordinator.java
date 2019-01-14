/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author dom
 */
public class UserAuthMuniCoordinator extends BackingBeanUtils implements Serializable {
    
    public UserAuthMuniCoordinator() {
    
    }
    
    public ArrayList<Municipality> getUnauthorizedMunis(User u) throws IntegrationException{
        System.out.println("UserAuthMuniCoordinator.getUnauthorizedMunis for " + u);
        UserIntegrator ui = getUserIntegrator();
        ArrayList<Municipality> authMunis = ui.getUserAuthMunis(u.getUserID());
        
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        ArrayList<Municipality> munis = mi.getCompleteMuniList();        
        
        if(authMunis != null){
            for(Municipality authMuni:authMunis){
                munis.remove(authMuni);
            }
        }
        
        return munis;
    }
}
