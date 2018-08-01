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

import com.tcvcog.tcvce.domain.IntegrationException;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.occupancy.integration.OccupancyInspectionIntegrator;
import com.tcvcog.tcvce.occupancy.entities.OccupancyInspection;
import java.io.Serializable;
import java.time.ZoneId;
import java.util.ArrayList;
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
public class OccupancyInspectionBB extends BackingBeanUtils implements Serializable {
    
    private ArrayList<OccupancyInspection> occupancyInspectionList;
    private OccupancyInspection selectedOccupancyInspection;
    private int formInspectionID;
    private int formPropertyUnitID;
    private int formLoginUserID;
    private java.util.Date formFirstInspectionDate;
    private boolean formFirstInspectionPass;
    private java.util.Date formSecondInspectionDate;
    private boolean formSecondInspectionPass;
    private boolean formResolved;
    private boolean formTotalFeePaid;
    private String formOccupancyInspectionNotes;
    
    /**
     * Creates a new instance of OccupancyInspectionBB
     */
    public OccupancyInspectionBB() {
    }
    
    public void editOccupancyInspection(ActionEvent e){
        if(getSelectedOccupancyInspection() != null){
            setFormInspectionID(selectedOccupancyInspection.getInspectionID());
//            setFormPropertyUnitID(selectedOccupancyInspection.getPropertyUnitID());
//            setFormLoginUserID(selectedOccupancyInspection.getLoginUserID());
            setFormFirstInspectionPass(selectedOccupancyInspection.isFirstInspectionPass());
            setFormSecondInspectionPass(selectedOccupancyInspection.isSecondInspectionPass());
//            setFormResolved(selectedOccupancyInspection.isResolved());
            setFormTotalFeePaid(selectedOccupancyInspection.isTotalFeePaid());
            setFormOccupancyInspectionNotes(selectedOccupancyInspection.getOccupancyInspectionNotes());
            //setFormOccupancyInspectionFeeNotes(selectedOccupancyInspectionFee.getOccupancyInspectionFeeNotes());
            /*
            Have to figure out what to do w/ setting dates...
            setFormOccupancyInspectionFeeEffDate(formOccupancyInspectionFeeEffDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime());
            */
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please select an occupancy inspection record to update", ""));
        }
    }
    
    public void deleteSelectedOccupancyInspection(ActionEvent e){
        OccupancyInspectionIntegrator oii = getOccupancyInspectionIntegrator();
        if(getSelectedOccupancyInspection() != null){
            try {
                oii.deleteOccupancyInspection(getSelectedOccupancyInspection());
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Occupancy inspection record deleted forever!", ""));
            } catch (IntegrationException ex) {
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Unable to delete occupancy inspection record--probably because it is used "
                                    + "somewhere in the database. Sorry.", 
                            "This category will always be with us."));
            }
            
        } else {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Please select a court entity from the table to delete", ""));
        }
    }

    public void commitOccupancyInspectionUpdates(ActionEvent e){
        OccupancyInspectionIntegrator oii = getOccupancyInspectionIntegrator();
        OccupancyInspection occInspection = selectedOccupancyInspection;
        
        occInspection.setInspectionID(formInspectionID);
//        occInspection.setPropertyUnitID(formPropertyUnitID);
//        occInspection.setLoginUserID(formLoginUserID);
        occInspection.setFirstInspectionDate(formFirstInspectionDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
        occInspection.setFirstInspectionPass(formFirstInspectionPass);
        occInspection.setSecondInspectionDate(formSecondInspectionDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
        occInspection.setSecondInspectionPass(formSecondInspectionPass);
//        occInspection.setResolved(formResolved);
        occInspection.setTotalFeePaid(formTotalFeePaid);
        occInspection.setOccupancyInspectionNotes(formOccupancyInspectionNotes);
        //oif.setOccupancyInspectionFeeNotes(formOccupancyInspectionFeeNotes);
        try{
            oii.updateOccupancyInspection(occInspection);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Occupancy Inspection Record updated!", ""));
        } catch (IntegrationException ex){
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to update occupancy inspection record in database.",
                    "This must be corrected by the System Administrator"));
        }
    }
    
    public String addOccupancyInspection(){
        OccupancyInspection o = new OccupancyInspection();
        OccupancyInspectionIntegrator oii =  getOccupancyInspectionIntegrator();
        
//        o.setPropertyUnitID(formPropertyUnitID);
//        o.setLoginUserID(formLoginUserID);
        o.setFirstInspectionDate(formFirstInspectionDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime());
        o.setFirstInspectionPass(formFirstInspectionPass);
        o.setSecondInspectionDate(formSecondInspectionDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
        o.setSecondInspectionPass(formSecondInspectionPass);
        o.setTotalFeePaid(formTotalFeePaid);
        o.setOccupancyInspectionNotes(formOccupancyInspectionNotes);
    
    try{
        oii.insertOccupanyInspection(o);
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Succcessfully added occupancy inspection to the database!", ""));
    }catch (IntegrationException ex) {
            System.out.println(ex.toString());
               getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Unable to add occupancy inspection to the database, my apologies!", "Check again..."));
            return "";
        }
    return "occupancyInspectionManage";
    }

    /**
     * @return the occupancyInspectionList
     */
    public ArrayList<OccupancyInspection> getOccupancyInspectionList() {
        try {
            OccupancyInspectionIntegrator oii = getOccupancyInspectionIntegrator();
            ArrayList<OccupancyInspection> oil = oii.getOccupancyInspectionList(new PropertyUnit());
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Unable to load Occupancy Inspection List",
                        "This must be corrected by the system administrator"));
        }
        if(occupancyInspectionList != null){
        return occupancyInspectionList;
        }else{
         occupancyInspectionList = new ArrayList();
         return occupancyInspectionList;
        }
    }

    /**
     * @param occupancyInspectionList the occupancyInspectionList to set
     */
    public void setOccupancyInspectionList(ArrayList<OccupancyInspection> occupancyInspectionList) {
        this.occupancyInspectionList = occupancyInspectionList;
    }

    /**
     * @return the formInspectionID
     */
    public int getFormInspectionID() {
        return formInspectionID;
    }

    /**
     * @param formInspectionID the formInspectionID to set
     */
    public void setFormInspectionID(int formInspectionID) {
        this.formInspectionID = formInspectionID;
    }

    /**
     * @return the formLoginUserID
     */
    public int getFormLoginUserID() {
        return formLoginUserID;
    }

    /**
     * @param formLoginUserID the formLoginUserID to set
     */
    public void setFormLoginUserID(int formLoginUserID) {
        this.formLoginUserID = formLoginUserID;
    }

    /**
     * @return the formFirstInspectionDate
     */
    public java.util.Date getFormFirstInspectionDate() {
        return formFirstInspectionDate;
    }

    /**
     * @param formFirstInspectionDate the formFirstInspectionDate to set
     */
    public void setFormFirstInspectionDate(java.util.Date formFirstInspectionDate) {
        this.formFirstInspectionDate = formFirstInspectionDate;
    }

    /**
     * @return the formFirstInspectionPass
     */
    public boolean isFormFirstInspectionPass() {
        return formFirstInspectionPass;
    }

    /**
     * @param formFirstInspectionPass the formFirstInspectionPass to set
     */
    public void setFormFirstInspectionPass(boolean formFirstInspectionPass) {
        this.formFirstInspectionPass = formFirstInspectionPass;
    }

    /**
     * @return the formSecondInspectionDate
     */
    public java.util.Date getFormSecondInspectionDate() {
        return formSecondInspectionDate;
    }

    /**
     * @param formSecondInspectionDate the formSecondInspectionDate to set
     */
    public void setFormSecondInspectionDate(java.util.Date formSecondInspectionDate) {
        this.formSecondInspectionDate = formSecondInspectionDate;
    }

    /**
     * @return the formSecondInspectionPass
     */
    public boolean isFormSecondInspectionPass() {
        return formSecondInspectionPass;
    }

    /**
     * @param formSecondInspectionPass the formSecondInspectionPass to set
     */
    public void setFormSecondInspectionPass(boolean formSecondInspectionPass) {
        this.formSecondInspectionPass = formSecondInspectionPass;
    }

    /**
     * @return the formResolved
     */
    public boolean isFormResolved() {
        return formResolved;
    }

    /**
     * @param formResolved the formResolved to set
     */
    public void setFormResolved(boolean formResolved) {
        this.formResolved = formResolved;
    }

    /**
     * @return the formTotalFeePaid
     */
    public boolean isFormTotalFeePaid() {
        return formTotalFeePaid;
    }

    /**
     * @param formTotalFeePaid the formTotalFeePaid to set
     */
    public void setFormTotalFeePaid(boolean formTotalFeePaid) {
        this.formTotalFeePaid = formTotalFeePaid;
    }

    /**
     * @return the formOccupancyInspectionNotes
     */
    public String getFormOccupancyInspectionNotes() {
        return formOccupancyInspectionNotes;
    }

    /**
     * @param formOccupancyInspectionNotes the formOccupancyInspectionNotes to set
     */
    public void setFormOccupancyInspectionNotes(String formOccupancyInspectionNotes) {
        this.formOccupancyInspectionNotes = formOccupancyInspectionNotes;
    }

    /**
     * @return the formPropertyUnitID
     */
    public int getFormPropertyUnitID() {
        return formPropertyUnitID;
    }

    /**
     * @param formPropertyUnitID the formPropertyUnitID to set
     */
    public void setFormPropertyUnitID(int formPropertyUnitID) {
        this.formPropertyUnitID = formPropertyUnitID;
    }

    /**
     * @return the selectedOccupancyInspection
     */
    public OccupancyInspection getSelectedOccupancyInspection() {
        return selectedOccupancyInspection;
    }

    /**
     * @param selectedOccupancyInspection the selectedOccupancyInspection to set
     */
    public void setSelectedOccupancyInspection(OccupancyInspection selectedOccupancyInspection) {
        this.selectedOccupancyInspection = selectedOccupancyInspection;
    }
    
}
