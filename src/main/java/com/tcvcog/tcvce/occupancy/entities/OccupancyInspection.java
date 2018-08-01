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
package com.tcvcog.tcvce.occupancy.entities;

import com.tcvcog.tcvce.entities.User;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 *
 * @author Adam Gutonski
 */
public class OccupancyInspection {
    
    private int inspectionID;
    private User caseManager;
    
    private ArrayList<InspectedSpace> inspectedSpaceList;
    private ArrayList<InspectedSpace> spacesWithFailedElements;
    
    private boolean totalFeePaid;
    
    private OccPermitApplication application;
    private OccInspecStatus status;
    
    private LocalDateTime firstInspectionDate;
    private boolean firstInspectionPass;
    private LocalDateTime secondInspectionDate;
    private boolean SecondInspectionPass;
    
    private String occupancyInspectionNotes; 
    
    private int pacc;
    private boolean enablePacc;
    private User muniAuthGrantedBy;
    private String muniAuthNotes;
    
    private OccPermit permit;
    
    private ArrayList<Payment> payments;

    /**
     * @return the inspectionID
     */
    public int getInspectionID() {
        return inspectionID;
    }

    /**
     * @param inspectionID the inspectionID to set
     */
    public void setInspectionID(int inspectionID) {
        this.inspectionID = inspectionID;
    }

    /**
     * @return the firstInspectionDate
     */
    public LocalDateTime getFirstInspectionDate() {
        return firstInspectionDate;
    }

    /**
     * @param firstInspectionDate the firstInspectionDate to set
     */
    public void setFirstInspectionDate(LocalDateTime firstInspectionDate) {
        this.firstInspectionDate = firstInspectionDate;
    }

    /**
     * @return the firstInspectionPass
     */
    public boolean isFirstInspectionPass() {
        return firstInspectionPass;
    }

    /**
     * @param firstInspectionPass the firstInspectionPass to set
     */
    public void setFirstInspectionPass(boolean firstInspectionPass) {
        this.firstInspectionPass = firstInspectionPass;
    }

    /**
     * @return the secondInspectionDate
     */
    public LocalDateTime getSecondInspectionDate() {
        return secondInspectionDate;
    }

    /**
     * @param secondInspectionDate the secondInspectionDate to set
     */
    public void setSecondInspectionDate(LocalDateTime secondInspectionDate) {
        this.secondInspectionDate = secondInspectionDate;
    }

    /**
     * @return the SecondInspectionPass
     */
    public boolean isSecondInspectionPass() {
        return SecondInspectionPass;
    }

    /**
     * @param SecondInspectionPass the SecondInspectionPass to set
     */
    public void setSecondInspectionPass(boolean SecondInspectionPass) {
        this.SecondInspectionPass = SecondInspectionPass;
    }

    /**
     * @return the totalFeePaid
     */
    public boolean isTotalFeePaid() {
        return totalFeePaid;
    }

    /**
     * @param totalFeePaid the totalFeePaid to set
     */
    public void setTotalFeePaid(boolean totalFeePaid) {
        this.totalFeePaid = totalFeePaid;
    }

    /**
     * @return the occupancyInspectionNotes
     */
    public String getOccupancyInspectionNotes() {
        return occupancyInspectionNotes;
    }

    /**
     * @param occupancyInspectionNotes the occupancyInspectionNotes to set
     */
    public void setOccupancyInspectionNotes(String occupancyInspectionNotes) {
        this.occupancyInspectionNotes = occupancyInspectionNotes;
    }

  

    /**
     * @return the permit
     */
    public OccPermit getPermit() {
        return permit;
    }

    /**
     * @param permit the permit to set
     */
    public void setPermit(OccPermit permit) {
        this.permit = permit;
    }

    /**
     * @return the caseManager
     */
    public User getCaseManager() {
        return caseManager;
    }

    /**
     * @param caseManager the caseManager to set
     */
    public void setCaseManager(User caseManager) {
        this.caseManager = caseManager;
    }

    /**
     * @return the application
     */
    public OccPermitApplication getApplication() {
        return application;
    }

    /**
     * @param application the application to set
     */
    public void setApplication(OccPermitApplication application) {
        this.application = application;
    }

    /**
     * @return the status
     */
    public OccInspecStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(OccInspecStatus status) {
        this.status = status;
    }

    /**
     * @return the pacc
     */
    public int getPacc() {
        return pacc;
    }

    /**
     * @return the enablePacc
     */
    public boolean isEnablePacc() {
        return enablePacc;
    }

    /**
     * @return the muniAuthGrantedBy
     */
    public User getMuniAuthGrantedBy() {
        return muniAuthGrantedBy;
    }

    /**
     * @return the muniAuthNotes
     */
    public String getMuniAuthNotes() {
        return muniAuthNotes;
    }

    /**
     * @param pacc the pacc to set
     */
    public void setPacc(int pacc) {
        this.pacc = pacc;
    }

    /**
     * @param enablePacc the enablePacc to set
     */
    public void setEnablePacc(boolean enablePacc) {
        this.enablePacc = enablePacc;
    }

    /**
     * @param muniAuthGrantedBy the muniAuthGrantedBy to set
     */
    public void setMuniAuthGrantedBy(User muniAuthGrantedBy) {
        this.muniAuthGrantedBy = muniAuthGrantedBy;
    }

    /**
     * @param muniAuthNotes the muniAuthNotes to set
     */
    public void setMuniAuthNotes(String muniAuthNotes) {
        this.muniAuthNotes = muniAuthNotes;
    }

    /**
     * @return the payments
     */
    public ArrayList<Payment> getPayments() {
        return payments;
    }

    /**
     * @param payments the payments to set
     */
    public void setPayments(ArrayList<Payment> payments) {
        this.payments = payments;
    }

    /**
     * @return the inspectedSpaceList
     */
    public ArrayList<InspectedSpace> getInspectedSpaceList() {
        return inspectedSpaceList;
    }

    /**
     * @param inspectedSpaceList the inspectedSpaceList to set
     */
    public void setInspectedSpaceList(ArrayList<InspectedSpace> inspectedSpaceList) {
        this.inspectedSpaceList = inspectedSpaceList;
    }

    /**
     * @return the spacesWithFailedElements
     */
    public ArrayList<InspectedSpace> getSpacesWithFailedElements() {
        return spacesWithFailedElements;
    }

    /**
     * @param spacesWithFailedElements the spacesWithFailedElements to set
     */
    public void setSpacesWithFailedElements(ArrayList<InspectedSpace> spacesWithFailedElements) {
        this.spacesWithFailedElements = spacesWithFailedElements;
    }
    
    
}
