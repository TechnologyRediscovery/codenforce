/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

/**
 *
 * @author sylvia
 */
public interface Proposable {
    
    public int getChoiceID();
    public String getTitle();
    public String getDescription();
    public EventCategory getEventCategory();
    public EventRule getEventRule();
    public boolean isAddEventRule();
    public int getRelativeOrder();
    public boolean isActive();
    public int getMinimumRequiredUserRankToView();
    public int getMinimumRequiredUserRankToChoose();
    public Icon getIcon();
    
    
}
