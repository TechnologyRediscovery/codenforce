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
public class ChoiceEventRule 
        extends Choice {
    
    private EventRuleAbstract rule;
    
    /**
     * When true, add the proposed rule to the BOB
     * When false, inactivate any existing rules of the specified ID
     */
    private boolean addRuleFuncSwitch;

    /**
     * @return the addRuleFuncSwitch
     */
    public boolean isAddRuleFuncSwitch() {
        return addRuleFuncSwitch;
    }

    /**
     * @param addRuleFuncSwitch the addRuleFuncSwitch to set
     */
    public void setAddRuleFuncSwitch(boolean addRuleFuncSwitch) {
        this.addRuleFuncSwitch = addRuleFuncSwitch;
    }

    /**
     * @return the rule
     */
    public EventRuleAbstract getRule() {
        return rule;
    }

    /**
     * @param rule the rule to set
     */
    public void setRule(EventRuleAbstract rule) {
        this.rule = rule;
    }
    

   
}
