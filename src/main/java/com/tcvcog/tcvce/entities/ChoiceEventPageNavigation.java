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
public class ChoiceEventPageNavigation 
        extends Choice {
    
  private String navigationKeyConstant;

    /**
     * @return the navigationKeyConstant
     */
    public String getNavigationKeyConstant() {
        return navigationKeyConstant;
    }

    /**
     * @param navigationKeyConstant the navigationKeyConstant to set
     */
    public void setNavigationKeyConstant(String navigationKeyConstant) {
        this.navigationKeyConstant = navigationKeyConstant;
    }
   
}
