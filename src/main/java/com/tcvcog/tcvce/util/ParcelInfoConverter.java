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
import com.tcvcog.tcvce.entities.ParcelInfo;
import com.tcvcog.tcvce.entities.Property;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * Converter for ParcelInfo objects
 * @author Eric Darsow
 */

@FacesConverter(value="parcelInfoConverter")
public class ParcelInfoConverter extends EntityConverter implements Converter{

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String titleS) {
        if(titleS.isEmpty()) {
            return null; 
        }
        ParcelInfo pi = (ParcelInfo) this.getViewMap(fc).get(titleS);
        return pi;
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {
        if (o == null){
            return "";
        }
        
        ParcelInfo pi = (ParcelInfo) o;
        String title = String.valueOf(pi.getParcelInfoID());
        if (title != null){
            this.getViewMap(fc).put(title,o);
            return title;
            
        } else {
            return "parcel info error";
        }
        
        
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
