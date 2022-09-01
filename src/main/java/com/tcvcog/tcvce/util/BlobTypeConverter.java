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
import com.tcvcog.tcvce.entities.IntensityClass;
import com.tcvcog.tcvce.entities.IntensitySchema;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

/**
 * Converts BlobTypes to Strings and Strings to BlobTypes
 * @author Ellen Bascomb of 31Y
 */
@FacesConverter(value = "blobTypeConverter")
public class BlobTypeConverter extends EntityConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String titleS) {
        if (titleS.isEmpty()) {
            return null;
        }

        BlobType bt = (BlobType) this.getViewMap(fc).get(titleS);


        return bt;
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {

        if (o == null) {
            return "";
        }

        BlobType bt = (BlobType) o;
        String title = bt.getTitle();
        if (title != null) {
            this.getViewMap(fc).put(title, o);
            return title;

        } else {
            return "blob type error";
        }

    }

}
