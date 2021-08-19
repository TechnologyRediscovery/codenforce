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
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.CodeCoordinator;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.PaymentCoordinator;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.NavigationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.CodeSet;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.EventDomainEnum;
import com.tcvcog.tcvce.entities.Fee;
import com.tcvcog.tcvce.entities.FeeAssigned;
import com.tcvcog.tcvce.entities.MoneyCECaseFeeAssigned;
import com.tcvcog.tcvce.entities.MoneyOccPeriodFeeAssigned;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodDataHeavy;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodType;
import java.io.Serializable;
import java.util.*;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.*;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Adam Gutonski
 */
@ViewScoped
public class FeeManagementBB extends BackingBeanUtils implements Serializable {

    //feeTypeManage.xhtml fields
    private ArrayList<Fee> existingFeeTypeList;
    private Fee selectedFeeType;

    //feeManage.xhtml fields
    private OccPeriodDataHeavy currentOccPeriod;
    private CECaseDataHeavy currentCase;
    private Fee selectedFee;
    private ArrayList<Fee> feeList;
    private ArrayList<Fee> filteredFeeList;
    private ArrayList<CodeViolation> violationList;
    private ArrayList<CodeViolation> filteredViolationList;
    private CodeViolation selectedViolation;

    private FeeAssigned selectedAssignedFee;
    private ArrayList<FeeAssigned> feeAssignedList;
    private ArrayList<FeeAssigned> filteredFeeAssignedList;

    //feePermissions.xhtml fields
    private ArrayList<OccPeriodType> typeList;
    private ArrayList<OccPeriodType> filteredTypeList;
    private OccPeriodType selectedPeriodType;

    private ArrayList<EnforcableCodeElement> elementList;
    private ArrayList<EnforcableCodeElement> filteredElementList;
    private EnforcableCodeElement selectedCodeElement;

    private HashMap<Integer, Fee> existingFeeList;
    private ArrayList<Fee> workingFeeList;
    private Fee selectedWorkingFee;
    private List<Fee> allFees;

    //Generalized fields
    private EventDomainEnum currentDomain;
    private String currentMode;
    private boolean waived;
    private boolean redirected;
    private boolean currentFeeSelected; //Can be used to see if any entity is currently selected, not just fees

    public FeeManagementBB() {
    }

    @PostConstruct
    public void initBean() throws BObStatusException {
        selectedFeeType = new Fee();

        PaymentCoordinator pc = getPaymentCoordinator();

        selectedAssignedFee = new FeeAssigned();

        currentMode = "Lookup";

        //initialize default select button in list-column: false
        currentFeeSelected = false;

        //Check if we were redirected here from another page
        if (getSessionBean().getNavStack().peekLastPage() != null) {

            redirected = true;
            
            refreshFeeAssignedList();

            if (allFees == null) {

                try {
                    allFees = pc.getFeeList();
                } catch (IntegrationException ex) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Oops! We encountered a problem trying to fetch the list of fee templates!", ""));
                    System.out.println(ex.toString());
                }

            }

            if (feeList == null) {
                feeList = (ArrayList<Fee>) allFees;
            }

            refreshTypesAndElements();

            if (currentCase != null) {
                violationList = (ArrayList<CodeViolation>) currentCase.getViolationList();
            }

        }
    }

    //check if current mode == Lookup
    public boolean getActiveLookupMode() {
        return "Lookup".equals(currentMode);
    }

    //check if current mode == Insert
    public boolean getActiveInsertMode() {
        return "Insert".equals(currentMode);
    }

    //check if current mode == Update
    public boolean getActiveUpdateMode() {
        return "Update".equals(currentMode);
    }

    //check if current mode == Remove
    public boolean getActiveRemoveMode() {
        return "Remove".equals(currentMode);
    }

    //Select button on side panel can only be used in either Lookup Mode or Update Mode
    public boolean getSelectedButtonActive() {
        return !("Lookup".equals(currentMode) || "Update".equals(currentMode) || "Remove".equals(currentMode));
    }

    /**
     *
     * @param currentMode Lookup, Insert, Update, Remove
     * @throws IntegrationException
     */
    public void setCurrentMode(String currentMode) throws IntegrationException {

        //reset default setting every time the Mode has been selected 
        currentFeeSelected = false;
        
        //We can only use the input if it's not null
        if (currentMode != null) {
            this.currentMode = currentMode;
        }
        
        //create an instance of both fees if current mode == "Insert"
        if (getActiveInsertMode()) {
            selectedAssignedFee = new FeeAssigned();

            selectedFee = new Fee();
        }
        //show the current mode in p:messages box
        getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, this.currentMode + " Mode Selected", ""));
    }

    /**
     * Changing which fee is selected and not selected
     *
     * @param currentFee
     * @throws IntegrationException
     */
    public void onAssignedFeeSelectedButtonChange(FeeAssigned currentFee) throws IntegrationException, BObStatusException {

        // "Select" button was selected
        if (currentFeeSelected == true) {

            //Set the correct domain and create the correct type of Assigned Fee object
            if (currentDomain == EventDomainEnum.OCCUPANCY) {

                MoneyOccPeriodFeeAssigned skeleton = (MoneyOccPeriodFeeAssigned) currentFee;

                if (selectedAssignedFee != null) {
                    skeleton.setOccPeriodID(currentOccPeriod.getPeriodID());

                } else {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Please select an assigned fee to update", ""));
                }

                //set selected fee
                selectedAssignedFee = skeleton;
                //update the current selected fee list in side panel
                feeAssignedList = new ArrayList<>();
                feeAssignedList.add(skeleton);

            } else if (currentDomain == EventDomainEnum.CODE_ENFORCEMENT){

                MoneyCECaseFeeAssigned skeleton = (MoneyCECaseFeeAssigned) currentFee;

                if (selectedAssignedFee != null) {

                    skeleton.setCaseID(currentCase.getCaseID());

                } else {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Please select an assigned fee to update", ""));
                }

                //set selected fee
                selectedAssignedFee = skeleton;
                //update the current selected fee list in side panel
                feeAssignedList = new ArrayList<>();
                feeAssignedList.add(skeleton);

            }

            //Message Noticefication
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Current Selected Assigned Fee: " + selectedAssignedFee.getAssignedFeeID(), ""));
            
        } else {
            // "Select" button was deselected
            //turn to default setting
            currentFeeSelected = false;

            refreshFeeAssignedList();

            //Message Noticefication
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Default Selected Fee: " + selectedAssignedFee.getAssignedFeeID(), ""));
        }

    }

    /**
     * Changing which type is selected and not selected
     *
     * @param currentType
     * @throws IntegrationException
     */
    public void onOccPeriodTypeSelectedButtonChange(OccPeriodType currentType) throws IntegrationException, BObStatusException {

        if (currentFeeSelected == true) {

            selectedPeriodType = currentType;

            try {
                
                workingFeeList = new ArrayList<>(selectedPeriodType.getPermittedFees());
                for(Fee fee : workingFeeList){
                    existingFeeList.put(fee.getFeeID(), fee);
                }                
                
            } catch (NullPointerException e) {
                System.out.println("OccPeriodType has no existing permitted fee list, making new lists...");
                existingFeeList = new HashMap<>();
                workingFeeList = new ArrayList<>();
            }
            //update the current selected fee list in side panel

            typeList = new ArrayList<>();
            typeList.add(selectedPeriodType);

            //Message Noticefication
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Current Selected Occ Period Type: " + selectedPeriodType.getTypeID(), ""));

            
        } else {
            // "Select" button was deselected
            //turn to default setting
            currentFeeSelected = false;

            selectedPeriodType = new OccPeriodType();

            refreshFeeAssignedList();

            refreshTypesAndElements();

            //Message Noticefication
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Default Selected Occ Period Type: " + selectedPeriodType.getTypeID(), ""));
        }

    }

    /**
     * Changing which element is selected and not selected
     *
     * @param currentElement
     * @throws IntegrationException
     */
    public void onCodeElementSelectedButtonChange(EnforcableCodeElement currentElement) throws IntegrationException, BObStatusException {

        if (currentFeeSelected == true) {

            selectedCodeElement = currentElement;

            try {
                workingFeeList = new ArrayList<>(selectedCodeElement.getFeeList());
                for(Fee fee : workingFeeList){
                    existingFeeList.put(fee.getFeeID(), fee);
                }
            } catch (NullPointerException e) {
                System.out.println("EnforcableCodeElement has no existing permitted fee list, making new lists...");
                workingFeeList = new ArrayList<>();
                existingFeeList = new HashMap<>();
            }
            //update the current selected fee list in side panel

            elementList = new ArrayList<>();
            elementList.add(selectedCodeElement);

            //Message Noticefication
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Current Selected Code Set Element: " + selectedCodeElement.getCodeSetElementID(), ""));

            // "Select" button wasn't selected
        } else {
            //turn to default setting
            currentFeeSelected = false;

            selectedCodeElement = new EnforcableCodeElement();

            refreshFeeAssignedList();

            refreshTypesAndElements();

            //Message Noticefication
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Default Selected Code Set Element: " + selectedCodeElement.getCodeSetElementID(), ""));
        }

    }

    /**
     * Changing of which fee is being selected and not being selected
     *
     * @param currentFee
     * @throws IntegrationException
     */
    public void onFeeSelectedButtonChange(Fee currentFee) throws IntegrationException {

        // "Select" button was selected
        if (currentFeeSelected == true) {

            //set current selected fee
            selectedFeeType = currentFee;
            //update the current selected fee list in side panel
            feeList = new ArrayList<>();
            feeList.add(currentFee);

            //Message Noticefication
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Current Selected Fee: " + selectedFeeType.getName(), ""));

            
        } else {
            // "Select" button was deselected

            //turn to default setting
            currentFeeSelected = false;
            selectedFeeType = new Fee();

            refreshTypesAndElements();

            //Message Noticefication
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Default Selected Fee: " + selectedFeeType.getName(), ""));
        }

    }

    /**
     * Inserts a new FeeAssigned object into the database.
     * When the interface makes a new FeeAssigned object, it stores the new fee
     * in the "selectedAssignedFee" field. So, in this case, 
     * the selectedAssignedFee is a new fee.
     * @return 
     */
    public String onInsertAssignedFeeButtonChange() throws BObStatusException {

        PaymentCoordinator pc = getPaymentCoordinator();

        if (currentDomain == EventDomainEnum.OCCUPANCY) {

            try {
                pc.insertAssignedFee(selectedAssignedFee, currentOccPeriod, waived);
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully added new fee!", ""));
            } catch (BObStatusException ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                ex.getMessage(), ""));
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to add fee to database, sorry!", "Check server print out..."));
            }
        } else {
            try {
                pc.insertAssignedFee(selectedAssignedFee, currentCase, selectedViolation, waived);
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully added new fee!", ""));
            } catch (BObStatusException ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                ex.getMessage(), ""));
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to add fee to database, sorry!", "Check server print out..."));
            }
        }

        //Refresh so the new fee will show up in the list.
        refreshFeeAssignedList();

        return "feeManage";
    }

    /**
     * Applies changes on the selected AssignedFee to the database.
     * @return 
     */
    public String onUpdateAssignedFeeButtonChange() throws BObStatusException {

        PaymentCoordinator pc = getPaymentCoordinator();
        if (currentDomain == EventDomainEnum.OCCUPANCY) {
            try {

                pc.updateAssignedFee(selectedAssignedFee, currentOccPeriod, waived);
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully updated fee!", ""));
            } catch (BObStatusException ex) {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));

            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to update fee in database, sorry!", "Check server print out..."));

            }
        } else {

            try {
                pc.updateAssignedFee(selectedAssignedFee, currentCase, selectedViolation, waived);
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully updated fee!", ""));

            } catch (BObStatusException ex) {
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));

            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to update fee in database, sorry!", "Check server print out..."));
            }
        }
        
        //Refresh the list so the user can see the now updated fee
        refreshFeeAssignedList();
        return "feeManage";
    }

    /**
     * Waive the selected fee - remember, finanical data should not be deleted,
     * so fees need to be waived instead.
     * @return 
     */
    public String onRemoveAssignedFeeButtonChange() throws BObStatusException {

        PaymentCoordinator pc = getPaymentCoordinator();

        if (currentDomain == EventDomainEnum.OCCUPANCY) {

            try {
                pc.updateAssignedFee(selectedAssignedFee, currentOccPeriod, true); // set waived to true
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Fee waived!", ""));
            } catch (BObStatusException ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
            } catch (IntegrationException ex) {
                System.out.println(ex.toString());
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "We encountered a problem while waiving the fee!", ""));
            }
        }
        if (currentDomain == EventDomainEnum.CODE_ENFORCEMENT) {

            try {
                pc.updateAssignedFee(selectedAssignedFee, currentCase, selectedViolation, true); // set waived to true
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Fee waived!", ""));
            } catch (BObStatusException ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
            } catch (IntegrationException ex) {
                System.out.println(ex.toString());
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "We encountered a problem while waiving the fee!", ""));
            }
        }
        
        //Refresh the list so the user can see the now updated fee
        refreshFeeAssignedList();

        return "feeManage";
    }

    /**
     * The user is done editing managing fees, let's redirect them back to whatever page they were last.
     * @return 
     */
    public String finishAndRedir() {
        try {
            return getSessionBean().getNavStack().popLastPage();
        } catch (NavigationException ex) {
            System.out.println("FeeManagementBB.finishAndRedir | ERROR: " + ex);
            FacesContext context = getFacesContext();
                    context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "An error occured while trying to direct you back to the page you were last on."
                            + " Your changes to the database were saved. Please return to the page manually.",
                            "Do not hit the return button again but note the error."));
                    context.getExternalContext().getFlash().setKeepMessages(true);
            return "";
        }
    }

    public String goToFeePermissions() {

        if (editingCECase() || editingOccPeriod()) {
            getSessionBean().setFeeManagementDomain(currentDomain);
            getSessionBean().getNavStack().pushCurrentPage();
        }
        return "feePermissions";
    }

    public String goToFeeTypes() {

        getSessionBean().getNavStack().pushCurrentPage();
        return "feeTypeManage";
    }

    public String whatDomainFees() {
        if (currentDomain == EventDomainEnum.OCCUPANCY) {

            return "occupancy periods";

        } else {

            return "code enforcement elements";
        }

    }

    /**
     * Applies changes on the selected Fee to the database.
     * @return 
     */
    public String onUpdateFeeButtonChange() {

        PaymentCoordinator pc = getPaymentCoordinator();

        if (getSelectedFeeType() != null) {

            try {
                pc.updateFee(selectedFeeType);
            } catch (IntegrationException ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "We encountered a problem while updating the fee!", ""));
                System.out.println(ex.toString());
            }

            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Occupancy Inspection Fee updated!", ""));
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select an occupancy inspection fee from the table to delete", ""));
        }

        return "feeTypeManage";
    }

    /**
     * Inserts a new Fee object into the database.
     * When the interface makes a new Fee object, it stores the new fee
     * in the "selectedFeeType" field. So, in this case, 
     * the selectedFeeType is a new fee.
     * @return 
     */
    public String onInsertFeeButtonChange() {
        PaymentCoordinator pc = getPaymentCoordinator();
        if (getSelectedFeeType() != null) {

            try {
                pc.insertFee(selectedFeeType);
            } catch (IntegrationException ex) {
                System.out.println(ex.toString());
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "We encountered a problem while inserting the fee!", ""));
            }
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully added occupancy inspection fee to database!", ""));

        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select an occupancy inspection fee from the table to delete", ""));
        }
        return "feeTypeManage";

    }

    /**
     * Remove the selected Fee. 
     * 
     * Fee objects do not represent actually applied
     * fees or any financial data - they're just templates that are then assigned
     * to other entities via AssignedFee. So, it's okay for users to edit or remove
     * them at will.
     */
    public void onRemoveFeeButtonChange() {
        PaymentCoordinator pc = getPaymentCoordinator();

        if (getSelectedFeeType() != null) {
            try {
                pc.removeFee(selectedFeeType);
            } catch (IntegrationException ex) {
                System.out.println(ex.toString());
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "An error occurred while trying to delete the selected fee from the database.", ""));
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
        PaymentCoordinator pc = getPaymentCoordinator();
        try {
            existingFeeTypeList = (ArrayList<Fee>) pc.getAllFeeTypes();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }

        return existingFeeTypeList;

    }

    /**
     * A method used to only remove a Fee from the list of Fees permitted for
     * a given entity. It doesn't delete the fee from the database.
     * @param selectedFee 
     */
    public void removePermittedFee(Fee selectedFee) {
        workingFeeList.remove(selectedFee);
    }

    public void addFeeToPermittedFees() {

        if (selectedFee == null) {

            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a fee to add to the list.", ""));

        } else {

            boolean duplicate = false;

            for (Fee test : workingFeeList) {
                duplicate = test.getFeeID() == selectedFee.getFeeID();
                if (duplicate) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "You cannot permit the same fee twice.", ""));
                    break;
                }

            }
            if (!duplicate) {
                workingFeeList.add(selectedFee);
            }
        }
    }

    /**
     * Looks through the working list of permitted Fees and compares with the 
     * list of permitted Fees in the database.
     * 
     * As you can see, it's a pretty heavy operation, as we need to scan the
     * list multiple times to insert and deactive Fees as they are permitted and
     * prohibited respectively.
     * @return 
     */
    public String onUpdatePermissionButtonChange() {

        if (currentDomain == EventDomainEnum.OCCUPANCY) {

            if (existingFeeList.isEmpty() || existingFeeList == null) {
                
                //There are no currently permitted Fees for this entity
                //Let's skip scanning and insert them all to save on processing time.
                for (Fee workingFee : workingFeeList) {
                    insertOrReactivateOccPeriodJoin(workingFee);
                }

            } else {

                    //First check for fees that the user just prohibited
                    scanExistingOccPeriodFeeListWithDeactivate();

                    //Now we can check for newly permitted fees.
                    scanExistingOccPeriodFeeListWithInsert();

            }
        } else if (currentDomain == EventDomainEnum.CODE_ENFORCEMENT) {

            if (existingFeeList.isEmpty() || existingFeeList == null) {
                
                //There are no currently permitted Fees for this entity
                //Let's skip scanning and insert them all to save on processing time.
                for (Fee workingFee : workingFeeList) {

                    insertOrReactivateCodeElementJoin(workingFee);
                }

            } else {
                    
                    //First check for fees that the user just prohibited
                    scanExistingCodeElementFeeListWithDeactivate();

                    //Now we can check for newly permitted fees.
                    scanExistingCodeElementFeeListWithInsert();

            }
        }
        //Refresh the lists so the user can see their applied changes.
        refreshTypesAndElements();

        return "feePermissions";
    }
    /**
     * Activates a join between a workingFee and the selectedPeriodType.
     * @param workingFee 
     */
    public void insertOrReactivateOccPeriodJoin(Fee workingFee) {

        PaymentCoordinator pc = getPaymentCoordinator();

        try {
            pc.activateFeeJoin(workingFee, selectedPeriodType);
        } catch (IntegrationException ex) {
            System.out.println("FeeManagementBB.insertOrReactivateOccPeriodJoin | Error: " + ex.toString());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "You cannot permit the same fee twice.", ""));
        }

    }
    
    /**
     * Activates a join between a workingFee and the selectedCodeElement
     * @param workingFee 
     */
    public void insertOrReactivateCodeElementJoin(Fee workingFee) {

        PaymentCoordinator pc = getPaymentCoordinator();

        try {
            pc.activateFeeJoin(workingFee, selectedCodeElement);
        } catch (IntegrationException ex) {
            System.out.println("FeeManagementBB.insertOrReactivateCodeElementJoin | Error: " + ex.toString());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "You cannot permit the same fee twice.", ""));
        }

    }

    /**
     * Compares the workingFeeList with the existingFeeList, checking to see if 
     * any fee permission joins need to be activated or updated.
     */
    public void scanExistingOccPeriodFeeListWithInsert() {

        PaymentCoordinator pc = getPaymentCoordinator();

        for (Fee workingFee : workingFeeList) {
                if (existingFeeList.containsKey(workingFee.getFeeID())) {
                    //The working fee is in the existing fee list, let's update it
                    try {
                        pc.updateFeeJoin(workingFee, selectedPeriodType);
                    } catch (IntegrationException ex) {
                        System.out.println("FeeManagementBB.scanExistingOccPeriodFeeListWithInsert() | Error: " + ex.toString());
                        getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "An error occurred while trying to update an existing fee permission", ""));
                    }
                    break;
                } else {
                    //The working fee is new
                    insertOrReactivateOccPeriodJoin(workingFee);
                }
        }

    }

    /**
     * Compares the workingFeeList with the existingFeeList, checking to see if 
     * any fee permission joins need to be activated or updated.
     */
    public void scanExistingCodeElementFeeListWithInsert() {

        PaymentCoordinator pc = getPaymentCoordinator();

        for (Fee workingFee : workingFeeList) {
                if (existingFeeList.containsKey(workingFee.getFeeID())) {
                    //The working fee is in the existing fee list, let's update it
                    try {
                        pc.updateFeeJoin(workingFee, selectedCodeElement);
                    } catch (IntegrationException ex) {
                        System.out.println("FeeManagementBB.scanExistingCodeElementFeeListWithInsert() | Error: " + ex.toString());
                        getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "An error occurred while trying to update an existing fee permission", ""));
                    }
                    break;
                } else {
                    //The working fee is new
                    insertOrReactivateCodeElementJoin(workingFee);
                }
        }

    }

    /**
     * Finds which fees have been removed from the workingFeeList and deactivates
     * them in the database.
     */
    public void scanExistingOccPeriodFeeListWithDeactivate() {

        PaymentCoordinator pc = getPaymentCoordinator();

        //Work on a clone of the existingFeeList so we don't cause errors.
        HashMap<Integer, Fee> tempMap = new HashMap<>(existingFeeList);
        
        /*
        If any fees were removed from the workingFeeList, they would only be 
        in the existingFeeList. Let's remove all the fees that are in the 
        workingFeeList from the existingFeeList so we can isolate the no longer
        permitted fees.
        */
        for(Fee fee : workingFeeList){
            tempMap.remove(fee.getFeeID());
        }
        
        List<Fee> prohibitedFees = new ArrayList<>(tempMap.values());
        
        for (Fee prohibitedFee : prohibitedFees) {
                try {
                    pc.deactivateFeeJoin(prohibitedFee, selectedPeriodType);
                } catch (IntegrationException ex) {
                    System.out.println("FeeManagementBB.scanExistingOccPeriodFeeListWithDeactivate() | Error: " + ex.toString());
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "An error occurred while trying to remove an existing fee permission", ""));
                }

            }

    }

/**
     * Finds which fees have been removed from the workingFeeList and deactivates
     * them in the database.
     */
    public void scanExistingCodeElementFeeListWithDeactivate() {

        PaymentCoordinator pc = getPaymentCoordinator();

        //Work on a clone of the existingFeeList so we don't cause errors.
        HashMap<Integer, Fee> tempMap = new HashMap<>(existingFeeList);
        
        /*
        If any fees were removed from the workingFeeList, they would only be 
        in the existingFeeList. Let's remove all the fees that are in the 
        workingFeeList from the existingFeeList so we can isolate the no longer
        permitted fees.
        */
        for(Fee fee : workingFeeList){
            tempMap.remove(fee.getFeeID());
        }
        
        List<Fee> prohibitedFees = new ArrayList<>(tempMap.values());
        
        for (Fee prohibitedFee : prohibitedFees) {
                try {
                    pc.deactivateFeeJoin(prohibitedFee, selectedCodeElement);
                } catch (IntegrationException ex) {
                    System.out.println("FeeManagementBB.scanExistingCodeElementFeeListWithDeactivate() | Error: " + ex.toString());
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "An error occurred while trying to update an existing fee permission", ""));
                }

            }

    }


    /**
     * Refreshes the lists of assigned fees and loads the session BOb (either
     * an OccPeriod or a CECase)
     */
    public void refreshFeeAssignedList() throws BObStatusException {

        feeAssignedList = new ArrayList<>();

        PaymentCoordinator pc = getPaymentCoordinator();

        currentDomain = getSessionBean().getFeeManagementDomain();

        if (currentDomain == EventDomainEnum.OCCUPANCY) {

            OccupancyCoordinator oc = getOccupancyCoordinator();

            try {
                currentOccPeriod = oc.assembleOccPeriodDataHeavy(getSessionBean().getFeeManagementOccPeriod(), getSessionBean().getSessUser().getMyCredential());
            } catch (BObStatusException ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
            } catch (IntegrationException | SearchException ex) {
                System.out.println(ex.toString());
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Encountered error while trying to assemble the current Occupancy Period!", "Check server print out..."));
            }

            if (currentOccPeriod != null) {

                try {
                    feeAssignedList = (ArrayList<FeeAssigned>) pc.getAssignedFees(currentOccPeriod);

                    feeList = (ArrayList<Fee>) currentOccPeriod.getType().getPermittedFees();
                } catch (IntegrationException ex) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Oops! We encountered a problem trying to refresh the assigned fees list!", ""));
                }

            }

        } else if (currentDomain == EventDomainEnum.CODE_ENFORCEMENT) {

            CaseCoordinator cc = getCaseCoordinator();

            try {
                currentCase = cc.cecase_assembleCECaseDataHeavy(getSessionBean().getFeeManagementCeCase(), getSessionBean().getSessUser());
            } catch (BObStatusException | SearchException ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
            } catch (IntegrationException ex) {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Oops! We encountered a problem trying to prepare your selected CE Case before refreshing the assigned fees list!", ""));
            }

            if (currentCase != null) {

                try {

                    feeAssignedList = (ArrayList<FeeAssigned>) pc.getAssignedFees(currentCase);
                    feeList = new ArrayList<>();
                } catch (IntegrationException ex) {
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Oops! We encountered a problem trying to refresh the fee assigned list!", ""));
                }
            }
        }
    }

    /**
     * Refreshes Fee templates and the list of code elements
     */
    public void refreshTypesAndElements() {

        CaseCoordinator cc = getCaseCoordinator();
        OccupancyCoordinator oc = getOccupancyCoordinator();
        CodeCoordinator ec = getCodeCoordinator();

        typeList = (ArrayList<OccPeriodType>) oc.getOccPeriodTypesFromProfileID(getSessionBean().getSessMuni().getProfile().getProfileID());

        ArrayList<CodeSet> codeSetList = (ArrayList<CodeSet>) ec.getCodeSetsFromMuniID(getSessionBean().getSessMuni().getMuniCode());

        elementList = new ArrayList<>();

        for (CodeSet set : codeSetList) {

            elementList.addAll(ec.getCodeElementsFromCodeSetID(set.getCodeSetID()));

        }

        try {
            currentCase = cc.cecase_assembleCECaseDataHeavy(currentCase, getSessionBean().getSessUser());

        } catch (BObStatusException | SearchException ex) {
            getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
        } catch (IntegrationException ex) {
            System.out.println("FeeManagementBB.refreshTypesAndElements() | Error: " + ex.toString());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Oops! We encountered a problem trying to refresh the currentCase!", ""));
        } catch (NullPointerException ex) {

            System.out.println("FeeManagementBB.refreshTypesAndElements() | Null Pointer Exception when accessing case.");
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "No CE Case is currently selected!", ""));

        }

    }

    /**
     * The user has selected a violation, display all of its associated fees.
     * @param e 
     */
    public void violationSelected(ActionEvent e) {

        feeList.clear();

        feeList.addAll(selectedViolation.getViolatedEnfElement().getFeeList());

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

    public String getCurrentMode() {
        return currentMode;
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

    public ArrayList<CodeViolation> getViolationList() {
        return violationList;
    }

    public void setViolationList(ArrayList<CodeViolation> violationList) {
        this.violationList = violationList;
    }

    public FeeAssigned getSelectedAssignedFee() {
        return selectedAssignedFee;
    }

    public void setSelectedAssignedFee(FeeAssigned selectedAssignedFee) {
        this.selectedAssignedFee = selectedAssignedFee;
    }

    public ArrayList<FeeAssigned> getFeeAssignedList() {
        return feeAssignedList;
    }

    public void setFeeAssignedList(ArrayList<FeeAssigned> feeAssignedList) {
        this.feeAssignedList = feeAssignedList;
    }

    public ArrayList<FeeAssigned> getFilteredFeeAssignedList() {
        return filteredFeeAssignedList;
    }

    public void setFilteredFeeAssignedList(ArrayList<FeeAssigned> filteredFeeAssignedList) {
        this.filteredFeeAssignedList = filteredFeeAssignedList;
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

    public OccPeriodDataHeavy getCurrentOccPeriod() {
        return currentOccPeriod;
    }

    public void setCurrentOccPeriod(OccPeriodDataHeavy currentOccPeriod) {
        this.currentOccPeriod = currentOccPeriod;
    }

    public boolean editingOccPeriod() {
        return (getSessionBean().getNavStack().peekLastPage() != null && currentOccPeriod != null && currentDomain == EventDomainEnum.OCCUPANCY);
    }

    public boolean editingCECase() {
        return (getSessionBean().getNavStack().peekLastPage() != null && currentCase != null && currentDomain == EventDomainEnum.CODE_ENFORCEMENT);
    }

    public ArrayList<OccPeriodType> getTypeList() {
        return typeList;
    }

    public void setTypeList(ArrayList<OccPeriodType> typeList) {
        this.typeList = typeList;
    }

    public OccPeriodType getSelectedPeriodType() {
        return selectedPeriodType;
    }

    public void setSelectedPeriodType(OccPeriodType selectedPeriodType) {
        this.selectedPeriodType = selectedPeriodType;
    }

    public ArrayList<OccPeriodType> getFilteredTypeList() {
        return filteredTypeList;
    }

    public void setFilteredTypeList(ArrayList<OccPeriodType> filteredTypeList) {
        this.filteredTypeList = filteredTypeList;
    }

    public ArrayList<Fee> getWorkingFeeList() {
        return workingFeeList;
    }

    public void setWorkingFeeList(ArrayList<Fee> workingFeeList) {
        this.workingFeeList = workingFeeList;
    }

    public Fee getSelectedWorkingFee() {
        return selectedWorkingFee;
    }

    public void setSelectedWorkingFee(Fee selectedWorkingFee) {
        this.selectedWorkingFee = selectedWorkingFee;
    }

    public List<Fee> getAllFees() {
        return allFees;
    }

    public void setAllFees(List<Fee> allFees) {
        this.allFees = allFees;
    }

    public String getOccPeriodAddress() throws BObStatusException {

        PropertyCoordinator pc = getPropertyCoordinator();
        try {
            return pc.getPropertyByPropUnitID(currentOccPeriod.getPropertyUnitID()).getAddress();
        } catch (IntegrationException ex) {
            System.out.println(ex.toString());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to find property address!",
                            ""));
        }
        return "";

    }

    /**
     * This is used by the interface to display whether the selected fee is waived
     *
     * @return Whether or not the currently selected fee has been waived
     */
    public String isSelectedFeeWaived() {

        if (selectedAssignedFee.getWaivedBy() != null) {

            return "Yes";

        } else {

            return "No";
        }

    }

    /**
     *
     * @return
     */
    public String getCECaseAddress() throws BObStatusException {
        
        CaseCoordinator cc = getCaseCoordinator();
        try {
            return cc.cecase_assembleCECasePropertyUnitHeavy(currentCase).getProperty().getAddress();
        } catch (IntegrationException | SearchException ex) {
            System.out.println(ex);
        }
        return null;

    }

    public EventDomainEnum getCurrentDomain() {
        return currentDomain;
    }

    public void setCurrentDomain(EventDomainEnum currentDomain) {
        this.currentDomain = currentDomain;
    }

    public CECase getCurrentCase() {
        return currentCase;
    }

    public void setCurrentCase(CECase currentCase) {
        this.currentCase = (CECaseDataHeavy) currentCase;
    }

    public ArrayList<CodeViolation> getFilteredViolationList() {
        return filteredViolationList;
    }

    public void setFilteredViolationList(ArrayList<CodeViolation> filteredViolationList) {
        this.filteredViolationList = filteredViolationList;
    }

    public CodeViolation getSelectedViolation() {
        return selectedViolation;
    }

    public void setSelectedViolation(CodeViolation selectedViolation) {
        this.selectedViolation = selectedViolation;
    }

    public ArrayList<EnforcableCodeElement> getElementList() {
        return elementList;
    }

    public void setElementList(ArrayList<EnforcableCodeElement> elementList) {
        this.elementList = elementList;
    }

    public ArrayList<EnforcableCodeElement> getFilteredElementList() {
        return filteredElementList;
    }

    public void setFilteredElementList(ArrayList<EnforcableCodeElement> filteredElementList) {
        this.filteredElementList = filteredElementList;
    }

    public EnforcableCodeElement getSelectedCodeElement() {
        return selectedCodeElement;
    }

    public void setSelectedCodeElement(EnforcableCodeElement selectedCodeElement) {
        this.selectedCodeElement = selectedCodeElement;
    }

    public boolean isRedirected() {
        return redirected;
    }

    public void setRedirected(boolean redirected) {
        this.redirected = redirected;
    }

    public boolean isCurrentFeeSelected() {
        return currentFeeSelected;
    }

    public void setCurrentFeeSelected(boolean currentFeeSelected) {
        this.currentFeeSelected = currentFeeSelected;
    }

}