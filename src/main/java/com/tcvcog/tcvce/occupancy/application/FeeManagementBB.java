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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import javax.annotation.PostConstruct;
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
    private Municipality formMuni;
    private Fee formFee;

    private boolean editing;

    /**
     * Creates a new instance of NewJSFManagedBean
     */
    public FeeManagementBB() {
    }

    @PostConstruct
    public void initBean(){
        formFee = new Fee();
        formFee.setEffectiveDate(LocalDateTime.now());
        formFee.setExpiryDate(LocalDateTime.now());
        
    }
    
    public void editFeeType(ActionEvent e) {
        if (getSelectedFeeType() != null) {
            editing = true;
            formFee.setOccupancyInspectionFeeID(selectedFeeType.getOccupancyInspectionFeeID());
            formFee.setMuni(selectedFeeType.getMuni());
            formFee.setFeeName(selectedFeeType.getFeeName());
            formFee.setFeeAmount(selectedFeeType.getFeeAmount());
            formFee.setNotes(selectedFeeType.getNotes());
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

    public void commitFeeUpdates(ActionEvent e) {
        OccupancyIntegrator oifi = getOccupancyIntegrator();
        PaymentIntegrator pi = getPaymentIntegrator();
        Fee oif = selectedFeeType;

        oif.setMuni(formFee.getMuni());
        oif.setFeeName(formFee.getFeeName());
        oif.setFeeAmount(formFee.getFeeAmount());
        oif.setEffectiveDate(formFee.getEffectiveDate());
        oif.setExpiryDate(formFee.getExpiryDate());
        oif.setNotes(formFee.getNotes());
        try {
            pi.updateOccupancyInspectionFee(oif);
            editing = false;
        } catch (IntegrationException ex) {
        }
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Occupancy Inspection Fee updated!", ""));
    }

    public void initializeNewFee(ActionEvent e) {

        editing = false;
        formFee = new Fee();
    }

    public String saveNewFeeType() {
        PaymentIntegrator pi = getPaymentIntegrator();
        Fee oif = new Fee();
        oif.setOccupancyInspectionFeeID(formFee.getOccupancyInspectionFeeID());
        oif.setMuni(getFormMuni());
        oif.setFeeName(formFee.getFeeName());
        oif.setFeeAmount(formFee.getFeeAmount());
        oif.setEffectiveDate(formFee.getEffectiveDate());
        oif.setExpiryDate(formFee.getExpiryDate());
        oif.setNotes(formFee.getNotes());
        try {
            pi.insertOccupancyInspectionFee(oif);
        } catch (IntegrationException ex) {
        }
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Successfully added occupancy inspection fee to database!", ""));

        return "occupancyInspectionFeeManage";

    }

    public void deleteSelectedFee(ActionEvent e) {
        PaymentIntegrator pi = getPaymentIntegrator();

        if (getSelectedFeeType() != null) {
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
        if (existingFeeTypeList != null) {
            return existingFeeTypeList;
        } else {
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
        return formFee.getOccupancyInspectionFeeID();
    }

    /**
     * @param formFeeID the formFeeID to set
     */
    public void setFormFeeID(int formFeeID) {
        this.formFee.setOccupancyInspectionFeeID(formFeeID);
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
        return formFee.getFeeName();
    }

    /**
     * @param formFeeName the formFeeName to set
     */
    public void setFormFeeName(String formFeeName) {
        this.formFee.setFeeName(formFeeName);
    }

    /**
     * @return the formFeeAmount
     */
    public double getFormFeeAmount() {
        return formFee.getFeeAmount();
    }

    /**
     * @param formFeeAmount the formFeeAmount to set
     */
    public void setFormFeeAmount(double formFeeAmount) {
        this.formFee.setFeeAmount(formFeeAmount);
    }

    /**
     * @return the formFeeEffDate
     */
    public java.util.Date getFormFeeEffDate() {
        return java.util.Date.from(formFee.getEffectiveDate()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    /**
     * @param formFeeEffDate the formFeeEffDate to set
     */
    public void setFormFeeEffDate(java.util.Date formFeeEffDate) {
        this.formFee.setEffectiveDate(
                LocalDateTime.ofInstant(formFeeEffDate.toInstant(),
                        ZoneId.systemDefault()));
    }

    /**
     * @return the formFeeExpDate
     */
    public java.util.Date getFormFeeExpDate() {
        return java.util.Date.from(formFee.getExpiryDate()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    /**
     * @param formFeeExpDate the formFeeExpDate to set
     */
    public void setFormFeeExpDate(java.util.Date formFeeExpDate) {
        this.formFee.setExpiryDate(
                LocalDateTime.ofInstant(formFeeExpDate.toInstant(),
                        ZoneId.systemDefault()));
    }

    /**
     * @return the formFeeNotes
     */
    public String getFormFeeNotes() {
        return formFee.getNotes();
    }

    /**
     * @param formFeeNotes the formFeeNotes to set
     */
    public void setFormFeeNotes(String formFeeNotes) {
        this.formFee.setNotes(formFeeNotes);
    }

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }

    public Fee getFormFee() {
        return formFee;
    }

    public void setFormFee(Fee formFee) {
        this.formFee = formFee;
    }

}
