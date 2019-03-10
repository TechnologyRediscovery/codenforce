/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.CasePhase;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.User;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * @author Sylvia Garland
 */
public class SearchParamsCECases extends SearchParams implements Serializable{
    
    enum DateSearchType{
        Opening,
        TimeStamp,
        Closing;
    }
    
    private boolean useIsOpen;
    private boolean isOpen;
   
    private String dateToSearchCECases;

    private boolean useCasePhase;
    private CasePhase casePhase;
    
    private boolean useCaseStage;
    private String caseStage;
    
    private boolean useProperty;
    private Property property;
    
    private boolean usePropertyInfoCase;
    private boolean propertyInfoCase;
    
    private boolean useCaseManager;
    private User caseManagerUser;
    
    private boolean useLegacy;
    private boolean legacyCase;
    
    
   public SearchParamsCECases(){
       
   }

    /**
     * @return the useIsOpen
     */
    public boolean isUseIsOpen() {
        return useIsOpen;
    }

    /**
     * @return the isOpen
     */
    public boolean isIsOpen() {
        return isOpen;
    }

   

   

    /**
     * @return the useCaseManager
     */
    public boolean isUseCaseManager() {
        return useCaseManager;
    }

    /**
     * @return the caseManagerID
     */
    public User getCaseManagerID() {
        return getCaseManagerUser();
    }

    /**
     * @param useIsOpen the useIsOpen to set
     */
    public void setUseIsOpen(boolean useIsOpen) {
        this.useIsOpen = useIsOpen;
    }

    /**
     * @param isOpen the isOpen to set
     */
    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

   
   
    /**
     * @param useCaseManager the useCaseManager to set
     */
    public void setUseCaseManager(boolean useCaseManager) {
        this.useCaseManager = useCaseManager;
    }

    
    /**
     * @return the useLegacy
     */
    public boolean isUseLegacy() {
        return useLegacy;
    }

    /**
     * @return the legacyCase
     */
    public boolean isLegacyCase() {
        return legacyCase;
    }

    /**
     * @param useLegacy the useLegacy to set
     */
    public void setUseLegacy(boolean useLegacy) {
        this.useLegacy = useLegacy;
    }

    /**
     * @param legacyCase the legacyCase to set
     */
    public void setLegacyCase(boolean legacyCase) {
        this.legacyCase = legacyCase;
    }

    /**
     * @return the useCasePhase
     */
    public boolean isUseCasePhase() {
        return useCasePhase;
    }

    /**
     * @return the casePhase
     */
    public CasePhase getCasePhase() {
        return casePhase;
    }

    /**
     * @return the useCaseStage
     */
    public boolean isUseCaseStage() {
        return useCaseStage;
    }

    /**
     * @return the caseStage
     */
    public String getCaseStage() {
        return caseStage;
    }

    /**
     * @return the useProperty
     */
    public boolean isUseProperty() {
        return useProperty;
    }

    /**
     * @return the property
     */
    public Property getProperty() {
        return property;
    }

    /**
     * @return the usePropertyInfoCase
     */
    public boolean isUsePropertyInfoCase() {
        return usePropertyInfoCase;
    }

    /**
     * @return the propertyInfoCase
     */
    public boolean isPropertyInfoCase() {
        return propertyInfoCase;
    }

    /**
     * @return the caseManagerUser
     */
    public User getCaseManagerUser() {
        return caseManagerUser;
    }

    /**
     * @param useCasePhase the useCasePhase to set
     */
    public void setUseCasePhase(boolean useCasePhase) {
        this.useCasePhase = useCasePhase;
    }

    /**
     * @param casePhase the casePhase to set
     */
    public void setCasePhase(CasePhase casePhase) {
        this.casePhase = casePhase;
    }

    /**
     * @param useCaseStage the useCaseStage to set
     */
    public void setUseCaseStage(boolean useCaseStage) {
        this.useCaseStage = useCaseStage;
    }

    /**
     * @param caseStage the caseStage to set
     */
    public void setCaseStage(String caseStage) {
        this.caseStage = caseStage;
    }

    /**
     * @param useProperty the useProperty to set
     */
    public void setUseProperty(boolean useProperty) {
        this.useProperty = useProperty;
    }

    /**
     * @param property the property to set
     */
    public void setProperty(Property property) {
        this.property = property;
    }

    /**
     * @param usePropertyInfoCase the usePropertyInfoCase to set
     */
    public void setUsePropertyInfoCase(boolean usePropertyInfoCase) {
        this.usePropertyInfoCase = usePropertyInfoCase;
    }

    /**
     * @param propertyInfoCase the propertyInfoCase to set
     */
    public void setPropertyInfoCase(boolean propertyInfoCase) {
        this.propertyInfoCase = propertyInfoCase;
    }

    /**
     * @param caseManagerUser the caseManagerUser to set
     */
    public void setCaseManagerUser(User caseManagerUser) {
        this.caseManagerUser = caseManagerUser;
    }

    /**
     * @return the dateToSearchCECases
     */
    public String getDateToSearchCECases() {
        return dateToSearchCECases;
    }

    /**
     * @param dateToSearchCECases the dateToSearchCECases to set
     */
    public void setDateToSearchCECases(String dateToSearchCECases) {
        this.dateToSearchCECases = dateToSearchCECases;
    }
   
   
   

   
    
}
