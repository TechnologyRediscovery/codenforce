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

import com.tcvcog.tcvce.entities.CitationFilingType;
import com.tcvcog.tcvce.entities.CourtEntity;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.Municipality;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * Converter to convert between String representations of Municipalities 
 * and object types
 * @author echocharliedelta
 */
@FacesConverter(value="citationFilingTypeConverter")
public class CitationFilingTypeConverter extends EntityConverter implements Converter {
    
     @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String titleS) {
        if(titleS.isEmpty()) {
            return null; 
        }
        CitationFilingType o = (CitationFilingType) this.getViewMap(fc).get(titleS);
        return o;
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {
        
        if (o == null){
            return "";
        }
        
        CitationFilingType cft = (CitationFilingType) o;
        String title = cft.getTitle();
        if (title != null){
            this.getViewMap(fc).put(title,cft);
            return title;
            
        } else {
            return "muni error";
        }
        
        
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
}
