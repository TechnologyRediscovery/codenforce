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
package com.tcvcog.tcvce.util;

/**
 *
 * @author ellen bascomb 
 */
public enum SubSysEnum {
   
    N_USER(             0, 
                        "N", 
                        "User", 
                        "User authorization and credentialization", 
                        false, // is BOb?
                        false, // is searchable?
                        true), // should be initialized? (i.e. in active service)
    
    I_MUNICIPALITY(     1, 
                        "I", 
                        "Municipality", 
                        "Municipalities and their default configurations", 
                        true, 
                        false,
                        true),
    
    II_CODEBOOK(        2, 
                        "II", 
                        "CodeBook", 
                        "Municipal code book from any source", 
                        true, 
                        false,
                        true),
    
    III_PROPERTY(       3, 
                        "III", 
                        "Property", 
                        "Properties and their property units", 
                        true, 
                        false,
                        true),
    
    IV_PERSON(          4, 
                        "IV", 
                        "Person", 
                        "Persons", 
                        true, 
                        true,
                        true),
    
    V_EVENT(            5, 
                        "V", 
                        "Event", 
                        "Events pertaining to code enforcment cases and occupancy periods", 
                        true, 
                        true,
                        true),
    
    VI_OCCPERIOD(       6, 
                        "Occ Period", 
                        "Occupancy Period, inspections, and permits", 
                        "User authorization and credentialization", 
                        true, 
                        true,
                        true),
    
    VII_CECASE(         7, 
                        "VII", 
                        "Code Enf Case", 
                        "Code enforcement cases, their violations, notices, and citations", 
                        true, 
                        true,
                        true),
    
    VIII_CEACTIONREQ(   8, 
                        "VIII", 
                        "Code Enf Action Request", 
                        "Requests for code enforcement investigation", 
                        true, 
                        true,
                        true),
    
    VIV_OCCAPP(         9, 
                        "VIV", 
                        "Occ Period App", 
                        "Applications for occupancy of any kind", 
                        false, 
                        false,
                        true),
    
    X_PAYMENT(          10, 
                        "X", 
                        "Payment", 
                        "Payments against fees assigned to occupancy period types, and code violations", 
                        false, 
                        false,
                        true),
    
    XI_REPORT(          11, 
                        "XI", 
                        "Report", 
                        "Reporting of all business objects", 
                        false, 
                        false,
                        true),
    
    XII_BLOB(           12, 
                        "XII", 
                        "Blob", 
                        "Images, documents, video, media misc.", 
                        false, 
                        false,
                        true),
    
    XIII_PUBLICINFO(    13, 
                        "XIII", 
                        "Public info", 
                        "Filtering of internal case data for public release", 
                        false, 
                        false,
                        true);
       
    
    private final int subSysID;
    private final String subSysID_Roman;
    private final String title;
    private final String description;
    private final boolean bob;
    private final boolean bobSearchable;
    private final boolean initialize;
    
    private SubSysEnum(  int id, 
                            String rn, 
                            String t, 
                            String des, 
                            boolean bh, 
                            boolean bs,
                            boolean in){
        
        subSysID = id;
        subSysID_Roman = rn;
        title = t;
        description = des;
        bob = bh;
        bobSearchable = bs;
        initialize = in;

        
    }

    /**
     * @return the subSysID_Roman
     */
    public String getSubSysID_Roman() {
        return subSysID_Roman;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the bob
     */
    public boolean isBob() {
        return bob;
    }

    /**
     * @return the bobSearchable
     */
    public boolean isBobSearchable() {
        return bobSearchable;
    }

    /**
     * @return the subSysID
     */
    public int getSubSysID() {
        return subSysID;
    }

    /**
     * @return the initialize
     */
    public boolean isInitialize() {
        return initialize;
    }
    
    
    
}
