/*
 * Copyright (C) 2018 Turtle Creek Valley
Council of Governments, PA
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
package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.entities.BlobLight;
import com.tcvcog.tcvce.entities.BlobLinkEnum;

import com.tcvcog.tcvce.entities.CodeElement;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.IFace_BlobHolder;
import com.tcvcog.tcvce.entities.IFace_keyIdentified;
import com.tcvcog.tcvce.entities.IFace_transferrable;
import com.tcvcog.tcvce.entities.IntensityClass;
import com.tcvcog.tcvce.entities.TransferrableEnum;
import com.tcvcog.tcvce.entities.User;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Represents an ordinance that has been inspected (or is in queue
 * to be inspected) during an occupancy inspection
 * @author ellen bascomb of apt 31y
 */
public class OccInspectedSpaceElement
        extends OccSpaceElement
        implements Serializable, 
                    Comparable<OccInspectedSpaceElement>, 
                    IFace_BlobHolder,
                    IFace_keyIdentified,
                    IFace_transferrable{

    final static BlobLinkEnum BLOB_LINK_ENUM = BlobLinkEnum.INSPECTED_ELEMENT;
    final static BlobLinkEnum BLOP_UPSPTREAM_POOL = BlobLinkEnum.FIELD_INSPECTION;
    final static TransferrableEnum TRANSFER_ENUM = TransferrableEnum.INSPECTED_ELEMENT;
    
    private int inspectedSpaceElementID;

    // Hacky attempt at not updating database but still having a responsive UI
    private boolean inspected;
    private boolean inspectedWithCompliance;
    
    // Here lies the remains of composition replaced by inheritance! 2-AUG-19 on occbeta
//    private OccSpaceElement spaceElement;

    private LocalDateTime lastInspectedTS;
    private User lastInspectedBy;

    private LocalDateTime complianceGrantedTS;
    private User complianceGrantedBy;

    private boolean required;
    private User overrideRequiredFlag_thisElementNotInspectedBy;
    private String inspectionNotes;

    private List<BlobLight> blobList;
    private OccLocationDescriptor location;
    
    private IntensityClass faillureSeverity;
    private OccInspectableStatus status;

    private boolean migrateToCaseOnFail;
    
    protected LocalDateTime transferredTS;
    protected User transferredBy;
    protected int transferredToCECaseID;
    

    /**
     * For advanced checklist object management in the UI
     * since the occinspectedspaceelement records know about entries in the
     * inspectedspace table
     */
    private int inspectedSpaceID;
    private int occInspectionID;

    public OccInspectedSpaceElement() { }

    public OccInspectedSpaceElement(EnforcableCodeElement ece) {
        super(ece);
    }

    public OccInspectedSpaceElement(OccSpaceElement occSpaceElement) {
        super(occSpaceElement);
    }

    public OccInspectedSpaceElement(OccInspectedSpaceElement occInspectedSpaceElement) {
        super(occInspectedSpaceElement);
        if(occInspectedSpaceElement != null){
            
            this.inspectedSpaceElementID = occInspectedSpaceElement.getInspectedSpaceElementID();
            this.lastInspectedTS = occInspectedSpaceElement.getLastInspectedTS();
            this.lastInspectedBy = occInspectedSpaceElement.getLastInspectedBy();
            
            this.complianceGrantedTS = occInspectedSpaceElement.getComplianceGrantedTS();
            this.complianceGrantedBy = occInspectedSpaceElement.getComplianceGrantedBy();
            this.required = occInspectedSpaceElement.isRequired();
            
            this.overrideRequiredFlag_thisElementNotInspectedBy = occInspectedSpaceElement.getOverrideRequiredFlag_thisElementNotInspectedBy();
            this.inspectionNotes = occInspectedSpaceElement.getInspectionNotes();
            this.blobList = occInspectedSpaceElement.getBlobList();
            
            this.location = occInspectedSpaceElement.getLocation();
            this.faillureSeverity = occInspectedSpaceElement.getFaillureSeverity();
            this.status = occInspectedSpaceElement.getStatus();
            
            this.inspectedSpaceID = occInspectedSpaceElement.getInspectedSpaceID();
        }
    }

    /**
     * @return the inspectedSpaceElementID
     */
    public int getInspectedSpaceElementID() {
        return inspectedSpaceElementID;
    }

    /**
     * @return the complianceGrantedTS
     */
    public LocalDateTime getComplianceGrantedTS() {
        return complianceGrantedTS;
    }

    /**
     * @return the inspectionnotes
     */
    public String getInspectionNotes() {
        return inspectionNotes;
    }

    /**
     * @param inspectedSpaceElementID the inspectedSpaceElementID to set
     */
    public void setInspectedSpaceElementID(int inspectedSpaceElementID) {
        this.inspectedSpaceElementID = inspectedSpaceElementID;
    }

    /**
     * @param complianceGrantedTS the complianceGrantedTS to set
     */
    public void setComplianceGrantedTS(LocalDateTime complianceGrantedTS) {
        this.complianceGrantedTS = complianceGrantedTS;
    }

    /**
     * @param inspectionNotes the inspectionnotes to set
     */
    public void setInspectionNotes(String inspectionNotes) {
        this.inspectionNotes = inspectionNotes;
    }


    /**
     * @return the lastInspectedTS
     */
    public LocalDateTime getLastInspectedTS() {
        return lastInspectedTS;
    }

    /**
     * @return the lastInspectedBy
     */
    public User getLastInspectedBy() {
        return lastInspectedBy;
    }

    /**
     * @return the complianceGrantedBy
     */
    public User getComplianceGrantedBy() {
        return complianceGrantedBy;
    }

    /**
     * @param lastInspectedTS the lastInspectedTS to set
     */
    public void setLastInspectedTS(LocalDateTime lastInspectedTS) {
        this.lastInspectedTS = lastInspectedTS;
    }

    /**
     * @param lastInspectedBy the lastInspectedBy to set
     */
    public void setLastInspectedBy(User lastInspectedBy) {
        this.lastInspectedBy = lastInspectedBy;
    }

    /**
     * @param complianceGrantedBy the complianceGrantedBy to set
     */
    public void setComplianceGrantedBy(User complianceGrantedBy) {
        this.complianceGrantedBy = complianceGrantedBy;
    }

    /**
     * @return the location
     */
    public OccLocationDescriptor getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(OccLocationDescriptor location) {
        this.location = location;
    }

    /**
     * @return the required
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * @return the overrideRequiredFlag_thisElementNotInspectedBy
     */
    public User getOverrideRequiredFlag_thisElementNotInspectedBy() {
        return overrideRequiredFlag_thisElementNotInspectedBy;
    }

    /**
     * @param required the required to set
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * @param overrideRequiredFlag_thisElementNotInspectedBy the overrideRequiredFlag_thisElementNotInspectedBy to set
     */
    public void setOverrideRequiredFlag_thisElementNotInspectedBy(User overrideRequiredFlag_thisElementNotInspectedBy) {
        this.overrideRequiredFlag_thisElementNotInspectedBy = overrideRequiredFlag_thisElementNotInspectedBy;
    }


    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.inspectedSpaceElementID;
        hash = 97 * hash + Objects.hashCode(this.lastInspectedTS);
        hash = 97 * hash + Objects.hashCode(this.lastInspectedBy);
        hash = 97 * hash + Objects.hashCode(this.complianceGrantedTS);
        hash = 97 * hash + Objects.hashCode(this.complianceGrantedBy);
        hash = 97 * hash + (this.required ? 1 : 0);
        hash = 97 * hash + Objects.hashCode(this.overrideRequiredFlag_thisElementNotInspectedBy);
        hash = 97 * hash + Objects.hashCode(this.inspectionNotes);
        hash = 97 * hash + Objects.hashCode(this.location);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        final OccInspectedSpaceElement other = (OccInspectedSpaceElement) obj;
        if (this.inspectedSpaceElementID != other.inspectedSpaceElementID) {
            return false;
        }
       
        return true;
    }

   

    /**
     * @return the inspectedSpaceID
     */
    public int getInspectedSpaceID() {
        return inspectedSpaceID;
    }

    /**
     * @param inspectedSpaceID the inspectedSpaceID to set
     */
    public void setInspectedSpaceID(int inspectedSpaceID) {
        this.inspectedSpaceID = inspectedSpaceID;
    }

    /**
     * @return the blobList
     */
    @Override
    public List<BlobLight> getBlobList() {
        return blobList;
    }

    /**
     * @param blobList the blobList to set
     */
    @Override
    public void setBlobList(List<BlobLight> blobList) {
        this.blobList = blobList;
    }   

    /**
     * @return the status
     */
    public OccInspectableStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(OccInspectableStatus status) {
        this.status = status;
    }

    public boolean isMigrateToCaseOnFail() {
        return migrateToCaseOnFail;
    }

    public void setMigrateToCaseOnFail(boolean migrateToCaseOnFail) {
        this.migrateToCaseOnFail = migrateToCaseOnFail;
    }

    /**
     * Sorts by ordinance chapter
     * @param o
     * @return 
     */
    @Override
    public int compareTo(OccInspectedSpaceElement o) {
        if(ordSecTitle == null || o == null || o.ordSecTitle == null){
            return 0;
        }
        return ordSecTitle.compareTo(o.ordSecTitle);
    }

    /**
     * Not boring! These are getters and setter wrappers for the status parameter that take and give raw enums
     *
     * @return 
     **/
    public OccInspectionStatusEnum getStatusEnum() {
        return getStatus().getStatusEnum();
    }

   
    
    public void setStatusEnum(OccInspectionStatusEnum statusEnum) {
        setStatus(new OccInspectableStatus(statusEnum));
    }

    @Override
    public BlobLinkEnum getBlobLinkEnum() {
        return BLOB_LINK_ENUM;
    }

    @Override
    public int getParentObjectID() {
        return inspectedSpaceElementID;
    }

    @Override
    public BlobLinkEnum getBlobUpstreamPoolEnum() {
        return BLOP_UPSPTREAM_POOL;
    }

    /**
     * @return the occInspectionID
     */
    public int getOccInspectionID() {
        return occInspectionID;
    }

    /**
     * @param occInspectionID the occInspectionID to set
     */
    public void setOccInspectionID(int occInspectionID) {
        this.occInspectionID = occInspectionID;
    }

    @Override
    public int getBlobUpstreamPoolEnumPoolFeederID() {
        return occInspectionID;
    }

    /**
     * @return the faillureSeverity
     */
    public IntensityClass getFaillureSeverity() {
        return faillureSeverity;
    }

    /**
     * @param faillureSeverity the faillureSeverity to set
     */
    public void setFaillureSeverity(IntensityClass faillureSeverity) {
        this.faillureSeverity = faillureSeverity;
    }
    


    /**
     * @return the transferredTS
     */
    @Override
    public LocalDateTime getTransferredTS() {
        return transferredTS;
    }

    /**
     * @param transferredTS the transferredTS to set
     */
    @Override
    public void setTransferredTS(LocalDateTime transferredTS) {
        this.transferredTS = transferredTS;
    }


    @Override
    public void setTransferredBy(User usr) {
        transferredBy = usr;
    }

    @Override
    public void setTransferredToCECaseID(int ceCaseID) {
        transferredToCECaseID = ceCaseID;
    }

    @Override
    public User getTransferredBy() {
        return transferredBy;
    }

    @Override
    public int getTransferredToCECaseID() {
        return transferredToCECaseID;
    }

    @Override
    public TransferrableEnum getTransferEnum() {
        return TRANSFER_ENUM;
    }

    @Override
    public String getPKFieldName() {
        return TRANSFER_ENUM.getTargetPKField();
    }

    @Override
    public int getDBKey() {
        return occChecklistSpaceTypeElementID;
    }

    @Override
    public String getDBTableName() {
        return TRANSFER_ENUM.getTargetTableID();
        
    }

    /**
     * @return the inspected
     */
    public boolean isInspected() {
        return inspected;
    }

    /**
     * @return the inspectedWithCompliance
     */
    public boolean isInspectedWithCompliance() {
        return inspectedWithCompliance;
    }

    /**
     * @param inspected the inspected to set
     */
    public void setInspected(boolean inspected) {
        this.inspected = inspected;
    }

    /**
     * @param inspectedWithCompliance the inspectedWithCompliance to set
     */
    public void setInspectedWithCompliance(boolean inspectedWithCompliance) {
        this.inspectedWithCompliance = inspectedWithCompliance;
    }

   

}
