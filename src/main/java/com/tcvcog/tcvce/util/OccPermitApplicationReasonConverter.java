/*
 * Copyright (C) 2018 Turtle Creek Valley Council of Governments, PA
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

import com.tcvcog.tcvce.occupancy.entities.OccPermitApplicationReason;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * Converter to convert between String representations of OccPermitApplicationReason 
 * and object types
 * @author Dominic Pimpinella
 */
@FacesConverter(forClass=OccPermitApplicationReason.class, value="occPermitApplicationReasonConverter")
public class OccPermitApplicationReasonConverter extends EntityConverter implements Converter {
    
    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String titleS) {
        
        if(titleS.isEmpty()) {
            return null; 
        }
        
        OccPermitApplicationReason reason = (OccPermitApplicationReason) this.getViewMap(fc).get(titleS);
        return reason;
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {
        
        if (o == null){
            return "";
        }
        
        OccPermitApplicationReason reason = (OccPermitApplicationReason) o;
        String title = reason.getTitle();
        if (title != null){
            this.getViewMap(fc).put(title,o);
            return title;
            
        } else {
            return "OccPermitApplicationReason error";
        }
        
    }    
}
