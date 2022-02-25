/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.BOBSource;
import com.tcvcog.tcvce.entities.MailingAddress;
import com.tcvcog.tcvce.entities.MailingCityStateZip;
import com.tcvcog.tcvce.entities.MailingStreet;
import com.tcvcog.tcvce.entities.search.QueryMailingCityStateZip;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 * The official backing bean for address management
 * @author Ellen Bascomb of Apartment 31Y
 */
public  class   AddressBB 
        extends BackingBeanUtils{

    private MailingStreet currentStreet;
    private List<MailingStreet> streetList;
    private List<MailingStreet> streetListFiltered;
    private boolean editModeCurrentStreet;
    
    private MailingAddress currentAddress;
    private List<MailingAddress> mailingAddressList;
    private List<MailingAddress> mailingAddressListFiltered;
    
    private boolean editModeCurrentAddress;
    private MailingCityStateZip currentCityStateZip;
    private List<MailingCityStateZip> cityStateZipListFiltered;
    
    private String formBuildingNo;
    private String formStreet;
    private boolean formPOBox;
    private boolean formAddressVerified;
    private String formCity;
    private String formZip;
    
    private List<BOBSource> addressSourceList;
    
    // SEARCH
    private List<QueryMailingCityStateZip> qcszEnumList;
    private QueryMailingCityStateZip selectedCSZQuery;
    
    
    /**
     * Creates a new instance of AddressBB
     */
    public AddressBB() {
    
    }
  @PostConstruct
    public void initBean(){
        SystemCoordinator sc = getSystemCoordinator();
        addressSourceList = sc.getBobSourceListComplete();
        
        SearchCoordinator srchc = getSearchCoordinator();
        qcszEnumList = srchc.buildQueryMailingCityStateZipList(getSessionBean().getSessUser().getMyCredential());
        if(qcszEnumList != null && !qcszEnumList.isEmpty()){
            selectedCSZQuery = qcszEnumList.get(0);
        }
        
        // LOAD UP OUR FILTERED LISTS 
        cityStateZipListFiltered = new ArrayList<>();
        streetListFiltered = new ArrayList<>();
        mailingAddressListFiltered = new ArrayList<>();
        
        
    }
    
    
    /**
     * Listener for user requests to execute the selected search
     * @param ev 
     */
    public void onQueryCSZExecuteButtonChange(ActionEvent ev){
        SearchCoordinator sc = getSearchCoordinator();
        try {
            sc.runQuery(selectedCSZQuery);
            getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_INFO,
                         "Search Returned " + selectedCSZQuery.getResults().size() + " records!",""));
        } catch (SearchException ex) {
            System.out.println(ex);
               getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Search error!",""));
        }
        
        
    }
    
    /**
     * listener for changes in the query drop down dialog
     * @param ev 
     */
    public void onQueryChange(){
        System.out.println("AddressBB.onQueryChange | Selected Zip Query: " + selectedCSZQuery.getQueryTitle());
        
        
    }
    
    /**
     * Flushes out old query
     * @param ev 
     */
    public void onQueryResetButtonChange(ActionEvent ev){
        selectedCSZQuery = null;
        
        SearchCoordinator srchc = getSearchCoordinator();
        qcszEnumList = srchc.buildQueryMailingCityStateZipList(getSessionBean().getSessUser().getMyCredential());
         getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_INFO,
                         "Search system reset",""));
    }
    
    /**
     * Listener for user requests to close the search dialog
     * @param ev 
     */
    public void onQueryOperationCancelButtonChange(ActionEvent ev){
        //nothing to do here
        
        
    }
    
    /**
     * User requests to make their chosen CSZ result record the session active one
     * @param mcsz 
     */
    public void selectCityStateZip(MailingCityStateZip mcsz){
        getSessionBean().setSessMailingCityStateZip(mcsz);
        currentCityStateZip = mcsz;
        if(currentAddress != null && currentAddress.getStreet() != null){
            currentAddress.getStreet().setCityStateZip(currentCityStateZip);
        }
        
        searchForMailingStreet();
        
        
    }
    
     /**********************************************************/
    /************** Mailing address INFRASTRUCTURE *********************/
    /**********************************************************/
    
    /**
     * Listener for user requests to view street info
     * @param ms the MailingStreet to query
     */
    public void onStreetViewEditLinkClick(MailingStreet ms){
        currentStreet = ms;
    }
    
    /**
     * Listener for user requests to create a new street
     */
    public void onMailingStreetCreateInitButtonChange(){
        PropertyCoordinator pc = getPropertyCoordinator();
        currentStreet = pc.getMailingStreetSkeleton(currentCityStateZip);
    }
    
    /**
     * Listener for user requests to see building Numbers
     * connected to this street. Initiates DB search of this
     * Street in this ZIP Code
     * @param ms the MailingStreet to query
     */
    public void onStreetSelectLinkClick(MailingStreet ms){
        System.out.println("AddressBB.onStreetSelectLinkClick");
        currentStreet = ms;
        PropertyCoordinator pc = getPropertyCoordinator();
        try {
            mailingAddressList = pc.getMailingAddressListByStreet(currentStreet);
            if(mailingAddressList != null){
                getFacesContext().addMessage(null,
                               new FacesMessage(FacesMessage.SEVERITY_INFO,
                                       "Search for addresses returned " + mailingAddressList.size(),""));
            } 
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                           new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                   "Could not find Addresses by Street, Sorry" ,""));
        } 
    }
    
    
    
    /**
     * Listener for user requests to search for a street
     * @param ev 
     */
    public void onSearchForMailingStreet(ActionEvent ev){
        
        searchForMailingStreet();
    }
    
    
    /**
     * Undertakes a street search
     */
    public void searchForMailingStreet(){
        PropertyCoordinator pc = getPropertyCoordinator();
        System.out.println("AddressBB.searchForMailingStreet");
        try {
            streetList = pc.searchForMailingStreet(formStreet, currentCityStateZip);
            if(streetList != null){
                getFacesContext().addMessage(null,
                           new FacesMessage(FacesMessage.SEVERITY_INFO,
                                   "Search for Streets returned " + streetList.size(),""));
            } 
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                               "Fatal: Could not search for streets, sorry! ",""));
            
        } 
    }
    
    
     /**
     * Listener for user requests to change to or from edit mode on mailing address
     * @param ev 
     */
    public void onToggleStreetEditModeButtonChange(ActionEvent ev){
        toggleStreetEditMode();
    }
    
    /**
     * Toggles the mailing address edit mode
     */
    public void toggleStreetEditMode(){
        PropertyCoordinator pc = getPropertyCoordinator();
        System.out.println("AddressBB.toggleStreetEditMode");
        if(editModeCurrentStreet){
            
            try {
                if(currentStreet.getStreetID() == 0){
                    pc.insertMailingStreet(currentStreet, getSessionBean().getSessUser());
                    getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_INFO,
                               "Street Insert successful",""));
                } else {
                    pc.updateMailingStreet(currentStreet, getSessionBean().getSessUser());
                    getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_INFO,
                               "Street update successful",""));
                }
            } catch (BObStatusException | IntegrationException ex) {
                System.out.println(ex);
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not update or add  this street",""));
            } 
        }
        editModeCurrentStreet = !editModeCurrentStreet;
        
    }
    
    
    /**
     * Listener for user requests to start the process of adding a mailing address
     * @param ev 
     */
    public void onMailingStreetInitButtonChange(ActionEvent ev){
        PropertyCoordinator pc = getPropertyCoordinator();
        currentStreet = pc.getMailingStreetSkeleton(currentCityStateZip);
        
    }

    /**
     * Listener for user requests to abort street edit
     * @param ev 
     */
    public void onMailingStreetEditAbort(ActionEvent ev){
        System.out.println("AddressBB.onMailingStreetEditAbort");
    }
    
    /**
     * Listener to start the street deac process
     */
    public void onMailingStreetDeactivateInitLinkClick(ActionEvent ev){
        System.out.println("AddressBB.onMailingStreetDeactivateInit");
    }
    
    /**
     * Listener for user requests to confirm delete of street
     */
    public void onMailingStreetDeactivateCommit(){
        PropertyCoordinator pc = getPropertyCoordinator();
        System.out.println("AddressBB.onMailingStreetDeactivateCommit");
        try {
            pc.deactivateMailingStreet(currentStreet, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_INFO,
                       "Street deactivated",""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_ERROR,
                       "Could not remove this street",""));
        } 
        
    }
    

    /**
     * Listener for user requests to view the current address
     * @param ev 
     */
    public void onMailingAddressViewCurrentAddress(ActionEvent ev){
        getSessionBean().setSessMailingAddress(currentAddress);
        
    }
    
    /**
     * Listener to view current Maling addresss
     * @param ma 
     */
    public void onMailingAddressViewEditLinkClick(MailingAddress ma){
        currentAddress = ma;
        getSessionBean().setSessMailingAddress(currentAddress);
    }
    
    /**
     * Listener for user requests to change to or from edit mode on mailing address
     * @param ev 
     */
    public void onToggleMailingAddressEditModeButtonChange(ActionEvent ev){
        toggleMailingAddressEditMode();
    }
    
    /**
     * Toggles the mailing address edit mode
     */
    public void toggleMailingAddressEditMode(){
        PropertyCoordinator pc = getPropertyCoordinator();
        System.out.println("AddressBB.toggleMailingAddressEditMode");
        if(isEditModeCurrentAddress()){
            
            try {
                if(currentAddress.getAddressID() == 0){
                    pc.insertMailingAddress(currentAddress, getSessionBean().getSessUser());
                    getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_INFO,
                               "Address Insert successful",""));
                } else {
                    pc.updateMailingAddress(currentAddress, getSessionBean().getSessUser());
                    getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_INFO,
                               "Address update successful",""));
                }
            } catch (BObStatusException | IntegrationException ex) {
                System.out.println(ex);
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not update or delete this address",""));
            } 
        }
        setEditModeCurrentAddress(!isEditModeCurrentAddress());
        
    }
    
    
    /**
     * Listener for user requests to start the process of adding a mailing address
     * @param ev 
     */
    public void onMailingAddressAddInitButtonChange(ActionEvent ev){
        PropertyCoordinator pc = getPropertyCoordinator();
        currentAddress = pc.getMailingAddressSkeleton();
        editModeCurrentAddress = true;
        
    }
    
    /**
     * Listener for user requests to abort any mailing address operation
     * @param ev 
     */
    public void onMailingAddressAbortOperationButtonChange(ActionEvent ev){
        editModeCurrentAddress = false;
    }
    
    
    
    /************************************************************/
    /************** GETTERS AND SETTERS    **********************/
    /************************************************************/
    
    
    
    /**
     * @return the currentAddress
     */
    public MailingAddress getCurrentAddress() {
        return currentAddress;
    }

    /**
     * @return the editModeCurrentAddress
     */
    public boolean isAddressEditMode() {
        return isEditModeCurrentAddress();
    }

    /**
     * @return the currentCityStateZip
     */
    public MailingCityStateZip getCurrentCityStateZip() {
        return currentCityStateZip;
    }

    /**
     * @return the formBuildingNo
     */
    public String getFormBuildingNo() {
        return formBuildingNo;
    }

    /**
     * @return the formStreet
     */
    public String getFormStreet() {
        return formStreet;
    }

    /**
     * @return the formCity
     */
    public String getFormCity() {
        return formCity;
    }

    /**
     * @return the formZip
     */
    public String getFormZip() {
        return formZip;
    }

    /**
     * @param currentAddress the currentAddress to set
     */
    public void setCurrentAddress(MailingAddress currentAddress) {
        this.currentAddress = currentAddress;
    }

    /**
     * @param addressEditMode the editModeCurrentAddress to set
     */
    public void setAddressEditMode(boolean addressEditMode) {
        this.setEditModeCurrentAddress(addressEditMode);
    }

    /**
     * @param currentCityStateZip the currentCityStateZip to set
     */
    public void setCurrentCityStateZip(MailingCityStateZip currentCityStateZip) {
        this.currentCityStateZip = currentCityStateZip;
    }

    /**
     * @param formBuildingNo the formBuildingNo to set
     */
    public void setFormBuildingNo(String formBuildingNo) {
        this.formBuildingNo = formBuildingNo;
    }

    /**
     * @param formStreet the formStreet to set
     */
    public void setFormStreet(String formStreet) {
        this.formStreet = formStreet;
    }

    /**
     * @param formCity the formCity to set
     */
    public void setFormCity(String formCity) {
        this.formCity = formCity;
    }

    /**
     * @param formZip the formZip to set
     */
    public void setFormZip(String formZip) {
        this.formZip = formZip;
    }

    /**
     * @return the addressSourceList
     */
    public List<BOBSource> getAddressSourceList() {
        return addressSourceList;
    }

    /**
     * @param addressSourceList the addressSourceList to set
     */
    public void setAddressSourceList(List<BOBSource> addressSourceList) {
        this.addressSourceList = addressSourceList;
    }

    /**
     * @return the qcszEnumList
     */
    public List<QueryMailingCityStateZip> getQcszEnumList() {
        return qcszEnumList;
    }

    /**
     * @param qcszEnumList the qcszEnumList to set
     */
    public void setQcszEnumList(List<QueryMailingCityStateZip> qcszEnumList) {
        this.qcszEnumList = qcszEnumList;
    }

    /**
     * @return the selectedCSZQuery
     */
    public QueryMailingCityStateZip getSelectedCSZQuery() {
        return selectedCSZQuery;
    }

    /**
     * @param selectedCSZQuery the selectedCSZQuery to set
     */
    public void setSelectedCSZQuery(QueryMailingCityStateZip selectedCSZQuery) {
        this.selectedCSZQuery = selectedCSZQuery;
    }

    /**
     * @return the streetList
     */
    public List<MailingStreet> getStreetList() {
        return streetList;
    }

    /**
     * @return the editModeCurrentStreet
     */
    public boolean isEditModeCurrentStreet() {
        return editModeCurrentStreet;
    }

    /**
     * @return the editModeCurrentAddress
     */
    public boolean isEditModeCurrentAddress() {
        return editModeCurrentAddress;
    }

    /**
     * @param streetList the streetList to set
     */
    public void setStreetList(List<MailingStreet> streetList) {
        this.streetList = streetList;
    }

    /**
     * @param editModeCurrentStreet the editModeCurrentStreet to set
     */
    public void setEditModeCurrentStreet(boolean editModeCurrentStreet) {
        this.editModeCurrentStreet = editModeCurrentStreet;
    }

    /**
     * @param editModeCurrentAddress the editModeCurrentAddress to set
     */
    public void setEditModeCurrentAddress(boolean editModeCurrentAddress) {
        this.editModeCurrentAddress = editModeCurrentAddress;
    }

    /**
     * @return the currentStreet
     */
    public MailingStreet getCurrentStreet() {
        return currentStreet;
    }

    /**
     * @param currentStreet the currentStreet to set
     */
    public void setCurrentStreet(MailingStreet currentStreet) {
        this.currentStreet = currentStreet;
    }

    /**
     * @return the formPOBox
     */
    public boolean isFormPOBox() {
        return formPOBox;
    }

    /**
     * @param formPOBox the formPOBox to set
     */
    public void setFormPOBox(boolean formPOBox) {
        this.formPOBox = formPOBox;
    }

    /**
     * @return the mailingAddressList
     */
    public List<MailingAddress> getMailingAddressList() {
        return mailingAddressList;
    }

    /**
     * @param mailingAddressList the mailingAddressList to set
     */
    public void setMailingAddressList(List<MailingAddress> mailingAddressList) {
        this.mailingAddressList = mailingAddressList;
    }

    /**
     * @return the streetListFiltered
     */
    public List<MailingStreet> getStreetListFiltered() {
        return streetListFiltered;
    }

    /**
     * @return the mailingAddressListFiltered
     */
    public List<MailingAddress> getMailingAddressListFiltered() {
        return mailingAddressListFiltered;
    }

    /**
     * @return the cityStateZipListFiltered
     */
    public List<MailingCityStateZip> getCityStateZipListFiltered() {
        return cityStateZipListFiltered;
    }

    /**
     * @param streetListFiltered the streetListFiltered to set
     */
    public void setStreetListFiltered(List<MailingStreet> streetListFiltered) {
        this.streetListFiltered = streetListFiltered;
    }

    /**
     * @param mailingAddressListFiltered the mailingAddressListFiltered to set
     */
    public void setMailingAddressListFiltered(List<MailingAddress> mailingAddressListFiltered) {
        this.mailingAddressListFiltered = mailingAddressListFiltered;
    }

    /**
     * @param cityStateZipListFiltered the cityStateZipListFiltered to set
     */
    public void setCityStateZipListFiltered(List<MailingCityStateZip> cityStateZipListFiltered) {
        this.cityStateZipListFiltered = cityStateZipListFiltered;
    }

    /**
     * @return the formAddressVerified
     */
    public boolean isFormAddressVerified() {
        return formAddressVerified;
    }

    /**
     * @param formAddressVerified the formAddressVerified to set
     */
    public void setFormAddressVerified(boolean formAddressVerified) {
        this.formAddressVerified = formAddressVerified;
    }
    
    
    
}
