/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.BOBSource;
import com.tcvcog.tcvce.entities.MailingAddress;
import com.tcvcog.tcvce.entities.MailingCityStateZip;
import com.tcvcog.tcvce.entities.search.QueryMailingCityStateZip;
import com.tcvcog.tcvce.entities.search.QueryMailingCityStateZipEnum;
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

    private MailingAddress currentAddress;
    private boolean addressEditMode;
    private MailingCityStateZip currentCityStateZip;
    private String formBuildingNo;
    private String formStreet;
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
    public void onQueryChange(ActionEvent ev){
        
        
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
        
        
    }
    
     /**********************************************************/
    /************** Mailing address INFRASTRUCTURE *********************/
    /**********************************************************/
    
    /**
     * Listener for user clicks of the mailing address edit mode
     */
    public void toggleMailingAddressEditMode(){
        
        
    }
    
    
    /**
     * Listener for user requests to start the process of adding a mailing address
     * @param ev 
     */
    public void onMailingAddressAddInitButtonChange(ActionEvent ev){
        
        
    }
    
    /**
     * Listener for user requests to finalize the process of adding a mailing address
     * @param ev 
     */
    public void onMailingAddressAddCommitButtonChange(ActionEvent ev){
        
    }
    
    /**
     * Listener for user requests to start the process of editing a mailing address
     * @param ma
     */
    public void onMailingAddressEditInitButtonChange(MailingAddress ma){
        
        
    }
    
    /**
     * Listener for user requests to finalize mailing address edits
     * @param ev 
     */
    public void onMailingAddressEditCommitButtonChange(ActionEvent ev){
        
        
    }
    
    /**
     * Listener for user requests to abort any mailing address operation
     * @param ev 
     */
    public void onMailingAddressAbortOperationButtonChange(ActionEvent ev){
        
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
     * @return the addressEditMode
     */
    public boolean isAddressEditMode() {
        return addressEditMode;
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
     * @param addressEditMode the addressEditMode to set
     */
    public void setAddressEditMode(boolean addressEditMode) {
        this.addressEditMode = addressEditMode;
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
    
    
    
}
