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

import java.util.List;
import java.util.Objects;

/**
 *
 * @author Nathan Dietz
 */
public class IntensitySchema{
    
    private String label;
    
    protected List<IntensityClass> classList;
    
    public IntensitySchema() {
        
    }
    
    public IntensitySchema(String label) {

    this.label = label;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 24 * hash + Objects.hashCode(this.label);
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
        final IntensitySchema other = (IntensitySchema) obj;
        if (!Objects.equals(this.label, other.getLabel())) {
            return false;
        }
        return true;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel(){
        return label;
    }

    /**
     * @return the classList
     */
    public List<IntensityClass> getClassList() {
        return classList;
    }

    /**
     * @param classList the classList to set
     */
    public void setClassList(List<IntensityClass> classList) {
        this.classList = classList;
    }

}
