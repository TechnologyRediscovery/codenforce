/*
 * Copyright (C) 2021 Technology Rediscovery LLC
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
package com.tcvcog.tcvce.entities.reports;

import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.CECasePropertyUnitHeavy;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains a single street's set of cases over the reporting period
 * 
 * @author sylvia
 */
public class ReportCECaseListStreetCECaseContainer
            implements Comparable<ReportCECaseListStreetCECaseContainer>{
   
    private String streetName;
    
    private List<CECaseDataHeavy> caseOpenedList;
    private List<CECaseDataHeavy> caseContinuingList;
    private List<CECaseDataHeavy> caseClosedList;
    private List<CECaseDataHeavy> combinedCaseList;
    
    protected boolean caseOpenedListDisplay;
    protected boolean caseContinuingListDisplay;
    protected boolean caseClosedListDisplay;

    public ReportCECaseListStreetCECaseContainer(){
        caseOpenedList = new ArrayList<>();
        caseContinuingList = new ArrayList<>();
        caseClosedList = new ArrayList<>();
        combinedCaseList = new ArrayList<>();
    }
    
    /**
     * Antipattern getter: aggregates internal lists
     * @return the combinedCaseList
     */
    public List<CECaseDataHeavy> getCombinedCaseList() {
        if(combinedCaseList != null){
            if(caseOpenedList != null){
                combinedCaseList.addAll(caseOpenedList);
            }
            if(caseContinuingList != null){
                combinedCaseList.addAll(caseContinuingList);
            }
            if(caseClosedList != null){
                combinedCaseList.addAll(caseClosedList);
            }
        }
        return combinedCaseList;
    }

    
    
    
    
    /** 
     
     * @param combinedCaseList the combinedCaseList to set
     */
    public void setCombinedCaseList(List<CECaseDataHeavy> combinedCaseList) {
        this.combinedCaseList = combinedCaseList;
    }
    
   
    
    /**
     * @return the streetName
     */
    public String getStreetName() {
        return streetName;
    }

    /**
     * @return the caseOpenedList
     */
    public List<CECaseDataHeavy> getCaseOpenedList() {
        return caseOpenedList;
    }

    /**
     * @return the caseContinuingList
     */
    public List<CECaseDataHeavy> getCaseContinuingList() {
        return caseContinuingList;
    }

    /**
     * @return the caseClosedList
     */
    public List<CECaseDataHeavy> getCaseClosedList() {
        return caseClosedList;
    }

    /**
     * @param streetName the streetName to set
     */
    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    /**
     * @param caseOpenedList the caseOpenedList to set
     */
    public void setCaseOpenedList(List<CECaseDataHeavy> caseOpenedList) {
        this.caseOpenedList = caseOpenedList;
    }

    /**
     * @param caseContinuingList the caseContinuingList to set
     */
    public void setCaseContinuingList(List<CECaseDataHeavy> caseContinuingList) {
        this.caseContinuingList = caseContinuingList;
    }

    /**
     * @param caseClosedList the caseClosedList to set
     */
    public void setCaseClosedList(List<CECaseDataHeavy> caseClosedList) {
        this.caseClosedList = caseClosedList;
    }

    @Override
    public int compareTo(ReportCECaseListStreetCECaseContainer o) {
        if(o != null && this.getStreetName() != null){
            return o.getStreetName().compareTo(this.getStreetName());
        }
        return 0;
    }

    /**
     * @return the caseOpenedListDisplay
     */
    public boolean isCaseOpenedListDisplay() {
        caseOpenedListDisplay = !caseClosedList.isEmpty();
        return caseOpenedListDisplay;
    }

    /**
     * @return the caseContinuingListDisplay
     */
    public boolean isCaseContinuingListDisplay() {
        caseContinuingListDisplay = !caseClosedList.isEmpty();
        return caseContinuingListDisplay;
    }

    /**
     * @return the caseClosedListDisplay
     */
    public boolean isCaseClosedListDisplay() {
        caseClosedListDisplay = !caseClosedList.isEmpty();
        return caseClosedListDisplay;
    }

    /**
     * @param caseOpenedListDisplay the caseOpenedListDisplay to set
     */
    public void setCaseOpenedListDisplay(boolean caseOpenedListDisplay) {
        this.caseOpenedListDisplay = caseOpenedListDisplay;
    }

    /**
     * @param caseContinuingListDisplay the caseContinuingListDisplay to set
     */
    public void setCaseContinuingListDisplay(boolean caseContinuingListDisplay) {
        this.caseContinuingListDisplay = caseContinuingListDisplay;
    }

    /**
     * @param caseClosedListDisplay the caseClosedListDisplay to set
     */
    public void setCaseClosedListDisplay(boolean caseClosedListDisplay) {
        this.caseClosedListDisplay = caseClosedListDisplay;
    }

    

    
    
}
