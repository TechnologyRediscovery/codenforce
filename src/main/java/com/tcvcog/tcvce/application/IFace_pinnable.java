/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.entities.IFace_keyIdentified;
import com.tcvcog.tcvce.entities.User;

/**
 * Signals that an object can be pinned and unpinned in the DB
 * Applies to CECases and OccPeriods as of July 2022
 * @author sylvia
 */
public interface IFace_pinnable extends IFace_keyIdentified {
    /**
     * Tells caller if the object is pinned or not
     * @return if the object is currently pinned
     */
    public boolean isPinned();
    
    /**
     * Example: The FK field for a cecase in cecasepin is cecase_caseid
     * so this getter will return "cecase_caseid"
     * @return 
     */
    public String getPinTableFKString();
    
    /**
     * Example: public.cecasepin
     * 
     * @return 
     */
    public String getPinTableName();
    
    
    /**
     * Injection site for the user doing the pinning
     * @param usr 
     */
    public void setPinner(User usr);
    
    /**
     * Getter for who did the pinning
     * @return 
     */
    public User getPinner();
    
    
}
