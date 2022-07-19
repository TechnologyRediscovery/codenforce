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
 * Object for specifying either the Code Enforcement
 * or Occupancy domain
 * 
 * @author Ellen Bascomb
 */
public enum DomainEnum {
    
    CODE_ENFORCEMENT        (   "Code enforcement cases", 
                                "cecase_caseid",
                                "CE",
                                "fee"),
    OCCUPANCY               (   "Occupancy Periods", 
                                "occperiod_periodid",
                                "OCC",
                                "fine"),
    PARCEL                  (   "Parcels", 
                                "parcel_parcelid",
                                "PARCEL",
                                "none"),
    UNIVERSAL               (   "All event subdomains", 
                                "cecase_caseid",
                                "UNI",
                                "charge");                         // this field should never
                                                                // be used since the Search Coor
    private final String title;                                 // is interpreting UNIVERSAL to run
    private final String dbField;                               // the other two independently
                                                                // and combine the results\
    private final String abbrev;
    private final String chargeTypeName;
    
    private DomainEnum(String t, String db, String ab, String ctn){
        title = t;
        dbField = db;
        abbrev = ab;
        chargeTypeName = ctn;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the dbField
     */
    public String getDbField() {
        return dbField;
    }

    /**
     * @return the abbrev
     */
    public String getAbbrev() {
        return abbrev;
    }

    /**
     * @return the chargeTypeName
     */
    public String getChargeTypeName() {
        return chargeTypeName;
    }


    
}
