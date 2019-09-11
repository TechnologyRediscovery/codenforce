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
import com.tcvcog.tcvce.entities.MoneyOccPeriodFeeAssigned;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
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

    //feeManage.xhtml fields
    private ArrayList<Fee> existingFeeTypeList;
    private Fee selectedFeeType;
    private Municipality formMuni;

    //feeManage.xhtml fields
    private Fee formFee;
    private Fee selectedFee;
    private ArrayList<Fee> feeList;
    private ArrayList<Fee> filteredFeeList;

    private MoneyOccPeriodFeeAssigned occPeriodFormFee;
    private MoneyOccPeriodFeeAssigned selectedOccPeriodFee;
    private ArrayList<MoneyOccPeriodFeeAssigned> occPeriodFeeList;
    private ArrayList<MoneyOccPeriodFeeAssigned> occPeriodFilteredFeeList;

    private boolean editing;
    private String redirTo;
    private boolean waived;

    /**
     * Creates a new instance of NewJSFManagedBean
     */
    public FeeManagementBB() {
    }

    @PostConstruct
    public void initBean() {
        formFee = new Fee();
        formFee.setEffectiveDate(LocalDateTime.now());
        formFee.setExpiryDate(LocalDateTime.now());

        occPeriodFormFee = new MoneyOccPeriodFeeAssigned();

        if (getSessionBean().getFeeRedirTo() != null) {
            redirTo = getSessionBean().getFeeRedirTo();

            PaymentIntegrator pi = getPaymentIntegrator();

            OccPeriod period = getSessionBean().getFeeManagementOccPeriod();
            
            if (period != null) {
                
                try {
                    occPeriodFeeList = (ArrayList<MoneyOccPeriodFeeAssigned>) pi.getFeeAssigned(period);
                    feeList = (ArrayList<Fee>) pi.getFeeTypeList(period.getType());
                } catch (IntegrationException ex) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Oops! We encountered a problem trying to fetch the fee list!", ""));
                }

            }

        }

    }

    public String finishAndRedir() {
        getSessionBean().setFeeRedirTo(null);

        return redirTo;
    }

    public void deleteSelectedOccPeriodFee(ActionEvent e) {

        PaymentIntegrator pi = getPaymentIntegrator();

        if (selectedOccPeriodFee != null) {
            try {
                pi.deleteOccPeriodFee(selectedOccPeriodFee);
            } catch (IntegrationException ex) {
                System.out.println(ex.toString());
            }
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Fee deleted forever!", ""));

        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a fee from the table to delete", ""));
        }

    }

    public void editOccPeriodFee(ActionEvent e) {
        if (selectedOccPeriodFee != null) {
            occPeriodFormFee.setOccPerAssignedFeeID(selectedOccPeriodFee.getOccPerAssignedFeeID());
            occPeriodFormFee.setOccPeriodID(selectedOccPeriodFee.getOccPeriodID());
            occPeriodFormFee.setOccPeriodTypeID(selectedOccPeriodFee.getOccPeriodTypeID());
            occPeriodFormFee.setPaymentList(selectedOccPeriodFee.getPaymentList());
            occPeriodFormFee.setMoneyFeeAssigned(selectedOccPeriodFee.getMoneyFeeAssigned());
            occPeriodFormFee.setAssignedBy(selectedOccPeriodFee.getAssignedBy());
            occPeriodFormFee.setWaivedBy(selectedOccPeriodFee.getWaivedBy());
            occPeriodFormFee.setLastModified(selectedOccPeriodFee.getLastModified());
            occPeriodFormFee.setReducedBy(selectedOccPeriodFee.getReducedBy());
            occPeriodFormFee.setReducedByUser(selectedOccPeriodFee.getReducedByUser());
            occPeriodFormFee.setNotes(selectedOccPeriodFee.getNotes());
            occPeriodFormFee.setFee(selectedOccPeriodFee.getFee());
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select an assigned fee to update", ""));
        }

    }

    public void intializeNewOccPeriodFee(ActionEvent e) {

        editing = false;
        occPeriodFormFee = new MoneyOccPeriodFeeAssigned();

    }

    public void editFeeType(ActionEvent e) {
        if (getSelectedFeeType() != null) {
            editing = true;
            formFee.setOccupancyInspectionFeeID(selectedFeeType.getOccupancyInspectionFeeID());
            formFee.setMuni(selectedFeeType.getMuni());
            formFee.setName(selectedFeeType.getName());
            formFee.setAmount(selectedFeeType.getAmount());
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
        oif.setName(formFee.getName());
        oif.setAmount(formFee.getAmount());
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
        oif.setName(formFee.getName());
        oif.setAmount(formFee.getAmount());
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
                System.out.println(ex.toString());
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
        return formFee.getName();
    }

    /**
     * @param formFeeName the formFeeName to set
     */
    public void setFormFeeName(String formFeeName) {
        this.formFee.setName(formFeeName);
    }

    /**
     * @return the formFeeAmount
     */
    public double getFormFeeAmount() {
        return formFee.getAmount();
    }

    /**
     * @param formFeeAmount the formFeeAmount to set
     */
    public void setFormFeeAmount(double formFeeAmount) {
        this.formFee.setAmount(formFeeAmount);
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

    public Fee getSelectedFee() {
        return selectedFee;
    }

    public void setSelectedFee(Fee selectedFee) {
        this.selectedFee = selectedFee;
    }

    public ArrayList<Fee> getFeeList() {
        return feeList;
    }

    public void setFeeList(ArrayList<Fee> feeList) {
        this.feeList = feeList;
    }

    public MoneyOccPeriodFeeAssigned getOccPeriodFormFee() {
        return occPeriodFormFee;
    }

    public void setOccPeriodFormFee(MoneyOccPeriodFeeAssigned occPeriodFormFee) {
        this.occPeriodFormFee = occPeriodFormFee;
    }

    public MoneyOccPeriodFeeAssigned getSelectedOccPeriodFee() {
        return selectedOccPeriodFee;
    }

    public void setSelectedOccPeriodFee(MoneyOccPeriodFeeAssigned selectedOccPeriodFee) {
        this.selectedOccPeriodFee = selectedOccPeriodFee;
    }

    public ArrayList<MoneyOccPeriodFeeAssigned> getOccPeriodFeeList() {
        return occPeriodFeeList;
    }

    public void setOccPeriodFeeList(ArrayList<MoneyOccPeriodFeeAssigned> occPeriodFeeList) {
        this.occPeriodFeeList = occPeriodFeeList;
    }

    public String getRedirTo() {
        return redirTo;
    }

    public void setRedirTo(String redirTo) {
        this.redirTo = redirTo;
    }

    public ArrayList<MoneyOccPeriodFeeAssigned> getOccPeriodFilteredFeeList() {
        return occPeriodFilteredFeeList;
    }

    public void setOccPeriodFilteredFeeList(ArrayList<MoneyOccPeriodFeeAssigned> occPeriodFilteredFeeList) {
        this.occPeriodFilteredFeeList = occPeriodFilteredFeeList;
    }

    public boolean isWaived() {
        return waived;
    }

    public void setWaived(boolean waived) {
        this.waived = waived;
    }

    public ArrayList<Fee> getFilteredFeeList() {
        return filteredFeeList;
    }

    public void setFilteredFeeList(ArrayList<Fee> filteredFeeList) {
        this.filteredFeeList = filteredFeeList;
    }
    
}
