/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;


import com.tcvcog.tcvce.domain.IntegrationException;
import java.io.Serializable;
import java.sql.Connection;
import java.util.List;
import java.util.ArrayList;
import com.tcvcog.tcvce.entities.Property;
import javax.faces.event.ActionEvent;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIInput;

/** 
 *
 * @author Eric Darsow
 */
public class PropertySearchBB extends BackingBeanUtils implements Serializable {

    private String parid;
    private String address;
    private String houseNum;
    private String streetName;
    private String addrPartAllMunis;
    Connection con = null;
    private boolean allMunis;
    
    private Property selectedProperty;
    private ArrayList<Property> propList;
    private UIInput addressInput;
    
    private int selectedMuniCode;
    
    /**
     * Creates a new instance of PropSearchBean
     */
    public PropertySearchBB() {
        allMunis = false;

        
    } // close constructor
    
   
    public void searchForProperties(ActionEvent event){
        System.out.println("PropSearchBean.searchForPropertiesSingleMuni");
        PropertyIntegrator pi = new PropertyIntegrator();
        
        try {
            if(allMunis){
                propList = pi.searchForProperties(houseNum, streetName);
            } else {
                propList = pi.searchForProperties(houseNum, streetName, selectedMuniCode);
            }
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Your search completed with " + propList.size() + " results", ""));
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
    
    public String updateProperty(){
        
        if(selectedProperty != null){
            getSessionBean().setActiveProp(selectedProperty);
            return "propertyUpdate";
        } else {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Please search for and select a property before trying to view its profile.", ""));
            return "";
        }
    }

    /**
     * @return the parid
     */
    public String getParid() {
        return this.parid;
    }

    /**
     * @param parid the parid to set
     */
    public void setParid(String parid) {
        this.parid = parid;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return this.address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the houseNum
     */
    public String getHouseNum() {
        return houseNum;
    }

    /**
     * @param houseNum the houseNum to set
     */
    public void setHouseNum(String houseNum) {
        this.houseNum = houseNum;
    }

    /**
     * @return the propList
     */
    public List<Property> getPropList() {
        return propList;
    }

    /**
     * @param propList the propList to set
     */
    public void setPropList(ArrayList<Property> propList) {
        this.propList = propList;
    }

    /**
     * @return the selectedProperty
     */
    public Property getSelectedProperty() {
        return selectedProperty;
    }

    /**
     * @param selectedProperty the selectedProperty to set
     */
    public void setSelectedProperty(Property selectedProperty) {
        this.selectedProperty = selectedProperty;
    }

    /**
     * @return the addressInput
     */
    public UIInput getAddressInput() {
        return addressInput;
    }

    /**
     * @param addressInput the addressInput to set
     */
    public void setAddressInput(UIInput addressInput) {
        this.addressInput = addressInput;
    }

    /**
     * @return the selectedMuniCode
     */
    public int getSelectedMuniCode() {
        return selectedMuniCode;
    }

    /**
     * @param selectedMuniCode the selectedMuniCode to set
     */
    public void setSelectedMuniCode(int selectedMuniCode) {
        this.selectedMuniCode = selectedMuniCode;
    }

    /**
     * @return the addrPartAllMunis
     */
    public String getAddrPartAllMunis() {
        return addrPartAllMunis;
    }

    /**
     * @param addrPartAllMunis the addrPartAllMunis to set
     */
    public void setAddrPartAllMunis(String addrPartAllMunis) {
        this.addrPartAllMunis = addrPartAllMunis;
    }

    /**
     * @return the streetName
     */
    public String getStreetName() {
        return streetName;
    }

    /**
     * @param streetName the streetName to set
     */
    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    /**
     * @return the allMunis
     */
    public boolean isAllMunis() {
        return allMunis;
    }

    /**
     * @param allMunis the allMunis to set
     */
    public void setAllMunis(boolean allMunis) {
        this.allMunis = allMunis;
    }
    
    
    
}
