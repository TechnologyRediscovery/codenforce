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

import java.util.Objects;

/**
 *
 * @author Nathan Dietz
 */
public class IntensityClass implements Comparable<IntensityClass> {
    
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

    @Override
    public int compareTo(IntensityClass o) {
        int order = 0;
        if(o != null){
            if(this.numericRating < o.numericRating){
                order = 1;
            } else {
                order = -1;
            }
        }
        return order;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + this.classID;
        hash = 41 * hash + Objects.hashCode(this.title);
        hash = 41 * hash + Objects.hashCode(this.muni);
        hash = 41 * hash + this.numericRating;
        hash = 41 * hash + Objects.hashCode(this.schema);
        hash = 41 * hash + (this.active ? 1 : 0);
        hash = 41 * hash + Objects.hashCode(this.icon);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IntensityClass other = (IntensityClass) obj;
        if (this.classID != other.classID) {
            return false;
        }
        if (this.numericRating != other.numericRating) {
            return false;
        }
        if (this.active != other.active) {
            return false;
        }
        if (!Objects.equals(this.title, other.title)) {
            return false;
        }
        if (!Objects.equals(this.muni, other.muni)) {
            return false;
        }
        if (!Objects.equals(this.schema, other.schema)) {
            return false;
        }
        if (!Objects.equals(this.icon, other.icon)) {
            return false;
        }
        return true;
    }
    
    
    
}
