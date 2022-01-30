/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import com.tcvcog.tcvce.entities.occupancy.OccInspection;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;

/**
 * Marks a BlobHolder whose BlobList can be a pool for another
 * BlobHolder
 * 
 * @author sylvia
 */
public enum BlobPoolEnum {
    
    PROPERTY("Property", (IFace_BlobHolder) new Property(new Parcel())),
    CECASE("Code Enforcement Case", (IFace_BlobHolder) new CECase()),
    OCCPERIOD("Occupancy Period", (IFace_BlobHolder) new OccPeriod()),
    OCCINSPECTION("Occupancy Inspection", (IFace_BlobHolder) new OccInspection());
    
    private final String label;
    private final IFace_BlobHolder blobHolderClass;
    
    
    private BlobPoolEnum(String l, IFace_BlobHolder bh){
        label = l;
        blobHolderClass = bh;
        
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return the blobHolderClass
     */
    public IFace_BlobHolder getBlobHolderClass() {
        return blobHolderClass;
    }
    
}
