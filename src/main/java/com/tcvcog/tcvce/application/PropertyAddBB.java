/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyWithLists;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

/**
 *
 * @author noah
 */
public class PropertyAddBB extends BackingBeanUtils implements Serializable{
    
    private PropertyWithLists prop;

    /**
     * Creates a new instance of PropertyAddBB
     */
    public PropertyAddBB() {
    }
    
    @PostConstruct
    public void initBean(){
        SessionBean sb = getSessionBean();
        this.prop = new PropertyWithLists();
        this.prop.setMuni(sb.getSessionMuni());
        this.prop.setMuniCode(sb.getSessionMuni().getMuniCode());
    }
    
    public String insertProp(){
        PropertyIntegrator pi = getPropertyIntegrator();
        try {
            getProp().setPropertyID(pi.insertProperty(getProp()));
//            getSessionBean().setActivePropWithLists(getProp());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Successfully inserted property with ID " + getProp().getPropertyID() 
                                + ", which is now your 'active property'", ""));
            return "properties";
            
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to insert property in database, sorry. ", 
                        "Please make sure all required fields are completed. "));
            return "";
        }
    }

    /**
     * @return the prop
     */
    public PropertyWithLists getProp() {
        return prop;
    }

    /**
     * @param prop the prop to set
     */
    public void setProp(PropertyWithLists prop) {
        this.prop = prop;
    }

    
    
}
