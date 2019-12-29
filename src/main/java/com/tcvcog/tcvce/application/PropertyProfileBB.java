package com.tcvcog.tcvce.application;


import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.CaseLifecycleException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.InspectionException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.BlobType;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyUnitDataHeavy;
import com.tcvcog.tcvce.entities.PropertyDataHeavy;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodType;
import com.tcvcog.tcvce.entities.search.QueryProperty;
import com.tcvcog.tcvce.entities.search.SearchParamsProperty;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import org.primefaces.event.FileUploadEvent;

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
    
    private PropertyDataHeavy currProp;
    private PropertyUnit currPropUnit;
    private PropertyUnitDataHeavy currPropUnitWithLists;
    private OccPeriod currOccPeriod;
    
    private int selectedPhotoID;
    
    private List<Person> filteredPersonList;
    
    private String parid;
    private String address;
    private String houseNum;
    private String streetName;
    private String addrPartAllMunis;
    private boolean allMunis;
    
    private List<Property> propList;
    private List<Property> filteredPropList;
    private UIInput addressInput;
    
    private Municipality selectedMuni;
    
    private OccPeriodType selectedOccPeriodType;
    private List<OccPeriodType> occPeriodTypeList;
    
    private SearchParamsProperty searchParams;
    private QueryProperty selectedPropQuery;
    private List<QueryProperty> queryList;
    
    /**
     * Creates a new instance of PropertyProfileBB
     */
    public PropertyProfileBB() {
    }
    
    @PostConstruct
    public void initBean(){
        PropertyIntegrator pi = getPropertyIntegrator();
        OccupancyIntegrator oi = getOccupancyIntegrator();
        SearchCoordinator sc = getSearchCoordinator();
        
        try {
            this.setCurrProp(pi.getPropertyDataHeavy(getSessionBean().getSessionProperty().getPropertyID()));
        } catch (IntegrationException | CaseLifecycleException | EventException | AuthorizationException ex) {
            System.out.println(ex);
        }
        setPropList(getSessionBean().getSessionPropertyList());
        setOccPeriodTypeList(getSessionBean().getSessionMuni().getProfile().getOccPeriodTypeList());
        setSelectedMuni(getSessionBean().getSessionMuni());

        setSelectedPropQuery(getSessionBean().getQueryProperty());
        setSearchParams(getSelectedPropQuery().getParmsList().get(0));
        try {
            setQueryList(sc.buildQueryPropertyList(s().getSessionUser(), getSessionBean().getSessionMuni()));
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    
        getSearchParams().setDate_startEnd_ctl(false);
        getSearchParams().setMuni_ctl(true);
        getSearchParams().setBobID_ctl(false);
        getSearchParams().setDate_relativeDates_ctl(false); 
    }

    public void searchForProperties(ActionEvent event){
        System.out.println("PropSearchBean.searchForPropertiesSingleMuni");
        PropertyIntegrator pi = getPropertyIntegrator();
        
        try {
            setPropList(pi.searchForProperties(getHouseNum(), getStreetName(), getSessionBean().getSessionMuni().getMuniCode()));
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
    
    public void searchForPropParams(ActionEvent event){
        System.out.println("PropSearchBean.searchwithparams");
        PropertyIntegrator pi = getPropertyIntegrator();
        SearchParamsProperty params = getSearchParams();
//        params.setAddressPart("%" + getHouseNum() + "%" + getStreetName() + "%");
//        params.setFilterByAddressPart(true);
//        params.setMuni(selectedMuni);
  
        try{
            System.out.println("Got to the setPropList point actionEvent propprofbean.searchforpropparam");
            setPropList(pi.searchForProperties(getSearchParams()));
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Your search completed with " + getPropList().size() + " results", ""));
        } catch (IntegrationException ex) {
            System.out.println("Search with params failed");
            Logger.getLogger(PropertyProfileBB.class.getName()).log(Level.SEVERE, null, ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to complete search! ", ""));
        }
    }
    
    public void executeQuery(ActionEvent event){
        System.out.println("Property Search for props by params");
        PropertyIntegrator pi = getPropertyIntegrator();
        
        try {            
            setPropList(pi.searchForProperties(getSearchParams()));
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Your search completed with " + getPropList().size() + " results", ""));
        } catch (IntegrationException ex) {
            Logger.getLogger(PropertyProfileBB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
  
    public String goToChanges() {
        
        return "unitchanges";
    }
    
      
    public void beginPropertyUnitChanges(ActionEvent ev){
        
    }
    
     /**
     * Adds a blank unit to propUnitsToAdd list. This newly-created unit can
     * then be selected and edited by the user.
     */
    public void addUnitToNewPropUnits() {
        PropertyUnit unitToAdd;
        PropertyCoordinator pc = getPropertyCoordinator();
        unitToAdd = pc.getNewPropertyUnit();
        unitToAdd.setUnitNumber("");
//        unitToAdd.setRental(false);
        unitToAdd.setNotes("");
        getCurrProp().getUnitList().add(unitToAdd);
        
//        clearAddUnitFormValues();
    }
    
    public void removePropertyUnitFromEditTable(PropertyUnit pu){
        getCurrProp().getUnitList().remove(pu);
        getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Zap!", ""));
        
    }
    
    public void deactivatePropertyUnit(PropertyUnit pu){
        PropertyIntegrator pi = getPropertyIntegrator();
        pu.setActive(false);
        try {
            pi.updatePropertyUnit(pu);
            getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Unit deactivated with ID " + pu.getUnitID(), ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Could not deactivate unit with ID " + pu.getUnitID(), ""));
        }
        
    }
    
       /**
     * Finalizes the unit list the user has created so that it can be compared
     * to the existing one in the database.
     *
     * @param ev
     */
    public void finalizeUnitList(ActionEvent ev) {
        PropertyIntegrator pi = getPropertyIntegrator();
        
        boolean missingUnitNum = false;
        boolean duplicateUnitNum = false;
        int duplicateNums = 0;
        //The above boolean is a flag to see if there is more than 1 of  Unit Number. The int to the left stores how many of a given number the loop below finds.

        for (PropertyUnit firstUnit : getCurrProp().getUnitList()) {
            duplicateNums = 0;

            firstUnit.setUnitNumber(firstUnit.getUnitNumber().replaceAll("(?i)unit", ""));

            if (firstUnit.getUnitNumber().compareTo("") == 0) {
                missingUnitNum = true;
                break; //break for performance reasons. Can be removed if breaks are not welcome here.
            }

            for (PropertyUnit secondUnit : getCurrProp().getUnitList()) {
                if (firstUnit.getUnitNumber().compareTo(secondUnit.getUnitNumber()) == 0) {
                    duplicateNums++;
                }
            }

            if (duplicateNums > 1) {
                duplicateUnitNum = true;
                break; //break for performance reasons. Can be removed if breaks are not welcome here.
            }
        }

        if (getCurrProp().getUnitList().isEmpty()) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please add at least one unit.", ""));
            
        } else if (missingUnitNum) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "All units must have a Unit Number", ""));

        } else if (duplicateUnitNum) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Some Units have the same Number", ""));

        } else {
            Iterator<PropertyUnit> iter = getCurrProp().getUnitList().iterator();
            while(iter.hasNext()){
                PropertyUnit pu = iter.next();
                if(pu.getUnitID() == 0){
                    try {
                        pu.setPropertyID(getCurrProp().getPropertyID());
                        pi.insertPropertyUnit(pu);
                        refreshCurrPropWithLists();
                        
                        getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_INFO,
                                        "Success! Inserted property unit: " + pu.getUnitNumber(), ""));
                    } catch (IntegrationException ex) {
                        getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        "Could not insert unit with number: " + pu.getUnitNumber(), ""));
                    }
                } else {
                    try {
                        pu.setPropertyID(getCurrProp().getPropertyID());
                        pi.updatePropertyUnit(pu);
                        refreshCurrPropWithLists();
                        getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_INFO,
                                        "Success! Updated property unit: " + pu.getUnitNumber(), ""));
                    } catch (IntegrationException ex) {
                        getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        "Could not update unit with number: " + pu.getUnitNumber(), ""));
                    }
                }
            }
        }
    }
    
    private void refreshCurrPropWithLists(){
        PropertyIntegrator pi = getPropertyIntegrator();
        try {
            setCurrProp(pi.getPropertyDataHeavy(getCurrProp().getPropertyID()));
        } catch (IntegrationException | CaseLifecycleException | EventException | AuthorizationException ex) {
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
    
    public String openCECase(){
        getSessionBean().setSessionProperty(getCurrProp());
        return "addNewCase";
    }
    
    public String viewCase(CECase c){
        getSessionBean().setSessionCECase(c);
        return "ceCases";
    }
    
    public String manageOccPeriod(OccPeriod op){
        setCurrOccPeriod(op);
        getSessionBean().setSessionOccPeriod(getCurrOccPeriod());
        return "occPeriodStatus";
        
    }
    
    public void initiateNewOccPeriodCreation(PropertyUnit pu){
        setSelectedOccPeriodType(null);
        setCurrPropUnit(pu);
    }
    
    public String addNewOccPeriod(){
        
        OccupancyCoordinator oc = getOccupancyCoordinator();
        OccupancyIntegrator oi = getOccupancyIntegrator();
        try {
            if(getSelectedOccPeriodType() != null){
                System.out.println("PropertyProfileBB.initateNewOccPeriod | selectedType: " + getSelectedOccPeriodType().getTypeID());
                setCurrOccPeriod(oc.initializeNewOccPeriod(getCurrProp(), getCurrPropUnit(), getSelectedOccPeriodType(), getSessionBean().getSessionUser(), getSessionBean().getSessionMuni()));
                getCurrOccPeriod().setType(getSelectedOccPeriodType());
                int newID = 0;
                System.out.println("PropertyProfileBB.initateNewOccPeriod | currOccPeriod: " + getCurrOccPeriod().getPeriodID());
                newID = oc.insertNewOccPeriod(getCurrOccPeriod(), getSessionBean().getSessionUser());
                getSessionBean().setSessionOccPeriod(oi.getOccPeriod(newID));
            } else {
                getFacesContext().addMessage(null,
                                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                            "Please select a period type" , ""));
                return "";
            }
        } catch (EventException | AuthorizationException | CaseLifecycleException | ViolationException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        "Could not commit new occ period: " , ""));
            return "";
        } catch (InspectionException ex) { 
            System.out.println(ex);
            return "";
        }
        return "inspection";
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
    
    public void manageProperty(Property prop){
        PropertyIntegrator pi = getPropertyIntegrator();
        UserIntegrator ui = getUserIntegrator();
        SystemIntegrator si = getSystemIntegrator();
        
        try {
            setCurrProp(pi.getPropertyDataHeavy(prop.getPropertyID()));
            si.logObjectView_OverwriteDate(getSessionBean().getSessionUser(), prop);
            getSessionBean().setSessionProperty(prop);
            getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_INFO,
                                        "Managing property at " + prop.getAddress() , ""));
        } catch (IntegrationException | CaseLifecycleException | EventException | AuthorizationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        ex.getMessage(), ""));
        }
    }
    
    /**
     * @return the currentProperty
     */
    public PropertyDataHeavy getCurrProp() {
        PropertyIntegrator pi = getPropertyIntegrator();
        try {
            if(currProp == null){
                currProp = pi.getPropertyDataHeavy(getSessionBean().getSessionProperty().getPropertyID());
            }
        } catch (IntegrationException | CaseLifecycleException | EventException | AuthorizationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        ex.getMessage(), ""));
        }
        return currProp;
    }
    
    /**
     * delete blob if the blob is a photo
     * @param blobID
     */
    public void deletePhoto(int blobID){
        try {
            Blob blob = getBlobCoordinator().getBlob(blobID);
            if(blob.getType() == BlobType.PHOTO){
                getBlobCoordinator().deleteBlob(blobID);
            }
            }
        catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }
    
    public void handleFileUpload(FileUploadEvent ev){
        Blob blob = getBlobCoordinator().getNewBlob();
        blob.setBytes(ev.getFile().getContents());
        blob.setType(BlobType.PHOTO); // TODO: BAD CHANGE THIS SOON
        
        // DO nothing because I'm moving on to other issues,
        // need to be able to compile before I can do much in the way of testing
    }
    
    /**
     * @param currentProperty the currentProperty to set
     */
    public void setCurrProp(PropertyDataHeavy currentProperty) {
        this.currProp = currentProperty;
    }
    
    public String updateProperty(){
        getSessionBean().getSessionPropertyList().add(0, getCurrProp());
        return "propertyUpdate";
        
    }
    
        /**
     * @return the propList
     */
    public List<Property> getPropList() {
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
    public void setPropList(List<Property> propList) {
        this.propList = propList;
    }

    /**
     * @param filteredPropList the filteredPropList to set
     */
    public void setFilteredPropList(List<Property> filteredPropList) {
        this.filteredPropList = filteredPropList;
    }

    

    /**
     * @return the selectedPhotoID
     */
    public int getSelectedPhotoID() {
        return selectedPhotoID;
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
     * @return the currPropUnit
     */
    public PropertyUnit getCurrPropUnit() {
        return currPropUnit;
    }

    /**
     * @param currPropUnit the currPropUnit to set
     */
    public void setCurrPropUnit(PropertyUnit currPropUnit) {
        this.currPropUnit = currPropUnit;
    }

    /**
     * @return the currPropUnitWithLists
     */
    public PropertyUnitDataHeavy getCurrPropUnitWithLists() {
        return currPropUnitWithLists;
    }

    /**
     * @param currPropUnitWithLists the currPropUnitWithLists to set
     */
    public void setCurrPropUnitWithLists(PropertyUnitDataHeavy currPropUnitWithLists) {
        this.currPropUnitWithLists = currPropUnitWithLists;
    }

    /**
     * @return the occPeriodTypeList
     */
    public List<OccPeriodType> getOccPeriodTypeList() {
        return occPeriodTypeList;
    }

    /**
     * @param occPeriodTypeList the occPeriodTypeList to set
     */
    public void setOccPeriodTypeList(List<OccPeriodType> occPeriodTypeList) {
        this.occPeriodTypeList = occPeriodTypeList;
    }

    /**
     * @return the currOccPeriod
     */
    public OccPeriod getCurrOccPeriod() {
        return currOccPeriod;
    }

    /**
     * @param currOccPeriod the currOccPeriod to set
     */
    public void setCurrOccPeriod(OccPeriod currOccPeriod) {
        this.currOccPeriod = currOccPeriod;
    }

    /**
     * @return the selectedOccPeriodType
     */
    public OccPeriodType getSelectedOccPeriodType() {
        return selectedOccPeriodType;
    }

    /**
     * @param selectedOccPeriodType the selectedOccPeriodType to set
     */
    public void setSelectedOccPeriodType(OccPeriodType selectedOccPeriodType) {
        this.selectedOccPeriodType = selectedOccPeriodType;
    }

    /**
     * @return the photoID
     */
    public int getPhotoID() {
        return this.getSelectedPhotoID();
    }

    /**
     * @param photoID the photoID to set
     */
    public void setPhotoID(int photoID) {
        this.setSelectedPhotoID(photoID);
    }

    /**
     * @param selectedPhotoID the selectedPhotoID to set
     */
    public void setSelectedPhotoID(int selectedPhotoID) {
        this.selectedPhotoID = selectedPhotoID;
    }

    /**
     * @param addressInput the addressInput to set
     */
    public void setAddressInput(UIInput addressInput) {
        this.addressInput = addressInput;
    }

    /**
     * @return the searchParams
     */
    public SearchParamsProperty getSearchParams() {
        return searchParams;
    }

    /**
     * @param searchParams the searchParams to set
     */
    public void setSearchParams(SearchParamsProperty searchParams) {
        this.searchParams = searchParams;
    }

    /**
     * @return the selectedPropQuery
     */
    public QueryProperty getSelectedPropQuery() {
        return selectedPropQuery;
    }

    /**
     * @param selectedPropQuery the selectedPropQuery to set
     */
    public void setSelectedPropQuery(QueryProperty selectedPropQuery) {
        this.selectedPropQuery = selectedPropQuery;
    }

    /**
     * @return the queryList
     */
    public List<QueryProperty> getQueryList() {
        return queryList;
    }

    /**
     * @param queryList the queryList to set
     */
    public void setQueryList(List<QueryProperty> queryList) {
        this.queryList = queryList;
    }

   
 
   
 
}
