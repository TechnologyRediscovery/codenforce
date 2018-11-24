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
 * Encapsulates municipality restrictions and start/end
 * dates for searching. Types for all three commonly used date
 * formats system wide are included in the params object
 * and the conversions are made automatically based on the
 * LocalDateTime value, allowing objects on the front end and 
 * integration end to just grab the date Type they need and go!
 * @author Sylvia Garland
 */
public class SearchParamsCECase extends SearchParams implements Serializable{
    
    private boolean useIsOpen;
    private boolean isOpen;
    
    private boolean useCaseCloseDateRange;
    private LocalDateTime caseCloseStartDate;
    private LocalDateTime caseCloseEndDate;
    
    private boolean useCasePhases;
    // this list will contain an empty case whose
    // phase we want to select in the search
    // kinda hackey, I know.
    private List<CECase> casePhasesList;
    
    private boolean useCaseManagerID;
    private int caseManagerID;
    
    
    
    
    
   
    
    
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
     * @return the useCasePhases
     */
    public boolean isUseCasePhases() {
        return useCasePhases;
    }

    /**
     * @return the casePhasesList
     */
    public List<CECase> getCasePhasesList() {
        return casePhasesList;
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
     * @param useCasePhases the useCasePhases to set
     */
    public void setUseCasePhases(boolean useCasePhases) {
        this.useCasePhases = useCasePhases;
    }

    /**
     * @param casePhasesList the casePhasesList to set
     */
    public void setCasePhasesList(List<CECase> casePhasesList) {
        this.casePhasesList = casePhasesList;
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
   
   
   

   
    
}
