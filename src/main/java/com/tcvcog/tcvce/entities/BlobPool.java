/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.io.Serializable;
import java.util.List;

/**
 * A somewhat hacky composition frame for creating a BlobPool that could have 
 * a list of BlobLights in its belly that correspond to basically any of the 
 * implementing classes of IFace_BlobHolder
 * 
 * This is an experimental construct for building the Blob world 
 * with entirely cross-BOB reusable code--a design endeavor that has taken years
 * to undertake with fluidity.
 * 
 * @author Ellen Bascomb of Apt 31Y | Commenced Jan 2022
 */
public class BlobPool implements Serializable, IFace_BlobHolder{
    
    private List<BlobLight> blobList;
    private BlobLinkEnum poolLinkEnum;
    private int blobParentID;
    /**
     * BlobHolders don't get a cascade of upstream pools yet.
     */
    private static final BlobLinkEnum UPSTREAM_POOL_ENUM = null;

    /**
     * So this will always be zero since pools won't get their own pools
     */
    private final static int BLOB_UPSTREAMPOOL_PARENT_ID = 0;
    
    /**
     * Fancy constructor that's designed to take in a LinkEnum that could
     * come from one of the BlobHolder's pool specs
     * @param parentID
     * @param ble 
     */
    public BlobPool(int parentID, BlobLinkEnum ble){
        blobParentID = parentID;
        poolLinkEnum = ble;
    }
    
    /**
     * @return the blobList
     */
    @Override
    public List<BlobLight> getBlobList() {
        return blobList;
    }

    /**
     * @return the poolLinkEnum
     */
    public BlobLinkEnum getPoolLinkEnum() {
        return poolLinkEnum;
    }

    /**
     * @param blobList the blobList to set
     */
    @Override
    public void setBlobList(List<BlobLight> blobList) {
        this.blobList = blobList;
    }

    /**
     * @param poolLinkEnum the poolLinkEnum to set
     */
    public void setPoolLinkEnum(BlobLinkEnum poolLinkEnum) {
        this.poolLinkEnum = poolLinkEnum;
    }

    @Override
    public BlobLinkEnum getBlobLinkEnum() {
        return poolLinkEnum;
    }

    @Override
    public int getParentObjectID() {
        return getBlobParentID();
    }

    /**
     * Pools don't get to have pools
     * @return 
     */
    @Override
    public BlobLinkEnum getBlobUpstreamPoolEnum() {
        return UPSTREAM_POOL_ENUM;
    }

    @Override
    public int getBlobUpstreamPoolEnumPoolFeederID() {
        return BLOB_UPSTREAMPOOL_PARENT_ID;
    }

    /**
     * @return the blobParentID
     */
    public int getBlobParentID() {
        return blobParentID;
    }

    /**
     * @param blobParentID the blobParentID to set
     */
    public void setBlobParentID(int blobParentID) {
        this.blobParentID = blobParentID;
    }
    
}
