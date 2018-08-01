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
    private String addrPart;
    private String addrPartAllMunis;
    Connection con = null;
    
    private Property selectedProperty;
    private ArrayList<Property> propList;
    private UIInput addressInput;
    
    private int selectedMuniCode;
    
    /**
     * Creates a new instance of PropSearchBean
     */
    public PropertySearchBB() {

        
    } // close constructor
    
    public void searchForPropertiesAllMunis(ActionEvent event){
        System.out.println("PropSearchBean.searchForPropertiesAllMunis");
        PropertyIntegrator pi = new PropertyIntegrator();
        
        try {
            propList = pi.searchForProperties(addrPartAllMunis);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Your search completed with " + propList.size() + " results", ""));
            
            
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        " Unable to complete a search search! ", ""));
        }
    }
    
    //this method should correlate to the "Search only in Selected Muni"
    public void searchForPropertiesSingleMuni(ActionEvent event){
        System.out.println("PropSearchBean.searchForPropertiesSingleMuni");
        PropertyIntegrator pi = new PropertyIntegrator();
        
        try {
            propList = pi.searchForProperties(addrPart, selectedMuniCode);
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
    
    public String viewProperty(){
        System.out.println("PSearch.viewProperty");
        
        
        
        if(selectedProperty != null){
            getSessionBean().setActiveProp(selectedProperty);
            return "propertyProfile";
        } else {
            
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Please search for and select a property before trying to view its profile.", ""));
            
            return "";
        }
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
     * @return the addrPart
     */
    public String getAddrPart() {
        return addrPart;
    }

    /**
     * @param addrPart the addrPart to set
     */
    public void setAddrPart(String addrPart) {
        this.addrPart = addrPart;
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
    
    
    
}
