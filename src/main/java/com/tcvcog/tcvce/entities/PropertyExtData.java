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

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Storage container for records extracted from the
 * propertyexternaldata table
 * READ ONLY facilities for Beta V.0.9
 * 
 * @author SNAPPERS
 */
public class PropertyExtData {
    private int extdataid;
    private int property_propertyid;
    private String ownername;
    private String ownerphone;
    private String address_street;
    private String address_citystatezip;
    private String address_city;
    private String address_state;
    private String address_zip;
    private double saleprice;
    private double saleyear;
    private double assessedlandvalue;
    private double assessedbuildingvalue;
    private int assessmentyear;
    private String usecode;
    private int yearbuilt;
    private int livingarea;
    private String condition;
    private String notes;
    private LocalDateTime lastUpdatedTS;
    private String taxCode;
    private TaxStatus taxStatus;
    
    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + this.extdataid;
        hash = 67 * hash + this.property_propertyid;
        hash = 67 * hash + Objects.hashCode(this.ownername);
        hash = 67 * hash + Objects.hashCode(this.ownerphone);
        hash = 67 * hash + Objects.hashCode(this.address_street);
        hash = 67 * hash + Objects.hashCode(this.address_citystatezip);
        hash = 67 * hash + Objects.hashCode(this.address_city);
        hash = 67 * hash + Objects.hashCode(this.address_state);
        hash = 67 * hash + Objects.hashCode(this.address_zip);
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.saleprice) ^ (Double.doubleToLongBits(this.saleprice) >>> 32));
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.saleyear) ^ (Double.doubleToLongBits(this.saleyear) >>> 32));
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.assessedlandvalue) ^ (Double.doubleToLongBits(this.assessedlandvalue) >>> 32));
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.assessedbuildingvalue) ^ (Double.doubleToLongBits(this.assessedbuildingvalue) >>> 32));
        hash = 67 * hash + this.assessmentyear;
        hash = 67 * hash + Objects.hashCode(this.usecode);
        hash = 67 * hash + this.yearbuilt;
        hash = 67 * hash + this.livingarea;
        hash = 67 * hash + Objects.hashCode(this.condition);
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
        final PropertyExtData other = (PropertyExtData) obj;
        if (this.extdataid != other.extdataid) {
            return false;
        }
        if (this.property_propertyid != other.property_propertyid) {
            return false;
        }
        if (Double.doubleToLongBits(this.saleprice) != Double.doubleToLongBits(other.saleprice)) {
            return false;
        }
        if (Double.doubleToLongBits(this.saleyear) != Double.doubleToLongBits(other.saleyear)) {
            return false;
        }
        if (Double.doubleToLongBits(this.assessedlandvalue) != Double.doubleToLongBits(other.assessedlandvalue)) {
            return false;
        }
        if (Double.doubleToLongBits(this.assessedbuildingvalue) != Double.doubleToLongBits(other.assessedbuildingvalue)) {
            return false;
        }
        if (this.assessmentyear != other.assessmentyear) {
            return false;
        }
        if (this.yearbuilt != other.yearbuilt) {
            return false;
        }
        if (this.livingarea != other.livingarea) {
            return false;
        }
        if (!Objects.equals(this.ownername, other.ownername)) {
            return false;
        }
        if (!Objects.equals(this.ownerphone, other.ownerphone)) {
            return false;
        }
        if (!Objects.equals(this.address_street, other.address_street)) {
            return false;
        }
        if (!Objects.equals(this.address_citystatezip, other.address_citystatezip)) {
            return false;
        }
        if (!Objects.equals(this.address_city, other.address_city)) {
            return false;
        }
        if (!Objects.equals(this.address_state, other.address_state)) {
            return false;
        }
        if (!Objects.equals(this.address_zip, other.address_zip)) {
            return false;
        }
        if (!Objects.equals(this.usecode, other.usecode)) {
            return false;
        }
        if (!Objects.equals(this.condition, other.condition)) {
            return false;
        }
        if (!Objects.equals(this.notes, other.notes)) {
            return false;
        }
        return true;
    }
    
    /**
     * @return the extdataid
     */
    public int getExtdataid() {
        return extdataid;
    }

    /**
     * @return the property_propertyid
     */
    public int getProperty_propertyid() {
        return property_propertyid;
    }

    /**
     * @return the ownername
     */
    public String getOwnername() {
        return ownername;
    }

    /**
     * @return the ownerphone
     */
    public String getOwnerphone() {
        return ownerphone;
    }

    /**
     * @return the address_street
     */
    public String getAddress_street() {
        return address_street;
    }

    /**
     * @return the address_citystatezip
     */
    public String getAddress_citystatezip() {
        return address_citystatezip;
    }

    /**
     * @return the address_city
     */
    public String getAddress_city() {
        return address_city;
    }

    /**
     * @return the address_state
     */
    public String getAddress_state() {
        return address_state;
    }

    /**
     * @return the address_zip
     */
    public String getAddress_zip() {
        return address_zip;
    }

    /**
     * @return the saleprice
     */
    public double getSaleprice() {
        return saleprice;
    }

    /**
     * @return the saleyear
     */
    public double getSaleyear() {
        return saleyear;
    }

    /**
     * @return the assessedlandvalue
     */
    public double getAssessedlandvalue() {
        return assessedlandvalue;
    }

    /**
     * @return the assessedbuildingvalue
     */
    public double getAssessedbuildingvalue() {
        return assessedbuildingvalue;
    }

    /**
     * @return the assessmentyear
     */
    public int getAssessmentyear() {
        return assessmentyear;
    }

    /**
     * @return the usecode
     */
    public String getUsecode() {
        return usecode;
    }

    /**
     * @return the yearbuilt
     */
    public int getYearbuilt() {
        return yearbuilt;
    }

    /**
     * @return the livingarea
     */
    public int getLivingarea() {
        return livingarea;
    }

    /**
     * @return the condition
     */
    public String getCondition() {
        return condition;
    }


    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }


    /**
     * @param extdataid the extdataid to set
     */
    public void setExtdataid(int extdataid) {
        this.extdataid = extdataid;
    }

    /**
     * @param property_propertyid the property_propertyid to set
     */
    public void setProperty_propertyid(int property_propertyid) {
        this.property_propertyid = property_propertyid;
    }

    /**
     * @param ownername the ownername to set
     */
    public void setOwnername(String ownername) {
        this.ownername = ownername;
    }

    /**
     * @param ownerphone the ownerphone to set
     */
    public void setOwnerphone(String ownerphone) {
        this.ownerphone = ownerphone;
    }

    /**
     * @param address_street the address_street to set
     */
    public void setAddress_street(String address_street) {
        this.address_street = address_street;
    }

    /**
     * @param address_citystatezip the address_citystatezip to set
     */
    public void setAddress_citystatezip(String address_citystatezip) {
        this.address_citystatezip = address_citystatezip;
    }

    /**
     * @param address_city the address_city to set
     */
    public void setAddress_city(String address_city) {
        this.address_city = address_city;
    }

    /**
     * @param address_state the address_state to set
     */
    public void setAddress_state(String address_state) {
        this.address_state = address_state;
    }

    /**
     * @param address_zip the address_zip to set
     */
    public void setAddress_zip(String address_zip) {
        this.address_zip = address_zip;
    }

    /**
     * @param saleprice the saleprice to set
     */
    public void setSaleprice(double saleprice) {
        this.saleprice = saleprice;
    }

    /**
     * @param saleyear the saleyear to set
     */
    public void setSaleyear(double saleyear) {
        this.saleyear = saleyear;
    }

    /**
     * @param assessedlandvalue the assessedlandvalue to set
     */
    public void setAssessedlandvalue(double assessedlandvalue) {
        this.assessedlandvalue = assessedlandvalue;
    }

    /**
     * @param assessedbuildingvalue the assessedbuildingvalue to set
     */
    public void setAssessedbuildingvalue(double assessedbuildingvalue) {
        this.assessedbuildingvalue = assessedbuildingvalue;
    }

    /**
     * @param assessmentyear the assessmentyear to set
     */
    public void setAssessmentyear(int assessmentyear) {
        this.assessmentyear = assessmentyear;
    }

    /**
     * @param usecode the usecode to set
     */
    public void setUsecode(String usecode) {
        this.usecode = usecode;
    }

    /**
     * @param yearbuilt the yearbuilt to set
     */
    public void setYearbuilt(int yearbuilt) {
        this.yearbuilt = yearbuilt;
    }

    /**
     * @param livingarea the livingarea to set
     */
    public void setLivingarea(int livingarea) {
        this.livingarea = livingarea;
    }

    /**
     * @param condition the condition to set
     */
    public void setCondition(String condition) {
        this.condition = condition;
    }



    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }


    /**
     * @return the lastUpdatedTS
     */
    public LocalDateTime getLastUpdatedTS() {
        return lastUpdatedTS;
    }

    /**
     * @return the taxCode
     */
    public String getTaxCode() {
        return taxCode;
    }

    /**
     * @return the taxStatus
     */
    public TaxStatus getTaxStatus() {
        return taxStatus;
    }

    /**
     * @param lastUpdatedTS the lastUpdatedTS to set
     */
    public void setLastUpdatedTS(LocalDateTime lastUpdatedTS) {
        this.lastUpdatedTS = lastUpdatedTS;
    }

    /**
     * @param taxCode the taxCode to set
     */
    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    /**
     * @param taxStatus the taxStatus to set
     */
    public void setTaxStatus(TaxStatus taxStatus) {
        this.taxStatus = taxStatus;
    }
}
