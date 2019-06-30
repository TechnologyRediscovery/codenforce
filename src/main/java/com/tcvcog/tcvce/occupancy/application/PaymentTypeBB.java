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
import com.tcvcog.tcvce.occupancy.integration.PaymentIntegrator;
import com.tcvcog.tcvce.entities.PaymentType;
import java.io.Serializable;
import java.util.ArrayList;
import javax.inject.Named;
import javax.enterprise.context.Dependent;
import javax.faces.application.FacesMessage;
import javax.faces.bean.*;
import javax.faces.event.ActionEvent;


/**
 *
 * @author Adam Gutonski
 */

@ViewScoped
public class PaymentTypeBB extends BackingBeanUtils implements Serializable {
    
    private ArrayList<PaymentType> paymentTypeList;
    private ArrayList<PaymentType> paymentTypeTitleList;
    private PaymentType selectedPaymentType;
    private int formPaymentTypeID;
    private String formPaymentTypeTitle;
    
    private PaymentType newFormSelectedPaymentType;
    private int newFormPaymentTypeID;
    private String newFormPaymentTypeTitle;
    
    

    /**
     * Creates a new instance of PaymentTypeBB
     */
    public PaymentTypeBB() {
    }
    
    public void editPaymentType(ActionEvent e){
        if(getSelectedPaymentType() != null){
            setFormPaymentTypeID(selectedPaymentType.getPaymentTypeId());
            setFormPaymentTypeTitle(selectedPaymentType.getPaymentTypeTitle());
            
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please select a payment type to update", ""));
        }
    }
    
    public void commitPaymentTypeUpdates(ActionEvent e){
        PaymentIntegrator pti = getPaymentIntegrator();
        PaymentType pt = selectedPaymentType;
        
        pt.setPaymentTypeTitle(formPaymentTypeTitle);
        //oif.setOccupancyInspectionFeeNotes(formOccupancyInspectionFeeNotes);
        try{
            pti.updatePaymentType(pt);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Payment type updated!", ""));
        } catch (IntegrationException ex){
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to update Payment type in database.",
                    "This must be corrected by the System Administrator"));
        }
    }
    
    public String addPaymentType(){
        PaymentType pt = new PaymentType();
        PaymentIntegrator pti = new PaymentIntegrator();
        pt.setPaymentTypeId(formPaymentTypeID);
        pt.setPaymentTypeTitle(formPaymentTypeTitle);
        try {
            pti.insertPaymentType(pt);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Successfully added payment type to database!", ""));
        } catch(IntegrationException ex) {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to add payment type to database, sorry!", "Check server print out..."));
            return "";
        }
        
        return "paymentTypeManage";  
    }
    
    /**
     * @return the paymentTypeList
     */
    public ArrayList<PaymentType> getPaymentTypeList() {
         try {
            PaymentIntegrator pti = getPaymentIntegrator();
            paymentTypeList = pti.getPaymentTypeList();
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Unable to load Paymetn Type",
                        "This must be corrected by the system administrator"));
        }
        if(paymentTypeList != null){
        return paymentTypeList;
        }else{
         paymentTypeList = new ArrayList();
         return paymentTypeList;
        }
    }
    
    public void deleteSelectedPaymentType(ActionEvent e){
        PaymentIntegrator pti = getPaymentIntegrator();
        if(getSelectedPaymentType() != null){
            try {
                pti.deletePaymentType(getSelectedPaymentType());
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Payment type deleted forever!", ""));
            } catch (IntegrationException ex) {
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Unable to delete payment type--probably because it is used "
                                    + "somewhere in the database. Sorry.", 
                            "This category will always be with us."));
            }
            
        } else {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Please select a payment type from the table to delete", ""));
        }
    }

    /**
     * @param paymentTypeList the paymentTypeList to set
     */
    public void setPaymentTypeList(ArrayList<PaymentType> paymentTypeList) {
        this.paymentTypeList = paymentTypeList;
    }

    /**
     * @return the selectedPaymentType
     */
    public PaymentType getSelectedPaymentType() {
        return selectedPaymentType;
    }

    /**
     * @param selectedPaymentType the selectedPaymentType to set
     */
    public void setSelectedPaymentType(PaymentType selectedPaymentType) {
        this.selectedPaymentType = selectedPaymentType;
    }

    /**
     * @return the formPaymentTypeID
     */
    public int getFormPaymentTypeID() {
        return formPaymentTypeID;
    }

    /**
     * @param formPaymentTypeID the formPaymentTypeID to set
     */
    public void setFormPaymentTypeID(int formPaymentTypeID) {
        this.formPaymentTypeID = formPaymentTypeID;
    }

    /**
     * @return the formPaymentTypeTitle
     */
    public String getFormPaymentTypeTitle() {
        return formPaymentTypeTitle;
    }

    /**
     * @param formPaymentTypeTitle the formPaymentTypeTitle to set
     */
    public void setFormPaymentTypeTitle(String formPaymentTypeTitle) {
        this.formPaymentTypeTitle = formPaymentTypeTitle;
    }

    /**
     * @return the newFormSelectedPaymentType
     */
    public PaymentType getNewFormSelectedPaymentType() {
        return newFormSelectedPaymentType;
    }

    /**
     * @param newFormSelectedPaymentType the newFormSelectedPaymentType to set
     */
    public void setNewFormSelectedPaymentType(PaymentType newFormSelectedPaymentType) {
        this.newFormSelectedPaymentType = newFormSelectedPaymentType;
    }

    /**
     * @return the newFormPaymentTypeID
     */
    public int getNewFormPaymentTypeID() {
        return newFormPaymentTypeID;
    }

    /**
     * @param newFormPaymentTypeID the newFormPaymentTypeID to set
     */
    public void setNewFormPaymentTypeID(int newFormPaymentTypeID) {
        this.newFormPaymentTypeID = newFormPaymentTypeID;
    }

    /**
     * @return the newFormPaymentTypeTitle
     */
    public String getNewFormPaymentTypeTitle() {
        return newFormPaymentTypeTitle;
    }

    /**
     * @param newFormPaymentTypeTitle the newFormPaymentTypeTitle to set
     */
    public void setNewFormPaymentTypeTitle(String newFormPaymentTypeTitle) {
        this.newFormPaymentTypeTitle = newFormPaymentTypeTitle;
    }

    /**
     * @return the paymentTypeTitleList
     */
    public ArrayList<PaymentType> getPaymentTypeTitleList() throws IntegrationException {
        PaymentIntegrator pi = getPaymentIntegrator();
        paymentTypeTitleList = pi.getPaymentTypeList();
        return paymentTypeTitleList;
    }

    /**
     * @param paymentTypeTitleList the paymentTypeTitleList to set
     */
    public void setPaymentTypeTitleList(ArrayList<PaymentType> paymentTypeTitleList) {
        this.paymentTypeTitleList = paymentTypeTitleList;
    }
    
}
