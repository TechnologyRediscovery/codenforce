/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthorized;
import org.primefaces.model.menu.Submenu;
import java.io.Serializable;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
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
    
    public MenuModel getModel(){
        model = new DefaultMenuModel();
        UserAuthorized user = getSessionBean().getSessionUser();
        
        switch(user.getRoleType()){
            
            case Developer:
            case SysAdmin:
            case CogStaff:
            case EnforcementOfficial:
            case MuniStaff:
            case MuniReader:
            case Public:
            default:
        } // close switch
        
        
                

        
        
        DefaultSubMenu firstSub = new DefaultSubMenu("Java birthed me!");

        DefaultMenuItem item = new DefaultMenuItem("I am an item");
        item.setOutcome("propertySearch");
        firstSub.addElement(item);
        model.addElement(item);
        
        DefaultMenuItem item2 = new DefaultMenuItem("Take california");
        item2.setOutcome("personSearch");
        
        firstSub.addElement(item2);
        model.addElement(item2);
        
        model.addElement(getUniversalSubmenu());
        
        return model;
    }
    
}
