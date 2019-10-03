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
public class ChoiceEventCat 
        extends Choice {
    
    private EventCategory eventCategory;
    
    /**
     * When true, add the proposed category to the BOB
     * When false, inactivate any existing events of the specified category
     */
    private boolean addCategoryFuncSwitch;
    

    /**
     * @return the eventCategory
     */
    public EventCategory getEventCategory() {
        return eventCategory;
    }

    /**
     * @param eventCategory the eventCategory to set
     */
    public void setEventCategory(EventCategory eventCategory) {
        this.eventCategory = eventCategory;
    }

    /**
     * @return the addCategoryFuncSwitch
     */
    public boolean isAddCategoryFuncSwitch() {
        return addCategoryFuncSwitch;
    }

    /**
     * @param addCategoryFuncSwitch the addCategoryFuncSwitch to set
     */
    public void setAddCategoryFuncSwitch(boolean addCategoryFuncSwitch) {
        this.addCategoryFuncSwitch = addCategoryFuncSwitch;
    }

}
