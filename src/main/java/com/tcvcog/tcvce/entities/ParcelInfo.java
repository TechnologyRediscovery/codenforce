/*
 * Copyright (C) 2020 Technology Rediscovery LLC
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

import com.tcvcog.tcvce.entities.occupancy.OccLocationDescriptor;
import java.time.LocalDate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Note that we might have thought that this class should
 * extend Parcel: the tradeoff is that doing so would mean we lose
 * the TrackedEntity superclass applied to externally scraped info. Not
 * extending Parcel means that we can use the TrackedEntity tools
 * easily and compose a Parcel of its Info bundles. Not extending also
 * allows us to have a list of these objects in a single Parcel, not 
 * something we can do with inheritance.
 * 
 * @author Ellen Bascomb of Apartment 31Y
 */
public class ParcelInfo 
        extends TrackedEntity{
    
    final static String PARCELINFOTABLE = "parcelinfo";
    final static String PARCELINFOTABLE_PKFIELD = "parcelinfoid";
    
    
    protected PropertyUseType useType;
    protected int parcelInfoID;
    protected int parcelInternalID;
    
    protected String useGroup;
    protected String constructionType;
    protected String countyCode;
    protected String ownerCode;
    protected String propClass;
    
    protected OccLocationDescriptor  locationDescriptor;
    
    protected BOBSource bobSource;
    protected LocalDate unfitDateStart;
    protected LocalDate unfitDateStop;
    protected User unfitBy;
    
    protected LocalDate abandonedDateStart;
    protected LocalDate abandonedDateStop;
    protected User abandonedBy;
    protected LocalDate vacantDateStart;
    
    protected LocalDate vacantDateStop;
    protected User vacantBy;
    protected IntensityClass condition;
    
    protected IntensityClass landBankProspect;
    protected boolean LandBankHeld;
    protected boolean active;
    protected boolean nonAddressable;
    
    protected int saleYear;
    protected int salePrice;
    protected int landValue;
    protected int buildingValue;
    protected int assessmentYear;
    protected int yearBuilt;
    protected int livingArea;
    protected boolean taxStatus;
    protected int taxYear;
    
    protected String notes;

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.useType);
        hash = 67 * hash + this.parcelInfoID;
        hash = 67 * hash + this.parcelInternalID;
        hash = 67 * hash + Objects.hashCode(this.useGroup);
        hash = 67 * hash + Objects.hashCode(this.constructionType);
        hash = 67 * hash + Objects.hashCode(this.countyCode);
        hash = 67 * hash + Objects.hashCode(this.ownerCode);
        hash = 67 * hash + Objects.hashCode(this.propClass);
        hash = 67 * hash + Objects.hashCode(this.locationDescriptor);
        hash = 67 * hash + Objects.hashCode(this.bobSource);
        hash = 67 * hash + Objects.hashCode(this.unfitDateStart);
        hash = 67 * hash + Objects.hashCode(this.unfitDateStop);
        hash = 67 * hash + Objects.hashCode(this.unfitBy);
        hash = 67 * hash + Objects.hashCode(this.abandonedDateStart);
        hash = 67 * hash + Objects.hashCode(this.abandonedDateStop);
        hash = 67 * hash + Objects.hashCode(this.abandonedBy);
        hash = 67 * hash + Objects.hashCode(this.vacantDateStart);
        hash = 67 * hash + Objects.hashCode(this.vacantDateStop);
        hash = 67 * hash + Objects.hashCode(this.vacantBy);
        hash = 67 * hash + Objects.hashCode(this.condition);
        hash = 67 * hash + Objects.hashCode(this.landBankProspect);
        hash = 67 * hash + (this.LandBankHeld ? 1 : 0);
        hash = 67 * hash + (this.active ? 1 : 0);
        hash = 67 * hash + (this.nonAddressable ? 1 : 0);
        hash = 67 * hash + this.saleYear;
        hash = 67 * hash + this.salePrice;
        hash = 67 * hash + this.landValue;
        hash = 67 * hash + this.buildingValue;
        hash = 67 * hash + this.assessmentYear;
        hash = 67 * hash + this.yearBuilt;
        hash = 67 * hash + this.livingArea;
        hash = 67 * hash + (this.taxStatus ? 1 : 0);
        hash = 67 * hash + this.taxYear;
        hash = 67 * hash + Objects.hashCode(this.notes);
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
        final ParcelInfo other = (ParcelInfo) obj;
        if (this.parcelInfoID != other.parcelInfoID) {
            return false;
        }
        return true;
    }
    
    /**
     * Convenience method for asking the parcel info if
     * it's inside an abandonment range.
     * @return 
     */
    public boolean isAbandoned(){
        boolean ab = false;
        if(abandonedDateStart != null && abandonedDateStart.isBefore(LocalDate.now())){
            if(abandonedDateStop == null){
                ab = true;
            } else if(abandonedDateStop.isAfter(LocalDate.now())){
                ab = true;
            }
        }
        
        return ab;
    }
    

    /**
     * @return the parcelInfoID
     */
    public int getParcelInfoID() {
        return parcelInfoID;
    }

    /**
     * @return the locationDescriptor
     */
    public OccLocationDescriptor getLocationDescriptor() {
        return locationDescriptor;
    }

    /**
     * @return the bobSource
     */
    public BOBSource getBobSource() {
        return bobSource;
    }

    /**
     * @return the unfitDateStart
     */
    public LocalDate getUnfitDateStart() {
        return unfitDateStart;
    }

    /**
     * @return the unfitDateStop
     */
    public LocalDate getUnfitDateStop() {
        return unfitDateStop;
    }

    /**
     * @return the unfitBy
     */
    public User getUnfitBy() {
        return unfitBy;
    }

    /**
     * @return the abandonedDateStart
     */
    public LocalDate getAbandonedDateStart() {
        return abandonedDateStart;
    }

    /**
     * @return the abandonedDateStop
     */
    public LocalDate getAbandonedDateStop() {
        return abandonedDateStop;
    }

    /**
     * @return the abandonedBy
     */
    public User getAbandonedBy() {
        return abandonedBy;
    }

    /**
     * @return the vacantDateStart
     */
    public LocalDate getVacantDateStart() {
        return vacantDateStart;
    }

    /**
     * @return the vacantDateStop
     */
    public LocalDate getVacantDateStop() {
        return vacantDateStop;
    }

    /**
     * @return the vacantBy
     */
    public User getVacantBy() {
        return vacantBy;
    }

    /**
     * @return the condition
     */
    public IntensityClass getCondition() {
        return condition;
    }

    /**
     * @return the landBankProspect
     */
    public IntensityClass getLandBankProspect() {
        return landBankProspect;
    }

    /**
     * @return the LandBankHeld
     */
    public boolean isLandBankHeld() {
        return LandBankHeld;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @return the nonAddressable
     */
    public boolean isNonAddressable() {
        return nonAddressable;
    }

    /**
     * @return the saleYear
     */
    public int getSaleYear() {
        return saleYear;
    }

    /**
     * @return the salePrice
     */
    public int getSalePrice() {
        return salePrice;
    }

    /**
     * @return the landValue
     */
    public int getLandValue() {
        return landValue;
    }

    /**
     * @return the buildingValue
     */
    public int getBuildingValue() {
        return buildingValue;
    }

    /**
     * @return the assessmentYear
     */
    public int getAssessmentYear() {
        return assessmentYear;
    }

    /**
     * @return the yearBuilt
     */
    public int getYearBuilt() {
        return yearBuilt;
    }

    /**
     * @return the livingArea
     */
    public int getLivingArea() {
        return livingArea;
    }

    /**
     * @return the taxStatus
     */
    public boolean isTaxStatus() {
        return taxStatus;
    }

    /**
     * @return the taxYear
     */
    public int getTaxYear() {
        return taxYear;
    }

    /**
     * @param parcelInfoID the parcelInfoID to set
     */
    public void setParcelInfoID(int parcelInfoID) {
        this.parcelInfoID = parcelInfoID;
    }

    /**
     * @param locationDescriptor the locationDescriptor to set
     */
    public void setLocationDescriptor(OccLocationDescriptor locationDescriptor) {
        this.locationDescriptor = locationDescriptor;
    }

    /**
     * @param bobSource the bobSource to set
     */
    public void setBobSource(BOBSource bobSource) {
        this.bobSource = bobSource;
    }

    /**
     * @param unfitDateStart the unfitDateStart to set
     */
    public void setUnfitDateStart(LocalDate unfitDateStart) {
        this.unfitDateStart = unfitDateStart;
    }

    /**
     * @param unfitDateStop the unfitDateStop to set
     */
    public void setUnfitDateStop(LocalDate unfitDateStop) {
        this.unfitDateStop = unfitDateStop;
    }

    /**
     * @param unfitBy the unfitBy to set
     */
    public void setUnfitBy(User unfitBy) {
        this.unfitBy = unfitBy;
    }

    /**
     * @param abandonedDateStart the abandonedDateStart to set
     */
    public void setAbandonedDateStart(LocalDate abandonedDateStart) {
        this.abandonedDateStart = abandonedDateStart;
    }

    /**
     * @param abandonedDateStop the abandonedDateStop to set
     */
    public void setAbandonedDateStop(LocalDate abandonedDateStop) {
        this.abandonedDateStop = abandonedDateStop;
    }

    /**
     * @param abandonedBy the abandonedBy to set
     */
    public void setAbandonedBy(User abandonedBy) {
        this.abandonedBy = abandonedBy;
    }

    /**
     * @param vacantDateStart the vacantDateStart to set
     */
    public void setVacantDateStart(LocalDate vacantDateStart) {
        this.vacantDateStart = vacantDateStart;
    }

    /**
     * @param vacantDateStop the vacantDateStop to set
     */
    public void setVacantDateStop(LocalDate vacantDateStop) {
        this.vacantDateStop = vacantDateStop;
    }

    /**
     * @param vacantBy the vacantBy to set
     */
    public void setVacantBy(User vacantBy) {
        this.vacantBy = vacantBy;
    }

    /**
     * @param condition the condition to set
     */
    public void setCondition(IntensityClass condition) {
        this.condition = condition;
    }

    /**
     * @param landBankProspect the landBankProspect to set
     */
    public void setLandBankProspect(IntensityClass landBankProspect) {
        this.landBankProspect = landBankProspect;
    }

    /**
     * @param LandBankHeld the LandBankHeld to set
     */
    public void setLandBankHeld(boolean LandBankHeld) {
        this.LandBankHeld = LandBankHeld;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @param nonAddressable the nonAddressable to set
     */
    public void setNonAddressable(boolean nonAddressable) {
        this.nonAddressable = nonAddressable;
    }

    /**
     * @param saleYear the saleYear to set
     */
    public void setSaleYear(int saleYear) {
        this.saleYear = saleYear;
    }

    /**
     * @param salePrice the salePrice to set
     */
    public void setSalePrice(int salePrice) {
        this.salePrice = salePrice;
    }

    /**
     * @param landValue the landValue to set
     */
    public void setLandValue(int landValue) {
        this.landValue = landValue;
    }

    /**
     * @param buildingValue the buildingValue to set
     */
    public void setBuildingValue(int buildingValue) {
        this.buildingValue = buildingValue;
    }

    /**
     * @param assessmentYear the assessmentYear to set
     */
    public void setAssessmentYear(int assessmentYear) {
        this.assessmentYear = assessmentYear;
    }

    /**
     * @param yearBuilt the yearBuilt to set
     */
    public void setYearBuilt(int yearBuilt) {
        this.yearBuilt = yearBuilt;
    }

    /**
     * @param livingArea the livingArea to set
     */
    public void setLivingArea(int livingArea) {
        this.livingArea = livingArea;
    }

    /**
     * @param taxStatus the taxStatus to set
     */
    public void setTaxStatus(boolean taxStatus) {
        this.taxStatus = taxStatus;
    }

    /**
     * @param taxYear the taxYear to set
     */
    public void setTaxYear(int taxYear) {
        this.taxYear = taxYear;
    }

    /**
     * @return the useGroup
     */
    public String getUseGroup() {
        return useGroup;
    }

    /**
     * @return the constructionType
     */
    public String getConstructionType() {
        return constructionType;
    }

    /**
     * @return the countyCode
     */
    public String getCountyCode() {
        return countyCode;
    }

    /**
     * @return the ownerCode
     */
    public String getOwnerCode() {
        return ownerCode;
    }

    /**
     * @return the propClass
     */
    public String getPropClass() {
        return propClass;
    }

    /**
     * @param useGroup the useGroup to set
     */
    public void setUseGroup(String useGroup) {
        this.useGroup = useGroup;
    }

    /**
     * @param constructionType the constructionType to set
     */
    public void setConstructionType(String constructionType) {
        this.constructionType = constructionType;
    }

    /**
     * @param countyCode the countyCode to set
     */
    public void setCountyCode(String countyCode) {
        this.countyCode = countyCode;
    }

    /**
     * @param ownerCode the ownerCode to set
     */
    public void setOwnerCode(String ownerCode) {
        this.ownerCode = ownerCode;
    }

    /**
     * @param propClass the propClass to set
     */
    public void setPropClass(String propClass) {
        this.propClass = propClass;
    }

    /**
     * @return the useType
     */
    public PropertyUseType getUseType() {
        return useType;
    }

    /**
     * @param useType the useType to set
     */
    public void setUseType(PropertyUseType useType) {
        this.useType = useType;
    }

    /**
     * @return the parcelInternalID
     */
    public int getParcelInternalID() {
        return parcelInternalID;
    }

    /**
     * @param parcelInternalID the parcelInternalID to set
     */
    public void setParcelInternalID(int parcelInternalID) {
        this.parcelInternalID = parcelInternalID;
    }

    @Override
    public String getPKFieldName() {
        return PARCELINFOTABLE_PKFIELD;
    }

    @Override
    public int getDBKey() {
        return parcelInfoID;
    }

    @Override
    public String getDBTableName() {
        return PARCELINFOTABLE;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

   
    
}
