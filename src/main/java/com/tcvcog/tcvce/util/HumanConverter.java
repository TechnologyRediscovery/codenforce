/*
 * Copyright (C) 2018 Turtle Creek Valley
Council of Governments, PA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tcvcog.tcvce.util;

import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.Human;
import com.tcvcog.tcvce.entities.Person;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

/**
 * Converter for Person objects
 * @author Eric Darsow
 */

@FacesConverter(value="humanConverter")
public class HumanConverter extends EntityConverter implements Converter{

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String hName) {
        if(hName.isEmpty()) {
            return null; 
        }
        Person p = (Person) this.getViewMap(fc).get(hName);
        return p;
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {
        
        if (o == null){
            return "";
        }
        Human h = (Human) o;
        String fullName = h.getName();
        if (fullName != null){
            this.getViewMap(fc).put(fullName,o);
            return fullName;
            
        } else {
            return "human conversion error";
        }
        
        
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
