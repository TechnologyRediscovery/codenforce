/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.io.Serializable;

/**
 * Stores access data for a user
 * Note the switches are private and final--so they can't be tweaked on the fly
 * by changes in other classes. Watch git changes for those in here!
 *  
 * @author Ellen Baskem
 */
public class AccessKeyCard implements Serializable{

    private final boolean hasDeveloperPermissions;
    private final boolean hasSysAdminPermissions;
    private final boolean hasCOGStaffPermissions;
    private final boolean hasEnfOfficialPermissions;
    private final boolean hasMuniStaffPermissions;
    private final boolean hasMuniReaderPermissions;

    public AccessKeyCard( boolean dev,
                    boolean admin,
                    boolean cogstaff,
                    boolean ceo,
                    boolean munistaff,
                    boolean munireader){
        
        hasDeveloperPermissions = dev;
        hasSysAdminPermissions = admin;
        hasCOGStaffPermissions = cogstaff;
        hasEnfOfficialPermissions = ceo;
        hasMuniStaffPermissions = munistaff;
        hasMuniReaderPermissions = munireader;
        
    }


    /**
     * @return the hasDeveloperPermissions
     */
    public boolean isHasDeveloperPermissions() {
        return hasDeveloperPermissions;
    }

    /**
     * @return the hasSysAdminPermissions
     */
    public boolean isHasSysAdminPermissions() {
        return hasSysAdminPermissions;
    }

    /**
     * @return the hasCOGStaffPermissions
     */
    public boolean isHasCOGStaffPermissions() {
        return hasCOGStaffPermissions;
    }

    /**
     * @return the hasEnfOfficialPermissions
     */
    public boolean isHasEnfOfficialPermissions() {
        return hasEnfOfficialPermissions;
    }

    /**
     * @return the hasMuniStaffPermissions
     */
    public boolean isHasMuniStaffPermissions() {
        return hasMuniStaffPermissions;
    }

    /**
     * @return the hasMuniReaderPermissions
     */
    public boolean isHasMuniReaderPermissions() {
        return hasMuniReaderPermissions;
    }
    
    
    
}
