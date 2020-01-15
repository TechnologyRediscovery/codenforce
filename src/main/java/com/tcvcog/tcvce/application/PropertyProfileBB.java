package com.tcvcog.tcvce.application;


import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.BlobType;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PropertyDataHeavy;
import com.tcvcog.tcvce.entities.PropertyUseType;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import org.primefaces.event.FileUploadEvent;

/*
 * Copyright (C) 2018 Technology Rediscovery LLC
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
 * @author ellen bascomb of apt 31y
 */
public class PropertyProfileBB extends BackingBeanUtils implements Serializable{
    
    private PropertyDataHeavy currProp;
    
    
    
    private List<Person> filteredPersonList;
    
    private Municipality selectedMuni;
    private List<PropertyUseType> putList;
    
    
    /**
     * Creates a new instance of PropertyProfileBB
     */
    public PropertyProfileBB() {
    }
    
    @PostConstruct
    public void initBean(){
        PropertyCoordinator pc = getPropertyCoordinator();
        OccupancyIntegrator oi = getOccupancyIntegrator();
        SearchCoordinator sc = getSearchCoordinator();
        PropertyIntegrator pi = getPropertyIntegrator();
        
        currProp = (getSessionBean().getSessionProperty());
        
  
        selectedMuni = getSessionBean().getSessionMuni();

        try {
            putList = pi.getPropertyUseTypeList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }

     public String updateProperty(){
        PropertyCoordinator pc = getPropertyCoordinator();
        
        getSessionBean().getSessionPropertyList().add(0, currProp);
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Successfully updated property with ID " + getCurrProp().getPropertyID() 
                                + ", which is now your 'active property'", ""));
        return "propertyProfile";
    }
    
    
  
    
    private void refreshCurrPropWithLists(){
        PropertyCoordinator pc = getPropertyCoordinator();
        try {
            setCurrProp(pc.assemblePropertyDataHeavy(currProp, getSessionBean().getSessionUser().getMyCredential()));
        } catch (IntegrationException | BObStatusException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not update current property with lists | Exception details: " + ex.getMessage(), ""));
        }
        
    }
      
    
    public String addProperty(){
        //getSessionBean().setActiveProp(new Property());  // we do this after the prop has been inserted
        return "propertyAdd";
    }
    
 
    
    public void certifyDataFieldOccPeriod(ActionEvent ev){
        String fieldToCertify = null;
        FacesContext fc = getFacesContext();
        Map<String, String> params = fc.getExternalContext().getRequestParameterMap();
        fieldToCertify = params.get("certify-fieldid");
        System.out.println("PropertyProfileBB.certifyDateField | param value: " + fieldToCertify);
        switch(fieldToCertify){
            case "enddate":
                break;
            case "startdate":
                break;
            case "periodtype":
                break;
            case "authorization":
                break;
        }
        
    }
   
    
    public String viewPersonProfile(Person p){
        getSessionBean().getSessionPersonList().add(0,p);
        return "persons";
    }
    
    
    /**
     * @return the currentProperty
     */
    public PropertyDataHeavy getCurrProp() {
        return currProp;
    }
    
    
    
    /**
     * @param currentProperty the currentProperty to set
     */
    public void setCurrProp(PropertyDataHeavy currentProperty) {
        this.currProp = currentProperty;
    }
    
 
    
   
   

    /**
     * @return the photoList
     */
    public List<Person> getFilteredPersonList() {
        return filteredPersonList;
    }

    /**
     * @param photoList the photoList to set
     */
    public void setFilteredPersonList(List<Person> filteredPersonList) {
        this.filteredPersonList = filteredPersonList;
    }



    /**
     * @return the selectedMuni
     */
    public Municipality getSelectedMuni() {
        return selectedMuni;
    }

    /**
     * @param selectedMuni the selectedMuni to set
     */
    public void setSelectedMuni(Municipality selectedMuni) {
        this.selectedMuni = selectedMuni;
    }

 

    
    /**
     * @return the putList
     */
    public List<PropertyUseType> getPutList() {
        return putList;
    }

    /**
     * @param putList the putList to set
     */
    public void setPutList(List<PropertyUseType> putList) {
        this.putList = putList;
    }

   
 
   
 
}
