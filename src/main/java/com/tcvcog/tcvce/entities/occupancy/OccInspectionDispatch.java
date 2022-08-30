/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.entities.TrackedEntity;
import com.tcvcog.tcvce.entities.User;
import java.time.LocalDateTime;

/**
 * Created with CHEN&CHEN to represent an inspection ready for
 * being slurped up by a handheld computer user
 * 
 * @author ELLEN BASCOMB of apartment 31Y
 */
public class OccInspectionDispatch 
        extends TrackedEntity{

    private String DIPSATCH_PK_FIELD = "dispatchid";
    private String DISPATCH_TABLENAME = "occinspectiondispatch";
    
    private int dispatchID;
    private int inspectionID;
    private String dispatchNotes;
    private LocalDateTime retrievalTS;
    private User retrievedBy;
    private LocalDateTime synchronizationTS;
    private String synchronizationNotes;
    // CHEN&CHEN had municode and muniname in their table, which
    // would be duplicating this data since we can get it via the 
    // inspection >> unit >> parcel >> muni
    // perhaps create a materialized view with the municode and name appended?

    @Override
    public String getPKFieldName() {
        return getDIPSATCH_PK_FIELD();
    }

    @Override
    public int getDBKey() {
        return getDispatchID();
    }

    @Override
    public String getDBTableName() {
        return getDISPATCH_TABLENAME();
    }

    /**
     * @return the DIPSATCH_PK_FIELD
     */
    public String getDIPSATCH_PK_FIELD() {
        return DIPSATCH_PK_FIELD;
    }

    /**
     * @return the DISPATCH_TABLENAME
     */
    public String getDISPATCH_TABLENAME() {
        return DISPATCH_TABLENAME;
    }

    /**
     * @return the dispatchID
     */
    public int getDispatchID() {
        return dispatchID;
    }

    /**
     * @return the inspectionID
     */
    public int getInspectionID() {
        return inspectionID;
    }

    /**
     * @return the retrievalTS
     */
    public LocalDateTime getRetrievalTS() {
        return retrievalTS;
    }

    /**
     * @return the retrievedBy
     */
    public User getRetrievedBy() {
        return retrievedBy;
    }

    /**
     * @return the synchronizationTS
     */
    public LocalDateTime getSynchronizationTS() {
        return synchronizationTS;
    }

    /**
     * @return the synchronizationNotes
     */
    public String getSynchronizationNotes() {
        return synchronizationNotes;
    }

    /**
     * @param DIPSATCH_PK_FIELD the DIPSATCH_PK_FIELD to set
     */
    public void setDIPSATCH_PK_FIELD(String DIPSATCH_PK_FIELD) {
        this.DIPSATCH_PK_FIELD = DIPSATCH_PK_FIELD;
    }

    /**
     * @param DISPATCH_TABLENAME the DISPATCH_TABLENAME to set
     */
    public void setDISPATCH_TABLENAME(String DISPATCH_TABLENAME) {
        this.DISPATCH_TABLENAME = DISPATCH_TABLENAME;
    }

    /**
     * @param dispatchID the dispatchID to set
     */
    public void setDispatchID(int dispatchID) {
        this.dispatchID = dispatchID;
    }

    /**
     * @param inspectionID the inspectionID to set
     */
    public void setInspectionID(int inspectionID) {
        this.inspectionID = inspectionID;
    }

    /**
     * @param retrievalTS the retrievalTS to set
     */
    public void setRetrievalTS(LocalDateTime retrievalTS) {
        this.retrievalTS = retrievalTS;
    }

    /**
     * @param retrievedBy the retrievedBy to set
     */
    public void setRetrievedBy(User retrievedBy) {
        this.retrievedBy = retrievedBy;
    }

    /**
     * @param synchronizationTS the synchronizationTS to set
     */
    public void setSynchronizationTS(LocalDateTime synchronizationTS) {
        this.synchronizationTS = synchronizationTS;
    }

    /**
     * @param synchronizationNotes the synchronizationNotes to set
     */
    public void setSynchronizationNotes(String synchronizationNotes) {
        this.synchronizationNotes = synchronizationNotes;
    }

    /**
     * @return the dispatchNotes
     */
    public String getDispatchNotes() {
        return dispatchNotes;
    }

    /**
     * @param dispatchNotes the dispatchNotes to set
     */
    public void setDispatchNotes(String dispatchNotes) {
        this.dispatchNotes = dispatchNotes;
    }
}
