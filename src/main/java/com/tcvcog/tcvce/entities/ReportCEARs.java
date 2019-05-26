/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.io.Serializable;

/**
 *
 * @author sylvia
 */
public class ReportCEARs extends Report implements Serializable{
    
    private boolean printFullCEARQueue;
    private boolean includePhotos;

    /**
     * @return the printFullCEARQueue
     */
    public boolean isPrintFullCEARQueue() {
        return printFullCEARQueue;
    }

    /**
     * @return the includePhotos
     */
    public boolean isIncludePhotos() {
        return includePhotos;
    }

    /**
     * @param printFullCEARQueue the printFullCEARQueue to set
     */
    public void setPrintFullCEARQueue(boolean printFullCEARQueue) {
        this.printFullCEARQueue = printFullCEARQueue;
    }

    /**
     * @param includePhotos the includePhotos to set
     */
    public void setIncludePhotos(boolean includePhotos) {
        this.includePhotos = includePhotos;
    }
    
}
