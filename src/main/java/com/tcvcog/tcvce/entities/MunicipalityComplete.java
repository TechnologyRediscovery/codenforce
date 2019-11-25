/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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
package com.tcvcog.tcvce.entities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author sylvia
 */
public class MunicipalityComplete extends Municipality{
    
    private String address_street;
    private String address_city;
    private String address_state;
    
    private String address_zip;
    private String phone;
    private String fax;
    private String email;
    private int population;
    private boolean activeInProgram;
    
    private CodeSet codeSet;
    private CodeSource issuingCodeSource;
    private int defaultNOVStyleID;

    private MuniProfile profile;
    private boolean enableCodeEnforcement;
    private boolean enableOccupancy;
    private boolean enablePublicCEActionRequestSubmissions;
    
    private boolean enablePublicCEActionRequestInfo;
    private boolean enablePublicOccPermitApp;
    private boolean enablePublicOccInspectionTODOs;
    
    private User muniManager;
    private int muniOfficePropertyId;
    private String notes; 
    private LocalDateTime lastUpdatedTS;
    private User lastUpdaetdBy;
    private User primaryStaffContact;
    
    private List<User> codeOfficers;
    private List<CourtEntity> courtEntities;
    private List<Integer> photoDocList;
    
    public MunicipalityComplete(Municipality m){
        this.muniName = m.getMuniName();
        this.muniCode = m.getMuniCode();
    }

    /**
     * @return the address_street
     */
    public String getAddress_street() {
        return address_street;
    }

    /**
     * @return the address_city
     */
    public String getAddress_city() {
        return address_city;
    }

    /**
     * @return the address_state
     */
    public String getAddress_state() {
        return address_state;
    }

    /**
     * @return the address_zip
     */
    public String getAddress_zip() {
        return address_zip;
    }

    /**
     * @return the phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @return the fax
     */
    public String getFax() {
        return fax;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @return the population
     */
    public int getPopulation() {
        return population;
    }

    /**
     * @return the activeInProgram
     */
    public boolean isActiveInProgram() {
        return activeInProgram;
    }

    /**
     * @return the codeSet
     */
    public CodeSet getCodeSet() {
        return codeSet;
    }

    /**
     * @return the issuingCodeSource
     */
    public CodeSource getIssuingCodeSource() {
        return issuingCodeSource;
    }

    /**
     * @return the defaultNOVStyleID
     */
    public int getDefaultNOVStyleID() {
        return defaultNOVStyleID;
    }

    /**
     * @return the profile
     */
    public MuniProfile getProfile() {
        return profile;
    }

    /**
     * @return the enableCodeEnforcement
     */
    public boolean isEnableCodeEnforcement() {
        return enableCodeEnforcement;
    }

    /**
     * @return the enableOccupancy
     */
    public boolean isEnableOccupancy() {
        return enableOccupancy;
    }

    /**
     * @return the enablePublicCEActionRequestSubmissions
     */
    public boolean isEnablePublicCEActionRequestSubmissions() {
        return enablePublicCEActionRequestSubmissions;
    }

    /**
     * @return the enablePublicCEActionRequestInfo
     */
    public boolean isEnablePublicCEActionRequestInfo() {
        return enablePublicCEActionRequestInfo;
    }

    /**
     * @return the enablePublicOccPermitApp
     */
    public boolean isEnablePublicOccPermitApp() {
        return enablePublicOccPermitApp;
    }

    /**
     * @return the enablePublicOccInspectionTODOs
     */
    public boolean isEnablePublicOccInspectionTODOs() {
        return enablePublicOccInspectionTODOs;
    }

    /**
     * @return the muniManager
     */
    public User getMuniManager() {
        return muniManager;
    }

    /**
     * @return the muniOfficePropertyId
     */
    public int getMuniOfficePropertyId() {
        return muniOfficePropertyId;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @return the lastUpdatedTS
     */
    public LocalDateTime getLastUpdatedTS() {
        return lastUpdatedTS;
    }

    /**
     * @return the lastUpdaetdBy
     */
    public User getLastUpdaetdBy() {
        return lastUpdaetdBy;
    }

    /**
     * @return the primaryStaffContact
     */
    public User getPrimaryStaffContact() {
        return primaryStaffContact;
    }

    /**
     * @return the codeOfficers
     */
    public List<User> getCodeOfficers() {
        return codeOfficers;
    }

    /**
     * @return the courtEntities
     */
    public List<CourtEntity> getCourtEntities() {
        return courtEntities;
    }

    /**
     * @return the photoDocList
     */
    public List<Integer> getPhotoDocList() {
        return photoDocList;
    }

    /**
     * @param address_street the address_street to set
     */
    public void setAddress_street(String address_street) {
        this.address_street = address_street;
    }

    /**
     * @param address_city the address_city to set
     */
    public void setAddress_city(String address_city) {
        this.address_city = address_city;
    }

    /**
     * @param address_state the address_state to set
     */
    public void setAddress_state(String address_state) {
        this.address_state = address_state;
    }

    /**
     * @param address_zip the address_zip to set
     */
    public void setAddress_zip(String address_zip) {
        this.address_zip = address_zip;
    }

    /**
     * @param phone the phone to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * @param fax the fax to set
     */
    public void setFax(String fax) {
        this.fax = fax;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @param population the population to set
     */
    public void setPopulation(int population) {
        this.population = population;
    }

    /**
     * @param activeInProgram the activeInProgram to set
     */
    public void setActiveInProgram(boolean activeInProgram) {
        this.activeInProgram = activeInProgram;
    }

    /**
     * @param codeSet the codeSet to set
     */
    public void setCodeSet(CodeSet codeSet) {
        this.codeSet = codeSet;
    }

    /**
     * @param issuingCodeSource the issuingCodeSource to set
     */
    public void setIssuingCodeSource(CodeSource issuingCodeSource) {
        this.issuingCodeSource = issuingCodeSource;
    }

    /**
     * @param defaultNOVStyleID the defaultNOVStyleID to set
     */
    public void setDefaultNOVStyleID(int defaultNOVStyleID) {
        this.defaultNOVStyleID = defaultNOVStyleID;
    }

    /**
     * @param profile the profile to set
     */
    public void setProfile(MuniProfile profile) {
        this.profile = profile;
    }

    /**
     * @param enableCodeEnforcement the enableCodeEnforcement to set
     */
    public void setEnableCodeEnforcement(boolean enableCodeEnforcement) {
        this.enableCodeEnforcement = enableCodeEnforcement;
    }

    /**
     * @param enableOccupancy the enableOccupancy to set
     */
    public void setEnableOccupancy(boolean enableOccupancy) {
        this.enableOccupancy = enableOccupancy;
    }

    /**
     * @param enablePublicCEActionRequestSubmissions the enablePublicCEActionRequestSubmissions to set
     */
    public void setEnablePublicCEActionRequestSubmissions(boolean enablePublicCEActionRequestSubmissions) {
        this.enablePublicCEActionRequestSubmissions = enablePublicCEActionRequestSubmissions;
    }

    /**
     * @param enablePublicCEActionRequestInfo the enablePublicCEActionRequestInfo to set
     */
    public void setEnablePublicCEActionRequestInfo(boolean enablePublicCEActionRequestInfo) {
        this.enablePublicCEActionRequestInfo = enablePublicCEActionRequestInfo;
    }

    /**
     * @param enablePublicOccPermitApp the enablePublicOccPermitApp to set
     */
    public void setEnablePublicOccPermitApp(boolean enablePublicOccPermitApp) {
        this.enablePublicOccPermitApp = enablePublicOccPermitApp;
    }

    /**
     * @param enablePublicOccInspectionTODOs the enablePublicOccInspectionTODOs to set
     */
    public void setEnablePublicOccInspectionTODOs(boolean enablePublicOccInspectionTODOs) {
        this.enablePublicOccInspectionTODOs = enablePublicOccInspectionTODOs;
    }

    /**
     * @param muniManager the muniManager to set
     */
    public void setMuniManager(User muniManager) {
        this.muniManager = muniManager;
    }

    /**
     * @param muniOfficePropertyId the muniOfficePropertyId to set
     */
    public void setMuniOfficePropertyId(int muniOfficePropertyId) {
        this.muniOfficePropertyId = muniOfficePropertyId;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @param lastUpdatedTS the lastUpdatedTS to set
     */
    public void setLastUpdatedTS(LocalDateTime lastUpdatedTS) {
        this.lastUpdatedTS = lastUpdatedTS;
    }

    /**
     * @param lastUpdaetdBy the lastUpdaetdBy to set
     */
    public void setLastUpdaetdBy(User lastUpdaetdBy) {
        this.lastUpdaetdBy = lastUpdaetdBy;
    }

    /**
     * @param primaryStaffContact the primaryStaffContact to set
     */
    public void setPrimaryStaffContact(User primaryStaffContact) {
        this.primaryStaffContact = primaryStaffContact;
    }

    /**
     * @param codeOfficers the codeOfficers to set
     */
    public void setCodeOfficers(List<User> codeOfficers) {
        this.codeOfficers = codeOfficers;
    }

    /**
     * @param courtEntities the courtEntities to set
     */
    public void setCourtEntities(List<CourtEntity> courtEntities) {
        this.courtEntities = courtEntities;
    }

    /**
     * @param photoDocList the photoDocList to set
     */
    public void setPhotoDocList(List<Integer> photoDocList) {
        this.photoDocList = photoDocList;
    }
    
      @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.address_street);
        hash = 97 * hash + Objects.hashCode(this.address_city);
        hash = 97 * hash + Objects.hashCode(this.address_state);
        hash = 97 * hash + Objects.hashCode(this.address_zip);
        hash = 97 * hash + Objects.hashCode(this.phone);
        hash = 97 * hash + Objects.hashCode(this.fax);
        hash = 97 * hash + Objects.hashCode(this.email);
        hash = 97 * hash + this.population;
        hash = 97 * hash + (this.activeInProgram ? 1 : 0);
        hash = 97 * hash + Objects.hashCode(this.codeSet);
        hash = 97 * hash + Objects.hashCode(this.issuingCodeSource);
        hash = 97 * hash + this.defaultNOVStyleID;
        hash = 97 * hash + Objects.hashCode(this.profile);
        hash = 97 * hash + (this.enableCodeEnforcement ? 1 : 0);
        hash = 97 * hash + (this.enableOccupancy ? 1 : 0);
        hash = 97 * hash + (this.enablePublicCEActionRequestSubmissions ? 1 : 0);
        hash = 97 * hash + (this.enablePublicCEActionRequestInfo ? 1 : 0);
        hash = 97 * hash + (this.enablePublicOccPermitApp ? 1 : 0);
        hash = 97 * hash + (this.enablePublicOccInspectionTODOs ? 1 : 0);
        hash = 97 * hash + Objects.hashCode(this.muniManager);
        hash = 97 * hash + this.muniOfficePropertyId;
        hash = 97 * hash + Objects.hashCode(this.notes);
        hash = 97 * hash + Objects.hashCode(this.lastUpdatedTS);
        hash = 97 * hash + Objects.hashCode(this.lastUpdaetdBy);
        hash = 97 * hash + Objects.hashCode(this.primaryStaffContact);
        hash = 97 * hash + Objects.hashCode(this.codeOfficers);
        hash = 97 * hash + Objects.hashCode(this.courtEntities);
        hash = 97 * hash + Objects.hashCode(this.photoDocList);
        return hash;
    }
    
}
