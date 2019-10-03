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
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CourtEntity;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.integration.CourtEntityIntegrator;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Adam Gutonski
 */

@ViewScoped
public class CourtEntityBB extends BackingBeanUtils implements Serializable {
    
    private List<CourtEntity> courtEntityList;
    private CourtEntity selectedCourtEntity;
    private List<Municipality> muniList;
    
    
    private int formCourtEntityID;
    private String formCourtEntityOfficialNum;
    private String formJurisdictionLevel;
    private Municipality formMunicipality;
    private String formCourtEntityName;
    private String formAddressStreet;
    private String formAddressCity;
    private String formAddressZip;
    private String formAddressState;
    private String formAddressCounty;
    private String formPhone;
    private String formUrl;
    private String formNotes;
    
    
    public CourtEntityBB() {
        
    }
    
    public void editCourtEntity(ActionEvent e){
        if(getSelectedCourtEntity() != null){
            setFormCourtEntityID(selectedCourtEntity.getCourtEntityID());
            setFormCourtEntityOfficialNum(selectedCourtEntity.getCourtEntityOfficialNum());
            setFormJurisdictionLevel(selectedCourtEntity.getJurisdictionLevel());
            setFormCourtEntityName(selectedCourtEntity.getCourtEntityName());
            setFormAddressStreet(selectedCourtEntity.getAddressStreet());
            setFormAddressCity(selectedCourtEntity.getAddressCity());
            setFormAddressZip(selectedCourtEntity.getAddressZip());
            setFormAddressState(selectedCourtEntity.getAddressState());
            setFormAddressCounty(selectedCourtEntity.getAddressCounty());
            setFormPhone(selectedCourtEntity.getPhone());
            setFormUrl(selectedCourtEntity.getUrl());
            setFormNotes(selectedCourtEntity.getNotes());
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Please select a court entity to update", ""));
        }
    }
    
    public String commitCourtEntityUpdates(){
        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        
        CourtEntity ce = selectedCourtEntity;
        System.out.println("CourtEntityBB.commitCourtEntityUpdates | ceNamebefore = " + ce.getCourtEntityName());
        
        ce.setCourtEntityOfficialNum(formCourtEntityOfficialNum);
        ce.setJurisdictionLevel(formJurisdictionLevel);
        ce.setCourtEntityName(formCourtEntityName);
        ce.setAddressStreet(formAddressStreet);
        ce.setAddressCity(formAddressCity);
        ce.setAddressZip(formAddressZip);
        ce.setAddressState(formAddressState);
        ce.setAddressCounty(formAddressCounty);
        ce.setPhone(formPhone);
        ce.setUrl(formUrl);
        ce.setNotes(formNotes);
        System.out.println("CourtEntityBB.commitCourtEntityUpdates | ceNameafter = " + ce.getCourtEntityName());
        //oif.setOccupancyInspectionFeeNotes(formOccupancyInspectionFeeNotes);
        try{
            System.out.println("ATTEMPTING TO UPDATE COURT ENTITY");
            cei.updateCourtEntity(ce);
            System.out.println("CourtEntityBB.commitCourtEntityUpdates | after integrator");
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Court Entity updated!", ""));
        } catch (IntegrationException ex){
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to update Court Entity in database.",
                    "This must be corrected by the System Administrator"));
        }
        return "";
    }
    
    public String addCourtEntity(){
        CourtEntity courtEntity = new CourtEntity();
        CourtEntityIntegrator cei = new CourtEntityIntegrator();
        courtEntity.setCourtEntityID(formCourtEntityID);
        courtEntity.setCourtEntityOfficialNum(formCourtEntityOfficialNum);
        courtEntity.setJurisdictionLevel(formJurisdictionLevel);
        courtEntity.setCourtEntityName(formCourtEntityName);
        courtEntity.setAddressStreet(formAddressStreet);
        courtEntity.setAddressCity(formAddressCity);
        courtEntity.setAddressZip(formAddressZip);
        courtEntity.setAddressState(formAddressState);
        courtEntity.setAddressCounty(formAddressCounty);
        courtEntity.setPhone(formPhone);
        courtEntity.setUrl(formUrl);
        courtEntity.setNotes(formNotes);
        try {
            cei.insertCourtEntity(courtEntity);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Successfully added court entity to database!", ""));
        } catch(IntegrationException ex) {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to add court entity to database, sorry!", "Check server print out..."));
            return "";
        }
        
        return "courtEntityManage";
        
        
    }
    
    public void deleteSelectedCourtEntity(ActionEvent e){
        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        if(getSelectedCourtEntity() != null){
            try {
                cei.deleteCourtEntity(getSelectedCourtEntity());
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Court entity deleted forever!", ""));
            } catch (IntegrationException ex) {
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Unable to delete court entity--probably because it is used "
                                    + "somewhere in the database. Sorry.", 
                            "This category will always be with us."));
            }
            
        } else {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Please select a court entity from the table to delete", ""));
        }
    }
    

    /**
     * @return the courtEntityList
     */
    public List<CourtEntity> getCourtEntityList() {
        try {
            CourtEntityIntegrator courtEntityIntegrator = getCourtEntityIntegrator();
            courtEntityList = courtEntityIntegrator.getCourtEntityList();
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Unable to load OccupancyInspectionFeeList",
                        "This must be corrected by the system administrator"));
        }
        if(courtEntityList != null){
        return courtEntityList;
        }else{
         courtEntityList = new ArrayList();
         return courtEntityList;
        }
    }

    /**
     * @param courtEntityList the courtEntityList to set
     */
    public void setCourtEntityList(ArrayList<CourtEntity> courtEntityList) {
        this.courtEntityList = courtEntityList;
    }

    /**
     * @return the selectedCourtEntity
     */
    public CourtEntity getSelectedCourtEntity() {
        return selectedCourtEntity;
    }

    /**
     * @param selectedCourtEntity the selectedCourtEntity to set
     */
    public void setSelectedCourtEntity(CourtEntity selectedCourtEntity) {
        this.selectedCourtEntity = selectedCourtEntity;
    }

    /**
     * @return the formCourtEntityID
     */
    public int getFormCourtEntityID() {
        return formCourtEntityID;
    }

    /**
     * @param formCourtEntityID the formCourtEntityID to set
     */
    public void setFormCourtEntityID(int formCourtEntityID) {
        this.formCourtEntityID = formCourtEntityID;
    }

    /**
     * @return the formCourtEntityOfficialNum
     */
    public String getFormCourtEntityOfficialNum() {
        return formCourtEntityOfficialNum;
    }

    /**
     * @param formCourtEntityOfficialNum the formCourtEntityOfficialNum to set
     */
    public void setFormCourtEntityOfficialNum(String formCourtEntityOfficialNum) {
        this.formCourtEntityOfficialNum = formCourtEntityOfficialNum;
    }

    /**
     * @return the formJurisdictionLevel
     */
    public String getFormJurisdictionLevel() {
        return formJurisdictionLevel;
    }

    /**
     * @param formJurisdictionLevel the formJurisdictionLevel to set
     */
    public void setFormJurisdictionLevel(String formJurisdictionLevel) {
        this.formJurisdictionLevel = formJurisdictionLevel;
    }

    /**
     * @return the formMunicipality
     */
    public Municipality getFormMunicipality() {
        return formMunicipality;
    }

    /**
     * @param formMunicipality the formMunicipality to set
     */
    public void setFormMunicipality(Municipality formMunicipality) {
        System.out.println("CourtEntityBB.setFormMunicipality");
        this.formMunicipality = formMunicipality;
    }

    /**
     * @return the formCourtEntityName
     */
    public String getFormCourtEntityName() {
        return formCourtEntityName;
    }

    /**
     * @param formCourtEntityName the formCourtEntityName to set
     */
    public void setFormCourtEntityName(String formCourtEntityName) {
        this.formCourtEntityName = formCourtEntityName;
    }

    /**
     * @return the formAddressStreet
     */
    public String getFormAddressStreet() {
        return formAddressStreet;
    }

    /**
     * @param formAddressStreet the formAddressStreet to set
     */
    public void setFormAddressStreet(String formAddressStreet) {
        this.formAddressStreet = formAddressStreet;
    }

    /**
     * @return the formAddressCity
     */
    public String getFormAddressCity() {
        return formAddressCity;
    }

    /**
     * @param formAddressCity the formAddressCity to set
     */
    public void setFormAddressCity(String formAddressCity) {
        this.formAddressCity = formAddressCity;
    }

    /**
     * @return the formAddressZip
     */
    public String getFormAddressZip() {
        return formAddressZip;
    }

    /**
     * @param formAddressZip the formAddressZip to set
     */
    public void setFormAddressZip(String formAddressZip) {
        this.formAddressZip = formAddressZip;
    }

    /**
     * @return the formAddressState
     */
    public String getFormAddressState() {
        return formAddressState;
    }

    /**
     * @param formAddressState the formAddressState to set
     */
    public void setFormAddressState(String formAddressState) {
        this.formAddressState = formAddressState;
    }

    /**
     * @return the formAddressCounty
     */
    public String getFormAddressCounty() {
        return formAddressCounty;
    }

    /**
     * @param formAddressCounty the formAddressCounty to set
     */
    public void setFormAddressCounty(String formAddressCounty) {
        this.formAddressCounty = formAddressCounty;
    }

    /**
     * @return the formPhone
     */
    public String getFormPhone() {
        return formPhone;
    }

    /**
     * @param formPhone the formPhone to set
     */
    public void setFormPhone(String formPhone) {
        this.formPhone = formPhone;
    }

    /**
     * @return the formUrl
     */
    public String getFormUrl() {
        return formUrl;
    }

    /**
     * @param formUrl the formUrl to set
     */
    public void setFormUrl(String formUrl) {
        this.formUrl = formUrl;
    }

    /**
     * @return the fomrNotes
     */
    public String getFormNotes() {
        return formNotes;
    }

    /**
     * @param fomrNotes the fomrNotes to set
     */
    public void setFormNotes(String fomrNotes) {
        this.formNotes = fomrNotes;
    }

    

    /**
     * @param muniList the muniList to set
     */
    public void setMuniList(ArrayList<Municipality> muniList) {
        this.muniList = muniList;
    }
}
