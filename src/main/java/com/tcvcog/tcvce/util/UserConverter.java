package com.tcvcog.tcvce.util;

import com.tcvcog.tcvce.entities.User;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Dominic Pimpinella
 */
@FacesConverter(forClass=User.class, value="userConverter")
public class UserConverter extends EntityConverter implements Converter {
    
    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String titleS) {
        if(titleS.isEmpty()) {
            return null; 
        }
        User o = (User) this.getViewMap(fc).get(titleS);
        return o;
    }
    
        @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {
        
        if (o == null){
            return "";
        }
        
        User u = (User) o;
        String userID = Integer.toString(u.getUserID());            
        if (userID != null){
            this.getViewMap(fc).put(userID,o);
            return userID;
            
        } else {
            return "muni error";
        }

        }
}
