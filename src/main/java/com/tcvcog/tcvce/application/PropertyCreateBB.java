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

import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyDataHeavy;
import com.tcvcog.tcvce.entities.PropertyUseType;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author sylvia
 */
public class PropertyCreateBB 
        extends BackingBeanUtils{

    private PropertyDataHeavy currProp;
    private Property prop;
    private List<PropertyUseType> putList;
    
    /**
     * Creates a new instance of PropertyCreateBB
     */
    public PropertyCreateBB() {
    }
    
    
    
     
    @PostConstruct
    public void initBean(){
        PropertyIntegrator pi = getPropertyIntegrator();
        currProp = getSessionBean().getSessProperty();
         try {
             setPutList(pi.getPropertyUseTypeList());
         } catch (IntegrationException ex) {
             System.out.println(ex);
         }
        
    }
    
    
    
    public void initiatePropertyCreation(ActionEvent ev){
        PropertyCoordinator pc = getPropertyCoordinator();
        prop = pc.initProperty(getSessionBean().getSessMuni());
    }
    
    
    public void insertProp(ActionEvent ev){
        PropertyCoordinator pc = getPropertyCoordinator();
        UserAuthorized ua = getSessionBean().getSessUser();
        SystemCoordinator sc = getSystemCoordinator();
        int freshID = 0;
        try {
            freshID = pc.addProperty(prop, ua);
            prop = pc.getProperty(freshID); 
            getSessionBean().setSessProperty(pc.assemblePropertyDataHeavy(prop, ua));
            sc.logObjectView(ua, prop);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Successfully inserted property with ID " + freshID
                                + ", which is now your 'active property'", ""));
            
            
        } catch (IntegrationException | BObStatusException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to insert property in database, sorry. ", 
                        "Please make sure all required fields are completed. "));
        }
    }

    /**
     * @return the prop
     */
    public Property getProp() {
        return prop;
    }

    /**
     * @param prop the prop to set
     */
    public void setProp(Property prop) {
        this.prop = prop;
    }

    /**
     * @return the putList
     */
    public List<PropertyUseType> getPutList() {
        return putList;
    }

    /**
     * @param putList the putList to set
     */
    public void setPutList(List<PropertyUseType> putList) {
        this.putList = putList;
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
