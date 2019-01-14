/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CasePhase;
import com.tcvcog.tcvce.entities.Municipality;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 *
 * @author Sylvia Garland
 */
public class SearchParamsCECase extends SearchParams implements Serializable{
    
    private boolean useIsOpen;
    private boolean isOpen;
       
    private boolean useCaseCloseDateRange;
    private LocalDateTime caseCloseStartDate;
    private LocalDateTime caseCloseEndDate;
    
    private boolean useCaseManagerID;
    private int caseManagerID;
    
    private boolean useLegacy;
     private boolean legacyCase;
    
    
   public SearchParamsCECase(){
       
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
     * @return the useCaseCloseDateRange
     */
    public boolean isUseCaseCloseDateRange() {
        return useCaseCloseDateRange;
    }

    /**
     * @return the caseCloseStartDate
     */
    public LocalDateTime getCaseCloseStartDate() {
        return caseCloseStartDate;
    }

    /**
     * @return the caseCloseEndDate
     */
    public LocalDateTime getCaseCloseEndDate() {
        return caseCloseEndDate;
    }

   

    /**
     * @return the useCaseManagerID
     */
    public boolean isUseCaseManagerID() {
        return useCaseManagerID;
    }

    /**
     * @return the caseManagerID
     */
    public int getCaseManagerID() {
        return caseManagerID;
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
     * @param useCaseCloseDateRange the useCaseCloseDateRange to set
     */
    public void setUseCaseCloseDateRange(boolean useCaseCloseDateRange) {
        this.useCaseCloseDateRange = useCaseCloseDateRange;
    }

    /**
     * @param caseCloseStartDate the caseCloseStartDate to set
     */
    public void setCaseCloseStartDate(LocalDateTime caseCloseStartDate) {
        this.caseCloseStartDate = caseCloseStartDate;
    }

    /**
     * @param caseCloseEndDate the caseCloseEndDate to set
     */
    public void setCaseCloseEndDate(LocalDateTime caseCloseEndDate) {
        this.caseCloseEndDate = caseCloseEndDate;
    }

   
    /**
     * @param useCaseManagerID the useCaseManagerID to set
     */
    public void setUseCaseManagerID(boolean useCaseManagerID) {
        this.useCaseManagerID = useCaseManagerID;
    }

    /**
     * @param caseManagerID the caseManagerID to set
     */
    public void setCaseManagerID(int caseManagerID) {
        this.caseManagerID = caseManagerID;
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
   
   
   

   
    
}
