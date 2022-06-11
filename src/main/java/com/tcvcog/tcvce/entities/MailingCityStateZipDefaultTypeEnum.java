/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

/**
 * Enumerates the possible search fields for City/State/Zip
 * 
 * @author Ellen Bascomb of Apartment 31Y
 */
public enum MailingCityStateZipDefaultTypeEnum {
   
    MILITARY("Military", "MILITARY"),
    STANDARD("Standard", "STANDARD"),
    UNIQUE("Unique", "UNIQUE"),
    POBOX("PO Box", "PO BOX");
    
    private final String typeTitle;
    private final String typeDBString;
    
    private MailingCityStateZipDefaultTypeEnum(String t, String s){
        this.typeTitle = t;
        this.typeDBString = s;
    }

    /**
     * @return the typeTitle
     */
    public String getTypeTitle() {
        return typeTitle;
    }

    /**
     * @return the typeDBString
     */
    public String getTypeDBString() {
        return typeDBString;
    }
    
}
