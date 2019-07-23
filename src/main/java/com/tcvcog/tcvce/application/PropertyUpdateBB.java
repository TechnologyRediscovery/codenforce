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


import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyWithLists;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Eric C. Darsow
 */
public class PropertyUpdateBB extends BackingBeanUtils implements Serializable {
    
    
    private PropertyWithLists currProp;
    private HashMap propertyUseTypeMap;

    /**
     * Creates a new instance of PropertyUpdateBB
     */
    public PropertyUpdateBB() {
    }
    @PostConstruct
    public void initBean(){
        PropertyIntegrator pi = getPropertyIntegrator();
        try {
            currProp = pi.getPropertyWithLists(getSessionBean().getSessionPropertyList().get(0).getPropertyID());
        } catch (IntegrationException | CaseLifecyleException ex) {
            System.out.println(ex);
        }
    }
    
    public String updateProperty(){
        PropertyIntegrator pi = getPropertyIntegrator();
        
        try {
            pi.updateProperty(currProp);
            getSessionBean().getSessionPropertyList().add(0, currProp);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Successfully updated property with ID " + getCurrProp().getPropertyID() 
                                + ", which is now your 'active property'", ""));
            return "propertyProfile";
            
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to update property in database, sorry. ", 
                        "Property updates are tricky--please inform your administrator. "));
            return "";
        }
    }
    

    /**
     * @return the currProp
     */
    public PropertyWithLists getCurrProp() {
        return currProp;
    }

    /**
     * @param currProp the currProp to set
     */
    public void setCurrProp(PropertyWithLists currProp) {
        this.currProp = currProp;
    }

    
    
    
    
    
}
