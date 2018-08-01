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
import com.tcvcog.tcvce.occupancy.entities.OccupancyInspection;
import com.tcvcog.tcvce.occupancy.integration.OccupancyInspectionIntegrator;
import com.tcvcog.tcvce.occupancy.entities.OccInspecFee;
import java.io.Serializable;
import java.time.ZoneId;
import java.util.*;
import javax.faces.application.FacesMessage;
import javax.faces.bean.*;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Adam Gutonski
 */

@ViewScoped
public class OccupancyInspectionFeeBB extends BackingBeanUtils implements Serializable {
    
    private ArrayList<OccInspecFee> occupancyInspectionFeeList;
    private OccInspecFee selectedOccupancyInspectionFee;
    private int formOccupancyInspectionFeeID;
    private Municipality formMuni;
    private String formOccupancyInspectionFeeName;
    private double formOccupancyInspectionFeeAmount;
    private java.util.Date formOccupancyInspectionFeeEffDate;
    private java.util.Date formOccupancyInspectionFeeExpDate;
    private String formOccupancyInspectionFeeNotes;
    
    //create data fields for user editing/updating of occ. inspection fees
    private OccInspecFee newFormSelectedOccupancyInspectionFee;
    private int newFormOccupancyInspectionFeeID;
    private String newFormOccupancyInspectionFeeName;
    private double newFormOccupancyInspectionFeeAmount;
    private java.util.Date newFormOccupancyInspectionFeeEffDate;
    private java.util.Date newFormOccupancyInspectionFeeExpDate;
    private String newFormOccupancyInspectionFeeNotes;
    

    /**
     * Creates a new instance of NewJSFManagedBean
     */
    public OccupancyInspectionFeeBB() {
    }
    
    public void editOccupancyInspectionFee(ActionEvent e){
        if(getSelectedOccupancyInspectionFee() != null){
            setFormOccupancyInspectionFeeID(selectedOccupancyInspectionFee.getOccupancyInspectionFeeID());
            setFormMuni(selectedOccupancyInspectionFee.getMuni());
            setFormOccupancyInspectionFeeName(selectedOccupancyInspectionFee.getOccupancyInspectionFeeName());
            setFormOccupancyInspectionFeeAmount(selectedOccupancyInspectionFee.getOccupancyInspectionFeeAmount());
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
                    "Please select an occupancy inspection fee to update", ""));
        }
    }
    
    public void commitOccupancyInspectionFeeUpdates(ActionEvent e){
        OccupancyInspectionIntegrator oifi = getOccupancyInspectionIntegrator();
        OccInspecFee oif = selectedOccupancyInspectionFee;
        
        oif.setMuni(formMuni);
        oif.setOccupancyInspectionFeeName(formOccupancyInspectionFeeName);
        oif.setOccupancyInspectionFeeAmount(formOccupancyInspectionFeeAmount);
        oif.setOccupancyInspectionFeeEffDate(formOccupancyInspectionFeeEffDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
        oif.setOccupancyInspectionFeeExpDate(formOccupancyInspectionFeeExpDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
        //oif.setOccupancyInspectionFeeNotes(formOccupancyInspectionFeeNotes);
        try{
            oifi.updateOccupancyInspectionFee(oif);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Occupancy Inspection Fee updated!", ""));
        } catch (IntegrationException ex){
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to update occupancy inspection fee in database.",
                    "This must be corrected by the System Administrator"));
        }
    }
    
    public void addNewOccupancyInspectionFee(ActionEvent e){
        OccupancyInspectionIntegrator oifi = getOccupancyInspectionIntegrator();
        OccInspecFee oif = new OccInspecFee();
        
        oif.setOccupancyInspectionFeeID(newFormOccupancyInspectionFeeID);
        oif.setMuni(formMuni);
        oif.setOccupancyInspectionFeeName(newFormOccupancyInspectionFeeName);
        oif.setOccupancyInspectionFeeAmount(newFormOccupancyInspectionFeeAmount);
        oif.setOccupancyInspectionFeeEffDate(newFormOccupancyInspectionFeeEffDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
        oif.setOccupancyInspectionFeeExpDate(newFormOccupancyInspectionFeeExpDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
        oif.setOccupancyInspectionFeeNotes(newFormOccupancyInspectionFeeNotes);
        
        try{
            oifi.insertOccupancyInspectionFee(oif);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Occupancy Inspection Fee updated!", ""));
        } catch (IntegrationException ex){
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to add new Occupancy Inspection Fee to database.",
                    "This must be corrected by the System Administrator"));
        }
    
    }
    
    public String addOccupancyInspectionFee(){
        OccInspecFee oif = new OccInspecFee();
        OccupancyInspectionIntegrator oifi = new OccupancyInspectionIntegrator();
        oif.setOccupancyInspectionFeeID(formOccupancyInspectionFeeID);
        oif.setMuni(getFormMuni());
        oif.setOccupancyInspectionFeeName(formOccupancyInspectionFeeName);
        oif.setOccupancyInspectionFeeAmount(formOccupancyInspectionFeeAmount);
        oif.setOccupancyInspectionFeeEffDate(formOccupancyInspectionFeeEffDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
        oif.setOccupancyInspectionFeeExpDate(formOccupancyInspectionFeeExpDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
        oif.setOccupancyInspectionFeeNotes(formOccupancyInspectionFeeNotes);
        try {
            oifi.insertOccupancyInspectionFee(oif);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Successfully added occupancy inspection fee to database!", ""));
        } catch(IntegrationException ex) {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to add occupancy inspection fee to database, sorry!", "Check server print out..."));
            return "";
        }
        
        return "occupancyInspectionFeeManage";
        
        
    }
    
    public void deleteSelectedOccupancyInspectionFee(ActionEvent e){
        OccupancyInspectionIntegrator oifi = getOccupancyInspectionIntegrator();
        if(getSelectedOccupancyInspectionFee() != null){
            try {
                oifi.deleteOccupancyInspectionFee(getSelectedOccupancyInspectionFee());
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Occupancy inspection fee deleted forever!", ""));
            } catch (IntegrationException ex) {
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Unable to delete occupancy inspection fee--probably because it is used "
                                    + "somewhere in the database. Sorry.", 
                            "This category will always be with us."));
            }
            
        } else {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Please select an occupancy inspection fee from the table to delete", ""));
        }
    }

    /**
     * @return the occupancyInspectionFeeList
     */
    public ArrayList<OccInspecFee> getOccupancyInspectionFeeList() {
        try {
            OccupancyInspectionIntegrator oi = getOccupancyInspectionIntegrator();
            ArrayList<OccInspecFee> oil = oi.getOccupancyInspectionFeeList();
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Unable to load OccupancyInspectionFeeList",
                        "This must be corrected by the system administrator"));
        }
        if(occupancyInspectionFeeList != null){
        return occupancyInspectionFeeList;
        }else{
         occupancyInspectionFeeList = new ArrayList();
         return occupancyInspectionFeeList;
        }
    }

    /**
     * @param occupancyInspectionFeeList the occupancyInspectionFeeList to set
     */
    public void setOccupancyInspectionFeeList(ArrayList<OccInspecFee> occupancyInspectionFeeList) {
        this.occupancyInspectionFeeList = occupancyInspectionFeeList;
    }

    /**
     * @return the selectedOccupancyInspectionFee
     */
    public OccInspecFee getSelectedOccupancyInspectionFee() {
        return selectedOccupancyInspectionFee;
    }

    /**
     * @param selectedOccupancyInspectionFee the selectedOccupancyInspectionFee to set
     */
    public void setSelectedOccupancyInspectionFee(OccInspecFee selectedOccupancyInspectionFee) {
        this.selectedOccupancyInspectionFee = selectedOccupancyInspectionFee;
    }

    /**
     * @return the formOccupancyInspectionFeeID
     */
    public int getFormOccupancyInspectionFeeID() {
        return formOccupancyInspectionFeeID;
    }

    /**
     * @param formOccupancyInspectionFeeID the formOccupancyInspectionFeeID to set
     */
    public void setFormOccupancyInspectionFeeID(int formOccupancyInspectionFeeID) {
        this.formOccupancyInspectionFeeID = formOccupancyInspectionFeeID;
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

    /**
     * @return the formOccupancyInspectionFeeName
     */
    public String getFormOccupancyInspectionFeeName() {
        return formOccupancyInspectionFeeName;
    }

    /**
     * @param formOccupancyInspectionFeeName the formOccupancyInspectionFeeName to set
     */
    public void setFormOccupancyInspectionFeeName(String formOccupancyInspectionFeeName) {
        this.formOccupancyInspectionFeeName = formOccupancyInspectionFeeName;
    }

    /**
     * @return the formOccupancyInspectionFeeAmount
     */
    public double getFormOccupancyInspectionFeeAmount() {
        return formOccupancyInspectionFeeAmount;
    }

    /**
     * @param formOccupancyInspectionFeeAmount the formOccupancyInspectionFeeAmount to set
     */
    public void setFormOccupancyInspectionFeeAmount(double formOccupancyInspectionFeeAmount) {
        this.formOccupancyInspectionFeeAmount = formOccupancyInspectionFeeAmount;
    }

    /**
     * @return the formOccupancyInspectionFeeEffDate
     */
    public java.util.Date getFormOccupancyInspectionFeeEffDate() {
        return formOccupancyInspectionFeeEffDate;
    }

    /**
     * @param formOccupancyInspectionFeeEffDate the formOccupancyInspectionFeeEffDate to set
     */
    public void setFormOccupancyInspectionFeeEffDate(java.util.Date formOccupancyInspectionFeeEffDate) {
        this.formOccupancyInspectionFeeEffDate = formOccupancyInspectionFeeEffDate;
    }

    /**
     * @return the formOccupancyInspectionFeeExpDate
     */
    public java.util.Date getFormOccupancyInspectionFeeExpDate() {
        return formOccupancyInspectionFeeExpDate;
    }

    /**
     * @param formOccupancyInspectionFeeExpDate the formOccupancyInspectionFeeExpDate to set
     */
    public void setFormOccupancyInspectionFeeExpDate(java.util.Date formOccupancyInspectionFeeExpDate) {
        this.formOccupancyInspectionFeeExpDate = formOccupancyInspectionFeeExpDate;
    }

    /**
     * @return the formOccupancyInspectionFeeNotes
     */
    public String getFormOccupancyInspectionFeeNotes() {
        return formOccupancyInspectionFeeNotes;
    }

    /**
     * @param formOccupancyInspectionFeeNotes the formOccupancyInspectionFeeNotes to set
     */
    public void setFormOccupancyInspectionFeeNotes(String formOccupancyInspectionFeeNotes) {
        this.formOccupancyInspectionFeeNotes = formOccupancyInspectionFeeNotes;
    }

    /**
     * @return the newFormSelectedOccupancyInspectionFee
     */
    public OccInspecFee getNewFormSelectedOccupancyInspectionFee() {
        return newFormSelectedOccupancyInspectionFee;
    }

    /**
     * @param newFormSelectedOccupancyInspectionFee the newFormSelectedOccupancyInspectionFee to set
     */
    public void setNewFormSelectedOccupancyInspectionFee(OccInspecFee newFormSelectedOccupancyInspectionFee) {
        this.newFormSelectedOccupancyInspectionFee = newFormSelectedOccupancyInspectionFee;
    }

    /**
     * @return the newFormOccupancyInspectionFeeID
     */
    public int getNewFormOccupancyInspectionFeeID() {
        return newFormOccupancyInspectionFeeID;
    }

    /**
     * @param newFormOccupancyInspectionFeeID the newFormOccupancyInspectionFeeID to set
     */
    public void setNewFormOccupancyInspectionFeeID(int newFormOccupancyInspectionFeeID) {
        this.newFormOccupancyInspectionFeeID = newFormOccupancyInspectionFeeID;
    }

    /**
     * @return the newFormOccupancyInspectionFeeName
     */
    public String getNewFormOccupancyInspectionFeeName() {
        return newFormOccupancyInspectionFeeName;
    }

    /**
     * @param newFormOccupancyInspectionFeeName the newFormOccupancyInspectionFeeName to set
     */
    public void setNewFormOccupancyInspectionFeeName(String newFormOccupancyInspectionFeeName) {
        this.newFormOccupancyInspectionFeeName = newFormOccupancyInspectionFeeName;
    }

    /**
     * @return the newFormOccupancyInspectionFeeAmount
     */
    public double getNewFormOccupancyInspectionFeeAmount() {
        return newFormOccupancyInspectionFeeAmount;
    }

    /**
     * @param newFormOccupancyInspectionFeeAmount the newFormOccupancyInspectionFeeAmount to set
     */
    public void setNewFormOccupancyInspectionFeeAmount(double newFormOccupancyInspectionFeeAmount) {
        this.newFormOccupancyInspectionFeeAmount = newFormOccupancyInspectionFeeAmount;
    }

    /**
     * @return the newFormOccupancyInspectionFeeEffDate
     */
    public java.util.Date getNewFormOccupancyInspectionFeeEffDate() {
        return newFormOccupancyInspectionFeeEffDate;
    }

    /**
     * @param newFormOccupancyInspectionFeeEffDate the newFormOccupancyInspectionFeeEffDate to set
     */
    public void setNewFormOccupancyInspectionFeeEffDate(java.util.Date newFormOccupancyInspectionFeeEffDate) {
        this.newFormOccupancyInspectionFeeEffDate = newFormOccupancyInspectionFeeEffDate;
    }

    /**
     * @return the newFormOccupancyInspectionFeeExpDate
     */
    public java.util.Date getNewFormOccupancyInspectionFeeExpDate() {
        return newFormOccupancyInspectionFeeExpDate;
    }

    /**
     * @param newFormOccupancyInspectionFeeExpDate the newFormOccupancyInspectionFeeExpDate to set
     */
    public void setNewFormOccupancyInspectionFeeExpDate(java.util.Date newFormOccupancyInspectionFeeExpDate) {
        this.newFormOccupancyInspectionFeeExpDate = newFormOccupancyInspectionFeeExpDate;
    }

    /**
     * @return the newFormOccupancyInspectionFeeNotes
     */
    public String getNewFormOccupancyInspectionFeeNotes() {
        return newFormOccupancyInspectionFeeNotes;
    }

    /**
     * @param newFormOccupancyInspectionFeeNotes the newFormOccupancyInspectionFeeNotes to set
     */
    public void setNewFormOccupancyInspectionFeeNotes(String newFormOccupancyInspectionFeeNotes) {
        this.newFormOccupancyInspectionFeeNotes = newFormOccupancyInspectionFeeNotes;
    }
    
}
