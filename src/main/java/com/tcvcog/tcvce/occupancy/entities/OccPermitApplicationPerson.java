/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.occupancy.entities;

import com.tcvcog.tcvce.entities.Person;

/**
 *
 * @author Dominic Pimpinella
 */
public class OccPermitApplicationPerson extends Person {

    private boolean updated;
    private boolean newperson;
    private boolean verified;
    
    /**
     * @return the updated
     */
    public boolean isUpdated() {
        return updated;
    }

    /**
     * @param updated the updated to set
     */
    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    /**
     * @return the newperson
     */
    public boolean isNewperson() {
        return newperson;
    }

    /**
     * @param newperson the newperson to set
     */
    public void setNewperson(boolean newperson) {
        this.newperson = newperson;
    }

    /**
     * @return the verified
     */
    public boolean isVerified() {
        return verified;
    }

    /**
     * @param verified the verified to set
     */
    public void setVerified(boolean verified) {
        this.verified = verified;
    } 
    
}
