package com.tcvcog.tcvce.application;


import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyWithLists;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIInput;
import javax.faces.event.ActionEvent;

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

/**
 *
 * @author Eric C. Darsow
 */
public class PropertyProfileBB extends BackingBeanUtils implements Serializable{
    private PropertyWithLists currentProperty;
    private ArrayList<Person> propertyPersonList;
    private Person selectedPerson;
    private ArrayList<Person> filteredPersonList;
    
    private ArrayList<CECase> ceCaseList;
    private CECase selectedCECase;
    
    private ArrayList<CEActionRequest> ceActionRequestList;
    
    private String parid;
    private String address;
    private String houseNum;
    private String streetName;
    private String addrPartAllMunis;
    private boolean allMunis;
    
    private ArrayList<Property> propList;
    private List<Property> filteredPropList;
    private UIInput addressInput;
    
    private int selectedMuniCode;

    /**
     * @return the parid
     */
    public String getParid() {
        return parid;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @return the houseNum
     */
    public String getHouseNum() {
        return houseNum;
    }

    /**
     * @return the streetName
     */
    public String getStreetName() {
        return streetName;
    }

    /**
     * @return the addrPartAllMunis
     */
    public String getAddrPartAllMunis() {
        return addrPartAllMunis;
    }

    /**
     * @return the allMunis
     */
    public boolean isAllMunis() {
        return allMunis;
    }

    /**
     * @return the propList
     */
    public ArrayList<Property> getPropList() {
        return propList;
    }

    /**
     * @return the filteredPropList
     */
    public List<Property> getFilteredPropList() {
        return filteredPropList;
    }

    /**
     * @return the addressInput
     */
    public UIInput getAddressInput() {
        return addressInput;
    }

    /**
     * @return the selectedMuniCode
     */
    public int getSelectedMuniCode() {
        return selectedMuniCode;
    }

    /**
     * @param parid the parid to set
     */
    public void setParid(String parid) {
        this.parid = parid;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @param houseNum the houseNum to set
     */
    public void setHouseNum(String houseNum) {
        this.houseNum = houseNum;
    }

    /**
     * @param streetName the streetName to set
     */
    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    /**
     * @param addrPartAllMunis the addrPartAllMunis to set
     */
    public void setAddrPartAllMunis(String addrPartAllMunis) {
        this.addrPartAllMunis = addrPartAllMunis;
    }

    /**
     * @param allMunis the allMunis to set
     */
    public void setAllMunis(boolean allMunis) {
        this.allMunis = allMunis;
    }

    /**
     * @param propList the propList to set
     */
    public void setPropList(ArrayList<Property> propList) {
        this.propList = propList;
    }

    /**
     * @param filteredPropList the filteredPropList to set
     */
    public void setFilteredPropList(List<Property> filteredPropList) {
        this.filteredPropList = filteredPropList;
    }

    /**
     * @param addressInput the addressInput to set
     */
    public void setAddressInput(UIInput addressInput) {
        this.addressInput = addressInput;
    }

    /**
     * @param selectedMuniCode the selectedMuniCode to set
     */
    public void setSelectedMuniCode(int selectedMuniCode) {
        this.selectedMuniCode = selectedMuniCode;
    }

    /**
     * Creates a new instance of PropertyProfileBB
     */
    public PropertyProfileBB() {
    }
    
     public void searchForProperties(ActionEvent event){
        System.out.println("PropSearchBean.searchForPropertiesSingleMuni");
        PropertyIntegrator pi = new PropertyIntegrator();
        
        try {
            if(isAllMunis()){
                setPropList(pi.searchForProperties(getHouseNum(), getStreetName()));
            } else {
                setPropList(pi.searchForProperties(getHouseNum(), getStreetName(), getSelectedMuniCode()));
            }
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Your search completed with " + getPropList().size() + " results", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to complete search! ", ""));
        }
    }
    
    public String viewProperty(Property prop){
        getSessionBean().setActiveProp(prop);
        return "propertyProfile";
    }
    
    public String openCECase(Property prop){
        getSessionBean().setActiveProp(prop);
        return "addNewCase";
    }
    
    
    
    public String createCase(){
        
        
        return "addNewCase";
    }
    
    public String viewPersonProfile(Person p){
        System.out.println("PropertyProfileBB.viewPersonProfile");
        getSessionBean().setActivePerson(selectedPerson);
        return "personProfile";
    }
    
    /**
     * @return the currentProperty
     */
    public Property getCurrentProperty() {
        PropertyIntegrator pi = getPropertyIntegrator();
        try {
            currentProperty = pi.getPropertyWithLists(getSessionBean().getActiveProp().getPropertyID());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        return currentProperty;
    }
    
    public String viewCase(CECase c){
        
        return "caseProfile";
    }

    /**
     * @param currentProperty the currentProperty to set
     */
    public void setCurrentProperty(PropertyWithLists currentProperty) {
        this.currentProperty = currentProperty;
    }
    
    public String updateProperty(){
        System.out.println("PropertyProfileBB.updateProperty");
        return "propertyUpdate";
        
    }

    /**
     * @return the propertyPersonList
     */
    public ArrayList<Person> getPropertyPersonList() {
        propertyPersonList = currentProperty.getPropertyPersonList();
        return propertyPersonList;
    }

    /**
     * @return the ceCaseList
     */
    public ArrayList<CECase> getCeCaseList() {
        ceCaseList = currentProperty.getPropertyCaseList();
        return ceCaseList;
    }

    /**
     * @return the ceActionRequestList
     */
    public ArrayList<CEActionRequest> getCeActionRequestList() {
        return ceActionRequestList;
    }

    /**
     * @param propertyPersonList the propertyPersonList to set
     */
    public void setPropertyPersonList(ArrayList<Person> propertyPersonList) {
        this.propertyPersonList = propertyPersonList;
    }

    /**
     * @param ceCaseList the ceCaseList to set
     */
    public void setCeCaseList(ArrayList<CECase> ceCaseList) {
        this.ceCaseList = ceCaseList;
    }

    /**
     * @param ceActionRequestList the ceActionRequestList to set
     */
    public void setCeActionRequestList(ArrayList<CEActionRequest> ceActionRequestList) {
        this.ceActionRequestList = ceActionRequestList;
    }

    /**
     * @return the selectedPerson
     */
    public Person getSelectedPerson() {
        return selectedPerson;
    }

    /**
     * @param selectedPerson the selectedPerson to set
     */
    public void setSelectedPerson(Person selectedPerson) {
        this.selectedPerson = selectedPerson;
    }

    /**
     * @return the filteredPersonList
     */
    public ArrayList<Person> getFilteredPersonList() {
        return filteredPersonList;
    }

    /**
     * @param filteredPersonList the filteredPersonList to set
     */
    public void setFilteredPersonList(ArrayList<Person> filteredPersonList) {
        this.filteredPersonList = filteredPersonList;
    }

    /**
     * @return the selectedCECase
     */
    public CECase getSelectedCECase() {
        return selectedCECase;
    }

    /**
     * @param selectedCECase the selectedCECase to set
     */
    public void setSelectedCECase(CECase selectedCECase) {
        this.selectedCECase = selectedCECase;
    }  
}
