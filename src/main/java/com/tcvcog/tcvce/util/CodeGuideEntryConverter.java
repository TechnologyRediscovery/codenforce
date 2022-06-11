/*
 * Copyright (C) 2019 Nathan Dietz
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

import com.tcvcog.tcvce.entities.BOBSource;
import com.tcvcog.tcvce.entities.BlobType;
import com.tcvcog.tcvce.entities.CodeElementGuideEntry;
import com.tcvcog.tcvce.entities.IntensityClass;
import com.tcvcog.tcvce.entities.IntensitySchema;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * Converts CodeGuideEntries to Strings and Back for UI
 * @author Ellen Bascomb of 31Y
 */
@FacesConverter(value = "codeGuideEntryConverter")
public class CodeGuideEntryConverter extends EntityConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String titleS) {
        if (titleS.isEmpty()) {
            return null;
        }
        CodeElementGuideEntry cege = (CodeElementGuideEntry) this.getViewMap(fc).get(titleS);
        return cege;
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {

        if (o == null) {
            return "";
        }

        CodeElementGuideEntry cege = (CodeElementGuideEntry) o;
        StringBuilder sb = new StringBuilder();
        sb.append(cege.getCategory());
        if(cege.getSubCategory() != null){
            sb.append(":");
            sb.append(cege.getSubCategory());
        }
        String title = sb.toString();
        if (title != null) {
            this.getViewMap(fc).put(title, o);
            return title;

        } else {
            return "code guide converter error";
        }

    }

}
