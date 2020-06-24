/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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

/**
 *
 * @author sylvia
 */
public class    PropertyPublic 
       extends  BOb{
    
    protected int propertyID;
    
    protected String address;
    
    protected PropertyStatus status;
    
    protected Municipality muni;
    protected String parID;
    protected String lotAndBlock;
    
    protected String useGroup;
    protected String constructionType;
    protected String countyCode;
    protected String address_city;
    protected String address_state;
    protected String address_zip;
    protected String ownerCode;
    protected String propclass;
    protected PropertyUseType useType;

    public PropertyPublic() {
    }

    /**
     * @return the propertyID
     */
    public int getPropertyID() {
        return propertyID;
    }

    /**
     * @param propertyID the propertyID to set
     */
    public void setPropertyID(int propertyID) {
        this.propertyID = propertyID;
    }

    /**
     * @return the muni
     */
    public Municipality getMuni() {
        return muni;
    }

    /**
     * @param muni the muni to set
     */
    public void setMuni(Municipality muni) {
        this.muni = muni;
    }

    /**
     * @return the parID
     */
    public String getParID() {
        return parID;
    }

    /**
     * @param parID the parID to set
     */
    public void setParID(String parID) {
        this.parID = parID;
    }

    /**
     * @return the lotAndBlock
     */
    public String getLotAndBlock() {
        return lotAndBlock;
    }

    /**
     * @param lotAndBlock the lotAndBlock to set
     */
    public void setLotAndBlock(String lotAndBlock) {
        this.lotAndBlock = lotAndBlock;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @return the useType
     */
    public PropertyUseType getUseType() {
        return useType;
    }
    
    public void setUseType(PropertyUseType put){
        useType = put;
    }

    /**
     * @return the useGroup
     */
    public String getUseGroup() {
        return useGroup;
    }

    /**
     * @param constructionType the constructionType to set
     */
    public void setConstructionType(String constructionType) {
        this.constructionType = constructionType;
    }

    /**
     * @return the countyCode
     */
    public String getCountyCode() {
        return countyCode;
    }

    /**
     * @param countyCode the countyCode to set
     */
    public void setCountyCode(String countyCode) {
        this.countyCode = countyCode;
    }

    /**
     * @return the muniCode
     */
    public int getMuniCode() {
        if(muni != null){
            return muni.getMuniCode();
        } else {
            return -7;
        }
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
     * @return the ownerCode
     */
    public String getOwnerCode() {
        return ownerCode;
    }

    /**
     * @return the propclass
     */
    public String getPropclass() {
        return propclass;
    }

   
    /**
     * @param ownerCode the ownerCode to set
     */
    public void setOwnerCode(String ownerCode) {
        this.ownerCode = ownerCode;
    }

    /**
     * @param propclass the propclass to set
     */
    public void setPropclass(String propclass) {
        this.propclass = propclass;
    }


}
