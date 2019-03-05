package com.tcvcog.tcvce.application;


import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Photograph;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyWithLists;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
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
    
    private PropertyWithLists currProp;
    private ArrayList<Person> filteredPersonList;
    private ArrayList<Person> pList;
    
    private String parid;
    private String address;
    private String houseNum;
    private String streetName;
    private String addrPartAllMunis;
    private boolean allMunis;
    
    private ArrayList<Property> propList;
    private List<Property> filteredPropList;
    private UIInput addressInput;
    
    private int selectedPhotoID;
    private ArrayList<Photograph> photoList;
    
    private ArrayList<CEActionRequest> ceActionRequestList;

    
    /**
     * Creates a new instance of PropertyProfileBB
     */
    public PropertyProfileBB() {
    }
    
    public void handlePhotoUpload(FileUploadEvent ev){
        if(ev == null){
            System.out.println("PhotoBB.handleFileUpload | event: null");
            return;
        }
       
        System.out.println("PhotoBB.handleFileUpload | event: " + ev.toString());
        try {
            
            ImageServices is = getImageServices();
            Photograph ph = new Photograph();
            ph.setPhotoBytes(ev.getFile().getContents());
            ph.setDescription("hello photo!");
            ph.setTypeID(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("photoTypeId")));
            ph.setTimeStamp(LocalDateTime.now());
            is.storePhotograph(ph);
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        this.getPhotoList();
    }
    
    public String createCase(){
        
        
        try {
            setPropList(pi.searchForProperties(getHouseNum(), getStreetName(), getSessionBean().getActiveMuni().getMuniCode()));
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
    
    public String openCECase(){
        getSessionBean().setActiveProp(currProp);
        return "addNewCase";
    }
    
    public String viewCase(CECase c){
        getSessionBean().setcECase(c);
        return "ceCases";
    }
    
    
    
    
    public String viewPersonProfile(Person p){
        System.out.println("PropertyProfileBB.viewPersonProfile");
        getSessionBean().setActivePerson(p);
        return "personProfile";
    }
    
    public void manageProperty(Property prop){
        PropertyIntegrator pi = getPropertyIntegrator();
        try {
            currProp = pi.getPropertyWithLists(prop.getPropertyID());
            System.out.println("PropertyProfileBB.manageProperty | curr Prop: " + currProp.getAddress());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }
    
    /**
     * @return the currentProperty
     */
    public PropertyWithLists getCurrProp() {
        PropertyIntegrator pi = getPropertyIntegrator();
        try {
            if(currProp == null){
                currProp = pi.getPropertyWithLists(getSessionBean().getActiveProp().getPropertyID());
            }
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        return currProp;
    }
    
    /**
     * @param currentProperty the currentProperty to set
     */
    public void setCurrProp(PropertyWithLists currentProperty) {
        this.currProp = currentProperty;
    }
    
    public String updateProperty(){
        getSessionBean().setActivePropWithList(currProp);
        System.out.println("PropertyProfileBB.updateProperty");
        return "propertyUpdate";
        
    }
    
        /**
     * @return the propList
     */
    public ArrayList<Property> getPropList() {
        return propList;
    }

    
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
    public void setSelectedCECase(CECase selectedCECase) {
        this.selectedCECase = selectedCECase;
    }  

    /**
     * @return the selectedPhotoID
     */
    public int getSelectedPhotoID() {
        return selectedPhotoID;
    }

    /**
     * @param selectedPhotoID the selectedPhotoID to set
     */
    public void setSelectedPhotoID(int selectedPhotoID) {
        this.selectedPhotoID = selectedPhotoID;
    }

    /**
     * @return the photoList
     */
    public ArrayList<Photograph> getPhotoList() {
        ImageServices is = getImageServices();
        try {
            return is.getAllPhotographs();
        } catch (IntegrationException ex) {
            Logger.getLogger(PropertyProfileBB.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * @param photoList the photoList to set
     */
    public void setPhotoList(ArrayList<Photograph> photoList) {
        this.photoList = photoList;
    }

    /**
     * @return the pList
     */
    public ArrayList<Person> getpList() throws IntegrationException {
            PropertyIntegrator pi = getPropertyIntegrator();
        if(pList == null || currProp == null){
            pList= pi.getPersonIntegrator().getPersonList(selectedMuniCode);
        }
        return pList;
    }

    /**
     * @param pList the pList to set
     */
    public void setpList(ArrayList<Person> pList) {
        this.pList = pList;
    }

   
 
   
 
}
