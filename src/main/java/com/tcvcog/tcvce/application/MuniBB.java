/*
 * Copyright (C) 2018 Turtle Creek Valley
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
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import java.io.Serializable;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Eric C. Darsow
 */
public class MuniBB extends BackingBeanUtils implements Serializable {

    private ArrayList<Municipality> muniList;
    private HashMap<String, Integer> muniMap;
    
    
    /**
     * Creates a new instance of muniBB
     */
    public MuniBB() {
    }

    /**
     * @return the muniList
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public ArrayList<Municipality> getMuniList() throws IntegrationException {
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        muniList = mi.getCompleteMuniList();
        
        return muniList;
    }

    /**
     * @return the muniMap
     */
    public HashMap<String, Integer> getMuniMap() {
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        try {
            muniMap = mi.getMunicipalityMap();
        } catch (IntegrationException ex) {
            System.out.println("MuniBB.getMuniMap " + ex.toString());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Cannot delete person." + ex.toString(), "Best not to delete folks anyway..."));
            
        }
        return muniMap;
    }

    /**
     * @param muniList the muniList to set
     */
    public void setMuniList(ArrayList<Municipality> muniList) {
        this.muniList = muniList;
    }

    /**
     * @param muniMap the muniMap to set
     */
    public void setMuniMap(HashMap<String, Integer> muniMap) {
        this.muniMap = muniMap;
    }
    
}
