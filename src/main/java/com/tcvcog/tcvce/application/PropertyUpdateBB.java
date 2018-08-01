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
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Eric C. Darsow
 */
public class PropertyUpdateBB extends BackingBeanUtils implements Serializable {

    private Property property;
    
    private int formMuniCode;
    private String formParID;
    
    private String formLotAndBlock;
    private String formAddress;
    private String formPropertyUseType;
    // have not wired up property use type as an accessory table
//    private int formPropertyUseTypeID; 
    // also not needed
//    private HashMap propertyUseTypeMap;

    private boolean formRental;
    private boolean formMultiUnit;
    
    private String formUseGroup;
    private String formConstructionType;
    private String formCountyCode;
    
    private String formNotes;
    
    
    /**
     * Creates a new instance of PropertyUpdateBB
     */
    public PropertyUpdateBB() {
    }
    
    public String updateProperty(){
        PropertyIntegrator pi = getPropertyIntegrator();
        Property p = new Property();
        
        p.setPropertyID(property.getPropertyID());
        p.setParID(property.getParID());
        
        p.setLotAndBlock(formLotAndBlock);
        p.setAddress(formAddress);
        p.setPropertyUseType(formPropertyUseType);
        
        p.setUseGroup(formUseGroup);
        p.setConstructionType(formConstructionType);
        p.setCountyCode(formCountyCode);
        
        p.setNotes(formNotes);
        
        try {
            pi.updateProperty(p);
            // pull a new version of the property from the DB and store that in
            // the session to avoid errors in viewing any data that's not in the DB
            
            getSessionBean().setActiveProp(p);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Successfully updated property with ID " + property.getPropertyID() 
                                + ", which is now your 'active property'", ""));
            return "propertyProfile";
            
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Update property in database, sorry", 
                        "Property updates are tricky--please inform your administrator"));
            return "";
        }
    }

    /**
     * @return the formMuniCode
     */
    public int getFormMuniCode() {
        return formMuniCode;
    }

    /**
     * @return the formParID
     */
    public String getFormParID() {
        formParID = property.getParID();
        return formParID;
    }

    /**
     * @return the formLotAndBlock
     */
    public String getFormLotAndBlock() {
        formLotAndBlock = property.getLotAndBlock();
        return formLotAndBlock;
    }

    /**
     * @return the formAddress
     */
    public String getFormAddress() {
        formAddress = property.getAddress();
        return formAddress;
    }

   
    /**
     * @return the formUseGroup
     */
    public String getFormUseGroup() {
        formUseGroup = property.getUseGroup();
        return formUseGroup;
    }

    /**
     * @return the formConstructionType
     */
    public String getFormConstructionType() {
        formConstructionType = property.getConstructionType();
        return formConstructionType;
    }

    /**
     * @return the formCountyCode
     */
    public String getFormCountyCode() {
        formCountyCode = property.getCountyCode();
        return formCountyCode;
    }

    /**
     * @return the formNotes
     */
    public String getFormNotes() {
        formNotes = property.getNotes();
        return formNotes;
    }

    /**
     * @param formMuniCode the formMuniCode to set
     */
    public void setFormMuniCode(int formMuniCode) {
        this.formMuniCode = formMuniCode;
    }

    /**
     * @param formParID the formParID to set
     */
    public void setFormParID(String formParID) {
        this.formParID = formParID;
    }

    /**
     * @param formLotAndBlock the formLotAndBlock to set
     */
    public void setFormLotAndBlock(String formLotAndBlock) {
        this.formLotAndBlock = formLotAndBlock;
    }

    /**
     * @param formAddress the formAddress to set
     */
    public void setFormAddress(String formAddress) {
        this.formAddress = formAddress;
    }

    

    /**
     * @param formRental the formRental to set
     */
    public void setFormRental(boolean formRental) {
        this.formRental = formRental;
    }

    /**
     * @param formMultiUnit the formMultiUnit to set
     */
    public void setFormMultiUnit(boolean formMultiUnit) {
        this.formMultiUnit = formMultiUnit;
    }

    /**
     * @param formUseGroup the formUseGroup to set
     */
    public void setFormUseGroup(String formUseGroup) {
        this.formUseGroup = formUseGroup;
    }

    /**
     * @param formConstructionType the formConstructionType to set
     */
    public void setFormConstructionType(String formConstructionType) {
        this.formConstructionType = formConstructionType;
    }

    /**
     * @param formCountyCode the formCountyCode to set
     */
    public void setFormCountyCode(String formCountyCode) {
        this.formCountyCode = formCountyCode;
    }

    /**
     * @param formNotes the formNotes to set
     */
    public void setFormNotes(String formNotes) {
        this.formNotes = formNotes;
    }

    /**
     * @return the property
     */
    public Property getProperty() {
        
        property = getSessionBean().getActiveProp();
        return property;
    }

    /**
     * @param property the property to set
     */
    public void setProperty(Property property) {
        this.property = property;
    }

  


    /**
     * @return the currentPropUseTypeName
     */
    public String getCurrentPropUseTypeName() {
        
        formPropertyUseType = getSessionBean().getActiveProp().getPropertyUseType();
        return formPropertyUseType;
    }

    /**
     * @param currentPropUseType
     */
    public void setFormPropUseType(String currentPropUseType) {
        this.formPropertyUseType = currentPropUseType;
    }
    
    
    
    
}
