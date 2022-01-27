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
        implements Serializable, Comparable<OccInspectedSpaceElement>, IFace_BlobHolder {

    private final static BlobLinkEnum BLOB_LINK_ENUM = BlobLinkEnum.INSPECTED_ELEMENT;
    
    private int inspectedSpaceElementID;

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
    private int failureIntensityClassID;
    private OccInspectableStatus status;

    private boolean migrateToCaseOnFail;

    /**
     * For advanced checklist object management in the UI
     * since the occinspectedspaceelement records know about entries in the
     * inspectedspace table
     */
    private int inspectedSpaceID;

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
            this.failureIntensityClassID = occInspectedSpaceElement.getFailureIntensityClassID();
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

    /**
     * @return the failureIntensityClassID
     */
    public int getFailureIntensityClassID() {
        return failureIntensityClassID;
    }

    /**
     * @param failureIntensityClassID the failureIntensityClassID to set
     */
    public void setFailureIntensityClassID(int failureIntensityClassID) {
        this.failureIntensityClassID = failureIntensityClassID;
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
        hash = 97 * hash + this.failureIntensityClassID;
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        if (obj instanceof CodeElement){
            if(this.elementID == ((CodeElement) obj).getElementID()){
                return true;
            } else {
                return false;
            }
        }
        final OccInspectedSpaceElement other = (OccInspectedSpaceElement) obj;
        if (this.inspectedSpaceElementID != other.inspectedSpaceElementID) {
            return false;
        }
        if (this.required != other.required) {
            return false;
        }
        if (this.failureIntensityClassID != other.failureIntensityClassID) {
            return false;
        }
        if (!Objects.equals(this.inspectionNotes, other.inspectionNotes)) {
            return false;
        }
        if (!Objects.equals(this.lastInspectedTS, other.lastInspectedTS)) {
            return false;
        }
        if (!Objects.equals(this.lastInspectedBy, other.lastInspectedBy)) {
            return false;
        }
        if (!Objects.equals(this.complianceGrantedTS, other.complianceGrantedTS)) {
            return false;
        }
        if (!Objects.equals(this.complianceGrantedBy, other.complianceGrantedBy)) {
            return false;
        }
        if (!Objects.equals(this.overrideRequiredFlag_thisElementNotInspectedBy, other.overrideRequiredFlag_thisElementNotInspectedBy)) {
            return false;
        }
        if (!Objects.equals(this.location, other.location)) {
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

    @Override
    public int compareTo(OccInspectedSpaceElement o) {
        if(inspectedSpaceElementID > o.getInspectedSpaceElementID()){
            return 1;
        } else if(inspectedSpaceElementID == o.getInspectedSpaceElementID()){
            return 0;
        } else {
            return -1;
        }
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
}
