/*
 * Copyright (C) 2018 Adam Gutonski
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
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.occupancy.integration.OccupancyPermitIntegrator;
import com.tcvcog.tcvce.occupancy.entities.OccPermitType;
import java.io.Serializable;
import java.util.ArrayList;
import javax.inject.Named;
import javax.enterprise.context.Dependent;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Adam Gutonski
 */

@ViewScoped
public class OccupancyPermitTypeBB extends BackingBeanUtils implements Serializable {
    
    private ArrayList<OccPermitType> occupancyPermitTypeList;
    private OccPermitType selectedOccupancyPermitType;
    private int formOccupancyPermitTypeID;
    private int formOccupancyPermitTypeMuniCodeID;
    private String formOccupancyPermitTypeName;
    private String formOccupancyPermitTypeDescription; 
    
    //create data fields for user editing/updating of permit types
    private OccPermitType newFormSelectedOccupancyPermitType;
    private int newFormOccupancyPermitTypeID;
    private Municipality formMuni;
    //private int newFormOccupancyPermitTypeMuniCodeID;
    private String newFormOccupancyPermitTypeName;
    private String newFormOccupancyPermitTypeDescription; 
    
    /**
     * Creates a new instance of OccupancyPermitTypeBB
     */
    public OccupancyPermitTypeBB() {
    }
    
    public void addNewOccupancyPermitType(ActionEvent e){
        OccupancyPermitIntegrator oi = getOccupancyPermitIntegrator();
        OccPermitType o = new OccPermitType();
        
        o.setOccupancyPermitTypeID(newFormOccupancyPermitTypeID);
        o.setMuni(formMuni);
        o.setOccupancyPermitTypeName(newFormOccupancyPermitTypeName);
        o.setOccupancyPermitTypeDescription(newFormOccupancyPermitTypeDescription);
        
        try{
            oi.insertOccupancyPermitType(o);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Occupancy Permit Type updated!", ""));
        } catch (IntegrationException ex){
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to add new occupancy permit type to database.",
                    "This must be corrected by the System Administrator"));
        }
    
    }
    
     public void deleteSelectedOccupancyPermitType(ActionEvent e){
        OccupancyPermitIntegrator opti = getOccupancyPermitIntegrator();
        if(getSelectedOccupancyPermitType() != null){
            try {
                opti.deleteOccupancyPermitType(getSelectedOccupancyPermitType());
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Occupancy permit type deleted forever!", ""));
            } catch (IntegrationException ex) {
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Unable to delete occupancy permit type--probably because it is used "
                                    + "somewhere in the database. Sorry.", 
                            "This category will always be with us."));
            }
            
        } else {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Please select an occupancy inspection fee from the table to delete", ""));
        }
    }
    
    
    public void editOccupancyPermitType(ActionEvent e) {
        if (getSelectedOccupancyPermitType() != null) {
            //setFormOccupancyPermitTypeID(selectedOccupancyPermitType.getOccupancyPermitTypeID());
            //setFormOccupancyPermitTypeMuniCodeID(selectedOccupancyPermitType.getOccupancyPermitTypeMuniCodeID());
            setFormOccupancyPermitTypeName(selectedOccupancyPermitType.getOccupancyPermitTypeName());
            setFormOccupancyPermitTypeDescription(selectedOccupancyPermitType.getOccupancyPermitTypeDescription());
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select an occupancy permit type to update", ""));
        }
    }
    
    public void commitUpdates(ActionEvent e){
        OccupancyPermitIntegrator oi = getOccupancyPermitIntegrator();
        OccPermitType o = selectedOccupancyPermitType;
        
        //o.setOccupancyPermitTypeID(getFormOccupancyPermitTypeID());
        //o.setOccupancyPermitTypeMuniCodeID(getFormOccupancyPermitTypeMuniCodeID());
        o.setOccupancyPermitTypeName(formOccupancyPermitTypeName);
        o.setOccupancyPermitTypeDescription(formOccupancyPermitTypeDescription);
        
        try{
            oi.updateOccupancyPermitType(o);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Occupancy Permit Type updated!", ""));
        } catch (IntegrationException ex){
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to update occupancy permit type in database.",
                    "This must be corrected by the System Administrator"));
        }
    }
    
    
    public String addOccupancyPermitType(){
        OccPermitType o = new OccPermitType();
        OccupancyPermitIntegrator oi = new OccupancyPermitIntegrator();
        o.setOccupancyPermitTypeID(formOccupancyPermitTypeID);
        o.setMuni(getFormMuni());
        o.setOccupancyPermitTypeName(formOccupancyPermitTypeName);
        o.setOccupancyPermitTypeDescription(formOccupancyPermitTypeDescription);
        
        try {
            oi.insertOccupancyPermitType(o);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Successfully added occupancy permit type to database!", ""));
        } catch(IntegrationException ex) {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to add occupancy permit type to database, sorry!", "Check server print out..."));
            return "";
        }
        
        return "occupancyPermitTypeManage";
        
        
    }
    
    
    
    /**
     * @return the occupancyPermitTypeList
     */
    public ArrayList<OccPermitType> getOccupancyPermitTypeList() {
        try {
            OccupancyPermitIntegrator oi = getOccupancyPermitIntegrator();
            occupancyPermitTypeList = oi.getOccupancyPermitTypeList();
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Unable to load OccupancyPermitTypeList",
                        "This must be corrected by the system administrator"));
        }
        if(occupancyPermitTypeList != null){
        return occupancyPermitTypeList;
        }else{
         occupancyPermitTypeList = new ArrayList();
         return occupancyPermitTypeList;
        }
    }    
        

    /**
     * @param occupancyPermitTypeList the occupancyPermitTypeList to set
     */
    public void setOccupancyPermitTypeList(ArrayList<OccPermitType> occupancyPermitTypeList) {
        this.occupancyPermitTypeList = occupancyPermitTypeList;
    }

    /**
     * @return the formOccupancyPermitTypeID
     */
    public int getFormOccupancyPermitTypeID() {
        return formOccupancyPermitTypeID;
    }

    /**
     * @param formOccupancyPermitTypeID the formOccupancyPermitTypeID to set
     */
    public void setFormOccupancyPermitTypeID(int formOccupancyPermitTypeID) {
        this.formOccupancyPermitTypeID = formOccupancyPermitTypeID;
    }

    /**
     * @return the formOccupancyPermitTypeMuniCodeID
     */
    public int getFormOccupancyPermitTypeMuniCodeID() {
        return formOccupancyPermitTypeMuniCodeID;
    }

    /**
     * @param formOccupancyPermitTypeMuniCodeID the formOccupancyPermitTypeMuniCodeID to set
     */
    public void setFormOccupancyPermitTypeMuniCodeID(int formOccupancyPermitTypeMuniCodeID) {
        this.formOccupancyPermitTypeMuniCodeID = formOccupancyPermitTypeMuniCodeID;
    }

    /**
     * @return the formOccupancyPermitTypeName
     */
    public String getFormOccupancyPermitTypeName() {
        return formOccupancyPermitTypeName;
    }

    /**
     * @param formOccupancyPermitTypeName the formOccupancyPermitTypeName to set
     */
    public void setFormOccupancyPermitTypeName(String formOccupancyPermitTypeName) {
        this.formOccupancyPermitTypeName = formOccupancyPermitTypeName;
    }

    /**
     * @return the formOccupancyPermitTypeDescription
     */
    public String getFormOccupancyPermitTypeDescription() {
        return formOccupancyPermitTypeDescription;
    }

    /**
     * @param formOccupancyPermitTypeDescription the formOccupancyPermitTypeDescription to set
     */
    public void setFormOccupancyPermitTypeDescription(String formOccupancyPermitTypeDescription) {
        this.formOccupancyPermitTypeDescription = formOccupancyPermitTypeDescription;
    }

    /**
     * @return the selectedOccupancyPermitType
     */
    public OccPermitType getSelectedOccupancyPermitType() {
        return selectedOccupancyPermitType;
    }

    /**
     * @param selectedOccupancyPermitType the selectedOccupancyPermitType to set
     */
    public void setSelectedOccupancyPermitType(OccPermitType selectedOccupancyPermitType) {
        this.selectedOccupancyPermitType = selectedOccupancyPermitType;
    }

    /**
     * @return the newFormSelectedOccupancyPermitType
     */
    public OccPermitType getNewFormSelectedOccupancyPermitType() {
        return newFormSelectedOccupancyPermitType;
    }

    /**
     * @param newFormSelectedOccupancyPermitType the newFormSelectedOccupancyPermitType to set
     */
    public void setNewFormSelectedOccupancyPermitType(OccPermitType newFormSelectedOccupancyPermitType) {
        this.newFormSelectedOccupancyPermitType = newFormSelectedOccupancyPermitType;
    }

    /**
     * @return the newFormOccupancyPermitTypeID
     */
    public int getNewFormOccupancyPermitTypeID() {
        return newFormOccupancyPermitTypeID;
    }

    /**
     * @param newFormOccupancyPermitTypeID the newFormOccupancyPermitTypeID to set
     */
    public void setNewFormOccupancyPermitTypeID(int newFormOccupancyPermitTypeID) {
        this.newFormOccupancyPermitTypeID = newFormOccupancyPermitTypeID;
    }

    /**
     * @return the newFormOccupancyPermitTypeMuniCodeID
     
    public int getNewFormOccupancyPermitTypeMuniCodeID() {
        return newFormOccupancyPermitTypeMuniCodeID;
    }
    * */

    /**
     * @param newFormOccupancyPermitTypeMuniCodeID the newFormOccupancyPermitTypeMuniCodeID to set
     
    public void setNewFormOccupancyPermitTypeMuniCodeID(int newFormOccupancyPermitTypeMuniCodeID) {
        this.newFormOccupancyPermitTypeMuniCodeID = newFormOccupancyPermitTypeMuniCodeID;
    }
    * */

    /**
     * @return the newFormOccupancyPermitTypeName
     */
    public String getNewFormOccupancyPermitTypeName() {
        return newFormOccupancyPermitTypeName;
    }

    /**
     * @param newFormOccupancyPermitTypeName the newFormOccupancyPermitTypeName to set
     */
    public void setNewFormOccupancyPermitTypeName(String newFormOccupancyPermitTypeName) {
        this.newFormOccupancyPermitTypeName = newFormOccupancyPermitTypeName;
    }

    /**
     * @return the newFormOccupancyPermitTypeDescription
     */
    public String getNewFormOccupancyPermitTypeDescription() {
        return newFormOccupancyPermitTypeDescription;
    }

    /**
     * @param newFormOccupancyPermitTypeDescription the newFormOccupancyPermitTypeDescription to set
     */
    public void setNewFormOccupancyPermitTypeDescription(String newFormOccupancyPermitTypeDescription) {
        this.newFormOccupancyPermitTypeDescription = newFormOccupancyPermitTypeDescription;
    }

    /**
     * @return the formMuni
     */
    public Municipality getFormMuni() {
        return formMuni;
    }

    /**
     * @param formMuni the formMuni to set
     */
    public void setFormMuni(Municipality formMuni) {
        this.formMuni = formMuni;
    }
    
 

}
