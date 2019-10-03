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

import com.tcvcog.tcvce.entities.EventRuleSet;
import com.tcvcog.tcvce.entities.Icon;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * Converter to convert between String representations of Municipalities 
 * and object types
 * @author echocharliedelta
 */
@FacesConverter(value="eventRuleSetConverter")
public class EventRuleSetConverter extends EntityConverter implements Converter {
    
     @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String titleS) {
        if(titleS.isEmpty()) {
            return null; 
        }
        EventRuleSet ers = (EventRuleSet) this.getViewMap(fc).get(titleS);
        return ers;
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {
        
        if (o == null){
            return "";
        }
        
        EventRuleSet ers = (EventRuleSet) o;
        String title = ers.getTitle();
        if (title != null){
            this.getViewMap(fc).put(title,o);
            return title;
            
        } else {
            return "event rule set converter error";
        }
    }
}
