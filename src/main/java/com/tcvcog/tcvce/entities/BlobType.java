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
public enum BlobType {
    PHOTO(1),
    PDF(2);

    private final int typeID;

    private BlobType(int typeID){
        this.typeID = typeID;
    }
    
    public static BlobType blobTypeFromInt(int i){
        switch(i){
            case 1:
                return PHOTO;
            case 2:
                return PDF;
        }
        return null;
    }
    
    public int getTypID(){
        return this.typeID;
    }
}
