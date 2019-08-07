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
import com.tcvcog.tcvce.entities.Fee;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import com.tcvcog.tcvce.occupancy.integration.PaymentIntegrator;
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
public class FeeManagementBB extends BackingBeanUtils implements Serializable {
    
    private ArrayList<Fee> existingFeeTypeList;
    private Fee selectedFeeType;
    private int formFeeID;
    private Municipality formMuni;
    private String formFeeName;
    private double formFeeAmount;
    private java.util.Date formFeeEffDate;
    private java.util.Date formFeeExpDate;
    private String formFeeNotes;
    
    //create data fields for user editing/updating of occ. inspection fees
    private Fee newFormSelectedFee;
    private int newFormFeeID;
    private String newFormFeeName;
    private double newFormFeeAmount;
    private java.util.Date newFormFeeEffDate;
    private java.util.Date newFormFeeExpDate;
    private String newFormFeeNotes;
    private boolean editing;
    

    /**
     * Creates a new instance of NewJSFManagedBean
     */
    public FeeManagementBB() {
    }
    
    public void editFeeType(ActionEvent e){
        if(getSelectedFeeType() != null){
            editing = true;
            formFeeID = selectedFeeType.getOccupancyInspectionFeeID();
            formMuni = selectedFeeType.getMuni();
            formFeeName = selectedFeeType.getFeeName();
            formFeeAmount = selectedFeeType.getFeeAmount();
            //formOccupancyInspectionFeeNotes = selectedFeeType.getOccupancyInspectionFeeNotes();
            /*
            Have to figure out what to do w/ setting dates...
            setFormOccupancyInspectionFeeEffDate(formFeeEffDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime());
            */
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please select an occupancy inspection fee to update", ""));
        }
    }
    
    public void commitFeeUpdates(ActionEvent e){
        OccupancyIntegrator oifi = getOccupancyIntegrator();
        PaymentIntegrator pi = getPaymentIntegrator();
        Fee oif = selectedFeeType;
        
        oif.setMuni(formMuni);
        oif.setFeeName(formFeeName);
        oif.setFeeAmount(formFeeAmount);
        oif.setEffectiveDate(formFeeEffDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
        oif.setExpiryDate(formFeeExpDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
        try {
            //oif.setOccupancyInspectionFeeNotes(formFeeNotes);
            pi.updateOccupancyInspectionFee(oif);
            editing = false;
        } catch (IntegrationException ex) {
        }
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Occupancy Inspection Fee updated!", ""));
    }
    
    public void initializeNewFee(ActionEvent e){
        
            editing = false;
            formFeeID = 0;
            formMuni = new Municipality();
            formFeeName = "";
            formFeeAmount = 0.0;
            formFeeNotes = "";
            /*
            Have to figure out what to do w/ setting dates...
            setFormOccupancyInspectionFeeEffDate(formFeeEffDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime());
            */
        
    }
    
    public void addNewFeeType(ActionEvent e){
        PaymentIntegrator pi = getPaymentIntegrator();
        Fee oif = new Fee();
        
        oif.setOccupancyInspectionFeeID(newFormFeeID);
        oif.setMuni(formMuni);
        oif.setFeeName(newFormFeeName);
        oif.setFeeAmount(newFormFeeAmount);
        oif.setEffectiveDate(newFormFeeEffDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
        oif.setExpiryDate(newFormFeeExpDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
        oif.setNotes(newFormFeeNotes);
        
        try {
            pi.insertOccupancyInspectionFee(oif);
        } catch (IntegrationException ex) {
        }
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Occupancy Inspection Fee updated!", ""));
    
    }
    
    public String saveNewFeeType(){
        PaymentIntegrator pi = getPaymentIntegrator();
        Fee oif = new Fee();
        oif.setOccupancyInspectionFeeID(formFeeID);
        oif.setMuni(getFormMuni());
        oif.setFeeName(formFeeName);
        oif.setFeeAmount(formFeeAmount);
        oif.setEffectiveDate(formFeeEffDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
        oif.setExpiryDate(formFeeExpDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
        oif.setNotes(formFeeNotes);
        try {
            pi.insertOccupancyInspectionFee(oif);
        } catch (IntegrationException ex) {
        }
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Successfully added occupancy inspection fee to database!", ""));
        
        return "occupancyInspectionFeeManage";
        
        
    }
    
    public void deleteSelectedFee(ActionEvent e){
        PaymentIntegrator pi = getPaymentIntegrator();
        
        if(getSelectedFeeType() != null){
            try {
                pi.deleteOccupancyInspectionFee(getSelectedFeeType());
            } catch (IntegrationException ex) {
            }
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Occupancy inspection fee deleted forever!", ""));
            
        } else {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Please select an occupancy inspection fee from the table to delete", ""));
        }
    }

    /**
     * @return the existingFeeTypeList
     */
    public ArrayList<Fee> getFeeTypeList() {
        PaymentIntegrator pi = getPaymentIntegrator();
        try {
            existingFeeTypeList = pi.getOccupancyInspectionFeeList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        if(existingFeeTypeList != null){
        return existingFeeTypeList;
        }else{
         existingFeeTypeList = new ArrayList();
         return existingFeeTypeList;
        }
    }

    /**
     * @param existingFeeTypeList the existingFeeTypeList to set
     */
    public void setExistingFeeTypeList(ArrayList<Fee> existingFeeTypeList) {
        this.existingFeeTypeList = existingFeeTypeList;
    }

    /**
     * @return the selectedFeeType
     */
    public Fee getSelectedFeeType() {
        return selectedFeeType;
    }

    /**
     * @param selectedFeeType the selectedFeeType to set
     */
    public void setSelectedFeeType(Fee selectedFeeType) {
        this.selectedFeeType = selectedFeeType;
    }

    /**
     * @return the formFeeID
     */
    public int getFormFeeID() {
        return formFeeID;
    }

    /**
     * @param formFeeID the formFeeID to set
     */
    public void setFormFeeID(int formFeeID) {
        this.formFeeID = formFeeID;
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
     * @return the formFeeName
     */
    public String getFormFeeName() {
        return formFeeName;
    }

    /**
     * @param formFeeName the formFeeName to set
     */
    public void setFormFeeName(String formFeeName) {
        this.formFeeName = formFeeName;
    }

    /**
     * @return the formFeeAmount
     */
    public double getFormFeeAmount() {
        return formFeeAmount;
    }

    /**
     * @param formFeeAmount the formFeeAmount to set
     */
    public void setFormFeeAmount(double formFeeAmount) {
        this.formFeeAmount = formFeeAmount;
    }

    /**
     * @return the formFeeEffDate
     */
    public java.util.Date getFormFeeEffDate() {
        return formFeeEffDate;
    }

    /**
     * @param formFeeEffDate the formFeeEffDate to set
     */
    public void setFormFeeEffDate(java.util.Date formFeeEffDate) {
        this.formFeeEffDate = formFeeEffDate;
    }

    /**
     * @return the formFeeExpDate
     */
    public java.util.Date getFormFeeExpDate() {
        return formFeeExpDate;
    }

    /**
     * @param formFeeExpDate the formFeeExpDate to set
     */
    public void setFormFeeExpDate(java.util.Date formFeeExpDate) {
        this.formFeeExpDate = formFeeExpDate;
    }

    /**
     * @return the formFeeNotes
     */
    public String getFormFeeNotes() {
        return formFeeNotes;
    }

    /**
     * @param formFeeNotes the formFeeNotes to set
     */
    public void setFormFeeNotes(String formFeeNotes) {
        this.formFeeNotes = formFeeNotes;
    }

    /**
     * @return the newFormSelectedFee
     */
    public Fee getNewFormSelectedFee() {
        return newFormSelectedFee;
    }

    /**
     * @param newFormSelectedFee the newFormSelectedFee to set
     */
    public void setNewFormSelectedFee(Fee newFormSelectedFee) {
        this.newFormSelectedFee = newFormSelectedFee;
    }

    /**
     * @return the newFormFeeID
     */
    public int getNewFormFeeID() {
        return newFormFeeID;
    }

    /**
     * @param newFormFeeID the newFormFeeID to set
     */
    public void setNewFormFeeID(int newFormFeeID) {
        this.newFormFeeID = newFormFeeID;
    }

    /**
     * @return the newFormFeeName
     */
    public String getNewFormFeeName() {
        return newFormFeeName;
    }

    /**
     * @param newFormFeeName the newFormFeeName to set
     */
    public void setNewFormFeeName(String newFormFeeName) {
        this.newFormFeeName = newFormFeeName;
    }

    /**
     * @return the newFormFeeAmount
     */
    public double getNewFormFeeAmount() {
        return newFormFeeAmount;
    }

    /**
     * @param newFormFeeAmount the newFormFeeAmount to set
     */
    public void setNewFormFeeAmount(double newFormFeeAmount) {
        this.newFormFeeAmount = newFormFeeAmount;
    }

    /**
     * @return the newFormFeeEffDate
     */
    public java.util.Date getNewFormFeeEffDate() {
        return newFormFeeEffDate;
    }

    /**
     * @param newFormFeeEffDate the newFormFeeEffDate to set
     */
    public void setNewFormFeeEffDate(java.util.Date newFormFeeEffDate) {
        this.newFormFeeEffDate = newFormFeeEffDate;
    }

    /**
     * @return the newFormFeeExpDate
     */
    public java.util.Date getNewFormFeeExpDate() {
        return newFormFeeExpDate;
    }

    /**
     * @param newFormFeeExpDate the newFormFeeExpDate to set
     */
    public void setNewFormFeeExpDate(java.util.Date newFormFeeExpDate) {
        this.newFormFeeExpDate = newFormFeeExpDate;
    }

    /**
     * @return the newFormFeeNotes
     */
    public String getNewFormFeeNotes() {
        return newFormFeeNotes;
    }

    /**
     * @param newFormFeeNotes the newFormFeeNotes to set
     */
    public void setNewFormFeeNotes(String newFormFeeNotes) {
        this.newFormFeeNotes = newFormFeeNotes;
    }

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }
    
}
