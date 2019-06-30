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
import com.tcvcog.tcvce.entities.Payment;
import com.tcvcog.tcvce.entities.PaymentType;
import java.io.Serializable;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.bean.*;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Adam Gutonski
 */

@ViewScoped
public class PaymentBB extends BackingBeanUtils implements Serializable {
    
    private ArrayList<Payment> paymentList;
    private Payment selectedPayment;
    private int formPaymentID;
    private int formPaymentOccupancyInspectionID;
    private PaymentType formPaymentPaymentType;
    private java.util.Date formPaymentDateDeposited;
    private java.util.Date formPaymentDateReceived;
    private double formPaymentAmount;
    private int formPaymentPayerID;
    private String formPaymentReferenceNum;
    private int formCheckNum;
    private boolean formCleared;
    private String formNotes;

    
    /**
     * @return the paymentList
     */
    public ArrayList<Payment> getPaymentList() {
        try {
            PaymentIntegrator paymentIntegrator = getPaymentIntegrator();
            paymentList = paymentIntegrator.getPaymentList();
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Unable to load payment list",
                        "This must be corrected by the system administrator"));
        }
        if(paymentList != null){
        return paymentList;
        }else{
         paymentList = new ArrayList();
         return paymentList;
        }
    }
    
    public void commitPaymentUpdates(ActionEvent e){
        PaymentIntegrator paymentIntegrator = getPaymentIntegrator();
        Payment payment = selectedPayment;
        
        payment.setPaymentType(formPaymentPaymentType);
        payment.setOccupancyInspectionID(formPaymentOccupancyInspectionID);
        payment.setPaymentType(formPaymentPaymentType);
        payment.setPaymentDateDeposited(formPaymentDateDeposited.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
        payment.setPaymentDateReceived(formPaymentDateReceived.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
        payment.setPaymentAmount(formPaymentAmount);
        payment.setPaymentPayerID(formPaymentPayerID);
        payment.setPaymentReferenceNum(formPaymentReferenceNum);
        payment.setCheckNum(formCheckNum);
        payment.setCleared(formCleared);
        payment.setNotes(formNotes);
        //oif.setOccupancyInspectionFeeNotes(formOccupancyInspectionFeeNotes);
        try{
            paymentIntegrator.updatePayment(payment);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Payment record updated!", ""));
        } catch (IntegrationException ex){
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to update payment record in database.",
                    "This must be corrected by the System Administrator"));
        }
    }
    
    public void editPayment(ActionEvent e){
        if(getSelectedPayment() != null){
            setFormPaymentID(selectedPayment.getPaymentID());
            setFormPaymentPaymentType(selectedPayment.getPaymentType());
            setFormPaymentOccupancyInspectionID(selectedPayment.getOccupancyInspectionID());
            setFormPaymentAmount(selectedPayment.getPaymentAmount());
            setFormPaymentPayerID(selectedPayment.getPaymentPayerID());
            setFormPaymentReferenceNum(selectedPayment.getPaymentReferenceNum());
            setFormCheckNum(selectedPayment.getCheckNum());
            setFormCleared(selectedPayment.isCleared());
            setFormPaymentDateReceived(java.util.Date.from(selectedPayment.getPaymentDateDeposited()
                    .atZone(ZoneId.systemDefault()).toInstant()));
            setFormPaymentDateDeposited(java.util.Date.from(selectedPayment.getPaymentDateDeposited()
                    .atZone(ZoneId.systemDefault()).toInstant()));
            setFormNotes(selectedPayment.getNotes());
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please select a payment record to update", ""));
        }
    }
    
    public String addPayment(){
        Payment payment = new Payment();
        PaymentIntegrator paymentIntegrator = new PaymentIntegrator();
        payment.setPaymentID(formPaymentID);
        payment.setOccupancyInspectionID(formPaymentOccupancyInspectionID);
        payment.setPaymentType(getFormPaymentPaymentType());
        payment.setPaymentDateDeposited(formPaymentDateDeposited.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
        payment.setPaymentDateReceived(formPaymentDateReceived.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
        payment.setPaymentAmount(formPaymentAmount);
        payment.setPaymentPayerID(formPaymentPayerID);
        payment.setPaymentReferenceNum(formPaymentReferenceNum);
        payment.setCheckNum(formCheckNum);
        payment.setCleared(formCleared);
        payment.setNotes(formNotes);
        
        try {
            paymentIntegrator.insertPayment(payment);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Successfully added payment record to database!", ""));
        } catch(IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to add payment record to database, sorry!", "Check server print out..."));
            return "";
        }
        
        return "paymentManage";
        
        
    }
    
    public void deleteSelectedPayment(ActionEvent e){
        PaymentIntegrator paymentIntegrator = getPaymentIntegrator();
        if(getSelectedPayment() != null){
            try {
                paymentIntegrator.deletePayment(getSelectedPayment());
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Payment record deleted forever!", ""));
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Unable to delete payment record--probably because it is used "
                                    + "somewhere in the database. Sorry.", 
                            "This category will always be with us."));
            }
            
        } else {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Please select a payment record from the table to delete", ""));
        }
    }

    /**
     * @param paymentList the paymentList to set
     */
    public void setPaymentList(ArrayList<Payment> paymentList) {
        this.paymentList = paymentList;
    }

    /**
     * @return the selectedPayment
     */
    public Payment getSelectedPayment() {
        return selectedPayment;
    }

    /**
     * @param selectedPayment the selectedPayment to set
     */
    public void setSelectedPayment(Payment selectedPayment) {
        this.selectedPayment = selectedPayment;
    }

    /**
     * @return the formPaymentID
     */
    public int getFormPaymentID() {
        return formPaymentID;
    }

    /**
     * @param formPaymentID the formPaymentID to set
     */
    public void setFormPaymentID(int formPaymentID) {
        this.formPaymentID = formPaymentID;
    }

    /**
     * @return the formPaymentOccupancyInspectionID
     */
    public int getFormPaymentOccupancyInspectionID() {
        return formPaymentOccupancyInspectionID;
    }

    /**
     * @param formPaymentOccupancyInspectionID the formPaymentOccupancyInspectionID to set
     */
    public void setFormPaymentOccupancyInspectionID(int formPaymentOccupancyInspectionID) {
        this.formPaymentOccupancyInspectionID = formPaymentOccupancyInspectionID;
    }

    /**
     * @return the formPaymentPaymentType
     */
    public PaymentType getFormPaymentPaymentType() {
        return formPaymentPaymentType;
    }

    /**
     * @param formPaymentPaymentType the formPaymentTypeID to set
     */
    public void setFormPaymentPaymentType(PaymentType formPaymentPaymentType) {
        this.formPaymentPaymentType = formPaymentPaymentType;
    }

    /**
     * @return the formPaymentDateDeposited
     */
    public java.util.Date getFormPaymentDateDeposited() {
        return formPaymentDateDeposited;
    }

    /**
     * @param formPaymentDateDeposited the formPaymentDateDeposited to set
     */
    public void setFormPaymentDateDeposited(java.util.Date formPaymentDateDeposited) {
        this.formPaymentDateDeposited = formPaymentDateDeposited;
    }

    /**
     * @return the formPaymentDateReceived
     */
    public java.util.Date getFormPaymentDateReceived() {
        return formPaymentDateReceived;
    }

    /**
     * @param formPaymentDateReceived the formPaymentDateReceived to set
     */
    public void setFormPaymentDateReceived(java.util.Date formPaymentDateReceived) {
        this.formPaymentDateReceived = formPaymentDateReceived;
    }

    /**
     * @return the formPaymentAmount
     */
    public double getFormPaymentAmount() {
        return formPaymentAmount;
    }

    /**
     * @param formPaymentAmount the formPaymentAmount to set
     */
    public void setFormPaymentAmount(double formPaymentAmount) {
        this.formPaymentAmount = formPaymentAmount;
    }

    /**
     * @return the formPaymentPayerID
     */
    public int getFormPaymentPayerID() {
        return formPaymentPayerID;
    }

    /**
     * @param formPaymentPayerID the formPaymentPayerID to set
     */
    public void setFormPaymentPayerID(int formPaymentPayerID) {
        this.formPaymentPayerID = formPaymentPayerID;
    }

    /**
     * @return the formPaymentReferenceNum
     */
    public String getFormPaymentReferenceNum() {
        return formPaymentReferenceNum;
    }

    /**
     * @param formPaymentReferenceNum the formPaymentReferenceNum to set
     */
    public void setFormPaymentReferenceNum(String formPaymentReferenceNum) {
        this.formPaymentReferenceNum = formPaymentReferenceNum;
    }

    /**
     * @return the formCheckNum
     */
    public int getFormCheckNum() {
        return formCheckNum;
    }

    /**
     * @param formCheckNum the formCheckNum to set
     */
    public void setFormCheckNum(int formCheckNum) {
        this.formCheckNum = formCheckNum;
    }

    /**
     * @return the formCleared
     */
    public boolean isFormCleared() {
        return formCleared;
    }

    /**
     * @param formCleared the formCleared to set
     */
    public void setFormCleared(boolean formCleared) {
        this.formCleared = formCleared;
    }

    /**
     * @return the formNotes
     */
    public String getFormNotes() {
        return formNotes;
    }

    /**
     * @param formNotes the formNotes to set
     */
    public void setFormNotes(String formNotes) {
        this.formNotes = formNotes;
    }


}
