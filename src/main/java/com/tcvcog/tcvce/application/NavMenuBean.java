/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import org.primefaces.model.menu.Submenu;
import java.io.Serializable;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;

/**
 *
 * @author sylvia
 */
public class NavMenuBean extends BackingBeanUtils implements Serializable{  
    
    public MenuModel model;
    
    /**
     * Creates a new instance of NavMenuBean
     */
    public NavMenuBean() {
        
    }
    
    private Submenu getUniversalSubmenu(){
        DefaultSubMenu uniSub = new DefaultSubMenu("Java birthed me!");

        DefaultMenuItem item = new DefaultMenuItem("Help");
        item.setOutcome("help");
        uniSub.addElement(item);
        
        item.setDisabled(true);
        return uniSub;
    }
    
   
    
}
