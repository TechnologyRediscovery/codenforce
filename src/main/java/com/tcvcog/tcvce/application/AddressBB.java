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
import com.tcvcog.tcvce.entities.Human;
import com.tcvcog.tcvce.entities.IFace_addressListHolder;
import com.tcvcog.tcvce.entities.LinkedObjectRole;
import com.tcvcog.tcvce.entities.LinkedObjectSchemaEnum;
import com.tcvcog.tcvce.entities.MailingAddress;
import com.tcvcog.tcvce.entities.MailingAddressLink;
import com.tcvcog.tcvce.entities.MailingCityStateZip;
import com.tcvcog.tcvce.entities.MailingStreet;
import com.tcvcog.tcvce.entities.Parcel;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.search.QueryMailingCityStateZip;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
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
    
    private MailingAddress currentMailingAddress;
    private List<MailingAddress> mailingAddressList;
    private List<MailingAddress> mailingAddressListFiltered;
    
    private boolean editModeCurrentAddress;
    private MailingCityStateZip currentCityStateZip;
    private List<MailingCityStateZip> cityStateZipListFiltered;
    
    private boolean editModeCurrentAddressLink;
    private MailingAddressLink currentMailingAddressLink;
    private IFace_addressListHolder currentAddressListHolder;
    private List<LinkedObjectRole> mailingAddressLinkRoleCandidateList;
    private String addressListHolderComponentForUpdatePostMADLinkOperation;
    
    private String formBuildingNo;
    private String formStreet;
    private boolean formPOBox;
    private boolean formAddressVerified;
    private String formCity;
    private String formZip;
    private String formNotesAddress;
    private String formNotesStreet;
    
    
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
        if(currentMailingAddress != null && currentMailingAddress.getStreet() != null){
            currentMailingAddress.getStreet().setCityStateZip(currentCityStateZip);
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
        editModeCurrentStreet = false;
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
        getSessionBean().setSessMailingAddress(currentMailingAddress);
        
    }
    
    /**
     * Listener to view current Maling addresss
     * @param ma 
     */
    public void onMailingAddressViewEditLinkClick(MailingAddress ma){
        currentMailingAddress = ma;
        formAddressVerified = currentMailingAddress.getVerifiedTS() != null;
        getSessionBean().setSessMailingAddress(currentMailingAddress);
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
            int freshID;
            try {
                if(formAddressVerified && currentMailingAddress != null){
                    currentMailingAddress.setVerifiedBy(getSessionBean().getSessUser());
                    currentMailingAddress.setVerifiedTS(LocalDateTime.now());
                }
                if(currentMailingAddress.getAddressID() == 0){
                    freshID = pc.insertMailingAddress(currentMailingAddress, getSessionBean().getSessUser());
                    getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_INFO,
                               "Address Insert successful",""));
                    currentMailingAddress = pc.getMailingAddress(freshID);
                } else {
                    pc.updateMailingAddress(currentMailingAddress, getSessionBean().getSessUser());
                    getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_INFO,
                               "Address update successful",""));
                    currentMailingAddress = pc.getMailingAddress(currentMailingAddress.getAddressID());
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
        currentMailingAddress = pc.getMailingAddressSkeleton();
        if(currentStreet != null){
            currentStreet.setCityStateZip(currentCityStateZip);
            currentMailingAddress.setStreet(currentStreet);
        }
        editModeCurrentAddress = true;
        
    }
    
    /**
     * Listener for user requests to abort any mailing address operation
     * @param ev 
     */
    public void onMailingAddressAbortOperationButtonChange(ActionEvent ev){
        editModeCurrentAddress = false;
    }
    
    /**
     * Listener for user requests to start the address deactivation process
     * @param ev 
     */
    public void onMailingAddressDeactivateInitLinkClick(ActionEvent ev){
        
    }
    
    /**
     * 
     * Listener for user requests to commit the deactivation operation
     * @param ev 
     */
    public void onMailingAddressDeactivateConfirmButtonChange(ActionEvent ev){
        PropertyCoordinator pc = getPropertyCoordinator();
        try {
            pc.deactivateMailingAddress(currentMailingAddress, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Address deactivation successful! ",""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println("ex");
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could deactivate address: " + ex.getMessage(),""));
        } 
    }
    
    /**
     * Listener for user requests to start the note process on a 
     * mailing address
     * @param ev
     */
    public void onMailingAddressNoteInitButtonChange(ActionEvent ev){
        formNotesAddress = "";
    }
    
    /**
     * user requests to commit a note to a mailin address
     * @param ev
     */
    public void onMailingAddressNoteCommitButtonChage(ActionEvent ev){
        SystemCoordinator sc = getSystemCoordinator();
        PropertyCoordinator pc = getPropertyCoordinator();
        if(currentMailingAddress != null){
            MessageBuilderParams mbp = new MessageBuilderParams(currentMailingAddress.getNotes(), null, null, formNotesAddress, getSessionBean().getSessUser(), null);
            currentMailingAddress.setNotes(sc.appendNoteBlock(mbp));
            try {
                sc.writeNotes(currentMailingAddress, getSessionBean().getSessUser());
                currentMailingAddress = pc.getMailingAddress(currentMailingAddress.getAddressID());
                getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_INFO,
                               "Note write successful! Woot woot!",""));
                
            } catch (IntegrationException | BObStatusException ex) {
                System.out.println(ex);
                 getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                               "Fatal note error: " + ex.getMessage(),""));
            } 
            formNotesAddress = "";
        }
    }
    
    /**
     * Listener for user requests to start the note 
     * process on a street
     * @param ev
     */
    public void onMailingStreetNoteInitButtonChange(ActionEvent ev){
        formNotesStreet = "";
    }
    
    /**
     * Listener for user Requests to commit notes to the 
     * currentStreet
     * @param ev
     */
    public void onMailingStreetNoteCommitButtonChage(ActionEvent ev){
        SystemCoordinator sc = getSystemCoordinator();
        PropertyCoordinator pc = getPropertyCoordinator();
        if(currentStreet != null){
            MessageBuilderParams mbp = new MessageBuilderParams(currentStreet.getNotes(), null, null, formNotesStreet, getSessionBean().getSessUser(), null);
            currentStreet.setNotes(sc.appendNoteBlock(mbp));
            try {
                sc.writeNotes(currentStreet, getSessionBean().getSessUser());
                currentStreet = pc.getMailingStreet(currentStreet.getStreetID());
                getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_INFO,
                               "Note write successful! Woot woot!",""));
            } catch (IntegrationException | BObStatusException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                               "Fatal note error: " + ex.getMessage(),""));
            } 
            formNotesAddress = "";
        }
    }
    
    
    
    
    /************************************************************/
    /************** LINK MANAGEMENT TOOLS  **********************/
    /************************************************************/
    
    /**
     * Listener for user requests to start the address linking process
     * @param alholder
     */
    public void onLinkToSessionMailingAddressInitButtonChange(IFace_addressListHolder alholder){
        PropertyCoordinator pc = getPropertyCoordinator();
        currentAddressListHolder = alholder;
        
        
        configureAddressListHolderUpdateComponent();
      
        if(currentAddressListHolder != null && getSessionBean().getSessMailingAddress() != null){
            getSessionBean().setSessAddressListHolder(currentAddressListHolder);
            currentMailingAddress = getSessionBean().getSessMailingAddress();
            currentMailingAddressLink = pc.getMailingAddressLinkSkeleton(getSessionBean().getSessMailingAddress());
            if(currentAddressListHolder.getMailingAddressLinkList() != null){
                currentMailingAddressLink.setPriority(currentAddressListHolder.getMailingAddressLinkList().size() + 1);
            }
            configureLinkedObjectRoleList(null);
            editModeCurrentAddressLink = true;
        } else {
               getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                               "Fatal error! Could not initiate the address linking process",""));
        }
    }
    
    private void configureAddressListHolderUpdateComponent(){
          addressListHolderComponentForUpdatePostMADLinkOperation = 
                FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getRequestParameterMap()
                        .get("initiating-address-list-component-id");
         getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Viewing address link", ""));
        System.out.println("AddressBB.configureAddressListHolderUpdateComponent : " + addressListHolderComponentForUpdatePostMADLinkOperation);
        
    }
    
    /**
     * Asks this bean's currentAddressList holder what type it is, and 
     * sets the association candidate list appropriately
     * for the dialog that's about to appear
     * @param madLink if null, I'll make the list reflect the currentAddressListHolder
     * otherwise, I'll make the list reflect our current link
     */
    public void configureLinkedObjectRoleList(MailingAddressLink madLink){
        SystemCoordinator sc = getSystemCoordinator();
        try {
            if(madLink != null){
                mailingAddressLinkRoleCandidateList = sc.assembleLinkedObjectRolesBySchema(madLink.getLinkedObjectRoleSchemaEnum());
                return;
            }
            if(currentAddressListHolder != null){
                if(currentAddressListHolder instanceof Human){
                    mailingAddressLinkRoleCandidateList = sc.assembleLinkedObjectRolesBySchema(LinkedObjectSchemaEnum.MailingaddressHuman);

                } else if (currentAddressListHolder instanceof Parcel){
                    mailingAddressLinkRoleCandidateList = sc.assembleLinkedObjectRolesBySchema(LinkedObjectSchemaEnum.ParcelMailingaddress);
                }
            }
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
              getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_ERROR,
                           "Fatak error! Could not setup linked object role list" + ex.getMessage(),""));
        } 
    }
    
    /**
     * Finalizes the MailingAddres linking process
     * @param ev 
     */
    public void onLinkToSessionMailingAddressCommit(ActionEvent ev){
        PropertyCoordinator pc = getPropertyCoordinator();
        try {
            pc.linkToMailingAddress(   currentAddressListHolder, currentMailingAddressLink, getSessionBean().getSessUser());
            
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                               "Fatal Address linking error: " + ex.getMessage(),""));
        } 
    }
    
    
    /**
     * Entry point for viewing/editing this mailing address link stuff
     * @param madLink 
     */
    public void onMalingAddressLinkViewEditInit(MailingAddressLink madLink){
        
        currentMailingAddressLink = madLink;
        configureAddressListHolderUpdateComponent();
        configureLinkedObjectRoleList(madLink);
        System.out.println("AddressBB.onMalingAddressLinkViewEditInit | madLink: linkid " + madLink.getLinkID());
    }
    
    
    /**
     * Listener for user toggling of the edit/save mailing address record
     * @param ev 
     */
    public void onMailingAddressLinkEditToggleButtonChange(ActionEvent ev){
        if(editModeCurrentAddressLink){
            if(currentMailingAddressLink != null){
                if(currentMailingAddressLink.getLinkID() == 0){
                    onLinkToSessionMailingAddressCommit(ev);
                } else {
                    updateCurrentMailingAddressLink();
                }
                refreshCurrentAddressListHolderLinks();
            }
        }
        editModeCurrentAddressLink = !editModeCurrentAddressLink;
    }
    
    
    public void onMailingAddressLinkEditAbort(ActionEvent ev){
        System.out.println("AddressBB.madLinkAbort");
        editModeCurrentAddressLink = !editModeCurrentAddressLink;
    }
    
    /**
     * Internal caller to update a mad link
     */
    private void updateCurrentMailingAddressLink(){
        PropertyCoordinator pc = getPropertyCoordinator();
        try {
            pc.updateMailingAddressLink(currentMailingAddressLink, getSessionBean().getSessUser());
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
              getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                               "Fatal mailing address link update error: " + ex.getMessage(),""));
        } 
    }
    
    public void onMailingAddressLinkDeactivateInitLinkClick(ActionEvent ev){
        System.out.println("AddressBB.onMailingAddressLinkDeactivateInitLinkClick | linkid "+ currentMailingAddressLink.getLinkID());
    }
    
    /**
     * listener for user finalization of the address link removal process
     * @param ev 
     */
    public void onMailingAddressLinkDeactivateCommitButtonChange(ActionEvent ev){
        PropertyCoordinator pc = getPropertyCoordinator();
        try {
            pc.deactivateLinkToMailingAddress(currentMailingAddressLink, getSessionBean().getSessUser());
            refreshCurrentAddressListHolderLinks();
            getFacesContext().addMessage(null,
                     new FacesMessage(FacesMessage.SEVERITY_INFO,
                             "Tada! And it's gone! ",""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
              getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR,
                               "Fatal mailing address link deactivation error: " + ex.getMessage(),""));
        } 
    }
    
    
    /**
     * Internal method for getting a new copy of our AddressListHolder after updates
     */
    private void refreshCurrentAddressListHolderLinks() {
        PropertyCoordinator pc = getPropertyCoordinator();
        if(currentAddressListHolder != null){
            try {
                List<MailingAddressLink> madLinkList = pc.getMailingAddressLinkList(currentAddressListHolder);
                currentAddressListHolder.setMailingAddressLinkList(madLinkList);
                getSessionBean().setSessMailingAddressLinkRefreshedList(madLinkList);
            } catch (BObStatusException | IntegrationException ex) {
                getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_ERROR,
                           "Fatal object reload error: " + ex.getMessage(),""));
            } 
        }
    }
    
    
    
    
    
    /************************************************************/
    /************** GETTERS AND SETTERS    **********************/
    /************************************************************/
    
    
    
    /**
     * @return the currentMailingAddress
     */
    public MailingAddress getCurrentMailingAddress() {
        return currentMailingAddress;
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
     * @param currentMailingAddress the currentMailingAddress to set
     */
    public void setCurrentMailingAddress(MailingAddress currentMailingAddress) {
        this.currentMailingAddress = currentMailingAddress;
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

    /**
     * @return the formNotesAddress
     */
    public String getFormNotesAddress() {
        return formNotesAddress;
    }

    /**
     * @return the formNotesStreet
     */
    public String getFormNotesStreet() {
        return formNotesStreet;
    }

    /**
     * @param formNotesAddress the formNotesAddress to set
     */
    public void setFormNotesAddress(String formNotesAddress) {
        this.formNotesAddress = formNotesAddress;
    }

    /**
     * @param formNotesStreet the formNotesStreet to set
     */
    public void setFormNotesStreet(String formNotesStreet) {
        this.formNotesStreet = formNotesStreet;
    }

    /**
     * @return the mailingAddressLinkRoleCandidateList
     */
    public List<LinkedObjectRole> getMailingAddressLinkRoleCandidateList() {
        return mailingAddressLinkRoleCandidateList;
    }

    /**
     * @param mailingAddressLinkRoleCandidateList the mailingAddressLinkRoleCandidateList to set
     */
    public void setMailingAddressLinkRoleCandidateList(List<LinkedObjectRole> mailingAddressLinkRoleCandidateList) {
        this.mailingAddressLinkRoleCandidateList = mailingAddressLinkRoleCandidateList;
    }

    /**
     * @return the currentMailingAddressLink
     */
    public MailingAddressLink getCurrentMailingAddressLink() {
        return currentMailingAddressLink;
    }

    /**
     * @return the currentAddressListHolder
     */
    public IFace_addressListHolder getCurrentAddressListHolder() {
        return currentAddressListHolder;
    }

    /**
     * @param currentMailingAddressLink the currentMailingAddressLink to set
     */
    public void setCurrentMailingAddressLink(MailingAddressLink currentMailingAddressLink) {
        this.currentMailingAddressLink = currentMailingAddressLink;
    }

    /**
     * @param currentAddressListHolder the currentAddressListHolder to set
     */
    public void setCurrentAddressListHolder(IFace_addressListHolder currentAddressListHolder) {
        this.currentAddressListHolder = currentAddressListHolder;
    }

    /**
     * @return the editModeCurrentAddressLink
     */
    public boolean isEditModeCurrentAddressLink() {
        return editModeCurrentAddressLink;
    }

    /**
     * @param editModeCurrentAddressLink the editModeCurrentAddressLink to set
     */
    public void setEditModeCurrentAddressLink(boolean editModeCurrentAddressLink) {
        this.editModeCurrentAddressLink = editModeCurrentAddressLink;
    }

    /**
     * @return the addressListHolderComponentForUpdatePostMADLinkOperation
     */
    public String getAddressListHolderComponentForUpdatePostMADLinkOperation() {
        return addressListHolderComponentForUpdatePostMADLinkOperation;
    }

    /**
     * @param addressListHolderComponentForUpdatePostMADLinkOperation the addressListHolderComponentForUpdatePostMADLinkOperation to set
     */
    public void setAddressListHolderComponentForUpdatePostMADLinkOperation(String addressListHolderComponentForUpdatePostMADLinkOperation) {
        this.addressListHolderComponentForUpdatePostMADLinkOperation = addressListHolderComponentForUpdatePostMADLinkOperation;
    }
    
    
    
}
