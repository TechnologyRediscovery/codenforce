/*
 * Copyright (C) 2019 Eric C. Darsow
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

import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.occupancy.OccChecklistTemplate;
import com.tcvcog.tcvce.entities.occupancy.OccSpaceTypeInspectionDirective;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author Dominic Pimpinella
 */
@FacesConverter(value="OccSpaceTypeInspectionDirectiveConverter")
public class OccSpaceTypeInspectionDirectiveConverter extends EntityConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String titleS) {
        if (titleS.isEmpty()) {
            return null;
        }
        OccSpaceTypeInspectionDirective o = (OccSpaceTypeInspectionDirective) this.getViewMap(fc).get(titleS);
        
        return o;
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {

        if (o == null) {
            return "";
        }

        OccSpaceTypeInspectionDirective u = (OccSpaceTypeInspectionDirective) o;
        String title = u.getSpaceTypeTitle();
        if (title  != null) {
            this.getViewMap(fc).put(title , o);
            return title ;

        } else {
            return "user converter error";
        }

    }
}
