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
public interface IFace_Proposable {
    
    public int getChoiceID();
    public String getTitle();
    public String getDescription();
    public int getRelativeOrder();
    public boolean isActive();
    public int getMinimumRequiredUserRankToView();
    public int getMinimumRequiredUserRankToChoose();
    public Icon getIcon();
    public boolean isHidden();
    public void setHidden(boolean h);
    public boolean isCanChoose();
    public void setCanChoose(boolean ch);
    
    
}
