/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.CasePhase;
import com.tcvcog.tcvce.entities.CaseStage;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.User;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sylvia Garland
 */
public class SearchParamsCECases 
        extends SearchParams 
        implements Serializable{
    
    
    
    private boolean useIsOpen;
    private boolean isOpen;
   
    private List<String> dateSearchOptions;
    private String dateToSearchCECases;

    private boolean useCasePhase;
    private CasePhase casePhase;
    
    private boolean useCaseStage;
    private CaseStage caseStage;
    private List<CasePhase> caseStageAsPhaseList; 
    
    private boolean useProperty;
    private Property property;
    
    private boolean usePropertyInfoCase;
    private boolean propertyInfoCase;
    
    private boolean useCaseManager;
    private User caseManagerUser;
    
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

    /**
     * @return the caseStageAsPhaseList
     */
    public List<CasePhase> getCaseStageAsPhaseList() {
        List<CasePhase> phaseList = new ArrayList<>();
        if(caseStage != null){
            switch(caseStage){
                case Investigation:
                    phaseList.add(CasePhase.PrelimInvestigationPending);
                    phaseList.add(CasePhase.NoticeDelivery);
                    break;
                
                case Enforcement:
                    phaseList.add(CasePhase.InitialComplianceTimeframe);
                    phaseList.add(CasePhase.SecondaryPostHearingComplianceTimeframe);
                    break;
                    
                case Citation:
                    phaseList.add(CasePhase.AwaitingHearingDate);
                    phaseList.add(CasePhase.HearingPreparation);
                    phaseList.add(CasePhase.InitialPostHearingComplianceTimeframe);
                    phaseList.add(CasePhase.SecondaryPostHearingComplianceTimeframe);
                    break;
                
                case Closed:
                    phaseList.add(CasePhase.Closed);
                    phaseList.add(CasePhase.InactiveHolding);
                    break;
            }
        }
        
        caseStageAsPhaseList = phaseList;
        return caseStageAsPhaseList;
    }

    /**
     * @param caseStageAsPhaseList the caseStageAsPhaseList to set
     */
    public void setCaseStageAsPhaseList(List<CasePhase> caseStageAsPhaseList) {
        this.caseStageAsPhaseList = caseStageAsPhaseList;
    }

    /**
     * @return the caseStage
     */
    public CaseStage getCaseStage() {
        return caseStage;
    }

    /**
     * @param caseStage the caseStage to set
     */
    public void setCaseStage(CaseStage caseStage) {
        this.caseStage = caseStage;
    }

    /**
     * @return the dateSearchOptions
     */
    public List<String> getDateSearchOptions() {
        List<String> dateOptList = new ArrayList<>();
        dateOptList.add("Opening date of record");
        dateOptList.add("Database record timestamp");
        dateOptList.add("Closing date");
        dateSearchOptions = dateOptList;
        return dateSearchOptions;
    }

    /**
     * @param dateSearchOptions the dateSearchOptions to set
     */
    public void setDateSearchOptions(List<String> dateSearchOptions) {
        this.dateSearchOptions = dateSearchOptions;
    }
   
   
   

   
    
}
