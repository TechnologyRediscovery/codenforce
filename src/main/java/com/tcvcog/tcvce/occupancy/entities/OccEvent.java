/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.occupancy.entities;

import com.tcvcog.tcvce.entities.EntityUtils;
import com.tcvcog.tcvce.entities.Event;
import java.io.Serializable;

/**
 *
 * @author sylvia
 */
public class OccEvent extends Event implements Serializable {
    private int occPeriodID;

    /**
     * @return the occPeriodID
     */
    public int getOccPeriodID() {
        return occPeriodID;
    }

    /**
     * @param occPeriodID the occPeriodID to set
     */
    public void setOccPeriodID(int occPeriodID) {
        this.occPeriodID = occPeriodID;
    }
    
    
}
