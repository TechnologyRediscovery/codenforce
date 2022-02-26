/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.search;

import com.tcvcog.tcvce.entities.RoleType;

/**
 * Each enum value specifies a selectable query against the parcel table
 * but we use the term "property" from legacy table designations
 * @author ellen bascomb of apartment 31Y
 */
public  enum        QueryPropertyEnum 
        implements  IFace_RankLowerBounded{
    
    ADDRESS_BLDG_NUM_ONLY(              "Search by building number only", 
                                        "Enter house number to search in your current municipality", 
                                        RoleType.MuniReader, 
                                        false,
                                        false),
    
    ADDRESS_STREET_ONLY(              "Search by street name only", 
                                        "Enter a street name to search in your current municipality", 
                                        RoleType.MuniReader, 
                                        false,
                                        false),
    
    ADDRESS_BLDGANDSTREET(              "Search by building number and street name", 
                                        "Enter a building number and street name to search in your current municipality", 
                                        RoleType.MuniReader, 
                                        false,
                                        false),
    
    PARCELID            (              "Search by parcelID", 
                                        "Enter a county parcel ID with all the zeros in just the right place", 
                                        RoleType.MuniReader, 
                                        false,
                                        false),
    
    LOTANDBLOCK(                        "Search by lot and block", 
                                        "Enter the three parts of the lot and block in the format 123-H-456", 
                                        RoleType.MuniReader, 
                                        false,
                                        false),
    
    RECORD_SOURCE(                      "Search by where the parcel record came from", 
                                        "Choose an object source from the drop down", 
                                        RoleType.MuniReader, 
                                        false,
                                        false),
    
    LAND_BANK_HELD(                     "List only properties held by the Tri-COG land bank", 
                                        "Lists currently held land bank properties", 
                                        RoleType.MuniReader, 
                                        false,
                                        false),
    
    NON_ADDRESSABLE(                     "List only special properties that don't have a postal address or parcelID", 
                                        "Lists non-addressable parcels", 
                                        RoleType.MuniReader, 
                                        false,
                                        false),
    
    UPDATED_PAST_MONTH(                 "Queries for properties that have been updated in any way in the last month", 
                                        "Applies only to parcel info record updates, not the parcel record itself", 
                                        RoleType.MuniReader, 
                                        false,
                                        false),
    
    PARCELINTERNALID(                 "Searches based on the internal parcel ID--not the county parcel ID", 
                                        "Enter the internal parcel identifier", 
                                        RoleType.MuniReader, 
                                        false,
                                        false),
    
    CUSTOM(                             "Custom", 
                                        "Custom", 
                                        RoleType.MuniReader, 
                                        false,
                                        true);
    
    private final String title;
    private final String desc;
    private final RoleType requiredRoleMin;
    private final boolean log;
    private final boolean showAllControls;
    
    private QueryPropertyEnum(String t, String l, RoleType minRoleType, boolean lg, boolean showAll){
        this.title = t;
        this.desc = l;
        if(minRoleType != null){
            this.requiredRoleMin = minRoleType;
        } else {
            this.requiredRoleMin = RoleType.MuniStaff;
        }
        this.log = lg;
        this.showAllControls = showAll;
        
    }
    
    public String getDesc(){
        return desc;
    }
    
    public String getTitle(){
        return title;
    }
    
    public boolean logQueryRun(){
        return log;
    }

    @Override
    public RoleType getRequiredRoleMin() {
        return requiredRoleMin;
    }

    /**
     * @return the showAllControls
     */
    public boolean isShowAllControls() {
        return showAllControls;
    }
    
    
}
