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
import com.tcvcog.tcvce.entities.Person;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * Converter for EventCategory objects
 * @author Eric Darsow
 */

@FacesConverter(forClass=Person.class, value="personConverter")
public class PersonConverter extends EntityConverter implements Converter{

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String pName) {
        System.out.println("PersonConverter.getAsObject | name: " + pName);
        if(pName.isEmpty()) {
            return null; 
        }
        
        Person p = (Person) this.getViewMap(fc).get(pName);
        
        System.out.println("PersonConverter.getAsObject | Retrieved obj: " + p.getFirstName());
        
        return p;
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {
        
        if (o == null){
            return "";
        }
        
        System.out.println("PersonConverter.getAsString | incoming Object: " + o.toString() );
        Person p = (Person) o;
        String fullName = p.getFirstName() + " " +  p.getLastName();
        if (fullName != null){
            this.getViewMap(fc).put(fullName,o);
            
            System.out.println("PersonConverter.getAsString | fullName " + fullName );
            return fullName;
            
        } else {
            return "person conversion error";
        }
        
        
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
