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
package com.tcvcog.tcvce.entities;

/**
 *
 * @author Nathan Dietz
 */
public class IntensityClass {
    
    private int classID;
    private String title;
    private Municipality muni;
    private int numericRating;
    private IntensitySchema schema;
    private boolean active;
    private Icon icon;
    
    public IntensityClass() {
        
        
    }

    public int getClassID() {
        return classID;
    }

    public void setClassID(int classID) {
        this.classID = classID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Municipality getMuni() {
        return muni;
    }

    public void setMuni(Municipality muni) {
        this.muni = muni;
    }

    public int getNumericRating() {
        return numericRating;
    }

    public void setNumericRating(int numericRating) {
        this.numericRating = numericRating;
    }

    public IntensitySchema getSchema() {
        return schema;
    }

    public void setSchema(IntensitySchema schema) {
        this.schema = schema;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }
    
}
