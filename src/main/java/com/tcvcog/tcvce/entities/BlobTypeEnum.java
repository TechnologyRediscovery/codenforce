/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

/**
 *
 * @author noah
 */
public enum BlobTypeEnum {
    PHOTO(1, "Photograph"),
    PDF(2, "PDF");

    private final int typeID;
    private final String title;

    private BlobTypeEnum(int typeID, String t){
        this.typeID = typeID;
        this.title = t;
    }
    
    public static BlobTypeEnum blobTypeFromInt(int i){
        switch(i){
            case 1:
                return PHOTO;
            case 2:
                return PDF;
        }
        return null;
    }
    
    
    
    public int getTypeID(){
        return this.typeID;
    }

    
    public String getTitle(){
        return title;
    }
    
}
