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

import com.tcvcog.tcvce.entities.CEActionRequestStatus;
import com.tcvcog.tcvce.entities.CitationStatus;
import com.tcvcog.tcvce.entities.search.IFace_dateFieldHolder;
import com.tcvcog.tcvce.entities.search.IFace_userFieldHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author ellen bascomb of apt 31y
 */
@FacesConverter(value="userFieldConverter")
public class SearchParamsUserFieldConverter extends EntityConverter implements Converter{
    
     @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String titleS) {
        if(titleS.isEmpty()) {
            return null; 
        }
         IFace_userFieldHolder o = (IFace_userFieldHolder) this.getViewMap(fc).get(titleS);
        return o;
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {
        
        if (o == null){
            return "";
        }
        
        IFace_userFieldHolder usr = (IFace_userFieldHolder) o;
        String title = usr.extractUserFieldString();  
        if (title != null){
            this.getViewMap(fc).put(title,o);
            return title;
            
        } else {
            return "user converter error";
        }
        
    }
}
